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
 * 
 *
 * TextBox.java
 *
 * Created on July 13, 2004, 9:17 AM
 */

package org.wandora.modules.velocityhelpers;


import java.util.*;


/**
 *
 * @author  olli
 */
public class TextBox {
    
    public static final String PROP_SHORTNAMELENGTH="textbox.shortnamelength";
    
    private int shortNameLength;
    
    public TextBox(){
        this(new Properties());
    }
    
    public TextBox(int shortNameLength){
        this.shortNameLength=shortNameLength;
    }
    
    /** Creates a new instance of TextBox */
    public TextBox(Properties properties) {
        shortNameLength=30;
        if(properties.getProperty(PROP_SHORTNAMELENGTH)!=null)
            shortNameLength=Integer.parseInt(properties.getProperty(PROP_SHORTNAMELENGTH));
    }
    
    
    public String shortenName(String name){
        if(name.length()<=shortNameLength) return name;
        else return name.substring(0,shortNameLength/2-2)+"..."+name.substring(name.length()-shortNameLength/2-2);
    }
    
    public String charToStr(int c){
        return Character.valueOf((char)c).toString();
    }
    public String repeat(String s,int times){
        String r="";
        for(int i=0;i<times;i++){
            r+=s;
        }
        return r;
    }
}
