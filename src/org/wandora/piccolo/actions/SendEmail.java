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
 * 
 *
 * SendEmail.java
 *
 * Created on 24. tammikuuta 2006, 12:15
 */

package org.wandora.piccolo.actions;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.XMLParamProcessor;
import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.piccolo.utils.SimpleDataSource;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;

/**
 *
 * @author olli
 */
public class SendEmail implements Action,XMLParamAware {
    
    private Logger logger;
    private String emailTemplate;
    private String recipient;
    private String from;
    private String subject;
    private String smtpServer;
    private String smtpUser;
    private String smtpPass;

    /** Creates a new instance of SendEmail */
    public SendEmail() {
    }

    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("template")) emailTemplate=processor.getElementContents(e);
                else if(e.getNodeName().equals("usecache")) recipient=processor.getElementContents(e);
                else if(e.getNodeName().equals("from")) from=processor.getElementContents(e);
                else if(e.getNodeName().equals("subject")) subject=processor.getElementContents(e);
                else if(e.getNodeName().equals("recipient")) recipient=processor.getElementContents(e);
                else if(e.getNodeName().equals("smtpserver")) smtpServer=processor.getElementContents(e);
                else if(e.getNodeName().equals("smtpuser")) smtpUser=processor.getElementContents(e);
                else if(e.getNodeName().equals("smtppass")) smtpPass=processor.getElementContents(e);
            }
        }
        if(emailTemplate==null){
            logger.writelog("WRN","emailTemplate not specified");
        }
    }

    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        try{

            Properties props=new Properties();
            props.put("mail.smtp.host",smtpServer);
            Session session=Session.getDefaultInstance(props,null);
            
            MimeMessage message=new MimeMessage(session);
            if(subject!=null) message.setSubject(subject);
            if(from!=null) message.setFrom(new InternetAddress(from));
            Vector<String> recipients=new Vector<String>();
            if(recipient != null) {
                String[] rs = recipient.split(",");
                String r = null;
                for(int i=0; i<rs.length; i++) {
                    r = rs[i];
                    if(r!=null) recipients.add(r);
                }
            }

            MimeMultipart multipart=new MimeMultipart();

            Template template=application.getTemplate(emailTemplate,user);
            HashMap context=new HashMap();
            context.put("request",request);
            context.put("message",message);
            context.put("recipients",recipients);
            context.put("emailhelper",new MailHelper());
            context.put("multipart",multipart);
            context.putAll(application.getDefaultContext(user));

            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            template.process(context,baos);

            MimeBodyPart mimeBody=new MimeBodyPart();
            mimeBody.setContent(new String(baos.toByteArray(),template.getEncoding()),template.getMimeType());
//            mimeBody.setContent(baos.toByteArray(),template.getMimeType());
            multipart.addBodyPart(mimeBody);
            message.setContent(multipart);
            

            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer,smtpUser,smtpPass);
            Address[] recipientAddresses=new Address[recipients.size()];
            for(int i=0;i<recipientAddresses.length;i++){
                recipientAddresses[i]=new InternetAddress(recipients.elementAt(i));
            }
            transport.sendMessage(message,recipientAddresses);
        }
        catch(Exception e){logger.writelog("WRN",e);}
    }
    
    public static class MailHelper {
        public static Address makeInetAddress(String address) throws AddressException {
            return new InternetAddress(address);
        }
        public static List<FileItem> getItems(javax.servlet.ServletRequest request) throws FileUploadException {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(8000000); // larger files than 8M will be temporarily stored in disk
            ServletFileUpload upload=new ServletFileUpload(factory);
            upload.setSizeMax(8000000); // do not accept larger files than 8M
            List<FileItem> items=(List<FileItem>)upload.parseRequest((javax.servlet.http.HttpServletRequest)request);
            return items;
        }
        public static byte[] getBytes(List<FileItem> items,String name){
            FileItem i=getItem(items,name);
            if(i!=null) return i.get();
            else return null;
        }
        public static FileItem getItem(javax.servlet.ServletRequest request,String name) throws FileUploadException {
            List<FileItem> items=getItems(request);
            return getItem(items,name);
        }
        public static FileItem getItem(List<FileItem> items,String name){
            for(FileItem item : items){
                if(item.getFieldName()!=null && item.getFieldName().equals(name)) return item;
            }
            return null;            
        }
        public String getParameter(String name,List<FileItem> items){
            for(FileItem item : items){
                if(item.getFieldName()!=null && item.getFieldName().equals(name)) return item.getString();
            }
            return null;                        
        }
        public Collection<String> getParameters(String name,List<FileItem> items){
            Vector<String> v=new Vector<String>();
            for(FileItem item : items){
                if(item.getFieldName()!=null && item.getFieldName().equals(name)) v.add(item.getString());
            }
            return v;
        }
        public static void addAttachment(MimeMultipart multipart,FileItem file) throws IOException,MessagingException {
            MimeBodyPart body=new MimeBodyPart();
            body.setDataHandler(new DataHandler(new SimpleDataSource(file.getName(),file.getContentType(),file.getInputStream())));
            SimpleDataSource.addBase64Header(body);
            body.addHeader("Content-Type",file.getContentType()+"; filename=\""+file.getName()+"\"");
            body.addHeader("Content-Disposition","inline; filename=\""+file.getName()+"\"");
            multipart.addBodyPart(body);
        }
/*        public static void addBodyPart(MimeMultipart multipart,byte[] bytes,String mimeType) throws MessagingException {
            MimeBodyPart body=new MimeBodyPart();
            body.setContent(bytes,mimeType);
            multipart.addBodyPart(body);
        }*/
    }
}
