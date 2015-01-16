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
 * 
 * WandoraToolType.java
 *
 * Created on Jan 12, 2009
 */


package org.wandora.application;

import java.util.*;


/**
 *
 * @author akivela
 */
public class WandoraToolType {
    
    public static final String WANDORA_BUTTON_TYPE = "Default buttons";
    public static final String BROWSER_EXTRACTOR_TYPE = "Browser extractors";

    public static final String GENERIC_TYPE = "generic";
    public static final String IMPORT_TYPE = "import";
    public static final String EXPORT_TYPE = "export";
    public static final String EXTRACT_TYPE = "extract";
    public static final String GENERATOR_TYPE = "generator";
    
    
    private Set<String> types = new HashSet<String>();
    
    
    public WandoraToolType(String type) {
        types.add(type);
    }
    public WandoraToolType(String type1, String type2) {
        types.add(type1);
        types.add(type2);
    }
    public WandoraToolType(String type1, String type2, String type3) {
        types.add(type1);
        types.add(type2);
        types.add(type3);
    }
    
    
    public Set<String> asSet() {
        return types;
    }
    
    
    
    public String oneType() {
        if(!types.isEmpty())
            return types.iterator().next();
        else 
            return null;
    }
    
    
    // ------------------------------------------------------ TEST THIS TYPE ---
    
    public boolean isOfType(String t) {
        return types.contains(t);
    }
    
    public boolean isOfType(String t1, String t2) {
        return (types.contains(t1) && types.contains(t2));
    }
    
    public boolean isOfType(String t1, String t2, String t3) {
        return (types.contains(t1) && types.contains(t2) && types.contains(t3));
    }
    
    
    // --------------------------------------------------------- MODIFY TYPE ---
    
    
    
    public void addType(String t) {
        types.add(t);
    }
    public void removeType(String t) {
        types.remove(t);
    }
    
    
    // ---------------------------------------------------- STATIC FACTORIES ---
    
    
    
    public static WandoraToolType createGenericType() {
        return new WandoraToolType(GENERIC_TYPE);
    }
    public static WandoraToolType createImportType() {
        return new WandoraToolType(IMPORT_TYPE);
    }
    public static WandoraToolType createExportType() {
        return new WandoraToolType(EXPORT_TYPE);
    }
    public static WandoraToolType createExtractType() {
        return new WandoraToolType(EXTRACT_TYPE);
    }
    public static WandoraToolType createGeneratorType() {
        return new WandoraToolType(GENERATOR_TYPE);
    }
    public static WandoraToolType createImportExportType() {
        return new WandoraToolType(IMPORT_TYPE, EXPORT_TYPE);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(""); 
        for( Iterator<String> typeIterator = types.iterator(); typeIterator.hasNext(); ) {
            sb.append(typeIterator.next());
            if(typeIterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
