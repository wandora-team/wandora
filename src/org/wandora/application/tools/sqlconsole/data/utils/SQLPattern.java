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
 * SQLPattern.java
 *
 * Created on 5. joulukuuta 2004, 14:14
 */

package org.wandora.application.tools.sqlconsole.data.utils;


import java.util.regex.*;





/**
 *
 * @author  akivela, olli
 */
public class SQLPattern {
    
    boolean isCaseInsensitive;
    boolean findInsteadMatch;
    Pattern pattern;
    String name;
    
    
    
    /** Creates a new instance of KirjavaPattern
     * 
     * @param n 
     * @param ps 
     * @param findInstead 
     * @param caseInsensitive 
     */
    public SQLPattern(String n, String ps, boolean findInstead, boolean caseInsensitive) {
        try {
            name = n;
            isCaseInsensitive = caseInsensitive;
            if(isCaseInsensitive) pattern = Pattern.compile(ps, Pattern.CASE_INSENSITIVE);
            else pattern = Pattern.compile(ps);
            findInsteadMatch = findInstead;
        }
        catch (Exception e) {
            //Logger.println(e);
        }
    }
    
    public SQLPattern(String n, Pattern p, boolean findInstead, boolean caseInsensitive) {
        try {
            name = n;
            isCaseInsensitive = caseInsensitive;
            if(isCaseInsensitive) pattern = Pattern.compile(p.toString(), Pattern.CASE_INSENSITIVE);
            else pattern = Pattern.compile(p.toString());
            findInsteadMatch = findInstead;
        }
        catch (Exception e) {
            //Logger.println(e);
        }
    }
    
    
    public boolean matches(String s) {
        if(pattern != null && s != null) {
            if(findInsteadMatch) return pattern.matcher(s).find();
            else return pattern.matcher(s).matches();
        }
        return false;
    }
    
    
    public Pattern getPattern() {
        return pattern;
    }
    
    
    public String getPatternString() {
        if(pattern != null) return pattern.toString();
        return "";
    }
    
    
    public boolean findInsteadMatch() {
        return findInsteadMatch;
    }
    
    public boolean isCaseInsensitive() {
        return isCaseInsensitive;
    }
    
    public String getName() {
        return name;
    }
    
}
