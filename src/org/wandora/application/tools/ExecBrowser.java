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
 * 
 * ExecBrowser.java
 *
 * Created on 22.6.2009, 11:29
 */



package org.wandora.application.tools;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import java.awt.*;
import java.net.*;
import javax.swing.*;


/**
 * Open a WWW browser with a given URL address.
 *
 * @author akivela
 */



public class ExecBrowser extends AbstractWandoraTool implements WandoraTool, Runnable {
    

	private static final long serialVersionUID = 1L;

	
	private String uri = null;
    
    public ExecBrowser(){
    }
    
    public ExecBrowser(String uri) {
        this.uri=uri;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(uri != null) {
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(uri));
            }
            catch(Exception e) {
                log(e);
            }
        }
        else {
            singleLog("No uri set!");
        }
    }
    
    
    @Override
    public boolean allowMultipleInvocations() {
        return true;
    }

    @Override
    public String getName() {
        return "Open WWW browser";
    }

    @Override
    public String getDescription() {
        return "Open WWW browser with address "+uri;
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/open_browser.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
    

