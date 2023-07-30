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
package org.wandora.application.tools.git;

import java.io.File;
import java.io.IOException;

import javax.swing.Icon;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapType;
import org.wandora.topicmap.TopicMapTypeManager;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.topicmap.packageio.DirectoryPackageInput;
import org.wandora.topicmap.packageio.DirectoryPackageOutput;
import org.wandora.topicmap.packageio.PackageInput;
import org.wandora.topicmap.packageio.PackageOutput;
import org.wandora.topicmap.packageio.ZipPackageInput;


/**
 *
 * @author akikivela
 */
public abstract class AbstractGitTool extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private static String username = null;
    private static String password = null;
    
    
    
    public File getCurrentProjectFile() {
        Wandora wandora = Wandora.getWandora();
        String fname = wandora.getCurrentProjectFileName();
        if(fname != null) {
            File f = new File(fname);
            return f;
        }
        return null;
    }
    
    
    public void saveWandoraProject() throws IOException, TopicMapException {
        Wandora wandora = Wandora.getWandora();
        String fname = wandora.getCurrentProjectFileName();
        if(fname != null) {
            File f = new File(fname);
            log("Saving project to '" + f.getName() + "'.");
            TopicMap tm = wandora.getTopicMap();
            TopicMapType tmtype = TopicMapTypeManager.getType(tm);

            if(f.isDirectory() || f.exists()) {
                PackageOutput out = new DirectoryPackageOutput(f.getAbsolutePath());
                tmtype.packageTopicMap(tm, out, "", getCurrentLogger());  
                out.close();
            }
        }
    }
    
    
    public void reloadWandoraProject() {
        Wandora wandora = Wandora.getWandora();
        String fname = wandora.getCurrentProjectFileName();
        if(fname != null) {
            File f = new File(fname);
            log("Reloading Wandora project '" + f.getPath() + "'.");

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
                TopicMap tm = type.unpackageTopicMap(in,"",getCurrentLogger(),wandora);
                in.close();
                if(tm != null) {
                    wandora.setTopicMap((LayerStack)tm);
                    wandora.setCurrentProjectFileName(f.getAbsolutePath());
                }
            }
            catch(Exception e){
                log(e);
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public Git getGit() {
        String currentProject = null;
        try {
            Wandora wandora = Wandora.getWandora();
            currentProject = wandora.getCurrentProjectFileName();
            if(currentProject == null) return null;
            File currentProjectFile = new File(currentProject);
            if(currentProjectFile.exists() && currentProjectFile.isDirectory()) {
                Git git = Git.open(currentProjectFile);
                return git;
            }
        } 
        catch (IOException ex) {
            // log("Can't read git repository in '"+currentProject+"'.");
        }
        return null;
    }
    
    
    public String getGitRemoteUrl() {
        try {
            return getGit().getRepository().getConfig().getString( "remote", "origin", "url" );
        }
        catch(Exception e) {}
        return null;
    }
    
    
    
    public void setGitRemoteUrl(String remoteUrl) {
        try {
            StoredConfig config = getGit().getRepository().getConfig();
            config.setString("remote", "origin", "url", remoteUrl);
            config.save();
        }
        catch(Exception e) {}
    }
    
    
    
    public StoredConfig getGitConfig() {
        try {
            return getGit().getRepository().getConfig();
        }
        catch(Exception e) {}
        return null;
    }
    
    
    
    public void logAboutMissingGitRepository() {
        setDefaultLogger();
        setLogTitle("Git repository is missing");
        log("Current project is not a git repository.");
        log("Initialize the git repository first.");
        log("Ready.");
    }
    
    
    

    public String getDefaultCommitMessage() {
        return "Wandora project changes.";
    }
    
    
    public boolean isNotEmpty(String str) {
        if(str == null) return false;
        if(str.length() == 0) return false;
        return true;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    public String getUsername() {
        return username;
    }
    
    
    
    
    public void setUsername(String u) {
        username = u;
    }
    
    
    
    public String getPassword() {
        return password;
    }
    
    
    
    public void setPassword(String p) {
        password = p;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf1d3);
    }
}
