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
 * ImageTypeProcessor.java
 *
 * Created on August 18, 2004, 3:36 PM
 */

package org.wandora.application.tools.fng;

import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TMBox;
import org.wandora.piccolo.Logger;
import java.util.*;


/**
 *
 * @author  olli
 */
public class ImageTypeProcessor {
    
    /** Creates a new instance of ImageTypeProcessor */
    public ImageTypeProcessor() {
    }
    
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!! pictureType subject identifier changed from .../assembly/... to .../common/... !!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying ImageTypeProcessor filter");
        Topic roleRef=tm.getTopic("http://wandora.org/si/compatibility/occurrencerolereference");
        Topic roleTop=tm.getTopic("http://wandora.orgsi//compatibility/occurrenceroletopic");
        Topic occType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#digikuvatiedosto");
        if(roleRef==null || roleTop==null || occType==null){
            logger.writelog("Couldn't find all needed topics, aborting.");
            return tm;
        }
        Topic imageOccurrence=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/imageoccurrence");
        Topic pictureType=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/picture");
        Topic thumbnailType=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/thumbnail");
        Topic highresType=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/highresimage");
        Topic ultraresType=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/ultraresimage");
        Topic otherversionType=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/otherversionimage");
        Vector<Topic> v=new Vector<>();
        Iterator<Association> aiter=tm.getAssociations();
        while(aiter.hasNext()){
            Association a=(Association)aiter.next();
            if(a.getType()!=occType) continue;
            Topic topic=a.getPlayer(roleTop);
            Topic ref=a.getPlayer(roleRef);
            if(topic==null || ref==null) continue;
            if(a.getRoles().size()>2) continue;
                v.add(topic);
        }
        Iterator<Topic> titer=v.iterator();
        while(titer.hasNext()){
            Hashtable<String,ImageGroup> images=new Hashtable<>();
            Topic topic=(Topic)titer.next();
            Iterator<Association> aiter2=new ArrayList(topic.getAssociations(occType,roleTop)).iterator();
            while(aiter2.hasNext()){
                Association a=(Association)aiter2.next();
                Topic ref=a.getPlayer(roleRef);
                if(ref==null || a.getRoles().size()!=2) continue;
                String s=ref.getSubjectLocator().toExternalForm();
                s=s.substring(s.lastIndexOf("/")+1);
                String imagebase=s.substring(0, s.indexOf("."));
                String version=s.substring(s.indexOf(".")+1);
                ImageGroup group=(ImageGroup)images.get(imagebase);
                if(group==null){
                    group=new ImageGroup();
                    images.put(imagebase,group);
                }
                if(version.startsWith("huge")){
                    if(!group.huge.isEmpty()) logger.writelog("Two huges for "+topic.getDisplayName("fi"));
                    group.huge.add(ref);
                }
                else if(version.startsWith("tiny")) {
                    if(!group.tiny.isEmpty()) logger.writelog("Two tinies for "+topic.getDisplayName("fi"));
                    group.tiny.add(ref);
                }
                else if(version.startsWith("tif")){
                    if(!group.tif.isEmpty()) logger.writelog("Two tifs for "+topic.getDisplayName("fi"));
                    group.tif.add(ref);
                }
                else {
                    if(!group.normal.isEmpty()) logger.writelog("Two normals for "+topic.getDisplayName("fi"));
                    group.normal.add(ref);
                }
                a.remove();
            }
            Iterator<Map.Entry<String,ImageGroup>> igeiter2=images.entrySet().iterator();
            while(igeiter2.hasNext()){
                Map.Entry<String,ImageGroup> e=igeiter2.next();
                ImageGroup group=(ImageGroup)e.getValue();
                Topic img=null;
                if(!group.normal.isEmpty()) img=(Topic)group.normal.iterator().next();
                if(!group.huge.isEmpty()) img=(Topic)group.huge.iterator().next();
                if(!group.tif.isEmpty()) img=(Topic)group.tif.iterator().next();
                if(!group.tiny.isEmpty()) img=(Topic)group.tiny.iterator().next();
                
                Association a=tm.createAssociation(imageOccurrence);
                a.addPlayer(topic,pictureType);
                a.addPlayer(img,imageOccurrence);
                topic.addType(pictureType);
                img.addType(imageOccurrence);
                
                Iterator<Topic> iter3=group.normal.iterator();
                while(iter3.hasNext()){
                    Topic t=(Topic)iter3.next();
                    if(t!=img){
                        Association ta=tm.createAssociation(otherversionType);
                        t.addType(otherversionType);
                        ta.addPlayer(img,imageOccurrence);
                        ta.addPlayer(t,otherversionType);
                    }
                }
                iter3=group.tiny.iterator();
                while(iter3.hasNext()){
                    Topic t=(Topic)iter3.next();
                    if(t!=img){
                        Association ta=tm.createAssociation(thumbnailType);
                        t.addType(thumbnailType);
                        ta.addPlayer(img,imageOccurrence);
                        ta.addPlayer(t,thumbnailType);
                    }
                }
                iter3=group.huge.iterator();
                while(iter3.hasNext()){
                    Topic t=(Topic)iter3.next();
                    if(t!=img){
                        Association ta=tm.createAssociation(highresType);
                        t.addType(highresType);
                        ta.addPlayer(img,imageOccurrence);
                        ta.addPlayer(t,highresType);
                    }
                }
                iter3=group.tif.iterator();
                while(iter3.hasNext()){
                    Topic t=(Topic)iter3.next();
                    if(t!=img){
                        Association ta=tm.createAssociation(ultraresType);
                        t.addType(ultraresType);
                        ta.addPlayer(img,imageOccurrence);
                        ta.addPlayer(t,ultraresType);
                    }
                }
            }
            
            /*
            Association a=(Association)iter.next();
            Topic topic=a.getPlayer(roleTop);
            Topic ref=a.getPlayer(roleRef);
            if(topic==null || ref==null) continue;
            if(a.getRoles().size()>2) continue;
            a.setType(imageOccurrence);
            a.removePlayer(roleTop);
            a.removePlayer(roleRef);
            a.addPlayer(topic,pictureType);
            a.addPlayer(ref,imageOccurrence);
            topic.addType(pictureType);
            ref.addType(imageOccurrence);
             */
        }
        logger.writelog("Changed "+v.size()+" associations");
        return tm;
    }
    
    private class ImageGroup {
        public Collection<Topic> tif,huge,tiny,normal;
        public ImageGroup(){
            tif=new LinkedHashSet<Topic>();
            huge=new LinkedHashSet<Topic>();
            tiny=new LinkedHashSet<Topic>();
            normal=new LinkedHashSet<Topic>();
        }
    }
}
