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
 */

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.Category;
import com.restfb.types.FacebookType;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public class CategoryWrapper extends AbstractFBTypeWrapper {
    
    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE + "category/";

    
    private final Category category;
    
    CategoryWrapper(Category c){
        this.category = c;
    }
    
    @Override
    public FacebookType getEnclosedEntity() {
        return category;
    }

    @Override
    public String getType() {
        return "Category";
    }

    @Override
    public String getSIBase() {
        return SI_BASE;
    }

}
