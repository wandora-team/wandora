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
 *
 * GenericOptionsPanel.java
 *
 * Created on 25.7.2006, 15:30
 */
package org.wandora.application.tools;

import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.utils.Textbox;

import java.util.*;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author olli
 */
public class GenericOptionsPanel extends JPanel {


	private static final long serialVersionUID = 1L;
	
	protected HashMap<String,Component> components;
    protected Wandora admin;
    protected JPanel paddingPanel;
    protected boolean padding;

    public GenericOptionsPanel(String[][] fields,Wandora wandora){
        this(fields,wandora,true);
    }
    public GenericOptionsPanel(String[][] fields,Wandora wandora,boolean padding){
        this.admin=wandora;
        this.padding=padding;
        initFields(fields);
    }

    public static GridBagConstraints makeGBC(){
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,0,5);
        gbc.fill=GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    @Override
    public void setLayout(java.awt.LayoutManager layout){
    }

    /**
     * Init fields in the options panel. Fields are given in an array of arrays.
     * First index is the field index. The inner array should have id of the field,
     * and the type of the field. Optionally it may also contain the initial value
     * of the field and a short help text. The order of the parameters is: id, type,
     * initial value and help text.
     * Type can be "string", "password", "boolean" or "topic". A suitable user interface
     * element will be used for each field depending on their type. For example
     * boolean fields will have check boxes. New types may be added in the future.
     * The id is also used as the user visible label of the field.
     */
    public void initFields(String[][] fields){
        components=new HashMap<String,Component>();
        this.removeAll();
        super.setLayout(new GridBagLayout());
        GridBagConstraints gbc=makeGBC();

        Icon icon=UIBox.getIcon("gui/icons/help2.png");

        for(int i=0;i<fields.length;i++){
            gbc.gridy=i;
            gbc.gridx=0;
            gbc.weightx=0.0;
            gbc.gridwidth=1;
            gbc.insets=new Insets(5,5,0,5);
            String id=fields[i][0];
            String type=fields[i][1];
            String value="";
            if(fields[i].length>2) value=fields[i][2];
            if(!type.equalsIgnoreCase("separator")){
                this.add(new SimpleLabel(id),gbc);
            }
            Component c=null;
            if(type.equalsIgnoreCase("string")){
                c=new SimpleField();
                ((SimpleField)c).setText(value);
            }
            else if(type.equalsIgnoreCase("password")){
                c=new SimpleField();
                ((SimpleField)c).setText(value);
            }
            else if(type.equalsIgnoreCase("boolean")){
                c=new SimpleCheckBox();
                if(value.equalsIgnoreCase("true")) ((SimpleCheckBox)c).setSelected(true);
                else ((SimpleCheckBox)c).setSelected(false);
            }
            else if(type.equalsIgnoreCase("separator")){
                c=new JSeparator();
                ((JSeparator) c).setOrientation(JSeparator.HORIZONTAL);
            }
            else if(type.equalsIgnoreCase("topic")){
                try{
                    if(value == null || value.length()==0) {
                        c=new GetTopicButton((Topic)null,admin,admin,true);
                    }
                    else {
                        c=new GetTopicButton(admin.getTopicMap().getTopic(value),admin,admin,true);
                    }
                }
                catch(TopicMapException tme){
                    admin.handleError(tme);
                    c=new SimpleLabel("Topic map exception");
                }
            }
            else if(type.toLowerCase().startsWith("combo:")) {
                String[] options=type.substring("combo:".length()).split(";");
                c=new SimpleComboBox(options);
                ((SimpleComboBox)c).setEditable(false);
                ((SimpleComboBox)c).setSelectedItem(value);
            }
            if(type.equalsIgnoreCase("separator")){
                gbc.gridx=0;
                gbc.gridwidth = 2;
                gbc.insets=new Insets(12,5,7,5);
            }
            else {
                gbc.gridx=1;
                gbc.gridwidth=1;
                gbc.insets=new Insets(5,5,0,5);
            }
            gbc.weightx=1.0;
            this.add(c,gbc);
            components.put(id,c);
            if(fields[i].length>3) {
                gbc.gridx=2;
                gbc.weightx=0.0;
                JLabel label=new JLabel(icon);
                label.setToolTipText(Textbox.makeHTMLParagraph( fields[i][3], 40 ) );
                this.add(label,gbc);
            }
        }
        if(padding){
            addPadding(fields.length);
        }
        else this.invalidate();
    }

    public void removePadding(){
        if(paddingPanel!=null){
            this.remove(paddingPanel);
        }
    }

    public void addPadding(int pos){
        paddingPanel=new JPanel();
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridy=pos;
        gbc.gridx=0;
        gbc.weighty=1.0;
        gbc.weightx=0.0;
        this.add(paddingPanel,gbc);
        this.invalidate();
    }

    /**
     * Gets the values from this options panel. Return value is a map that
     * maps field IDs to their values. Note that all values are converted to
     * string regardless of the field type. Boolean fields are converted to
     * "true" or "false".
     */
    public Map<String,String> getValues(){
        HashMap<String,String> ret=new HashMap<String,String>();
        for(Map.Entry<String,Component> e : components.entrySet()){
            String id=e.getKey();
            Component c=e.getValue();
            if(c instanceof SimpleField){
                ret.put(id,((SimpleField)c).getText());
            }
            else if(c instanceof SimpleCheckBox){
                if(((SimpleCheckBox)c).isSelected()) ret.put(id,"true");
                else ret.put(id,"false");
            }
            else if(c instanceof GetTopicButton){
                try{
                    String si=((GetTopicButton)c).getTopicSI();
                    if(si==null) ret.put(id,"");
                    else ret.put(id,si);
                }catch(TopicMapException tme){
                    admin.handleError(tme);
                    ret.put(id,"");
                }
            }
            else if(c instanceof SimpleComboBox){
                ret.put(id,((SimpleComboBox)c).getSelectedItem().toString());
            }
            else{
                ret.put(id,"");
            }
        }
        return ret;
    }

}
