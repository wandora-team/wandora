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


public class Manifest extends ModelBase {
    
    protected ViewingDirection viewingDirection;
    protected final ArrayList<Sequence> sequences=new ArrayList<>();
    
    public Manifest(){
        addContext("http://iiif.io/api/presentation/2/context.json");
        setType("sc:Manifest");
    }
    
    public void addSequence(Sequence sequence){
        this.sequences.add(sequence);
    }
    
    public ArrayList<Sequence> getSequencesList(){
        return sequences;
    }

    public ViewingDirection getViewingDirection() {
        return viewingDirection;
    }

    public void setViewingDirection(ViewingDirection viewingDirection) {
        this.viewingDirection = viewingDirection;
    }

    
    
    @Override
    public JsonLD toJsonLD() {
        return super.toJsonLD()
                .appendNotNull("viewingDirection", viewingDirection)
                .append("sequences",sequences);
    }
    
    
    
}
