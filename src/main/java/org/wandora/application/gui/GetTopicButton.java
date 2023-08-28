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
 * GetTopicButton.java
 *
 * Created on 17. helmikuuta 2006, 14:28
 *
 */

package org.wandora.application.gui;





import static org.wandora.utils.Tuples.t2;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.TransferHandler;

import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Tuples.T2;




/**
 *
 * @author olli
 */
public class GetTopicButton extends SimpleButton {
	
	private static final long serialVersionUID = 1L;

    protected Topic selectedTopic = null;
    protected String originalSubjectIdentifier = null;
    protected Wandora wandora = null;
    protected java.awt.Window parent = null;
    protected boolean showNone;
    protected ButtonHandler buttonHandler;
    protected ButtonListener listener;
    
    
    /** Creates a new instance of GetTopicButton */
    public GetTopicButton() throws TopicMapException {
        this(Wandora.getWandora());
    }
    public GetTopicButton(Wandora wandora) throws TopicMapException {
        this((Topic)null,wandora,wandora);
    }
    public GetTopicButton(Wandora wandora,java.awt.Window parent) throws TopicMapException {
        this((Topic)null,wandora,parent);
    }

    public GetTopicButton(String si ,Wandora wandora) throws TopicMapException {
        this(wandora.getTopicMap().getTopic(si),wandora);
        originalSubjectIdentifier = si;
    }
    
    public GetTopicButton(Topic t,Wandora wandora) throws TopicMapException{
        this(t,wandora,wandora);
    }
    public GetTopicButton(Topic t,Wandora wandora,java.awt.Window parent) throws TopicMapException{
        this(t,wandora,parent,false);
    }
    public GetTopicButton(Topic t,Wandora wandora,java.awt.Window parent,boolean showNone) throws TopicMapException{
        this(t,wandora,parent,showNone,null);
    }
    public GetTopicButton(Topic t,Wandora wandora,java.awt.Window parent,boolean showNone,ButtonHandler buttonHandler) throws TopicMapException{
        this.selectedTopic=t;
        this.wandora=wandora;
        this.parent=parent;
        this.showNone=showNone;
        this.buttonHandler=buttonHandler;

        this.setBackground( Color.WHITE );
                
        updateText();
        this.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt){
                pressed();
            }
        });
        
        this.setTransferHandler(new TopicButtonTransferHandler());
    }
    

    @Override
    protected void initialize() {
        this.setFocusPainted(false);
        this.setFont(UIConstants.buttonLabelFont);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setOpaque(true);
        this.setBackground(Color.WHITE);
        this.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if(evt.getComponent().isEnabled())
                        evt.getComponent().setBackground(Color.WHITE);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if(evt.getComponent().isEnabled())
                        evt.getComponent().setBackground(Color.WHITE);
                }
            }
        );
    }
    
    
    public void setButtonListener(ButtonListener listener){
        this.listener=listener;
    }
    
    
    public void setButtonHandler(ButtonHandler buttonHandler){
        this.buttonHandler=buttonHandler;
    }
    
    
    public void setShowNone(boolean b){
        showNone=b;
    }
    
    
    public Topic getTopic(){
        return selectedTopic;
    }
    
    
    public String getTopicSI() throws TopicMapException {
        if(selectedTopic==null) return originalSubjectIdentifier;
        else return selectedTopic.getOneSubjectIdentifier().toExternalForm();
    }
    
    
    public void setTopic(Topic t) throws TopicMapException {
        selectedTopic=t;
        updateText();
        if(listener!=null) listener.topicChanged(this);
    }
    
    
    public void setTopic(String si) throws TopicMapException {
        originalSubjectIdentifier = si;
        if(si==null) setTopic((Topic)null);
        else setTopic(wandora.getTopicMap().getTopic(si));
    }
    
    
    public void updateText() throws TopicMapException {
        if(selectedTopic==null) this.setText("<No topic>");
        else {
            String text = TopicToString.toString(selectedTopic);
            this.setText(text);
        }
        this.setToolTipText(this.getText());
    }
    
    
    public String getCopyString() {
        return this.getText();
    }
    
    
    @Override
    public java.awt.Dimension getMinimumSize(){
        java.awt.Dimension d=super.getPreferredSize();
        if(d==null) return null;
        return new java.awt.Dimension(30,d.height);
    }

    
    protected void pressed() {
        try{
            T2<Topic,Boolean> t=null;
            if(buttonHandler!=null) t=buttonHandler.pressed(this);
            else t=defaultPressHandler();
            if(!t.e2) {
                selectedTopic=t.e1;
                updateText();
                if(listener!=null) listener.topicChanged(this);
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
    }
    
    
    public T2<Topic,Boolean> defaultPressHandler() throws TopicMapException {
        if(!showNone){
            Topic t=null;
            if(parent!=null && parent instanceof java.awt.Frame) {
                t=wandora.showTopicFinder((java.awt.Frame)parent);
            }
            else if(parent!=null && parent instanceof java.awt.Dialog) {
                t=wandora.showTopicFinder((java.awt.Dialog)parent);
            }
            else {
                t=wandora.showTopicFinder();
            }
            if(t!=null) return t2(t,false);
            else return t2(null,true);
        }
        else {
            T2<Topic,Boolean> t=null;
            if(parent!=null && parent instanceof java.awt.Frame) {
                t=wandora.showTopicFinderWithNone((java.awt.Frame)parent);
            }
            else if(parent!=null && parent instanceof java.awt.Dialog) {
                t=wandora.showTopicFinderWithNone((java.awt.Dialog)parent);
            }
            else {
                t=wandora.showTopicFinderWithNone();
            }
            return t;
        }
    }
    
    
    public void addPopupList(final Collection<Topic> topics){
        this.addMouseListener(new PopupMouseListener(new PopupListHandler(){
            @Override
            public Collection<Topic> getListTopics() {
                return topics;
            }
        }));
    }
    public void addPopupList(PopupListHandler handler){
        this.addMouseListener(new PopupMouseListener(handler));
    }
        

    public static interface ButtonHandler {
        public T2<Topic,Boolean> pressed(GetTopicButton button) throws TopicMapException ;
    }
    
    
    public static interface PopupListHandler {
        public Collection<Topic> getListTopics() throws TopicMapException;
    }
    
    
    public static abstract class CachedPopupListHandler implements PopupListHandler {
        protected Collection<Topic> topics;
        protected boolean handlerCalled=false;
        public abstract Collection<Topic> getUncachedTopics() throws TopicMapException ;
        @Override
        public Collection<Topic> getListTopics() throws TopicMapException {
            if(!handlerCalled){
                handlerCalled=true;
                topics=getUncachedTopics();
            }
            return topics;
        }
    }
    
    
    public class PopupMouseListener extends MouseAdapter {
        protected Collection<Topic> topics;
        protected PopupListHandler handler;
        public PopupMouseListener(PopupListHandler handler){
            this.handler=handler;
        }
        public void topicSelected(Topic t){
            try {
                setTopic(t);
            }catch(TopicMapException tme){
                wandora.handleError(tme);
            }
        }
        @Override
        public void mousePressed(MouseEvent evt) {
            if(evt.getButton()!=MouseEvent.BUTTON3) return;
            try{
                topics=handler.getListTopics();
                if(topics==null || topics.isEmpty()) return;
                JPopupMenu menu=new JPopupMenu();
                int counter=0;
                for(final Topic t : topics){
                    counter++;
                    if(counter>10) {
                        SimpleMenuItem item=new SimpleMenuItem("More...");
                        item.addActionListener(new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent evt){
                                GetTopicButton.this.pressed();
                            }
                        });
                        menu.add(new JSeparator());
                        menu.add(item);
                        break;
                    }
                    SimpleMenuItem item=new SimpleMenuItem(t.getDisplayName());
                    item.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent evt){
                            PopupMouseListener.this.topicSelected(t);
                        }
                    });
                    menu.add(item);
                }
                menu.show(evt.getComponent(),evt.getX(),evt.getY());
            }
            catch(TopicMapException tme){
                wandora.handleError(tme);
            }
        }        
    }
    
    
    public static interface ButtonListener {
        public void topicChanged(GetTopicButton button);
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------ Drag and Drop ----
    // -------------------------------------------------------------------------
    
    
    
    
    
    private class TopicButtonTransferHandler extends TransferHandler {
    	
    	private static final long serialVersionUID = 1L;

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return DnDHelper.makeTopicTransferable(GetTopicButton.this);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            
            System.out.println("DROP ON GETTOPICBUTTON");
            
            if(!support.isDrop()) return false;
            try {
                TopicMap tm=Wandora.getWandora().getTopicMap();
                List<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null || topics.isEmpty()) return false;
                setTopic(topics.get(0));
                
                revalidate();
                return true;
            }
            catch(TopicMapException tme) { tme.printStackTrace(); }
            catch(Exception ce){}
            return false;
        }

    }

}
