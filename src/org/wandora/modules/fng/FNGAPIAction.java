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
 */


package org.wandora.modules.fng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.application.Wandora;
// import org.wandora.application.tools.fng.opendata.v2.FngOpenDataStruct;
import org.wandora.application.tools.fng.opendata.v2b.FngOpenDataStruct;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.CachedAction;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.topicmap.TopicMapManager;
import org.wandora.modules.topicmap.ViewTopicAction;
import org.wandora.query2.*;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.velocity.GenericVelocityHelper;

/**
 *
 * @author olli
 */


public class FNGAPIAction extends CachedAction {
    
    protected String ARTIST_SI="http://www.muusa.net/taiteilija";
    protected String ARTWORK_SI="http://www.muusa.net/Teos";
    protected String ARTWORK_PARTIAL_SI="http://www.muusa.net/Teososa";
    protected String COLLECTION_SI="http://www.muusa.net/E78.Collection"; // used as association type, role type and topic type
    
    protected String AUTHOR_SI="http://www.muusa.net/P14.Production_carried_out_by"; // association type, roles artist and artwork
    protected String KEYWORD_SI="http://www.muusa.net/keyword";

    protected String TIME_SI="http://www.muusa.net/E52.Time-Span";
    protected String IS_IDENTIFIED_BY_SI = "http://www.muusa.net/P131.is_identified_by";
    protected String IS_IDENTIFIED_BY_ROLE1_SI = "http://www.muusa.net/P131.is_identified_by_role_1";
            
    protected String BIRTH_SI = "http://www.wandora.net/person_birth";
    protected String DEATH_SI = "http://www.wandora.net/person_death";
    protected String PERSON_SI = "http://www.muusa.net/person";
    protected String LOCATION_SI = "http://www.muusa.net/paikka";
    
    protected String ACQUISITION_SI = "http://www.muusa.net/P24.transferred_title_of";
    protected String TECHNIQUE_SI = "http://www.muusa.net/P32.used_general_technique";

    
    
    protected String outputModeParamKey="format";
    protected String defaultOutputMode="dc-xml";
    
    protected String topicParamKey="q";
    
    protected TopicMapManager tmManager;

    protected int maxTopics=100;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(this,TopicMapManager.class, deps);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        tmManager=manager.findModule(this,TopicMapManager.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        tmManager=null;
        super.stop(manager);
    }

    
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        
        Object o=settings.get("defaultOutput");
        if(o!=null) defaultOutputMode=o.toString();
        
        o=settings.get("outputParamKey");
        if(o!=null) outputModeParamKey=o.toString();
        
        o=settings.get("topicParamKey");
        if(o!=null) topicParamKey=o.toString();
        
        o=settings.get("maxTopics");
        if(o!=null) maxTopics=Integer.parseInt(o.toString());
        
        super.init(manager, settings);
    }

    @Override
    protected void returnOutput(InputStream cacheIn, HttpServletResponse resp) throws IOException {
        try{
            Map<String,String> metadata=readMetadata(cacheIn);
            String contenttype=metadata.get("contentType");
            String encoding=metadata.get("encoding");
            resp.setContentType(contenttype);
            resp.setCharacterEncoding(encoding);
            super.returnOutput(cacheIn, resp);
        }
        finally{
            cacheIn.close();
        }
    }


    
    
    
    protected String outputTopics(String query, String outputMode, TopicMap tm) throws TopicMapException, ActionException {
        ArrayList<Topic> outputTopics = new ArrayList();
        int count=0;
        
        // ------------------------------------------------- SEARCH ARTISTS ----
        
        if(query.startsWith("search-artists:") ||
           query.startsWith("artist-search:") ||
           query.startsWith("search-artists/") ||
           query.startsWith("artist-search/")) {
            
            if(query.startsWith("search-artists:")) query=query.substring("search-artists:".length());
            if(query.startsWith("artist-search:")) query=query.substring("artist-search:".length());
            if(query.startsWith("search-artists/")) query=query.substring("search-artists/".length());
            if(query.startsWith("artist-search/")) query=query.substring("artist-search/".length());
                       
            Directive directive=
                new Unique(
                    new Union(
                        new Directive[] {
                            new Instances().from( ARTIST_SI ).where(
                                new Or(
                                    new Exists( new BaseName().where( new Contains(query) ) ),
                                    new Exists( new Variant().where( new Contains(query) ) )
                                )
                            ),
                            new Players(IS_IDENTIFIED_BY_SI, IS_IDENTIFIED_BY_ROLE1_SI).whereInputIs(ARTIST_SI).from(
                                new Instances().from( ARTIST_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            /*
                            new Players(AUTHOR_SI, ARTIST_SI).whereInputIs(ARTWORK_SI).from(
                                new Instances().from( ARTWORK_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            */
                            new Players(DEATH_SI, PERSON_SI).whereInputIs(LOCATION_SI).from(
                                new Instances().from( LOCATION_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(DEATH_SI, PERSON_SI).whereInputIs(TIME_SI).from(
                                new Instances().from( TIME_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(BIRTH_SI, PERSON_SI).whereInputIs(LOCATION_SI).from(
                                new Instances().from( LOCATION_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(BIRTH_SI, PERSON_SI).whereInputIs(TIME_SI).from(
                                new Instances().from( TIME_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            )
                        }
                    ).ofActiveRole()
                );
            
            if(maxTopics>=0) directive=new First(maxTopics,directive);
            
            try {
                QueryContext context=new QueryContext(tm, null);
                ArrayList<ResultRow> res=directive.doQuery(context, new ResultRow(null));

                for(ResultRow row : res) {
                    Object v=row.getActiveValue();
                    if(!(v instanceof Topic)) continue;
                    Topic t=(Topic)v;
                    outputTopics.add(t);
                }
            }
            catch(QueryException qe){
                logging.warn("Error executing query",qe);
                throw new ActionException("Error executing query");
            }
        }
        
        // -------------------------------------------------------- ARTISTS ----
        
        else if(query.startsWith("artists:") ||
                query.startsWith("artist:") ||
                query.startsWith("artists/") ||
                query.startsWith("artist/")) {
            
            if(query.startsWith("artists:")) query = query.substring("artists:".length());
            if(query.startsWith("artist:")) query = query.substring("artist:".length());
            if(query.startsWith("artists/")) query = query.substring("artists/".length());
            if(query.startsWith("artist/")) query = query.substring("artist/".length());
            
            Topic rootTopic=ViewTopicAction.getTopic(query, tm);

            if(rootTopic!=null) {
                Topic artist=tm.getTopic(ARTIST_SI);
                Topic artwork=tm.getTopic(ARTWORK_SI);
                Topic collection=tm.getTopic(COLLECTION_SI);
                Topic author=tm.getTopic(AUTHOR_SI);
                Topic artworkPartial=tm.getTopic(ARTWORK_PARTIAL_SI);

                if(artist==null) {
                    logging.warn("Couldn't find some of the needed base topics.");
                    return "";
                }
                if(artwork==null || collection==null || author==null || artworkPartial==null){
                    logging.warn("Couldn't find some of the needed base topics.");
                }
                
                if((artwork != null && rootTopic.isOfType(artwork)) || (artworkPartial != null && rootTopic.isOfType(artworkPartial))) {
                    Collection<Association> artworkAuthors=rootTopic.getAssociations(author, artist);
                    for(Association artworkAuthor : artworkAuthors){
                        Topic artistTopic = artworkAuthor.getPlayer(artist);
                        if(artistTopic!=null) {
                            outputTopics.add(artistTopic);
                            if(count++>=maxTopics && maxTopics>=0) break;
                        }
                    }
                }
                else if(collection != null && rootTopic.isOfType(collection)){
                    Collection<Association> collectionArtworks=rootTopic.getAssociations(collection, collection);
                    for(Association collectionArtwork : collectionArtworks){
                        Topic art=collectionArtwork.getPlayer(artwork);
                        if(art!=null) {
                            HashSet<Topic> artists = new HashSet();
                            Collection<Association> artworkAuthors=art.getAssociations(author, artist);
                            for(Association artworkAuthor : artworkAuthors){
                                Topic artistTopic = artworkAuthor.getPlayer(artist);
                                if(artistTopic!=null) {
                                    artists.add(artistTopic);
                                    if(artists.size() > maxTopics) break;
                                }
                            }
                            outputTopics.addAll(artists);
                        }
                    }            
                }
                else if(rootTopic.isOfType(artist)) {
                    outputTopics.add(rootTopic);
                }
            }
            
        }
        

        // ------------------------------------------------ SEARCH ARTWORKS ----
        
        else if(query.startsWith("search:") || 
                query.startsWith("search-artworks:") ||
                query.startsWith("artwork-search:") ||
                query.startsWith("search/") || 
                query.startsWith("search-artworks/") ||
                query.startsWith("artwork-search/")) {
            
            if(query.startsWith("search:")) query=query.substring("search:".length());
            if(query.startsWith("search-artworks:")) query=query.substring("search-artworks:".length());
            if(query.startsWith("artwork-search:")) query=query.substring("artwork-search:".length());
            if(query.startsWith("search/")) query=query.substring("search/".length());
            if(query.startsWith("search-artworks/")) query=query.substring("search-artworks/".length());
            if(query.startsWith("artwork-search/")) query=query.substring("artwork-search/".length());
            
            Directive directive=
                new Unique(
                    new Union(
                        new Directive[] {
                            new Instances().from( ARTWORK_SI, ARTWORK_PARTIAL_SI ).where(
                                new Or(
                                    new Exists( new BaseName().where( new Contains(query) ) ),
                                    new Exists( new Variant().where( new Contains(query) ) )
                                )
                            ),
                            new Players(AUTHOR_SI, ARTWORK_SI).whereInputIs(ARTIST_SI).from(
                                new Instances().from( ARTIST_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(KEYWORD_SI, ARTWORK_SI).whereInputIs(KEYWORD_SI).from(
                                new Instances().from( KEYWORD_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(TIME_SI, ARTWORK_SI).whereInputIs(TIME_SI).from(
                                new Instances().from( TIME_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(ACQUISITION_SI, ARTWORK_SI).whereInputIs(TIME_SI).from(
                                new Instances().from( TIME_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            ),
                            new Players(TECHNIQUE_SI, ARTWORK_SI).whereInputIs(TECHNIQUE_SI).from(
                                new Instances().from( TECHNIQUE_SI ).where(
                                    new Or(
                                        new Exists( new BaseName().where( new Contains(query) ) ),
                                        new Exists( new Variant().where( new Contains(query) ) )
                                    )
                                )
                            )
                        }
                    ).ofActiveRole()
                );
            
            if(maxTopics>=0) directive=new First(maxTopics,directive);
            
            try {
                QueryContext context=new QueryContext(tm, null);
                ArrayList<ResultRow> res=directive.doQuery(context, new ResultRow(null));

                for(ResultRow row : res) {
                    Object v=row.getActiveValue();
                    if(!(v instanceof Topic)) continue;
                    Topic t=(Topic)v;
                    outputTopics.add(t);
                }
            }
            catch(QueryException qe){
                logging.warn("Error executing query",qe);
                throw new ActionException("Error executing query");
            }
        }
        
        
        
        // ------------------------------------------------------- ARTWORKS ----
        
        
        else {
            
            if(query.startsWith("artwork:")) query=query.substring("artwork:".length());
            if(query.startsWith("artwork/")) query=query.substring("artwork/".length());
            
            Topic rootTopic=ViewTopicAction.getTopic(query, tm);
            if(rootTopic!=null) {

                Topic artist=tm.getTopic(ARTIST_SI);
                Topic artwork=tm.getTopic(ARTWORK_SI);
                Topic collection=tm.getTopic(COLLECTION_SI);
                Topic author=tm.getTopic(AUTHOR_SI);
                Topic artworkPartial=tm.getTopic(ARTWORK_PARTIAL_SI);

                if(artwork == null) {
                    logging.warn("Couldn't find some of the needed base topics.");
                    return "";
                }
                if(artist==null || collection==null || author==null || artworkPartial==null){
                    logging.warn("Couldn't find some of the needed base topics.");
                }

                if(artist != null && author != null && rootTopic.isOfType(artist)){
                    Collection<Association> as=rootTopic.getAssociations(author, artist);
                    for(Association a : as){
                        Topic art=a.getPlayer(artwork);
                        if(art!=null) {
                            outputTopics.add(art);
                            if(count++>=maxTopics && maxTopics>=0) break;
                        }
                    }
                }
                else if(collection != null && rootTopic.isOfType(collection)){
                    Collection<Association> as=rootTopic.getAssociations(collection, collection);
                    for(Association a : as){
                        Topic art=a.getPlayer(artwork);
                        if(art!=null) {
                            outputTopics.add(art);
                            if(count++>=maxTopics && maxTopics>=0) break;
                        }
                    }            
                }
                else if((rootTopic.isOfType(artwork)) || (artworkPartial != null && rootTopic.isOfType(artworkPartial))) {
                    outputTopics.add(rootTopic);
                }
            }
        }
        
        
        // -------------------------------------------- SERIALIZE ARRAYLIST ----
        
        count = 0;
        StringBuilder sb=new StringBuilder("");
        if(!outputTopics.isEmpty()) {
            for(Topic t : outputTopics) {
                FngOpenDataStruct struct=new FngOpenDataStruct();            
                struct.populate(t, tm);
                // System.out.println("Populating...");
                String topicOutput = struct.toString(outputMode);
                if(topicOutput != null && topicOutput.length()>0) {
                    // System.out.println("Appending...");
                    sb.append(topicOutput);
                    if(outputMode.equals("dc-json")) sb.append(",");
                    if(count++>=maxTopics && maxTopics>=0) break;
                }
            }
            if(outputMode.equals("dc-json")) trimLastComma(sb);
        }
        
        if(count == 0) {
            throw new ActionException("Topic not found");
        }

        return sb.toString();
    }
    
    
    

    
    
    

    @Override
    protected boolean doOutput(HttpServletRequest req, HttpMethod method, String action, OutputProvider out, org.wandora.modules.usercontrol.User user) throws ServletException, IOException, ActionException {
        LinkedHashMap<String,String> params=getCacheKeyParams(req, method, action);
        if(params==null) return false;
        String outputMode=params.get("outputMode");
        String topicRequest=params.get("q");
        
        try {
            TopicMap tm=null;
            try { tm=tmManager.getTopicMap(); }
            catch(Exception e) {}
            if(tm == null) {
                try { tm = Wandora.getWandora().getTopicMap(); }
                catch(Exception e) {}
            }
            if(tm == null) throw new ActionException("Topicmap not found");
/*            Topic topic=ViewTopicAction.getTopic(topicRequest, tm);
            if(topic==null) throw new ActionException("Topic not found",this);
*/            
            String output=outputTopics(topicRequest,outputMode, tm);
            if(output==null) throw new ActionException("Topic not found",this);
            
            String contentType="text/plain";
            String encoding="UTF-8";
            if(outputMode.equals("dc-xml")){
                contentType="application/xml";
                String nameSpaces="xmlns:dcx=\"http://purl.org/dc/xml/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"";
                output="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n"+
                        "<dcx:descriptionSet "+nameSpaces+">\n"+
                        output+
                        "\n</dcx:descriptionSet>";
            }
            else if(outputMode.equals("dc-ds-xml")){
                contentType="application/xml";
                String nameSpaces="xmlns:dcds=\"http://purl.org/dc/xmlns/2008/09/01/dc-ds-xml/\" xml:base=\"http://purl.org/dc/terms/\"";
                output="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n"+
                        "<dcds:descriptionSet "+nameSpaces+">\n"+
                        output+
                        "\n</dcds:descriptionSet>";
            }
            else if(outputMode.equals("dc-json")){
                contentType="application/json";
                output = "{ \"descriptionSet\": [\n"+output+"\n] }";
            }
            else if(outputMode.equals("dc-text")){
                output = "@prefix dcterms: <http://purl.org/dc/terms/> .\n\nDescriptionSet (\n"+output+"\n)";
            }
            
            OutputStream outStream=out.getOutputStream();
            try{
                HashMap<String,String> metadata=new HashMap<String,String>();
                metadata.put("contentType",contentType);
                metadata.put("encoding",encoding);
                writeMetadata(metadata, outStream);

                outStream.write(output.getBytes(encoding));
            }
            finally{
                outStream.close();
            }
            return true;
        }
        catch(TopicMapException tme){
            logging.warn(tme);
            throw new ActionException("Server error",this);
        }
    }

    protected LinkedHashMap<String,String> getCacheKeyParams(HttpServletRequest req, HttpMethod method, String action) throws ActionException {
        // note that similar handling and checking of outputmode is done in FNGAPIExceptionHandler
        String outputMode=req.getParameter(outputModeParamKey);
        if(outputMode==null || outputMode.length()==0) {
            if(defaultOutputMode==null) throw new ActionException(outputModeParamKey+" parameter not supplied");
            else outputMode=defaultOutputMode;
        }
        
        if(!outputMode.equals("dc-ds-xml") && !outputMode.equals("dc-xml") && !outputMode.equals("dc-json")
             && !outputMode.equals("dc-text")) throw new ActionException("invalid "+outputModeParamKey);
        
        String topicRequest=req.getParameter(topicParamKey);
        if(topicRequest==null || topicRequest.length()==0) throw new ActionException(topicParamKey+" parameter not supplied");
        
        LinkedHashMap<String,String> params=new LinkedHashMap<String,String>();
        params.put("outputMode",outputMode);
        params.put("q",topicRequest);
        return params;
    }
    
    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action) {
        try{
            LinkedHashMap<String,String> params=getCacheKeyParams(req, method, action);
            if(params!=null) return buildCacheKey(params);
            else return null;
        }
        catch(ActionException ae){return null;}
    }
    
    
    protected void trimLastComma(StringBuilder sb) {
        if(sb != null) {
            int l = sb.lastIndexOf(",");
            if(l > 0) {
                sb.deleteCharAt(l);
            }
        }
    }
}
