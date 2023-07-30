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
 * TopicPanelManager.java
 *
 * Created on 19. lokakuuta 2005, 19:23
 *
 */

package org.wandora.application.gui.topicpanels;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.wandora.application.CancelledException;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.EditorPanel;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.topicmap.TopicMapReadOnlyException;
import org.wandora.topicmap.TopicTools;
import org.wandora.utils.Options;



/**
 * @author akivela
 */
public class TopicPanelManager implements ActionListener {

    private Set<String> topicPanelsSupportingOpenTopic = new LinkedHashSet();
    private Map<String,String> topicPanelMap = new LinkedHashMap();
    private Map<String,Integer> topicPanelOrder = new LinkedHashMap();
    private Map<String,Icon> topicPanelIcon = new LinkedHashMap();
    private Wandora wandora;
    private TopicPanel oldTopicPanel = null;
    private TopicPanel baseTopicPanel;
    private String baseTopicPanelClassName;
    private Options options;

    
    
    
    private KeyStroke[] accelerators = new KeyStroke[] {
        KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.ALT_DOWN_MASK ),
        KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.ALT_DOWN_MASK ),
    };

    
    
    public TopicPanelManager(Wandora w) {
        this.wandora = w;
        this.options = w.options;
        try {
            baseTopicPanelClassName = options.get("gui.topicPanels.base");
            initialize();
        }
        catch (Exception e) {
            
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void reset() {
        deactivateTopicPanel();
        TopicPanel tp = getTopicPanel();
        activateTopicPanel();
        wandora.topicPanelsChanged();
    }
    
    
    

    public void setTopicPanel(String topicPanelName)  throws TopicMapException {
        if(topicPanelName != null) {
            String topicPanelClassName = topicPanelMap.get(topicPanelName);
            if(topicPanelClassName != null && !topicPanelClassName.equals(baseTopicPanelClassName)) {
                baseTopicPanelClassName = topicPanelClassName;
                options.put("gui.topicPanels.base", baseTopicPanelClassName);
                baseTopicPanel = getTopicPanel();
            }
        }
    }

    
    
    
    public TopicPanel getCurrentTopicPanel() {
        return baseTopicPanel;
    }

    
    
    
    public TopicPanel getTopicPanel() {
        try {
            boolean reuse=false;
            if(baseTopicPanel!=null) {
                if(baseTopicPanel.getClass().getName().equals(baseTopicPanelClassName)) {
                    reuse=true;
                }
            }
            if(!reuse) {
                if(baseTopicPanel != null) {
                    baseTopicPanel.stop();
                }
                baseTopicPanel = getTopicPanelWithClassName(baseTopicPanelClassName);
                if(baseTopicPanel != null) {
                    baseTopicPanel.init();
                }
            }
            if(baseTopicPanel == null) {
                baseTopicPanel = new DockingFramePanel();
                baseTopicPanel.init();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return baseTopicPanel;
    }
    
    
    
    public TopicPanel getTopicPanel(String topicPanelName) {
        TopicPanel topicPanel = null;

        try {
            String topicPanelClassName = (String) topicPanelMap.get(topicPanelName);
            topicPanel = getTopicPanelWithClassName(topicPanelClassName);
        }
        catch (Exception e) {
            System.out.println("No topic panel for name: " + topicPanelName);
            if(wandora != null) wandora.handleError(e);
        }
        return topicPanel;
    }
    

    
    
    
    
    public TopicPanel getTopicPanelWithClassName(String topicPanelClassName) throws Exception {
        TopicPanel topicPanel = null;
        try {
            if(topicPanelClassName != null) {
                if(!topicPanelClassName.contains("$")) { // Skip inner classes!
                    Class topicPanelClass = Class.forName(topicPanelClassName);
                    if(TopicPanel.class.isAssignableFrom(topicPanelClass) &&
                            !Modifier.isAbstract(topicPanelClass.getModifiers()) &&
                            !Modifier.isInterface(topicPanelClass.getModifiers()) ){
                        topicPanel = (TopicPanel) topicPanelClass.newInstance();
                    }
                }
            }
        }
        catch (Exception e) {
            Wandora.getWandora().handleError(e);
            // All kinds of exceptions are caught here, some because we try
            // to instantiate something we shouldn't and something which we
            // might care about. Ideally would catch a bit more specifically.
            
            //e.printStackTrace();
        }
        return topicPanel;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void initialize() {
        try {
            int panelCounter = 0;
            String className = null;
            do {
                className = options.get("gui.topicPanels.panel["+panelCounter+"]");
                if(className != null) {
                    TopicPanel topicPanel = getTopicPanelWithClassName(className);
                    if(topicPanel != null) {
                        //System.out.println("TopicPanel class found: " + className);
                        String panelName = topicPanel.getName();
                        topicPanelMap.put(panelName, className);
                        topicPanelOrder.put(panelName, topicPanel.getOrder());
                        topicPanelIcon.put(panelName, topicPanel.getIcon());
                        if(topicPanel.supportsOpenTopic()) {
                            topicPanelsSupportingOpenTopic.add(className);
                        }
                    }
                }
                panelCounter++;
            }
            while(className != null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    
    public List<List> getAvailableTopicPanels() {
        ArrayList availablePanels = new ArrayList<ArrayList>();
        for(String panelName : sortedTopicPanels()) {
            ArrayList panelData = new ArrayList();
            String panelClass = topicPanelMap.get(panelName);
            panelData.add(panelClass);
            panelData.add(panelName);
            panelData.add(topicPanelIcon.get(panelName));
            
            availablePanels.add(panelData);
        }
        
        return availablePanels;
    }
    
    
    
    public List<List> getAvailableTopicPanelsSupportingOpenTopic() {
        ArrayList availablePanels = new ArrayList<ArrayList>();
        for(String panelName : sortedTopicPanels()) {
            ArrayList panelData = new ArrayList();
            String panelClass = topicPanelMap.get(panelName);
            if(topicPanelsSupportingOpenTopic.contains(panelClass)) {
                panelData.add(panelClass);
                panelData.add(panelName);
                panelData.add(topicPanelIcon.get(panelName));

                availablePanels.add(panelData);
            }
        }
        
        return availablePanels;
    }
    
    
    
    
    // -------------------------------------------------------------------------

    
    
    public Object[] getViewMenuStruct() {
        if(baseTopicPanel != null) {
            return baseTopicPanel.getViewMenuStruct();
            // Current configuration of Wandora sets baseTopicPanel to 
            // DockingFramePanel. See getViewMenuStruct method in DockingFramePanel.
        }
        else {
            return new Object[] { };
        }
    }
    
    

    public SimpleMenu getTopicPanelMenu() {
        return (SimpleMenu)getTopicPanelMenu(null);
    }
    
    public JComponent getTopicPanelMenu(JComponent topicPanelMenu) {
        SimpleMenuItem topicPanelMenuItem = null;
        if(topicPanelMenu==null) {
            topicPanelMenu = new SimpleMenu("New panel");
            ((SimpleMenu)topicPanelMenu).setIcon(UIBox.getIcon("gui/icons/topic_panels.png"));
        }
        
        int numberOfTopicPanels = 0;
        for( String topicPanelName : sortedTopicPanels() ) {
            try {
                TopicPanel topicPanel = getTopicPanel(topicPanelName);
                if(topicPanel != null) {
                    topicPanelMenuItem = new SimpleMenuItem(topicPanel.getName(), this);
                    if(topicPanelMenuItem != null) {
                        if(numberOfTopicPanels < accelerators.length) {
                            //topicPanelMenuItem.setAccelerator(accelerators[numberOfTopicPanels]);
                        }
                        numberOfTopicPanels++;
                        topicPanelMenuItem.setIcon(topicPanel.getIcon());
                        topicPanelMenu.add(topicPanelMenuItem);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return topicPanelMenu;
    }

    
    
    /**
     * Returns topic panels in the same order as they were read from
     * options. Old deprecated implementation used to sort topic panels
     * using a special sort value returned by a topic panel.
     */
    private Collection<String> sortedTopicPanels() {
        ArrayList<String> sortedPanels = new ArrayList();
        sortedPanels.addAll(topicPanelOrder.keySet());

        /*
        Collections.sort(sortedPanels, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                int oi1 = topicPanelOrder.get(o1).intValue();
                int oi2 = topicPanelOrder.get(o2).intValue();
                if(oi1 == oi2) return 0;
                if(oi1 > oi2) return 1;
                else return -1;
            }
        });
        */
        return sortedPanels;
    }
    
    
    public HashMap getDockedTopicPanels() {
        if(baseTopicPanel != null && baseTopicPanel instanceof DockingFramePanel) {
            return ((DockingFramePanel) baseTopicPanel).getDockedTopicPanels();
        }
        return null;
    }
    
    
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if(actionCommand != null) {
            //WAS: setTopicPanel(actionCommand);
            try {
                DockingFramePanel dockingPanel = (DockingFramePanel) baseTopicPanel;
                String dockableClassName = topicPanelMap.get(actionCommand);
                Class dockableClass = Class.forName(dockableClassName);
                if(dockableClass != null && dockingPanel != null) {
                    TopicPanel topicPanel = (TopicPanel) dockableClass.newInstance();
                    topicPanel.init();
                    dockingPanel.changeTopicPanelInCurrentDockable(topicPanel, getOpenTopic());
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            //System.out.println("Topic panel manager activation!");
        }
    }
    
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- OPEN TOPIC ---
    // -------------------------------------------------------------------------
    


    /**
     * Open the topic again in the base topic panel.
     */
    public void reopenTopic() throws TopicMapException, OpenTopicNotSupportedException {
        if(baseTopicPanel != null) {
            openTopic(baseTopicPanel.getTopic());
        }
    }
    

    
    
    
    /**
     * Enforces base topic panel to save all changes. 
     */
    public boolean applyChanges() {
        try {
            if(baseTopicPanel != null) {
                baseTopicPanel.applyChanges();
                return true;
            }
            else {
                return true;
            }
        }
        catch(TopicMapReadOnlyException troe) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, 
                    "You have locked current topic map layer. Can't apply changes unless you unlock the layer. Press Cancel to reject changes.", 
                    "Unlock current topic map layer", 
                    WandoraOptionPane.OK_CANCEL_OPTION); 
            if(a == WandoraOptionPane.CANCEL_OPTION) {
                return true;
            }
        }
        catch(TopicMapException tme){ 
            wandora.handleError(tme);
        }
        catch(CancelledException ce) {
        }
        catch(Exception exc) {
        }
        return false;
    }
    
    

    
    
    
    /**
     * Opens the argument topic in the base topic panel. DockingFramePanel
     * opens the topic in current dockable.
     * 
     * @param topic to be opened in the current topic panel.
     */
    public void openTopic(Topic topic) throws TopicMapException, OpenTopicNotSupportedException {
        if(topic == null || topic.isRemoved()) return;
        
        if(topic.getSubjectIdentifiers().isEmpty()) {
            topic.addSubjectIdentifier(TopicTools.createDefaultLocator());
        }
        
        if(baseTopicPanel == null || !baseTopicPanel.equals(oldTopicPanel)) {
            deactivateTopicPanel();
        }
        getTopicPanel().open(topic);
        if(!baseTopicPanel.equals(oldTopicPanel)) {
            activateTopicPanel();
        }
        oldTopicPanel = baseTopicPanel;

        wandora.topicPanelsChanged(); 
    }
    
    
    
    
    
    
    /**
     * This method is called always before topic panel changes. It removes the
     * panel from Wandora's UI and removes listeners.
     */
    protected void deactivateTopicPanel() {
        wandora.editorPanel.removeAll();
        
        if(baseTopicPanel != null) {
            if(baseTopicPanel instanceof TopicMapListener) {
                wandora.removeTopicMapListener((TopicMapListener)baseTopicPanel);
            }
            if(baseTopicPanel instanceof RefreshListener) {
                wandora.removeRefreshListener((RefreshListener)baseTopicPanel);
            }
        }
    }
    
    
    
    /**
     * This method is called always after topic panel changes. It attaches
     * the topic panel to Wandora's main window.
     */
    protected void activateTopicPanel() {
        if(baseTopicPanel instanceof TopicMapListener) {
            wandora.addTopicMapListener((TopicMapListener)baseTopicPanel);
        }
        if(baseTopicPanel instanceof RefreshListener) {
            wandora.addRefreshListener((RefreshListener)baseTopicPanel);
        }

        ((EditorPanel)wandora.editorPanel).addTopicPanel(baseTopicPanel);
        ((EditorPanel)wandora.editorPanel).revalidate();
        ((EditorPanel)wandora.editorPanel).repaint();
    }
    
    
    

    
    
    
    /**
     * Returns a topic that is open in base topic panel. 
     * DockingFramePanel returns the topic in current dockable.
     * 
     * @return Current topic opened in the panel.
     */
    public Topic getOpenTopic() {
        if(baseTopicPanel!=null) {
            try {
                return baseTopicPanel.getTopic();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
    
    /**
     * Returns an icon for base topic panel. DockingFramePanel 
     * returns the icon for current dockable.
     * 
     * @return Topic panel's icon.
     */
    public Icon getIcon() {
        if(baseTopicPanel!=null) {
            try {
                return baseTopicPanel.getIcon();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
}
