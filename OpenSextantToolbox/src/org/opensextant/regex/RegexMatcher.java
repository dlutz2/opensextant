package org.opensextant.regex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexMatcher {
  // the rules used by this RegexMatcher
  List<RegexRule> rules = new ArrayList<RegexRule>();
  // the list of entity type this matcher can find
  Set<String> types = new HashSet<String>();
  
  // future stuff: named groups useful?
  // String namedGroup = "\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>";
  // Pattern namedGroupPattern = Pattern.compile(namedGroup);
  
  // the pattern of a DEFINE within a RULE e.g "<somePiece>"
  // NOTE: should we look for existing (not DEFINEDd) capture groups
  String elementRegex = "<[a-zA-Z0-9_]+>";
  Pattern elementPattern = Pattern.compile(elementRegex);

  // Log object
  private static Logger log = LoggerFactory.getLogger(RegexMatcher.class);
  
  public RegexMatcher(URL patternFile) {
    initialize(patternFile);
  }

  public RegexMatcher(File patternFile) {
    initialize(patternFile);
  }

  public ArrayList<RegexAnnotation> match(String input) {
    // The matches to return
    ArrayList<RegexAnnotation> matches = new ArrayList<RegexAnnotation>();
    for (RegexRule r : rules) {
      String t = r.getEntityType();
      Normalizer normer = r.getNormalizer();
      // Do the matching, looping over the rules
      Matcher matcher = r.getPattern().matcher(input);
      while (matcher.find()) {
        // for each hit from the regex, create a RegexAnnotation
        RegexAnnotation tmp = new RegexAnnotation(t, matcher.group(0), matcher.start(), matcher.end());
        // add the "hierarchy" feature
        tmp.getFeatures().put("hierarchy", r.getTaxo());
        
        // if the a normalizer has been specified, 
        if (normer != null) {
          normer.normalize(tmp, r, matcher.toMatchResult());
        }
        
        // check to see if the normalizer declared the match invalid
        if(tmp.isValid()){
         matches.add(tmp);
        }
      }
    }
    return matches;
  }

  public void initialize(URL patFile) {
    // the #DEFINE statements as name and regex
    HashMap<String, String> defines = new HashMap<String, String>();
    // the #RULE statements as name and a sequence of DEFINES and regex bits
    // HashMap<String, String> rulesLines = new HashMap<String, String>();
    // the #CLASS statements as entitytype and class
    HashMap<String, String> normalizerClassnames = new HashMap<String, String>();
    // the #TAXO statements as entitytype and taxonomy string
    HashMap<String, String> taxos = new HashMap<String, String>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(patFile.openStream(), "UTF-8"));
    } catch (UnsupportedEncodingException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return;
    }

    if (reader == null) {
      // log err
      return;
    }
    String _line = null;
    String[] fields;
    while (true) {
      try {
        _line = reader.readLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (_line == null) {
        break;
      }
      String line = _line.trim();
      // Is it a define statement?
      if (line.startsWith("#DEFINE")) {
        // line should be
        // #DEFINE<tab><defineName><tab><definePattern>
        fields = line.split("[\t ]+", 3);
        defines.put(fields[1].trim(), fields[2].trim());
      } else if (line.startsWith("#RULE")) {// Is it a rule statement?
        // line should be
        // #RULE<tab><entityType><tab><rule_fam><tab><rule_name><tab><pattern>
        fields = line.split("[\t ]+", 5);
        String type = fields[1].trim();
        String fam = fields[2].trim();
        String ruleName = fields[3].trim();
        String rulePattern = fields[4].trim();
        RegexRule tmpRule = new RegexRule();
        tmpRule.setEntityType(type);
        tmpRule.setRuleFamily(fam);
        tmpRule.setRuleName(ruleName);
        tmpRule.setPatternString(rulePattern);
        rules.add(tmpRule);
        types.add(type);
      } else if (line.startsWith("#CLASS")) {
        fields = line.split("[\t ]+", 3);
        String type = fields[1].trim();
        normalizerClassnames.put(type, fields[2].trim());
      } else if (line.startsWith("#TAXO")) {
        fields = line.split("[\t ]+", 3);
        String type = fields[1].trim();
        taxos.put(type, fields[2].trim());
      }
      // Ignore everything else
    } // end file read loop
    try {
      if (reader != null) {
        reader.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // defines,rules and classes should be completely populated
    // substitute all uses of DEFINE patterns within a RULE
    // with the DEFINE pattern surrounded by a numbered capture group
    for (RegexRule r : rules) {
      String tmpRulePattern = r.getPatternString();
      Matcher elementMatcher = elementPattern.matcher(tmpRulePattern);
      // find all of the element definitions within the pattern
      int groupNum = 1;
      // add the entity type as the name for group 0 (whole match)
      r.getElementMap().put(0, r.getEntityType());
      // find and replace any DEFINEd patterns, keeping track of the group
      // number
      // first find any DEFINEd patterns and stick it and its group number
      // into the
      // rule element map
      while (elementMatcher.find()) {
        int elementStart = elementMatcher.start();
        int elementEnd = elementMatcher.end();
        String elementName = tmpRulePattern.substring(elementStart + 1, elementEnd - 1);
        r.getElementMap().put(groupNum, elementName);
        groupNum++;
      }
      // now, replace each of the DEFINEd patterns with its regex
      // equivalent
      // wrapped in ( ), so it becomes a numbered capture group
      for (String tmpDefineName : defines.keySet()) {
        String tmpDefinePattern = "(" + defines.get(tmpDefineName) + ")";
        tmpDefineName = "<" + tmpDefineName + ">";
        tmpRulePattern = tmpRulePattern.replace(tmpDefineName, tmpDefinePattern);
      }
      // set the modified pattern on the rule and create the Pattern from
      // it
      r.setModifedPatternString(tmpRulePattern);
      r.setPattern(Pattern.compile(r.getModifedPatternString()));
      // resolve and attach the normalizer object
      if (normalizerClassnames.containsKey(r.getEntityType())) {
        String normClassName = normalizerClassnames.get(r.getEntityType());
        Normalizer normer;
        try {
          normer = (Normalizer) Class.forName(normClassName).newInstance();
        } catch (InstantiationException e) {
          normer = new NoOpNormalizer();
          // log.error("Cannot instantiate a " + normClassName)
        } catch (IllegalAccessException e) {
          normer = new NoOpNormalizer();
          // log.error("Cannot access a " + normClassName +
          // " to create one" )
        } catch (ClassNotFoundException e) {
          normer = new NoOpNormalizer();
          // log.error("Class " + normClassName + " not found")
        } catch (java.lang.ClassCastException e) {
          normer = new NoOpNormalizer();
          // log.error("Class " + normClassName +
          // " does not implement Normalizer")
        }
        r.setNormalizer(normer);
      } else { // nothing in file use NoOpNormalzer
        r.setNormalizer(new NoOpNormalizer());
      }
      // resolve and attach the taxonomic string object
      if (taxos.containsKey(r.getEntityType())) {
        r.setTaxo(taxos.get(r.getEntityType()));
      } else { // nothing in file
        r.setTaxo("");
      }
    }
  } // end initialize

  public void initialize(File patFile) {
    try {
      initialize(patFile.toURI().toURL());
    } catch (MalformedURLException e) {
      // log error
    }
  }

  public List<RegexRule> getRules() {
    return rules;
  }

  public Set<String> getTypes() {
    return types;
  }

  public static void main(String[] args) throws IOException {
    File patternFile = new File(args[0]);
    File inputFile = new File(args[1]);
    // contents to be tagged
    String content = FileUtils.readFileToString(inputFile, "UTF-8");
    // initialize the matcher
    RegexMatcher reger = new RegexMatcher(patternFile);
    // what can the matcher find
    System.out.println("This tagger can find " + reger.types);
    for (RegexRule r : reger.rules) {
      System.out.println(r);
    }
    // Create tokens and print them
    ArrayList<RegexAnnotation> annos = reger.match(content);
    for (RegexAnnotation anno : annos) {
      System.out.println(anno);
    }
  }
}
