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
 * TopicInUseException.java
 *
 * Created on June 17, 2004, 10:41 AM
 */

package org.wandora.topicmap;

/**
 * An exception thrown when deleting a topic could not be carried out because
 * the topic is used in such a place that it cannot be removed without greatly
 * modifying the topic map.
 *
 * @author  olli
 */
public class TopicInUseException extends TopicMapException {
    public static final int NOREASON=-1;
    public static final int USEDIN_ASSOCIATIONTYPE=0;
    public static final int USEDIN_ASSOCIATIONROLE=1;
    public static final int USEDIN_DATATYPE=2;
    public static final int USEDIN_DATAVERSION=3;
    public static final int USEDIN_VARIANTSCOPE=4;
    public static final int USEDIN_TOPICTYPE=5;
    private Topic t;
    private int reason;
    public TopicInUseException(Topic t){
        this(t,NOREASON);
    }
    public TopicInUseException(Topic t,int reason){
        this.t=t;
        this.reason=reason;
    }
    public Topic getTopic(){
        return t;
    }
    public int getReason(){
        return reason;
    }
}
