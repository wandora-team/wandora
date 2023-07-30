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
 */



package org.wandora.application.gui.topicpanels.dockingpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import bibliothek.gui.dock.util.BackgroundComponent;
import bibliothek.gui.dock.util.BackgroundPaint;
import bibliothek.gui.dock.util.PaintableComponent;
import bibliothek.gui.dock.util.Transparency;

/**
 *
 * @author akivela
 */


public class WandoraBackgroundPaint implements BackgroundPaint {
    /* the entire image as it was read from the disk */
    private BufferedImage baseImage;
    /* an image with the same size as the frame */
    private BufferedImage image;

    /* our anchor point, the location 0/0 of our image and of this component will always match */
    private Component content;


    /* standard constructor */
    public WandoraBackgroundPaint( Component content ) {
        this.content = content;
        resolveImage("gui/startup_image.gif");
    }

    public void install( BackgroundComponent component ){
        // ignore    
    }

    public void uninstall( BackgroundComponent component ){
        // ignore
    }
    
    
    
    private void resolveImage(String imageLocator) {
        if(imageLocator != null) {
            baseImage = null;
            try {
                URL url = new URL(imageLocator);
                this.baseImage = ImageIO.read(url);
            }
            catch (Exception e) {
                try {
                    File file = new File(imageLocator);
                    this.baseImage = ImageIO.read(file);
                }
                catch (Exception e2) {
                    try {
                        URL url = ClassLoader.getSystemResource(imageLocator);
                        this.baseImage = ImageIO.read(url);
                    }
                    catch (Exception e3) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }
    
    
    

    /* gets the image that should be used for painting */ 
    public BufferedImage getImage(){
        if( image == null || image.getWidth() != content.getWidth() || image.getHeight() != content.getHeight() ){
            image = createBackground( baseImage, content.getWidth(), content.getHeight() );
        }
        return image;
    }

    /* this is the method that paints the background */
    public void paint( BackgroundComponent background, PaintableComponent paintable, Graphics g ){
        if( SwingUtilities.isDescendingFrom( background.getComponent(), content )){
            /* If we are painting an non-transparent component we paint our custom background image, otherwise
             * we just let it shine through */
            if( paintable.getTransparency() == Transparency.SOLID ){
                Point point = new Point( 0, 0 );
                point = SwingUtilities.convertPoint( paintable.getComponent(), point, content );
                BufferedImage image = getImage();
                if( image != null ){
                    int w = paintable.getComponent().getWidth();
                    int h = paintable.getComponent().getHeight();
                    g.drawImage( image, 0, 0, w, h, point.x, point.y, point.x + w, point.y + h, null );
                }
            }

            /* and now we paint the original content of the component */
            Graphics2D g2 = (Graphics2D)g.create();

            //g2.setComposite( alpha );

            paintable.paintBackground( g2 );
            paintable.paintForeground( g );
            paintable.paintBorder( g2 );

            g2.dispose();

            paintable.paintChildren( g );

        }
    }
    
    
    
    
    /* This helper method creates an image we use for the background */
    private static BufferedImage createBackground( BufferedImage image, int width, int height ){
        if( width <= 0 || height <= 0 ){
            return null;
        }
        BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
        Graphics g = result.createGraphics();
        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, width, height );
        
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        int mw = Math.min( width, imageWidth );
        int mh = Math.min( height, imageHeight );
        
        g.drawImage( image, width/2-mw/2, height/2-mh/2, width/2+mw/2, height/2+mh/2, imageWidth/2-mw/2, imageHeight/2-mh/2, imageWidth/2+mw/2, imageHeight/2+mh/2, null );
        g.dispose();
        
        return result;
    }
}
