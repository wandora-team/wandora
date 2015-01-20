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
 * CopyAsImage.java
 *
 * Created on 14. kesäkuuta 2007, 16:54
 *
 */

package org.wandora.application.tools;



import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.utils.*;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;


/**
 * Copies selected UI component as an image and store the image to system clipboard.
 *
 * @author akivela
 */



public class CopyAsImage extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of CopyAsImage */
    public CopyAsImage() {
    }
    public CopyAsImage(Context proposedContext) {
        setContext(proposedContext);
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        Object o = context.getContextSource();
        if(o instanceof Component) {
            try {
                Component c = (Component) o;
                if(c instanceof GraphTopicPanel) {
                    c = ((GraphTopicPanel) c).getGraphPanel();
                }
                BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
                c.print(image.getGraphics());
                ClipboardBox.setClipboard(image);
                log("Copied context component to system clipboard as an image. Done.");
            }
            catch(Exception e) {
                log(e);
            }
        }
    }
    
    
    @Override
    public boolean runInOwnThread() {
        return false;
    }

    @Override
    public boolean allowMultipleInvocations(){
        return true;
    }

    @Override
    public String getName() {
        return "Copy as image";
    }

    @Override
    public String getDescription() {
        return "Copies current UI element to system clipboard as a bitmap image.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/copy_as_image.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
