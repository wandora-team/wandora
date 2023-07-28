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
 *
 * SubjectLocatorChecker.java
 *
 * Created on 2009-11-20
 */

package org.wandora.application.tools.occurrences;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Performs a test to given occurrences. Test checks if the occurrence is an URL,
 * if the URL is a valid URL, and if the resource addressed by URL occurrences exists.
 *
 * @author akivela
 */
public class URLOccurrenceChecker extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	String urlExists = "EXISTS\t'<locator/>'";
    String urlDoesntExists = "MISSING\t'<locator/>'";
    String noURLOccurrences = "NOT URL\t'<topic/>'.";
    String illegalURLOccurrence = "INVALID URL\t'<topic/>'.";
    String topicError = "TOPIC ERROR\t'<topic/>'.";


    /**
     * <code>reportType</code> contains character set defining tool
     * generated reports. Possible report characters are:
     *
     * m = report missing url occurrences
     * s = report existing url occurrences (successful)
     * i = report invalid url occurrences (exception occurred)
     * n = report when topic has no url occurrences
     * e = report topic errors (topic == null || exceptions)
     */
    String reportType = "sm";

    File currentDirectory = null;
    Iterator<Topic> topicsToCheck;


    public URLOccurrenceChecker() {}
    public URLOccurrenceChecker(Context context) {
        setContext(context);
    }
    public URLOccurrenceChecker(Collection<Topic> topics) {
        topicsToCheck = topics.iterator();
    }
    public URLOccurrenceChecker(Iterator<Topic> topics) {
        topicsToCheck = topics;
    }



    @Override
    public void execute(Wandora wandora, Context context) {
        setDefaultLogger();

        if(topicsToCheck == null) {
            topicsToCheck = context.getContextObjects();
        }
        if(topicsToCheck != null) {
            try {
                setLogTitle("Check URL occurrences of topics...");
                Topic topic = null;
                int validURLOccurrences = 0;
                int invalidURLOccurrences = 0;
                int totalOccurrences = 0;
                int totalURLOccurrences = 0;
                int topicsChecked = 0;
                CheckResult result = null;

                while(topicsToCheck.hasNext() && !forceStop()) {
                    try {
                        topicsChecked++;
                        topic = (Topic) topicsToCheck.next();
                        if(topic != null && !topic.isRemoved()) {
                            result = checkURLOccurrences(topic);
                            validURLOccurrences += result.validURLOccurrences;
                            invalidURLOccurrences += result.invalidURLOccurrences;
                            totalURLOccurrences += result.totalURLOccurrences;
                            totalOccurrences += result.totalOccurrences;
                        }
                    }
                    catch (Exception e) {
                        log(e);
                    }
                    setLogTitle("Check URL occurrences in topics... " + topicsChecked);
                }
                log(topicsChecked + " topics checked.");
                log(totalOccurrences + " occurrences checked.");
                log(totalURLOccurrences + " URL occurrences checked.");

                log(validURLOccurrences + " valid and existing url occurrences found.");
                log(invalidURLOccurrences + " invalid or missing url occurrences found.");
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
            topicsToCheck = null;
        }
    }




    @Override
    public String getName() {
        return "Check URL occurrences";
    }


    @Override
    public String getDescription() {
        return "Check if URL occurrences of given topics really resolve existing resource.";
    }





    public CheckResult checkURLOccurrences(Topic t)  throws TopicMapException {
        int valid = 0;
        int invalid = 0;
        int totalOccurrences = 0;
        int totalUrls = 0;
        if(t != null) {
            Collection<Topic> occurrenceTypes = t.getDataTypes();
            for(Topic occurrenceType : occurrenceTypes) {
                Hashtable<Topic, String> occurrences = t.getData(occurrenceType);
                for(String occurrence : occurrences.values()) {
                    totalOccurrences++;
                    URL urlOccurrence = getURLOccurrence(occurrence);
                    if(urlOccurrence != null) {
                        totalUrls++;
                        try {
                            if(IObox.urlExists(urlOccurrence)) {
                                if(reportAbout('s')) log(urlExists.replaceAll("<locator/>", urlOccurrence.toExternalForm()));
                                valid++;
                            }
                            else {
                                if(reportAbout('m')) log(urlDoesntExists.replaceAll("<locator/>", urlOccurrence.toExternalForm()));
                                invalid++;
                            }
                        }
                        catch (Exception e) {
                            if(reportAbout('i')) {
                                log(illegalURLOccurrence.replaceAll("<topic/>", t.getBaseName()));
                                log("\t"+e.toString());
                                invalid++;
                            }
                        }
                    }
                }
            }
            if(valid == 0) {
                if(reportAbout('i')) { log(noURLOccurrences.replace("<topic/>", t.getBaseName())); }
            }
        }
        else {
            if(reportAbout('e')) { log(topicError.replaceAll("<topic/>", "TOPIC IS NULL")); }
        }
        
        return new CheckResult(valid, invalid, totalUrls, totalOccurrences);
    }




    private boolean reportAbout(char reportCode) {
        return ( reportType.indexOf(reportCode) != -1 );
    }


    private URL getURLOccurrence(String urlString) {
        if(urlString != null) {
            urlString = urlString.trim();
            if(urlString.startsWith("http://") || urlString.startsWith("https://") || urlString.startsWith("file://") || urlString.startsWith("ftp://") || urlString.startsWith("ftps://")) {
                try {
                    URL url = new URL(urlString);
                    return url;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println("NOT A URL: '"+urlString+"'");
        return null;
    }




    // -------------------------------------------------------------------------

    

    public class CheckResult {
        int validURLOccurrences = 0;
        int invalidURLOccurrences = 0;
        int totalOccurrences = 0;
        int totalURLOccurrences = 0;

        public CheckResult(int v, int i, int t, int to) {
            validURLOccurrences = v;
            invalidURLOccurrences = i;
            totalURLOccurrences = t;
            totalOccurrences = to;
        }
    }

}
