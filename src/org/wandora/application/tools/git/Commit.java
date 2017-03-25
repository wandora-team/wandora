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
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;

/**
 *
 * @author akikivela
 */
public class Commit extends AbstractWandoraTool implements WandoraTool {
    
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
                setLogTitle("Commit");

                String commitMessage = commitUI.getMessage();
                String currentProject = wandora.getCurrentProjectFileName();
                File currentProjectFile = new File(currentProject);

                if(currentProjectFile.exists() && currentProjectFile.isDirectory()) {
                    log("Committing");
                    Git.open(currentProjectFile).commit().setMessage(commitMessage).call();
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
    
}
