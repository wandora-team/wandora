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

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.ActionContentModifier;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.gui.topicpanels.dockingpanel.WandoraDockable;

/**
 *
 * @author akivela
 */


public class TopicPanelMenuAction extends SimpleButtonAction {

    private TopicPanel topicPanel = null;
    
    
    public TopicPanelMenuAction(TopicPanel tp) {
        super(false);
        this.topicPanel = tp;
        
        this.setIcon( topicPanel.getIcon() );
        this.setIcon( ActionContentModifier.NONE_HOVER, topicPanel.getIcon() );
        this.setIcon( ActionContentModifier.NONE_PRESSED, topicPanel.getIcon() );
    }
    
    
    @Override
    public Icon getIcon(Dockable dckbl, ActionContentModifier acm) {
        return topicPanel != null ? topicPanel.getIcon() : null;
    }
    @Override
    public Icon getIcon(ActionContentModifier modifier) {
        return getIcon(modifier);
    }
    @Override
    public Icon getDisabledIcon() {
        return topicPanel != null ? topicPanel.getIcon() : null;
    }


    @Override
    public String getText(Dockable dckbl) {
        return (topicPanel != null ? topicPanel.getName() : "Topic panel") + " options";
    }

    @Override
    public String getTooltipText(Dockable dckbl) {
        return getText(dckbl);
    }

    @Override
    public void action(Dockable dockable) {
        System.out.println("ACTION Open topic panel options TRIGGERED");
        try {
            if(topicPanel != null) {
                JPopupMenu popupMenu = topicPanel.getViewPopupMenu();
                Point p = new Point(dockable.getComponent().getWidth(), 0);
                if(dockable instanceof WandoraDockable) {
                    System.out.println("Dockable is WandoraDockable");
                    MouseEvent me = ((WandoraDockable) dockable).getLastMouseEvent();
                    System.out.println("Dockable's last mousevent is "+me);
                    if(me != null) {
                        p = me.getPoint();
                    }
                }
                popupMenu.show(dockable.getComponent(), p.x, p.y );
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
}
