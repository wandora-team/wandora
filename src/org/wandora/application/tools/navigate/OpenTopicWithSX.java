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
 * OpenTopicWithSX.java
 *
 * Created on July 19, 2004, 2:25 PM
 */

package org.wandora.application.tools.navigate;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import org.wandora.utils.swing.*;

import javax.swing.*;


/**
 *
 * @author  olli, akivela
 */
public class OpenTopicWithSX extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	private static InputDialogWithHistory dialog = null;
    

    public OpenTopicWithSX() {
    }
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        if(dialog == null) {
            dialog = new InputDialogWithHistory((java.awt.Frame) wandora, true, "Enter subject indentifier or subject locator", "Go to topic...");
        }
        
        String sx = dialog.showDialog();
        if(sx != null && dialog.accepted) {
            sx = Textbox.trimExtraSpaces(sx);
            if(sx.length() > 0) {
                TopicMap topicMap = wandora.getTopicMap();
                Topic t = topicMap.getTopic(sx);
                if(t == null) {
                    t = topicMap.getTopicBySubjectLocator(sx);
                    if(t == null) {
                        WandoraOptionPane.showMessageDialog(wandora, "Topic not found with given SI or SL.", WandoraOptionPane.ERROR_MESSAGE);
                    }
                }
                if(t != null) {
                    wandora.openTopic(t);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Open with SX";
    }

    @Override
    public String getDescription() {
        return "Open a topic with a subject identifier URI or "+
               "a subject locator URI.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/open_topic_sx.png");
    }
    

    
}
