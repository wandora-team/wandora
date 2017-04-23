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
 * PackageOutput.java
 *
 * Created on 17. maaliskuuta 2006, 13:30
 *
 */

package org.wandora.topicmap.packageio;
import java.io.*;
/**
 * A package is a collection of data consisting of entries. Each entry has a name
 * and data. Data is written with OutputStream to allow binary entries. A new entry
 * is started by calling nextEntry with the entry name. After that an OutputStream
 * that can be used to write the entry can be retrieved with getOutputStream. A new
 * getOutputStream must be used once for each entry. See notes about order of entries
 * in PackageInput.
 *
 * @author olli
 */
public interface PackageOutput {
    
    
    /**
     * Start a new entry in the package with the given name.
     */
    public void nextEntry(String name) throws IOException;
    /**
     * Closes the package.
     */
    public void close() throws IOException;
    /**
     * Gets the output stream for current entry.
     */
    public OutputStream getOutputStream() throws IOException;
    /**
     * Ensure entry will be removed.
     */
    public void removeEntry(String name) throws IOException;
}
