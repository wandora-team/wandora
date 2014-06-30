/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * Unknown.java
 *
 * Created on 12. lokakuuta 2007, 17:14
 *
 */

package org.wandora.application.gui.previews.formats;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewPanel;

/**
 *
 * @author anttirt
 */
public class Unknown extends JPanel implements PreviewPanel {
    private BufferedImage bgImage;
    private Wandora admin;
    
    public Unknown(String locator, Wandora admin) {
        this.admin = admin;
        this.bgImage = UIBox.getImage("gui/doctype_unknown.png");

        Dimension dim = new Dimension(bgImage.getWidth(), bgImage.getHeight());
        this.setPreferredSize(dim);
        this.setMinimumSize(dim);
        this.setMaximumSize(dim);
        revalidate();
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(bgImage != null) {
            g.drawImage(bgImage, 0, 0, this);
        }
    }

    @Override
    public void stop() {}
    
    
    @Override
    public void finish() {
    }
    
    
    @Override
    public Component getGui() {
        return this;
    }
    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
}
