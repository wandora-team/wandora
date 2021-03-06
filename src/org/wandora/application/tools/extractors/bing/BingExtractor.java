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
 * BingExtractor.java
 *
 * Created on 19.12.2009, 19:47:39
 */



package org.wandora.application.tools.extractors.bing;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;


/**
 *
 * @author akivela
 */
public class BingExtractor extends AbstractWandoraTool {

    private static BingExtractorSelector selector = null;

    @Override
    public String getName() {
        return "Bing extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert various Bing XML feeds to topic maps";
    }
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_bing.png");
    }








    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            if(selector == null) {
                selector = new BingExtractorSelector(wandora);
            }
            selector.setAccepted(false);
            selector.setWandora(wandora);
            selector.setContext(context);
            selector.setVisible(true);
            if(selector.wasAccepted()) {
                setDefaultLogger();
                WandoraTool extractor = selector.getWandoraTool(this);
                if(extractor != null) {
                    extractor.setToolLogger(getDefaultLogger());
                    extractor.execute(wandora, context);
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
        else setState(CLOSE);
    }




    // -------------------------------------------------------------------------




    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        BingExtractorConfiguration dialog=new BingExtractorConfiguration(wandora,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
    }


}
