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
 * DropExtractor.java
 *
 * Created on 24. tammikuuta 2007, 14:38
 *
 */

package org.wandora.application.tools;


import java.io.File;
import org.wandora.application.Wandora;
import org.wandora.topicmap.TopicMapException;



/**
 * Drop extractor is a special UI area in Wandora. Drop extractor locates in
 * lower right corner of empty topic panel area. Wandora user can drop files and
 * strings into the drop extractor. This interface specifies the extractor methods
 * a drop extractor must implement. Notice, AbstractExtractor implements 
 * DropExtractor. As a consequence almost all extractors are automatically also
 * DropExtractors.
 *
 * @author akivela
 */


public interface DropExtractor {
    public void dropExtract(File[] files) throws TopicMapException;
    public void dropExtract(String[] urls) throws TopicMapException;
    public void dropExtract(String content) throws TopicMapException;
    
}
