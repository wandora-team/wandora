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
 */

package org.wandora.topicmap.undowrapper;

/**
 *
 * @author olli
 */


public class UndoMarker extends UndoOperation {
    
    protected String undoLabel;
    protected String redoLabel;
    
    public UndoMarker(String label) {
        this(label,label);
    }
    
    public UndoMarker(String undoLabel,String redoLabel){
        this.undoLabel=undoLabel;
        this.redoLabel=redoLabel;
        this.isMarker=true;
    }

    @Override
    public void undo() throws UndoException {
    }

    @Override
    public void redo() throws UndoException {
    }

    @Override
    public String getLabel() {
        return undoLabel;
    }

    @Override
    public String getRedoLabel() {
        return redoLabel;
    }

    @Override
    public String getUndoLabel() {
        return undoLabel;
    }
    
    @Override
    public UndoOperation combineWith(UndoOperation previous) {
        return null;
    }
    
    
}
