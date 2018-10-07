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
 * SplashWindow.java
 *
 * Created on 15. kes�kuuta 2006, 12:00
 *
 */

package org.wandora.application.gui;

import javax.swing.*;
import java.awt.*;



/**
 *
 * @author akivela
 */
public class SplashWindow extends JWindow {
    

	private static final long serialVersionUID = 1L;

	
	protected JLabel splashLabel = null;
    
    
    /** Creates a new instance of SplashWindow */
    public SplashWindow() {
        this.setLayout(new BorderLayout());
        this.setSize(450, 186);
        this.setPreferredSize(this.getSize());
        
        this.splashLabel = new JLabel();
        this.splashLabel.setIcon(UIBox.getIcon("gui/splash.gif"));
        this.add(splashLabel);
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((dim.width - this.getWidth()) / 2, (dim.height - this.getHeight()) / 2);
        this.pack();
        
        this.setVisible(true);
    }
    
}
