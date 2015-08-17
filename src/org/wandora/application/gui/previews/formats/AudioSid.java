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

import javax.swing.JComponent;
import org.wandora.application.gui.UIBox;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import static org.wandora.application.gui.previews.Util.startsWithAny;
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
            "Stop", UIBox.getIcon(0xf04d), this,
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Save as", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
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
                        if(lowercaseMimeType.startsWith("audio/sid")) {
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
