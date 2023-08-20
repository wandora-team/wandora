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
 * 
 * WandoraTool.java
 *
 * Created on June 17, 2004, 3:35 PM
 */

package org.wandora.application;




import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.Icon;

import org.wandora.application.contexts.Context;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;



/**
 * The interface for Wandora tools. Wandora tool is a fundamental way of packing
 * actions in Wandora. Instead of implementing this interface directly, you
 * should extend <code>AbstractWandoraTool</code>. It contains implementation
 * for most of the interface methods and eases developing tools.
 * 
 * @see org.wandora.application.tools.AbstractWandoraTool
 * @author  olli, akivela
 */
public interface WandoraTool extends WandoraToolLogger, Serializable {
    
    /**
     * Return tool's name. 
     * 
     * @return String representing tool's name.
     */
    public String getName();
    
    
    /**
     * Returns description of the tool.
     * 
     * @return String representing tool's description.
     */
    public String getDescription();   
    
    
    /**
     * Runs the tool.
     * 
     * @param wandora is the application context.
     * @param actionEvent is the event triggering the event.
     * @throws TopicMapException 
     */
    public void execute(Wandora wandora, ActionEvent actionEvent) throws TopicMapException;
    
    
    /**
     * Runs the tool. This is the main entry to the tool. Here the context
     * 
     * 
     * @param wandora
     * @param context
     * @throws TopicMapException 
     */
    public void execute(Wandora wandora, Context context) throws TopicMapException;
    
    
    /**
     * Runs the tool.
     * 
     * @param wandora
     * @throws TopicMapException 
     */
    public void execute(Wandora wandora) throws TopicMapException;
    
    
    /**
     * Returns boolean value true if the tool is running and false if the
     * execution has ended.
     * 
     * @return true if the tool is running.
     */
    public boolean isRunning();

    
    /** 
     * Set tool's execution context. The context contains topics and associations
     * the tool should process, and application component that was active
     * when the tool was executed. Usually the context is given as an argument
     * while executing the tool.
     * 
     * @param context Tool's new context.
     */
    public void setContext(Context context);
    
    
    /**
     * Get tool's execution context.
     * 
     * @return Context object.
     */
    public Context getContext();

    
    
    /**
     * Returns tool's type. Tool type is a deprecated feature. It was originally
     * used to distinguish tools that import, extract and export something.
     * These days it has no strict meaning in Wandora.
     * 
     * @return Tool's type object.
     */
    public WandoraToolType getType();

    
    // ------------------------------------------------------- CONFIGURATION ---
    
    /** 
     * Read settings from options and initialize tool.
     * 
     * @param wandora
     * @param options
     * @param prefix
     * @throws org.wandora.topicmap.TopicMapException
     */
    public void initialize(Wandora wandora, Options options, String prefix) throws TopicMapException;
    
    
    
    /** 
     * Return true if tool has something to configure.
     * 
     * @return true if the tool is configurable.
     */
    public boolean isConfigurable();
    
    
    /** 
     * Open configuration dialog and allow user to configure the tool. Should save 
     * changes to options after the configuration.
     * 
     * @param wandora
     * @param options
     * @param prefix
     * @throws org.wandora.topicmap.TopicMapException
     */
    public void configure(Wandora wandora, Options options, String prefix) throws TopicMapException;
    
    
    /** 
     * Save current tool settings to options.
     * 
     * @param wandora
     * @param options
     * @param prefix
     */
    public void writeOptions(Wandora wandora,Options options,String prefix);
    
    
    /**
     * Should the Wandora application refresh. Refresh is required if the tool
     * changed options, topics or associations, or any other setting that
     * has an effect to the user interface.
     * 
     * @return true if Wandora application should refresh user interface.
     */
    public boolean requiresRefresh();
    
    
    
    
    public SimpleMenuItem getToolMenuItem(Wandora wandora, String instanceName);
    //public WandoraButton getToolButton(Wandora admin);
    //public WandoraButton getToolButton(Wandora admin, int styleHints);
    
    
    /**
     * Return tool's icon. Wandora views the icon in the user interface.
     * 
     * @return icon for the tool.
     */
    public Icon getIcon();
    
    
    
    // -------------------------------------------------------------- LOGGER ---
    
    
    /**
     * Sets tools logger. Logger is used to output textual message about
     * the progress of tool execution.
     * 
     * @param logger is new logger object.
     */
    public void setToolLogger(WandoraToolLogger logger);
    
    
    /**
     * Shortcut to access tool's logger.
     * 
     * @param message to be logged.
     */
    @Override
    public void hlog(String message);
    
    
    /**
     * Shortcut to access tool's logger.
     * 
     * @param message to be logged.
     */
    @Override
    public void log(String message);
    
    
    /**
     * Shortcut to access tool's logger.
     * 
     * @param message to be logged.
     * @param e Exception to be logged.
     */
    @Override
    public void log(String message, Exception e);
    
    
    /**
     * Shortcut to access tool's logger.
     * 
     * @param e Exception to be logged.
     */
    @Override
    public void log(Exception e);
    
    
    /**
     * Shortcut to access tool's logger.
     * 
     * @param e Error to be logged.
     */
    @Override
    public void log(Error e);
}
