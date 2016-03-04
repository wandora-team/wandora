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
 */

package org.wandora.application.tools.extractors.guardian;

import javax.swing.Icon;
import java.util.HashMap;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author
 * Eero Lehtonen
 */


public abstract class AbstractGuardianExtractor extends AbstractExtractor {
  
  @Override
    public String getName() {
        return "Abstract The Guardian API extractor";
    }

    @Override
    public String getDescription(){
        return "Abstract extractor for The Guardian API.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_nyt.png");
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

    // -------------------------------------------------------------------------
    
    public static final String GUARDIAN_BASE_SI = "http://wandora.org/si/theguardian/";
    public static final String FIELD_BASE_SI = GUARDIAN_BASE_SI + "field/";
    public static final String TAG_BASE_SI = GUARDIAN_BASE_SI + "tag/";
    public static final String CONTENT_SI = GUARDIAN_BASE_SI + "content";
    public static final String DATE_SI = GUARDIAN_BASE_SI + "date";
    public static final String ID_SI = GUARDIAN_BASE_SI + "ID";
    public static final String TITLE_SI = GUARDIAN_BASE_SI + "title";
    public static final String PUBLICATION_TIME_SI = GUARDIAN_BASE_SI + "pubtime";
    public static final String SECTION_ID_SI = GUARDIAN_BASE_SI + "sectionid";
    public static final String SECTION_NAME_SI = GUARDIAN_BASE_SI + "sectionname";
    public static final String WEB_URL_SI = GUARDIAN_BASE_SI + "web_url";
    public static final String API_URL_SI = GUARDIAN_BASE_SI + "api_url";
    public static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";

    private static  HashMap<String,String> fieldNames = new HashMap() {{
      put("trailText","trail text");
      put("showInRelatedContent","show in related content");
      put("lastModified","last modified");
      put("hasStoryPackage","has story package");
      put("standFirst","stand first");
      put("shortUrl","short URL");
      put("shouldHideAdverts","should hide adverts");
      put("liveBloggingNow","live blogging now");
      put("newspaperEditionDate","newspaper edition date");
      put("newspaperPageNumber","newspaper page number");
    }};
    
    
    public static Topic getIDType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, ID_SI, "content ID (The Guardian API)");
      Topic guardianTopic = getGuardianType(tm);
      makeSubclassOf(tm, type, guardianTopic);
      return type;
    }
    
    public static Topic getTitleType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, TITLE_SI, "content title (The Guardian API)");
      Topic guardianTopic = getGuardianType(tm);
      makeSubclassOf(tm, type, guardianTopic);
      return type;
    }
    
    public static Topic getDateType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, DATE_SI, "content date (The Guardian API)");
      Topic guardianTopic = getGuardianType(tm);
      makeSubclassOf(tm, type, guardianTopic);
      return type;
    }
    
    public static Topic getDateTopic(TopicMap tm, String date) throws TopicMapException{
      Topic dateTopic = getOrCreateTopic(tm, DATE_SI+"/"+urlEncode(date), date + " (The Guardian API)");
      dateTopic.addType(getDateType(tm));
      return dateTopic;
    }
    
    public static Topic getPubTimeType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, PUBLICATION_TIME_SI, "content publication time (The Guardian API)");
      Topic guardianTopic = getGuardianType(tm);
      makeSubclassOf(tm, type, guardianTopic);
      return type;
    }
    
    public static Topic getContentType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, CONTENT_SI, "content (The Guardian API)");
      Topic guardianTopic = getGuardianType(tm);
      makeSubclassOf(tm, type, guardianTopic);
      return type;
    }
    
    public static Topic getFieldType(TopicMap tm, String siExt){
        Topic t = null;
        String desc = fieldNames.containsKey(siExt) ? fieldNames.get(siExt) : siExt;
        try {
            t = getOrCreateTopic(tm, FIELD_BASE_SI + siExt, desc + " (The Guardian API / Field)");
            Topic fieldTopicType = getFieldTopicType(tm);
            makeSubclassOf(tm, t, fieldTopicType);
        } catch (TopicMapException e) {
            e.printStackTrace();
        }
        return t;
    }
    
    public static Topic getFieldTopic(TopicMap tm, String siExt, String id){
        Topic t = null;
        String siEnd  = urlEncode(id);
        try {
            t = getOrCreateTopic(tm, FIELD_BASE_SI+siExt+"/" + siEnd,id + " (The Guardian API / Field)");
        } catch (TopicMapException e) {
            e.printStackTrace();
        }
        return t;
    }
    
    public static Topic getTagType(TopicMap tm, String siExt){
        Topic t = null;
        String desc = fieldNames.containsKey(siExt) ? fieldNames.get(siExt) : siExt;
        try {
            t = getOrCreateTopic(tm, TAG_BASE_SI + siExt, desc + " (The Guardian API / Tag)");
            Topic tagTopicType = getTagTopicType(tm);
        } catch (TopicMapException e) {
            e.printStackTrace();
        }
        return t;
    }
    
    public static Topic getTagTopic(TopicMap tm, String siExt, String id){
        Topic t = null;
        String siEnd  = urlEncode(id);
        try {
            t = getOrCreateTopic(tm, TAG_BASE_SI+siExt+"/" + siEnd,id + " (The Guardian API / Tag)");
        } catch (TopicMapException e) {
            e.printStackTrace();
        }
        return t;
    }
    
    public static Topic getGuardianType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, GUARDIAN_BASE_SI, "The Guardian API");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    public static Topic getFieldTopicType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, FIELD_BASE_SI, "Field (The Guardian API)");
        Topic guardianType = getGuardianType(tm);
        makeSubclassOf(tm, type, guardianType);
        return type;
    }
    
    public static Topic getTagTopicType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TAG_BASE_SI, "Tag (The Guardian API)");
        Topic guardianType = getGuardianType(tm);
        makeSubclassOf(tm, type, guardianType);
        return type;
    }
    
    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }
    
    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        Topic lang = getOrCreateTopic(tm, LANG_SI);
        return lang;
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
    
}
