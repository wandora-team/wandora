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
 */


package org.wandora.application.gui.topicpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import javafx.scene.web.WebEngine;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class WebViewTopicPanel implements TopicPanel {

    private Topic currentTopic = null;
    private WebViewPanel webPanel = null;
    
    
    public WebViewTopicPanel() {
        
    }
    
    
    @Override
    public void init() {
    }
    
    
    
    @Override
    public boolean supportsOpenTopic() {
        return true;
    }
    
    
    @Override
    public void open(Topic topic) throws TopicMapException {
        currentTopic = topic;
        if(webPanel != null) webPanel.open(topic);
    }

    @Override
    public void stop() {
        if(webPanel != null) webPanel.stop();
    }

    @Override
    public void refresh() throws TopicMapException {
        if(webPanel != null) webPanel.refresh();
    }

    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        if(webPanel != null) return webPanel.applyChanges();
        return false;
    }

    @Override
    public JPanel getGui() {
        if(webPanel == null) {
            try {
                Class.forName("javafx.embed.swing.JFXPanel");
                webPanel = new WebViewPanel();
                if(currentTopic != null) {
                    try {
                        webPanel.open(currentTopic);
                    }
                    catch(Exception e) {}
                }
            }
            catch(NoClassDefFoundError e) {
                return getErrorPanel();
            } 
            catch (ClassNotFoundException ex) {
                return getErrorPanel();
            }
        }
        return webPanel;
    }

    
    
    private JPanel getErrorPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        SimpleLabel sl = new SimpleLabel();
        sl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        sl.setBorder(new LineBorder(Color.BLACK, 1));
        sl.setText("Can't find JavaFX. Webview has been disabled.");
        p.add(sl, BorderLayout.CENTER);
        p.setBackground(Color.WHITE);
        return p;
    }
    
    
    @Override
    public Topic getTopic() throws TopicMapException {
        if(webPanel != null) return webPanel.getTopic();
        return currentTopic;
    }

    @Override
    public String getName() {
        return "Webview";
    }

    @Override
    public String getTitle() {
        if(webPanel != null) return webPanel.getTitle();
        else if(currentTopic == null) return getName();
        else return TopicToString.toString(currentTopic);
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_webview.png");
    }

    @Override
    public int getOrder() {
        return 2000;
    }

    @Override
    public Object[] getViewMenuStruct() {
        return null;
    }

    @Override
    public JMenu getViewMenu() {
        return null;
    }

    @Override
    public JPopupMenu getViewPopupMenu() {
        return null;
    }

    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }
    
    @Override
    public boolean noScroll(){
        return false;
    }
    
    // -------------------------------------------------------------------------
    
    
    public WebEngine getWebEngine() {
        if(webPanel != null) return webPanel.getWebEngine();
        return null;
    }
    
    public String getWebLocation() {
        if(webPanel != null) return webPanel.getWebLocation();
        return null;
    }
    
    public String getSource() {
        if(webPanel != null) return webPanel.getSource();
        return null;
    }
    
    public String getSelectedText() {
        if(webPanel != null) return webPanel.getSelectedText();
        return null;
    }
    
    public String getWebTitle() {
        if(webPanel != null) return webPanel.getWebTitle();
        return null;
    }
    
    public Object executeJavascript(String script) {
        if(webPanel != null) return webPanel.executeSynchronizedScript(script);
        return null;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        if(webPanel != null) webPanel.topicSubjectIdentifierChanged(t, added, removed);
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        if(webPanel != null) webPanel.topicBaseNameChanged(t, newName, oldName);
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        if(webPanel != null) webPanel.topicTypeChanged(t, added, removed);
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        if(webPanel != null) webPanel.topicVariantChanged(t, scope, newName, oldName);
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        if(webPanel != null) webPanel.topicDataChanged(t, type, version, newValue, oldValue);
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        if(webPanel != null) webPanel.topicSubjectLocatorChanged(t, newLocator, oldLocator);
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(webPanel != null) webPanel.topicRemoved(t);
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        if(webPanel != null) webPanel.topicChanged(t);
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        if(webPanel != null) webPanel.associationTypeChanged(a, newType, oldType);
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        if(webPanel != null) webPanel.associationPlayerChanged(a, role, newPlayer, oldPlayer);
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(webPanel != null) webPanel.associationRemoved(a);
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        if(webPanel != null) webPanel.associationChanged(a);
    }
    
}
