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
 * SimpleURIField.java
 *
 * Created on November 12, 2004, 3:22 PM
 */


package org.wandora.application.gui.simple;


import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JPopupMenu;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.utils.Base64;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;




/**
 *
 * @author akivela
 */
public class SimpleURIField extends SimpleField {

    private static final long serialVersionUID = 1L;
    
    private static Color BROKEN_URI_COLOR = new Color(255, 240, 240);
    private static Color DATA_URI_COLOR = Color.WHITE;
    private static Color UNSET_URI_COLOR = new Color(246, 246, 246);
    
    private String completeFieldText = null;

    private Object[] popupStruct = new Object[] {
        "Cut", UIBox.getIcon("gui/icons/cut.png"),
        "Copy", UIBox.getIcon("gui/icons/copy.png"),
        "Paste", UIBox.getIcon("gui/icons/paste.png"),
        "Clear", UIBox.getIcon("gui/icons/clear.png"),
    };
    
    
    
    public SimpleURIField() {
        initialize();
    }

    
    public boolean isValidURI(String uriString) {
        if(uriString != null && uriString.length() > 0) {
            try {
                new URI(uriString);
            }
            catch(Exception e) {
                return DataURL.isDataURL(uriString);
            }
        }
        return true;
    }
    
    
    private Color getBackgroundColorFor(String uriString) {
        if(uriString == null || uriString.length() == 0) {
            return UNSET_URI_COLOR;
        }
        if(DataURL.isDataURL(uriString)) {
            return DATA_URI_COLOR;
        }
        try {
            new URI(uriString);
        }
        catch(Exception e) {
            return BROKEN_URI_COLOR;
        }
        return Color.WHITE;
    }
    

    
    @Override
    public void setText(String text) {
        setBackground(getBackgroundColorFor(text));
        
        try {
            if(DataURL.isDataURL(text)) {
                String textFragment = text.substring(0, Math.min(text.length(), 64))+"... ("+text.length()+")";
                completeFieldText = text;
                super.setText(textFragment);
                setEditable(false);
            }
            else {
                completeFieldText = null;
                super.setText(text);
                setEditable(true);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    @Override
    public String getText() {
        try {
            if(completeFieldText != null) {
                return completeFieldText;
            }
            else {
                return super.getText();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    
    
    @Override
    public void setPopupMenu() {
        JPopupMenu popup = UIBox.makePopupMenu(popupStruct, this);
        setComponentPopupMenu(popup);
    }
    
    
    @Override
    public void drop(final java.awt.dnd.DropTargetDropEvent e) {
        try {
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            Transferable tr = e.getTransferable();
            if(e.isDataFlavorSupported(fileListFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                final SimpleURIField uriField = this;
                final java.util.List<File> files = (java.util.List<File>) tr.getTransferData(fileListFlavor);
                
                Thread dropThread = new Thread() {
                    public void run() {
                        try {
                            int ret=WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Make DataURI out of given file content? Answering no uses filename as an URI.","Make DataURI?", WandoraOptionPane.YES_NO_OPTION);
                            if(ret==WandoraOptionPane.YES_OPTION) {
                                for( File file : files ) {
                                    DataURL dataURL = new DataURL(file);
                                    uriField.setText(dataURL.toExternalForm(Base64.DONT_BREAK_LINES));
                                    break; // CAN'T HANDLE MULTIPLE FILES. ONLY FIRST IS USED.
                                }
                            }
                            else if(ret==WandoraOptionPane.NO_OPTION) {
                                String text="";
                                for( File file : files ) {
                                    if(text.length()>0) text+=";";
                                    text+=file.toURI().toString();
                                }
                                uriField.setText(text);
                            }
                            triggerChangeAction();
                            e.dropComplete(true);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                dropThread.start();
            }
            else if(e.isDataFlavorSupported(stringFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String data = (String)tr.getTransferData(stringFlavor);
                this.setText(data);
                e.dropComplete(true);
            }
            else {
                System.out.println("Drop rejected! Unsupported data flavor!");
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
        this.setBorder(defaultBorder);
    }
    
    
    // ------------------------------------------------- copy, cut and paste ---
    
    
    @Override
    public void copy() {
        if(completeFieldText != null) {
            if(getSelectionStart() == 0 && getSelectionEnd() == super.getText().length()) {
                ClipboardBox.setClipboard(completeFieldText);
                return;
            }
        }
        super.copy();
    }
    
    
    @Override
    public void cut() {
        if(completeFieldText != null) {
            if(getSelectionStart() == 0 && getSelectionStart() == super.getText().length()) {
                ClipboardBox.setClipboard(completeFieldText);
                setText("");
                return;
            }
        }
        super.cut();
    }
    
    
    @Override
    public void paste() {
        if(completeFieldText != null) {
            String newText = ClipboardBox.getClipboard();
            setText(newText);
            return;
        }
        super.paste();
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected void triggerChangeAction() {
        this.requestFocusInWindow();

        try { 
            Robot robot = new Robot(); 
            robot.keyPress(KeyEvent.VK_ENTER); 
        } 
        catch (AWTException e) { 
            e.printStackTrace(); 
        } 
    }

}
