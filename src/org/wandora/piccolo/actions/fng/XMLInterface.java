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
 */

package org.wandora.piccolo.actions.fng;



import org.wandora.utils.GripCollections;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.Application;
import org.wandora.piccolo.User;
import org.wandora.query.IsOfTypeDirective;
import org.wandora.topicmap.Locator;
import org.wandora.query.IsTopicDirective;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.query.ResultRow;
import org.wandora.topicmap.Topic;
import org.wandora.query.CountDirective;
import org.wandora.query.ContextIsOfTypeDirective;
import org.wandora.query.SortDirective;
import org.wandora.query.QueryContext;
import org.wandora.query.JoinDirective;
import org.wandora.query.SelectDirective;
import org.wandora.query.RolesDirective;
import org.wandora.query.Directive;
import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.query.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import org.w3c.dom.*;

import static org.wandora.piccolo.actions.fng.XMLInterfaceSIs.*;

/**
 *
 * @author olli
 */
public class XMLInterface implements Action {
    

    
    public static final Map<String,String> typeTagMap = GripCollections.addArrayToMap(new HashMap<String,String>(),new Object[]{
        artworkSI,"artwork",
        artistSI,"artist",
        collectionSI,"collection",
        museumSI,"museum",
        iconclassSI,"iconclass",
        keywordSI,"keyword",
        timeSI,"time",
        placeSI,"place",
        techniqueSI,"technique",
        techniqueTextSI,"technique",
        materialMuusaSI,"material",
        usageSI,"usage",
        officialUsageSI,"officialUsage",
        artworkClassSI,"artworkClass",
        officialProfessionSI,"officialProfession",
        routeSI,"route",
        routePageSI,"routePage",
        orderSI,"order",
        artistGroupSI,"artistGroup",
        tekstidokumenttiSI,"tekstidokumentti",
        tekstintyyppiSI,"tekstintyyppi",
        textSI,"text",
        imageSI,"image",
        imageTypeSI,"imageType",
        tekstidokumenttiSI,"textDocument",
        "","topic"
    });
    
    public static final int LEVEL_BN=0;
    public static final int LEVEL_BN_SI=1;
    public static final int LEVEL_STUB=2;
    public static final int LEVEL_DEFAULT=3;
    public static final int LEVEL_COMPLETE=4;

/*    public static final Map<String,Integer> associationLevelMap = GripCollections.addArrayToMap(new HashMap<String,Integer>(),new Object[]{
        imageSI,LEVEL_STUB
    });
    
    public static final Map<String,AssociationInfo> associationInfoMap = GripCollections.addArrayToMap(new HashMap<String,AssociationInfo>(),new Object[]{
        personBirthSI,new AssociationInfo("birth",personSI,placeSI,timeSI),
        personDeathSI,new AssociationInfo("death",personSI,placeSI,timeSI),
        authorSI,new AssociationInfo("author",LEVEL_STUB,artworkSI,artistSI),
        techniqueSI,new AssociationInfo("technique",artworkSI,techniqueSI),
        groupSI,new AssociationInfo("artistGroup",groupSI,groupMemberSI),
    });*/
    
    public static HashMap<Locator,String> makeRoleTagMap(Object ... params){
        HashMap<Locator,String> ret=new HashMap<Locator,String>();
        for(int i=0;i+1<params.length;i+=2){
            if(params[i] instanceof Locator)
                ret.put((Locator)params[i],(String)params[i+1]);
            else
                ret.put(new Locator((String)params[i]),(String)params[i+1]);
        }
        return ret;
    }
    
    public static final ArrayList<AssociationInfo> associationInfosAuthor = GripCollections.arrayToCollection(new ArrayList<AssociationInfo>(),new AssociationInfo[]{
        new AssociationInfo("author",new SortDirective(new SelectDirective(authorSI,artworkSI,artistSI),artworkSI))
    });
    public static final ArrayList<AssociationInfo> associationInfos = GripCollections.arrayToCollection(new ArrayList<AssociationInfo>(),new AssociationInfo[]{
        new AssociationInfo("birth",new SelectDirective(personBirthSI,personSI,placeSI,timeSI)),
        new AssociationInfo("death",new SelectDirective(personDeathSI,personSI,placeSI,timeSI)),
        new AssociationInfo("authorcount",new ContextIsOfTypeDirective(new CountDirective(new SelectDirective(authorSI,artworkSI,artistSI),authorSI+"count",countSI),artistSI)),
        new AssociationInfo("author",new ContextIsOfTypeDirective(new SelectDirective(authorSI,artworkSI,artistSI),artworkSI)),
        new AssociationInfo("artistGroup",new IsOfTypeDirective(new SelectDirective(groupSI,groupSI,groupMemberSI),groupSI,artistGroupSI)),
        new AssociationInfo("nationality",new IsTopicDirective(new JoinDirective(
                                            new SelectDirective(groupSI,groupSI,groupMemberSI),groupSI,
                                            new SelectDirective(genericTypeSI,"~"+genericTypeSI,"~"+genericTypeCarrierSI)
                                          ),"~"+genericTypeSI,nationalitySI)),
        new AssociationInfo("textDocument",new SelectDirective(taiteilijaviiteSI,artistSI,tekstidokumenttiSI)),
        
        new AssociationInfo("publishingContext",new SelectDirective(julkaisukontekstiSI,julkaisukontekstiSI,textSI,timeSI)),
        new AssociationInfo("generalHeading",new SelectDirective(tekstinyleisotsikkoSI,tekstinyleisotsikkoSI,textSI)),
        
        new AssociationInfo("image",new ContextIsOfTypeDirective(new SelectDirective(imageSI,imageSI,imageTypeSI,targetSI),artworkSI,true)),
        new AssociationInfo("image",new ContextIsOfTypeDirective(new SelectDirective(imageSI,imageSI,imageTypeSI,targetSI),artworkSI),LEVEL_STUB),
        new AssociationInfo("technique",new SortDirective(new SelectDirective(techniqueSI,artworkSI,techniqueSI,"~"+orderSI),"~"+orderSI),makeRoleTagMap(techniqueSI,"technique")),
        new AssociationInfo("material",new SortDirective(new SelectDirective(materialSI,artworkSI,materialSI,"~"+orderSI),"~"+orderSI),makeRoleTagMap(materialSI,"material")),
        new AssociationInfo("time",new SelectDirective(timeSI,artworkSI,timeSI)),
        new AssociationInfo("keeper",new SelectDirective(keeperSI,artworkSI,keeperSI)),
        new AssociationInfo("type",new RolesDirective(new JoinDirective(
                                        new SelectDirective(genericTypeSI,genericTypeSI,genericTypeCarrierSI),genericTypeSI,
                                        new SelectDirective(linkedProfessionsSI,professionSI,officialProfessionSI)
                                   ),genericTypeCarrierSI,officialProfessionSI)),
        new AssociationInfo("collection",new SelectDirective(collectionSI,artworkSI,collectionSI)),
        new AssociationInfo("aqcuisition",new SelectDirective(aqcuisitionSI,aqcuisitionSI,artworkSI,timeSI)),
        new AssociationInfo("iconclass",new SelectDirective(iconclassSI,artworkSI,iconclassSI)),
        new AssociationInfo("usage",new SelectDirective(usageSI,artworkSI,usageSI)),
        new AssociationInfo("route",new SelectDirective(routePageATypeSI,routeSI,routePageSI,orderSI)),
        new AssociationInfo("routeArtwork",new SelectDirective(routePageArtworkSI,artworkSI,orderSI,routePageSI)),
    });
    
    public static final ArrayList<OccurrenceInfo> occurrenceInfos = GripCollections.arrayToCollection(new ArrayList<OccurrenceInfo>(),new OccurrenceInfo[]{
        new OccurrenceInfo("text",textOccSI),
        new OccurrenceInfo("routeText",routePageTextSI),
    });
    
    public static final Map<String,TopicNameCleaner> topicNameCleaners;
    static {
        Map<String,TopicNameCleaner> temp = null;
        try{
            temp = GripCollections.addArrayToMap(new HashMap<String,TopicNameCleaner>(),new Object[]{
                techniqueTextSI,new RegexNameCleaner("^tekniikka(teksti)?\\s+(.*)$","$2"),
                materialMuusaSI,new RegexNameCleaner("^(pohja)?materiaali\\s+(.*)$","$2"),
            });
        }
        catch(PatternSyntaxException pse){pse.printStackTrace();}
        topicNameCleaners = temp;
    }
    
    public static final Map<String,RequestProfile> requestProfiles;
    static {
        Map<String,RequestProfile> temp = null;
        try{
            temp = GripCollections.addArrayToMap(new HashMap<String,RequestProfile>(),new Object[]{
                "default",new RequestProfile(associationInfos,occurrenceInfos),
                "artworks",new RequestProfile(associationInfosAuthor,new ArrayList<OccurrenceInfo>()),
            });
        }
        catch(PatternSyntaxException pse){pse.printStackTrace();}
        requestProfiles = temp;        
    }

    public static final String header="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    public static final String openXML="<result>\n";
    public static final String closeXML="</result>\n";

    
    protected Logger logger;
    
    /** Creates a new instance of XMLInterface */
    public XMLInterface() {
    }

    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        WandoraManager manager=(WandoraManager)application.getService("WandoraManager");
        if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
            try{

                response.setContentType("application/xml");
                response.setCharacterEncoding("UTF-8");
                
                String lang = request.getParameter("lang");
                if(lang==null) lang="en";
                TopicMap tm = manager.getTopicMap();
                
                String profileS = request.getParameter("profile");
                RequestProfile profile=null;
                if(profileS==null) profile=requestProfiles.get("default");
                else profile=requestProfiles.get(profileS);
                
                int start=0;
                int limit=-1;
                String startS=request.getParameter("start");
                if(startS!=null) start=Integer.parseInt(startS);
                String limitS=request.getParameter("limit");
                if(limitS!=null) limit=Integer.parseInt(limitS);
                
                
                XMLOut out=new XMLOut(new PrintWriter(new OutputStreamWriter(response.getOutputStream(),"UTF-8")),tm,lang);
                
                out.openResponse();

                out.outputTopicBaseName(request.getParameter("id"),profile,start,limit);
                out.outputTopicBaseName(request.getParameter("id0"),profile,start,limit);
                out.outputTopicBaseName(request.getParameter("id1"),profile,start,limit);
                for(int i=2;true;i++){
                    String id=request.getParameter("id"+i);
                    if(id==null) break;
                    out.outputTopicBaseName(id,profile,start,limit);
                }
                
                out.outputTopicSI(request.getParameter("si"),profile,start,limit);
                for(int i=0;true;i++){
                    String id=request.getParameter("si"+i);
                    if(id==null) break;
                    out.outputTopicSI(id,profile,start,limit);
                }

                out.closeResponse();
                out.close();
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally{
                manager.releaseTopicMap(WandoraManager.LOCK_READ);
            }
        }
    }
    
    public static <E> ArrayList<E> combineLists(ArrayList<E> list, E ... items){
        ArrayList<E> ret=new ArrayList<E>();
        ret.addAll(list);
        for(int i=0;i<items.length;i++){
            ret.add(items[i]);
        }
        return ret;
    }
    public static <E> ArrayList<E> combineLists(ArrayList<E> ... lists){
        ArrayList<E> ret=new ArrayList<E>();
        for(ArrayList<E> l : lists){
            ret.addAll(l);
        }
        return ret;
    }
    
    static class RequestProfile {
        public ArrayList<AssociationInfo> associationInfos;
        public ArrayList<OccurrenceInfo> occurrenceInfos;
        public RequestProfile(ArrayList<AssociationInfo> associationInfos,ArrayList<OccurrenceInfo> occurrenceInfos){
            this.associationInfos=associationInfos;
            this.occurrenceInfos=occurrenceInfos;
        }
    }
    
    static interface TopicNameCleaner {
        public String clean(String name);
    }
    
    static class RegexNameCleaner implements TopicNameCleaner {
        public Pattern pattern;
        public String replace;
        public RegexNameCleaner(String pattern,String replace) throws PatternSyntaxException {
            this.pattern=Pattern.compile(pattern);
            this.replace=replace;
        }
        public String clean(String name){
            Matcher m=pattern.matcher(name);
            return m.replaceAll(replace);
        }
    }
    
    static class AssociationInfo {
        public Directive query;
        public String associationTag;
        public int level;
        
        public HashMap<Locator,String> playerTags;
        
        public AssociationInfo(String associationTag, Directive query){
            this(associationTag,query,LEVEL_COMPLETE,new HashMap<Locator,String>());
        }
        public AssociationInfo(String associationTag, Directive query, int level){
            this(associationTag,query,level,new HashMap<Locator,String>());
        }
        public AssociationInfo(String associationTag, Directive query,HashMap<Locator,String> playerTags){
            this(associationTag,query,LEVEL_COMPLETE,playerTags);            
        }
        public AssociationInfo(String associationTag, Directive query, int level,HashMap<Locator,String> playerTags){
            this.associationTag=associationTag;
            this.query=query;
            this.level=level;
            this.playerTags=playerTags;
        }
    }
    
    static class OccurrenceInfo {
        public String occurrenceTag;
        public String si;
        public int level;
        
        public OccurrenceInfo(String occurrenceTag,String si,int level){
            this.occurrenceTag=occurrenceTag;
            this.si=si;
            this.level=level;
        }
        public OccurrenceInfo(String occurrenceTag,String si){
            this(occurrenceTag,si,LEVEL_COMPLETE);
        }
    }
    
    class XMLOut {        
        
        private PrintWriter out;
        private TopicMap tm;
        private String lang;
        public XMLOut(PrintWriter out,TopicMap tm,String lang){
            this.out=out;
            this.tm=tm;
            this.lang=lang;
        }
        public void close() throws IOException {out.close();}
        public void openResponse() throws IOException {
            out.print(header);
            out.print(openXML);
        }
        public void closeResponse() throws IOException {
            out.print(closeXML);
        }
        
        public TopicNameCleaner getTopicNameCleaner(Topic topic) throws TopicMapException {
            for(Map.Entry<String,TopicNameCleaner> e : topicNameCleaners.entrySet()){
                String keySI=e.getKey();
                Topic key=tm.getTopic(keySI);
                if(key!=null && topic.isOfType(key)) return e.getValue();
            }
            return null;
        }
        public String getTopicTag(Topic topic) throws TopicMapException {
            if(topic!=null){
                for(Map.Entry<String,String> e : typeTagMap.entrySet()){
                    String keySI=e.getKey();
                    if(keySI.length()==0) continue;
                    Topic key=tm.getTopic(keySI);
                    if(topic.isOfType(key)) return e.getValue();
                }
            }
            return typeTagMap.get("");
        }
        public String idEscape(String s){
            return ""+s.hashCode();
        }
        public String attributeEscape(String s){
            s=s.replace("&","&amp;");
            s=s.replace("\"","&quot;");
            return s;
        }
        public String textEscape(String s){
            s=s.replace("&","&amp;");
            s=s.replace("<","&lt;");
            return s;
        }
        
        public String getTopicName(Topic topic) throws TopicMapException {
            return getTopicName(topic,lang);
        }
        public String getTopicName(Topic topic,String lang) throws TopicMapException {
            TopicNameCleaner cleaner=getTopicNameCleaner(topic);
            String name=topic.getDisplayName(lang);
            if(cleaner!=null) name=cleaner.clean(name);
            return name;
        }
        
        public void outputTopicBaseName(String topicID,RequestProfile profile) throws IOException, TopicMapException {
            outputTopicBaseName(topicID,profile,0,-1);
        }
        public void outputTopicBaseName(String topicID,RequestProfile profile,int start,int limit) throws IOException, TopicMapException {
            if(topicID==null) return;
            Topic topic = tm.getTopicWithBaseName(topicID);
            if(topic==null) return;
            outputTopic(topic,profile,LEVEL_COMPLETE,"\t",null,null,start,limit);      
        }
        public void outputTopicSI(String topicSI,RequestProfile profile) throws IOException, TopicMapException {
            outputTopicSI(topicSI,profile,0,-1);
        }
        public void outputTopicSI(String topicSI,RequestProfile profile,int start,int limit) throws IOException, TopicMapException {
            if(topicSI==null) return;
            Topic topic = tm.getTopic(topicSI);
            if(topic == null) {
                topic = tm.getTopicWithBaseName(topicSI);
            }
            if(topic == null) return;
            outputTopic(topic,profile,LEVEL_COMPLETE,"\t",null,null,start,limit);      
        }
        
        public void outputTopic(Topic topic,RequestProfile profile) throws IOException, TopicMapException {
            outputTopic(topic,profile,LEVEL_COMPLETE,"\t",null);
        }
        public void outputTopic(Topic topic,RequestProfile profile,int level,String indent) throws IOException, TopicMapException {
            outputTopic(topic,profile,level,indent,null);
        }
        public void outputTopic(Topic topic,RequestProfile profile,int level,String indent,String tag) throws IOException, TopicMapException {
            outputTopic(topic,profile,level,indent,tag,null);
        }
        public void outputTopic(Topic topic,RequestProfile profile,int level,String indent,String tag,String role) throws IOException, TopicMapException {
            outputTopic(topic,profile,level,indent,tag,role,0,Integer.MAX_VALUE);
        }
        public void outputTopic(Topic topic,RequestProfile profile,int level,String indent,String tag,String role,int start,int limit) throws IOException, TopicMapException {
            if(tag==null) tag=getTopicTag(topic);
            if(tag==null) return;
            out.print(indent+"<"+tag);
            if(level>=LEVEL_BN_SI) out.print(" id=\""+attributeEscape(topic.getBaseName())+"\"");
            out.print(" level=\""+level+"\"");
            if(role!=null) out.print(" role=\""+role+"\"");
            out.print(">");
            out.print("\n"+indent+"\t<name lang=\""+lang+"\">"+textEscape(getTopicName(topic))+"</name>\n");
            
            int limit2=limit;
            
            if(level>=LEVEL_STUB){
                
                for(AssociationInfo info : profile.associationInfos){
                    if(level>=info.level){
                        int counter=outputAssociationType(info,profile,topic,indent+"\t",level,start,limit2);
                        start-=counter;
                        if(start<0){
                            limit2+=start; // start is now negative and means how many items were actually printed
                            start=0;
                            if(limit==-1) limit2=-1;
                            else if(limit2<=0) break;
                        }
                    }
                }
                
                for(OccurrenceInfo info : profile.occurrenceInfos){
                    if(level>=info.level){
                        if(start<=0) outputOccurrenceType(info,topic,indent+"\t",level);
                        start--;
                        if(start<0){
                            limit2--;
                            if(limit!=-1 && limit2<=0) break;
                        }
                    }
                }
                
            }
            out.print(indent+"</"+tag+">\n");
        }
        public int outputAssociationType(AssociationInfo info,RequestProfile profile,Topic context,String indent,int level,int start,int limit) throws IOException, TopicMapException {
            TopicMap tm=context.getTopicMap();
            int nextLevel=(level>LEVEL_STUB?LEVEL_STUB:LEVEL_BN_SI);
            ArrayList<ResultRow> results=info.query.query(new QueryContext(context,lang));
            int counter=0;
            for(ResultRow row : results){
                counter++;
                if(counter-1<start) continue;
                if(limit!=-1 && counter>start+limit) break;
                
                out.print(indent+"<"+info.associationTag+">");
                for(int i=0;i<row.getNumValues();i++){
                    Locator r=row.getRole(i);
                    if(r.toString().startsWith("~")) continue;
                    Object o=row.getValue(i);
                    if(o instanceof Locator || o==null){
                        out.print("\n");
                        Locator p=(Locator)o;
                        Topic rt=tm.getTopic(r);
                        String roleName=null;
                        if(rt!=null) roleName=rt.getBaseName();
                        if(p==null) {
                            if(roleName!=null) out.print(indent+"\t<null role=\""+roleName+"\"/>\n");
                            else out.print(indent+"\t<null/>\n");
                        }
                        else{
                            Topic player=tm.getTopic(p);
                            String tag=info.playerTags.get(r);
                            if(!player.mergesWithTopic(context)) outputTopic(player,profile,nextLevel,indent+"\t",tag,roleName);
                        }
                        out.print(indent);
                    }
                    else{
                        out.print(o.toString());
                    }
                }
                out.print("</"+info.associationTag+">\n");
            }
            return results.size();
        }
        public void outputOccurrenceType(OccurrenceInfo info,Topic context,String indent,int level) throws IOException, TopicMapException {
            TopicMap tm=context.getTopicMap();
            Topic type=tm.getTopic(info.si);
            if(type==null) return;
            String data=context.getData(type,lang);
            if(data==null) return;
            out.print(indent+"<"+info.occurrenceTag+" lang=\""+lang+"\">"+data+"</"+info.occurrenceTag+">\n");
        }
    }
}
