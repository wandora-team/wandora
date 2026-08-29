/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 * SplashWindow.java
 *
 * Created on 15.6.2006, 12:00
 *
 */

package org.wandora.application.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.wandora.application.Wandora;
import org.wandora.utils.logger.Log4j2Logger;



/**
 *
 * @author akivela
 */
public class SplashWindow extends JWindow {
	private static final long serialVersionUID = 1L;
	private static final Log4j2Logger logger = Log4j2Logger.getLogger(SplashWindow.class);

	
	protected JLabel splashLabel = null;
    protected JPanel splashPanel = null;
    
    
    
    /** Creates a new instance of SplashWindow */
    public SplashWindow() {
        this.setLayout(new BorderLayout());
        this.setSize(450, 186);
        this.setPreferredSize(this.getSize());
        
        this.splashPanel = new SplashPanel();
        this.splashPanel.setLayout(new BorderLayout());
        this.splashPanel.setSize(450, 186);
        this.splashPanel.setPreferredSize(this.getSize());
        
        this.splashLabel = new JLabel();
        this.splashLabel.setIcon(UIBox.getIcon("gui/splash.gif"));
        this.splashPanel.add(splashLabel);
        
        this.add(splashPanel);
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((dim.width - this.getWidth()) / 2, (dim.height - this.getHeight()) / 2);
        this.pack();
        
        this.setVisible(true);
    }
    
    
    
    public class SplashPanel extends JPanel {
	    private static final long serialVersionUID = 1L;

		@Override
	    public void paint(Graphics g) {
			super.paint(g);
	        
	        try {
	            String[] texts = Wandora.getVersionLicenseInfo();
	            g.setColor(UIConstants.wandoraBlueColorAlt);
	            g.setFont(UIConstants.wandoraVersionInfoFont);
	            for (int i=0; i<texts.length; i++) {
		            int viWidth = g.getFontMetrics().stringWidth(texts[i]);
		            int vix = this.getWidth() / 2 - viWidth / 2;
		            int viy = this.getHeight() - 50 + 10 * i;
		            g.drawString(texts[i], vix, viy);
	            }
	        }
	        catch(Exception e) {
	        	logger.error(e);
	        }
	    }
    }    
}
