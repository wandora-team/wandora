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
 */

package org.wandora.application.gui.topicpanels;



import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.Wandora;
import org.wandora.application.gui.LayerTree;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapStatOptions;
import org.wandora.topicmap.layered.Layer;
import org.wandora.utils.ClipboardBox;

/**
 *
 * @author akivela
 */


public class LayerInfoPanel implements ActionListener, TopicPanel {
    
    private JPanel infoPanel = null;
    private Wandora wandora = null;
    private TopicMap map = null;
    private StringBuilder stats = null;
    private HashMap originalValues = null;
    private boolean trackChanges = false;
    private JPanel buttonPanel = null;
    private HashMap<Object, SimpleLabel> fieldLabels = null;
    
    
    public LayerInfoPanel() {
        
    }
    
    
    @Override
    public void init() {
        wandora = Wandora.getWandora();
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        fieldLabels = new HashMap();
        initInfo();
    }
    
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if(cmd != null) {
            
        }
    }
    

    @Override
    public boolean supportsOpenTopic() {
        return false;
    }
    
    
    @Override
    public void open(Topic topic) throws TopicMapException, OpenTopicNotSupportedException {

    }

    
    
    @Override
    public void stop() {

    }

    
    
    
    public void initInfo() {

        map = getCurrentTopicMap();
        
        int[] statOptions = TopicMapStatOptions.getAvailableOptions();
        int bagCount = 0;
        stats = new StringBuilder();
        GridBagConstraints gbc = null;
        SimpleLabel statDescription = null;
        SimpleLabel statValue = null;
        String statDescriptionString = null;
        String statString = "n.a.";
        String originalStatString = "n.a.";
        gbc=new GridBagConstraints();
        Insets rowInsets = new Insets(0, 9, 0, 9);
        gbc.insets = new Insets(9, 9, 0, 9);
        infoPanel.removeAll();
        
        gbc.gridy=bagCount++;
        gbc.gridx=0;
        gbc.weightx=1;
        gbc.fill=gbc.HORIZONTAL;
        String layerName = getCurrentLayerName();
        SimpleLabel layerNameLabel = new SimpleLabel(layerName);
        layerNameLabel.setFont(UIConstants.largeLabelFont);
        fieldLabels.put("title", layerNameLabel);
        infoPanel.add(layerNameLabel, gbc);
        stats.append(layerName).append("\n");
        
        boolean fillOriginalValues = false;
        if(originalValues == null) {
            originalValues = new HashMap();
            fillOriginalValues = true;
        }
        
        for(int i=0; i<statOptions.length; i++) {
            try {
                gbc.gridy=bagCount++;
                gbc.gridx=0;
                gbc.weightx=0.5;
                gbc.gridwidth=1;
                gbc.fill=gbc.HORIZONTAL;

                statDescriptionString = TopicMapStatOptions.describeStatOption(statOptions[i]);
                stats.append(statDescriptionString);
                statDescription = new SimpleLabel(statDescriptionString);
                infoPanel.add(statDescription, gbc);
               
                gbc.gridx=1;
                statString = "n.a.";
                try {
                    if(map != null) {
                        statString = map.getStatistics(new TopicMapStatOptions(statOptions[i])).toString();
                        if(fillOriginalValues) {
                            originalValues.put(statOptions[i], statString);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                
                if(trackChanges) {
                    originalStatString = originalValues.get(statOptions[i]).toString();
                    stats.append("\t").append(originalStatString);
                    SimpleLabel originalValue = new SimpleLabel(originalStatString);
                    originalValue.setHorizontalAlignment(SimpleLabel.RIGHT);
                    fieldLabels.put("o"+statOptions[i], originalValue);
                    infoPanel.add(originalValue, gbc);
                    gbc.gridx=2;
                }
                
                Color statValueColor = Color.BLACK;
                if(trackChanges) {
                    try {
                        int statInt = Integer.parseInt(statString);
                        int originalInt = Integer.parseInt(originalStatString);

                        int delta = statInt - originalInt;
                        statString = (delta > 0 ? "+"+delta : ""+delta);
                        
                        if(delta > 0) statValueColor = Color.green.darker();
                        else if(delta < 0) statValueColor = Color.red.darker();
                    }
                    catch(Exception e) {}
                }
                
                stats.append("\t").append(statString);
                statValue = new SimpleLabel(statString);
                statValue.setForeground(statValueColor);
                statValue.setHorizontalAlignment(SimpleLabel.RIGHT);
                fieldLabels.put(statOptions[i], statValue);
                infoPanel.add(statValue, gbc);
                
                stats.append("\n");
                gbc.gridy=bagCount++;
                gbc.gridx=0;
                gbc.weightx=1;
                gbc.gridwidth=trackChanges ? 3 : 2;
                gbc.insets = rowInsets;
                JSeparator js = new JSeparator(JSeparator.HORIZONTAL);
                js.setForeground(new Color(0x909090));
                infoPanel.add(js, gbc);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        gbc.gridx=0;
        gbc.gridy=bagCount++;
        gbc.weightx=0;
        gbc.weightx=0;
        gbc.fill=GridBagConstraints.NONE;
        gbc.insets = new Insets(9, 9, 9, 9);
        
        JPanel buttonPanel = getButtonPanel();
        infoPanel.add(buttonPanel, gbc);
      
        
        gbc.gridy=bagCount++;
        gbc.weightx=1;
        gbc.weighty=1;
        gbc.fill=GridBagConstraints.BOTH;
        infoPanel.add(new JPanel(), gbc); // Filler
    }
    
    
    
    public void refreshInfo() {
        map = getCurrentTopicMap();
        
        int[] statOptions = TopicMapStatOptions.getAvailableOptions();
        stats = new StringBuilder();
        SimpleLabel statValue = null;
        String statString = "n.a.";
        String originalStatString = "n.a.";
        
        String layerName = getCurrentLayerName();
        SimpleLabel layerNameLabel = fieldLabels.get("title");
        if(layerNameLabel != null) {
            layerNameLabel.setText(layerName);
        }

        stats.append(layerName).append("\n");
        
        boolean fillOriginalValues = false;
        if(originalValues == null) {
            originalValues = new HashMap();
            fillOriginalValues = true;
        }
        
        for(int i=0; i<statOptions.length; i++) {
            try {

                statString = "n.a.";
                try {
                    if(map != null) {
                        statString = map.getStatistics(new TopicMapStatOptions(statOptions[i])).toString();
                        if(fillOriginalValues) {
                            originalValues.put(statOptions[i], statString);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                
                if(trackChanges) {
                    originalStatString = originalValues.get(statOptions[i]).toString();
                    stats.append("\t").append(originalStatString);
                    SimpleLabel originalValue = fieldLabels.get("o"+statOptions[i]);
                    if(originalValue != null) {
                        originalValue.setText(originalStatString);
                    }
                }
                
                Color statValueColor = Color.BLACK;
                if(trackChanges) {
                    try {
                        int statInt = Integer.parseInt(statString);
                        int originalInt = Integer.parseInt(originalStatString);

                        int delta = statInt - originalInt;
                        statString = (delta > 0 ? "+"+delta : ""+delta);
                        
                        if(delta > 0) statValueColor = Color.green.darker();
                        else if(delta < 0) statValueColor = Color.red.darker();
                    }
                    catch(Exception e) {}
                }
                
                stats.append("\t").append(statString);
                statValue = fieldLabels.get(statOptions[i]);
                if(statValue != null) {
                    statValue.setForeground(statValueColor);
                    statValue.setText(statString);
                }

                stats.append("\n");
             }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private JPanel getButtonPanel() {
        if(buttonPanel != null) {
            return buttonPanel;
        }
        else {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            SimpleButton copyButton = new SimpleButton("Copy");
            copyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(stats != null) {
                        ClipboardBox.setClipboard(stats.toString());
                    }
                    else {
                        ClipboardBox.setClipboard("n.a.");
                    }
                }
            });
            buttonPanel.add(copyButton);

            SimpleToggleButton trackChangesButton = new SimpleToggleButton("Track changes");
            Insets margin = trackChangesButton.getMargin();
            margin.left = 4;
            margin.right = 4;
            trackChangesButton.setMargin(margin);
            trackChangesButton.setSelected(trackChanges);
            trackChangesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    trackChanges = !trackChanges;
                    if(trackChanges) {
                        originalValues = null;
                    }
                    if(infoPanel != null) {
                        initInfo();
                        infoPanel.revalidate();
                        infoPanel.repaint();
                    }
                }
            });
            buttonPanel.add(trackChangesButton);

            return buttonPanel;
        }
    }
    
    
    
    private TopicMap getCurrentTopicMap() {
        LayerTree layerTree = wandora.layerTree;
        if(layerTree != null) {
            Layer layer = layerTree.getSelectedLayer();
            if(layer != null) {
                return layer.getTopicMap();
            }
        }
        return null;
    }
    
    private String getCurrentLayerName() {
        LayerTree layerTree = wandora.layerTree;
        if(layerTree != null) {
            Layer layer = layerTree.getSelectedLayer();
            if(layer != null) {
                return layer.getName();
            }
        }
        return "n.a.";
    }
    
    
    
    @Override
    public void refresh() throws TopicMapException {
        if(infoPanel != null) {
            refreshInfo();
            infoPanel.revalidate();
            infoPanel.repaint();
        }
    }

    
    
    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        return true;
    }

    @Override
    public JPanel getGui() {
        return infoPanel;
    }

    @Override
    public Topic getTopic() throws TopicMapException {
        return null;
    }
    
    @Override
    public String getName(){
        return "Layer info";
    }
    
    @Override
    public String getTitle() {
        return "Layer info";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_layer_info.png");
    }

    @Override
    public boolean noScroll(){
        return false;
    }
    
    @Override
    public int getOrder() {
        return 9996;
    }

    @Override
    public Object[] getViewMenuStruct() {
        return null;
    }

    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }

    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }

    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }


    
    // ---------------------------------------------------- TopicMapListener ---
    

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        refresh();
    }

}
