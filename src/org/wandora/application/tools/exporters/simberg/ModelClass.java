
package org.wandora.application.tools.exporters.simberg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * @author olli
 */


public class ModelClass {
    private String name;
    private final LinkedHashSet<ModelField> fields=new LinkedHashSet<ModelField>();

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
