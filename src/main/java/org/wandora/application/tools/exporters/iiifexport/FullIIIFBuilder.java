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
package org.wandora.application.tools.exporters.iiifexport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.rdf.AbstractRDFExtractor;
import org.wandora.application.tools.extractors.rdf.rdfmappings.DublinCoreMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.EXIFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.OAMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.SCMapping;
import org.wandora.application.tools.importers.SimpleRDFImport;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class FullIIIFBuilder implements IIIFBuilder {

    @Override
    public Manifest buildIIIF(Wandora wandora, Context context, IIIFExport tool) throws TopicMapException {
        Iterator iter=context.getContextObjects();
        if(!iter.hasNext()) return null;
        Topic topic=(Topic)iter.next();
        
        return buildManifest(topic,tool);
    }
    
    protected void setItem(Object modelObject, String setter, Object value) throws TopicMapException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        boolean languageString=false;
        Method setterMethod=null;
        if(value instanceof Integer){
            try{
                setterMethod=modelObject.getClass().getMethod(setter, Integer.TYPE);
            }
            catch(NoSuchMethodException nsme){
                try{
                    setterMethod=modelObject.getClass().getMethod(setter, Integer.class);
                }
                catch(NoSuchMethodException nsme2){throw nsme;}            
            }
        }
        else {
            try{
                setterMethod=modelObject.getClass().getMethod(setter, String.class);
            }
            catch(NoSuchMethodException nsme){
                try {
                    languageString=true;
                    setterMethod=modelObject.getClass().getMethod(setter, LanguageString.class);
                }
                catch(NoSuchMethodException nsme2){
                    throw nsme;
                }
            }
        }
        
        if(languageString) setterMethod.invoke(modelObject, new LanguageString((String)value));
        else setterMethod.invoke(modelObject, value);            
    }
    protected void copyOccurrenceInteger(Object modelObject, String setter, Topic t, String occurrenceType) throws TopicMapException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        Topic type=t.getTopicMap().getTopic(occurrenceType);
        String o=t.getData(type, (String)null);
        if(o!=null) {
            try{
                int i=Integer.parseInt(o);
                setItem(modelObject, setter, i);
            }catch(NumberFormatException nfe){}
        }
        
    }
    protected void copyOccurrence(Object modelObject, String setter, Topic t, String occurrenceType) throws TopicMapException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        Topic type=t.getTopicMap().getTopic(occurrenceType);
        String o=t.getData(type, (String)null);
        if(o!=null) setItem(modelObject, setter, o);
    }
    protected void copyAssociationString(Object modelObject, String setter, Topic t, String associationType, String roleType) throws TopicMapException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        Topic type=t.getTopicMap().getTopic(associationType);
        Topic role=t.getTopicMap().getTopic(roleType);
        if(type==null || role==null) return;
        ArrayList<Association> as=new ArrayList<>(t.getAssociations(type));
        for(Association a : as){
            Topic p=a.getPlayer(role);
            if(p!=null && !p.mergesWithTopic(t) && p.getBaseName()!=null) {
                setItem(modelObject,setter, p.getBaseName());
                break;
            }
        }
    }
    protected void copyAssociationSI(Object modelObject, String setter, Topic t, String associationType, String roleType) throws TopicMapException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        Topic type=t.getTopicMap().getTopic(associationType);
        Topic role=t.getTopicMap().getTopic(roleType);
        if(type==null || role==null) return;
        ArrayList<Association> as=new ArrayList<>(t.getAssociations(type));
        for(Association a : as){
            Topic p=a.getPlayer(role);
            if(p!=null && !p.mergesWithTopic(t)) {
                setItem(modelObject,setter, p.getOneSubjectIdentifier().toExternalForm());
                break;
            }
        }
    }
    
    protected boolean orderByOrderRole(ArrayList<Association> associations) throws TopicMapException {
        if(associations.isEmpty()) return false;
        TopicMap tm=associations.get(0).getTopicMap();
        final Topic orderTopic=tm.getTopic(AbstractRDFExtractor.RDF_LIST_ORDER);
        if(orderTopic==null) return false;
        Collections.sort(associations,new Comparator<Association>(){
            @Override
            public int compare(Association o1, Association o2) {
                try{
                    Topic t1=o1.getPlayer(orderTopic);
                    Topic t2=o2.getPlayer(orderTopic);
                    if(t1!=null && t2!=null) {
                        String bn1=t1.getBaseName();
                        String bn2=t2.getBaseName();
                        if(bn1!=null && bn2!=null){
                            int i1=-1;
                            int i2=-1;
                            try{ i1=Integer.parseInt(bn1); } catch(NumberFormatException nfe){}
                            try{ i2=Integer.parseInt(bn2); } catch(NumberFormatException nfe){}
                            
                            return i2-i1;
                        }
                        else if(bn1!=null) return -1;
                        else if(bn2!=null) return 1;
                        else return 0;
                    }
                    else if(t1!=null) return -1;
                    else if(t2!=null) return 1;
                    else return 0;
                }catch(TopicMapException tme){
                    return 0;
                }
            }
        });
        return true;
    }
    
    protected Manifest buildManifest(Topic t,IIIFExport tool) throws TopicMapException {
        TopicMap tm=t.getTopicMap();
        Manifest m=new Manifest();
        String name=t.getDisplayName();
        if(name!=null) m.addLabel(new LanguageString(name));
        
        try{
            copyOccurrence(m, "addAttribution", t, SCMapping.SC_NS+"attributionLabel");
            copyOccurrence(m, "addDescription", t, DublinCoreMapping.DC_ELEMENTS_NS+"description");
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            tool.log(e);
        }
        
        m.setId(t.getOneSubjectIdentifier().toExternalForm());        
        
        Topic hasSequences=tm.getTopic(SCMapping.SC_NS+"hasSequences");
        if(hasSequences!=null){
            ArrayList<Association> sequences=new ArrayList<>(t.getAssociations(hasSequences));
            Topic sequenceType=tm.getTopic(SCMapping.SC_NS+"Sequence");
            if(sequenceType!=null){
                orderByOrderRole(sequences);
                for(Association a : sequences){
                    Topic sequence=a.getPlayer(sequenceType);
                    if(sequence!=null){
                        Sequence s=buildSequence(sequence,tool);
                        if(s!=null) m.addSequence(s);
                    }
                }
            }
        }
        
        return m;
    }
    
    protected Sequence buildSequence(Topic t,IIIFExport tool) throws TopicMapException {
        TopicMap tm=t.getTopicMap();
        Sequence s=new Sequence();
        String name=t.getDisplayName();
        if(name!=null) s.addLabel(new LanguageString(name));
        
        try{
            copyOccurrence(s, "addAttribution", t, SCMapping.SC_NS+"attributionLabel");
            copyOccurrence(s, "addDescription", t, DublinCoreMapping.DC_ELEMENTS_NS+"description");
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            tool.log(e);
        }
        
        s.setId(t.getOneSubjectIdentifier().toExternalForm());        
        
        Topic hasCanvases=tm.getTopic(SCMapping.SC_NS+"hasCanvases");
        if(hasCanvases!=null){
            ArrayList<Association> canvases=new ArrayList<>(t.getAssociations(hasCanvases));
            Topic canvasType=tm.getTopic(SCMapping.SC_NS+"Canvas");
            if(canvasType!=null){
                orderByOrderRole(canvases);
                for(Association a : canvases){
                    Topic canvas=a.getPlayer(canvasType);
                    if(canvas!=null){
                        Canvas c=buildCanvas(canvas,tool);
                        if(c!=null) s.addCanvas(c);
                    }
                }
            }
        }
        
        return s;
    }
    
    protected Canvas buildCanvas(Topic t,IIIFExport tool) throws TopicMapException {
        TopicMap tm=t.getTopicMap();
        Canvas c=new Canvas();
        String name=t.getDisplayName();
        if(name!=null) c.addLabel(new LanguageString(name));
        
        try{
            copyOccurrence(c, "addDescription", t, DublinCoreMapping.DC_ELEMENTS_NS+"description");
            
            copyOccurrenceInteger(c, "setWidth", t, EXIFMapping.EXIF_NS+"width");
            copyOccurrenceInteger(c, "setHeight", t, EXIFMapping.EXIF_NS+"height");
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            tool.log(e);
        }
        
        c.setId(t.getOneSubjectIdentifier().toExternalForm());
        
        Topic hasImageAnnotations=tm.getTopic(SCMapping.SC_NS+"hasImageAnnotations");
        if(hasImageAnnotations!=null){
            ArrayList<Association> imageAnnotations=new ArrayList<>(t.getAssociations(hasImageAnnotations));
            Topic imageAnnotationType=tm.getTopic(OAMapping.OA_NS+"Annotation");
            if(imageAnnotationType!=null){
                orderByOrderRole(imageAnnotations);
                for(Association a : imageAnnotations){
                    Topic imageAnnotation=a.getPlayer(imageAnnotationType);
                    if(imageAnnotation!=null){
                        Content co=buildContent(imageAnnotation,tool);
                        if(co!=null) c.addImage(co);
                    }
                }
            }
        }
        
        return c;
    }
    
    protected Content buildContent(Topic t, IIIFExport tool) throws TopicMapException {
        TopicMap tm=t.getTopicMap();
        Content c=new Content();
        String name=t.getDisplayName();
        if(name!=null) c.addLabel(new LanguageString(name));
        
        try{
            copyAssociationString(c, "setMotivation", t, OAMapping.OA_NS+"motivatedBy", OAMapping.OA_NS+"Motivation");
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            tool.log(e);
        }
        
        c.setId(t.getOneSubjectIdentifier().toExternalForm());
        c.setResourceType(Content.RESOURCE_TYPE_IMAGE);
        
        Topic hasBody=tm.getTopic(OAMapping.OA_NS+"hasBody");
        Topic rdfObject=tm.getTopic(RDFMapping.RDF_NS+"object");
        if(hasBody!=null && rdfObject!=null){
            ArrayList<Association> bodies=new ArrayList<>(t.getAssociations(hasBody));
            if(!bodies.isEmpty()) {
                Topic body=bodies.get(0).getPlayer(rdfObject);
                if(body!=null){
                    addContentBody(c, body, tool);
                }
            }
        }
        
        return c;        
    }

    protected void addContentBody(Content c, Topic t, IIIFExport tool) throws TopicMapException {
        TopicMap tm=t.getTopicMap();
        
        try{
            copyOccurrenceInteger(c, "setWidth", t, EXIFMapping.EXIF_NS+"width");
            copyOccurrenceInteger(c, "setHeight", t, EXIFMapping.EXIF_NS+"height");
            copyOccurrence(c, "setFormat", t, DublinCoreMapping.DC_ELEMENTS_NS+"format");
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            tool.log(e);
        }
        
        Locator l=t.getSubjectLocator();
        if(l==null) l=t.getOneSubjectIdentifier();
        c.setResourceId(l.toExternalForm());
        
        Topic hasService=tm.getTopic(SCMapping.SC_NS+"hasRelatedService");
        Topic rdfObject=tm.getTopic(RDFMapping.RDF_NS+"object");
        if(hasService!=null && rdfObject!=null){
            ArrayList<Association> services=new ArrayList<>(t.getAssociations(hasService));
            for(Association a : services){
                Topic service=a.getPlayer(rdfObject);
                if(service!=null){
                    Service s=buildService(service, tool);
                    if(s!=null) c.addResourceService(s);
                }
            }
        }
        
    }

    protected Service buildService(Topic t, IIIFExport tool) throws TopicMapException {
        TopicMap tm=t.getTopicMap();
        Service s=new Service();

        try{
            copyAssociationSI(s, "setProfile", t, DublinCoreMapping.DC_TERMS_NS+"conformsTo", SimpleRDFImport.objectTypeSI);
        }
        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            tool.log(e);
        }
        
        
        s.setId(t.getOneSubjectIdentifier().toExternalForm());
        
        return s;
    }
    
    @Override
    public String getBuilderName() {
        return "Full IIIF Builder";
    }
    
}
