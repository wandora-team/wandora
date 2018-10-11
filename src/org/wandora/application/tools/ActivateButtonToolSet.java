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
 */

package org.wandora.application.tools;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.utils.*;
import javax.swing.*;



/**
 * Activates button tool set. Button tools sets are created in ToolManager2.
 * Button tool set is viewed in Wandora window below topic menu bar. Activated
 * button tool set is given as an argument to constructor.
 * 
 * @author akivela
 */
public class ActivateButtonToolSet extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private String toolSetName = null;


    public ActivateButtonToolSet() {
        toolSetName = WandoraToolType.WANDORA_BUTTON_TYPE;
    }


    public ActivateButtonToolSet(String setName) {
        toolSetName = setName;
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/button_set.png");
    }


    @Override
    public String getName() {
        return "Activate set '"+toolSetName+"'";
    }
    @Override
    public String getDescription() {
        return "Activates button tool set named '"+toolSetName+"'";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Options options = wandora.getOptions();
            if(options != null) {
                options.put("gui.toolPanel.currentToolSet", toolSetName);
                wandora.refreshToolPanel();
                wandora.refresh();
            }
        }
        catch(Exception e) {
            log(e);
        }
    }


    @Override
    public boolean requiresRefresh() {
        return true;
    }
}
