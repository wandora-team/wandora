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
 * 
 * AbstractElavaArkistoExtractor.java
 *
 * Created on 2015-04-30
 */

package org.wandora.application.tools.extractors.yle.elavaarkisto;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 * Finnish broadcasting company (YLE) has published Elava arkisto 
 * metadata under the CC-BY-SA 4.0 license. Wandora features a set of extractors
 * that transfer the published metadata to Topic Maps. This extractor is an abstract
 * class of these extractor. The metadata and it's documentation is available at 
 *
 * http://elavaarkisto.kokeile.yle.fi/data/
 * 
 * 
 * @author akivela
 */


public abstract class AbstractElavaArkistoExtractor extends AbstractExtractor {

    public static final String ELAVA_ARKISTO_SI = "http://wandora.org/si/yle/elava-arkisto";
    
    public static final String ELAVA_ARKISTO_ARTICLE_TYPE_SI = ELAVA_ARKISTO_SI+"/article";
    public static final String ELAVA_ARKISTO_ARTICLE_ID_TYPE_SI = ELAVA_ARKISTO_SI+"/article-id";
    public static final String ELAVA_ARKISTO_ARTICLE_PUBLISHED_TYPE_SI = ELAVA_ARKISTO_SI+"/article-published";
    public static final String ELAVA_ARKISTO_SERVICE_TYPE_SI = ELAVA_ARKISTO_SI+"/service";
    public static final String ELAVA_ARKISTO_TAG_TYPE_SI = ELAVA_ARKISTO_SI+"/tag";
    public static final String ELAVA_ARKISTO_TAG_ARTICLE_RELATION_TYPE_SI = ELAVA_ARKISTO_SI+"/tag-article-relation";
    public static final String ELAVA_ARKISTO_MEDIA_TYPE_SI = ELAVA_ARKISTO_SI+"/media";
    public static final String ELAVA_ARKISTO_DATE_TYPE_SI = ELAVA_ARKISTO_SI+"/date";
    public static final String ELAVA_ARKISTO_TIME_TYPE_SI = ELAVA_ARKISTO_SI+"/time";
    
    
    
    
    @Override
    public String getName() {
        return "Abstract YLE Elava arkisto extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Abstract YLE Elava arkisto extractor.";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_yle.png");
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        String str = FileUtils.readFileToString(f, "UTF-8");
        return _extractTopicsFrom(str, tm);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        InputStream in = u.openStream();
        String str = null;
        try {
            str = IOUtils.toString( in, "UTF-8" );
        } 
        finally {
            IOUtils.closeQuietly(in);
        }
        if(str != null) return _extractTopicsFrom(str, tm);
        return false;
    }


    // -------------------------------------------------------------------------
    
    
    public Topic getWandoraType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class", tm);
        return type;
    }
    
    
    public Topic getElavaArkistoType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_SI, "Elava-arkisto", getWandoraType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoArticleTopic(String aid, String url, TopicMap tm) throws TopicMapException {
        if(!isValidData(aid)) {
            aid = "" + System.currentTimeMillis();
        }
        String si = ELAVA_ARKISTO_ARTICLE_TYPE_SI + "/" + urlEncode(aid);
        
        Topic articleTopic = tm.getTopic(si);
        if(articleTopic == null && url != null) articleTopic = tm.getTopic(url);
        if(articleTopic == null && url != null) articleTopic = tm.getTopicBySubjectLocator(url);

        if(articleTopic == null) {
            articleTopic = tm.createTopic();
            articleTopic.addSubjectIdentifier(new Locator(si));
        }
        if(isValidData(url)) {
            articleTopic.addSubjectIdentifier(new Locator(url));
            articleTopic.setSubjectLocator(new Locator(url));
        }
        Topic articleTypeTopic = getElavaArkistoArticleType(tm);
        if(articleTypeTopic != null) {
            articleTopic.addType(articleTypeTopic);
        }
        return articleTopic;
    }
    
    
    public Topic getElavaArkistoArticleType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ARTICLE_TYPE_SI, "Elava-arkisto article", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoArticleIdType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ARTICLE_ID_TYPE_SI, "Elava-arkisto article id", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoServiceTopic(String id, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_SERVICE_TYPE_SI+"/"+urlEncode(id);
        Topic serviceTopic = null;
        try {
            serviceTopic = tm.getTopic(si);
            if(serviceTopic == null) {
                serviceTopic = tm.createTopic();
                serviceTopic.addSubjectIdentifier(new Locator(si));
                serviceTopic.addType(getElavaArkistoServiceType(tm));
                serviceTopic.setBaseName(id + " (YLE service)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return serviceTopic;
    }
    
    
    public Topic getElavaArkistoServiceType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_SERVICE_TYPE_SI, "Elava-arkisto service", getElavaArkistoType(tm), tm);
        return type;
    }

    
    public Topic getElavaArkistoMediaType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_TYPE_SI, "Elava-arkisto media", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoMediaTopic(String mid, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_TYPE_SI + "/" + urlEncode(mid);
        Topic tagTopic = tm.getTopic(si);
        if(tagTopic == null) {
            tagTopic = tm.createTopic();
            tagTopic.addSubjectIdentifier(new Locator(si));
            tagTopic.setBaseName( mid );
            tagTopic.addType(getElavaArkistoMediaType(tm));
        }
        return tagTopic;
    }
    
    
    public Topic getElavaArkistoTagType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TAG_TYPE_SI, "Elava-arkisto tag", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoTagTopic(String kid, String label, TopicMap tm) throws TopicMapException {       
        try {
            // Fix label encoding.
            // label = new String(label.getBytes(), "UTF-8");
        }
        catch(Exception e) {}
        
        if(kid.startsWith("keyword=")) {
            kid = label;
        }
        
        String si = ELAVA_ARKISTO_TAG_TYPE_SI + "/" + urlEncode(kid);
        
        
        if(kid.startsWith("/term/finto/httpwwwysofiontokoko")) {
            String ysoId = kid.substring("/term/finto/httpwwwysofiontokoko".length());
            ysoId = ysoId.substring(0, ysoId.indexOf("/"));
            kid = "http://www.yso.fi/onto/koko/"+ysoId;
        }
        if(kid.startsWith("/aihe/termi/finto/httpwwwysofiontokoko")) {
            String ysoId = kid.substring("/aihe/termi/finto/httpwwwysofiontokoko".length());
            ysoId = ysoId.substring(0, ysoId.indexOf("/"));
            kid = "http://www.yso.fi/onto/koko/"+ysoId;
        }
        
        if(kid.startsWith("/aihe/termi/freebase/m")) {
            String freebaseId = kid.substring("/aihe/termi/freebase/m".length());
            freebaseId = freebaseId.substring(0, freebaseId.indexOf("/"));
            kid = "http://www.freebase.com/m/"+freebaseId;
        }
        if(kid.startsWith("/term/freebase/m")) {
            String freebaseId = kid.substring("/term/freebase/m".length());
            freebaseId = freebaseId.substring(0, freebaseId.indexOf("/"));
            kid = "http://www.freebase.com/m/"+freebaseId;
        }
        if(kid.startsWith("http://")) {
            si = kid;
        }
        if(kid.startsWith("http://www.freebase.com/m/")) {
            kid = kid.substring("http://www.freebase.com/m/".length());
        }
        if(kid.startsWith("http://www.yso.fi/onto/koko/")) {
            kid = kid.substring("http://www.yso.fi/onto/koko/".length());
        }
        if(kid.equals(label)) {
            kid = "tag";
        }

        Topic tagTopic = tm.getTopic(si);
        if(tagTopic == null) {
            tagTopic = tm.createTopic();
            tagTopic.addSubjectIdentifier(new Locator(si));
            tagTopic.setBaseName(label + " (" + kid + ")");
            tagTopic.setDisplayName(null, label);
            tagTopic.addType(getElavaArkistoTagType(tm));
        }
        return tagTopic;
    }
    

    public Topic getElavaArkistoTagArticleRelationType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TAG_ARTICLE_RELATION_TYPE_SI, "Elava-arkisto tag article relation", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoTagArticleRelationTopic(String rel,TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_TAG_ARTICLE_RELATION_TYPE_SI + "/" + urlEncode(rel);
        Topic tagTopic = tm.getTopic(si);
        if(tagTopic == null) {
            tagTopic = tm.createTopic();
            tagTopic.addSubjectIdentifier(new Locator(si));
            tagTopic.setBaseName( rel );
            tagTopic.addType(getElavaArkistoTagArticleRelationType(tm));
        }
        return tagTopic;
    }
    
    
    public Topic getElavaArkistoDateType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_DATE_TYPE_SI, "Elava-arkisto date", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoDateTopic(String date, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_DATE_TYPE_SI + "/" + urlEncode(date);
        Topic dateTopic = tm.getTopic(si);
        if(dateTopic == null) {
            dateTopic = tm.createTopic();
            dateTopic.addSubjectIdentifier(new Locator(si));
            dateTopic.setBaseName( date );
            dateTopic.addType(getElavaArkistoDateType(tm));
        }
        return dateTopic;
    }
    

    public Topic getElavaArkistoTimeType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TIME_TYPE_SI, "Elava-arkisto time", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoTimeTopic(String time, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_TIME_TYPE_SI + "/" + urlEncode(time);
        Topic timeTopic = tm.getTopic(si);
        if(timeTopic == null) {
            timeTopic = tm.createTopic();
            timeTopic.addSubjectIdentifier(new Locator(si));
            timeTopic.setBaseName( time );
            timeTopic.addType(getElavaArkistoTimeType(tm));
        }
        return timeTopic;
    }
    
    
    
    // -------------------------------------------------------------------------
    

    protected boolean isValidData(String d) {
        if(d == null) return false;
        if(d.length() == 0) return false;
        if("null".equalsIgnoreCase(d)) return false;
        return true;
    }
    
    
    protected String stringify(Object o) {
        if(o != null) return o.toString();
        return "null";
    }
}
