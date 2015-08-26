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
 * PreviewFactory.java
 */

package org.wandora.application.gui.previews;


import org.wandora.application.gui.previews.formats.*;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Locator;



/**
 *
 * @author anttirt, akivela
 */
public class PreviewFactory {
    

    
    public static PreviewPanel create(final Locator locator) {
        final Wandora wandora = Wandora.getWandora();
        final String urlString = locator.toExternalForm();
        PreviewPanel previewPanel = null;

        try {
            if(hasJavaFX() && FXMediaPlayer.canView(urlString)) {
                previewPanel = new FXMediaPlayer(urlString);
            }
            else if(AudioFlac.canView(urlString)) {
                previewPanel = new AudioFlac(urlString); 
            }
            else if(AudioOgg.canView(urlString)) {
                previewPanel = new AudioOgg(urlString); 
            }
            else if(AudioSid.canView(urlString)) {
                previewPanel = new AudioSid(urlString); 
            }
            else if(AudioMod.canView(urlString)) {
                previewPanel = new AudioMod(urlString); 
            }
            else if(AudioMidi.canView(urlString)) {
                previewPanel = new AudioMidi(urlString); 
            }
            else if(AudioSample.canView(urlString)) {
                previewPanel = new AudioSample(urlString); 
            }
            else if(AudioMP3.canView(urlString)) {
                previewPanel = new AudioMP3(urlString);
            }
            else if(Picture.canView(urlString)) {
                previewPanel = new Picture(urlString);
            }
            else if(PDFnew.canView(urlString)) {
                previewPanel = new PDFnew(urlString);
            }
            else if(Text.canView(urlString)) {
                previewPanel = new Text(urlString); 
            }
            else if(XML.canView(urlString)) {
                previewPanel = new XML(urlString);
            }
            else if(FMJ.canView(urlString)) {
                previewPanel = new FMJ(urlString);
            }
            else if(GST.canView(urlString)) {
                previewPanel = new GST(urlString);
            }
            else if(HTML.canView(urlString)) {
                previewPanel = new HTML(urlString);
            }
            else if(ZMachine.canView(urlString)) {
                previewPanel = new ZMachine(urlString); 
            }
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
        
        
        if(previewPanel != null && previewPanel.isHeavy()) {
            return new AWTWrapper(Wandora.getWandora(), previewPanel);
        }
        else {
            return previewPanel;
        }
    }
    
    
    
    
    private static boolean hasJavaFX() {
        try {
            Class jfxPanel = Class.forName("javafx.embed.swing.JFXPanel");
            return true;
        } 
        catch (ClassNotFoundException e) {
            return false;
        }
    }
}