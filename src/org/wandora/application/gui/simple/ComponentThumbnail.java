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
 * ComponentThumbnail.java
 *
 * Created on 29. lokakuuta 2007, 14:49
 *
 */

package org.wandora.application.gui.simple;


import java.awt.*;
import javax.swing.*;
import java.awt.image.*;

/**
 *
 * @author akivela
 */
public class ComponentThumbnail extends JPanel implements Runnable {
    
    Component original = null;
    int thumbWidth = 50;
    int thumbHeight = 50;
    Image thumbnailImage = null;
    
    
    
    /** Creates a new instance of ComponentThumbnail */
    public ComponentThumbnail(Component original) {
        this.original = original;
        this.add(original);
        
        Thread runner = new Thread(this);
        runner.start();
    }
    
    
    
    
    public void run() {
        while(true) {               
            if(original.getWidth() > 0 && original.getHeight() > 0) {
                
                int w = original.getWidth();
                int h = original.getHeight();
                w = ( w > 2000 ? 2000 : w );
                h = ( h > 2000 ? 2000 : h );
                thumbWidth = 200*w/h;
                thumbHeight = 200;

                BufferedImage tempImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR );
                original.paint(tempImage.getGraphics());

                Graphics2D g2 = tempImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );

                thumbnailImage = tempImage.getScaledInstance( thumbWidth, thumbHeight, BufferedImage.SCALE_SMOOTH );
                
                repaint();
                revalidate();
                
                if(getParent() != null) {
                    getParent().invalidate();
                }
            }
            
            try {
                Thread.currentThread().sleep(1000);
            }
            catch(Exception e) {}
        }
    }
    
    
    
    public void paint(Graphics g) {
        //super.paint(g);
        if(getParent() != null) g.setColor(getParent().getBackground());
        g.fillRect(0,0,g.getClipBounds().width, g.getClipBounds().height);
        g.drawImage(thumbnailImage,0,0,this);
        g.setColor(Color.GRAY);
        g.drawRect(0,0,thumbWidth-1,thumbHeight-1);
    }
    
    
    public Dimension getPreferredSize() {
        Dimension thumbDimensions = new Dimension(thumbWidth, thumbHeight);
        return thumbDimensions;
    }
    

}
