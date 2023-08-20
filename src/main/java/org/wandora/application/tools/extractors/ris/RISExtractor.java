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
 * RISExtractor.java
 *
 * Created on March 3, 2008, 1:45 PM
 */




package org.wandora.application.tools.extractors.ris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ris.RISReference.Date;
import org.wandora.application.tools.extractors.ris.RISReference.ReferenceType;
import org.wandora.application.tools.extractors.ris.RISReference.Types;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;
import org.wandora.utils.Tuples.T2;

/**
 * 
 * @author anttirt
 */
public class RISExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;

	@Override
    public String getName()
    {
        return "RIS bibliography extractor";
    }
    
    @Override
    public String getDescription()
    {
        return "Extracts data from RIS bibliography format files";
    }
    

    
    @Override
    public Topic createTopic(TopicMap topicMap, String baseString) throws TopicMapException {
        return createTopic(topicMap, baseString, "", baseString, new Topic[] { });
    }
    
    @Override
    public Topic createTopic(TopicMap topicMap, String siString, String baseString) throws TopicMapException {
        return createTopic(topicMap, siString, "", baseString, new Topic[] { });
    }
    
    @Override
    public Topic createTopic(TopicMap topicMap, String siString, String baseString, Topic type) throws TopicMapException {
        return createTopic(topicMap, siString, "", baseString, new Topic[] { type });
    }
    
    @Override
    public Topic createTopic(TopicMap topicMap, String baseString, Topic type)  throws TopicMapException {
        return createTopic(topicMap, baseString, "", baseString, new Topic[] { type });
    }

    @Override
    public Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString)  throws TopicMapException {
        return createTopic(topicMap, siString, baseNameString, baseString, new Topic[] { });
    }


    @Override
    public Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic type)  throws TopicMapException {
        return createTopic(topicMap, siString, baseNameString, baseString, new Topic[] { type });
    }


    @Override
    public Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic[] types)  throws TopicMapException {
        if(baseString != null && baseString.length() > 0) {
            Locator si = buildSI(siString);
            Topic t = topicMap.getTopic(si);
            if(t == null) {
                t = topicMap.getTopicWithBaseName(baseString + baseNameString);
                if(t == null) {
                    t = topicMap.createTopic();
                    t.setBaseName(baseString + baseNameString);
                }
                t.addSubjectIdentifier(si);
            }

            //setDisplayName(t, "fi", baseString);
            setDisplayName(t, "en", baseString);
            for(int i=0; i<types.length; i++) {
                Topic typeTopic = types[i];
                if(typeTopic != null) {
                    t.addType(typeTopic);
                }
            }
            return t;
        }
        System.out.println("Failed to create topic!");
        return null;
    }
    
    @Override
    public boolean useTempTopicMap()
    {
        return true;
    }
    
    private void makeSubClass(TopicMap map, Topic subClass, Topic superClass)
    {
        try
        {
            Topic supersubClassTopic = null;
            Topic subClassTopic = null;
            Topic superClassTopic = null;
            
            Locator l = new Locator(XTMPSI.SUPERCLASS_SUBCLASS);
            if((supersubClassTopic = map.getTopic(l)) == null)
            {            
                supersubClassTopic = map.createTopic();
                supersubClassTopic.addSubjectIdentifier(l);
            }
            
            l = new Locator(XTMPSI.SUBCLASS);
            if((subClassTopic = map.getTopic(l)) == null)
            {
                subClassTopic = map.createTopic();
                subClassTopic.addSubjectIdentifier(l);
            }
            
            l = new Locator(XTMPSI.SUPERCLASS);
            if((superClassTopic = map.getTopic(l)) == null)
            {
                superClassTopic = map.createTopic();
                superClassTopic.addSubjectIdentifier(l);
            }
            
            Association as = map.createAssociation(supersubClassTopic);
            as.addPlayer(subClass, subClassTopic);
            as.addPlayer(superClass, superClassTopic);
        }
        catch(TopicMapException e)
        {
            log(e);
        }
    }
    
    @Override
    public boolean isConfigurable()
    {
        return true;
    }
    
    private String mEncoding = "UTF-8";
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException
    {
        mEncoding = WandoraOptionPane.showInputDialog(admin, "Please enter default encoding", "UTF-8", "Select encoding");
    }
    
    private String url(String str)
    {
        try
        {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch(Exception e)
        {
            return "";
        }
    }
    
    private boolean extract(CharBuffer cb, TopicMap map)
    {
        log("Starting RIS extract");
        boolean gotsome = false;

        
        try
        {
            Topic wandoraClass = map.createTopic();
            wandoraClass.addSubjectIdentifier(new Locator(TMBox.WANDORACLASS_SI));
            wandoraClass.setBaseName("Wandora class");

            Topic RISClass = map.createTopic();
            RISClass.addSubjectIdentifier(new Locator("http://wandora.org/si/ris/"));
            RISClass.setBaseName("RIS");
            RISClass.addType(wandoraClass);

            
            /**
             * Central player
             */
            Topic tyTitle = createTopic(map, "title", RISClass);
            Topic tyBook = createTopic(map, "book", RISClass);
            
            /**
             * Key players
             */
            Topic tyAuthor = createTopic(map, "author", RISClass);
            Topic tyKeyword = createTopic(map, "keyword", RISClass);
            Topic tyPage = createTopic(map, "page", RISClass);
            Topic tyPeriodical = createTopic(map, "periodical", RISClass);
            Topic tySeries = createTopic(map, "series", RISClass);
            Topic tyDate = createTopic(map, "date", RISClass);
            Topic tySerial = createTopic(map, "isbn/issn", RISClass);
            Topic tyCity = createTopic(map, "city", RISClass);
            Topic tyPublisher = createTopic(map, "publisher", RISClass);
            Topic tyIssue = createTopic(map, "issue", RISClass);
            Topic tyProceeding = createTopic(map, "proceeding", RISClass);
            Topic tyArticle = createTopic(map, "article", RISClass);
            Topic tyReport = createTopic(map, "report", RISClass);
            
            /**
             * Presentation context (conference, magazine, hearing, etc)
             */
            Topic tyPresentationContext = createTopic(map, "presentationContext", RISClass);
            
            /**
             * Contexts with specialized processing
             */
            Topic tyJournal = createTopic(map, "journal", RISClass);
                makeSubClass(map, tyJournal, tyPresentationContext);
                
            Topic tyMagazine = createTopic(map, "magazine", RISClass);
                makeSubClass(map, tyMagazine, tyPresentationContext);
                
            Topic tyConference = createTopic(map, "conference", RISClass);
                makeSubClass(map, tyConference, tyPresentationContext);
                
            Topic tyNewspaper = createTopic(map, "newspaper", RISClass);
                makeSubClass(map, tyNewspaper, tyPresentationContext);
                
            
            
            /**
             * Occurrence types
             */
            Topic tyAbstract = createTopic(map, "abstract", RISClass);
            Topic tyNotes = createTopic(map, "notes", RISClass);
            Topic tyWebURL = createTopic(map, "webURL", RISClass);
            Topic tyPDFURL = createTopic(map, "PDFURL", RISClass);
            Topic tyFullTextURL = createTopic(map, "fullTextURL", RISClass);
            Topic tyMiscInfo = createTopic(map, "miscInfo", RISClass);
            Topic tyUserInfo = createTopic(map, "userInfo", RISClass);
            Topic tyUnknownInfo = createTopic(map, "unknownInfo", RISClass);
            Topic tyDOI = createTopic(map, "DOI", RISClass);

            /**
             * Associations
             */
            Topic asKeyword = createTopic(map, "isKeyword", RISClass);
            Topic asPrimaryAuthor = createTopic(map, "isPrimaryAuthor", RISClass);
            Topic asSecondaryAuthor = createTopic(map, "isSecondaryAuthor", RISClass);
            Topic asContains = createTopic(map, "contains", RISClass);
            Topic asPublished = createTopic(map, "published", RISClass);
            Topic asPublisher = createTopic(map, "publisher", RISClass);
            Topic asInSeries = createTopic(map, "inSeries", RISClass);
            Topic asPeriodical = createTopic(map, "isPeriodical", RISClass);
            
            /**
             * Roles
             */
            Topic roStartPage = createTopic(map, "startPage", RISClass);
            Topic roEndPage = createTopic(map, "endPage", RISClass);
            Topic roPublication = createTopic(map, "publication", RISClass);
            
            /**
             * languages
             */
            Topic lanT = map.getTopic(XTMPSI.getLang(null));
            if(lanT == null)
            {
                lanT = map.createTopic();
                lanT.addSubjectIdentifier(new Locator(XTMPSI.getLang(null)));
                lanT.setBaseName("");
            }
            Topic enLangT = map.getTopic(XTMPSI.getLang("en"));
            if(enLangT == null)
            {
                enLangT = map.createTopic();
                enLangT.addSubjectIdentifier(new Locator(XTMPSI.getLang("en")));
                enLangT.setBaseName("");
            }
            
            while(cb.hasRemaining())
            {
                RISReference ref = new RISReference(cb, this);
                gotsome = true;
                
                log("Extracting " + ref.titleTag);
                // Title
                Topic titleT;
                
                // Publication info
                switch(ref.refType)
                {
                    case JOUR: case MGZN: // the title is an article in a magazine or journal
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (article)", ref.titleTag, tyTitle);
                        String issueStr = ref.periodicalName + " " + (ref.issue == null ? "[unknown issue]" : ref.issue);
                        Topic issueT = createTopic(map, url(issueStr), " (issue)", issueStr, tyIssue);
                        
                        Topic periodicalT = ref.refType == ReferenceType.MGZN ?
                            createTopic(map, url(ref.periodicalName), " (magazine)", ref.periodicalName, tyMagazine) :
                            createTopic(map, url(ref.periodicalName), " (journal)", ref.periodicalName, tyJournal);
                        
                        createAssociation(map, asPeriodical, new Topic[] { issueT, periodicalT });
                        
                        if(ref.startPage != null && ref.endPage != null)
                        {
                            Topic spT = createTopic(map, url(ref.startPage), " (page)", ref.startPage, tyPage);
                            Topic epT = createTopic(map, url(ref.endPage), " (page)", ref.endPage, tyPage);
                            createAssociation(map, asContains, new Topic[]{ issueT, titleT, spT, epT }, new Topic[]{ roPublication, tyTitle, roStartPage, roEndPage });
                        }
                        else
                        {
                            createAssociation(map, asContains, new Topic[]{ issueT, titleT }, new Topic[]{ roPublication, tyTitle });
                        }
                        
                        break;
                    }
                    
                    case BOOK: // the title is a book
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (book)", ref.titleTag, tyTitle);
                        titleT.addType(tyBook);
                        break;
                    }
                    
                    case CHAP: case CHAPTER: // the title is a chapter in a book
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (chapter)", ref.titleTag, tyTitle);
                        Topic bookT = createTopic(map, url(ref.periodicalName), " (book)", ref.periodicalName, tyBook);
                        if(ref.startPage != null && ref.endPage != null)
                        {
                            Topic spT = createTopic(map, url(ref.startPage), " (page)", ref.startPage, tyPage);
                            Topic epT = createTopic(map, url(ref.endPage), " (page)", ref.endPage, tyPage);
                            createAssociation(map, asContains, new Topic[]{ bookT, titleT, spT, epT }, new Topic[]{ roPublication, tyTitle, roStartPage, roEndPage });
                        }
                        else
                        {
                            createAssociation(map, asContains, new Topic[]{ bookT, titleT }, new Topic[]{ roPublication, tyTitle });
                        }
                        break;
                    }

                    case CONF:
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (conference proceeding)", ref.titleTag, tyProceeding);
                        Topic confT = createTopic(map, url(ref.seriesTag), " (conference)", ref.seriesTag, tyConference);
                        if(ref.startPage != null && ref.endPage != null)
                        {
                            Topic spT = createTopic(map, url(ref.startPage), " (page)", ref.startPage, tyPage);
                            Topic epT = createTopic(map, url(ref.endPage), " (page)", ref.endPage, tyPage);
                            createAssociation(map, asContains, new Topic[]{ confT, titleT, spT, epT }, new Topic[]{ tyConference, tyProceeding, roStartPage, roEndPage });
                        }
                        else
                        {
                            createAssociation(map, asContains, new Topic[]{ confT, titleT });
                        }
                        break;
                    }
                    
                    case NEWS:
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (article)", ref.titleTag, tyArticle);
                        Topic paperT = createTopic(map, url(ref.periodicalName), " (newspaper)", ref.periodicalName, tyNewspaper);
                        if(ref.startPage != null && ref.endPage != null)
                        {
                            Topic spT = createTopic(map, url(ref.startPage), " (page)", ref.startPage, tyPage);
                            Topic epT = createTopic(map, url(ref.endPage), " (page)", ref.endPage, tyPage);
                            createAssociation(map, asContains, new Topic[]{ paperT, titleT, spT, epT }, new Topic[]{ roPublication, tyArticle, roStartPage, roEndPage });
                        }
                        else
                        {
                            createAssociation(map, asContains, new Topic[]{ paperT, titleT });
                        }
                        break;
                    }
                    
                    case INPR: // "The section "Articles in Press" contains peer reviewed accepted articles to be published in this journal."
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (article)", ref.titleTag, tyArticle);
                        Topic journalT = createTopic(map, url(ref.periodicalName), " (journal)", ref.periodicalName, tyJournal);
                        createAssociation(map, asContains, new Topic[]{ journalT, titleT });
                        break;
                    }
                    
                    case RPRT:
                    {
                        titleT = createTopic(map, url(ref.titleTag), " (report)", ref.titleTag, tyReport);
                        Topic contextT = createTopic(map, url(ref.periodicalName), "", ref.periodicalName, tyPresentationContext);
                        if(ref.startPage != null && ref.endPage != null)
                        {
                            Topic spT = createTopic(map, url(ref.startPage), " (page)", ref.startPage, tyPage);
                            Topic epT = createTopic(map, url(ref.endPage), " (page)", ref.endPage, tyPage);
                            createAssociation(map, asContains, new Topic[]{ contextT, titleT, spT, epT }, new Topic[]{ roPublication, tyReport, roStartPage, roEndPage });
                        }
                        else
                        {
                            createAssociation(map, asContains, new Topic[]{ contextT, titleT });
                        }
                        break;
                    }
                    
                    default:
                        titleT = createTopic(map, url(ref.titleTag), RISReference.getWart(ref.refType), ref.titleTag, tyTitle);
                        break;
                }
                
                // Publishing info
                {
                    Topic
                            publisherT = ref.publisher == null ? null : createTopic(map, url(ref.publisher), " (publisher)", ref.publisher, tyPublisher),
                            cityT = ref.publicationCity == null ? null : createTopic(map, url(ref.publicationCity), " (city)", ref.publicationCity, tyCity),
                            serialT = ref.serial == null ? null : createTopic(map, url(ref.serial), " (serial)", ref.serial, tySerial),
                            dateT = null;
                    
                    if(ref.dateTag != null)
                    {
                        String dateStr = getDateString(ref.dateTag);
                        dateT = createTopic(map, url(dateStr), " (date)", dateStr, tyDate);
                    }
                    
                    ArrayList<Topic> players = new ArrayList<Topic>();
                    
                    if(publisherT != null) players.add(publisherT);
                    if(cityT != null) players.add(cityT);
                    if(serialT != null) players.add(serialT);
                    if(dateT != null) players.add(dateT);
                    
                    if(players.size() != 0)
                    {
                        players.add(titleT);
                        createAssociation(map, asPublished, players.toArray(new Topic[players.size()]));                        
                    }
                    
                    if(ref.doi != null)
                    {
                        final String doiString = ref.doi.toString();
                        titleT.setData(tyDOI, lanT, doiString);
                        if(ref.mainURL.size() == 0)
                            titleT.setData(tyWebURL, lanT, "http://dx.doi.org/" + doiString);
                        titleT.addSubjectIdentifier(new Locator("http://dx.doi.org/" + doiString));
                    }
                }
                
                // Authors
                for(T2<Types, String> author : ref.authorTags)
                {
                    Topic authorT = createTopic(map, url(author.e2), " (author)", author.e2, tyAuthor);
                    
                    switch(author.e1)
                    {
                        case A1: case AU:
                            createAssociation(map, asPrimaryAuthor, new Topic[] { authorT, titleT });
                            break;
                        case A2: case ED: case A3:
                            createAssociation(map, asSecondaryAuthor, new Topic[] { authorT, titleT });
                            break;
                        default:
                            break;
                    }
                }
                
                // Keywords
                for(String keyword : ref.keyWords)
                {
                    Topic keywordT = createTopic(map, url(keyword), " (keyword)", keyword, tyKeyword);
                    createAssociation(map, asKeyword, new Topic[] { titleT, keywordT });
                }
                
                // Notes/abstract
                for(T2<Types, String> note : ref.noteTags)
                {
                    switch(note.e1)
                    {
                        case N1: case AB:
                            titleT.setData(tyNotes, enLangT, note.e2);
                            break;
                        case N2:
                            titleT.setData(tyAbstract, enLangT, note.e2);
                            break;
                    }
                }
                
                // URLs
                {
                    ArrayList<String> urls = new ArrayList<String>();

                    for(URL url : ref.mainURL) urls.add(url.toString());
                    if(urls.size() != 0) titleT.setData(tyWebURL, lanT, implode(urls.toArray(new String[urls.size()]), ";"));
                    urls.clear();

                    for(URL url : ref.pdfURL) urls.add(url.toString());
                    if(urls.size() != 0) titleT.setData(tyPDFURL, lanT, implode(urls.toArray(new String[urls.size()]), ";"));
                    urls.clear();

                    for(URL url : ref.fullTextURL) urls.add(url.toString());
                    if(urls.size() != 0) titleT.setData(tyFullTextURL, lanT, implode(urls.toArray(new String[urls.size()]), ";"));
                }
                
                // Miscellanea
                for(T2<Types, String> userInfo : ref.userTags)
                {
                    Topic userInfoT = createTopic(map, url(userInfo.e1.toString()), " (user info)", userInfo.e1.toString(), tyUserInfo);
                    titleT.setData(userInfoT, lanT, userInfo.e2);
                }
                for(T2<Types, String> miscInfo : ref.userTags)
                {
                    Topic miscInfoT = createTopic(map, url(miscInfo.e1.toString()), " (user info)", miscInfo.e1.toString(), tyMiscInfo);
                    titleT.setData(miscInfoT, lanT, miscInfo.e2);
                }
                for(T2<Types, String> unknownInfo : ref.userTags)
                {
                    Topic unknownInfoT = createTopic(map, url(unknownInfo.e1.toString()), " (user info)", unknownInfo.e1.toString(), tyUnknownInfo);
                    titleT.setData(unknownInfoT, lanT, unknownInfo.e2);
                }
            }
        }
        catch(TopicMapException e)
        {
            log(e);
        }
        catch(IllegalArgumentException e)
        {
            log(e);
        }
        
        return gotsome;
    }
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap map) throws Exception {
            
        CharBuffer cb = null;
        {
            FileInputStream fis = null;
            MappedByteBuffer mbb = null;

            Charset cs = Charset.forName(mEncoding);
            CharsetDecoder decoder = cs.newDecoder();

            try
            {
                fis = new FileInputStream(f);
                FileChannel fc = fis.getChannel();
                int fs = (int)fc.size(); 
                mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fs);
                cb = decoder.decode(mbb);
            }
            catch(FileNotFoundException e) { log(e); return false; }
            catch(CharacterCodingException e) { log(e); return false; }
            catch(IOException e) { log(e); return false; }
        }
        
        return extract(cb, map);
    }
    
    public final String[] risContentTypes = { "text/html", "text/plain", "application/x-Research-Info-Systems", "application/xhtml+xml" };
    
    @Override
    public String[] getContentTypes()
    {
        return risContentTypes;
    }
    private String implode(String[] vals, String sep)
    {
        StringBuilder bldr = new StringBuilder();
        
        for(int i = 0; i < vals.length - 1; ++i)
        {
            bldr.append(vals[i]);
            bldr.append(sep);
        }
        bldr.append(vals[vals.length - 1]);
        
        return bldr.toString();
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        CharBuffer cb = null;
        
        String s = IObox.doUrl(u);
        cb = CharBuffer.wrap(s);
        
        return extract(cb, t);
    }
    
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return extract(CharBuffer.wrap(str), topicMap);
    }



    
    public static void main(String[] args) throws Exception {
        Charset cs = Charset.forName("UTF-8");
        CharsetDecoder decoder = cs.newDecoder();
        FileInputStream fis = new FileInputStream("C:\\test.ris");
        FileChannel fc = fis.getChannel();
        int fs = (int)fc.size();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fs);
        CharBuffer cb = decoder.decode(mbb);

        try
        {
            for(;;)
            {
                RISReference ref = new RISReference(cb, null);
                String refName = ref.titleTag;
                String bar = refName + ".";
            }
        }
        catch(IllegalArgumentException e)
        {
            System.out.println("foobar");
        }
    }

    /**
     * ISO 8601
     * @param date
     * @return
     */
    protected String getDateString(final Date date) {
        String dateString = date.Year == null ? "0" : String.valueOf(date.Year);
        if (date.Month != null) {
            dateString += "-" + String.valueOf(date.Month);
            if (date.Day != null) {
                dateString += "-" + String.valueOf(date.Day);
            }
        }
        
        return dateString;
    }
}
