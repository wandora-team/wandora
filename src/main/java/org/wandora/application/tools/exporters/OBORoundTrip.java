/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 */

package org.wandora.application.tools.exporters;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.importers.OBO;
import org.wandora.application.tools.importers.OBOConfiguration;
import org.wandora.application.tools.importers.OBOImport;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;


/**
 *
 * @author akivela
 */
public class OBORoundTrip extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;


	@Override
    public String getName() {
        return "OBO round trip";
    }
    
    @Override
    public String getDescription() {
        return "Imports OBO file, converts it to a topic map, and exports topic map back to OBO file. "+
                "Separate topic maps are used for round trip. Wandora's topic map is not modified. "+
                "Exported OBO files are saved to 'wandora_round_trip' folder with same file name leaving original files untouched.";
    }
    
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createExportType();
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_export_obo.png");
    }

    
    // **** Configuration ****
    @Override
    public void initialize(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        String o=options.get(OBO.optionPrefix+"options");
        if(o!=null){
            int i=Integer.parseInt(o);
            OBO.setOptions(i);
            System.out.println("oboroundtrip init:"+i);
        }
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        //System.out.println(prefix);
        OBOConfiguration dialog=new OBOConfiguration(wandora,true);
        dialog.setOptions(OBO.getOptions());
        dialog.setVisible(true);
        if(!dialog.wasCancelled()){
            int i=dialog.getOptions();
            OBO.setOptions(i);
            options.put(OBO.optionPrefix+"options",""+i);
            //System.out.println("oboroundtrip configure:"+i);
        }
    }
    
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
        options.put(OBO.optionPrefix+"options",""+OBO.getOptions());
    }  
    
    
    
    
    
    
    
    public void execute(Wandora wandora, Context context) {      
        try {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle("Select OBO files to round trip");
            if(chooser.open(wandora)==SimpleFileChooser.APPROVE_OPTION) {
                setDefaultLogger();
                importExport(chooser.getSelectedFiles());
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    private void importExport(File[] importFiles) {
        if(importFiles != null && importFiles.length > 0) {
            long starttime = System.currentTimeMillis();
            for(int i=0; i<importFiles.length && !forceStop(); i++) {
                try {
                    TopicMap map = new org.wandora.topicmap.memory.TopicMapImpl();

                    File importFile = importFiles[i];
                    setLogTitle("Importing '"+importFile.getName()+"'");
                    log("Importing '"+importFile.getName()+"'");
                    OBOImport importer = new OBOImport();
                    importer.setToolLogger(this.getCurrentLogger());
                    importer.importOBO(new FileInputStream(importFile), map);

                    OBOExport exporter = new OBOExport();
                    exporter.setToolLogger(this.getCurrentLogger());
                    
                    String exportPath = importFile.getParent()+File.separator+"wandora_round_trip";
                    IObox.createPathFor(new File(exportPath));
                    
                    String exportFileName = exportPath+File.separator+importFile.getName();
                            
                    File exportFile = new File(exportFileName);
                    ArrayList<String> namespaces = importer.getNamespaces();
                    if(namespaces != null && namespaces.size() >0) {
                        log("Exporting '"+exportFile.getName()+"'");
                        setLogTitle("Exporting '"+exportFile.getName()+"'");
                        exporter.exportOBO(exportFile, namespaces.toArray( new String[] {} ), map);
                    }
                    else {
                        log("No valid namespaces found in imported OBO file. Export failed.");
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            long endtime = System.currentTimeMillis();
            long duration = (Math.round(((endtime-starttime)/1000)));
            if(duration > 1) {
                log("Round tripping OBO files took "+duration+" seconds.");
            }
            log("Ready.");
        }
        else {
            System.out.println("No OBO files to import!");
        }
    }
    
}
