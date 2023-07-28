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
 */

package org.wandora.application.tools.exporters;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 *
 * @author akivela
 */
public class GephiExport extends AbstractExportTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	
	public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    public boolean EXPORT_CLASSES = true;
    public boolean EXPORT_OCCURRENCES = true;
    public boolean EXPORT_ALL_ASSOCIATIONS = true;

    private List<AttributeColumn> nodeAttributes;
    private List<AttributeColumn> edgeAttributes;

    private int edgeCounter = 0;
    private int nodeCounter = 0;

    private Map<String, DataNode> dataNodes;
    private List<DataEdge> dataEdges;

    public GephiExport() {
    }
    public GephiExport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_graph.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }


    @Override
    public boolean isConfigurable(){
        return true;
    }

    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog genOptDiag=new GenericOptionsDialog(admin,"Gephi","Gephi Export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should Wandora export also topic types (class-instance relations)?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Export all associations","boolean",(EXPORT_ALL_ASSOCIATIONS ? "true" : "false"),"Should associations with multiple roles be exported?"},

        },admin);
        genOptDiag.setVisible(true);
        if(genOptDiag.wasCancelled()) return;

        Map<String, String> values = genOptDiag.getValues();
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        EXPORT_ALL_ASSOCIATIONS = ("true".equals(values.get("Export all associations")) ? true : false );
    }





    static final int BUFFER = 2048;

    @Override
    public void execute(Wandora wandora, Context context) {
       String topicMapName = null;
       String exportInfo = null;

       nodeAttributes = new ArrayList<AttributeColumn>();
       edgeAttributes = new ArrayList<AttributeColumn>();

       edgeCounter = 0;
       nodeCounter = 0;

       dataNodes = new LinkedHashMap<String, DataNode>();
       dataEdges = new ArrayList<DataEdge>();


        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as Gephi graph";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(wandora, context);
            topicMapName = this.solveNameForTopicMap(wandora, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as Gephi graph";
            }
            else {
                exportInfo =  "Exporting topic map as Gephi graph";
                topicMapName = "no_name_topic_map";
            }
        }


        // --- Then solve target file
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");

        File file;

        if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            file = chooser.getSelectedFile();
            String fileName = file.getName();

            // --- Finally write topicmap as Gephi to chosen file
            OutputStream out=null;
            File fileXML = null;
            try {
                file=IObox.addFileExtension(file, "gephi"); // Ensure file extension exists!
                fileXML = File.createTempFile(file.getName(), "gephi");
                fileName = file.getName(); // Updating filename if file has changed!

                out=new FileOutputStream(fileXML);
                log(exportInfo+" to '"+fileName+"'.");

                exportGephi(out, tm, topicMapName, getCurrentLogger());

                out.flush();
                out.close();
            }
            catch(Exception e) {
                log(e);
                try { if(out != null) out.close(); }
                catch(Exception e2) { log(e2); }
            }

            // Lets read and archive the file we just created
            try {
                FileInputStream fi = new FileInputStream(fileXML);
                BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                FileOutputStream dest = new FileOutputStream(file);
                ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(dest));

                byte data[] = new byte[BUFFER];

                zout.putNextEntry(new ZipEntry(file.getName()));

                int count;

                while((count = origin.read(data, 0, BUFFER)) != -1) {
                   zout.write(data, 0, count);
                }

                zout.flush();
                zout.close();

            } 
            catch(Exception e) {
               Logger.getLogger(GephiExport.class.getName()).log(Level.SEVERE, null, e);
            }
            log("Ready.");


        }

        setState(WAIT);


       nodeAttributes = null;
       edgeAttributes = null;

       edgeCounter = 0;
       nodeCounter = 0;

       dataNodes = null;
       dataEdges = null;

    }


    @Override
    public String getName() {
        return "Export Gephi graph";
    }
    @Override
    public String getDescription() {
        return "Exports topic map layer as Gephi file.";
    }

    WandoraToolLogger logger;
    public void exportGephi(OutputStream out, TopicMap topicMap, String graphName, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while instantiating PrintWriter with UTF-8 character encoding. Using default encoding.");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }

        //int totalCount = 2*topicMap.getNumTopics() + topicMap.getNumAssociations();
        logger.setProgressMax(100); // For now I have to "fake" the real progress count
        int count = 0;

        println(writer, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        println(writer, "<gephiFile version=\"0.7\">"); // Should this be versionless?

        println(writer, "<core tasks=\"0\">");
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.format(date);

        println(writer, "<lastModifiedDate>"+df.format(date)+"</lastModifiedDate>");
        println(writer, "</core>");

        println(writer, "<project name=\"Wandora Project\">"); // TODO Get real project name here, if even one exists
        println(writer, "<metadata>");
        println(writer, "<title/>"); // TODO See if you can put something reasonable here.
        println(writer, "<author>Wandora</author>"); // TODO Real author or just leave it as "Wandora"
        println(writer, "<keywords/>");
        println(writer, "<description/>");
        println(writer, "</metadata>");
        println(writer, "<workspaces>");

        println(writer, "<workspace name=\"Default workspace\" status=\"open\">");



        collectNodesAndEdges(topicMap); // 50
        logger.setProgress(50);


        echoAttributeModel(writer); // 10
        logger.setProgress(60);

        echoDhns(writer); // 10
        logger.setProgress(70);

        echoAttributeRows(writer); // 10 This can be done only after echoAttributeModel
        logger.setProgress(80);

        echoGraphData(writer); // 10
        logger.setProgress(90);

        echoEmptyTables(writer); // 10
        logger.setProgress(100);

        // Finally write closing tags for the xml
        println(writer, "</workspace>");
        println(writer, "</workspaces>");
        println(writer, "</project>");
        println(writer, "</gephiFile>");

        writer.flush();
        writer.close();
    }
    
    
    

    private void collectNodesAndEdges(TopicMap topicMap) throws TopicMapException {

        Iterator<Topic> ti = topicMap.getTopics();

        int assosNum = topicMap.getNumAssociations();

        // First nodes, then edges
        while(ti.hasNext())
        {
            Topic topic = ti.next();
            DataNode dNode = new DataNode(makeString(topic.getBaseName()), makeString(topic.getOneSubjectIdentifier().toString()));


            StringBuilder occurences = new StringBuilder("");

            if(EXPORT_OCCURRENCES) {
                Collection<Topic> types=topic.getDataTypes();
                Iterator<Topic> iter2=types.iterator();
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    String typeName = type.getBaseName();
                    Hashtable<Topic,String> ht=topic.getData(type);
                    Iterator<Map.Entry<Topic, String>> iter3=ht.entrySet().iterator();
                    while(iter3.hasNext()){
                        Map.Entry<Topic,String> e=iter3.next();
                        String data=(String)e.getValue();
                        String occurence = makeString(typeName)+" : "+makeString(data)+",";
                        occurences.append(occurence);
                    }
                }
            }
            if(occurences.length() > 1) {
                occurences.deleteCharAt(occurences.length()-1);
            }
            dNode.setOccurences(occurences.toString());

            dataNodes.put(topic.getOneSubjectIdentifier().toExternalForm(), dNode);

        }

        Iterator<Topic> iter;

        // Classes

        if(EXPORT_CLASSES) {
            iter=topicMap.getTopics();
            while(iter.hasNext()) {
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                Iterator<Topic> iter2=t.getTypes().iterator();
                while(iter2.hasNext()) {
                    Topic t2=(Topic)iter2.next();
                    if(t2.isRemoved()) continue;
                    DataEdge de = new DataEdge(dataNodes.get(t2.getOneSubjectIdentifier().toExternalForm()), dataNodes.get(t.getOneSubjectIdentifier().toExternalForm()), "class-instance", "class", "instance");
                    dataEdges.add(de);
                }
            }
        }



        // Associations
        Iterator<Association> iter2=topicMap.getAssociations();
        
        while( iter2.hasNext() ) {

            Association a=(Association)iter2.next();

            Collection<Topic> roles = a.getRoles();
            if(roles.size() < 2) continue;
            else if(roles.size() == 2) {
                Topic[] roleArray = (Topic[]) roles.toArray(new Topic[2]);
                Topic source = a.getPlayer(roleArray[0]);
                Topic target = a.getPlayer(roleArray[1]);

                DataEdge de = new DataEdge(dataNodes.get(source.getOneSubjectIdentifier().toExternalForm()), dataNodes.get(target.getOneSubjectIdentifier().toExternalForm()),
                        makeString(a.getType().getBaseName()), makeString(roleArray[0].getBaseName()),
                        makeString(roleArray[1].getBaseName()));
                dataEdges.add(de);

            }
            else if (EXPORT_ALL_ASSOCIATIONS && roles.size() > 2){
                // TODO multiple roles association

                DataNode dumNode = createDummyNode();
                for (Topic role : roles) {
                    
                    Topic targetTopic = a.getPlayer(role);

                    DataEdge de = new DataEdge(dumNode, dataNodes.get(targetTopic.getOneSubjectIdentifier().toExternalForm()),
                            makeString(a.getType().getBaseName())+" - "+makeString(role.getBaseName()),
                            "", makeString(role.getBaseName()));
                    dataEdges.add(de);


                }

            }

        }

        Collections.sort(dataEdges, new sortByEdgeId());


    }

    private int dummyCounter = 0;
    private DataNode createDummyNode() {
        DataNode dNode = new DataNode("", "", true);

        String key = "empty_dummy_node_association_"+dummyCounter;

        dataNodes.put(key, dNode);
        dummyCounter++;
        return dNode;
    }

    /*private ArrayList<Topic> addAssociations(Topic topic) throws TopicMapException
    {
        ArrayList<Topic> subTopics = new ArrayList<Topic>();
        ArrayList<Association> associations = new ArrayList<Association>(topic.getAssociations());

        for (Association assoc : associations) {

            ArrayList<Topic> roles =  new ArrayList<Topic>(assoc.getRoles());

            Topic playerRole = null;

            for (Topic role : roles)
            {

                Topic player = assoc.getPlayer(role);

                if(player != topic)
                {
                    playerRole = player;
                    addTopic(player, false);
                    subTopics.add(player);
                    break;
                    //Node noud = topicNodes.get(inst);
                }

            }

            if(playerRole != null &&
               !topicNodes.get(topic).isConnectedTo(topicNodes.get(playerRole), ConnectionEdge.Type.ASSOCIATION, assoc) ) {

                createConnection(topic, playerRole, ConnectionEdge.Type.ASSOCIATION, assoc);

            }

        }


        return subTopics;
    }*/

    // --------------------------------------------------------------- ECHOS ---

    protected void echoAttributeRows(PrintWriter writer) {

        println(writer, "<attributerows>");


        // Nodes

        //Collection<DataNode> nodeColl = dataNodes.values();

        ArrayList<DataNode> nodeList = new ArrayList<DataNode>(dataNodes.values());
        Collections.sort(nodeList, new sortByNodeId());
        //for (DataNode dataNode : nodeColl) {
        for (int i=0;i<nodeList.size();i++) {
            DataNode dataNode = nodeList.get(i);

            println(writer, "<noderow for=\""+dataNode.id+"\" version=\"6\">");
            for (int j = 0; j < nodeAttributes.size(); j++) {
                AttributeColumn attCol = nodeAttributes.get(j);

                String value = ""; // TODO warning! in future this value might different type

                if(attCol.id.equals("id"))
                {
                    value = Integer.toString(dataNode.id);
                } else if(attCol.id.equals("label")) {
                    value = dataNode.label;
                } else if(attCol.id.equals("si")) {
                    value = dataNode.si;
                } else if(attCol.id.equals("basename")) {
                    value = dataNode.baseName;
                } else  if(attCol.id.equals("occurences")) {
                    value = dataNode.occurences;
                }

                println(writer, "<attvalue index=\""+attCol.index+"\">"+makeString(value)+"</attvalue>");
            }

            println(writer, "</noderow>");

        }

        // Edges

        for (int i=0;i<dataEdges.size();i++) {
            DataEdge dataEdge = dataEdges.get(i);

            println(writer, "<edgerow for=\""+dataEdge.id+"\" version=\"3\">");

            for (int j = 0; j < edgeAttributes.size(); j++) {
                AttributeColumn attCol = edgeAttributes.get(j);

                String value = ""; // TODO warning! in future this value might different type

                if(attCol.id.equals("id"))
                {
                    value = Integer.toString(dataEdge.id);
                } else if(attCol.id.equals("label")) {
                    value = dataEdge.label;
                } else if(attCol.id.equals("type"))
                {
                    value = dataEdge.type;
                } else if(attCol.id.equals("role1"))
                {
                    value = dataEdge.role1;
                } else if(attCol.id.equals("role2"))
                {
                    value = dataEdge.role2;
                }

                println(writer, "<attvalue index=\""+attCol.index+"\">"+value+"</attvalue>");
            }

            println(writer, "</edgerow>");

        }

        println(writer, "</attributerows>");

    }

    protected void echoAttributeModel(PrintWriter writer) {

        nodeAttributes = new ArrayList<AttributeColumn>();
        edgeAttributes = new ArrayList<AttributeColumn>();

        println(writer, "<attributemodel>");

        println(writer, "<table edgetable=\"false\" name=\"Nodes\" nodetable=\"true\" version=\"2\">");

        nodeAttributes.add(echoAttributeColumn(writer, 0, "id", "Id"));
        nodeAttributes.add(echoAttributeColumn(writer, 1, "label", "Label"));
        nodeAttributes.add(echoAttributeColumn(writer, 2, "si", "Subject identifier"));
        nodeAttributes.add(echoAttributeColumn(writer, 3, "basename", "Basename"));
        nodeAttributes.add(echoAttributeColumn(writer, 4, "occurences", "Occurences", "LIST_STRING","DATA"));

        println(writer, "</table>");

        println(writer, "<table edgetable=\"true\" name=\"Edges\" nodetable=\"false\" version=\"2\">");

        edgeAttributes.add(echoAttributeColumn(writer, 0, "id", "Id"));
        edgeAttributes.add(echoAttributeColumn(writer, 1, "label", "Label"));
        edgeAttributes.add(echoAttributeColumn(writer, 2, "role1", "Source role"));
        edgeAttributes.add(echoAttributeColumn(writer, 3, "role2", "Target role"));
        edgeAttributes.add(echoAttributeColumn(writer, 4, "type", "Type"));



        println(writer, "</table>");

        println(writer, "</attributemodel>");

    }

    protected AttributeColumn echoAttributeColumn(PrintWriter writer, int index, String id, String title, String type, String origin)
    {
        println(writer, "<column>");
        println(writer, "<index>"+index+"</index>");
        println(writer, "<id>"+id+"</id>");
        println(writer, "<title>"+title+"</title>");
        println(writer, "<type>"+type+"</type>");
        println(writer, "<origin>"+origin+"</origin>");
        println(writer, "<default/>");
        println(writer, "</column>");

        return new AttributeColumn(index, id, title, type, origin);

    }

    protected AttributeColumn echoAttributeColumn(PrintWriter writer, int index, String id, String title)
    {
        return echoAttributeColumn(writer, index, id, title, "STRING", "PROPERTY");
    }

    protected void echoGraphData(PrintWriter writer) {

        writer.println("<Data>");

        // Nodes

        Collection<DataNode> nodeColl = dataNodes.values();

        int maxArea = nodeColl.size() * 2;

        if(maxArea > 5000)
        {
            maxArea = 5000;
        }

        ArrayList<DataNode> nodeList = new ArrayList<DataNode>(dataNodes.values());
        Collections.sort(nodeList, new sortByNodeId());

        // Lets make just a simple grid, nothing fancy.

        double nodesPerLine = Math.floor(Math.sqrt(nodeColl.size()));

        double lineXCounter = 1;
        double lineYCounter = 1;

        //for (DataNode dataNode : nodeColl) {
        for (int i=0;i<nodeList.size();i++) {
            DataNode dataNode = nodeList.get(i);

            double randomX = (maxArea/2)-maxArea*(lineXCounter/nodesPerLine);
            double randomY = (maxArea/2)-maxArea*(lineYCounter/nodesPerLine);

            lineXCounter++;

            if(lineXCounter >= nodesPerLine){
                lineXCounter = 1;
                lineYCounter++;
            }


            //randomX = (i+1)*2;

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

            DecimalFormat formatter = (DecimalFormat)nf;
            formatter.applyPattern("#.##");

            String ranX = formatter.format(randomX);
            String ranY = formatter.format(randomY);

            println(writer, "<nodedata nodepre=\""+dataNode.pre+"\">");
            println(writer, "<position x=\""+ranX+"\" y=\""+ranY+"\" z=\"0.0\"/>");

            if(dataNode.isDummy) {
                println(writer, "<color a=\"1.0\" b=\"0.7\" g=\"0.7\" r=\"0.7\"/>");
            } else {
                println(writer, "<color a=\"1.0\" b=\"0.61568628\" g=\"0.39411766\" r=\"0.31176471\"/>");
            }
            
            println(writer, "<size value=\"5.0\"/>");
            println(writer, "</nodedata>");
        }

        // Edges

        //for (DataEdge dataEdge : dataEdges) {
        for (int i=0;i<dataEdges.size();i++) {
            DataEdge dataEdge = dataEdges.get(i);
            println(writer, "<edgedata sourcepre=\""+dataEdge.node1.pre+"\" targetpre=\""+dataEdge.node2.pre+"\">");
            println(writer, "<color a=\"1.0\" b=\"0.0\" g=\"0.0\" r=\"-1.0\"/>");
            println(writer, "</edgedata>");
        }

        writer.println("</Data>");

    }

    protected void echoEmptyTables(PrintWriter writer) {


        // Attributes
        println(writer, "<attributemodel/>");

        // Attributes
        println(writer, "<attributerows/>");

        // Filter model
        println(writer, "<filtermodel>");
        println(writer, "<queries/>");
        println(writer, "</filtermodel>");

        // Text data
        println(writer, "<textdata/>");

        // Statistics
        println(writer, "<statistics/>");

        // Partition model
        println(writer, "<partitionmodel/>");

        // Layout model
        println(writer, "<layoutmodel>");
        println(writer, "<properties/>");
        println(writer, "</layoutmodel>");

        // Partition model
        println(writer, "<partitionmodel/>");

        // Viz model
        //println(writer, "<vizmodel/>");

        // Preview model
        println(writer, "<previewmodel/>");

    }

    private class sortByNodeId implements java.util.Comparator<DataNode> {

        public int compare(DataNode o1, DataNode o2) {
            int sdif = o1.id - o2.id;
            return sdif;
        }

    }

    private class sortByEdgeId implements java.util.Comparator<DataEdge> {

        public int compare(DataEdge o1, DataEdge o2) {
            int sdif = o1.id - o2.id;
            return sdif;
        }

    }

    protected void echoDhns(PrintWriter writer) {
        //topicTotal = topicMap.getNumTopics();

        println(writer, "<Dhns>");
        println(writer, "<Status directed=\"true\" hierarchical=\"false\" mixed=\"false\" undirected=\"false\"/>");
        println(writer, "<IDGen edge=\""+dataEdges.size()+"\" node=\""+dataNodes.size()+"\"/>");
        println(writer, "<Settings></Settings>"); // Lets try without settings...
        println(writer, "<GraphVersion edge=\""+dataEdges.size()+"\" node=\""+dataNodes.size()+"\"/>");


        // First nodes

        println(writer, "<TreeStructure edgesenabled=\""+dataEdges.size()+"\" edgestotal=\""+dataEdges.size()+"\" nodesenabled=\""+dataNodes.size()+"\" mutualedgesenabled=\"0\" mutualedgestotal=\"0\">");
        println(writer, "<Tree>");

        //Collection<DataNode> nodeColl = dataNodes.values();
        ArrayList<DataNode> nodeList = new ArrayList<DataNode>(dataNodes.values());
        Collections.sort(nodeList, new sortByNodeId());
        //for (DataNode dataNode : nodeColl) {
        for (int i=0;i<nodeList.size();i++) {
            DataNode dataNode = nodeList.get(i);
            println(writer, "<Node enabled=\"true\" id=\""+dataNode.id+"\" parent=\"0\" pre=\""+dataNode.pre+"\" enabledindegree=\"1\" enabledmutualdegree=\"0\" enabledoutdegree=\"0\"/>");
        }

        println(writer, "</Tree>");
        println(writer, "</TreeStructure>");


        // Then edges

        println(writer, "<Edges>");

        //ArrayList<DataEdge> edgeList = new ArrayList<DataEdge>(dataEdges.values());

        //for (DataEdge dataEdge : dataEdges) {
        for (int i=0;i<dataEdges.size();i++) {
            DataEdge dataEdge = dataEdges.get(i);
            if(dataEdge.node1.pre == dataEdge.node2.pre)
            {
                println(writer, "<SelfLoop id=\""+dataEdge.id+"\" source=\""+dataEdge.node1.pre+"\" target=\""+dataEdge.node2.pre+"\" weight=\"2.0\"/>");
            } else {
                println(writer, "<MixedEdge directed=\"true\" id=\""+dataEdge.id+"\" source=\""+dataEdge.node1.pre+"\" target=\""+dataEdge.node2.pre+"\" weight=\"2.0\"/>");
            }
        }

        println(writer, "</Edges>");

        // End
        println(writer, "</Dhns>");

    }

    protected String makeID(Topic t) throws TopicMapException {
        if(t.getBaseName() != null) {
            String bn = t.getBaseName();
            bn = makeID(bn);
            return bn;
        }
        return ""+t.getID();
    }
    protected String makeID(String s) {
        if(s != null && s.length() > 0) {
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<s.length(); i++) {
                if(Character.isJavaIdentifierPart(s.charAt(i))) {
                    sb.append(s.charAt(i));
                }
                else {
                    sb.append('_');
                }
            }
            return sb.toString();
        }
        return s;
    }


    protected String makeString(String s) {
        if(s == null) return null;
        //s = s.substring(0,Math.min(s.length(), 240));
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\\&", "&amp;");
        s = s.replaceAll("\"", "'");
        return s;
    }
    protected String makeString(Topic t) throws TopicMapException {
        if(t == null) return null;
        String s = t.getBaseName();
        if(s == null) s = t.getOneSubjectIdentifier().toExternalForm();
        return makeString(s);
    }



    // -------------------------------------------------------------------------
    // Elementary print methods are used to ensure output is UTF-8

    protected void print(PrintWriter writer, String str) {
        writer.print(str);
    }
    protected void println(PrintWriter writer, String str) {
        writer.println(str);
    }



    private class DataNode {

        int pre;
        int id;

        String label;
        String baseName;
        String si;

        String occurences = "";

        boolean isDummy = false;

        public DataNode(String label, String si) {
            this.id = nodeCounter;
            this.pre = nodeCounter+1;
            //this.id = id;
            //this.pre = id+1;
            this.label = label;
            this.si = si;
            this.baseName = label;

            if(label == null || label.length() < 1) {
                this.label = si;
                this.baseName = si;
            }

            nodeCounter++;
        }

        public DataNode(String label, String si, boolean isDummy) {
            this(label, si);
            this.isDummy = isDummy;
        }

        public void setOccurences(String occurences) {
            this.occurences = occurences;
        }
    }
    

    private class DataEdge {

        DataNode node1;
        DataNode node2;

        String label;

        String type;
        String role1;
        String role2;

        int id;

        public DataEdge(DataNode node1, DataNode node2, String type, String role1, String role2) {
            this.node1 = node1;
            this.node2 = node2;
            this.label = type;
            this.label = type;

            this.type = type;
            this.role1 = role1;
            this.role2 = role2;

            this.id = edgeCounter;

            edgeCounter++;
        }
    }

    
    
    private class AttributeColumn {
        int index;
        String id;
        String title;
        String type;
        String origin;

        public AttributeColumn(int index, String id, String title, String type, String origin) {
            this.index = index;
            this.id = id;
            this.title = title;
            this.type = type;
            this.origin = origin;
        }
    }


}
