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
 * 
 * SimpleEmailExtractor.java
 *
 * Created on 20. kesäkuuta 2006, 20:00
 *
 */



package org.wandora.application.tools.extractors.email;



import org.wandora.application.tools.browserextractors.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.utils.*;

import javax.mail.*;
import javax.mail.internet.*;

//import jmbox.oe5dbx.*;
import javax.swing.Icon;
import net.fortuna.mstor.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;



/**
 *
 * @author akivela
 */
public class SimpleEmailExtractor extends AbstractExtractor implements BrowserPluginExtractor {
    
    private ArrayList visitedEmailFolders = null;
    private ArrayList visitedDirectories = null;

    private String defaultLang = "en";
    private boolean shouldExtractHeaders = false;
    private boolean shouldExtractUnknownContentTypeAttachments = false;
    
    private SimpleEmailExtractorPanel gui = null;

    
    /** Creates a new instance of SimpleEmailExtractor */
    public SimpleEmailExtractor() {
    }
    
    
    // ---------------------------------------------------- PLUGIN EXTRACTOR ---
    

    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        String url=request.getSource();
        try{
            ByteArrayInputStream in=new ByteArrayInputStream(request.getContent().getBytes("ISO-8859-1"));
            TopicMap tm=wandora.getTopicMap();
            _extractTopicsFromStream(request.getSource(), in, tm);
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
        wandora.doRefresh();
        return null;
    }




    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        if(request.getContent()!=null && request.isApplication(BrowserExtractRequest.APP_THUNDERBIRD))
            return true;
        else
            return false;
    }

    
    // -------------------------------------------------------------------------

    
    

    @Override
    public String getName() {
        return "Simple Email extractor...";
    }
    @Override
    public String getDescription(){
        return "Extracts text and metadata from email files.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_email.png");
    }
    


   
    
    
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        if(gui == null) {
            gui = new SimpleEmailExtractorPanel(admin);
        }
        gui.showDialog();
        // WAIT TILL CLOSED

        if(gui.wasAccepted()) {
            int resourceType = gui.getEmailResourceType();
            String resourceAddress = gui.getResourceAddress();
            if(resourceAddress != null && resourceAddress.length() > 0) {
                TopicMap tm = admin.getTopicMap();
                try {
                    setDefaultLogger();
                    _extractTopicsFrom(new File(resourceAddress), tm, resourceType);
                    setState(WAIT);
                }
                catch(Exception e) {
                    log(e);
                }
            }
            else {
                log("No email resource given.");
            }
        }
    }
    
    
    
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        try {
            URLConnection uc = null;
            if(getWandora() != null) { uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(url); }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(url.toExternalForm(), uc.getInputStream(), topicMap);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url " + url.toExternalForm(), e);
            takeNap(1000);
        }
        return false;
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        _extractTopicsFromStream("http://wandora.org/si/simple-email-extractor/"+System.currentTimeMillis(), new ByteArrayInputStream(str.getBytes()), topicMap);
        return true;
    }

    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(file, topicMap, SimpleEmailExtractorPanel.EMAIL_RESOURCE);
    }
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap, int type) throws Exception {
        if(file == null) return false;
        if(file.isDirectory()) {
            if(!visitedDirectories.contains(file)) {
                visitedDirectories.add(file);
                log("Extracting from folder '"+file+"'.");
                File[] fs = file.listFiles();
                for(int i=0; i<fs.length; i++) {
                    _extractTopicsFrom(fs[i], topicMap, type);
                }
            }
        }
        else {
            try {
                if(SimpleEmailExtractorPanel.DBX_EMAIL_REPOSITORY == type) {
                    log("Extracting DBX email repository '"+file+"'.");
                    return extractTopicsFromDBX(file, topicMap);
                }
                else if(SimpleEmailExtractorPanel.MBOX_EMAIL_REPOSITORY == type) {
                    log("Extracting MBOX email repository '"+file+"'.");
                    return extractTopicsFromMBOX(file, topicMap);
                }
                else {
                    FileInputStream fis = new FileInputStream(file);
                    try {
                        log("Extracting single email file '"+file+"'.");
                        _extractTopicsFromStream(file.getPath(), fis, topicMap);
                    }
                    finally {
                        if(fis != null) fis.close();
                    }
                    return true;
                }
            }
            catch(Exception e) {
                log("Exception occurred while extracting from file " + file.getName(), e);
                takeNap(1000);
            }
        }
        return false;
    }



    // -------------------------------------------------------------------------

    /*
     * #mstor.mbox.bufferStrategy={default|mapped|direct}
        mstor.mbox.bufferStrategy=default
        #mstor.mbox.cacheBuffers={true|false}
        #mstor.mbox.metadataStrategy={yaml|xml|none}
        mstor.mbox.metadataStrategy=none
        #mstor.mbox.mozillaCompatibility={true|false}
        #mstor.mbox.parsing.relaxed={true|false}
        #mstor.mbox.encoding=UTF-8
     */
    



    private int extractedEmails = 0;
    
    
    public boolean extractTopicsFromMBOX(File file, TopicMap topicMap) throws Exception {
        extractedEmails = 0;
        try {
            visitedEmailFolders = new ArrayList();
            Properties properties = new Properties();
            properties.setProperty("mstor.mbox.metadataStrategy", "none");
            
            log("Initializing MBOX store '"+file.getName()+"'.");
            Session session = Session.getDefaultInstance(properties);
            //Provider mstorProvider = new Provider(Provider.Type.STORE, "mstor", "net.fortuna.mstor.MStorStore", null, null);
            //session.setProvider(mstorProvider);
            MStorStore store = new MStorStore(session, new URLName("mstor:"+file.getAbsolutePath()));
            store.connect();
            Folder folder = store.getDefaultFolder();
            extractTopicsFromFolder(topicMap, folder);
        }
        catch(javax.mail.NoSuchProviderException nspe) {
            log("Unable to read MBOX mail store! No suitable library available!");
            log("In order to read the MBOX store please download mstor's jars (http://mstor.sourceforge.net/)\ninto Wandora's lib directory and add jars to bin/Wandora.bat!");
            log(nspe);
            return false;
        }
        catch(Exception e) {
            log(e);
        }
        catch(NoClassDefFoundError er) {
            log(er);
            log("Unable to read MBOX mail store! No suitable library available!");
            log("In order to read the MBOX store please download mstor's jars (http://mstor.sourceforge.net/)\ninto Wandora's lib directory and add jars to bin/Wandora.bat!");
            return false;
        }
        log("Total "+extractedEmails+" emails extracted.");
        return true;
    }
    
    
    
    public boolean extractTopicsFromDBX(File file, TopicMap topicMap) throws Exception {
        extractedEmails = 0;
        try {
            visitedEmailFolders = new ArrayList();
            Properties properties = new Properties();
            
            log("Initializing DBX store '"+file.getName()+"'.");
            properties.put("mail.store.protocol", "oe5dbx");
            String path = file.getPath();
            properties.put("mail.oe5dbx.path", path);
            Session session = Session.getInstance(properties);
            Store store = session.getStore();
            store.connect();
            Folder folder = store.getDefaultFolder();
            extractTopicsFromFolder(topicMap, folder);
        }
        catch(NoClassDefFoundError er) {
            log("Unable to read DBX mail storage! No suitable library available!");
            log("In order to read the DBX store please download jmbox's jars (https://jmbox.dev.java.net/)\ninto Wandora's lib directory and add jars to bin/Wandora.bat!");
            return false;
        }
        catch(javax.mail.NoSuchProviderException nspe) {
            log("Unable to read DBX mail store! No suitable library available!");
            log("In order to read the DBX store please download jmbox's jars (https://jmbox.dev.java.net/)\ninto Wandora's lib directory and add jars to bin/Wandora.bat!");
            return false;
        }
        catch(Exception e) {
            log(e);
        }
        log("Total "+extractedEmails+" emails extracted.");
        return true;
    }
    
    
    
    // -------------------------------------------------------------------------


    
    
    public void _extractTopicsFromStream(String locator, InputStream inputStream, TopicMap map) {
        try {
            Session session = null;
            Message message = new MimeMessage(session, inputStream);
            extractTopicsFromMessage(map, message);
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    

    
    public void extractTopicsFromFolder(TopicMap map, Folder folder) {
        try {
            visitedEmailFolders.add(folder.getFullName());

            Topic folderType = createFolderTypeTopic(map);           
            Topic folderTopic = createTopic(map, folder.getName() + " (email folder)");
            folderTopic.addType(folderType);

            if(!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
            if((folder.getType() & Folder.HOLDS_MESSAGES) > 0) {
                int c = folder.getMessageCount();
                Message message = null;
                Topic emailType = createEmailTypeTopic(map);
                Topic emailTopic = null;

                log("Found " + c + " messages in folder '"+ folder.getName() + "'.");
                setProgressMax(c);
                setProgress(0);
                for(int i=1; i<=c && !forceStop(); i++) {
                    try {
                        setProgress(i);
                        message = folder.getMessage(i);
                        if(message != null) {
                            emailTopic = extractTopicsFromMessage(map, message);
                            if(emailTopic != null) {
                                createAssociation(map, folderType, new Topic[] { emailTopic, folderTopic }, new Topic[] { emailType, folderType });
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e.toString());
                    }
                }
            }
            
            if((folder.getType() & Folder.HOLDS_FOLDERS) > 0) {
                Folder[] allFolders = folder.list("*");
                Folder anotherFolder = null;

                for(int i=0; i<allFolders.length && !forceStop(); i++) {
                    anotherFolder = allFolders[i];
                    if(!visitedEmailFolders.contains(anotherFolder.getFullName())) {
                        extractTopicsFromFolder(map, anotherFolder);
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        catch(Error e) {
            log(e);
        }
        try {
            if(folder.isOpen()) {
                folder.close(false);
            }
        }
        catch(Exception e) {}
    }
    
    
    
    
    public Topic extractTopicsFromMessage(TopicMap map, Message message) {
        Topic emailType = null;
        Topic emailTopic = null;
        try {
            if(message != null) {
                String subject = message.getSubject();
                if(subject == null) {
                    subject = "No subject";
                }
                extractedEmails++;

                String bnsuffix = null;
                if(message.getSentDate() != null) {
                    bnsuffix = DateFormat.getInstance().format(message.getSentDate());
                }
                else {
                    bnsuffix = System.currentTimeMillis() + "-" + Math.random()*99999;
                }
                String basename = subject + " (" + bnsuffix + ")";

                emailType = createEmailTypeTopic(map);
                emailTopic = createTopic(map, basename, " (email)", basename, emailType);
                emailTopic.setBaseName(basename);
                emailTopic.setDisplayName(defaultLang, subject);

                extractRecipients(map, emailTopic, message);
                extractSender(map, emailTopic, message);
                //extractReplyTo(map, emailTopic, message);
                extractMessageFlags(map, emailTopic, message);
                extractDates(map, emailTopic, message);

                extractContent(map, emailTopic, message);
                if(shouldExtractHeaders) extractHeaders(map, emailTopic, message);
            }
            else {
                log("Rejecting illegal (null) message.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        catch(Error e) {
            log(e);
        }
        return emailTopic;
    }
    
    
    
    public void extractRecipients(TopicMap map, Topic emailTopic, Message message) {
        try {
            Address[] recipients = message.getAllRecipients();
            if(recipients != null) {
                Address recipient = null;
                Topic emailType = createEmailTypeTopic(map);
                for(int i=0; i<recipients.length && !forceStop(); i++) {
                    recipient = recipients[i];
                    if(recipient != null) {
                        Topic addressType = createAddressTypeTopic(map);

                        Topic recipientType = createTopic(map, "email-recipient");
                        Topic recipientTopic = createEmailAddressTopic(recipient, map);

                        createAssociation(map, recipientType, new Topic[] { emailTopic, recipientTopic }, new Topic[] { emailType, addressType } );
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    public void extractSender(TopicMap map, Topic emailTopic, Message message) {
        try {
            Address[] senders = message.getFrom();
            if(senders != null) {
                Topic emailType = createEmailTypeTopic(map);
                Address sender = null;
                for(int i=0; i<senders.length && !forceStop(); i++) {
                    sender = senders[i];
                    if(sender != null) {
                        Topic addressType = createAddressTypeTopic(map);
                        Topic senderType = createTopic(map, "email-sender");
                        Topic senderTopic = createEmailAddressTopic(sender, map);
                        
                        createAssociation(map, senderType, new Topic[] { emailTopic, senderTopic },  new Topic[] { emailType, addressType } );
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    public void extractReplyTo(TopicMap map, Topic emailTopic, Message message) {
        try {
            Address[] replyTo = message.getFrom();
            if(replyTo != null) {
                Topic emailType = createEmailTypeTopic(map);
                Address address = null;
                for(int i=0; i<replyTo.length && !forceStop(); i++) {
                    address = replyTo[i];
                    if(address != null) {
                        Topic addressType = createAddressTypeTopic(map);
                        Topic replyToType = createTopic(map, "email-reply-to");
                        Topic replyToTopic = createEmailAddressTopic(address, map);
                        
                        createAssociation(map, replyToType, new Topic[] { emailTopic, replyToTopic }, new Topic[] { emailType, addressType } );
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    
    public void extractMessageFlags(TopicMap map, Topic emailTopic, Message message) {
        try {
            Flags flags = message.getFlags();
            if(flags != null) {
                if(flags.contains(Flags.Flag.ANSWERED)) {
                    Topic answeredType = createTopic(map, "answered-email");
                    emailTopic.addType(answeredType);
                }
                if(flags.contains(Flags.Flag.DRAFT)) {
                    Topic draftType = createTopic(map, "draft-email");
                    emailTopic.addType(draftType);
                }
                if(flags.contains(Flags.Flag.FLAGGED)) {
                    Topic flaggedType = createTopic(map, "flagged-email");
                    emailTopic.addType(flaggedType);
                }
                if(flags.contains(Flags.Flag.RECENT)) {
                    Topic recentType = createTopic(map, "recent-email");
                    emailTopic.addType(recentType);
                }
                if(flags.contains(Flags.Flag.SEEN)) {
                    Topic seenType = createTopic(map, "seen-email");
                    emailTopic.addType(seenType);
                }
                if(flags.contains(Flags.Flag.USER)) {
                    Topic userType = createTopic(map, "user-email");
                    emailTopic.addType(userType);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    public void extractDates(TopicMap map, Topic emailTopic, Message message) {
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Topic dateType = createTopic(map, "date");
            Topic emailType = createEmailTypeTopic(map);

            try {
                Date rDate = message.getReceivedDate();
                if(rDate != null) {
                    Topic receivedDateType = createTopic(map, "received-date");
                    String rDateString = dateFormatter.format(rDate);
                    Topic rDateTopic = createTopic(map, rDateString);
                    rDateTopic.addType(dateType);
                    createAssociation(map, receivedDateType, new Topic[] { emailTopic, rDateTopic } );
                }
            }
            catch(Exception e) {
                log("Warning: Exception occurred while processing 'received-date' field of an email.");
                e.printStackTrace();
            }
            
            try {
                Date sDate = message.getSentDate();
                if(sDate != null) {
                    Topic sendDateType = createTopic(map, "sent-date");
                    String sDateString = dateFormatter.format(sDate);
                    Topic sDateTopic = createTopic(map, sDateString);
                    sDateTopic.addType(dateType);
                    setData(emailTopic, sendDateType, defaultLang, timeFormatter.format(sDate));
                    createAssociation(map, sendDateType, new Topic[] { emailTopic, sDateTopic }, new Topic[] { emailType, dateType } );
                }
            }
            catch(Exception e) {
                log("Warning: Exception occurred while processing 'sent-date' field of an email.");
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    public void extractContent(TopicMap map, Topic emailTopic, Part part) {
        try {
            Object content = part.getContent();
            String contentType = part.getContentType();
            String lowerCaseType = contentType.toLowerCase();
            
            if(lowerCaseType.startsWith("text/plain")) {
                Topic textContentType = createTopic(map, "text-content");
                String stringContent = (content != null ? content.toString() : "");
                setData(emailTopic, textContentType, defaultLang, Textbox.trimExtraSpaces(stringContent));
            }
            else if(lowerCaseType.startsWith("text/html")) {
                Topic htmlTextContentType = createTopic(map, "html-text-content");
                String stringContent = (content != null ? content.toString() : "");
                setData(emailTopic, htmlTextContentType, defaultLang, Textbox.trimExtraSpaces(stringContent));
            }
            else if(lowerCaseType.startsWith("text/xml") ||
                    lowerCaseType.startsWith("application/xml")) {
                Topic contentTypeTopic = createTopic(map, "xml-content");
                String stringContent = (content != null ? content.toString() : "");
                setData(emailTopic, contentTypeTopic, defaultLang, stringContent);
            }
            else if(lowerCaseType.startsWith("application/msword") ||
                    lowerCaseType.startsWith("application/x-msword") ||
                    lowerCaseType.startsWith("application/x-ms-word") ||
                    lowerCaseType.startsWith("application/x-word")) {
                Topic contentTypeTopic = createTopic(map, "ms-word-text-content");
                String stringContent = MSOfficeBox.getText(part.getInputStream());
                setData(emailTopic, contentTypeTopic, defaultLang, Textbox.trimExtraSpaces(stringContent));
            }
            else if(lowerCaseType.startsWith("application/msexcel") ||
                    lowerCaseType.startsWith("application/x-msexcel") ||
                    lowerCaseType.startsWith("application/x-ms-excel") ||
                    lowerCaseType.startsWith("application/x-excel") ||
                    lowerCaseType.startsWith("application/vnd.ms-excel")) {
                Topic contentTypeTopic = createTopic(map, "ms-excel-text-content");
                String stringContent = MSOfficeBox.getText(part.getInputStream());
                setData(emailTopic, contentTypeTopic, defaultLang, Textbox.trimExtraSpaces(stringContent));
            }
            else if(lowerCaseType.startsWith("application/powerpoint") ||
                    lowerCaseType.startsWith("application/x-mspowerpoint") ||
                    lowerCaseType.startsWith("application/x-ms-powerpoint") ||
                    lowerCaseType.startsWith("application/x-powerpoint") ||
                    lowerCaseType.startsWith("application/vnd.ms-powerpoint")) {
                Topic contentTypeTopic = createTopic(map, "ms-powerpoint-text-content");
                String stringContent = MSOfficeBox.getText(part.getInputStream());
                setData(emailTopic, contentTypeTopic, defaultLang, Textbox.trimExtraSpaces(stringContent));
            }
            else if(lowerCaseType.startsWith("application/pdf")) {
                Topic contentTypeTopic = createTopic(map, "pdf-text-content");
                String stringContent = "";
                try {
                    PDDocument doc = PDDocument.load(part.getInputStream());
                    PDFTextStripper stripper = new PDFTextStripper();
                    stringContent = stripper.getText(doc);
                    doc.close();
                }
                catch(Exception e) {
                    System.out.println("No PDF support!");
                }
                setData(emailTopic, contentTypeTopic, defaultLang, stringContent.trim());
            }
            else if(lowerCaseType.startsWith("multipart")) {
                Multipart multipart = (Multipart) content;
                BodyPart bodypart = null;
                int c = multipart.getCount();
                for(int i=0; i<c; i++) {
                    bodypart = multipart.getBodyPart(i);
                    extractContent(map, emailTopic, bodypart);
                }
            }
            else {
                if(contentType.indexOf(";") > -1) {
                    contentType = contentType.substring(0, contentType.indexOf(";"));
                }
                log("Unsupported attachment type '" + contentType + "' found.");

                if(shouldExtractUnknownContentTypeAttachments) {
                    log("Processing anyway...");
                    Topic contentTypeTopic = createTopic(map, "unknown-content");
                    String unknownContent = (String) content;
                    setData(emailTopic, contentTypeTopic, defaultLang, unknownContent);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        catch(Error e) {
            log(e);
        }
    }

    
    
    public void extractHeaders(TopicMap map, Topic emailTopic, Part part) {
        try {
            Enumeration e = part.getAllHeaders();
            Header header = null;
            String name = null;
            String value = null;
            Topic emailTopicType = createEmailTypeTopic(map);
            Topic headerType = createTopic(map, "header");
            Topic headerNameType = createTopic(map, "header-name");
            Topic headerValueType = createTopic(map, "header-value");
            Topic headerName = null;
            Topic headerValue = null;
            while(e.hasMoreElements()) {
                header = (Header) e.nextElement();
                if(header != null) {
                    name = header.getName();
                    value = header.getValue();
                    if(name != null && value != null && name.length() > 0 && value.length() > 0) {
                        headerName = createTopic(map, name);
                        headerName.addType(headerNameType);
                        headerValue = createTopic(map, value);
                        headerValue.addType(headerValueType);

                        createAssociation(map, headerType, new Topic[] { emailTopic, headerName, headerValue },  new Topic[] { emailTopicType, headerNameType, headerValueType });
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }


    // -------------------------------------------------------------------------


    public Topic createAddressTypeTopic(TopicMap map) throws TopicMapException {
        Topic e = createEmailsTypeTopic(map);
        Topic t = createTopic(map, "email-address");
        t.addType(e);
        return t;
    }


    public Topic createEmailsTypeTopic(TopicMap map) throws TopicMapException {
        Topic t = createTopic(map, "Emails");
        Topic w = createWandoraTypeTopic(map);
        t.addType(w);
        return t;
    }


    public Topic createFolderTypeTopic(TopicMap map) throws TopicMapException {
        Topic t = createTopic(map, "email folder");
        Topic w = createEmailsTypeTopic(map);
        t.addType(w);
        return t;
    }




    public Topic createEmailTypeTopic(TopicMap map) throws TopicMapException {
        Topic t = createTopic(map, "email");
        Topic e = createEmailsTypeTopic(map);
        t.addType(e);
        return t;
    }
    

    public Topic createWandoraTypeTopic(TopicMap map) throws TopicMapException {
        return createTopic(map, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    // -------------------------------------------------------------------------


    private String emailPatternString = "([\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3})";
    private Pattern emailPattern = Pattern.compile(emailPatternString);

    private String namePatternString = "(\\w+.+) \\<(.+)\\>";
    private Pattern namePattern = Pattern.compile(namePatternString);


    public Topic createEmailAddressTopic(Address emailAddress, TopicMap map) throws Exception {
        return createEmailAddressTopic(emailAddress.toString(), null, map);
    }

    public Topic createEmailAddressTopic(String emailAddress, TopicMap map) throws Exception {
        return createEmailAddressTopic(emailAddress, null, map);
    }

    public Topic createEmailAddressTopic(Address emailAddress, Topic additionalType, TopicMap map) throws Exception {
        return createEmailAddressTopic(emailAddress.toString(), additionalType, map);
    }

    public Topic createEmailAddressTopic(String emailAddress, Topic additionalType, TopicMap map) throws Exception {
        //String originalAddress = emailAddress;
        String emailName = null;

        Matcher nameMatcher = namePattern.matcher(emailAddress);
        if(nameMatcher.matches()) {
            emailName = nameMatcher.group(1);
            emailAddress = nameMatcher.group(2);
            //System.out.println("found name '"+emailName+"' and address '"+emailAddress+"' in '"+originalAddress+"'");
        }

        Matcher addressMatcher = emailPattern.matcher(emailAddress);
        if(addressMatcher.matches()) {
            //System.out.println("good! email address '"+emailAddress+"' matches the email pattern!");
        }
        else {
            if(addressMatcher.find()) {
                emailAddress = addressMatcher.group(1);
                //System.out.println("found email address '"+emailAddress+"'!");
            }
        }

        Topic addressType = createAddressTypeTopic(map);
        Topic emailAddressTopic = createTopic(map, emailAddress);
        emailAddressTopic.addType(addressType);
        if(additionalType != null) emailAddressTopic.addType(additionalType);

        if(emailName != null && emailName.length() > 0) {
            Topic nameType = createTopic(map, "name");
            setData(emailAddressTopic, nameType, defaultLang, emailName.trim());
        }

        return emailAddressTopic;
    }



    // -------------------------------------------------------------------------
    

    
    
    @Override
    public Locator buildSI(String siend) {
        try {
            return new Locator("http://wandora.org/si/email/" + URLEncoder.encode(siend, "utf-8"));
        }
        catch(Exception e) {
            return new Locator("http://wandora.org/si/email/" + siend);
        }
    }
    
}
