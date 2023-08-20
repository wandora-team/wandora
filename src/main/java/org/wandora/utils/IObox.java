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
 * IObox.java
 *
 * Created on July 23, 2001, 5:31 PM
 * Modified 15.10.2004 / ak
 */


package org.wandora.utils;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.wandora.application.Wandora;




/**
 *
 * @author  akivela
 */
public class IObox extends java.lang.Object {

    /** Creates new IObox */
    public IObox() {
    }

    
    
    
    // ------------------------------------------------------------------ IO ---
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static String loadFile(String fname, String enc) throws FileNotFoundException, IOException {
        File pf = new File(fname);
        FileInputStream fis = new FileInputStream(pf);
        String result = loadFile(fis, enc);
        fis.close();
        return result;
    }     
    
       
    public static String loadFile(String fname) throws FileNotFoundException, IOException {
        File pf = new File(fname);
        FileReader pfr = new FileReader(pf);
        String result = loadFile(pfr);
        pfr.close();
        return result;
    }
   
           
    public static String loadFile(File f) throws FileNotFoundException, IOException {
        FileReader pfr = new FileReader(f); 
        String result = loadFile(pfr);
        pfr.close();
        return result;
    }
   
    
    
    public static String loadFile(InputStream inputStream, String enc) throws IOException {
        InputStreamReader input = new InputStreamReader(inputStream, enc);
        String result = loadFile(input);
        input.close();
        return result;
    }
    
    
    public static String loadFile(InputStreamReader input) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        int c;
        while((c = input.read()) != -1) { sb.append((char) c); }
        input.close();
        return sb.toString();
    }


    public static String loadFile(Reader input) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        int c;
        while((c = input.read()) != -1) { sb.append((char) c); }
        input.close();
        return sb.toString();
    }

/*
    public static BWImage loadBWImage(FileStoreService fileSys, String fileName) {
        return BWImage.loadFromGif(fileSys, fileName);
    }
    
    
    public static BWImage loadBWImage(FileInputStream bfile) throws IOException, FileNotFoundException {
        byte[] bytes = loadBFile(bfile);
        InputStream is = new ByteArrayInputStream( bytes );
        BWImage img = BWImage.loadFromWbmp( is );
        return img;
    }

    public static byte[] loadBFile(String fname) throws IOException, FileNotFoundException {
        FileInputStream bfile = new FileInputStream(fname);
        return loadBFile(bfile);
    }        
*/    
    
    
    /*
     * Note: loadBFile methods can not be used for loading operator logos nor
     * picture messages since these picture files tend to be compressed.
     * Use loadBWFile(FileSys, String) method instead.
     * 
     * Note2: loadBFile methods have not been tested in real life. Expect
     * minor problems if used. 
     */
    
    public static byte[] loadBFile(InputStream inputStream) throws IOException {
        byte[] btable, btabletemp;
        boolean reading = true;
        int bytesRead = 0;
        int newBytes = 0;
        ArrayList<byte []> byteChunks = new ArrayList();
        int chunkSize = 5000;
        
        while (reading) {
            btable = new byte[chunkSize];
            newBytes = inputStream.read(btable);
            if (newBytes != -1) {
                byteChunks.add(btable);
                bytesRead = newBytes;
            }
            else {
                reading = false;
            }
        }
        // System.out.println("loadBFile bytes read: " + ((byteChunks.size()-1) * chunkSize + bytesRead) + "!");
        
        btable = new byte[(byteChunks.size()-1) * chunkSize + bytesRead];
        for(int i=0; i<byteChunks.size()-1 ; i++) {
            // if (debug) System.out.println("loadBFile 1: " + i + "!");
            btabletemp = byteChunks.get(i);
            System.arraycopy(btabletemp, 0, btable, i*chunkSize, chunkSize);
        }
        btabletemp = byteChunks.get(byteChunks.size()-1);
        System.arraycopy(btabletemp, 0, btable, (byteChunks.size()-1) * chunkSize, bytesRead);
        
        return btable;
    }
    
    
    
        
    public static String loadResource(String resourceName) {
        try {
            URL resouceUrl = ClassLoader.getSystemResource(resourceName);
            String resource = IObox.doUrl(resouceUrl);
            return resource;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    


    public static void saveBFile(String fname, byte[] data) throws IOException {
        File pf = new File(fname);
        if (pf.exists()) {
            pf.delete();
            System.out.println("Deleting previously existing file '" + fname + "' before save file operation!");
        }

        FileOutputStream os = new FileOutputStream(fname);
        os.write(data);
        os.flush();
        os.close();
        System.out.println("Saving a file '" + fname + "'");  
    }
    
    
    
    
    public static void saveBFile(String fname, InputStream in) throws IOException {
        byte[] btable;
        int bytesRead = 0;
        int chunkSize = 5000;
        boolean reading = true;
        
        File pf = new File(fname);
        if (pf.exists()) {
            pf.delete();
            System.out.println("Deleting previously existing file '" + fname + "' before save file operation!");
        }
        FileOutputStream os = new FileOutputStream(fname);
        btable = new byte[chunkSize];
            
        while (reading) {
            bytesRead = in.read(btable);
            if (bytesRead != -1) {
                os.write(btable,0 , bytesRead);
            }
            else {
                reading = false;
            }
        }
        os.flush();
        os.close();
    }

    
    /**
     * Method writes given string to a file specified by given file name. If the
     * file with given file name exist the file is removed before data is written.
     * Note that this method does not suit for binary operations. 
     * @param fname The file name of created file.
     * @param data The content to be written into the file.
     * @throws IOException is thrown if the operation failed.
     */
    public static void saveFile(String fname, String data) throws IOException {
        File pf = new File(fname);
        if (pf.exists()) {
            pf.delete();
            System.out.println("Deleting previously existing file '" + fname + "' before save file operation!");
        }

        FileWriter pfr = new FileWriter(fname);
        pfr.write(data, 0, data.length());
        pfr.flush();
        pfr.close();
        System.out.println("Saving a file '" + fname + "'");
    }

    
    
    /**
     * Method writes given string to a file specified by given file object. If the
     * file with given file name exist the file is removed before data is written.
     * Note that this method does not suit for binary operations. 
     * @param pf The file where data is written..
     * @param data The content to be written into the file.
     * @throws IOException is thrown if the operation failed.
     */
    public static void saveFile(File pf, String data) throws IOException {
        if (pf.exists()) {
            pf.delete();
            System.out.println("Deleting previously existing file '" + pf.getName() + "' before save file operation!");
        }

        FileWriter pfr = new FileWriter(pf);
        pfr.write(data, 0, data.length());
        pfr.flush();
        pfr.close();
        System.out.println("Saving a file '" + pf.getAbsolutePath() + "'");
    }
    
    /**
     * Method deletes a local file specified by file's name.
     * @param fname The name of the file to be deleted.
     * @throws FileNotFoundException is thrown if the file was not found.
     * @throws IOException is thrown if the deletion failed.
     */
    public static void deleteFile(String fname) throws FileNotFoundException, IOException {
        File pf = new File(fname);
        if (pf.exists()) {
            pf.delete();
            System.out.println("Deleting file '" + fname + "'");
        }
        else {
            System.out.println("File '" + fname + "' can not be deleted. File does not exist!");
            throw new FileNotFoundException();
        }
    }


    /**
     * Method deletes a local file specified by file's name.
     * @param directory The name of the directory where deleted files locate.
     */
    public static void deleteFiles(String directory) {
        String[] files = getFileNames(directory, ".*", 1, 9999);
        for(int i=0; i<files.length; i++) {
            try {
                deleteFile(files[i]);
            }
            catch (Exception e) {}
        }
    }
    

    
    /**
     * MoveFile method renames local file. File can be tranferred into another directory.
     * @param sourcefile The filename of source file.
     * @param targetfile The filename of target file.
     * @throws FileNotFoundException is thrown if the source file was not found.
     * @throws IOException is thrown if the renaming failed.
     */
    public static void moveFile(String sourcefile, String targetfile, boolean createTargetPath, boolean overwrite) throws FileNotFoundException, IOException {
        File source = new File(sourcefile);
        File target = new File(targetfile);

        if (source.exists()) {
            if(target.exists()) deleteFile(target.getAbsolutePath());
            if(createTargetPath) createPathFor(target.getParentFile());
            source.renameTo(target);
            System.out.println("Renaming file '" + sourcefile + "' to '" + targetfile + "'!");
        }
        else {
            System.out.println("File '" + sourcefile + "' can not be renamed to '" + targetfile + "'. File does not exist!");
            throw new FileNotFoundException();
        }
    }
    
    
    
    
    public static void moveFile(String sourcefile, String targetfile) throws FileNotFoundException, IOException {
        moveFile(sourcefile, targetfile, false, false);
    }
    
    
    
    
    public static void copyFile(String sourcefile, String targetfile, boolean createTargetPath, boolean overwrite) throws FileNotFoundException, IOException, Exception {
        File source = new File(sourcefile);
        File target = new File(targetfile);

        if (source.exists()) {
            if(target.exists() && overwrite) deleteFile(target.getAbsolutePath());
            if(createTargetPath) createPathFor(target.getParentFile());
            //InputStream in, OutputStream out
            moveData(new FileInputStream(source), new FileOutputStream(target));
            System.out.println("Copying file '" + sourcefile + "' to '" + targetfile + "'!");
        }
        else {
            System.out.println("File '" + sourcefile + "' can not be renamed to '" + targetfile + "'. File does not exist!");
            throw new FileNotFoundException();
        }
    }


    public static void copyFile(String sourcefile, String targetfile) throws FileNotFoundException, IOException, Exception {
        copyFile(sourcefile, targetfile, false, false);
    }
    
    
    
    
    
    public static void createPathFor(File dir) {
        if(dir != null) {
            dir.mkdirs();
        }
    }
    
    
 
    
    
    //----------------------------------------------------------------------------
   
    
    
    public static File[] getFiles(String fileName) {
        return hashSetToFileArray(getFilesAsHash(fileName));
        
    }
    
    
    public static File[] getFiles(String fileName, String fileMask, int depth, int space) {
        return hashSetToFileArray(getFilesAsHash(fileName, fileMask, depth, space));
    }
    
    
    
    public static String[] getFileNames(String fileName) {
        return hashSetToStringArray(getFilesAsHash(fileName));
    }
    
    
    
    public static String[] getFileNames(String fileName, String fileMask, int depth, int space) {
        return hashSetToStringArray(getFilesAsHash(fileName, fileMask, depth, space));
    }
    
        
    
    public static HashSet<String> getFilesAsHash(String fileName) {
        return getFilesAsHash(fileName, ".*", 10, 10000);
    }
    
    
    
    public static HashSet<String> getFilesAsHash(String fileName, String fileMask, int depth, int space) {
        Pattern p = null;
        try {
            p = Pattern.compile(fileMask);
            return getFilesAsHash(fileName, p, new ArrayList(), depth, space);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new LinkedHashSet();
    }
    
    
    public static HashSet<String> getFilesAsHash(String fileName, Pattern fileMask, Collection visited, int depth, int space) {
        HashSet files = new LinkedHashSet();
        if(depth >= 0) {
            if (space >= 0) {
                File file = new File(fileName);
                if (file.exists()) {
                    if(file.isDirectory()) {
                        if(!visited.contains(fileName)) {
                            visited.add(fileName);
                            try {
                                // System.out.println("a dir found: " + fileName); 
                                String[] directoryFiles = file.list();
                                for(int i=0; i<directoryFiles.length; i++) {
                                    // System.out.println(" trying: " + File.separator + directoryFiles[i]);
                                    files.addAll(getFilesAsHash(fileName + File.separator + directoryFiles[i], fileMask, visited, depth-1, space-files.size()));
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        try {
                            if(fileMask == null) {
                                files.add(fileName);
                            }
                            else {
                                Matcher m = fileMask.matcher(fileName);
                                if(m.matches()) {
                                    files.add(fileName);
                                    // System.out.println("matching file found: " + fileName);
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else {
                System.out.println("Maximum file number exceeded! Accepting no more files in getFiles!");
            }
        }
        else {
            //System.out.println("Maximum browse depth exceeded!");
        }
        return files;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public static int countFiles(String fileName, String fileMask, int depth, int space) {
        Pattern p = null;
        try {
            p = Pattern.compile(fileMask);
            return countFiles(fileName, p, new ArrayList(), depth, space);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    

 
    
    public static int countFiles(String fileName, Pattern fileMask, Collection visited, int depth, int space) {             
        int fileCount = 0;
        if(depth >= 0) {
            if (space >= 0) {
                File file = new File(fileName);
                if (file.exists()) {
                    if(file.isDirectory()) {
                        if(!visited.contains(fileName)) {
                            visited.add(fileName);
                            //System.out.println("a dir found: " + fileName); 
                            String[] directoryFiles = file.list();
                            for(int i=0; i<directoryFiles.length; i++) {
                                //System.out.println(" trying: " + File.separator + directoryFiles[i]);
                                fileCount = fileCount + countFiles(fileName + File.separator + directoryFiles[i], fileMask, visited, depth-1, space-fileCount);
                            }
                        }
                    }
                    else {
                        //System.out.println("a file found: " + fileName);
                        try {
                            if(fileMask == null) {
                                fileCount++;
                            }
                            else {
                                Matcher m = fileMask.matcher(fileName);
                                if(m.matches()) {
                                    fileCount++;
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else {
                System.out.println("Maximum file number exceeded! Accepting no more files in countFiles!");
            }
        }
        else {
            //System.out.println("Maximum browse depth exceeded!");
        }
        return fileCount;
    }
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- FIND FILE ---
    // -------------------------------------------------------------------------
    
    
    
    public static String findFile(String fileName, String fileMask, int depth) {
        Pattern p = null;
        try {
            p = Pattern.compile(fileMask);
            return findFile(fileName, p, new ArrayList(), depth);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static String findFile(String root, Pattern fileMask, Collection visited, int depth) {
        String foundFile = null;

        if(depth >= 0) {
            File file = new File(root);
            if(file.exists()) {
                if(file.isDirectory()) {
                    if(!visited.contains(root)) {
                        try {
                            visited.add(root);
                            //System.out.println("a dir found: " + root); 
                            String[] directoryFiles = file.list();
                            String dir = null;
                            for(int i=0; i<directoryFiles.length && foundFile == null; i++) {
                                dir = root + File.separator + directoryFiles[i];
                                //System.out.println(" trying: " + File.separator + directoryFiles[i]);
                                foundFile = findFile(dir, fileMask, visited, depth-1);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    try {
                        if(fileMask != null) {
                            Matcher m = fileMask.matcher(root);
                            if(m.matches()) {
                                foundFile = root;
                                System.out.println("matching file found: " + foundFile);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            System.out.println("Maximum browse depth exceeded!");
        }
        return foundFile;
    }
    
    
    
    // ------------------------------------------------------------ FIND URI ---
    
    
    
    public static String findURI(String fileName, String fileMask, int depth) {
        Pattern p = null;
        try {
            p = Pattern.compile(fileMask);
            return findFile(fileName, p, new ArrayList(), depth);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static String findURI(String root, Pattern fileMask, Collection visited, int depth) {
        String foundFile = null;

        if(depth >= 0) {
            File file = new File(root);
            if(file.exists()) {
                if(file.isDirectory()) {
                    if(!visited.contains(root)) {
                        try {
                            visited.add(root);
                            //System.out.println("a dir found: " + root); 
                            String[] directoryFiles = file.list();
                            String dir = null;
                            for(int i=0; i<directoryFiles.length && foundFile == null; i++) {
                                dir = root + File.separator + directoryFiles[i];
                                //System.out.println(" trying: " + File.separator + directoryFiles[i]);
                                foundFile = findFile(dir, fileMask, visited, depth-1);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    try {
                        if(fileMask != null) {
                            Matcher m = fileMask.matcher(root);
                            if(m.matches()) {
                                foundFile = root;
                                System.out.println("matching file found: " + foundFile);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            System.out.println("Maximum browse depth exceeded!");
        }
        return foundFile;
    }
    
    
    
    
    
    //--------------------------------------------------------------------------
    
        
    public static long getModificationTime(URL url) {
        try {
            URLConnection connection = url.openConnection();
            Wandora.initUrlConnection(connection);
            //System.out.println("LAST MOD:" + connection.getLastModified());
            return connection.getLastModified();
        }
        catch (Exception e) {}
        return 0;
    }
    
    
    public static long getModificationTime(String fname) {
        try {
            File pf = new File(fname);
            if (pf.exists()) {
                return pf.lastModified();
            }
        }
        catch (Exception e) {}
        return 0;
    }
    

    
    
    
    
    public static String getYoungestFile(String baseName, String fileMask) {             
        return getOldestOrYoungestFile(baseName, fileMask, false);
    }
    


    
    public static String getOldestFile(String baseName, String fileMask) {             
        return getOldestOrYoungestFile(baseName, fileMask, true);
    }
    
    
    
    public static String getOldestOrYoungestFile(String baseName, String fileMask, boolean oldest) {
        Pattern p = null;
        try {
            p = Pattern.compile(fileMask);
            return getOldestOrYoungestFile(baseName, p, oldest);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    public static String getOldestOrYoungestFile(String baseName, Pattern fileMask, boolean oldest) {             
        String requestedFile = null;
        long requestedFileMod;
        long fileMod;
        String fileName = null;
        
        if(oldest) requestedFileMod = Long.MAX_VALUE;
        else requestedFileMod = 0;
        
        File file = new File(baseName);
        if (file.exists()) {
            if(file.isDirectory()) {
                System.out.println("a dir found: " + fileName); 
                String[] directoryFiles = file.list();
                for(int i=0; i<directoryFiles.length; i++) {
                    fileName = baseName + File.separator + directoryFiles[i];
                    System.out.println(" trying: " + baseName + File.separator + directoryFiles[i]);
                    try {
                        if(fileMask == null || fileMask.matcher(fileName).matches()) {
                            fileMod = getModificationTime(fileName);

                            if(oldest) {
                                if(fileMod < requestedFileMod) {
                                    System.out.println(" oldest found: " + directoryFiles[i]);
                                    requestedFileMod = fileMod;
                                    requestedFile = fileName;
                                }
                            }
                            else {
                                if(fileMod > requestedFileMod) {
                                    System.out.println(" youngest found: " + directoryFiles[i]);
                                    requestedFileMod = fileMod;
                                    requestedFile = fileName;
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                //System.out.println("a file found: " + fileName); 
                if(fileMask.matcher(fileName).matches()) {
                    requestedFile = baseName;
                }
            }
        }
        return requestedFile;
    }
    
    
    
    //----------------------------------------------------------------------------
    /**
     * Method executes an url (with given data) and returns the string url generated.
     * Method can be used to download web data or execute cgi or similar programs
     * addressable by url. Method uses deprecated methods - to be updated.
     *
     * @param url The url executed.
     * @param data The string transferred to the url during execution.
     * @param ctype The string representing content type used during execution.
     * @return The string url execution generated.
     */
    public static String doUrl(URL url, String data, String ctype, String method) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        if (url != null) {
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setDoInput(true);
            con.setUseCaches(false);

            if(method != null && con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).setRequestMethod(method);
                //System.out.println("****** Setting HTTP request method to "+method);
            }

            if(ctype != null) {
                con.setRequestProperty("Content-type", ctype);
            }
            
            if(data != null && data.length() > 0) {
                con.setRequestProperty("Content-length", data.length() + "");
                con.setDoOutput(true);
                PrintWriter out = new PrintWriter(con.getOutputStream());
                out.print(data);
                out.flush();
                out.close();
            }
            //DataInputStream in = new DataInputStream(con.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
     
            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
                if(!(s.endsWith("\n") || s.endsWith("\r"))) sb.append("\n");
            }
            in.close();
        }
        return sb.toString();
    }


    public static String doUrl(URL url) throws IOException {
        return doUrl(url, "", "text/plain", null);
    }
    
    public static String doUrl(URL url, String data) throws IOException {
        return doUrl(url, data, "text/plain", null);
    }  
    
    public static String doUrl(URL url, String data, String ctype) throws IOException {
        return doUrl(url, data, ctype, null);
    }




    public static void moveUrl(URL sourceUrl, File targetFile) throws Exception {
        moveUrl(sourceUrl, targetFile, null, null, true);
    }
    
    
    
    
    
    public static void moveUrl(URL sourceUrl, File targetFile, String user, String password, boolean deleteFile) throws Exception {
        String protocol = sourceUrl.getProtocol();
        if("file".equals(protocol)) {
            String filename = sourceUrl.toExternalForm().substring(6);
            FileInputStream in = new FileInputStream(new File(filename));
            FileOutputStream out = new FileOutputStream(targetFile);

            moveData(in, out);
            if(deleteFile) deleteFile(filename);
        }
        else {
            URLConnection con = sourceUrl.openConnection();
            Wandora.initUrlConnection(con);
            con.setUseCaches(false);
            if(user != null && password != null) {
                String userPassword = user + ":" + password;
//                String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
                String encodedUserPassword = Base64.encodeBytes(userPassword.getBytes());
                con.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);
            }
            InputStream in = con.getInputStream();

            FileOutputStream out = new FileOutputStream(targetFile);
            moveData(in, out);
        }
    }
    
    
    public static void moveData(InputStream in, OutputStream out) throws Exception {
        byte[] data = new byte[8000];
        int l = 0;
        while ((l = in.read(data)) > -1) {
            out.write(data, 0, l);
        }
        in.close();
        out.flush();
        out.close();
    }
    
    
    public static boolean urlExists(URL url) {
        return urlExists(url, null);
    }
    
    public static boolean urlExists(URL url, HttpAuthorizer httpAuthorizer) {
        if (url != null) {
            String protocol = url.getProtocol();
            if("file".equals(protocol)) {
                try {
                    String fname = url.toExternalForm().substring(5);
                    while(fname.startsWith("/")) fname = fname.substring(1);
                    File pf = new File(fname);
                    if (pf.exists()) {
                        return true;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                InputStream in = null;
                try {
                    URLConnection connection = null;
                    if(httpAuthorizer != null) {
                        connection = httpAuthorizer.getAuthorizedAccess(url);
                    }
                    else {
                        connection = url.openConnection();
                        Wandora.initUrlConnection(connection);
                    }
                    in = connection.getInputStream();
                    int b = in.read();
                    in.close();
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try { if(in != null) in.close(); }
                    catch (Exception e) {}
                }
            }
        }
        return false;
    }
    
    
    
        
    
    public static boolean isAuthException(Exception e) {
        if(e instanceof java.io.IOException) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                if(sw.toString().indexOf("Server returned HTTP response code: 401") != -1) return true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------- FILE EXTENSIONS ---
    // -------------------------------------------------------------------------
    
    
    
    public static File forceFileExtension(File candidate, String extension) {
        if(!candidate.exists()) {
            if(!extension.startsWith(".")) extension = "." + extension;
            candidate = new File(getFileWithoutExtension(candidate) + extension);
        }
        return candidate;
    }
    
    public static File addFileExtension(File candidate, String extension) {
        if(!candidate.exists()) {
            String oldExtension = getFileExtension(candidate);
            if(oldExtension == null || oldExtension.length()<1) {
                if(!extension.startsWith(".")) extension = "." + extension;
                candidate = new File(candidate.getPath() + extension);
            }
        }
        return candidate;
    }
    
    public static String getFileExtension(File file) {
        String extension = null;
        if(file != null) {
            String filename = file.getName();
            int i = filename.lastIndexOf('.');
            if(i > 0) {
                extension = filename.substring(i);
            }
        }
        return extension;
    }
    
    
    public static String getFileWithoutExtension(File file) {
        String filenameWithoutExtension = file.getPath();
        if(filenameWithoutExtension != null) {
            int i = filenameWithoutExtension.lastIndexOf('.');
            int j = filenameWithoutExtension.lastIndexOf('/');
            if(i > 0 && i > j) {
                filenameWithoutExtension = filenameWithoutExtension.substring(0, i);
            }
        }
        return filenameWithoutExtension;
    }
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // -- Next are from Http tool of com.gripstudios.platform.tools.HttpTools --
    // -------------------------------------------------------------------------
    
    
    
    private static final int BLOCK_LENGTH = 8192;

    /**
     * Fetches the contents of the specified url.
     * @param url URL to be fetched
     * @return the returned contents of the url
     */
    public static byte[] fetchUrl( URL url ) {
        if( url!=null ) {
            try {
                URLConnection con = url.openConnection();
                Wandora.initUrlConnection(con);
                con.setDoOutput(false);
                con.setDoInput(true);
                con.setUseCaches(false);
                
                con.connect();
                
                DataInputStream inS = new DataInputStream(con.getInputStream());
                Vector bufs = new Vector();
                Vector lengths = new Vector();
                int length = 0;
                for(;;) {
                    byte[] buf = new byte[BLOCK_LENGTH];
                    int readBytes = inS.read(buf);
//                    System.out.println("+"+readBytes+"b");
                    if( readBytes==-1 ) {
                        break;
                    }
                    length += readBytes;
                    bufs.addElement(buf);
                    lengths.addElement(Integer.valueOf(readBytes));
                }
                inS.close();
                byte[] data = new byte[length];
                int pos = 0;
                for( int el=0;el<bufs.size();el++ ) {
                    int len = ((Integer)lengths.elementAt(el)).intValue();
                    System.arraycopy( bufs.elementAt(el),0,data,pos,len );
                    pos += len;
                }
                return data;
            } catch( Exception e ) {
                System.out.println( "ERR Caught: "+e.toString() );
            }
            return null;
        }
        return null;
    }
    
    
    
    public static void executeUrlCall( URL url ) {
        StringBuffer sb = new StringBuffer(5000);
        if (url != null) {
            try {
                System.out.println( url.toString() );
                URLConnection con = url.openConnection();
                Wandora.initUrlConnection(con);
                con.setDoOutput(false);
                con.setDoInput(true);
                con.setUseCaches(false);
                
                con.connect();
                
                //        	BufferedWriter outS = new BufferedWriter( new OutputStreamWriter(con.getOutputStream()) );
                //                outS.close();
                
                DataInputStream inS = new DataInputStream(con.getInputStream());
                byte[] buf = new byte[1024];
                while( inS.read(buf)!=-1 ) {}
                inS.close();
                
            } catch( Exception e ) {
                System.out.println( "ERR Caught: "+e.toString() );
            }
        }
    }
    
    
    
    public static String executeUrlPost( URL url, byte[] data, String ctype, boolean waitForResponse ) {
        return executeUrlPost(url, data, ctype, waitForResponse, null);
    }
    
    
    
    
    public static String executeUrlPost( URL url, byte[] data, String ctype, boolean waitForResponse, java.util.Hashtable params ) {
        StringBuilder sb = new StringBuilder(5000);
        if (url != null) {
            try {
                System.out.println( url.toString() );
                URLConnection con = url.openConnection();
                Wandora.initUrlConnection(con);
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setUseCaches(false);
                con.setRequestProperty("Content-Type",ctype);
                if (params!=null) {
                    for (java.util.Enumeration e = params.keys(); e.hasMoreElements(); ) {
                        String key = (String) e.nextElement();
                        String value = (String) params.get(key);
                        con.setRequestProperty(key, value);
                    }
                }
              	OutputStream outS = con.getOutputStream();
                outS.write(data);
                outS.flush();
                outS.close();
                
                con.connect();
                
                if( waitForResponse ) {
                    DataInputStream inS = new DataInputStream(con.getInputStream());
                    byte[] buf = new byte[1024];
                    while( inS.read(buf)!=-1 ) {}
                    inS.close();
                    // String reply = new String(buf);
                }
                
                return "OK";
            } catch( Exception e ) {
                return e.toString();
            }
        }
        return "No url!";
    }
    
    
    
    
    public static String executeSocketUrlCall( URL url ) {
        if( null==url ) {
            return "No url!";
        }
        try {
            int port = url.getPort();
            if( port==-1 ) 
                port = 80;
            
            Socket s = new Socket( url.getHost(), port );
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            
            String host = url.getProtocol()+"://"+url.getHost()+":"+port;
            String encoded = url.getPath()+"?"+url.getQuery();
            String header = "GET " + encoded + " HTTP/1.1\r\n";
//            System.out.println( header );
            
            bw.write( header );
            bw.write( "\r\n" );
            bw.flush();

            // TODO could parse return code and return it
            BufferedReader br =
                new BufferedReader( new InputStreamReader( s.getInputStream() ) );

            if ( br.ready() ) {
                String line = null;
                while ( (line = br.readLine()) != null ) {
                }
            }
        } catch( Exception e ) {
            return e.toString();
        }
        return "OK";
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
        
    
    public static String[] hashSetToStringArray(HashSet s) {
        String[] strings = null;
        if(s != null) {
            if(s.size() > 0) {
                strings = new String[s.size()];
                int i = 0;
                for(Object o : s) {
                    try {
                        strings[i] = (String) o;
                    }
                    catch (Exception e) {
                        System.out.println("Object not a String in hashSetToStringArray. Skipping!");
                        strings[i] = "";
                    }
                    i++;
                }
            }
            else {
                strings = new String[0];
            }
        }
        return strings;
    }
    
            
    
    public static File[] hashSetToFileArray(HashSet s) {
        File[] files = null;
        if(s != null) {
            if(s.size() > 0) {
                files = new File[s.size()];
                int i = 0;
                for(Object o : s) {
                    try {
                        files[i] = new File((String) o);
                    }
                    catch (Exception e) {
                        System.out.println("Object not a File in hashSetToFileArray. Skipping!");
                        files[i] = null;
                    }
                    i++;
                }
            }
            else {
                files = new File[0];
            }
        }
        return files;
    }
    
    
    
    
    
    public static TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
    };
    
    
    public static void disableHTTPSCertificateValidation() {
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (Exception e) {
        }
    }


    public static String getFileFromURI(URI uri){
        return getFileFromURL(uri.toString());
    }
    public static String getFileFromURL(URL url){
        return getFileFromURL(url.toExternalForm());
    }
    public static String getFileFromURL(String url){
        if(url.startsWith("file:")){
            url=url.substring("file:".length());
            while(url.startsWith("//")) url=url.substring(1);
            try{
                return URLDecoder.decode(url, "UTF-8");
            }catch(UnsupportedEncodingException uee){uee.printStackTrace();return null;}
        }
        else return null;
    }
}
