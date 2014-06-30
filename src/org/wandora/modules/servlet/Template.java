/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
package org.wandora.modules.servlet;

import java.util.HashMap;

/**
 * <p>
 * The interface for all templates. A template is an object which can 
 * executed with a given template context to produce content of some kind.
 * Typically the template is defined with some template language. The template
 * is read from a file and the bulk of the output in literal text with some
 * special markup for dynamic elements. The dynamic elements may then refer
 * to the variables in the template context to fill in the rest of the page.
 * </p>
 * <p>
 * The interface however is generic enough for many other types of templates too.
 * You need not read a template from an actual file. Any kind of dynamic content
 * generation based on some context parameters could work as a template.
 * </p>
 *
 * @author olli
 */

public interface Template {
    /**
     * Returns the template key. The template key is how actions refer to
     * this template.
     * @return The template key.
     */
    public String getKey();
    /**
     * Returns the template version. You may define several templates using
     * the same key with different versions. For example, you could differentiate
     * between language versions of the otherwise same template.
     * @return The version identifier.
     */
    public String getVersion();
    /**
     * Returns the mime type of the content from this template.
     * @return The mime type of the content returned by this template.
     */
    public String getMimeType();
    /**
     * Returns the character encoding used by the returned content.
     * @return The character encoding used by the content returned from this template.
     */
    public String getEncoding();
    /**
     * Processes the template with the given context and writes the resulting
     * content in the output stream.
     * @param params The template context used for processing of the template.
     * @param output The output stream where the result is written.
     */
    public void process(java.util.HashMap<String,Object> params,java.io.OutputStream output);    
    /**
     * Returns the default context for this template. You may then add more
     * content into the context before passing it on to process.
     * @return The default context of the template.
     */
    public HashMap<String,Object> getTemplateContext();
}
