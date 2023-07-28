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
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import javax.swing.JPanel;
import org.wandora.application.Wandora;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;
import org.wandora.query2.DirectiveUIHints.Addon;
import org.wandora.query2.DirectiveUIHints.Constructor;
import org.wandora.query2.DirectiveUIHints.Parameter;
import org.wandora.query2.Operand;
import org.wandora.query2.TopicOperand;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author olli
 */


public class DirectiveEditor extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	protected DirectiveUIHints.Constructor selectedConstructor;
    protected AbstractTypePanel[] constructorParamPanels;
    
    protected final ArrayList<AddonPanel> addonPanels=new ArrayList<AddonPanel>();
    
    protected DirectivePanel directivePanel;
    
    protected DirectiveUIHints hints;
    
    /**
     * Creates new form DirectiveEditor
     */
    public DirectiveEditor(DirectivePanel directivePanel,DirectiveUIHints hints) {
        this.directivePanel=directivePanel;
        initComponents();
        setDirective(hints);
        setDirectiveParameters(directivePanel.getDirectiveParameters());
    }
    
    public DirectivePanel getDirectivePanel(){
        return directivePanel;
    }
    
    public void setDirective(DirectiveUIHints hints){
        this.hints=hints;
        
        this.directiveLabel.setText(hints.getLabel());
        for(DirectiveUIHints.Constructor c : hints.getConstructors()){
            String label="(";
            boolean first=true;
            for(DirectiveUIHints.Parameter p : c.getParameters()){
                if(!first) label+=",";
                else first=false;
                label+=p.getLabel();
            }
            label+=")";
            constructorComboBox.addItem(new ConstructorComboItem(c,label));
        }
        constructorComboBox.setMinimumSize(new Dimension(50,constructorComboBox.getMinimumSize().height));
        constructorComboBox.setPreferredSize(new Dimension(50,constructorComboBox.getPreferredSize().height));
     
        DirectiveUIHints.Addon[] addons=hints.getAddons();
        if(addons!=null){
            for(DirectiveUIHints.Addon a : addons) {
                String label=a.getLabel()+"(";
                boolean first=true;
                for(DirectiveUIHints.Parameter p : a.getParameters()){
                    if(!first) label+=",";
                    else first=false;
                    label+=p.getLabel();
                }
                label+=")";
                addonComboBox.addItem(new AddonComboItem(a,label));            
            }
        }
        addonComboBox.setMinimumSize(new Dimension(50,addonComboBox.getMinimumSize().height));
        addonComboBox.setPreferredSize(new Dimension(50,addonComboBox.getPreferredSize().height));
    }    
    

    private class ConstructorComboItem {
        public DirectiveUIHints.Constructor c;
        public String label;
        public ConstructorComboItem(DirectiveUIHints.Constructor c,String label){
            this.c=c;
            this.label=label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
    
    private class AddonComboItem {
        public DirectiveUIHints.Addon a;
        public String label;
        public AddonComboItem(DirectiveUIHints.Addon a,String label){
            this.a=a;
            this.label=label;
        }

        @Override
        public String toString() {
            return label;
        }        
    }    
    
    public void saveChanges(){
        if(directivePanel!=null){
            directivePanel.saveDirectiveParameters(getDirectiveParameters());
        }
    }
    
    public void setDirectiveParameters(DirectiveParameters params){
        if(params==null){
            constructorComboBox.setSelectedIndex(0);
            populateParametersPanel(((ConstructorComboItem)constructorComboBox.getItemAt(0)).c);
        }
        else {
            Constructor c=params.constructor;
            for(int i=0;i<constructorComboBox.getItemCount();i++){
                Object o=constructorComboBox.getItemAt(i);
                if(o instanceof ConstructorComboItem){
                    if(((ConstructorComboItem)o).c.equals(c)) {
                        constructorComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }

            this.selectedConstructor=c;
            populateParametersPanel(c);
            
            for(int i=0;i<constructorParamPanels.length;i++){
                AbstractTypePanel panel=constructorParamPanels[i];
                Object value=null;
                if(params.parameters.length>i && params.parameters[i]!=null) value=params.parameters[i].getValue();
                panel.setValue(value);
            }
            
            
            for(AddonParameters ap : params.addons){
                Addon addon=ap.addon;
                
                for(int i=0;i<addonComboBox.getItemCount();i++){
                    Object o=addonComboBox.getItemAt(i);
                    if(o instanceof AddonComboItem){
                        if(((AddonComboItem)o).a.equals(addon)){
                            AddonPanel addonPanel=addAddon(addon);
                            for(int j=0;j<addonPanel.parameterPanels.length;j++){
                                AbstractTypePanel valuePanel=addonPanel.parameterPanels[j];
                                Object value=null;
                                if(j<ap.parameters.length && ap.parameters[j]!=null) {
                                    value=ap.parameters[j].value;
                                }
                                valuePanel.setValue(value);
                            }
                            break;
                        }
                    }
                }
            }
            
        }        
    }
    
    public DirectiveParameters getDirectiveParameters(){
        
        Object o=constructorComboBox.getSelectedItem();
        if(o==null || !(o instanceof ConstructorComboItem)) return null;
        
        Constructor c=((ConstructorComboItem)o).c;
        BoundParameter[] constructorParams=getParameters(constructorParamPanels);
        
        AddonParameters[] addonParams=getAddonParameters();
        
        DirectiveParameters params=new DirectiveParameters(directivePanel.getDirectiveId(),c,constructorParams,addonParams);
        params.from=directivePanel.getFromPanel();
        params.cls=hints.getDirectiveClass().getName();
        
        Rectangle bounds=directivePanel.getBounds();
        params.posx=bounds.x;
        params.posy=bounds.y;
        
        return params;
    }
    
    public AddonParameters[] getAddonParameters(){
        AddonParameters[] ret=new AddonParameters[addonPanels.size()];
        for(int i=0;i<addonPanels.size();i++){
            AddonPanel panel=addonPanels.get(i);
            
            BoundParameter[] params=getParameters(panel.getParameterPanels());
            
            ret[i]=new AddonParameters(panel.getAddon(), params);
        }
        return ret;
    }
    
    public BoundParameter[] getParameters(AbstractTypePanel[] panels){
        BoundParameter[] ret=new BoundParameter[panels.length];
        for(int i=0;i<panels.length;i++){
            AbstractTypePanel paramPanel=panels[i];
            String s=paramPanel.getValueScript();
            
            Parameter param=paramPanel.getParameter();
            Object value=paramPanel.getValue();
            
            ret[i]=new BoundParameter(param, value);
        }
        return ret;
    }
    
    public void setParameters(AbstractTypePanel[] panels, BoundParameter[] values){
        for(int i=0;i<panels.length;i++){
            AbstractTypePanel paramPanel=panels[i];
            paramPanel.setValue(values[i]);
        }        
    }
    
    
    public static class DirectiveParameters {
        public String id;
        public String cls;
        public Constructor constructor;
        public BoundParameter[] parameters;
        public AddonParameters[] addons;
        public int posx;
        public int posy;
        @JsonIgnore
        public Object from; // this can be a DirectivePanel or the id for the directive
        public DirectiveParameters(){}
        public DirectiveParameters(String directiveId,Constructor constructor, BoundParameter[] parameters, AddonParameters[] addons) {
            this.id=directiveId;
            this.constructor = constructor;
            this.parameters = parameters;
            this.addons = addons;
        }

        public Object getFrom() {
            if(from instanceof DirectivePanel) return ((DirectivePanel)from).getDirectiveId();
            else return from;
        }

        public void setFrom(Object from) {
            this.from = from; // DirectivePanel and id mapping handled later when references are fixed
        }
            
        @JsonIgnore
        public DirectivePanel getFromPanel(){
            if(from!=null && from instanceof DirectivePanel) return (DirectivePanel)from;
            else return null;
        }        

        @JsonIgnore
        public void resolveDirectiveValues(Map<String,DirectivePanel> directiveMap){
            for(BoundParameter p : parameters){
                p.resolveDirectiveValues(directiveMap);
            }
            for(AddonParameters a : addons){
                a.resolveDirectiveValues(directiveMap);
            }
            if(from!=null && from instanceof String){
                from=directiveMap.get((String)from);
            }
        }
        
        @JsonIgnore
        public void connectAnchors(DirectivePanel panel){
            for(int i=0;i<parameters.length;i++){
                BoundParameter p=parameters[i];
                p.connectAnchors(panel,(i<10?"0":"")+i);
            }
            for(int i=0;i<addons.length;i++){
                AddonParameters a=addons[i];
                a.connectAnchors(panel,"a"+(i<10?"0":"")+i);
            }
            if(from!=null){
                DirectivePanel p=(DirectivePanel)from;
                p.getFromConnectorAnchor().setTo(panel.getToConnectorAnchor());
            }
        }
        
        @JsonIgnore
        public DirectiveParameters duplicate(){
            BoundParameter[] newParams=new BoundParameter[parameters.length];
            AddonParameters[] newAddons=new AddonParameters[addons.length];
            for(int i=0;i<parameters.length;i++){
                newParams[i]=parameters[i].duplicate();
            }
            for(int i=0;i<addons.length;i++){
                newAddons[i]=addons[i].duplicate();
            }
            
            DirectiveParameters ret=new DirectiveParameters(id, constructor, newParams, newAddons);
            ret.cls=this.cls;
            ret.posx=this.posx;
            ret.posy=this.posy;
            ret.from=this.from;
            return ret;
        }
    }
    
    public static class AddonParameters {
        public Addon addon;
        public BoundParameter[] parameters;
        public AddonParameters(){}
        public AddonParameters(Addon addon, BoundParameter[] parameters) {
            this.addon = addon;
            this.parameters = parameters;
        }
        
        @JsonIgnore
        public void resolveDirectiveValues(Map<String,DirectivePanel> directiveMap){
            for(BoundParameter p : parameters){
                p.resolveDirectiveValues(directiveMap);
            }
        }
        
        @JsonIgnore
        public void connectAnchors(DirectivePanel panel,String orderingHint){
            for(int i=0;i<parameters.length;i++){
                BoundParameter p=parameters[i];
                p.connectAnchors(panel,orderingHint+(i<10?"0":"")+i);
            }            
        }
        
        @JsonIgnore
        public AddonParameters duplicate(){
            BoundParameter[] newParams=new BoundParameter[parameters.length];
            for(int i=0;i<parameters.length;i++){
                newParams[i]=parameters[i].duplicate();
            }
            return new AddonParameters(addon,newParams);
        }
    }

    public static class TypedValue {
        public String parser;
        public Object value;
        public TypedValue[] array;
        public TypedValue(){};
        public TypedValue(String parser, Object value) {
            this.parser = parser;
            this.value = value;
        }
        public TypedValue(TypedValue[] array) {
            this.parser = "array";
            this.array=array;
        }
    }

    public static class BoundParameter {
        @JsonIgnore
        protected Parameter parameter;
        @JsonIgnore
        protected Object value;
        public BoundParameter(){}
        public BoundParameter(Parameter parameter,Object value){
            this.parameter=parameter;
            this.value=value;
        }
        public Parameter getParameter(){return parameter;}
        public void setParameter(Parameter p){this.parameter=p;}
        @JsonIgnore
        public Object getBuildValue(Parameter parameter,Object value,boolean multipleComponent){
            if(value==null) return null;
            if(!multipleComponent && parameter.isMultiple()){
                Object array=Array.newInstance(parameter.getReflectType().getComponentType(), Array.getLength(value));
                
                for(int i=0;i<Array.getLength(value);i++){
                    Object v=Array.get(value, i);
                    Array.set(array, i, getBuildValue(parameter,v,true));
                }
                return array;
            }
            else{
                if(TopicOperand.class.isAssignableFrom(parameter.getType())){
                    if(value instanceof DirectivePanel) return new TopicOperand(((DirectivePanel)value).buildDirective());
                    else return new TopicOperand(value);
                }
                else if(Operand.class.isAssignableFrom(parameter.getType())){
                    if(value instanceof DirectivePanel) return new Operand(((DirectivePanel)value).buildDirective());
                    else return new Operand(value);
                }
                else if(Directive.class.isAssignableFrom(parameter.getType())){
                    return ((DirectivePanel)value).buildDirective();
                }
                else return value;            
            }
        }
        @JsonIgnore
        public Object getBuildValue(){
            return getBuildValue(parameter,value,false);
        }
        @JsonIgnore
        public Object getValue(){
            return value;
        }
        @JsonIgnore
        public String getScriptValue(){
            return getScriptValue(parameter,value,false);
        }
        @JsonIgnore
        public static String escapeString(Object o){
            if(o==null) return "null";
            return "\""+(o.toString().replace("\\","\\\\").replace("\"","\\\""))+"\"";
        }
        
        @JsonIgnore
        private static String getScriptValue(Parameter parameter,Object value,boolean multipleComponent){
            if(value==null) return "null";
            if(!multipleComponent && parameter.isMultiple()){
                
/*              // This is the way you'd create arrays in java usually but
                // Mozilla Rhino doesn't support this. We have to use
                // a helper class to get this done.
                StringBuilder sb=new StringBuilder();
                sb.append("new ");
                sb.append(parameter.getType().getSimpleName());
                sb.append("[]{");
                boolean first=true;
                for(int i=0;i<Array.getLength(value);i++){
                    if(!first) sb.append(", ");
                    else first=false;
                    Object v=Array.get(value, i);
                    sb.append(getScriptValue(parameter,v,true));
                }
                sb.append("}");
                return sb.toString();*/
                
                StringBuilder sb=new StringBuilder();
                sb.append("new org.wandora.utils.ScriptManager.ArrayBuilder(").append(parameter.getType().getName()).append(")");
                for(int i=0;i<Array.getLength(value);i++){
                    Object v=Array.get(value, i);
                    sb.append(".add(");
                    sb.append(getScriptValue(parameter,v,true));
                    sb.append(")");
                }
                sb.append(".finalise()");
                return sb.toString();
            }
            else if(parameter.getType().equals(String.class)) return escapeString(value);
            else if(Number.class.isAssignableFrom(parameter.getType())) return value.toString();
            else if(Topic.class.isAssignableFrom(parameter.getType())) {
                try{
                    return escapeString(((Topic)value).getOneSubjectIdentifier());
                }catch(TopicMapException tme){
                    Wandora.getWandora().handleError(tme);
                    return null;
                }
            }
            else if(TopicOperand.class.isAssignableFrom(parameter.getType())){
                if(value instanceof DirectivePanel) return "new TopicOperand("+((DirectivePanel)value).buildScript()+")";
                else if(value instanceof Topic) {
                    try{
                        return "new TopicOperand("+escapeString(((Topic)value).getOneSubjectIdentifier())+")";
                    }catch(TopicMapException tme){
                        Wandora.getWandora().handleError(tme);
                        return null;
                    }
                }
                else return "new TopicOperand("+escapeString(value)+")"; // Subject identifier string
            }
            else if(Operand.class.isAssignableFrom(parameter.getType())){
                if(value instanceof DirectivePanel) return "new Operand("+((DirectivePanel)value).buildScript()+")";
                else return "new Operand("+escapeString(value)+")";
                
            }
            else if(Directive.class.isAssignableFrom(parameter.getType())){
                return ((DirectivePanel)value).buildScript();
            }
            
            else throw new RuntimeException("Unable to convert value to script");
        }
        
        @JsonIgnore
        protected static TypedValue getJsonValue(Object value,Parameter parameter,boolean multipleComponent){
            if(parameter!=null && parameter.isMultiple() && !multipleComponent){
                TypedValue[] ret=new TypedValue[Array.getLength(value)];
                for(int i=0;i<Array.getLength(value);i++){
                    Object v=Array.get(value,i);
                    ret[i]=getJsonValue(v,parameter,true);
                }
                return new TypedValue(ret);
            }
            else {
                 if(value instanceof DirectivePanel){
                    return new TypedValue("directive",((DirectivePanel)value).getDirectiveId());
                }
                else if(value instanceof Topic){
                    try{
                        return new TypedValue("topic",((Topic)value).getOneSubjectIdentifier().toExternalForm());
                    }catch(TopicMapException tme){Wandora.getWandora().handleError(tme);return null;}
                }
                else if(value instanceof TypedValue){
                    return (TypedValue)value;
                }
                else return new TypedValue("default",value);
            }
        }
        
        @JsonIgnore
        public static Object parseJsonValue(TypedValue value){
            if(value==null) return null;
            switch (value.parser) {
                case "directive":
                    return value;
                case "topic":
                    return value.value;
                case "array":
                    Object[] ret=new Object[Array.getLength(value.array)];
                    for(int i=0;i<Array.getLength(value.array);i++){
                        Object v=Array.get(value.array,i);
                        ret[i]=parseJsonValue((TypedValue)v);
                    }
                    return ret;                        
                case "default":
                    return value.value;
                default:
                    throw new RuntimeException("Unknown type in stored json");
            }
        }
         
        public TypedValue getJsonValue(){
            return getJsonValue(value,parameter,false);

        }
        public void setJsonValue(TypedValue o){
            // Directive references are resolved later in resolveDirectiveValues.
            // Topics can be stored as SI Strings, they are resolved to Topic objects
            // automatically as needed.
            value=parseJsonValue(o);
        }

        @JsonIgnore
        public static Object resolveDirectiveValues(Object value,Parameter parameter,Map<String,DirectivePanel> directiveMap){
            if(value==null) return null;
            if(parameter!=null && parameter.isMultiple()){
                Object[] os=new Object[Array.getLength(value)];
                for(int i=0;i<Array.getLength(value);i++){
                    Object v=Array.get(value,i);
                    os[i]=resolveDirectiveValues(v,null,directiveMap);
                }                    
                return os;
            }
            else {
                if(value instanceof TypedValue){
                    TypedValue pv=(TypedValue)value;
                    switch (pv.parser) {
                        case "directive":
                            return directiveMap.get((String)pv.value);
                        default:
                            throw new RuntimeException("No handling for parameter value type "+pv.parser);
                    }
                    
                }
                else return value;
            }
        }
        
        @JsonIgnore
        public void resolveDirectiveValues(Map<String,DirectivePanel> directiveMap){
            value=resolveDirectiveValues(value,parameter,directiveMap);
            
            /*
            if(parameter.getType().equals(Directive.class)){
                if(value==null) return;
                if(parameter.isMultiple()){
                    Object[] os=new Object[Array.getLength(value)];
                    for(int i=0;i<Array.getLength(value);i++){
                        Object v=Array.get(value,i);
                        if(v==null) os[i]=null;
                        else if(v instanceof String) os[i]=directiveMap.get((String)v);
                        else os[i]=v;
                    }                    
                    value=os;
                }
                else {
                    if(value!=null && value instanceof String)
                        value=directiveMap.get((String)value);
                }
            }   */        
        }
        
        @JsonIgnore
        public void connectAnchors(DirectivePanel panel,String orderingHint){
            if(parameter.isMultiple()){
                for(int i=0;i<Array.getLength(value);i++){
                    Object v=Array.get(value,i);
                    if(v instanceof DirectivePanel)
                        panel.connectParamAnchor( ((DirectivePanel)v).getFromConnectorAnchor(), orderingHint+i);
                }                
            }
            else if(value instanceof DirectivePanel){
                panel.connectParamAnchor( ((DirectivePanel)value).getFromConnectorAnchor(), orderingHint);
            }
        }
        
        @JsonIgnore
        public BoundParameter duplicate(){
            return new BoundParameter(parameter, value);
        }
        

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.parameter);
            hash = 37 * hash + Objects.hashCode(this.value);
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
            final BoundParameter other = (BoundParameter) obj;
            if (!Objects.equals(this.parameter, other.parameter)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }


        
    }    
    
    public static AbstractTypePanel makeMultiplePanel(DirectiveUIHints.Parameter param,Class<? extends AbstractTypePanel> typePanel,String label,DirectivePanel directivePanel){
        MultipleParameterPanel p=new MultipleParameterPanel(param,typePanel,directivePanel);
        p.setLabel(label);
        return p;
    }
    
    public static Class<? extends AbstractTypePanel> getTypePanelClass(DirectiveUIHints.Parameter p){
        Class<?> cls=p.getType();
        if(cls.equals(Integer.class) || cls.equals(Integer.TYPE)) return IntegerParameterPanel.class;
        else if(cls.equals(String.class)) return StringParameterPanel.class;
        else if(cls.equals(TopicOperand.class)) return TopicOperandParameterPanel.class;
        else if(cls.equals(Operand.class)) return OperandParameterPanel.class;
        else if(Directive.class.isAssignableFrom(cls)) return DirectiveParameterPanel.class;
        // this is a guess really
        else if(cls.equals(Object.class)) return TopicOperandParameterPanel.class;

        else return UnknownParameterTypePanel.class;
    }
    
    public static AbstractTypePanel[] populateParametersPanel(JPanel panelContainer,DirectiveUIHints.Parameter[] parameters,AbstractTypePanel[] oldPanels,DirectivePanel directivePanel){
        if(oldPanels!=null){
            for(AbstractTypePanel p : oldPanels){
                p.disconnect();
            }
        }
        panelContainer.removeAll();
        
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.insets=new Insets(5, 5, 0, 5);
        gbc.weightx=1.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        
        
        AbstractTypePanel[] panels=new AbstractTypePanel[parameters.length];
        
        for(int i=0;i<parameters.length;i++){
            DirectiveUIHints.Parameter p=parameters[i];
            Class<? extends AbstractTypePanel> panelCls=getTypePanelClass(p);
            if(panelCls==null) continue;
            
            AbstractTypePanel panel;
            if(p.isMultiple()){
                panel=makeMultiplePanel(p, panelCls, p.getLabel(),directivePanel);
                panel.setOrderingHint("0"+(i<10?"0":"")+i);
            }
            else {
                try{
                    java.lang.reflect.Constructor<? extends AbstractTypePanel> panelConstructor=panelCls.getConstructor(DirectiveUIHints.Parameter.class,DirectivePanel.class);
                    panel=panelConstructor.newInstance(p,directivePanel);
                    panel.setLabel(p.getLabel());
                    panel.setOrderingHint("0"+(i<10?"0":"")+i);
                    
                    if(panel instanceof DirectiveParameterPanel){
                        if(!p.getType().equals(Directive.class)){
                            Class<? extends Directive> cls=(Class<? extends Directive>)p.getType();
                            ((DirectiveParameterPanel)panel).setDirectiveType(cls);
                        }
                    }
                }catch(IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
                    Wandora.getWandora().handleError(e);
                    return null;
                }
            }
            
            panels[i]=panel;
            
            panelContainer.add(panel,gbc);
            gbc.gridy++;
        }
        
        return panels;
    }
    
    protected void populateParametersPanel(DirectiveUIHints.Constructor c){
        if(c==null) return;
        DirectiveUIHints.Parameter[] parameters=c.getParameters();
        this.constructorParamPanels=populateParametersPanel(constructorParameters,parameters,this.constructorParamPanels,directivePanel);
        this.revalidate();
        constructorParameters.repaint();        
        
    }
    
    public AddonPanel addAddon(Addon addon){
        AddonPanel addonPanel=new AddonPanel(this,addon);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx=1.0;
        gbc.gridx=0;
        gbc.gridy=addonPanels.size();
        gbc.insets=new Insets(5, 0, 0, 0);
        addonPanelContainer.add(addonPanel,gbc);

        addonPanels.add(addonPanel);
        return addonPanel;
    }
    
    public void removeAddon(AddonPanel addonPanel){
        synchronized(addonPanels){
            int index=addonPanels.indexOf(addonPanel);
            if(index<0) return;
            addonPanel.disconnect();
            GridBagLayout gbl=(GridBagLayout)addonPanelContainer.getLayout();
            for(int i=index+1;i<addonPanels.size();i++){
                AddonPanel p=addonPanels.get(i);
                GridBagConstraints gbc=gbl.getConstraints(p);
                gbc.gridy--;
                gbl.setConstraints(p, gbc);
            }
            addonPanels.remove(index);
            addonPanelContainer.remove(addonPanel);
        }
        this.revalidate();
        addonPanelContainer.repaint();        
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        directiveLabel = new javax.swing.JLabel();
        deleteButton = new javax.swing.JButton();
        constructorComboBox = new javax.swing.JComboBox();
        constructorParameters = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        addAddonButton = new javax.swing.JButton();
        addonComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        addonPanelContainer = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        directiveLabel.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        directiveLabel.setText("Directive label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(directiveLabel, gridBagConstraints);

        deleteButton.setText("Remove");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(deleteButton, gridBagConstraints);

        constructorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                constructorComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel2.add(constructorComboBox, gridBagConstraints);

        constructorParameters.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        constructorParameters.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(constructorParameters, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        addAddonButton.setText("Add addon");
        addAddonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAddonButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(addAddonButton, gridBagConstraints);

        addonComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addonComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(addonComboBox, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        jLabel2.setText("Addons");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel2.add(jPanel1, gridBagConstraints);

        addonPanelContainer.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel2.add(addonPanelContainer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(fillerPanel, gridBagConstraints);

        jLabel1.setText("Constructor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel2);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void constructorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constructorComboBoxActionPerformed
        Object o=constructorComboBox.getSelectedItem();
        ConstructorComboItem c=(ConstructorComboItem)o;
        this.selectedConstructor=c.c;
        populateParametersPanel(c.c);
    }//GEN-LAST:event_constructorComboBoxActionPerformed

    private void addAddonButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAddonButtonActionPerformed
        synchronized(addonPanels){
            Object o=addonComboBox.getSelectedItem();
            if(o==null || !(o instanceof AddonComboItem)) return;

            AddonComboItem aci=(AddonComboItem)o;
            Addon addon=aci.a;

            addAddon(addon);
        }
        this.revalidate();
        addonPanelContainer.repaint();

    }//GEN-LAST:event_addAddonButtonActionPerformed

    private void addonComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addonComboBoxActionPerformed

    }//GEN-LAST:event_addonComboBoxActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if(directivePanel==null) return;
        
        QueryEditorComponent editor=directivePanel.getEditor();
        if(editor==null) return;
        
        editor.removeDirective(directivePanel);
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAddonButton;
    private javax.swing.JComboBox addonComboBox;
    private javax.swing.JPanel addonPanelContainer;
    private javax.swing.JComboBox constructorComboBox;
    private javax.swing.JPanel constructorParameters;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel directiveLabel;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
