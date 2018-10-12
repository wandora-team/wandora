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
 * PasteClasses.java
 *
 * Created on 7. marraskuuta 2005, 10:39
 */

package org.wandora.application.tools;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;



/**
 * Inject clipboard topics i.e. base names to current topic as classes.
 *
 * @author akivela
 */
public class PasteClasses extends PasteTopics implements WandoraTool {
    
    
	private static final long serialVersionUID = 1L;



	public PasteClasses() {
        super();
    }
    public PasteClasses(int includeOrders) {
        super(includeOrders);
    }
    public PasteClasses(int includeOrders, int pasteOrders) {
        super(includeOrders, pasteOrders);
    }
    public PasteClasses(Context preferredContext) {
        setContext(preferredContext);        
    }
    public PasteClasses(Context preferredContext, int includeOrders) {
        super(includeOrders);
        setContext(preferredContext);        
    }
    public PasteClasses(Context preferredContext, int includeOrders, int pasteOrders) {
        super(includeOrders, pasteOrders);
        setContext(preferredContext);        
    }
    
    
    @Override
    public String getName() {
        return "Paste classes";
    }


    @Override
    public String getDescription() {
        return "Inject clipboard topics i.e. basenames "+
               "to current topic as classes.";
    }
    

    @Override
    public void initialize(Wandora wandora) {
        super.initialize(wandora);
        ACCEPT_UNKNOWN_TOPICS = true;
    }
    
    
    
    @Override
    public void topicPrologue(Topic topic) {
        if(topic != null && currentTopic != null) {
            try{
                currentTopic.addType(topic);
            }
            catch(TopicMapException tme){
                log(tme);
            }
        }
    }
    
    
    
}
