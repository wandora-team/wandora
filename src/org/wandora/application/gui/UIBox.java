/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * UIBox.java
 *
 * Created on 23. marraskuuta 2004, 16:07
 */

package org.wandora.application.gui;




import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.imageio.*;
import javax.swing.*;

import org.wandora.utils.*;
import org.wandora.utils.swing.*;
import org.wandora.topicmap.layered.*;

import org.wandora.application.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;
import org.wandora.utils.Base64;



/**
 * UIBox is a library of user interface related helper methods. All methods are
 * static and require no class instance. Offered helper methods include menu
 * creation, button array creation, icon creation and cache, for example.
 *
 * @author  akivela
 */
public class UIBox {

    /** Creates a new instance of UIBox */
    public UIBox() {
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    public static JPopupMenu attachPopup(JPopupMenu base, Object[] struct, ActionListener defaultListener) {
        return makePopupMenu(base, struct, defaultListener);
    }

    
    /**
     * Takes an array of AdminTools and returns an array that can be used with
     * makeMenu and makePopupMenu methods. Essentially adds the name of each tool
     * before the tool itself, doubling the array length.
     */
    public static Object[] makeMenuStruct(WandoraTool[] tools){
        Object[] ret=new Object[tools.length*2];
        for(int i=0;i<tools.length;i++){
            ret[i*2]=tools[i].getName();
            ret[i*2+1]=tools[i];
        }
        return ret;
    }
    public static Object[] makeMenuStruct(Collection<? extends WandoraTool> tools){
        return makeMenuStruct(tools.toArray(new WandoraTool[tools.size()]));
    }
    
    public static PopupMenu makeAWTPopupMenu(PopupMenu menu, Object[] struct, ActionListener defaultListener) {
        if(struct != null) {
            if(struct.length > 0) {
                MenuItem menuItem = null;
                for(int i=0; i<struct.length; i++) {
                    if(struct[i] instanceof String) {
                        String menuName = (String) struct[i];
                        if(struct.length > i+1 && (struct[i+1] instanceof Object[] || struct[i+1] instanceof Collection) ) {
                            Menu submenu = new Menu();
                            if(menuName.startsWith("[") && menuName.endsWith("]")) {
                                submenu.setEnabled(false);
                                menuName = menuName.substring(1, menuName.length()-1);
                            }
                            submenu.setFont(UIConstants.menuFont);
                            submenu.setLabel(menuName); // submenu.setText(menuName);
                            submenu.setName(menuName);
                            //submenu.setIcon(UIBox.getIcon("gui/icons/empty.png"));
                            Object[] subStructure;
                            if(struct[i+1] instanceof Collection) subStructure=((Collection)struct[i+1]).toArray();
                            else subStructure=(Object[]) struct[i+1];
                            makeAWTMenu(submenu, subStructure, defaultListener); 
                            if(menu == null) { menu = new PopupMenu(); }
                            menu.add(submenu);
                            menuItem = null;
                        }
                        else {
                            if(menuName.startsWith("---")) {
                                //JSeparator s = new JSeparator();
                                //menu.add(s);
                                if(menu != null) menu.addSeparator();
                                menuItem = null;
                            }
                            else {
                                menuItem = new MenuItem();
                                if(menuName.startsWith("[") && menuName.endsWith("]")) {
                                    menuItem.setEnabled(false);
                                    menuName = menuName.substring(1, menuName.length()-1);
                                }
                                menuItem.setFont(UIConstants.menuFont);
                                menuItem.setLabel(menuName); //menuItem.setText(menuName);
                                menuItem.setName(menuName);
                                //menuItem.setIcon(UIBox.getIcon("gui/icons/empty.png"));
                                menuItem.setActionCommand(menuName);
                                menuItem.addActionListener(defaultListener);
                                if(menu == null) { menu = new PopupMenu(); }
                                menu.add(menuItem);
                            }
                        }
                    }
                    else if(struct[i] instanceof KeyStroke) {
                        if(menuItem != null) {
                            // ACCELERATOR KEYS DISABLED ON POPUPS!
                            //menuItem.setAccelerator((KeyStroke) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof WandoraTool) {
                        if(menuItem != null) {
                            try {
                                menuItem.setActionCommand(((WandoraTool) struct[i]).getName());
                                //menuItem.setToolTipText(Textbox.makeHTMLParagraph(((WandoraTool) struct[i]).getDescription(), 30));
                                Wandora w = null;
                                if(defaultListener instanceof Wandora) {
                                    w = (Wandora) defaultListener;
                                }
                                else if(defaultListener instanceof Component) {
                                    w = Wandora.getWandora((Component) defaultListener);
                                }
                                WandoraTool t = (WandoraTool) struct[i];
                                menuItem.removeActionListener(defaultListener);
                                menuItem.addActionListener(new WandoraToolActionListener(w, t));                               
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(struct[i] instanceof Icon) {
                        if(menuItem != null) {
                            //menuItem.setIcon((Icon) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof Menu) {
                        if(menu != null) { 
                            //System.out.println("Adding menu " + struct[i]);
                            menu.add((Menu) struct[i]); 
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof MenuItem) {
                        if(menu != null) { 
                            menu.add((MenuItem) struct[i]);
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof Menu[]) {
                        if(menu != null) {
                            Menu[] menuArray = (Menu[]) struct[i];
                            for(int j=0; j<menuArray.length; j++) {
                                menu.add(menuArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof MenuItem[]) {
                        if(menu != null) { 
                            MenuItem[] menuItemArray = (MenuItem[]) struct[i];
                            for(int j=0; j<menuItemArray.length; j++) {
                                menu.add(menuItemArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof ActionListener) {
                        if(menuItem != null) {
                            menuItem.removeActionListener(defaultListener);
                            menuItem.addActionListener((ActionListener) struct[i]);
                        }
                    }
                }
            }
        }
        return menu;

    }
    
    public static PopupMenu makeAWTPopupMenu(Object[] struct, ActionListener defaultListener) {
        return makeAWTPopupMenu((PopupMenu)null, struct, defaultListener);
    }
    
    public static JPopupMenu makePopupMenu(Object[] struct, ActionListener defaultListener) {
        return makePopupMenu((JPopupMenu)null, struct, defaultListener);
    }
    
    public static JPopupMenu makePopupMenu(JPopupMenu menu, Object[] struct, ActionListener defaultListener) {
        if(struct != null) {
            if(struct.length > 0) {
                SimpleMenuItem menuItem = null;
                for(int i=0; i<struct.length; i++) {
                    if(struct[i] instanceof String) {
                        String menuName = (String) struct[i];
                        if(struct.length > i+1 && (struct[i+1] instanceof Object[] || struct[i+1] instanceof Collection) ) {
                            SimpleMenu submenu = new SimpleMenu();
                            if(menuName.startsWith("[") && menuName.endsWith("]")) {
                                submenu.setEnabled(false);
                                menuName = menuName.substring(1, menuName.length()-1);
                            }
                            submenu.setFont(UIConstants.menuFont);
                            submenu.setText(menuName);
                            submenu.setName(menuName);
                            //submenu.setIcon(UIBox.getIcon("gui/icons/empty.png"));
                            Object[] subStructure;
                            if(struct[i+1] instanceof Collection) subStructure=((Collection)struct[i+1]).toArray();
                            else subStructure=(Object[]) struct[i+1];
                            makeMenu(submenu, subStructure, defaultListener); 
                            if(menu == null) { menu = new JPopupMenu(); }
                            menu.add(submenu);
                            menuItem = null;
                        }
                        else {
                            if(menuName.startsWith("---")) {
                                JSeparator s = new JSeparator();
                                if(menu != null) menu.add(s);
                                menuItem = null;
                            }
                            else {
                                menuItem = new SimpleMenuItem();
                                if(menuName.startsWith("[") && menuName.endsWith("]")) {
                                    menuItem.setEnabled(false);
                                    menuName = menuName.substring(1, menuName.length()-1);
                                }
                                menuItem.setFont(UIConstants.menuFont);
                                menuItem.setText(menuName);
                                menuItem.setName(menuName);
                                //menuItem.setIcon(UIBox.getIcon("gui/icons/empty.png"));
                                menuItem.setActionCommand(menuName);
                                menuItem.addActionListener(defaultListener);
                                if(menu == null) { menu = new JPopupMenu(); }
                                menu.add(menuItem);
                            }
                        }
                    }
                    else if(struct[i] instanceof KeyStroke) {
                        if(menuItem != null) {
                            // ACCELERATOR KEYS DISABLED ON POPUPS!
                            //menuItem.setAccelerator((KeyStroke) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof WandoraTool) {
                        if(menuItem != null) {
                            try {
                                menuItem.setActionCommand(((WandoraTool) struct[i]).getName());
                                menuItem.setToolTipText(Textbox.makeHTMLParagraph(((WandoraTool) struct[i]).getDescription(), 30));
                               
                                Wandora w = null;
                                if(defaultListener instanceof Wandora) {
                                    w = (Wandora) defaultListener;
                                }
                                else if(defaultListener instanceof Component) {
                                    w = Wandora.getWandora((Component) defaultListener);
                                }
                                WandoraTool t = (WandoraTool) struct[i];
                                menuItem.removeActionListener(defaultListener);

                                menuItem.addActionListener(new WandoraToolActionListener(w, t));                               
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(struct[i] instanceof Icon) {
                        if(menuItem != null) {
                            menuItem.setIcon((Icon) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof JMenu) {
                        if(menu != null) { 
                            //System.out.println("Adding menu " + struct[i]);
                            menu.add((JMenu) struct[i]); 
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof JMenuItem) {
                        if(menu != null) { 
                            menu.add((JMenuItem) struct[i]);
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof JMenu[]) {
                        if(menu != null) {
                            JMenu[] menuArray = (JMenu[]) struct[i];
                            for(int j=0; j<menuArray.length; j++) {
                                menu.add(menuArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof JMenuItem[]) {
                        if(menu != null) { 
                            JMenuItem[] menuItemArray = (JMenuItem[]) struct[i];
                            for(int j=0; j<menuItemArray.length; j++) {
                                menu.add(menuItemArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof ActionListener) {
                        if(menuItem != null) {
                            ActionListener[] listeners = menuItem.getActionListeners();
                            for(int l=0; l<listeners.length; l++) {
                                menuItem.removeActionListener(listeners[l]);
                            }
                            menuItem.addActionListener((ActionListener) struct[i]);
                        }
                    }
                }
            }
        }
        return menu;
    }
    

    
    
    
    
    // -------------------------------------------------------------------------
    
    public static Menu attachAWTMenu(Menu base, Object[] struct, ActionListener defaultListener) {
        return makeAWTMenu(base, struct, defaultListener);
    }
    
    public static Menu makeAWTMenu(Object[] struct, ActionListener defaultListener) {
        return makeAWTMenu(null, struct, defaultListener);
    }

    private static Menu makeAWTMenu(Menu menu, Object[] struct, ActionListener defaultListener) {
        if(struct != null) {
            if(struct.length > 0) {
                MenuItem menuItem = null;
                for(int i=0; i<struct.length; i++) {
                    if(struct[i] instanceof String) {
                        String menuName = (String) struct[i];
                        if(struct.length > i+1 && (struct[i+1] instanceof Object[] || struct[i+1] instanceof Collection) ) {
                            Menu submenu = new SimpleAWTMenu(menuName);
                            Object[] subStruct;
                            if(struct[i+1] instanceof Collection) subStruct=((Collection)struct[i+1]).toArray();
                            else subStruct=(Object[])struct[i+1];
                            makeAWTMenu(submenu, subStruct, defaultListener); 
                            if(menu != null) { 
                                menu.add(submenu); 
                            }
                            else { 
                                menu = submenu; 
                                //menu.setIcon(null);
                            }
                            menuItem = null;
                        }
                        else {
                            if(menuName.startsWith("---")) {
                                //JSeparator s = new JSeparator();
                                //menu.add(s);
                                if(menu != null) menu.addSeparator();
                                menuItem = null;
                            }
                            else {
                                menuItem = new SimpleAWTMenuItem(menuName, defaultListener);
                                if(menu == null) { menu = new SimpleAWTMenu(); }
                                if(menuItem != null) menu.add(menuItem);
                            }
                        }
                    }
                    else if(struct[i] instanceof KeyStroke) {
                        if(menuItem != null) {
                            //menuItem.setAccelerator((KeyStroke) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof WandoraTool) {
                        if(menuItem != null) {
                            try {
                                menuItem.setActionCommand(((WandoraTool) struct[i]).getName());
                                //menuItem.setToolTipText(Textbox.makeHTMLParagraph(((WandoraTool) struct[i]).getDescription(), 30));
                                Wandora w = null;
                                if(defaultListener instanceof Wandora) {
                                    w = (Wandora) defaultListener;
                                }
                                else if(defaultListener instanceof Component) {
                                    w = Wandora.getWandora((Component) defaultListener);
                                }
                                WandoraTool t = (WandoraTool) struct[i];
                                menuItem.removeActionListener(defaultListener);
                                menuItem.addActionListener(new WandoraToolActionListener(w, t));                               
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(struct[i] instanceof Icon) {
                        if(menuItem != null) {
                            //menuItem.setIcon((Icon) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof Menu) {
                        //System.out.println("Adding menu " + struct[i]);
                        if(menu != null) { 
                            menu.add((Menu) struct[i]); 
                        }
                        else { 
                            menu = (Menu) struct[i]; 
                            //menu.setIcon(null);
                        }
                        menuItem = null;
                    }
                    else if(struct[i] instanceof MenuItem) {
                        if(menu != null) { 
                            menu.add((MenuItem) struct[i]);
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof Menu[]) {
                        if(menu != null) {
                            Menu[] menuArray = (Menu[]) struct[i];
                            for(int j=0; j<menuArray.length; j++) {
                                menu.add(menuArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof MenuItem[]) {
                        if(menu != null) { 
                            MenuItem[] menuItemArray = (MenuItem[]) struct[i];
                            for(int j=0; j<menuItemArray.length; j++) {
                                menu.add(menuItemArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof ActionListener) {
                        if(menuItem != null) {
                            ActionListener[] listeners = menuItem.getActionListeners();
                            for(int l=0; l<listeners.length; l++) {
                                menuItem.removeActionListener(listeners[l]);
                            }
                            menuItem.addActionListener((ActionListener) struct[i]);
                        }
                    }
                }
            }
        }
        return menu;
    }
    
    public static JMenu makeMenu(Object[] struct, ActionListener defaultListener) {
        return makeMenu(null, struct, defaultListener);
    }
    
    public static JMenu attachMenu(JMenu base, Object[] struct, ActionListener defaultListener) {
        return makeMenu(base, struct, defaultListener);
    }
    
    
    public static JMenu makeMenu(JMenu menu, Object[] struct, ActionListener defaultListener) {
        if(struct != null) {
            if(struct.length > 0) {
                JMenuItem menuItem = null;
                for(int i=0; i<struct.length; i++) {
                    if(struct[i] instanceof String) {
                        String menuName = (String) struct[i];
                        if(struct.length > i+1 && (struct[i+1] instanceof Object[] || struct[i+1] instanceof Collection) ) {
                            JMenu submenu = new SimpleMenu(menuName);
                            Object[] subStruct;
                            if(struct[i+1] instanceof Collection) subStruct=((Collection)struct[i+1]).toArray();
                            else subStruct=(Object[])struct[i+1];
                            makeMenu(submenu, subStruct, defaultListener); 
                            if(menu != null) { 
                                menu.add(submenu); 
                            }
                            else { 
                                menu = submenu; 
                                menu.setIcon(null);
                            }
                            menuItem = null;
                        }
                        else {
                            if(menuName.startsWith("---")) {
                                JSeparator s = new JSeparator();
                                if(menu != null) menu.add(s);
                                menuItem = null;
                            }
                            else {
                                menuItem = new SimpleMenuItem(menuName, defaultListener);
                                if(menu == null) { menu = new SimpleMenu(); }
                                menu.add(menuItem); 
                            }
                        }
                    }
                    else if(struct[i] instanceof KeyStroke) {
                        if(menuItem != null) {
                            menuItem.setAccelerator((KeyStroke) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof WandoraTool) {
                        if(menuItem != null) {
                            try {
                                menuItem.setActionCommand(((WandoraTool) struct[i]).getName());
                                menuItem.setToolTipText(Textbox.makeHTMLParagraph(((WandoraTool) struct[i]).getDescription(), 30));
                                Wandora w = null;
                                if(defaultListener instanceof Wandora) {
                                    w = (Wandora) defaultListener;
                                }
                                else if(defaultListener instanceof Component) {
                                    w = Wandora.getWandora((Component) defaultListener);
                                }
                                WandoraTool t = (WandoraTool) struct[i];
                                menuItem.removeActionListener(defaultListener);
                                menuItem.addActionListener(new WandoraToolActionListener(w, t));                               
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(struct[i] instanceof Icon) {
                        if(menuItem != null) {
                            menuItem.setIcon((Icon) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof JMenu) {
                        //System.out.println("Adding menu " + struct[i]);
                        if(menu != null) { 
                            menu.add((JMenu) struct[i]); 
                        }
                        else { 
                            menu = (JMenu) struct[i]; 
                            menu.setIcon(null);
                        }
                        menuItem = null;
                    }
                    else if(struct[i] instanceof JMenuItem) {
                        if(menu != null) { 
                            menu.add((JMenuItem) struct[i]);
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof JMenu[]) {
                        if(menu != null) {
                            JMenu[] menuArray = (JMenu[]) struct[i];
                            for(int j=0; j<menuArray.length; j++) {
                                menu.add(menuArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof JMenuItem[]) {
                        if(menu != null) { 
                            JMenuItem[] menuItemArray = (JMenuItem[]) struct[i];
                            for(int j=0; j<menuItemArray.length; j++) {
                                menu.add(menuItemArray[j]); 
                            }
                            menuItem = null;
                        }
                    }
                    else if(struct[i] instanceof ActionListener) {
                        if(menuItem != null) {
                            ActionListener[] listeners = menuItem.getActionListeners();
                            for(int l=0; l<listeners.length; l++) {
                                menuItem.removeActionListener(listeners[l]);
                            }
                            menuItem.addActionListener((ActionListener) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof Boolean){
                        if(menuItem != null) {
                            menuItem.setEnabled( ((Boolean)struct[i]).booleanValue() );
                        }
                    }
                }
            }
        }
        return menu;
    }
    
    
    
    

    // -------------------------------------------------------------------------
    
    
    
    

    public static JComponent makeButtonContainer(Object[] struct, ActionListener defaultListener) {
        JPanel container = new SimplePanel();
        FlowLayout layout = new FlowLayout(FlowLayout.LEADING, 0, 0);
        container.setLayout(layout);
        return fillButtonContainer(container, struct, defaultListener);
    }
    
    
    public static JComponent fillButtonContainer(JComponent container, Object[] struct, ActionListener defaultListener) {
        if(struct != null) {
            if(struct.length > 0) {
                JButton button = null;
                for(int i=0; i<struct.length; i++) {
                    if(struct[i] == null) continue;
                    // System.out.println("BUTTON: "+struct[i]);
                    // System.out.println();
                    if(struct[i] instanceof String) {
                        String str = (String) struct[i];
                        if("---".equals(str)) {
                            button = null;
                            JPanel separatorPanel = new JPanel();
                            separatorPanel.setPreferredSize(new Dimension(10,42));
                            container.add(separatorPanel);
                        }
                        else {
                            button = makeDefaultButton();
                            if(str.startsWith("[") && str.endsWith("]")) {
                                button.setEnabled(false);
                                str = str.substring(1, str.length()-1);
                            }
                            button.setText(str);
                            button.setActionCommand(str);
                            container.add(button);
                        }
                    }
                    else if(struct[i] instanceof WandoraTool) {
                        try {
                            WandoraTool tool = (WandoraTool) struct[i];
                            if(button == null) {
                                button = makeDefaultButton();
                                String str = tool.getName();
                                if(str.startsWith("[") && str.endsWith("]")) {
                                    button.setEnabled(false);
                                    str = str.substring(1, str.length()-1);
                                }
                                button.setText(str);
                                button.setActionCommand(str);
                                container.add(button);
                            }
                            if(button != null) {
                                if(button.getIcon() == null) button.setIcon(tool.getIcon());                               
                                button.setToolTipText(Textbox.makeHTMLParagraph(((WandoraTool) struct[i]).getDescription(), 30));
                                button.addActionListener(new WandoraToolActionListener(Wandora.getWandora(), tool));
                                container.add(button);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if(struct[i] instanceof Image) {
                        if(button != null) {
                            Image image = (Image) struct[i];
                            button.setIcon(new ImageIcon(image));
                        }
                    }
                    else if(struct[i] instanceof Icon) {
                        if(button != null) {
                            Icon icon = (Icon) struct[i];
                            button.setIcon(icon);
                        }
                    }
                    else if(struct[i] instanceof JButton) {
                        //System.out.println("Adding menu " + struct[i]);
                        button = (JButton) struct[i];
                        container.add(button); 
                    }
                    else if(struct[i] instanceof JButton[]) {
                        JButton[] buttonArray = (JButton[]) struct[i];
                        for(JButton b : buttonArray) {
                            container.add(b); 
                        }
                        button = null;
                    }
                    else if(struct[i] instanceof ActionListener) {
                        if(button != null) {
                            ActionListener[] listeners = button.getActionListeners();
                            for(int l=0; l<listeners.length; l++) {
                                button.removeActionListener(listeners[l]);
                            }
                            button.addActionListener((ActionListener) struct[i]);
                        }
                    }
                    else if(struct[i] instanceof Object[]) {
                        if(button != null) {
                            Object[] menuStruct = (Object[]) struct[i];
                            JPopupMenu buttonMenu = makePopupMenu(menuStruct, defaultListener);
                            button.setComponentPopupMenu(buttonMenu);
                        }
                    }
                    else if(struct[i] instanceof JPopupMenu) {
                        if(button != null) {
                            JPopupMenu menu = (JPopupMenu) struct[i];
                            button.setComponentPopupMenu(menu);
                        }
                    }
                }
            }
        }
        return container;
    }
    
    
    private static SimpleButton makeDefaultButton() {
        SimpleButton button = new SimpleButton();
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(60,42));
        button.setBackground(UIConstants.buttonBarBackgroundColor);
        button.setForeground(UIConstants.buttonBarLabelColor);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(UIConstants.miniButtonLabel);
        button.setBorder(new EtchedBorder( Color.WHITE, Color.GRAY ));
        button.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    evt.getComponent().setBackground(UIConstants.defaultActiveBackground);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    evt.getComponent().setBackground(UIConstants.buttonBarBackgroundColor);
                }
            }
        );
        return button;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static Container makeGraphToolBar(Object[] struct, ActionListener defaultListener) {
        return fillGraphToolBar(new JToolBar(), struct, defaultListener);
    }
    
    
    public static Container fillGraphToolBar(Container bar, Object[] struct, ActionListener defaultListener) {
        if(struct != null) {
            if(struct.length > 0) {
                if(bar == null) bar = new JToolBar();
                AbstractButton button = null;
                ButtonGroup bg = null;
                for(int i=0; i<struct.length; i++) {
                    Object o = struct[i];
                    if(o instanceof ButtonGroup) {
                        bg = (ButtonGroup) o;
                    }
                    else if(o instanceof JToggleButton) {
                        button = (JToggleButton) o;
                        fixGraphToolButton(button);
                        if(bar != null) bar.add(button);
                        if(bg != null) bg.add(button);
                    }
                    else if(o instanceof JButton) {
                        button = (JButton) o;
                        fixGraphToolButton(button);
                        if(bar != null) bar.add(button);
                        if(bg != null) bg.add(button);
                    }
                    else if(o instanceof WandoraTool) {
                        if(button != null) {
                            try {
                                button.setActionCommand(((WandoraTool) struct[i]).getName());
                                button.setToolTipText(Textbox.makeHTMLParagraph(((WandoraTool) struct[i]).getDescription(), 30));
                                Wandora w = null;
                                if(defaultListener instanceof Wandora && defaultListener != null) {
                                    w = (Wandora) defaultListener;
                                }
                                else if(defaultListener instanceof Component) {
                                    w = Wandora.getWandora((Component) defaultListener);
                                }
                                WandoraTool t = (WandoraTool) struct[i];
                                button.removeActionListener(defaultListener);
                                button.addActionListener(new WandoraToolActionListener(w, t));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(o instanceof KeyStroke) {
                        if(button != null) {
                            final KeyStroke ks = (KeyStroke) o;
                            final AbstractButton b = button;
                            button.addKeyListener( new KeyListener() {
                                @Override
                                public void keyPressed(KeyEvent e) {
                                    //System.out.println("key pressed1");
                                    if(ks.equals(KeyStroke.getKeyStrokeForEvent(e))) {
                                        b.doClick();
                                        //System.out.println("key pressed2");
                                    }
                                }
                                @Override
                                public void keyReleased(KeyEvent e) {}
                                @Override
                                public void keyTyped(KeyEvent e) {}
                            } );
                        }
                    }
                    else if(o instanceof String) {
                        String s = (String) o;
                        if(s != null && s.startsWith("---")) {
                            JSeparator js = new JSeparator(JSeparator.HORIZONTAL);
                            //js.setPreferredSize(new Dimension(30, 3));
                            //js.setMargin(new Insets(1,1,1,1));
                            if(bar != null) bar.add(js);
                            bg = null;
                        }
                    }
                    
                }
            }
        }
        return bar;
    }
    
    
    public static void fixGraphToolButton(AbstractButton b) {
        b.setBorderPainted(true);
        b.setFocusPainted(true);
        b.setMinimumSize(new Dimension(30, 24));
        b.setMaximumSize(new Dimension(30, 24));
        b.setPreferredSize(new Dimension(30, 24));
        b.setMargin(new Insets(1,1,1,1));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    
    
    
    public static JToolBar makeToolBar(Object[] struct, ActionListener listener) {
        JToolBar bar = null;
        if(struct != null) {
            if(struct.length > 0) {
                bar = new JToolBar();
                for(int i=0; i<struct.length; i++) {
                    if(struct[i] instanceof Object[]) {
                        Object[] componentData = (Object[]) struct[i];
                        try {
                            String type = (String) componentData[0];
                            if(type.equalsIgnoreCase("button")) {
                                JButton button = new JButton();
                                button.setIcon(UIBox.getIcon((String) componentData[1]));
                                button.setToolTipText((String) componentData[2]);
                                button.setActionCommand((String) componentData[3]);
                                button.setMargin(new Insets(0,0,0,0));
                                //button.setPreferredSize(new java.awt.Dimension(45, 45));
                                button.addActionListener(listener);
                                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                if(bar != null) bar.add(button);
                            }
                            else if(type.equalsIgnoreCase("logopanel")) {
                                ImagePanel logoPanel = new ImagePanel((String) componentData[1]);
                                JPanel logoContainer = new JPanel();
                                logoContainer.setLayout(new java.awt.BorderLayout());
                                logoContainer.add(logoPanel, java.awt.BorderLayout.EAST);
                                if(bar != null) bar.add(logoContainer);
                            }
                        }
                        catch (Exception e) {}
                    }
                }
            }
        }
        return bar;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static Object[] fillMenuTemplate(String key, Object[] items, Object[] menuTemplate){
        ArrayList<Object> menu=new ArrayList<Object>();
        
        Object lastItem="DUMMY";
        for(int i=0;i<menuTemplate.length;i++){
            Object item=menuTemplate[i];
            if(item.equals(key)){
                if(items!=null){
                    for(int j=0;j<items.length;j++){
                        menu.add(items[j]);
                    }
                }
            }
            else {
                if(lastItem.equals(key) && item.equals("---") && menu.get(menu.size()-1).equals("---")) {
                    // don't add two separators
                }
                else menu.add(item);
            }
            lastItem=item;
        }
        return menu.toArray();
    }
    
    
    public static Object[] injectMenuStruct(Object[] a, Object[] b, int injectPoint) {
        if(a == null || b == null) return a;
        Object[] c = new Object[a.length+b.length];
        int p=0;
        int i=0;
        for(; i<Math.min(a.length, injectPoint); i++) {
            c[p++] = a[i];
        }
        for(int j=0; j<b.length; j++) {
            c[p++] = b[j];
        }
        for(; i<a.length; i++) {
            c[p++] = a[i];
        }
        return c;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    private static HashMap iconCache = null;
    private static Font iconFont = null;
    
    
    
    public static Icon getIcon(int iconCharacter) {
        if(iconCache != null) {
            try {
                Icon icon = (Icon) iconCache.get(iconCharacter);
                if(icon != null) return icon;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            iconCache = new HashMap();
        }
        Image image = getImage(iconCharacter);
        if(image != null) {
            try {
                Icon icon = new ImageIcon(image);
                iconCache.put(iconCharacter, icon);
                return icon;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    
    public static Icon getIcon(String iconName) {
        if(iconCache != null) {
            try {
                Icon icon = (Icon) iconCache.get(iconName);
                if(icon != null) return icon;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            iconCache = new HashMap();
        }
        Image image = getImage(iconName);
        if(image != null) {
            try {
                Icon icon = new ImageIcon(image);
                iconCache.put(iconName, icon);
                return icon;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
        
    
    
    /**
     * Create image with a character in font-awesome. Font-awesome is a widely 
     * used icon collection created by Dave Gandy. The font is described in
     * http://fortawesome.github.io/Font-Awesome/ .
     * 
     * Use character code arguments between 0x7000 - 0x720C.
     *  
     **/
    public static BufferedImage getImage(int character) {
        try {
            if(iconFont == null) {
                InputStream is = ClassLoader.getSystemResourceAsStream("gui/fonts/fontawesome/fontawesome-webfont.ttf");
                iconFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
            if(iconFont != null) {
                BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Font iconFont16 = iconFont.deriveFont(12f);
                g.setFont(iconFont16);
                g.setColor(UIConstants.wandoraBlueColor);
                String iconText = Character.toString((char)character);
                g.drawChars(iconText.toCharArray(), 0, 1, 2, 13);
                return image;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    public static BufferedImage getImage32(int character) {
        try {
            if(iconFont == null) {
                InputStream is = ClassLoader.getSystemResourceAsStream("gui/fonts/fontawesome/fontawesome-webfont.ttf");
                iconFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
            if(iconFont != null) {
                BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Font iconFont28 = iconFont.deriveFont(28f);
                g.setFont(iconFont28);
                g.setColor(UIConstants.wandoraBlueColor);
                String iconText = Character.toString((char)character);
                g.drawChars(iconText.toCharArray(), 0, 1, 2, 13);
                return image;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    public static BufferedImage getImage(String imageName) {
        return getImage(imageName, null);
    }
            
    public static BufferedImage getImage(String imageName, HttpAuthorizer httpAuthorizer) {
        if(DataURL.isDataURL(imageName)) {
            try {
                DataURL dataURL = new DataURL(imageName);
                String mimeType = dataURL.getMimetype();
                if(mimeType.startsWith("image")) {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataURL.getData()));
                    return image;
                }
            }
            catch(Exception e) {
                System.out.println("'"+e.getMessage()+"' occurred while reading datauri image '" + imageName + "'!");
            }
        }
        else {
            try {
                URL iconUrl = ClassLoader.getSystemResource(imageName);
                BufferedImage image = javax.imageio.ImageIO.read(iconUrl);
                return image;
            }
            catch (Exception e) {
                try {

                    URL url = new URL(imageName);
                    URLConnection urlConnection = null;
                    if(httpAuthorizer != null) {
                         urlConnection = httpAuthorizer.getAuthorizedAccess(url);
                    }
                    else {
                        urlConnection = url.openConnection();
                        Wandora.initUrlConnection(urlConnection);
                    }
                    BufferedImage image = ImageIO.read(urlConnection.getInputStream());
                    return image;
                }
                catch (Exception e1) {
                    //e1.printStackTrace();
                    try {
                        String fname = imageName;
                        if(fname.startsWith("file:")) {
                            fname = IObox.getFileFromURL(fname);
                        }
                        //System.out.print("Finding file name: " + fname);
                        File imageFile = new File(fname); 
                        BufferedImage image = ImageIO.read(imageFile);
                        return image;
                    }
                    catch (Exception e2) {
                        //e2.printStackTrace();
                        System.out.println("'"+e2.getMessage()+"' occurred while reading image '" + imageName + "'!");
                    }
                }
            }
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static javafx.scene.image.Image tranformAwtImageToFXImage(java.awt.Image image) throws IOException {
        if(!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                    image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            image = bufferedImage;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
      }
    
    
    // -------------------------------------------------------------------------
    
    private static HashMap iconThumbCache = new HashMap();
    
    
    public static Icon getCachedIconThumbForLocator(String l, int width, int height) {
        String key = l + ":" + width + ":" + height;
        Icon thumb = (Icon) iconThumbCache.get(key);
        if(thumb != null) {
            return thumb;
        }
        else {
            thumb = new ImageIcon( getThumbForLocator(l, width, height) );
            iconThumbCache.put(key, thumb);
            return thumb;
        }
    }
    
    
    public static BufferedImage getThumbForLocator(String l, int width, int height) {
        BufferedImage thumb = getThumbForLocator(l, null);
        BufferedImage resizedThumb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = resizedThumb.getGraphics();
        g.drawImage(thumb, 0, 0, width, height, (ImageObserver) null);
        return resizedThumb;
    }
    
    
    public static BufferedImage getThumbForLocator(String l) {
        return getThumbForLocator(l, null);
    }
    
    public static BufferedImage getThumbForLocator(String l, HttpAuthorizer httpAuthorizer ) {
        BufferedImage thumb = null;
        if(l != null) {
            try {
                thumb = UIBox.getImage(l, httpAuthorizer);
            }
            catch(Exception e) {}
            
            if(thumb == null) {
                if(endsWith(l, new String[] { ".mp1", ".mp2", ".mp3", ".aif", ".aiff", ".wav", ".au", ".mid", ".midi" })) {
                    thumb = UIBox.getImage("gui/icons/doctype/doctype_audio.png");
                }
                else if(endsWith(l, new String[] { ".jpg", ".jpeg", ".png", ".tif", ".gif", ".psd", ".jfif", ".jpe"  })) {
                    if(thumb == null) thumb = UIBox.getImage("gui/icons/doctype/doctype_image.png");
                }
                else if(endsWith(l, new String[] { ".mpg", ".mpeg", ".mpg", ".mov", ".avi", ".mpa" })) {
                    thumb =  UIBox.getImage("gui/icons/doctype/doctype_video.png");
                }
                else if(endsWith(l, new String[] { ".txt", ".doc", ".rtf" })) {
                    thumb =  UIBox.getImage("gui/icons/doctype/doctype_text.png");
                }
                else {
                    thumb = UIBox.getImage("gui/icons/doctype/doctype_unknown.png", httpAuthorizer);
                }
            }
        }
        return thumb;
    }
    
    
    
    
    private static boolean endsWith(String s, String[] ends) {
        if(s != null && ends != null) {
            s = s.toLowerCase();
            for(int i=0; i<ends.length; i++) {
                ends[i] = ends[i].toLowerCase();
                if(s.endsWith(ends[i])) return true;
            }
        }
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
        
    public static Object getActionsRealSource(ActionEvent e) {
        try {
            //System.out.println("INVESTIGATING ACTION SOURCE");
            if(e != null) {
                Object s = e.getSource();
                if(s instanceof JMenuItem) {
                    Component c = (Component) s;
                    Component nc = c;
                    int count = 30;
                    while(nc != null && count-- > 0 && (c instanceof MenuElement || c instanceof JMenuBar || c instanceof JLayeredPane || c instanceof JRootPane)) {
                        // System.out.println("source "+c+"\n");
                        c = nc;
                        if(c instanceof JPopupMenu) nc = ((JPopupMenu)c).getInvoker();
                        else if(c instanceof JMenu) nc = ((JMenu) c).getParent();
                        else if(c instanceof JLayeredPane) nc = ((JLayeredPane) c).getRootPane();
                        //else if(c instanceof JRootPane) nc = ((JRootPane) c).getContentPane();
                        else if(c instanceof JDialog) return c;
                        else if(c instanceof JFrame) return c;
                        else if(c instanceof LayerStatusPanel) return c;
                        else if(c instanceof TopicMapGraphPanel) return c;
                        else if(c instanceof GraphTopicPanel) return c;
                        else if(c instanceof WebViewPanel) return c;
                        else if(c instanceof DockingFramePanel) return ((DockingFramePanel) c).getCurrentTopicPanel();
                        else nc = c.getParent();
                    }
                    if(c != null) {
                        //System.out.println("source "+c+"\n");
                        return c;
                    }
                }
                return s;
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
        
    public static Component getComponentByName(String componentName, Component root) {
        try {
            //System.out.println(" COMPARE " + componentName + " == " + root.getName());
            if(componentName.equalsIgnoreCase(root.getName())) return root;
            else {
                Component[] components = new Component[] {};
                if(root instanceof Container) components = ((Container) root).getComponents();
                if(root instanceof JMenu) components = ((JMenu) root).getMenuComponents();
                
                Component component = null;
                for(int i=0; i<components.length; i++) {
                    component = getComponentByName(componentName, components[i]);
                    if(component != null) return component;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static Component getParentComponentByName(String componentName, Component root, int maxDepth) {
        if(componentName == null) return null;
        while(root != null && !componentName.equals(root.getName()) && maxDepth > 0) {
            maxDepth--;
            //if(root != null) System.out.println("CNAME: " + root.getName());
            root = root.getParent();
        }
        if(maxDepth > 0 && root != null) return root;
        else return null;
    }
    
    
    
    
    public static Component getParentComponentByClass(Class componentClass, Component root, int maxDepth) {
        if(componentClass == null) return null;
        while(root != null && !componentClass.equals(root.getClass()) && maxDepth > 0) {
            maxDepth--;
            //if(root != null) System.out.println("CNAME: " + root.getName());
            root = root.getParent();
        }
        if(maxDepth > 0 && root != null) return root;
        else return null;
    }
    
    
    
    public static Component getParentComponentByClass(String componentClassName, Component root, int maxDepth) {
        try {
            return getParentComponentByClass(Class.forName(componentClassName), root, maxDepth);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // -------------------------------------------------------------------------
    
    
    public static void centerWindow(JFrame w, Frame parent) {
        try {
            if(w != null && parent != null) {
                Dimension parentSize = parent.getSize();
                w.setLocation(parent.getX()+(parentSize.width-w.getSize().width)/2, parent.getY()+(parentSize.height-w.getSize().height)/2);
            }
        } catch (Exception e) {}
    }
    
    
    
    public static void centerWindow(JDialog w, Frame parent) {
        try {
            if(w != null && parent != null) {
                Dimension parentSize = parent.getSize();
                w.setLocation(parent.getX()+(parentSize.width-w.getSize().width)/2, parent.getY()+(parentSize.height-w.getSize().height)/2);
            }
        } catch (Exception e) {}
    }
    
    
    public static void centerScreen(Frame f) {
        try {
            Toolkit kit = Toolkit.getDefaultToolkit();
            Dimension screenSize = kit.getScreenSize();
            f.setLocation((screenSize.width-f.getSize().width)/2, (screenSize.height-f.getSize().height)/2);
        } catch (Exception e) {}
    }
    
    
        
    
    public static void centerScreen(JDialog f) {
        try {
            Toolkit kit = Toolkit.getDefaultToolkit();
            Dimension screenSize = kit.getScreenSize();
            f.setLocation((screenSize.width-f.getSize().width)/2, (screenSize.height-f.getSize().height)/2);
        } catch (Exception e) {}
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    


    public static Icon resizeIconCanvas(Icon icon, int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB );
        if(icon != null) {
            Graphics g = image.getGraphics();
            int x = (w-icon.getIconWidth()) / 2;
            int y = (h-icon.getIconHeight()) / 2;
            icon.paintIcon(null, g, x, y);
        }
        ImageIcon ii = new ImageIcon(image);
        return ii;
    }

    
    
    // -------------------------------------------------------------------------
    
    
    public static Component getPreview(DataURL dataURL) {
        if(dataURL != null) {
            String mimetype = dataURL.getMimetype();
            if("image/gif".equalsIgnoreCase(mimetype) ||
               "image/bmp".equalsIgnoreCase(mimetype) ||
               "image/vbmp".equalsIgnoreCase(mimetype) ||
               "image/jpg".equalsIgnoreCase(mimetype) ||
               "image/jpeg".equalsIgnoreCase(mimetype) ||
               "image/png".equalsIgnoreCase(mimetype)) {
                try {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataURL.getData()));
                    JLabel imageComponent = new JLabel(new ImageIcon(image));
                    imageComponent.setSize(image.getWidth(), image.getHeight());
                    return imageComponent;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    
}
