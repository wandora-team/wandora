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
 * IncidenceMatrixImport.java
 *
 * Created on 2009-12-27, 17:42
 *
 */
package org.wandora.application.tools.importers.graphs;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import static org.wandora.utils.Tuples.T2;

/**
 *
 * @author akivela
 */
public class IncidenceMatrixImport extends AbstractWandoraTool implements WandoraTool {



    public final static String SI_PREFIX = "http://wandora.org/si/topic/";



    /** Creates a new instance of AdjacencyMatrixImport */
    public IncidenceMatrixImport() {
    }






    @Override
    public String getName() {
        return "Incidence matrix import";
    }

    @Override
    public String getDescription() {
        return "Generates topics and associations from given incidence matrix.";
    }

    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);

        IncidenceMatrixImportDialog matrixSourceDialog = new IncidenceMatrixImportDialog(admin, this, true);
        matrixSourceDialog.setVisible(true);
        if(!matrixSourceDialog.wasAccepted()) return;

        setDefaultLogger();
        setLogTitle("Incidence matrix import");

        log("Reading incidence matrix!");
        String matrixData = matrixSourceDialog.getContent();

        log("Parsing incidence matrix!");
        MatrixParser matrixParser = new MatrixParser(matrixData, topicmap);
        matrixParser.parse();
        log("Total "+matrixParser.counter+" associations created!");

        log("Ok!");
        setState(WAIT);
    }




    public Topic getOrCreateTopic(TopicMap map, String si, String basename) {
        Topic topic = null;
        try {
            topic = map.getTopic(si);
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                if(basename != null && basename.length() > 0) topic.setBaseName(basename);
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        return topic;
    }




    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createImportType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_incidence_matrix.png");
    }






    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------





    private class MatrixParser {

        private String rowRegex = "[\n\r]+";
        private String colRegex = "[\\s]+";

        private String data = null;
        private int len = 0;
        private TopicMap topicmap = null;
        public int counter = 0;
        ArrayList<String>[] associations = null;


        public MatrixParser(String data, TopicMap topicmap) {
            this.data = data;
            this.topicmap = topicmap;
            this.len = data.length();
            setProgressMax(len);
        }



        private void parse() {
            String[] rows = data.split(rowRegex);
            associations = new ArrayList[rows.length];
            ArrayList a = null;
            for(int row=0; row<rows.length; row++) {
                if(rows[row] != null && rows[row].length() > 0) {
                    String[] cols = rows[row].split(colRegex);
                    for(int col=0; col<cols.length; col++) {
                        if(cols[col] != null && cols[col].length() > 0) {
                            String cell = cols[col].trim();
                            if(!"0".equals(cell)) {
                                try {
                                    a = associations[col];
                                    if(a == null) {
                                        a = new ArrayList<String>();
                                        associations[col] = a;
                                    }
                                    if(a != null) {
                                        a.add(""+row);
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }


            for(ArrayList<String> associationStruct : associations) {
                try {
                    Topic atype = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "edge");
                    Association realAssociation = topicmap.createAssociation(atype);
                    //System.out.println("Creating asso: ");
                    int roleCount = 0;
                    for(String playerStr : associationStruct) {
                        Topic player = getOrCreateTopic(topicmap, SI_PREFIX+playerStr, ""+playerStr);
                        Topic role = getOrCreateTopic(topicmap, SI_PREFIX+"role-"+roleCount, "role-"+roleCount);
                        //System.out.println("    player: "+player+", role: "+role);
                        realAssociation.addPlayer(player, role);
                        roleCount++;
                    }
                    counter++;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
