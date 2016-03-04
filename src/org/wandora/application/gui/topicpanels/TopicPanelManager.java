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
 * TopicPanelManager.java
 *
 * Created on 19. lokakuuta 2005, 19:23
 *
 */

package org.wandora.application.gui.topicpanels;


import javax.swing.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import java.lang.reflect.Modifier;

import org.wandora.utils.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.exceptions.OpenTopicNotSupportedException;



/**
 * @author akivela
 */
public class TopicPanelManager implements ActionListener {

    private HashSet<String> topicPanelsSupportingOpenTopic = new HashSet();
    private HashMap<String,String> topicPanelMap = new HashMap();
    private HashMap<String,Integer> topicPanelOrder = new HashMap();
    private HashMap<String,Icon> topicPanelIcon = new HashMap();
    private Wandora wandora;
    private TopicPanel oldTopicPanel = null;
    private TopicPanel currentTopicPanel;
    private String currentTopicPanelName;
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
            currentTopicPanelName = options.get("gui.topicPanels.current");
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
        if(topicPanelName != null && !topicPanelName.equals(currentTopicPanelName)) {
            currentTopicPanelName = topicPanelName;
            options.put("gui.topicPanels.current", currentTopicPanelName);
            currentTopicPanel = getTopicPanel();
        }
    }

    
    
    
    public TopicPanel getCurrentTopicPanel() {
        return currentTopicPanel;
    }

    
    
    
    public TopicPanel getTopicPanel() {
        try {
            boolean reuse=false;
            if(currentTopicPanel!=null) {
                if(currentTopicPanel.getClass().getName().equals(topicPanelMap.get(currentTopicPanelName))) {
                    reuse=true;
                }
            }
            if(!reuse) {
                if(currentTopicPanel != null) {
                    currentTopicPanel.stop();
                }
                currentTopicPanel = getTopicPanel(currentTopicPanelName);
                currentTopicPanel.init();
            }
            if(currentTopicPanel == null) {
                currentTopicPanel = new DockingFramePanel();
                currentTopicPanel.init();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return currentTopicPanel;
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
            boolean continueSearch = true;
            int pathCounter = -1;
            ArrayList<String> paths = new ArrayList<String>();
            while(continueSearch) {
                pathCounter++;
                String topicPanelResourcePath = options.get("gui.topicPanels.path["+pathCounter+"]");
                if(topicPanelResourcePath == null || topicPanelResourcePath.length() == 0) {
                    topicPanelResourcePath = "org/wandora/application/gui/topicpanels";
                    //System.out.println("Using default topicpanel resource path: " + topicPanelResourcePath);
                    continueSearch = false;
                }
                if(paths.contains(topicPanelResourcePath)) continue;
                paths.add(topicPanelResourcePath);
                String classPath = topicPanelResourcePath.replace('/', '.');
                Enumeration topicPanelResources = ClassLoader.getSystemResources(topicPanelResourcePath);
                
                while(topicPanelResources.hasMoreElements()) {
                    URL topicPanelBaseUrl = (URL) topicPanelResources.nextElement();
                    if(topicPanelBaseUrl.toExternalForm().startsWith("file:")) {
                        String baseDir = URLDecoder.decode(topicPanelBaseUrl.toExternalForm().substring(6), "UTF-8");
                        if(!baseDir.startsWith("/") && !baseDir.startsWith("\\") && baseDir.charAt(1)!=':') 
                            baseDir="/"+baseDir;
                        //System.out.println("Basedir: " + baseDir);
                        HashSet<String> topicPanelFileNames = IObox.getFilesAsHash(baseDir, ".*\\.class", 1, 1000);
                        for(String classFileName : topicPanelFileNames) {
                            try {
                                File classFile = new File(classFileName);
                                String className = classPath + "." + classFile.getName().replaceFirst(".class", "");
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
                                else {
                                     //System.out.println("NOT A TopicPanel!!! " + className);
                                }
                            }
                            catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public ArrayList<ArrayList> getAvailableTopicPanels() {
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
    
    
    
    public ArrayList<ArrayList> getAvailableTopicPanelsSupportingOpenTopic() {
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
        if(currentTopicPanel != null) {
            return currentTopicPanel.getViewMenuStruct();
            // Current configuration of Wandora sets currentTopicPanel to 
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
            // As the base topic panel in dockable topic panel, next line is there to prevent recursive dockable panels.
            if(topicPanelName.startsWith("Dockable")) continue;
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

    
    
    private Collection<String> sortedTopicPanels() {
        ArrayList<String> sortedPanels = new ArrayList();
        sortedPanels.addAll(topicPanelOrder.keySet());

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
        return sortedPanels;
    }
    
    
    public HashMap getDockedTopicPanels() {
        if(currentTopicPanel != null && currentTopicPanel instanceof DockingFramePanel) {
            return ((DockingFramePanel) currentTopicPanel).getDockedTopicPanels();
        }
        return null;
    }
    
    
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if(actionCommand != null) {
            //WAS: setTopicPanel(actionCommand);
            try {
                DockingFramePanel dockingPanel = (DockingFramePanel) currentTopicPanel;
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
    


    
    public void reopenTopic() throws TopicMapException, OpenTopicNotSupportedException {
        if(currentTopicPanel != null) {
            openTopic(currentTopicPanel.getTopic());
        }
    }
    

    
    
    
    /*
     * Enforces current topic panel to save all changes. 
     */
    public boolean applyChanges() {
        try {
            if(currentTopicPanel != null) {
                currentTopicPanel.applyChanges();
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
    
    

    
    
    
    /*
     * Opens an argument topic in current TopicPanel. Notice, DockingFramePanel
     * opens topic in current dockable.
     */
    public void openTopic(Topic topic) throws TopicMapException, OpenTopicNotSupportedException {
        if(topic == null || topic.isRemoved()) return;
        
        if(topic.getSubjectIdentifiers().isEmpty()) {
            topic.addSubjectIdentifier(TopicTools.createDefaultLocator());
        }
        
        if(currentTopicPanel == null || !currentTopicPanel.equals(oldTopicPanel)) {
            deactivateTopicPanel();
        }
        getTopicPanel().open(topic);
        if(!currentTopicPanel.equals(oldTopicPanel)) {
            activateTopicPanel();
        }
        oldTopicPanel = currentTopicPanel;

        wandora.topicPanelsChanged(); 
    }
    
    
    
    
    
    
    /*
     * This method is called always before topic panel changes. It removes the
     * panel from Wandora's UI and deattaches used listeners.
     */
    protected void deactivateTopicPanel() {
        wandora.editorPanel.removeAll();
        
        if(currentTopicPanel != null) {
            if(currentTopicPanel instanceof TopicMapListener) {
                wandora.removeTopicMapListener((TopicMapListener)currentTopicPanel);
            }
            if(currentTopicPanel instanceof RefreshListener) {
                wandora.removeRefreshListener((RefreshListener)currentTopicPanel);
            }
        }
    }
    
    
    
    /*
     * This method is called always after topic panel changes. It attaches
     * the topic panel to Wandora's main window.
     */
    protected void activateTopicPanel() {
        if(currentTopicPanel instanceof TopicMapListener) {
            wandora.addTopicMapListener((TopicMapListener)currentTopicPanel);
        }
        if(currentTopicPanel instanceof RefreshListener) {
            wandora.addRefreshListener((RefreshListener)currentTopicPanel);
        }

        ((EditorPanel)wandora.editorPanel).addTopicPanel(currentTopicPanel);
        ((EditorPanel)wandora.editorPanel).revalidate();
        ((EditorPanel)wandora.editorPanel).repaint();
    }
    
    
    

    
    
    
    /**
     * Returns a topic that is open in current topic panel. Notice, 
     * DockingFramePanel returns a topic for current dockable.
     */
    public Topic getOpenTopic() {
        if(currentTopicPanel!=null) {
            try {
                return currentTopicPanel.getTopic();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
    
    /**
     * Returns an icon for current topic panel. Notice, DockingFramePanel 
     * returns an icon for current dockable.
     */
    public Icon getIcon() {
        if(currentTopicPanel!=null) {
            try {
                return currentTopicPanel.getIcon();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
}
