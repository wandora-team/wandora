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
 */
package org.wandora.topicmap;

import de.topicmapslab.tmql4j.components.processor.results.model.IResult;
import de.topicmapslab.tmql4j.components.processor.results.model.IResultSet;
import de.topicmapslab.tmql4j.components.processor.runtime.ITMQLRuntime;
import de.topicmapslab.tmql4j.components.processor.runtime.TMQLRuntimeFactory;
import de.topicmapslab.tmql4j.path.components.processor.runtime.TmqlRuntime2007;
import de.topicmapslab.tmql4j.query.IQuery;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import org.tmapi.core.DatatypeAware;
import org.tmapi.core.Name;
import org.wandora.application.gui.table.MixedTopicTable;
import static org.wandora.application.server.topicmapservice.TopicMapService.wandora;
import org.wandora.topicmap.wandora2tmapi.W2TRole;
import org.wandora.topicmap.wandora2tmapi.W2TTopic;

/**
 * A helper class used to run TMQL queries. Can be used in Java code that
 * need a simple mechanism for tmql or in a Velocity template. The CatchException
 * version of runTMQL is meant for Velocity where catching exceptions is impossible
 * in the template code. Instead you can use that version and then check for the
 * exception later.
 *
 * @author olli
 */


public class TMQLRunner {
    
    public TMQLRunner() {}
    

    public static TMQLResult runTMQL(TopicMap topicMap, String query) throws TopicMapException {
        return runTMQL(topicMap, query, "TMQL-2010");
    }
    
    public static TMQLResult runTMQL(TopicMap topicMap, String query, String runtime) throws TopicMapException {
        ITMQLRuntime tmql = TMQLRuntimeFactory.newFactory().newRuntime(runtime);
        //ITMQLRuntime tmql = TMQLRuntimeFactory.newFactory().newRuntime();
        //ITMQLRuntime tmql = new TmqlRuntime2007();
        
        System.out.println(tmql.getLanguageName());
        System.out.println(tmql.getTmqlProcessor().toString());
        
        org.tmapi.core.TopicMap tm;

        tm = new org.wandora.topicmap.wandora2tmapi.W2TTopicMap(topicMap);

        IQuery tmqlQuery = tmql.run(tm,query);
        IResultSet<?> resultSet = tmqlQuery.getResults();

        if(resultSet.isEmpty()) {
            System.out.println("TMQL resultset is empty.");
            return new TMQLResult(new Object[0][0],new String[0]);
        }

        IResult firstRow = resultSet.get(0);
        int rowSize = firstRow.size();
        String[] columns = new String[rowSize];        
        for(int i=0; i<rowSize; i++) {
            String label=resultSet.getAlias(i);
            if(label==null) label=""+i;
            columns[i]=label;
        }

        Object[][] data = new Object[resultSet.size()][rowSize];
        for(int i=0; i<resultSet.size(); i++) {
            IResult row=resultSet.get(i);
            for(int j=0; j<rowSize; j++) {
                Object o=row.get(j);
                if(o instanceof W2TTopic) o=((W2TTopic)o).getWrapped();
                else if(o instanceof DatatypeAware) o=((DatatypeAware)o).getValue();
                else if(o instanceof Name) o=((Name)o).getValue();
                else if(o instanceof W2TRole) o=((W2TTopic) ((W2TRole)o).getPlayer() ).getWrapped();
                data[i][j]=o;
            }
        }

        return new TMQLResult(data,columns);
    }
    
    
    public static TMQLResult runTMQLCatchException(TopicMap topicMap, String query) {
        try { 
            return runTMQL(topicMap, query); 
        }
        catch(Exception e){ 
            return new TMQLResult(e);
        }
        catch(Error er) {
            return new TMQLResult(er);
        }
    }
    
            
    public static TMQLResult runTMQLCatchException(TopicMap topicMap, String query, String runtime) {
        try { 
            return runTMQL(topicMap, query, runtime); 
        }
        catch(Exception e){ 
            return new TMQLResult(e);
        }
        catch(Error er) {
            return new TMQLResult(er);
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static class TMQLResult {
        Object[][] data;
        String[] columns;
        Throwable exception;
        public TMQLResult(Object[][] data,String[] columns){
            this.data=data;
            this.columns=columns;
        }
        public TMQLResult(Throwable exception){
            this.exception=exception;
        }
        public Object[][] getData(){return data;}
        public String[] getColumns(){return columns;}
        public Object getData(int row,int column){return data[row][column];}
        public String getColumnName(int index){return columns[index];}
        public int getNumRows(){return data.length;}
        public int getNumColumns(){return columns.length;}
        public boolean isException(){return exception!=null;}
        public Throwable getException(){return exception;}
        public String getStackTrace(){
            if(exception==null) return "";
            StringWriter sw=new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }
}
