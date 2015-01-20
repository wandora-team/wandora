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
 * FileTypes.java
 *
 * Created on 27. heinäkuuta 2006, 17:16
 *
 */

package org.wandora.utils;
import java.util.*;
/**
 * This class provides mappings between commonly used file suffixes and 
 * content types.
 *
 * @author olli
 */
public class FileTypes {
    
    /**
     * Maps file suffixes to content types. There may be several suffixes
     * mapping to same content type.
     */
    public static Map<String,String> suffixToType=GripCollections.addArrayToMap(new HashMap<String,String>(),new Object[]{
        "a","application/octet-stream",
        "ai","application/postscript",
        "aif","audio/x-aiff",
        "aifc","audio/x-aiff",
        "aiff","audio/x-aiff",
        "arc","application/octet-stream",
        "au","audio/basic",
        "avi","application/x-troff-msvideo",
        "bcpio","application/x-bcpio",
        "bin","application/octet-stream",
        "c","text/plain",
        "c++","text/plain",
        "cc","text/plain",
        "cdf","application/x-netcdf",
        "cpio","application/x-cpio",
        "djv","image/x-djvu",
        "djvu","image/x-djvu",
        "dump","application/octet-stream",
        "dvi","application/x-dvi",
        "eps","application/postscript",
        "etx","text/x-setext",
        "exe","application/octet-stream",
        "gif","image/gif",
        "gtar","application/x-gtar",
        "gz","application/octet-stream",
        "h","text/plain",
        "hdf","application/x-hdf",
        "hqx","application/octet-stream",
        "htm","text/html",
        "html","text/html",
        "iw4","image/x-iw44",
        "iw44","image/x-iw44",
        "ief","image/ief",
        "java","text/plain",
        "jfif","image/jpeg",
        "jfif-tbnl","image/jpeg",
        "jpe","image/jpeg",
        "jpeg","image/jpeg",
        "jpg","image/jpeg",
        "latex","application/x-latex",
        "man","application/x-troff-man",
        "me","application/x-troff-me",
        "mime","message/rfc822",
        "mov","video/quicktime",
        "movie","video/x-sgi-movie",
        "mpe","video/mpeg",
        "mpeg","video/mpeg",
        "mpg","video/mpeg",
        "ms","application/x-troff-ms",
        "mv","video/x-sgi-movie",
        "nc","application/x-netcdf",
        "o","application/octet-stream",
        "oda","application/oda",
        "pbm","image/x-portable-bitmap",
        "pdf","application/pdf",
        "pgm","image/x-portable-graymap",
        "pl","text/plain",
        "pnm","image/x-portable-anymap",
        "ppm","image/x-portable-pixmap",
        "ps","application/postscript",
        "qt","video/quicktime",
        "ras","image/x-cmu-rast",
        "rgb","image/x-rgb",
        "roff","application/x-troff",
        "rtf","application/rtf",
        "rtx","application/rtf",
        "saveme","application/octet-stream",
        "sh","application/x-shar",
        "shar","application/x-shar",
        "snd","audio/basic",
        "src","application/x-wais-source",
        "sv4cpio","application/x-sv4cpio",
        "sv4crc","application/x-sv4crc",
        "t","application/x-troff",
        "tar","application/x-tar",
        "tex","application/x-tex",
        "texi","application/x-texinfo",
        "texinfo","application/x-texinfo",
        "text","text/plain",
        "tif","image/tiff",
        "tiff","image/tiff",
        "tr","application/x-troff",
        "tsv","text/tab-separated-values",
        "txt","text/plain",
        "ustar","application/x-ustar",
        "uu","application/octet-stream",
        "wav","audio/x-wav",
        "wsrc","application/x-wais-source",
        "xbm","image/x-xbitmap",
        "xpm","image/x-xpixmap",
        "xwd","image/x-xwindowdump",
        "z","application/octet-stream",
        "zip","application/zip",
    });
    
    /**
     * Maps content types to file suffixes.
     */
    public static Map<String,String> typeToSuffix=GripCollections.addArrayToMap(new HashMap<String,String>(),new Object[]{
        "application/postscript","ai",
        "audio/x-aiff","aif",
        "audio/basic","au",
        "application/x-troff-msvideo","avi",
        "application/x-bcpio","bcpio",
        "application/x-netcdf","cdf",
        "application/x-cpio","cpio",
        "image/x-djvu","djv",
        "application/x-dvi","dvi",
        "application/postscript","eps",
        "text/x-setext","etx",
        "image/gif","gif",
        "application/x-gtar","gtar",
        "application/x-hdf","hdf",
        "text/html","htm",
        "image/x-iw44","iw4",
        "image/ief","ief",
        "image/jpeg","jpg",
        "application/x-latex","latex",
        "application/x-troff-man","man",
        "application/x-troff-me","me",
        "message/rfc822","mime",
        "video/quicktime","mov",
        "video/mpeg","mpg",
        "application/x-troff-ms","ms",
        "video/x-sgi-movie","mv",
        "application/x-netcdf","nc",
        "application/oda","oda",
        "image/x-portable-bitmap","pbm",
        "application/pdf","pdf",
        "image/x-portable-graymap","pgm",
        "image/x-portable-anymap","pnm",
        "image/x-portable-pixmap","ppm",
        "application/postscript","ps",
        "video/quicktime","qt",
        "image/x-cmu-rast","ras",
        "image/x-rgb","rgb",
        "application/x-troff","roff",
        "application/rtf","rtf",
        "application/x-shar","shar",
        "audio/basic","snd",
        "application/x-wais-source","src",
        "application/x-sv4cpio","sv4cpio",
        "application/x-sv4crc","sv4crc",
        "application/x-troff","t",
        "application/x-tar","tar",
        "application/x-tex","tex",
        "application/x-texinfo","texi",
        "image/tiff","tif",
        "application/x-troff","tr",
        "text/tab-separated-values","tsv",
        "text/plain","txt",
        "application/x-ustar","ustar",
        "audio/x-wav","wav",
        "application/x-wais-source","wsrc",
        "image/x-xbitmap","xbm",
        "image/x-xpixmap","xpm",
        "image/x-xwindowdump","xwd",
        "application/zip","zip",
        "application/octet-stream","",
    });
    
    /** Creates a new instance of FileTypes */
    public FileTypes() {
    }
    
    /**
     * Gets a file suffix for the specified content type.
     */
    public static String getSuffixForContentType(String contentType){
        return typeToSuffix.get(contentType.toLowerCase());
    }
    /**
     * Gets content type for the specified file suffix.
     */
    public static String getContentTypeForSuffix(String suffix){
        return suffixToType.get(suffix.toLowerCase());
    }
    
}
