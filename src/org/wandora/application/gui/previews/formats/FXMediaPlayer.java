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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javax.swing.JPanel;
import org.wandora.application.gui.previews.PreviewPanel;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class FXMediaPlayer extends JPanel implements PreviewPanel {
    private String mediaUrlString = null;
    private JFXPanel fxPanel;
    private MediaPlayer player;
    private Scene scene;
    private VBox sliderBox;
    private MediaView mediaView;
    private Slider slider;
    
    
    public FXMediaPlayer(String mediaUrlString) {
        Platform.setImplicitExit(false);
        this.mediaUrlString = mediaUrlString;
        initialize();
    }
    
    

    private void initialize() {
        fxPanel = new JFXPanel();
        this.setLayout(new BorderLayout());
        this.add(fxPanel, BorderLayout.CENTER);
        
        Platform.runLater(new Runnable() {
            @Override public void run() {
                Group root = new Group();
                scene = new Scene(root);
                scene.setCursor(Cursor.HAND);
                scene.setFill(Color.rgb(255, 255, 255, 0.0));

                Media media = getMediaFor(mediaUrlString);
                player = getMediaPlayerFor(media);
                if(player != null) {
                    slider = new Slider();
                    slider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, final Boolean isNowChanging) {
                            if(!isNowChanging) {
                                player.seek(Duration.seconds(slider.getValue()));
                            }

                        }
                    });
                    
                    sliderBox = new VBox();
                    sliderBox.visibleProperty().set(false);
                    sliderBox.getChildren().add(slider);
                    
                    mediaView = new MediaView(player);

                    root.getChildren().add(mediaView);
                    root.getChildren().add(sliderBox);
                }

                fxPanel.setScene(scene);
            }
        });
    }
    
    
    // -------------------------------------------------------------------------
    
    
    private Media getMediaFor(String mediaLocator) {
        Media media = new Media(mediaLocator);
        if(media.getError() == null) {
            media.setOnError(new Runnable() {
                public void run() {
                    // Handle asynchronous error in Media object.
                }
            });
        }
        else {
            // Handle synchronous error creating Media.
        }
        return media;
    }
    
    
    private MediaPlayer getMediaPlayerFor(Media media) {
        try {
            final MediaPlayer mediaPlayer = new MediaPlayer(media);
            if(mediaPlayer.getError() == null) {
                mediaPlayer.currentTimeProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        if(!slider.isValueChanging()) {
                            Duration newDuration = (Duration) newValue;
                            slider.setValue(newDuration.toSeconds());
                        }
                    }
                });
                mediaPlayer.setOnReady(new Runnable() {
                    public void run() {
                        int w = mediaPlayer.getMedia().getWidth();
                        int h = mediaPlayer.getMedia().getHeight();
                        
                        fxPanel.setSize(w, h);
                        fxPanel.setPreferredSize(new Dimension(w, h));
                        fxPanel.revalidate();
                        
                        sliderBox.setMaxSize(w-10, 20);
                        sliderBox.setMinSize(w-10, 20);
                        sliderBox.translateYProperty().set(h-20);
                        sliderBox.visibleProperty().set(true);
                        
                        slider.setMin(0.0);
                        slider.setValue(0.0);
                        slider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                        
                        player.play();
                    }
                });
                mediaPlayer.setOnError(new Runnable() {
                    public void run() {
                        // Handle asynchronous error in MediaPlayer object.
                    }
                });
            }
            else {
                // Handle synchronous error creating MediaPlayer.
            }
            return mediaPlayer;
        }
        catch(Exception e) {
            // Handle exception creating MediaPlayer.
        }
        return null;
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
                    // System.out.println("FXMediaPlayer stopped.");
                    player.stop();
                }
            });
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        boolean answer = false;
        if(url != null) {
            if(DataURL.isDataURL(url)) {
                try {
                    DataURL dataURL = new DataURL(url);
                    String mimeType = dataURL.getMimetype();
                    if(mimeType != null) {
                        String lowercaseMimeType = mimeType.toLowerCase();
                        if(lowercaseMimeType.startsWith("video/mp4") ||
                            lowercaseMimeType.startsWith("video/x-flv") ||
                            lowercaseMimeType.startsWith("video/x-javafx") ||
                            lowercaseMimeType.startsWith("application/vnd.apple.mpegurl") ||
                            lowercaseMimeType.startsWith("audio/mpegurl") ||
                            //lowercaseMimeType.startsWith("audio/mp3") ||
                            //lowercaseMimeType.startsWith("audio/aiff") ||
                            //lowercaseMimeType.startsWith("audio/x-aiff") ||
                            //lowercaseMimeType.startsWith("audio/wav") ||
                            lowercaseMimeType.startsWith("audio/x-m4a") ||
                            lowercaseMimeType.startsWith("video/x-m4v")) {
                                answer = true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".mp4", ".flv", ".fxm", ".m3u8", /* ".mp3", ".aif", ".aiff", ".wav", */ ".m4a", ".m4v")) {
                    answer = true;
                }
            }
        }
        
        return answer && hasJavaFX();
    }
    
    
    public static boolean hasJavaFX() {
        try {
            Class jfxPanel = Class.forName("javafx.embed.swing.JFXPanel");
            return true;
        } 
        catch (ClassNotFoundException e) {
            return false;
        }
    }
}
