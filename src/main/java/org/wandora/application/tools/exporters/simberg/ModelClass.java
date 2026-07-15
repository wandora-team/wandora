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
 */
package org.wandora.application.tools.exporters.simberg;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author olli
 */


public class ModelClass {
	
    private String name;
    private final Set<ModelField> fields=new LinkedHashSet<ModelField>();

    public ModelClass() {
    }

    public ModelClass(String name) {
        this.name = name;
    }
    
    public List<ModelField> getFields() {
        return new ArrayList<ModelField>(fields);
    }
    
    public ModelField findField(String fieldName){
        for(ModelField field : fields){
            if(field.getName().equals(fieldName)) return field;
        }
        return null;
    }

    public void addField(ModelField field){
        fields.add(field);
    }
    
    public void setFields(List<ModelField> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }
    
    public boolean hasField(ModelField f){
        return fields.contains(f);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
