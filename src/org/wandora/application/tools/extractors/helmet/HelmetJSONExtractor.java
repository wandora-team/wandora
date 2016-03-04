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



package org.wandora.application.tools.extractors.helmet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.IObox;

/*
 * See http://data.kirjastot.fi/
 *
 * @author akivela
 */
public class HelmetJSONExtractor extends AbstractExtractor {

    private static String defaultEncoding = "UTF-8"; //"ISO-8859-1";
    private HelmetUI ui = null;
    

    @Override
    public String getName() {
        return "HelMet Data API Extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts library data from HelMet data API at http://data.kirjastot.fi/. "+
               "HelMet is a bibliographical database of Helsinki (Finland) region libraries and contains "+
               "information about 670000 bibliographical entries.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_helmet.png");
    }

    private final String[] contentTypes=new String[] { "text/plain", "text/json", "application/json" };



    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }


    @Override
    public void execute(Wandora admin, Context context) {
        try {
            if(ui == null) {
                ui = new HelmetUI(admin);
            }

            ui.open(admin, context);

            if(ui.wasAccepted()) {
                setDefaultLogger();
                TopicMap tm = admin.getTopicMap();
                String[] urls = ui.getQueryURLs(this);
                int c = 0;
                if(urls != null && urls.length > 0) {
                    for(int i=0; i<urls.length; i++) {
                        try {
                            URL u = new URL(urls[i]);
                            log("Extracting feed '"+u.toExternalForm()+"'");
                            _extractTopicsFrom(u, tm);
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                    log("Ready.");
                }
            }
            else {
                // log("User cancelled the extraction!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(ui != null && ui.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }



    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url != null) {
            try {
                HelmetJSONParser parser = new HelmetJSONParser(topicMap, this);
                parser.clearHistory();
                parser.parse(url);
                return true;
            }
            catch (Exception ex) {
               log(ex);
            }
        }
        return false;
    }


    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String str = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(str, topicMap);
    }


    public boolean _extractTopicsFrom(String in, TopicMap topicMap) throws Exception {
        try {
            HelmetJSONParser parser = new HelmetJSONParser(topicMap, this);
            parser.clearHistory();
            parser.parse(new JSONObject(in));
        }
        catch (Exception ex) {
           log(ex);
        }
        return true;
    }



}
