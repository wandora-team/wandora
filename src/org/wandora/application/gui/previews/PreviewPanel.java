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
 * PreviewPanel.java
 */

package org.wandora.application.gui.previews;


import java.awt.Component;

/**
 *
 * @author akivela
 */
public interface PreviewPanel {
    /**
     * Called whenever the PreviewPanel should stop i.e. exit. This is usually
     * called whenever the user closes the preview by closing the topic or by
     * opening another topic. The PreviewPanel should release all resources while
     * stopping. This method is usually called from the PreviewWrapper's stop
     * method.
     */
    public void stop();
    
    public void finish();
    
    /**
     * Is called to get the actual preview component. Wandora places the preview
     * component into the user interface of the application. Usually the returned
     * component is JPanel containing various other components such as images and
     * buttons. This method is usually called from the PreviewWrapper. Running
     * the method has no time limit. PreviewWrapper views a loading message and
     * uses a separate thread to call the getGui, preventing the application to
     * freeze.
     */
    public Component getGui();
    
    /**
     * This is a little deprecated method to tell the preview is a heavy weight
     * AWT component. These days Java can mix heavy weight and swing component,
     * and the isHeavy is not that important any more. Notice, returning a true
     * value actually triggers some additional wrapping for the component returned
     * by the getGui method. See AWTWrapper class for details.
     */
    public boolean isHeavy();
}
