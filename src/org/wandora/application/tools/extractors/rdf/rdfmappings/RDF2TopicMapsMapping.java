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
 * RDF2TopicMapsMapping.java
 *
 * Created on 13.2.2009,15:25
 */

package org.wandora.application.tools.extractors.rdf.rdfmappings;

import org.wandora.utils.Tuples.*;



/**
 *
 * @author akivela
 */


public abstract class RDF2TopicMapsMapping {

    
    public abstract String[] getRoleMappings();
    public abstract String[] getBasenameMappings();
    
    
    
    public static final String DEFAULT_SUBJECT_ROLE_SI = "http://wandora.org/si/core/rdf-subject";
    public static final String DEFAULT_SUBJECT_ROLE_BASENAME = "subject-role";
    public static final String DEFAULT_OBJECT_ROLE_SI = "http://wandora.org/si/core/rdf-object";
    public static final String DEFAULT_OBJECT_ROLE_BASENAME = "object-role";
    
    
    

    public T2<String,String> solveSubjectRoleFor(String predicate, String subject) {
        String si = null;
        String bn = null;
        String[] roles = getRoleMappings();
        if(predicate != null) {
            for(int i=0; i<roles.length; i=i+5) {
                if(predicate.equals(roles[i])) {
                    si = roles[i+1];
                    bn = roles[i+2];
                    break;
                }
            }
        }
        return new T2(si, bn);
    }
    
    
    public T2<String,String> solveObjectRoleFor(String predicate, String object) {
        String si = null;
        String bn = null;
        String[] roles = getRoleMappings();
        if(predicate != null) {
            for(int i=0; i<roles.length; i=i+5) {
                if(predicate.equals(roles[i])) {
                    si = roles[i+3];
                    bn = roles[i+4];
                    break;
                }
            }
        }
        return new T2(si, bn);
    }
    
    
    
    public String solveBasenameFor(String si) {
        if(si == null) return null;
        String bn = null;
        String[] basenameMappings = getBasenameMappings();
        for(int i=0; i<basenameMappings.length; i=i+2) {
            if(si.equals(basenameMappings[i])) {
                bn = basenameMappings[i+1];
                break;
            }
        }
        return bn;
    }
    
}
