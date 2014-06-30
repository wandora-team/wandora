/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
 *
 * OmaKaupunkiSelector.java
 *
 * Created on Oct 5, 2011, 8:11:32 PM
 */

package org.wandora.application.tools.extractors.omakaupunki;

import java.awt.Component;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.Topic;

/**
 *
 * @author akivela
 */


public class OmaKaupunkiSelector extends javax.swing.JPanel {

    public static final String BASE_URL = "http://api.omakaupunki.fi/v1/";
    
    private boolean wasAccepted = false;
    private JDialog myDialog = null;
    private Wandora wandora = null;
    private Context context = null;
    
    
    /** Creates new form OmaKaupunkiExtractorDialog */
    public OmaKaupunkiSelector() {
        initComponents();
        eventAreaComboBox.setEditable(false);
        eventCategoriesComboBox.setEditable(false);
        searchServiceAreaComboBox.setEditable(false);
        searchServiceCategoriesComboBox.setEditable(false);
        
        String[] categories = getSearchCategories();
        for(int i=0; i<categories.length; i++) {
            eventCategoriesComboBox.addItem(categories[i]);
        }
    }
    
    
    public void setContext(Context c) {
        context = c;
    }
    
    public void setWandora(Wandora w) {
        wandora = w;
    }
    
    public void setAccepted(boolean isAccepted) {
        wasAccepted = isAccepted;
    }
    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    
    public void openDialog(Wandora w) {
        wasAccepted = false;
        if(myDialog == null) {
            myDialog = new JDialog(w, true);
            myDialog.setTitle("Oma kaupunki extractor");
            wandora = w;
            myDialog.add(this);
            myDialog.setSize(600,300);
            UIBox.centerWindow(myDialog, w);
        }
        myDialog.setVisible(true);
    }
    
    
    
    public WandoraTool getWandoraTool(OmaKaupunkiExtractor okm) {
        Component component = omaKaupunkiTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        String akey = solveAPIKey();
        String extractUrl = BASE_URL;
        String pagesStr = pageTextField.getText();
        long[] pages = parsePages(pagesStr);
        
        // ***** EVENTS *****
        if(eventPanel.equals(component)) {
            if(allEventsRadioButton.isSelected()) {
                extractUrl += "event";
                extractUrl += "?api_key="+urlEncode(akey);
                String[] extractUrls = addPageParam(extractUrl, pages);
                OmaKaupunkiEventExtractor e = new OmaKaupunkiEventExtractor();
                e.setForceUrls( extractUrls );
                wt = e;
            }
            else if(singleEventRadioButton.isSelected()) {
                String eventId = eventIdTextField.getText();
                extractUrl += "event/"+urlEncode(eventId);
                extractUrl += "?api_key="+urlEncode(akey);
                OmaKaupunkiEventExtractor e = new OmaKaupunkiEventExtractor();
                e.setForceUrls( new String[] { extractUrl } );
                wt = e;
            }
            else if(eventCategoriesRadioButton.isSelected()) {
                extractUrl += "event/categories";
                extractUrl += "?api_key="+urlEncode(akey);
                String[] extractUrls = addPageParam(extractUrl, pages);
                OmaKaupunkiEventCategoriesExtractor e = new OmaKaupunkiEventCategoriesExtractor();
                e.setForceUrls( extractUrls );
                wt = e;
            }
            /*
            else if(searchEventsRadioButton.isSelected()) {
                extractUrl += "event/categories/"+eventCategoryIdTextField.getText();
            }
             */
            else if(searchEventsRadioButton.isSelected()) {
                String areaFilter = eventAreaComboBox.getSelectedItem().toString();
                String categoryFilter = eventCategoriesComboBox.getSelectedItem().toString();
                
                extractUrl += "event/search";
                extractUrl += "?text="+urlEncode(searchEventsTextField.getText());
                if(!"All areas".equalsIgnoreCase(areaFilter)) {
                    extractUrl += "&area="+urlEncode(areaFilter.toLowerCase());
                }
                if(!"All categories".equalsIgnoreCase(categoryFilter)) {
                    extractUrl += "&category="+urlEncode(categoryFilter);
                }
                extractUrl += "&api_key="+urlEncode(akey);
                
                //System.out.println("extractUrl=="+extractUrl);
                
                OmaKaupunkiEventExtractor e = new OmaKaupunkiEventExtractor();
                e.setForceUrls( new String[] { extractUrl } );
                wt = e;
            }
        }
                
                
        // ***** SERVICES ******
        else if(directoryPanel.equals(component)) {
            if(allServicesRadioButton.isSelected()) {
                extractUrl += "directory";
                extractUrl += "?api_key="+urlEncode(akey);
                String[] extractUrls = addPageParam(extractUrl, pages);
                OmaKaupunkiDirectoryExtractor e = new OmaKaupunkiDirectoryExtractor();
                e.setForceUrls( extractUrls );
                wt = e;
            }
            else if(singleServiceRadioButton.isSelected()) {
                String serviceIds = singleServiceTextField.getText();
                String[] serviceIdArray = serviceIds.split(",");
                ArrayList<String> extractUrlList = new ArrayList();
                for(int i=0; i<serviceIdArray.length; i++) {
                    extractUrl = BASE_URL;
                    extractUrl += "directory";
                    extractUrl += "/"+urlEncode(serviceIdArray[i]);
                    extractUrl += "?api_key="+urlEncode(akey);
                    extractUrlList.add(extractUrl);
                }
                if(!extractUrlList.isEmpty()) {
                    OmaKaupunkiDirectoryExtractor e = new OmaKaupunkiDirectoryExtractor();
                    e.setForceUrls( extractUrlList.toArray( new String[] {} ) );
                    wt = e;
                }
            }
            else if(serviceCategoriesRadioButton.isSelected()) {
                extractUrl += "directory/categories";
                extractUrl += "?api_key="+urlEncode(akey);
                OmaKaupunkiDirectoryCategoriesExtractor e = new OmaKaupunkiDirectoryCategoriesExtractor();
                e.setForceUrls( new String[] { extractUrl } );
                wt = e;
            }
            else if(searchServicesRadioButton.isSelected()) {
                String areaFilter = searchServiceAreaComboBox.getSelectedItem().toString();
                String categoryFilter = searchServiceCategoriesComboBox.getSelectedItem().toString();
                
                extractUrl += "directory/search";
                extractUrl += "?text="+urlEncode(searchServicesTextField.getText());
                if(areaFilter != null && !"All areas".equalsIgnoreCase(areaFilter)) {
                    extractUrl += "&area="+urlEncode(areaFilter.toLowerCase());
                }
                if(categoryFilter != null && !"All categories".equalsIgnoreCase(categoryFilter)) {
                    extractUrl += "&category="+urlEncode(categoryFilter.toLowerCase());
                }
                extractUrl += "&api_key="+urlEncode(akey);
                String[] extractUrls = addPageParam(extractUrl, pages);
                OmaKaupunkiDirectoryExtractor e = new OmaKaupunkiDirectoryExtractor();
                e.setForceUrls( extractUrls );
                wt = e;
            }

        }
        return wt;
    }
    
    
    
    
    
    
    protected String[] addPageParam(String url, long[] pages) {
        String[] urls = new String[pages.length];
        for(int i=0; i<pages.length; i++) {
            String u = url + "&page="+pages[i];
            urls[i] = u;
        }
        return urls;
    }
    
    
    
    protected long[] parsePages(String pagesStr) {
        ArrayList<Long> pages = new ArrayList<Long>();
        String[] p1 = pagesStr.split(",");
        for(int i=0; i<p1.length; i++) {
            String pageStr = p1[i];
            if(pageStr.indexOf("-") != -1) {
                String[] pageDis = pageStr.split("-");
                if(pageDis.length > 1) {
                    String firstStr = pageDis[0].trim();
                    String lastStr = pageDis[pageDis.length-1].trim();
                    try {
                        long first = Long.parseLong(firstStr);
                        long last = Long.parseLong(lastStr);
                        for(long p=first; p<=last; p++) {
                            Long pl = new Long(p);
                            if(!pages.contains(pl)) {
                                pages.add(pl);
                            }
                        }
                    }
                    catch(Exception e) {
                        // SKIP
                    }
                }
            }
            else {
                try {
                    long p = Long.parseLong(pageStr.trim());
                    if(p > 0) {
                        Long pl = new Long(p);
                        if(!pages.contains(pl)) {
                            pages.add(pl);
                        }
                    }
                }
                catch(Exception e) {
                    // SKIP
                }
            }
        }
        long[] pgs = new long[pages.size()];
        for(int i=0; i<pages.size(); i++) {
            pgs[i] = pages.get(i).longValue();
        }
        return pgs;
    }
    
    
    protected static String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {}
        return str;
    }

    
    
    
    
    protected String[] getSearchCategories() {
        return new String[] {
            "Amerikkalainen jalkapallo",
            "Ampumahiihto",
            "Autourheilu",
            "Blues",
            "Elokuva",
            "Elokuva-arkisto",
            "Etno",
            "Festarilehti",
            "Futsal",
            "Golf",
            "Hiihto",
            "Hölkkä",
            "Iskelmä",
            "Jalkapallo",
            "Jazz",
            "Juhlaviikot",
            "Jääkiekko",
            "Jääpallo",
            "Kaukalopallo",
            "Keilailu",
            "Kirjallisuus",
            "Kirpputorit",
            "Klassinen",
            "Klubit",
            "Koripallo",
            "Kuntoilu",
            "Kurssit",
            "Käsipallo",
            "Lapsille",
            "Lastentapahtumat",
            "Lentopallo",
            "Melonta",
            "Messut",
            "Miekkailu",
            "Moottoripyöräily",
            "Musiikki",
            "Muut elokuvat",
            "Muut menot",
            "Muut tapahtumat",
            "Myyjäiset",
            "Nyrkkeily",
            "Näyttelyt",
            "Ooppera",
            "Paini",
            "Pesäpallo",
            "Pikaluistelu",
            "Pyöräily",
            "Pöytätennis",
            "Ravintolavinkki",
            "Retket",
            "Ringette",
            "Rock",
            "Rock & jazz",
            "Rugby",
            "Salibandy",
            "Squash",
            "Sulkapallo",
            "Suunnistus",
            "Taitoluistelu",
            "Tanssi",
            "Tanssit",
            "Tanssiurheilu",
            "Teatteri",
            "Teatterifestivaali",
            "Teatteriravintolat",
            "Tennis",
            "Urheilu",
            "Vesipallo",
            "Voimistelu",
            "Yleisurheilu",
            "Yleisöluennot",
        };
    }
    
    
    
    public void solveServiceContext() {
        StringBuilder serviceIds = new StringBuilder("");
        try {
            Iterator contextIterator = context.getContextObjects();
            Topic serviceIdentifierType = Wandora.getWandora().getTopicMap().getTopic(OmaKaupunkiAbstractExtractor.OMA_KAUPUNKI_SERVICE_IDENTIFIER_SI);
            while(contextIterator.hasNext()) {
                Object contextObject = contextIterator.next();
                System.out.println("Found context object: "+contextObject);
                if(contextObject != null) {
                    if(contextObject instanceof Topic) {
                        Topic contextTopic = (Topic) contextObject;
                        String id = contextTopic.getData(serviceIdentifierType, "fi");
                        System.out.println("  it is a topic: "+contextTopic.getBaseName());
                        if(id != null && id.length() > 0) {
                            System.out.println("    it has an service id: "+contextTopic.getBaseName());
                            if(serviceIds.length() > 0) serviceIds.append(",");
                            serviceIds.append(id);
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(serviceIds != null && serviceIds.length() > 0) {
            singleServiceTextField.setText(serviceIds.toString());
        }
    }
    
    
    
    public void solveEventContext() {
        StringBuilder eventIds = new StringBuilder("");
        try {
            Iterator contextIterator = context.getContextObjects();
            Topic eventIdentifierType = Wandora.getWandora().getTopicMap().getTopic(OmaKaupunkiAbstractExtractor.OMA_KAUPUNKI_EVENT_IDENTIFIER_SI);
            while(contextIterator.hasNext()) {
                Object contextObject = contextIterator.next();
                System.out.println("Found context object: "+contextObject);
                if(contextObject != null) {
                    if(contextObject instanceof Topic) {
                        Topic contextTopic = (Topic) contextObject;
                        String id = contextTopic.getData(eventIdentifierType, "fi");
                        System.out.println("  it is a topic: "+contextTopic.getBaseName());
                        if(id != null && id.length() > 0) {
                            System.out.println("    it has an event id: "+contextTopic.getBaseName());
                            if(eventIds.length() > 0) eventIds.append(",");
                            eventIds.append(id);
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(eventIds != null && eventIds.length() > 0) {
            eventIdTextField.setText(eventIds.toString());
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        directorySelectionGroup = new javax.swing.ButtonGroup();
        eventSelectionGroup = new javax.swing.ButtonGroup();
        eventAreaComboBox = new SimpleComboBox();
        eventCategoriesComboBox = new SimpleComboBox();
        searchServiceAreaComboBox = new SimpleComboBox();
        searchServiceCategoriesComboBox = new SimpleComboBox();
        omaKaupunkiTabbedPane = new SimpleTabbedPane();
        eventPanel = new javax.swing.JPanel();
        eventPanelInner = new javax.swing.JPanel();
        eventInfoLabel = new SimpleLabel();
        allEventsRadioButton = new SimpleRadioButton();
        eventCategoriesRadioButton = new SimpleRadioButton();
        singleEventPanel = new javax.swing.JPanel();
        singleEventRadioButton = new SimpleRadioButton();
        eventIdTextField = new SimpleField();
        getEventContextButton = new SimpleButton();
        searchEventsPanel = new javax.swing.JPanel();
        searchEventsRadioButton = new SimpleRadioButton();
        searchEventsTextField = new SimpleField();
        directoryPanel = new javax.swing.JPanel();
        directoryPanelInner = new javax.swing.JPanel();
        directoryInfoLabel = new SimpleLabel();
        allServicesRadioButton = new SimpleRadioButton();
        serviceCategoriesRadioButton = new SimpleRadioButton();
        singleServicePanel = new javax.swing.JPanel();
        singleServiceRadioButton = new SimpleRadioButton();
        singleServiceTextField = new SimpleField();
        getServiceContextButton = new SimpleButton();
        searchServicesPanel = new javax.swing.JPanel();
        searchServicesRadioButton = new SimpleRadioButton();
        searchServicesTextField = new SimpleField();
        buttonPanel = new javax.swing.JPanel();
        paginationPanel = new javax.swing.JPanel();
        pageLabel = new SimpleLabel();
        pageTextField = new SimpleField();
        buttonFillerPanel = new javax.swing.JPanel();
        extractButton = new SimpleButton();
        cancelButton = new SimpleButton();

        eventAreaComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All areas", "Helsinki", "Vantaa", "Espoo" }));
        eventAreaComboBox.setMinimumSize(new java.awt.Dimension(120, 20));
        eventAreaComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        eventAreaComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                eventAreaComboBoxMouseReleased(evt);
            }
        });

        eventCategoriesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All categories" }));
        eventCategoriesComboBox.setMinimumSize(new java.awt.Dimension(120, 20));
        eventCategoriesComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        eventCategoriesComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                eventCategoriesComboBoxMouseReleased(evt);
            }
        });

        searchServiceAreaComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All areas", "Helsinki", "Espoo", "Vantaa" }));
        searchServiceAreaComboBox.setMinimumSize(new java.awt.Dimension(120, 20));
        searchServiceAreaComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        searchServiceAreaComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                searchServiceAreaComboBoxMouseReleased(evt);
            }
        });

        searchServiceCategoriesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All categories" }));
        searchServiceCategoriesComboBox.setMinimumSize(new java.awt.Dimension(120, 20));
        searchServiceCategoriesComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        searchServiceCategoriesComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                searchServiceCategoriesComboBoxMouseReleased(evt);
            }
        });

        setMinimumSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        eventPanel.setLayout(new java.awt.GridBagLayout());

        eventPanelInner.setLayout(new java.awt.GridBagLayout());

        eventInfoLabel.setText("<html>OmaKaupunki is a Helsinki city based web service providing local event information and local service directory. Web service locates at omakaupunki.hs.fi. This tab is used to extract event data from OmaKaupunki API. Select event extraction type. To search events enter a search query.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        eventPanelInner.add(eventInfoLabel, gridBagConstraints);

        eventSelectionGroup.add(allEventsRadioButton);
        allEventsRadioButton.setSelected(true);
        allEventsRadioButton.setText("Extract all events");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        eventPanelInner.add(allEventsRadioButton, gridBagConstraints);

        eventSelectionGroup.add(eventCategoriesRadioButton);
        eventCategoriesRadioButton.setText("Extract event categories");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        eventPanelInner.add(eventCategoriesRadioButton, gridBagConstraints);

        singleEventPanel.setLayout(new java.awt.GridBagLayout());

        eventSelectionGroup.add(singleEventRadioButton);
        singleEventRadioButton.setText("Extract event using identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        singleEventPanel.add(singleEventRadioButton, gridBagConstraints);

        eventIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                eventIdTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        singleEventPanel.add(eventIdTextField, gridBagConstraints);

        getEventContextButton.setText("get context");
        getEventContextButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        getEventContextButton.setMinimumSize(new java.awt.Dimension(75, 19));
        getEventContextButton.setPreferredSize(new java.awt.Dimension(75, 19));
        getEventContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getEventContextButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        singleEventPanel.add(getEventContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        eventPanelInner.add(singleEventPanel, gridBagConstraints);

        searchEventsPanel.setLayout(new java.awt.GridBagLayout());

        eventSelectionGroup.add(searchEventsRadioButton);
        searchEventsRadioButton.setText("Search for events");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        searchEventsPanel.add(searchEventsRadioButton, gridBagConstraints);

        searchEventsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchEventsTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        searchEventsPanel.add(searchEventsTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        eventPanelInner.add(searchEventsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        eventPanel.add(eventPanelInner, gridBagConstraints);

        omaKaupunkiTabbedPane.addTab("Events", eventPanel);

        directoryPanel.setLayout(new java.awt.GridBagLayout());

        directoryPanelInner.setLayout(new java.awt.GridBagLayout());

        directoryInfoLabel.setText("<html>OmaKaupunki is a Helsinki city based web service providing local event information and local service directory. Web service locates at omakaupunki.hs.fi. This tab is used to extract services from OmaKaupunki API. Select to extract all services or search for services with a given search word.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        directoryPanelInner.add(directoryInfoLabel, gridBagConstraints);

        directorySelectionGroup.add(allServicesRadioButton);
        allServicesRadioButton.setSelected(true);
        allServicesRadioButton.setText("Extract all services");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        directoryPanelInner.add(allServicesRadioButton, gridBagConstraints);

        directorySelectionGroup.add(serviceCategoriesRadioButton);
        serviceCategoriesRadioButton.setText("Extract service categories");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        directoryPanelInner.add(serviceCategoriesRadioButton, gridBagConstraints);

        singleServicePanel.setLayout(new java.awt.GridBagLayout());

        directorySelectionGroup.add(singleServiceRadioButton);
        singleServiceRadioButton.setText("Extract service using identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        singleServicePanel.add(singleServiceRadioButton, gridBagConstraints);

        singleServiceTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                singleServiceTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        singleServicePanel.add(singleServiceTextField, gridBagConstraints);

        getServiceContextButton.setText("get context");
        getServiceContextButton.setMargin(new java.awt.Insets(0, 3, 0, 3));
        getServiceContextButton.setMinimumSize(new java.awt.Dimension(75, 19));
        getServiceContextButton.setPreferredSize(new java.awt.Dimension(75, 19));
        getServiceContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getServiceContextButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        singleServicePanel.add(getServiceContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        directoryPanelInner.add(singleServicePanel, gridBagConstraints);

        searchServicesPanel.setLayout(new java.awt.GridBagLayout());

        directorySelectionGroup.add(searchServicesRadioButton);
        searchServicesRadioButton.setText("Search for services");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        searchServicesPanel.add(searchServicesRadioButton, gridBagConstraints);

        searchServicesTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchServicesTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        searchServicesPanel.add(searchServicesTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        directoryPanelInner.add(searchServicesPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        directoryPanel.add(directoryPanelInner, gridBagConstraints);

        omaKaupunkiTabbedPane.addTab("Services", directoryPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 95;
        gridBagConstraints.ipady = 95;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(omaKaupunkiTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        paginationPanel.setLayout(new java.awt.GridBagLayout());

        pageLabel.setText("page");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        paginationPanel.add(pageLabel, gridBagConstraints);

        pageTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pageTextField.setText("1");
        pageTextField.setPreferredSize(new java.awt.Dimension(50, 20));
        paginationPanel.add(pageTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(paginationPanel, gridBagConstraints);

        buttonFillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        extractButton.setText("Extract");
        extractButton.setMinimumSize(new java.awt.Dimension(75, 23));
        extractButton.setPreferredSize(new java.awt.Dimension(75, 23));
        extractButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extractButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(extractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMinimumSize(new java.awt.Dimension(75, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    wasAccepted = false;
    if(myDialog != null) myDialog.setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void extractButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extractButtonMouseReleased
    wasAccepted = true;
    if(myDialog != null) myDialog.setVisible(false);
}//GEN-LAST:event_extractButtonMouseReleased

private void getServiceContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getServiceContextButtonMouseReleased
    solveServiceContext();
    this.singleServiceRadioButton.setSelected(true);
}//GEN-LAST:event_getServiceContextButtonMouseReleased

    private void searchEventsTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchEventsTextFieldFocusGained
        this.searchEventsRadioButton.setSelected(true);
    }//GEN-LAST:event_searchEventsTextFieldFocusGained

    private void eventIdTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_eventIdTextFieldFocusGained
        this.singleEventRadioButton.setSelected(true);
    }//GEN-LAST:event_eventIdTextFieldFocusGained

    private void getEventContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getEventContextButtonMouseReleased
        solveEventContext();
        this.singleEventRadioButton.setSelected(true);
    }//GEN-LAST:event_getEventContextButtonMouseReleased

    private void eventAreaComboBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eventAreaComboBoxMouseReleased
        this.searchEventsRadioButton.setSelected(true);
    }//GEN-LAST:event_eventAreaComboBoxMouseReleased

    private void eventCategoriesComboBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eventCategoriesComboBoxMouseReleased
        this.searchEventsRadioButton.setSelected(true);
    }//GEN-LAST:event_eventCategoriesComboBoxMouseReleased

    private void searchServicesTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchServicesTextFieldFocusGained
        this.searchServicesRadioButton.setSelected(true);
    }//GEN-LAST:event_searchServicesTextFieldFocusGained

    private void searchServiceAreaComboBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchServiceAreaComboBoxMouseReleased
        this.searchServicesRadioButton.setSelected(true);
    }//GEN-LAST:event_searchServiceAreaComboBoxMouseReleased

    private void searchServiceCategoriesComboBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchServiceCategoriesComboBoxMouseReleased
        this.searchServicesRadioButton.setSelected(true);
    }//GEN-LAST:event_searchServiceCategoriesComboBoxMouseReleased

    private void singleServiceTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_singleServiceTextFieldFocusGained
        this.singleServiceRadioButton.setSelected(true);
    }//GEN-LAST:event_singleServiceTextFieldFocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allEventsRadioButton;
    private javax.swing.JRadioButton allServicesRadioButton;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel directoryInfoLabel;
    private javax.swing.JPanel directoryPanel;
    private javax.swing.JPanel directoryPanelInner;
    private javax.swing.ButtonGroup directorySelectionGroup;
    private javax.swing.JComboBox eventAreaComboBox;
    private javax.swing.JComboBox eventCategoriesComboBox;
    private javax.swing.JRadioButton eventCategoriesRadioButton;
    private javax.swing.JTextField eventIdTextField;
    private javax.swing.JLabel eventInfoLabel;
    private javax.swing.JPanel eventPanel;
    private javax.swing.JPanel eventPanelInner;
    private javax.swing.ButtonGroup eventSelectionGroup;
    private javax.swing.JButton extractButton;
    private javax.swing.JButton getEventContextButton;
    private javax.swing.JButton getServiceContextButton;
    private javax.swing.JTabbedPane omaKaupunkiTabbedPane;
    private javax.swing.JLabel pageLabel;
    private javax.swing.JTextField pageTextField;
    private javax.swing.JPanel paginationPanel;
    private javax.swing.JPanel searchEventsPanel;
    private javax.swing.JRadioButton searchEventsRadioButton;
    private javax.swing.JTextField searchEventsTextField;
    private javax.swing.JComboBox searchServiceAreaComboBox;
    private javax.swing.JComboBox searchServiceCategoriesComboBox;
    private javax.swing.JPanel searchServicesPanel;
    private javax.swing.JRadioButton searchServicesRadioButton;
    private javax.swing.JTextField searchServicesTextField;
    private javax.swing.JRadioButton serviceCategoriesRadioButton;
    private javax.swing.JPanel singleEventPanel;
    private javax.swing.JRadioButton singleEventRadioButton;
    private javax.swing.JPanel singleServicePanel;
    private javax.swing.JRadioButton singleServiceRadioButton;
    private javax.swing.JTextField singleServiceTextField;
    // End of variables declaration//GEN-END:variables




    
    // -------------------------------------------------------------------------
    


    private static String apikey = null; // "698bdc32-ee85-11e0-a468-000c29f7271d";
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Please give an Oma kaupunki API key.", apikey, "Oma kaupunki API key", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }



    
    public void forgetAuthorization() {
        apikey = null;
    }


}
