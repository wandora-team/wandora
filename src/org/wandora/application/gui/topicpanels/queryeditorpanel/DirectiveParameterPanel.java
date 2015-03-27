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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.wandora.application.gui.topicpanels.queryeditorpanel.ConnectorAnchor.Direction;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;
import org.wandora.query2.DirectiveUIHints.Parameter;

/**
 *
 * @author olli
 */


public class DirectiveParameterPanel extends AbstractTypePanel {

    protected ConnectorAnchor connectorAnchor;
    protected Class<? extends Directive> directiveType;
    
    protected ConnectorAnchor fromAnchor;
    protected DirectivePanel fromPanel;
    
    /**
     * Creates new form DirectiveParameterPanel
     */
    public DirectiveParameterPanel(Parameter parameter,DirectivePanel panel) {
        super(parameter, panel);
        initComponents();
        disconnectButton.setVisible(false);
        
        connectorAnchor=new ComponentConnectorAnchor(directiveAnchor,Direction.RIGHT,true,false);
        
        DnDTools.addDropTargetHandler(directiveAnchor, DnDTools.directiveHintsDataFlavor, 
                new DnDTools.DropTargetCallback<DirectiveUIHints>() {

            @Override
            public boolean callback(JComponent component, DirectiveUIHints hints, TransferHandler.TransferSupport support) {
                if(directiveType!=null && !directiveType.isAssignableFrom(hints.getClass())) return false;
                
                DirectivePanel directivePanel=getDirectivePanel();
                QueryEditorComponent editor=directivePanel.getEditor();

                DirectivePanel panel=editor.addDirective(hints);
                Point point=support.getDropLocation().getDropPoint();
                Rectangle rect=directivePanel.getBounds();
                panel.setBounds(rect.x+rect.width+10,rect.y,panel.getWidth(),panel.getHeight());
                
                connectValue(panel);
                
                return true;                
            }
        });
        
        DnDTools.addDropTargetHandler(directiveAnchor, DnDTools.directivePanelDataFlavor, 
                new DnDTools.DropTargetCallback<DirectivePanel>() {

            @Override
            public boolean callback(JComponent component, DirectivePanel o, TransferHandler.TransferSupport support) {
                DirectivePanel directivePanel=getDirectivePanel();
                if(o==directivePanel) return false;
                connectValue(o);
                return true;
            }
        });        
    }
    
    @Override
    public void disconnect() {
        super.disconnect(); 
        connectValue(null);
    }

    public void connectValue(DirectivePanel p){
        if(p==null) {
            if(fromAnchor!=null) fromAnchor.setTo(null);
            disconnectButton.setVisible(false);
            directiveAnchor.setVisible(true);
            
            fromPanel=null;
            fromAnchor=null;
            
            return;
        }
        
        fromAnchor=p.getFromConnectorAnchor();
        fromPanel=p;
        
        getDirectivePanel().connectParamAnchor(fromAnchor,this.orderingHint);
        
        disconnectButton.setVisible(true);
        directiveAnchor.setVisible(false);        
    }

    
    public void setDirectiveType(Class<? extends Directive> cls){
        directiveType=cls;
    }

    @Override
    public void setLabel(String label){
        parameterLabel.setText(label);
    }
    
    @Override
    public void setValue(Object o){
        if(o==null || !(o instanceof DirectivePanel)) {
            fromAnchor=null;
            fromPanel=null;
            disconnectButton.setVisible(false);
            directiveAnchor.setVisible(true);
            return;
        }
        
        DirectivePanel p=(DirectivePanel)o;
        fromPanel=p;
        fromAnchor=p.getFromConnectorAnchor();
        
        disconnectButton.setVisible(true);
        directiveAnchor.setVisible(false);        
        
    }
    
    
    @Override
    public Object getValue(){
        return fromPanel;
    }
    @Override
    public String getValueScript(){
        if(fromPanel==null) return null;
        return fromPanel.buildScript();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        parameterLabel = new javax.swing.JLabel();
        directiveAnchor = new javax.swing.JLabel();
        disconnectButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        parameterLabel.setText("Label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(parameterLabel, gridBagConstraints);

        directiveAnchor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        directiveAnchor.setText("Drag directive here");
        directiveAnchor.setMaximumSize(new java.awt.Dimension(200, 20));
        directiveAnchor.setMinimumSize(new java.awt.Dimension(20, 20));
        directiveAnchor.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(directiveAnchor, gridBagConstraints);

        disconnectButton.setText("Disconnect directive");
        disconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        add(disconnectButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectButtonActionPerformed
        connectValue(null);
    }//GEN-LAST:event_disconnectButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel directiveAnchor;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JLabel parameterLabel;
    // End of variables declaration//GEN-END:variables
}
