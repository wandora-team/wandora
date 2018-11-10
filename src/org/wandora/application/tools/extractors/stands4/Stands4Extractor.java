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
 *
 *
 * Stands4Extractor.java
 *
 * Created on 25.3.2010
 */


package org.wandora.application.tools.extractors.stands4;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;

import javax.swing.*;

/**
 *
 * @author akivela
 */
public class Stands4Extractor extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	private static Stands4Selector selector = null;

    @Override
    public String getName() {
        return "Stands4 word describer";
    }

    @Override
    public String getDescription(){
        return "Describes given words using Stands4 web api.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_stands4.png");
    }

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }



    public void execute(Wandora wandora, Context context) {
        int counter = 0;
        try {
            if(selector == null) {
                selector = new Stands4Selector(wandora);
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
    }

}

