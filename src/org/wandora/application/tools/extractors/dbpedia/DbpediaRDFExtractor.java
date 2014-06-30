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
 * DbpediaRDFExtractor.java
 *
 * Created on 21.10.2009, 11:17:11
 */



package org.wandora.application.tools.extractors.dbpedia;


import javax.swing.*;
import org.wandora.application.gui.*;
import org.wandora.utils.Tuples.*;
import org.wandora.application.tools.extractors.rdf.*;
import org.wandora.application.tools.extractors.rdf.rdfmappings.*;

/**
 *
 * @author akivela
 */
public class DbpediaRDFExtractor extends AbstractRDFExtractor {


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
