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
 *
 * BeanShellXMLParam.java
 *
 * Created on 29. marraskuuta 2004, 12:16
 */

package org.wandora.utils;
import bsh.*;
import org.w3c.dom.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class BeanShellXMLParam implements XMLParamAware {
    
    protected Interpreter interpreter;
    protected String getID;
    
    /** Creates a new instance of BeanShellXMLParam */
    public BeanShellXMLParam() {
        interpreter=new Interpreter();
        getID="returnValue";
    }
    
    public BeanShellXMLParam(String src,Map params) throws Exception {
        this(src,params,"returnValue");
    }
    public BeanShellXMLParam(String src,Map params,String id) throws Exception {
        this();
        Iterator iter=params.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry en=(Map.Entry)iter.next();
            interpreter.set((String)en.getKey(),en.getValue());
        }
        interpreter.source(src);
        getID=id;
    }
    
    public Interpreter getInterpreter(){
        return interpreter;
    }
    
    public Object get(){
        return get(getID);
    }
    
    public Object get(String id){
        try{
            return interpreter.get(id);
        }catch(EvalError ee){
            ee.printStackTrace();
            return null;
        }
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        try{
            NodeList nl=element.getChildNodes();
            for(int i=0;i<nl.getLength();i++){
                Node n=nl.item(i);
                if(n instanceof Element){
                    Element e=(Element)n;
                    if(e.getNodeName().equals("objects")){
                        Map m=(Map)processor.createObject(e);
                        Iterator iter=m.entrySet().iterator();
                        while(iter.hasNext()){
                            Map.Entry en=(Map.Entry)iter.next();
                            interpreter.set((String)en.getKey(),en.getValue());
                        }
                    }
                    else if(e.getNodeName().equals("source")){
                        String src=e.getAttribute("src");
                        if(src!=null && src.length()>0){
                            interpreter.source(src);
                        }
                        else{
                            String contents=processor.getElementContents(e);
                            interpreter.eval(contents);
                        }
                    }
                    else if(e.getNodeName().equals("id")){
                        getID=(String)processor.createObject(e);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
