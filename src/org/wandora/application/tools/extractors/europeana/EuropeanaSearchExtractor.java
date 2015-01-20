/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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

package org.wandora.application.tools.extractors.europeana;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import javax.swing.Icon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author nlaitinen
 */

public class EuropeanaSearchExtractor extends AbstractEuropeanaExtractor {
    
    
    private static String defaultEncoding = "UTF-8";
    private static String defaultLang = "en";
    private static String currentURL = null;
    
    
    
    @Override
    public String getName() {
        return "Europeana API search extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extracts data from The Europeana data API at http://pro.europeana.eu";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_europeana.png");
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        currentURL = null;
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        try {
            currentURL = u.toExternalForm();

            log("Item search extraction with " + currentURL);

            String in = doUrl(u);

            System.out.println("---------------Europeana API returned------------\n"+in+
                               "\n-----------------------------------------------");

            JSONObject json = new JSONObject(in);
            parse(json, tm);
   
        } catch (Exception e){
           e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        parse(json, tm);
        return true;
    }
    
    public String doUrl (URL url) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        
        if (url != null) {
           
                URLConnection con = url.openConnection();
                Wandora.initUrlConnection(con);
                con.setDoInput(true);
                con.setUseCaches(false);
                con.setRequestProperty("Content-type", "text/plain");
                
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName(defaultEncoding)));

                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    if(!(s.endsWith("\n") || s.endsWith("\r"))) sb.append("\n");
                }
                in.close();
            } catch (Exception ex) {
                log("Authentication failed. Check API Key.");
            }
        }
        
        return sb.toString();
    }
    
    
    // ------------------------- PARSING ---------------------------------------
    
    
    public void parse(JSONObject json, TopicMap tm) throws TopicMapException {
        if(json.has("items")) {
            try {
                JSONArray resultsArray = json.getJSONArray("items");
                for(int i=0; i<resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);
                    parseResult(result, tm);
                }
            }
            catch (JSONException ex) {
                log(ex);
            }
        } else if (!json.has("items")) {
            log("API returned no results.");
        }
    }

    public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {
        
        if(result.has("id")) { // All results should contain an url at least.
            String id = result.getString("id");
            String subjectId = ITEM_SI + urlEncode(id);
            Topic itemTopic = tm.createTopic();
            itemTopic.addSubjectIdentifier(new Locator(subjectId));
            itemTopic.addType(getItemTypeTopic(tm));
            
            Topic itemTypeTopic = getItemTypeTopic(tm);

            if(result.has("provider")) {
                JSONArray provider = result.getJSONArray("provider");
                if(provider.length() > 0) {
                    for(int i=0; i<provider.length(); i++) {
                        String value = provider.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = PROVIDER_SI + "/" + urlEncode(value);
                            Topic providerTopic = getProviderTopic(subjectValue, tm);
                            Topic providerTypeTopic = getProviderTypeTopic(tm);
                            
                            Association a = tm.createAssociation(providerTypeTopic);
                            a.addPlayer(providerTopic, providerTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            providerTopic.setBaseName(value);
                        }
                    }
                }
            }
            
            if(result.has("language")) {
                JSONArray language = result.getJSONArray("language");
                if(language.length() > 0) {
                    for(int i=0; i<language.length(); i++) {
                        String value = language.getString(i);
                        if(value != null && value.length() > 0) {
                            Topic languageTypeTopic = getLanguageTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            itemTopic.setData(languageTypeTopic, langTopic, value);
                        }
                    }
                }
            }
            
            if(result.has("year")) {
                JSONArray year = result.getJSONArray("year");
                if(year.length() > 0) {
                    for(int i=0; i<year.length(); i++) {
                        String value = year.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = YEAR_SI + "/" + urlEncode(value);
                            Topic yearTopic = getYearTopic(subjectValue, tm);
                            Topic yearTypeTopic = getYearTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            
                            Association a = tm.createAssociation(yearTypeTopic);
                            a.addPlayer(yearTopic, yearTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            itemTopic.setData(yearTypeTopic, langTopic, value);
                        }
                    }
                }
            }
            
            if(result.has("rights")) {
                JSONArray rights = result.getJSONArray("rights");
                if(rights.length() > 0) {
                    for(int i=0; i<rights.length(); i++) {
                        String value = rights.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = RIGHTS_LINK_SI + "/" + urlEncode(value);
                            Topic rightsLinkTopic = getRightsLinkTopic(subjectValue, tm);
                            Topic rightsLinkTypeTopic = getRightsLinkTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            
                            Association a = tm.createAssociation(rightsLinkTypeTopic);
                            a.addPlayer(rightsLinkTopic, rightsLinkTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            itemTopic.setData(rightsLinkTypeTopic, langTopic, value);
                            rightsLinkTopic.setBaseName(value);
                        }
                    }
                }
            }
            
            if(result.has("title")) {
                JSONArray title = result.getJSONArray("title");
                if(title.length() > 0) {
                    for(int i=0; i<title.length(); i++) {
                        String value = title.getString(i);
                        if(value != null && value.length() > 0) {
                             Topic titleTypeTopic = getTitleTypeTopic(tm);
                             itemTopic.setDisplayName(defaultLang, value);
                             itemTopic.setBaseName(value + " (" + id + ")");
                             Topic langTopic = getLangTopic(tm);
                             itemTopic.setData(titleTypeTopic, langTopic, value);
                        }
                    }
                }
            }
            
            if(result.has("dcCreator")) {
                JSONArray dcCreator = result.getJSONArray("dcCreator");
                if(dcCreator.length() > 0) {
                    for(int i=0; i<dcCreator.length(); i++) {
                        String value = dcCreator.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = DC_CREATOR_SI + "/" + urlEncode(value);
                            Topic dcCreatorTopic = getDcCreatorTopic(subjectValue, tm);
                            Topic dcCreatorTypeTopic = getDcCreatorTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            
                            Association a = tm.createAssociation(dcCreatorTypeTopic);
                            a.addPlayer(dcCreatorTopic, dcCreatorTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            itemTopic.setData(dcCreatorTypeTopic, langTopic, value);
                            dcCreatorTopic.setBaseName(value);
                        }
                    }
                }
            }
            
            if(result.has("country")) {
                JSONArray country = result.getJSONArray("country");
                if(country.length() > 0) {
                    for(int i=0; i<country.length(); i++) {
                        String value = country.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = COUNTRY_SI + "/" + urlEncode(value);
                            Topic countryTopic = getCountryTopic(subjectValue, tm);
                            Topic countryTypeTopic = getCountryTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            
                            Association a = tm.createAssociation(countryTypeTopic);
                            a.addPlayer(countryTopic, countryTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            itemTopic.setData(countryTypeTopic, langTopic, value);
                        }
                    }
                }
            }
            
            if(result.has("europeanaCollectionName")) {
                JSONArray collectionName = result.getJSONArray("europeanaCollectionName");
                if(collectionName.length() > 0) {
                    for(int i=0; i<collectionName.length(); i++) {
                        String value = collectionName.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = COLLECTION_NAME_SI + "/" + urlEncode(value);
                            Topic collectionNameTopic = getCollectionNameTopic(subjectValue, tm);
                            Topic collectionNameTypeTopic = getCollectionNameTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            
                            Association a = tm.createAssociation(collectionNameTypeTopic);
                            a.addPlayer(collectionNameTopic, collectionNameTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            itemTopic.setData(collectionNameTypeTopic, langTopic, value);
                            collectionNameTopic.setBaseName(value);
                        }
                    }
                }
            }
            
            if(result.has("edmConceptLabel")) {
                JSONArray conceptLabel = result.getJSONArray("edmConceptLabel");
                if(conceptLabel.length() > 0) {
                    for(int i=0; i<conceptLabel.length(); i++) {
                        JSONObject obj = conceptLabel.getJSONObject(i);
                        String def = obj.getString("def");
                        if(obj != null && def != null) {
                            Topic conceptLabelTypeTopic = getConceptLabelTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            itemTopic.setData(conceptLabelTypeTopic, langTopic, def);
                        }
                    }
                }
            }
            
            if(result.has("type")) {
                String type = result.getString("type");
                if(type != null && type.length() > 0) {
                    String subjectValue = TYPE_SI + "/" + urlEncode(type);
                    Topic typeTopic = getTypeTopic(subjectValue, tm);
                    Topic typeTypeTopic = getTypeTypeTopic(tm);
                    
                    Association a = tm.createAssociation(typeTypeTopic);
                    a.addPlayer(typeTopic, typeTypeTopic);
                    a.addPlayer(itemTopic, itemTypeTopic);
                    typeTopic.setBaseName(type);
                }
            }
            
            if(result.has("dataProvider")) {
                JSONArray dataProvider = result.getJSONArray("dataProvider");
                if(dataProvider.length() > 0) {
                    for(int i=0; i<dataProvider.length(); i++) {
                        String value = dataProvider.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = DATA_PROVIDER_SI + "/" + urlEncode(value);
                            Topic dataProviderTopic = getDataProviderTopic(subjectValue, tm);
                            Topic dataProviderTypeTopic = getDataProviderTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            
                            Association a = tm.createAssociation(dataProviderTypeTopic);
                            a.addPlayer(dataProviderTopic, dataProviderTypeTopic);
                            a.addPlayer(itemTopic, itemTypeTopic);
                            itemTopic.setData(dataProviderTypeTopic, langTopic, value);
                            dataProviderTopic.setBaseName(value);
                        }
                    }
                }
            }
            
            if(result.has("edmPlaceLabel")) {
                JSONArray placeLabel = result.getJSONArray("edmPlaceLabel");
                if(placeLabel.length() > 0) {
                    for(int i=0; i<placeLabel.length(); i++) {
                        JSONObject obj = placeLabel.getJSONObject(i);
                        String def = obj.getString("def");
                        if(obj != null && def.length() > 0) {
                            Topic placeLabelTypeTopic = getPlaceLabelTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            itemTopic.setData(placeLabelTypeTopic, langTopic, def);
                        }
                    }
                }
            }
            
            if(result.has("edmPreview")) {
                JSONArray previewLink = result.getJSONArray("edmPreview");
                if(previewLink.length() > 0) {
                    for(int i=0; i<previewLink.length(); i++) {
                        String value = previewLink.getString(i);
                        if(value != null && value.length() > 0) {
                            Topic previewLinkTypeTopic = getPreviewLinkTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            itemTopic.setData(previewLinkTypeTopic, langTopic, value);
                        }
                    }
                }
            }
            
            if(result.has("guid")) {
                String guid = result.getString("guid");
                if(guid != null && guid.length() > 0) {
                    Topic guidTypeTopic = getGuidLinkTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    itemTopic.setData(guidTypeTopic, langTopic, guid);
                    itemTopic.addSubjectIdentifier(new Locator(guid));
                }
           }
        }
    }
}
