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
 * SimpleJPGExtractor.java
 *
 * Created on November 12, 2004, 5:08 PM
 */

package org.wandora.application.tools.extractors.files;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.Icon;
//import org.w3c.tools.jpeg.JpegException;
//import org.w3c.tools.jpeg.*;
//import org.xml.sax.*;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.utils.*;



/**
 *
 * @author  olli, akivela
 */
public class SimpleJPGExtractor extends AbstractExtractor implements WandoraTool {

    private String defaultLang = "en";

    
    
    /** Creates a new instance of SimpleJPGExtractor */
    public SimpleJPGExtractor() {
    }
    
    
    
 
   
    @Override
    public String getName() {
        return "Extract PS metadata from JPEGs...";
    }
    @Override
    public String getDescription(){
        return "Extracts PS metadata from JPEG image files!";
    }
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf1c5);
    }

    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select JPEG file(s) or directories containing JPEG files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking JPEG files!";
        
            case FILE_PATTERN: return ".*\\.(jpg|JPG|jpeg|JPEG)";
            
            case DONE_FAILED: return "Done! No extractions! %1 jpeg file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 jpeg file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 jpeg(s) crawled!";
            
            case LOG_TITLE: return "JPEG Image Extraction Log";
        }
        return "";
    }
    


    // -------------------------------------------------------------------------




    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }


    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        
        try {
            String name = url.getFile();
            if(name.lastIndexOf("/") > -1) {
                name = name.substring(name.lastIndexOf("/")+1);
            }

            Topic imageType = createImageTypeTopic(topicMap);
            String location = url.toExternalForm();
            int hash = location.hashCode();
            Topic imageTopic = createTopic(topicMap, location, " ("+hash+")", name, imageType);
            imageTopic.setSubjectLocator(new Locator( location ));

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat();
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(imageTopic, extractionTimeType, defaultLang, dateString);

            URLConnection uc = null;
            if(getWandora() != null) {
                uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(uc.getInputStream(), topicMap, imageTopic);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url '" + url.toExternalForm()+"'.", e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || file.isDirectory()) return false;
        
        try {
            Topic imageType = createImageTypeTopic(topicMap);
            String location = file.toURI().toURL().toExternalForm();
            int hash = location.hashCode();
            Topic imageTopic = createTopic(topicMap, location, " ("+hash+")", file.getName(), imageType);
            imageTopic.setSubjectLocator(new Locator( location ));
            
            // --- ADD LAST MODIFICATION TIME AS OCCURRENCE ---
            try {
                DateFormat dateFormatter = new SimpleDateFormat();
                Topic modType = createTopic(topicMap, "file-modified");
                String dateString = dateFormatter.format( new Date(file.lastModified()) );
                setData(imageTopic, modType, defaultLang, dateString);
            }
            catch(Exception e) {
                log("Exception occurred while setting file topic's modification time!", e);
            }

            // --- ADD FILE SIZE AS OCCURRENCE ---
            try {
                Topic sizeType = createTopic(topicMap, "file-size");
                setData(imageTopic, sizeType, defaultLang, ""+file.length());
            }
            catch(Exception e) {
                log("Exception occurred while setting file topic's file size!", e);
            }

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat();
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(imageTopic, extractionTimeType, defaultLang, dateString);
            
            FileInputStream fis = new FileInputStream(file);
            _extractTopicsFromStream(fis, topicMap, imageTopic);
            if(fis != null) fis.close();
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file '" + file.getName()+"'.", e);
            takeNap(1000);
        }
        return false;
    }

    

    // -------------------------------------------------------------------------
        
        
    public void _extractTopicsFromStream(InputStream in, TopicMap topicMap, Topic imageTopic) throws Exception {
        // --- DEFAULT JPEG HEADERS ---

        JpegHeaderParser jh=new JpegHeaderParser(in);
        String[] cs = jh.getComments();
        if(cs.length>0){
            for(int i=0;i<cs.length;i++){
                // --- OTHER METADATA ---
                Topic metadataType = createTopic(topicMap, "metadata"+i);
                setData(imageTopic, metadataType, defaultLang, cs[i]);
            }
        }

        // --- PHOTOSHOP 3.0 COMMENTS ---
        else {
            byte[][] acs;
            acs=jh.getByteAppComments(0xed); // photoshop comments
            JPEGInfo jpegInfo=getAPP13Comments(acs);
            if(jpegInfo != null) {

                // --- IMAGE AUTHOR ---
                Topic authorType = null;
                Topic authorTopic = null;
                if(jpegInfo.author != null && jpegInfo.author.length() > 0) {
                    authorType = createTopic(topicMap, "author");
                    authorTopic = createTopic(topicMap, jpegInfo.author, " (author)", jpegInfo.author, authorType);                        
                    createAssociation(topicMap, authorType, new Topic[] { imageTopic, authorTopic } );
                }

                // --- IMAGE TITLE ---
                if(jpegInfo.title != null && jpegInfo.title.length() > 0) {
                    setDisplayName(imageTopic, defaultLang, jpegInfo.title);
                }

                // --- IMAGE KEYWORDS ---
                Vector jpegKeywords = jpegInfo.getKeywords();
                if(jpegKeywords != null) {
                    Topic keywordType = createTopic(topicMap, "keyword");
                    for(int i=0; i<jpegKeywords.size(); i++) {
                        Object keyword = jpegKeywords.elementAt(i);
                        String keywordString = keyword.toString();
                        Topic keywordTopic = createTopic(topicMap, keywordString, " (keyword)", keywordString, keywordType);
                        createAssociation(topicMap, keywordType, new Topic[] { imageTopic, keywordTopic} );
                    }
                }

                // --- COMMENT AKA DESCRIPTION ---
                String jpegComment = jpegInfo.getComment();
                if(jpegComment != null && jpegComment.length() > 0) {
                    Topic descriptionType = createTopic(topicMap, "description");
                    setData(imageTopic, descriptionType, defaultLang, jpegComment);
                }

                // --- COPYRIGHT STATUS ---
                if(jpegInfo.copyright != null && jpegInfo.copyright.length() > 0) {
                    Topic copyrightType = createTopic(topicMap, "copyright-status");
                    Topic copyrightTopic = createTopic(topicMap, jpegInfo.copyright, " (copyright)", jpegInfo.copyright, copyrightType);
                    createAssociation(topicMap, copyrightType, new Topic[] { imageTopic, copyrightTopic} );
                }

                // --- COPYRIGHT NOTICE ---
                if(jpegInfo.copyrightNotice != null && jpegInfo.copyrightNotice.length() > 0) {
                    Topic copyrightNoticeType = createTopic(topicMap, "copyright-notice");
                    setData(imageTopic, copyrightNoticeType, defaultLang, jpegInfo.copyrightNotice);
                }

                // --- COPYRIGHT URL ---
                if(jpegInfo.copyrightUrl != null && jpegInfo.copyrightUrl.length() > 0) {
                    Topic copyrightUrlType = createTopic(topicMap, "copyright-url");
                    setData(imageTopic, copyrightUrlType, defaultLang, jpegInfo.copyrightUrl);
                }
            }
        }
    }


    // ----------------------------


   public Topic createImageTypeTopic(TopicMap tm) throws Exception {
        Topic t = createTopic(tm, "JPG image");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, t, w);
        return t;
    }


    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }



    // -------------------------------------------------------------------------



    @Override
    public Locator buildSI(String siend) {
        if(siend.startsWith("http://")) {
            return new Locator(siend);
        }
        else if(siend.startsWith("file:/")) {
            return new Locator(siend);
        }
        else {
            try {
                return new Locator(new File(siend).toURI().toString());
            }
            catch(Exception e) {
                return new Locator("file:/" + siend);
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    

    
    public JPEGInfo getAPP13Comments(byte[][] data){
        int length=0;
        for(int i=0;i<data.length;i++){length+=data[i].length;}
        byte[] d=new byte[length];
        int offs=0;
        for(int i=0;i<data.length;i++){
            System.arraycopy(data[i],0,d,offs,data[i].length);
            offs+=data[i].length;
        }
        return getAPP13Comments(d);
    }
    
    public JPEGInfo getAPP13Comments(byte[] data){
        short[] datai=new short[data.length];
        
        Vector keywords=new Vector();
        String comment = "";
        String commentWriter = "";
        String title = "";
        String author = "";
        String copyright = "";
        String copyrightNotice = "";
        String copyrightUrl = "";
        
        for(int i=0;i<data.length;i++){
            datai[i]=data[i];
            if(datai[i]<0) datai[i]+=256;
        }        
        if(data.length<14) return null; // not a Photoshop 3.0 block.
        String id=new String(data,0,13);
        if(!id.equals("Photoshop 3.0")) return null; // not a Photoshop 3.0 block.
        int offs=14;
        try{
            while(offs<data.length) {
                if( !( datai[offs]==0x38 && datai[offs+1]==0x42 && datai[offs+2]==0x49 && datai[offs+3]==0x4d) ){ // type (is always "8BIM")
                    break;
                }
                offs+=4;
                int identifier=(datai[offs]<<8)|datai[offs+1];
                offs+=2;
                int nlength=datai[offs++]; // a Pascal String (first byte is lenth, the rest is the actual string)
                String name=new String(data,offs,nlength);
                offs+=nlength;
                if((1+nlength)%2==1) offs++; // name padded to make even bytes (null string is 0x00 0x00)
                int length=(datai[offs]<<24)|(datai[offs+1]<<16)|(datai[offs+2]<<8)|(datai[offs+3]); // lenth of the data
                offs+=4;
                if(identifier==0x0404) { // IPTC data stream
                    int iptcend=offs+length;
                    while(offs<iptcend){
                        if(datai[offs++]!=0x1c) break; // dataset separator
                        int record=datai[offs++];
                        int dataset=datai[offs++];
                        int dlength=(datai[offs]<<8)|datai[offs+1];
                        offs+=2;
                        if(record==0x02 && dataset==0x78) { // comment dataset
                            comment+=new String(data,offs,dlength);
                        }
                        if(record==0x02 && dataset==0x19) { // keyword dataset
                            String keyword=new String(data,offs,dlength);
                            keywords.add(keyword);
                        }
                        if(record==0x02 && dataset==0x7a) { // comment writer dataset
                            commentWriter+=new String(data,offs,dlength);
                        }
                        if(record==0x02 && dataset==0x50) { // author dataset
                            author+=new String(data,offs,dlength);
                        }
                        if(record==0x02 && dataset==0x05) { // title dataset
                            title+=new String(data,offs,dlength);
                        }
                        if(record==0x02 && dataset==0x74) { // copright notice dataset
                            copyrightNotice+=new String(data,offs,dlength);
                        }
                        offs+=dlength;
                    }
                }
                else { 
                    offs+=length; // otherwise skip
                }
                if(length%2==1) offs++; // data padded to make size even
            }
        }
        catch(ArrayIndexOutOfBoundsException e) {
        }
        return new JPEGInfo(title, author, comment, commentWriter, keywords, copyright, copyrightNotice, copyrightUrl);
    }
    
    
    
    
    
    public static final String[] contentTypes=new String[] { "image/jpeg", "image/jpg" };
    public String[] getContentTypes() {
        return contentTypes;
    }
    
}

class JPEGInfo {
    String title;
    String author;
    String comment;
    String commentWriter;
    Vector keywords;
    String copyright;
    String copyrightNotice;
    String copyrightUrl;
    
    public JPEGInfo(){
        comment=null;
        keywords=new Vector();
    }
    public JPEGInfo(String c,Vector k) {
        comment=c;
        keywords=k;
    }
    public JPEGInfo(String t, String a, String c, String cw, Vector k, String copyright, String copyrightNotice, String copyrightUrl) {
        this.title = t;
        this.author = a;
        this.comment = c;
        this.commentWriter = cw;
        this.keywords = k;
        this.copyright = copyright;
        this.copyrightNotice = copyrightNotice;
        this.copyrightUrl = copyrightUrl;
    }
    public void setKeywords(Vector v) { keywords=v; }
    public Vector getKeywords() { return keywords; }
    public void setComment(String c) { comment=c; }
    public String getComment() { return comment; }
}






