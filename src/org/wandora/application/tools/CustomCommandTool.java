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
 * CustomCommandTool.java
 *
 * Created on July 27, 2004, 1:56 PM
 */

package org.wandora.application.tools;

import org.wandora.topicmap.remote.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.TopicMap;


/**
 *
 * @author  olli
 */
public class CustomCommandTool extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	@Override
    public void execute(Wandora wandora, Context context) {
        TopicMap tm = this.solveContextTopicMap(wandora, context);
        if(tm instanceof RemoteTopicMap) {
            RemoteTopicMap topicMap = (RemoteTopicMap) tm;
            String cmd=WandoraOptionPane.showInputDialog(wandora,"Enter custom command");
            if(cmd!=null){
                try{
                    String result = topicMap.customCommand(cmd);
                    WandoraOptionPane.showMessageDialog(wandora,"Result: \""+result+"\"");
                } catch(ServerException se){
                    log(se);
                    //admin.getManager().handleServerError(se);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Custom command";
    }

    @Override
    public String getDescription() {
        return "Executes remote commands used with remote topic map installation of Piccolo for example.";
    }
    
/*    public SimpleMenuItem getToolMenuItem(Wandora admin) {
        return _getToolMenuItem(admin, "gui/icons/generic_tool.png");
    }*/
    
}
