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
 * LTMFileFilter.java
 *
 *
 */

package org.wandora.application.gui.filechooser;

import java.io.File;

/**
 *
 * @author akivela
 */


public class LTMFileFilter extends javax.swing.filechooser.FileFilter {
    
    /** Creates a new instance of LTMFileFilter */
    public LTMFileFilter() {
    }
    


    @Override
    public boolean accept(File f) {
        if(f != null) {
            if (f.isDirectory()) {
                return true;
            }
            String uppercaseName = f.getName().toUpperCase();
            if(uppercaseName.endsWith(".LTM")) {
                return true;
            }
        }
        return false;
    }
    

    //The description of this filter
    @Override
    public String getDescription() {
        return "LTM topic map";
    }
    
}
