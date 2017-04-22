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
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.contexts.Context;

/**
 *
 * @author akikivela
 */
public class Status extends AbstractGitTool implements WandoraTool {
    
    
    @Override
    public void execute(Wandora wandora, Context context) {

        try {
            setDefaultLogger();
            setLogTitle("Git status");

            Git git = getGit();
            if(git != null) {
                log("Getting git status...");
                org.eclipse.jgit.api.Status status = git.status().call();
                log("Added: " + status.getAdded());
                log("Changed: " + status.getChanged());
                log("Conflicting: " + status.getConflicting());
                log("ConflictingStageState: " + status.getConflictingStageState());
                log("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
                log("Missing: " + status.getMissing());
                log("Modified: " + status.getModified());
                log("Removed: " + status.getRemoved());
                log("Untracked: " + status.getUntracked());
                log("UntrackedFolders: " + status.getUntrackedFolders());
                log("Ready.");
            }
            else {
                log("Current project is not a git directory. Can't print status.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }

          
    
    @Override
    public String getName() {
        return "Git status";
    }
    
    
    @Override
    public String getDescription() {
        return "Prints git status to log window.";
    }
    
    
}
