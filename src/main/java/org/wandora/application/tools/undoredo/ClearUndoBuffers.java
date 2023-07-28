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



package org.wandora.application.tools.undoredo;

import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMapException;



/**
 *
 * @author akivela
 */


public class ClearUndoBuffers extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	@Override
    public Icon getIcon() {
        return super.getIcon();
    }

    @Override
    public String getName() {
        return "Clear undo buffers";
    }

    @Override
    public String getDescription() {
        return "Clear undo buffers";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        if(wandora != null) {
            wandora.clearUndoBuffers();
        }
    }
}
