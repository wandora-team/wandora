/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2013 Wandora Team
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

package org.wandora.application.tools.extractors.discogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.wandora.application.Wandora;

/**
 *
 * @author nlaitinen
 */

public class DiscogsSearchExtractor extends AbstractDiscogsExtractor {
        
 
	private static final long serialVersionUID = 1L;
	
	private static String defaultEncoding = "UTF-8";
    
	
	
    public static String doUrl (URL url) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        
        if (url != null) {
           
                URLConnection con = url.openConnection();
                Wandora.initUrlConnection(con);
                con.setDoInput(true);
                con.setUseCaches(false);
                con.setRequestProperty("Content-type", "text/plain");
                
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName(defaultEncoding)));

                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    if(!(s.endsWith("\n") || s.endsWith("\r"))) sb.append("\n");
                }
                in.close();
            } catch (Exception ex) {
                System.out.println("There was an error fetching data from Discogs.");
            }
        }
        
        return sb.toString();
    }
     
}