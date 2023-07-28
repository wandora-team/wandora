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
 * 
 * SystemClipboard.java
 *
 * Created on 9. marraskuuta 2005, 18:56
 *
 */

package org.wandora.application.tools;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import javax.swing.*;



/**
 * WandoraTool that transfers Wandora application data to system clipboard and vice versa.
 * Transfer direction is selected with an argument to constructor.
 * 
 * @author akivela
 */
public class SystemClipboard extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public static final int PASTE = 1;
    public static final int COPY = 2;
    public static final int CUT = 4;
    
    private int operation = COPY;
    

    public SystemClipboard() {
    }
    public SystemClipboard(int operation) {
        this.operation = operation;
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        Object focusOwner = wandora.getFocusOwner();
        if(focusOwner != null) {
            if(focusOwner instanceof Clipboardable) {
                Clipboardable clipboardable = (Clipboardable) focusOwner;
                switch(operation) {
                    case COPY: {
                        clipboardable.copy();
                        break;
                    }
                    case CUT: {
                        clipboardable.cut();
                        break;
                    }
                    case PASTE: {
                        clipboardable.paste();
                        break;
                    }
                }
            }
        }
    }


    @Override
    public Icon getIcon() {
        switch(operation) {
            case COPY: {
                return UIBox.getIcon("gui/icons/copy.png");
            }
            case CUT: {
                return UIBox.getIcon("gui/icons/cut.png");
            }
            case PASTE: {
                return UIBox.getIcon("gui/icons/paste.png");
            }
        }
        return super.getIcon();
    }



    @Override
    public String getName() {
        switch(operation) {
            case COPY: {
                return "Copy";
            }
            case CUT: {
                return "Cut";
            }
            case PASTE: {
                return "Paste";
            }
        }
        return "System clipboard";
    }

    @Override
    public String getDescription() {
        switch(operation) {
            case COPY: {
                return "Copy selection of current UI element to the system clipboard.";
            }
            case CUT: {
                return "Cut selection of current UI element to the system clipboard.";
            }
            case PASTE: {
                return "Paste data on system clipboard to Wandora's active UI element.";
            }
        }
        return "Transfers Wandora application originated data to system clipboard and vice versa.";
    }

    @Override
    public boolean requiresRefresh() {
        switch(operation) {
            case COPY: {
                return false;
            }
            case CUT: {
                return true;
            }
            case PASTE: {
                return true;
            }
        }
        return true;
    }


}
