/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
 */


package org.wandora.application.gui.topicpanels.webview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javax.swing.JPopupMenu;
import org.wandora.application.CancelledException;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.utils.Options;

import javafx.scene.web.*;
import javafx.util.Callback;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import netscape.javascript.JSObject;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.modulesserver.ModulesWebApp;
import org.wandora.application.modulesserver.WandoraModulesServer;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserExtractorManager;
import org.wandora.application.tools.server.HTTPServerTool;
import org.wandora.application.tools.webview.AddWebLocationAsOccurrence;
import org.wandora.application.tools.webview.AddWebLocationAsSubjectIdentifier;
import org.wandora.application.tools.webview.AddWebLocationAsSubjectLocator;
import org.wandora.application.tools.webview.AddWebSelectionAsBasename;
import org.wandora.application.tools.webview.AddWebSelectionAsOccurrence;
import org.wandora.application.tools.webview.AddWebSourceAsOccurrence;
import org.wandora.application.tools.webview.CreateWebLocationTopic;
import org.wandora.application.tools.webview.OpenFirebugInWebView;
import org.wandora.application.tools.webview.OpenOccurrenceInWebView;
import org.wandora.application.tools.webview.OpenWebLocationInExternalBrowser;
import org.wandora.utils.Tuples.T3;

/**
 *
 * @author akivela
 */


public class WebViewPanel extends javax.swing.JPanel implements TopicMapListener, RefreshListener, ActionListener, ComponentListener {

    private static final String JAVASCRIPT_RESOURCE_GET_SELECTED_SOURCE = "js/GetSelectionHTML.js";
    private static final String JAVASCRIPT_RESOURCE_GET_SOURCE_WITH_SELECTION_INDEXES = "js/GetSourceWithSelectionIndexes.js";
    
    public static String javaFXVersion = "";
    public static int javaFXVersionInt = 0;
    
    public boolean USE_LOCAL_OPTIONS = true;
    
    private String title = null;
    private Topic rootTopic = null;
    private TopicMap tm = null;
    private boolean isUIInitialized = false;
    private Options options = null;

    private Component fxPanelHandle = null;
    private WebView webView = null;
    private WebEngine webEngine = null;
    private String webSource = null;
    
    private boolean informPopupBlocking = true;
    private boolean informVisibilityChanges = true;
    
    private BrowserExtractorManager browserExtractorManager = null;
    
    private boolean viewBrowser = true;
    
    private static final String failedToOpenMessage = "<h1>Failed to open URL</h1>";
    
    private ModulesWebApp selectedWebApp=null;
    
    public class WandoraJFXPanel extends JFXPanel {

        // EmbeddedScene.mouseEvent calls it's listeners with 40x wheel rotation 
        // multiplier -> mouseDelta in JS is 4800 instead of 120. This screws up
        // the zoom behavior in D3 powered visualizations.
        //
        // The Bug is in D3! Wandora uses a slightly modified version of D3 Javascript
        // library with fixed wheel multiplier.
        @Override
        protected void processMouseWheelEvent(MouseWheelEvent e) {
            MouseWheelEvent ee = new MouseWheelEvent(
                    (Component) e.getSource(), e.getID(), e.getWhen(),
                    e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(),
                    e.getYOnScreen(), e.getClickCount(),
                    e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(),
                    e.getWheelRotation(), e.getPreciseWheelRotation());
            super.processMouseWheelEvent(ee);
        }
    }
    
    
    /**
     * Creates new form WebViewPanel
     */
    public WebViewPanel() {
        try {
            javaFXVersion = com.sun.javafx.runtime.VersionInfo.getRuntimeVersion();
            javaFXVersionInt = Integer.parseInt(javaFXVersion.substring(0, javaFXVersion.indexOf(".")));
            if(javaFXVersionInt >= 8) {
                System.out.println("You are using JavaFX version "+javaFXVersion);
                System.out.println("WebViewPanel has been tested with JavaFX 2.x.");
                System.out.println("At this time WebViewPanel may have problems with JavaFX 8 and above.");
            }
        }
        catch(Exception e) {}
        try {
            browserExtractorManager = new BrowserExtractorManager(Wandora.getWandora());
        }
        catch(Exception e) {}
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonPanel = new javax.swing.JPanel();
        backButton = new SimpleButton();
        forwardButton = new SimpleButton();
        reloadButton = new SimpleButton();
        stopButton = new SimpleButton();
        urlTextField = new SimpleField();
        menuButton = new SimpleButton();

        setMinimumSize(new java.awt.Dimension(20, 20));
        setPreferredSize(new java.awt.Dimension(20, 20));
        setLayout(new java.awt.BorderLayout());

        buttonPanel.setBackground(new java.awt.Color(238, 238, 238));
        buttonPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        buttonPanel.setLayout(new java.awt.GridBagLayout());

        backButton.setText("<");
        backButton.setToolTipText("Go back one page");
        backButton.setBorder(null);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        backButton.setPreferredSize(new java.awt.Dimension(24, 24));
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        buttonPanel.add(backButton, gridBagConstraints);

        forwardButton.setText(">");
        forwardButton.setToolTipText("Go forward one page");
        forwardButton.setBorder(null);
        forwardButton.setBorderPainted(false);
        forwardButton.setContentAreaFilled(false);
        forwardButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        forwardButton.setPreferredSize(new java.awt.Dimension(24, 24));
        forwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        buttonPanel.add(forwardButton, gridBagConstraints);

        reloadButton.setText("R");
        reloadButton.setToolTipText("Reload current page");
        reloadButton.setBorderPainted(false);
        reloadButton.setContentAreaFilled(false);
        reloadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reloadButton.setPreferredSize(new java.awt.Dimension(24, 24));
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        buttonPanel.add(reloadButton, gridBagConstraints);

        stopButton.setText("S");
        stopButton.setToolTipText("Stop and close current page");
        stopButton.setBorderPainted(false);
        stopButton.setContentAreaFilled(false);
        stopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        stopButton.setPreferredSize(new java.awt.Dimension(24, 24));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(stopButton, new java.awt.GridBagConstraints());

        urlTextField.setMargin(new java.awt.Insets(2, 4, 2, 4));
        urlTextField.setPreferredSize(new java.awt.Dimension(6, 24));
        urlTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urlTextFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        buttonPanel.add(urlTextField, gridBagConstraints);

        menuButton.setText("=");
        menuButton.setToolTipText("More Webview options and tools");
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        menuButton.setPreferredSize(new java.awt.Dimension(25, 25));
        menuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuButtonMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        buttonPanel.add(menuButton, gridBagConstraints);

        add(buttonPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void urlTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlTextFieldKeyPressed
        //System.out.println("evt.getKeyCode() == "+evt.getKeyCode());
        if(webEngine != null && evt.getKeyCode() == 10) {
            final String u = toURL(urlTextField.getText());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webEngine.load(u);
                }
            });
        }
    }//GEN-LAST:event_urlTextFieldKeyPressed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        if(webEngine != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webEngine.executeScript("history.back()");
                }
            });
        }
    }//GEN-LAST:event_backButtonActionPerformed

    private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
        if(webEngine != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webEngine.executeScript("history.forward()");
                }
            }); 
        }
    }//GEN-LAST:event_forwardButtonActionPerformed

    
    
    private void menuButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuButtonMousePressed
        Object[] menuItems = getBrowserMenuStruct();
        if(menuItems != null && menuItems.length > 0) {
            JPopupMenu popupMenu = UIBox.makePopupMenu(menuItems, this);
            popupMenu.show(this, menuButton.getX()+evt.getX(), menuButton.getY()+evt.getY());
        }
    }//GEN-LAST:event_menuButtonMousePressed

    
    
    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
        if(webEngine != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    webEngine.reload();
                }
            }); 
        }
    }//GEN-LAST:event_reloadButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        browse((String) null);
    }//GEN-LAST:event_stopButtonActionPerformed

    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton forwardButton;
    private javax.swing.JButton menuButton;
    private javax.swing.JButton reloadButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables

    
    
    // -------------------------------------------------------------------------
    
    
    
    public WebEngine getWebEngine() {
        return this.webEngine;
    }
    
    
    
    
    public String getWebLocation() {
        return urlTextField.getText();
        //return webEngine.getLocation();
    }
    
    
    
    private Object[] getBrowserMenuStruct() {
        ArrayList topicMenuItems = new ArrayList();
        if(rootTopic != null) {
            try {
                if(rootTopic.getSubjectLocator() != null) {
                    topicMenuItems.add(rootTopic.getSubjectLocator().toExternalForm());
                    topicMenuItems.add("---");
                }
                for(Locator l : rootTopic.getSubjectIdentifiers()) {
                    topicMenuItems.add(l.toExternalForm());
                }
                boolean firstOccurrence = true;
                for(Topic occurrenceType : rootTopic.getDataTypes()) {
                    Hashtable<Topic,String> scopedOccurrences = rootTopic.getData(occurrenceType);
                    for(Topic occurrenceScope : scopedOccurrences.keySet()) {
                        if(firstOccurrence) {
                            topicMenuItems.add("---");
                            firstOccurrence = false;
                        }
                        topicMenuItems.add("Open occurrence "+TopicToString.toString(occurrenceType)+" - "+TopicToString.toString(occurrenceScope));
                        topicMenuItems.add(new OpenOccurrenceInWebView(rootTopic, occurrenceType, occurrenceScope));
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        
        ArrayList browseServices = new ArrayList();
        Wandora wandora = Wandora.getWandora();
        if(wandora != null) {
            WandoraModulesServer httpServer = wandora.httpServer;
            
            ArrayList<ModulesWebApp> webApps=httpServer.getWebApps();
            HashMap<String,ModulesWebApp> webAppsMap=new HashMap<String,ModulesWebApp>();
            for(ModulesWebApp wa : webApps) webAppsMap.put(wa.getAppName(), wa);
            ArrayList<String> sorted = new ArrayList<String>(webAppsMap.keySet());
            Collections.sort(sorted);

            for(String appName : sorted) {
                ModulesWebApp wa=webAppsMap.get(appName);
                
                if(wa.isRunning()) {
                     String url=wa.getAppStartPage();
                     if(url==null) continue;

                     browseServices.add(appName);
                     browseServices.add(UIBox.getIcon("gui/icons/open_browser.png"));
                     browseServices.add(new HTTPServerTool(HTTPServerTool.OPEN_PAGE_IN_BROWSER_TOPIC_PANEL, wa, this));
                }
                else {
                     browseServices.add(appName);
                     browseServices.add(UIBox.getIcon("gui/icons/open_browser.png"));
                     browseServices.add(new HTTPServerTool(HTTPServerTool.OPEN_PAGE_IN_BROWSER_TOPIC_PANEL, wa, this));
                }
            }
        }
        
        ArrayList extractors = new ArrayList();
        if(browserExtractorManager != null) {
            T3<String, Integer, Integer> contentWithSelectionIndexes = getSourceWithSelectionIndexes();
            String content = contentWithSelectionIndexes.e1;
            int start = contentWithSelectionIndexes.e2.intValue();
            int end = contentWithSelectionIndexes.e3.intValue();
            String selection = null;
            try {
                if(start != -1 && end != -1) {
                    selection = content.substring(start,end);
                }
            }
            catch(Exception e) {}
            BrowserExtractRequest extractRequest=new BrowserExtractRequest(getWebLocation(), content, null, "WebView", start, end, selection);
            String[] browserExtractors = browserExtractorManager.getExtractionMethods(extractRequest);

            for(String browserExtractorName : browserExtractors) {
                extractors.add(browserExtractorName);
                extractors.add((ActionListener) this);
            }
        }
        
        Object[] menuStruct = new Object[] {
            "Open current topic",
                topicMenuItems.toArray(),
            "Open Wandora's services",
                browseServices.toArray(),
            "---",
            "Open Firebug", new OpenFirebugInWebView(),
            "Open in external browser", new OpenWebLocationInExternalBrowser(),
            "---",
            "Add to current topic",
            new Object[] {
                "Add selection as an occurrence...", new AddWebSelectionAsOccurrence(),
                "Add selection source as an occurrence...", new AddWebSourceAsOccurrence(true),
                "Add source as an occurrence...", new AddWebSourceAsOccurrence(),
                "Add location as an occurrence...", new AddWebLocationAsOccurrence(),
                "---",
                "Add location as a subject locator", new AddWebLocationAsSubjectLocator(),
                "Add location as a subject identifier", new AddWebLocationAsSubjectIdentifier(),
                "---",
                "Add selection as a basename", new AddWebSelectionAsBasename(),
                // "---",
                // "Add links as associations",
                // "Add image locations as associations",
            },
            "Create a topic",
            new Object[] {
                "Create topic from location", new CreateWebLocationTopic(),
                "---",
                "Create topic from location and make instance", new CreateWebLocationTopic(true,false,true,false),
                "Create topic from location and make subclass", new CreateWebLocationTopic(false,true,true,false),
                "Create topic from location and associate", new CreateWebLocationTopic(false,false,true,true),
            },
            "---",
            "Extract",
            extractors.toArray()
        };
        

        return menuStruct;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } 
        catch (MalformedURLException exception) {
            if(!str.startsWith("http://")) {
                return "http://"+str;
            }
            else {
                return null;
            }
        }
    }
    
    
    
    
    // ------------------------------------------------------ topic listener ---
    
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        doRefresh();
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        doRefresh();
    }

    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void doRefresh() throws TopicMapException {
        
    }

    
    public void open(Topic topic) throws TopicMapException {
        try {
            rootTopic = topic;
            if(!isUIInitialized) {
                isUIInitialized = true;
                initializeUI();
            }
            else {
                browse(rootTopic);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    
    public void openContent(final String str) {
        try {
            if(str != null) {
                if(!isUIInitialized) {
                    isUIInitialized = true;
                    initializeUI();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        webEngine.loadContent(str);
                    }
                });
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    private void initializeUI() {
        Wandora wandora = Wandora.getWandora();
        if(options == null) {
            if(USE_LOCAL_OPTIONS) {
                options = new Options(wandora.getOptions());
            }
            else {
                options = wandora.getOptions();
            }
        }
        tm = wandora.getTopicMap();
        initComponents();
        Platform.setImplicitExit(false);
        final WandoraJFXPanel fxPanel = new WandoraJFXPanel();
        fxPanelHandle = fxPanel;
        this.add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
                browse(rootTopic);
            }
        });
        this.addComponentListener(this);

        backButton.setText("");
        backButton.setIcon(UIBox.getIcon("gui/icons/webview/backward.png"));
        forwardButton.setText("");
        forwardButton.setIcon(UIBox.getIcon("gui/icons/webview/forward.png"));
        stopButton.setText("");
        stopButton.setIcon(UIBox.getIcon("gui/icons/webview/stop.png"));
        reloadButton.setText("");
        reloadButton.setIcon(UIBox.getIcon("gui/icons/webview/reload.png"));
        menuButton.setText("");
        menuButton.setIcon(UIBox.getIcon("gui/icons/webview/menu.png"));
    }
    
    
    
    
    
    public void browse(Topic topic) {
        try {
            String u = null;
            if(topic != null) {
                u = topic.getFirstSubjectIdentifier().toExternalForm();
                if(selectedWebApp!=null){
                    String url=selectedWebApp.getAppTopicPage(u);
                    if(url!=null) {
                        browse(url,false);
                        return;
                    }
                }
            }
            browse(u);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void browse(final String url) {
        browse(url,true);
    }
    
    public void browse(final String url, boolean resetWebApp) {
        if(resetWebApp && url != null) {
            selectedWebApp=null;
            WandoraModulesServer s=Wandora.getWandora().getHTTPServer();
            for(ModulesWebApp webApp : s.getWebApps()){
                if(url.equals(webApp.getAppStartPage())) {
                    selectedWebApp=webApp;
                    break;
                }
            }
        }
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webEngine.load(url);
            }
        });
    }
    
    public void executeScript(final String script) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webEngine.executeScript(script);
            }
        });
    }
    
    
    private Object scriptReturn = null;
    public Object executeSynchronizedScript(final String script) {
        scriptReturn = null;
        if(script != null && script.length()>0) {
            Thread runner = new Thread() {
                @Override
                public void run() {
                    scriptReturn = webEngine.executeScript(script);
                }
            };
            Platform.runLater(runner);

            do {
                try {
                    Thread.sleep(100);
                }
                catch(Exception ex) {

                }
            }
            while(runner.isAlive());
        }
        return scriptReturn;
    }
    

    public Object executeSynchronizedScriptResource(String scriptResource) {
        try {
            String script = IOUtils.toString(this.getClass().getResourceAsStream(scriptResource),"UTF-8");
            return executeSynchronizedScript(script);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    private void startLoadingAnimation() {
        Wandora wandora = Wandora.getWandora();
        if(wandora != null) wandora.setAnimated(true, this);
    }
    private void stopLoadingAnimation() {
        Wandora wandora = Wandora.getWandora();
        if(wandora != null) wandora.setAnimated(false, this);
    }
    
    
    
    private void initFX(final JFXPanel fxPanel) {
        Group group = new Group();
        Scene scene = new Scene(group);
        fxPanel.setScene(scene);

        webView = new WebView();

        if(javaFXVersionInt >= 8) {
            webView.setScaleX(1.0);
            webView.setScaleY(1.0);
            //webView.setFitToHeight(false);
            //webView.setFitToWidth(false);
            //webView.setZoom(javafx.stage.Screen.getPrimary().getDpi() / 96);
        }

        group.getChildren().add(webView);
        
        int w = this.getWidth();
        int h = this.getHeight()-34;
        
        webView.setMinSize(w, h);
        webView.setMaxSize(w, h);
        webView.setPrefSize(w, h);

        // Obtain the webEngine to navigate
        webEngine = webView.getEngine();
        
        webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                    if (newValue.endsWith(".pdf")) {
                        try {
                            int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Open PDF document in external application?", "Open PDF document in external application?", WandoraOptionPane.YES_NO_OPTION);
                            if(a == WandoraOptionPane.YES_OPTION) {
                                Desktop dt = Desktop.getDesktop();
                                dt.browse(new URI(newValue));
                            }
                        }
                        catch(Exception e) {}
                    }
                    else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                urlTextField.setText(newValue);
                            }
                        });
                    }
                }
        });
        webEngine.titleProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override 
                        public void run() {
                            title = newValue;
                        }
                    });
                }
        });
        webEngine.setOnAlert(new EventHandler<WebEvent<java.lang.String>>() {
            @Override
            public void handle(WebEvent<String> t) {
                if(t != null) {
                    String str = t.getData();
                    if(str != null && str.length() > 0) {
                        WandoraOptionPane.showMessageDialog(Wandora.getWandora(), str, "Javascript Alert", WandoraOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        webEngine.setConfirmHandler(new Callback<String, Boolean>() {
            @Override 
            public Boolean call(String msg) {
                int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), msg, "Javascript Alert", WandoraOptionPane.YES_NO_OPTION);
                return (a == WandoraOptionPane.YES_OPTION);
            }
        });
        webEngine.setPromptHandler(new Callback<PromptData, String>() {
            @Override 
            public String call(PromptData data) {
                String a = WandoraOptionPane.showInputDialog(Wandora.getWandora(), data.getMessage(), data.getDefaultValue(), "Javascript Alert", WandoraOptionPane.QUESTION_MESSAGE);
                return a;
            }
        });
        
        webEngine.setCreatePopupHandler(new Callback<PopupFeatures,WebEngine>() {
            @Override 
            public WebEngine call(PopupFeatures features) {
                if(informPopupBlocking) {
                    WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                            "A javascript popup has been blocked. Wandora doesn't allow javascript popups in Webview topic panel.", 
                            "Javascript popup blocked", 
                            WandoraOptionPane.PLAIN_MESSAGE);
                }
                informPopupBlocking = false;
                return null;
            }
        });
        webEngine.setOnVisibilityChanged(new EventHandler<WebEvent<Boolean>>() {
            @Override
            public void handle(WebEvent<Boolean> t) {
                if(t != null) {
                    Boolean b = t.getData();
                    if(informVisibilityChanges) {
                        WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                                "A browser window visibility change has been blocked. Wandora doesn't allow visibility changes of windows in Webview topic panel.", 
                                "Javascript visibility chnage blocked", 
                                WandoraOptionPane.PLAIN_MESSAGE);
                        informVisibilityChanges = false;
                    }
                }
            }
        });
        webEngine.getLoadWorker().stateProperty().addListener(
            new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    if(newState == Worker.State.SCHEDULED) {
                        //System.out.println("Scheduled!");
                        startLoadingAnimation();
                    }
                    if(newState == Worker.State.SUCCEEDED) {
                        Document doc = webEngine.getDocument();
                        try {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                            // transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
                            
                            StringWriter stringWriter = new StringWriter();
                            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
                            webSource = stringWriter.toString();
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        stopLoadingAnimation();
                    }
                    else if(newState == Worker.State.CANCELLED) {
                        //System.out.println("Cancelled!");
                        stopLoadingAnimation();
                    }
                    else if(newState == Worker.State.FAILED) {
                        webEngine.loadContent(failedToOpenMessage);
                        stopLoadingAnimation();
                    }
                }
            });
        
    }
    
    
    

    public void stop() {
        System.out.println("---- Stopping Webview topic panel!");
        browse((String) null);
    }
    
    


    public void refresh() throws TopicMapException {
        
    }


    public boolean applyChanges() throws CancelledException, TopicMapException {
        return true;
    }

    

    public Topic getTopic() throws TopicMapException {
        return rootTopic;
    }

    

    public String getTitle() {
        if(rootTopic == null) {
            return "Webview";
        }
        else {
            return TopicToString.toString(rootTopic);
        }
    }






    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if(cmd != null) {
            if(cmd.startsWith("http://")) {
                browse(cmd);
            }
            else {
                Wandora w = Wandora.getWandora();
                if(w != null) w.setAnimated(true, this);
                T3<String, Integer, Integer> contentWithSelectionIndexes = getSourceWithSelectionIndexes();
                String content = contentWithSelectionIndexes.e1;
                int start = contentWithSelectionIndexes.e2.intValue();
                int end = contentWithSelectionIndexes.e3.intValue();
                String selection = null;
                try {
                    if(start != -1 && end != -1) {
                        selection = content.substring(start,end);
                    }
                }
                catch(Exception ex) {}
                BrowserExtractRequest extractRequest=new BrowserExtractRequest(getWebLocation(), content, cmd, "WebView", start, end, selection);
                String message=browserExtractorManager.doPluginExtract(extractRequest);
                if(message!=null) {
                    WandoraOptionPane.showMessageDialog(Wandora.getWandora(), message, cmd);
                }
                if(w != null) w.setAnimated(false, this);
            }
        }
    }
    
    
    
    public Image getSnapshot() {
        WritableImage image = webView.snapshot(null, null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        return bufferedImage;
    }
    
    
    
    public String getSelectedText() {
        //System.out.println("get selected text");
        String selection = (String) executeSynchronizedScript("window.getSelection().toString()");
        //System.out.println("  and the selection is "+selection);
        return selection;
    }
    
    
    public String getSelectedSource() {
        String selection = (String) executeSynchronizedScriptResource(JAVASCRIPT_RESOURCE_GET_SELECTED_SOURCE);
        //System.out.println("--------");
        //System.out.println(selection);
        //System.out.println("--------");
        return selection;
    }
    
    public T3<String,Integer,Integer> getSourceWithSelectionIndexes() {
        String content = null;
        int start = -1;
        int end = -1;
        try {
            JSObject d = (JSObject) executeSynchronizedScriptResource(JAVASCRIPT_RESOURCE_GET_SOURCE_WITH_SELECTION_INDEXES);

            if(d != null) {
                //System.out.println("========");
                //System.out.println(d.toString());
                //System.out.println("========");
                try {
                    content = (String) d.getMember("content");
                    start = (Integer) d.getMember("selectionStart");
                    end = (Integer) d.getMember("selectionEnd");
                } catch(Exception e) {}
            }
            System.out.println("--------");
            //System.out.println(content);
            if(start != -1 && end != -1) System.out.println(content.substring(start, end));
            else System.out.println("ALL");
            System.out.println("--------");
            System.out.println("start: "+start);
            System.out.println("end: "+end);
            System.out.println("--------");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new T3(content, new Integer(start), new Integer(end));
    }
    
    
    public String getSource() {
        return webSource;
    }
    
    
    
    public String getWebTitle() {
        if(webEngine != null) { 
            return webEngine.getTitle();
        }
        else return null;
    }
    

    
    // -------------------------------------------------------------------------
    // --------------------------------------------------- ComponentListener ---
    // -------------------------------------------------------------------------
    
    
    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }
   
    
    private void handleComponentEvent(ComponentEvent e) {
        try {
            Component c = this;
            if(this.getParent() != null) c = this.getParent();
            
            final int w = c.getWidth();
            final int h = c.getHeight()-35;
            Dimension d = new Dimension(w, h);
            
            if(this.getParent() != null) {
                this.setPreferredSize(d);
                this.setMinimumSize(d);
                this.setMaximumSize(d);
            }
            

            if(webView != null && w > 1 && h > 1) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        webView.setMinSize(w, h);
                        webView.setMaxSize(w, h);
                        webView.setPrefSize(w, h);
                    }
                });
            }
            revalidate();
            repaint();
        }
        catch(Exception ex) {}
    }

    // -------------------------------------------------------------------------
    
    
    


}
