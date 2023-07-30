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
 * MergeWandoraProject.java
 *
 * Created on 11. joulukuuta 2006, 10:50
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
 *
 * @author akivela
 */
public class MergeWandoraProject extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	public File forceFile = null;
    
    
    /** 
     * Creates a new instance of MergeWandoraProject 
     */
    public MergeWandoraProject() {
    }
    public MergeWandoraProject(String projectFilename) {
        this(new File(projectFilename));
    }
    public MergeWandoraProject(File projectFile) {
        this.forceFile = projectFile;
    }

    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(forceFile == null) {
            SimpleFileChooser chooser = UIConstants.getWandoraProjectFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle("Merge Wandora project");
            chooser.setApproveButtonText("Merge");
            if(chooser.open(wandora)==SimpleFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                mergeProject(f, wandora);
            }
        }
        else {
            mergeProject(forceFile, wandora);
            forceFile = null;
        }
    }
    
    
    public void mergeProject(File f, Wandora wandora) {
        if(f == null) {
            return;
        }
        
        if(f.exists() && !f.isDirectory() && !f.getName().toUpperCase().endsWith(".WPR")) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "The project file doesn't end with Wandora project suffix WPR.\nDo you want to open the file as a Wandora project?", "Wrong file suffix", WandoraOptionPane.YES_NO_OPTION);
            if(a == WandoraOptionPane.NO_OPTION) return;
        }
        
        setDefaultLogger();
        setLogTitle("Merging Wandora project");
        log("Merging Wandora project '" + f.getPath() + "'.");
        
        wandora.options.put("current.directory", f.getPath());
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
            
            TopicMapType type = TopicMapTypeManager.getType(org.wandora.topicmap.layered.LayerStack.class);
            TopicMap tm = wandora.getTopicMap();
            TopicMap results = type.unpackageTopicMap(tm,in,"",getCurrentLogger(),wandora);
            in.close();

            if(results != null) {
                // Next is used to signalize Wandora to redraw the layer stack
                wandora.setTopicMap((LayerStack)tm);
                if(!forceStop()) {
                    setState(CLOSE);
                }
                else {
                    log("Merging interrupted.");
                    log("Project was only partially merged to Wandora.");
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
        return UIBox.getIcon("gui/icons/merge_project.png");
    }
    @Override
    public String getName() {
        return "Merge project";
    }
    @Override
    public String getDescription() {
        return "Merges Wandora project to the current project in Wandora. Wandora imports topic map layers in merged project file.";
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

