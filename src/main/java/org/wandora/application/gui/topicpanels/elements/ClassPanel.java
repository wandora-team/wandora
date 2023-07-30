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
 * ClassPanel.java
 *
 * Created on 2008-11-14
 *
 */


package org.wandora.application.gui.topicpanels.elements;

import java.awt.GridBagConstraints;
import java.util.HashSet;

import javax.swing.JPanel;

import org.wandora.application.Wandora;
import org.wandora.application.gui.table.ClassTable;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;


/**
 *
 * @author akivela
 */
public class ClassPanel extends JPanel {

    private Topic topic = null;
    private Wandora admin = null;
    protected HashSet<Locator> visibleTopics;
    
    
    public ClassPanel(Wandora admin, Topic t) {
        this.topic = t;
        this.admin = admin;
    }
    
    
    public void setTopic(Topic t) {
        this.topic = t;
    }
    
    
    
    public Topic getTopic() {
        return topic;
    }
    
    
    
    
    public void initialize() {
        GridBagConstraints gbc;
        try {
            this.removeAll();
            /*
            Collection types=TMBox.sortTopics(topic.getTypes(),"en");
            classesPanel.setLayout(new java.awt.GridLayout(types.size(),2));
            iter=types.iterator();
            while(iter.hasNext()){
                final Topic cls=(Topic)iter.next();
                classesPanel.add(new TopicLink(cls,parent));
                javax.swing.JButton button=new javax.swing.JButton("Remove");
                button.addActionListener(new java.awt.event.ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent e){
                        topic.removeType(cls);
                        parent.refreshTopic();
                    }
                });
                classesPanel.add(button);
            }
            */
            this.setLayout(new java.awt.GridBagLayout());
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.weightx=1.0;
            this.add(new ClassTable(topic,admin),gbc);
            
            {
                for(Topic t : topic.getTypes()){
                    visibleTopics.addAll(t.getSubjectIdentifiers());
                }
            }
        }
        catch(Exception e) {
            admin.handleError(e);
        }
    }
    
    
}
