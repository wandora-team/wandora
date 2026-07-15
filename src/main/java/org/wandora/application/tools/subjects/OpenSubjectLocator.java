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
 * OpenSubjectLocator.java
 *
 * Created on 12. lokakuuta 2007, 12:31
 *
 */

package org.wandora.application.tools.subjects;



import java.awt.Desktop;
import java.net.URI;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;

/**
 * Opens subject locator in an external application provided by the operating
 * system. Usually subject identifiers matching URL schema are opened in a
 * default WWW browser.
 *
 * @author akivela
 */
public class OpenSubjectLocator extends AbstractWandoraTool implements WandoraTool, Runnable {

	private static final long serialVersionUID = 1L;

	public OpenSubjectLocator() {
    }
    public OpenSubjectLocator(Context proposedContext) {
        setContext(proposedContext);
    }
    
    @Override
    public void execute(Wandora admin, Context context) {
        try {
            // setDefaultLogger();
            if(admin != null) {
                Iterator contextTopics = getContext().getContextObjects();
                int openCount = 0;
                int tryCount = 0;
                boolean openAll = false;
                boolean openNext = true;
                boolean forceStop = false;
                
                if(contextTopics == null) return;
                while(contextTopics.hasNext() && !forceStop) {
                    tryCount++;
                    Topic t = (Topic) (contextTopics.next());
                    Locator sl = t.getSubjectLocator();
                    if(sl != null) {
                        String slString = sl.toExternalForm();
                        if(openCount > 0 && !openAll) {
                            //setState(INVISIBLE);
                            int a = WandoraOptionPane.showConfirmDialog(admin, "Would you like to open topic's '"+getTopicName(t)+"' subject locator\n"+slString, "Open SL?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                            //setState(VISIBLE);
                            if(a == WandoraOptionPane.YES_TO_ALL_OPTION) openAll = true;
                            else if(a == WandoraOptionPane.YES_OPTION) openNext = true;
                            else if(a == WandoraOptionPane.CANCEL_OPTION) { forceStop=true; continue; }
                            else if(a == WandoraOptionPane.CLOSED_OPTION) { forceStop=true; continue; }
                        }
                        if(openAll || openNext) {
                            //log("Opening subject locator '"+slString+"'.");
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.browse(new URI(slString));
                            }
                            catch(java.net.URISyntaxException e) {
                                log("Subject locator is not valid URI. Unable to open subject in external browser.");
                            }
                            catch(Exception e) {
                                log(e);
                            }
                            try { Thread.sleep(200); } 
                            catch(Exception e) {} // WAKEUP!
                            openNext = false;
                            openCount++;
                        }
                    }
                    else {
                        log("No valid subject locator in topic '"+getTopicName(t)+"'.");
                    }
                }
                if(tryCount > 1 && openCount == 0) {
                    log("Context didn't contain valid subject locators.");
                }
            }
            else {
                log("Error: Wandora application is not available!");
            }
        }
        catch(Exception e) {
            log(e);
        }
        //setState(WAIT);
    }
    
    
    @Override
    public String getName() {
        return "Open subject locator";
    }

    @Override
    public String getDescription() {
        return "Open subject locator of context topics in external application.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
