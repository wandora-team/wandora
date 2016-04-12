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
 */

package org.wandora.application.gui.previews.formats;

import de.quippy.javamod.mixer.Mixer;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewUtils;
import static org.wandora.application.gui.previews.PreviewUtils.startsWithAny;

/**
 *
 * @author akivela
 */
public class AudioSid extends AudioAbstract {
    
    
    public AudioSid(String locator) {
        super(locator);
    }
    
    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Play", PreviewUtils.ICON_PLAY, this,
            "Pause", PreviewUtils.ICON_PAUSE, this,
            "Stop", PreviewUtils.ICON_STOP, this,
            "Previous", PreviewUtils.ICON_PREVIOUS, this,
            "Next", PreviewUtils.ICON_NEXT, this,
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Save as", PreviewUtils.ICON_SAVE, this,
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
        return PreviewUtils.isOfType(url, 
            new String[] { 
                "audio/x-sidtune",
                "audio/sidtune",
                "audio/x-psid",
                "audio/psid",
                "audio/prs.sid",
                "audio/x-sid",
                "application/sid-commodore-64"
            }, 
            new String[] { 
                "sid"
            }
        );
    }
}
