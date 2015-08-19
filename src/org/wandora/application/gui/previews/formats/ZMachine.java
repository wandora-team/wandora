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
 */

package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import static org.wandora.application.gui.previews.Util.startsWithAny;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.blorb.BlorbStory;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.media.Resources;
import org.zmpp.media.StoryMetadata;
import org.zmpp.swingui.ColorTranslator;
import org.zmpp.swingui.DisplaySettings;
import org.zmpp.swingui.FileSaveGameDataStore;
import org.zmpp.swingui.GameInfoDialog;
import org.zmpp.swingui.GameThread;
import org.zmpp.swingui.LineEditorImpl;
import org.zmpp.swingui.PreferencesDialog;
import org.zmpp.swingui.TextViewport;
import org.zmpp.swingui.Viewport6;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;
import org.zmpp.vmutil.FileUtils;



/**
 * Uses and is based on Wei-ju Wu's ZMMP (The Z-machine Preservation) Project.
 *
 * @author akivela
 */
public class ZMachine implements ActionListener, PreviewPanel {
    
    private String locator = null;
    private JPanel ui = null;
    private ZmppPanel gamePanel = null;
    private Machine machine = null;
    
    
    
    
    public ZMachine(String loc) {
        this.locator = loc;
    }
    
    
    
    public void runStoryFile(File storyfile) {

        // Read in the story file
        if (storyfile != null && storyfile.exists() && storyfile.isFile()) {
            JPanelMachineFactory factory;
            if(isZblorbSuffix(storyfile.getName())) {
                factory = new JPanelMachineFactory(storyfile);
            }
            else {
                File blorbfile = searchForResources(storyfile);
                factory = new JPanelMachineFactory(storyfile, blorbfile);
            }

            try {
                machine = factory.buildMachine();
                gamePanel = factory.getUI();      
                gamePanel.startMachine();
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                    String.format("Could not read game.\nReason: '%s'", ex.getMessage()),
                    "Story file error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        else {
            JOptionPane.showMessageDialog(null,
                String.format("The selected story file '%s' was not found",
                storyfile != null ? storyfile.getPath() : ""),
                "Story file not found", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    

    private static boolean isZblorbSuffix(String filename) {
        return filename.endsWith("zblorb") || filename.endsWith("zlb");
    }
  
    
    /**
     * Trys to find a resource file in Blorb format.
     * 
     * @param storyfile the storyfile
     * @return the blorb file if one exists or null
     */
    private static File searchForResources(File storyfile) {
        StringTokenizer tok = new StringTokenizer(storyfile.getName(), ".");
        String prefix = tok.nextToken();
        String dir = storyfile.getParent();
        String blorbpath1 = ((dir != null) ? dir + System.getProperty("file.separator") : "")
                            + prefix + ".blb";
        String blorbpath2 = ((dir != null) ? dir + System.getProperty("file.separator") : "")
                            + prefix + ".blorb";

        File blorbfile1 = new File(blorbpath1);
        System.out.printf("does '%s' exist ? -> %b\n", blorbfile1.getPath(), blorbfile1.exists());
        if (blorbfile1.exists()) return blorbfile1;

        File blorbfile2 = new File(blorbpath2);
        System.out.printf("does '%s' exist ? -> %b\n", blorbfile2.getPath(), blorbfile2.exists());
        if (blorbfile2.exists()) return blorbfile2;

        return null;
    }  
    
    
    
    private JPanel makeUI() {
        JPanel ui = new JPanel();
        
        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        if(gamePanel == null) {
            try {
                runStoryFile(new File((new URL(locator)).toURI()) );
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        JPanel gameWrapperPanel = new JPanel();
        // gameWrapperPanel.setBackground(Color.WHITE);
        if(gamePanel != null) gameWrapperPanel.add(gamePanel, BorderLayout.CENTER);
        
        ui.setLayout(new BorderLayout(8,8));       
        ui.add(gameWrapperPanel, BorderLayout.CENTER);
        ui.add(controllerPanel, BorderLayout.SOUTH);

        return ui;
    }
    
    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Restart", UIBox.getIcon(0xf04b), this,
            "Info", UIBox.getIcon(0xf04c), this,
            "Stop", UIBox.getIcon(0xf04d), this,
            "Open ext", UIBox.getIcon(0xf08e), this,
            "Copy location", UIBox.getIcon(0xf0c5), this,
            "Save as", UIBox.getIcon(0xf0c7), this, // f019
        }, this);
    }
    
    
    
    
    
    @Override
    public void stop() {
        if(machine != null) {
            System.out.println("Stopping machine!");
            //machine.quit();
        }
    }

    @Override
    public void finish() {
        if(machine != null) {
            System.out.println("Finishing machine!");
            //machine.quit();
        }
    }

    @Override
    public Component getGui() {
        if(ui == null) {
            ui = makeUI();
        }
        return ui;
    }

    @Override
    public boolean isHeavy() {
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if(startsWithAny(cmd, "Restart")) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if(machine != null) {
                        machine.restart();
                    }
                }
            });
        }
        else if(startsWithAny(cmd, "Stop")) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if(machine != null) {
                        machine.restart();
                    }
                }
            });
        }
        else if(startsWithAny(cmd, "Open ext")) {
            if(locator != null) {
                forkExternalPlayer();
            }
        }
        else if(startsWithAny(cmd, "Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(startsWithAny(cmd, "Save")) {
            if(locator != null) {
                
            }
        }
    }
    

    public void forkExternalPlayer() {
        if(locator != null && locator.length() > 0) {
            if(!DataURL.isDataURL(locator)) {
                System.out.println("Spawning viewer for \""+locator+"\"");
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(locator));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), 
                        "Due to Java's security restrictions Wandora can't open the DataURI "+
                        "in external application. Manually copy and paste the locator to browser's "+
                        "address field to view the locator.", 
                        "Can't open the locator in external application",
                        WandoraOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        if(url != null) {
            if(DataURL.isDataURL(url)) {
                try {
                    DataURL dataURL = new DataURL(url);
                    String mimeType = dataURL.getMimetype();
                    if(mimeType != null) {
                        String lowercaseMimeType = mimeType.toLowerCase();
                        if(lowercaseMimeType.startsWith("application/z1") ||
                           lowercaseMimeType.startsWith("application/z2") ||
                           lowercaseMimeType.startsWith("application/z3") ||
                           lowercaseMimeType.startsWith("application/z4") ||
                           lowercaseMimeType.startsWith("application/z5") ||
                           lowercaseMimeType.startsWith("application/z6") ||
                           lowercaseMimeType.startsWith("application/z7") ||
                           lowercaseMimeType.startsWith("application/z8") ||
                           lowercaseMimeType.startsWith("application/zblorb")) {
                                return true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".z1", ".z2", ".z3", ".z4", ".z5", ".z6", ".z7", ".z8", ".zblorb", "zlb")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------ JPanelMachineFactory ---
    // -------------------------------------------------------------------------
    
    
    
    public class JPanelMachineFactory extends MachineFactory<ZmppPanel> {

        private File storyfile;
        private File blorbfile;
        private ZmppPanel zmppPanel;
        private FormChunk blorbchunk;
        private SaveGameDataStore savegamestore;
  
        public JPanelMachineFactory(File storyfile, File blorbfile) {
            this.storyfile = storyfile;
            this.blorbfile = blorbfile;
        }

        public JPanelMachineFactory(File blorbfile) {
            this.blorbfile = blorbfile;
        }
  
        /**
         * {@inheritDoc}
         */
        protected byte[] readStoryData() throws IOException {

            if (storyfile != null) {
                return FileUtils.readFileBytes(storyfile);
            } 
            else {
              // Read from Z BLORB
              FormChunk formchunk = readBlorb();
              return formchunk != null ? new BlorbStory(formchunk).getStoryData() : null;
            }
        }
  
        private FormChunk readBlorb() throws IOException {
            if (blorbchunk == null) {
                byte[] data = FileUtils.readFileBytes(blorbfile);
                if (data != null) {
                    blorbchunk = new DefaultFormChunk(new DefaultMemoryAccess(data));
                    if (!"IFRS".equals(new String(blorbchunk.getSubId()))) {
                        throw new IOException("not a valid Blorb file");
                    }
                }
            }
            return blorbchunk;
        }
  
        /**
         * {@inheritDoc}
         */
        protected Resources readResources() throws IOException {
            FormChunk formchunk = readBlorb();
            return (formchunk != null) ? new BlorbResources(formchunk) : null;
        }

        /**
         * {@inheritDoc}
         */
        protected void reportInvalidStory() {
            JOptionPane.showMessageDialog(null,
                "Invalid story file.",
                "Story file read error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
      
        
        /**
         * {@inheritDoc}
         */
        protected ZmppPanel initUI(Machine machine) {

          zmppPanel = new ZmppPanel(machine);
          savegamestore = new FileSaveGameDataStore(zmppPanel);
          return zmppPanel;
        }

        /**
         * {@inheritDoc}
         */
        public ZmppPanel getUI() { return zmppPanel; }

        /**
         * {@inheritDoc}
         */
        protected IOSystem getIOSystem() { return zmppPanel; }  

        /**
         * {@inheritDoc}
         */
        protected InputStream getKeyboardInputStream() { return zmppPanel; }

        /**
         * {@inheritDoc}
         */
        protected StatusLine getStatusLine() { return zmppPanel; }

        /**
         * {@inheritDoc}
         */
        protected ScreenModel getScreenModel() { return zmppPanel.getScreenModel(); }

        /**
         * {@inheritDoc}
         */
        protected SaveGameDataStore getSaveGameDataStore() { return savegamestore; }
    }

    
    
    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- ZmppPanel ---
    // -------------------------------------------------------------------------
    
    
    
    public class ZmppPanel extends JPanel implements InputStream, StatusLine, IOSystem {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        private JLabel global1ObjectLabel;
        private JLabel statusLabel;
        private ScreenModel screen;
        private Machine machine;
        private LineEditorImpl lineEditor;
        private GameThread currentGame;
        private DisplaySettings settings;
        private Preferences preferences;

        /**
         * Constructor.
         * 
         * @param machine a Machine object
         */
        public ZmppPanel(final Machine machine) {

            this.machine = machine;
            lineEditor = new LineEditorImpl(machine.getGameData().getStoryFileHeader(),
                machine.getGameData().getZsciiEncoding());

            JComponent view = null;

            preferences = Preferences.userNodeForPackage(ZmppPanel.class);
            settings = createDisplaySettings(preferences);

            if (machine.getGameData().getStoryFileHeader().getVersion() ==  6) {
                view = new Viewport6(machine, lineEditor, settings);
                screen = (ScreenModel) view;
            } 
            else {
                view = new TextViewport(machine, lineEditor, settings);
                screen = (ScreenModel) view;
            }
            view.setPreferredSize(new Dimension(640, 476));
            view.setMinimumSize(new Dimension(400, 300));

            if (machine.getGameData().getStoryFileHeader().getVersion() <= 3) {
                JPanel statusPanel = new JPanel(new GridLayout(1, 2));
                JPanel status1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JPanel status2Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                statusPanel.add(status1Panel);
                statusPanel.add(status2Panel);

                global1ObjectLabel = new JLabel(" ");
                statusLabel = new JLabel(" ");
                status1Panel.add(global1ObjectLabel);
                status2Panel.add(statusLabel);    
                this.add(statusPanel, BorderLayout.NORTH);
                this.add(view, BorderLayout.CENTER);
            } 
            else {
                this.add(view, BorderLayout.CENTER);
            }

            JPopupMenu menubar = new JPopupMenu();
            this.setComponentPopupMenu(menubar);

            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic('F');
            menubar.add(fileMenu);

            // Quit is already in the application menu
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.setMnemonic('x');
            fileMenu.add(exitItem);
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // System.exit(0);
                    System.out.println("EXIT!");
                }
            });

            JMenu editMenu = new JMenu("Edit");
            menubar.add(editMenu);
            editMenu.setMnemonic('E');
            JMenuItem preferencesItem = new JMenuItem("Preferences...");
            preferencesItem.setMnemonic('P');
            editMenu.add(preferencesItem);
            preferencesItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editPreferences();
                }
            });

            JMenu helpMenu = new JMenu("Help");
            menubar.add(helpMenu);
            helpMenu.setMnemonic('H');

            JMenuItem aboutItem = new JMenuItem("About ZMPP...");
            aboutItem.setMnemonic('A');
            helpMenu.add(aboutItem);
            aboutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    about();
                }
            });

            //addKeyListener(lineEditor);
            view.addKeyListener(lineEditor);
            view.addMouseListener(lineEditor);

            // just for debugging
            view.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    //System.out.printf("mouse pos: %d %d\n", e.getX(), e.getY());
                }
            });

            // Add an info dialog and a title if metadata exists
            Resources resources = machine.getGameData().getResources();
            if (resources != null && resources.getMetadata() != null) {
                StoryMetadata storyinfo = resources.getMetadata().getStoryInfo();
                setTitle(storyinfo.getTitle() + " (" + storyinfo.getAuthor() + ")");

                JMenuItem aboutGameItem = new JMenuItem("About this Game ...");
                helpMenu.add(aboutGameItem);
                aboutGameItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        aboutGame();
                    }
                });
            }
        }
        


        public void setTitle(String title) {
            System.out.println("title: "+title);
        }


        /**
         * Access to screen model.
         * 
         * @return the screen model
         */
        public ScreenModel getScreenModel() {
            return screen;
        }

        public void startMachine() {
            currentGame = new GameThread(machine, screen);
            currentGame.start();
        }

        
        
        
        // *************************************************************************
        // ******** StatusLine interface
        // ******************************************

        
        @Override
        public void updateStatusScore(final String objectName, final int score, final int steps) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    global1ObjectLabel.setText(objectName);
                    statusLabel.setText(score + "/" + steps);
                }
            });
        }

        
        @Override
        public void updateStatusTime(final String objectName, final int hours, final int minutes) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    global1ObjectLabel.setText(objectName);
                    statusLabel.setText(String.format("%02d:%02d", hours, minutes));
                }
            });
        }
        
                

        // *************************************************************************
        // ******** IOSystem interface
        // ******************************************

        
        @Override
        public Writer getTranscriptWriter() {
            File currentdir = new File(System.getProperty("user.dir"));    
            JFileChooser fileChooser = new JFileChooser(currentdir);
            fileChooser.setDialogTitle("Set transcript file ...");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                  return new FileWriter(fileChooser.getSelectedFile());
                }
                catch (IOException ex) {
                  ex.printStackTrace();
                }
            }
            return null;
        }

        
        @Override
        public Reader getInputStreamReader() {
            File currentdir = new File(System.getProperty("user.dir"));    
            JFileChooser fileChooser = new JFileChooser(currentdir);
            fileChooser.setDialogTitle("Set input stream file ...");
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                  return new FileReader(fileChooser.getSelectedFile());
                }
                catch (IOException ex) {
                  ex.printStackTrace();
                }
            }
            return null;
        }

        
        
        
        
        // *************************************************************************
        // ******** InputStream interface
        // ******************************************

        
        public void close() { 
        }

        
        public void cancelInput() {
            lineEditor.cancelInput();
        }

        /**
         * {@inheritDoc}
         */
        public short getZsciiChar(boolean flushBeforeGet) {
            enterEditMode(flushBeforeGet);
            short zsciiChar = lineEditor.nextZsciiChar();
            leaveEditMode(flushBeforeGet);
            return zsciiChar;
        }
        

        private void enterEditMode(boolean flushbuffer) {
            if (!lineEditor.isInputMode()) {
                screen.resetPagers();
                lineEditor.setInputMode(true, flushbuffer);
            }
        }

        
        private void leaveEditMode(boolean flushbuffer) {
            lineEditor.setInputMode(false, flushbuffer);
        }

        
        
        // -------------------------------------
        
        
        private void about() {
          JOptionPane.showMessageDialog(this,
              "\n\u00a9 2005-2006 by Wei-ju Wu\n" +
              "This software is released under the GNU public license.",
              "About...",
              JOptionPane.INFORMATION_MESSAGE);
        }

        
        private void aboutGame() {
            GameInfoDialog dialog = new GameInfoDialog(Wandora.getWandora(),machine.getGameData().getResources());
            dialog.setVisible(true);
        }
        

        private void editPreferences() {
            PreferencesDialog dialog = new PreferencesDialog(Wandora.getWandora(), preferences, settings);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        
        private DisplaySettings createDisplaySettings(Preferences preferences) {
            int stdfontsize = preferences.getInt("stdfontsize", 12);
            int fixedfontsize = preferences.getInt("fixedfontsize", 12);
            int defaultforeground = preferences.getInt("defaultforeground", ColorTranslator.UNDEFINED);
            int defaultbackground = preferences.getInt("defaultbackground", ColorTranslator.UNDEFINED);
            boolean antialias = preferences.getBoolean("antialias", true);

            return new DisplaySettings(stdfontsize, fixedfontsize, defaultbackground, defaultforeground, antialias);    
        }

        
    }

    
}
