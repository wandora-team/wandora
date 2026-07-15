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
 * PackageInput.java
 *
 * Created on 17. maaliskuuta 2006, 13:41
 *
 */

package org.wandora.topicmap.packageio;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;


/**
 * <p>
 * A package is a collection of data consisting of entries. Each entry has a name
 * and data. Data is read with InputStream to allow binary entries. 
 * </p><p>
 * Most implementations will probably have entries in some order (such as order
 * of data in a file). Thus it would be best if data is read in the same order
 * it is returned by gotoNextEntry. If gotoEntry method is used, the implementation
 * may need to read the file multiple times to find the required entries. However,
 * because often entries will need to be read in a specific order, care should be
 * taken when the package is created. To get best possible performance, entries
 * should be stored in the order they are needed when unpackaging. The performance
 * may suffer significantly if packages stored remotely instead of locally.
 * </p>
 * @author olli
 */
public interface PackageInput {
    
    /**
     * Moves to the entry that has the given name.
     * 
     * @param name
     * @return 
     * @throws java.io.IOException
     */
    public boolean gotoEntry(String name) throws IOException;
    
    
    /**
     * Moves to the entry that has the given path and name.
     * 
     * @param path
     * @param name
     * @return 
     * @throws java.io.IOException
     */
    public boolean gotoEntry(String path, String name) throws IOException;
    
 
    /**
     * Moves to next entry and returns it's name.
     * 
     * @return 
     * @throws java.io.IOException
     */
    public String gotoNextEntry() throws IOException;
    
    
    /**
     * Gets input stream for current entry.
     * 
     * @return 
     * @throws java.io.IOException
     */
    public InputStream getInputStream() throws IOException;
    
    
    /**
     * Closes the package.
     * 
     * @throws java.io.IOException
     */
    public void close() throws IOException;
    
    
    
    /**
     * Get list of entries in the package.
     * 
     * @return Collection of entries in the package.
     * @throws java.io.IOException
     */
    public Collection<String> getEntries() throws IOException;
    
    
    
    
    /**
     * Returns file separator used by the PackageInput.
     * 
     * @return 
     */
    public String getSeparator();
    
    
    
    /**
     * Joins path and name, and returns joined resource name. Usually
     * Package input adds a separator string between the path and the name.
     * For example, DirectoryPackageInput adds File.separator string
     * between the path and the name.
     * 
     * @param path
     * @param name
     * @return 
     */
    public String joinPath(String path, String name);
    
    
    
}
