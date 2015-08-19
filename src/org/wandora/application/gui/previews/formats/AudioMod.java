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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import static org.wandora.application.gui.previews.Util.startsWithAny;
import org.wandora.application.gui.simple.SimpleTimeSlider;
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
    private SimpleTimeSlider progressBar = null;
    
    
    
    public AudioMod(String locator) {
        super(false);
        this.locator = locator;
        ui = makeUI();
        
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
    }
    
    
    public void createMixer() {
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
        ProgressThread progressThread = null;
        try {
            if(currentMixer == null) {
                createMixer();
            }
            if(currentMixer != null) {
                progressThread = new ProgressThread(currentMixer, progressBar);
                progressThread.start();
                currentMixer.startPlayback();
            }
        }
        catch(Throwable ex) {
            ex.printStackTrace(System.err);
        }
        if(progressThread != null) {
            progressThread.abort();
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
            if(currentMixer != null && currentMixer.isPaused()) {
                currentMixer.pausePlayback();
            }
            else {
                playerThread = new Thread(this);
                playerThread.setDaemon(true);
                playerThread.start();
            }
        }
        else if(startsWithAny(cmd, "Pause")) {
            if(currentMixer != null) {
                currentMixer.pausePlayback();
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
                if(currentMixer.isNotSeeking() && currentMixer.isNotPausingNorPaused()) {
                    long currentPosition = currentMixer.getMillisecondPosition();
                    long forwardPosition = currentPosition + 10000;
                    currentMixer.setMillisecondPosition(forwardPosition);
                }
            }
        }
        else if(startsWithAny(cmd, "Backward")) {
            if(currentMixer != null) {
                if(currentMixer.isNotSeeking() && currentMixer.isNotPausingNorPaused()) {
                    long currentPosition = currentMixer.getMillisecondPosition();
                    long backwardPosition = currentPosition - 10000;
                    currentMixer.setMillisecondPosition(Math.max(0, backwardPosition));
                }
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
    
    
    protected Mixer getMixer() {
        return currentMixer;
    }
    
    
    // ------------------
    
    private JPanel makeUI() {
        JPanel ui = new JPanel();
        
        progressBar = new SimpleTimeSlider();
        progressBar.setString(locator);
        JPanel progressBarContainer = new JPanel();
        progressBarContainer.setLayout(new BorderLayout());
        progressBarContainer.add(progressBar, BorderLayout.CENTER);
        
        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        ui.setLayout(new BorderLayout(8,8));
        ui.add(progressBarContainer, BorderLayout.CENTER);
        ui.add(controllerPanel, BorderLayout.SOUTH);

        progressBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                int mouseValue = progressBar.getValueFor(e);
                if(currentMixer != null && currentMixer.isSeekSupported()) {
                    if(currentMixer.isNotSeeking() && currentMixer.isNotPausingNorPaused()) {
                        currentMixer.setMillisecondPosition(mouseValue*1000);
                        System.out.println("newPosition: "+mouseValue*1000);
                        System.out.println("newPosition2: "+currentMixer.getMillisecondPosition());
                    }
                }
            }
        });
        
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
    

    
    // -------------------------------------------------------------------------
    
    
    
    private class ProgressThread extends Thread {
        private Mixer progressMixer = null;
        private SimpleTimeSlider progressBar = null;
        private boolean isRunning = true;
        
        
        public ProgressThread(Mixer mixer, SimpleTimeSlider bar) {
            progressMixer = mixer;
            progressBar = bar;
            if(progressBar != null) {
                progressBar.setMinimum(0.0);
                progressBar.setValue(0.0);
                if(progressMixer != null) {
                    progressBar.setMaximum(progressMixer.getLengthInMilliseconds() / 1000);
                }
            }
        }
        
        
        
        @Override
        public void run() {
            while(progressMixer != null && progressBar != null && isRunning) {
                if(currentMixer.isNotSeeking()) {
                    long progress = progressMixer.getMillisecondPosition() / 1000;
                    progressBar.setValue((int) progress);
                }
                try {
                    Thread.sleep(100);
                }
                catch(Exception e) {}
            }
            progressBar.setValue(0);
            progressBar.setString(locator);
        }
        
        
        public void abort() {
            isRunning = false;
        }
    }
    
    
    
}
