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
 * UploadFile.java
 *
 * Created on 26. heinäkuuta 2006, 12:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.application.tools;

import /*
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
org.wandora.utils.fileserver.SimpleFileServerClient;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import java.io.*;
import java.util.*; 
import java.sql.*;
import javax.swing.*;
import org.wandora.utils.swing.GuiTools;
import static org.wandora.utils.Tuples.*;
import org.wandora.utils.fileserver.*;
/**
 *
 * @author olli
 */

public class UploadFile extends AbstractWandoraTool {

    private String host;
    private int port;
    private String user;
    private String password;
    boolean useSSL;
    
    /** Creates a new instance of UploadFile */
    public UploadFile() {
        host="127.0.0.1";
        port=8898;
        user=null;
        password=null;
        useSSL=false;
    }

    @Override
    public String getName() {
        return "Upload file";
    }

    @Override
    public String getDescription() {
        return "Uploads a file to a file server.";
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
    }

    @Override
    public boolean isConfigurable(){
        return true;
    }

    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Upload file options","File server connection options",true,new String[][]{
            new String[]{"Host","string",host},
            new String[]{"Port","string",""+port},
            new String[]{"Use SSL","boolean",""+useSSL},
            new String[]{"User","string",user},
            new String[]{"Password","password",password},
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
        writeOptions(admin,options,prefix);
    }

    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
        options.put(prefix+"host",host);
        options.put(prefix+"port",""+port);
        options.put(prefix+"user",user==null?"":user);
        options.put(prefix+"password",password==null?"":password);
        options.put(prefix+"usessl",useSSL?"true":"false");
    }    
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        setDefaultLogger();        
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select file to send");
        if(chooser.open(admin, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            File f=chooser.getSelectedFile();
            if(f==null) return;
            String filename=WandoraOptionPane.showInputDialog(admin,"Enter remote path and filename",f.getName());
            if(filename==null) return;
            
            java.net.Socket s=null;
            try{
                SimpleFileServerClient fs=new SimpleFileServerClient();
                s=fs.connect(host,port,useSSL);
                InputStream in=s.getInputStream();
                OutputStream out=s.getOutputStream();
                Writer writer=new OutputStreamWriter(out);
                if(!fs.login(in,writer,user,password)){
                    log("Couldn't login to file server");
                    log("Server response: "+fs.getLastServerResponse());
                    setState(WAIT);
                    return;
                }
                if(!fs.sendFile(in,writer,out,filename,chooser.getSelectedFile())){
                    log("Could not send file");
                    log("Server response: "+fs.getLastServerResponse());
                    setState(WAIT);
                    return;
                }
                String url=fs.getURLFor(in,writer,filename);
                if(url==null) log("File sent, could not get url for file.");
                else if(url.length()==0) log("File sent, server did not return url for file.");
                else log("File sent, url for file is "+url);
                fs.logout(writer);
            }
            catch(IOException ioe){
                log(ioe);
            }
            finally{
                if(s!=null) try{s.close();}catch(IOException ioe){}
            }
            setState(WAIT);
        }
        
    }    
    
}
