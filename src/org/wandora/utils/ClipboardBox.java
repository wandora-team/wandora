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
 * 
 *
 * ClipboardBox.java
 *
 * Created on 27. marraskuuta 2004, 13:26
 */

package org.wandora.utils;


import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import org.wandora.utils.transferables.TransferableImage;


/**
 * This class provides methods to modify and retrieve information from the system
 * clipboard.
 *
 * @author  akivela
 */


public class ClipboardBox {
    
    /** Creates a new instance of ClipboardBox */
    public ClipboardBox() {
    }
    
    
    /**
     * Copies the selected text of the specified text component.
     */
    public static void copy(Component c) {
        if(c != null) {
            if(c instanceof JTextComponent) {
                ((JTextComponent) c).copy();
            }
            else {
                System.out.println("Copy event not handled!");
            }
        }
    }
    
    
    /**
     * Cuts the selected text of the specified text component.
     */
    public static void cut(Component c) {
        if(c != null) {
            if(c instanceof JTextComponent) {
                ((JTextComponent) c).cut();
            }
            else {
                System.out.println("Cut event not handled!");
            }
        }        
    }
    
    /**
     * Pastes contents of the clipboard in the specified text component.
     */
    public static void paste(Component c) {
        if(c != null) {
            if(c instanceof JTextComponent) {
                ((JTextComponent) c).paste();
            }
            else {
                System.out.println("Paste event not handled!");
            }
        }       
    }
    
    
    
    
    
    /**
     * Returns the contents of the clipboard if it contains a string.
     * Otherwise returns null.
     */
    public static String getClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if(t == null) return null;
        
        try {
            if(t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                BufferedImage image = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write( image, "png", baos );
                baos.flush();
                byte[] imageBytes = baos.toByteArray();
                baos.close();
                DataURL dataURL = new DataURL("image/png", imageBytes);
                return dataURL.toExternalForm();
            }
            else if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String)t.getTransferData(DataFlavor.stringFlavor);
                return text;
            }
        } catch (UnsupportedFlavorException e) {
        } catch (IOException e) {
        }
        return null;
    }

    
    
    
    /**
     * Sets the contents of the clipboard.
     */
    public static void setClipboard(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection ss = new StringSelection(str);
        if(DataURL.isDataURL(str)) {
            try {
                DataURL dataURL = new DataURL(str);
                if(dataURL.getMimetype().contains("image")) {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataURL.getData()));
                    TransferableImage imageTranferable = new TransferableImage(image, str);
                    clipboard.setContents(imageTranferable, null);
                    return;
                }
            }
            catch(Exception e) {
                // IGNORE
            }
        }
        clipboard.setContents(ss, null);
    }
    
    /**
     * Sets the contents of the clipboard. Calls toString of the specified
     * object and sets the returned string in the clipboard.
     */
    public static void setClipboard(Object o) {
        StringSelection ss = new StringSelection(o.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }
    
    
    
    /**
     * Sets the contents of the clipboard.
     */
    public static void setClipboard(Image image) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        TransferableImage imgSel = new TransferableImage(image);
        clipboard.setContents(imgSel, null);
    }

}
