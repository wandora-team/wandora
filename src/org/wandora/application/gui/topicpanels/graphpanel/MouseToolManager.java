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
 * MouseToolManager.java
 *
 * Created on 25. kesäkuuta 2007, 11:24
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class MouseToolManager implements MouseListener, MouseMotionListener {
    
    public static final int EVENT_LEFTCLICK=1;
    public static final int EVENT_RIGHTCLICK=2;
    public static final int EVENT_LEFTDOUBLECLICK=4;
    public static final int EVENT_RIGHTDOUBLECLICK=8;
    public static final int EVENT_LEFTPRESS=16;
    public static final int EVENT_RIGHTPRESS=32;
    public static final int EVENT_LEFTRELEASE=64;
    public static final int EVENT_RIGHTRELEASE=128;
    public static final int EVENT_DRAG=256;
    public static final int EVENT_MOVE=512;
    
    public static final int EVENTS_LEFTDRAG=EVENT_LEFTPRESS|EVENT_DRAG|EVENT_LEFTRELEASE;
    public static final int EVENTS_RIGHTDRAG=EVENT_RIGHTPRESS|EVENT_DRAG|EVENT_RIGHTRELEASE;
    
    public static final int MASK_SHIFT=MouseEvent.SHIFT_MASK;
    public static final int MASK_CONTROL=MouseEvent.CTRL_MASK;
    
    private ArrayList<StackEntry> toolStack;
    
    
    private MouseTool lockedTool;
    
    private TopicMapGraphPanel panel;
    /** Creates a new instance of MouseToolManager */
    public MouseToolManager(TopicMapGraphPanel panel) {
        this.panel=panel;
        clearToolStack();
    }
    
    public void addTool(int events,MouseTool tool){
        addTool(events,0,tool);
    }
    public void addTool(int events,int modifiers,MouseTool tool){
        toolStack.add(new StackEntry(events,modifiers,tool));
    }
    
    public boolean lockTool(MouseTool tool){
        if(lockedTool==null){
            lockedTool=tool;
            updateCursor(0,-1,-1);
            return true;
        }
        else {
            return false;
        }
    }
    public void releaseLockedTool(){
        lockedTool=null;
        updateCursor(0,-1,-1);
    }
    
    public void clearToolStack(){
        toolStack=new ArrayList<StackEntry>();
    }
    
    public void updateCursor(int modifiers,int x,int y){
        if(lockedTool!=null){
            Cursor cursor=lockedTool.getCursor(panel,x,y);
            if(cursor!=null) panel.updateCursor(cursor);
            else panel.updateCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        for(StackEntry tool : toolStack){
            if( (modifiers&(MASK_SHIFT)) == tool.modifiers ){
                Cursor cursor=tool.tool.getCursor(panel,x,y);
                if(cursor!=null) {
                    panel.updateCursor(cursor);
                    return;
                }
            }
        }        
        panel.updateCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void paint(Graphics2D g2){
        for(StackEntry tool : toolStack){
            tool.tool.paint(g2,panel);
        }        
    }

    private boolean dispatchEvent(MouseEvent e,int event,MouseTool tool){
        boolean processed=false;
        synchronized(panel){
            switch(event){
                case EVENT_LEFTCLICK: 
                case EVENT_RIGHTCLICK: 
                case EVENT_LEFTDOUBLECLICK:
                case EVENT_RIGHTDOUBLECLICK: 
                    processed=tool.mouseClicked(panel,e.getX(),e.getY());
                    break;
                case EVENT_LEFTPRESS:
                case EVENT_RIGHTPRESS:
                    processed=tool.mousePressed(panel,e.getX(),e.getY());
                    break;
                case EVENT_LEFTRELEASE:
                case EVENT_RIGHTRELEASE:
                    processed=tool.mouseReleased(panel,e.getX(),e.getY());
                    break;
                case EVENT_DRAG:
                    processed=tool.mouseDragged(panel,e.getX(),e.getY());
                    break;
                case EVENT_MOVE:
                    processed=tool.mouseMoved(panel,e.getX(),e.getY());
                    break;
            }
        }
        return processed;
    }
    
    public void processEvent(MouseEvent e,int event){
        int modifiers=e.getModifiers();
        if(lockedTool!=null){
            dispatchEvent(e,event,lockedTool);
            return;
        }
        for(StackEntry tool : toolStack){
            if( (tool.events&event)!=0 ){
                if( (modifiers&(MASK_SHIFT)) == tool.modifiers ){
                    if(dispatchEvent(e,event,tool.tool)) break;
                }
            }
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        boolean ctrl=((e.getModifiers()&MouseEvent.CTRL_MASK)!=0);
        if(e.getButton()==1 && !ctrl) processEvent(e,EVENT_LEFTRELEASE);
        else if(e.getButton()==3 || ctrl) processEvent(e,EVENT_RIGHTRELEASE);
    }

    public void mousePressed(MouseEvent e) {
        boolean ctrl=((e.getModifiers()&MouseEvent.CTRL_MASK)!=0);
        if(e.getButton()==1 && !ctrl) processEvent(e,EVENT_LEFTPRESS);
        else if(e.getButton()==3 || ctrl) processEvent(e,EVENT_RIGHTPRESS);
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        boolean ctrl=((e.getModifiers()&MouseEvent.CTRL_MASK)!=0);
        if(e.getClickCount()>=2){
            if(e.getButton()==1 && !ctrl) processEvent(e,EVENT_LEFTDOUBLECLICK);
            else if(e.getButton()==3 || ctrl) processEvent(e,EVENT_RIGHTDOUBLECLICK);
        }
        else{
            if(e.getButton()==1 && !ctrl) processEvent(e,EVENT_LEFTCLICK);
            else if(e.getButton()==3 || ctrl) processEvent(e,EVENT_RIGHTCLICK);
        }
    }

    public void mouseMoved(MouseEvent e) {
        processEvent(e,EVENT_MOVE);
    }

    public void mouseDragged(MouseEvent e) {
        processEvent(e,EVENT_DRAG);
    }
    
    private static class StackEntry {
        public int events;
        public int modifiers;
        public MouseTool tool;

        public int getEvents() {
            return events;
        }
        public StackEntry(int events,int modifiers, MouseTool tool){
            this.events=events;
            this.modifiers=modifiers;
            this.tool=tool;
        }
    }
    
}
