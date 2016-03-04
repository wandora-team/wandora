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
 * 
 *
 * Locator.java
 *
 * Created on June 10, 2004, 11:14 AM
 */

package org.wandora.topicmap;

/**
 *
 * @author  olli
 */
public class Locator implements Comparable<Locator> {
    
    private String reference;
    private String notation;
    
    /** 
     * Creates a new instance of Locator with "URI" notation. Usually you shouldn't call this directly.
     * Instead use TopicMap.createLocator.
     */
    public Locator(String reference) {
        this("URI",reference);
    }
    /** 
     * Creates a new instance of Locator. Usually you shouldn't call this directly.
     * Instead use TopicMap.createLocator.
     */
    public Locator(String notation,String reference) {
        this.notation=notation;
        this.reference=reference;
        if(reference==null) throw new RuntimeException("Trying to create locator with null reference");
    }
    
    public String getNotation(){
        return notation;
    }
    public String getReference(){
        return reference;
    }
    @Override
    public int hashCode(){
        return notation.hashCode()+reference.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Locator)) return false;
        Locator l=(Locator)o;
        return (notation.equals(l.notation) && reference.equals(l.reference));
    }
    /* this needed to implement tmapi Locator
    public Locator resolveRelative(String relative){
        
    }
    */
    public String toExternalForm(){
        return getReference();
    }

    @Override
    public String toString(){
        return toExternalForm();
    }
    
    public static Locator parseLocator(String s){
        return new Locator(s);
    }

    @Override
    public int compareTo(Locator o) {
        int r=notation.compareTo(o.notation);
        if(r!=0) return r;
        return reference.compareTo(o.reference);
    }
    
    
}
