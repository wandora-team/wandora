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
 * EditorPanel.java
 *
 * Created on 16. joulukuuta 2004, 11:27
 */

package org.wandora.application.gui;




import org.wandora.utils.swing.JPanelWithBackground;
import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.wandora.utils.DnDBox;
import org.wandora.application.*;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.tools.*;


/**
 *
 * @author  akivela
 */
public class EditorPanel extends JPanelWithBackground implements DropTargetListener, DragGestureListener {
    
    private Object dropContext = null;
    protected Wandora parent = null;
    private DropTarget dt;
    private int orders = 0;
    
    private Border defaultBorder = null;
    
    
    
    public EditorPanel(Wandora admin, Object dc) {
        this(admin, 0, dc);
    }
    
    
    /** Creates a new instance of EditorPanel */
    public EditorPanel(Wandora admin, int o, Object dc) {
        parent = admin;
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
        ArrayList<WandoraTool> importTools=WandoraToolManager.getImportTools(files, orders);
        for(WandoraTool t : importTools){
            if(t==null){
                WandoraOptionPane.showMessageDialog(parent, "You have dropped Wandora a file with unsupported file type! Wandora supports drop of wpr, xtm, ltm, jtm, rdf(s), n3, and obo files. Extractors may support also other file types.", "Unsupported file type", WandoraOptionPane.ERROR_MESSAGE);                
                break;
            }
        }
        //System.out.println("drop context == " + dropContext);
        ActionEvent fakeEvent = new ActionEvent(dropContext != null ? dropContext : parent, 0, "merge");
        ChainExecuter chainExecuter = new ChainExecuter(importTools);
        chainExecuter.execute(parent, fakeEvent);
    }
    
    
    
    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        java.util.List<File> files=DnDBox.acceptFileList(e);
        if(files==null){
            System.out.println("Drop rejected! Wrong data flavor!");
            e.rejectDrop();
        }
        else{
            try{
                if(files.size()>0) acceptFileList(files);
            }
            catch(Exception ex){
                ex.printStackTrace();
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
