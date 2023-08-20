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
 * MixedTopicGuiWrapper.java
 *
 */

package org.wandora.application.gui;


import java.net.URL;

import javax.swing.tree.TreePath;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli, akivela
 */


public class MixedTopicGuiWrapper {

    public static final String TOPIC_RENDERS_OPTION_KEY = "gui.topicRenders";

    public static final int TOPIC_RENDERS_SL = 100;
    public static final int TOPIC_RENDERS_SI = 110;
    public static final int TOPIC_RENDERS_SI_WITHOUT_DOMAIN = 120;
    public static final int TOPIC_RENDERS_BASENAME = 130;
    public static final int TOPIC_RENDERS_BASENAME_WITH_SL_ICON = 140;

    public static final String PROCESSING_TYPE="PROCESSING_TYPE";

    public Object content;
    public String icon;
    public String associationType;
    public TreePath path;

    
    
    
    
    
    public MixedTopicGuiWrapper(Object content, String icon,String associationType,TreePath parent) {
        this.content=content;
        this.icon=icon;
        this.associationType=associationType;
        if(parent==null) path=new TreePath(this);
        else path=parent.pathByAddingChild(this);
    }
    public MixedTopicGuiWrapper(Object content, String icon) {
        this(content,icon,"",null);
    }
    public MixedTopicGuiWrapper(Object content) {
        this(content,null,"",null);
    }

    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        try {
            if(associationType.equals(PROCESSING_TYPE))return "Processing...";
            else if(content == null) return "[null]";
            else if(content instanceof Topic) {
                Topic topic = (Topic) content;
                if(topic.isRemoved()) {
                    return "[removed]";
                }
                else {
                    if(topic.getBaseName() != null) {
                        return topic.getBaseName();
                    }
                    else {
                        return topic.getOneSubjectIdentifier().toExternalForm();
                    }
                }
            }
            else {
                return content.toString();
            }
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION
            return "[Exception retrieving name]";
        }
    }


    public String toString(int stringType) throws TopicMapException {
        try {
            if(associationType.equals(PROCESSING_TYPE)) return "Processing...";
            else if(content == null) return "[null]";
            else if(content instanceof Topic) {
                Topic topic=(Topic)content;

                if(topic.isRemoved()) return "[removed]";
                else {
                    switch(stringType) {
                        case TOPIC_RENDERS_BASENAME: {
                            if(topic.getBaseName() == null) {
                                return topic.getOneSubjectIdentifier().toExternalForm();
                            }
                            else { 
                                return topic.getBaseName(); 
                            }
                        }
                        case TOPIC_RENDERS_SI: {
                            return topic.getOneSubjectIdentifier().toExternalForm();
                        }
                        case TOPIC_RENDERS_SI_WITHOUT_DOMAIN: {
                            String urlString = topic.getOneSubjectIdentifier().toExternalForm();
                            URL url = new URL(urlString);
                            urlString = url.getFile();
                            if(url.getRef() != null) urlString += "#" + url.getRef();
                            return urlString;
                        }

                        case TOPIC_RENDERS_SL: {
                            if(topic.getSubjectLocator() == null) {
                                return "";
                            }
                            else { 
                                return topic.getSubjectLocator().toExternalForm(); 
                            }
                        }


                        // BY DEFAULT TOPIC RENDERS AS THE BASE NAME
                        default: {
                            if(topic.getBaseName() == null) {
                                return topic.getOneSubjectIdentifier().toExternalForm();
                            }
                            else { 
                                return topic.getBaseName(); 
                            }
                        }
                    }
                }
            }
            else {
                return content.toString();
            }
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION
            return "[Exception retrieving name]";
        }
    }

}
