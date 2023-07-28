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
 * 
 *
 * DragJTree.java
 *
 * Created on 2.6.2005, 10:19
 *
 * Copyright 2004-2005 Grip Studios Interactive Oy (office@gripstudios.com)
 * Created by Olli Lyytinen, AKi Kivela
 */

package org.wandora.utils.swing;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
/**
 * A JTree that allows moving nodes in the tree with drag and drop. You will need to
 * implement allowDrop and doDrop. allowDrop checks if drop is allowed in the location
 * where user holds the mouse. doDrop is called when the user finishes the drag and
 * drop event. It must modify the model of the tree and notify the tree of changes in
 * the model.
 *
 * @author olli
 */
public abstract class DragJTree extends JTree implements Autoscroll {
    
    protected TreePath selectedTreePath = null;
    private Point offsetPoint = new Point();
    private BufferedImage ghostImage;
    private DragSource dragSource = null;
    private DragSourceContext dragSourceContext = null;

    private TreePath currentParent = null;
    private TreePath currentPosition = null;
    
    protected boolean enableDrag;
    
    protected boolean localDragging;
    
    /** Creates a new instance of DragJTree */
    public DragJTree() {
        super();
        initialize();
    }
    public DragJTree(Hashtable<?,?> value){
        super(value);
        initialize();
    }
    public DragJTree(Object[] value){
        super(value);
        initialize();
    }
    public DragJTree(TreeModel model){
        super(model);
        initialize();
    }
    public DragJTree(TreeNode root){
        super(root);
        initialize();
    }
    public DragJTree(TreeNode root,boolean asksAllowsChildren){
        super(root,asksAllowsChildren);
        initialize();
    }
    public DragJTree(Vector<?> value){
        super(value);
        initialize();
    }
    
    protected void initialize(){
        enableDrag=true;
        localDragging=false;
        
        dragSource = DragSource.getDefaultDragSource();
        
        final DragSourceListener dragSourceListener = new DragSourceListener(){
            public void dragDropEnd(DragSourceDropEvent dsde){
                localDragging=false;
            }
            
            public void dragEnter(DragSourceDragEvent dsde){
                
            }
            
            public void dragExit(DragSourceEvent dse){
                
            }
            
            public void dragOver(DragSourceDragEvent dsde){
                
            }            
            
            public void dropActionChanged(DragSourceDragEvent dsde){
                
            }
            
        };
        
        DragGestureListener dragGestureListener = new DragGestureListener(){
            public void dragGestureRecognized(DragGestureEvent dge) {
                if(!enableDrag) return;
                Point dragOrigin=dge.getDragOrigin();
                TreePath path=getPathForLocation(dragOrigin.x,dragOrigin.y);
                if(path!=null && !isRootPath(path)){
                    Rectangle pathRect=getPathBounds(path);
                    offsetPoint.setLocation(dragOrigin.x-pathRect.x,dragOrigin.y-pathRect.y);
                    
                    Component comp = getCellRenderer().getTreeCellRendererComponent(DragJTree.this,path.getLastPathComponent(),
                                                                                    false,isExpanded(path),
                                                                                    getModel().isLeaf(path.getLastPathComponent()), 
                                                                                    0,false);
                    comp.setSize((int)pathRect.getWidth(),(int)pathRect.getHeight());
                    ghostImage = new BufferedImage((int)pathRect.getWidth(),(int)pathRect.getHeight(),BufferedImage.TYPE_INT_ARGB_PRE);
                    Graphics2D g2 = ghostImage.createGraphics();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC,0.5f));
                    comp.paint(g2);
                    
                    g2.dispose();
                    
                    selectedTreePath=path;
                    setSelectionPath(path);
                    
                    currentParent=null;
                    currentPosition=null;
                    
                    Transferable transferable=getTransferable(path);
                    localDragging=true;
                    dge.startDrag(null, ghostImage, new Point(5,5), transferable, dragSourceListener);
                }
            }
        };
        
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE,  dragGestureListener);
        dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
        
        DropTarget dropTarget = new DropTarget(this,new MyDropTargetListener());
        
    }
    
    
    
    public boolean isDragEnabled(){return enableDrag;}
    /**
     * Enable or disable dragging. If disabled DragJTree behaves like a regular
     * JTree.
     */
    // Does this intentionally override JTree method or accidentally?
    @Override
    public void setDragEnabled(boolean val){
        enableDrag=val;
    }
    
    
    private static final int AUTOSCROLL_MARGIN = 12;
    public void autoscroll(Point pt)  {
        // Figure out which row we are on.
        int nRow = getRowForLocation(pt.x, pt.y);

        // If we are not on a row then ignore this autoscroll request
        if (nRow < 0) return;

        Rectangle raOuter = getBounds();

        nRow =	(pt.y + raOuter.y <= AUTOSCROLL_MARGIN)			// Is row at top of screen? 
        ?	
        (nRow <= 0 ? 0 : nRow - 1)						// Yes, scroll up one row
        :
        (nRow < getRowCount() - 1 ? nRow + 1 : nRow);	// No, scroll down one row

        scrollRowToVisible(nRow);
    }
    public Insets getAutoscrollInsets()  {
        Rectangle raOuter = getBounds();
        Rectangle raInner = getParent().getBounds();
        return new Insets(
            raInner.y - raOuter.y + AUTOSCROLL_MARGIN, raInner.x - raOuter.x + AUTOSCROLL_MARGIN,
            raOuter.height - raInner.height - raInner.y + raOuter.y + AUTOSCROLL_MARGIN,
            raOuter.width - raInner.width - raInner.x + raOuter.x + AUTOSCROLL_MARGIN);
    }
    
    protected Transferable getTransferable(TreePath path){
        return new TransferableTreePath(path);
    }
    
    private boolean isRootPath(TreePath path) {
        return isRootVisible() && getRowForPath(path) == 0;
    }

    /**
     * Checks if drop is allowed in the destination.
     * @param destinationParent The parent of the destination where user is about to drop something.
     * @param destinationPosition The position after which user is about to drop something. May be
     *                            null in which case the item is to be dropped as the first child of
     *                            destinationParent.
     * @param source The item being dragged.
     * @return One of action constants in java.awt.dnd.DnDConstants representing the possible drop actions.
     */
    public abstract int allowDrop(TreePath destinationParent,TreePath destinationPosition,TreePath source);
    /**
     * Performs the drop event. Must modify the tree model to actually make the changes in the tree and
     * also notify the model of these changes so that the UI can be updated.
     *
     * @param destinationParent The parent of the destination where user is about to drop something.
     * @param destinationPosition The position after which user is about to drop something. May be
     *                            null in which case the item is to be dropped as the first child of
     *                            destinationParent.
     * @param source The item being dragged.
     * @param action The drop action. One of the action constants in java.awt.dnd.DnDConstants.
     */
    public abstract void doDrop(TreePath destinationParent,TreePath destinationPosition, TreePath source, int action);
    
    /**
     * Override this method if you wish to process drag and drop events originating from
     * outside this component. Default implementation calls dtde.rejectDrop().
     */
    public void nonLocalDrop(DropTargetDropEvent dtde){
        dtde.rejectDrop();
    }
    /**
     * Override this method if you wish to process drag and drop events originating from
     * outside this component. Default implementation calls dtde.rejectDrag().
     */
    public void nonLocalDragEnter(DropTargetDragEvent dtde){
        dtde.rejectDrag();
    }
    /**
     * Override this method if you wish to process drag and drop events originating from
     * outside this component. Default implementation calls dtde.rejectDrag().
     */
    public void nonLocalDragOver(DropTargetDragEvent dtde){
        dtde.rejectDrag();
    }
    /**
     * Override this method if you wish to process drag and drop events originating from
     * outside this component. Default implementation does nothing.
     */
    public void nonLocalDragExit(DropTargetEvent dtde){
    }
    /**
     * Override this method if you wish to process drag and drop events originating from
     * outside this component. Default implementation calls dtde.rejectDrag().
     */
    public void nonLocalDropActionChanged(DropTargetDragEvent dtde) {
        dtde.rejectDrag();
    }
    
    private class MyDropTargetListener implements DropTargetListener {
        private TreePath lastPath = null;
        private Rectangle2D cueLineRect = new Rectangle2D.Float();
        private Rectangle2D ghostRect = new Rectangle2D.Float();
        private Color cueLineColor;
        private Point lastPoint = new Point();
        private Timer hoverTimer;
                
        public MyDropTargetListener(){
            cueLineColor = new Color(SystemColor.controlShadow.getRed(),
                                     SystemColor.controlShadow.getGreen(),
                                     SystemColor.controlShadow.getBlue(), 128);
            hoverTimer = new Timer(3000, new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if(lastPath==null || isRootPath(lastPath)) return;
                    if(!isExpanded(lastPath)) expandPath(lastPath);
                }
            });
            hoverTimer.setRepeats(false);
        }
        
        public void drop(DropTargetDropEvent dtde) {
            if(!localDragging){
                nonLocalDrop(dtde);
                return;
            }
            hoverTimer.stop();
            int action=dtde.getDropAction();
            
            int accepted=allowDrop(currentParent,currentPosition,selectedTreePath);
            if(accepted==action || 
                (accepted==DnDConstants.ACTION_COPY_OR_MOVE && (action==DnDConstants.ACTION_COPY || action==DnDConstants.ACTION_MOVE))){
                dtde.acceptDrop(accepted);
            }
            else{
                dtde.rejectDrop();
                return;
            }            
            
            Transferable transferable = dtde.getTransferable();
            if(!transferable.isDataFlavorSupported(TransferableTreePath.FLAVOR)) {
                dtde.rejectDrop();
                return;
            }

            doDrop(currentParent,currentPosition,selectedTreePath,action);
            repaint();
        }

        public void dragEnter(DropTargetDragEvent dtde) {
            if(!localDragging){
                nonLocalDragEnter(dtde);
                return;
            }
            int accepted=allowDrop(currentParent,currentPosition,selectedTreePath);
            if(accepted!=DnDConstants.ACTION_NONE) dtde.acceptDrag(accepted);
            else dtde.rejectDrag();
        }

        public void dragExit(DropTargetEvent dte) {
            if(!localDragging){
                nonLocalDragExit(dte);
                return;
            }
            if(!DragSource.isDragImageSupported()){
                repaint(ghostRect.getBounds());
            }
        }

        public void dragOver(DropTargetDragEvent dtde) {
            if(!localDragging){
                nonLocalDragOver(dtde);
                return;
            }
            Point pt = dtde.getLocation();
            if(pt.equals(lastPoint)) return;
            
            Graphics2D g2 = (Graphics2D)getGraphics();
            
            // Drag image support has been disabled as the image flickers.
            if(false && !DragSource.isDragImageSupported()) {
                paintImmediately(ghostRect.getBounds());
                ghostRect.setRect(pt.x-offsetPoint.x,pt.y-offsetPoint.y, ghostImage.getWidth(), ghostImage.getHeight());
                g2.drawImage(ghostImage, AffineTransform.getTranslateInstance(ghostRect.getX(),ghostRect.getY()),null);
            }
            int row=getClosestRowForLocation(pt.x,pt.y);
            TreePath path=getPathForRow(row);
            if(path!=lastPath) {
                lastPath=path;
                hoverTimer.restart();
            }
            Rectangle pathRect=getPathBounds(path);
            int accepted=DnDConstants.ACTION_NONE;
            
            if((pt.y>=pathRect.y+pathRect.height/4 || (row==0 && DragJTree.this.isRootVisible())) && pt.y<pathRect.y+pathRect.height*3/4){
                cueLineRect.setRect(pathRect);
                currentParent=path;
                currentPosition=null;
                accepted=allowDrop(currentParent,currentPosition,selectedTreePath);
            }
            if(accepted==DnDConstants.ACTION_NONE){
                int x=pathRect.x;
                TreePath next=getPathForRow(row+1);
                TreePath prev=getPathForRow(row-1);
                if(row==0 && !DragJTree.this.isRootVisible()) prev=new TreePath(getModel().getRoot());
                if(pt.y<pathRect.y+pathRect.height/2 && prev!=null){
                    next=path;
                    path=prev;
                    prev=getPathForRow(row-2);
                    if(row==1 && !DragJTree.this.isRootVisible()) prev=new TreePath(getModel().getRoot());
                    if(path.getPathCount()==1 && !DragJTree.this.isRootVisible())
                        pathRect=new Rectangle(0,0,pathRect.width,0);
                    else pathRect=getPathBounds(path);
                }
                currentParent=path.getParentPath();
                currentPosition=path;
                if(next!=null && next.getParentPath().getLastPathComponent()==path.getLastPathComponent()){
                    x=getPathBounds(next).x;
                    currentParent=path;
                    currentPosition=null;
                }
                else if(pt.y>=pathRect.y+pathRect.height){
                    if(next==null || next.getParentPath().getLastPathComponent()!=path.getParentPath().getLastPathComponent()){
                        if(path.getParentPath().getParentPath()!=null){
                            x=getPathBounds(path.getParentPath()).x;
                            currentParent=path.getParentPath().getParentPath();
                            currentPosition=path.getParentPath();
                        }
                    }
                }
                cueLineRect.setRect(x,pathRect.y+(int)pathRect.getHeight(),getWidth()-x,2);
                accepted=allowDrop(currentParent,currentPosition,selectedTreePath);
            }
            
            
            if(accepted!=DnDConstants.ACTION_NONE){
                g2.setColor(cueLineColor);
                g2.fill(cueLineRect);

                ghostRect = ghostRect.createUnion(cueLineRect);
                
                dtde.acceptDrag(accepted);
            }
            else{
                dtde.rejectDrag();
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            if(!localDragging){
                nonLocalDropActionChanged(dtde);
                return;
            }
            Point pt = dtde.getLocation();
            TreePath path=getClosestPathForLocation(pt.x,pt.y);
            int accepted=allowDrop(currentParent,currentPosition,selectedTreePath);
            if(accepted!=DnDConstants.ACTION_NONE){
                dtde.acceptDrag(accepted);
            }
            else{
                dtde.rejectDrag();
            }            
        }
            
        
    }
    
    public static class TransferableTreePath implements Transferable {
        public static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType,"TreePath");
        private TreePath path;
        private DataFlavor[] flavors=new DataFlavor[]{FLAVOR};
        public TransferableTreePath(TreePath path){
            this.path=path;
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor==FLAVOR;
        }
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            return path;
        }        
    }
    
    public static boolean isDescendant(TreePath parent,TreePath child){
        Object last=parent.getLastPathComponent();
        for(Object n : child.getPath()){
            if(n==last) return true;
        }
        return false;
    }
    
    public static void main(String[] args){
        final JFrame w=new JFrame();
        w.add(new DragJTree(){
            public int allowDrop(TreePath destinationParent,TreePath destinationPosition,TreePath source){
                if(destinationParent==null) return DnDConstants.ACTION_NONE;
                if(isDescendant(source,destinationParent)) return DnDConstants.ACTION_NONE;
                return DnDConstants.ACTION_MOVE;
            }
            public void doDrop(TreePath destinationParent,TreePath destinationPosition,TreePath source,int action){
                System.out.println("Moving "+source+" to "+destinationParent+", "+destinationPosition);
            }
        });
        w.pack();
        w.setSize(400,400);
        w.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                w.setVisible(false);
                w.dispose();
                System.exit(0);
            }
        });
        w.setVisible(true);
    }

}
