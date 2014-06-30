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
 */
package org.wandora.modules.cache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.utils.Tuples;
import org.wandora.utils.Tuples.T2;

/**
 * <p>
 * Stores cached content on a file system. The file names are MD5 hashes
 * of the provided cache key. To avoid hash collisions, the key is stored at the
 * start of each cache file and is checked when the cached input stream is returned.
 * The returned input and output streams are at the position where the actual
 * cached contents are read/written, that is, this class handles internally
 * everything about the keys. Note that hash collisions should be extremely
 * rare.
 * </p>
 * <p>
 * Use parameter name cacheDir in init options to set the directory where files
 * are cached. maxCacheTime parameter can be used to set the maximum age of
 * cached content. Any page that is older than that will not be returned from the
 * cache. The time is given in milliseconds. Use 0 for no cache expiry, this is 
 * the default setting.
 * </p>
 * <p>
 * The cached files can optionally be organised into a directory tree based on
 * the first few characters of the MD5 hash. This avoids having directories 
 * with massive amounts of files which may in some situations be problematic. To
 * configure this in the module parameters, use parameter names numSubdirs and
 * subdirPrefixLength. subdirPrefixLength is the number of characters to use for
 * each subdir, it defaults to 2. numSubdirs is the number of subdirectories to
 * use, it defaults to 0, i.e. put everything directly under the cache directory
 * instead of using any subdirectories.
 * </p>
 * <p>
 * It is essential that the streams returned by getPage and storePage be closed
 * properly. Failing to do so will cause the cache to remain locked. Even if an 
 * IOException is thrown by one of the read methods, close should still be called.
 * </p>
 * 
 * @author olli
 */


public class DiskCacheModule extends AbstractModule implements CacheService {

    protected final String COLLISION_SUFFIX="___collision";
    
    protected int numSubdirs=0;
    protected int subdirPrefixLength=2;
    
    protected long maxCacheTime=0;
    
    protected ReadWriteLock cacheLock = new ReentrantReadWriteLock(false);
    protected String cacheDir;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        Object cacheDirO=settings.get("cacheDir");
        if(cacheDirO!=null) {
            this.cacheDir=cacheDirO.toString();
            if(!this.cacheDir.endsWith(File.separator)) this.cacheDir=this.cacheDir+File.separator;
        }
        else throw new ModuleException("Cache dir not provided, use key cacheDir");
        
        Object o=settings.get("maxCacheTime");
        if(o!=null) maxCacheTime=Long.parseLong(o.toString());
        
        o=settings.get("numSubdirs");
        if(o!=null) numSubdirs=Integer.parseInt(o.toString());
        
        o=settings.get("subdirPrefixLength");
        if(o!=null) subdirPrefixLength=Integer.parseInt(o.toString());
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        File dir=new File(cacheDir);
        if(!dir.exists()) {
            if(!dir.mkdirs()) throw new ModuleException("Could not create cache directory");
        }
        else {
            if(!dir.isDirectory())
                throw new ModuleException("File with the same name as the cache directory already exists");
        }
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        cacheLock.writeLock().lock();
        try{
            super.stop(manager);
        }finally{
            cacheLock.writeLock().unlock();
        }
    }
    
    protected String makeHash(String key){
        MessageDigest hasher=null;
        try{
            hasher=MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException nsae){
            // this shouldn't happen, MD5 is required to be implemented by all Java VMs
            logging.error(nsae);
            return null;
        }
        
        try{
            byte[] hashBytes=hasher.digest(key.getBytes("UTF-8"));
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<hashBytes.length;i++){
                String s=Integer.toHexString(hashBytes[i]<0?256+hashBytes[i]:hashBytes[i]);
                if(s.length()==1) sb.append("0");
                sb.append(s);
            }
            return sb.toString();
        }
        catch(UnsupportedEncodingException uee){
            logging.error(uee);
            return null;
        }
    }
    
    protected String readCacheFileKey(InputStream in) throws IOException {
        byte[] keyBytes=new byte[256];
        int pos=0;
        int read=in.read();
        while(read!=-1 && read!=0){
            if(pos==keyBytes.length) {
                byte[] newBytes=new byte[keyBytes.length*2];
                System.arraycopy(keyBytes, 0, newBytes, 0, keyBytes.length);
                keyBytes=newBytes;
            }
            keyBytes[pos++]=(byte)read;
            read=in.read();
        }
        try{
            return new String(keyBytes, 0, pos, "UTF-8");
        }catch(UnsupportedEncodingException uee){
            logging.error(uee);
            return null;
        }
    }
    
    protected String getSubdirs(String hash){
        if(numSubdirs<=0 || subdirPrefixLength<=0) return "";
        String subdirs="";
        for(int i=0;i<numSubdirs;i++){
            subdirs+=hash.substring(i*subdirPrefixLength,(i+1)*subdirPrefixLength)+File.separator;
        }
        return subdirs;
    }
    
    // you should have the read lock before calling this
    protected InputStream getInputStream(String originalKey, long modifyTime) {
        String key=originalKey;
        try{
            while(true){
                String hash=makeHash(key);
                
                String subdirs=getSubdirs(hash);
                
                File f=new File(cacheDir+subdirs+hash);
                if(!f.exists()) return null;

                FileInputStream fis=new FileInputStream(f);

                // read bytes until first zero byte, those bytes contain the key used to store this file
                String readKey=readCacheFileKey(fis);
                // make sure that the key we read is the expected key
                if(!readKey.equals(originalKey)) {
                    // hash collision, close this input stream and try again with a different key
                    fis.close();
                    key=key+COLLISION_SUFFIX;
                }
                else {
                    // check the modify time if we were provided a sane value for it
                    if(modifyTime>0 && modifyTime>f.lastModified()) return null;

                    return fis;
                }
            }
        }catch(IOException ioe){
            logging.error(ioe);
            return null;
        }
    }
    
    protected void writeCacheFileKey(OutputStream out, String key) throws IOException {
        byte[] bytes=key.getBytes("UTF-8");
        out.write(bytes);
        out.write(0);
    }
    
    // you should have the write lock before calling this
    protected OutputStream getOutputStream(String originalKey) throws IOException {
        String key=originalKey;
        try{
            while(true){
                String hash=makeHash(key);
                
                String subdirs=getSubdirs(hash);
                if(subdirs.length()>0){
                    File dir=new File(cacheDir+subdirs);
                    if(!dir.exists()) {
                        if(!dir.mkdirs()) {
                            logging.error("Couldn't create cache prefix directory "+cacheDir+subdirs);
                            return null;
                        }
                    }
                }
                
                
                File f=new File(cacheDir+subdirs+hash);
                if(f.exists()) {

                    FileInputStream fis=new FileInputStream(f);

                    // read bytes until first zero byte, those bytes contain the key used to store this file
                    String readKey=readCacheFileKey(fis);
                    // make sure that the key we read is the expected key
                    if(!readKey.equals(originalKey)) {
                        // hash collision, close this input stream and try again with a different key
                        fis.close();
                        key=key+COLLISION_SUFFIX;
                        logging.debug("Cache hash collision for keys "+readKey+" and "+key);
                        continue;
                    }
                    else {
                        // this file is the old version of the page we want to store 
                        // so we can just overwrite this file
                        fis.close();
                    }
                }
                
                OutputStream out=new FileOutputStream(f);
                writeCacheFileKey(out, originalKey);
                return out;
            }
        }catch(IOException ioe){
            logging.error(ioe);
            return null;
        }
        
    }
    
    @Override
    public InputStream getPage(String key, long modifyTime) {
        cacheLock.readLock().lock();
        boolean lockReturned=false;
        try{
            if(maxCacheTime>0){
                modifyTime=Math.max(modifyTime, System.currentTimeMillis()-maxCacheTime);
            }
            InputStream in=getInputStream(key, modifyTime);
            if(in!=null){
//                lockReturned=true;
//                return new LockedInputStream(in);
                return in;
            }
            else return null;
        }
        finally{
            if(!lockReturned) cacheLock.readLock().unlock();
        }
    }

    @Override
    public OutputStream storePage(String key, long modifyTime) {
        cacheLock.writeLock().lock();
        boolean lockReturned=false;
        try{
            OutputStream out=getOutputStream(key);
            if(out!=null) {
                lockReturned=true;
                return new LockedOutputStream(out);
            }
            else return null;
        }
        catch(IOException ioe){
            logging.error(ioe);
            return null;
        }
        finally {
            if(!lockReturned) cacheLock.writeLock().unlock();
        }
    }
    
    private class LockedInputStream extends InputStream {

        private InputStream in;
        private boolean released=false;
        public LockedInputStream(InputStream in){
            this.in=in;
        }
        
        private void release(){
            if(!released) {
                cacheLock.readLock().unlock();
                released=true;
            }
        }
        
        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            release();
            in.close();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return in.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }
    
    }
    
    private class LockedOutputStream extends OutputStream {

        private OutputStream out;
        private boolean released=false;
        public LockedOutputStream(OutputStream out){
            this.out=out;
        }
        
        private void release(){
            if(!released){
                cacheLock.writeLock().unlock();
                released=true;
            }
        }
        
        @Override
        public void close() throws IOException {
            release();
            out.close();
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }
        
    }
}
