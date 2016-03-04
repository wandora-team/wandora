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
 * SimpleFileServer.java
 *
 * Created on 24. heinäkuuta 2006, 16:33
 *
 */

package org.wandora.utils.fileserver;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;
/**
 * For the ssl to work you need to create a certificate in command prompt with the
 * keytool utility (should be in jdk bin directory).
 *
 * For example:
 * keytool -genkey -keystore storefile -keyalg RSA
 * 
 * After you have generated the certificate you need to run java with the following
 * parameters (or you may set the properties programmatically with System.setProperty):
 * -Djavax.net.ssl.keyStore=storefile -Djavax.net.ssl.keyStorePassword=password
 *
 * @author  olli
 */

/* Initialize with XMLParamProcessor like this

<fileserver xp:class="org.wandora.piccolo.services.FileServerService" xp:id="fileserver">
    <param>8898</param>
    <param xp:class="com.gripstudios.utils.fileserver.SimpleVirtualFileSystem">
        <param>/</param>
        <param>C:/</param>
        <param>http://localhost/</param>
    </param>
    <param>false</param>
    <param>user:password</param>
</fileserver> 
<fileserver xp:idref="fileserver" xp:method="start"/>
 
 */
public class SimpleFileServer extends Thread {

    private int port;
    private boolean running;
    private boolean printExceptions;
    private boolean useSSL;
    private String requiredCredentials;
    private VirtualFileSystem fileSystem;
    private String lf="\r\n";
    
    /** Creates a new instance of SimpleFileServer */
    public SimpleFileServer(int port,VirtualFileSystem fileSystem) {
        this(port,fileSystem,false,null);
    }
    public SimpleFileServer(String port,VirtualFileSystem fileSystem,String useSSL,String credentials) {
        this(Integer.parseInt(port),fileSystem,Boolean.parseBoolean(useSSL),credentials);
    }
    public SimpleFileServer(int port,VirtualFileSystem fileSystem,boolean useSSL,String credentials) {
        this.port=port;
        this.fileSystem=fileSystem;
        this.useSSL=useSSL;
        this.requiredCredentials=credentials;
    }
    
    public void setRequiredCredentials(String credentials){
        requiredCredentials=credentials;
    }
    public void setUseSSL(boolean value){
        useSSL=value;
    }
    
    public static void main(String[] args) throws Exception {
        String user="admin";
        String password="n1mda";
        String mountPoint=".";
        String httpServer="http://localhost/";
        int port=8898;
        
        for(int i=0;i<args.length;i++){
            if(args[i].equals("-P")){
                port=Integer.parseInt(args[i+1]);
                i++;
            }
            else if(args[i].equals("-u")){
                user=args[i+1];
                i++;
            }
            else if(args[i].equals("-p")){
                password=args[i+1];
                i++;
            }
            else if(args[i].equals("-m")){
                mountPoint=args[i+1];
                i++;
            }
            else if(args[i].equals("-s")){
                httpServer=args[i+1];
                i++;
            }
        }
        
        
        mountPoint=mountPoint.replace("\\","/");
        if(!mountPoint.endsWith("/")) mountPoint+="/";
        
/*        SimpleVirtualFileSystem fs=new SimpleVirtualFileSystem();
        fs.addDirectory("/",mountPoint,httpServer);
        SimpleFileServer sfs=new SimpleFileServer(port,fs);
        sfs.setRequiredCredentials(user+":"+password);
        sfs.start();*/
        SimpleFileServer sfs=new SimpleFileServer(port,new SimpleVirtualFileSystem("/",mountPoint,httpServer),false,user+":"+password);
        sfs.start();
        System.out.println("Server running at port "+port);
    }
    
    @Override
    public void start(){
        running=true;
        super.start();
    }
    
    public void stopServer(){
        running=false;
        this.interrupt();
    }
    
    @Override
    public void run(){
        try{
            ServerSocket ss;
            if(!useSSL){
                ss=new ServerSocket(port);
            }
            else{
                ss=SSLServerSocketFactory.getDefault().createServerSocket(port);
            }

            while(running){
                try{
                    final Socket s=ss.accept();
                    Thread t=new ClientThread(s);
                    t.start();
                }catch(Exception e){
                    if(printExceptions) e.printStackTrace();
                }
            }
        }catch(Exception e){
            if(printExceptions) e.printStackTrace();
        }
    }
    
    public static String readLine(InputStream in) throws IOException {
        StringBuilder buf=new StringBuilder();
        int c=0;
        while(true){
            c=in.read();
            if(c==-1) break;
            if( (char)c == '\r' ) continue;
            if( (char)c == '\n' ) break;
            buf.append((char)c);
            if(buf.length()>8192){
                throw new IOException("Line buffer exeeded");
            }
        }
        if(c==-1 && buf.length()==0) return null;
        return buf.toString();
    }
    
    public static String[] parseLine(InputStream in) throws IOException {
       
        String line=readLine(in);
        if(line==null) return null;

        ArrayList<String> parsed=new ArrayList<String>();
        StringBuffer item=new StringBuffer();
        int pos=0;
        boolean escape=false;
        while(pos<line.length()){
            char c=line.charAt(pos++);
            if(c=='\\') escape=true;
            else if(escape==true || c!=' ') {
                item.append(c);
                escape=false;
            }
            else{
                parsed.add(item.toString());
                item=new StringBuffer();
            }
        }
        if(item.length()>0) parsed.add(item.toString());
        return parsed.toArray(new String[parsed.size()]);
    }
    
    private class ClientThread extends Thread {
        private Socket socket;
        public ClientThread(Socket socket){
            this.socket=socket;
        }
        @Override
        public void run(){
            boolean loggedin=false;
            int logintries=0;
            try{
                OutputStream outStream=socket.getOutputStream();
                Writer out=new OutputStreamWriter(outStream);
                InputStream in=socket.getInputStream();
                String[] parsed=parseLine(in);
                while(parsed!=null){
                    if(parsed.length>0){
                        if(!loggedin){
                            if(parsed[0].equals("login")){
                                if(requiredCredentials==null || 
                                  (parsed.length>=2 && requiredCredentials.equals(parsed[1])) ){
                                    loggedin=true;
                                    out.write("OK login ok"+lf);
                                    out.flush();
                                }
                                else {
                                    logintries++;
                                    if(logintries>=3) 
                                        try{
                                            Thread.sleep(5000);
                                        }
                                        catch(InterruptedException ie){}
                                    out.write("ERR invalid user name or password"+lf);
                                    out.flush();
                                }
                            }
                            else if(parsed[0].equals("logout")){
                                out.write("OK terminating connection"+lf);
                                out.flush();
                                break;
                            }
                            else{
                                out.write("ERR invalid command"+lf);
                                out.flush();
                            }
                        }
                        else{
                            if(parsed[0].equals("listfiles")){
                                if(parsed.length>=2){
                                    String[] files=fileSystem.listFiles(parsed[1]);
                                    out.write("OK sending file list"+lf);
                                    out.write(files.length+lf);
                                    for(int i=0;i<files.length;i++){
                                        out.write(files[i]+lf);
                                    }
                                    out.flush();
                                }
                                else {
                                    out.write("ERR directory not given"+lf);
                                    out.flush();
                                }
                            }
                            else if(parsed[0].equals("listdirs")){
                                if(parsed.length>=2){
                                    String[] files=fileSystem.listDirectories(parsed[1]);
                                    out.write("OK sending dir list"+lf);
                                    out.write(files.length+lf);
                                    for(int i=0;i<files.length;i++){
                                        out.write(files[i]+lf);
                                    }
                                    out.flush();
                                }
                                else {
                                    out.write("ERR directory not given"+lf);
                                    out.flush();
                                }                            
                            }
                            else if(parsed[0].equals("fileexists")){
                                if(parsed.length>=2){
                                    File f=fileSystem.getRealFileFor(parsed[1]);
                                    if(f!=null){
                                        out.write("OK"+lf);
                                        out.write(""+f.exists()+lf);
                                        out.flush();
                                    }
                                    else{
                                        out.write("ERR invalid filename"+lf);
                                        out.flush();
                                    }
                                }
                                else{
                                    out.write("ERR file not given"+lf);
                                    out.flush();
                                }
                            }
                            else if(parsed[0].equals("get")){
                                if(parsed.length>=2){
                                    File f=fileSystem.getRealFileFor(parsed[1]);
                                    if(!f.exists()){
                                        out.write("ERR file does not exist");
                                        out.flush();
                                    }
                                    else{
                                        long size=f.length();
                                        out.write("OK sending file"+lf);
                                        out.write(size+lf);
                                        out.flush();
                                        byte[] buf=new byte[4096];
                                        try{
                                            InputStream fin=new FileInputStream(f);
                                            int read=0;
                                            while( (read=fin.read(buf))!=-1 ){
                                                outStream.write(buf,0,read);
                                            }
                                        }
                                        catch(IOException ioe){
                                            ioe.printStackTrace();
                                            break;
                                        }
                                    }
                                }
                                else{
                                    out.write("ERR file not given"+lf);
                                    out.flush();
                                }
                            }
                            else if(parsed[0].equals("put")){
                                if(parsed.length>=3){
                                    File f=fileSystem.getRealFileFor(parsed[1]);
                                    if(f==null){
                                        out.write("ERR invalid file name"+lf);
                                        out.flush();
                                    }
                                    else{
                                        out.write("OK ready to receive file"+lf);
                                        out.flush();
                                        long size=Long.parseLong(parsed[2]);
                                        OutputStream fout=new FileOutputStream(f);
                                        byte[] buf=new byte[4096];
                                        long read=0;
                                        int bread=0;
                                        while(read<size){
                                            if(size-read>buf.length) bread=in.read(buf);
                                            else bread=in.read(buf,0,(int)(size-read));
                                            read+=bread;
                                            fout.write(buf,0,bread);
                                        }
                                        fout.close();
                                        out.write("OK file received"+lf);
                                        out.flush();
                                    }
                                }
                                else{
                                    out.write("ERR file and/or size not given"+lf);
                                    out.flush();
                                }
                            }
                            else if(parsed[0].equals("geturlfor")){
                                if(parsed.length>=2){
                                    String url=fileSystem.getURLFor(parsed[1]);
                                    out.write("OK"+lf);
                                    if(url==null) out.write("null"+lf);
                                    else out.write(url+lf);
                                    out.flush();
                                }
                                else{
                                    out.write("ERR file not given"+lf);
                                    out.flush();
                                }
                            }
                            else if(parsed[0].equals("logout")){
                                out.write("OK terminating connection"+lf);
                                out.flush();
                                break;
                            }
                            else{
                                out.write("ERR invalid command"+lf);
                                out.flush();
                            }
                        }
                    }
                    parsed=parseLine(in);
                }
            }catch(Exception e){
                if(printExceptions) e.printStackTrace();
            }
            finally{
                try{
                    socket.close();
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
        }
    }
}
