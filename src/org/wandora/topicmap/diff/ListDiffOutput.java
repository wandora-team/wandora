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
 */

package org.wandora.topicmap.diff;
import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author olli
 */
public class ListDiffOutput implements DiffOutput {
    private ArrayList<TopicMapDiff.DiffEntry> diff;
    public ListDiffOutput(){
        diff=new ArrayList<TopicMapDiff.DiffEntry>();
    }
    public void startCompare(){}
    public void endCompare(){}
    public boolean outputDiffEntry(TopicMapDiff.DiffEntry d){
        diff.add(d);
        return true;
    }
    public boolean noDifferences(Topic t){
        return true;
    }
    public boolean noDifferences(Association a){
        return true;
    }
    public ArrayList<TopicMapDiff.DiffEntry> getList(){
        return diff;
    }
}
