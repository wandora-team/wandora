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

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.User;
import java.io.File;
import java.net.URL;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public class FBGraphExtractor extends AbstractFBGraphExtractor{

    protected static String token = null;
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_facebook.png");
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        FBGraphExtractorPanel panel = new FBGraphExtractorPanel();
        panel.open();
        if(!panel.wasAccepted) return;
        setDefaultLogger();
        try {
            AbstractFBWrapper.setLogger(getDefaultLogger());
            AbstractFBGraphExtractor.extractObject(wandora.getTopicMap(), panel.getObjectId(), panel.getObjectClass());
            setState(WAIT);
        } catch (TopicMapException ex) {
            log(ex);
        }

        
    }
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
