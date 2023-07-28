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
 * TopicPanel.java
 *
 * Created on 4. marraskuuta 2005, 12:53
 *
 */

package org.wandora.application.gui.topicpanels;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import javax.swing.*;
import org.wandora.exceptions.OpenTopicNotSupportedException;


/**
 * TopicPanel is an UI element used to view and edit topic map structures
 * such as topics and associations in Wandora. Wandora contains several
 * different topic panels such as GraphTopicPanel and TraditionalTopicPanel,
 * each implementing this interface.
 *
 * @author akivela
 */
public interface TopicPanel extends TopicMapListener {
   
    
    /**
     * Initialize the TopicPanel. Called always before the topic panel is used
     */
    public void init();
    
    
    
    /**
     * Does the topic panel support topic open? In other words can one call the
     * open method with a topic.
     */
    public boolean supportsOpenTopic();
    
    
    /**
     * Open a topic in the topic panel. Usually opening means that
     * the topic is being visualized in some way.
     */
    public void open(Topic topic) throws TopicMapException, OpenTopicNotSupportedException;
    
    
    /**
     * A topic panel should stop all threads and close all (shared) resources. 
     * Stopping closes the topic panel too.
     */
    public void stop();
    
    /**
     * Request topic panel UI refresh. The topic panel should refresh it's
     * content and trigger repaint.
     */
    public void refresh() throws TopicMapException;
    
    /**
     * Topic panel should store all pending changes immediately.
     * Especially topic and association changes are important and should
     * be stored during applyChanges.
     */
    public boolean applyChanges() throws CancelledException,TopicMapException;
    
    /**
     * Return the UI element for the topic panel. Wandora gets topic panel's
     * UI element here.
     */
    public JPanel getGui();
    
    /**
     * Return the active topic in topic panel. Usually the returned
     * topic is the same topic that was opened with open methed.
     */
    public Topic getTopic() throws TopicMapException;
    
    
    /**
     * Return name of the topic panel. Name is viewed in Wandora's UI.
     */
    public String getName();
    
    /**
     * Return title of the topic panel. Name is viewed in the title bar of dockable.
     */
    public String getTitle();
    
    /**
     * Return icon image of the topic panel. Icon is viewed in Wandora's UI. 
     */
    public Icon getIcon();
    
    /**
     * Return integer number that specifies topic panel's order. Order is used
     * to sort topic panel sets. Order number is not used anymore as available
     * panels are read from options.xml and the order in options is the order
     * Wandora views the panels.
     * 
     * @deprecated
     */
    public int getOrder();
    
    /**
     * Topic panel can provide a menu structure that Wandora views in UI.
     * This method returns a plain object array of menu objects. Plain object
     * array is transformed into a menu using UIBox.
     */
    public Object[] getViewMenuStruct();
    public JMenu getViewMenu();
    public JPopupMenu getViewPopupMenu();
   
    /**
     * If this returns true, then the topic panel will not be wrapped inside
     * a scroll pane. Useful if the panel provides its own scrolling.
     */
    public boolean noScroll();
    
    /**
     * A topic panel can store it's own topic history. Topic history is used
     * to navigate backward and forward in Wandora. However, this is a hidden 
     * feature and has NOT been taken into production yet! Thus, Wandora
     * doesn't support the topic panel specific topic histories yet!
     */
    public LocatorHistory getTopicHistory();
}
