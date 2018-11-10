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
 *
 */




package org.wandora.application.tools.extractors.yahoo.yql;

import java.io.InputStream;
import java.net.URLEncoder;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */
public abstract class AbstractYQLExtractor extends AbstractExtractor {


	private static final long serialVersionUID = 1L;

	@Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_yahoo.png");
    }

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }

    private final String[] contentTypes=new String[] { "text/plain", "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }



    // -------------------------------------------------------------------------

    public static final String YQL_BASE = "http://query.yahooapis.com/v1/public/yql";


    public String makeYQLURLRequest(String query, String appid) {
        String request = YQL_BASE;
        try {
            request += "?q=" + URLEncoder.encode(query, "utf-8");
        }
        catch(Exception e) {
            request += "?q=" + query;
        }
        request += "&diagnostics=true";
        return request;
    }



    // -------------------------------------------------------------------------



    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        YQLExtractorConfiguration dialog=new YQLExtractorConfiguration(admin,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }


    // -------------------------------------------------------------------------




    public abstract boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception;



    // -------------------------------------------------------------------------




    private static String appid = "o8xD0DzV34G_76ER9eoNDhEgq5EMofWDE6k6JE3_pQyS0aS0JutM91shEbDivKUFSA--";
    public String solveAppId(Wandora wandora) {
        setWandora(wandora);
        return solveAppId();
    }
    public String solveAppId() {
        if(appid == null) {
            appid = "";
            appid = WandoraOptionPane.showInputDialog(getWandora(), "Please give valid AppID for Yahoo! Term Extraction web service. You can register your apikey at http://developer.yahoo.com/search/content/V1/termExtraction.html", appid, "AppID", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return appid;
    }




    public void forgetAuthorization() {
        appid = null;
    }






}
