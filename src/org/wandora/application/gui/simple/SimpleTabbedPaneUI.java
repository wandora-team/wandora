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
 * SimpleTabbedPaneUI.java
 *
 */


package org.wandora.application.gui.simple;


import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import org.wandora.application.gui.UIConstants;

/**
 *
 * @author akivela
 */


public class SimpleTabbedPaneUI extends BasicTabbedPaneUI {

    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        //return super.getContentBorderInsets(tabPlacement);
        return new Insets(2,2,2,2);
    }
    
    
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        int w = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
        return w+8;
    }
    
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight)  {
        int h = super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
        return h+2;
    }
    

    
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // NO FOCUS INDICATOR
    }
    
    
    
    
    
    
}
