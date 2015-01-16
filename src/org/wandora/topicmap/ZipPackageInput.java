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
 * ZipPackageInput.java
 *
 * Created on 17. maaliskuuta 2006, 13:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.topicmap;
import java.util.zip.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.imageio.stream.FileImageInputStream;
/**
 * <p>
 * Reads entries from a zip file. Entries are stored in ZIP file in the same
 * order they were entered with ZipPackageOutput. Searching entries is relatively
 * smart, gotoEntry will scan the file from the current position for the requested
 * entry, unless the entry has been seen already and it is known to be before
 * the current file position. However it is always best to read entries
 * in the order they appear in the file if possible. This avoids opening
 * the file several times which in the case of a remote file means transferring
 * the file several times over network.</p>
 *
 * @author olli
 */
public class ZipPackageInput implements PackageInput {
    
    private URL url;
    private ZipInputStream zis;
    private Vector<String> entries=null;
    private Vector<String> passedEntries=null;
    private int currentEntry=-1;
    
    /** Creates a new instance of ZipPackageInput */
    public ZipPackageInput(File file) throws IOException {
        this(file.toURL());
    }
    public ZipPackageInput(String file) throws IOException {
        this(new File(file));
    }
    
    public ZipPackageInput(URL url) throws IOException {
        this.url=url;
    }

    private void openStream() throws IOException {
        if(zis!=null) zis.close();
        zis=new ZipInputStream(url.openStream());
        currentEntry=-1;
        passedEntries=new Vector<String>();
    }
    
    /**
     * Moves to the entry with the specified name. Returns true if that entry was
     * found, false otherwise.
     */
    public boolean gotoEntry(String name) throws IOException {
        if(zis==null) currentEntry=-1;
        boolean findFromStart=false;
        if(entries!=null){
            int ind=entries.indexOf(name);
            if(ind==-1) return false;
            if(ind<=currentEntry) findFromStart=true;
        }
        else if(passedEntries!=null && passedEntries.contains(name)) findFromStart=true;
        if( (findFromStart && currentEntry>=0) || zis==null) openStream();
        String e=null;
        while(true){
            e=gotoNextEntry();
            if(e==null) return false;
            if(e.equals(name)) return true;
        }
    }
    
    /**
     * Goes to next entry in the file.
     */
    public String gotoNextEntry() throws IOException{
        if(zis==null)  openStream();
        currentEntry++;
        ZipEntry e=zis.getNextEntry();
        if(e==null) return null;
        passedEntries.add(e.getName());
        return e.getName();
    }
    /**
     * Gets the input stream for current entry.
     */
    public InputStream getInputStream() throws IOException {
        return new InputStream(){
            @Override
            public int available() throws IOException {
                return zis.available();
            }
            public int read() throws IOException {
                return zis.read();
            }
            @Override
            public int read(byte[] b) throws IOException {
                return zis.read(b);
            }
            @Override
            public int read(byte[] b,int off,int len) throws IOException {
                return zis.read(b,off,len);
            }
            @Override
            public long skip(long n) throws IOException {
                return zis.skip(n);
            }
        };
    }
    /**
     * Gets the names of all entries in the file. This requires scanning
     * of the entire file and then reopening the file after the entries
     * are later actually read when gotoEntry or gotoNextEntry is called.
     */
    public Collection<String> getEntries() throws IOException {
        if(entries!=null) return entries;
        if(zis!=null) openStream();
        ZipEntry e=null;
        entries=new Vector<String>();
        while( (e=zis.getNextEntry())!=null ){
            entries.add(e.getName());
        }
        zis.close();
        zis=null;
        return entries;
    }

    /**
     * Closes the file.
     */
    public void close() throws IOException {
        zis.close();
        zis=null;
    }

    
}
