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
 * WandoraCodec.java
 *
 * Created on 1. huhtikuuta 2003, 19:01
 */

package org.wandora.utils.regexextractor.codecs;



import org.wandora.utils.IObox;
import org.wandora.utils.Textbox;
import org.wandora.utils.Rexbox;
import org.wandora.utils.regexextractor.*;
import java.net.*;
import java.util.*;
import org.wandora.utils.*;



public class WandoraCodec {

    public WandoraCodec() {
    }
    
    
    public String encodeURL(String s) {
        return java.net.URLEncoder.encode(s);
    }
    
    
    
    public String buildURL(String base, String file, String appendix) {
        if(base != null && file != null && appendix != null) {
            try {
                String urlString = null;
                URL url = null;
                if(base.charAt(base.length()-1) != '/') base = base + "/";
                if(appendix.charAt(0) != '.') appendix = "." + appendix;
                urlString = base+file+appendix;
                url = new URL(urlString);
                if (IObox.urlExists(url)) {
                    return urlString;
                }
                else {
                    urlString = base+file+appendix.toUpperCase();
                    url = new URL(urlString);
                    if (IObox.urlExists(url)) {
                        return urlString;
                    }
                }
            }
            catch (Exception e) {}
        }
        return null;
    }
    
    
    
    public String encodeText(String s) {
        String newString = "";
        String subString = "";
        if(s != null && s.length() > 0) {
            try {
                for(int i=0; i<s.length(); i++) {
                    subString = s.substring(i,i+1);
                    newString = newString + new String(subString.getBytes(), "UTF-8");
                }
            }
            catch(Exception e) {}
        }
        return encodeXML(newString);
    }
    
    
    
    
    public String encodeHTML(String s) {
        /*
        s = Rexbox.replace(s, "å", "&aring;");
        s = Rexbox.replace(s, "ä", "&auml;");
        s = Rexbox.replace(s, "ö", "&ouml;");
        s = Rexbox.replace(s, "Å", "&Aring;");
        s = Rexbox.replace(s, "Ä", "&Auml;");
        s = Rexbox.replace(s, "Ö", "&Ouml;");
        s = Rexbox.replace(s, new String(new byte[] { 12, 23 }), "&Ouml;");
         **/
        return s;
    }
    
    
    
    
    public String encodeXML(String s) {
        return Textbox.encodeXML(s);
    }

    
    
    
    public String text2HTML(String s) {
        s = Rexbox.replace(s, "Ty.el.m..n\\s+sijoittuminen\\s*(\\n\\r?\\s*)+", "<p class=\"occurrencetitle\">Työelämään sijoittuminen</p>");
        s = Rexbox.replace(s, "\\n\\r", "<br>");
        s = Rexbox.replace(s, "\\n", "<br>");
        return s;
    }
    
    
    public String toLowerCase(String s) {
        return s.toLowerCase();
    }

    public String toUpperCase(String s) {
        return s.toUpperCase();
    }
    
    public String toTitleCase(String s) {
        if(s != null && s.length()>0) {
            s = s.substring(0,1).toUpperCase() + s.substring(1);
        }
        return s;
    }
    
    
    public String limitLength(String s, int limit) {
        if(s != null && s.length()>limit) {
            int originalLimit = limit;
            while(limit > 1 && s.charAt(limit)!=' ') limit--;
            if(limit<5) limit = originalLimit;
            s = s.substring(0,limit) + "...";
        }
        return s;
    }
    
    
    
    public String sortNameForNumber(String number, int digits) {
        String sortName = number;
        while(sortName.length() < digits) {
            sortName = "0" + sortName;
        }
        return sortName;
    }
    
    
    
    public String encodeProfession(String profession) {
        Rexbox.replace(profession, "prof.", "professori");
        Rexbox.replace(profession, "dos.", "dosentti");
        return profession;
    }
    

    
    public String reverseName(String name) {
        return Textbox.reverseName(name);
    }
    
    

    public String getSlice(String text, String delimiter, int sliceNumber) {
        String slice = Textbox.getSlice(text, delimiter, sliceNumber);
        if(meaningless(slice)) return "";
        else return slice;
    }
    
    

    public String solveFamilyName(String fullName, int order) {
        String familyName = null;
        try {
            if(fullName != null & fullName.length() > 0) {
                Vector nameSlices = Textbox.slice(fullName, " ");
                if(order == 0) {
                    familyName = (String) nameSlices.elementAt(0);
                }
                else {
                    familyName = (String) nameSlices.elementAt(nameSlices.size()-1);
                }
                if(!isName(familyName)) {
                    familyName = null;
                }
            }
        }
        catch (Exception e) {}
        return familyName;
    }
    
    
        
    public boolean isName(String suspectedName) {
        String nameCharSet = "abcdefghijklmnopqrstuvwzyxåäö-ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ";
        boolean isName = true;
        try {
            if(Character.isUpperCase(suspectedName.charAt(0))) {
                char ch;
                for(int i=0; i<suspectedName.length() && isName; i++) {
                    ch = suspectedName.charAt(i);
                    if(nameCharSet.indexOf(ch) != -1) {
                        isName = false;
                    }
                }
            }
            else {
                isName = false;
            }
        }
        catch(Exception e) {
            isName = false;
        }
        return isName;
    }
        
    
    
    public boolean meaningless(String s) {
        return Textbox.meaningless(s);
    }
    
    
    public String trimExtraSpaces(String s) {
        return Textbox.trimExtraSpaces(s);
    }
    
    
    
    public String getSortAlpha(String s, String charSet) {
        String sortAlpha = "?";
        if(charSet == null || charSet.length() == 0) charSet = "UTF-8";
        try {
            sortAlpha = s.substring(0,1);
            sortAlpha = new String(sortAlpha.getBytes(), charSet);
            if(sortAlpha.equalsIgnoreCase("ä")) sortAlpha="A";
        }
        catch (Exception e) {}
        return sortAlpha.toUpperCase();
    }
    
    

    
    
    public String populateLinks(String text, String linkTemplate) {
        if(text != null && text.length()>0) {
            String DELIMITERS = " \n\t";
            StringBuffer newText = new StringBuffer(1000);
            String searchword;
            String link;
            String substring;
            int index=0;
            int wordStart;
            int wordEnd;
            while(index < text.length()) {
                while(index < text.length() && DELIMITERS.indexOf(text.charAt(index)) != -1) {
                    newText.append(text.charAt(index));
                    index++;
                }
                
                // pass html/xml tags
                while(index < text.length() && text.charAt(index) == '<') {
                   while(index < text.length() && text.charAt(index) != '>') {
                        newText.append(text.charAt(index));
                        index++;
                   }
                   newText.append(text.charAt(index));
                   index++;
                }
                // potential word found
                wordStart=index;
                while(index < text.length() && DELIMITERS.indexOf(text.charAt(index)) == -1 && text.charAt(index) != '<') {
                    index++;
                }
                
                if(index > wordStart) {
                    substring = text.substring(wordStart, index);
                    try { substring = encodeHTML(substring); } catch (Exception e) {}
                    if(index-wordStart > 3) {
                        searchword = trimNonAlphaNums(substring);
                        if(searchword.length() > 3) {
                            link = Rexbox.replace(linkTemplate, "%searchw%", encodeURL(searchword));
                            link = Rexbox.replace(link, "%word%", substring);
                            newText.append(link);
                        }
                        else {
                            newText.append(substring);
                        }
                    }
                    else {
                        newText.append(substring);
                    }
                }
                
            }
            text = newText.toString();
        }
        return text;
    }
    
    
    
    
    
    public String trimNonAlphaNums(String word) {
        if(word == null || word.length() < 1) return "";
        
        int i=0;
        int j=word.length()-1;
        for(; i<word.length() && !Character.isJavaLetterOrDigit(word.charAt(i)); i++);
        for(; j>i+1 && !Character.isJavaLetterOrDigit(word.charAt(j)); j--);
        
        return word.substring(i,j+1);
    }
    
    
    
    public String replace(String string, String regularExpression, String placement) {
        return Rexbox.replace(string, regularExpression, placement);
    }
    
    
    
    
    public String escapeAttributeValue(String value){
		value = encodeXML(value);
		value = Rexbox.replace(value, "'", "&#39;");
		value = Rexbox.replace(value, "\"", "&quot;");
		return value;
	}

    public String trimSpacesToUnderlines(String value) {
        String rval = value.trim().replace(' ', '_');
        return rval;
    }
		
	public String cleanFileName(String value){
		value = Rexbox.replace(value, "[ \n/\\\\,*?'\\\"<>|$+]+", "_");
		return value;
	}
	
	public String escapeJavaScriptString(String value){
		value = Rexbox.replace(value, "\\", "\\\\");
		value = Rexbox.replace(value, "'", "\\'");
		value = Rexbox.replace(value, "\"", "\\\"");
		return value;
	}
    
    
}
