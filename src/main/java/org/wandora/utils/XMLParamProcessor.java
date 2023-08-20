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
 *
 * 
 * XMLParamProcessor.java
 *
 * Created on 1.6.2004, 12:51
 */

package org.wandora.utils;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * <p>
 * Utility to create Java Objects from xml. Recursively creates or retrieves objects from symbol table
 * for constructor and method parameters. With some helper classes inserted into the symbol table beforehand,
 * can be pretty much used to execute arbitrary Java code. Normally parameters to class constructors are
 * parsed recursively from the child elements of the creating element. Your class can however implement 
 * <code>XMLParamAware</code> in which case your class will be provided with the DOM element and you can implement custom
 * parameter parsing. <code>XMLParamAware</code> clasesses are also given this <code>XMLParamProcessor</code> so they have access to the
 * entire symbol table created (at the time of their generation). So parameters can also be passed by putting objects
 * in the symbol table with specific keys. This is especially usefull for objects that are passed to allmost
 * every object (such as loggers; which many classes will try to get from symbol table with key "logger").</p>
 * <p>
 * New objects are usually created by providing the full class name of the created object. You can also map
 * element names to different classes beforehand and then create new objects with these element names.</p>
 * <ul>
 * <li>id attribute specifies the id of the created or returned object in the symbol table. If the symbol table
 * allready contains an object with the given id, it is overwritten.</li>
 * <li>idref attribute specifies the id of the object to be retrieved from the symbol table.</li>
 * <li>class attribute specifies the full class name of the object to be created.</li>
 * <li>array attribute specifies the full class name of the component type of the array to be created.</li>
 * <li>method attribute specifies the name of the method to execute.</li>
 * <li>literal attribute specifies whether the contents of the element should be interpreted as a literal string.
 * Possible values true and false, default is false.</li>
 * </ul></p>
 * <p>
 * Only one of the idref, class, array and static attributes can be specified. This determines how to create or
 * retrieve the object. The value of idref must be the id of the object in the symbol table, the value of the
 * other three must be a full class name. With idref you can use either method or field attribute to invoke a method
 * or get the value of a field respectively and with static you must use one of these.
 * <ul>
 * <li>When class is used, a new object is created and child elements are used as constructor parameters.</li>
 * <li>When array is used, a new array object is created and child elements are used to populate the array.</li>
 * <li>When idref is used, object is retrieved from the symbol table.</li>
 * <li>When static is used, the resulting object is the return value of a static method or the value of a static field.</li>
 * <li>When none of the above are specified
 *    <ol>
 *    <li>If the used element name has been mapped to a class, this mapping will be used to create a new instance of
 *       the class as if class attribute were used. The following mappings are used by default
 *       <ul>
 *          <li>string => java.lang.String</li>
 *          <li>integer => java.lang.Integer</li>
 *          <li>double => java.lang.Double</li>
 *          <li>boolean => java.lang.Boolean</li>
 *          <li>properties => org.wandora.piccolo.XMLProperties</li></ul></li>
 *    <li>If that fails, class attribute is treated to be "java.lang.String" and literal is forced to true.</li>
 *    <ol></li>
 * </ul></p>
 * <p>
 * When you use class attribute to create a new object, note that the class you are creating might implement
 * XMLParamAware, in which case refer to the documentation of that class about how the child elements are used.
 * You must use either method or field attribute when using static.
 * <ul>
 * <li>Using the method attribute will cause the invokation of the specified method with the parameters read from the child elements
 *   except when it is used with class attribute, in which case the method is called without parameters.</li>
 * <li>Using the field attribute will get the specified field of the object.</li>
 * <li>When id attribute is specified, the created object or the object returned by the method call or the object retrieved from the symbol table is
 * assigned into the symbol table with the specified id. Also the used element name is stored and can be retrieved with getObjectType method.</li>
 * <li>literal attribute is used to create String objects from the contents of the element.</li>
 * </ul></p>
 * <p>
 * For example, consider the following xml:
 * <code><pre>
 * &lt;object id="p" class="Store">
 *  &lt;param class="java.lang.String" literal="true">Store name&lt;/param>
 *  &lt;param array="StoreItem">
 *   &lt;item class="StoreItem">
 *    &lt;param class="java.lang.String" literal="true">apple&lt;/param>
 *    &lt;param class="java.lang.Integer" literal="true">5&lt;/param>
 *   &lt;/item>   
 *   &lt;item class="StoreItem">
 *    &lt;String>banana&lt;/String>
 *    &lt;Integer>10&lt;/Integer>
 *   &lt;/item>   
 *  &lt;/param>
 * &lt;/object>
 * &lt;object id="customer" class="Customer"/>
 * &lt;method id="error" idref="p" method="buyOne">
 *  &lt;param idref="customer"/>
 *  &lt;param class="java.lang.String">apple&lt;/param>
 * &lt;/method>
 * </pre></code>
 * <p>
 * This would roughly be equivalent to the following java code:<br>
 * <code><pre>
 * Store p=new Store("Store name",new StoreItem[]{new StoreItem("apple",5),new StoreItem("banana",10)});
 * Customer customer=new Customer();
 * Boolean error=p.buyOne(customer,"apple");
 * </pre></code>
 * Note that the first StoreItem is created using the full syntax and the second item shows how to use the
 * shorter syntax for java.lang.* classes.</p>
 * <p>
 * As another example, the following is pretty much equal to <code>System.out.println("Hello World");</code> (note the use of
 * name space).
 * <code><pre>
 * &lt;root xmlns:xp="http://wandora.org/xmlparamprocessor">
 *  &lt;object xp:id="out" xp:static="java.lang.System" xp:field="out"/>
 *  &lt;method xp:idref="out" xp:method="println">
 *   &lt;param xp:class="java.lang.String" xp:literal="true">Hello World!&lt;/param>
 *  &lt;/method>
 * &lt;/root>
 * </pre></code></p>
 * <p>
 * Note the autoboxing/unboxing of primitive types. However, the symbol table will only contain Objects, that
 * is no primitive types.
 * Also note that String,Integer,Double,Boolean etc all have a constructor accepting a single String parameter
 * thus the creation of Integers with literal attribute set to true works (as does the shorter syntax using Integer
 * element name).
 * Note that arithmetic operations are not directly possible. However you could make an object into the symbol
 * table which provides the necessary methods.</p>
 * <p>
 * To use <code>XMLParamProcessor</code>, first create a new instance of it.
 * Then add whatever objects you want to the symbol table (optional).
 * Then use <code>processElement</code> or one of the create methods to process elements of the XML.
 * Finally you can get objects from the symbol table if you need to.</p>
 * <p>
 * You can use getObjectType to get the element name used to create the object. This way you can easily
 * group your objects into different categories.</p>
 * <p>
 * Note that by default <code>XMLParamProcessor</code> uses a namespace "http://wandora.org/xmlparamprocessor" for
 * all the attributes. So either use setNameSpace method to set the name space to null to disable this or take
 * care that your xml has proper name spaces setup and that the xml implementation supports name spaces.
 * You should use  javax.xml.parsers.DocumentBuilderFactory.setNamespaceAware(true) before creating the
 * document builder used to parse the document.</p>
 *
 * @see XMLParamAware
 * @author  olli
 */
public class XMLParamProcessor {
    
    /**
     * The default namespace URI.
     */
    public static final String DEFAULT_NAMESPACE="http://www.gripstudios.com/xmlparamprocessor";
    
    private HashMap objectTable;
    private HashMap objectTypeTable;
    private String nameSpace;
    private HashMap classMap;
    private HashSet forcedLiterals;
    
    /** Creates a new instance of XMLParamProcessor */
    public XMLParamProcessor() {
        objectTable=new HashMap();
        objectTypeTable=new HashMap();
        classMap=new HashMap();
        nameSpace=DEFAULT_NAMESPACE;
        addObjectToTable("this",this);
        mapClass("integer","java.lang.Integer");
        mapClass("string","java.lang.String");
        mapClass("boolean","java.lang.Boolean");
        mapClass("double","java.lang.Double");
        mapClass("properties","org.wandora.piccolo.XMLProperties");
        forcedLiterals=new HashSet();
        forcedLiterals.add("integer");
        forcedLiterals.add("string");
        forcedLiterals.add("boolean");
        forcedLiterals.add("double");
    }
    
    /**
     * Sets the used name space URI.
     * @param ns The new name space URI or null to disable the use of name spaces.
     */
    public void setNameSpace(String ns){
        nameSpace=ns;
    }
    
    /**
     * Reset the whole symbol table.
     * @param table The new symbol table. Should contain id Strings mapped to the corresponding objects.
     */
    public void setSymbolTable(HashMap table){
        objectTable=table;
    }
    
    /**
     * Map a given node name to a specific class. If className is null, removes the previous association.
     * After a node name N is mapped to class C, an element with node name N will behave as if it had
     * attribute class with value C. Thus if you create many instances of the same class, you can map
     * a specific node name to that class after which you don't need to write the class attribute for
     * every element.
     */
    public void mapClass(String nodeName,String className){
        if(className==null) classMap.remove(nodeName);
        else classMap.put(nodeName,className);
    }
    
    public String getClassMapping(String nodeName){
        return (String)classMap.get(nodeName);
    }
    
    /**
     * Adds (or reassigns) a single object to the symbol table.
     * @param id The id of the object.
     * @param o The object to be assigned with the given id.
     */
    public void addObjectToTable(String id,Object o){
        objectTable.put(id,o);
    }
    
    /**
     * Gets an object from the symbol table.
     * @param id The id of the object to get.
     * @return The object or null if not found (or the object really is null).
     */
    public Object getObject(String id){
        return objectTable.get(id);
    }
    public String getObjectType(String id){
        return (String)objectTypeTable.get(id);
    }
    /**
     * Returns the whole symbol table.
     * @return The symbol table with ids mapped to objects.
     */    
    public HashMap getSymbolTable(){
        return objectTable;
    }
    
    public static Class[] getTypeArray(Object[] params){
        Class[] paramTypes=new Class[params.length];
        for(int i=0;i<paramTypes.length;i++) paramTypes[i]=params[i].getClass();
        return paramTypes;
    }
    
    public static boolean isInstanceOrBoxed(Class type,Object param){
        if(param==null) return !type.isPrimitive();
        if(type.isInstance(param)) return true;
        Class pclass=param.getClass();
        if(type.isPrimitive()){
            if(type.equals(Boolean.TYPE) && pclass.equals(Boolean.class)) return true;
            if(type.equals(Character.TYPE) && pclass.equals(Character.class)) return true;
            if(type.equals(Byte.TYPE) && pclass.equals(Byte.class)) return true;
            if(type.equals(Short.TYPE) && pclass.equals(Short.class)) return true;
            if(type.equals(Integer.TYPE) && pclass.equals(Integer.class)) return true;
            if(type.equals(Long.TYPE) && pclass.equals(Long.class)) return true;
            if(type.equals(Float.TYPE) && pclass.equals(Float.class)) return true;
            if(type.equals(Double.TYPE) && pclass.equals(Double.class)) return true;
        }
        return false;
    }

    public static Constructor findConstructor(Class cls,Object[] params) throws Exception {
        Constructor[] cs=cls.getConstructors();
        Outer: for(int i=0;i<cs.length;i++){
//            if(!cs[i].isAccessible()) continue;
            Class[] types=cs[i].getParameterTypes();
            if(types.length!=params.length) continue;
            for(int j=0;j<types.length;j++){
//                if(!types[j].isInstance(params[j])) continue Outer;
                if(!isInstanceOrBoxed(types[j],params[j])) continue Outer;
            }
            return cs[i];
        }
        return null;
    }
    
    public static Method findMethod(Class cls,String method,Object[] params) throws Exception {
        Method[] ms=cls.getMethods();
        Outer: for(int i=0;i<ms.length;i++){
            if(!ms[i].getName().equals(method)) continue;
//            if(!ms[i].isAccessible()) continue;
            Class[] types=ms[i].getParameterTypes();
            if(types.length!=params.length) continue;
            for(int j=0;j<types.length;j++){
//                if(!types[j].isInstance(params[j])) continue Outer;
                if(!isInstanceOrBoxed(types[j],params[j])) continue Outer;
            }
            return ms[i];
        }
        return null;
    }

    private String getAttribute(Element e,String attr){
        if(nameSpace!=null) return e.getAttributeNS(nameSpace,attr);
        else return e.getAttribute(attr);
    }
    
    public static String getNodeContents(Node n){
        short type=n.getNodeType();
        if(type==Node.TEXT_NODE) return n.getNodeValue();
        else if(type==Node.CDATA_SECTION_NODE) return n.getNodeValue();
        else if(type==Node.ELEMENT_NODE){
            StringBuffer buf=new StringBuffer();
            NodeList nl=n.getChildNodes();
            for(int i=0;i<nl.getLength();i++){
                buf.append(getNodeContents(nl.item(i)));
            }
            return buf.toString();
        }
        else return "";
    }
    
    public static String getElementContents(Element e){
        return getNodeContents(e);
/*        NodeList nl=e.getChildNodes();
        StringBuffer s=new StringBuffer();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            String v=n.getNodeValue();
            if(v!=null) s.append(v);
        }
        return s.toString();*/
    }
    
    /**
     * Interprets one element and returns the created or retrieved object. Also interpretes any parameters
     * (child nodes) the interpration of the actual element might require. Adds objects to symbol table
     * where elements contain id attribute.
     * @param e The element to interpret.
     * @return The created object, the retrieved object or the result of the method call or null if the symbol
     *          table didn't contain the object or the method call returns null.
     */
    public Object createObject(Element e) throws Exception {
        return createObject(e,null);
    }
    public Object createObject(Element e,String forceClass) throws Exception {
        String idref=getAttribute(e,"idref");
        String id=getAttribute(e,"id");
        String array=getAttribute(e,"array");
        String cls=getAttribute(e,"class");
        if(forceClass!=null) cls=forceClass;
        String method=getAttribute(e,"method");
        String field=getAttribute(e,"field");
        String literal=getAttribute(e,"literal");
        String stc=getAttribute(e,"static");
        if(idref.length()>0 || stc.length()>0){
            Object o=null;
            Class c=null;
            if(idref.length()>0){
                if(idref.equals("null")){
                    o=null;
                    c=null;
                }
                else{
                    o=objectTable.get(idref);
                    if(o==null) throw new Exception("invalid object id :\""+idref+"\"");
                    c=o.getClass();
                }
            }
            else{
                c=Class.forName(stc);    
                o=null;
                if(field.length()==0 && method.length()==0) throw new Exception("Can't use static class "+stc+" without filed or method.");
            }
            
            if(field.length()>0){
                Object r=c.getField(field).get(o);
                if(id.length()>0) {
                    objectTable.put(id,r);
                    objectTypeTable.put(id,e.getNodeName());
                }
                return r;
            }
            else if(method.length()>0){
                Object[] params=createArray(e,new Object[0]);
                Method m=findMethod(c,method,params);
                if(m==null) throw new NoSuchMethodException(o.getClass().getName()+"."+method);
                Object r=m.invoke(o,params);
                if(id.length()>0){
                    objectTable.put(id,r);
                    objectTypeTable.put(id,e.getNodeName());
                }
                return r;
            }
            else{
                if(id.length()>0){
                    objectTable.put(id,o);
                    objectTypeTable.put(id,e.getNodeName());
                }
                return o;
            }
        }
        else if(array.length()>0){
            Object arrayType=Array.newInstance(Class.forName(array),0);
            Object a=createArray(e,(Object[])arrayType);
            if(id.length()>0) objectTable.put(id,a);
            return a;
        }
        else{
            if(cls.length()==0){
                String nname=e.getNodeName();
                if(classMap.get(nname)!=null){
                    cls=(String)classMap.get(nname);
                    if(forcedLiterals.contains(nname)) literal="true";
                }
                else{
/*                    nname=nname.toLowerCase();
                    nname=nname.substring(0,1).toUpperCase()+nname.substring(1);
                    try{
                        Class.forName("java.lang."+nname);
                        cls="java.lang."+nname;
                    }catch(ClassNotFoundException ex){
                        cls="java.lang.String";
                    }*/
                    cls="java.lang.String";
                    literal="true";
                }
            }
            boolean aware=false;
            Class c=Class.forName(cls);
            Class[] ints=c.getInterfaces();
            for(int i=0;i<ints.length;i++){
                if(ints[i]==XMLParamAware.class){
                    aware=true;
                    break;
                }
            }
            Object o;
            if(!aware){
                Object[] params;
                if(literal.equalsIgnoreCase("true")){
                    String s=getElementContents(e);
                    params=new Object[]{s};
                }
                else{
                    params=createArray(e,new Object[0]);
                }

                Constructor constructor=findConstructor(c,params);
                if(constructor==null) throw new NoSuchMethodException(cls+".<init>");
                o=constructor.newInstance(params);
            }
            else{
                Constructor constructor=c.getConstructor(new Class[0]);
                if(constructor==null) throw new NoSuchMethodException(cls+".<init>");
                o=constructor.newInstance(new Object[]{});
                ((XMLParamAware)o).xmlParamInitialize(e,this);
            }
            
            if(field.length()>0){
                o=o.getClass().getField(field).get(o);
            }
            else if(method.length()>0){
                Object[] params=new Object[0];
                Method m=findMethod(o.getClass(),method,params);
                if(m==null) throw new NoSuchMethodException(o.getClass().getName()+"."+method);
                o=m.invoke(o,params);
            }            
            
            if(id.length()>0){
                objectTable.put(id,o);
                objectTypeTable.put(id,e.getNodeName());
            }
            return o;
        }
        
    }
    
    /**
     * Creates an array of the child nodes of the given element. This is equivalent to calling
     * createObject for each child element of the given element, and putting the results in an
     * array. The returned array will have the type or arrayType or Object[] if arrayType is null.
     * @param e The element whose child elements are parsed.
     * @param arrayType An object used to determine the type of the returned array. If null, the returned
     *                  array will be of type Object[].
     * @return The created array.
     */
    public Object[] createArray(Element e,Object[] arrayType) throws Exception {
        NodeList nl=e.getChildNodes();
        ArrayList al=new ArrayList();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                al.add(createObject((Element)n));
            }
        }
        if(arrayType==null) return al.toArray();
        else return al.toArray(arrayType);
    }
    
    /**
     * Processes all the child elements of the given element. First (non null) element is returned 
     * and the processing might
     * add objects to the symbol table which can then be retrieved there.
     * @param e The element whose child elements are parsed.
     */
    public Object processElement(Element e) throws Exception {
//        createArray(e,null);
        Object first=null;
        NodeList nl=e.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                if(first==null) first=createObject((Element)n);
                else createObject((Element)n);
            }
        }
        return first;
    }
    
    /**
     * A convenience method to parse a Document from a File. The returned document
     * should support name spaces.
     */
    public static Document parseDocument(String file) throws Exception {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder=factory.newDocumentBuilder();
        return builder.parse(new File(file));        
    }
    /**
     * A convenience method to parse a Document from a File. The returned document
     * should support name spaces.
     */
    public static Document parseDocument(InputStream in) throws Exception {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder=factory.newDocumentBuilder();
        return builder.parse(in);        
    }

    /**
     * A test main method. Parses the file given as the first command line parameter.
     */
    public static void main(String[] args) throws Exception {
        Element doc=parseDocument(args[0]).getDocumentElement();
	XMLParamProcessor processor=new XMLParamProcessor();
        processor.createArray(doc,null);
    }
    
}
