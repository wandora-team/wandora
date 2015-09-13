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


import org.wandora.application.gui.previews.PreviewUtils;


/**
 * @author akivela
 */
public class AudioMod extends AudioAbstract {
    

    public AudioMod(String locator) {
        super(locator);
    }
    
    
    
    // -------------------------------------------------------------------------
    

    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
            new String[] { 
                "audio/mod",
                "audio/xm",
                "audio/wow",
                "audio/it",
                "audio/stm",
                "audio/s3m",
                "audio/xm"
            }, 
            new String[] { 
                "mod", 
                "wow", 
                "it", 
                "stm",
                "s3m", 
                "xm"
            }
        );
    }
}
