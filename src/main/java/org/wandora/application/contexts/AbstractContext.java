package org.wandora.application.contexts;

import java.awt.event.ActionEvent;

import javax.swing.table.JTableHeader;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.LayerTree;
import org.wandora.application.gui.OccurrenceTable;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.TopicLinkBasename;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import org.wandora.application.gui.topicpanels.GraphTopicPanel;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.gui.tree.TopicTreePanel;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.layered.Layer;

public class AbstractContext {

    private Object contextSource;
    private WandoraTool contextOwner = null;
    private ActionEvent actionEvent = null;
    private Wandora wandora = null;
    
    

    public void initialize(Wandora wandora, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.wandora = wandora;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
        
        Object proposedContextSource = UIBox.getActionsRealSource(actionEvent);
        if( !isContextSource(proposedContextSource) ) {
            proposedContextSource = wandora.getFocusOwner();
            if( !isContextSource(proposedContextSource) ) {
                proposedContextSource = wandora;
            }
        }
        
        // *** IF CONTEXT WAS WANDORA THEN TRY TO SOLVE WANDORA'S FOCUS OWNER ***
        else {
            if( proposedContextSource instanceof Wandora ) {
                Object wandoraRegisteredContext = ((Wandora) proposedContextSource).getFocusOwner();
                if( isContextSource(wandoraRegisteredContext) ) {
                    proposedContextSource = wandoraRegisteredContext;
                }
            }
        }
        
        setContextSource( proposedContextSource );
    }
    
	


    public void setContextSource(Object proposedContextSource) {
        if(isContextSource(proposedContextSource)) {
            contextSource = proposedContextSource;
        }
        else {
            contextSource = null;
        }
    }
    
    

    
    
    public Object getContextSource() {
        return contextSource;
    }
    
    
    
    
    public boolean isContextSource(Object contextSource) {
        if(contextSource != null && (
                contextSource instanceof Wandora ||
                contextSource instanceof TopicLinkBasename ||
                contextSource instanceof Topic ||
                contextSource instanceof Topic[] ||
                contextSource instanceof GraphTopicPanel ||
                contextSource instanceof WebViewPanel ||
                contextSource instanceof TopicTable ||
                contextSource instanceof TopicGrid ||
                contextSource instanceof MixedTopicTable ||
                contextSource instanceof OccurrenceTable ||
                contextSource instanceof OccurrenceTextEditor ||
                contextSource instanceof TopicTreePanel ||
                contextSource instanceof TopicTree ||
                contextSource instanceof SITable ||
                contextSource instanceof LayerTree ||
                contextSource instanceof Layer ||
                contextSource instanceof JTableHeader && ((JTableHeader) contextSource).getTable() instanceof TopicTable)) {
                    return true;
        }
        return false;
    }
    
    
    
    
    public WandoraTool getContextOwner() {
    	return this.contextOwner;
    }
    
    
    
    public ActionEvent getContextEvent() {
    	return this.actionEvent;
    }
    
    
    
    public Wandora getWandora() {
    	return this.wandora;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void log(Exception e) {
        if(contextOwner != null) contextOwner.log(e);
        else e.printStackTrace();
    }
}
