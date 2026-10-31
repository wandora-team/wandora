/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 * LayeredTopicContext.java
 *
 * Created on 7. huhtikuuta 2006, 13:47
 *
 */

package org.wandora.application.contexts;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.JTableHeader;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.LayerTree;
import org.wandora.application.gui.OccurrenceTable;
import org.wandora.application.gui.simple.TopicLinkBasename;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import org.wandora.application.gui.topicpanels.GraphTopicPanel;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.gui.tree.TopicTreePanel;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.layered.Layer;


/**
 * This is basic context for topics. LayeredTopicContext is used to pass topics
 * into tools from various UI components of Wandora application.
 *
 * @author akivela
 */

public class LayeredTopicContext extends AbstractContext implements Context<Topic> {



    /**
     * Creates a new instance of LayeredTopicContext
     */
    public LayeredTopicContext() {
        // Nothing here
    }


    public LayeredTopicContext(Wandora wandora, ActionEvent actionEvent, WandoraTool contextOwner) {
        initialize(wandora, actionEvent, contextOwner);
    }



    @Override
    public Iterator<Topic> getContextObjects() {
        return getContextObjects(getContextSource());
    }



    public Iterator<Topic> getContextObjects(Object contextSource) {
        if (contextSource == null)
            return null;

        List<Topic> contextTopics = new ArrayList<>();

        // ***** Wandora *****
        if (contextSource instanceof Wandora wandora) {
            try {
                Topic currentTopic = wandora.getOpenTopic();
                if (currentTopic != null) {
                    contextTopics.add(currentTopic);
                }
            }
            catch (Exception e) {
                log(e);
            }
        }

        // ***** TopicLinkBasename *****
        else if (contextSource instanceof TopicLinkBasename tlbn) {
            try {
                contextTopics.add(tlbn.getTopic());
            }
            catch (Exception e) {
                log(e);
            }
        }

        // ***** Topic *****
        else if (contextSource instanceof Topic t) {
            contextTopics.add(t);
        }

        // ***** Topic[] *****
        else if (contextSource instanceof Topic[] topicArray) {
            contextTopics.addAll(Arrays.asList(topicArray));
        }

        // ***** GraphTopicPanel *****
        else if (contextSource instanceof GraphTopicPanel gtp) {
            contextTopics.addAll(gtp.getContextTopics());
        }

        // ***** WebViewPanel *****
        else if (contextSource instanceof WebViewPanel wvp) {
            try {
                contextTopics.add(wvp.getTopic());
            }
            catch (Exception e) {
                /* Ignore */ }
        }

        // ***** TopicTable *****
        else if (contextSource instanceof TopicTable tt) {
            Topic[] topicArray = tt.getSelectedTopics();
            contextTopics.addAll(Arrays.asList(topicArray));
        }

        // ***** TopicGrid *****
        else if (contextSource instanceof TopicGrid tg) {
            Topic[] topicArray = tg.getSelectedTopics();
            contextTopics.addAll(Arrays.asList(topicArray));
        }

        // ***** MixedTopicTable *****
        else if (contextSource instanceof MixedTopicTable mtt) {
            Topic[] topicArray = mtt.getSelectedTopics();
            contextTopics.addAll(Arrays.asList(topicArray));
        }

        // ***** JTableHeader *****
        else if (contextSource instanceof JTableHeader th) {
            if (th.getTable() instanceof TopicTable topicTable) {
                contextTopics.add(topicTable.getSelectedHeaderTopic());
            }
        }

        // ***** TopicTreePanel *****
        else if (contextSource instanceof TopicTreePanel ttp) {
            contextTopics.add(ttp.getSelection());
        }

        // ***** TopicTree *****
        else if (contextSource instanceof TopicTree tree) {
            if (tree.getSelection() instanceof Topic t) {
                contextTopics.add(t);
            }
        }

        // ***** LayerTree *****
        else if (contextSource instanceof LayerTree layerTree) {
            TopicMap atm = getWandora().getTopicMap();
            Layer l = layerTree.getLastClickedLayer();
            TopicMap tm = null;
            if (l == null) {
                tm = getWandora().getTopicMap();
            }
            else {
                tm = l.getTopicMap();
            }
            try {
                Iterator<Topic> topics = tm.getTopics();
                Topic t = null;
                while (topics.hasNext()) {
                    t = topics.next();
                    if (t != null && !t.isRemoved()) {
                        Topic t2 = atm.getTopic(t.getOneSubjectIdentifier());
                        if (t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch (Exception e) {
                log(e);
            }
        }

        // ***** Layer *****
        else if (contextSource instanceof Layer layer) {
            TopicMap topicmap = layer.getTopicMap();
            try {
                TopicMap tm = getWandora().getTopicMap();
                Iterator<Topic> topics = topicmap.getTopics();
                Topic t = null;
                while (topics.hasNext()) {
                    t = topics.next();
                    if (t != null && !t.isRemoved()) {
                        Topic t2 = tm.getTopic(t.getOneSubjectIdentifier());
                        if (t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch (TopicMapException tme) {
                log(tme);
            }
        }


        // ***** SITable *****
        else if (contextSource instanceof SITable siTable) {
            Locator[] locators = siTable.getSelectedLocators();
            TopicMap topicmap = getWandora().getTopicMap();
            Topic t = null;
            for (int i = 0; i < locators.length; i++) {
                try {
                    t = topicmap.getTopic(locators[i]);
                    if (!contextTopics.contains(t)) {
                        contextTopics.add(t);
                    }
                }
                catch (Exception e) {
                    log(e);
                }
            }
        }

        // ***** TopicMap *****
        else if (contextSource instanceof TopicMap topicmap) {
            try {
                TopicMap tm = getWandora().getTopicMap();
                Iterator<Topic> topics = topicmap.getTopics();
                Topic t = null;
                while (topics.hasNext()) {
                    t = (Topic) topics.next();
                    if (t != null && !t.isRemoved()) {
                        Topic t2 = tm.getTopic(t.getOneSubjectIdentifier());
                        if (t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch (TopicMapException tme) {
                log(tme);
            }
        }

        // ***** OccurrenceTable *****
        else if (contextSource instanceof OccurrenceTable otable) {
            contextTopics.add(otable.getTopic());
        }

        // ***** OccurrenceTextEditor *****
        else if (contextSource instanceof OccurrenceTextEditor editor) {
            contextTopics.add(editor.getOccurrenceTopic());
        }
        return contextTopics.iterator();
    }


}
