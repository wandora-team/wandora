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
 *
 * SparqlExtractorUI.java
 *
 * Created on 26.5.2011, 14:13:37
 */





package org.wandora.application.tools.extractors.sparql;

import java.awt.Component;
import java.awt.Desktop;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JDialog;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.utils.swing.TextLineNumber;

/**
 *
 * @author akivela
 */
public class SparqlExtractorUI extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;

	public static final String WIKIDATA_ENDPOINT = "https://query.wikidata.org/sparql";
    public static final String EUROPEAN_OPEN_DATA_ENDPOINT = "http://open-data.europa.eu/sparqlep";
    public static final String DBPEDIA_ENDPOINT = "http://dbpedia.org/sparql";
    public static final String DATAGOVUK_ENDPOINT = "http://services.data.gov.uk/reference/sparql";
    public static final String EUROPEANA_ENDPOINT = "http://europeana.ontotext.com/sparql.xml";
    
    
    
    public String HRI_NAMESPACES = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"+
                "PREFIX dataset: <http://semantic.hri.fi/data/datasets#>\n"+
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
                "PREFIX stat: <http://data.mysema.com/schemas/stat#>\n"+
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                "PREFIX dimension: <http://semantic.hri.fi/data/dimensions/>\n"+
                "PREFIX geo: <http://www.w3.org/2003/01/geo/>\n"+
                "PREFIX scv: <http://purl.org/NET/scovo#>\n"+
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n"+
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
                "PREFIX meta: <http://data.mysema.com/schemas/meta#>\n"+
                "PREFIX rahoitusmuoto: <http://semantic.hri.fi/data/dimensions/Rahoitusmuoto#>\n"+
                "PREFIX talotyyppi: <http://semantic.hri.fi/data/dimensions/Talotyyppi#>\n"+
                "PREFIX alue: <http://semantic.hri.fi/data/dimensions/Alue#>\n"+
                "PREFIX hallintaperuste: <http://semantic.hri.fi/data/dimensions/Hallintaperuste#>\n"+
                "PREFIX vuosi: <http://semantic.hri.fi/data/dimensions/Vuosi#>\n"+
                "PREFIX huoneistotyyppi: <http://semantic.hri.fi/data/dimensions/Huoneistotyyppi#>\n"+
                "PREFIX yksikk�: <http://semantic.hri.fi/data/dimensions/Yksikk�#>\n"+
                "PREFIX henkil�luku: <http://semantic.hri.fi/data/dimensions/Henkil�luku#>\n"+
                "PREFIX ik�ryhm�: <http://semantic.hri.fi/data/dimensions/Ik�ryhm�#>\n"+
                "PREFIX �idinkieli: <http://semantic.hri.fi/data/dimensions/�idinkieli#>\n"+
                "PREFIX sukupuoli: <http://semantic.hri.fi/data/dimensions/Sukupuoli#>\n"+
                "PREFIX ik�: <http://semantic.hri.fi/data/dimensions/Ik�#>\n"+
                "PREFIX lasten_m��r�_perheess�_140-17v_15: <http://semantic.hri.fi/data/dimensions/Lasten_m��r�_perheess�_140-17v_15#>\n"+
                "PREFIX perhetyyppi: <http://semantic.hri.fi/data/dimensions/Perhetyyppi#>\n"+
                "PREFIX v�est�n_m��r�: <http://semantic.hri.fi/data/dimensions/V�est�n_m��r�#>\n"+
                "PREFIX lasten_m��r�_perheess�: <http://semantic.hri.fi/data/dimensions/Lasten_m��r�_perheess�#>\n"+
                "PREFIX muuttosuunta: <http://semantic.hri.fi/data/dimensions/Muuttosuunta#>\n"+
                "PREFIX koulutusaste: <http://semantic.hri.fi/data/dimensions/Koulutusaste#>\n"+
                "PREFIX toimiala: <http://semantic.hri.fi/data/dimensions/Toimiala#>\n"+
                "PREFIX toimiala-1995: <http://semantic.hri.fi/data/dimensions/Toimiala-1995#>\n"+
                "PREFIX ty�tt�myys: <http://semantic.hri.fi/data/dimensions/Ty�tt�myys#>\n"+
                "PREFIX valmistumisvuosi: <http://semantic.hri.fi/data/dimensions/Valmistumisvuosi#>\n"+
                "PREFIX k�yt�ss�olotilanne: <http://semantic.hri.fi/data/dimensions/K�yt�ss�olotilanne#>\n"+
                "PREFIX k�ytt�tarkoitus_ja_kerrosluku: <http://semantic.hri.fi/data/dimensions/K�ytt�tarkoitus_ja_kerrosluku#>\n"+
                "PREFIX tuloluokka: <http://semantic.hri.fi/data/dimensions/Tuloluokka#>\n"+
                "PREFIX k�ytt�tarkoitus: <http://semantic.hri.fi/data/dimensions/K�ytt�tarkoitus#>\n"+
                "PREFIX toimenpide: <http://semantic.hri.fi/data/dimensions/Toimenpide#>\n";


    
    private static final String QUERY_MACRO = "__QUERY__";

    private boolean accepted = false;
    private JDialog dialog = null;
    private Wandora wandora = null;



    /** Creates new form SparqlExtractorUI */
    public SparqlExtractorUI(Wandora w) {
        wandora = w;
        initComponents();

        resultFormatComboBox.setEditable(false);
        genericEncodingComboBox.setEditable(false);
        
        // Add line number column to all query panels.
        
        TextLineNumber wikidataLineNumbers = new TextLineNumber(wikidataQueryTextPane);
        wikidataQueryScrollPane.setRowHeaderView( wikidataLineNumbers );
        
        TextLineNumber datagovukLineNumbers = new TextLineNumber(datagovukQueryTextPane);
        datagovukQueryScrollPane.setRowHeaderView( datagovukLineNumbers );
        
        TextLineNumber europeanOpenDataLineNumbers = new TextLineNumber(europeanOpenDataQueryTextPane);
        europeanOpenDataQueryScrollPane.setRowHeaderView( europeanOpenDataLineNumbers );
        
        TextLineNumber dbpediaLineNumbers = new TextLineNumber(dbpediaQueryTextPane);
        dbpediaQueryScrollPane.setRowHeaderView( dbpediaLineNumbers );
        
        TextLineNumber genericLineNumbers = new TextLineNumber(genericQueryTextPane);
        genericQueryScrollPane.setRowHeaderView( genericLineNumbers );
    }



    public boolean wasAccepted() {
        return accepted;
    }
    public void setAccepted(boolean b) {
        accepted = b;
    }

    public void open(Wandora w) {
        accepted = false;
        dialog = new JDialog(w, true);
        dialog.setSize(800,500);
        dialog.add(this);
        dialog.setTitle("SPARQL extractor");
        UIBox.centerWindow(dialog, w);
        dialog.setVisible(true);
    }


    
    
    public String[] getQueryURLs(WandoraTool parentTool) {
        return getQueryURLs();
    }




    public String[] getQueryURLs() {
        Component component = tabbedPane.getSelectedComponent();
        String[] q = null;

        // ***** GENERIC *****
        if(genericPanel.equals(component)) {
            String query = prepareQuery(genericQueryTextPane.getText());
            String queryURL = genericURLTextField.getText();
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** DBPEDIA *****
        else if(dbpediaPanel.equals(component)) {
            String query = prepareQuery(dbpediaQueryTextPane.getText(), "UTF-8");
            String queryURL = DBPEDIA_ENDPOINT+"?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=__QUERY__&debug=on&timeout=&format=application%2Fsparql-results%2Bjson&save=display&fname=";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** HRI ***** / DEPRECATED
        else if(hriPanel.equals(component)) {
            String query = hriQueryTextPane.getText();
            query = HRI_NAMESPACES + query;
            query = prepareQuery(query, "UTF-8");
            String queryURL = "http://semantic.hri.fi/sparql?query=__QUERY__";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** DATA.GOV.UK *****
        else if(datagovukPanel.equals(component)) {
            String query = datagovukQueryTextPane.getText();
            query = prepareQuery(query, "UTF-8");
            String queryURL = DATAGOVUK_ENDPOINT+"?query=__QUERY__";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** DATA.GOV ***** / DEPRECATED
        else if(datagovPanel.equals(component)) {
            String query = datagovQueryTextPane.getText();
            query = prepareQuery(query, "UTF-8");
            String queryURL = "http://services.data.gov/sparql?query=__QUERY__&format=application%2Fxml";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** EUROPEANA *****
        else if(europeanaPanel.equals(component)) {
            String query = europeanaQueryTextPane.getText();
            query = prepareQuery(query, "UTF-8");
            String queryURL = EUROPEANA_ENDPOINT+"?query=__QUERY__";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** EUROPEAN OPEN DATA *****
        else if(europeanOpenDataPanel.equals(component)) {
            String query = europeanOpenDataQueryTextPane.getText();
            query = prepareQuery(query, "UTF-8");
            String queryURL = EUROPEAN_OPEN_DATA_ENDPOINT+"?query=__QUERY__&format=application%2Fxml";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** WIKIDATA *****
        else if(wikidataPanel.equals(component)) {
            String query = wikidataQueryTextPane.getText();
            query = prepareQuery(query, "UTF-8");
            String queryURL = WIKIDATA_ENDPOINT+"?query=__QUERY__&format=json";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        
        return q;
    }



    public String getResultSetFormat() {
        Component component = tabbedPane.getSelectedComponent();
        if(genericPanel.equals(component)) {
            return resultFormatComboBox.getSelectedItem().toString();
        }
        else if(dbpediaPanel.equals(component)) {
            return "JSON";
        }
        else if(hriPanel.equals(component)) {
            return "XML";
        }
        else if(datagovukPanel.equals(component)) {
            return "XML";
        }
        else if(datagovPanel.equals(component)) {
            return "XML";
        }
        else if(europeanaPanel.equals(component)) {
            return "XML";
        }
        else if(europeanOpenDataPanel.equals(component)) {
            return "XML";
        }
        return "JSON";
    }

    
    
    public String getHandleMethod() {
        return "RESULTSET-TOPICMAP";
    }

    

    public String prepareQuery(String query) {
        String encoding = genericEncodingComboBox.getSelectedItem().toString();
        return prepareQuery(query, encoding);
    }


    public String prepareQuery(String query, String encoding) {
        try {
            //query = query.replace('\n', ' ');
            //query = query.replace('\r', ' ');
            //query = query.replace('\t', ' ');
            if(!"no encoding".equalsIgnoreCase(encoding)) {
                query = URLEncoder.encode(query, encoding);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return query;
    }



    public void checkSPARQLQuery(String query) {
        try {
            Query q = QueryFactory.create(query);
            WandoraOptionPane.showMessageDialog(wandora, "SPARQL query successfully checked. Syntax OK.", "Syntax OK");
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(wandora, "Syntax Error: "+e.getMessage(), "Syntax Error");
            //wandora.handleError(e);
        }
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

        disabledPanels = new javax.swing.JPanel();
        hriPanel = new javax.swing.JPanel();
        hriInnerPanel = new javax.swing.JPanel();
        hriTitle = new SimpleLabel();
        hriQueryScrollPane = new SimpleScrollPane();
        hriQueryTextPane = new SimpleTextPane();
        hriButtonPanel = new javax.swing.JPanel();
        hriCheckButton = new SimpleButton();
        datagovPanel = new javax.swing.JPanel();
        datagovInnerPanel = new javax.swing.JPanel();
        datagovTitle = new SimpleLabel();
        datagovQueryScrollPane = new SimpleScrollPane();
        datagovQueryTextPane = new SimpleTextPane();
        datagovButtonPanel = new javax.swing.JPanel();
        datagovCheckButton = new SimpleButton();
        europeanaPanel = new javax.swing.JPanel();
        europeanaInnerPanel = new javax.swing.JPanel();
        europeanaTitle = new SimpleLabel();
        europeanaQueryScrollPane = new SimpleScrollPane();
        europeanaQueryTextPane = new SimpleTextPane();
        europeanaButtonPanel = new javax.swing.JPanel();
        europeanaCheckButton = new SimpleButton();
        tabbedPane = new SimpleTabbedPane();
        genericPanel = new javax.swing.JPanel();
        genericPanelInner = new javax.swing.JPanel();
        genericTitle = new SimpleLabel();
        genericURLLabel = new SimpleLabel();
        urlPanel = new javax.swing.JPanel();
        genericURLTextField = new SimpleField();
        genericQueryLabel = new SimpleLabel();
        genericQueryScrollPane = new SimpleScrollPane();
        genericQueryTextPane = new SimpleTextPane();
        genericButtonPanel = new javax.swing.JPanel();
        encodingLabel = new SimpleLabel();
        genericEncodingComboBox = new SimpleComboBox();
        resultFormatLabel = new SimpleLabel();
        resultFormatComboBox = new SimpleComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        showGenericButton = new SimpleButton();
        checkGenericButton = new SimpleButton();
        wikidataPanel = new javax.swing.JPanel();
        wikidataInnerPanel = new javax.swing.JPanel();
        wikidataTitle = new SimpleLabel();
        wikidataQueryScrollPane = new SimpleScrollPane();
        wikidataQueryTextPane = new SimpleTextPane();
        wikidataButtonPanel = new javax.swing.JPanel();
        wikidataCheckButton = new SimpleButton();
        wikidataOpenEndPoint = new SimpleButton();
        wikidataOpenQueryButton = new SimpleButton();
        dbpediaPanel = new javax.swing.JPanel();
        dbpediaInnerPanel = new javax.swing.JPanel();
        dbpediaTitle = new SimpleLabel();
        dbpediaQueryScrollPane = new SimpleScrollPane();
        dbpediaQueryTextPane = new SimpleTextPane();
        dbpediaButtonPanel = new javax.swing.JPanel();
        dbpediaCheckButton = new SimpleButton();
        dbpediaOpenEndPointButton = new SimpleButton();
        dbpediaOpenQueryButton = new SimpleButton();
        europeanOpenDataPanel = new javax.swing.JPanel();
        europeanOpenDataInnerPanel = new javax.swing.JPanel();
        europeanOpenDataTitle = new SimpleLabel();
        europeanOpenDataQueryScrollPane = new SimpleScrollPane();
        europeanOpenDataQueryTextPane = new SimpleTextPane();
        europeanOpenDataButtonPanel = new javax.swing.JPanel();
        europeanOpenDataCheckButton = new SimpleButton();
        europeanOpenDataOpenEndPointButton = new SimpleButton();
        europeanOpenDataOpenQueryButton = new SimpleButton();
        datagovukPanel = new javax.swing.JPanel();
        datagovukInnerPanel = new javax.swing.JPanel();
        datagovukTitle = new SimpleLabel();
        datagovukQueryScrollPane = new SimpleScrollPane();
        datagovukQueryTextPane = new SimpleTextPane();
        datagovukButtonPanel = new javax.swing.JPanel();
        datagovukCheckButton = new SimpleButton();
        datagovukOpenEndPointButton = new SimpleButton();
        datagovukOpenQueryButton = new SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        hriPanel.setLayout(new java.awt.GridBagLayout());

        hriInnerPanel.setLayout(new java.awt.GridBagLayout());

        hriTitle.setText("<html>Write and send your SPARQL query to Helsinki Region Inforshare (HRI) SPARQL end point at http://semantic.hri.fi/sparql and transform result set to a topic map.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        hriInnerPanel.add(hriTitle, gridBagConstraints);

        hriQueryTextPane.setText("SELECT ?area ?pred ?obj\nWHERE { \n  ?area rdf:type dimension:Alue;\n     ?pred ?obj.\n}");
        hriQueryScrollPane.setViewportView(hriQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        hriInnerPanel.add(hriQueryScrollPane, gridBagConstraints);

        hriButtonPanel.setLayout(new java.awt.GridBagLayout());

        hriCheckButton.setText("Check Query");
        hriCheckButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        hriCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                hriCheckButtonMouseReleased(evt);
            }
        });
        hriButtonPanel.add(hriCheckButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        hriInnerPanel.add(hriButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        hriPanel.add(hriInnerPanel, gridBagConstraints);

        datagovPanel.setLayout(new java.awt.GridBagLayout());

        datagovInnerPanel.setLayout(new java.awt.GridBagLayout());

        datagovTitle.setText("Write and send SPARQL query to data.gov's SPARQL end point and transform result set to a topic map.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        datagovInnerPanel.add(datagovTitle, gridBagConstraints);

        datagovQueryTextPane.setText("select distinct ?Concept where {[] a ?Concept}");
        datagovQueryScrollPane.setViewportView(datagovQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        datagovInnerPanel.add(datagovQueryScrollPane, gridBagConstraints);

        datagovButtonPanel.setLayout(new java.awt.GridBagLayout());

        datagovCheckButton.setText("Check Query");
        datagovCheckButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        datagovCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                datagovCheckButtonMouseReleased(evt);
            }
        });
        datagovButtonPanel.add(datagovCheckButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        datagovInnerPanel.add(datagovButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        datagovPanel.add(datagovInnerPanel, gridBagConstraints);

        europeanaPanel.setLayout(new java.awt.GridBagLayout());

        europeanaInnerPanel.setLayout(new java.awt.GridBagLayout());

        europeanaTitle.setText("<html>Write and send query to Europeana's SPARQL end point at http://europeana.ontotext.com/sparql and transform result set to a topic map.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        europeanaInnerPanel.add(europeanaTitle, gridBagConstraints);

        europeanaQueryTextPane.setText("# Creators and collections of Europeana objects from Italy\n\nPREFIX dc: <http://purl.org/dc/elements/1.1/>\nPREFIX edm: <http://www.europeana.eu/schemas/edm/>\n\nSELECT DISTINCT ?EuropeanaObject ?Creator ?Collection\nWHERE {\n?EuropeanaObject dc:creator ?Creator ;\n                 edm:collectionName ?Collection ;\n                 edm:country \"italy\"\n}\nLIMIT 100");
        europeanaQueryScrollPane.setViewportView(europeanaQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        europeanaInnerPanel.add(europeanaQueryScrollPane, gridBagConstraints);

        europeanaButtonPanel.setLayout(new java.awt.GridBagLayout());

        europeanaCheckButton.setText("Check Query");
        europeanaCheckButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        europeanaCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                europeanaCheckButtonMouseReleased(evt);
            }
        });
        europeanaButtonPanel.add(europeanaCheckButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        europeanaInnerPanel.add(europeanaButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        europeanaPanel.add(europeanaInnerPanel, gridBagConstraints);

        javax.swing.GroupLayout disabledPanelsLayout = new javax.swing.GroupLayout(disabledPanels);
        disabledPanels.setLayout(disabledPanelsLayout);
        disabledPanelsLayout.setHorizontalGroup(
            disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(disabledPanelsLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(hriPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(disabledPanelsLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(datagovPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(disabledPanelsLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(europeanaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        disabledPanelsLayout.setVerticalGroup(
            disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(disabledPanelsLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(hriPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(disabledPanelsLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(datagovPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(disabledPanelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(disabledPanelsLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(europeanaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        setLayout(new java.awt.GridBagLayout());

        genericPanel.setLayout(new java.awt.GridBagLayout());

        genericPanelInner.setLayout(new java.awt.GridBagLayout());

        genericTitle.setText("<html>This tab is used to send SPARQL queries to a given endpoint and transform the result set to a topic map. Fill in the endpoint URL and the query. Use special keyword '__QUERY__' to mark the query location in the URL.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        genericPanelInner.add(genericTitle, gridBagConstraints);

        genericURLLabel.setText("URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        genericPanelInner.add(genericURLLabel, gridBagConstraints);

        urlPanel.setLayout(new java.awt.BorderLayout(4, 0));

        genericURLTextField.setText("http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=__QUERY__&debug=on&timeout=&format=application%2Fsparql-results%2Bjson&save=display&fname=");
        urlPanel.add(genericURLTextField, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        genericPanelInner.add(urlPanel, gridBagConstraints);

        genericQueryLabel.setText("Query");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 0);
        genericPanelInner.add(genericQueryLabel, gridBagConstraints);

        genericQueryTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        genericQueryTextPane.setText("SELECT DISTINCT ?Concept WHERE {[] a ?Concept}");
        genericQueryScrollPane.setViewportView(genericQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        genericPanelInner.add(genericQueryScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        genericPanel.add(genericPanelInner, gridBagConstraints);

        genericButtonPanel.setLayout(new java.awt.GridBagLayout());

        encodingLabel.setText("Encoding");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 2);
        genericButtonPanel.add(encodingLabel, gridBagConstraints);

        genericEncodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UTF-8", "ISO-8859-1", "No encoding" }));
        genericEncodingComboBox.setPreferredSize(new java.awt.Dimension(80, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        genericButtonPanel.add(genericEncodingComboBox, gridBagConstraints);

        resultFormatLabel.setText("Result format");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        genericButtonPanel.add(resultFormatLabel, gridBagConstraints);

        resultFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "JSON", "XML", "RDF/XML" }));
        resultFormatComboBox.setPreferredSize(new java.awt.Dimension(80, 23));
        genericButtonPanel.add(resultFormatComboBox, new java.awt.GridBagConstraints());

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        genericButtonPanel.add(jSeparator1, gridBagConstraints);

        showGenericButton.setText("Open query URL");
        showGenericButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        showGenericButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showGenericButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        genericButtonPanel.add(showGenericButton, gridBagConstraints);

        checkGenericButton.setText("Check query");
        checkGenericButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        checkGenericButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkGenericButtonMouseReleased(evt);
            }
        });
        genericButtonPanel.add(checkGenericButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        genericPanel.add(genericButtonPanel, gridBagConstraints);

        tabbedPane.addTab("Generic", genericPanel);

        wikidataPanel.setLayout(new java.awt.GridBagLayout());

        wikidataInnerPanel.setLayout(new java.awt.GridBagLayout());

        wikidataTitle.setText("<html>Query the Wikidata's SPARQL endpoint at https://query.wikidata.org/sparql.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        wikidataInnerPanel.add(wikidataTitle, gridBagConstraints);

        wikidataQueryTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        wikidataQueryTextPane.setText("PREFIX wikibase: <http://wikiba.se/ontology#>\nPREFIX wd: <http://www.wikidata.org/entity/> \nPREFIX wdt: <http://www.wikidata.org/prop/direct/>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX p: <http://www.wikidata.org/prop/>\nPREFIX v: <http://www.wikidata.org/prop/statement/>\n\nSELECT ?p ?pLabel ?w ?wLabel WHERE {\n   wd:Q30 p:P6/v:P6 ?p .\n   ?p wdt:P26 ?w .\n   SERVICE wikibase:label {\n    bd:serviceParam wikibase:language \"en\" .\n   }\n }");
        wikidataQueryScrollPane.setViewportView(wikidataQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        wikidataInnerPanel.add(wikidataQueryScrollPane, gridBagConstraints);

        wikidataButtonPanel.setLayout(new java.awt.GridBagLayout());

        wikidataCheckButton.setText("Check query");
        wikidataCheckButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        wikidataCheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikidataCheckButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        wikidataButtonPanel.add(wikidataCheckButton, gridBagConstraints);

        wikidataOpenEndPoint.setText("Open endpoint URL");
        wikidataOpenEndPoint.setMargin(new java.awt.Insets(2, 8, 2, 8));
        wikidataOpenEndPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikidataOpenEndPointActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        wikidataButtonPanel.add(wikidataOpenEndPoint, gridBagConstraints);

        wikidataOpenQueryButton.setText("Open query URL");
        wikidataOpenQueryButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        wikidataOpenQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openQueryUrl(evt);
            }
        });
        wikidataButtonPanel.add(wikidataOpenQueryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        wikidataInnerPanel.add(wikidataButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        wikidataPanel.add(wikidataInnerPanel, gridBagConstraints);

        tabbedPane.addTab("Wikidata", wikidataPanel);

        dbpediaPanel.setLayout(new java.awt.GridBagLayout());

        dbpediaInnerPanel.setLayout(new java.awt.GridBagLayout());

        dbpediaTitle.setText("<html>Query the SPARQL endpoit of DBPedia at http://dbpedia.org/sparql.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        dbpediaInnerPanel.add(dbpediaTitle, gridBagConstraints);

        dbpediaQueryTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        dbpediaQueryTextPane.setText("SELECT DISTINCT ?Concept WHERE {[] a ?Concept}");
        dbpediaQueryScrollPane.setViewportView(dbpediaQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        dbpediaInnerPanel.add(dbpediaQueryScrollPane, gridBagConstraints);

        dbpediaButtonPanel.setLayout(new java.awt.GridBagLayout());

        dbpediaCheckButton.setText("Check query");
        dbpediaCheckButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        dbpediaCheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbpediaCheckButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        dbpediaButtonPanel.add(dbpediaCheckButton, gridBagConstraints);

        dbpediaOpenEndPointButton.setText("Open endpoint URL");
        dbpediaOpenEndPointButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        dbpediaOpenEndPointButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbpediaOpenEndPointButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        dbpediaButtonPanel.add(dbpediaOpenEndPointButton, gridBagConstraints);

        dbpediaOpenQueryButton.setText("Open query URL");
        dbpediaOpenQueryButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        dbpediaOpenQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openQueryUrl(evt);
            }
        });
        dbpediaButtonPanel.add(dbpediaOpenQueryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        dbpediaInnerPanel.add(dbpediaButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        dbpediaPanel.add(dbpediaInnerPanel, gridBagConstraints);

        tabbedPane.addTab("DBPedia", dbpediaPanel);

        europeanOpenDataPanel.setLayout(new java.awt.GridBagLayout());

        europeanOpenDataInnerPanel.setLayout(new java.awt.GridBagLayout());

        europeanOpenDataTitle.setText("<html>Query the European Union Open Data Portal SPARQL endpoint at http://open-data.europa.eu/sparqlep.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        europeanOpenDataInnerPanel.add(europeanOpenDataTitle, gridBagConstraints);

        europeanOpenDataQueryTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        europeanOpenDataQueryTextPane.setText("PREFIX dcat: <http://www.w3.org/ns/dcat#>\nPREFIX odp:  <http://open-data.europa.eu/ontologies/ec-odp#>\nPREFIX dc: <http://purl.org/dc/terms/>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\n\nSELECT DISTINCT ?g ?o WHERE { graph ?g {?s dc:title ?o. filter regex(?o, 'Statistics', 'i') } } LIMIT 10");
        europeanOpenDataQueryScrollPane.setViewportView(europeanOpenDataQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        europeanOpenDataInnerPanel.add(europeanOpenDataQueryScrollPane, gridBagConstraints);

        europeanOpenDataButtonPanel.setLayout(new java.awt.GridBagLayout());

        europeanOpenDataCheckButton.setText("Check query");
        europeanOpenDataCheckButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        europeanOpenDataCheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                europeanOpenDataCheckButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        europeanOpenDataButtonPanel.add(europeanOpenDataCheckButton, gridBagConstraints);

        europeanOpenDataOpenEndPointButton.setText("Open endpoint URL");
        europeanOpenDataOpenEndPointButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        europeanOpenDataOpenEndPointButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                europeanOpenDataOpenEndPointButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        europeanOpenDataButtonPanel.add(europeanOpenDataOpenEndPointButton, gridBagConstraints);

        europeanOpenDataOpenQueryButton.setText("Open query URL");
        europeanOpenDataOpenQueryButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        europeanOpenDataOpenQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openQueryUrl(evt);
            }
        });
        europeanOpenDataButtonPanel.add(europeanOpenDataOpenQueryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        europeanOpenDataInnerPanel.add(europeanOpenDataButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        europeanOpenDataPanel.add(europeanOpenDataInnerPanel, gridBagConstraints);

        tabbedPane.addTab("open-data.europa.eu", europeanOpenDataPanel);

        datagovukPanel.setLayout(new java.awt.GridBagLayout());

        datagovukInnerPanel.setLayout(new java.awt.GridBagLayout());

        datagovukTitle.setText("Query the SPARQL endpoint of data.gov.uk.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        datagovukInnerPanel.add(datagovukTitle, gridBagConstraints);

        datagovukQueryTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        datagovukQueryTextPane.setText("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n\nSELECT DISTINCT ?type\n    WHERE {\n      ?thing a ?type\n    }    LIMIT 10");
        datagovukQueryScrollPane.setViewportView(datagovukQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        datagovukInnerPanel.add(datagovukQueryScrollPane, gridBagConstraints);

        datagovukButtonPanel.setLayout(new java.awt.GridBagLayout());

        datagovukCheckButton.setText("Check query");
        datagovukCheckButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        datagovukCheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datagovukCheckButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        datagovukButtonPanel.add(datagovukCheckButton, gridBagConstraints);

        datagovukOpenEndPointButton.setText("Open endpoint URL");
        datagovukOpenEndPointButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        datagovukOpenEndPointButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datagovukOpenEndPointButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        datagovukButtonPanel.add(datagovukOpenEndPointButton, gridBagConstraints);

        datagovukOpenQueryButton.setText("Open query URL");
        datagovukOpenQueryButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        datagovukOpenQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openQueryUrl(evt);
            }
        });
        datagovukButtonPanel.add(datagovukOpenQueryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        datagovukInnerPanel.add(datagovukButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        datagovukPanel.add(datagovukInnerPanel, gridBagConstraints);

        tabbedPane.addTab("data.gov.uk", datagovukPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonFillerPanel.setPreferredSize(new java.awt.Dimension(415, 20));

        javax.swing.GroupLayout buttonFillerPanelLayout = new javax.swing.GroupLayout(buttonFillerPanel);
        buttonFillerPanel.setLayout(buttonFillerPanelLayout);
        buttonFillerPanelLayout.setHorizontalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 430, Short.MAX_VALUE)
        );
        buttonFillerPanelLayout.setVerticalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        okButton.setText("Extract");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Close");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    private void checkGenericButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkGenericButtonMouseReleased
        checkSPARQLQuery(genericQueryTextPane.getText());
    }//GEN-LAST:event_checkGenericButtonMouseReleased

    private void showGenericButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showGenericButtonMouseReleased
        try {
            String query = prepareQuery(genericQueryTextPane.getText());
            String queryURL = genericURLTextField.getText();
            queryURL = queryURL.replace(QUERY_MACRO, query);
            Desktop.getDesktop().browse(new URL(queryURL).toURI());
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(wandora, "Error: "+e.getMessage(), "Error occurred");
        }
    }//GEN-LAST:event_showGenericButtonMouseReleased



    private void hriCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hriCheckButtonMouseReleased
        checkSPARQLQuery(hriQueryTextPane.getText());
    }//GEN-LAST:event_hriCheckButtonMouseReleased

    private void datagovCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datagovCheckButtonMouseReleased
        checkSPARQLQuery(datagovQueryTextPane.getText());
    }//GEN-LAST:event_datagovCheckButtonMouseReleased

    private void europeanaCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_europeanaCheckButtonMouseReleased
        checkSPARQLQuery(europeanaQueryTextPane.getText());
    }//GEN-LAST:event_europeanaCheckButtonMouseReleased

    private void datagovukCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datagovukCheckButtonActionPerformed
        checkSPARQLQuery(datagovukQueryTextPane.getText());
    }//GEN-LAST:event_datagovukCheckButtonActionPerformed

    private void datagovukOpenEndPointButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datagovukOpenEndPointButtonActionPerformed
        openUrl(DATAGOVUK_ENDPOINT);
    }//GEN-LAST:event_datagovukOpenEndPointButtonActionPerformed

    private void europeanOpenDataCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_europeanOpenDataCheckButtonActionPerformed
        checkSPARQLQuery(europeanOpenDataQueryTextPane.getText());
    }//GEN-LAST:event_europeanOpenDataCheckButtonActionPerformed

    private void europeanOpenDataOpenEndPointButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_europeanOpenDataOpenEndPointButtonActionPerformed
        openUrl(EUROPEAN_OPEN_DATA_ENDPOINT);
    }//GEN-LAST:event_europeanOpenDataOpenEndPointButtonActionPerformed

    private void dbpediaCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbpediaCheckButtonActionPerformed
        checkSPARQLQuery(dbpediaQueryTextPane.getText());
    }//GEN-LAST:event_dbpediaCheckButtonActionPerformed

    private void dbpediaOpenEndPointButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbpediaOpenEndPointButtonActionPerformed
        openUrl(DBPEDIA_ENDPOINT);
    }//GEN-LAST:event_dbpediaOpenEndPointButtonActionPerformed

    private void wikidataCheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikidataCheckButtonActionPerformed
        checkSPARQLQuery(wikidataQueryTextPane.getText());
    }//GEN-LAST:event_wikidataCheckButtonActionPerformed

    private void wikidataOpenEndPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikidataOpenEndPointActionPerformed
        openUrl(WIKIDATA_ENDPOINT);
    }//GEN-LAST:event_wikidataOpenEndPointActionPerformed

    private void openQueryUrl(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openQueryUrl
        String[] queryUrls = getQueryURLs();
        if(queryUrls != null) {
            for(String url : queryUrls) {
                if(url != null) {
                    openUrl(url);
                }
            }
        }
    }//GEN-LAST:event_openQueryUrl

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        accepted = false;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        accepted = true;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    
    
    
    private void openUrl(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URL(url).toURI());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
            
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton checkGenericButton;
    private javax.swing.JPanel datagovButtonPanel;
    private javax.swing.JButton datagovCheckButton;
    private javax.swing.JPanel datagovInnerPanel;
    private javax.swing.JPanel datagovPanel;
    private javax.swing.JScrollPane datagovQueryScrollPane;
    private javax.swing.JTextPane datagovQueryTextPane;
    private javax.swing.JLabel datagovTitle;
    private javax.swing.JPanel datagovukButtonPanel;
    private javax.swing.JButton datagovukCheckButton;
    private javax.swing.JPanel datagovukInnerPanel;
    private javax.swing.JButton datagovukOpenEndPointButton;
    private javax.swing.JButton datagovukOpenQueryButton;
    private javax.swing.JPanel datagovukPanel;
    private javax.swing.JScrollPane datagovukQueryScrollPane;
    private javax.swing.JTextPane datagovukQueryTextPane;
    private javax.swing.JLabel datagovukTitle;
    private javax.swing.JPanel dbpediaButtonPanel;
    private javax.swing.JButton dbpediaCheckButton;
    private javax.swing.JPanel dbpediaInnerPanel;
    private javax.swing.JButton dbpediaOpenEndPointButton;
    private javax.swing.JButton dbpediaOpenQueryButton;
    private javax.swing.JPanel dbpediaPanel;
    private javax.swing.JScrollPane dbpediaQueryScrollPane;
    private javax.swing.JTextPane dbpediaQueryTextPane;
    private javax.swing.JLabel dbpediaTitle;
    private javax.swing.JPanel disabledPanels;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel europeanOpenDataButtonPanel;
    private javax.swing.JButton europeanOpenDataCheckButton;
    private javax.swing.JPanel europeanOpenDataInnerPanel;
    private javax.swing.JButton europeanOpenDataOpenEndPointButton;
    private javax.swing.JButton europeanOpenDataOpenQueryButton;
    private javax.swing.JPanel europeanOpenDataPanel;
    private javax.swing.JScrollPane europeanOpenDataQueryScrollPane;
    private javax.swing.JTextPane europeanOpenDataQueryTextPane;
    private javax.swing.JLabel europeanOpenDataTitle;
    private javax.swing.JPanel europeanaButtonPanel;
    private javax.swing.JButton europeanaCheckButton;
    private javax.swing.JPanel europeanaInnerPanel;
    private javax.swing.JPanel europeanaPanel;
    private javax.swing.JScrollPane europeanaQueryScrollPane;
    private javax.swing.JTextPane europeanaQueryTextPane;
    private javax.swing.JLabel europeanaTitle;
    private javax.swing.JPanel genericButtonPanel;
    private javax.swing.JComboBox genericEncodingComboBox;
    private javax.swing.JPanel genericPanel;
    private javax.swing.JPanel genericPanelInner;
    private javax.swing.JLabel genericQueryLabel;
    private javax.swing.JScrollPane genericQueryScrollPane;
    private javax.swing.JTextPane genericQueryTextPane;
    private javax.swing.JLabel genericTitle;
    private javax.swing.JLabel genericURLLabel;
    private javax.swing.JTextField genericURLTextField;
    private javax.swing.JPanel hriButtonPanel;
    private javax.swing.JButton hriCheckButton;
    private javax.swing.JPanel hriInnerPanel;
    private javax.swing.JPanel hriPanel;
    private javax.swing.JScrollPane hriQueryScrollPane;
    private javax.swing.JTextPane hriQueryTextPane;
    private javax.swing.JLabel hriTitle;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox resultFormatComboBox;
    private javax.swing.JLabel resultFormatLabel;
    private javax.swing.JButton showGenericButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel urlPanel;
    private javax.swing.JPanel wikidataButtonPanel;
    private javax.swing.JButton wikidataCheckButton;
    private javax.swing.JPanel wikidataInnerPanel;
    private javax.swing.JButton wikidataOpenEndPoint;
    private javax.swing.JButton wikidataOpenQueryButton;
    private javax.swing.JPanel wikidataPanel;
    private javax.swing.JScrollPane wikidataQueryScrollPane;
    private javax.swing.JTextPane wikidataQueryTextPane;
    private javax.swing.JLabel wikidataTitle;
    // End of variables declaration//GEN-END:variables

}
