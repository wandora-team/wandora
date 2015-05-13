/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
package org.wandora.application.tools.exporters.iiifexport;

import java.util.ArrayList;

/**
 *
 * @author olli
 */


public class Sequence extends ModelBase {

    protected ViewingDirection viewingDirection;
    protected Canvas startCanvas;
    protected final ArrayList<Canvas> canvases=new ArrayList<>();
    
    public Sequence(){
        setType("sc:Sequence");        
    }
    
    public void addCanvas(Canvas canvas){
        this.canvases.add(canvas);
    }
    
    public ArrayList<Canvas> getCanvasesList(){
        return canvases;
    }
    

    public ViewingDirection getViewingDirection() {
        return viewingDirection;
    }

    public void setViewingDirection(ViewingDirection viewingDirection) {
        this.viewingDirection = viewingDirection;
    }

    public Canvas getStartCanvas() {
        return startCanvas;
    }

    public void setStartCanvas(Canvas startCanvas) {
        this.startCanvas = startCanvas;
    }
    
    
    
    @Override
    public JsonLD toJsonLD() {
        return super.toJsonLD()
                .appendNotNull("viewingDirection", viewingDirection)
                .appendNotNull("startCanvas", startCanvas==null?null:startCanvas.getId() )
                .append("canvases", canvases);
    }
    
    
}
