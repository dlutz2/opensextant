/** 
 Copyright 2009-2013 The MITRE Corporation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


 * **************************************************************************
 *                          NOTICE
 * This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
**/
package org.mitre.opensextant.extraction;

//import gate.Annotation;
import gate.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.mitre.opensextant.util.TextUtils;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.placedata.PlaceCandidate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a Solr sever via HTTP and tags place names in document.
 * The <code>SOLR_HOME</code> environment variable must be set to the location of the Solr server.
 * 
 * @author David Smiley - dsmiley@mitre.org
 * @author Marc Ubaldino - ubaldino@mitre.org
 */
public class SolrMatcher extends PlacenameMatcher {

    private final static String requestHandler = "/tag";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final boolean debug = log.isDebugEnabled();
    private final String APRIORI_NAME_RULE = "AprioriNameBias";
    private TaggerQueryRequest tag_request = null;
    private Map<Integer, Place> beanMap = new HashMap<>(100); // initial size
    /** In the interest of optimization we made the Solr instance 
     *  a static class attribute that should be thread safe and shareable across instances of SolrMatcher
     */
    private static SolrParams params = null;
    private static SolrProxy solr = null;
    private boolean allow_lowercase_abbrev = false;

    /**
     *
     * @throws IOException
     */
    public SolrMatcher() throws IOException {
        initialize();

        // Instance variable that will have the transient payload to tag
        // this is not thread safe and is not static:
        tag_request = new TaggerQueryRequest(params, SolrRequest.METHOD.POST);
    }

    public void setAllowLowerCaseAbbreviations(boolean b) {
        allow_lowercase_abbrev = b;
    }

    /** 
     */
    private static void initialize() throws IOException {
        if (solr != null) {
            return;
        }
        solr = new SolrProxy();

        ModifiableSolrParams _params = new ModifiableSolrParams();
        _params.set(CommonParams.QT, requestHandler);
        //request all fields in the Solr index
        // Do we need specific fields or *?  If faster use specific fields. TODO.
        //_params.set(CommonParams.FL, "*,score");
        // Note -- removed score for now, as we have not evaluated how score could be used in this sense.
        // Score depends on FST creation and other factors.
        // 
        // TODO: verify that all the right metadata is being retrieved here
        _params.set(CommonParams.FL, "id,name,cc,adm1,adm2,feat_class,feat_code,lat,lon,place_id,name_bias,id_bias,name_type");

        _params.set("tagsLimit", 20000);
        _params.set("subTags", false);
        _params.set("matchText", false);//we've got the input doc as a string instead

        params = _params;
    }

    /**
     *
     */
    @Override
    public void cleanup() {
        if (solr != null) {
            solr.close();
        }
    }

    /** Capture this */
    @SuppressWarnings("serial")
    class TaggerQueryRequest extends QueryRequest {

        public String input = null;

        public TaggerQueryRequest(SolrParams p, SolrRequest.METHOD m) {
            super(p, m);
        }

        @Override
        public Collection<ContentStream> getContentStreams() {
            return Collections.singleton((ContentStream) new ContentStreamBase.StringStream(input));
        }
    }

    /**
     * Tag a document, returning PlaceCandidates for the mentions in document.
     * Converts a GATE document to a string and passes it to the Solr server via 
     * HTTP POST. The tokens and featureName parameters are not used.
     *
     * @param doc The GATE document to be tagged
     * @param tokens 
     * @param featureName 
     * @return place_candidates List of place candidates
     * @throws MatcherException  
     */
    // "tagsCount":10, "tags":[{ "ids":[35], "endOffset":40, "startOffset":38},
    // { "ids":[750308, 2769912, 2770041, 10413973, 10417546], "endOffset":49,
    // "startOffset":41},
    // ...
    // "matchingDocs":{"numFound":75, "start":0, "docs":[ {
    // "place_id":"USGS1992921", "name":"Monterrey", "cc":"PR"}, {
    //"place_id":"USGS1991763", "name":"Monterrey", "cc":"PR"}, ]
    @Override
    public List<PlaceCandidate> tagDocument(Document doc, List<gate.Annotation> tokens,
            String featureName)
            throws MatcherException {

        // tokens ignored
        // featureName ignored
        // 
        return tagText(doc.getContent().toString(), doc.getName());
    }

    public List<PlaceCandidate> tagText(String buffer, String docid) throws MatcherException {

        if (debug) {
            log.debug("TEXT SIZE = " + buffer.length());
        }

        List<PlaceCandidate> candidates = new ArrayList<>();
        //Map<String, PlaceCandidate> lookup = new HashMap<String, PlaceCandidate>();

        //This is a little awkward because it is abnormal for a query to POST raw text
        /*
         @SuppressWarnings("serial")
         QueryRequest request = new QueryRequest(params, SolrRequest.METHOD.POST) {
         @Override
         public Collection<ContentStream> getContentStreams() {
         return Collections.singleton((ContentStream) new ContentStreamBase.StringStream(docContent));
         }
         }; */

        // Setup request to tag... 
        tag_request.input = buffer;
        QueryResponse response = null;
        try {
            response = tag_request.process(solr.getInternalSolrServer());
        } catch (Exception err) {
            throw new MatcherException("Failed to tag document=" + docid, err);
        }

        // -- Process Solr Response

        //List<GeoBean> geoBeans = response.getBeans(GeoBean.class); maybe works but probably slow
        SolrDocumentList docList = (SolrDocumentList) response.getResponse().get("matchingDocs");
        beanMap.clear();
        for (SolrDocument solrDoc : docList) {
            Place bean = new Place();

            bean.setName_type(SolrProxy.getChar(solrDoc, "name_type"));

            // Gazetteer place name & country:
            //   NOTE: this may be different than "matchtext" or PlaceCandidate.name field.
            // 
            bean.setPlaceName(SolrProxy.getString(solrDoc, "name"));
            bean.setCountryCode(SolrProxy.getString(solrDoc, "cc"));

            // Other metadata.
            bean.setAdmin1(SolrProxy.getString(solrDoc, "adm1"));
            bean.setAdmin2(SolrProxy.getString(solrDoc, "adm2"));
            bean.setFeatureClass(SolrProxy.getString(solrDoc, "feat_class"));
            bean.setFeatureCode(SolrProxy.getString(solrDoc, "feat_code"));
            bean.setLatitude(SolrProxy.getDouble(solrDoc, "lat"));
            bean.setLongitude(SolrProxy.getDouble(solrDoc, "lon"));

            //bean.setSourceNameID(SolrProxy.getString(solrDoc, "SOURCE_NAME_ID"));
            //bean.setSourceFeatureID(SolrProxy.getString(solrDoc, "SOURCE_FEATURE_ID"));
            bean.setPlaceID(SolrProxy.getString(solrDoc, "place_id"));
            bean.setName_bias(SolrProxy.getDouble(solrDoc, "name_bias"));
            bean.setId_bias(SolrProxy.getDouble(solrDoc, "id_bias"));

            // Hashed on "id"
            Integer id = (Integer) solrDoc.getFirstValue("id");
            beanMap.put(id, bean);
        }

        @SuppressWarnings("unchecked")
        List<NamedList<?>> tags = (List<NamedList<?>>) response.getResponse().get("tags");

        if (debug) {
            log.debug("DOC=" + docid + " TAGS SIZE = " + tags.size());
        }

        /*
         * Retrieve all offsets into a long list.  These offsets will report
         * a text span and all the gazetteer record IDs that are associated to that span.
         * The text could either be a name, a code or some other abbreviation.
         * 
         * For practical reasons the default behavior is to filter trivial spans given 
         * the gazetteer data that is returned for them.
         * 
         * WARNING: lots of optimizations occur here due to the potentially large volume of tags
         * and gazetteer data that is involved.  And this is relatively early in the pipline.
         * 
         */
        PlaceCandidate pc;
        Place Pgeo;
        int x1 = -1, x2 = -1;
        Set<String> seenPlaces = new HashSet<>();
        Double name_bias = 0.0;

        for (NamedList<?> tag : tags) {
            pc = new PlaceCandidate();
            x1 = (Integer) tag.get("startOffset");
            x2 = (Integer) tag.get("endOffset");//+1 char after last matched
            pc.setStart(x1);
            pc.setEnd(x2);

            // Could have enabled the "matchText" option from the tagger to get
            // this, but since we already have the content as a String then
            // we might as well not make the tagger do any more work.
            pc.setPlaceName(buffer.substring(x1, x2)); //
            name_bias = 0.0;

            @SuppressWarnings("unchecked")
            List<Integer> placeRecordIds = (List<Integer>) tag.get("ids");
            //clear out places seen for the next candidate
            seenPlaces.clear();
            boolean _is_valid = true;
            boolean _is_lower = StringUtils.isAllLowerCase(pc.getText());

            for (Integer solrId : placeRecordIds) {
                Pgeo = beanMap.get(solrId);
                if (Pgeo == null) {
                    if (debug) {
                        log.debug("Logic error. Did not find place object for Solr ID=" + solrId);
                    }
                    continue;
                }

                // Optimization:  abbreviation filter.
                // 
                // Do not add PlaceCandidates for lower case tokens that are marked as Abbreviations
                // Unless flagged to do so.
                // DEFAULT behavior is to avoid lower case text that is tagged as an abbreviation in gazetteer,
                // 
                // Common terms:  in, or, oh, me, us, we, 
                //   etc.
                // Are all not typically place names or valid abbreviations in text.
                //                 
                if (!allow_lowercase_abbrev) {
                    if (Pgeo.isAbbreviation() && _is_lower) {
                        _is_valid = false;
                        if (debug) {
                            log.debug("Ignore lower case term="+pc.getText());
                        }
                        
                        break;
                    }

                }
                // Optimization: Add distinct place objects once. 
                //   don't add Place if null or already added to this instance of PlaceCandidate
                // 
                if (!seenPlaces.contains(Pgeo.getPlaceID())) {
                    pc.addPlace(Pgeo);
                    seenPlaces.add(Pgeo.getPlaceID());

                    // get max name bias
                    Double n_bias = Pgeo.getName_bias();
                    if (n_bias > name_bias) {
                        name_bias = n_bias;
                    }
                }

                // Indeed this does happen.
                // else { log.info("Does this ever happen -- ? " + pc.getText() + " " + Pgeo.getPlaceName()); }
            }

            /** Some rule above triggered a flag that indicates this token/place/name is not valid.
             *  
             */
            if (!_is_valid) {
                continue;
            }

            // if the max name bias seen >0; add apriori evidence
            if (name_bias != 0.0) {
                pc.addRuleAndConfidence(APRIORI_NAME_RULE, name_bias);
            }

            candidates.add(pc);
        }

        if (debug) {
            summarizeExtraction(candidates, docid);
        }

        return candidates;
    }

    /** Debugging */
    private void summarizeExtraction(List<PlaceCandidate> candidates, String docid) {
        log.debug("DOC=" + docid + " PLACE CANDIDATES SIZE = " + candidates.size());
        Map<String, Integer> countries = new HashMap<>();

        // This loops through findings and reports out just Country names for now.
        for (PlaceCandidate candidate : candidates) {
            boolean _break = false;
            String namekey = TextUtils.normalize_text_entity(candidate.getText()); // .toLowerCase();
            namekey = namekey.toLowerCase();

            for (Place p : candidate.getPlaces()) {
                if (p.isAbbreviation()) {
                    log.debug("Ignore all abbreviations for now " + candidate.getText());
                    _break = true;
                    break;
                }
                if (p.isCountry()) {
                    Integer count = countries.get(namekey);
                    if (count == null) {
                        count = new Integer(1);
                        countries.put(namekey, count);
                    }
                    ++count;
                    countries.put(namekey, count);
                    _break = true;
                    break;
                }
            }
            if (_break) {
                continue;
            }
        }
        log.debug("Countries found:" + countries.toString());
    }

    /**
     * Do a basic test
     */
    public static void main(String[] args) throws Exception {
        //String solrHome = args[0];

        SolrMatcher sm = new SolrMatcher();

        try {
            String docContent = "I want to go to New York City some day.";

            System.out.println(docContent);

            List<PlaceCandidate> matches = sm.tagText(docContent, "main-test");

            for (PlaceCandidate pc : matches) {
                System.out.println(pc.toString());
            }

            sm.cleanup();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
