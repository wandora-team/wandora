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
 */
package org.wandora.query2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.ScriptManager;

/**
 *
 * This is a utility class to help with running queries read from a query
 * script in a String. The CatchException versions are mostly meant for Velocity
 * where catching exceptions is impossible in the template code. Instead you
 * can just check for an exception in the result after the method finishes.
 * 
 * @author olli
 */


public class QueryRunner {
    protected String scriptEngineName = null;
    static protected ScriptManager scriptManager = new ScriptManager();
    protected ScriptEngine scriptEngine;
    
    /**
     * Initialize the runner with the specified scripting engine. Give the
     * string "none" as scripting engine name to not initialize any engine. In 
     * this case, you may only use the runQuery methods that take a directive
     * directly instead of a query script.
     * 
     * @param scriptEngineName 
     */
    public QueryRunner(String scriptEngineName){
        this.scriptEngineName=scriptEngineName;
        if(this.scriptEngineName==null || !this.scriptEngineName.trim().equals("none")) {
            if(scriptEngineName==null) this.scriptEngine=scriptManager.getScriptEngine(ScriptManager.getDefaultScriptEngine());
            else this.scriptEngine=scriptManager.getScriptEngine(scriptEngineName);
            if(this.scriptEngine==null) throw new RuntimeException("Couldn't find a suitable script engine");
        }
    }
    
    /**
     * Initialize the runner with the default scripting engine.
     */
    public QueryRunner(){
        this(null);
    }
    
    public ArrayList<ResultRow> runQuery(String query, Topic contextTopic) throws ScriptException, QueryException {
        ArrayList<Topic> context=new ArrayList<Topic>();
        context.add(contextTopic);
        return runQuery(query,context);
    }
    public QueryResult runQueryCatchException(String query, Topic contextTopic) {
        try{ return new QueryResult(runQuery(query,contextTopic)); }
        catch(Exception e){ return new QueryResult(e); }
    }
    public ArrayList<ResultRow> runQuery(Directive directive, Topic contextTopic) throws QueryException {
        ArrayList<Topic> context=new ArrayList<Topic>();
        context.add(contextTopic);
        return runQuery(directive,context);
    }
    public QueryResult runQueryCatchException(Directive directive, Topic contextTopic) {
        try{ return new QueryResult(runQuery(directive,contextTopic)); }
        catch(Exception e){ return new QueryResult(e); }
    }
    public ArrayList<ResultRow> runQuery(String query, Collection<Topic> contextTopics) throws ScriptException, QueryException {
        if(this.scriptEngine==null) throw new RuntimeException("No scripting engine initialised");
        
        Directive directive = null;
        Object o=scriptEngine.eval(query);
        if(o==null) o=scriptEngine.get("query");
        if(o!=null && o instanceof Directive) {
            directive = (Directive)o;
        }
        
        if(directive==null) throw new RuntimeException("Couldn't get directive from script");
        
        return runQuery(directive,contextTopics);
    }
    public QueryResult runQueryCatchException(String query, Collection<Topic> contextTopics) {
        try{ return new QueryResult(runQuery(query,contextTopics)); }
        catch(Exception e){ return new QueryResult(e); }
    }
    
    public ArrayList<ResultRow> runQuery(Directive directive, Collection<Topic> contextTopics) throws QueryException {
        TopicMap tm=null;
        ArrayList<ResultRow> context=new ArrayList<ResultRow>();
        for(Topic t : contextTopics){
            if(tm==null) tm=t.getTopicMap();
            context.add(new ResultRow(t));
        }
        
        if(tm==null) return new ArrayList<ResultRow>(); // no topics in contextTopics
        
        QueryContext queryContext=new QueryContext(tm, "en");
        
        if(context.isEmpty()){
            return new ArrayList<ResultRow>();
        }
        else if(context.size()==1){
            return directive.doQuery(queryContext, context.get(0));
        }
        else{
            return directive.from(new Static(context)).doQuery(queryContext, context.get(0));
        }
    }    
    public QueryResult runQueryCatchException(Directive directive,Collection<Topic> contextTopics) {
        try{ return new QueryResult(runQuery(directive,contextTopics)); }
        catch(Exception e){ return new QueryResult(e); }
    }
    
    
    
    
    public static class QueryResult {
        public ArrayList<ResultRow> rows;
        public Throwable exception;
        
        public QueryResult(ArrayList<ResultRow> rows){
            this.rows=rows;
        }
        
        public QueryResult(Throwable exception){
            this.exception=exception;
        }
        
        public ArrayList<ResultRow> getRows() {
            return rows;
        }
        
        public boolean isException() {
            return exception!=null;
        }
        
        public Throwable getException() {
            return exception;
        }
        
        public String getStackTrace() {
            if(exception==null) return "";
            StringWriter sw=new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
        
        public Object[][] getData() {
            String[] columns = getColumns();
            Object[][] data = new Object[rows.size()][columns.length];
            for(int i=0; i<rows.size(); i++){
                ResultRow row=rows.get(i);
                ArrayList<String> roles=row.getRoles();
                for(int j=0; j<columns.length; j++){
                    String r=columns[j];
                    int ind=roles.indexOf(r);
                    if(ind!=-1) data[i][j]=row.getValue(ind);
                    else data[i][j]=null;
                }
            }
            return data;
        }
        
        public String[] getColumns() {
            ArrayList<String> columns=new ArrayList<>();
            for(ResultRow row : rows) {
                for(int i=0;i<row.getNumValues();i++){
                    String l=row.getRole(i);
                    if(!columns.contains(l)) columns.add(l);
                }
            }
            return columns.toArray( new String[] {} );
        }
    }
}
