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
 * Created on Oct 19, 2011, 8:12:21 PM
 */


package org.wandora.application.gui.topicpanels.treemap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import org.wandora.application.contexts.PreContext;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.TreeMapTopicPanel;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.navigate.OpenTopic;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;



/**
 *
 * @author elias, akivela
 */


public class TreeMapComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, ActionListener, MouseWheelListener {
    
    private static final int topicFontSize = 12;
    private static final int typeFontSize = 9;
    private static final int textLineY = 15;
    
    private Topic topic = null;

    private int mouseX;
    private int mouseY;
    private boolean isMouseOver = false;
    
    private int width;
    private int height;

    private float zoom = 1;
    private Rect zoomRect;
    private Rect zoomDrawArea = null;

    private int iterationDepth = 2;
    private int treeMapWidth, treeMapHeight, treeMapX, treeMapY;

    private Rect bounds;

    private TopicInfo[] viewedTopics = null;

    private Font sansFont;

    private String TYPE_INSTANCE = "instance-of";
    private String TYPE_CLASS = "class-of";

    private static final Color[] colorList = new Color[] {
        Color.YELLOW, Color.RED, Color.PINK, 
        Color.ORANGE, Color.MAGENTA, Color.CYAN,
        Color.YELLOW.darker().darker(), Color.RED.darker().darker(), Color.PINK.darker().darker(), 
        Color.ORANGE.darker().darker(), Color.MAGENTA.darker().darker(), Color.CYAN.darker().darker(),
        Color.YELLOW.brighter(), Color.RED.brighter(), Color.PINK.brighter(), 
        Color.ORANGE.brighter(), Color.MAGENTA.brighter(), Color.CYAN.brighter(),
    };
    private static HashMap<String,Color> topicColors = new HashMap();
    private static HashMap<Topic, HashMap<Topic, Collection<Topic>>> associationTopicsCache = new HashMap();
    private static HashMap<Topic, Integer> associationTopicsSizeCache = new HashMap();

    
    private Graphics g = null;

    private StripTreeMap algorithm;
    private TreeModel model;
    private DefaultMutableTreeNode tree;
    
    private JPopupMenu popup = null;
    
    private TreeMapTopicPanel treeMapTopicPanel = null;
    
    
    public TreeMapComponent(TreeMapTopicPanel topicPanel) {
        treeMapTopicPanel = topicPanel;
        sansFont = new Font("SansSerif", Font.PLAIN, 12);
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.g = this.getGraphics();
    }
    
    
    
    
    public void setIterationDepth(int d) {
        if(d < 10 && d > 0) {
            this.iterationDepth = d;
            initialize(topic);
        }
        else {
            System.out.println("Illegal iteration depth used ("+d+"). Rejecting.");
        }
    }
    
    
    public int getIterationDepth() {
        return iterationDepth;
    }
    
    
    public void initialize(Topic t) {
        topic = t;
        tree = createNode(topic, 0, 0, TYPE_INSTANCE);
        model = new TopicTree(tree);
        algorithm = new StripTreeMap();
        prepareDraw();
        repaint();
    }
    
    
    public void resetZoom() {
        zoom = 1;
        prepareDraw();
        repaint();
    }
    

    public void updateZoom(int steps) {
        /*
        boolean needsUpdating = false;
        boolean isZoomingIn = false;

        if(mouseX < treeMapX || mouseY < treeMapY || mouseX > (treeMapX+treeMapWidth) || mouseY > (treeMapY+treeMapHeight)) {
            return;
        }
        steps = steps * -1;
        if(steps > 0) {
            if(zoom < 5)  {
                needsUpdating = true;
                zoom += (float)(steps/10f); 
                if(zoom > 5) zoom = 5;
                isZoomingIn = true;
            }
        } 
        else if(steps < 0) {
            if(zoom > 1) {
                needsUpdating = true;
                zoom += (float)(steps/10f);
                if(zoom < 1) zoom = 1;
            }
        }

        if(needsUpdating) {
            zoomRect.w = bounds.w/zoom;
            zoomRect.h = bounds.h/zoom;

            float zoomX = ((float)(mouseX - treeMapX) / (float)treeMapWidth - 0.5f); // Ranges from -0.5 to 0.5
            float zoomY = ((float)(mouseY - treeMapY) / (float)treeMapHeight - 0.5f);

            float addX = (float)zoomRect.w / 2f * zoomX;
            float addY = (float)zoomRect.h / 2f * zoomY;

            if(isZoomingIn) { // Disable zoom movement if zooming out
                zoomRect.x += addX;
                zoomRect.y += addY;
            }

            if(zoomRect.x < 0) zoomRect.x = 0f;
            if(zoomRect.y < 0) zoomRect.y = 0f;

            if(zoomRect.x+zoomRect.w > bounds.w) zoomRect.x = bounds.w-zoomRect.w;
            if(zoomRect.y+zoomRect.h > bounds.h) zoomRect.y = bounds.h-zoomRect.h;

            zoomDrawArea.x = zoomRect.x / bounds.w * (float)treeMapWidth * zoom;
            zoomDrawArea.y = zoomRect.y / bounds.h * (float)treeMapHeight * zoom;
        }
         * 
         */
    }

    
    RenderingHints qualityHints = new RenderingHints(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);
    RenderingHints antialiasHints = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    RenderingHints antialiasText = new RenderingHints(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    RenderingHints lcdText = new RenderingHints(
        RenderingHints.KEY_TEXT_ANTIALIASING, 
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    
    
    
    private void initializePaint(Graphics g) {
        if(g instanceof Graphics2D) {
            ((Graphics2D) g).addRenderingHints(lcdText);
            //((Graphics2D) g).addRenderingHints(qualityHints);
            //((Graphics2D) g).addRenderingHints(antialiasHints);
            //((Graphics2D) g).addRenderingHints(antialiasText);
        }
    }


    
    
    @Override
    public void paint(Graphics g) {
        //super.paint(g);
        if(g != null) {
            initializePaint(g);
            //g.setClip(0, 0, this.getWidth()+1, this.getHeight()+1);
            //g.setColor(this.getBackground());
            //g.fillRect(0, 0, this.getWidth()+5, this.getHeight()+5);
            this.g = g;
            draw();
            super.paint(g);  
        }
    }
    
    
    
    
    public void prepareDraw() {
        width = this.getWidth();
        height = this.getHeight();
        
        treeMapWidth = width; // 515;
        treeMapHeight = height-22; //450;
        treeMapX = 0; // width/2 - treeMapWidth/2;
        treeMapY = 22; // (int)((height/2 - treeMapHeight/2) * 0.7);

        bounds = new Rect(0f,0f,(float)treeMapWidth/(float)treeMapHeight,1f);
        if(model != null && algorithm != null) {
            model.layout(algorithm, bounds);
        }
        zoomRect = new Rect(0f,0f,bounds.w,bounds.h);
        zoomDrawArea = new Rect(0f, 0f, (float)treeMapWidth, (float)treeMapHeight);
    }
    
    

    public void draw() {               
        isMouseOver = false;
        viewedTopics = null;
        drawNodeTree((DefaultMutableTreeNode)tree);
        drawMouseOverCanvas();
        drawInfoWindow(); // Must come AFTER the drawNodeTree
    }


    public void drawNodeTree(DefaultMutableTreeNode node) {
        //System.out.println("drawing node: "+node.getChildCount());

        int w=treeMapWidth;
        int h=treeMapHeight;
        int tx=treeMapX; 
        int ty=treeMapY;
        
        if(g != null) {
            //g.setClip(tx, ty, w, h);
        }
        
        //itemCount++;

        TopicInfo ti = (TopicInfo)node.getUserObject();
        Rect r = ti.getBounds();
        //System.out.println("bounds: "+r);

        if(r.x > zoomRect.x+zoomRect.w || r.y > zoomRect.y+zoomRect.h || r.x+r.w < zoomRect.x || r.y+r.h < zoomRect.y) {
            return;
        }

        int x=(int)Math.round(w*r.x/bounds.w);
        int width=(int)Math.round(w*(r.x+r.w)/bounds.w)-x;
        int y=(int)Math.round(h*r.y/bounds.h);
        int height=(int)Math.round(h*(r.y+r.h)/bounds.h)-y;

        x *= zoom;
        y *= zoom;
        width *= zoom;
        height *= zoom;

        x -= zoomDrawArea.x;
        y -= zoomDrawArea.y;

        x += tx;
        y += ty;

        ti.rectArea = new Rect(x, y, width, height);

        setColor(solveMapAreaColor(ti.type, ti.getOrder()));
        drawFilledRect(x,y,width,height);
        setColor(Color.BLACK);
        drawRect(x,y,width,height);

        Enumeration children = node.children();
        if(children != null)  {
            while (children.hasMoreElements()) {
                drawNodeTree((DefaultMutableTreeNode) children.nextElement());
            }
        }

        if(!isMouseOver && ti.depth > 1 && overTopic(x, y, width ,height)) {
            viewedTopics = new TopicInfo[ti.depth-1];
            viewedTopics[ti.depth-2] = ti;
            DefaultMutableTreeNode currentNode = node;
            for(int i=ti.depth-3; i>=0; i--) {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
                viewedTopics[i] = (TopicInfo)parentNode.getUserObject();
                currentNode = parentNode;
            }
            isMouseOver = true;
        }
    }
    
    

    private Color solveMapAreaColor(String t) {
        return solveMapAreaColor(t, 0);
    }
    private Color solveMapAreaColor(String t, int order) {
        Color c = topicColors.get(t);
        if(c == null) c = Color.BLUE;
        return c;
    }


    private void drawMouseOverCanvas() {
        if(isMouseOver) {
            if(g != null) {
                g.setClip(0,0,width+2, height+2);
            }
            for(int i=0;i<viewedTopics.length;i++) {
                TopicInfo t = viewedTopics[i];
                Rect rect = t.rectArea;
                setColor(Color.WHITE);
                drawRect(rect.x, rect.y, rect.w, rect.h);
                drawRect(rect.x-1, rect.y-1, rect.w+2, rect.h+2);
                drawRect(rect.x-2, rect.y-2, rect.w+4, rect.h+4);
            }
        }
    }


    private void drawInfoWindow() {
        if(g != null) {
            g.setClip(0,0,width,height);
        }
        int currentX = width-10;
        if(isMouseOver) {
            String[] names = new String[viewedTopics.length+1];
            String[] types = new String[viewedTopics.length]; 
            names[0] = getTopicName();

            for(int i=0;i<viewedTopics.length;i++) {
                TopicInfo t = viewedTopics[i];
                names[i+1] = t.name;
                if(t.name.length() > 80) {
                   names[i+1] = t.name.substring(0, 77) + "...";
                }
                types[i] = t.type;
            }
            currentX = width-10;

            for(int i=0; i<names.length; i++) {
                setColor(Color.BLACK);
                textFont(sansFont, topicFontSize);
                currentX -= textWidth(names[i]);
                drawText(names[i], (float)currentX, textLineY);
                if(i<names.length-1) {
                    textFont(sansFont, typeFontSize);
                    int textWidth = textWidth(types[i]);
                    currentX -= textWidth + 10;
                    drawText(types[i], (float)currentX, textLineY);
                    setColor(solveMapAreaColor(viewedTopics[i].type));
                    drawFilledRect(currentX, 0, textWidth, 3);
                    currentX -= 10;
                }
            }
        }
        else {
            textFont(sansFont, topicFontSize);
            currentX -= textWidth(getTopicName());
            drawText(getTopicName(), currentX, textLineY);
        }
        //textFont(sans_font,10);
        //String zoom_str = "zoom "+ (int)(zoom * 100) + "%";
        //text(zoom_str, width-textWidth(zoom_str)-2, 14);
    }

    
    
    

    public boolean overTopic(int x, int y, int width, int height) {
        if(mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height) {
            return true;
        } 
        else {
            return false;
        }
    }

    
    
    
    // -------------------------------------------------------------------------
    
    
    public String getTopicName(Topic t) {
        return TopicToString.toString(t);
    }
    
    
    
    public String getTopicName() {
        return getTopicName(topic);
    }
    
    
    
    // -------------------------------------------------------------------------
    

    public void setColor(Color c) {
        if(g != null) {
            g.setColor(c);
        }
    }
    
    
    public void setColor(int c) {
        if(g != null) {
            g.setColor(new Color(c));
        }
    }
    
    
    public void drawRect(double x, double y, double w, double h) {
        drawRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(w), (int) Math.round(h));
    }
    
    
    public void drawRect(int x, int y, int w, int h) {
        if(g != null) {
            g.drawRect(x, y, w, h);
        }
    }
    
    public void drawFilledRect(float x, float y, float w, float h) {
        drawFilledRect(Math.round(x), Math.round(y), Math.round(w), Math.round(h));
    }
    
    public void drawFilledRect(int x, int y, int w, int h) {
        if(g != null) {
            g.fillRect(x, y, w, h);
        }
    }

    

    
    public void textFont(Font f, int s) {
        if(g != null) {
            g.setFont(f.deriveFont(Font.PLAIN, s));
        }
    }
    
    public int textWidth(String str) {
        if(g != null) {
            FontMetrics fm = g.getFontMetrics();
            return fm.stringWidth(str);
        }
        else {
            return 0;
        }
    }
    
    public void drawText(String str, int x, int y) {
        if(g != null)
            g.drawString(str, x, y);
    }
    
    public void drawText(String str, float x, int y) {
        if(g != null)
            g.drawString(str, Math.round(x), Math.round(y));
    }
    
    

    
    
    
    // -------------------------------------------------------------------------
    
    



    public void mouseClicked(MouseEvent e) {
    }

    
    public void mousePressed(MouseEvent e) {
    }
    

    public void mouseReleased(MouseEvent evt) {
        if(isMouseOver) {
            for(int i=0; i<viewedTopics.length; i++) {
                TopicInfo t = viewedTopics[i];
                if(overTopic((int)t.rectArea.x, (int)t.rectArea.y, (int)t.rectArea.w, (int)t.rectArea.h)) {
                    Object [] struct = new Object[viewedTopics.length*2];
                    for(int j=0;j<viewedTopics.length;j++) {
                        TopicInfo t2 = viewedTopics[j];
                        int index = j*2;
                        struct[index] = t2.name;
                        try {
                            struct[index+1] = new OpenTopic(new PreContext( t2.t.getOneSubjectIdentifier() ));
                        } 
                        catch(Exception e){};
                    }
                    //System.out.println("popup created!");
                    popup = UIBox.makePopupMenu(struct, this);
                    
                    Object[] optionsStruct = new Object[] {
                        "---",
                        "Options", treeMapTopicPanel.getViewMenuStruct()
                    };
                    popup = UIBox.attachPopup(popup, optionsStruct, treeMapTopicPanel);
                    
                    popup.show(this, mouseX-2, mouseY-2);
                    break;
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    
    

    // -------------------------------------------------------------------------
    
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    public void componentHidden(ComponentEvent e) {
    }
    
    
    public void handleComponentEvent(ComponentEvent e) {
        try {
            Dimension size = getSize();
            Component c = this.getParent().getParent().getParent();
            if(c != null) {
                if(!(c instanceof JScrollPane)) {
                    size = c.getSize();
                }
                if(!size.equals(getSize())) {
                    System.out.println("new size treemapcomponent: "+size);
                    setPreferredSize(size);
                    setMinimumSize(size);
                    setSize(size);
                }
            }
            revalidate();
            prepareDraw();
            repaint();

        }
        catch(Exception ex) {
            // SKIP
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    

    
    
    public void clearAssociationTopicsCache() {
        associationTopicsCache = new HashMap();
        associationTopicsSizeCache = new HashMap();
        //topicColors = new HashMap();
    }
    
    
    
    private DefaultMutableTreeNode createNode(Topic curTopic, int curOrder, int curDepth, String type) {
        try {
            Collection<Topic> instances = curTopic.getTopicMap().getTopicsOfType(curTopic);
            Collection<Topic> classes = curTopic.getTypes();
            HashMap<Topic, Collection<Topic>> associationTopics = associationTopicsCache.get(curTopic);
            int associationTopicsSize = 0;
            
            if(associationTopics == null) {
                associationTopics = new HashMap();
                Collection<Association> associations = curTopic.getAssociations();
                associationTopicsSize = 0;
                for(Association a : associations) {
                    Topic at = a.getType();
                    Collection<Topic> linkedTopics = associationTopics.get(at);
                    if(linkedTopics == null) {
                        linkedTopics = new ArrayList<Topic>();
                        associationTopics.put(at, linkedTopics);
                    }
                    Topic player = null;
                    boolean skipCurrentTopic = true;
                    for(Topic role : a.getRoles()) {
                        player = a.getPlayer(role);
                        if(skipCurrentTopic && player.mergesWithTopic(curTopic)) {
                            skipCurrentTopic = false;
                            continue;
                        }
                        linkedTopics.add(player);
                        associationTopicsSize++;
                    }
                }
                associationTopicsSizeCache.put(curTopic, new Integer(associationTopicsSize));
                associationTopicsCache.put(curTopic, associationTopics);
            }
            
            associationTopicsSize = associationTopicsSizeCache.get(curTopic);
            
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                new TopicInfo(curTopic, associationTopicsSize+instances.size()+classes.size(), curOrder, curDepth, type)
            );

            if(curDepth < iterationDepth) {
                // *** Instances ***
                int counter = 0;
                if(!topicColors.containsKey(TYPE_INSTANCE))
                    topicColors.put(TYPE_INSTANCE, Color.BLUE);
                for(Topic t : instances) {
                    DefaultMutableTreeNode leaf = createNode(t, counter, curDepth+1, TYPE_INSTANCE);
                    node.add(leaf);
                    counter++;
                }

                // *** Types aka classes ***
                counter = 0;
                if(!topicColors.containsKey(TYPE_CLASS))
                    topicColors.put(TYPE_CLASS, Color.GREEN);
                for(Topic t : classes) {
                    DefaultMutableTreeNode leaf = createNode(t, counter, curDepth+1, TYPE_CLASS);
                    node.add(leaf);
                    counter++;
                }
                
                // *** Associations ***
                for(Topic key : associationTopics.keySet()) {
                    counter = 0;
                    Collection<Topic> linkedTopics = associationTopics.get(key);
                    String atype = getTopicName(key);
                    if(!topicColors.containsKey(atype)) {
                        topicColors.put(atype, colorList[topicColors.size() % colorList.length]);
                    }
                    for(Topic t : linkedTopics) {
                        DefaultMutableTreeNode leaf = createNode(t, counter, curDepth+1, atype);
                        node.add(leaf);
                        counter++;
                    }
                }
            }
            return node;
        }
        catch (Exception ex) {
            //Logger.getLogger(SketchTemplate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        
        if(popup != null) {
            if(popup.isVisible()) {
                popup.setVisible(false);
            }
        }
        
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int steps = e.getWheelRotation();
        updateZoom(steps);
        repaint();
    }
    
    
    



    
    

}
