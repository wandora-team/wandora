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
 */



package org.wandora.application.tools.exporters.simberg;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.query2.*;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;
import org.wandora.utils.Tuples;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author olli
 */


public class SimbergExport extends AbstractExportTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	
	@Override
    public String getName() {
        return "Simberg export";
    }

    @Override
    public String getDescription() {
        return "Exports topic map for Simberg.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/fng.png");
    }
    
    
    public static Topic getAssociatedTopic(Topic t,Topic atype,Topic role,HashMap<Topic,Topic> constraints,Topic orderT) throws TopicMapException {
        ArrayList<Topic> ret=getAssociatedTopics(t, atype, role, constraints, orderT);
        if(ret.isEmpty()) return null;
        else return ret.get(0);
    }
    
    public static ArrayList<Association> getAssociations(Topic t,Topic atype,HashMap<Topic,Topic> constraints,Topic orderT) throws TopicMapException {
        ArrayList<Tuples.T2<Association,Topic>> ret=new ArrayList<Tuples.T2<Association,Topic>>();
        AS: for(Association a : t.getAssociations(atype)){
            if(constraints!=null){
                for(Map.Entry<Topic,Topic> e : constraints.entrySet()){
                    Topic p=a.getPlayer(e.getKey());
                    if(p==null || !p.mergesWithTopic(e.getValue())) continue AS;
                }
            }
            
            Topic order=null;
            if(orderT!=null) order=a.getPlayer(orderT);
            
            ret.add(Tuples.t2(a,order));
        }
        Collections.sort(ret, new Comparator<Tuples.T2<Association,Topic>>(){
            public int compare(Tuples.T2<Association, Topic> o1, Tuples.T2<Association, Topic> o2) {
                if(o1.e2==null){
                    if(o2.e2==null) return 0;
                    else return -1;
                }
                else if(o2.e2==null) return 1;
                else {
                    try{
                        if(o1.e2.getBaseName()==null){
                            if(o2.e2.getBaseName()==null) return 0;
                            else return -1;
                        }
                        else if(o2.e2.getBaseName()==null) return 1;
                        else return o1.e2.getBaseName().compareTo(o2.e2.getBaseName());
                    }catch(TopicMapException tme){
                        return 0;
                    }
                }
            }
        });
        
        ArrayList<Association> ret2=new ArrayList<Association>();
        for(Tuples.T2<Association,Topic> tu : ret) ret2.add(tu.e1);
        
        return ret2;        
    }

    public static ArrayList<Topic> getAssociatedTopics(Topic t,Topic atype,Topic role,HashMap<Topic,Topic> constraints,Topic orderT) throws TopicMapException {
        ArrayList<Association> as=getAssociations(t, atype, constraints, orderT);
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(Association a : as){
            Topic player=a.getPlayer(role);
            if(player!=null) ret.add(player);
        }
        return ret;
    }
    
    public static ArrayList<ModelTopic> getOrMakeTopics(Collection ts,ModelClass cls,String nameField,HashMap<T2<ModelClass,Object>,ModelTopic> modelTopics) throws TopicMapException {
        return getOrMakeTopics(ts, cls, nameField, modelTopics, null);
    }
    
    public static ArrayList<ModelTopic> getOrMakeTopics(Collection ts,ModelClass cls,String nameField,HashMap<T2<ModelClass,Object>,ModelTopic> modelTopics, String lang) throws TopicMapException {
        ArrayList<ModelTopic> ret=new ArrayList<ModelTopic>();
        for(Object t : ts){
            ModelTopic mt=getOrMakeTopic(t, cls, nameField, modelTopics,lang);
            ret.add(mt);
        }
        return ret;
    }
    
    public static ModelTopic getOrMakeTopic(Object t,ModelClass cls,String nameField,HashMap<T2<ModelClass,Object>,ModelTopic> modelTopics) throws TopicMapException {
        return getOrMakeTopic(t, cls, nameField, modelTopics, null);
    }
    
    public static ModelTopic getOrMakeTopic(Object t,ModelClass cls,String nameField,HashMap<T2<ModelClass,Object>,ModelTopic> modelTopics,String lang) throws TopicMapException {
        if(t==null) return null;
        ModelTopic mt=modelTopics.get(Tuples.t2(cls,t));
        if(mt==null) {
            mt=new ModelTopic(cls);
            if(nameField!=null) {
                if(t instanceof Topic){
                    if(lang!=null) mt.setField(nameField,((Topic)t).getDisplayName(lang));
                    else mt.setField(nameField, ((Topic)t).getBaseName());
                }
                else mt.setField(nameField,t.toString());
            }
            modelTopics.put(Tuples.t2(cls,t),mt);
        }
        return mt;
    }
    
    public static String makeFileName(Topic digikuva) throws TopicMapException {
        if(digikuva.getBaseName()==null) return null;
        return digikuva.getBaseName().replaceAll("[^a-zA-Z0-9,_-]", "_");
    }
    
    public static ArrayList<ResultRow> doQuery(Directive d,Topic context,TopicMap tm,String lang){
        try{
            return d.doQuery(new QueryContext(tm, lang), context!=null?new ResultRow(context):new ResultRow());
        }catch(QueryException qe){
            qe.printStackTrace();
            return null;
        }
    }
    
    private static Pattern numberPattern=Pattern.compile("(?:Number )?(\\d+)");
    public static String parseMeasure(Object valueO,Object unitO){
        if(valueO==null || unitO==null) return null;
        String value=valueO.toString().trim();
        String unit=unitO.toString().toLowerCase().trim();
        if(unit.startsWith("type ")) unit=unit.substring(5).trim();
        Matcher matcher=numberPattern.matcher(value);
        if(!matcher.matches()) return null;
        
        if(unit.startsWith("mm")) unit="mm";
        else if(unit.startsWith("cm")) unit="cm";
        else return null;

        String fieldValue=matcher.group(1);
        fieldValue+=" "+unit;        
        return fieldValue;
    }
    
    public static boolean parseNegative(Object o){
        String s=o.toString().toLowerCase();
        int pind=s.indexOf("positiivi");
        int nind=s.indexOf("negatiivi");
        if(pind<0 && nind>=0) return true;
        else if(pind>=0 && nind<0) return false;
        else if(pind<0 && nind<0) return true;
        else {
            if(s.substring(nind).startsWith("negatiivista")) return false;
            else return true;
        }
    }
    
    public static Object getResult(ArrayList<ResultRow> res,String role){
        if(res.isEmpty()) return null;
        return res.get(0).get(role);
    }
    public static Topic getTopicResult(ArrayList<ResultRow> rows,String role) {
        if(rows.isEmpty()) return null;
        try{
            return (Topic)rows.get(0).getValue(role);
        }catch(QueryException qe){ qe.printStackTrace(); return null;}
    }
    public static ArrayList<Topic> getTopicResults(ArrayList<ResultRow> rows,String role) {
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(ResultRow row : rows){
            Object o;
            try{
                if(role!=null) o=row.getValue(role);
                else o=row.getActiveValue();
                if(o==null || !(o instanceof Topic)) continue;
                else ret.add((Topic)o);
            }catch(QueryException qe){ qe.printStackTrace(); }
        }
        return ret;
    }

    public static int[] levenshtein(String text,String query){
        int[][] distance = new int[text.length() + 1][query.length() + 1];

        for(int i=0;i<=text.length();i++) distance[i][0] = 0;
        for(int i=1;i<=query.length();i++) distance[0][i] = i;

        String ignoreChars=" \t.-";
        
        for(int i=1;i<=text.length();i++)
            for(int j=1;j<=query.length();j++)
                distance[i][j]=Math.min( 
                            distance[i-1][j]+(ignoreChars.indexOf((int)text.charAt(i-1))>=0?0:1),
                            Math.min(
                                distance[i][j-1]+(ignoreChars.indexOf((int)query.charAt(j-1))>=0?0:1),
                                distance[i-1][j-1]+((text.charAt(i-1)==query.charAt(j-1))?0:1)
                            )
                        );

        int end=0;
        int min=query.length();
        for(int i=0;i<=text.length();i++){
            if(distance[i][query.length()]<min) {
                min=distance[i][query.length()];
                end=i;
            }
        }
        
        int j=query.length();
        int start=end;
        while(j>0){
            int s1=query.length();
            int s2=query.length();
            int s3=distance[start][j-1];
            if(start>0 && j>0) s1=distance[start-1][j-1];
            if(start>0) s2=distance[start-1][j];
            
            if(s1<=s2 && s1<=s3){
                start--;
                j--;
            }
            else if(s2<=s1 && s2<=s3){
                start--;
            }
            else {
                j--;
            }
        }
        
        return new int[]{min,start,end};
    }

    
    public static ArrayList<String> readKeywords(File f) throws IOException {
        ArrayList<String> ret=new ArrayList<String>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line=null;
        while((line=reader.readLine())!=null){
            line=line.trim();
            if(line.isEmpty()) continue;
            ret.add(line);
        }
        return ret;
    }
    
    public static T2<ArrayList<String>,String> matchKeywords(String keywordString,ArrayList<String> keywordList){
        String stopChars=" ,;.()[]";
        
        Collections.sort(keywordList,new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                int l1=o1.length();
                int l2=o2.length();
                if(l1!=l2){
                    if(l1>l2) return -1;
                    else return 1;
                }
                else return o1.compareTo(o2);
            }
        });
        
        ArrayList<String> matched=new ArrayList<String>();
        String keywordLinks=keywordString;
        
        keywordString=keywordString.toLowerCase();
        for(String keyword : keywordList){
            int[] m=levenshtein(keywordString, keyword.toLowerCase());
            if(m[0]<=Math.floor(keyword.length()/10)) {
                
                int start=m[1];
                int end=m[2];
                for(;start>=0;start--) if(stopChars.indexOf(keywordString.charAt(start))>=0) break;
                start++;
                for(;end<keywordString.length();end++) if(stopChars.indexOf(keywordString.charAt(end))>=0) break;
                
                // disable fuzzy matching for some very similar but distinct names
                if(keyword.indexOf("Elma")>0 || keyword.indexOf("Elsa")>0 || keyword.indexOf("Elsi")>0){
                    if(m[0]!=0) continue;
                }
                
                if(keyword.indexOf(" ")==-1){ // skip these checks if the keyword itself consists of several words
                    if(end-m[2]>0 && m[1]-start>0) continue; // match strictly inside a word
                    if(keyword.length()<=4 && end-m[2]>1) continue; // a small keyword is a prefix of something big
                    if(keyword.length()<=4 && m[1]-start>0) continue; // a small keyword is a postfix of something
                }
                
                // Add the link to keywordLinks
                // If matched word exactly matches the keyword, don't repeat the keyword, otherwise it must be included
                // in the link so we know what to link to. Use wiki style links.
                String link=keywordLinks.substring(start,end);
                if(!link.equals(keyword)) link=link+"|"+keyword;
                keywordLinks=keywordLinks.substring(0,start)+"["+link+"]"+keywordLinks.substring(end);
                
                // blank out the matched area in keywordString and keep it aligned with keywordLinks
                StringBuilder padding=new StringBuilder();
                for(int i=0;i<link.length();i++) padding.append(" ");
                keywordString=keywordString.substring(0,start)+"["+padding+"]"+keywordString.substring(end);
                
                // add the matched keyword in the array
                matched.add(keyword);
/*
                if(start>end){
                    System.out.println("Matched "+keyword+" to "+
                        keywordString.substring(Math.max(0,start-10),m[1])+"|^|"+
                        keywordString.substring(end,Math.min(keywordString.length(),m[1]+10))
                        );                    
                }
                else{
                    System.out.println("Matched "+keyword+" to "+
                        keywordString.substring(Math.max(0,start-10),start)+"|"+
                        keywordString.substring(start,m[1])+"^"+
                        keywordString.substring(m[1],end)+"|"+
                        keywordString.substring(end,Math.min(keywordString.length(),end+10))
                        );
                }*/
            }
        }
/*        
        Collections.sort(keywordsAndPos,new Comparator<T2<String,Integer>>(){
            @Override
            public int compare(T2<String, Integer> o1, T2<String, Integer> o2) {
                if(o1.e2==o2.e2) return o1.e1.compareTo(o2.e1); // shouldn't really happen
                else return o1.e2-o2.e2;
            }
        });
        
        ArrayList<String> ret=new ArrayList<String>();
        for(T2<String,Integer> e : keywordsAndPos){
            ret.add(e.e1);
        }
        
        return ret;*/
        
        return Tuples.t2(matched, keywordLinks);
    }
    
    // This reads the image dimensions without reading the whole file, which would take
    // a lot of time for a big collection of big images.
    public static int[] getImageDimensions(File f) throws IOException {
        ImageInputStream in = ImageIO.createImageInputStream(f);
        try{
            final Iterator readers = ImageIO.getImageReaders(in);
            if(readers.hasNext()){
                    ImageReader reader=(ImageReader)readers.next();
                    try{
                            reader.setInput(in);
                            return new int[]{reader.getWidth(0), reader.getHeight(0)};
                    }finally{
                            reader.dispose();
                    }
            }
        }finally {
            if(in!=null) in.close();
        }
        return null;
    }
    
    public static Collection<ModelTopic> buildModel(TopicMap tm,String imagesDir,ArrayList<String> keywordList) throws TopicMapException {
        String lang="fi";
        
        ModelClass photoCls=new ModelClass("photograph");
        photoCls.addField(new ModelField("leveys", ModelField.Type.String));
        photoCls.addField(new ModelField("korkeus", ModelField.Type.String));        
        photoCls.addField(new ModelField("date", ModelField.Type.String));
        photoCls.addField(new ModelField("dateRaw", ModelField.Type.String));
        photoCls.addField(new ModelField("digital", ModelField.Type.Topic));
        photoCls.addField(new ModelField("digitalneg", ModelField.Type.Topic));
        photoCls.addField(new ModelField("technique", ModelField.Type.TopicList));
        photoCls.addField(new ModelField("author", ModelField.Type.Topic));
        photoCls.addField(new ModelField("identifier", ModelField.Type.String));
        photoCls.addField(new ModelField("keywords", ModelField.Type.TopicList));
        photoCls.addField(new ModelField("keywordText", ModelField.Type.String));
        photoCls.addField(new ModelField("material", ModelField.Type.Topic));
        ModelClass keywordCls=new ModelClass("keyword");
        keywordCls.addField(new ModelField("name", ModelField.Type.String));
/*        ModelClass pictureCls=new ModelClass("original"); 
        pictureCls.addField(new ModelField("name", ModelField.Type.String));
        pictureCls.addField(new ModelField("author", ModelField.Type.TopicList));
        pictureCls.addField(new ModelField("keeper", ModelField.Type.Topic));
        pictureCls.addField(new ModelField("time", ModelField.Type.String));
        pictureCls.addField(new ModelField("material", ModelField.Type.TopicList));
        pictureCls.addField(new ModelField("leveys", ModelField.Type.String));
        pictureCls.addField(new ModelField("korkeus", ModelField.Type.String));
        pictureCls.addField(new ModelField("nayttelytiedot", ModelField.Type.TopicList));
//        pictureCls.addField(new ModelField("keywords", ModelField.Type.TopicList));
        pictureCls.addField(new ModelField("identifier", ModelField.Type.String));*/
        ModelClass digiCls=new ModelClass("digi");
        digiCls.addField(new ModelField("file", ModelField.Type.String));
        digiCls.addField(new ModelField("width", ModelField.Type.String));
        digiCls.addField(new ModelField("height", ModelField.Type.String));
        digiCls.addField(new ModelField("isnegative", ModelField.Type.String));
        ModelClass personCls=new ModelClass("person");
        personCls.addField(new ModelField("name", ModelField.Type.String));
        personCls.addField(new ModelField("born", ModelField.Type.String));
        personCls.addField(new ModelField("died", ModelField.Type.String));
/*        ModelClass keeperCls=new ModelClass("keeper");
        keeperCls.addField(new ModelField("name", ModelField.Type.String));
        ModelClass materialCls=new ModelClass("material");
        materialCls.addField(new ModelField("name", ModelField.Type.String));
        ModelClass timeCls=new ModelClass("time");
        timeCls.addField(new ModelField("name", ModelField.Type.String));
        ModelClass activityCls=new ModelClass("activity");
        activityCls.addField(new ModelField("name", ModelField.Type.String));*/
        ModelClass techniqueCls=new ModelClass("technique");
        techniqueCls.addField(new ModelField("name", ModelField.Type.String));
        ModelClass materialCls=new ModelClass("material");
        materialCls.addField(new ModelField("name", ModelField.Type.String));
        
        
        
        Directive query=
                new If(
                    new Variant2("http://www.muusa.net/E55.Type_sisaltokuvaus_"+lang).as("#nega"),
                    new If.COND(),
                    new Identity().join(new Null().as("#nega"))
                )
                .from(
                    new If(
                        new Players("http://www.muusa.net/P62.depicts"
                            ,"http://www.muusa.net/Source")
                            .whereInputIs("http://www.muusa.net/Target")
                            .where(new Not(new IsOfType("http://www.muusa.net/Valokuva"))).as("#digikuva"),
                        new If.COND(),
                        new Identity().join(new Null().as("#digikuva"))
                    )
                    .from(
                        new Instances().from("http://www.muusa.net/Valokuva")
                        
                        .as("#photograph")
                    )
                )
//                .where(new Of("#nega"),"!=",null)
                ;
        
        ArrayList<ResultRow> res=doQuery(query, null, tm, lang);
        
        Pattern datePattern=Pattern.compile("\\d\\d\\d\\d");
        
        ModelTopic unknownAuthor=new ModelTopic(personCls);
        unknownAuthor.setField("name","Tuntematon");
        
        HashMap<T2<ModelClass,Object>,ModelTopic> modelTopics=new HashMap<T2<ModelClass,Object>,ModelTopic>();
        for(ResultRow row : res ){
            Topic digi=(Topic)row.get("#digikuva");
            Topic photograph=(Topic)row.get("#photograph");
            String negaInfo=(String)row.get("#nega");
            
            if(photograph==null) continue;
            
            ModelTopic photoM=modelTopics.get(Tuples.t2(photoCls,(Object)photograph));
            if(photoM==null){
                photoM=new ModelTopic(photoCls);
                
                query=
                    new Join(new Directive[]{
                        new If(
                            new Players("http://www.muusa.net/P14.Production_carried_out_by","http://www.muusa.net/E39.Actor").as("#author"),
                            new If.COND(),
                            new Null().as("#author")
                        ),
                        new Players("http://www.muusa.net/E52.Time-Span","http://www.muusa.net/E52.Time-Span").as("#timespan"),
                        new Players("http://www.muusa.net/P129.is_about",
                            "http://www.muusa.net/P129.is_about","http://www.muusa.net/E32.Authority_Document")
                            .usingColumns("#keyword","#keywordtype")
                            .where(new Of("#keywordtype"), "t=", "http://www.muusa.net/P71_lists_asiasanat"),
                        new If(
                            new Players("http://www.muusa.net/P43.has_dimension",
                                "http://www.muusa.net/P43.has_dimension_role_1",
                                "http://www.muusa.net/P43.has_dimension_role_3",
                                "http://www.muusa.net/P43.has_dimension_role_2")
                                .usingColumns("~dimtype","#leveysunit","#leveys")
                                .where(new Of("~dimtype"),"t=","http://www.muusa.net/E55.Type_leveys"),
                            new If.COND(),
                            new Join(new Null().as("#leveysunit"),new Null().as("#leveys"))
                        ),
                        new If(
                            new Players("http://www.muusa.net/P43.has_dimension",
                                "http://www.muusa.net/P43.has_dimension_role_1",
                                "http://www.muusa.net/P43.has_dimension_role_3",
                                "http://www.muusa.net/P43.has_dimension_role_2")
                                .usingColumns("~dimtype","#korkeusunit","#korkeus")
                                .where(new Of("~dimtype"),"t=","http://www.muusa.net/E55.Type_korkeus"),
                            new If.COND(),
                            new Join(new Null().as("#korkeusunit"),new Null().as("#korkeus"))
                        ),
                        new If(
                            new Players("http://www.muusa.net/P45.consists_of","http://www.muusa.net/P45.consists_of_role_0")
                                .as("#material"),
                            new If.COND(),
                            new Null().as("#material")
                        )
                    }).from(new Identity().as("#photograph"));
                ArrayList<ResultRow> res2=doQuery(query,photograph,tm,lang);
                
                photoM.setField("identifier", photograph.getBaseName());
                
                photoM.setField("leveys", parseMeasure(getResult(res2,"#leveys"),getResult(res2,"#leveysunit")));
                photoM.setField("korkeus", parseMeasure(getResult(res2,"#korkeus"),getResult(res2,"#korkeusunit")));
                Object date=getResult(res2,"#timespan");
                photoM.setField("dateRaw", date);
                if(date!=null){
                    Matcher m=datePattern.matcher(date.toString());
                    String parsedDate=null;
                    if(m.find()) {
                        parsedDate=m.group();
                    }
                    photoM.setField("date", parsedDate);
                }
                else photoM.setField("date", null);

                
                Topic author=(Topic)getResult(res2,"#author");
                if(author!=null){
                    ModelTopic authorM=modelTopics.get(Tuples.t2(personCls,(Object)author));
                    if(authorM==null){
                        authorM=new ModelTopic(personCls);
                        authorM.setField("name",author.getBaseName());
                        modelTopics.put(Tuples.t2(personCls,(Object)author),authorM);
                    }
                    photoM.setField("author",authorM);
                }
                else {
                    photoM.setField("author",unknownAuthor);
                }
                          
                if(keywordList!=null){
                    Topic keywordStringTopic=getTopicResult(res2,"#keyword");
                    String keywordString=null;
                    T2<ArrayList<String>,String> keywords=null;
                    if(keywordStringTopic!=null) keywordString=keywordStringTopic.getBaseName();
                    if(keywordString==null || keywordString.trim().length()==0) keywordString="valokuvat";
                    else keywordString="valokuvat, "+keywordString;
                    keywords=matchKeywords(keywordString, keywordList);
                    if(keywords!=null){
                        ArrayList<ModelTopic> keywordsM=getOrMakeTopics(keywords.e1, keywordCls, "name", modelTopics,lang);
                        photoM.setField("keywords",keywordsM);
                        photoM.setField("keywordText",keywords.e2);
                    }
                    else photoM.setField("keywords",new ArrayList<ModelTopic>());
                }
                else photoM.setField("keywords",new ArrayList<ModelTopic>());
                
                /*
                {
                    Topic keywordStringTopic=getTopicResult(res2,"#keyword");
                    if(keywordStringTopic!=null){
                        String keywords=keywordStringTopic.getBaseName().trim();
                        photoM.setField("keywords",keywords);
                    }
                }
                */
                
                Topic material=(Topic)getResult(res2,"#material");
                if(material!=null){
                    ModelTopic materialM=modelTopics.get(Tuples.t2(materialCls,(Object)material));
                    if(materialM==null){
                        String name=material.getBaseName();
                        if(name.startsWith("valokuvamateriaali")) name=name.substring("valokuvamateriaali".length()).trim();
                        int ind=name.indexOf("vpakkane");
                        if(ind>=0) name=name.substring(0,ind).trim();
                        
                        materialM=new ModelTopic(materialCls);
                        materialM.setField("name",name);
                        modelTopics.put(Tuples.t2(materialCls,(Object)material),materialM);
                    }
                    photoM.setField("material",materialM);
                }                
                
                res2=doQuery(
                    new OrderBy(
                        new Players("http://www.muusa.net/P32.used_general_technique",
                            "http://www.muusa.net/P32.used_general_technique","http://www.muusa.net/Order")
                            .usingColumns("#technique","#order").to(new Of("#order"))
                    ), photograph, tm, lang);
                ArrayList<Topic> techniques=getTopicResults(res2, "#technique");
                ArrayList<ModelTopic> techniquesM=getOrMakeTopics(techniques, techniqueCls, "name", modelTopics, lang);
                photoM.setField("technique",techniquesM);
                
                
                
/*                
                res2=doQuery(
                    new OrderBy(
                        new Players("http://www.muusa.net/P129.is_about",
                            "http://www.muusa.net/P129.is_about","http://www.muusa.net/E32.Authority_Document")
                            .usingColumns("#keyword","#keywordtype")
                            .where(new Of("#keywordtype"), "t=", "http://www.muusa.net/P71_lists_asiasanat")
                    ), photograph, tm, lang);
                ArrayList<Topic> keywords=getTopicResults(res2, "#keyword");
                ArrayList<ModelTopic> keywordsM=getOrMakeTopics(keywords, keywordCls, "name", modelTopics,lang);
                photoM.setField("keywords",keywordsM);*/
                                
                modelTopics.put(Tuples.t2(photoCls,(Object)photograph), photoM);
            }
            
            if(digi!=null && negaInfo!=null){
                boolean negative=parseNegative(negaInfo);
                
                ModelTopic digiM=new ModelTopic(digiCls);
                digiM.setField("isnegative",negative?"1":"0");
                String imageFile=makeFileName(digi);
                digiM.setField("file",imageFile);

                int width=0;
                int height=0;
                if(imagesDir!=null){
                    File f=new File(imagesDir+imageFile+".jpg");
                    if(f.exists()){
                        try{
    /*                        BufferedImage bi=ImageIO.read(f);
                            width=bi.getWidth();
                            height=bi.getHeight();*/

                            int[] dim=getImageDimensions(f);
                            if(dim!=null){
                                width=dim[0];
                                height=dim[1];
                            }
                        }
                        catch(IOException ioe){
                        }
                    }
                }
                digiM.setField("width",""+width);
                digiM.setField("height",""+height);                

                if(negative) photoM.setField("digitalneg",digiM);
                else photoM.setField("digital",digiM);
                
                modelTopics.put(Tuples.t2(digiCls,(Object)digi),digiM);
            }
        }
        
        return modelTopics.values();
    }
    
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap tm=Wandora.getWandora().getTopicMap();
        
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Export nyblin data as JSON");

        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();

            // --- Finally write topicmap as GXL to chosen file
            OutputStream out=null;
            try {
                file=IObox.addFileExtension(file, "json"); // Ensure file extension exists!
                
                String parentDir=file.getParent();
                if(!(parentDir.endsWith("/") || parentDir.endsWith("\\"))) parentDir+="/";
                
                ArrayList<String> keywords=null;
                File keywordFile=new File(parentDir+"keywords.txt");
                if(keywordFile.exists()) {
                    keywords=readKeywords(keywordFile);
                    if(keywords.indexOf("valokuvat")==-1) keywords.add("valokuvat");    
                }
                
                out=new FileOutputStream(file);
                OutputStreamWriter writer=new OutputStreamWriter(out,"UTF-8");
                Collection<ModelTopic> modelTopics=buildModel(tm,parentDir+"images/",keywords);
                ModelTools.exportJSON(modelTopics, writer);
                writer.close();
            }
            catch(Exception e){
                log(e);
                try { if(out != null) out.close(); }
                catch(Exception e2) { log(e2); }                
            }
        }
        setState(WAIT);
    }
    /*
    public static void main(String[] args){
        String test="taiteilija, Simberg-suku, kesäpaikka Niemenlautta, Säkkijärvi, ".toLowerCase();
        int[] ret=levenshtein(test,"niemenlautta");
        System.out.println(ret[0]+" "+ret[1]+" "+ret[2]);
        System.out.println(test);
        for(int i=0;i<test.length();i++){
            if(i==ret[1] || i==ret[2]) System.out.print("^");
            else System.out.print(" ");
        }
        System.out.println();
    }*/
    
}
