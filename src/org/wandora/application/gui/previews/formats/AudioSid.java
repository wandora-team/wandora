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
 */

package org.wandora.application.gui.previews.formats;

import de.quippy.javamod.mixer.Mixer;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import org.wandora.application.gui.UIBox;
import static org.wandora.application.gui.previews.PreviewUtils.endsWithAny;
import static org.wandora.application.gui.previews.PreviewUtils.startsWithAny;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class AudioSid extends AudioMod {
    
    
    public AudioSid(String locator) {
        super(locator);
    }
    
    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", UIBox.getIcon(0xf04b), this,
            "Pause", UIBox.getIcon(0xf04c), this,
            "Stop", UIBox.getIcon(0xf04d), this,
            "Previous", UIBox.getIcon(0xf048), this,
            "Next", UIBox.getIcon(0xf051), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Save as", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        Mixer mixer = getMixer();
        if(startsWithAny(cmd, "Next")) {
            if(mixer != null) {
                if(mixer.isNotSeeking() && mixer.isNotPausingNorPaused()) {
                    long currentTrack = mixer.getMillisecondPosition();
                    long nextTrack = currentTrack + 1;
                    mixer.setMillisecondPosition(Math.min(mixer.getLengthInMilliseconds(), nextTrack));
                }
            }
        }
        else if(startsWithAny(cmd, "Previous")) {
            if(mixer != null) {
                if(mixer.isNotSeeking() && mixer.isNotPausingNorPaused()) {
                    long currentTrack = mixer.getMillisecondPosition();
                    long previousTrack = currentTrack - 1001;
                    mixer.setMillisecondPosition(Math.max(0, previousTrack));
                }
            }
        }
        else {
            super.actionPerformed(e);
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
                        if(lowercaseMimeType.startsWith("audio/x-sidtune") ||
                           lowercaseMimeType.startsWith("audio/sidtune") ||
                           lowercaseMimeType.startsWith("audio/x-psid") ||
                           lowercaseMimeType.startsWith("audio/psid") ||
                           lowercaseMimeType.startsWith("audio/prs.sid") ||
                           lowercaseMimeType.startsWith("audio/x-sid")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".sid")) {
                    return true;
                }
            }
        }
        return false;
    }
}
