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
package org.wandora.application.tools.exporters.iiifexport;

/**
 *
 * @author olli
 */


public class Service implements JsonLDOutput {
    public static final String IIIF_IMAGE_API_LEVEL_1="http://library.stanford.edu/iiif/image-api/1.1/conformance.html#level1";
    
    protected String id;
    protected String profile;
    
    public Service(){
        
    }

    public Service(String id, String profile) {
        this.id = id;
        this.profile = profile;
    }
        

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
    
    

    @Override
    public JsonLD toJsonLD() {
        JsonLD jsonLD=new JsonLD();
        jsonLD.append("@id",id)
              .appendNotNull("profile",profile);
        return jsonLD;
    }
    
    
}
