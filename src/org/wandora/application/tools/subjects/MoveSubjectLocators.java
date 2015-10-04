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
 * MoveSubjectLocators.java
 *
 * Created on 27. heinäkuuta 2006, 14:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.application.tools.subjects;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.wandora.utils.fileserver.*;



/**
 *
 * @author olli
 */
public class MoveSubjectLocators extends AbstractWandoraTool {

    private String host;
    private int port;
    private String user;
    private String password;
    boolean useSSL;
    private String filePrefix;
    
    public MoveSubjectLocators(Context preferredContext) {
        this();
        setContext(preferredContext);
    }
    
    /** Creates a new instance of UploadFile */
    public MoveSubjectLocators() {
        host="127.0.0.1";
        port=8898;
        user=null;
        password=null;
        useSSL=false;
        filePrefix="";
    }

    @Override
    public String getName() {
        return "Move subject locators";
    }

    @Override
    public String getDescription() {
        return "Copies subject locator files to a remote file server and changes\n"+
               "subject locators to point to the URLs returned by the file server.";
    }

    @Override
    public void initialize(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        host=options.get(prefix+"host");
        String p=options.get(prefix+"port");
        port=Integer.parseInt(p);
        user=options.get(prefix+"user");
        if(user!=null && user.length()==0) user=null;
        password=options.get(prefix+"password");
        if(password!=null && password.length()==0) password=null;
        String s=options.get(prefix+"usessl");
        if(s!=null && s.equalsIgnoreCase("true")) useSSL=true;
        else useSSL=false;
        filePrefix=options.get(prefix+"prefix");
        if(filePrefix==null) filePrefix="";
    }

    @Override
    public boolean isConfigurable(){
        return true;
    }

    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"File server options","File server connection options",true,new String[][]{
            new String[]{"Host","string",host},
            new String[]{"Port","string",""+port},
            new String[]{"Use SSL","boolean",""+useSSL},
            new String[]{"User","string",user},
            new String[]{"Password","password",password},
            new String[]{"File prefix","string",filePrefix},
        });
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        host=values.get("Host");
        port=Integer.parseInt(values.get("Port"));
        useSSL=values.get("Use SSL").equalsIgnoreCase("true");
        user=values.get("User");
        password=values.get("Password");
        if(user!=null && user.length()==0) user=null;
        if(password!=null && password.length()==0) password=null;
        filePrefix=values.get("File prefix");
        if(filePrefix==null) filePrefix="";
        writeOptions(admin,options,prefix);
    }

    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
        options.put(prefix+"host",host);
        options.put(prefix+"port",""+port);
        options.put(prefix+"user",user==null?"":user);
        options.put(prefix+"password",password==null?"":password);
        options.put(prefix+"usessl",useSSL?"true":"false");
        options.put(prefix+"prefix",filePrefix);
    }    
    
    public static String cleanFileName(String name){
        StringBuffer sb=new StringBuffer(name);
        for(int i=0;i<sb.length();i++){
            char c=sb.charAt(i);
            if("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.~".indexOf(c)==-1){
                sb.setCharAt(i,'_');
            }
        }
        return sb.toString();
    }
    
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        setDefaultLogger();        
        Iterator iter=context.getContextObjects();
        java.net.Socket s=null;
        try{
            log("Connectiong to file server");
            SimpleFileServerClient fs=new SimpleFileServerClient();
            try{
                s=fs.connect(host,port,useSSL);
            }catch(IOException ioe){
                log("Couldn't connect to fileserver: "+ioe.getMessage());
                setState(WAIT);
                return;
            }
            InputStream in=s.getInputStream();
            OutputStream out=s.getOutputStream();
            Writer writer=new OutputStreamWriter(out);
            if(!fs.login(in,writer,user,password)){
                log("Couldn't login to file server");
                log("Server response: "+fs.getLastServerResponse());
                setState(WAIT);
                return;
            }
            else log("Logged in to file server");
            //WandoraHttpAuthorizer authorizer=new WandoraHttpAuthorizer(admin);
            WandoraHttpAuthorizer authorizer=admin.wandoraHttpAuthorizer;
            
            while(iter.hasNext()){
                Object o=iter.next();
                if(!(o instanceof Topic)) continue;
                Topic t=(Topic)o;
                if(t.getSubjectLocator()==null) continue;
                if(t.getBaseName()==null) continue;
                URL url=null;
                URLConnection c=null;
                InputStream urlIn=null;
                try{
                    url=new URL(t.getSubjectLocator().toExternalForm());
                    c=authorizer.getAuthorizedAccess(url);
                    urlIn=c.getInputStream();
                
                    String filename=filePrefix+cleanFileName(t.getBaseName());

                    String suffix=null;
                    String mime=c.getContentType();
                    if(mime!=null) suffix=org.wandora.utils.FileTypes.getSuffixForContentType(mime);
                    if(suffix==null){
                        String l=t.getSubjectLocator().toExternalForm();
                        int ind=l.lastIndexOf(".");
                        if(ind!=-1 && l.length()-ind>1) suffix=l.substring(ind+1);
                    }
                    if(suffix!=null) filename+="."+suffix;

                    log("Sending file "+t.getSubjectLocator());
                    if(!fs.sendFile(in,writer,out,filename,urlIn)){
                        log("Could not send file "+t.getSubjectLocator());
                        log("Server response: "+fs.getLastServerResponse());
                    }
                    else{
                        String serverUrl=fs.getURLFor(in,writer,filename);
                        if(serverUrl==null) 
                            log("File sent, could not get url for file "+t.getSubjectLocator());
                        else if(serverUrl.length()==0)
                            log("File sent, server did not return url for file "+t.getSubjectLocator());
                        else {
                            t.setSubjectLocator(t.getTopicMap().createLocator(serverUrl));
                            log("File sent and subjectlocator changed for file "+t.getSubjectLocator());
                        }
                    }
                }catch(IOException ioe){
                    log(ioe);
                    continue;
                }
                                
            }
            log("Logging out from file server");
            fs.logout(writer);
        }
        catch(Exception e){
            log(e);
        }
        finally{
            if(s!=null) try{s.close();}catch(IOException ioe){}
        }
        log("Ready.");
        setState(WAIT);
    }
    
}
