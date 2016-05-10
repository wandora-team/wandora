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
 */


package org.wandora.application.gui.previews.formats;

import de.joergjahnke.c64.core.C1541;
import de.joergjahnke.c64.core.C64;
import de.joergjahnke.c64.extendeddevices.EmulatorUtils;
import de.joergjahnke.c64.swing.C64Canvas;
import de.joergjahnke.common.extendeddevices.WavePlayer;
import de.joergjahnke.common.vmabstraction.sunvm.SunVMResourceLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.application.tools.extractors.files.SimpleFileExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;



/**
 *
 * @author akivela
 */
public class ApplicationC64 implements ActionListener, PreviewPanel, ComponentListener {

    private String locator = null;
    private String imageData = null; // disk image
    
    private WavePlayer wavePlayer = null;
    private C64Canvas c64canvas = null;
    private C64 c64 = null;
    private JPanel c64wrapper = null;
    private JPanel ui = null;
    
    private static Properties settings = new Properties();
    //private final HashMap<Integer,String> attachedImages = new HashMap();
    

    private int sizeScaler = 1; // 1,2,3
    private int joystickPort = 0; // 0,1
    private int driveEmulationMode;
    private int mouseEmulationMode;
    private boolean automaticTurboMode = false;
    private String userInput = "";
    
    
    
    public ApplicationC64(String locator) {
        this.locator = locator;
    }
    
    
    

    @Override
    public void stop() {
        stopC64();
    }
    

    @Override
    public void finish() {
        stopC64();
    }

    
    private void stopC64() {
        if(c64 != null) {
            c64.getVIC().reset();
            c64.getSID().reset();
            c64.getKeyboard().reset();
            c64.getIECBus().reset();
            c64.getCPU().reset();
            c64.getCIA(0).reset();
            c64.getCIA(1).reset();
            
            c64.stop();
            
            wavePlayer.stop();
            
            c64 = null;
        }
    }
    

    
    @Override
    public Component getGui() {
        if(ui == null) {
            ui = new JPanel();
            ui.setLayout(new BorderLayout(8,8));
            
            c64wrapper = new JPanel();
            
            try {
                if(c64canvas == null) {
                    this.c64canvas = new C64Canvas();
                    this.c64canvas.addComponentListener(this);

                    // create C64 instance and inform the canvas about this instance
                    this.c64 = new C64(new SunVMResourceLoader());
                    this.c64canvas.setC64(c64);

                    // create a player that observes the SID and plays its sound
                    wavePlayer = new WavePlayer(this.c64.getSID());
                    this.c64.getSID().addObserver(wavePlayer);
                    this.c64.setActiveDrive(0);
                    this.c64.setThrottlingEnabled(true);
                    
                    setDriveEmulation(C1541.COMPATIBLE_EMULATION);
                    setMouseUsage(C64Canvas.MOUSE_AS_FIRE_BUTTON);
                }

                JPanel toolbarWrapper = new JPanel();
                toolbarWrapper.add(getJToolBar());

                c64wrapper.add(c64canvas);
                
                ui.add(c64wrapper, BorderLayout.CENTER);
                ui.add(toolbarWrapper, BorderLayout.SOUTH);
                
                // start the emulation
                new Thread(this.c64).start();

                attach(locator);
                autoloadProgram();
                runProgram();
            }
            catch(Exception e) {
                PreviewUtils.previewError(ui, "Can't initialize text viewer. Exception occurred.", e);
            }
        }
        return ui;
    }

    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            getMenuButton(),
        }, this);
    }
    
    
    
    
    public JButton getMenuButton() {
        JButton button = UIBox.makeDefaultButton();
        button.setIcon(UIBox.getIcon(0xf0c9));
        button.setToolTipText("C64 preview options.");
        button.setBorder(null);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getOptionsMenu().show(button, e.getX(), e.getY());
            }

        });
        return button;
    }
    
    

    private static final Icon selectedIcon = UIBox.getIcon("gui/icons/checked2.png");
    private static final Icon unselectedIcon = UIBox.getIcon("gui/icons/empty.png");
    
    public JPopupMenu getOptionsMenu() {
        Object[] menuStruct = new Object[] {
            "Open subject locator in ext", PreviewUtils.ICON_OPEN_EXT,
            "Copy subject locator", PreviewUtils.ICON_COPY_LOCATION,
            "Save subject locator resource...", PreviewUtils.ICON_SAVE,
            "---",
            "Reify snapshot",
            "Reify screen capture",
            "---",
            "Type text...",
            "Enter a special C64 key...",
            "---",
            "Joystick", new Object[] {
                "Use joystick port 1", joystickPort == 0 ? selectedIcon : unselectedIcon,
                "Use joystick port 2", joystickPort == 1 ? selectedIcon : unselectedIcon,
                "---",
                "Mouse emulates joystick button only", mouseEmulationMode == C64Canvas.MOUSE_AS_FIRE_BUTTON ? selectedIcon : unselectedIcon,
                "Mouse emulates virtual joystick", mouseEmulationMode == C64Canvas.MOUSE_FOR_VIRTUAL_JOYSTICK ? selectedIcon : unselectedIcon,
                "No joystick emulation", mouseEmulationMode == C64Canvas.MOUSE_NO_USAGE ? selectedIcon : unselectedIcon,
            },
            /*
            "Turbo mode", new Object[] {
                "Auto turbo mode",
                "Turbo mode on",
                "Turbo mode off"
            },
            */
            "Floppy drive mode", new Object[] {
                "Fast emulation", driveEmulationMode == C1541.FAST_EMULATION ? selectedIcon : unselectedIcon,
                "Balanced emulation", driveEmulationMode == C1541.BALANCED_EMULATION ? selectedIcon : unselectedIcon,
                "Compatible emulation", driveEmulationMode == C1541.COMPATIBLE_EMULATION ? selectedIcon : unselectedIcon,
            },
            "Display size", new Object[] {
                "1x", sizeScaler == 1 ? selectedIcon : unselectedIcon,
                "2x", sizeScaler == 2 ? selectedIcon : unselectedIcon,
                "3x", sizeScaler == 3 ? selectedIcon : unselectedIcon,
            },
            "---",
            "Pause", c64.isPaused() ? selectedIcon : unselectedIcon,
            "Reset",
            "---",
            "About"
        };
        JPopupMenu optionsPopup = UIBox.makePopupMenu(menuStruct, this);
        return optionsPopup;
    }

    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        if(c64 == null) return;
        if(c64canvas == null) return;
        
        System.out.println("ApplicationC64 action '"+c+"'.");
        
        
        if("Open subject locator in ext".equalsIgnoreCase(c)) {
            PreviewUtils.forkExternalPlayer(locator);
        }
        else if("Copy subject locator".equalsIgnoreCase(c)) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if("Save subject locator resource...".equalsIgnoreCase(c)) {
            PreviewUtils.saveToFile(locator);
        }
        
        
        
        else if("Reify snapshot".equalsIgnoreCase(c)) {
            DataURL snapshot = makeSnapshot();
            if(snapshot != null) {
                saveToTopic(snapshot);
            }
        }
        else if("Reify screen capture".equalsIgnoreCase(c)) {
            DataURL screenCapture = makeScreenCapture();
            if(screenCapture != null) {
                saveToTopic(screenCapture);
            }
        }     

        else if("Type text...".equalsIgnoreCase(c)) {
            c64.pause();
            userInput = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Enter input text", userInput, "Input text");
            c64.resume();
            if(userInput != null && userInput.length() == 0) {
                c64.getKeyboard().textTyped(userInput);
            }
        }    

        
        else if("Enter a special C64 key...".equalsIgnoreCase(c)) {
            // show a dialog to let the user select the special key
            final String[] specialKeys = { "Run", "Break", "Commodore", "Pound" };
            final Object key = JOptionPane.showInputDialog(
                    Wandora.getWandora(), 
                    "Select a key", "Special keys", 
                    JOptionPane.PLAIN_MESSAGE, 
                    null, 
                    specialKeys, 
                    null);

            if(key != null) {
                this.c64.getKeyboard().keyTyped(key.toString().toUpperCase());
            }
        }
        
        
        
        else if("1x".equalsIgnoreCase(c)) {
            setScaling(1);
        }
        else if("2x".equalsIgnoreCase(c)) {
            setScaling(2);
        }
        else if("3x".equalsIgnoreCase(c)) {
            setScaling(3);
        }
        else if("Display size".equalsIgnoreCase(c)) {
            sizeScaler += 1;
            if(sizeScaler > 3) sizeScaler = 1;
            setScaling(sizeScaler);
        }
        

        
        else if("Joystick port".equalsIgnoreCase(c)) {
            setActiveJoystick(joystickPort == 0 ? 1 : 0);
        }
        else if("Use joystick port 1".equalsIgnoreCase(c)) {
            setActiveJoystick(0);
        }
        else if("Use joystick port 2".equalsIgnoreCase(c)) {
            setActiveJoystick(1);
        }
        
        
        else if("Mouse emulates joystick button only".equalsIgnoreCase(c)) {
            setMouseUsage(C64Canvas.MOUSE_AS_FIRE_BUTTON);
        }
        else if("Mouse emulates virtual joystick".equalsIgnoreCase(c)) {
            setMouseUsage(C64Canvas.MOUSE_FOR_VIRTUAL_JOYSTICK);
        }
        else if("No joystick emulation".equalsIgnoreCase(c)) {
            setMouseUsage(C64Canvas.MOUSE_NO_USAGE);
        }

        
        else if("Fast emulation".equalsIgnoreCase(c)) {
            setDriveEmulation(C1541.FAST_EMULATION);
        }
        else if("Balanced emulation".equalsIgnoreCase(c)) {
            setDriveEmulation(C1541.BALANCED_EMULATION);
        }
        else if("Compatible emulation".equalsIgnoreCase(c)) {
            setDriveEmulation(C1541.COMPATIBLE_EMULATION);
        }
 
        
        
        else if("Auto turbo mode".equalsIgnoreCase(c)) {
            setTurboMode(true, true);
        }
        else if("Turbo mode on".equalsIgnoreCase(c)) {
            setTurboMode(true, false);
        }
        else if("Turbo mode off".equalsIgnoreCase(c)) {
            setTurboMode(false, false);
        }
        
        
        else if("Reset".equalsIgnoreCase(c)) {
            c64.reset();
        }
        
        else if("Pause".equalsIgnoreCase(c)) {
            if(c64.isPaused()) c64.resume();
            else c64.pause();
        }
        else if("Resume".equalsIgnoreCase(c)) {
            c64.resume();
        }
        
        else if("About".equalsIgnoreCase(c)) {
            StringBuilder aboutBuilder = new StringBuilder("");
            aboutBuilder.append("Wandora's C64 emulation support is based on JSwingC64 version 1.10.4 ");
            aboutBuilder.append("created and copyrighted by Joerg Jahnke 2006-2009 ");
            aboutBuilder.append("distributed under the license of GNU GPL. \n\n");
            aboutBuilder.append("The original Commodore 64 ROM images are included with kind permission of Commodore Internation Corporation.");
            
            c64.pause();
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), aboutBuilder.toString(), "About C64 emulation");
            c64.resume();
        }
    }
    
    
    
    private void setScaling(int scale) {
        sizeScaler = scale;
        c64canvas.setScaling(scale);
        c64wrapper.validate();
        c64wrapper.repaint();
    }
    
    
    private void setActiveJoystick(int port) {
        joystickPort = port;
        c64.setActiveJoystick(port);
    }
    
    
    private void setMouseUsage(int emulationMode) {
        mouseEmulationMode = emulationMode;
        c64canvas.setMouseUsage(emulationMode);
    }
    
    private void setDriveEmulation(int emulationMode) {
        driveEmulationMode = emulationMode;
        for (int i=0; i<C64.MAX_NUM_DRIVES; ++i) {
            this.c64.getDrive(i).setEmulationLevel(driveEmulationMode);
        }
    }
    
    private void setTurboMode(boolean throttling, boolean autoTurbo) {
        c64.setThrottlingEnabled(throttling);
        automaticTurboMode = autoTurbo;
    }
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "application/d64",
                    "application/x-d64",
                    "application/x-cbm-d64",
                    "application/t64",
                    "application/x-t64",
                    "application/x-cbm-t64",
                    "application/x-c64-program",
                    "application/x-c64-snaphot"
                },
                new String[] { 
                    ".d64", 
                    ".t64", 
                    ".prg", 
                    ".p00"
                }
        );
    }
    
    
    // -------------------------------------------------------------------------
    
    
    private void refreshSize() {
        if(c64canvas != null) {
            c64wrapper.setPreferredSize(c64canvas.getPreferredSize());
            c64wrapper.setSize(c64canvas.getPreferredSize());
            c64wrapper.revalidate();
            ui.validate();
        }
    }
    

    @Override
    public void componentResized(ComponentEvent e) {
        refreshSize();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        refreshSize();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        refreshSize();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        refreshSize();
    }
    
    
    // -------------------------------------------------------------------------
    
    

    private void attach(String data) {
        try {
            if(DataURL.isDataURL(data)) {
                DataURL dataUrl = new DataURL(data);
                if(dataUrl.getMimetype().equalsIgnoreCase("application/x-c64-snaphot")) {
                    c64.pause();
                    DataInputStream in = new DataInputStream(new BufferedInputStream(dataUrl.getDataStream()));

                    int numberOfDrives = in.readInt();
                    // attached the previously attached images
                    int driveNo = in.readInt();
                    
                    int imageDataSize = in.readInt();
                    byte[] imageDataBytes = new byte[imageDataSize];
                    in.readFully(imageDataBytes);
                    
                    imageData = new String(imageDataBytes, "UTF-8");
                    attach(imageData);

                    // load the emulator state
                    c64.deserialize(in);
                    c64.resume();
                }
                else {
                    imageData = data;
                    File tmpFile = dataUrl.createTempFile();
                    if(tmpFile != null) {
                        System.out.println("disk attached: "+tmpFile.getAbsolutePath());
                        EmulatorUtils.attachImage(c64, c64.getActiveDrive(), tmpFile.getAbsolutePath());
                        //attachedImages.put(new Integer(c64.getActiveDrive()), tmpFile.getAbsolutePath());
                    }
                    else {
                        PreviewUtils.previewError(ui, "Unable to create temporal file for a dataurl.");
                    }
                }
            }
            else {
                imageData = data;
                File imageFile = null;
                if(data.startsWith("file")) {
                    imageFile = new File(new URL(data).toURI());
                }
                else {
                    DataURL dataUrl = new DataURL(new URL(data));
                    imageFile = dataUrl.createTempFile();
                }
                String imageFilename = imageFile.getAbsolutePath();
                EmulatorUtils.attachImage(c64, c64.getActiveDrive(), imageFilename);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Detach all images from all drives
     */
    private void detachImages() {
        // detach images from all drives
        for (int i = 0; i < C64.MAX_NUM_DRIVES; ++i) {
            c64.getDrive(i).detachImage();
        }
    }
    
    
    private void autoloadProgram() {
        this.c64.getKeyboard().textTyped("load \"*\",8,1");
        this.c64.getKeyboard().keyTyped("ENTER");
    }
    

    private void runProgram() {
        this.c64.getKeyboard().textTyped("run");
        this.c64.getKeyboard().keyTyped("ENTER");
    }
    
    
    
    private DataURL makeSnapshot() {
        this.c64.pause();

        DataURL snapshot = null;
        DataOutputStream out = null;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try {
            out = new DataOutputStream(new BufferedOutputStream(byteOutputStream));

            // save attached image
            out.writeInt(1);
            out.writeInt(c64.getActiveDrive());
            
            byte[] imageDataBytes = imageData.getBytes("UTF-8");
            out.writeInt(imageDataBytes.length);
            out.write(imageDataBytes);

            // save current emulator state
            c64.serialize(out);

            out.close();
            
            snapshot = new DataURL(byteOutputStream.toByteArray());
            snapshot.setMimetype("application/x-c64-snaphot");
        }
        catch (Throwable t) {
            t.printStackTrace();
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        c64.resume();
        return snapshot;
    }
    
    
    private DataURL makeScreenCapture() {
        try {
            BufferedImage screenCaptureImage = new BufferedImage(c64canvas.getPreferredSize().width, c64canvas.getPreferredSize().height, BufferedImage.TYPE_INT_RGB); 
            Graphics screenCaptureGraphics = screenCaptureImage.createGraphics();
            c64canvas.paintAll(screenCaptureGraphics);
            DataURL screenCaptureDataUrl = null;
            screenCaptureDataUrl = new DataURL(screenCaptureImage);
            return screenCaptureDataUrl;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    private void saveToTopic(DataURL dataUrl) {
        Wandora wandora = Wandora.getWandora();
        try {
            TopicMap topicMap = wandora.getTopicMap();

            SimpleFileExtractor simpleFileExtractor = new SimpleFileExtractor();
            simpleFileExtractor._extractTopicsFrom(dataUrl.toExternalForm(), topicMap);

            wandora.doRefresh();
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }

}