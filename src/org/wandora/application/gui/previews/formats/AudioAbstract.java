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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import static org.wandora.application.gui.previews.PreviewUtils.startsWithAny;
import org.wandora.application.gui.simple.SimpleTimeSlider;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import org.wandora.utils.MimeTypes;

/**
 * AudioAbstract uses Daniel Becker's Javamod player.
 * 
 * @author akivela
 */
public abstract class AudioAbstract extends JavaModMainBase implements PreviewPanel, ActionListener {
    
    private String locator = null;
    private PlayerThread playerThread = null;
    private JPanel ui = null;
    private Mixer currentMixer;
    private SimpleTimeSlider progressBar = null;
    private File tmpFile = null;
    
    
    
    public AudioAbstract(String locator) {
        super(false);
        this.locator = locator;
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
        
        if(startsWithAny(cmd, "Play")) {
            if(currentMixer != null && currentMixer.isPaused()) {
                currentMixer.pausePlayback();
            }
            else {
                playerThread = new PlayerThread();
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
                PreviewUtils.forkExternalPlayer(locator);
            }
        }
        else if(startsWithAny(cmd, "Copy audio location", "Copy location")) {
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
    
    
    protected Mixer getMixer() {
        return currentMixer;
    }
    
    
    // ------------------
    
    
    protected JPanel makeUI() {
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
                        // System.out.println("newPosition: "+mouseValue*1000);
                        // System.out.println("newPosition2: "+currentMixer.getMillisecondPosition());
                    }
                }
            }
        });
        
        return ui;
    }
    
    
    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", PreviewUtils.ICON_PLAY, this,
            "Pause", PreviewUtils.ICON_PAUSE, this,
            "Stop", PreviewUtils.ICON_STOP, this,
            "---",
            "Backward", PreviewUtils.ICON_BACKWARD, this,
            "Forward", PreviewUtils.ICON_FORWARD, this,
            "---",
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Save as", PreviewUtils.ICON_SAVE, this,
        }, this);
    }
    

    
    // -------------------------------------------------------------------------
    
    
    
    protected class PlayerThread extends Thread {
        private ProgressThread progressThread = null;
        
        public PlayerThread() {
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
            
            this.setDaemon(true);
            this.start();
        }
        
        
        
        @Override
        public void run() {
            try {
                if(progressThread == null) {
                    progressThread = new ProgressThread(progressBar);
                }
                if(currentMixer == null) {
                    createMixer();
                }
                if(currentMixer != null) {
                    progressThread.setMixer(currentMixer);
                    progressThread.start();
                    currentMixer.startPlayback();
                }
            }
            catch(Exception ex) {
                ui.removeAll();
                JComponent errorPanel = PreviewUtils.previewError(ui, "Error while starting player", ex);
                ex.printStackTrace(System.err);
            }
            if(progressThread != null) {
                progressThread.abort();
                progressThread = null;
            }
            currentMixer = null;
        }
        
        

        public void createMixer() {
            try {
                if(DataURL.isDataURL(locator)) {
                    DataURL dataUrl = new DataURL(locator);
                    tmpFile = dataUrl.createTempFile();
                    if(tmpFile != null) {
                        MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(tmpFile);
                        currentMixer = multimediaContainer.createNewMixer();
                    }
                    else {
                        PreviewUtils.previewError(ui, "Unable to create temporal file for a dataurl.");
                    }
                }
                else if(locator.startsWith("file:")) {
                    MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(new URL(locator));
                    currentMixer = multimediaContainer.createNewMixer();
                }
                else {
                    File tempfile = createTempFile(new URL(locator));
                    MultimediaContainer multimediaContainer = MultimediaContainerManager.getMultimediaContainer(tempfile.toURI().toURL());
                    currentMixer = multimediaContainer.createNewMixer();
                }
            }
            catch(IOException ioe) {
                PreviewUtils.previewError(ui, "IOException occurred while preparing media.", ioe);
            }
            catch(Exception e) {
                PreviewUtils.previewError(ui, "Unable to play audio.", e);
            }
        }
        
        
        private File createTempFile(URL url) throws Exception {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            Wandora.initUrlConnection(con);
            con.setRequestMethod("GET");
            con.setDoOutput(false);
            //con.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            //con.setRequestProperty("Accept","*/*");
            
            String prefix = "wandora" + url.hashCode();
            String mimetype = null;
            String suffix = null;
            mimetype = MimeTypes.getMimeType(url);
            if(mimetype != null) {
                suffix = MimeTypes.getExtension(mimetype);
            }
            
            if(suffix == null) {
                mimetype = con.getContentType();
                suffix = MimeTypes.getExtension(mimetype);
            }
            
            //System.out.println("mimetype: "+mimetype);
            //System.out.println("suffix: "+suffix);
            
            if(suffix == null) suffix = "tmp";
            if(!suffix.startsWith(".")) suffix = "."+suffix;
            String tmpdir = System.getProperty("java.io.tmpdir");
                    
            File tempFile = new File(tmpdir + File.separator + prefix + suffix);
            
            //System.out.println("tempFile: "+tempFile.getAbsolutePath());
            
            if(!tempFile.exists()) {
                tempFile.deleteOnExit();
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] chunk = new byte[128];
                int bytesRead;
                int contentLength = con.getContentLength();
                int totalBytesRead = 0;
                InputStream stream = con.getInputStream();

                try {
                    while ((bytesRead = stream.read(chunk)) > 0) {
                        outputStream.write(chunk, 0, bytesRead);
                        if(progressThread != null) {
                            totalBytesRead = totalBytesRead + bytesRead;
                            progressThread.setProgress("Downloading...", totalBytesRead, contentLength);
                        }
                    }
                    stream.close();
                }
                catch(Exception e) {
                    stream.close();
                    throw e;
                }

                byte[] bytes = outputStream.toByteArray();

                if(bytes != null && bytes.length > 0) {
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    try {
                        fos.write(bytes);
                        fos.close();
                    }
                    catch(Exception e) {
                        fos.close();
                        throw e;
                    }
                }
            }
            return tempFile;
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected class ProgressThread extends Thread {
        private Mixer progressMixer = null;
        private SimpleTimeSlider progressBar = null;
        private boolean isRunning = true;
        
        
        public ProgressThread(SimpleTimeSlider bar) {           
            progressBar = bar;
            if(progressBar != null) {
                progressBar.setMinimum(0.0);
                progressBar.setValue(0.0);
            }
        }
        
        
        public void setMixer(Mixer mixer) {
            progressMixer = mixer;
            if(progressBar != null) {
                progressBar.setValue(0.0);
                if(progressMixer != null) {
                    progressBar.setMaximum(progressMixer.getLengthInMilliseconds() / 1000);
                }
            }
        }
        
        
        public void setProgress(String text, int value, int maxValue) {
            progressBar.setProgress(text, 0, value, maxValue);
        }
        
        
        
        @Override
        public void run() {
            long progress = 0;
            while(progressMixer != null && progressBar != null && isRunning) {
                try {
                    if(progressMixer.isNotSeeking()) {
                        progress = progressMixer.getMillisecondPosition() / 1000;
                        progressBar.setValue((int) progress);
                    }
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
