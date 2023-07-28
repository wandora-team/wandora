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
 */

package org.wandora.application.tools.extractors.freebase;

import java.io.File;
import java.net.URL;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author
 * Eero Lehtonen
 */


public class FreebaseExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;
	
	private FreebaseExtractorUI ui = null;
    
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createExtractType();
    }
    @Override
    public String getName(){
        return "Freebase Extractor";
    }
    @Override
    public String getDescription(){
        return "Extracts topics and associations from the Freebase API. "+
                "A personal api-key is required for the API access.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_freebase.png");
    }
    private final String[] contentTypes=new String[] { "text/plain", "text/json", "application/json" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    // -------------------------------------------------------------------------
    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            if(ui == null) {
                ui = new FreebaseExtractorUI();
            }

            ui.open(wandora, context);

            if(ui.wasAccepted()) {
                WandoraTool[] extrs = null;
                try{
                      extrs = ui.getExtractors(this);
                } catch(Exception e) {
                    log(e.getMessage());
                    return;
                }
                if(extrs != null && extrs.length > 0) {
                    setDefaultLogger();
                    int c = 0;
                    log("Performing Freebase API query...");
                    for(int i=0; i<extrs.length && !forceStop(); i++) {
                        try {
                            log(extrs[i].toString());
                            WandoraTool e = extrs[i];
                            e.setToolLogger(getDefaultLogger());
                            e.execute(wandora);
                            c++;
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                    log("Done.");
                }
                else {
                    log("Couldn't find a suitable subextractor to perform or there was an error with an extractor.");
                }
            }
        } catch(Exception e) {
            singleLog(e);
        }
        if(ui != null && ui.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }
    
    // -------------------------------------------------------------------------
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other extractors for Freebase. It doesn't perform extration it self.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other extractors for Freebase. It doesn't perform extration it self.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other extractors for Freebase. It doesn't perform extration it self.");
    }
    
}
