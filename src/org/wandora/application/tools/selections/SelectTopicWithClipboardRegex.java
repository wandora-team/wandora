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
 * SelectTopicWithClipboardRegex.java
 *
 * Created on 14. heinäkuuta 2006, 12:18
 *
 */

package org.wandora.application.tools.selections;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.util.*;
import java.util.regex.*;


/**
 * Tool selects topics in topic table. Topic is selected if clipboard contains
 * regular expression that matches topic's subject identifier, base name or subject
 * locator. Clipboard may contain multiple regular expressions separated with
 * newline character. Depending on parameters given in tool construction tool
 * selects full matches (regular expression must match full identifier) or
 * partial matches (if regular expression matches any part of the identifier).
 *
 * Tool uses Java's native regular expressions. See <code>String</code> and 
 * <code>Pattern</code> classes for more details.
 *
 * @author akivela
 */


public class SelectTopicWithClipboardRegex extends DoTopicSelection {
    
    Pattern[] patterns = null;
    Pattern pattern = null;
    String[] regexes = null;
    String regex = null;
    Iterator<Locator> sis = null;
    Locator si = null;
    String identifier = null;
    Matcher m = null;
    
    boolean fullMatch = true;
    
    
    public SelectTopicWithClipboardRegex() {
        this.fullMatch = true;
    }
    public SelectTopicWithClipboardRegex(boolean fullMatch) {
        this.fullMatch = fullMatch;
    }
    
    
    
    @Override
    public void initializeTool() {
        regex = ClipboardBox.getClipboard();
        if(regex != null) {
            if(regex.indexOf("\n") != -1) {
                regexes = regex.split("\r*\n\r*");
                for(int i=0; i<regexes.length; i++) {
                    regexes[i] = regexes[i].trim();
                    patterns[i] = Pattern.compile(regexes[i]);
                }
            }
            else {
                regexes = new String[] { regex };
                patterns = new Pattern[] { Pattern.compile(regex) };
            }
        }
    }
    
    
    
    
    @Override
    public boolean acceptTopic(Topic topic)  {
        try {
            if(patterns != null && patterns.length > 0 && topic != null && !topic.isRemoved()) {
                for(int i=0; i<patterns.length; i++) {
                    pattern = patterns[i];
                    if(pattern != null) {
                        if(matches(pattern, topic.getSubjectLocator())) return true;
                        if(matches(pattern, topic.getBaseName())) return true;

                        sis = topic.getSubjectIdentifiers().iterator();
                        while(sis.hasNext()) {
                            si = sis.next();
                            if(matches(pattern, si)) return true;
                        }
                    }
                }
            }
        }
        catch(TopicMapException tme) {
            log(tme);
        }
        return false;
    }

    
    public boolean matches(Pattern pattern, Locator locator) {
        if(locator != null) {
            return matches(pattern, locator.toExternalForm());
        }
        return false;
    }
    
    
    public boolean matches(Pattern pattern, String str) {
        if(str != null && str.length() > 0 && pattern != null) {
            m = pattern.matcher(str);
            if(fullMatch) {
                return m.matches();
            }
            else {
                return m.find();
            }
        }
        return false;
    }
    
    
    @Override
    public String getName() {
        return "Select topic with clipboard";
    }
    
    
    @Override
    public String getDescription() {
        return "Select topic if clipboard contains topic's subject locator, base name or subject identifier.";
    }
    
}
