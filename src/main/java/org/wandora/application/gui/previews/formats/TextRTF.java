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
 */

package org.wandora.application.gui.previews.formats;


import java.awt.Color;
import java.awt.Cursor;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.rtf.RTFEditorKit;

import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.utils.DataURL;


/**
 *
 * @author akivela
 */
public class TextRTF extends Text {
     
    
    public TextRTF(String locator) {
        super(locator);
    }
    
    
    @Override
    protected JComponent getTextComponent(String locator) throws Exception {
        RTFEditorKit rtf = new RTFEditorKit();
        JEditorPane textComponent = new JEditorPane();
        textComponent.setEditorKit(rtf);
        textComponent.setBackground(Color.WHITE);
        
        if(locator.startsWith("file:")) {
            FileInputStream in = new FileInputStream(new URL(locator).getFile());
            rtf.read(in, textComponent.getDocument(), 0);
        }
        else if(DataURL.isDataURL(locator)) {
            DataURL dataUrl = new DataURL(locator);
            ByteArrayInputStream in = new ByteArrayInputStream(dataUrl.getData());
            rtf.read(in, textComponent.getDocument(), 0);
        }
        else {
            InputStream in = new URL(locator).openStream();
            rtf.read(in, textComponent.getDocument(), 0);
        }

        textComponent.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        textComponent.setEditable(false);
        textComponent.setCaretPosition(0);
        return textComponent;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { "text/rtf", "application/rtf" }, 
                new String[] { "rtf" }
        );
    }
}
