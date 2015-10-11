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
 * PasteSubjectIdentifiers.java
 *
 * Created on 6. tammikuuta 2005, 15:12
 */

package org.wandora.application.tools.subjects;



import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.utils.*;
import java.util.*;
import java.net.*;



/**
 * Adds URLs in system clipboard to a topic as subject identifiers. If
 * topic map already contains topics with added subjects the application
 * merges all topics.
 *
 * @author akivela
 */
public class PasteSubjectIdentifiers extends AbstractWandoraTool implements WandoraTool {
    
    public boolean confirm = true;
    
    
    public PasteSubjectIdentifiers() {}
    public PasteSubjectIdentifiers(Context context) {
        setContext(context);
    }
    
    
    
    public void execute(Wandora admin, Context context) {
        Iterator topics = null;
        if(context instanceof SIContext) {
            Iterator sis = context.getContextObjects();
            TopicMap topicmap = admin.getTopicMap();
            ArrayList l = new ArrayList();
            Locator si = null;
            Topic t = null;
            while(sis.hasNext()) {
                try {
                    si = (Locator) sis.next();
                    t = topicmap.getTopic(si);
                    if(!l.contains(t)) {
                        l.add(t);
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            topics = l.iterator();
        }
        else {
            topics = context.getContextObjects();
        }
        
        if(topics == null || !topics.hasNext()) return;
        
        
        
        Collection<Locator> SIs = solveSIs(admin);
        if(SIs == null) return;

        Topic topic = null;
        Topic existingTopic = null;
        Iterator<Locator> SIIterator = SIs.iterator();
        Locator locator = null;
        int answer = 0;
        boolean shouldAdd = true;

        ArrayList<Object> dt = new ArrayList<>();
        while(topics.hasNext() && !forceStop()) {
            dt.add(topics.next());
        }
        topics = dt.iterator();
        
        while(topics.hasNext() && !forceStop()) {
            topic = (Topic) topics.next();
            try {
                if(topic != null && !topic.isRemoved()) {
                    while(SIIterator.hasNext()) {
                        locator = SIIterator.next();
                        existingTopic = topic.getTopicMap().getTopic(locator);
                        shouldAdd = true;
                        if(confirm && existingTopic != null && !existingTopic.equals(topic)) {
                            answer = WandoraOptionPane.showConfirmDialog(admin,"There exists another topic with subject identifier '" + locator.toExternalForm() + "'. Merge occurs if subject identifier is added to the topic. Do you want to add the subject identifier to the topic?","Confirm merge", WandoraOptionPane.YES_NO_OPTION);
                            if(answer != WandoraOptionPane.YES_OPTION) shouldAdd = false;
                        }
                        if(shouldAdd) {
                            topic.addSubjectIdentifier(locator);
                        }
                    }
                }
            }
            catch (Exception e) {
                answer = WandoraOptionPane.showConfirmDialog(admin,"Exception '" + e.getMessage() + "' occurred while adding subject identifier '" + locator.toExternalForm() + "'. Would you like to continue with the operation?","Continue?", WandoraOptionPane.YES_NO_OPTION);
                if(answer != WandoraOptionPane.YES_OPTION) return;
            }
        }
    }
    


    @Override
    public String getName() {
        return "Paste subject identifiers";
    }

    @Override
    public String getDescription() {
        return "Adds subject identifiers in clipboard "+
               "to current topics. If same subject identifier is added to many topics, the "+
               "topics are merged automatically by Wandora.";
    }
    
    
    public Collection<Locator> solveSIs(Wandora admin) {
        ArrayList<Locator> SIs = new ArrayList<>();
        String text = ClipboardBox.getClipboard();
        StringTokenizer st = new StringTokenizer(text, "\n");
        String sis = null;
        Locator sil = null;
        while(st.hasMoreTokens()) {
            try {
                sis = st.nextToken();
                sis = Textbox.trimExtraSpaces(sis);
                sil = new Locator(sis);
                URL siurl = new URL(sis); // throws an exception if sis malformed!
                SIs.add(sil);
            }
            catch(java.net.MalformedURLException mue){
                int answer = WandoraOptionPane.showConfirmDialog(admin,"Malformed subject identifier given: '" + sis + "'. Would you like to continue with the operation?", "Malformed subject identifier", WandoraOptionPane.YES_NO_OPTION);
                if(answer != WandoraOptionPane.YES_OPTION) return null;
            }
            catch(Exception e) {
                log(e);
            }
        }
        return SIs;
    }
    
    
    
}
