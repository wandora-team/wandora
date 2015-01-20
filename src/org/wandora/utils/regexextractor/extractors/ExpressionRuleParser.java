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
 * ExpressionRuleParser.java
 *
 * Created on 24. helmikuuta 2003, 16:38
 */

package org.wandora.utils.regexextractor.extractors;


import org.wandora.utils.regexextractor.LogWriter;
import org.wandora.utils.Textbox;
import java.util.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.*;
import gnu.regexp.*;
import java.text.*;





public class ExpressionRuleParser {
    
    
    private String charset = null;
    
    
    int counter = 0;
    REMatch match = null;
    String text = null;
    String rule = null;
    
    
    /** Creates a new instance of RuleParser */
    public ExpressionRuleParser() {
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void setCharset(String newCharset) {
        charset = newCharset;
    }
    
    
    public String getCharset() {
        return charset;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public Vector parse(String t, REMatch reMatch, String r) {
        counter = 0;
        rule = r;
        match = reMatch;
        text = t;    
        return parseExpressions();
    }
    
    
    
    
    public Vector parseExpressions() {
        String result = null;
        Vector results = new Vector();

        if (rule.length() > 0) {
            while(counter < rule.length()) {
                try {
                    result = parseExpression();
                    if (result != null && result.length() > 0) {
                        results.add(Textbox.encode(result, charset));
                        // LogWriter.println("A result '" + result + "' found for parse expression rule '" + rule + "'.");
                    }
                }
                catch (Exception e) {
                    LogWriter.println("Exception while parsing rule expression '" + rule + "'.");
                }
            }
        }
        return results;
    }
    
    
    
    public String parseExpression() {
        String value = parseMatchIndex();
        if (counter < rule.length() && rule.charAt(counter) == '+') {
            counter++;
            return value + parseExpression();
        }
        else {
            counter++;
        }
        return value;
    }
    
    

    
    private String parseMatchIndex() {
        int localCounter = counter;
        int index = 0;
        String value = "";
        
        while(localCounter < rule.length() && isDigit(rule.charAt(localCounter))) {
            index = index * 10 + (rule.charAt(localCounter) - '0');
            localCounter++;
        }
        counter = localCounter;
        if (localCounter != 0) {
            try {
                value = text.substring(match.getStartIndex(index), match.getEndIndex(index));
            }
            catch (Exception e) {
                LogWriter.println("Illegal index of sub expression '" + index + "' used in rule!");
            }
        }
        return value;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public boolean isDigit(char c) {
        return Character.isDigit(c);
    }
    
    
    public boolean isSpace(char c) {
        return Character.isSpace(c);
    }
    
    
}
