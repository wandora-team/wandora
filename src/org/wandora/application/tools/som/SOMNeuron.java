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
 * SOMNeuron.java
 *
 * Created on 29. heinakuuta 2008, 17:42
 *
 */



package org.wandora.application.tools.som;

/**
 *
 * @author akivela
 */
public class SOMNeuron {
    private SOMVector vector = null;
        
    public SOMNeuron(int dim) {
        this.vector = new SOMVector(dim);
    }
    
    public SOMNeuron(SOMVector v) {
        this.vector = v;
    }

    public SOMVector getSOMVector() {
        return vector;
    }
    
    public void setSOMVector(SOMVector v) {
        vector = v;
    }

    public void randomize() {
        if(vector == null) throw new NullPointerException();
        int d = vector.dimension();
        for(int i=0; i<d; i++) {
            vector.set(i, Math.random() > 0.5 ? 1 : 0);
        }
    }
        
    public void print() {
        if(vector == null) System.out.println("-null-");
        else vector.print();
    }
    @Override
    public String toString() {
        if(vector == null) return "null";
        else return vector.toString();
    }
    public String toNLString() {
        if(vector == null) return "null";
        else return vector.toNLString();
    }
    
}
