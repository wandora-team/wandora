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
 * LogWriter.java
 *
 * Created on July 23, 2001, 5:05 PM
 */

package org.wandora.utils.regexextractor;



import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;


/**
 *
 * @author  akivela
 */
public class LogWriter extends Object {
    public static PrintWriter printWriter = null;
    public static String prefix = "";
    public static boolean active = true; 
    
    
    /** Creates new LogWriter */
    public LogWriter() {
    }

    
   // --------------------------------------------------------------------------     

    public static void setLogFile(String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            printWriter = new PrintWriter(out);
        } catch (Exception e) {}
    }
    
    
   /**
     * SetOutputWriter method is used to set the stream used for output printing.
     * All debugging and information is streamed to outWriter stream.
     * If outputWriter Stream does not exist or is null printing is streamed to
     * System.out default stream.
     *
     * @param writer The stream where output is targeted.
     */
    public static void setPrintWriter(PrintWriter writer) {
        printWriter = writer;
    }
    
    
    
    public static void setPrefix(String pre) {
        prefix = new String(pre);
    }
    
    
        
    public static void setActive(boolean flag) {
        active = flag;
    }
    
    
    /**
     * Println method is used to print strings to previously set output stream.
     * If output stream is null or does not exist printed strings are put
     * to System.out stream. Println uses print method. Line separator is printed
     * after the string.
     *
     * @param str The string to be printed.
     */
    public static void println(String str) {
        println("APP",str);
    }
    public static void println(String level,String str) {
        if (active) {
            if( null==printWriter )
                print(level, str + System.getProperty("line.separator"));
            else
                print(level, str + System.getProperty("line.separator"));
        }
    }
    
    
    
    
    public static void print(String level, String plainstr) {
        if (active) {
            try {
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat();
                String str = plainstr;
                if (printWriter != null) {
                    try {
                        printWriter.write(str, 0, str.length());
                        printWriter.flush();
                    } catch (Exception e) { System.out.print(str + " (FAILED TO LOG)"); }
                }
                else System.out.print(str);
            } catch (Exception e) { System.out.print(plainstr); }
        }
    }
    
    public static void print(String str) {
        print("APP",str);
    }
    
}
