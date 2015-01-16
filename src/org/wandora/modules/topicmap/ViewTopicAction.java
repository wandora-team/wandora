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
package org.wandora.modules.topicmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.CachedAction;
import org.wandora.modules.servlet.GenericTemplateAction;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.servlet.Template;
import org.wandora.topicmap.*;

/**
 * <p>
 * An action for displaying something about a single topic using 
 * a template. This extends GenericTemplateAction and adds some features
 * related to topics. For the most part, however, it works exactly like
 * GenericTemplateAction.
 * </p>
 * <p>
 * The topic this action uses can be specified in the HTTP request. The request
 * parameter used for this is topic, but it can be changed using the initialisation
 * parameter topicRequestKey. The request parameter value can be a number of things.
 * It can be prefixed with one of "si:", "sl:" or "bn:" standing for subject
 * identifier, subject locator and base name, respectively. The prefix is then
 * followed by the value of the chosen identifying field. If you do not use any
 * prefix, a topic is looked for trying all the identifying fields in that order
 * until a topic is found. That is, the value is first assumed to be a subject
 * identifier, then a subject locator and finally a base name. If no suitable
 * topic is found, whether a prefix is used or not, the action will fail.
 * </p>
 * <p>
 * You may set the initialisation parameter noTopic to true to disable automatic
 * topic resolution. In this case you should resolve the topic in your template.
 * You will also need to forward the relevant request parameter to the template,
 * which is best done using GenericTemplateAction's forwardRequestParameters.
 * </p>
 * <p>
 * Unless noTopic is set to true, the topic will be automatically added to the
 * template context. By default this is done using template parameter name
 * "topic", but this can be changed using the initialisation parameter topicContextKey.
 * Similarly the topic map is added to the context using parameter name "topicmap"
 * and this can be changed with the initialisation parameter topicMapContextKey.
 * The exact value of the HTTP request parameter is also added in the context with
 * parameter name "topicRequest". This will include the possible prefix of the
 * value, as described above.
 * </p>
 * <p>
 * You can set the default topic and default language using initialisation 
 * parameters defaultTopic and defaultLang, respectively. If either is left
 * undefined in the HTTP request, the defaults will be used instead.
 * </p>
 * <p>
 * Note that the getTopic and getTopics methods are static and can, and should,
 * be used in other actions which need to resolve topics based on request
 * parameters.
 * </p>
 * 
 * @author olli
 */

public class ViewTopicAction extends GenericTemplateAction {

    protected String topicRequestKey="topic";
    
    protected String topicMapContextKey="topicmap";
    protected String topicContextKey="topic";
    
    protected boolean noTopic=false;
    
    protected String defaultLang="en";
    protected String defaultTopic="http://wandora.org/si/core/wandora-class";
    
    protected TopicMapManager tmManager;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(this,TopicMapManager.class, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        Object o;
        o=settings.get("defaultLang");
        if(o!=null) defaultLang=o.toString().trim();
        
        o=settings.get("defaultTopic");
        if(o!=null) defaultTopic=o.toString().trim();
        
        o=settings.get("noTopic");
        if(o!=null) noTopic=Boolean.parseBoolean(o.toString());
        
        o=settings.get("topicMapContextKey");
        if(o!=null) topicMapContextKey=o.toString().trim();

        o=settings.get("topicContextKey");
        if(o!=null) topicContextKey=o.toString().trim();
        
        o=settings.get("topicRequestKey");
        if(o!=null) topicRequestKey=o.toString().trim();
    }

    
    
    @Override
    public void start(ModuleManager manager) throws ModuleException {
        tmManager=manager.findModule(this,TopicMapManager.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        tmManager=null;
        super.stop(manager);
    }
    
    
    @Override
    protected LinkedHashMap<String,String> getCacheKeyParams(HttpServletRequest req, HttpMethod method, String action) {
        LinkedHashMap<String,String> params=super.getCacheKeyParams(req, method, action);
        
        String lang=req.getParameter("lang");
        if(lang==null || lang.length()==0) lang=defaultLang;
        
        params.put("lang",lang);
        
        if(!noTopic){
            String topic=req.getParameter(topicRequestKey);
            if(topic==null || topic.length()==0) topic=defaultTopic;
            params.put("topicRequest",topic);
        }
                        
        return params;
    }
    
    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action) {
        return CachedAction.buildCacheKey(getCacheKeyParams(req, method, action));
    }

    /**
     * <p>
     * Returns topics based on a simple query. Currently this will only ever
     * return a single topic at most, in the future more supported query types
     * might be added which could return more topics.
     * </p>
     * <p>
     * At the moment, the query can be prefixed with one of "si:", "sl:" or "bn:"
     * which stand for subject identifier, subject locator and base name,
     * respectively. After the prefix, add the value of the chosen identifying
     * field. That topic will then be returned, or an empty collection if it doesn't
     * exist. If you don't use any prefix, the value is first assumed to be a
     * subject identifier, if that doesn't correspond to an existing topic, then
     * subject locator is tried, and finally base name. If none work, then an
     * empty collection is returned. Otherwise the first match is returned.
     * </p>
     * @param query The topic query.
     * @param tm The topic map where to execute the query.
     * @param max Maximum number of results to return.
     * @return The topics matching the query.
     * @throws TopicMapException 
     */
    public static ArrayList<Topic> getTopics(String query,TopicMap tm,int max) throws TopicMapException {
        ArrayList<Topic> ret=new ArrayList<Topic>();
        if(query.startsWith("si:")){
            Topic t=tm.getTopic(query.substring(3));
            if(t!=null) ret.add(t);
        }
        else if(query.startsWith("bn:")){
            Topic t=tm.getTopicWithBaseName(query.substring(3));
            if(t!=null) ret.add(t);
        }
        else if(query.startsWith("sl:")){
            Topic t=tm.getTopicBySubjectLocator(query.substring(3));
            if(t!=null) ret.add(t);
        }
        else {
            Topic t=tm.getTopic(query);
            if(t!=null) ret.add(t);
            else {
                t=tm.getTopicBySubjectLocator(query);
                if(t!=null) ret.add(t);
                else {
                    t=tm.getTopicWithBaseName(query);
                    if(t!=null) ret.add(t);
                }
            }
        }        
        return ret;
    }
    
    /**
     * Same as getTopics, but returns only the first result of what
     * getTopics would return. As noted in getTopics documentation,
     * at the moment it will only ever return at most a single topic anyway
     * so there isn't much difference between these two methods yet. This
     * method will return null if no topic is found.
     * 
     * @param query The topic query.
     * @param tm The topic map where to execute the query.
     * @return The first result of the query or null if no topics were found.
     * @throws TopicMapException 
     */
    public static Topic getTopic(String query,TopicMap tm) throws TopicMapException {
        ArrayList<Topic> ts=getTopics(query,tm,1);
        if(ts.isEmpty()) return null;
        else return ts.get(0);
    }

    @Override
    protected HashMap<String, Object> getStaticTemplateContext() {
        HashMap<String,Object> context=super.getStaticTemplateContext();
        context.put("tmbox",new TMBox());
        return context;
    }
    
    
    
    @Override
    protected HashMap<String, Object> getTemplateContext(Template template, HttpServletRequest req, HttpMethod method, String action, org.wandora.modules.usercontrol.User user) throws ActionException {
        HashMap<String, Object> context=super.getTemplateContext(template, req, method, action, user);
        
        context.putAll(getCacheKeyParams(req, method, action));
        
        TopicMap tm=tmManager.getTopicMap();
        if(!noTopic){
            try{
                Topic t=getTopic((String)context.get("topicRequest"),tm);
                if(t==null) return null; // returning null causes this action to abort, probably results in a 404 in the end or something
                context.put(topicContextKey,t);
            }
            catch(TopicMapException tme){
                logging.warn(tme);
                return null;
            }
        }
        context.put(topicMapContextKey,tm);
        
        return context;
    }

    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, org.wandora.modules.usercontrol.User user) throws ServletException, IOException, ActionException {
        if(tmManager.lockRead()){
            try{
                return super.handleAction(req, resp, method, action, user);
            }
            finally{
                tmManager.unlockRead();
            }
        }
        else return false;
    }
 
    
}
