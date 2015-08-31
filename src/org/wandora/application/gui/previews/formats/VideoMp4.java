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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import static org.wandora.application.gui.previews.PreviewUtils.endsWithAny;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTimeSlider;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class VideoMp4 extends JPanel implements PreviewPanel, ActionListener, ComponentListener {
    private String mediaUrlString = null;
    private JFXPanel fxPanel;
    private MediaPlayer player;
    private boolean playerReady = false;
    private Scene scene;
    private MediaView mediaView;
    private SimpleTimeSlider progressBar;
    private Media media;

    private JPanel errorPanel = null;
    

    public VideoMp4(String mediaUrlString) {
        Platform.setImplicitExit(false);
        this.mediaUrlString = mediaUrlString;
        initialize();
    }
    
    

    private void initialize() {
        fxPanel = new JFXPanel();

        JPanel fxPanelContainer = new JPanel();
        fxPanelContainer.setBackground(java.awt.Color.BLACK);
        fxPanelContainer.setLayout(new BorderLayout());
        fxPanelContainer.add(fxPanel, BorderLayout.CENTER);
        
        progressBar = new SimpleTimeSlider();
        JPanel progressBarContainer = new JPanel();
        progressBarContainer.setLayout(new BorderLayout());
        progressBarContainer.add(progressBar, BorderLayout.CENTER);

        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        this.setLayout(new BorderLayout(4,4));
        this.add(fxPanelContainer, BorderLayout.NORTH);
        this.add(progressBarContainer, BorderLayout.CENTER);
        this.add(controllerPanel, BorderLayout.SOUTH);
        
        errorPanel = null;
        
        Platform.runLater(new Runnable() {
            @Override 
            public void run() {
                Group root = new Group();
                scene = new Scene(root);
                scene.setCursor(Cursor.HAND);
                scene.setFill(Color.rgb(0, 0, 0, 1.0));
                media = getMediaFor(mediaUrlString);
                if(media != null) {
                    player = getMediaPlayerFor(media);
                    if(player != null) {
                        progressBar.addMouseMotionListener(new MouseMotionAdapter() {
                            public void mouseDragged(java.awt.event.MouseEvent e) {
                                int mouseValue = progressBar.getValueFor(e);
                                player.seek(Duration.seconds(mouseValue));
                            }
                        });
                        progressBar.addMouseListener(new MouseAdapter() {
                            public void mousePressed(java.awt.event.MouseEvent e) {
                                int mouseValue = progressBar.getValueFor(e);
                                player.seek(Duration.seconds(mouseValue));
                            }
                        });

                        mediaView = new MediaView(player);
                        root.getChildren().add(mediaView);
                    }
                }

                fxPanel.setScene(scene);
            }
        });
        this.addComponentListener(this);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    private Media getMediaFor(String mediaLocator) {
        try {
            final Media m = new Media(mediaLocator);
            if(m.getError() == null) {
                m.setOnError(new Runnable() {
                    public void run() {
                        processError(m.getError());
                    }
                });
                return m;
            }
            else {
                processError(m.getError());
            }
        }
        catch(Exception e) {
            processError(e);
        }
        return null;
    }
    
    
    private MediaPlayer getMediaPlayerFor(Media media) {
        try {
            playerReady = false;
            final MediaPlayer mediaPlayer = new MediaPlayer(media);
            if(mediaPlayer.getError() == null) {
                mediaPlayer.setAutoPlay(false);
                mediaPlayer.currentTimeProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        Duration newDuration = (Duration) newValue;
                        progressBar.setValue((int) Math.round(newDuration.toSeconds()));
                        updateTimeLabel();
                    }
                });
                mediaPlayer.setOnReady(new Runnable() {
                    public void run() {
                        playerReady = true;
                        progressBar.setMinimum(0.0);
                        progressBar.setValue(0.0);
                        progressBar.setMaximum(mediaPlayer.getTotalDuration().toSeconds());
                        refreshLayout();
                    }
                });
                mediaPlayer.setOnError(new Runnable() {
                    public void run() {
                        processError(mediaPlayer.getError());
                    }
                });
                return mediaPlayer;
            }
            else {
                processError(mediaPlayer.getError());
            }
        }
        catch(Exception e) {
            processError(e);
        }
        return null;
    }
    
    
    
    private void processError(Exception e) {
        if(e != null) {
            if(errorPanel == null) {
                
                errorPanel = new JPanel();
                errorPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20,20));
                errorPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED, 4));
                
                String message = "<html><center>Can't view preview for '"+mediaUrlString+"'. <br> " +e.getMessage()+"</center></html>";
                SimpleLabel label = new SimpleLabel();
                label.setText(message);
                label.setHorizontalAlignment(SimpleLabel.CENTER);
                errorPanel.add(label);
                
                this.removeAll();
                this.add(errorPanel, BorderLayout.CENTER);
                
                this.revalidate();
                this.repaint();
            }
            e.printStackTrace();
        }
    }
    
    
    
    private JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", UIBox.getIcon(0xf04b), this,
            "Pause", UIBox.getIcon(0xf04c), this,
            "Stop", UIBox.getIcon(0xf04d), this,
            "Backward", UIBox.getIcon(0xf04a), this,
            "Forward", UIBox.getIcon(0xf04e), this,
            //"Start", UIBox.getIcon(0xf048), this,
            //"End", UIBox.getIcon(0xf051), this,
            "Restart", UIBox.getIcon(0xf0e2), this,
            "---",
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Save as", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
    }

    
    
    private void refreshLayout() {
        if(playerReady) {
            int w = player.getMedia().getWidth();
            int h = player.getMedia().getHeight();

            int outerWidth = VideoMp4.this.getWidth();

            fxPanel.setSize(w, h);
            fxPanel.setPreferredSize(new Dimension(w, h));
            fxPanel.revalidate();

            mediaView.translateXProperty().set(outerWidth/2 - w/2);

            //progressBar.setPreferredSize(new Dimension(outerWidth, 17));
            //progressBar.setMaximumSize(new Dimension(outerWidth, 16));
            //progressBar.setMinimumSize(new Dimension(outerWidth, 16));
            //progressBar.validate();
            this.validate();
        }
    }
    
    
    private void updateTimeLabel() {
        if(progressBar != null) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    Duration currentTime = player.getCurrentTime();
                    progressBar.setValue(currentTime.toSeconds());
                }
            });
        }
    }
    
    
    
    
    
    // -------------------------------------------------------------------------


    @Override
    public void finish() {
        if(player != null) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    player.stop();
                }
            });
        }
    }

    @Override
    public Component getGui() {
        return this;
    }

    @Override
    public boolean isHeavy() {
        return false;
    }

    @Override
    public void stop() {
        if(player != null) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    // System.out.println("VideoMp4 stopped.");
                    player.stop();
                }
            });
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if(e == null) return;
        
        String actionCommand = e.getActionCommand();
        System.out.println("Action '"+actionCommand+"' performed at FXMediaPlayer");
        
        if("Play".equalsIgnoreCase(actionCommand)) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    Status status = player.getStatus();
                    if(!status.equals(Status.PLAYING)) {
                        player.play();
                    }
                }
            });
        }
        
        else if("Pause".equalsIgnoreCase(actionCommand)) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    Status status = player.getStatus();
                    if(status.equals(Status.PAUSED) || status.equals(Status.READY) || status.equals(Status.STOPPED)) {
                        player.play();
                    }
                    else if(status.equals(Status.PLAYING)) {
                        player.pause();
                    }
                }
            });
        }
        
        else if("Stop".equalsIgnoreCase(actionCommand)) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    player.seek(player.getStartTime());
                    player.stop();
                }
            });
        }
        
        else if("Backward".equalsIgnoreCase(actionCommand)) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    player.seek(player.getCurrentTime().divide(1.5));
                }
            });
        }
        
        else if("Forward".equalsIgnoreCase(actionCommand)) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    player.seek(player.getCurrentTime().multiply(1.5));
                }
            });
        }

        else if("Restart".equalsIgnoreCase(actionCommand)) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    player.play();
                    player.seek(player.getStartTime());
                }
            });
        }
        
        else if("Open ext".equalsIgnoreCase(actionCommand)) {
            PreviewUtils.forkExternalPlayer(mediaUrlString);
        }
        
        else if("Save".equalsIgnoreCase(actionCommand)) {
            PreviewUtils.saveToFile(mediaUrlString);
        }
        
        else if("Copy location".equalsIgnoreCase(actionCommand)) {
            if(mediaUrlString != null) {
                ClipboardBox.setClipboard(mediaUrlString);
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    

    @Override
    public void componentResized(ComponentEvent e) {
        Platform.runLater(new Runnable() {
            @Override 
            public void run() {
                refreshLayout();
            }
        });
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    
    
    @Override
    public void componentShown(ComponentEvent e) {
        
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    
    
    
    // -------------------------------------------------------------------------
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "video/mp4",
                    "video/x-flv",
                    "video/x-javafx",
                    "application/vnd.apple.mpegurl",
                    "audio/mpegurl",
                    "audio/mp3",
                    "audio/aiff",
                    "audio/x-aiff",
                    "audio/wav",
                    "audio/x-m4a",
                    "video/x-m4v"
                }, 
                new String[] { 
                    "mp4", 
                    "flv", 
                    "fxm", 
                    "m3u8", 
                    "mp3",
                    "m3a",
                    "aif", 
                    "aiff", 
                    "wav",
                    "m4a", 
                    "m4v"
                }
        );
    }
}
