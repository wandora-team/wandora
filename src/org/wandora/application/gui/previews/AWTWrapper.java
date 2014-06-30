/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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


package org.wandora.application.gui.previews;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.topicpanels.AbstractTopicPanel;

/**
 *
 * @author anttirt
 */
public class AWTWrapper extends JPanel implements PreviewPanel {
    
    private Panel heavyContainer;
    private PreviewPanel innerPanel;
    private Wandora wandora;
    private Dimension prefSize;
    
    public AWTWrapper(Wandora w, PreviewPanel inner) {
        innerPanel = inner;
        wandora = w;
        setLayout(null);
        heavyContainer = new java.awt.Panel();
        add(heavyContainer);
        heavyContainer.add((Component)innerPanel);
        setSize(innerPanel.getGui().getPreferredSize());
        heavyContainer.setSize(getSize());
        heavyContainer.repaint();
        final JViewport vp = wandora.getViewPort();
        cl = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Rectangle viewRec = vp.getViewRect();
                
                try
                {
                    Point p = getTopDistance();
                    viewRec.x -= p.x;
                    viewRec.y -= p.y;
                    
                    resizeHeavy(viewRec);
                }
                catch(Exception ex) {
                    System.err.println(ex.toString());
                }
            }
        };
        prefSize = innerPanel.getGui().getPreferredSize();
        setSize(prefSize);
        setPreferredSize(prefSize);
        setMinimumSize(prefSize);
        setMaximumSize(prefSize);
        heavyContainer.setBounds(new Rectangle(0, 0, getWidth(), getHeight()));
        heavyContainer.repaint();
        wandora.getViewPort().addChangeListener(cl);
    }
    
    @Override
    public boolean isHeavy() {
        return true;
    }
    
    private void resizeHeavy(Rectangle newRec) {
        final Rectangle myBounds = new Rectangle(0, 0, prefSize.width, prefSize.height);
        if(newRec.contains(myBounds)) {
            heavyContainer.setBounds(myBounds);
        }
        else {
            final Rectangle heavyBounds = newRec.intersection(myBounds);
            heavyContainer.setBounds(heavyBounds);
            innerPanel.getGui().setLocation(-heavyBounds.x, -heavyBounds.y);
        }
    }
    
    private Point getTopDistance() throws Exception {
        Component c = this;
        Point ret = new Point();
        while(!AbstractTopicPanel.class.isInstance(c)) {
            ret.x += c.getX();
            ret.y += c.getY();
            c = c.getParent();
            if(c == null)
                throw new Exception("Couldn't find a topic panel in the parent chain!");
        }
        return ret;
    }
    
    private final ChangeListener cl;

    @Override
    public void stop() {
        innerPanel.stop();
    }

    @Override
    public void finish() {
        innerPanel.finish();
        heavyContainer.remove((Component)innerPanel);
        wandora.getViewPort().removeChangeListener(cl);
        innerPanel = null;
    }

    @Override
    public Component getGui() {
        return this;
    }

}
