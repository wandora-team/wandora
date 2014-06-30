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
 */


package org.wandora.topicmap.undowrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;

/**
 * <p>
 * UndoBuffer implements a storage for undo operations. Stored undo operations 
 * are grouped by a special marker-operations, also stored into the buffer.
 * Marker-operation is added to the buffer <b>after</b> real operations.
 * Undoing processes all undo-operations till next marker-operation.
 * </p>
 * <p>
 * UndoBuffer contains a redo-buffer also. Undoed operations are stored
 * in redo-buffer until next operation is stored into the undo-buffer.
 * </p>
 * <p>
 * Undo-buffer's size is limited to <code>MAX_BUFFER_SIZE</code> operations. 
 * Undo-buffer removes marked sets of operations until the number of operations
 * is below <code>MAX_BUFFER_SIZE</code>.
 * </p>
 *
 * @author olli
 */


public class UndoBuffer {
    
    /*
     * Should the UndoBuffer skip markers that have no preceeding
     * (real) undoable operations. By default UndoBuffer doesn't
     * store empty markers.
     */
    public static boolean SKIP_EMPTY_OPERATIONS = true;
    
    /*
     * Should the UndoBuffer inform Wandora user when the size of 
     * undo-buffer hits <code>MAX_BUFFER_SIZE</code>.
     */
    public static boolean SHOW_MESSAGE = true;
    
    /*
     * How many operations the undo-buffer can hold until it tries to
     * remove oldest operations.
     */
    public static final int MAX_BUFFER_SIZE = 50000;
    public static boolean SKIP_OPERATIONS_TILL_NEXT_MARKER = false;
 
    /*
     * Actual storage of undoable operations.
     */
    private LinkedList<UndoOperation> undobuffer;
    
    /*
     * Actual storage of undoed operations (that can be redoed).
     */
    private LinkedList<UndoOperation> redobuffer;
    
    public UndoBuffer(){
        undobuffer=new LinkedList<UndoOperation>();
        redobuffer=new LinkedList<UndoOperation>();
    }
    
    /*
     * Clears both undo-buffer and redo-buffer.
     */
    public void clear(){
        undobuffer.clear();
        redobuffer.clear();
    }
    
    
    /*
     * This is the hearth of UndoBuffer. AddOperation method stores given
     * operation into the undo-buffer. There is a special logic to prevent
     * empty markers to smudge the undo-buffer. Also, added operation
     * may combine with the previous operation if they support combining.
     * Method also checks if the size of undo-buffer has exceeded the 
     * <code>MAX_BUFFER_SIZE</code> and removes operations if necessary.
     */
    public void addOperation(UndoOperation op) {
        if(SKIP_OPERATIONS_TILL_NEXT_MARKER && !op.isMarker()) {
            return;
        }
        if(SKIP_OPERATIONS_TILL_NEXT_MARKER && op.isMarker()) {
            SKIP_OPERATIONS_TILL_NEXT_MARKER = false;
            return;
        }
        
        if(!op.isMarker()) {
            // Redo buffer becomes invalid after new operation.
            redobuffer.clear();
        }
        if(!undobuffer.isEmpty()) {
            UndoOperation previous=undobuffer.peekLast();
            
            // If there already is a marker on top of the buffer, don't add another.
            if(SKIP_EMPTY_OPERATIONS && previous.isMarker() && op.isMarker()) return;
            
            // Check if new operation can be combined with a previous one.
            UndoOperation combined=op.combineWith(previous);
            if(combined!=null) {
                undobuffer.removeLast();
                op = combined;
            }
        }
        else {
            // If undo buffer is empty, we really shouldn't add a marker first.
            if(SKIP_EMPTY_OPERATIONS && op.isMarker()) return;
        }
        
        undobuffer.addLast(op);
        //System.out.println("undobuffer["+ undobuffer.size() +"] "+op);
        
        // If the undo buffer size is too big, remove oldest operation.
        if(undobuffer.size() > MAX_BUFFER_SIZE ) {
            if(SHOW_MESSAGE) {
                Wandora w = Wandora.getWandora();
                WandoraOptionPane.showMessageDialog(w, 
                        "Undo buffer contains too many operations ("+undobuffer.size()+"). "+
                        "Wandora removes oldest operations in the undo buffer. "+
                        "This is a one time message. "+
                        "Next time the buffer is cleaned automatically without any messages.", 
                        "Undo buffer contains too many operations", 
                        WandoraOptionPane.INFORMATION_MESSAGE);
                SHOW_MESSAGE = false;
            }
            System.out.println("Removing oldest operations in the undo buffer");
            while(!undobuffer.isEmpty() && !undobuffer.peekFirst().isMarker()) {
                undobuffer.removeFirst();
            }
            if(!undobuffer.isEmpty()) {
                // Remove the marker if any.
                undobuffer.removeFirst(); 
            }
            if(undobuffer.isEmpty()) {
                // If the undobuffer is empty, do not store any operations during
                // current execution cycle.
                SKIP_OPERATIONS_TILL_NEXT_MARKER = true;
            }
        }
    }
    
    
    
    public void pruneOne(){
        while(!undobuffer.isEmpty() && undobuffer.peekLast().isMarker()) {
            undobuffer.removeLast(); // This handles all preceding markers!
        }
        while(!undobuffer.isEmpty() && !undobuffer.peekLast().isMarker()){
            undobuffer.removeLast();
        }
    }
    
    
    /*
     * This is a shurtcut method to add a marker into the undo-buffer.
     * Markers are used to separate undo-operations from different 
     * sources. 
     */
    public void addMarker(String label){
        addMarker(label,label);
    }
    
    /*
     * This is a shurtcut method to add a marker into the undo-buffer.
     * Markers are used to separate undo-operations from different 
     * sources. 
     */
    public void addMarker(String undoLabel,String redoLabel){
        addOperation(new UndoMarker(undoLabel,redoLabel));
    }
    
    
    
    /*
     * Undo next available operation in the undo-buffer.
     */
    private void undoOne() throws UndoException {
        UndoOperation op=undobuffer.removeLast();
        // System.out.println("undo: "+op);
        op.undo();
        redobuffer.addFirst(op);
    }
    
    
    
    /*
     * Undo operations under the first marker, until next marker is faced or
     * the undo-buffer is empty.
     */
    public void undo() throws UndoException {
        if(undobuffer.isEmpty()) throw new UndoException("Nothing to undo.");

        if(!undobuffer.isEmpty() && undobuffer.peekLast().isMarker()) {
            undoOne(); // This handles the marker
        }
        while(!undobuffer.isEmpty() && !undobuffer.peekLast().isMarker()){
            undoOne();
        }
    }
    
    
    
    /*
     * Redo exactly (and only) first redoable operation if such exists.
     */
    private void redoOne() throws UndoException {
        UndoOperation op=redobuffer.removeFirst();
        // System.out.println("undobuffer["+ undobuffer.size() +"] "+op);
        op.redo();
        undobuffer.addLast(op);
    }
    
    
    /*
     * Redo next set of redoable operations.
     */
    public void redo() throws UndoException {
        if(redobuffer.isEmpty()) throw new UndoException("Nothing to redo.");
        
        while(!redobuffer.isEmpty() && !redobuffer.peekFirst().isMarker()){
            redoOne();
        }
        if(!redobuffer.isEmpty() && redobuffer.peekFirst().isMarker()){
            redoOne();
        }
    }
    
    // -----------------
    
    
    /*
     * Is there any undoable operations available in the undo-buffer.
     */
    public boolean canUndo(){
        return !undobuffer.isEmpty();
    }
    
    /*
     * Is there any redoable operations available in the redo-buffer.
     */
    public boolean canRedo(){
        return !redobuffer.isEmpty();
    }
    
    public int getUndoOperationNumber(){
        if(undobuffer.isEmpty()) return Integer.MIN_VALUE;
        else return undobuffer.peekLast().getOperationNumber();
    }
    public int getRedoOperationNumber(){
        if(redobuffer.isEmpty()) return Integer.MAX_VALUE;
        else return redobuffer.peekFirst().getOperationNumber();
    }
    
    
    /*
     * Should the undo-buffer reject a marker operation if it doesn't
     * preceed real undoable operations i.e. it is empty. This method is used
     * to set the static boolean type variable.
     */
    public void setSkipEmptyOperations(boolean s) {
        SKIP_EMPTY_OPERATIONS = s;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    /*
     * Return all stored undo-operations. This method is used to get a 
     * list of stored operations viewed in the configuration dialog
     * of Undo (and Redo) tools.
     */    
    public Collection<UndoOperation> getOperations() {
        ArrayList<UndoOperation> ops = new ArrayList();
        ops.addAll(undobuffer);
        return ops;
    }
}
