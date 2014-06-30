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
 * PictureList.java
 *
 * Created on July 28, 2004, 11:42 AM
 */

package org.wandora.piccolo.actions;
import org.wandora.utils.ReaderWriterLock;
import org.wandora.utils.XMLParamAware;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.Template;
import org.wandora.piccolo.Application;
import org.wandora.piccolo.SimpleLogger;
import org.wandora.piccolo.User;
import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;

/**
 *
 * @author  olli
 */
public class PictureList implements Action,XMLParamAware {
    
    private Logger logger;
    private List recentList;
    
    private Vector types;
    private boolean join;
    private TopicFilter topicFilter;
    
//    private String typeSI;
//    private String dataTypeSI;
    
    private String templateKey;
    
    private int listcount;
    
    private long listEditTime;
    
    private ReaderWriterLock listLock;
    
    /** Creates a new instance of PictureList */
    public PictureList() {
        recentList=new ArrayList();
        listEditTime=-1;
        listLock=new ReaderWriterLock();
        templateKey="picturelist";
    }
    
    public long getEditTime(TopicMap tm) throws TopicMapException {
        long time=0;
        for(int i=0;i<types.size();i++){
            Object[] typedef=(Object[])types.elementAt(i);
            String typeSI=(String)typedef[0];
            Topic type=tm.getTopic(typeSI);
            long t=type.getDependentEditTime();
            if(t>time) time=t;
        }
        return time;
    }
    
    public synchronized void updateList(WandoraManager manager) throws TopicMapException {
        if(listLock.getWriterLock()){
            try{
                if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
                    try{
                        TopicMap tm=manager.getTopicMap();
                        long edittime=getEditTime(tm);
                        if(edittime==listEditTime) return;
                        recentList.clear();
                        Vector joinTemp=new Vector();
                        for(int i=0;i<types.size();i++){
                            Object[] typedef=(Object[])types.elementAt(i);
                            String typeSI=(String)typedef[0];
                            String dataTypeSI=(String)typedef[1];
                            int listcount=((Integer)typedef[2]).intValue();
                            
                            Topic type=tm.getTopic(typeSI);

                            logger.writelog("DBG","PictureList rebuilding list");
                            if(type==null){
                                logger.writelog("WRN","Trying to update recent image list but type topic not found.");
                                return;
                            }
                            Topic dataType=tm.getTopic(dataTypeSI);
                            if(dataType==null){
                                logger.writelog("WRN","Trying to update recent image list but data type topic not found.");
                                return;
                            }
                            Topic langIndep=tm.getTopic(WandoraManager.LANGINDEPENDENT_SI);
                            Collection c=tm.getTopicsOfType(type);
                            c=TMBox.sortTopicsByData(c, dataType,null,true);
                            
                            Iterator iter=c.iterator();
                            int counter=0;
                            while(iter.hasNext() && counter<listcount){
                                Topic t=(Topic)iter.next();
                                if(topicFilter!=null && !topicFilter.topicVisible(t)) continue;
                                if(!join) recentList.add(t);
                                else joinTemp.add(new JoinHolder(t.getData(dataType,langIndep),t));
                                counter++;
                            }
                        }
                        if(join){
                            Collections.sort(joinTemp);
                            int count=((Integer)((Object[])types.elementAt(0))[2]).intValue();
                            for(int i=0;i<joinTemp.size() && i<count;i++){
                                recentList.add(((JoinHolder)joinTemp.elementAt(i)).topic);
                            }
                        }
                        listEditTime=edittime;
                    }finally{
                        manager.releaseTopicMap(WandoraManager.LOCK_READ);
                    }
                }
            } finally{
                listLock.releaseWriterLock();
            }
        }
    }
    
    private static class JoinHolder implements Comparable<JoinHolder> {
        public String data;
        public Topic topic;
        public JoinHolder(String data,Topic topic){
            this.data=data;
            if(this.data==null) this.data="";
            this.topic=topic;
        }
        public int compareTo(JoinHolder o){
            return o.data.compareTo(data); // note descending sort
        }
    }
    
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application)  {
        WandoraManager manager=(WandoraManager)application.getService("WandoraManager");
        PageCacheService cache=(PageCacheService)application.getService("PageCacheService");
        
        Template template=application.getTemplate(templateKey,user);
        long editTime=-1;
        try{editTime=getEditTime(manager.getTopicMap());}catch(TopicMapException tme){logger.writelog("ERR","TopicMapException",tme);}
//        logger.writelog("DBG","Edittime is "+editTime+"; "+types.size()+" typedefs");
        InputStream page=null;
        String cacheKey=template.getKey()+";;"+template.getVersion()+";;"+user.getProperty(User.KEY_LANG);
        if(cache!=null){
             page=cache.getPage(cacheKey,editTime);
        }
        if(page==null){
            if(editTime!=listEditTime){
                synchronized(this){
                    try{updateList(manager);}catch(TopicMapException tme){logger.writelog("ERR","TopicMapException",tme);}                    
                }
            }
            if(listLock.getReaderLock()){
                try{
                    if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
                        try{
                            HashMap context=new HashMap();
                            context.put("request",request);
                            context.put("topics", recentList);
                            context.putAll(application.getDefaultContext(user));
                            if(cache!=null){
                                OutputStream out=cache.storePage(cacheKey, listEditTime);
                                try{
                                    template.process(context,out);
                                }finally{
                                    try{
                                        out.close();
                                    }catch(Exception e){}
                                }
                                page=cache.getPage(cacheKey, listEditTime);
                            }
                            else{
                                response.setContentType(template.getMimeType());
                                response.setCharacterEncoding(template.getEncoding());
                                try{
                                    template.process(context,response.getOutputStream());
                                }catch(IOException ioe){
                                    logger.writelog("WRN","PictureList couldn't generate page. IOException "+ioe.getMessage());
                                }
                                return;
                            }            
                        }
                        finally{
                            manager.releaseTopicMap(WandoraManager.LOCK_READ);
                        }
                    }
                }finally{
                    listLock.releaseReaderLock();
                }
            }
            else{
                logger.writelog("WRN","PictureList couldn't lock list.");
            }
        }
        if(page!=null){
            try{
                response.setContentType(template.getMimeType());
                response.setCharacterEncoding(template.getEncoding());
                OutputStream out=response.getOutputStream();
                byte[] buf=new byte[4096];
                int r=0;
                while( (r=page.read(buf))!=-1 ){
                    out.write(buf,0,r);
                }
                out.flush();
            }catch(java.io.IOException ioe){
                logger.writelog("WRN","PictureList couldn't generate page. IOException "+ioe.getMessage());
                return;
            }
            finally{
                try{
                    page.close();                
                }catch(IOException e){}
            }
            return;
        }
        
        
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        NodeList nl=element.getChildNodes();
        String imageTypeSI=null;
        String imageDataTypeSI=null;
        int imageCount=10;
        join=false;
        types=new Vector();
        topicFilter=null;
        try{
            for(int i=0;i<nl.getLength();i++){
                Node n=nl.item(i);
                if(n instanceof Element){
                    Element e=(Element)n;
                    if(e.getNodeName().equals("imagetype")){
                        imageTypeSI=processor.createObject(e).toString();
                    }
                    else if(e.getNodeName().equals("datatype")){
                        imageDataTypeSI=processor.createObject(e).toString();
                    }
                    else if(e.getNodeName().equals("imagecount")){
                        imageCount=Integer.parseInt(processor.createObject(e).toString());
                    }
                    else if(e.getNodeName().equals("join")){
                        if(processor.createObject(e).toString().equalsIgnoreCase("true")) join=true;
                        else join=false;
                    }
                    else if(e.getNodeName().equals("filter")){
                        topicFilter=(TopicFilter)processor.createObject(e);
                    }
                    else if(e.getNodeName().equals("typedef")){
                        NodeList nl2=e.getChildNodes();
                        String typeSI=null;
                        String dataTypeSI=null;
                        int count=10;
                        for(int j=0;j<nl2.getLength();j++){
                            Node n2=nl2.item(j);
                            if(n2 instanceof Element){
                                Element e2=(Element)n2;
                                if(e2.getNodeName().equals("datatype")) dataTypeSI=processor.createObject(e2).toString();
                                else if(e2.getNodeName().equals("count")) count=Integer.parseInt(processor.createObject(e2).toString());
                                else if(e2.getNodeName().equals("type")) typeSI=processor.createObject(e2).toString();
                            }
                        }
                        if(typeSI!=null){
                            types.add(new Object[]{typeSI,dataTypeSI,new Integer(count)});
                        }
                    }
                    else if(e.getNodeName().equals("template")){
                        templateKey=processor.getElementContents(e).trim();
                    }
                }
            }
        }
        catch(Exception e){
            logger.writelog("ERR","Error initializing PictureList", e);
        }
        if(imageTypeSI!=null){
            types.add(new Object[]{imageTypeSI,imageDataTypeSI,new Integer(imageCount)});
        }
    }
    
}
