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
 */
package org.wandora.application.tools.git;

import java.util.Set;
import org.eclipse.jgit.api.Git;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;



/**
 *
 * @author akikivela
 */
public class Commit extends AbstractGitTool implements WandoraTool {
    
    private CommitUI commitUI = null;
    
    
    @Override
    public void execute(Wandora wandora, Context context) {

        try {
            if(commitUI == null) {
                commitUI = new CommitUI();
            }
            
            commitUI.openInDialog();
            
            if(commitUI.wasAccepted()) {
                setDefaultLogger();
                setLogTitle("Git commit");
                Git git = getGit();
                
                if(git != null) {
                    log("Saving project.");
                    saveWandoraProject();
                    
                    log("Removing missing files.");
                    org.eclipse.jgit.api.Status status = git.status().call();
                    Set<String> missing = status.getMissing();
                    if(missing != null && !missing.isEmpty()) {
                        for(String missingFile : missing) {
                            git.rm()
                                    .addFilepattern(missingFile)
                                    .call();
                        }
                    }
                    
                    log("Adding new files to local repository.");
                    git.add()
                            .addFilepattern(".")
                            .call();
                    
                    log("Committing changes to local repository.");
                    String commitMessage = commitUI.getMessage();
                    if(commitMessage == null || commitMessage.length() == 0) {
                        commitMessage = getDefaultCommitMessage();
                        log("No commit message provided. Using default message.");
                    }
                    git.commit()
                            .setMessage(commitMessage)
                            .call();
                    log("Ready.");
                }
                else {
                    log("Current project is not a git directory. Can't commit.");
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    
    
    @Override
    public String getName() {
        return "Git commit";
    }
    
    
    @Override
    public String getDescription() {
        return "Saves current project and commits changes to local git repository.";
    }
    
    
}
