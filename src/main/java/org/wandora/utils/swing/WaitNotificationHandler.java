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
 * WaitNotificationHandler.java
 *
 * Created on 1.6.2005, 11:23
 *
 * Copyright 2004-2005 Grip Studios Interactive Oy (office@gripstudios.com)
 * Created by Olli Lyytinen, AKi Kivela
 */

package org.wandora.utils.swing;
import org.wandora.utils.Delegate;
/**
 *
 * @author olli
 */
public class WaitNotificationHandler {
    
    private Delegate<Delegate.Void,Boolean> listener=null;
    
    /** Creates a new instance of WaitNotificationHandler */
    public WaitNotificationHandler() {
    }
    public WaitNotificationHandler(Delegate<Delegate.Void,Boolean> l) {
        setListener(l);
    }
    
    private int counter=0;
    
    public void showNotification(){
        counter++;
        if(listener!=null) listener.invoke(isNotificationVisible());
    }
    public void hideNotification(){
        counter--;
        if(listener!=null) listener.invoke(isNotificationVisible());
    }
    public boolean isNotificationVisible(){
        return counter>0;
    }
    
    public void setListener(Delegate<Delegate.Void,Boolean> l){
        this.listener=l;
    }
}
