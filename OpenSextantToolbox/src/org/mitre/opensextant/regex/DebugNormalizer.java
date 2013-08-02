package org.mitre.opensextant.regex;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
public class DebugNormalizer implements Normalize {
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
