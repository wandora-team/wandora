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
 * Search.java
 *
 * Created on August 30, 2004, 3:32 PM
 */

package org.wandora.application.tools;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import javax.swing.*;
import org.wandora.application.gui.search.SearchTopicsFrame;


/**
 *
 * @author  olli, ak
 */
public class Search extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	
	private static SearchTopicsFrame searchFrame = null;

    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(searchFrame == null) {
            searchFrame = new SearchTopicsFrame();
        }
        if(searchFrame != null) {
            searchFrame.setVisible(true);
        }
        else {
            System.out.println("Unable in instantiate SearchTopicsFrame.");
        }
    }

    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/find_topics.png");
    }
    
    @Override
    public String getName() {
        return "Search";
    }

    @Override
    public String getDescription() {
        return "Search for topics.";
    }
    
    public boolean useDefaultGui() {
        return false;
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
}
