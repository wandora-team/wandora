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


package org.wandora.application.tools.occurrences.run;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.OccurrenceTable;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.application.gui.table.TableViewerPanel;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.query2.QueryRunner;
import org.wandora.topicmap.Topic;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */


public class RunOccurrenceAsQuery extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	public RunOccurrenceAsQuery() {}
    public RunOccurrenceAsQuery(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "Run Wandora Query script in occurrence";
    }

    @Override
    public String getDescription() {
        return "Run Wandora Query script stored in occurrence.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        Object contextSource = context.getContextSource();
        
        // ***** OCCURRENCE TABLE ***** 
        if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) contextSource;
            String occurrence = ot.getPointedOccurrence();
            if(DataURL.isDataURL(occurrence)) {
                DataURL dataURL = null;
                try {
                    dataURL = new DataURL(occurrence);
                } 
                catch (MalformedURLException ex) {
                    log(ex);
                }
                if(dataURL != null) {
                    occurrence = new String(dataURL.getData());
                }
            }
            System.out.println("Running Wandora Query script:\n"+occurrence);
            try {
                QueryRunner queryRunner = new QueryRunner();
                Collection<Topic> contextTopics = new ArrayList<>();
                contextTopics.add(ot.getTopic());
                
                QueryRunner.QueryResult result = new QueryRunner.QueryResult(queryRunner.runQuery(occurrence, contextTopics));

                Object[][] data = result.getData();
                Object[] columns = result.getColumns();

                MixedTopicTable table = new MixedTopicTable(wandora);
                table.initialize(data,columns);
                String title = "Query result";
                        
                TableViewerPanel viewer = new TableViewerPanel();
                viewer.openInDialog(table, title);
            }
            catch(Exception e) {
                wandora.handleError(e);
                e.printStackTrace();
            }
        }
    }
    
}
