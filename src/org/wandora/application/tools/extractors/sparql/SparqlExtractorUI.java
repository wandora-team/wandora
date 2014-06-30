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
 * SparqlExtractorUI.java
 *
 * Created on 26.5.2011, 14:13:37
 */





package org.wandora.application.tools.extractors.sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.JDialog;
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

/**
 *
 * @author akivela
 */
public class SparqlExtractorUI extends javax.swing.JPanel {

    
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
                "PREFIX yksikkö: <http://semantic.hri.fi/data/dimensions/Yksikkö#>\n"+
                "PREFIX henkilöluku: <http://semantic.hri.fi/data/dimensions/Henkilöluku#>\n"+
                "PREFIX ikäryhmä: <http://semantic.hri.fi/data/dimensions/Ikäryhmä#>\n"+
                "PREFIX äidinkieli: <http://semantic.hri.fi/data/dimensions/Äidinkieli#>\n"+
                "PREFIX sukupuoli: <http://semantic.hri.fi/data/dimensions/Sukupuoli#>\n"+
                "PREFIX ikä: <http://semantic.hri.fi/data/dimensions/Ikä#>\n"+
                "PREFIX lasten_määrä_perheessä_140-17v_15: <http://semantic.hri.fi/data/dimensions/Lasten_määrä_perheessä_140-17v_15#>\n"+
                "PREFIX perhetyyppi: <http://semantic.hri.fi/data/dimensions/Perhetyyppi#>\n"+
                "PREFIX väestön_määrä: <http://semantic.hri.fi/data/dimensions/Väestön_määrä#>\n"+
                "PREFIX lasten_määrä_perheessä: <http://semantic.hri.fi/data/dimensions/Lasten_määrä_perheessä#>\n"+
                "PREFIX muuttosuunta: <http://semantic.hri.fi/data/dimensions/Muuttosuunta#>\n"+
                "PREFIX koulutusaste: <http://semantic.hri.fi/data/dimensions/Koulutusaste#>\n"+
                "PREFIX toimiala: <http://semantic.hri.fi/data/dimensions/Toimiala#>\n"+
                "PREFIX toimiala-1995: <http://semantic.hri.fi/data/dimensions/Toimiala-1995#>\n"+
                "PREFIX työttömyys: <http://semantic.hri.fi/data/dimensions/Työttömyys#>\n"+
                "PREFIX valmistumisvuosi: <http://semantic.hri.fi/data/dimensions/Valmistumisvuosi#>\n"+
                "PREFIX käytössäolotilanne: <http://semantic.hri.fi/data/dimensions/Käytössäolotilanne#>\n"+
                "PREFIX käyttötarkoitus_ja_kerrosluku: <http://semantic.hri.fi/data/dimensions/Käyttötarkoitus_ja_kerrosluku#>\n"+
                "PREFIX tuloluokka: <http://semantic.hri.fi/data/dimensions/Tuloluokka#>\n"+
                "PREFIX käyttötarkoitus: <http://semantic.hri.fi/data/dimensions/Käyttötarkoitus#>\n"+
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
        dialog.setSize(750,400);
        dialog.add(this);
        dialog.setTitle("SPARQL Extractor");
        UIBox.centerWindow(dialog, w);
        dialog.setVisible(true);
    }






    public String[] getQueryURLs(WandoraTool parentTool) {
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
            String queryURL = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=__QUERY__&debug=on&timeout=&format=application%2Fsparql-results%2Bjson&save=display&fname=";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** HRI *****
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
            String queryURL = "http://services.data.gov.uk/reference/sparql?query=__QUERY__";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** DATA.GOV *****
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
            String queryURL = "http://europeana.ontotext.com/sparql.xml?query=__QUERY__";
            queryURL = queryURL.replace(QUERY_MACRO, query);
            q = new String[] { queryURL };
        }
        // ***** EUROPEANA *****
        else if(europeanOpenDataPanel.equals(component)) {
            String query = europeanOpenDataQueryTextPane.getText();
            query = prepareQuery(query, "UTF-8");
            String queryURL = "http://open-data.europa.eu/open-data/sparql?query=__QUERY__&format=application%2Fxml";
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
        dbpediaPanel = new javax.swing.JPanel();
        dbpediaInnerPanel = new javax.swing.JPanel();
        dbpediaTitle = new SimpleLabel();
        dbpediaQueryScrollPane = new SimpleScrollPane();
        dbpediaQueryTextPane = new SimpleTextPane();
        dbpediaButtonPanel = new javax.swing.JPanel();
        dbpediaCheckButton = new SimpleButton();
        hriPanel = new javax.swing.JPanel();
        hriInnerPanel = new javax.swing.JPanel();
        hriTitle = new SimpleLabel();
        hriQueryScrollPane = new SimpleScrollPane();
        hriQueryTextPane = new SimpleTextPane();
        hriButtonPanel = new javax.swing.JPanel();
        hriCheckButton = new SimpleButton();
        europeanaPanel = new javax.swing.JPanel();
        europeanaInnerPanel = new javax.swing.JPanel();
        europeanaTitle = new SimpleLabel();
        europeanaQueryScrollPane = new SimpleScrollPane();
        europeanaQueryTextPane = new SimpleTextPane();
        europeanaButtonPanel = new javax.swing.JPanel();
        europeanaCheckButton = new SimpleButton();
        europeanOpenDataPanel = new javax.swing.JPanel();
        europeanOpenDataInnerPanel = new javax.swing.JPanel();
        europeanOpenDataTitle = new SimpleLabel();
        europeanOpenDataQueryScrollPane = new SimpleScrollPane();
        europeanOpenDataQueryTextPane = new SimpleTextPane();
        europeanOpenDataButtonPanel = new javax.swing.JPanel();
        europeanOpenDataCheckButton = new SimpleButton();
        datagovukPanel = new javax.swing.JPanel();
        datagovukInnerPanel = new javax.swing.JPanel();
        datagovukTitle = new SimpleLabel();
        datagovukQueryScrollPane = new SimpleScrollPane();
        datagovukQueryTextPane = new SimpleTextPane();
        datagovukButtonPanel = new javax.swing.JPanel();
        datagovukCheckButton = new SimpleButton();
        datagovPanel = new javax.swing.JPanel();
        datagovInnerPanel = new javax.swing.JPanel();
        datagovTitle = new SimpleLabel();
        datagovQueryScrollPane = new SimpleScrollPane();
        datagovQueryTextPane = new SimpleTextPane();
        datagovButtonPanel = new javax.swing.JPanel();
        datagovCheckButton = new SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        genericPanel.setLayout(new java.awt.GridBagLayout());

        genericPanelInner.setLayout(new java.awt.GridBagLayout());

        genericTitle.setText("<html>This tab is used to send SPARQL queries to a given end point and transform the result set to a topic map. Fill in the end point URL and the query. Use special keyword '__QUERY__' to mark the query location in the URL.</html>");
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
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 0);
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

        genericQueryTextPane.setText("select distinct ?Concept where {[] a ?Concept}");
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
        genericEncodingComboBox.setMinimumSize(new java.awt.Dimension(70, 18));
        genericEncodingComboBox.setPreferredSize(new java.awt.Dimension(80, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        genericButtonPanel.add(genericEncodingComboBox, gridBagConstraints);

        resultFormatLabel.setText("Result set format");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        genericButtonPanel.add(resultFormatLabel, gridBagConstraints);

        resultFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "JSON", "XML", "RDF/XML" }));
        resultFormatComboBox.setMinimumSize(new java.awt.Dimension(70, 18));
        resultFormatComboBox.setPreferredSize(new java.awt.Dimension(80, 21));
        genericButtonPanel.add(resultFormatComboBox, new java.awt.GridBagConstraints());

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 10);
        genericButtonPanel.add(jSeparator1, gridBagConstraints);

        showGenericButton.setText("Open Request URL");
        showGenericButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        showGenericButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showGenericButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        genericButtonPanel.add(showGenericButton, gridBagConstraints);

        checkGenericButton.setText("Check Query");
        checkGenericButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
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

        dbpediaPanel.setLayout(new java.awt.GridBagLayout());

        dbpediaInnerPanel.setLayout(new java.awt.GridBagLayout());

        dbpediaTitle.setText("<html>Write and send your query to DBPedia's SPARQL end point at http://dbpedia.org/sparql and transform result set to a topic map.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        dbpediaInnerPanel.add(dbpediaTitle, gridBagConstraints);

        dbpediaQueryScrollPane.setViewportView(dbpediaQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        dbpediaInnerPanel.add(dbpediaQueryScrollPane, gridBagConstraints);

        dbpediaButtonPanel.setLayout(new java.awt.GridBagLayout());

        dbpediaCheckButton.setText("Check Query");
        dbpediaCheckButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        dbpediaCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dbpediaCheckButtonMouseReleased(evt);
            }
        });
        dbpediaButtonPanel.add(dbpediaCheckButton, new java.awt.GridBagConstraints());

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

        tabbedPane.addTab("HRI", hriPanel);

        europeanaPanel.setLayout(new java.awt.GridBagLayout());

        europeanaInnerPanel.setLayout(new java.awt.GridBagLayout());

        europeanaTitle.setText("<html>Write and send query to Europeana's SPARQL end point at http://europeana.ontotext.com/sparql and transform result set to a topic map.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        europeanaInnerPanel.add(europeanaTitle, gridBagConstraints);

        europeanaQueryTextPane.setText("# Creators and collections of Europeana objects from Italy\n\nPREFIX dc: <http://purl.org/dc/elements/1.1/>\nSELECT DISTINCT ?EuropeanaObject ?Creator ?Collection\nWHERE {\n\n?EuropeanaObject dc:creator ?Creator ;\n                 edm:collectionName ?Collection ;\n                 edm:country \"italy\"\n\n}\nLIMIT 100");
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

        tabbedPane.addTab("Europeana", europeanaPanel);

        europeanOpenDataPanel.setLayout(new java.awt.GridBagLayout());

        europeanOpenDataInnerPanel.setLayout(new java.awt.GridBagLayout());

        europeanOpenDataTitle.setText("<html>Write and send query to European Comission's Open Data SPARQL end point at http://open-data.europa.eu/open-data/sparql and transform result set to a topic map.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        europeanOpenDataInnerPanel.add(europeanOpenDataTitle, gridBagConstraints);

        europeanOpenDataQueryTextPane.setText("select distinct ?Concept where {[] a ?Concept} LIMIT 100");
        europeanOpenDataQueryScrollPane.setViewportView(europeanOpenDataQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        europeanOpenDataInnerPanel.add(europeanOpenDataQueryScrollPane, gridBagConstraints);

        europeanOpenDataButtonPanel.setLayout(new java.awt.GridBagLayout());

        europeanOpenDataCheckButton.setText("Check Query");
        europeanOpenDataCheckButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        europeanOpenDataCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                europeanOpenDataCheckButtonMouseReleased(evt);
            }
        });
        europeanOpenDataButtonPanel.add(europeanOpenDataCheckButton, new java.awt.GridBagConstraints());

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

        tabbedPane.addTab("European Comission Open Data", europeanOpenDataPanel);

        datagovukPanel.setLayout(new java.awt.GridBagLayout());

        datagovukInnerPanel.setLayout(new java.awt.GridBagLayout());

        datagovukTitle.setText("Write and send SPARQL query to data.gov.uk's SPARQL end point and transform result set to a topic map.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        datagovukInnerPanel.add(datagovukTitle, gridBagConstraints);

        datagovukQueryTextPane.setText("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX skos: <http://www.w3.org/2004/02/skos/core#>\nSELECT DISTINCT ?type\n    WHERE {\n      ?thing a ?type\n    }    LIMIT 10");
        datagovukQueryScrollPane.setViewportView(datagovukQueryTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        datagovukInnerPanel.add(datagovukQueryScrollPane, gridBagConstraints);

        datagovukButtonPanel.setLayout(new java.awt.GridBagLayout());

        datagovukCheckButton.setText("Check Query");
        datagovukCheckButton.setMargin(new java.awt.Insets(0, 5, 0, 5));
        datagovukCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                datagovukCheckButtonMouseReleased(evt);
            }
        });
        datagovukButtonPanel.add(datagovukCheckButton, new java.awt.GridBagConstraints());

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

        tabbedPane.addTab("data.gov", datagovPanel);

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
            .addGap(0, 425, Short.MAX_VALUE)
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
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
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
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
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

    
    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        accepted = false;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
        accepted = true;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_okButtonMouseReleased

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

    private void dbpediaCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dbpediaCheckButtonMouseReleased
        checkSPARQLQuery(dbpediaQueryTextPane.getText());
    }//GEN-LAST:event_dbpediaCheckButtonMouseReleased



    private void hriCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hriCheckButtonMouseReleased
        checkSPARQLQuery(hriQueryTextPane.getText());
    }//GEN-LAST:event_hriCheckButtonMouseReleased

    private void datagovukCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datagovukCheckButtonMouseReleased
        checkSPARQLQuery(datagovukQueryTextPane.getText());
    }//GEN-LAST:event_datagovukCheckButtonMouseReleased

    private void datagovCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datagovCheckButtonMouseReleased
        checkSPARQLQuery(datagovQueryTextPane.getText());
    }//GEN-LAST:event_datagovCheckButtonMouseReleased

    private void europeanaCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_europeanaCheckButtonMouseReleased
        checkSPARQLQuery(europeanaQueryTextPane.getText());
    }//GEN-LAST:event_europeanaCheckButtonMouseReleased

    private void europeanOpenDataCheckButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_europeanOpenDataCheckButtonMouseReleased
        checkSPARQLQuery(europeanOpenDataQueryTextPane.getText());
    }//GEN-LAST:event_europeanOpenDataCheckButtonMouseReleased


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
    private javax.swing.JPanel datagovukPanel;
    private javax.swing.JScrollPane datagovukQueryScrollPane;
    private javax.swing.JTextPane datagovukQueryTextPane;
    private javax.swing.JLabel datagovukTitle;
    private javax.swing.JPanel dbpediaButtonPanel;
    private javax.swing.JButton dbpediaCheckButton;
    private javax.swing.JPanel dbpediaInnerPanel;
    private javax.swing.JPanel dbpediaPanel;
    private javax.swing.JScrollPane dbpediaQueryScrollPane;
    private javax.swing.JTextPane dbpediaQueryTextPane;
    private javax.swing.JLabel dbpediaTitle;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel europeanOpenDataButtonPanel;
    private javax.swing.JButton europeanOpenDataCheckButton;
    private javax.swing.JPanel europeanOpenDataInnerPanel;
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
    // End of variables declaration//GEN-END:variables

}
