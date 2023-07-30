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
 * OpenCycExtractor.java
 * 
 */



package org.wandora.application.tools.extractors.opencyc;



import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;



/**
 * OpenCyc API has been closed and this extractor (and all other OpenCyc
 * extractors are more or less deprecated. However, the code is still
 * kept in safe if the API opens up later on or somebody publishes
 * similar API.
 *
 * @author akivela
 */
public class OpenCycExtractor extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	private static OpenCycExtractorSelector selector = null;
    
    @Override
    public String getName() {
        return "OpenCyc extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert various OpenCyc XML feeds to topic maps";
    }
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_opencyc.png");
    }
    
    
    
    public void execute(Wandora admin, Context context) {
        int counter = 0;   
        try {
            if(selector == null) {
                selector = new OpenCycExtractorSelector(admin);
            }
            selector.setAccepted(false);
            selector.setWandora(admin);
            selector.setContext(context);
            selector.setVisible(true);
            if(selector.wasAccepted()) {
                setDefaultLogger();
                WandoraTool extractor = selector.getWandoraTool(this);
                if(extractor != null) {
                    extractor.setToolLogger(getDefaultLogger());
                    extractor.execute(admin, context);
                }
            }
            else {
                //log("User cancelled the extraction!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(selector != null && selector.wasAccepted()) setState(WAIT);
    }

}

