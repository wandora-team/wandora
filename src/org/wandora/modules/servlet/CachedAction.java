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
 */
package org.wandora.modules.servlet;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.cache.CacheService;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.User;

/**
 * <p>
 * This is a base class for actions that take advantage of caching
 * services. You can naturally also directly use the caching service without
 * deriving from this class, but many common operations are already done here.
 * </p>
 * <p>
 * As described in the CacheService class, the basic idea in caching is that
 * each cached page has a unique key identifying it. If in the future you
 * need a page with a key that has already been cached, you can just return
 * the cached page instead. The key itself is just any string that identifies the
 * page. As such, everything that affects the page has to be somehow included
 * in the key. This class contains a method, buildCacheKey, that takes a String
 * map and builds a cache key based on that. The key will essentially be a
 * serialisation of the Map. It is not required that you use this method
 * to make the key but it is suitable for most situations.
 * </p>
 * <p>
 * Obviously it is not useful to cache actions that are highly dynamic
 * and requests with the exact same key are unlikely to happen often.
 * </p>
 * <p>
 * To have an action use caching at all, the caching parameter in init parameters
 * has to be set to true. By default caching is turned off. This is because many
 * actions derive from this class that could potentially benefit from caching
 * in some situations but not always. 
 * </p>
 * <p>
 * When extending this class, you have to override at least the doOutput method,
 * which writes the page into a stream. The stream might either be returned
 * directly, or it may be written to a disk from where a copy is then automatically
 * returned to the request.
 * </p>
 * <p>
 * You can also override returnOutput, which forwards
 * data from an input stream into an HTTP response. The input stream might be
 * coming from a cached file, or directly from do Output. The default implementation
 * sets the HTTP headers possibly defined in AbstractAction and then copies the
 * output directly. If you wish to set additional headers, such as content type
 * or character encoding, the way to do it is to use writeMetadata in doOutput,
 * which writes some extra information at the start of the stream, and then
 * readMetadata at returnOutput, which reads it. Then you can add headers, or
 * do anything else necessary, based on the metadata. The writing and reading
 * of metadata must always be done symmetrically.
 * </p>
 * 
 * @author olli
 */


public abstract class CachedAction extends AbstractAction {

    /**
     * The cache service used to cache this action.
     */
    protected CacheService cache;
    /**
     * Is caching turned on.
     */
    protected boolean caching=false;
    
    protected ExecutorService threadPool=null;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.optionalModule(this,CacheService.class, deps);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("caching");
        if(o!=null && o.toString().equalsIgnoreCase("true")) caching=true;
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        cache=manager.findModule(this,CacheService.class);
        if(caching && cache==null) logging.info("Caching is set to true but no caching module found");
        threadPool=Executors.newCachedThreadPool();
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        cache=null;
        threadPool.shutdown();
        try{
            if(!threadPool.awaitTermination(5, TimeUnit.SECONDS)){
                threadPool.shutdownNow();
            }
        }catch(InterruptedException ie){}
        super.stop(manager);
    }
    
    /**
     * Builds a cache key from a Map of Strings. Essentially serialises
     * the map, with the keys having been sorted so that the same set of key-value
     * pairs always returns the same cache key, even if the keys are returned
     * in different order from the maps.
     * 
     * @param params A map of key value pairs used in the cache key.
     * @return A cache key.
     */
    public static String buildCacheKey(Map<String,String> params){
        StringBuilder sb=new StringBuilder();
        
        ArrayList<String> keys=new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        
        for(String key : keys){
            String value=params.get(key);
            if(sb.length()>0) sb.append(";");
            key=key.replace("\\","\\\\").replace("=", "\\=").replace(";","\\s");
            value=value.replace("\\","\\\\").replace(";","\\s");
            sb.append(key);
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }
    
    /**
     * Should return a string that uniquely identifies this request. A future
     * request with the same cache key may use the cached copy of the page.
     * If you don't want some requests cached at all, return a null cache key for
     * those pages. You can use the buildCacheKey method to create a cache key out
     * of a map.
     * 
     * @param req The HTTP request which, result of which is to be cached.
     * @param method The method of the HTTP request.
     * @param action The parsed action parameter from the request.
     * @return A cache key that uniquely identifies the results of this request
     *          being processed.
     */
    protected abstract String getCacheKey(HttpServletRequest req, ModulesServlet.HttpMethod method, String action);
    
    /**
     * <p>
     * Writes a page read from an input stream into an HTTP servlet response.
     * The input stream might be coming from a cached file, or directly from the
     * doOutput method if caching is not in use. In any case, the contents of it
     * need to be written to the response object. Note that any HTTP headers must
     * be set before writing anything else. 
     * </p>
     * <p>
     * If you used writeMetadata in doOutput, you have to override this method
     * and use readMetadata correspondingly to read the metadata from the start
     * of the stream. You can then, for example, set any HTTP headers you read
     * from the metadata. After this, you can call this super implementation which
     * will do the rest of the stream copying.
     * </p>
     * <p>
     * Make absolutely certain that the input stream is closed before returning
     * from this method. Failing to do so may cause the caching service to become
     * locked which can then lock the whole application. It is best to put the
     * contents in a try-finally block and then call cacheIn.close in the finally
     * part. Do this even if you call the super implementation as your code could
     * possibly fail before reaching that part.
     * </p>
     * 
     * @param cacheIn
     * @param resp
     * @throws IOException 
     */
    protected void returnOutput(InputStream cacheIn, HttpServletResponse resp) throws IOException {        
        try{
            setHttpHeaders(resp, httpHeaders);
            
            OutputStream out=resp.getOutputStream();
            byte[] buf=new byte[4096];
            int read;
            while( (read=cacheIn.read(buf))!=-1 ){
                out.write(buf, 0, read);
            }
            out.close();
        }
        finally {
            cacheIn.close();            
        }
    }
    
    /**
     * Use this to write some metadata at the start of the cached file,
     * for example the mime type and character encoding of the file.
     * Use this in doOutput as the first thing after opening the output stream.
     * Read the metadata with readMetadata method in an overridden returnOutput.
     */
    public static void writeMetadata(Map<String,String> data,OutputStream out) throws IOException {
        out.write((""+data.size()).getBytes("UTF-8"));
        out.write(0);
        for(Map.Entry<String,String> e : data.entrySet()){
            String key=e.getKey();
            String value=e.getValue();
            out.write(key.getBytes("UTF-8"));
            out.write(0);
            out.write(value.getBytes("UTF-8"));
            out.write(0);
        }
        out.flush();
    }
    
    private static String readUntilZero(InputStream in) throws IOException {
        byte[] buf=new byte[128];
        int pos=0;
        int read;
        while( (read=in.read())!=-1 ){
            if(read==0) break;
            else {
                if(pos>=buf.length){
                    byte[] newbuf=new byte[buf.length*2];
                    System.arraycopy(buf, 0, newbuf, 0, buf.length);
                    buf=newbuf;
                }
                buf[pos++]=(byte)read;
            }                
        }
        if(read!=0) return null; // broke out of loop due to end of file
        return new String(buf,0,pos,"UTF-8");
    }
    /**
     * Use this to read the metadata written by writeMetadata method. 
     * These two methods must always be used in pair, do not try to use readMetadata
     * if you haven't used writeMetadata. Make your cache keys in such a way that
     * you'll always know whether metadata has been used or not. Use readMetadata
     * by overriding returnOutput and reading it from the cache InputStream as the
     * first thing, after that you can use the original returnOutput for the rest
     * of the input stream.
     */
    public static Map<String,String> readMetadata(InputStream in) throws IOException {
        String countS=readUntilZero(in);
        if(countS==null) return null;
        int count=Integer.parseInt(countS);
        HashMap<String,String> ret=new HashMap<String,String>();
        for(int i=0;i<count;i++){
            String key=readUntilZero(in);
            String value=readUntilZero(in);
            ret.put(key,value);
        }
        return ret;
    }
    
    /**
     * <p>
     * Does the output that might be either cached or returned directly.
     * The OutputProvider is used to get the output stream where the output should be
     * written. Before you get this stream, you may abort the action by returning false
     * but after the output stream is retrieved, you are committed to outputting the
     * page that then might be cached for future use. Therefore
     * you should do all the preparations first, and only then get the output stream
     * when you know that you will be able to output something sensible.
     * </p>
     * <p>
     * If you opened the output stream using the output provider, you must make
     * sure that the stream is properly closed. It is best to place all your code
     * after this in a try-finally block and close the stream in the finally part.
     * Failing to do this may cause the caching service to become locked, which
     * in turn could potentially lock the whole application.
     * </p>
     */
    protected abstract boolean doOutput(HttpServletRequest req, HttpMethod method, String action, OutputProvider out, User user) throws ServletException, IOException, ActionException;

    @Override
    public boolean handleAction(final HttpServletRequest req, final HttpServletResponse resp, final HttpMethod method, final String action, final User user) throws ServletException, IOException, ActionException {
        if(caching && cache!=null ){
            String cacheKey=getCacheKey(req, method, action);
            if(cacheKey!=null){
                InputStream cacheIn=cache.getPage(cacheKey, 0);
                if(cacheIn!=null){
                    returnOutput(cacheIn, resp);
                    return true;
                }
                
                OutputProvider outProvider=new OutputProvider(cacheKey);
                boolean cont=false;
                try{
                    cont=doOutput(req, method, action, outProvider, user);
                }
                finally {
                    if(outProvider.out!=null) outProvider.out.close();
                }
                
                if(cont){
                    cacheIn=cache.getPage(cacheKey, 0);
                    if(cacheIn!=null){
                        returnOutput(cacheIn, resp);
                        return true;
                    }                   
                    else {
                        // If the cache is working correctly, this really shouldn't happen but let's do something sensible
                        logging.error("Could not find the cached page that was just saved");
                        return false;
                    }
                }
                else return false;
            }
        }
        
        OutputProvider out=new OutputProvider(resp);
        try{
            boolean ret=doOutput(req, method, action, out, user);
            if(ret){
                out.out.close(); // this must be done before getFuture().get(), otherwise you'll get a dead lock
                try {
                    out.getFuture().get();
                    return ret;
                } catch (Exception ex) {
                    logging.info("IOException piping result",ex);                
                    return false;
                } 
            }
            else return false;
        }
        finally{
            if(out.out!=null) out.out.close();
        }
    }
    
    /**
     * A class used to pass an output stream to the doOutput method.
     * The stream is not opened until getOutputStream is called. At that point
     * a request is made to the caching system, if caching is used that is, to
     * store the page. Some aspects of the caching system are locked while this
     * stream is open. Furthermore, something sensible should be written when
     * started, and that output may be used again from the cache. Thus you should
     * open the stream only when you know that the request is valid and you can
     * output something sensible. The stream also must be properly closed once
     * done. Failing to do so will keep the caching system locked which may 
     * in turn lock the whole application.
     */
    public class OutputProvider {
        private String cacheKey;
        private OutputStream out;
        
        private HttpServletResponse resp;
        private Future future;
        
        private OutputProvider(HttpServletResponse resp){this.resp=resp;}
        private OutputProvider(OutputStream out){this.out=out;}
        private OutputProvider(String cacheKey){
            this.cacheKey=cacheKey;
        }
        
        /**
         * <p>
         * Returns a future object representing a returnOutput method
         * call. In case output is written directly into an HTTP response, the
         * whole thing is done with piped input and output streams to
         * avoid buffering all the results. Another thread is created to call the
         * returnOutput method while page contents are written in doOutput.
         * The Future object returned by this method represents the returnOutput
         * call. Calling get method of the Future object will wait for that process
         * to finish. Prior to that, the output stream must be closed, otherwise
         * returnOutput won't know when the end of stream has been reached and the
         * threads will dead lock.
         * </p>
         * <p>
         * This is all done in the CachedAction implementation of handleAction.
         * Unless you completely circumvent this implementation, you don't need
         * to worry about any of this.
         * </p>
         * @return A Future object representing an asynchronous returnOutput call
         *          if the output is done directly to an HTTP response. Otherwise
         *          returns null.
         */
        public Future getFuture(){return future;}
        
        /**
         * Opens and returns the output stream where page contents can
         * be written.
         * @return An OutputStream where to write page contents.
         */
        public OutputStream getOutputStream(){
            if(out!=null) return out;
            else if(resp!=null){
                try{                   
                    final PipedInputStream pin=new PipedInputStream();
                    final PipedOutputStream pout=new PipedOutputStream(pin);

                    out=pout;
                    future=threadPool.submit(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                returnOutput(pin, resp);
                            }catch(IOException ioe){
                                logging.info("IOException piping result",ioe);
                            }
                        }
                    });
                    return out;
                
                }catch(IOException ioe){
                    logging.warn("Unable to pipe output",ioe);
                    return null;
                }
            }
            else {
                this.out=cache.storePage(cacheKey, 0);
                return this.out;
            }
        }
    }
}
