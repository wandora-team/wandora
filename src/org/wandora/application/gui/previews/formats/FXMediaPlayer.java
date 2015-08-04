/*
 * Copyright (C) 2015 akivela
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
 */

package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Component;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
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
    
    
    public FXMediaPlayer(String mediaUrlString) {
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
                Scene scene = new Scene(root, 500, 200);

                Media media = new Media(mediaUrlString);
                player = new MediaPlayer(media);
                player.play();

                //Add a mediaView, to display the media. Its necessary !
                //This mediaView is added to a Pane
                MediaView mediaView = new MediaView(player);
                root.getChildren().add(mediaView);

                fxPanel.setScene(scene);
            }
        });
    }
    
    


    @Override
    public void finish() {
        if(player != null) player.stop();
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
        if(player != null) player.stop();
    }
    
    
    public static boolean canView(String url) {
        boolean answer = false;
        if(url != null) {
            if(DataURL.isDataURL(url)) {
                try {
                    DataURL dataURL = new DataURL(url);
                    String mimeType = dataURL.getMimetype();
                    if(mimeType != null) {
                        String lowercaseMimeType = mimeType.toLowerCase();
                        if(lowercaseMimeType.startsWith("video/mp4")) {
                                answer = true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), "mp4")) {
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
