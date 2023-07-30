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
 * SimpleField.java
 *
 * Created on November 12, 2004, 3:22 PM
 */

package org.wandora.application.gui.simple;



import java.awt.AWTKeyStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.undo.UndoManager;

import org.wandora.application.Wandora;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.EasyVector;


/**
 *
 * @author  akivela
 */
public class SimpleField extends JTextField implements MouseListener, KeyListener, ActionListener, SimpleComponent, Clipboardable, DropTargetListener, DragGestureListener {
    
    protected Border defaultBorder = null;
    protected DropTarget dt;
    protected Wandora wandora = null;
    protected UndoManager undoManager = null;
    protected Insets defaultMargins = new Insets(3,3,3,3);
    
    
    protected String[] options = new String[] {};
    private Object[] popupStruct = new Object[] {
        "Cut", UIBox.getIcon("gui/icons/cut.png"),
        "Copy", UIBox.getIcon("gui/icons/copy.png"),
        "Paste", UIBox.getIcon("gui/icons/paste.png"),
        "Clear", UIBox.getIcon("gui/icons/clear.png")
    };
    
    
    public SimpleField(String name) {
        super(name);
        initialize();
    }
    
    
    
    /** Creates a new instance of SimpleField */
    public SimpleField() {
        initialize();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void initialize() {
        this.addMouseListener(this);
        // this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(this);
        this.setFocusable(true);
        this.addFocusListener(this);
        this.setFocusTraversalKeysEnabled(true);
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,new HashSet(new EasyVector(new Object[]{AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,0)})));
        this.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,new HashSet(new EasyVector(new Object[]{AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,InputEvent.SHIFT_DOWN_MASK)})));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        
        // undoManager = new UndoManager();
        // Document document = this.getDocument();
        // document.addUndoableEditListener(undoManager);
 
        this.setMargin(defaultMargins);
        
        this.setDragEnabled(true);
        dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        
        setPopupMenu();
    }
    
    
    
    public void setPopupMenu() {
        JPopupMenu popup = UIBox.makePopupMenu(popupStruct, this);
        setComponentPopupMenu(popup);
    }
    
    
    
    public void setOptions(String[] ops) {
        this.options = ops;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void setCurrentPart(String partText) {
        setPart(currentPartNumber(), partText);
    }
    
    
    public void setPart(int partNumber, String partText) {
        String[] fields = text2Parts(this.getText());
        String oldPart = fields[partNumber];
        fields[partNumber] = partText;
        setText(parts2Text(fields));
        moveCaretToPart(partNumber+1);
    }
   
    
    public int currentPartNumber() {
        String s = getText().substring(0, getCaretPosition());
        String[] fields = text2Parts(s);
        return fields.length-1;
    }
    
    
    public String currentPartString() {
        String s = getText().substring(0, getCaretPosition());
        String[] fields = text2Parts(s);
        return fields[fields.length-1];
    }
    
   

    public String parts2Text(String[] fields) {
        StringBuilder sb = new StringBuilder();
        int size = fields.length;
        for(int i=0; i<size; i++) {
            sb.append(fields[i]);
            if(i<size-1) sb.append(" ; ");
        }
        return sb.toString();
    }

    
    public String[] text2Parts(String text) {
        String[] parts = text.split(" ; ");
        try {
            if(Pattern.compile(" ; " + "$").matcher(text).find()) {
                String[] parts2 = new String[parts.length + 1];
                for(int i=0; i<parts.length; i++) {
                    parts2[i] = parts[i];
                }
                parts2[parts.length] = "";
                parts = parts2;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return parts;
    }
    
    
    public void moveCaretToPart(int partNumber) {
        Pattern p = Pattern.compile("^" + " ; ");
        String text = getText();
        int i = 0;
        int c = 0;
        for(; i<text.length(); i++) {
            Matcher m = p.matcher(text.substring(i));
            if(m.find()) {
                c++;
                i = i+m.end();
            }
            if(c == partNumber) break;
        }
        setCaretPosition(i);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
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
    
    @Override
    public void keyPressed(java.awt.event.KeyEvent keyEvent) {
    }
    
    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
/*        if(listWindow==null && e.getKeyCode()==e.VK_TAB){
            e.consume();
            showList(); 
        }*/
    }
    
    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
/*        if(listWindow!=null){
            e.setSource(listWindow.l);
            listWindow.l.dispatchEvent(e);
        }*/
    }
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c.equals("Copy")) {
            this.copy();
        }
        else if(c.equals("Cut")) {
            this.cut();
        }
        else if(c.equals("Paste")) {
            this.paste();
        }
        else if(c.equals("Clear")) {
            this.setText("");
        }
        else if(c.equals("Undo")) {
            if(undoManager != null) {
                if(undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        else if(c.equals("Redo")) {
            if(undoManager != null) {
                if(undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        }

    }    

    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------------- FOCUS ---
    // -------------------------------------------------------------------------
    
    
     
    
    @Override
    public void focusGained(java.awt.event.FocusEvent focusEvent) {
        if(wandora == null) wandora = Wandora.getWandora(this);
        if(wandora != null) {
            wandora.gainFocus(this);
        }
    }
    
    @Override
    public void focusLost(java.awt.event.FocusEvent focusEvent) {
        // DO NOTHING...
    }
   
    
    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------- DRAG & DROP ---
    // -------------------------------------------------------------------------
    
    

    
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
    
    
    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            Transferable tr = e.getTransferable();
            if(e.isDataFlavorSupported(fileListFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                java.util.List<File> files = (java.util.List<File>) tr.getTransferData(fileListFlavor);
                String text="";
                for( File file : files ) {
                    if(text.length()>0) text+=";";
                    text+=file.getPath();
                }
                this.setText(text);
                e.dropComplete(true);
            }
            else if(e.isDataFlavorSupported(stringFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String data = (String)tr.getTransferData(stringFlavor);
                this.setText(data);
                e.dropComplete(true);
            }
            else {
                System.out.println("Drop rejected! Wrong data flavor!");
                e.rejectDrop();
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        catch(Error err) {
            err.printStackTrace();
        }
        this.setBorder(defaultBorder);
    }
    
    @Override
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

    @Override
    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent dragGestureEvent) {
    }    
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
    
    // ----------------------------------------------------------- CLIPBOARD ---
    
    
    @Override
    public void copy() {
        String text = getSelectedText();
        if(text == null || text.length() == 0) {
            text = getText();
        }
        ClipboardBox.setClipboard(text);
    }
    
    
    @Override
    public void cut() {
        String text = getSelectedText();
        if(text == null || text.length() == 0) {
            ClipboardBox.setClipboard(getText());
            setText("");
        }
        else {
            ClipboardBox.setClipboard(text);
            removeSelectedText();
        }   
    }
    
    
    @Override
    public void paste() {
        String text = ClipboardBox.getClipboard();
        replaceSelectedText(text);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public void removeSelectedText() {
        try {
            int selectionStartLoc = this.getSelectionStart();
            int selectionEndLoc = this.getSelectionEnd();

            if(selectionStartLoc != selectionEndLoc) {
                int d = selectionEndLoc-selectionStartLoc;
                this.getDocument().remove(selectionStartLoc, d);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    public void replaceSelectedText(String txt) {
        try {
            int selectionStartLoc = this.getSelectionStart();
            int selectionEndLoc = this.getSelectionEnd();

            if(selectionStartLoc != selectionEndLoc) {
                int d = selectionEndLoc-selectionStartLoc;
                this.getDocument().remove(selectionStartLoc, d);
            }
            this.getDocument().insertString(selectionStartLoc, txt, null);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
}


