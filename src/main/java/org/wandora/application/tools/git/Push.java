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


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;


/**
 *
 * @author akikivela
 */
public class Push extends AbstractGitTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private static PushUI pushUI = null;
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        
        try {
            Git git = getGit();
            if(git != null) {
                if(isNotEmpty(getGitRemoteUrl())) {
                    if(pushUI == null) {
                        pushUI = new PushUI();
                    }

                    pushUI.setPassword(getPassword());
                    pushUI.setUsername(getUsername());
                    pushUI.setRemoteUrl(getGitRemoteUrl());
                    pushUI.openInDialog();

                    if(pushUI.wasAccepted()) {
                        setDefaultLogger();
                        setLogTitle("Git push");

                        String username = pushUI.getUsername();
                        String password = pushUI.getPassword();
                        String remoteUrl = pushUI.getRemoteUrl();

                        setUsername(username);
                        setPassword(password);
                        // setGitRemoteUrl(remoteUrl);

                        PushCommand push = git.push();

                        log("Pushing local changes to upstream.");
                        if(username != null && username.length() > 0) {
                            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( username, password );
                            push.setCredentialsProvider(credentialsProvider);
                        }

                        Iterable<PushResult> pushResults = push.call();

                        for(PushResult pushResult : pushResults) {
                            String pushResultMessage = pushResult.getMessages();
                            if(isNotEmpty(pushResultMessage)) {
                                log(pushResultMessage);
                            }
                        }
                        log("Ready.");
                    }
                }
                else {
                    log("Repository has no remote origin and can't be pushed. " 
                        +"Initialize repository by cloning remote repository to set the remote origin.");
                }
            }
            else {
                logAboutMissingGitRepository();
            }
        }
        catch(TransportException tre) {
            if(tre.toString().contains("origin: not found.")) {
                log("Git remote origin is not found. Check the remote url and remote git repository.");
            }
        }
        catch(GitAPIException gae) {
            log(gae.toString());
        }
        catch(NoWorkTreeException nwte) {
            log(nwte.toString());
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    
      
    @Override
    public String getName() {
        return "Git push";
    }
    
    
    @Override
    public String getDescription() {
        return "Pushes changes to upstream.";
    }
    
    

}
