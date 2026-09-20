/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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



package org.wandora.topicmap.undowrapper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.memory.TopicMapImpl;
import org.wandora.utils.logger.Log4j2Logger;


/**
 *
 * @author olli
 */
public class MergeOperation extends UndoOperation {
    private static final Log4j2Logger logger = Log4j2Logger.getLogger(MergeOperation.class);

    protected TopicMap tm;
    protected TopicMap tmcopy;
    protected Locator si1;
    protected Locator si2;

    private Set<UndoOperation> dependencies;

    public MergeOperation(Topic t1, Topic t2) throws TopicMapException, UndoException {
        tm = t1.getTopicMap();
        tmcopy = new TopicMapImpl();

        si1 = t1.getOneSubjectIdentifier();
        si2 = t2.getOneSubjectIdentifier();

        // This is a Set to so that same RemoveAssociationOperations
        // don't get added twice.
        // RemoveAssociationOperation makes sure that two operations for same
        // association will have the same hashCode and match with equals.        
        dependencies = new LinkedHashSet<UndoOperation>();
        addDependencies(t1);
        addDependencies(t2);

        tmcopy.copyTopicIn(t1, true);
        tmcopy.copyTopicAssociationsIn(t1);
        tmcopy.copyTopicIn(t2, true);
        tmcopy.copyTopicAssociationsIn(t2);

    }


    private void addDependencies(Topic t) throws TopicMapException, UndoException {
        for (Topic s : t.getTopicsWithDataType()) {
            Hashtable<Topic, String> data = s.getData(t);
            for (Topic version : data.keySet()) {
                dependencies.add(new SetOccurrenceOperation(s, t, version, null));
            }
        }
        for (Topic s : t.getTopicsWithDataVersion()) {
            for (Topic type : s.getDataTypes()) {
                String data = s.getData(type, t);
                if (data != null) {
                    dependencies.add(new SetOccurrenceOperation(s, type, t, null));
                    // SetOccurrenceOperation(Topic t,Topic type,Topic version,String value)
                }
            }
        }
        for (Topic s : t.getTopicsWithVariantScope()) {
            for (Set<Topic> scope : s.getVariantScopes()) {
                for (Topic st : scope) {
                    if (st.mergesWithTopic(t)) {
                        dependencies.add(new SetVariantOperation(s, scope, null));
                        break;
                    }
                }
            }
        }
        for (Topic s : t.getTopicMap().getTopicsOfType(t)) {
            dependencies.add(new RemoveTypeOperation(s, t));
        }

        for (Association a : t.getAssociationsWithType()) {
            dependencies.add(new RemoveAssociationOperation(a));
        }
        for (Association a : t.getAssociationsWithRole()) {
            dependencies.add(new RemoveAssociationOperation(a));
        }
    }


    private void deleteDependencies(Topic t) throws TopicMapException, UndoException {
        // make new ArrayLists to avoid concurrent modification
        for (Topic s : new ArrayList<Topic>(t.getTopicsWithDataType())) {
            s.removeData(t);
        }
        for (Topic s : new ArrayList<Topic>(t.getTopicsWithDataVersion())) {
            for (Topic type : new ArrayList<Topic>(s.getDataTypes())) {
                s.removeData(type, t);
            }
        }
        for (Topic s : new ArrayList<Topic>(t.getTopicsWithVariantScope())) {
            ScopeLoop: for (Set<Topic> scope : new ArrayList<Set<Topic>>(s.getVariantScopes())) {
                for (Topic st : scope) {
                    if (st.mergesWithTopic(t)) {
                        s.removeVariant(scope);
                    }
                }
            }
        }
        for (Topic s : new ArrayList<Topic>(t.getTopicMap().getTopicsOfType(t))) {
            s.removeType(t);
        }
        for (Association a : new ArrayList<Association>(t.getAssociationsWithType())) {
            a.remove();
        }
        for (Association a : new ArrayList<Association>(t.getAssociationsWithRole())) {
            a.remove();
        }
    }


    @Override
    public String getLabel() {
        return "merge";
    }


    @Override
    public void undo() throws UndoException {
        try {
            Topic t = tm.getTopic(si1);
            if (t == null)
                throw new UndoException();

            // this deletes all related things so that we can then remove the topic itself
            deleteDependencies(t);

            // remove the topic
            try {
                t.remove();
            }
            catch (Exception e) {
                // This really shouldn't happen if the deleteDependencies worked,
                // if it does happen then something has gone wrong and it's better
                // to abort.
                throw new UndoException(e);

                // Instead of removing a topic we'll make it a stub containing only 
                // a single subject identifier.
                //stubizeTopic(t);
            }

            // copy in the separate topics
            Topic t1 = tmcopy.getTopic(si1);
            Topic t2 = tmcopy.getTopic(si2);
            tm.copyTopicIn(t1, true);
            tm.copyTopicAssociationsIn(t1);
            tm.copyTopicIn(t2, true);
            tm.copyTopicAssociationsIn(t2);

            // this adds back all the related things in the correct separated topics
            for (UndoOperation uo : dependencies) {
                uo.undo();
            }
        }
        catch (TopicMapException tme) {
            throw new UndoException(tme);
        }
    }


    @Override
    public void redo() throws UndoException {
        try {
            // the topic map itself will handle the merge completely

            Topic t1 = tm.getTopic(si1);
            if (t1 == null)
                throw new UndoException();
            Topic t2 = tm.getTopic(si2);
            if (t2 == null)
                throw new UndoException();
            t1.addSubjectIdentifier(si2);
        }
        catch (TopicMapException tme) {
            throw new UndoException(tme);
        }
    }


}
