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
 * 
 * PictureImportDialog.java
 *
 * Created on July 15, 2004, 9:00 AM
 */

package org.wandora.application.tools;


import org.wandora.piccolo.WandoraManager;
import org.wandora.application.tools.oldies.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import java.util.*;

/**
 *
 * @author  olli
 */
public class PictureImportDialog extends javax.swing.JDialog {

    private WandoraAdminManager manager;
    private Wandora parent;
    
    private javax.swing.JTextField baseNameAutoCopy;
    
    private Collection keywords;
    
    private Hashtable nameTable;
    private Hashtable captionTable;
    
    private boolean tvImageCrop;
    
    private java.awt.image.BufferedImage loadedImage;
    
    /** Creates new form PictureImportDialog */
    public PictureImportDialog(Wandora parent, boolean modal, boolean tvImageCrop) throws TopicMapException  {
        super(parent, modal);
        this.tvImageCrop=tvImageCrop;
        initComponents();
        keywords=new HashSet();
        this.parent=parent;
        
        nameTable=new Hashtable();
        variantPanel.removeAll();
        String[] langs=TMBox.getLanguageSIs(parent.getTopicMap());
        String[] vers=TMBox.getNameVersionSIs(parent.getTopicMap());
        Topic[] langTopics=parent.getTopicMap().getTopics(langs);
        Topic dispTopic=parent.getTopicMap().getTopic(XTMPSI.DISPLAY);
        variantPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc;
        for(int i=0;i<langs.length;i++){
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=1+i;
            gbc.gridy=0;
            gbc.fill=gbc.NONE;
            gbc.weightx=1.0;
            variantPanel.add(new javax.swing.JLabel(langTopics[i].getDisplayName("en")),gbc);
        }
        gbc=new java.awt.GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=1;
        gbc.fill=gbc.NONE;
        gbc.weightx=1.0;
        variantPanel.add(new javax.swing.JLabel(dispTopic.getDisplayName("en")),gbc);
        for(int j=0;j<langs.length;j++){
            HashSet s=new HashSet(); s.add(dispTopic); s.add(langTopics[j]);
            javax.swing.JTextField field=new javax.swing.JTextField("");
            field.setPreferredSize(new java.awt.Dimension(130,19));

            nameTable.put(s,field);
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=1+j;
            gbc.gridy=1;
            gbc.fill=gbc.HORIZONTAL;
            gbc.weightx=1.0;
            variantPanel.add(field,gbc);
            
            
            if(langs[j].equals(XTMPSI.getLang("en"))){
                baseNameAutoCopy=field;
                field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                    public void changedUpdate(javax.swing.event.DocumentEvent e){
                        autoSetBaseName();
                    }
                    public void insertUpdate(javax.swing.event.DocumentEvent e){
                        autoSetBaseName();                        
                    }
                    public void removeUpdate(javax.swing.event.DocumentEvent e){
                        autoSetBaseName();                        
                    }
                });
/*                field.addKeyListener(new java.awt.event.KeyAdapter(){
                    public void keyTyped(java.awt.event.KeyEvent e){
                        autoSetBaseName();
                    }
                });*/
            }
        }
        
        captionPanel.setLayout(new java.awt.GridBagLayout());
        Topic captionTopic=manager.getWorkspace().getTopic("http://wandora.org/si/common/caption");
        captionTable=new Hashtable();
        for(int i=0;i<langs.length;i++){
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=i;
            gbc.gridy=0;
            gbc.fill=gbc.NONE;
            gbc.weightx=1.0;
            captionPanel.add(new javax.swing.JLabel(langTopics[i].getDisplayName("en")),gbc);
        }
        ((javax.swing.border.TitledBorder)captionPanel.getBorder()).setTitle(captionTopic.getDisplayName("en"));
        for(int j=0;j<langs.length;j++){
//            javax.swing.JTextField field=new javax.swing.JTextField("");
            final javax.swing.JTextArea field=new javax.swing.JTextArea("");
            javax.swing.JScrollPane sp=new javax.swing.JScrollPane();
            field.setWrapStyleWord(true);
            field.setLineWrap(true);
            field.addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyPressed(java.awt.event.KeyEvent e){
                    if(e.getKeyCode()==e.VK_TAB){
                        e.consume();
                        field.transferFocus();
                    }
                }
            });

            captionTable.put(langTopics[j],field);
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=j;
            gbc.gridy=1;
            gbc.fill=gbc.BOTH;
            gbc.weightx=1.0;
            gbc.weighty=1.0;
            sp.setViewportView(field);
            captionPanel.add(sp,gbc);
            
        }

        this.setLocation(parent.getLocation().x+parent.getWidth()/2-this.getWidth()/2, 
                         parent.getLocation().y+parent.getHeight()/2-this.getHeight()/2);
        
        fileTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void fireEvent(){
                java.io.File file=new java.io.File(fileTextField.getText().trim());
                if(file.exists()){
                    updatePreview();
                }
                else{
                    loadedImage=null;
                    previewPanel.repaint();
                }
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e){
                fireEvent();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e){
                fireEvent();                
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e){
                fireEvent();                
            }
        });
        updateKeywordPanel();
    }
    
    public void autoSetBaseName(){
        if(autoBaseNameCheckBox.isSelected() && baseNameAutoCopy!=null){
            StringBuffer sb=new StringBuffer(baseNameAutoCopy.getText());
            for(int i=0;i<sb.length();i++){
                char c=sb.charAt(i);
                if(!Character.isLetterOrDigit(c)){
                    sb.setCharAt(i, '_');
                }
            }
            baseNameTextField.setText(sb.toString());
        }
    }
    
    public void updateKeywordPanel() throws TopicMapException {
        keywordPanel.removeAll();
        keywordPanel.setLayout(new java.awt.GridBagLayout());
        int counter=0;
        Iterator iter=keywords.iterator();
        java.awt.GridBagConstraints gbc;
        while(iter.hasNext()){
            final Topic keyword=(Topic)iter.next();
            javax.swing.JLabel label=new javax.swing.JLabel(keyword.getDisplayName("en"));
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=counter;
            gbc.weightx=1.0;
            keywordPanel.add(label,gbc);
            javax.swing.JButton button=new javax.swing.JButton("Remove");
            button.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent event){
                    keywords.remove(keyword);
                    try{
                        updateKeywordPanel();
                    }catch(TopicMapException tme){
                        tme.printStackTrace(); // TODO EXCEPTION
                    }
                }
            });
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=1;
            gbc.gridy=counter;
            gbc.weightx=1.0;
            keywordPanel.add(button,gbc);
            counter++;
        }
        keywordPanel.invalidate();
        // TODO: try to figure out how to do this properly (set the height so that everything will fit in the container)
        int minHeight=basePanel.getHeight()+variantPanel.getHeight()+captionPanel.getHeight()+counter*24+50;
        jPanel2.setMinimumSize(new java.awt.Dimension(500,minHeight));
        jPanel2.setPreferredSize(new java.awt.Dimension(500,minHeight));
        jPanel2.invalidate();
        // this.validateTree(); // TRIGGERS EXCEPTION IN JAVA 1.7
        this.repaint();
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        addKeyword = new javax.swing.JButton();
        keywordPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        previewPanel = new javax.swing.JPanel(){
            public void paint(java.awt.Graphics g){
                super.paint(g);
                if(loadedImage!=null){
                    double sx=(double)this.getWidth()/(double)loadedImage.getWidth();
                    double sy=(double)this.getHeight()/(double)loadedImage.getHeight();
                    if(sx<sy) sy=sx;
                    else sx=sy;
                    g.drawImage(loadedImage,0,0,(int)(loadedImage.getWidth()*sx),(int)(loadedImage.getHeight()*sy),null);
                }
            }
        };
        captionPanel = new javax.swing.JPanel();
        variantPanel = new javax.swing.JPanel();
        basePanel = new javax.swing.JPanel();
        autoBaseNameCheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        fileTextField = new javax.swing.JTextField();
        baseNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setMinimumSize(new java.awt.Dimension(500, 300));
        jPanel2.setPreferredSize(new java.awt.Dimension(500, 300));
        jPanel7.setLayout(new java.awt.GridBagLayout());

        jPanel7.setBorder(new javax.swing.border.TitledBorder("Keywords"));
        addKeyword.setText("Add");
        addKeyword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addKeywordActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanel7.add(addKeyword, gridBagConstraints);

        keywordPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel7.add(keywordPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jPanel7, gridBagConstraints);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(new javax.swing.border.TitledBorder("Preview"));
        jPanel3.setMinimumSize(new java.awt.Dimension(150, 150));
        jPanel3.setPreferredSize(new java.awt.Dimension(150, 150));
        previewPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                previewPanelMouseClicked(evt);
            }
        });

        jPanel3.add(previewPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(jPanel3, gridBagConstraints);

        captionPanel.setLayout(new java.awt.BorderLayout());

        captionPanel.setBorder(new javax.swing.border.TitledBorder("Caption"));
        captionPanel.setMinimumSize(new java.awt.Dimension(300, 200));
        captionPanel.setPreferredSize(new java.awt.Dimension(300, 200));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(captionPanel, gridBagConstraints);

        variantPanel.setBorder(new javax.swing.border.TitledBorder("Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(variantPanel, gridBagConstraints);

        basePanel.setLayout(new java.awt.GridBagLayout());

        autoBaseNameCheckBox.setSelected(true);
        autoBaseNameCheckBox.setText("Auto");
        autoBaseNameCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoBaseNameCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        basePanel.add(autoBaseNameCheckBox, gridBagConstraints);

        jLabel2.setText("File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        basePanel.add(jLabel2, gridBagConstraints);

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        basePanel.add(browseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        basePanel.add(fileTextField, gridBagConstraints);

        baseNameTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        basePanel.add(baseNameTextField, gridBagConstraints);

        jLabel1.setText("BaseName");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        basePanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(basePanel, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel2);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel4.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel4.add(cancelButton);

        jPanel1.add(jPanel4, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        setBounds(0, 0, 684, 475);
    }//GEN-END:initComponents

    private void previewPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_previewPanelMouseClicked
        if(loadedImage==null) updatePreview();
        else{
            if(loadedImage!=null){
                PictureView pv=new PictureView(parent,true,loadedImage);
                pv.setVisible(true);
            }
        }
    }//GEN-LAST:event_previewPanelMouseClicked

    private void addKeywordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeywordActionPerformed
/*        BaseNamePrompt prompt=new BaseNamePrompt(parent.getManager(),parent,false,"http://wandora.org/si/common/keyword",true);
        prompt.setVisible(true);
        Topic t=prompt.getTopic();*/
        try{
            Topic t=parent.showTopicFinder();                
            if(t!=null){
                if(!keywords.contains(t)){
                    keywords.add(t);
                    updateKeywordPanel();
                }
            }
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
        
    }//GEN-LAST:event_addKeywordActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    public static final int THUMB_WIDTH=88;
    public static final int THUMB_HEIGHT=66;
    
    public boolean doTopics() throws TopicMapException {
        autoSetBaseName();
        updatePreview(); // also loads image
        
        if(loadedImage==null){
            WandoraOptionPane.showMessageDialog(this, "Couldn't load image file!", WandoraOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // make thumbnail
        double sx=(double)THUMB_WIDTH/(double)loadedImage.getWidth();
        double sy=(double)THUMB_HEIGHT/(double)loadedImage.getHeight();
        if(sx>sy) sy=sx;
        else sx=sy;
        
        java.awt.image.BufferedImage thumbnailImage=new java.awt.image.BufferedImage(THUMB_WIDTH,THUMB_HEIGHT,java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics g=thumbnailImage.getGraphics();
        int scaledw=(int)(sx*loadedImage.getWidth()+0.5);
        int scaledh=(int)(sy*loadedImage.getHeight()+0.5);
        final Object notifyThis=this;
        synchronized(this){
            if(!g.drawImage(loadedImage,-(scaledw-THUMB_WIDTH)/2,-(scaledh-THUMB_HEIGHT)/2,scaledw,scaledh,new java.awt.image.ImageObserver(){
                public boolean imageUpdate(java.awt.Image image,int infoflags,int x,int y,int width,int height){
                    if((infoflags&java.awt.image.ImageObserver.ALLBITS)!=0){
                        synchronized(notifyThis){
                            notifyThis.notify();
                        }
                        return false;
                    }
                    return true;
                }
            })){
                try{
                    this.wait();
                }catch(InterruptedException e){ e.printStackTrace();}
            }
        }
        
        // make tv image
        sx=(double)768/(double)loadedImage.getWidth();
        sy=(double)576/(double)loadedImage.getHeight();
        if(tvImageCrop){
            if(sx<sy) sy=sx;
            else sx=sy;
        }
        else{
            if(sx>sy) sy=sx;
            else sx=sy;            
        }

        java.awt.image.BufferedImage tvImage=new java.awt.image.BufferedImage(768,576,java.awt.image.BufferedImage.TYPE_INT_RGB);
        g=tvImage.getGraphics();
        scaledw=(int)(sx*loadedImage.getWidth()+0.5);
        scaledh=(int)(sy*loadedImage.getHeight()+0.5);
        final Object notifyThis2=this;
        synchronized(this){
            if(!g.drawImage(loadedImage,-(scaledw-768)/2,-(scaledh-576)/2,scaledw,scaledh,new java.awt.image.ImageObserver(){
                public boolean imageUpdate(java.awt.Image image,int infoflags,int x,int y,int width,int height){
                    if((infoflags&java.awt.image.ImageObserver.ALLBITS)!=0){
                        synchronized(notifyThis2){
                            notifyThis2.notify();
                        }
                        return false;
                    }
                    return true;
                }
            })){
                try{
                    this.wait();
                }catch(InterruptedException e){e.printStackTrace();}
            }
        }
        
        //upload original image
        java.io.File imageFile=new java.io.File(fileTextField.getText());
        String imageURL=null;
        try {
            imageURL=manager.upload(new java.io.FileInputStream(imageFile),
                                            UploadFileOld.makeFileName(imageFile.getAbsolutePath()),
                                            imageFile.length());
            if(imageURL==null){
                WandoraOptionPane.showMessageDialog(this,"Couldn't upload image file!", WandoraOptionPane.ERROR_MESSAGE);
                return false;            
            }
        } catch(java.io.IOException ioe){
            WandoraOptionPane.showMessageDialog(this, "Couldn't read image. "+ioe.getMessage(), WandoraOptionPane.ERROR_MESSAGE);
            return false;                        
        } catch(ServerException se){
            parent.handleError(se);
            return false;
        }

        // upload thumbnail
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        try{
            javax.imageio.ImageIO.write(thumbnailImage,"jpeg",baos);
        }catch(java.io.IOException ioe){
            WandoraOptionPane.showMessageDialog(this, "Couldn't make thumbnail. "+ioe.getMessage(), WandoraOptionPane.ERROR_MESSAGE);
            return false;                                    
        }
        
        String thumbFileName=imageFile.getAbsolutePath();
        int ind=thumbFileName.lastIndexOf(".");
        if(ind!=-1)
            thumbFileName=thumbFileName.substring(0,ind)+"_tn"+thumbFileName.substring(ind);
        else
            thumbFileName=""+System.currentTimeMillis()+(int)(Math.random()*1000000)+"_tn.jpg";
        
        byte[] thumbBytes=baos.toByteArray();
        String thumbURL=null;
        try{
            thumbURL=manager.upload(new java.io.ByteArrayInputStream(thumbBytes), 
                                            UploadFileOld.makeFileName(thumbFileName),
                                            thumbBytes.length);
        }catch(ServerException se){
            parent.handleError(se);
            return false;
        }

        if(thumbURL==null){
            WandoraOptionPane.showMessageDialog(this,"Couldn't upload thumbnail", WandoraOptionPane.ERROR_MESSAGE);
            return false;            
        }
        
        // upload tv image
        baos=new java.io.ByteArrayOutputStream();
        try{
            javax.imageio.ImageIO.write(tvImage,"jpeg",baos);
        }catch(java.io.IOException ioe){
            WandoraOptionPane.showMessageDialog(this,"Couldn't create tv image. "+ioe.getMessage(), WandoraOptionPane.ERROR_MESSAGE);
            return false;                                    
        }
        
        String tvFileName=imageFile.getAbsolutePath();
        ind=tvFileName.lastIndexOf(".");
        if(ind!=-1)
            tvFileName=tvFileName.substring(0,ind)+"_tv"+tvFileName.substring(ind);
        else
            tvFileName=""+System.currentTimeMillis()+(int)(Math.random()*1000000)+"_tv.jpg";
        
        byte[] tvBytes=baos.toByteArray();
        String tvURL=null;
        try{
            tvURL=manager.upload(new java.io.ByteArrayInputStream(tvBytes), 
                                        UploadFileOld.makeFileName(tvFileName),
                                        tvBytes.length);
        } catch(ServerException se){
            parent.handleError(se);
            return false;
        }

        if(tvURL==null){
            WandoraOptionPane.showMessageDialog(this,"Couldn't upload tv image!", WandoraOptionPane.ERROR_MESSAGE);
            return false;            
        }
        
        // create topics
        
        Topic hideLevel = manager.getWorkspace().getTopic(WandoraManager.HIDELEVEL_SI);
        Topic langIndep = manager.getWorkspace().getTopic(WandoraManager.LANGINDEPENDENT_SI);
        
        Topic entryTime = manager.getWorkspace().getTopic(WandoraManager.ENTRYTIME_SI);
        
        java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        Topic picture = manager.getWorkspace().createTopic();
        Topic pictureType = manager.getWorkspace().getTopic("http://wandora.org/si/common/picture");
        picture.addSubjectIdentifier(picture.getTopicMap().createLocator(picture.getTopicMap().makeSubjectIndicator()));
        picture.addType(pictureType);
        picture.setBaseName(baseNameTextField.getText());
        picture.setData(entryTime,langIndep, sdf.format(new Date()) );
        
        Iterator iter=nameTable.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Set scope=(Set)e.getKey();
            javax.swing.text.JTextComponent field=(javax.swing.text.JTextComponent)e.getValue();
            picture.setVariant(scope,field.getText());
        }
        
        Topic captionTopic=manager.getWorkspace().getTopic("http://wandora.org/si/common/caption");
        iter=captionTable.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Topic version=(Topic)e.getKey();
            javax.swing.text.JTextComponent field=(javax.swing.text.JTextComponent)e.getValue();
            if(field.getText().trim().length()>0) picture.setData(captionTopic,version, field.getText().trim());
        }
        
        Topic occurrence = manager.getWorkspace().createTopic();
        Topic occurrenceType = manager.getWorkspace().getTopic("http://wandora.org/si/common/imageoccurrence");
        occurrence.addSubjectIdentifier(occurrence.getTopicMap().createLocator(occurrence.getTopicMap().makeSubjectIndicator()));
        occurrence.setBaseName(picture.getBaseName()+" (image occurrence)");
        occurrence.addType(occurrenceType);
        occurrence.setSubjectLocator(occurrence.getTopicMap().createLocator(imageURL));
        occurrence.setData(hideLevel,langIndep, "1");
        
        Topic thumbnail = manager.getWorkspace().createTopic();
        Topic thumbnailType = manager.getWorkspace().getTopic("http://wandora.org/si/common/thumbnail");
        thumbnail.addSubjectIdentifier(thumbnail.getTopicMap().createLocator(thumbnail.getTopicMap().makeSubjectIndicator()));
        thumbnail.setBaseName(picture.getBaseName()+" (thumbnail)");
        thumbnail.addType(thumbnailType);
        thumbnail.setSubjectLocator(thumbnail.getTopicMap().createLocator(thumbURL));
        thumbnail.setData(hideLevel,langIndep, "1");

        Topic tv = manager.getWorkspace().createTopic();
        Topic tvType = manager.getWorkspace().getTopic("http://wandora.org/si/common/tvimage");
        tv.addSubjectIdentifier(tv.getTopicMap().createLocator(tv.getTopicMap().makeSubjectIndicator()));
        tv.setBaseName(picture.getBaseName()+" (tv)");
        tv.addType(tvType);
        tv.setSubjectLocator(tv.getTopicMap().createLocator(tvURL));
        tv.setData(hideLevel,langIndep, "1");
        
        
        Association pictureOccurrence=manager.getWorkspace().createAssociation(occurrenceType);
        pictureOccurrence.addPlayer(picture,pictureType);
        pictureOccurrence.addPlayer(occurrence,occurrenceType);
        
        Association occurrenceThumbnail=manager.getWorkspace().createAssociation(thumbnailType);
        occurrenceThumbnail.addPlayer(occurrence,occurrenceType);
        occurrenceThumbnail.addPlayer(thumbnail,thumbnailType);

        Association occurrenceTV=manager.getWorkspace().createAssociation(tvType);
        occurrenceTV.addPlayer(occurrence,occurrenceType);
        occurrenceTV.addPlayer(tv,tvType);
        
        Topic keywordType = manager.getWorkspace().getTopic("http://wandora.org/si/common/keyword");
        iter=keywords.iterator();
        while(iter.hasNext()){
            Topic keyword=(Topic)iter.next();
            Association a=manager.getWorkspace().createAssociation(keywordType);
            a.addPlayer(keyword,keywordType);
            a.addPlayer(picture,pictureType);
        }
        
        return true;
    }
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        try{
            if(doTopics()) this.setVisible(false);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void updatePreview(){
        java.io.File file=new java.io.File(fileTextField.getText());
        if(file.exists()){
            try{
                loadedImage = javax.imageio.ImageIO.read(file);
            }catch(java.io.IOException e){
                WandoraOptionPane.showMessageDialog(this,"IOException "+e.getMessage(), WandoraOptionPane.ERROR_MESSAGE);
                loadedImage=null;
            }
        }
        else{
            loadedImage = null;
        }
        previewPanel.repaint();
    }
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        if(chooser.open(this)==SimpleFileChooser.APPROVE_OPTION){
            fileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
//            updatePreview();
        }        
    }//GEN-LAST:event_browseButtonActionPerformed

    private void autoBaseNameCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoBaseNameCheckBoxActionPerformed
        baseNameTextField.setEnabled(!autoBaseNameCheckBox.isSelected());
        if(autoBaseNameCheckBox.isSelected()) autoSetBaseName();
    }//GEN-LAST:event_autoBaseNameCheckBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addKeyword;
    private javax.swing.JCheckBox autoBaseNameCheckBox;
    private javax.swing.JTextField baseNameTextField;
    private javax.swing.JPanel basePanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel captionPanel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel keywordPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JPanel variantPanel;
    // End of variables declaration//GEN-END:variables
    
}
