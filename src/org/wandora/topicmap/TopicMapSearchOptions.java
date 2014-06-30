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
 * TopicMapSearchOptions.java
 *
 * Created on 24. helmikuuta 2006, 20:24
 *
 */

package org.wandora.topicmap;





/**
 *
 * @author akivela
 */
public class TopicMapSearchOptions {
    

    public boolean searchBasenames = false;
    public boolean searchVariants = false;
    public boolean searchOccurrences = false;
    public boolean searchSIs = false;
    public boolean searchSL = false;
    
    // different topic map implementations may or may not respect this,
    // so if you absolutely don't want more results, do check the size
    // of the final result yourself
    public int maxResults=-1;

    
    public TopicMapSearchOptions(boolean searchBasenames, boolean searchVariants, boolean searchOccurrences, boolean searchSIs, boolean searchSL) {
        this(searchBasenames, searchVariants, searchOccurrences, searchSIs, searchSL, -1);
    }
    public TopicMapSearchOptions(boolean searchBasenames, boolean searchVariants, boolean searchOccurrences, boolean searchSIs, boolean searchSL,int maxResults) {
        this.searchBasenames = searchBasenames;
        this.searchVariants = searchVariants;
        this.searchOccurrences = searchOccurrences;
        this.searchSIs = searchSIs;
        this.searchSL = searchSL;
        this.maxResults=maxResults;
    }
    public TopicMapSearchOptions() {
        this.searchBasenames = true;
        this.searchVariants = true;
        this.searchOccurrences = true;
        this.searchSIs = true;
        this.searchSL = true;
        this.maxResults = -1;
    }

    public TopicMapSearchOptions duplicate(){
        return new TopicMapSearchOptions(searchBasenames, searchVariants, searchOccurrences, searchSIs, searchSL, maxResults);
    }
}
