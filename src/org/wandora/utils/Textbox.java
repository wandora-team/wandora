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
 * TextTools.java
 *
 * Created on 24. helmikuuta 2003, 13:28
 */

package org.wandora.utils;


import java.util.*;
import gnu.regexp.*;
import java.awt.*;
import java.io.*;
import javax.swing.text.rtf.*;
import javax.swing.text.*;



/**
 * Textbox is an utility class providing useful text manipulation and processing
 * services as static methods. Some methods here are reserved for historical
 * reasons.
 * 
 * @author akikivela
 */
public class Textbox {
    
    /** Creates a new instance of TextTools */
    public Textbox() {
    }
    
    
    /**
     * Makes a String representation of a map. Useful for debugging.
     * 
     */
    public static String mapToString(Map map){
        StringBuilder buf=new StringBuilder();
        Iterator iter=map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            buf.append(e.getKey().toString()).append(" => ").append(e.getValue().toString()).append(",\n");
        }
        return buf.toString();
    }
    
    
    public static Vector capitalizeFirst(Vector words) {
        Vector newWords = new Vector();
        if (words != null) {
            for (int i=0; i<words.size(); i++) {
                newWords.add(capitalizeFirst((String) words.elementAt(i)));
            }
        }
        return newWords;
    }
    
    

    public static String capitalizeFirst(String word) {
        StringBuilder newWord = new StringBuilder();
        boolean shouldCapitalize = true;
        if (word != null) {
            for (int i=0; i<word.length(); i++) {
                if (Character.isLetter(word.charAt(i)) && shouldCapitalize) {
                    newWord.append(Character.toUpperCase(word.charAt(i)));
                    shouldCapitalize = false;
                }
                else if (!Character.isLetter(word.charAt(i))) {
                    newWord.append(word.charAt(i));
                    shouldCapitalize = true;
                }
                else newWord.append(Character.toLowerCase(word.charAt(i)));
            }
        }
        return newWord.toString();
    }
    
    
    
    public static String makeHTMLParagraph(String text, int width) {
        if(text == null) return null;
        int l = text.length();
        StringBuilder sb = new StringBuilder(l);
        int pl = 0;
        for(int i=0; i<l; i++) {
            if(pl++ < width) sb.append(text.charAt(i));
            else {
                int c = text.charAt(i);
                if(c == ' ') {
                    sb.append("<br>");
                    pl = 0;
                }
                else sb.append((char) c);
            }
        }
        return "<html>" + sb.toString() + "</html>";
    }
    

    
    
    
    public static String getSlice(String text, String delimiter, int sliceNumber) {
        String slice = "";
        try {
            Vector slices = slice(text, delimiter);
            if(slices.size() > sliceNumber) {
                slice = (String) slices.elementAt(sliceNumber);
            }
        }
        catch (Exception e) {}
        if("".equals(slice) && sliceNumber == 0) {
            slice = text;
        }
        return slice;
    }
    
    
    
    public static Vector slice(String text, String delimiter) {
        Vector slices = new Vector();
        String slice;
        
        if (text != null && delimiter != null) {
            while(text.length() > delimiter.length()) {
                try {
                    if (text.indexOf(delimiter) > -1) {
                        slice = text.substring(0, text.indexOf(delimiter));
                        slices.add(slice);
                        text = text.substring(text.indexOf(delimiter) + delimiter.length());
                    }
                    else {
                        //LogWriter.println("No more delimiters found!");
                        slices.add(text);
                        text = "";
                    }
                }
                catch (Exception e) {
                    System.out.println("Exception '" + e.toString() + "' occurred while slicing text!");
                }
            }
        }
        else {
            System.out.println("Either text or delimiter is null! Unable to slice given text!");
        }
        return slices;
    }
   

    public static Vector sliceWithRE(String text, String regularExpression) {
        Vector slices = new Vector();
        String slice;
        try { 
            RE re = new RE(regularExpression);

            if (text != null && re != null) {
                while(text.length() > 0) {
                    try {
                        REMatch match = re.getMatch(text);
                        if (match != null && match.getEndIndex() != -1) {
                            slice = text.substring(0, match.getStartIndex());
                            slices.add(slice);
                            text = text.substring(match.getEndIndex());
                        }
                        else {
                            //LogWriter.println("No more delimiters found!");
                            slices.add(text);
                            text = "";
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Exception '" + e.toString() + "' occurred while slicing text!");
                    }
                }
            }
            else {
                System.out.println("Either text or delimiter is null! Unable to slice given text!");
            }
        }
        catch (Exception e) {
            System.out.println("Illegal regular expression '" + regularExpression + "' used for slicing!");
        }
        return slices;    
    }
    
    
    

    
    public static String filterNonAlphaNums(String string) {
        StringBuilder newString = new StringBuilder();
        char c;
        if(string != null) {
            for(int i=0; i<string.length(); i++) {
                c = string.charAt(i);
                if (c == 's') newString.append('s');
                else if(Character.isLetterOrDigit(c)) {
                    newString.append(c);
                }
                else newString.append('_');
            }
        }
        return newString.toString();
    }
    
    
    
    
    public static String toLowerCase(String string) {
        StringBuilder newString = new StringBuilder();
        char c;
        if(string != null) {
            for(int i=0; i<string.length(); i++) {
                c = string.charAt(i);
                newString.append(Character.toLowerCase(c));
            }
        }
        return newString.toString();
    }
    
    
    
    
    
    public static Vector encode(Vector strings, String charset) {
        Vector newStrings = new Vector();
        for (int i=0; i < strings.size(); i++) {
            try {
                newStrings.add(encode((String) strings.elementAt(i), charset));
            }
            catch (Exception e) {
                System.out.println("Exception '" + e.toString() + "' occurred while encoding vector of strings!");
            }
        }
        return newStrings;
    }
    
    
    
    
    public static String encode(String string, String charset) {
        StringBuilder newString = new StringBuilder();
        
        if (charset != null && charset.length()>0) {
            if(charset.equalsIgnoreCase("UTF-8") || charset.equalsIgnoreCase("UNICODE")) {
                try {
                    return new String(string.getBytes(), charset);
                }
                catch (Exception e) {
                    System.out.println("Unable to convert '" + string + "' to '" + charset + ".");
                }
            }
            else if(charset.equalsIgnoreCase("URL_ENCODE")) {
                return java.net.URLEncoder.encode(string);
            }
        }
        
        return string;        
    }
    
    
    
    /**
     * Transforms all XML specific characters to character entities and
     * Windows new line characters to simple unix style new line characters.
     * 
     * @param s
     * @return Encoded XML string.
     */
    public static String encodeXML(String s) {
        s = Rexbox.replace(s, "&", "&amp;");
        s = Rexbox.replace(s, ">", "&gt;");
        s = Rexbox.replace(s, "<", "&lt;");
        s = Rexbox.replace(s, "\\n\\r", "\\n");
        return s;
    }

    
    
    /**
     * Reverses a name such as "Doe, John" to "John Doe".
     * 
     * @param name
     * @return Reversed name.
     */
    public static String firstNameFirst(String name) {
        if(name != null) {
            int commaIndex = name.indexOf(",");
            if(commaIndex > -1) {
                String fname = name.substring(commaIndex+1);
                String lname = name.substring(0, commaIndex);
                
                return fname.trim() + " " + lname.trim();
            }
        }
        return name;
    }

    
    /**
     * Reverses a name such as "John Doe" to "Doe, John".
     * 
     * @param name
     * @return Reversed name.
     */
    public static String reverseName(String name) {
        String reversedName = name;
        try {
            if (name != null && name.length() > 0) {
                Vector nameSlices = Textbox.slice(name, " ");
                String nameSlice = null;
                if(nameSlices.size() > 1) {
                    nameSlice = (String) nameSlices.elementAt(nameSlices.size() - 1);
                    if(Character.isUpperCase(nameSlice.charAt(0))) {
                        reversedName = nameSlice;
                        String firstNames = "";
                        for(int i=0; i<nameSlices.size()-1; i++) {
                            nameSlice = (String) nameSlices.elementAt(i);
                            if(Character.isUpperCase(nameSlice.charAt(0))) {
                                firstNames = firstNames + " " + nameSlice;
                            }
                        }
                        if(firstNames.length() > 0) {
                            reversedName = reversedName + "," + firstNames;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            reversedName = name;
        }
        return reversedName;
    }
    
    
    
    
    /**
     * Checks if a string has any other characters than white space characters.
     * If the string has only white space characters, method returns true. The
     * String is "meaningless". Otherwise method returns false.
     * 
     * @param string
     * @return true if the string has only white space characters. Otherwise false.
     */
    public static boolean meaningless(String string) {
        if (string != null) {
            if (string.length() > 0) {
                for (int i=0; i<string.length(); i++) {
                    if (!Character.isWhitespace(string.charAt(i))) return false;
                }
            }
        }
        return true;
    }

    
    public static String removeQuotes(String quoted) {
        try {
            if (quoted != null) {
                if (quoted.length() > 0) {
                    if(quoted.startsWith("\"")) {
                        if(quoted.endsWith("\"")) {
                            quoted = quoted.substring(1, quoted.length()-1);
                        }
                        else {
                            quoted = quoted.substring(1);
                        }
                    }
                }
             }
        }
        catch (Exception e) { 
            System.out.println("Catched exception in remove quotes (Textbox)");
            e.printStackTrace();
        }
        return quoted;
    }
    
    
    
    public static String[][] makeStringTable(String s) {
        Vector lines = new Vector();
        Vector linev;
        String line;
        StringTokenizer st = new StringTokenizer(s, "\n");
        StringTokenizer st2;
        int maxc = 0;
        while(st.hasMoreTokens()) {
            line = st.nextToken();
            linev = new Vector();
            st2 = new StringTokenizer(line, "\t");
            int c = 0;
            while(st2.hasMoreTokens()) {
                linev.add(st2.nextToken());
                c++;
            }
            lines.add(linev);
            maxc = Math.max(maxc, c);
        }
        int maxl = lines.size();
        String[][] table = new String[lines.size()][maxc];
        for(int i=0; i<maxl; i++) {
            Vector v = (Vector) lines.elementAt(i);
            for(int j=0; j<maxc; j++) {
                try { table[i][j] = (String) v.elementAt(j); }
                catch (Exception e) { table[i][j] = null; }
            }
        }
        return table;
    }
    
    
    

    public static String sortStringVector(Vector v) {
        String[] a = new String[v.size()];
        String t = null;
        v.toArray(a);
        for(int i=a.length-1; i>0; i--) {
            for(int j=0; j<i; j++) {
                if(a[i].compareTo(a[j]) < 0) {
                   t = a[i];
                   a[i] = a[j];
                   a[j] = t;
                }
            }
        }
        StringBuilder sb = new StringBuilder("");
        for(int i=0; i<a.length; i++) {
            sb.append(a[i] + "\n");
        }
        return sb.toString();
    }
    
    
    
    /**
     * Method creates a string array from objects in given vector.
     * 
     * @param v The vector containing strings to be attached to the generated string.
     * @return Method returns an array containing string picked out of given vector.
     * If vector contains no strings null is returned.
     */
    public static String[] vectorToStringArray(Vector v) {
        String[] a = null;
        if (v != null && v.size() > 0) {
            a = new String[v.size()];
            for (int i=0; i<v.size(); i++) {
                try {
                    a[i] = (String) v.elementAt(i);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
        return a;
    }
    
    
        
    // -------------------------------------------------------------------------

    
    
    
    /**
     * Trims both starting and ending white space characters of a string.
     * This method is here for historical reasons.
     * 
     * @param string
     * @return 
     */
    public static String trimExtraSpaces(String string) {
        return trimEndingSpaces(trimStartingSpaces(string));
    }
    

    /**
     * Trims all ending white space characters of a string. Delegates the
     * action to method trimEndingSpaces.
     * 
     * @param text
     * @return 
     */
    public static String chop(String text) {
        return trimEndingSpaces(text);
    }
    
    
    /**
     * Trims all ending white space characters of a string. This method is
     * here for historical reasons.
     * 
     * @param string
     * @return 
     */
    public static String trimEndingSpaces(String string) {
        if (string != null) {
            int i = string.length()-1;
            while(i > 0 && Character.isWhitespace(string.charAt(i))) i--;
            string = string.substring(0, i+1);
        }
        return string;
    }

    
    /**
     * Trims all starting white space characters of a string. This method is
     * here for historical reasons.
     * 
     * @param string
     * @return 
     */
    public static String trimStartingSpaces(String string) {
        if (string != null) {
            int i = 0;
            while(i < string.length() && Character.isWhitespace(string.charAt(i))) i++;
            string = string.substring(i);
        }
        return string;
    }
    
    
    

    // -------------------------------------------------------------------------
    
    
    /**
     * Returns hexadecimal string representing a color. Color is given as
     * an argument. Hexadecimal string resembles color codes used in HTML and
     * CSS but doesn't contain the hash prefix. For example, this method returns
     * string "ffffff" for the white color and "ff0000" for the red color.
     * 
     * @param color is the color we want the HTML code.
     * @return String representing color's HTML code.
     */
    public static String getColorHTMLCode(Color color) {
        StringBuilder colorName = new StringBuilder("");
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        String code = Integer.toHexString(red);
        if(code.length() < 2) code = "0" + code;
        colorName.append(code);
        code = Integer.toHexString(green);
        if(code.length() < 2) code = "0" + code;
        colorName.append(code);
        code = Integer.toHexString(blue);
        if(code.length() < 2) code = "0" + code;
        colorName.append(code);
        //System.out.println("color: " + colorName.toString());
        return colorName.toString();
    }
    
    
    
    
    /**
     * Reads RTF text from an input stream and returns plain text.
     * Method uses Java's RTFEditorKit for the transformation.
     * 
     * @param in is input stream to be transformed.
     * @return String that contains the RTF text as a plain text.
     * @throws IOException if the input stream doesn't resolve RTF document.
     */
    public static String RTF2PlainText(InputStream in) throws IOException {
        StyledDocument doc=new DefaultStyledDocument();
        RTFEditorKit kit=new RTFEditorKit();
        try {
            kit.read(in,doc,0);
            return doc.getText(0,doc.getLength());
        }
        catch(BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }

    
}
