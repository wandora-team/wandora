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
 * RConsole2.java
 *
 * Created on Aug 26, 2011, 9:19:18 PM
 */

package org.wandora.application.tools.r;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTextConsole;
import org.wandora.application.gui.simple.SimpleTextConsoleListener;
import org.wandora.application.tools.ExecBrowser;
import org.wandora.utils.swing.GuiTools;


/**
 *
 * @author akivela
 */




public class RConsole2 extends javax.swing.JPanel implements ActionListener, SimpleTextConsoleListener, RBridgeListener {
    

	private static final long serialVersionUID = 1L;

	private SimpleTextConsole simpleTextConsole = null;
    
    protected static Object[] fileMenuStruct = new Object[] {
        "Import...", UIBox.getIcon("gui/icons/file_open.png"),
        "Export...", UIBox.getIcon("gui/icons/file_save.png"),
        //"Export input...",
        "---",
        "Close", UIBox.getIcon("gui/icons/exit.png"),
    };
    
    protected static Object[] editMenuStruct = new Object[] {
        "Cut", UIBox.getIcon("gui/icons/cut.png"),
        "Copy", UIBox.getIcon("gui/icons/copy.png"),
        "Paste", UIBox.getIcon("gui/icons/paste.png"),
        "Clear", UIBox.getIcon("gui/icons/clear.png"),
    };
    
    protected static Object[] viewMenuStruct = new Object[] {
        "Font size 9", UIBox.getIcon("gui/icons/font.png"),
        "Font size 10", UIBox.getIcon("gui/icons/font.png"),
        "Font size 12", UIBox.getIcon("gui/icons/font.png"),
        "Font size 14", UIBox.getIcon("gui/icons/font.png"),
        "Font size 16", UIBox.getIcon("gui/icons/font.png"),
        "Font size 18", UIBox.getIcon("gui/icons/font.png"),
        "---",
        "Inverse colors"
    };
    
    protected static Object[] helpMenuStruct = new Object[] {
        "R manuals", UIBox.getIcon("gui/icons/open_browser.png"), new ExecBrowser("http://cran.r-project.org/manuals.html"),
    };
    

    private static JDialog consoleDialog;
    private RBridge rBridge = null;

    

    
    
    
    
    
    /** Creates new form RConsole2 */
    public RConsole2() {
        initComponents();
        simpleTextConsole = (SimpleTextConsole) consolePane;
        rBridge = RBridge.getRBridge();
        rBridge.addRBridgeListener(this);
    }
    
    
    

    @Override
    public void output(String text) {
        try {
            if(simpleTextConsole.isVisible()) {
                simpleTextConsole.output(text);
                simpleTextConsole.refresh();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    
    
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e == null) return;
        String c = e.getActionCommand();

        if(simpleTextConsole != null) {
            if("Import...".equalsIgnoreCase(c)) {
                simpleTextConsole.load();
            }
            if("Export...".equalsIgnoreCase(c)) {
                simpleTextConsole.save();
            }
            if("Export input...".equalsIgnoreCase(c)) {
                simpleTextConsole.saveInput();
            }
            else if("Close".equalsIgnoreCase(c)) {
                close();
            }
            else if("Cut".equalsIgnoreCase(c)) {
                simpleTextConsole.cut();
            }
            else if("Copy".equalsIgnoreCase(c)) {
                simpleTextConsole.copy();
            }
            else if("Paste".equalsIgnoreCase(c)) {
                simpleTextConsole.paste();
            }
            else if("Clear".equalsIgnoreCase(c)) {
                simpleTextConsole.clear();
            }
            else if("Refresh".equalsIgnoreCase(c)) {
                simpleTextConsole.refresh();
            }
            else if(c.startsWith("Font size ")) {
                String n = c.substring("Font size ".length());
                int s = Integer.parseInt(n);
                simpleTextConsole.setFontSize(s);
            }
            else if("Inverse colors".equalsIgnoreCase(c)) {
                Color color = simpleTextConsole.getBackground();
                simpleTextConsole.setBackground(simpleTextConsole.getForeground());
                simpleTextConsole.setForeground(color);
                simpleTextConsole.setCaretColor(color);
            }
        }
    }

    
    
    @Override
    public String handleInput(String input) {
        if(rBridge != null) {
            return rBridge.handleInput(input);
        }
        return "";
    }



    public static synchronized JDialog getConsoleDialog(){
        if(consoleDialog!=null) return consoleDialog;

        RConsole2 console=new RConsole2();

        consoleDialog=new JDialog(Wandora.getWandora(),"R console",false);
        consoleDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        consoleDialog.getContentPane().add(console);
        consoleDialog.setSize(800, 700);
        
        JMenuBar menubar = new JMenuBar();
        SimpleMenu fileMenu = new SimpleMenu("File");
        SimpleMenu editMenu = new SimpleMenu("Edit");
        SimpleMenu viewMenu = new SimpleMenu("View");
        SimpleMenu helpMenu = new SimpleMenu("Help");
        fileMenu.setIcon(null);
        editMenu.setIcon(null);
        viewMenu.setIcon(null);
        helpMenu.setIcon(null);
        UIBox.attachMenu(fileMenu, fileMenuStruct, console);
        UIBox.attachMenu(editMenu, editMenuStruct, console);
        UIBox.attachMenu(viewMenu, viewMenuStruct, console);
        UIBox.attachMenu(helpMenu, helpMenuStruct, console);
        menubar.add(fileMenu);
        menubar.add(editMenu);
        menubar.add(viewMenu);
        menubar.add(helpMenu);
        consoleDialog.setJMenuBar(menubar);
        
        //consoleDialog.setJMenuBar(console.consoleMenuBar);
        GuiTools.centerWindow(consoleDialog, Wandora.getWandora());

        return consoleDialog;
    }

    
    
    
    
    public static void close() {
        if(consoleDialog != null) {
            consoleDialog.setVisible(false);
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

        scrollPane = new SimpleScrollPane();
        consolePane = new SimpleTextConsole(this);

        setLayout(new java.awt.GridBagLayout());

        scrollPane.setViewportView(consolePane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane consolePane;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
