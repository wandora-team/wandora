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

package org.wandora.application.tools.extractors.word;

/**
 *
 * @author Eero Lehtonen
 */


class WordConfiguration {
    
    private boolean BASE_NAME;
    private boolean INSTANCE_DATA;
    private boolean VARIANT_NAME;
    private boolean CASE_SENSITIVE;
    private boolean ASSOCIATE_SCORE;
    
    public WordConfiguration() {

        CASE_SENSITIVE = false;
        BASE_NAME = true;
        VARIANT_NAME = true;
        INSTANCE_DATA = true;
        ASSOCIATE_SCORE = false;

    }
    
    protected void setBaseName(boolean b){
        BASE_NAME = b;
    }
    
    protected void setInstanceData(boolean b){
        INSTANCE_DATA = b;
    }
    
    protected void setVariantName(boolean b){
        VARIANT_NAME = b;
    }
    
    protected void setCaseSensitive(boolean b){
        CASE_SENSITIVE = b;
    }
    
    protected void setAssociateScore(boolean b){
        ASSOCIATE_SCORE = b;
    }
    
    protected boolean getBaseName(){
        return BASE_NAME;
    }
    
    protected boolean getInstanceData(){
        return INSTANCE_DATA;
    }
    
    protected boolean getVariantName(){
        return VARIANT_NAME;
    }
    
    protected boolean getCaseSensitive(){
        return CASE_SENSITIVE;
    }
    
    protected boolean getAssociateScore(){
        return ASSOCIATE_SCORE;
    }
    
}
