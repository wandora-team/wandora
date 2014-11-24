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
 *
 * 
 * 
 */

package org.wandora.application.tools;


import org.wandora.topicmap.diff.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.utils.swing.GuiTools;

import java.io.*;
import javax.swing.*;


/**
 *
 * @author olli
 */
public class DiffTool extends AbstractWandoraTool implements WandoraTool  {



    @Override
    public String getName() {
        return "Topic map diff";
    }
    
    @Override
    public String getDescription() {
        return "Finds differences between two topic maps";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/compare_topicmaps.png");
    }

    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    
    public TopicMap openFile(String f,Wandora admin) throws IOException, TopicMapException {
        String extension="";
        int ind=f.lastIndexOf(".");
        if(ind>-1) extension=f.substring(ind+1);
        if(extension.equalsIgnoreCase("wpr")){
            PackageInput in=new ZipPackageInput(f);
            TopicMapType type=TopicMapTypeManager.getType(LayerStack.class);
            log("Loading Wandora project file");
            TopicMap tm=type.unpackageTopicMap(in,"",getCurrentLogger(),admin);
            in.close();
            return tm;
        }
        else if(extension.equalsIgnoreCase("ltm")){
            TopicMap tm=new TopicMapImpl();
            log("Loading LTM topic map");
            tm.importLTM(f);
            return tm;
        }
        else if(extension.equalsIgnoreCase("jtm")){
            TopicMap tm=new TopicMapImpl();
            log("Loading JTM topic map");
            tm.importJTM(f);
            return tm;
        }
        else{ // xtm
            TopicMap tm=new TopicMapImpl();
            log("Loading XTM topic map");
            tm.importXTM(f);
            return tm;
        }
    }
    
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        
        JDialog dialog=new JDialog(admin,"Compare topic maps",true);
        DiffToolConfigPanel configPanel=new DiffToolConfigPanel(admin,dialog);
        dialog.getContentPane().add(configPanel);
        dialog.setSize(440, 350);
        configPanel.addFormat("HTML format");
        configPanel.addFormat("Patch format");
        GuiTools.centerWindow(dialog, admin);
        dialog.setVisible(true);
        
        if(configPanel.wasCancelled()) return;
        
        setDefaultLogger();
        setProgressMax(10000);
        
        String filename=null;
        try{
            TopicMap tm1=null;
            TopicMap tm2=null;
            int mode1=configPanel.getMap1Mode();
            if(mode1==DiffToolConfigPanel.MODE_FILE){
                filename = configPanel.getMap1Value();
                if(filename != null && !"".equals(filename)) {
                    tm1=openFile(filename,admin);
                }
                else {
                    log("Filename not specified for topic map 1!");
                    log("Cancelling compare.");
                    setState(WAIT);
                    return;
                }
            }
            else if(mode1==DiffToolConfigPanel.MODE_LAYER){
                Layer l=admin.getTopicMap().getLayer(configPanel.getMap1Value());
                if(l==null) {
                    log("Layer not found");
                    log("Cancelling compare.");
                    setState(WAIT);
                    return;
                }
                tm1=l.getTopicMap();
            }
            else if(mode1==DiffToolConfigPanel.MODE_PROJECT){
                tm1=admin.getTopicMap();
            }

            int mode2=configPanel.getMap2Mode();
            filename = null;
            if(mode2==DiffToolConfigPanel.MODE_FILE){
                filename = configPanel.getMap2Value();
                if(filename != null && !"".equals(filename)) {
                    tm2=openFile(filename,admin);
                }
                else {
                    log("Filename not specified for topic map 2!");
                    log("Cancelling compare.");
                    setState(WAIT);
                    return;
                }
            }
            else if(mode2==DiffToolConfigPanel.MODE_LAYER){
                Layer l=admin.getTopicMap().getLayer(configPanel.getMap2Value());
                if(l==null) {
                    log("Layer not found");
                    log("Cancelling compare.");
                    setState(WAIT);
                    return;
                }
                tm2=l.getTopicMap();
            }
            else if(mode2==DiffToolConfigPanel.MODE_PROJECT){
                tm2=admin.getTopicMap();
            }
        
            TopicMapDiff diffMaker=new TopicMapDiff();
            ToolDiffOutput output=null;
            String format=configPanel.getFormat();
            boolean html=true;
            if(format.equalsIgnoreCase("Patch Format")){
                html=false;
                output=new ToolDiffOutput(new PatchDiffEntryFormatter());
            }
            else{
                output=new ToolDiffOutput(new HTMLDiffEntryFormatter());
            }
            log("Comparing topic maps");
            if(diffMaker.makeDiff(tm1,tm2,output)){
                log("Comparison ready");
                TextEditor e=null;
                if(html) e=new TextEditor(admin,true,"<html>"+output.getResult()+"</html>","text/html");
                else e=new TextEditor(admin,true,output.getResult());
                e.setCancelButtonVisible(false);
                setState(INVISIBLE);
                e.setVisible(true);
                setState(VISIBLE);
                setState(WAIT);
            }
            else{
                setState(WAIT);
            }
        } catch(FileNotFoundException fnfe) {
            log("File not found exception occurred while opening file '"+filename+"'.");
            setState(WAIT);
        } catch(IOException ioe){
            log(ioe);
            setState(WAIT);
        }
    }    
    
    private class ToolDiffOutput extends BasicDiffOutput {
        private int counter=0;
        private StringWriter sw;
        public ToolDiffOutput(DiffEntryFormatter formatter){
            super(formatter,new StringWriter());
            this.sw=(StringWriter)writer;
        }
        private void increaseCounter(){
            counter++;
            setProgress(counter);
        }
        @Override
        public boolean outputDiffEntry(TopicMapDiff.DiffEntry d){
            if(forceStop()) return false;
            increaseCounter();
            try{
                doOutput(d);
            }catch(Exception e){
                log(e);
                return false;
            }
            return true;
        }
        @Override
        public boolean noDifferences(Topic t){
            if(forceStop()) return false;
            increaseCounter();
            return true;
        }
        @Override
        public boolean noDifferences(Association a){
            if(forceStop()) return false;
            increaseCounter();
            return true;
        }
        public String getResult(){
            return sw.toString();
        }
    }
}
