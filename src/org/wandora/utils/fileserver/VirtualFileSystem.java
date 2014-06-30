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
 * VirtualFileSystem.java
 *
 * Created on 24. heinäkuuta 2006, 16:34
 *
 */

package org.wandora.utils.fileserver;
import java.io.*;
/**
 *
 * A virtual file system is like a file system with directories which contain
 * other directories and files. Usually it is somehow mapped to a real file
 * system but with restrictions so that only certain files can be accessed
 * through it for security reasons. A simple way to do this is to mount
 * one directory as the root directory and then using everything inside that
 * as the file system.
 *
 * @author  olli
 */
public interface VirtualFileSystem {
   
    /**
     * Returns an URL that can be used to access the given virtual file.
     * If the file system does not support this kind of operation, returns null.
     */
    public String getURLFor(String file);
    /**
     * Gets the real File for a virtual file name.
     */
    public File getRealFileFor(String file);
    /**
     * Lists all virtual directories inside the given virtual directory.
     */
    public String[] listDirectories(String dir);
    /**
     * Lists all virtual files in the given virual directory.
     */
    public String[] listFiles(String dir);
    
}
