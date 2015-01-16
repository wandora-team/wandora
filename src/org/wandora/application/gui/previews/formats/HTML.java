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
 * HTML.java
 *
 * Created on 24. lokakuuta 2007, 17:15
 *
 */

package org.wandora.application.gui.previews.formats;



import org.wandora.application.gui.previews.*;
import org.wandora.utils.Options;
import org.wandora.utils.IObox;
import org.wandora.utils.ClipboardBox;
import org.wandora.application.gui.simple.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;

import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;


/**
 *
 * @author akivela
 */
public class HTML extends JPanel implements MouseListener, ActionListener, PreviewPanel, HyperlinkListener {
    private static final String OPTIONS_PREFIX = "gui.htmlPreviewPanel.";
    
    private Wandora admin;
    private Options options;
    
    private String locator;
    private JEditorPane htmlPane;
    
    private Color borderColor = Color.DARK_GRAY;

    private JPopupMenu linkPopup = null;
    private MouseEvent mouseEvent = null;
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    /** Creates a new instance of HTMLPreviewPanel */
   
    /** Creates a new instance of WandoraImagePanel */
    public HTML(String locator, Wandora admin) {
        this.locator = locator;
        
        this.admin = admin;
        
        this.addMouseListener(this);
        this.setLayout(new BorderLayout());
        
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setContentType("text/html; charset=ISO-8859-1");
        htmlPane.addHyperlinkListener(this);
        htmlPane.addMouseListener(this);
        htmlPane.setBorder(BorderFactory.createLineBorder(borderColor));
                
        if(admin != null) {
            options = admin.options;
            if(options != null) {

            }
        }
        
        updateLinkMenu();
        this.add(htmlPane, BorderLayout.CENTER);
        repaint();
        revalidate();
        
        //System.out.println("HTML locator is " + locator);
        try {
            htmlPane.setPage(new URL(locator));
        }
        catch(java.io.FileNotFoundException fex) {
            System.out.println("Wandora can't resolve subject locator resource!");
            htmlPane.setContentType("text/html");
            htmlPane.setText("<html><br><center><font face=sanserif size=2>Unable to resolve subject locator resource!</font></center><br></html>");
        }
        catch(Exception e) {
            htmlPane.setContentType("text/html");
            htmlPane.setText("<html><br><center><font face=sanserif size=2>Exception "+e.toString()+" occurred while opening the subject locator resource!</font></center><br></html>");
            e.printStackTrace();
        }
        
        updateMenu();
    }
    
    @Override
    public void stop() {}
    
    
    @Override
    public void finish() {
    }
    
    @Override
    public JPanel getGui() {
        return this;
    }
    
  
    
  public void forkExternal() {
        if(locator != null && locator.length() > 0) {
            System.out.println("Spawning viewer for \""+locator+"\"");
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(locator));
            }
            catch(Exception tme) {
                tme.printStackTrace(); // TODO EXCEPTION
            }
        }
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
                this.setComponentPopupMenu(m);
                if(htmlPane != null) htmlPane.setComponentPopupMenu(m);
            }
        }
        else {
            if(htmlPane != null) htmlPane.setComponentPopupMenu(null);
        }
    }
    
    
    
    
    public JPopupMenu getMenu() {
        WandoraToolSet extractTools = admin.toolManager.getToolSet("extract");
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
                            ((DropExtractor) myTool).dropExtract(s, admin);
                            //System.out.println("extracting " + s);
                            //System.out.println("extractor " + myTool);
                        }
                        catch(Exception exx) { 
                            admin.handleError(exx);
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
                "Make selection base name",
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
        
        if(c.startsWith("Open in external")) {
            forkExternal();
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
                    admin.handleError(e);
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
            save();
        }
        
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        
        
        else if("Make selection display name".equals(c)) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    Topic t = admin.getOpenTopic();
                    if(t != null) {
                        Topic type = admin.showTopicFinder("Select display name language");
                        if(type == null) return;
                        HashSet scope = new HashSet();
                        scope.add(type);
                        scope.add(t.getTopicMap().getTopic(XTMPSI.DISPLAY));
                        t.setVariant(scope, s);
                        admin.doRefresh();
                    }
                }
                catch(Exception e) {
                    admin.handleError(e);
                }
            }
        }
        else if("Make selection base name".equals(c)) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    Topic t = admin.getOpenTopic();
                    if(t != null) {
                        t.setBaseName(s);
                        admin.doRefresh();
                    }
                }
                catch(Exception e) {
                    admin.handleError(e);
                }
            }
        }
        else if("Make selection occurrence...".equals(c)) {
            String s = getSelection();
            if(s != null && s.length() > 0) {
                try {
                    Topic type = admin.showTopicFinder("Select occurrence type");
                    if(type == null) return;
                    Topic scope = admin.showTopicFinder("Select occurrence scope");
                    if(scope == null) return;
                    Topic t = admin.getOpenTopic();
                    if(t != null) {
                        t.setData(type, scope, s);
                        admin.doRefresh();
                    }
                }
                catch(Exception e) {
                    admin.handleError(e);
                }
            }
        }
    }
   
    

    public String getSelection() {
        /*
        Document doc = htmlPane.getDocument();
        String selection = "";
        
        try {
            selection = doc.getText(htmlPane.getSelectionStart(), htmlPane.getSelectionEnd()-htmlPane.getSelectionStart());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("selection == "+ selection);
        */
        return htmlPane.getSelectedText(); //selection;
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    
    
    
    
    public void updateLinkMenu() {
        WandoraToolSet extractTools = admin.toolManager.getToolSet("extract");
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
                            ((DropExtractor) myTool).dropExtract(new String[] { url }, admin);
                            System.out.println("extracting url " + url);
                            System.out.println("extractor " + myTool);
                        }
                        catch(Exception exx) { 
                            admin.handleError(exx);
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
                    linkPopup.show(this, mouseEvent.getX(), mouseEvent.getY());
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
    
    
   
   // ----------------------------------------------------------------- SAVE ---
   
   

    public void save() {
        Wandora w = Wandora.getWandora(this);
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Save XML file");
        try {
            chooser.setSelectedFile(new File(locator.substring(locator.lastIndexOf(File.pathSeparator)+1)));
        }
        catch(Exception e) {}
        if(chooser.open(w,SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
        }
    }
    
    
    
    public void save(File file) {
        if(file != null) {
            try {
                IObox.moveUrl(new URL(locator), file);
            }
            catch(Exception e) {
                System.out.println("Exception '" + e.toString() + "' occurred while saving file '" + file.getPath() + "'.");
            }
        }
    }
}
