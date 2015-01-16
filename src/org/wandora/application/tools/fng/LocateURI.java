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
 * LocateURI.java
 *
 * Created on 15. toukokuuta 2006, 10:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.application.tools.fng;

import org.wandora.utils.IObox;
import org.wandora.topicmap.Locator;
import org.wandora.application.contexts.Context;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.*;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.utils.*;
import org.wandora.utils.*;
import java.util.*;
import java.text.*;
import java.lang.*;

/**
 *
 * @author akivela
 */
public class LocateURI extends AbstractWandoraTool implements WandoraTool {
    Wandora admin = null;
    
    
    
    
    /** Creates a new instance of LocateURI */
    public LocateURI() {
    }
        
        
    public String getName() {
        return "Locate URIs";
    }
    
    
    public void execute(Wandora admin, Context context) {      
        try {
            log("Processing URI...");
            locateURI("D:\\projects\\Wandora", ".*snippet\\.txt");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
  
    
    
    public Locator locateURI(String base, String fileName) {
        Locator l = null;
        String f = IObox.findFile(base, fileName, 4);
        System.out.println("FILE FOUND: " + f);
        if(f != null) return new Locator(f);
        else return null;
    }
}
