/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * TopicMapFileView.java
 *
 * Created on 11. huhtikuuta 2006, 18:16
 *
 */

package org.wandora.application.gui.filechooser;

import org.wandora.application.gui.*;
import javax.swing.filechooser.*;
import java.io.*;
import javax.swing.*;



/**
 *
 * @author akivela
 */
public class WPRFileView extends FileView {
    
    Icon wprFileIcon = null;

    
    
    /** Creates a new instance of WPRFileView */
    public WPRFileView() {
        try { wprFileIcon = UIBox.getIcon("gui/icons/wpr_file.png"); } catch(Exception e) {}
    }
    
    
    @Override
    public String getTypeDescription(File f) {
        if(f != null) {
            String uppercaseName = f.getName().toUpperCase();
            if(uppercaseName.endsWith(".WPR")) return "Wandora project file";
        }
        return null;
    }
    
    
    
    @Override
    public Icon getIcon(File f) {
        if(f != null) {
            String uppercaseName = f.getName().toUpperCase();
            if(uppercaseName.endsWith(".WPR")) return wprFileIcon;
        }
        return null;
    }
    
}
