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

import java.util.ArrayList;
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


public class JsoupAssociationRowTableExtractor extends AbstractJsoupExtractor 
implements WandoraTool, BrowserPluginExtractor {

	
	private static final long serialVersionUID = 1L;
	private TopicMap tm;
    
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception {
        
        this.tm = t;
        
        Elements tables = d.select("table");

        for(Element table: tables) parseTable(table);
        
        return true;
    }    

    private void parseTable(Element table) throws Exception{
        
        Elements rows = table.select("tr");
        
        Element headerRow = rows.first();
        
        ArrayList<Topic> roles = new ArrayList<Topic>();
        
        for(Element headerCell: headerRow.select("th")){
            String roleValue = headerCell.text().trim();
            if(roleValue.length() == 0) continue;
            
            Topic role = getOrCreateTopic(tm, null, roleValue);
            roles.add(role);
        }
        
        List<Element> playerRows = rows.subList(1,rows.size());
        
        for(Element playerRow: playerRows){
            try {
                handlePlayerRow(playerRow, roles);
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
    }

    private void handlePlayerRow(Element playerRow, ArrayList<Topic> roles)  throws Exception{
        
        Elements playerCells = playerRow.select("td");
        
        if(playerCells.size() != roles.size()) 
            throw new Exception("Invalid row!");
        
        Topic assocType = getOrCreateTopic(tm, null);
        Association a = tm.createAssociation(assocType);
        
        for(int i = 0; i < roles.size(); i++){
            Topic role = roles.get(i);
            
            Element playerCell = playerCells.get(i);
            String playerValue = playerCell.text();
            Topic playerTopic = getOrCreateTopic(tm, null, playerValue);
            
            a.addPlayer(playerTopic, role);
        }
    }
}
