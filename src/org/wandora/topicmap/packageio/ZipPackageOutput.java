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
 * ZipPackageOutput.java
 *
 * Created on 17. maaliskuuta 2006, 13:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.topicmap.packageio;
import java.util.zip.*;
import java.io.*;

/**
 * <p>
 * This class provides methods to write a ZIP file. Each entry is started with
 * nextEntry method that needs the next entry name (file name). Closing
 * entries is not needed, previous entry is closed when nextEntry is called
 * or the entire file is closed with close method.
 * </p>
 * @author olli
 */
public class ZipPackageOutput implements PackageOutput {
    
    private OutputStream out;
    private ZipOutputStream zos;
    
    /** Creates a new instance of ZipPackageOutput */
    public ZipPackageOutput(OutputStream out) {
        this.out=out;
        zos=new ZipOutputStream(out);        
    }

    /**
     * Starts next entry with the specified name.
     */
    @Override
    public void nextEntry(String name) throws IOException{
        zos.putNextEntry(new ZipEntry(name));
    }

    @Override
    public void removeEntry(String name) throws IOException {
    }
    
    /**
     * Gets the output stream for current entry.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new OutputStream(){
            public void write(int b) throws IOException {
                zos.write(b);
            }
            @Override
            public void write(byte[] b,int off,int len) throws IOException {
                zos.write(b,off,len);
            }
            @Override
            public void write(byte[] b) throws IOException {
                zos.write(b);
            }
        };
    }
    
    /**
     * Closes the file.
     */
    @Override
    public void close() throws IOException{
        zos.finish();
        out.close();
    }
    
}
