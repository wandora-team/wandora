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
package org.wandora.modules;

import java.util.HashMap;

/**
 * The interface for the replacements system. The replacements system is
 * a simpler lighter weight option to proper templates. It can perform simple
 * search and replace functions, with possibly dynamic replace values. For
 * example, if you only need to add a name, or email address, or some other small
 * simple thing, in an otherwise static string, you can use the replacements
 * method instead of a full templating language. The only implementation
 * at the moment is DefaultReplacementsModule, but other implementations could
 * be added.
 * 
 * 
 * @author olli
 */
public interface ReplacementsModule extends Module {
    /**
     * Replace the values in the given string using the given context.
     * The exact way the context is used depends on the implementation. It is
     * not necessarily used so that every occurrence of the key in the context
     * is replaced with the value.
     * 
     * @param value The string in which replacements are to be done.
     * @param context A context used in the replacements.
     * @return 
     */
    public String replaceValue(String value,HashMap<String,Object> context);
}
