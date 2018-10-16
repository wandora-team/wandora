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

package org.wandora.application.tools.extractors.freebase;

/**
 *
 * @author
 * Eero Lehtonen
 */


import javax.swing.Icon;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.*;

public abstract class AbstractFreebaseExtractor extends AbstractExtractor {
    

	private static final long serialVersionUID = 1L;
	
	
	@Override
    public String getName() {
        return "Abstract Freebase API extractor";
    }

    @Override
    public String getDescription(){
        return "Abstract extractor for the Freebase API.";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract.png");
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
    
    public static final String FREEBASE_WANDORA_SI = "http://wandora.org/si/freebase";
    public static final String FREEBASE_BASE_SI = "http://www.freebase.com";
    public static final String OBJECT_SI = FREEBASE_WANDORA_SI + "/OBJECT";
    public static final String TYPE_SI = FREEBASE_WANDORA_SI + "/TYPE";
    public static final String NAME_SI = FREEBASE_WANDORA_SI + "/name";
    public static final String TARGET_SI = FREEBASE_WANDORA_SI + "/target";
    public static final String SOURCE_SI = FREEBASE_WANDORA_SI + "/source";
    public static final String LINK_SI = FREEBASE_WANDORA_SI + "/link";
    public static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    
    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }
    
    public static Topic getNameType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, NAME_SI, "Object name (Freebase API)");
      return type;
    }
    
    public static Topic getObjectType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, OBJECT_SI, "Object (Freebase API)");
      Topic freebaseTopic = getFreebaseType(tm);
      makeSubclassOf(tm, type, freebaseTopic);
      return type;
    }
    
    public static Topic getTypeType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, TYPE_SI, "Type (Freebase API)");
      Topic freebaseTopic = getFreebaseType(tm);
      makeSubclassOf(tm, type, freebaseTopic);
      return type;
    }
    
    public static Topic getTargetType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, TARGET_SI, "Target (Freebase API)");
      Topic freebaseTopic = getFreebaseType(tm);
      return type;
    }
    
    public static Topic getSourceType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, SOURCE_SI, "Source (Freebase API)");
      Topic freebaseTopic = getFreebaseType(tm);
      return type;
    }
    
    public static Topic getLinkType(TopicMap tm) throws TopicMapException{
      Topic type = getOrCreateTopic(tm, LINK_SI, "Link (Freebase API)");
      Topic freebaseTopic = getFreebaseType(tm);
      makeSubclassOf(tm, type, freebaseTopic);
      return type;
    }
    
    public Topic createFreebaseTopic(TopicMap tm, JSONObject mqlObject) throws TopicMapException, JSONException{
        String topicID = mqlObject.getString(("id"));
        String topicGUID = mqlObject.getString("guid");
        topicGUID = topicGUID.replace("#", "/guid/");
        String topicName = (mqlObject.get("name") instanceof String) ?  mqlObject.getString(("name")) : "<no name>";
        Topic topic = getOrCreateTopic(tm, FREEBASE_BASE_SI + topicGUID);
        topic.addSubjectIdentifier(new Locator(FREEBASE_BASE_SI + topicID));
        topic.setBaseName(topicName + " (" + topicID + ")");
        topic.setDisplayName("en", topicName);
        topic.setData(getNameType(tm), getLangTopic(tm), topicName);
        return topic;
    }
    
    public Topic createType(TopicMap tm, JSONObject mqlObject) throws TopicMapException, JSONException{
        Topic type = createFreebaseTopic(tm, mqlObject);
        type.addType(getTypeType(tm));
        return type;
    }
    
    public Topic createLinkType(TopicMap tm, JSONObject mqlObject) throws TopicMapException, JSONException{
        Topic type = createFreebaseTopic(tm, mqlObject);
        type.addType(getLinkType(tm));
        return type;
    }
    
    public static Topic getFreebaseType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, FREEBASE_BASE_SI, "Freebase API");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
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
    
    protected static String getQuery(String id){
        return "{"
                +    "\"id\": \"" + id + "\","
                +    "\"name\": null,"
                +    "\"guid\": null,"
                +    "\"type\": [{"
                +        "\"id\": null,"
                +        "\"name\": null,"
                +        "\"guid\": null,"
                +        "\"properties\": [{"
                +            "\"expected_type\":{"
                +                "\"id\":null,"
                +                "\"guid\": null,"
                +                "\"name\":null"
                +            "},"
                +            "\"id\": null,"
                +            "\"guid\": null,"
                +            "\"name\": null,"
                +            "\"links\": [{"
                +                "\"source\": {"
                +                    "\"id\": \"" + id + "\""
                +                "},"
                +                "\"target\": {"
                +                    "\"guid\": null,"
                +                    "\"id\": null,"
                +                    "\"name\": null,"
                +                    "\"optional\": true"
                +                "},"
                +                "\"optional\": true,"
                +                "\"target_value\": null"
                +            "}],"
                +            "\"master_property\": {"
                +                "\"optional\": true,"
                +                "\"expected_type\":{"
                +                    "\"guid\": null,"
                +                    "\"id\":null,"
                +                    "\"name\":null"
                +                "},"
                +                "\"id\": null,"
                +                "\"guid\": null,"
                +                "\"name\": null,"
                +                "\"links\": [{"
                +                    "\"source\": {"
                +                        "\"guid\": null,"
                +                        "\"id\": null,"
                +                        "\"name\": null"
                +                    "},"
                +                    "\"target\": {"
                +                        "\"id\": \"" + id + "\""
                +                    "}"
                +                "}]"
                +            "}"
                +        "}]"
                +    "}]"
                +"}";
    }
}
