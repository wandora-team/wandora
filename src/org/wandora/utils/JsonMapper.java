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

package org.wandora.utils;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.*;
/**
 * Transform java objects to JSON and vice versa. To get the JSON simply
 * use the writeValue method. If you don't want some fields in the java class
 * to be included, use the @JsonIgnore annotation.
 * 
 * To parse JSON into java, use the readValue method with the java class you want
 * the results in. The Java class must have an anonymous constructor and suitable
 * getters/setters or public fields.
 * 
 * 
 * @author olli
 */
public class JsonMapper extends ObjectMapper {
    public JsonMapper(){
        super();
    }

    public String writeValue(Object value){
        try{
            StringWriter sw=new StringWriter();
            writeValue(sw,value);
            return sw.toString();
        }catch(IOException ioe){ioe.printStackTrace();}
        return null;
    }
    
    
}
