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

import javax.swing.Icon;
import org.eclipse.jgit.api.Git;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;

/**
 *
 * @author akikivela
 */
public class Pull extends AbstractGitTool implements WandoraTool {
    
    
    @Override
    public void execute(Wandora wandora, Context context) {

        try {
            setDefaultLogger();
            setLogTitle("Git pull");

            Git git = getGit();
            if(git != null) {
                log("Pulling changes from remote repository...");
                git.pull().call();
                
                int a = WandoraOptionPane.showConfirmDialog(wandora, "Reload Wandora project after pull?", "Reload Wandora project after pull?", WandoraOptionPane.YES_NO_OPTION);
                if(a == WandoraOptionPane.YES_OPTION) {
                    reloadWandoraProject();
                }
                
                log("Ready.");
            }
            else {
                log("Current project is not a git directory. Can't pull.");
            }
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
