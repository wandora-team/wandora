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
 * ApplicationPDF.java
 *
 * Created on 12. lokakuuta 2007, 17:14
 *
 */

package org.wandora.application.gui.previews.formats;

import com.sun.pdfview.PDFFile;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import javax.swing.*;
import javax.swing.event.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.utils.*;
import static org.wandora.utils.Functional.*;
import static java.awt.event.KeyEvent.*;
import java.net.URISyntaxException;
import java.net.URL;





public class ApplicationPDF implements PreviewPanel {
    private static double ZOOMFACTOR = 0.8;
    private PDFFile pdfFile;
    private final ApplicationPDFPanel pdfPanel = new ApplicationPDFPanel();
    private JPanel masterPanel = null;
    private int pageCount;
    private int currentPage;
    private final PDFActionListener actionListener = new PDFActionListener();
    
    final JPopupMenu menu = UIBox.makePopupMenu(popupStructure, actionListener);
    final MenuElement pageNumItem = menu.getSubElements()[0];
    final JMenuItem item = ((JMenuItem)pageNumItem.getComponent());
    
    private final Pr2<Integer, Integer> setPageText = new Pr2<Integer, Integer>() {
        @Override
        public void invoke(final Integer page,final Integer count) {
            item.setText("Page " + page + " of " + count);
        }
    };

    private final String source;


    
    public ApplicationPDF(String pdfLocator) {
        this.source = pdfLocator;
        currentPage = 0;
    }

    
    private void initialize() throws FileNotFoundException, IOException, MalformedURLException, URISyntaxException, Exception {
        
        pdfPanel.addMouseListener(actionListener);
        pdfPanel.addMouseMotionListener(actionListener);
        pdfPanel.addKeyListener(actionListener);
        pdfPanel.setComponentPopupMenu(menu);
        
        byte[] pdfBytes = null;
        if(DataURL.isDataURL(source)) {
            pdfBytes = new DataURL(source).getData();
        }
        else {
            URL sourceURL = new URL(source);
            if("file".equalsIgnoreCase(sourceURL.getProtocol())) {
                pdfBytes = Files.readAllBytes(new File(sourceURL.toURI()).toPath());
            }
            else {
                pdfBytes = IObox.fetchUrl(sourceURL);
            }
        }
        
        if(pdfBytes != null) {
            pdfFile = new PDFFile(ByteBuffer.wrap(pdfBytes));
            if(pdfFile != null) {
                pageCount = pdfFile.getNumPages();
                if(pageCount > 0) {
                    setPageText.invoke(1, pageCount);
                    pdfPanel.changePage(pdfFile.getPage(0));
                }
                else {
                    throw new Exception("PDF has no pages at all. Can't view.");
                }          
            }
        }
        else {
            throw new Exception("Can't read data out of locator resource.");
        }
    }
    
    
    @Override
    public void stop() { 
    }

    
    @Override
    public void finish() {
    }

    
    @Override
    public Component getGui() {
        if(masterPanel == null) {
            masterPanel = new JPanel();
            masterPanel.setLayout(new BorderLayout(8, 8));
            try {
                initialize();

                JPanel pdfWrapper = new JPanel();
                pdfWrapper.add(pdfPanel, BorderLayout.CENTER);

                JPanel controllerPanel = new JPanel();
                controllerPanel.add(getJToolBar(), BorderLayout.CENTER);

                masterPanel.add(pdfWrapper, BorderLayout.CENTER);
                masterPanel.add(controllerPanel, BorderLayout.SOUTH);
            }
            catch(URISyntaxException use) {
                PreviewUtils.previewError(masterPanel, "Malformed locator URI syntax. Can't resolve.", use);
            }
            catch(MalformedURLException mue) {
                PreviewUtils.previewError(masterPanel, "Malformed locator URL. Can't resolve.", mue);
            }
            catch(FileNotFoundException fnfe) {
                PreviewUtils.previewError(masterPanel, "Locator resource not found.", fnfe);
            }
            catch(IOException ioe) {
                PreviewUtils.previewError(masterPanel, "Can't read the locator resource.", ioe);
            }
            catch(Exception e) {
                PreviewUtils.previewError(masterPanel, "Error occurred while initializing the PFD preview.", e);
            }
        }
        return masterPanel;
    }

    
    @Override
    public boolean isHeavy() {
        return false;
    }
    

    private JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            FIRST, UIBox.getIcon(0xf049), actionListener,
            PREV, UIBox.getIcon(0xf048), actionListener,
            NEXT, UIBox.getIcon(0xf051), actionListener,
            LAST, UIBox.getIcon(0xf050), actionListener,
            ZOOM_IN, PreviewUtils.ICON_ZOOM_IN, actionListener,
            ZOOM_OUT, PreviewUtils.ICON_ZOOM_OUT, actionListener,
            COPY_IMAGE, PreviewUtils.ICON_COPY_IMAGE, actionListener,
            COPY_LOCATION, PreviewUtils.ICON_COPY_LOCATION, actionListener,
            SAVE, PreviewUtils.ICON_SAVE, actionListener,
        }, actionListener);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "application/pdf",
                }, 
                new String[] { 
                    "pdf"
                }
        );
    }

    
    // -------------------------------------------------------------------------
    
    
    private class PDFActionListener extends MouseInputAdapter implements ActionListener, KeyListener {
        private Point lastPoint;
        private Dimension offset;


        public PDFActionListener() {
            lastPoint = null;
            offset = new Dimension(0, 0);
        }
        
        
        private boolean leftBtnDown(MouseEvent e) {
            final int modifiers = e.getModifiersEx();
            
            return (modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0;
        }
        
        
        @Override
        public void mousePressed(MouseEvent e) {
            pdfPanel.requestFocus();
            if(leftBtnDown(e) && lastPoint == null) {
                lastPoint = e.getPoint();
            }
        }

        
        @Override
        public void mouseDragged(MouseEvent e) {
        }

        
        @Override
        public void mouseReleased(MouseEvent e) {
            if(!leftBtnDown(e)) {
                lastPoint = null;
            }
        }
        
        
        private int intoRange(final int pageNumber) {
            if(pageNumber < 0) {
                return pageCount + (pageNumber % pageCount);
            }
            else {
                return pageNumber % pageCount;
            }
        }
        
        
        private void setLastPage() {
            currentPage = pageCount-1;
            pdfPanel.changePage(pdfFile.getPage(currentPage + 1));
            setPageText.invoke(currentPage + 1, pageCount);
        }
        
        
        private void setFirstPage() {
            currentPage = 0;
            pdfPanel.changePage(pdfFile.getPage(currentPage + 1));
            setPageText.invoke(currentPage + 1, pageCount);
        }
        
        
        private void changePage(final int offset) {
            currentPage = intoRange(currentPage + offset);
            pdfPanel.changePage(pdfFile.getPage(currentPage + 1));
            setPageText.invoke(currentPage + 1, pageCount);
        }
        

        @Override
        public void actionPerformed(ActionEvent args) {
            String c = args.getActionCommand();
            if(c == null) return;

            if(c.equals(OPEN_EXTERNAL) || c.equals(OPEN_EXT)) {
                PreviewUtils.forkExternalPlayer(source);
            }
            else if(c.equals(COPY_LOCATION)) {
                ClipboardBox.setClipboard(source.toString());
            }
            else if(c.equals(COPY_IMAGE)) {
                BufferedImage image = new BufferedImage(pdfPanel.getWidth(), pdfPanel.getHeight(), BufferedImage.TYPE_INT_BGR);
                pdfPanel.paint(image.getGraphics());
                ClipboardBox.setClipboard(image);
            }
            else if(c.equals(SAVE_AS) || c.equals(SAVE)) {
                PreviewUtils.saveToFile(source);
            }
            else if(c.equals(ZOOM_DEFAULT)) {
                pdfPanel.setZoom(1.0);
            }
            else if(c.equals(ZOOM_50)) {
                pdfPanel.setZoom(0.5);
            }
            else if(c.equals(ZOOM_200)) {
                pdfPanel.setZoom(2.0);
            }
            else if(c.equals(ZOOM_IN)) {
                pdfPanel.setZoom(pdfPanel.getZoom() / ZOOMFACTOR);
            }
            else if(c.equals(ZOOM_OUT)) {
                pdfPanel.setZoom(pdfPanel.getZoom() * ZOOMFACTOR);
            }
            else if(c.equals(NEXT_PAGE) || c.equals(NEXT)) {
                changePage(1);
            }
            else if(c.equals(PREV_PAGE) || c.equals(PREV)) {
                changePage(-1);
            }
            else if(c.equals(JUMP_10_FWD)) {
                changePage(10);
            }
            else if(c.equals(JUMP_10_REV)) {
                changePage(-10);
            }
            else if(c.equals(JUMP_100_FWD)) {
                changePage(100);
            }
            else if(c.equals(JUMP_100_REV)) {
                changePage(-100);
            }
            else if(c.equals(JUMP_HOME) || c.equals(FIRST)) {
                setFirstPage();
            }
            else if(c.equals(JUMP_END) || c.equals(LAST)) {
                setLastPage();
            }
        }
        
        
        // ---------------------------------------------
        
        
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if(keyCode == KeyEvent.VK_PAGE_UP) {
                if(e.isShiftDown()) changePage(-10);
                else if(e.isControlDown()) changePage(-100);
                else changePage(-1);
                e.consume();
            }
            else if(keyCode == KeyEvent.VK_PAGE_DOWN) {
                if(e.isShiftDown()) changePage(10);
                else if(e.isControlDown()) changePage(100);
                else changePage(1);
                e.consume();
            }
            else if(keyCode == KeyEvent.VK_HOME) {
                setFirstPage();
            }
            else if(keyCode == KeyEvent.VK_END) {
                setLastPage();
            }
            else if(keyCode == KeyEvent.VK_MINUS) { 
                pdfPanel.setZoom(pdfPanel.getZoom() * ZOOMFACTOR); // ZOOMING OUT
            }
            else if(keyCode == KeyEvent.VK_PLUS) { 
                pdfPanel.setZoom(pdfPanel.getZoom() / ZOOMFACTOR); // ZOOMING IN
            }
            else if(keyCode == KeyEvent.VK_C && e.isControlDown()) {
                BufferedImage image = new BufferedImage(pdfPanel.getWidth(), pdfPanel.getHeight(), BufferedImage.TYPE_INT_BGR);
                pdfPanel.paint(image.getGraphics());
                ClipboardBox.setClipboard(image);
            }
        }
        
        
        @Override
        public void keyReleased(KeyEvent e) {
            
        }
        
        
        @Override
        public void keyTyped(KeyEvent e) {
            
        }
    }
    

    private static final String 
        OPEN_EXT = "Open ext",
        OPEN_EXTERNAL = "Open in external viewer...",
        COPY_LOCATION = "Copy location",
        COPY_IMAGE = "Copy as image",
        SAVE = "Save",
        SAVE_AS = "Save media as...",
        ZOOM_OUT = "Zoom out",
        ZOOM_IN = "Zoom in",
        ZOOM_50 = "50%",
        ZOOM_DEFAULT = "100%",
        ZOOM_150 = "150%",
        ZOOM_200 = "200%",
        NEXT = "Next",
        NEXT_PAGE = "Next page",
        PREV = "Previous",
        PREV_PAGE = "Previous page",
        JUMP_10_FWD = "10 pages forward",
        JUMP_10_REV = "10 pages back",
        JUMP_100_FWD = "100 pages forward",
        JUMP_100_REV = "100 pages back",
        FIRST = "First",
        JUMP_HOME = "First page",
        LAST = "Last",
        JUMP_END = "Last page";
    
    
    private static final Object[] popupStructure = new Object[] {
        "[No page loaded]",
        "---",
        NEXT_PAGE, KeyStroke.getKeyStroke(VK_PAGE_UP, 0),
        PREV_PAGE, KeyStroke.getKeyStroke(VK_PAGE_UP, 0),
        "Jump", new Object[] {
            JUMP_HOME, KeyStroke.getKeyStroke(VK_HOME, 0),
            JUMP_END, KeyStroke.getKeyStroke(VK_END, 0),
            "---",
            JUMP_100_FWD, KeyStroke.getKeyStroke(VK_PAGE_DOWN, CTRL_MASK),
            JUMP_10_FWD, KeyStroke.getKeyStroke(VK_PAGE_DOWN, SHIFT_MASK),
            JUMP_10_REV, KeyStroke.getKeyStroke(VK_PAGE_UP, SHIFT_MASK),
            JUMP_100_REV, KeyStroke.getKeyStroke(VK_PAGE_UP, CTRL_MASK),
        },
        "---",
        // OFFSET_DEFAULT,
        "Zoom", new Object[] {
            ZOOM_IN, KeyStroke.getKeyStroke(VK_PLUS, 0),
            ZOOM_OUT, KeyStroke.getKeyStroke(VK_MINUS, 0),
            "---",
            ZOOM_50,
            ZOOM_DEFAULT,
            ZOOM_150,
            ZOOM_200,
        },
        "---",
        OPEN_EXTERNAL,
        COPY_LOCATION,
        COPY_IMAGE, KeyStroke.getKeyStroke(VK_C, CTRL_MASK),
        "---",
        SAVE_AS,
    };
}


