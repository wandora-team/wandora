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
package org.wandora.application.tools.extractors.rekognition;

import java.util.ArrayList;
import java.util.HashMap;
import org.wandora.application.Wandora;

/**
 *
 * @author Eero Lehtonen
 */
class RekognitionConfiguration {
    
    protected static enum AUTH_KEY{
        KEY,
        SECRET
    };
    
    protected ArrayList<String> jobs;
    protected double celebrityTreshold;
    protected boolean celebrityNaming;
    protected HashMap<AUTH_KEY,String> auth;
    
    protected RekognitionConfiguration(ArrayList<String> jobs, boolean naming,
            double treshold, HashMap<AUTH_KEY,String> auth){
        
        this.jobs = jobs;
        this.celebrityNaming = naming;
        this.celebrityTreshold = treshold;
        this.auth = auth;
        
    }
    
    //Defaults
    protected RekognitionConfiguration(){
        this.jobs = new ArrayList<>();
        jobs.add("celebrity");
        jobs.add("emotion");
        this.celebrityNaming = false;
        this.celebrityTreshold = 0;
        this.auth = null;
    }
    
    protected boolean hasAuth(){
        return this.auth != null;
    }
    
    /**
     * Prompt for credentials if we don't have any
     */
    protected boolean askForAuth(){
        RekognitionAuthenticationDialog d = new RekognitionAuthenticationDialog();
        d.open(Wandora.getWandora());
        if(d.wasAccepted()){
            this.auth = d.getAuth();
            return true;
        }
        return false;
    }
    
}
