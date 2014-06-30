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
 * CMDParamParser.java
 *
 * Created on 20. lokakuuta 2005, 21:13
 *
 */

package org.wandora.utils;

/**
 *
 * @author olli
 */
public class CMDParamParser {
    

    private String[] args;
    
    public CMDParamParser(String[] args){
        this.args=args;
    }
    
    
    public String getLast() {
        if(args.length > 0) {
            return args[args.length-1];
        }
        else return null;
    }
    
    
    public String get(String param){
        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("-")){
                if(args[i].substring(1).equals(param) && i+1<args.length){
                    return args[i+1];
                }
                i++;
            }
        }
        return null;        
    }
    
    
    public boolean isSet(String param){
        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("-")){
                if(args[i].substring(1).equals(param)){
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public String get(String param,String def){
        String p=get(param);
        if(p==null) return def;
        else return p;
    }
    
    
    public Object ifSet(String param,Object ifset,Object otherwise){
        if(isSet(param)) return ifset;
        else return otherwise;
    }
}
