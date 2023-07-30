/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * GeoNamesExtractorSelector.java
 *
 * Created on 11. lokakuuta 2008, 18:27
 */




package org.wandora.application.tools.extractors.geonames;


import java.awt.Component;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.utils.Tuples;



/**
 *
 * @author  akivela
 */
public class GeoNamesExtractorSelector extends javax.swing.JDialog {

    
	private static final long serialVersionUID = 1L;
	
	
	public static String BASE_URL = "http://api.geonames.org/";
    private Wandora wandora = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    /** Creates new form GeoNamesExtractorSelector */
    public GeoNamesExtractorSelector(Wandora w) {
        super(w, true);
        initComponents();
        initGUIDatas();
        setSize(550,300);
        setTitle("GeoNames extractors");
        w.centerWindow(this);
        this.wandora = w;
        accepted = false;
    }

    
    
    public void initGUIDatas() {
        searchCountryComboBox.setEditable(false);
        searchContinentComboBox.setEditable(false);
        searchFeatureClassComboBox.setEditable(false);
        findNearByFeatureComboBox.setEditable(false);
        
        // **** COUNTRIES ****
        String [][] countryData = AbstractGeoNamesExtractor.getCountryData();
        ArrayList<String> countryNames = new ArrayList();
        for(int i=0; i<countryData.length; i++) {
            countryNames.add(countryData[i][1]);
        }
        countryInfoComboBox.addItem("");
        searchCountryComboBox.addItem("");
        Collections.sort(countryNames);
        for(int i=0; i<countryNames.size(); i++) {
            countryInfoComboBox.addItem(countryNames.get(i));
            searchCountryComboBox.addItem(countryNames.get(i));
        }
        
        // **** CONTINENTS ****
        String [][] continentData = AbstractGeoNamesExtractor.getContinentData();
        ArrayList<String> continentNames = new ArrayList();
        for(int i=0; i<continentData.length; i++) {
            continentNames.add(continentData[i][1]);
        }
        searchContinentComboBox.addItem("");
        Collections.sort(continentNames);
        for(int i=0; i<continentNames.size(); i++) {
            searchContinentComboBox.addItem(continentNames.get(i));
        }
        
        // **** FEATURE CLASSES ****
        String [][] featureClassData = AbstractGeoNamesExtractor.getFeatureClassData();
        ArrayList<String> featureClassNames = new ArrayList();
        for(int i=0; i<featureClassData.length; i++) {
            featureClassNames.add(featureClassData[i][1]);
        }
        searchFeatureClassComboBox.addItem("");
        findNearByFeatureComboBox.addItem("");
        Collections.sort(featureClassNames);
        for(int i=0; i<featureClassNames.size(); i++) {
            searchFeatureClassComboBox.addItem(featureClassNames.get(i));
            findNearByFeatureComboBox.addItem(featureClassNames.get(i));
        }
    }

    
    public void setWandora(Wandora w) {
        this.wandora = w;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }
    
    @Override
    public void setVisible(boolean v) {
        if(v) {
            if(apikey == null) forgetButton.setEnabled(false);
            else forgetButton.setEnabled(true);
        }
        super.setVisible(v);
    }

    
    public boolean wasAccepted() {
        return accepted;
    }
    public void setAccepted(boolean b) {
        accepted = b;
    }
    

    public WandoraTool getWandoraTool(WandoraTool parentTool) {
        Component component = geoNamesTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        String requestLang = getRequestLanguage();
        String username = solveAPIKey();
        
        // ***** SEARCH *****
        if(searchPanel.equals(component)) {
            String countryFilter = AbstractGeoNamesExtractor.solveCountryCode((String) searchCountryComboBox.getSelectedItem() );
            String continentFilter = AbstractGeoNamesExtractor.solveContinentCode((String) searchContinentComboBox.getSelectedItem() );
            String featureClassFilter = AbstractGeoNamesExtractor.solveFeatureClassCode((String) searchFeatureClassComboBox.getSelectedItem() );
            boolean isNameRequired = searchIsNameRequiredCheckBox.isSelected();
            String query = searchTextField.getText();
            if(query == null) query = "";
            
            String searchUrl = BASE_URL+"search?username="+username+"&style=full&maxRows=1000&lang="+urlEncode(requestLang);
            searchUrl += "&q="+urlEncode(query.trim());
            if(countryFilter != null) {
                searchUrl += "&country="+countryFilter;
            }
            if(continentFilter != null) {
                searchUrl += "&continentCode="+continentFilter;
            }
            if(featureClassFilter != null) {
                searchUrl += "&featureClass="+featureClassFilter;
            }
            if(isNameRequired) {
                searchUrl += "&isNameRequired=true";
            }
            
            GeoNamesSearch e = new GeoNamesSearch();
            e.dataLang = requestLang;
            e.setForceUrls( new String[] { searchUrl } );
            wt = e;
        }
        
        
        // ***** WIKIPEDIA SEARCH *****
        if(wikiSearchPanel.equals(component)) {
            String query = wikiSearchTextField.getText();
            if(query == null) query = "";
            
            String searchUrl = BASE_URL+"wikipediaSearch?username="+username+"&maxRows=1000&lang="+urlEncode(requestLang);
            searchUrl += "&q="+urlEncode(query.trim());
                        
            GeoNamesWikipediaSearch e = new GeoNamesWikipediaSearch();
            e.dataLang = requestLang;
            e.setForceUrls( new String[] { searchUrl } );
            wt = e;
        }
        
        
        
        // ***** WIKIPEDIA BOUNDING BOX *****
        if(wikiBoxPanel.equals(component)) {
            String northString = wikiBoxNorthTextField.getText();
            String southString = wikiBoxSouthTextField.getText();
            String westString = wikiBoxWestTextField.getText();
            String eastString = wikiBoxEastTextField.getText();
            
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(northString)) {
                parentTool.log("Warning: North coordinate is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(southString)) {
                parentTool.log("Warning: South coordinate is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(westString)) {
                parentTool.log("Warning: West coordinate is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(eastString)) {
                parentTool.log("Warning: East coordinate is not valid GPS number.");
            }
            
            GeoNamesWikipediaBBox e = new GeoNamesWikipediaBBox();
            e.dataLang = requestLang;
            e.setForceUrls( new String[] { BASE_URL+"wikipediaBoundingBox?username="+username+"&lang="+urlEncode(requestLang)+
                    "&maxRows=1000"+
                    "&north="+urlEncode(northString)+
                    "&south="+urlEncode(southString)+
                    "&east="+urlEncode(eastString)+
                    "&west="+urlEncode(westString) } );
            wt = e;
        }
        
        
        // ***** FIND NEAR BY *****
        else if(findNearByPanel.equals(component)) {
            String latString = findNearByLatTextField.getText();
            String lonString = findNearByLonTextField.getText();
            String featureClassFilter = AbstractGeoNamesExtractor.solveFeatureClassCode((String) searchFeatureClassComboBox.getSelectedItem() );
            
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(latString)) {
                parentTool.log("Warning: Latitude is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(lonString)) {
                parentTool.log("Warning: Longitude is not valid GPS number.");
            }
            
            FindNearByGeoNames e = new FindNearByGeoNames();
            e.dataLang = requestLang;
            String urlStr = BASE_URL+"findNearby?username="+username+"&style=full&lang="+urlEncode(requestLang);
            urlStr += "&lat="+urlEncode(latString);
            urlStr += "&lng="+urlEncode(lonString);
            urlStr += "&maxRows=1000";
            if(featureClassFilter != null && featureClassFilter.length()>0) {
                urlStr += "&featureClass="+featureClassFilter;
            }
            String radius = findNearByRadiusTextField.getText();
            if(radius != null) {
                radius = radius.trim();
                if(radius.length() > 0) {
                    urlStr += "&radius="+urlEncode(radius);
                }
            }
            e.setForceUrls( new String[] { urlStr } );
            wt = e;
        }
        
        
        // ***** COUNTRY INFO *****
        else if(countryInfoPanel.equals(component)) {
            Object[] countryObjects = countryInfoComboBox.getSelectedObjects();
            ArrayList<String> countries = new ArrayList<String>();
                    
            if(countryObjects == null || countryObjects.length == 0) {
                parentTool.log("No country codes given.");
                return null;
            }
            String country;
            String countryCode;
            String[][] countryData = AbstractGeoNamesExtractor.getCountryData();
            for(int i=0; i<countryObjects.length; i++) {
                country = (String) countryObjects[i];
                if("ALL".equalsIgnoreCase(country.trim())) {
                    countries.add( BASE_URL+"countryInfo?username="+username+"&lang="+urlEncode(requestLang));
                }
                else {
                    String[] splitCountry = country.split(",");
                    for(int k=0; k<splitCountry.length; k++) {
                        countryCode = splitCountry[k].trim();
                        if(splitCountry[k] != null && splitCountry[k].length() > 0) {
                            for(int j=0; j<countryData.length; j++) {
                                if(splitCountry[k].equalsIgnoreCase(countryData[j][1])) {
                                    countryCode = countryData[j][0];
                                    break;
                                }
                            }
                            if(countryCode.length() != 2) parentTool.log("Warning: Found country code '"+countryCode+"' is not valid country code!");
                            countries.add( BASE_URL+"countryInfo?username="+username+"&lang="+urlEncode(requestLang)+"&country="+urlEncode(countryCode) );
                        }
                    }
                }
            }
            
            GeoNamesCountryInfo cie = new GeoNamesCountryInfo();
            cie.dataLang = requestLang;
            cie.setForceUrls( countries.toArray( new String[] {} ));
            wt = cie;
        }
        
        // ***** CITIES *****
        else if(citiesPanel.equals(component)) {
            String northString = citiesNorthTextField.getText();
            String southString = citiesSouthTextField.getText();
            String westString = citiesWestTextField.getText();
            String eastString = citiesEastTextField.getText();
            
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(northString)) {
                parentTool.log("Warning: North coordinate is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(southString)) {
                parentTool.log("Warning: South coordinate is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(westString)) {
                parentTool.log("Warning: West coordinate is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(eastString)) {
                parentTool.log("Warning: East coordinate is not valid GPS number.");
            }
            
            GeoNamesCities ce = new GeoNamesCities();
            ce.dataLang = requestLang;
            ce.setForceUrls( new String[] { BASE_URL+"cities?username="+username+"&lang="+urlEncode(requestLang)+
                    "&north="+urlEncode(northString)+
                    "&south="+urlEncode(southString)+
                    "&east="+urlEncode(eastString)+
                    "&west="+urlEncode(westString) } );
            wt = ce;
        }
        
        
        // ***** CHILDREN *****
        else if(childrenPanel.equals(component)) {
            String geoidstr = childrenTextField.getText();
            String[] geoids = urlEncode(commaSplitter(geoidstr));
            String[] geoUrls = completeString(BASE_URL+"children?username="+username+"&style=full&lang="+urlEncode(requestLang)+"&geonameId=__1__", geoids);
            
            GeoNamesChildren che = new GeoNamesChildren();
            che.dataLang = requestLang;
            che.setForceUrls( geoUrls );
            wt = che;
        }
        
        // ***** HIERARCHY *****
        else if(hierarchyPanel.equals(component)) {
            String geoidstr = hierarchyTextField.getText();
            String[] geoids = urlEncode(commaSplitter(geoidstr));
            String[] geoUrls = completeString(BASE_URL+"hierarchy?username="+username+"&style=full&lang="+urlEncode(requestLang)+"&geonameId=__1__", geoids);
            
            GeoNamesHierarchy hie = new GeoNamesHierarchy();
            hie.dataLang = requestLang;
            hie.setForceUrls( geoUrls );
            wt = hie;
        }
        
        // ***** NEIGHBOURS *****
        else if(neighboursPanel.equals(component)) {
            String geoidstr = neighboursTextField.getText();
            String[] geoids = urlEncode(commaSplitter(geoidstr));
            String[] geoUrls = completeString(BASE_URL+"neighbours?username="+username+"&style=full&lang="+urlEncode(requestLang)+"&geonameId=__1__", geoids);
            
            GeoNamesNeighbours e = new GeoNamesNeighbours();
            e.dataLang = requestLang;
            e.setForceUrls( geoUrls );
            wt = e;
        }
        
        // ***** SIBLINGS *****
        else if(siblingsPanel.equals(component)) {
            String geoidstr = siblingsTextField.getText();
            String[] geoids = urlEncode(commaSplitter(geoidstr));
            String[] geoUrls = completeString(BASE_URL+"siblings?username="+username+"&style=full&lang="+urlEncode(requestLang)+"&geonameId=__1__", geoids);
            
            GeoNamesSiblings e = new GeoNamesSiblings();
            e.dataLang = requestLang;
            e.setForceUrls( geoUrls );
            wt = e;
        }
        
        // ***** WEATHER *****
        else if(weatherPanel.equals(component)) {
            String latString = weatherLatTextField.getText();
            String lonString = weatherLonTextField.getText();
           
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(latString)) {
                parentTool.log("Warning: Latitude is not valid GPS number.");
            }
            if(!AbstractGeoNamesExtractor.isValidGPSCoordinate(lonString)) {
                parentTool.log("Warning: Longitude is not valid GPS number.");
            }
            
            GeoNamesNearByWeather e = new GeoNamesNearByWeather();
            e.dataLang = requestLang;
            String urlStr = BASE_URL+"findNearByWeatherXML?username="+username+"";
            urlStr += "&lat="+urlEncode(latString);
            urlStr += "&lng="+urlEncode(lonString);
            
            e.setForceUrls( new String[] { urlStr } );
            wt = e;
        }
        
        
        return wt;
    }
    
    
    
    
    
    
    
    public String[] commaSplitter(String str) {
        if(str.indexOf(',') != -1) {
            String[] strs = str.split(",");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.length() > 0) {
                    strList.add(s);
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
        
    }
    
    
    
    public String[] completeString(String template, String[] strs) {
        if(strs == null || template == null) return null;
        String[] completed = new String[strs.length];
        for(int i=0; i<strs.length; i++) {
            completed[i] = template.replaceAll("__1__", strs[i]);
        }
        return completed;
    }
    
    
    public String[] completeString(String template, String[] strs1, String[] strs2) {
        if(strs1 == null || strs2 == null || template == null) return null;
        if(strs1.length != strs2.length) return null;
        
        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2[i]);
        }
        return completed;
    }
    
    
    public String[] urlEncode(String[] urls) {
        if(urls == null) return null;
        String[] cleanUrls = new String[urls.length];
        for(int i=0; i<urls.length; i++) {
            cleanUrls[i] = urlEncode(urls[i]);
        }
        return cleanUrls;
    }
    
    
    
    public String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch(Exception e) {
            return url;
        }
    }
    
    
    
    
    public void solveCountryContext() {
        StringBuilder sb = new StringBuilder("");
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                String str = null;
                Object o = null;
                while(contextObjects.hasNext()) {
                    str = null;
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        str = t.getDisplayName(AbstractGeoNamesExtractor.LANG);
                        if(str != null) {
                            str = str.trim();
                        }
                    }

                    if(str != null && str.length() > 0) {
                        sb.append(str);
                        if(contextObjects.hasNext()) {
                            sb.append(", ");
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        String contextStr= sb.toString();
        if(contextStr != null || contextStr.length() == 0) {
            countryInfoComboBox.addItem(contextStr);
            countryInfoComboBox.setSelectedItem(contextStr);
        }
    }
    
    
    
    
    
    public Tuples.T2 solveGPSLocation() {
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                Object o = null;
                boolean found = false;
                while(contextObjects.hasNext() && !found) {
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        if(t != null && !t.isRemoved()) {
                            Collection<Association> as = t.getAssociations(AbstractGeoNamesExtractor.getLocationTypeTopic(t.getTopicMap()));
                            Association a = null;
                            for(Iterator<Association> ai = as.iterator(); ai.hasNext() && !found; ) {
                                a = ai.next();
                                if(a != null && !a.isRemoved()) {
                                    Topic lat = a.getPlayer(AbstractGeoNamesExtractor.getLatTypeTopic(t.getTopicMap()));
                                    Topic lon = a.getPlayer(AbstractGeoNamesExtractor.getLngTypeTopic(t.getTopicMap()));

                                    if(lat != null && !lat.isRemoved() &&
                                       lon != null && !lon.isRemoved()) {
                                            String latStr = lat.getDisplayName(null);
                                            String lonStr = lon.getDisplayName(null);

                                            return new Tuples.T2(latStr, lonStr);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
    
    public Tuples.T4 solveGPSBoundingBox() {
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                Object o = null;
                boolean found = false;
                while(contextObjects.hasNext() && !found) {
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        if(t != null && !t.isRemoved()) {
                            Collection<Association> as = t.getAssociations(AbstractGeoNamesExtractor.getBBoxTypeTopic(t.getTopicMap()));
                            Association a = null;
                            for(Iterator<Association> ai = as.iterator(); ai.hasNext() && !found; ) {
                                a = ai.next();
                                if(a != null && !a.isRemoved()) {
                                    Topic nt = a.getPlayer(AbstractGeoNamesExtractor.getBBoxNorthTypeTopic(t.getTopicMap()));
                                    Topic st = a.getPlayer(AbstractGeoNamesExtractor.getBBoxSouthTypeTopic(t.getTopicMap()));
                                    Topic et = a.getPlayer(AbstractGeoNamesExtractor.getBBoxEastTypeTopic(t.getTopicMap()));
                                    Topic wt = a.getPlayer(AbstractGeoNamesExtractor.getBBoxWestTypeTopic(t.getTopicMap()));

                                    if(nt != null && !nt.isRemoved() &&
                                       st != null && !st.isRemoved() &&
                                       et != null && !et.isRemoved() &&
                                       wt != null && !wt.isRemoved()) {
                                            String nc = nt.getDisplayName(null);
                                            String sc = st.getDisplayName(null);
                                            String ec = et.getDisplayName(null);
                                            String wc = wt.getDisplayName(null);

                                            return new Tuples.T4(nc, wc, sc, ec);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    public String solveGeonameIdContext() {
        StringBuffer sb = new StringBuffer("");
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                String str = null;
                String lstr = null;
                Object o = null;
                boolean isFirst = true;
                while(contextObjects.hasNext()) {
                    str = null;
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        Collection<Locator> sis = t.getSubjectIdentifiers();
                        Locator l = null;
                        for(Iterator<Locator> sii = sis.iterator(); sii.hasNext(); ) {
                            l = sii.next();
                            if(l != null) {
                                lstr = l.toExternalForm();
                                if(lstr != null && lstr.startsWith(AbstractGeoNamesExtractor.GEONAMEID_SI)) {
                                    str = lstr.substring(AbstractGeoNamesExtractor.GEONAMEID_SI.length());
                                    while(str.startsWith("/")) str = str.substring(1);
                                    while(str.endsWith("/")) str = str.substring(0,str.length()-1);
                                    str = str.trim();
                                    if(str.length() > 0) {
                                        if(!isFirst) {
                                            sb.append(", ");
                                        }
                                        sb.append(str);
                                        isFirst = false;
                                    }
                                }
                                else if(lstr != null && lstr.startsWith("http://sws.geonames.org/")) {
                                    str = lstr.substring("http://sws.geonames.org/".length());
                                    while(str.startsWith("/")) str = str.substring(1);
                                    while(str.endsWith("/")) str = str.substring(0,str.length()-1);
                                    str = str.trim();
                                    if(str.length() > 0) {
                                        if(!isFirst) {
                                            sb.append(", ");
                                        }
                                        sb.append(str);
                                        isFirst = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    

    
    public String getRequestLanguage() {
        String lan = langTextField.getText();
        if(lan != null) {
            lan = lan.trim();
            return lan;
        }
        return "en";
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        geoNamesTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchPanelInner = new javax.swing.JPanel();
        searchLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchTextField = new org.wandora.application.gui.simple.SimpleField();
        searchFilterPanel = new javax.swing.JPanel();
        searchContinentLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchContinentComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        searchCountryLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchCountryComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        searchFeatureLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchFeatureClassComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        searchIsNameRequiredLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchIsNameRequiredCheckBox = new javax.swing.JCheckBox();
        findNearByPanel = new javax.swing.JPanel();
        findNearByPanelInner = new javax.swing.JPanel();
        findNearByLabel = new org.wandora.application.gui.simple.SimpleLabel();
        findNearByCoordinatesPanel = new javax.swing.JPanel();
        findNearByLatLabel = new org.wandora.application.gui.simple.SimpleLabel();
        findNearByLatTextField = new org.wandora.application.gui.simple.SimpleField();
        findNearByLonLabel = new org.wandora.application.gui.simple.SimpleLabel();
        findNearByLonTextField = new org.wandora.application.gui.simple.SimpleField();
        findNearByGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        findNearByFilterPanel = new javax.swing.JPanel();
        findNearByradiusLabel = new SimpleLabel();
        findNearByRadiusTextField = new javax.swing.JTextField();
        findNearByFeatureLabel = new org.wandora.application.gui.simple.SimpleLabel();
        findNearByFeatureComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        countryInfoPanel = new javax.swing.JPanel();
        countryInfoPanelInner = new javax.swing.JPanel();
        countryInfoLabel = new org.wandora.application.gui.simple.SimpleLabel();
        countryInfoComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        countryInfoButton = new org.wandora.application.gui.simple.SimpleButton();
        citiesPanel = new javax.swing.JPanel();
        citiesPanelInner = new javax.swing.JPanel();
        citiesLabel = new org.wandora.application.gui.simple.SimpleLabel();
        citiesCoordinatesPanel = new javax.swing.JPanel();
        citiesNorthTextField = new org.wandora.application.gui.simple.SimpleField();
        citiesSouthTextField = new org.wandora.application.gui.simple.SimpleField();
        citiesEastTextField = new org.wandora.application.gui.simple.SimpleField();
        citiesWestTextField = new org.wandora.application.gui.simple.SimpleField();
        citiesGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        childrenPanel = new javax.swing.JPanel();
        childrenPanelInner = new javax.swing.JPanel();
        childrenLabel = new org.wandora.application.gui.simple.SimpleLabel();
        childrenTextField = new org.wandora.application.gui.simple.SimpleField();
        childrenGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        hierarchyPanel = new javax.swing.JPanel();
        hierarchyPanelInner = new javax.swing.JPanel();
        hierarchyLabel = new org.wandora.application.gui.simple.SimpleLabel();
        hierarchyTextField = new org.wandora.application.gui.simple.SimpleField();
        hierarchyGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        neighboursPanel = new javax.swing.JPanel();
        neighboursPanelInner = new javax.swing.JPanel();
        neigboursLabel = new org.wandora.application.gui.simple.SimpleLabel();
        neighboursTextField = new org.wandora.application.gui.simple.SimpleField();
        neighboursGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        siblingsPanel = new javax.swing.JPanel();
        siblingsPanelInner = new javax.swing.JPanel();
        siblingsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        siblingsTextField = new org.wandora.application.gui.simple.SimpleField();
        siblingsGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        weatherPanel = new javax.swing.JPanel();
        weatherPanelInner = new javax.swing.JPanel();
        weatherLabel = new javax.swing.JLabel();
        weatherCoordinatesPanel = new javax.swing.JPanel();
        weatherLatLabel = new org.wandora.application.gui.simple.SimpleLabel();
        weatherLatTextField = new org.wandora.application.gui.simple.SimpleField();
        weatherLonLabel = new org.wandora.application.gui.simple.SimpleLabel();
        weatherLonTextField = new org.wandora.application.gui.simple.SimpleField();
        weatherGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        wikiSearchPanel = new javax.swing.JPanel();
        wikiSearchPanelInner = new javax.swing.JPanel();
        wikiSearchLabel = new org.wandora.application.gui.simple.SimpleLabel();
        wikiSearchTextField = new org.wandora.application.gui.simple.SimpleField();
        wikiBoxPanel = new javax.swing.JPanel();
        wikiBoxPanelInner = new javax.swing.JPanel();
        wikiBoxLabel = new org.wandora.application.gui.simple.SimpleLabel();
        wikiBoxCoordinatesPanel = new javax.swing.JPanel();
        wikiBoxNorthTextField = new org.wandora.application.gui.simple.SimpleField();
        wikiBoxSouthTextField = new org.wandora.application.gui.simple.SimpleField();
        wikiBoxEastTextField = new org.wandora.application.gui.simple.SimpleField();
        wikiBoxWestTextField = new org.wandora.application.gui.simple.SimpleField();
        wikiBoxGetContextButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        langPanel = new javax.swing.JPanel();
        langTextField = new org.wandora.application.gui.simple.SimpleField();
        langLabel = new org.wandora.application.gui.simple.SimpleLabel();
        fillerPanel = new javax.swing.JPanel();
        forgetButton = new SimpleButton();
        buttonSeparator = new javax.swing.JSeparator();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchPanelInner.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("<html>This tab is used to perform GeoName searches. Write search word below and select possible filters.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        searchPanelInner.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        searchPanelInner.add(searchTextField, gridBagConstraints);

        searchFilterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, org.wandora.application.gui.UIConstants.tabFont));
        searchFilterPanel.setLayout(new java.awt.GridBagLayout());

        searchContinentLabel.setText("continent filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 5);
        searchFilterPanel.add(searchContinentLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 4);
        searchFilterPanel.add(searchContinentComboBox, gridBagConstraints);

        searchCountryLabel.setText("country filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 5);
        searchFilterPanel.add(searchCountryLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 4);
        searchFilterPanel.add(searchCountryComboBox, gridBagConstraints);

        searchFeatureLabel.setText("feature class filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 5);
        searchFilterPanel.add(searchFeatureLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 4);
        searchFilterPanel.add(searchFeatureClassComboBox, gridBagConstraints);

        searchIsNameRequiredLabel.setText("name is required");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 5);
        searchFilterPanel.add(searchIsNameRequiredLabel, gridBagConstraints);

        searchIsNameRequiredCheckBox.setMargin(new java.awt.Insets(1, 0, 1, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 4);
        searchFilterPanel.add(searchIsNameRequiredCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchPanelInner.add(searchFilterPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        searchPanel.add(searchPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Search", searchPanel);

        findNearByPanel.setLayout(new java.awt.GridBagLayout());

        findNearByPanelInner.setLayout(new java.awt.GridBagLayout());

        findNearByLabel.setText("<html>Find geo names near given location. Location is specified by latitude and longitude coordinates. You can also filter result set with given feature class.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        findNearByPanelInner.add(findNearByLabel, gridBagConstraints);

        findNearByCoordinatesPanel.setLayout(new java.awt.GridBagLayout());

        findNearByLatLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        findNearByLatLabel.setText("latitude");
        findNearByLatLabel.setMaximumSize(new java.awt.Dimension(90, 14));
        findNearByLatLabel.setMinimumSize(new java.awt.Dimension(90, 14));
        findNearByLatLabel.setPreferredSize(new java.awt.Dimension(90, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 5);
        findNearByCoordinatesPanel.add(findNearByLatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
        findNearByCoordinatesPanel.add(findNearByLatTextField, gridBagConstraints);

        findNearByLonLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        findNearByLonLabel.setText("longitude");
        findNearByLonLabel.setMaximumSize(new java.awt.Dimension(90, 14));
        findNearByLonLabel.setMinimumSize(new java.awt.Dimension(90, 14));
        findNearByLonLabel.setPreferredSize(new java.awt.Dimension(90, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        findNearByCoordinatesPanel.add(findNearByLonLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        findNearByCoordinatesPanel.add(findNearByLonTextField, gridBagConstraints);

        findNearByGetContextButton.setText("Get context");
        findNearByGetContextButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        findNearByGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNearByGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 1, 0);
        findNearByCoordinatesPanel.add(findNearByGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
        findNearByPanelInner.add(findNearByCoordinatesPanel, gridBagConstraints);

        findNearByFilterPanel.setLayout(new java.awt.GridBagLayout());

        findNearByradiusLabel.setText("radius (km)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 5);
        findNearByFilterPanel.add(findNearByradiusLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
        findNearByFilterPanel.add(findNearByRadiusTextField, gridBagConstraints);

        findNearByFeatureLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        findNearByFeatureLabel.setText("feature class");
        findNearByFeatureLabel.setMaximumSize(new java.awt.Dimension(90, 14));
        findNearByFeatureLabel.setMinimumSize(new java.awt.Dimension(90, 14));
        findNearByFeatureLabel.setPreferredSize(new java.awt.Dimension(90, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        findNearByFilterPanel.add(findNearByFeatureLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        findNearByFilterPanel.add(findNearByFeatureComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        findNearByPanelInner.add(findNearByFilterPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        findNearByPanel.add(findNearByPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Near by", findNearByPanel);

        countryInfoPanel.setLayout(new java.awt.GridBagLayout());

        countryInfoPanelInner.setLayout(new java.awt.GridBagLayout());

        countryInfoLabel.setText("<html>Get country related data from GeoNames. Select country name or write two letter geonames country code(s) below. Separate different country codes with comma (,) character. Use keyword ALL to get all available countries. You can also try to get country codes from context topics.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        countryInfoPanelInner.add(countryInfoLabel, gridBagConstraints);

        countryInfoComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        countryInfoPanelInner.add(countryInfoComboBox, gridBagConstraints);

        countryInfoButton.setText("Get context");
        countryInfoButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        countryInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countryInfoButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        countryInfoPanelInner.add(countryInfoButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        countryInfoPanel.add(countryInfoPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Country info", countryInfoPanel);

        citiesPanel.setLayout(new java.awt.GridBagLayout());

        citiesPanelInner.setLayout(new java.awt.GridBagLayout());

        citiesLabel.setText("<html>Get cities in bounding box. Write bounding box coordinates below. You can also try to get coordinates from context topics.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        citiesPanelInner.add(citiesLabel, gridBagConstraints);

        citiesCoordinatesPanel.setPreferredSize(new java.awt.Dimension(400, 60));
        citiesCoordinatesPanel.setLayout(new java.awt.GridBagLayout());

        citiesNorthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        citiesNorthTextField.setText("north");
        citiesNorthTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        citiesNorthTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        citiesNorthTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                citiesNorthTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        citiesCoordinatesPanel.add(citiesNorthTextField, gridBagConstraints);

        citiesSouthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        citiesSouthTextField.setText("south");
        citiesSouthTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        citiesSouthTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        citiesSouthTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                citiesSouthTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        citiesCoordinatesPanel.add(citiesSouthTextField, gridBagConstraints);

        citiesEastTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        citiesEastTextField.setText("east");
        citiesEastTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        citiesEastTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        citiesEastTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                citiesEastTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        citiesCoordinatesPanel.add(citiesEastTextField, gridBagConstraints);

        citiesWestTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        citiesWestTextField.setText("west");
        citiesWestTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        citiesWestTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        citiesWestTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                citiesWestTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        citiesCoordinatesPanel.add(citiesWestTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        citiesPanelInner.add(citiesCoordinatesPanel, gridBagConstraints);

        citiesGetContextButton.setText("Get context");
        citiesGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        citiesGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                citiesGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        citiesPanelInner.add(citiesGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        citiesPanel.add(citiesPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Cities", citiesPanel);

        childrenPanel.setLayout(new java.awt.GridBagLayout());

        childrenPanelInner.setLayout(new java.awt.GridBagLayout());

        childrenLabel.setText("<html>Get GeoName children of a given geonameId and convert the feed to a topic map. Please write geonameIds below. Use comma (,) character to separate ids. You can also try to get ids from the context.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        childrenPanelInner.add(childrenLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        childrenPanelInner.add(childrenTextField, gridBagConstraints);

        childrenGetContextButton.setText("Get context");
        childrenGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        childrenGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        childrenPanelInner.add(childrenGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        childrenPanel.add(childrenPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Children", childrenPanel);

        hierarchyPanel.setLayout(new java.awt.GridBagLayout());

        hierarchyPanelInner.setLayout(new java.awt.GridBagLayout());

        hierarchyLabel.setText("<html>Get GeoName hierarchy of a given geonameId and convert the feed to a topic map. Please write geonameIds below. Use comma (,) character to separate ids. You can also try to get ids from the context.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        hierarchyPanelInner.add(hierarchyLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        hierarchyPanelInner.add(hierarchyTextField, gridBagConstraints);

        hierarchyGetContextButton.setLabel("Get context");
        hierarchyGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        hierarchyGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hierarchyGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        hierarchyPanelInner.add(hierarchyGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        hierarchyPanel.add(hierarchyPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Hierarchy", hierarchyPanel);

        neighboursPanel.setLayout(new java.awt.GridBagLayout());

        neighboursPanelInner.setLayout(new java.awt.GridBagLayout());

        neigboursLabel.setText("<html>Get neighbours of a given geonameId and convert the feed to a topic map. Please write ids below. Use comma (,) character as id separator. You can also try to get ids from the context.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        neighboursPanelInner.add(neigboursLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        neighboursPanelInner.add(neighboursTextField, gridBagConstraints);

        neighboursGetContextButton.setText("Get context");
        neighboursGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        neighboursGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neighboursGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        neighboursPanelInner.add(neighboursGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        neighboursPanel.add(neighboursPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Neighbours", neighboursPanel);

        siblingsPanel.setLayout(new java.awt.GridBagLayout());

        siblingsPanelInner.setLayout(new java.awt.GridBagLayout());

        siblingsLabel.setText("<html>Get siblings of a given geonameId and convert the feed to a topic map. Please write ids below. Use comma (,) character as id separator. You can also try to get ids from the context.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        siblingsPanelInner.add(siblingsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        siblingsPanelInner.add(siblingsTextField, gridBagConstraints);

        siblingsGetContextButton.setText("Get context");
        siblingsGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        siblingsGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siblingsGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        siblingsPanelInner.add(siblingsGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        siblingsPanel.add(siblingsPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Siblings", siblingsPanel);

        weatherPanel.setLayout(new java.awt.GridBagLayout());

        weatherPanelInner.setLayout(new java.awt.GridBagLayout());

        weatherLabel.setText("<html>Fetch weather data for given geo location.The service will return weather station closest to given point.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        weatherPanelInner.add(weatherLabel, gridBagConstraints);

        weatherCoordinatesPanel.setLayout(new java.awt.GridBagLayout());

        weatherLatLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        weatherLatLabel.setText("latitude");
        weatherLatLabel.setMaximumSize(new java.awt.Dimension(80, 14));
        weatherLatLabel.setMinimumSize(new java.awt.Dimension(80, 14));
        weatherLatLabel.setPreferredSize(new java.awt.Dimension(70, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 5);
        weatherCoordinatesPanel.add(weatherLatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
        weatherCoordinatesPanel.add(weatherLatTextField, gridBagConstraints);

        weatherLonLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        weatherLonLabel.setText("longitude");
        weatherLonLabel.setMaximumSize(new java.awt.Dimension(80, 14));
        weatherLonLabel.setMinimumSize(new java.awt.Dimension(80, 14));
        weatherLonLabel.setPreferredSize(new java.awt.Dimension(70, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        weatherCoordinatesPanel.add(weatherLonLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        weatherCoordinatesPanel.add(weatherLonTextField, gridBagConstraints);

        weatherGetContextButton.setText("Get context");
        weatherGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        weatherGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                weatherGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 1, 0);
        weatherCoordinatesPanel.add(weatherGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
        weatherPanelInner.add(weatherCoordinatesPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        weatherPanel.add(weatherPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Weather", weatherPanel);

        wikiSearchPanel.setLayout(new java.awt.GridBagLayout());

        wikiSearchPanelInner.setLayout(new java.awt.GridBagLayout());

        wikiSearchLabel.setText("<html>This tab is used to perform GeoName Wikipedia searches. Please write search word below.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        wikiSearchPanelInner.add(wikiSearchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        wikiSearchPanelInner.add(wikiSearchTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        wikiSearchPanel.add(wikiSearchPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Wikipedia search", wikiSearchPanel);

        wikiBoxPanel.setLayout(new java.awt.GridBagLayout());

        wikiBoxPanelInner.setLayout(new java.awt.GridBagLayout());

        wikiBoxLabel.setText("<html>Get Wikipedia geo articles in bounding box. Write bounding box coordinates below. You can also try to get coordinates from context topics.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        wikiBoxPanelInner.add(wikiBoxLabel, gridBagConstraints);

        wikiBoxCoordinatesPanel.setPreferredSize(new java.awt.Dimension(400, 60));
        wikiBoxCoordinatesPanel.setLayout(new java.awt.GridBagLayout());

        wikiBoxNorthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        wikiBoxNorthTextField.setText("north");
        wikiBoxNorthTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        wikiBoxNorthTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        wikiBoxNorthTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wikiBoxNorthTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        wikiBoxCoordinatesPanel.add(wikiBoxNorthTextField, gridBagConstraints);

        wikiBoxSouthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        wikiBoxSouthTextField.setText("south");
        wikiBoxSouthTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        wikiBoxSouthTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        wikiBoxSouthTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wikiBoxSouthTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        wikiBoxCoordinatesPanel.add(wikiBoxSouthTextField, gridBagConstraints);

        wikiBoxEastTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        wikiBoxEastTextField.setText("east");
        wikiBoxEastTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        wikiBoxEastTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        wikiBoxEastTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wikiBoxEastTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        wikiBoxCoordinatesPanel.add(wikiBoxEastTextField, gridBagConstraints);

        wikiBoxWestTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        wikiBoxWestTextField.setText("west");
        wikiBoxWestTextField.setMinimumSize(new java.awt.Dimension(100, 23));
        wikiBoxWestTextField.setPreferredSize(new java.awt.Dimension(100, 23));
        wikiBoxWestTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wikiBoxWestTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        wikiBoxCoordinatesPanel.add(wikiBoxWestTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        wikiBoxPanelInner.add(wikiBoxCoordinatesPanel, gridBagConstraints);

        wikiBoxGetContextButton.setText("Get context");
        wikiBoxGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        wikiBoxGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikiBoxGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        wikiBoxPanelInner.add(wikiBoxGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        wikiBoxPanel.add(wikiBoxPanelInner, gridBagConstraints);

        geoNamesTabbedPane.addTab("Wikipedia b-box", wikiBoxPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(geoNamesTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        langPanel.setLayout(new java.awt.GridBagLayout());

        langTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        langTextField.setText("en");
        langTextField.setMinimumSize(new java.awt.Dimension(30, 23));
        langTextField.setPreferredSize(new java.awt.Dimension(30, 23));
        langPanel.add(langTextField, new java.awt.GridBagConstraints());

        langLabel.setText("request language");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        langPanel.add(langLabel, gridBagConstraints);

        buttonPanel.add(langPanel, new java.awt.GridBagConstraints());

        fillerPanel.setPreferredSize(new java.awt.Dimension(5, 5));

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 116, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        forgetButton.setText("Forget username");
        forgetButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        forgetButton.setMaximumSize(new java.awt.Dimension(110, 23));
        forgetButton.setMinimumSize(new java.awt.Dimension(110, 23));
        forgetButton.setPreferredSize(new java.awt.Dimension(110, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        buttonPanel.add(forgetButton, gridBagConstraints);

        buttonSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        buttonSeparator.setMinimumSize(new java.awt.Dimension(1, 15));
        buttonSeparator.setPreferredSize(new java.awt.Dimension(1, 15));
        buttonPanel.add(buttonSeparator, new java.awt.GridBagConstraints());

        okButton.setText("Extract");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setPreferredSize(new java.awt.Dimension(75, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 3, 4, 0);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    accepted = true;
    setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    accepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void countryInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countryInfoButtonActionPerformed
    solveCountryContext();
}//GEN-LAST:event_countryInfoButtonActionPerformed

private void citiesNorthTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_citiesNorthTextFieldFocusGained
    if("north".equals(citiesNorthTextField.getText())) {
        citiesNorthTextField.setText("");
    }
}//GEN-LAST:event_citiesNorthTextFieldFocusGained

private void citiesWestTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_citiesWestTextFieldFocusGained
    if("west".equals(citiesWestTextField.getText())) {
        citiesWestTextField.setText("");
    }
}//GEN-LAST:event_citiesWestTextFieldFocusGained

private void citiesSouthTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_citiesSouthTextFieldFocusGained
    if("south".equals(citiesSouthTextField.getText())) {
        citiesSouthTextField.setText("");
    }
}//GEN-LAST:event_citiesSouthTextFieldFocusGained

private void citiesEastTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_citiesEastTextFieldFocusGained
    if("east".equals(citiesEastTextField.getText())) {
        citiesEastTextField.setText("");
    }
}//GEN-LAST:event_citiesEastTextFieldFocusGained

private void childrenGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenGetContextButtonActionPerformed
    String str = solveGeonameIdContext();
    if(str == null) str = "";
    childrenTextField.setText(str);
}//GEN-LAST:event_childrenGetContextButtonActionPerformed

private void citiesGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_citiesGetContextButtonActionPerformed
    Tuples.T4 gpsRect = solveGPSBoundingBox();
    if(gpsRect != null) {
        try {
            citiesNorthTextField.setText(gpsRect.e1.toString());
            citiesWestTextField.setText(gpsRect.e2.toString());
            citiesSouthTextField.setText(gpsRect.e3.toString());
            citiesEastTextField.setText(gpsRect.e4.toString());
        }
        catch(Exception e) {}
    }
}//GEN-LAST:event_citiesGetContextButtonActionPerformed

private void hierarchyGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hierarchyGetContextButtonActionPerformed
    String str = solveGeonameIdContext();
    if(str == null) str = "";
    hierarchyTextField.setText(str);
}//GEN-LAST:event_hierarchyGetContextButtonActionPerformed

private void neighboursGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neighboursGetContextButtonActionPerformed
    String str = solveGeonameIdContext();
    if(str == null) str = "";
    neighboursTextField.setText(str);
}//GEN-LAST:event_neighboursGetContextButtonActionPerformed

private void siblingsGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_siblingsGetContextButtonActionPerformed
    String str = solveGeonameIdContext();
    if(str == null) str = "";
    siblingsTextField.setText(str);
}//GEN-LAST:event_siblingsGetContextButtonActionPerformed

private void findNearByGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findNearByGetContextButtonActionPerformed
    Tuples.T2 geoLocation = solveGPSLocation();
    if(geoLocation != null) {
        try {
            findNearByLatTextField.setText(geoLocation.e1.toString());
            findNearByLonTextField.setText(geoLocation.e2.toString());
        }catch(Exception e) {}
    }
    else {
        Tuples.T4 gpsRect = solveGPSBoundingBox();
        if(gpsRect != null) {
            try {
                double n = Double.parseDouble(gpsRect.e1.toString());
                double w = Double.parseDouble(gpsRect.e2.toString());
                double s = Double.parseDouble(gpsRect.e3.toString());
                double e = Double.parseDouble(gpsRect.e4.toString());
                
                double lat = n + ((s-n)/2);
                double lon = w + ((e-w)/2);
                
                findNearByLatTextField.setText("" + lat);
                findNearByLonTextField.setText("" + lon);
            }
            catch(Exception e) {}
        }
    }
}//GEN-LAST:event_findNearByGetContextButtonActionPerformed

private void weatherGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_weatherGetContextButtonActionPerformed
    Tuples.T2 geoLocation = solveGPSLocation();
    if(geoLocation != null) {
        try {
            weatherLatTextField.setText(geoLocation.e1.toString());
            weatherLonTextField.setText(geoLocation.e2.toString());
        }catch(Exception e) {}
    }
    else {
        Tuples.T4 gpsRect = solveGPSBoundingBox();
        if(gpsRect != null) {
            try {
                double n = Double.parseDouble(gpsRect.e1.toString());
                double w = Double.parseDouble(gpsRect.e2.toString());
                double s = Double.parseDouble(gpsRect.e3.toString());
                double e = Double.parseDouble(gpsRect.e4.toString());
                
                double lat = n + ((s-n)/2);
                double lon = w + ((e-w)/2);
                
                weatherLatTextField.setText("" + lat);
                weatherLonTextField.setText("" + lon);
            }
            catch(Exception e) {}
        }
    }
}//GEN-LAST:event_weatherGetContextButtonActionPerformed

private void wikiBoxNorthTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wikiBoxNorthTextFieldFocusGained
    if("north".equals(wikiBoxNorthTextField.getText())) {
        wikiBoxNorthTextField.setText("");
    }
}//GEN-LAST:event_wikiBoxNorthTextFieldFocusGained

private void wikiBoxSouthTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wikiBoxSouthTextFieldFocusGained
    if("south".equals(wikiBoxNorthTextField.getText())) {
        wikiBoxNorthTextField.setText("");
    }
}//GEN-LAST:event_wikiBoxSouthTextFieldFocusGained

private void wikiBoxEastTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wikiBoxEastTextFieldFocusGained
    if("east".equals(wikiBoxNorthTextField.getText())) {
        wikiBoxNorthTextField.setText("");
    }
}//GEN-LAST:event_wikiBoxEastTextFieldFocusGained

private void wikiBoxWestTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wikiBoxWestTextFieldFocusGained
    if("west".equals(wikiBoxNorthTextField.getText())) {
        wikiBoxNorthTextField.setText("");
    }
}//GEN-LAST:event_wikiBoxWestTextFieldFocusGained

private void wikiBoxGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikiBoxGetContextButtonActionPerformed
    Tuples.T4 gpsRect = solveGPSBoundingBox();
    if(gpsRect != null) {
        try {
            wikiBoxNorthTextField.setText(gpsRect.e1.toString());
            wikiBoxWestTextField.setText(gpsRect.e2.toString());
            wikiBoxSouthTextField.setText(gpsRect.e3.toString());
            wikiBoxEastTextField.setText(gpsRect.e4.toString());
        }
        catch(Exception e) {}
    }
    else {
        Tuples.T2 geoLocation = solveGPSLocation();
        if(geoLocation != null) {
            try {
                double c1 = Double.parseDouble(geoLocation.e1.toString());
                double c2 = Double.parseDouble(geoLocation.e2.toString());
                
                String radiusStr = WandoraOptionPane.showInputDialog(wandora, "Found only single geo location. To convert found geo location to bounding box give radius:", "1.0", "Radius?");
                if(radiusStr != null && radiusStr.length() > 0) {
                    double radius = Double.parseDouble(radiusStr);
                    wikiBoxNorthTextField.setText(""+ (c1-radius));
                    wikiBoxWestTextField.setText(""+ (c2-radius));
                    wikiBoxSouthTextField.setText(""+ (c1+radius));
                    wikiBoxEastTextField.setText("" + (c2+radius));
                }
            }
            catch(Exception e) {}
        }
    }
}//GEN-LAST:event_wikiBoxGetContextButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JSeparator buttonSeparator;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton childrenGetContextButton;
    private javax.swing.JLabel childrenLabel;
    private javax.swing.JPanel childrenPanel;
    private javax.swing.JPanel childrenPanelInner;
    private javax.swing.JTextField childrenTextField;
    private javax.swing.JPanel citiesCoordinatesPanel;
    private javax.swing.JTextField citiesEastTextField;
    private javax.swing.JButton citiesGetContextButton;
    private javax.swing.JLabel citiesLabel;
    private javax.swing.JTextField citiesNorthTextField;
    private javax.swing.JPanel citiesPanel;
    private javax.swing.JPanel citiesPanelInner;
    private javax.swing.JTextField citiesSouthTextField;
    private javax.swing.JTextField citiesWestTextField;
    private javax.swing.JButton countryInfoButton;
    private javax.swing.JComboBox countryInfoComboBox;
    private javax.swing.JLabel countryInfoLabel;
    private javax.swing.JPanel countryInfoPanel;
    private javax.swing.JPanel countryInfoPanelInner;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel findNearByCoordinatesPanel;
    private javax.swing.JComboBox findNearByFeatureComboBox;
    private javax.swing.JLabel findNearByFeatureLabel;
    private javax.swing.JPanel findNearByFilterPanel;
    private javax.swing.JButton findNearByGetContextButton;
    private javax.swing.JLabel findNearByLabel;
    private javax.swing.JLabel findNearByLatLabel;
    private javax.swing.JTextField findNearByLatTextField;
    private javax.swing.JLabel findNearByLonLabel;
    private javax.swing.JTextField findNearByLonTextField;
    private javax.swing.JPanel findNearByPanel;
    private javax.swing.JPanel findNearByPanelInner;
    private javax.swing.JTextField findNearByRadiusTextField;
    private javax.swing.JLabel findNearByradiusLabel;
    private javax.swing.JButton forgetButton;
    private javax.swing.JTabbedPane geoNamesTabbedPane;
    private javax.swing.JButton hierarchyGetContextButton;
    private javax.swing.JLabel hierarchyLabel;
    private javax.swing.JPanel hierarchyPanel;
    private javax.swing.JPanel hierarchyPanelInner;
    private javax.swing.JTextField hierarchyTextField;
    private javax.swing.JLabel langLabel;
    private javax.swing.JPanel langPanel;
    private javax.swing.JTextField langTextField;
    private javax.swing.JLabel neigboursLabel;
    private javax.swing.JButton neighboursGetContextButton;
    private javax.swing.JPanel neighboursPanel;
    private javax.swing.JPanel neighboursPanelInner;
    private javax.swing.JTextField neighboursTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox searchContinentComboBox;
    private javax.swing.JLabel searchContinentLabel;
    private javax.swing.JComboBox searchCountryComboBox;
    private javax.swing.JLabel searchCountryLabel;
    private javax.swing.JComboBox searchFeatureClassComboBox;
    private javax.swing.JLabel searchFeatureLabel;
    private javax.swing.JPanel searchFilterPanel;
    private javax.swing.JCheckBox searchIsNameRequiredCheckBox;
    private javax.swing.JLabel searchIsNameRequiredLabel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel searchPanelInner;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton siblingsGetContextButton;
    private javax.swing.JLabel siblingsLabel;
    private javax.swing.JPanel siblingsPanel;
    private javax.swing.JPanel siblingsPanelInner;
    private javax.swing.JTextField siblingsTextField;
    private javax.swing.JPanel weatherCoordinatesPanel;
    private javax.swing.JButton weatherGetContextButton;
    private javax.swing.JLabel weatherLabel;
    private javax.swing.JLabel weatherLatLabel;
    private javax.swing.JTextField weatherLatTextField;
    private javax.swing.JLabel weatherLonLabel;
    private javax.swing.JTextField weatherLonTextField;
    private javax.swing.JPanel weatherPanel;
    private javax.swing.JPanel weatherPanelInner;
    private javax.swing.JPanel wikiBoxCoordinatesPanel;
    private javax.swing.JTextField wikiBoxEastTextField;
    private javax.swing.JButton wikiBoxGetContextButton;
    private javax.swing.JLabel wikiBoxLabel;
    private javax.swing.JTextField wikiBoxNorthTextField;
    private javax.swing.JPanel wikiBoxPanel;
    private javax.swing.JPanel wikiBoxPanelInner;
    private javax.swing.JTextField wikiBoxSouthTextField;
    private javax.swing.JTextField wikiBoxWestTextField;
    private javax.swing.JLabel wikiSearchLabel;
    private javax.swing.JPanel wikiSearchPanel;
    private javax.swing.JPanel wikiSearchPanelInner;
    private javax.swing.JTextField wikiSearchTextField;
    // End of variables declaration//GEN-END:variables

    
    
    
    
    
    
    // -------------------------------------------------------------------------
    


    private static String apikey = null;
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Please give a valid Geonames username. You can register your username at http://www.geonames.org/login", apikey, "GeoName username", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }



    
    public void forgetAuthorization() {
        apikey = null;
    }

    
    
}
