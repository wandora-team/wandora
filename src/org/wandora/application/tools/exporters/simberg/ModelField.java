
package org.wandora.application.tools.exporters.simberg;

/**
 *
 * @author olli
 */


public class ModelField {
    private String name;
    private Type type;

    public ModelField() {
    }

    public ModelField(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Type {
        String,StringList,Topic,TopicList
    }
}
