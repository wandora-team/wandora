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
 */

package org.wandora.application.gui.topicstringify;




import java.net.URL;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;


/**
 *
 * @author akivela
 */


public class DefaultTopicStringifier implements TopicStringifier {
 
    public static final int TOPIC_RENDERS_SL = 100;
    public static final int TOPIC_RENDERS_SI = 110;
    public static final int TOPIC_RENDERS_SI_WITHOUT_DOMAIN = 120;
    public static final int TOPIC_RENDERS_BASENAME = 130;
    public static final int TOPIC_RENDERS_BASENAME_WITH_SL_ICON = 140;
    public static final int TOPIC_RENDERS_BASENAME_WITH_INFO = 150;
    public static final int TOPIC_RENDERS_ENGLISH_DISPLAY_NAME = 200;
    
    private int stringType = TOPIC_RENDERS_BASENAME;
    
    
    
    
    
    public DefaultTopicStringifier() {
    }
    
    
    public DefaultTopicStringifier(int type) {
        stringType = type;
    }
    

    
    @Override
    public boolean initialize(Wandora wandora, Context context) {
        return true;
    }

    
    @Override
    public String getDescription() {
        String prefix = "View topic as a ";
        switch(stringType) {
            case TOPIC_RENDERS_SL:                      return prefix + "subject locator";
            case TOPIC_RENDERS_SI:                      return prefix + "subject identifier";
            case TOPIC_RENDERS_SI_WITHOUT_DOMAIN:       return prefix + "subject identifier without domain";
            case TOPIC_RENDERS_BASENAME:                return prefix + "base name";
            case TOPIC_RENDERS_BASENAME_WITH_INFO:      return prefix + "base name with topic info";
            case TOPIC_RENDERS_ENGLISH_DISPLAY_NAME:    return prefix + "english display name";
        }
        return "Default topic renderer";
    }
    
    @Override
    public Icon getIcon() {
        switch(stringType) {
            case TOPIC_RENDERS_SL:                      return UIBox.getIcon("gui/icons/view_topic_as_sl.png");
            case TOPIC_RENDERS_SI:                      return UIBox.getIcon("gui/icons/view_topic_as_si.png");
            case TOPIC_RENDERS_SI_WITHOUT_DOMAIN:       return UIBox.getIcon("gui/icons/view_topic_as_si_wo_domain.png");
            case TOPIC_RENDERS_BASENAME:                return UIBox.getIcon("gui/icons/view_topic_as_basename.png");
            case TOPIC_RENDERS_BASENAME_WITH_INFO:      return UIBox.getIcon("gui/icons/view_topic_as_basename.png");
            case TOPIC_RENDERS_ENGLISH_DISPLAY_NAME:    return UIBox.getIcon("gui/icons/view_topic_as_english_display.png");
        }
        return UIBox.getIcon("gui/icons/view_topic_as_basename.png");
    }
    
    
    
    @Override
    public String toString(Topic t) {
        return _toString(t, stringType);
    }
    
    
    

    public static String _toString(Topic t, int stringType) {
        try {
            if(t == null) return "[null]";
            else if(t.isRemoved()) return "[removed]";
            else {

                switch(stringType) {
                    case TOPIC_RENDERS_BASENAME: {
                        if(t.getBaseName() == null) {
                            return t.getOneSubjectIdentifier().toExternalForm();
                        }
                        else { 
                            return t.getBaseName(); 
                        }
                    }
                    case TOPIC_RENDERS_BASENAME_WITH_INFO: {
                        String info = getTopicInfo(t);
                        if(t.getBaseName() == null) {
                            return t.getOneSubjectIdentifier().toExternalForm() + " " + info;
                        }
                        else { 
                            return t.getBaseName() + " " + info; 
                        }
                    }
                    case TOPIC_RENDERS_SI: {
                        return t.getOneSubjectIdentifier().toExternalForm();
                    }
                    case TOPIC_RENDERS_SI_WITHOUT_DOMAIN: {
                        String urlString = t.getOneSubjectIdentifier().toExternalForm();
                        URL url = new URL(urlString);
                        urlString = url.getFile();
                        if(url.getRef() != null) urlString += "#" + url.getRef();
                        return urlString;
                    }
                    case TOPIC_RENDERS_SL: {
                        if(t.getSubjectLocator() == null) {
                            if(t.getBaseName() == null) {
                                return "["+t.getOneSubjectIdentifier().toExternalForm()+"]";
                            }
                            else { 
                                return "["+t.getBaseName()+"]"; 
                            }
                        }
                        else { 
                            return t.getSubjectLocator().toExternalForm(); 
                        }
                    }
                    case TOPIC_RENDERS_ENGLISH_DISPLAY_NAME: {
                        try {
                            String englishDisplayName = TMBox.getTopicDisplayName(t, "en");
                            if(englishDisplayName != null) return englishDisplayName;
                        }
                        catch(Exception e) {}
                        if(t.getBaseName() == null) {
                            return "["+t.getOneSubjectIdentifier().toExternalForm()+"]";
                        }
                        else { 
                            return "["+t.getBaseName()+"]"; 
                        }
                    }


                    // BY DEFAULT TOPIC RENDERS AS THE BASE NAME
                    default: {
                        if(t.getBaseName() == null) {
                            return t.getOneSubjectIdentifier().toExternalForm();
                        }
                        else { 
                            return t.getBaseName(); 
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            
        }
        return "[error retrieving topic string]";
    }
    
    
    
    
    public static String getTopicInfo(Topic t) {
        StringBuilder s = new StringBuilder("");
        try {
            s.append("SI").append(t.getSubjectIdentifiers().size());
            s.append(" SL").append(t.getSubjectLocator() == null ? "0" : "1");
            s.append(" CL").append(t.getTypes().size());
            s.append(" IN").append(t.getTopicMap().getTopicsOfType(t).size());
            s.append(" AS").append(t.getAssociations().size());
            s.append(" VN").append(t.getVariantScopes().size());
            s.append(" OC").append(t.getDataTypes().size());
        }
        catch(Exception e) {}
        return s.toString();
    }
}
