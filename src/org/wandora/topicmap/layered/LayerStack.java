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
 * LayerStack.java
 *
 * Created on 8. syyskuuta 2005, 11:31
 */

package org.wandora.topicmap.layered;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.wandora.topicmap.undowrapper.UndoBuffer;
import org.wandora.topicmap.undowrapper.UndoException;
import org.wandora.topicmap.undowrapper.UndoOperation;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
import org.wandora.utils.Tuples.T2;


/**
 * <p>
 * LayerStack combines several topic maps (called layers) into one topic map. This
 * has the advantage of being able to hide and show parts of the topic map when
 * necessary and keep different kinds of information apart from each other but still
 * be able to view all the information as a single topic map when needed.
 * </p><p>
 * All LayeredTopics consist of a collection of topics from all layers. The collection
 * may contain any number of topics for any layer.
 * All methods in LayerStack, LayeredTopic and LayeredAssociation always return
 * layered objects instead of the objects of different layers except for the method
 * where it is specifically stated that return value consists of individual topics.
 * Also all methods expect to receive their parameters as Layered objects instead
 * of objects of individual layers except when something else is specifically stated.
 * </p><p>
 * LayerStack has some requirements about the behavior of equals and hashCode
 * methods. Objects representing the same topic in the topic map must be equal
 * when checked with the equals method. Also, the hashCode of of a topic must
 * not change after the creation of the topic. One way to achieve this is to
 * use the default equals and hashCode methods and at any time have at most one object for
 * each topic in the topic map. LayerStack itself doesn't meet these requirements,
 * hashCode of topics will change when the set of topics it contains changes,
 * and thus you cannot use LayerStacks as layers in another LayerStack. 
 * </p><p>
 * Note: <br />
 * TopicMapListeners work a bit different in LayerStack than other TopicMap implementations.
 * Listeners may get changed events even when nothing that is visible using TopicMap interface
 * methods has changed. For example changing a variant name in any layer will cause topicVariantNameChanged
 * event even if another layer overrides the name. Also changes to subject identifiers may cause
 * complex merge or split operations which are reported as a single (or in some cases a few) events.
 * </p>
 * @author olli
 */
public class LayerStack extends ContainerTopicMap implements TopicMapListener {
    protected boolean useTopicIndex=true;
    protected Object indexLock=new Object();
    
    /**
     * Maps subject identifiers to layered topics for fast access in makeLayered.
     * Contains topics that have recently been used in makeLayered topic. Each
     * subject identifier of every topic in index is (and must be) indexed.
     *
     * General principle with updating index is that when a topic changes, it
     * is removed from index. That is, mappings from all it's (previous)
     * subject identifiers is removed. It will be added to index later when
     * it is used in makeLayered again. Indexes are completely wiped after
     * significant changes in topic map, such as adding layers or changing
     * visibility of layers.
     */
    protected HashMap<Locator,LayeredTopic> topicIndex;
    
    /**
     * All layers in the layer stack.
     */
    protected Vector<Layer> layers;
    /**
     * Maps the topic maps of each layer to the layer itself.
     */
    protected HashMap<TopicMap,Layer> layerIndex;
    
    /**
     * The selected layer.
     */
    protected Layer selectedLayer;
    /**
     * All layers that are currently visible, in the order they are in the stack.
     */
    protected Vector<Layer> visibleLayers;

    protected boolean trackDependent;
    protected ArrayList<TopicMapListener> topicMapListeners;
    protected ArrayList<TopicMapListener> disabledListeners;
    
//    private LayerControlPanel controlPanel;
    
    private AmbiguityResolver ambiguityResolver;
    
    protected ContainerTopicMapListener containerListener;
    
    protected boolean useUndo=true;
    
    /** Creates a new instance of LayerStack */
    public LayerStack(String wandoraProjectFilename) {
        this();
        try {
            File f = new File(wandoraProjectFilename);
            PackageInput in=new ZipPackageInput(f);
            TopicMapType type=TopicMapTypeManager.getType(org.wandora.topicmap.layered.LayerStack.class);
            TopicMap tm=type.unpackageTopicMap(this, in, "", this,null);
            in.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public LayerStack() {
        layers=new Vector<Layer>();
        visibleLayers=new Vector<Layer>();
        layerIndex=new LinkedHashMap<TopicMap,Layer>();
        topicMapListeners=new ArrayList<TopicMapListener>();
        containerListener=new ContainerTopicMapListener(){
            public void layerAdded(Layer l) {
                notifyLayersChanged();
                fireLayerAdded(l);
            }
            public void layerChanged(Layer oldLayer, Layer newLayer) {
                notifyLayersChanged();
                fireLayerChanged(oldLayer,newLayer);
            }
            public void layerRemoved(Layer l) {
                notifyLayersChanged();
                fireLayerRemoved(l);
            }
            public void layerStructureChanged() {
                notifyLayersChanged();
                fireLayerStructureChanged();
            }
            public void layerVisibilityChanged(Layer l) {
                notifyLayersChanged();
                fireLayerVisibilityChanged(l);
            }
        };
    }

    public boolean isUseUndo(){
        return this.useUndo;
    }
    
    // Use this to make LayerStack wrap layers in UndoTopicMap
    public void setUseUndo(boolean useUndo) throws UndoException{
/*        if(useUndo && !this.useUndo){
            // TODO: wrap all existing layers
        }
        else if(!useUndo && this.useUndo){
            // TODO: unwrap all existing layers
        }*/
        if(!layers.isEmpty()) throw new UndoException("Can't change undo status with existing layers");
        this.useUndo=useUndo;
    }

    // Use this to temporarily disable undo, for example before a very big
    // operation that you don't want to end up in the undo buffer.
    public void setUndoDisabled(boolean value){
        for(Layer l : layers){
            TopicMap tm=l.getTopicMap();
            if(tm instanceof UndoTopicMap) {
                ((UndoTopicMap)tm).setUndoDisabled(value);
            }
        }
    }
    
    public void undo() throws UndoException {
        if(!this.useUndo) throw new UndoException("Undo is not in use.");
        UndoBuffer buf=getNextUndo();
        if(buf==null) throw new UndoException("Nothing to undo.");
        buf.undo();
    }
    
    public void redo() throws UndoException {
        if(!this.useUndo) throw new UndoException("Undo is not in use.");
        UndoBuffer buf=getNextRedo();
        if(buf==null) throw new UndoException("Nothing to redo.");
        buf.redo();        
    }

    private UndoBuffer getNextUndo(){
        T2<Integer,UndoBuffer> nextUndoBuffer = getNextUndo(this);
        return nextUndoBuffer.e2;
    }
    
    public static T2<Integer,UndoBuffer> getNextUndo(LayerStack lst) {
        UndoBuffer ret=null;
        int biggest=Integer.MIN_VALUE;
        for(Layer l : lst.getLayers()) {
            TopicMap tm=l.getTopicMap();
            if(tm instanceof LayerStack) {
                T2<Integer,UndoBuffer> subBuffer = getNextUndo((LayerStack) tm);
                if(subBuffer.e1>biggest){
                    biggest=subBuffer.e1;
                    ret=subBuffer.e2;
                }
            }
            if(tm instanceof UndoTopicMap) {
                UndoBuffer buf=((UndoTopicMap)tm).getUndoBuffer();
                int num=buf.getUndoOperationNumber();
                if(num>biggest){
                    biggest=num;
                    ret=buf;
                }
            }
        }
        return new T2(new Integer(biggest), ret);
    }
    
    
    
    private UndoBuffer getNextRedo(){
        T2<Integer,UndoBuffer> nextRedoBuffer = getNextRedo(this);
        return nextRedoBuffer.e2;      
    }
    
    public static T2<Integer,UndoBuffer> getNextRedo(LayerStack lst) {
        UndoBuffer ret=null;
        int smallest=Integer.MAX_VALUE;
        for(Layer l : lst.getLayers()){
            TopicMap tm=l.getTopicMap();
            if(tm instanceof LayerStack) {
                T2<Integer,UndoBuffer> subBuffer = getNextRedo((LayerStack) tm);
                if(subBuffer.e1<smallest){
                    smallest=subBuffer.e1;
                    ret=subBuffer.e2;
                }
            }
            if(tm instanceof UndoTopicMap) {
                UndoBuffer buf=((UndoTopicMap)tm).getUndoBuffer();
                int num=buf.getRedoOperationNumber();
                if(num<smallest){
                    smallest=num;
                    ret=buf;
                }
            }
        }
        return new T2(new Integer(smallest), ret);
    }
    
    public boolean canUndo(){
        if(!this.useUndo) return false;
        UndoBuffer buf=getNextUndo();
        if(buf==null) return false;
        else return true;
    }
    
    public boolean canRedo(){
        if(!this.useUndo) return false;
        UndoBuffer buf=getNextRedo();
        if(buf==null) return false;
        else return true;
    }
    
    public void addUndoMarker(String label) {
        if(!this.useUndo) return;
        TopicMap tm = getSelectedLayer().getTopicMap();
        if(tm != null && tm instanceof UndoTopicMap) {
            UndoBuffer buf=((UndoTopicMap)tm).getUndoBuffer();
            buf.addMarker(label);
        }
    }
    
    public void clearUndoBuffers() {
        if(!this.useUndo) return;
        for(Layer l : layers){
            TopicMap tm=l.getTopicMap();
            if(tm instanceof UndoTopicMap) {
                UndoBuffer buf=((UndoTopicMap)tm).getUndoBuffer();
                buf.clear();
            }
        }
    }
    
    public ArrayList<UndoOperation> getUndoOperations() {
        ArrayList<UndoOperation> ops = new ArrayList();
        if(this.useUndo) {
            for(Layer l : layers){
                TopicMap tm=l.getTopicMap();
                if(tm instanceof UndoTopicMap) {
                    UndoBuffer buf=((UndoTopicMap)tm).getUndoBuffer();
                    ops.addAll(buf.getOperations());
                }
            }
            Collections.sort(ops, new Comparator() {
                public int compare(Object o1, Object o2) {
                    UndoOperation oo1 = (UndoOperation) o1;
                    UndoOperation oo2 = (UndoOperation) o2;
                    int oo1i = oo1.getOperationNumber();
                    int oo2i = oo2.getOperationNumber();
                    return (oo1i>oo2i ? -1 : (oo1i==oo2i ? 0 : 1));
                }
            });
        }
        return ops;
    }
    
    
    @Override
    public void clearTopicMap() throws TopicMapException{
        getSelectedLayer().getTopicMap().clearTopicMap();
        clearTopicIndex();
    }
    
    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
        clearTopicIndex();
        Layer layer = null;
        for(int i=layers.size()-1;i>=0; i--) {
            layer = layers.elementAt(i);
            layer.topicMap.clearTopicMapIndexes();
        }
    }
    
    /**
     * Clears topicIndex.
     */
    public void clearTopicIndex(){
        if(!useTopicIndex) return;
        synchronized(indexLock){
            topicIndex=new LinkedHashMap<Locator,LayeredTopic>();
        }
    }
    /**
     * Removes topic from topicIndex. Each subject identifier of the topic
     * is removed. It is assumed that each subject identifier of a topic is
     * in the index. Thus whatever the subject identifier used as parameter is,
     * the topic will be found if it is in the index. Note that if the indexed
     * topic doesn't anymore contain all its previous subject identifiers, you
     * will have to manually remove the old subject identifiers from the index.
     */
    void removeTopicFromIndex(Locator l) throws TopicMapException {
        if(!useTopicIndex) return;
        if(l==null) return;
        synchronized(indexLock){
            LayeredTopic lt=topicIndex.get(l);
            if(lt!=null){
                for(Locator lo : lt.getSubjectIdentifiers()){
                    topicIndex.remove(lo);
                }
            }
            topicIndex.remove(l); // remove the one used as parameter (not in the topic necessarily anymore)
        }
    }
    /**
     * Adds a topic to topicIndex. Each subject identifier of the topic is
     * mapped to the layered topic itself.
     */
    void addTopicToIndex(LayeredTopic lt) throws TopicMapException {
        synchronized(indexLock){
            if(topicIndex.size()>500) clearTopicIndex();
            for(Locator l : lt.getSubjectIdentifiers()){
                topicIndex.put(l,lt);
            }
        }
    }

    
    @Override
    public void setConsistencyCheck(boolean value) throws TopicMapException {
        consistencyCheck=value;
        Layer layer = null;
        for(int i=layers.size()-1; i>=0; i--) {
            layer = layers.elementAt(i);
            layer.topicMap.setConsistencyCheck(value);
        }
    }
    @Override
    public boolean getConsistencyCheck() throws TopicMapException {
        return consistencyCheck;
    }
    
    
/*    public LayerControlPanel getControlPanel(){
        if(controlPanel!=null) return controlPanel;
        
        controlPanel=new LayerControlPanel(this);
        controlPanel.resetLayers(layers);
        return controlPanel;
    }*/
    
    public void setAmbiguityResolver(AmbiguityResolver resolver){
        this.ambiguityResolver=resolver;
    }
    
    /* See note beginning of the file about TopicMapListeners in LayerStack
     */
    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicRemoved(lt);
            }
        }
        removeTopicFromIndex(t.getOneSubjectIdentifier());
    }
    
    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            for(TopicMapListener listener : topicMapListeners){
                listener.associationRemoved(makeLayeredAssociation(a));
            }
        }
    }
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException{
        if(removed!=null) removeTopicFromIndex(removed);
        if(added!=null) removeTopicFromIndex(added);
        for(Locator l : t.getSubjectIdentifiers()){
            removeTopicFromIndex(l);
        }
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicSubjectIdentifierChanged(lt,added,removed);
            }
        }
    }
    
    @Override
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException{
        for(Locator l : t.getSubjectIdentifiers()){
            removeTopicFromIndex(l);
        }
        removeTopicFromIndex(t.getOneSubjectIdentifier());
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicBaseNameChanged(lt,newName,oldName);
            }
        }
    }
    
    @Override
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            LayeredTopic ladded=makeLayeredTopic(added);
            LayeredTopic lremoved=makeLayeredTopic(removed);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicTypeChanged(lt,ladded,lremoved);
            }
        }
    }
    
    @Override
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            Collection<Topic> lscope=makeLayeredTopics(scope);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicVariantChanged(lt,lscope,newName,oldName);
            }
        }
    }
    
    @Override
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            LayeredTopic ltype=makeLayeredTopic(type);
            LayeredTopic lversion=makeLayeredTopic(version);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicDataChanged(lt,ltype,lversion,newValue,oldValue);
            }
        }
    }
    
    @Override
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        for(Locator l : t.getSubjectIdentifiers()){
            removeTopicFromIndex(l);
        }
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=makeLayeredTopic(t);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicSubjectLocatorChanged(lt,newLocator,oldLocator);
            }
        }
    }
    
    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        for(Locator l : t.getSubjectIdentifiers()){
            removeTopicFromIndex(l);
        }
        if(!topicMapListeners.isEmpty()) {
            LayeredTopic lt=null;
            if(t instanceof LayeredTopic && t.getTopicMap()==this) lt=(LayeredTopic)t;
            else lt=makeLayeredTopic(t);
            for(TopicMapListener listener : topicMapListeners){
                listener.topicChanged(lt);
            }
        }
    }
    
    @Override
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            LayeredAssociation la=makeLayeredAssociation(a);
            LayeredTopic lNewType=makeLayeredTopic(newType);
            LayeredTopic lOldType=makeLayeredTopic(oldType);
            for(TopicMapListener listener : topicMapListeners){
                listener.associationTypeChanged(la,lNewType,lOldType);
            }
        }
    }
    
    @Override
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        if(!topicMapListeners.isEmpty()) {
            LayeredAssociation la=makeLayeredAssociation(a);
            LayeredTopic lrole=makeLayeredTopic(role);
            LayeredTopic lNewPlayer=makeLayeredTopic(newPlayer);
            LayeredTopic lOldPlayer=makeLayeredTopic(oldPlayer);
            for(TopicMapListener listener : topicMapListeners){
                listener.associationPlayerChanged(la,lrole,lNewPlayer,lOldPlayer);
            }
        }
    }
    
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        if(!topicMapListeners.isEmpty()){
            LayeredAssociation la=null;
            if(a instanceof LayeredAssociation && a.getTopicMap()==this) la=(LayeredAssociation)a;
            else la=makeLayeredAssociation(a);
            for(TopicMapListener listener : topicMapListeners){
                listener.associationChanged(la);
            }
        }
    }
    

    /**
     * Checks if the selected layer is in read only mode.
     */
    public boolean isSelectedReadOnly(){
        return getSelectedLayer().isReadOnly();
    }
    
    /**
     * Gets layer position in the stack. Layer with index 0 is at the top.
     */
    public int getLayerZPos(Layer l) {
        return layers.indexOf(l);
    }
    
    /**
     * Gets the layer a topic belongs to.
     */
    public Layer getLayer(Topic t) {
        return getLayer(t.getTopicMap());
    }
    
    /**
     * Gets the layer of a topic map.
     */
    public Layer getLayer(TopicMap tm) {
        return layerIndex.get(tm);
    }
    
    /**
     * Gets the layer with the specified name.
     */
    @Override
    public Layer getLayer(String layerName) {
        Layer layer = null;
        if(layerName != null) {
            for(int i=layers.size()-1; i>=0; i--) {
                layer = layers.elementAt(i);
                if(layerName.equals(layer.getName())) {
                    return layer;
                }
            }
        }
        return null;
    }
    
    /**
     * Gets the selected layer.
     */
    @Override
    public Layer getSelectedLayer(){
        return selectedLayer;
    }
    
    /**
     * Gets the selected layer position in the stack.
     */
    @Override
    public int getSelectedIndex(){
        return getLayerZPos(selectedLayer);
    }
    
    /**
     * Makes the specified layer the selected layer.
     */
    @Override
    public void selectLayer(Layer layer){
        selectedLayer=layer; 
//        if(controlPanel!=null) controlPanel.resetLayers(layers);
    }
    
    /**
     * Gets all layers in the order they are in the stack.
     */
    @Override
    public List<Layer> getLayers(){
        return layers;
    }
    
    /**
     * Gets all visible layers in the order they are in the stack.
     */
    @Override
    public List<Layer> getVisibleLayers(){
        return visibleLayers;
    }
    
    @Override
    public void notifyLayersChanged(){
        clearTopicIndex();
        visibleLayers=new Vector<Layer>();
        for(Layer l : layers) {
            if(l.isVisible()) visibleLayers.add(l);
        }
//        TopicMap parent=getParentTopicMap();
//        if(parent!=null && parent instanceof LayerStack) ((LayerStack)parent).notifyLayersChanged();
    }

    @Override
    public Collection<Topic> getTopicsForLayer(Layer l,Topic t) {
        return ((LayeredTopic)t).getTopicsForLayer(l);
    }
    
    /**
     * Adds a layer at the bottom of the stack.
     */
    @Override
    public void addLayer(Layer l) {
        addLayer(l,layers.size());
    }
    
    /**
     * Inserts a layer at the specified position in the stack. Layers after
     * that index are moved one position down.
     */
    @Override
    public void addLayer(Layer l,int pos) {
        if(useUndo) l.wrapInUndo();
        
        if(layers.size()<pos) pos=layers.size();
        layers.insertElementAt(l,pos);
        layerIndex.put(l.getTopicMap(),l);
        if(layers.size()==1) selectedLayer=layers.elementAt(0);
        l.getTopicMap().addTopicMapListener(this);
        if(l.getTopicMap() instanceof ContainerTopicMap)
            ((ContainerTopicMap)l.getTopicMap()).addContainerListener(containerListener);
        l.getTopicMap().setParentTopicMap(this);
        notifyLayersChanged();
//        visibilityChanged(l);        
        fireLayerAdded(l);
    }
    
    
    /**
     * Sets layer in the specified position removing old layer at that position.
     */
    @Override
    public void setLayer(Layer l, int pos) {
        if(useUndo) l.wrapInUndo();
        
        Layer old=layers.elementAt(pos);
        layerIndex.remove(old);
        layerIndex.put(l.getTopicMap(),l);
        old.getTopicMap().removeTopicMapListener(this);
        if(old.getTopicMap() instanceof ContainerTopicMap)
            ((ContainerTopicMap)old.getTopicMap()).removeContainerListener(containerListener);
        layers.setElementAt(l,pos);
        l.getTopicMap().addTopicMapListener(this);
        if(l.getTopicMap() instanceof ContainerTopicMap)
            ((ContainerTopicMap)l.getTopicMap()).addContainerListener(containerListener);
        l.getTopicMap().setParentTopicMap(this);
        if(selectedLayer==old) selectedLayer=l;
        notifyLayersChanged();
//        visibilityChanged(l);
        fireLayerChanged(old, l);
    }
    
    /**
     * Removes the specified layer. Layers after the removed layer are
     * moved one position up.
     */
    @Override
    public boolean removeLayer(Layer l) {
        if(layers.remove(l)){
            layerIndex.remove(l);
            if(selectedLayer==l) selectedLayer=(layers.size()>0?layers.elementAt(0):null);
            l.getTopicMap().removeTopicMapListener(this);
            if(l.getTopicMap() instanceof ContainerTopicMap)
                ((ContainerTopicMap)l.getTopicMap()).removeContainerListener(containerListener);
            l.getTopicMap().setParentTopicMap(null);
            notifyLayersChanged();
//            visibilityChanged(l);
            fireLayerRemoved(l);
            return true;
        }
        return false;
    }
    
    /**
     * Moves layers around to reverse layer order.
     */
    @Override
    public void reverseLayerOrder() {
        Vector<Layer> newLayers=new Vector<Layer>();
        for(int i=layers.size()-1; i>=0; i--) {
            newLayers.add(layers.elementAt(i));
        }
        layers = newLayers;
        notifyLayersChanged();
        fireLayerStructureChanged();
    }
    
    /**
     * Merges all layers in the specified layer.
     */
    public void mergeAllLayers(int targetLayerIndex) {
        if(targetLayerIndex < 0 || targetLayerIndex >= layers.size()) return;
        int[] mergeIndex = new int[layers.size()];
        mergeIndex[0] = targetLayerIndex;
        int j = 1;
        for(int i=0; i<layers.size(); i++) {
            if(i != targetLayerIndex) {
                mergeIndex[j++] = i;
            }
        }
        mergeLayers(mergeIndex);
    }
    
    /**
     * Merges some layers. The array given as parameter should contain indexes
     * of layers to be merged. First index is used as the target layer and all
     * other layers are merged into that.
     */
    public void mergeLayers(int[] layerIndexes) {
        if(layerIndexes == null || layerIndexes.length < 2) return;
        Layer targetLayer = layers.elementAt(layerIndexes[0]);
        Vector<Layer> sourceLayers = new Vector<Layer>();
        Layer sourceLayer = null;
        for(int i=1; i<layerIndexes.length; i++) {
            sourceLayers.add(layers.elementAt(layerIndexes[i]));
        }
        for(int i=sourceLayers.size()-1; i>=0; i--) {
            try {
                sourceLayer = sourceLayers.elementAt(i);
                targetLayer.getTopicMap().mergeIn(sourceLayer.getTopicMap());
                removeLayer(sourceLayer);
//                layers.remove(sourceLayer);
                sourceLayers.remove(sourceLayer);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    void ambiguity(String s){
        if(ambiguityResolver!=null) ambiguityResolver.ambiguity(s);
    }
    
    public AmbiguityResolution resolveAmbiguity(String event){
        if(ambiguityResolver!=null) return ambiguityResolver.resolveAmbiguity(event);
        else return AmbiguityResolution.addToSelected;
    }
    
    public AmbiguityResolution resolveAmbiguity(String event,String msg){
        if(ambiguityResolver!=null) return ambiguityResolver.resolveAmbiguity(event,msg);
        else return AmbiguityResolution.addToSelected;
    }
/*    
    void visibilityChanged(Layer layer){
        clearTopicIndex();
        visibleLayers=new Vector<Layer>();
        for(Layer l : layers){
            if(l.isVisible()) visibleLayers.add(l);
        }
//        if(controlPanel!=null){
//            controlPanel.resetLayers(layers);
//        }
    }
    */
    /* ************************* TopicMap functions ************************* */
    
    /**
     * Collects all topics from all layers that merge with the given topic.
     */
    protected Set<Topic> collectTopics(Topic t) throws TopicMapException {
        Set<Topic> collected=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        /*
         if(visibleLayers.size() < 2) {
            collected.add(t);
        }
        else {
         **/
            collectTopics(collected,t);
        //}
        return collected;
    }
    
    /**
     * Collects all topics from all layers that merge with the given topic. Initially
     * the collected Set is empty but all merging topics are added to it to be returned
     * later and to avoid processing same topic several times.
     */
    protected void collectTopics(Set<Topic> collected,Topic t) throws TopicMapException {
        for(Layer l : visibleLayers){
            Collection<Topic> merging=l.getTopicMap().getMergingTopics(t);
            for(Topic m : merging){
                if(collected.add(m)){
                    collectTopics(collected,m);
                }
            }
        }
    }
    
    /**
     * Makes a layered topic when given a topic in one of the layers, that is a topic
     * that isn't yet a LayeredTopic of this LayerStack. First
     * checks if the topic is available in the topicIndex map for fast retrieval.
     * If not, collects all topics from different layers and adds the topic to
     * the index before it is returned.
     */
    LayeredTopic makeLayeredTopic(Topic t) throws TopicMapException {
        if(t==null) return null;
        if(useTopicIndex){
            synchronized(indexLock){
                Locator l=t.getOneSubjectIdentifier();
                LayeredTopic lt=topicIndex.get(l);
                if(lt!=null) return lt;
            }
        }
        Set<Topic> collected=collectTopics(t);
        LayeredTopic lt=new LayeredTopic(collected,this);
        
        if(useTopicIndex){
            addTopicToIndex(lt);
        }
        
        return lt;
    }
    
    /**
     * Makes layered topics for all topics in the collection. Note that some topics
     * in the collection may end up in the same layered topic and thus the returned
     * collection may have less items than the collection used as the parameter.
     */
    Collection<Topic> makeLayeredTopics(Collection<Topic> ts) throws TopicMapException {
        ArrayList<Topic> ret=new ArrayList<Topic>();
        Set<Topic> processed=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        for(Topic t : ts){
            if(processed.contains(t)) continue;
            Set<Topic> collected=collectTopics(t);
            processed.addAll(collected);
            ret.add(new LayeredTopic(collected,this));
        }
        return ret;
    }
    
    /**
     * Makes layered association from an individual association. Note that unlike
     * LayeredTopic.getAssociations, this method returns only one LayeredAssociation
     * for each individual association, even if roles have been merged and
     * there are actually several LayeredAssociations that are constructed from
     * the association used as parameter. In the case of merged roles, one of
     * the possible LayeredAssociations is returned arbitrarily.
     */
    LayeredAssociation makeLayeredAssociation(Association a) throws TopicMapException {
        LayeredTopic type=(a.getType()==null?null:makeLayeredTopic(a.getType()));
        LayeredAssociation la=new LayeredAssociation(this,type);
        for(Topic role : a.getRoles()){
            Topic player=a.getPlayer(role);
            LayeredTopic lrole=makeLayeredTopic(role);
            LayeredTopic lplayer=makeLayeredTopic(player);
            if(la.getPlayer(lrole)!=null) {
                ambiguity("Assocition roles merged (makeLayeredAssociation)");
                continue;
            }
            la.addLayeredPlayer(lplayer,lrole);
        }
        return la;
    }
    
    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        Set<Topic> collected=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        for(Layer l : visibleLayers){
            Topic t=l.getTopicMap().getTopic(si);
            if(t!=null) return makeLayeredTopic(t); 
            // note: makeLayeredTopic calls collectTopics which will get rest of topics
            //       with the specified subject identifier
        }
        return null;
    }
    
    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        Topic[] ret=new Topic[sis.length];
        for(int i=0;i<sis.length;i++){
            ret[i]=getTopic(sis[i]);
        }
        return ret;
    }
    
    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        HashSet<Topic> collected=new LinkedHashSet<Topic>();
        for(Layer l : visibleLayers){
            Topic t=l.getTopicMap().getTopicBySubjectLocator(sl);
            if(t!=null) return makeLayeredTopic(t); 
        }        
        return null;
    }
    
    @Override
    public Topic createTopic() throws TopicMapException {
        if(isSelectedReadOnly()) throw new TopicMapReadOnlyException();
        if(selectedLayer!=null){
            Topic t=selectedLayer.getTopicMap().createTopic();
            return new LayeredTopic(t,this);
        }
        else{
            // TODO: some other exception
            throw new RuntimeException("No selected layer");
        }
    }
    
    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        if(isSelectedReadOnly()) throw new TopicMapReadOnlyException();
        if(selectedLayer!=null){
            LayeredTopic lt=(LayeredTopic)type;
            Collection<Topic> c=lt.getTopicsForSelectedLayer();
            Topic st=null;
            if(c.isEmpty()){
                AmbiguityResolution res=resolveAmbiguity("createAssociation.type.noSelected","No type in selected layer");
                if(res==AmbiguityResolution.addToSelected){
                    st=((LayeredTopic)type).copyStubTo(getSelectedLayer().getTopicMap());
                    if(st==null){
                        ambiguity("Cannot copy topic to selected layer");
                        throw new TopicMapException("Cannot copy topic to selected layer");
                    }
                }
                else throw new RuntimeException("Not implemented");
            }
            else{
                if(c.size()>1) ambiguity("Multiple possible types in layer (createAssociation)");
                st=c.iterator().next();
            }
//            Layer l=getSelectedLayer();
//            l.getTopicMap().createAssociation(st);
            return new LayeredAssociation(this,lt);
        }
        else{
            throw new RuntimeException("No selected layer");
            // TODO: some other exception
        }
    }
    
    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        Set<Topic> processed=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        ArrayList<Topic> ret=new ArrayList<Topic>();
        LayeredTopic lt=(LayeredTopic)type;
        for(Layer l : visibleLayers){
            for(Topic typeIn : lt.getTopicsForLayer(l)){
                Collection<Topic> c=l.getTopicMap().getTopicsOfType(typeIn);
                for(Topic t : c){
                    if(processed.contains(t)) continue;
                    Set<Topic> collected=collectTopics(t);
                    processed.addAll(collected);
                    LayeredTopic add=new LayeredTopic(collected,this);
                    addTopicToIndex(add);
                    ret.add(add);
                }
            }
        }
        return ret;
    }
    
    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        HashSet<Topic> collected=new LinkedHashSet<Topic>();
        for(Layer l : visibleLayers){
            Topic t=l.getTopicMap().getTopicWithBaseName(name);
            if(t!=null) return makeLayeredTopic(t);
        }
        return null;
    }


    /*
    public Iterator<Topic> getTopics() throws TopicMapException {
        // TODO: implementation that doesn't get everything in memory at once
        Set<Topic> processed=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        Vector<Topic> ret=new Vector<Topic>();
        for(Layer l : visibleLayers){
            Iterator<Topic> c=l.getTopicMap().getTopics();
            while(c.hasNext()){
                Topic t=c.next();
                if(processed.contains(t)) continue;
                Set<Topic> collected=collectTopics(t);
                processed.addAll(collected);
                ret.add(new LayeredTopic(collected,this));
            }
        }
        return ret.iterator();
    }
    */
    
    private class TopicsIterator implements TopicIterator {
        public LayeredTopic next=null;
        public Iterator<Topic> currentIterator = null;
        public int layerIndex = 0;
        public Set<Topic> processed;
        
        public TopicsIterator(){
            processed=processed=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        }

        @Override
        public void dispose() {
            if(currentIterator!=null){
                if(currentIterator instanceof TopicIterator) ((TopicIterator)currentIterator).dispose();
                else {
                    while(currentIterator.hasNext()) currentIterator.next();
                }
                currentIterator=null;
                layerIndex=visibleLayers.size();
                next=null;
            }
        }
        
        @Override
        public boolean hasNext() {
            if(next!=null) return true;
            while(_hasNext()){
                Topic t=_next();
                if(processed.contains(t)){
                    continue;
                }
                else{
                    try{
                        Set<Topic> collected=collectTopics(t);
                        next=new LayeredTopic(collected, LayerStack.this);
                        processed.addAll(collected);
                        break;
                    }catch(TopicMapException tme){
                        log(tme);
                        next=null;
                        return false;
                    }
                }
            }
            return next!=null;
        }

        @Override
        public Topic next() {
            if(!hasNext()) throw new NoSuchElementException();
            Topic ret=next;
            next=null;
            return ret;
        }

        public boolean _hasNext(){
            currentIterator = solveCurrentIterator(currentIterator);
            if(currentIterator != null) {
                return currentIterator.hasNext();
            }
            return false;                
        }

        public Topic _next() {
            currentIterator = solveCurrentIterator(currentIterator);
            if(currentIterator != null) {
                if(currentIterator.hasNext()) {
                    return currentIterator.next();
                }
            }
            throw new NoSuchElementException();
        }

        public Iterator<Topic> solveCurrentIterator(Iterator iterator) {
            while(true){
                if(iterator!=null && iterator.hasNext()) return iterator;

                if( layerIndex < visibleLayers.size()) {
                    try {
                        iterator = visibleLayers.elementAt(layerIndex).getTopicMap().getTopics();
                        layerIndex++;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                else return null;
            }
        }

        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }
    
    
    @Override
    public Iterator<Topic> getTopics() throws TopicMapException {
        return new TopicsIterator();
    }
    
    /*
    public Iterator<Association> getAssociations() throws TopicMapException {
        // TODO: implementation that doesn't get everything in memory at once
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new HashSet<Association>();
        for(Layer l : visibleLayers) {
            Iterator<Association> c=l.getTopicMap().getAssociations();
            while(c.hasNext()){
                Association a=c.next();
                LayeredTopic lt=getLayeredTopic(a.getType(),layeredTopics);
                LayeredAssociation la=new LayeredAssociation(this,lt);
                for(Topic role : a.getRoles()){
                    LayeredTopic lrole=getLayeredTopic(role,layeredTopics);
                    Topic player=a.getPlayer(role);
                    LayeredTopic lplayer=getLayeredTopic(player,layeredTopics);
                    la.addLayeredPlayer(lplayer, lrole);
                }
                associations.add(la);
            }
        }
        return associations.iterator();
    }
    */
    
    private class AssociationsIterator implements Iterator<Association> {
        public TopicsIterator topicsIterator;
        public Association next;
        
        public Topic currentTopic;
        public Iterator<Association> currentAssociations;
        
        public AssociationsIterator(){
            topicsIterator=new TopicsIterator();
        }
        @Override
        public boolean hasNext(){
            if(next!=null) return true;
            
            try{
                
                Outer: while(true){
                    if(currentAssociations==null || !currentAssociations.hasNext()){
                        if(topicsIterator.hasNext()){
                            currentTopic=topicsIterator.next();
                            currentAssociations=currentTopic.getAssociations().iterator();
                            continue;
                        }
                        else return false;
                    }
                    Association a=currentAssociations.next();
                    // if, and only if, any of the players, except current topic,
                    // is in topicsIterator.processed then we have included this
                    // association already
                    for(Topic role : a.getRoles()){
                        LayeredTopic player=(LayeredTopic)a.getPlayer(role);
                        if(player.mergesWithTopic(currentTopic)) continue;
                        for(Topic t : player.getTopicsForAllLayers()){
                            if(topicsIterator.processed.contains(t)){
                                continue Outer; // skip this association
                            }
                        }
                    }
                    next=a;
                    return true;
                }
            
            }
            catch(TopicMapException tme){
                log(tme);
                return false;
            }
        }
        @Override
        public Association next(){
            if(!hasNext()) throw new NoSuchElementException();
            Association ret=next;
            next=null;
            return ret;
        }
        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }
    
    
    @Override
    public Iterator<Association> getAssociations() throws TopicMapException {
        return new AssociationsIterator();
/*        final KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(new TopicAndLayerKeyMaker());
        final LayerStack layerStack = this;
        System.out.println("Getting all associations from layerStack!");
        
        return new Iterator<Association>() {
            Iterator<Association> currentIterator = null;
            int layerIndex = 0;
            public boolean hasNext() {
                currentIterator = solveCurrentIterator(currentIterator);
                if(currentIterator != null) {
                    return currentIterator.hasNext();
                }
                return false;
            }
            
            public Association next() {
                currentIterator = solveCurrentIterator(currentIterator);
                if(currentIterator != null) {
                    if(currentIterator.hasNext()) {
                        try {
                            Association a=currentIterator.next();
                            LayeredTopic lt=getLayeredTopic(a.getType(),layeredTopics);
                            LayeredAssociation la=new LayeredAssociation(layerStack, lt);
                            for(Topic role : a.getRoles()){
                                LayeredTopic lrole=getLayeredTopic(role,layeredTopics);
                                Topic player=a.getPlayer(role);
                                LayeredTopic lplayer=getLayeredTopic(player,layeredTopics);
                                la.addLayeredPlayer(lplayer, lrole);
                            }
                            return la;
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                throw new NoSuchElementException();
            }
            
            public Iterator<Association> solveCurrentIterator(Iterator iterator) {
                while(true){
                    if(iterator!=null && iterator.hasNext()) return iterator;
                    
                    if( layerIndex < visibleLayers.size()) {
                        try {
                            iterator = visibleLayers.elementAt(layerIndex).getTopicMap().getAssociations();
                            layerIndex++;
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else return null;
                }
            }
            
            
            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
        */
    }
    
    
    
    
    LayeredTopic getLayeredTopic(Topic t,Map<Topic,LayeredTopic> layeredTopics) throws TopicMapException {
        LayeredTopic lt=layeredTopics.get(t);
        if(lt==null){
            Set<Topic> collected=collectTopics(t);
            lt=new LayeredTopic(collected,this);
            for(Topic ct : collected) layeredTopics.put(ct,lt);
        }
        return lt;
    }
    
    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        LayeredTopic lt=(LayeredTopic)type;
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new LinkedHashSet<Association>();
        for(Layer l : visibleLayers){
            for(Topic typeIn : lt.getTopicsForLayer(l)){
                Collection<Association> c=l.getTopicMap().getAssociationsOfType(typeIn);
                for(Association a : c ){
                    LayeredAssociation la=new LayeredAssociation(this,lt);
                    for(Topic role : a.getRoles()){
                        LayeredTopic lrole=getLayeredTopic(role,layeredTopics);
                        Topic player=a.getPlayer(role);
                        LayeredTopic lplayer=getLayeredTopic(player,layeredTopics);
                        la.addLayeredPlayer(lplayer, lrole);
                    }
                    associations.add(la);
                }
            }
        }
        return associations;
    }
    
    @Override
    public int getNumTopics() throws TopicMapException {
        Set<Topic> processed=new KeyedHashSet<Topic>(new TopicAndLayerKeyMaker());
        int count=0;
        for(Layer l : visibleLayers){
            Iterator<Topic> c=l.getTopicMap().getTopics();
            while(c.hasNext()){
                Topic t=c.next();
                if(processed.contains(t)) continue;
                Set<Topic> collected=collectTopics(t);
                processed.addAll(collected);
                count++;
            }
        }
        return count;
    }
    
    
    @Override
    public int getNumAssociations() throws TopicMapException {
        int counter=0;
        AssociationsIterator iter=new AssociationsIterator();
        while(iter.hasNext()){
            iter.next();
            counter++;
        }

        return counter;
        
        /*
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new HashSet<Association>();
        for(Layer l : visibleLayers){
            Iterator<Association> c=l.getTopicMap().getAssociations();
            while(c.hasNext()){
                Association a=c.next();
                LayeredTopic lt=getLayeredTopic(a.getType(),layeredTopics);
                LayeredAssociation la=new LayeredAssociation(this,lt);
                for(Topic role : a.getRoles()){
                    LayeredTopic lrole=getLayeredTopic(role,layeredTopics);
                    Topic player=a.getPlayer(role);
                    LayeredTopic lplayer=getLayeredTopic(player,layeredTopics);
                    la.addLayeredPlayer(lplayer, lrole);
                }
                associations.add(la);
            }
        }        
        return associations.size();*/
    }
    
    @Override
    public Topic copyTopicIn(Topic t,boolean deep) throws TopicMapException {
        Topic ct=selectedLayer.topicMap.copyTopicIn(t,deep);
        return makeLayeredTopic(ct);
    }
    
    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        Association ca=selectedLayer.topicMap.copyAssociationIn(a);
        return makeLayeredAssociation(ca);
    }
    
    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        selectedLayer.topicMap.copyTopicAssociationsIn(t);
    }
    
    @Override
    public void importXTM(java.io.InputStream in, TopicMapLogger logger) throws java.io.IOException,TopicMapException {
        selectedLayer.topicMap.importXTM(in, logger);
    }
    
    @Override
    public void importLTM(java.io.InputStream in, TopicMapLogger logger) throws java.io.IOException, TopicMapException {
        selectedLayer.topicMap.importLTM(in, logger);
    }
    
    @Override
    public void importLTM(java.io.File in) throws java.io.IOException, TopicMapException {
        selectedLayer.topicMap.importLTM(in);
    }    
    @Override
    public void mergeIn(TopicMap tm)  throws TopicMapException {
        selectedLayer.topicMap.mergeIn(tm);
    }
    
    @Override
    public boolean trackingDependent(){
        return trackDependent;
    }
    
    @Override
    public void setTrackDependent(boolean v){
        trackDependent=v;
    }
    
    @Override
    public List<TopicMapListener> getTopicMapListeners(){
        return topicMapListeners;
    }

    @Override
    public void addTopicMapListener(TopicMapListener listener){
        if(!topicMapListeners.contains(listener)) topicMapListeners.add(listener);
    }
    
    @Override
    public void removeTopicMapListener(TopicMapListener listener){
        topicMapListeners.remove(listener);
    }
    
    @Override
    public void disableAllListeners(){
        if(disabledListeners==null){
            disabledListeners=topicMapListeners;
            topicMapListeners=new ArrayList<TopicMapListener>();
        }
    }
    
    @Override
    public void enableAllListeners(){
        if(disabledListeners!=null){
            topicMapListeners=disabledListeners;
            disabledListeners=null;
        }
    }
    
/*    public TopicMapListener setTopicMapListener(TopicMapListener listener){
        TopicMapListener old=topicMapListener;
        topicMapListener=listener;
        return old;        
    }*/
    
    @Override
    public boolean resetTopicMapChanged() throws TopicMapException {
        boolean ret=false;
        for(Layer l : visibleLayers){
            ret|=l.getTopicMap().resetTopicMapChanged();
        }
        return ret;
    }
    
    @Override
    public boolean isTopicMapChanged() throws TopicMapException {
        for(Layer l : visibleLayers){
            if(l.getTopicMap().isTopicMapChanged()) return true;
        }
        return false;
    }
    
    
    // ---------------------------------------------- TOPIC MAP TRANSACTIONS ---
    
    @Override
    public void startTransaction() {
        for(Layer l : visibleLayers){
            l.getTopicMap().startTransaction();
        }
    }
    
    @Override
    public void endTransaction() {
        for(Layer l : visibleLayers){
            l.getTopicMap().endTransaction();
        }
    }
    
    
    
    
    
    
    
    public class TopicAndLayerKeyMaker implements Delegate<String,Topic> {
        public String invoke(Topic t){
            String lname=getLayer(t).getName();
            String min=null;
            try{
                for(Locator l : t.getSubjectIdentifiers()){
                    String s=l.toExternalForm();
                    if(min==null) min=s;
                    else if(s.compareTo(min)<0) min=s;
                }
                return lname+"//"+min;
            }
            catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
                return lname;
            }
        }
    }
    
    
    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options)  throws TopicMapException {
        TopicMapSearchOptions options2=options.duplicate();
        options2.maxResults=-1; // we can't know yet how many results we need from individual topic maps
        
        HashSet<Topic> searchResult = new LinkedHashSet<Topic>();
        HashSet<Topic> searchResultLayered = new LinkedHashSet<Topic>();
        Outer: for(Layer l : visibleLayers) {
            searchResult.clear();
            searchResult.addAll(l.getTopicMap().search(query, options2));
            for(Topic t : searchResult){
                searchResultLayered.add(this.getTopic(t.getOneSubjectIdentifier()));
                if(options.maxResults>=0 && searchResultLayered.size()>=options.maxResults) break Outer;
            }
        }
        return searchResultLayered;
    }
    
    
    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        if(options == null) return null;
        int option = options.getOption();
        switch(option) {
            case TopicMapStatOptions.NUMBER_OF_TOPICS: {
                return new TopicMapStatData(getNumTopics());
            }
            case TopicMapStatOptions.NUMBER_OF_TOPIC_CLASSES: {
                HashSet typeIndex = new HashSet();
                Iterator<Topic> topicIter=this.getTopics();
                Topic t = null;
                Topic type = null;
                while(topicIter.hasNext()) {
                    t = topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        Collection<Topic> types = t.getTypes();
                        if(types != null && !types.isEmpty()) {
                            for(Iterator<Topic> iter = types.iterator(); iter.hasNext(); ) {
                                type = iter.next();
                                if(type != null && !type.isRemoved()) {
                                    typeIndex.add(type);
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(typeIndex.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATIONS: {
                return new TopicMapStatData(getNumAssociations());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_PLAYERS: {
                HashSet associationPlayers = new HashSet();
                Iterator topicIter=this.getTopics();
                Topic t = null;
                Collection associations = null;
                Collection associationRoles = null;
                Iterator associationIter = null;
                Iterator associationRoleIter = null;
                Association association = null;
                Topic role = null;
                while(topicIter.hasNext()) {
                    t=(Topic) topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        associations = t.getAssociations();
                        if(associations != null && !associations.isEmpty()) {
                            associationIter = associations.iterator();
                            while(associationIter.hasNext()) {
                                association = (Association) associationIter.next();
                                if(association != null && !association.isRemoved()) {
                                    associationRoles = association.getRoles();
                                    if(associationRoles != null) {
                                        associationRoleIter = associationRoles.iterator();
                                        while(associationRoleIter.hasNext()) {
                                            role = (Topic) associationRoleIter.next();
                                            associationPlayers.add(association.getPlayer(role));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(associationPlayers.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_ROLES: {
                HashSet associationRoles = new HashSet();
                Iterator topicIter=this.getTopics();
                Topic t = null;
                Collection associations = null;
                Iterator associationIter = null;
                Association association = null;
                while(topicIter.hasNext()) {
                    t=(Topic) topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        associations = t.getAssociations();
                        if(associations != null && !associations.isEmpty()) {
                            associationIter = associations.iterator();
                            while(associationIter.hasNext()) {
                                association = (Association) associationIter.next();
                                if(association != null && !association.isRemoved()) {
                                    associationRoles.addAll( association.getRoles() );
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(associationRoles.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_TYPES: {
                HashSet typeIndex = new HashSet();
                Iterator<Association> aIter=this.getAssociations();
                Association a = null;
                Topic t = null;
                while(aIter.hasNext()) {
                    a = aIter.next();
                    if(a != null && !a.isRemoved()) {
                        t = a.getType();
                        if(t != null && !t.isRemoved()) {
                            typeIndex.add(t);
                        }
                    }
                }
                return new TopicMapStatData(typeIndex.size());
            }
            case TopicMapStatOptions.NUMBER_OF_BASE_NAMES: {
                HashSet nameIndex = new HashSet();
                Iterator<Topic> topicIter=this.getTopics();
                Topic t = null;
                while(topicIter.hasNext()) {
                    t = topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        if(t.getBaseName() != null) {
                            nameIndex.add(t.getBaseName());
                        }
                    }
                }
                return new TopicMapStatData(nameIndex.size());
            }
            case TopicMapStatOptions.NUMBER_OF_OCCURRENCES: {
                int count=0;
                Iterator topicIter=this.getTopics();
                Topic t = null;
                Collection dataTypes = null;
                while(topicIter.hasNext()) {
                    t=(Topic) topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        dataTypes = t.getDataTypes();
                        if(dataTypes != null) count += dataTypes.size();
                    }
                }
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_IDENTIFIERS: {
                HashSet siIndex = new HashSet();
                Iterator<Topic> topicIter=this.getTopics();
                Topic t = null;
                Collection<Locator> sis = null;
                while(topicIter.hasNext()) {
                    t = topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        sis = t.getSubjectIdentifiers();
                        if(sis != null) {
                            for(Iterator<Locator> iter2=sis.iterator(); iter2.hasNext(); ) {
                                if(iter2 != null) {
                                    siIndex.add(iter2.next().toExternalForm());
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(siIndex.size());
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_LOCATORS: {
                int count = 0;
                Iterator<Topic> topicIter=this.getTopics();
                Topic t = null;
                Locator sl = null;
                while(topicIter.hasNext()) {
                    t = topicIter.next();
                    if(t != null && !t.isRemoved()) {
                        sl = t.getSubjectLocator();
                        if(sl != null) {
                            count++;
                        }
                    }
                }
                return new TopicMapStatData(count);
            }
        }
        return new TopicMapStatData();
    }
    
    
    
    public static void main(String[] args) throws Exception {
        // Note: this is far from exhaustive test
        Topic t,t2,t3,a,b;
        LayerStack ls=new LayerStack();
        Layer l1=new Layer(new org.wandora.topicmap.memory.TopicMapImpl(),"Layer 1",ls);
        Layer l2=new Layer(new org.wandora.topicmap.memory.TopicMapImpl(),"Layer 2",ls);
        Layer l3=new Layer(new org.wandora.topicmap.memory.TopicMapImpl(),"Layer 3",ls);
        ls.addLayer(l1);
        ls.addLayer(l2);
        ls.addLayer(l3);
        ls.selectLayer(l1);
        t=ls.createTopic();
        t.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi1"));
        t=ls.createTopic();
        t.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi3"));
        ls.selectLayer(l2);
        t=ls.createTopic();
        t.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi2"));
        t=ls.createTopic();
        t.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi3"));
        t.setBaseName("basename");
        
        ls.selectLayer(l3);
        t3=ls.createTopic();
        t3.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi3"));
        t=ls.createTopic();
        t.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi1"));
        t.addType(t3);
        t2=ls.createTopic();
        t2.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testi2"));
        t2.addType(t3);
        a=ls.createTopic();
        a.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testia"));
        b=ls.createTopic();
        b.addSubjectIdentifier(new Locator("http://wandora.org/si/testi/testib"));
        Association a1=ls.createAssociation(t3);
        a1.addPlayer(t, a);
        a1.addPlayer(t2,b);
        HashSet<Topic> scope=new HashSet<Topic>();
        scope.add(a); scope.add(b);
        t.setVariant(scope, "variant");
        

        int counter=1;
        Topic test;
        String s;
        Collection<Topic> c;
        Collection<Association> d;
        
        l3.setVisible(false);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi1"));
        System.out.println("Test "+(counter++)+" "+(test==null?"failed":"passed"));
        c=test.getTypes();
        System.out.println("Test "+(counter++)+" "+(c.size()!=0?"failed":"passed"));
        d=test.getAssociations();
        System.out.println("Test "+(counter++)+" "+(d.size()!=0?"failed":"passed"));
        l1.setVisible(false);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi1"));
        System.out.println("Test "+(counter++)+" "+(test!=null?"failed":"passed"));
        l1.setVisible(true);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi1"));
        System.out.println("Test "+(counter++)+" "+(test==null?"failed":"passed"));
        l1.setVisible(false);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi2"));
        System.out.println("Test "+(counter++)+" "+(test==null?"failed":"passed"));
        c=test.getTypes();
        System.out.println("Test "+(counter++)+" "+(!c.isEmpty()?"failed":"passed"));
        l2.setVisible(false);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi2"));
        System.out.println("Test "+(counter++)+" "+(test!=null?"failed":"passed"));
        l1.setVisible(true);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi3"));
        System.out.println("Test "+(counter++)+" "+(test==null?"failed":"passed"));
        s=test.getBaseName();
        System.out.println("Test "+(counter++)+" "+(s!=null?"failed":"passed"));
        l1.setVisible(false);
        l2.setVisible(true);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi3"));
        System.out.println("Test "+(counter++)+" "+(test==null?"failed":"passed"));
        s=test.getBaseName();
        System.out.println("Test "+(counter++)+" "+(s==null || !s.equals("basename")?"failed":"passed"));
        l1.setVisible(false);
        l2.setVisible(false);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi3"));
        System.out.println("Test "+(counter++)+" "+(test!=null?"failed":"passed"));
        l3.setVisible(true);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi1"));
        System.out.println("Test "+(counter++)+" "+(test==null?"failed":"passed"));
        c=test.getTypes();
        System.out.println("Test "+(counter++)+" "+(c.size()!=1?"failed":"passed"));        
        d=test.getAssociations();
        System.out.println("Test "+(counter++)+" "+(d.size()!=1?"failed":"passed"));
        t3=ls.getTopic(new Locator("http://wandora.org/si/testi/testi3"));
        d=test.getAssociations(t3);
        System.out.println("Test "+(counter++)+" "+(d.size()!=1?"failed":"passed"));
        a=ls.getTopic(new Locator("http://wandora.org/si/testi/testia"));
        b=ls.getTopic(new Locator("http://wandora.org/si/testi/testib"));
        d=test.getAssociations(t3,a);
        System.out.println("Test "+(counter++)+" "+(d.size()!=1?"failed":"passed"));
        a1=d.iterator().next();
        System.out.println("Test "+(counter++)+" "+(a1.getPlayer(a)==null?"failed":"passed"));
        System.out.println("Test "+(counter++)+" "+(a1.getPlayer(b)==null?"failed":"passed"));
        d=test.getAssociations(a);
        System.out.println("Test "+(counter++)+" "+(!d.isEmpty()?"failed":"passed"));
        System.out.println("Test "+(counter++)+" "+(test.getVariantScopes().size()!=1?"failed":"passed"));
        scope=new HashSet<Topic>();
        scope.add(a); scope.add(b);
        s=test.getVariant(scope);
        System.out.println("Test "+(counter++)+" "+(s==null?"failed":"passed"));
        l1.setVisible(true);
        l2.setVisible(true);
        test=ls.getTopic(new Locator("http://wandora.org/si/testi/testi3"));
        c=ls.getTopicsOfType(test);
        System.out.println("Test "+(counter++)+" "+(c.size()!=2?"failed":"passed"));                
        
    }
}
