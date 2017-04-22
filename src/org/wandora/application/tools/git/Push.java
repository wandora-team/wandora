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
public class Push extends AbstractGitTool implements WandoraTool {
    
    private static PushUI pushUI = null;
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        
        try {
            if(pushUI == null) {
                pushUI = new PushUI();
            }
            
            GitSettings gitSettings = getGitSettings();
            
            pushUI.setPassword(gitSettings.getPassword());
            pushUI.setUsername(gitSettings.getUsername());
            pushUI.openInDialog();
            
            if(pushUI.wasAccepted()) {
                setDefaultLogger();
                setLogTitle("Pushing");
                Git git = getGit();
                if(git != null) {
                    String username = pushUI.getUsername();
                    String password = pushUI.getPassword();

                    log("Pushing ");
                    if(username != null && username.length() > 0) {
                        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( username, password );
                        git.push()
                                .setCredentialsProvider(credentialsProvider)
                                .call();
                    }
                    else {
                        git.push()
                                .call();
                    }
                    log("Ready.");
                }
                else {
                    log("Current project is not a git directory. Can't push.");
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
        return "Git push";
    }
    
    
    @Override
    public String getDescription() {
        return "Pushes changes to upstream.";
    }
    
    

}
