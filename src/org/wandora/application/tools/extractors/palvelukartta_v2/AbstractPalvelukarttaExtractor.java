/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2016 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.wandora.application.tools.extractors.palvelukartta_v2;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import javax.swing.Icon;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */


public abstract class AbstractPalvelukarttaExtractor extends AbstractExtractor {
    
	private static final long serialVersionUID = 1L;


	public static final String LANG = "fi";
    
    
    public static final String PALVELUKARTTA_SI = "http://www.palvelukartta.fi";
    
    public static final String API_BASE = "http://www.hel.fi/palvelukarttaws/rest/v2/";
    public static final String WANDORA_BASE = "http://wandora.org/si/palvelukartta/";
    
    public static final String PALVELUKARTTA_SERVICE_SI = API_BASE+"service";
    public static final String PALVELUKARTTA_ORGANIZATION_SI = API_BASE+"organization";
    public static final String PALVELUKARTTA_DEPARTMENT_SI = API_BASE+"department";
    public static final String PALVELUKARTTA_UNIT_SI = API_BASE+"unit";
    
    
    public static final String PALVELUKARTTA_DEPARTMENT_ORGANIZATION_SI = WANDORA_BASE + "department-organization";
    public static final String PALVELUKARTTA_ABBR_SI = WANDORA_BASE + "abbr";
    public static final String PALVELUKARTTA_DATASOURCE_SI = WANDORA_BASE + "data-source";
    public static final String PALVELUKARTTA_GEOLOCATION_SI = WANDORA_BASE + "geo-location";
    public static final String PALVELUKARTTA_UNIT_ORGANIZATION_SI = WANDORA_BASE + "unit-organization";
    public static final String PALVELUKARTTA_UNIT_DEPARTMENT_SI = WANDORA_BASE + "unit-department";
    public static final String PALVELUKARTTA_UNIT_SERVICE_SI = WANDORA_BASE + "unit-service";
    public static final String PALVELUKARTTA_UNIT_CITY_SI = WANDORA_BASE + "unit-city";
    public static final String PALVELUKARTTA_CITY_SI = WANDORA_BASE + "city";
    public static final String PALVELUKARTTA_ZIP_SI = WANDORA_BASE + "zip";
    public static final String PALVELUKARTTA_UNITZIP_SI = WANDORA_BASE + "unit-zip";
    public static final String PALVELUKARTTA_STREETADDRESS_SI = WANDORA_BASE + "street-address";
    public static final String PALVELUKARTTA_PROVIDER_TYPE_SI = WANDORA_BASE + "provider-type";
    
    public static final String PALVELUKARTTA_UNIT_CONNECTION_SI = WANDORA_BASE + "unit-connection";
    public static final String PALVELUKARTTA_CONNECTION_SI = WANDORA_BASE + "connection";
    
    public static final String PALVELUKARTTA_WWW_SI = WANDORA_BASE + "www";
    public static final String PALVELUKARTTA_PHONE_SI = WANDORA_BASE + "phone";
    public static final String PALVELUKARTTA_EMAIL_SI = WANDORA_BASE + "email";
    public static final String PALVELUKARTTA_CONTACT_PERSON_SI = WANDORA_BASE + "contact-person";
    
    public static final String PALVELUKARTTA_SERVICE_RELATION_SI = WANDORA_BASE + "service-relation";
    public static final String PALVELUKARTTA_PARENT_SERVICE_SI = WANDORA_BASE + "parent-service";
    public static final String PALVELUKARTTA_CHILD_SERVICE_SI = WANDORA_BASE + "child-service";
    
    
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_palvelukartta.png");
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
 
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        String in = IObox.loadFile(f);
        boolean r = false;
        try {
            JSONObject json = new JSONObject(in);
            r = extractFrom(json, tm);
        }
        catch(Exception e) {
            JSONArray json = new JSONArray(in);
            r = extractFrom(json, tm);
        }
        return r;
    }
    
    

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        InputStream inputStream = u.openStream();
        String in = IObox.loadFile(inputStream, "UTF-8");
        boolean r = false;
        try {
            JSONArray json = new JSONArray(in);
            r = extractFrom(json, tm);
        }
        catch(Exception e) {
            JSONObject json = new JSONObject(in);
            r = extractFrom(json, tm);
        }
        return r;
    }

    
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        boolean r = false;
        try {
            JSONArray json = new JSONArray(str);
            r = extractFrom(json, tm);
        }
        catch(Exception e) {
            JSONObject json = new JSONObject(str);
            r = extractFrom(json, tm);
        }
        return r;
    }
    
    
    public abstract boolean extractFrom(JSONObject json, TopicMap tm);
    public abstract boolean extractFrom(JSONArray json, TopicMap tm);
    
    
    
    
    
    // -------------------------------------------------------------------------

    
    protected double getDoubleJSONValue(JSONObject o, String key) {
        if(o != null && key != null) {
            if(o.has(key)) {
                try {
                    double d = o.getDouble(key);
                    return d;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }
    
    
    
    
    protected long getLongJSONValue(JSONObject o, String key) {
        if(o != null && key != null) {
            if(o.has(key)) {
                try {
                    long l = o.getLong(key);
                    return l;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }
    
    protected String getStringJSONValue(JSONObject o, String key) {
        Object ob = getJSONValue(o, key);
        if(ob != null) return ob.toString();
        return null;
    }
    
    
    protected Object getJSONValue(JSONObject o, String key) {
        if(o != null && key != null) {
            if(o.has(key)) {
                try {
                    return o.get(key);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    
    
    
    public Topic getContactPersonType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_CONTACT_PERSON_SI, "Palvelukartta contact person");
    }
    public Topic getEmailType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_EMAIL_SI, "Palvelukartta email");
    }
    public Topic getPhoneType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_PHONE_SI, "Palvelukartta phone");
    }
    public Topic getWWWType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_WWW_SI, "Palvelukartta WWW");
    }
    
    
    
    public Topic getAbbrType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_ABBR_SI, "Palvelukartta abbreviation");
    }
    public Topic getDataSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_DATASOURCE_SI, "Palvelukartta data source");
    }
    
    
    
    
    public Topic getUnitConnectionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_CONNECTION_SI, "Palvelukartta unit's connection");
    }
    public Topic getConnectionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_CONNECTION_SI, "Palvelukartta connection");
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public Topic getProviderTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_PROVIDER_TYPE_SI, "Palvelukartta provider type", getPalvelukarttaType(tm));
    }
    public Topic getProviderTypeTopic(long provider_type, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_PROVIDER_TYPE_SI + "/" + provider_type, null, getProviderTypeType(tm));
    }
    
    
    // -------------------------------------------------------------------------
    

    public Topic getServiceTopic(long serviceID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_SERVICE_SI + "/" + serviceID, null, getServiceType(tm));
    }
    
    
    public Topic getServiceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_SERVICE_SI, "Palvelukartta service", getPalvelukarttaType(tm));
    }
    
    
    
    
    
    public Topic getServiceRelationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_SERVICE_RELATION_SI, "Palvelukartta service relation", getPalvelukarttaType(tm));
    }
    
    public Topic getParentServiceRole(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_PARENT_SERVICE_SI, "Palvelukartta parent service");
    }
    
    public Topic getChildServiceRole(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_CHILD_SERVICE_SI, "Palvelukartta child service");
    }
    
    
    
    
    // -------
    
    
    public Topic getOrganizationTopic(long organizationID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_ORGANIZATION_SI + "/" + organizationID, null, getOrganizationType(tm));
    }
    
    
    public Topic getOrganizationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_ORGANIZATION_SI, "Palvelukartta organization", getPalvelukarttaType(tm));
    }
    
    
    // ------
    

    public Topic getDepartmentTopicWithAbbr(String abbr, TopicMap tm) throws TopicMapException {
        Collection<Topic> depts = tm.getTopicsOfType(getDepartmentType(tm));
        Topic abbrType = getAbbrType(tm);
        Topic langTopic = getDefaultLangTopic(tm);
        for(Topic dept : depts) {
            if(dept != null && !dept.isRemoved()) {
                if(abbr.equals(dept.getData(abbrType, langTopic))) {
                    return dept;
                }
            }
        }
        return null;
    }
    
    
    public Topic getDepartmentTopic(String departmentID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_DEPARTMENT_SI + "/" + urlEncode(departmentID), null, getDepartmentType(tm));
    }
    
    
    public Topic getDepartmentType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_DEPARTMENT_SI, "Palvelukartta department", getPalvelukarttaType(tm));
    }
    
    
    public Topic getDepartmentOrganizationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_DEPARTMENT_ORGANIZATION_SI, "Palvelukartta department's organization");
    }
            
    
    
    
    // ------
    
    
    
    public Topic getUnitTopic(long unitID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_SI + "/" + unitID, null, getUnitType(tm));
    }
    
    
    public Topic getUnitType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_SI, "Palvelukartta unit", getPalvelukarttaType(tm));
    }
    
    
    public Topic getUnitDepartmentType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_DEPARTMENT_SI, "Palvelukartta unit's department", getPalvelukarttaType(tm));
    }
    
    public Topic getUnitOrganizationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_ORGANIZATION_SI, "Palvelukartta unit's organization", getPalvelukarttaType(tm));
    }
    
    public Topic getUnitServiceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_SERVICE_SI, "Palvelukartta unit's service", getPalvelukarttaType(tm));
    }
            
    
    public Topic getUnitCityType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNIT_CITY_SI, "Palvelukartta unit's city", getPalvelukarttaType(tm));
    }
    
    
    public Topic getCityType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_CITY_SI, "Palvelukartta city", getPalvelukarttaType(tm));
    }
    
    public Topic getStreetAddressType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_STREETADDRESS_SI, "Palvelukartta street address");
    }
    
    public Topic getGeoLocationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_GEOLOCATION_SI, "Palvelukartta geo location");
    }
    
    

    
    
    public Topic getCityTopic(String city_fi, String city_sv, String city_en, TopicMap tm) throws TopicMapException {
        Topic cityTopic = getOrCreateTopic(tm, PALVELUKARTTA_CITY_SI + "/" + urlEncode(city_fi), city_fi, getCityType(tm));
        if(cityTopic != null) {
            if(city_fi != null) {
                cityTopic.setDisplayName("fi", city_fi);
            }
            if(city_sv != null) {
                cityTopic.setDisplayName("sv", city_sv);
            }
            if(city_en != null) {
                cityTopic.setDisplayName("en", city_en);
            }
        }
        return cityTopic;
    }
    
    
    
    
    // ------
    
    
    
    
    public Topic getZipTopic(String zip, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_ZIP_SI + "/" + urlEncode(zip), null, getZipType(tm));
    }
    
    public Topic getZipType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_ZIP_SI, "Palvelukartta zip", getPalvelukarttaType(tm));
    }
    
    public Topic getUnitZipType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PALVELUKARTTA_UNITZIP_SI, "Palvelukartta unit's zip", getPalvelukarttaType(tm));
    }
    // ------
    
    
    public Topic getPalvelukarttaType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, PALVELUKARTTA_SI, "Palvelukartta");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }
    
    public Topic getDefaultLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, XTMPSI.getLang(LANG));
    }
    

    // ******** TOPIC MAPS *********
    
    
    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }



    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }
    
    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn, Topic type) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, type, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    
    
    // -------------------------------------------------------------------------
    

    protected Topic getATopic(String str, String si, Topic type, TopicMap tm) throws TopicMapException {
        if(str != null && si != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic topic=getOrCreateTopic(tm, si+"/"+urlEncode(str), str);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }

    protected Topic getUTopic(String si, Topic type, TopicMap tm) throws TopicMapException {
        if(si != null) {
            si = si.trim();
            if(si.length() > 0) {
                Topic topic=getOrCreateTopic(tm, si, null);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------

    
    
    
    
    public void associateServices(Topic parentService, Topic childService, TopicMap tm) throws TopicMapException {
        if(parentService != null && childService != null) {
            Topic parentServiceRole = getParentServiceRole(tm);
            Topic childServiceRole = getChildServiceRole(tm);
            Topic serviceRelationType = getServiceRelationType(tm);
            
            Association a = tm.createAssociation(serviceRelationType);
            a.addPlayer(childService, childServiceRole);
            a.addPlayer(parentService, parentServiceRole);
        }
    }
    
    

}
