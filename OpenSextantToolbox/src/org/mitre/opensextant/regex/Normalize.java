package org.mitre.opensextant.regex;
import java.util.Map;
import java.util.regex.MatchResult;
public interface Normalize {
	public abstract Map<String, Object> normalize(RegexRule r,
			MatchResult matchResult);
}