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
 */


package org.wandora.application.tools.occurrences.clipboards;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.OccurrenceTable;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 * Searches for URL occurrences that address PasteBin resources. If
 * URL occurrence addresses PasteBin resource, 
 * downloads the resource and replaces the occurrence URL with
 * it.
 * 
 * @author akivela
 */


public class PasteBinOccurrenceDownloader extends AbstractWandoraTool implements WandoraTool {
    
    private static final String URL_PREFIX = "http://pastebin.com/";
    private static final String RAW_PREFIX = "http://pastebin.com/raw.php?i=";
    private boolean cancelled = false;
    
    
    
    public PasteBinOccurrenceDownloader() {
    }
    
    
    public PasteBinOccurrenceDownloader(Context proposedContext) {
        this.setContext(proposedContext);
    }
    
    
    
    @Override
    public String getName() {
        return "Download occurrence from Pastebin";
    }

    @Override
    public String getDescription() {
        return "Downloads occurrence from Pastebin.";
    }

    @Override
    public void execute(Wandora w, Context context) {
        Object source = context.getContextSource();
        cancelled = false;
        String o = null;
        Topic carrier = null;
        Topic type = null;
        Topic lang = null;
        if(source instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) source;
            o = ot.getPointedOccurrence();
            String pid = getPastebinId(o);
            if(pid != null) {
                String purl = RAW_PREFIX + pid;
                type = ot.getPointedOccurrenceType();
                lang = ot.getPointedOccurrenceLang();
                carrier = ot.getTopic();
                restorePastebinData(carrier, type, lang, purl, w);
            }
            else {
                WandoraOptionPane.showMessageDialog(w, "It appears given occurrence is not a Pastebin URL. Cancelling download.", "Cancelling download", WandoraOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            Iterator contextObjects = context.getContextObjects();
            setDefaultLogger();
            int counter = 0;
            while(contextObjects.hasNext() && !forceStop() && !cancelled) {
                Object co = contextObjects.next();
                if(co instanceof Topic) {
                    carrier = (Topic) co;
                    try {
                        Collection<Topic> types = carrier.getDataTypes();
                        for(Topic otype : types) {
                            if(forceStop() || cancelled) break;
                            type = otype;
                            Hashtable<Topic, String> occurrences = carrier.getData(type);
                            for(Iterator scopes = occurrences.keySet().iterator(); scopes.hasNext(); ) {
                                Topic scope = (Topic) scopes.next();
                                if(scope != null) {
                                    o = occurrences.get(scope);
                                    String pid = getPastebinId(o);
                                    if(pid != null) {
                                        String purl = RAW_PREFIX + pid;
                                        boolean restored = restorePastebinData(carrier, type, scope, purl, w);
                                        if(restored) counter++;
                                        if(cancelled) break;
                                    }
                                    else {
                                        // DO NOTHING
                                    }
                                }
                            }
                        }
                    }
                    catch(TopicMapException ex) {
                        log(ex);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            if(counter > 0) {
                log("Total "+counter+" Pastebin stored texts restored.");
            }
            else {
                log("Found no Pastebin URLs. No Pastebin data restored.");
            }
            log("Ok.");
            setState(WAIT);
        }
    }
    
    
    
    
    public boolean restorePastebinData(Topic carrier, Topic type, Topic scope, String purl, Wandora w) {
        try {
            String pastebinContent = getUrl(new URL(purl));
            boolean storeToOccurrence = true;
            if(pastebinContent == null || pastebinContent.length() == 0) {
                storeToOccurrence = false;
                int a = WandoraOptionPane.showConfirmDialog(w, "Downloaded data is either null or zero length. Would you like to replace the occurrence with downloaded data anyway", "Downloaded data is invalid", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                if(a == WandoraOptionPane.CANCEL_OPTION) cancelled = true;
                if(a == WandoraOptionPane.YES_OPTION) storeToOccurrence = true;
            }
            if(storeToOccurrence) {
                carrier.setData(type, scope, pastebinContent);
                return true;
            }
        }
        catch(Exception e) {
            log("Exception '"+e.getMessage()+"' occurred while downloading URL '"+purl+"'.");
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    public static String getPastebinId(String u) {
        String pid = null;
        if(u != null) {
            u = u.trim();
            if(u.startsWith(RAW_PREFIX)) {
                pid = u.substring(RAW_PREFIX.length());
            }
            else if(u.startsWith(URL_PREFIX)) {
                pid = u.substring(URL_PREFIX.length());
            }
        }
        return pid;
    }
    
    
    
    
    public static String getUrl(URL url) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        if(url != null) {
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setUseCaches(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
     
            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
        }
        return sb.toString();
    }
    
}
