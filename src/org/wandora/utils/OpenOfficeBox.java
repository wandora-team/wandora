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

package org.wandora.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.odftoolkit.simple.TextDocument;

/**
 *
 * @author akivela
 */
public class OpenOfficeBox {
    
    
    
    
    public static String getText(URL url) {
        try {
            TextDocument doc = TextDocument.loadDocument(url.openStream());
            String text = doc.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static String getDocxText(File file) {
        try {
            TextDocument doc = TextDocument.loadDocument(file);
            String text = doc.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    

    public static String getText(InputStream is) {
        try {
            TextDocument doc = TextDocument.loadDocument(is);
            String text = doc.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
}
