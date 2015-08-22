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
 * AudioMidi.java
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

import javax.sound.midi.*;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import org.wandora.utils.DataURL;



/**
 *
 * @author akivela
 */
public class AudioMidi extends JPanel implements MouseListener, ActionListener, MetaEventListener, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.audioMidiPreviewPanel.";
    
    //Wandora admin;
    private Map<String, String> options;
    private String audioLocator;
    private Dimension panelDimensions;
    private BufferedImage bgImage;
    private boolean isPlaying = false;
    private Sequencer sequencer = null;
    private int volume = 100;
    
    
    /** Creates a new instance of AudioMidiPreviewPanel */
    /*
    public AudioMidiPreviewPanel(Wandora admin) {
        initialize(admin);
    }
    */
    public AudioMidi(String audioLocator) {
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
        bgImage = UIBox.getImage("gui/icons/doctype/doctype_audio_midi.png");
        
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
    public JPanel getGui() {
        return this;
    }
    

    @Override
    public void stop() {
        isPlaying = false;
        if(sequencer != null) sequencer.stop();
    }

    
    public void play() {
        if(!isPlaying) {
            if(audioLocator != null && audioLocator.length() > 0) {
                try {
                    sequencer = MidiSystem.getSequencer();
                    sequencer.open();
                    Sequence sequence = null;
                    
                    if(DataURL.isDataURL(audioLocator)) {
                        DataURL dataURL = new DataURL(audioLocator);
                        sequence = MidiSystem.getSequence(new ByteArrayInputStream(dataURL.getData()));
                    }
                    else {
                        URL audioURL = new URL(audioLocator);
                        sequence = MidiSystem.getSequence(audioURL);
                    }
                    if(sequence != null) {
                        sequencer.setSequence(sequence);
                        sequencer.addMetaEventListener(this);
                        // Start playing.
                        sequencer.start();
                    }
                }
                catch (MidiUnavailableException e) {  e.printStackTrace(); }
                catch (MalformedURLException e) {  e.printStackTrace(); } 
                catch (IOException e) {  e.printStackTrace(); } 
                catch (InvalidMidiDataException e) {  e.printStackTrace(); }
            }
        }
    }
    

    
    @Override
    public void meta(MetaMessage event) {
        if (event.getType() == 47) {
            isPlaying = false;
        }
    }

    
    public void setVolume(int gain) {
        try {
            if(sequencer != null) {
                Synthesizer synthesizer = null;
                if(sequencer instanceof Synthesizer) {
                    synthesizer = (Synthesizer) sequencer;
                }
                else {
                    synthesizer = MidiSystem.getSynthesizer();
                }
                if(synthesizer != null) {
                    MidiChannel[] channels = synthesizer.getChannels();
                    for (int i=0; i<channels.length; i++) {                
                        channels[i].controlChange(7, (int)(gain * 1.27)); // gain == 0..100
                    }
                    volume = gain;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
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
            /*
            "Set volume", new Object[] {
                (volume == 100 ? "X" : "O") + " Volume 100%",
                (volume == 75 ? "X" : "O") + " Volume 75%",
                (volume == 50 ? "X" : "O") + " Volume 50%",
                (volume == 25 ? "X" : "O") + " Volume 25%",
            },
             */
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
            Util.forkExternalPlayer(audioLocator);
        }
        
        else if(c.equalsIgnoreCase("Copy audio location")) {
            if(audioLocator != null) {
                ClipboardBox.setClipboard(audioLocator);
            }
        }
        else if(c.startsWith("Save audio")) {
            Util.saveToFile(audioLocator);
        }
        else if(c.equalsIgnoreCase("Volume 100%")) {
            setVolume(100);
        }
        else if(c.equalsIgnoreCase("Volume 75%")) {
            setVolume(75);
        }
        else if(c.equalsIgnoreCase("Volume 50%")) {
            setVolume(50);
        }
        else if(c.equalsIgnoreCase("Volume 25%")) {
            setVolume(25);
        }
    }
   
   
   
   // ----------------------------------------------------------------- SAVE ---
   
   

    public void save() {
        Wandora admin = Wandora.getWandora(this);
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Save audio file");
        try {
            chooser.setSelectedFile(new File(audioLocator.substring(audioLocator.lastIndexOf(File.pathSeparator)+1)));
        }
        catch(Exception e) {}
        if(chooser.open(admin, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
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
                        if(lowercaseMimeType.startsWith("audio/midi") ||
                           lowercaseMimeType.startsWith("application/x-midi")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".mid", ".midi", ".rmf")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
}
