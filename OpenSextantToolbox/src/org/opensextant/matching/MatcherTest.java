package org.opensextant.matching;

import java.util.List;

import org.opensextant.placedata.PlaceCandidate;

public class MatcherTest {

  /**
   * @param args
   */
  public static void main(String[] args) {

    MatcherFactory.config("C:\\mitre_loose\\opensextant");
    MatcherFactory.start();

    PlacenameMatcher m = MatcherFactory.getMatcher();

    // List<Place> places = m.search("London");
    // for (Place p : places) {
    // System.out.println(p);
    // }

    m.tagAbbreviations(true);
    List<PlaceCandidate> cands = m.matchText("We drove over London Bridge, which is in Springfield,IN", "test text");

    System.out.println("true =->" + cands.size());
    for (PlaceCandidate pc : cands) {
      System.out.println("\t" + pc.getPlaceName());
    }

    m.tagAbbreviations(false);
    List<PlaceCandidate> cands2 = m.matchText("We drove over London Bridge, which is in Springfield,IN", "test text");

    System.out.println("false =->" + cands2.size());
    for (PlaceCandidate pc : cands2) {
      System.out.println("\t" + pc.getPlaceName());
    }

    m.cleanup();

  }

}
