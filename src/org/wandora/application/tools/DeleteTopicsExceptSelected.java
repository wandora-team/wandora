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
 */


package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;


/**
 *
 * @author akivela
 */
public class DeleteTopicsExceptSelected extends AbstractWandoraTool implements WandoraTool {



    /** Creates a new instance of DeleteTopicsExceptSelected */
    public DeleteTopicsExceptSelected() {
    }




    @Override
    public String getName() {
        return "Delete all topics except selected";
    }

    @Override
    public String getDescription() {
        return "Deleted all topics except selected ones in current layer.";
    }


    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        Iterator selectedTopics = getContext().getContextObjects();
        Topic selectedTopic = null;
        int count = 0;

        int answer = WandoraOptionPane.showConfirmDialog(wandora,"This tool deletes all topics in selected layer except selected topics! Are you sure you want to continue?", "Delete all topics except selected?", WandoraOptionPane.YES_NO_OPTION);

        if(answer == WandoraOptionPane.YES_OPTION) {

            setDefaultLogger();
            TopicMap contextTopicMap = wandora.getTopicMap(); // solveContextTopicMap(wandora, context);
            Iterator<Topic> allTopics = contextTopicMap.getTopics();

            Topic atopic = null;
            while(allTopics.hasNext() && !forceStop()) {
                atopic = allTopics.next();
                try {
                    if(atopic != null && !atopic.isRemoved() && !atopic.isDeleteAllowed()) {
                        boolean deleteATopic = true;
                        selectedTopics = getContext().getContextObjects();
                        if(selectedTopics != null && selectedTopics.hasNext()) {
                            while(selectedTopics.hasNext() && !forceStop()) {
                                selectedTopic = (Topic) selectedTopics.next();
                                if(selectedTopic != null) {
                                    if(selectedTopic.mergesWithTopic(atopic)) {
                                        deleteATopic = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if(deleteATopic) {
                            try {
                                hlog("Deleting topic '" + getTopicName(atopic) + "'.");
                                atopic.remove();
                                count++;
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("Total " + count + " topics deleted!");
            setState(WAIT);
        }
    }

}
