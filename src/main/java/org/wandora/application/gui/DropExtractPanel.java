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
 * DropExtractPanel.java
 *
 * Created on 8.6.2006, 15:25
 */

package org.wandora.application.gui;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wandora.application.*;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.application.tools.*;
import org.wandora.application.tools.extractors.*;




/**
 *
 * @author  akivela
 */
public class DropExtractPanel extends JPanel implements ComponentListener, ActionListener, MouseListener, DropTargetListener, DragGestureListener, WandoraToolLogger {

	private static final long serialVersionUID = 1L;
	
	private Wandora wandora = null;
    private WandoraTool tool = null;
    private DropTarget dt;
    private JPopupMenu popup = null;
    private WandoraToolSet extractTools = null;
    
    
    private Color mouseOverColor = new Color(0,0,0);
    private Color mouseOutColor = new Color(102,102,102);

    private boolean forceStop = false;
    
    
    
    /** Creates new form DropExtractPanel */
    public DropExtractPanel() {
        this.wandora = Wandora.getWandora();
        initComponents();
        extractorPanel.addMouseListener(this);
        addComponentListener(this);
        updateMenu();
        dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        String toolName = wandora.options.get("dropExtractor.currentTool");
        if(toolName == null) {
            toolName = "Simple File Extractor";
        }
        if(toolName != null) {
            setTool(toolName);
        }
        try {
            tabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    // System.out.println("Tab: " + tabbedPane.getSelectedIndex());
                    setCurrentPanel(tabbedPane.getSelectedComponent());
                }
            });
            setCurrentPanel(extractorPanel);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    

    private void setCurrentPanel(Component p) {
        if(p != null) {
            if(p.equals(extractorPanel)) {
                logTextPane.setText("");
            }
            else if(p.equals(loggerPanel)) {
                logTextPane.setText(log.toString());
            }
            this.revalidate();
        }
    }
    
    
    
    public void setTool(String toolName) {
        forceStop = false;
        WandoraTool t = extractTools.getToolForName(toolName);
        if(t == null) t = extractTools.getToolForRealName(toolName);
        if(t != null) {
            setTool(t, toolName);
        }
        else {
            extractorNameLabel.setText("No tool available");
        }
    }
    
    
    public void setTool(WandoraTool tool, String toolName) {
        forceStop = false;
        this.tool = tool;
        if(tool != null) {
            extractorNameLabel.setText(toolName);
            //extractorNameLabel.setToolTipText(Textbox.makeHTMLParagraph(tool.getDescription(), 40));
        }
        wandora.options.put("dropExtractor.currentTool", toolName);
    }
    
    
    public void updateMenu() {
        this.setComponentPopupMenu(getPopupMenu());
    }
    

    public JPopupMenu getPopupMenu() {
        extractTools = wandora.toolManager.getToolSet("extract");
        Object[] menuItems = getPopupMenu(extractTools);
        popup = UIBox.makePopupMenu(menuItems, this);
        return popup;
    }
    
    
    public Object[] getPopupMenu(WandoraToolSet tools) {
        final ActionListener popupListener = this;
        return tools.getAsObjectArray(
            tools.new ToolFilter() {
                @Override
                public boolean acceptTool(WandoraTool tool) {
                    return (tool instanceof DropExtractor);
                }
                @Override
                public Object[] addAfterTool(WandoraTool tool) {
                    return new Object[] { tool.getIcon(), popupListener };
                }
             }
        );
    }
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new SimpleTabbedPane();
        extractorPanel = new javax.swing.JPanel();
        centeringPanel = new javax.swing.JPanel();
        extractorNameLabel = new javax.swing.JLabel();
        iconLabel = new javax.swing.JLabel();
        infoPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        loggerPanel = new javax.swing.JPanel();
        jScrollPane1 = new SimpleScrollPane();
        logTextPane = new SimpleTextPane();

        setLayout(new java.awt.GridBagLayout());

        extractorPanel.setBackground(new java.awt.Color(255, 255, 255));
        extractorPanel.setLayout(new java.awt.GridBagLayout());

        centeringPanel.setBackground(new java.awt.Color(255, 255, 255));
        centeringPanel.setLayout(new java.awt.GridBagLayout());

        extractorNameLabel.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        extractorNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        extractorNameLabel.setText("No extractor selected");
        centeringPanel.add(extractorNameLabel, new java.awt.GridBagConstraints());

        iconLabel.setIcon(org.wandora.application.gui.UIBox.getIcon("gui/drop_extract.gif"));
        centeringPanel.add(iconLabel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        extractorPanel.add(centeringPanel, gridBagConstraints);

        infoPanel.setBackground(new java.awt.Color(255, 255, 255));
        infoPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        infoLabel.setForeground(new java.awt.Color(102, 102, 102));
        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setText("<html><p align=\"center\">Drop extractor applies selected \nextractor to dropped files/text. First, select \nextractor in the popup menu. Then, drop a file/text here to start \nextraction.</p></html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        infoPanel.add(infoLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        extractorPanel.add(infoPanel, gridBagConstraints);

        tabbedPane.addTab("Extract", extractorPanel);

        loggerPanel.setLayout(new java.awt.GridBagLayout());

        logTextPane.setBorder(null);
        logTextPane.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        logTextPane.setFocusable(false);
        jScrollPane1.setViewportView(logTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        loggerPanel.add(jScrollPane1, gridBagConstraints);

        tabbedPane.addTab("Log", loggerPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centeringPanel;
    private javax.swing.JLabel extractorNameLabel;
    private javax.swing.JPanel extractorPanel;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane logTextPane;
    private javax.swing.JPanel loggerPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String toolName = actionEvent.getActionCommand();
        System.out.println("action performed in Drop extract panel");
        setTool(toolName);
    }
    

    
    
    // ------------------------------------------------------------- mouse -----
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(popup != null && !popup.isVisible()) {
            popup.show(this, mouseEvent.getX(), mouseEvent.getY());
        }
    }    
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }    
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        if(popup != null && !popup.isVisible()) {
            popup.show(this, mouseEvent.getX(), mouseEvent.getY());
        }
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    

    // --------------------------------------------------------------- dnd -----
    
    @Override
    public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        extractorNameLabel.setForeground(mouseOverColor);
        iconLabel.setIcon(UIBox.getIcon("gui/drop_extract_on.gif"));
        this.revalidate();
    }
    
    
    @Override
    public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
        extractorNameLabel.setForeground(mouseOutColor);
        iconLabel.setIcon(UIBox.getIcon("gui/drop_extract.gif"));
        this.revalidate();
    }
    
    
    @Override
    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {

    }
    
 
    
    private void setLogger(WandoraTool tool) {
        tool.setToolLogger(this);
    }
    
    
    
    @Override
    public void drop(final java.awt.dnd.DropTargetDropEvent e) {
        try {

            final DropExtractPanel dropExtractPanel = this;
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            DataFlavor htmlFlavor = new DataFlavor("text/html; class=java.lang.String");
            
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable tr = e.getTransferable();
            
            java.util.List<File> fileListTransfer = null;
            if(tr.isDataFlavorSupported(fileListFlavor)) {
                fileListTransfer = (java.util.List<File>) tr.getTransferData(fileListFlavor);
            }
            final java.util.List<File> files = fileListTransfer;
            
            Collection<URI> uriListTransfer = null;
            if(tr.isDataFlavorSupported(uriListFlavor)) {
                String urisString = (String) tr.getTransferData(uriListFlavor);
                uriListTransfer = getURIs(urisString);
            }
            final Collection<URI> uris = uriListTransfer;
            
            String stringTransfer = null;
            if(tr.isDataFlavorSupported(stringFlavor)) {
                stringTransfer = (String) tr.getTransferData(stringFlavor);
            }
            final String string = stringTransfer;
            
            
            String htmlTransfer = null;
            if(tr.isDataFlavorSupported(htmlFlavor)) {
                htmlTransfer = (String) tr.getTransferData(htmlFlavor);
            }
            final String html = htmlTransfer;
            
            byte[] dataTransfer = null;
            if(tool instanceof AbstractExtractor) {
                AbstractExtractor atool = (AbstractExtractor) tool;
                String[] contentTypes = atool.getContentTypes();
                for(String contentType : contentTypes) {
                    DataFlavor contentTypeFlavor = new DataFlavor(contentType+"; class=java.lang.String");
                    if(tr.isDataFlavorSupported(contentTypeFlavor)) {
                        dataTransfer = (byte[]) tr.getTransferData(contentTypeFlavor);
                        break;
                    }
                }
            }
            final byte[] data = dataTransfer;
            
            
            /**
             * Actual drop extraction is handled in a separate thread. Thread
             * is required because drop-thread can't use blocking swing
             * dialogs properly (in single-thread systems).
             */
            Thread dropThread = new Thread() {
                
                public void run() {
                    try {
                        if(tool != null) {
                            setLogger(tool);
                            if(tool instanceof DropExtractor) {
                                System.out.println("Drop!");
                                
                                DropExtractor dropTool = (DropExtractor) tool;
                                
                                forceStop = false;

                                if(files != null) {
                                    dropTool.dropExtract(files.toArray(new File[files.size()]));
                                }
                                else if(uris != null) {
                                    Collection<File> fileURIs = substractFileURIs(uris);
                                    if(fileURIs != null) {
                                        dropTool.dropExtract(files.toArray(new File[files.size()]));
                                    }
                                    dropTool.dropExtract(getURIStrings(uris).toArray(new String[uris.size()]));
                                }
                                else if(data != null) {
                                    // dropTool.dropExtract(data);
                                }
                                else if(html != null) {
                                    dropTool.dropExtract(html);
                                }
                                else if(string != null) {
                                    dropTool.dropExtract(string);
                                }
                            }
                            else {
                                WandoraOptionPane.showMessageDialog(wandora, "Selected tool does not support drag and drop feature!", "Drag'n'drop not supported", WandoraOptionPane.WARNING_MESSAGE);
                            }
                        }
                        else {
                            WandoraOptionPane.showMessageDialog(wandora, "No extractor selected. Can't extract. Select extractor first.", "No extractor selected", WandoraOptionPane.WARNING_MESSAGE);
                        }
                    }

                    catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    catch(Error err) {
                        err.printStackTrace();
                    }
                    extractorNameLabel.setForeground(mouseOutColor);
                    iconLabel.setIcon(UIBox.getIcon("gui/drop_extract.gif"));
                    dropExtractPanel.revalidate();
                }
            };
            dropThread.start();
            e.dropComplete(true);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        catch(Error err) {
            err.printStackTrace();
        }
    }
    
    @Override
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

    @Override
    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent dragGestureEvent) {
    }    
    
    
    
    public Collection<URI> getURIs(String urisString) {
        String[] split = urisString.split("\n");
        Collection<URI> uris = new ArrayList<URI>();
        for (String splitPart : split) {
            try {
                URI u = new URI(splitPart.trim());
                if(u.getScheme()==null) continue;
                uris.add(u);
            }
            catch(java.net.URISyntaxException ue) {}
        }
        return uris;
    }
    
    
    public Collection<String> getURIStrings(Collection<URI> uris) {
        if(uris == null) return null;
        Collection<String> uriStrings = new ArrayList<String>();
        for(URI uri : uris) {
            try {
                uriStrings.add(uri.toURL().toExternalForm());
            }
            catch(Exception e) {}
        }
        return uriStrings;
    }

    public Collection<File> substractFileURIs(Collection<URI> uris) {
        if(uris == null) return null;
        Collection<File> files = new ArrayList<>();
        Collection<URI> urisCopy = new ArrayList<>();
        urisCopy.addAll(uris);
        for(URI uri : urisCopy) {
            try {
                if("file".equals(uri.getScheme())) {
                    files.add(new File(uri.toURL().getFile()));
                    uris.remove(uri);
                }
            }
            catch(Exception e) {}
        }
        return files.isEmpty() ? null : files;
    }
    
    // --------------------------------------------------------- tool logger ---
    
    // DropExtractorPanel overrides logger in used tool as the default logger
    // usually popups annoying messages that we don't want to show the user
    // during drop aextraction.

    
    private StringBuilder log = new StringBuilder("");
    

    @Override
    public void hlog(String message) {
        dolog(message);
    }

    @Override
    public void log(String message) {
        dolog(message);
    }

    @Override
    public void log(String message, Exception e) {
        dolog(message);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        dolog(sw.getBuffer().toString());
    }

    @Override
    public void log(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        dolog(sw.getBuffer().toString());
    }

    @Override
    public void log(Error e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        dolog(sw.getBuffer().toString());
    }

    @Override
    public void setProgress(int n) {
        
    }

    @Override
    public void setProgressMax(int maxn) {
        
    }

    @Override
    public void setLogTitle(String title) {
        
    }

    @Override
    public void lockLog(boolean lock) {
        
    }

    @Override
    public String getHistory() {
        return "";
    }

    @Override
    public void setState(int state) {
        
    }

    @Override
    public int getState() {
       return WandoraToolLogger.EXECUTE;
    }

    @Override
    public boolean forceStop() {
        return forceStop;
    }
    
    
    private void dolog(String str) {
        if(str.length() > 50000) {
            str = str.substring(str.indexOf('\n'));
        }
        log.append("\n").append(str);
    }
    
    

    // -------------------------------------------------------------------------
    
    
    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
    
    
    public void handleComponentEvent(ComponentEvent e) {
        try {
            Dimension size = getSize();
            Component c = this.getParent().getParent().getParent();
            if(c != null) {
                if(!(c instanceof JScrollPane)) {
                    size = c.getSize();
                }
                if(!size.equals(getSize())) {
                    //System.out.println("new size treemapcomponent: "+size);
                    setPreferredSize(size);
                    setMinimumSize(size);
                    setSize(size);
                }
            }
            revalidate();
            repaint();
        }
        catch(Exception ex) {
            // SKIP
        }
    }
    
    
}
