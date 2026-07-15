
package org.wandora.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author olli
 */


public class JarClassLoader extends URLClassLoader {

    protected File[] files;
    
    private static URL[] makeURLs(File[] files) throws MalformedURLException {
        URL[] ret=new URL[files.length];
        for(int i=0;i<files.length;i++){
            ret[i]=files[i].toURI().toURL();
        }
        return ret;
    }
    
    public JarClassLoader(File file) throws MalformedURLException {
        this(new File[]{file});
    }
    
    public JarClassLoader(File[] files, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(makeURLs(files), parent, factory);
        this.files=files;
    }

    public JarClassLoader(File[] files) throws MalformedURLException {
        super(makeURLs(files));
        this.files=files;
    }

    public JarClassLoader(File[] files, ClassLoader parent) throws MalformedURLException {
        super(makeURLs(files), parent);
        this.files=files;
    }
    
    public Collection<String> listClasses() throws IOException {
        LinkedHashSet<String> ret=new LinkedHashSet<String>();
        for(File f : files){
            JarFile jf=new JarFile(f);
            try {
                Enumeration<? extends JarEntry> entries=jf.entries();
                while(entries.hasMoreElements()){
                    JarEntry e=entries.nextElement();
                    if(!e.isDirectory()){
                        String name=e.getName();
                        if(name.endsWith(".class")){
                            name=name.substring(0,name.length()-6);
                            name=name.replaceAll("[/\\\\]", ".");
                            ret.add(name);
                        }
                    }
                }
            }
            finally {
                jf.close();
            }
        }
        return ret;
    }

    public Collection<String> findServices(Class cls) throws IOException {
        return findServices(cls.getName());
    }
    public Collection<String> findServices(String service) throws IOException {
        LinkedHashSet<String> ret=new LinkedHashSet<String>();
        for(File f : files){
            JarFile jf=new JarFile(f);
            try {
                JarEntry e=jf.getJarEntry("META-INF/services/"+service);
                if(e!=null){
                    InputStream is=jf.getInputStream(e);
                    BufferedReader in=new BufferedReader(new InputStreamReader(is));
                    String line;
                    while( (line=in.readLine())!=null ){
                        int commentInd=line.indexOf("#");
                        if(commentInd>=0) line=line.substring(0,commentInd);
                        line=line.trim();
                        if(line.length()>0){
                            ret.add(line);
                        }
                    }
                }
            }
            finally {
                jf.close();
            }
        }
        return ret;
        
    }
}
