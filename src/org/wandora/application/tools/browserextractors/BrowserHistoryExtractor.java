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
 */
package org.wandora.application.tools.browserextractors;

import java.io.File;
import java.net.URL;
import org.wandora.topicmap.*;
import org.wandora.application.Wandora;
import java.util.*;
import java.text.SimpleDateFormat;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;




/**
 *
 * @author olli
 */
public class BrowserHistoryExtractor extends AbstractExtractor implements BrowserPluginExtractor {


	private static final long serialVersionUID = 1L;
	
	
	public static final String historySI="http://wandora.org/si/browser-history";
    public static final String entrySI="http://wandora.org/si/browser-history/entry";
    public static final String dateSI="http://wandora.org/si/browser-history/date";
    public static final String pageSI="http://wandora.org/si/browser-history/page";




    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        String url=request.getSource();
        TopicMap tm=wandora.getTopicMap();
        Topic wc = TMBox.getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        Topic historyTopic = TMBox.getOrCreateTopic(tm, historySI, "Browser history");
        ExtractHelper.makeSubclassOf(historyTopic, wc, tm);

        Topic entryTopic=TMBox.getOrCreateTopic(tm, entrySI, "Browser history entry");
        ExtractHelper.makeSubclassOf(entryTopic, historyTopic, tm);
        Topic dateTopic=TMBox.getOrCreateTopic(tm, dateSI, "Browser history date");
        ExtractHelper.makeSubclassOf(dateTopic, historyTopic, tm);
        Topic pageTopic=TMBox.getOrCreateTopic(tm, pageSI, "Browser history page");
        ExtractHelper.makeSubclassOf(pageTopic, historyTopic, tm);
        Topic p=tm.getTopicBySubjectLocator(url);
        if(p==null) {
            p=tm.createTopic();
            p.setSubjectLocator(tm.createLocator(url));
            p.addSubjectIdentifier(tm.createLocator(url));
            p.addType(pageTopic);
            p.setBaseName("Page: "+url);
        }
        Topic t=tm.createTopic();
        t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
        t.addType(entryTopic);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date=sdf.format(new GregorianCalendar().getTime());
        Topic langIndep=TMBox.getOrCreateTopic(tm, XTMPSI.getLang(null));
        t.setData(dateTopic, langIndep, date);
        t.setBaseName("Browser history "+date);
        Association a=tm.createAssociation(pageTopic);
        a.addPlayer(t, entryTopic);
        a.addPlayer(p, pageTopic);
        wandora.doRefresh();
        return null;
    }



    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        if(request.isApplication(BrowserExtractRequest.APP_FIREFOX))
            return true;
        else
            return false;
    }



    @Override
    public String getBrowserExtractorName() {
        return "Collect browse history";
    }

    @Override
    public String getName() {
        return "Collect browse history";
    }

    @Override
    public String getDescription() {
        return "Creates a topic for browsed page. Created topic is time stamped. Extractor is a browser extractor and can't be used as a regular extractor.";
    }



    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }

}
