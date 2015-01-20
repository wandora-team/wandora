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
 * 
 *
 * ExpressionExtractor.java
 *
 * Created on 24. helmikuuta 2003, 16:43
 */

package org.wandora.utils.regexextractor.extractors;


import org.wandora.utils.regexextractor.Toolbox;
import org.wandora.utils.regexextractor.LogWriter;
import java.util.*;
import org.wandora.utils.regexextractor.*;
import gnu.regexp.*;
import java.lang.*;
import java.text.*;


public class ExpressionExtractor {
    
    


    public Vector extractExpressions(String[][][] expressions, String text) {
        Vector results = new Vector();

        if (expressions != null && expressions.length>0 && text != null) {
            results = extractExpressions(expressions[0], text);
            for(int i=1; i<expressions.length; i++) {
                results = extractExpressions(expressions[i], results);
            }
        }
        else {
            LogWriter.println("Illegal parameters for expression extractor.");
        }
        return Toolbox.removeDuplicates(results);
    }
    
    
    
      
    public Vector extractExpressions(String[][] expressions, Vector texts) {
        String text = null;
        Vector results = new Vector();
        
        if (expressions != null && texts != null) {
            for(int i=0; i<texts.size(); i++) {
                try {
                    text = (String) texts.elementAt(i);
                    results.addAll(extractExpressions(expressions, text));
                }
                catch (Exception e) {
                    LogWriter.println("Exception occurred while extracting expression from vector of texts. Text is currently '" + text + "'!");
                }
            }
        }
        else {
            LogWriter.println("Either expression array or text vector is null. Can't extract expressions!");
        }
        return Toolbox.removeDuplicates(results);
    }
    
    
    
    
    public Vector extractExpressions(String[][] expressions, String text) {
        Vector results = new Vector();
        ExpressionRuleParser ruleParser = new ExpressionRuleParser();
        REMatchEnumeration matches = null;
        RE re;
        REMatch match;
        int numberOfMatches;
        
        for(int i=0; i<expressions.length; i++) {
            try {
                // WAS: re = new RE(expressions[i][0];
                re = new RE(expressions[i][0], RE.REG_DOT_NEWLINE);
                matches = re.getMatchEnumeration(text);
                numberOfMatches = 0;
                while (matches.hasMoreMatches()) {
                    match = matches.nextMatch();
                    numberOfMatches++;
                    if (match != null) {
                        results.addAll(ruleParser.parse(text, match, expressions[i][1]));
                    }
                }
                if(numberOfMatches == 0) {
                    if("!".equals(expressions[i][1])) {
                        //LogWriter.println("APP", "Negation of '" + expressions[i][0] + "' matched. Adding '" + text + "'.");
                        results.add(text);
                    }
                }
            }
            catch (Exception e) {
                LogWriter.println("Exception '" + e.toString() + "' occurred while match regular expression!");
            }
        }
        return Toolbox.removeDuplicates(results);
    }
    
    
    
}
