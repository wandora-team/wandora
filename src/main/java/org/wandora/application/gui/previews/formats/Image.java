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
 * Image.java
 *
 * Created on 17. toukokuuta 2006, 16:38
 *
 */

package org.wandora.application.gui.previews.formats;




import static java.awt.event.InputEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_PLUS;
import static org.wandora.application.gui.previews.PreviewUtils.startsWithAny;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.Options;





/**
 *
 * @author akivela
 */
public class Image extends JPanel implements Runnable, MouseListener, KeyListener, ImageObserver, ActionListener, Printable, PreviewPanel {
    private static final String OPTIONS_PREFIX = "gui.imagePreviewPanel.";
    private static final double ZOOMFACTOR = 1.1;
    
    private String imageLocator;
    private BufferedImage image;
    private BufferedImage scaledImage;
    
    private Dimension panelDimensions;
    private Wandora wandora;
    private Options options;

    private double zoomFactor = 1.0;
    
    private JPanel wrapperPanel = null;
    
    
    @Override
    public boolean isHeavy() {
        return false;
    }
    

    
    
    
    public Image(String imageLocator) {
        this.wandora = Wandora.getWandora();
        this.imageLocator = imageLocator;

        this.addMouseListener(this);
        this.addKeyListener(this);
        setImageSize(1.0);
        
        if(wandora != null) {
            options = wandora.options;
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
        reset();
        
        
        JPanel controllerPanel = new JPanel();
        controllerPanel.add(getJToolBar(), BorderLayout.CENTER);
        
        JPanel imagePanel = new JPanel();
        imagePanel.add(this, BorderLayout.CENTER);
        
        wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BorderLayout(8,8));
        wrapperPanel.add(imagePanel, BorderLayout.CENTER);
        wrapperPanel.add(controllerPanel, BorderLayout.SOUTH);
    }
    
    
    private void reset() {
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
            reset();
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
        return wrapperPanel;
    }
    

    
    
    @Override
    public void run() {
        if(imageLocator != null && imageLocator.length() > 0) {
            image = UIBox.getThumbForLocator(imageLocator, wandora.wandoraHttpAuthorizer);
        }
        if(image != null) {
            int targetWidth = (int) (image.getWidth() * zoomFactor);
            int targetHeight = (int) (image.getHeight() * zoomFactor);
            scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = scaledImage.createGraphics();
            if(g != null) {
                if(g instanceof Graphics2D) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.drawImage(image, 0,0, targetWidth, targetHeight, this);
                    g2.dispose();
                }
                else {
                    g.drawImage(image, 0,0, targetWidth, targetHeight, this);
                }
            }
            panelDimensions = new Dimension(targetWidth, targetHeight);
        }
        else {
            panelDimensions = new Dimension(2, 2);
        }
        this.setPreferredSize(panelDimensions);
        this.setMaximumSize(panelDimensions);
        this.setMinimumSize(panelDimensions);
        
        updateImageMenu();
        repaint();
        revalidate();
    }
    

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
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
        if(scaledImage != null && panelDimensions != null) {
            //System.out.println(" image x =" + imageDimensions.width + ", y=" + imageDimensions.height );
            g.drawImage(scaledImage,0,0,scaledImage.getWidth(), scaledImage.getHeight(), this);
        }
    }
    

    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {
            PreviewUtils.forkExternalPlayer(imageLocator);
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
            if(image != null) {
                ClipboardBox.setClipboard(image);
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
    
    
    private JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Zoom in", PreviewUtils.ICON_ZOOM_IN, this,
            "Zoom out", PreviewUtils.ICON_ZOOM_OUT, this,
            "Reset size", PreviewUtils.ICON_ZOOM_RESET, this,
            "---",
            "Copy image", PreviewUtils.ICON_COPY_IMAGE, this,
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Save", PreviewUtils.ICON_SAVE, this, // f019
            "Print", PreviewUtils.ICON_PRINT, this,
        }, this);
    }

        
    // -------------------------------------------------------------------------
    
    
    public void updateImageMenu() {
        if(image != null) {
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
        
        if(startsWithAny(c, "Open in external", "Open ext")) {
            PreviewUtils.forkExternalPlayer(imageLocator);
        }
        else if(startsWithAny(c, "25")) {
            setImageSize(0.25);
        }
        else if(startsWithAny(c, "50")) {
            setImageSize(0.5);
        }
        else if(startsWithAny(c, "100")) {
            setImageSize(1.0);
        }
        else if(startsWithAny(c, "150")) {
            setImageSize(1.5);
        }
        else if(startsWithAny(c, "200")) {
            setImageSize(2.0);
        }
        else if(startsWithAny(c, "Zoom in")) {
            setImageSize(zoomFactor * 1.1);
        }
        else if(startsWithAny(c, "Zoom out")) {
            setImageSize(zoomFactor / 1.1);
        }
        else if(startsWithAny(c, "Reset size")) {
            setImageSize(1.0);
        }
        else if(startsWithAny(c, "Copy image")) {
            if(image != null) {
                ClipboardBox.setClipboard(image);
            }
        }
        else if(startsWithAny(c, "Copy location")) {
            if(imageLocator != null) {
                ClipboardBox.setClipboard(imageLocator);
            }
        }
        else if(startsWithAny(c, "Save")) {
            if(image != null) {
                save();
            }
        }
        else if(startsWithAny(c, "Print")) {
            if(image != null) {
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
                ImageIO.write(image, format, imageFile);
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
    
    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { "image" }, 
                new String[] { "gif", "jpg", "jpeg", "tif", "tiff", "bmp", "png" }
        );
    }
        
    
}
