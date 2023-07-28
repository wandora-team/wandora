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
 * PasteInstances.java
 *
 * Created on 6. tammikuuta 2005, 12:56
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;




/**
 * Inject clipboard topics i.e. base names to current topic as instances.
 *
 * @author  akivela
 */
public class PasteInstances extends PasteTopics implements WandoraTool {
    

	private static final long serialVersionUID = 1L;



	public PasteInstances() {}
    public PasteInstances(int includeOrders) {
        super(includeOrders);
    }
    public PasteInstances(int includeOrders, int pasteOrders) {
        super(includeOrders, pasteOrders);
    }
    public PasteInstances(Context preferredContext) {
        setContext(preferredContext);        
    }
    public PasteInstances(Context preferredContext, int includeOrders) {
        super(includeOrders);
        setContext(preferredContext);        
    }
    public PasteInstances(Context preferredContext, int includeOrders, int pasteOrders) {
        super(includeOrders, pasteOrders);
        setContext(preferredContext);        
    }
    
    
    
    @Override
    public String getName() {
        return "Paste instances";
    }

    @Override
    public String getDescription() {
        return "Inject clipboard topics to selected topic as instances.";
    }

    @Override
    public void initialize(Wandora admin) {
        super.initialize(admin);
        ACCEPT_UNKNOWN_TOPICS = true;
    }
    
    
    
    @Override
    public void topicPrologue(Topic topic) {
        if(topic != null && currentTopic != null) {
            try {
                topic.addType(currentTopic);
            } catch(TopicMapException tme){
                log(tme);
            }
        }
    }
    
    

}
