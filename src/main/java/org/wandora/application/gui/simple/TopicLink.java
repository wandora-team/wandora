/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * TopicLink.java
 *
 * Created on June 14, 2004, 9:32 AM
 */

package org.wandora.application.gui.simple;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.topicstringify.TopicToString;



/**
 *
 * @author  olli, ak
 */
public class TopicLink extends TopicLinkBasename {

    private boolean limitLength = true;
    private String lname = "";
    private String sname = "";
    
    
    
    /** Creates a new instance of TopicLink */
    public TopicLink(Topic t, Wandora wandora) {
        super(t, wandora);
        setText(t);
    }




    public void setLimitLength(boolean limit) {
        limitLength = limit;
        if(limitLength)
            this.setText(sname);
        else
            this.setText(lname);
        this.setVisible(true);
    }
    



    public void setText(Topic t) {
        lname = TopicToString.toString(t);
        if(lname.length() > 30)
            sname = lname.substring(0,13)+"..."+lname.substring(lname.length()-13);
        else
            sname = lname;
        if(limitLength)
            this.setText(sname);
        else
            this.setText(lname);
        this.setVisible(true);
    }
        
    

    
    

    
}
