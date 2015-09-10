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
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;


/**
 *
 * @author anttirt
 */
public class ApplicationPDFPanel extends JPanel {
    private Image currentImage;
    private Dimension lastPageSize;
    private PDFPage currentPage;
    private Rectangle2D currentClip;
    private boolean needsRefresh;
    private final AtomicBoolean imageDone;
    private double zoom;
    private Dimension viewOffset;
    
    private static final Dimension defaultSize =
            new Dimension(400, 400);

    
    public ApplicationPDFPanel() {
        super();
        imageDone = new AtomicBoolean();
        this.currentPage = null;
        zoom = 1.0;
        viewOffset = new Dimension(0, 0);
        currentClip = null;
        currentImage = null;
        imageDone.set(true);
        setDoubleBuffered(true);
        needsRefresh = true;
        setMinimumSize(defaultSize);
        setPreferredSize(defaultSize);
    }

    public void changePage(PDFPage currentPage) {
        this.currentPage = currentPage;
        currentClip = null;
        currentImage = null;
        needsRefresh = true;
        
        repaint();
    }

    public void setViewOffset(Dimension viewOffset) {
        if(viewOffset == null)
            throw new NullPointerException(
                    "Attempt to set view offset to null");
        this.viewOffset = viewOffset;
        
        repaint();
    }

    public void setZoom(double zoom) {
        if(zoom > .1 && zoom < 10) {
            this.zoom = zoom;
            needsRefresh = true;
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
        final int w = getWidth();
        final int h = getHeight();
        
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
    
        if(needsRefresh)
            refresh();
        
        if(currentImage == null) {
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
            g.setColor(Color.black);
            g.drawString("No page selected",
                         getWidth() / 2 - 30,
                         getHeight() / 2);
        }
        else {
            if(!imageDone.get()) {
                return;
            }
            
            final Dimension imgOffset =
                    imageToComponentSpace(new Dimension(0, 0));
            
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
            g.drawImage(currentImage,
                        imgOffset.width,
                        imgOffset.height,
                        null);
        }
    }
    
    private Dimension imageToComponentSpace(Dimension dim) {
        final int w = viewOffset.width - dim.width,
                  h = viewOffset.height - dim.height;
        return new Dimension(w, h);
    }
    
    
    
    private void refresh() {
        if(currentPage != null) {
            if(!imageDone.get()) {
                currentPage.stop(lastPageSize.width,
                          lastPageSize.height,
                          currentClip);
            }
            
            // clip rect in page coordinates (invariant per page)
            currentClip = currentPage.getBBox();
            
            // w,h = size of virtual display space
            final int w = (int)(defaultSize.width * zoom),
                      h = (int)(defaultSize.height * zoom);
            
            if(w == 0 && h == 0) {
                return;
            }
            
            final Dimension pageSize =
                currentPage.getUnstretchedSize((int) (defaultSize.width * zoom), (int) (defaultSize.height * zoom), null);

            setPreferredSize(pageSize);
            setMinimumSize(pageSize);
            setMaximumSize(pageSize);

            imageDone.set(false);
            
            // get a size where the image is
            // scaled to fit virtual display space
            lastPageSize = currentPage.getUnstretchedSize(w, h, currentClip);
            
            
            System.out.println("currentClip: "+currentClip);
            currentImage = currentPage.getImage(lastPageSize.width,
                                         lastPageSize.height,
                                         currentClip,
                                         this,   /* imageobserver */
                                         true,  /* draw white bg */
                                         false); /* block */
        }
        revalidate();
        needsRefresh = false;
    }

    
    
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        final boolean //someDone = (infoflags & SOMEBITS) != 0,
                      allDone = (infoflags & ALLBITS) != 0,
                      fail = (infoflags & (ERROR | ABORT)) != 0;
        
        if(allDone || fail) {
            imageDone.set(true);
        }
        
        if(allDone) {
            repaint();
        }
        
        if(allDone || fail) {
            return false;
        }
        
        return true;
    }
}
