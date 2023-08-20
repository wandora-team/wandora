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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import org.wandora.query2.DirectiveUIHints.Parameter;
import org.wandora.topicmap.Topic;

/**
 *
 * @author olli
 */


public class TopicOperandParameterPanel extends OperandParameterPanel {

	private static final long serialVersionUID = 1L;

	public TopicOperandParameterPanel(Parameter parameter,DirectivePanel panel){
        super(parameter,panel);
    }
    
    @Override
    public void setValue(Object o){
        if(o instanceof Topic){
            operandTypeComboBox.setSelectedItem("Topic");
            operandTypeChanged();
            parameterPanel.setValue(o);
        }
        else if(o instanceof String){
            operandTypeComboBox.setSelectedItem("Subject Identifier");
            operandTypeChanged();
            parameterPanel.setValue(o);            
        }
        else super.setValue(o);
    }    
    @Override
    protected void setOperandTypes() {
        super.setOperandTypes();
        operandTypeComboBox.removeItem("String");
        operandTypeComboBox.addItem("Subject Identifier");
        operandTypeComboBox.addItem("Topic");
    }
    
    @Override
    protected boolean operandTypeChanged(){
        if(super.operandTypeChanged()) return true;
        Object o=operandTypeComboBox.getSelectedItem();
        if(o==null) return false; // shouldn't happen as super takes care of this
        String type=o.toString();
        if(type.equalsIgnoreCase("Topic")){
            operandPanel.removeAll();
            TopicParameterPanel p=new TopicParameterPanel(parameter,this.directivePanel);
            p.setLabel("");
            p.setOrderingHint(this.orderingHint);
            operandPanel.add(p);
            this.revalidate();
            operandPanel.repaint();
            parameterPanel=p;
            return true;
        }
        else if(type.equalsIgnoreCase("Subject Identifier")){
            operandPanel.removeAll();
            StringParameterPanel p=new StringParameterPanel(parameter,this.directivePanel);
            p.setLabel("");
            p.setOrderingHint(this.orderingHint);
            operandPanel.add(p);
            this.revalidate();
            operandPanel.repaint();
            parameterPanel=p;
            return true;
        }
        else return false;
    }
}
