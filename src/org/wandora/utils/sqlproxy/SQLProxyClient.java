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
 *
 * SQLProxyClient.java
 *
 * Created on 12. lokakuuta 2006, 13:08
 *
 */

package org.wandora.utils.sqlproxy;
import java.sql.*;
import java.util.*;
import java.util.zip.*;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;

import static org.wandora.utils.Tuples.*;

/**
 *
 * @author olli
 */
public class SQLProxyClient {
    
    private boolean useSSL=false;
    private Socket socket;
    private Writer outWriter;
    private PushbackReader inReader;
    private String charset="UTF-8";
    private boolean compress=false;
    private String databaseFlavour="generic";
    
    /** Creates a new instance of SQLProxyClient */
    public SQLProxyClient() {
    }

    /**
     * sqlproxy:hostname[:port][/database][?param1=value1&param2=value2]
     *
     * params:
     *  compress[=true|false] (default false, if param is specified without value, true is assumed)
     *  flavour=mysql|generic (note this is not actually used in any way in SQLProxyClient, only stored for application use)
     *  usessl[=true|false] (default false, if param is specified without value, true is assumed)
     */
    public static SQLProxyClient createProxy(String connectionString,String user,String password) throws IOException,SQLProxyException {
        if(!connectionString.startsWith("sqlproxy:")) return null;
        
        connectionString=connectionString.substring("sqlproxy:".length());
        if(connectionString.startsWith("//")) connectionString=connectionString.substring(2);
        
        int ind=connectionString.indexOf("?");
        String params="";
        String server=connectionString;
        if(ind>=0) {
            params=connectionString.substring(ind+1);
            server=connectionString.substring(0,ind);
        }
        String database="default";
        ind=server.indexOf("/");
        if(ind>=0){
            database=server.substring(ind+1);
            server=server.substring(0,ind);
        }
        int port=SQLProxyServer.defaultPort;
        ind=server.indexOf(":");
        if(ind>=0){
            port=Integer.parseInt(server.substring(ind+1));
            server=server.substring(0,ind);
        }

        String flavour="generic";
        boolean compress=false;
        boolean useSSL=false;
        
        String[] split=params.split("&");
        for(int i=0;i<split.length;i++){
            String key=split[i];
            String value="";
            ind=key.indexOf("=");
            if(ind>=0){
                value=key.substring(ind+1);
                key=key.substring(0,ind);
            }
            try{
                key=java.net.URLDecoder.decode(key,"UTF-8");
                value=java.net.URLDecoder.decode(value,"UTF-8");
            }catch(UnsupportedEncodingException uee){uee.printStackTrace();}

            if(key.equals("compress")){
                if(value.equalsIgnoreCase("false") || value.equals("0") || value.equalsIgnoreCase("no"))
                    compress=false;
                else compress=true;
            }
            else if(key.equals("flavour")){
                flavour=value;
            }
            else if(key.equals("usessl")){
                if(value.equalsIgnoreCase("false") || value.equals("0") || value.equalsIgnoreCase("no"))
                    useSSL=false;
                else useSSL=true;
            }
            else if(key.equals("user")){
                user=value;
            }
            else if(key.equals("password")){
                password=value;
            }
        }

        SQLProxyClient sqlProxy=new SQLProxyClient();
        sqlProxy.setUseSSL(useSSL);
        sqlProxy.setFlavour(flavour);
        sqlProxy.connect(server,port);
        if(compress) sqlProxy.compress();
        if(user!=null && user.length()>0) sqlProxy.login(user,password);
        sqlProxy.open(database);
        
        return sqlProxy;
    }
    
    public String getFlavour(){return databaseFlavour;}
    public void setFlavour(String f){this.databaseFlavour=f;}
    
    public void connect(String host) throws IOException {
        connect(host,SQLProxyServer.defaultPort,this.useSSL);
    }
    public void connect(String host,int port) throws IOException {
        connect(host,port,false);
    }
    public void connect(String host,int port,boolean useSSL) throws IOException {
        if(useSSL){
            socket=SSLSocketFactory.getDefault().createSocket(host,port);
        }
        else{
            socket=SocketFactory.getDefault().createSocket(host,port);
        }
        inReader=new PushbackReader(new InputStreamReader(socket.getInputStream(),charset));
        outWriter=new OutputStreamWriter(socket.getOutputStream(),charset);
    }
    
    public void close() throws IOException {
        socket.close();
    }
    
    public void setUseSSL(boolean b){
        this.useSSL=b;
    }
    
    private String readLine() throws IOException {
        int c=-1;
        boolean escape=false;
        StringBuffer buf=new StringBuffer();
        while( (c=inReader.read())!=-1 ){
            if(!escape && c=='\\') escape=true;
            else {
                if(!escape && c=='\n') break;
                if(!escape && c=='\r') continue;
                else buf.append((char)c);
                escape=false;
            }
        }
        return buf.toString();        
    }
    
    private T2<String,Boolean> readString() throws IOException,SQLProxyException {
        int c=inReader.read();
        if(c=='"'){
            boolean escape=false;
            StringBuffer buf=new StringBuffer();
            while( (c=inReader.read())!=-1 ){
                if(!escape && c=='\\') escape=true;
                else {
                    if(!escape && c=='\"') break;
                    else buf.append((char)c);
                    escape=false;
                }
            }
            while( (c=inReader.read())!=-1 ){
                if(c==',') return t2(buf.toString(),false);
                else if(c=='\n') return t2(buf.toString(),true);
            }
            return t2(buf.toString(),true);
        }
        else if(c=='n'){
            if(inReader.read()=='u' && inReader.read()=='l' && inReader.read()=='l') {
                while( (c=inReader.read())!=-1 ){
                    if(c==',') return t2(null,false);
                    else if(c=='\n') return t2(null,true);
                }
                return t2(null,true);
            }
        }
        throw new SQLProxyException("Unexpected response");
    }
    
    private T2<Integer,Boolean> readInteger() throws IOException,SQLProxyException {
        StringBuffer buf=new StringBuffer();
        boolean eol=false;
        int c=-1;
        while( (c=inReader.read())!=-1 ){
            if(c==',') {
                eol=false;
                break;
            }
            else if(c=='\n'){
                eol=true;
                break;
            }
            else if(c=='\r') continue;
            else buf.append((char)c);
        }
        String s=buf.toString();
        if(s.trim().equals("null")) return t2(null,eol);
        try{
            int i=Integer.parseInt(s);
            return t2(i,eol);
        }
        catch(NumberFormatException nfe){
            throw new SQLProxyException("Number format exception");        
        }
    }
    
    private T2<Double,Boolean> readDouble() throws IOException,SQLProxyException {
        StringBuffer buf=new StringBuffer();
        boolean eol=false;
        int c=-1;
        while( (c=inReader.read())!=-1 ){
            if(c==',') {
                eol=false;
                break;
            }
            else if(c=='\n'){
                eol=true;
                break;
            }
            else if(c=='\r') continue;
            else buf.append((char)c);
        }
        String s=buf.toString();
        if(s.trim().equals("null")) return t2(null,eol);
        try{
            double d=Double.parseDouble(s);
            return t2(d,eol);
        }
        catch(NumberFormatException nfe){
            throw new SQLProxyException("Number format exception");        
        }        
    }
    
    private Collection<Map<String,Object>> readResultSet() throws IOException,SQLProxyException {
        String line=readLine();
        if(!line.equals(SQLProxyServer.RES_RESULTSET)) throw new SQLProxyException("Unexpected return type "+line);
        line=readLine();
        String[] parsed=line.split(",");
        int columnCount=parsed.length/2;
        String[] columnNames=new String[columnCount];
        int[] columnTypes=new int[columnCount];
        for(int i=0;i<parsed.length;i+=2){
            columnNames[i/2]=parsed[i];
            columnTypes[i/2]=Integer.parseInt(parsed[i+1]);
        }
        Vector<Map<String,Object>> result=new Vector<Map<String,Object>>();
        while( true ){
            int c=inReader.read();
            if(c=='\r') continue;
            else if(c=='\n') break;
            else inReader.unread(c);
            Map<String,Object> row=new HashMap<String,Object>();
            for(int i=0;i<columnCount;i++){
                switch(columnTypes[i]){
                    case Types.BIGINT: case Types.BIT: case Types.INTEGER:
                    case Types.SMALLINT: case Types.TINYINT:{
                        T2<Integer,Boolean> r=readInteger();
                        row.put(columnNames[i],r.e1);
                        break;
                    }
                    case Types.DECIMAL: case Types.DOUBLE: case Types.FLOAT:
                    case Types.NUMERIC: case Types.REAL: {
                        T2<Double,Boolean> r=readDouble();
                        row.put(columnNames[i],r.e1);
                        break;
                    }
                    default: {
                        T2<String,Boolean> r=readString();
                        row.put(columnNames[i],r.e1);
                        break;
                    }
                }
            }
            result.add(row);
        }
        return result;
    }
    
    public int readUpdateCount() throws IOException, SQLProxyException {
        String line=readLine();
        if(!line.equals(SQLProxyServer.RES_COUNT)) throw new SQLProxyException("Unexpected return type "+line);
        line=readLine();
        try{
            int i=Integer.parseInt(line);
            readLine();
            return i;
        }catch(NumberFormatException nfe){
            throw new SQLProxyException("Number format exception");
        }
    }
    
    public synchronized void compress() throws IOException,SQLProxyException {
        outWriter.write("compress\n");
        outWriter.flush();
        compress=true;
    }
    
    public synchronized boolean login(String user,String pass) throws IOException {
        outWriter.write("login "+user+":"+pass+"\n");
        outWriter.flush();
        if(compress) inReader=new PushbackReader(new InputStreamReader(new GZIPInputStream(socket.getInputStream()),charset));
        String line=readLine();
        boolean ret=false;
        if(line.equals(SQLProxyServer.RES_OK)) ret=true;
        if(compress) finishCompressedStream();
        return ret;
    }
    
    public synchronized boolean open(String database) throws IOException {
        outWriter.write("open "+database+"\n");
        outWriter.flush();
        if(compress) inReader=new PushbackReader(new InputStreamReader(new GZIPInputStream(socket.getInputStream()),charset));
        String line=readLine();
        boolean ret=false;
        if(line.equals(SQLProxyServer.RES_OK)) ret=true;
        if(compress) finishCompressedStream();
        return ret;
    }
    
    public synchronized void executeCommand(String cmd) throws IOException,SQLProxyException {
        outWriter.write(cmd+"\n");
        outWriter.flush();
    }
    
    private void finishCompressedStream() throws IOException {
        while(inReader.read()!=-1) ;
    }
    
    
    public synchronized Collection<Map<String,Object>> executeQuery(String query) throws IOException,SQLProxyException {
        outWriter.write(query+"\n");
        outWriter.flush();
        if(compress) inReader=new PushbackReader(new InputStreamReader(new GZIPInputStream(socket.getInputStream()),charset));
        Collection<Map<String,Object>> ret=readResultSet();
        if(compress) finishCompressedStream();
        return ret;
    }
    public synchronized int executeUpdate(String update) throws IOException,SQLProxyException {
        System.out.println("Executing proxy update "+update);
        outWriter.write(update+"\n");
        outWriter.flush();
        if(compress) inReader=new PushbackReader(new InputStreamReader(new GZIPInputStream(socket.getInputStream()),charset));
        int ret=readUpdateCount();
        if(compress) finishCompressedStream();
        return ret;
    }
    
    public static void main(String[] args) throws Exception {
        String connectionString="sqlproxy:";
//        String connectionString="sqlproxy:localhost/default?user=test&password=aaa&compress";
        if(args.length>0) connectionString=args[0];
        SQLProxyClient client=SQLProxyClient.createProxy(connectionString,null,null);
        BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String line=reader.readLine();
            if(line.equalsIgnoreCase("quit")) {
                client.executeCommand("quit");
                break;
            }
            Collection<Map<String,Object>> res=client.executeQuery(line);
            for(Map<String,Object> row : res){
                boolean first=true;
                for(Map.Entry<String,Object> e : row.entrySet()){
                    if(!first) System.out.print(", ");
                    first=false;
                    System.out.print(e.getKey()+"=>"+e.getValue());
                }
                System.out.println();
            }
        }
        client.close();
    }
}
