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
 * OccurrenceTextEditor.java
 *
 * Created on 5. toukokuuta 2006, 20:29
 */


package org.wandora.application.gui.texteditor;

import org.wandora.application.gui.texteditor.TextEditor;
import com.google.api.translate.Language;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import org.wandora.application.*;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import org.wandora.application.tools.associations.FindAssociationsInOccurrenceSimple;
import org.wandora.application.tools.extractors.alchemy.*;
import org.wandora.application.tools.extractors.bing.*;
import org.wandora.application.tools.extractors.gate.AnnieExtractor;
import org.wandora.application.tools.extractors.opencalais.*;
import org.wandora.application.tools.extractors.stanfordner.StanfordNERClassifier;
import org.wandora.application.tools.extractors.tagthe.TagtheExtractor;
import org.wandora.application.tools.extractors.textwise.TextWiseClassifier;
import org.wandora.application.tools.extractors.uclassify.SentimentUClassifier;
import org.wandora.application.tools.extractors.uclassify.TextLanguageUClassifier;
import org.wandora.application.tools.extractors.uclassify.TopicsUClassifier;
import org.wandora.application.tools.extractors.uclassify.UClassifier;
import org.wandora.application.tools.extractors.yahoo.yql.SearchTermExtract;
import org.wandora.application.tools.extractors.zemanta.ZemantaExtractor;

import org.wandora.application.tools.occurrences.*;
import org.wandora.topicmap.*;
import org.wandora.utils.language.GoogleTranslateBox;
import org.wandora.utils.language.MicrosoftTranslateBox;
import org.wandora.utils.language.SelectGoogleTranslationLanguagesPanel;
import org.wandora.utils.language.SelectMicrosoftTranslationLanguagesPanel;
import org.wandora.utils.language.SelectWatsonTranslationLanguagesPanel;
import org.wandora.utils.language.WatsonTranslateBox;


/**
 *
 * @author akivela
 */
public class OccurrenceTextEditor extends TextEditor {

    protected Topic occurrenceTopic = null;
    protected Topic occurrenceType = null;
    protected Topic occurrenceVersion = null;
    
    
    
    public OccurrenceTextEditor(Wandora admin, boolean modal, String initText, Topic occurrenceTopic, Topic type, Topic version) {
        this(admin,modal,initText,null,occurrenceTopic,type,version);
    }
    public OccurrenceTextEditor(Wandora admin, boolean modal, String initText) {
        this(admin,modal,initText,null);
    }
    public OccurrenceTextEditor(Wandora admin, boolean modal, String initText, String contentType) {
        super(admin, modal, initText, contentType);
        infoLabel.setText("Editing occurrence");
    }
    public OccurrenceTextEditor(Wandora admin, boolean modal, String initText, String contentType, Topic occurrenceTopic, Topic type, Topic version) {
        super(admin, modal, initText, contentType);
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
                "Classify with OpenCalais", new OpenCalaisClassifier(), UIBox.getIcon("gui/icons/extract_opencalais.png"),
                "Classify with TextWise", new TextWiseClassifier(), UIBox.getIcon("gui/icons/extract_textwise.png"),
                "Classify with Yahoo YQL term extractor", new SearchTermExtract(), UIBox.getIcon("gui/icons/extract_yahoo.png"),
                "Classify with Tagthe", new TagtheExtractor(), UIBox.getIcon("gui/icons/extract_tagthe.png"),
                "Classify with Zemanta", new ZemantaExtractor(), UIBox.getIcon("gui/icons/extract_zemanta.png"),
                "Classify with Bing search engine...", new BingSearchExtractor(), UIBox.getIcon("gui/icons/extract_bing.png"),
            },
            "uClassify", new Object[] {
                "uClassify Text Language", new TextLanguageUClassifier(), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Sentiment", new SentimentUClassifier(), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Topics", new TopicsUClassifier(), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Mood", new UClassifier("Mood", "prfekt", 0.0001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify GenderAnalyzer_v5", new UClassifier("GenderAnalyzer_v5", "uClassify", 0.0001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                "uClassify Ageanalyzer", new UClassifier("Ageanalyzer", "uClassify", 0.0001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
            },
            "Alchemy", new Object[] {
                "Alchemy entity extractor", new AlchemyEntityExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                "Alchemy keyword extractor", new AlchemyKeywordExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                "Alchemy category extractor", new AlchemyCategoryExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                "Alchemy language extractor", new AlchemyLanguageExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                "Alchemy sentiment extractor", new AlchemySentimentExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                "Alchemy relation extractor", new AlchemyRelationExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
            },
            "Translate", new Object[] {
                "Translate with Google...", UIBox.getIcon("gui/icons/google_translate.png"),
                "Translate with Microsoft...", UIBox.getIcon("gui/icons/microsoft_translate.png"),
                "Translate with Watson...", UIBox.getIcon("gui/icons/watson_translate.png"),
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
    public OccurrenceTextEditor(Wandora admin, boolean modal) {
        super(admin, modal);
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
                    Set<Topic> scope = new HashSet<Topic>();
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
            
            else if(c.startsWith("Translate with Google")) {
                simpleTextPane.translateWithGoogle();
            }
            
            else if(c.startsWith("Translate with Microsoft")) {
                simpleTextPane.translateWithMicrosoft();
            }
            
            else if(c.startsWith("Translate with Watson")) {
                simpleTextPane.translateWithWatson();
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
