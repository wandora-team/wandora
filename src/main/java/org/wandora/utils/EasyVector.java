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
 *
 * 
 *
 * EasyVector.java
 *
 * Created on June 19, 2001, 4:48 PM
 */

package org.wandora.utils;
import java.util.Vector;
/**
 *
 * @author  olli
 */
public class EasyVector extends Vector {

    /** Creates new EasyVector */
    public EasyVector(Object[] o) {
        for(int i=0;i<o.length;i++){
            this.add(o[i]);
        }
    }

}
