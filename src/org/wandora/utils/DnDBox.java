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

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
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
        try{
             f = new DataFlavor("text/uri-list; class=java.lang.String");
        }catch(ClassNotFoundException cnfe){cnfe.printStackTrace();}
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
                if(e.isDataFlavorSupported(stringFlavor)) 
                    data=(String)tr.getTransferData(stringFlavor);
                else
                    data=(String)tr.getTransferData(uriListFlavor);

                try{
                    String[] split=data.split("\n");
                    java.net.URI[] uris=new java.net.URI[split.length];
                    for(int i=0;i<split.length;i++){
                        uris[i]=new java.net.URI(split[i].trim());
                    }
                    java.util.List<File> files=new java.util.ArrayList<File>();
                    for(int i=0;i<uris.length;i++){
                        try{
                            files.add(new File(uris[i]));
                        }
                        catch(IllegalArgumentException iae){
                            iae.printStackTrace();
                        }
                    }
                    e.dropComplete(true);
                    return files;
                }
                catch(java.net.URISyntaxException ue){
                    ue.printStackTrace();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                e.dropComplete(true);
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
        return null;
    }
    
}
