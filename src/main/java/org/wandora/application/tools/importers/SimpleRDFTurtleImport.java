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
 * SimpleRDFTurtleImport.java
 *
 * Created on 2009-11-20
 *
 */

package org.wandora.application.tools.importers;


import org.wandora.topicmap.*;
import java.io.*;
import org.apache.jena.rdf.model.*;


/**
 *
 * @author akivela
 */
public class SimpleRDFTurtleImport extends SimpleRDFImport {

	
	private static final long serialVersionUID = 1L;



	/**
     * Creates a new instance of SimpleRDFTurtleImport
     */
    public SimpleRDFTurtleImport() {
    }
    public SimpleRDFTurtleImport(int options) {
        setOptions(options);
    }


    @Override
    public String getName() {
        return "Simple RDF TURTLE import";
    }

    @Override
    public String getDescription() {
        return "Tool imports RDF TURTLE file and merges triplets to current topic map.";
    }



    @Override
    public void importRDF(InputStream in, TopicMap map) {
        if(in != null) {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
            // read the RDF/XML file
            model.read(in, "", "TURTLE");
            RDF2TopicMap(model, map);
        }
    }
}

