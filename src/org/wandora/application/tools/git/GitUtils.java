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
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author akikivela
 */
public class GitUtils {
    
    private static String localPath = "";
    private static String remotePath = "git@github.com:me/mytestrepo.git";
    private static Repository localRepo = null;
    private static Git git;
    
    
    public static void initialize() throws IOException {
        
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }
    
    
    public static Git getGit() {
        return git;
    }
    
    
    public static void cloneRepository() throws IOException, GitAPIException {
        Git.cloneRepository().setURI(remotePath)
                .setDirectory(new File(localPath)).call();
    }
    
    
    public static void commit() throws IOException, GitAPIException, JGitInternalException {
        git.commit().setMessage("Added myfile").call();
    }
    
    public static void pull() throws IOException, GitAPIException {
        git.pull().call();
    }
    
    public static void push() throws IOException, GitAPIException {
        git.push().call();
    }
}
