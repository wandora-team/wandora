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
package org.wandora.application.tools.extractors.nyt;

import java.io.File;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.HTMLEntitiesCoder;
import org.wandora.utils.IObox;


/**
 *
 * @author akivela
 * @author Eero Lehtonen
 */
public class NYTArticleSearchExtractor extends AbstractNYTExtractor {

	private static final long serialVersionUID = 1L;
	
	private static String defaultLang = "en";
    private static String currentURL = null;

    @Override
    public String getName() {
        return "New York Times Article Search API extractor";
    }

    @Override
    public String getDescription() {
        return "Extractor performs an article search using The New York Times API and "
                + "transforms results to topics and associations.";
    }

    // -------------------------------------------------------------------------
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        currentURL = null;
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        currentURL = u.toExternalForm();

        log("Article search extraction with " + currentURL);

        String in = IObox.doUrl(u);

        System.out.println("New York Times API returned-------------------------\n" + in
                + "\n----------------------------------------------------");

        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        parse(json, tm);
        return true;
    }

    // -------------------------------------------------------------------------
    public void parse(JSONObject json, TopicMap tm) throws TopicMapException {
        if (json.has("response")) {
            try {
                json = json.getJSONObject("response");
                if (json.has("docs")) {
                    JSONArray resultsArray = json.getJSONArray("docs");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject result = resultsArray.getJSONObject(i);
                        try {
                            parseResult(result, tm);
                        } catch (JSONException | TopicMapException e) {
                            e.printStackTrace();
                            log(e);
                        }
                    }

                }
                handlePagination(json, tm);
            } catch (JSONException e) {
                log(e);
            }
        }
    }
    private boolean shouldHandlePagination = true;
    private String defaultPagingOption = null;

    private void handlePagination(JSONObject json, TopicMap tm) {
        if (!shouldHandlePagination || forceStop()) {
            return;
        }
        try {
            JSONObject meta = json.getJSONObject("meta");
            int page = 0;
            if (meta.has("offset")) {
                page = meta.getInt("offset");
            }
            int total = meta.getInt("hits");
            int totalPages = (total / 10) + (total % 10 == 0 ? 0 : 1);
            if (page >= totalPages || currentURL == null) return;
            
            String[] pagingOptions = new String[]{
                "Do not extract any more pages",
                "Extract only next page",
                "Extract next page",
                "Extract 10 next pages",
                "Extract all next pages"
            };
            String message = "You have just extracted page " + (page + 1) 
                    + ". There is total " + totalPages + " pages available. "
                    + "What would you like to do? Remember New York Times "
                    + "APIs limit daily requests. Extracting one page takes "
                    + "one request.";
            if (defaultPagingOption == null) {
                defaultPagingOption = pagingOptions[0];
            }
            String a = WandoraOptionPane.showOptionDialog(
                    Wandora.getWandora(),
                    message,
                    "Found more pages",
                    WandoraOptionPane.OK_CANCEL_OPTION,
                    pagingOptions,
                    defaultPagingOption);

            defaultPagingOption = a;
            if (a == null) return;
            
            String originalURL = currentURL;
            
            if (pagingOptions[1].equals(a)) {
                System.out.println("Selected to extract only next page");
                String newURL = originalURL.replace("page=" + page, "page=" + (page + 1));
                shouldHandlePagination = false;
                _extractTopicsFrom(new URL(newURL), tm);
            } else if (pagingOptions[2].equals(a)) {
                System.out.println("Selected to extract next page");
                String newURL = originalURL.replace("page=" + page, "page=" + (page + 1));
                _extractTopicsFrom(new URL(newURL), tm);
            } else if (pagingOptions[3].equals(a)) {
                System.out.println("Selected to extract 10 next pages");
                shouldHandlePagination = false;
                setProgress(1);
                setProgressMax(10);
                int progress = 1;
                for (int p = page + 1; p <= Math.min(page + 10, totalPages) && !forceStop(); p++) {
                    String newURL = originalURL.replace("page=" + page, "page=" + p);
                    if (p == page + 10) {
                        shouldHandlePagination = true;
                    }
                    _extractTopicsFrom(new URL(newURL), tm);
                    setProgress(progress++);
                    nap();
                }
            } else if (pagingOptions[4].equals(a)) {
                System.out.println("Selected to extract all pages");
                shouldHandlePagination = false;
                setProgress(1);
                setProgressMax((int) (total - page));
                int progress = 1;
                for (int p = page + 1; p <= totalPages && !forceStop(); p++) {
                    String newURL = originalURL.replace("page=" + page, "page=" + p);
                    _extractTopicsFrom(new URL(newURL), tm);
                    setProgress(progress++);
                    nap();
                }
                shouldHandlePagination = true;
            }
            
        } catch (Exception ex) {
            log(ex);
        }
    }

    private void nap() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            // WAKE UP
        }
    }

    public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {

        String url = result.getString("web_url"); // All results should contain an url at least.

        Topic articleTopic = tm.createTopic();
        articleTopic.addSubjectIdentifier(new Locator(url));
        articleTopic.addType(getArticleTypeTopic(tm));

        if (result.has("body") && !result.isNull("body")) {
            String body = result.getString("body");
            if (body != null && body.length() > 0) {
                body = HTMLEntitiesCoder.decode(body);
                Topic bodyTypeTopic = getBodyTypeTopic(tm);
                Topic langTopic = getLangTopic(tm);
                articleTopic.setData(bodyTypeTopic, langTopic, body);
            }
        }
        
        if (result.has("abstract") && !result.isNull("abstract")) {
            String abst = result.getString("abstract");
            if (abst != null && abst.length() > 0) {
                abst = HTMLEntitiesCoder.decode(abst);
                Topic abstTypeTopic = getAbstractTypeTopic(tm);
                Topic langTopic = getLangTopic(tm);
                articleTopic.setData(abstTypeTopic, langTopic, abst);
            }
        }
        
        if (result.has("text") && !result.isNull("text")) {
            String text = result.getString("text");
            if (text != null && text.length() > 0) {
                text = HTMLEntitiesCoder.decode(text);
                Topic textTypeTopic = getTextTypeTopic(tm);
                Topic langTopic = getLangTopic(tm);
                articleTopic.setData(textTypeTopic, langTopic, text);
            }
        }
        
        if (result.has("lead_paragraph") && !result.isNull("lead_paragraph")) {
            String lead_paragraph = result.getString("lead_paragraph");
            if (lead_paragraph != null && lead_paragraph.length() > 0) {
                lead_paragraph = HTMLEntitiesCoder.decode(lead_paragraph);
                Topic leadParagraphTypeTopic = getLeadParagraphTypeTopic(tm);
                Topic langTopic = getLangTopic(tm);
                articleTopic.setData(leadParagraphTypeTopic, langTopic, lead_paragraph);
            }
        }
        
        if (result.has("pub_date") && !result.isNull("pub_date")) {
            String date = result.getString("pub_date");
            if (date != null && date.length() > 0) {
                Topic dateTypeTopic = getDateTypeTopic(tm);
                Topic langTopic = getLangTopic(tm);
                articleTopic.setData(dateTypeTopic, langTopic, date);
            }
        }
        
        if (result.has("headline") && !result.isNull("headline")) {
            JSONObject headline = result.getJSONObject("headline");
            if(headline.has("main")){
                String title = headline.getString("main");
                if (title != null && title.length() > 0) {
                    title = HTMLEntitiesCoder.decode(title);
                    articleTopic.setDisplayName(defaultLang, title);
                    articleTopic.setBaseName(title + " (NYT article)");
                }
            }
        }

        if (result.has("keywords") && !result.isNull("keywords")) {
            JSONArray keywords = result.getJSONArray("keywords");
            for (int i = 0; i < keywords.length(); i++) {
                try {
                    JSONObject keyword = keywords.getJSONObject(i);
                    String name = keyword.getString("name");
                    name = HTMLEntitiesCoder.decode(name);
                    String value = keyword.getString("value");
                    value = HTMLEntitiesCoder.decode(value);
                    parseKeyword(name, value, articleTopic, tm);

                } catch (JSONException | TopicMapException e) {
                    log(e);
                }

            }

        }

        if (result.has("dbpedia_resource_url") && !result.isNull("dbpedia_resource_url")) {
            JSONArray resources = result.getJSONArray("dbpedia_resource_url");
            for (int i = 0; i < resources.length(); i++) {
                String res = resources.getString(i);
                if (res == null) continue;
                res = res.trim();
                if( res.length() == 0) continue;
                
                Topic resourceTopic = getDBpediaResourceTopic(res, tm);
                Topic articleTypeTopic = getArticleTypeTopic(tm);
                Topic resourceTypeTopic = getDBpediaResourceTypeTopic(tm);

                if (resourceTopic != null && resourceTypeTopic != null && 
                        articleTypeTopic != null) {
                    Association a = tm.createAssociation(resourceTypeTopic);
                    a.addPlayer(resourceTopic, resourceTypeTopic);
                    a.addPlayer(articleTopic, articleTypeTopic);
                }
            }
        }

        if (result.has("byline") && !result.isNull("byline")) {
            try {
                JSONObject byline = result.getJSONObject("byline");
                if (byline.has("original")){
                    String bylineString = byline.getString("original");
                    if (bylineString != null && bylineString.length() > 0) {
                        bylineString = HTMLEntitiesCoder.decode(bylineString);
                        Topic bylineTopic = getBylineTopic(bylineString, tm);
                        Topic bylineTypeTopic = getBylineTypeTopic(tm);
                        Topic articleTypeTopic = getArticleTypeTopic(tm);

                        Association a = tm.createAssociation(bylineTypeTopic);
                        a.addPlayer(bylineTopic, bylineTypeTopic);
                        a.addPlayer(articleTopic, articleTypeTopic);
                    }
                }
            } catch (JSONException e) {
                // The API might represent an empty byline as an empty array..?
            }

        }
    }

    private void parseKeyword(String name, String value, Topic articleTopic, TopicMap tm) throws TopicMapException {
        
        Topic keywordTypeTopic = getKeywordNameTopic(name, tm);
        Topic keywordTopic = getKeywordTopic(value, tm);
        Topic articleTypeTopic = getArticleTypeTopic(tm);
        
        Association a = tm.createAssociation(keywordTypeTopic);
        a.addPlayer(keywordTopic, keywordTypeTopic);
        a.addPlayer(articleTopic, articleTypeTopic);
        
    }
}
