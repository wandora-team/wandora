/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * TopicTree.java
 *
 * Created on 27. joulukuuta 2005, 23:26
 *
 */

package org.wandora.application.gui.tree;


import java.io.IOException;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import static org.wandora.utils.Tuples.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import java.net.URL;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicstringify.TopicToString;



/**
 * TopicTree implements <code>JTree</code> type structure for topics.
 * Each tree node represents single topic. Node's children depend
 * on associations and instance-of relations of node topic. Associations
 * represented as a parent-child relation are user configurable. Also
 * TopicTree's root node is user configurable. 
 *
 * @author olli, akivela
 */


public class TopicTree extends SimpleTree implements Clipboardable, MouseListener, TopicMapListener /*, DragSourceListener, DragGestureListener*/ {
   

    
    private boolean needsRefresh;
    private ArrayList<TopicTreeRelation> selectedAs;
    private HashSet<Locator> selectedAsLocators;
    protected String rootTopicSI;
    protected Topic rootTopic;
    protected TopicTreeModel model;
    protected Wandora wandora;
    private MouseEvent mouseEvent;
    
    private TopicTreeRelation[] associations;
        
    private boolean openWithDoubleClick;
    
    private TopicTreePanel chooser;
    
    
    
    
    /** Creates a new instance of TopicTree */
    public TopicTree(String rootTopicSI, Wandora wandora) throws TopicMapException {
        this(rootTopicSI, wandora, null);
    }
    
    public TopicTree(String rootTopicSI, Wandora wandora, TopicTreePanel chooser) throws TopicMapException {
        this(rootTopicSI,wandora,GripCollections.newHashSet("Instances","Subclasses"),new TopicTreeRelation[]{
                new TopicTreeRelation("Instances","","","","gui/icons/topictree/instanceof.png"),
                new TopicTreeRelation("Subclasses",XTMPSI.SUBCLASS,XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUPERCLASS,"gui/icons/topictree/supersubclass.png")
        },chooser);
    }
    
    public TopicTree(String rootTopicSI, Wandora wandora, Set<String> selectedAssociations, TopicTreeRelation[] associations) throws TopicMapException {
        this(rootTopicSI,wandora,selectedAssociations,associations,null);
    }
    
    public TopicTree(String rootTopicSI, Wandora wandora, Set<String> selectedAssociations, TopicTreeRelation[] associations,TopicTreePanel chooser) throws TopicMapException {
        this.rootTopicSI = rootTopicSI;
        this.wandora = wandora;
        this.associations = associations;
        this.chooser = chooser;
        
        this.openWithDoubleClick = true;
        setToggleClickCount(4); 
        
        //initMenuStructure();
        
        // this.model = new TopicTreeModel(rootTopic,associations);
        // setModel(model);
        updateModel(rootTopicSI,selectedAssociations,associations);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        TopicTreeTopicRenderer topicTreeCellRenderer = new TopicTreeTopicRenderer(this);
        setCellRenderer(topicTreeCellRenderer);
        
        TopicTreeTopicEditor topicEditor = new TopicTreeTopicEditor(wandora, this, new SimpleField(), topicTreeCellRenderer);
        setCellEditor(topicEditor);  
        
        this.setDragEnabled(true);
        this.setDropMode(DropMode.ON);
        this.setTransferHandler(new TopicTreeTransferHandler());
        
        setExpandsSelectedPaths(true);
        this.addMouseListener(this);
        
        setEditable(true);
        
        this.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener(){
            @Override
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent event){
                refreshSize();
            }
            @Override
            public void treeExpanded(javax.swing.event.TreeExpansionEvent event){
                refreshSize();                
            }
        });
    }



    private <T> boolean collectionsOverlap(Collection<T> a,Collection<T> b){
        if(a.size()>b.size()){
            Collection<T> c=a;
            a=b;
            b=c;
        }
        for(T o : a){
            if(b.contains(o)) return true;
        }
        return false;
    }
    
    public boolean isBroken(){
        return model==null;
    }
    
    public void setNeedsRefresh(boolean value){
        needsRefresh=value;
    }
    
    public boolean getNeedsRefresh(){
        return needsRefresh;
    }
    
    
    
    // ---------------------------------------------------- TopicMapListener ---
    
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        if(needsRefresh) return;
        if( (added!=null && rootTopicSI != null && rootTopicSI.equals(added.toExternalForm())) ||
            (removed!=null && rootTopicSI.equals(removed.toExternalForm())) ){
            setNeedsRefresh(true);
        }
        if(model==null) return;
        if(t == null) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),model.getVisibleTopics()) ||
           (added!=null && model.getVisibleTopics().contains(added)) ||
           (removed!=null && model.getVisibleTopics().contains(removed)) ) setNeedsRefresh(true);        
    }
    
    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(t == null) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
    }
    
    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(added!=null && collectionsOverlap(added.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
        else if(removed!=null && collectionsOverlap(removed.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
    }
    
    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(t == null) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
    }
    
    @Override
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
    }
    
    @Override
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
    }
    
    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(t == null) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
    }
    
    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(t == null) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);        
    }
    
    @Override
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if( (oldType!=null && collectionsOverlap(oldType.getSubjectIdentifiers(),model.getVisibleTopics())) || 
            (newType!=null && collectionsOverlap(newType.getSubjectIdentifiers(),model.getVisibleTopics())) ){
            for(Topic r : a.getRoles()){
                if(collectionsOverlap(a.getPlayer(r).getSubjectIdentifiers(),model.getVisibleTopics())){
                    setNeedsRefresh(true);
                    break;
                }
            }
        }
    }
    
    @Override
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(a == null) return;
        boolean cont=false;
        for(int i=0;i<associations.length;i++) {
            if(associations[i].assocSI != null) {
                Locator l=a.getTopicMap().createLocator(associations[i].assocSI);
                if(a.getType().getSubjectIdentifiers().contains(l)){
                    cont=true;
                    break;
                }
            }
        }
        if(!cont) return;
        if(newPlayer!=null && collectionsOverlap(newPlayer.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
        else if(newPlayer!=null && collectionsOverlap(newPlayer.getSubjectIdentifiers(),model.getVisibleTopics())) setNeedsRefresh(true);
    }
    
    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(needsRefresh || model==null) return;
        if(a == null) return;
        boolean cont=false;
        for(int i=0;i<associations.length;i++) {
            if(associations[i].assocSI != null) {
                Locator l=a.getTopicMap().createLocator(associations[i].assocSI);
                if(a.getType().getSubjectIdentifiers().contains(l)){
                    cont=true;
                    break;
                }
            }
        }
        if(!cont) return;
        for(Topic r : a.getRoles()) {
            if(collectionsOverlap(a.getPlayer(r).getSubjectIdentifiers(),model.getVisibleTopics())) {
                setNeedsRefresh(true);
                break;
            }
        }
    }
    
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        needsRefresh=true;
    }
    
    
    // --------------------------------------------------- /TopicMapListener ---
    
    
    public void setOpenWithDoubleClick(boolean value){
        openWithDoubleClick=value;
    }
    
    public void refreshSize(){
        int maxx=0;
        int maxy=0;
        for(int i=0;i<getRowCount();i++){
            java.awt.Rectangle rect=getRowBounds(i);
            if(rect==null) continue;
            if(rect.getMaxX()>maxx) maxx=(int)rect.getMaxX();
            if(rect.getMaxY()>maxy) maxy=(int)rect.getMaxY();
        }
        this.setPreferredSize(new java.awt.Dimension(maxx,maxy));
        this.setMinimumSize(new java.awt.Dimension(maxx,maxy));
        this.setMaximumSize(new java.awt.Dimension(maxx,maxy));
        getParent().invalidate();
        getParent().getParent().validate();
    }
    
    private void recreateModel() throws TopicMapException {
        TopicMap topicMap = wandora.getTopicMap();
        if(rootTopicSI != null) {
            rootTopic=topicMap.getTopic(rootTopicSI);
            if(rootTopic==null){
                System.out.println("Topic tree root topic not found ("+rootTopicSI+")");
    /*            System.out.println("Root topic not found in topic map. Trying default root '"+WandoraManager.WANDORACLASS_SI+"'.");
                rootTopic=topicMap.getTopic(WandoraManager.WANDORACLASS_SI);
                if(rootTopic == null) {
                    System.out.println("Default root not found. Trying XTM base '"+XTMPSI.XTM1_BASE+"'.");
                    rootTopic=topicMap.getTopic(XTMPSI.XTM1_BASE);
                    if(rootTopic == null) {
                        // TODO: WHAT NOW! THERE IS NO SUFFICIENT ROOT FOR THE TREE!!!
                    }
                }*/
            }
        }
        if(rootTopic!=null){
            TopicTreeModel newModel=new TopicTreeModel(rootTopic,selectedAs,this);
            this.model=newModel;
            // System.out.println("Resetting tree model "+selectedAs.size());
            setModel(newModel); 
            needsRefresh=false;
            if(chooser!=null) chooser.setTreeEnabled(true);
        }
        else{
            System.out.println("Root topic not found for topic tree. Disabling tree.");
            if(chooser!=null) chooser.setTreeEnabled(false);
            this.model=null;
            // setModel(null);
            needsRefresh=false;
        }
    }
    
    /**
     * @param rootSI Subject identifier of the topic that is used as root for the tree
     * @param selectedAssociations Names of the associations in associations array that
     *        are used in this topic tree chooser.
     * @param associations A list of tree association types. Not all of them are
     *        necessarily used in this topic tree chooser. selectedAssociations
     *        contains the names of the used association types.
     */
    public void updateModel(String rootSI, Set<String> selectedAssociations, TopicTreeRelation[] associations ) throws TopicMapException{
        
        selectedAs=new ArrayList<TopicTreeRelation>();
        selectedAsLocators=new HashSet<Locator>();
        
        for(String selected : selectedAssociations) {
            for(int i=0;i<associations.length;i++) {
                if(associations[i].name.equals(selected)) {
                    selectedAs.add(associations[i]);
                    if(associations[i].assocSI != null && associations[i].assocSI.length() != 0) {
                        selectedAsLocators.add(new Locator(associations[i].assocSI));
                    }
                    break;
                }
            }
        }
        this.rootTopicSI=rootSI;
        recreateModel();
        this.revalidate();
        this.repaint();
    }


    
    public boolean selectTopic(Topic t) {
        TreePath path = model.getPathFor(t);
        if(path != null) {  
            // System.out.println(path.toString());    // Able to get the exact node here    
            setExpandsSelectedPaths(true);                  
            setSelectionPath(path);
            scrollPathToVisible(path);
            requestFocus();
            return true;
        }
        return false;
    }


    
    public Topic getSelection() {
        TreePath path = getSelectionPath();
        if(path != null) {
            return ((TopicGuiWrapper)path.getLastPathComponent()).topic;
        }
        else {
            if(mouseEvent != null) return getTopicAt(mouseEvent);
            else return null;
        }
    }

    
    
    public void update() {
    }

    
    public ArrayList<T2<Locator,String>> getLocatorPath(TreePath tp) throws TopicMapException {
        Object[] path=tp.getPath();
        ArrayList<T2<Locator,String>> lpath=new ArrayList<T2<Locator,String>>();
        for(int i=0;i<path.length;i++){
            TopicGuiWrapper wrapper=((TopicGuiWrapper)path[i]);
            lpath.add(t2(wrapper.topic.getOneSubjectIdentifier(),wrapper.associationType));
        }
        return lpath;
    }
    
    public TreePath getTreePath(ArrayList<T2<Locator,String>> path) throws TopicMapException {
        if(model==null) return null;
        Object parent=model.getRootNode();
        TreePath treePath=new TreePath(parent);
        ILoop: for(int i=1;i<path.size();i++){
            int count=model.getChildCount(parent);
            T2<Locator,String> p=path.get(i);
            for(int j=0;j<count;j++){
                TopicGuiWrapper child=(TopicGuiWrapper)model.getChild(parent,j);
                if(child.topic.getSubjectIdentifiers().contains(p.e1) && child.associationType.equals(p.e2)){
                    parent=child;
                    treePath=treePath.pathByAddingChild(child);
                    continue ILoop;
                }
            }
            return null;
        }
        return treePath;
    }
    
    public ArrayList<ArrayList<T2<Locator,String>>> getAllExpandedPaths(TreePath root) throws TopicMapException {
        ArrayList<ArrayList<T2<Locator,String>>> expandedPaths=new ArrayList<ArrayList<T2<Locator,String>>>();
        Enumeration<TreePath> e=this.getExpandedDescendants(root);
        if(e==null) return expandedPaths;
        while(e.hasMoreElements()){
            TreePath tp=e.nextElement();
            expandedPaths.add(getLocatorPath(tp));
            expandedPaths.addAll(getAllExpandedPaths(tp));
        }
        return expandedPaths;
    }
    
    public void expandPath(ArrayList<T2<Locator,String>> path) throws TopicMapException {
        TreePath treePath=getTreePath(path);
        if(treePath!=null) expandPath(treePath);
    }
    
    public synchronized void refresh() throws TopicMapException {
        ArrayList<ArrayList<T2<Locator,String>>> expandedPaths=null;
        if(model!=null) expandedPaths=getAllExpandedPaths(getPathForRow(0));        
        ArrayList<T2<Locator,String>> selected=null;
        ArrayList<T2<Locator,String>> scrollPath=null;
        JViewport vp=org.wandora.utils.swing.GuiTools.getViewport(this);
        if(vp!=null && model!=null) {
            int viewPos=(int)vp.getViewPosition().getY();
            TreePath path=getClosestPathForLocation(0,viewPos);
            scrollPath=getLocatorPath(path);
        }
        if(model!=null && getSelectionPath()!=null) {
            selected=getLocatorPath(getSelectionPath());            
        }
//        if(model != null) model.childrenModified(model.getRootNode());
        recreateModel();
        
        if(expandedPaths!=null){
            for(ArrayList<T2<Locator,String>> tp : expandedPaths){
                TreePath path=getTreePath(tp);
                if(path==null) continue;
                expandPath(path);
                model.waitExpansionDone((TopicGuiWrapper)path.getLastPathComponent());
            }
        }        
        if(model!=null && selected!=null) setSelectionPath(getTreePath(selected));
        if(model!=null && scrollPath!=null){
            TreePath tp = getTreePath(scrollPath);
            vp.revalidate();
            if(tp != null) {
                Rectangle rect=getPathBounds(tp);
                if(rect != null) {
                    vp.setViewPosition(new Point(0,rect.y));
                }
            }
        }
        
        /*
        TreeSelectionModel sm=getSelectionModel();
        TreePath path=getSelectionPath();
        try {
            this.model = new TopicTreeModel(this.rootTopic);
            setModel(this.model);
            expandPath(path);
            setSelectionPath(path);
            //this.setSelectionModel(sm);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
         **/
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void cut() {
        copy();
    }
    
    
    
    
    private boolean autoCreateTopicsInPaste = false;
    
    @Override
    public void paste() {
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
                                }
                            }
                        }
                        if(pastedTopic != null) {
                            Topic t = getSelection();
                            if(t != null) {
                                pastedTopic.addType(t);
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                
            }
        }
    }
    
    
    @Override
    public void copy() {
        ClipboardBox.setClipboard(getCopyString());
    }
    
    public String getCopyString() {
        StringBuilder sb = new StringBuilder("");
        Topic selectedTopic = getSelection();
        if(selectedTopic != null) {
            sb.append(TopicToString.toString(selectedTopic));
        }
        else {}
        return sb.toString();
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
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
 


    protected void handlePopupMouseEvent(java.awt.event.MouseEvent e) {
        try {
            if(e.isPopupTrigger()) {
                JPopupMenu popupMenu = UIBox.makePopupMenu(WandoraMenuManager.getTreeMenu(wandora, this), wandora);
                setSelectionRow(getClosestRowForLocation(e.getX(),e.getY()));
                popupMenu.show(e.getComponent(),e.getX(),e.getY());
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    
        
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent != null) {
            try {
                this.mouseEvent = mouseEvent;
                if(openWithDoubleClick && mouseEvent.getClickCount() == 2) {
                    if(getSelection() != null && wandora != null) {
                        wandora.applyChangesAndOpen(getSelection());
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            handlePopupMouseEvent(mouseEvent);
        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        handlePopupMouseEvent(mouseEvent);
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        handlePopupMouseEvent(mouseEvent);
    }
    
    
     public Object getValueAt(MouseEvent e) {
        return getTopicAt(e.getX(), e.getY());
    }
     
    public Object getValueAt(Point p) {
        return getValueAt(p.x, p.y);
    }
    
    public Object getValueAt(int x, int y) {
        try {
            int selRow = getRowForLocation(x, y);
            TreePath selPath = getPathForLocation(x, y);
            if(selRow != -1) {
                return (TopicGuiWrapper) selPath.getLastPathComponent();
            }
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
        return null;
    }
    
    
    
    public Topic getTopicAt(MouseEvent e) {
        return getTopicAt(e.getX(), e.getY());
    }
    
    public Topic getTopicAt(Point point) {
        return getTopicAt(point.x, point.y);
    }
    
    public Topic getTopicAt(int x, int y) {
        Object object = getValueAt(x, y);
        if(object != null) {
            if(object instanceof TopicGuiWrapper) {
                TopicGuiWrapper wrapper = (TopicGuiWrapper) object;
                return wrapper.topic;
            }
        }
        return null;
    }
    

    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- DND ---
    // -------------------------------------------------------------------------
    
    
    
    
    public static class TopicTreeTransferable extends DnDHelper.WandoraTransferable {
        public static final DataFlavor treeDataFlavor=new DataFlavor(TopicGuiWrapper.class,"TopicTree node");
        
        protected TopicGuiWrapper wrapper;
        
        public TopicTreeTransferable(TopicGuiWrapper wrapper){
            this.wrapper=wrapper;
            if(this.wrapper!=null) setTopic(this.wrapper.topic);
        }
        
        @Override
        public void updateFlavors(){
            super.updateFlavors();
            if(this.wrapper==null) return;
            DataFlavor[] old=supportedFlavors;
            supportedFlavors=new DataFlavor[old.length+1];
            supportedFlavors[0]=treeDataFlavor;
            System.arraycopy(old, 0, supportedFlavors, 1, old.length);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(wrapper!=null && flavor.equals(treeDataFlavor)) return wrapper;
            else return super.getTransferData(flavor);
        }
    }
    
    private class TopicTreeTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(TopicTreeTransferable.treeDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            TreePath path=getSelectionPath();
            if(path==null) return new TopicTreeTransferable(null);
            else return new TopicTreeTransferable((TopicGuiWrapper)path.getLastPathComponent());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try{
                if(support.isDataFlavorSupported(TopicTreeTransferable.treeDataFlavor)) {
                    TopicGuiWrapper node=(TopicGuiWrapper)support.getTransferable().getTransferData(TopicTreeTransferable.treeDataFlavor);
                    if(node.path.getPathCount()<2) {
                        WandoraOptionPane.showMessageDialog(TopicTree.this.wandora,"Can't move root, drop cancelled.", "Drop cancelled");
                        return false;
                    }
                    TopicGuiWrapper parent=(TopicGuiWrapper)node.path.getParentPath().getLastPathComponent();
                    int action=support.getDropAction();

                    String typeName=node.associationType;
                    TopicTreeRelation type=null; 
                    for(int i=0;i<associations.length;i++){
                        if(associations[i].name.equals(typeName)){
                            type=associations[i];
                            break;
                        }
                    }
                    if(type==null){
                        WandoraOptionPane.showMessageDialog(TopicTree.this.wandora,"Couldn't find tree association type, drop cancelled.", "Drop cancelled");
                        return false;
                    }

                    JTree.DropLocation location=(JTree.DropLocation)support.getDropLocation();
                    TreePath dropPath=location.getPath();
                    if(dropPath==null){
                        WandoraOptionPane.showMessageDialog(TopicTree.this.wandora,"Invalid drop location, drop cancelled.", "Drop cancelled");
                        return false;
                    }
                    TopicGuiWrapper locationNode=(TopicGuiWrapper)dropPath.getLastPathComponent();
                    try{
                        if(parent.topic.mergesWithTopic(locationNode.topic)){
                            WandoraOptionPane.showMessageDialog(TopicTree.this.wandora,
                                    "Drop location is same as current parent, drop cancelled.", "Drop cancelled");
                            return false;
                        }
                        if(action==TransferHandler.MOVE && locationNode.topic.mergesWithTopic(node.topic)){
                            int c=WandoraOptionPane.showConfirmDialog(TopicTree.this.wandora,
                                    "Are you sure you want to move the item on itself?");
                            if(c!=WandoraOptionPane.YES_OPTION) return false;
                        }
                        if(type.name.equals("Instances")){
                            if(action==TransferHandler.MOVE){
                                node.topic.removeType(parent.topic);
                            }
                            node.topic.addType(locationNode.topic);
                        }
                        else{
                            TopicMap tm=TopicTree.this.wandora.getTopicMap();
                            Topic atype=tm.getTopic(type.assocSI);
                            Topic superTopic=tm.getTopic(type.superSI);
                            Topic subTopic=tm.getTopic(type.subSI);
                            if(atype==null || superTopic==null || subTopic==null){
                                WandoraOptionPane.showMessageDialog(TopicTree.this.wandora,"Couldn't find tree association topics, drop cancelled.");
                                return false;
                            }
                            if(action==TransferHandler.MOVE){
                                Collection<Association> as=node.topic.getAssociations(atype,subTopic);
                                for(Association a : as){
                                    Topic p=a.getPlayer(superTopic);
                                    if(p==null) continue;
                                    if(p.mergesWithTopic(parent.topic)){
                                        a.remove();
                                        break;
                                    }
                                }
                            }
                            Association a=tm.createAssociation(atype);
                            a.addPlayer(locationNode.topic, superTopic);
                            a.addPlayer(node.topic,subTopic);
                        }
                    }
                    catch(TopicMapException tme){
                        tme.printStackTrace();
                        return false;
                    }
                    doRefresh();
                    return true;
                }
                else {
                    try{
                        TopicMap tm=TopicTree.this.wandora.getTopicMap();
                        ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                        if(topics==null) return false;
                        
                        JTree.DropLocation location=(JTree.DropLocation)support.getDropLocation();
                        TreePath dropPath=location.getPath();
                        if(dropPath==null){
                            WandoraOptionPane.showMessageDialog(TopicTree.this.wandora,"Invalid drop location, drop cancelled.");
                            return false;
                        }
                        TopicGuiWrapper locationNode=(TopicGuiWrapper)dropPath.getLastPathComponent();
                        for(Topic t :topics){
                            t.addType(locationNode.topic);
                        }
                        doRefresh();
                        return true;
                    }
                    catch(TopicMapException tme){
                        tme.printStackTrace();
                        return false;
                    }
                }
            }
            catch(UnsupportedFlavorException ufe){ufe.printStackTrace();}
            catch(IOException ioe){ioe.printStackTrace();}
            return false;
        }

        public void doRefresh(){
            TopicTree.this.wandora.doRefresh();
        }
    }
}
