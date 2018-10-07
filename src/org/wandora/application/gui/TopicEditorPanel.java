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
 * TopicEditorPanel.java
 *
 * Created on 31. hein�kuuta 2006, 11:41
 *
 */

package org.wandora.application.gui;


import javax.swing.*;
import java.awt.*;
import org.wandora.application.*;


/**
 *
 * @author olli
 */
public class TopicEditorPanel extends EditorPanel implements Scrollable {
    

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of TopicEditorPanel */
    public TopicEditorPanel(Wandora wandora, Object dc) {
        super(wandora,dc);
    }
    
    
    /** Creates a new instance of EditorPanel */
    public TopicEditorPanel(Wandora wandora, int o, Object dc) {
        super(wandora,o,dc);
    }
    
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if(this.getComponentCount()>0){
            Component co=this.getComponent(0);
            if(co!=null && co instanceof Scrollable) return ((Scrollable)co).getScrollableTracksViewportWidth();
        }
        Container c=this.getParent();
        if(c==null) return true;
        if(c.getWidth()>500) return true;
        else return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if(this.getComponentCount()==0) return false;
        Component c=this.getComponent(0);
        if(c==null) return false;
        if(c instanceof Scrollable) return ((Scrollable)c).getScrollableTracksViewportHeight();
        if(c==parent.getStartupPanel()) return true;
        else return false;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if(orientation==SwingConstants.VERTICAL) return visibleRect.height;
        else return visibleRect.width;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    
    
}
