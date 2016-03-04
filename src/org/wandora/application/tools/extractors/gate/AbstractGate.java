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



package org.wandora.application.tools.extractors.gate;

import org.wandora.application.tools.extractors.AbstractExtractor;

/**
 *
 * @author akivela
 */
public abstract class AbstractGate extends AbstractExtractor {

    public static final String GATE_HOME = "lib/gate";

    public static final String GATE_PLUGIN_HOME = GATE_HOME+"/plugins";
    public static final String CONFIG_FILE = GATE_HOME+"/gate.xml";
}
