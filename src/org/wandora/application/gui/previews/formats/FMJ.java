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

import org.wandora.application.gui.previews.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.media.CannotRealizeException;
import javax.media.DataSink;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.DataSource;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.wandora.application.gui.UIBox;
import org.wandora.utils.Abortable.Impl;
import org.wandora.utils.Abortable.ImplFactory;
import org.wandora.utils.ClipboardBox;
import static org.wandora.utils.Functional.*;
import org.wandora.utils.Abortable;
import static org.wandora.utils.Option.*;
import org.wandora.utils.ManualFileCopy;

/**
 *
 * @author anttirt
 */
public class FMJ extends JPanel implements PreviewPanel, ActionListener {
    
    private static final String OPTIONS_PREFIX = "gui.fmjPreviewPanel.";

    private final Player player;
    private final boolean heavyWeight;
    private final String source;
    private final Frame dlgParent;
    private final Map<String, String> options;
    
    private final Fn2<Abortable, String, String> makeCopier_ = new Fn2<Abortable, String, String>() {
    public Abortable invoke(final String destination, final String source) {
        if (source.startsWith("file")) {
            /*
            for(final String cmd : Util.getOption(options, "copycommand"))
                return new Abortable(dlgParent, NativeFileCopy.factory(cmd.split("\\s+"), destination, source), some("Copying file"));
            */
            return new Abortable(dlgParent, ManualFileCopy.factory(destination, source), some("Copying file"));
        }
        else {
            return new Abortable(dlgParent, StreamCopy.factory(destination, source), some("Copying file"));
        }
    }};
    
    // the above but with source provided
    private final Fn1<Abortable, String> makeCopier;
    
    
    
    
    
    
    public FMJ(final String source, final Frame dlgParent, final Map<String, String> options) throws CannotRealizeException, NoPlayerException, IOException {
        if(source == null || source.length() == 0)
            throw new NoPlayerException("Null or empty subject locator passed to FMJ preview panel");
        
        setLayout(new BorderLayout());
        
        Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
        
        this.source = source;
        this.dlgParent = dlgParent;
        this.makeCopier = flip(curry(makeCopier_)).invoke(source); // add salt and serve with chardonnay
        this.options = options;
        
        player = Manager.createRealizedPlayer(new MediaLocator(source));
        final Component video = player.getVisualComponent();
        final Component controls = player.getControlPanelComponent();

        final boolean lightVideo = video == null || video instanceof JComponent;
        final boolean lightCtrls = controls == null || controls instanceof JComponent;
        
        heavyWeight = !(lightVideo && lightCtrls);
        
        // fmj might return a video component even if there isn't any video to show
        if(video != null && !Util.endsWithAny(source, ".mp3"))
            add(video, BorderLayout.NORTH);
        if(controls != null)
            add(controls, BorderLayout.SOUTH);
        
        initPopup(controls, video);
        
        validate();
    }
    
    
    
    private final String OPEN_EXTERNAL = "Open in external viewer...",
                         COPY_LOCATION = "Copy media location",
                         SAVE_AS = "Save media as...";
    
    private final Object[] popupStructure = new Object[] {
            OPEN_EXTERNAL,
            "---",
            COPY_LOCATION,
            "---",
            SAVE_AS,
        };
    
    @Override
    public void actionPerformed(ActionEvent args) {
        for(String c : some(args.getActionCommand())) {
            if(c.equals(OPEN_EXTERNAL)) {
                for(final String cmdRaw : Util.getOption(options, "mediaviewer")) {
                    final String cmd = cmdRaw.replaceAll("__URL__", source);
                    try {
                        Runtime.getRuntime().exec(cmd);
                    }
                    catch(IOException e) {
                        
                    }
                }
            }
            else if(c.equals(COPY_LOCATION)) {
                if(source != null) {
                    ClipboardBox.setClipboard(source);
                }
            }
            else if(c.equals(SAVE_AS)) {
                Util.choosePath(options, this, OPTIONS_PREFIX).map(makeCopier).apply(runner());
            }
        }
    }
    
    @Override
    public boolean isHeavy() {
        return heavyWeight;
    }

    
    // PreviewPanel control implementation
    public void play() {
        player.start();
    }
    
    
    @Override
    public void finish() {
        player.stop();
        player.close();
        player.deallocate();
    }
    
    
    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public Component getGui() {
        return this;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private static class StreamCopy implements Abortable.Impl, DataSinkListener {
        private final Abortable parent;
        private String source;
        private String destination;
        private DataSource src;
        private DataSink dest;
        
        public StreamCopy(Abortable parent, String destination, String source) {
            this.parent = parent;
            this.source = source;
            this.destination = destination;
        }

        public static Abortable.ImplFactory factory(final String destination, final String source) {
            return new ImplFactory() {
                @Override
                public Impl create(Abortable parent) {
                    return new StreamCopy(parent, destination, source);
                }
            };
        }
        
        @Override
        public void forceAbort() {
            parent.progress(0.0, Abortable.Status.Failure, "User aborted transmission.");
            try {
                dest.stop();
            }
            catch(IOException e) {
                parent.progress(0.0, Abortable.Status.Failure, "User aborted transmission. Caught IOException: " + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            parent.progress(0.0, Abortable.Status.InProgress, "Stream copy operation in progress");
            try {
                src = Manager.createDataSource(new MediaLocator(source));
                dest = Manager.createDataSink(src, new MediaLocator(new File(new URI(destination)).toURI().toString()));
                

                dest.open();
                dest.start();
                
                dest.addDataSinkListener(this);
            }
            catch (URISyntaxException e) {
                parent.progress(0.0, Abortable.Status.Failure, "URI syntax error: " + e.getMessage());
            }
            catch (NoDataSinkException e) {
                parent.progress(0.0, Abortable.Status.Failure, "No suitable data sink found: " + e.getMessage());
            }
            catch (NoDataSourceException e) {
                parent.progress(0.0, Abortable.Status.Failure, "No suitable data source found: " + e.getMessage());
            }
            catch (IOException e) {
                parent.progress(0.0, Abortable.Status.Failure, e.getMessage());
            }
        }

        @Override
        public void dataSinkUpdate(DataSinkEvent arg0) {
            if(arg0 instanceof EndOfStreamEvent) {
                parent.progress(0.0, Abortable.Status.Success, "Transfer succeeded!");
            }
            else if(arg0 instanceof DataSinkErrorEvent) {
                parent.progress(0.0, Abortable.Status.Failure, "Transfer failed: " + ((DataSinkErrorEvent)arg0).toString());
            }
            dest.removeDataSinkListener(this);
            try { dest.stop(); } catch(IOException e) {}
            dest.close();
        }
    }

    private void initPopup(final Component controls, final Component video) {
        if (isHeavy()) {
            final PopupMenu popup = UIBox.makeAWTPopupMenu(popupStructure, this);
            final MouseListener ml = new MouseListener() {
                private void show(MouseEvent e) {
                    if (!e.isPopupTrigger()) return;
                    Point mousePos = e.getPoint();
                    popup.show(e.getComponent(), mousePos.x, mousePos.y);
                }
                public void mousePressed(MouseEvent e) { show(e); }
                public void mouseReleased(MouseEvent e) { show(e); }
                public void mouseClicked(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            };
            
            if (video != null)
                video.addMouseListener(ml);
           
            if (controls != null)
                controls.addMouseListener(ml);
           

            this.add(popup);
            this.addMouseListener(ml);
            
        } 
        else {
            final JPopupMenu popup = UIBox.makePopupMenu(popupStructure, this);
            this.setComponentPopupMenu(popup);
            
            if(video != null)
                ((JComponent) video).setInheritsPopupMenu(true);
            
            if(controls != null)
                ((JComponent) controls).setInheritsPopupMenu(true);
        }
    }
}
