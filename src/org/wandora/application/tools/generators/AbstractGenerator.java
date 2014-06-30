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
 * 
 * AbstractGenerator.java
 *
 * Created on 1. kesäkuuta 2007, 10:39
 *
 */


package org.wandora.application.tools.generators;

import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import javax.swing.*;


/**
 *
 * @author akivela
 */
public abstract class AbstractGenerator extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of AbstractGenerator */
    public AbstractGenerator() {
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
