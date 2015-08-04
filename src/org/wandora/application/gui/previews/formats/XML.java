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
 * XML.java
 *
 * Created on 12. lokakuuta 2007, 17:14
 *
 */

package org.wandora.application.gui.previews.formats;


import org.wandora.application.gui.previews.*;
import org.wandora.utils.Options;
import org.wandora.utils.IObox;
import org.wandora.utils.ClipboardBox;
import org.wandora.application.gui.simple.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.print.*;

import org.wandora.utils.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;

/**
 *
 * @author akivela
 */
public class XML extends JPanel implements Runnable, MouseListener, ActionListener, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.xmlPreviewPanel.";
    
    Wandora wandora;
    Options options;
    Dimension panelDimensions;
    BufferedImage bgImage;
    String xmlLocator;
    
    public boolean isHeavy() {
        return false;
    }
    
    /** Creates a new instance of XMLPreviewPanel */
    public XML(String xmlLocator) {
        this.xmlLocator = xmlLocator;
        initialize();
        updateXMLMenu();
    }
    
    
    public void initialize() {
        this.wandora = Wandora.getWandora();
        this.addMouseListener(this);
        bgImage = UIBox.getImage("gui/icons/doctype/doctype_xml.png");
        
        if(wandora != null) {
            options = wandora.options;
            if(options != null) {

            }
        }
        panelDimensions = new Dimension(100, 100);
        this.setPreferredSize(panelDimensions);
        this.setMaximumSize(panelDimensions);
        this.setMinimumSize(panelDimensions);
        
        repaint();
        revalidate();
    }

    
    
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(bgImage != null) {
            g.drawImage(bgImage,0,0,this);
        }
    }

    
    @Override
    public void finish() {
    }
    
    @Override
    public void stop() {}
    
    @Override
    public JPanel getGui() {
        return this;
    }
    
    
    
    

    @Override
    public void run() {

    }
    
    
    
    
  public void forkExternal() {
        if(xmlLocator != null && xmlLocator.length() > 0) {
            if(!DataURL.isDataURL(xmlLocator)) {
                System.out.println("Spawning viewer for \""+xmlLocator+"\"");
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(xmlLocator));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                        "Due to Java's security restrictions Wandora can't open the DataURI "+
                        "in external application. Manually copy and paste the locator to browser's "+
                        "address field to view the locator.", 
                        "Can't open the locator in external application",
                        WandoraOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    

    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {

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
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    // -------------------------------------------------------------------------
    
    
    public void updateXMLMenu() {
        if(xmlLocator != null && xmlLocator.length() > 0) {
            this.setComponentPopupMenu(getMenu());
        }
        else {
            this.setComponentPopupMenu(null);
        }
    }
    
    public JPopupMenu getMenu() {
        Object[] menuStructure = new Object[] {
            "Open in external viewer...",
            "Copy location",
            "Save as...",
           
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
            if(xmlLocator != null) {
                ClipboardBox.setClipboard(xmlLocator);
            }
        }
        else if(c.startsWith("Save as")) {
            save();
        }
    }
   
   
   
   // ----------------------------------------------------------------- SAVE ---
   
   

    public void save() {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Save XML file");
        try {
            chooser.setSelectedFile(new File(xmlLocator.substring(xmlLocator.lastIndexOf(File.pathSeparator)+1)));
        }
        catch(Exception e) {}
        if(chooser.open(wandora, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
        }
    }
    
    
    
    public void save(File xmlFile) {
        if(xmlFile != null) {
            try {
                IObox.moveUrl(new URL(xmlLocator), xmlFile);
            }
            catch(Exception e) {
                System.out.println("Exception '" + e.toString() + "' occurred while saving file '" + xmlFile.getPath() + "'.");
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        if(url != null) {
            if(DataURL.isDataURL(url)) {
                try {
                    DataURL dataURL = new DataURL(url);
                    String mimeType = dataURL.getMimetype();
                    if(mimeType != null) {
                        String lowercaseMimeType = mimeType.toLowerCase();
                        if(lowercaseMimeType.startsWith("application/xml") ||
                           lowercaseMimeType.startsWith("text/xml")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".xml")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
}
