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
 */

package org.wandora.utils;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.wandora.application.gui.UIBox;

/**
 *
 * @author akivela
 */


public class DataURL {
    public static String defaultStringEncoding = "utf-8";
    
    private byte[] data = new byte[] { };
    private String encoding = "base64";
    private String mimetype = "application/octet-stream";
    
    
    
    public DataURL() {
    }
    
    
    public DataURL(String dataUrl) throws MalformedURLException {
        parseDataURL(dataUrl);
    }
    
    
    public DataURL(File file) throws MalformedURLException {
        if(file != null) {
            mimetype = MimeTypes.getMimeType(file);
            setData(file);
        }
        else {
            throw new MalformedURLException();
        }
    }
    
    
    public DataURL(URL url) throws MalformedURLException {
        if(url != null) {
            mimetype = MimeTypes.getMimeType(url);
            setData(url);
        }
        else {
            throw new MalformedURLException();
        }
    }
    
    
    public DataURL(Image image) throws MalformedURLException {
        File tempFile = null;
        try {
            if(image != null) {
                String prefix = "wandora" + image.hashCode();
                String suffix = ".png";
                tempFile = File.createTempFile(prefix, suffix);
                tempFile.deleteOnExit();
                ImageIO.write(UIBox.makeBufferedImage(image), "png", tempFile);
                mimetype = MimeTypes.getMimeType("png");               
                setData(Files.readAllBytes(tempFile.toPath()));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
        
    public DataURL(byte[] data) {
        this.data = data;
    }
    
    public DataURL(String mimetype, byte[] data) {
        this.mimetype = mimetype;
        this.data = data;
    }
    
    public DataURL(String mimetype, String encoding, byte[] data) {
        this.mimetype = mimetype;
        this.encoding = encoding;
        this.data = data;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public void setData(String data) {
        if("base64".equalsIgnoreCase(encoding)) {
            this.data = Base64.decode(data);
        }
        else {
            this.data = data.getBytes();
        }
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void setData(File file) {
        if(file != null) {
            try {
                setData(Files.readAllBytes(file.toPath()));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void setData(URL url) {
        if(url != null) {
            try {
                setData(IOUtils.toByteArray(url.openStream()));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public InputStream getDataStream() {
        return new ByteArrayInputStream(data);
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public String getMimetype() {
        return mimetype;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public String toExternalForm() {
        StringBuilder dataURL = new StringBuilder("");
        dataURL.append("data:");
        if(mimetype != null) dataURL.append(mimetype).append(";");
        if(encoding != null) dataURL.append(encoding).append(",");
        if(data != null) {
            if("base64".equalsIgnoreCase(encoding)) {
                dataURL.append(Base64.encodeBytes(data));
            }
            else {
                try {
                    dataURL.append(new String(data,defaultStringEncoding));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return dataURL.toString();
    }
    
    
    public String toExternalForm(int options) {
        StringBuilder dataURL = new StringBuilder("");
        dataURL.append("data:");
        if(mimetype != null) dataURL.append(mimetype).append(";");
        if(encoding != null) dataURL.append(encoding).append(",");
        if(data != null) {
            if("base64".equalsIgnoreCase(encoding)) {
                dataURL.append(Base64.encodeBytes(data, options));
            }
            else {
                try {
                    dataURL.append(new String(data,defaultStringEncoding));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return dataURL.toString();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private void parseDataURL(String dataURL) throws MalformedURLException {
        if(dataURL != null && dataURL.length() > 0) {
            if(dataURL.startsWith("data:")) {
                dataURL = dataURL.substring("data:".length());
                int mimeTypeEndIndex = dataURL.indexOf(';');
                if(mimeTypeEndIndex > 0) {
                    mimetype = dataURL.substring(0, mimeTypeEndIndex);
                    dataURL = dataURL.substring(mimeTypeEndIndex+1);
                }
                int encodingEndIndex = dataURL.indexOf(',');
                if(encodingEndIndex > 0) {
                    encoding = dataURL.substring(0, encodingEndIndex);
                    dataURL = dataURL.substring(encodingEndIndex+1);
                }

                if("base64".equalsIgnoreCase(encoding)) {
                    data = Base64.decode(dataURL);
                }
                else {
                    data = dataURL.getBytes();
                }
            }
            else {
                throw new MalformedURLException();
            }
        }
        else {
            throw new MalformedURLException();
        }
    }

    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean equals(Object o) {
        if(o != null) {
            if(o instanceof DataURL) {
                byte[] odata = ((DataURL) o).getData();
                if(data != null && odata != null) {
                    if(data.length == odata.length) {
                        for(int i=0; i<data.length; i++) {
                            if(data[i] != odata[i]) return false;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    
    @Override
    public int hashCode() {
        if(data != null) {
            return Arrays.deepHashCode(new Object[] { data } );
        }
        else {
            return 0;
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public File createTempFile() {
        File tempFile = null;
        try {
            byte[] bytes = this.getData();
            if(bytes != null && bytes.length > 0) {
                String mimetype = this.getMimetype();
                String prefix = "wandora" + this.hashCode();
                String suffix = MimeTypes.getExtension(mimetype);
                if(suffix == null) suffix = "tmp";
                if(!suffix.startsWith(".")) suffix = "."+suffix;
                tempFile = File.createTempFile(prefix, suffix);
                tempFile.deleteOnExit();
                
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(bytes);
                fos.close();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static boolean isDataURL(String dataURLString) {
        if(dataURLString != null) {
            if(dataURLString.startsWith("data:")) {
                return true;
            }
        }
        return false;
    }
    
    
    public static String removeLineBreaks(String dataURLString) throws MalformedURLException {
        DataURL dataURL = new DataURL(dataURLString);
        dataURLString = dataURL.toExternalForm(Base64.DONT_BREAK_LINES);
        return dataURLString;
    }
    
    
    public static void saveToFile(String dataURLString, File file) throws MalformedURLException, IOException {
        DataURL dataURL = new DataURL(dataURLString);
        byte[] bytes = dataURL.getData();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();
    }
    
    public void saveToFile(File file) throws MalformedURLException, IOException {
        byte[] bytes = getData();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();
    }
}
