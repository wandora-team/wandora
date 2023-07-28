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
 */


package org.wandora.utils;


import java.util.*;


public class StateMachine extends Object {

    private Vector transitions;
    private Vector currentStates;
    private boolean valid;
    private boolean initialized;
    

    
    public StateMachine() {
        valid = true;
        initialized = false;
        transitions = new Vector();
        currentStates = new Vector();
    }

    
    
    public synchronized void initialize(Object [][] transitionArray) {
        for (int i=0; i<transitionArray.length; i++) {
            if (transitionArray[i].length > 2) {
                transitions.add(new StateTransition(
                    transitionArray[i][0],
                    transitionArray[i][1],
                    transitionArray[i][2])
                    );
            }
        }
        this.initialize();
    }
    
    
    
    public synchronized void initialize() {
        StateTransition transition = null;
        for (int i=0; i<transitions.size(); i++) {
            transition = (StateTransition) transitions.elementAt(i);
            if (transition.isInitial()) {
                currentStates.add(transition.getNewState());
            }
        }
        initialized = true;
    }
    
    
    public synchronized void forward(Object input) {
        if (input != null) {
            StateTransition transition = null;
            Vector newStates = new Vector();
            for (int j=0; j<currentStates.size(); j++) {
                for (int i=0; i<transitions.size(); i++) {
                    transition = (StateTransition) transitions.elementAt(i);
                    if (transition.getInitialState().equals(currentStates.elementAt(j)) &&
                        transition.getInput().equals(input)) {
                            if (!transition.isFinal()) newStates.add(transition.getNewState());
                    }
                }
            }
            currentStates = newStates;
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public synchronized boolean hasStopped() {
        if (currentStates.size() > 0) return true;
        return false;
    }
    
    
    
}
