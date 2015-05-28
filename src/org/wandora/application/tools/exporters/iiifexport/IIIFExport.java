/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 */

package org.wandora.application.tools.exporters.iiifexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;

/**
 *
 * @author olli
 */


public class IIIFExport extends AbstractExportTool {
    
    protected HashMap<String,String> options;
    protected IIIFBuilder selectedBuilder;
    protected boolean prettyPrint=true;

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExportType();
    }

    @Override
    public String getDescription() {
        return "Exports image data in IIIF format JSON-LD";
    }

    @Override
    public String getName() {
        return "IIIF Export";
    }
    
    
    
    public ArrayList<IIIFBuilder> getBuilders(){
        // TODO: smart scanning of builders
        ArrayList<IIIFBuilder> al=new ArrayList<>();
        al.add(new SimpleSelectionIIIFBuilder());
        al.add(new SelectionInstancesIIIFBuilder());
        al.add(new FullIIIFBuilder());
        return al;
    }
    
    public IIIFBuilder resolveBuilder(String name){
        ArrayList<IIIFBuilder> builders=getBuilders();
        for(IIIFBuilder builder : builders){
            if(builder.getBuilderName().equals(name)) {
                return builder;
            }
        }
        return null;
    }
    
    @Override
    public void initialize(Wandora wandora, Options options, String prefix) throws TopicMapException {
        super.initialize(wandora, options, prefix); 

        if(options!=null){
            String builderStr=options.get(prefix+"builder");
            selectedBuilder=null;
            ArrayList<IIIFBuilder> builders=getBuilders();
            if(builderStr!=null){
                selectedBuilder=resolveBuilder(builderStr);
                if(selectedBuilder==null){
                    WandoraOptionPane.showMessageDialog(wandora, "Cannot find IIIF builder \""+builderStr+"\" which was specified in options.");
                }
            }
            if(selectedBuilder==null){
                selectedBuilder=builders.get(0);
            }

            prettyPrint=options.getBoolean(prefix+"prettyprint",prettyPrint);
        }
    }

    
    
    @Override
    public void writeOptions(Wandora wandora, Options options, String prefix) {
        if(selectedBuilder!=null) options.put(prefix+"builder",selectedBuilder.getBuilderName());
        options.put(prefix+"prettyprint",""+prettyPrint);
    }

    @Override
    public void configure(Wandora wandora, Options options, String prefix) throws TopicMapException {
        ArrayList<IIIFBuilder> builders=getBuilders();
        String builderOptions="";
        for(IIIFBuilder builder : builders){
            if(!builderOptions.isEmpty()) builderOptions+=";";
            builderOptions+=builder.getBuilderName();
        }
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora, "IIIF Export options", "Options for IIIF Export", true, new String[][]{
            new String[]{"IIIF Builder","combo:"+builderOptions,builders.get(0).getBuilderName(),"Select the builder that transforms topic map constructs into the IIIF model"},
            new String[]{"Pretty print","boolean",""+prettyPrint,"Add line feeds and tabs into output"}
        }, wandora);
        
        god.setVisible(true);
        if(!god.wasCancelled()){
            Map values=god.getValues();
            String builderStr=values.get("IIIF Builder").toString();
            selectedBuilder=resolveBuilder(builderStr);
            options.put(prefix+"builder", builderStr);
            prettyPrint=Boolean.parseBoolean(values.get("Pretty print").toString());
            options.put(prefix+"prettyprint", ""+prettyPrint);
        }
    }

    /*
     These two methods are mostly intended for situations where the export is
     inside other code and normal configuration dialogs and options mechanics
     are skipped.
    */
    public void setBuilder(IIIFBuilder builder){
        selectedBuilder=builder;
    }
    
    public void setPrettyPrint(boolean p){
        prettyPrint=p;
    }
    
    @Override
    public boolean isConfigurable() {
        return true;
    }

    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("IIIF Export");
        if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();        
            File file = chooser.getSelectedFile();
            
            Manifest m=selectedBuilder.buildIIIF(wandora, context, this);
            
            StringBuilder sb=new StringBuilder();
            m.toJsonLD().outputJson(sb, prettyPrint?"":null );
            
            try (FileOutputStream os=new FileOutputStream(file)) {
                OutputStreamWriter out=new OutputStreamWriter(os,"UTF-8");
                
                out.write(sb.toString());
                out.flush();
            }
            catch(IOException ioe){
                log(ioe);
            }
            
            setState(WAIT);
        }
        
    }
    
}
