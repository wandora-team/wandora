/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * AnyTopiMapFileFilter.java
 *
 * Created on 11. huhtikuuta 2006, 18:01
 *
 */

package org.wandora.application.gui.filechooser;


import java.io.*;


/**
 *
 * @author akivela
 */
public class AnyTopiMapFileFilter extends javax.swing.filechooser.FileFilter {
    
    /** Creates a new instance of AnyTopiMapFileFilter */
    public AnyTopiMapFileFilter() {
    }
    


    @Override
    public boolean accept(File f) {
        if(f != null) {
            if(f.isDirectory()) {
                return true;
            }
            String uppercaseName = f.getName().toUpperCase();
            if(uppercaseName.endsWith(".XTM10") ||
               uppercaseName.endsWith(".XTM1") ||
               uppercaseName.endsWith(".XTM20") ||
               uppercaseName.endsWith(".XTM2") ||
               uppercaseName.endsWith(".XTM") ||
               uppercaseName.endsWith(".LTM") ||
               uppercaseName.endsWith(".JTM")) {
                    return true;
            }
        }
        return false;
    }
    

    //The description of this filter
    @Override
    public String getDescription() {
        return "Any topic map";
    }
    
}
