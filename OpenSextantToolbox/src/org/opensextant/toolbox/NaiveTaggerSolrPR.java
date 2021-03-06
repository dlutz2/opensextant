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
package org.opensextant.toolbox;

import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

import java.util.List;

import org.opensextant.matching.MatcherFactory;
import org.opensextant.matching.PlacenameMatcher;
import org.opensextant.placedata.PlaceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Solr-based ProcessingResource that tags mentions of geospatial candidates found in a dcoument. The
 * <code>SOLR_HOME</code> environment variable must be set to the location of the Solr server. *
 * @author David Smiley, MITRE, dsmiley@mitre.org
 * @author Marc Ubaldino, MITRE, ubaldino@mitre.org *
 */
@CreoleResource(name = "OpenSextant NaiveTaggerSolr", comment = "A Solr-based tagger")
public class NaiveTaggerSolrPR extends AbstractLanguageAnalyser implements ProcessingResource {
  /**
     *
     */
  private static final long serialVersionUID = -6167312014577862928L;
  // Log object
  static Logger log = LoggerFactory.getLogger(NaiveTaggerSolrPR.class);
  private PlacenameMatcher matcher;
  private String outputASName;
  private String annotationType;
  // The parameters passed in by the user
  String inputASName; // The name of the input AnnotationSet
  Boolean tagAbbreviations; // tag placenames which are abreviations or codes
  // TODO expose CALIBRATE and CALIBRATE_SCORE as PR parameters
  // to force all confidences to CALIBRATE_SCORE for calibration
  boolean CALIBRATE = false;
  Double CALIBRATE_SCORE = 0.0;

  /**
   *
   * @return gate_resource
   * @throws ResourceInstantiationException
   */
  @Override
  public Resource init() throws ResourceInstantiationException {
    super.init();
    
    matcher = MatcherFactory.getMatcher();
    if(matcher == null){
      log.error("Could not get a matcher from MatcherFactory. Not configured?");
      return this;
    }
    matcher.tagAbbreviations(tagAbbreviations);
    return this;
  }

  /**
     *
     */
  @Override
  public void cleanup() {
    super.cleanup();
  }

  /**
   * Uses a SolrMatcher object to tag place names. Key elements:
   *
   * <pre>
   * + SOLR_HOME -- see the Gazetteer/solr folder for the data index used here
   * + PlacenameMatcher -- PlaceNameMatcher has the logic for matching; SolrProxy
   *    is used to broker interaction with the Solr server at SOLR_HOME.
   *    If Given a URL, SolrMatcher will attempt to use a solr server via http -- this is not common
   * + PlaceNameMatcher -- wraps SolrProxy, which brokers access
   * + MatcherException -- error to throw for low level matching implementation
   * </pre>
   *
   * @throws ExecutionException
   */
  @Override
  public void execute() throws ExecutionException {
    if (matcher == null) {
      throw new IllegalStateException("This PR hasn't been init'ed!");
    }
    List<PlaceCandidate> matches = null;
    try {
      matches = matcher.matchText(document.getContent().toString(), document.getName());
    } catch (Exception err) {
      log.error("Error when tagging document " + document.getName(), err);
      return;
    }
    // If no output Annotation set was given, append to the input AS
    AnnotationSet annotSet = (output_as_name ? document.getAnnotations(outputASName) : document.getAnnotations());

    for (PlaceCandidate pc : matches) {
      // create and populate the PlaceCandidate annotation
      FeatureMap feats = Factory.newFeatureMap();
      feats.put("string", pc.getPlaceName());
      feats.put("placeCandidate", pc);
      if (CALIBRATE) {
        pc.setPlaceConfidenceScore(CALIBRATE_SCORE);
      }
      try {
        annotSet.add(pc.getStart(), pc.getEnd(), annotationType, feats);
      } catch (InvalidOffsetException offsetErr) {
        // Silent.
        // Should do something more interesting.
      }
    }
  }

  /**
   *
   * @return
   */
  public String getInputASName() {
    return inputASName;
  }

  /**
   *
   * @param inputASName
   */
  @Optional
  @RunTime
  @CreoleParameter
  public void setInputASName(String inputASName) {
    this.inputASName = inputASName;
  }

  /**
   *
   * @return
   */
  public String getOutputASName() {
    return outputASName;
  }

  /**
   *
   * @return
   */
  public String getAnnotationType() {
    return annotationType;
  }

  /**
   *
   * @param annotationType
   */
  @Optional
  @RunTime
  @CreoleParameter(defaultValue = "placecandidate")
  public void setAnnotationType(String annotationType) {
    this.annotationType = annotationType;
  }

  private boolean output_as_name = false;

  /**
   *
   * @param outputASName
   */
  @Optional
  @RunTime
  @CreoleParameter
  public void setOutputASName(String outputASName) {
    this.outputASName = outputASName;
    output_as_name = (outputASName != null && !outputASName.isEmpty());
  }

  public Boolean getTagAbbreviations() {
    return tagAbbreviations;
  }

  @Optional
  @CreoleParameter(defaultValue = "false")
  public void setTagAbbreviations(Boolean tagAbbreviations) {
    this.tagAbbreviations = tagAbbreviations;
  }
}
