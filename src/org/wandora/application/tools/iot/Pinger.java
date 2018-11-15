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
 * 
 */

package org.wandora.application.tools.iot;

import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen
 */


public class Pinger extends AbstractWandoraTool implements WandoraTool, Runnable {


	private static final long serialVersionUID = 1L;

	@Override
    public String getDescription(){
        return "Open the IoT pinger";
    }
    
    @Override
    public String getName(){
        return "IoT pinger";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/iot_pinger.png");
    }
    
    @Override
    public void execute(final Wandora wandora, Context context) throws TopicMapException {
        PingerPanel panel = new PingerPanel(wandora.getTopicMap());
        panel.openInOwnWindow(wandora);
    }
    
}
