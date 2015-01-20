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
 * FlipNameMatrix.java
 *
 * Created on 5. toukokuuta 2006, 12:39
 *
 */

package org.wandora.application.tools;


import org.wandora.utils.Options;
import org.wandora.application.*;
import org.wandora.application.contexts.*;



/**
 * WandoraTool changing the orientation of name matrix in topic panel. Affects only the 
 * orientation of schema name matrix.
 *
 * @author akivela
 */
public class FlipNameMatrix extends AbstractWandoraTool implements WandoraTool {
    
    private Options localOptions = null;
    public String optionsPrefix = "gui.";
    
    /**
     * Creates a new instance of FlipNameMatrix
     */
    public FlipNameMatrix() {
    }
    
    public FlipNameMatrix(String optionsPrefix) {
        this.optionsPrefix = optionsPrefix;
    }
    public FlipNameMatrix(Options localOpts) {
        this.localOptions = localOpts;
    }
    
    public FlipNameMatrix(String optionsPrefix, Options localOpts) {
        this.optionsPrefix = optionsPrefix;
        this.localOptions = localOpts;
    }
    
    
    
    public void execute(Wandora wandora, Context context) {
        setFlipOptions(localOptions);
        setFlipOptions(wandora.getOptions());
    }
    
    
    private void setFlipOptions(Options options) {
        if(options != null) {
            if("vertical".equals(options.get(optionsPrefix + "namePanelOrientation"))) {
                options.put(optionsPrefix + "namePanelOrientation", "horizontal");
            }
            else {
                options.put(optionsPrefix + "namePanelOrientation", "vertical");
            }
        }
    }
    

    @Override
    public String getName() {
        return "Flip name matrix";
    }

    @Override
    public String getDescription() {
        return "Change the orientation of name matrix in topic panel. Affects only the orientation of schema name matrix.";
    }

    @Override
    public boolean runInOwnThread(){
        return false;
    }
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
}
