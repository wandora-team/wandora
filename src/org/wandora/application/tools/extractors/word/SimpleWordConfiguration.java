/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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
package org.wandora.application.tools.extractors.word;

import java.util.HashMap;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
class SimpleWordConfiguration {

    private HashMap<Bools, Boolean> booleanConfig;

    protected enum Bools {
        REGEX,
        CASE_SENSITIVE,
        MATCH_WORDS,
        BASE_NAME,
        VARIANT_NAME,
        INSTANCE_DATA
    }

    public SimpleWordConfiguration() {

        booleanConfig = new HashMap<>();
        booleanConfig.put(Bools.REGEX, false);
        booleanConfig.put(Bools.CASE_SENSITIVE, false);
        booleanConfig.put(Bools.MATCH_WORDS, false);
        booleanConfig.put(Bools.BASE_NAME, true);
        booleanConfig.put(Bools.VARIANT_NAME, true);
        booleanConfig.put(Bools.INSTANCE_DATA, true);

    }

    protected boolean bool(Bools b) {
        return booleanConfig.get(b);
    }

    protected boolean bool(Bools b, boolean v) {
        return booleanConfig.put(b, v);
    }
}
