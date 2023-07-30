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
 * DbpediaRDFExtractor.java
 *
 * Created on 21.10.2009, 11:17:11
 */



package org.wandora.application.tools.extractors.dbpedia;


import javax.swing.Icon;

import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.rdf.AbstractRDFExtractor;
import org.wandora.application.tools.extractors.rdf.rdfmappings.DublinCoreMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.FOAFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.OWLMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDF2TopicMapsMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFSMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RSSMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.SKOSMapping;

/**
 *
 * @author akivela
 */
public class DbpediaRDFExtractor extends AbstractRDFExtractor {


	private static final long serialVersionUID = 1L;
	
	
	private RDF2TopicMapsMapping[] mappings = new RDF2TopicMapsMapping[] {
        new FOAFMapping(),
        new RDFSMapping(),
        new RDFMapping(),
        new OWLMapping(),
        new RSSMapping(),
        new SKOSMapping(),
        new DublinCoreMapping(),
    };



    public DbpediaRDFExtractor() {

    }


    @Override
    public String getName() {
        return "DBpedia RDF/XML extractor...";
    }

    @Override
    public String getDescription(){
        return "Read DBpedia RDF/XML feed and convert it to a topic map.";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_dbpedia.png");
    }


    @Override
    public RDF2TopicMapsMapping[] getMappings() {
        return mappings;
    }

}
