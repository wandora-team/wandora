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
 * UploadFileOld.java
 *
 * Created on July 14, 2004, 3:09 PM
 */

package org.wandora.application.tools.oldies;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  olli
 */
public class UploadFileOld extends AbstractWandoraTool implements WandoraTool {
    
    private File file;
    
    /**
     * Creates a new instance of UploadFileOld
     */
    public UploadFileOld() {
        file=null;
    }
    
    public UploadFileOld(File file) {
        this.file=file;
    }
    
    public void execute(Wandora admin, Context context) {
        UploadDialog d=new UploadDialog(admin,true);
        d.setVisible(true);
/*        if(file==null){
            WandoraFileChooser chooser=new WandoraFileChooser();
            if(chooser.open(admin)==WandoraFileChooser.APPROVE_OPTION){
                file=chooser.getSelectedFile();
            }
        }
        if(file!=null){
            InfoThread infoThread=new InfoThread(admin);
            infoThread.start();
            while(!infoThread.dialog.isVisible()){
                Thread.yield();
            }
            // this will probably get executed from the ui thead so better not do anything big in this thread
            WorkerThread workerThread=new WorkerThread(admin,file,infoThread);
            workerThread.start();            
        }*/
    }
    
    public String getName() {
        return "Upload file to server";
    }
    public boolean requiresRefresh() {
        return false;
    }
    
    
    public static String makeFileName(String original){
        String extension=original;
        int ind=extension.lastIndexOf(".");
        if(ind!=-1 && extension.length()-ind<10) extension=extension.substring(ind);
        else extension="";
        String filename=""+System.currentTimeMillis()+((int)(Math.random()*100000))+extension;
        return filename;
    }
/*    
    private class WorkerThread extends Thread {
        private Wandora parent;
        private File file;
        private InfoThread info;
        public WorkerThread(Wandora parent,File file,InfoThread info){
            this.parent=parent;
            this.file=file;
            this.info=info;
        }
        public void run() {
            try{
                info.setMessage("Uploading file");
                InputStream in=new FileInputStream(file);
                WandoraAdminManager manager=parent.getManager();
                String filename=makeFileName(file.getAbsolutePath());
                String url=manager.upload(in,filename,file.length());
                if(url==null)
                    info.setMessage("Unable to upload file, see server log for details");
                else
                    info.setMessage("Done, external url to file: "+url);
                info.button.setEnabled(true);
                info.dialog.pack();
            }catch(IOException e){
                info.dialog.setVisible(false);
                WandoraOptionPane.showMessageDialog(parent,"Writing topic map failed: "+e.getMessage());
            }catch(ServerException se){
                info.dialog.setVisible(false);
                parent.getManager().handleServerError(se);
            }
        }        
    }
    
    
    private class InfoThread extends Thread {
        private Wandora parent;
        public javax.swing.JDialog dialog;
        public javax.swing.JLabel label;
        public javax.swing.JButton button;
        public InfoThread(Wandora parent){
            this.parent=parent;
            dialog=new javax.swing.JDialog(parent,"Processing",true);
            dialog.getContentPane().setLayout(new java.awt.BorderLayout());
            label=new javax.swing.JLabel("________________________________________________");
            label.setVisible(true);
            label.setForeground(java.awt.Color.BLACK);
            
            dialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
            dialog.getContentPane().add(label,java.awt.BorderLayout.CENTER);
            button=new javax.swing.JButton("OK");
            button.setEnabled(false);
            button.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    dialog.setVisible(false);
                }
            });
            dialog.getContentPane().add(button,java.awt.BorderLayout.SOUTH);
            dialog.setResizable(false);
            dialog.pack();            
            dialog.setLocation(parent.getWidth()/2-dialog.getWidth()/2,parent.getHeight()/2-dialog.getHeight()/2);
        }
        public void setMessage(String message){
            label.setText(message);
        }
        public void run() {
            dialog.show();
        }
    }*/
    
}
