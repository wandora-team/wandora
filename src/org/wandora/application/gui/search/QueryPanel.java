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
 *
 * 
 */




package org.wandora.application.gui.search;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraScriptManager;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.tree.TopicTreePanel;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.application.gui.simple.SimpleTextPaneResizeable;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.query2.Directive;
import org.wandora.query2.QueryContext;
import org.wandora.query2.ResultRow;
import org.wandora.query2.Static;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;
import org.wandora.utils.Tuples;

/**
 *
 * @author akivela
 */


public class QueryPanel extends javax.swing.JPanel {

    private Wandora wandora = null;
    private String SCRIPT_QUERY_OPTION_KEY = "scriptQueries";
    private ArrayList<Tuples.T3<String,String,String>> storedQueryScripts = new ArrayList<Tuples.T3<String,String,String>>();
    private MixedTopicTable resultsTable = null;
    private SimpleLabel message = null;
    
    
    
    /**
     * Creates new form QueryPanel
     */
    public QueryPanel() {
        wandora = Wandora.getWandora();
        initComponents();
        message = new SimpleLabel();
        message.setHorizontalAlignment(SimpleLabel.CENTER);
        message.setIcon(UIBox.getIcon("gui/icons/warn.png"));
        scriptTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        engineComboBox.setEditable(false);
        ArrayList<String> engines=WandoraScriptManager.getAvailableEngines();
        engineComboBox.removeAllItems();
        for(int i=0;i<engines.size();i++){
            String e=engines.get(i);
            if(e != null && e.length() > 0) {
                engineComboBox.addItem(e);
            }
        }
        queryComboBox.setEditable(false);
        clearResultsButton.setEnabled(false);
        readStoredScriptQueries();
    }

    

    
    private void readStoredScriptQueries() {
        storedQueryScripts = new ArrayList<Tuples.T3<String,String,String>>();
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                int queryCount = 0;
                String queryScript = null;
                String queryEngine = null;
                String queryName = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].name");
                while(queryName != null && queryName.length() > 0) {
                    queryScript = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].script");
                    queryEngine = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].engine");
                    storedQueryScripts.add( new Tuples.T3(queryName, queryEngine, queryScript) );
                    queryCount++;
                    queryName = options.get(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].name");
                }
                updateQueryComboBox();
            }
        }
    }


    private void writeScriptQueries() {
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                options.removeAll(SCRIPT_QUERY_OPTION_KEY);
                int queryCount = 0;
                for( Tuples.T3<String,String,String> storedQuery : storedQueryScripts ) {
                    if(storedQuery != null) {
                        options.put(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].name", storedQuery.e1);
                        options.put(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].engine", storedQuery.e2);
                        options.put(SCRIPT_QUERY_OPTION_KEY+".query["+queryCount+"].script", storedQuery.e3);
                        queryCount++;
                    }
                }
            }
        }
    }


    public void updateQueryComboBox() {
        queryComboBox.removeAllItems();
        String name = "";
        String script = "";
        String engine = "";
        for( Tuples.T3<String,String,String> storedQuery : storedQueryScripts ) {
            if(storedQuery != null) {
                name = storedQuery.e1;
                engine = storedQuery.e2;
                script = storedQuery.e3;
                queryComboBox.addItem(name);
            }
        }
        queryComboBox.setSelectedItem(name);
        engineComboBox.setSelectedItem(engine);
        scriptTextPane.setText(script);
    }


    public void addScriptQuery() {
        String queryName = WandoraOptionPane.showInputDialog(wandora, "Give name for the query script?", "", "Name of the query script");
        if(queryName != null && queryName.length() > 0) {
            String queryEngine = engineComboBox.getSelectedItem().toString();
            String queryScript = scriptTextPane.getText();
            storedQueryScripts.add( new Tuples.T3(queryName, queryEngine, queryScript) );
            writeScriptQueries();
            updateQueryComboBox();
        }
    }



    public void deleteScriptQuery() {
        int index = queryComboBox.getSelectedIndex();
        if(index < storedQueryScripts.size() && index >= 0) {
            String name = storedQueryScripts.get(index).e1;
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Would you like to remove query script '"+name+"'?", "Delete query script?");
            if(a == WandoraOptionPane.YES_OPTION) {
                storedQueryScripts.remove(index);
                writeScriptQueries();
                updateQueryComboBox();
            }
        }
    }


    public void selectScriptQuery() {
        int index = queryComboBox.getSelectedIndex();
        if(index < storedQueryScripts.size() && index >= 0) {
            Tuples.T3<String,String,String> query = storedQueryScripts.get(index);
            // queryComboBox.setSelectedIndex(index);
            engineComboBox.setSelectedItem(query.e2);
            scriptTextPane.setText(query.e3);
        }
    }
    

    public MixedTopicTable getTopicsByQuery(Iterator<Topic> contextTopics) throws ScriptException, TopicMapException, Exception {
        TopicMap tm = wandora.getTopicMap();
        WandoraScriptManager sm = new WandoraScriptManager();
        String engineName = engineComboBox.getSelectedItem().toString();
        ScriptEngine engine = sm.getScriptEngine(engineName);
        String scriptStr =  scriptTextPane.getText();
        Directive query = null;
        if(engine != null && engineName.toLowerCase().contains("nashorn")) {
            try {
                // https://bugs.openjdk.java.net/browse/JDK-8025132
                engine.eval("load('nashorn:mozilla_compat.js');");
            }
            catch(Exception e) {}
        }
        Object o=engine.eval(scriptStr);
        if(o==null) o=engine.get("query");
        if(o!=null && o instanceof Directive) {
            query = (Directive)o;
        }

        ArrayList<ResultRow> res = new ArrayList<>();
        Topic contextTopic = null;
        if(contextTopics == null) contextTopics = (new ArrayList()).iterator();
        if(!contextTopics.hasNext()){
            // if context is empty just add some (root of a tree chooser) topic
            HashMap<String,TopicTreePanel> trees=wandora.getTopicTreeManager().getTrees();
            TopicTreePanel tree=trees.values().iterator().next();
            Topic t=tm.getTopic(tree.getRootSI());
            ArrayList<Topic> al=new ArrayList<>();
            al.add(t);
            contextTopics=al.iterator();
        }
        while(contextTopics.hasNext()) {
            contextTopic = contextTopics.next();
            if(contextTopic != null && !contextTopic.isRemoved()) {
                res.add( new ResultRow(contextTopic) );
            }
        }

        QueryContext context=new QueryContext(tm, "en");

        System.out.println("Query: "+query.debugString());

        if(res.isEmpty()){}
        else if(res.size()==1){
            res=query.doQuery(context, res.get(0));
        }
        else{
            res=query.from(new Static(res)).doQuery(context, res.get(0));
        }

        ArrayList<String> columns=new ArrayList<>();
        for(ResultRow row : res){
            for(int i=0;i<row.getNumValues();i++){
                String l=row.getRole(i);
                if(!columns.contains(l)) columns.add(l);
            }
        }
        ArrayList<Object> columnTopicsA=new ArrayList<>();
        for(int i=0;i<columns.size();i++){
            String l=columns.get(i);
            if(l.startsWith("~")){
                columns.remove(i);
                i--;
            }
            else{
                Topic t=tm.getTopic(l);
                if(t!=null) columnTopicsA.add(t);
                else columnTopicsA.add(l);
            }
        }
        Object[] columnTopics=columnTopicsA.toArray(new Object[columnTopicsA.size()]);
        if(res.size() > 0) {
            Object[][] data=new Object[res.size()][columns.size()];
            for(int i=0;i<res.size();i++){
                ResultRow row=res.get(i);
                ArrayList<String> roles=row.getRoles();
                for(int j=0;j<columns.size();j++){
                    String r=columns.get(j);
                    int ind=roles.indexOf(r);
                    if(ind!=-1) data[i][j]=row.getValue(ind);
                    else data[i][j]=null;
                }
            }

            MixedTopicTable table=new MixedTopicTable(wandora);
            table.initialize(data,columnTopics);
            return table;
        }
        return null;
    }
    
    
    
    public void refresh() {
        if(resultsTable != null) {
            ((DefaultTableModel) resultsTable.getModel()).fireTableDataChanged();
        }
        resultPanel.revalidate();
        revalidate();
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

        queryPanel = new javax.swing.JPanel();
        queryPanelInner = new javax.swing.JPanel();
        selectQueryPanel = new javax.swing.JPanel();
        queryComboBox = new SimpleComboBox();
        addQueryButton = new SimpleButton();
        delQueryButton = new SimpleButton();
        scriptQueryPanel = new javax.swing.JPanel();
        engineLabel = new SimpleLabel();
        engineComboBox = new SimpleComboBox();
        scriptLabel = new SimpleLabel();
        scriptScrollPane = new javax.swing.JScrollPane();
        scriptTextPane = new QueryTextPane();
        scripButtonPanel = new javax.swing.JPanel();
        runButton = new SimpleButton();
        clearResultsButton = new SimpleButton();
        resultPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        queryPanel.setLayout(new java.awt.GridBagLayout());

        queryPanelInner.setLayout(new java.awt.GridBagLayout());

        selectQueryPanel.setLayout(new java.awt.GridBagLayout());

        queryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        queryComboBox.setPreferredSize(new java.awt.Dimension(57, 21));
        queryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        selectQueryPanel.add(queryComboBox, gridBagConstraints);

        addQueryButton.setText("Add");
        addQueryButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        addQueryButton.setPreferredSize(new java.awt.Dimension(50, 21));
        addQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addQueryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        selectQueryPanel.add(addQueryButton, gridBagConstraints);

        delQueryButton.setText("Del");
        delQueryButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        delQueryButton.setPreferredSize(new java.awt.Dimension(50, 21));
        delQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delQueryButtonActionPerformed(evt);
            }
        });
        selectQueryPanel.add(delQueryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        queryPanelInner.add(selectQueryPanel, gridBagConstraints);

        scriptQueryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        scriptQueryPanel.setLayout(new java.awt.GridBagLayout());

        engineLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        engineLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        engineLabel.setText("Engine");
        engineLabel.setPreferredSize(new java.awt.Dimension(70, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 4);
        scriptQueryPanel.add(engineLabel, gridBagConstraints);

        engineComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        engineComboBox.setPreferredSize(new java.awt.Dimension(57, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 2);
        scriptQueryPanel.add(engineComboBox, gridBagConstraints);

        scriptLabel.setFont(org.wandora.application.gui.UIConstants.tabFont);
        scriptLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        scriptLabel.setIcon(org.wandora.application.gui.UIBox.getIcon("resources/gui/icons/help_in_context.png"));
        scriptLabel.setText("Script");
        scriptLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        scriptLabel.setIconTextGap(0);
        scriptLabel.setPreferredSize(new java.awt.Dimension(70, 14));
        scriptLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                scriptLabelMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 2, 4);
        scriptQueryPanel.add(scriptLabel, gridBagConstraints);

        scriptScrollPane.setPreferredSize(new java.awt.Dimension(8, 100));
        scriptScrollPane.setViewportView(scriptTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        scriptQueryPanel.add(scriptScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        queryPanelInner.add(scriptQueryPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        queryPanel.add(queryPanelInner, gridBagConstraints);

        scripButtonPanel.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        scripButtonPanel.setMinimumSize(new java.awt.Dimension(83, 40));
        scripButtonPanel.setPreferredSize(new java.awt.Dimension(83, 40));
        scripButtonPanel.setLayout(new java.awt.GridBagLayout());

        runButton.setText("Run query");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        scripButtonPanel.add(runButton, new java.awt.GridBagConstraints());

        clearResultsButton.setText("Clear results");
        clearResultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearResultsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        scripButtonPanel.add(clearResultsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        queryPanel.add(scripButtonPanel, gridBagConstraints);

        resultPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        queryPanel.add(resultPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(queryPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void queryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryComboBoxActionPerformed
        if((evt.getModifiers() | ActionEvent.MOUSE_EVENT_MASK) != 0) {
            selectScriptQuery();
        }
    }//GEN-LAST:event_queryComboBoxActionPerformed

    private void addQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addQueryButtonActionPerformed
        if((evt.getModifiers() | ActionEvent.MOUSE_EVENT_MASK) != 0) {
            addScriptQuery();
        }
    }//GEN-LAST:event_addQueryButtonActionPerformed

    private void delQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delQueryButtonActionPerformed
        if((evt.getModifiers() | ActionEvent.MOUSE_EVENT_MASK) != 0) {
            deleteScriptQuery();
        }
    }//GEN-LAST:event_delQueryButtonActionPerformed

    private void scriptLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scriptLabelMouseReleased
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI("http://wandora.org/wiki/Query_language"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_scriptLabelMouseReleased

    
    
    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        Iterator contextObjects = (new ArrayList()).iterator();
        
        // TODO: Get global context objects and pass them into the getTopicsByQuery.
        // if(context != null) contextObjects = context.getContextObjects();
        
        try {
            resultPanel.removeAll();
            clearResultsButton.setEnabled(false);
            resultsTable = getTopicsByQuery(contextObjects);
            if(resultsTable != null) {
                resultPanel.add(resultsTable, BorderLayout.NORTH);
                clearResultsButton.setEnabled(true);
            }
            else {
                message.setText("No search results!");
                resultPanel.add(message, BorderLayout.CENTER);
            }
        }
        catch(ScriptException se) {
            message.setText("Script error!");
            resultPanel.add(message, BorderLayout.CENTER);
            revalidate();
            repaint();
            wandora.handleError(se);
        }
        catch(TopicMapException tme) {
            message.setText("Topic map exception!");
            resultPanel.add(message, BorderLayout.CENTER);
            tme.printStackTrace();
            //wandora.handleError(tme);
        }
        catch(Exception e) {
            message.setText("Error!");
            resultPanel.add(message, BorderLayout.CENTER);
            wandora.handleError(e);
        }
        revalidate();
        repaint();
    }//GEN-LAST:event_runButtonActionPerformed

    private void clearResultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearResultsButtonActionPerformed
        resultPanel.removeAll();
        resultsTable = null;
        clearResultsButton.setEnabled(false);
        revalidate();
        repaint();
    }//GEN-LAST:event_clearResultsButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addQueryButton;
    private javax.swing.JButton clearResultsButton;
    private javax.swing.JButton delQueryButton;
    private javax.swing.JComboBox engineComboBox;
    private javax.swing.JLabel engineLabel;
    private javax.swing.JComboBox queryComboBox;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JPanel queryPanelInner;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JButton runButton;
    private javax.swing.JPanel scripButtonPanel;
    private javax.swing.JLabel scriptLabel;
    private javax.swing.JPanel scriptQueryPanel;
    private javax.swing.JScrollPane scriptScrollPane;
    private javax.swing.JTextPane scriptTextPane;
    private javax.swing.JPanel selectQueryPanel;
    // End of variables declaration//GEN-END:variables



    
    
    
    
    private class QueryTextPane extends SimpleTextPaneResizeable {
    
        private int scriptQueryPanelWidth = 100;
        private int scriptQueryPanelHeight = scriptQueryPanel.getHeight();
        
        
        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            if(mousePressedInTriangle) {
                inTheTriangleZone = true;
                int yDiff = (mousePressedPoint.y - p.y);
                newSize = new Dimension(100, sizeAtPress.height - yDiff);

                JScrollPane sp = getScrollPane();

                if(scrollPane != null) {
                    sp.getViewport().setSize(newSize);
                    sp.getViewport().setPreferredSize(newSize);
                    sp.getViewport().setMinimumSize(newSize);

                    sp.setSize(newSize);
                    sp.setPreferredSize(newSize);
                    sp.setMinimumSize(newSize);
                }

                scriptQueryPanel.setSize(scriptQueryPanelWidth, scriptQueryPanelHeight - yDiff);
                scriptQueryPanel.revalidate();
                scriptQueryPanel.repaint();
            }
        }
        
        
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if(mousePressedInTriangle) {
                scriptQueryPanelHeight = scriptQueryPanel.getHeight();
            }
        }
        
    }



}
