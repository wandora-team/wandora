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
 */
package org.wandora.application.tools.extractors.rekognition;

import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
public class RekognitionExtractor extends AbstractRekognitionExtractor{

    private RekognitionExtractorUI ui;
    
    public RekognitionExtractor() {
        super();
        this.ui = null;
    }
    
    private static final String[] contentTypes=new String[] { 
        "text/plain", "text/json", "application/json" 
    };
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public String getName() {
        return "Rekognition API Extractor";
    }

    @Override
    public String getDescription() {
        return "Extracts topics and associations from the ReKognition API. ";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_rekognition.png");
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        
        String[] forceUrls = this.getForceUrls();
                
        try {
            if(ui == null){
                ui = new RekognitionExtractorUI(forceUrls);
            }

            ui.open(wandora,context);

            if(ui.wasAccepted()) handleUI(wandora, context);


        } catch (TopicMapException e) {
        } finally {
            setState(WAIT);
        }
    }
    
    
    private void handleUI(Wandora wandora, Context context) throws TopicMapException {
        WandoraTool[] extractors;
        
        extractors = ui.getExtractors(this);
        
        if(extractors.length > 0) {
            
            setDefaultLogger();
            
            for (WandoraTool extractor : extractors) {
                try {
                    
                    extractor.setToolLogger(getDefaultLogger());
                    extractor.execute(wandora,context);
                    
                } catch (TopicMapException tme){
                    log(tme);
                }
            }
            
        } else {
            log("Couldn't find a suitable subextractor to perform or "
               +"there was an error with an extractor.");
        }   
    }    

}
