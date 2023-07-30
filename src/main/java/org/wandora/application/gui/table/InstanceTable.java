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
 * InstanceTable.java
 *
 * Created on August 18, 2004, 9:19 AM
 */

package org.wandora.application.gui.table;




import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.ApplicationContext;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AddInstance;
import org.wandora.application.tools.DeleteFromTopics;
import org.wandora.application.tools.PasteInstances;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.ClipboardBox;




/**
 * @author  olli, akivela
 */
public class InstanceTable extends TopicTable /*implements DropTargetListener*/ {

    private Topic topic;
    private Topic[] instances;


    
    
    private Object[] instancePopupStruct = new Object[] {
        "---",
        "Add instance...", new AddInstance(new ApplicationContext()),
        "Paste instances", new Object[] {
            "Paste instances as basenames...", new PasteInstances(new ApplicationContext()),
            "Paste instances as SIs...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_NOTHING, PasteInstances.PASTE_SIS),
            "---",
            "Paste instances with names...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_NAMES),
            "Paste instances with SLs...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_SLS),
            "Paste instances with SIs...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_SIS),
            "Paste instances with classes...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_CLASSES),
            "Paste instances with instances...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_INSTANCES),
            "Paste instances with players...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_PLAYERS),
            "Paste instances with occurrences...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_TEXTDATAS),
        },
        "Delete instances...", new DeleteFromTopics(DeleteFromTopics.LOOSE_INSTANCES_IN_CONTEXT),
         
    };
    
    
    
    /** Creates a new instance of InstanceTable */
    public InstanceTable(Topic topic, Wandora w)  throws TopicMapException  {
        super(w);
        
        this.topic = topic;
        Collection unsorted = topic.getTopicMap().getTopicsOfType(topic);
        this.instances = (Topic[]) TMBox.sortTopics(unsorted,null).toArray(new Topic[0]);
        initialize(instances, null);
        
        this.setTransferHandler(new InstanceTableTransferHandler());
        this.setDropMode(DropMode.ON);
    }
    
    
    
    @Override
    public Object[] getPopupStruct() {
        return getPopupStruct(instancePopupStruct);
    }
    
    
    
    private boolean autoCreateTopicsInPaste = false;
    
    @Override
    public void paste() {
        String tabText = ClipboardBox.getClipboard();
        StringTokenizer tabLines = new StringTokenizer(tabText, "\n");
        autoCreateTopicsInPaste = false;
        while(tabLines.hasMoreTokens()) {
            String tabLine = tabLines.nextToken();
            StringTokenizer topicIdentifiers = new StringTokenizer(tabLine, "\t");
            try {
                String topicIdentifier = null;
                while(topicIdentifiers.hasMoreTokens()) {
                    topicIdentifier = topicIdentifiers.nextToken();
                    if(topicIdentifier != null && topicIdentifier.length() > 0) {
                        Topic pastedTopic = getTopicForIdentifier(topicIdentifier);
                        if(pastedTopic == null) {
                            boolean createTopicInPaste = false;
                            if(!autoCreateTopicsInPaste) {
                                int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Can't find a topic for identifier '"+topicIdentifier+"'. Would you like to create a topic for '"+topicIdentifier+"'?", "Create new topic?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                if(a == WandoraOptionPane.YES_OPTION) {
                                    createTopicInPaste = true;
                                }
                                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                                    autoCreateTopicsInPaste = true;
                                }
                                else if(a == WandoraOptionPane.CANCEL_OPTION) {
                                    return;
                                }
                            }
                            if(autoCreateTopicsInPaste || createTopicInPaste) {
                                TopicMap tm = Wandora.getWandora().getTopicMap();
                                if(tm != null) {
                                    boolean identifierIsURL = false;
                                    try {
                                        URL u = new URL(topicIdentifier);
                                        identifierIsURL = true;
                                    }
                                    catch(Exception e) {}
                                    pastedTopic = tm.createTopic();
                                    if(identifierIsURL) {
                                        pastedTopic.addSubjectIdentifier(new Locator(topicIdentifier));
                                    }
                                    else {
                                        pastedTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                                        pastedTopic.setBaseName(topicIdentifier);
                                    }
                                }
                            }
                        }
                        if(pastedTopic != null && topic != null) {
                            pastedTopic.addType(topic);
                        }
                    }
                }
            }
            catch(Exception e) {
                
            }
        }
    }
    
    

    // -------------------------------------------------------------------------
    // ----------------------------------------------------- actionPerformed ---
    // -------------------------------------------------------------------------
    
    
        
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------- DRAG & DROP ---
    // -------------------------------------------------------------------------
   
    
    
    private class InstanceTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return DnDHelper.makeTopicTableTransferable(InstanceTable.this);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try{
                TopicMap tm=Wandora.getWandora().getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base=Wandora.getWandora().getOpenTopic();
                if(base==null) return false;

                for(Topic t : topics){
                    t.addType(base);
                }
                Wandora.getWandora().doRefresh();
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(Exception ce){}
            return false;
        }

    }
    
}
