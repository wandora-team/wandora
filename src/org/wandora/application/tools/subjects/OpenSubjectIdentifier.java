/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * OpenSubjectIdentifier.java
 *
 * Created on 23. lokakuuta 2007, 16:15
 *
 */

package org.wandora.application.tools.subjects;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.*;

import java.util.*;
import java.awt.*;
import java.net.*;


/**
 * Opens subject identifiers in an external application provided by the operating
 * system. Usually subject identifiers matching URL schema are opened in a
 * default WWW browser.
 *
 * @author akivela
 */   
public class OpenSubjectIdentifier extends AbstractWandoraTool implements WandoraTool, Runnable {
    

    public OpenSubjectIdentifier() {
    }
    public OpenSubjectIdentifier(Context proposedContext) {
        setContext(proposedContext);
    }
    
    @Override
    public void execute(Wandora admin, Context context) {
        boolean errors = false;
        // setDefaultLogger();
        try {
            if(admin != null) {
                Iterator contextSIs = null;
                if(context instanceof SIContext) {
                    contextSIs = getContext().getContextObjects();
                }
                else if(context instanceof LayeredTopicContext) {
                    HashSet<Locator> siSet = new LinkedHashSet();
                    Iterator contextTopics = getContext().getContextObjects();
                    if(contextTopics == null) return;
                    while(contextTopics.hasNext() && !forceStop()) {
                        Topic t = (Topic) (contextTopics.next());
                        if(t != null && !t.isRemoved()) {
                            Collection<Locator> sis = t.getSubjectIdentifiers();
                            for(Locator si : sis) {
                                siSet.add(si);
                            }
                        }
                    }
                    contextSIs = siSet.iterator();
                }

                int openCount = 0;
                boolean openAll = false;
                boolean openNext = true;
                boolean forceStop = false;
                
                if(contextSIs == null) return;
                while(contextSIs.hasNext() && !forceStop) {
                    Locator locator = (Locator) (contextSIs.next());
                    if(locator != null) {
                        String siString = locator.toExternalForm();
                        if(openCount > 0 && !openAll) {
                            setState(INVISIBLE);
                            int a = WandoraOptionPane.showConfirmDialog(admin, "Would you like to open subject identifier '"+siString+"'?", "Open SI?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                            setState(VISIBLE);
                            if(a == WandoraOptionPane.YES_TO_ALL_OPTION) openAll = true;
                            else if(a == WandoraOptionPane.YES_OPTION) openNext = true;
                            else if(a == WandoraOptionPane.CANCEL_OPTION) { forceStop=true; continue; }
                            else if(a == WandoraOptionPane.CLOSED_OPTION) { forceStop=true; continue; }
                        }
                        if(openAll || openNext) {
                            //log("Opening subject identifier '"+siString+"'.");
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.browse(new URI(siString));
                            }
                            catch(java.net.URISyntaxException e) {
                                errors = true;
                                log("Subject identifier is not valid URI. Unable to open subject identifier in external browser.");
                            }
                            catch(Exception e) {
                                errors = true;
                                log(e);
                            }
                            try { Thread.currentThread().sleep(200); } 
                            catch(Exception e) {} // WAKEUP!
                            openNext = false;
                            openCount++;
                        }
                    }
                    else {
                        errors = true;
                        log("No valid subject identifier available.");
                    }             
                }
                if(openCount == 0) {
                    errors = true;
                    log("Context didn't contain valid subject identifiers.");
                }
            }
            else {
                errors = true;
                log("Error: Wandora application is not available!");
            }
        }
        catch(Exception e) {
            log(e);
        }
        //if(errors) setState(WAIT);
        //else setState(CLOSE);
    }
    
    

    @Override
    public String getName() {
        return "Open subject identifier";
    }

    @Override
    public String getDescription() {
        return "Open subject identifier in external application.";
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
