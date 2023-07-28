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



package org.wandora.application.tools;


import org.wandora.application.*;
import org.wandora.application.contexts.*;



/**
 * An empty WandoraTool that doesn't do anything. Tool can be used as a
 * separator in tool lists, for example.
 * 
 * @author akivela
 */
public class DummyTool extends AbstractWandoraTool {


	private static final long serialVersionUID = 1L;


	@Override
    public String getName() {
        return "Dummy tool";
    }

    @Override
    public String getDescription() {
        return "This tool does nothing! It is used as a empty tool.";
    }

    @Override
    public void execute(Wandora wandora, Context context) {
        // NOTHING HERE!
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
