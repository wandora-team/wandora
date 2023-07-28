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
 *
 */

package org.wandora.application.tools.statistics;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 * This Wandora tool calculates asset weights for a topic selection. Asset
 * weight is a topic specific value describing how many associations and
 * occurrences is linked to the topic and it's nearest neighbor topics. Asset
 * weight measurement has been developed by Petra Haluzova and is described in
 * detail in her research paper at
 *
 * http://www.topicmapslab.de/publications/evaluation-of-instances-asset-in-a-topic-maps-based-ontology
 *
 * This tool was implemented by Wandora Team / Aki
 *
 * @author akivela
 */



public class AssetWeights extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	public static int CONTEXT_IS_GIVEN = 0;
    public static int CONTEXT_IS_TOPICMAP = 1;

    private int forcedContext = CONTEXT_IS_GIVEN;

    private AssetWeightPanel assetWeightPanel = null;
    


    public AssetWeights() {
    }
    public AssetWeights(int context) {
        forcedContext = context;
    }
    public AssetWeights(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/asset_weight.png");
    }

    @Override
    public String getName() {
        return "Calculate topics' asset weights";
    }

    @Override
    public String getDescription() {
        return "Calculate asset weights for selected topics. Asset weight of a topic is a "+
                "numeric value proportional to the number of associations and occurrences topic and it's neighbours have. "+
                "Asset weight is a topic measure developed by Petra Haluzova. For more info see http://www.topicmapslab.de/publications/evaluation-of-instances-asset-in-a-topic-maps-based-ontology.";
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }
    

    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        ArrayList<Topic> topics = new ArrayList<Topic>();
        String topicMapName = null;
        TopicMap tm = null;
        if(forcedContext == CONTEXT_IS_TOPICMAP) {
            tm = solveContextTopicMap(wandora, context);
            Iterator<Topic> tmTopics = tm.getTopics();
            while(tmTopics.hasNext()) topics.add(tmTopics.next());
            topicMapName = solveNameForTopicMap(wandora, tm);
            if(topicMapName == null) topicMapName = "Layer stack";
        }
        else if(forcedContext == CONTEXT_IS_GIVEN) {
            tm = wandora.getTopicMap();
            int c = 0;
            Iterator<Topic> i = context.getContextObjects();
            while(i.hasNext()) { topics.add(i.next()); c++; }
            topicMapName = "Selection of "+c+" topics";
        }
        if(!topics.isEmpty()) {
            if(assetWeightPanel == null) {
                assetWeightPanel = new AssetWeightPanel(wandora, topics);
            }
            else {
                assetWeightPanel.setTopics(topics);
            }
            assetWeightPanel.open(topicMapName, tm); // BLOCKS TILL CLOSED
        }
        else {
            WandoraOptionPane.showMessageDialog(wandora, "You have not selected any topics for asset weight calculations. Select topics and try again, please.", "Select topics");
        }
    }


}
