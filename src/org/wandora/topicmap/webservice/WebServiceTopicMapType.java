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
 */
package org.wandora.topicmap.webservice;
import java.io.*;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.*;
import org.wandora.utils.Options;

/**
 *
 * @author olli
 */
public class WebServiceTopicMapType implements TopicMapType {

    public TopicMap createTopicMap(Object params) throws TopicMapException {
        WebServiceTopicMap wstm=new WebServiceTopicMap();
        wstm.setWebService((String)params);
        return wstm;
    }

    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options) {
        return new WebServiceConfiguration(admin);
    }

    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora admin, Options options, TopicMap tm) {
        WebServiceConfiguration wsc=new WebServiceConfiguration(admin);
        String endpoint=((WebServiceTopicMap)tm).getWebService()._getServiceClient().getTargetEPR().getAddress();
        wsc.setEndPoint(endpoint);
        return wsc;
    }

    public JMenuItem[] getTopicMapMenu(TopicMap tm, Wandora admin) {
        return null;
    }

    public Icon getTypeIcon() {
        return UIBox.getIcon("gui/icons/layerinfo/layer_type_webservice.png");
    }

    public String getTypeName() {
        return "Web service";
    }

    public TopicMap modifyTopicMap(TopicMap tm, Object params) throws TopicMapException {
        ((WebServiceTopicMap)tm).setWebService((String)params);
        return tm;
    }

    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) throws IOException, TopicMapException {
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        WebServiceTopicMap wstm=(WebServiceTopicMap)tm;
        Options options=new Options();
        String endpoint=wstm.getWebService()._getServiceClient().getTargetEPR().getAddress();
        options.put("endpoint",endpoint);
        out.nextEntry(pathpre+"wsoptions.xml");
        options.save(new java.io.OutputStreamWriter(out.getOutputStream()));
    }

    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger, Wandora wandora) throws IOException, TopicMapException {
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        in.gotoEntry(pathpre+"wsoptions.xml");
        Options options=new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        String endpoint=options.get("endpoint");
        if(endpoint!=null){
            return new WebServiceTopicMap(endpoint);
        }
        else{
            return new WebServiceTopicMap();
        }
    }

    public TopicMap unpackageTopicMap(TopicMap tm, PackageInput in, String path, TopicMapLogger logger, Wandora wandora) throws IOException, TopicMapException {
        return unpackageTopicMap(in, path, logger, wandora);
    }

    @Override
    public String toString(){return getTypeName();}
}
