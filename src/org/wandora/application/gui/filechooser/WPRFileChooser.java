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
 * WPRFileChooser.java
 *
 * Created on 9. huhtikuuta 2006, 19:17
 */

package org.wandora.application.gui.filechooser;

import org.wandora.application.gui.simple.SimpleFileChooser;

/**
 *
 * @author akivela
 */


public class WPRFileChooser extends SimpleFileChooser {
    
    public WPRFileChooser() {
        initialize();
    }
    public WPRFileChooser(String currentPath) {
        super(currentPath);
        initialize();       
    }

    
    
    private void initialize() {
        setFileFilter(new WPRFileFilter());
        setFileView(new WPRFileView());
        setFileSelectionMode(WPRFileChooser.FILES_AND_DIRECTORIES);
    }
}
