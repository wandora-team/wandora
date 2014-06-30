/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * PDFnew.java
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
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.event.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.utils.Abortable.Impl;
import org.wandora.utils.*;
import static org.wandora.utils.Functional.*;
import org.wandora.utils.Option;
import static org.wandora.utils.Option.*;

import static java.awt.event.KeyEvent.*;



public class PDFnew implements PreviewPanel {
    private double ZOOMFACTOR = 0.8;
    private final Frame dlgParent;
    private final Pr0 cleanup;
    private PDFFile pdfFile;
    private final PDFPanel pdfPanel = new PDFPanel();
    private int pageCount;
    private int currentPage;
    private final PDFActionListener actionListener = new PDFActionListener();
    
    final JPopupMenu menu = UIBox.makePopupMenu(popupStructure, actionListener);
    final MenuElement pageNumItem = menu.getSubElements()[0];
    final JMenuItem item = ((JMenuItem)pageNumItem.getComponent());
    
    private final Pr2<Integer, Integer> setPageText = new Pr2<Integer, Integer>() {
        public void invoke(final Integer page,final Integer count) {
            item.setText("Page " + page + " of " + count);
        }
    };

    private final URI source;
    private final Map<String, String> options;
    private final Fn1<Abortable, URI> makeCopier;
    


    
    
    public PDFnew(final URI source, final Frame dlgParent, final Map<String, String> options) throws FileNotFoundException, IOException, MalformedURLException {
        this.options = options;
        this.dlgParent = dlgParent;
        this.source = source;
        currentPage = 0;

        pdfPanel.addMouseListener(actionListener);
        pdfPanel.addMouseMotionListener(actionListener);
        pdfPanel.addKeyListener(actionListener);
        pdfPanel.setComponentPopupMenu(menu);
        
        if("file".equalsIgnoreCase(source.getScheme())) {
            final RandomAccessFile file =
                    new RandomAccessFile(createPath(source), "r");
            
            cleanup = new Pr0() {
                public void invoke() {
                    pdfFile = null;
                    try { file.close(); } catch(IOException e) {}
                }
            };
            
            try {
                final FileChannel channel = file.getChannel();
                final MappedByteBuffer buf =
                        channel.map(FileChannel.MapMode.READ_ONLY,
                                    0,
                                    channel.size());

                pdfFile = new PDFFile(buf);
                pageCount = pdfFile.getNumPages();
                if(pageCount == 0) {
                    System.err.println("No pages in pdf file!");
                }
                else {
                    setPageText.invoke(currentPage + 1, pageCount);
                }
                
                pdfPanel.changePage(some(pdfFile.getPage(0)));
                
                makeCopier = flip(curry(makeFileCopier)).invoke(source);
            }
            catch(IOException e) {
                cleanup.invoke();
                throw e;
            }
        }
        else {
            cleanup = new Pr0() { public void invoke() { } };
            
            final DownloadDialog[] dlg = new DownloadDialog[1];
            
            final Abortable.ImplFactory dlgFactory =
                new Abortable.ImplFactory() {
                    public Impl create(Abortable parent) {
                        dlg[0] = new DownloadDialog(source, parent);
                        return dlg[0];
                    }
                };
            
            final Abortable ab =
                    new Abortable(dlgParent, dlgFactory, some("Downloading pdf"));
            
            ab.run();
            
            switch(ab.getStatus()) {
                case Failure:
                    for(MalformedURLException e : dlg[0].urlException)
                        throw e;
                    for(IOException e : dlg[0].ioException)
                        throw e;
                    for(RuntimeException e : dlg[0].runtimeException)
                        throw e;
                    
                    assert false :
                        "Download failure with no exception stored.";
                    
                case InProgress:
                    assert false :
                        "Abortable.run returned with " +
                        "status InProgress.";
                    
                case Success:
                    assert dlg[0].data != null:
                        "Download returned with success but " +
                        "with no buffer stored.";
                    
                    pdfFile = new PDFFile(ByteBuffer.wrap(dlg[0].data));

                    pageCount = pdfFile.getNumPages();
                    if(pageCount == 0) {
                        System.err.println("No pages in pdf file!");
                    }
                    else {
                        setPageText.invoke(currentPage + 1, pageCount);
                    }

                    pdfPanel.changePage(some(pdfFile.getPage(0)));
                    break;
            }
            
            makeCopier = flip(curry(makeMemoryCopier))
                         .invoke(dlg[0].data);
        }
    }

    @Override
    public void stop() { }

    @Override
    public void finish() {
        cleanup.invoke();
    }

    @Override
    public Component getGui() {
        return pdfPanel;
    }

    @Override
    public boolean isHeavy() {
        return false;
    }
    

    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    private File createPath(final URI uri) throws IOException {
        try {
            return new File(uri);
        }
        catch(IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
    
    
    /**
     * Creates a file copier factory object
     * that can be passed to Abortable's constructor
     */ 
    private Fn2<Abortable, URI, URI> makeFileCopier = new Fn2<Abortable, URI, URI>() {
        public Abortable invoke(final URI destination, final URI source) {
            final String in = source.toString();
            final String out = destination.toString();
            
            return new Abortable(
                dlgParent,
                ManualFileCopy.factory(out, in),
                some("Copying file")
            );
        }
    };
    
    
    
    private Fn2<Abortable, URI, byte[]> makeMemoryCopier = new Fn2<Abortable, URI, byte[]>() {
        public Abortable invoke(final URI dest, final byte[] src) {
            return new Abortable(
                dlgParent,
                new Abortable.ImplFactory() {
                    public Impl create(Abortable parent) {
                        return new MemoryFileCopier(dest, src, parent);
                    }
                },
                some("Saving pdf file"));
        }
    };
    
    
    
    private static Dimension difference(final Point lhs, final Point rhs) {
        return new Dimension(lhs.x - rhs.x, lhs.y - rhs.y);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    private class PDFActionListener extends MouseInputAdapter implements ActionListener, KeyListener {
        private Option<Point> lastPoint;
        private Dimension offset;


        public PDFActionListener() {
            lastPoint = none();
            offset = new Dimension(0, 0);
        }
        
        private boolean leftBtnDown(MouseEvent e) {
            final int modifiers = e.getModifiersEx();
            
            return (modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            pdfPanel.requestFocus();
            if(leftBtnDown(e) && lastPoint.empty()) {
                lastPoint = some(e.getPoint());
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            /*
            // Feature enables PDF preview drag!
            for(final Point last : this.lastPoint) {
                final Point next = e.getPoint();
                final Dimension distance = difference(next, last);
                lastPoint = some(next);

                setOffset(new Dimension(offset.width + distance.width,
                                       offset.height + distance.height));
            }
            */
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(!leftBtnDown(e)) {
                lastPoint = none();
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
            pdfPanel.changePage(some(pdfFile.getPage(currentPage + 1)));
            setPageText.invoke(currentPage + 1, pageCount);
        }
        
        private void setFirstPage() {
            currentPage = 0;
            pdfPanel.changePage(some(pdfFile.getPage(currentPage + 1)));
            setPageText.invoke(currentPage + 1, pageCount);
        }
        
        private void changePage(final int offset) {
            currentPage = intoRange(currentPage + offset);
            pdfPanel.changePage(some(pdfFile.getPage(currentPage + 1)));
            setPageText.invoke(currentPage + 1, pageCount);
        }
        
        private void setOffset(final Dimension offset) {
            this.offset = offset;
            pdfPanel.setViewOffset(offset);
        }

        public void actionPerformed(ActionEvent args) {
            for(final String c : some(args.getActionCommand())) {
                if(c.equals(OPEN_EXTERNAL)) {
                    for(final String cmdRaw : Util.getOption(options, "mediaviewer")) {
                        final String cmd = cmdRaw.replaceAll("__URL__", source.toString());
                        try {
                            Runtime.getRuntime().exec(cmd);
                        }
                        catch(IOException e) {

                        }
                    }
                }
                else if(c.equals(COPY_LOCATION)) {
                    ClipboardBox.setClipboard(source.toString());
                }
                else if(c.equals(COPY_IMAGE)) {
                    BufferedImage image = new BufferedImage(pdfPanel.getWidth(), pdfPanel.getHeight(), BufferedImage.TYPE_INT_BGR);
                    pdfPanel.paint(image.getGraphics());
                    ClipboardBox.setClipboard(image);
                }
                else if(c.equals(SAVE_AS)) {
                    final Option<String> path =
                            Util.choosePath(options,
                                            pdfPanel,
                                            "pdfPreviewPanel");
                    
                    
                    final Option<URI> uri = path.flatMap(Util.makeFileURI);
                    uri
                        .map(makeCopier)
                        .apply(runner());
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
                else if(c.equals(OFFSET_DEFAULT)) {
                    setOffset(new Dimension(0, 0));
                }
                else if(c.equals(NEXT_PAGE)) {
                    changePage(1);
                }
                else if(c.equals(PREV_PAGE)) {
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
                else if(c.equals(JUMP_HOME)) {
                    setFirstPage();
                }
                else if(c.equals(JUMP_END)) {
                    setLastPage();
                }
            }
        }
        
        
        // ---------------------------------------------
        
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
        public void keyReleased(KeyEvent e) {
            
        }
        public void keyTyped(KeyEvent e) {
            
        }
    }
    
    
    
    
    
    private static final
            String OPEN_EXTERNAL = "Open in external viewer...",
                   COPY_LOCATION = "Copy media location",
                   COPY_IMAGE = "Copy as image",
                   SAVE_AS = "Save media as...",
                   ZOOM_OUT = "Zoom out",
                   ZOOM_IN = "Zoom in",
                   ZOOM_50 = "50%",
                   ZOOM_DEFAULT = "100%",
                   ZOOM_150 = "150%",
                   ZOOM_200 = "200%",
                   NEXT_PAGE = "Next page",
                   PREV_PAGE = "Previous page",
                   JUMP_10_FWD = "10 pages forward",
                   JUMP_10_REV = "10 pages back",
                   JUMP_100_FWD = "100 pages forward",
                   JUMP_100_REV = "100 pages back",
                   JUMP_HOME = "First page",
                   JUMP_END = "Last page",
                   OFFSET_DEFAULT = "Reset panning";
    
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


// -----------------------------------------------------------------------------



class MemoryFileCopier implements Abortable.Impl {
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final byte[] src;
    private final URI dest;
    private final Abortable parent;

    
    public MemoryFileCopier(URI dest, byte[] src, Abortable parent) {
        this.src = src;
        this.dest = dest;
        this.parent = parent;
    }

    public void forceAbort() {
        stop.set(true);
    }

    private void drain(final OutputStream dest, final InputStream src) throws IOException {
        final byte[] buf = new byte[0x2000];
        int readc = 0;
        long written = 0;
        long time = System.currentTimeMillis();
        while((readc = src.read(buf)) != -1 && !stop.get()) {
            dest.write(buf, 0, readc);
            written += readc;
            long newTime = System.currentTimeMillis();
            if(newTime - time > 300) {
                time = newTime;
                parent.progress((double)written / (double)this.src.length,
                                Abortable.Status.InProgress,
                                "Saving file...");
            }
        }
    }

    public void run() {
        try {
            parent.progress(0.0,
                            Abortable.Status.InProgress,
                            "Saving file...");
            
            drain(new FileOutputStream(new File(dest)),
                    Util.makeInputStream(ByteBuffer.wrap(src)));
            
            if(!stop.get())
                parent.progress(1.0,
                                Abortable.Status.Success,
                                "File saved!");
            else
                parent.progress(0.0,
                                Abortable.Status.Failure,
                                "User aborted.");
        }
        catch(FileNotFoundException e) {
            parent.progress(0.0,
                            Abortable.Status.Failure,
                            "File not found:\n" + dest.toString());
        }
        catch(IOException e) {
            parent.progress(0.0,
                            Abortable.Status.Failure,
                            "IOException caught while saving file:\n" +
                                e.getMessage());
        }
    }
}


class DownloadDialog implements Abortable.Impl {
    private final URI source;
    private final Abortable parent;
    private final AtomicBoolean stop;
    public byte[] data;

    public Option<MalformedURLException> urlException;
    public Option<IOException> ioException;
    public Option<RuntimeException> runtimeException;

    public DownloadDialog(final URI source, final Abortable parent) {
        this.source = source;
        this.parent = parent;
        stop = new AtomicBoolean(false);
        urlException = none();
        ioException = none();
        runtimeException = none();
    }

    private void drain(final OutputStream dest, final InputStream src) throws IOException {
        final byte[] buf = new byte[0x2000];
        long curTime = System.currentTimeMillis();

        long downloaded = 0;
        int readc = -1;
        while((readc = src.read(buf)) != -1 && !stop.get()) {
            dest.write(buf, 0, readc);
            downloaded += readc;

            long nextTime = System.currentTimeMillis();
            if(nextTime - curTime > 300) {
                curTime = nextTime;
                parent.progress(0.0, Abortable.Status.InProgress,
                        "Download in progress, downloaded " +
                        (downloaded / 1024) + "KiB so far.");
            }
        }
    }

    public void forceAbort() {
        stop.set(true);
    }

    private void runImpl() throws MalformedURLException, IOException {
        final ByteArrayOutputStream dest = new ByteArrayOutputStream(0x40000);
        final InputStream src = source.toURL().openStream();

        try {
            parent.progress(0.0, Abortable.Status.InProgress,
                    "Download in progress.");

            drain(dest, src);
            this.data = dest.toByteArray();
        }
        finally {
            try { src.close(); } catch(IOException e) {}
        }
    }

    public void run() {
        try {
            runImpl();
            if(!stop.get())
                parent.progress(1.0, Abortable.Status.Success,
                        "Download succeeded!");
            else
                parent.progress(0.0,
                                Abortable.Status.Failure,
                                "User aborted");
        }
        catch(MalformedURLException e) {
            urlException = some(e);
            parent.progress(0.0, Abortable.Status.Failure,
                    "Malformed URL in subject locator:\n" +
                    e.getMessage());
        }
        catch(IOException e) {
            ioException = some(e);
            parent.progress(0.0, Abortable.Status.Failure,
                    "IOException occurred while downloading:\n" +
                    e.getMessage());
        }
        catch(RuntimeException e) {
            runtimeException = some(e);
            parent.progress(0.0, Abortable.Status.Failure,
                    "RuntimeException occurred while downloading:\n" +
                    e.getMessage());
        }
    }
}
