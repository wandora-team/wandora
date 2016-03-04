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
 * SOMVector.java
 *
 * Created on 29. heinakuuta 2008, 17:42
 *
 */

package org.wandora.application.tools.som;

/**
 *
 * @author akivela
 */
public class SOMVector {
    private double[] vector = null;

    
    
    
    public SOMVector(int dimension) {
        dimension = Math.abs(dimension);
        this.vector = new double[dimension];
    }
    public SOMVector(double[] v) {
        this.vector = v;
    }



    public double get(int i) {
        return vector[i];
    }
    public void set(int i, double value) {
        vector[i] = value;
    }

    public int dimension() {
        if(vector == null) return 0;
        else return vector.length;
    }

    public double length() {
        double l = 0.0;
        if(vector == null) return l;
        for(int i=0; i<vector.length; i++) {
            l += vector[i]*vector[i];
        }
        return Math.sqrt(l);
    }

    public double distance(SOMVector other) throws IllegalDimensionException {
        if(other == null) throw new NullPointerException();
        if(other.dimension() != this.dimension()) throw new IllegalDimensionException();

        SOMVector diff = this.duplicate();
        diff.sub(other);

        return diff.length();
    }


    public void sub(SOMVector s) throws IllegalDimensionException {
        if(vector == null) return;
        if(s == null) throw new NullPointerException();
        if(s.dimension() != this.dimension()) throw new IllegalDimensionException();
        for(int i=0; i<vector.length; i++) {
            vector[i] -= s.get(i);
        }
    }

    public void add(SOMVector s) throws IllegalDimensionException {
        if(vector == null) return;
        if(s == null) throw new NullPointerException();
        if(s.dimension() != this.dimension()) throw new IllegalDimensionException();
        for(int i=0; i<vector.length; i++) {
            vector[i] += s.get(i);
        }
    }

    public void sub(double d) {
        if(vector == null) return;
        for(int i=0; i<vector.length; i++) {
            vector[i] -= d;
        }
    }

    public void add(double d) {
        if(vector == null) return;
        for(int i=0; i<vector.length; i++) {
            vector[i] += d;
        }
    }

    public void multiply(double d) {
        if(vector == null) return;
        for(int i=0; i<vector.length; i++) {
            vector[i] *= d;
        }
    }

    public void div(double d) {
        if(vector == null) return;
        for(int i=0; i<vector.length; i++) {
            vector[i] /= d;
        }
    }

    public void normalize() {
        if(vector == null) return;
        double d = length();
        for(int i=0; i<vector.length; i++) {
            vector[i] /= d;
        }
    }

    public SOMVector duplicate() {
        int d = this.dimension();
        SOMVector n = new SOMVector(d);
        for(int i=0; i<vector.length; i++) {
            n.set(i, vector[i]);
        }
        return n;
    }
    
    
    public void print() {
        System.out.print("[ ");
        for(int i=0; i<vector.length; i++) {
            System.out.print( vector[i]+";" );
        }
        System.out.println(" ]");
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ ");
        int m = vector.length-1;
        for(int i=0; i<m; i++) {
            sb.append(vector[i]).append( " ; ");
        }
        sb.append(vector[vector.length-1]).append( " ]");
        return sb.toString();
    }
    
    
    public String toNLString() {
        StringBuilder sb = new StringBuilder("");
        int m = vector.length-1;
        for(int i=0; i<m; i++) {
            sb.append(vector[i]).append( "\n");
        }
        sb.append( vector[vector.length-1] );
        return sb.toString();
    }
}
