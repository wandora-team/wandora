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

package org.wandora.application.tools.exporters.iiifexport;

/**
 *
 * @author olli
 */


public class LanguageString implements JsonLDOutput {
    private String content;
    private String language;
    public LanguageString(String content){
        this(content,null);
    }
    public LanguageString(String content,String language){
        this.content=content;
        this.language=language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    

    @Override
    public JsonLD toJsonLD(){
        if(language==null) return new JsonLD(content);
        else return new JsonLD().append("@value",content)
                                .append("@language",language);
    }
}
