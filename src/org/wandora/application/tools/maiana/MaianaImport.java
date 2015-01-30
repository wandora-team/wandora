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
 *
 *
 */


package org.wandora.application.tools.maiana;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.swing.Icon;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.application.tools.importers.AbstractImportTool;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */
public class MaianaImport extends AbstractWandoraTool implements WandoraTool {



    public MaianaImport() {
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_maiana.png");
    }

    @Override
    public boolean isConfigurable(){
        return false;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        /*
        
        THESE ARE HERE FOR A REFERENCE. HOW TO SET UP CONFIGURABLE OPTIONS.
        COPIED FROM GML EXPORT.
        
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
        String topicMapName = "";
        String topicMapOwner = "";
        
        MaianaImportPanel maianaPanel = new MaianaImportPanel();
        maianaPanel.setName(topicMapName);
        maianaPanel.setOwner(topicMapOwner);
        if(MaianaUtils.getApiKey() != null) maianaPanel.setApiKey(MaianaUtils.getApiKey());
        if(MaianaUtils.getApiEndPoint() != null) maianaPanel.setApiEndPoint(MaianaUtils.getApiEndPoint());

        maianaPanel.open(wandora);

        if(maianaPanel.wasAccepted()) {
            try {
                MaianaUtils.setApiKey(maianaPanel.getApiKey());
                MaianaUtils.setApiEndPoint(maianaPanel.getApiEndPoint());

                setDefaultLogger();

                String[] shortNames = maianaPanel.getTopicMapShortNames();
                String[] names = maianaPanel.getTopicMapNames();
                String[] owners = maianaPanel.getOwners();
                String format = maianaPanel.getFormat();
                String apikey = MaianaUtils.getApiKey();

                if(shortNames.length > 0) {
                    if(shortNames.length > 1) {
                        log("Importing " + shortNames.length + " topic maps...");
                    }
                }
                else {
                    log("You didn't specify which topic maps should be imported. Aborting.");
                }
                
                for(int i=0; i<shortNames.length; i++) {
                    String sn = shortNames[i];
                    String o = "";
                    try { o = owners[i]; } catch(Exception e) {}
                    String n = "";
                    try { n = names[i]; } catch(Exception e) { n = sn; }

                    log("Importing topic map '"+n+"'...");

                    String request = MaianaUtils.getImportTemplate(apikey, sn, o, format);
                    String apiEndPoint = maianaPanel.getApiEndPoint();
                    MaianaUtils.checkForLocalService(apiEndPoint);
                        
                    String reply = MaianaUtils.doUrl(new URL(apiEndPoint), request, "application/json");

                    //System.out.println("reply:\n"+reply);

                    JSONObject replyObject = new JSONObject(reply);

                    if(replyObject.has("code")) {
                        String code = replyObject.getString("code");
                        if(!"0".equals(code)) {
                            log("An error occurred while requesting a topic map '"+n+"' from user '"+o+"'.");
                        }
                    }
                    if(replyObject.has("msg")) {
                        String msg = replyObject.getString("msg");
                        log(msg);
                    }
                    if(replyObject.has("data")) {
                        String serializedTopicMap = replyObject.getString("data");
                        //log("Downloaded topic map is "+serializedTopicMap);

                        log("Parsing topic map '"+n+"'...");

                        ByteArrayInputStream topicMapStream = new ByteArrayInputStream(serializedTopicMap.getBytes("UTF-8"));
                        TopicMap map = new org.wandora.topicmap.memory.TopicMapImpl();
                        map.importXTM(topicMapStream, getCurrentLogger());

                        LayerStack layerStack = wandora.getTopicMap();
                        String layerName = n;
                        int c = 2;
                        if(layerStack.getLayer(layerName) != null) {
                            String originalLayerName = layerName;
                            do {
                                layerName = originalLayerName + " " + c;
                                c++;
                            }
                            while(layerStack.getLayer(layerName) != null);
                        }
                        log("Creating new layer '" + layerName + "' for the topic map.");
                        layerStack.addLayer(new Layer(map,layerName,layerStack));
                        wandora.layerTree.resetLayers();
                    }
                    else {
                        log("Invalid topic map serialization for '"+n+"'.");
                    }
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
        return "Import topic map from Waiana";
    }

    @Override
    public String getDescription() {
        return "Import topic map from Waiana or Maiana API.";
    }



    // -------------------------------------------------------------------------



}
