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
 * RandomNameGenerator.java
 *
 * Created on September 20, 2004, 1:34 PM
 */

package org.wandora.application.tools.fng;


import org.wandora.utils.IObox;

import org.wandora.utils.*;

/**
 *
 * @author  akivela
 */
public class RandomNameGenerator {
    
    /** Creates a new instance of RandomNameGenerator */
    public RandomNameGenerator() {
    }
    
    
     public static void main(String args[]) throws Exception {       
        StringBuffer sb = new StringBuffer("");
        String postfix = "@kiasmail.info\n";
        String prefix = "user";
        String name = "";
        for(int i=1; i<=100; i++) {
            name = "" + i;
            int l = name.length();
            for(int j=5; j>l; j--) {
                name = "0" + name;
            }
            sb.append(prefix + name + postfix);
        }
        IObox.saveFile("C:\\emails_100.txt", sb.toString());
    }
}
