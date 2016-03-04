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
 * 
 * AudioWav.java
 *
 * Created on 29. toukokuuta 2006, 14:33
 *
 */

package org.wandora.application.gui.previews.formats;




import org.wandora.application.gui.previews.*;
import org.wandora.utils.ClipboardBox;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;

import java.util.Map;
import org.wandora.application.gui.*;
import org.wandora.application.*;

import javax.sound.sampled.*;
import static org.wandora.application.gui.previews.PreviewUtils.startsWithAny;
import org.wandora.utils.DataURL;



/**
 *
 * @author akivela
 */
public class AudioWav extends JPanel implements Runnable, MouseListener, ActionListener, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.audioSamplePreviewPanel.";

    private Map<String, String> options;
    private String audioLocator;
    private boolean isPlaying = false;
    private SourceDataLine player = null;
    private JPanel wrapperPanel = null;
    private WaveformPanel waveformPanel = null;
    private long frameLength = 0;
    
    
    /** Creates a new instance of AudioSample */
    public AudioWav(String audioLocator) {
        this.audioLocator = audioLocator;
        initialize();
    }
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    public void initialize() {
        this.options = Wandora.getWandora().getOptions().asMap();

        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);

        waveformPanel = new WaveformPanel(audioLocator);
        this.addMouseListener(this);
        this.setLayout(new BorderLayout());
        this.add(waveformPanel, BorderLayout.CENTER);
        
        wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BorderLayout(8,8));
        wrapperPanel.add(this, BorderLayout.CENTER);
        wrapperPanel.add(controllerPanel, BorderLayout.SOUTH);
        
        repaint();
        revalidate();
        updateAudioMenu();
    }


    @Override
    public void finish() {
        isPlaying = false;
       if(waveformPanel != null) {
            waveformPanel.stop();
        }
        if(player != null) {
            player.stop();
            player.flush();
        }
    }

    @Override
    public void stop() {
        isPlaying = false;
        if(waveformPanel != null) {
            waveformPanel.stop();
        }
        if(player != null) {
            player.stop();
            player.flush();
        }
    }
    
    @Override
    public JPanel getGui() {
        return wrapperPanel;
    }
    
    

    @Override
    public void run() {
        try {
            isPlaying = true;
            playSample(audioLocator);
        }
        catch (MalformedURLException e)  { e.printStackTrace();  }
        catch (IOException e)  { e.printStackTrace();  }
        catch (LineUnavailableException e)  { e.printStackTrace();  }
        catch (UnsupportedAudioFileException e) { e.printStackTrace();  }
        
        catch (Exception e) { e.printStackTrace(); }
        isPlaying = false;
    }

    
    private void playSample(String audioLocator) throws Exception {
        AudioInputStream audioStream = null;
        if(DataURL.isDataURL(audioLocator)) {
            DataURL dataURL = new DataURL(audioLocator);
            audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(dataURL.getData()));
        }
        else {
            URL audioURL = new URL(audioLocator);
            audioStream = AudioSystem.getAudioInputStream(audioURL);
        }
        AudioFormat format = audioStream.getFormat();
        frameLength = audioStream.getFrameLength();

        // Create line
        SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        player = (SourceDataLine) AudioSystem.getLine(info);
        player.addLineListener((LineListener) waveformPanel);
        player.open(format);
        player.start();

        byte[] audioBuffer = new byte[player.getBufferSize()];
        int numRead = 0;
        int offset = 0;

        while(isPlaying && (numRead = audioStream.read(audioBuffer)) >= 0) {
            offset = 0;
            while (offset < numRead) {
                offset += player.write(audioBuffer, offset, numRead-offset);
            }
        }
        player.drain();
        player.stop();
    }
    
    
    public void play() {
        if(!isPlaying) {
            if(audioLocator != null && audioLocator.length() > 0) {
                Thread audioThread = new Thread(this);
                audioThread.start();
            }
        }
    }
    
    
    public long getFramePosition() {
        if(player != null && isPlaying) {
            int fp = player.getFramePosition();
            return fp;
        }
        return 0;
    }
    
    
    public long getFrameLength() {
        return frameLength;
    }
    
    

    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {
            play();
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
    
    
    public void updateAudioMenu() {
        if(audioLocator != null && audioLocator.length() > 0) {
            this.setComponentPopupMenu(getImageMenu());
        }
        else {
            this.setComponentPopupMenu(null);
        }
    }
    
    public JPopupMenu getImageMenu() {
        Object[] menuStructure = new Object[] {
            "Play",
            "Stop",
            "---",
            "Open in external player...",
            "---",
            "Copy audio location",
            "---",
            "Save audio as...",
           
        };
        return UIBox.makePopupMenu(menuStructure, this);
    }
    
    
    private JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", PreviewUtils.ICON_PLAY, this,
            // "Pause", PreviewUtils.ICON_PAUSE, this,
            "Stop", PreviewUtils.ICON_STOP, this,
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Save as", PreviewUtils.ICON_SAVE, this,
        }, this);
    }
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        if(startsWithAny(c, "Play")) {
            play();
        }
        else if(startsWithAny(c, "Stop")) {
            isPlaying = false;
            if(player != null) {
                player.drain();
                player.stop();
            }
        }
        else if(startsWithAny(c, "Open in external", "Open ext")) {
            PreviewUtils.forkExternalPlayer(audioLocator);
        }
        else if(startsWithAny(c, "Copy audio location", "Copy location")) {
            if(audioLocator != null) {
                ClipboardBox.setClipboard(audioLocator);
            }
        }
        else if(startsWithAny(c, "Save")) {
            PreviewUtils.saveToFile(audioLocator);
        }
    }

    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "audio/x-aiff",
                    "audio/basic",
                    "audio/x-wav"
                }, 
                new String[] { 
                    "aif", 
                    /*"mp3", */
                    "wav", 
                    "au"
                }
        );
    }
    
   
    // -------------------------------------------------------------------------
    // ------------------------------------------------------- WaveformPanel ---
    // -------------------------------------------------------------------------

    
    
    public class WaveformPanel extends JPanel implements Runnable, LineListener, ComponentListener {    
        private int[][] waveformData = null;
        private int[][][] waveformView = null;
        private String audioLocator = null;
        private Font infoFont = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
        private Thread waveformThread = null;
        private long framePosition = 0;
        private long frameLength = 0;
        private AudioFormat format = null;
        private boolean isRunning = false;
        private boolean requiresRefresh = true;
        
        
	public WaveformPanel(String audioLocator) {
            this.addComponentListener(this);
            this.audioLocator = audioLocator;
            isRunning = true;
            waveformThread = new Thread(this);
            waveformThread.start();
	}
        
        
        
        public void stop() {
            isRunning = false;
        }
        
        
        
        @Override
        public void run() {
            try {
                AudioInputStream audioStream = null;
                if(DataURL.isDataURL(audioLocator)) {
                    DataURL dataURL = new DataURL(audioLocator);
                    audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(dataURL.getData()));
                }
                else {
                    URL audioURL = new URL(audioLocator);
                    audioStream = AudioSystem.getAudioInputStream(audioURL);
                }
                format = audioStream.getFormat();
                frameLength = audioStream.getFrameLength();
                byte[] waveformRawData = new byte[(int) frameLength*format.getFrameSize()];
                int bytesRead = audioStream.read(waveformRawData);
                waveformData = sortAudioBytes(waveformRawData, format);

                if(audioStream != null) {
                    audioStream.close();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
            while(isRunning) {
                try {
                    long newFramePosition = getFramePosition();
                    if(newFramePosition != framePosition) {
                        framePosition = newFramePosition;
                        requiresRefresh = true;
                    }
                    if(requiresRefresh) {
                        repaint();
                        requiresRefresh = false;
                    }
                    Thread.sleep(50);
                }
                catch(Exception e) {
                    
                }
            }
        }
        

        
        private int[][] sortAudioBytes(byte[] raw, AudioFormat format) {
            int numChannels = format.getChannels();
            int[][] waveform = new int[numChannels][(int) frameLength];
            int sampleIndex = 0;
            boolean isBigEndian = format.isBigEndian();
            boolean isSigned = true;
            
            if(format.getSampleSizeInBits() == 8) {
                for(int t=0; t<raw.length;) {
                    for(int channel=0; channel<numChannels; channel++) {
                        int sample = 0;
                        if(isSigned) {
                            sample = (raw[t] << 8) ;
                        }
                        else {
                            sample = ((raw[t] ^ 0x80) << 8);
                        }
                        waveform[channel][sampleIndex] = sample;
                        t = t+1;
                    }
                    sampleIndex++;
                }
            }
            else if(format.getSampleSizeInBits() == 16) {
                for(int t=0; t<raw.length;) {
                    for(int channel=0; channel<numChannels; channel++) {
                        int low = (int) raw[t];
                        int high = (int) raw[t+1];
                        int sample = 0;
                        if(isBigEndian) {
                            sample = (low << 8) | (high & 0x00ff);
                        }
                        else {
                            sample = (low & 0x00ff) | (high << 8);
                        }
                        waveform[channel][sampleIndex] = sample;
                        t = t+2;
                    }
                    sampleIndex++;
                }
            }
            else if(format.getSampleSizeInBits() == 24) {
                for(int t=0; t<raw.length;) {
                    for(int channel=0; channel<numChannels; channel++) {
                        int low = (int) raw[t];
                        int mid = (int) raw[t+1];
                        int high = (int) raw[t+2];
                        int sample = 0;
                        if(isBigEndian) {
                            sample = (low << 16) | ((mid & 0xFF) << 8) | (high & 0x00ff);
                        }
                        else {
                            sample = (low & 0x00ff) | ((mid & 0xFF) << 8) | (high << 16);
                        }
                        waveform[channel][sampleIndex] = sample;
                        t = t+3;
                    }
                    sampleIndex++;
                }
            }
            else if(format.getSampleSizeInBits() == 32) {
                for(int t=0; t<raw.length;) {
                    for(int channel=0; channel<numChannels; channel++) {
                        int low = (int) raw[t];
                        int mid1 = (int) raw[t+1];
                        int mid2 = (int) raw[t+2];
                        int high = (int) raw[t+3];
                        int sample = 0;
                        if(isBigEndian) {
                            sample = (low << 24) | ((mid1 & 0xFF) << 16) | ((mid2 & 0xFF) << 8) | (high & 0x00ff);
                        }
                        else {
                            sample = (low & 0x00ff) | ((mid1 & 0xFF) << 8) | ((mid1 & 0xFF) << 16) | (high << 24);
                        }
                        waveform[channel][sampleIndex] = sample;
                        t = t+4;
                    }
                    sampleIndex++;
                }
            }

            return waveform;
        }
        
        
        private void makeWaveformView() {
            int w = this.getWidth();
            int h = this.getHeight();
            int numberOfChannels = waveformData.length;
            waveformView = new int[numberOfChannels][w][2];
            for(int channel=0; channel<numberOfChannels; channel++) {
                int step = waveformData[channel].length / w;
                if(step == 0) step = 1;
                int o = (channel+1) * h/numberOfChannels - (h/numberOfChannels)/2;

                int y = 0;
                for(int x=0; x<w; x++) {
                    int xstep = x*step;
                    int max = waveformData[channel][xstep];
                    int min = waveformData[channel][xstep];
                    for(int s=1; s<step; s++) {
                        if(waveformData[channel][xstep+s] > max) {
                            max = waveformData[channel][xstep+s];
                        }
                        if(waveformData[channel][xstep+s] < min) {
                            min = waveformData[channel][xstep+s];
                        }
                    }
                    int ymax = o + (max / (256*numberOfChannels));
                    int ymin = o + (min / (256*numberOfChannels));
                    
                    waveformView[channel][x][0] = ymin;
                    waveformView[channel][x][1] = ymax;
                }
            }
        }
        
        
        @Override
        public void update(LineEvent event) {
            if(isPlaying) {
                //framePosition = event.getFramePosition();
                //repaint();
            }
        }
    
    
        @Override
        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(infoFont);
            if(waveformData == null) {
                String text = "Preparing waveform";
                byte[] textBytes = text.getBytes();
                FontMetrics fontMetrics = g.getFontMetrics(infoFont);
                int w = fontMetrics.bytesWidth(textBytes, 0, 0);
                int h = fontMetrics.getHeight();
                g.drawString(text, getWidth()/2-w/2, getHeight()/2-h/2);
            }
            else if(waveformView == null) {
                makeWaveformView();
            }
            if(waveformView != null) {
                int h = this.getHeight();
                int numberOfChannels = waveformData.length;
                int w = waveformView[0].length;
                for(int channel=0; channel<numberOfChannels; channel++) {
                    int o = (channel+1) * h/numberOfChannels - (h/numberOfChannels)/2;
                    w = waveformView[channel].length;
                    g.drawLine(0, o, w, o);
                    for(int x=0; x<w; x++) {
                        int ymax = waveformView[channel][x][0];
                        int ymin = waveformView[channel][x][1];
                        g.drawLine(x, ymin, x, ymax);
                    }
                }
                
                if(framePosition > 0 && frameLength > 0) {
                    int playPositionOnScreen = (int) ((framePosition * w) / frameLength);
                    g.drawLine(playPositionOnScreen, 0, playPositionOnScreen, h);
                }
            }
        }

        
        @Override
        public Dimension getMinimumSize() {
            return new Dimension(320, 256);
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(320, 256);
        }
        
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(320, 256);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            makeWaveformView();
            requiresRefresh = true;
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            makeWaveformView();
            requiresRefresh = true;
        }

        @Override
        public void componentShown(ComponentEvent e) {
            makeWaveformView();
            requiresRefresh = true;
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            //makeWaveformView();
            //requiresRefresh = true;
        }
    }
    
}
