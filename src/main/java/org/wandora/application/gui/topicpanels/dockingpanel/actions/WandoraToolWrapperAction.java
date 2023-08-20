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
 */


package org.wandora.application.gui.topicpanels.dockingpanel.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.navigate.OpenTopic;
import org.wandora.topicmap.TopicMapException;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.ActionContentModifier;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;

/**
 *
 * @author akivela
 */


public class WandoraToolWrapperAction extends SimpleButtonAction {

    private WandoraTool tool = new OpenTopic();
    
    
    public WandoraToolWrapperAction(WandoraTool tool) {
        super(true);
        this.tool = tool;
        this.setIcon( tool.getIcon() );
        this.setIcon( ActionContentModifier.NONE_HOVER, tool.getIcon() );
        this.setIcon( ActionContentModifier.NONE_PRESSED, tool.getIcon() );
    }
    
    @Override
    public Icon getIcon(Dockable dckbl, ActionContentModifier acm) {
        return tool != null ? tool.getIcon() : null;
    }


    @Override
    public String getText(Dockable dckbl) {
        return tool != null ? tool.getName() : "Unknown tool";
    }

    @Override
    public String getTooltipText(Dockable dckbl) {
        return tool.getDescription();
    }

    @Override
    public void action(Dockable dockable) {
        System.out.println("ACTION TRIGGERED");
        try {
            if(tool != null) {
                tool.execute(Wandora.getWandora(), (ActionEvent) null);
            }
        }
        catch (TopicMapException ex) {
            ex.printStackTrace();
        }
    }
    
    
}
