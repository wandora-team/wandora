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
package org.wandora.modules;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.servlet.AbstractAction;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 * A module that provides email sending services for other modules. 
 * Requires details of the SMTP server in the module parameters passed to the
 * init method. These are given in parameters smtpServer, smtpPort smtpUseTLS,
 * smtpUseSSLAuth, smtpUser and smtpPass. In addition to these a default
 * from field and default subject can be specified in parameters defaultFrom 
 * and defaultSubject. These will be used if other modules don't specify anything
 * else for those fields.
 * 
 * @author olli
 */


public class EmailModule extends AbstractModule {

    protected String smtpServer;
    protected int smtpPort=-1;
    protected String smtpUser;
    protected String smtpPass;
    protected boolean smtpTLS=false;
    protected boolean smtpSSLAuth=false;
    
    protected String defaultFrom=null;
    protected String defaultSubject=null;
    protected String defaultCharset="UTF-8";
    protected String defaultContentType="text/plain";
    
    protected Properties mailProps;
    
    public boolean send(List<String> recipients,String from,String subject,MimeMultipart content) {
        if(from==null) from=defaultFrom;
        if(subject==null) subject=defaultSubject;
        try{
//            Properties props=new Properties();
//            props.put("mail.smtp.host",smtpServer);
            Session session=Session.getDefaultInstance(mailProps,null);

            MimeMessage message=new MimeMessage(session);
            if(subject!=null) message.setSubject(subject);
            if(from!=null) message.setFrom(new InternetAddress(from));

            message.setContent(content);

            Transport transport = session.getTransport("smtp");
            if(smtpPort>0) transport.connect(smtpServer,smtpPort,smtpUser,smtpPass);
            else transport.connect(smtpServer,smtpUser,smtpPass);
            Address[] recipientAddresses=new Address[recipients.size()];
            for(int i=0;i<recipientAddresses.length;i++){
                recipientAddresses[i]=new InternetAddress(recipients.get(i));
            }
            transport.sendMessage(message,recipientAddresses);

            return true;
        }
        catch(MessagingException me){
            logging.warn("Couldn't send email",me);
            return false;
        }
    }
    
    public boolean send(List<String> recipients,String from,String subject,List<BodyPart> parts){
        MimeMultipart content=new MimeMultipart();
        try{
            for(BodyPart p : parts){
                content.addBodyPart(p);
            }
            return send(recipients, from, subject, content);
        }
        catch(MessagingException me){
            logging.warn("Couldn't send email",me);
            return false;
        }
    }
/*
    public boolean send(List<String> recipients,String from,String subject,byte[] content,String mimeType){
        try{
            MimeBodyPart mimeBody=new MimeBodyPart();
            mimeBody.setContent(content, mimeType);
            ArrayList<BodyPart> parts=new ArrayList<BodyPart>();
            parts.add(mimeBody);
            return send(recipients, from, subject, parts);
        }
        catch(MessagingException me){
            logging.warn("Couldn't send email",me);
            return false;
        }
    }
*/    
    protected static Pattern charsetPattern=Pattern.compile("(?:^|;)\\s*charset\\s*=\\s*([^;\\s]+)\\s*(?:;|$)", Pattern.CASE_INSENSITIVE);
    public boolean send(List<String> recipients,String from,String subject,Object message,String mimeType){
        String charset;
        if(mimeType!=null){
            Matcher m=charsetPattern.matcher(mimeType);
            if(m.find()){
                charset=m.group(1).toUpperCase();
            }
            else {
                charset=defaultCharset;
                mimeType+="; charset="+defaultCharset.toLowerCase();
            }
        }
        else {
            charset=defaultCharset;
            mimeType=defaultContentType+"; charset="+defaultCharset.toLowerCase();
        }
        
        try{
            MimeBodyPart mimeBody=new MimeBodyPart();
            mimeBody.setContent(message, mimeType);
            ArrayList<BodyPart> parts=new ArrayList<BodyPart>();
            parts.add(mimeBody);
            return send(recipients, from, subject, parts);
            
//            byte[] content=message.getBytes(charset);
//            return send(recipients, from, subject, content, mimeType);
        }
        catch(MessagingException me){
            logging.warn("Couldn't send email",me);
            return false;
        }
/*        catch(UnsupportedEncodingException uee){
            logging.warn("Couldn't send email, unsupported encoding "+charset,uee);
            return false;
        }*/
    }
    
    public boolean send(String recipient,String from,String subject,Object message, String mimeType){
        ArrayList<String> recipients=new ArrayList<String>();
        recipients.add(recipient);
        return send(recipients, from, subject, message, mimeType);
    }
    
    public boolean send(String recipient,String from,String subject,Object message){
        return send(recipient, from, subject, message, defaultContentType+"; charset="+defaultCharset.toLowerCase());
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o;
        
        mailProps=new Properties();
        
        o=settings.get("smtpServer");
        if(o!=null) {
            smtpServer=o.toString();
            mailProps.put("mail.smtp.host",smtpServer);
        }
        
        o=settings.get("smtpPort");
        if(o!=null) {
            smtpPort=Integer.parseInt(o.toString());
            if(smtpPort>0) mailProps.put("mail.smtp.port",smtpPort);
        }
        
        o=settings.get("smtpUseTLS");
        if(o!=null) {
            smtpTLS=Boolean.parseBoolean(o.toString());
            if(smtpTLS) mailProps.put("mail.smtp.starttls.enabled","true");
        }
        
        o=settings.get("smtpUseSSLAuth");
        if(o!=null){
            smtpSSLAuth=Boolean.parseBoolean(o.toString());
            if(smtpSSLAuth) mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        
        o=settings.get("smtpUser");
        if(o!=null) {
            smtpUser=o.toString();
            if(smtpUser!=null) mailProps.put("mail.smtp.auth","true");
        }
        
        o=settings.get("smtpPass");
        if(o!=null) smtpPass=o.toString();
        
        o=settings.get("defaultFrom");
        if(o!=null) defaultFrom=o.toString();
        
        o=settings.get("defaultSubject");
        if(o!=null) defaultSubject=o.toString();
        
        super.init(manager, settings);
    }
    
}
