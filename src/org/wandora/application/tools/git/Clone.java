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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.project.LoadWandoraProject;


/**
 *
 * @author akikivela
 */
public class Clone extends AbstractWandoraTool implements WandoraTool {

    
    private CloneUI cloneUI = null;
    
    
    /**
     * Creates a new instance of Clone 
     */
    public Clone() {
    }

    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(cloneUI == null) {
            cloneUI = new CloneUI();
        }
        
        cloneUI.openInDialog();
        
        if(cloneUI.wasAccepted()) {
            setDefaultLogger();
            setLogTitle("Git clone");
            
            String cloneUrl = cloneUI.getCloneUrl();
            String destinationDirectory = cloneUI.getDestinationDirectory();
            String username = cloneUI.getUsername();
            String password = cloneUI.getPassword();
            
            log("Cloning git repository from "+cloneUrl);
            
            try {
                if(username == null) {
                    Git.cloneRepository()
                            .setURI(cloneUrl)
                            .setDirectory(new File(destinationDirectory))
                            .call();
                }
                else {
                    CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( username, password );
                    
                    Git.cloneRepository()
                            .setURI(cloneUrl)
                            .setCredentialsProvider(credentialsProvider)
                            .setDirectory(new File(destinationDirectory))
                            .call();
                }
                
                if(cloneUI.getOpenProject()) {
                    log("Opening project");
                    LoadWandoraProject loadProject = new LoadWandoraProject(destinationDirectory);
                    loadProject.setToolLogger(this);
                    loadProject.execute(wandora, context);
                }
                log("Ready.");
            } 
            catch (GitAPIException ex) {
                log(ex);
            }
            setState(WAIT);
        }
    }
    
    
}
