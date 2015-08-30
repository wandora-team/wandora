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
 * XML.java
 *
 *
 */

package org.wandora.application.gui.previews.formats;


import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import org.wandora.application.gui.previews.*;
import static org.wandora.application.gui.previews.PreviewUtils.endsWithAny;
import org.wandora.utils.*;


/**
 *
 * @author akivela
 */
public class XML extends Text implements ActionListener, PreviewPanel {

    
    /** Creates a new instance of XML */
    public XML(String locator) {
        super(locator);
    }
    
    
    protected JComponent getTextComponent(String locator) {
        JTextPane textComponent = new JTextPane();
        textComponent.setText(getContent(locator));
        textComponent.setFont(new Font("monospaced", Font.PLAIN, 12));
        textComponent.setEditable(false);
        textComponent.setCaretPosition(0);
        textComponent.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        return textComponent;
    }
    
    
    
    // -------------------------------------------------------------------------
    

    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "application/xml",
                    "text/xml" }, 
                new String[] { 
                    "xml"
                }
        );
    }
    
}
