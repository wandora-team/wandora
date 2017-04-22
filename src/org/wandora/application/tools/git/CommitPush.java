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

import java.io.File;
import javax.swing.Icon;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;

/**
 *
 * @author akikivela
 */
public class CommitPush extends AbstractGitTool implements WandoraTool {
    
    private CommitPushUI commitPushUI = null;
    
    
    @Override
    public void execute(Wandora wandora, Context context) {

        try {
            if(commitPushUI == null) {
                commitPushUI = new CommitPushUI();
            }
            
            GitSettings gitSettings = getGitSettings();
            
            commitPushUI.setPassword(gitSettings.getPassword());
            commitPushUI.setUsername(gitSettings.getUsername());
            commitPushUI.openInDialog();
            
            if(commitPushUI.wasAccepted()) {
                setDefaultLogger();
                setLogTitle("Commit and push");
                Git git = getGit();
                
                if(git != null) {
                    String commitMessage = commitPushUI.getMessage();
                    String username = commitPushUI.getUsername();
                    String password = commitPushUI.getPassword();
                
                    log("Saving...");
                    saveWandoraProject();
                    
                    log("Adding...");
                    git.add()
                            .addFilepattern(".")
                            .call();
                    
                    log("Committing...");
                    git.commit()
                            .setMessage(commitMessage)
                            .call();
                    
                    if(username != null && username.length() > 0) {
                        log("Pushing with credentials.");
                        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( username, password );
                        git.push()
                                .setCredentialsProvider(credentialsProvider)
                                .call();
                    }
                    else {
                        log("Pushing without credentials.");
                        git.push()
                                .call();
                    }
                    
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
        return "Git commit and push";
    }
    
    
    @Override
    public String getDescription() {
        return "Saves current project, commits changes to local git repository and pushes commits to upstream.";
    }
    
    
}
