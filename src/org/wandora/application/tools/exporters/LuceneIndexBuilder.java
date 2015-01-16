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
 * 
 * LuceneIndexBuilder.java
 *
 * Created on 6. marraskuuta 2006, 14:03
 *
 */



package org.wandora.application.tools.exporters;



import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.indexer.*;
import javax.swing.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import java.io.*;
import java.util.*;





/**
 *
 * @author olli, akivela
 */


public class LuceneIndexBuilder extends AbstractExportTool {
    private boolean checkVisibility=true;
    
    
    /** Creates a new instance of LuceneIndexBuilder */
    public LuceneIndexBuilder() {
    }

    @Override
    public String getName() {
        return "Lucene index builder";
    }
    
    @Override
    public String getDescription() {
        return "Builds Lucene search index out of topic map.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_lucene_index.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    // -------------------------------------------------------------------------
    
    @Override
    public void initialize(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        String temp=options.get(prefix+"checkVisibility");
        if(temp!=null) checkVisibility=Boolean.parseBoolean(temp);
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Lucene index builder options","Lucene index builder options",true,new String[][]{
            new String[]{"Check visibility","boolean",""+checkVisibility},
        });
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        checkVisibility=Boolean.parseBoolean(values.get("Check visibility"));
        writeOptions(admin,options,prefix);
    }
    
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
        options.put(prefix+"checkVisibility",""+checkVisibility);
    }    
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Select Lucene search index export folder");
        if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            try {
                File file = chooser.getSelectedFile();
                TopicMap tm=solveContextTopicMap(wandora, context);
                //TopicMap tm=wandora.getTopicMap();
                if(tm instanceof LayerStack) tm=((LayerStack)tm).getSelectedLayer().getTopicMap();

                TopicMapIndexBuilder indexBuilder=new TopicMapIndexBuilder(this);
                indexBuilder.setCheckVisibility(checkVisibility);
                indexBuilder.processTopicMap(tm, file.getAbsolutePath());
            }
            catch(Exception e){
                log(e);
            }
            setState(WAIT);
        }
    }

}
