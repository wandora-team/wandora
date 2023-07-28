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
 * 
 * SimpleCheckBox.java
 *
 * Created on 29. joulukuuta 2005, 19:22
 *
 */

package org.wandora.application.gui.simple;




import java.awt.*;
import javax.swing.*;
import org.wandora.application.gui.*;

/**
 *
 * @author akivela
 */
public class SimpleCheckBox extends JCheckBox {


    public SimpleCheckBox() {
        this.setFont(UIConstants.buttonLabelFont);
        UIConstants.setFancyFont(this);
        this.setIcon(UIBox.getIcon("resources/gui/icons/checkbox.png"));
        this.setSelectedIcon(UIBox.getIcon("resources/gui/icons/checkbox_selected.png"));
        this.setFocusPainted(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // this.setUI(new SimpleCheckBoxUI());
        // this.setBackground(UIConstants.checkBoxBackgroundColor);
    }

    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }

}
