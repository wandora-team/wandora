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
 */
package org.wandora.modules.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.wandora.modules.EmailModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.User;

/**
 * Sends an email composed using a template. This action extends GenericTemplateAction
 * but behaves a bit differently than most of them. The template is used to compose
 * the email, not to send a response to the actual action. In fact, no response is
 * ever sent to the http request, instead you have to use this action through a
 * ChainedAction and respond in the next action in the chain.
 * 
 * Also note that this action extends CachedAction via GenericTemplateAction but does
 * not in fact support caching. If you try to turn on caching, a warning is
 * written to the log and caching is turned off. In fact, handleAction is overridden
 * in such a way that no caching will take place regardless of whether it's turned on or not.
 * 
 * @author olli
 */


public class SendEmailAction extends GenericTemplateAction {

    protected EmailModule email;
    
    protected String from=null;
    protected String subject=null;
    protected String recipients=null;
    protected boolean parseSubject=false;
    protected boolean parseRecipients=false;
    protected boolean parseFrom=false;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(EmailModule.class, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        
        Object o=settings.get("subject");
        if(o!=null) subject=o.toString();
        
        o=settings.get("recipient");
        if(o!=null) recipients=o.toString();        
        o=settings.get("recipients");
        if(o!=null) recipients=o.toString();
        
        o=settings.get("from");
        if(o!=null) from=o.toString();
        
        o=settings.get("parseSubject");
        if(o!=null) parseSubject=Boolean.parseBoolean(o.toString());
       
        o=settings.get("parseRecipient");
        if(o!=null) parseRecipients=Boolean.parseBoolean(o.toString());        
        o=settings.get("parseRecipients");
        if(o!=null) parseRecipients=Boolean.parseBoolean(o.toString());
        
        o=settings.get("parseFrom");
        if(o!=null) parseFrom=Boolean.parseBoolean(o.toString());
        
        super.init(manager, settings);
        
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(caching) {
            logging.warn("this action does not support caching, caching turned off");
            caching=false;
        }
        email=manager.findModule(EmailModule.class);
        super.start(manager);
    }

    
    @Override
    public void stop(ModuleManager manager) {
        email=null;
        super.stop(manager);
    }

    public static class EmailContent {
        public String subject;
        public String recipients;
        public String from;
        public String message;
    }
    public static EmailContent parseEmail(byte[] bytes, String encoding,boolean parseRecipients, boolean parseSubject, boolean parseFrom) throws IOException {
        String content=new String(bytes,encoding);        
        EmailContent ret=new EmailContent();

        for(int i=0;i<(parseRecipients?1:0)+(parseSubject?1:0)+(parseFrom?1:0);i++){
            int ind=content.indexOf("\n");
            if(ind<0) return null;
            
            String line=content.substring(0, ind).trim();
            if(line.length()==0) continue;

            content=content.substring(ind+1); 
            
            int ind2=line.indexOf(":");
            if(ind2<0) return null;
            
            String key=line.substring(0,ind2).trim().toLowerCase();
            String value=line.substring(ind2+1).trim();
            
            if(key.equals("subject")){
                ret.subject=value;
            }
            else if(key.equals("recipient") || key.equals("recipients")){
                ret.recipients=value;
            }
            else if(key.equals("from")){
                ret.from=value;
            }
        }
        ret.message=content;
        return ret;
    }
    
    protected boolean parseOutputAndSend(byte[] bytes,String contentType,String encoding) throws IOException{

        String content=new String(bytes,encoding);        
        
        String sendSubject=subject;
        String sendRecipients=recipients;
        String sendFrom=from;
        
        String mimeType=contentType+"; charset="+encoding.toLowerCase();
        
        EmailContent ec=parseEmail(bytes, encoding, parseRecipients, parseSubject, parseFrom);
        if(ec==null) {
            logging.warn("unable to parse email headers");
            return false;
        }
        content=ec.message;
        if(parseSubject && ec.subject!=null) sendSubject=ec.subject;
        else if(!parseSubject && ec.subject!=null) logging.warn("Found subject at the start of email but parseSubject is false");

        if(parseRecipients && ec.recipients!=null) sendRecipients=ec.recipients;
        else if(!parseRecipients && ec.recipients!=null) logging.warn("Found recipients at the start of email but parseRecipients is false");

        if(parseFrom && ec.from!=null) sendFrom=ec.from;
        else if(!parseFrom && ec.from!=null) logging.warn("Found from at the start of email but parseFrom is false");
        
        String[] split=sendRecipients.split("\\s*,\\s*");
        
        return email.send(Arrays.asList(split), sendFrom, sendSubject, content, mimeType);
    }
    
    @Override
    public boolean handleAction(final HttpServletRequest req, final HttpServletResponse resp, final HttpMethod method, final String action, final User user) throws ServletException, IOException, ActionException {
        Template template=getTemplate(req, method, action);
        if(template==null) return false;

        Map<String,Object> context=getTemplateContext(template, req, method, action, user);
        if(context==null) return false;

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        template.process(context, baos);
        
        return parseOutputAndSend(baos.toByteArray(), template.getMimeType(), template.getEncoding());
    }
    
    @Override
    protected void returnOutput(InputStream cacheIn, HttpServletResponse resp) throws IOException {
        // should never be called as handleAction is overridden bypassing all caching
        throw new RuntimeException("returnOutput called in SendEmailAction");
    }

    @Override
    protected boolean doOutput(HttpServletRequest req, HttpMethod method, String action, OutputProvider out, User user) throws ServletException, IOException {
        // should never be called as handleAction is overridden bypassing all caching
        throw new RuntimeException("doOutput called in SendEmailAction");
    }
    
}
