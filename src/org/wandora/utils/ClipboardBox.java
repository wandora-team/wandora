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
import java.io.*;


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
    
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
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
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
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
        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    }
    
    
    
    
    
    // This class is used to hold an image while on the clipboard.
    public static class ImageSelection implements Transferable {
        private Image image;
    
        public ImageSelection(Image image) {
            this.image = image;
        }
    
        // Returns supported flavors
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }
    
        // Returns true if flavor is supported
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }
    
        // Returns image
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
    
    
}
/*




*/