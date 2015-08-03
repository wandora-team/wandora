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
 */
package org.wandora.utils.transferables;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author akivela
 */
public class TransferableImage implements Transferable {

    private Image transferableImage = null;
    private String transferableString = null;

    
    public TransferableImage( Image transferableImage ) {
        this.transferableImage = transferableImage;
    }
    
    
    public TransferableImage( Image transferableImage, String transferableString ) {
        this.transferableImage = transferableImage;
        this.transferableString = transferableString;
    }
    
    

    @Override
    public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
        if(flavor.equals( DataFlavor.imageFlavor ) && transferableImage != null) {
            return transferableImage;
        }
        else if(flavor.equals( DataFlavor.stringFlavor ) && transferableString != null) {
            return transferableString;
        }
        else {
            throw new UnsupportedFlavorException( flavor );
        }
    }

    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = null;
        if(transferableString != null) {
            flavors = new DataFlavor[2];
            flavors[0] = DataFlavor.imageFlavor;
            flavors[1] = DataFlavor.stringFlavor;
        }
        else {
            flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.imageFlavor;
        }
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
}
