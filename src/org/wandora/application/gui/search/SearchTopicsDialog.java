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
 * 
 * SearchTopicsDialog.java
 *
 * Created on 30. joulukuuta 2008, 12:42
 */

package org.wandora.application.gui.search;

import de.topicmapslab.tmql4j.components.processor.results.model.IResult;
import de.topicmapslab.tmql4j.components.processor.results.model.IResultSet;
import de.topicmapslab.tmql4j.components.processor.runtime.ITMQLRuntime;
import de.topicmapslab.tmql4j.components.processor.runtime.TMQLRuntimeFactory;
import de.topicmapslab.tmql4j.query.IQuery;
import org.wandora.application.gui.table.MixedTopicTable;
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;
import uk.ac.shef.wit.simmetrics.tokenisers.*;

import org.wandora.utils.*;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.Tuples.T3;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.*;
import org.wandora.query2.*;
import java.awt.*;
import java.util.*;
import javax.script.*;
import java.net.*;
import org.tmapi.core.DatatypeAware;
import org.tmapi.core.Name;
import org.wandora.application.gui.tree.TopicTreePanel;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.wandora2tmapi.W2TRole;
import org.wandora.topicmap.wandora2tmapi.W2TTopic;



/**
 * Use SearchTopicsFrame instead.
 * 
 * @deprecated 
 * @author  akivela
 */
public class SearchTopicsDialog extends javax.swing.JDialog {

    public static final int SEARCH = 1;
    public static final int SIMILARITY = 2;
    public static final int QUERY = 3;
    public static final int TMQL = 4;
    

    public static final int SIMILARITY_LEVENSHTEIN_DISTANCE = 101;
    public static final int SIMILARITY_NEEDLEMAN_WUNCH_DISTANCE = 102;
    public static final int SIMILARITY_SMITH_WATERMAN_DISTANCE = 104;
    public static final int SIMILARITY_GOTOH_DISTANCE = 105;
    public static final int SIMILARITY_BLOCK_DISTANCE = 107;
    public static final int SIMILARITY_MONGE_ELKAN_DISTANCE = 110;
    public static final int SIMILARITY_JARO_DISTANCE_METRIC = 111;
    public static final int SIMILARITY_JARO_WINKLER = 112;
    public static final int SIMILARITY_SOUNDEX_DISTANCE_METRIC = 113;
    public static final int SIMILARITY_MATCHING_COEFFICIENT = 114;
    public static final int SIMILARITY_DICES_COEFFICIENT = 115;
    public static final int SIMILARITY_JACCARD_SIMILARITY = 116;
    public static final int SIMILARITY_OVERLAP_COEFFICIENT = 119;
    public static final int SIMILARITY_EUCLIDEAN_DISTANCE = 120;
    public static final int SIMILARITY_COSINE_SIMILARITY = 122;
    public static final int SIMILARITY_Q_GRAM = 136;

    
    
    public T2[] similarityTypes = {
        new T2("Levenshtein distance",                  new Integer(SIMILARITY_LEVENSHTEIN_DISTANCE)),
        new T2("Needleman-Wunch distance (Sellers Algorithm)", new Integer(SIMILARITY_NEEDLEMAN_WUNCH_DISTANCE)),
        new T2("Smith-Waterman distance",               new Integer(SIMILARITY_SMITH_WATERMAN_DISTANCE)),
        new T2("Block distance (L1 distance or City block distance)", new Integer(SIMILARITY_BLOCK_DISTANCE)),
        new T2("Monge Elkan distance",                  new Integer(SIMILARITY_MONGE_ELKAN_DISTANCE)),
        new T2("Jaro distance metric",                  new Integer(SIMILARITY_JARO_DISTANCE_METRIC)),
        new T2("Jaro Winkler",                          new Integer(SIMILARITY_JARO_WINKLER)),
        new T2("SoundEx distance metric",               new Integer(SIMILARITY_SOUNDEX_DISTANCE_METRIC)),
        new T2("Matching Coefficient",                  new Integer(SIMILARITY_MATCHING_COEFFICIENT)),
        new T2("Dice's Coefficient",                    new Integer(SIMILARITY_DICES_COEFFICIENT)),
        new T2("Jaccard Similarity (Jaccard Coefficient or Tanimoto coefficient)", new Integer(SIMILARITY_OVERLAP_COEFFICIENT)),
        new T2("Overlap Coefficient",                   new Integer(SIMILARITY_JACCARD_SIMILARITY)),
        new T2("Euclidean distance (L2 distance)",      new Integer(SIMILARITY_EUCLIDEAN_DISTANCE)),
        new T2("Cosine similarity",                     new Integer(SIMILARITY_COSINE_SIMILARITY)),
        new T2("q-gram",                                new Integer(SIMILARITY_Q_GRAM)),
    };
    
    
    public T2[] similarityTokenizers = {
        new T2("Whitespace", new TokeniserWhitespace()),
        new T2("CSVBasic", new TokeniserCSVBasic()),
        new T2("QGram2", new TokeniserQGram2()),
        new T2("QGram2 extended", new TokeniserQGram2Extended()),
        new T2("QGram3", new TokeniserQGram3()),
        new T2("QGram3 extended", new TokeniserQGram3Extended())
    };
    
    
    private Wandora wandora = null;
    private WandoraTool tool = null;
    private boolean accept = false;
    
    
    
    /** Creates new form SearchTopicsDialog */
    public SearchTopicsDialog(Wandora wandora, WandoraTool tool, boolean modal) {
        super(wandora, modal);
        this.wandora = wandora;
        this.tool = tool;
        this.accept = false;
        initComponents();
        
        scriptTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tmqlTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        similarityThresholdSlider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        similarityTypeComboBox.removeAllItems();
        similarityTypeComboBox.setEditable(false);
        for(int i=0; i<similarityTypes.length; i++) {
            similarityTypeComboBox.addItem(similarityTypes[i].e1);
        }
        similarityTokenizerComboBox.removeAllItems();
        similarityTokenizerComboBox.setEditable(false);
        for(int i=0; i<similarityTokenizers.length; i++) {
            similarityTokenizerComboBox.addItem(similarityTokenizers[i].e1);
        }
        updateSimilarityOptions();


        engineComboBox.setEditable(false);
        ArrayList<String> engines=WandoraScriptManager.getAvailableEngines();
        engineComboBox.removeAllItems();
        for(int i=0;i<engines.size();i++){
            String e=engines.get(i);
            if(e != null && e.length() > 0) {
                engineComboBox.addItem(e);
            }
        }
        queryComboBox.setEditable(false);
        readStoredScriptQueries();
        readStoredTmqlQueries();

        this.setSize(800, 400);
        if(wandora != null) wandora.centerWindow(this);
    }

    
    
    public boolean wasAccepted() {
        return accept;
    }
    
    
    @Override
    public void setVisible(boolean visibility) {
        if(visibility) {
            this.accept = false;
            Component currentTab = searchTabbedPane.getSelectedComponent();
            if(searchPanel.equals(currentTab)) {
                searchTextPane.requestFocus();
            }
            else if(similarityPanel.equals(currentTab)) {
                similarityTextPane.requestFocus();
            }
            else if(queryPanel.equals(currentTab)) {
                scriptTextPane.requestFocus();
            }
        }
        super.setVisible(visibility);
    }
    
    
    public int getSearchType() {
        Component c = searchTabbedPane.getSelectedComponent();
        if(searchPanel.equals(c)) {
            return SEARCH;
        }
        else if(similarityPanel.equals(c)) {
            return SIMILARITY;
        }
        else if(queryPanel.equals(c)) {
            return QUERY;
        }
        else if(tmqlPanel.equals(c)){
            return TMQL;
        }
        else return 0;
    }
    
    
    public String getTMQLQuery() {
        return tmqlTextPane.getText();
    }
    
    
    public String getSearchQuery() {
        return searchTextPane.getText();
    }
    public TopicMapSearchOptions getSearchOptions() {
        return new TopicMapSearchOptions(
                searchBasenameCheckBox.isSelected(),
                searchVariantCheckBox.isSelected(),
                searchOccurrenceCheckBox.isSelected(),
                searchSICheckBox.isSelected(),
                searchSLCheckBox.isSelected()
                );
    }
    
    public String getSimilarityQuery() {
        return similarityTextPane.getText();
    }
    
    public int getSimilarityType() {
        Object type = similarityTypeComboBox.getSelectedItem();
        if(type != null) {
            Object t = null;
            for(int i=0; i<similarityTypes.length; i++) {
                if(type.equals(similarityTypes[i].e1)) {
                    t = similarityTypes[i].e2;
                    if(t instanceof Integer) {
                        return ((Integer) t).intValue();
                    }
                }
            }
        }
        return 0;
    }
    
    
    
    public InterfaceTokeniser getSimilarityTokenizer() {
        Object tokenizer = similarityTokenizerComboBox.getSelectedItem();
        if(tokenizer != null) {
            Object t = null;
            for(int i=0; i<similarityTokenizers.length; i++) {
                if(tokenizer.equals(similarityTokenizers[i].e1)) {
                    t = similarityTokenizers[i].e2;
                    if(t instanceof InterfaceTokeniser) {
                        return (InterfaceTokeniser) t;
                    }
                }
            }
        }
        return new TokeniserWhitespace();
    }
    
    
    
    
    public TopicMapSearchOptions getSimilarityOptions() {
        return new TopicMapSearchOptions(
                similarityBasenameCheckBox.isSelected(),
                similarityVariantCheckBox.isSelected(),
                similarityOccurrenceCheckBox.isSelected(),
                similaritySICheckBox.isSelected(),
                similaritySLCheckBox.isSelected()
                );
    }
    
    
    public float getSimilarityThreshold() {
        String ts = similarityThresholdTextField.getText();
        float threshold = 0.5f;
        try {
            threshold = Float.parseFloat(ts);
        }
        catch(Exception e) {
            e.printStackTrace();     
        }
        return Math.max(0, Math.min( threshold, 100 )) / 100;
    }
    
    

    
    
    
    public void updateSimilarityThreshold() {
        int threshold = similarityThresholdSlider.getValue();
        similarityThresholdTextField.setText(""+threshold);
    }
    
    
    
    
    public Collection<Topic> getSimilarTopics(TopicMap tm, WandoraTool tool) {
        if(tm != null) {
            try {
                int similarityType = getSimilarityType();
                float threshold = getSimilarityThreshold();
                Iterator<Topic> iterator = tm.getTopics();
                String query = getSimilarityQuery();
                TopicMapSearchOptions options = getSimilarityOptions();
                boolean differenceInsteadOfSimilarity = similarityDifferenceCheckBox.isSelected();
                return getSimilarTopics(query, options, iterator, similarityType, threshold, differenceInsteadOfSimilarity, tool);
            }
            catch(Exception e) {
                if(wandora != null) wandora.handleError(e);
                else e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
    public InterfaceStringMetric getStringMetric(int similarityType) {
        InterfaceStringMetric stringMetric = null;
        InterfaceTokeniser tokenizer = getSimilarityTokenizer();
        switch(similarityType) {
            case SIMILARITY_LEVENSHTEIN_DISTANCE: {
                stringMetric = new Levenshtein();
                break;
            }
            case SIMILARITY_NEEDLEMAN_WUNCH_DISTANCE: {
                float cost = getGapCost(0.5f);
                stringMetric = new NeedlemanWunch(cost);
                break;
            }
            case SIMILARITY_SMITH_WATERMAN_DISTANCE: {
                float cost = getGapCost(0.5f);
                stringMetric = new SmithWaterman(cost);
                break;
            }
            case SIMILARITY_BLOCK_DISTANCE: {
                stringMetric = new BlockDistance(tokenizer);
                break;
            }
            case SIMILARITY_MONGE_ELKAN_DISTANCE: {
                stringMetric = new MongeElkan(tokenizer);
                break;
            }
            case SIMILARITY_JARO_DISTANCE_METRIC: {
                stringMetric = new Jaro();
                break;
            }
            case SIMILARITY_JARO_WINKLER: {
                stringMetric = new JaroWinkler();
                break;
            }
            case SIMILARITY_SOUNDEX_DISTANCE_METRIC: {
                stringMetric = new Soundex();
                break;
            }
            case SIMILARITY_MATCHING_COEFFICIENT: {
                stringMetric = new MatchingCoefficient(tokenizer);
                break;
            }
            case SIMILARITY_DICES_COEFFICIENT: {
                stringMetric = new DiceSimilarity(tokenizer);
                break;
            }
            case SIMILARITY_JACCARD_SIMILARITY: {
                stringMetric = new JaccardSimilarity(tokenizer);
                break;
            }
            case SIMILARITY_OVERLAP_COEFFICIENT: {
                stringMetric = new OverlapCoefficient(tokenizer);
                break;
            }
            case SIMILARITY_EUCLIDEAN_DISTANCE: {
                stringMetric = new EuclideanDistance(tokenizer);
                break;
            }
            case SIMILARITY_COSINE_SIMILARITY: {
                stringMetric = new CosineSimilarity(tokenizer);
                break;
            }
            case SIMILARITY_Q_GRAM: {
                stringMetric = new QGramsDistance(tokenizer);
                break;
            }
            default: {
                System.out.println("Unknown similarity type used in similarity test");
            }
        }
        return stringMetric;
    }
    
    
    
    
    public Collection<Topic> getSimilarTopics(String query, TopicMapSearchOptions options, Iterator<Topic> topicIterator, int similarityType, float threshold, boolean differenceInsteadOfSimilarity, WandoraTool tool) {
        InterfaceStringMetric stringMetric = getStringMetric(similarityType);
        if(stringMetric != null) {
            return getSimilarTopics(query, options, topicIterator, stringMetric, threshold, differenceInsteadOfSimilarity, tool);
        }
        return new ArrayList<Topic>();
    }
    
    
    
    
    
    
    public Collection<Topic> getSimilarTopics(String query, TopicMapSearchOptions options, Iterator<Topic> topicIterator, InterfaceStringMetric stringMetric, float threshold, boolean differenceInsteadOfSimilarity, WandoraTool tool) {
        ArrayList<Topic> selection = new ArrayList<Topic>();
        int count = 0;
        Topic t = null;
        boolean isSimilar = false;
        float similarity = 0.0f;
        try {
            while(topicIterator.hasNext() && (tool != null && !tool.forceStop())) {
                if(tool != null) tool.setProgress(++count / 100);
                similarity = 0.0f;
                isSimilar = false;
                t = topicIterator.next();
                if(t != null && !t.isRemoved()) {
                    if(options.searchBasenames) {
                        String n = t.getBaseName();
                        if(n != null && n.length() > 0) {
                            similarity = getSimilarity(query, n, stringMetric);
                            isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                        }
                    }
                    if(!isSimilar && options.searchVariants) {
                        String n = null;
                        Set<Set<Topic>> scopes = t.getVariantScopes();
                        Iterator<Set<Topic>> scopeIterator = scopes.iterator();
                        Set<Topic> scope = null;
                        while(!isSimilar && scopeIterator.hasNext()) {
                            scope = scopeIterator.next();
                            if(scope != null) {
                                n = t.getVariant(scope);
                                if(n != null && n.length() > 0) {
                                    similarity = getSimilarity(query, n, stringMetric);
                                    isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                                }
                            }
                        }
                    }
                    if(!isSimilar && options.searchOccurrences) {
                        String o = null;
                        Collection<Topic> types = t.getDataTypes();
                        Iterator<Topic> typeIterator = types.iterator();
                        Topic type = null;
                        Hashtable<Topic, String> os = null;
                        Enumeration<Topic> osEnumeration = null;
                        Topic osTopic = null;
                        while(!isSimilar && typeIterator.hasNext()) {
                            type = typeIterator.next();
                            if(type != null && !type.isRemoved()) {
                                os = t.getData(type);
                                osEnumeration = os.keys();
                                while(osEnumeration.hasMoreElements() && !isSimilar) {
                                    osTopic = osEnumeration.nextElement();
                                    o = os.get(osTopic);
                                    if(o != null && o.length() > 0) {
                                        similarity = getSimilarity(query, o, stringMetric);
                                        isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                                    }
                                }
                            }
                        }
                    }
                    if(!isSimilar && options.searchSIs) {
                        Collection<Locator> locs = t.getSubjectIdentifiers();
                        if(locs != null && locs.size() > 0) {
                            Iterator<Locator> locIter = locs.iterator();
                            Locator loc = null;
                            String l = null;
                            while(locIter.hasNext() && !isSimilar) {
                                loc = locIter.next();
                                if(loc != null) {
                                    l = loc.toExternalForm();
                                    similarity = getSimilarity(query, l, stringMetric);
                                    isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                                }
                            }
                        }
                    }
                    if(!isSimilar && options.searchSL) {
                        Locator loc = t.getSubjectLocator();
                        if(loc != null) {
                            String l = loc.toExternalForm();
                            similarity = getSimilarity(query, l, stringMetric);
                            isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                        }
                    }
                    // ***** IF TOPIC IS SIMILAR ****
                    if(isSimilar) {
                        selection.add(t);
                        if(tool != null) {
                            //tool.log("Found similar topic '"+t.getBaseName()+"'");
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return selection;
    }
    
    
    
    public float getSimilarity(String s1, String s2, InterfaceStringMetric stringMetric) {
        float similarity = 0.0f;
        if(s1 != null && s2 != null && stringMetric != null) {
            similarity = stringMetric.getSimilarity(s1, s2);
        }
        return similarity;
    }
    
    
    
    
    public boolean isSimilar(float similarity, float threshold, boolean differenceInsteadOfSimilarity) {
        if(differenceInsteadOfSimilarity) {
            return (similarity < threshold);
        }
        else {
            return (similarity > threshold);
        }
    }
    
    
    
    public void updateSimilarityOptions() {
        int t = getSimilarityType();
        if(t == SIMILARITY_SMITH_WATERMAN_DISTANCE ||
           t == SIMILARITY_NEEDLEMAN_WUNCH_DISTANCE) {
            gapCostTextField.setEnabled(true);
            gapCostLabel.setEnabled(true);
        }
        else {
            gapCostTextField.setEnabled(false);
            gapCostLabel.setEnabled(false);
        }
        
        
        if(t == SIMILARITY_SOUNDEX_DISTANCE_METRIC ||
           t == SIMILARITY_JARO_WINKLER ||
           t == SIMILARITY_JARO_DISTANCE_METRIC ||
           t == SIMILARITY_LEVENSHTEIN_DISTANCE ||
           t == SIMILARITY_NEEDLEMAN_WUNCH_DISTANCE ||
           t == SIMILARITY_SMITH_WATERMAN_DISTANCE) {
                similarityTokenizerComboBox.setEnabled(false);
                similarityTokenizerLabel.setEnabled(false);
        }
        else {
            similarityTokenizerComboBox.setEnabled(true);
            similarityTokenizerLabel.setEnabled(true);
        }
        
        InterfaceStringMetric stringMetric = getStringMetric(getSimilarityType());
        if(stringMetric != null) {
            similarityTypeComboBox.setToolTipText(Textbox.makeHTMLParagraph(stringMetric.getLongDescriptionString(), 40));
        }
    }
    
    public void updateTokenizerOptions() {
        InterfaceTokeniser tokenizer = getSimilarityTokenizer();
        if(tokenizer != null) {
            similarityTokenizerComboBox.setToolTipText(Textbox.makeHTMLParagraph(tokenizer.getShortDescriptionString(), 40));
        }
    }
    
    
    
    public float getGapCost(float defaultValue) {
        float cost = defaultValue;
        try {
            cost = Float.parseFloat(gapCostTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return cost;
    }




    // -------------------------------------------------------------------------
    // ------------------------------------------------------- QUERY SCRIPTS ---
    // -------------------------------------------------------------------------




    private String SCRIPT_QUERY_OPTION_KEY = "scriptQueries";
    private ArrayList<T3<String,String,String>> storedQueryScripts = new ArrayList<T3<String,String,String>>();


    
    private void readStoredScriptQueries() {
        storedQueryScripts = new ArrayList<T3<String,String,String>>();
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                int queryCount = 0;
                String queryScript = null;
                String queryEngine = null;
                String queryName = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].name");
                while(queryName != null && queryName.length() > 0) {
                    queryScript = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].script");
                    queryEngine = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].engine");
                    storedQueryScripts.add( new T3(queryName, queryEngine, queryScript) );
                    queryCount++;
                    queryName = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].name");
                }
                updateQueryComboBox();
            }
        }
    }


    private void writeScriptQueries() {
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                options.removeAll(SCRIPT_QUERY_OPTION_KEY);
                int queryCount = 0;
                for( T3<String,String,String> storedQuery : storedQueryScripts ) {
                    if(storedQuery != null) {
                        options.put(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].name", storedQuery.e1);
                        options.put(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].engine", storedQuery.e2);
                        options.put(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].script", storedQuery.e3);
                        queryCount++;
                    }
                }
            }
        }
    }


    public void updateQueryComboBox() {
        queryComboBox.removeAllItems();
        String name = "";
        String script = "";
        String engine = "";
        for( T3<String,String,String> storedQuery : storedQueryScripts ) {
            if(storedQuery != null) {
                name = storedQuery.e1;
                engine = storedQuery.e2;
                script = storedQuery.e3;
                queryComboBox.addItem(name);
            }
        }
        queryComboBox.setSelectedItem(name);
        engineComboBox.setSelectedItem(engine);
        scriptTextPane.setText(script);
    }


    public void addScriptQuery() {
        String queryName = WandoraOptionPane.showInputDialog(wandora, "Give name for the query script?", "", "Name of the query script");
        if(queryName != null && queryName.length() > 0) {
            String queryEngine = engineComboBox.getSelectedItem().toString();
            String queryScript = scriptTextPane.getText();
            storedQueryScripts.add( new T3(queryName, queryEngine, queryScript) );
            writeScriptQueries();
            updateQueryComboBox();
        }
    }



    public void deleteScriptQuery() {
        int index = queryComboBox.getSelectedIndex();
        if(index < storedQueryScripts.size() && index >= 0) {
            String name = storedQueryScripts.get(index).e1;
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Would you like to remove query script '"+name+"'?", "Delete query script?");
            if(a == WandoraOptionPane.YES_OPTION) {
                storedQueryScripts.remove(index);
                writeScriptQueries();
                updateQueryComboBox();
            }
        }
    }


    public void selectScriptQuery() {
        int index = queryComboBox.getSelectedIndex();
        if(index < storedQueryScripts.size() && index >= 0) {
            T3<String,String,String> query = storedQueryScripts.get(index);
            // queryComboBox.setSelectedIndex(index);
            engineComboBox.setSelectedItem(query.e2);
            scriptTextPane.setText(query.e3);
        }
    }
    
    // ------------------------------------------- TMQL Queries ----
    
    private String TMQL_QUERY_OPTION_KEY = "tmqlQueries";
    private ArrayList<T2<String,String>> storedTmqlQueries = new ArrayList<T2<String,String>>();
    
    private void readStoredTmqlQueries() {
        storedTmqlQueries = new ArrayList<T2<String,String>>();
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                int queryCount = 0;
                String query = null;
                String queryName = options.get(TMQL_QUERY_OPTION_KEY+".query["+queryCount+"].name");
                while(queryName != null && queryName.length() > 0) {
                    query = options.get(TMQL_QUERY_OPTION_KEY+".query["+queryCount+"].query");
                    storedTmqlQueries.add( new T2(queryName, query) );
                    queryCount++;
                    queryName = options.get(TMQL_QUERY_OPTION_KEY+".query["+queryCount+"].name");
                }
                updateTmqlComboBox();
            }
        }
    }


    private void writeTmqlQueries() {
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                options.removeAll(TMQL_QUERY_OPTION_KEY);
                int queryCount = 0;
                for( T2<String,String> storedQuery : storedTmqlQueries ) {
                    if(storedQuery != null) {
                        options.put(TMQL_QUERY_OPTION_KEY+".query["+queryCount+"].name", storedQuery.e1);
                        options.put(TMQL_QUERY_OPTION_KEY+".query["+queryCount+"].query", storedQuery.e2);
                        queryCount++;
                    }
                }
            }
        }
    }


    public void updateTmqlComboBox() {
        tmqlComboBox.removeAllItems();
        String name = "";
        String query = "";
        for( T2<String,String> storedQuery : storedTmqlQueries ) {
            if(storedQuery != null) {
                name = storedQuery.e1;
                query = storedQuery.e2;
                tmqlComboBox.addItem(name);
            }
        }
        tmqlComboBox.setSelectedItem(name);
        tmqlTextPane.setText(query);
    }
    
    public void addTmqlQuery(){
        String queryName = WandoraOptionPane.showInputDialog(wandora, "Give name for the tmql query?", "", "Name of the tmql query");
        if(queryName != null && queryName.length() > 0) {
            String query = tmqlTextPane.getText();
            storedTmqlQueries.add( new T2(queryName, query) );
            writeTmqlQueries();
            updateTmqlComboBox();
        }        
    }
    public void deleteTmqlQuery(){
        int index = tmqlComboBox.getSelectedIndex();
        if(index < storedTmqlQueries.size() && index >= 0) {
            String name = storedTmqlQueries.get(index).e1;
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Would you like to remove tmql query '"+name+"'?", "Delete tmql query?");
            if(a == WandoraOptionPane.YES_OPTION) {
                storedTmqlQueries.remove(index);
                writeTmqlQueries();
                updateTmqlComboBox();
            }
        }        
    }
    public void selectTmqlQuery(){
        int index = tmqlComboBox.getSelectedIndex();
        if(index < storedTmqlQueries.size() && index >= 0) {
            T2<String,String> query = storedTmqlQueries.get(index);
            tmqlTextPane.setText(query.e2);
        }        
    }
    
    public MixedTopicTable getTopicsByTMQL(){
        TopicMap topicMap=wandora.getTopicMap();
        String query=tmqlTextPane.getText();
        try{
            TMQLRunner.TMQLResult res=TMQLRunner.runTMQL(topicMap,query);
            Object[][] data=res.getData();
            Object[] columns=Arrays.copyOf(res.getColumns(), res.getNumColumns(), Object[].class);

            MixedTopicTable table=new MixedTopicTable(wandora);
            table.initialize(data,columns);
            return table;        
        }catch(Exception e){
            wandora.handleError(e);
            return null;
        }
        
    }


    public MixedTopicTable getTopicsByQuery(Iterator<Topic> contextTopics) {
        try {
            TopicMap tm = wandora.getTopicMap();
            WandoraScriptManager sm = new WandoraScriptManager();
            String engineName = engineComboBox.getSelectedItem().toString();
            ScriptEngine engine = sm.getScriptEngine(engineName);
            String scriptStr =  scriptTextPane.getText();
            Directive query = null;
            Object o=engine.eval(scriptStr);
            if(o==null) o=engine.get("query");
            if(o!=null && o instanceof Directive) {
                query = (Directive)o;
            }

            ArrayList<ResultRow> res = new ArrayList<ResultRow>();
            Topic contextTopic = null;
            if(!contextTopics.hasNext()){
                // if context is empty just add some (root of a tree chooser) topic
                HashMap<String,TopicTreePanel> trees=wandora.getTopicTreeManager().getTrees();
                TopicTreePanel tree=trees.values().iterator().next();
                Topic t=tm.getTopic(tree.getRootSI());
                ArrayList<Topic> al=new ArrayList<Topic>();
                al.add(t);
                contextTopics=al.iterator();
            }
            while(contextTopics.hasNext()) {
                contextTopic = contextTopics.next();
                if(contextTopic != null && !contextTopic.isRemoved()) {
                    res.add( new ResultRow(contextTopic) );
                }
            }

            QueryContext context=new QueryContext(tm, "en");

            System.out.println("Query: "+query.debugString());

            if(res.size()==0){}
            else if(res.size()==1){
                res=query.doQuery(context, res.get(0));
            }
            else{
                res=query.from(new Static(res)).doQuery(context, res.get(0));
            }

            ArrayList<String> columns=new ArrayList<String>();
            for(ResultRow row : res){
                for(int i=0;i<row.getNumValues();i++){
                    String l=row.getRole(i);
                    if(!columns.contains(l)) columns.add(l);
                }
            }
            ArrayList<Object> columnTopicsA=new ArrayList<Object>();
            for(int i=0;i<columns.size();i++){
                String l=columns.get(i);
                if(l.toString().startsWith("~")){
                    columns.remove(i);
                    i--;
                }
                else{
                    Topic t=tm.getTopic(l);
                    if(t!=null) columnTopicsA.add(t);
                    else columnTopicsA.add(l);
                }
            }
            Object[] columnTopics=columnTopicsA.toArray(new Object[columnTopicsA.size()]);
            if(res.size() > 0) {
                Object[][] data=new Object[res.size()][columns.size()];
                for(int i=0;i<res.size();i++){
                    ResultRow row=res.get(i);
                    ArrayList<String> roles=row.getRoles();
                    for(int j=0;j<columns.size();j++){
                        String r=columns.get(j);
                        int ind=roles.indexOf(r);
                        if(ind!=-1) data[i][j]=row.getValue(ind);
                        else data[i][j]=null;
                    }
                }

                MixedTopicTable table=new MixedTopicTable(wandora);
                table.initialize(data,columnTopics);
                return table;
            }
        }
        catch(ScriptException se) {
            wandora.handleError(se);
        }
        catch(Exception e) {
            //e.printStackTrace();
            wandora.handleError(e);
        }
        return null;
    }


    /*
    public MixedTopicTable getTopicsByQuery_old(Iterator<Topic> contextTopics) {
        try {
            TopicMap tm = wandora.getTopicMap();
            WandoraScriptManager sm = new WandoraScriptManager();
            ScriptEngine engine = sm.getScriptEngine(engineComboBox.getSelectedItem().toString());
            String scriptStr =  scriptTextPane.getText();
            Directive query = null;
            Object o=engine.eval(scriptStr);
            if(o!=null && o instanceof Directive) {
                query = (Directive)o;
            }

            
            ArrayList<ResultRow> res = new ArrayList<ResultRow>();
            Topic contextTopic = null;
            if(!contextTopics.hasNext()){
                // if context is empty just add some (root of a tree chooser) topic
                HashMap<String,TopicTreePanel> trees=wandora.getTrees();
                TopicTreePanel tree=trees.values().iterator().next();
                Topic t=tm.getTopic(tree.getRootSI());
                ArrayList<Topic> al=new ArrayList<Topic>();
                al.add(t);
                contextTopics=al.iterator();
            }
            while(contextTopics.hasNext()) {
                contextTopic = contextTopics.next();
                if(contextTopic != null && !contextTopic.isRemoved()) {
                    res.addAll( query.query(new QueryContext(contextTopic, wandora.getLang())) );
                }
            }

            ArrayList<Locator> columns=new ArrayList<Locator>();
            for(ResultRow row : res){
                for(int i=0;i<row.getNumValues();i++){
                    Locator l=row.getRole(i);
                    if(!columns.contains(l)) columns.add(l);
                }
            }
            ArrayList<Object> columnTopicsA=new ArrayList<Object>();
            for(int i=0;i<columns.size();i++){
                Locator l=columns.get(i);
                if(l.toString().startsWith("~")){
                    columns.remove(i);
                    i--;
                }
                else{
                    Topic t=tm.getTopic(l);
                    if(t!=null) columnTopicsA.add(t);
                    else columnTopicsA.add(l);
                }
            }
            Object[] columnTopics=columnTopicsA.toArray(new Object[columnTopicsA.size()]);
            if(res.size() > 0) {
                Object[][] data=new Object[res.size()][columns.size()];
                for(int i=0;i<res.size();i++){
                    ResultRow row=res.get(i);
                    for(int j=0;j<columns.size();j++){
                        Locator r=columns.get(j);
                        Object p=row.getValue(r);
                        if(p instanceof Locator){
                            Topic t=tm.getTopic((Locator)p);
                            if(t!=null) p=t;
                        }
                        data[i][j]=p;
                    }
                }

                MixedTopicTable table=new MixedTopicTable(wandora);
                table.initialize(data,columnTopics);
                return table;
            }
        }
        catch(ScriptException se) {
            wandora.handleError(se);
        }
        catch(Exception e) {
            //e.printStackTrace();
            wandora.handleError(e);
        }
        return null;
    }

*/


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

        searchTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchPanelInner = new javax.swing.JPanel();
        searchScrollPane = new javax.swing.JScrollPane();
        searchTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        searchTargetPanel = new javax.swing.JPanel();
        searchChecksLabel = new SimpleLabel();
        searchBasenameCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchVariantCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchOccurrenceCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchSICheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchSLCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similarityPanel = new javax.swing.JPanel();
        similarityPanelInner = new javax.swing.JPanel();
        similarityTypePanel = new javax.swing.JPanel();
        similarityTypeLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarityTypeComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        similarityTokenizerLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarityTokenizerComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        similarityThresholdPanel = new javax.swing.JPanel();
        similarityThresholdLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarityThresholdSlider = new javax.swing.JSlider();
        similarityThresholdTextField = new org.wandora.application.gui.simple.SimpleField();
        similarityDifferenceCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        gapCostPanel = new javax.swing.JPanel();
        gapCostLabel = new org.wandora.application.gui.simple.SimpleLabel();
        gapCostTextField = new org.wandora.application.gui.simple.SimpleField();
        similarityTextPanel = new javax.swing.JPanel();
        similarityTextLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarityScrollPane = new javax.swing.JScrollPane();
        similarityTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        similarityTargetPanel = new javax.swing.JPanel();
        similarityChecksLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarityBasenameCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similarityVariantCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similarityOccurrenceCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similaritySICheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similaritySLCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        jPanel1 = new javax.swing.JPanel();
        queryPanel = new javax.swing.JPanel();
        queryPanelInner = new javax.swing.JPanel();
        selectQueryPanel = new javax.swing.JPanel();
        queryComboBox = new SimpleComboBox();
        addQueryButton = new SimpleButton();
        delQueryButton = new SimpleButton();
        scriptQueryPanel = new javax.swing.JPanel();
        engineLabel = new SimpleLabel();
        engineComboBox = new SimpleComboBox();
        scriptLabel = new SimpleLabel();
        scriptScrollPane = new javax.swing.JScrollPane();
        scriptTextPane = new SimpleTextPane();
        tmqlPanel = new javax.swing.JPanel();
        selectQueryPanel1 = new javax.swing.JPanel();
        tmqlComboBox = new SimpleComboBox();
        addTmqlButton = new SimpleButton();
        delTmqlButton = new SimpleButton();
        tmqlScrollPane = new javax.swing.JScrollPane();
        tmqlTextPane = new SimpleTextPane();
        buttonPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search and query topics");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchPanelInner.setLayout(new java.awt.GridBagLayout());

        searchScrollPane.setViewportView(searchTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        searchPanelInner.add(searchScrollPane, gridBagConstraints);

        searchTargetPanel.setLayout(new java.awt.GridBagLayout());

        searchChecksLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        searchChecksLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        searchChecksLabel.setText("Search in");
        searchChecksLabel.setMaximumSize(new java.awt.Dimension(70, 14));
        searchChecksLabel.setMinimumSize(new java.awt.Dimension(70, 14));
        searchChecksLabel.setPreferredSize(new java.awt.Dimension(70, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        searchTargetPanel.add(searchChecksLabel, gridBagConstraints);

        searchBasenameCheckBox.setSelected(true);
        searchBasenameCheckBox.setText("base names");
        searchTargetPanel.add(searchBasenameCheckBox, new java.awt.GridBagConstraints());

        searchVariantCheckBox.setSelected(true);
        searchVariantCheckBox.setText("variant names");
        searchTargetPanel.add(searchVariantCheckBox, new java.awt.GridBagConstraints());

        searchOccurrenceCheckBox.setSelected(true);
        searchOccurrenceCheckBox.setText("occurrences");
        searchTargetPanel.add(searchOccurrenceCheckBox, new java.awt.GridBagConstraints());

        searchSICheckBox.setSelected(true);
        searchSICheckBox.setText("subject identifiers");
        searchTargetPanel.add(searchSICheckBox, new java.awt.GridBagConstraints());

        searchSLCheckBox.setSelected(true);
        searchSLCheckBox.setText("subject locators");
        searchTargetPanel.add(searchSLCheckBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        searchPanelInner.add(searchTargetPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        searchPanel.add(searchPanelInner, gridBagConstraints);

        searchTabbedPane.addTab("Search", searchPanel);

        similarityPanel.setLayout(new java.awt.GridBagLayout());

        similarityPanelInner.setLayout(new java.awt.GridBagLayout());

        similarityTypePanel.setMinimumSize(new java.awt.Dimension(125, 24));
        similarityTypePanel.setPreferredSize(new java.awt.Dimension(100, 24));
        similarityTypePanel.setLayout(new java.awt.GridBagLayout());

        similarityTypeLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityTypeLabel.setText("Similarity type");
        similarityTypeLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        similarityTypeLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        similarityTypeLabel.setPreferredSize(new java.awt.Dimension(120, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        similarityTypePanel.add(similarityTypeLabel, gridBagConstraints);

        similarityTypeComboBox.setPreferredSize(new java.awt.Dimension(29, 20));
        similarityTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                similarityTypeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        similarityTypePanel.add(similarityTypeComboBox, gridBagConstraints);

        similarityTokenizerLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityTokenizerLabel.setText("Tokenizer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 3);
        similarityTypePanel.add(similarityTokenizerLabel, gridBagConstraints);

        similarityTokenizerComboBox.setMinimumSize(new java.awt.Dimension(130, 20));
        similarityTokenizerComboBox.setPreferredSize(new java.awt.Dimension(130, 20));
        similarityTokenizerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                similarityTokenizerComboBoxActionPerformed(evt);
            }
        });
        similarityTypePanel.add(similarityTokenizerComboBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        similarityPanelInner.add(similarityTypePanel, gridBagConstraints);

        similarityThresholdPanel.setPreferredSize(new java.awt.Dimension(100, 24));
        similarityThresholdPanel.setLayout(new java.awt.GridBagLayout());

        similarityThresholdLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityThresholdLabel.setText("Similarity threshold");
        similarityThresholdLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        similarityThresholdLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        similarityThresholdLabel.setPreferredSize(new java.awt.Dimension(120, 14));
        similarityThresholdPanel.add(similarityThresholdLabel, new java.awt.GridBagConstraints());

        similarityThresholdSlider.setToolTipText(Textbox.makeHTMLParagraph("Similarity threshold specifies allowed difference between similar strings. When threshold is near 100, only minimal differences are allowed. When threshold is near 0, strings may be very different and they are still similar.", 40));
        similarityThresholdSlider.setMinimumSize(new java.awt.Dimension(100, 24));
        similarityThresholdSlider.setPreferredSize(new java.awt.Dimension(100, 24));
        similarityThresholdSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                similarityThresholdSliderMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                similarityThresholdSliderMouseReleased(evt);
            }
        });
        similarityThresholdSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                similarityThresholdSliderMouseDragged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        similarityThresholdPanel.add(similarityThresholdSlider, gridBagConstraints);

        similarityThresholdTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        similarityThresholdTextField.setText("50");
        similarityThresholdTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        similarityThresholdPanel.add(similarityThresholdTextField, gridBagConstraints);

        similarityDifferenceCheckBox.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityDifferenceCheckBox.setText("Difference instead similarity");
        similarityDifferenceCheckBox.setToolTipText("Results different topics instead of similar topics, if checked.");
        similarityDifferenceCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        similarityDifferenceCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        similarityThresholdPanel.add(similarityDifferenceCheckBox, new java.awt.GridBagConstraints());

        gapCostPanel.setLayout(new java.awt.GridBagLayout());

        gapCostLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        gapCostLabel.setText("Gap cost");
        gapCostLabel.setToolTipText("Some similarity metrics require gap cost in similarity calculations.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        gapCostPanel.add(gapCostLabel, gridBagConstraints);

        gapCostTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gapCostTextField.setText("0.5");
        gapCostTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        gapCostTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        gapCostPanel.add(gapCostTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        similarityThresholdPanel.add(gapCostPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        similarityPanelInner.add(similarityThresholdPanel, gridBagConstraints);

        similarityTextPanel.setLayout(new java.awt.GridBagLayout());

        similarityTextLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityTextLabel.setText("Similarity text");
        similarityTextLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        similarityTextLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        similarityTextLabel.setPreferredSize(new java.awt.Dimension(120, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        similarityTextPanel.add(similarityTextLabel, gridBagConstraints);

        similarityScrollPane.setViewportView(similarityTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        similarityTextPanel.add(similarityScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 2, 0);
        similarityPanelInner.add(similarityTextPanel, gridBagConstraints);

        similarityTargetPanel.setMinimumSize(new java.awt.Dimension(571, 24));
        similarityTargetPanel.setPreferredSize(new java.awt.Dimension(100, 24));
        similarityTargetPanel.setLayout(new java.awt.GridBagLayout());

        similarityChecksLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityChecksLabel.setText("Compare to");
        similarityChecksLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        similarityChecksLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        similarityChecksLabel.setPreferredSize(new java.awt.Dimension(120, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        similarityTargetPanel.add(similarityChecksLabel, gridBagConstraints);

        similarityBasenameCheckBox.setSelected(true);
        similarityBasenameCheckBox.setText("base names");
        similarityTargetPanel.add(similarityBasenameCheckBox, new java.awt.GridBagConstraints());

        similarityVariantCheckBox.setSelected(true);
        similarityVariantCheckBox.setText("variant names");
        similarityTargetPanel.add(similarityVariantCheckBox, new java.awt.GridBagConstraints());

        similarityOccurrenceCheckBox.setText("occurrences");
        similarityTargetPanel.add(similarityOccurrenceCheckBox, new java.awt.GridBagConstraints());

        similaritySICheckBox.setText("subject identifiers");
        similarityTargetPanel.add(similaritySICheckBox, new java.awt.GridBagConstraints());

        similaritySLCheckBox.setText("subject locators");
        similarityTargetPanel.add(similaritySLCheckBox, new java.awt.GridBagConstraints());

        jPanel1.setPreferredSize(new java.awt.Dimension(4, 4));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 156, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        similarityTargetPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        similarityPanelInner.add(similarityTargetPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        similarityPanel.add(similarityPanelInner, gridBagConstraints);

        searchTabbedPane.addTab("Similarity", similarityPanel);

        queryPanel.setLayout(new java.awt.GridBagLayout());

        queryPanelInner.setLayout(new java.awt.GridBagLayout());

        selectQueryPanel.setLayout(new java.awt.GridBagLayout());

        queryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        queryComboBox.setPreferredSize(new java.awt.Dimension(57, 21));
        queryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        selectQueryPanel.add(queryComboBox, gridBagConstraints);

        addQueryButton.setText("Add");
        addQueryButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        addQueryButton.setPreferredSize(new java.awt.Dimension(50, 21));
        addQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addQueryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        selectQueryPanel.add(addQueryButton, gridBagConstraints);

        delQueryButton.setText("Del");
        delQueryButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        delQueryButton.setPreferredSize(new java.awt.Dimension(50, 21));
        delQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delQueryButtonActionPerformed(evt);
            }
        });
        selectQueryPanel.add(delQueryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        queryPanelInner.add(selectQueryPanel, gridBagConstraints);

        scriptQueryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        scriptQueryPanel.setLayout(new java.awt.GridBagLayout());

        engineLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        engineLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        engineLabel.setText("Engine");
        engineLabel.setPreferredSize(new java.awt.Dimension(70, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 4);
        scriptQueryPanel.add(engineLabel, gridBagConstraints);

        engineComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        engineComboBox.setPreferredSize(new java.awt.Dimension(57, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 2);
        scriptQueryPanel.add(engineComboBox, gridBagConstraints);

        scriptLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        scriptLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        scriptLabel.setIcon(org.wandora.application.gui.UIBox.getIcon("resources/gui/icons/help_in_context.png"));
        scriptLabel.setText("Script");
        scriptLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        scriptLabel.setIconTextGap(0);
        scriptLabel.setPreferredSize(new java.awt.Dimension(70, 14));
        scriptLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                scriptLabelMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 2, 4);
        scriptQueryPanel.add(scriptLabel, gridBagConstraints);

        scriptScrollPane.setViewportView(scriptTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        scriptQueryPanel.add(scriptScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        queryPanelInner.add(scriptQueryPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        queryPanel.add(queryPanelInner, gridBagConstraints);

        searchTabbedPane.addTab("Query", queryPanel);

        tmqlPanel.setName(""); // NOI18N
        tmqlPanel.setLayout(new java.awt.GridBagLayout());

        selectQueryPanel1.setLayout(new java.awt.GridBagLayout());

        tmqlComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        tmqlComboBox.setPreferredSize(new java.awt.Dimension(57, 21));
        tmqlComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tmqlComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        selectQueryPanel1.add(tmqlComboBox, gridBagConstraints);

        addTmqlButton.setText("Add");
        addTmqlButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        addTmqlButton.setPreferredSize(new java.awt.Dimension(50, 21));
        addTmqlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTmqlButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        selectQueryPanel1.add(addTmqlButton, gridBagConstraints);

        delTmqlButton.setText("Del");
        delTmqlButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        delTmqlButton.setPreferredSize(new java.awt.Dimension(50, 21));
        delTmqlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delTmqlButtonActionPerformed(evt);
            }
        });
        selectQueryPanel1.add(delTmqlButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        tmqlPanel.add(selectQueryPanel1, gridBagConstraints);

        tmqlScrollPane.setViewportView(tmqlTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        tmqlPanel.add(tmqlScrollPane, gridBagConstraints);

        searchTabbedPane.addTab("TMQL", tmqlPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(searchTabbedPane, gridBagConstraints);
        searchTabbedPane.getAccessibleContext().setAccessibleName("searchTabbed");

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        okButton.setText("Search");
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

        cancelButton.setText("Cancel");
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void similarityThresholdSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarityThresholdSliderMouseDragged
    updateSimilarityThreshold();
}//GEN-LAST:event_similarityThresholdSliderMouseDragged

private void similarityThresholdSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarityThresholdSliderMouseReleased
    updateSimilarityThreshold();
}//GEN-LAST:event_similarityThresholdSliderMouseReleased

private void similarityThresholdSliderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarityThresholdSliderMousePressed
    updateSimilarityThreshold();
}//GEN-LAST:event_similarityThresholdSliderMousePressed

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    this.accept = true;
    this.setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    this.accept = false;
    this.setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void similarityTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_similarityTypeComboBoxActionPerformed
    if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
        updateSimilarityOptions();
    }
}//GEN-LAST:event_similarityTypeComboBoxActionPerformed

private void similarityTokenizerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_similarityTokenizerComboBoxActionPerformed
    if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
        updateTokenizerOptions();
    }
}//GEN-LAST:event_similarityTokenizerComboBoxActionPerformed

private void queryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryComboBoxActionPerformed
    if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
        selectScriptQuery();
    }
}//GEN-LAST:event_queryComboBoxActionPerformed

private void addQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addQueryButtonActionPerformed
    if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
        addScriptQuery();
    }
}//GEN-LAST:event_addQueryButtonActionPerformed

private void delQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delQueryButtonActionPerformed
    if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
        deleteScriptQuery();
    }
}//GEN-LAST:event_delQueryButtonActionPerformed

private void scriptLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scriptLabelMouseReleased
    try {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI("http://wandora.org/wiki/Query_language"));
    }
    catch(Exception e) {
        e.printStackTrace();
    }
}//GEN-LAST:event_scriptLabelMouseReleased

    private void tmqlComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tmqlComboBoxActionPerformed
        if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
            selectTmqlQuery();
        }
    }//GEN-LAST:event_tmqlComboBoxActionPerformed

    private void addTmqlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTmqlButtonActionPerformed
        if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
            addTmqlQuery();
        }
    }//GEN-LAST:event_addTmqlButtonActionPerformed

    private void delTmqlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTmqlButtonActionPerformed
        if((evt.getModifiers() | evt.MOUSE_EVENT_MASK) != 0) {
            deleteTmqlQuery();
        }
    }//GEN-LAST:event_delTmqlButtonActionPerformed





    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addQueryButton;
    private javax.swing.JButton addTmqlButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton delQueryButton;
    private javax.swing.JButton delTmqlButton;
    private javax.swing.JComboBox engineComboBox;
    private javax.swing.JLabel engineLabel;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel gapCostLabel;
    private javax.swing.JPanel gapCostPanel;
    private javax.swing.JTextField gapCostTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox queryComboBox;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JPanel queryPanelInner;
    private javax.swing.JLabel scriptLabel;
    private javax.swing.JPanel scriptQueryPanel;
    private javax.swing.JScrollPane scriptScrollPane;
    private javax.swing.JTextPane scriptTextPane;
    private javax.swing.JCheckBox searchBasenameCheckBox;
    private javax.swing.JLabel searchChecksLabel;
    private javax.swing.JCheckBox searchOccurrenceCheckBox;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel searchPanelInner;
    private javax.swing.JCheckBox searchSICheckBox;
    private javax.swing.JCheckBox searchSLCheckBox;
    private javax.swing.JScrollPane searchScrollPane;
    private javax.swing.JTabbedPane searchTabbedPane;
    private javax.swing.JPanel searchTargetPanel;
    private javax.swing.JTextPane searchTextPane;
    private javax.swing.JCheckBox searchVariantCheckBox;
    private javax.swing.JPanel selectQueryPanel;
    private javax.swing.JPanel selectQueryPanel1;
    private javax.swing.JCheckBox similarityBasenameCheckBox;
    private javax.swing.JLabel similarityChecksLabel;
    private javax.swing.JCheckBox similarityDifferenceCheckBox;
    private javax.swing.JCheckBox similarityOccurrenceCheckBox;
    private javax.swing.JPanel similarityPanel;
    private javax.swing.JPanel similarityPanelInner;
    private javax.swing.JCheckBox similaritySICheckBox;
    private javax.swing.JCheckBox similaritySLCheckBox;
    private javax.swing.JScrollPane similarityScrollPane;
    private javax.swing.JPanel similarityTargetPanel;
    private javax.swing.JLabel similarityTextLabel;
    private javax.swing.JTextPane similarityTextPane;
    private javax.swing.JPanel similarityTextPanel;
    private javax.swing.JLabel similarityThresholdLabel;
    private javax.swing.JPanel similarityThresholdPanel;
    private javax.swing.JSlider similarityThresholdSlider;
    private javax.swing.JTextField similarityThresholdTextField;
    private javax.swing.JComboBox similarityTokenizerComboBox;
    private javax.swing.JLabel similarityTokenizerLabel;
    private javax.swing.JComboBox similarityTypeComboBox;
    private javax.swing.JLabel similarityTypeLabel;
    private javax.swing.JPanel similarityTypePanel;
    private javax.swing.JCheckBox similarityVariantCheckBox;
    private javax.swing.JComboBox tmqlComboBox;
    private javax.swing.JPanel tmqlPanel;
    private javax.swing.JScrollPane tmqlScrollPane;
    private javax.swing.JTextPane tmqlTextPane;
    // End of variables declaration//GEN-END:variables

}
