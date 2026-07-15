 /*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
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
package org.wandora.application.tools.extractors.reddit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
// import org.wandora.dep.json.*;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.MultipartBody;

/**
 *
 * @author Eero Lehtonen
 * @author akivela
 */
public abstract class AbstractRedditExtractor extends AbstractExtractor {


	private static final long serialVersionUID = 1L;

	private static final String ERROR_MSG = "Reddit API error occurred:";
    private static final String ERROR_RESPONSE_MSG = "API response follows:";
    
    protected static final String apiRoot = "http://api.reddit.com/";
    private static final String THING_TYPE_COMMENT = "t1";
    private static final String THING_TYPE_ACCOUNT = "t2";
    private static final String THING_TYPE_LINK = "t3";
    private static final String THING_TYPE_MESSAGE = "t4";
    private static final String THING_TYPE_SUBREDDIT = "t5";
    private static final String THING_TYPE_AWARD = "t6";
    private static final String THING_TYPE_PROMO = "t8";
    private static final String THING_TYPE_MORE = "more";
    private static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    private static final String SI_ROOT = "http://wandora.org/si/reddit/";
    private static final String COMMENT_SI = SI_ROOT + "comment";
    private static final String LINK_SI = SI_ROOT + "link";
    private static final String ACCOUNT_SI = SI_ROOT + "account";
    private static final String MESSAGE_SI = SI_ROOT + "message";
    private static final String SUBREDDIT_SI = SI_ROOT + "subreddit";
    private static final String AWARD_SI = SI_ROOT + "award";
    private static final String PROMO_SI = SI_ROOT + "promo";
    private static final String PARENT_SI = SI_ROOT + "parent";
    private static final String CHILD_SI = SI_ROOT + "child";
    private static final String PAR_CHILD_SI = SI_ROOT + "parent_child";
    private static final String DESTINATION_SI = SI_ROOT + "destination";

    //Common
    private static final String CREATED_SI = SI_ROOT + "created";
    private static final String CREATED_U_SI = SI_ROOT + "created_utc";
    //Link, Comment
    private static final String BODY_SI = SI_ROOT + "body";
    private static final String UP_SI = SI_ROOT + "upvotes";
    private static final String DOWN_SI = SI_ROOT + "downvotes";
    private static final String SCORE_SI = SI_ROOT + "score";
    // Account
    private static final String CKARMA_SI = SI_ROOT + "comment_karma";
    private static final String LKARMA_SI = SI_ROOT + "link_karma";
    // Subreddit
    private static final String DESC_SI = SI_ROOT + "description";
    private static final String PUB_DESC_SI = SI_ROOT + "public_description";
    private static final String ACC_ACT_SI = SI_ROOT + "accounts_active";
    private static final String SUBS_SI = SI_ROOT + "subscribers";
    private static final String TITLE_SI = SI_ROOT + "title";
    
    
    private static HashMap<String, Boolean> CRAWL_SETTINGS = null;
    private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    private static ArrayList<String> extracted;
    
    private static int progress = 0;
    
    protected Requester requester = new Requester();
    

    
    @Override
    public String getName() {
        return "Abstract Reddit extractor";
    }

    @Override
    public String getDescription() {
        return "Abstract extractor for Reddit.";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_reddit.png");
    }
    
    private final String[] contentTypes = new String[]{
        "text/plain", "text/json", "application/json"
    };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }

    @Override
    public boolean runInOwnThread() {
        return false;
    }

    // -----------------------------------------------------------------
    
    public static void resetExtracted() {
        extracted = new ArrayList<>();
    }

    
    private static final Map<Integer, String> additionalPhrases;
    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(429, "Too many requests");
        additionalPhrases = Collections.unmodifiableMap(map);
    }
    
    protected static String statusToPhrase(int status) {
        String phrase = null; // WAS: HttpStatus.getStatusText(status);
        if(phrase == null && additionalPhrases.containsKey(status)){
            phrase = additionalPhrases.get(status);
        }
        return phrase;
    }
    
    
    // ------------------------------------------------------------- HELPERS ---
    
    
    protected static Topic getRedditClass(TopicMap tm) throws TopicMapException {
        Topic reddit = getOrCreateTopic(tm, SI_ROOT, "Reddit");
        makeSubclassOf(tm, reddit, getWandoraClassTopic(tm));
        return reddit;
    }

    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, LANG_SI);
    }

    protected static String unixToString(long unixTimeStamp) {
        Date d = new Date(unixTimeStamp * 1000);
        return dateTimeFormat.format(d);
    }

    
    // ------------------------------------------------------------------
    
    
    protected static HashMap<String, Topic> getThingTypes(TopicMap tm) throws TopicMapException {
        HashMap<String, Topic> types = new HashMap<>();

        types.put(THING_TYPE_COMMENT, getOrCreateTopic(tm, COMMENT_SI, "Comment"));
        types.put(THING_TYPE_LINK, getOrCreateTopic(tm, LINK_SI, "Link"));
        types.put(THING_TYPE_ACCOUNT, getOrCreateTopic(tm, ACCOUNT_SI, "Account"));
        types.put(THING_TYPE_MESSAGE, getOrCreateTopic(tm, MESSAGE_SI, "Message"));
        types.put(THING_TYPE_SUBREDDIT, getOrCreateTopic(tm, SUBREDDIT_SI, "Subreddit"));
        types.put(THING_TYPE_AWARD, getOrCreateTopic(tm, AWARD_SI, "Award"));
        types.put(THING_TYPE_PROMO, getOrCreateTopic(tm, PROMO_SI, "Promo"));

        Topic redditClass = getRedditClass(tm);
        for (Topic type : types.values()) {
            makeSubclassOf(tm, type, redditClass);
        }

        return types;
    }

    
    protected static HashMap<String, Topic> getAssociationTypes(TopicMap tm) throws TopicMapException {

        HashMap<String, Topic> types = new HashMap<>();

        types.put("Parent", getOrCreateTopic(tm, PARENT_SI, "Parent"));
        types.put("Child", getOrCreateTopic(tm, CHILD_SI, "Child"));
        types.put("Parent-Child", getOrCreateTopic(tm, PAR_CHILD_SI, "Parent-Child"));
        types.put("Destination", getOrCreateTopic(tm, DESTINATION_SI, "Destination"));
        
        types.put("Lang", getLangTopic(tm));

        // Comment, Link
        types.put("Body", getOrCreateTopic(tm, BODY_SI, "Body"));
        types.put("Created", getOrCreateTopic(tm, CREATED_SI, "Created"));
        types.put("Created UTC", getOrCreateTopic(tm, CREATED_U_SI, "Created UTC"));
        types.put("Upvotes", getOrCreateTopic(tm, UP_SI, "Upvotes"));
        types.put("Downvotes", getOrCreateTopic(tm, DOWN_SI, "Downvotes"));
        types.put("Score", getOrCreateTopic(tm, SCORE_SI, "Score"));

        // Account
        types.put("Comment Karma", getOrCreateTopic(tm, CKARMA_SI, "Comment Karma"));
        types.put("Link Karma", getOrCreateTopic(tm, LKARMA_SI, "Link Karma"));

        // Subreddit
        types.put("Description", getOrCreateTopic(tm, DESC_SI, "Description"));
        types.put("Public Description", getOrCreateTopic(tm, PUB_DESC_SI, "Public Description"));
        types.put("Accounts Active", getOrCreateTopic(tm, ACC_ACT_SI, "Accounts Active"));
        types.put("Subscribers", getOrCreateTopic(tm, SUBS_SI, "Subscribers"));
        types.put("Title", getOrCreateTopic(tm, TITLE_SI, "Title"));

        Topic redditClass = getRedditClass(tm);
        for(Topic type : types.values()){
            type.addType(redditClass);
        }

        return types;
    }

    
    private void associateParent(TopicMap tm, Topic commentTopic, Topic parentTopic) throws TopicMapException {
        HashMap<String, Topic> types = getAssociationTypes(tm);

        Association a = tm.createAssociation(types.get("Parent-Child"));
        a.addPlayer(parentTopic, types.get("Parent"));
        a.addPlayer(commentTopic, types.get("Child"));
    }

    
    private void associateAccount(TopicMap tm, JSONObject thing, Topic account, HashMap<String, Topic> types) throws TopicMapException, JSONException {
        if(account == null) return;

        JSONObject thingData = thing.getJSONObject("data");
        String thingKind = thing.getString("kind");
        String thingId = thingData.getString("name");

        Topic thingTopic = getOrCreateTopic(tm, SI_ROOT + thingId);

        Association a = tm.createAssociation(types.get(THING_TYPE_ACCOUNT));
        a.addPlayer(account, types.get(THING_TYPE_ACCOUNT));
        a.addPlayer(thingTopic, types.get(thingKind));
    }
    

    private void associateSubreddit(TopicMap tm, JSONObject thing, Topic subreddit, HashMap<String, Topic> types) throws TopicMapException, JSONException {
        JSONObject thingData = thing.getJSONObject("data");
        String thingKind = thing.getString("kind");
        String thingId = thingData.getString("name");

        Topic thingTopic = getOrCreateTopic(tm, SI_ROOT + thingId);

        Association a = tm.createAssociation(types.get(THING_TYPE_SUBREDDIT));
        a.addPlayer(subreddit, types.get(THING_TYPE_SUBREDDIT));
        a.addPlayer(thingTopic, types.get(thingKind));
    }

    
    private void addLinkOccurenceData(TopicMap tm, JSONObject linkData, Topic linkTopic) throws TopicMapException, JSONException {
        HashMap<String, Topic> types = getAssociationTypes(tm);
        Topic lang = types.get("Lang");

        Long created = linkData.getLong("created");
        String createdString = unixToString(created);
        if(createdString != null) {
            linkTopic.setData(types.get("Created"), lang, createdString);
        }

        Long createdUtc = linkData.getLong("created_utc");
        String createdUtcString = unixToString(createdUtc);
        if(createdUtcString != null) {
            linkTopic.setData(types.get("Created UTC"), lang, createdUtcString);
        }
    }

    
    private void addCommentOccurenceData(TopicMap tm, JSONObject commentData, Topic commentTopic) throws TopicMapException, JSONException {
        HashMap<String, Topic> types = getAssociationTypes(tm);
        Topic lang = types.get("Lang");

        String body = commentData.getString("body");
        if(body != null) {
            commentTopic.setData(types.get("Body"), lang, body);
        }

        Long created = commentData.getLong("created");
        String createdString = unixToString(created);
        if(createdString != null) {
            commentTopic.setData(types.get("Created"), lang, createdString);
        }

        String ups = Integer.toString(commentData.getInt("ups"));
        if(ups != null) {
            commentTopic.setData(types.get("Upvotes"), lang, ups);
        }

        String downs = Integer.toString(commentData.getInt("downs"));
        if(downs != null) {
            commentTopic.setData(types.get("Downvotes"), lang, downs);
        }

        //String score = commentData.getString("score");
        //if(score != null) commentTopic.setData(types.get("Score"), lang, score);
    }

    
    private void addAccountOccurenceData(TopicMap tm, JSONObject accountData, Topic accountTopic) throws TopicMapException, JSONException {
        HashMap<String, Topic> types = getAssociationTypes(tm);
        Topic lang = types.get("Lang");

        Long created = accountData.getLong("created");
        String createdString = unixToString(created);
        if(createdString != null) {
            accountTopic.setData(types.get("Created"), lang, createdString);
        }

        Long createdUtc = accountData.getLong("created_utc");
        String createdUtcString = unixToString(createdUtc);
        if(createdUtcString != null) {
            accountTopic.setData(types.get("Created UTC"), lang, createdUtcString);
        }

        String cKarma = Integer.toString(accountData.getInt("comment_karma"));
        if(cKarma != null) {
            accountTopic.setData(types.get("Comment Karma"), lang, cKarma);
        }

        String lKarma = Integer.toString(accountData.getInt("link_karma"));
        if(lKarma != null) {
            accountTopic.setData(types.get("Link Karma"), lang, lKarma);
        }
    }

    
    private void addSubredditOccurrenceData(TopicMap tm, JSONObject subredditData, Topic subredditTopic) throws TopicMapException, JSONException {
        HashMap<String, Topic> types = getAssociationTypes(tm);
        Topic lang = types.get("Lang");

        Long created = subredditData.getLong("created");
        String createdString = unixToString(created);
        if(createdString != null) {
            subredditTopic.setData(types.get("Created"), lang, createdString);
        }

        Long createdUtc = subredditData.getLong("created_utc");
        String createdUtcString = unixToString(createdUtc);
        if(createdUtcString != null) {
            subredditTopic.setData(types.get("Created UTC"), lang, createdUtcString);
        }

        String desc = subredditData.getString("description");
        if(desc != null && desc.length() > 0) {
            subredditTopic.setData(types.get("Description"), lang, desc);
        }

        String pubDesc = subredditData.getString("public_description");
        if(pubDesc != null && pubDesc.length() > 0) {
            subredditTopic.setData(types.get("Public Description"), lang, pubDesc);
        }

        String accAct = Integer.toString(subredditData.getInt("accounts_active"));
        if(accAct != null) {
            subredditTopic.setData(types.get("Accounts Active"), lang, accAct);
        }

        String subs = Integer.toString(subredditData.getInt("subscribers"));
        if(subs != null) {
            subredditTopic.setData(types.get("Subscribers"), lang, subs);
        }

        String title = subredditData.getString("title");
        if(title != null && title.length() > 0) {
            subredditTopic.setData(types.get("Title"), lang, title);
        }
    }
    

    
    /*
        This is being called from the RedditExtractorUI.
    */
    protected static void getSubmissions(String q, ParseCallback<JsonNode> callback) {
        String u = apiRoot + "search?q=" + urlEncode(q);
        Requester tempRequester = new Requester();
        tempRequester.addRequest(Unirest.get(u),callback);
        tempRequester.runNext();
    }
    
    
    /*
        This is being called from the RedditExtractorUI.
    */
    protected static void getSubreddits(String q, ParseCallback<JsonNode> callback) {
        String u = apiRoot + "subreddits/search?q=" + urlEncode(q);
        Requester tempRequester = new Requester();
        tempRequester.addRequest(Unirest.get(u),callback);
        tempRequester.runNext();
    }

    
    
    // ---------------------------------------------------------------------- //
    
    
    protected void parseThing(JSONObject thing, TopicMap tm, HashMap<String, Topic> thingTypes, HashMap<String, Boolean> crawlSettings) {
        CRAWL_SETTINGS = crawlSettings;
        parseThing(thing, tm, thingTypes);
    }

    
    protected void parseThing(JSONObject thing, TopicMap tm, HashMap<String, Topic> thingTypes) {
        progress = (progress+1)%100;
        getDefaultLogger().setProgress(progress);
        
        if(forceStop()) {
            return;
        }

        try {
            if(thing.has("error")){
                Object error = thing.get("error");
                throw new JSONException("API error \"" + statusToPhrase((int)error) + "\"");
            }
          
            JSONObject data = thing.getJSONObject("data");
            String kind = thing.getString("kind");
            switch(kind) {
                case THING_TYPE_COMMENT:
                    parseComment(thing, thingTypes, tm);
                    break;
                case THING_TYPE_LINK:
                    parseLink(thing, thingTypes, tm);
                    break;
                case THING_TYPE_ACCOUNT:
                    parseAccount(thing, thingTypes, tm);
                    break;
                case THING_TYPE_SUBREDDIT:
                    parseSubreddit(thing, thingTypes, tm);
                    break;
            }

            if(data.has("children")) {
                JSONArray children = data.getJSONArray("children");
                for(int i = 0; i < children.length(); i++) {
                    JSONObject child;
                    try {
                        child = children.getJSONObject(i);
                    } 
                    catch(JSONException jse) {
                        jse.printStackTrace();
                        continue;
                    }
                    parseThing(child, tm, thingTypes);
                }
            }
        } 
        catch(JSONException jse) {
            log("Parsing response failed: " + jse.getMessage());
            try {
                log("The JSON message in question was");
                log(thing.toString(2));
            }
            catch(JSONException jsee) {
                log("The message JSON was invalid.");
            }
            jse.printStackTrace();
        } 
        catch (TopicMapException tme){
            log(tme.getMessage());
        }
    }

    
    private void parseMore(JSONObject commentData, JSONObject child, TopicMap tm, HashMap<String, Topic> thingTypes) throws JSONException, TopicMapException {
        if(forceStop()) {
            return;
        }

        String id = commentData.getString("name");
        String link_id = commentData.getString("link_id");

        StringBuilder childString = new StringBuilder();
        JSONArray children = child.getJSONObject("data").getJSONArray("children");
        int count = children.length();
        for(int i=0; i<count; i++) {
            childString.append(children.getString(i));
            if(i < count - 1) {
                childString.append(",");
            }
        }

        MultipartBody r = Unirest.post(apiRoot + "api/morechildren")
                .header("accept", "application/json")
                .field("api_type", "json")
                .field("id", id)
                .field("link_id", link_id)
                .field("children", childString.toString());
        
        ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
            @Override
            public void run(HttpResponse<JsonNode> response) {
                try {
                    JSONObject respJSON = response.getBody().getObject();
                    if(respJSON.getJSONObject("json").getJSONArray("errors").length() == 0) {
                        JSONArray things = respJSON
                                .getJSONObject("json")
                                .getJSONObject("data")
                                .getJSONArray("things");
                        for(int i = 0; i < things.length(); i++) {
                            parseThing(things.getJSONObject(i), tm, thingTypes);
                        }
                    }
                }
                catch (Exception e) {
                    logParseCallbackRunError(e);
                }

            }
            @Override
            protected void error(Exception e, String body) {
                logParseCallbackError(e, body);
            }
        };
        requester.addRequest(r, callback);
    }

    
    private Topic parseLink(JSONObject l, HashMap<String, Topic> thingTypes, TopicMap tm) throws JSONException, TopicMapException {
        if(forceStop()) {
            return null;
        }
        
        final JSONObject link = l;

        JSONObject linkData = link.getJSONObject("data");
        String kind = link.getString("kind");
        String id = linkData.getString("name");
        String title = linkData.getString("title");

        hlog("Parsing link " + title);
        
        Topic linkTopic = getOrCreateTopic(tm, SI_ROOT + id);

        linkTopic.setDisplayName("en", title);
        linkTopic.setBaseName(title + " (" + id + ")");
        if(!linkData.getBoolean("is_self")){ // Handle link 
            String linkUrl = linkData.getString("url");
            Topic destinationTopic = getOrCreateTopic(tm, linkUrl);
            destinationTopic.setSubjectLocator(new Locator(linkUrl));
            HashMap<String,Topic> assTypes = getAssociationTypes(tm);
            Association a = tm.createAssociation(assTypes.get("Destination"));
            a.addPlayer(linkTopic, thingTypes.get(THING_TYPE_LINK));
            a.addPlayer(destinationTopic, assTypes.get("Destination"));
        }
        linkTopic.addType(thingTypes.get(kind));

        addLinkOccurenceData(tm, linkData, linkTopic);

        if(linkData.has("author")) {
            final String author = linkData.getString("author");

            if(!extracted.contains(author) && CRAWL_SETTINGS.get("linkUser")) {
                String authorUrl = apiRoot + "user/" + author + "/about.json";

                ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                    @Override
                    public void run(HttpResponse<JsonNode> response) {
                        try {
                            JSONObject respObject = response.getBody().getObject();
                            Topic account = null;
                            if(respObject.has("kind") && respObject.getString("kind").equals(THING_TYPE_ACCOUNT)) {
                                extracted.add(author);
                                account = parseAccount(respObject, thingTypes, tm);
                            }
                            if (account != null) {
                                associateAccount(tm, link, account, thingTypes);
                            }
                        }
                        catch (JSONException | TopicMapException e) {
                            logParseCallbackRunError(e);
                        }
                    }
                    @Override
                    protected void error(Exception e, String body) {
                        logParseCallbackError(e, body);
                    }
                };
                
                requester.addRequest(Unirest.get(authorUrl), callback);
            }
        }

        try {
            if(linkData.has("subreddit")) {
                String subredditId = linkData.getString("subreddit_id");
                String subredditName = linkData.getString("subreddit");
                Topic subredditTopic = getOrCreateTopic(tm, SI_ROOT + subredditId);

                associateSubreddit(tm, link, subredditTopic, thingTypes);

                final String subreddit = linkData.getString("subreddit");
                if(!extracted.contains(subreddit) && CRAWL_SETTINGS.get("linkSubreddit")) {

                    String subredditUrl = apiRoot + "r/" + subreddit + "/about.json";

                    ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                        @Override
                        public void run(HttpResponse<JsonNode> response){
                            try {
                                JSONObject respObject = response.getBody().getObject();
                                Topic subredditTopic = null;
                                if (respObject.has("kind") && respObject.getString("kind").equals(THING_TYPE_SUBREDDIT)) {
                                    extracted.add(subreddit);
                                    subredditTopic = parseSubreddit(respObject, thingTypes, tm);
                                }
                                if (subredditTopic != null) {
                                    associateSubreddit(tm, link, subredditTopic, thingTypes);
                                }
                            }
                            catch (JSONException | TopicMapException e) {
                                logParseCallbackRunError(e);
                            }
                        }
                        @Override
                        protected void error(Exception e, String body) {
                            logParseCallbackError(e, body);
                        }
                    };

                    requester.addRequest(Unirest.get(subredditUrl), callback);
                }
            }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        
        if(CRAWL_SETTINGS.get("linkComment")) {
            String commentUrl = apiRoot + "comments/" + linkData.getString("id");
            
            ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                @Override
                public void run(HttpResponse<JsonNode> response){
                    try {
                      JSONArray respArray = response.getBody().getArray();

                        // Object #0 is the link, which we skip here
                        parseThing(respArray.getJSONObject(1),tm,thingTypes);
                    } 
                    catch (Exception e) {
                        logParseCallbackRunError(e);
                    }
                }
                @Override
                protected void error(Exception e, String body) {
                    logParseCallbackError(e, body);
                }
            };
            
            System.out.println("Requesting " + commentUrl);
            requester.addRequest(Unirest.get(commentUrl), callback);
        }
        return linkTopic;
    }

    private Topic parseComment(JSONObject c, HashMap<String, Topic> thingTypes, TopicMap tm) throws JSONException, TopicMapException {
        if(forceStop()) {
            return null;
        }

        final JSONObject comment = c;
        
        JSONObject commentData = comment.getJSONObject("data");
        String kind = comment.getString("kind");
        String id = commentData.getString("name");
        
        hlog("Parsing comment " + id);
        
        Topic commentTopic = getOrCreateTopic(tm, SI_ROOT + id);
        commentTopic.setBaseName("Comment " + " (" + id + ")");
        commentTopic.addType(thingTypes.get(kind));

        addCommentOccurenceData(tm, commentData, commentTopic);

        if(commentData.has("author")) {
            String author = commentData.getString("author");

            Topic account = tm.getTopic(SI_ROOT + author);

            //Early return if we already scraped the url
            if (account != null) {
                associateAccount(tm, comment, account, thingTypes);
            }
            else if(!author.equals("[deleted]") && CRAWL_SETTINGS.get("commentUser")) {
                hlog("Parsing user " + author);
                String authorUrl = apiRoot + "user/" + author + "/about";
                
                ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                    @Override
                    public void run(HttpResponse<JsonNode> response) {
                        try {
                            JSONObject respObject = response.getBody().getObject();
                            if (respObject.has("kind") && respObject.getString("kind").equals(THING_TYPE_ACCOUNT)) {
                                Topic account = parseAccount(respObject, thingTypes, tm);
                                associateAccount(tm, comment, account, thingTypes);
                            }
                        } 
                        catch (JSONException | TopicMapException e) {
                            logParseCallbackRunError(e);
                        }
                    }
                    @Override
                    protected void error(Exception e, String body) {
                        logParseCallbackError(e, body);
                    }
                };
                
                requester.addRequest(Unirest.get(authorUrl), callback);
            }
        }

        if(commentData.has("parent_id")) {
            String parentId = commentData.getString("parent_id");
            Topic parentTopic = getOrCreateTopic(tm, SI_ROOT + parentId);
            associateParent(tm, commentTopic, parentTopic);
        }

        if(commentData.has("replies")) {
            JSONArray children;
            try {
                children = commentData
                        .getJSONObject("replies")
                        .getJSONObject("data")
                        .getJSONArray("children");

            } 
            catch(JSONException jse) {
                children = new JSONArray();
            }

            for (int i = 0; i < children.length(); i++) {
                JSONObject childData = children.getJSONObject(i);
                String childKind = childData.getString("kind");
                if (childKind.equals(THING_TYPE_MORE) && CRAWL_SETTINGS.get("more")) {
                    parseMore(commentData, childData, tm, thingTypes);
                } 
                else {
                    parseThing(childData, tm, thingTypes);
                }
            }
        }

        if(commentData.has("link_id")) {

            String link_id = commentData.getString("link_id");
            Topic linkTopic = tm.getTopic(SI_ROOT + link_id);

            if(linkTopic == null  && CRAWL_SETTINGS.get("commentLink")) {
                ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                    @Override
                    public void run(HttpResponse<JsonNode> response){
                        try {
                            JSONObject respObj = response.getBody().getObject();
                            parseThing(respObj, tm, thingTypes);
                        } 
                        catch (Exception e) {
                            logParseCallbackRunError(e);
                        }
                    }
                    @Override
                    protected void error(Exception e, String body) {
                        logParseCallbackError(e, body);
                    }
                };
                
                requester.addRequest(Unirest.get(apiRoot + "api/info?id=" + link_id), callback);
            }
        }
        return commentTopic;
    }

    
    private Topic parseAccount(JSONObject account, HashMap<String, Topic> thingTypes, TopicMap tm) throws JSONException, TopicMapException {
        if (forceStop()) {
            return null;
        }

        JSONObject accountData = account.getJSONObject("data");
        String kind = account.getString("kind");
        String id = accountData.getString("name");

        hlog("Parsing account " + id);
        
        Topic accountTopic = getOrCreateTopic(tm, SI_ROOT + id);
        accountTopic.setDisplayName("en", id);
        accountTopic.setBaseName(id);

        addAccountOccurenceData(tm, accountData, accountTopic);
        accountTopic.addType(thingTypes.get(kind));

        if(CRAWL_SETTINGS.get("userLink")) {
            ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                @Override
                public void run(HttpResponse<JsonNode> response) {
                    try {
                        JSONObject respObj = response.getBody().getObject();
                        parseThing(respObj, tm, thingTypes);
                    } 
                    catch(Exception e) {
                        logParseCallbackRunError(e);
                    }
                }
                @Override
                protected void error(Exception e, String body) {
                    logParseCallbackError(e, body);
                }
            };

            requester.addRequest(Unirest.get(apiRoot + "user/" + id + "/submitted.json"), callback);
        }

        if(CRAWL_SETTINGS.get("userComment")) {
            ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                @Override
                public void run(HttpResponse<JsonNode> response){
                    try {
                        JSONObject respObj = response.getBody().getObject();
                        parseThing(respObj, tm, thingTypes);
                    } 
                    catch(Exception e) {
                        logParseCallbackRunError(e);
                    }
                }
                @Override
                protected void error(Exception e, String body) {
                    logParseCallbackError(e, body);
                }
            };

            requester.addRequest(Unirest.get(apiRoot + "user/" + id + "/comments.json"), callback);
        }
        return accountTopic;
    }

    private Topic parseSubreddit(JSONObject subreddit, HashMap<String, Topic> thingTypes, TopicMap tm) throws JSONException, TopicMapException {
        if(forceStop()) {
            return null;
        }

        JSONObject subredditData = subreddit.getJSONObject("data");
        String kind = subreddit.getString("kind");
        String id = subredditData.getString("name");
        final String disp = subredditData.getString("display_name");

        hlog("Parsing subreddit " + disp);
        
        Topic subredditTopic = getOrCreateTopic(tm, SI_ROOT + id);
        subredditTopic.setDisplayName("en",disp );
        subredditTopic.setBaseName(disp + " (" + id + ")");
        
        addSubredditOccurrenceData(tm, subredditData, subredditTopic);
        subredditTopic.addType(thingTypes.get(kind));

        if(CRAWL_SETTINGS.get("subredditLink")) {
            ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>(tm, thingTypes) {
                @Override
                public void run(HttpResponse<JsonNode> response){
                    try {
                        JSONObject respObj = response.getBody().getObject();
                        parseThing(respObj, tm, thingTypes);

                        if(WandoraOptionPane.showConfirmDialog(getWandora(),
                                "Load more links?","Load more links?",
                                WandoraOptionPane.YES_NO_OPTION) == WandoraOptionPane.YES_OPTION) {

                            String after = respObj.getJSONObject("data").getString("after");
                            requester.addRequest(Unirest.get(apiRoot + "r/" + disp + "/hot.json?after=" + after), this);
                        }
                    } 
                    catch(Exception e) {
                        logParseCallbackRunError(e);
                    }
                }
                @Override
                protected void error(Exception e, String body) {
                    logParseCallbackError(e, body);
                }
            };

            requester.addRequest(Unirest.get(apiRoot + "r/" + disp + "/hot.json"), callback);
        }

        return subredditTopic;
    }
    
    
    
    
    /*
        ParseCallBack objects use this method to log exceptions in error methods.
    */
    protected void logParseCallbackError(Exception e, String body) {
        if(e != null) {
            e.printStackTrace();
            log(ERROR_MSG);
            log(e.getMessage());
            if(body != null) {
                log(ERROR_RESPONSE_MSG);
                log(body);
            }
        }
    }
    
    /*
        ParseCallBack objects use this method to log exceptions in run methods.
    */
    protected void logParseCallbackRunError(Exception e) {
        if(e != null) {
            e.printStackTrace();
            if(e instanceof JSONException) {
                log("Exception occurred while processing JSON data in Reddit extractor. Details follow.");
                log(e.getMessage());
            }
            else if(e instanceof TopicMapException) {
                log("Exception occurred while accessing topic map in Reddit extractor. Details follow.");
                log(e.getMessage());
            }
            else {
                log("Exception occurred while processing API response in Reddit extractor. Details follow.");
                log(e.getMessage());
            }
        }
    }
    
}
