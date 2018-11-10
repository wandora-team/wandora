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
 */

package org.wandora.application.tools.extractors.palvelukartta_v2;

import java.util.ArrayList;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;

/**
 *
 * @author akivela
 */


public class PalvelukarttaExtractor extends AbstractWandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	private static PalvelukarttaSelector selector = null;
    
    
    
    @Override
    public String getName() {
        return "Palvelukartta extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert Palvelukartta feeds to a topic map. Palvelukartta (Service Map) is "+
               "a Helsinki/Espoo/Vantaa/Kauniainen region web service providing information regarding the departments and services of cities. "+
               "Helsinki, Espoo, Vantaa and Kauniainen cities locate in Southern Finland. "+
               "Read more at http://www.hel.fi/palvelukartta";
    }
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_palvelukartta.png");
    }
    
    
    
    public void execute(Wandora w, Context context) {
        try {
            if(selector == null) {
                selector = new PalvelukarttaSelector();
            }
            selector.setContext(context);
            selector.openDialog(w);
            if(selector.wasAccepted()) {
                setDefaultLogger();
                ArrayList<WandoraTool> extractors = selector.getSelectedExtractors(this);
                if(extractors != null && !extractors.isEmpty()) {
                    for(WandoraTool extractor : extractors) {
                        extractor.setToolLogger(getDefaultLogger());
                        extractor.execute(w, context);
                    }
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
