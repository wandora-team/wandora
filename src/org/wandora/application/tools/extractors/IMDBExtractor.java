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
 * IMDBExtractor.java
 *
 * Created on 16. kesäkuuta 2006, 10:41
 */

package org.wandora.application.tools.extractors;
import org.wandora.topicmap.database.DatabaseTopicMap;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.utils.crawler.handlers.*;
import org.wandora.utils.swing.*;
import org.wandora.utils.*;


/**
 *
 * @author olli
 */
public class IMDBExtractor extends AbstractExtractor implements WandoraTool {
    
    public static final String ACTOR_SI="http://wandora.org/si/imdb/actor";
    public static final String DIRECTOR_SI="http://wandora.org/si/imdb/director";
    public static final String PRODUCER_SI="http://wandora.org/si/imdb/producer";
    public static final String ROLE_SI="http://wandora.org/imdb/si/role";
    public static final String EPISODE_SI="http://wandora.org/si/imdb/episode";
    
    public static final String SHOW_SI="http://wandora.org/si/imdb/show";
    public static final String TVSHOW_SI="http://wandora.org/si/imdb/tvshow";
    public static final String TVMINI_SI="http://wandora.org/si/imdb/tvmini";
    public static final String MOVIE_SI="http://wandora.org/si/imdb/movie";
    public static final String TVMOVIE_SI="http://wandora.org/si/imdb/tvmovie";
    public static final String VIDEOMOVIE_SI="http://wandora.org/si/imdb/videomovie";
    public static final String VIDEOGAME_SI="http://wandora.org/si/imdb/videogame";
    
    public static final String KEYWORD_SI="http://wandora.org/si/imdb/keyword";
    public static final String LANGUAGE_SI="http://wandora.org/si/imdb/language";
    public static final String COUNTRY_SI="http://wandora.org/si/imdb/country";
    public static final String YEAR_SI="http://wandora.org/si/imdb/year";
    public static final String GENRE_SI="http://wandora.org/si/imdb/genre";
    public static final String LOCATION_SI="http://wandora.org/si/imdb/location";
    public static final String RUNTIME_SI="http://wandora.org/si/imdb/runtime";
    public static final String RUNTIMEINFO_SI="http://wandora.org/si/imdb/runtimeinfo";
    public static final String RELEASEDATE_SI="http://wandora.org/si/imdb/releasedate";
    public static final String RELEASEDATEINFO_SI="http://wandora.org/si/imdb/releasedateinfo";

    public static final String PLOT_SI="http://wandora.org/si/imdb/plot";

    public static final String PERSON_SI="http://wandora.org/si/imdb/person";
    public static final String DATE_SI="http://wandora.org/si/imdb/date";
    public static final String DATEOFBIRTH_SI="http://wandora.org/si/imdb/dateofbirth";
    public static final String DATEOFDEATH_SI="http://wandora.org/si/imdb/dateofdeath";
    public static final String BIOGRAPHY_SI="http://wandora.org/si/imdb/biography";
    public static final String REALNAME_SI="http://wandora.org/si/imdb/realname";
    public static final String PLACE_SI="http://wandora.org/si/imdb/place";
    
    /** Creates a new instance of IMDBExtractor */
    public IMDBExtractor() {
    }

    @Override
    public String getName() {
        return "IMDB Extractor";
    }

    @Override
    public String getDescription(){
        return "Extract data from Internet Movie Database data files";
    }

    @Override
    public boolean useTempTopicMap(){
        return false;
    }

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select IMDB list file(s) or directories containing list files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking list files!";
        
            case FILE_PATTERN: return ".*\\.list";
            
            case DONE_FAILED: return "Done! No extractions! %1 list file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 list file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 list file(s) crawled!";
            
            case LOG_TITLE: return "IMDB data extraction Log";
        }
        return "";
    }



    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }



    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = extractFromReader(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }



    
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        boolean result = false;
        try {
            result = extractFromReader(br,t);
        }
        finally {
            if(br != null) br.close();
        }
        return result;
    }



    
    
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream()));
        boolean result = false;
        try {
            result = extractFromReader(br,t);
        }
        finally {
            if(br != null) br.close();
        }
        return result;
    }
    
    
    
    public boolean extractFromReader(BufferedReader in,TopicMap tm) throws Exception {
        boolean resetIndexes=false;
        if(tm instanceof LayerStack){
            LayerStack ls=(LayerStack)tm;
            tm=ls.getSelectedLayer().getTopicMap();
        }
        if(tm instanceof DatabaseTopicMap){
            ((DatabaseTopicMap)tm).setCompleteIndex();
            resetIndexes=true;
        }
        boolean oldConsistencyCheck=tm.getConsistencyCheck();
        tm.setConsistencyCheck(false);
        tm.disableAllListeners();
        try{
            String line=null;
            int counter=0;
            while( (line=in.readLine())!=null ){
                if(line.equals("THE ACTORS LIST")){
                    log("Extracting actor list");
                    in.readLine(); // ===============
                    in.readLine(); //
                    in.readLine(); // Name
                    in.readLine(); // ----
                    return extractActorList(in,tm);
                }
                else if(line.equals("THE ACTRESSES LIST")){
                    log("Extracting actor list");
                    in.readLine(); // ===============
                    in.readLine(); //
                    in.readLine(); // Name
                    in.readLine(); // ----
                    return extractActorList(in,tm);
                }
                else if(line.equals("8: THE KEYWORDS LIST")){
                    log("Extracting keyword list");
                    in.readLine(); // ==============
                    in.readLine(); //
                    return extractKeywordList(in,tm);
                }
                else if(line.equals("COUNTRIES LIST")){
                    log("Extracting country list");
                    in.readLine(); // ==============
                    return extractCountryList(in,tm);
                }
                else if(line.equals("LANGUAGE LIST")){
                    log("Extracting language list");
                    in.readLine(); // ==============
                    return extractLanguageList(in,tm);
                }
                else if(line.equals("LOCATIONS LIST")){
                    log("Extracting location list");
                    in.readLine(); // ==============
                    in.readLine(); //
                    return extractLocationList(in,tm);
                }
                else if(line.equals("8: THE GENRES LIST")){
                    log("Extracting genre list");
                    in.readLine(); // ==============
                    in.readLine(); //
                    return extractGenreList(in,tm);
                }
                else if(line.equals("MOVIES LIST")){
                    log("Extracting movie list");
                    in.readLine(); // ==============
                    in.readLine(); //
                    return extractMovieList(in,tm);
                }
                else if(line.equals("BIOGRAPHY LIST")){
                    log("Extracting biography list");
                    in.readLine(); // ==============
                    in.readLine(); // --------------
                    return extractBiographyList(in,tm);
                }
                else if(line.equals("THE PRODUCERS LIST")){
                    log("Extracting producer list");
                    in.readLine(); // ==============
                    in.readLine(); // 
                    in.readLine(); // Name            Titles
                    in.readLine(); // ----            ------
                    return extractProducerList(in,tm);
                }
                else if(line.equals("THE DIRECTORS LIST")){
                    log("Extracting directors list");
                    in.readLine(); // ==============
                    in.readLine(); // 
                    in.readLine(); // Name            Titles
                    in.readLine(); // ----            ------
                    return extractDirectorList(in,tm);
                }
                else if(line.equals("PLOT SUMMARIES LIST")){
                    log("Extracting plot summary list");
                    in.readLine(); // ==============
                    in.readLine(); // 
                    return extractPlotList(in,tm);
                }
                else if(line.equals("RUNNING TIMES LIST")){
                    log("Extracting running time list");
                    in.readLine(); // ==============
                    return extractRunningTimeList(in,tm);
                }
                else if(line.equals("RELEASE DATES LIST")){
                    log("Extracting release date list");
                    in.readLine(); // ==============
                    return extractReleaseDateList(in,tm);
                }
                counter++;
                if(counter>20000) break;
            }
            log("Unknown file type");
            if(resetIndexes){
                ((DatabaseTopicMap)tm).resetCompleteIndex();
            }
            return false;
        }
        finally{
            tm.enableAllListeners();
            tm.setConsistencyCheck(oldConsistencyCheck);
        }
    }
    
    public void createSchemaTopics(TopicMap tm) throws TopicMapException {
        Topic actor=getOrCreateTopic(tm,ACTOR_SI);
        actor.setBaseName("Actor");
        Topic director=getOrCreateTopic(tm,DIRECTOR_SI);
        director.setBaseName("Director");
        Topic producer=getOrCreateTopic(tm,PRODUCER_SI);
        producer.setBaseName("Producer");
        Topic role=getOrCreateTopic(tm,ROLE_SI);
        role.setBaseName("Role name");
        Topic episode=getOrCreateTopic(tm,EPISODE_SI);
        episode.setBaseName("Episode");
        Topic show=getOrCreateTopic(tm,SHOW_SI);
        show.setBaseName("Show");
        Topic tvshow=getOrCreateTopic(tm,TVSHOW_SI);
        tvshow.setBaseName("TV series");
        Topic tvmini=getOrCreateTopic(tm,TVMINI_SI);
        tvmini.setBaseName("TV mini series");
        Topic movie=getOrCreateTopic(tm,MOVIE_SI);
        movie.setBaseName("Movie");
        Topic tvmovie=getOrCreateTopic(tm,TVMOVIE_SI);
        tvmovie.setBaseName("TV Movie");
        Topic videomovie=getOrCreateTopic(tm,VIDEOMOVIE_SI);
        videomovie.setBaseName("Video Movie");
        Topic videogame=getOrCreateTopic(tm,VIDEOGAME_SI);
        videogame.setBaseName("Video Game");
        Topic keyword=getOrCreateTopic(tm,KEYWORD_SI);
        keyword.setBaseName("Keyword");
        Topic language=getOrCreateTopic(tm,LANGUAGE_SI);
        language.setBaseName("Language");
        Topic country=getOrCreateTopic(tm,COUNTRY_SI);
        country.setBaseName("Country");
        Topic genre=getOrCreateTopic(tm,GENRE_SI);
        genre.setBaseName("Genre");
        Topic location=getOrCreateTopic(tm,LOCATION_SI);
        location.setBaseName("Location");
        Topic person=getOrCreateTopic(tm,PERSON_SI);
        person.setBaseName("Person");
        Topic date=getOrCreateTopic(tm,DATE_SI);
        date.setBaseName("Date");
        Topic dateofbirth=getOrCreateTopic(tm,DATEOFBIRTH_SI);
        dateofbirth.setBaseName("Date of birth");
        Topic dateofdeath=getOrCreateTopic(tm,DATEOFDEATH_SI);
        dateofdeath.setBaseName("Date of death");
        Topic biography=getOrCreateTopic(tm,BIOGRAPHY_SI);
        biography.setBaseName("Biography");
        Topic realname=getOrCreateTopic(tm,REALNAME_SI);
        realname.setBaseName("Real name");
        Topic place=getOrCreateTopic(tm,PLACE_SI);
        place.setBaseName("Place");
        Topic plot=getOrCreateTopic(tm,PLOT_SI);
        plot.setBaseName("Plot");
        Topic runtime=getOrCreateTopic(tm,RUNTIME_SI);
        runtime.setBaseName("Running time");
        Topic runtimeinfo=getOrCreateTopic(tm,RUNTIMEINFO_SI);
        runtimeinfo.setBaseName("Running time info");
        Topic releasedate=getOrCreateTopic(tm,RELEASEDATE_SI);
        releasedate.setBaseName("Release date");
        Topic releasedateinfo=getOrCreateTopic(tm,RELEASEDATEINFO_SI);
        releasedateinfo.setBaseName("Release date info");
    }
    
//    private HashMap<String,Topic> topicCache=new HashMap<String,Topic>();
    
    public Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
//        Topic t=topicCache.get(si);
//        if(t!=null) return t;
        Topic t=tm.getTopic(si);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(si));
        }
//        topicCache.put(si,t);
        return t;
    }
    private static String cleanLocator(String s){
        s=TopicTools.cleanDirtyLocator(s);
        if(s.length()>255) s=s.substring(0,255);
        return s;
    }
    public Topic getPersonTopic(TopicMap tm,String name) throws TopicMapException {
        return getPersonTopic(tm,name,null);
    }
    public Topic getPersonTopic(TopicMap tm,String name,String type) throws TopicMapException {
        if(type==null) type=PERSON_SI;
        Locator l=tm.createLocator(cleanLocator(type+"/"+name));
        Topic t=tm.getTopicWithBaseName(name);
        if(t==null) t=tm.getTopic(l);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(name);
            t.addType(getOrCreateTopic(tm,type));
        }
        return t;
    }
    public Topic getActorTopic(TopicMap tm,String name) throws TopicMapException {
        return getPersonTopic(tm,name,ACTOR_SI);
    }
    private HashSet<String> typeFilter;
//    { typeFilter=null; }
    { typeFilter=new HashSet<String>(); typeFilter.add(MOVIE_SI); }
    
    public Topic getShowTopic(TopicMap tm,String name) throws TopicMapException {        
        Topic t=tm.getTopicWithBaseName(name);
        if(t==null){
            String type=null;
            if(name.startsWith("\"")){
                if(name.contains("(mini)")) type=TVMINI_SI;
                else type=TVSHOW_SI;
            }
            else if(name.contains("(TV)")) type=TVMOVIE_SI;
            else if(name.contains("(V)")) type=VIDEOMOVIE_SI;
            else if(name.contains("(VG)")) type=VIDEOGAME_SI;
            else type=MOVIE_SI;
            
            if(typeFilter!=null && !typeFilter.contains(type)) return null;
            
            Locator l=tm.createLocator(cleanLocator(type+"/"+name));
            t=tm.getTopic(l);
            if(t!=null) return t;
            
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(name);
            t.addType(getOrCreateTopic(tm,type));
            
            String dispName=name;
            int ind=dispName.indexOf("(");
            if(ind!=-1) dispName=dispName.substring(0,ind).trim();
            if(dispName.startsWith("\"")){
                int ind2=dispName.lastIndexOf("\"");
                if(ind2>ind) dispName=dispName.substring(1,ind2).trim();
            }
            t.setDisplayName(null,dispName);
        }
        return t;
    }
    public Topic getRoleTopic(TopicMap tm,String name) throws TopicMapException {
        Locator l=tm.createLocator(cleanLocator(ROLE_SI+"/"+name));
        Topic t=tm.getTopicWithBaseName(name+" (role name)");
        if(t==null) t=tm.getTopic(l);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(name+" (role name)");
            t.addType(getOrCreateTopic(tm,ROLE_SI));
        }
        return t;
    }
    public Topic getEpisodeTopic(TopicMap tm,Topic show,String name) throws TopicMapException {
        Locator l=tm.createLocator(cleanLocator(EPISODE_SI+"/"+show.getBaseName()+"/"+name));
        Topic t=tm.getTopicWithBaseName(show.getBaseName()+" {"+name+"}");
        if(t==null) t=tm.getTopic(l);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(show.getBaseName()+" {"+name+"}");
            Topic episodeTopic=getOrCreateTopic(tm,EPISODE_SI);
            t.addType(episodeTopic);
            Association a=tm.createAssociation(episodeTopic);
            a.addPlayer(show,getOrCreateTopic(tm,SHOW_SI));
            a.addPlayer(t,episodeTopic);
        }
        return t;
    }
    private static final HashMap<String,String> typeNames=new HashMap<String,String>();
    static {
        typeNames.put(KEYWORD_SI,"keyword");
        typeNames.put(LANGUAGE_SI,"language");
        typeNames.put(COUNTRY_SI,"country");
        typeNames.put(GENRE_SI,"genre");
        typeNames.put(LOCATION_SI,"location");
        typeNames.put(RUNTIME_SI,"runtime");
        typeNames.put(RUNTIMEINFO_SI,"runtimeinfo");
        typeNames.put(RELEASEDATE_SI,"releasedate");
        typeNames.put(RELEASEDATEINFO_SI,"releasedateinfo");
    }
    public Topic getKeywordTopic(TopicMap tm,String name,String keywordType) throws TopicMapException {
        String typeName=typeNames.get(keywordType);
        Locator l=tm.createLocator(cleanLocator(keywordType+"/"+name));
        Topic t=tm.getTopicWithBaseName(name+" ("+typeName+")");
        if(t==null) t=tm.getTopic(l);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(name+" ("+typeName+")");
            Topic keywordTopic=getOrCreateTopic(tm,keywordType);
            t.addType(keywordTopic);
        }
        return t;
    }
    public Topic getDateTopic(TopicMap tm,String text) throws TopicMapException {
        Locator l=tm.createLocator(cleanLocator(DATE_SI+"/"+text));
        Topic t=tm.getTopicWithBaseName(text);
        if(t==null) t=tm.getTopic(l);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(text);
            Topic keywordTopic=getOrCreateTopic(tm,DATE_SI);
            t.addType(keywordTopic);
        }
        return t;        
    }
    public Topic getPlaceTopic(TopicMap tm,String text) throws TopicMapException {
        Locator l=tm.createLocator(cleanLocator(PLACE_SI+"/"+text));
        Topic t=tm.getTopicWithBaseName(text);
        if(t==null) t=tm.getTopic(l);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(l);
            t.setBaseName(text);
            Topic keywordTopic=getOrCreateTopic(tm,PLACE_SI);
            t.addType(keywordTopic);
        }
        return t;                
    }
    public Association addAppearance(TopicMap tm,Topic actor,Topic show,Topic episode,Topic role,String type) throws TopicMapException {
        Topic actorType=getOrCreateTopic(tm,type);
        Association a=tm.createAssociation(actorType);
        a.addPlayer(actor,actorType);
        a.addPlayer(show,getOrCreateTopic(tm,SHOW_SI));
        if(episode!=null) a.addPlayer(episode,getOrCreateTopic(tm,EPISODE_SI));
        if(role!=null) a.addPlayer(role,getOrCreateTopic(tm,ROLE_SI));
        return a;
    }
    public Association addKeyword(TopicMap tm,Topic show,Topic keyword,String keywordType) throws TopicMapException {
         Topic keywordTopic=getOrCreateTopic(tm,keywordType);
         Association a=tm.createAssociation(keywordTopic);
         a.addPlayer(keyword,keywordTopic);
         a.addPlayer(show,getOrCreateTopic(tm,SHOW_SI));
         return a;
    }
    public void addDateOfBirth(TopicMap tm,Topic person,String text) throws TopicMapException {
        int ind=text.indexOf("(");
        if(ind!=-1) text=text.substring(0,ind).trim();
        ind=text.indexOf(",");
        String date=text;
        String place=null;
        if(ind!=-1) {
            date=text.substring(0,ind);
            place=text.substring(ind+1).trim();
        }
        Association a=tm.createAssociation(getOrCreateTopic(tm,DATEOFBIRTH_SI));
        a.addPlayer(person,getOrCreateTopic(tm,PERSON_SI));
        a.addPlayer(getDateTopic(tm,date),getOrCreateTopic(tm,DATE_SI));
        if(place!=null) a.addPlayer(getPlaceTopic(tm,place),getOrCreateTopic(tm,PLACE_SI));
    }
    public void addDateOfDeath(TopicMap tm,Topic person,String text) throws TopicMapException {
        int ind=text.indexOf("(");
        if(ind!=-1) text=text.substring(0,ind).trim();
        ind=text.indexOf(",");
        String date=text;
        String place=null;
        if(ind!=-1) {
            date=text.substring(0,ind);
            place=text.substring(ind+1).trim();
        }
        Association a=tm.createAssociation(getOrCreateTopic(tm,DATEOFDEATH_SI));
        a.addPlayer(person,getOrCreateTopic(tm,PERSON_SI));
        a.addPlayer(getDateTopic(tm,date),getOrCreateTopic(tm,DATE_SI));
        if(place!=null) a.addPlayer(getPlaceTopic(tm,place),getOrCreateTopic(tm,PLACE_SI));        
    }
    public void addRuntime(TopicMap tm,Topic show,Topic runtime,Topic runtimeinfo) throws TopicMapException {
        Topic runtimeType=getOrCreateTopic(tm,RUNTIME_SI);
        Association a=tm.createAssociation(runtimeType);
        a.addPlayer(show,getOrCreateTopic(tm,SHOW_SI));
        a.addPlayer(runtime,runtimeType);
        if(runtimeinfo!=null) a.addPlayer(runtimeinfo,getOrCreateTopic(tm,RUNTIMEINFO_SI));
    }
    public void addReleaseDate(TopicMap tm,Topic show,Topic date,Topic info) throws TopicMapException {
        Association a=tm.createAssociation(getOrCreateTopic(tm,RELEASEDATE_SI));
        a.addPlayer(show,getOrCreateTopic(tm,SHOW_SI));
        a.addPlayer(date,getOrCreateTopic(tm,DATE_SI));
        if(info!=null) a.addPlayer(info,getOrCreateTopic(tm,RELEASEDATEINFO_SI));
    }
    public void addBiography(TopicMap tm,Topic person,String text) throws TopicMapException {
        person.setData(getOrCreateTopic(tm,BIOGRAPHY_SI),getOrCreateTopic(tm,XTMPSI.getLang("en")),text);
    }
    public void addRealName(TopicMap tm,Topic person,String text) throws TopicMapException {
        person.setData(getOrCreateTopic(tm,REALNAME_SI),getOrCreateTopic(tm,XTMPSI.getLang(null)),text);        
    }
    public void addPlot(TopicMap tm,Topic show,String text) throws TopicMapException {
        show.setData(getOrCreateTopic(tm,PLOT_SI),getOrCreateTopic(tm,XTMPSI.getLang("en")),text);        
    }
    
    
    public boolean extractDirectorList(BufferedReader in,TopicMap tm) throws Exception {
        return extractPersonList(in,tm,DIRECTOR_SI);        
    }
    public boolean extractProducerList(BufferedReader in,TopicMap tm) throws Exception {
        return extractPersonList(in,tm,PRODUCER_SI);        
    }
    public boolean extractActorList(BufferedReader in,TopicMap tm) throws Exception {
        return extractPersonList(in,tm,ACTOR_SI);
    }
    public boolean extractPersonList(BufferedReader in,TopicMap tm,String type) throws Exception {
        String line=null;
        String actor=null;
        Topic actorTopic=null;
//        TopicMap originalMap=tm;
//        tm=new org.wandora.topicmap.memory.TopicMapImpl();
        createSchemaTopics(tm);
        int counter=0;
        while( (line=in.readLine())!=null ){
            counter++;

            if((counter%10)==0){
                setProgress(counter/100);
                if(forceStop()) break;
                takeNap(0);
            }
            if((counter%10000)==0){
                hlog("Extracting line "+counter);
            }
/*            if((counter%10000)==0){
                log("merging temporary map at line "+counter);
                takeNap(0);
                originalMap.mergeIn(tm);
                tm=new org.wandora.topicmap.memory.TopicMapImpl();
                log("extracting line "+counter);
            }            */
            
            if(line.trim().length()==0) continue;
            if(line.startsWith("-------------------------------------------------")) break;
            
            int tabIndex=line.indexOf("\t");
            if(tabIndex>0) {
                actor=line.substring(0,tabIndex);
                actorTopic=null;
            }
            
            String appearance=line.substring(tabIndex+1).trim();
            int spaceIndex=appearance.indexOf("  ");
            String show=null;
            String roleName=null;
            String episode=null;
            if(spaceIndex==-1) show=appearance;
            else{
                show=appearance.substring(0,spaceIndex);
                int roleIndex=appearance.indexOf("[",spaceIndex+2);
                if(roleIndex!=-1){
                    int b=appearance.indexOf("]",roleIndex);
                    if(b!=-1) roleName=appearance.substring(roleIndex+1,b);
                }
            }
            int episodeIndex=show.indexOf("{");
            if(episodeIndex!=-1){
                int b=show.indexOf("}",episodeIndex);
                if(b!=-1) {
                    episode=show.substring(episodeIndex+1,b);
                    show=show.substring(0,episodeIndex).trim();
                }
            }
//            if(show.startsWith("\"'")) continue; // MYSQL driver bug? throws exception
            
            Topic showTopic=getShowTopic(tm,show);
            if(showTopic!=null){
                if(actorTopic==null) actorTopic=getPersonTopic(tm,actor,type);
                Topic roleTopic=null;
                Topic episodeTopic=null;
                if(roleName!=null) roleTopic=getRoleTopic(tm,roleName);
                if(episode!=null) episodeTopic=getEpisodeTopic(tm,showTopic,episode);
                addAppearance(tm,actorTopic,showTopic,episodeTopic,roleTopic,type);            
            }
        }
        log("Extracted "+counter+" lines");
        return true;
    }
    public boolean extractKeywordList(BufferedReader in,TopicMap tm) throws Exception {
        return extractKeywords(in,tm,KEYWORD_SI);
    }
    public boolean extractLanguageList(BufferedReader in,TopicMap tm) throws Exception {
        return extractKeywords(in,tm,LANGUAGE_SI);
    }
    public boolean extractCountryList(BufferedReader in,TopicMap tm) throws Exception {
        return extractKeywords(in,tm,COUNTRY_SI);
    }
    public boolean extractLocationList(BufferedReader in,TopicMap tm) throws Exception {
        return extractKeywords(in,tm,LOCATION_SI);
    }
    public boolean extractGenreList(BufferedReader in,TopicMap tm) throws Exception {
        return extractKeywords(in,tm,GENRE_SI);
    }
    public boolean extractMovieList(BufferedReader in,TopicMap tm) throws Exception {
        return extractKeywords(in,tm,YEAR_SI);
    }
    public boolean extractKeywords(BufferedReader in,TopicMap tm,String keywordType) throws Exception {
        createSchemaTopics(tm);
        int counter=0;
        String line=null;
        while( (line=in.readLine())!=null ){
            counter++;

            if((counter%10)==0){
                setProgress(counter/100);
                if(forceStop()) break;
                takeNap(0);
            }
            if((counter%10000)==0){
                hlog("Extracting line "+counter);
            }
            
            int ind=line.indexOf("\t");
            if(ind==-1) continue;
            String show=line.substring(0,ind).trim();
            String keyword=line.substring(ind+1).trim();
            Topic showTopic=getShowTopic(tm,show);
            if(showTopic!=null){
                Topic keywordTopic=getKeywordTopic(tm,keyword,keywordType);
                addKeyword(tm,showTopic,keywordTopic,keywordType);
            }
        }
        log("Extracted "+counter+" lines");
        return true;
    }
    public boolean extractBiographyList(BufferedReader in,TopicMap tm) throws Exception {
        createSchemaTopics(tm);
        int counter=0;
        String line=null;
        String type="";
        StringBuffer collected=new StringBuffer();
        String name=null;
        boolean bgfound=false;
        while( (line=in.readLine())!=null ){
            counter++;

            if((counter%10)==0){
                setProgress(counter/100);
                if(forceStop()) break;
                takeNap(0);
            }
            if((counter%10000)==0){
                hlog("Extracting line "+counter);
            }
            
            if(line.length()==0){
                if(type.equals("NM")) name=collected.toString();
                else if(type.equals("RN")){
                    Topic personTopic=getPersonTopic(tm,name);
                    addRealName(tm,personTopic,collected.toString());
                }
                else if(type.equals("DB")){
                    Topic personTopic=getPersonTopic(tm,name);
                    addDateOfBirth(tm,personTopic,collected.toString());
                }
                else if(type.equals("DD")){
                    Topic personTopic=getPersonTopic(tm,name);
                    addDateOfDeath(tm,personTopic,collected.toString());                    
                }
                else if(type.equals("BG")){
                    if(!bgfound){
                        Topic personTopic=getPersonTopic(tm,name);
                        addBiography(tm,personTopic,collected.toString());                    
                        bgfound=true;
                    }
                }
                collected=new StringBuffer();
                type="";
            }
            else{
                type=line.substring(0,2);
                if(collected.length()>0) collected.append(" ");
                collected.append(line.substring(3).trim());
            }
            
            if(line.startsWith("----------------------------")) {
                name=null;
                bgfound=false;
                type="";
                collected=new StringBuffer();
            }
            
        }
        log("Extracted "+counter+" lines");
        return true;
    }
    public boolean extractPlotList(BufferedReader in,TopicMap tm) throws Exception {
        createSchemaTopics(tm);
        int counter=0;
        String line=null;
        String type="";
        StringBuffer collected=new StringBuffer();
        String name=null;
        boolean plfound=false;
        while( (line=in.readLine())!=null ){
            counter++;

            if((counter%10)==0){
                setProgress(counter/100);
                if(forceStop()) break;
                takeNap(0);
            }
            if((counter%10000)==0){
                hlog("Extracting line "+counter);
            }
            
            if(line.length()==0){
                if(type.equals("MV")) name=collected.toString();
                else if(type.equals("PL")){
                    if(!plfound){
                        Topic show=getShowTopic(tm,name);
                        if(show!=null){
                            addPlot(tm,show,collected.toString());                    
                        }
                        plfound=true;                            
                    }
                }
                collected=new StringBuffer();
                type="";
            }
            else{
                type=line.substring(0,2);
                if(collected.length()>0) collected.append(" ");
                collected.append(line.substring(3).trim());
            }
            
            if(line.startsWith("----------------------------")) {
                name=null;
                plfound=false;
                type="";
                collected=new StringBuffer();
            }
            
        }
        log("Extracted "+counter+" lines");
        return true;
    }
    public boolean extractRunningTimeList(BufferedReader in,TopicMap tm) throws Exception {
        createSchemaTopics(tm);
        int counter=0;
        String line=null;
        while( (line=in.readLine())!=null ){
            counter++;

            if((counter%10)==0){
                setProgress(counter/100);
                if(forceStop()) break;
                takeNap(0);
            }
            if((counter%10000)==0){
                hlog("Extracting line "+counter);
            }
            
            int ind=line.indexOf("\t");
            if(ind==-1) continue;
            String show=line.substring(0,ind).trim();
            String time=line.substring(ind+1).trim();
            Topic showTopic=getShowTopic(tm,show);
            if(showTopic!=null){
                String extra=null;
                String country=null;
                ind=time.indexOf("\t");
                if(ind!=-1){
                    extra=time.substring(ind+1).trim();
                    time=time.substring(0,ind).trim();
                }
                ind=time.indexOf(":");
                if(ind!=-1){
                    country=time.substring(0,ind);
                    time=time.substring(ind+1);
                }
                if(extra!=null){
                    if(country==null) country=extra;
                    else country+=" "+extra;
                }
                Topic runtimeTopic=getKeywordTopic(tm,time,RUNTIME_SI);
                Topic infoTopic=null;
                if(country!=null) infoTopic=getKeywordTopic(tm,country,RUNTIMEINFO_SI);
                addRuntime(tm,showTopic,runtimeTopic,infoTopic);
            }
        }
        log("Extracted "+counter+" lines");
        return true;
    }
    public boolean extractReleaseDateList(BufferedReader in,TopicMap tm) throws Exception {
        createSchemaTopics(tm);
        int counter=0;
        String line=null;
        while( (line=in.readLine())!=null ){
            counter++;

            if((counter%10)==0){
                setProgress(counter/100);
                if(forceStop()) break;
                takeNap(0);
            }
            if((counter%10000)==0){
                hlog("Extracting line "+counter);
            }
            
            int ind=line.indexOf("\t");
            if(ind==-1) continue;
            String show=line.substring(0,ind).trim();
            String time=line.substring(ind+1).trim();
            Topic showTopic=getShowTopic(tm,show);
            if(showTopic!=null){
                String extra=null;
                String country=null;
                ind=time.indexOf("\t");
                if(ind!=-1){
                    extra=time.substring(ind+1).trim();
                    time=time.substring(0,ind).trim();
                }
                ind=time.indexOf(":");
                if(ind!=-1){
                    country=time.substring(0,ind);
                    time=time.substring(ind+1);
                }
                if(extra!=null){
                    if(country==null) country=extra;
                    else country+=" "+extra;
                }
                Topic dateTopic=getDateTopic(tm,time);
                Topic infoTopic=null;
                if(country!=null) infoTopic=getKeywordTopic(tm,country,RELEASEDATEINFO_SI);
                addReleaseDate(tm,showTopic,dateTopic,infoTopic);
            }
        }
        log("Extracted "+counter+" lines");
        return true;
    }
    
}
