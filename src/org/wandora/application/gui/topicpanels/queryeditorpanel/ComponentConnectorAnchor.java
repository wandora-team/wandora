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
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Container;
import java.awt.Point;
import javax.swing.JComponent;

/**
 *
 * @author olli
 */


public class ComponentConnectorAnchor extends ConnectorAnchor {
    protected JComponent component;
    protected Direction exitDirection;
    public ComponentConnectorAnchor(JComponent component,Direction exitDirection){
        this.component=component;
        this.exitDirection=exitDirection;
    }
    public ComponentConnectorAnchor(JComponent component,Direction exitDirection,boolean hasFrom,boolean hasTo){
        this(component,exitDirection);
        this.hasFromAnchor=hasFrom;
        this.hasToAnchor=hasTo;
    }
    @Override
    public JComponent getComponent(){
        return this.component;
    }

    @Override
    public JComponent getRootComponent() {
        return findEditor().getGraphPanel();
    }

    
    @Override
    public QueryEditorComponent getEditor(){
        return findEditor();
    }

    protected QueryEditorComponent findEditor(){
        Container parent=component.getParent();
        while(parent!=null && !(parent instanceof QueryEditorComponent)){
            parent=parent.getParent();
        }
        if(parent!=null) return (QueryEditorComponent)parent;
        else return null;
    }        

    @Override
    public Point getAnchorPoint(){
        switch(exitDirection){
            case UP:
                return new Point(this.component.getWidth()/2,0);
            case RIGHT:
                return new Point(this.component.getWidth(),this.component.getHeight()/2);
            case DOWN:
                return new Point(this.component.getWidth()/2,this.component.getHeight());
            case LEFT:
                return new Point(0,this.component.getHeight()/2);
        }
        // shouldn't happen
        return new Point(0,0);
    }

    @Override
    public Direction getExitDirection(){
        return exitDirection;
    }
    
}
