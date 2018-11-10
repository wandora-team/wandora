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



import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;


/**
 *
 * @author akikivela
 */
public class Pull extends AbstractGitTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	private PullUI pullUI = null;
    
    
    @Override
    public void execute(Wandora wandora, Context context) {

        try {
            Git git = getGit();
            if(git != null) {
                if(isNotEmpty(getGitRemoteUrl())) {
                    PullCommand pull = git.pull();
                    String user = getUsername();
                    if(user == null) {
                        if(pullUI == null) {
                            pullUI = new PullUI();
                        }
                        pullUI.setUsername(getUsername());
                        pullUI.setPassword(getPassword());
                        pullUI.setRemoteUrl(getGitRemoteUrl());

                        pullUI.openInDialog();

                        if(pullUI.wasAccepted()) {
                            setUsername(pullUI.getUsername());
                            setPassword(pullUI.getPassword());
                            // setGitRemoteUrl(pullUI.getRemoteUrl());    
                            
                            // pull.setRemote(pullUI.getRemoteUrl());
                            if(isNotEmpty(getUsername())) {
                                CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( getUsername(), getPassword() );
                                pull.setCredentialsProvider(credentialsProvider);
                            }
                        }
                        else {
                            return;
                        }
                    }

                    setDefaultLogger();
                    setLogTitle("Git pull");

                    log("Pulling changes from remote repository...");
                    PullResult result = pull.call();

                    FetchResult fetchResult = result.getFetchResult();
                    MergeResult mergeResult = result.getMergeResult();
                    MergeStatus mergeStatus = mergeResult.getMergeStatus();

                    String fetchResultMessages = fetchResult.getMessages();
                    if(isNotEmpty(fetchResultMessages)) {
                        log(fetchResult.getMessages());
                    }
                    log(mergeStatus.toString());

                    if(mergeStatus.equals(MergeStatus.MERGED)) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "Reload Wandora project after pull?", "Reload Wandora project after pull?", WandoraOptionPane.YES_NO_OPTION);
                        if(a == WandoraOptionPane.YES_OPTION) {
                            reloadWandoraProject();
                        }
                    }
                    log("Ready.");
                }
                else {
                    log("Repository has no remote origin and can't be pulled. " 
                        + "Initialize repository by cloning remote repository to set the remote origin.");
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
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }

    
    
    
    
      
    @Override
    public String getName() {
        return "Git pull";
    }
    
    
    @Override
    public String getDescription() {
        return "Pulls changes from upstream and optionally reloads current project.";
    }
    
    
}
