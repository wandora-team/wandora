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
 * AdminSocketServer.java
 *
 * Created on June 8, 2004, 11:54 AM
 */

package org.wandora.topicmap.remote.server;
import org.wandora.*;
import org.wandora.piccolo.*;
import org.wandora.topicmap.remote.server.VirtualFileSystem;
import org.wandora.topicmap.TMBox;
import org.wandora.application.*;
import org.wandora.tools.*;
import org.wandora.exceptions.*;
import org.wandora.topicmap.*;
import java.util.*;
import java.io.*;
import java.net.*;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;


/**
 *
 * @author  olli
 */
public class AdminSocketServer extends Thread implements PiccoloShutdownHook {
    
    public static final int DEFAULT_PORT=8989;
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private WandoraManager manager;
    
    private Collection<SocketThread> socketThreads;
    
    private Logger logger;
    
//    private String fileStore;
//    private String fileStoreExternal;
    private VirtualFileSystem vfs;
    private CustomCommand[] customCommands;
    
    /** Creates a new instance of AdminSocketServer */
    public AdminSocketServer(WandoraManager manager) {
        this(manager,DEFAULT_PORT,new SimpleLogger(),null);
    }
    public AdminSocketServer(WandoraManager manager,Logger logger) {
        this(manager,DEFAULT_PORT,logger,null);
    }
    public AdminSocketServer(WandoraManager manager,int serverPort) {
        this(manager,serverPort,new SimpleLogger(),null);
    }
    public AdminSocketServer(WandoraManager manager,int serverPort,Logger logger) {
        this(manager,serverPort,logger,null);
    }
    public AdminSocketServer(WandoraManager manager,int serverPort,Logger logger,VirtualFileSystem vfs) {
        this(manager,serverPort,logger,vfs,null);
    }
    public AdminSocketServer(WandoraManager manager,int serverPort,Logger logger,VirtualFileSystem vfs,CustomCommand[] customCommands) {
        this.manager=manager;
        this.logger=logger;
//        this.fileStore=fileStorePath;
//        this.fileStoreExternal=fileStoreExternalURL;
        this.vfs=vfs;
        if(customCommands==null) this.customCommands=new CustomCommand[0];
        else this.customCommands=customCommands;
//        if(!fileStore.endsWith(File.separator)) fileStore+=File.separator;
        port=serverPort;
        customCommands=null;
        socketThreads=new Vector<>();
        try{
            serverSocket=new ServerSocket(port);
            running=true;
        }catch(Exception e){
            logger.writelog("WRN","Exception initializing admin socket server",e);
            running=false;
        }
    }
    
    public void doShutdown(){
        running=false;
        try{
            serverSocket.close();
        }catch(IOException ioe){}
        Iterator<SocketThread> iter=new Vector(socketThreads).iterator();
        while(iter.hasNext()){
            SocketThread st=(SocketThread)iter.next();
            st.stopThread();
        }
    }
    
    public void run() {
        while(running){
            try{
                Socket s=serverSocket.accept();
                SocketThread st=new SocketThread(manager,s);
                socketThreads.add(st);
                st.start();
            }catch(Exception e){
                if(running) logger.writelog("WRN","Exception in socket server listener.",e);
            }
        }
    }

    public int getConnectionCount(){
        return socketThreads.size();
    }
    
    class SocketThread extends Thread {
        private InputStream originalIn;
        private InputStream in;
        private OutputStream out;
        private Socket socket;
        
        private boolean running;
        
        private WandoraManager manager;
        
        private boolean gzip;
        
        private boolean loggedin;
        
        private SecretKey key;
        
        private TopicMap usedTopicMap;

        private ArrayList actions;
    
        public SocketThread(WandoraManager manager,Socket s) throws IOException {
            this.manager=manager;
            socket=s;
            out=s.getOutputStream();
            originalIn=s.getInputStream();
//            in=new PushbackInputStream(originalIn,32);
            in=originalIn;
            running=true;
            gzip=false;
            loggedin=false;
            actions=new ArrayList();
        }
        
        public String processLine(String line){
            char[] buf=new char[line.length()];
            int offs=0;
            char c;
            for(int i=0;i<line.length();i++){
                c=line.charAt(i);
                if(c!='\b') {
                    buf[offs]=c;
                    offs++;
                }
                else if(offs>0){
                    offs--;
                }
            }
            return new String(buf,0,offs);
        }
        
        private String readLine() throws IOException{
            return SocketServerInterface.readLine(in);
        }

        
        private Object[] parseParams(String line,int startind){
            Stack stack=new Stack();
            ArrayList al=new ArrayList();
            int ptr=startind;
            while(ptr<line.length()){
                while(ptr<line.length() && line.charAt(ptr)==' ') ptr++;
                if(line.charAt(ptr)=='('){
                    stack.push(al);
                    al=new ArrayList();
                    ptr++;
                }
                else if(line.charAt(ptr)==')'){
                    if(stack.isEmpty()) break;
                    ArrayList p=al;
                    al=(ArrayList)stack.pop();
                    al.add(p.toArray());
                    ptr++;
                }
                else if(line.charAt(ptr)==','){
                    ptr++;
                }
                else{
                    if(line.charAt(ptr)=='"') {
                        int ptr2=ptr;
                        String p=null;
                        while(ptr2<line.length()){
                            int ind=line.indexOf('"',ptr2+1);
                            if(ind!=-1){
                                int count=0;
                                while(ind-count-1>ptr && line.charAt(ind-count-1)=='\\') count++;
                                if(count%2==0) {
                                    p=line.substring(ptr+1,ind);
                                    ptr=ind+1;
                                    break;
                                }
                                else ptr2=ind+1;
                            }
                            else{
                                al.add(line);
                                logger.writelog("WRN","Parse error parsing params.");
                                return al.toArray();
                            }
                        }
                        if(p==null){
                            al.add(line);
                            logger.writelog("WRN","Parse error parsing params.");
                            return al.toArray();
                        }
                        al.add(SocketServerInterface.decode(p));
                    }
                    else {
                        int ind=line.indexOf(',',ptr);
                        int ind2=line.indexOf(')',ptr);
                        if(ind2!=-1 && (ind2<ind || ind==-1)) ind=ind2;
                        String p;
                        if(ind!=-1){
                            p=line.substring(ptr,ind);
                            ptr=ind;
                        }
                        else { 
                            p=line.substring(ptr);
                            ptr=line.length();
                        }
                        al.add(SocketServerInterface.decode(p));
                    }
                }
            }
            
            
            Object[] parsed=al.toArray();
/*            System.out.print("Parsed params ");
            StringBuffer buf=new StringBuffer();
            getParsed(parsed,buf);
            System.out.println(buf);*/
            return parsed;
        }        
        
        private void getParsed(Object[] params,StringBuffer buf){
            for(int i=0;i<params.length;i++){
                if(i>0) buf.append(",");
                if(params[i] instanceof Object[]){
                    buf.append("(");
                    getParsed((Object[])params[i],buf);
                    buf.append(")");
                }
                else{
                    buf.append("\""+params[i]+"\"");
                }
            }
        }
                
        private byte[] getCopiedXTM(Collection topics) throws IOException, TopicMapException {
            return getCopiedXTM(topics,true);
        }
        private byte[] getCopiedXTM(Collection topics,boolean associations) throws IOException, TopicMapException {
            TopicMap tm=manager.createTopicMap();
            
            Iterator iter=topics.iterator();
            while(iter.hasNext()) {
                Object o=iter.next();
                Topic t=(Topic)o;
                tm.copyTopicIn(t, false);
                if(associations) tm.copyTopicAssociationsIn(t);
            }
            
/*          //Debugging
            int count=0;
            int count2=0;
            iter=tm.getTopics();
            while(iter.hasNext()){count++;iter.next();}
            iter=tm.getTopics();
            while(iter.hasNext()){count2++;iter.next();}
            System.out.println("Copying "+topics.size()+" topics. Copied map has "+count+" topics, consistent "+(count==count2));
*/            
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            if(gzip) {
                java.util.zip.GZIPOutputStream gzip=new java.util.zip.GZIPOutputStream(baos);
                tm.exportXTM(gzip);
                gzip.finish();
                gzip.flush();
            }
            else tm.exportXTM(baos);
            
            return baos.toByteArray();
        }

        private TopicMap readTopicMap(long length){
            try{
                TopicMap tm=manager.createTopicMap();
                if(gzip)
                    tm.importXTM(new java.util.zip.GZIPInputStream(new LimitedInputStream(in,length)));
                else
                    tm.importXTM(new LimitedInputStream(in,length));                    
                return tm;
            }catch(Exception e){
                logger.writelog("WRN","Exception reading topicmap ",e);
                return null;
            }
        }
        
        public void stopThread(){
            try{
                socket.close();
            }catch(IOException ioe){};
            running=false;
        }
        
        public void run() {
            String line;
            String cmd;
            Object[] params;
            int ind;
            try{
                while(running){
                    line=readLine();
                    if(line!=null) line=processLine(line);
                    if(line==null) {
                        running=false;
                        break;
                    }
                    if(line.startsWith("login("))
                        logger.writelog("DBG","got command \"login(***,***)\"");
                    else
                        logger.writelog("DBG","got command \""+line+"\"");
                    ind=line.indexOf('(');
                    if(ind!=-1){
                        cmd=line.substring(0,ind).trim();
                        params=parseParams(line.substring(ind+1),0);
                        if(params==null) logger.writelog("DBG","params is null");
                    }
                    else{
                        cmd=line.trim();
                        params=new Object[0];
                    }

                    if(cmd.equalsIgnoreCase("cipher")){
                        try{
/*                          
                            // This take a VERY long time
                            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
                            paramGen.init(512);
                            AlgorithmParameters aparams = paramGen.generateParameters();
                            DHParameterSpec dhParamSpec= (DHParameterSpec)aparams.getParameterSpec(DHParameterSpec.class);
 */
                            DHParameterSpec dhParamSpec = new DHParameterSpec(AdminSocketServer.DHModulus,AdminSocketServer.DHBase);

                            KeyPairGenerator kpairGen = KeyPairGenerator.getInstance("DH");
                            kpairGen.initialize(dhParamSpec);
                            KeyPair kpair = kpairGen.generateKeyPair();
                            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                            keyAgree.init(kpair.getPrivate());
                            byte[] pubkey = kpair.getPublic().getEncoded();
                            logger.writelog("DBG","Sending public key");
                            out.write( ("PUBLIC "+pubkey.length+"\n").getBytes("UTF-8") );
                            out.write(pubkey);
                            out.flush();

                            logger.writelog("DBG","Reading public key of client");
                            line=readLine();
                            if(line!=null) line=processLine(line);
                            ind=line.indexOf('(');
                            if(ind!=-1){
                                cmd=line.substring(0,ind).trim();
                                params=parseParams(line.substring(ind+1),0);
                            }
                            else{
                                cmd=line.trim();
                                params=new Object[0];
                            }
                            int publength=Integer.parseInt((String)params[0]);
                            InputStream lin=new LimitedInputStream(in,publength);
                            byte[] pub=new byte[publength];
                            int read=0;
                            while(read<publength){
                                read+=lin.read(pub,read,publength-read);
                            }
                            logger.writelog("DBG","Got public key of client");
                            
                            // generate shared secret key
                            KeyFactory keyFac = KeyFactory.getInstance("DH");
                            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pub);
                            PublicKey pubKey = keyFac.generatePublic(x509KeySpec);
                            keyAgree.doPhase(pubKey,true);
                            key=keyAgree.generateSecret("DES");
                            
                            // make ciphered out stream
                            Cipher c=Cipher.getInstance("DES/CFB8/NoPadding");
                            c.init(Cipher.ENCRYPT_MODE,key);
                            AlgorithmParameters aparams=c.getParameters();
                            // send parameters to client
                            pub=aparams.getEncoded();
                            out.write( ("PARAMS "+pub.length+"\n").getBytes("UTF-8") );
                            out.write(pub);
                            out.flush();
                            out=new CipherOutputStream(out,c);
                            
                            logger.writelog("DBG","Reading parameters from client");
                            line=readLine();
                            if(line!=null) line=processLine(line);
                            ind=line.indexOf('(');
                            if(ind!=-1){
                                cmd=line.substring(0,ind).trim();
                                params=parseParams(line.substring(ind+1),0);
                            }
                            else{
                                cmd=line.trim();
                                params=new Object[0];
                            }
                            int paramlength=Integer.parseInt((String)params[0]);
                            lin=new LimitedInputStream(in,paramlength);
                            pub=new byte[paramlength];
                            read=0;
                            while(read<paramlength){
                                read+=lin.read(pub,read,paramlength-read);
                            }
                            aparams = AlgorithmParameters.getInstance("DES");
                            aparams.init(pub);
                            
                            // make ciphered in stream
                            c=Cipher.getInstance("DES/CFB8/NoPadding");
                            c.init(Cipher.DECRYPT_MODE,key,aparams);
//                            in=new PushbackInputStream(new CipherInputStream(in,c),32);
                            in=new CipherInputStream(in,c);
                            logger.writelog("DBG","Switched to ciphered streams");
                        }catch(Exception e){
                            logger.writelog("WRN","Couldn't initialize ciphering.",e);
                            out.write("ERROR\n".getBytes("UTF-8"));
                        }
                    }
                    else if(cmd.equalsIgnoreCase("login")){
                        String user="";
                        if(params.length>0) user=(String)params[0];
                        String password="";
                        if(params.length>1) password=(String)params[1];
                        if(manager.isAdminUser(user,password)){
                            loggedin=true;
                            logger.writelog("SEC",user+" logged in from "+socket.getInetAddress().toString()+":"+socket.getPort());                            
                            out.write( "OK\n".getBytes("UTF-8") );
                        }
                        else{
                            logger.writelog("SEC","Invalid password. "+socket.getInetAddress().toString()+":"+socket.getPort());                            
                            out.write( "ERROR INVALID PASSWORD\n".getBytes("UTF-8") );
                        }
                    }
                    else if(cmd.equalsIgnoreCase("exit")){
                        out.write( "OK closing connection\n".getBytes("UTF-8") );
                        running=false;
                        socket.close();
                    }
                    else if(cmd.equalsIgnoreCase("ping")){
                        out.write("PONG\n".getBytes("UTF-8") );
                    }
                    else if(loggedin){
                        if(cmd.equalsIgnoreCase("opentopicmap")){
                            if(params.length>0)
                                usedTopicMap=manager.getTopicMap((String)params[0]);
                            else
                                usedTopicMap=manager.getTopicMap();
                            if(usedTopicMap!=null) {
                                actions=new ArrayList();
                                out.write( "OK\n".getBytes("UTF-8") );
                            }
                            else out.write( "ERROR specified topicmap not found\n".getBytes("UTF-8") );
                        }
                        else if(usedTopicMap==null){
                            out.write("ERROR topic map not opened\n".getBytes("UTF-8") );
                        }else if(cmd.equalsIgnoreCase("search")){
                            String query=(String)params[0];
                            Collection c=manager.search(query);
                            out.write( ("OK "+(c.size()>50?50:c.size())+"\n").getBytes("UTF-8") );
                            Iterator iter=c.iterator();
                            int counter=0;
                            while(iter.hasNext() && counter<50){
                                SearchResultItem sri=(SearchResultItem)iter.next();
                                Topic t=sri.getTopic();
                                out.write( (sri.getScore()+"\n"+t.getOneSubjectIdentifier()+"\n"+t.getBaseName()+"\n").getBytes("UTF-8") );
                                counter++;
                            }
                            out.flush();
                        }
                        else if(cmd.equalsIgnoreCase("gettopics")){
                            if(manager.lockTopicMap(manager.LOCK_READ)){
                                try{
                                    ArrayList p=new ArrayList(params.length);
                                    ArrayList al=new ArrayList(params.length);
                                    for(int i=0;i<params.length;i++) {
                                        if(!p.contains(params[i])) {
                                            if(params[i] instanceof String) {
                                                Topic t=usedTopicMap.getTopic((String)params[i]);
                                                if(t!=null) al.add(t);
                                                p.add(params[i]);
                                            }
                                        }
                                    }
                                    byte[] buf=getCopiedXTM(al);
                                    out.write( ("OK "+(buf.length)+"\n").getBytes("UTF-8") );
                                    out.write(buf);
                                }finally{
                                    manager.releaseTopicMap(manager.LOCK_READ);
                                }
                            }
                            else{
                                out.write( "ERROR couldn't lock topicmap\n".getBytes("UTF-8"));
                            }
                        }
                        else if(cmd.equalsIgnoreCase("gettopicsoftype")){
                            if(params.length>0){
                                if(manager.lockTopicMap(manager.LOCK_READ)){
                                    try{
                                        byte[] buf=getCopiedXTM(usedTopicMap.getTopicsOfType((String)params[0]),false);
                                        out.write( ("OK "+(buf.length)+"\n").getBytes("UTF-8") );
                                        out.write(buf);                    
                                    }finally{
                                        manager.releaseTopicMap(manager.LOCK_READ);
                                    }
                                }
                                else{
                                    out.write( "ERROR couldn't lock topicmap\n".getBytes("UTF-8"));
                                }
                            }
                            else out.write( ("ERROR invalid params\n").getBytes("UTF-8") );
                        }
                        else if(cmd.equalsIgnoreCase("gettopicbyname")){
                            if(params.length>0){
                                if(manager.lockTopicMap(manager.LOCK_READ)){
                                    try{
                                        Topic t=usedTopicMap.getTopicWithBaseName((String)params[0]);
                                        Collection topics=new HashSet();
                                        if(t!=null) topics.add(t);
                                        byte[] buf=getCopiedXTM(topics);
                                        out.write( ("OK "+(buf.length)+"\n").getBytes("UTF-8") );
                                        out.write(buf);                    
                                    }finally{
                                        manager.releaseTopicMap(manager.LOCK_READ);
                                    }
                                }
                                else{
                                    out.write( "ERROR couldn't lock topicmap\n".getBytes("UTF-8"));
                                }
                            }
                            else out.write( ("ERROR invalid params\n").getBytes("UTF-8") );
                        }
                        else if(cmd.equalsIgnoreCase("mergein")){
                            if(params.length>0){
                                long length=Long.parseLong((String)params[0]);
                                TopicMap tm=readTopicMap(length);

                                // remove other merge actions
                                Iterator iter=actions.iterator();
                                while(iter.hasNext()){
                                    AdminSocketServerEditAction a=(AdminSocketServerEditAction)iter.next();
                                    if(a.getType()==AdminSocketServerEditAction.TYPE_MERGEIN){
                                        iter.remove();
                                        break;
                                    }
                                }

                                actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_MERGEIN,tm));
                                out.write( "OK\n".getBytes("UTF-8") );
                            }
                            else out.write( ("ERROR invalid params\n").getBytes("UTF-8") );
                        }
                        else if(cmd.equalsIgnoreCase("commit")){
                            if(manager.lockTopicMap(WandoraManager.LOCK_WRITE)){
                                try{
                                    Iterator iter;
                                    boolean proceed=true;
                                    if(params.length<1 || !((String)params[0]).equalsIgnoreCase("true")){
                                        iter=actions.iterator();
                                        try{
                                            while(iter.hasNext()){
                                                AdminSocketServerEditAction a=(AdminSocketServerEditAction)iter.next();
                                                if(a.getType()==AdminSocketServerEditAction.TYPE_CHECK){
                                                    logger.writelog("DBG","Checking edit status");
                                                    a.runAction(usedTopicMap);
                                                    logger.writelog("DBG","Edit status checked");
                                                }
                                            }
                                        }catch(WandoraException we){
                                            if(we instanceof ConcurrentEditingException){
                                                Set failed=((ConcurrentEditingException)we).getFailedTopics();
                                                Set removed=((ConcurrentEditingException)we).getRemovedTopics();
                                                StringBuffer buf=new StringBuffer();
                                                buf.append("CONCURRENTEDITING "+failed.size()+" "+removed.size()+"\n");
                                                iter=failed.iterator();
                                                while(iter.hasNext()){
                                                    buf.append(""+iter.next()+"\n");
                                                }
                                                iter=removed.iterator();
                                                while(iter.hasNext()){
                                                    buf.append(""+iter.next()+"\n");
                                                }
                                                out.write( buf.toString().getBytes("UTF-8") );
                                            }
                                            else{
                                                logger.writelog("WRN","Exception commiting changeds",we);
                                                out.write( ("ERROR\n").getBytes("UTF-8") );                                        
                                            }
                                            proceed=false;
                                        }
                                    }
                                    if(proceed){
                                        try{
                                            logger.writelog("DBG","Checking runnability");
                                            iter=actions.iterator();
                                            while(iter.hasNext()){
                                                AdminSocketServerEditAction a=(AdminSocketServerEditAction)iter.next();
                                                if(a.getType()!=AdminSocketServerEditAction.TYPE_CHECK && !a.isResolved())
                                                    a.resolveParams(usedTopicMap);
                                            }
                                            iter=actions.iterator();
                                            while(iter.hasNext()){
                                                AdminSocketServerEditAction a=(AdminSocketServerEditAction)iter.next();
                                                if(a.getType()!=AdminSocketServerEditAction.TYPE_CHECK){
                                                    try{
                                                        if(!a.checkRunnability(usedTopicMap)){
                                                            proceed=false;
                                                        }
                                                    }catch(TopicInUseException e){
                                                        // remove troublesome action.
                                                        iter.remove();
                                                        throw e;
                                                    }
                                                }
                                            }
                                            if(proceed){
                                                logger.writelog("DBG","Running all but merge");
                                                AdminSocketServerEditAction mergeAction=null; // run merge last
                                                iter=actions.iterator();
                                                while(iter.hasNext()){
                                                    AdminSocketServerEditAction a=(AdminSocketServerEditAction)iter.next();
                                                    if(a.getType()!=AdminSocketServerEditAction.TYPE_CHECK){
                                                        if(a.getType()==AdminSocketServerEditAction.TYPE_MERGEIN)
                                                            mergeAction=a;
                                                        else{
                                                            logger.writelog("DBG","Running action "+a.toString());
                                                            a.runAction(usedTopicMap);
                                                        }
                                                    }
                                                }
                                                if(mergeAction!=null) {
                                                    logger.writelog("Running merge");
                                                    mergeAction.runAction(usedTopicMap);
                                                }
                                                actions=new ArrayList();
                                                logger.writelog("DBG","Commit finished");
                                                out.write( ("OK\n").getBytes("UTF-8") );
                                            }
                                            else{
                                                logger.writelog("DBG","Runnability failed");
                                                out.write( ("ERROR\n").getBytes("UTF-8") );                                        
                                            }
                                        }catch(WandoraException we){
                                            if(we instanceof TopicInUseException){
                                                Topic t=((TopicInUseException)we).getTopic();
                                                out.write( ("TOPICINUSE "+t.getSubjectIdentifiers().iterator().next()+"\n").getBytes("UTF-8") );
                                            }
                                            else{
                                                logger.writelog("WRN","Exception commiting changes.",we);
                                                out.write( ("ERROR\n").getBytes("UTF-8") );
                                            }
                                            logger.writelog("DBG","Exception commiting changes.",we);
                                        }catch(Exception e){
                                            out.write( ("ERROR "+e.getMessage()+"\n").getBytes("UTF-8") );                                        
                                            logger.writelog("WRN","Exception commiting changes.",e);
                                        }
                                    }
                                }finally{
                                    manager.releaseTopicMap(WandoraManager.LOCK_WRITE);
                                }
                                try{
                                    manager.updateSearchIndex();
                                }catch(WandoraException e){
                                    // TODO: use logger
                                }
                            }
                            else{
                                out.write( ("ERROR couldn't lock topicmap\n").getBytes("UTF-8") );
                            }
                        }
                        else if(cmd.equalsIgnoreCase("rollback")){
                            actions=new ArrayList();
                            out.write( "OK\n".getBytes("UTF-8") );                    
                        }
                        else if(cmd.equalsIgnoreCase("checkedited")){
                            if(params.length>0){
                                long length=Long.parseLong((String)params[0]);
                                TopicMap tm=readTopicMap(length);

                                // remove other check actions
                                Iterator iter=actions.iterator();
                                while(iter.hasNext()){
                                    AdminSocketServerEditAction a=(AdminSocketServerEditAction)iter.next();
                                    if(a.getType()==AdminSocketServerEditAction.TYPE_CHECK){
                                        iter.remove();
                                        break;
                                    }
                                }

                                actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_CHECK,tm));
                                out.write( "OK\n".getBytes("UTF-8") );
                            }
                            else out.write( ("ERROR invalid params\n").getBytes("UTF-8") );
                        }
                        else if(cmd.equalsIgnoreCase("removeassociation")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVEASSOC,params));
                            out.write( "OK\n".getBytes("UTF-8") );                    
                        }
                        else if(cmd.equalsIgnoreCase("removetopic")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVETOPIC,params));                    
                            out.write( "OK\n".getBytes("UTF-8") );                    
                        }
/* // UNTESTED                        
                        else if(cmd.equalsIgnoreCase("removetopicallowed")){
                            Topic t=usedTopicMap.getTopic((String)params[0]);
                            if(t==null || t.isDeleteAllowed()) out.write( "OK\n".getBytes("UTF-8") );
                            else out.write("INUSE\n".getBytes("UTF-8") );
                        }*/
                        else if(cmd.equalsIgnoreCase("removetopicvariantname")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVEVARIANTNAME,params));                    
                            out.write( "OK\n".getBytes("UTF-8") );                                        
                        }
                        else if(cmd.equalsIgnoreCase("removetopicbasename")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVEBASENAME,params));                    
                            out.write( "OK\n".getBytes("UTF-8") );                                        
                        }
                        else if(cmd.equalsIgnoreCase("removesubjectlocator")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVESUBJECTLOCATOR,params));
                            out.write( "OK\n".getBytes("UTF-8") );                                        
                        }
                        else if(cmd.equalsIgnoreCase("removedata")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVEDATA,params));                    
                            out.write( "OK\n".getBytes("UTF-8") );                                        
                        }
                        else if(cmd.equalsIgnoreCase("removetopictype")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVETOPICTYPE,params));                    
                            out.write( "OK\n".getBytes("UTF-8") );                                        
                        }
                        else if(cmd.equalsIgnoreCase("removesubjectidentifier")){
                            actions.add(new AdminSocketServerEditAction(AdminSocketServerEditAction.TYPE_REMOVESI,params));
                            out.write( "OK\n".getBytes("UTF-8") );
                        }
                        else if(cmd.equalsIgnoreCase("gettopicmap")){
                            if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
                                try{
                                    TopicMap tm=usedTopicMap;
                                    ByteArrayOutputStream baos=new ByteArrayOutputStream(256000);
                                    if(gzip){
                                        java.util.zip.GZIPOutputStream gzip=new java.util.zip.GZIPOutputStream(baos);
                                        tm.exportXTM(gzip);
                                        gzip.finish();
                                    }
                                    else tm.exportXTM(baos);
                                    byte[] buf=baos.toByteArray();

                                    out.write( ("OK "+(buf.length)+"\n").getBytes("UTF-8") );
                                    out.write(buf);                    

                                }finally{
                                    manager.releaseTopicMap(WandoraManager.LOCK_READ);
                                }
                            }                        
                            else{
                                out.write( "ERROR couldn't lock topicmap\n".getBytes("UTF-8") );
                            }
                        }
                        else if(cmd.equalsIgnoreCase("listfiles")){
                            String dir=(String)params[0];
                            String[] files=vfs.listFiles(dir);
                            if(files==null){
                                out.write("ERROR INVALID FILE NAME\n".getBytes("UTF-8"));
                            }
                            else{
                                out.write(("OK "+files.length+"\n").getBytes("UTF-8"));
                                for(int i=0;i<files.length;i++){
                                    out.write((files[i]+"\n").getBytes("UTF-8"));
                                }
                                out.flush();
                            }
                        }
                        else if(cmd.equalsIgnoreCase("listdirectories")){
                            String dir=(String)params[0];
                            String[] files=vfs.listDirectories(dir);
                            if(files==null){
                                out.write("ERROR INVALID FILE NAME\n".getBytes("UTF-8"));
                            }
                            else{
                                out.write(("OK "+files.length+"\n").getBytes("UTF-8"));
                                for(int i=0;i<files.length;i++){
                                    out.write((files[i]+"\n").getBytes("UTF-8"));
                                }
                                out.flush();
                            }
                        }
                        else if(cmd.equalsIgnoreCase("fileexists")){
                            String file=(String)params[0];
                            File f=vfs.getRealFileFor(file);
                            if(f==null){
                                out.write("ERROR INVALID DIRECTORY\n".getBytes("UTF-8"));
                            }
                            else{
                                out.write( ("OK "+f.exists()+"\n").getBytes("UTF-8"));
                            }
                        }
                        else if(cmd.equalsIgnoreCase("upload")){
                            long length=Long.parseLong((String)params[0]);
                            String filename=(String)params[1];
                            OutputStream out=null;
                            try{
                                InputStream in=new LimitedInputStream(this.in,length);
                                File f=vfs.getRealFileFor(filename);
                                if(f==null || (f.exists() && f.isDirectory()) ){
                                    this.out.write( ("ERROR INVALID FILE NAME\n".getBytes("UTF-8")) );
                                }
                                else{
                                    if(f.exists() && (params.length<3 || !((String)params[2]).equalsIgnoreCase("true")) ){
                                        this.out.write( "ERROR FILE ALLREADY EXISTS\n".getBytes("UTF-8") );
                                    }
                                    else{
                                        out=new FileOutputStream(f);
                                        int read=0;
                                        byte[] buf=new byte[4096];
                                        while( (read=in.read(buf))!=-1 ){
                                            out.write(buf,0,read);
                                        }
                                        out.close();
                                        String url=vfs.getURLFor(filename);
                                        this.out.write( ("OK "+url+"\n").getBytes("UTF-8") );
                                    }
                                }
                            }catch(IOException ioe){
                                logger.writelog("DBG","Unable to save file",ioe);
                                if(out!=null) try{
                                    out.close();
                                }catch(IOException ioe2){}
                                this.out.write( ("ERROR\n").getBytes("UTF-8") );
                            }
                        }
                        else if(cmd.equalsIgnoreCase("deletefile")){
                            String filename=(String)params[0];
                            if(filename!=null && filename.trim().length()>0){
                                filename=filename.trim();
                                File file=vfs.getRealFileFor(filename);
                                if(file==null){
                                    out.write( "ERROR INVALID FILE NAME\n".getBytes("UTF-8"));
                                }
                                else{
                                    if(file.delete()){
                                        out.write( ("OK\n").getBytes("UTF-8") );
                                    }
                                    else{
                                        out.write( ("ERROR COULD NOT DELETE FILE\n").getBytes("UTF-8") );
                                    }
                                }
                            }
                            else{
                                out.write( ("ERROR\n").getBytes("UTF-8") );
                            }
                        }
                        else if(cmd.equalsIgnoreCase("gzip")){
                            gzip=true;
                            out.write( ("OK\n").getBytes("UTF-8") );
                        }
                        else if(cmd.equalsIgnoreCase("cleartopicmap")){
                            if(manager.lockTopicMap(WandoraManager.LOCK_WRITE)){
                                try{
                                    TMBox.clearTopicMap(usedTopicMap);
                                    out.write( ("OK\n").getBytes("UTF-8") );
                                }finally{
                                    manager.releaseTopicMap(WandoraManager.LOCK_WRITE);
                                }
                            }
                            else{
                                out.write( "ERROR couldn't lock topicmap\n".getBytes("UTF-8") );
                            }
                        }else if(cmd.equalsIgnoreCase("updateindex")){
                            try{
                                manager.rebuildSearchIndex();
                                out.write( "OK\n".getBytes("UTF-8") ); 
                            }catch(WandoraException we){
                                out.write( ("ERROR WandoraException "+we.getMessage()+"\n").getBytes("UTF-8"));
                            }
                        }else if(cmd.equalsIgnoreCase("writelog")){
                            String lvl="INF";
                            String msg="";
                            if(params.length==1) msg=(String)params[0];
                            else {
                                msg=(String)params[1];
                                lvl=(String)params[0];
                            }
                            logger.writelog(lvl,"Remote log entry: "+msg);
                            out.write( "OK\n".getBytes("UTF-8") ); 
                        }
                        else{
                            boolean matched=false;
                            for(int i=0;i<customCommands.length;i++){
                                if(customCommands[i].match(cmd)){
                                    customCommands[i].execute(cmd,in,out);
                                    matched=true;
                                    break;
                                }
                            }
                            if(!matched){
                                out.write( "ERROR INVALID COMMAND\n".getBytes("UTF-8") );
                                logger.writelog("WRN","Invalid command.");
                            }
                        }
                    }
                    else{
                        logger.writelog("SEC","User not logged in. "+socket.getInetAddress().toString()+":"+socket.getPort());
                        out.write( ("ERROR NOT LOGGED IN\n").getBytes("UTF-8") );                        
                    }
                }
            }catch(IOException ioe){
                if(running) logger.writelog("WRN","IOException in admin socket server",ioe);
            }catch(Exception e){
                if(running) logger.writelog("WRN","Exception in admin socket server",e);
            }finally{
                socketThreads.remove(this);
            }
        }
    }
    
    // Generated by com.gripstudios.utils.DHParamGenerator
    private static final BigInteger DHModulus = new BigInteger(1,new byte[]{
    (byte)0x00,(byte)0xe3,(byte)0x0b,(byte)0x21,
    (byte)0xc0,(byte)0xad,(byte)0xbe,(byte)0x61,
    (byte)0xfd,(byte)0xb5,(byte)0xcc,(byte)0xdc,
    (byte)0xdf,(byte)0x9f,(byte)0x35,(byte)0xa5,
    (byte)0x3e,(byte)0x62,(byte)0x64,(byte)0x4d,
    (byte)0x3b,(byte)0xfb,(byte)0x1e,(byte)0x20,
    (byte)0x1e,(byte)0x94,(byte)0x1e,(byte)0xcf,
    (byte)0xcd,(byte)0xe1,(byte)0x0f,(byte)0xe8,
    (byte)0x3d,(byte)0x94,(byte)0x92,(byte)0xd3,
    (byte)0xe9,(byte)0x3c,(byte)0x5b,(byte)0xa0,
    (byte)0xf6,(byte)0xc7,(byte)0x53,(byte)0x79,
    (byte)0xb7,(byte)0xd0,(byte)0x82,(byte)0x1d,
    (byte)0xfe,(byte)0xca,(byte)0x73,(byte)0x55,
    (byte)0x29,(byte)0x01,(byte)0x9b,(byte)0x4f,
    (byte)0xb2,(byte)0x51,(byte)0xa1,(byte)0xbf,
    (byte)0xeb,(byte)0x30,(byte)0x97,(byte)0x37,
    (byte)0x4b,(byte)0x13,(byte)0x55,(byte)0xf1,
    (byte)0xe5,(byte)0x04,(byte)0x3f,(byte)0xde,
    (byte)0x45,(byte)0xe0,(byte)0x30,(byte)0xa1,
    (byte)0x66,(byte)0x26,(byte)0x24,(byte)0x09,
    (byte)0x1b,(byte)0x5b,(byte)0x56,(byte)0xf2,
    (byte)0x38,(byte)0xc2,(byte)0xa4,(byte)0xf5,
    (byte)0xe8,(byte)0x92,(byte)0xef,(byte)0x55,
    (byte)0x81,(byte)0xb2,(byte)0x82,(byte)0xb4,
    (byte)0x1c,(byte)0x6e,(byte)0x67,(byte)0x94,
    (byte)0x67,(byte)0x9a,(byte)0xa0,(byte)0xf5,
    (byte)0xb3,(byte)0xc5,(byte)0xc6,(byte)0x1a,
    (byte)0x86,(byte)0x8c,(byte)0x5c,(byte)0x3a,
    (byte)0xe2,(byte)0x20,(byte)0x84,(byte)0x50,
    (byte)0x85,(byte)0x1b,(byte)0x38,(byte)0x76,
    (byte)0xe1,(byte)0x7b,(byte)0x71,(byte)0x23,
    (byte)0x27,(byte)0x84,(byte)0xda,(byte)0xa1,
    (byte)0xb3
    });
    private static final BigInteger DHBase = new BigInteger(1,new byte[]{
    (byte)0x00,(byte)0xce,(byte)0x45,(byte)0x06,
    (byte)0x1b,(byte)0xec,(byte)0x93,(byte)0xb0,
    (byte)0x62,(byte)0x4d,(byte)0xf6,(byte)0x3d,
    (byte)0x93,(byte)0x72,(byte)0x16,(byte)0x51,
    (byte)0xe0,(byte)0x6c,(byte)0x92,(byte)0x55,
    (byte)0x35,(byte)0x86,(byte)0x8a,(byte)0x57,
    (byte)0x3d,(byte)0x76,(byte)0x28,(byte)0x84,
    (byte)0x29,(byte)0xce,(byte)0xf3,(byte)0x9c,
    (byte)0x8f,(byte)0xd4,(byte)0xb8,(byte)0xde,
    (byte)0x93,(byte)0xb9,(byte)0x99,(byte)0xe5,
    (byte)0x85,(byte)0x3c,(byte)0xb7,(byte)0x06,
    (byte)0x81,(byte)0x54,(byte)0xaa,(byte)0xac,
    (byte)0xfd,(byte)0x5e,(byte)0xa5,(byte)0x9b,
    (byte)0x66,(byte)0xee,(byte)0x26,(byte)0x66,
    (byte)0x0b,(byte)0xe2,(byte)0xd2,(byte)0x9e,
    (byte)0x95,(byte)0xae,(byte)0xb1,(byte)0x12,
    (byte)0xaf,(byte)0xff,(byte)0xd4,(byte)0x1e,
    (byte)0x35,(byte)0xe0,(byte)0x8f,(byte)0x4f,
    (byte)0x88,(byte)0x8e,(byte)0xd2,(byte)0xaa,
    (byte)0xea,(byte)0x71,(byte)0x82,(byte)0x63,
    (byte)0x71,(byte)0xc0,(byte)0x80,(byte)0x4e,
    (byte)0xef,(byte)0xfc,(byte)0xa5,(byte)0xfe,
    (byte)0x2e,(byte)0xbb,(byte)0xb0,(byte)0xcb,
    (byte)0x01,(byte)0x0e,(byte)0xf8,(byte)0xc0,
    (byte)0xee,(byte)0xc9,(byte)0x86,(byte)0xba,
    (byte)0x4f,(byte)0x3c,(byte)0x6b,(byte)0x23,
    (byte)0xd6,(byte)0xf5,(byte)0xff,(byte)0xa3,
    (byte)0xfa,(byte)0x96,(byte)0x2e,(byte)0x09,
    (byte)0x14,(byte)0xd5,(byte)0x43,(byte)0x03,
    (byte)0x43,(byte)0xbd,(byte)0xa6,(byte)0xce,
    (byte)0x14,(byte)0x39,(byte)0x5c,(byte)0x32,
    (byte)0x70,(byte)0x65,(byte)0xfe,(byte)0xad,
    (byte)0x85
    });

/*    
    // The 1024 bit Diffie-Hellman modulus values used by SKIP
    public static final byte skip1024ModulusBytes[] = { 
        (byte)0xF4, (byte)0x88, (byte)0xFD, (byte)0x58, 
        (byte)0x4E, (byte)0x49, (byte)0xDB, (byte)0xCD, 
        (byte)0x20, (byte)0xB4, (byte)0x9D, (byte)0xE4, 
        (byte)0x91, (byte)0x07, (byte)0x36, (byte)0x6B, 
        (byte)0x33, (byte)0x6C, (byte)0x38, (byte)0x0D, 
        (byte)0x45, (byte)0x1D, (byte)0x0F, (byte)0x7C, 
        (byte)0x88, (byte)0xB3, (byte)0x1C, (byte)0x7C, 
        (byte)0x5B, (byte)0x2D, (byte)0x8E, (byte)0xF6, 
        (byte)0xF3, (byte)0xC9, (byte)0x23, (byte)0xC0, 
        (byte)0x43, (byte)0xF0, (byte)0xA5, (byte)0x5B, 
        (byte)0x18, (byte)0x8D, (byte)0x8E, (byte)0xBB, 
        (byte)0x55, (byte)0x8C, (byte)0xB8, (byte)0x5D, 
        (byte)0x38, (byte)0xD3, (byte)0x34, (byte)0xFD, 
        (byte)0x7C, (byte)0x17, (byte)0x57, (byte)0x43, 
        (byte)0xA3, (byte)0x1D, (byte)0x18, (byte)0x6C, 
        (byte)0xDE, (byte)0x33, (byte)0x21, (byte)0x2C, 
        (byte)0xB5, (byte)0x2A, (byte)0xFF, (byte)0x3C, 
        (byte)0xE1, (byte)0xB1, (byte)0x29, (byte)0x40, 
        (byte)0x18, (byte)0x11, (byte)0x8D, (byte)0x7C, 
        (byte)0x84, (byte)0xA7, (byte)0x0A, (byte)0x72, 
        (byte)0xD6, (byte)0x86, (byte)0xC4, (byte)0x03, 
        (byte)0x19, (byte)0xC8, (byte)0x07, (byte)0x29, 
        (byte)0x7A, (byte)0xCA, (byte)0x95, (byte)0x0C, 
        (byte)0xD9, (byte)0x96, (byte)0x9F, (byte)0xAB, 
        (byte)0xD0, (byte)0x0A, (byte)0x50, (byte)0x9B, 
        (byte)0x02, (byte)0x46, (byte)0xD3, (byte)0x08, 
        (byte)0x3D, (byte)0x66, (byte)0xA4, (byte)0x5D, 
        (byte)0x41, (byte)0x9F, (byte)0x9C, (byte)0x7C, 
        (byte)0xBD, (byte)0x89, (byte)0x4B, (byte)0x22, 
        (byte)0x19, (byte)0x26, (byte)0xBA, (byte)0xAB, 
        (byte)0xA2, (byte)0x5E, (byte)0xC3, (byte)0x55, 
        (byte)0xE9, (byte)0x2F, (byte)0x78, (byte)0xC7 }; 
    // The SKIP 1024 bit modulus
    public static final BigInteger skip1024Modulus = new BigInteger(1, skip1024ModulusBytes);
    // The base used with the SKIP 1024 bit modulus 
    public static final BigInteger skip1024Base = BigInteger.valueOf(2);    
*/
}
