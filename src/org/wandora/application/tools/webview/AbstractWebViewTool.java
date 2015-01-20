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


package org.wandora.application.tools.webview;

import javafx.scene.web.WebEngine;
import javax.swing.Icon;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.WebViewTopicPanel;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;

/**
 *
 * @author akivela
 */


public abstract class AbstractWebViewTool extends AbstractWandoraTool {

    
    public AbstractWebViewTool() {
        
    }
    
    
    @Override
    public String getName() {
        return "Abstract web view tool";
    }
    
    @Override
    public String getDescription() {
        return "Abstract web view tool is used as a basis of various WebViewPanel tools.";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_webview.png");
    }
    
    
    
    
    
    // -------------------------------------------------------- WebViewPanel ---
    
    protected WebViewPanel getWebViewPanel(Context context) {
        if(context != null) {
            Object source = context.getContextSource();
            if(source != null && source instanceof WebViewPanel) {
                return (WebViewPanel) source;
            }
            else {
                System.out.println("Invalid context source. Expecting WebViewPanel but found "+source.getClass());
            }
        }
        return null;
    }
    
    
    
    protected WebEngine getWebEngine(Context context) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            return webViewPanel.getWebEngine();
        }
        return null;
    }
    
    
    protected String getWebLocation(Context context) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            return webViewPanel.getWebLocation();
        }
        return null;
    }
    
    
    protected String getSource(Context context) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            return webViewPanel.getSource();
        }
        return null;
    }
    
    
    protected String getSelectedSource(Context context) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            return webViewPanel.getSelectedSource();
        }
        return null;
    }
    
    
    protected String getSelectedText(Context context) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            return webViewPanel.getSelectedText();
        }
        return null;
    }
    
    
    protected Topic getTopic(Context context) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            try {
                return webViewPanel.getTopic();
            }
            catch(Exception e) {
                // IGNORE SILENTLY
            }
        }
        return null;
    }
    
    
    
    protected Object executeJavascript(Context context, String script) {
        WebViewPanel webViewPanel = getWebViewPanel(context);
        if(webViewPanel != null) {
            return webViewPanel.executeSynchronizedScript(script);
        }
        return null;
    }
}
