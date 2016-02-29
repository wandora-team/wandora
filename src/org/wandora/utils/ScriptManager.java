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
 */
package org.wandora.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 *
 * @author olli
 */


public class ScriptManager {
    
    private HashMap<String,ScriptEngineFactory> engines;
    
    /** Creates a new instance of ScriptManager */
    public ScriptManager() {
        engines=new LinkedHashMap<String,ScriptEngineFactory>();
    }
    
    /**
     * Tries to match an engine key with an available scripting engine. Returns an
     * integer between 0 and 10. 0 means that engine does not match key at all and
     * 10 is exact match. Other values indicate quality of match, for example same
     * language but different engine name or matching language and engine but different
     * version.
     */
    public int matchEngine(String key, ScriptEngineFactory f) {
        String[] info=key.split("\\s*;\\s*");
        String e=f.getEngineName().replace(";","_");
        if(e==null) e="";
        String l=f.getLanguageName().replace(";","_");
        if(l==null) return 0;
        String v=f.getLanguageVersion().replace(";","_");
        if(v==null) v="0";
        if(l.equals(info[1])){
            int offs=0;
            if(e.equals(info[0])) offs=5;
            
            if(v.equals(info[2])) return 5+offs;
            try{
                if(Double.parseDouble(v)>=Double.parseDouble(info[2])) return 4+offs;
            }catch(NumberFormatException nfe){}
            return offs+1;
        }
        return 0;
    }
    
    public static String getDefaultScriptEngine(){
        return "Oracle Nashhorn ; ECMAScript ; 1.0";
    }
    
    public static String makeEngineKey(ScriptEngineFactory f){
        String e=f.getEngineName().replace(";","_");
        if(e==null) e="";
        String l=f.getLanguageName().replace(";","_");
        if(l==null) l="";
        String v=f.getLanguageVersion().replace(";","_");
        if(v==null) v="";        
        return e+" ; "+l+" ; "+v;
    }
    
    public static ArrayList<String> getAvailableEngines(){
        ArrayList<String> ret=new ArrayList<String>();
        javax.script.ScriptEngineManager manager=new javax.script.ScriptEngineManager();
        List<ScriptEngineFactory> fs=manager.getEngineFactories();
        for(ScriptEngineFactory f : fs){
            ret.add(makeEngineKey(f));
        }        
        return ret;
    }
    
    public ScriptEngine getScriptEngine(String engineInfo){
        ScriptEngineFactory factory=engines.get(engineInfo);
        if(factory==null){
            javax.script.ScriptEngineManager manager=new javax.script.ScriptEngineManager();
            List<ScriptEngineFactory> fs=manager.getEngineFactories();
            int bestScore=0;
            for(ScriptEngineFactory f : fs){
                int s=matchEngine(engineInfo,f);
                if(s==10) {
                    factory=f;
                    break;
                }
                else if(s>bestScore){
                    bestScore=s;
                    factory=f;
                }
            }
            engines.put(engineInfo,factory);
        }
        if(factory==null) return null;
        
        ScriptEngine engine = factory.getScriptEngine();
        
        if(engine != null) {
            try {
                String engineName = factory.getEngineName();
                if(engineName != null && engineName.toLowerCase().contains("nashorn")) {
                    // https://bugs.openjdk.java.net/browse/JDK-8025132
                    engine.eval("load('nashorn:mozilla_compat.js');");
                }
            }
            catch(Exception e) {}
        }
        return engine;
    }
    
    public Object executeScript(String script) throws ScriptException {
        return executeScript(script,getScriptEngine(getDefaultScriptEngine()));
    }
    public Object executeScript(String script, ScriptEngine engine) throws ScriptException {
        Object o=engine.eval(script);
        return o;
    }
    
    /**
     * This helper class is meant for creation of arrays in javascript. Mozilla
     * Rhino engine doesn't have an easy way to dynamically create array objects.
     * This helper class can be used like so:
     * 
     * <code>
     * new ArrayBuilder(java.lang.String.class).add("foo").add("bar").add("qux").finalise()
     * </code>
     * 
     * In actual Rhino code, leave out the .class after java.lang.String.
     */
    public static class ArrayBuilder {
        public Class<?> cls;
        public ArrayList<Object> list;
        
        public ArrayBuilder(Class<?> cls){
            this.cls=cls;
            list=new ArrayList<>();
        }
        
        public ArrayBuilder add(Object o){
            list.add(o);
            return this;
        }
        
        public Object finalise(){
            Object array=java.lang.reflect.Array.newInstance(cls, list.size());
            for(int i=0;i<list.size();i++){
                Object o=list.get(i);
                java.lang.reflect.Array.set(array, i, o);
            }
            return array;
        }
    }
}
