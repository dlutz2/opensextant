/**
 *
 * Copyright 2009-2013 The MITRE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * **************************************************************************
 * NOTICE This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
 */
///** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
//
// _____                                ____                     __                       __
///\  __`\                             /\  _`\                  /\ \__                   /\ \__
//\ \ \/\ \   _____      __     ___    \ \,\L\_\      __   __  _\ \ ,_\     __       ___ \ \ ,_\
// \ \ \ \ \ /\ '__`\  /'__`\ /' _ `\   \/_\__ \    /'__`\/\ \/'\\ \ \/   /'__`\   /' _ `\\ \ \/
//  \ \ \_\ \\ \ \L\ \/\  __/ /\ \/\ \    /\ \L\ \ /\  __/\/>  </ \ \ \_ /\ \L\.\_ /\ \/\ \\ \ \_
//   \ \_____\\ \ ,__/\ \____\\ \_\ \_\   \ `\____\\ \____\/\_/\_\ \ \__\\ \__/.\_\\ \_\ \_\\ \__\
//    \/_____/ \ \ \/  \/____/ \/_/\/_/    \/_____/ \/____/\//\/_/  \/__/ \/__/\/_/ \/_/\/_/ \/__/
//            \ \_\
//             \/_/
//
//   OpenSextant Commons
// *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
// */
package org.mitre.opensextant.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class Place implements Comparable<Object>, Serializable {

    private static final long serialVersionUID = 2389068012345L;
    /**
     * For normalization purposes tracking the Province may be helpful.
     * Coordinate and Place both share this common field. However no need to
     * create an intermediate parent-class yet.
     *
     * Province is termed ADM1 -- or the first level of administrative boundary
     */
    protected String admin1 = null;
    protected String admin2 = null;

    String name;
    String id;
    
    double lat;
    double lon;
   
    protected char name_type = 0;

    public void setName_type(char t) {
        name_type = t;
    }

    public char getName_type() {
        return name_type;
    }

    public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
     *
     */
    protected String country_id = null;

    /**
     * Compat: set country_id aka CountryCode
     */
    public void setCountryCode(String cc) {
        country_id = cc;
    }

    public String getCountryCode() {
        return country_id;
    }
    
        private String featureClass = null;

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }
    private String featureCode = null;

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }



    public final void setPlaceName(String nm) {
        this.name=nm;
    }

    public final String getPlaceName() {
        return name;
    }

    public String getAdmin1() {
        return admin1;
    }

    public void setAdmin1(String key) {
        admin1 = key;
    }

    public String getAdmin2() {
        return admin2;
    }

    public void setAdmin2(String key) {
        admin2 = key;
    }






    // the a priori estimates
    private Double name_bias;
    private Double id_bias;
	/** ISO 2-character country code */
	public String CC_ISO2 = null;
	/** ISO 3-character country code */
	public String CC_ISO3 = null;
	/** FIPS 10-4 2-character country code */
	public String CC_FIPS = null;
	/** Any list of country alias names. */
	private Set<String> aliases = new HashSet<String>();
	private Set<String> regions = new HashSet<String>();

    /**
     * The name bias is a measure of the a priori likelihood that a mention of
     * this place's name actually refers to a place.
     */
    public Double getName_bias() {
        return name_bias;
    }

    public void setName_bias(Double bias) {
        name_bias = bias;
    }

    /**
     * The ID bias is a measure of the a priori likelihood that a mention of
     * this name refers to this particular place.
     */
    public Double getId_bias() {
        return id_bias;
    }

    public void setId_bias(Double bias) {
        id_bias = bias;
    }

    @Override
    public String toString() {
        return this.getPlaceName() + "(" + this.getAdmin1() + ","
                + this.getCountryCode() + "," + this.getFeatureCode() + ")";
    }

    /* two Places with the same PlaceID are the same "place"
     * two Places with different PlaceIDs ARE PROBABLY different "places"
     */
    @Override
    public int compareTo(Object other) {
        if (!(other instanceof Place)) {
            return 0;
        }
        Place tmp = (Place) other;
        return this.getId().compareTo(tmp.getId());
    }

	/** Country is also known as some list of aliases
	 * @param nm 
	 */
	public void addAlias(String nm) {
	    aliases.add(nm);
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getAliases() {
	    return aliases;
	}

	/** Country is also known as some list of aliases
	 * @param regionid 
	*/
	public void addRegion(String regionid) {
	    regions.add(regionid);
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getRegions() {
	    return regions;
	}
}
