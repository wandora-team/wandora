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
 * 
 * Search.java
 *
 * Created on August 30, 2004, 3:32 PM
 */

package org.wandora.application.tools;


import java.util.*;
import org.wandora.application.gui.search.SearchTopicsResults;
import org.wandora.application.gui.search.SearchTopicsDialog;
import de.topicmapslab.tmql4j.components.processor.results.model.ResultType;
import de.topicmapslab.tmql4j.components.processor.runtime.ITMQLRuntime;
import de.topicmapslab.tmql4j.components.processor.runtime.TMQLRuntimeFactory;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import javax.swing.*;
import org.wandora.application.gui.search.SearchTopicsFrame;


/**
 *
 * @author  olli, ak
 */
public class Search extends AbstractWandoraTool implements WandoraTool {
    
    public static boolean useFrameSearch = true;
    
    private static SearchTopicsDialog dialog = null;
    private static SearchTopicsFrame searchFrame = null;

    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(useFrameSearch) {
            if(searchFrame == null) {
                searchFrame = new SearchTopicsFrame();
            }
            if(searchFrame != null) {
                searchFrame.setVisible(true);
            }
            else {
                System.out.println("Unable in instantiate SearchTopicsFrame.");
            }
        }
        else {
            // This is the old search dialog of Wandora application.
            // It is here only for historical curiosity and code preservation.
            oldExecute(wandora, context);
        }
    }
    
    
    


    public void oldExecute(final Wandora wandora, Context context) {
        Object contextSource = context.getContextSource();
        TopicMap topicMap = null;
        if(contextSource != null && contextSource instanceof LayerStatusPanel) {
            Layer l = ((LayerStatusPanel) context).getLayer();
            if(l != null) topicMap = l.getTopicMap();
        }
        else {
            topicMap = wandora.getTopicMap();
        }
        if(topicMap != null) {
            boolean anotherSearch = false;
            do {
                anotherSearch = false;
                if(dialog == null) {
                    dialog = new SearchTopicsDialog(wandora, this, true);
                }
                dialog.setVisible(true);

                // Now user has closed the dialog
                if(dialog.wasAccepted()) {
                    int searchType = dialog.getSearchType();

                    if(searchType == SearchTopicsDialog.SEARCH) {
                        String query = dialog.getSearchQuery();
                        if(query==null || query.length() == 0) return;
                        try {
                            Collection<Topic> results = topicMap.search(query, dialog.getSearchOptions());
                            SearchTopicsResults resultDialog = new SearchTopicsResults(wandora, results);
                            resultDialog.setVisible(true);
                            anotherSearch = resultDialog.doSearchAgain();
                        }
                        catch(Exception e){
                            wandora.handleError(e);
                            return;
                        }
                    }

                    // ***** DO SIMILARITY SEARCH *****
                    else if(searchType == SearchTopicsDialog.SIMILARITY) {
                        try {
                            //setDefaultLogger();
                            //setState(EXECUTE);
                            //log("Searching for similar topics.");
                            Collection<Topic> results = dialog.getSimilarTopics(topicMap, this);
                            setState(CLOSE);
                            
                            SearchTopicsResults resultDialog = new SearchTopicsResults(wandora, results);
                            resultDialog.setVisible(true);
                            anotherSearch = resultDialog.doSearchAgain();
                        }
                        catch(Exception e){
                            wandora.handleError(e);
                            return;
                        }
                    }

                    // ***** QUERY SEARCH ******
                    else if(searchType == SearchTopicsDialog.QUERY ) {
                        java.awt.Component focusOwner=wandora.getFocusOwner();
                        try {
                            //setDefaultLogger();
                            //setState(EXECUTE);
                            //log("Searching topics by query.");
                            MixedTopicTable resultsTable = dialog.getTopicsByQuery(context.getContextObjects());
                            if(resultsTable != null) {
                                SearchTopicsResults resultDialog = new SearchTopicsResults(wandora, resultsTable);
                                resultDialog.setVisible(true);
                                anotherSearch = resultDialog.doSearchAgain();
                            }
                            else {
                                WandoraOptionPane.showMessageDialog(wandora, "No search results.");
                                anotherSearch = true;
                            }
                        }
                        catch(Exception e){
                            wandora.handleError(e);
                            return;
                        }
                        finally{
                            wandora.gainFocus(focusOwner);
                        }
                    }
                    else if(searchType == SearchTopicsDialog.TMQL ){
                        java.awt.Component focusOwner=wandora.getFocusOwner();
                        try {
                            MixedTopicTable resultsTable = dialog.getTopicsByTMQL();
                            if(resultsTable != null) {
                                SearchTopicsResults resultDialog = new SearchTopicsResults(wandora, resultsTable);
                                resultDialog.setVisible(true);
                                anotherSearch = resultDialog.doSearchAgain();
                            }
                            else {
                                WandoraOptionPane.showMessageDialog(wandora, "No search results.");
                                anotherSearch = true;
                            }
                        }
                        catch(Exception e){
                            wandora.handleError(e);
                            return;
                        }
                        finally{
                            wandora.gainFocus(focusOwner);
                        }
                    }
                }
            }
            while(anotherSearch);
        }
    }

    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/find_topics.png");
    }
    
    @Override
    public String getName() {
        return "Search";
    }

    @Override
    public String getDescription() {
        return "Search for topics.";
    }
    
    public boolean useDefaultGui() {
        return false;
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
}
