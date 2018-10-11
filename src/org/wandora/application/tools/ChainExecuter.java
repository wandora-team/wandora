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
 * ChainExecuter.java
 *
 * Created on 2. kes�kuuta 2006, 13:50
 *
 */

package org.wandora.application.tools;

import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;

import javax.swing.*;
import java.util.*;


/**
 * This meta-tool executes given tools in sequential order.
 *
 * @author akivela
 */
public class ChainExecuter extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private ArrayList<WandoraTool> tools = new ArrayList<WandoraTool>();
    
    
    
    public ChainExecuter() {
    }
    public ChainExecuter(WandoraTool tool) {
        tools.add(tool);
    }
    public ChainExecuter(WandoraTool tool1, WandoraTool tool2) {
        tools.add(tool1);
        tools.add(tool2);
    }
    public ChainExecuter(WandoraTool tool1, WandoraTool tool2, WandoraTool tool3) {
        tools.add(tool1);
        tools.add(tool2);
        tools.add(tool3);
    }
    public ChainExecuter(Collection<WandoraTool> mytools) {
        for(Iterator<WandoraTool> toolIterator = mytools.iterator(); toolIterator.hasNext();) {
            tools.add(toolIterator.next());
        }
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        for(WandoraTool tool : tools) {
            try {
                if(tool != null) {
                    tool.setContext(context);
                    tool.execute(wandora, context.getContextEvent());
                    while(tool.isRunning() && !forceStop()) {
                        try {
                            Thread.sleep(250);
                        }
                        catch(Exception e) {}
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/chain_executer.png");
    }
    
    
    @Override
    public String getName() {
        return "Chain executer";
    }

    @Override
    public String getDescription() {
        return "Executes multiple tools in sequential order.";
    }
    
    
}
