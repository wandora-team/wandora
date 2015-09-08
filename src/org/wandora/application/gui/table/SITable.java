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
 * SITable.java
 *
 * Created on August 18, 2004, 8:53 AM
 */

package org.wandora.application.gui.table;


import org.wandora.topicmap.*;
import java.awt.*;

import org.wandora.application.*;


/**
 *
 * @author  olli, akivela
 */
public class SITable extends LocatorTable {

    private Topic topic;
    private Locator[] sis;
    private Color[] colors;

    
    
    /** Creates a new instance of SITable */
    public SITable(Topic topic, Wandora w) throws TopicMapException {
        super(w);
        this.topic=topic;
        sis=(Locator[])topic.getSubjectIdentifiers().toArray(new Locator[0]);
        colors=new Color[sis.length];
        for(int i=0;i<colors.length;i++){
            colors[i]=w.topicHilights.getSIColor(topic,sis[i]);
        }
        initialize(sis, "Subject identifiers", colors);
    }
    
    

    @Override
    public Object[] getHeaderPopupStruct() {
        return WandoraMenuManager.getSubjectIdentifierTablePopupStruct();
    }
    
    
    @Override
    public Object[] getPopupStruct() {
        return WandoraMenuManager.getSubjectIdentifierTablePopupStruct();
    }
    

    @Override
    public void processDrop(String data) {
        boolean siAdded = false;
        //System.out.println("data == " + data);
        if(data.startsWith("file:")) {
            try {
                topic.addSubjectIdentifier(new Locator(data));
                siAdded = true;
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
        else if(data.startsWith("data:")) {
            try {
                topic.addSubjectIdentifier(new Locator(data));
                siAdded = true;
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
        else {
            while(data.length() > 0) {
                int l = data.indexOf("http:");
                if(l == -1) l = data.indexOf("https:");
                if(l == -1) l = data.indexOf("ftp:");
                if(l == -1) l = data.indexOf("ftps:");
                if(l == -1) l = data.indexOf("mailto:");
                if(l == -1) l = data.indexOf("gopher:");
                if(l == -1) l = data.indexOf("file:");
                if(l > -1) {
                    String url = data.substring(l);
                    // System.out.println("url == " + url);
                    data = "";
                    int j = url.indexOf(' ');
                    if(j == -1) j = url.indexOf('\n');
                    if(j > -1) {
                        data = url.substring(j);
                        url = url.substring(0, j);
                        // System.out.println("url == " + url);
                    }
                    try {
                        topic.addSubjectIdentifier(new Locator(url.trim()));
                        siAdded = true;
                    }
                    catch(Exception e) {
                        wandora.handleError(e);
                    }
                }
                else {
                    break;
                }
            }
        }
        if(siAdded) {
            wandora.reopenTopic();
        }
    }
}
