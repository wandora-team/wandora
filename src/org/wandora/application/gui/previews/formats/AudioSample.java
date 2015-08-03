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
 * AudioSample.java
 *
 * Created on 29. toukokuuta 2006, 14:33
 *
 */

package org.wandora.application.gui.previews.formats;




import org.wandora.application.gui.previews.*;
import org.wandora.utils.IObox;
import org.wandora.utils.ClipboardBox;
import org.wandora.application.gui.simple.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;

import java.util.Map;
import org.wandora.application.gui.*;
import org.wandora.application.*;

import javax.sound.sampled.*;
import javax.sound.midi.*;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import org.wandora.utils.DataURL;



/**
 *
 * @author akivela
 */
public class AudioSample extends JPanel implements Runnable, MouseListener, ActionListener, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.audioSamplePreviewPanel.";

    Map<String, String> options;
    String audioLocator;
    Dimension panelDimensions;
    BufferedImage bgImage;
    boolean isPlaying = false;
    Sequencer sequencer = null;
    
    
    /** Creates a new instance of WandoraImagePanel */
    public AudioSample(String audioLocator) {
        this.audioLocator = audioLocator;
        initialize();
    }
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    public void initialize() {
        this.options = Wandora.getWandora().getOptions().asMap();
        this.addMouseListener(this);
        bgImage = UIBox.getImage("gui/icons/doctype/doctype_audio_sample.png");
        
        panelDimensions = new Dimension(100, 100);
        this.setPreferredSize(panelDimensions);
        this.setMaximumSize(panelDimensions);
        this.setMinimumSize(panelDimensions);
        
        repaint();
        revalidate();
        updateAudioMenu();
    }

    
    
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(bgImage != null) {
            g.drawImage(bgImage,0,0,this);
        }
    }
    
    @Override
    public void finish() {
        isPlaying = false;
        if(sequencer != null) sequencer.stop();
    }

    @Override
    public void stop() {
        isPlaying = false;
        if(sequencer != null) sequencer.stop();
    }
    
    @Override
    public JPanel getGui() {
        return this;
    }
    
    
    
    
    // http://dailywav.com/0506/everyonewillbe.wav
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
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    format.getSampleSizeInBits()*2,
                    format.getChannels(),
                    format.getFrameSize()*2,
                    format.getFrameRate(),
                    true);        // big endian
            audioStream = AudioSystem.getAudioInputStream(format, audioStream);
        }
        // Create line
        SourceDataLine.Info info = new DataLine.Info(
            SourceDataLine.class, audioStream.getFormat(),
            ((int)audioStream.getFrameLength()*format.getFrameSize()));
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioStream.getFormat());
        line.start();

        byte[] audioBuffer = new byte[line.getBufferSize()];
        int numRead = 0;
        int offset = 0;

        while(isPlaying && (numRead = audioStream.read(audioBuffer)) >= 0) {
            offset = 0;
            while (offset < numRead) {
                offset += line.write(audioBuffer, offset, numRead-offset);
            }
        }
        line.drain();
        line.stop();
    }
    
    
    public void play() {
        if(!isPlaying) {
            if(audioLocator != null && audioLocator.length() > 0) {
                Thread audioThread = new Thread(this);
                audioThread.start();
            }
        }
    }
    
    
    
    
    public void forkAudioPlayer() {
        if(audioLocator != null && audioLocator.length() > 0) {
            if(!DataURL.isDataURL(audioLocator)) {
                System.out.println("Spawning viewer for \""+audioLocator+"\"");
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(audioLocator));
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
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        if(c.startsWith("Play")) {
            play();
        }
        if(c.startsWith("Stop")) {
            isPlaying = false;
            if(sequencer != null) sequencer.stop();
        }
        if(c.startsWith("Open in external")) {
            forkAudioPlayer();
        }
        
        else if(c.equalsIgnoreCase("Copy audio location")) {
            if(audioLocator != null) {
                ClipboardBox.setClipboard(audioLocator);
            }
        }
        else if(c.startsWith("Save audio")) {
            save();
        }
    }
   
   
   
   // ----------------------------------------------------------------- SAVE ---
   
   

    public void save() {
        Wandora wandora = Wandora.getWandora(this);
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Save audio file");
        try {
            chooser.setSelectedFile(new File(audioLocator.substring(audioLocator.lastIndexOf(File.pathSeparator)+1)));
        }
        catch(Exception e) {}
        if(chooser.open(wandora, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
        }
    }
    
    
    
    public void save(File audioFile) {
        if(audioFile != null) {
            try {
                if(DataURL.isDataURL(audioLocator)) {
                    DataURL.saveToFile(audioLocator, audioFile);
                }
                else {
                    IObox.moveUrl(new URL(audioLocator), audioFile);
                }
            }
            catch(Exception e) {
                System.out.println("Exception '" + e.toString() + "' occurred while saving file '" + audioFile.getPath() + "'.");
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
                        if(lowercaseMimeType.startsWith("audio/x-aiff") ||
                           lowercaseMimeType.startsWith("audio/basic") ||
                           lowercaseMimeType.startsWith("audio/x-wav")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".aif", /*".mp3", */".wav", ".au")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
   
    
}
