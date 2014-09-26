/* 
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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



package org.wandora.application.tools;
import org.wandora.topicmap.diff.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import static org.wandora.topicmap.diff.TopicMapDiff.*;
import org.wandora.utils.swing.GuiTools;

import java.io.*;
import java.util.*;
import javax.swing.*;


/**
 * Applies a topic map patch to current topic map. A patch is a special file
 * that captures changes in topic map.
 * 
 * @author olli
 */
public class ApplyPatchTool extends AbstractWandoraTool implements WandoraTool {
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/patch_topicmap.png");
    }


    @Override
    public String getName() {
        return "Topic map patcher";
    }
    
    @Override
    public String getDescription() {
        return "Applies a topic map patch";
    }
        
    @Override
    public void execute(final Wandora admin, Context context) throws TopicMapException  {
        JDialog dialog=new JDialog(admin,"Apply topic map patch",true);
        ApplyPatchToolConfigPanel configPanel=new ApplyPatchToolConfigPanel(admin,dialog);
        dialog.getContentPane().add(configPanel);
        dialog.setSize(440, 240);
        GuiTools.centerWindow(dialog, admin);
        dialog.setVisible(true);
        
        if(configPanel.wasCancelled()) return;        
        setDefaultLogger();

        String filename=null;
        try{
            TopicMap tm=null;
            int mode=configPanel.getMapMode();
            if(mode==ApplyPatchToolConfigPanel.MODE_FILE){
                filename = configPanel.getMapValue();
                if(filename != null && !"".equals(filename)) {
                    log("Reading topic map");
                    tm=openFile(filename);
                }
                else {
                    log("Filename not specified for topic map 1!");
                    log("Cancelling patching.");
                    setState(WAIT);
                    return;
                }
            }
            else if(mode==ApplyPatchToolConfigPanel.MODE_LAYER){
                Layer l=admin.getTopicMap().getLayer(configPanel.getMapValue());
                if(l==null) {
                    log("Layer not found");
                    log("Cancelling patching.");
                    setState(WAIT);
                    return;
                }
                tm=l.getTopicMap();
            }
            else if(mode==ApplyPatchToolConfigPanel.MODE_PROJECT){
                tm=admin.getTopicMap();                
            }
            
            filename=configPanel.getPatchFile();
            Reader reader=new InputStreamReader(new FileInputStream(filename),"UTF-8");
            ArrayList<DiffEntry> diff=null;
            try{
                PatchDiffParser parser=new PatchDiffParser(reader);
                log("Reading patch");
                diff=parser.parse();
            }
            finally{
                reader.close();
            }
                        
            TopicMapDiff tmDiff=new TopicMapDiff();
            if(configPanel.getPatchReverse()) {
                log("Reversing patch");
                diff=tmDiff.makeInverse(diff);
            }
            
            log("Applying patch");
            final ArrayList<PatchException> pes=new ArrayList<PatchException>();
            final Boolean[] yesToAll=new Boolean[]{false};
            final Boolean[] aborted=new Boolean[]{false};
            tmDiff.applyDiff(diff, tm,new PatchExceptionHandler(){
                public boolean handleException(PatchException e){
                    pes.add(e);
                    if(yesToAll[0]) return false;
                    int c=WandoraOptionPane.showConfirmDialog(admin, 
                            "Exception applying patch, do you want to continue?<br>"+
                            e.level+": "+e.message, "Exception applying patch",
                            WandoraOptionPane.YES_TO_ALL_NO_OPTION);
                    if(c==WandoraOptionPane.YES_TO_ALL_OPTION){
                        yesToAll[0]=true;
                        return false;
                    }
                    else if(c==WandoraOptionPane.YES_OPTION) return true;
                    else {
                        aborted[0]=true;
                        return true;
                    }
                }
            });
                        
            if(pes.size()>0){
                log("Encountered following problems while patching");
                for(PatchException pe : pes){
                    log(pe.level+": "+pe.message);
                }
            }
            
            if(aborted[0]) log("Patching aborted");
            
            if(mode==ApplyPatchToolConfigPanel.MODE_FILE){
                filename = configPanel.getMapValue();
                int ind=filename.lastIndexOf(".");
                if(ind==-1) filename+="_patched";
                else filename=filename.substring(0,ind)+"_patched"+filename.substring(ind);
                File f=new File(filename);
                if(f.exists()){
                    int r=WandoraOptionPane.showConfirmDialog(admin, "File "+filename+" already exists, overwrite?");
                    if(r!=WandoraOptionPane.YES_OPTION) {
                        log("Patching aborted");
                        setState(WAIT);
                        return;
                    }
                }
                log("Writing topic map to "+filename);
                tm.exportXTM(filename,getCurrentLogger());
                log("Patched topic map written to "+filename);
            }
            else {
                log("Topic map patched");
            }
            
        } catch(FileNotFoundException fnfe) {
            log("File not found exception occurred while opening file '"+filename+"'.");
        } catch(IOException ioe){
            log(ioe);
        } catch(java.text.ParseException pe){
            log("Parse error in patch file at line "+pe.getErrorOffset()+"<br>"+pe.getMessage());            
        }
        setState(WAIT);
        
    }    

}
