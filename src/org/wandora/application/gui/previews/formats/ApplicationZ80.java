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
 */


package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.application.gui.previews.formats.applicationz80.Qaop;
import org.wandora.utils.ClipboardBox;

/**
 *
 * @author akivela
 */
public class ApplicationZ80 implements ActionListener, PreviewPanel, ComponentListener {

    private String locator = null;
    private Qaop qaop = null;
    private JPanel ui = null;
    
    
    

    public ApplicationZ80(String locator) {
        this.locator = locator;
    }
    
    
    

    @Override
    public void stop() {
        if(qaop != null) {
            qaop.destroy();
            qaop = null;
        }
    }
    

    @Override
    public void finish() {
        if(qaop != null) {
            qaop.destroy();
            qaop = null;
        }
    }

    
    
    @Override
    public Component getGui() {
        if(ui == null) {
            ui = new JPanel();
            ui.setLayout(new BorderLayout(8,8));
            ui.addComponentListener(this);

            try {
                if(qaop == null) {
                    HashMap params = new HashMap();
                    params.put("load", locator);
                    params.put("focus", "1");
                    qaop = new Qaop(params);
                }

                JPanel toolbarWrapper = new JPanel();
                toolbarWrapper.add(getJToolBar());

                ui.add(qaop, BorderLayout.CENTER);
                ui.add(toolbarWrapper, BorderLayout.SOUTH);
            }
            catch(Exception e) {
                PreviewUtils.previewError(ui, "Can't initialize text viewer. Exception occurred.", e);
            }
        }
        return ui;
    }

    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Save", PreviewUtils.ICON_SAVE, this,
        }, this);
    }
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        
        if(c.startsWith("Open ext")) {
            PreviewUtils.forkExternalPlayer(locator);
        }
        else if(c.equalsIgnoreCase("Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(c.startsWith("Save")) {
            PreviewUtils.saveToFile(locator);
        }
    }
    
    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "application/x.zx.",
                    "application/x-spectrum-",
                    "application/x.spectrum."
                },
                new String[] { 
                    "z80",
                    "slt",
                    "tap",
                    "sna",
                    "rom",
                    
                    "z80.gz",
                    "slt.gz",
                    "tap.gz",
                    "sna.gz",
                    "rom.gz"
                }
        );
    }
    
    
    // -------------------------------------------------------------------------
    

    @Override
    public void componentResized(ComponentEvent e) {
        if(ui != null) {
            ui.repaint();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if(ui != null) {
            ui.repaint();
        }
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if(ui != null) {
            ui.repaint();
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        
    }
    
}
