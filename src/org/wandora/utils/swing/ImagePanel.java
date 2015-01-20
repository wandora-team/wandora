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
 * 
 *
 * ImagePanel.java
 *
 * Created on August 30, 2004, 4:13 PM
 */

package org.wandora.utils.swing;


import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;



/**
 *
 * @author  akivela
 */
public class ImagePanel extends JPanel {
    private URL url;
    private File file;
    private BufferedImage image;
    private Dimension imageDimensions;

    
    
    /** Creates a new instance of ImagePanel */
    public ImagePanel(String imageLocator) {
        setImage(imageLocator);
    }
    public ImagePanel(String imageLocator, Color bgcolor) {
        setImage(imageLocator);
        this.setBackground(bgcolor);
    }
    
    
    public void setImage(String imageLocator) {
        if(imageLocator != null) {
            image = null;
            try {
                this.url = new URL(imageLocator);
                this.image = ImageIO.read(url);
                //System.out.println("ImagePanel initialized with URL "+ imageLocator);
            }
            catch (Exception e) {
                try {
                    this.file = new File(imageLocator);
                    this.image = ImageIO.read(file);
                    //System.out.println("ImagePanel initialized with FILE "+ imageLocator);
                }
                catch (Exception e2) {
                    try {
                        this.url = ClassLoader.getSystemResource(imageLocator);
                        this.image = ImageIO.read(url);
                        //System.out.println("ImagePanel initialized with URL (System Resource) "+ imageLocator);
                    }
                    catch (Exception e3) {
                        //System.out.println("Unable to initialize ImagePanel with "+ imageLocator);
                        e2.printStackTrace();
                    }
                }
            }
            if(image != null) {
                imageDimensions = new Dimension(image.getWidth(), image.getHeight());
                this.setPreferredSize(imageDimensions);
                this.setMaximumSize(imageDimensions);
                this.setMinimumSize(imageDimensions);
            }

        }
        this.revalidate();
    }
    
    
    
  
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(image != null) {
            //System.out.println(" image x =" + imageDimensions.width + ", y=" + imageDimensions.height );
            int x = (this.getSize().width - imageDimensions.width) / 2;
            int y = (this.getSize().height - imageDimensions.height) / 2;
            if(x < 0) x = 0;
            if(y < 0) y = 0;
            g.drawImage(image,x ,y ,this);
        }
    }
    
    
}
