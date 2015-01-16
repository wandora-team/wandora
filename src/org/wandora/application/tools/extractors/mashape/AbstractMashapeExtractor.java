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
 */

package org.wandora.application.tools.extractors.mashape;


import java.util.HashMap;
import org.wandora.application.tools.extractors.AbstractExtractor;
import javax.swing.Icon;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


public abstract class AbstractMashapeExtractor extends AbstractExtractor{
    
    
    private static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    private static final String LAMBDA_SI = "https://lambda-face-detection-and-recognition.p.mashape.com";
    
    private static final String TAG_SI = "http://wandora.org/si/mashape/lambda/tag";
    private static final String FACE_SI = "http://wandora.org/si/mashape/lambda/face";
    private static final String PHOTO_SI = "http://wandora.org/si/mashape/lambda/photo";
    private static final String SMILE_SI = "http://wandora.org/si/mashape/lambda/smiling";
    
    private static final String WIDTH_SI = "http://wandora.org/si/mashape/lambda/width";
    private static final String HEIGHT_SI = "http://wandora.org/si/mashape/lambda/height";
    
    @Override
    public String getName() {
        return "Abstract Mashape extractor";
    }

    @Override
    public String getDescription(){
        return "Abstract extractor for Mashapi.";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_mashape.png");
    }

    private final String[] contentTypes=new String[] { "text/plain", "text/json", "application/json" };
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public boolean runInOwnThread() {
        return false;
    }
    
    // ------------------------------------------------------ HELPERS ---
    
    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
    
    // ---------------------------------------------------------
    
    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException{
        return getOrCreateTopic(tm, LANG_SI);
    }
    
    protected static Topic getAPIClass(TopicMap tm, String si, String baseName)
    throws TopicMapException{
        Topic t = getOrCreateTopic(tm, si, baseName);
        makeSubclassOf(tm, t, getWandoraClassTopic(tm));
        return t;
    }
    
    protected static Topic getTypeClass(TopicMap tm, String si, String baseName) 
    throws TopicMapException{
        return getTypeClass(tm, null, si, baseName);
    }
    
    protected static Topic getTypeClass(TopicMap tm, Topic superClass,
        String si, String baseName) 
    throws TopicMapException{
        Topic type=getOrCreateTopic(tm, si, baseName);
        type.setBaseName(baseName);
        if(superClass != null){
            makeSubclassOf(tm, type, superClass);
        }
        return type;
    }
    
    protected static Topic getTopic(TopicMap tm, Topic type, String si, 
        String baseName ) 
    throws TopicMapException{
        Topic t = getOrCreateTopic(tm, si);
        t.addType(type);
        t.setBaseName(baseName);
        return t;
    }
    
    protected static HashMap<String, Topic> getTypes(TopicMap tm, String[][] typeStrings, Topic api)
    throws TopicMapException{
        HashMap<String, Topic> ts = new HashMap<String,Topic>();
        for (int i = 0; i < typeStrings.length; i++) {
            Topic t = getTypeClass(tm, api, typeStrings[i][1], typeStrings[i][2]);
            ts.put(typeStrings[i][0], t);
        }
        
        return ts;
    }
    
}
