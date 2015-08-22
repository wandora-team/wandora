/*
 * Copyright (C) 2015 akivela
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
 */
package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.Util;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.application.gui.simple.SimpleTextPaneResizeable;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */
public class Text implements ActionListener, PreviewPanel {
    String locator = null;
    JPanel ui = null;
    JTextPane textPane = null;
    
    
    public Text(String locator) {
        this.locator = locator;
    }
    
    

    @Override
    public void finish() {

    }

    @Override
    public void stop() {

    }
    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    
    @Override
    public JPanel getGui() {
        if(ui == null) {
            ui = new JPanel();
            ui.setLayout(new BorderLayout(8,8));
            textPane = new JTextPane();
            textPane.setText(getContent(locator));
            textPane.setEditable(false);
            textPane.setCaretPosition(0);
            
            JScrollPane scrollPane = new SimpleScrollPane(textPane);
            
            JPanel textPaneWrapper = new JPanel();
            textPaneWrapper.setLayout(new BorderLayout());
        
            textPaneWrapper.setPreferredSize(new Dimension(640, 400));
            textPaneWrapper.setMaximumSize(new Dimension(640, 400));
            textPaneWrapper.setSize(new Dimension(640, 400));
            textPaneWrapper.add(scrollPane);
            
            JPanel toolbarWrapper = new JPanel();
            toolbarWrapper.add(getJToolBar());
            
            ui.add(textPaneWrapper, BorderLayout.CENTER);
            ui.add(toolbarWrapper, BorderLayout.SOUTH);
        }
        return ui;
    }
    
    
    
    private String getContent(String locator) {
        try {
            return IObox.doUrl(new URL(locator));
        }
        catch(Exception e) {
            
        }
        return "";
    }
    
    
    
    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Save", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
    }
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        
        if(c.startsWith("Open ext")) {
            Util.forkExternalPlayer(locator);
        }
        else if(c.equalsIgnoreCase("Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(c.startsWith("Save")) {
            Util.saveToFile(locator);
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
                        if(lowercaseMimeType.startsWith("text/plain")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".txt")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
}
