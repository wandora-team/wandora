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
 */



package org.wandora.application.tools.webview;

import java.awt.Desktop;
import java.net.URI;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class OpenWebLocationInExternalBrowser extends AbstractWebViewTool {
    
        
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        try {
            String location = getWebLocation(context);
            if(location != null && location.length() > 0) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(location));
            }
        }
        catch(Exception ex) {
            log(ex);
        }
    }
    
    
   @Override
    public String getDescription() {
        return "Open current web location in external web browser application.";
    }
}
