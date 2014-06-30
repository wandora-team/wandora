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
 * JDICBrowser.java
 *
 * Created on 22.6.2009, 14:33
 *
 */

package org.wandora.application.gui.previews.formats;


import org.wandora.application.gui.previews.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;


import java.util.Map;

import org.jdesktop.jdic.browser.*;
import org.jdic.web.*;
import org.jdic.web.event.BrComponentListener;
import org.jdic.web.event.BrComponentEvent;


/**
 *
 * @author akivela
 */
public class JDICBrowser extends JPanel implements MouseListener, PreviewPanel, BrComponentListener {
    private static final String OPTIONS_PREFIX = "gui.jdicPreviewPanel.";

    private Map<String, String> options;
    private String wwwLocator;
    private WebBrowser webBrowser;
    private BrComponent explorer = null;
    
    
    public JDICBrowser(String wwwLocator, Map<String, String> options) {
        this.wwwLocator = wwwLocator;
        initialize(options);
    }
    
    @Override
    public boolean isHeavy() {
        return true;
    }
    
    
    
    public void initialize(Map<String, String> options) {
        this.options = options;
        try {
            this.setLayout(new BorderLayout());
            BrowserEngineManager mng=BrowserEngineManager.instance();  
            mng.setActiveEngine(BrowserEngineManager.IE);

            if(false) {
                BrComponent.DESIGN_MODE = false;
                explorer = new BrComponent();
                explorer.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent evt) {
                    }
                });
                //explorer.execJSLater("function donothing() {return false;} " + "document.oncontextmenu = donothing;");
                javax.swing.GroupLayout brMainLayout = new javax.swing.GroupLayout(explorer);
                explorer.setLayout(brMainLayout);
                //explorer.setDefaultPaintAlgorithm(BrComponent.PAINT_JAVA);
                explorer.setAutoscrolls(false);
                
                this.add(explorer, BorderLayout.CENTER);

                explorer.setPreferredSize(new Dimension(800, 500));
                explorer.setMinimumSize(new Dimension(100, 500));
                explorer.setURL(wwwLocator);
                explorer.addBrComponentListener(this);
            }
            else {
                this.setBorder(new javax.swing.border.LineBorder(Color.LIGHT_GRAY));
                webBrowser = new WebBrowser();
                webBrowser.setURL(new URL(wwwLocator));
                this.add(webBrowser, BorderLayout.NORTH);
                webBrowser.setPreferredSize(new Dimension(800, 600));
                webBrowser.setMinimumSize(new Dimension(200, 200));
            }
            System.out.println("JDICBrowser initialized successfully!");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        this.addMouseListener(this);
        
        repaint();
        revalidate();
    }

    
    
    
    @Override
    public void stop() {

    }
    
    @Override
    public void finish() {
    }
    
    
    @Override
    public JPanel getGui() {
        return this;
    }
    


    
    
    
    

    
    

    
    
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {
            
        }
    }
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    // -------------------------------------------------------------------------
    
    
    public String sync(BrComponentEvent e) {
        switch(e.getID()){
        case BrComponentEvent.DISPID_WINDOWCLOSING:
            String stValue = e.getValue();
            if(null!=stValue){
                //OLE boolean: -1 - true, 0 - false; params:(bCancel, bIsChildWindow)
                final boolean isChildWindow = (0!=Integer.valueOf(stValue.split(",")[1]));
                javax.swing.SwingUtilities.invokeLater ( new Runnable() {
                        public void run() {

                        }
                });
            }
            break;
        }
        return null;
    }

   
    private void onBrowserPropertyChange(java.beans.PropertyChangeEvent evt) {
        String stPN = evt.getPropertyName();
        if(stPN.equals("navigatedURL")) {}
        else if(stPN.equals("progressBar")) {}
        else if(stPN.equals("securityIcon")) {}

    }
}
