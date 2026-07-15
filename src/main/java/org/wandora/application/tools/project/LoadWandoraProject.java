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
 * LoadWandoraProject.java
 *
 * Created on 17. helmikuuta 2006, 16:25
 *
 */

package org.wandora.application.tools.project;



import java.io.File;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapType;
import org.wandora.topicmap.TopicMapTypeManager;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.topicmap.packageio.DirectoryPackageInput;
import org.wandora.topicmap.packageio.PackageInput;
import org.wandora.topicmap.packageio.ZipPackageInput;




/**
 * Class implements WandoraTool used to open and load Wandora project file.
 * Wandora project file is a zipped collection of XTM topic maps and one configuration
 * file named options.xml. Each XTM topic map represents one layer in Wandora.
 * Configuration file contains layer name and type settings.
 * 
 * @author akivela
 */
public class LoadWandoraProject extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	public File forceFile = null;
    
    
    /**
     * Creates a new instance of LoadWandoraProject 
     */
    public LoadWandoraProject() {
    }
    /**
     * Creates a new instance of LoadWandoraProject 
     * @param projectFilename is a name for opened Wandora project file.
     */
    public LoadWandoraProject(String projectFilename) {
        this(new File(projectFilename));
    }
    /**
     * Creates a new instance of LoadWandoraProject 
     * @param projectFile is a file of opened Wandora project file.
     */
    public LoadWandoraProject(File projectFile) {
        this.forceFile = projectFile;
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(forceFile == null) {
            SimpleFileChooser chooser=UIConstants.getWandoraProjectFileChooser();
            chooser.setDialogTitle("Open Wandora project");

            if(chooser.open(wandora)==SimpleFileChooser.APPROVE_OPTION) {
                File f=chooser.getSelectedFile();
                loadProject(f, wandora);
            }
        }
        else {
            loadProject(forceFile, wandora);
            forceFile = null;
        }
    }
    
    
    /**
     * Tool uses method loadProject to actually load the project into the Wandora.
     * @param f Wandora project file.
     * @param wandora Wandora object.
     */
    public void loadProject(File f, Wandora wandora) {
        if(f == null) return;

        if(f.exists() && !f.isDirectory() && !f.getName().toUpperCase().endsWith(".WPR")) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "The project file doesn't end with Wandora project suffix WPR.\nDo you want to open the file as a Wandora project?", "Wrong file suffix", WandoraOptionPane.YES_NO_OPTION);
            if(a == WandoraOptionPane.NO_OPTION) return;
        }
        
        setDefaultLogger();
        setLogTitle("Loading Wandora project");

        if(!f.exists()) {
            log("Project '"+f.getName()+"' not found error!");
            setState(WAIT);
            return;
        }
        
        log("Loading Wandora project '" + f.getPath() + "'.");

        try {
            wandora.getTopicMap().clearTopicMapIndexes();
        }
        catch(Exception e) {
            log(e);
        }
        try {
            PackageInput in = null;
            if(!f.isDirectory()) {
                in = new ZipPackageInput(f);
            }
            else {
                in = new DirectoryPackageInput(f);
            }
            TopicMapType type = TopicMapTypeManager.getType(LayerStack.class);
            TopicMap tm = type.unpackageTopicMap(in, "", getCurrentLogger(), wandora);
            in.close();
            if(tm != null) {
                wandora.setTopicMap((LayerStack)tm);
                wandora.setCurrentProjectFileName(f.getAbsolutePath());
                
                if(!forceStop()) {
                    setState(CLOSE);
                }
                else {
                    log("Loading interrupted.");
                    log("Project was only partially loaded.");
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
        return UIBox.getIcon("gui/icons/load_project.png");
    }
    @Override
    public String getName() {
        return "Open project";
    }
    @Override
    public String getDescription() {
        return "Open Wandora project.";
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
