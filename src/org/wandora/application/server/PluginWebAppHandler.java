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
 */
package org.wandora.application.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.utils.Options;
import org.wandora.utils.XMLbox;

import org.wandora.application.tools.browserextractors.*;

import java.io.*;

/**
 *
 * @author olli
 */
public class PluginWebAppHandler implements WebAppHandler {

    private BrowserExtractorManager extractorManager;

    public PluginWebAppHandler(){

    }

    protected PrintWriter startPluginResponse(HttpServletResponse response,int code,String text){
        try{
            PrintWriter writer=new PrintWriter(new OutputStreamWriter(response.getOutputStream(),"UTF-8"));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<wandoraplugin>");
            writer.println("  <resultcode>"+code+"</resultcode>");
            writer.println("  <resulttext>"+text+"</resulttext>");
            return writer;
        } catch(IOException ioe){ioe.printStackTrace(); return null;}
    }

    public boolean getPage(WandoraWebApp webApp, WandoraWebAppServer server, String target, HttpServletRequest request, HttpServletResponse response) {
        if(target.equals("/")){
            String content=request.getParameter("content");
            String page=request.getParameter("page");
            String selectionStart=request.getParameter("selectionStart");
            String selectionEnd=request.getParameter("selectionEnd");
            String selectionText=request.getParameter("selectionText");
            int sStart=-1;
            int sEnd=-1;
            try{
                if(selectionStart!=null && selectionStart.length()>0) sStart=Integer.parseInt(selectionStart);
                if(selectionEnd!=null && selectionEnd.length()>0) sEnd=Integer.parseInt(selectionEnd);
            }
            catch(NumberFormatException nfe){nfe.printStackTrace();}
            String action=request.getParameter("action");
            String app=request.getParameter("application");
            if(action==null || action.length()==0) action="doextract";
            if(action.equalsIgnoreCase("getextractors")){
                response.setContentType("text/xml");
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer=startPluginResponse(response,0,"OK");

                BrowserExtractRequest extractRequest=new BrowserExtractRequest(page, content, null, app,sStart,sEnd,selectionText);

                String[] methods=extractorManager.getExtractionMethods(extractRequest);

                for(int i=0;i<methods.length;i++){
                    writer.println("  <method>"+methods[i]+"</method>");
                }

                writer.println("</wandoraplugin>");
                writer.flush();
                return true;
            }
            else if(action.equalsIgnoreCase("doextract")){
                String method=request.getParameter("method");
                if(method!=null && method.length()>0){
                    String[] methods=method.split(";");
                    String message=null;
                    for(String m : methods){
                        BrowserExtractRequest extractRequest=new BrowserExtractRequest(page, content, m, app,sStart,sEnd,selectionText);
                        message=extractorManager.doPluginExtract(extractRequest);
                        if(message!=null && message.startsWith(BrowserPluginExtractor.RETURN_ERROR)){
                            break;
                        }
                    }
                    response.setContentType("text/xml");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter writer;
                    if(message==null || !message.startsWith(BrowserPluginExtractor.RETURN_ERROR)){
                        writer=startPluginResponse(response,0,"OK");
                        if(message!=null){
                            int ind=message.indexOf(" ");
                            if(ind>0 && ind<10) message=message.substring(ind+1);
                            writer.print("<returnmessage>");
                            writer.print(XMLbox.cleanForXML(message));
                            writer.println("</returnmessage>");
                        }
                    }
                    else{
                        writer=startPluginResponse(response,3,message);
                    }
                    writer.println("</wandoraplugin>");
                    writer.flush();
                    return true;
                }
                else{
                    PrintWriter writer=startPluginResponse(response,2,"No method provided for doextract");
                    writer.println("</wandoraplugin>");
                    writer.flush();
                    return true;
                }
            }
            else{
                response.setContentType("text/xml");
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer=startPluginResponse(response,1,"Invalid action");
                writer.println("</wandoraplugin>");
                writer.flush();
                return true;
            }
        }
        else{
            server.returnNotFound(response);
            return true;
        }
    }

    public ConfigComponent getConfigComponent(WandoraWebApp app, final WandoraWebAppServer server) {
        return null;
    }


    public void init(WandoraWebApp app, WandoraWebAppServer server, Options options) {
        extractorManager=new BrowserExtractorManager(server.getWandora());
    }

    public void save(WandoraWebApp app, WandoraWebAppServer server, Options options) {
    }

    public void start(WandoraWebApp app, WandoraWebAppServer server){
    }
    public void stop(WandoraWebApp app, WandoraWebAppServer server){
    }

}
