/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * JPanelWithBackground.java
 *
 * Created on 27. huhtikuuta 2007, 14:07
 *
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
 * @author akivela
 */
public class JPanelWithBackground extends JPanel {
    URL url;
    File file;
    BufferedImage image;
    
    public static final int TOP_LEFT_ALIGN = 100;
    public static final int BOTTOM_LEFT_ALIGN = 200;
    public int align = TOP_LEFT_ALIGN;
    
    
    /** Creates a new instance of JPanelWithBackground */
    public JPanelWithBackground() {
    }
    
    
    public void setAlign(int newAlign) {
        this.align = newAlign;
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
        }
        this.revalidate();
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d = getSize();
        if(image != null) {
            switch(align) {
                case TOP_LEFT_ALIGN: {
                    g.drawImage(image, 0,0, this);
                    break;
                }
                case BOTTOM_LEFT_ALIGN: {
                    int yoffset = d.height - image.getHeight();
                    g.drawImage(image, 0, yoffset, this);
                    break;
                }
            }
        }

    }
}
