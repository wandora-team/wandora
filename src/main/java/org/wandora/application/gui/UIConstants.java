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
 * UIConstants.java
 *
 * Created on 17.7.2007, 13:15
 *
 */

package org.wandora.application.gui;


import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import org.wandora.application.gui.filechooser.WPRFileChooser;
import org.wandora.application.gui.simple.*;



/**
 * UIContants class contains static variables related to Wandora's user interface.
 * Static variables include fonts and colors used by the application.
 *
 * @author akivela
 */
public class UIConstants {
    public static boolean ANTIALIASING = true;
    
    
    public static Border defaultTableCellLabelBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    public static Border defaultLabelBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    
    
    public static Font smallButtonLabelFont = new Font("SansSerif", Font.PLAIN, 10);
    public static Font buttonLabelFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font comboBoxFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font largeLabelFont = new Font("SansSerif", Font.PLAIN, 14);
    public static Font panelTitleFont = new Font("SansSerif", Font.BOLD, 12);
    //public static Font wandoraBigLabelFont = new Font("SansSerif", Font.BOLD, 12);
    public static Font plainFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font titleFont = new Font("SansSerif", Font.PLAIN, 14);
    public static Font menuFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font tabFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font miniButtonLabel = new Font("SansSerif", Font.BOLD, 7);
    
    public static Font h3Font = new Font("SansSerif", Font.PLAIN, 14);
    public static Font h2Font = new Font("SansSerif", Font.PLAIN, 16);
    public static Font h1Font = new Font("SansSerif", Font.PLAIN, 20);

    public static Border dragBorder = BorderFactory.createLineBorder(Color.GRAY);

    private static SimpleFileChooser fileChooser = null;
    private static WPRFileChooser wandoraProjectFileChooser = null;

    public static Color menuColor = new Color(60,60,60);
    public static Color buttonBarLabelColor = new Color(90,90,90);
    public static Color buttonBarBackgroundColor = new Color(238,238,238);
    public static Color buttonBarMouseOverBackgroundColor = new Color(220,231,242);

    public static Color buttonBackgroundColor = new Color(200,221,242);
    public static Color buttonMouseOverBackgroundColor = new Color(220,231,242);
    
    public static Color checkBoxBackgroundColor = new Color(255,255,255);
    
    
    public static final Color defaultTextColor = new Color(60,60,60);
    public static final Color defaultInactiveBackground = new Color(229, 229, 229);
    public static final Color defaultActiveBackground = new Color(200,221,242);
    
    public static final Color editableBackgroundColor = new Color(255,255,255);
    public static final Color noContentBackgroundColor = new Color(245,245,245);
    
    public static final Color defaultBorderHighlight = new Color(255,255,255);
    public static final Color defaultBorderShadow = new Color(200,221,242);
    
    
    public static final Color wandoraBlueColor = new Color(53,56,87);
    
    
    
    public static WPRFileChooser getWandoraProjectFileChooser() {
        if(wandoraProjectFileChooser == null) wandoraProjectFileChooser = new WPRFileChooser();
        return wandoraProjectFileChooser;
    }
    
    
    public static SimpleFileChooser getFileChooser() {
        if(fileChooser == null) fileChooser = new SimpleFileChooser();
        fileChooser.setFileSelectionMode(SimpleFileChooser.FILES_AND_DIRECTORIES);
        return fileChooser;
    }


    private static final String[] COPY_PASTE_COMPONENTS = {
        "TextField.focusInputMap",
        "PasswordField.focusInputMap",
        "TextArea.focusInputMap",
        "TextPane.focusInputMap",
        "EditorPane.focusInputMap",
        "FormattedTextField.focusInputMap",
        "List.focusInputMap",
        "Table.ancestorInputMap",
        "Tree.focusInputMap"
    };
    
    // http://deeploveprogram.pbworks.com/w/page/17134461/UIManager,%20set%20default%20look%20and%20feel
    
    public static void initializeGUI() {
        try {
            javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.OceanTheme());
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            
            UIManager.put("ToolTip.background", defaultActiveBackground);
            
            UIManager.put("TabbedPane.selectHighlight", new Color(229, 229, 229));
            UIManager.put("TabbedPane.foreground", defaultTextColor);
            UIManager.put("TabbedPane.background", defaultInactiveBackground);
            //UIManager.put("TabbedPane.darkShadow", Color.WHITE);
            
            UIManager.put("ToggleButton.disabledBackground", defaultActiveBackground);
            UIManager.put("ToggleButton.background", defaultActiveBackground);
            
            UIManager.put("ComboBox.disabledBackground", defaultInactiveBackground);
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.selectionBackground", defaultActiveBackground);
            UIManager.put("ComboBox.buttonBackground", defaultActiveBackground);
            UIManager.put("ComboBox.buttonDarkShadow", defaultActiveBackground);
            UIManager.put("ComboBox.buttonHighlight", defaultActiveBackground);
            UIManager.put("ComboBox.buttonShadow", defaultActiveBackground);

            UIManager.put("Table.focusCellBackground", defaultActiveBackground);
            UIManager.put("Table.selectionBackground", defaultActiveBackground);
            
            UIManager.put("TextField.selectionBackground", defaultActiveBackground);
            UIManager.put("TextField.background", editableBackgroundColor);
            UIManager.put("TextArea.background", editableBackgroundColor);
            
            UIManager.put("MenuItem.selectionBackground", defaultActiveBackground);
            UIManager.put("Menu.selectionBackground", defaultActiveBackground);
            UIManager.put("Menu.background", defaultInactiveBackground);
            
            UIManager.put("Button.background", defaultActiveBackground);
            UIManager.put("Button.foreground", defaultTextColor);
            
            UIManager.put("Tree.selectionBackground", defaultActiveBackground);
            UIManager.put("Tree.line", "None");
            UIManager.put("Tree.collapsedIcon", UIBox.getIcon("resources/gui/icons/tree_branch.png"));
            UIManager.put("Tree.expandedIcon", UIBox.getIcon("resources/gui/icons/tree_branch_open.png"));
            
            UIManager.put("Slider.foreground", defaultActiveBackground);
            UIManager.put("Slider.highlight", defaultActiveBackground);
            UIManager.put("Slider.horizontalThumbIcon", UIBox.getIcon("resources/gui/icons/slider_thumb_horizontal.png"));
            UIManager.put("Slider.VerticalThumbIcon", UIBox.getIcon("resources/gui/icons/slider_thumb_vertical.png"));
            
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
            
            for (String component : COPY_PASTE_COMPONENTS) {
                InputMap im = (InputMap) UIManager.get(component);
                int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutMask), DefaultEditorKit.copyAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutMask), DefaultEditorKit.pasteAction);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutMask), DefaultEditorKit.cutAction);
            }
            
        } 
        catch (UnsupportedLookAndFeelException e) {
           e.printStackTrace();
        }
        catch (Exception e) {
           e.printStackTrace();
        }
    }
    
    
    
    /**
     * Used to set rendering hints for given graphics before actual
     * painting is done. At the moment this method doesn't do anything at all!
     * 
     * @param g 
     */
    public static void preparePaint(Graphics g) {
        /*
        if(UIConstants.ANTIALIASING && g instanceof Graphics2D) {
            RenderingHints qualityHints = new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            RenderingHints antialiasHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            RenderingHints metricsHints = new RenderingHints(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            ((Graphics2D) g).addRenderingHints(qualityHints);
            ((Graphics2D) g).addRenderingHints(antialiasHints);
            //((Graphics2D) g).addRenderingHints(metricsHints);
        }
        */
    }
    
    
    
    
    //private static Font fancyBaseFont = null;
    
    /**
     * Used to set a font of a component. Doesn't do anything at all at the moment. 
     * 
     * @param component 
     */
    public static void setFancyFont(Component component) {
        /*
        try {
            if(fancyBaseFont == null) {
                InputStream is = new BufferedInputStream(new FileInputStream("./resources/gui/fonts/Arimo-Regular.ttf"));
                fancyBaseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
            if(fancyBaseFont != null) {
                Font fancyFont = fancyBaseFont.deriveFont(Font.PLAIN, component.getFont().getSize());
                component.setFont(fancyFont);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        */
    }
    
    
}
