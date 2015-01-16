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
 */
package org.wandora.application.tools.selections;

import java.util.Iterator;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class LocateSelectTopicInTree extends AbstractWandoraTool implements WandoraTool {

    

    
    /** Creates a new instance of LocateSelectTopicInTree */
    public LocateSelectTopicInTree() {
    }
    public LocateSelectTopicInTree(Context preferredContext) {
        setContext(preferredContext);
    }
 
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        Iterator contextTopics = context.getContextObjects();
        TopicTree tree = wandora.getCurrentTopicTree();
        if(tree != null && contextTopics != null && contextTopics.hasNext()) {
            try {
                boolean found = tree.selectTopic((Topic) contextTopics.next());
                if(!found) {
                    WandoraOptionPane.showMessageDialog(wandora, "Topic is not visible in the topic tree and can't be selected.", "Topic not found in the tree", WandoraOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "Locate and select topic in tree";
    }

    @Override
    public String getDescription() {
        return "Locates current topic in topic tree and selects the located topic.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_open.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
}
