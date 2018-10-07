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
 * LogoAnimation.java
 *
 * Created on 22. toukokuuta 2006, 17:53
 *
 */

package org.wandora.application.gui;



import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import org.wandora.application.*;




/**
 *
 * @author akivela
 */
public class LogoAnimation extends JPanel implements Runnable, MouseListener, ImageObserver {
	
	private static final long serialVersionUID = 1L;
	

    public static final int PASSIVE_SLEEP_TIME = 200000;
    public static final int ACTIVE_SLEEP_TIME = 50;
    
    public static final int ANIM_LENGTH = 59;
    
    private BufferedImage[] images;
    private int currentImageIndex = 0;
    private int imageWidth;
    private int imageHeight;
    private Wandora wandora;
    private Graphics imageGraphics;
    private boolean animate;
    private Thread imageThread = null;
    
    
    /** Creates a new instance of LogoAnimation */
    public LogoAnimation(Wandora w) {
        initialize(w);
    }
    
    
    private void initialize(Wandora w) {
        this.wandora = w;
        this.addMouseListener(this);

        JPopupMenu logoMenu = UIBox.makePopupMenu(WandoraMenuManager.getLogoMenu(), w);
        this.setComponentPopupMenu(logoMenu);
        
        images = new BufferedImage[ANIM_LENGTH];
        String imageLocator = null;
        for(int i=0; i<ANIM_LENGTH; i++) {
            imageLocator = "00" + (i+1);
            if(imageLocator.length() < 4) imageLocator = "0" + imageLocator;
            imageLocator = "gui/logoanim/" + imageLocator + ".png";
            images[i] = UIBox.getThumbForLocator(imageLocator);
        }
        animate = false;
        repaint();
    }
    
    
    
    
    
    public void animate(boolean shouldAnimate) {
        if(shouldAnimate && !animate) {
            animate = shouldAnimate;
            if(imageThread == null) {
                imageThread = new Thread(this);
                imageThread.start();
            }
            else {
                imageThread.interrupt();
            }
        }
        else if(!shouldAnimate && animate) {
            animate = shouldAnimate;
        }
    }
    
    

    
    
    @Override
    public void run() {
        while(true) {
            while(animate) {
                try {
                    imageThread.sleep(ACTIVE_SLEEP_TIME);
                }
                catch(Exception e) {
                    // WAKE UP
                }
                currentImageIndex++;
                if(currentImageIndex >= ANIM_LENGTH) currentImageIndex = 0; 
                repaint(10);
            }
            try {
                imageThread.sleep(PASSIVE_SLEEP_TIME);
            }
            catch(Exception e) {
                // WAKE UP
            }
        }
    }
    

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        if((infoflags & ImageObserver.ALLBITS) != 0) {
            repaint(10);
            return false;
        }
        return true;
    }
    
    

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        try {
            g.drawImage(images[currentImageIndex],0,0,this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void clearImage() {
        imageGraphics.setColor(getBackground());
        imageGraphics.fillRect(0, 0, imageWidth, imageHeight);
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {
            animate = false;
        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
    }
}
