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
 * ApplicationPDFPanel.java
 *
 * Created on 12. lokakuuta 2007, 17:14
 *
 */

package org.wandora.application.gui.previews.formats;

import com.sun.pdfview.PDFPage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.wandora.application.gui.UIBox;


/**
 *
 * @author akivela, anttirt
 */
public class ApplicationPDFPanel extends JPanel {
    private Image currentImage;
    private BufferedImage currentBufferedImage;
    private PDFPage currentPage;
    private double zoom;
    

    public ApplicationPDFPanel() {
        super();
        this.currentPage = null;
        zoom = 1.0;
        currentImage = null;
    }

    
    public void changePage(PDFPage page) {
        currentPage = page;
        refresh();
        repaint();
    }


    public void setZoom(double zoom) {
        if(zoom > .1 && zoom < 10) {
            this.zoom = zoom;
            refresh();
            repaint();
        }
    }
    
    
    public double getZoom() {
        return zoom;
    }

    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        repaint();
    }

    
    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        repaint();
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        if(g instanceof Graphics2D) {
            RenderingHints qualityHints = new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            RenderingHints antialiasHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            ((Graphics2D) g).addRenderingHints(qualityHints);
            ((Graphics2D) g).addRenderingHints(antialiasHints);
        }
        
        if(currentBufferedImage == null) {
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
            g.setColor(Color.black);
            g.drawString("No page selected",
                         getWidth() / 2 - 30,
                         getHeight() / 2);
        }
        else {
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
            g.drawImage(currentBufferedImage,
                        0,
                        0, 
                        currentBufferedImage.getWidth(), 
                        currentBufferedImage.getHeight(),
                        null);
        }
    }

    
    private void refresh() {
        if(currentPage != null) {
            int w = (int) (currentPage.getWidth() * zoom);
            int h = (int) (currentPage.getHeight() * zoom);
            Rectangle rect = new Rectangle(0, 0, w, h);
            Rectangle clip = currentPage.getBBox().getBounds();

            int rotation = currentPage.getRotation();
            if(rotation == 90 || rotation == 270) {
                clip = new Rectangle(
                        (int) clip.getY(), 
                        (int) clip.getX(), 
                        (int) clip.getHeight(), 
                        (int) clip.getWidth()
                );
            }
            
            Dimension pageSize = new Dimension(rect.width, rect.height);
            setPreferredSize(pageSize);
            setMinimumSize(pageSize);
            setMaximumSize(pageSize);
            
            currentImage = currentPage.getImage(
                                        rect.width, // lastPageSize.width,
                                        rect.height, // lastPageSize.height,
                                        clip,
                                        null,   /* imageobserver */
                                        true,  /* draw white bg */
                                        true); /* block */
            
            currentBufferedImage = UIBox.makeBufferedImage(currentImage);
            
            revalidate();
        }
    }
}
