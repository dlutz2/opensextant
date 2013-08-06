package org.opensextant.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugNormalizer implements Normalize {
  
  // Log object
  private static Logger log = LoggerFactory.getLogger(DebugNormalizer.class);
  @Override
  public Map<String, Object> normalize(RegexRule r, MatchResult matchResult) {
    Map<String, Object> elementsFound = new HashMap<String, Object>();
    int numGroups = matchResult.groupCount();
    for (int i = 0; i < numGroups + 1; i++) {
      // int s = matchResult.start(i);
      // int e = matchResult.end(i);
      String elemenValue = matchResult.group(i);
      String elemName = r.getElementMap().get(i);
      elementsFound.put(elemName, elemenValue);
    }
    elementsFound.put("entityType", r.getEntityType());
    elementsFound.put("ruleFamily", r.getRuleFamily());
    elementsFound.put("ruleName", r.getRuleName());
    return elementsFound;
  }
}
