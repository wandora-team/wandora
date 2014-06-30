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

package org.wandora.application.tools.extractors.reddit;

/**
 *
 * @author Eero
 */
import java.io.File;
import java.net.URL;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import static org.wandora.application.WandoraToolLogger.CLOSE;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;


public class RedditExtractor extends AbstractWandoraTool{
    
    private RedditExtractorUI ui = null;

    @Override
    public String getName() {
        return "Reddit API Extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts topics and associations from reddit API. "+
               "A personal api-key is required for the API access.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_reddit.png");
    }

    private final String[] contentTypes=new String[] { 
        "text/plain", "text/json", "application/json" 
    };



    public String[] getContentTypes() {
        return contentTypes;
    }
    public boolean useURLCrawler() {
        return false;
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            if(ui == null) {
                ui = new RedditExtractorUI();
            }

            ui.open(wandora, context);

            if(ui.wasAccepted()) {
                WandoraTool[] extrs = null;
                try{
                     extrs = ui.getExtractors(this);
                } catch(Exception e) {
                    log(e.getMessage());
                    return;
                }
                if(extrs != null && extrs.length > 0) {
                    setDefaultLogger();
                    int c = 0;
                    
                    log("Performing the API query...");
                    for(int i=0; i<extrs.length && !forceStop(); i++) {
                        try {
                            WandoraTool e = extrs[i];
                            e.setToolLogger(getDefaultLogger());
                            e.execute(wandora,context);
                            c++;
                        }
                        catch(Exception e) {
                            log(e);
                        }
                        
                    }
                    log("Done.");
                }
                else {
                    log("Couldn't find a suitable subextractor to perform or "
                       +"there was an error with an extractor.");
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            singleLog(e);
        } finally {
            if(ui != null && ui.wasAccepted()) setState(WAIT);
            else setState(WAIT);
        }
        
       
    }
    
    // -------------------------------------------------------------------------
    
}
