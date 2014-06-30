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
 *
 *
 * WSTopic.java
 *
 * Created on 27. huhtikuuta 2007, 14:03
 *
 */

package org.wandora.webapi.definition;

/**
 *
 * @author olli
 */
public class WSTopic {
    
    public boolean full;
    public String baseName;
    public String[] variantTypes;
    public String[][] variantLanguages;
    public String[][] variantNames;
    public String subjectLocator;
    public String[] subjectIdentifiers;
    public String[] types;
    public WSAssociation[] associations;
    public WSOccurrence[] occurrences;
    
    /** Creates a new instance of WSTopic */
    public WSTopic() {
    }
    
}
