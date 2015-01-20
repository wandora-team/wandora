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
import org.wandora.application.Wandora;

/**
 *
 * @author olli
 */


public class DirectiveUIHints implements Serializable {
    
    protected String label;
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
    
    public Class<? extends Directive> getDirectiveClass(){
        return cls;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    
    
    public Constructor[] getConstructors(){
        return constructors;
    }
    public Addon[] getAddons(){
        return addons;
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
    
    public static class BoundParameter {
        protected Parameter parameter;
        protected Object value;
        public BoundParameter(Parameter parameter,Object value){
            this.parameter=parameter;
            this.value=value;
        }
        public Parameter getParameter(){return parameter;}
        public Object getValue(){return value;}
    }
    
    public static class Parameter implements Serializable {
        /**
         * Type of the parameter. Use Directive, Operand, String, Integer or
         * whatever else is suitable. Do not make this an array type though,
         * use the multiple flag for that.
         */
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
        
        
        public Class<?> getType() {
            return type;
        }

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
        
        
        public Class<?> getReflectType(){
            Class<?> ret=type;
            if(multiple) ret=Array.newInstance(ret, 0).getClass();
            return ret;
        }
        
        
        public BoundParameter bind(Object value){
            return new BoundParameter(this,value);
        }
        
        public BoundParameter bindSerial(String s){
            return new BoundParameter(this,s);
        }
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
        
        public <D> java.lang.reflect.Constructor<D> getReflectConstructor(Class<D> cls) throws NoSuchMethodException {
            return resolveConstructor(cls);
        }
        
        public <D> java.lang.reflect.Constructor<D> resolveConstructor(Class<D> cls) throws NoSuchMethodException {
            Class[] params=new Class[parameters.length];
            for(int i=0;i<params.length;i++){
                params[i]=parameters[i].getReflectType();
            }
            java.lang.reflect.Constructor<D> c=cls.getConstructor(params);
            return c;
        }
        
        public <D> D newInstance(BoundParameter[] values,Class<D> cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            java.lang.reflect.Constructor<D> con=getReflectConstructor(cls);
            Object[] params=new Object[values.length];
            for(int i=0;i<values.length;i++) params[i]=values[i].getValue();
            return con.newInstance(params);
        }
        
        /*
         BoundParameters need to be bound to the already serialised
         values with this one. Use the bindSerial methods in Parameter.
        */
        public String newScript(BoundParameter[] values,Class<?> cls){
            StringBuilder sb=new StringBuilder();
            sb.append("new ");
            sb.append(cls.getSimpleName());
            sb.append("(");
            for(int i=0;i<values.length;i++){
                if(i!=0) sb.append(", ");
                Object o=values[i].getValue();
                if(o==null) sb.append("null");
                else {
                    String s=o.toString();
                    if(s.startsWith("\n")){
                        sb.append(indent(s,2));
                        if(!s.endsWith("\n")) sb.append("\n");
                    }
                    else sb.append(s);
                }                
            }
            sb.append(")");
            return sb.toString();
        }
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
        
        public Directive applyAddon(BoundParameter[] values,Directive d) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
            java.lang.reflect.Method m=resolveMethod(d.getClass());
            Object[] params=new Object[values.length];
            for(int i=0;i<values.length;i++) params[i]=values[i].getValue();
            Object ret=m.invoke(d, params);
            return (Directive)ret;
        }
        
        /*
         BoundParameters need to be bound to the already serialised
         values with this one. Use the bindSerial methods in Parameter.
        */
        public String applyAddonScript(BoundParameter[] values){
            StringBuilder sb=new StringBuilder();
            sb.append("."); sb.append(method); sb.append("(");
            for(int i=0;i<values.length;i++){
                if(i!=0) sb.append(", ");
                Object o=values[i].getValue();
                if(o==null) sb.append("null");
                else {
                    String s=o.toString();
                    if(s.startsWith("\n")){
                        sb.append(indent(s,2));
                        if(!s.endsWith("\n")) sb.append("\n");
                    }
                    else sb.append(s);
                }                
            }
            sb.append(")");
            return sb.toString();
        }
    }
    
    public static interface Provider {
        public DirectiveUIHints getUIHints();
    }
}
