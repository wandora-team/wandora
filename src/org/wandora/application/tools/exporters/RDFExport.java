/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * RDFExport.java
 *
 * Created on 4. heinäkuuta 2006, 13:16
 *
 */

package org.wandora.application.tools.exporters;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.contexts.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.wandora.utils.XMLbox;




/**
 *
 * @author olli
 */
public class RDFExport extends AbstractExportTool {
    
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    
    public RDFExport() {
    }
    public RDFExport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }


    @Override
    public String getName() {
        return "RDF Export";
    }
    
    @Override
    public String getDescription() {
        return "Exports topic map in RDF N3 format. Association roles nor topic names are not exported as RDF has no similar structures.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_rdf.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("RDF N3 Export");
        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){

            setDefaultLogger();        
            File file = chooser.getSelectedFile();
            
            TopicMap tm = null;
            // --- Solve first topic map to be exported
            if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
                tm = makeTopicMapWith(context);
            }
            else {
                tm = solveContextTopicMap(admin, context);
            }
            
            try {
                OutputStream out = new FileOutputStream(file);
                exportRDF(out, tm, this);
                out.flush();
                out.close();
                
                log("Ready.");
                setState(WAIT);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    
    
    public void exportRDF(OutputStream out, TopicMap tm, WandoraToolLogger logger) throws TopicMapException, IOException {
        if(logger == null) logger = this;
        PrintStream writer = new PrintStream(out);
        exportHeader(writer);

        logger.log("Exporting topics.");

        Iterator<Topic> topics=tm.getTopics();
        while(topics.hasNext()){
            Topic t=topics.next();
            exportTopic(writer,t);
        }
        logger.log("Exporting associations.");
        Iterator<Association> associations=tm.getAssociations();
        while(associations.hasNext()){
            Association a=associations.next();
            exportAssociation(writer,a,null);
        }

        exportFooter(writer);
        
        writer.flush();
        writer.close();
    }
    
    
    
    
    
    
    
    
    public static String attribute(String s){
        return XMLbox.cleanForAttribute(s);
    }
    public static String clean(String s){
//        return XMLbox.cleanForXML(s);
        return s.replace("\"","\\\"");
    }
    
    public static Locator getTopicLocator(Topic t) throws TopicMapException {
        Locator si=t.getSubjectLocator();
        if(si==null) si=t.getOneSubjectIdentifier();
        return si;
    }
    
    public static void exportHeader(PrintStream out) throws IOException {
/*        out.println("<?xml version=\"1.0\"?>");
        out.println("<rdf:RDF ");
        out.println("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
        out.println("  xmlns:rdfs=\"http://www.w3.org/2000/02/rdf-schema#\">");*/
        out.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        out.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
    }
    
    public static void exportFooter(PrintStream out) throws IOException {
//        out.println("</rdf:RDF>");
    }
            
    public static void exportTopic(PrintStream out,Topic t) throws IOException,TopicMapException {
        Locator si=getTopicLocator(t);
        
        String id=t.getID();
        
//        out.println("  <rdf:Description rdf:about=\""+attribute(si.toExternalForm())+"\">");
        
        if(t.getBaseName()!=null)
//            out.println("    <rdfs:label>"+clean(t.getBaseName())+"</rdfs:label>");
            out.println("<"+si.toExternalForm()+"> rdfs:label \""+clean(t.getBaseName())+"\" .");
        
        for(Topic type : t.getTypes()){
//            out.println("    <rdf:type rdf:resource=\""+getTopicLocator(type).toExternalForm()+"\"/>");
            out.println("<"+si.toExternalForm()+"> rdf:type <"+getTopicLocator(type).toExternalForm()+"> .");
        }

        for(Topic dataType : t.getDataTypes()){
            Hashtable<Topic,String> data=t.getData(dataType);
            for(Map.Entry<Topic,String> e : data.entrySet()){
                String value=e.getValue();
                if(value!=null && value.length()>0){
/*                    out.println("  <rdf:Statement> ");
                    out.println("      <rdf:subject rdf:resource=\""+attribute(si.toExternalForm())+"\"/>");
                    out.println("      <rdf:predicate rdf:resource=\""+attribute(getTopicLocator(dataType).toExternalForm())+"\"/>");
                    out.println("      <rdf:object>"+clean(value)+"</rdf:object>");
                    out.println("  </rdf:Statement>");*/
                    out.println("<"+si.toExternalForm()+"> <"+getTopicLocator(dataType).toExternalForm()+"> \""+clean(value)+"\" .");
                    break;
                }
            }
        }
        
//        out.println("  </rdf:Description>");        
    }
    
    public static int associationCounter=0;
    
    public static void exportAssociation(PrintStream out,Association a,Topic subjectRole) throws IOException,TopicMapException{
        if(a.getRoles().size()==2){
            if(subjectRole==null){
                Vector<Topic> v=new Vector<Topic>();
                v.addAll(a.getRoles());
                Collections.sort(v,new Comparator<Topic>(){
                    @Override
                    public int compare(Topic a,Topic b){
                        try {
                            String ab=a.getBaseName();
                            String bb=b.getBaseName();
                            if(ab==null) ab="";
                            if(bb==null) bb="";
                            return ab.compareTo(bb);
                        }
                        catch(TopicMapException tme){
                            return 0;
                        }
                    }
                });
                subjectRole=v.firstElement();
            }
            Topic subject=a.getPlayer(subjectRole);
            Topic predicate=a.getType();
            Topic object=null;
            for(Topic t : a.getRoles()){
                if(t!=subjectRole){
                    object=a.getPlayer(t);
                    break;
                }
            }
/*            out.println("  <rdf:Statement> ");
            out.println("      <rdf:subject rdf:resource=\""+attribute(getTopicLocator(subject).toExternalForm())+"\"/>");
            out.println("      <rdf:predicate rdf:resource=\""+attribute(getTopicLocator(predicate).toExternalForm())+"\"/>");
            out.println("      <rdf:object rdf:resource=\""+attribute(getTopicLocator(object).toExternalForm())+"\"/>");
            out.println("  </rdf:Statement>");*/
            out.println("<"+getTopicLocator(subject).toExternalForm()+"> <"+
                            getTopicLocator(predicate).toExternalForm()+"> <"+
                            getTopicLocator(object).toExternalForm()+"> .");
        }
        else{
            String about="http://wandora.org/si/rdfexport/association/"+(associationCounter++);
            for(Topic t : a.getRoles()){
/*                out.println("  <rdf:Statement> ");
                out.println("      <rdf:subject rdf:resource=\""+attribute(about)+"\"/>");
                out.println("      <rdf:predicate rdf:resource=\""+attribute(getTopicLocator(t).toExternalForm())+"\"/>");
                out.println("      <rdf:object rdf:resource=\""+attribute(getTopicLocator(a.getPlayer(t)).toExternalForm())+"\"/>");
                out.println("  </rdf:Statement>");*/
                out.println("<"+about+"> <"+
                                getTopicLocator(t).toExternalForm()+"> <"+
                                getTopicLocator(a.getPlayer(t)).toExternalForm()+"> .");                
            }
        }
    }
    
}
