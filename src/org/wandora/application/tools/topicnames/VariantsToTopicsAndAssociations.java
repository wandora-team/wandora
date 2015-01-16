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
 */



package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;
import org.wandora.utils.Tuples.T2;


/**
 * A tool inspired by Patrick Durusau.
 *
 * @author akivela
 */


public class VariantsToTopicsAndAssociations extends AbstractWandoraTool implements WandoraTool {




    public VariantsToTopicsAndAssociations() {
    }
    public VariantsToTopicsAndAssociations(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Transform variants to topics and associations";
    }

    @Override
    public String getDescription() {
        return "Transforms variants to topics and associates variant topic with the variant carrier. "+
               "Variant scope is also added to the association as third player.";
    }


    public void execute(Wandora wandora, Context context) {
        try {

            GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Transform variants to topics and associations","Transform variants to topics and associations",true,new String[][]{
                new String[]{"Association type topic","topic","","What kind of associations you wish to create."},
                new String[]{"Variant role topic","topic","","Role topic of player topics created out of actual variant names."},
                new String[]{"Scope role topic","topic","", "Role of variant name scope topics."},
                new String[]{"Original role topic","topic","", "Role of the original topic carrying the variant name"},
                new String[]{"Delete variant names","boolean","true"},
            },wandora);
            god.setVisible(true);
            if(god.wasCancelled()) return;

            Map<String,String> values=god.getValues();

            setDefaultLogger();
            setLogTitle("Transform variants to topcs and associations");
            log("Transforming variants to topcs and associations");

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            TopicMap tm = wandora.getTopicMap();
            Topic associationType = tm.getTopic( values.get("Association type topic") );
            Topic variantRole = tm.getTopic( values.get("Variant role topic") );
            Topic scopeRole = tm.getTopic( values.get("Scope role topic") );
            Topic originalRole = tm.getTopic( values.get("Original role topic") );
            boolean deleteVariants = Boolean.parseBoolean(values.get("Delete variant names"));
            
            if(associationType == null) log("Association type topic has not been specified. Aborting.");
            if(variantRole == null) log("Variant role topic has not been specified. Aborting.");
            if(scopeRole == null) log("Scope role topic has not been specified. Aborting.");
            if(originalRole == null) log("Original role topic has not been specified. Aborting.");

            if(associationType != null && variantRole != null && scopeRole != null && originalRole != null) {

                int progress = 0;
                int associationCount = 0;

                ArrayList<T2<Topic,Set<Topic>>> deleteScopes = new ArrayList();
                while(topics.hasNext() && !forceStop()) {
                    try {
                        Topic topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            progress++;
                            Collection scopes = topic.getVariantScopes();
                            if(scopes != null) {
                                Iterator scopeIterator = scopes.iterator();
                                while(scopeIterator.hasNext()) {
                                    try {
                                        Set<Topic> scope = (Set<Topic>) scopeIterator.next();
                                        String variant = topic.getVariant(scope);
                                        if(variant != null) {
                                            for(Topic scopeTopic : scope) {
                                                log("Transforming variant '"+variant+"' to topics and associations" );
                                                try {
                                                    Topic variantTopic = tm.getTopicWithBaseName(variant);
                                                    if(variantTopic == null) {
                                                        variantTopic = tm.createTopic();
                                                        variantTopic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                                                        variantTopic.setBaseName(variant);
                                                    }
                                                    Association a = tm.createAssociation(associationType);
                                                    a.addPlayer(variantTopic, variantRole);
                                                    a.addPlayer(scopeTopic, scopeRole);
                                                    a.addPlayer(topic, originalRole);
                                                    associationCount++;
                                                }
                                                catch(Exception e) {
                                                    e.printStackTrace();
                                                    log(e);
                                                }
                                            }
                                            if(deleteVariants) {
                                                deleteScopes.add(new T2(topic, scope));
                                            }
                                        }
                                    }
                                    catch(Exception e) {
                                        e.printStackTrace();
                                        log(e);
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                if(deleteScopes != null && !deleteScopes.isEmpty()) {
                    log("Deleting original variant names...");
                    for(T2<Topic,Set<Topic>> deleteScope : deleteScopes) {
                        try {
                            deleteScope.e1.removeVariant(deleteScope.e2);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    log("Ok.");
                }
                log("Total "+associationCount+" associations created out of given variants.");
            }
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }


}
