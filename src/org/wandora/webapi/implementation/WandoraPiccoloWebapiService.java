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
 * WandoraPiccoloWebapiService.java
 *
 * Created on 2. toukokuuta 2007, 14:29
 *
 */

package org.wandora.webapi.implementation;
import org.apache.axis.MessageContext;
import javax.servlet.http.HttpServlet;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * <p>
 * The org.wandora.webapi package contains all classes
 * needed by the Wandora web api. The definition subpackage contains the defining
 * classes. From these classes a wsdl document is created using the java2wsdl
 * tool in Apache Axis. After that, stubs for server implementation can be created
 * using the wsdl2java tool. These classes are in the implementation subpackage.
 * The classes in definition subpackage aren't actually used for anything else
 * than creating the server stubs, they aren't referenced anywhere in java code
 * outside the definition subpackage.
 * <p>
 * </p>
 * All classes in implementation subpackage
 * except this class are automatically created by the wsdl2java tool and will
 * be overwritten if web api is changed and the stubs are created again. The
 * deplay.wsdd is also automatically created and contains webservice definition
 * for the Axis servlet. Contents of this file should be copied in the 
 * server-config.wsdd in WEB-INF directory of the web application.
 * </p>
 * <p>
 * This class contains the actual functionality for the Wandora web api. The Apache Axis
 * servlet uses the automatically generated WandoraServiceSoapBindingImpl class which directly
 * calls this class. Having all functionality in this class avoids the possibility of
 * accidentally overwriting all the functionality with an automatically created stub class
 * when web api is updated and the stubs are recreated.
 * </p>
 * <p>
 * If the web api needs to be updated, changes should be made to the classes in the
 * definition subpackage. After that create the wsdl document and then the server
 * stubs and place them in the implementation subpackage. Then WandoraServiceSoapBindingImpl
 * class needs to be modified to call implementations in this class. Best way to
 * do this is to have a private variable containing an instance of this class and
 * which is initialized in the constructor. Then call the methods in that variable
 * in each web api method.
 * </p>
 * <p>
 * To use Wandora Service, an Axis servlet must be started and configured to use the
 * wandora web service. Also, this class needs the topic map that is used to retrieve
 * all the information. This topic map is passed in a servlet context attribute with
 * identifier "org.wandora.PiccoloWandoraTopicMap". This
 * can be done in the piccoloconfig.xml file with following lines.
 * </p>
 * <pre><code>
&lt;servletcontext xp:id="servletcontext" xp:idref="servlet" xp:method="getServletContext"/>
&lt;servletcontext xp:idref="servletcontext" xp:method="setAttribute">
    &lt;param>org.wandora.PiccoloWandoraTopicMap&lt;/param>
&lt;param xp:idref="wandora" xp:method="getTopicMap"/>
&lt;/servletcontext>
</code></pre>

 * @author olli
 */
public class WandoraPiccoloWebapiService implements org.wandora.webapi.implementation.WandoraService_PortType{
    
    /** Creates a new instance of WandoraPiccoloWebapiService */
    public WandoraPiccoloWebapiService() {        
    }
    protected TopicMap getTopicMap(){
        HttpServlet servlet=(HttpServlet)MessageContext.getCurrentContext().getProperty(org.apache.axis.transport.http.HTTPConstants.MC_HTTP_SERVLET);
        TopicMap tm=(TopicMap)servlet.getServletContext().getAttribute("org.wandora.PiccoloWandoraTopicMap");
        return tm;
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
        
        WSTopic ret=new WSTopic(full,t.getBaseName(),variantTypes,variantLanguages,variantNames,sl,sis,types,was,occs.toArray(new WSOccurrence[occs.size()]));
        return ret;
    }
    
    public org.wandora.webapi.implementation.WSTopic getTopic(String si,boolean full) throws java.rmi.RemoteException {
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
    
    public org.wandora.webapi.implementation.WSTopic[] getTopics(String[] sis,boolean full) throws java.rmi.RemoteException {
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

    public org.wandora.webapi.implementation.WSTopic getTopicWithBaseName(String si,boolean full) throws java.rmi.RemoteException {
        TopicMap tm=getTopicMap();
        try{
            Topic t=tm.getTopicWithBaseName(si);
            if(t!=null) return makeWSTopic(t,full);
            else return null;
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    public org.wandora.webapi.implementation.WSTopic[] getTopicsOfType(String in0,boolean full) throws java.rmi.RemoteException {
        TopicMap tm=getTopicMap();
        try{
            Collection<Topic> ts=tm.getTopicsOfType(in0);
            if(ts!=null) return makeWSTopics(ts,full);
            else return null;
        } catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    
}
