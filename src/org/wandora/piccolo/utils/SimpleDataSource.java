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
 * SimpleDataSource.java
 *
 * Created on 25. tammikuuta 2006, 11:09
 */

package org.wandora.piccolo.utils;
import javax.activation.*;
import javax.mail.internet.*;
import javax.mail.*;
import java.io.*;
import org.wandora.utils.Base64;

/**
 *
 * @author olli
 */
public class SimpleDataSource implements DataSource {
    
    private String name;
    private String contentType;
    private InputStream in;
    
    public SimpleDataSource(String name,String contentType,InputStream in) {
        this.name=name;
        this.contentType=contentType;
        this.in=in;
    }

    public java.io.OutputStream getOutputStream() throws java.io.IOException {
        throw new java.io.IOException("Illegal operation");
    }

    public String getName() {
        return name;
    }

    public java.io.InputStream getInputStream() throws java.io.IOException {
        return in;
    }

    public String getContentType() {
        return contentType;
    }
    
    public static void addBase64Header(MimeBodyPart mbp) throws MessagingException {
        mbp.addHeader("Content-Transfer-Encoding","base64");
    }
}
