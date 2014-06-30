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
 */


package org.wandora.topicmap.similarity;

import org.wandora.topicmap.Topic;


/**
 * TopicSimilarity is an interface to measure topic similarity. Interface
 * consists of two methods. The similarity method takes two topics as arguments
 * and returns a double number. If returned double number is zero, topics are
 * identical (in the similarity model). Nonzero values suggest the topics
 * are different. Second interface method is used to return a name for the
 * similarity measure.
 * 
 * @author akivela
 */


public interface TopicSimilarity {
    
    public double similarity(Topic t1, Topic t2);
    public String getName();
    
}
