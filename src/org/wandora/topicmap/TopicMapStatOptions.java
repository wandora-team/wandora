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
 * TopicMapStatOptions.java
 *
 * Created on 25. toukokuuta 2006, 18:26
 *
 */

package org.wandora.topicmap;

/**
 *
 * @author akivela
 */
public class TopicMapStatOptions {
    public static final int NUMBER_OF_TOPICS = 1000;
    public static final int NUMBER_OF_ASSOCIATIONS = 1100;
    
    public static final int NUMBER_OF_BASE_NAMES = 2000;
    public static final int NUMBER_OF_SUBJECT_IDENTIFIERS = 2010;
    public static final int NUMBER_OF_SUBJECT_LOCATORS = 2020;
    public static final int NUMBER_OF_OCCURRENCES = 2030;
    public static final int NUMBER_OF_TOPIC_CLASSES = 2040;
    
    public static final int NUMBER_OF_ASSOCIATION_TYPES = 3000;
    public static final int NUMBER_OF_ASSOCIATION_ROLES = 3010;
    public static final int NUMBER_OF_ASSOCIATION_PLAYERS = 3020;
    
    
    // **** REMEMBER TO ADD NEW OPTIONS BELOW TO getAvailableOptions &
    // **** describeStatOption ALSO!!
    
    
    
    private int option = NUMBER_OF_TOPICS;
    
    /** Creates a new instance of TopicMapStatOptions */
    public TopicMapStatOptions() {
    }
    public TopicMapStatOptions(int option) {
        this.option = option;
    }
    
    
    
    public void setOption(int option) {
        this.option = option;
    }
    public int getOption() {
        return this.option;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static int[] getAvailableOptions() {
        return new int[] {
            NUMBER_OF_TOPICS,
            NUMBER_OF_ASSOCIATIONS,

            NUMBER_OF_BASE_NAMES,
            NUMBER_OF_SUBJECT_IDENTIFIERS,
            NUMBER_OF_SUBJECT_LOCATORS,
            NUMBER_OF_OCCURRENCES,
            NUMBER_OF_TOPIC_CLASSES,

            NUMBER_OF_ASSOCIATION_TYPES,
            NUMBER_OF_ASSOCIATION_ROLES,
            NUMBER_OF_ASSOCIATION_PLAYERS,
        };
    }
    
    
    
    public static String describeStatOption(int opt) {
        switch(opt) {
            case NUMBER_OF_TOPICS: {
                return "Number of topics";
            }
            case NUMBER_OF_ASSOCIATIONS: {
                return "Number of associations";
            }
            case NUMBER_OF_ASSOCIATION_PLAYERS: {
                return "Number of distinct players in associations";
            }
            case NUMBER_OF_ASSOCIATION_ROLES: {
                return "Number of distinct roles in associations";
            }
            case NUMBER_OF_ASSOCIATION_TYPES: {
                return "Number of distinct types of associations";
            }
            case NUMBER_OF_BASE_NAMES: {
                return "Number of topic base names";
            }
            case NUMBER_OF_OCCURRENCES: {
                return "Number of occurrences";
            }
            case NUMBER_OF_SUBJECT_IDENTIFIERS: {
                return "Number of subject identifiers";
            }
            case NUMBER_OF_SUBJECT_LOCATORS: {
                return "Number of subject locators";
            }
            case NUMBER_OF_TOPIC_CLASSES: {
                return "Number of distinct topic classes";
            }
        }
        return "Statistics option description not available";
    }
}



