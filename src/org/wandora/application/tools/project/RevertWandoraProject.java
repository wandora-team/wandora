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
 * 
 * RevertWandoraProject.java
 *
 * Created on 17. helmikuuta 2006, 16:25
 *
 */


package org.wandora.application.tools.project;

import org.wandora.topicmap.layered.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.*;

import javax.swing.Icon;
import java.io.*;


/**
 *
 * @author akivela
 */
public class RevertWandoraProject extends AbstractWandoraTool implements WandoraTool {
   
    
    /**
     * Creates a new instance of RevertWandoraProject 
     */
    public RevertWandoraProject() {
    }

    
    
    public void execute(Wandora wandora, Context context) {
        String recentProject = wandora.getCurrentProjectFileName();
        if(recentProject != null && recentProject.length() > 0) {
            File recentProjectFile = new File(recentProject);
            if(recentProjectFile != null && recentProjectFile.exists()) {
                loadProject(recentProjectFile, wandora);
            }
            else {
                singleLog("Recent project file not found!");
            }
        }
        else {
            singleLog("No recent project file available!");
        }
    }
    
    
    /**
     * Tool uses method loadProject to actually load the project into the Wandora.
     * @param f Wandora project file.
     * @param wandora Wandora object.
     */
    public void loadProject(File f, Wandora wandora) {
        if(f == null) return;
        if(!f.getName().toUpperCase().endsWith(".WPR")) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Your project file doesn't end with Wandora project suffix WPR.\nDo you still want to open given file as Wandora project?", "Wrong file suffix", WandoraOptionPane.YES_NO_OPTION);
            if(a == WandoraOptionPane.NO_OPTION) return;
        }
        setDefaultLogger();
        setLogTitle("Reverting Wandora Project");

        if(!f.exists()) {
            log("File '"+f.getName()+"' not found error!");
            setState(WAIT);
            return;
        }
        
        log("Reverting Wandora project '" + f.getPath() + "'.");

        try {
            wandora.getTopicMap().clearTopicMapIndexes();
        }
        catch(Exception e) {
            log(e);
        }
        try {
            PackageInput in=new ZipPackageInput(f);
            TopicMapType type=TopicMapTypeManager.getType(LayerStack.class);
            TopicMap tm=type.unpackageTopicMap(in,"",getCurrentLogger(),wandora);
            in.close();
            if(tm != null) {
                wandora.setTopicMap((LayerStack)tm);
                wandora.setCurrentProjectFileName(f.getAbsolutePath());
                
                if(!forceStop()) {
                    setState(CLOSE);
                }
                else {
                    log("Revert interrupted!");
                    log("Project was only partially loaded!");
                    setState(WAIT);
                }
            }
            else {
                setState(WAIT);
            }
        }
        catch(Exception e){
            log(e);
            setState(WAIT);
        }
    }
    
    
    
   @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/revert_project.png");
    }
    @Override
    public String getName() {
        return "Revert project";
    }
    @Override
    public String getDescription() {
        return "Reverts to recently opened Wandora project file (wpr).";
    }
    @Override
    public boolean runInOwnThread() {
        return true;
    }
    @Override
    public boolean requiresRefresh() {
        return true;
    }
}
