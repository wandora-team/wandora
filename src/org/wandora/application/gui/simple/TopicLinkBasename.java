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
 * TopicLinkBasename.java
 *
 * Created on 14. joulukuuta 2004, 19:20
 */

package org.wandora.application.gui.simple;




import java.awt.Dimension;
import org.wandora.topicmap.*;
import java.awt.event.*;
import org.wandora.application.*;



/**
 *
 * @author  akivela
 */
public class TopicLinkBasename extends SimpleLabel {
    
    
    protected Wandora parent;
    protected Locator locator;
    protected Topic topic;
    
    
    
    /** Creates a new instance of TopicLink */
    public TopicLinkBasename(Topic t, Wandora wandora) {
        super();
        try {
            this.topic = t;
            this.locator=(Locator)topic.getOneSubjectIdentifier();
            this.parent=wandora;
            String basename = topic.getBaseName();
            if(basename != null) {
                this.setText(basename);
            }
            else {
                if(!topic.isRemoved()) {
                    this.setText(locator.toExternalForm());
                }
                else {
                    this.setText("[removed]");
                }
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            this.setText("[Exception retrieving name]");
        }

        java.awt.Color c=parent.topicHilights.get(t);
        if(c==null) c=parent.topicHilights.getLayerColor(t);
        if(c!=null){
              this.setForeground(c);
//            this.setBackground(c);
//            this.setOpaque(true);
        }
        this.setVisible(true);
    }
        
        
    
    
    public Topic getTopic() {
        return topic;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()>=2) {
            //System.out.println("link pressed");
            parent.applyChangesAndOpen(locator);
        }
    }

    
}
