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
 * StoredQuery.java
 *
 * Created on 28. joulukuuta 2004, 15:54
 */

package org.wandora.application.tools.sqlconsole;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author  olli
 */
public class StoredQuery {
    
    private String name;
    private String description;
    private String query;
    
    /** Creates a new instance of StoredQuery */
    public StoredQuery(String name,String description,String query) {
        this.name=name;
        this.description=description;
        this.query=query;
    }
    public String getName(){return name;}
    public String getDescription(){return description;}
    public String getQuery(){return query;}
    public void setName(String name){this.name=name;}
    public void setDescription(String description){this.description=description;}
    public void setQuery(String query){this.query=query;}
    
    private static String cleanXML(String s){
        return s.replaceAll("&","&amp;").replaceAll("<","&lt;");
    }
    private static String getElementContents(Element e){
        return org.wandora.utils.XMLParamProcessor.getElementContents(e);
    }
    public String getXML(){
        return "\t\t<name>"+cleanXML(name)+"</name>\n"+
               "\t\t<description>"+cleanXML(description)+"</description>\n"+
               "\t\t<query>"+cleanXML(query)+"</query>";
    }
    
    public static StoredQuery parseXML(Element element){
        String name="";
        String description="";
        String query="";
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                String nodename=e.getNodeName();
                if(nodename.equals("name")){
                    name=getElementContents(e);
                }
                else if(nodename.equals("description")){                    
                    description=getElementContents(e);
                }
                else if(nodename.equals("query")){
                    query=getElementContents(e);                    
                }
            }
        }
        return new StoredQuery(name,description,query);
    }
    
    public static Map<String,StoredQuery> loadStoredQueries(String file) throws Exception {
        Map<String,StoredQuery> storedQueries=new TreeMap<String,StoredQuery>();
        Document doc=org.wandora.utils.XMLParamProcessor.parseDocument(file);
        NodeList nl=doc.getDocumentElement().getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("storedquery")){
                    StoredQuery sq=StoredQuery.parseXML(e);
                    storedQueries.put(sq.getName(),sq);
                }
            }
        }
        return storedQueries;
    }
    public static void saveStoredQueries(Map<String,StoredQuery> storedQueries,String file) throws IOException {
        FileOutputStream fos=new FileOutputStream(file);
        PrintWriter writer=new PrintWriter(new OutputStreamWriter(fos,"UTF-8"));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<storedqueries>");
        for(Map.Entry<String,StoredQuery> e : storedQueries.entrySet() ){
            writer.println("\t<storedquery>");
            StoredQuery sq=e.getValue();
            writer.println(sq.getXML());
            writer.println("\t</storedquery>");
        }
        writer.println("</storedqueries>");
        writer.close();
    }
}
