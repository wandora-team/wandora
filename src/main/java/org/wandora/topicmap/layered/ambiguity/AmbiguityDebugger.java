/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 *
 * AmbiguityDebugger.java
 *
 * Created on 11. joulukuuta 2006, 12:15
 *
 */

package org.wandora.topicmap.layered.ambiguity;


import org.wandora.topicmap.layered.AmbiguityResolution;
import org.wandora.topicmap.layered.AmbiguityResolver;
import org.wandora.utils.logger.Log4j2Logger;

/**
 *
 * @author akivela
 */
public class AmbiguityDebugger implements AmbiguityResolver {
    private static final Log4j2Logger logger = Log4j2Logger.getLogger(AmbiguityDebugger.class);

    /** Creates a new instance of AmbiguityDebugger */
    public AmbiguityDebugger() {
    }


    @Override
    public void ambiguity(String s) {
        logger.info(s);
    }


    @Override
    public AmbiguityResolution resolveAmbiguity(String event) {
        return resolveAmbiguity(event, null);
    }


    @Override
    public AmbiguityResolution resolveAmbiguity(String event, String msg) {
        ambiguity(event + (msg == null ? "" : (" " + msg)));
        return AmbiguityResolution.addToSelected;
    }
}
