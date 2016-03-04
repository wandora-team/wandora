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
 * AssociationTable.java
 *
 * Created on August 12, 2004, 11:15 AM
 */

package org.wandora.application.gui.table;




import java.util.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.net.URL;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.utils.ClipboardBox;



/**
 * @author  olli, akivela
 */
public class AssociationTable extends TopicTable {
    
    private Wandora wandora;
    private Association[] associations;
    private Topic[][] tableTopics;
    private Topic[] columnTopics;
    private Topic topic;
    boolean hideTopicColumn = true;
    private Topic associationTypeTopic = null;
    
    
    
    /** Creates a new instance of AssociationTable */
    public AssociationTable(Collection as, Wandora w, Topic topic)  throws TopicMapException {
        super(w);
        
        this.wandora = w;
        this.topic=topic;
        Collection roles=new HashSet();
        Iterator iter=as.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            roles.addAll(a.getRoles());
            associationTypeTopic = a.getType();
        }
        roles=TMBox.sortTopics(roles,null);
        
        associations = (Association[]) as.toArray( new Association[0] );
        columnTopics=(Topic[])roles.toArray(new Topic[0]);
        tableTopics = new Topic[associations.length][columnTopics.length];
            
        Topic role = null;
        Topic player = null;
        int len = associations.length;
        for(int i=0; i<len; i++) {
            Association a=associations[i];
            for(int j=0; j<columnTopics.length; j++) {
                role=columnTopics[j];
                player=a.getPlayer(role);
                tableTopics[i][j]=player; // may be null
            }
        }

        initialize(tableTopics, columnTopics);
        
        this.setTransferHandler(new AssociationTableTransferHandler());
        this.setDropMode(DropMode.ON);
    }
    

    
    
    @Override
    public Object[] getPopupStruct() {
        return getPopupStruct(WandoraMenuManager.getAssociationsPopupStruct());
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public Topic getAssociationTypeTopic() {
        return associationTypeTopic;
    }
    
    
    
    
    public void selectAssociations(Association[] associations) {
        if(associations == null || associations.length == 0) return;
        for(Association a : associations) {
            selectAssociation(a);
        }
    }
    
    public void selectAssociation(Association association) {
        int r = associations.length;
        int c = this.getColumnCount();
        Association tableAssociation = null;
        for(int y=0; y<r; y++) {
            tableAssociation = associations[y];
            try {
                if(tableAssociation != null) {
                    if(tableAssociation.equals(association)) {
                        for(int x=0; x<c; x++) {
                            this.selectCell(x,y);
                        }
                        return;
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    
    public void selectAssociations(Map<Association,ArrayList<Topic>> selection) {
        if(selection == null || selection.isEmpty()) return;
        Set<Association> as = selection.keySet();
        for(Association a : as) {
            selectAssociation(a, selection.get(a));
        }
    }
    
    
    public void selectAssociation(Association association, ArrayList<Topic> roles) {
        int r = associations.length;
        int c = columnTopics.length;
        Association tableAssociation = null;
        for(int y=0; y<r; y++) {
            tableAssociation = associations[y];
            try {
                if(tableAssociation != null) {
                    if(tableAssociation.equals(association)) {
                        for(int x=0; x<c; x++) {
                            if(roles.contains(columnTopics[x])) {
                                this.selectCell(convertColumnIndexToModel(x),y);
                            }
                        }
                        return;
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    // -------------------------------------------------------------------------

    
    public Collection getSelectedAssociations() {
        ArrayList<int[]> selected = getSelectedCells();
        ArrayList selectedAssociations = new ArrayList();
        Association a = null;

        if(!selected.isEmpty()) {
            for(int[] cell : selected) {
                int row = cell[0];
                a = associations[convertRowIndexToModel(row)];
                if(!selectedAssociations.contains(a)) {
                    selectedAssociations.add(a);
                }
            }
        }
        else {
            selectedAssociations.addAll( getAllAssociations() );
        }
        return selectedAssociations;
    }
    
    
    public Collection getAllAssociations() {
        ArrayList allAssociations = new ArrayList();
        allAssociations.addAll(Arrays.asList(associations));
        return allAssociations;
    }
    
    
    public Map<Association,ArrayList<Topic>> getSelectedAssociationsWithSelectedPlayers() {
        HashMap<Association,ArrayList<Topic>> selection = new HashMap<Association,ArrayList<Topic>>();
        ArrayList<Topic> players = null;
        Collection<int[]> selectedCells = getSelectedCells();
        
        for(int[] cell : selectedCells) {
            Association a = associations[cell[0]];
            Topic player = getTopicAt(cell[0], cell[1]);
            
            players = selection.get(a);
            if(players == null) players = new ArrayList<Topic>();
            players.add(player);
            
            selection.put(a, players);
        }
        return selection;
    }
    
    
    
    public Map<Association,ArrayList<Topic>> getSelectedAssociationsWithSelectedRoles() {
        HashMap<Association,ArrayList<Topic>> selection = new HashMap<Association,ArrayList<Topic>>();
        ArrayList<Topic> roles = null;
        Collection<int[]> selectedCells = getSelectedCells();
        
        for(int[] cell : selectedCells) {
            Association a = associations[cell[0]];
            Topic role = columnTopics[cell[1]];
            
            roles = selection.get(a);
            if(roles == null) roles = new ArrayList<Topic>();
            roles.add(role);
            
            selection.put(a, roles);
        }
        return selection;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    

    
    private boolean autoCreateTopicsInPaste = false;
    
    @Override
    public void paste() {
        TopicMap tm = Wandora.getWandora().getTopicMap();
        Map<Association,ArrayList<Topic>> selectedAssociationsAndRoles = getSelectedAssociationsWithSelectedRoles();
        String tabText = ClipboardBox.getClipboard();
        StringTokenizer tabLines = new StringTokenizer(tabText, "\n");
        autoCreateTopicsInPaste = false;
        while(tabLines.hasMoreTokens()) {
            String tabLine = tabLines.nextToken();
            StringTokenizer topicIdentifiers = new StringTokenizer(tabLine, "\t");
            try {
                String topicIdentifier = null;
                while(topicIdentifiers.hasMoreTokens()) {
                    topicIdentifier = topicIdentifiers.nextToken();
                    if(topicIdentifier != null && topicIdentifier.length() > 0) {
                        Topic pastedTopic = getTopicForIdentifier(topicIdentifier);
                        if(pastedTopic == null) {
                            boolean createTopicInPaste = false;
                            if(!autoCreateTopicsInPaste) {
                                int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Can't find a topic for identifier '"+topicIdentifier+"'. Would you like to create a topic for '"+topicIdentifier+"'?", "Create new topic?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                if(a == WandoraOptionPane.YES_OPTION) {
                                    createTopicInPaste = true;
                                }
                                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                                    autoCreateTopicsInPaste = true;
                                }
                                else if(a == WandoraOptionPane.CANCEL_OPTION) {
                                    return;
                                }
                            }
                            if(autoCreateTopicsInPaste || createTopicInPaste) {
                                if(tm != null) {
                                    boolean identifierIsURL = false;
                                    try {
                                        URL u = new URL(topicIdentifier);
                                        identifierIsURL = true;
                                    }
                                    catch(Exception e) {}
                                    pastedTopic = tm.createTopic();
                                    if(identifierIsURL) {
                                        pastedTopic.addSubjectIdentifier(new Locator(topicIdentifier));
                                    }
                                    else {
                                        pastedTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                                        pastedTopic.setBaseName(topicIdentifier);
                                    }
                                }
                            }
                        }
                        if(pastedTopic != null && topic != null) { 
                            for(Association a : selectedAssociationsAndRoles.keySet()) {
                                if(a != null && !a.isRemoved()) {
                                    Association newa = tm.createAssociation(a.getType());
                                    Collection<Topic> selectedRoles = selectedAssociationsAndRoles.get(a);
                                    for(Topic r : a.getRoles()) {
                                        if(!selectedRoles.contains(r)) {
                                            newa.addPlayer(a.getPlayer(r), r);
                                        }
                                    }
                                    for(Topic r : selectedRoles) {
                                        newa.addPlayer(pastedTopic, r);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                
            }
        }
    }
    
    
    
    
   


    // -------------------------------------------------------------------------
    // --------------------------------------------------------- DRAG & DROP ---
    // -------------------------------------------------------------------------
   
    

    
    private class AssociationTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int[] rows=getSelectedRows();
            DnDHelper.WandoraTransferable ret=DnDHelper.makeTopicTableTransferable(AssociationTable.this);
            ArrayList<Association> selectedAssociations=new ArrayList<Association>();
            for(int i=0;i<rows.length;i++){
                selectedAssociations.add(associations[rows[i]]);
            }
            ret.setAssociations(selectedAssociations.toArray( new Association[] {} ));
            return ret;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try{
                TopicMap tm=AssociationTable.this.wandora.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                
                JTable.DropLocation location=(JTable.DropLocation)support.getDropLocation();
                int row=location.getRow();
                if(row==-1) return false;
                row=convertRowIndexToModel(row);
                int col=location.getColumn();
                if(col==-1) return false;
                col=convertColumnIndexToModel(col);
                Association rowAssociation=associations[row];
                Topic associationType=rowAssociation.getType();
                
                for(Topic t : topics){
                    Association a=tm.createAssociation(associationType);
                    for(int i=0;i<columnTopics.length;i++){
                        if(i==col){
                            a.addPlayer(t,columnTopics[i]);
                        }
                        else{
                            Topic player=rowAssociation.getPlayer(columnTopics[i]);
                            a.addPlayer(player,columnTopics[i]);
                        }
                    }
                }
                AssociationTable.this.wandora.doRefresh();
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(Exception ce){}
            return false;
        }

    }

    
}
