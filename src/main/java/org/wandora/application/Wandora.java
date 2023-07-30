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
 * 
 * Wandora.java
 * 
 * 
 * 
 * Created on June 8, 2004, 11:22 AM
 */




package org.wandora.application;




import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.wandora.application.gui.LayerTree;
import org.wandora.application.gui.LogoAnimation;
import org.wandora.application.gui.TabbedTopicSelector;
import org.wandora.application.gui.TopicEditorPanel;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.SplashWindow;
import org.wandora.application.gui.ErrorDialog;
import org.wandora.application.gui.search.QueryPanel;
import org.wandora.application.gui.search.SearchPanel;
import org.wandora.application.gui.search.SimilarityPanel;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimplePanel;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.gui.topicpanels.TopicPanelManager;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.gui.tree.TopicTreePanel;
import org.wandora.application.gui.tree.TopicTreeTabManager;

import org.wandora.application.modulesserver.WandoraModulesServer;
import org.wandora.application.tools.importers.TopicMapImport;
import org.wandora.application.tools.importers.SimpleRDFImport;
import org.wandora.application.tools.importers.SimpleN3Import;
import org.wandora.application.tools.importers.SimpleRDFTurtleImport;
import org.wandora.application.tools.importers.SimpleRDFJsonLDImport;
import org.wandora.application.tools.navigate.Back;
import org.wandora.application.tools.navigate.Forward;
import org.wandora.application.tools.navigate.OpenTopic;
import org.wandora.application.tools.project.LoadWandoraProject;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.utils.logger.Logger;
import org.wandora.utils.logger.SimpleLogger;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.topicmap.layered.ContainerTopicMap;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.topicmap.layered.LayeredTopic;
import org.wandora.topicmap.memory.TopicMapImpl;
import org.wandora.topicmap.undowrapper.UndoException;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
import org.wandora.utils.CMDParamParser;
import org.wandora.utils.Options;
import org.wandora.utils.Textbox;
import org.wandora.utils.Delegate;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.swing.ImagePanel;




/**
 *
 * The main frame of Wandora application. Contains, among other things, initialization
 * of the application, top level handling tools, topic trees, browse history,
 * application options, topic map event propagation and user interaction.
 *
 * @author  olli, akivela
 */
public class Wandora extends javax.swing.JFrame implements ErrorHandler, ActionListener, MouseListener, TopicMapListener {
    
    private static final long serialVersionUID = 1L;

	/*
     * If the application makes URL requests and the URL stream is initialized
     * properly, USER_AGENT is a user agent string for Wandora application.
     */
    public static String USER_AGENT = "Wandora";

    /*
     * Static variable that is initialized during application startup. Contains
     * a reference to a running Wandora application. Variable is returned
     * with a static method called <code>getWandora</code>.
     */
    private static Wandora wandora = null;
    
    /*
     * Most menu structures of Wandora are stored and initialized in 
     * <code>menuManager</code>.
     */
    public WandoraMenuManager menuManager;
    
    
    /*
     * The main topic map in Wandora is a layer stack. Notice that layer stack
     * is not a real topic map implementation that can it self store topics and
     * associations. Layer stack needs a real topic map implementation inside
     * to be usable.
     */
    private LayerStack topicMap;
    
    /*
     * <code>layerTree</code> is a UI component that is used to view the
     * layer stack topic map. Notice that the <code>LayerTree</code> is 
     * actually a modified <code>JTree</code>.
     */
    public LayerTree layerTree;

    
    /*
     * <code>topicPanelManager</code> handles the topic editors and visualizations in
     * Wandora's main frame. It is one of the big UI elements of Wandora and
     * provides content for the editorPanel. Notice that the default topic panel 
     * is a DockingFramePanel at the moment.
     */
    public TopicPanelManager topicPanelManager;

    
    /*
     * <code>searchPanel</code> stores the UI component for the search
     * tab in the Wandora application. Search tab locates in left upper
     * corner, beside the Topics tab of Wandora application.
     */
    private SearchPanel searchPanel;
    

    /*
     * <code>toolmanager</code> is a special manager that is used handle
     * all available tool classes and class paths. It has a UI of it's own
     * that can be used to customize button lists, import and extract features
     * for example.
     */
    public WandoraToolManager2 toolManager;
    
    
    /*
     * <code>options</code> is a data structure that contains application
     * specific information required to initialize the application. <code>options</code>
     * is read from XML file <code>conf/options.xml</code> at startup and
     * stored to the same file at shutdown. Broken or incomplete options data structure
     * may prevent running the Wandora application.
     */
    public Options options;
    
    /*
     * If Wandora faces an HTTP authentication, during an extraction, for exmple,
     * a user name and a password are resolved using <code>wandoraHttpAuthorizer</code>.
     * <code>wandoraHttpAuthorizer</code> asks these from the Wandora user if
     * suitable information has not been provided earlier. Notice that 
     * <code>wandoraHttpAuthorizer</code> doesn't store given user names nor
     * passwords over use sessions.
     */
    public WandoraHttpAuthorizer wandoraHttpAuthorizer;
    
    
    /*
     * <code>topicHilights</code> implements a hidden feature in Wandora application.
     * It is poorly used at the moment. <code>topicHilights</code> provides
     * a way to color topics in Wandora's user interface.
     */
    public TopicHilights topicHilights;
    
    
    public TopicMap clipboardtm = new org.wandora.topicmap.memory.TopicMapImpl();
    
    public Shortcuts shortcuts;
    
    private String frameTitle="Wandora";
    
    private Set<TopicMapListener> topicMapListeners;
    private Set<RefreshListener> refreshListeners;
  
  
    private Component focusOwner;
    
    private JPopupMenu backPopup = new JPopupMenu();
    private JPopupMenu forwardPopup = new JPopupMenu();
    

    private Set animationCallers = new HashSet();
    


    private TopicTreeTabManager topicTreeManager = null;

            

    /*
     * If application user has explicitly opened a project file or has saved a
     * project file, the application knows the name of the project file. Known
     * project file name is stored in variable <code>currentProjectFileName</code>.
     * It is viewed in the application top bar.
     */
    private String currentProjectFileName = null;
    
    
    /*
     * Wandora contains an embedded HTTP server. Variable <code>httpServer</code>
     * contains a reference to the embedded HTTP server.
     */
//    public WandoraWebAppServer httpServer;
    public WandoraModulesServer httpServer;

    /*
     * <code>wandoraIcons</code> is a data structure for Wandora application icons.
     */
    public List<BufferedImage> wandoraIcons = new ArrayList<>();


    /*
     * Current implementation of back and forward butons uses a global
     * data structure implemented in <code>LocatorHistory</code> class. <code>history</code>
     * is a variable that contains the actual navigation history of Wandora
     * application.
     */
    private LocatorHistory history = new LocatorHistory();
    
    
    
    /*
     * Not in use at the moment.
     */
    private boolean skipTopicMapListenerEvents = false;


    
    
    private TabbedTopicSelector topicSelector = null;
    
    
    
    
    /** Creates new form Wandora */
    public Wandora() throws java.io.IOException {
        this(null);
    }
    
    
    public Wandora(CMDParamParser cmdparams) {
        this.wandora = this;
        if(cmdparams != null && cmdparams.isSet("options")) {
            this.options = new Options(cmdparams.get("options"));
        }
        else {
            this.options = new Options("conf/options.xml");
        }
        
        try {
            wandoraIcons.add( UIBox.getImage("gui/appicon/64x64_24bit.png") );
            wandoraIcons.add( UIBox.getImage("gui/appicon/48x48_24bit.png") );
            wandoraIcons.add( UIBox.getImage("gui/appicon/32x32_24bit.png") );
            wandoraIcons.add( UIBox.getImage("gui/appicon/24x24_24bit.png") );
            wandoraIcons.add( UIBox.getImage("gui/appicon/16x16.png") );
            wandoraIcons.add( UIBox.getImage("gui/appicon/icon.gif") );
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        try {
            topicMapListeners=new HashSet<TopicMapListener>();
            refreshListeners=new HashSet<RefreshListener>();
	    
	    //JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            initComponents();
            initializeWandora();
        }
        catch(Exception e) {
            displayException("Exception occurred during startup!", e);
        }
    }
    
    
    public WandoraModulesServer getHTTPServer(){
        if(httpServer==null) {
            try {
                // httpServer=new WandoraHttpServer(this);
                // httpServer=new WandoraWebAppServer(this);
                httpServer=new WandoraModulesServer(this);
                httpServer.setStatusComponent(serverButton,"gui/icons/server_start.png","gui/icons/server_stop.png","gui/icons/server_hit.png");
            }
            catch(Exception e) {
                handleError(e);
            }
        }
        return httpServer;
    }
    
    
    public void startHTTPServer() {
        WandoraModulesServer server = getHTTPServer();
        if(server != null) {
            server.start();
            menuManager.refreshServerMenu();
        }
    }
    
    
    public void stopHTTPServer() {
        WandoraModulesServer server = getHTTPServer();
        if(server != null) {
            server.stopServer();
        }
    }
    
    public JPanel getStartupPanel(){
        return startupPanel;
    }
    
    public void addRefreshListener(RefreshListener l){
        refreshListeners.add(l);
    }
    public void removeRefreshListener(RefreshListener l){
        refreshListeners.remove(l);
    }
    public void addTopicMapListener(TopicMapListener l){
        topicMapListeners.add(l);
    }
    public void removeTopicMapListener(TopicMapListener l){
        topicMapListeners.remove(l);
    }
    /**
     * <p>
     * Informs refresh listeners that now is an appropriate time to refresh any
     * components that contain information about the topic map that may have changed.
     * This method should be called after a tool has finished modifying topic map
     * or any other operation that modifies topic map is finished. It shouldn't
     * be called after every single minor change to topic map while the complete operation
     * is still unfinished.</p>
     * <p>
     * Effectively informs the listeners that a complex logical
     * operation which consists of several small changes to topic map has finished.</p>
     * <p>
     * Listeners will include topic trees, topic panels and any such components
     * that contain information about topic map that needs to be kept up to date.
     * </p>
     */
    private boolean alreadyRefreshing = false;
    public void doRefresh() {
        if(!alreadyRefreshing) {
            alreadyRefreshing = true;
            try {
                topicMap.clearTopicMapIndexes();
            }
            catch(Exception e) {
                // IGNORE
            }
            Collection<RefreshListener> duplicate=new ArrayList<RefreshListener>();
            duplicate.addAll(refreshListeners); // refresh may result in closeTopic which modifies refreshListeners
            for(RefreshListener l : duplicate){
                try {
                    l.doRefresh();
                }
                catch(Exception tme){
                    handleError(tme);
                }
            }
            refreshInfoFields();
            menuManager.refreshViewMenu();
            alreadyRefreshing = false;
            addUndoMarker();
        }
    }
    
    public WandoraToolManager2 getToolManager(){
        return toolManager;
    }
    
    public JViewport getViewPort() {
        return contentScrollPane.getViewport();
    }
    
    /**
     * Creates a new layered topic map and initializes it with the base topic map.
     */
    public void initializeTopicMap() throws TopicMapException {
        topicMap=new LayerStack();
        try{
            topicMap.setUseUndo(true);
        }
        catch(UndoException ue){handleError(ue);}
        TopicMap initialTopicMap = new org.wandora.topicmap.memory.TopicMapImpl();
        try {
        	String basemapFile = options.get("basemap");
        	if(!new File(basemapFile).exists()) {
        		basemapFile = "resources/main/"+basemapFile;
        		if(!new File(basemapFile).exists()) {
        			basemapFile = "build/"+basemapFile;
        		}
        	}
            initialTopicMap.importTopicMap(basemapFile);
        }
        catch(Exception e) {
            handleError(e);
        }
        topicMap.addLayer(new Layer(initialTopicMap, "Base", topicMap));
        topicMap.resetTopicMapChanged();
        this.setCurrentProjectFileName(null);
    }
    
    /**
     * Called when the topic map object is changed to another topic map object.
     */
    private void topicMapObjectChanged(){
        layerTree=new LayerTree(topicMap,this);
        layerTree.setChangingListener(new Delegate<Boolean,Object>() {
            @Override
            public Boolean invoke(Object o) {
                TopicPanel topicPanel = topicPanelManager.getCurrentTopicPanel();
                boolean reponse = true;
                if(topicPanel!=null) {
                    try {
                        topicPanel.applyChanges();
                    }
                    catch(CancelledException ce) {
                        reponse = false;
                    }
                    catch(TopicMapException tme) {
                        handleError(tme);
                        reponse = false;
                    }
                }
                return reponse;
            }
        });
        layerTree.setChangedListener(new Delegate<Object,Object>(){
            @Override
            public Object invoke(Object o){
                try {
                    refreshTopicTrees();
                    doRefresh();
                }
                catch(Exception e) {
                    handleError(e);
                }
                return null;
            }
        });
        layersPanel.removeAll();
        layersPanel.add(layerTree,BorderLayout.CENTER);
        topicMap.addTopicMapListener(this);

        topicTreeManager = new TopicTreeTabManager(this, tabbedPane);
        topicTreeManager.initializeTopicTrees();
        tabbedPane.addTab("Finder", finderPanel);

        searchPanel = new SearchPanel();
        finderPanel.removeAll();
        finderPanel.add(searchPanel, BorderLayout.CENTER);
    }
    
    private void removeButtonActionListeners(JButton b){
        ActionListener[] listeners=b.getActionListeners();
        for (ActionListener listener : listeners) {
            b.removeActionListener(listener);
        }
    }




    /**
     * Performs most of Wandora initialization.
     */
    public void initializeWandora() {
        try {
            USER_AGENT = options.get("httpuseragent");
            currentProjectFileName = null;
            
            this.focusOwner = null;
            this.topicHilights = new TopicHilights(this);
            this.topicPanelManager = new TopicPanelManager(this);
            this.toolManager = new WandoraToolManager2(this);
            
            initializeTopicMap();
            topicMapObjectChanged(); // this also initializes topic trees
            placeWindow();

            history = new LocatorHistory();
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);

            removeButtonActionListeners(openButton);
            removeButtonActionListeners(backButton);
            removeButtonActionListeners(forwardButton);
            openButton.addActionListener(new WandoraToolActionListener(this, new OpenTopic(OpenTopic.ASK_USER)));
            backButton.addActionListener(new WandoraToolActionListener(this, new Back()));
            forwardButton.addActionListener(new WandoraToolActionListener(this, new Forward()));
                        
            backButton.setComponentPopupMenu(backPopup);
            forwardButton.setComponentPopupMenu(forwardPopup);
            
            openButton.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        evt.getComponent().setBackground(UIConstants.defaultActiveBackground);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                    }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                    }
                }
            );
            backButton.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        if(evt.getComponent().isEnabled())
                            evt.getComponent().setBackground(UIConstants.defaultActiveBackground);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        if(evt.getComponent().isEnabled())
                            evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                    }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                    }
                }
            );
            forwardButton.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        if(evt.getComponent().isEnabled())
                            evt.getComponent().setBackground(UIConstants.defaultActiveBackground);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        if(evt.getComponent().isEnabled())
                            evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                    }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                    }
                }
            );

            refreshToolPanel();

            this.wandoraHttpAuthorizer = new WandoraHttpAuthorizer(this);
            this.shortcuts = new Shortcuts(this);
            this.menuManager = new WandoraMenuManager(this);
            this.setJMenuBar(menuManager.getWandoraMenuBar());

            getHTTPServer(); // this will also create the server and initialize it if it doesn't exist yet
            if(httpServer.isAutoStart()) {
                startHTTPServer();
            }

            refresh();
            
            topicPanelManager.reset();
        }
        catch(Exception e) {
            handleError(e);
        }
    }




    public void refreshToolPanel() {
        buttonToolPanel.removeAll();
        String currentToolSetName = options.get("gui.toolPanel.currentToolSet");
        JComponent buttonToolBar = toolManager.getToolButtonBar(currentToolSetName);
        if(buttonToolBar != null) {
            Dimension d = buttonToolBar.getPreferredSize();
            d = new Dimension(d.width+5, d.height);
            buttonToolPanel.add(buttonToolBar);
            buttonToolPanel.setPreferredSize(d);
            buttonToolPanel.setMinimumSize(d);
        }

        logoContainer.setComponentPopupMenu(toolManager.getToolButtonSelectPopupMenu());

        buttonToolPanel.invalidate();
        buttonToolPanel.repaint();
    }

    
    
    public String getTopicGUIName(Topic t){
        return TopicToString.toString(t); 
    }
    
    
    
    
    /**
     * Refreshes all topic trees. This method doesn't refresh TreeTopicPanels.
     */
    public void refreshTopicTrees() {
        try {
            Map<String,TopicTreePanel> trees = topicTreeManager.getTrees();
            if(trees != null) {
                for(TopicTreePanel tree : trees.values()){
                    tree.refresh();
                    tree.repaint();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens a dialog and shows information about an exception and a String message.
     */
    public void displayException(String message,Throwable e){
        ErrorDialog ed = new ErrorDialog(this,true,e,message);
        ed.setVisible(true);
    }
    
    /**
     * Displays an exception dialog with the given message and throwable object and
     * buttons labeled with parameters yes and no. If yes button is pressed returns 1
     * otherwise returns 0.
     */
    public int displayExceptionYesNo(String message,Throwable e,String no,String yes){
        ErrorDialog ed=new ErrorDialog(this,true,e,message,no,yes);
        ed.setVisible(true); 
        return ed.getButton();
    }
    public int displayExceptionYesNo(String message,Throwable e){
        return displayExceptionYesNo(message,e,"No","Yes");
    }

    
    
    
    
    
   private SearchPanel searchTopicSelector = null;
   private SimilarityPanel similarityTopicSelector = null;
   private QueryPanel queryTopicSelector = null;
   private Collection<TopicTreePanel> topicTreeSelectors = null;
           
   /**
    * Creates a topic selector with all configured tree choosers and a SelectTopicPanel. The returned
    * selector will be a tabbed selector with one tab for each tree and the SelectTopicPanel.
    */
    public TabbedTopicSelector getTopicFinder() throws TopicMapException {
        boolean refreshSelectors = true;
        Component currentSelector = null;
        
        if(topicSelector != null) {
            refreshSelectors = !topicSelector.remember();
        }
        if(!refreshSelectors) {
            currentSelector = topicSelector.getSelectedSelector();
        }
        topicSelector = new TabbedTopicSelector();
        topicSelector.setRemember(!refreshSelectors);
        
        if(topicTreeSelectors == null || refreshSelectors) {
            topicTreeSelectors = topicTreeManager.getTreeChoosers();
        }
        for(TopicTreePanel c : topicTreeSelectors){
            topicSelector.addTab(c);
        }
            
        if(searchTopicSelector == null || refreshSelectors) {
            searchTopicSelector = new SearchPanel(false);
        }
        topicSelector.addTab(searchTopicSelector);
        
        if(similarityTopicSelector == null || refreshSelectors) {
            similarityTopicSelector = new SimilarityPanel();
        }
        topicSelector.addTab(similarityTopicSelector);
                
        if(queryTopicSelector == null || refreshSelectors) {
            queryTopicSelector = new QueryPanel();
        }
        topicSelector.addTab(queryTopicSelector);
        
        if(currentSelector != null && !refreshSelectors) {
            topicSelector.setSelectedSelector(currentSelector);
        } 

        return topicSelector;
    }




    
    
    /**
     * Informs that tools have changed. Will cause all menus containing configurable
     * tools to be refreshed.
     */
    public void toolsChanged() {
        refreshToolPanel();
        menuManager.refreshToolMenu();
        menuManager.refreshImportMenu();
        menuManager.refreshGeneratorMenu();
        menuManager.refreshExportMenu();
        menuManager.refreshExtractMenu();
        // this.validateTree(); // TRIGGERS EXCEPTION IN JAVA 1.7
        //repaint();
        refresh();
    }
    
    public void shortcutsChanged()  throws TopicMapException {
        menuManager.refreshShortcutsMenu();
    }
    
    public void topicPanelsChanged() {
        menuManager.refreshViewMenu();
    }
    
    
    /**
     * Places the window according to options in the <code>options</code> object.
     * Sets window placement, width, height and horizontal and vertical splitter
     * locations.
     */
    public void placeWindow() {
        boolean placedSuccessfully = false;
        if(options != null) {
            placedSuccessfully = true;
            try {
                int x = options.getInt("options.window.x");
                int y = options.getInt("options.window.y");
                int width = options.getInt("options.window.width");
                int height = options.getInt("options.window.height");
                
                if(x <= 0) x = 10;
                if(y <= 0) y = 10;
                if(width <= 0) width = 300;
                if(height <= 0) height = 300;
                
                try {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    if(x > screenSize.width) {
                        x = screenSize.width - 300;
                        width = 300;
                    }
                    if(y > screenSize.height) {
                        y = screenSize.height - 300;
                        height = 300;
                    }
                }
                catch(Exception e) {}

                if(x > 0 && y > 0 && width > 0 && height > 0) {
                    super.setSize(width, height);
                    super.setLocation(x, y);
                }
                int split1 = options.getInt("options.window.horizontalsplitter");
                int split2 = options.getInt("options.window.verticalsplitter");
                if(split1 > 0 && split2 > 0) {
                    paragraphSplitPane.setDividerLocation(split1);
                    toolSplitPane.setDividerLocation(split2);
                }
            }
            catch (Exception e) {
                handleError(e);
            }
        }
        if(!placedSuccessfully) {
            super.setSize(800, 600);
            UIBox.centerScreen(this);
        }
    }
    
    
    
    public void setTitleMessage(String message) {
        if(message == null || message.length() == 0) {
            this.setTitle(frameTitle);
        }
        else {
            this.setTitle(frameTitle+" - "+message);
        }
    }
    

    

    /**
     * Gets a property from application options.
     */
    public String getProperty(String key){
        return options.get(key);
    }
    
    
    public String getLang(){
        return "en";
    }

    
    
    
    public TopicPanel getTopicPanel() {
        return topicPanelManager.getCurrentTopicPanel();
    }
    
    
    public Shortcuts getShortcuts() {
        return this.shortcuts;
    }


    public TopicTreeTabManager getTopicTreeManager() {
        return topicTreeManager;
    }
    
    public TopicTree getCurrentTopicTree() {
        if(tabbedPane != null) {
            Component selectedTab = tabbedPane.getSelectedComponent();
            if(selectedTab != null && !selectedTab.equals(finderPanel)) {
                TopicTreePanel treeTopicChooser = (TopicTreePanel) selectedTab;
                return treeTopicChooser.getTopicTree();
            }
        }
        return null;
    }
    
    
    

    public String getCurrentProjectFileName() {
        return currentProjectFileName;
    }
    public void setCurrentProjectFileName(String f) {
        currentProjectFileName = f;
        if(f != null && f.length() > 0) {
            int i = f.lastIndexOf(File.separator);
            if(i>-1) {
                f = f.substring(i+1);
            }
        }
        this.setTitleMessage(f);
    }


    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topicChooserPanel = new javax.swing.JPanel();
        selectPanel = new javax.swing.JPanel();
        statLabel = new org.wandora.application.gui.simple.SimpleLabel();
        toolBar = new javax.swing.JToolBar();
        openButton = new SimpleButton();
        backButton = new org.wandora.application.gui.simple.SimpleButton();
        forwardButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonToolPanel = new javax.swing.JPanel();
        logoContainer = new javax.swing.JPanel();
        toolbarFillerPanel = new javax.swing.JPanel();
        logoPanel = new ImagePanel("gui/main_logo.gif");
        logoAnimPanel = new LogoAnimation(this);
        paragraphSplitPane = new javax.swing.JSplitPane();
        contentContainerPanel = new javax.swing.JPanel();
        contentScrollPane = new org.wandora.application.gui.simple.SimpleScrollPane();
        editorPanel = new TopicEditorPanel(this, this);
        startupPanel = new javax.swing.JPanel();
        titlePanel = new ImagePanel("gui/startup_image.gif");
        infoBar = new javax.swing.JToolBar();
        infobarPanel = new javax.swing.JPanel();
        numberOfTopicAssociationsLabel = new SimpleLabel();
        fillerPanel = new javax.swing.JPanel();
        topicLabel = new org.wandora.application.gui.simple.SimpleLabel();
        jSeparator1 = new javax.swing.JSeparator();
        topicDistributionLabel = new org.wandora.application.gui.simple.SimpleLabel();
        jSeparator4 = new javax.swing.JSeparator();
        layerLabel = new org.wandora.application.gui.simple.SimpleLabel();
        jSeparator3 = new javax.swing.JSeparator();
        stringifierButton = new javax.swing.JButton();
        panelButton = new javax.swing.JButton();
        serverButton = new javax.swing.JButton();
        toolSplitPane = new javax.swing.JSplitPane();
        layersPanel = new javax.swing.JPanel();
        tabbedPane = new SimpleTabbedPane();
        finderPanel = new SimplePanel();

        topicChooserPanel.setLayout(new java.awt.BorderLayout());

        selectPanel.setLayout(new java.awt.BorderLayout());

        statLabel.setFont(org.wandora.application.gui.UIConstants.plainFont);
        statLabel.setText("OK");
        statLabel.setToolTipText("");
        statLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Wandora");
        setIconImage(org.wandora.application.gui.UIBox.getImage("gui/appicon/48x48_24bit.png"));
        setIconImages(wandoraIcons);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolBar.setFloatable(false);
        toolBar.setOrientation(JToolBar.HORIZONTAL);
        toolBar.setAlignmentX(0.0F);
        toolBar.setMinimumSize(new java.awt.Dimension(900, 46));
        toolBar.setPreferredSize(new java.awt.Dimension(900, 46));

        openButton.setBackground(new Color(238,238,238));
        openButton.setIcon(org.wandora.application.gui.UIBox.getIcon("gui/button_open.png"));
        openButton.setToolTipText("Open topic...");
        openButton.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.gray));
        openButton.setFocusPainted(false);
        openButton.setMaximumSize(new java.awt.Dimension(60, 46));
        openButton.setMinimumSize(new java.awt.Dimension(60, 46));
        openButton.setPreferredSize(new java.awt.Dimension(60, 46));
        toolBar.add(openButton);

        backButton.setBackground(new Color(238,238,238));
        backButton.setIcon(org.wandora.application.gui.UIBox.getIcon("gui/button_back.png"));
        backButton.setToolTipText("Backward");
        backButton.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.gray));
        backButton.setFocusPainted(false);
        backButton.setMaximumSize(new java.awt.Dimension(60, 46));
        backButton.setMinimumSize(new java.awt.Dimension(60, 46));
        backButton.setPreferredSize(new java.awt.Dimension(60, 46));
        toolBar.add(backButton);

        forwardButton.setBackground(new Color(238,238,238));
        forwardButton.setIcon(org.wandora.application.gui.UIBox.getIcon("gui/button_forward.png"));
        forwardButton.setToolTipText("Forward");
        forwardButton.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.gray));
        forwardButton.setFocusPainted(false);
        forwardButton.setMaximumSize(new java.awt.Dimension(60, 46));
        forwardButton.setMinimumSize(new java.awt.Dimension(60, 46));
        forwardButton.setPreferredSize(new java.awt.Dimension(60, 46));
        toolBar.add(forwardButton);

        buttonToolPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        toolBar.add(buttonToolPanel);

        logoContainer.setAlignmentX(0.0F);
        logoContainer.setPreferredSize(new java.awt.Dimension(300, 48));
        logoContainer.setLayout(new java.awt.GridBagLayout());

        toolbarFillerPanel.setMinimumSize(new java.awt.Dimension(10, 46));
        toolbarFillerPanel.setPreferredSize(new java.awt.Dimension(10, 46));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        logoContainer.add(toolbarFillerPanel, gridBagConstraints);

        logoPanel.setAlignmentX(0.0F);
        logoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        logoContainer.add(logoPanel, gridBagConstraints);

        logoAnimPanel.setMinimumSize(new java.awt.Dimension(64, 46));
        logoAnimPanel.setPreferredSize(new java.awt.Dimension(64, 46));
        logoContainer.add(logoAnimPanel, new java.awt.GridBagConstraints());

        toolBar.add(logoContainer);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

        paragraphSplitPane.setDividerLocation(220);
        paragraphSplitPane.setDividerSize(2);
        paragraphSplitPane.setOneTouchExpandable(true);
        paragraphSplitPane.setPreferredSize(new java.awt.Dimension(10, 6));

        contentContainerPanel.setMinimumSize(new java.awt.Dimension(44, 44));
        contentContainerPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        contentContainerPanel.setLayout(new java.awt.BorderLayout());

        contentScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        contentScrollPane.setPreferredSize(new java.awt.Dimension(300, 300));

        editorPanel.setBackground(new java.awt.Color(255, 255, 255));
        editorPanel.setLayout(new java.awt.BorderLayout());

        startupPanel.setBackground(new java.awt.Color(255, 255, 255));
        startupPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        startupPanel.setLayout(new java.awt.GridBagLayout());

        titlePanel.setBackground(new java.awt.Color(255, 255, 255));
        titlePanel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        startupPanel.add(titlePanel, gridBagConstraints);

        editorPanel.add(startupPanel, java.awt.BorderLayout.CENTER);

        contentScrollPane.setViewportView(editorPanel);

        contentContainerPanel.add(contentScrollPane, java.awt.BorderLayout.CENTER);

        infoBar.setMinimumSize(new java.awt.Dimension(350, 20));
        infoBar.setPreferredSize(new java.awt.Dimension(350, 20));

        infobarPanel.setMinimumSize(new java.awt.Dimension(350, 18));
        infobarPanel.setPreferredSize(new java.awt.Dimension(350, 18));
        infobarPanel.setLayout(new java.awt.GridBagLayout());

        numberOfTopicAssociationsLabel.setFont(org.wandora.application.gui.UIConstants.plainFont);
        numberOfTopicAssociationsLabel.setForeground(new java.awt.Color(51, 51, 51));
        numberOfTopicAssociationsLabel.setToolTipText("Number of topics and associations in layer stack.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        infobarPanel.add(numberOfTopicAssociationsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        infobarPanel.add(fillerPanel, gridBagConstraints);

        topicLabel.setFont(org.wandora.application.gui.UIConstants.plainFont
        );
        topicLabel.setForeground(new java.awt.Color(51, 51, 51));
        topicLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        topicLabel.setText("topic name");
        topicLabel.setToolTipText("Name or subject identifier of current topic in panel.");
        topicLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 1, 0, 1));
        topicLabel.setMaximumSize(new java.awt.Dimension(300, 14));
        topicLabel.setMinimumSize(new java.awt.Dimension(53, 14));
        topicLabel.setPreferredSize(new java.awt.Dimension(300, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        infobarPanel.add(topicLabel, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        infobarPanel.add(jSeparator1, gridBagConstraints);

        topicDistributionLabel.setFont(org.wandora.application.gui.UIConstants.plainFont);
        topicDistributionLabel.setForeground(new java.awt.Color(51, 51, 51));
        topicDistributionLabel.setText("0");
        topicDistributionLabel.setToolTipText("Layer distribution of current topic.");
        topicDistributionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 1, 0, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        infobarPanel.add(topicDistributionLabel, gridBagConstraints);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        infobarPanel.add(jSeparator4, gridBagConstraints);

        layerLabel.setFont(org.wandora.application.gui.UIConstants.plainFont);
        layerLabel.setForeground(new java.awt.Color(51, 51, 51));
        layerLabel.setText("layer name");
        layerLabel.setToolTipText("Name of selected topic map layer.");
        layerLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 1, 0, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        infobarPanel.add(layerLabel, gridBagConstraints);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        infobarPanel.add(jSeparator3, gridBagConstraints);

        stringifierButton.setToolTipText("View topics as...");
        stringifierButton.setBorder(null);
        stringifierButton.setBorderPainted(false);
        stringifierButton.setContentAreaFilled(false);
        stringifierButton.setMaximumSize(new java.awt.Dimension(16, 16));
        stringifierButton.setMinimumSize(new java.awt.Dimension(16, 16));
        stringifierButton.setPreferredSize(new java.awt.Dimension(16, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        infobarPanel.add(stringifierButton, gridBagConstraints);

        panelButton.setToolTipText("Type of current topic panel.");
        panelButton.setBorder(null);
        panelButton.setBorderPainted(false);
        panelButton.setContentAreaFilled(false);
        panelButton.setMaximumSize(new java.awt.Dimension(16, 16));
        panelButton.setMinimumSize(new java.awt.Dimension(16, 16));
        panelButton.setPreferredSize(new java.awt.Dimension(16, 16));
        panelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                panelButtonMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        infobarPanel.add(panelButton, gridBagConstraints);

        serverButton.setToolTipText("Status of Wandora's internal web server.");
        serverButton.setBorder(null);
        serverButton.setBorderPainted(false);
        serverButton.setContentAreaFilled(false);
        serverButton.setMaximumSize(new java.awt.Dimension(16, 16));
        serverButton.setMinimumSize(new java.awt.Dimension(16, 16));
        serverButton.setPreferredSize(new java.awt.Dimension(16, 16));
        serverButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serverButtonMouseClicked(evt);
            }
        });
        infobarPanel.add(serverButton, new java.awt.GridBagConstraints());

        infoBar.add(infobarPanel);

        contentContainerPanel.add(infoBar, java.awt.BorderLayout.SOUTH);

        paragraphSplitPane.setRightComponent(contentContainerPanel);

        toolSplitPane.setDividerLocation(350);
        toolSplitPane.setDividerSize(3);
        toolSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        toolSplitPane.setResizeWeight(1.0);
        toolSplitPane.setOneTouchExpandable(true);

        layersPanel.setBackground(new java.awt.Color(255, 255, 255));
        layersPanel.setLayout(new java.awt.BorderLayout());
        toolSplitPane.setBottomComponent(layersPanel);

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabbedPaneMouseClicked(evt);
            }
        });

        finderPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Finder", finderPanel);

        toolSplitPane.setTopComponent(tabbedPane);

        paragraphSplitPane.setLeftComponent(toolSplitPane);

        getContentPane().add(paragraphSplitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    
    
        
    private void tabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabbedPaneMouseClicked
        topicTreeManager.tabbedPaneMouseClicked(evt);
        if(tabbedPane != null) {
            Component selectedTab = tabbedPane.getSelectedComponent();
            if(selectedTab != null && selectedTab.equals(finderPanel)) {
                if(searchPanel != null) {
                    searchPanel.requestSearchFieldFocus();
                }
            }
        }
    }//GEN-LAST:event_tabbedPaneMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        tryExit();
    }//GEN-LAST:event_formWindowClosing

private void serverButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serverButtonMouseClicked
        WandoraModulesServer server = getHTTPServer();
        if(server != null) {
            if(server.isRunning()) {
                stopHTTPServer();
            }
            else {
                startHTTPServer();
            }
            menuManager.refreshServerMenu();
        }
}//GEN-LAST:event_serverButtonMouseClicked

    private void panelButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelButtonMouseClicked

        JPopupMenu popup = new JPopupMenu();         topicPanelManager.getTopicPanelMenu(popup);         popup.show(evt.getComponent(), evt.getX(), evt.getY());     }//GEN-LAST:event_panelButtonMouseClicked


    

    /**
     * Starts and stops logo animation at the right upper corner of the window.
     */
    public void setAnimated(boolean shouldAnimate, Object caller) {
        if(shouldAnimate) {
            if(caller != null) animationCallers.add(caller);
            ((LogoAnimation) logoAnimPanel).animate(shouldAnimate);
        }
        else {
            if(caller != null) animationCallers.remove(caller);
            if(animationCallers.isEmpty()) {
                ((LogoAnimation) logoAnimPanel).animate(shouldAnimate);
            }
        }
    }
    
    
    public void forceStopAnimation() {
        animationCallers.clear();
        ((LogoAnimation) logoAnimPanel).animate(false);
    }
    
    
    
    /**
     * Resets Wandora application. Will close topic and do a complete reinitialization
     * of the application.
     */
    public void resetWandora() {
        // DO NOT ADD A USER CONFIRMATION MECHANISM HERE. 
        // CALLER HANDLES USER CONFIRMATION!
        stopHTTPServer();
        resetTopicPanels();
        clearHistory();
        saveOptions();
        topicMap.close();
        exitCode = RESTART_APPLICATION;
    }
    
    
     
    /**
     * Exits the application, checking possible changes first. The current topic panel
     * may contain changes that haven't been committed in topic map yet. These may
     * cause merges which require user confirmation. The user may cancel the operation at
     * this point.
     * 
     * Also the topic map itself may be unsaved in which case a warning dialog is shown.
     * User may either cancel the operation or exit the application losing all changes.
     */
    public void tryExit(){
        try {
            applyChanges();
        }
        catch(Exception ce){
            return;
        }
        /* // note that manager.isUnsaved always returned false, uncomment this when unsaved tracking works
        if(manager.isUnsaved()){
            if(WandoraOptionPane.showConfirmDialog(this,"Unsaved changes exist. Are you sure you want to exit?","Unsaved changes",WandoraOptionPane.YES_NO_OPTION)
                ==WandoraOptionPane.YES_OPTION){
                doExit();
            }
        }
        else*/
        doExit();
    }



    /**
     * Closes the application without any user interaction and without saving any
     * unsaved data.
     */
    public void doExit() {
        try {
            stopHTTPServer();
            saveOptions();
            topicMap.close();
        }
        catch(Exception e) {
        }
        exitCode = EXIT_APPLICATION;
    }
    

    
    /**
     * Stores application window placement, size and splitter locations to the
     * <code>options</code> object and commands the <code>options</code> to
     * save itself. Saving the <code>options</code> writes it into an XML
     * file. By default this file is <code>conf/options.xml</code>.
     */
    public void saveOptions() {
        try {
            if(options != null) {
                Dimension d = super.getSize();
                options.put("options.window.width", d.width);
                options.put("options.window.height", d.height);
                options.put("options.window.x", this.getX());
                options.put("options.window.y", this.getY());
                options.put("options.window.horizontalsplitter",paragraphSplitPane.getDividerLocation());
                options.put("options.window.verticalsplitter",toolSplitPane.getDividerLocation());
            
                options.save();
            }
        }
        catch (Exception e) {
            handleError(e); 
        }
    }
    


 
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonToolPanel;
    private javax.swing.JPanel contentContainerPanel;
    public javax.swing.JScrollPane contentScrollPane;
    public javax.swing.JPanel editorPanel;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel finderPanel;
    private javax.swing.JButton forwardButton;
    public javax.swing.JToolBar infoBar;
    private javax.swing.JPanel infobarPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel layerLabel;
    private javax.swing.JPanel layersPanel;
    private javax.swing.JPanel logoAnimPanel;
    private javax.swing.JPanel logoContainer;
    private javax.swing.JPanel logoPanel;
    private javax.swing.JLabel numberOfTopicAssociationsLabel;
    private javax.swing.JButton openButton;
    private javax.swing.JButton panelButton;
    private javax.swing.JSplitPane paragraphSplitPane;
    private javax.swing.JPanel selectPanel;
    private javax.swing.JButton serverButton;
    private javax.swing.JPanel startupPanel;
    private javax.swing.JLabel statLabel;
    private javax.swing.JButton stringifierButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JSplitPane toolSplitPane;
    private javax.swing.JPanel toolbarFillerPanel;
    private javax.swing.JPanel topicChooserPanel;
    private javax.swing.JLabel topicDistributionLabel;
    public javax.swing.JLabel topicLabel;
    // End of variables declaration//GEN-END:variables
    
    
    /**
     * Returns the <code>options</code> object containing all application options.
     * Options are persistent, they are saved when application exits and loaded
     * when it starts.
     */
    public Options getOptions(){
        return options;
    }
    
    /**
     * Returns the currently open layered topic map that.
     */
    public LayerStack getTopicMap() {
        return topicMap;
    }
    
    public void setTopicMap(LayerStack topicMap) {
        if(this.topicMap!=null) {
            this.topicMap.removeTopicMapListener(this);
        }
        this.topicMap=topicMap;
        topicMapObjectChanged();
    }
    
    /**
     * Returns the currently open topic or <code>null</code> if no topic is open.
     */
    public Topic getOpenTopic(){
        return topicPanelManager.getOpenTopic();
    }
    
    


    // -------------------------------------------------------------------------
    // ------------------------------------------------------------- HISTORY ---
    // -------------------------------------------------------------------------
    
    
    public void addToHistory(Topic topic) {
        try {
            if(topic != null && !topic.isRemoved()) {
                Point viewPos = contentScrollPane.getViewport().getViewPosition();
                history.setCurrentViewPosition(viewPos.y);
                history.add(topic.getOneSubjectIdentifier(),0);
                forwardButton.setEnabled(history.peekNext() != null);
                backButton.setEnabled(history.peekPrevious() != null);
                updateHistoryPopups();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * Performs a back history operation moving to the topic that was previously
     * open.
     * @see #forward()
     * @see #clearHistory()
     */
    public void back() {
        try {
            Point viewPos = contentScrollPane.getViewport().getViewPosition();
            history.setCurrentViewPosition(viewPos.y);
            T2<Locator,Integer> page = history.getPrevious();
            topicPanelManager.openTopic(topicMap.getTopic(page.e1));
            contentScrollPane.getViewport().setViewPosition(new Point(0,page.e2));
            forwardButton.setEnabled(history.peekNext() != null);
            backButton.setEnabled(history.peekPrevious() != null);
            updateHistoryPopups();
        }
        catch(Exception e) {
            handleError(e);
        }
    }
    
    /**
     * Performs a forward history operation moving to the topic that was open
     * before last back operation.
     * @see #back()
     * @see #clearHistory()
     */
    public void forward() {
        try {
            Point viewPos = contentScrollPane.getViewport().getViewPosition();
            history.setCurrentViewPosition(viewPos.y);
            T2<Locator,Integer> page = history.getNext();
            topicPanelManager.openTopic(topicMap.getTopic(page.e1));
            contentScrollPane.getViewport().setViewPosition(new Point(0,page.e2));
            forwardButton.setEnabled(history.peekNext() != null);
            backButton.setEnabled(history.peekPrevious() != null);
            updateHistoryPopups();
        }
        catch(Exception e) {
            handleError(e);
        }
    }
    
    
    
    /**
     * Clears application history. 
     * @see #back()
     * @see #forward()
     */
    public void clearHistory() {
        history.clear();
        backButton.setEnabled(false);
        forwardButton.setEnabled(false);
        updateHistoryPopups();
        refresh();
    }
    
    
    
    
    /**
     * Pop-up menus activated on back and forward button.
     * This should be called when history or current location in history has been
     * changed.
     */
    public void updateHistoryPopups() {
        Object[] popupStruct = history.getBackPopupStruct(this);
        backPopup.removeAll();
        UIBox.attachPopup(backPopup, popupStruct, this);
        popupStruct = history.getForwardPopupStruct(this);
        forwardPopup.removeAll();
        UIBox.attachPopup(forwardPopup, popupStruct, this);
    }
        
    
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- OPEN TOPIC ---
    // -------------------------------------------------------------------------
    

 
    
    /**
     * Opens the given topic without applying changes made to the currently open topic.
     *
     * @return <code>true</code> if the topic was opened, <code>false</code> if there
     *         was a topic map error.
     */
    public boolean openTopic(Topic topic) {
        try {
            addToHistory(topic);
            topicPanelManager.openTopic(topic);
            return true;
        }
        catch(TopicMapException tme) { 
            handleError(tme); 
            return false;
        } 
        catch (OpenTopicNotSupportedException otnse) {
           handleError(otnse); 
           return false;
        }
    }
    
    
    public boolean openTopic(Locator l) {
        try {
            return openTopic(topicMap.getTopic(l));
        }
        catch(Exception e) { 
            handleError(e); 
            return false;
        }
    }
    
    
    public void reopenTopic() {
        try {
            topicPanelManager.reopenTopic();
            refresh();
        }
        catch(Exception e) { 
            handleError(e);
        }
    }
    
    
    
    public void applyChangesAndOpen(Topic topic) {
        boolean shouldOpen = topicPanelManager.applyChanges();
        if(shouldOpen) {
            openTopic(topic);
        }
        // SHOULD NEXT REALLY BE HERE OR NOT?
        refreshInfoFields();
    }
    
    
    public void applyChangesAndOpen(Locator l) {
        try {
            applyChangesAndOpen(topicMap.getTopic(l));
        }
        catch(Exception e) {
            handleError(e);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    /**
     * Applies changes made to the currently open topic. This may cause merges
     * in the topic map which will open a warning dialog. User may use the dialog
     * to cancel the operation which will cause a <code>CancelledException</code>
     * to be thrown.
     */
    public void applyChanges() throws CancelledException {
        topicPanelManager.applyChanges();
    }
    
    
    
    
    /**
     * Resets topic panel manager.
     */
    public void resetTopicPanels(){
        topicPanelManager.reset();
    }
 
    
    /**
     * Refreshes currently open topic panel. Applies changes first which may cause
     * topics to be merged and a warning dialog to be shown to the user. User may
     * cancel the operation, which will cause a <code>CancelledException</code>
     * to be thrown.
     */
    public void refreshTopic() throws CancelledException, TopicMapException {
        refreshTopic(true);
    }

    
    
    
    
    /**
     * Refreshes currently open topic panel. If <code>applyChanges</code> parameter
     * is true, will apply changes first which may cause
     * topics to be merged and a warning dialog to be shown to the user. User may
     * cancel the operation, which will cause a <code>CancelledException</code>
     * to be thrown.
     */
    public void refreshTopic(boolean applyChanges) throws CancelledException, TopicMapException {
        if(applyChanges) {
            applyChanges();
        }
        reopenTopic();
        refresh();
    }
    
    
    
    private Object errorHandlerLock=new Object();
    private boolean handleErrors=true;
    /**
     * A generic method to handle exceptions thrown anywhere. Will show a dialog
     * with the message "Exception occurred" and the exception stack trace.
     * Also ErrorHandler implementation.
     */
    public void handleError(Throwable throwable) {
        synchronized(errorHandlerLock){
            /*debug*/ throwable.printStackTrace();
            if(!handleErrors) return;
            ErrorDialog ed = new ErrorDialog(this, throwable);
        }
    }
    
    
    

    // -------------------------------------------------------------------------
    // ------------------------------------------------------------- REFRESH ---
    // -------------------------------------------------------------------------
    

    
    /**
     * Refreshes the main window and main panels.
     */
    public void refresh() {
        //System.out.println("Wandora - refresh");
        Dimension d = super.getSize();
        Point l = super.getLocation();

        if(options != null) {
            options.put("options.window.width", d.width);
            options.put("options.window.height", d.height);
            options.put("options.window.x", l.x);
            options.put("options.window.y", l.y);
            options.put("options.window.horizontalsplitter",paragraphSplitPane.getDividerLocation());
            options.put("options.window.verticalsplitter",toolSplitPane.getDividerLocation());
        }
        
        //initMenuBar();
        refreshTopicTrees();
        refreshInfoFields();
        searchPanel.refresh();
        
        editorPanel.revalidate();
        searchPanel.refresh();
        finderPanel.revalidate();
        toolbarFillerPanel.revalidate();
        
        repaint();
    }



    public String makeDistributionVector(LayeredTopic topic,Layer selectedLayer,ContainerTopicMap tm) throws TopicMapException {
        StringBuilder sb=new StringBuilder();
        for(Layer l : tm.getLayers()) {
            if(sb.length()!=0) {
                sb.append(":");
            }
            TopicMap ltm=l.getTopicMap();
            if(ltm instanceof ContainerTopicMap) {
                sb.append("<font color=\"#606060\">(</font>");
                sb.append(makeDistributionVector(topic,selectedLayer,(ContainerTopicMap)ltm));
                sb.append("<font color=\"#606060\">)</font>");
            }
            else {
                if(!l.equals(selectedLayer)){
                    sb.append("<font color=\"#").append(Textbox.getColorHTMLCode(TopicHilights.notActiveLayerColor)).append("\">");
                }
                int num=0;
                if(l.isVisible()) {
                    num=l.getTopicMap().getMergingTopics(topic).size();
                    sb.append("").append(num);
                }
                else {
                    // Layer is set invisble and we don't know topic's distribution in the layer.
                    sb.append("u");
                }
                if(!l.equals(selectedLayer)) {
                    sb.append("</font>");
                }
            }
        }
        return sb.toString();
    }
    
    
    
    
    /**
     * Refreshes various labels in the main user interface.
     */
    public void refreshInfoFields() {
        refreshStatus();
        refreshLayerInfo();
        refreshTopicDistribution();
        refreshCurrentTopicString();
        refreshTopicStringifierIcon();
        refreshTopicPanelIcon();
        
        infobarPanel.revalidate();
        infobarPanel.repaint();
    }
    
    
    
    
    public void refreshStatus() {
        String statText = "";
        try {
            //statText = "local mode: "+topicMap.getNumTopics() + " topics, " + topicMap.getNumAssociations() + " associations";           
            statText = "OK";
            statLabel.setText(statText);
        }
        catch(Exception e) {
            handleError(e);
        }
    }
    
    

    private String getNumberOfTopicsAndAssociationsInCurrentLayer() {
        String label = "";
        
        try {
            LayerStack ls = topicMap;
            Layer layer = null;
            TopicMap tm = null;
            String layerName = null;
            int c=10;
            while(ls != null && c-- > 0) {
                layer = ls.getSelectedLayer();
                tm = layer.getTopicMap();
                layerName = layer.getName();
                if(tm != null && tm instanceof LayerStack) {
                    ls = (LayerStack) tm;
                }
                else {
                    ls = null;
                }
            }
            if(layer != null && layer.isVisible()) {
                if(tm != null) {
                    if(tm instanceof UndoTopicMap) {
                        tm = ((UndoTopicMap) tm).getWrappedTopicMap();
                    }
                    if(tm != null && tm instanceof TopicMapImpl) {
                        label = tm.getNumTopics() + "," + tm.getNumAssociations();
                    }
                }
            }
        }
        catch(Exception e) {
            label = "";
        }
        return label;
    }
    
    
    
    
    private Color selectedLayerReadOnlyColor = new Color(0x990000);
    private Color selectedLayerColor = new Color(0x333333);
    public void refreshLayerInfo() {
        try {
            String layerInfo = getNumberOfTopicsAndAssociationsInCurrentLayer(); // :" + (layerControlPanel.layerStack.getSelectedIndex()+1) + "/" + layerControlPanel.layerStack.getLayers().size();
            
            if(layerTree.isSelectedReadOnly()) {
                layerLabel.setForeground(selectedLayerReadOnlyColor);
            }
            else {
                layerLabel.setForeground(selectedLayerColor);
            }
            if(layerInfo != null && layerInfo.length() > 0) {
                layerLabel.setText("" + layerTree.getSelectedName() + " (" + layerInfo + ")");
            }
            else {
                layerLabel.setText("" + layerTree.getSelectedName());
            }
        }
        catch(Exception e) {
            System.out.println("Exception '"+ e.toString() +"' thrown while updating layer info!");
            //e.printStackTrace();
        }
    }
    
    
    
    
    public void refreshTopicDistribution() {
        try {
            Layer currentLayer = layerTree.getSelectedLayer();
            Topic openedTopic = topicPanelManager.getOpenTopic();
            if(openedTopic != null && openedTopic instanceof LayeredTopic && currentLayer!=null) {
                String s="<html><nobr>"+makeDistributionVector((LayeredTopic)openedTopic, currentLayer, topicMap)+"</nobr></html>";
                topicDistributionLabel.setText(s);
            }
            else {
                topicDistributionLabel.setText("-");
            }
        }
        catch(Exception e) {
            System.out.println("Exception '"+ e.toString() +"' thrown while updating topic distribution info!");
        }
    }
    
    
    
    public void refreshCurrentTopicString() {
        try {
            String name = "no open topic";
            Topic openedTopic = topicPanelManager.getOpenTopic();
            if(openedTopic != null) {
                name = TopicToString.toString(openedTopic);
            }
            topicLabel.setText(name);
        }
        catch(Exception e) {
            System.out.println("Exception '"+ e.toString() +"' thrown while updating topic status panel!");
            //e.printStackTrace();
        }
    }
    
    
    
    public void refreshTopicStringifierIcon() {
        stringifierButton.setIcon(TopicToString.getIcon());
    }
    
    
    public void refreshTopicPanelIcon() {
        panelButton.setIcon(topicPanelManager.getIcon());
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------- TOPIC FINDER ---
    // -------------------------------------------------------------------------
    
    
    private T2<Topic,Boolean> _showTopicFinder(JDialog d,java.awt.Component parent,String title,boolean clearButton,TabbedTopicSelector finder) throws TopicMapException {
        if(title!=null) {
            d.setTitle(title);
        }
        d.add(finder);
        d.setSize(500,600);
        org.wandora.utils.swing.GuiTools.centerWindow(d,parent);
        finder.init();
        if(clearButton) {
            finder.setClearVisible(true);
        }
        d.setVisible(true);
        T2<Topic,Boolean> ret=null;
        if(finder.wasCancelled()) {
            ret=new T2((Topic) null, true);
        }
        else {
            ret=new T2(finder.getSelectedTopic(), false);
        }
        finder.cleanup();
        return ret;
    }
    public Topic showTopicFinder(java.awt.Frame parent) throws TopicMapException {
        return showTopicFinder(parent,"Select topic");
    }
    public Topic showTopicFinder(java.awt.Frame parent,TabbedTopicSelector finder) throws TopicMapException {
        return showTopicFinder(parent,"Select topic",finder);
    }
    public Topic showTopicFinder(java.awt.Dialog parent) throws TopicMapException {
        return showTopicFinder(parent,"Select topic");
    }
    public Topic showTopicFinder(java.awt.Dialog parent,TabbedTopicSelector finder) throws TopicMapException {
        return showTopicFinder(parent,"Select topic",finder);
    }
    public Topic showTopicFinder() throws TopicMapException {
        return showTopicFinder("Select topic");
    }
    public Topic showTopicFinder(java.awt.Frame parent,String title) throws TopicMapException {
        return showTopicFinder(parent,title,getTopicFinder());
    }
    public Topic showTopicFinder(java.awt.Frame parent,String title,TabbedTopicSelector finder) throws TopicMapException {
        JDialog d=new JDialog(parent,true);
        return _showTopicFinder(d,parent,title,false,finder).e1;
    }
    /**
     * Opens a modal topic finder dialog which the user can use to select a topic.
     * @return The selected topic.
     */
    public Topic showTopicFinder(java.awt.Dialog parent,String title) throws TopicMapException {
        return showTopicFinder(parent,title,getTopicFinder());
    }
    public Topic showTopicFinder(java.awt.Dialog parent,String title,TabbedTopicSelector finder) throws TopicMapException {
        JDialog d=new JDialog(parent,true);
        return _showTopicFinder(d,parent,title,false,finder).e1;
    }
    public Topic showTopicFinder(String title) throws TopicMapException {
        JDialog d=new JDialog(this,true);
        return _showTopicFinder(d,this,title,false,getTopicFinder()).e1;        
    }
    public T2<Topic,Boolean> showTopicFinderWithNone() throws TopicMapException {
        return showTopicFinderWithNone(this,"Select topic");
    }
    public T2<Topic,Boolean> showTopicFinderWithNone(java.awt.Dialog parent) throws TopicMapException {
        return showTopicFinderWithNone(parent,"Select topic");
    }
    public T2<Topic,Boolean> showTopicFinderWithNone(java.awt.Frame parent) throws TopicMapException {
        return showTopicFinderWithNone(parent,"Select topic");
    }
    /**
     * Opens a modal topic finder dialog which the user can use to select a topic with
     * the option to select none.
     * @return The selected topic or null if none was selected.
     */
    public T2<Topic,Boolean> showTopicFinderWithNone(java.awt.Dialog parent,String title) throws TopicMapException {
        return showTopicFinderWithNone(parent,title,getTopicFinder());
    }
    public T2<Topic,Boolean> showTopicFinderWithNone(java.awt.Dialog parent,String title,TabbedTopicSelector finder) throws TopicMapException {
        JDialog d=new JDialog(parent,true);
        return _showTopicFinder(d,this,title,true,finder);
    }
    public T2<Topic,Boolean> showTopicFinderWithNone(java.awt.Frame parent,String title) throws TopicMapException {
        return showTopicFinderWithNone(parent,title,getTopicFinder());
    }
    public T2<Topic,Boolean> showTopicFinderWithNone(java.awt.Frame parent,String title,TabbedTopicSelector finder) throws TopicMapException {
        JDialog d=new JDialog(parent,true);
        return _showTopicFinder(d,this,title,true,finder);
    }

    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        System.out.println("Wandora catched action command '" + c + "'.");
        
        if("Reset".equalsIgnoreCase(c)) {
            resetWandora();
        }
        else {
            System.out.println("Warning: Action command " + c + " NOT processed!");
        }
    }    
    

    
    // ---- mouse -----
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    }    
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }    
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    public void centerWindow(Dialog win, Dialog parent) {
        win.setLocation(parent.getX()+parent.getWidth()/2-win.getWidth()/2,parent.getY()+parent.getHeight()/2-win.getHeight()/2);
    }
    public void centerWindow(Dialog win) {
        win.setLocation(this.getX()+this.getWidth()/2-win.getWidth()/2,this.getY()+this.getHeight()/2-win.getHeight()/2);
    }
    public void centerWindow(JDialog win, JDialog parent) {
        win.setLocation(parent.getX()+parent.getWidth()/2-win.getWidth()/2,parent.getY()+parent.getHeight()/2-win.getHeight()/2);
    }
    public void centerWindow(JDialog win) {
        win.setLocation(this.getX()+this.getWidth()/2-win.getWidth()/2,this.getY()+this.getHeight()/2-win.getHeight()/2);
    }
    public void centerWindow(JDialog win, int deltax, int deltay) {
        win.setLocation(this.getX()+this.getWidth()/2-win.getWidth()/2 + deltax,this.getY()+this.getHeight()/2-win.getHeight()/2 + deltay);
    }
    public void centerWindow(JFrame win) {
        win.setLocation(this.getX()+this.getWidth()/2-win.getWidth()/2,this.getY()+this.getHeight()/2-win.getHeight()/2);
    }
    public void centerWindow(JFrame win, int deltax, int deltay) {
        win.setLocation(this.getX()+this.getWidth()/2-win.getWidth()/2 + deltax,this.getY()+this.getHeight()/2-win.getHeight()/2 + deltay);
    }



    public static Wandora getWandora(Component c) {
        Wandora w = wandora;
        try {
            while(!(c instanceof Wandora) && c.getParent() != null) {
                c = c.getParent();
            }
            if(c instanceof Wandora) {
                w = (Wandora) c;
            }
        }
        catch (Exception e) { 
            System.out.println("Couldn't solve Wandora with component '"+c+"'. Exception '"+e.toString()+"' occurred."); 
        }
        return w;
    }


    public static Wandora getWandora() {
        return wandora;
    }
    
    
    
    public static URLConnection initUrlConnection(URLConnection uc) {
        if(uc == null) return null;
        if(USER_AGENT!=null && USER_AGENT.length()>0){
            uc.setRequestProperty("User-Agent", USER_AGENT);
        }
        else {
            uc.setRequestProperty("User-Agent", "");
        }
        return uc;
    }
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------- FOCUS OWNER ---
    // -------------------------------------------------------------------------
    
    
    
    public void gainFocus(Component c) {
        focusOwner = c;
        // System.out.println("gain focus for "+(c==null?"null":c.getClass()));
    }
    
    public void looseFocus(Component c) {
        if(focusOwner == c) {
            focusOwner = null;
        }
    }
    
    @Override
    public Component getFocusOwner() {
        return focusOwner;
    }
    
    public void clearFocus() {
        focusOwner = null;
    }

    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------- Topicmap listener ---
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    
    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicRemoved(t);
        }
    }
    
    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.associationRemoved(a);
        }
    }
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicSubjectIdentifierChanged(t,added,removed);
        }
    }
    
    @Override
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicBaseNameChanged(t,newName,oldName);
        }
    }
    
    @Override
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicTypeChanged(t,added,removed);
        }
    }
    
    @Override
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicVariantChanged(t,scope,newName,oldName);
        }
    }
    
    @Override
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicDataChanged(t,type,version,newValue,oldValue);
        }
    }
    
    @Override
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicSubjectLocatorChanged(t,newLocator,oldLocator);
        }
    }
    
    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.topicChanged(t);
        }        
    }
    
    @Override
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.associationTypeChanged(a,newType,oldType);
        }
    }
    
    @Override
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.associationPlayerChanged(a,role,newPlayer,oldPlayer);
        }
    }
    
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        if(skipTopicMapListenerEvents) return;
        for(TopicMapListener c : topicMapListeners){
            c.associationChanged(a);
        }
    }
    
    // --------------------------------------------------------- UNDO / REDO ---
    
    
    
    public void redo() throws UndoException {
        if(topicMap != null) {
            topicMap.redo();
        }
    }
    
    
    public void undo() throws UndoException {
        if(topicMap != null) {
            topicMap.undo();
        }
    }
    
    
    public void addUndoMarker() {
        addUndoMarker((String) null);
    }
    
    
    public void addUndoMarker(String label) {
        if(topicMap != null) {
            topicMap.addUndoMarker(label);
        }
    }
    
    
    public void clearUndoBuffers() {
        if(topicMap != null) {
            topicMap.clearUndoBuffers();
        }
    }
    

    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------------- MAIN ---
    // -------------------------------------------------------------------------
    
    
    public static final int WAIT_FOR_APPLICATION = 0;
    public static final int RESTART_APPLICATION = 1;
    public static final int EXIT_APPLICATION = 2;
    
    /*
     * Global variable <code>exitCode</code> is used to pass application state out of 
     * <code>Wandora</code> object. Specifically, it is used to decide if
     * the application should be restarted.
     */
    public static int exitCode = WAIT_FOR_APPLICATION;
    
    
   /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        CMDParamParser cmdparams=new CMDParamParser(args);
        UIConstants.initializeGUI();
        SplashWindow splashWindow = new SplashWindow();
        Logger.setLogger(new SimpleLogger());

        do {
            Wandora wandoraApplication = new Wandora(cmdparams);
            initializeWandoraApplication(wandoraApplication, cmdparams);
            
            if(splashWindow.isVisible()) {
                splashWindow.setVisible(false);
            }
            wandoraApplication.setVisible(true);
            exitCode = WAIT_FOR_APPLICATION;
            do {
                try { Thread.sleep(300); }
                catch(Exception e) {}
            }
            while(exitCode == WAIT_FOR_APPLICATION);
            wandoraApplication.setVisible(false);
            wandoraApplication.dispose();
        }
        while(exitCode == RESTART_APPLICATION);
        
        System.exit(0);
    }
 
    
    
    
    
    private static void initializeWandoraApplication(Wandora w, CMDParamParser cmdparams) {
        // If command line paramenters contain a project file, XTM or LTM file
        // then load the file to Wandora.
        String lastParam = cmdparams.getLast();
        if(lastParam != null) {
            String lastParamLower = lastParam.toLowerCase();
            if(lastParamLower.endsWith(".wpr")) {
                try {
                    LoadWandoraProject projectLoader = new LoadWandoraProject();
                    projectLoader.loadProject(new File(lastParam), wandora);
                }
                catch(Exception e) {
                    w.displayException("Unable to load project file '"+lastParam+"' due to an exception!", e);
                }
            }
            else if(lastParamLower.endsWith(".xtm20") || lastParamLower.endsWith(".xtm10") || lastParamLower.endsWith(".xtm1") || lastParamLower.endsWith(".xtm2") || lastParamLower.endsWith(".xtm") || lastParamLower.endsWith(".ltm") || lastParamLower.endsWith(".jtm")) {
                try {
                    TopicMapImport importer = new TopicMapImport();
                    importer.setOptions(importer.getOptions() | TopicMapImport.CLOSE_LOGS);
                    importer.forceFiles = new File(lastParam);
                    importer.execute(w);
                }
                catch(Exception e) {
                    String type = "XTM";
                    if(lastParamLower.endsWith(".ltm")) {
                        type = "LTM";
                    }
                    w.displayException("Unable to load "+type+" file '"+lastParam+"' due to an exception!", e);
                }
            }
            else if(lastParamLower.endsWith(".rdf") || lastParamLower.endsWith(".rdfs")) {
                try {
                    SimpleRDFImport importer = new SimpleRDFImport();
                    importer.setOptions(importer.getOptions() | TopicMapImport.CLOSE_LOGS);
                    importer.forceFiles = new File(lastParam);
                    importer.execute(w);
                }
                catch(Exception e) {
                    w.displayException("Unable to load RDF file '"+lastParam+"' due to an exception!", e);
                }
            }
            else if(lastParamLower.endsWith(".n3")) {
                try {
                    SimpleN3Import importer = new SimpleN3Import();
                    importer.setOptions(importer.getOptions() | TopicMapImport.CLOSE_LOGS);
                    importer.forceFiles = new File(lastParam);
                    importer.execute(w);
                }
                catch(Exception e) {
                    w.displayException("Unable to load N3 file '"+lastParam+"' due to an exception!", e);
                }
            }
            else if(lastParamLower.endsWith(".ttl")) {
                try {
                    SimpleRDFTurtleImport importer = new SimpleRDFTurtleImport();
                    importer.setOptions(importer.getOptions() | TopicMapImport.CLOSE_LOGS);
                    importer.forceFiles = new File(lastParam);
                    importer.execute(w);
                }
                catch(Exception e) {
                    w.displayException("Unable to load RDF turtle file '"+lastParam+"' due to an exception!", e);
                }
            }
            else if(lastParamLower.endsWith(".jsonld")) {
                try {
                    SimpleRDFJsonLDImport importer = new SimpleRDFJsonLDImport();
                    importer.setOptions(importer.getOptions() | TopicMapImport.CLOSE_LOGS);
                    importer.forceFiles = new File(lastParam);
                    importer.execute(w);
                }
                catch(Exception e) {
                    w.displayException("Unable to load JSON-LD file '"+lastParam+"' due to an exception!", e);
                }
            }
        }
    }

}
