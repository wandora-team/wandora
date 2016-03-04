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
 * Created on Oct 19, 2011, 8:12:21 PM
 */


package org.wandora.application.gui.topicpanels.treemap;

import java.util.ArrayList;


/**
 *
 * @author elias, akivela
 */


public class TreeModel {
    private MapItem mapItem;
    private MapItem[] childItems;
    private MapItem[] cachedTreeItems; // we assume tree structure doesn't change.
    private TreeModel[] cachedLeafModels;
    private TreeModel parent;
    private ArrayList children=new ArrayList();
    private boolean sumsChildren;

    public TreeModel() {
        this.mapItem=new MapItem();
        sumsChildren=true;
    }

    public TreeModel(MapItem mapItem) {
        this.mapItem=mapItem;
    }


    public void setOrder(int order) {
        mapItem.setOrder(order);
    }

    public TreeModel[] getLeafModels() {
        if(cachedLeafModels!=null)
            return cachedLeafModels;
        ArrayList v=new ArrayList();
        addLeafModels(v);
        int n=v.size();
        TreeModel[] m=new TreeModel[n];
        v.toArray(m);
        cachedLeafModels=m;
        return m;
    }

    private ArrayList addLeafModels(ArrayList v) {
        if(!hasChildren()) {
            System.err.println("Somehow tried to get child model for leaf!!!");
            return v;
        }
        if(!getChild(0).hasChildren())
            v.add(this);
        else
            for (int i=childCount()-1; i>=0; i--)
                getChild(i).addLeafModels(v);
        return v;
    }
    

    public int depth() {
        if(parent==null) return 0;
        return 1+parent.depth();
    }

    public void layout(StripTreeMap tiling) {
        layout(tiling, mapItem.getBounds());
    }

    public void layout(StripTreeMap tiling, Rect bounds) {
        mapItem.setBounds(bounds);
        if(!hasChildren()) return;
        double s=sum();
        tiling.layout(this, bounds);
        for (int i=childCount()-1; i>=0; i--)
            getChild(i).layout(tiling);
    }

    public MapItem[] getTreeItems() {
        if(cachedTreeItems!=null)
            return cachedTreeItems;

        ArrayList v=new ArrayList();
        addTreeItems(v);
        int n=v.size();
        MapItem[] m=new MapItem[n];
        v.toArray(m);
        cachedTreeItems=m;
        return m;
    }

    private void addTreeItems(ArrayList v) {
        if(!hasChildren())
            v.add(mapItem);
        else
            for(int i=childCount()-1; i>=0; i--)
                getChild(i).addTreeItems(v);
    }

    private double sum() {
        if(!sumsChildren)
            return mapItem.getSize();
        double s=0;
        for(int i=childCount()-1; i>=0; i--)
            s+=getChild(i).sum();
        mapItem.setSize(s);
        return s;
    }

    public MapItem[] getItems() {
        if(childItems!=null) return childItems;
        int n=childCount();
        childItems=new MapItem[n];
        for(int i=0; i<n; i++) {
            childItems[i]=getChild(i).getMapItem();
            childItems[i].setDepth(1+depth());
        }
        return childItems;
    }

    public MapItem getMapItem() {
        return mapItem;
    }

    public void addChild(TreeModel child) {
        child.setParent(this);
        children.add(child);
        childItems=null;
    }

    public void setParent(TreeModel parent) {
        for(TreeModel p=parent; p!=null; p=p.getParent())
            if(p==this) throw new IllegalArgumentException("Circular ancestry!");
        this.parent=parent;
    }

    public TreeModel getParent() {
        return parent;
    }

    public int childCount() {
        return children.size();
    }

    public TreeModel getChild(int n) {
        return (TreeModel)children.get(n);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void print() {
        print("");
    }

    private void print(String prefix) {
        System.out.println(prefix+"size="+mapItem.getSize());
        for(int i=0; i<childCount(); i++)
            getChild(i).print(prefix+"..");
    }
}





