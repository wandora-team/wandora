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
 * Picture.java
 *
 * Created on 17. toukokuuta 2006, 16:38
 *
 */

package org.wandora.application.gui.previews.formats;




import org.wandora.application.gui.previews.*;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.Options;
import org.wandora.application.gui.simple.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.print.*;

import static java.awt.event.KeyEvent.*;

import org.wandora.application.gui.*;
import org.wandora.application.*;




/**
 *
 * @author akivela
 */
public class Picture extends JPanel implements Runnable, MouseListener, KeyListener, ImageObserver, ActionListener, Printable, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.imagePreviewPanel.";
    private static final double ZOOMFACTOR = 1.1;
    
    private String imageLocator;
    private BufferedImage tempImage;
    private Dimension panelDimensions;
    private Wandora admin;
    private Options options;

    private double zoomFactor = 1.0;
    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    
    /**
     * Creates a new instance of Picture
     */
    public Picture(String imageLocator, Wandora admin) {
        this.admin = admin;
        this.imageLocator = imageLocator;
        this.addMouseListener(this);
        this.addKeyListener(this);
        setImageSize(1.0);
        
        if(admin != null) {
            options = admin.options;
            if(options != null) {
                String zoomFactorString = options.get(OPTIONS_PREFIX + "imageSize");
                if(zoomFactorString != null) {
                    try {
                        setImageSize(Double.parseDouble(zoomFactorString));
                    }
                    catch(Exception e) {}
                }
            }
        }
//        setURL(imageLocator);
        reset(imageLocator);
    }
    
    
    private void reset(String imageLocator) {
        this.imageLocator = imageLocator;
        updateImageMenu();
        if(imageLocator != null && imageLocator.length() > 0) {
            Thread imageThread = new Thread(this);
            imageThread.start();
        }
        else {
            run();
        }
    }
    
    public void setImageSize(double newZoomFactor) {
        if(newZoomFactor > 0.1 && newZoomFactor < 10) {
            if(options != null) options.put(OPTIONS_PREFIX + "imageSize", newZoomFactor);
            zoomFactor = newZoomFactor;
            reset(imageLocator);
        }
    }
    
    

    
    @Override
    public void finish() {
        // Nothing here...
    }
    
    @Override
    public void stop() {}
    
    @Override
    public Component getGui() {
        return this;
    }
    

    
    
    @Override
    public void run() {
        if(imageLocator != null && imageLocator.length() > 0) {
            tempImage = UIBox.getThumbForLocator(imageLocator, admin.wandoraHttpAuthorizer);
            if(tempImage != null) {
                int targetWidth = (int) (tempImage.getWidth() * zoomFactor);
                int targetHeight = (int) (tempImage.getHeight() * zoomFactor);
                Graphics g = this.getGraphics();
                if(g != null)
                    g.drawImage(tempImage, 0,0, targetWidth, targetHeight, this);
                panelDimensions = new Dimension(targetWidth, targetHeight);
            }
        }
        else {
            panelDimensions = new Dimension(2, 2);
        }
        this.setPreferredSize(panelDimensions);
        this.setMaximumSize(panelDimensions);
        this.setMinimumSize(panelDimensions);
        
        repaint();
        revalidate();
    }
    

    public boolean imageUpdate(Picture img, int infoflags, int x, int y, int width, int height) {
        if((infoflags & ImageObserver.ALLBITS) != 0) {
            repaint();
            revalidate();
            return false;
        }
        return true;
    }
    
    
  
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(tempImage != null && panelDimensions != null) {
            //System.out.println(" image x =" + imageDimensions.width + ", y=" + imageDimensions.height );
            g.drawImage(tempImage,0,0,panelDimensions.width, panelDimensions.height, this);
        }
    }
    
    
    

    
   
    // -------------------------------------------------------------------------
    
    
    
    public void forkImageViewer() {
        if(imageLocator != null && imageLocator.length() > 0) {
            System.out.println("Spawning viewer for \""+imageLocator+"\"");
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(imageLocator));
            }
            catch(Exception tme) {
                tme.printStackTrace(); // TODO EXCEPTION
            }
        }
    }
   
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {
            forkImageViewer();
        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.requestFocus();
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    // ---------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if(keyCode == KeyEvent.VK_MINUS) {
            setImageSize(zoomFactor / ZOOMFACTOR);
        }
        else if(keyCode == KeyEvent.VK_PLUS) {
            setImageSize(zoomFactor * ZOOMFACTOR);
        }
        else if(keyCode == KeyEvent.VK_C && e.isControlDown()) {
            if(tempImage != null) {
                ClipboardBox.setClipboard(tempImage);
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {

    }
    @Override
    public void keyTyped(KeyEvent e) {

    }
        
        
    // -------------------------------------------------------------------------
    
    
    public void updateImageMenu() {
        if(imageLocator != null && imageLocator.length() > 0) {
            this.setComponentPopupMenu(getImageMenu());
        }
        else {
            this.setComponentPopupMenu(null);
        }
    }
    
    public JPopupMenu getImageMenu() {
        Object[] menuStructure = new Object[] {
            "Open in external viewer...",
            "---",
            "Zoom", new Object[] {
                "Zoom in", KeyStroke.getKeyStroke(VK_PLUS, 0),
                "Zoom out", KeyStroke.getKeyStroke(VK_MINUS, 0),
                "---",
                "25 %",
                "50 %",
                "100 %",
                "150 %",
                "200 %",
            },
            /*
            "Scale", new Object[] {
                (scaleToFit ? "X " : "O ") + "Scale to fit",
                (!scaleToFit ? "X " : "O ") + "No scaling",
            },
            */
            "---",
            "Copy image", KeyStroke.getKeyStroke(VK_C, CTRL_MASK),
            "Copy image location",
            "---",
            "Save image as...",
            "Print image...",
            
        };
        return UIBox.makePopupMenu(menuStructure, this);
    }
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        if(c.startsWith("Open in external")) {
            forkImageViewer();
        }
        else if(c.startsWith("25")) {
            setImageSize(0.25);
        }
        else if(c.startsWith("50")) {
            setImageSize(0.5);
        }
        else if(c.startsWith("100")) {
            setImageSize(1.0);
        }
        else if(c.startsWith("150")) {
            setImageSize(1.5);
        }
        else if(c.startsWith("200")) {
            setImageSize(2.0);
        }
        else if(c.startsWith("Zoom in")) {
            setImageSize(zoomFactor * 1.1);
        }
        else if(c.startsWith("Zoom out")) {
            setImageSize(zoomFactor / 1.1);
        }

        else if(c.equalsIgnoreCase("Copy image")) {
            if(tempImage != null) {
                ClipboardBox.setClipboard(tempImage);
            }
        }
        else if(c.equalsIgnoreCase("Copy image location")) {
            if(imageLocator != null) {
                ClipboardBox.setClipboard(imageLocator);
            }
        }
        else if(c.startsWith("Save image")) {
            if(tempImage != null) {
                save();
            }
        }
        else if(c.startsWith("Print image")) {
            if(tempImage != null) {
                print();
            }
        }
    }
   
   
   
   // ----------------------------------------------------------------- SAVE ---
   
   

    public void save() {
        Wandora wandora = Wandora.getWandora(this);
        SimpleFileChooser chooser = UIConstants.getFileChooser();
        chooser.setDialogTitle("Save image file");
        try {
            chooser.setSelectedFile(new File(imageLocator.substring(imageLocator.lastIndexOf(File.pathSeparator)+1)));
        }
        catch(Exception e) {}
        if(chooser.open(wandora,SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
        }
    }
    
    
    
    public void save(File imageFile) {
        if(imageFile != null) {
            try {
                String format = solveImageFormat(imageFile.getName());
                ImageIO.write(tempImage, format, imageFile);
            }
            catch(Exception e) {
                System.out.println("Exception '" + e.toString() + "' occurred while saving file '" + imageFile.getPath() + "'.");
            }
        }
    }
    
    
    
    public String solveImageFormat() {
        return solveImageFormat(imageLocator);
    }

    public String solveImageFormat(String fileName) {
        String fileFormat = "jpg";
        try {
            fileFormat = fileName.substring(fileName.lastIndexOf('.')+1);
            if(fileFormat == null || fileFormat.length() == 0) {
                fileFormat = "jpg";
            }
        }
        catch(Exception e) {}
        return fileFormat;
    }
    
    
    
    // ------------------------------------------------------------ PRINTING ---
    
    public void print() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch(PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
        }
    }
    
  
    @Override
    public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int param) throws java.awt.print.PrinterException {
        if (param > 0) {
            return(NO_SUCH_PAGE);
        } else {
            Graphics2D g2d = (Graphics2D)graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            // Turn off double buffering
            this.paint(g2d);
            // Turn double buffering back on
            return(PAGE_EXISTS);
        }
    }
    
    
}
