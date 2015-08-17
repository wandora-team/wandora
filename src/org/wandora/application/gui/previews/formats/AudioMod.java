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
 */


package org.wandora.application.gui.previews.formats;

import de.quippy.javamod.main.JavaModMainBase;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.ModContainer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import static org.wandora.application.gui.previews.Util.startsWithAny;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;

/**
 * AudioMod uses Daniel Becker's Javamod player.
 *
 * @author akivela
 */
public class AudioMod extends JavaModMainBase implements PreviewPanel, ActionListener, Runnable {
    private String locator = null;
    private Thread playerThread = null;
    private Mixer currentMixer;
    private JPanel ui = null;
    
    
    
    
    public AudioMod(String locator) {
        super(false);
        this.locator = locator;
        ui = makeUI();
    }
    
    
    public void createMixer() {
        Properties props = new Properties();
        props.setProperty(ModContainer.PROPERTY_PLAYER_ISP, "3");
        props.setProperty(ModContainer.PROPERTY_PLAYER_STEREO, "2");
        props.setProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, "FALSE");
        props.setProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, "FALSE");
        props.setProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, "1");
        props.setProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, "TRUE");
        props.setProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, "16");			
        props.setProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, "48000");
        props.setProperty(ModContainer.PROPERTY_PLAYER_MSBUFFERSIZE, "250");
        MultimediaContainerManager.configureContainer(props);
        
        try {
            MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(new URL(locator));
            currentMixer = multimediaContainer.createNewMixer();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @Override
    public void run() {
        try {
            if(currentMixer == null) {
                createMixer();
            }
            if(currentMixer != null) {
                currentMixer.startPlayback();
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void stop() {
        if(currentMixer != null) {
            currentMixer.stopPlayback();
        }
    }

    @Override
    public void finish() {
        if(currentMixer != null) {
            currentMixer.stopPlayback();
        }
    }

    @Override
    public Component getGui() {
        return ui;
    }

    @Override
    public boolean isHeavy() {
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if(startsWithAny(cmd, "Play")) {
            playerThread = new Thread(this);
            playerThread.setDaemon(true);
            playerThread.start();
        }
        else if(startsWithAny(cmd, "Pause")) {
            if(currentMixer != null) {
                currentMixer.stopPlayback();
            }
        }
        else if(startsWithAny(cmd, "Stop")) {
            if(currentMixer != null) {
                currentMixer.stopPlayback();
                currentMixer = null;
            }
        }
        else if(startsWithAny(cmd, "Forward")) {
            if(currentMixer != null) {
                currentMixer.setMillisecondPosition(Math.min(currentMixer.getLengthInMilliseconds(), currentMixer.getMillisecondPosition()+2000));
            }
        }
        else if(startsWithAny(cmd, "Backward")) {
            if(currentMixer != null) {
                currentMixer.setMillisecondPosition(Math.max(0, currentMixer.getMillisecondPosition()-2000));
            }
        }
        else if(startsWithAny(cmd, "Open ext")) {
            if(locator != null) {
                forkExternalPlayer();
            }
        }
        else if(startsWithAny(cmd, "Copy audio location", "Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(startsWithAny(cmd, "Save")) {
            if(locator != null) {
                
            }
        }
    }
    
    // ------------------
    
    private JPanel makeUI() {
        JPanel ui = new JPanel();
        
        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        ui.setLayout(new BorderLayout(8,8));
        ui.add(controllerPanel, BorderLayout.SOUTH);
        
        return ui;
    }
    
    
    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", UIBox.getIcon(0xf04b), this,
            "Pause", UIBox.getIcon(0xf04c), this,
            "Stop", UIBox.getIcon(0xf04d), this,
            "Backward", UIBox.getIcon(0xf04a), this,
            "Forward", UIBox.getIcon(0xf04e), this,
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Save as", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
    }
    
    
    
    public void forkExternalPlayer() {
        if(locator != null && locator.length() > 0) {
            if(!DataURL.isDataURL(locator)) {
                System.out.println("Spawning viewer for \""+locator+"\"");
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(locator));
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
    
    
    
    // -------------------------------------------------------------------------
    

    public static boolean canView(String url) {
        if(url != null) {
            if(DataURL.isDataURL(url)) {
                try {
                    DataURL dataURL = new DataURL(url);
                    String mimeType = dataURL.getMimetype();
                    if(mimeType != null) {
                        String lowercaseMimeType = mimeType.toLowerCase();
                        if(lowercaseMimeType.startsWith("audio/mod") ||
                           lowercaseMimeType.startsWith("audio/xm") ||
                           lowercaseMimeType.startsWith("audio/s3m")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".mod", ".s3m", ".xm") || startsWithAny(url.toLowerCase(), "mod.")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
}
