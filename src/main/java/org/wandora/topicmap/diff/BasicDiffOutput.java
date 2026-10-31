/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 */

package org.wandora.topicmap.diff;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.diff.TopicMapDiff.DiffEntry;
import org.wandora.utils.logger.Log4j2Logger;
/**
 *
 * @author olli
 */
public class BasicDiffOutput implements DiffOutput {
	private static final Log4j2Logger logger = Log4j2Logger.getLogger(BasicDiffOutput.class);
	
    protected DiffEntryFormatter formatter;
    protected Writer writer;
    
    
    public BasicDiffOutput(DiffEntryFormatter formatter,Writer writer){
        this.formatter=formatter;
        this.writer=writer;
    }
    
    protected void doOutput(DiffEntry d) throws IOException,TopicMapException{
        this.formatter.formatDiffEntry(d, writer);        
    }
    
    public void startCompare(){
        try{
            this.formatter.header(writer);
        }
        catch(IOException ioe){
        	logger.error(ioe);
        }
        catch(TopicMapException tme){
        	logger.error(tme);
        }
    }
    
    public void endCompare(){
        try{
            this.formatter.footer(writer);
        }
        catch(IOException ioe){
        	logger.error(ioe);
        }
        catch(TopicMapException tme){
        	logger.error(tme);
        }
    }
    
    public boolean outputDiffEntry(DiffEntry d) {
        try{
            doOutput(d);
        }
        catch(IOException ioe){
        	logger.error(ioe);
            return false;
        }
        catch(TopicMapException tme){
        	logger.error(tme);
            return false;
        }
        return true;
    }
    
    public boolean noDifferences(Topic t){
        return true;
    }
    
    public boolean noDifferences(Association a){
        return true;
    }
    
    public void outputDiff(ArrayList<DiffEntry> diff) {
        for(DiffEntry d : diff){
            outputDiffEntry(d);
        }
    }
}
