/* 
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
package org.wandora.topicmap.layered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.undowrapper.UndoTopicMap;

/**
 *
 * @author olli
 */
public abstract class ContainerTopicMap extends TopicMap {

    protected List<ContainerTopicMapListener> containerListeners;

    public ContainerTopicMap() {
        containerListeners = new ArrayList<ContainerTopicMapListener>();
    }


    public List<ContainerTopicMapListener> getContainerListeners() {
        return containerListeners;
    }


    public void addContainerListener(ContainerTopicMapListener listener) {
        if (!containerListeners.contains(listener))
            containerListeners.add(listener);
    }


    public void removeContainerListener(ContainerTopicMapListener listener) {
        containerListeners.remove(listener);
    }


    public void addContainerListeners(List<ContainerTopicMapListener> listeners) {
        for (ContainerTopicMapListener l : listeners) {
            addContainerListener(l);
        }
    }


    protected void fireLayerAdded(Layer layer) {
        for (ContainerTopicMapListener l : containerListeners) {
            l.layerAdded(layer);
        }
    }


    protected void fireLayerRemoved(Layer layer) {
        for (ContainerTopicMapListener l : containerListeners) {
            l.layerRemoved(layer);
        }
    }


    protected void fireLayerStructureChanged() {
        for (ContainerTopicMapListener l : containerListeners) {
            l.layerStructureChanged();
        }
    }


    protected void fireLayerChanged(Layer oldLayer, Layer newLayer) {
        for (ContainerTopicMapListener l : containerListeners) {
            l.layerChanged(oldLayer, newLayer);
        }
    }


    public void fireLayerVisibilityChanged(Layer layer) {
        for (ContainerTopicMapListener l : containerListeners) {
            l.layerVisibilityChanged(layer);
        }
    }


    /**
     * Gets the number of layers in this container. Default implementation
     * return getLayers().size().
     */
    public int getNumLayers() {
        return getLayers().size();
    }


    public abstract Collection<Topic> getTopicsForLayer(Layer l, Topic t);


    public Topic getTopicForLayer(Layer l, Topic t) {
        Collection<Topic> c = getTopicsForLayer(l, t);
        if (c == null || c.isEmpty())
            return null;
        else
            return c.iterator().next();
    }


    public Collection<Topic> getTopicsForTreeLayer(Layer l, Topic t) {
        ContainerTopicMap tm = (ContainerTopicMap) t.getTopicMap();
        List<Layer> path = getTreeLayerPath(l, tm);
        Set<Topic> topics = new LinkedHashSet<>();
        topics.add(t);
        for (Layer p : path) {
            Set<Topic> nextTopics = new LinkedHashSet<>();
            for (Topic to : topics) {
                nextTopics.addAll(tm.getTopicsForLayer(p, to));
            }
            TopicMap nexttm = p.getTopicMap();
            if (nexttm instanceof ContainerTopicMap) { // if not instanceof, for loop will terminate
                tm = (ContainerTopicMap) nexttm;
            }
            topics = nextTopics;
            if (topics.isEmpty()) {
                break;
            }
        }
        return topics;
    }


    public boolean getTreeLayerPath(Layer l, ContainerTopicMap root, List<Layer> stack) {
        List<Layer> layers = root.getLayers();
        for (Layer layer : layers) {
            stack.add(layer);
            if (layer == l) {
                return true;
            }
            else {
                TopicMap tm = layer.getTopicMap();
                if (tm instanceof ContainerTopicMap containerTopicMap) {
                    if (getTreeLayerPath(l, containerTopicMap, stack)) {
                        return true;
                    }
                }
            }
            stack.remove(stack.size() - 1);
        }
        return false;
    }


    /**
     * Finds a layer tree path from root to the given layer. Returned path
     * is a list of layers that must be followed to reach the given layer. Return
     * value will not include a layer for the root. If no path was found, i.e.
     * given layer is not in the layer tree or not under the given root map,
     * returns null.
     */
    public List<Layer> getTreeLayerPath(Layer l, ContainerTopicMap root) {
        List<Layer> ret = new ArrayList<>();
        if (getTreeLayerPath(l, root, ret)) {
            return ret;
        }
        else {
            return null;
        }
    }


    /**
     * Gets all topics in leaf layer tree layers that merge with the given
     * topic.
     */
    public Collection<Topic> getTopicsForAllLeafLayers(Topic t) {
        List<Layer> leaves = getLeafLayers();
        List<Topic> ret = new ArrayList<>();
        for (Layer l : leaves) {
            ret.addAll(getTopicsForTreeLayer(l, t));
        }
        return ret;
    }


    /**
     * Gets the leaf layer that the specifiec topic belongs to. That is, the given
     * topic should not be a layered topic of this topic map but a topic of one of
     * the leaf layers of the layer tree of which this topic map is the root.
     */
    public Layer getLeafLayer(Topic t) {
        for (Layer l : getLeafLayers()) {
            if (t.getTopicMap() == l.getTopicMap()) {
                return l;
            }
        }
        return null;
    }


    public Topic getTopicForSelectedTreeLayer(Topic t) {
        Collection<Topic> c = getTopicsForSelectedTreeLayer(t);
        if (c == null || c.isEmpty()) {
            return null;
        }
        else {
            return c.iterator().next();
        }
    }


    /**
     * Get all topics in the selected tree layer topic map that merge with the
     * given topic. See getSelectedTreeLayer about the definition of
     * selected tree layer.
     */
    public Collection<Topic> getTopicsForSelectedTreeLayer(Topic t) {
        Layer sl = getSelectedLayer();
        if (sl == null) {
            return new ArrayList<Topic>();
        }
        Collection<Topic> ts = getTopicsForLayer(sl, t);
        if (sl.getTopicMap() instanceof ContainerTopicMap containerTopicMap && ts.size() > 0) {
            Set<Topic> ret = new LinkedHashSet<>();
            for (Topic t2 : ts) {
                ret.addAll(containerTopicMap.getTopicsForSelectedTreeLayer(t2));
            }
            return ret;
        }
        else
            return ts;
    }


    public void getLeafLayers(List<Layer> list) {
        for (Layer l : getLayers()) {
            if (l.getTopicMap() instanceof ContainerTopicMap containerTopicMap) {
                containerTopicMap.getLeafLayers(list);
            }
            else {
                list.add(l);
            }
        }
    }


    /**
     * Returns all leaf layers in the layer tree. Leaf layers are layers that
     * are not container topic maps.
     */
    public List<Layer> getLeafLayers() {
        List<Layer> ret = new ArrayList<>();
        getLeafLayers(ret);
        return ret;
    }


    public Layer getTreeLayer(TopicMap tm) {
        for (Layer l : getTreeLayers()) {
            TopicMap ltm = l.getTopicMap();
            if (ltm != null) {
                if (ltm == tm) {
                    return l;
                }
                if (ltm instanceof UndoTopicMap undoTopicMap) {
                    TopicMap iltm = undoTopicMap.getWrappedTopicMap();
                    if (iltm == tm) {
                        return l;
                    }
                }
            }
        }
        return null;
    }


    public Layer getTreeLayer(String name) {
        for (Layer l : getTreeLayers()) {
            if (l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }


    public void getTreeLayers(List<Layer> list) {
        for (Layer l : getLayers()) {
            list.add(l);
            if (l.getTopicMap() instanceof ContainerTopicMap containerTopicMap) {
                containerTopicMap.getTreeLayers(list);
            }
        }
    }


    /**
     * Gets all layers in the layer tree. This includes this container topic mapand
     * any layers under possible other container topic maps in any of the layers of this
     * topic map.
     */
    public List<Layer> getTreeLayers() {
        List<Layer> ret = new ArrayList<>();
        getTreeLayers(ret);
        return ret;
    }


    /**
     * Gets the selected layer in the layer tree. If topic map in selected layer in this
     * topic map is another container topic map, then returns the selected tree layer
     * in that container. Otherwise returns the selected layer in this container.
     * In other words, recursively follows selected layers in container topic maps as
     * deep as possible. 
     * 
     * Note that the returned layer is not necessarily the same user has selected in the
     * layer tree GUI. This will be the case when user selects a nonleaf layer, that is
     * a container topic map.
     */
    public Layer getSelectedTreeLayer() {
        Layer l = getSelectedLayer();
        if (l == null)
            return null;
        if (l.getTopicMap() instanceof ContainerTopicMap containerTopicMap) {
            return containerTopicMap.getSelectedTreeLayer();
        }
        else
            return l;
    }


    /**
     * Gets all layers in the order they are in the container.
     */
    public abstract List<Layer> getLayers();

    public abstract void notifyLayersChanged();

    /**
     * Gets layer position in the container. Layer with index 0 is at the top.
     */
    public abstract int getLayerZPos(Layer l);


    /**
     * Adds a layer at the bottom of the container.
     */
    public void addLayer(Layer l) {
        addLayer(l, getNumLayers());
    }


    /**
     * Sets layer in the specified position removing old layer at that position.
     */
    public abstract void setLayer(Layer l, int pos);

    /**
     * Inserts a layer at the specified position in the container. Layers after
     * that index are moved one position down.
     */
    public abstract void addLayer(Layer l, int index);

    /**
     * Removes the specified layer. Layers after the removed layer are
     * moved one position up.
     */
    public abstract boolean removeLayer(Layer l);


    /**
     * Moves the specified layer in another position. Moves other layers to
     * make room and to remove the resulting empty position.
     */
    public void moveLayer(Layer l, int pos) {
        if (getNumLayers() < pos) {
            pos = getNumLayers();
        }
        if (removeLayer(l)) {
            addLayer(l, pos);
        }
    }


    /**
     * Gets the layer with the specified name.
     */
    public abstract Layer getLayer(String name);

    /**
     * Gets the selected layer.
     */
    public abstract Layer getSelectedLayer();

    /**
     * Gets the selected layer position in the stack.
     */
    public abstract int getSelectedIndex();

    /**
     * Makes the specified layer the selected layer.
     */
    public abstract void selectLayer(Layer layer);

    public abstract void reverseLayerOrder();

    /**
     * Gets all visible layers in the order they are in the container.
     */
    public abstract List<Layer> getVisibleLayers();


}
