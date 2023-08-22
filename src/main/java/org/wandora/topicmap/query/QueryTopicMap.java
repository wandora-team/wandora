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
 */
package org.wandora.topicmap.query;
import static org.wandora.utils.Tuples.t2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraScriptManager;
import org.wandora.query2.Directive;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.topicmap.TopicMapReadOnlyException;
import org.wandora.topicmap.TopicMapSearchOptions;
import org.wandora.topicmap.TopicMapStatData;
import org.wandora.topicmap.TopicMapStatOptions;
import org.wandora.topicmap.layered.ContainerTopicMap;
import org.wandora.topicmap.layered.ContainerTopicMapListener;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author olli
 */
public class QueryTopicMap extends ContainerTopicMap implements TopicMapListener, ContainerTopicMapListener  {


    protected LayerStack layerStack;
    protected Wandora admin;
    
    protected Collection<QueryInfo> originalQueries;
    protected Map<Locator,Directive> queries;
    protected Map<T2<Locator,Locator>,List<QueryAssociation>> associationCache;
    
    
    public QueryTopicMap(Wandora admin){
        setLayerStack(new LayerStack());
        this.admin=admin;
        queries=new HashMap<Locator,Directive>();
        clearAssociationCache();
        
        admin.addTopicMapListener(this);
        
/*        
        queries.put(new Locator(XTMPSI.SUPERCLASS_SUBCLASS),
            new UnionDirective(
                new JoinDirective(
                    new RecursiveDirective(
                        new RolesDirective(
                            new IsContextTopicDirective(
                                new SelectDirective(XTMPSI.SUPERCLASS_SUBCLASS,
                                                    XTMPSI.SUPERCLASS,XTMPSI.SUBCLASS),
                                XTMPSI.SUBCLASS),
                            XTMPSI.SUPERCLASS),
                        XTMPSI.SUPERCLASS
                    ),
                    new ContextTopicDirective(XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUBCLASS)
                ),
                new JoinDirective(
                    new RecursiveDirective(
                        new RolesDirective(
                            new IsContextTopicDirective(
                                new SelectDirective(XTMPSI.SUPERCLASS_SUBCLASS,
                                                    XTMPSI.SUPERCLASS,XTMPSI.SUBCLASS),
                                XTMPSI.SUPERCLASS),
                            XTMPSI.SUBCLASS),
                        XTMPSI.SUBCLASS
                    ),
                    new ContextTopicDirective(XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUPERCLASS)
                )
            )
        );*/
        
/*        ArrayList<QueryInfo> qinfos=new ArrayList<QueryInfo>();
        qinfos.add(new QueryInfo(
                XTMPSI.SUPERCLASS_SUBCLASS,
"            importPackage(org.wandora.query);\n"+
"            importPackage(org.wandora.topicmap);\n"+
"            new UnionDirective(\n"+
"                new JoinDirective(\n"+
"                    new RecursiveDirective(\n"+
"                        new RolesDirective(\n"+
"                            new IsContextTopicDirective(\n"+
"                                new SelectDirective(XTMPSI.SUPERCLASS_SUBCLASS,\n"+
"                                                    XTMPSI.SUPERCLASS,XTMPSI.SUBCLASS),\n"+
"                                XTMPSI.SUBCLASS),\n"+
"                            XTMPSI.SUPERCLASS),\n"+
"                        XTMPSI.SUPERCLASS\n"+
"                    ),\n"+
"                    new ContextTopicDirective(XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUBCLASS)\n"+
"                ),\n"+
"                new JoinDirective(\n"+
"                    new RecursiveDirective(\n"+
"                        new RolesDirective(\n"+
"                            new IsContextTopicDirective(\n"+
"                                new SelectDirective(XTMPSI.SUPERCLASS_SUBCLASS,\n"+
"                                                    XTMPSI.SUPERCLASS,XTMPSI.SUBCLASS),\n"+
"                                XTMPSI.SUPERCLASS),\n"+
"                            XTMPSI.SUBCLASS),\n"+
"                        XTMPSI.SUBCLASS\n"+
"                    ),\n"+
"                    new ContextTopicDirective(XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUPERCLASS)\n"+
"                )\n"+
"            );\n",
                WandoraScriptManager.getDefaultScriptEngine()));
        setQueries(qinfos);*/
    }
    
    public static class QueryInfo {
        public String name;
        public String script;
        public String type;
        public String engine;
        public QueryInfo(String name){
            this(name,"","",WandoraScriptManager.getDefaultScriptEngine());
        }
        public QueryInfo(String type,String script,String engine){
            this("",type,script,engine);
        }
        public QueryInfo(String name,String type,String script,String engine){
            this.name=name;
            this.type=type;
            this.script=script;
            this.engine=engine;
        }
        @Override
        public String toString(){return name;}
    }
    
    @Override
    public void addLayer(Layer l, int index) {
        layerStack.addLayer(l,index);
    }

    @Override
    public Layer getLayer(String name) {
        return layerStack.getLayer(name);
    }

    @Override
    public int getLayerZPos(Layer l) {
        return layerStack.getLayerZPos(l);
    }

    @Override
    public List<Layer> getLayers() {
        return layerStack.getLayers();
    }

    @Override
    public int getSelectedIndex() {
        return layerStack.getSelectedIndex();
    }

    @Override
    public Layer getSelectedLayer() {
        return layerStack.getSelectedLayer();
    }

    @Override
    public Collection<Topic> getTopicsForLayer(Layer l, Topic t) {
        return new ArrayList<Topic>();
        //return layerStack.getTopicsForLayer(l,t);
    }

    @Override
    public List<Layer> getVisibleLayers() {
        return layerStack.getVisibleLayers();
    }

    @Override
    public void notifyLayersChanged() {
        layerStack.notifyLayersChanged();
    }

    @Override
    public boolean removeLayer(Layer l) {
        return layerStack.removeLayer(l);
    }

    @Override
    public void reverseLayerOrder() {
        layerStack.reverseLayerOrder();
    }

    @Override
    public void selectLayer(Layer layer) {
        layerStack.selectLayer(layer);
    }

    @Override
    public void setLayer(Layer l, int pos) {
        layerStack.setLayer(l,pos);
    }
    
    
    
    public void setQueries(Collection<QueryInfo> queryInfos){
        this.originalQueries=queryInfos;
        queries=new HashMap<Locator,Directive>();
        WandoraScriptManager sm=new WandoraScriptManager();
        for(QueryInfo info : queryInfos){
            Locator l=new Locator(info.type);
            ScriptEngine engine=sm.getScriptEngine(info.engine);
            try{
                Object o=engine.eval(info.script);
                if(o==null) o=engine.get("query");
                if(o!=null && o instanceof Directive) {
                    queries.put(l,(Directive)o);
                }
            }
            catch(Exception e){
                admin.handleError(e);
            }
        }
    }
    
    public Collection<QueryInfo> getOriginalQueries(){
        return originalQueries;
    }
    
    public List<QueryAssociation> getCachedAssociations(Locator topic,Locator type){
        if(associationCache==null) associationCache=new HashMap<T2<Locator,Locator>,List<QueryAssociation>>();
        return associationCache.get(t2(topic,type));
    }
    public List<QueryAssociation> getCachedAssociations(QueryTopic topic,QueryTopic type){
        return getCachedAssociations(topic.si,type.si);
    }
    public void cacheAssociations(Locator topic,Locator type,List<QueryAssociation> associations){
        if(associationCache==null) associationCache=new HashMap<T2<Locator,Locator>,List<QueryAssociation>>();
        associationCache.put(t2(topic,type),associations);
    }
    public void cacheAssociations(QueryTopic topic,QueryTopic type,List<QueryAssociation> associations){
        cacheAssociations(topic.si,type.si,associations);
    }
    
    public void clearAssociationCache(){
        associationCache=null;
    }
    
    public void setLayerStack(LayerStack tm){
        if(layerStack!=null){
            layerStack.setParentTopicMap(null);
            layerStack.removeContainerListener(this);
        }
        layerStack=tm;
        layerStack.setParentTopicMap(this);
        layerStack.addContainerListener(this);
    }
    
    public LayerStack getLayerStack(){
        return layerStack;
    }
    
    public Map<Locator,Directive> getQueries(){
        return queries;
    }
    
    @Override
    public boolean isReadOnly(){
        return true;
    }
    
    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        if(queries.get(si)!=null) return new QueryTopic(si,this);
        for(Layer l : layerStack.getLayers()){
            if(l.getTopicMap()==this) continue;
            if(l.getTopicMap().getTopic(si)!=null){
                QueryTopic t=new QueryTopic(si,this);
                return t;
            }
        }
        return null;
    }

    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        return null;
    }

    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        return null;
    }

    @Override
    public Iterator<Topic> getTopics() throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        Topic[] ret=new Topic[sis.length];
        for(int i=0;i<ret.length;i++){
            ret[i]=getTopic(sis[i]);
        }
        return ret;
    }

    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) throws TopicMapException {
        return new ArrayList<Topic>();
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
        return null;
    }

    
    @Override
    public boolean isTopicMapChanged() throws TopicMapException {
        return false;
    }

    @Override
    public boolean resetTopicMapChanged() throws TopicMapException {
        return false;
    }
    
    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
    }

    @Override
    public void clearTopicMap() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }
    
    @Override
    public void close() {
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
    public Topic createTopic(String id) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }
    
    @Override
    public Topic createTopic() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }
    
/*    @Override
    public TopicMapListener setTopicMapListener(TopicMapListener listener) {
        return null;
    }*/
    public List<TopicMapListener> getTopicMapListeners(){
        return new ArrayList<TopicMapListener>();
    }
    public void addTopicMapListener(TopicMapListener listener){
    }
    public void removeTopicMapListener(TopicMapListener listener){
    }
    public void disableAllListeners(){
    }
    public void enableAllListeners(){
    }

    @Override
    public void setTrackDependent(boolean v) throws TopicMapException {
    }

    @Override
    public boolean trackingDependent() throws TopicMapException {
        return false;
    }

    
    // ---------------------------------------------------- TopicMapListener ---
    
    
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        clearAssociationCache();
    }

    @Override
    public void layerAdded(Layer l) {
        fireLayerAdded(l);
    }

    @Override
    public void layerChanged(Layer oldLayer, Layer newLayer) {
        fireLayerChanged(oldLayer,newLayer);
    }

    @Override
    public void layerRemoved(Layer l) {
        fireLayerRemoved(l);
    }

    @Override
    public void layerStructureChanged() {
        fireLayerStructureChanged();
    }

    @Override
    public void layerVisibilityChanged(Layer l) {
        fireLayerVisibilityChanged(l);
    }


}
