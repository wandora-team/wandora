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
 * TextHTML.java
 *
 * Created on 24. lokakuuta 2007, 17:15
 *
 */

package org.wandora.application.gui.previews.formats;



import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.HostInterface;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;



/**
 *
 * @author akivela
 */
public class TextHTML implements MouseListener, ActionListener, PreviewPanel, HyperlinkListener, ComponentListener {

    private Wandora wandora;
    private String locator;
    private FXHTML htmlPane;

    private JPopupMenu linkPopup = null;
    private MouseEvent mouseEvent = null;
    private JPanel ui = null;
    
    
    
    /** Creates a new instance of TextHTML */
    public TextHTML(String locator) {
        Platform.setImplicitExit(false);
        this.wandora = Wandora.getWandora();
        this.locator = locator;
    }
    
    
    @Override
    public void stop() {
        if(htmlPane != null) {
            htmlPane.close();
        }
    }
    
    
    @Override
    public void finish() {
        if(htmlPane != null) {
            // htmlPane.close();
        }  
    }
    
    
    @Override
    public JPanel getGui() {
        // System.out.println("getting ui...");
        if(ui == null) {
            ui = new JPanel();
            // ui.addMouseListener(this);
            ui.setLayout(new BorderLayout(4,4));
            ui.setPreferredSize(new Dimension(640, 480+56));
            ui.setMinimumSize(new Dimension(640, 480+56));

            JPanel htmlWrapper = new JPanel();
            htmlWrapper.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            htmlWrapper.setLayout(new BorderLayout());

            htmlPane = new FXHTML(locator);
            htmlPane.initialize();

            htmlWrapper.add(htmlPane, BorderLayout.CENTER);
            
            
            //htmlPane.addMouseListener(this);
            //htmlPane.setBorder(BorderFactory.createLineBorder(borderColor));
            //updateLinkMenu();

            JPanel controllerPanel = new JPanel();
            controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
            
            ui.add(htmlWrapper, BorderLayout.CENTER);
            ui.add(controllerPanel, BorderLayout.SOUTH);
            ui.addComponentListener(this);
        }
        ui.revalidate();
        ui.repaint();
        return ui;
    }

    
    
    private JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Reload", UIBox.getIcon(0xf015), this, // 0xf01e
            "---",
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Copy selection", UIBox.getIcon(0xf0c5), this,
            "Copy as image", UIBox.getIcon(0xf0c5), this,
            "Save as", UIBox.getIcon(0xf0c7), this, // f019
            "---",
            "Set basename", UIBox.getIcon(0xf067), this,
            "Set display name", UIBox.getIcon(0xf067), this,
            "Set occurrence", UIBox.getIcon(0xf067), this,
        }, this);
    }

    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    

    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {

        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    
    // -------------------------------------------------------------------------

    
    
    public void updateMenu() {
        if(locator != null && locator.length() > 0) {
            JPopupMenu m = getMenu();
            if(m != null) {
                ui.setComponentPopupMenu(m);
                if(htmlPane != null) htmlPane.setComponentPopupMenu(m);
            }
        }
        else {
            if(htmlPane != null) htmlPane.setComponentPopupMenu(null);
        }
    }
    
    
    
    
    public JPopupMenu getMenu() {
        WandoraToolSet extractTools = wandora.toolManager.getToolSet("extract");
        WandoraToolSet.ToolFilter filter = extractTools.new ToolFilter() {
            @Override
            public boolean acceptTool(WandoraTool tool) {
                return (tool instanceof DropExtractor);
            }
            @Override
            public Object[] addAfterTool(final WandoraTool tool) {
                ActionListener toolListener = new ActionListener() {
                    DropExtractor myTool = (DropExtractor) tool;
                    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                        String c = actionEvent.getActionCommand();
                        try {
                            String s = getSelection();
                            ((DropExtractor) myTool).dropExtract(s);
                            //System.out.println("extracting " + s);
                            //System.out.println("extractor " + myTool);
                        }
                        catch(Exception exx) { 
                            wandora.handleError(exx);
                        }
                    }
                };
                return new Object[] { tool.getIcon(), toolListener };
            }
        };
        
        Object[] extractMenu = extractTools.getAsObjectArray(filter);
        

        Object[] menuStructure = new Object[] {
            "Open in external viewer...",
            "---",
            "Copy location",
            "Copy selection",
            "Copy as image",
            "Save as...",
            "---",
            "For selection", new Object[] {
                "Make selection basename",
                "Make selection display name",
                "Make selection occurrence...",
            },
            "Extract selection", extractMenu,
          
        };
        return UIBox.makePopupMenu(menuStructure, this);
    }
    

    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        if(PreviewUtils.startsWithAny(c, "Reload")) {
            if(htmlPane != null) {
                htmlPane.reload();
            }
        }
        
        else if(PreviewUtils.startsWithAny(c, "Open in external", "Open ext")) {
            PreviewUtils.forkExternalPlayer(locator);
        }
        
        else if(c.equalsIgnoreCase("Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        
        else if(c.equalsIgnoreCase("Copy selection")) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    ClipboardBox.setClipboard(s);
                }
                catch(Exception e) {
                    wandora.handleError(e);
                }
            }
        }
        
        else if(c.equalsIgnoreCase("Copy as image")) {
            int w = htmlPane.getWidth();
            int h = htmlPane.getHeight();
            BufferedImage htmlPaneImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR );
            htmlPane.paint(htmlPaneImage.getGraphics());
            ClipboardBox.setClipboard(htmlPaneImage);
        }
        
        else if(c.startsWith("Save as")) {
            PreviewUtils.saveToFile(locator);
        }
        
        else if(PreviewUtils.startsWithAny(c, "Make selection display name", "Make display name")) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    Topic t = wandora.getOpenTopic();
                    if(t != null) {
                        Topic type = wandora.showTopicFinder("Select display name language");
                        if(type == null) return;
                        HashSet scope = new HashSet();
                        scope.add(type);
                        scope.add(t.getTopicMap().getTopic(XTMPSI.DISPLAY));
                        t.setVariant(scope, s);
                        wandora.doRefresh();
                    }
                }
                catch(Exception e) {
                    wandora.handleError(e);
                }
            }
        }
        
        else if(PreviewUtils.startsWithAny(c, "Make selection basename", "Set basename")) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    Topic t = wandora.getOpenTopic();
                    if(t != null) {
                        t.setBaseName(s);
                        wandora.doRefresh();
                    }
                }
                catch(Exception e) {
                    wandora.handleError(e);
                }
            }
        }
        
        else if(PreviewUtils.startsWithAny(c, "Make selection occurrence...", "Set occurrence")) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    Topic type = wandora.showTopicFinder("Select occurrence type");
                    if(type == null) return;
                    Topic scope = wandora.showTopicFinder("Select occurrence scope");
                    if(scope == null) return;
                    Topic t = wandora.getOpenTopic();
                    if(t != null) {
                        t.setData(type, scope, s);
                        wandora.doRefresh();
                    }
                }
                catch(Exception e) {
                    wandora.handleError(e);
                }
            }
        }
    }
   
    

    public String getSelection() {
        if(htmlPane != null) {
            return htmlPane.getSelectedText();
        }
        return "";
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    
    
    public void updateLinkMenu() {
        WandoraToolSet extractTools = wandora.toolManager.getToolSet("extract");
        WandoraToolSet.ToolFilter filter = extractTools.new ToolFilter() {
            @Override
            public boolean acceptTool(WandoraTool tool) {
                return (tool instanceof DropExtractor);
            }
            @Override
            public Object[] addAfterTool(final WandoraTool tool) {
                ActionListener toolListener = new ActionListener() {
                    DropExtractor myTool = (DropExtractor) tool;
                    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                        String c = actionEvent.getActionCommand();
                        try {
                            String url = currentHyperLink.toExternalForm();
                            ((DropExtractor) myTool).dropExtract(new String[] { url });
                            System.out.println("extracting url " + url);
                            System.out.println("extractor " + myTool);
                        }
                        catch(Exception exx) { 
                            wandora.handleError(exx);
                        }
                    }
                };
                return new Object[] { tool.getIcon(), toolListener };
            }
        };
        
        Object[] linkExtractMenu = extractTools.getAsObjectArray(filter);

        Object[] menuStructure = new Object[] {
            "Open in external viewer...",
            "---",
            "Copy location",
            "Copy as image",
            "Save as...",
            "---",
            "For selection", new Object[] {
                "Make link url subject identifier",
                "Make link url subject locator",
            },
            "Extract link url", linkExtractMenu,
        };
        linkPopup = UIBox.makePopupMenu(menuStructure, this);
       
    }
    
    
    
    public URL currentHyperLink;
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            currentHyperLink = e.getURL();
            
            if(!linkPopup.isVisible()) {
                System.out.println("link pupup1");
                if(mouseEvent != null) {
                    System.out.println("link pupup2");
                    linkPopup.show(ui, mouseEvent.getX(), mouseEvent.getY());
                }
            }
             
            
            /*
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                HTMLDocument doc = (HTMLDocument)pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            }
            else {
                try {
                    pane.setPage(e.getURL());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
             **/
        }
    }
    

    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { "text/html" }, 
                new String[] { "html", "htm" });
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    public class FXHTML extends JFXPanel {
        
        private final String locator;
        
        public WebView webView = null;
        private WebEngine webEngine = null;
        private boolean informPopupBlocking = true;
        private boolean informVisibilityChanges = true;
        private String webSource = null;
        
        
        public FXHTML(final String locator) {
            this.locator = locator;
        }
        
        

        public void initialize() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    initializeFX();
                    open(locator);
                }
            });
        }
        


        private void initializeFX() {
            Group group = new Group();
            Scene scene = new Scene(group);
            this.setScene(scene);

            webView = new WebView();

            String javaFXVersion = com.sun.javafx.runtime.VersionInfo.getRuntimeVersion();
            int javaFXVersionInt = Integer.parseInt(javaFXVersion.substring(0, javaFXVersion.indexOf(".")));
            
            if(javaFXVersionInt >= 8) {
                webView.setScaleX(1.0);
                webView.setScaleY(1.0);
                //webView.setFitToHeight(false);
                //webView.setFitToWidth(false);
                //webView.setZoom(javafx.stage.Screen.getPrimary().getDpi() / 96);
            }

            group.getChildren().add(webView);

            //int w = TextHTML.this.ui.getWidth();
            //int h = TextHTML.this.ui.getHeight();

            //webView.setMinSize(w, h);
            //webView.setMaxSize(w, h);
            //webView.setPrefSize(w, h);

            // Obtain the webEngine to navigate
            webEngine = webView.getEngine();

            
            webEngine.setOnVisibilityChanged(new EventHandler<WebEvent<java.lang.Boolean>>() {
                @Override
                public void handle(WebEvent<Boolean> t) {
                    // Nothing here
                }
            });
            
            webEngine.locationProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        if(newValue == null) return;
                        if(newValue.endsWith(".pdf")) {
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
                                    // Nothing here
                                }
                            });
                        }
                    }
            });
            webEngine.titleProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                // Nothing here
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
                new javafx.beans.value.ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if(newState == Worker.State.SCHEDULED) {
                            //System.out.println("Scheduled!");
                            
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
                        }
                        else if(newState == Worker.State.CANCELLED) {
                            //System.out.println("Cancelled!");
                        }
                        else if(newState == Worker.State.FAILED) {
                            String failedToOpenMessage = "<h1>Failed to open URL</h1>";
                            webEngine.loadContent(failedToOpenMessage);
                        }
                    }
                }
            );

        }
        
        

        
        public void open(final String locator) {
            try {
                if(DataURL.isDataURL(locator)) {
                    DataURL dataUrl = new DataURL(locator);
                    String data = new String( dataUrl.getData() );
                    webEngine.loadContent(data);
                }
                else if(locator != null && locator.startsWith("file:")) {

                }
                else {
                    webEngine.load(locator);
                }
                
                Component c = TextHTML.this.ui.getParent();
                if(c != null) {
                    int w = c.getWidth();
                    webView.setMinSize(w, 480);
                    webView.setMaxSize(w, 480);
                    webView.setPrefSize(w, 480);
                }
                else {
                    webView.setMinSize(640, 480);
                    webView.setMaxSize(640, 480);
                    webView.setPrefSize(640, 480);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        
        public void close() {
            // System.out.println("TEST1");
            Thread runner = new Thread() {
                @Override
                public void run() {
                    try {
                        if(webEngine != null) {
                            webEngine.load(null);
                            // System.out.println("TEST2");
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
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
    
        
        
        public void reload() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    open(locator);
                }
            });
        }
        
        
        
        public String getSelectedText() {
            //System.out.println("get selected text");
            String selection = (String) executeSynchronizedScript("window.getSelection().toString()");
            //System.out.println("  and the selection is "+selection);
            return selection;
        }
        
        
        private Object scriptReturn = null;
        public Object executeSynchronizedScript(final String script) {
            scriptReturn = null;
            if(script != null && script.length()>0) {
                Thread runner = new Thread() {
                    @Override
                    public void run() {
                        if(webEngine != null) {
                            scriptReturn = webEngine.executeScript(script);
                        }
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
        //handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }
   
    
    private void handleComponentEvent(ComponentEvent e) {
        try {           
            if(ui != null) {
                if(ui.getParent() != null) {
                    Dimension d = new Dimension(640, 480+56);
                    ui.setPreferredSize(d);
                    ui.setMinimumSize(d);
                    ui.setMaximumSize(d);
                }

                if(htmlPane != null) {
                    if(htmlPane.webView != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if(ui != null && htmlPane != null && htmlPane.webView != null) {
                                    Component c = ui;
                                    if(ui.getParent() != null) c = ui.getParent();
                                    if(c != null) {
                                        int w = c.getWidth();
                                        int h = c.getHeight();
                                        if(w > 1 && h > 1) {
                                            htmlPane.webView.setMinSize(w, h-56);
                                            htmlPane.webView.setMaxSize(w, h-56);
                                            htmlPane.webView.setPrefSize(w, h-56);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
                ui.revalidate();
                ui.repaint();
            }
        }
        catch(Exception ex) {}
    }
}
