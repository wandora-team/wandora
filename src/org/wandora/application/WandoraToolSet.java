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
 * WandoraToolSet.java
 *
 * Created on 17.2.2009, 16:00
 *
 */


package org.wandora.application;

import java.io.*;
import java.util.*;
import javax.swing.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;




/**
 *
 * @author akivela
 */

    
public class WandoraToolSet implements Serializable {
    private int index = 0;
    private String name = null;
    private ArrayList tools = new ArrayList();
    private Wandora wandora = null;
    
    
    
    

    public WandoraToolSet(String n, Wandora w) {
        this.name = n;
        this.wandora = w;
    }
    public WandoraToolSet(String n, int i, Wandora w) {
        this.name = n;
        this.index = i;
        this.wandora = w;
    }

    
    public void add(Object o) {
        if(o instanceof ToolItem || o instanceof WandoraToolSet) {
            tools.add(o);
        }
    }
    
    public void add(Object o, int index) {
        if(o instanceof ToolItem || o instanceof WandoraToolSet) {
            tools.add(index, o);
        }
    }
    
    public void add(String name, WandoraTool t, int index) {
        tools.add(index, new ToolItem(name, t));
    }
    
    public void add(String name, WandoraTool t) {
        tools.add(new ToolItem(name, t));
    }



    
    public String getName() {
        return name;
    }
    public void setName(String n) {
        this.name = n;
    }
    public int getIndex() {
        return index;
    }
    @Override
    public String toString() {
        return name;
    }
    public int size() {
        return tools.size();
    }

    public String getNameForTool(WandoraTool tool) {
        return getNameForTool(tool, tools);
    }
    public String getNameForTool(WandoraTool tool, ArrayList set) {
        for(Object o : set) {
            if(o != null) {
                if(o instanceof ToolItem) {
                    ToolItem t = (ToolItem) o;
                    if(tool.equals(t.getTool())) {
                        return t.getName();
                    }
                }
                else if(o instanceof WandoraToolSet) {
                    WandoraToolSet ts = (WandoraToolSet) o;
                    String n = ts.getNameForTool(tool);
                    if(n != null) return n;
                }
            }
        }
        return null;
    }
    
    
    public Object getToolWrapperWithHash(int hash) {
        for(Object o : tools) {
            if(o != null) {
                if(o instanceof ToolItem) {
                    if(hash == o.hashCode()) {
                        return o;
                    }
                }
                else if(o instanceof WandoraToolSet) {
                    WandoraToolSet ts = (WandoraToolSet) o;
                    Object s = ts.getToolWrapperWithHash(hash);
                    if(s != null) return s;
                }
            }
        }
        if(hash == this.hashCode()) return this;
        return null;
    }

    
    public WandoraTool getToolForRealName(String name) {
        return getToolForRealName(name, tools);
    }
    public WandoraTool getToolForRealName(String name, ArrayList set) {
        for(Object o : set) {
            if(o != null) {
                if(o instanceof ToolItem) {
                    ToolItem t = (ToolItem) o;
                    if(name.equals(t.getTool().getName())) {
                        return t.getTool();
                    }
                }
                else if(o instanceof WandoraToolSet) {
                    WandoraToolSet ts = (WandoraToolSet) o;
                    WandoraTool t = ts.getToolForRealName(name);
                    if(t != null) return t;
                }
            }
        }
        return null;
    }
    
    

    public WandoraTool getToolForName(String name) {
        return getToolForName(name, tools);
    }
    public WandoraTool getToolForName(String name, ArrayList set) {
        for(Object o : set) {
            if(o != null) {
                if(o instanceof ToolItem) {
                    ToolItem t = (ToolItem) o;
                    if(name.equals(t.getName())) {
                        return t.getTool();
                    }
                }
                else if(o instanceof WandoraToolSet) {
                    WandoraToolSet ts = (WandoraToolSet) o;
                    WandoraTool t = ts.getToolForName(name);
                    if(t != null) return t;
                }
            }
        }
        return null;
    }

    
    

    public ArrayList getTools() {
        return tools;
    }

    
    public boolean remove(Object toolOrSet) {
        if(toolOrSet == null) return false;
        for(Object o : tools) {
            if(o != null) {
                if(toolOrSet.equals(o)) {
                    tools.remove(o);
                    return true;
                }
                else if(o instanceof WandoraToolSet) {
                    WandoraToolSet ts = (WandoraToolSet) o;
                    boolean removed = ts.remove(toolOrSet);
                    if(removed) return true;
                }
            }
        }
        return false;
    }
    

    public JMenu getMenu(JMenu toolMenu) {
        return getMenu(toolMenu, this);
    }


    public JMenu getMenu(JMenu toolMenu, WandoraToolSet toolSet) {
        toolMenu.removeAll();
        if(toolSet == null) return toolMenu;
        for(Object o : toolSet.getTools()) {
            if(o instanceof WandoraToolSet) {
                WandoraToolSet subTools = (WandoraToolSet) o;
                if(subTools.size() > 0) {
                    JMenu subMenu = new SimpleMenu(subTools.getName());
                    getMenu(subMenu, subTools);
                    toolMenu.add(subMenu);
                }
            }
            else if(o instanceof ToolItem) {
                ToolItem wrappedTool = (ToolItem) o;
                String toolName = wrappedTool.getName();
                if(toolName.startsWith("---")) {
                    toolMenu.add(new JSeparator());
                }
                else {
                    WandoraTool tool = wrappedTool.getTool();
                    SimpleMenuItem item = tool.getToolMenuItem(wandora, toolName);
                    item.setToolTipText(Textbox.makeHTMLParagraph(tool.getDescription(), 40));
                    toolMenu.add(item);
                }
            }
        }
        return toolMenu;
    }


    public Object[] getAsObjectArray() {
        return getAsObjectArray(this, null);
    }
    public Object[] getAsObjectArray(ToolFilter filter) {
        Object[] array = getAsObjectArray(this, filter);
        return array;
    }
    public Object[] getAsObjectArray(WandoraToolSet toolSet) {
        return getAsObjectArray(toolSet, null);
    }

    public Object[] getAsObjectArray(WandoraToolSet toolSet, ToolFilter filter) {
        ArrayList array = new ArrayList();
        if(toolSet == null) return array.toArray();
        boolean previousWasSeparator = true;
        for(Object o : toolSet.getTools()) {
            if(o instanceof WandoraToolSet) {
                WandoraToolSet subTools = (WandoraToolSet) o;
                if(subTools.size() > 0) {
                    Object[] subArray = getAsObjectArray(subTools, filter);
                    if(subArray != null && subArray.length > 0) {
                        array.add(subTools.getName());
                        array.add(subArray);
                        previousWasSeparator = false;
                    }
                }
            }
            else if(o instanceof ToolItem) {
                ToolItem toolItem = (ToolItem) o;
                String toolName = toolItem.getName();
                if(toolName.startsWith("---")) {
                    if(!previousWasSeparator) {
                        array.add(toolName);
                    }
                    previousWasSeparator = true;
                }
                else {
                    WandoraTool tool = toolItem.getTool();
                    if(filter == null || filter.acceptTool(tool)) {
                        array.add(toolName);
                        array.add(filter == null ? tool : filter.polishTool(tool));
                        previousWasSeparator = false;
                        if(filter != null) {
                            Object[] extra = filter.addAfterTool(tool);
                            if(extra != null && extra.length > 0) {
                                array.addAll(Arrays.asList(extra));
                            }
                        }
                    }
                }
            }
        }
        return array.toArray();
    }
    
    
    public Object[] asArray() {
        return tools.toArray();
    }



    public HashMap<String,WandoraTool> getAsHash() {
        return getAsHash(this, null);
    }
    public HashMap<String,WandoraTool> getAsHash(ToolFilter filter) {
        HashMap<String,WandoraTool> hash = getAsHash(this, filter);
        return hash;
    }
    public HashMap<String,WandoraTool> getAsHash(WandoraToolSet toolSet) {
        return getAsHash(toolSet, null);
    }

    public HashMap<String,WandoraTool> getAsHash(WandoraToolSet toolSet, ToolFilter filter) {
        LinkedHashMap hash = new LinkedHashMap();
        if(toolSet == null) return hash;
        for(Object o : toolSet.getTools()) {
            if(o instanceof WandoraToolSet) {
                WandoraToolSet subTools = (WandoraToolSet) o;
                if(subTools.size() > 0) {
                    HashMap subHash = getAsHash(subTools, filter);
                    if(subHash != null && !subHash.isEmpty()) {
                        hash.putAll(subHash);
                    }
                }
            }
            else if(o instanceof ToolItem) {
                ToolItem toolItem = (ToolItem) o;
                WandoraTool tool = toolItem.getTool();
                if(filter == null || filter.acceptTool(tool)) {
                    String toolName = toolItem.getName();
                    hash.put(toolName, tool);
                }
            }
        }
        return hash;
    }


    
    // -------------------------------------------------------------------------
    
    
    public class ToolFilter {
        public boolean acceptTool(WandoraTool tool) { return true; }
        public WandoraTool polishTool(WandoraTool tool) { return tool; }
        public Object[] addAfterTool(WandoraTool tool) { return null; }
    }


    
    // -------------------------------------------------------------------------
    
    
    
    
    public class ToolItem implements Serializable {
        private String name = null;
        private WandoraTool tool = null;

        public ToolItem(String n, WandoraTool t) {
            this.name = n;
            this.tool = t;
        }

        public String getName() {
            return name;
        }

        public void setName(String n) {
            this.name = n;
        }
        
        public WandoraTool getTool() {
            return tool;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}