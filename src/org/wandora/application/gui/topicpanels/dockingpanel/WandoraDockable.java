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



package org.wandora.application.gui.topicpanels.dockingpanel;


import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.title.DockTitle;
import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.event.MouseInputListener;
import org.wandora.application.gui.topicpanels.TopicPanel;



/**
 * 
 *
 * @author akivela
 */


public class WandoraDockable extends DefaultDockable {
    private TopicPanel topicPanel = null;
    private MouseEvent lastMouseEvent = null;
    private Component wrapper = null;
    
    
    public WandoraDockable(Component c, TopicPanel tp, String title, Icon icon) {
        super(c, title, icon);
        wrapper = c;
        DockTitle[] dtitles = listBoundTitles();
        for(int i=0; i<dtitles.length; i++) {
            DockTitle dtitle = dtitles[i];
            Component comp = dtitle.getComponent();
            //System.out.println("COMP="+comp);
            //comp.add(tp.getViewPopupMenu());
        }
        this.addMouseInputListener(new WandoraDockableMouseListener());
        topicPanel = tp;
    }
    
    
    public Component getWrapper() {
        return wrapper;
    }
            
    public TopicPanel getInnerTopicPanel() {
        return topicPanel;
    }

    public MouseEvent getLastMouseEvent() {
        return lastMouseEvent;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    public class WandoraDockableMouseListener implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            lastMouseEvent = e;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastMouseEvent = e;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            lastMouseEvent = e;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            lastMouseEvent = e;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            lastMouseEvent = e;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            lastMouseEvent = e;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastMouseEvent = e;
        }
        
    }
    
    
}
