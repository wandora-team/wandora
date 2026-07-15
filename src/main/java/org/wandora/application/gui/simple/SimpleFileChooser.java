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
 * SimpleFileChooser.java
 *
 * Created on 9. huhtikuuta 2006, 19:15
 *
 */

package org.wandora.application.gui.simple;

import java.awt.Component;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JFileChooser;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;




/**
 *
 * @author akivela
 */
public class SimpleFileChooser extends JFileChooser {

    private static final long serialVersionUID = 1L;
   
    
    public SimpleFileChooser() {
        setLookAndFeel();
    }
    public SimpleFileChooser(String currentPath) {
        setLookAndFeel();
        File f = new File(currentPath);
        if(f.exists()) {
            this.setCurrentDirectory(f);
        }
    }

    
    public void setLookAndFeel() {
        /*
         * FileChooser.listFont
         * com.sun.java.plaf.windows.WindowsFileChooserUI
         */
        try {
            //this.setUI(new SimpleFileChooserUI(this));
            //System.out.println("SET UI: "+this.getUI());
        }
        catch(Exception e) { e.printStackTrace(); }
        catch(Error er) { er.printStackTrace(); }

    }
    
    
    public int open(Component parent, String buttonLabel) {
        return open(parent, this.OPEN_DIALOG, buttonLabel);
    }
    public int open(Component parent) {
        return open(parent, this.OPEN_DIALOG, null);
    }
    public int open(Component parent, int type) {
        return open(parent, type, null);
    }
    public int open(Component parent, int type, String buttonLabel) {
        /*
        LookAndFeel originalLookAndFeel = null;
        try {
            originalLookAndFeel = UIManager.getLookAndFeel();
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
            SwingUtilities.updateComponentTreeUI(this);
            this.validate();
        }
        catch (UnsupportedLookAndFeelException e) {
           // handle exception
        }
        catch (ClassNotFoundException e) {
           // handle exception
        }
        catch (InstantiationException e) {
           // handle exception
        }
        catch (IllegalAccessException e) {
           // handle exception
        }

         
        */ 
        
        
        // ***** RESTORE PREVIOUS DIRECTORY ******

        if(parent instanceof Wandora && parent != null) {
            String currentDirectory = ((Wandora) parent).options.get("current.directory");
            if(currentDirectory != null) {
                File f = new File(currentDirectory);
                if(f.exists()) {
                    this.setCurrentDirectory(f);
                }
            }
        }
        int answer = JFileChooser.CANCEL_OPTION;
        if(buttonLabel == null) {
            if(type == this.OPEN_DIALOG) {
                answer = this.showOpenDialog(parent);
            }
            else {
                answer = this.showSaveDialog(parent);
            }
        }
        else {
            answer = this.showDialog(parent, buttonLabel);
        }
         // ***** SAVE CURRENT DIRECTORY ******
        if(answer == JFileChooser.APPROVE_OPTION) {
            if(parent instanceof Wandora && parent != null) {
                ((Wandora) parent).options.put("current.directory", this.getCurrentDirectory().getPath());
            }
        }
        
        
        /*
        try {
            if(originalLookAndFeel != null) {
                UIManager.setLookAndFeel(originalLookAndFeel);
            }
        } 
        catch (UnsupportedLookAndFeelException e) {
           // handle exception
        }
        */
        
        
        return answer;
    }
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }








}
