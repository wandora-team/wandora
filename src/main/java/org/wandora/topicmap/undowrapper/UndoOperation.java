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

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author olli
 */


public abstract class UndoOperation {

    private static final AtomicInteger operationCounter=new AtomicInteger(0);
    
    protected int operationNumber;
    protected boolean isMarker;
    
    public UndoOperation() {
        isMarker=false;
        operationNumber=operationCounter.getAndIncrement();
    }
    
    public int getOperationNumber(){
        return operationNumber;
    }
    
    public boolean isMarker(){
        return isMarker;
    }
    
    public boolean canUndo() {
        return isMarker;
    }
    
    public boolean canRedo(){
        return isMarker;        
    }
    
    public abstract void undo() throws UndoException;
    public abstract void redo() throws UndoException;
    
    public abstract String getLabel();
    
    public String getUndoLabel(){return getLabel();}
    public String getRedoLabel(){return getLabel();}

    public String getDescription() {return getLabel();}
    
    // In some cases we might want to combine several operations into one.
    // Override this and make it return the combination of first doing
    // the previous edit and then this edit in single operation. That
    // operation will then replace these two operations in the buffer.
    public UndoOperation combineWith(UndoOperation previous){return null;}
}
