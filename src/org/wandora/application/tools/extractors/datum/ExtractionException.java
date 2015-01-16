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
 * ExtractionException.java
 *
 * Created on 24. marraskuuta 2004, 16:30
 */

package org.wandora.application.tools.extractors.datum;

/**
 *
 * @author  olli
 */
public class ExtractionException extends Exception {
    
    /** Creates a new instance of ExtractionException */
    public ExtractionException(String message) {
        super(message);
    }
    public ExtractionException(String message,Throwable cause){
        super(message,cause);
    }
    public ExtractionException(Throwable cause){
        super(cause);
    }
    public ExtractionException(){
    }    
}
