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

package org.wandora.application.tools.extractors.europeana;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;

/**
 *
 * @author nlaitinen
 */

public class EuropeanaExtractor extends AbstractEuropeanaExtractor {
    
    private EuropeanaExtractorUI ui = null;

    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            if(ui == null) {
                ui = new EuropeanaExtractorUI();
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
                    log("Performing Europeana API query...");
                    for(int i=0; i<extrs.length && !forceStop(); i++) {
                        try {
                            WandoraTool e = extrs[i];
                            e.setToolLogger(getDefaultLogger());
                            e.execute(wandora);
                            setState(EXECUTE);
                            c++;
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                    log("Ready.");
                }
                else {
                    log("Couldn't find a suitable subextractor to perform or there was an error with an extractor.");
                }
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        
        if(ui != null && ui.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }
}
