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

import java.io.IOException;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akikivela
 */
public class CommitPush extends AbstractGitTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private CommitPushUI commitPushUI = null;
    
    
    @Override
    public void execute(Wandora wandora, Context context) {

        try {
            Git git = getGit();    
            if(git != null) {
                if(isNotEmpty(getGitRemoteUrl())) {
                    if(commitPushUI == null) {
                        commitPushUI = new CommitPushUI();
                    }

                    commitPushUI.setPassword(getPassword());
                    commitPushUI.setUsername(getUsername());
                    commitPushUI.openInDialog();

                    if(commitPushUI.wasAccepted()) {
                        setDefaultLogger();
                        setLogTitle("Git commit and push");

                        saveWandoraProject();

                        log("Removing deleted files from local repository.");
                        org.eclipse.jgit.api.Status status = git.status().call();
                        Set<String> missing = status.getMissing();
                        if(missing != null && !missing.isEmpty()) {
                            for(String missingFile : missing) {
                                git.rm()
                                        .addFilepattern(missingFile)
                                        .call();
                            }
                        }

                        log("Adding new files to the local repository.");
                        git.add()
                                .addFilepattern(".")
                                .call();

                        log("Committing changes to the local repository.");
                        String commitMessage = commitPushUI.getMessage();
                        if(commitMessage == null || commitMessage.length() == 0) {
                            commitMessage = getDefaultCommitMessage();
                            log("No commit message provided. Using default message.");
                        }
                        git.commit()
                                .setMessage(commitMessage)
                                .call();

                        String username = commitPushUI.getUsername();
                        String password = commitPushUI.getPassword();

                        setUsername(username);
                        setPassword(password);

                        PushCommand push = git.push();
                        if(isNotEmpty(username)) {
                            log("Setting push credentials.");
                            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( username, password );
                            push.setCredentialsProvider(credentialsProvider);
                        }
                        log("Pushing upstream.");
                        push.call();

                        log("Ready.");
                    }
                }
                else {
                    log("Repository has no remote origin and can't be pushed. " 
                            + "Commit changes to the local repository using Commit to local... "
                            + "To push changes to a remote repository initialize repository by cloning.");
                }
            }
            else {
                logAboutMissingGitRepository();
            }
        }
        catch(GitAPIException gae) {
            log(gae.toString());
        }
        catch(NoWorkTreeException nwte) {
            log(nwte.toString());
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
        return "Git commit and push";
    }
    
    
    @Override
    public String getDescription() {
        return "Saves current project, commits changes to local git repository and pushes commits to upstream.";
    }
    
    
}
