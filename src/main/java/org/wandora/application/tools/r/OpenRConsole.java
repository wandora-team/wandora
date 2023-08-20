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
 * 
 */

package org.wandora.application.tools.r;

import javax.swing.Icon;
import javax.swing.JDialog;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */
public class OpenRConsole extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	public void execute(Wandora wandora, Context context) throws TopicMapException {
        JDialog dialog=RConsole2.getConsoleDialog();
        if(!dialog.isVisible()) dialog.setVisible(true);
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/r.png");
    }

    @Override
    public String getName() {
        return "Open R console";
    }

    @Override
    public String getDescription() {
        return "Open R console.";
    }

}
