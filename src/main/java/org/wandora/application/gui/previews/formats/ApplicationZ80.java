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
 */


package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.application.gui.previews.formats.applicationz80.Qaop;
import org.wandora.application.tools.extractors.files.SimpleFileExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.Base64;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class ApplicationZ80 implements ActionListener, PreviewPanel, ComponentListener {

    private String locator = null;
    private Qaop qaop = null;
    private JPanel qaopWrapper = null;
    private JPanel ui = null;
    
    
    private boolean isPaused = false;
    private boolean isMuted = false;
    private int sizeScaler = 1;
    
    

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
                qaopWrapper = new JPanel();
                if(qaop == null) {
                    HashMap params = new HashMap();
                    if(DataURL.isDataURL(locator)) {
                        params.put("load", new DataURL(locator).toExternalForm(Base64.DONT_BREAK_LINES));
                    }
                    else {
                        params.put("load", locator);
                    }
                    params.put("focus", "1");
                    qaop = new Qaop(params);
                    qaop.addComponentListener(this);
                    qaopWrapper.add(qaop);
                }

                JPanel toolbarWrapper = new JPanel();
                toolbarWrapper.add(getJToolBar());

                ui.add(qaopWrapper, BorderLayout.CENTER);
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
            getMenuButton(),
        }, this);
    }
    
    
    
    
    public JButton getMenuButton() {
        final JButton button = UIBox.makeDefaultButton();
        button.setIcon(UIBox.getIcon(0xf0c9));
        button.setToolTipText("Sinclair ZX preview options.");
        button.setBorder(null);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getOptionsMenu().show(button, e.getX(), e.getY());
            }

        });
        return button;
    }
    
    
    
    
    private static final Icon selectedIcon = UIBox.getIcon("gui/icons/checked2.png");
    private static final Icon unselectedIcon = UIBox.getIcon("gui/icons/empty.png");
    
    public JPopupMenu getOptionsMenu() {
        Object[] menuStruct = new Object[] {
            "Open subject locator in ext", PreviewUtils.ICON_OPEN_EXT,
            "Copy subject locator", PreviewUtils.ICON_COPY_LOCATION,
            "Save subject locator resource...", PreviewUtils.ICON_SAVE,
            "---",
            "Reify snapshot",
            "Reify screen capture",
            "---",
            "Display size", new Object[] {
                "1x", sizeScaler == 1 ? selectedIcon : unselectedIcon,
                "2x", sizeScaler == 2 ? selectedIcon : unselectedIcon,
            },
            "Mute", isMuted ? selectedIcon : unselectedIcon,
            "---",
            "Pause", isPaused ? selectedIcon : unselectedIcon,
            "Reset",
            "---",
            "About"
        };
        JPopupMenu optionsPopup = UIBox.makePopupMenu(menuStruct, this);
        return optionsPopup;
    }

    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        if("Open subject locator in ext".equalsIgnoreCase(c)) {
            PreviewUtils.forkExternalPlayer(locator);
        }
        else if("Copy subject locator".equalsIgnoreCase(c)) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if("Save subject locator resource...".equalsIgnoreCase(c)) {
            PreviewUtils.saveToFile(locator);
        }
        
        
        
        else if("Reify snapshot".equalsIgnoreCase(c)) {
            DataURL snapshot = makeSnapshot();
            if(snapshot != null) {
                saveToTopic(snapshot);
            }
        }
        else if("Reify screen capture".equalsIgnoreCase(c)) {
            DataURL screenCapture = makeScreenCapture();
            if(screenCapture != null) {
                saveToTopic(screenCapture);
            }
        }     

        else if("Type text...".equalsIgnoreCase(c)) {

        }    

        
        else if("Enter a special key...".equalsIgnoreCase(c)) {

        }
        
        
        
        else if("1x".equalsIgnoreCase(c)) {
            setScaling(1);
        }
        else if("2x".equalsIgnoreCase(c)) {
            setScaling(2);
        }
        else if("Display size".equalsIgnoreCase(c)) {
            sizeScaler += 1;
            if(sizeScaler > 2) sizeScaler = 1;
            setScaling(sizeScaler);
        }
        
        
        else if("Mute".equalsIgnoreCase(c)) {
            isMuted = !isMuted;
            qaop.mute(isMuted);
        }

        
        else if("Reset".equalsIgnoreCase(c)) {
            qaop.reset();
        }
        
        else if("Pause".equalsIgnoreCase(c)) {
            isPaused = !isPaused;
            qaop.pause(isPaused);
        }
        else if("Resume".equalsIgnoreCase(c)) {
            isPaused = false;
            qaop.pause(isPaused);
        }
        
        else if("About".equalsIgnoreCase(c)) {
            StringBuilder aboutBuilder = new StringBuilder("");
            aboutBuilder.append("Wandora's Sinclair ZX emulation support is based on Qaop - ZX Spectrum emulator by Jan Bobrowski ");
            aboutBuilder.append("distributed under the license of GNU GPL.");

            qaop.pause(true);
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), aboutBuilder.toString(), "About Sinclair ZX emulation");
            qaop.pause(false);
        }
    }
    
    
    
    
    
    private void setScaling(int scale) {
        sizeScaler = scale;
        qaop.setScreenSize(scale);
        qaop.validate();
        qaop.repaint();
    }
    
    

    // -------------------------------------------------------------------------
    
    
    
    private DataURL makeSnapshot() {
        try {
            return new DataURL(qaop.save());
        } 
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    private DataURL makeScreenCapture() {
        try {
            BufferedImage screenCaptureImage = new BufferedImage(qaop.getPreferredSize().width, qaop.getPreferredSize().height, BufferedImage.TYPE_INT_RGB); 
            Graphics screenCaptureGraphics = screenCaptureImage.createGraphics();
            qaop.paintAll(screenCaptureGraphics);
            DataURL screenCaptureDataUrl = null;
            screenCaptureDataUrl = new DataURL(screenCaptureImage);
            return screenCaptureDataUrl;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    private void saveToTopic(DataURL dataUrl) {
        Wandora wandora = Wandora.getWandora();
        try {
            TopicMap topicMap = wandora.getTopicMap();

            SimpleFileExtractor simpleFileExtractor = new SimpleFileExtractor();
            simpleFileExtractor._extractTopicsFrom(dataUrl.toExternalForm(), topicMap);

            wandora.doRefresh();
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
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
    
    private void refreshSize() {
        if(qaopWrapper != null) {
            qaopWrapper.setPreferredSize(qaop.getPreferredSize());
            qaopWrapper.setSize(qaop.getPreferredSize());
            qaopWrapper.revalidate();
            ui.validate();
        }
    }
    
    
    @Override
    public void componentResized(ComponentEvent e) {
        refreshSize();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        refreshSize();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        refreshSize();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        
    }
    
}
