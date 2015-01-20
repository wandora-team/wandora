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
 * AdjacencyMatrixImport.java
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
public class AdjacencyMatrixImport extends AbstractWandoraTool implements WandoraTool {



    public final static String SI_PREFIX = "http://wandora.org/si/topic/";

    private boolean cellValueToPlayer = false;




    /** Creates a new instance of AdjacencyMatrixImport */
    public AdjacencyMatrixImport() {
    }






    @Override
    public String getName() {
        return "Adjacency matrix import";
    }

    @Override
    public String getDescription() {
        return "Generates topics and associations from given adjacency matrix.";
    }

    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);

        AdjacencyMatrixImportDialog matrixSourceDialog = new AdjacencyMatrixImportDialog(admin, this, true);
        matrixSourceDialog.setVisible(true);
        if(!matrixSourceDialog.wasAccepted()) return;

        setDefaultLogger();
        setLogTitle("Adjacency matrix import");

        log("Reading adjacency matrix!");
        String matrixData = matrixSourceDialog.getContent();

        log("Parsing adjacency matrix!");
        MatrixParser matrixParser = new MatrixParser(matrixData, topicmap);
        cellValueToPlayer = matrixSourceDialog.cellValueToPlayer();
        matrixParser.parse();
        log("Total "+matrixParser.counter+" associations created!");

        log("Ok!");
        setState(WAIT);
    }



    protected void createEdgeAssociation(ArrayList<T2<String,String>> nodes, TopicMap topicmap) {
        try {
            if(topicmap != null && nodes != null && nodes.size() > 0) {
                hlog("Processing association "+nodes);
                Topic atype = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "edge");
                Association a = null;
                Topic player = null;
                Topic role = null;
                a = topicmap.createAssociation(atype);
                int rc = 0;
                for(T2<String,String> node : nodes) {
                    if(node != null) {
                        player = getOrCreateTopic(topicmap, SI_PREFIX+node.e1, node.e1);
                        role = getOrCreateTopic(topicmap, SI_PREFIX+node.e2, node.e2);
                        if(player != null && !player.isRemoved()) {
                            if(role != null && !role.isRemoved()) {
                                a.addPlayer(player, role);
                            }
                        }
                        rc++;
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
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
        return UIBox.getIcon("gui/icons/import_adjacency_matrix.png");
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



        public MatrixParser(String data, TopicMap topicmap) {
            this.data = data;
            this.topicmap = topicmap;
            this.len = data.length();
            setProgressMax(len);
        }



        private void parse() {
            String[] rows = data.split(rowRegex);
            for(int row=0; row<rows.length; row++) {
                if(rows[row] != null && rows[row].length() > 0) {
                    String[] cols = rows[row].split(colRegex);
                    for(int col=0; col<cols.length; col++) {
                        if(cols[col] != null && cols[col].length() > 0) {
                            String cell = cols[col].trim();
                            if(cellValueToPlayer) {
                                ArrayList<T2<String,String>> nodes = new ArrayList<T2<String,String>>();
                                nodes.add(new T2(""+col, "col"));
                                nodes.add(new T2(""+row, "row"));
                                nodes.add(new T2(""+cell, "cell"));
                                createEdgeAssociation(nodes, topicmap);
                                counter++;
                            }
                            else {
                                if(!"0".equals(cell)) {
                                    ArrayList<T2<String,String>> nodes = new ArrayList<T2<String,String>>();
                                    nodes.add(new T2(""+col, "col"));
                                    nodes.add(new T2(""+row, "row"));
                                    createEdgeAssociation(nodes, topicmap);
                                    counter++;
                                }
                            }
                        }
                    }
                }
            }

        }

    }

}
