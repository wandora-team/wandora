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
 * 
 * Search.java
 *
 * Created on August 30, 2004, 3:32 PM
 */

package org.wandora.application.tools;


import de.topicmapslab.tmql4j.components.processor.results.model.ResultType;
import de.topicmapslab.tmql4j.components.processor.runtime.ITMQLRuntime;
import de.topicmapslab.tmql4j.components.processor.runtime.TMQLRuntimeFactory;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import java.util.*;
import javax.swing.*;


/**
 *
 * @author  olli, ak
 */
public class Search extends AbstractWandoraTool implements WandoraTool {
    
    private static SearchTopicsDialog dialog = null;
    

    
    
    
    @Override
    public void execute(final Wandora wandora, Context context) {
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
        return "Searches for topics.";
    }
    
    public boolean useDefaultGui() {
        return false;
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
}




/* MOVED TO SearchTopicsResults

class SearchResults extends JDialog {
    private Collection<Topic> res;
    public SearchResults(Wandora parent,boolean modal, Collection<Topic> res){
        super(parent,modal);
        this.res=res;
        this.setTitle("Search results");

        TopicTable table = new TopicTable(parent);
        table.initialize(res.toArray(new Topic[] {} ), null);
        this.getContentPane().setLayout(new java.awt.BorderLayout());
        WandoraButton cancel=new WandoraButton();
        cancel.setText("Close");
        final SearchResults thisf=this;
        cancel.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e){
                thisf.setVisible(false);
            }
        });
        JScrollPane sp=new JScrollPane(table);
        this.getContentPane().add(cancel,java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(sp,java.awt.BorderLayout.CENTER);
        this.setSize(400,500);
        this.setLocation(parent.getLocation().x+parent.getWidth()/2-this.getWidth()/2, 
                         parent.getLocation().y+parent.getHeight()/2-this.getHeight()/2);
    }
}
 * 
 * 
*/
