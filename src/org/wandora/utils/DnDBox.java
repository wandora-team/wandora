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
 */
package org.wandora.utils;

import java.awt.Image;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 *
 * @author olli
 */
public class DnDBox {

    // At least Ubuntu uses this data flavor
    public static final DataFlavor uriListFlavor;
    static {
        DataFlavor f=null;
        try {
             f = new DataFlavor("text/uri-list; class=java.lang.String");
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        uriListFlavor=f;
    }
    
    
    public static List<File> acceptFileList(java.awt.dnd.DropTargetDropEvent e) {
        try {
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            Transferable tr = e.getTransferable();
            if(e.isDataFlavorSupported(fileListFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                java.util.List<File> files = (java.util.List<File>) tr.getTransferData(fileListFlavor);
                e.dropComplete(true);
                return files;
            }
            else if(e.isDataFlavorSupported(stringFlavor) ||
                    e.isDataFlavorSupported(uriListFlavor)) {                   
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String data = null;
                if(e.isDataFlavorSupported(stringFlavor)) {
                    data = (String)tr.getTransferData(stringFlavor);
                }
                else {
                    data = (String)tr.getTransferData(uriListFlavor);
                }
                try {
                    String[] split = data.split("\n");
                    ArrayList<URI> uris = new ArrayList();
                    for(int i=0; i<split.length; i++){
                        try {
                            URI uri = new URI(split[i].trim());
                            uris.add( uri );
                        }
                        catch(Exception ex) {
                            // Silently ignore illegal URIs.
                        }
                    }
                    java.util.List<File> files = new java.util.ArrayList<File>();
                    for(URI uri : uris) {
                        try{
                            files.add(new File(uri));
                        }
                        catch(Exception exc){
                            // Silently ignore illegal file URIs.
                        }
                    }
                    if(!files.isEmpty()) {
                        e.dropComplete(true);
                    }
                    return files;
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                return new ArrayList<File>();
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
        return null;
    }
    
    
    public static Image acceptImage(java.awt.dnd.DropTargetDropEvent e) {
        try {
            Transferable tr = e.getTransferable();
            if(e.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Image image = (Image) tr.getTransferData(DataFlavor.imageFlavor);
                e.dropComplete(true);
                return image;
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
        return null;
    }
    
}
