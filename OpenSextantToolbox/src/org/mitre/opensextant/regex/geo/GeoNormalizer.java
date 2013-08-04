package org.mitre.opensextant.regex.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

import org.mitre.opensextant.placedata.Geocoord;
import org.mitre.opensextant.regex.Normalize;
import org.mitre.opensextant.regex.RegexRule;
import org.opensextant.geodesy.Geodetic2DPoint;
import org.opensextant.geodesy.MGRS;
import org.opensextant.geodesy.UTM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoNormalizer implements Normalize {
  
  // Log object
  private static Logger log = LoggerFactory.getLogger(GeoNormalizer.class);
  
  
  
  @Override
  public Map<String, Object> normalize(RegexRule r, MatchResult matchResult) {
    Map<String, Object> normalizedResults = new HashMap<String, Object>();
    Map<String, String> elementsFound = new HashMap<String, String>();
    int numGroups = matchResult.groupCount();
    for (int i = 0; i < numGroups + 1; i++) {
      // int s = matchResult.start(i);
      // int e = matchResult.end(i);
      String elemenValue = matchResult.group(i);
      String elemName = r.getElementMap().get(i);
      elementsFound.put(elemName, elemenValue);
    }
    String textMatch = matchResult.group(0);
    normalizedResults.put("hierarchy", "Geo.place.geocoordinate");
    normalizedResults.put("isEntity", true);
    // the rule family will tell us the set of elements to expect
    String family = r.getRuleFamily();
    // there are 5 geocoord families
    String DD_family = "DD";
    String DM_family = "DM";
    String DMS_family = "DMS";
    String MGRS_family = "MGRS";
    String UTM_family = "UTM";
    if (family.equals(DD_family)) {
      DMSOrdinate ddlat;
      DMSOrdinate ddlon;
      ddlat = new DMSOrdinate(elementsFound, true, textMatch);
      ddlon = new DMSOrdinate(elementsFound, false, textMatch);
      Geocoord geo = new Geocoord(ddlat.getValue(), ddlon.getValue());
      normalizedResults.put("geo", geo);
    }
    if (family.equals(DM_family)) {
      DMSOrdinate dmlat;
      DMSOrdinate dmlon;
      dmlat = new DMSOrdinate(elementsFound, true, textMatch);
      dmlon = new DMSOrdinate(elementsFound, false, textMatch);
      Geocoord geo = new Geocoord(dmlat.getValue(), dmlon.getValue());
      normalizedResults.put("geo", geo);
    }
    if (family.equals(DMS_family)) {
      DMSOrdinate dmslat;
      DMSOrdinate dmslon;
      dmslat = new DMSOrdinate(elementsFound, true, textMatch);
      dmslon = new DMSOrdinate(elementsFound, false, textMatch);
      Geocoord geo = new Geocoord(dmslat.getValue(), dmslon.getValue());
      normalizedResults.put("geo", geo);
    }
    if (family.equals(MGRS_family)) {
      List<MGRS> mgrs_candidates = MGRSParser.parseMGRS(textMatch, textMatch, elementsFound);
      if (mgrs_candidates != null && !mgrs_candidates.isEmpty()) {
        MGRS mgrs = mgrs_candidates.get(0);
        Geodetic2DPoint pt = mgrs.toGeodetic2DPoint();
        Geocoord geo = new Geocoord(pt.getLatitudeAsDegrees(), pt.getLongitudeAsDegrees());
        normalizedResults.put("geo", geo);
        List<Geocoord> altCoords = new ArrayList<Geocoord>();
        if (mgrs_candidates.size() > 1) {
          for (MGRS m : mgrs_candidates) {
            Geodetic2DPoint pt2 = m.toGeodetic2DPoint();
            Geocoord geo2 = new Geocoord(pt2.getLatitudeAsDegrees(), pt2.getLongitudeAsDegrees());
            altCoords.add(geo2);
          }
          normalizedResults.put("geoAlternatives", geo);
        }
      }
    }
    if (family.equals(UTM_family)) {
      UTM utm = UTMParser.parseUTM(textMatch, elementsFound);
      if (utm != null) {
        Geodetic2DPoint pt = utm.getGeodetic();
        Geocoord geo = new Geocoord(pt.getLatitudeAsDegrees(), pt.getLongitudeAsDegrees());
        normalizedResults.put("geo", geo);
      }
    }
    return normalizedResults;
  }
}
