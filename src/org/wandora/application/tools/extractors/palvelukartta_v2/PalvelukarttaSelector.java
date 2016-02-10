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
 * PalvelukarttaSelector.java
 *
 * Created on Dec 16, 2011, 6:49:47 PM
 */

package org.wandora.application.tools.extractors.palvelukartta_v2;

import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JTextField;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleRadioButton;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;

import org.apache.commons.lang.StringUtils;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */


public class PalvelukarttaSelector extends javax.swing.JPanel {

    private static final String BASE_URL 
            = "http://www.hel.fi/palvelukarttaws/rest/v2/";
    private static final String ORGANIZATION_URL = BASE_URL + "organization/";
    private static final String DEPARTMENT_URL = BASE_URL + "department/";
    private static final String UNIT_URL = BASE_URL + "unit/";
    private static final String SERVICE_URL = BASE_URL + "service/";
    
    private boolean wasAccepted = false;
    private JDialog myDialog = null;
    private Wandora wandora = null;
    private Context context = null;
    
    
    /** Creates new form PalvelukarttaSelector */
    public PalvelukarttaSelector() {
        initComponents();
    }
    
    
    

    
    public void setContext(Context c) {
        context = c;
    }
    
    public void setWandora(Wandora w) {
        wandora = w;
    }
    
    public void setAccepted(boolean isAccepted) {
        wasAccepted = isAccepted;
    }
    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    
    public void openDialog(Wandora w) {
        wasAccepted = false;
        if(myDialog == null) {
            myDialog = new JDialog(w, true);
            myDialog.setTitle("Palvelukartta extractor");
            wandora = w;
            myDialog.add(this);
            myDialog.setSize(600,350);
            UIBox.centerWindow(myDialog, w);
        }
        myDialog.setVisible(true);
    }
    
    
    public ArrayList<WandoraTool> getSelectedExtractors(PalvelukarttaExtractor pke) {
        ArrayList<WandoraTool> extractors = new ArrayList();
        Component component = pkTabbedPane.getSelectedComponent();

        // ***** EASY/ALL *****
        if(easyPanel.equals(component)) {
            
            if(allOrganizationsCheckBox.isSelected()) {
                PalvelukarttaOrganizationExtractor e 
                        = new PalvelukarttaOrganizationExtractor();
                e.setForceUrls( new String[] { ORGANIZATION_URL } );
                extractors.add(e);
            }
            if(allDepartmentsCheckBox.isSelected()) {
                PalvelukarttaDepartmentExtractor e 
                        = new PalvelukarttaDepartmentExtractor();
                e.setForceUrls( new String[] { DEPARTMENT_URL } );
                extractors.add(e);
            }
            if(allUnitsCheckBox.isSelected()) {
                boolean deepExtraction = deepUnitsCheckBox.isSelected();
                PalvelukarttaUnitExtractor e 
                        = new PalvelukarttaUnitExtractor(deepExtraction);
                e.setForceUrls( new String[] { UNIT_URL } );
                extractors.add(e);
            }
            if(allServicesCheckBox.isSelected()) {
                PalvelukarttaServiceExtractor e 
                        = new PalvelukarttaServiceExtractor();
                e.setForceUrls( new String[] { SERVICE_URL } );
                extractors.add(e);
            }
        }
        
        if(queryPanel.equals(component)){
            
            ArrayList<String> args = new ArrayList<String>();
            
            String[][] fieldData = new String[][]{
                {"search",      searchTextField.getText()},
                {"organization",orgTextField.getText()},
                {"arealcity",   areaTextField.getText()},
                {"service",     serviceTextField.getText()},
                {"department",  depTextField.getText()},
                {"lat",         latTextField.getText()},
                {"lon",         longTextField.getText()},
                {"distance",    distanceTextField.getText()}
            };
            
            for (int i = 0; i < 8;  i++) {
                if(fieldData[i][1].length() > 0){
                    String arg = urlEncode(fieldData[i][1].trim());
                    arg = arg.replaceAll("%2C", "+");
                    args.add(fieldData[i][0] + "=" + arg);
                }
            }
            
            StringBuilder sb = new StringBuilder(UNIT_URL);
            sb.append("?");
            sb.append(StringUtils.join(args,"&"));
            
            PalvelukarttaUnitExtractor e = new PalvelukarttaUnitExtractor();
            System.out.println(sb.toString());
            e.setForceUrls(new String[] {sb.toString()});
            extractors.add(e);
            
            
        }
        
        if(idPanel.equals(component)) {
            
            String idListString = idTextField.getText();
            String[] idList = urlEncode(commaSplitter(idListString));
            
            if(organizationIdRadioButton.isSelected()) {
                String extractUrl = ORGANIZATION_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaOrganizationExtractor e 
                        = new PalvelukarttaOrganizationExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
            if(departmentIdRadioButton.isSelected()) {
                String extractUrl = DEPARTMENT_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaDepartmentExtractor e 
                        = new PalvelukarttaDepartmentExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
            if(unitIdRadioButton.isSelected()) {
                String extractUrl = UNIT_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaUnitExtractor e 
                        = new PalvelukarttaUnitExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
            if(serviceIdRadioButton.isSelected()) {
                String extractUrl = SERVICE_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaServiceExtractor e 
                        = new PalvelukarttaServiceExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
        }

        return extractors;
    }
    
    public String[] commaSplitter(String str) {
        if(str.indexOf(',') != -1) {
            String[] strs = str.split(",");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.length() > 0) {
                    strList.add(s);
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
        
    }
    
    public String[] completeString(String template, String[] strs) {
        if(strs == null || template == null) return null;
        String[] completed = new String[strs.length];
        for(int i=0; i<strs.length; i++) {
            completed[i] = template.replaceAll("__1__", strs[i]);
        }
        return completed;
    }
    
    public String[] completeString(String template, String[] strs1, String[] strs2) {
        if(strs1 == null || strs2 == null || template == null) return null;
        if(strs1.length != strs2.length) return null;
        
        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2[i]);
        }
        return completed;
    }
    
    public String[] urlEncode(String[] urls) {
        if(urls == null) return null;
        String[] cleanUrls = new String[urls.length];
        for(int i=0; i<urls.length; i++) {
            cleanUrls[i] = urlEncode(urls[i]);
        }
        return cleanUrls;
    }
    
    protected static String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {}
        return str;
    }
    
    private void getIdContext() {
        getIdContext(idTextField);
    }
    
    private void getIdContext(JTextField target){
        try {
            if(context == null) return;
            Iterator i = context.getContextObjects();
            if(!i.hasNext()) return;
           
            ArrayList<String> ids = new ArrayList<String>();
            while(i.hasNext()) {
                Object o = i.next();
                if(o != null && o instanceof Topic) {
                    Topic t = (Topic) o;
                    Collection<Locator> sis = t.getSubjectIdentifiers();
                    for(Locator si : sis) {
                        if (si==null) continue;
                        boolean atItPanel = (pkTabbedPane.getSelectedComponent().equals(idPanel));
                        String siStr = si.toExternalForm();
                        String id;
                        System.out.println(siStr);
                        if(siStr.startsWith(ORGANIZATION_URL)) {
                            id = siStr.substring(ORGANIZATION_URL.length());
                            if(id != null && id.length() > 0) {
                                ids.add(id);
                                if(atItPanel) organizationIdRadioButton.setSelected(true);
                            }
                        }
                        else if(siStr.startsWith(DEPARTMENT_URL)) {
                            id = siStr.substring(DEPARTMENT_URL.length());
                            if(id != null && id.length() > 0) {
                                ids.add(id);
                                if(atItPanel) departmentIdRadioButton.setSelected(true);
                            }
                        }
                        else if(siStr.startsWith(UNIT_URL)) {
                            id = siStr.substring(UNIT_URL.length());
                            if(id != null && id.length() > 0) {
                                ids.add(id);
                                if(atItPanel) unitIdRadioButton.setSelected(true);
                            }
                        }
                        else if(siStr.startsWith(SERVICE_URL)) {
                            id = siStr.substring(SERVICE_URL.length());
                            if(id != null && id.length() > 0) {
                                ids.add(id);
                                if(atItPanel) serviceIdRadioButton.setSelected(true);
                            }
                        }
                    }
                }
            }
            
            for(String id : ids){
                System.out.println(id);
            }
            
            target.setText(StringUtils.join(ids, ","));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * lat+long+dist: We can't use multiple values here so we just pick the first one.
     */
    private void getIdContext(JTextField distField, JTextField latField, 
            JTextField longField){
        
        if (context == null || wandora == null) return;
        TopicMap tm = wandora.getTopicMap();
        String locSI = AbstractPalvelukarttaExtractor.PALVELUKARTTA_GEOLOCATION_SI;
                
        try {
            Topic t = null;
            Topic locType = tm.getTopic(new Locator(locSI));
            Iterator i = context.getContextObjects();
            while(i.hasNext()){
                Object o = i.next();
                if((o instanceof Topic)) t = (Topic) o;
            }
            
            if(t != null){
                String loc = t.getData(locType, XTMPSI.LANG_INDEPENDENT);
                String[] coords = StringUtils.split(loc, ",");
                if (coords.length < 2 ) return;
                String latitude = coords[0];
                String longitude = coords[1];
                latField.setText(latitude);
                longField.setText(longitude);
                distField.setText("100");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        idButtonGroup = new javax.swing.ButtonGroup();
        pkTabbedPane = new SimpleTabbedPane();
        easyPanel = new javax.swing.JPanel();
        allPanel = new javax.swing.JPanel();
        allLabel = new SimpleLabel();
        allOrganizationsCheckBox = new SimpleCheckBox();
        allDepartmentsCheckBox = new SimpleCheckBox();
        allUnitsCheckBox = new SimpleCheckBox();
        deepUnitsCheckBox = new SimpleCheckBox();
        allServicesCheckBox = new SimpleCheckBox();
        queryPanel = new javax.swing.JPanel();
        inPanelInner1 = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        orgLabel = new SimpleLabel();
        orgTextField = new SimpleField();
        orgGetContextButton = new SimpleButton();
        areaLabel = new SimpleLabel();
        areaTextField = new SimpleField();
        areaGetContextButton = new SimpleButton();
        serviceLabel = new SimpleLabel();
        serviceTextField = new SimpleField();
        serviceGetContextButton = new SimpleButton();
        depLabel = new SimpleLabel();
        depTextField = new SimpleField();
        depGetContextButton = new SimpleButton();
        termLabel = new SimpleLabel();
        searchTextField = new SimpleField();
        latLabel = new SimpleLabel();
        latTextField = new SimpleField();
        longLabel = new SimpleLabel();
        longTextField = new SimpleField();
        distanceLabel = new SimpleLabel();
        distanceTextField = new SimpleField();
        distanceGetContextButton = new SimpleButton();
        idPanel = new javax.swing.JPanel();
        inPanelInner = new javax.swing.JPanel();
        idLabel = new SimpleLabel();
        idTextField = new SimpleField();
        jPanel2 = new javax.swing.JPanel();
        organizationIdRadioButton = new SimpleRadioButton();
        departmentIdRadioButton = new SimpleRadioButton();
        unitIdRadioButton = new SimpleRadioButton();
        serviceIdRadioButton = new SimpleRadioButton();
        jPanel1 = new javax.swing.JPanel();
        idFieldClearButton = new SimpleButton();
        idGetContextButton = new SimpleButton();
        pkButtonPanel = new javax.swing.JPanel();
        openButton = new SimpleButton();
        pkFillerPanel = new javax.swing.JPanel();
        extractButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setMinimumSize(new java.awt.Dimension(408, 350));
        setLayout(new java.awt.GridBagLayout());

        easyPanel.setLayout(new java.awt.GridBagLayout());

        allPanel.setLayout(new java.awt.GridBagLayout());

        allLabel.setText("<html>Extract complete lists from Palvelukartta. You can extract a list of organizations, a list of departments, units and services. You can select to extract multiple lists at once.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        allPanel.add(allLabel, gridBagConstraints);

        allOrganizationsCheckBox.setSelected(true);
        allOrganizationsCheckBox.setText("Extract all organizations");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allOrganizationsCheckBox, gridBagConstraints);

        allDepartmentsCheckBox.setSelected(true);
        allDepartmentsCheckBox.setText("Extract all departments");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allDepartmentsCheckBox, gridBagConstraints);

        allUnitsCheckBox.setSelected(true);
        allUnitsCheckBox.setText("Extract all units");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allUnitsCheckBox, gridBagConstraints);

        deepUnitsCheckBox.setSelected(true);
        deepUnitsCheckBox.setText("Trigger separate extraction for each unit (more information but slower)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        allPanel.add(deepUnitsCheckBox, gridBagConstraints);

        allServicesCheckBox.setSelected(true);
        allServicesCheckBox.setText("Extract all services");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allServicesCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        easyPanel.add(allPanel, gridBagConstraints);

        pkTabbedPane.addTab("Lists", easyPanel);

        queryPanel.setLayout(new java.awt.GridBagLayout());

        inPanelInner1.setLayout(new java.awt.GridBagLayout());

        searchLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchLabel.setText("<html>Extract Palvelukartta units with a search using terms and filters.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        inPanelInner1.add(searchLabel, gridBagConstraints);

        orgLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        orgLabel.setText("<html>Organization id(s)</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        inPanelInner1.add(orgLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        inPanelInner1.add(orgTextField, gridBagConstraints);

        orgGetContextButton.setText("Get context");
        orgGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        orgGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                orgGetContextButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        inPanelInner1.add(orgGetContextButton, gridBagConstraints);

        areaLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        areaLabel.setText("<html>Area  id(s)</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        inPanelInner1.add(areaLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        inPanelInner1.add(areaTextField, gridBagConstraints);

        areaGetContextButton.setText("Get context");
        areaGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        areaGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                areaGetContextButtonMouseReleased(evt);
            }
        });
        areaGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                areaGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        inPanelInner1.add(areaGetContextButton, gridBagConstraints);

        serviceLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        serviceLabel.setText("<html>Service  id(s)</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        inPanelInner1.add(serviceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        inPanelInner1.add(serviceTextField, gridBagConstraints);

        serviceGetContextButton.setText("Get context");
        serviceGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        serviceGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                serviceGetContextButtonMouseReleased(evt);
            }
        });
        serviceGetContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serviceGetContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        inPanelInner1.add(serviceGetContextButton, gridBagConstraints);

        depLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        depLabel.setText("<html>Department  id(s)</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        inPanelInner1.add(depLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        inPanelInner1.add(depTextField, gridBagConstraints);

        depGetContextButton.setText("Get context");
        depGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        depGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                depGetContextButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
        inPanelInner1.add(depGetContextButton, gridBagConstraints);

        termLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        termLabel.setText("<html>Search term(s)</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        inPanelInner1.add(termLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        inPanelInner1.add(searchTextField, gridBagConstraints);

        latLabel.setText("<html>Latitude</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        inPanelInner1.add(latLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        inPanelInner1.add(latTextField, gridBagConstraints);

        longLabel.setText("<html>Longitude</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        inPanelInner1.add(longLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        inPanelInner1.add(longTextField, gridBagConstraints);

        distanceLabel.setText("<html>Distance (m)</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        inPanelInner1.add(distanceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.1;
        inPanelInner1.add(distanceTextField, gridBagConstraints);

        distanceGetContextButton.setText("Get context");
        distanceGetContextButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        distanceGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                distanceGetContextButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        inPanelInner1.add(distanceGetContextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        queryPanel.add(inPanelInner1, gridBagConstraints);

        pkTabbedPane.addTab("Search", queryPanel);

        idPanel.setLayout(new java.awt.GridBagLayout());

        inPanelInner.setLayout(new java.awt.GridBagLayout());

        idLabel.setText("<html>Extract Palvelukartta organizations, departments, units or services with a comma separated list of identifiers.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        inPanelInner.add(idLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        inPanelInner.add(idTextField, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        idButtonGroup.add(organizationIdRadioButton);
        organizationIdRadioButton.setText("Organization");
        jPanel2.add(organizationIdRadioButton, new java.awt.GridBagConstraints());

        idButtonGroup.add(departmentIdRadioButton);
        departmentIdRadioButton.setText("Department");
        jPanel2.add(departmentIdRadioButton, new java.awt.GridBagConstraints());

        idButtonGroup.add(unitIdRadioButton);
        unitIdRadioButton.setText("Unit");
        jPanel2.add(unitIdRadioButton, new java.awt.GridBagConstraints());

        idButtonGroup.add(serviceIdRadioButton);
        serviceIdRadioButton.setText("Service");
        jPanel2.add(serviceIdRadioButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        inPanelInner.add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        idFieldClearButton.setText("Clear");
        idFieldClearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                idFieldClearButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(idFieldClearButton, gridBagConstraints);

        idGetContextButton.setText("Get context");
        idGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                idGetContextButtonMouseReleased(evt);
            }
        });
        jPanel1.add(idGetContextButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        inPanelInner.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        idPanel.add(inPanelInner, gridBagConstraints);

        pkTabbedPane.addTab("With id", idPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pkTabbedPane, gridBagConstraints);

        pkButtonPanel.setLayout(new java.awt.GridBagLayout());

        openButton.setText("Open Palvelukartta");
        openButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        openButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                openButtonMouseReleased(evt);
            }
        });
        pkButtonPanel.add(openButton, new java.awt.GridBagConstraints());

        pkFillerPanel.setPreferredSize(new java.awt.Dimension(8, 8));
        pkFillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pkButtonPanel.add(pkFillerPanel, gridBagConstraints);

        extractButton.setText("Extract");
        extractButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extractButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        pkButtonPanel.add(extractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        pkButtonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(pkButtonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        wasAccepted = false;
        if(myDialog != null) myDialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void extractButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extractButtonMouseReleased
        wasAccepted = true;
        if(myDialog != null) myDialog.setVisible(false);
    }//GEN-LAST:event_extractButtonMouseReleased

    private void idFieldClearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_idFieldClearButtonMouseReleased
        idTextField.setText("");
    }//GEN-LAST:event_idFieldClearButtonMouseReleased

    private void idGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_idGetContextButtonMouseReleased
        getIdContext();
    }//GEN-LAST:event_idGetContextButtonMouseReleased

    private void openButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openButtonMouseReleased
        Desktop dt = Desktop.getDesktop();
        if(dt != null) {
            try {
                dt.browse(new URI("http://www.hel.fi/palvelukartta"));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_openButtonMouseReleased

    private void orgGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_orgGetContextButtonMouseReleased
        getIdContext(orgTextField);
    }//GEN-LAST:event_orgGetContextButtonMouseReleased

    private void areaGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_areaGetContextButtonMouseReleased
        getIdContext(areaTextField);
    }//GEN-LAST:event_areaGetContextButtonMouseReleased

    private void areaGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_areaGetContextButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_areaGetContextButtonActionPerformed

    private void serviceGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serviceGetContextButtonMouseReleased
        getIdContext(serviceTextField);
    }//GEN-LAST:event_serviceGetContextButtonMouseReleased

    private void serviceGetContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceGetContextButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_serviceGetContextButtonActionPerformed

    private void depGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depGetContextButtonMouseReleased
        getIdContext(depTextField);
    }//GEN-LAST:event_depGetContextButtonMouseReleased

    private void distanceGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_distanceGetContextButtonMouseReleased
        getIdContext(distanceTextField, latTextField, longTextField);
    }//GEN-LAST:event_distanceGetContextButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allDepartmentsCheckBox;
    private javax.swing.JLabel allLabel;
    private javax.swing.JCheckBox allOrganizationsCheckBox;
    private javax.swing.JPanel allPanel;
    private javax.swing.JCheckBox allServicesCheckBox;
    private javax.swing.JCheckBox allUnitsCheckBox;
    private javax.swing.JButton areaGetContextButton;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JTextField areaTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox deepUnitsCheckBox;
    private javax.swing.JButton depGetContextButton;
    private javax.swing.JLabel depLabel;
    private javax.swing.JTextField depTextField;
    private javax.swing.JRadioButton departmentIdRadioButton;
    private javax.swing.JButton distanceGetContextButton;
    private javax.swing.JLabel distanceLabel;
    private javax.swing.JTextField distanceTextField;
    private javax.swing.JPanel easyPanel;
    private javax.swing.JButton extractButton;
    private javax.swing.ButtonGroup idButtonGroup;
    private javax.swing.JButton idFieldClearButton;
    private javax.swing.JButton idGetContextButton;
    private javax.swing.JLabel idLabel;
    private javax.swing.JPanel idPanel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JPanel inPanelInner;
    private javax.swing.JPanel inPanelInner1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel latLabel;
    private javax.swing.JTextField latTextField;
    private javax.swing.JLabel longLabel;
    private javax.swing.JTextField longTextField;
    private javax.swing.JButton openButton;
    private javax.swing.JButton orgGetContextButton;
    private javax.swing.JLabel orgLabel;
    private javax.swing.JTextField orgTextField;
    private javax.swing.JRadioButton organizationIdRadioButton;
    private javax.swing.JPanel pkButtonPanel;
    private javax.swing.JPanel pkFillerPanel;
    private javax.swing.JTabbedPane pkTabbedPane;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton serviceGetContextButton;
    private javax.swing.JRadioButton serviceIdRadioButton;
    private javax.swing.JLabel serviceLabel;
    private javax.swing.JTextField serviceTextField;
    private javax.swing.JLabel termLabel;
    private javax.swing.JRadioButton unitIdRadioButton;
    // End of variables declaration//GEN-END:variables





  
    // -------------------------------------------------------------------------
    



}
