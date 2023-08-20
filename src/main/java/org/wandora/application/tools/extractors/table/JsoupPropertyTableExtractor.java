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
 */

package org.wandora.application.tools.extractors.table;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.application.tools.extractors.AbstractJsoupExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author Eero
 */


public class JsoupPropertyTableExtractor extends AbstractJsoupExtractor 
implements WandoraTool, BrowserPluginExtractor {

	private static final long serialVersionUID = 1L;

	private TopicMap tm;
    private Topic wandoraClass;
    
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception {
        
        this.tm = t;
        this.wandoraClass = getWandoraClassTopic(tm);
        
        Elements tables = d.select("table");
        if(tables.isEmpty()) throw new Exception("No table found!");
        
        
        return parseTable(tables.first());
    }    

    private boolean parseTable(Element table) throws Exception{
        
        Elements rows = table.select("tr");
        
        Element masterRow = rows.first();
        Element masterCell = masterRow.select("td").first();
        
        if(masterCell == null) throw new Exception("No master row!");
        
        String masterValue = masterCell.text();
        
        Topic masterTopic = getOrCreateTopic(tm, null, masterValue);
        Association assoc = tm.createAssociation(masterTopic);
        
        List<Element> playerRows = rows.subList(1, rows.size());
        
        for(Element playerRow: playerRows) {
            try {
                handleAssoc(assoc, playerRow);
            } catch (Exception e) {
                log(e);
            }
        }
        
        return true;
        
    }

    private void handleAssoc(Association assoc, Element playerRow) throws Exception {
        
        Elements playerCells = playerRow.select("td");
        if(playerCells.size() != 2) throw new Exception("Invalid player row");
        
        String roleValue = playerCells.first().text();
        String playerValue = playerCells.last().text();
        
        Topic roleTopic = getOrCreateTopic(tm, null, roleValue);
        Topic playerTopic = getOrCreateTopic(tm, null, playerValue);
        
        assoc.addPlayer(playerTopic, roleTopic);
        
    }
}
