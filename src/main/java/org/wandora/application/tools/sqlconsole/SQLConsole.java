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
 * SQLConsole.java
 *
 * Created on 29. joulukuuta 2004, 14:56
 */

package org.wandora.application.tools.sqlconsole;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;

import org.wandora.application.gui.UIBox;
import org.wandora.utils.Options;
import org.wandora.utils.RegexFileChooser;
/**
 *
 * @author  olli
 */
public class SQLConsole extends javax.swing.JDialog {

	private static final long serialVersionUID = 1L;

	private Options options;
        
    /** Creates new form SQLConsole */
    public SQLConsole(Options options) {
        //super(parent, modal);
        this.options=options;
        initComponents();
        menuBar.add(UIBox.makeMenu(new Object[]{
            "SQL-Konsoli",new Object[]{
                "Tuo",
                "Tallenna nimell�",
                "---",
                "Exit"
            }
        },
        new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e){
                String cmd=e.getActionCommand();
                if(cmd.equalsIgnoreCase("Tuo")){
                    importQueries();
                }
                else if(cmd.equalsIgnoreCase("Tallenna nimell�")){
                    exportQueries();
                }
                else if(cmd.equalsIgnoreCase("Exit")){
                    setVisible(false);
                }
            }
        }));
        try{
            ((SQLConsolePanel)consolePanel).loadStoredQueries();
        }catch(Exception e){
            SQLConsole.reportException(e);
        }
        //this.setLocation(kirjava.getX()+kirjava.getWidth()/2-this.getWidth()/2,kirjava.getY()+kirjava.getHeight()/2-this.getHeight()/2);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        consolePanel = new SQLConsolePanel(options);
        menuBar = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SQL-Konsoli");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        getContentPane().add(consolePanel, java.awt.BorderLayout.CENTER);

        setJMenuBar(menuBar);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-680)/2, (screenSize.height-567)/2, 680, 567);
    }//GEN-END:initComponents

    public void exportQueries(){
        QuerySelector querySelector=new QuerySelector(true,((SQLConsolePanel)consolePanel).getStoredQueries(),"Valitse tallennettavat lausekkeet");
        querySelector.setVisible(true);
        Map<String,StoredQuery> selectedQueries=querySelector.getSelection();
        if(selectedQueries!=null){
            JFileChooser fc=new JFileChooser();
            fc.addChoosableFileFilter(RegexFileChooser.suffixChooser("xml","XML files(*.xml)"));
            String path=(String)options.get("options.sqlconsole.export.path");
            if(path!=null){
                File f=new File(path);
                if(f.exists()) fc.setCurrentDirectory(f);
            }
            if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
                File dir=fc.getCurrentDirectory();
                options.put("options.sqlconsole.export.path",dir.getAbsolutePath());
                try{
                    StoredQuery.saveStoredQueries(selectedQueries, fc.getSelectedFile().getAbsolutePath());
                }catch(IOException ioe){
                    SQLConsole.reportException(ioe);
                }
            }
        }        
    }
    
    public void importQueries(){
        JFileChooser fc=new JFileChooser();
        fc.addChoosableFileFilter(RegexFileChooser.suffixChooser("xml","XML files(*.xml)"));
        String path=(String)options.get("options.sqlconsole.import.path");
        if(path!=null){
            File f=new File(path);
            if(f.exists()) fc.setCurrentDirectory(f);
        }
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            File dir=fc.getCurrentDirectory();
            options.put("options.sqlconsole.import.path",dir.getAbsolutePath());
            try{
                Map<String,StoredQuery> loadedQueries=StoredQuery.loadStoredQueries(fc.getSelectedFile().getAbsolutePath());
                QuerySelector querySelector=new QuerySelector(true,loadedQueries,"Valitse tuotavat lausekkeet");
                querySelector.setVisible(true);
                Map<String,StoredQuery> selectedQueries=querySelector.getSelection();
                if(selectedQueries!=null){
                    ((SQLConsolePanel)consolePanel).importQueries(selectedQueries);
                    ((SQLConsolePanel)consolePanel).updateSimpleView();
                }
            }catch(Exception e){
                SQLConsole.reportException(e);
            }
        }
    }
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try{
            ((SQLConsolePanel)consolePanel).saveStoredQueries();
        }catch(java.io.IOException ioe){
            SQLConsole.reportException(ioe);
        }
    }//GEN-LAST:event_formWindowClosing
    
    public void connect(String driver,String connectString,String user,String password){
        ((SQLConsolePanel)consolePanel).connect(driver,connectString,user,password);
    }
    
    public static void reportException(Exception e){
        e.printStackTrace();
        //new KirjavaExceptionDialog(parent,true,e).setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel consolePanel;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
    
}
