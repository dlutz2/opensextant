package org.mitre.opensextant.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.placedata.PlaceCandidate;

public class PlacenameMatcher {

	SolrServer s;
	private ModifiableSolrParams SearchParams = new ModifiableSolrParams();
	private ModifiableSolrParams matchParams = new ModifiableSolrParams();
	private static final String requestHandler = "/tag";
	private final String APRIORI_NAME_RULE = "AprioriNameBias";
	private SolrTaggerRequest tag_request = null;
	private Map<Integer, Place> placeIDMap = new HashMap<Integer, Place>(100);
	private boolean tagAbbrev = false;

	protected PlacenameMatcher(SolrServer svr) {
		this.s = svr;

		SearchParams.set(CommonParams.Q, "*:*");
		SearchParams
				.set(CommonParams.FL,
						"id,name,cc,adm1,adm2,feat_class,feat_code,geo,place_id,name_bias,id_bias,name_type");

		matchParams.set(CommonParams.QT, requestHandler);
		matchParams
				.set(CommonParams.FL,
						"id,name,cc,adm1,adm2,feat_class,feat_code,geo,place_id,name_bias,id_bias,name_type");
		matchParams.set("tagsLimit", 100000);
		matchParams.set(CommonParams.ROWS, 100000);
		matchParams.set("subTags", false);
		matchParams.set("matchText", false);
		matchParams.set("overlaps", "LONGEST_DOMINANT_RIGHT");

	}

	public void tagAbbreviations(boolean b) {
		tagAbbrev = b;
	}

	public List<PlaceCandidate> matchText(String buffer, String docName) {

		List<PlaceCandidate> candidates = new ArrayList<PlaceCandidate>();
		// Setup request to tag...
		tag_request = new SolrTaggerRequest(matchParams,
				SolrRequest.METHOD.POST);
		tag_request.input = buffer;

		QueryResponse response = null;

		try {
			response = tag_request.process(s);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// -- Process Solr Response

		SolrDocumentList docList = (SolrDocumentList) response.getResponse().get("matchingDocs");

		placeIDMap.clear();

		for (SolrDocument solrDoc : docList) {
			Integer id = (Integer) solrDoc.getFirstValue("id");
			Place place = MatcherFactory.createPlace(solrDoc);
			placeIDMap.put(id, place);
		}

		@SuppressWarnings("unchecked")
		List<NamedList<?>> tags = (List<NamedList<?>>) response.getResponse()
				.get("tags");

		PlaceCandidate pc = null;
		// Place place = null;
		int x1 = -1, x2 = -1;
		Set<String> seenPlaces = new HashSet<String>();
		Double name_bias = 0.0;
		String matchText = null;
		for (NamedList<?> tag : tags) {
			x1 = (Integer) tag.get("startOffset");
			x2 = (Integer) tag.get("endOffset"); // +1 char after last matched
			matchText = buffer.substring(x1, x2);

			pc = new PlaceCandidate();
			pc.setStart(x1);
			pc.setEnd(x2);

			pc.setPlaceName(matchText);
			name_bias = 0.0;
			@SuppressWarnings("unchecked")
			List<Integer> placeRecordIds = (List<Integer>) tag.get("ids");
			// clear out places seen for the next candidate
			seenPlaces.clear();
			boolean isValid = true;
			boolean isLower = StringUtils.isAllLowerCase(pc.getText());

			for (Integer solrId : placeRecordIds) {

				Place place = placeIDMap.get(solrId);

				// Optimization: abbreviation filter.
				//
				// Do not add PlaceCandidates for lower case tokens that are
				// marked as Abbreviations
				// Unless flagged to do so.
				// DEFAULT behavior is to avoid lower case text that is tagged
				// as an abbreviation in gazetteer,
				//

				//
				if (!tagAbbrev) {
					if (place.isAbbreviation() && isLower) {
						isValid = false;
						break;
					}
				}

				// Optimization: Add distinct place objects once.
				// don't add Place if null or already added to this instance of
				// PlaceCandidate
				//
				if (!seenPlaces.contains(place.getPlaceID())) {
					pc.addPlace(place);
					seenPlaces.add(place.getPlaceID());
					// get max name bias
					Double n_bias = place.getName_bias();
					if (n_bias > name_bias) {
						name_bias = n_bias;
					}
				}
			}
			if (!isValid || !pc.hasPlaces()) {
				continue;
			}
			// if the max name bias seen >0; add apriori evidence
			if (name_bias != 0.0) {
				pc.addRuleAndConfidence(APRIORI_NAME_RULE, name_bias);
			}
			candidates.add(pc);
		}
		return candidates;

	}

	public List<Place> search(String placeName) {

		List<Place> places = new ArrayList<Place>();
		SearchParams.set("q", placeName);

		QueryResponse response = null;
		try {
			response = s.query(SearchParams);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response != null) {
			SolrDocumentList docList = response.getResults();
			for (SolrDocument d : docList) {
				Place p = MatcherFactory.createPlace(d);
				places.add(p);
			}
		}
		return places;

	}

	void cleanup() {
		MatcherFactory.shutdown(this);
	}
}
