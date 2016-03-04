/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * RemoteTopicMap.java
 *
 * Created on August 16, 2004, 12:25 PM
 */

package org.wandora.topicmap.remote;
import org.wandora.exceptions.WandoraException;
import org.wandora.exceptions.ConcurrentEditingException;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;
import org.wandora.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;
import java.io.*;
import org.wandora.topicmap.remote.server.ServerInterface;

/**
 *
 * @author  olli
 */
public class RemoteTopicMap extends TopicMapImpl {
    
    private ServerInterface server;
    private HashSet fullTopics;
    private HashSet editedTopics;
    private HashSet editedAssociations;
    private HashSet deletedTopicSIs;
    
    private HashSet gotTopicsOfType;
    
    private HashSet fetchedBaseNames;
    private HashSet fetchedSIs;
    
    int importing;
    
    private Object connectionParams;
    public Object getConnectionParams(){return connectionParams;}
    
    /** Creates a new instance of RemoteTopicMap */
    public RemoteTopicMap(ServerInterface server,Object params) {
        this.server=server;
        this.connectionParams=params;
        importing=0;
        fullTopics=new HashSet();
        editedTopics=new HashSet();
        editedAssociations=new HashSet();
        deletedTopicSIs=new HashSet();
        gotTopicsOfType=new HashSet();
        fetchedBaseNames=new HashSet();
        fetchedSIs=new HashSet();
    }
    
    public RemoteTopicMap(ServerInterface server){
        this(server,null);
    }
    
    @Override
    public void clearTopicMap() throws TopicMapException {
        //server.clearTopicMap() // have to check if it's safe to call this
        throw new TopicMapException("Not supported");
    }
    
    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
        // TODO: IMPLEMENTATION REQUIRED!
    }
    
    
    public Collection getEditedTopics(){
        return editedTopics;
    }
    public Collection getEditedAssociations(){
        return editedAssociations;
    }
    
    @Override
    public Association copyAssociationIn(Association a)  throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Association as=super.copyAssociationIn(a);
        if(importing==0 && server!=null){
            editedAssociations.add(as);
        }
        return as;
    }
    
    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        super.copyTopicAssociationsIn(t);
    }
    
    @Override
    public Topic copyTopicIn(Topic t, boolean deep)  throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Topic to=super.copyTopicIn(t,deep);
        // TODO: how to handle deep copying?
        if(importing==0 && server!=null){
            editedTopics.add(to);
        }
        return to;
    }
    
    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Association a=super.createAssociation(type);
        if(importing==0 && server!=null){
            editedAssociations.add(a);
        }
        return a;
    }
    
    protected TopicImpl constructTopic() throws TopicMapException {
        return new RemoteTopic(this);
    }
    
    protected AssociationImpl constructAssociation(Topic type) throws TopicMapException {
        return new RemoteAssociation(this,type);
    }
    
    @Override
    public Topic createTopic() throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Topic t=super.createTopic();
        if(importing==0 && server!=null){
            editedTopics.add(t);
            fullTopics.add(t);
        }
        return t;
    }
    
    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException{
        return getTopics(sis,false);
    }
    
    public Topic[] getTopics(String[] sis,boolean overwrite) throws TopicMapException{
        Topic[] ts=new Topic[sis.length];
        boolean gotall=true;
        for(int i=0;i<sis.length;i++){
            ts[i]=super.getTopic(createLocator(sis[i]));
            if((ts[i]==null || !fullTopics.contains(ts[i])) && server!=null){
                gotall=false;
                break;
            }
        }
        if(gotall || server==null) return ts;
        try{
            ts=server.getTopics(sis);
        }catch(ServerException se){
            server.handleServerError(se);
            return new Topic[sis.length];
        }
        for(int i=0;i<ts.length;i++){
            Topic t=super.getTopic(createLocator(sis[i]));
            if((t!=null && fullTopics.contains(t)) || fetchedSIs.contains(sis[i])) ts[i]=t;
            else{
                if(ts[i]!=null) {
                    addFetchedTopicToCatalog(ts[i]);
                    ts[i]=copyFetchedTopicIn(ts[i],overwrite);
                    fullTopics.add(ts[i]);
                }
            }
        }
        return ts;
    }
    
    @Override
    public Topic getTopic(Locator si)  throws TopicMapException{
        if(importing!=0 || fetchedSIs.contains(si) || server==null) return super.getTopic(si);
        Topic t=super.getTopic(si);
        if(t!=null && fullTopics.contains(t)) return t;
        else {
            try{
                t=server.getTopic(si.toExternalForm());
            }catch(ServerException se){
                server.handleServerError(se);
                return null;
            }
        }
        if(t==null) return null;
        addFetchedTopicToCatalog(t);
        t=copyFetchedTopicIn(t);
        fullTopics.add(t);
        return t;
    }
    
        
    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        if(importing!=0 || fetchedBaseNames.contains(name) || server==null) return super.getTopicWithBaseName(name);
        Topic t=super.getTopicWithBaseName(name);
        if(t!=null && fullTopics.contains(t)) return t;
        else {
            try{
                t=server.getTopicByName(name);
            }catch(ServerException se){
                server.handleServerError(se);
                return null;
            }
        }
        if(t!=null){
            addFetchedTopicToCatalog(t);
            t=copyFetchedTopicIn(t);
            fullTopics.add(t);
            return t;
        }
        else return null;
    }
    
    @Override
    public java.util.Collection getTopicsOfType(Topic type)  throws TopicMapException{
        if(server==null) return super.getTopicsOfType(type);
        Iterator iter=type.getSubjectIdentifiers().iterator();
        HashSet ts=new HashSet();
        boolean first=true;
        // NOTE: If type is unedited, no need to get types for all subject identifiers, but if it has been edited,
        // it is possible that type is several topics on server side and thus we need to get instances for all
        // subject identifiers. (With concurrent editing this would be possible even when type is unedited, but
        // in that case we'll have several other problems too.)
        while((first || editedTopics.contains(type)) && iter.hasNext()){
            first=false;
            Locator l=(Locator)iter.next();
            Collection c=getTopicsOfType(l.toExternalForm());
            ts.addAll(c);
        }
        return ts;
    }
    
    @Override
    public Collection getTopicsOfType(String si)  throws TopicMapException{
        if(importing!=0 || server==null) return super.getTopicsOfType(si);
        Topic[] ts;
        if(gotTopicsOfType.contains(si)) ts=new Topic[0];
        else {
            try{
                ts=server.getTopicsOfType(si);
            }catch(ServerException se){
                server.handleServerError(se);
                return new HashSet();
            }
            gotTopicsOfType.add(si);
        }
        HashSet c=new HashSet();
        for(int i=0;i<ts.length;i++){
            Topic to=copyFetchedTopicIn(ts[i],false);
            if(to!=null) c.add(to);
        }
        Topic top=super.getTopic(createLocator(si));
        if(top!=null)
            c.addAll(super.getTopicsOfType(top));
        return c;
    }
    
    private boolean isFetchedTopicDeleted(Topic t) throws TopicMapException {
        Iterator iter=t.getSubjectIdentifiers().iterator();
        while(iter.hasNext()){
            Locator l=(Locator)iter.next();
            if(deletedTopicSIs.contains(l)){
                return true;
            }
        }        
        return false;
    }
    
    private void addFetchedTopicToCatalog(Topic t) throws TopicMapException {
        if(t.getBaseName()!=null) fetchedBaseNames.add(t.getBaseName());
        Iterator iter=t.getSubjectIdentifiers().iterator();
        while(iter.hasNext()){
            fetchedSIs.add(iter.next());
        }        
    }
    
    private Topic copyFetchedTopicIn(Topic t) throws TopicMapException{
        return copyFetchedTopicIn(t,false);
    }
    
    private Topic copyFetchedTopicIn(Topic t,boolean overwrite)  throws TopicMapException{
        importing++;
        
        Iterator iter=this.getMergingTopics(t).iterator();
        Topic edited=null;
        boolean deleted=isFetchedTopicDeleted(t);
        while(iter.hasNext()){
            Topic to=(Topic)iter.next();
            if(editedTopics.contains(to)){
                edited=to;
                break;
            }
        }
        if((edited==null && !deleted) || overwrite){
            Iterator iter2=t.getAssociations().iterator();
            t=copyTopicIn(t,false); // don't change order of lines, we must get the associations of the uncopied topic above
            Outer: while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                Iterator iter3=a.getRoles().iterator();
                while(iter3.hasNext()){
                    Topic role=(Topic)iter3.next();
                    Topic player=a.getPlayer(role);
                    // do not copy association if one of the players has been deleted
                    if(isFetchedTopicDeleted(role)) continue Outer;
                    if(isFetchedTopicDeleted(player)) continue Outer;
                    // also do not copy association if one of the players has been fully fetched
                    // in which case this association has allready been copied
                    Iterator iter4=getMergingTopics(player).iterator();
                    while(iter4.hasNext()){
                        Topic mt=(Topic)iter4.next();
                        if(fullTopics.contains(mt)) continue Outer;
                    }
                }
                copyAssociationIn(a);
            }
        }
        importing--;
        if(deleted) return null;
        else if(edited!=null) return edited;
        else return t;
    }
    
    public Topic makeFull(Topic t) throws TopicMapException{
        if(importing==0 && server!=null){
            return refreshTopic(t);
        }
        else return t;
    }
    
    public Topic refreshTopic(Topic t) throws TopicMapException{
        if(fullTopics.contains(t) || server==null) return t;
        Collection c=t.getSubjectIdentifiers();
        String[] sis=new String[c.size()];
        int counter=0;
        Iterator iter=c.iterator();
        while(iter.hasNext()){
            Locator l=(Locator)iter.next();
            sis[counter++]=l.toExternalForm();
        }
        Topic[] ts=null;
        try{
            ts=server.getTopics(sis);
        }catch(ServerException se){
            server.handleServerError(se);
            return null;
        }
        Topic to=null;
        for(int i=0;i<ts.length;i++){
            if(ts[i]!=null){
                to=copyFetchedTopicIn(ts[i]);
                fullTopics.add(to);
            }
        }
        if(to!=null) return to;
        return t;
    }
    
    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(importing==0 && server!=null){
            Iterator iter=t.getSubjectIdentifiers().iterator();
            while(iter.hasNext()){
                Locator l=(Locator)iter.next();
                deletedTopicSIs.add(l);
            }
            try{
                server.removeTopic(t);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
        super.topicRemoved(t);
    }
    
    private void _topicChanged(Topic t) throws TopicMapException {
        if(importing==0 && !t.isRemoved() && server!=null){
            editedTopics.add(t);
        }
    }
    
    private void _associationChanged(Association a) throws TopicMapException {
        if(!((RemoteAssociation)a).isRemoteInitialized() || ((RemoteAssociation)a).isRemoved() || server==null) return;
        if(importing==0 && !editedAssociations.contains(a)){
            try{
                server.removeAssociation(a);
            }catch(ServerException se){
                server.handleServerError(se);
            }
            editedAssociations.add(a);
        }
    }
    
    // -------------------------------------------------------------------------
    
    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        super.topicChanged(t);
        _topicChanged(t);
    }
    
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        super.associationChanged(a);
        _associationChanged(a);
    }
            
    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        super.associationRemoved(a);
        if(server==null) return;
        editedAssociations.remove(a);
        if(importing==0){
            boolean removeFromServer=true;
            Iterator iter=a.getRoles().iterator();
            while(iter.hasNext()){
                Topic role=(Topic)iter.next();
                Topic player=a.getPlayer(role);
                if(player.isRemoved()){
                    removeFromServer=false;
                    break;
                }
            }
            if(removeFromServer){
                try{
                    server.removeAssociation(a);
                }catch(ServerException se){
                    server.handleServerError(se);
                }
            }
        }
    }
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException{
        super.topicSubjectIdentifierChanged(t,added,removed);
        _topicChanged(t);
    }
    
    @Override
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException{
        super.topicBaseNameChanged(t,newName,oldName);
        _topicChanged(t);
    }
    
    @Override
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        super.topicTypeChanged(t,added,removed);
        _topicChanged(t);
    }
    
    @Override
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        super.topicVariantChanged(t,scope,newName,oldName);
        _topicChanged(t);
    }
    
    @Override
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        super.topicDataChanged(t,type,version,newValue,oldValue);
        _topicChanged(t);
    }
    
    @Override
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        super.topicSubjectLocatorChanged(t,newLocator,oldLocator);
        _topicChanged(t);
    }
    
    @Override
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        super.associationTypeChanged(a,newType,oldType);
        _associationChanged(a);
    }
    
    @Override
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        super.associationPlayerChanged(a,role,newPlayer,oldPlayer);
        _associationChanged(a);
    }
    
    @Override
    public void topicsMerged(Topic newtopic,Topic deletedtopic){
        super.topicsMerged(newtopic,deletedtopic);
        if(server!=null && editedTopics.contains(deletedtopic)){
            editedTopics.remove(deletedtopic);
            editedTopics.add(newtopic);
        }
    }
    
    @Override
    public void duplicateAssociationRemoved(Association a,Association removeda){
        super.duplicateAssociationRemoved(a,removeda);
        if(server!=null && editedAssociations.contains(removeda)){
            editedAssociations.remove(removeda);
            editedAssociations.add(a);
        }
    }
    
    public void removeTopicData(Topic topic,Topic type,Topic version) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null){
            try{
                server.removeData(topic,type,version);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
    }
    
    public void removeTopicBaseName(Topic topic) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null){
            try{
                server.removeBaseName(topic);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
    }
    
    @Override
    public void removeTopicSubjectIdentifier(Topic topic,Locator l) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null) {
            try{
                server.removeSubjectIdentifier(topic,l);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
        super.removeTopicSubjectIdentifier(topic,l);
    }
    
    @Override
    public void removeTopicType(Topic topic,Topic type) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null) {
            try{
                server.removeTopicType(topic,type);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
        super.removeTopicType(topic,type);
    }
    
    public void removeTopicVariant(Topic topic,Collection scope) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null) {
            try{
                server.removeVariantName(topic,scope);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
    }
/*  // The topic name is removed allready in RemoteTopic by calling RemoveTopicBaseName  
    public void setTopicName(Topic topic,String name,String oldName){
        if(importing==0 && !topic.isRemoved() && server!=null){
            if(oldName!=null && name!=null && !name.equals(oldName)){
                try{
                    server.removeBaseName(topic);
                }catch(ServerException se){
                    server.handleServerError(se);
                }
            }
        }
        super.setTopicName(topic,name,oldName);
    }*/
    public void setTopicData(Topic topic,Topic type,Topic version) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null){
            try{
                server.removeData(topic,type,version);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
    }
    
    @Override
    public void setTopicSubjectLocator(Topic topic,Locator locator,Locator oldLocator) throws TopicMapException {
        if(importing==0 && !topic.isRemoved() && server!=null && isReadOnly()) {
            try{
                server.removeSubjectLocator(topic);
            }catch(ServerException se){
                server.handleServerError(se);
            }
        }
        super.setTopicSubjectLocator(topic,locator,oldLocator);
    }
    
    // NOTE: getTopicBySubjectLocator doesn't get topics from the server
/*    public Topic getTopicBySubjectLocator(Locator sl) {
        if(server==null) return super.getTopicBySubjectLocator(sl);
        throw new RuntimeException("Not implemented");
    }*/
    // TODO: New operations are needed in ServerInterface to implement these efficiently.
    @Override
    public java.util.Collection getAssociationsOfType(Topic type) throws TopicMapException {
        if(server==null) return super.getAssociationsOfType(type);
        throw new RuntimeException("Not implemented");
    }
    
    @Override
    public java.util.Iterator getAssociations() throws TopicMapException {
        if(server==null) return super.getAssociations();
        throw new RuntimeException("Not implemented");
    }
    
    @Override
    public int getNumAssociations() throws TopicMapException {
        if(server==null) return super.getNumAssociations();
        throw new RuntimeException("Not implemented");
    } 
    
    @Override
    public int getNumTopics()  throws TopicMapException{
        if(server==null) return super.getNumTopics();
        throw new RuntimeException("Not implemented");
    }
    
    @Override
    public java.util.Iterator getTopics()  throws TopicMapException{
        if(server==null) return super.getTopics();
        throw new RuntimeException("Not implemented");
    }
    
    public String customCommand(String command) throws ServerException {
        if(server!=null) return server.customCommand(command);
        else return "No connection available, custom commands disabled.";
    }
    
    public boolean writeTopicMapTo(java.io.OutputStream out) throws java.io.IOException,ServerException {
        if(server==null) return false;
        return server.writeTopicMapTo(out);
    }
    
    public void saveSession(Wandora parent) {
        try{
            File file = new File("./temp/temp_session.xtms");
            OutputStream out=new FileOutputStream(file);
            StringBuffer session=getSession();
            out.write(session.toString().getBytes("UTF-8"));
            out.write("merge\n".getBytes("UTF-8"));
            getEditedTopicMap().exportXTM(out);
            out.close();
        } catch(Exception e) {
            WandoraOptionPane.showMessageDialog(parent,"Writing session failed: "+e.getMessage(), WandoraOptionPane.ERROR_MESSAGE);
        }
    }
    
    public StringBuffer getSession(){
        if(server!=null) return server.getSession();
        else return null;
    }
    
    
    
    public TopicMap getEditedTopicMap() throws TopicMapException{
        TopicMap edited=new org.wandora.topicmap.memory.TopicMapImpl();
        Iterator iter=getEditedTopics().iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t!=null) edited.copyTopicIn(t, false);
        }
        iter=getEditedAssociations().iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            edited.copyAssociationIn(a);
        }       
        return edited;
    }

    
    
    // -------------------------------------------------------------------------
    
    


    
    public void writelog(String lvl,String msg) throws ServerException{
        if(server==null) System.out.println("["+lvl+"] "+msg);
        else server.writelog(lvl,msg);
    }
    

    
    public boolean gzip() throws ServerException{
        if(server==null) return true;
        return server.gzip();
    }
    public boolean cipher() throws ServerException{
        if(server==null) return true;
        return server.cipher();
    }
    public boolean login(String user,String password) throws ServerException{
        if(server==null) return true;
        return server.login(user,password);
    }
    public boolean needLogin() throws ServerException{
        if(server==null) return false;
        else return server.needLogin();
    }
    @Override
    public void close() {
        if(server!=null) {
            try {
                server.close();
            }
            catch(ServerException se) {
                throw new RuntimeException(se);
            }
        }
    }
    
    public void connect() throws java.io.IOException {
        if(server!=null) server.connect();
    }
    public void handleServerError(Exception e){
        if(server!=null) server.handleServerError(e);
        else e.printStackTrace();
    }
    
    public void setErrorHandler(ErrorHandler handler){
        if(server!=null) server.setServerErrorHandler(handler);
    }
    
    
    public boolean isLocal(){
        return server==null;
    }
    
    @Override
    public boolean isConnected(){
        if(server==null) return false;
        else return server.isConnected();
    }
    
    

    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) {
        Collection<Topic> resultTopics = new ArrayList<Topic>();
        int MAX_SEARCH_RESULTS = 100;
        if(options.maxResults>=0 && options.maxResults<MAX_SEARCH_RESULTS) MAX_SEARCH_RESULTS=options.maxResults;
        try {
            if(server!=null) {
                String[] results = server.search(query);
                String score = null;
                String si = null;
                String basename = null;
                int limit = Math.min(results.length,  MAX_SEARCH_RESULTS*3);
                for(int i=0; i+2<limit; i=i+3) {
                    try {
                        score = results[i];
                        si = results[i+1];
                        basename = results[i+2];
                        if(si != null) {
                            resultTopics.add(getTopic(si));
                        }
                    }
                    catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return resultTopics;
    }

    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        if(server == null) return super.getStatistics(options);
        else {
            return new TopicMapStatData(super.getStatistics(options), new TopicMapStatData());
        }
    }
    
    

    public void commitSession(StringBuffer session,TopicMap edited) throws ServerException,WandoraException{
        server.applySession(session);
        server.mergeIn(edited);
        doCommit();
    }
    
    public void rollback() throws ServerException{
        if(server==null){
            System.out.println("Working in local mode, can't rollback.");
            return;
        }
        server.rollback();
    }
    
    
    
    private void doCommit() throws WandoraException, ServerException {
        try{
            server.commit(false);
        }
        catch(ConcurrentEditingException cee) {
            if(!isLocal()) {
                // TODO: concurrent editing handling code very much untested
                System.out.println("ConcurrentEditingException");
                Set s=cee.getRemovedTopics();
                Locator[] removedls=(Locator[])s.toArray(new Locator[0]);
                String[] removedsis=new String[removedls.length];
                for(int i=0;i<removedls.length;i++){
                    removedsis[i]=removedls[i].toExternalForm();
                }
                Topic[] removed=getTopics(removedsis,true);
                HashSet newRemoved=new HashSet();
                for(int i=0;i<removed.length;i++){
                    Iterator iter=removed[i].getSubjectIdentifiers().iterator();
                    while(iter.hasNext()){
                        Locator l=(Locator)iter.next();
                    }
                    newRemoved.add(removed[i]);
                }
                cee.setRemovedTopics(newRemoved);
                s=cee.getFailedTopics();
                String[] failedsis=new String[s.size()];
                Iterator iter=s.iterator();
                for(int i=0;i<failedsis.length;i++){
                    failedsis[i]=((Locator)iter.next()).toExternalForm();
                }
                Topic[] failed=getTopics(failedsis,true);
                HashSet newFailed=new HashSet();
                for(int i=0;i<failed.length;i++){
                    iter=failed[i].getSubjectIdentifiers().iterator();
                    while(iter.hasNext()){
                        Locator l=(Locator)iter.next();
                    }
                    newFailed.add(failed[i]);
                }
                cee.setFailedTopics(newFailed);
            }
            throw cee;
        }        
    }
    
    public void mergeIn() throws ServerException,TopicMapException{
        if(server==null) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        TopicMap edited=getEditedTopicMap();
        server.mergeIn(edited);        
    }
    
    
    public boolean isUncommitted(){
        if(server==null) return false;
        try{
            return server.isUncommitted();
        }
        catch(ServerException se){
            server.handleServerError(se);
            return true;
        }
    }
        

    
    public void commit() throws WandoraException,ServerException{
        if(server==null) {
            System.out.println("Working in local mode, can't commit.");
            return;
        }
        System.out.println("commiting changes");
//        checkEdited();
        mergeIn();
        doCommit();
        // note that these don't get executed if commit fails
        System.out.println("done, resetting local topic map");
    }
    
 
    /*
    
    public void topicChanged(Topic t){
        if(isLocal()) setLocalUncommitted();
        //if(topicMapListener!=null) topicMapListener.topicChanged(t);
    }
    public void topicRemoved(Topic t){
        if(isLocal()) setLocalUncommitted();
        //if(topicMapListener!=null) topicMapListener.topicRemoved(t);
    }
    public void associationChanged(Association a){
        if(isLocal()) setLocalUncommitted();
        //if(topicMapListener!=null) topicMapListener.associationChanged(a);
    }
    public void associationRemoved(Association a){
        if(isLocal()) setLocalUncommitted();
        //if(topicMapListener!=null) topicMapListener.associationRemoved(a);
    }
    */
}
