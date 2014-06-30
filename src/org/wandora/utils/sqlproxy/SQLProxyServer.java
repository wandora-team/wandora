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
 * SQLProxyServer.java
 *
 * Created on 12. lokakuuta 2006, 13:08
 *
 */

package org.wandora.utils.sqlproxy;
import java.sql.*;
import java.util.*;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;

import java.util.zip.*;

import static org.wandora.utils.Tuples.*;

/**
 *
 * @author olli
 */
public class SQLProxyServer extends Thread {
    
    private boolean useSSL=false;
    public static final int defaultPort=8891;
    private int port;
    private boolean running=false;
    private boolean printExceptions=true;
    
    private boolean verbose=false;
    private boolean echo=false;
    
    private String credentials=null;
    
    private HashMap<String,T4<String,String,String,String>> connections;
       
    /** Creates a new instance of SQLProxyServer */
    public SQLProxyServer(String dbDriver,String dbConnectionString,String dbUser,String dbPassword, String proxyUser, String proxyPassword) {
        this(dbDriver,dbConnectionString,dbUser,dbPassword,defaultPort);
        setCredentials(proxyUser + ":" + proxyPassword);
    }
    
    public SQLProxyServer(String dbDriver,String dbConnectionString,String dbUser,String dbPassword) {
        this(dbDriver,dbConnectionString,dbUser,dbPassword,defaultPort);
    }
    public SQLProxyServer(String dbConnectionString,String dbUser,String dbPassword) {
        this(null,dbConnectionString,dbUser,dbPassword,defaultPort);
    }
    public SQLProxyServer(String dbDriver,String dbConnectionString,String dbUser,String dbPassword,int port) {
        this(port);
        addConnection("default",dbDriver,dbConnectionString,dbUser,dbPassword);
    }
    public SQLProxyServer(String dbDriver,String dbConnectionString,String dbUser,String dbPassword,int port, String proxyUser, String proxyPassword) {
        this(port);
        setCredentials(proxyUser + ":" + proxyPassword);
        addConnection("default",dbDriver,dbConnectionString,dbUser,dbPassword);
    }
    public SQLProxyServer(String dbDriver,String dbConnectionString,String dbUser,String dbPassword,Integer port, String proxyUser, String proxyPassword) {
        this(port.intValue());
        setCredentials(proxyUser + ":" + proxyPassword);
        addConnection("default",dbDriver,dbConnectionString,dbUser,dbPassword);
    }
    public SQLProxyServer() {
        this(defaultPort);
    }
    public SQLProxyServer(int port) {
        connections=new HashMap<String,T4<String,String,String,String>>();
        credentials="test:aaa";
        this.port=port;
    }
    
    
    /**
     * Credentials should be "username:password".
     */
    public void setCredentials(String credentials){
        this.credentials=credentials;
    }
    
    public void addConnection(String key,String dbDriver,String dbConnectionString,String dbUser,String dbPassword){
        if(dbDriver==null) dbDriver=guessDBDriver(dbConnectionString);
        connections.put(key,t4(dbDriver,dbConnectionString,dbUser,dbPassword));
    }
    
    public static String guessDBDriver(String dbConnectionString){
        if(dbConnectionString.startsWith("jdbc:mysql:")) return "com.mysql.jdbc.Driver";
        else if(dbConnectionString.startsWith("jdbc:microsoft:sqlserver:")) return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        else if(dbConnectionString.startsWith("jdbc:hsqldb:")) return "org.hsqldb.jdbcDriver";
        else return null;
    }
    
    public void setPort(int port){
        this.port=port;
    }
    
    public int getPort(){return port;}
    
    public void setVerbose(boolean b){verbose=b;}
    
    public void setEcho(boolean b){echo=b;}
    
    public static void printUsage(){
        System.out.println("Usage: java com.gripstudios.utils.sqlproxy.SQLProxyServer [driver] connectionString user password");
    }
    
    public static void main(String[] args){
        SQLProxyServer server=null;
        if(args.length>4) server=new SQLProxyServer(args[0],args[1],args[2],args[3],Integer.parseInt(args[4]));
        else if(args.length>3) server=new SQLProxyServer(args[0],args[1],args[2],args[3]);
        else if(args.length>2) server=new SQLProxyServer(args[0],args[1],args[2]);
        else {
            printUsage();
            return;
        };
        server.setVerbose(true);
//        server.setEcho(true);
        server.start();
        try{
            server.join();
        }catch(InterruptedException ie){
            ie.printStackTrace();
            return;
        }
    }
    
    public Connection createConnection(String key){
        T4<String,String,String,String> params=connections.get(key);
        if(params==null) return null;
        String dbDriver=params.e1;
        String dbConnectionString=params.e2;
        String dbUser=params.e3;
        String dbPassword=params.e4;
        try{
            if(dbDriver!=null) Class.forName(dbDriver);
            Connection con=DriverManager.getConnection(dbConnectionString,dbUser,dbPassword);        
            return con;
        }
        catch(Exception e){
            System.out.println("Database connection failed with");
            System.out.println("Driver: " + dbDriver);
            System.out.println("Connection string: " + dbConnectionString);
            System.out.println("User: " + dbUser);
            System.out.println("Password: " + dbPassword);
            e.printStackTrace();
            return null;
        }
    }
    
    public void run() {
        running=true;
        if(verbose) System.out.println("Listening to port "+port);
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
                    if(verbose) System.out.println("Accepted connection from "+s.getRemoteSocketAddress().toString());
                    ServerThread t=new ServerThread(s);
                    t.start();
                }catch(Exception e){
                    if(printExceptions) e.printStackTrace();
                }
            }
        }catch(Exception e){
            if(printExceptions) e.printStackTrace();
        }
    }
    
    public static final String RES_RESULTSET="0"; // query result is a result set
    public static final String RES_COUNT="1"; // query result is an update count
    public static final String RES_OK="2"; // command succesfull, no return value
    public static final String RES_ERROR="3"; // invalid command
    public static final String RES_AUTHREQUIRED="4"; // command requires authentication or invalid credentials when authenticating
    public static final String RES_CONREQUIRED="5"; // command requires an open database connection or invaling connection key when opening connection
    
    private class ServerThread extends Thread {
        private Connection connection;
        private Statement stmt;
        private Socket socket;
        private boolean running=true;
        private boolean sendExceptions=false;
        private Reader inReader;
        private Writer outWriter;
        private OutputStream out;
        private String lf="\r\n";
        private String charset="UTF-8";
        private boolean compress=false;
        private boolean authenticated=false;
        
        
        public ServerThread(Socket s) throws IOException,SQLException  {
            socket=s;
            InputStream inStream=socket.getInputStream();
            out=socket.getOutputStream();
            if(echo) inStream=new EchoInputStream(inStream,out);
            inReader=new InputStreamReader(inStream,charset);
            outWriter=new OutputStreamWriter(out,charset);
        }
        
        public boolean openConnection(String key) throws SQLException {
            connection=createConnection(key);
            if(connection==null) return false;
            stmt=connection.createStatement();
            return true;
        }
        
        public String readQuery() throws IOException {
            int c=-1;
            boolean escape=false;
            StringBuffer buf=new StringBuffer();
            while( (c=inReader.read())!=-1 ){
                if(!escape && c=='\\') escape=true;
                else {
                    if(!escape && c=='\n') break;
                    if(!escape && c=='\r') continue;
                    if(!escape && c==8){ // backspace
                        if(buf.length()>0) buf.deleteCharAt(buf.length()-1);
                    }
                    else buf.append((char)c);
                    escape=false;
                }
            }
            return buf.toString();
        }
        
        public void writeString(String s) throws IOException {
            if(s==null) outWriter.write("null");
            else{
                s=s.replace("\\","\\\\");
                s=s.replace("\"","\\\"");
                outWriter.write("\""+s+"\"");
            }
        }       
        
        public boolean isTypeNumeric(int type){
            switch(type){
                case Types.BIGINT: case Types.BOOLEAN:
                case Types.DECIMAL: case Types.DOUBLE: case Types.FLOAT:
                case Types.INTEGER: case Types.NUMERIC: case Types.REAL:
                case Types.SMALLINT: case Types.TINYINT:
                    return true;
                default: return false;
            }
        }
        
        public void handleException(Throwable e) throws IOException {
            e.printStackTrace();
            outWriter.flush();
            PrintWriter writer=new PrintWriter(outWriter);
            if(sendExceptions) e.printStackTrace(writer);
            writer.flush();
        }
        
        public void sendResponse(String response) throws IOException {
            if(compress){
                out=new GZIPOutputStream(socket.getOutputStream());
                outWriter=new OutputStreamWriter(out);                                
            }
            outWriter.write(response);
            outWriter.flush();
            if(compress) ((GZIPOutputStream)out).finish();            
        }
        
        public void run(){
            if(credentials==null) authenticated=true;
            while(running){
                try{
                    try{
                        String query=readQuery().trim();
                        if(query.equals("quit")) break;
                        else if(query.equals("echo")){
                            InputStream inStream=socket.getInputStream();
                            out=socket.getOutputStream();
                            inStream=new EchoInputStream(inStream,out);
                            inReader=new InputStreamReader(inStream,charset);
                        }
                        else if(query.equals("noecho")){
                            InputStream inStream=socket.getInputStream();
                            inReader=new InputStreamReader(inStream,charset);
                        }
                        else if(query.equals("exceptions")){
                            sendExceptions=true;
                        }
                        else if(query.equals("noexceptions")){
                            sendExceptions=false;
                        }
                        else if(query.equals("human")){
                            InputStream inStream=socket.getInputStream();
                            out=socket.getOutputStream();
                            inStream=new EchoInputStream(inStream,out);
                            inReader=new InputStreamReader(inStream,charset);
                            sendExceptions=true;
                        }
                        else if(query.equals("compress")){
                            compress=true;
                        }
                        else if(query.startsWith("login")){
                            if(authenticated){
                                sendResponse(RES_OK+lf);
                            }
                            else{
                                int ind=query.indexOf(" ");
                                if(ind>0){
                                    String parsed=query.substring(ind+1).trim();
                                    if(parsed.equals(credentials)) {
                                        authenticated=true;
                                        sendResponse(RES_OK+lf);
                                    }
                                    else sendResponse(RES_AUTHREQUIRED+lf);
                                }
                                else sendResponse(RES_ERROR+lf);
                            }
                        }
                        else if(authenticated) {
                            if(query.startsWith("open")){
                                int ind=query.indexOf(" ");
                                if(ind>0){
                                    String parsed=query.substring(ind+1).trim();
                                    if(openConnection(parsed)) sendResponse(RES_OK+lf);
                                    else sendResponse(RES_CONREQUIRED+lf);
                                }
                                else sendResponse(RES_ERROR+lf);                            
                            }
                            else if(connection==null){
                                sendResponse(RES_CONREQUIRED+lf);
                                continue;
                            }
                            else if(stmt.execute(query)){
                                if(compress){
                                    out=new GZIPOutputStream(socket.getOutputStream());
                                    outWriter=new OutputStreamWriter(out,charset);                                
                                }
                                outWriter.write(RES_RESULTSET+lf);
                                ResultSet rs=stmt.getResultSet();

                                ResultSetMetaData rsmd=rs.getMetaData();
                                StringBuffer metaData=new StringBuffer();
                                int columnCount=rsmd.getColumnCount();
                                int[] types=new int[columnCount];
                                for(int i=0;i<columnCount;i++){
                                    if(i>0) metaData.append(",");
                                    String name=rsmd.getColumnName(i+1);
                                    int type=rsmd.getColumnType(i+1);
                                    types[i]=type;
                                    name=name.replace(",","\\,");
                                    metaData.append(name+","+type);
                                }
                                outWriter.write(metaData.toString()+lf);

                                while(rs.next()){
                                    for(int i=0;i<columnCount;i++){
                                        if(i>0) outWriter.write(",");
                                        if(isTypeNumeric(types[i])) outWriter.write(""+rs.getString(i+1));
                                        else writeString(rs.getString(i+1));
                                    }
                                    outWriter.write(lf);
                                }
                                outWriter.write(lf);

                                outWriter.flush();
                                if(compress) ((GZIPOutputStream)out).finish();

                                rs.close();
                            }
                            else{
                                int count=stmt.getUpdateCount();
                                sendResponse(RES_COUNT+lf+count+lf);
                            }
                        }
                        else{
                            sendResponse(RES_AUTHREQUIRED+lf);
                        }
                    }
                    catch(SQLException sqle){
                        if(compress){
                            out=new GZIPOutputStream(socket.getOutputStream());
                            outWriter=new OutputStreamWriter(out);                                
                        }
                        outWriter.write(RES_ERROR+lf);
                        handleException(sqle);
                        if(compress) ((GZIPOutputStream)out).finish();
                    }
                }
                catch(IOException ioe){
                    if(!ioe.getMessage().equals("Connection reset")){
                        ioe.printStackTrace();
                    }
                    running=false;
                }
            }
            if(verbose) System.out.println("Closing connection from "+socket.getRemoteSocketAddress().toString());
            dispose();
        }
        
        public void dispose(){
            try{
                socket.close();
            }
            catch(IOException ioe){
                ioe.printStackTrace();
            }
            try{
                if(stmt!=null) stmt.close();
                if(connection!=null) connection.close();
            }
            catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }
        
    }
    
    public static class EchoInputStream extends InputStream {
        private InputStream in;
        private OutputStream out;
        
        public EchoInputStream(InputStream in,OutputStream out){
            this.in=in;
            this.out=out;
        }
        
        public int available() throws IOException {return in.available();}
        public void close() throws IOException {in.close();}
        public void mark(int readlimit){in.mark(readlimit);}
        public boolean markSupported(){return in.markSupported();}
        public int read() throws IOException {
            int r=in.read();
            out.write(r);
            return r;
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
        public void reset() throws IOException {in.reset();}
        public long skip(long n) throws IOException {return in.skip(n);}
    }
}
