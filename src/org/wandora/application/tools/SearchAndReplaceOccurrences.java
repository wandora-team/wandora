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
 * 
 * */

package org.wandora.application.tools;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;
import java.util.regex.*;




public class SearchAndReplaceOccurrences extends AbstractWandoraTool {
    
    public SearchAndReplaceOccurrences() {
        this.setContext(new TopicContext());
    }
    
    @Override
    public void execute(final Wandora admin, Context context) {
        String search=WandoraOptionPane.showInputDialog(admin,"Enter search string","","Search and replace");
        if(search==null || search.length()==0) return;
        String replace=WandoraOptionPane.showInputDialog(admin,"Enter replace string","","Search and replace");
        if(replace==null) return;
        Iterator iter=context.getContextObjects();
        Pattern pattern;
        try{
            pattern=Pattern.compile(search);
        }catch(PatternSyntaxException pse){
            singleLog(pse);
            return;
        }
        int counter=0;
        while(iter.hasNext()){
            counter++;
            if((counter%100)==0){
                if(forceStop()){
                    singleLog("Aborting execute");
                    return;
                }
            }
            
            Object cObject=iter.next();
            if(!(cObject instanceof Topic)) continue;
            Topic cTopic=(Topic)cObject;
            try{
                for(Topic type : cTopic.getDataTypes()){
                    Hashtable<Topic,String> data=cTopic.getData(type);
                    for(Topic version : data.keySet()){
                        String occurrence=data.get(version);
                        Matcher matcher=pattern.matcher(occurrence);
                        occurrence=matcher.replaceAll(replace);
                        cTopic.setData(type,version,occurrence);
                    }
                }
            }
            catch(TopicMapException tme){
                singleLog(tme);
                return;
            }
        }
    }


    @Override
    public String getName() {
        return "Search and replace in occurrences";
    }

    @Override
    public String getDescription() {
        return "Tool searches for matches of a regular expression in topic occurrences and replaces them with another string. "+
               "Replacement is done with the Java java.util.regex.Matcher.replaceAll method which allows you to refer to mathed "+
               "subsequences with with a dollar sign ($). Backslash (\\) can be used to escape literal characters, especially the "+
               "dollar sign and backslash itself.";
    }
    
}

