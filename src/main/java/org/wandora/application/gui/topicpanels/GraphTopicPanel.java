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
 * GraphTopicPanel.java
 *
 * Created on 12.6.2007, 14:09
 *
 */

package org.wandora.application.gui.topicpanels;



import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;

import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleComponent;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.application.gui.topicpanels.graphpanel.AssociationEdge;
import org.wandora.application.gui.topicpanels.graphpanel.Edge;
import org.wandora.application.gui.topicpanels.graphpanel.FilterManagerPanel;
import org.wandora.application.gui.topicpanels.graphpanel.Node;
import org.wandora.application.gui.topicpanels.graphpanel.OccurrenceNode;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.TopicNode;
import org.wandora.application.gui.topicpanels.graphpanel.VEdge;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.graph.CenterCurrentTopic;
import org.wandora.application.tools.graph.ChangeCurvature;
import org.wandora.application.tools.graph.ChangeFramerate;
import org.wandora.application.tools.graph.ChangeNodeMass;
import org.wandora.application.tools.graph.ChangeScale;
import org.wandora.application.tools.graph.ChangeStiffness;
import org.wandora.application.tools.graph.SetMouseTool;
import org.wandora.application.tools.graph.ToggleProjectionSettings;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.Options;


/**
 *
 * @author olli, akivela
 */
public class GraphTopicPanel extends JPanel implements TopicPanel, Scrollable, SimpleComponent, Clipboardable, TopicMapListener, RefreshListener, ActionListener, ComponentListener {
    private static final long serialVersionUID = 1L;
    
    private String OPTIONS_PREFIX = "gui.graphTopicPanel.";
    private String OPTIONS_VIEW_PREFIX = OPTIONS_PREFIX + "view.";

    protected Wandora wandora = null;
    private TopicMapGraphPanel graphPanel;
    private HashMap<Integer,SimpleToggleButton> toolButtons;
    private Options localOptions = null;
    
    
    public GraphTopicPanel() {
    }
    
    
    @Override
    public void init() {
        this.wandora = Wandora.getWandora();
        JPanel leftPane=new JPanel();
        leftPane.setLayout(new BorderLayout());
        
        localOptions = wandora.getOptions();
        graphPanel = new TopicMapGraphPanel(wandora, new Options(localOptions)) {
            @Override
            public void setMouseTool(int tool) {
                GraphTopicPanel.this.mouseToolChanged(tool);
                super.setMouseTool(tool);
            }
        };
        graphPanel.addFocusListener(this);
        this.setFocusable(true);
        
        leftPane.add(graphPanel,BorderLayout.CENTER);
        JToolBar toolBar=new JToolBar(JToolBar.VERTICAL);
        // toolBar.setBorder(MetalBorders.ToolBarBorder());
        toolBar.setMargin(new Insets(0,0,0,0));
        leftPane.add(toolBar,BorderLayout.WEST);
        makeToolBar(toolBar);
        
        //mouseToolChanged(graphPanel.getMouseTool());
               
        this.setLayout(new BorderLayout());
        this.add(leftPane);
        
        FilterManagerPanel filterManager=new FilterManagerPanel(wandora);
        graphPanel.setFilterManagerPanel(filterManager);
    }
    
    
    
    
    public void mouseToolChanged(int tool){
        if(toolButtons!=null){
            SimpleToggleButton selected=toolButtons.get(tool);
            for(SimpleToggleButton b : toolButtons.values()){
                b.setSelected(false);
            }
            selected.setSelected(true);
        }
    }
    
    
    
    
    public TopicMapGraphPanel getGraphPanel(){
        return graphPanel;
    }
    
    
    private void makeToolBar(Container container){
        
        Object[] struct = new Object[] {
            new ButtonGroup(),
            new SimpleToggleButton("gui/icons/graphpanel/hand_on.png", "gui/icons/graphpanel/hand.png", true), new SetMouseTool(getGraphPanel(), TopicMapGraphPanel.TOOL_OPEN), KeyStroke.getKeyStroke(KeyEvent.VK_1, 0),
            new SimpleToggleButton("gui/icons/graphpanel/lasso_on.png", "gui/icons/graphpanel/lasso.png", true), new SetMouseTool(getGraphPanel(), TopicMapGraphPanel.TOOL_SELECT), KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),
            new SimpleToggleButton("gui/icons/graphpanel/pen_on.png", "gui/icons/graphpanel/pen.png", true), new SetMouseTool(getGraphPanel(), TopicMapGraphPanel.TOOL_ASSOCIATION), KeyStroke.getKeyStroke(KeyEvent.VK_3, 0),
            //new SimpleToggleButton("gui/icons/graphpanel/eraser_on.png", "gui/icons/graphpanel/eraser.png", true), new SetMouseTool(getGraphPanel(), TopicMapGraphPanel.TOOL_ERASER), KeyStroke.getKeyStroke(KeyEvent.VK_4, 0),
            "---",
            //new SimpleButton(UIBox.getIcon("gui/icons/graphpanel/filter_win.png")), new ToggleFilterWindow(),
            
            new SimpleToggleButton("gui/icons/graphpanel/change_scale_on.png", "gui/icons/graphpanel/change_scale.png"), new ChangeScale(getGraphPanel()),
            new SimpleToggleButton("gui/icons/graphpanel/change_curvature_on.png", "gui/icons/graphpanel/change_curvature.png"), new ChangeCurvature(getGraphPanel()),
            new SimpleToggleButton("gui/icons/graphpanel/change_framerate_on.png", "gui/icons/graphpanel/change_framerate.png"), new ChangeFramerate(getGraphPanel()),
            new SimpleToggleButton("gui/icons/graphpanel/change_mass_on.png", "gui/icons/graphpanel/change_mass.png"), new ChangeNodeMass(getGraphPanel()),
            new SimpleToggleButton("gui/icons/graphpanel/change_stiffness_on.png", "gui/icons/graphpanel/change_stiffness.png"), new ChangeStiffness(getGraphPanel()),
            "---",
            new SimpleButton(UIBox.getIcon("gui/icons/graphpanel/change_view.png")), new ToggleProjectionSettings(getGraphPanel()),
            new SimpleButton(UIBox.getIcon("gui/icons/graphpanel/center_view.png")), new CenterCurrentTopic(getGraphPanel()),
        };
        
        toolButtons=new HashMap<Integer,SimpleToggleButton>();
        SimpleToggleButton lastButton=null;
        for(int i=0;i<struct.length;i++){
            if(struct[i] instanceof SimpleToggleButton){
                lastButton=(SimpleToggleButton)struct[i];
            }
            if(struct[i] instanceof SetMouseTool){
                if(lastButton!=null){
                    int tool=((SetMouseTool)struct[i]).getMouseTool();
                    toolButtons.put(tool,lastButton);
                }
            }
        }
        
        UIBox.fillGraphToolBar(container, struct, wandora);
     
        if(graphPanel!=null) mouseToolChanged(graphPanel.getMouseTool());
    }
    

    
    @Override
    public void refresh() throws TopicMapException {
        graphPanel.refreshGraph();
    }

    
    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }
    


    public Collection<Association> getContextAssociations() {
        Collection<VEdge> edges=graphPanel.getSelectedEdges();
        Collection<Association> ret = new ArrayList<Association>();
        if(edges != null) {
            VEdge vedge = null;
            Edge edge = null;
            Association a = null;
            
            for(Iterator<VEdge> iter = edges.iterator(); iter.hasNext();) {
                vedge = iter.next();
                if(vedge != null) {
                    edge = vedge.getEdge();
                    if(edge instanceof AssociationEdge) {
                        ret.add(((AssociationEdge) edge).getAssociation());
                    }
                }
            }
        }
        return ret;
    }
    
    
    public Collection<Topic> getContextTopics(){
        Collection<Topic> ret=graphPanel.getSelectedTopics();
        if(ret.isEmpty()) {
            ArrayList<Topic> al=new ArrayList<Topic>();
            al.add(graphPanel.getRootTopic());
            return al;
        }
        else return ret;
    }
    
    
    public Collection getContext(){
        Collection ret=graphPanel.getSelectedNodes();
        if(ret.isEmpty()) {
            ArrayList al=new ArrayList();
            al.add(graphPanel.getRootNode());
            return al;
        }
        else return ret;
    }
    
    
    @Override
    public Topic getTopic() throws TopicMapException {
        return graphPanel.getRootTopic();
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_graph.png");
    }

    @Override
    public String getName(){
        return "Graph";
    }
    
    @Override
    public String getTitle() {
        Topic t = graphPanel.getMouseOverTopic();
        if(t == null) t = graphPanel.getRootTopic();
        if(t != null) return TopicToString.toString(t);
        else return "";
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
    
    @Override
    public JPanel getGui() {
        return this;
    }

    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        return true;
    }

    @Override
    public boolean noScroll(){
        return false;
    }

    
    
    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }
    @Override
    public JMenu getViewMenu() {
        ArrayList menuStructure = new ArrayList();
        menuStructure.add("---");
        Object[] a = graphPanel.getOptionsMenuStruct();
        for(Object o : a) {
            menuStructure.add(o);
        }
        return UIBox.makeMenu(menuStructure.toArray(new Object[] {}), this);
    }

    @Override
    public Object[] getViewMenuStruct() {
        return graphPanel.getOptionsMenuStruct();
    }
    
    
    @Override
    public void stop() {
        // NOTHING TO STOP!
    }
    
    
    @Override
    public boolean supportsOpenTopic() {
        return true;
    }
    
    
    
    @Override
    public void open(Topic topic) throws TopicMapException {
        graphPanel.setRootTopic(topic);
    }

    
    public void toggleVisibility(String componentName) {
    }

    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        System.out.println("GraphTopicPanel catched action command '" + c + "'.");
        toggleVisibility(c);       
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- awt stuff ---
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(wandora == null) {
            wandora = Wandora.getWandora(this);
        }
        if(wandora != null) {
            wandora.gainFocus(this);
        }
    }
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------- Topic map listener ----
    // -------------------------------------------------------------------------
    
    

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        graphPanel.topicTypeChanged(t,added,removed);
    }

    @Override
    public void doRefresh() throws TopicMapException {
        graphPanel.doRefresh();
    }

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        graphPanel.topicSubjectIdentifierChanged(t,added,removed);
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        graphPanel.topicSubjectLocatorChanged(t,newLocator,oldLocator);
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        graphPanel.associationChanged(a);
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        graphPanel.associationRemoved(a);
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        graphPanel.topicBaseNameChanged(t,newName,oldName);
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        graphPanel.topicDataChanged(t,type,version,newValue,oldValue);
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        graphPanel.topicVariantChanged(t,scope,newName,oldName);
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        graphPanel.associationPlayerChanged(a,role,newPlayer,oldPlayer);
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        graphPanel.associationTypeChanged(a,newType,oldType);
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        graphPanel.topicChanged(t);
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        graphPanel.topicRemoved(t);
    }
    
    
    // ------------------------------------------------------- CLIPBOARDABLE ---
    
    
    @Override
    public void copy() {
        VModel model = graphPanel.getModel();
        StringBuilder sb = new StringBuilder("");
        if(model != null) {
            Topic t = null;
            Iterator<VNode> nodeIterator = null;
            
            Set<VNode> copyNodes = model.getSelectedNodes();
            if(copyNodes != null && !copyNodes.isEmpty()) {
                nodeIterator = copyNodes.iterator();
            }
            else {
                nodeIterator = model.getNodes().iterator();
            }
            VNode vnode = null;
            while( nodeIterator.hasNext() ) {
                vnode = nodeIterator.next();
                if(vnode != null) {
                    Node n = vnode.getNode();
                    if(n != null) {
                        if(n instanceof TopicNode) {
                            t = ((TopicNode) n).getTopic();
                            if(t != null) {
                                try {
                                    sb.append(TopicToString.toString(t)).append("\n");
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if(n instanceof OccurrenceNode) {
                            try {
                                sb.append(((OccurrenceNode) n).getOccurrence()).append("\n");
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        ClipboardBox.setClipboard(sb.toString());
    }
    
    
    @Override
    public void cut() {
        copy();
    }
    
    
    
    private boolean autoCreateTopicsInPaste = false;
    
    @Override
    public void paste() {
        Topic originalRootTopic = graphPanel.getRootTopic();
        String tabText = ClipboardBox.getClipboard();
        StringTokenizer tabLines = new StringTokenizer(tabText, "\n");
        autoCreateTopicsInPaste = false;
        while(tabLines.hasMoreTokens()) {
            String tabLine = tabLines.nextToken();
            StringTokenizer topicIdentifiers = new StringTokenizer(tabLine, "\t");
            try {
                String topicIdentifier = null;
                while(topicIdentifiers.hasMoreTokens()) {
                    topicIdentifier = topicIdentifiers.nextToken();
                    if(topicIdentifier != null && topicIdentifier.length() > 0) {
                        Topic pastedTopic = getTopicForIdentifier(topicIdentifier);
                        if(pastedTopic == null) {
                            boolean createTopicInPaste = false;
                            if(!autoCreateTopicsInPaste) {
                                int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Can't find a topic for identifier '"+topicIdentifier+"'. Would you like to create a topic for '"+topicIdentifier+"'?", "Create new topic?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                if(a == WandoraOptionPane.YES_OPTION) {
                                    createTopicInPaste = true;
                                }
                                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                                    autoCreateTopicsInPaste = true;
                                }
                                else if(a == WandoraOptionPane.CANCEL_OPTION) {
                                    graphPanel.setRootTopic(originalRootTopic);
                                    return;
                                }
                            }
                            if(autoCreateTopicsInPaste || createTopicInPaste) {
                                TopicMap tm = Wandora.getWandora().getTopicMap();
                                if(tm != null) {
                                    boolean identifierIsURL = false;
                                    try {
                                        URL u = new URL(topicIdentifier);
                                        identifierIsURL = true;
                                    }
                                    catch(Exception e) {}
                                    pastedTopic = tm.createTopic();
                                    if(identifierIsURL) {
                                        pastedTopic.addSubjectIdentifier(new Locator(topicIdentifier));
                                    }
                                    else {
                                        pastedTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                                        pastedTopic.setBaseName(topicIdentifier);
                                    }
                                    // pastedTopic.addType(originalRootTopic);
                                }
                            }
                        }
                        if(pastedTopic != null) {
                            graphPanel.setRootTopic(pastedTopic);
                        }
                    }
                }
            }
            catch(Exception e) {
                
            }
        }
        graphPanel.setRootTopic(originalRootTopic);
    }
    
    

    protected Topic getTopicForIdentifier(String id) {
        TopicMap tm = wandora.getTopicMap();
        Topic t = null;
        try {
            t = tm.getTopicWithBaseName(id);
            if(t == null) {
                t = tm.getTopic(id);
                if(t == null) {
                    t = tm.getTopicBySubjectLocator(new Locator(id));
                }
            }
        }
        catch(Exception e) {
            
        }
        return t;
    }
    

    
    /*
     * This is the old and deprecated paste method.
     */
    public void old_paste() {
        boolean requiresRefresh = false;
        TopicMap topicMap = Wandora.getWandora().getTopicMap();
        if(topicMap != null) {
            String clipboardText = ClipboardBox.getClipboard();
            if(clipboardText != null && clipboardText.length() > 0) {
                String[] identifiers = clipboardText.split("\n");
                if(identifiers != null && identifiers.length > 0) {
                    for(int i=0; i<identifiers.length; i++) {
                        String identifier = identifiers[i];
                        if(identifier != null && identifier.length() > 0) {
                            try {
                                Topic t = topicMap.getTopic(identifier);
                                if(t == null) t = topicMap.getTopicWithBaseName(identifier);
                                if(t == null) t = topicMap.getTopicBySubjectLocator(identifier);
                                if(t == null) {
                                    identifier = identifier.trim();
                                    t = topicMap.getTopic(identifier);
                                    if(t == null) t = topicMap.getTopicWithBaseName(identifier);
                                    if(t == null) t = topicMap.getTopicBySubjectLocator(identifier);
                                }
                                if(t != null) {
                                    if(t != null && !t.isRemoved()) {
                                        graphPanel.setRootTopic(t);
                                        requiresRefresh = true;
                                    }
                                }
                            }
                            catch(Exception e) {
                                
                            }
                        }
                    }
                }
            }
        }
        if(requiresRefresh) {
            try {
                Wandora.getWandora().doRefresh();
            }
            catch(Exception e) {}
        }
    }

    
    // -------------------------------------------------- Component Listener ---
    
    
    
    
    @Override
    public void componentResized(ComponentEvent e) {
        if(graphPanel != null) graphPanel.setSize(getWidth(), getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if(graphPanel != null) graphPanel.setSize(getWidth(), getHeight());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if(graphPanel != null) graphPanel.setSize(getWidth(), getHeight());
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        if(graphPanel != null) graphPanel.setSize(getWidth(), getHeight());
    }
    
    

    
}
