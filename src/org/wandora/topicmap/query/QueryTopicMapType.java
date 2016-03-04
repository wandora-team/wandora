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
package org.wandora.topicmap.query;
import java.io.*;
import java.util.*;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.utils.Options;
import org.wandora.query.*;

/**
 *
 * @author olli
 */
public class QueryTopicMapType implements TopicMapType {

    
    public QueryTopicMapType(){
    }
    
    public TopicMap createTopicMap(Object params) throws TopicMapException {
        QueryTopicMapConfiguration.QueryTopicMapParams p=(QueryTopicMapConfiguration.QueryTopicMapParams)params;
        QueryTopicMap qtm=new QueryTopicMap(p.wandora);
        qtm.setQueries(p.queryInfos);
        return qtm;
    }
    public TopicMap modifyTopicMap(TopicMap tm,Object params) throws TopicMapException {
        QueryTopicMap qtm=(QueryTopicMap)createTopicMap(params);
        qtm.addTopicMapListeners(tm.getTopicMapListeners());
        qtm.addContainerListeners(((QueryTopicMap)tm).getContainerListeners());
        qtm.setLayerStack(((QueryTopicMap)tm).getLayerStack());
        return qtm;
    }

    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options) {
        return new QueryTopicMapConfiguration(admin);
        //return new QueryConfigPanel(admin);
    }

    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora admin, Options options, TopicMap tm) {
        QueryTopicMap qtm=(QueryTopicMap)tm;
        return new QueryTopicMapConfiguration(qtm.getOriginalQueries(),admin);
    }

    public JMenuItem[] getTopicMapMenu(TopicMap tm, Wandora admin) {
        return null;
    }

    public String getTypeName() {
        return "Query";
    }
    
    @Override
    public String toString(){return getTypeName();}

    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) throws IOException, TopicMapException {
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        QueryTopicMap qtm=(QueryTopicMap)tm;
        Options options=new Options();
        Collection<QueryTopicMap.QueryInfo> queries=qtm.getOriginalQueries();

        int counter=1;
        if(queries!=null){
            for(QueryTopicMap.QueryInfo info : queries){
                options.put("query"+counter+".engine",info.engine);
                options.put("query"+counter+".type",info.type);
                options.put("query"+counter+".script",info.script);
                options.put("query"+counter+".name",info.name);

                counter++;
            }
        }
        
        out.nextEntry(pathpre+"queries.xml");
        options.save(new java.io.OutputStreamWriter(out.getOutputStream()));
        
        LayerStack ls=((QueryTopicMap)tm).getLayerStack();
        LayeredTopicMapType lttype=new LayeredTopicMapType();
        lttype.packageTopicMap(ls, out, path, logger);
        
    }

    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws IOException, TopicMapException {
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        in.gotoEntry(pathpre+"queries.xml");
        ArrayList<QueryTopicMap.QueryInfo> queries=new ArrayList<QueryTopicMap.QueryInfo>();
        Options options=new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        int counter=1;
        while(true){
            String engine=options.get("query"+counter+".engine");
            String type=options.get("query"+counter+".type");
            String script=options.get("query"+counter+".script");
            String name=options.get("query"+counter+".name");
            if(engine==null || type==null || script==null) break;
            
            queries.add(new QueryTopicMap.QueryInfo(name,type,script,engine));

            counter++;
        }
        QueryTopicMap qtm=new QueryTopicMap(wandora);
        qtm.setQueries(queries);
        
        LayeredTopicMapType lttype=new LayeredTopicMapType();
        LayerStack ls=(LayerStack)lttype.unpackageTopicMap(in, path, logger, wandora);
        qtm.setLayerStack(ls);
        
        return qtm;
    }

    public TopicMap unpackageTopicMap(TopicMap tm, PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws IOException, TopicMapException {
        return unpackageTopicMap(in,path,logger,wandora);
    }
    
    public static class QueryConfigPanel extends TopicMapConfigurationPanel {
        public Object param;
        public QueryConfigPanel(){}
        public QueryConfigPanel(Object param){super();this.param=param;}
        @Override
        public Object getParameters() {
            return param;
        }
        
    }
    
    
    @Override
    public Icon getTypeIcon(){
        return UIBox.getIcon("gui/icons/layerinfo/layer_type_query.png");
    }

}
