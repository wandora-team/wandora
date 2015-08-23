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
 */

package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.Util;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import static org.wandora.application.gui.previews.Util.startsWithAny;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.blorb.BlorbStory;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.media.Resources;
import org.zmpp.media.StoryMetadata;
import org.zmpp.swingui.ColorTranslator;
import org.zmpp.swingui.DisplaySettings;
import org.zmpp.swingui.FileSaveGameDataStore;
import org.zmpp.swingui.GameInfoDialog;
import org.zmpp.swingui.GameThread;
import org.zmpp.swingui.JPanelMachineFactory;
import org.zmpp.swingui.LineEditorImpl;
import org.zmpp.swingui.PreferencesDialog;
import org.zmpp.swingui.TextViewport;
import org.zmpp.swingui.Viewport6;
import org.zmpp.swingui.ZmppPanel;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;
import org.zmpp.vmutil.FileUtils;



/**
 * Uses and is based on Wei-ju Wu's ZMMP (The Z-machine Preservation) Project.
 *
 * @author akivela
 */
public class ZMachine implements ActionListener, PreviewPanel {
    
    private String locator = null;
    private JPanel ui = null;
    private ZmppPanel gamePanel = null;
    private Machine machine = null;
    
    
    
    
    public ZMachine(String loc) {
        this.locator = loc;
    }
    
    
    public void runStoryData(byte[] storydata) {
        // Read in the story file
        if (storydata != null) {
            try {
                JPanelMachineFactory factory = new JPanelMachineFactory(storydata);
                machine = factory.buildMachine();
                gamePanel = factory.getUI();      
                gamePanel.startMachine();
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                    String.format("Could not read game.\nReason: '%s'", ex.getMessage()),
                    "Story data error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        else {
            JOptionPane.showMessageDialog(null,
                String.format("The selected story data was illegal"),
                "Story url not found", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void runStoryUrl(URL storyurl) {
        // Read in the story file
        if (storyurl != null) {
            try {
                JPanelMachineFactory factory = new JPanelMachineFactory(storyurl);
                machine = factory.buildMachine();
                gamePanel = factory.getUI();      
                gamePanel.startMachine();
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                    String.format("Could not read game.\nReason: '%s'", ex.getMessage()),
                    "Story file error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        else {
            JOptionPane.showMessageDialog(null,
                String.format("The selected story url '%s' was not found",
                storyurl != null ? storyurl : ""),
                "Story url not found", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    public void runStoryFile(File storyfile) {
        // Read in the story file
        if (storyfile != null && storyfile.exists() && storyfile.isFile()) {
            JPanelMachineFactory factory;
            if(isZblorbSuffix(storyfile.getName())) {
                factory = new JPanelMachineFactory(storyfile);
            }
            else {
                File blorbfile = searchForResources(storyfile);
                factory = new JPanelMachineFactory(storyfile, blorbfile);
            }

            try {
                machine = factory.buildMachine();
                gamePanel = factory.getUI();      
                gamePanel.startMachine();
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                    String.format("Could not read game.\nReason: '%s'", ex.getMessage()),
                    "Story file error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        else {
            JOptionPane.showMessageDialog(null,
                String.format("The selected story file '%s' was not found",
                storyfile != null ? storyfile.getPath() : ""),
                "Story file not found", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    

    private static boolean isZblorbSuffix(String filename) {
        return filename.endsWith("zblorb") || filename.endsWith("zlb");
    }
  
    
    /**
     * Trys to find a resource file in Blorb format.
     * 
     * @param storyfile the storyfile
     * @return the blorb file if one exists or null
     */
    private static File searchForResources(File storyfile) {
        StringTokenizer tok = new StringTokenizer(storyfile.getName(), ".");
        String prefix = tok.nextToken();
        String dir = storyfile.getParent();
        String blorbpath1 = ((dir != null) ? dir + System.getProperty("file.separator") : "")
                            + prefix + ".blb";
        String blorbpath2 = ((dir != null) ? dir + System.getProperty("file.separator") : "")
                            + prefix + ".blorb";

        File blorbfile1 = new File(blorbpath1);
        System.out.printf("does '%s' exist ? -> %b\n", blorbfile1.getPath(), blorbfile1.exists());
        if (blorbfile1.exists()) return blorbfile1;

        File blorbfile2 = new File(blorbpath2);
        System.out.printf("does '%s' exist ? -> %b\n", blorbfile2.getPath(), blorbfile2.exists());
        if (blorbfile2.exists()) return blorbfile2;

        return null;
    }  
    
    
    
    private JPanel makeUI() {
        JPanel ui = new JPanel();
        
        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        if(gamePanel == null) {
            try {
                if(DataURL.isDataURL(locator)) {
                    runStoryData((new DataURL(locator)).getData() );
                }
                else if(locator.startsWith("file:")) {
                    runStoryFile(new File((new URL(locator)).toURI()) );
                }
                else {
                    runStoryUrl(new URL(locator));
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        JPanel gameWrapperPanel = new JPanel();
        // gameWrapperPanel.setBackground(Color.WHITE);
        if(gamePanel != null) gameWrapperPanel.add(gamePanel, BorderLayout.CENTER);
        
        ui.setLayout(new BorderLayout(8,8));       
        ui.add(gameWrapperPanel, BorderLayout.CENTER);
        ui.add(controllerPanel, BorderLayout.SOUTH);

        return ui;
    }
    
    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            //"Restart", UIBox.getIcon(0xf021), this,
            //"Info", UIBox.getIcon(0xf129), this,
            "Preferences", UIBox.getIcon(0xf013), this,
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Save", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
    }
    
    
    
    
    
    @Override
    public void stop() {
        if(machine != null) {
            System.out.println("Stopping machine!");
            //machine.quit();
        }
    }

    @Override
    public void finish() {
        if(machine != null) {
            System.out.println("Finishing machine!");
            //machine.quit();
        }
    }

    @Override
    public Component getGui() {
        if(ui == null) {
            ui = makeUI();
        }
        return ui;
    }

    @Override
    public boolean isHeavy() {
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if(startsWithAny(cmd, "Preferences")) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if(gamePanel != null) {
                        gamePanel.editPreferences(Wandora.getWandora());
                    }
                }
            });
        }
        else if(startsWithAny(cmd, "Info")) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if(gamePanel != null) {
                        gamePanel.aboutGame(Wandora.getWandora());
                    }
                }
            });
        }
        else if(startsWithAny(cmd, "Restart")) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if(machine != null) {
                        machine.restart();
                    }
                }
            });
        }
        else if(startsWithAny(cmd, "Stop")) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if(machine != null) {
                        machine.restart();
                    }
                }
            });
        }
        else if(startsWithAny(cmd, "Open ext")) {
            if(locator != null) {
                Util.forkExternalPlayer(locator);
            }
        }
        else if(startsWithAny(cmd, "Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(startsWithAny(cmd, "Save")) {
            if(locator != null) {
                Util.saveToFile(locator);
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
                        if(lowercaseMimeType.startsWith("application/z1") ||
                           lowercaseMimeType.startsWith("application/z2") ||
                           lowercaseMimeType.startsWith("application/z3") ||
                           lowercaseMimeType.startsWith("application/z4") ||
                           lowercaseMimeType.startsWith("application/z5") ||
                           lowercaseMimeType.startsWith("application/z6") ||
                           lowercaseMimeType.startsWith("application/z7") ||
                           lowercaseMimeType.startsWith("application/z8") ||
                           lowercaseMimeType.startsWith("application/zblorb")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".z1", ".z2", ".z3", ".z4", ".z5", ".z6", ".z7", ".z8", ".zblorb", "zlb")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    
    
}
