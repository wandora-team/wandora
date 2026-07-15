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
 * EditorPanel.java
 *
 * Created on 16. joulukuuta 2004, 11:27
 */

package org.wandora.application.gui;




import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.border.Border;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolManager;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.tools.ChainExecuter;
import org.wandora.application.tools.extractors.files.SimpleFileExtractor;
import org.wandora.utils.DnDBox;
import org.wandora.utils.swing.JPanelWithBackground;


/**
 *
 * @author  akivela
 */
public class EditorPanel extends JPanelWithBackground implements DropTargetListener, DragGestureListener {
    
	private static final long serialVersionUID = 1L;
	
    private Object dropContext = null;
    protected Wandora parent = null;
    private DropTarget dt;
    private int orders = 0;
    
    private Border defaultBorder = null;
    
    
    
    public EditorPanel(Wandora wandora, Object dc) {
        this(wandora, 0, dc);
    }
    
    
    /** Creates a new instance of EditorPanel */
    public EditorPanel(Wandora wandora, int o, Object dc) {
        parent = wandora;
        orders = o;
        dropContext = dc;
        dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
    
    
    
  // ---- dnd -----
    
    @Override
    public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        if(! UIConstants.dragBorder.equals( this.getBorder())) {
            defaultBorder = this.getBorder();
            this.setBorder(UIConstants.dragBorder);
        }
    }
    
    
    @Override
    public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
        this.setBorder(defaultBorder);
    }
    
    
    @Override
    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        if(! UIConstants.dragBorder.equals( this.getBorder())) {
            defaultBorder = this.getBorder();
            this.setBorder(UIConstants.dragBorder);
        }
    }
    
    private void acceptFileList(java.util.List<File> files) throws Exception {
        ArrayList<WandoraTool> importTools = new ArrayList<>();
        boolean yesToAll = false;
        
        for(File file : files) {
            ArrayList<WandoraTool> importToolsForFile = WandoraToolManager.getImportTools(file, orders);
            if(importToolsForFile != null && !importToolsForFile.isEmpty()) {
                importTools.addAll(importToolsForFile);
            }
            else {
                if(yesToAll) {
                    SimpleFileExtractor extractor = new SimpleFileExtractor();
                    extractor.setForceFiles(new File[] { file } );
                    importTools.add(extractor);
                }
                else {
                    int a = WandoraOptionPane.showConfirmDialog(parent, 
                        "Extract the dropped file with Simple File Extractor?",
                        "Extract instead of import?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                    if(a == WandoraOptionPane.YES_OPTION || a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                        SimpleFileExtractor extractor = new SimpleFileExtractor();
                        extractor.setForceFiles(new File[] { file } );
                        importTools.add(extractor);
                    }
                    if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                        yesToAll = true;
                    }
                    if(a == WandoraOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }               
            }
        }
        //System.out.println("drop context == " + dropContext);
        ActionEvent fakeEvent = new ActionEvent(dropContext != null ? dropContext : parent, 0, "merge");
        ChainExecuter chainExecuter = new ChainExecuter(importTools);
        chainExecuter.execute(parent, fakeEvent);
    }
    
    
    
    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        final java.util.List<File> files = DnDBox.acceptFileList(e);
        if(files==null) {
            System.out.println("Drop rejected! Wrong data flavor!");
            e.rejectDrop();
        }
        else {
            try {
                if(!files.isEmpty()) {
                    Thread dropThread = new Thread() {
                        public void run() {
                            try {
                                acceptFileList(files);
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                            catch(Error err) {
                                err.printStackTrace();
                            }
                        }
                    };
                    dropThread.start();
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            catch(Error err) {
                err.printStackTrace();
            }
        }
        this.setBorder(null);
    }
    
    @Override
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

    @Override
    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent dragGestureEvent) {
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------- ADDING TOPIC PANEL ---
    // -------------------------------------------------------------------------
    
    
    
    
    public void addTopicPanel(TopicPanel topicPanel) {
        this.add(topicPanel.getGui(), BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    
    
}
