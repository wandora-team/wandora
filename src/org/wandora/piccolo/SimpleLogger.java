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
 * SimpleLogger.java
 *
 * Created on July 8, 2004, 4:37 PM
 */

package org.wandora.piccolo;
import java.io.*;
/**
 *
 * @author  olli
 */
public class SimpleLogger extends Logger {
    protected PrintStream stream;
    /** Creates a new instance of SimpleLogger */
    public SimpleLogger() {
        this(System.out);
    }
    public SimpleLogger(PrintStream stream) {
        this.stream=stream;
    }
    
    public void writelog(String level, String s) {
        stream.println(level+" "+s);
    }
    
}
