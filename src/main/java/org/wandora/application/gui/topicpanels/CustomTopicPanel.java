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
 * CustomTopicPanel.java
 *
 * Created on 7. tammikuuta 2008, 10:00
 */

package org.wandora.application.gui.topicpanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.ArrayList;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraMenuManager;
import org.wandora.application.WandoraScriptManager;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewWrapper;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimplePanel;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.application.gui.topicpanels.custompanel.CustomTopicPanelConfiguration;
import org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.query2.Directive;
import org.wandora.query2.QueryContext;
import org.wandora.query2.QueryException;
import org.wandora.query2.ResultRow;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;



/**
 *
 * @author  olli
 */
public class CustomTopicPanel extends AbstractTraditionalTopicPanel implements ActionListener, TopicPanel {
    public static boolean USE_GLOBAL_OPTIONS = true;
    
    private boolean viewSubjectLocatorResources = false;
    
    protected Topic topic;
    protected String topicSI;
    
    protected Wandora wandora;
    protected String originalBN;
    protected String originalSL;
    protected Options options;
    protected Options globalOptions;
    
    protected SimplePanel customPanel;
    
    protected ArrayList<QueryGroupInfo> queryGroups;

    private String OPTIONS_PREFIX = "gui.customTopicPanel.";
    private String OPTIONS_VIEW_PREFIX = OPTIONS_PREFIX + "view.";
    
    
    
    
    /** Creates new form CustomTopicPanel */
    public CustomTopicPanel() {
    }

    
    @Override
    public void init() {
    }
    
    
    @Override
    public Topic getTopic() throws TopicMapException {
        return topic;
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_custom.png");
    }

    @Override
    public String getName() {
        return "Custom";
    }
    
    @Override
    public String getTitle() {
        if(topic != null) return TopicToString.toString(topic);
        else return "";
    }
    
    @Override
    public int getOrder() {
        return 20;
    }
    
    @Override
    public JPanel getGui() {
        return this;
    }

    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        return false;
    }

    public JMenu getViewMenu(JMenu baseMenu) {
        UIBox.attachMenu(baseMenu, new Object[] { "---" }, this);
        UIBox.attachMenu(baseMenu, getViewMenuStruct(), this);
        return baseMenu;
    }
    
    
    
    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }
    
    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }
    
    
    
    @Override
    public Object[] getViewMenuStruct() {
        Icon viewIcon = UIBox.getIcon("gui/icons/view2.png");
        Icon hideIcon = UIBox.getIcon("gui/icons/view2_no.png");
        Icon configureIcon = UIBox.getIcon("gui/icons/topic_panel_custom_configure.png");

        ArrayList menuVector = new ArrayList();
        for(int i=0;i<queryGroups.size();i++){
            QueryGroupInfo groupInfo=queryGroups.get(i);
            menuVector.add(groupInfo.name);
            menuVector.add( options.isFalse(getGroupOptionsKey(groupInfo.name)) ? hideIcon : viewIcon );
            menuVector.add( this );
        }

        menuVector.add( "---" );
        menuVector.add("View subject locator resources");
        menuVector.add( viewSubjectLocatorResources ? viewIcon : hideIcon );
        menuVector.add( this );
        
        menuVector.add( "---" );
        menuVector.add( "Configure..." );
        menuVector.add( configureIcon );
        menuVector.add( this );
        
        return menuVector.toArray();
    }
    
    
    
    @Override
    public void open(Topic topic) throws TopicMapException {
        try {
            this.topic = topic;
            this.topicSI = topic.getOneSubjectIdentifier().toExternalForm();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        this.wandora = Wandora.getWandora();
        if(globalOptions == null) {
            globalOptions = wandora.getOptions();
        }
        if(options == null) {
            if(USE_GLOBAL_OPTIONS) {
                options = globalOptions;
            }
            else {
                options = new Options(globalOptions);
            }
        }
        
        this.removeAll();
        this.setTransferHandler(new TopicPanelTransferHandler());
        initComponents();
        refresh();
    }
    
    
    
    public String getGroupOptionsKey(String name){
        StringBuilder sb=new StringBuilder(name);
        for(int i=0;i<sb.length();i++){
            char c=sb.charAt(i);
            if( (c>='A' && c<='Z') || (c>='a' && c<='z') || (i>0 && c>='0' && c<='9') || c=='_'){}
            else {
                sb.setCharAt(i,'_');
            }
        }
        return OPTIONS_VIEW_PREFIX+sb.toString();
    }

    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command == null) return;
            
        if(command.startsWith("Configure")){
            showConfigureDialog();            
        }
        else if(command.equals("View subject locator resources")) {
            viewSubjectLocatorResources = !viewSubjectLocatorResources;
            refresh();
            wandora.topicPanelsChanged();
        }
        else{
            boolean found=false;
            for(QueryGroupInfo queryGroup : queryGroups) {
                if(queryGroup.name.equals(command)) {
                    found=true;
                }
            }
            if(found){
                boolean old=options.isFalse(getGroupOptionsKey(command));
                options.put(getGroupOptionsKey(command), old ? "true" : "false");
                refresh();
                wandora.topicPanelsChanged();
            }
        }
    }
    
    
    
    
    public void parseOptions(){
        queryGroups=new ArrayList<QueryGroupInfo>();
        
        for(int i=0;true;i++){
            String groupName=options.get(OPTIONS_PREFIX+"group["+i+"].name");
            if(groupName==null) break;
            QueryGroupInfo group=new QueryGroupInfo();
            group.name=groupName;
            
            for(int j=0;true;j++){
                String queryName=options.get(OPTIONS_PREFIX+"group["+i+"].query["+j+"].name");
                if(queryName==null) break;
                String engine=options.get(OPTIONS_PREFIX+"group["+i+"].query["+j+"].engine");
                String script=options.get(OPTIONS_PREFIX+"group["+i+"].query["+j+"].script");
                QueryInfo info=new QueryInfo(queryName,engine,script);
                group.queries.add(info);
            }
            queryGroups.add(group);
        }
    }
    
    

    
    
    
    public void writeOptions() {
        
        // If user adds new custom topic panel script, it is saved to global
        // options instead of local. Therefore we hide class level variable
        // with another:
        Options options = globalOptions;
        
        for(int i=0;true;i++) {
            String groupName=options.get(OPTIONS_PREFIX+"group["+i+"].name");
            if(groupName==null) break;
            options.put(OPTIONS_PREFIX+"group["+i+"].name",null);
            for(int j=0;true;j++) {
                String queryName=options.get(OPTIONS_PREFIX+"group["+i+"].query["+j+"].name");
                if(queryName==null) break;
                options.put(OPTIONS_PREFIX+"group["+i+"].query["+j+"].name",null);
                options.put(OPTIONS_PREFIX+"group["+i+"].query["+j+"].engine",null);
                options.put(OPTIONS_PREFIX+"group["+i+"].query["+j+"].script",null);
            }
        }
        for(int i=0;i<queryGroups.size();i++) {
            QueryGroupInfo groupInfo=queryGroups.get(i);
            options.put(OPTIONS_PREFIX+"group["+i+"].name",groupInfo.name);
            for(int j=0;j<groupInfo.queries.size();j++) {
                QueryInfo info=groupInfo.queries.get(j);
                options.put(OPTIONS_PREFIX+"group["+i+"].query["+j+"].name",info.name);
                options.put(OPTIONS_PREFIX+"group["+i+"].query["+j+"].engine",info.scriptEngine);
                options.put(OPTIONS_PREFIX+"group["+i+"].query["+j+"].script",info.script);                
            }
        }
    }
    
    
    
    
    protected void evalQuery(QueryInfo info){
        WandoraScriptManager sm=new WandoraScriptManager();
        ScriptEngine engine=sm.getScriptEngine(info.scriptEngine);
        info.directive=null;
        try {
            Object o=engine.eval(info.script);
            if(o!=null && o instanceof Directive) info.directive=(Directive)o;
        }
        catch(ScriptException se){
//            sm.showScriptExceptionDialog("custom dialog script "+info.name,se);
            info.evalException=se;
        }
        catch(Exception e){
            wandora.handleError(e);
        }
//        if(info.directive==null){
//            WandoraOptionPane.showMessageDialog(parent, "Error evaluating custom topic panel script.<br>Use Configure panel in view menu for details.", "Custom panel script", WandoraOptionPane.ERROR_MESSAGE);        
//        }
    }
    
    
    
    
    public SimplePanel buildCustomPanel(){
        SimplePanel ret=new SimplePanel();
        ret.setLayout(new java.awt.GridBagLayout());
        parseOptions();
        int bcounter=0;
        for(QueryGroupInfo groupInfo : queryGroups){
            boolean disabled=options.isFalse(getGroupOptionsKey(groupInfo.name));
            if(disabled) continue;
            
            SimplePanel groupPanel=new SimplePanel();
            groupPanel.setLayout(new java.awt.GridBagLayout());
            groupPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(groupInfo.name));
//            groupPanel.setComponentPopupMenu(getAssociationsMenu());
            groupPanel.setName(groupInfo.name+"GroupPanel");
            groupPanel.addMouseListener(wandora);
            
            GridBagConstraints gbc=new GridBagConstraints();
            int acounter=0;
            
            for(QueryInfo info : groupInfo.queries){
                
                if(info.directive==null) {
                    evalQuery(info);
                    if(info.directive==null) {
                        if(info.evalException!=null)
                            WandoraOptionPane.showMessageDialog(wandora, "Error evaluating custom topic panel script "+groupInfo.name+"/"+info.name+"<br>"+info.evalException.getMessage(), "Custom panel script", WandoraOptionPane.ERROR_MESSAGE);        
                        else
                            WandoraOptionPane.showMessageDialog(wandora, "Error evaluating custom topic panel script "+groupInfo.name+"/"+info.name+"<br>Script did not return a Directive object.", "Custom panel script", WandoraOptionPane.ERROR_MESSAGE);        
                        continue;
                    }
                }
                MixedTopicTable table=buildCustomQuery(info.directive,topic);
                if(table==null) continue;
                
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.anchor=GridBagConstraints.WEST;
                if(acounter!=1) gbc.insets=new java.awt.Insets(10,0,0,0);
                SimpleLabel label=new SimpleLabel(info.name);
//                JPopupMenu popup = this.getAssociationTypeMenu();
//                label.setComponentPopupMenu(popup);
                groupPanel.add(label,gbc);
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                groupPanel.add(table.getTableHeader(),gbc);
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                groupPanel.add(table,gbc);
            }
            
            if(acounter>0){
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=bcounter++;
                gbc.gridx=0;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                gbc.insets = new java.awt.Insets(7, 0, 7, 0);
                ret.add(groupPanel, gbc);        
            }
            
        }
        return ret;
    }
    
    
    
    
    public MixedTopicTable buildCustomQuery(Directive query, Topic context){
        
        try {
            TopicMap tm=wandora.getTopicMap();
            ArrayList<ResultRow> res=query.doQuery(new QueryContext(tm,wandora.getLang()),new ResultRow(context));
            ArrayList<String> columns=new ArrayList<String>();
            for(ResultRow row : res){
                for(int i=0;i<row.getNumValues();i++){
                    String r=row.getRole(i);
                    if(!columns.contains(r)) columns.add(r);
                }
            }
            ArrayList<Object> columnLabelsA = new ArrayList<Object>();
            for(int i=0;i<columns.size();i++){
                String r=columns.get(i);
                if(r.startsWith("~")) {
                    columns.remove(i);
                    i--;
                }
                else {
                    String locator = r;
                    if(locator.startsWith("#")) locator = Directive.DEFAULT_NS+locator;
                    Topic t = tm.getTopic(locator);
                    if(t != null) columnLabelsA.add(t);
                    else columnLabelsA.add(r);
                }
            }
            Object[] columnLabels = columnLabelsA.toArray(new Object[columnLabelsA.size()]);
            if(res.size()>0) {
                Object[][] data=new Object[res.size()][columns.size()];
                for(int i=0; i<res.size(); i++){
                    ResultRow row=res.get(i);
                    for(int j=0; j<columns.size(); j++) {
                        String r=columns.get(j);
                        Object v=row.getValue(r);

                        if(v != null) {
                            if(v instanceof Topic) {
                                Locator l = ((Topic) v).getOneSubjectIdentifier();
                                if(l != null) {
                                    Topic t = tm.getTopic((Locator)l);
                                    data[i][j]=t;
                                }
                                else {
                                    data[i][j]=l.toExternalForm();
                                }
                            }
                            else if(v instanceof Locator) {
                                data[i][j]=((Locator) v).toExternalForm();
                            }
                            else if(v instanceof String) {
                                data[i][j]=((String) v);
                            }
                            else {
                                data[i][j] = v.toString();
                            }
                        }
                        
                        /*
                        Locator l=new Locator(r);
                        if(v instanceof Topic) v=((Topic)v).getOneSubjectIdentifier();
                        else if(v instanceof String) v=new Locator((String)v);
                        else if(v instanceof Locator){}
                        else v=null;
                        Topic t=null;
                        if(v!=null) t=tm.getTopic((Locator)v);
                        data[i][j]=t;
                        */
                    }
                }
                
                MixedTopicTable table=new MixedTopicTable(wandora);
                table.initialize(data, columnLabels);
                
                return table;
            }
        }
        catch(QueryException qe) {
            qe.printStackTrace();
        }
        catch(TopicMapException tme) {
            tme.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    
    
    @Override
    public void refresh() {
        
        try {
            topic=wandora.getTopicMap().getTopic(topicSI);
            if(topic==null || topic.isRemoved()) {
                System.out.println("Topic is null or removed!");
                panelContainer.setVisible(false);
                removedTopicMessage.setVisible(true);
                return;
            }
            else {
                panelContainer.setVisible(true);
                removedTopicMessage.setVisible(false);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Topic is null or removed!");
            panelContainer.setVisible(false);
            removedTopicMessage.setVisible(true);
            return;
        }
        
        super.refresh();

        try {
            if(viewSubjectLocatorResources && topic.getSubjectLocator() != null) {
                ((PreviewWrapper) previewPanel).setURL(topic.getSubjectLocator());
            }
            else {
                ((PreviewWrapper) previewPanel).setURL(null);
            }
        }
        catch(Exception e) {}
        
        if(subjectIdentifierRootPanel.isVisible()) {
            buildSubjectIdentifierPanel(subjectIdentifierPanel, topic, options, wandora);
        }
        
        if(customPanel!=null) panelContainer.remove(customPanel);
        customPanel=buildCustomPanel();
        GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(0, 10, 0, 10);
        panelContainer.add(customPanel, gbc);        
        panelContainer.revalidate();
        try {
            if(topic.getBaseName()!=null) {
                baseNameField.setText(topic.getBaseName());
                baseNameField.setCaretPosition(0);
                Color c=wandora.topicHilights.getBaseNameColor(topic);
                if(c!=null) baseNameField.setForeground(c);
                else baseNameField.setForeground(Color.BLACK);
            }
            else {
                baseNameField.setText("");
                baseNameField.setForeground(Color.BLACK);
            }
            originalBN=topic.getBaseName();
            if(topic.getSubjectLocator()!=null) {
                subjectLocatorField.setText(topic.getSubjectLocator().toString());
                subjectLocatorField.setCaretPosition(0);
                Color c=wandora.topicHilights.getSubjectLocatorColor(topic);
                if(c!=null) subjectLocatorField.setForeground(c);
                else subjectLocatorField.setForeground(Color.BLACK);
                originalSL=topic.getSubjectLocator().toString();
            }
            else {
                subjectLocatorField.setText("");
                subjectLocatorField.setForeground(Color.BLACK);
                originalSL=null;
            }
        }
        catch(Exception e) {
            System.out.println("Failed to initialize base or/and sl!");
            e.printStackTrace();
        }
        
        this.setComponentPopupMenu(this.getViewPopupMenu());
    }    

    
    
    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }
    
    
    
    
    @Override
    public void stop() {
        if(previewPanel != null && previewPanel instanceof PreviewWrapper) {
            ((PreviewWrapper) previewPanel).stop();
        }
    }
    
    
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int param) throws PrinterException {
        if (param > 0) {
            return(NO_SUCH_PAGE);
        } else {
            Graphics2D g2d = (Graphics2D)graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            // Turn off double buffering
            this.paint(g2d);
            // Turn double buffering back on
            return(PAGE_EXISTS);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    @Override
    public JPopupMenu getNamesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getVariantsLabelPopupStruct(options), wandora);
    }
    

    @Override
    public JPopupMenu getClassesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getClassesTablePopupStruct(), wandora);
    }
    
    
    @Override
    public JPopupMenu getInstancesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getInstancesTablePopupStruct(), wandora);
    }
    
    
    @Override
    public JPopupMenu getSIMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getSubjectIdentifierLabelPopupStruct(), wandora);
    }

    
    @Override
    public JPopupMenu getOccurrencesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getOccurrencesLabelPopupStruct(options), wandora);
    }
    
    
    @Override
    public JPopupMenu getOccurrenceTypeMenu(Topic occurrenceType) {
         return UIBox.makePopupMenu(WandoraMenuManager.getOccurrenceTypeLabelPopupStruct(occurrenceType, topic), wandora);
    }
    
    
    public JPopupMenu getSLMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getSubjectLocatorLabelPopupStruct(), wandora);
    }
    
    
    @Override
    public JPopupMenu getAssociationsMenu() {
         return UIBox.makePopupMenu(WandoraMenuManager.getAssociationTableLabelPopupStruct(), wandora);
    }

    
    @Override
    public JPopupMenu getAssociationTypeMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getAssociationTypeLabelPopupStruct(), wandora);
    }

    
    @Override
    public JPopupMenu getSubjectMenu() {
        return null;
    }
    
    @Override
    public boolean noScroll(){
        return false;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        configureButton = new org.wandora.application.gui.simple.SimpleButton();
        configDialog = new JDialog(wandora);
        configurationPanel = new CustomTopicPanelConfiguration(wandora);
        jPanel4 = new javax.swing.JPanel();
        configCancelButton = new org.wandora.application.gui.simple.SimpleButton();
        configOkButton = new org.wandora.application.gui.simple.SimpleButton();
        panelContainer = new javax.swing.JPanel();
        previewPanelContainer = new javax.swing.JPanel();
        previewPanel = PreviewWrapper.getPreviewWrapper(this);
        idPanelWrapper = new javax.swing.JPanel();
        idPanel = new javax.swing.JPanel();
        baseNameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        baseNameField = new org.wandora.application.gui.simple.SimpleField();
        subjectLocatorLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectLocatorField = new org.wandora.application.gui.simple.SimpleURIField();
        subjectIdentifierLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectIdentifierRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        subjectIdentifierPanel = new javax.swing.JPanel();
        removedTopicMessage = new javax.swing.JPanel();
        removedTopicMessageLabel = new javax.swing.JLabel();

        configureButton.setText("Configure");
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        configDialog.setTitle("Custom topic panel configuration");
        configDialog.setModal(true);
        configDialog.getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        configDialog.getContentPane().add(configurationPanel, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        configCancelButton.setText("Cancel");
        configCancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        configCancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        configCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configCancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel4.add(configCancelButton, gridBagConstraints);

        configOkButton.setText("OK");
        configOkButton.setMaximumSize(new java.awt.Dimension(70, 23));
        configOkButton.setPreferredSize(new java.awt.Dimension(70, 23));
        configOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configOkButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel4.add(configOkButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        configDialog.getContentPane().add(jPanel4, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        panelContainer.setLayout(new java.awt.GridBagLayout());

        previewPanelContainer.setLayout(new java.awt.GridBagLayout());

        previewPanel.setToolTipText("");
        previewPanel.setMinimumSize(new java.awt.Dimension(2, 2));
        previewPanel.setName("previewPanel"); // NOI18N
        previewPanel.setPreferredSize(new java.awt.Dimension(2, 2));
        previewPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        previewPanelContainer.add(previewPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 5, 15);
        panelContainer.add(previewPanelContainer, gridBagConstraints);

        idPanelWrapper.setLayout(new java.awt.GridBagLayout());

        idPanel.setLayout(new java.awt.GridBagLayout());

        baseNameLabel.setText("Base name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        idPanel.add(baseNameLabel, gridBagConstraints);

        baseNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                baseNameFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        idPanel.add(baseNameField, gridBagConstraints);

        subjectLocatorLabel.setText("Subject locator");
        subjectLocatorLabel.setComponentPopupMenu(getSLMenu());
        subjectLocatorLabel.addMouseListener(wandora);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        idPanel.add(subjectLocatorLabel, gridBagConstraints);

        subjectLocatorField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                subjectLocatorFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        idPanel.add(subjectLocatorField, gridBagConstraints);

        subjectIdentifierLabel.setText("Subject identifiers");
        subjectIdentifierLabel.setComponentPopupMenu(getSIMenu());
        subjectLocatorLabel.addMouseListener(wandora);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        idPanel.add(subjectIdentifierLabel, gridBagConstraints);

        subjectIdentifierRootPanel.setName("subjectIdentifierRootPanel"); // NOI18N
        subjectIdentifierRootPanel.setLayout(new java.awt.BorderLayout());

        subjectIdentifierPanel.setLayout(new java.awt.GridLayout(1, 0));
        subjectIdentifierRootPanel.add(subjectIdentifierPanel, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 1);
        idPanel.add(subjectIdentifierRootPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 15, 7, 15);
        idPanelWrapper.add(idPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panelContainer.add(idPanelWrapper, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panelContainer, gridBagConstraints);

        removedTopicMessage.setBackground(new java.awt.Color(255, 255, 255));
        removedTopicMessage.setLayout(new java.awt.GridBagLayout());

        removedTopicMessageLabel.setText("<html>Topic is either merged or removed and can not be viewed!</html>");
        removedTopicMessage.add(removedTopicMessageLabel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(removedTopicMessage, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public static String checkScript(Component parent,String engineString,String script){
        WandoraScriptManager sm=new WandoraScriptManager();
        ScriptEngine engine=sm.getScriptEngine(engineString);
        if(engine==null){
            return "Couldn't find script engine";
        }
        try {
            Object o=engine.eval(script);
            if(o==null){
                return "Script returned null.";
            }
            else if(!(o instanceof org.wandora.query2.Directive)){
                return "Script didn't return an instance of Directive.<br>"+
                       "Class of return value is "+o.getClass().getName();
            }
        }
        catch(ScriptException se){
            return "ScriptException at line "+se.getLineNumber()+" column "+se.getColumnNumber()+"<br>"+se.getMessage();
        }
        catch(Exception e){
            e.printStackTrace();
            return "Exception occurred during execution: "+e.getClass().getName()+" "+e.getMessage();
        }
        return null;        
    }
    
    private void configOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configOkButtonActionPerformed
        if(configDialog.isVisible()){
            
            ArrayList<QueryGroupInfo> newGroups=((CustomTopicPanelConfiguration)configurationPanel).getQueryGroups();
/*            boolean error=false;
            for(QueryGroupInfo group : newGroups){
                for(QueryInfo query : group.queries){
                    String message=checkScript(this,query.scriptEngine,query.script);
                    if(message!=null){
                        error=true;
                        WandoraOptionPane.showMessageDialog(configDialog, "Error in group "+group.name+", query "+query.name+"<br>"+message, "Check syntax", WandoraOptionPane.ERROR_MESSAGE);        
                    }
                }
            }
            if(!error){*/
                queryGroups=newGroups;
                writeOptions();
                refresh();
                configDialog.setVisible(false);
//            }
        }        
    }//GEN-LAST:event_configOkButtonActionPerformed

    private void configCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configCancelButtonActionPerformed
        if(configDialog.isVisible()){
            configDialog.setVisible(false);
        }
    }//GEN-LAST:event_configCancelButtonActionPerformed

    protected void showConfigureDialog(){
        ((CustomTopicPanelConfiguration)configurationPanel).readQueryGroups(queryGroups);
        configDialog.setSize(400,500);
        wandora.centerWindow(configDialog);
        configDialog.setVisible(true);        
        wandora.topicPanelsChanged();
    }
    
    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        showConfigureDialog();
    }//GEN-LAST:event_configureButtonActionPerformed

    private void subjectLocatorFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subjectLocatorFieldKeyReleased
        try {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
                applyChanges();
            }
        } catch(Exception e) {}
    }//GEN-LAST:event_subjectLocatorFieldKeyReleased

    private void baseNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_baseNameFieldKeyReleased
        try {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
                applyChanges();
            }
        } catch(Exception e) {}
    }//GEN-LAST:event_baseNameFieldKeyReleased
    
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField baseNameField;
    private javax.swing.JLabel baseNameLabel;
    private javax.swing.JButton configCancelButton;
    private javax.swing.JDialog configDialog;
    private javax.swing.JButton configOkButton;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JButton configureButton;
    private javax.swing.JPanel idPanel;
    private javax.swing.JPanel idPanelWrapper;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel panelContainer;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JPanel previewPanelContainer;
    private javax.swing.JPanel removedTopicMessage;
    private javax.swing.JLabel removedTopicMessageLabel;
    private javax.swing.JLabel subjectIdentifierLabel;
    private javax.swing.JPanel subjectIdentifierPanel;
    private javax.swing.JPanel subjectIdentifierRootPanel;
    private javax.swing.JTextField subjectLocatorField;
    private javax.swing.JLabel subjectLocatorLabel;
    // End of variables declaration//GEN-END:variables
    
    
    // -------------------------------------------------------------------------
    
    public static class QueryGroupInfo {
        public String name;
        public ArrayList<QueryInfo> queries;
        public QueryGroupInfo(){
            queries=new ArrayList<QueryInfo>();
        }
        public QueryGroupInfo deepCopy(){
            QueryGroupInfo g=new QueryGroupInfo();
            g.name=this.name;
            for(QueryInfo q : queries){
                g.queries.add(q.copy());
            }
            return g;
        }
        
        
        
        @Override
        public String toString(){ return name; }
    }

    
    
    
    
    // -------------------------------------------------------------------------

    public static class QueryInfo {
        public String name;
        public String scriptEngine;
        public String script;
        
        public Directive directive;
        public ScriptException evalException;
        public QueryInfo(){}
        public QueryInfo(String name,String scriptEngine,String script){
            this.name=name;
            this.scriptEngine=scriptEngine;
            this.script=script;
        }
        public QueryInfo copy(){
            return new QueryInfo(name,scriptEngine,script);
        }
        public String toString(){ return name; }
    }





    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------


    
    private class TopicPanelTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            return false;
            //if(!support.isDrop()) return false;
            //return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
            //       support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                /*
                TopicMap tm=parent.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base=topic;
                if(base==null) return false;

                for(Topic t : topics) {
                    if(t != null && !t.isRemoved()) {
                        base.addType(t);
                    }
                }
                */ 
                wandora.doRefresh();
            }
            catch(Exception ce){
                Wandora.getWandora().handleError(ce);
            }
            return false;
        }

    }
    

}

