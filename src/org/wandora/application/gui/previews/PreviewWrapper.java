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
 * PreviewWrapper.java
 *
 * Created on 29. toukokuuta 2006, 14:55
 *
 */

package org.wandora.application.gui.previews;


import javax.swing.*;
import java.awt.*;

import org.wandora.application.*;
import org.wandora.application.gui.previews.formats.Unknown;
import org.wandora.topicmap.Locator;
import static org.wandora.utils.Functional.*;


/**
 *
 * @author akivela
 */
public class PreviewWrapper extends JPanel {
    private PreviewPanel currentPanel = null;
    private Component currentUI = null;
    private Wandora wandora = null;
    
    
    
    /**
     * Creates a new instance of PreviewWrapper
     */
    public PreviewWrapper() {
        this.wandora = Wandora.getWandora();
        this.setLayout(new BorderLayout());
    }
    
    
    
    public static PreviewWrapper getPreviewWrapper() {
        return new PreviewWrapper();
    }
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    public void stop() {
        if(currentPanel != null) {
            // System.out.println("Stopping preview wrapper.");
            currentPanel.stop();
        }
    }
    
    public void setURL(Locator subjectLocator) {
        if(currentPanel != null) {
            currentPanel.stop();
            currentPanel.finish();
            currentPanel = null;
            currentUI = null;
        }
        removeAll();

        if(subjectLocator == null)
            return;
        
        if(subjectLocator.toExternalForm().equals(""))
            return;
        
        try {
            currentPanel = PreviewFactory.create(subjectLocator);
            String locatorString = subjectLocator.toExternalForm();
            if(locatorString.length() > 50) locatorString = locatorString.substring(0,50)+"...";
            System.out.println("Created preview "+currentPanel.getClass()+" for "+locatorString);
        }
        catch(Exception e) {
            Util.previewError(this, "Creating preview failed.", e);
        }
        
        if(currentPanel != null) {
            currentUI = currentPanel.getGui();
            if(currentUI != null) {
                add(currentUI, BorderLayout.CENTER);
                setPreferredSize(currentUI.getPreferredSize());
            }
        }
        revalidate();
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public Dimension getPreferredSize() {
        if(currentUI != null) {
            return currentUI.getPreferredSize();
        }
        else {
            return new Dimension(0,0);
        }
    }
    
    
    @Override
    public Dimension getMinimumSize() {
        if(currentUI != null) {
            return currentUI.getMinimumSize();
        }
        else {
            return new Dimension(0,0);
        }
    }
    
    
    /*
    @Override
    public Dimension getMaximumSize() {
        if(currentUI != null) {
            return currentUI.getMaximumSize();
        }
        else {
            return new Dimension(0,0);
        }
    }
    */
}
