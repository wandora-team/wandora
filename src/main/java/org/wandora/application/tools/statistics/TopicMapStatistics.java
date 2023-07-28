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
 * TopicMapStatistics.java
 *
 * Created on 25. toukokuuta 2006, 19:50
 *
 */

package org.wandora.application.tools.statistics;


import org.wandora.application.tools.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;

import javax.swing.*;

/**
 *
 * @author akivela
 */
public class TopicMapStatistics extends AbstractWandoraTool implements WandoraTool {
    
	private static final long serialVersionUID = 1L;

	public TopicMapStatistics() {
    }
    public TopicMapStatistics(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            TopicMap map = solveContextTopicMap(wandora, context);
            String name = solveNameForTopicMap(wandora, map);
            TopicMapStatisticsDialog d = new TopicMapStatisticsDialog(wandora, map, name);
            d.setVisible(true);
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_info.png");
    }
    
    
    @Override
    public String getName() {
        return "Topic map statistics";
    }

    @Override
    public String getDescription() {
        return "View topic map statistics.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
}
