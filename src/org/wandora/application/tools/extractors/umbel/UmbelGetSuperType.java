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
 * 
 */

package org.wandora.application.tools.extractors.umbel;

/**
 *
 * @author akivela
 */


public class UmbelGetSuperType extends UmbelGetConcept {
    
    
    public static final String API_URL = "http://umbel.org/ws/super-type/";

    
    @Override
    public String getName(){
        return "Umbel super type extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extract super types from Umbel knowledge graph.";
    }
    
    @Override
    public String getApiRequestUrlFor(String str) {
        return API_URL + urlEncode(str);
    }
    
}
