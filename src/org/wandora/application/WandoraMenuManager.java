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
 *
 * 
 * 
 * WandoraMenuManager.java
 *
 * Created on 13. huhtikuuta 2006, 14:06
 *
 */

package org.wandora.application;


import bibliothek.gui.Dockable;
import java.awt.*;
import static java.awt.event.InputEvent.*;
import static java.awt.event.KeyEvent.*;
import java.util.*;
import javax.swing.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.*;
import static org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel.OPTIONS_PREFIX;
import static org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel.VARIANT_GUITYPE_OPTIONS_KEY;
import static org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel.VARIANT_GUITYPE_SCHEMA;
import static org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel.VARIANT_GUITYPE_USED;
import org.wandora.application.gui.topicstringify.DefaultTopicStringifier;
import org.wandora.application.gui.topicstringify.TopicStringifierToVariant;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.modulesserver.ModulesWebApp;
import org.wandora.application.modulesserver.WandoraModulesServer;
import org.wandora.application.tools.*;
import org.wandora.application.tools.associations.*;
import org.wandora.application.tools.associations.AddSchemalessAssociation;
import org.wandora.application.tools.associations.ModifySchemalessAssociation;
import org.wandora.application.tools.exporters.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.layers.*;
import org.wandora.application.tools.layers.ClearTopicMap;
import org.wandora.application.tools.maiana.MaianaExport;
import org.wandora.application.tools.mediawiki.MediawikiOccurrenceUploader;
import org.wandora.application.tools.mediawiki.MediawikiSubjectLocatorUploader;
import org.wandora.application.tools.mediawikiapi.MediaWikiAPIUploader;
import org.wandora.application.tools.navigate.*;
import org.wandora.application.tools.occurrences.*;
import org.wandora.application.tools.occurrences.clipboards.*;
import org.wandora.application.tools.occurrences.refine.*;
import org.wandora.application.tools.occurrences.run.RunOccurrenceAsQuery;
import org.wandora.application.tools.occurrences.run.RunOccurrenceAsTMQL;
import org.wandora.application.tools.project.*;
import org.wandora.application.tools.selections.*;
import org.wandora.application.tools.server.*;
import org.wandora.application.tools.som.*;
import org.wandora.application.tools.statistics.*;
import org.wandora.application.tools.subjects.*;
import org.wandora.application.tools.subjects.expand.*;
import org.wandora.application.tools.topicnames.*;
import org.wandora.application.tools.undoredo.Redo;
import org.wandora.application.tools.undoredo.Undo;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;



/**
 * <p>
 * WandoraMenuManager is a storage for most menu structures used in
 * Wandora. Class contains static methods to retrieve menu content
 * of the File menu, for example.
 * </p>
 * <p>
 * As menu structures are centralized here, it should be rather easy to
 * customize Wandora. 
 * </p>
 * 
 * @see WandoraToolManager
 * @author akivela
 */
public class WandoraMenuManager {

    public JMenu importToLayerMenu = new SimpleMenu("Merge to layer", UIBox.getIcon("gui/icons/layers_importto.png"));
    public JMenu exportLayerMenu = new SimpleMenu("Export layer", UIBox.getIcon("gui/icons/layers_export.png"));
    public JMenu generatorLayerMenu = new SimpleMenu("Generate to layer", UIBox.getIcon("gui/icons/layers_generateto.png"));
    
    public JMenu importMenu = new SimpleMenu("Import", UIBox.getIcon("gui/icons/import.png"));
    public JMenu exportMenu = new SimpleMenu("Export", UIBox.getIcon("gui/icons/export.png"));
    public JMenu extractMenu = new SimpleMenu("Extract", UIBox.getIcon("gui/icons/extract.png"));
    public JMenu generatorMenu = new SimpleMenu("Generate", UIBox.getIcon("gui/icons/generate.png"));

    public static HashMap SLExtractorMenus = new LinkedHashMap();
    //public static JMenu extractWithSLTableMenu = new SimpleMenu("Extract with SL"); // Here the context is default topic context.
    //public static JMenu extractWithSLTreeMenu = new SimpleMenu("Extract with SL"); // Here the context is default topic context.
    public JMenu extractWithSLTopicMenu = new SimpleMenu("Extract with SL"); // Here the context is application.
    
    public JMenu fileMenu = new SimpleMenu("File", (Icon) null);
    public JMenu editMenu = new SimpleMenu("Edit", (Icon) null);
    public JMenu viewMenu = new SimpleMenu("View", (Icon) null);
    public JMenu topicMenu = new SimpleMenu("Topics", (Icon) null);
    public JMenu layersMenu = new SimpleMenu("Layers", (Icon) null);
    public JMenu shortcutsMenu = new SimpleMenu("Shortcuts", (Icon) null);
    public JMenu toolMenu = new SimpleMenu("Tools", (Icon) null);
    public JMenu serverMenu = new SimpleMenu("Server", (Icon) null);
    public JMenu helpMenu = new SimpleMenu("Help", (Icon) null);
    
    public JMenu[] wandoraBarMenus = new JMenu[] {
        fileMenu, editMenu, viewMenu, topicMenu, layersMenu, layersMenu, shortcutsMenu, toolMenu, serverMenu, helpMenu
    };
    private JMenuBar wandoraMenuBar;    
    
    
    private Wandora wandora;
    private WandoraToolManager2 toolManager;

    private static WandoraMenuManager menuManager;
    
    
    /**
     * Creates a new instance of WandoraMenuManager
     */
    public WandoraMenuManager(Wandora wandora)  throws TopicMapException {
        this.menuManager = this;
        this.wandora = wandora;
        this.toolManager = wandora.toolManager;
        refreshMenus();
        this.wandoraMenuBar = new JMenuBar();
        refreshMenuBar();
    }
    
    
 
    
    public void refreshMenus() throws TopicMapException  {
        associationsPopupStruct = null;
        defaultAddToTopicMenuStruct = null;
        defaultBasenameMenuStruct = null;
        defaultCopyAlsoMenuStruct = null;
        defaultCopyMenuStruct = null;
        defaultDeleteFromTopicMenuStruct = null;
        defaultDeleteTopicMenuStruct = null;
        defaultNewTopicMenuStruct = null;
        defaultOccurrenceMenuStruct = null;
        defaultPasteAlsoMenuStruct = null;
        defaultSIMenuStruct = null;
        defaultSLMenuStruct = null;
        defaultSelectMenuStruct = null;
        defaultSelectMenuStructForTopicTable = null;
        defaultVariantNameMenu = null;
        layerTreeMenuStructure = null;
        logoMenuStructure = null;
        
        SLExtractorMenus = new LinkedHashMap();
        refreshFileMenu();
        refreshEditMenu();
        refreshViewMenu();
        refreshTopicsMenu();
        refreshLayersMenu();
        refreshShortcutsMenu();
        refreshToolMenu();
        refreshServerMenu();
        refreshHelpMenu();
    }
    
    
    
    public void refreshMenuBar() { 
        wandoraMenuBar.removeAll();
        JMenu menu = null;
        for(int i=0; i<wandoraBarMenus.length; i++) {
            menu = wandoraBarMenus[i];
            if(menu != null) {
                wandoraMenuBar.add( menu );
            }
            else {
                System.out.println("Warning! Null menu item found in wandoraAdminBarMenus");
            }
        }
    }
    
    
    public JMenuBar getWandoraMenuBar() {
        return wandoraMenuBar;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void refreshToolMenu() {        
        toolManager.getToolMenu( toolMenu );
    }
    
    
    
    public void refreshShortcutsMenu()  throws TopicMapException {
        wandora.shortcuts.getShortcutsMenu( shortcutsMenu );
    }
    
    
    
    
    public void refreshImportMenu() {
        toolManager.getImportMenu( importMenu );
        toolManager.getImportMenu( importToLayerMenu );
    }
    
    
    public void refreshExtractMenu() {
        toolManager.getExtractMenu( extractMenu );
        
        for(Iterator i=SLExtractorMenus.values().iterator(); i.hasNext();) {
            refreshExtractWithSLMenu( (JMenu) i.next(), null, wandora);
        }
        
        refreshExtractWithSLMenu( extractWithSLTopicMenu, new ApplicationContext() , wandora);
    }
    
    
    public void refreshExportMenu() {
        toolManager.getExportMenu( exportMenu );
        toolManager.getExportMenu( exportLayerMenu );
    }
    
    
    public void refreshGeneratorMenu() {
        toolManager.getGeneratorMenu( generatorMenu );
        toolManager.getGeneratorMenu( generatorLayerMenu );
    }
    
    
    
    public void refreshLayersMenu() {
        refreshImportMenu();
        refreshExtractMenu();
        refreshExportMenu();
        refreshGeneratorMenu();
        
        // --- Merge ---
        Object[] mergeLayers = new Object[] {
            "Merge up...", KeyStroke.getKeyStroke(VK_UP, SHIFT_MASK | ALT_MASK), new MergeLayers(MergeLayers.MERGE_UP),
            "Merge down...", KeyStroke.getKeyStroke(VK_DOWN, SHIFT_MASK | ALT_MASK), new MergeLayers(MergeLayers.MERGE_DOWN),
            "Merge all...", KeyStroke.getKeyStroke(VK_M, SHIFT_MASK | ALT_MASK), new MergeLayers(MergeLayers.MERGE_ALL),
            "Merge visible...", KeyStroke.getKeyStroke(VK_M, ALT_MASK), new MergeLayers(MergeLayers.MERGE_VISIBLE),
        };
        JMenu mergeLayersMenu =  new SimpleMenu("Merge layers", UIBox.getIcon("gui/icons/layers_merge.png"));
        UIBox.attachMenu( mergeLayersMenu, mergeLayers, wandora );
        
        // --- Arrange ---
        Object[] arrangeLayers = new Object[] {
            "Move up", UIBox.getIcon("gui/icons/move_up.png"), KeyStroke.getKeyStroke(VK_UP, ALT_MASK), new ArrangeLayers(LayerTree.MOVE_LAYER_UP),
            "Move down", UIBox.getIcon("gui/icons/move_down.png"), KeyStroke.getKeyStroke(VK_DOWN, ALT_MASK), new ArrangeLayers(LayerTree.MOVE_LAYER_DOWN),
            "Move top", UIBox.getIcon("gui/icons/move_top.png"), KeyStroke.getKeyStroke(VK_HOME, ALT_MASK), new ArrangeLayers(LayerTree.MOVE_LAYER_TOP),
            "Move bottom", UIBox.getIcon("gui/icons/move_bottom.png"), KeyStroke.getKeyStroke(VK_END, ALT_MASK), new ArrangeLayers(LayerTree.MOVE_LAYER_BOTTOM),
            "---",
            "Reverse order", UIBox.getIcon("gui/icons/reverse_order.png"), new ArrangeLayers(LayerTree.REVERSE_LAYERS),
        };
        JMenu arrangeLayersMenu =  new SimpleMenu("Arrange layers", UIBox.getIcon("gui/icons/layers_arrange.png"));
        UIBox.attachMenu( arrangeLayersMenu, arrangeLayers, wandora );
        
        // --- View ---
        Object[] viewLayers = new Object[] {
            "View all", KeyStroke.getKeyStroke(VK_V, ALT_MASK), new ViewLayers(ViewLayers.VIEW_ALL),
            "Hide all", KeyStroke.getKeyStroke(VK_H, ALT_MASK), new ViewLayers(ViewLayers.HIDE_ALL),
            "Hide all but current", KeyStroke.getKeyStroke(VK_V, SHIFT_MASK | ALT_MASK), new ViewLayers(ViewLayers.HIDE_ALL_BUT_CURRENT),
            "Reverse visibility", new ViewLayers(ViewLayers.REVERSE_VISIBILITY), 
        };
        JMenu viewLayersMenu =  new SimpleMenu("View layers", UIBox.getIcon("gui/icons/layers_view.png"));
        UIBox.attachMenu( viewLayersMenu, viewLayers, wandora );
        
        // --- Lock ---
        Object[] lockLayers = new Object[] {
            "Lock all", KeyStroke.getKeyStroke(VK_L, ALT_MASK), new LockLayers(LockLayers.LOCK_ALL),
            "Unlock all", KeyStroke.getKeyStroke(VK_U, ALT_MASK), new LockLayers(LockLayers.UNLOCK_ALL),
            "Lock all but current", KeyStroke.getKeyStroke(VK_L, SHIFT_MASK | ALT_MASK), new LockLayers(LockLayers.LOCK_ALL_BUT_CURRENT),
            "Reverse locks", new LockLayers(LockLayers.REVERSE_LOCKS),
        };
        JMenu lockLayersMenu =  new SimpleMenu("Lock layers", UIBox.getIcon("gui/icons/layers_lock.png"));
        UIBox.attachMenu( lockLayersMenu, lockLayers, wandora );
        
        // --- Stats ---
        Object[] statLayers = new Object[] {
            "Topic map info...", UIBox.getIcon("gui/icons/layer_info.png"), KeyStroke.getKeyStroke(VK_I, ALT_MASK), new TopicMapStatistics(),
            "Topic map connection statistics...", UIBox.getIcon("gui/icons/layer_acount.png"), new AssociationCounterTool(),
            "Asset weights...", UIBox.getIcon("gui/icons/asset_weight.png"), new AssetWeights(AssetWeights.CONTEXT_IS_TOPICMAP),
            "Topic map diameter...", UIBox.getIcon("gui/icons/topicmap_diameter.png"), new TopicMapDiameter(),
            //"Topic map diameter (alternative)...", UIBox.getIcon("gui/icons/topicmap_diameter.png"), new TopicMapDiameterAlternative(),
            "Average clustering coefficient...", UIBox.getIcon("gui/icons/clustering_coefficient.png"), new AverageClusteringCoefficient(),
            "Merge ratio matrix...",UIBox.getIcon("gui/icons/layer_info.png"), new MergeMatrixTool(),
            "---",
            "Occurrence summary report...", UIBox.getIcon("gui/icons/summary_report.png"), new OccurrenceSummaryReport(),
        };
        JMenu statsMenu =  new SimpleMenu("Statistics", UIBox.getIcon("gui/icons/layer_stats.png"));
        UIBox.attachMenu( statsMenu, statLayers, wandora );
        
        // --- Compose Layer Menu ---
        Object[] menuStructure = new Object[] {
            "New layer...", UIBox.getIcon("gui/icons/layer_create.png"), KeyStroke.getKeyStroke(VK_N, ALT_MASK), new NewLayer(),
            "---",
            "Rename layer", UIBox.getIcon("gui/icons/layer_rename.png"), KeyStroke.getKeyStroke(VK_R, ALT_MASK), new RenameLayer(),
            "Configure layer...", UIBox.getIcon("gui/icons/layer_configure.png"), KeyStroke.getKeyStroke(VK_O, ALT_MASK), new ConfigureLayer(),
            "Delete layer...", UIBox.getIcon("gui/icons/layer_delete.png"), KeyStroke.getKeyStroke(VK_DELETE, ALT_MASK), new DeleteLayer(),
            "Clear layer topic map...", UIBox.getIcon("gui/icons/layer_topicmap_clear.png"), new ClearTopicMap(),
            "Clear topic map indexes...", UIBox.getIcon("gui/icons/layer_index_clear.png"), new ClearTopicMapIndexes(),
            "---",
            mergeLayersMenu,
            importToLayerMenu,
            generatorLayerMenu,
            exportLayerMenu,
            "---",
            arrangeLayersMenu,
            viewLayersMenu,
            lockLayersMenu,
            "---",
            statsMenu,
        };
        layersMenu.removeAll();
        UIBox.attachMenu( layersMenu, menuStructure, wandora );
    }
    
    
    
    
    
    
    public void refreshServerMenu() {
        ArrayList menuStructure = new ArrayList();
        WandoraModulesServer httpServer = wandora.getHTTPServer();
       
        if(httpServer != null) {
            if(httpServer.isRunning()){
                menuStructure.add("Stop server");
                menuStructure.add(UIBox.getIcon("gui/icons/server_stop.png"));
                //menuStructure[2]=KeyStroke.getKeyStroke(VK_M, CTRL_MASK);
                menuStructure.add(new HTTPServerTool(HTTPServerTool.STOP_AND_MENU));
            }
            else{
                menuStructure.add("Start server");
                menuStructure.add(UIBox.getIcon("gui/icons/server_start.png"));
                //menuStructure[2]=KeyStroke.getKeyStroke(VK_M, CTRL_MASK);
                menuStructure.add(new HTTPServerTool(HTTPServerTool.START_AND_MENU));
            }

            ArrayList<ModulesWebApp> webApps=httpServer.getWebApps();
            HashMap<String,ModulesWebApp> webAppsMap=new HashMap<String,ModulesWebApp>();
            for(ModulesWebApp wa : webApps) {
                webAppsMap.put(wa.getAppName(), wa);
            }
            ArrayList<String> sorted = new ArrayList<String>(webAppsMap.keySet());
            Collections.sort(sorted);

            ArrayList browseServices = new ArrayList();
            JMenu browseServicesMenu =  new SimpleMenu("Browse services", UIBox.getIcon("gui/icons/server_open.png"));

            for(String appName : sorted) {
                ModulesWebApp wa=webAppsMap.get(appName);

                if(wa.isRunning()) {
                     String url=wa.getAppStartPage();
                     if(url==null) continue;

                     browseServices.add(appName);
                     browseServices.add(UIBox.getIcon("gui/icons/open_browser.png"));
                     browseServices.add(new HTTPServerTool(HTTPServerTool.OPEN_PAGE, wa));
                }
                else {
                     browseServices.add(appName);
                     browseServices.add(UIBox.getIcon("gui/icons/open_browser.png"));
                     browseServices.add(new HTTPServerTool(HTTPServerTool.OPEN_PAGE, wa));
                }
            }

            UIBox.attachMenu(browseServicesMenu, browseServices.toArray(), wandora);
            menuStructure.add( browseServicesMenu );

            menuStructure.add("Server settings...");
            menuStructure.add(UIBox.getIcon("gui/icons/server_configure.png"));
            menuStructure.add(new HTTPServerTool(HTTPServerTool.CONFIGURE+HTTPServerTool.UPDATE_MENU));
        }
        else {
            menuStructure.add("[No server available]");
        }

        serverMenu.removeAll();
        UIBox.attachMenu( serverMenu, menuStructure.toArray(), wandora );        
    }
   
    
    
    
    
    
    public void refreshHelpMenu() {
        Object[] menuStructure = new Object[] {
           "Wandora home", UIBox.getIcon("gui/icons/open_browser.png"), KeyStroke.getKeyStroke(VK_H, CTRL_MASK), new ExecBrowser("http://www.wandora.org"),
           "---",
           "Documentation", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://wandora.org/wiki/Documentation"),
           "Forum", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://wandora.org/forum/index.php"),
           "YouTube channel", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://wandora.org/tv/"),
           "Github repository", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("https://github.com/wandora-team/wandora"),
           "Twitter", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("https://twitter.com/wandora_app"),
           "---",
           "About Wandora...", UIBox.getIcon("gui/icons/info.png"),new AboutWandora(),
           "Wandora credits...", UIBox.getIcon("gui/icons/info.png"),new AboutCredits(),
        };
        helpMenu.removeAll();
        UIBox.attachMenu( helpMenu, menuStructure, wandora );
    }
    

    
    public void refreshViewMenu() { 
        viewMenu.removeAll();

        UIBox.attachMenu(viewMenu, wandora.topicPanelManager.getViewMenuStruct() , wandora);
        
        JMenu viewTopicAsMenu =  new SimpleMenu("View topic as", UIBox.getIcon("gui/icons/view_topic_as.png"));
        Object[] viewTopicAsMenuStructure = new Object[] {
            "Base name", UIBox.getIcon("gui/icons/view_topic_as_basename.png"), new SetTopicStringifier(new DefaultTopicStringifier(DefaultTopicStringifier.TOPIC_RENDERS_BASENAME)),
            "Subject identifier", UIBox.getIcon("gui/icons/view_topic_as_si.png"), new SetTopicStringifier(new DefaultTopicStringifier(DefaultTopicStringifier.TOPIC_RENDERS_SI)),
            "Subject identifier without domain", UIBox.getIcon("gui/icons/view_topic_as_si_wo_domain.png"), new SetTopicStringifier(new DefaultTopicStringifier(DefaultTopicStringifier.TOPIC_RENDERS_SI_WITHOUT_DOMAIN)),
            "Subject locator", UIBox.getIcon("gui/icons/view_topic_as_sl.png"), new SetTopicStringifier(new DefaultTopicStringifier(DefaultTopicStringifier.TOPIC_RENDERS_SL)),
            "---",
            "English display name", UIBox.getIcon("gui/icons/view_topic_as_english_display.png"), new SetTopicStringifier(new DefaultTopicStringifier(DefaultTopicStringifier.TOPIC_RENDERS_ENGLISH_DISPLAY_NAME)),
            "Variant name...", UIBox.getIcon("gui/icons/view_topic_as_variant.png"), new SetTopicStringifier(new TopicStringifierToVariant()),
            "---",
            "Base name with topic info", UIBox.getIcon("gui/icons/view_topic_as_basename.png"), new SetTopicStringifier(new DefaultTopicStringifier(DefaultTopicStringifier.TOPIC_RENDERS_BASENAME_WITH_INFO)),
        };
                
        UIBox.attachMenu( viewTopicAsMenu, viewTopicAsMenuStructure, wandora );

        viewMenu.add(new JSeparator());
        viewMenu.add(viewTopicAsMenu);
    }
    
    
    
    public void refreshTopicsMenu() {
        
        Object[] splitTopicStructure = new Object[] {
            "Split topic with subject identifiers", KeyStroke.getKeyStroke(VK_S, CTRL_MASK | SHIFT_MASK), new SplitTopics(new ApplicationContext()), 
            "Split topic with a base name regex...", new SplitTopicsWithBasename(new ApplicationContext()),
            "---",
            "Split to descending instances with a base name regex...", new SplitToInstancesWithBasename(new ApplicationContext(), true),
            "Split to ascending instances with a base name regex...", new SplitToInstancesWithBasename(new ApplicationContext(), false),
            "---",
            "Split to descending superclasses with a base name regex...", new SplitToSuperclassesWithBasename(new ApplicationContext(), true),
            "Split to ascending superclasses with a base name regex...", new SplitToSuperclassesWithBasename(new ApplicationContext(), false),
        };
        JMenu splitTopicMenu = new SimpleMenu("Split topic", UIBox.getIcon("gui/icons/topic_split.png"));
        splitTopicMenu.removeAll();
        UIBox.attachMenu( splitTopicMenu, splitTopicStructure, wandora );
        
        // ---- THE STRUCTURE ----
        Object[] menuStructure = new Object[] {
            "Open topic", UIBox.getIcon("gui/icons/topic_open.png"), KeyStroke.getKeyStroke(VK_O, CTRL_MASK), new OpenTopic(OpenTopic.ASK_USER),
            "Close panel", UIBox.getIcon("gui/icons/topic_close.png"), KeyStroke.getKeyStroke(VK_W, CTRL_MASK), new CloseCurrentTopicPanel(),
            "---",
            "New topic...", UIBox.getIcon("gui/icons/new_topic.png"), KeyStroke.getKeyStroke(VK_N, CTRL_MASK), new NewTopicExtended(),
            "Delete topic...", UIBox.getIcon("gui/icons/topic_delete.png"), KeyStroke.getKeyStroke(VK_DELETE, CTRL_MASK), new DeleteTopics(new ApplicationContext()),
            "Duplicate topic...", UIBox.getIcon("gui/icons/topic_duplicate.png"), KeyStroke.getKeyStroke(VK_D, CTRL_MASK), new DuplicateTopics(new ApplicationContext()),
            splitTopicMenu,
            
            "---",
            "Add to topic", new Object[] {
                "Add class...", new AddClass(new ApplicationContext()),
                "Add instance...",new AddInstance(new ApplicationContext()),
//                "Add associations...", new AddAssociations(new ApplicationContext()),
                "Add association...", new AddSchemalessAssociation(new ApplicationContext()),
                //"Add occurrence...", new AddOccurrences(new ApplicationContext()),
                "Add variant name...", new AddVariantName(new ApplicationContext()),
                "Add occurrence...", new AddSchemalessOccurrence(new ApplicationContext()),
                "Add subject identifier...", new AddSubjectIdentifier(new ApplicationContext()),
            },
            "Delete from topic", new Object[] {
                "Delete associations with type...",new DeleteAssociationsInTopicWithType(new ApplicationContext()),
                "Delete empty and unary associations...", new DeleteUnaryAssociations(new ApplicationContext()),
                "Delete all associations...",new DeleteAssociationsInTopic(new ApplicationContext()),
                "---",
                "Delete base name...", new BasenameRemover(new ApplicationContext()),
                "Delete all variant names...", new AllVariantRemover(new ApplicationContext()),
                "---",
                "Delete instances...",new DeleteFromTopics(new ApplicationContext(), DeleteFromTopics.LOOSE_INSTANCES),
                "Delete classes...",new DeleteFromTopics(new ApplicationContext(), DeleteFromTopics.LOOSE_CLASSES),
                "---",
                "Delete all but one SI...", new FlattenSubjectIdentifiers(new ApplicationContext()),
                "Delete SL...", new DeleteSubjectLocator(new ApplicationContext()),
            },
            "---",
            
            "Subject locator", new Object[] {
                "Open subject locator...", new OpenSubjectLocator(new ApplicationContext()),
                "Check subject locator...", new CheckSubjectLocator(new ApplicationContext()),
                "---",
                "Make subject locator from a filename...", new MakeSubjectLocatorFromFilename(new ApplicationContext()),
                "Make subject locator from file content...", new MakeSubjectLocatorFromFileContent(new ApplicationContext()),
                "Make subject locator from a subject identifier...", new MakeSubjectLocatorFromSubjectIdentifier(new ApplicationContext()),
                "Make subject locator from a base name...", new MakeSubjectLocatorFromBasename(new ApplicationContext()),
                "Make subject locator from an occurrence...", new MakeSubjectLocatorFromOccurrence(new ApplicationContext()),
                "---",
                "Download subject locator...", new DownloadSubjectLocators(new ApplicationContext()),
                "Download and change subject locator...", new DownloadSubjectLocators(new ApplicationContext(), true),
                "Upload subject locator resource to Mediawiki...", new MediawikiSubjectLocatorUploader(new ApplicationContext()),
                "---",
                "Find subject locator...", new FindSubjectLocator(new ApplicationContext()),
                "Find subject locator with base name...", new FindSubjectLocatorWithBasename(new ApplicationContext()),
                "---",
                "Modify subject locator with a regex...", new ModifySubjectLocatorWithRegex(new ApplicationContext()),
                "---",
                "Remove subject locator...", new DeleteSubjectLocator(new ApplicationContext()),
                "---",
                extractWithSLTopicMenu,
            },
            "Subject identifiers", new Object[] {
                "Open subject identifiers...", new OpenSubjectIdentifier(new ApplicationContext()),
                "Check subject identifiers...", new CheckSubjectIdentifiers(new ApplicationContext()),
                "---",
                "Make subject identifier from a filename...", new MakeSubjectIdentifierFromFilename(new ApplicationContext()),
                "Make subject identifier from a subject locator", new MakeSubjectIdentifierFromSubjectLocator(new ApplicationContext()),
                "Make subject identifier from a base name...", new MakeSubjectIdentifierFromBasename(new ApplicationContext()),
                "Make subject identifier from an occurrence...", new MakeSubjectIdentifierFromOccurrence(new ApplicationContext()),
                "---",
                "Copy subject identifiers", new CopySubjectIdentifiers(new ApplicationContext()),
                "Paste subject identifiers", new PasteSubjectIdentifiers(new ApplicationContext()),
                "Duplicate subject identifiers...", new DuplicateSubjectIdentifier(new ApplicationContext()),
                "---",
                "Flatten identity...", new FlattenSubjectIdentifiers(new ApplicationContext()),
                "Remove subject identifiers with a regex...", new DeleteSubjectIdentifiersWithRegex(new ApplicationContext()),
                "---",
                "Remove references in subject identifiers", new RemoveReferencesInSubjectIdentifiers(new ApplicationContext()),
                "Modify subject identifiers with a regex...", new ModifySubjectIdentifiersWithRegex(new ApplicationContext()),
                // "Fix SIs", new SIFixer(new ApplicationContext()),
                "Fix subject identifiers", new FixSubjectIdentifiers2(new ApplicationContext()),
            },
            "Base name", new Object[] {
                "Make base name from a subject identifier", new MakeBasenameFromSubjectIdentifier(new ApplicationContext()),
                "Make base name from an occurrence...", new MakeBasenameFromOccurrence(new ApplicationContext()),
                "---",
                "Modify base name with a regex...", new BasenameRegexReplacer(new ApplicationContext()),
                "Remove new line characters", new BasenameNewlineRemover(new ApplicationContext()),
                "Collapse white spaces", new BasenameWhiteSpaceCollapser(new ApplicationContext()),
                "Trim base name", new BasenameTrimmer(new ApplicationContext()),
                "---",
                "Remove base name...", new BasenameRemover(new ApplicationContext()),
            },
            "Variant names", new Object[] {
                "Make display variants from occurrences", new MakeDisplayVariantsFromOccurrences(new ApplicationContext()), 
                "Make display variants from base names", new MakeDisplayVariantsFromBasename(new ApplicationContext()),
                "Make sort variants from base names", new MakeSortVariantsFromBasename(new ApplicationContext()),
                "---",
                "Copy all variant names to clipboard", new TopicNameCopier(new ApplicationContext()),
                "---",
                "Copy variant names to other scope...", new VariantScopeCopier(new ApplicationContext()),
                "Move variant names to other scope...", new VariantScopeCopier(true, new ApplicationContext()),
                "---",
                "Transform variants to topics...", new VariantsToTopicsAndAssociations(new ApplicationContext()),
                "---",
                "Translate with Google...", new VariantGoogleTranslate(new ApplicationContext()),
                "Translate with Microsoft...", new VariantMicrosoftTranslate(new ApplicationContext()),
                "Translate with Watson...", new VariantWatsonTranslate(new ApplicationContext()),
                "---",
                "Modify variant names with a regex...", new VariantRegexReplacer(new ApplicationContext()),
                "Remove new line characters", new VariantNewlineRemover(new ApplicationContext()),
                "Collapse white spaces", new VariantWhiteSpaceCollapser(new ApplicationContext()),
                "---",
                "Add missing display scope", new AddImplicitDisplayScopeToVariants(new ApplicationContext()),
                "Add missing sort scope", new AddImplicitSortScopeToVariants(new ApplicationContext()),
                "Add missing language...", new AddMissingLanguageScope(new ApplicationContext()),

                "---",
                "Remove variant names...", new VariantRemover(new ApplicationContext()),
                "Remove all empty variant names...", new AllEmptyVariantRemover(new ApplicationContext()),
                "Remove all variant names...", new AllVariantRemover(new ApplicationContext()),


            },
            "Occurrences", new Object[] {
                "Make occurrence from a subject locator...", new MakeOccurrenceFromSubjectLocator(new ApplicationContext()),
                "Make occurrence from a subject identifier...", new MakeOccurrenceFromSubjectIdentifier(new ApplicationContext()),
                "Make occurrence from a base name...", new MakeOccurrenceFromBasename(new ApplicationContext()),
                "Make occurrence from a variant name...", new MakeOccurrenceFromVariant(new ApplicationContext()),
                "Make occurrences from all variant name languages...", new MakeOccurrencesFromVariants(new ApplicationContext()),
                "Make occurrence from an association...", new MakeOccurrenceFromAssociation(new ApplicationContext()),
                "---",
                "Modify occurrences with a regex...", new OccurrenceRegexReplacerOne(new ApplicationContext()),
                "Modify all occurrences with a regex...", new OccurrenceRegexReplacerAll(new ApplicationContext()),
                "---",
                "Copy occurrence to all other scopes...", new SpreadOccurrence(new ApplicationContext()),
                "Copy occurrence to other scope...", new OccurrenceScopeCopier(new ApplicationContext()),
                "Move occurrence to other scope...", new OccurrenceScopeCopier(true, new ApplicationContext()),
                "---",
                "Translate with Google...", new OccurrenceGoogleTranslate(new ApplicationContext()),
                "Translate with Microsoft...", new OccurrenceMicrosoftTranslate(new ApplicationContext()),
                "Translate with Watson...", new OccurrenceWatsonTranslate(new ApplicationContext()),
                "---",
                "Check URL occurrences...", new URLOccurrenceChecker(new ApplicationContext()),
                "Download URL occurrences...", new DownloadOccurrence(new ApplicationContext()),
                "Download all URL occurrences...", new DownloadAllOccurrences(new ApplicationContext()),
                "---",
                "Upload to Pastebin...", new PasteBinOccurrenceUploader(new ApplicationContext()),
                "Download from Pastebin", new PasteBinOccurrenceDownloader(new ApplicationContext()),
                "---",
                "Upload URL resource to MediaWiki", new MediawikiOccurrenceUploader(new ApplicationContext()),
                "Upload content to MediaWiki...", new MediaWikiAPIUploader(new ApplicationContext()),
                "---",
                "Delete occurrence with type...", new DeleteOccurrence(new ApplicationContext()),
            },
            "---",
            "Schema", new Object[] {
                "Create association type...", new CreateAssociationType(new EmptyContext()),
                "Create association type from selected topics...", new CreateAssociationType(new TopicContext()),
                "Create association type from selected association...", new CreateAssociationType(new AssociationContext()),
            }

        };
        topicMenu.removeAll();
        UIBox.attachMenu( topicMenu, menuStructure, wandora );
    }
    
    
    
    
    public void refreshEditMenu() {
        Object[] menuStructure = new Object[] {
            "Undo",  KeyStroke.getKeyStroke(VK_Z, CTRL_MASK), UIBox.getIcon("gui/icons/undo_undo.png"), new Undo(),
            "Redo",  KeyStroke.getKeyStroke(VK_Y, CTRL_MASK), UIBox.getIcon("gui/icons/undo_redo.png"), new Redo(),
            "---",
            "Cut",  KeyStroke.getKeyStroke(VK_X, CTRL_MASK), UIBox.getIcon("gui/icons/cut.png"), new SystemClipboard(SystemClipboard.CUT),
            "Copy",  KeyStroke.getKeyStroke(VK_C, CTRL_MASK), UIBox.getIcon("gui/icons/copy.png"), new SystemClipboard(SystemClipboard.COPY),
            "Paste",  KeyStroke.getKeyStroke(VK_V, CTRL_MASK), UIBox.getIcon("gui/icons/paste.png"), new SystemClipboard(SystemClipboard.PASTE),
            "---",
            "Select",
                new Object[] {
                "Select all", KeyStroke.getKeyStroke(VK_A, CTRL_MASK), new SelectAll(),
                /*
                "Select row(s)", new SelectRows(),
                "Select column(s)", new SelectColumns(),
                 **/
                "Deselect", new ClearSelection(),
                "Invert selection", new InvertSelection(),
                "---",
                "Select topics without associations", new SelectTopicIfNoAssociations(),
                "Select topics without occurrences", new SelectTopicIfNoOccurrences(),
                "Select topics without base names", new SelectTopicIfNoBasenames(),
                "Select topics without classes", new SelectTopicIfNoClasses(),
                "Select topics without instances", new SelectTopicIfNoInstances(),
                "Select topics without A+I", new SelectTopicIfNoAI(),
                "---",
                "Select topics with SL", new SelectTopicIfSL(),
                "Select topics with typed associations", new SelectTopicWithAssociationType(),
                "---",
                "Select topics with clipboard identifiers", new SelectTopicWithClipboard(),
                "Select topics with clipboard regexes", new SelectTopicWithClipboardRegex(),
                "Select topics with clipboard regex finders", new SelectTopicWithClipboardRegex(false),
            },
            "Selection info...", new SelectionInfo(),
            "---",
            /*
            "[Copy topic]", new CopyToClipBoard(clipboardtm),
            "[Import topic clipboard...]", new ImportClipBoard(clipboardtm),
            "[Save topic clipboard...]", new SaveClipBoard(clipboardtm),
            "---",
             **/
            "Search...", KeyStroke.getKeyStroke(VK_F, CTRL_MASK), UIBox.getIcon("gui/icons/find_topics.png"), new Search(),
            // "Go to...", KeyStroke.getKeyStroke(VK_G, CTRL_MASK), UIBox.getIcon("gui/icons/goto2.png"), new OpenTopicWithSX(),
            // "---",
            // "Locate topic in tree", new LocateSelectTopicInTree(),
        };
        editMenu.removeAll();
        UIBox.attachMenu( editMenu, menuStructure, wandora );
    }
    
    
        
        
        
    public void refreshFileMenu() {
        refreshImportMenu();
        refreshExtractMenu();
        refreshExportMenu();
        refreshGeneratorMenu();
        
        Object[] patchMenuStruct = new Object[] {
            "Compare topic maps...", UIBox.getIcon("gui/icons/compare_topicmaps.png"), new DiffTool(),
            "Apply topic map patch...", UIBox.getIcon("gui/icons/patch_topicmap.png"), new ApplyPatchTool(),
        };
        JMenu patchMenu =  new SimpleMenu("Compare and patch", UIBox.getIcon("gui/icons/compare_patch_topicmaps.png"));
        patchMenu.removeAll();
        UIBox.attachMenu( patchMenu, patchMenuStruct, wandora );
        
        Object[] menuStructure = new Object[] {
            "New project...", KeyStroke.getKeyStroke(VK_N, CTRL_MASK | SHIFT_MASK), UIBox.getIcon("gui/icons/new_project.png"), new ResetWandora(),
            "Open project...", KeyStroke.getKeyStroke(VK_L, CTRL_MASK), UIBox.getIcon("gui/icons/load_project.png"), new LoadWandoraProject(),
            "Merge project...", KeyStroke.getKeyStroke(VK_M, CTRL_MASK), UIBox.getIcon("gui/icons/merge_project.png"), new MergeWandoraProject(),
            "---",
            "Save project", KeyStroke.getKeyStroke(VK_S, CTRL_MASK), UIBox.getIcon("gui/icons/save_project.png"), new SaveWandoraProject(true),
            "Save project as...", KeyStroke.getKeyStroke(VK_S, CTRL_MASK | SHIFT_MASK), UIBox.getIcon("gui/icons/save_project_as.png"), new SaveWandoraProject(),
            "Revert", UIBox.getIcon("gui/icons/revert_project.png"), new RevertWandoraProject(),
            "---",

            importMenu, 
            extractMenu,
            generatorMenu,
            exportMenu, 
            
            "---",
            patchMenu,
            "---",
            "Print...", new PrintTopic(), KeyStroke.getKeyStroke(VK_P, CTRL_MASK), UIBox.getIcon("gui/icons/print.png"),
            "---",
            "Exit", KeyStroke.getKeyStroke(VK_Q, CTRL_MASK), UIBox.getIcon("gui/icons/exit.png"), new ExitWandora(),
        };
        
        fileMenu.removeAll();
        UIBox.makeMenu( fileMenu, menuStructure, wandora );
    }
    
    
    
     
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public String disableMenuIfNoTopic(String menuName) {
        return menuName;
        // TO DO... At the moment menus are static!!! Menu's visual representation
        // does NOT change if there is no topic open in topic panel.
        
        // return (this.openedTopic == null ? "["+ menuName +"]" : menuName);
    }
    
    
    
    public void hideMenu(String menuName) {
        Component menu = UIBox.getComponentByName(menuName, wandora);
        if(menu != null) {
            menu.setVisible(false);
        }
     }
    public void showMenu(String menuName) {
        Component menu = UIBox.getComponentByName(menuName, wandora);
        if(menu != null) {
            menu.setVisible(true);
        }
     }
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------ STATIC MENU STRUCTS ----
    // -------------------------------------------------------------------------
    
    
    
    
    private static Object[] defaultNewTopicMenuStruct = null;
    public static Object[] getDefaultNewTopicMenuStruct(Wandora admin, Object source) {
        if(defaultNewTopicMenuStruct == null) {
            try {
                defaultNewTopicMenuStruct = new Object[] {                    
                    "New topic...", new NewTopicExtended(),
                    "---",
                    "New instance topic...", new NewTopic(NewTopic.MAKE_INSTANCE_OF_CONTEXT),
                    "New subclass topic...", new NewTopic(NewTopic.MAKE_SUBCLASS_OF_CONTEXT),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultNewTopicMenuStruct;
    }
    
    
    
    
    
    private static Object[] defaultDeleteTopicMenuStruct = null;
    public static Object[] getDefaultDeleteTopicMenuStruct(Wandora admin, Object source) {
        if(defaultDeleteTopicMenuStruct == null) {
            try {
                defaultDeleteTopicMenuStruct = new Object[] {
                    "Delete topics...", new DeleteTopics(),
                    "---",
                    "Delete topics without A+C+I...", new DeleteTopicsWithoutACI(),
                    "Delete topics without A+I...", new DeleteTopicsWithoutAI(),
                    "Delete topics without associations...", new DeleteTopicsWithoutAssociations(),
                    "Delete topics without classes...", new DeleteTopicsWithoutClasses(),
                    "Delete topics without instances...", new DeleteTopicsWithoutInstances(),
                    "Delete topics without base names...", new DeleteTopicsWithoutBasename(),
                    "Delete topics without variant names...", new DeleteTopicsWithoutVariants(),
                    "Delete topics without occurrences...", new DeleteTopicsWithoutOccurrences(),
                    "---",
                    "Delete topics with SI regexes...", new DeleteTopicsWithSIRegex(),
                    "Delete topics with base name regexes...", new DeleteTopicsWithBasenameRegex(),
                    "---",
                    "Delete all topics in layer except selected...", new DeleteTopicsExceptSelected(),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultDeleteTopicMenuStruct;
    }
    
    
    
    private static Object[] defaultAddToTopicMenuStruct = null;
    public static Object[] getDefaultAddToTopicMenuStruct(Wandora admin, Object source) {
        if(defaultAddToTopicMenuStruct == null) {
            try {
                defaultAddToTopicMenuStruct = new Object[] {
                    "Add class...", new AddClass(),
                    "Add instance...",new AddInstance(),
                    // "Add associations...", new AddAssociations(),
                    "Add association...", new AddSchemalessAssociation(),
                    // "Add occurrences...", new AddOccurrences(),
                    "Add variant name...", new AddVariantName(),
                    "Add occurrence...", new AddSchemalessOccurrence(),
                    "Add subject identifier...", new AddSubjectIdentifier(),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultAddToTopicMenuStruct;
    }
    
    
    
    
    private static Object[] defaultDeleteFromTopicMenuStruct = null;
    public static Object[] getDefaultDeleteFromTopicMenuStruct(Wandora admin, Object source) {
        if(defaultDeleteFromTopicMenuStruct == null) {
            try {
                defaultDeleteFromTopicMenuStruct = new Object[] {
                    "Delete base names within...", new BasenameRemover(),
                    "Delete variant names within...", new VariantRemover(),
                    "Delete all variant names within...", new AllVariantRemover(),
                    "---",
                    "Delete classes within...", new DeleteFromTopics(DeleteFromTopics.LOOSE_CLASSES), 
                    "Delete instances within...", new DeleteFromTopics(DeleteFromTopics.LOOSE_INSTANCES), 
                    "---",
                    "Delete associations within...", new DeleteAssociationsInTopic(), 
                    "Delete associations within with type...",new DeleteAssociationsInTopicWithType(),
                    "Delete empty and unary associations within...", new DeleteUnaryAssociations(),
                    "---",
                    "Delete occurrences within...", new DeleteFromTopics(DeleteFromTopics.DELETE_TEXTDATAS),
                    "Delete occurrences within with type...", new DeleteOccurrence(),
                    "---",
                    "Delete subject locator within...", new DeleteSubjectLocator(),
                    "Delete all but one subject identifier within...", new FlattenSubjectIdentifiers(),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultDeleteFromTopicMenuStruct;
    }
    
    
    
    
    private static Object[] defaultSelectMenuStructForTopicGrid = null;
    private static Object[] defaultSelectMenuStructForTopicTable = null;
    private static Object[] defaultSelectMenuStruct = null;
    
    public static Object[] getDefaultSelectMenuStruct(Wandora admin, Object source) {
        Object[] menuStruct = null;
        try {
            if(source instanceof TopicGrid) {
                if(defaultSelectMenuStructForTopicGrid == null) {
                    defaultSelectMenuStructForTopicGrid = new Object[] {
                        "Select all", new SelectAll(),
                        "Select row(s)", new SelectRows(),
                        "Select column(s)", new SelectColumns(),
                        "Invert selection", new InvertSelection(),
                        "Deselect", new ClearSelection(),
                        "---",
                        "Select topics without associations", new SelectTopicIfNoAssociations(),
                        "Select topics without occurrences", new SelectTopicIfNoOccurrences(),
                        "Select topics without base names", new SelectTopicIfNoBasenames(),
                        "Select topics without classes", new SelectTopicIfNoClasses(),
                        "Select topics without instances", new SelectTopicIfNoInstances(),
                        "Select topics without A+I", new SelectTopicIfNoAI(),
                        "---",
                        "Select topics with SL", new SelectTopicIfSL(),
                        "Select topics with typed associations", new SelectTopicWithAssociationType(),
                        "---",
                        "Select topics with clipboard identifiers", new SelectTopicWithClipboard(),
                        "Select topics with clipboard regexes", new SelectTopicWithClipboardRegex(),
                        "Select topics with clipboard regex finders", new SelectTopicWithClipboardRegex(false),
                        "---",
                        "Select topics in selected layer", new SelectTopicIfInLayer(),
                        "Select topics not in selected layer", new SelectTopicIfNotInLayer(),
                        "---",
                        "Selection info...", new SelectionInfo(),
                    };
                }
                menuStruct = defaultSelectMenuStructForTopicGrid;
            }
            else if(source instanceof TopicTable) {
                if(defaultSelectMenuStructForTopicTable == null) {
                    defaultSelectMenuStructForTopicTable = new Object[] {
                        "Select all", new SelectAll(),
                        "Select row(s)", new SelectRows(),
                        "Select column(s)", new SelectColumns(),
                        "Invert selection", new InvertSelection(),
                        "Deselect", new ClearSelection(),
                        "---",
                        "Select topics without associations", new SelectTopicIfNoAssociations(),
                        "Select topics without occurrences", new SelectTopicIfNoOccurrences(),
                        "Select topics without base names", new SelectTopicIfNoBasenames(),
                        "Select topics without classes", new SelectTopicIfNoClasses(),
                        "Select topics without instances", new SelectTopicIfNoInstances(),
                        "Select topics without A+I", new SelectTopicIfNoAI(),
                        "---",
                        "Select topics with subject locator", new SelectTopicIfSL(),
                        "Select topics with typed associations", new SelectTopicWithAssociationType(),
                        "---",
                        "Select topics with clipboard identifiers", new SelectTopicWithClipboard(),
                        "Select topics with clipboard regexes", new SelectTopicWithClipboardRegex(),
                        "Select topics with clipboard regex finders", new SelectTopicWithClipboardRegex(false),
                        "---",
                        "Select topics in selected layer", new SelectTopicIfInLayer(),
                        "Select topics not in selected layer", new SelectTopicIfNotInLayer(),
                        "---",
                        "Selection info...", new SelectionInfo(),
                    };
                }
                menuStruct = defaultSelectMenuStructForTopicTable;
            }
            else {
                if(defaultSelectMenuStruct == null) {
                    defaultSelectMenuStruct = new Object[] {
                        "Select all", new SelectAll(),
                        "Invert selection", new InvertSelection(),
                        "Deselect", new ClearSelection(),
                        "---",
                        "Select topics without associations", new SelectTopicIfNoAssociations(),
                        "Select topics without occurrences", new SelectTopicIfNoOccurrences(),
                        "Select topics without base names", new SelectTopicIfNoBasenames(),
                        "Select topics without classes", new SelectTopicIfNoClasses(),
                        "Select topics without instances", new SelectTopicIfNoInstances(),
                        "Select topics without A+I", new SelectTopicIfNoAI(),
                        "---",
                        "Select topics with subject locator", new SelectTopicIfSL(),
                        "Select topics with typed associations", new SelectTopicWithAssociationType(),
                        "---",
                        "Select topics with clipboard identifiers", new SelectTopicWithClipboard(),
                        "Select topics with clipboard regexes", new SelectTopicWithClipboardRegex(),
                        "Select topics with clipboard regex finders", new SelectTopicWithClipboardRegex(false),
                        "---",
                        "Select topics in selected layer", new SelectTopicIfInLayer(),
                        "Select topics not in selected layer", new SelectTopicIfNotInLayer(),
                        "---",
                        "Selection info...", new SelectionInfo(),
                    };
                }
                menuStruct = defaultSelectMenuStruct;
            }
        }
        catch(Exception e) {
            if(admin != null) admin.handleError(e);
            else e.printStackTrace();
        }
        return menuStruct;
    }
    
    
    
    
    private static Object[] defaultCopyAlsoMenuStruct = null;
    public static Object[] getDefaultCopyAlsoMenuStruct(Wandora admin, Object source) {
        if(defaultCopyAlsoMenuStruct == null) {
            try {
                defaultCopyAlsoMenuStruct = new Object[] {
                    "Copy also names", new CopyTopics(CopyTopics.INCLUDE_NAMES),
                    "Copy also subject locator", new CopyTopics(CopyTopics.INCLUDE_SLS),
                    "Copy also subject identifiers", new CopyTopics(CopyTopics.INCLUDE_SIS),
                    "Copy also classes", new CopyTopics(CopyTopics.INCLUDE_CLASSES),
                    "Copy also instances", new CopyTopics(CopyTopics.INCLUDE_INSTANCES),
                    "Copy also players...", new CopyTopics(CopyTopics.INCLUDE_PLAYERS),
                    "Copy also played roles", new CopyTopics(CopyTopics.INCLUDE_PLAYED_ROLES),
                    "Copy also occurrences...", new CopyTopics(CopyTopics.INCLUDE_OCCURRENCES),
                    "Copy also all occurrences", new CopyTopics(CopyTopics.INCLUDE_ALL_OCCURRENCES),
                    "Copy also occurrence types", new CopyTopics(CopyTopics.INCLUDE_OCCURRENCE_TYPES),
                    "Copy also association types", new CopyTopics(CopyTopics.INCLUDE_ASSOCIATION_TYPES),
                    "---",
                    "Copy also subject identifier count", new CopyTopics(CopyTopics.INCLUDE_SI_COUNT),
                    "Copy also class count", new CopyTopics(CopyTopics.INCLUDE_CLASS_COUNT),
                    "Copy also instance count", new CopyTopics(CopyTopics.INCLUDE_INSTANCE_COUNT),
                    "Copy also association count", new CopyTopics(CopyTopics.INCLUDE_ASSOCIATION_COUNT),
                    "Copy also typed association count...", new CopyTopics(CopyTopics.INCLUDE_TYPED_ASSOCIATION_COUNT),
                    "Copy also occurrence count", new CopyTopics(CopyTopics.INCLUDE_OCCURRENCE_COUNT),
                    "---",
                    "Copy also topic's layer distribution", new CopyTopics(CopyTopics.INCLUDE_LAYER_DISTRIBUTION),
                    "Copy also topic's clustering coefficient", new CopyTopics(CopyTopics.INCLUDE_CLUSTER_COEFFICIENT),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultCopyAlsoMenuStruct;
    }
    
    
    
    
    private static Object[] defaultCopyMenuStruct = null;
    public static Object[] getDefaultCopyMenuStruct(Wandora admin, Object source) {
        if(defaultCopyMenuStruct == null) {
            try {
                defaultCopyMenuStruct = new Object[] {
                    "Copy base name", new CopyTopics(),
                    "Copy subject identifier", new CopyTopics(CopyTopics.COPY_SIS, CopyTopics.INCLUDE_NOTHING),
                    "---",
                    "Copy instances within...", new CopyTopicInstances(),
                    "Copy classes within...", new CopyTopicClasses(),
                    "Copy associations within...", new CopyAssociations(),
                    "Copy roles within...", new CopyTopicRoles(),
                    "Copy players within...", new CopyTopicPlayers(),
                    "Copy association types within...", new CopyTopicAssociationTypes(),
                    "Copy variant names within", new TopicNameCopier(),
                    "---",
                    "Copy as image", new CopyAsImage(),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultCopyMenuStruct;
    }
    
    
    
    private static Object[] defaultCopyToLayerMenuStruct = null;
    public static Object[] getDefaultCopyToLayerMenuStruct(Wandora wandora, Object source) {
        if(defaultCopyToLayerMenuStruct == null) {
            try {
                defaultCopyToLayerMenuStruct = new Object[] {
                    "Copy to layer as a single subject identifier stub", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC_AS_A_SINGLE_SI_STUB),
                    "Copy to layer as a subject identifier stub", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC_AS_A_SI_STUB),
                    "---",
                    "Copy to layer as a stub", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC_AS_A_STUB),
                    "Copy to layer as a stub with variants", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC_AS_A_STUB_WITH_VARIANTS),
                    "Copy to layer as a stub with occurrences", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC_AS_A_STUB_WITH_OCCURRENCES),
                    "---",
                    "Copy to layer as a complete topic", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC),
                    "Copy to layer as a complete topic with associations", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_TOPIC_WITH_ASSOCIATIONS),
                    "---",
                    "Deep copy to layer...", new CopyTopicsToLayer(CopyTopicsToLayer.COPY_DEEP),
                };
            }
            catch(Exception e) {
                if(wandora != null) wandora.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultCopyToLayerMenuStruct;
    }
    
    
    
    
    public static Object[] getDefaultPasteMenuStruct(Wandora admin, Object source) {
        Object[] menuStruct = null;
        try {
            menuStruct = new Object[] {
                "Paste instances", new PasteInstances(),
                "Paste classes", new PasteClasses(),
                "Paste associations", new PasteAssociations(),
            };
        }
        catch(Exception e) {
            if(admin != null) admin.handleError(e);
            else e.printStackTrace();
        }
        return menuStruct;
    }
    
    
    
    private static Object[] defaultPasteAlsoMenuStruct = null;
    public static Object[] getDefaultPasteAlsoMenuStruct(Wandora admin, Object source) {
        if(defaultPasteAlsoMenuStruct == null) {
            try {
                defaultPasteAlsoMenuStruct = new Object[] {
                    "Paste also names...", new PasteTopics(PasteTopics.INCLUDE_NAMES),
                    "Paste also subject locators...", new PasteTopics(PasteTopics.INCLUDE_SLS),
                    "Paste also subject identifiers...", new PasteTopics(PasteTopics.INCLUDE_SIS),
                    "Paste also classes...", new PasteTopics(PasteTopics.INCLUDE_CLASSES),
                    "Paste also instances...", new PasteTopics(PasteTopics.INCLUDE_INSTANCES),
                    "Paste also players...", new PasteTopics(PasteTopics.INCLUDE_PLAYERS),
                    "Paste also occurrences...", new PasteTopics(PasteTopics.INCLUDE_TEXTDATAS),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultPasteAlsoMenuStruct;
    }
    

    
    private static Object[] defaultSLMenuStruct = null; 
    public static Object[] getDefaultSLMenuStruct(Wandora admin, Object source) {
        if(true || defaultSLMenuStruct == null) {
            //System.out.println("Creating extractor menus for " + source.hashCode());
            //System.out.println("SLExtractor menu number " + SLExtractorMenus.size());
            JMenu extractMenu = (JMenu) SLExtractorMenus.get(source);
            if(source == null || extractMenu == null) {
                extractMenu = new SimpleMenu("Extract with subject locator");
                //refreshExtractWithSLMenu(extractMenu, null, admin);
                refreshExtractWithSLMenu(extractMenu, null, admin);
                SLExtractorMenus.put(source, extractMenu);
            }
            try {
                defaultSLMenuStruct = new Object[] {
                    "Open subject locator...", new OpenSubjectLocator(),
                    "Check subject locator...", new CheckSubjectLocator(),
                    "---",
                    "Make subject locator from a filename...", new MakeSubjectLocatorFromFilename(),
                    "Make subject locator from file content...", new MakeSubjectLocatorFromFileContent(),
                    "Make subject locator from a subject identifier...", new MakeSubjectLocatorFromSubjectIdentifier(),
                    "Make subject locator from a base name...", new MakeSubjectLocatorFromBasename(),
                    "Make subject locator from an occurrence...", new MakeSubjectLocatorFromOccurrence(),
                    "---",
                    // "Find SLs...", new FindSubjectLocator(),
                    // "Find SLs with base names...", new FindSubjectLocatorWithBasename(),
                    // "---",
                    "Modify subject locator with a regex...", new ModifySubjectLocatorWithRegex(),
                    "Remove subject locator...", new DeleteSubjectLocator(),
                    "---",
                    "Download subject locator...", new DownloadSubjectLocators(),
                    "Download and change subject locator...", new DownloadSubjectLocators(true),
                    "Upload subject locator resource to Mediawiki", new MediawikiSubjectLocatorUploader(new ApplicationContext()),
                    "---",
                    extractMenu,
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultSLMenuStruct;
    }
    
    
    private static Object[] defaultSIMenuStruct = null;
    public static Object[] getDefaultSIMenuStruct(Wandora admin, Object source) {
        if(defaultSIMenuStruct == null) {
            try {
                defaultSIMenuStruct = new Object[] {
                    "Open subject identifier...", new OpenSubjectIdentifier(),
                    "Check subject identifiers...", new CheckSubjectIdentifiers(),
                    "---",
                    "Make subject identifier from a file", new MakeSubjectIdentifierFromFilename(),
                    "Make subject identifier from a subject locator", new MakeSubjectIdentifierFromSubjectLocator(),
                    "Make subject identifier from a base name...", new MakeSubjectIdentifierFromBasename(),
                    "Make subject identifier from an occurrence...", new MakeSubjectIdentifierFromOccurrence(),
                    "---",
                    "Copy subject identifiers", new CopySubjectIdentifiers(),
                    "Paste subject identifiers", new PasteSubjectIdentifiers(),
                    "Duplicate subject identifiers...", new DuplicateSubjectIdentifier(),
                    "---",
                    "Remove subject identifiers with a regex...", new DeleteSubjectIdentifiersWithRegex(),
                    "Remove reference-parts in subject identifiers...", new RemoveReferencesInSubjectIdentifiers(),
                    "Modify subject identifiers with a regex...", new ModifySubjectIdentifiersWithRegex(),
                    
                    // "Fix SIs", new SIFixer(),
                    "Fix subject identifiers...", new FixSubjectIdentifiers2(),
                    "Flatten identity...", new FlattenSubjectIdentifiers(),
                    "Expand identity", new Object[] {
                        "Expand with sameAs.org", new SameAsSubjectExpander(),
                        "Expand with sameAs.org store", new Object[] {
                            "Expand with 270a store", new SameAs270aStoreSubjectExpander(),
                            "Expand with British Library store", new SameAsBritishLibraryStoreSubjectExpander(),
                            "Expand with Data Southampton store", new SameAsDataSouthamptonStoreSubjectExpander(),
                            "Expand with Disaster 20 store", new SameAsDisaster20StoreSubjectExpander(),
                            "Expand with Email store", new SameAsEmailStoreSubjectExpander(),
                            "Expand with Freebase store", new SameAsFreebaseStoreSubjectExpander(),
                            "Expand with Kelle store", new SameAsKelleStoreSubjectExpander(),
                            "Expand with LATC store", new SameAsLATCStoreSubjectExpander(),
                            "Expand with Libris store", new SameAsLibrisStoreSubjectExpander(),
                            "Expand with MusicNet store", new SameAsMusicNetStoreSubjectExpander(),
                            "Expand with NoTube store", new SameAsNoTubeStoreSubjectExpander(),
                            "Expand with Ordnance Survey store", new SameAsOrdnanceSurveyStoreSubjectExpander(),
                            "Expand with Pleiades store", new SameAsPleiadesStoreSubjectExpander(),
                            "Expand with Schema.org store", new SameAsSchemaOrgStoreSubjectExpander(),
                            "Expand with Torver Data store", new SameAsTorverDataStoreSubjectExpander(),
                            "Expand with VIAF store", new SameAsVIAFStoreSubjectExpander(),
                            "Expand with Web Index store", new SameAsWebIndexStoreSubjectExpander(),
                        },
                        "Expand with sameAs anywhere", new SameAsAnywhereSubjectExpander(),
                    },
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultSIMenuStruct;
    }
    
    
    
    private static Object[] defaultBasenameMenuStruct = null;
    public static Object[] getDefaultBasenameMenuStruct(Wandora admin, Object source) {
        if(defaultBasenameMenuStruct == null) {
            try {
                defaultBasenameMenuStruct = new Object[] {
                    "Make base name from a subject identifier", new MakeBasenameFromSubjectIdentifier(),
                    "Make base name from an occurrence...", new MakeBasenameFromOccurrence(),
                    "---",
                    "Modify base names with a regex...", new BasenameRegexReplacer(),
                    "Remove new line characters", new BasenameNewlineRemover(),
                    "Collapse white spaces", new BasenameWhiteSpaceCollapser(),
                    "Trim base names", new BasenameTrimmer(),
                    "---",
                    "Remove base names...", new BasenameRemover(),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultBasenameMenuStruct;
    }
    
    
    
    private static Object[] defaultVariantNameMenu = null;
    public static Object[] getDefaultVariantNameMenuStruct(Wandora admin, Object source) {
        if(defaultVariantNameMenu == null) {
            try {
                defaultVariantNameMenu = new Object[] {
                    "Add variant name...", new AddVariantName(),
                    "---",
                    "Make display variants from occurrences", new MakeDisplayVariantsFromOccurrences(), 
                    "Make display variants from base names...", new MakeDisplayVariantsFromBasename(),
                    "Make sort variants from base names...", new MakeSortVariantsFromBasename(),
                    "---",
                    "Copy all variant names to clipboard", new TopicNameCopier(),
                    "---",
                    "Copy variant names to other scope...", new VariantScopeCopier(),
                    "Move variant names to other scope...", new VariantScopeCopier(true),
                    "---",
                    "Translate with Google...", new VariantGoogleTranslate(),
                    "Translate with Microsoft...", new VariantMicrosoftTranslate(),
                    "Translate with Watson...", new VariantWatsonTranslate(),
                    "---",
                    "Modify variant names with a regex...", new VariantRegexReplacer(),
                    "Remove new line characters", new VariantNewlineRemover(),
                    "Collapse white spaces", new VariantWhiteSpaceCollapser(),
                    "---",
                    "Add missing display scope", new AddImplicitDisplayScopeToVariants(),
                    "Add missing sort scope", new AddImplicitSortScopeToVariants(),
                    "Add missing language...", new AddMissingLanguageScope(),
                    "---",
                    "Remove variant names...", new VariantRemover(),
                    "Remove all empty variant names...", new AllEmptyVariantRemover(),
                    "Remove all variant names...", new AllVariantRemover(),
                    "---",
                    "Transform variants to topics...", new VariantsToTopicsAndAssociations(),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultVariantNameMenu;
    }
    
    
    
    
    private static Object[] defaultAssociationMenuStruct = null;
    public static Object[] getDefaultAssociationMenuStruct(Wandora admin, Object source) {
        if(defaultAssociationMenuStruct == null) {
            try {
                defaultAssociationMenuStruct = new Object[] {
                    "Add association...", new AddSchemalessAssociation(),
                    "---",
                    "Make superclass of current topic", new MakeSuperclassOf(),
                    "Make subclass of current topic", new MakeSubclassOf(),
                    "Make class-instances with associations", new MakeInstancesWithAssociations(),
                    "Make associations with class-instances", new MakeAssociationWithClassInstance(),
                    "---",
                    "Make associations from occurrences...", new MakeAssociationWithOccurrence(),
                    "Find associations in occurrences...", new FindAssociationsInOccurrence(),
                    // "---",
                    // "Collect binary associations to n-ary association...", new CollectBinaryToNary(),
                    // "Steal associations...", new StealAssociations(),
                    "---",
                    "Delete associations with type...",new DeleteAssociationsInTopicWithType(),
                    "Delete empty and unary associations...", new DeleteUnaryAssociations(),
                    "Delete all associations...",new DeleteAssociationsInTopic(),                  
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultAssociationMenuStruct;
    }
    
    
    
    
    
    private static Object[] defaultOccurrenceMenuStruct = null;
    public static Object[] getDefaultOccurrenceMenuStruct(Wandora admin, Object source) {
        if(defaultOccurrenceMenuStruct == null) {
            try {
                defaultOccurrenceMenuStruct = new Object[] {
                    "Make occurrences from subject locators...", new MakeOccurrenceFromSubjectLocator(),
                    "Make occurrences from subject identifiers...", new MakeOccurrenceFromSubjectIdentifier(),
                    "Make occurrences from base names...", new MakeOccurrenceFromBasename(),
                    "Make occurrences from variant names...", new MakeOccurrenceFromVariant(),
                    "Make occurrences from all variant names...", new MakeOccurrencesFromVariants(),
                    "Make occurrences from associations...", new MakeOccurrenceFromAssociation(),
                    "---",
                    "Modify occurrences with a regex...", new OccurrenceRegexReplacerOne(),
                    "Modify all occurrences with a regex...", new OccurrenceRegexReplacerAll(),
                    "---",
                    "Copy occurrence to all other scopes...", new SpreadOccurrence(),
                    "Copy occurrences to other scope...", new OccurrenceScopeCopier(),
                    "Move occurrences to other scope...", new OccurrenceScopeCopier(true),
                    "---",
                    "Translate with Google...", new OccurrenceGoogleTranslate(),
                    "Translate with Microsoft...", new OccurrenceMicrosoftTranslate(),
                    "Translate with Watson...", new OccurrenceWatsonTranslate(),
                    "---",
                    "Refine occurrences",
                        new Object[] {
                            "With GATE Annie...", new AnnieOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_gate.png"),
                            "With Stanford NER...", new StanfordNEROccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_stanford_ner.png"),
                            "---",
                            "With uClassify sentiment classifier...", new UClassifyOccurrenceExtractor("Sentiment", "uClassify", 0.001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                            "With uClassify text language classifier...", new UClassifyOccurrenceExtractor("Text Language", "uClassify", 0.001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                            "With uClassify topics classifier...", new UClassifyOccurrenceExtractor("Topics", "uClassify", 0.001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                            "With uClassify mood classifier...", new UClassifyOccurrenceExtractor("Mood", "prfekt", 0.001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                            "With uClassify news classifier...", new UClassifyOccurrenceExtractor("News Classifier", "mvazquez", 0.001), UIBox.getIcon("gui/icons/extract_uclassify.png"),
                            "---",
                            "With Alchemy entity extractor...", new AlchemyEntityOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                            "With Alchemy keyword extractor...", new AlchemyKeywordOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                            "With Alchemy category extractor...", new AlchemyCategoryOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                            "With Alchemy sentiment extractor...", new AlchemySentimentOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                            "With Alchemy relation extractor...", new AlchemyRelationOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_alchemy.png"),
                            "---",
                            "With OpenCalais classifier...", new OpenCalaisOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_opencalais.png"),
                            "With Yahoo! YQL term extractor...", new YahooTermOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_yahoo.png"),
                            "With Zemanta extractor...", new ZemantaOccurrenceExtractor(), UIBox.getIcon("gui/icons/extract_zemanta.png"),
                            "With GeoNames near by extractor...", new FindNearByGeoNamesOccurrence(), UIBox.getIcon("gui/icons/extract_geonames.png"),
                            "---",
                            "Find associations in occurrences....", new FindAssociationsInOccurrenceSimple(),
                            "Find associations in occurrences using a pattern....", new FindAssociationsInOccurrence(),
                            // "---",
                            // "Associate points in polygons occurrence carriers...", new FindPointInPolygonOccurrence(),
                            // "Associate nearby point occurrence carriers...", new AssociateNearByOccurrenceCarriers(),
                        },
                    "---",
                    "Check URL occurrences...", new URLOccurrenceChecker(),
                    "Download URL occurrences...", new DownloadOccurrence(),
                    "Download all URL occurrences...", new DownloadAllOccurrences(),
                    "---",
                    "Upload to Pastebin...", new PasteBinOccurrenceUploader(),
                    "Download from Pastebin", new PasteBinOccurrenceDownloader(),
                    "---",
                    "Upload URL resource to MediaWiki", new MediawikiOccurrenceUploader(),
                    "Upload content to MediaWiki...", new MediaWikiAPIUploader(),
                    
                    "---",
                    "Delete occurrences with type...", new DeleteOccurrence(),
                    "Delete all occurrences...", new DeleteFromTopics(DeleteFromTopics.DELETE_TEXTDATAS),
                };
            }
            catch(Exception e) {
                if(admin != null) admin.handleError(e);
                else e.printStackTrace();
            }
        }
        return defaultOccurrenceMenuStruct;
    }
    
    

    public static Object[] getDefaultTopicMenuStruct(Wandora admin, Object source) {
        /* Notice: topicMenuStruct can NOT be cached as source parameter is used
         * to build the structure!!!
         */
        Object[] topicMenuStruct = null;
        try {
            topicMenuStruct = new Object[] {
                "New topic...", new NewTopicExtended(),
                "Duplicate topics...", new DuplicateTopics(),
                "Merge topics...", new MergeTopics(),
                "Split topics", new Object[] {
                    "Split topic with subject identifiers", new SplitTopics(),
                    "Split topic with a base name regex...", new SplitTopicsWithBasename(),
                    "---",
                    "Split to descending instances with a base name regex...", new SplitToInstancesWithBasename(true),
                    "Split to ascending instances with a base name regex...", new SplitToInstancesWithBasename(false),
                    "---",
                    "Split to descending superclasses with a base name regex...", new SplitToSuperclassesWithBasename(true),
                    "Split to ascending superclasses with a base name regex...", new SplitToSuperclassesWithBasename(false),
                },
                "Delete topics", getDefaultDeleteTopicMenuStruct(admin, source),
                "---",
                "Add to topic", getDefaultAddToTopicMenuStruct(admin, source),
                "Delete from topics", getDefaultDeleteFromTopicMenuStruct(admin, source),
                "---",
                "Copy", getDefaultCopyMenuStruct(admin, source),
                "Copy also", getDefaultCopyAlsoMenuStruct(admin, source),
                "Copy to layer", getDefaultCopyToLayerMenuStruct(admin, source),
                "Paste", getDefaultPasteMenuStruct(admin, source),
                "Paste also", getDefaultPasteAlsoMenuStruct(admin, source),
                "Export", new Object[] {
                    "Export selection as topic map...", new ExportTopicMap(true), UIBox.getIcon("gui/icons/export_topicmap.png"),
                    "Export selection to Maiana...", new MaianaExport(true), UIBox.getIcon("gui/icons/export_maiana.png"),
                    "Export selection as RDF N3...", new RDFExport(true), UIBox.getIcon("gui/icons/export_rdf.png"),
                    "---",
                    "Export selection as DOT graph...", new DOTExport(true), UIBox.getIcon("gui/icons/export_graph.png"),
                    "Export selection as GML graph...", new GMLExport(true), UIBox.getIcon("gui/icons/export_graph.png"),
                    "Export selection as GraphML graph...", new GraphMLExport(true), UIBox.getIcon("gui/icons/export_graph.png"),
                    "Export selection as GraphXML graph...", new GraphXMLExport(true), UIBox.getIcon("gui/icons/export_graph.png"),
                    "Export selection as GXL graph...", new GXLExport(true), UIBox.getIcon("gui/icons/export_graph.png"),
                    "Export selection as Gephi graph...", new GephiExport(true), UIBox.getIcon("gui/icons/export_graph.png"),
                    "---",
                    "Export selection as adjacency matrix...", new AdjacencyMatrixExport(true), UIBox.getIcon("gui/icons/export_adjacency_matrix.png"),
                    "Export selection as similarity matrix...", new SimilarityMatrixExport(true), UIBox.getIcon("gui/icons/export_similarity_matrix.png"),
                    "Export selection as incidence matrix...", new IncidenceMatrixExport(true), UIBox.getIcon("gui/icons/export_incidence_matrix.png"),
                    "---",
                    "Export selection as a HTML page collection...", new ExportSite(true), UIBox.getIcon("gui/icons/export_site.png"),
                },
                "---",
                "Subject locators", getDefaultSLMenuStruct(admin, source),
                "Subject identifiers", getDefaultSIMenuStruct(admin, source),
                "Base names", getDefaultBasenameMenuStruct(admin, source),
                "Variant names", getDefaultVariantNameMenuStruct(admin, source),
                "Associations", getDefaultAssociationMenuStruct(admin, source),
                "Occurrences", getDefaultOccurrenceMenuStruct(admin, source),
                "---",
                "Statistics", new Object[] {
                    "Asset weights...", UIBox.getIcon("gui/icons/asset_weight.png"), new AssetWeights(),
                },
            };
        }
        catch(Exception e) {
            if(admin != null) admin.handleError(e);
            else e.printStackTrace();
        }
        return topicMenuStruct;
    }
    
    
    
    public static void refreshExtractWithSLMenu(JMenu m, Context proposedContext, Wandora admin) {
        m.removeAll();
        UIBox.attachMenu(m, getSubjectLocatorExtractorMenu(admin, proposedContext), admin);
    }
    
    
    public static Object[] getSubjectLocatorExtractorMenu(Wandora admin) {
        return getSubjectLocatorExtractorMenu(admin, null);
    }
    public static Object[] getSubjectLocatorExtractorMenu(Wandora admin, Context proposedContext) {
        return getSubjectLocatorExtractorMenu(admin, proposedContext, admin.toolManager.getToolSet("extract"));
    }
    
    public static Object[] getSubjectLocatorExtractorMenu(Wandora admin, Context proposedContext, WandoraToolSet extractTools) {
        final Context context = proposedContext;
        if(extractTools == null) {
            return new Object[] {};
        } 
        else {
            return extractTools.getAsObjectArray(
                    extractTools.new ToolFilter() {
                        @Override
                        public boolean acceptTool(WandoraTool tool) {
                            return (tool instanceof AbstractExtractor);
                        }
                        @Override
                        public WandoraTool polishTool(WandoraTool tool) {
                            ExtractWithSubjectLocator wrapper = null;
                            if(context != null) {
                                wrapper = new ExtractWithSubjectLocator((AbstractExtractor) tool, context);
                            }
                            else {
                                wrapper = new ExtractWithSubjectLocator((AbstractExtractor) tool);
                            }
                            return wrapper;
                        }
                        @Override
                        public Object[] addAfterTool(WandoraTool tool) {
                            return new Object[] { tool.getIcon() };
                        }
                    }
            );
        }
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    private static Object[] associationsPopupStruct = null;
    public static Object[] getAssociationsPopupStruct() {
        if(associationsPopupStruct == null) {
            associationsPopupStruct = new Object[] {
                "---",
                "Copy associations", new Object[] {
                    "Copy associations as Wandora layout tab text", new CopyAssociations(),
                    "Copy associations as Wandora layout HTML", new CopyAssociations(CopyAssociations.HTML_OUTPUT),
                    "---",
                    "Copy associations as LTM layout tab text", new CopyAssociations(CopyAssociations.TABTEXT_OUTPUT, CopyAssociations.LTM_LAYOUT),
                    "Copy associations as LTM layout HTML", new CopyAssociations(CopyAssociations.HTML_OUTPUT, CopyAssociations.LTM_LAYOUT),
                },

                // "Add associations...", new AddAssociations(new ApplicationContext()),
                "Edit association...", new ModifySchemalessAssociation(),
                "Delete associations...", new DeleteAssociations(),
                // "Duplicate associations...",
                "---",
                "Change association type...", new ChangeAssociationType(),
                "Change association role...", new ChangeAssociationRole(),
                // "Change association roles...", new ChangeAssociationRoles(),
                "Insert player to associations...", new InsertPlayer(),
                "Delete players in associations...", new RemovePlayer(),
                "Merge players in associations...", new MergePlayers(),
                "---",
                "Swap players within associations", new SwapPlayers(),
                "Create symmetric associations", new CreateSymmetricAssociation(),
                "Delete symmetric associations", new DeleteSymmetricAssociation(),
                "---",
                "Collect to n-ary association...", new CollectNary(),
                // "Collect binary to n-ary", new CollectBinaryToNary(),
                "Split n-ary to binary associations...", new SplitToBinaryAssociations(),
                "Transpose associations...", new TransposeAssociations(),
                "---",
                "Open edge of associations", new OpenEdgeTopic(),
                "Copy path to the edge of associations", new CopyEdgePath(),
                "Detect cycle...", new DetectCycles(),
                "---",
                "SOM classifier...", new SOMClassifier(new AssociationContext()), 
                /*
                 "---",
                 "Cut associations", new Object[] {
                    "Copy all associations as tab text", new CopyAssociations(),
                    "Copy all associations as HTML", new CopyAssociations(CopyAssociations.HTML_OUTPUT),
                },
                 **/

            };
        }
        return associationsPopupStruct;

    }




    // -------------------------------------------------------------------------


    
    private static Object[] layerTreeMenuStructure = null;
    public static Object[] getLayerTreeMenu() {
        if(layerTreeMenuStructure == null) {
            layerTreeMenuStructure = new Object[] {
                "New layer...", new NewLayer(),
                "---",
                //"Rename layer...", new RenameLayer(),
                "Configure layer...", new ConfigureLayer(),
                "Delete layer...", new DeleteLayer(),
                "Clear layer topic map...", new ClearTopicMap(),
                "Clear topic map indexes...", new ClearTopicMapIndexes(),
                "---",
                "Make association consistent...",new MakeAssociationConsistentTool(),
                "___TOPICMAPMENU___", // topic map implementation specific menu gets inserted here, this item must be on top level in the menu
                "---",
                "Merge layers", new Object[] {
                    "Merge up...", new MergeLayers(MergeLayers.MERGE_UP),
                    "Merge down...", new MergeLayers(MergeLayers.MERGE_DOWN),
                    "Merge all...", new MergeLayers(MergeLayers.MERGE_ALL),
                    "Merge visible...", new MergeLayers(MergeLayers.MERGE_VISIBLE),
                },
                "___IMPORTMENU___",
                "___GENERATEMENU___",
                "___EXPORTMENU___",
                "---",
                "Arrange", new Object[] {
                    "Move up", new ArrangeLayers(LayerTree.MOVE_LAYER_UP),
                    "Move down", new ArrangeLayers(LayerTree.MOVE_LAYER_DOWN),
                    "Move top", new ArrangeLayers(LayerTree.MOVE_LAYER_TOP),
                    "Move bottom", new ArrangeLayers(LayerTree.MOVE_LAYER_BOTTOM),
                    "---",
                    "Reverse order", new ArrangeLayers(LayerTree.REVERSE_LAYERS),
                },
                "View",new Object[] {
                    "View all", new ViewLayers(ViewLayers.VIEW_ALL),
                    "Hide all", new ViewLayers(ViewLayers.HIDE_ALL),
                    "Hide all but current", new ViewLayers(ViewLayers.HIDE_ALL_BUT_CURRENT),
                    "Reverse visibility", new ViewLayers(ViewLayers.REVERSE_VISIBILITY),
                },
                "Lock",new Object[] {
                    "Lock all", new LockLayers(LockLayers.LOCK_ALL),
                    "Unlock all", new LockLayers(LockLayers.UNLOCK_ALL),
                    "Lock all but current", new LockLayers(LockLayers.LOCK_ALL_BUT_CURRENT),
                    "Reverse locks", new LockLayers(LockLayers.REVERSE_LOCKS),
                },
                "---",
                "Topics", new Object[] {
                    "Delete topics",new Object[] {
                        "Delete topics without A+C+I...", new DeleteTopicsWithoutACI(),
                        "Delete topics without A+I...", new DeleteTopicsWithoutAI(),
                        "Delete topics without associations...", new DeleteTopicsWithoutAssociations(),
                        "Delete topics without classes...", new DeleteTopicsWithoutClasses(),
                        "Delete topics without instances...", new DeleteTopicsWithoutInstances(),
                        "Delete topics without base names...", new DeleteTopicsWithoutBasename(),
                        "Delete topics without occurrences...", new DeleteTopicsWithoutOccurrences(),
                        "---",
                        "Delete topics with SI regex...", new DeleteTopicsWithSIRegex(),
                        "Delete topics with base name regex...", new DeleteTopicsWithBasenameRegex(),
                    },
                    "---",
                    "Subject locators", new Object[] {
                        "Make subject locator from a subject identifier...", new MakeSubjectLocatorFromSubjectIdentifier(),
                        "Make subject locator from a base name...", new MakeSubjectLocatorFromBasename(),
                        "Make subject locator from an occurrence...", new MakeSubjectLocatorFromOccurrence(),
                        "---",
                        "Check subject locators...", new CheckSubjectLocator(),
                        "---",
                        "Modify subject locators with a regex...", new ModifySubjectLocatorWithRegex(),
                        "Remove subject locators...", new DeleteSubjectLocator(),
                        "---",
                        "Download subject locators...", new DownloadSubjectLocators(),
                        "Download and change subject locators...", new DownloadSubjectLocators(true),
                        // "---",
                        // "Find SLs...", new FindSubjectLocator(),
                        // "Find SLs with base names...", new FindSubjectLocatorWithBasename(),
                        "Upload subject locator resources to Mediawiki", new MediawikiSubjectLocatorUploader(),
                    },
                    "Subject identifiers", new Object[] {
                        "Make subject identifier from a subject locator", new MakeSubjectIdentifierFromSubjectLocator(),
                        "Make subject identifier from a base name...", new MakeSubjectIdentifierFromBasename(),
                        "Make subject identifier from an occurrence...", new MakeSubjectIdentifierFromOccurrence(),
                        "---",
                        "Check subject identifiers...", new CheckSubjectIdentifiers(),
                        "Modify subject identifiers with a regex...", new ModifySubjectIdentifiersWithRegex(),
                        // "Fix SIs", new SIFixer(),
                        "Fix subject identifiers", new FixSubjectIdentifiers2(),
                        "Remove references in subject identifiers...", new RemoveReferencesInSubjectIdentifiers(),
                        "---",
                        "Flatten indentity...", new FlattenSubjectIdentifiers(),
                        "Remove subject identifiers with a regex...", new DeleteSubjectIdentifiersWithRegex(),
                    },
                    "Base names", new Object[] {
                        "Make base name from a subject identifier", new MakeBasenameFromSubjectIdentifier(),
                        "Make base name from an occurrence...", new MakeBasenameFromOccurrence(),
                        "---",
                        "Modify base names with a regex...", new BasenameRegexReplacer(),
                        "Remove new line characters", new BasenameNewlineRemover(),
                        "Collapse white spaces", new BasenameWhiteSpaceCollapser(),
                        "Trim base names", new BasenameTrimmer(),
                        "---",
                        "Remove base names...", new BasenameRemover(),
                    },
                    "Variant names", new Object[] {
                        "Add variant name...", new AddVariantName(),
                        "---",
                        "Make display variants from occurrences", new MakeDisplayVariantsFromOccurrences(),
                        "Make display variants from base names...", new MakeDisplayVariantsFromBasename(),
                        "Make sort variants from base names", new MakeSortVariantsFromBasename(),
                        "---",
                        "Modify variant names with a regex...", new VariantRegexReplacer(),
                        "Remove new line characters", new VariantNewlineRemover(),
                        "---",
                        "Copy variant names to other scope...", new VariantScopeCopier(),
                        "Move variant names to other scope...", new VariantScopeCopier(true),
                        "---",
                        "Translate variant names with Google...", new VariantGoogleTranslate(),
                        "Translate variant names with Microsoft...", new VariantMicrosoftTranslate(),
                        "Translate variant names with Watson...", new VariantWatsonTranslate(),
                        "---",
                        "Remove variant name...", new VariantRemover(),
                        "Remove all empty variant names...", new AllEmptyVariantRemover(),
                        "Remove all variant names...", new AllVariantRemover(),
                        "---",
                        "Add missing display scope to variant names", new AddImplicitDisplayScopeToVariants(),
                        "Add missing sort scope to variant names", new AddImplicitSortScopeToVariants(),
                        "Add missing language to variant names...", new AddMissingLanguageScope(),
                        "---",
                        "Transform variant names to topics...", new VariantsToTopicsAndAssociations(),
                    },
                    "Associations", new Object[] {
                        "Make associations from occurrences...", new MakeAssociationWithOccurrence(),
                        "Find associations in occurrences...", new FindAssociationsInOccurrence(),
                        "---",
                        "Delete associations with type...",new DeleteAssociationsInTopicWithType(),
                        "Delete empty and unary associations...", new DeleteUnaryAssociations(),
                        "Delete all associations...",new DeleteAssociationsInTopic(new LayeredTopicContext()),
                    },
                    "Occurrences", new Object[] {
                        "Delete occurrences with a type...", new DeleteOccurrence(),
                        "Delete all occurrences...", new DeleteFromTopics(DeleteFromTopics.DELETE_TEXTDATAS),
                        "---",
                        "Modify occurrences with a regex...", new OccurrenceRegexReplacerAll(),
                        "---",
                        "Copy occurrences to other scope...", new OccurrenceScopeCopier(),
                        "Move occurrences to other scope...", new OccurrenceScopeCopier(true),
                        "---",
                        "Translate occurrences with Google...", new OccurrenceGoogleTranslate(),
                        "Translate occurrences with Microsoft...", new OccurrenceMicrosoftTranslate(),
                        "Translate occurrences with Watson...", new OccurrenceWatsonTranslate(),
                        "---",
                        "Check URL occurrences...", new URLOccurrenceChecker(),
                        "Download URL occurrences...", new DownloadOccurrence(),
                        "Download all URL occurrences...", new DownloadAllOccurrences(),
                        "---",
                        "Upload occurrences to Pastebin...", new PasteBinOccurrenceUploader(),
                        "Download occurrences from Pastebin", new PasteBinOccurrenceDownloader(),
                        "---",
                        "Upload occurrences to Mediawiki...", new MediawikiOccurrenceUploader(),
                        "Upload content to MediaWiki...", new MediaWikiAPIUploader()
                    }



                },
                "---",
                "Statistics", new Object[] {
                    "Layer info...", UIBox.getIcon("gui/icons/layer_info.png"), new TopicMapStatistics(),
                    "Layer connection statistics...", UIBox.getIcon("gui/icons/layer_acount.png"), new AssociationCounterTool(),
                    "Asset weights...", UIBox.getIcon("gui/icons/asset_weight.png"), new AssetWeights(AssetWeights.CONTEXT_IS_TOPICMAP),
                    "Topic map diameter...", UIBox.getIcon("gui/icons/topicmap_diameter.png"), new TopicMapDiameter(),
                    "Average clustering coefficient...", UIBox.getIcon("gui/icons/clustering_coefficient.png"), new AverageClusteringCoefficient(),
                    "---",
                    "Occurrence summary report...", UIBox.getIcon("gui/icons/summary_report.png"), new OccurrenceSummaryReport(),
                }

                //        "---",
                //        "Debug tool",new TestTool(),
            };
        }

        return layerTreeMenuStructure;

    }




    public static Object[] getSimpleLayerTreeMenu() {
        Object[] simpleMenuStructure = new Object[] {
            "New layer...", new NewLayer(),
        };
        return simpleMenuStructure;
    }
    
    
    
    
    
    private static Object[] logoMenuStructure = null;
    public static Object[] getLogoMenu() {
        if(logoMenuStructure == null) {
            logoMenuStructure = new Object[] {
               "Wandora home", UIBox.getIcon("gui/icons/open_browser.png"), KeyStroke.getKeyStroke(VK_H, CTRL_MASK), new ExecBrowser("http://www.wandora.org"),
               "---",
               "Documentation", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://wandora.org/wiki/Documentation"),
               "Discussion forum", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://wandora.org/forum/"),
               "WandoraTV", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://wandora.org/tv/"),
               "---",
               "About Wandora...", UIBox.getIcon("gui/icons/info.png"),new AboutWandora(),
               "Wandora credits...", UIBox.getIcon("gui/icons/info.png"),new AboutCredits(),
            };
        }
        return logoMenuStructure;
    }
    
    
    
    
    public static Object[] getOccurrenceTableMenu(OccurrenceTable ot, Options options) {
        String viewType = OccurrenceTable.VIEW_SCHEMA;
        int rowHeight = 1;
        try {
            if(options != null) {
                viewType = options.get(OccurrenceTable.VIEW_OPTIONS_KEY, OccurrenceTable.VIEW_SCHEMA);
                rowHeight = options.getInt(OccurrenceTable.ROW_HEIGHT_OPTIONS_KEY, rowHeight);
            }
        }
        catch(Exception e) {}
        
        return new Object[] {
            "Cut occurrence", new CutOccurrence(),
            "Copy occurrence", new CopyOccurrence(), 
            "Paste occurrence", new PasteOccurrence(),
            "---",
            "Append occurrence", new AppendOccurrence(), 
            "Spread occurrence", new SpreadOccurrence(),
            "---",
            "Duplicate occurrence...", new DuplicateOccurrence(),
            "Change type of the occurrence...", new ChangeOccurrenceType(),
            "Delete occurrence...", new DeleteOccurrence(),
            "---",
            "Open URL occurrence...", new OpenURLOccurrence(),
            "Download URL to the occurrence", new DownloadOccurrence(DownloadOccurrence.TARGET_OCCURRENCE),
            "Save URL to a file...", new DownloadOccurrence(DownloadOccurrence.TARGET_FILE),
            "---",
            "Upload occurrence to Pastebin...", new PasteBinOccurrenceUploader(),
            "Download occurrence from Pastebin", new PasteBinOccurrenceDownloader(),
            "---",
            "Upload URL occurrence resource to Mediawiki...", new MediawikiOccurrenceUploader(),
            "Upload content to MediaWiki...", new MediaWikiAPIUploader(),
            "---",
            "Translate occurrence with Google...", new OccurrenceGoogleTranslate(),
            "Translate occurrence with Microsoft...", new OccurrenceMicrosoftTranslate(),
            "Translate occurrence with Watson...", new OccurrenceWatsonTranslate(),
            "---",
            "Run occurrence", new Object[] {
                "Run as Query", new RunOccurrenceAsQuery(),
                "Run as TMQL", new RunOccurrenceAsTMQL(),
            },
            "---",
            "View", new Object[] {
                "View schema scopes", new ChangeOccurrenceView(OccurrenceTable.VIEW_SCHEMA, options), (OccurrenceTable.VIEW_SCHEMA.equals(viewType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View used scopes", new ChangeOccurrenceView(OccurrenceTable.VIEW_USED, options), (OccurrenceTable.VIEW_USED.equals(viewType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View schema and used scopes", new ChangeOccurrenceView(OccurrenceTable.VIEW_USED_AND_SCHEMA, options), (OccurrenceTable.VIEW_USED_AND_SCHEMA.equals(viewType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "---",
                "View single row", new ChangeOccurrenceTableRowHeight(1, options), (rowHeight == 1 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View 5 rows", new ChangeOccurrenceTableRowHeight(5, options), (rowHeight == 5 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View 10 rows", new ChangeOccurrenceTableRowHeight(10, options), (rowHeight == 10 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View 20 rows", new ChangeOccurrenceTableRowHeight(20, options), (rowHeight == 20 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
            },
        };
    }
    

    public static Object[] getOccurrenceTypeLabelPopupStruct(Topic occurrenceType, Topic topic) {
        return new Object[] {
            "Open topic", new OpenTopic(),
            // "Open topic in", getOpenInMenu(),
            "---",
            "Edit occurrences", new EditOccurrences(occurrenceType, topic),
            "Duplicate occurrences...", new DuplicateOccurrence(occurrenceType, topic),
            "Change occurrence type...", new ChangeOccurrenceType(occurrenceType, topic),
            "Delete occurrences...", new DeleteOccurrence(occurrenceType, topic),
        };
    }
    
    
    
    
    public static Object[] getTreeMenu(Wandora wandora, TopicTree tree) {
        return new Object[] {
            "Open topic", new OpenTopic(),
            "Open topic in",
            getOpenInMenu(),
            "---",
            "New topic...", new NewTopicExtended(),
            "Delete topic...", new DeleteTopics(),
            "Duplicate topic...", new DuplicateTopics(),
            "Split topic", new Object[] {
                "Split topic with subject identifiers", new SplitTopics(), 
                "Split topic with a base name regex...", new SplitTopicsWithBasename(),
                "---",
                "Split to descending instances with a base name regex...", new SplitToInstancesWithBasename(true),
                "Split to ascending instances with a base name regex...", new SplitToInstancesWithBasename(false),
                "---",
                "Split to descending superclasses with a base name regex...", new SplitToSuperclassesWithBasename(true),
                "Split to ascending superclasses with a base name regex...", new SplitToSuperclassesWithBasename(false),
            },
            "---",
            "Add to topic", getDefaultAddToTopicMenuStruct(wandora, tree),
            "Delete from topic", getDefaultDeleteFromTopicMenuStruct(wandora, tree),
            "---",
            "Copy", getDefaultCopyMenuStruct(wandora, tree),
            "Copy also", getDefaultCopyAlsoMenuStruct(wandora, tree),
            "Copy to layer", getDefaultCopyToLayerMenuStruct(wandora, tree),
            "Paste", getDefaultPasteMenuStruct(wandora, tree),
            "Paste also", getDefaultPasteAlsoMenuStruct(wandora, tree),
            "Export", new Object[] {
                "Export selection as topic map...", new ExportTopicMap(true),
                "Export selection as RDF N3...", new RDFExport(true),
                "---",
                "Export selection as GML graph...", new GMLExport(true),
                "Export selection as GraphML graph...", new GraphMLExport(true),
                "Export selection as GraphXML graph...", new GraphXMLExport(true),
                "Export selection as GXL graph...", new GXLExport(true),
                "---",
                "Export selection as adjacency matrix...", new AdjacencyMatrixExport(true),
                "Export selection as incidence matrix...", new IncidenceMatrixExport(true),
                "---",
                "Export selection as a HTML page collection...", new ExportSite(true),
            },
            "---",
            "Subject locator", getDefaultSLMenuStruct(wandora, tree),
            "Subject identifiers", getDefaultSIMenuStruct(wandora, tree),
            "Base name", getDefaultBasenameMenuStruct(wandora, tree),
            "Variant names", getDefaultVariantNameMenuStruct(wandora, tree),
            "Associations", getDefaultAssociationMenuStruct(wandora, tree),
            "Occurrences", getDefaultOccurrenceMenuStruct(wandora, tree),
            "---",
            "Refresh tree", new RefreshTopicTrees(),
        };
    }

    
    
    
    
    public static Object[] getOpenInMenu() {
        Wandora wandora = Wandora.getWandora();
        ArrayList struct = new ArrayList();
        
        if(wandora != null) {
            HashMap<Dockable,TopicPanel> dockedTopicPanels = wandora.topicPanelManager.getDockedTopicPanels();
            if(dockedTopicPanels != null && !dockedTopicPanels.isEmpty()) {
                for(Dockable dockable : dockedTopicPanels.keySet()) {
                    TopicPanel tp = dockedTopicPanels.get(dockable);
                    if(tp != null && tp.supportsOpenTopic()) {
                        struct.add( tp.getName() + DockingFramePanel.getAdditionalLabel(tp) );
                        struct.add( tp.getIcon() );
                        struct.add( new OpenTopicIn(tp) );
                    }
                }
            }
            else {
                struct.add("[No panels open]");
            }
            struct.add("---");
            ArrayList<ArrayList> availableTopicPanels = wandora.topicPanelManager.getAvailableTopicPanelsSupportingOpenTopic();
            for(ArrayList panelData : availableTopicPanels) {
                try {
                    Class panelClass = Class.forName((String) panelData.get(0));
                    if(!DockingFramePanel.class.equals(panelClass)) {
                        struct.add( "New " + (String) panelData.get(1) );
                        struct.add( (Icon) panelData.get(2) );
                        struct.add( new OpenTopicInNew( panelClass ) );
                    }
                }
                catch(Exception e) {}

            }
            struct.add( "---" );
            struct.add("Locate in topic tree");
            struct.add(UIBox.getIcon("gui/icons/locate_in_tree.png"));
            struct.add(new LocateSelectTopicInTree());
        }
        return struct.toArray( new Object[] {} );
    }
    
    
    
    
    public static Object[] getSubjectIdentifierTablePopupStruct() {
        return new Object[] {
            "Open...", new OpenSubjectIdentifier(new SIContext()),
            "Add...", new AddSubjectIdentifier(),
            "---",
            "Select all", new SelectAll(),
            "Deselect", new ClearSelection(),
            "Invert selection", new InvertSelection(),
            "---",
            "Copy", new CopySubjectIdentifiers(new SIContext()),
            "Paste", new PasteSubjectIdentifiers(new SIContext()),
            "---",
            "Duplicate", new DuplicateSubjectIdentifier(new SIContext()),
            "Remove...", new DeleteSubjectIdentifiers(new SIContext()),
            "Remove with regex...", new DeleteSubjectIdentifiersWithRegex(new SIContext()),
            "Flatten...", new FlattenSubjectIdentifiers(),
            "Expand", new Object[] {
                "Expand with sameAs.org", new SameAsSubjectExpander(new SIContext()),
                "Expand with sameAs.org store", new Object[] {
                    "Expand with 270a store", new SameAs270aStoreSubjectExpander(new SIContext()),
                    "Expand with British Library store", new SameAsBritishLibraryStoreSubjectExpander(new SIContext()),
                    "Expand with Data Southampton store", new SameAsDataSouthamptonStoreSubjectExpander(new SIContext()),
                    "Expand with Disaster 20 store", new SameAsDisaster20StoreSubjectExpander(new SIContext()),
                    "Expand with Email store", new SameAsEmailStoreSubjectExpander(new SIContext()),
                    "Expand with Freebase store", new SameAsFreebaseStoreSubjectExpander(new SIContext()),
                    "Expand with Kelle store", new SameAsKelleStoreSubjectExpander(new SIContext()),
                    "Expand with LATC store", new SameAsLATCStoreSubjectExpander(new SIContext()),
                    "Expand with Libris store", new SameAsLibrisStoreSubjectExpander(new SIContext()),
                    "Expand with MusicNet store", new SameAsMusicNetStoreSubjectExpander(new SIContext()),
                    "Expand with NoTube store", new SameAsNoTubeStoreSubjectExpander(new SIContext()),
                    "Expand with Ordnance Survey store", new SameAsOrdnanceSurveyStoreSubjectExpander(new SIContext()),
                    "Expand with Pleiades store", new SameAsPleiadesStoreSubjectExpander(new SIContext()),
                    "Expand with Schema.org store", new SameAsSchemaOrgStoreSubjectExpander(new SIContext()),
                    "Expand with Torver Data store", new SameAsTorverDataStoreSubjectExpander(new SIContext()),
                    "Expand with VIAF store", new SameAsVIAFStoreSubjectExpander(new SIContext()),
                    "Expand with Web Index store", new SameAsWebIndexStoreSubjectExpander(new SIContext()),
                },
                "Expand with sameAs anywhere", new SameAsAnywhereSubjectExpander(new SIContext()),
            },
            "---",
            "Copy to subject locator", new MakeSubjectLocatorFromSubjectIdentifier(new SIContext()),
        };
    }
    
    
    
    public static Object[] getSubjectIdentifierLabelPopupStruct() {
        return new Object[] {
            "Add...", new AddSubjectIdentifier(new ApplicationContext()),
            "Copy", new CopySubjectIdentifiers(new ApplicationContext()),
            "Paste", new PasteSubjectIdentifiers(new ApplicationContext()),
            "---",
            "Flatten...", new FlattenSubjectIdentifiers(new ApplicationContext()),
            "Expand", new Object[] {
                "Expand with sameAs.org", new SameAsSubjectExpander(new ApplicationContext()),
                "Expand with sameAs.org store", new Object[] {
                    "Expand with 270a store", new SameAs270aStoreSubjectExpander(new ApplicationContext()),
                    "Expand with British Library store", new SameAsBritishLibraryStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Data Southampton store", new SameAsDataSouthamptonStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Disaster 20 store", new SameAsDisaster20StoreSubjectExpander(new ApplicationContext()),
                    "Expand with Email store", new SameAsEmailStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Freebase store", new SameAsFreebaseStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Kelle store", new SameAsKelleStoreSubjectExpander(new ApplicationContext()),
                    "Expand with LATC store", new SameAsLATCStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Libris store", new SameAsLibrisStoreSubjectExpander(new ApplicationContext()),
                    "Expand with MusicNet store", new SameAsMusicNetStoreSubjectExpander(new ApplicationContext()),
                    "Expand with NoTube store", new SameAsNoTubeStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Ordnance Survey store", new SameAsOrdnanceSurveyStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Pleiades store", new SameAsPleiadesStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Schema.org store", new SameAsSchemaOrgStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Torver Data store", new SameAsTorverDataStoreSubjectExpander(new ApplicationContext()),
                    "Expand with VIAF store", new SameAsVIAFStoreSubjectExpander(new ApplicationContext()),
                    "Expand with Web Index store", new SameAsWebIndexStoreSubjectExpander(new ApplicationContext()),
                },
                "Expand with sameAs anywhere", new SameAsAnywhereSubjectExpander(new ApplicationContext()),
            }  
        };
    }
    
    public static Object[] getSubjectLocatorLabelPopupStruct() {
        return new Object[] {
            "Open...", new OpenSubjectLocator(new ApplicationContext()),
            "---",
            "Copy from filename...", new MakeSubjectLocatorFromFilename(new ApplicationContext()),
            "Copy from file content...", new MakeSubjectLocatorFromFileContent(new ApplicationContext()),
            "Copy from subject identifier", new MakeSubjectLocatorFromSubjectIdentifier(new ApplicationContext()),
            "Copy from basename...", new MakeSubjectLocatorFromBasename(new ApplicationContext()),
            "Copy from occurrence...", new MakeSubjectLocatorFromOccurrence(new ApplicationContext()),
            "---",
            "Copy to subject identifier", new MakeSubjectIdentifierFromSubjectLocator(new ApplicationContext()),
            "Copy to occurrence", new MakeOccurrenceFromSubjectLocator(new ApplicationContext()),
            "---", 
            "Remove...", new DeleteSubjectLocator(new ApplicationContext()),
            "---",
            "Check...", new CheckSubjectLocator(new ApplicationContext()),
            "Download...", new DownloadSubjectLocators(new ApplicationContext()),
            "Download and change...", new DownloadSubjectLocators(new ApplicationContext(), true),
            //"Move to fileserver", new MoveSubjectLocators(new ApplicationContext()),
                    // new ContextToolWrapper(
                    // parent.getToolManager().getConfigurableTool(MoveSubjectLocators.class,"movesl","Move SL to fileserver"),
                    // new ApplicationContext()),
            "Upload to Mediawiki...", new MediawikiSubjectLocatorUploader(new ApplicationContext()),
            "---",
            "Extract", WandoraMenuManager.getSubjectLocatorExtractorMenu(Wandora.getWandora(), new ApplicationContext()),
        };
    }
    
    
    
    
    public static Object[] getInstancesTablePopupStruct() {
        return new Object[] {
            "Add instance...", new AddInstance(new ApplicationContext()),
            "Paste instances", new Object[] {
                "Paste instances as base names...", new PasteInstances(new ApplicationContext()),
                "Paste instances as subject identifiers...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_NOTHING, PasteInstances.PASTE_SIS),
                "---",
                "Paste instances with names...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_NAMES),
                "Paste instances with subject locators...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_SLS),
                "Paste instances with subject identifiers...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_SIS),
                "Paste instances with classes...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_CLASSES),
                "Paste instances with instances...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_INSTANCES),
                "Paste instances with players...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_PLAYERS),
                "Paste instances with occurrences...", new PasteInstances(new ApplicationContext(), PasteInstances.INCLUDE_TEXTDATAS),
            },
        };
    }
    
    
    
    public static Object[] getClassesTablePopupStruct() {
        return new Object[] {
            "Add class...", new AddClass(new ApplicationContext()),
            "Paste classes", new Object[] {
                "Paste classes as base names...", new PasteClasses(new ApplicationContext()),
                "Paste classes as subject identifiers...", new PasteClasses(new ApplicationContext(), PasteClasses.INCLUDE_NOTHING, PasteInstances.PASTE_SIS),
                "---",
                "Paste classes with names...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_NAMES),
                "Paste classes with subject lcoators...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_SLS),
                "Paste classes with subject identifiers...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_SIS),
                "Paste classes with classes...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_CLASSES),
                "Paste classes with instances...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_INSTANCES),
                "Paste classes with players...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_PLAYERS),
                "Paste classes with occurrences...", new PasteClasses(new ApplicationContext(), PasteInstances.INCLUDE_TEXTDATAS),
            },
        };
    }
    
    
    public static Object[] getVariantsLabelPopupStruct(Options options) {
        String variantGUIType = VARIANT_GUITYPE_SCHEMA;
        try {
            variantGUIType = options.get(VARIANT_GUITYPE_OPTIONS_KEY, variantGUIType);
        }
        catch(Exception e) {}
        return new Object[] {
            "Add variant name...", new AddVariantName(new ApplicationContext()),
            "---",
            "Add missing display scope", new AddImplicitDisplayScopeToVariants(new ApplicationContext()),
            "Add missing sort scope", new AddImplicitSortScopeToVariants(new ApplicationContext()),
            "Add missing language...", new AddMissingLanguageScope(new ApplicationContext()),
            "---",
            "Copy all variant names", new TopicNameCopier(new ApplicationContext()),
            "---",
            "Remove variant name...", new VariantRemover(new ApplicationContext()),
            "Remove all empty variant names...", new AllEmptyVariantRemover(new ApplicationContext()),
            "---",
            "View", new Object[] {
                "View schema scopes", new ChangeVariantView(VARIANT_GUITYPE_SCHEMA, options), (VARIANT_GUITYPE_SCHEMA.equals(variantGUIType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View used scopes", new ChangeVariantView(VARIANT_GUITYPE_USED, options), (VARIANT_GUITYPE_USED.equals(variantGUIType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "---",
                "Flip name matrix", new FlipNameMatrix(OPTIONS_PREFIX, options),
            }
        };
    }
    
    
    public static Object[] getOccurrencesLabelPopupStruct(Options options) {
        String viewType = OccurrenceTable.VIEW_SCHEMA;
        int rowHeight = 1;
        try {
            if(options != null) {
                viewType = options.get(OccurrenceTable.VIEW_OPTIONS_KEY, OccurrenceTable.VIEW_SCHEMA);
                rowHeight = options.getInt(OccurrenceTable.ROW_HEIGHT_OPTIONS_KEY, rowHeight);
            }
        }
        catch(Exception e) {}
        
        return new Object[] {
            "Add occurrence...", new AddSchemalessOccurrence(new ApplicationContext()),
            "Delete all occurrences...", new DeleteAllOccurrences(new ApplicationContext()),
            "---",
            "View", new Object[] {
                "View schema scopes", new ChangeOccurrenceView(OccurrenceTable.VIEW_SCHEMA, options), (OccurrenceTable.VIEW_SCHEMA.equals(viewType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View used scopes", new ChangeOccurrenceView(OccurrenceTable.VIEW_USED, options), (OccurrenceTable.VIEW_USED.equals(viewType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View schema and used scopes", new ChangeOccurrenceView(OccurrenceTable.VIEW_USED_AND_SCHEMA, options), (OccurrenceTable.VIEW_USED_AND_SCHEMA.equals(viewType) ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "---",
                "View single row", new ChangeOccurrenceTableRowHeight(1, options), (rowHeight == 1 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View 5 rows", new ChangeOccurrenceTableRowHeight(5, options), (rowHeight == 5 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View 10 rows", new ChangeOccurrenceTableRowHeight(10, options), (rowHeight == 10 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
                "View 20 rows", new ChangeOccurrenceTableRowHeight(20, options), (rowHeight == 20 ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")),
            },
        };
    }
    
    
    
    
    public static Object[] getAssociationTableLabelPopupStruct() {
        return new Object[] {
//            "Add association...", new AddAssociations(new ApplicationContext()),
            "Add association...", new AddSchemalessAssociation(new ApplicationContext()),
            "---",
            /*
            "Count associations...", new CountAssociations(),
            "---",
             **/
            "Copy", new Object[] {
                "Copy associations as Wandora layout tab text", new CopyAssociations(new ApplicationAssociationContext()),
                "Copy associations as Wandora layout HTML", new CopyAssociations(new ApplicationAssociationContext(), CopyAssociations.HTML_OUTPUT),
                "---",
                "Copy associations as LTM layout tab text", new CopyAssociations(new ApplicationAssociationContext(), CopyAssociations.TABTEXT_OUTPUT, CopyAssociations.LTM_LAYOUT),
                "Copy associations as LTM layout HTML", new CopyAssociations(new ApplicationAssociationContext(), CopyAssociations.HTML_OUTPUT, CopyAssociations.LTM_LAYOUT),
            },
            "Paste", new Object[] {
                "Paste associations...", new PasteAssociations(new ApplicationContext()),
            },
            "Delete", new Object[] {
                "Delete associations...", new DeleteAssociationsInTopic(new ApplicationContext()),
                "Delete associated topics...", new DeleteFromTopics(new ApplicationContext(), DeleteFromTopics.DELETE_ASSOCIATED_TOPICS),
            }
        };
    }
    
    
    public static Object[] getAssociationTypeLabelPopupStruct() {
        return new Object[] {
            "Open association type topic", new OpenTopic(),
            /*
             "---",
            "Count associations of type...", new CountAssociationsOfType(),
             
            "---",
            "Copy", new Object[] {
                "Copy association type's base name", new CopyTopics(CopyTopics.INCLUDE_NOTHING),
                "Copy association type's SIs", new CopyTopics(CopyTopics.INCLUDE_NOTHING),
                "---",
                "Copy associations of this type as tab text", 
                "Copy associations of this type as HTML", 
                 "---",
                "Copy all associations of this type as tab text", 
                "Copy all associations of this type as HTML", 
                 
            },
            "Paste", new Object[] {
                "Paste tab text associations", new PasteAssociationsOfType(),
            },
             ***/
            "---",
            "Duplicate associations of type...", new DuplicateAssociations(),
            "Delete associations of this type...", new DeleteAssociations(),
            "Change association type...", new ChangeAssociationType(),
            "Change association role...", new ChangeAssociationRole(),
            //"Make players instance of role...", //new AddInstanceToPlayers(),
        };
    }
    
    
    
}
