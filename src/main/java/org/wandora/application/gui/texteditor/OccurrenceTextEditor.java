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
 * OccurrenceTextEditor.java
 *
 * Created on 5. toukokuuta 2006, 20:29
 */


package org.wandora.application.gui.texteditor;


import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.tools.FindTopicsWithSimilarOccurrence;
import org.wandora.application.tools.associations.FindAssociationsInOccurrenceSimple;
import org.wandora.application.tools.extractors.gate.AnnieExtractor;
import org.wandora.application.tools.extractors.stanfordner.StanfordNERClassifier;
import org.wandora.application.tools.extractors.tagthe.TagtheExtractor;
import org.wandora.application.tools.extractors.textwise.TextWiseClassifier;
import org.wandora.application.tools.extractors.uclassify.SentimentUClassifier;
import org.wandora.application.tools.extractors.uclassify.TextLanguageUClassifier;
import org.wandora.application.tools.extractors.uclassify.TopicsUClassifier;
import org.wandora.application.tools.extractors.uclassify.UClassifier;
import org.wandora.application.tools.extractors.yahoo.yql.SearchTermExtract;
import org.wandora.application.tools.occurrences.CreateTopicWithOccurrenceSelection;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;




/**
 *
 * @author akivela
 */
public class OccurrenceTextEditor extends TextEditor {

    private static final long serialVersionUID = 1L;
    
    protected Topic occurrenceTopic = null;
    protected Topic occurrenceType = null;
    protected Topic occurrenceVersion = null;
    
    
    
    public OccurrenceTextEditor(Wandora wandora, boolean modal, String initText, Topic occurrenceTopic, Topic type, Topic version) {
        this(wandora,modal,initText,null,occurrenceTopic,type,version);
    }
    public OccurrenceTextEditor(Wandora wandora, boolean modal, String initText) {
        this(wandora,modal,initText,null);
    }
    public OccurrenceTextEditor(Wandora wandora, boolean modal, String initText, String contentType) {
        super(wandora, modal, initText, contentType);
        infoLabel.setText("Editing occurrence");
    }
    public OccurrenceTextEditor(Wandora wandora, boolean modal, String initText, String contentType, Topic occurrenceTopic, Topic type, Topic version) {
        super(wandora, modal, initText, contentType);
        this.occurrenceTopic = occurrenceTopic;
        this.occurrenceType = type;
        this.occurrenceVersion = version;
        infoLabel.setText("Editing occurrence");
    }
    
    
    
    @Override
    public JMenu[] getUserMenus() {
        JMenu processMenu = new SimpleMenu("Refine", (Icon) null);
        Object[] menuStruct = new Object[] {
            "Make topics", new Object[] {
                "Make topic with selection", new CreateTopicWithOccurrenceSelection(false),
                "Make topic with selection and associate", new CreateTopicWithOccurrenceSelection(true), KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK)
            },
            "Find topics", new Object[] {
                "Find topics in occurrences...", new FindAssociationsInOccurrenceSimple(), KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK),
                "Find topics with similar occurrences...", new FindTopicsWithSimilarOccurrence(),
            },
            "Classify", new Object[] {
                "Classify with GATE Annie", new AnnieExtractor(), UIBox.getIcon("gui/icons/extract_gate.png"),
                "Classify with Stanford NER", new StanfordNERClassifier(), UIBox.getIcon("gui/icons/extract_stanford_ner.png"),
                "Classify with TextWise", new TextWiseClassifier(), UIBox.getIcon("gui/icons/extract_textwise.png"),
                "Classify with Yahoo YQL term extractor", new SearchTermExtract(), UIBox.getIcon("gui/icons/extract_yahoo.png"),
                "Classify with Tagthe", new TagtheExtractor(), UIBox.getIcon("gui/icons/extract_tagthe.png"),
            },
            "uClassify", new Object[] {
                "uClassify Text Language", new TextLanguageUClassifier(), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Sentiment", new SentimentUClassifier(), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Topics", new TopicsUClassifier(), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Mood", new UClassifier("Mood", "prfekt", 0.0001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify GenderAnalyzer_v5", new UClassifier("GenderAnalyzer_v5", "uClassify", 0.0001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Ageanalyzer", new UClassifier("Ageanalyzer", "uClassify", 0.0001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
            },
            "Insert", new Object[] {
                "Insert base name", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK),
                "Insert variant name", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK | java.awt.event.InputEvent.SHIFT_MASK),
                "Insert subject identifier", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK | java.awt.event.InputEvent.ALT_MASK),
                "Insert subject locator",KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK | java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK),
            }
        };
        return new JMenu[] { UIBox.attachMenu(processMenu, menuStruct, this) };
    }

    
    
    
    /** Creates new form OccurrenceTextEditor */
    public OccurrenceTextEditor(Wandora wandora, boolean modal) {
        super(wandora, modal);
    }
    
    
    
    public Topic getOccurrenceTopic() {
        return occurrenceTopic;
    }
    
    public Topic getOccurrenceType() {
        return occurrenceType;
    }
    
    public Topic getOccurrenceVersion() {
        return occurrenceVersion;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if(actionEvent == null) return;
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        try {
            if("Insert base name".equalsIgnoreCase(c)) {
                if(occurrenceTopic != null && !occurrenceTopic.isRemoved()) {
                    String bn = occurrenceTopic.getBaseName();
                    if(bn != null) {
                        simpleTextPane.insertText(bn);
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(this, "Base name is null and can not be inserted to the occurrence text.", "Base name is null", WandoraOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(this, "Invalid occurrence carrier topic.", "Invalid occurrence carrier topic.", WandoraOptionPane.ERROR_MESSAGE);
                }
            }
            
            else if("Insert variant name".equalsIgnoreCase(c)) {
                if(occurrenceTopic != null && !occurrenceTopic.isRemoved()) {
                    Set<Topic> scope = new HashSet<>();
                    scope.add(this.occurrenceVersion);
                    scope.add(TMBox.getDisplayNameTopic(occurrenceType));
                    String variantName = occurrenceTopic.getVariant(scope);
                    if(variantName == null) variantName = occurrenceTopic.getDisplayName();
                    if(variantName != null) {
                        simpleTextPane.insertText(variantName);
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(this, "No variant names found for insertion.", "No variant names found", WandoraOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(this, "Invalid occurrence carrier topic.", "Invalid occurrence carrier topic.", WandoraOptionPane.ERROR_MESSAGE);
                }
            }
            else if("Insert subject identifier".equalsIgnoreCase(c)) {
                if(occurrenceTopic != null && !occurrenceTopic.isRemoved()) {
                    String si = occurrenceTopic.getOneSubjectIdentifier().toExternalForm();
                    if(si != null) {
                        simpleTextPane.insertText(si);
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(this, "No subject identifiers found for insertion. Invalid topic as occurrence carrier.", "No subject identifier found", WandoraOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(this, "Invalid occurrence carrier topic.", "Invalid occurrence carrier topic.", WandoraOptionPane.ERROR_MESSAGE);
                }
            }
            else if("Insert subject locator".equalsIgnoreCase(c)) {
                if(occurrenceTopic != null && !occurrenceTopic.isRemoved()) {
                    Locator sl = occurrenceTopic.getSubjectLocator();
                    if(sl != null) {
                        String sls = sl.toExternalForm();
                        simpleTextPane.insertText(sls);
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(this, "No subject locator found for insertion.", "No subject locator found", WandoraOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(this, "Invalid occurrence carrier topic.", "Invalid occurrence carrier topic.", WandoraOptionPane.ERROR_MESSAGE);
                }
            }

            else {
                super.actionPerformed(actionEvent);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
