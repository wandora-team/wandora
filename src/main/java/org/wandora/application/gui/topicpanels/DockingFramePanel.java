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
 * DockingFramePanel.java
 *
 *
 */

package org.wandora.application.gui.topicpanels;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolManager;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.topicpanels.dockingpanel.SelectTopicPanelPanel;
import org.wandora.application.gui.topicpanels.dockingpanel.WandoraDockActionSource;
import org.wandora.application.gui.topicpanels.dockingpanel.WandoraDockController;
import org.wandora.application.gui.topicpanels.dockingpanel.WandoraDockable;
import org.wandora.application.gui.topicpanels.dockingpanel.WandoraSplitDockStation;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.ChainExecuter;
import org.wandora.application.tools.docking.AddDockable;
import org.wandora.application.tools.docking.DeleteAllDockables;
import org.wandora.application.tools.docking.DeleteCurrentDockable;
import org.wandora.application.tools.docking.DeleteDockable;
import org.wandora.application.tools.docking.MaximizeDockable;
import org.wandora.application.tools.docking.SelectDockable;
import org.wandora.application.tools.extractors.files.SimpleFileExtractor;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.utils.Base64;
import org.wandora.utils.DataURL;
import org.wandora.utils.DnDBox;
import org.wandora.utils.Options;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.dockable.AbstractDockable;
import bibliothek.gui.dock.event.DockableFocusEvent;
import bibliothek.gui.dock.event.DockableFocusListener;
import bibliothek.gui.dock.event.DockableListener;
import bibliothek.gui.dock.title.DockTitle;




/**
 * DockingFramePanel is a base topic panel of the Wandora application.
 * It is a topic panel container and can hold several different topic panels.
 * 
 * 
 * @author akivela
 */


public class DockingFramePanel extends JPanel implements TopicPanel, ActionListener, RefreshListener, TopicMapListener, DockableFocusListener, ComponentListener, DockableListener, DropTargetListener, DragGestureListener {

	private static final long serialVersionUID = 1L;


	private String OPTIONS_PREFIX = "gui.dockingFramePanel.";
    

    private HashMap<Dockable,TopicPanel> dockedTopicPanels;
    private Dockable currentDockable = null;
    private WandoraSplitDockStation station = null;
    private WandoraDockController control = null;
    private Wandora wandora = null;

    private BufferedImage backgroundImage = null;
    private int backgroundImageWidth = 0;
    private int backgroundImageHeight = 0;
    
    private DropTarget dropTarget = null;
    
    private HashMap<TopicPanel,TopicPanel> chainedTopicPanels = null;
    
    private Topic openedTopic = null;
    
    
    
    
    public DockingFramePanel() {
    }
    
    
    
    @Override
    public void init() {
        backgroundImage = UIBox.getImage("gui/startup_image.gif");
        backgroundImageWidth = backgroundImage.getWidth();
        backgroundImageHeight = backgroundImage.getHeight();

        wandora = Wandora.getWandora();
        dockedTopicPanels = new HashMap<>();
        chainedTopicPanels = new HashMap<>(); 
        control = new WandoraDockController();
        control.addDockableFocusListener(this);
        station = new WandoraSplitDockStation();
        control.add(station);
        
        this.setLayout(new BorderLayout () );
        this.add( station.getComponent() );
        this.addComponentListener(this);
        
        dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
    
    // -------------------------------------------------------------------------
    
    private int x = 0;
    private int y = 0;
    private int w = 0;
    private int h = 0;
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(dockedTopicPanels.isEmpty() && backgroundImage != null) {
            w = this.getWidth();
            h = this.getHeight();
            x = (w - backgroundImageWidth) / 2;
            y = (h - backgroundImageHeight) / 2;
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.drawImage(backgroundImage, x, y, this);
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public Dockable getDockableFor(TopicPanel topicPanel) {
        if(topicPanel != null) {
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel dockedTopicPanel = dockedTopicPanels.get(dockable);
                if(dockedTopicPanel.equals(topicPanel)) {
                    return dockable;
                }
            }
        }
        return null;
    }
    
    
    public void updateDockableTitle(TopicPanel topicPanel) {
        if(topicPanel != null) {
            Dockable dockable = getDockableFor(topicPanel);
            if(dockable != null) {
                if(dockable instanceof AbstractDockable) {
                    AbstractDockable abstractDockable = (AbstractDockable) dockable;
                    abstractDockable.setTitleText(topicPanel.getTitle());
                }
            }
        }
    }
    
    
    
    
    public void openTo(Topic topic, TopicPanel topicPanel) throws TopicMapException, OpenTopicNotSupportedException {
        for(Dockable dockable : dockedTopicPanels.keySet()) {
            TopicPanel dockedTopicPanel = dockedTopicPanels.get(dockable);
            if(dockedTopicPanel.equals(topicPanel)) {
                currentDockable = dockable;
                open(topic);
            }
        }
    }
    
    
    @Override
    public boolean supportsOpenTopic() {
        return true;
    }
    
    
    
    
    @Override
    public void open(Topic topic) throws TopicMapException, OpenTopicNotSupportedException {
        //System.out.println("initialize Docking Frame Panel");
        //String name = TopicToString.toString(topic);

        openedTopic = topic;
        
        if(currentDockable == null) {
            if(!dockedTopicPanels.isEmpty()) {
                currentDockable = dockedTopicPanels.keySet().iterator().next();
            }
        }
        if(currentDockable == null) {
            // Ensure there is at least one dockable when topic is opened.
            addDockable(getDefaultPanel(), topic);
        }
        else {
            control.setFocusedDockable(currentDockable, true);
            TopicPanel currentTopicPanel = dockedTopicPanels.get(currentDockable);
            if(currentTopicPanel != null) {
                if(currentTopicPanel.supportsOpenTopic()) {
                    currentTopicPanel.open(topic);
                    updateDockableTitle(currentTopicPanel);
                }
                else {
                    // We are going to ask the user where the topic will be opened.
                    ArrayList<TopicPanel> availableTopicPanels = new ArrayList();
                    for(Dockable dockable : dockedTopicPanels.keySet()) {
                        TopicPanel availableTopicPanel = dockedTopicPanels.get(dockable);
                        if(availableTopicPanel != null && availableTopicPanel.supportsOpenTopic()) {
                            availableTopicPanels.add( availableTopicPanel );
                        }
                    }
                    // Easy. No suitable topic panels available. Create one.
                    if(availableTopicPanels.isEmpty()) {
                        addDockable(getDefaultPanel(), topic);
                    }
                    // Easy. Only one suitable topic panel available. Use it.
                    else if(availableTopicPanels.size() == 1) {
                        TopicPanel alternativeTopicPanel = availableTopicPanels.get(0);
                        alternativeTopicPanel.open(topic);
                        updateDockableTitle(alternativeTopicPanel);
                    }
                    // Ask the user if we don't remember any earlier topic panel. 
                    else {
                        boolean askForATopicPanel = true;
                        TopicPanel rememberedTargetTopicPanel = chainedTopicPanels.get(currentTopicPanel);
                        if(rememberedTargetTopicPanel != null) {
                            Dockable dockable = this.getDockableFor(rememberedTargetTopicPanel);
                            if(dockable != null) {
                                rememberedTargetTopicPanel.open(topic);
                                updateDockableTitle(rememberedTargetTopicPanel);
                                askForATopicPanel = false;
                            }
                            else {
                                chainedTopicPanels.remove(currentTopicPanel);
                            }
                        }
                        if(askForATopicPanel) {
                            SelectTopicPanelPanel topicPanelSelector = new SelectTopicPanelPanel();
                            topicPanelSelector.openInDialog(availableTopicPanels, wandora);
                            // WAIT TILL CLOSED
                            if(topicPanelSelector.wasAccepted()) {
                                TopicPanel userSelectedTopicPanel = topicPanelSelector.getSelectedTopicPanel();
                                if(userSelectedTopicPanel != null) {
                                    userSelectedTopicPanel.open(topic);
                                    updateDockableTitle(userSelectedTopicPanel);
                                    if(topicPanelSelector.getRememberSelection()) {
                                        chainedTopicPanels.put(currentTopicPanel, userSelectedTopicPanel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        revalidate();
        repaint();
    }

    
    
    
    @Override
    public void stop() {
        if(dockedTopicPanels != null && dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                if(tp != null) {
                    tp.stop();
                }
            }
        }
        if(control != null) {
            control.kill();
        }
        openedTopic = null;
        dockedTopicPanels.clear();
        chainedTopicPanels.clear();
    }

    
    


    @Override
    public void refresh() throws TopicMapException {
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel topicPanel = dockedTopicPanels.get(dockable);
                if(topicPanel != null) {
                    //System.out.println("refresh topic panel at docking frame panel");
                    topicPanel.refresh();
                    updateDockableTitle(topicPanel);
                }
            }
        }
        revalidate();
        repaint();
    }

    
    
    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }
    
    
    
    
    
    public TopicPanel getCurrentTopicPanel() {
        if(currentDockable != null) {
            TopicPanel tp = dockedTopicPanels.get(currentDockable);
            return tp;
        }
        return null;
    }
    
    
    
    
    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        boolean reply = false;
         if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                if(tp != null) {
                    reply = tp.applyChanges() || reply;
                }
            }
        }
        //revalidate();
        //repaint();
        return reply;
    }
    
    

    @Override
    public JPanel getGui() {
        return this;
    }

    
    
    @Override
    public Topic getTopic() throws TopicMapException {
        if(currentDockable != null) {
            TopicPanel tp = dockedTopicPanels.get(currentDockable);
            if(tp != null) {
                return tp.getTopic();
            }
        }
        return openedTopic;
    }
    
    
    @Override
    public String getName() {
        return "Dockable frame panel";
    }

    @Override
    public String getTitle() {
        return getName();
    }
    
    
    @Override
    public Icon getIcon() {
        if(currentDockable != null) {
            TopicPanel tp = dockedTopicPanels.get(currentDockable);
            if(tp != null) {
                return tp.getIcon();
            }
        }
        return null;
    }
    
    
    

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean noScroll(){
        return false;
    }
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }
    
    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }
    


    @Override
    public Object[] getViewMenuStruct() {
        JMenu addMenu =  new SimpleMenu("New panel", UIBox.getIcon("gui/icons/topic_panel_add.png"));
        List<List> availableTopicPanels = wandora.topicPanelManager.getAvailableTopicPanels();
        ArrayList addTopicPanelMenuStruct = new ArrayList();
        for(List panelData : availableTopicPanels) {
            try {
                Class panelClass = Class.forName((String) panelData.get(0));
                if(!this.getClass().equals(panelClass)) {
                    addTopicPanelMenuStruct.add( (String) panelData.get(1) );
                    addTopicPanelMenuStruct.add( (Icon) panelData.get(2) );
                    addTopicPanelMenuStruct.add( new AddDockable( panelClass ) );
                }
            }
            catch(Exception e) {}
            
        }

        UIBox.attachMenu(addMenu, addTopicPanelMenuStruct.toArray(new Object[] {} ), wandora);
        
        //JMenu selectMenu =  new SimpleMenu("Select", UIBox.getIcon("gui/icons/topic_panel_select.png"));
        //UIBox.attachMenu(selectMenu, getSelectMenuStruct(), wandora);
        
        //JMenu closeMenu =  new SimpleMenu("Close", UIBox.getIcon("gui/icons/topic_panel_close.png"));
        //UIBox.attachMenu(closeMenu, getCloseMenuStruct(), wandora);

        //JMenu optionsMenu =  new SimpleMenu("Options", UIBox.getIcon("gui/icons/topic_panel_options.png"));
        //UIBox.attachMenu(optionsMenu, getOptionsMenuStruct(), wandora);

        ArrayList struct = new ArrayList();
        
        struct.add(addMenu);
               
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            struct.add("---");
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel tp = dockedTopicPanels.get(dockable);
                if(tp != null) {
                    String label = tp.getName();
                    label = label + getAdditionalLabel(tp);
                    JMenu topicPanelMenu =  new SimpleMenu(label, tp.getIcon());
                    
                    ArrayList subStruct = new ArrayList();
                    
                    Object[] subMenuStruct = tp.getViewMenuStruct();
                    if(subMenuStruct != null) {
                        if(subMenuStruct.length > 0) {
                            for( Object subMenuItem : subMenuStruct ) {
                                subStruct.add(subMenuItem);
                            }
                            subStruct.add("---");
                        }
                    }
                    
                    subStruct.add( "Select" );
                    subStruct.add( UIBox.getIcon( "gui/icons/select_dockable.png" ) );
                    subStruct.add( new SelectDockable(dockable) );
                    
                    if(dockable.equals(station.getFullScreen())) {
                        subStruct.add( "Demaximize" );
                        subStruct.add( UIBox.getIcon( "gui/icons/demaximize_dockable.png" ) );
                    }
                    else {
                        subStruct.add( "Maximize" );
                        subStruct.add( UIBox.getIcon( "gui/icons/maximize_dockable.png" ) );
                    }
                    subStruct.add( new MaximizeDockable(dockable) );

                    subStruct.add( "Close" );
                    subStruct.add( UIBox.getIcon("gui/icons/close_dockable.png") );
                    subStruct.add( new DeleteDockable(dockable) );
                    
                    UIBox.attachMenu(topicPanelMenu, subStruct.toArray(), wandora);
                    
                    struct.add( topicPanelMenu );
                }
            }
            struct.add("---");
            struct.add("Close current");
            struct.add(new DeleteCurrentDockable());
            struct.add("Close all");
            struct.add(new DeleteAllDockables());
        }
        
        return struct.toArray();
    }
    
    

    
    
    public HashMap getDockedTopicPanels() {
        return (HashMap) dockedTopicPanels.clone();
    }
    
    
    
    
    private Object[] getCloseMenuStruct() {
        ArrayList struct = new ArrayList();
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel tp = dockedTopicPanels.get(dockable);
                if(tp != null) {
                    String label = "Close "+tp.getName();
                    label = label + getAdditionalLabel(tp);
                    struct.add(label);
                    struct.add( tp.getIcon() );
                    struct.add( new DeleteDockable(dockable) );
                }
            }
        }
        else {
            struct.add("[Nothing to close]");
        }
        return struct.toArray( new Object[] {} );
    }
    
    
    private Object[] getSelectMenuStruct() {
        ArrayList struct = new ArrayList();
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel tp = dockedTopicPanels.get(dockable);
                if(tp != null) {
                    String label = "Select "+tp.getName();
                    label = label + getAdditionalLabel(tp);
                    struct.add(label);
                    struct.add( tp.getIcon() );
                    struct.add( new SelectDockable(dockable) );
                }
            }
        }
        else {
            struct.add("[Nothing to select]");
        }
        return struct.toArray( new Object[] {} );
    }
    
    
    
    
    private Object[] getOptionsMenuStruct() {
        ArrayList struct = new ArrayList();
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel tp = dockedTopicPanels.get(dockable);
                if(tp != null) {
                    String label = "Configure "+tp.getName();
                    label = label + getAdditionalLabel(tp);
                    SimpleMenu confMenu = new SimpleMenu(label);
                    confMenu.setIcon(tp.getIcon());
                    UIBox.attachMenu(confMenu, tp.getViewMenuStruct(), wandora);
                    struct.add( confMenu );
                }
            }
        }
        else {
            struct.add("[Nothing to configure]");
        }
        return struct.toArray( new Object[] {} );
    }
    

    
    public static String getAdditionalLabel(TopicPanel tp) {
        String additionalLabel = "";
        try {
            Topic t = tp.getTopic();
            if(t != null) {
                additionalLabel = TopicToString.toString(t);
                if(additionalLabel.length() > 30) {
                    additionalLabel = additionalLabel.substring(0,27) + "...";
                }
                additionalLabel = " w " + additionalLabel;
            }
        }
        catch(Exception e) {
            // IGNORE
        }
        return additionalLabel;
    }

    
    

    // -------------------------------------------------------------------------

    
    
    public void toggleVisibility(String componentName) {
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
    
    
    
    public void addDockable(Object c, Topic topic) {
        addDockable((TopicPanel) c, topic);
    }
    
    
    public void addDockable(TopicPanel tp, Topic topic) {
        //System.out.append("Adding dockable "+tp);
        
        if(tp == null) return;
        
        Icon icon = tp.getIcon();

        try {
            tp.init();
            if(tp.supportsOpenTopic()) {
                if(topic == null) topic = openedTopic;
                tp.open(topic);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        if(tp.noScroll()) {
            wrapper.add(tp.getGui(), BorderLayout.CENTER);            
        }
        else {
            JScrollPane wrapperScroll = new SimpleScrollPane(tp.getGui());
            wrapper.add(wrapperScroll, BorderLayout.CENTER);
        }

        WandoraDockable d = new WandoraDockable(wrapper, tp, tp.getTitle(), icon);
        d.setActionOffers(new WandoraDockActionSource(tp, this, control));
        
        station.addDockable(d);
        dockedTopicPanels.put(d, tp);
        currentDockable = d;
        
        control.setFocusedDockable(currentDockable, true);
        
        wandora.topicPanelsChanged();
    }

    
    
    public void deleteCurrentDockable() {
        if(currentDockable != null) {
            TopicPanel tp = dockedTopicPanels.get(currentDockable);
            if(tp != null) {
                tp.stop();
            }
            dockedTopicPanels.remove(currentDockable);
            station.removeDockable(currentDockable);
            
            if(!dockedTopicPanels.isEmpty()) {
                currentDockable = dockedTopicPanels.keySet().iterator().next();
            }
            else {
                currentDockable = null;
            }
            wandora.topicPanelsChanged();
        }
    }
    
    
    
    
    
    public void deleteAllDockables() {
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(Dockable dockable : dockedTopicPanels.keySet()) {
                TopicPanel tp = dockedTopicPanels.get(dockable);
                if(tp != null) {
                    tp.stop();
                }
                station.removeDockable(dockable);
            }
            dockedTopicPanels.clear();
            chainedTopicPanels.clear();
            currentDockable = null;
            wandora.topicPanelsChanged();
        }
    }
    
    
    
    
    public void deleteDockable(Dockable dockable) {
        TopicPanel tp = dockedTopicPanels.get(dockable);
        if(tp != null) {
            tp.stop();
        }
        dockedTopicPanels.remove(dockable);
        station.removeDockable(dockable);
        if(!dockedTopicPanels.isEmpty()) {
            currentDockable = dockedTopicPanels.keySet().iterator().next();
        }
        else {
            currentDockable = null;
        }
        wandora.topicPanelsChanged();
    }
    
    
    
    
    public void selectDockable(Dockable dockable) {
        control.setFocusedDockable(dockable, true);
    }
    
    
    
    
    
    public void maximizeDockable(Dockable dockable) {
        if(dockable != null) {
            if(dockable.equals(station.getFullScreen())) {
                station.setFullScreen(null);
                try {
                    if(currentDockable != null) {
                        TopicPanel tp = dockedTopicPanels.get(dockable);
                        if(tp != null) {
                            tp.refresh();
                        }
                    }
                }
                catch(Exception e) {}
            }
            else {
                station.setFullScreen(dockable);
            }
        }
    }
    
    
    
    
    public void changeTopicPanelInCurrentDockable(TopicPanel tp, Topic topic) {
        //System.out.append("changeTopicPanelInDockable "+tp);
        
        if(tp == null || currentDockable == null) return;

        try {
            if(currentDockable instanceof WandoraDockable) {
                try { 
                    if(tp.supportsOpenTopic()) {
                        tp.open(topic); 
                    }
                }
                catch(Exception e) { 
                    e.printStackTrace(); 
                }
                WandoraDockable currentWandoraDockable = (WandoraDockable) currentDockable;
                TopicPanel oldTopicPanel = currentWandoraDockable.getInnerTopicPanel();
                oldTopicPanel.applyChanges();
                oldTopicPanel.stop();

                currentWandoraDockable.setTitleIcon(tp.getIcon());
                currentWandoraDockable.setTitleText(tp.getTitle());
                JPanel wrapper = (JPanel) currentWandoraDockable.getWrapper();
                wrapper.removeAll();
                JScrollPane wrapperScroll = new SimpleScrollPane(tp.getGui());
                wrapper.add(wrapperScroll, BorderLayout.CENTER);
                
                dockedTopicPanels.put(currentDockable, tp);
            }

            control.setFocusedDockable(currentDockable, true);
            wandora.topicPanelsChanged();
            wandora.refreshTopicPanelIcon();
        }
        catch(CancelledException ce) {
            // PASS
        }
        catch(TopicMapException te) {
            te.printStackTrace();
        }
    }

    
    
    
    // ----------------------------------------------- DockableFocusListener ---
    
    
    @Override
    public void dockableFocused(DockableFocusEvent dfe) {
        if(!dockedTopicPanels.isEmpty()) {
            if(dfe.getNewFocusOwner() != null) {
                currentDockable = dfe.getNewFocusOwner();
                if(wandora != null) {
                    wandora.refreshInfoFields();
                }
            }
        }
    }

    
    
    
    // ----------------------------------------------------- RefreshListener ---

    @Override
    public void doRefresh() throws TopicMapException {
        refresh();
    }

    
    // -------------------------------------------------- Topic map listener ---
    
    
    private boolean skipTopicMapActions = false;
    
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicSubjectIdentifierChanged(t, added, removed);
            }
        }
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicBaseNameChanged(t, newName, oldName);
            }
        }
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicTypeChanged(t, added, removed);
            }
        }
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicVariantChanged(t, scope, newName, oldName);
            }
        }
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicDataChanged(t, type, version, newValue, oldValue);
            }
        }
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicSubjectLocatorChanged(t, newLocator, oldLocator);
            }
        }
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicRemoved(t);
            }
        }
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.topicChanged(t);
            }
        }
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.associationTypeChanged(a, newType, oldType);
            }
        }
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.associationPlayerChanged(a, role, newPlayer, oldPlayer);
            }
        }
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.associationRemoved(a);
            }
        }
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        if(skipTopicMapActions) return;
        if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
            for(TopicPanel tp : dockedTopicPanels.values()) {
                tp.associationChanged(a);
            }
        }
    }
    
    
    
    
    // -------------------------------------------------- Component listener ---
    
    

    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        handleComponentEvent(e);
    }
    
    
    
    private void handleComponentEvent(ComponentEvent e) {
        try {
            Dimension size = this.getParent().getParent().getSize();
            setPreferredSize(size);
            setMinimumSize(size);
            setSize(size);
            //revalidate();
            //repaint();
        }
        catch(Exception ex) {
            // SKIP
        }
    }

    
    
    // --------------------------------------------------- DockableListener ----
    
    
    @Override
    public void titleBound(Dockable dckbl, DockTitle dt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void titleUnbound(Dockable dckbl, DockTitle dt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void titleTextChanged(Dockable dckbl, String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void titleIconChanged(Dockable dckbl, Icon icon, Icon icon1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void titleToolTipChanged(Dockable dckbl, String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void titleExchanged(Dockable dckbl, DockTitle dt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------ DRAG AND DROP ----
    // -------------------------------------------------------------------------
    
    
    private int orders = 0;
    
    
    @Override
    public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        
    }
    
    
    @Override
    public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
        
    }
    
    
    @Override
    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        
    }
    
    
    
    private void processImage(Image image) throws Exception {
        SimpleFileExtractor extractor = new SimpleFileExtractor();
        DataURL dataUrl = new DataURL(image);
        extractor.setForceContent( dataUrl.toExternalForm(Base64.DONT_BREAK_LINES) );
        ActionEvent fakeEvent = new ActionEvent(wandora, 0, "merge");
        extractor.execute(wandora, fakeEvent);
    }
    
    
    private void processFileList(java.util.List<File> files) throws Exception {
        ArrayList<WandoraTool> importTools = new ArrayList<>();
        boolean yesToAll = false;
        
        for(File file : files) {
            ArrayList<WandoraTool> importToolsForFile = WandoraToolManager.getImportTools(file, orders);
            if(importToolsForFile != null && !importToolsForFile.isEmpty()) {
                importTools.addAll(importToolsForFile);
            }
            else {
                if(yesToAll) {
                    SimpleFileExtractor extractor = new SimpleFileExtractor();
                    extractor.setForceFiles(new File[] { file } );
                    importTools.add(extractor);
                }
                else {
                    int a = WandoraOptionPane.showConfirmDialog(wandora, 
                        "Extract the dropped file with Simple File Extractor?",
                        "Extract instead of import?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                    if(a == WandoraOptionPane.YES_OPTION || a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                        SimpleFileExtractor extractor = new SimpleFileExtractor();
                        extractor.setForceFiles(new File[] { file } );
                        importTools.add(extractor);
                    }
                    if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                        yesToAll = true;
                    }
                    if(a == WandoraOptionPane.CANCEL_OPTION || a == WandoraOptionPane.CLOSED_OPTION) {
                        return;
                    }
                }               
            }
        }
        //System.out.println("drop context == " + dropContext);
        ActionEvent fakeEvent = new ActionEvent(wandora, 0, "merge");
        ChainExecuter chainExecuter = new ChainExecuter(importTools);
        chainExecuter.execute(wandora, fakeEvent);
    }

    
    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {

        try {
            final Image image = DnDBox.acceptImage(e);
            if(image != null) {
                Thread dropThread = new Thread() {
                    public void run() {
                        try {
                            processImage(image);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                        catch(Error err) {
                            err.printStackTrace();
                        }
                    }
                };
                dropThread.start(); 
            }

            else {
                final java.util.List<File> files = DnDBox.acceptFileList(e);
                if(files != null) {
                    if(!files.isEmpty()) {
                        Thread dropThread = new Thread() {
                            public void run() {
                                try {
                                    processFileList(files);
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                                catch(Error err) {
                                    err.printStackTrace();
                                }
                            }
                        };
                        dropThread.start();
                    }
                }
                else {
                    System.out.println("Drop rejected! Wrong data flavor!");
                    e.rejectDrop();
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        catch(Error err) {
            err.printStackTrace();
        }

        this.setBorder(null);
    }
    
    @Override
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

    @Override
    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent dragGestureEvent) {
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected TopicPanel getDefaultPanel() {
        try {
            Options options = wandora.getOptions();
            String defaultPanelClassName = options.get("gui.topicPanels.defaultPanel");
            if(defaultPanelClassName != null) {
                Class topicPanelClass = Class.forName(defaultPanelClassName);
                if(TopicPanel.class.isAssignableFrom(topicPanelClass) &&
                        !Modifier.isAbstract(topicPanelClass.getModifiers()) &&
                        !Modifier.isInterface(topicPanelClass.getModifiers()) ){
                    return (TopicPanel) topicPanelClass.newInstance();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        // Backup plan if default panel fails.
        return new TraditionalTopicPanel();
    }
    
}
