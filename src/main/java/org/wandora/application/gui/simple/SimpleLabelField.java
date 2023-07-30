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
 * SimpleLabelField.java
 *
 * Created on 16. helmikuuta 2006, 12:23
 *
 */

package org.wandora.application.gui.simple;



import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;

/**
 *
 * @author akivela
 */
public class SimpleLabelField extends JPanel implements MouseListener, KeyListener, ActionListener, SimpleComponent {

    public final static int LABEL = 1;
    public final static int FIELD = 2;
    
    private SimpleField field;
    private SimpleLabel label;
    
    private ArrayList<ChangeListener> changeListeners;

    private int mode = LABEL;
    private String previousText = "";
    private boolean initialized = false;
    
    
    
    public SimpleLabelField(String name) {
        changeListeners = new ArrayList();
        field = new SimpleField(name);
        label = new SimpleLabel(name);
        initialize();
        initialized = true;
    }
    
    
    
    /** Creates a new instance of WandoraTextField */
    public SimpleLabelField() {
        this("");
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public void initialize() {
        label.removeMouseListener(label);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        field.addKeyListener(this);
        
        this.setLayout(new BorderLayout());
        this.setInheritsPopupMenu(true);
        //this.addMouseListener(this);
        this.addKeyListener(this);
        //this.setFocusTraversalKeysEnabled(false);
        this.setFocusable(true);
        this.addFocusListener(this);
        setUpGui(mode);
    }
        
        
   

    
    public void setUpGui(int mode) {
        this.mode = mode;
        if(mode == FIELD) {
            previousText = field.getText();
            this.removeAll();
            this.add(field, BorderLayout.CENTER);
            field.setCaretPosition(previousText.length());
            field.requestFocus();
        }
        else if(mode == LABEL) {
            label.setText(field.getText());
            this.removeAll();
            this.add(label, BorderLayout.CENTER);
        }
        if(initialized) {
            this.setVisible(false);
            this.setVisible(true);
        }
    }
    
    
    
    
    public String getText() {
        return field.getText();
    }
    
    public void setText(String newText) {
        label.setText(newText);
        field.setText(newText);
    }
    
    
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }
    
    
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    public void removeAllChangeListeners() {
        changeListeners = new ArrayList();
    }
    
    private void sendChangeEvent(ChangeEvent e) {
        if(changeListeners == null || changeListeners.size() == 0) return;
        ChangeListener listener;
        for(Iterator<ChangeListener> listeners = changeListeners.iterator(); listeners.hasNext(); ) {
            try {
                listener = listeners.next();
                listener.stateChanged(e);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    } 
    
    
    
    
    
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() > 1 && mode == LABEL) {
            setUpGui(FIELD);
        }
    }
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void keyPressed(java.awt.event.KeyEvent keyEvent) {
    }
    
    public void keyReleased(java.awt.event.KeyEvent e) {
        if(e.getKeyCode() == e.VK_ENTER) {
            setUpGui(LABEL);
            e.consume();
            if(! this.getText().equals(previousText)) {
                sendChangeEvent(new ChangeEvent(this));
            }
        }
    }
    
    public void keyTyped(java.awt.event.KeyEvent e) {
/*        if(listWindow!=null){
            e.setSource(listWindow.l);
            listWindow.l.dispatchEvent(e);
        }*/
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c.equals("Copy")) {
            field.copy();
        }
        else if(c.equals("Cut")) {
            field.cut();
        }
        else if(c.equals("Paste")) {
            field.paste();
        }
        else if(c.equals("Edit")) {
            setUpGui(FIELD);
        }
    }

    
    
    
     
    
    public void focusGained(java.awt.event.FocusEvent focusEvent) {
        Wandora w = Wandora.getWandora(this);
        if(w != null) {
            w.gainFocus(this);
        }
    }
    
    public void focusLost(java.awt.event.FocusEvent focusEvent) {
        // DO NOTHING...
    }
 
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
}
