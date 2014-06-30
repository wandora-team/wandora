/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
 * Eval.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;
import javax.script.*;
import org.wandora.application.WandoraScriptManager;
/**
 *
 * @author olli
 */
public class Eval extends Directive {

    protected String script;
    protected ScriptEngine scriptEngine;
    protected CompiledScript compiled;
    protected HashMap<String,Object> objects;
    protected Object constructorParam;

    public Eval(){
    }

    public Eval(String script,Object param){
        this.script=script;
        this.constructorParam=param;
        scriptEngine=new WandoraScriptManager().getScriptEngine(WandoraScriptManager.getDefaultScriptEngine());
    }
    public Eval(String script){
        this(script,null);
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        compiled=null;
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        objects=new HashMap<String,Object>();
        if(scriptEngine instanceof Compilable){
            try{
                compiled=((Compilable)scriptEngine).compile(script);
                return true;
            }catch(ScriptException se){
                throw new QueryException(se);
            }
        }
        else return true;
    }

    @Override
    public String debugStringParams() {
        return "\""+script.replace("\\", "\\\\").replace("\"","\\\"")+"\"";
    }



    protected ResultRow input;
    protected QueryContext context;
    public Object eval(Object val) throws QueryException {
        return val;
    }

    public Object get(String key){return objects.get(key);}
    public Object set(String key,Object value){return objects.put(key,value);}
    public ResultIterator empty(){return new ResultIterator.EmptyIterator();}

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        Object o=null;
        if(script!=null){

            Bindings bindings=scriptEngine.createBindings();
            bindings.put("input", input);
            bindings.put("context", context);
            bindings.put("val", input.getActiveValue());
            bindings.put("dir", this);
            bindings.put("param",constructorParam);
            try{
                if(compiled!=null) o=compiled.eval(bindings);
                else o=scriptEngine.eval(script, bindings);
            }catch(ScriptException se){
                throw new QueryException(se);
            }
        }
        else{
            this.input=input;
            this.context=context;
            o=eval(input.getActiveValue());
        }

        if(o!=null && o instanceof ResultIterator){
            return (ResultIterator)o;
        }
        else if(o!=null && o instanceof ResultRow){
            return ((ResultRow)o).toIterator();
        }
        else if(o!=null && o instanceof ArrayList){
            return new ResultIterator.ListIterator((ArrayList<ResultRow>)o);
        }
        else {
            return input.addValue(DEFAULT_COL, o).toIterator();
        }
    }

}
