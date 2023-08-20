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
 * TopicSelectList.java
 *
 * Created on August 11, 2004, 2:00 PM
 */

package org.wandora.application.gui;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author  olli
 */
public class TopicSelectList extends javax.swing.JPanel  {
    

	private static final long serialVersionUID = 1L;

	
	private Topic[] topics;
    private JComboBox cbox;
    private JTextField field;
    private boolean running;
    private ListWindow listWindow;
    private boolean editable;
    private JDialog owner;
    
    /** Creates new form TopicSelector */
    public TopicSelectList(Collection ts,boolean editable,JDialog owner) {
        this.owner=owner;
        this.topics=(Topic[])TMBox.sortTopics(ts,null).toArray(new Topic[0]);
        this.editable=editable;
        initComponents();
        this.setLayout(new java.awt.BorderLayout());
        if(topics.length<0){
            cbox=new JComboBox();
            for(int i=0;i<topics.length;i++){
                Topic t=topics[i];
                cbox.addItem(new ComboBoxTopicWrapper(t));
            }
            cbox.setEditable(editable);
            this.add(cbox,java.awt.BorderLayout.CENTER);
        }
        else{
            field=new JTextField();
            field.setFocusTraversalKeysEnabled(false);
/*            field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                public void changedUpdate(javax.swing.event.DocumentEvent e){
                    if(listWindow!=null){
                        listWindow.hide();
                        listWindow.dispose();
                        showList();
                    }
                }
                public void insertUpdate(javax.swing.event.DocumentEvent e){changedUpdate(e);}
                public void removeUpdate(javax.swing.event.DocumentEvent e){changedUpdate(e);}
            });*/
            field.addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyReleased(java.awt.event.KeyEvent e){
                    /*if(listWindow!=null){
                        e.consume();
                        e.setSource(listWindow.l);
                        listWindow.l.dispatchEvent(e);
                    }
                    else */
                    if(listWindow==null && e.getKeyCode()==e.VK_TAB){
                        e.consume();
                        showList(); 
                    }
                }
                public void keyTyped(java.awt.event.KeyEvent e){
                    if(listWindow!=null){
                        e.setSource(listWindow.l);
                        listWindow.l.dispatchEvent(e);
                    }
                }
/*                public void keyPressed(java.awt.event.KeyEvent e){
                    if(listWindow!=null){
                        if(e.getKeyCode()==e.VK_ENTER){
                            if(listWindow.l.getSelectedIndex()!=-1){
                                String text=((ComboBoxTopicWrapper)listWindow.l.getSelectedValue()).topic.getBaseName();
                                setText(text);
                            }
                            listWindow.hide();
                            listWindow.dispose();
                            listWindow=null;
                        }
                        else if(e.getKeyCode()==e.VK_ESCAPE){
                            listWindow.hide();
                            listWindow.dispose();
                            listWindow=null;                            
                        }
                        else{
                            e.setSource(listWindow.l);
                            listWindow.l.dispatchEvent(e);
                        }
                    }
                }*/
            });
            
            this.add(field,java.awt.BorderLayout.CENTER);
        }
    }
    
    
    
    public void requestFocusOnField() {
        field.requestFocusInWindow();
    }
    
    
    
    private void showList(){
        if(listWindow!=null){
            listWindow.setVisible(false);
            listWindow.dispose();
        }
        listWindow=new ListWindow(topics,field.getText(),this,owner);
        if(!listWindow.isVisible()){
            listWindow.dispose();
            listWindow=null;
        }
    }
    
    public void hideList(){
        if(listWindow!=null){
            listWindow.setVisible(false);
            listWindow.dispose();
            listWindow=null;
        }
    }
    
    public void setText(String text){
        field.setText(text);
    }
    public JTextField getField(){
        return field;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    public Object getSelection() throws TopicMapException {
        if(cbox!=null) {
            Object o=cbox.getSelectedItem();
            if(o instanceof ComboBoxTopicWrapper) return ((ComboBoxTopicWrapper)o).topic;
            else return o;
        }
        else{
            String text=field.getText();
            for(int i=0;i<topics.length;i++){
                String basename = topics[i].getBaseName();
                if(basename != null)
                    if(basename.equals(text)) return topics[i];
            }
            if(!editable) return null;
            else return text;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}



// -----------------------------------------------------------------------------




class ListWindow extends JDialog {
    public JList l;
    public Vector data;
    public ListWindow(Topic[] topics,final String written,TopicSelectList selector,JDialog owner) {
        super(owner,"",false);
        final JDialog wnd=this;
        final TopicSelectList s=selector;
        final JList list=new JList();
        JScrollPane scroller=new JScrollPane();
        scroller.setViewportView(list);
        l=list;
        data=new Vector();
        String prefix=null;
        for(int i=0;i<topics.length;i++){
            try{
                if(topics[i].getBaseName()!=null && topics[i].getBaseName().startsWith(written)){
                    if(prefix==null) prefix=topics[i].getBaseName();
                    else{
                        String name=topics[i].getBaseName();
                        int j;
                        for(j=written.length();j<prefix.length() && j<name.length() && prefix.charAt(j)==name.charAt(j);j++) ;
                        if(j<prefix.length()) prefix=name.substring(0,j);
                    }
                    data.add(new ComboBoxTopicWrapper(topics[i]));
                }
            }catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
            }
        }
        if(data.size()==0) return;
        if(prefix!=null && prefix.length()>written.length()) {
            s.setText(prefix);
//            s.setText( ((ComboBoxTopicWrapper)data.elementAt(0)).topic.getBaseName() );
            return;
        }
        list.setListData(data);
        list.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusLost(java.awt.event.FocusEvent e){
                s.hideList();
            }
        });
/*        list.addListSelectionListener(new javax.swing.event.ListSelectionListener(){
            public void valueChanged(javax.swing.event.ListSelectionEvent e){
                if(list.getSelectedIndex()!=-1){
                    String text=((ComboBoxTopicWrapper)list.getSelectedValue()).topic.getBaseName();
                    s.setText(text);
                }
            }
        });*/
        list.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){
                if(list.getSelectedIndex()!=-1){
                    s.hideList();
                    try{
                        String text=((ComboBoxTopicWrapper)list.getSelectedValue()).topic.getBaseName();
                        s.setText(text);
                    }catch(TopicMapException tme){
                        tme.printStackTrace(); // TODO EXCEPTION;
                        s.setText("Exception retrieving name");
                    }
                }
            }
        });
        list.addKeyListener(new java.awt.event.KeyAdapter(){
            public void keyReleased(java.awt.event.KeyEvent e){
                int key=e.getKeyCode();
                switch(key){
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        if(list.getSelectedIndex()!=-1){
                            s.hideList();
                            try{
                                String text=((ComboBoxTopicWrapper)list.getSelectedValue()).topic.getBaseName();
                                s.setText(text);
                            }catch(TopicMapException tme){
                                tme.printStackTrace(); // TODO EXCEPTION;
                                s.setText("Exception retrieving name");
                            }
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        s.hideList();
                        break;
                    case KeyEvent.VK_UP: case KeyEvent.VK_DOWN: case KeyEvent.VK_PAGE_DOWN: case KeyEvent.VK_PAGE_UP:
                        break;
                    default:
                        char c=e.getKeyChar();
                        s.hideList();
                        if(c!=e.CHAR_UNDEFINED && !Character.isISOControl(c)){
                            s.setText(written+c);
                        }
                        break;
                }
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setEnabled(true);
        this.setUndecorated(true);
        this.getContentPane().add(scroller);
        this.pack();
        this.setLocation(selector.getLocationOnScreen().x,selector.getLocationOnScreen().y+selector.getHeight());
        this.show();
///        this.toFront();
    }
}