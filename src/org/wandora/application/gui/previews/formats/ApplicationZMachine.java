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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import static org.wandora.application.gui.previews.PreviewUtils.endsWithAny;
import static org.wandora.application.gui.previews.PreviewUtils.startsWithAny;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import org.zmpp.swingui.PanelMachineFactory;
import org.zmpp.swingui.ZmppPanel;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.Input;
import org.zmpp.vm.Machine;
import org.zmpp.vm.Output;



/**
 * Uses and is based on Wei-ju Wu's ZMMP (The Z-machine Preservation) Project.
 *
 * @author akivela
 */
public class ApplicationZMachine implements ActionListener, PreviewPanel {
    
    private String locator = null;
    private JPanel ui = null;
    private ZmppPanel gamePanel = null;
    private Machine machine = null;
    
    
    
    
    public ApplicationZMachine(String loc) {
        this.locator = loc;
    }
    
    
    
    public void runStory(String locator) {
        PanelMachineFactory factory = null;
        
        // First get a game factory with the locator. The factory is always
        // PanelMachineFactory but the instantiation varies and depends on the
        // locator. Locator can be either a file, an url or a dataurl.
        try {
            if(DataURL.isDataURL(locator)) {
                DataURL storyDataURL = new DataURL(locator);
                byte[] storyData = storyDataURL.getData();
                if(storyDataURL.getMimetype().contains("application/x-blorb")) {
                    factory = new PanelMachineFactory(storyData); // storyData == blorb
                }
                else {
                    factory = new PanelMachineFactory(storyData, null); 
                }
            }
            else if(locator.startsWith("file:")) {
                File storyFile = new File((new URL(locator)).toURI());
                if(storyFile.isFile() && storyFile.exists()) {
                    String filename = storyFile.getName();
                    if(filename.endsWith("zblorb") || filename.endsWith("zlb")) {
                        factory = new PanelMachineFactory(storyFile); // storyFile == blorb
                    }
                    else {
                        File blorbfile = searchForResources(storyFile);
                        factory = new PanelMachineFactory(storyFile, blorbfile);
                    }
                }
            }
            else {
                if(locator.endsWith("zblorb") || locator.endsWith("zlb")) {
                    factory = new PanelMachineFactory(null, new URL(locator));
                }
                else {
                    factory = new PanelMachineFactory(new URL(locator), null);
                }
            }
        }
        catch(MalformedURLException mfue) {
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                    "Illegal URL used as a Z Machine source.", "Illegal URL", 
                    WandoraOptionPane.WARNING_MESSAGE);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        // Now we should have a valid game factory. Create ui and start the game.
        if(factory != null) {
            try {
                machine = factory.buildMachine();
                gamePanel = factory.getUI();      
                gamePanel.startMachine();
            }
            catch(IOException ioe) {
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                        "Could not read the Z Machine source.", "Read error", 
                        WandoraOptionPane.WARNING_MESSAGE);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else {
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                    "Could not initialize game.", "Story file error", 
                    WandoraOptionPane.WARNING_MESSAGE);
        }
    }
    
    
        
    
    
    /**
     * Tries to find a resource file in Blorb format.
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
        if (blorbfile1.exists()) return blorbfile1;

        File blorbfile2 = new File(blorbpath2);
        if (blorbfile2.exists()) return blorbfile2;

        return null;
    }  
    
    
    
    private JPanel makeUI() {
        JPanel ui = new JPanel();
        
        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        if(gamePanel == null) {
            try {
                runStory(locator);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        JPanel gameWrapperPanel = new JPanel();
        if(gamePanel != null) gameWrapperPanel.add(gamePanel, BorderLayout.CENTER);
        
        ui.setLayout(new BorderLayout(8,8));       
        ui.add(gameWrapperPanel, BorderLayout.CENTER);
        ui.add(controllerPanel, BorderLayout.SOUTH);

        return ui;
    }
    
    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            // "Restart", PreviewUtils.ICON_RESTART, this,
            // "Info", PreviewUtils.ICON_INFO, this,
            "Preferences", PreviewUtils.ICON_CONFIGURE, this,
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Save", PreviewUtils.ICON_SAVE, this,
        }, this);
    }
    
    
    
    
    
    @Override
    public void stop() {
        if(machine != null) {
            System.out.println("Stopping machine!");
            /*
            Input in = machine.getInput();
            if(in != null) {
                in.close();
            }
            Output out = machine.getOutput();
            if(out != null) {
                out.close();
            }
            Cpu cpu = machine.getCpu();
            if(cpu != null) {
                cpu.setRunning(false);
            }
            */
        }
    }

    @Override
    public void finish() {
        if(machine != null) {
            System.out.println("Finishing machine!");
            
            /*
            Input in = machine.getInput();
            if(in != null) {
                in.close();
            }
            Output out = machine.getOutput();
            if(out != null) {
                out.close();
            }
            Cpu cpu = machine.getCpu();
            if(cpu != null) {
                cpu.setRunning(false);
            }
            */
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
                PreviewUtils.forkExternalPlayer(locator);
            }
        }
        else if(startsWithAny(cmd, "Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(startsWithAny(cmd, "Save")) {
            if(locator != null) {
                PreviewUtils.saveToFile(locator);
            }
        }
    }
    

    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "application/x-zmachine",
                    "application/x-zmachine-1",
                    "application/x-zmachine-2",
                    "application/x-zmachine-3",
                    "application/x-zmachine-4",
                    "application/x-zmachine-5",
                    "application/x-zmachine-6",
                    "application/x-zmachine-7",
                    "application/x-zmachine-8",
                    "application/x-blorb" }, 
                new String[] { 
                    "z1", 
                    "z2", 
                    "z3", 
                    "z4", 
                    "z5", 
                    "z6", 
                    "z7", 
                    "z8", 
                    "zblorb", 
                    "zlb"
                }
        );
    }
}
