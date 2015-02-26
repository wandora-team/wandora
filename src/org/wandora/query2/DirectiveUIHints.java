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
 *
 */
package org.wandora.query2;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.wandora.application.Wandora;

/**
 *
 * @author olli
 */


public class DirectiveUIHints implements Serializable {
    
    protected String label;
    protected String category;
    protected Constructor[] constructors;
    protected Addon[] addons;
    
    protected Class<? extends Directive> cls;

    public DirectiveUIHints(Class<? extends Directive> cls) {
        this.cls=cls;
    }

    public DirectiveUIHints(Class<? extends Directive> cls,Constructor[] constructors) {
        this(cls);
        this.constructors = constructors;
    }

    public DirectiveUIHints(Class<? extends Directive> cls,Constructor[] constructors, Addon[] addons) {
        this(cls);
        this.constructors = constructors;
        this.addons = addons;        
    }
    
    public DirectiveUIHints(Class<? extends Directive> cls,Constructor[] constructors, Addon[] addons, String label, String category) {
        this(cls);
        this.constructors = constructors;
        this.addons = addons;
        this.label = label;
        this.category = category;
    }
    
    public Class<? extends Directive> getDirectiveClass(){
        return cls;
    }
    
    public String getCategory(){
        return category;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setCategory(String s){
        this.category=s;
    }
    
    public Constructor[] getConstructors(){
        return constructors;
    }
    public Addon[] getAddons(){
        return addons;
    }
    
    @Override
    public String toString(){
        return label;
    }
    
    public static void cleanConstructorArray(ArrayList<Constructor> constructors){
        // TODO: remove redundant constructors. For example, if we have
        // one which takes a directive array and then for convenience we also
        // have versions which take one, or two, or three directives, remove all
        // but the array version.
    }
    
    public static DirectiveUIHints guessHints(Class<? extends Directive> cls){
        ArrayList<Constructor> constructors=new ArrayList<Constructor>();
        Outer: for(java.lang.reflect.Constructor<?> c : cls.getConstructors()){
            ArrayList<Parameter> ps=new ArrayList<Parameter>();
            Class<?>[] params=c.getParameterTypes();
            for (Class<?> param : params) {
                boolean multiple=false;
                if(param.isArray()) {
                    multiple=true;
                    param=param.getComponentType();
                    if(param.isArray()) {
                        // multidimensional array
                        continue Outer;
                    }
                }
                
                String label=param.getSimpleName();
                if(multiple) label+="[]";
                
                Parameter p=new Parameter(param, multiple, label);
                ps.add(p);
            }
            
            String label=c.getName();
            
            constructors.add(new Constructor(ps.toArray(new Parameter[ps.size()]),label));
        }
        
        cleanConstructorArray(constructors);
        DirectiveUIHints ret=new DirectiveUIHints(cls,constructors.toArray(new Constructor[constructors.size()]));
        ret.setLabel(cls.getSimpleName());
        return ret;
    }
    
    public static DirectiveUIHints getDirectiveUIHints(Class<? extends Directive> cls){
        if(Provider.class.isAssignableFrom(cls)) {
            try{
                Method m=cls.getMethod("getUIHints");
                if(Modifier.isStatic(m.getModifiers())){
                    return (DirectiveUIHints)m.invoke(null);
                }
                else {
                    return ((Provider)cls.newInstance()).getUIHints();
                }
            }catch( IllegalAccessException | InstantiationException | NoSuchMethodException| InvocationTargetException e){
                Wandora.getWandora().handleError(e);
            }
        }
        
        return guessHints(cls);
    }
    
    
    public static class Parameter implements Serializable {
        /**
         * Type of the parameter. Use Directive, Operand, String, Integer or
         * whatever else is suitable. Do not make this an array type though,
         * use the multiple flag for that.
         */
        @JsonIgnore
        protected Class<?> type;
        /**
         * Is this parameter an array?
         */
        protected boolean multiple;
        /**
         * Label to show in the UI.
         */
        protected String label;

        public Parameter(){}

        public Parameter(Class<?> type, boolean multiple, String label) {
            this.type = type;
            this.multiple = multiple;
            this.label = label;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.type);
            hash = 23 * hash + (this.multiple ? 1 : 0);
            hash = 23 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Parameter other = (Parameter) obj;
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            if (this.multiple != other.multiple) {
                return false;
            }
            if (!Objects.equals(this.label, other.label)) {
                return false;
            }
            return true;
        }

        public String getTypeName(){
            if(type==null) return null;
            else return type.getName();
        }
        
        public void setTypeName(String s){
            if(s==null) type=null;
            else {
                try{
                    type=Class.forName(s);
                }
                catch(ClassNotFoundException cnfe){
                    throw new RuntimeException(cnfe);
                }
            }
        }
        
        
        @JsonIgnore
        public Class<?> getType() {
            return type;
        }

        @JsonIgnore
        public void setType(Class<?> type) {
            this.type = type;
        }

        public boolean isMultiple() {
            return multiple;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
        
        @JsonIgnore
        public Class<?> getReflectType(){
            Class<?> ret=type;
            if(multiple) ret=Array.newInstance(ret, 0).getClass();
            return ret;
        }

        /*
        @JsonIgnore
        public HashMap<String,String> getOptions(String prefix){
            HashMap<String,String> ret=new HashMap<String,String>();
            ret.put(prefix+"label",label);
            ret.put(prefix+"type",type.getName());
            ret.put(prefix+"multiple",""+multiple);
            return ret;
        }
        
        @JsonIgnore
        public static Parameter readOptions(HashMap<String,String> map,String prefix){
            Parameter ret=new Parameter();
            ret.label=map.get(prefix+"label");
            ret.multiple=Boolean.parseBoolean(map.get(prefix+"multiple"));
            String cls=map.get(prefix+"type");
            try{
                ret.type=Class.forName(cls);
            }catch(ClassNotFoundException cnfe){ throw new RuntimeException(cnfe); }
            return ret;
        }*/
    }
    
    
    public static String indent(String s,int amount){
        StringBuilder sb=new StringBuilder();
        String[] ss=s.split("\n");
        for(int i=0;i<ss.length;i++){
            for(int j=0;j<amount;j++) sb.append(" ");
            sb.append(ss[i]);
            sb.append("\n");
        }
        return sb.toString();
    }
        
    public static class Constructor implements Serializable {
        protected Parameter[] parameters;
        protected String label;
        
        public Constructor(){}
        public Constructor(Parameter[] params,String label){
            this.parameters=params;
            this.label=label;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Arrays.deepHashCode(this.parameters);
            hash = 53 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Constructor other = (Constructor) obj;
            if (!Arrays.deepEquals(this.parameters, other.parameters)) {
                return false;
            }
            if (!Objects.equals(this.label, other.label)) {
                return false;
            }
            return true;
        }

        
        
        public Parameter[] getParameters() {
            return parameters;
        }

        public void setParameters(Parameter[] parameters) {
            this.parameters = parameters;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
        
        @JsonIgnore
        public <D> java.lang.reflect.Constructor<D> getReflectConstructor(Class<D> cls) throws NoSuchMethodException {
            return resolveConstructor(cls);
        }
        
        @JsonIgnore
        public <D> java.lang.reflect.Constructor<D> resolveConstructor(Class<D> cls) throws NoSuchMethodException {
            Class[] params=new Class[parameters.length];
            for(int i=0;i<params.length;i++){
                params[i]=parameters[i].getReflectType();
            }
            java.lang.reflect.Constructor<D> c=cls.getConstructor(params);
            return c;
        }
        
        /*
        @JsonIgnore
        public HashMap<String,String> getOptions(String prefix){
            HashMap<String,String> ret=new HashMap<String,String>();
            ret.put(prefix+"label",label);
            for(int i=0;i<parameters.length;i++){
                String paramPrefix=prefix+"parameter["+i+"].";
                ret.putAll(parameters[i].getOptions(paramPrefix));
            }
            return ret;
        }
        
        @JsonIgnore
        public static Constructor readOptions(HashMap<String,String> map,String prefix){
            Constructor ret=new Constructor();
            ret.label=map.get(prefix+"label");
            ArrayList<Parameter> parameters=new ArrayList<>();
            int index=0;
            while(true){
                String paramPrefix=prefix+"parameter["+index+"].";
                String test=map.get(paramPrefix+"label");
                if(test==null) break;
                parameters.add(Parameter.readOptions(map, paramPrefix));
                index++;
            }
            ret.parameters=parameters.toArray(new Parameter[parameters.size()]);
            return ret;
        } 
        */
    }
    
    public static class Addon implements Serializable {
        protected Parameter[] parameters;
        protected String method;
        protected String label;
        
        public Addon(){}
        public Addon(String method, Parameter[] params,String label){
            this.method=method;
            this.parameters=params;
            this.label=label;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + Arrays.deepHashCode(this.parameters);
            hash = 89 * hash + Objects.hashCode(this.method);
            hash = 89 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Addon other = (Addon) obj;
            if (!Arrays.deepEquals(this.parameters, other.parameters)) {
                return false;
            }
            if (!Objects.equals(this.method, other.method)) {
                return false;
            }
            if (!Objects.equals(this.label, other.label)) {
                return false;
            }
            return true;
        }

        
        
        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        
        public Parameter[] getParameters() {
            return parameters;
        }

        public void setParameters(Parameter[] parameters) {
            this.parameters = parameters;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
        
        @JsonIgnore
        public java.lang.reflect.Method resolveMethod(Class<?> cls) throws NoSuchMethodException {
            Class[] params=new Class[parameters.length];
            for(int i=0;i<params.length;i++){
                params[i]=parameters[i].getReflectType();
            }
            java.lang.reflect.Method m=cls.getMethod(method, params);
            
            Class<?> ret=m.getReturnType();
            if(ret==null || !Directive.class.isAssignableFrom(ret)) {
                throw new RuntimeException("Addon method doesn't return a directive");
            }
                    
            return m;
        }

        /*
        @JsonIgnore
        public HashMap<String,String> getOptions(String prefix){
            HashMap<String,String> ret=new HashMap<String,String>();
            ret.put(prefix+"label",label);
            ret.put(prefix+"method",method);
            for(int i=0;i<parameters.length;i++){
                String paramPrefix=prefix+"parameter["+i+"].";
                ret.putAll(parameters[i].getOptions(paramPrefix));
            }
            return ret;
        }
        
        @JsonIgnore
        public static Addon readOptions(HashMap<String,String> map,String prefix){
            Addon ret=new Addon();
            ret.label=map.get(prefix+"label");
            ret.method=map.get(prefix+"method");
            ArrayList<Parameter> parameters=new ArrayList<>();
            int index=0;
            while(true){
                String paramPrefix=prefix+"parameter["+index+"].";
                String test=map.get(paramPrefix+"label");
                if(test==null) break;
                parameters.add(Parameter.readOptions(map, paramPrefix));
                index++;
            }
            ret.parameters=parameters.toArray(new Parameter[parameters.size()]);
            return ret;
        }         */     
    }
    
    public static interface Provider {
        public DirectiveUIHints getUIHints();
    }
}
