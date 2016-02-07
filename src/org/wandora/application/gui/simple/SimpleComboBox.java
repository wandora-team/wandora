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
 * SimpleComboBox.java
 *
 * Created on November 16, 2004, 5:15 PM
 */

package org.wandora.application.gui.simple;


import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.wandora.application.gui.*;

/**
 *
 * @author  akivela
 */
public class SimpleComboBox extends JComboBox implements MouseListener, SimpleComponent {
    
   
    
    /** Creates a new instance of SimpleComboBox */
    public SimpleComboBox() {
        initialize();
    }
    
    
    public SimpleComboBox(Vector v) {
        super(v);
        initialize();
    }
    
    
    public SimpleComboBox(String[] content) {
        super();
        initialize();
        setOptions(content);
    }
    
    
    public SimpleComboBox(Set content) {
        super();
        initialize();
        setOptions(content);
    }
    
    
    public SimpleComboBox(Collection content) {
        super();
        initialize();
        setOptions(content);
    }
    
    
    private void initialize() {
        this.addMouseListener(this);
        this.setFont(UIConstants.comboBoxFont);
        UIConstants.setFancyFont(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ComboBoxEditor e = this.getEditor();
        Component ec = e.getEditorComponent();
        this.setRenderer(new BorderListCellRenderer());
        this.setEditable(true);
    }
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    
    public void setOptions(String[] content) {
        removeAllItems();
        for (String item : content) {
            addItem(item);
        }
    }
    
        
    public void setOptions(Enumeration content) {
        removeAllItems();
        while(content.hasMoreElements()) {
            try {
                addItem(content.nextElement());
            }
            catch (Exception e) {}
        }
    }
    
    public void setOptions(Set content) {
        removeAllItems();
        for(Object o : content) {
            try {
                addItem(o);
            }
            catch (Exception e) {}
        }
    }
    
    public void setOptions(Collection content) {
        removeAllItems();
        for(Object o : content) {
            try {
                addItem(o);
            }
            catch (Exception e) {}
        }
    }

    
    @Override
    public void focusGained(java.awt.event.FocusEvent focusEvent) {
        // DO NOTHING...
    }
    
    @Override
    public void focusLost(java.awt.event.FocusEvent focusEvent) {
        // DO NOTHING...
    }
   
    
    
    /*
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    */
    
    
    // -------------------------------------------------------------------------
    
    
    
    public class BorderListCellRenderer implements ListCellRenderer {

        private Border insetBorder;
        private DefaultListCellRenderer defaultRenderer;

        public BorderListCellRenderer() {
            this.insetBorder = new EmptyBorder(0, 4, 0, 4);
            this.defaultRenderer = new DefaultListCellRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(
                    list, 
                    value, 
                    index, 
                    isSelected, 
                    cellHasFocus
            );
            renderer.setBorder(insetBorder);
            return renderer;
        }

    }
}
