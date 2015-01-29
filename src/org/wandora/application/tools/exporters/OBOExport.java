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
 * */


package org.wandora.application.tools.exporters;

import org.wandora.topicmap.TMBox;
import org.wandora.application.tools.importers.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.contexts.*;
import java.io.*;
import java.util.*;
import javax.swing.*;



/**
 *
 * @author akivela
 */
public class OBOExport extends AbstractExportTool {

   
    
    /** Creates a new instance of RDFExport */
    public OBOExport() {
    }
    
    @Override
    public String getName() {
        return "OBO Export";
    }
    
    @Override
    public String getDescription() {
        return "Exports specific topic map structures in OBO flat file format.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createExportType();
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_obo.png");
    }

    @Override
    public void initialize(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        String o=options.get(OBO.optionPrefix+"options");
        if(o!=null){
            int i=Integer.parseInt(o);
            OBO.setOptions(i);
        }
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        //System.out.println(prefix);
        OBOConfiguration dialog=new OBOConfiguration(admin,true);
        dialog.setOptions(OBO.getOptions());
        dialog.setVisible(true);
        if(!dialog.wasCancelled()){
            int i=dialog.getOptions();
            OBO.setOptions(i);
            options.put(OBO.optionPrefix+"options",""+i);
        }
    }
    
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
        options.put(prefix+"OBO.optionPrefix",""+OBO.getOptions());
    }  
    
    
    
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        String namespace = WandoraOptionPane.showInputDialog(admin, "Give OBO namespace to export", "", "OBO namespace");
        if(namespace == null || namespace.trim().length() == 0) return;
        
        String[] namespaces = null;
        if(namespace.indexOf(",") != -1) {
            namespaces = namespace.split(",");
            for(int i=0; i<namespaces.length; i++) {
                namespaces[i] = namespaces[i].trim();
            }
        }
        else {
            namespaces = new String[] { namespace };
        }
        
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("OBO Export");
        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();        
            File file = chooser.getSelectedFile();
            TopicMap tm=solveContextTopicMap(admin,context);
            exportOBO(file, namespaces, tm);
            setState(WAIT);
        }
        
    }
    
    
    
    
    public void exportOBO(File file, String[] namespaces, TopicMap tm) {
        try{
            PrintStream out=new PrintStream(file);
            
            exportHeader(out, namespaces, tm);
            exportTypedefs(out, namespaces, tm);
            exportTerms(out, namespaces, tm);
            exportInstances(out, namespaces, tm);

            out.close();
            log("Done.");
        }
        catch(Exception e){
            log(e);
        }
    }
    
    
    
    
    public void exportHeader(PrintStream out, String[] namespaces, TopicMap tm) {
        int headerCount = 0;
        try {
            for(int i=0; i<namespaces.length; i++) {
                String namespace = namespaces[i];
                if(namespace != null && namespace.length() > 0) {
                    Topic headerTopic = OBO.getHeaderTopic(tm, namespace);
                    if(headerTopic != null) {
                        if(headerCount==0) {
                            log("Exporting header for namespace '"+namespace+"'.");
                            headerCount++;
                            ArrayList<String> orederedTags = new ArrayList<String>();
                            orederedTags.add("format-version");
                            orederedTags.add("data-version");
                            orederedTags.add("date");
                            orederedTags.add("saved-by");
                            orederedTags.add("auto-generated-by");
                            orederedTags.add("import");
                            
                            ArrayList<String> orederedTags2 = new ArrayList<String>();
                            orederedTags2.add("synonymtypedef");
                            orederedTags2.add("default-namespace");
                            orederedTags2.add("remark");

                            String tag = null;
                            String value = null;

                            for(String otag : orederedTags) {
                                Topic tagTopic = OBO.getTopicForSchemaTerm(tm, otag);
                                if(tagTopic != null) {
                                    value = headerTopic.getData(tagTopic, OBO.LANG);
                                    exportHeaderTagValue(out, otag, value);
                                }
                            }

                            Topic cetegoryTopic = OBO.getCategoryTopic(tm, namespace);
                            if(cetegoryTopic != null) {
                                Collection<Topic> categories = tm.getTopicsOfType(cetegoryTopic);
                                if(categories != null && categories.size() > 0) {
                                    for(Topic category : categories) {
                                        if(category != null && !category.isRemoved()) {
                                            String cid = OBO.solveOBOId(category);
                                            String description = OBO.solveOBODescription(category);
                                            if(cid != null && cid.length() > 0) {
                                                out.print("subsetdef: "+cid);
                                                if(description != null && description.length() >0) {
                                                    out.print(" \""+description+"\"");
                                                }
                                                out.println();
                                            }
                                        }
                                    }
                                }
                            }
                            
                            for(String otag : orederedTags2) {
                                Topic tagTopic = OBO.getTopicForSchemaTerm(tm, otag);
                                if(tagTopic != null) {
                                    value = headerTopic.getData(tagTopic, OBO.LANG);
                                    exportHeaderTagValue(out, otag, value);
                                }
                            }
                            
                            Collection<Topic> occurrenceTypes = headerTopic.getDataTypes();
                            for(Topic occurrenceType : occurrenceTypes) {
                                tag = occurrenceType.getBaseName();
                                if(tag != null && tag.length() > 0 && !orederedTags.contains(tag) && !orederedTags2.contains(tag)) {
                                    value = headerTopic.getData(occurrenceType, OBO.LANG);
                                    exportHeaderTagValue(out, tag, value);
                                }
                            }
                        }
                        else {
                            log("Header already processed! More than one header available! Only first header exported.");
                        }
                    }
                    else {
                        log("Can't find header for namespace '"+namespace+"'.");
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    
    public void exportTerms(PrintStream out, String[] namespaces, TopicMap tm) {
        try {
            HashSet<String> processedIds = new HashSet<String>();
            Topic definitionType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION);
                
            for(int i=0; i<namespaces.length; i++) {
                String namespace = namespaces[i];
                Topic termtype = OBO.getTermTopic(tm, namespace);
                if(termtype != null) {
                    log("Exporting terms in namespace '"+namespace+"'.");
                    String id = null;
                    String name = null;
                    String definition = null;

                    Collection<Topic> terms = tm.getTopicsOfType(termtype);
                    Collection<Topic> sortedTerms = TMBox.sortTopicsByData(terms,OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ID),OBO.LANG);

                    int count = sortedTerms.size();
                    int c = 0;
                    for(Topic term : sortedTerms) {
                        try {
                            setProgress((++c * 100) / count);
                            if(term != null && !term.isRemoved()) {
                                id = OBO.solveOBOId(term);
                                if(processedIds.contains(id)) continue;
                                processedIds.add(id);
                                name = OBO.solveOBOName(term);
                                if(id != null && id.length() > 0) {
                                    out.println();
                                    out.println("[Term]");
                                    out.println("id: "+id);

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_ANONYMOUS, "is_anonymous");                      

                                    // ******* NAME ********
                                    if(name != null && name.length() > 0) {
                                        out.println("name: "+name);
                                    }

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_NAMESPACE, "namespace");

                                    // ***** ALT-IDS ******
                                    exportAltIds(out, tm, term, id);


                                    // ******* DEFINITION ********
                                    exportDefinition(out, tm, term);

                                    // ***** COMMENT *****
                                    exportComment(out, tm, term);
                                   

                                    // ******* SUBSETS *******
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_CATEGORY, OBO.SCHEMA_TERM_CATEGORY, "subset");

                                    
                                    
                                    // ****** SYNONYMS ******
                                    exportSynonyms(out, tm, term);
                                    

                                    //******* XREFS ********
                                    exportXrefs(out, tm, term);
                                    

                                    // ****** RELATIONS ******
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_SUPERCLASS_SUBCLASS, OBO.SCHEMA_TERM_SUPERCLASS, "is_a");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_INTERSECTION_OF, OBO.SCHEMA_TERM_RELATED_TO, "intersection_of");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_UNION_OF, OBO.SCHEMA_TERM_RELATED_TO, "union_of");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_DISJOINT_FROM, OBO.SCHEMA_TERM_RELATED_TO, "disjoint_from");


                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_RELATIONSHIP, OBO.SCHEMA_TERM_RELATED_TO, "relationship");

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_PART_OF, OBO.SCHEMA_TERM_RELATED_TO, "part_of");

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_ADJACENT_TO, OBO.SCHEMA_TERM_RELATED_TO, "adjacent_to");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_LOCATED_IN, OBO.SCHEMA_TERM_RELATED_TO, "located_in");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_CONTAINS, OBO.SCHEMA_TERM_RELATED_TO, "contains");

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_TRANSFORMATION_OF, OBO.SCHEMA_TERM_RELATED_TO, "transformation_of");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_DERIVES_FROM, OBO.SCHEMA_TERM_RELATED_TO, "derives_from");

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_AGENT_IN, OBO.SCHEMA_TERM_RELATED_TO, "agent_in");



                                    // ******* REST OF RELATIONSHIPS *******
                                    Topic typedefType = OBO.getTypedefTopic(tm, namespace);
                                                                       
                                    if(typedefType != null) {
                                        Collection<Topic> typedefs = tm.getTopicsOfType(typedefType);
                                        if(typedefs != null && typedefs.size() > 0) {
                                            for(Topic typedef : typedefs) {
                                                exportRelationships(out, tm, term, typedef.getBaseName(), OBO.SCHEMA_TERM_RELATED_TO, typedef.getBaseName());
                                            }
                                        }
                                    }



                                    // ***** IS OBSOLETE *****
                                    Topic isObsoleteType = OBO.getObsoleteTopic(tm, namespace);
                                    if(isObsoleteType != null) {
                                        if(term.isOfType(isObsoleteType)) {
                                            out.println("is_obsolete: true");
                                        }
                                    }

                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_REPLACED_BY, OBO.SCHEMA_TERM_REPLACED_BY, "replaced_by");
                                    exportRelations(out, tm, term, OBO.SCHEMA_TERM_CONSIDER_USING, OBO.SCHEMA_TERM_CONSIDER_USING, "consider");
                                    
                                    exportCreatorAndCreationDate(out, tm, term);
                                }
                            }
                            if(forceStop()) {
                                return;
                            }
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                }
                else {
                    log("Can't find terms for namespace '"+namespace+"'.");
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    public void exportInstances(PrintStream out, String[] namespaces, TopicMap tm) {
        try {
            if(namespaces != null && namespaces.length > 0) {
                HashSet<String> processedIds = new HashSet<String>();
                for(int i=0; i<namespaces.length; i++) {
                    
                    String namespace = namespaces[i];
                    if(namespace != null && namespace.length() > 0) {
                        Topic typeTopic = OBO.getInstanceTopic(tm, namespace);

                        if(typeTopic != null) {
                            log("Exporting instances in namespace '"+namespace+"'.");
                            String id = null;
                            String name = null;
                            String definition = null;

                            Topic definitionType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION);
                            Collection<Topic> terms = tm.getTopicsOfType(typeTopic);
                            Collection<Topic> sortedTerms = TMBox.sortTopicsByData(terms,OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ID),OBO.LANG);

                            int count = terms.size();
                            int c = 0;
                            for(Topic term : sortedTerms) {
                                try {
                                    setProgress((++c * 100) / count);
                                    if(term != null && !term.isRemoved()) {
                                        id = OBO.solveOBOId(term);
                                        if(processedIds.contains(id)) continue;
                                        processedIds.add(id);
                                        name = OBO.solveOBOName(term);
                                        if(id != null && id.length() > 0) {
                                            out.println();
                                            out.println("[Instance]");
                                            out.println("id: "+id);

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_ANONYMOUS, "is_anonymous");                          

                                            // ****** NAME *******
                                            if(name != null && name.length() > 0) {
                                                out.println("name: "+name);
                                            }

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_NAMESPACE, "namespace");

                                            // ***** ALT-IDS ******
                                            exportAltIds(out, tm, term, id);

                                            
                                            // ******* DEFINITION ********
                                            exportDefinition(out, tm, term);


                                            // ***** COMMENT *****
                                            exportComment(out, tm, term);


                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_CATEGORY, OBO.SCHEMA_TERM_CATEGORY, "subset");


                                             // ****** SYNONYMS ******
                                            exportSynonyms(out, tm, term);



                                            //******* XREFS ********
                                            exportXrefs(out, tm, term);


                                            // ****** RELATIONS ******
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_INSTANCE_OF, "instance_of");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_PROPERTY_VALUE, "property_value");

                                            // ***** IS OBSOLETE *****
                                            Topic isObsoleteType = OBO.getObsoleteTopic(tm, namespace);
                                            if(isObsoleteType != null) {
                                                if(term.isOfType(isObsoleteType)) {
                                                    out.println("is_obsolete: true");
                                                }
                                            }                            
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_REPLACED_BY, "replaced_by");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_CONSIDER_USING, "consider");
                                            
                                            //******* PROPERTIES ********
                                            exportProperties(out, tm, term);
                                        }
                                    }
                                    if(forceStop()) {
                                        return;
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        else {
                            log("Can't find instances for namespace '"+namespace+"'.");
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    public void exportTypedefs(PrintStream out, String[] namespaces, TopicMap tm) {
        try {
            if(namespaces != null && namespaces.length > 0) {
                HashSet<String> processedIds = new HashSet<String>();
                for(int i=0; i<namespaces.length; i++) {
                    String namespace = namespaces[i];
                    if(namespace!=null && namespace.length() >0) {
                        Topic typeTopic = OBO.getTypedefTopic(tm, namespace);

                        if(typeTopic != null) {
                            log("Exporting typedefs in namespace '"+namespace+"'.");
                            String id = null;
                            String name = null;
                            String definition = null;

                            Topic definitionType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION);
                            Collection<Topic> terms = tm.getTopicsOfType(typeTopic);
                            Collection<Topic> sortedTerms = TMBox.sortTopicsByData(terms,OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ID),OBO.LANG);

                            int c = 0;
                            int count = sortedTerms.size();
                            for(Topic term : sortedTerms) {
                                try {
                                    setProgress((++c * 100) / count);
                                    if(term != null && !term.isRemoved()) {
                                        id = OBO.solveOBOId(term);
                                        if(processedIds.contains(id)) continue;
                                        processedIds.add(id);
                                        name = OBO.solveOBOName(term);
                                        if(id != null && id.length() > 0) {
                                            out.println();
                                            out.println("[Typedef]");
                                            out.println("id: "+id);

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_ANONYMOUS, "is_anonymous");

                                            if(name != null && name.length() > 0) {
                                                out.println("name: "+name);
                                            }

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_NAMESPACE, OBO.SCHEMA_TERM_NAMESPACE, "namespace");

                                            
                                            // ***** ALT-IDS ******
                                            exportAltIds(out, tm, term, id);

                                            
                                            
                                            // ******* DEFINITION ********
                                            exportDefinition(out, tm, term);


                                            // ***** COMMENT *****
                                            exportComment(out, tm, term);


                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_CATEGORY, "subset");

                                            
                                             // ****** SYNONYMS ******
                                            exportSynonyms(out, tm, term);



                                            //******* XREFS ********
                                            exportXrefs(out, tm, term);


                                            // ****** RELATIONS ******
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_DOMAIN, "domain");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_RANGE, "range");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_ANTISYMMETRIC, "is_anti_symmetric");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_CYCLIC, "is_cyclic");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_REFLEXIVE, "is_reflexive");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_SYMMETRIC, "is_symmetric");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_TRANSITIVE, "is_transitive");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_METADATA_TAG, "is_metadata_tag");

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_SUPERCLASS_SUBCLASS, OBO.SCHEMA_TERM_SUPERCLASS, "is_a");

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_INVERSE_OF, "inverse_of");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_IS_TRANSITIVE_OVER, "transitive_over");


                                            /*
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_RELATIONSHIP, OBO.SCHEMA_TERM_RELATED_TERM, "relationship");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_INTERSECTION_OF, OBO.SCHEMA_TERM_INTERSECTION_OF, "intersection_of");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_PART_OF, OBO.SCHEMA_TERM_HAS_PART, "part_of");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_DISJOINT_FROM, OBO.SCHEMA_TERM_DISJOINT_FROM, "disjoint_from");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_ADJACENT_TO, OBO.SCHEMA_TERM_ADJACENT_TO, "adjacent_to");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_LOCATED_IN, OBO.SCHEMA_TERM_LOCATED_IN, "located_in");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_CONTAINS, OBO.SCHEMA_TERM_CONTAINED_IN, "contains");

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_TRANSFORMATION_OF, OBO.SCHEMA_TERM_TRANSFORMATION_OF, "transformation_of");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_DERIVES_FROM, OBO.SCHEMA_TERM_DERIVES_FROM, "derives_from");

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_AGENT_IN, OBO.SCHEMA_TERM_AGENT_IN, "agent_in");
                                            */


                                            // ******* REST OF RELATIONSHIPS *******
                                            /*
                                            Topic typedefType = OBO.getTypedefTopic(tm, namespace);
                                            if(typedefType != null) {
                                                Collection<Topic> typedefs = tm.getTopicsOfType(typedefType);
                                                if(typedefs != null && typedefs.size() > 0) {
                                                    for(Topic typedef : typedefs) {
                                                        exportRelationships(out, tm, term, typedef.getBaseName(), OBO.SCHEMA_TERM_RELATED_TERM, typedef.getBaseName());
                                                    }
                                                }
                                            }
                                            */

                                            // ***** IS OBSOLETE *****
                                            Topic isObsoleteType = OBO.getObsoleteTopic(tm, namespace);
                                            if(isObsoleteType != null) {
                                                if(term.isOfType(isObsoleteType)) {
                                                    out.println("is_obsolete: true");
                                                }
                                            }

                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_REPLACED_BY, OBO.SCHEMA_TERM_REPLACED_BY, "replaced_by");
                                            exportRelations(out, tm, term, OBO.SCHEMA_TERM_CONSIDER_USING, OBO.SCHEMA_TERM_CONSIDER_USING, "consider");

                                        }
                                    }
                                    if(forceStop()) {
                                        return;
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        else {
                            log("Can't find typedefs for namespace '"+namespace+"'.");
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    protected void exportRelationships(PrintStream out, TopicMap tm, Topic term, String atype, String arole, String modifier) {
        try {
            Topic atypeTopic = OBO.getTopicForSchemaTerm(tm, atype);
            Topic atypeRole = OBO.getTopicForSchemaTerm(tm, arole);
            if(atypeTopic != null && atypeRole != null) {
                Collection<Topic> relatedTerms = TopicTools.getPlayers(term, atypeTopic, atypeRole);
                if(relatedTerms != null && relatedTerms.size() > 0) {
                    String id = null;
                    String name = null;
                    for(Topic relatedTerm : relatedTerms) {
                        if(!term.mergesWithTopic(relatedTerm)) {
                            id = OBO.solveOBOId(relatedTerm);
                            name = OBO.solveOBOName(relatedTerm);
                            if(id != null && id.length() > 0) {
                                out.print("relationship: "+modifier+" "+id);
                                if(name != null && name.length() > 0) {
                                    out.print(" ! "+name);
                                }
                                out.println();
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    protected void exportRelations(PrintStream out, TopicMap tm, Topic term, String atype, String oboTerm) {
        exportRelations(out, tm, term, atype, atype, oboTerm);
    }

    protected void exportRelations(PrintStream out, TopicMap tm, Topic term, String atype, String arole, String oboTerm) {
        try {
            Topic atypeTopic = OBO.getTopicForSchemaTerm(tm, atype);
            Topic aroleTopic = OBO.getTopicForSchemaTerm(tm, arole);
            Topic modifierRole = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_MODIFIER);
            if(atypeTopic != null && aroleTopic != null) {
                Collection<Association> relations = term.getAssociations(atypeTopic);
                if(relations != null && relations.size() > 0) {
                    String id = null;
                    String name = null;
                    String modifier = null;
                    if(modifierRole != null) relations = TMBox.sortAssociations(relations, OBO.LANG, modifierRole);
                    for(Association relation : relations) {
                        Topic relatedTerm = relation.getPlayer(aroleTopic);
                        if(relatedTerm != null && !term.mergesWithTopic(relatedTerm)) {
                            id = OBO.solveOBOId(relatedTerm);
                            name = OBO.solveOBOName(relatedTerm);
                            if(id != null && id.length() > 0) {
                                out.print(oboTerm+": ");
                                if(modifierRole != null) {
                                    Topic modifierTopic = relation.getPlayer(modifierRole);
                                    if(modifierTopic != null) {
                                        out.print(modifierTopic.getBaseName()+" ");
                                    }
                                }
                                out.print(id);
                                if(name != null && name.length() > 0) {
                                    out.print(" ! "+name);
                                }
                                out.println();
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    protected void exportHeaderTagValue(PrintStream out, String tag, String value) {
        String values[] = null;
        if(value != null && value.length() > 0) {
            if(value.indexOf('\n') > -1) {
                values = value.split("\n");
                for(int i=0; i<values.length; i++) {
                    value = values[i];
                    if(value != null && value.length() > 0) {
                        out.println(tag+": "+value);
                    }
                }
            }
            else {
                out.println(tag+": "+value);
            }
        }
    }
    
    
    
    
    protected void exportXrefs(PrintStream out, TopicMap tm, Topic term) {
        try {
            if(OBO.USE_SCOPED_XREFS) {
                Topic xrefType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_XREF);
                Topic xrefScope = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_XREF_SCOPE);
                if(xrefType != null) {
                    Collection<Association> xrefAssociations = term.getAssociations(xrefType);
                    String xrefId = null;
                    String xrefDescription = null;
                    for(Association xrefAssociation : xrefAssociations) {
                        if(xrefAssociation != null) {
                            Topic xref = xrefAssociation.getPlayer(xrefType);
                            Topic scope = (xrefScope != null ? xrefAssociation.getPlayer(xrefScope) : null );
                            if(xref != null && !xref.isRemoved()) {
                                if(!term.mergesWithTopic(xref)) {
                                    xrefId = OBO.solveOBOId(xref);
                                    xrefDescription = OBO.solveOBODescription(xrefAssociation, xref);
                                    if(xrefId != null && xrefId.length() > 0) {
                                        boolean scopeUsed = false;
                                        if(scope != null) {
                                            if("ANALOG".equals(scope.getBaseName())) {
                                                out.print("xref_analog: "+xrefId);
                                                scopeUsed = true;
                                            }
                                            else if("UNKNOWN".equals(scope.getBaseName())) {
                                                out.print("xref_unknown: "+xrefId);
                                                scopeUsed = true;
                                            }
                                        }
                                        if(!scopeUsed) {
                                            out.print("xref: "+xrefId);
                                        }
                                        if(xrefDescription != null && xrefDescription.length() > 0) {
                                            out.print(" \""+xrefDescription+"\"");
                                        }
                                        out.println();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                Topic xrefType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_XREF);
                if(xrefType != null) {
                    Collection<Association> xrefs = term.getAssociations(xrefType); //TopicTools.getPlayers(term, OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_XREF), OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_XREF));
                    if(xrefs != null && xrefs.size() > 0) {
                        String xrefId = null;
                        String xrefDescription = null;
                        Topic xref = null;
                        for(Association xrefAssociation : xrefs) {
                            xref = xrefAssociation.getPlayer(xrefType);
                            if(!term.mergesWithTopic(xref)) {
                                xrefId = OBO.solveOBOId(xref);
                                xrefDescription = OBO.solveOBODescription(xrefAssociation, xref);
                                if(xrefId != null && xrefId.length() > 0) {
                                    out.print("xref: "+xrefId);
                                    if(xrefDescription != null && xrefDescription.length() > 0) {
                                        out.print(" \""+xrefDescription+"\"");
                                    }
                                    out.println();
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    protected void exportSynonyms(PrintStream out, TopicMap tm, Topic term) {
        try {
            Topic synonymAssociationType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_SYNONYM);
            if(synonymAssociationType != null) {
                Collection<Association> synonymAssociations = term.getAssociations(synonymAssociationType);
                if(synonymAssociations != null && synonymAssociations.size() > 0) {
                    Topic synonymTypeRole = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_SYNONYM_TYPE);
                    Topic synonymScopeRole = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_SYNONYM_SCOPE);
                    Topic synonymTextRole = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_SYNONYM);
                    Topic synonymOriginRole = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_SYNONYM_ORIGIN);

                    if(OBO.COLLATE_SIMILAR_SYNONYMS) {
                    
                        HashMap<String,ArrayList<Association>> groupedSynonyms = new HashMap<String,ArrayList<Association>>();
                        for(Association synonymAssociation : synonymAssociations) {
                            Topic synonymText = (synonymTextRole != null ? synonymAssociation.getPlayer(synonymTextRole) : null);
                            Topic synonymType = (synonymTypeRole != null ? synonymAssociation.getPlayer(synonymTypeRole) : null);
                            Topic synonymScope = (synonymScopeRole != null ? synonymAssociation.getPlayer(synonymScopeRole) : null);
                            String key = synonymText+":::"+synonymType+"::::"+synonymScope;
                            
                            ArrayList<Association> synonymGroup = groupedSynonyms.get(key);
                            if(synonymGroup == null) {
                                synonymGroup =  new ArrayList<Association>();
                                synonymGroup.add(synonymAssociation);
                                groupedSynonyms.put(key,synonymGroup);
                            }
                            else {
                                synonymGroup.add(synonymAssociation);
                            }
                        }

                        for(Iterator<String> groupKeys = groupedSynonyms.keySet().iterator(); groupKeys.hasNext(); ) {
                            HashSet usedOrigins = new HashSet();
                            String groupKey = groupKeys.next();
                            ArrayList<Association> synonymGroup = groupedSynonyms.get(groupKey);
                            boolean isFirst = true;
                            boolean scopeAndTypeProcessed = false;
                            boolean isFirstOrigin = true;
                            for(Association synonymAssociation : synonymGroup) {
                                Topic synonymText = (synonymTextRole != null ? synonymAssociation.getPlayer(synonymTextRole) : null);
                                Topic synonymType = (synonymTypeRole != null ? synonymAssociation.getPlayer(synonymTypeRole) : null);
                                Topic synonymScope = (synonymScopeRole != null ? synonymAssociation.getPlayer(synonymScopeRole) : null);
                                Topic synonymOrigin = (synonymOriginRole != null ? synonymAssociation.getPlayer(synonymOriginRole) : null);
                                if(isFirst) {
                                    out.print("synonym: \""+OBO.Java2OBO(synonymText.getDisplayName(OBO.LANG))+"\"");
                                    isFirst = false;
                                }
                                if(!scopeAndTypeProcessed) {
                                    if(synonymScope != null) {
                                        out.print(" "+synonymScope.getBaseName());
                                    }
                                    if(synonymType != null) {
                                        out.print(" "+synonymType.getBaseName());
                                    }
                                    scopeAndTypeProcessed = true;
                                    out.print(" [");
                                }
                                if(synonymOrigin != null) {
                                    String oid = OBO.solveOBOId(synonymOrigin);
                                    if(!usedOrigins.contains(oid)) {
                                        usedOrigins.add(oid);
                                        String odescription = OBO.solveOBODescription(synonymAssociation, synonymOrigin);
                                        if(oid != null && oid.length() > 0) {
                                            if(isFirstOrigin) {
                                                isFirstOrigin = false;
                                            }
                                            else {
                                                out.print(", ");
                                            }
                                            out.print(oid);
                                            if(odescription != null && odescription.length() > 0) {
                                                out.print(" \""+odescription+"\"");
                                            }
                                        }
                                    }
                                }
                            }
                            out.print("]");
                            out.println();
                        }
                    }
                    else {
                        synonymAssociations = TMBox.sortAssociations(synonymAssociations, OBO.LANG, synonymOriginRole);
                        synonymAssociations = TMBox.sortAssociations(synonymAssociations, OBO.LANG, synonymTextRole);
                        for(Association synonymAssociation : synonymAssociations) {
                            Topic synonymType = (synonymTypeRole != null ? synonymAssociation.getPlayer(synonymTypeRole) : null);
                            Topic synonymScope = (synonymScopeRole != null ? synonymAssociation.getPlayer(synonymScopeRole) : null);
                            Topic synonymText = (synonymTextRole != null ? synonymAssociation.getPlayer(synonymTextRole) : null);
                            Topic synonymOrigin = (synonymOriginRole != null ? synonymAssociation.getPlayer(synonymOriginRole) : null);

                            if(synonymText != null) {
                                out.print("synonym: \""+OBO.Java2OBO(synonymText.getDisplayName(OBO.LANG))+"\"");
                                if(synonymScope != null) {
                                    out.print(" "+synonymScope.getBaseName());
                                }
                                if(synonymType != null) {
                                    out.print(" "+synonymType.getBaseName());
                                }

                                out.print(" [");
                                boolean isFirst = true;
                                if(synonymOrigin != null) {
                                    String oid = OBO.solveOBOId(synonymOrigin);
                                    String odescription = OBO.solveOBODescription(synonymAssociation, synonymOrigin);
                                    if(oid != null && oid.length() > 0) {
                                        if(isFirst) {
                                            isFirst = false;
                                        }
                                        else {
                                            out.print(", ");
                                        }
                                        out.print(oid);
                                        if(odescription != null && odescription.length() > 0) {
                                            out.print(" \""+odescription+"\"");
                                        }
                                    }
                                }
                                out.print("]");
                                out.println();
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    protected void exportCreatorAndCreationDate(PrintStream out, TopicMap tm, Topic term) {
        try {
            Topic creatorTypeTopic = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_CREATED_BY);
            if(creatorTypeTopic != null) {
                Collection<Association> creatorRelations = term.getAssociations(creatorTypeTopic);
                if(creatorRelations != null) {
                    for(Association creatorRelation : creatorRelations) {
                        Topic creatorTopic = creatorRelation.getPlayer(creatorTypeTopic);
                        if(creatorTopic != null) {
                            String creatorName = OBO.solveOBOName(creatorTopic);
                            if(creatorName != null && creatorName.trim().length() > 0) {
                                out.println("created_by: "+creatorName+ " ! " +creatorName);
                            }
                        }
                    }
                }
            }
            
            Topic creationDateType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_CREATION_DATE);
            if(creationDateType != null) {
                String creationDate = term.getData(creationDateType, OBO.LANG);
                if(creationDate != null) {
                    creationDate = OBO.Java2OBOLite(creationDate);
                    out.println("creation_date: "+creationDate);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    protected void exportComment(PrintStream out, TopicMap tm, Topic term) {
        try {
            Topic commentType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_COMMENT);
            if(commentType != null) {
                String comment = term.getData(commentType, OBO.LANG);
                if(comment != null) {
                    comment = OBO.Java2OBOLite(comment);
                    out.println("comment: "+comment);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    protected void exportAltIds(PrintStream out, TopicMap tm, Topic term, String id) {
        try {
            if(OBO.MAKE_SIS_FROM_ALT_IDS) {
                Collection<Locator> sis = term.getSubjectIdentifiers();
                if(sis != null && sis.size() > 1) {
                    String ls = null;
                    String altId = null;
                    for(Locator l : sis) {
                        altId = OBO.solveOBOId(l);
                        if(altId != null && altId.length() > 0) {
                            if(!altId.equals(id))
                                out.println("alt_id: "+altId);
                        }
                    }
                }
            }
            else {
                exportRelations(out, tm, term, OBO.SCHEMA_TERM_ALTERNATIVE_ID, "alt_id");
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    protected void exportDefinition(PrintStream out, TopicMap tm, Topic term) {
        try {
            Topic definitionType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION);
            if(definitionType != null) {
                String definition = term.getData(definitionType, OBO.LANG);
                if(definition != null) {
                    definition = OBO.Java2OBOLite(definition);
                    out.print("def: \""+definition+"\"");
                    Topic defOriginType = OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION_ORIGIN);
                    out.print(" [");
                    if(defOriginType != null) {
                        Collection<Association> definitionOrigins = term.getAssociations(defOriginType); // TopicTools.getPlayers(term, , OBO.getTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION_ORIGIN)).toArray( new Topic[] {} );
                        if(definitionOrigins.size()>0) {
                            boolean isFirst = true;
                            Topic dot = null;
                            String doid = null;
                            String dodescription = null;
                            for(Association defOrigin : definitionOrigins) {
                                dot = defOrigin.getPlayer(defOriginType);
                                doid = OBO.solveOBOId(dot);
                                dodescription = OBO.solveOBODescription(defOrigin, dot);
                                if(doid != null && doid.length() > 0) {
                                    if(isFirst) {
                                        isFirst = false;
                                    }
                                    else {
                                        out.print(", ");
                                    }
                                    out.print(doid);
                                    if(dodescription != null && dodescription.length() > 0) {
                                        dodescription = OBO.Java2OBOLite(dodescription);
                                        out.print(" \""+dodescription+"\"");
                                    }
                                }
                            }
                        }
                    }
                    out.print("]");
                    out.println();
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    protected void exportProperties(PrintStream out, TopicMap tm, Topic term) {
        try {

        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
}
