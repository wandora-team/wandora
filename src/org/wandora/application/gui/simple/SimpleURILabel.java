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
 */



package org.wandora.application.gui.simple;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URI;
import org.wandora.application.gui.UIBox;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */


public class SimpleURILabel extends SimpleLabel {

    private static BufferedImage invalidURIImage = UIBox.getImage("resources/gui/icons/invalid_uri.png");
    
    private String completeLabelString = null;


    
    @Override
    public void setText(String str) {
        if(DataURL.isDataURL(str)) {
            completeLabelString = str;
            String strFragment = str.substring(0, Math.min(str.length(), 64)) + "... ("+str.length()+")";
            System.out.println("strFragment=="+strFragment);
            super.setText(strFragment);
        }
        else {
            completeLabelString = null;
            super.setText(str);
        }
    }
    
    
    @Override
    public String getText() {
        try {
            if(completeLabelString != null) {
                return completeLabelString;
            }
            else {
                return super.getText();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    
    public boolean isValidURI() {
        String u = getText();
        if(u != null && u.length() > 0) {
            try {
                if(DataURL.isDataURL(u)) {
                    return true;
                }
                new URI(u);
            }
            catch(Exception e) {
                return false;
            }
        }
        return true;
    }

    
    
    @Override
    public void paint(Graphics gfx) {
        super.paint(gfx);
        if(!isValidURI()) {
            gfx.drawImage(invalidURIImage, this.getWidth()-18, 0, this);
        }
    }
    
    
    
}
