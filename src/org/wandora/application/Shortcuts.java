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
 * 
 * Shortcuts.java
 *
 * Created on 13. tammikuuta 2005, 15:01
 */

package org.wandora.application;


import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;


/**
 * Shortcuts is a subject identifier storage implementing
 * Wandora's shortcuts menu and a manager for shortcuts. Subject identifiers
 * stored into shortcuts are stored to Wandora's options.
 *
 * @see org.wandora.utils.Options
 * @author  akivela
 */
public class Shortcuts implements ActionListener {
    public static final String OPTIONS_PREFIX = "shortcuts.";
    
    private ArrayList shortcuts;
    private Wandora wandora;
    
    private ManageDialog manageDialog = null;
    
    
    
    private Object[] accelerators = new Object[] {
        KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
    };
    Object[] staticMenuStructure = new Object[] {
        "Add shortcut", UIBox.getIcon("gui/icons/shortcut_new.png"),
        "Manage shortcuts...", UIBox.getIcon("gui/icons/shortcuts_configure.png"),
        "---"
    };
    Object[] staticFullMenuStructure = new Object[] {
        "Shortcuts",
        staticMenuStructure
    };


    ShortcutMenuListener shortcutMenuListener = new ShortcutMenuListener();
    
    
    
    /** Creates a new instance of Shortcuts */
    public Shortcuts(Wandora w) {
        this.wandora = w;
        shortcuts = new ArrayList();
        loadShortcuts(wandora.options);
    }

    
    public void addShortcut(String shortcut) {
        if(shortcuts.contains(shortcut)) shortcuts.remove(shortcut);
        shortcuts.add(0, shortcut);
    }
    
    
    public void manage() {
        manageDialog=new ManageDialog(wandora, false, this);
        manageDialog.setSize(600, 400);
        wandora.centerWindow(manageDialog);
        manageDialog.setVisible(true);
    }
    
    
    
   
    public JMenu getShortcutsMenu(JMenu shortcutsMenu)  throws TopicMapException {
        shortcutsMenu.removeAll();
        UIBox.attachMenu(shortcutsMenu, staticMenuStructure, this);
        UIBox.attachMenu(shortcutsMenu, getMenuStructure(), shortcutMenuListener);
        return shortcutsMenu;
    }
    
    
    public JMenu getShortcutsMenu()  throws TopicMapException {
        return UIBox.attachMenu(UIBox.makeMenu(staticFullMenuStructure, this), getMenuStructure(), shortcutMenuListener);
    }
    

    
    public Object[] getMenuStructure() throws TopicMapException {
        ArrayList shortcutMenuStructure = new ArrayList();
        Topic t = null;
        TopicMap topicMap = wandora.getTopicMap();
        if(topicMap != null) {
            String name = null;
            int acceleratorCount = 0;

            for(Iterator iter=shortcuts.iterator(); iter.hasNext(); ) {
                String si = (String) iter.next();
                t = null;
                try{
                    t=topicMap.getTopic(si);
                }
                catch(TopicMapException tme) {
                    tme.printStackTrace();
                    //admin.handleError(tme);
                }

                if(t != null) {
                    name = t.getBaseName();
                    if(name != null && name.length() > 0) {
                        shortcutMenuStructure.add(name);
                    }
                    else {
                        shortcutMenuStructure.add(si);
                    }
                }
                else {
                    shortcutMenuStructure.add(si);
                }
                if(acceleratorCount < accelerators.length) {
                    shortcutMenuStructure.add(accelerators[acceleratorCount++]);
                }
                shortcutMenuStructure.add(UIBox.getIcon("gui/icons/shortcut.png"));
            }
        }
        return shortcutMenuStructure.toArray();
    }
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
 
        if("Add shortcut".equalsIgnoreCase(c)) {
            Topic t=wandora.getOpenTopic();
            if(t != null) {
                Collection tsis=null;
                try {
                    tsis=t.getSubjectIdentifiers();
                    if(tsis!=null && tsis.size() > 1) {
                        Object[] tsisArray = tsis.toArray();
                        Object answer = WandoraOptionPane.showOptionDialog(wandora, "Topic contains multiple subject identifiers. Select subject identifier for the shortcut.", "Select subject identifier", WandoraOptionPane.QUESTION_MESSAGE, tsisArray, tsisArray[0]);
                        if(answer == null) return;
                        else {
                            String si=answer.toString();
                            addShortcut(si);
                        }
                    }
                    else {
                        Locator si = (Locator) (tsis.iterator().next());
                        addShortcut(si.toExternalForm());
                    }
                    wandora.shortcutsChanged();
                }
                catch(Exception e) {
                    if(wandora != null) wandora.handleError(e);
                    else e.printStackTrace();
                    return;
                }
                if(wandora != null) saveShortcuts(wandora.options);
            }
        }
        
        
        else if("Manage shortcuts...".equalsIgnoreCase(c)) {
            manage();
        }
       
        else {
            // Should not be here!
        }
    }
    
    
    
    public void loadShortcuts(Options opts) {
        int i = 0;
        boolean loadOk = true;
        String shortcut = null;
        while(loadOk) {
            shortcut = opts.get(OPTIONS_PREFIX + "si["+i+"]");
            if(shortcut != null && shortcut.length() > 0) {
                if(!shortcuts.contains(shortcut)) shortcuts.add(shortcut);
                i++;
            }
            else {
                loadOk = false;
            }
        }
    }
    
    
    
    public void loadShortcuts(String filename) {
        try {
            String shortcutString = IObox.loadFile(filename);
            parseShortcuts(shortcutString);
        }
        catch (Exception e) {
            System.out.println("Exception occurred '" + e.toString() + "' while loading shortcuts from '" + filename + "'!");
            if(wandora != null) wandora.handleError(e);
            else e.printStackTrace();
        }
    }
    
    
    public void parseShortcuts(String shortcutString) {
        try {
            StringTokenizer st = new StringTokenizer(shortcutString, "\n");
            while(st.hasMoreTokens()) {
                String shortcut = Textbox.trimExtraSpaces(st.nextToken());
                if(shortcut != null && shortcut.length() > 0) {
                    if(!shortcuts.contains(shortcut)) shortcuts.add(shortcut);
                }
            }
        }
        catch (Exception e) {
            System.out.println("Exception occurred '" + e.toString() + "' while parsing shortcuts!");
            wandora.handleError(e);
        }
    }
    
    
    
    public void saveShortcuts(String fileName) {
        saveShortcuts(new File(fileName));
    }
    
    public void saveShortcuts(File file) {
        try {
            OutputStream fos=new FileOutputStream(file);
            PrintWriter writer=new PrintWriter(fos);
            Iterator iter=shortcuts.iterator();
            while(iter.hasNext()){
                Object o=iter.next();
                writer.print(o.toString()+ "\n");
            }
            writer.close();
        }
        catch(Exception e) {
            //System.out.println("Exception occurred '" + e.toString() + "' while saving shortcuts to '" + file.getPath() + "'!");
            WandoraOptionPane.showMessageDialog(wandora, "Exception occurred '" + e.toString() + "' while saving shortcuts to '" + file.getPath() + "'!", WandoraOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    public void saveShortcuts(Options opts) {
        try {
            int i=0;
            Iterator iter=shortcuts.iterator();
            while(iter.hasNext()) {
                Object o=iter.next();
                opts.put(OPTIONS_PREFIX + "si["+i+"]", o.toString());
                i++;
            }
        }
        catch(Exception e) {
            //System.out.println("Exception occurred '" + e.toString() + "' while saving shortcuts to options!");
            WandoraOptionPane.showMessageDialog(wandora,"Exception occurred '" + e.toString() + "' while saving shortcuts to options!");
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // ---- Shortcut menu listener opens topics selected from menu. ------------
    // -------------------------------------------------------------------------
    
    
    private class ShortcutMenuListener implements ActionListener {
        
        
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            try {
                // Expecting action command to be si or base name!
                String c = actionEvent.getActionCommand();
                Topic t = null;
                TopicMap topicMap = wandora.getTopicMap();
                boolean updateMenu = false;
                
                t = topicMap.getTopicWithBaseName(c);   // base name
                if(t == null) {
                    t = topicMap.getTopic(c); // subject identifier
                    updateMenu = true;
                }
                if(t == null) {
                    wandora.applyChangesAndOpen(new Locator(c));
                }
                else {
                    wandora.applyChangesAndOpen(t);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                wandora.handleError(e);
            }
        }
        
    }
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // --- Shortcut's manage dialog --------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    private class ManageDialog extends JDialog implements ActionListener {
        
        
        Object[] fileMenuStructure = new Object[] {
            "File",
                new Object[] {
                    "Import...",
                    "Merge...",
                    "Export...",
                    "---",
                    "Close",
            }
        };
        
        Object[] editMenuStructure = new Object[] {
            "Edit",
                new Object[] {
                    "Open",
                    "---",
                    "Cut",
                    "Copy",
                    "Paste",
                    "---",
                    "Delete",
                    "---",
                    "Move up",
                    "Move down",
                    "Move top",
                    "Move bottom"
            }
        };
        
        
        JList list = null;
        Wandora wandora = null;
        Shortcuts parent = null;
        
        
        
        public ManageDialog(Wandora w, boolean modal, Shortcuts parent) {
            super((JFrame) w, modal);
            this.setTitle("Manage shortcuts");
            this.wandora = w;
            this.parent = parent;
            
            JMenuBar menuBar = new JMenuBar();
            menuBar.add(UIBox.makeMenu(fileMenuStructure, this));
            menuBar.add(UIBox.makeMenu(editMenuStructure, this));
            setJMenuBar(menuBar);
            
            this.getContentPane().setLayout(new java.awt.BorderLayout());
            list=new SimpleList(shortcuts.toArray());
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.setFont(UIConstants.plainFont);
            list.setComponentPopupMenu(getPopupMenu(this));
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setPreferredSize(new java.awt.Dimension(500,300));
            scrollPane.setViewportView(list);
            
            this.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
            JPanel panel=new JPanel();
            
            JButton okButton=new SimpleButton("Close");
            okButton.setFont(UIConstants.buttonLabelFont);
            okButton.setPreferredSize(new Dimension(80, 23));
            okButton.setActionCommand("Close");
            okButton.addActionListener(this);
            
            JButton importButton=new SimpleButton("Import");
            importButton.setPreferredSize(new Dimension(80, 23));
            importButton.setFont(UIConstants.buttonLabelFont);
            importButton.setActionCommand("Import");
            importButton.addActionListener(this);
            
            JButton saveButton=new SimpleButton("Export");
            saveButton.setPreferredSize(new Dimension(80, 23));
            saveButton.setFont(UIConstants.buttonLabelFont);
            saveButton.setActionCommand("Export");
            saveButton.addActionListener(this);
            
            JButton removeButton=new SimpleButton("Delete");
            removeButton.setFont(UIConstants.buttonLabelFont);
            removeButton.setPreferredSize(new Dimension(80, 23));
            removeButton.setActionCommand("Delete");
            removeButton.addActionListener(this);
            
            JButton openButton=new SimpleButton("Open");
            openButton.setPreferredSize(new Dimension(80, 23));
            openButton.setFont(UIConstants.buttonLabelFont);
            openButton.setActionCommand("Open");
            openButton.addActionListener(this);
            
            panel.add(openButton);
            panel.add(removeButton);
            panel.add(importButton); 
            panel.add(saveButton); 
            panel.add(okButton);

            this.getContentPane().add(panel,java.awt.BorderLayout.SOUTH);
            this.pack();
        }
        
        
        
        
        public void update() {
            list.setListData(shortcuts.toArray());
            list.clearSelection();
            repaint();
            if(wandora != null) saveShortcuts(wandora.options);
        }
        
        
        
        public JPopupMenu getPopupMenu(ActionListener listener) {
            Object[] menuStructure = new Object[] {
                "Open",
                "---",
                "Cut",
                "Copy",
                "Paste",
                "---",
                "Delete",
                "---",
                "Move up",
                "Move down",
                "Move top",
                "Move bottom"
            };
            return UIBox.makePopupMenu(menuStructure, listener);
        }
        
        
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            String c = actionEvent.getActionCommand();
            
             if("Move up".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                for(int i=0; i<selected.length; i++) {
                    shortcuts.add(selected[i]-1 < 0 ? 0 : selected[i]-1, shortcuts.get(selected[i]));
                    shortcuts.remove(selected[i]+1);
                }
                update();
            }
            else if("Move down".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                for(int i=selected.length-1; i>=0; i--) {
                    shortcuts.add(selected[i]+2 >= shortcuts.size() ? shortcuts.size() : selected[i]+2, shortcuts.get(selected[i]));
                    shortcuts.remove(selected[i]);
                }
                update();
            }
            else if("Move bottom".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                int p = 0;
                for(int i=selected.length-1; i>=0; i--) {
                    shortcuts.add(shortcuts.size()-p, shortcuts.get(selected[i]));
                    p++;
                    shortcuts.remove(selected[i]);
                }
                update();
            }
             if("Move top".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                for(int i=0; i<selected.length; i++) {
                    shortcuts.add(i, shortcuts.get(selected[i]));
                    shortcuts.remove(selected[i]+1);
                }
                update();
            }
            if("Cut".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                StringBuilder sb = new StringBuilder("");
                for(int i=0; i<selected.length; i++) {
                    String si = (String) shortcuts.get(selected[i]);
                    sb.append(si).append("\n");
                }
                ClipboardBox.setClipboard(sb.toString());
                for(int i=selected.length-1; i>=0; i--) {
                    shortcuts.remove(selected[i]);
                }
                update();
            }
            else if("Copy".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                StringBuilder sb = new StringBuilder("");
                for(int i=0; i<selected.length; i++) {
                    String si = (String) shortcuts.get(selected[i]);
                    sb.append(si).append("\n");
                }
                ClipboardBox.setClipboard(sb.toString());
            }
            else if("Paste".equalsIgnoreCase(c)) {
                String shortcutString = ClipboardBox.getClipboard();
                parseShortcuts(shortcutString);
                update();
            }
            else if("Delete".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                for(int i=selected.length-1;i>=0;i--){
                    shortcuts.remove(selected[i]);
                }
                update();
            }
            else if("Export".equalsIgnoreCase(c) || "Export...".equalsIgnoreCase(c)) {
                JFileChooser chooser=new JFileChooser();
                chooser.setDialogTitle("Export shortcuts...");
                if(wandora != null) {
                    String currentDirectoryString = wandora.options.get("current.directory");
                    if(currentDirectoryString != null) {
                        chooser.setCurrentDirectory(new File(currentDirectoryString));
                    }
                }
                if(chooser.showDialog(this, "Export")==JFileChooser.APPROVE_OPTION) {
                    if(wandora != null) {
                        wandora.options.put("current.directory", chooser.getCurrentDirectory().getPath());
                    }
                    saveShortcuts(chooser.getSelectedFile().getPath());
                }
            }
            else if("Import".equalsIgnoreCase(c) || "Import...".equalsIgnoreCase(c)) {
                JFileChooser chooser=new JFileChooser();
                chooser.setDialogTitle("Import shortcuts...");
                if(wandora != null) {
                    String currentDirectoryString = wandora.options.get("current.directory");
                    if(currentDirectoryString != null) {
                        chooser.setCurrentDirectory(new File(currentDirectoryString));
                    }
                }
                if(chooser.showDialog(this, "Import")==JFileChooser.APPROVE_OPTION){
                    shortcuts = new ArrayList();
                    if(wandora != null) {
                        wandora.options.put("current.directory", chooser.getCurrentDirectory().getPath());
                    }
                    loadShortcuts(chooser.getSelectedFile().getPath());
                }
                update();
            }
            else if("Merge".equalsIgnoreCase(c) || "Merge...".equalsIgnoreCase(c)) {
                JFileChooser chooser=new JFileChooser();
                chooser.setDialogTitle("Merge shortcuts...");
                if(wandora != null) {
                    String currentDirectoryString = wandora.options.get("current.directory");
                    if(currentDirectoryString != null) {
                        chooser.setCurrentDirectory(new File(currentDirectoryString));
                    }
                }
                if(chooser.showDialog(this, "Merge")==JFileChooser.APPROVE_OPTION){
                    if(wandora != null) {
                        wandora.options.put("current.directory", chooser.getCurrentDirectory().getPath());
                    }
                    loadShortcuts(chooser.getSelectedFile().getPath());
                }
                update();
            }
            else if("Open".equalsIgnoreCase(c)) {
                int[] selected=manageDialog.list.getSelectedIndices();
                for(int i=selected.length-1;i>=0;i--) {
                    wandora.openTopic(new Locator((String) shortcuts.get(selected[i])));
                }
                manageDialog.repaint();
            }
            else if("Close".equalsIgnoreCase(c)) {
                this.setVisible(false);
                try {
                    wandora.shortcutsChanged();
                }
                catch(TopicMapException tme) {
                    tme.printStackTrace();
                    wandora.handleError(tme);
                }
            }
        }
        
        
    }

}


