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
 * RegexFileChooser.java
 *
 * Created on 30. joulukuuta 2004, 11:08
 */

package org.wandora.utils;
import java.io.*;
import javax.swing.filechooser.FileFilter;
import java.util.regex.*;
/**
 *
 * @author olli
 */
public class RegexFileChooser extends FileFilter{
    
    public String description;
    public Pattern pattern;
    
    /** Creates a new instance of RegexFileChooser */
    public RegexFileChooser(String regex,String description) {
        pattern=Pattern.compile(regex);
        this.description=description;
    }
    public boolean accept(File f){
        if(f.isDirectory()) return true;
        return pattern.matcher(f.getAbsolutePath()).matches();
    }
    public String getDescription(){
        return description;
    }
    
    /**
     * Makes a RegexFileChooser for regular expression "(?i)^.*"+suffix+"$", that is, the
     * file must end with the given suffix (case insensitive).
     */
    public static RegexFileChooser suffixChooser(String suffix,String description){
        return new RegexFileChooser("(?i)^.*"+suffix+"$",description);
    }
    
    public static java.io.FileFilter ioFileFilter(final FileFilter ff){
        return new java.io.FileFilter(){
            public boolean accept(File pathname){
                return ff.accept(pathname);
            }
        };
    }
}
