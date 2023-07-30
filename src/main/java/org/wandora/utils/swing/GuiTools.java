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
 *
 * 
 *
 * GuiTools.java
 *
 * Created on 6.7.2005, 9:46
 */

package org.wandora.utils.swing;



import java.awt.Component;
import java.awt.Window;

import javax.swing.JViewport;
/**
 *
 * @author olli
 */
public class GuiTools {
    
    /** Creates a new instance of GuiTools */
    public GuiTools() {
    }
 
    public static void centerWindow(Window wnd,Component parent){
        int x=parent.getLocation().x+parent.getWidth()/2-wnd.getWidth()/2;
        int y=parent.getLocation().y+parent.getHeight()/2-wnd.getHeight()/2;
        if(x<0) x=0;
        if(y<0) y=0;
        wnd.setLocation(x,y);        
    }
    
    public static Window getWindow(Component c){
        while(!(c instanceof Window) && c!=null){
            c=c.getParent();
        }
        return (Window)c;
    }
    
    public static JViewport getViewport(Component c){
        c=c.getParent();
        if(c==null) return null;
        if(c instanceof JViewport) return (JViewport)c;
        return getViewport(c);
    }
}
