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
 */
package org.wandora.application.server.topicmapservice;

import org.wandora.application.Wandora;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import java.util.*;

import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.Constants;

/**
 *
 * @author olli
 */
public class TopicMapService {
    public static final String ROOT_NAME="Root stack";
    public static String layerName;
    public static Wandora wandora;
    public static TopicMap tm;

    protected TopicMap getTopicMap(){
        if(tm==null){
            if(layerName.equals(ROOT_NAME)) tm=wandora.getTopicMap();
            else {
                Layer layer=wandora.getTopicMap().getTreeLayer(layerName);
                if(layer!=null) tm=layer.getTopicMap();
                else tm=null;
            }
        }
        return tm;
    }

    public void setOutputType(String outputType){
        if(outputType!=null){
            try{
                MessageContext outMsgCtx = MessageContext.getCurrentMessageContext().getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                outMsgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, outputType);
            }catch(Exception e){e.printStackTrace();}
        }
    }

    private WSTopic[] makeWSTopics(Collection<Topic> ts,boolean full) throws TopicMapException {
        WSTopic[] ret=new WSTopic[ts.size()];
        int i=0;
        for(Topic t : ts){
            ret[i]=makeWSTopic(t,full);
            i++;
        }
        return ret;
    }

    private String getLanguageSI(Topic t) throws TopicMapException {
        for(Locator l : t.getSubjectIdentifiers()){
            if(l.toString().startsWith(XTMPSI.LANG_PREFIX) || l.toString().equals(XTMPSI.LANG_INDEPENDENT)) return l.toString();
        }
        return null;
    }
    private String getMinSI(Topic t) throws TopicMapException {
        String min=null;
        for(Locator l : t.getSubjectIdentifiers()) {
            if(min==null) min=l.toString();
            else if(min.compareTo(l.toString())>0) min=l.toString();
        }
        return min;
    }

    private WSTopic makeWSTopic(Topic t,boolean full) throws TopicMapException {
        int i;
        WSAssociation[] was=new WSAssociation[0];
        ArrayList<WSOccurrence> occs=new ArrayList<WSOccurrence>();
        if(full){
            was=new WSAssociation[t.getAssociations().size()];
            i=0;
            for(Association a : t.getAssociations()){
                WSPlayer[] players=new WSPlayer[a.getRoles().size()];
                int j=0;
                for(Topic r : a.getRoles()){
                    players[j]=new WSPlayer(r.getOneSubjectIdentifier().toString(),a.getPlayer(r).getOneSubjectIdentifier().toString());
                    j++;
                }
                was[i]=new WSAssociation(a.getType().getOneSubjectIdentifier().toString(),players);
                i++;
            }

//            occs=new ArrayList<WSOccurrence>();
            for(Topic ot : t.getDataTypes()){
                Hashtable<Topic,String> data=t.getData(ot);
                for(Map.Entry<Topic,String> e : data.entrySet()){
                    occs.add(new WSOccurrence(ot.getOneSubjectIdentifier().toString(),e.getKey().getOneSubjectIdentifier().toString(),e.getValue()));
                }
            }
        }

        String sl=null;
        if(t.getSubjectLocator()!=null) sl=t.getSubjectLocator().toString();

        String[] sis=new String[t.getSubjectIdentifiers().size()];
        i=0;
        for(Locator l : t.getSubjectIdentifiers()){
            sis[i]=l.toString();
            i++;
        }

        String[] types=new String[t.getTypes().size()];
        i=0;
        for(Topic type : t.getTypes()){
            types[i]=type.getOneSubjectIdentifier().toString();
            i++;
        }

        HashMap<String,HashMap<String,String>> variants=new HashMap<String,HashMap<String,String>>();
        for(Set<Topic> s : t.getVariantScopes()){
            if(s.size()!=2) continue;
            Iterator<Topic> iter=s.iterator();
            Topic st1=iter.next();
            Topic st2=iter.next();
            String si2=getLanguageSI(st2);
            boolean swap=false;
            if(si2==null) {
                si2=getLanguageSI(st1);
                if(si2==null){
                    si2=getMinSI(st2);
                    if(variants.get(si2)==null){
                        si2=getMinSI(st1);
                        swap=true;
                    }
                }
                else swap=true;
            }
            if(swap){
                Topic temp=st1;
                st1=st2;
                st2=temp;
            }
            String si1=getMinSI(st1);
            HashMap<String,String> m=variants.get(si1);
            if(m==null){
                m=new HashMap<String,String>();
                variants.put(si1,m);
            }
            m.put(si2,t.getVariant(s));
        }
        String[] variantTypes=new String[variants.size()];
        String[][] variantLanguages=new String[variants.size()][];
        String[][] variantNames=new String[variants.size()][];
        i=0;
        for(String si1 : variants.keySet()){
            variantTypes[i]=si1;
            i++;
        }
        for(i=0;i<variantTypes.length;i++){
            HashMap<String,String> variants2=variants.get(variantTypes[i]);
            variantLanguages[i]=new String[variants2.size()];
            variantNames[i]=new String[variants2.size()];
            int j=0;
            for(Map.Entry<String,String> e : variants2.entrySet()){
                variantLanguages[i][j]=e.getKey();
                variantNames[i][j]=e.getValue();
                j++;
            }
        }
        String bn=t.getBaseName();
        if(bn==null) bn="null";
        WSTopic ret=new WSTopic(full,bn,variantTypes,variantLanguages,variantNames,sl,sis,types,was,occs.toArray(new WSOccurrence[occs.size()]));
        return ret;
    }

    public WSTopic getTopic(String si,boolean full,String outputType)  {
        System.out.println("getTopic(\""+si+"\","+full+","+outputType+")");
        setOutputType(outputType);
        TopicMap tm=getTopicMap();
        try{
            Topic t=tm.getTopic(si);
            if(t!=null) return makeWSTopic(t,full);
            else return null;
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    public WSTopic[] getTopics(String[] sis,boolean full,String outputType)  {
        System.out.println("getTopics(#"+sis.length+","+full+","+outputType+")");
        setOutputType(outputType);
        TopicMap tm=getTopicMap();
        try{
            WSTopic[] ret=new WSTopic[sis.length];
            int i=0;
            for(String si : sis){
                Topic t=tm.getTopic(si);
                if(t!=null) ret[i]=makeWSTopic(t,full);
                else ret[i]=null;
                i++;
            }
            return ret;
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    public WSTopic getTopicWithBaseName(String name,boolean full,String outputType)  {
        System.out.println("getTopicWithBaseName(\""+name+"\","+full+","+outputType+")");
        setOutputType(outputType);
        TopicMap tm=getTopicMap();
        try{
            Topic t=tm.getTopicWithBaseName(name);
            if(t!=null) return makeWSTopic(t,full);
            else return null;
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    public WSTopic getTopicWithSubjectLocator(String sl,boolean full,String outputType){
        System.out.println("getTopicWithSubjectLocator(\""+sl+"\","+full+","+outputType+")");
        setOutputType(outputType);
        TopicMap tm=getTopicMap();
        try{
            Topic t=tm.getTopicBySubjectLocator(sl);
            if(t!=null) return makeWSTopic(t,full);
            else return null;
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    public WSTopic[] getTopicsOfType(String si,boolean full,String outputType)  {
        System.out.println("getTopicsOfType(\""+si+"\","+full+","+outputType+")");
        setOutputType(outputType);
        TopicMap tm=getTopicMap();
        try{
            Collection<Topic> ts=tm.getTopicsOfType(si);
            if(ts!=null) return makeWSTopics(ts,full);
            else return new WSTopic[0];
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    public WSTopic[] getAllTopics(boolean full,String outputType) {
        System.out.println("getAllTopics("+full+","+outputType+")");
        setOutputType(outputType);
        TopicMap tm=getTopicMap();
        try{
            Iterator<Topic> iter=tm.getTopics();
            ArrayList<WSTopic> ret=new ArrayList<WSTopic>();
            while(iter.hasNext()){
                Topic t=iter.next();
                ret.add(makeWSTopic(t,full));
            }
            return ret.toArray(new WSTopic[ret.size()]);
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }

    }

}
