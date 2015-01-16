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
 * SimpleURIField.java
 *
 * Created on November 12, 2004, 3:22 PM
 */


package org.wandora.application.gui.simple;


import java.awt.Graphics;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URI;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.wandora.application.gui.UIBox;




/**
 *
 * @author akivela
 */
public class SimpleURIField extends SimpleField implements DocumentListener {

    
    
    private static BufferedImage invalidURIImage = UIBox.getImage("resources/gui/icons/invalid_uri.png");
    
    
    
    public SimpleURIField() {
        super();
        Document d = this.getDocument();
        d.addDocumentListener(this);
    }
    

    
    public boolean isValidURI() {
        String u = getText();
        if(u != null && u.length() > 0) {
            try {
                new URI(u);
            }
            catch(Exception e) {
                return false;
            }
        }
        return true;
    }
    
    
  
    
    
    @Override
    public void paint(Graphics gfx) {
        super.paint(gfx);
        if(!isValidURI() && gfx != null) {
            gfx.drawImage(invalidURIImage, this.getWidth()-20, 1, this);
        }
    }
    
    
    
    
    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            Transferable tr = e.getTransferable();
            if(e.isDataFlavorSupported(fileListFlavor)) {
                //int ret=WandoraOptionPane.showOptionDialog(this, "Would you like to load text from the dropped document?","Load or reject", WandoraOptionPane.YES_NO_OPTION,WandoraOptionPane.QUESTION_MESSAGE);
                //if(ret==WandoraOptionPane.YES_OPTION) {
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    java.util.List<File> files = (java.util.List<File>) tr.getTransferData(fileListFlavor);
                    String text="";
                    for( File file : files ) {
                        if(text.length()>0) text+=";";
                        text+=file.toURI().toString();
                    }
                    this.setText(text);
                    e.dropComplete(true);
                //}
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
        this.setBorder(defaultBorder);
    }

    
    
    
    // -------------------------------------------------- DOCUMENT LISTENER ----
    
    
    
    public void insertUpdate(DocumentEvent e) {
        paint(this.getGraphics());
        revalidate();
    }

    public void removeUpdate(DocumentEvent e) {
        paint(this.getGraphics());
        revalidate();
    }

    public void changedUpdate(DocumentEvent e) {
        paint(this.getGraphics());
        revalidate();
    }
}
