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
 * SIFixer.java
 *
 * Created on 6. tammikuuta 2005, 13:38
 */

package org.wandora.application.tools.subjects;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import static org.wandora.application.gui.ConfirmResult.*;
import org.wandora.*;

import java.util.*;



/**
 * <code>SIFixer2</code> iterates through all subject identifiers in context
 * topics and replaces all invalid characters in SI's with underline
 * character (_). Invalid characters in subject identifiers may cause
 * problems if subject identifiers are used to aquire topics within
 * web application.
 *
 * Subject identifier changes may cause topic merges.
 *
 * @author  akivela
 */
public class SIFixer2 extends AbstractWandoraTool implements WandoraTool {

    boolean quiet = false;

    public SIFixer2() {
    }
    public SIFixer2(Context context) {
        setContext(context);
    }


    @Override
    public String getName() {
        return "SI Fixer v2";
    }

    @Override
    public String getDescription() {
        return "Fixes all subject identifiers of context topics. "+
               "Fix includes illegal character removal.";
    }

    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Iterator contextTopics = context.getContextObjects();
        TopicMap tm = admin.getTopicMap();
        if(contextTopics != null && contextTopics.hasNext()) {
            if(WandoraOptionPane.showConfirmDialog(admin, "Are you sure you want to fix subject identifiers?","Confirm SI fix", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                setDefaultLogger();
                setLogTitle("Fixing SIs");
                Topic topic = null;
                Locator nl = null;
                Locator l = null;
                Collection sis = null;
                ConfirmResult result = yes;
                int progress = 0;
                int fixed = 0;
                int checked = 0;

                while(contextTopics.hasNext() && !forceStop(result)) {
                    try {
                        topic = (Topic) contextTopics.next();
                        if(topic != null  && !topic.isRemoved()) {
                            setProgress(progress++);
                            sis = topic.getSubjectIdentifiers();
                            if(sis.isEmpty()) {
                                log("Adding SI " + nl.toExternalForm());
                                topic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                            }
                            else {
                                for(Iterator siIterator = sis.iterator(); siIterator.hasNext(); ) {
                                    l = (Locator) siIterator.next();
                                    if(l != null) {
                                        checked++;
                                        hlog("Investigating SI\n"+l.toExternalForm());
                                        try {
                                            String si = l.toExternalForm();
                                            String osi = si;
                                            String reference = null;
                                            int referenceIndex = si.indexOf('#');
                                            if(referenceIndex > -1) {
                                                reference = si.substring(referenceIndex+1);
                                                si = si.substring(0, referenceIndex);
                                            }
                                            boolean endsWithSlash = si.endsWith("/");
                                            String[] siparts = si.split("/");
                                            if(siparts.length > 2) {
                                                StringBuilder encodedsi = new StringBuilder("");
                                                int i=0;
                                                for(; i<3 && i<siparts.length; i++) {
                                                    encodedsi.append(siparts[i]);
                                                    if(i<siparts.length-1) encodedsi.append("/");
                                                }
                                                for(; i<siparts.length; i++) {
                                                    encodedsi.append(URLEncode(siparts[i]));
                                                    if(i<siparts.length-1) encodedsi.append("/");
                                                }
                                                if(endsWithSlash) encodedsi.append("/");

                                                if(reference != null) {
                                                    encodedsi.append("#");
                                                    encodedsi.append(URLEncode(reference));
                                                }
                                                String encodedsistr = encodedsi.toString();
                                                if(!encodedsistr.equals(osi)) {
                                                    try {
                                                        topic.addSubjectIdentifier(new Locator(encodedsistr));
                                                    }
                                                    catch(Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        topic.removeSubjectIdentifier(l);
                                                    }
                                                    catch(Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    log("Fixed SI " + encodedsistr);
                                                    fixed++;
                                                }
                                            }
                                        }
                                        catch(Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Checked "+checked+" subject identifiers.");
                log("Fixed "+fixed+" subject identifiers that are not valid URIs.");
                setState(WAIT);
            }
        }
    }


    private static final String chars = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_-!.~'()*";

    private String URLEncode(String str) {
        try {
            if(str != null && str.length() > 0) {
                StringBuilder sb = new StringBuilder("");
                for(int i=0; i<str.length(); i++) {
                    int c = str.charAt(i);
                    if(chars.indexOf(c)>-1) {
                        sb.append((char) c);
                    }
                    else if(c == '%') {
                        sb.append((char) c);
                    }
                    else {
                        sb.append(URLEncoder.encode(""+(char) c, "utf-8"));
                    }
                }
                return sb.toString();
            }
        }
        catch(Exception e) {}
        return str;
    }


}
