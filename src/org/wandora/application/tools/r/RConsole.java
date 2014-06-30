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
 */


package org.wandora.application.tools.r;


import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.utils.swing.GuiTools;




/**
 *
 * @author olli
 */


public class RConsole extends javax.swing.JPanel implements ActionListener {
    

    private ArrayList<String> history=new ArrayList<String>(); { history.add(""); }
    private int historyPtr=0;

    private boolean prompt=false;

    private boolean isBusy=false;
    private Rengine engine;
    private final LoopCallbacks callbacks=new LoopCallbacks();

    private static final String rErrorMessage=
            "Unable to initialize R.\n"+
            "\n"+
            "This is likely because R has not been installed or the environment\n"+
            "variables aren't setup correctly or the rJava library has not been\n"+
            "installed.\n"+
            "\n"+
            "To setup R do the following steps. These instructions are also\n"+
            "explained in more detail in the Wandora wiki at\n"+
            "http://wandora.org/wiki/R_in_Wandora\n"+
            "\n"+
            "1. Download R from http://www.r-project.org and install it.\n"+
            "\n"+
            "2. Install the required libraries in R. You need to run R as an\n"+
            "   administrator to do this. In Windows right click the R icon\n"+
            "   select Run as Adminsitrator. In Linux run R on console using\n"+
            "   \"sudo R\".\n"+
            "\n"+
            "   Then in the R console install the rJava package with\n"+
            "   install.packages(\"rJava\")\n"+
            "   You will likely also want to install the igraph package with\n"+
            "   install.packages(\"igraph\")\n"+
            "   and in Windows also the JavaGD packages\n"+
            "   install.packages(\"JavaGD\")\n"+
            "   You will be asked to select a mirror to download the packages\n"+
            "   from, just select the country you're in or one close to it.\n"+
            "\n"+
            "3. Next make sure that the environment variables are setup correctly.\n"+
            "   Open the SetR.bat (on Windows) or SetR.sh (on Linux) in the bin\n"+
            "   directory. Make sure the R_HOME directory is right. If you are\n"+
            "   using Windows also make sure that the version number matches your\n"+
            "   installation and that the R_ARCH matches your Java installation.\n"+
            "   Use i386 for 32-bit Java and x64 for 64-bit Java. If you did a\n"+
            "   standard installation of R then the other variables should be\n"+
            "   correct, otherwise adjust them as needed.\n"+
            "\n"+
            "You should now be able to use R in Wandora.\n"+
            "\n"+
            "The error encountered while trying to initiale R is shown below:\n\n"
            ;

    /** Creates new form RConsole */
    public RConsole() {
        initComponents();
        
        try{
            // this makes JRI behave gracefully when the native library isn't found
            System.setProperty("jri.ignore.ule", "yes");
            
            if(!Rengine.versionCheck()){
                throw new Exception("R version mismatch, Java files don't match library version.");
            }
            engine=new Rengine(new String[]{},false,callbacks);
            if(engine.waitForR()){
                initEngine();
            }
            else {
                throw new Exception("Couldn't load R");
            }
        }catch(UnsatisfiedLinkError e){
            engine=null;
//            Wandora.getWandora().handleError(new Exception(e));
//            output.append("Unable to initialize R");
            appendOutput(rErrorMessage);

            StringWriter sw=new StringWriter();
            PrintWriter pw=new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            appendOutput("\n\n"+sw.toString());

        }
        catch(Exception e){
            engine=null;
            //Wandora.getWandora().handleError(e);
            //output.append("Unable to initialize R");
            appendOutput(rErrorMessage);

            StringWriter sw=new StringWriter();
            PrintWriter pw=new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            appendOutput("\n\n"+sw.toString());
        }
    }
    
    
    

    private void appendOutput(String text){
        Document d=output.getDocument();
        try{
            d.insertString(d.getLength(), text, null);
            output.setCaretPosition(output.getDocument().getLength());
        }catch(BadLocationException ble){}
    }

    private final Object promptLock=new Object();
    private String promptIn=null;
    private String promptInput(String promptText){
        synchronized(promptLock){
            //output.append(promptText);
            appendOutput(promptText);
            prompt=true;
            promptIn=null;
            while(promptIn==null){
                try{
                    promptLock.wait(100);
                }catch(InterruptedException ie){return null;}
                engine.rniIdle();
            }
            prompt=false;
            return promptIn;
        }
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public Rengine getEngine(){
        return engine;
    }
    
    private void initEngine(){
        engine.eval("source(\"resources/conf/rinit.r\",TRUE)");
        engine.startMainLoop();
    }

    private static RConsole console;
    public static synchronized RConsole getConsole(){
        if(console!=null) return console;
        console=new RConsole();
        return console;
    }

    private static JDialog consoleDialog;
    public static synchronized JDialog getConsoleDialog(){
        if(consoleDialog!=null) return consoleDialog;

        RConsole console=getConsole();

        consoleDialog=new JDialog(Wandora.getWandora(),"R Console",false);
        consoleDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        consoleDialog.getContentPane().add(console);
        consoleDialog.setSize(800, 700);
        
        
        consoleDialog.setJMenuBar(console.consoleMenuBar);
        GuiTools.centerWindow(consoleDialog, Wandora.getWandora());

        return consoleDialog;
    }

    class LoopCallbacks implements RMainLoopCallbacks {

        public String rChooseFile(Rengine re, int newFile) {
            FileDialog fd = new FileDialog(Wandora.getWandora(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
            fd.setVisible(true);
            String res=null;
            if (fd.getDirectory()!=null) res=fd.getDirectory();
            if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
            return res;
        }

        public void rFlushConsole(Rengine re) {
            //output.setText("");
        }

        public void rLoadHistory(Rengine re, String filename) {
        }

        public void rSaveHistory(Rengine re, String filename) {
        }

        public String rReadConsole(Rengine re, String prompt, int addToHistory) {
            String ret=promptInput(prompt);
            return ret;
        }

        public void rBusy(Rengine re, int which) {
            synchronized(this){ isBusy=(which!=0); }
        }

        public void rShowMessage(Rengine re, String message) {
            //output2.append("message: "+message);
            appendOutput("message: "+message);
        }

        public void rWriteConsole(Rengine re, String text, int oType) {
            //output2.append(text);
            appendOutput(text);
        }

    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        consoleMenuBar = new javax.swing.JMenuBar();
        fileMenu = new SimpleMenu();
        closeMenuItem = new SimpleMenuItem();
        outputScrollPane = new SimpleScrollPane();
        output = new SimpleTextPane();
        jScrollPane2 = new SimpleScrollPane();
        input = new SimpleTextPane();
        evalButton = new SimpleButton();

        fileMenu.setText("File");
        fileMenu.setFont(UIConstants.menuFont);

        closeMenuItem.setFont(UIConstants.menuFont);
        closeMenuItem.setIcon(UIBox.getIcon("resources/gui/icons/exit.png"));
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        consoleMenuBar.add(fileMenu);

        setLayout(new java.awt.GridBagLayout());

        output.setEditable(false);
        output.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        outputScrollPane.setViewportView(output);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 240;
        gridBagConstraints.ipady = 65;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(outputScrollPane, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(23, 75));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(8, 75));

        input.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        input.setMaximumSize(new java.awt.Dimension(2147483647, 75));
        input.setMinimumSize(new java.awt.Dimension(6, 75));
        input.setPreferredSize(new java.awt.Dimension(6, 75));
        input.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                inputKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(input);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jScrollPane2, gridBagConstraints);

        evalButton.setText("Evaluate");
        evalButton.setMaximumSize(new java.awt.Dimension(75, 75));
        evalButton.setMinimumSize(new java.awt.Dimension(75, 75));
        evalButton.setPreferredSize(new java.awt.Dimension(75, 75));
        evalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evalButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(evalButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void evalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evalButtonActionPerformed
        synchronized( callbacks ){ if(isBusy) return; }
        synchronized(promptLock){
            String in=input.getText();

            history.set(history.size()-1,in);
            history.add("");
            historyPtr=history.size()-1;

            input.setText("");
            //output2.append(in+"\n");
            appendOutput(in+"\n");
            if(prompt){
                promptIn=in+"\n";
                promptLock.notifyAll();
            }
            return;
        }

    }//GEN-LAST:event_evalButtonActionPerformed

    private void inputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            evalButtonActionPerformed(null);
            evt.consume();
        }
        else if(evt.getKeyCode()==KeyEvent.VK_UP){
            if(historyPtr>0){
                if(historyPtr==history.size()-1)
                    history.set(history.size()-1,input.getText());
                historyPtr--;
                input.setText(history.get(historyPtr));
            }
        }
        else if(evt.getKeyCode()==KeyEvent.VK_DOWN){
            if(historyPtr<history.size()-1) {
                historyPtr++;
                input.setText(history.get(historyPtr));
            }
        }
    }//GEN-LAST:event_inputKeyPressed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        consoleDialog.setVisible(false);
    }//GEN-LAST:event_closeMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuBar consoleMenuBar;
    private javax.swing.JButton evalButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextPane input;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane output;
    private javax.swing.JScrollPane outputScrollPane;
    // End of variables declaration//GEN-END:variables

    
    
    

    
    
    
}
