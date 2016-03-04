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
 * 
 *
 * EasyReplaceExpression.java
 *
 * Created on 11. kesäkuuta 2005, 16:18
 */

package org.wandora.utils;


import java.util.regex.*;


/**
 *
 * @author akivela
 */
public class EasyReplaceExpression {

    
    boolean isCaseInsensitive;
    boolean findInsteadMatch;
    Pattern pattern;
    String replacement;
    String name;
    
    
    
    
    /** Creates a new instance of EasyPattern */
    public EasyReplaceExpression() {
    }
    

    
    
    /** Creates a new instance of KirjavaPattern */
    public EasyReplaceExpression(String n, String ps, String rep, boolean findInstead, boolean caseInsensitive) {
        try {
            name = n;
            isCaseInsensitive = caseInsensitive;
            if(isCaseInsensitive) pattern = Pattern.compile(ps, Pattern.CASE_INSENSITIVE);
            else pattern = Pattern.compile(ps);
            replacement = rep;
            findInsteadMatch = findInstead;
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public EasyReplaceExpression(String n, Pattern p, String rep, boolean findInstead, boolean caseInsensitive) {
        try {
            name = n;
            isCaseInsensitive = caseInsensitive;
            if(isCaseInsensitive) pattern = Pattern.compile(p.toString(), Pattern.CASE_INSENSITIVE);
            else pattern = Pattern.compile(p.toString());
            replacement = rep;
            findInsteadMatch = findInstead;
        }
        catch (Exception e) {
            System.out.println(e);
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
    
    public String getReplacementString() {
        return replacement != null ? replacement : "";
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
