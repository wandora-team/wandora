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

import java.io.InputStream;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicTools;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;


/**
 * http://www.hel.fi/palvelukarttaws/rest/v1/unit/
 *
 * @author akivela
 */


public class PalvelukarttaUnitExtractor extends AbstractPalvelukarttaExtractor {
    


    private boolean deepExtraction = false;
    private int unitCount = 0;
    
    
    
    public PalvelukarttaUnitExtractor() {
        
    }
    public PalvelukarttaUnitExtractor(boolean deep) {
        deepExtraction = deep;
    }
    
    
    
    
    @Override
    public String getName() {
        return "Palvelukartta unit extractor";
    }
    @Override
    public String getDescription(){
        return "Convert Palvelukartta feed of units to a topic map. Palvelukartta (Service Map) is "+
               "a Helsinki/Espoo/Vantaa/Kauniainen region web service providing information regarding the departments and services of cities. "+
               "Helsinki, Espoo, Vantaa and Kauniainen cities locate in Southern Finland. "+
               "Read more at http://www.hel.fi/palvelukartta";
    }
    
    
   

    
    
    public boolean extractFrom(JSONObject json, TopicMap tm) {
        log("Parsing JSON object!");
        extractUnitFrom(json, tm);
        return true;
    }
    
    
    public boolean extractFrom(JSONArray json, TopicMap tm) {
        unitCount = 0;
        log("Parsing JSON array!");
        extractUnitsFrom(json, tm);
        log("Found total "+unitCount+" units!");
        return true;
    }
    
    
    // -------------------------------------------------------------------------
    

    
    
    
    public boolean extractUnitsFrom(JSONArray json, TopicMap tm) {
        if(json == null || tm == null) return false;
        setProgressMax(json.length());
        if(deepExtraction) {
            log("Deep extracting Palvelukartta units");
        }
        for(int i=0; i<json.length() && !forceStop(); i++) {
            try {
                setProgress(i);
                Object ob = json.get(i);
                if(ob != null) {
                    if(ob instanceof JSONObject) {
                        JSONObject jsono = (JSONObject) ob;
                        if(deepExtraction) {
                            long id = getLongJSONValue(jsono, "id");
                            if(id != -1) {
                                try {
                                    hlog("Deep extracting Palvelukartta unit "+id);
                                    String u = "http://www.hel.fi/palvelukarttaws/rest/v2/unit/" + id;
                                    InputStream inputStream = new URL(u).openStream();
                                    String in = IObox.loadFile(inputStream, "UTF-8");
                                    JSONObject unitJSON = new JSONObject(in);
                                    extractUnitFrom(unitJSON, tm);
                                    
                                    try {
                                        Thread.currentThread().sleep(200);
                                    }
                                    catch(Exception e) {
                                        // WAKE UP
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else {
                            extractUnitFrom(jsono, tm);
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
                e.printStackTrace();
            }
        }
        return true;
    }
    
    
    
    
    
    
    public boolean extractUnitFrom(JSONObject json, TopicMap tm) {
        if(json == null) return false;
        
        try {
            long id = getLongJSONValue(json, "id");
            String name_fi = getStringJSONValue(json, "name_fi");
            String name_sv = getStringJSONValue(json, "name_sv");
            String name_en = getStringJSONValue(json, "name_en");
            String street_address_fi = getStringJSONValue(json, "street_address_fi");
            String street_address_sv = getStringJSONValue(json, "street_address_sv");
            String street_address_en = getStringJSONValue(json, "street_address_en");
            String address_city_fi = getStringJSONValue(json, "address_city_fi");
            String address_city_sv = getStringJSONValue(json, "address_city_sv");
            String address_city_en = getStringJSONValue(json, "address_city_en");
            String address_zip = getStringJSONValue(json, "address_zip");
            double latitude = getDoubleJSONValue(json, "latitude");
            double longitude = getDoubleJSONValue(json, "longitude");
            long org_id = getLongJSONValue(json, "org_id");
            String dept_id = getStringJSONValue(json, "dept_id");
            long provider_type = getLongJSONValue(json, "provider_type");
            
            if(id != -1) {
                unitCount++;
                Topic unitTopic = getUnitTopic(id, tm);
                if(unitTopic != null) {
                    boolean hasBasename = false;
                    if(name_fi != null) {
                        unitTopic.setDisplayName("fi", name_fi);
                        if(!hasBasename) {
                            unitTopic.setBaseName(name_fi + " ("+id+")");
                            hasBasename = true;
                        }
                    }
                    if(name_en != null) {
                        unitTopic.setDisplayName("en", name_en);
                        if(!hasBasename) {
                            unitTopic.setBaseName(name_en + " ("+id+")");
                            hasBasename = true;
                        }
                    }
                    if(name_sv != null) {
                        unitTopic.setDisplayName("sv", name_sv);
                        if(!hasBasename) {
                            unitTopic.setBaseName(name_sv + " ("+id+")");
                            hasBasename = true;
                        }
                    }
                    if(longitude != -1 && latitude != -1) {
                        Topic geoLocationType = getGeoLocationType(tm);
                        Topic langTopic = getOrCreateTopic(tm,XTMPSI.LANG_INDEPENDENT);
                        unitTopic.setData(geoLocationType, langTopic, longitude+","+latitude);
                    }
                    if(street_address_fi != null) {
                        Topic streetAddressType = getStreetAddressType(tm);
                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("fi"));
                        unitTopic.setData(streetAddressType, langTopic, street_address_fi);
                    }
                    if(street_address_sv != null) {
                        Topic streetAddressType = getStreetAddressType(tm);
                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("sv"));
                        unitTopic.setData(streetAddressType, langTopic, street_address_sv);
                    }
                    if(street_address_en != null) {
                        Topic streetAddressType = getStreetAddressType(tm);
                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("en"));
                        unitTopic.setData(streetAddressType, langTopic, street_address_en);
                    }
                    if(address_city_fi != null || address_city_sv != null || address_city_en != null) {
                        Topic cityTopic = getCityTopic(address_city_fi, address_city_sv, address_city_en, tm);
                        if(cityTopic != null) {
                            Topic unitCityType = getUnitCityType(tm);
                            Topic unitType = getUnitType(tm);
                            Topic cityType = getCityType(tm);

                            Association a = tm.createAssociation(unitCityType);
                            a.addPlayer(cityTopic, cityType);
                            a.addPlayer(unitTopic, unitType);
                        }
                    }
                    if(address_zip != null) {
                        Topic zipTopic = getZipTopic(address_zip, tm);
                        if(zipTopic != null) {
                            Topic unitZipType = getUnitZipType(tm);
                            Topic unitType = getUnitType(tm);
                            Topic zipType = getZipType(tm);

                            Association a = tm.createAssociation(unitZipType);
                            a.addPlayer(zipTopic, zipType);
                            a.addPlayer(unitTopic, unitType);
                        }
                    }
                    if(org_id != -1) {
                        Topic organizationTopic = getOrganizationTopic(org_id, tm);
                        if(organizationTopic != null) {
                            Topic unitOrganizationType = getUnitOrganizationType(tm);
                            Topic unitType = getUnitType(tm);
                            Topic organizationType = getOrganizationType(tm);

                            Association a = tm.createAssociation(unitOrganizationType);
                            a.addPlayer(organizationTopic, organizationType);
                            a.addPlayer(unitTopic, unitType);
                        }
                    }
                    if(dept_id != null) {
                        Topic departmentTopic = getDepartmentTopic(dept_id, tm);
                        if(departmentTopic != null) {
                            Topic unitDepartmentType = getUnitDepartmentType(tm);
                            Topic unitType = getUnitType(tm);
                            Topic departmentType = getDepartmentType(tm);

                            Association a = tm.createAssociation(unitDepartmentType);
                            a.addPlayer(departmentTopic, departmentType);
                            a.addPlayer(unitTopic, unitType);
                        }
                    }
                    if(provider_type != -1) {
                        Topic providerTypeType = getProviderTypeType(tm);
                        Topic unitType = getUnitType(tm);
                        Topic providerTypeTopic = getProviderTypeTopic(provider_type, tm);

                        Association a = tm.createAssociation(providerTypeType);
                        a.addPlayer(providerTypeTopic, providerTypeType);
                        a.addPlayer(unitTopic, unitType);
                    }
                    
                    
                    // ***** ADDITIONAL FIELDS ******
                    
                    
                    String www_fi = getStringJSONValue(json, "www_fi");
                    String www_sv = getStringJSONValue(json, "www_sv");
                    String www_en = getStringJSONValue(json, "www_en");
                    String phone = getStringJSONValue(json, "phone");
                    String email = getStringJSONValue(json, "email");
                    Object service_ids = getJSONValue(json, "service_ids");
                    
                    if(www_fi != null) {
                        Topic wwwType = getWWWType(tm);
                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("fi"));
                        unitTopic.setData(wwwType, langTopic, www_fi);
                    }
                    if(www_sv != null) {
                        Topic wwwType = getWWWType(tm);
                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("sv"));
                        unitTopic.setData(wwwType, langTopic, www_sv);
                    }
                    if(www_en != null) {
                        Topic wwwType = getWWWType(tm);
                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("en"));
                        unitTopic.setData(wwwType, langTopic, www_en);
                    }
                    
                    if(phone != null) {
                        Topic phoneType = getPhoneType(tm);
                        Topic langTopic = getDefaultLangTopic(tm);
                        unitTopic.setData(phoneType, langTopic, phone);
                    }
                    if(email != null) {
                        Topic emailType = getEmailType(tm);
                        Topic langTopic = getDefaultLangTopic(tm);
                        unitTopic.setData(emailType, langTopic, email);
                    }
                    
                    if(service_ids != null) {
                        if(service_ids instanceof JSONArray) {
                            JSONArray serviceArray = (JSONArray) service_ids;
                            for(int i=0; i<serviceArray.length(); i++) {
                                long service_id = serviceArray.getLong(i);
                                Topic serviceTopic = getServiceTopic(service_id, tm);
                                if(serviceTopic != null) {
                                    Topic unitServiceType = getUnitServiceType(tm);
                                    Topic unitType = getUnitType(tm);
                                    Topic serviceType = getServiceType(tm);
                                    
                                    Association a = tm.createAssociation(unitServiceType);
                                    a.addPlayer(unitTopic, unitType);
                                    a.addPlayer(serviceTopic, serviceType);
                                }
                            }
                        }
                    }
                    
                    // *****
                    
                    Object connections = getJSONValue(json, "connections");
                    Object events = getJSONValue(json, "events");
                    Object accessibility_sentences = getJSONValue(json, "accessibility_sentences");
                    Object travel_service_ids = getJSONValue(json, "travel_service_ids");
                    
                    if(connections != null) {
                        if(connections instanceof JSONArray) {
                            JSONArray connectionsArray = (JSONArray) connections;
                            for(int j=0; j<connectionsArray.length(); j++) {
                                try {
                                    JSONObject c = connectionsArray.getJSONObject(j);
                                    if(c != null) {
                                        Topic connectionTopic = tm.createTopic();
                                        connectionTopic.addSubjectIdentifier(TopicTools.createDefaultLocator());

                                        String cname_fi = getStringJSONValue(c, "name_fi");
                                        String cname_sv = getStringJSONValue(c, "name_sv");
                                        String cname_en = getStringJSONValue(c, "name_en");
                                        if(cname_fi != null && cname_fi.trim().length() > 0) {
                                            connectionTopic.setDisplayName("fi", cname_fi);
                                            connectionTopic.setBaseName(cname_fi+" ("+name_fi+")");
                                        }
                                        if(cname_sv != null && cname_sv.trim().length() > 0) {
                                            connectionTopic.setDisplayName("sv", cname_sv);
                                        }
                                        if(cname_en != null && cname_en.trim().length() > 0) {
                                            connectionTopic.setDisplayName("en", cname_en);
                                        }

                                        String cwww_fi = getStringJSONValue(c, "www_fi");
                                        String cwww_sv = getStringJSONValue(c, "www_sv");
                                        String cwww_en = getStringJSONValue(c, "www_en");
                                        if(cwww_fi != null && cwww_fi.trim().length() > 0) {
                                            Topic wwwType = getWWWType(tm);
                                            Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("fi"));
                                            connectionTopic.setData(wwwType, langTopic, cwww_fi);
                                        }
                                        if(cwww_sv != null && cwww_sv.trim().length() > 0) {
                                            Topic wwwType = getWWWType(tm);
                                            Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("sv"));
                                            connectionTopic.setData(wwwType, langTopic, cwww_sv);
                                        }
                                        if(cwww_en != null && cwww_en.trim().length() > 0) {
                                            Topic wwwType = getWWWType(tm);
                                            Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("en"));
                                            connectionTopic.setData(wwwType, langTopic, cwww_en);
                                        }

                                        String cemail = getStringJSONValue(c, "email");
                                        if(cemail != null && cemail.trim().length() > 0) {
                                            Topic emailType = getEmailType(tm);
                                            Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("fi"));
                                            connectionTopic.setData(emailType, langTopic, cemail);
                                        }
                                        
                                        String ctel = getStringJSONValue(c, "tel");
                                        if(ctel != null && ctel.trim().length() > 0) {
                                            Topic telType = getPhoneType(tm);
                                            Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("fi"));
                                            connectionTopic.setData(telType, langTopic, ctel);
                                        }

                                        String contactPerson = getStringJSONValue(c, "contactPerson");
                                        if(contactPerson != null && contactPerson.trim().length() > 0) {
                                            Topic contactPersonType = getContactPersonType(tm);
                                            Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang("fi"));
                                            connectionTopic.setData(contactPersonType, langTopic, contactPerson);
                                        }
                                        
                                        
                                        // ---------------------------
                                        // and finally associate...
                                        Topic unitConnectionType = getUnitConnectionType(tm);
                                        Topic connectionType = getConnectionType(tm);
                                        Topic unitType = getUnitType(tm);
                                        
                                        Association a = tm.createAssociation(unitConnectionType);
                                        a.addPlayer(unitTopic, unitType);
                                        a.addPlayer(connectionTopic, connectionType);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(events != null) {
                        System.out.println("SPOTTED events AT CONNECTIONS OF "+name_fi+":"+events);
                    }
                    if(accessibility_sentences != null) {
                        System.out.println("SPOTTED accessibility_sentences AT CONNECTIONS OF "+name_fi+":"+accessibility_sentences);
                    }
                    if(travel_service_ids != null) {
                        System.out.println("SPOTTED travel_service_ids AT CONNECTIONS OF "+name_fi+":"+travel_service_ids);
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        
        return true;
    }
    
    
}
