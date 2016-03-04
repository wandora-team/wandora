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
 * AudioMP3.java
 *
 *
 */

package org.wandora.application.gui.previews.formats;




import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.Map;
import javax.sound.sampled.*;
import javax.swing.*;
import javazoom.jl.player.Player;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;


/**
 *
 * @author akivela
 */
public class AudioMP3 extends JPanel implements Runnable, MouseListener, ActionListener, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.audioMP3PreviewPanel.";

    Map<String, String> options;
    String audioLocator;
    Dimension panelDimensions;
    BufferedImage bgImage;
    boolean isPlaying = false;
    Player player = null;
    
    
    /** Creates a new instance of AudioMP3 */
    public AudioMP3(String audioLocator) {
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
        bgImage = UIBox.getImage("gui/icons/doctype/doctype_audio_mp3.png");
        
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
        if(player != null) player.close();
    }

    @Override
    public void stop() {
        isPlaying = false;
        if(player != null) player.close();
    }
    
    @Override
    public JPanel getGui() {
        return this;
    }
    
    
    
    
    public void run() {
        try {
            isPlaying = true;
            playMP3(audioLocator);
        }
        catch (MalformedURLException e)  { e.printStackTrace();  }
        catch (IOException e)  { e.printStackTrace();  }
        catch (LineUnavailableException e)  { e.printStackTrace();  }
        catch (UnsupportedAudioFileException e) { e.printStackTrace();  }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    
    
    
    
    
    private void playMP3(String audioLocator) throws Exception {
        if(DataURL.isDataURL(audioLocator)) {
            DataURL dataURL = new DataURL(audioLocator);
            player = new Player(new ByteArrayInputStream(dataURL.getData()));
        }
        else {
            URL audioURL = new URL(audioLocator);
            player = new Player(audioURL.openStream());
        }
        player.play();
    }
    
    
    public void play() {
        if(!isPlaying) {
            if(audioLocator != null && audioLocator.length() > 0) {
                Thread audioThread = new Thread(this);
                audioThread.start();
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
            "Copy audio location",
            "---",
            "Open in external player...",
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
        else if(c.startsWith("Stop")) {
            isPlaying = false;
            if(player != null) {
                player.close();
            }
        }
        else if(c.startsWith("Open in external")) {
            PreviewUtils.forkExternalPlayer(audioLocator);
        }
        else if(c.equalsIgnoreCase("Copy audio location")) {
            if(audioLocator != null) {
                ClipboardBox.setClipboard(audioLocator);
            }
        }
        else if(c.startsWith("Save audio")) {
            PreviewUtils.saveToFile(audioLocator);
        }
    }
   

    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "audio/mpeg",
                    "audio/x-mpeg-3",
                    "audio/mpeg3"
                }, 
                new String[] { 
                    "mp3",
                    "m3a"
                }
        );
    }
}
