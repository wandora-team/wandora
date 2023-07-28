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
 * SIFixer.java
 *
 * Created on 6. tammikuuta 2005, 13:38
 */

package org.wandora.application.tools.subjects;

import java.net.URLEncoder;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import java.util.*;
import org.wandora.utils.DataURL;



/**
 * <code>FixSubjectIdentifiers2</code> iterates through all subject identifiers in context
 * topics and replaces all invalid characters in SI's with underline
 * character (_). Invalid characters in subject identifiers may cause
 * problems if subject identifiers are used to aquire topics within
 * web application.
 *
 * Subject identifier changes may cause topic merges.
 *
 * @author  akivela
 */
public class FixSubjectIdentifiers2 extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	boolean quiet = false;

    public FixSubjectIdentifiers2() {
    }
    public FixSubjectIdentifiers2(Context context) {
        setContext(context);
    }


    @Override
    public String getName() {
        return "Subject identifier fixer v2";
    }

    @Override
    public String getDescription() {
        return "Encodes all illegal charatcers in the subject locator URL. "+
               "Leaves data URLs untouched.";
    }

    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Iterator<Topic> contextTopics = context.getContextObjects();
        TopicMap tm = admin.getTopicMap();
        if(contextTopics != null && contextTopics.hasNext()) {
            if(WandoraOptionPane.showConfirmDialog(admin, "Are you sure you want to fix subject identifiers?","Fix subject identifiers?", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                setDefaultLogger();
                setLogTitle("Fixing subject identifiers");
                Topic topic = null;
                Collection<Locator> subjectIdentifiers = null;
                int progress = 0;
                int fixCount = 0;
                int checkCount = 0;
                int dataURLCounter = 0;
                int addCounter = 0;

                while(contextTopics.hasNext() && !forceStop()) {
                    try {
                        topic = contextTopics.next();
                        if(topic != null  && !topic.isRemoved()) {
                            setProgress(progress++);
                            subjectIdentifiers = topic.getSubjectIdentifiers();
                            if(subjectIdentifiers.isEmpty()) {
                                Locator defaultSubjectIdentifier = TopicTools.createDefaultLocator();
                                log("Topic has no subject identifiers at all. Adding subject identifier " + defaultSubjectIdentifier.toExternalForm());
                                topic.addSubjectIdentifier(defaultSubjectIdentifier);
                                addCounter++;
                            }
                            else {
                                subjectIdentifiers = new ArrayList<>(subjectIdentifiers);
                                for(Locator subjectIdentifier : subjectIdentifiers) {
                                    if(subjectIdentifier != null) {
                                        checkCount++;
                                        hlog("Investigating subject identifier \n"+getPrintable(subjectIdentifier.toExternalForm()));
                                        try {
                                            String si = subjectIdentifier.toExternalForm();
                                            if(DataURL.isDataURL(si)) {
                                                dataURLCounter++;
                                                continue;
                                            }
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
                                                        topic.removeSubjectIdentifier(subjectIdentifier);
                                                    }
                                                    catch(Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    log("Fixed subject identifier " + encodedsistr);
                                                    fixCount++;
                                                }
                                            }
                                        }
                                        catch(Exception e) {
                                            log(e);
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
                log("Checked "+checkCount+" subject identifiers.");
                log("Fixed "+fixCount+" subject identifiers.");
                if(addCounter == 1) log("Added "+addCounter+" missing subject identifier.");
                if(addCounter > 1) log("Added "+addCounter+" missing subject identifiers.");
                if(dataURLCounter == 1) log("Found "+dataURLCounter+" data URL subject identifier.");
                if(dataURLCounter > 1) log("Found "+dataURLCounter+" data URL subject identifiers.");
                log("Ready.");
                setState(WAIT);
            }
        }
    }


    private static final String chars = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_-!.~'()*";

    protected String URLEncode(String str) {
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


    /**
     * Used to trim long subject identifiers shorter for prettier printing. 
     */
    private String getPrintable(String str) {
        if(str.length() > 128) {
            return str.substring(0, 127) + "...";
        }
        return str;
    }
}
