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
 * DnDHelper.java
 *
 * Created on August 12, 2004, 11:15 AM
 */



package org.wandora.application.gui;


import java.awt.Component;
import org.wandora.application.gui.table.TopicTable;
import java.io.IOException;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.*;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.table.TopicTableModel;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;



/**
 *
 * @author olli, akivela
 */
public class DnDHelper {
    public static final DataFlavor associationDataFlavor;
    public static final DataFlavor associationArrayDataFlavor;
    public static final DataFlavor topicDataFlavor;
    public static final DataFlavor topicArrayDataFlavor;
    public static final DataFlavor topicGridDataFlavor;
    static {
        DataFlavor a=null,aa=null,t=null,ta=null,tg=null;
        try{
            a=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Association.class.getName()+"\"");
            aa=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Association[].class.getName()+"\"");
            t=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Topic.class.getName()+"\"");
            ta=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Topic[].class.getName()+"\"");
            tg=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Topic[][].class.getName()+"\"");
        }
        catch(ClassNotFoundException cnfe){cnfe.printStackTrace();}
        associationDataFlavor=a;
        associationArrayDataFlavor=aa;
        topicDataFlavor=t;
        topicArrayDataFlavor=ta;
        topicGridDataFlavor=tg;
    }

    public static WandoraTransferable makeTopicTransferable(GetTopicButton button){
        DnDHelper.WandoraTransferable ret = makeTopicTransferable(button.getTopic(), button.getCopyString());
        return ret;
    }
    
    public static WandoraTransferable makeTopicTransferable(MixedTopicTable table){
        DnDHelper.WandoraTransferable ret = makeTopicTransferable(table.getSelectedValues(), table.getCopyString());
        return ret;
    }
    
    public static WandoraTransferable makeTopicTableTransferable(TopicTable table){
        DnDHelper.WandoraTransferable ret = makeTopicTransferable(table.getSelectedTopics(), table.getCopyString());
        return ret;
    }
    
    public static WandoraTransferable makeTopicTransferable(TopicGrid grid){
        DnDHelper.WandoraTransferable ret=new DnDHelper.WandoraTransferable(grid.getSelectedTopicsNormalized());
        ret.setStringData(grid.getCopyString());
        return ret;
    }
    
    public static WandoraTransferable makeTopicTransferable(Topic[] topics, String str) {
        DnDHelper.WandoraTransferable ret=new DnDHelper.WandoraTransferable(topics);
        ret.setStringData(str);
        return ret;
    }
    
    public static WandoraTransferable makeTopicTransferable(Topic topic, String str) {
        DnDHelper.WandoraTransferable ret=new DnDHelper.WandoraTransferable(topic);
        ret.setStringData(str);
        return ret;
    }
    

    public static WandoraTransferable makeTopicTransferable(Object[][] objects, String str) {
        ArrayList<Topic> selected=new ArrayList<Topic>();
        Object o = null;
        try {
            for(int y=0; y<objects.length; y++) {
                for(int x=0; x<objects[y].length; x++) {
                    o = objects[y][x];
                    if(o != null) {
                        if(o instanceof Topic) {
                            selected.add((Topic) o);
                        }
                    }
                }
            }
        }
        catch(Exception tme){tme.printStackTrace();}
        DnDHelper.WandoraTransferable ret=new DnDHelper.WandoraTransferable(selected);
        ret.setStringData(str);
        return ret;
    }

    

    public static WandoraTransferable makeTopicTransferable(TopicTable table, int[] rows, int[] columns){
        ArrayList<Topic> selected=new ArrayList<Topic>();
        StringBuilder sb=new StringBuilder();
        Topic t = null;
        try {
            for(int y=0; y<rows.length; y++) {
                if(y!=0) sb.append("\n");
                for(int x=0; x<columns.length; x++) {
                    t = table.getTopicAt(y,x);
                    System.out.println("transferable at "+x+", "+y);
                    selected.add(t);
                    if(x!=0) sb.append("\t");
                    if(t != null && !t.isRemoved()) {
                        sb.append(t.getBaseName());
                    }
                    else {
                        sb.append("");
                    }
                }
            }
        }
        catch(TopicMapException tme){tme.printStackTrace();}
        DnDHelper.WandoraTransferable ret=new DnDHelper.WandoraTransferable(selected);
        ret.setStringData(sb.toString());
        return ret;
    }


    public static ArrayList<Topic> getTopicList(TransferHandler.TransferSupport support,TopicMap tm,boolean create) throws TopicMapException {
        return getTopicList(support.getTransferable(), tm, create);
    }


    public static ArrayList<Topic> getTopicList(Transferable transferable,TopicMap tm,boolean create) throws TopicMapException {
        try{
            ArrayList<Topic> topics=null;
            if(transferable.isDataFlavorSupported(DnDHelper.topicArrayDataFlavor)){
                Topic[] ts=(Topic[])transferable.getTransferData(DnDHelper.topicArrayDataFlavor);
                topics=new ArrayList<Topic>();
                for(int i=0;i<ts.length;i++) topics.add(ts[i]);
            }
            else if(transferable.isDataFlavorSupported(DnDHelper.topicDataFlavor)){
                Topic t=(Topic)transferable.getTransferData(DnDHelper.topicDataFlavor);
                topics=new ArrayList<Topic>();
                topics.add(t);
            }
            else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)){
                String data=(String)transferable.getTransferData(DataFlavor.stringFlavor);
                String[] split=data.split("\n");
                topics=new ArrayList<Topic>();
                for(int i=0;i<split.length;i++){
                    split[i]=split[i].trim();
                    if(split[i].length()==0) continue;
                    Topic t=null;
                    if(create){
                        if(split[i].startsWith("http:")){
                            t=TMBox.getOrCreateTopic(tm, split[i]);
                        }
                        else{
                            t=TMBox.getOrCreateTopicWithBaseName(tm, split[i]);
                        }
                    }
                    else{
                        if(split[i].startsWith("http:")){
                            t=tm.getTopic(split[i]);
                        }
                        else{
                            t=tm.getTopicWithBaseName(split[i]);
                        }
                    }
                    if(t!=null) topics.add(t);
                }
            }
            return topics;
        }
        catch(UnsupportedFlavorException ufe){ufe.printStackTrace();}
        catch(IOException ioe){ioe.printStackTrace();}
        return null;
    }
           
    public static class GenericTransferable implements Transferable {
        protected DataFlavor[] flavorsA;
        protected ArrayList<DataFlavor> flavors;
        protected ArrayList<Object> data;
        
        public GenericTransferable(){
            this.flavors=new ArrayList<DataFlavor>();
            this.data=new ArrayList<Object>();
        }

        public void addData(DataFlavor flavor,Object data){
            flavors.add(flavor);
            flavorsA=null;
            this.data.add(data);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            for(int i=0;i<flavors.size();i++){
                if(flavors.get(i).equals(flavor)) return data.get(i);
            }
            throw new UnsupportedFlavorException(flavor);
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            if(flavorsA==null) flavorsA=flavors.toArray(new DataFlavor[0]);
            return flavorsA;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for(int i=0;i<flavors.size();i++){
                if(flavors.get(i).equals(flavor)) return true;
            }
            return false;
        }
        
    }

    
    
    public static class WandoraTransferable implements Transferable {

        protected Topic topic;
        protected Topic[] topics;
        protected Topic[][] grid;
        protected Association association;
        protected Association[] associations;
        protected String stringData;
        
        protected DataFlavor[] supportedFlavors;
        
        public WandoraTransferable(){
        }
        public WandoraTransferable(Topic t){
            setTopic(t);
            makeTopicListStringData();
        }
        public WandoraTransferable(Topic[] t){
            setTopics(t);
            makeTopicListStringData();
        }
        public WandoraTransferable(Topic[][] t){
            setTopics(t);
            makeTopicListStringData();
        }
        public WandoraTransferable(Collection<Topic> t){
            this(t.toArray(new Topic[t.size()]));
        }
        public WandoraTransferable(Association a){
            setAssociation(a);
            getTopicsFromAssociationsData();
        }
        public WandoraTransferable(Association[] a){
            setAssociations(a);
            getTopicsFromAssociationsData();
        }
        
        
        
        public void setTopic(Topic t){
            topic=t;
            if(topics==null) topics=new Topic[]{topic};
            supportedFlavors=null;
        }
        
        public void setTopics(Topic[] t){
            topics=t;
            if(topic==null && topics!=null && topics.length>0) topic=topics[0];
            supportedFlavors=null;
        }
        public void setTopics(Topic[][] t){
            grid=t;
            ArrayList<Topic> tt = new ArrayList<Topic>();
            for(int i=0; i<t.length; i++) {
                for(int j=0; j<t[i].length; j++) {
                    tt.add(t[i][j]);
                }
            }
            topics = tt.toArray( new Topic[] {} );
            if(topic==null && topics!=null && topics.length>0) topic=topics[0];
            supportedFlavors=null;
        }
        
        public void setAssociation(Association a){
            association=a;
            if(associations==null) associations=new Association[]{association};
            supportedFlavors=null;
        }
        
        public void setAssociations(Association[] a){
            associations=a;
            if(association==null && associations!=null && associations.length>0) association=associations[0];
            supportedFlavors=null;
        }
        
        public void makeTopicListStringData(){
            StringBuilder sb=new StringBuilder();
            try{
                if(topics!=null){
                    for(int i=0;i<topics.length;i++){
                        if(topics[i] != null) {
                            sb.append(topics[i].getBaseName());
                        }
                        sb.append("\n");
                    }
                }
                else if(topic!=null) stringData=topic.getBaseName()+"\n";
            }catch(TopicMapException tme){tme.printStackTrace();}            
            stringData=sb.toString();
            supportedFlavors=null;
        }
        public void setStringData(String s){
            stringData=s;
            supportedFlavors=null;
        }
        
        public void getTopicsFromAssociationsData(){
            try{
                if(topic==null && association!=null){
                    topic=association.getPlayer(association.getRoles().iterator().next());
                }
                if(topics==null){
                    if(associations!=null){
                        ArrayList<Topic> ts=new ArrayList<Topic>();
                        for(Association a : associations){
                            for(Topic role : a.getRoles()){
                                ts.add(a.getPlayer(role));
                            }
                        }
                        topics=ts.toArray(new Topic[ts.size()]);
                    }
                    else if(association!=null){
                        topics=new Topic[association.getRoles().size()];
                        int counter=0;
                        for(Topic role : association.getRoles()){
                            topics[counter++]=association.getPlayer(role);
                        }
                    }
                }
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
            }
            supportedFlavors=null;
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(flavor.equals(associationArrayDataFlavor)){
                if(associations==null) throw new UnsupportedFlavorException(flavor);
                return associations;
            }
            else if(flavor.equals(associationDataFlavor)){
                if(association==null) throw new UnsupportedFlavorException(flavor);
                return association;
            }
            else if(flavor.equals(topicGridDataFlavor)){
                if(grid==null) throw new UnsupportedFlavorException(flavor);
                return grid;                
            }
            else if(flavor.equals(topicArrayDataFlavor)){
                if(topics==null) throw new UnsupportedFlavorException(flavor);
                return topics;                
            }
            else if(flavor.equals(topicDataFlavor)){
                if(topic==null) throw new UnsupportedFlavorException(flavor);
                return topic;                
            }
            else if(flavor.equals(DataFlavor.stringFlavor)){
                if(stringData==null) throw new UnsupportedFlavorException(flavor);
                return stringData;                
            }
            else throw new UnsupportedFlavorException(flavor);
        }
        
        public void updateFlavors(){
            ArrayList<DataFlavor> dfs=new ArrayList<DataFlavor>();
            if(associations!=null) dfs.add(associationArrayDataFlavor);
            if(association!=null) dfs.add(associationDataFlavor);
            if(grid!=null) dfs.add(topicGridDataFlavor);
            if(topics!=null) dfs.add(topicArrayDataFlavor);
            if(topic!=null) dfs.add(topicDataFlavor);
            if(stringData!=null) dfs.add(DataFlavor.stringFlavor);
            supportedFlavors=dfs.toArray(new DataFlavor[dfs.size()]);
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            if(supportedFlavors==null) updateFlavors();
            return supportedFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if(supportedFlavors==null) updateFlavors();
            for(int i=0;i<supportedFlavors.length;i++){
                if(supportedFlavors[i].equals(flavor)) return true;
            }
            return false;
        }
        
    }
    
}
