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
 * QueryProcessor.java
 *
 * Created on 28. joulukuuta 2004, 15:35
 */

package org.wandora.application.tools.sqlconsole;

import java.util.regex.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
/**
 *
 * @author  olli
 */
public class QueryProcessor {
    
    /** Creates a new instance of QueryProcessor */
    public QueryProcessor() {
    }
    
    public static String[] parseParemeterFields(String query){
        System.out.println("Finding params from "+query);
        Vector params=new Vector();
        Pattern pattern=Pattern.compile("(^|[^\\{])((\\{\\{)*)\\{([^\\{\\}]+)\\}");
        Matcher matcher=pattern.matcher(query);
        int ptr=0;
        while(matcher.find(ptr)){
            String param=matcher.group(4);
            if(!params.contains(param)) params.add(param);
            ptr=matcher.start()+1;
        }
        System.out.println("Found parameters "+params);
        return (String[])params.toArray(new String[0]);
    }
    public static String replaceParams(String query,String[] params){
        if(params!=null) {
            for(int i=0;i+1<params.length;i+=2){
                String rep=params[i+1].replaceAll("\\\\","\\\\\\\\");
                rep=rep.replaceAll("\\$","\\\\\\$");
                Pattern pattern=Pattern.compile("(^|[^\\{])((\\{\\{)*)\\{"+params[i]+"\\}");
                Matcher matcher=pattern.matcher(query);
                query=matcher.replaceAll("$1$2"+rep);
            }
        }
        query=query.replaceAll("\\{\\{","{");
        return query;
    }
    public static String replaceParams(String query,Map<String,JTextComponent> params){
        String[] p=new String[params.size()*2];
        int ptr=0;
        for(Map.Entry<String,JTextComponent> e : params.entrySet()){
            p[ptr++]=e.getKey();
            p[ptr++]=e.getValue().getText();
        }
        return replaceParams(query,p);
    }
    public static HashMap<String,JTextComponent> fillQueryFields(String[] params,Container labelContainer,Container fieldContainer){
        HashMap<String,JTextComponent> fieldMap=new HashMap<String,JTextComponent>();
        labelContainer.removeAll();
        fieldContainer.removeAll();
//        labelContainer.setLayout(new GridBagLayout());
//        fieldContainer.setLayout(new GridBagLayout());
        if(params.length>0) {
            labelContainer.setLayout(new GridLayout(params.length,0,0,3));
            fieldContainer.setLayout(new GridLayout(params.length,0,0,3));
            for(int i=0;i<params.length;i++){
                JLabel label=new JLabel(params[i]);
                JTextField field=new JTextField();//new JTextField();
    /*            GridBagConstraints gbc=new GridBagConstraints();
                gbc.gridx=0;
                gbc.gridy=i;
                gbc.weightx=1.0;
                gbc.fill=gbc.HORIZONTAL;
                gbc.insets.bottom=10;
                labelContainer.add(label,gbc);*/
                label.setVerticalAlignment(SwingConstants.TOP);
                labelContainer.add(label);
    /*            gbc=new GridBagConstraints();
                gbc.gridx=0;
                gbc.gridy=i;
                gbc.weightx=1.0;
                gbc.fill=gbc.HORIZONTAL;
                gbc.insets.bottom=10;
                fieldContainer.add(field,gbc);*/
                fieldContainer.add(field);
                fieldMap.put(params[i],field);
            }
        }
        labelContainer.validate();
        labelContainer.repaint();
        fieldContainer.validate();
        fieldContainer.repaint();
        return fieldMap;
    }
}
