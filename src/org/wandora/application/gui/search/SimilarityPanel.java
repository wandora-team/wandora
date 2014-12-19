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
 */



package org.wandora.application.gui.search;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapSearchOptions;
import org.wandora.utils.Textbox;
import org.wandora.utils.Tuples;
import uk.ac.shef.wit.simmetrics.similaritymetrics.BlockDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MatchingCoefficient;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;
import uk.ac.shef.wit.simmetrics.similaritymetrics.OverlapCoefficient;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex;
import uk.ac.shef.wit.simmetrics.tokenisers.InterfaceTokeniser;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserCSVBasic;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserQGram2;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserQGram2Extended;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserQGram3;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserQGram3Extended;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserWhitespace;

/**
 *
 * @author akivela
 */


public class SimilarityPanel extends javax.swing.JPanel {



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

    
    
    public Tuples.T2[] similarityTypes = {
        new Tuples.T2("Levenshtein distance",                  new Integer(SIMILARITY_LEVENSHTEIN_DISTANCE)),
        new Tuples.T2("Needleman-Wunch distance (Sellers Algorithm)", new Integer(SIMILARITY_NEEDLEMAN_WUNCH_DISTANCE)),
        new Tuples.T2("Smith-Waterman distance",               new Integer(SIMILARITY_SMITH_WATERMAN_DISTANCE)),
        new Tuples.T2("Block distance (L1 distance or City block distance)", new Integer(SIMILARITY_BLOCK_DISTANCE)),
        new Tuples.T2("Monge Elkan distance",                  new Integer(SIMILARITY_MONGE_ELKAN_DISTANCE)),
        new Tuples.T2("Jaro distance metric",                  new Integer(SIMILARITY_JARO_DISTANCE_METRIC)),
        new Tuples.T2("Jaro Winkler",                          new Integer(SIMILARITY_JARO_WINKLER)),
        new Tuples.T2("SoundEx distance metric",               new Integer(SIMILARITY_SOUNDEX_DISTANCE_METRIC)),
        new Tuples.T2("Matching Coefficient",                  new Integer(SIMILARITY_MATCHING_COEFFICIENT)),
        new Tuples.T2("Dice's Coefficient",                    new Integer(SIMILARITY_DICES_COEFFICIENT)),
        new Tuples.T2("Jaccard Similarity (Jaccard Coefficient or Tanimoto coefficient)", new Integer(SIMILARITY_OVERLAP_COEFFICIENT)),
        new Tuples.T2("Overlap Coefficient",                   new Integer(SIMILARITY_JACCARD_SIMILARITY)),
        new Tuples.T2("Euclidean distance (L2 distance)",      new Integer(SIMILARITY_EUCLIDEAN_DISTANCE)),
        new Tuples.T2("Cosine similarity",                     new Integer(SIMILARITY_COSINE_SIMILARITY)),
        new Tuples.T2("q-gram",                                new Integer(SIMILARITY_Q_GRAM)),
    };
    
    
    public Tuples.T2[] similarityTokenizers = {
        new Tuples.T2("Whitespace", new TokeniserWhitespace()),
        new Tuples.T2("CSVBasic", new TokeniserCSVBasic()),
        new Tuples.T2("QGram2", new TokeniserQGram2()),
        new Tuples.T2("QGram2 extended", new TokeniserQGram2Extended()),
        new Tuples.T2("QGram3", new TokeniserQGram3()),
        new Tuples.T2("QGram3 extended", new TokeniserQGram3Extended())
    };
    
    
    
    /**
     * Creates new form SimilarityPanel
     */
    public SimilarityPanel() {
        initComponents();
        
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
    }

    

    public String getSimilarityQuery() {
        return similarityTextPane.getText();
    }
    
    public int getSimilarityType() {
        Object type = similarityTypeComboBox.getSelectedItem();
        if(type != null) {
            Object t = null;
            for(Tuples.T2 similarityType : similarityTypes) {
                if(type.equals(similarityType.e1)) {
                    t = similarityType.e2;
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
            for(Tuples.T2 similarityTokenizer : similarityTokenizers) {
                if(tokenizer.equals(similarityTokenizer.e1)) {
                    t = similarityTokenizer.e2;
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
    
    
    
    
    public Collection<Topic> getSimilarTopics(TopicMap tm) {
        if(tm != null) {
            try {
                int similarityType = getSimilarityType();
                float threshold = getSimilarityThreshold();
                Iterator<Topic> iterator = tm.getTopics();
                String query = getSimilarityQuery();
                TopicMapSearchOptions options = getSimilarityOptions();
                boolean differenceInsteadOfSimilarity = similarityDifferenceCheckBox.isSelected();
                return getSimilarTopics(query, options, iterator, similarityType, threshold, differenceInsteadOfSimilarity);
            }
            catch(Exception e) {
                Wandora wandora = Wandora.getWandora();
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
    
    
    
    
    public Collection<Topic> getSimilarTopics(String query, TopicMapSearchOptions options, Iterator<Topic> topicIterator, int similarityType, float threshold, boolean differenceInsteadOfSimilarity) {
        InterfaceStringMetric stringMetric = getStringMetric(similarityType);
        if(stringMetric != null) {
            return getSimilarTopics(query, options, topicIterator, stringMetric, threshold, differenceInsteadOfSimilarity);
        }
        return new ArrayList<Topic>();
    }
    
    
    
    
    
    
    public Collection<Topic> getSimilarTopics(String query, TopicMapSearchOptions options, Iterator<Topic> topicIterator, InterfaceStringMetric stringMetric, float threshold, boolean differenceInsteadOfSimilarity) {
        ArrayList<Topic> selection = new ArrayList<Topic>();
        int count = 0;
        Topic t = null;
        boolean isSimilar = false;
        float similarity = 0.0f;
        try {
            while(topicIterator.hasNext()) {
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
    
    
    
    private void updateSimilarityOptions() {
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


    
    

    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        containerPanel = new javax.swing.JPanel();
        similarityPanel = new javax.swing.JPanel();
        similarityPanelInner = new javax.swing.JPanel();
        similarityTypePanel = new javax.swing.JPanel();
        similarityTypeComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        similarityTextPanel = new javax.swing.JPanel();
        similarityScrollPane = new javax.swing.JScrollPane();
        similarityTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        optionsTabbedPane = new SimpleTabbedPane();
        thresholdPanel = new javax.swing.JPanel();
        thresholdInnerPanel = new javax.swing.JPanel();
        similarityThresholdSlider = new javax.swing.JSlider();
        similarityThresholdTextField = new org.wandora.application.gui.simple.SimpleField();
        similarityDifferenceCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similarityTokenizerPanel = new javax.swing.JPanel();
        similarityTokenizerLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarityTokenizerComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        similaritygapCostPanel = new javax.swing.JPanel();
        gapCostLabel = new org.wandora.application.gui.simple.SimpleLabel();
        gapCostTextField = new org.wandora.application.gui.simple.SimpleField();
        similarityTargetPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        similarityBasenameCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similarityVariantCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similarityOccurrenceCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        jPanel2 = new javax.swing.JPanel();
        similaritySICheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        similaritySLCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        runButtonPanel = new javax.swing.JPanel();
        searchButton = new SimpleButton();
        resultPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        containerPanel.setLayout(new java.awt.GridBagLayout());

        similarityPanel.setLayout(new java.awt.GridBagLayout());

        similarityPanelInner.setLayout(new java.awt.GridBagLayout());

        similarityTypePanel.setLayout(new java.awt.GridBagLayout());

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        similarityPanelInner.add(similarityTypePanel, gridBagConstraints);

        similarityTextPanel.setLayout(new java.awt.GridBagLayout());

        similarityScrollPane.setPreferredSize(new java.awt.Dimension(8, 50));
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 2, 0);
        similarityPanelInner.add(similarityTextPanel, gridBagConstraints);

        thresholdPanel.setLayout(new java.awt.GridBagLayout());

        thresholdInnerPanel.setLayout(new java.awt.GridBagLayout());

        similarityThresholdSlider.setToolTipText(Textbox.makeHTMLParagraph("Similarity threshold specifies allowed difference between similar strings. When threshold is near 100, only minimal differences are allowed. When threshold is near 0, strings may be very different and they are still similar.", 40));
        similarityThresholdSlider.setMinimumSize(new java.awt.Dimension(100, 24));
        similarityThresholdSlider.setPreferredSize(new java.awt.Dimension(200, 24));
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
        thresholdInnerPanel.add(similarityThresholdSlider, new java.awt.GridBagConstraints());

        similarityThresholdTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        similarityThresholdTextField.setText("50");
        similarityThresholdTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        similarityThresholdTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        thresholdInnerPanel.add(similarityThresholdTextField, gridBagConstraints);

        similarityDifferenceCheckBox.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityDifferenceCheckBox.setText("Difference instead similarity");
        similarityDifferenceCheckBox.setToolTipText("Results different topics instead of similar topics, if checked.");
        similarityDifferenceCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        similarityDifferenceCheckBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        thresholdInnerPanel.add(similarityDifferenceCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 0);
        thresholdPanel.add(thresholdInnerPanel, gridBagConstraints);

        optionsTabbedPane.addTab("Threshold", thresholdPanel);

        similarityTokenizerPanel.setLayout(new java.awt.GridBagLayout());

        similarityTokenizerLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        similarityTokenizerLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        similarityTokenizerLabel.setText("Tokenizer");
        similarityTokenizerLabel.setMaximumSize(new java.awt.Dimension(80, 14));
        similarityTokenizerLabel.setMinimumSize(new java.awt.Dimension(80, 14));
        similarityTokenizerLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        similarityTokenizerPanel.add(similarityTokenizerLabel, gridBagConstraints);

        similarityTokenizerComboBox.setMinimumSize(new java.awt.Dimension(130, 20));
        similarityTokenizerComboBox.setPreferredSize(new java.awt.Dimension(130, 20));
        similarityTokenizerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                similarityTokenizerComboBoxActionPerformed(evt);
            }
        });
        similarityTokenizerPanel.add(similarityTokenizerComboBox, new java.awt.GridBagConstraints());

        similaritygapCostPanel.setLayout(new java.awt.GridBagLayout());

        gapCostLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        gapCostLabel.setText("Gap cost");
        gapCostLabel.setToolTipText("Some similarity metrics require gap cost in similarity calculations.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        similaritygapCostPanel.add(gapCostLabel, gridBagConstraints);

        gapCostTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gapCostTextField.setText("0.5");
        gapCostTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        gapCostTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        similaritygapCostPanel.add(gapCostTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        similarityTokenizerPanel.add(similaritygapCostPanel, gridBagConstraints);

        optionsTabbedPane.addTab("Tokenizer", similarityTokenizerPanel);

        similarityTargetPanel.setMinimumSize(new java.awt.Dimension(571, 24));
        similarityTargetPanel.setPreferredSize(new java.awt.Dimension(100, 24));
        similarityTargetPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        similarityBasenameCheckBox.setSelected(true);
        similarityBasenameCheckBox.setText("base names");
        jPanel1.add(similarityBasenameCheckBox, new java.awt.GridBagConstraints());

        similarityVariantCheckBox.setSelected(true);
        similarityVariantCheckBox.setText("variant names");
        jPanel1.add(similarityVariantCheckBox, new java.awt.GridBagConstraints());

        similarityOccurrenceCheckBox.setText("occurrences");
        jPanel1.add(similarityOccurrenceCheckBox, new java.awt.GridBagConstraints());

        similarityTargetPanel.add(jPanel1, new java.awt.GridBagConstraints());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        similaritySICheckBox.setText("subject identifiers");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        jPanel2.add(similaritySICheckBox, gridBagConstraints);

        similaritySLCheckBox.setText("subject locators");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        jPanel2.add(similaritySLCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        similarityTargetPanel.add(jPanel2, gridBagConstraints);

        optionsTabbedPane.addTab("Compare to", similarityTargetPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        similarityPanelInner.add(optionsTabbedPane, gridBagConstraints);
        optionsTabbedPane.getAccessibleContext().setAccessibleName("tab2");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        similarityPanel.add(similarityPanelInner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        containerPanel.add(similarityPanel, gridBagConstraints);

        runButtonPanel.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        runButtonPanel.setMinimumSize(new java.awt.Dimension(92, 40));
        runButtonPanel.setPreferredSize(new java.awt.Dimension(10, 40));
        runButtonPanel.setLayout(new java.awt.GridBagLayout());

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 13, 39, 14);
        runButtonPanel.add(searchButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        containerPanel.add(runButtonPanel, gridBagConstraints);

        resultPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        containerPanel.add(resultPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(containerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void similarityTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_similarityTypeComboBoxActionPerformed
        if((evt.getModifiers() | ActionEvent.MOUSE_EVENT_MASK) != 0) {
            updateSimilarityOptions();
        }
    }//GEN-LAST:event_similarityTypeComboBoxActionPerformed

    private void similarityTokenizerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_similarityTokenizerComboBoxActionPerformed
        if((evt.getModifiers() | ActionEvent.MOUSE_EVENT_MASK) != 0) {
            updateTokenizerOptions();
        }
    }//GEN-LAST:event_similarityTokenizerComboBoxActionPerformed

    private void similarityThresholdSliderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarityThresholdSliderMousePressed
        updateSimilarityThreshold();
    }//GEN-LAST:event_similarityThresholdSliderMousePressed

    private void similarityThresholdSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarityThresholdSliderMouseReleased
        updateSimilarityThreshold();
    }//GEN-LAST:event_similarityThresholdSliderMouseReleased

    private void similarityThresholdSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarityThresholdSliderMouseDragged
        updateSimilarityThreshold();
    }//GEN-LAST:event_similarityThresholdSliderMouseDragged

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        try {
            Wandora wandora = Wandora.getWandora();
            TopicMap topicMap = wandora.getTopicMap();
            Collection<Topic> results = getSimilarTopics(topicMap);
            resultPanel.removeAll();
            if(results != null && !results.isEmpty()) {
                TopicTable resultsTable = new TopicTable(wandora);
                resultsTable.initialize(results);
                resultPanel.add(resultsTable, BorderLayout.NORTH);
            }
            else {
                SimpleLabel message = new SimpleLabel();
                message.setText("No search results.");
                resultPanel.add(message, BorderLayout.NORTH);
            }
            revalidate();
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel gapCostLabel;
    private javax.swing.JTextField gapCostTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane optionsTabbedPane;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JPanel runButtonPanel;
    private javax.swing.JButton searchButton;
    private javax.swing.JCheckBox similarityBasenameCheckBox;
    private javax.swing.JCheckBox similarityDifferenceCheckBox;
    private javax.swing.JCheckBox similarityOccurrenceCheckBox;
    private javax.swing.JPanel similarityPanel;
    private javax.swing.JPanel similarityPanelInner;
    private javax.swing.JCheckBox similaritySICheckBox;
    private javax.swing.JCheckBox similaritySLCheckBox;
    private javax.swing.JScrollPane similarityScrollPane;
    private javax.swing.JPanel similarityTargetPanel;
    private javax.swing.JTextPane similarityTextPane;
    private javax.swing.JPanel similarityTextPanel;
    private javax.swing.JSlider similarityThresholdSlider;
    private javax.swing.JTextField similarityThresholdTextField;
    private javax.swing.JComboBox similarityTokenizerComboBox;
    private javax.swing.JLabel similarityTokenizerLabel;
    private javax.swing.JPanel similarityTokenizerPanel;
    private javax.swing.JComboBox similarityTypeComboBox;
    private javax.swing.JPanel similarityTypePanel;
    private javax.swing.JCheckBox similarityVariantCheckBox;
    private javax.swing.JPanel similaritygapCostPanel;
    private javax.swing.JPanel thresholdInnerPanel;
    private javax.swing.JPanel thresholdPanel;
    // End of variables declaration//GEN-END:variables
}
