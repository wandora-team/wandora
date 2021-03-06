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
 * OccurrencePanel.java
 *
 * Created on August 10, 2004, 9:42 AM
 */

package org.wandora.application.gui;




import java.awt.event.KeyEvent;
import org.wandora.topicmap.*;
import java.util.*;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;



/**
 *
 * @author  olli
 */
public class OccurrencePanel extends ResourceEditor {
    
    private Wandora wandora;
    private Topic topic;
    private Topic occurrenceType;
    
    private HashMap dataTable;
    private HashMap invDataTable;
    
    private boolean deleted;
    
    /** Creates new form OccurrencePanel */
    public OccurrencePanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

    }
    // </editor-fold>//GEN-END:initComponents

    @Override
    public boolean applyChanges(Topic t, Wandora wandora)  throws TopicMapException {
        boolean changed=false;
        if(!deleted) {
            Iterator iter=dataTable.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry e=(Map.Entry)iter.next();
                Topic version=(Topic)e.getKey();
                SimpleTextPane comp=(SimpleTextPane)e.getValue();
                String newText = comp.getText();
                String orig=topic.getData(occurrenceType,version);
                if(orig==null) orig="";
                if(!orig.equals(newText)) {
                    changed=true;
                    topic.removeData(occurrenceType,version);
                    if(newText.length()>0) {
                        topic.setData(occurrenceType, version, newText );
                    }
                }
            }
        }
        return changed;
    }
    
    
    public void setOccurrenceType(org.wandora.topicmap.Topic t){
        occurrenceType=t;
    }
    
    
    @Override
    public boolean hasChanged() throws TopicMapException {
        Iterator iter=dataTable.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry e=(Map.Entry)iter.next();
            Topic version=(Topic)e.getKey();
            SimpleTextPane comp=(SimpleTextPane)e.getValue();
            String orig=topic.getData(occurrenceType,version);
            if(orig==null) orig="";
            if(!orig.equals(comp.getText())) return true;
        }
        return false;
    }    
    
    
    @Override
    public void initializeAssociation(org.wandora.topicmap.Topic t, org.wandora.topicmap.Association a, Wandora parent) {
        throw new RuntimeException("Association editing not supported");
    }
    
    
    @Override
    public void initializeOccurrence(Topic t, Topic otype, Wandora w)  throws TopicMapException {
        this.wandora=w;
        topic=t;
        occurrenceType=otype;
        
        dataTable=new HashMap();
        invDataTable=new HashMap();
        deleted=false;
        
        TopicMap tm=w.getTopicMap();
        
        String[] langs=null;
        String[] vers=null;
        Topic[] langTopics=null;
        Topic[] verTopics=null;
        try {
            langs=TMBox.getLanguageSIs(wandora.getTopicMap());
            vers=TMBox.getNameVersionSIs(wandora.getTopicMap());
            langTopics=tm.getTopics(langs);
            verTopics=tm.getTopics(vers);
        }
        catch(TopicMapException tme){
            wandora.handleError(tme);
            return;
        }
        this.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc;
        for(int i=0;i<langs.length;i++){
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=i+1;
            gbc.gridy=0;
            javax.swing.JLabel label=new SimpleLabel(wandora.getTopicGUIName(langTopics[i]));
            this.add(label,gbc);
        }
        
        gbc=new java.awt.GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=1;
//        this.add(new TopicLink(occurrenceType,parent),gbc);
//        this.add(new javax.swing.JLabel(occurrenceType.getBaseName()),gbc);
        int j;
        for(j=0;j<langs.length;j++) {
            String data="";
            if(occurrenceType!=null) {
                data=topic.getData(occurrenceType,langTopics[j]);
            }
            final SimpleTextPane field=new SimpleTextPane();
            field.setText(data==null ? "" : data);
            javax.swing.JScrollPane sp=new javax.swing.JScrollPane();
            field.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if(e.getKeyCode()==KeyEvent.VK_TAB){
                        e.consume();
                        field.transferFocus();
                    }
                }
            });
            sp.setWheelScrollingEnabled(false);
            sp.setPreferredSize(new java.awt.Dimension(130,60));
            //field.setLineWrap(true);
            //field.setWrapStyleWord(true);
            dataTable.put(langTopics[j],field);
            invDataTable.put(field,langTopics[j]);
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=1+j;
            gbc.gridy=1;
            gbc.fill=java.awt.GridBagConstraints.BOTH;
            gbc.weightx=1.0;
            gbc.weighty=1.0;
            sp.setViewportView(field);
            this.add(sp,gbc);
        }
        this.validate();
        this.repaint();
/*        javax.swing.JButton deleteButton=new javax.swing.JButton("Remove");
        deleteButton.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e){
                delete();
            }
        });
        gbc.gridx=1+j;
        gbc.gridy=1;
        gbc.fill=java.awt.GridBagConstraints.HORIZONTAL;
        this.add(deleteButton,gbc);*/
        
    }

    
    public void delete() throws TopicMapException {
        try {
            wandora.applyChanges();
        }
        catch(CancelledException ce){return;}
        HashSet versions=new HashSet(); // avoid concurrent modification
        versions.addAll(topic.getData(occurrenceType).keySet());
        Iterator iter=versions.iterator();
        while(iter.hasNext()) {
            Topic version=(Topic)iter.next();
            topic.removeData(occurrenceType,version);
        }
        deleted=true;
        try {
            wandora.doRefresh();
        } catch(Exception ce){return;}
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
