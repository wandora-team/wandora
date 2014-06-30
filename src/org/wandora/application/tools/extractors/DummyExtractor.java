/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 */

package org.wandora.application.tools.extractors;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;




/**
 * Dummy extractor is used as an empty extractor tool and a separator in extractor menu.
 * 
 * @author akivela
 */
public class DummyExtractor extends AbstractWandoraTool {
    

    public DummyExtractor() {
    }
     
    @Override
    public String getName() {
        return "---Dummy Extractor";
    }
    @Override
    public String getDescription(){
        return "Dummy extractor is an empty extractor tool.";
    }
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    private final String[] contentTypes=new String[] { };

    public String[] getContentTypes() {
        return contentTypes;
    }


    public void execute(Wandora admin, Context context) {
        /* NOTHING HERE */
    }
    
    
}
