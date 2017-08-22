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
 */


package org.wandora.utils;

import java.util.*;
import java.io.File;
import java.net.URL;


/**
 * This class contains mappings from common file extensions to their mime types.
 * Loads mimetypes from a class path file conf/mime.types. See one in Wandora's
 * conf folder for an example.
 *
 * @author olli
 */
public class MimeTypes {
    public static HashMap<String,String> extensionMap = new LinkedHashMap<String,String>();
    static {
        try {
            String mimeContent = IObox.loadResource("conf/mime.types");
            String[] mimeLines = mimeContent.split("\n");
            for(int i=0; i<mimeLines.length; i++) {
                String mimeLine = mimeLines[i].trim();
                if(mimeLine.length() == 0) continue;
                if(mimeLine.startsWith("#")) continue;
                String[] mimeParts = mimeLine.split("\\s");
                if(mimeParts.length > 1) {
                    String mimeType = mimeParts[0].trim();
                    if(mimeType.length() > 0) {
                        for(int j=1; j<mimeParts.length; j++) {
                            String fileExtension = mimeParts[j].trim();
                            if(fileExtension.length() > 0) {
                                extensionMap.put(fileExtension, mimeType);
                                // System.out.println(" "+mimeType+"\t\t"+fileExtension);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Exception '"+e.getMessage()+"' occurred while reading mime types from 'conf/mime.types'.");
        }

        /*
        extensionMap.put("ai","application/postscript");
        extensionMap.put("aif","audio/x-aiff");
        extensionMap.put("aifc","audio/x-aiff");
        extensionMap.put("aiff","audio/x-aiff");
        extensionMap.put("asc","text/plain");
        extensionMap.put("atom","application/atom+xml ");
        extensionMap.put("au","audio/basic");
        extensionMap.put("avi","video/x-msvideo");
        extensionMap.put("bin","application/octet-stream");
        extensionMap.put("c","text/plain");
        extensionMap.put("cc","text/plain");
        extensionMap.put("class","application/octet-stream");
        extensionMap.put("csh","application/x-csh");
        extensionMap.put("css","text/css");
        extensionMap.put("doc","application/msword");
        extensionMap.put("dvi","application/x-dvi");
        extensionMap.put("eps","application/postscript");
        extensionMap.put("exe","application/octet-stream");
        extensionMap.put("fli","video/x-fli");
        extensionMap.put("gif","image/gif");
        extensionMap.put("gml","application/gml+xml");
        extensionMap.put("gpx","application/gpx+xml");
        extensionMap.put("gxf","application/gxf");
        extensionMap.put("gtar","application/x-gtar");
        extensionMap.put("gz","application/x-gzip");
        extensionMap.put("htm","text/html");
        extensionMap.put("html","text/html");
        extensionMap.put("hqx","application/mac-binhex40");
        extensionMap.put("ief","image/ief");
        extensionMap.put("jar","application/java-archive");
        extensionMap.put("ser","application/java-serialized-object");
        extensionMap.put("class","application/java-vm");
        extensionMap.put("jpe","image/jpeg");
        extensionMap.put("jpeg","image/jpeg");
        extensionMap.put("jpg","image/jpeg");
        extensionMap.put("js","application/x-javascript");
        extensionMap.put("json","application/json");
        extensionMap.put("jsonml","application/jsonml+json");
        extensionMap.put("latex","application/x-latex");
        extensionMap.put("lha","application/octet-stream");
        extensionMap.put("lsp","application/x-lisp");
        extensionMap.put("lzh","application/octet-stream");
        extensionMap.put("ma","application/mathematica");
        extensionMap.put("mads","application/mads+xml");
        extensionMap.put("man","application/x-troff-man");
        extensionMap.put("mathml","application/mathml+xml");
        extensionMap.put("mbox","application/mbox");
        extensionMap.put("mrc","application/marc");
        extensionMap.put("mrcx","application/marcxml+xml");
        extensionMap.put("mesh","model/mesh");
        extensionMap.put("mods","application/mods+xml");
        extensionMap.put("mid","audio/midi");
        extensionMap.put("midi","audio/midi");
        extensionMap.put("mime","www/mime");
        extensionMap.put("mov","video/quicktime");
        extensionMap.put("movie","video/x-sgi-movie");
        extensionMap.put("mp2","audio/mpeg");
        extensionMap.put("mp3","audio/mpeg");
        extensionMap.put("mpe","video/mpeg");
        extensionMap.put("mpeg","video/mpeg");
        extensionMap.put("mpg","video/mpeg");
        extensionMap.put("mpga","audio/mpeg");
        extensionMap.put("ms","application/x-troff-ms");
        extensionMap.put("msh","model/mesh");
        extensionMap.put("nc","application/x-netcdf");
        extensionMap.put("ogx","application/ogg");
        extensionMap.put("pbm","image/x-portable-bitmap");
        extensionMap.put("pdf","application/pdf");
        extensionMap.put("pgm","image/x-portable-graymap");
        extensionMap.put("png","image/png");
        extensionMap.put("pnm","image/x-portable-anymap");
        extensionMap.put("pot","application/mspowerpoint");
        extensionMap.put("ppm","image/x-portable-pixmap");
        extensionMap.put("pps","application/mspowerpoint");
        extensionMap.put("ppt","application/mspowerpoint");
        extensionMap.put("ppz","application/mspowerpoint");
        extensionMap.put("ps","application/postscript");
        extensionMap.put("qt","video/quicktime");
        extensionMap.put("ra","audio/x-realaudio");
        extensionMap.put("ram","audio/x-pn-realaudio");
        extensionMap.put("rdf","application/rdf+xml");
        extensionMap.put("rgb","image/x-rgb");
        extensionMap.put("rm","audio/x-pn-realaudio");
        extensionMap.put("roff","application/x-troff");
        extensionMap.put("rpm","audio/x-pn-realaudio-plugin");
        extensionMap.put("rss","application/rss+xml");
        extensionMap.put("rtf","text/rtf");
        extensionMap.put("rtx","text/richtext");
        extensionMap.put("sgm","text/sgml");
        extensionMap.put("sgml","text/sgml");
        extensionMap.put("sh","application/x-sh");
        extensionMap.put("sit","application/x-stuffit");
        extensionMap.put("smi","application/smil");
        extensionMap.put("smil","application/smil");
        extensionMap.put("snd","audio/basic");
        extensionMap.put("swf","application/x-shockwave-flash");
        extensionMap.put("tar","application/x-tar");
        extensionMap.put("tcl","application/x-tcl");
        extensionMap.put("tex","application/x-tex");
        extensionMap.put("texi","application/x-texinfo");
        extensionMap.put("texinfo","application/x-texinfo");
        extensionMap.put("tif","image/tiff");
        extensionMap.put("tiff","image/tiff");
        extensionMap.put("tr","application/x-troff");
        extensionMap.put("txt","text/plain");
        extensionMap.put("unv","application/i-deas");
        extensionMap.put("viv","video/vnd.vivo");
        extensionMap.put("vivo","video/vnd.vivo");
        extensionMap.put("vrml","model/vrml");
        extensionMap.put("wav","audio/x-wav");
        extensionMap.put("wrl","model/vrml");
        extensionMap.put("xbm","image/x-xbitmap");
        extensionMap.put("xlc","application/vnd.ms-excel");
        extensionMap.put("xll","application/vnd.ms-excel");
        extensionMap.put("xlm","application/vnd.ms-excel");
        extensionMap.put("xls","application/vnd.ms-excel");
        extensionMap.put("xlw","application/vnd.ms-excel");
        extensionMap.put("xml","text/xml");
        extensionMap.put("xpm","image/x-xpixmap");
        extensionMap.put("zip","application/zip");
        */
    }
    public static HashMap<String,String> inverseMap = new LinkedHashMap<String,String>();
    static {
        for(Map.Entry<String,String> e : extensionMap.entrySet()) {
            if(!inverseMap.containsKey(e.getValue())) {
                inverseMap.put(e.getValue(), e.getKey());
            }
        }
    }

    public MimeTypes(){
    }

    /**
     * Returns mime type for a file or extension.
     * @param file File name or just file extension.
     * @return
     */
    public static String getMimeType(String file){
        int ind=file.lastIndexOf(".");
        if(ind>-1) file=file.substring(ind+1);
        file=file.toLowerCase();
        return extensionMap.get(file);
    }

    public static String getMimeType(File file){
        return getMimeType(file.getAbsolutePath());
    }

    public static String getMimeType(URL url){
        return getMimeType(url.getPath());
    }

    public static String getExtension(String mimeType){
        return inverseMap.get(mimeType);
    }
}
