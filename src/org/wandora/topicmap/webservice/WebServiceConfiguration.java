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
 *
 * MemoryConfiguration.java
 *
 * Created on 24. marraskuuta 2005, 13:42
 */

package org.wandora.topicmap.webservice;



import org.wandora.topicmap.memory.*;
import javax.swing.*;
import javax.swing.event.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;
import java.util.*;
import java.util.regex.*;
import static org.wandora.utils.Tuples.*;
/**
 *
 * @author  olli
 */
public class WebServiceConfiguration extends TopicMapConfigurationPanel {
    public static final String LOAD_MINI_SCHEMA_PARAM = "LOAD_MINI_SCHEMA_PARAM";
    
    Wandora admin = null;
    
    private boolean documentChanging;
    
    /** Creates new form MemoryConfiguration */
    public WebServiceConfiguration(Wandora admin) {
        initComponents();
        endpointTextField.getDocument().addDocumentListener(new DocumentListener(){
            public void changedUpdate(DocumentEvent e) {endpointChanged();}
            public void insertUpdate(DocumentEvent e) {endpointChanged();}
            public void removeUpdate(DocumentEvent e) {endpointChanged();}
        });
        hostTextField.getDocument().addDocumentListener(new DocumentListener(){
            public void changedUpdate(DocumentEvent e) {hostChanged();}
            public void insertUpdate(DocumentEvent e) {hostChanged();}
            public void removeUpdate(DocumentEvent e) {hostChanged();}
        });
        portTextField.getDocument().addDocumentListener(new DocumentListener(){
            public void changedUpdate(DocumentEvent e) {portChanged();}
            public void insertUpdate(DocumentEvent e) {portChanged();}
            public void removeUpdate(DocumentEvent e) {portChanged();}
        });
        documentChanging=false;
    }
    public WebServiceConfiguration(Wandora admin,String endpoint) {
        this(admin);
        setEndPoint(endpoint);
    }

    private static final Pattern endpointPattern=Pattern.compile("([a-zA-Z0-9-]*:(?:/){0,2})([^:/]*)(?::([0-9]+))?(.*)");
    private T4<String,String,String,String> parseEndPoint(String endpoint){
        Matcher m=endpointPattern.matcher(endpoint);
        if(m.matches()){
            String pre=m.group(1);
            String host=m.group(2);
            String port=m.group(3);
            if(port==null) port="";
            String after=m.group(4);
            return t4(pre,host,port,after);
        }
        else return null;
    }

    private void endpointChanged(){
        if(!documentChanging){
            documentChanging=true;
            try{
                T4<String,String,String,String> parsed=parseEndPoint(endpointTextField.getText());
                if(parsed!=null){
                    hostTextField.setText(parsed.e2);
                    portTextField.setText(parsed.e3);
                }
            }
            finally{documentChanging=false;}
        }
    }
    private void hostChanged(){
        if(!documentChanging){
            documentChanging=true;
            try{
                T4<String,String,String,String> parsed=parseEndPoint(endpointTextField.getText());
                if(parsed!=null){
                    endpointTextField.setText(parsed.e1+hostTextField.getText()+
                            (parsed.e3.length()>0?":"+parsed.e3:"")+parsed.e4);
                    endpointTextField.setCaretPosition(0);
                }
            }
            finally{documentChanging=false;}
        }
    }
    private void portChanged(){
        if(!documentChanging){
            documentChanging=true;
            try{
                T4<String,String,String,String> parsed=parseEndPoint(endpointTextField.getText());
                if(parsed!=null){
                    String port=portTextField.getText().trim();
                    endpointTextField.setText(parsed.e1+parsed.e2+
                            (port.length()>0?":"+port:"")+parsed.e4);
                    endpointTextField.setCaretPosition(0);
                }
            }
            finally{documentChanging=false;}
        }
    }

    @Override
    public Object getParameters() {
        return getEndPoint();
    }

    public void setEndPoint(String endpoint){
        endpointTextField.setText(endpoint);
        endpointTextField.setCaretPosition(0);
    }

    public String getEndPoint(){
        return endpointTextField.getText().trim();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new SimpleLabel();
        endpointTextField = new SimpleField();
        jLabel2 = new SimpleLabel();
        portTextField = new SimpleField();
        jLabel3 = new SimpleLabel();
        hostTextField = new SimpleField();
        jLabel4 = new SimpleLabel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("End point");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabel1, gridBagConstraints);

        endpointTextField.setText("http://localhost:8898/axis/services/TopicMapService.TopicMapServiceHttpSoap12Endpoint/");
        endpointTextField.setPreferredSize(new java.awt.Dimension(6, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(endpointTextField, gridBagConstraints);

        jLabel2.setText("Host");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabel2, gridBagConstraints);

        portTextField.setText("8898");
        portTextField.setPreferredSize(new java.awt.Dimension(6, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(portTextField, gridBagConstraints);

        jLabel3.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabel3, gridBagConstraints);

        hostTextField.setText("localhost");
        hostTextField.setPreferredSize(new java.awt.Dimension(6, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(hostTextField, gridBagConstraints);

        jLabel4.setText("<html>Consider Web Service Topic Map Layer as an experimental feature meant as a demonstration of peer-to-peer topic mapping. Many Wandora features such as search has no Web Service Topic Map implementation yet, and causes Java exceptions.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField endpointTextField;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField portTextField;
    // End of variables declaration//GEN-END:variables
    
}
