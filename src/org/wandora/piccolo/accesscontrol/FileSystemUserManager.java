package org.wandora.piccolo.accesscontrol;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.User;
import org.wandora.piccolo.UserManager;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.XMLParamProcessor;

/**
 *
 * @author olli
 */


public class FileSystemUserManager implements UserManager, XMLParamAware {

    public static String PASSWORD_KEY="password";
    
    private String userFile;
    private HashMap<String,FSUser> users;
    private String requestParam="user";
    private String passRequestParam="password";
    
    private Logger logger=null;
    
    public FileSystemUserManager(){
        users=new HashMap<String,FSUser>();
    }
    
    private static Pattern propPattern=Pattern.compile("^([^\\\\;]|\\\\\\\\|\\\\;|\\\\=)(?:;(.*)$|$)");
    private static Pattern propPattern2=Pattern.compile("^([^=]|\\\\=)(?:=(.*))?$");
    private void readUserFile() throws IOException {
        users.clear();
        
        File f=new File(userFile);
        FileInputStream fis=new FileInputStream(f);
        InputStreamReader reader=new InputStreamReader(fis);
        BufferedReader in=new BufferedReader(reader);
        
        int lineNum=0;
        String line=null;
        while((line=in.readLine())!=null){
            lineNum++;
            Matcher m=propPattern.matcher(line);
            if(!m.matches()) {
                log("WRN","Error parsing user file "+userFile+" on line "+lineNum);
                continue;
            }
            String userName=m.group(1).replaceAll("\\\\([\\\\;])","$1");
            FSUser user=new FSUser(userName);
            line=m.group(2);
            while(line!=null && line.length()>0){
                m=propPattern.matcher(line);
                if(!m.matches()){
                    log("WRN","Error parsing user file "+userFile+" on line "+lineNum);
                    break;
                }
                String prop=m.group(1);
                line=m.group(2);
                
                m=propPattern2.matcher(prop);
                if(!m.matches()){
                    log("WRN","Error parsing user file "+userFile+" on line "+lineNum);
                    break;                    
                }
                prop=m.group(1).replaceAll("\\\\([\\\\;=])","$1");
                String value=m.group(2).replaceAll("\\\\([\\\\;])","$1");
                user.setProperty(prop, value);
            }
            user.resetChanged();
            users.put(user.userName,user);
        }
    }
    
    private synchronized void writeUserFile() throws IOException {
        File f=new File(userFile);
        PrintWriter out=new PrintWriter(f);
        
        ArrayList<String> names=new ArrayList<String>(users.keySet());
        Collections.sort(names);
        for(String name : names){
            FSUser user=users.get(name);
            
            HashMap<String,Object> props=user.getAllProperties();
            out.print(name);
            for(Map.Entry<String,Object> e : props.entrySet()){
                out.print(";");
                
                String key=e.getKey();
                Object valueO=e.getValue();
                String value=(valueO==null?"":valueO.toString());
                
                key=key.replace("\\", "\\\\").replace(";","\\;").replace("\n"," ");
                value=value.replace("\\", "\\\\").replace(";","\\;").replace("\n"," ");
                
                out.print(key+"="+value);
            }
            out.println();
        }
        out.close();
    }
    
    private void log(String level,String message){
        if(logger!=null) logger.writelog(level, message);
        else System.out.println(level+" "+message);
    }
    
    @Override
    public User getUser(ServletRequest request) {
        String user=request.getParameter(requestParam);
        User u=users.get(user);
        if(u!=null){
            String suppliedPW=request.getParameter(passRequestParam);
            String password=u.getStringProperty(PASSWORD_KEY);
            if(password!=null && password.length()>0) {
                if(!password.equals(suppliedPW)) return null;
            }
        }
        return u;
    }

    @Override
    public void updateUser(User _user) {
        FSUser user=(FSUser)_user;
        if(user.hasChanged()){
            try{
                writeUserFile();
            }catch(IOException ioe){
                log("ERR",ioe.getClass().getName()+" writing user file");
            }
            
            user.resetChanged();
        }
    }

    @Override
    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        this.logger=(Logger)processor.getObject("logger");
        
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                try{
                    String name=e.getNodeName();
                    if(name.equals("file")){
                        this.userFile=processor.createObject(e).toString();
                    }
                    else if(name.equals("requestparam")) {
                        this.requestParam=processor.createObject(e).toString();
                    }
                    else if(name.equals("passrequestparam")) {
                        this.passRequestParam=processor.createObject(e).toString();
                    }
                }catch(Exception ex){
                    if(processor.getObject("logger")!=null)
                        ((Logger)processor.getObject("logger")).writelog("WRN",ex.getClass().getName()+" when processing config file, element "+e.getNodeName()+". "+ex.getMessage());  
                }
            }
        }
        if(this.userFile==null){
            log("WRN","User file not provided to FileSystemUserManager.");
        }
        else {
            try{
                readUserFile();
            }catch(IOException ioe){
                log("ERR",ioe.getClass().getName()+" reading user file");
            }
        }
    }

    
    private class FSUser extends User {
        private final HashMap<String,Object> properties;
        private boolean changed=false;
        private String userName;
        
        public FSUser(String userName){
            this.userName=userName;
            this.properties=new HashMap<String,Object>();
        }

        @Override
        public Object getProperty(String key) {
            synchronized(properties){
                return properties.get(key);
            }
        }

        @Override
        public void setProperty(String key, Object value) {
            synchronized(properties){
                properties.put(key,value);
                this.changed=true;
            }
        }
        
        public HashMap<String,Object> getAllProperties(){
            synchronized(properties){
                HashMap<String,Object> ret=new HashMap<String,Object>();
                ret.putAll(properties);
                return ret;
            }
        }
        
        public boolean hasChanged(){
            return this.changed;
        }
        
        public void resetChanged(){
            this.changed=false;
        }
    }
}
