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
 */



package org.wandora.application.tools.extractors.yahoo.boss;

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
public class BossExtractor extends AbstractWandoraTool {


	private static final long serialVersionUID = 1L;
	
	private BossExtractorSelector selector = null;



    @Override
    public String getName() {
        return "Yahoo! BOSS extractor";
    }

    @Override
    public String getDescription(){
        return "Search with Yahoo! BOSS and convert query and the result set to Topic Maps. Read more at http://developer.yahoo.com/search/boss.";
    }

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_yahoo.png");
    }




    
    public void execute(Wandora wandora, Context context) {
        try {
            if(selector == null) {
                selector = new BossExtractorSelector(wandora);
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
                // log("User cancelled the extraction!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(selector != null && selector.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }




}
