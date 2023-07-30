/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * ScreenShotServer.java
 *
 * Created on 27. lokakuuta 2004, 17:35
 */

package org.wandora.utils;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLServerSocketFactory;
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
public class ScreenShotServer extends Thread {
    
    private int port;
    private boolean running;
    private Rectangle screenRect;
    private boolean printExceptions;
    private String format;
    private boolean useSSL;
    private String requiredCredentials;
    
    /** Creates a new instance of ScreenShotServer */
    public ScreenShotServer(int port,int x,int y,int w,int h) {
        this(port,new Rectangle(x,y,w,h));
    }
    public ScreenShotServer(int port,Rectangle screenRect) {
        this.port=port;
        this.screenRect=screenRect;
        printExceptions=true;
        format="jpeg";
        useSSL=false;
    }
    
    public static void main(String[] args) throws Exception {
        DisplayMode dm=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        int port=8899;
        int x=0;
        int y=0;
        int w=dm.getWidth();
        int h=dm.getHeight();
        if(args.length>0) port=Integer.parseInt(args[0]);
        if(args.length>4){
            x=Integer.parseInt(args[1]);
            y=Integer.parseInt(args[2]);
            w=Integer.parseInt(args[3]);
            h=Integer.parseInt(args[4]);
        }
        ScreenShotServer sss=new ScreenShotServer(port,new Rectangle(x,y,w,h));
        sss.setRequiredCredentials("admin:n1mda");
        sss.start();
        System.out.println("ScreenShotServer started at port "+sss.port);
    }
    
    public void setPrintExceptions(boolean value){
        printExceptions=value;
    }
    
    public void setRequiredCredentials(String credentials){
        requiredCredentials=credentials;
    }
    public void setUseSSL(boolean value){
        useSSL=value;
    }
    
    public void setScreenRect(Rectangle rect){
        screenRect=rect;
    }
    public Rectangle getScreenRect(){
        return screenRect;
    }
    public String getImageFormat(){
        return format;
    }
    /**
     * The value entered is used as format for ImageIO.write and in returned content type as "image/"+f.
     * Possible values will probably include at least "jpeg" and "png". Full list depends on image formats
     * supported by ImageIO.
     */
    public void setImageFormat(String f){
        format=f;
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
    public void run() {
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
                    Thread t=new Thread(){
                        @Override
                        public void run(){
                            try{
                                String credentials=null;
                                String get=null;
                                Rectangle rect=screenRect;
                                BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
                                String request=in.readLine();
                                while(request.trim().length()>0){
//                                    System.out.println(request);
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
                                
                                if(requiredCredentials!=null){
                                    boolean ok=false;
                                    if(credentials!=null){
                                        byte[] bs=Base64.decode(credentials);
                                        String gotc=new String(bs);
                                        ok=requiredCredentials.equals(gotc);
                                    }
                                    if(!ok){
                                        OutputStream out=s.getOutputStream();
                                        out.write( "HTTP/1.0 401 Authorization Required\nWWW-Authenticate: Basic realm=\"abc\"\n".getBytes() );
                                        s.close();
                                        return;
                                    }
                                }
                                
                                
                                if( get!=null ){
                                    String f=get.trim();
                                    if(f.startsWith("/")) f=f.substring(1);
                                    if(f.length()>0){
                                        try{
                                            StringTokenizer st2=new StringTokenizer(f, ",");
                                            int x=screenRect.x+Integer.parseInt(st2.nextToken());
                                            int y=screenRect.y+Integer.parseInt(st2.nextToken());
                                            int w=Integer.parseInt(st2.nextToken());
                                            int h=Integer.parseInt(st2.nextToken());
                                            if(x<screenRect.x) x=screenRect.x;
                                            if(x>=screenRect.x+screenRect.width) x=screenRect.x+screenRect.width-1;
                                            if(y<screenRect.y) y=screenRect.y;
                                            if(y>=screenRect.y+screenRect.height) y=screenRect.y+screenRect.height-1;
                                            if(x+w>screenRect.x+screenRect.width) w=screenRect.x+screenRect.width-x;
                                            if(y+h>screenRect.y+screenRect.height) h=screenRect.y+screenRect.height-y;
                                            rect=new Rectangle(x,y,w,h);
                                        }catch(Exception e){
                                            // probably a parse exception or a null pointer exception as a result of st2.nextToken returning null
                                            // both are caused by bad requests
                                            OutputStream out=s.getOutputStream();
                                            out.write( "HTTP/1.0 400 Bad Request\n\nBad Request.".getBytes() );
                                            s.close();
                                            return;
                                        }
                                    }
                                    Robot r=new Robot();
                                    BufferedImage img=r.createScreenCapture(rect);
                                    OutputStream out=s.getOutputStream();
                                    out.write( "HTTP/1.0 200 OK\n".getBytes() );
                                    out.write( ("Content-Type: image/"+format+"\n\n").getBytes() );
                                    ImageIO.write(img,format,out);
                                    s.close();
                                }
                                else{
                                    OutputStream out=s.getOutputStream();
                                    out.write( "HTTP/1.0 400 Bad Request\n\nBad Request.".getBytes() );
                                    s.close();
                                }
                            }catch(Exception e){
                                if(printExceptions) e.printStackTrace();
                            }
                        }
                    };
                    t.start();
                }catch(Exception e){
                    if(printExceptions) e.printStackTrace();
                }
            }
        }catch(Exception e){
            if(printExceptions) e.printStackTrace();
        }
    }
    
}
