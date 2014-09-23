
package org.wandora.application.tools.exporters.simberg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.wandora.application.tools.exporters.simberg.ModelField.Type;

/**
 *
 * @author olli
 */


public class ModelTopic {
    private ModelClass cls;
    private final HashMap<ModelField,Object> fields=new HashMap<ModelField,Object>();

    public ModelTopic() {
    }

    public ModelTopic(ModelClass cls) {
        this.cls = cls;
    }
    
    public ModelClass getCls() {
        return cls;
    }

    public void setCls(ModelClass cls) {
        this.cls = cls;
    }
    
    public Object getField(ModelField field){
        return fields.get(field);
    }
    
    public void setField(String field,Object object){
        if(cls==null) throw new RuntimeException("ModelTopic class is null, can't set field value.");
        ModelField f=cls.findField(field);
        if(f==null) throw new RuntimeException("ModelTopic class doesn't have a field "+field);
        setField(f,object);
    }
    
    public void setField(ModelField field,Object object){
        if(cls==null) throw new RuntimeException("ModelTopic class is null, can't set field value.");
        if(!cls.hasField(field)) throw new RuntimeException("ModelTopic class doesn't have a field "+field.getName());
        
        if(object==null){
            fields.put(field,null);
        }
        else {
            Type type=field.getType();
            if(type==Type.String) {
                fields.put(field,object.toString());
            }
            else if(type==Type.StringList) {
                if(object instanceof Collection){
                    ArrayList<String> l=new ArrayList<String>();
                    for(Object o : (Collection)object){
                        if(o==null) l.add(null);
                        else l.add(o.toString());
                    }
                    fields.put(field,l);
                }
                else throw new ClassCastException("Field requires a String list but value is of type "+object.getClass().getName());
            }
            else if(type==Type.Topic){
                if(object instanceof ModelTopic){
                    fields.put(field,object);
                }
                else throw new ClassCastException("Field requires a ModelTopic but value is of type "+object.getClass().getName());
            }
            else if(type==Type.TopicList){
                if(object instanceof Collection){
                    for(Object o : (Collection)object){
                        if(!(o instanceof ModelTopic)){
                            throw new ClassCastException("Field requires a ModelTopic list but value list element is of type "+o.getClass().getName());
                        }
                    }
                    fields.put(field,new ArrayList<ModelTopic>((Collection)object));
                }
                else throw new ClassCastException("Field requires a ModelTopic list but value is of type "+object.getClass().getName());
            }
            else throw new RuntimeException("Unknown field type "+type);
        }
    }
}
