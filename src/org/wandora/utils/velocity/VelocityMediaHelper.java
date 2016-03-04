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
 * 
 *
 * AssemblyHelper.java
 *
 * Created on July 14, 2004, 12:36 PM
 */

package org.wandora.utils.velocity;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.*;
import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class VelocityMediaHelper {
    
    /** Creates a new instance of AssemblyHelper */
    public VelocityMediaHelper() {
    }
        
    public static String getTopicVideoThumbnail(Topic t) throws TopicMapException {
        Topic vwork=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/videowork");
        Topic occ=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/videooccurrence");
        Topic mocc=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/mediaoccurrence");
        Topic tn=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/thumbnail");
        Iterator iter=t.getAssociations(occ,vwork).iterator();
        String found=null;
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic image=a.getPlayer(occ);
            if(image==null) continue;
            Iterator iter2=image.getAssociations(tn,mocc).iterator();
            while(iter2.hasNext()){
                Association a2=(Association)iter2.next();
                Topic tnimage=a2.getPlayer(tn);
                if(tnimage==null) continue;
                if(tnimage.getSubjectLocator()!=null) return tnimage.getSubjectLocator().toExternalForm();
            }
        }
        return found;        
    }
    public static String getOccurrenceVideoThumbnail(Topic t) throws TopicMapException {
        Topic mocc=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/mediaoccurrence");
        Topic tn=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/thumbnail");
        Iterator iter2=t.getAssociations(tn,mocc).iterator();
        while(iter2.hasNext()){
            Association a2=(Association)iter2.next();
            Topic tnimage=a2.getPlayer(tn);
            if(tnimage==null) continue;
            if(tnimage.getSubjectLocator()!=null) return tnimage.getSubjectLocator().toExternalForm();
        }
        return null;        
    }
    public static String getVideoPreview(Topic t){
        return null;
/*        Topic occ=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/videooccurrence");
        Topic preview=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/videopreview");
        Iterator iter=t.getAssociations(preview,occ).iterator();
        String found=null;
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic tnimage=a.getPlayer(preview);
            if(tnimage==null) continue;
            if(tnimage.getSubjectLocator()!=null) return tnimage.getSubjectLocator().toExternalForm();
        }
        return found;                */
    }
     
/*    public static String getTopicImageThumbnail(Topic t){
        Topic occ=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/imageoccurrence");
        Topic tn=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/thumbnail");
        Iterator iter=t.getAssociations().iterator();
        while(iter.hasNext()) {
            try {
                Association a=(Association)iter.next();
                Topic image=a.getPlayer(occ);
                if(image != null) return image.getSubjectLocator().toExternalForm();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }*/
    
    public static String getTopicImageThumbnail(Topic t) throws TopicMapException {
        Topic vwork=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/visualwork");
        Topic occ=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/imageoccurrence");
        Topic mocc=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/mediaoccurrence");
        Topic tn=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/thumbnail");
        Iterator iter=t.getAssociations(occ,vwork).iterator();
        String found=null;
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic image=a.getPlayer(occ);
            if(image==null) continue;
            Iterator iter2=image.getAssociations(tn,mocc).iterator();
            while(iter2.hasNext()){
                Association a2=(Association)iter2.next();
                Topic tnimage=a2.getPlayer(tn);
                if(tnimage==null) continue;
                if(tnimage.getSubjectLocator()!=null) return tnimage.getSubjectLocator().toExternalForm();
            }
            if(found==null && image.getSubjectLocator()!=null) found=image.getSubjectLocator().toExternalForm();
        }
        return found;
    }
    
    public static String getTopicTVImage(Topic t) throws TopicMapException {
        Topic vwork=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/visualwork");
        Topic occ=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/imageoccurrence");
        Topic mocc=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/mediaoccurrence");
        Topic tn=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/tvimage");
        Iterator iter=t.getAssociations(occ,vwork).iterator();
        String found=null;
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic image=a.getPlayer(occ);
            if(image==null) continue;
            Iterator iter2=image.getAssociations(tn,mocc).iterator();
            while(iter2.hasNext()){
                Association a2=(Association)iter2.next();
                Topic tnimage=a2.getPlayer(tn);
                if(tnimage==null) continue;
                if(tnimage.getSubjectLocator()!=null) return tnimage.getSubjectLocator().toExternalForm();
            }
            if(found==null && image.getSubjectLocator()!=null) found=image.getSubjectLocator().toExternalForm();
        }
        return found;
    }
    public static String getTopicImage(Topic t) throws TopicMapException {
        Topic vwork=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/visualwork");
        Topic occ=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/imageoccurrence");
        Iterator iter=t.getAssociations(occ,vwork).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic image=a.getPlayer(occ);
            if(image==null) continue;
            if(image.getSubjectLocator()!=null) return image.getSubjectLocator().toExternalForm();
        }
        return null;
    }

    public String trimNonAlphaNums(String word) {
        if(word == null || word.length() < 1) return "";
        
        int i=0;
        int j=word.length()-1;
        for(; i<word.length() && !Character.isJavaLetterOrDigit(word.charAt(i)); i++);
        for(; j>i+1 && !Character.isJavaLetterOrDigit(word.charAt(j)); j--);
        
        return word.substring(i,j+1);
    }
    public String encodeURL(String s) {
        return java.net.URLEncoder.encode(s);
    }
    
    public String populateLinks(String text, String linkTemplate) {
        if(text != null && text.length()>0) {
            String DELIMITERS = " \n\t',.\"";
            StringBuffer newText = new StringBuffer(1000);
            String searchword;
            String link;
            String substring;
            int index=0;
            int wordStart;
            int wordEnd;
            while(index < text.length()) {
                while(index < text.length() && DELIMITERS.indexOf(text.charAt(index)) != -1) {
                    newText.append(text.charAt(index));
                    index++;
                }
                
                // pass html/xml tags
                while(index < text.length() && text.charAt(index) == '<') {
                   while(index < text.length() && text.charAt(index) != '>') {
                        newText.append(text.charAt(index));
                        index++;
                   }
                   newText.append(text.charAt(index));
                   index++;
                }
                // potential word found
                wordStart=index;
                while(index < text.length() && DELIMITERS.indexOf(text.charAt(index)) == -1 && text.charAt(index) != '<') {
                    index++;
                }
                
                if(index > wordStart) {
                    substring = text.substring(wordStart, index);
//                    try { substring = encodeHTML(substring); } catch (Exception e) {}
                    if(index-wordStart > 3) {
                        searchword = trimNonAlphaNums(substring);
                        if(searchword.length() > 3) {
                            link = linkTemplate.replaceAll("%searchw%", encodeURL(searchword));
                            link = link.replaceAll("%word%", substring);
                            newText.append(link);
                        }
                        else {
                            newText.append(substring);
                        }
                    }
                    else {
                        newText.append(substring);
                    }
                }
                
            }
            text = newText.toString();
        }
        return text;
    }

    public static class TreeNode {
        public Topic content;
        public boolean openChildren;
        public boolean closeChildren;
        public TreeNode(Topic content,boolean openChildren,boolean closeChildren){
            this.content=content;
            this.openChildren=openChildren;
            this.closeChildren=closeChildren;
        }
    }
    
}
