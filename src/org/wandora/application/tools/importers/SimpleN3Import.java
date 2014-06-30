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
 * SimpleN3Import.java
 *
 * Created on 6. heinäkuuta 2006, 10:29
 *
 */

package org.wandora.application.tools.importers;




import org.wandora.topicmap.*;
import java.io.*;
import com.hp.hpl.jena.rdf.model.*;



/**
 *
 * @author akivela
 */
public class SimpleN3Import extends SimpleRDFImport {
    
    /**
     * Creates a new instance of SimpleN3Import
     */
    public SimpleN3Import() {
    }
    public SimpleN3Import(int options) {
        setOptions(options);
    }
    
    
    @Override
    public String getName() {
        return "Simple RDF N3 import";
    }

    @Override
    public String getDescription() {
        return "Tool imports RDF N3 file and merges triplets to current topic map.";
    }
    


    @Override
    public void importRDF(InputStream in, TopicMap map) {
        if(in != null) {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
            // read the RDF/XML file
            model.read(in, "", "N3");
            RDF2TopicMap(model, map);
        }
    }
}
