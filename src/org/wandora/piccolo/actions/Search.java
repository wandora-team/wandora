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
 * Search.java
 *
 * Created on July 12, 2004, 2:53 PM
 */

package org.wandora.piccolo.actions;
import org.wandora.piccolo.WandoraManager;
import org.wandora.utils.XMLParamAware;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.Template;
import org.wandora.piccolo.Application;
import org.wandora.piccolo.SimpleLogger;
import org.wandora.piccolo.User;
import org.wandora.piccolo.actions.*;
import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  olli, akivela
 */
public class Search implements Action,XMLParamAware {
    
    private Logger logger;
    
    /** Creates a new instance of Search */
    public Search() {
    }
    
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        WandoraManager manager=(WandoraManager)application.getService("WandoraManager");
        if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
            try {
                String templateId = request.getParameter("template");
                Template template = application.getTemplate(templateId,user);
                if(template == null) {
                    template=application.getTemplate("search",user);
                }
                String oquery=request.getParameter("query");
                int limit = 100;
                try {
                    limit = Integer.parseInt(request.getParameter("limit"));
                }
                catch(Exception e) {
                    // KEEP THE DEFAULT LIMIT;
                }
                String query=oquery;
                if(query==null) query="";
                else query=query.trim();
                try{
                    ArrayList found = new ArrayList();
                    if(query.length()>0){
                        String[] queryWords = makeQueryWords(query);
                        found = doSearch(queryWords, manager, limit);
                    }
                    HashMap context=new HashMap();
                    context.putAll(application.getDefaultContext(user));
                    context.put("results",new LimitedCollection(found, 100));
                    context.put("originalquery",oquery);
                    context.put("query",query);
                    response.setContentType(template.getMimeType());
                    response.setCharacterEncoding(template.getEncoding());
                    template.process(context,response.getOutputStream());
                }
                catch(IOException ioe){
                    logger.writelog("WRN","Search couldn't generate page. IOException "+ioe.getMessage());
                }
            }
            finally{
                manager.releaseTopicMap(WandoraManager.LOCK_READ);
            }
        }
        else{
            logger.writelog("WRN","Search couldn't lock topicmap");
        }
    }
    
    
    
    public String[] makeQueryWords(String query) {
        return makeQueryWords(query, 3);
    }
    public String[] makeQueryWords(String query, int maxlen) {
        Vector queryWords = new Vector();
        String queryWord;
        if(query==null) query="";
        StringTokenizer st = new StringTokenizer(query, " \t\n");
        while(st.hasMoreTokens()) {
            try {
                queryWord = st.nextToken().trim();
                if(queryWord != null && queryWord.length() > maxlen) {
                    queryWords.add(queryWord);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return (String[]) queryWords.toArray( new String[] {} );
    }
    

    
    
    public ArrayList doSearch(String[] queryWords, Object searchObject, int limit) {
        String queryWord;
        ArrayList<Topic> found = new ArrayList<Topic>();
        ArrayList<Topic> foundTemp;
        Topic sitem1;
        Topic sitem2;
        boolean keep = false;
        boolean add = false;
        
        if(queryWords != null && queryWords.length > 0) {
            try {
                found = doSearch(queryWords[0], searchObject);
                
                System.out.println("Initial found size: " + found.size());
                for(int k=1; k<queryWords.length; k++) {
                    queryWord = queryWords[k];

                    // UNION...
                    if(queryWord.startsWith("+")) {
                        queryWord = queryWord.substring(1);
                        if(queryWord.length() > 0) {
                            foundTemp = doSearch(queryWord, searchObject);
                            for(Iterator<Topic> foundTempIter = foundTemp.iterator(); foundTempIter.hasNext(); ) {
                                try {
                                    add = true;
                                    sitem2 = foundTempIter.next();
                                    for(Iterator<Topic> foundIterator = found.iterator(); foundIterator.hasNext(); ) {
                                        sitem1 = foundIterator.next();
                                        if( sitem1.mergesWithTopic(sitem2) ) {
                                            add = false;
                                            break;
                                        }
                                    }
                                    if(add) {
                                        found.add(sitem2);
                                    }
                                }
                                catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                        System.out.println("Found size after union "+ queryWord +": " + found.size());
                    }
                    
                    // NEGATION...
                    else if(queryWord.startsWith("-")) {
                        queryWord = queryWord.substring(1);
                        if(queryWord.length() > 0) {
                            ArrayList<Topic> remove = new ArrayList<Topic>();
                            foundTemp = doSearch(queryWord, searchObject);
                            for(Iterator<Topic> foundIterator = found.iterator(); foundIterator.hasNext(); ) {
                                try {
                                    keep = true;
                                    sitem1 = foundIterator.next();
                                    for(Iterator<Topic> foundTempIterator = foundTemp.iterator(); foundTempIterator.hasNext(); ) {
                                        sitem2 = foundTempIterator.next();
                                        if( sitem1.mergesWithTopic(sitem2) ) {
                                            keep = false;
                                            break;
                                        }
                                    }
                                    if(!keep) {
                                        remove.add(sitem1);
                                    }
                                }
                                catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                            for(Iterator<Topic> removeIterator = remove.iterator(); removeIterator.hasNext(); ) {
                                try {
                                    found.remove(removeIterator.next());
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        System.out.println("Found size after negation "+ queryWord +": " + found.size());
                    }
                    
                    // INTERSECTION (BY DEFAULT)
                    else {
                        ArrayList<Topic> remove = new ArrayList<Topic>();
                        foundTemp = doSearch(queryWord, searchObject);
                        
                        for(Iterator<Topic> foundIterator = found.iterator(); foundIterator.hasNext(); ) {
                            try {
                                keep = false;
                                sitem1 = foundIterator.next();
                                for(Iterator<Topic> foundTempIterator = foundTemp.iterator(); foundTempIterator.hasNext(); ) {
                                    sitem2 = foundTempIterator.next();
                                    if( sitem1.mergesWithTopic(sitem2)) {
                                        keep = true;
                                        break;
                                    }
                                }
                                if(!keep) {
                                    remove.add(sitem1);
                                }
                            }
                            catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        for(Iterator<Topic> removeIterator = remove.iterator(); removeIterator.hasNext(); ) {
                            try {
                                found.remove(removeIterator.next());
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("Found size after intersecting "+ queryWord +": " + found.size());
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
       return found;
    }
    
    
    
    
    public ArrayList<Topic> doSearch(String queryWord, Object searchObject) {
        ArrayList<Topic> found = new ArrayList<Topic>();
        try {
            if( searchObject instanceof WandoraManager) {
                found = new ArrayList<Topic>( ((WandoraManager) searchObject).search(queryWord) );
            }
            else if(searchObject instanceof TopicMap) {
                found = new ArrayList<Topic>(((TopicMap) searchObject).search(queryWord, new TopicMapSearchOptions(true, true, true, false, false)));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return found;
    }
    
    
    
    
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
    }
    
}

class LimitedCollection implements Collection {
    
    private Collection c;
    private int limit;
    
    public LimitedCollection(Collection c,int limit){
        this.c=c;
        this.limit=limit;
    }
    
    public boolean add(Object o) {
        return c.add(o);
    }
    
    public boolean addAll(Collection c) {
        return c.addAll(c);
    }
    
    public void clear() {
        c.clear();
    }
    
    public boolean contains(Object o) {
        return c.contains(o);
    }
    
    public boolean containsAll(Collection c) {
        return c.containsAll(c);
    }
    
    public boolean isEmpty() {
        return c.isEmpty();
    }
    
    public Iterator iterator() {
        return new Iterator(){
            int counter=0;
            Iterator iter=c.iterator();
            public boolean hasNext(){
                return (counter<limit && iter.hasNext());
            }
            public Object next(){
                if(counter>=limit) throw new NoSuchElementException();
                else return iter.next();
            }
            public void remove(){
                iter.remove();
            }
        };
    }
    
    public boolean remove(Object o) {
        return c.remove(o);
    }
    
    public boolean removeAll(Collection c) {
        return this.c.removeAll(c);
    }
    
    public boolean retainAll(Collection c) {
        return this.c.retainAll(c);
    }
    
    public int size() {
        return c.size();
    }
    
    public Object[] toArray() {
        return c.toArray();
    }
    
    public Object[] toArray(Object[] a) {
        return c.toArray(a);
    }
 
    
        
    
}

