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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author akikivela
 */
public class Init extends AbstractGitTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	@Override
    public void execute(Wandora wandora, Context context) {

        try {
            Git git = getGit();
            if(git == null) {

                File projectDir = getCurrentProjectFile();

                if(projectDir == null || !projectDir.isDirectory()) {
                    SimpleFileChooser chooser = UIConstants.getWandoraProjectFileChooser();
                    chooser.setDialogTitle("Select directory for Wandora project git repository");
                    if(chooser.open(wandora, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                        File f = chooser.getSelectedFile();
                        if(f.exists() && f.isDirectory()) {
                            projectDir = f;
                        }
                    }
                }

                setDefaultLogger();
                setLogTitle("Git init");
                if(projectDir != null && projectDir.exists() && projectDir.isDirectory()) {

                    wandora.setCurrentProjectFileName(projectDir.getAbsolutePath());

                    saveWandoraProject();

                    Git.init()
                        .setDirectory(projectDir)
                        .call();

                    log("Created a new repository at " + projectDir.getAbsolutePath());
                    log("Ready.");
                }
                else {
                    log("No repository directory found.");
                    log("Try again and select directory for the repository.");
                    log("Ready.");
                }
            }
            else {
                setDefaultLogger();
                setLogTitle("Git init");
                log("Current project directory has already been initialized.");
                log("To create new git repository save project to a fresh directory.");
                log("Ready.");
            }
        }
        catch(GitAPIException gae) {
            log(gae.toString());
        }
        catch(IOException ioe) {
            log(ioe.toString());
        }
        catch(TopicMapException tme) {
            log(tme.toString());
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }

    
    
    
    
      
    @Override
    public String getName() {
        return "Git init";
    }
    
    
    @Override
    public String getDescription() {
        return "Initialize local git repository. Initialization is usual method to start "
                + "your own repository. If you wish to use remote repository, consider "
                + "using Clone instead.";
    }
    
    
    
}
