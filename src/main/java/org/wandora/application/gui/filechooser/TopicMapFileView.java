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
public class TopicMapFileView extends FileView {
    
    Icon xtmFileIcon = null;
    Icon ltmFileIcon = null;
    Icon jtmFileIcon = null;
    
    
    /** Creates a new instance of TopicMapFileView */
    public TopicMapFileView() {
        try { xtmFileIcon = UIBox.getIcon("gui/icons/xtm_topicmap_file.png"); } catch(Exception e) {}
        try { ltmFileIcon = UIBox.getIcon("gui/icons/ltm_topicmap_file.png"); } catch(Exception e) {}
        try { jtmFileIcon = UIBox.getIcon("gui/icons/jtm_topicmap_file.png"); } catch(Exception e) {}
    }
    
    
    @Override
    public String getTypeDescription(File f) {
        if(f != null) {
            String uppercaseName = f.getName().toUpperCase();
            if(uppercaseName.endsWith(".XTM")) return "XML topic map file";
            if(uppercaseName.endsWith(".XTM1")) return "XML 1.0 topic map file";
            if(uppercaseName.endsWith(".XTM10")) return "XML 1.0 topic map file";
            if(uppercaseName.endsWith(".XTM2")) return "XML 2.0 topic map file";
            if(uppercaseName.endsWith(".XTM20")) return "XML 2.0 topic map file";
            if(uppercaseName.endsWith(".LTM")) return "Linear topic map file";
            if(uppercaseName.endsWith(".JTM")) return "JSON topic map file";
        }
        return null;
    }
    
    
    
    @Override
    public Icon getIcon(File f) {
        if(f != null) {
            String uppercaseName = f.getName().toUpperCase();
            if(uppercaseName.endsWith(".XTM")) return xtmFileIcon;
            if(uppercaseName.endsWith(".XTM1")) return xtmFileIcon;
            if(uppercaseName.endsWith(".XTM10")) return xtmFileIcon;
            if(uppercaseName.endsWith(".XTM2")) return xtmFileIcon;
            if(uppercaseName.endsWith(".XTM20")) return xtmFileIcon;
            if(uppercaseName.endsWith(".LTM")) return ltmFileIcon;
            if(uppercaseName.endsWith(".JTM")) return jtmFileIcon;
        }
        return null;
    }
    
}
