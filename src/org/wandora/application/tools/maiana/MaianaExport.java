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
 *
 *
 */


package org.wandora.application.tools.maiana;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import javax.swing.Icon;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;




/**
 * See: http://projects.topicmapslab.de/projects/maiana/wiki/API_Controller
 *
 * @author akivela
 */
public class MaianaExport extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;


    public MaianaExport() {
    }
    public MaianaExport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_maiana.png");
    }

    @Override
    public boolean isConfigurable(){
        return false;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        /*
        Next commented code is here for an example.
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"GML export options","GML export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should Wandora export also topic types (class-instance relations)?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Export n associations","boolean",(EXPORT_N_ASSOCIATIONS ? "true" : "false"), "Should associations with more than 2 players also export?"},
            new String[]{"Is directed","boolean",(EXPORT_DIRECTED ? "true" : "false"), "Export directed or undirected graph" },
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;

        Map<String, String> values = god.getValues();

        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_N_ASSOCIATIONS = ("true".equals(values.get("Export n associations")) ? true : false );
        EXPORT_DIRECTED = ("true".equals(values.get("Is directed")) ? true : false );
         * 
         */
    }





    @Override
    public void execute(Wandora wandora, Context context) {
       String topicMapName = null;
       String shortName = null;
       String exportInfo = null;

       SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
       String stamp = sdf.format(System.currentTimeMillis());

       SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       String fineStamp = df.format(System.currentTimeMillis());

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics";
            shortName = "wandora_export_"+stamp;
            topicMapName = "Wandora export "+fineStamp;
        }
        else {
            tm = solveContextTopicMap(wandora, context);
            topicMapName = this.solveNameForTopicMap(wandora, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "'";
                shortName = MaianaUtils.makeShortName(topicMapName);
            }
            else {
                exportInfo =  "Exporting topic map";
                shortName = "wandora_export_"+stamp;
                topicMapName = "Wandora export "+fineStamp;
            }
        }

        MaianaExportPanel maianaPanel = new MaianaExportPanel();
        maianaPanel.setTopicMapName(topicMapName);
        maianaPanel.setShortName(shortName);
        if(MaianaUtils.getApiEndPoint() != null) maianaPanel.setApiEndPoint(MaianaUtils.getApiEndPoint());
        if(MaianaUtils.getApiKey() != null) maianaPanel.setApiKey(MaianaUtils.getApiKey());
        maianaPanel.open(wandora);

        if(maianaPanel.wasAccepted()) {
            try {
                MaianaUtils.setApiKey(maianaPanel.getApiKey());
                MaianaUtils.setApiEndPoint(maianaPanel.getApiEndPoint());
                
                setDefaultLogger();
                log(exportInfo);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                tm.exportXTM(out);

                String json = MaianaUtils.getExportTemplate(maianaPanel.getApiKey(), maianaPanel.getShortName(), maianaPanel.getTopicMapName(), maianaPanel.isPublic(), maianaPanel.isDownloable(), maianaPanel.isEditable(), maianaPanel.isSchema(), out.toString());
              
                String apiEndPoint = maianaPanel.getApiEndPoint();
                MaianaUtils.checkForLocalService(apiEndPoint);

                String reply = MaianaUtils.doUrl(new URL(apiEndPoint), json, "application/json");

                //System.out.println("reply:\n"+reply);

                JSONObject replyObject = new JSONObject(reply);
                if(replyObject.has("msg")) {
                    String msg = replyObject.getString("msg");
                    log("API says '"+msg+"'");
                }
                if(replyObject.has("data")) {
                    String u = replyObject.getString("data");
                    log("You can access your topic map now with URL ");
                    log(u);
                }
            }
            catch(Exception e) {
                log(e);
            }
            log("Ok.");
            setState(WAIT);
        }
    }

    @Override
    public String getName() {
        return "Export topic map to Waiana";
    }

    @Override
    public String getDescription() {
        return "Export topic map to Waiana or Maiana API.";
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }

    // -------------------------------------------------------------------------


}
