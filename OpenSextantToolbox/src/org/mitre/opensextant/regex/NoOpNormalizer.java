package org.mitre.opensextant.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

public class NoOpNormalizer implements Normalize {
  @Override
  public Map<String, Object> normalize(RegexRule r, MatchResult matchResult) {
    Map<String, Object> elementsFound = new HashMap<String, Object>();
    return elementsFound;
  }
}
