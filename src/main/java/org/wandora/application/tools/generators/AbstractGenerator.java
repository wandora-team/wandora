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
 * AbstractGenerator.java
 *
 * Created on 1.6.2007, 10:39
 *
 */


package org.wandora.application.tools.generators;

import javax.swing.Icon;

import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;


/**
 *
 * @author akivela
 */
public abstract class AbstractGenerator extends AbstractWandoraTool implements WandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	/** Creates a new instance of AbstractGenerator */
    public AbstractGenerator() {
    }
    
    
    public Topic getOrCreateTopic(TopicMap map, String si) {
        return getOrCreateTopic(map, si, null);
    }
    

    
    public Topic getOrCreateTopic(TopicMap map, String si, String basename) {
        Topic topic = null;
        try {
            topic = map.getTopic(si);
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                if(basename != null && basename.length() > 0) topic.setBaseName(basename);
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        return topic;
    }
    
    
    public Topic getOrCreateTopic(TopicMap map, String si, String basename, Topic type) {
        Topic topic = null;
        try {
            topic = map.getTopic(si);
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                if(basename != null && basename.length() > 0) topic.setBaseName(basename);
                if(type != null && type.isRemoved()) topic.addType(type);
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        return topic;
    }
    
    
    public void makeSuperclassSubclass(TopicMap map, Topic superclass, Topic subclass) {
        try {
            if(map == null || superclass == null || subclass == null) return;
            Topic associationType = getOrCreateTopic(map, XTMPSI.SUPERCLASS_SUBCLASS);
            Topic superRole = getOrCreateTopic(map, XTMPSI.SUPERCLASS);
            Topic subRole = getOrCreateTopic(map, XTMPSI.SUBCLASS);

            if(associationType != null && superRole != null && subRole != null) {
                Association a = map.createAssociation(associationType);
                a.addPlayer(subclass, subRole);
                a.addPlayer(superclass, superRole);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createGeneratorType();
    }
    @Override
    public String getName() {
        return "Abstract Generator Tool";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/generate.png");
    }

}
