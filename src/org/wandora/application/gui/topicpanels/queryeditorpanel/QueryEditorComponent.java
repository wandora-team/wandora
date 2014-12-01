/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveManager;
import org.wandora.query2.DirectiveUIHints;

/**
 *
 * @author olli
 */


public class QueryEditorComponent extends javax.swing.JPanel {

    protected final ArrayList<Connector> connectors=new ArrayList<Connector>();
    
    /**
     * Creates new form QueryEditorComponent
     */
    public QueryEditorComponent() {
        initComponents();
        populateDirectiveList();
        
        this.setTransferHandler(new TransferHandler(){
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DirectiveListLine.directiveDataFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if(!canImport(support)) return false;
                
                Transferable t=support.getTransferable();
                try{
                    Object o=t.getTransferData(DirectiveListLine.directiveDataFlavor);
                    DirectiveUIHints hints=(DirectiveUIHints)o;

                    DirectivePanel panel=addDirective(hints);
                    
                    Point point=support.getDropLocation().getDropPoint();
                    panel.setBounds(point.x,point.y,panel.getWidth(),panel.getHeight());

                    return true;
                }
                catch(UnsupportedFlavorException | IOException e){return false;}
            }
            
            
        });
    }
    
    public void addConnector(Connector c){
        synchronized(connectors){
            connectors.add(c);
        }
        queryGraphPanel.repaint();
    }
    
    public void removeConnector(Connector c){
        synchronized(connectors){
            connectors.remove(c);
        }
        queryGraphPanel.repaint();
    }
    
    protected void populateDirectiveList(){
        directiveListPanel.removeAll();
        directiveListPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.anchor=GridBagConstraints.WEST;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx=1.0;
        
        DirectiveManager directiveManager=DirectiveManager.getDirectiveManager();
        List<Class<? extends Directive>> directives=directiveManager.getDirectives();
        List<DirectiveUIHints> hints=new ArrayList<>();
        
        for(Class<? extends Directive> dir : directives){
            DirectiveUIHints h=DirectiveUIHints.getDirectiveUIHints(dir);
            hints.add(h);
        }
        
        Collections.sort(hints,new Comparator<DirectiveUIHints>(){
            @Override
            public int compare(DirectiveUIHints o1, DirectiveUIHints o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        for(DirectiveUIHints h: hints){
            DirectiveListLine line=new DirectiveListLine(h);
            directiveListPanel.add(line, gbc);
            
            gbc.gridy++;
        }
    }
    
    public void addDirectivePanel(DirectivePanel panel){
        this.queryGraphPanel.add(panel);
    }
    
    public DirectivePanel addDirective(Class<? extends Directive> dir){
        return addDirective(DirectiveUIHints.getDirectiveUIHints(dir));
    }
    
    public DirectivePanel addDirective(DirectiveUIHints hints){
        DirectivePanel panel=new DirectivePanel(hints);
        addDirectivePanel(panel);
        panel.setBounds(10, 10, 200, 100);
        panel.revalidate();
        return panel;
    }
    
    public void panelMoved(DirectivePanel panel){
        Rectangle rect=panel.getBounds();
        int width=Math.max(rect.x+rect.width,this.getWidth());
        int height=Math.max(rect.y+rect.height,this.getHeight());
        if(width!=this.getWidth() || height!=this.getHeight()){
            this.setSize(width,height);
        }
        
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        directiveListPanel = new javax.swing.JPanel();
        queryScrollPane = new javax.swing.JScrollPane();
        queryGraphPanel = new GraphPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setDividerLocation(600);

        directiveListPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(directiveListPanel);

        jSplitPane1.setRightComponent(jScrollPane1);

        queryGraphPanel.setLayout(null);
        queryScrollPane.setViewportView(queryGraphPanel);

        jSplitPane1.setLeftComponent(queryScrollPane);

        add(jSplitPane1);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel directiveListPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel queryGraphPanel;
    private javax.swing.JScrollPane queryScrollPane;
    // End of variables declaration//GEN-END:variables

    private class GraphPanel extends JPanel {
        public GraphPanel(){
            super();
            this.setOpaque(false);
        }
        
        @Override
        public void paint(Graphics g) {
            g.setColor(this.getBackground());
            g.drawRect(0, 0, this.getWidth(), this.getHeight());
            synchronized(connectors){
                for(Connector c : connectors){
                    c.repaint(this);
                }
            }
            super.paint(g);
        }        
    }
}
