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
 * EmailExtractorPanel.java
 *
 * Created on 5. heinäkuuta 2005, 14:43
 */

package org.wandora.application.tools.extractors.email;



import org.wandora.utils.Delegate;
import org.wandora.utils.XMLbox;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.regex.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.PictureView;
import com.sun.mail.pop3.POP3Folder;
import org.wandora.utils.*;

/**
 *
 * @author  olli
 */
public class EmailExtractorPanel extends javax.swing.JPanel {
    
    private DefaultTableModel tableModel;
    private Vector<BufferedImage> fullImages;
    private Vector<String> uids;
    private Vector<String> senders;
    private HashMap<String,Integer> uidMap;
    private java.awt.Frame parent;
    
    private String protocol="pop3";
    private String host="localhost";
    private String user="wandora";
    private String pass="wandora";
    private String msgFolder="INBOX";
    private String port="110";
    
    private String[] columnNames;
    private Class[] columnClasses;
    private boolean[] columnEditable;
    private String[] columnXMLKeys;    
    
    private boolean tryXMLExtract;
    
    private Delegate<Delegate.Void,EmailExtractorPanel> okHandler;
    private Delegate<Delegate.Void,EmailExtractorPanel> cancelHandler;
    
    private int popupRow=-1;
    
    /** Creates new form EmailExtractorPanel */
    public EmailExtractorPanel(java.awt.Frame parent,Delegate<Delegate.Void,EmailExtractorPanel> okHandler,Delegate<Delegate.Void,EmailExtractorPanel> cancelHandler,String host,String port,String user,String pass) {
        this(parent,okHandler,cancelHandler,host,port,user,pass,new Object[]{"Message",String.class,true,null});
    }
    /*
     * Additional columns are given with four elements per column:
     *      Column name,column class,editable,xml key for column.
     * Column name is the name shown to user in the table and also key for data in extracted Hashmap. 
     * Column class is the java class of the data,
     * usually <code>String.class</code>. Editable is boolean indicating whether the column is editable.
     * XML key is the key used to get data for column from message xml or null if data is not taken
     * from message xml but left blank instead.
     * Column name "Message" is a special case and is used to get the original message of the email.
     */
    public EmailExtractorPanel(java.awt.Frame parent,Delegate<Delegate.Void,EmailExtractorPanel> okHandler,Delegate<Delegate.Void,EmailExtractorPanel> cancelHandler,String host,String port,String user,String pass,Object[] additionalColumns) {
        this.host=host;
        this.port=port;
        this.user=user;
        this.pass=pass;
        this.okHandler=okHandler;
        this.cancelHandler=cancelHandler;
        this.parent=parent;
        
        this.columnNames=new String[4+additionalColumns.length/4];
        this.columnClasses=new Class[columnNames.length];
        this.columnEditable=new boolean[columnNames.length];
        this.columnXMLKeys=new String[columnNames.length];
        this.columnNames[0]="Import"; this.columnNames[1]="Delete"; this.columnNames[2]="Sent"; this.columnNames[3]="Image";
        this.columnClasses[0]=this.columnClasses[1]=Boolean.class; this.columnClasses[2]=Date.class; this.columnClasses[3]=ImageIcon.class;
        this.columnEditable[0]=this.columnEditable[1]=this.columnEditable[3]=true; this.columnEditable[2]=false;
        this.tryXMLExtract=false;
        
        for(int i=0;i+3<additionalColumns.length;i+=4){
            this.columnNames[4+i/4]=(String)additionalColumns[i];
            this.columnClasses[4+i/4]=(Class)additionalColumns[i+1];
            this.columnEditable[4+i/4]=((Boolean)additionalColumns[i+2]).booleanValue();
            this.columnXMLKeys[4+i/4]=(String)additionalColumns[i+3];
            if(columnXMLKeys[4+i/4]!=null) tryXMLExtract=true;
        }
        Object[] objectNames=new Object[this.columnNames.length];
        for(int i=0;i<this.columnNames.length;i++) objectNames[i]=this.columnNames[i];
        
        this.tableModel=new DefaultTableModel( 
                /*new Object[]{"Import","Delete","Sent","Image","Message"}*/objectNames,0){
            public Class getColumnClass(int columnIndex){
/*                switch(columnIndex){
                    case 0: return Boolean.class;
                    case 1: return Boolean.class;
                    case 2: return Date.class;
                    case 3: return ImageIcon.class;
                    case 4: return String.class;
                }*/
                if(columnIndex<columnClasses.length) return columnClasses[columnIndex];
                return null;
            }
            public boolean isCellEditable(int rowIndex,int columnIndex){
/*                switch(columnIndex){
                    case 0: return true;
                    case 1: return true;
                    case 2: return false;
                    case 3: return true;
                    case 4: return true;
                }           */
                if(columnIndex<columnEditable.length) return columnEditable[columnIndex];
                return false;
            }
        };
        initComponents();
        table.setRowHeight(50);        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        int picwidth=(int)(50.0*4.0/3.0+10.0);
        table.getColumnModel().getColumn(3).setPreferredWidth(picwidth);
        table.getColumnModel().getColumn(3).setMaxWidth(picwidth);
        table.getColumnModel().getColumn(3).setCellEditor(new ImageViewerCellEditor());
        table.setComponentPopupMenu(popupMenu);
    }
    
    private class ImageViewerCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;
        private ImageIcon currentImage;
        private BufferedImage fullImage;
        public ImageViewerCellEditor(){
            button=new JButton();
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    PictureView pv=new PictureView(parent,true,fullImage);
                    pv.setVisible(true);
                    fireEditingStopped();
                }
            });
            button.setBorderPainted(false);
        }
        public Object getCellEditorValue(){
            return currentImage;
        }
        public java.awt.Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column){
            currentImage=(ImageIcon)value;
            fullImage=fullImages.get(row);
            return button;
        }
    }
    
    public static class ImageTableRenderer extends JComponent {
        private java.awt.Image img;
        public ImageTableRenderer(){}
        public void setImage(java.awt.Image img){
            this.img=img;
        }
        public void paint(java.awt.Graphics g){
            g.drawImage(img,0,0,this);
        }
    }
    
    public static class EmailSession{
        public Session session;
        public Folder folder;
        public Store store;
        public EmailSession(Session session,Store store,Folder folder){
            this.session=session;
            this.store=store;
            this.folder=folder;
        }
        public void close(boolean expunge) throws MessagingException {
            folder.close(expunge);
            store.close();
        }
        public Message[] getMessages() throws MessagingException {
            int numMessages=folder.getMessageCount();
            Message[] msgs=folder.getMessages(1,numMessages);            
            return msgs;
        }
        public int getNumMessages() throws MessagingException {
            return folder.getMessageCount();
        }
        public int getNumNewMessages() throws MessagingException {
            return folder.getNewMessageCount();            
        }
    }
    
    public EmailSession openEmailSession() throws MessagingException {
        System.out.println("Connecting to mail server");
        Properties mailSessionProps=new Properties();
        mailSessionProps.setProperty("mail.store.protocol",protocol);
        mailSessionProps.setProperty("mail."+protocol+".host",host);
        mailSessionProps.setProperty("mail."+protocol+".port",port);
        mailSessionProps.setProperty("mail."+protocol+".user",user);
        Session session=Session.getDefaultInstance(mailSessionProps);
        session.setDebug(false);
        URLName url=new URLName(protocol,host,-1,null,user,pass);
        Store store=session.getStore(url);
        store.connect();
        Folder folder=store.getDefaultFolder();
        folder=folder.getFolder(msgFolder);
        folder.open(Folder.READ_WRITE);
        return new EmailSession(session,store,folder);
    }
    
    public void getMessages() throws Exception {
        tableModel.setRowCount(0);
        fullImages=new Vector<BufferedImage>();
        uids=new Vector<String>();
        uidMap=new HashMap<String,Integer>();
        senders=new Vector<String>();
        
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                messageLabel.setText("Connecting to mail server");
            }
        });
        EmailSession session=openEmailSession();
        
        final int numMessages=session.getNumMessages();
        int newMessages=session.getNumNewMessages();
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                progressBar.setMinimum(0);
                progressBar.setMaximum(numMessages);
                progressBar.setValue(0);
                messageLabel.setText("Fetching "+numMessages+" messages");
            }
        });
        System.out.println("Fetching "+numMessages+" messages");
        Message[] msgs=session.getMessages();
        FetchProfile prof=new FetchProfile();
        prof.add(FetchProfile.Item.ENVELOPE);
        prof.add(FetchProfile.Item.CONTENT_INFO);
        session.folder.fetch(msgs,prof);
        for(int i=0;i<msgs.length;i++){
            final int progress=i;
            java.awt.EventQueue.invokeLater(new Runnable(){
                public void run(){
                    progressBar.setValue(progress);
                }
            });
            Object c=msgs[i].getContent();
            String uid=msgs[i].getHeader("message-ID")[0];
            boolean finalText=false;
            if(c instanceof MimeMultipart){
                try{
                    MimeMultipart mm=(MimeMultipart)c;
                    Object[] row=new Object[columnNames.length];
                    row[0]=Boolean.FALSE; row[1]=Boolean.FALSE;
                    BufferedImage img=null;
                    row[2]=msgs[i].getSentDate();
                    String message="";

                    for(int j=0;j<mm.getCount();j++){
                        BodyPart bp=mm.getBodyPart(j);
                        String contentType=bp.getContentType();
                        if(contentType.startsWith("image/")){
                            System.out.println("found image");
                            img=ImageIO.read(bp.getInputStream());
                            int height=50;
                            int width=(int)(img.getWidth()*(double)height/(double)img.getHeight());
                            BufferedImage thumb=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                            thumb.getGraphics().drawImage(img,0,0,width,height,null);
                            row[3]=new ImageIcon(thumb);
                        }
                        else if(!finalText && contentType.startsWith("text/plain")){
                            String[] cl=msgs[i].getHeader("Content-Location");
                            if(cl!=null && cl.length>0 && cl[0]!=null && cl[0].toUpperCase().endsWith(".txt")) finalText=true;
                            message=bp.getContent().toString();
                        }
                    }
                    if(row[3]!=null){
                        Hashtable table=null;
                        if(tryXMLExtract){
                            try{
                                org.w3c.dom.Document doc=XMLbox.getDocument(message);
                                table=XMLbox.xml2Hash(doc);
                            }catch(Exception e){}
                        }
                        for(int j=4;j<columnNames.length;j++){
                            if(columnNames[j].equalsIgnoreCase("message")) row[j]=message;
                            else if(columnXMLKeys[j]!=null && table!=null) row[j]=table.get(columnXMLKeys[j]);
                            if(row[j]==null) row[j]="";
                        }
                        
                        tableModel.addRow(row);
                        fullImages.add(img);
                        uids.add(uid);
                        uidMap.put(uid,uids.size()-1);
                        String from="";
                        for(Address a : msgs[i].getFrom()){
                            if(from.length()>0) from+=", ";
                            from+=a.toString();
                        }
                        senders.add(from);
                        System.out.println("UID: "+uid);
                    }
                    else msgs[i].setFlag(Flags.Flag.DELETED,true);
                }catch(Exception e){
                    e.printStackTrace();
                    msgs[i].setFlag(Flags.Flag.DELETED,true);
                }
            }
            else msgs[i].setFlag(Flags.Flag.DELETED,true);
        }
        session.close(true);
    }
    
    public Collection<Map<String,Object>> getSelectedData() {
        Collection<Map<String,Object>> selectedData=new Vector<Map<String,Object>>();
        for(int i=0;i<tableModel.getRowCount();i++){
            if(((Boolean)tableModel.getValueAt(i, 0)).booleanValue()){
                Object sent=tableModel.getValueAt(i,2);
                Object image=fullImages.elementAt(i);
                Map<String,Object> map=new HashMap<String,Object>();
                map.put("id",uids.elementAt(i));
                map.put("sent",sent);
                map.put("image",image);
                map.put("sender",senders.elementAt(i));
                for(int j=4;j<columnNames.length;j++){
                    map.put(columnNames[j],tableModel.getValueAt(i,j));
                }
                selectedData.add(map);
            }
        }
        return selectedData;
    }
    
    public void deleteSelected() throws Exception {
        fullImages=new Vector<BufferedImage>();
        uids=new Vector<String>();
        EmailSession session=openEmailSession();
        Message[] msgs=session.getMessages();
        FetchProfile prof=new FetchProfile();
        prof.add(FetchProfile.Item.ENVELOPE);
        session.folder.fetch(msgs,prof);
        int count=0;
        for(int i=0;i<msgs.length;i++){
            Object c=msgs[i].getContent();
            String uid=msgs[i].getHeader("message-ID")[0];            
            Integer index=uidMap.get(uid);
            if(index==null) continue;
            if(((Boolean)tableModel.getValueAt(index.intValue(), 1)).booleanValue()){
                msgs[i].setFlag(Flags.Flag.DELETED, true);
                count++;
            }
        }
        System.out.println("Deleting "+count+" messages");
        session.close(true);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        messageToolPanel = new javax.swing.JPanel();
        importAllButton = new javax.swing.JButton();
        importNoneButton = new javax.swing.JButton();
        deleteAllButton = new javax.swing.JButton();
        deleteNoneButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        popupMenu = new javax.swing.JPopupMenu();
        menuItemRotRight = new javax.swing.JMenuItem();
        menuItemRotLeft = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        toolPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        jPanel3 = new javax.swing.JPanel();
        cancelButton2 = new javax.swing.JButton();
        getMessagesButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        messageToolPanel.setLayout(new java.awt.GridBagLayout());

        importAllButton.setText("Import All");
        importAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importAllButtonActionPerformed(evt);
            }
        });

        messageToolPanel.add(importAllButton, new java.awt.GridBagConstraints());

        importNoneButton.setText("Import None");
        importNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importNoneButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        messageToolPanel.add(importNoneButton, gridBagConstraints);

        deleteAllButton.setText("Delete All");
        deleteAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        messageToolPanel.add(deleteAllButton, gridBagConstraints);

        deleteNoneButton.setText("Delete None");
        deleteNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNoneButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        messageToolPanel.add(deleteNoneButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        messageToolPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        messageToolPanel.add(cancelButton, gridBagConstraints);

        saveButton.setText("Save Image");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        messageToolPanel.add(saveButton, gridBagConstraints);

        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                popupMenuPopupMenuWillBecomeVisible(evt);
            }
        });

        menuItemRotRight.setText("Rotate right");
        menuItemRotRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRotRightActionPerformed(evt);
            }
        });

        popupMenu.add(menuItemRotRight);

        menuItemRotLeft.setText("Rotate left");
        menuItemRotLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRotLeftActionPerformed(evt);
            }
        });

        popupMenu.add(menuItemRotLeft);

        setLayout(new java.awt.GridBagLayout());

        table.setModel(tableModel);
        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jScrollPane1, gridBagConstraints);

        toolPanel.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 5, 30);
        jPanel2.add(progressBar, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        cancelButton2.setText("Cancel");
        cancelButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel3.add(cancelButton2, gridBagConstraints);

        getMessagesButton.setText("Get Messages");
        getMessagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getMessagesButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel3.add(getMessagesButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(messageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel3, gridBagConstraints);

        toolPanel.add(jPanel2, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        add(toolPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void popupMenuPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_popupMenuPopupMenuWillBecomeVisible
        int row=table.rowAtPoint(table.getMousePosition());
        popupRow=row;
    }//GEN-LAST:event_popupMenuPopupMenuWillBecomeVisible

    private void menuItemRotLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRotLeftActionPerformed
        if(popupRow!=-1){
            BufferedImage oldImg=fullImages.elementAt(popupRow);
            BufferedImage newImg=new BufferedImage(oldImg.getHeight(),oldImg.getWidth(),BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2=(java.awt.Graphics2D)newImg.getGraphics();
            java.awt.geom.AffineTransform t=new java.awt.geom.AffineTransform(0,-1,1,0,0,oldImg.getWidth());
            g2.drawImage(oldImg,t,null);
            fullImages.setElementAt(newImg,popupRow);
            
            int height=50;
            int width=(int)(newImg.getWidth()*(double)height/(double)newImg.getHeight());
            BufferedImage thumb=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            thumb.getGraphics().drawImage(newImg,0,0,width,height,null);
            table.getModel().setValueAt(new ImageIcon(thumb), popupRow, 3);
        }
    }//GEN-LAST:event_menuItemRotLeftActionPerformed

    private void menuItemRotRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRotRightActionPerformed
        if(popupRow!=-1){
            BufferedImage oldImg=fullImages.elementAt(popupRow);
            BufferedImage newImg=new BufferedImage(oldImg.getHeight(),oldImg.getWidth(),BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2=(java.awt.Graphics2D)newImg.getGraphics();
            java.awt.geom.AffineTransform t=new java.awt.geom.AffineTransform(0,1,-1,0,oldImg.getHeight(),0);
            g2.drawImage(oldImg,t,null);
            fullImages.setElementAt(newImg,popupRow);
            
            int height=50;
            int width=(int)(newImg.getWidth()*(double)height/(double)newImg.getHeight());
            BufferedImage thumb=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            thumb.getGraphics().drawImage(newImg,0,0,width,height,null);
            table.getModel().setValueAt(new ImageIcon(thumb), popupRow, 3);
            
        }        
    }//GEN-LAST:event_menuItemRotRightActionPerformed

    private void cancelButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButton2ActionPerformed
        cancelHandler.invoke(this);
    }//GEN-LAST:event_cancelButton2ActionPerformed

    private void getMessagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getMessagesButtonActionPerformed
        getMessagesButton.setEnabled(false);
        Thread t=new Thread(){
            public void run(){
                try{
                    getMessages();
                    java.awt.EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            if(fullImages.size()==0) messageLabel.setText("No messages");
                            else{
                                toolPanel.removeAll();
                                toolPanel.add(messageToolPanel);
                                toolPanel.revalidate();
                            }
                        }
                    });
                    
                }catch(Exception e){
                    e.printStackTrace();
                    messageLabel.setText("Error: "+e.getMessage());
                }
            }
        };
        t.start();
    }//GEN-LAST:event_getMessagesButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        okHandler.invoke(this);
        /*        try{
            deleteSelected();
        }catch(Exception e){
            e.printStackTrace();
        }*/
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelHandler.invoke(this);
//        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        int[] rs=table.getSelectedRows();
        if(rs.length!=1) return;
        BufferedImage img=fullImages.get(rs[0]);
        SimpleFileChooser fc=UIConstants.getFileChooser();
        fc.setMultiSelectionEnabled(false);
        int s=fc.open(this, SimpleFileChooser.SAVE_DIALOG);
        if(s==SimpleFileChooser.APPROVE_OPTION){
            try{
                String file=fc.getSelectedFile().getName();
                int ind=file.lastIndexOf(".");
                if(ind==-1){
                    WandoraOptionPane.showMessageDialog(this,"File name must end in a valid image suffix (e.g jpg,png)");                    
                    return;
                }
                String suffix=file.substring(ind+1).toLowerCase();
                boolean success=ImageIO.write(img,suffix,fc.getSelectedFile());
                if(!success){
                    WandoraOptionPane.showMessageDialog(this,"File format "+suffix+" not supported.", WandoraOptionPane.ERROR_MESSAGE);                    
                    return;
                }
            }catch(IOException ioe){
                ioe.printStackTrace();
                WandoraOptionPane.showMessageDialog(this,"Error saving image: "+ioe.getMessage(), WandoraOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void deleteNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNoneButtonActionPerformed
        for(int i=0;i<tableModel.getRowCount();i++){
            tableModel.setValueAt(new Boolean(false), i, 1);
        }
    }//GEN-LAST:event_deleteNoneButtonActionPerformed

    private void deleteAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllButtonActionPerformed
        for(int i=0;i<tableModel.getRowCount();i++){
            tableModel.setValueAt(new Boolean(true), i, 1);
        }
    }//GEN-LAST:event_deleteAllButtonActionPerformed

    private void importNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importNoneButtonActionPerformed
        for(int i=0;i<tableModel.getRowCount();i++){
            tableModel.setValueAt(new Boolean(false), i, 0);
        }
    }//GEN-LAST:event_importNoneButtonActionPerformed

    private void importAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importAllButtonActionPerformed
        for(int i=0;i<tableModel.getRowCount();i++){
            tableModel.setValueAt(new Boolean(true), i, 0);
        }
    }//GEN-LAST:event_importAllButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton cancelButton2;
    private javax.swing.JButton deleteAllButton;
    private javax.swing.JButton deleteNoneButton;
    private javax.swing.JButton getMessagesButton;
    private javax.swing.JButton importAllButton;
    private javax.swing.JButton importNoneButton;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem menuItemRotLeft;
    private javax.swing.JMenuItem menuItemRotRight;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel messageToolPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JPanel toolPanel;
    // End of variables declaration//GEN-END:variables
    
}
