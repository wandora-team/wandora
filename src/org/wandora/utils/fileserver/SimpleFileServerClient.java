/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * SimpleFileServerClient.java
 *
 * Created on 25. heinäkuuta 2006, 12:27
 *
 */

package org.wandora.utils.fileserver;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;

/**
 *
 * You can use a SimpleFileServer with this class.  Make a new instance of this
 * class, then use connect method to get a socket and connect to a file server.
 * After that you can use methods to upload files.
 *
 * Note: not all file server methods are currently implemented.
 *
 * @author olli
 */
public class SimpleFileServerClient {
    
    private String serverResponse=null;
    
    /** Creates a new instance of SimpleFileServerClient */
    public SimpleFileServerClient() {
    }
    
    public String getLastServerResponse(){return serverResponse;}
    
    public Socket connect(String host,int port,boolean useSSL) throws IOException {
        Socket s=null;
        if(!useSSL){
            s=new Socket(host,port);
        }
        else{
            s=SSLSocketFactory.getDefault().createSocket(host,port);
        }
        return s;
    }
    
    public String readLine(InputStream in) throws IOException {
        return SimpleFileServer.readLine(in);
    }
    
    public boolean login(InputStream in,Writer out,String user,String pass) throws IOException {
        if(user!=null) out.write("login "+user+":"+pass+"\n");
        else out.write("login\n");
        out.flush();
        serverResponse=readLine(in);
        if(serverResponse.startsWith("OK")) return true;
        else return false;
    }
    
    public void logout(Writer out) throws IOException {
        out.write("logout\n");
        out.flush();
    }
    
    public boolean sendFile(InputStream in,Writer out,OutputStream outStream,String filename,File f) throws IOException {
        return sendFile(in,out,outStream,filename,f.length(),new FileInputStream(f));
    }
    public boolean sendFile(InputStream in,Writer out,OutputStream outStream,String filename,InputStream f) throws IOException {
        byte[] buf=new byte[32768];
        int pos=0;
        int read=0;
        
        while( (read=f.read(buf,pos,buf.length-pos))!=-1 ){
            pos+=read;
            if(pos==buf.length){
                byte[] newbuf=new byte[buf.length*2];
                System.arraycopy(buf,0,newbuf,0,buf.length);
                buf=newbuf;
            }
        }
        return sendFile(in,out,outStream,filename,pos,new ByteArrayInputStream(buf,0,pos));
    }
    
    public boolean sendFile(InputStream in,Writer out,OutputStream outStream,String filename,long length,InputStream f) throws IOException {
        out.write("put "+filename+" "+length+"\n");
        out.flush();
        serverResponse=readLine(in);
        if(!serverResponse.startsWith("OK")) return false;
        byte[] buf=new byte[4096];
        int read=0;
        while( (read=f.read(buf))!=-1 ){
            outStream.write(buf,0,read);
        }
        outStream.flush();
        serverResponse=readLine(in);
        if(!serverResponse.startsWith("OK")) return false;
        else return true;
    }
    
    public String getURLFor(InputStream in,Writer out,String file) throws IOException {
        out.write("geturlfor "+file+"\n");
        out.flush();
        serverResponse=readLine(in);
        if(!serverResponse.startsWith("OK")) return null;
        String res=readLine(in);
        if(res.equals("null")) return "";
        else return res;
    }
    
    public static int FILE_EXISTS=0;
    public static int FILE_NOTEXISTS=1;
    public static int INVALIDFILE=2;
    public int fileExists(InputStream in,Writer out,String file) throws IOException {
        out.write("fileexists "+file+"\n");
        out.flush();
        serverResponse=readLine(in);
        if(!serverResponse.startsWith("OK")) return INVALIDFILE;
        String res=readLine(in);
        if(res.equalsIgnoreCase("true")) return FILE_EXISTS;
        else return FILE_NOTEXISTS;
    }
    

}
