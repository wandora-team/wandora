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
package org.wandora.application.gui.topicpanels.queryeditorpanel;


import java.awt.Point;
import javax.swing.JComponent;

/**
 *
 * @author olli
 */


public abstract class ConnectorAnchor {
    
    protected boolean hasFromAnchor=false;
    protected boolean hasToAnchor=false;
    
    protected ConnectorAnchor to;
    protected ConnectorAnchor from;
    protected Connector toConnector;
    protected Connector fromConnector;
    
    public static boolean setLink(QueryEditorComponent editor,ConnectorAnchor from,ConnectorAnchor to){
        // Things are nulled seemingly unnecessarily because the recursive calls
        // would otherwise cause infinite recursion. Nulling them acts as a
        // marker that we've done that anchor already.
        
        if(from==null || from.to!=to){
            if(from!=null){
                if(from.toConnector!=null){
                    editor.removeConnector(from.getToConnector());
                    from.toConnector=null;
                }
                ConnectorAnchor old=from.to;
                from.to=null;
                if(old!=null) old.setFrom(null);
                from.to=to;
            }
            
            if(to!=null){
                if(to.fromConnector!=null){
                    editor.removeConnector(to.fromConnector);
                    to.fromConnector=null;
                }
                ConnectorAnchor old=to.from;
                to.from=null;
                if(old!=null) old.setTo(null);
                to.from=from;
                
                if(from!=null){
                    from.toConnector=new Connector(from.getRootComponent(), from, to);
                    to.fromConnector=from.toConnector;
                    editor.addConnector(from.toConnector);
                }
            }
        }        
        return true;
    }
    
    public ConnectorAnchor getFrom(){return from;}
    public ConnectorAnchor getTo(){return to;}
    public Connector getFromConnector(){return fromConnector;}
    public Connector getToConnector(){return toConnector;}
    public boolean setTo(ConnectorAnchor to){
        if(!this.hasToAnchor()) return false;
        return setLink(getEditor(), this, to);
    }
    public boolean setFrom(ConnectorAnchor from){
        if(!this.hasFromAnchor()) return false;
        return setLink(getEditor(), from, this);
    }
    public boolean hasFromAnchor(){return hasFromAnchor;}
    public boolean hasToAnchor(){return hasToAnchor;}
    
    public abstract QueryEditorComponent getEditor();
    public abstract Point getAnchorPoint();
    public abstract JComponent getComponent();
    public abstract JComponent getRootComponent();
    
    public Direction getExitDirection(){
        return Direction.RIGHT;
    }
    
    public static enum Direction {
        UP, RIGHT, DOWN, LEFT;
    }
    
}
