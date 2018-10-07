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
 * FreeAssociationPrompt.java
 *
 * Created on 14. kes�kuuta 2006, 10:08
 */

package org.wandora.application.gui;

import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.utils.swing.GuiTools;
import org.wandora.utils.GripCollections;
import javax.swing.*;
import java.awt.*;
import java.util.*;


/**
 *
 * @author  olli
 */
public class FreeAssociationPrompt extends javax.swing.JDialog {

	
	private static final long serialVersionUID = 1L;
	
	
    private static Topic previousAssociationType = null;
    private static ArrayList<Topic> previousRoles = null;


    
    private Association createdAssociation;
    private Association originalAssociation;
    private GetTopicButton typeButton;
    private Vector<T2<GetTopicButton,Topic>> fixedPlayers;

    
//    private GetTopicButton selectedButton;
    
    private Wandora admin;
//    private Topic contextTopic;
    
    private Vector<T2<GetTopicButton,GetTopicButton>> players;




    /** Creates new form FreeAssociationPrompt */
    public FreeAssociationPrompt(Wandora wandora, Topic contextTopic) throws TopicMapException {
        this(wandora,GripCollections.newVector(contextTopic),null);        
    }
    public FreeAssociationPrompt(Wandora wandora, Topic contextTopic, Association original) throws TopicMapException {
        this(wandora,GripCollections.newVector(contextTopic),original);        
    }
    public FreeAssociationPrompt(Wandora wandora, Association original) throws TopicMapException {
        this(wandora, new Vector<Topic>(),original);        
    }
    public FreeAssociationPrompt(final Wandora wandora, Vector<Topic> contextTopics) throws TopicMapException {
        this(wandora,contextTopics,null);
    }
    public FreeAssociationPrompt(final Wandora wandora, Vector<Topic> contextTopics, Association original) throws TopicMapException {
        super(wandora,true);
        this.originalAssociation=original;
        this.admin=wandora;
  //      this.contextTopic=contextTopic;
        
        fixedPlayers=new Vector<T2<GetTopicButton,Topic>>();
        for(Topic contextTopic : contextTopics){
            GetTopicButton b=new GetTopicButton(admin,this);
            b.updateText();
            fixedPlayers.add(t2(b,contextTopic));
        }
        final Vector<Topic> fixedPlayers2=new Vector<Topic>();
        for(T2<GetTopicButton,Topic> player : fixedPlayers){
            fixedPlayers2.add(player.e2);
        }
        
        players=new Vector<T2<GetTopicButton,GetTopicButton>>();
        typeButton=new GetTopicButton(null,admin,this,false,new GetTopicButton.ButtonHandler(){
            private Vector<Topic> suggested=null;
            public T2<Topic,Boolean> pressed(GetTopicButton button) throws TopicMapException {
                if(suggested==null) suggested=suggestAssociationType(fixedPlayers2,false);
                if(suggested==null || suggested.isEmpty()) return button.defaultPressHandler();
                else{
                    TabbedTopicSelector finder=admin.getTopicFinder();
                    finder.insertTab(new TopicListSelector(suggested),0);
                    Topic s=admin.showTopicFinder(admin,finder);
                    if(s==null) return t2(null,true);
                    else return t2(s,false);                    
                }
            }
        });
        typeButton.setButtonListener(new GetTopicButton.ButtonListener(){
            public void topicChanged(GetTopicButton button){
                try{
                    prefill(false);
                }catch(TopicMapException tme){tme.printStackTrace();}
            }
        });
        typeButton.addPopupList(new GetTopicButton.PopupListHandler() {
            public Collection<Topic> getListTopics() throws TopicMapException {
                return suggestAssociationType(fixedPlayers2,false);
            }
        });
//        selectedButton=new GetTopicButton(admin,this);
        initComponents();
        typeButton.updateText();
//        selectedButton.updateText();
//        selectedTopicLabel.setText(admin.getTopicGUIName(contextTopic));

        if(previousAssociationType == null && previousRoles == null) {
            usePrevButton.setEnabled(false);
        }

        GuiTools.centerWindow(this,admin);
        
        if(originalAssociation!=null) fillWith(originalAssociation);
        else if(fixedPlayers.size()<2) addPlayer();
        else refreshPlayersPanel(); // this is done in addPlayer or fillWith otherwise
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        _typeButton = typeButton;
        jPanel2 = new javax.swing.JPanel();
        addPlayerButton = new org.wandora.application.gui.simple.SimpleButton();
        jSeparator1 = new javax.swing.JSeparator();
        useDefaultsButton = new org.wandora.application.gui.simple.SimpleButton();
        usePrevButton = new org.wandora.application.gui.simple.SimpleButton();
        jPanel1 = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        playersPanel = new PlayersPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Association editor");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Association type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel3.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel3.add(_typeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(jPanel3, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        addPlayerButton.setText("Add player");
        addPlayerButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addPlayerButton.setMaximumSize(new java.awt.Dimension(90, 23));
        addPlayerButton.setMinimumSize(new java.awt.Dimension(90, 23));
        addPlayerButton.setPreferredSize(new java.awt.Dimension(90, 23));
        addPlayerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlayerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel2.add(addPlayerButton, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel2.add(jSeparator1, gridBagConstraints);

        useDefaultsButton.setText("Use defaults");
        useDefaultsButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        useDefaultsButton.setMaximumSize(new java.awt.Dimension(90, 23));
        useDefaultsButton.setMinimumSize(new java.awt.Dimension(90, 23));
        useDefaultsButton.setPreferredSize(new java.awt.Dimension(90, 23));
        useDefaultsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                useDefaultsButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 3);
        jPanel2.add(useDefaultsButton, gridBagConstraints);

        usePrevButton.setText("Use previous");
        usePrevButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        usePrevButton.setMaximumSize(new java.awt.Dimension(90, 23));
        usePrevButton.setMinimumSize(new java.awt.Dimension(90, 23));
        usePrevButton.setPreferredSize(new java.awt.Dimension(90, 23));
        usePrevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                usePrevButtonMouseReleased(evt);
            }
        });
        jPanel2.add(usePrevButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(jPanel1, gridBagConstraints);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        playersPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(playersPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        setBounds(0, 0, 771, 345);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        try{
            if((createdAssociation=createAssociation()) != null) {
                previousAssociationType = typeButton.getTopic();
                previousRoles = new ArrayList<Topic>();
                for(T2<GetTopicButton,Topic> player : fixedPlayers) {
                    previousRoles.add(player.e1.getTopic());
                }
                for(T2<GetTopicButton,GetTopicButton> member : players) {
                    previousRoles.add(member.e1.getTopic());
                }
                this.setVisible(false);
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void addPlayerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlayerButtonActionPerformed
        try{
            addPlayer();
        }catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }//GEN-LAST:event_addPlayerButtonActionPerformed

    private void useDefaultsButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_useDefaultsButtonMouseReleased
        try {
            typeButton.setTopic(SchemaBox.DEFAULT_ASSOCIATION_SI);
            int i=1;
            for(T2<GetTopicButton,Topic> player : fixedPlayers) {
                player.e1.setTopic("http://wandora.org/si/core/default-role-"+i);
                i++;
            }
            for(T2<GetTopicButton,GetTopicButton> member : players) {
                member.e1.setTopic("http://wandora.org/si/core/default-role-"+i);
                i++;
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }//GEN-LAST:event_useDefaultsButtonMouseReleased

    private void usePrevButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usePrevButtonMouseReleased
        try {
            if(previousAssociationType != null && !previousAssociationType.isRemoved()) {
                typeButton.setTopic(previousAssociationType);
            }
            if(previousRoles != null && previousRoles.size() > 0) {
                int i=0;
                for(T2<GetTopicButton,Topic> player : fixedPlayers) {
                    if(previousRoles.size() > i) {
                        Topic r = previousRoles.get(i);
                        if(r != null && !r.isRemoved()) {
                            player.e1.setTopic(r);
                        }
                        i++;
                    }
                }
                for(T2<GetTopicButton,GetTopicButton> members : players) {
                    if(previousRoles.size() > i) {
                        Topic r = previousRoles.get(i);
                        if(r != null && !r.isRemoved()) {
                            members.e1.setTopic(r);
                        }
                        i++;
                    }
                }
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }//GEN-LAST:event_usePrevButtonMouseReleased
    
    private static int fitRoles(Vector<Topic> players, Vector<Topic> classes) throws TopicMapException {
        if(players.isEmpty() || classes.isEmpty()) return 0;
        int max=0;
        for(Topic player : players){
            for(Topic cls : classes){
//                if(player.isOfType(cls)){
                if(SchemaBox.isInstanceOf(player,cls)){
                    if(players.size()==1 || classes.size()==1) return 1;
                    Vector<Topic> np=new Vector<Topic>(players);
                    np.remove(player);
                    Vector<Topic> nc=new Vector<Topic>(classes);
                    nc.remove(cls);
                    int f=fitRoles(np,nc);
                    if(f==Math.min(np.size(),nc.size())) return f+1;
                    else if(f+1>max) max=f+1;
                }
            }
        }
        return max;
    }
    
    public Vector<Topic> suggestAssociationType(Vector<Topic> players,boolean strict) throws TopicMapException {
        TopicHashMap<Integer> results=new TopicHashMap<Integer>();
        for(Topic player : players){
            Collection<Topic> types=SchemaBox.getAssociationTypesFor(player);
            for(Topic type : types){
                int score=0;
                Collection<Topic> roles=SchemaBox.getAssociationTypeRoles(type);
                Vector<Topic> roleClasses=new Vector<Topic>();
                for(Topic role : roles){
                    Topic roleClass=SchemaBox.getRoleClass(role);
                    roleClasses.add(roleClass);
                }
                int s=fitRoles(players,roleClasses);
                Integer o=results.get(type);
                if(o==null || s>o) results.put(type,s);
            }
        }
        Vector<Topic> ret=new Vector<Topic>();
        if(strict){
            for(Map.Entry<Topic,Integer> e : results.entrySet()){
                if(e.getValue()==players.size()){
                    ret.add(e.getKey());
                }
            }
        }
        else{
            while(results.size()>0){
                int max=-1;
                Topic maxTopic=null;
                for(Map.Entry<Topic,Integer> e : results.entrySet()){
                    if(e.getValue()>max){
                        max=e.getValue();
                        maxTopic=e.getKey();
                    }
                }
                results.remove(maxTopic);
                ret.add(maxTopic);
            }
        }
        return ret;
    }
    
    public Vector<Topic> suggestAssociationRole(Topic associationType, Topic player, boolean strict) throws TopicMapException{
        Collection<Topic> roles=SchemaBox.getAssociationTypeRoles(associationType);
        Vector<Topic> ofType=new Vector<Topic>();
        Vector<Topic> ofTypeUsed=new Vector<Topic>();
        Vector<Topic> notType=new Vector<Topic>();
        Vector<Topic> notTypeUsed=new Vector<Topic>();
        for(Topic role : roles){
            Topic cls=SchemaBox.getRoleClass(role);
            boolean used=false;
            for(T2<GetTopicButton,Topic> p : fixedPlayers){
                Topic r=p.e1.getTopic();
                if(r!=null && r.mergesWithTopic(role)){
                    used=true;
                    break;
                }
            }
            if(!used){
                for(T2<GetTopicButton,GetTopicButton> p : players){
                    Topic r=p.e1.getTopic();
                    if(r!=null && r.mergesWithTopic(role)){
                        used=true;
                        break;
                    }
                }
            }
//            if(player!=null && player.isOfType(cls)){
            if(player!=null && SchemaBox.isInstanceOf(player,cls)){
                if(used) ofTypeUsed.add(role);
                else ofType.add(role);
            }
            else{
                if(used) notTypeUsed.add(role);
                else notType.add(role);
            }
        }
        if(!strict){
            ofType.addAll(ofTypeUsed);
            ofType.addAll(notType);
            ofType.addAll(notTypeUsed);
        }
        
        return ofType;
    }
    
    public void fillWith(Association association) throws TopicMapException {
        typeButton.setTopic(association.getType());
        ArrayList<Topic> roles=new ArrayList<Topic>(association.getRoles());
        for(T2<GetTopicButton,Topic> player : fixedPlayers){
            for(int i=0;i<roles.size();i++){
                Topic r=roles.get(i);
                Topic p=association.getPlayer(r);
                if(p.mergesWithTopic(player.e2)){
                    player.e1.setTopic(r);
                    roles.remove(i);
                    break;
                }
            }
        }
        if(players==null || players.size()>0) players=new Vector<T2<GetTopicButton,GetTopicButton>>();
        
        for(Topic r : roles){
            Topic p=association.getPlayer(r);
            T2<GetTopicButton,GetTopicButton> t=t2(new GetTopicButton(admin,this),new GetTopicButton(admin,this));
            t.e1.setTopic(r);
            t.e2.setTopic(p);
            players.add(t);
        }
        refreshPlayersPanel();        
    }
    
    public void prefill(boolean overwrite) throws TopicMapException {
        Topic type=null;
        Vector<Topic> suggested;
        if(typeButton.getTopic()==null || overwrite){
            Vector<Topic> players=new Vector<Topic>();
            for(T2<GetTopicButton,Topic> player : fixedPlayers){
                players.add(player.e2);
            }
            suggested=suggestAssociationType(players,true);
            if(suggested.size()==1){
                type=suggested.elementAt(0);
                typeButton.setTopic(type);
            }
        }
        else type=typeButton.getTopic();
        
        if(type!=null){
            boolean loop=true;
            int matched=0;
            while(loop){
                loop=false;
                for(T2<GetTopicButton,Topic> player : fixedPlayers){
                    if(player.e1.getTopic()==null || overwrite){
                        suggested=suggestAssociationRole(type,player.e2,true);
                        if(suggested.size()==1) {
                            Topic role=suggested.elementAt(0);
                            player.e1.setTopic(role);
                            loop=true;
                            matched++;
                        }
                    }
                }
            }
            Collection<Topic> roles=SchemaBox.getAssociationTypeRoles(type);
            while(fixedPlayers.size()+players.size()<roles.size()) addPlayer();
            if(matched==fixedPlayers.size()){
                for(T2<GetTopicButton,GetTopicButton> player : players){
                    suggested=suggestAssociationRole(type,null,false);
                    if(suggested.size()>0){
                        player.e1.setTopic(suggested.elementAt(0));
                    }
                }
            }
        }
    }

    public Association getCreatedAssociation(){
        return createdAssociation;
    }
    
    public Association getOriginalAssociation(){
        return originalAssociation;
    }
    
    private void refreshPlayersPanel(){
        playersPanel.removeAll();
        int counter=0;
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new java.awt.Insets(3,3,2,3);
        gbc.weightx=1.0;
        gbc.anchor=GridBagConstraints.CENTER;
        gbc.gridx=1;
        gbc.gridy=1;
        SimpleLabel roleLabel = new SimpleLabel("<html><b>Role</b></html>");
        playersPanel.add(roleLabel,gbc);
        gbc.gridx=2;
        playersPanel.add(new SimpleLabel("<html><b>Player</b></html>"),gbc);
        counter=2;
        for(T2<GetTopicButton,Topic> fixedPlayer : fixedPlayers){
            gbc.gridy=counter;
            gbc.gridx=1;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            final Topic player=fixedPlayer.e2;
            playersPanel.add(fixedPlayer.e1,gbc);
            fixedPlayer.e1.setButtonHandler(new GetTopicButton.ButtonHandler(){
                public T2<Topic,Boolean> pressed(GetTopicButton button) throws TopicMapException {
                    Topic t=typeButton.getTopic();
                    if(t!=null){
                        Vector<Topic> v=suggestAssociationRole(t,player,false);
                        TabbedTopicSelector finder=admin.getTopicFinder();
                        finder.insertTab(new TopicListSelector(v),0);
                        Topic s=admin.showTopicFinder(admin,finder);
                        if(s==null) return t2(null,true);
                        else return t2(s,false);                        
                    }
                    else return button.defaultPressHandler();
                }
            });
            fixedPlayer.e1.addPopupList(new GetTopicButton.PopupListHandler() {
                public Collection<Topic> getListTopics() throws TopicMapException {
                    Topic t=typeButton.getTopic();
                    if(t!=null) return suggestAssociationRole(t,player,false);
                    else return null;
                }
            });
            gbc.gridx=2;
            gbc.fill=GridBagConstraints.NONE;
            SimpleLabel l=new SimpleLabel(admin.getTopicGUIName(fixedPlayer.e2));
            playersPanel.add(l,gbc);
            counter++;
        }
        int pcounter=0;
        for(T2<GetTopicButton,GetTopicButton> t : players) {            
            gbc.insets=new java.awt.Insets(3,3,2,3);
            gbc.gridx=1;
            gbc.weightx=1.0;
            gbc.gridy=counter;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            final GetTopicButton roleButton=t.e1;
            final GetTopicButton playerButton=t.e2;
            playersPanel.add(t.e1,gbc);
            t.e1.setButtonHandler(new GetTopicButton.ButtonHandler(){
                public T2<Topic,Boolean> pressed(GetTopicButton button) throws TopicMapException {
                    Topic t=typeButton.getTopic();
                    Topic p=playerButton.getTopic();
                    if(t!=null){
                        Vector<Topic> v=suggestAssociationRole(t,p,false);
                        TabbedTopicSelector finder=admin.getTopicFinder();
                        finder.insertTab(new TopicListSelector(v),0);
                        Topic s=admin.showTopicFinder(admin,finder);
                        if(s==null) return t2(null,true);
                        else return t2(s,false);                        
                    }
                    else return button.defaultPressHandler();
                }
            });
            t.e1.addPopupList(new GetTopicButton.PopupListHandler() {
                public Collection<Topic> getListTopics() throws TopicMapException {
                    Topic t=typeButton.getTopic();
                    Topic p=playerButton.getTopic();
                    if(t!=null) return suggestAssociationRole(t,p,false);
                    else return null;  
                }
            });
            gbc.gridx=2;
            playersPanel.add(t.e2,gbc);
            t.e2.setButtonHandler(new GetTopicButton.ButtonHandler(){
                public T2<Topic,Boolean> pressed(GetTopicButton button) throws TopicMapException {
                    Topic t=roleButton.getTopic();
                    if(t!=null){
                        t=SchemaBox.getRoleClass(t);
                        TabbedTopicSelector finder=admin.getTopicFinder();
                        finder.insertTab(new TopicsOfTypeSelector(t,null,true),0);
                        Topic s=admin.showTopicFinder(admin,finder);
                        if(s==null) return t2(null,true);
                        else return t2(s,false);
                    }
                    else return button.defaultPressHandler();
                }
            });
            t.e2.addPopupList(new GetTopicButton.PopupListHandler() {
                public Collection<Topic> getListTopics() throws TopicMapException {
                    Topic t=roleButton.getTopic();
                    if(t!=null){
                        t=SchemaBox.getRoleClass(t);
                        Collection<Topic> topics=SchemaBox.getInstancesOf(t);
                        topics=TMBox.sortTopics(topics,null);
                        return topics;
                    }
                    else return null;
                }
            });
            SimpleButton deleteButton=new SimpleButton("Remove");
            final int fcounter=pcounter;
            deleteButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    players.remove(fcounter);
                    refreshPlayersPanel();
                }
            });
            gbc.gridx=3;
            gbc.weightx=0.0;
            gbc.fill=GridBagConstraints.NONE;
            gbc.insets=new java.awt.Insets(3,8,2,3);
            playersPanel.add(deleteButton,gbc);
            counter++;
            pcounter++;
        }
        gbc.gridx=1;
        gbc.gridy=counter+3;
        gbc.weighty=1.0;
        gbc.weightx=1.0;
        playersPanel.add(new JPanel(),gbc);
        
        playersPanel.revalidate();
        playersPanel.repaint();
    }
    
    public void addPlayer() throws TopicMapException {
        T2<GetTopicButton,GetTopicButton> t=t2(new GetTopicButton(admin,this),new GetTopicButton(admin,this));
        players.add(t);
        refreshPlayersPanel();
    }
    
    public Association createAssociation() throws TopicMapException {
        TopicMap tm = admin.getTopicMap();
//        TopicMap tm=contextTopic.getTopicMap();
//        TopicMap tm=fixedPlayers.elementAt(0).e2.getTopicMap();
        boolean cont=true;
        if(typeButton.getTopic()==null) cont=false;
        for(T2<GetTopicButton,Topic> fixedPlayer : fixedPlayers){
            if(fixedPlayer.e1.getTopic()==null) cont=false;
        }
        for(T2<GetTopicButton,GetTopicButton> t : players){
            if(t.e1.getTopic()==null || t.e2.getTopic()==null) cont=false;
        }
        if(!cont){
            WandoraOptionPane.showMessageDialog(admin,"You must select topic for association type and all players and roles.", "Select topics", WandoraOptionPane.WARNING_MESSAGE);
            return null;
        }
        
        if(originalAssociation!=null) originalAssociation.remove();
        
        Association a=tm.createAssociation(typeButton.getTopic());
        for(T2<GetTopicButton,Topic> fixedPlayer : fixedPlayers){
            a.addPlayer(tm.getMergingTopics(fixedPlayer.e2).iterator().next(),
                        tm.getMergingTopics(fixedPlayer.e1.getTopic()).iterator().next());
        }        
//        a.addPlayer(contextTopic,selectedButton.getTopic());
        for(T2<GetTopicButton,GetTopicButton> t : players){
            a.addPlayer(tm.getMergingTopics(t.e2.getTopic()).iterator().next(),
                        tm.getMergingTopics(t.e1.getTopic()).iterator().next());
        }
        
        Wandora.getWandora().doRefresh();
        return a;
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _typeButton;
    private javax.swing.JButton addPlayerButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel playersPanel;
    private javax.swing.JButton useDefaultsButton;
    private javax.swing.JButton usePrevButton;
    // End of variables declaration//GEN-END:variables
    
    private static class PlayersPanel extends JPanel implements Scrollable {
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        public Dimension getPreferredScrollableViewportSize() {
            return null;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 30;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 30;
        }
        
    }
}
