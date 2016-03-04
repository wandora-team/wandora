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
 *
 * 
 * SaveWandoraProject.java
 *
 * Created on 17. helmikuuta 2006, 16:27
 *
 */

package org.wandora.application.tools.project;



import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.contexts.*;
import org.wandora.*;
import org.wandora.utils.IObox;

import java.io.*;
import javax.swing.Icon;



/**
 *
 * @author akivela
 */
public class SaveWandoraProject extends AbstractWandoraTool implements WandoraTool {
    private boolean useCurrentFileName = false;
    
    

    public SaveWandoraProject() {
    }
    public SaveWandoraProject(boolean u) {
            useCurrentFileName = u;
    }
    
    
    
    public void execute(Wandora admin, Context context) {

        File f = null;
        boolean doSave = false;
        
        try {
            if(useCurrentFileName) {
                String fname = admin.getCurrentProjectFileName();
                if(fname != null) f = new File(fname);
                doSave = true;
            }

            if(f == null) {
                SimpleFileChooser chooser = UIConstants.getWandoraProjectFileChooser();
                chooser.setDialogTitle("Save Wandora project");
                if(chooser.open(admin, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                    f = IObox.addFileExtension(chooser.getSelectedFile(), "wpr");
                    doSave = true;
                }
            }
        }
        catch(Exception e) {
            admin.handleError(e);
        }
        
        if(doSave && f!=null) {
            try {
                if(f.exists() && !useCurrentFileName) {
                    int overWrite = WandoraOptionPane.showConfirmDialog(admin, "Overwrite existing project file '"+f.getName()+"'?", "Overwrite file?", WandoraOptionPane.YES_NO_OPTION);
                    if(overWrite == WandoraOptionPane.NO_OPTION) return;
                }
                setDefaultLogger();
                log("Saving project to '" + f.getName() + "'.");
                TopicMap tm=admin.getTopicMap();
                TopicMapType tmtype=TopicMapTypeManager.getType(tm);
                PackageOutput out=new ZipPackageOutput(new FileOutputStream(f));
                tmtype.packageTopicMap(tm,out,"",getCurrentLogger());  
                out.close();
                admin.setCurrentProjectFileName(f.getAbsolutePath());
                if(!forceStop()) {
                    setState(CLOSE);
                }
                else {
                    log("Saving interrupted!");
                    log("Project was only partially saved!");
                    setState(WAIT);
                }
            }
            catch(Exception e){
                log(e);
                setState(WAIT);
            }
        }
    }
    
    
    
   @Override
    public Icon getIcon() {
       if(useCurrentFileName) return UIBox.getIcon("gui/icons/save_project.png");
       else return UIBox.getIcon("gui/icons/save_project_as.png");
    }
    @Override
    public String getName() {
        if(useCurrentFileName) return "Save project as";
        else return "Save project";
    }
    @Override
    public String getDescription() {
        return "Saves all topic map layers in Wandora to a Wandora project file (wpr).";
    }
    @Override
    public boolean runInOwnThread() {
        return true;
    }
}
