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
import org.wandora.utils.DataURL;



/**
 *
 * @author akivela
 */
public class AudioMidi implements ActionListener, MetaEventListener, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.audioMidiPreviewPanel.";
    
    //Wandora admin;
    private Map<String, String> options;
    private String audioLocator;
    private Dimension panelDimensions;
    private BufferedImage bgImage;
    private boolean isPlaying = false;
    private Sequencer sequencer = null;
    private int volume = 100;
    
    private JPanel ui = null;
    
    
    
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
    }

    
    
    
    @Override
    public void finish() {
        isPlaying = false;
        if(sequencer != null) sequencer.stop();
    }
    
    
    @Override
    public JPanel getGui() {
        if(ui == null) {
            ui = makeUI();
        }
        return ui;
    }
    

    @Override
    public void stop() {
        isPlaying = false;
        if(sequencer != null) sequencer.stop();
    }

    
    protected JPanel makeUI() {
        if(ui == null) {
            ui = new JPanel();

            JPanel controllerPanel = new JPanel();
            controllerPanel.add(getJToolBar(), BorderLayout.CENTER);

            ui.setLayout(new BorderLayout(8,8));
            ui.add(controllerPanel, BorderLayout.SOUTH);

            updateAudioMenu();
        }
        return ui;
    }
    
    
    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", PreviewUtils.ICON_PLAY, this,
            "Stop", PreviewUtils.ICON_STOP, this,
            "---",
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Save as", PreviewUtils.ICON_SAVE, this,
        }, this);
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
                catch (MidiUnavailableException e) { 
                    PreviewUtils.previewError(ui, "Midi is unavailable.", e);
                }
                catch (MalformedURLException e) {
                    PreviewUtils.previewError(ui, "Midi locator is malformed.", e);
                } 
                catch (IOException e) {  
                    PreviewUtils.previewError(ui, "Unable to read locator resource.", e);
                } 
                catch (InvalidMidiDataException e) {
                    PreviewUtils.previewError(ui, "Midi resource contains bad data.", e);
                }
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
    
 
    // -------------------------------------------------------------------------
    
    
    public void updateAudioMenu() {
        if(ui != null) {
            if(audioLocator != null && audioLocator.length() > 0) {
                ui.setComponentPopupMenu(getImageMenu());
            }
            else {
                ui.setComponentPopupMenu(null);
            }
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
            "Copy locator",
            "---",
            "Open in external player...",
            "---",
            "Save to file...",
           
        };
        return UIBox.makePopupMenu(menuStructure, this);
    }
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        if(PreviewUtils.startsWithAny(c, "Play")) {
            play();
        }
        else if(PreviewUtils.startsWithAny(c, "Stop")) {
            isPlaying = false;
            if(sequencer != null) sequencer.stop();
        }
        else if(PreviewUtils.startsWithAny(c, "Open in external", "Open ext")) {
            PreviewUtils.forkExternalPlayer(audioLocator);
        }
        
        else if(PreviewUtils.startsWithAny(c, "Copy locator")) {
            if(audioLocator != null) {
                ClipboardBox.setClipboard(audioLocator);
            }
        }
        else if(PreviewUtils.startsWithAny(c, "Save to file", "Save as")) {
            PreviewUtils.saveToFile(audioLocator);
        }
        else if(PreviewUtils.startsWithAny(c, "Volume 100%")) {
            setVolume(100);
        }
        else if(PreviewUtils.startsWithAny(c, "Volume 75%")) {
            setVolume(75);
        }
        else if(PreviewUtils.startsWithAny(c, "Volume 50%")) {
            setVolume(50);
        }
        else if(PreviewUtils.startsWithAny(c, "Volume 25%")) {
            setVolume(25);
        }
    }

    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "audio/midi",
                    "application/x-midi",
                }, 
                new String[] { 
                    ".mid", 
                    ".midi", 
                    ".rmf"
                }
        );
    }
}
