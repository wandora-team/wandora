/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * TopicTreeTopicEditor.java
 *
 * Created on 4. elokuuta 2006, 16:55
 */

package org.wandora.application.gui.tree;


import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.application.gui.topicstringify.TopicToString;

/**
 *
 * @author akivela
 */


public class TopicTreeTopicEditor extends DefaultTreeCellEditor implements TreeCellEditor, CellEditorListener {
    private TopicTree topicTree;
    private Component editor;
    private SimpleField field = null;
    private Wandora wandora;
    private Object originalValue = null;
    
   

    public TopicTreeTopicEditor(Wandora w, TopicTree topicTree, SimpleField field, TopicTreeTopicRenderer renderer) {
        super(topicTree, renderer, new DefaultCellEditor(field));
        this.renderer = renderer;
        this.wandora = w;
        this.topicTree = topicTree;
        this.field = field;
        this.addCellEditorListener(this);
    }
    
    

    @Override
    public Component getTreeCellEditorComponent(JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row) {
        
        originalValue = value;
        
        /* Workaround for swing bug 'DefaultTreeCellEditor doesn't get Icon set in DefaultTreeCellRenderer'
         * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4663832 */
        String res=((TopicGuiWrapper)value).icon;
        renderer.setIcon(((TopicTreeTopicRenderer)renderer).solveIcon(res));
        
        editor = super.getTreeCellEditorComponent(tree, value, sel, expanded, leaf,row);
        return editor;
    }

    
    
    
    
    /* Workaround for swing bug 'DefaultTreeCellEditor doesn't get Icon set in DefaultTreeCellRenderer'
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4663832 */
    @Override
    protected void determineOffset(JTree tree, Object value,boolean isSelected, boolean expanded,boolean leaf, int row) {
        if(renderer != null) {
            renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true );
            editingIcon = renderer.getIcon();
            if(editingIcon != null)
                offset = renderer.getIconTextGap() +
                        editingIcon.getIconWidth();
            else
                offset = renderer.getIconTextGap();
        }
        else {
            editingIcon = null;
            offset = 0;
        }
    }




    
    
    @Override
    public void editingCanceled(ChangeEvent e) {
        
    }
    
   
    @Override
    public void editingStopped(ChangeEvent e) {
        try {
            if(field instanceof JTextField) {
                String value = ((JTextField) field).getText();
                if(value != null && value.length() > 0) {
                    Topic topic = topicTree.getSelection();
                    if(topic != null && !topic.isRemoved()) {
                        if(TopicToString.supportsStringIntoTopic()) {
                            TopicToString.stringIntoTopic((String) originalValue.toString(), value, topic);
                            if(wandora != null) wandora.doRefresh();
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            wandora.handleError(ex);
        }
    }

}
