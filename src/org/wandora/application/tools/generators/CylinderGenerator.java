/*
 * WANDORA Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * CylinderGenerator.java
 *
 * Created on 2012-05-11
 *
 */

package org.wandora.application.tools.generators;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.io.*;
import java.util.*;
import static org.wandora.utils.Tuples.T2;

/**
 *
 * http://en.wikipedia.org/wiki/Tiling_by_regular_polygons
 *
 * @author elehtonen
 */
public class CylinderGenerator extends AbstractGenerator implements WandoraTool {

    /**
     * Creates a new instance of Cylinder Generator
     */
    public CylinderGenerator() {
    }

    @Override
    public String getName() {
        return "Cylinder graph generator";
    }

    @Override
    public String getDescription() {
        return "Generates cylinder graph topic maps";
    }

    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);

        GenericOptionsDialog god = new GenericOptionsDialog(admin,
                "Cylinder graph generator",
                "Cylinder graph generator creates cylinder abstractions with topic map structures.",
                true, new String[][]{
                    new String[]{"Create a cylinder with square tiling", "boolean"},
                    new String[]{"Create a cylinder with triangular tiling", "boolean"},
                    new String[]{"Create a cylinder with hexagonal tiling", "boolean"},
                    new String[]{"Width of cylinder", "string"},
                    new String[]{"Height of cylinder", "string"},
                    new String[]{"Toroid", "boolean"},}, admin);
        god.setVisible(true);
        if (god.wasCancelled()) {
            return;
        }
        Map<String, String> values = god.getValues();

        ArrayList<Cylinder> cylinders = new ArrayList<Cylinder>();

        int progress = 0;
        int width = 0;
        int height = 0;
        int depth = 0;
        boolean toggleToroid = false;
        try {
            toggleToroid = "true".equals(values.get("Toroid"));
            width = Integer.parseInt(values.get("Width of cylinder"));
            height = Integer.parseInt(values.get("Height of cylinder"));
            if ("true".equals(values.get("Create a cylinder with square tiling"))) {
                cylinders.add(new SquareCylinder(width, height, toggleToroid));
            }
            if ("true".equals(values.get("Create a cylinder with triangular tiling"))) {
                cylinders.add(new TriangularCylinder(width, height, toggleToroid));
            }
            if ("true".equals(values.get("Create a cylinder with hexagonal tiling"))) {
                cylinders.add(new HexagonalCylinder(width, height, toggleToroid));
            }
        } catch (Exception e) {
            singleLog(e);
            return;
        }

        setDefaultLogger();
        setLogTitle("Cylinder graph generator");

        for (Cylinder cylinder : cylinders) {
            ArrayList<T2> edges = cylinder.getEdges();

            log("Creating " + cylinder.getName() + " graph");

            Topic atype = getOrCreateTopic(topicmap, cylinder.getSIPrefix() + "edge", cylinder.getName() + " edge");
            Topic role1 = getOrCreateTopic(topicmap, cylinder.getSIPrefix() + "role1", "role1");
            Topic role2 = getOrCreateTopic(topicmap, cylinder.getSIPrefix() + "role2", "role2");
            Association a = null;
            Topic node1 = null;
            Topic node2 = null;

            if (edges.size() > 0) {
                setProgressMax(edges.size());
                for (T2 edge : edges) {
                    if (edge != null) {
                        node1 = getOrCreateTopic(topicmap, cylinder.getSIPrefix() + "vertex-" + edge.e1, cylinder.getName() + " " + edge.e1);
                        node2 = getOrCreateTopic(topicmap, cylinder.getSIPrefix() + "vertex-" + edge.e2, cylinder.getName() + " " + edge.e2);
                        if (node1 != null && node2 != null) {
                            a = topicmap.createAssociation(atype);
                            a.addPlayer(node1, role1);
                            a.addPlayer(node2, role2);
                        }
                        setProgress(progress++);
                    }
                }
            }
        }

        setState(CLOSE);
    }

    // -------------------------------------------------------------------------
    private interface Cylinder {

        public String getSIPrefix();

        public String getName();

        public int getSize();

        public ArrayList<T2> getEdges();

        public ArrayList<String> getVertices();
    }

    // -------------------------------------------------------------------------
    private class SquareCylinder implements Cylinder {

        private int size = 0;
        private int width = 0;
        private int height = 0;
        private boolean isToroid = false;

        public SquareCylinder(int w, int h, boolean toroid) {
            this.width = w;
            this.height = h;
            this.size = w * h;
            this.isToroid = toroid;
        }

        public String getSIPrefix() {
            return "http://wandora.org/si/cylinder/square/";
        }

        public String getName() {
            return "square-cylinder";
        }

        public int getSize() {
            return size;
        }

        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();

            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    int ww = (w == width - 1) ? 0 : (w + 1);
                    int hh = (h == height - 1 && isToroid) ? 0 : (h + 1);
                    edges.add(new T2(h + "-" + w, h + "-" + ww));
                    edges.add(new T2((h + "-" + ww), hh + "-" + ww));
                }
            }
            if (!this.isToroid) {
                for (int w = 0; w < width; w++) {
                    int ww = (w != width - 1) ? (w + 1) : 0;
                    edges.add(new T2(height + "-" + w, height + "-" + ww));
                }
            }


            return edges;
        }

        public ArrayList<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<String>();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    vertices.add(x + "-" + y);
                }
            }
            return vertices;
        }
    }

    // -------------------------------------------------------------------------
    private class TriangularCylinder implements Cylinder {

        private int depth = 0;
        private int width = 0;
        private boolean isToroid = false;

        public TriangularCylinder(int w, int d, boolean toroid) {
            this.width = w;
            this.depth = d;
            this.isToroid = toroid;
        }

        public String getSIPrefix() {
            return "http://wandora.org/si/cylinder/triangular/";
        }

        public String getName() {
            return "triangular-cylinder";
        }

        public int getSize() {
            int size = 0;
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d; f++) {
                    size++;
                } 
           }
            return size;
        }

        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();

            for (int d = 0; d < depth; d++) {
                for (int w = 0; w < width; w++) {
                    int ww = (w == width - 1) ? 0 : (w + 1);
                    int dd = (d == depth - 1 && this.isToroid) ? 0 : (d + 1);
                    edges.add(new T2(d + "-" + w, d + "-" + ww));
                    edges.add(new T2(d + "-" + w, dd + "-" + ww));
                    edges.add(new T2(d + "-" + ww, dd + "-" + ww));
                }
            }

            if (!this.isToroid) {
                for (int w = 0; w < width; w++) {
                    int ww = (w != width - 1) ? (w + 1) : 0;
                    edges.add(new T2(depth + "-" + w, depth + "-" + ww));
                }
            }


            return edges;
        }

        public ArrayList<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<String>();
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d; f++) {
                    vertices.add(d + "-" + f);
                }
            }
            return vertices;
        }
    }

    // -------------------------------------------------------------------------
    private class HexagonalCylinder implements Cylinder {

        private int depth = 0;
        private int width = 0;
        private boolean isToroid = false;

        public HexagonalCylinder(int w, int d, boolean toroid) {
            this.width = w;
            this.depth = d;
            this.isToroid = toroid;
        }

        public String getSIPrefix() {
            return "http://wandora.org/si/cylinder/hexagonal/";
        }

        public String getName() {
            return "hexagonal-cylinder";
        }

        public int getSize() {
            int size = 0;
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d; f++) {
                    size++;
                }
            }
            return size;
        }

        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();
            String nc = null;
            String n1 = null;
            String n2 = null;
            String n3 = null;

            for (int d = 0; d < depth; d++) {
                for (int w = 0; w < width; w++) {
                    
                    nc = d + "-" + w + "-c";
                    n1 = (d == depth -1 && this.isToroid) ? 0 + "-" + w : (d+1) + "-" + w ;
                    n2 = d + "-" + w;

                    edges.add(new T2(nc, n1));
                    edges.add(new T2(nc, n2));

                    n3 = (w == width - 1) ? d + "-" + 0 : d + "-" + (w + 1); 

                    edges.add(new T2(nc, n3));

                }
            }

            if (!this.isToroid) {
                for (int w = 0; w < width; w++) {
                    nc = depth + "-" + w + "-c";
                    n1 = depth + "-" + w;
                    n2 = (w == width -1 ) ? depth + "-" + 0 : depth + "-" + (w + 1);

                    edges.add(new T2(nc, n1));
                    edges.add(new T2(nc, n2));
                }
            }


            return edges;
        }

        public ArrayList<String> getVertices() {
            HashSet<String> verticesSet = new HashSet<String>();
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d + 1; f++) {
                    verticesSet.add(d + "-" + f + "-c");
                    verticesSet.add(d + "-" + f);
                    verticesSet.add(d + "-" + (f + 1));

                    if (d == 0) {
                        verticesSet.add(d + "-" + f + "-t");
                    } else {
                        verticesSet.add((d - 1) + "-" + f);
                    }
                }
            }
            ArrayList<String> vertices = new ArrayList<String>();
            vertices.addAll(verticesSet);
            return vertices;
        }
    }
}
