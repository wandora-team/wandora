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
 */


package org.wandora.utils.transferables;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class TransferableDataURL implements Transferable {

    private DataURL transferableData = null;

    
    public TransferableDataURL( DataURL transferableData ) {
        this.transferableData = transferableData;
    }
    

    @Override
    public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
        if(transferableData != null) {
            if(flavor.getMimeType().equalsIgnoreCase(transferableData.getMimetype())) {
                return transferableData.getData();
            }
            else if(flavor.equals( DataFlavor.stringFlavor )) {
                return transferableData.toExternalForm();
            }
        }
        throw new UnsupportedFlavorException( flavor );
    }

    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = null;
        if(transferableData != null) {
            try {
                flavors = new DataFlavor[2];
                flavors[0] = new DataURLFlavor(transferableData.getMimetype());
                flavors[1] = DataFlavor.stringFlavor;
                return flavors;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        flavors = new DataFlavor[0];
        return flavors;
    }

    
    @Override
    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        DataFlavor[] flavors = getTransferDataFlavors();
        for( int i = 0; i < flavors.length; i++) {
            if( flavor.equals( flavors[ i ] )) {
                return true;
            }
        }

        return false;
    }

    
    
    
    public class DataURLFlavor extends DataFlavor {
        public DataURLFlavor(String mimeType) throws ClassNotFoundException {
            super(mimeType);
        }

        
        @Override
        public Class getRepresentationClass() {
            try {
                Class c = Class.forName("[B");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
        
        @Override
        public boolean isRepresentationClassReader() {
            return false;
        }
        
        @Override
        public boolean isRepresentationClassCharBuffer() {
            return false;
        }
        
        @Override
        public boolean isRepresentationClassByteBuffer() {
            return false;
        }
        
        public boolean isFlavorCharsetTextType() {
            return false;
        }
    }
}
