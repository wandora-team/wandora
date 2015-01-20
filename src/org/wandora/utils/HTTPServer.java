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
 * HTTPServer.java
 *
 * Created on 6. kesäkuuta 2005, 14:54
 *
 */

package org.wandora.utils;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;

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
 * Every request is sent to handleRequest(Socket) which, after doing basic
 * authentication and parsing the requests, calls handleRequest(Socket,String).
 * It then calls the two abstract methods getPage and getContentType.
 * The most simple way to make a HTTPServer is to implement these two methods.
 * If you need to do more advanced handling you may also override one of the
 * handleRequest methods.
 *
 * @author  olli
 */
public abstract class HTTPServer {
    protected int port;
    protected boolean running;
    protected boolean printExceptions;
    protected boolean useSSL;
    protected String loginUser;
    protected String loginPass;
    protected ServerSocket serverSocket;
    protected ServerThread serverThread;
    
    protected HashMap<String,String> mimeTypes;

    /** Creates a new instance of ScreenShotServer */
    public HTTPServer(int port) {
        this.port=port;
        printExceptions=true;
        useSSL=false;
        mimeTypes=new HashMap<String,String>();
        registerMimeTypes();
    }
    
    protected void registerMimeTypes(){
        mimeTypes.put("html","text/html");
        mimeTypes.put("htm", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("jpg", "image/jpg");
        mimeTypes.put("jpeg","image/jpg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("mp3", "audio/mpeg");
        mimeTypes.put("mpg", "video/mpeg");
        mimeTypes.put("mpeg","video/mpeg");
        mimeTypes.put("avi", "video/x-msvideo");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("zip", "application/zip");
    }
    
    /**
     * Handles http request. Does simple authentication check if requiredCredentials
     * has been set.
     */
    protected void handleRequest(Socket s) throws IOException {
        String credentials=null;
        String get=null;
        BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
        String request=in.readLine();
        while(request.trim().length()>0){
            StringTokenizer st=new StringTokenizer(request);
            if(st.hasMoreTokens()){
                String first=st.nextToken();
                if(first.equals("Authorization:")){
                    if(!st.hasMoreTokens()) continue;
                    st.nextToken();
                    if(!st.hasMoreTokens()) continue;
                    credentials=st.nextToken();
                }
                else if(first.equals("GET")){
                    if(!st.hasMoreTokens()) continue;
                    get=st.nextToken();
                }
            }
            request=in.readLine();
        }
        String requiredCredentials=null;
        if(loginUser!=null) requiredCredentials=loginUser+":"+loginPass;

        if(requiredCredentials!=null){            
            boolean ok=false;
            if(credentials!=null){
                byte[] bs=Base64.decode(credentials);
                String gotc=new String(bs);
                ok=requiredCredentials.equals(gotc);
            }
            if(!ok){
                OutputStream out=s.getOutputStream();
                out.write( ("HTTP/1.0 401 Authorization Required\nWWW-Authenticate: Basic realm=\""+getRealm()+"\"\n").getBytes() );
                s.close();
                return;
            }
        }

        handleRequest(s,get);
    }
    
    public String getRealm(){
        return "Grip Realm";
    }
    
    public static void copyStream(InputStream in,OutputStream out) throws IOException {
        byte[] buf=new byte[8192];
        int read=-1;
        while((read=in.read(buf))!=-1){
            out.write(buf,0,read);
        }
    }
    
    public void returnFile(OutputStream out,File f) throws IOException {
        if(!f.exists() || f.isDirectory()){
            notFound(out);
            return;
        }
        writeHeaderForFile(out,f);
        InputStream in=new FileInputStream(f);
        copyStream(in,out);
        in.close();
    }
    
    public void writeHeaderForFile(OutputStream out,String extension) throws IOException {
        String type=null;
        if(extension!=null) type=mimeTypes.get(extension.toLowerCase());
        if(type==null) type="text/plain";
        writeHeader(out,type);
    }
    public void writeHeaderForFile(OutputStream out,File f) throws IOException {
        String name=f.getName();
        int ind=name.lastIndexOf(".");
        if(ind==-1) writeHeaderForFile(out,(String)null);
        else writeHeaderForFile(out,name.substring(ind+1));
    }
    public static void writeHTMLHeader(OutputStream out) throws IOException {
        writeHeader(out,"text/html");
    }
    public static void writeHeader(OutputStream out,String contentType) throws IOException {
        out.write( "HTTP/1.0 200 OK\n".getBytes() );
        out.write( ("Content-Type: "+contentType+"\n\n").getBytes() );
    }
    public static void writeSimpleHTML(OutputStream out,String title,String content) throws IOException {
        writeHTMLHeader(out);
        String page="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"+
                    "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"+
                    (title!=null?"<title>"+title+"</title>":"")+
                    "</head><body>"+content+"</body></html>";
        try{
            out.write(page.getBytes("UTF-8"));
        }catch(UnsupportedEncodingException uee){}
    }
    public static void writeInternalServerError(OutputStream out,String message,Throwable t) throws IOException {
        StringWriter s=new StringWriter();
        PrintWriter p=new PrintWriter(s);
        t.printStackTrace(p);
        p.flush();
        writeSimpleHTML(out,"500 Internal Server Error","500 Internal Server Error<br />"+(message!=null?message+"<br />":"")+"<pre>"+s.toString()+"</pre>");
    }
    /**
     * Handles the request by getting the contents of the page using getPage.
     * Then writes the header and the contents and closes socket.
     */
    protected void handleRequest(Socket s,String get) throws IOException {
        String[] parameters=parseGetParams(get);
        OutputStream out=s.getOutputStream();
        
        if(!getPage(out,parameters)){
            notFound(out);
        }
        s.close();
    }
    
    /**
     * Gets the contents of the page for the given request.
     */
    protected abstract boolean getPage(OutputStream out,String[] parameters);

    
    /**
     * Parses parameters from url. 
     * @return A String array where first element is the requested page and after that
     *         every odd index contains a parameter key and
     *         every even index value for previous key.
     */
    public static String[] parseGetParams(String get){
        try{
            int ind=get.indexOf("?");
            if(ind==-1) return new String[]{URLDecoder.decode(get,"UTF-8")};
            String query=get.substring(0,ind);
            String[] params=get.substring(ind+1).split("&");
            String[] ret=new String[params.length*2+1];
            ret[0]=URLDecoder.decode(query,"UTF-8");
            for(int i=0;i<params.length;i++){
                String key=null;
                String value=null;
                ind=params[i].indexOf("=");
                if(ind==-1) key=params[i];
                else {
                    key=params[i].substring(0,ind);
                    value=params[i].substring(ind+1);
                }
                if(key!=null) key=URLDecoder.decode(key,"UTF-8");
                if(value!=null) value=URLDecoder.decode(value,"UTF-8");
                ret[1+i*2]=key;
                ret[1+i*2+1]=value;
            }
            return ret;
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * A convenience method to respond with a not found message.
     */
    public static void notFound(OutputStream out) throws IOException {
        out.write( "HTTP/1.0 404 Not Found\n\n404 Not Found.".getBytes() );
    }
    public static void badRequest(OutputStream out) throws IOException {
        out.write( "HTTP/1.0 400 Bad Request\n\n400 Bad Request.".getBytes() );
    }
    
    /**
     * Returns the first parameter value with the given key.
     * @param param The key of the param to find.
     * @param params The parameters as returned by parseGetParams.
     * @return The first parameter value with the given key or null if non is found.
     */
    public static String getParamValue(String param,String[] params){
        for(int i=1;i<params.length;i+=2){
            if(params[i].equals(param)) return params[i+1];
        }
        return null;
    }
        
    public void setPrintExceptions(boolean value){
        printExceptions=value;
    }
    
    
    public int getPort(){return port;}
    public void setPort(int p){port=p;}
    
    /**
     * Set credential required to use the server. If user is null or empty string,
     * allows anonymous login.
     */
    public void setLogin(String user,String password){
        if(user==null || user.length()==0) {
            loginUser=null;
            loginPass=null;
        }
        else {
            loginUser=user;
            loginPass=password;
        }
    }
    
    public String getLoginUser(){return loginUser;}
    public String getLoginPassword(){return loginPass;}
    /**
     * Enables or disables use of SSL sockets. You must set this before
     * starting the server. See notes in the class description about
     * configuring SSL.
     */
    public void setUseSSL(boolean value){
        useSSL=value;
    }
    
    public boolean isUseSSL(){return useSSL;}
    
    /**
     * Starts the server. If server is already running, stops it first.
     */
    public void start(){
        stopServer();
        running=true;
        serverThread=new ServerThread();
        serverThread.start();
    }
    
    public boolean isRunning(){return running;}
    
    /**
     * Stops the server and waits for the server thread to die before returning.
     */
    public void stopServer(){
        if(serverThread==null) return;
        boolean oldPrint=printExceptions;
        printExceptions=false;
        running=false;
        try{
            while(serverThread.isAlive()){
                if(!serverSocket.isClosed()) serverSocket.close();
                try{
                    serverThread.join();
                }catch(InterruptedException ie){}
            }
        }catch(IOException ioe){}
        printExceptions=oldPrint;
        serverThread=null;
    }
    
    
    private class ServerThread extends Thread {
        @Override
        public void run() {
            try{
                if(!useSSL){
                    serverSocket=new ServerSocket(port);
                }
                else{
                    serverSocket=SSLServerSocketFactory.getDefault().createServerSocket(port);
                }

                while(running){
                    try{
                        final Socket s=serverSocket.accept();
                        Thread t=new Thread(){
                            @Override
                            public void run(){
                                try{
                                    handleRequest(s);
                                }catch(Exception e){
                                    if(printExceptions) e.printStackTrace();
                                }
                            }
                        };
                        t.start();
                    }
                    catch(Exception e){
                        if(printExceptions) e.printStackTrace();
                    }
                }
                if(!serverSocket.isClosed()) serverSocket.close();
            }catch(Exception e){
                if(printExceptions) e.printStackTrace();
            }
            serverSocket=null;
        }
    }    
    
}
