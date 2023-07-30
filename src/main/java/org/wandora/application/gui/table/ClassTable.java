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
 * ClassTable.java
 *
 * Created on August 17, 2004, 4:41 PM
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
import org.wandora.application.tools.AddClass;
import org.wandora.application.tools.DeleteFromTopics;
import org.wandora.application.tools.PasteClasses;
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


public class ClassTable extends TopicTable /*implements DropTargetListener*/ {
    

    private Topic topic;
    private Topic[] types;

    
    private Object[] classPopupStruct = new Object[] {
        "---",
        "Add class...", new AddClass(new ApplicationContext()),
        "Paste classes", new Object[] {
            "Paste classes as basenames...", new PasteClasses(new ApplicationContext()),
            "Paste classes as SIs...", new PasteClasses(new ApplicationContext(), PasteClasses.INCLUDE_NOTHING, PasteClasses.PASTE_SIS),

            "---",
            "Paste classes with names...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_NAMES),
            "Paste classes with SLs...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_SLS),
            "Paste classes with SIs...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_SIS),
            "Paste classes with classes...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_CLASSES),
            "Paste classes with instances...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_INSTANCES),
            "Paste classes with players...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_PLAYERS),
            "Paste classes with occurrences...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_TEXTDATAS),
        },
        "Delete classes...", new DeleteFromTopics(DeleteFromTopics.LOOSE_CLASSES_OF_CURRENT),
    };
    
    
    
    /** Creates a new instance of ClassTable */
    public ClassTable(Topic topic, Wandora w) throws TopicMapException {
        super(w);
        
        this.topic = topic;
        Collection unsorted = topic.getTypes();
        this.types = (Topic[]) TMBox.sortTopics(unsorted,null).toArray(new Topic[0]);
        
        initialize(types, null);
        
        this.setTransferHandler(new ClassTableTransferHandler());
        this.setDropMode(DropMode.ON);
    }
    
    
    
    
    @Override
    public Object[] getPopupStruct() {
        return getPopupStruct(classPopupStruct);
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
                        if(pastedTopic != null) {
                            topic.addType(pastedTopic);
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
    
    

/*
    public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        if(! parent.dragBorder.equals( this.getBorder())) {
            defaultBorder = this.getBorder();
            this.setBorder(parent.dragBorder);
        }
    }
    
    
    public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
        this.setBorder(defaultBorder);
    }
    
    
    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        if(! parent.dragBorder.equals( this.getBorder())) {
            defaultBorder = this.getBorder();
            this.setBorder(parent.dragBorder);
        }
    }
    
    
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            Transferable tr = e.getTransferable();

            if(e.isDataFlavorSupported(stringFlavor)) {                   
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String data = (String)tr.getTransferData(stringFlavor);

                System.out.println("Trying string data flavor. Data="+data);                    

                String[] split=data.split("\n");
                String identifier = null;
                TopicMap topicmap = parent.getTopicMap();
                Topic t = null;
                Topic base = parent.getOpenTopic();
                boolean changed = false;
                if(base != null) {
                    for(int i=0; i<split.length; i++) {
                        identifier = split[i];
                        if(identifier != null) {
                            identifier = identifier.trim();
                            if(identifier.length() > 0) {
                                if(identifier.startsWith("http:")) {
                                    t = TMBox.getOrCreateTopic(topicmap, identifier);
                                }
                                else {
                                    t = TMBox.getOrCreateTopicWithBaseName(topicmap, identifier);
                                }
                                if(t != null) {
                                    base.addType(t);
                                    changed = true;
                                }
                            }
                        }
                    }
                    if(changed) {
                        parent.refreshTopic();
                    }
                }
                e.dropComplete(true);
            }
            else {
                System.out.println("Drop rejected! Wrong data flavor!");
                e.rejectDrop();
            }
            this.setBorder(null);
        }
        catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }*/

    
    
    
    
    private class ClassTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return DnDHelper.makeTopicTableTransferable(ClassTable.this);
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
                    base.addType(t);
                }
                Wandora.getWandora().doRefresh();
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(Exception ce){}
            return false;
        }

    }

}
