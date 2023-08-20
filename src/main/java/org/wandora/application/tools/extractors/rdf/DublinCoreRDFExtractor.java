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
 * DublinCoreRDFExtractor.java
 *
 * Created on 12.2.2009,12:32
 */


package org.wandora.application.tools.extractors.rdf;



import javax.swing.Icon;

import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.rdf.rdfmappings.DublinCoreMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDF2TopicMapsMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFSMapping;


/**
 *
 * @author akivela
 */
public class DublinCoreRDFExtractor extends AbstractRDFExtractor {


	private static final long serialVersionUID = 1L;

	private RDF2TopicMapsMapping[] mappings = new RDF2TopicMapsMapping[] {
        new DublinCoreMapping(),
        new RDFSMapping(),
        new RDFMapping(),
    };
    
    
    
    public DublinCoreRDFExtractor() {
        
    }
    

    @Override
    public String getName() {
        return "Dublin core RDF extractor...";
    }
    
    @Override
    public String getDescription(){
        return "Read Dublin core RDF feed and convert it to a topic map.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_dc.png");
    }
    

    public RDF2TopicMapsMapping[] getMappings() {
        return mappings;
    }
    
    
}
