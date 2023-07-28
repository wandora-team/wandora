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
 * PreviewWrapper.java
 *
 * Created on 29. toukokuuta 2006, 14:55
 *
 */

package org.wandora.application.gui.previews;


import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wandora.topicmap.Locator;



/**
 *
 * @author akivela
 */
public class PreviewWrapper extends JPanel {
    private static HashMap<Object,PreviewWrapper> previewWrappers = null;
    
    private PreviewPanel currentPanel = null;
    private Component currentUI = null;
    private Locator currentLocator = null;
    private PreviewWrapperInitializer initializer = null;

    
    /**
     * To create a new preview wrapper, call static method getPreviewWrapper
     * with the caller as an argument. Created preview wrapper is caller
     * specific and same preview wrapped is returned for the same caller. This
     * behavior prevents the preview restarting after small changes in the
     * topic panels.
     */
    private PreviewWrapper() {
        this.setLayout(new BorderLayout());
    }
    
    
    public static PreviewWrapper getPreviewWrapper(Object owner) {
        if(previewWrappers == null) {
            previewWrappers = new HashMap();
        }
        PreviewWrapper previewWrapper = previewWrappers.get(owner);
        if(previewWrapper == null) {
            previewWrapper = new PreviewWrapper();
            previewWrappers.put(owner, previewWrapper);
        }
        return previewWrapper;
    }
    
    
    public static void removePreviewWrapper(Object owner) {
        if(previewWrappers != null) {
            if(previewWrappers.containsKey(owner)) {
                previewWrappers.remove(owner);
            }
            else {
                System.out.println("Found no preview wrapper "+owner);
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    public void stop() {
        if(initializer != null) {
            if(initializer.isAlive()) {
                System.out.println("Setting the preview initializer aborted.");
                initializer.setAborted();
            }
        }
        if(currentPanel != null) {
            // System.out.println("Stopping preview wrapper.");
            currentPanel.stop();
        }
        currentPanel = null;
        currentLocator = null;
        
        removeAll();
    }
    
    
    
    
    public void setURL(final Locator subjectLocator) {
        if(subjectLocator != null && subjectLocator.equals(currentLocator)) {
            return;
        }
        forceSetURL(subjectLocator);
    }
    
    
    

    public void forceSetURL(final Locator subjectLocator) {
        
        if(currentPanel != null) {
            currentPanel.stop();
            currentPanel.finish();
        }
        currentPanel = null;
        currentUI = null;
        
        removeAll();

        currentLocator = subjectLocator;
        if(subjectLocator == null)
            return;

        if(subjectLocator.toExternalForm().equals(""))
            return;

        currentUI = PreviewUtils.previewLoadingMessage(this);
        
        // All the rest of building the preview happens in a separate class
        // extending a thread. This way the application doesn't freeze
        // if the preview initialization takes longer.
        
        initializer = new PreviewWrapperInitializer(subjectLocator, this);
        initializer.start();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public Dimension getPreferredSize() {
        if(currentUI != null) {
            return currentUI.getPreferredSize();
        }
        else {
            //return super.getPreferredSize();
            return new Dimension(0,0);
        }
    }
    
    
    @Override
    public Dimension getMinimumSize() {
        if(currentUI != null) {
            return currentUI.getMinimumSize();
        }
        else {
            //return super.getMinimumSize();
            return new Dimension(0,0);
        }
    }
    
    
    
    /** ------------------------------------------------------------------------
     * PreviewWrapperInitializer is a Thread that solves the preview viewer class,
     * instantiates preview viewer and initialized it. Downloading and initializing
     * the preview in a thread of it's own, Wandora's user interface doesn't 
     * block and freeze if it takes longer. Notice, the thread exits once the
     * preview is ready. If the user stops (exits) the preview for some reason,
     * the isAborted variable is set true and the thread does as little as it can.
     * The thread is never really killed (forced to abort) though. This is because
     * the preview may become unstable if the initialization is stopped carelessly.
     */
    
    protected class PreviewWrapperInitializer extends Thread {
        private final Locator subjectLocator;
        private final PreviewWrapper previewWrapper;
        private boolean isAborted;
        
        
        public PreviewWrapperInitializer(Locator locator, PreviewWrapper wrapper) {
            subjectLocator = locator;
            previewWrapper = wrapper;
            isAborted = false;
        }
        
        
        public void setAborted() {
            isAborted = true;
        }
        
        
        @Override
        public void run() {
            if(!isAborted) {
                try {
                    currentPanel = PreviewFactory.create(subjectLocator);
                    if(!isAborted) {
                        if(currentPanel != null) {
                            String locatorString = subjectLocator.toExternalForm();
                            if(locatorString.length() > 50) locatorString = locatorString.substring(0,50)+"...";
                            System.out.println("Created preview "+currentPanel.getClass()+" for "+locatorString);
                        }
                        else {
                            currentUI = PreviewUtils.previewNoPreview(previewWrapper, subjectLocator);
                        }
                    }
                }
                catch(Exception e) {
                    if(!isAborted) {
                        currentUI = PreviewUtils.previewError(previewWrapper, "Creating preview failed.", e);
                    }
                }
                catch(Error err) {
                    if(!isAborted) {
                        currentUI = PreviewUtils.previewError(previewWrapper, "Creating preview failed.", err);
                    }
                }
            }
            
            if(currentPanel != null && !isAborted) {
                currentUI = currentPanel.getGui();
                if(currentUI != null && !isAborted) {
                    removeAll();
                    add(currentUI, BorderLayout.CENTER);
                    setPreferredSize(currentUI.getPreferredSize());
                }
            }
            
            revalidate();
            repaint();
        }
    
        
    }
    
}
