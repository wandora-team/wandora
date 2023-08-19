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
 */
package org.wandora.application.gui.simple;

import java.awt.AWTKeyStroke;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.JPasswordField;

import org.wandora.utils.EasyVector;

/**
 *
 * @author akikivela
 */
public class SimplePasswordField extends JPasswordField {
    
    private static final long serialVersionUID = 1L;
    
    protected Insets defaultMargins = new Insets(3,3,3,3);
    
    public SimplePasswordField() {
        initialize();
    }
    
    
    
    public void initialize() {

        this.setFocusTraversalKeysEnabled(true);
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,new HashSet(new EasyVector(new Object[]{AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,0)})));
        this.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,new HashSet(new EasyVector(new Object[]{AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,InputEvent.SHIFT_DOWN_MASK)})));

        this.setMargin(defaultMargins);
        
    }
}
