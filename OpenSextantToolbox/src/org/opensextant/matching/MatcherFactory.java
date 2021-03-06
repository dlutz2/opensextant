package org.opensextant.matching;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.core.CoreContainer;
import org.opensextant.placedata.Place;
import org.opensextant.solrtexttagger.NoSerializeEmbeddedSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatcherFactory {

  private static String envParam = "solr.home";

  // string specifying solr home, could be file path or URL
  private static String homeLocation;

  // states of solr server and thus the MatcherFactory
  private static boolean isRemote = false;
  private static boolean isConfigured = false;
  private static boolean isStarted = false;

  // the solr server which is the heart of the MatcherFactory
  private static SolrServer solrServer = null;

  // all of the Matchers the Factory has created
  // weak references so they can be GC'ed when
  static Map<PlacenameMatcher, Boolean> matchers = new WeakHashMap<PlacenameMatcher, Boolean>();

  // Log object
  private static Logger log = LoggerFactory.getLogger(MatcherFactory.class);

  public static void config(String home) {

    if (isStarted) {
      // already running
      return;
    }

    // not running but already configured
    if (isConfigured) {
      // re-configuring
      isRemote = false;
      isConfigured = false;
      isStarted = false;
      solrServer = null;
    }

    // defaulting to system env param
    if (home == null || home.length() == 0) {
      String solrEnv = System.getenv(envParam);
      if (solrEnv != null && solrEnv.length() > 0) {
        if (validFile(solrEnv)) {
          isRemote = false;
          homeLocation = solrEnv;
          isConfigured = true;
          return;
        }
      } else {
        // nothing given and nothing in env
        isConfigured = false;
        return;
      }
    }

    // remote solr
    if (home.toLowerCase().startsWith("http:") && validURL(home)) {
      isConfigured = true;
      isRemote = true;
      homeLocation = home;
      isConfigured = true;
      return;
    }

    // local using file URL
    if (home.toLowerCase().startsWith("file:") && validURL(home)) {

      URL tmpURL = null;
      try {
        tmpURL = new URL(home);
      } catch (MalformedURLException e) {
        // how could this happen we just checked it was valid
        log.error("Malformed URL in initializing MatcherFactory:" + tmpURL);
        return;
      }

      String filePath = tmpURL.getPath();
      if (validFile(filePath)) {
        homeLocation = filePath;
      } else {
        return;
      }

      isRemote = false;
      isConfigured = true;
      return;
    }

    // anything else, local using file path
    if (validFile(home)) {
      isRemote = false;
      homeLocation = home;
      isConfigured = true;
      return;
    }

    // fell through no valid config
    homeLocation = "";
    isConfigured = false;

    return;

  }

  public static void start() {

    if (isConfigured) {

      if (isRemote) {
        HttpSolrServer server = new HttpSolrServer(homeLocation);
        server.setAllowCompression(true);
        // test to see if it is really working?
        // server.ping();
        solrServer = server;
        isStarted = true;
      } else {
        try {
          File solr_xml = new File(homeLocation + File.separator + "solr.xml");
          CoreContainer solrContainer = new CoreContainer(homeLocation);
          solrContainer.load(homeLocation, solr_xml);
          EmbeddedSolrServer server = new NoSerializeEmbeddedSolrServer(solrContainer, "");
          // test to see if it is really working?
          // server.ping();
          solrServer = server;
          isStarted = true;
        } catch (FileNotFoundException e) {
          // this should never happen since we check before calling
          log.error("Could not find solr home when initializing MatcherFactory:" + homeLocation);
        }

      }

    } else {
      // can't start not configured
      log.error("Could not start MatcherFactory, it hasnt been configured yet");
    }

  }

  public static PlacenameMatcher getMatcher() {

    // if started/configed etc
    if (isConfigured) {

      if (isStarted) {
        PlacenameMatcher tmp = new PlacenameMatcher(solrServer);
        matchers.put(tmp, true);
        return tmp;
      } else {
        // configured but not started
        start();
        log.debug("Autostarting MatcherFactory");
        PlacenameMatcher tmp = new PlacenameMatcher(solrServer);
        matchers.put(tmp, true);
        return tmp;
      }
    } else {
      // not configured
      // try default config
      log.debug("Trying to defult config and autostarting Matcher Factory");
      config("");
      if (isConfigured) {
        log.debug("Default config worked. Try to start");
        start();
        PlacenameMatcher tmp = new PlacenameMatcher(solrServer);
        matchers.put(tmp, true);
        return tmp;
      } else {
        log.error("MatcherFactory not configured and default config did'nt work");
        return null;
      }

    }

  }

  static void shutdown(PlacenameMatcher m) {
    matchers.remove(m);
    MatcherFactory.shutdown(false);
  }

  public static void shutdown(boolean force) {

    if (force) {
      if (solrServer != null) {
        solrServer.shutdown();
      }
      isStarted = false;
    } else {
      if (solrServer != null && matchers.isEmpty()) {
        solrServer.shutdown();
        isStarted = false;
      }
    }

  }

  private static boolean validURL(String url) {

    URL solrURL = null;

    try {
      solrURL = new URL(url);
    } catch (MalformedURLException e) {
      // eat the exception and return not valid
      return false;
    }

    // just so FindBug doesn't complain about unused variable
    solrURL.getHost();
    // check for existence/access here?
    // solrURL.openStream(); ?

    return true;

  }

  private static boolean validFile(String file) {

    File tmp = new File(file);

    if (!tmp.exists()) {
      // file doesn't exist
      return false;
    }

    if (!tmp.isDirectory()) {
      // not a directory
      return false;
    }

    File solrXML = new File(tmp, "solr.xml");

    if (!solrXML.exists()) {
      // doesn't contain a solr.xml
      return false;
    }

    return true;

  }

  public static boolean isConfigured() {
    return isConfigured;
  }

  public static boolean isStarted() {
    return isStarted;
  }

  /**
   * Get an integer from a record
   */
  public static int getInteger(SolrDocument d, String f) {
    Object obj = d.getFieldValue(f);
    if (obj == null) {
      return 0;
    }
    if (obj instanceof Integer) {
      return ((Integer) obj).intValue();
    } else {
      Integer v = Integer.parseInt(obj.toString());
      return v.intValue();
    }
  }

  /**
   * Get a floating point object from a record
   */
  public static Float getFloat(SolrDocument d, String f) {
    Object obj = d.getFieldValue(f);
    if (obj == null) {
      return 0F;
    } else {
      return (Float) obj;
    }
  }

  /**
   * Get a Date object from a record
   * 
   * @throws java.text.ParseException
   */
  public static Date getDate(SolrDocument d, String f) throws java.text.ParseException {
    if (d == null || f == null) {
      return null;
    }
    Object obj = d.getFieldValue(f);
    if (obj == null) {
      return null;
    }
    if (obj instanceof Date) {
      return (Date) obj;
    } else if (obj instanceof String) {
      return DateUtil.parseDate((String) obj);
    }
    return null;
  }

  /**
     *
     */
  public static char getChar(SolrDocument solrDoc, String name) {
    String result = getString(solrDoc, name);
    if (result == null) {
      return 0;
    }
    if (result.isEmpty()) {
      return 0;
    }
    return result.charAt(0);
  }

  /**
   * Get a String object from a record
   */
  public static String getString(SolrDocument solrDoc, String name) {
    Object result = solrDoc.getFirstValue(name);
    if (result == null) {
      return null;
    }
    return result.toString();
  }

  /**
   * 
   * Get a double from a record
   */
  public static double getDouble(SolrDocument solrDoc, String name) {
    Object result = solrDoc.getFirstValue(name);
    if (result == null) {
      throw new IllegalStateException("Blank: " + name + " in " + solrDoc);
    }
    if (result instanceof Number) {
      Number number = (Number) result;
      return number.doubleValue();
    } else {
      return Double.parseDouble(result.toString());
    }
  }

  /**
   * Parse XY pair stored in Solr Spatial4J record. No validation is done.
   * 
   * @return XY double array, [lat, lon]
   */
  public static double[] getCoordinate(SolrDocument solrDoc, String field) {
    String xy = (String) solrDoc.getFirstValue(field);
    if (xy == null) {
      throw new IllegalStateException("Blank: " + field + " in " + solrDoc);
    }
    final double[] xyPair = {0.0, 0.0};
    String[] lat_lon = xy.split(",", 2);
    xyPair[0] = Double.parseDouble(lat_lon[0]);
    xyPair[1] = Double.parseDouble(lat_lon[1]);
    return xyPair;
  }

  /**
   * Parse XY pair stored in Solr Spatial4J record. No validation is done.
   * 
   * @return XY double array, [lat, lon]
   */
  public static double[] getCoordinate(String xy) {
    final double[] xyPair = {0.0, 0.0};
    String[] lat_lon = xy.split(",", 2);
    xyPair[0] = Double.parseDouble(lat_lon[0]);
    xyPair[1] = Double.parseDouble(lat_lon[1]);
    return xyPair;
  }

  /**
   * Create a Place object from a Solr document
   * 
   * @return Place
   */
  public static Place createPlace(SolrDocument gazEntry) {

    Place place = new Place(getString(gazEntry, "place_id"), getString(gazEntry, "name"));

    place.setName_type(getChar(gazEntry, "name_type"));
    // Gazetteer place name & country:
    // NOTE: this may be different than "matchtext" or PlaceCandidate.name
    // field.
    //
    String cc = getString(gazEntry, "cc");
    cc = cc.intern();
    place.setCountryCode(cc);
    // Other metadata.
    place.setAdmin1(getString(gazEntry, "adm1"));
    place.setAdmin2(getString(gazEntry, "adm2"));
    String fClass = getString(gazEntry, "feat_class");
    fClass = fClass.intern();
    place.setFeatureClass(fClass);
    String fCode = getString(gazEntry, "feat_code");
    fCode = fCode.intern();
    place.setFeatureCode(fCode);
    // Geo field is specifically Spatial4J lat,lon format.
    // Note -- Spatial4J ParseUtils offers a full validation of the parsing.
    // But since we validate on entry into the gazetteer, we need not pay
    // that price here
    //
    double[] xy = getCoordinate(gazEntry, "geo");
    place.setLatitude(xy[0]);
    place.setLongitude(xy[1]);
    place.setName_bias(getDouble(gazEntry, "name_bias"));
    place.setId_bias(getDouble(gazEntry, "id_bias"));
    return place;
  }

}
