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
package org.wandora.topicmap.webservice;
import java.util.*;
import java.util.concurrent.Semaphore;
import org.apache.axis2.AxisFault;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSTopic;

/**
 *
 * @author olli
 */
public class WebServiceTopicMap extends TopicMap {

    private ArrayList<TopicMapListener> topicMapListeners;
    private ArrayList<TopicMapListener> disabledListeners;
    private Map<String,WebServiceTopic> topicSIIndex;
    private Map<String,WebServiceTopic> topicSLIndex;
    private Map<String,WebServiceTopic> topicBNIndex;
    private TopicMapServiceStub webService;

    private final HashMap<Request,Request> requestLocks=new HashMap<Request,Request>();

    private static final int REQ_TYPES=1;
    private static final int REQ_TOPIC=2;

    public WebServiceTopicMap() {
        topicMapListeners=new ArrayList<TopicMapListener>();
    }
    public WebServiceTopicMap(String serviceEndPoint) {
        this();
        setWebService(serviceEndPoint);
    }
    public WebServiceTopicMap(TopicMapServiceStub service){
        this();
        setWebService(service);
    }

    private Request startRequest(int type,String si){
        Request request=new Request(type,si);
        synchronized(requestLocks){
            Request old=requestLocks.get(request);
            if(old!=null) request=old;
            else requestLocks.put(request,request);
            request.addCounter();
        }
        try{
            request.getSemaphore().acquire();
        }catch(InterruptedException ie){ie.printStackTrace();}
        return request;
    }

    private void endRequest(Request request){
        synchronized(requestLocks){
            if(request.decCounter()==0){
                requestLocks.remove(request);
            }
        }
        request.getSemaphore().release();
    }


    public void setWebService(TopicMapServiceStub service){
        webService=service;
        if(webService!=null){
            webService._getServiceClient().getOptions().setTimeOutInMilliSeconds(1000*60*2);
        }
    }
    public void setWebService(String service){
        try{
            if(service==null || service.length()==0) setWebService((TopicMapServiceStub)null);
            else setWebService(new TopicMapServiceStub(service));
            clearTopicMapIndexes();
        }catch(AxisFault af){
            af.printStackTrace();
        }catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }


    public TopicMapServiceStub getWebService(){
        return webService;
    }

    @Override
    public boolean isConnected() throws TopicMapException {
        return webService!=null;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }



    @Override
    public void addTopicMapListener(TopicMapListener listener) {
        topicMapListeners.add(listener);
    }

    @Override
    public void clearTopicMap() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
        topicSIIndex=Collections.synchronizedMap(new HashMap<String,WebServiceTopic>());
        topicSLIndex=Collections.synchronizedMap(new HashMap<String,WebServiceTopic>());
        topicBNIndex=Collections.synchronizedMap(new HashMap<String,WebServiceTopic>());
    }

    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public Topic copyTopicIn(Topic t, boolean deep) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public Topic createTopic() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void disableAllListeners() {
        if(disabledListeners==null){
            disabledListeners=topicMapListeners;
            topicMapListeners=new ArrayList<TopicMapListener>();
        }
    }

    @Override
    public void enableAllListeners() {
        if(disabledListeners!=null){
            topicMapListeners=disabledListeners;
            disabledListeners=null;
        }
    }

    @Override
    public Iterator<Association> getAssociations() throws TopicMapException {
        return new ArrayList<Association>().iterator();
    }

    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        return new ArrayList<Association>();
    }

    @Override
    public int getNumAssociations() throws TopicMapException {
        return 0;
    }

    @Override
    public int getNumTopics() throws TopicMapException {
        return 0;
    }

    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void indexTopic(WSTopic wstopic,WebServiceTopic t){
        String[] sis=wstopic.getSubjectIdentifiers();
        for(int i=0;i<sis.length;i++){
            topicSIIndex.put(sis[i],t);
        }
        String sl=wstopic.getSubjectLocator();
        if(sl!=null) topicSLIndex.put(sl,t);
        String bn=wstopic.getBaseName();
        if(bn!=null) topicBNIndex.put(bn,t);
    }

    public Topic getTopic(String si,boolean full) throws TopicMapException {
        if(webService==null) return null;
        WebServiceTopic t=topicSIIndex.get(si);
        if(t!=null || topicSIIndex.containsKey(si)) return t;

        try{
            WSTopic wstopic=webService.getTopic(si, full, null);
            if(wstopic==null){
                topicSIIndex.put(si,null);
                return null;
            }
            t=new WebServiceTopic(this,wstopic);
            indexTopic(wstopic, t);
            return t;
        }catch(java.rmi.RemoteException re){
            re.printStackTrace();
            return null;
        }
    }

    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        return getTopic(si,true);
    }
    public Topic getTopic(Locator si,boolean full) throws TopicMapException {
        return getTopic(si.toString(),full);
    }

    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        if(webService==null) return null;
        WebServiceTopic t=topicSLIndex.get(sl.toString());
        if(t!=null || topicSLIndex.containsKey(sl.toString())) return t;

        try{
            WSTopic wstopic=webService.getTopicWithSubjectLocator(sl.toString(), true, null);
            if(wstopic==null){
                topicSLIndex.put(sl.toString(),null);
                return null;
            }
            t=new WebServiceTopic(this,wstopic);
            indexTopic(wstopic, t);
            return t;
        }catch(java.rmi.RemoteException re){
            re.printStackTrace();
            return null;
        }
    }

    @Override
    public List<TopicMapListener> getTopicMapListeners() {
        return topicMapListeners;
    }

    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        if(webService==null) return null;
        WebServiceTopic t=topicBNIndex.get(name);
        if(t!=null || topicBNIndex.containsKey(name)) return t;

        try{
            WSTopic wstopic=webService.getTopicWithBaseName(name, true, null);
            if(wstopic==null){
                topicBNIndex.put(name,null);
                return null;
            }
            t=new WebServiceTopic(this,wstopic);
            indexTopic(wstopic, t);
            return t;
        }catch(java.rmi.RemoteException re){
            re.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterator<Topic> getTopics() throws TopicMapException {
        if(webService==null) new ArrayList<Topic>().iterator();
        try{
            WSTopic[] wstopics=webService.getAllTopics(false, null);
            ArrayList<Topic> ret=new ArrayList<Topic>();
            if(wstopics!=null){
                for(int i=0;i<wstopics.length;i++){
                    WebServiceTopic t=topicSIIndex.get(wstopics[i].getSubjectIdentifiers()[0]);
                    if(t==null){
                        t=new WebServiceTopic(this, wstopics[i]);
                        indexTopic(wstopics[i], t);
                    }
                    ret.add(t);
                }
            }
            return ret.iterator();
        }catch(java.rmi.RemoteException re){
            re.printStackTrace();
            return null;
        }
    }

    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        return getTopics(sis,true);
    }
    public Topic[] getTopics(String[] sis,boolean full) throws TopicMapException {
        if(webService==null) return new Topic[sis.length];

        ArrayList<String> fetch=new ArrayList<String>();
        ArrayList<Integer> indices=new ArrayList<Integer>();
        Topic[] ret=new Topic[sis.length];

        for(int i=0;i<sis.length;i++){
            Topic t=topicSIIndex.get(sis[i]);
            if(t!=null || topicSIIndex.containsKey(sis[i])) ret[i]=t;
            else {
                fetch.add(sis[i]);
                indices.add(i);
            }
        }
        if(fetch.size()==0) return ret;

        try{
            String[] fetchA=fetch.toArray(new String[fetch.size()]);
            WSTopic[] res=webService.getTopics(fetchA, full, null);
            for(int i=0;i<res.length;i++){
                WebServiceTopic t=null;
                if(res[i]!=null){
                    t=new WebServiceTopic(this, res[i]);
                    indexTopic(res[i], t);
                }
                else topicSIIndex.put(fetchA[i],null);
                ret[indices.get(i)]=t;
            }
            return ret;
        }catch(java.rmi.RemoteException re){
            re.printStackTrace();
            return null;
        }

    }

    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        String reqSI=type.getOneSubjectIdentifier().toString();
        Request request=startRequest(REQ_TYPES, reqSI);
        try{
            if(webService==null) new ArrayList<Topic>().iterator();
            WebServiceTopic wstype=((WebServiceTopic)type);
            if(wstype.instances!=null) return wstype.instances;
            try{
                WSTopic[] wstopics=webService.getTopicsOfType(type.getOneSubjectIdentifier().toString(), false, null);
                ArrayList<Topic> ret=new ArrayList<Topic>();
                if(wstopics!=null){
                    for(int i=0;i<wstopics.length;i++){
                        WebServiceTopic t=topicSIIndex.get(wstopics[i].getSubjectIdentifiers()[0]);
                        if(t==null){
                            t=new WebServiceTopic(this, wstopics[i]);
                            indexTopic(wstopics[i], t);
                        }
                        ret.add(t);
                    }
                }
                wstype.instances=ret;
                return ret;
            }catch(java.rmi.RemoteException re){
                re.printStackTrace();
                return null;
            }
        }
        finally{
            endRequest(request);
        }
    }

    @Override
    public boolean isTopicMapChanged() throws TopicMapException {
        return false;
    }

    @Override
    public void removeTopicMapListener(TopicMapListener listener) {
        topicMapListeners.remove(listener);
    }

    @Override
    public boolean resetTopicMapChanged() throws TopicMapException {
        return false;
    }

    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTrackDependent(boolean v) throws TopicMapException {
    }

    @Override
    public boolean trackingDependent() throws TopicMapException {
        return false;
    }

    private static class Request {
        private int type;
        private String si;
        private Semaphore wait;
        private int count;

        public Request(int type,String si){
            this.type=type;
            this.si=si;
            count=0;
        }

        public synchronized Semaphore getSemaphore(){
            if(wait==null) wait=new Semaphore(1);
            return wait;
        }

        public synchronized int getCounter(){
            return count;
        }
        public synchronized int addCounter(){
            return ++count;
        }
        public synchronized int decCounter(){
            return --count;
        }

        public int hashCode(){
            return (si==null?0:si.hashCode())+type;
        }
        public boolean equals(Object o){
            if(o.getClass()!=Request.class) return false;
            Request r=(Request)o;
            if(r.type!=type) return false;
            if(si==null && r.si==null) return true;
            if((si==null && r.si!=null) || (si!=null && r.si==null)) return false;
            return si.equals(r.si);
        }
    }
}
