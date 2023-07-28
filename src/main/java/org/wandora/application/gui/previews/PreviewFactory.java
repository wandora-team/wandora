/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
    

    /**
     * Creates a PreviewPanel used to show a preview for a locator, usually the
     * subject locator. Resolving a suitable preview for the locator is hard coded
     * into the method as a simple if-then statement. This may change in future
     * releases of the application. More dynamic preview resolver would be better.
     * All PreviewPanel classes contain a static method canView that tells if
     * the class can view the locator. Usually canView methods delegates the
     * test to the PreviewUtils.isOfType method.
    */
    public static PreviewPanel create(final Locator locator) throws Exception {
        final Wandora wandora = Wandora.getWandora();
        final String urlString = locator.toExternalForm();
        PreviewPanel previewPanel = null;

        if(AudioMidi.canView(urlString)) {
            previewPanel = new AudioMidi(urlString); 
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
        else if(AudioWav.canView(urlString)) {
            previewPanel = new AudioWav(urlString); 
        }
        else if(AudioMP3v2.canView(urlString)) {
            previewPanel = new AudioMP3v2(urlString);
        }
        else if(PreviewUtils.hasJavaFX() && VideoMp4.canView(urlString)) {
            previewPanel = new VideoMp4(urlString);
        }
        else if(Image.canView(urlString)) {
            previewPanel = new Image(urlString);
        }
        else if(ApplicationZMachine.canView(urlString)) {
            previewPanel = new ApplicationZMachine(urlString); 
        }
        else if(ApplicationPDF.canView(urlString)) {
            previewPanel = new ApplicationPDF(urlString);
        }
        else if(ApplicationZ80.canView(urlString)) {
            previewPanel = new ApplicationZ80(urlString);
        }
        else if(ApplicationC64.canView(urlString)) {
            previewPanel = new ApplicationC64(urlString);
        }
        else if(TextRTF.canView(urlString)) {
            previewPanel = new TextRTF(urlString); 
        }
        else if(TextHTML.canView(urlString)) {
            previewPanel = new TextHTML(urlString);
        }
        else if(ApplicationXML.canView(urlString)) {
            previewPanel = new ApplicationXML(urlString);
        }
        else if(Text.canView(urlString)) {
            previewPanel = new Text(urlString); 
        }
        else if(ApplicationZip.canView(urlString)) {
            previewPanel = new ApplicationZip(urlString);
        }
        
        
        // If created panel is heavy-weight, wrap it into an AWRWrapper.
        if(previewPanel != null && previewPanel.isHeavy()) {
            return new AWTWrapper(previewPanel);
        }
        else {
            return previewPanel;
        }
    }
    
}