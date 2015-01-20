/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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




import org.wandora.application.contexts.Context;
import java.awt.event.ActionEvent;
import javax.swing.*;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.topicmap.*;
import org.wandora.utils.Options;
import java.io.*;



/**
 * The interface for Wandora tools. You should nearly always extend
 * <code>AbstractWandoraTool</code> instead of implementing this class directly.
 * @see org.wandora.application.tools.AbstractWandoraTool
 * @author  olli, akivela
 */
public interface WandoraTool extends WandoraToolLogger, Serializable {
    
    public String getName();
    public String getDescription();   
    
    public void execute(Wandora wandora, ActionEvent actionEvent)  throws TopicMapException;
    public void execute(Wandora wandora, Context context)  throws TopicMapException;
    public void execute(Wandora wandora) throws TopicMapException;
    
    public boolean isRunning();

    
    /** Tool context */
    public void setContext(Context context);
    public Context getContext();

    
    public WandoraToolType getType();

    
    /** Read settings from options and initialize tool. */
    public void initialize(Wandora wandora,Options options,String prefix) throws TopicMapException ;
    /** Return true if tool has something to configure */
    public boolean isConfigurable();
    /** Open configuration dialog and allow user to configure tool. Should save changes to options. */
    public void configure(Wandora wandora,Options options,String prefix) throws TopicMapException ;
    /** Save current tool settings to options. */
    public void writeOptions(Wandora wandora,Options options,String prefix);
    
    public boolean requiresRefresh();
    public SimpleMenuItem getToolMenuItem(Wandora wandora,String instanceName);
    //public WandoraButton getToolButton(Wandora admin);
    //public WandoraButton getToolButton(Wandora admin, int styleHints);
    public Icon getIcon();
    
    
    public void setToolLogger(WandoraToolLogger logger);
    @Override
    public void hlog(String message);
    @Override
    public void log(String message);
    @Override
    public void log(String message, Exception e);
    @Override
    public void log(Exception e);
    @Override
    public void log(Error e);
}


   

