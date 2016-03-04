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
 * SocketServerInterface.java
 *
 * Created on June 8, 2004, 11:53 AM
 */

package org.wandora.topicmap.remote.server;
import org.wandora.application.*;
import org.wandora.exceptions.WandoraException;
import org.wandora.exceptions.ConcurrentEditingException;
import org.wandora.*;
import org.wandora.tools.*;
import org.wandora.topicmap.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.net.*;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

/**
 * This is the class implementing ServerInterface, the server interface for the 
 * old remote topic maps. It is not used anymore and should be considered deprecated.
 *
 * @author  olli
 */
public class SocketServerInterface  implements ServerInterface {
    
    private TopicMap tm;
    private Socket s;
    private InputStream in;
    private InputStream originalIn;
    private OutputStream out;
    private Hashtable originalTimes;
    private long pingdelay;
    
    private boolean uncommitted;
    
    private boolean gzip;
//    private Hashtable typeIndex;
//    private Hashtable topicIndex;
    
    private SecretKey key;
    
    private StringBuffer session;
    
    private ErrorHandler errorHandler;
    
    private String host;
    private int port;
    
    /** Creates a new instance of SocketServerInterface */
/*    public SocketServerInterface(Socket s) throws IOException {
        pingdelay=0;
        this.s=s;
        initialize();
        gzip=false;
        uncommitted=false;
    }*/
    
    public SocketServerInterface(String host,int port) throws IOException {
        this(host,port,0);
    }
    public SocketServerInterface(String host,int port,long pingdelay) throws IOException {
        this.host=host;
        this.port=port;
        this.pingdelay=pingdelay;
//        s=SocketFactory.getDefault().createSocket(host,port);
//        connect();
    }
    public SocketServerInterface() throws IOException {
        this("localhost",AdminSocketServer.DEFAULT_PORT);
    }
    
    public void connect() throws IOException {
        s=SocketFactory.getDefault().createSocket(host,port);
        initialize();
        if(pingdelay!=0){
            Thread thread=new PingThread();
            thread.start();
        }
        uncommitted=false;
    }
    
    private void initialize() throws IOException {
//        originalIn=new SpyInputStream(s.getInputStream(),System.out);
        originalIn=s.getInputStream();
//        in=new PushbackInputStream(originalIn,32);
        in=originalIn;
//        out=new SpyOutputStream(s.getOutputStream(),System.out);
        out=s.getOutputStream();
        originalTimes=new Hashtable();
        initializeTopicMap();        
    }

    private void initializeTopicMap() {
        tm=new org.wandora.topicmap.memory.TopicMapImpl();
        session=new StringBuffer();
//        typeIndex=new Hashtable();
//        topicIndex=new Hashtable();
    }
    
    public synchronized void checkTopics(TopicMap tm) throws ServerException {
        try{
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            if(gzip){
                java.util.zip.GZIPOutputStream gzip=new java.util.zip.GZIPOutputStream(baos);
                tm.exportXTM(gzip);
                gzip.finish();                
                gzip.flush();
            }
            else tm.exportXTM(baos);
            byte[] buf=baos.toByteArray();
            out.write( ("checkedited("+buf.length+")\n").getBytes("UTF-8") );
            out.write(buf);
            out.flush();
            String line=readLine();
            if(!line.startsWith("OK")) {
                throw new ServerException(line);
            }
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public String getTimeStamp(Topic t){
        return ""+originalTimes.get(t);
    }
    
    public synchronized void commit(boolean force) throws WandoraException,ServerException {
        try{
            String cmd="commit("+force+")\n";
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")){
                uncommitted=false;
                // ok
            }
            else if(line.startsWith("CONCURRENTEDITING")){
                HashSet failed=new HashSet();
                HashSet removed=new HashSet();
                String rest=line.substring("CONCURRENTEDITING ".length());
                int ind=rest.indexOf(" ");
                int count=Integer.parseInt(rest.substring(0,ind));;
                int removedcount=Integer.parseInt(rest.substring(ind+1));
                String l;
                for(int i=0;i<count;i++){
                    l=readLine();
                    failed.add(Locator.parseLocator(l));
                }
                for(int i=0;i<removedcount;i++){
                    l=readLine();
                    removed.add(Locator.parseLocator(l));
                }
                throw new ConcurrentEditingException(failed,removed);
            }
            else if(line.startsWith("TOPICINUSE")){
                String l=line.substring("TOPICINUSE ".length());
                Topic t=getTopic(l);
                throw new TopicInUseException(t);
            }
            else {
                throw new WandoraException("Error commiting changes. Server returned: "+line);
            }
            initializeTopicMap();
        } catch(Exception ioe){
            throw new ServerException(ioe);
        }
    }
    
    private String readLine() throws IOException {
        return readLine(in);
    }
/*    public static String readLine(PushbackInputStream in){
        byte[] buf=new byte[32];
        byte[] read=new byte[256];
        int ptr=0;
        int l;
        int copy;
        boolean cont=true;
        while(cont){
            try{
                l=in.read(buf);
            }catch(IOException ioe){
                ioe.printStackTrace();
                return null;
            }
            if(l==-1) {
                if(ptr==0) return null;
                else break;
            }
            if(ptr+l>read.length){
                byte[] nread=new byte[read.length*2];
                System.arraycopy(read, 0, nread, 0, read.length);
                read=nread;
            }
            for(int i=0;i<l;i++){
                if(buf[i]!='\n') {
                    read[ptr++]=buf[i];
                    System.out.print((char)buf[i]);
                }
                else {
                    System.out.print("\\n");
                    cont=false;
                    if(i+1<l){
                        try{
                            System.out.print(" unreading \""+new String(buf,i+1,l-(i+1))+"\"");
                            in.unread(buf,i+1, l-(i+1));
                        }catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                    System.out.println();
                    break;
                }
            }
        }
        String s=new String(read,0,ptr);
//        System.out.println(s);
        return s;
    }*/
    public static String readLine(InputStream in) throws IOException {
        byte[] read=new byte[256];
        int ptr=0;
        int c;
        do{
            c=in.read();
            if(c==-1) throw new IOException("Socket closed");
            if(c!='\n'){
                if(ptr==read.length){
                    byte[] nread=new byte[read.length*2];
                    System.arraycopy(read, 0, nread, 0, read.length);
                    read=nread;
                }
                read[ptr++]=(byte)c;
            }
        }while(c!='\n');
        return new String(read,0,ptr, "UTF-8");
    }
    
    private synchronized Topic fetchTopic(String topicSI) throws ServerException {
        Topic[] ts=fetchTopics(new String[]{topicSI});
        if(ts!=null) return ts[0];
        else return null;
    }
    
    private void readTopicMapIn() throws IOException,TopicMapException {
        String line=readLine();
        if(line.startsWith("OK")){
            long length=Long.parseLong(line.substring(3).trim());
            TopicMap temptm=new org.wandora.topicmap.memory.TopicMapImpl();
            if(gzip) temptm.importXTM(new java.util.zip.GZIPInputStream(new LimitedInputStream(in,length)));
            else temptm.importXTM(new LimitedInputStream(in,length));
            tm.mergeIn(temptm);
        }
        else return;
    }
    
    private synchronized Topic[] fetchTopicsOfType(String typeSI) throws ServerException{
        System.out.println("getting topics of type "+typeSI);
//        if(typeIndex.get(typeSI)!=null) return (Topic[])typeIndex.get(typeSI);
        try{
            out.write( ("gettopicsoftype(\""+encode(typeSI)+"\")\n").getBytes("UTF-8") );
            readTopicMapIn();
        }catch(Exception e){
            throw new ServerException(e);
        }
        try{
            Collection topics=new ArrayList();
            Topic t=tm.getTopic(typeSI);
            if(t!=null) topics=tm.getTopicsOfType(t);
            Topic[] ts=(Topic[])topics.toArray(new Topic[topics.size()]);
//            typeIndex.put(typeSI,ts);
            return ts;
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    
    private synchronized Topic[] fetchTopics(String[] topicSIs) throws ServerException{
        String neededTopics="";
        Topic[] ts=new Topic[topicSIs.length];
        boolean first=true;
        for(int i=0;i<topicSIs.length;i++){
//            Topic t=(Topic)topicIndex.get(topicSIs[i]);
//            if(t!=null) ts[i]=t;
//            else{
                if(first) first=false;
                else neededTopics+=",";
                neededTopics+="\""+encode(topicSIs[i])+"\"";
//            }
        }
        if(neededTopics.length()==0) return ts;
        System.out.println("getting topics "+neededTopics);
        
        try{
            out.write( ("gettopics("+neededTopics+")\n").getBytes("UTF-8") );
            readTopicMapIn();
        }catch(Exception e){
            throw new ServerException(e);
        }
        try{
            for(int i=0;i<topicSIs.length;i++){
                Topic t=tm.getTopic(tm.createLocator(topicSIs[i]));
//                if(t!=null) topicIndex.put(topicSIs[i],t);
                ts[i]=t;
            }
            return ts;
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public synchronized Topic getTopic(String topicSI) throws ServerException{
//        Topic t=(Topic)topicIndex.get(topicSI);
//        if(t!=null) return t;
//        else 
            return fetchTopic(topicSI);
    }
    
    public synchronized Topic[] getTopics(String[] topicSIs)  throws ServerException{
/*        boolean missing=false;
        Topic[] ts=new Topic[topicSIs.length];
        for(int i=0;i<topicSIs.length && !missing;i++){
            Topic t=(Topic)topicIndex.get(topicSIs[i]);
            if(t!=null) ts[i]=t;
            else missing=true;
        }
        if(!missing) return ts;
        else*/ return fetchTopics(topicSIs);
    }
    
    public synchronized Topic[] getTopicsOfType(String typeSI)  throws ServerException {
//        if(typeIndex.get(typeSI)!=null) return (Topic[])typeIndex.get(typeSI);
//        else
            return fetchTopicsOfType(typeSI);
    }
    
    public synchronized void mergeIn(TopicMap tm)  throws ServerException {
        try{
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            if(gzip){
                java.util.zip.GZIPOutputStream gzip=new java.util.zip.GZIPOutputStream(baos);
                tm.exportXTM(gzip);
                gzip.finish();                
                gzip.flush();
            }
            else tm.exportXTM(baos);
            byte[] buf=baos.toByteArray();
            out.write( ("mergein("+buf.length+")\n").getBytes("UTF-8") );
            out.write(buf);
            out.flush();
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public synchronized void removeAssociation(Association a)  throws ServerException{
        try{
            String cmd="removeassociation("+
                        "\""+getTransferableSI(a.getType())+"\",(";
                        
            boolean first=true;
            Iterator iter=a.getRoles().iterator();
            while(iter.hasNext()){
                Topic role=(Topic)iter.next();
                if(!first) cmd+=",";
                else first=false;
                cmd+="(\""+getTransferableSI(a.getPlayer(role))+"\",";
                cmd+="\""+getTransferableSI(role)+"\")";
            }
            cmd+="))\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public synchronized void removeData(Topic t, Topic type, Topic version) throws ServerException {
        try{
            String cmd="removedata("+
                        "\""+getTransferableSI(t)+"\","+
                        "\""+getTransferableSI(type)+"\","+
                        "\""+getTransferableSI(version)+"\"\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized void removeTopic(Topic t) throws ServerException {
        try{
            String cmd="removetopic("+
                        "\""+getTransferableSI(t)+"\")\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized void removeVariantName(Topic t,Collection scope) throws ServerException {
        try{
            String cmd="removetopicvariantname("+
                        "\""+getTransferableSI(t)+"\","+
                        "(";
            
            boolean first=true;
            Iterator iter=scope.iterator();
            while(iter.hasNext()){
                Topic st=(Topic)iter.next();
                if(first) first=false;
                else cmd+=",";
                cmd+="\""+getTransferableSI(st)+"\"";
            }
            cmd+="))\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized void removeBaseName(Topic t) throws ServerException {
        try{
            String cmd="removetopicbasename("+
                        "\""+getTransferableSI(t)+"\")\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized void rollback() throws ServerException {
        try{
            String cmd="rollback\n";
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            initializeTopicMap();
            uncommitted=false;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized Topic getTopicByName(String name) throws ServerException {
        try{
            out.write( ("gettopicbyname(\""+encode(name)+"\")\n").getBytes("UTF-8") );
            readTopicMapIn();
        }catch(Exception e){
            throw new ServerException(e);
        }
        try{
            Topic t=tm.getTopicWithBaseName(name);
//            if(t!=null) topicIndex.put(((Locator)t.getSubjectIdentifiers().iterator().next()).toExternalForm(),t);
            return t;
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public synchronized void removeSubjectLocator(Topic t) throws ServerException {
        try{
            String cmd="removesubjectlocator("+
                        "\""+getTransferableSI(t)+"\")\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized void removeTopicType(Topic t, Topic type) throws ServerException {
        try{
            String cmd="removetopictype("+
                        "\""+getTransferableSI(t)+"\","+
                        "\""+getTransferableSI(type)+"\")\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized boolean clearTopicMap() throws ServerException {
        try{
            out.write( "cleartopicmap()\n".getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            else return false;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public void clearCache() {
        initializeTopicMap();
    }
    
    public synchronized void writelog(String lvl,String msg) throws ServerException {
        try{
            out.write( ("writelog(\""+encode(lvl)+"\",\""+encode(msg)+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized boolean writeTopicMapTo(java.io.OutputStream tmout) throws IOException,ServerException {
        try{
            out.write( "gettopicmap()\n".getBytes("UTF-8") );
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
        String line=readLine();
        if(line.startsWith("OK")){
            long length=Long.parseLong(line.substring(3).trim());
            InputStream tmin=new LimitedInputStream(in,length);
            if(gzip){
                tmin=new java.util.zip.GZIPInputStream(tmin);
            }
            byte[] buf=new byte[4096];
            int read=-1;
            while((read=tmin.read(buf))!=-1){
                tmout.write(buf,0,read);
            }
            return true;
        }
        else return false;
    }
    
    public synchronized String upload(java.io.InputStream in, String filename, long length) throws ServerException {
        return upload(in,filename,length,false);
    }
    public synchronized String upload(java.io.InputStream in, String filename, long length,boolean overwrite) throws ServerException {
        try{
            out.write( ("upload(\""+length+"\",\""+encode(filename)+"\",\""+overwrite+"\")\n").getBytes("UTF-8") );
            byte[] buf=new byte[4096];
            int read=0;
            while( (read=in.read(buf) )!=-1 ){
                out.write(buf,0,read);
            }
            out.flush();
            in.close();
            String line=readLine();
            if(line.startsWith("OK")){
                return line.substring(3).trim();
            }
            else throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    public synchronized String[] listDirectories(String dir) throws ServerException {
        try{
            out.write( ("listdirectories(\""+encode(dir)+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")){
                int count=Integer.parseInt(line.substring(3).trim());
                String[] dirs=new String[count];
                for(int i=0;i<count;i++){
                    dirs[i]=readLine().trim();
                }
                return dirs;
            }
            else throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    public synchronized String[] listFiles(String dir) throws ServerException {
        try{
            out.write( ("listfiles(\""+encode(dir)+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")){
                int count=Integer.parseInt(line.substring(3).trim());
                String[] files=new String[count];
                for(int i=0;i<count;i++){
                    files[i]=readLine().trim();
                }
                return files;
            }
            else throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }        
    }
    public synchronized boolean fileExists(String file) throws ServerException {
        try{
            out.write( ("fileexists(\""+encode(file)+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")){
                boolean b=new Boolean(line.substring(3).trim()).booleanValue();
                return b;
            }
            else throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
        
    }
    
    public synchronized boolean delete(String filename) throws ServerException {
        try{
            out.write( ("deletefile(\""+encode(filename)+"\")").getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")) return true;
            else throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized boolean gzip() throws ServerException{
        try{
            out.write( "gzip\n".getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")){
                gzip=true;
                return true;
            }
            else throw new ServerException(line);
        }catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public synchronized boolean cipher() throws ServerException{
        try{
            out.write( "cipher\n".getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("PUBLIC")){
                int publength=Integer.parseInt(line.substring("PUBLIC ".length()));
                byte[] pub=new byte[publength];
                int read=0;
                while(read<publength){
                    read+=in.read(pub,read, publength-read);
                }
                KeyFactory keyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pub);
                PublicKey pubKey = keyFac.generatePublic(x509KeySpec);
                DHParameterSpec dhParamSpec = ((DHPublicKey)pubKey).getParams();
                KeyPairGenerator kpairGen = KeyPairGenerator.getInstance("DH");
                kpairGen.initialize(dhParamSpec);
                KeyPair kpair = kpairGen.generateKeyPair();
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                keyAgree.init(kpair.getPrivate());

                byte[] pubKeyEnc = kpair.getPublic().getEncoded();
                out.write( ("PUBLIC(\""+pubKeyEnc.length+"\")\n").getBytes("UTF-8") );
                out.write(pubKeyEnc);
                out.flush();

                keyAgree.doPhase(pubKey,true);
                key = keyAgree.generateSecret("DES");
                line=readLine();
                if(line.startsWith("PARAMS")){
                    int paramlength=Integer.parseInt(line.substring("PARAMS ".length()));
                    read=0;
                    pub=new byte[paramlength];
                    while(read<paramlength){
                        read+=in.read(pub,read, paramlength-read);
                    }
                    AlgorithmParameters aparams = AlgorithmParameters.getInstance("DES");
                    aparams.init(pub);
                    Cipher c=Cipher.getInstance("DES/CFB8/NoPadding");
                    c.init(Cipher.DECRYPT_MODE,key,aparams);
//                    in=new PushbackInputStream(new CipherInputStream(in,c),32);                    
                    in=new CipherInputStream(in,c);                    
                    
                    c=Cipher.getInstance("DES/CFB8/NoPadding");
                    c.init(Cipher.ENCRYPT_MODE,key);
                    aparams=c.getParameters();
                    pub=aparams.getEncoded();
                    out.write( ("PARAMS(\""+pub.length+"\")\n").getBytes("UTF-8") );
                    out.write(pub);
                    out.flush();
                    out=new CipherOutputStream(out,c);
                    System.out.println("Switched to ciphered streams");
                    return true;
                }
                else throw new ServerException(line);
            }
            else throw new ServerException(line);
        }
        catch(Exception e){
            throw new ServerException(e);
        }
    }
    
    public synchronized boolean login(String user, String password) throws ServerException {
        try{
            out.write( ("login(\""+encode(user)+"\",\""+encode(password)+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")) return true;
            else return false;
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public boolean needLogin() {
        return true;
    }
    
    public synchronized boolean openTopicMap(String key)  throws ServerException{
        try{
            if(key==null)
                out.write( "opentopicmap()\n".getBytes("UTF-8") );
            else
                out.write( ("opentopicmap(\""+encode(key)+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(line.startsWith("OK")){
                uncommitted=false;
                return true;
            }
            else throw new ServerException(line);
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public synchronized void removeSubjectIdentifier(Topic t, Locator l) throws ServerException {
        try{
            String cmd="removesubjectidentifier(\""+getTransferableSI(t)+"\",\""+encode(l.toExternalForm())+"\")\n";
            session.append(cmd);
            out.write( cmd.getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            uncommitted=true;
        }catch(IOException e){
            throw new ServerException(e);
        }
    }
    
    public synchronized String customCommand(String command) throws ServerException {
        if(!command.endsWith("\n")) command+="\n";
        try{
            out.write(command.getBytes("UTF-8"));
            String line=readLine();
            return line;
        }catch(IOException e){
            throw new ServerException(e);
        }
    }
    
    public synchronized boolean ping() throws ServerException{
        try{
            out.write( ("ping\n").getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("PONG")) throw new ServerException(line);
            return true;
        }catch(IOException e){
            throw new ServerException(e);
        }
    }
    
    public boolean isUncommitted() {
        return uncommitted;
    }
    
    public synchronized String[] search(String query) throws ServerException {
        query=query.replace('\n',' ');
        query=query.replaceAll("\"","\\\"");
        try{
            out.write( ("search(\""+query+"\")\n").getBytes("UTF-8") );
            String line=readLine();
            if(!line.startsWith("OK")) throw new ServerException(line);
            int count=Integer.parseInt(line.substring("OK ".length()));
            String[] res=new String[count*3];
            for(int i=0;i<count*3;i++){
                res[i]=readLine();
            }
            return res;
        }catch(IOException e){
            throw new ServerException(e);
        }
    }
    
    public synchronized void close()  throws ServerException{
        try{
            out.write( "exit\n".getBytes("UTF-8") );
            s.close();
        }catch(IOException ioe){
            throw new ServerException(ioe);
        }
    }
    
    public StringBuffer getSession(){
        return session;
    }
    
    public void setServerErrorHandler(ErrorHandler handler) {
        errorHandler=handler;
    }
    
    public void handleServerError(Exception e) {
        if(errorHandler!=null) errorHandler.handleError(e);
        else{
            e.printStackTrace();
        }
    }
    
    public boolean isConnected() {
        if(s==null) return false;
        if(!s.isConnected()) return false;
        if(s.isClosed()) return false;
        return true;
    }
    
    public void applySession(StringBuffer session) throws ServerException {
        StringTokenizer st=new StringTokenizer(session.toString(),"\n");
        while(st.hasMoreTokens()){
            String t=st.nextToken();
            try{
                out.write( (t+"\n").getBytes("UTF-8") );
                String line=readLine();
                if(!line.startsWith("OK")) throw new ServerException(line);
            }catch(IOException ioe){
                throw new ServerException(ioe);
            }
        }
    }
    
    
    
    private String getTransferableSI(Topic t) {
        try {
            String sis = encode(((Locator) t.getSubjectIdentifiers().iterator().next()).toExternalForm());
            return sis;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "http://wandora.org/si/dummy";
    }
    
    
    
    public static String decode(String sis) {
        try { 
            return URLDecoder.decode(sis, "UTF-8");
        }
        catch (Exception e) {
            return sis;
        }
    }
    
    
    public static String encode(String sis) {
        try { 
            return URLEncoder.encode(sis, "UTF-8");
        }
        catch (Exception e) { 
            return sis;
        }
    }
    
    
    
    
/*  // UNTESTED and not needed  
    public boolean removeTopicAllowed(Topic t) {
        try{
            String cmd="removetopicallowed("+
                        "\""+t.getSubjectIdentifiers().iterator().next()+"\")\n";
            out.write( cmd.getBytes("UTF-8") );
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        String line=readLine();
        if(!line.startsWith("OK")) return true;
        else return false;
    }*/
    
    private class PingThread extends Thread {
        public boolean running;
        public PingThread(){
            running=true;
        }
        public void run(){
            while(running){
                long nextPing=System.currentTimeMillis()+pingdelay;
                long time=System.currentTimeMillis();
                while(time<nextPing){
                    try{
                        Thread.sleep(nextPing-time);
                    }catch(InterruptedException e){
                        System.out.println("Ping thread interrupted");
                        return;
                    }
                    time=System.currentTimeMillis();
                }
                try{
                    if(!ping()){
                        System.out.println("Ping failed, stopping ping thread.");
                        running=false;
                    }
                }catch(ServerException se){
                    System.out.println("Ping failed, stopping ping thread.");
                    if(errorHandler!=null) errorHandler.handleError(se);
                    running=false;
                }
            }
        }
    }
    
}

class SpyInputStream extends InputStream {
    private InputStream in;
    private OutputStream out;
    public SpyInputStream(InputStream in,OutputStream out){
        this.in=in;
        this.out=out;
    }
    public int available() throws IOException {
        return in.available();
    }
    public void close() throws IOException {
        in.close();
    }
    public void mark(int readlimit) {
        in.mark(readlimit);
    }
    public boolean markSupported(){
        return in.markSupported();
    }
    public int read() throws IOException {
        int i=in.read();
        out.write(i);
        return i;
    }
    public int read(byte[] b) throws IOException {
        int r=in.read(b);
        out.write(b,0,r);
        return r;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        int r=in.read(b,off,len);
        out.write(b,off,r);
        return r;
    }
    public void reset() throws IOException {
        in.reset();
    }
    public long skip(long n) throws IOException {
        return in.skip(n);
    }
}

class SpyOutputStream extends OutputStream {
    private OutputStream out;
    private OutputStream out2;
    public SpyOutputStream(OutputStream out,OutputStream out2){
        this.out=out;
        this.out2=out2;
    }
    
    public void write(int b) throws IOException {
        out.write(b);
        out2.write(b);
    }
    public void write(byte[] buf) throws IOException {
        out.write(buf);
        out2.write(buf);
    }
    public void write(byte[] buf,int off,int len) throws IOException {
        out.write(buf,off,len);
        out2.write(buf,off,len);
    }
    public void flush() throws IOException {
        out.flush();
        out2.flush();
    }
    public void close() throws IOException {
        out.close();
        out2.close();
    }
}
