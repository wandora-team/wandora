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
 * CopyAssociations.java
 *
 * Created on 6. tammikuuta 2005, 16:23
 */

package org.wandora.application.tools.associations;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.ClipboardBox;


/**
 * <p>
 * Tool is used to generate textual representations of given associations. Tool
 * puts the generated association representation to system clipboard.
 * </p>
 * <p>
 * Tool is capable to generate plain text and HTML table representation. Default
 * format is plain text.
 * </p>
 * 
 * @author  akivela
 */
public class CopyAssociations extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public static final int WANDORA_LAYOUT = 5000;
    public static final int LTM_LAYOUT = 5010;
    
    public static final int TABTEXT_OUTPUT = 2000;
    public static final int HTML_OUTPUT = 2010;
    
    
    private int outputFormat = 0;
    private int layout = WANDORA_LAYOUT;

    
    public CopyAssociations(Wandora wandora, Context context)  throws TopicMapException {
        this(wandora, context, TABTEXT_OUTPUT);
    }
    
    public CopyAssociations(Wandora wandora, Context context, int outputFormat)  throws TopicMapException {
        this.outputFormat = outputFormat;
        setContext(context);
    }
    
    public CopyAssociations(Wandora wandora, Context context, int outputFormat, int outputLayout)  throws TopicMapException {
        this.outputFormat = outputFormat;
        this.layout = outputLayout;
        setContext(context);
    }
    
    
    
    public CopyAssociations(Context context) {
        this(context, TABTEXT_OUTPUT);
    }
    
    
    public CopyAssociations(Context context, int outputFormat) {
        setContext(context);
        this.outputFormat = outputFormat;
    }
    
    
    public CopyAssociations(Context context, int outputFormat, int outputLayout) {
        setContext(context);
        this.outputFormat = outputFormat;
        this.layout = outputLayout;
    }
    
    
    public CopyAssociations(int outputFormat) {
        this(new AssociationContext(), outputFormat);
    }
    
    
    public CopyAssociations(int outputFormat, int outputLayout) {
        this(new AssociationContext(), outputFormat, outputLayout);
    }
    
    
    
    public CopyAssociations() {
        this(new AssociationContext(), TABTEXT_OUTPUT);
    }
    
    
    // -------------------------------------------------------------------------
    

    public void setOutputFormat(int outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        String associationText = makeString(wandora);
        if(associationText != null && associationText.length() > 0) {
            ClipboardBox.setClipboard(associationText);
        }
    }
    
    
    
    
    public String makeString(Wandora wandora)  throws TopicMapException {
        StringBuilder sb = new StringBuilder("");

        Map<Topic,List<Map<Topic,Topic>>> associationsByType = new LinkedHashMap<>();
        Map<Topic,Set<Topic>> rolesByType = new LinkedHashMap<>();

        Iterator<Association> associations = null;
        Iterator context = getContext().getContextObjects();
        Association a = null;
        Topic t = null;
        Object aort = null;
        int count = 0;

        if(context != null) {
            setDefaultLogger();
            log("Copying associations...");
            while(context.hasNext()) {
                aort = context.next();
                associations = null;
                if(aort instanceof Topic) {
                    t = (Topic) aort;
                    associations = t.getAssociations().iterator();
                }
                else if(aort instanceof Association) {
                    a = (Association) aort;
                    List<Association> as = new ArrayList<>();
                    as.add(a);
                    associations = as.iterator();
                }
                if(associations == null) continue;
                
                // Ok, at this point we should have a valid association iterator.
                while(associations.hasNext()) {
                    count++;
                    setProgress(count);
                    a = (Association) associations.next();
                    Topic type = a.getType();
                    hlog("Copying association of type '" + getNameFor(type) + "'.");
                    List<Map<Topic, Topic>> typedAssociations = associationsByType.get(type);
                    if(typedAssociations == null) {
                    	typedAssociations = new ArrayList<>();
                    }

                    Collection<Topic> aRoles = a.getRoles();
                    Set<Topic> roles = rolesByType.get(type);
                    if(roles == null) roles = new LinkedHashSet<>();
                    Map<Topic,Topic> association = new LinkedHashMap<>();

                    for(Iterator<Topic> aRoleIter = aRoles.iterator(); aRoleIter.hasNext(); ) {
                        Topic role = (Topic) aRoleIter.next();
                        Topic player = a.getPlayer(role);
                        association.put(role, player);
                        roles.add(role);
                    }
                    typedAssociations.add(association);
                    rolesByType.put(type, roles);
                    associationsByType.put(type, typedAssociations);
                }
            }

            if(count != 0) {
                log("Formatting output...");
                boolean HTMLOutput = (outputFormat == HTML_OUTPUT);
                // ----- Transform created data structure to text&html! -----
                
                if(layout == WANDORA_LAYOUT) {
                    for(Topic associationType : associationsByType.keySet()) {
                        sb.append(getNameFor(associationType)).append("\n");
                        if(HTMLOutput) sb.append("<br>\n<table>\n<tr>");
                        for(Topic role : rolesByType.get(associationType)) {
                            if(HTMLOutput) sb.append("<td>");
                            sb.append(getNameFor(role));
                            if(HTMLOutput) sb.append("</td>");
                            else sb.append("\t");
                        }
                        sb.deleteCharAt(sb.length()-1); // remove last tabulator
                        
                        if(HTMLOutput) sb.append("</tr>");
                        sb.append("\n");
                        for(Map<Topic,Topic> association : associationsByType.get(associationType)) {
                            if(HTMLOutput) sb.append("<tr>");
                            for(Topic role : rolesByType.get(associationType)) {
                                if(HTMLOutput) sb.append("<td>");
                                Topic player = association.get(role);
                                sb.append( getNameFor(player) );
                                if(HTMLOutput) sb.append("</td>");
                                else sb.append("\t");
                            }
                            sb.deleteCharAt(sb.length()-1); // remove last tabulator
                            if(HTMLOutput) sb.append("</tr>");
                            sb.append("\n");
                        }
                        if(HTMLOutput) sb.append("</table>");
                        sb.append("\n");
                    }
                }
                
                else if(layout == LTM_LAYOUT) {
                    for(Topic associationType : associationsByType.keySet()) {
                        if(HTMLOutput) sb.append("<br>\n<table>\n");
                        for(Map<Topic,Topic> association : associationsByType.get(associationType)) {
                            if(HTMLOutput) sb.append("<tr>");
                            if(HTMLOutput) sb.append("<td>");
                            sb.append(getNameFor(associationType));
                            if(HTMLOutput) sb.append("</td>");
                            else sb.append("\t");
                            for(Topic role : rolesByType.get(associationType)) {
                                Topic player = association.get(role);
                                if(player != null) {
                                    if(HTMLOutput) sb.append("<td>");
                                    sb.append( getNameFor(player) );
                                    if(HTMLOutput) sb.append("</td>");
                                    else sb.append("\t");
                                    
                                    if(HTMLOutput) sb.append("<td>");
                                    sb.append( getNameFor(role) );
                                    if(HTMLOutput) sb.append("</td>");
                                    else sb.append("\t");
                                }
                            }
                            sb.deleteCharAt(sb.length()-1); // remove last tabulator
                            if(HTMLOutput) sb.append("</tr>");
                            sb.append("\n");
                        }
                        if(HTMLOutput) sb.append("</table>");
                        sb.append("\n");
                    }
                }
                log("Total "+count+" associations copied.");
                log("Total "+associationsByType.size()+" different association types found.");
            }
            else {
                log("No associations found.");
            }
            log("Ready.");
            setState(WAIT);
        }
        return(sb.toString());
    }

    
    

    
    
    public String getNameFor(Topic t)  throws TopicMapException {
        if(t != null) {
            return TopicToString.toString(t);
        }
        return "";
    }
    
    
    @Override
    public String getName() {
        return "Copy associations";
    }

    @Override
    public String getDescription() {
        return "Copy selected associations to clipboard.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
}
