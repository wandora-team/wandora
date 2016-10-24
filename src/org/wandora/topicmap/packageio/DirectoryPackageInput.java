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
 */


package org.wandora.topicmap.packageio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



/**
 *
 * @author akikivela
 */
public class DirectoryPackageInput implements PackageInput {

    private File directory;
    private List<String> entries=null;
    private int currentEntry=-1;
    private InputStream inputStream;
    
    
    
    public DirectoryPackageInput(File dir) throws FileNotFoundException {
        directory = dir;
        initializePackageInput();
    }
    
    
    public DirectoryPackageInput(String dir) throws FileNotFoundException {
        directory = new File(dir);
        initializePackageInput();
    }
    
    
    private void initializePackageInput() throws FileNotFoundException {
        currentEntry=-1;
        entries = new ArrayList<String>();
        collectEntries(directory);
    }
    
    
    private void collectEntries(File d) throws FileNotFoundException {
        if(entries.size() < 999) {
            for (final File fileEntry : d.listFiles()) {
                if (fileEntry.isDirectory()) {
                    collectEntries(fileEntry);
                } 
                else {
                    entries.add(getEntryName(fileEntry));
                }
            }
        }
    }
    
    
    private String getEntryName(File fileEntry) {
        String fileEntryName = fileEntry.getAbsolutePath();
        String entryName = fileEntryName.substring(directory.getAbsolutePath().length());
        if(entryName.startsWith(File.separator)) {
            entryName = entryName.substring(File.separator.length());
        }
        return entryName;
    }
    
    
    @Override
    public boolean gotoEntry(String name) throws IOException {
        if(entries.contains(name)) {
            closeCurrentInputStream();
            currentEntry = entries.indexOf(name);
            return true;
        }
        return false;
    }

    @Override
    public String gotoNextEntry() throws IOException {
        currentEntry++;
        if(currentEntry < entries.size()) {
            closeCurrentInputStream();
            return entries.get(currentEntry);
        }
        else {
            return null;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new InputStream(){
            @Override
            public int available() throws IOException {
                return getCurrentInputStream().available();
            }
            public int read() throws IOException {
                return getCurrentInputStream().read();
            }
            @Override
            public int read(byte[] b) throws IOException {
                return getCurrentInputStream().read(b);
            }
            @Override
            public int read(byte[] b,int off,int len) throws IOException {
                return getCurrentInputStream().read(b,off,len);
            }
            @Override
            public long skip(long n) throws IOException {
                return getCurrentInputStream().skip(n);
            }
        };
    }

    
    @Override
    public void close() throws IOException {
        closeCurrentInputStream();
    }

    
    @Override
    public Collection<String> getEntries() throws IOException {
        return entries;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    private void closeCurrentInputStream() throws IOException {
        if(inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
    
    
    private InputStream getCurrentInputStream() throws FileNotFoundException {
        if(inputStream == null && currentEntry < entries.size()) {
            String fileEntry = entries.get(currentEntry);
            if(fileEntry != null) {
                inputStream = new FileInputStream(new File(directory.getAbsolutePath() + File.separator + fileEntry));
            }
        }
        if(inputStream != null) {
            return inputStream;
        }
        return null;
    }
    



}
