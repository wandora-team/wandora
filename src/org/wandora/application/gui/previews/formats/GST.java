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
 * GST.java
 *
 * Created on 12. lokakuuta 2007, 17:14
 *
 */


package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.Gst;
import org.gstreamer.GstException;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.URIType;
import org.gstreamer.elements.PlayBin;
import org.gstreamer.lowlevel.GstUriHandlerAPI;
import org.gstreamer.swing.VideoComponent;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.*;
import static org.wandora.application.gui.previews.Util.endsWithAny;
import org.wandora.utils.Abortable;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import static org.wandora.utils.Functional.*;
import org.wandora.utils.Functional.Fn1;
import org.wandora.utils.Functional.Fn2;
import org.wandora.utils.ManualFileCopy;
import org.wandora.utils.NativeFileCopy;
import org.wandora.utils.Option;
import static org.wandora.utils.Option.*;


/**
 *
 * @author anttirt
 */
public class GST extends JPanel implements PreviewPanel, ActionListener {
    
    private final Map<String, String> options;
    private final Frame dlgParent;
    private final PlayBin playbin;
    private final VideoComponent vidc;
    private final Component controls;
    private final Fn1<Abortable, URI> makeCopier;
    private final String source;
    
    
    
    public GST(final String subjectLocator) throws GstException, URISyntaxException {
        Gst.init("Wandora", new String[]{});
        
        this.options = Wandora.getWandora().getOptions().asMap();
        this.dlgParent = Wandora.getWandora();
        this.source = subjectLocator;
        
        setLayout(new BorderLayout());
        setComponentPopupMenu(UIBox.makePopupMenu(menuStructure, this));

        this.playbin = new PlayBin("Wandora", new URI(source));
        this.vidc = new VideoComponent();
        this.controls = new GSTControls(
                        Option.some(playAction), 
                        Option.some(pauseAction), 
                        Option.some(stopAction));

        playbin.setVideoSink(vidc.getElement());
        
        if(subjectLocator.toString().startsWith("file"))
                makeCopier = flip(curry(makeFileCopier)).invoke(new URI(source));
        else
                makeCopier = flip(curry(makeStreamCopier)).invoke(new URI(source));

        add(vidc, BorderLayout.NORTH);
        add(controls, BorderLayout.SOUTH);

        pauseAction.run();

        //revalidate();
    }
    
    
    public GST(final File inputFile) throws GstException, URISyntaxException {
        this(inputFile.toURI().toString());
    }
    

    // to avoid threading issues, the gstreamer objects will only be modified from the Swing EDT
    public void play() {
        SwingUtilities.invokeLater(playAction);
    }
    
    @Override
    public void stop() {
        SwingUtilities.invokeLater(stopAction);
    }
    
    @Override
    public void finish() {
        SwingUtilities.invokeLater(cleanupAction);
    }

    @Override
    public Component getGui() {
        return this;
    }

    @Override
    public boolean isHeavy() {
        return false;
    }
    
    private final String OPEN_EXTERNAL = "Open in external viewer...",
                         COPY_LOCATION = "Copy media location",
                         SAVE_AS = "Save media as...";
    
    private final Object[] menuStructure = new Object[] {
            OPEN_EXTERNAL,
            "---",
            COPY_LOCATION,
            "---",
            SAVE_AS,
        };
    
    /**
     * Listens to popup menu events
     * @param args
     */
    @Override
    public void actionPerformed(ActionEvent args) {
        final String locatorString = source.toString();
        for(String c : Option.some(args.getActionCommand())) {
            if(c.equals(OPEN_EXTERNAL)) {
                if(!DataURL.isDataURL(source )) {
                    System.out.println("Spawning viewer for \""+source+"\"");
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(new URI(source));
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
            else if(c.equals(COPY_LOCATION)) {
                ClipboardBox.setClipboard(locatorString);
            }
            else if(c.equals(SAVE_AS)) {
                Util.choosePath(options, this, "gstPreviewPanel")
                        .flatMap(Util.makeFileURI)
                        .map(makeCopier)
                        .apply(runner());
            }
        }
    }
    
    /**
     * Changes the preferredSize of the preview panel according
     * to the size of the video frame
     */
    private PropertyChangeListener
        prefSizeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final Dimension vcDims = vidc.getPreferredSize();
            final Dimension ctrlDims = controls.getPreferredSize();
            Dimension newDim = new Dimension(vcDims.width, vcDims.height + ctrlDims.height);
            setPreferredSize(newDim);
            setMinimumSize(newDim);
            setMaximumSize(newDim);
            revalidate();
        }};

    /**
     * Actions that should be run using
     * the swing EDT thread through
     * SwingUtilities.InvokeLater.
     */
    private Runnable
            
    /**
     * Starts playing the media
     */ 
    playAction = new Runnable() {
        @Override
        public void run() {
            playbin.setState(State.PLAYING);
            vidc.addPropertyChangeListener(
                    "preferredSize",
                    prefSizeListener);
        }
    },
    /**
     * pauses playing the media
     */
    pauseAction = new Runnable() {
        @Override
        public void run() {
            playbin.setState(State.PAUSED);
        }
    },
    /**
     * stops playing the media
     * and resets the data source's state
     */
    stopAction = new Runnable() {
        @Override
        public void run() {
            playbin.setState(State.READY);
        }
    },
    /**
     * stops playing the media and cleans up
     * the player.
     */
    cleanupAction = new Runnable() {
        @Override
        public void run() {
            playbin.setState(State.NULL);
            remove(vidc);
        }
    };

    /**
     * Creates a file copier factory object
     * that can be passed to Abortable's constructor
     */ 
    private Fn2<Abortable, URI, URI>
    makeFileCopier = new Fn2<Abortable, URI, URI>() {
        
    public Abortable invoke(final URI destination, final URI source) {
        final String in = source.toString();
        final String out = destination.toString();

        for(String c : Util.getOption(options, "copycommand"))
            return new Abortable(dlgParent, NativeFileCopy.factory(c.split("\\s+"), out, in), some("Copying file"));

        return new Abortable(dlgParent, ManualFileCopy.factory(out, in), some("Copying file"));
    }};

    
    
    /**
     * Same as above but copies from a gstreamer stream
     * instead of a file
     */ 
    private Fn2<Abortable, URI, URI>
    makeStreamCopier = new Fn2<Abortable, URI, URI>() {
    public Abortable invoke(final URI destination, final URI source) {
        return new Abortable(dlgParent, StreamCopy.factory(destination, source), some("Copying file"));
    }};


    
    
    // -------------------------------------------------------------------------
    
    
    
    /**
     * Implements the Abortable.Impl interface for a gstreamer stream copy
     * operation to copy media from non-local sources.
     */
    private static class StreamCopy
                   implements Abortable.Impl,
                              Bus.INFO,
                              Bus.STATE_CHANGED,
                              Bus.EOS,
                              Bus.ERROR
    {
        private final Abortable parent;
        private final URI inputLocator;
        private final URI outPath;
        
        private final Option<Pipeline> dataPipe;
        
        public StreamCopy(
                Abortable parent,
                URI outPath,
                URI inputLocator)
        {
            this.parent = parent;
            this.inputLocator = inputLocator;
            this.outPath = outPath;

            GstUriHandlerAPI uriApi = GstUriHandlerAPI.INSTANCE;
            Element src = uriApi.gst_element_make_from_uri(
                    URIType.GST_URI_SRC,
                    inputLocator.toString(),
                    "Source");
            Element dest = uriApi.gst_element_make_from_uri(
                    URIType.GST_URI_SINK,
                    outPath.toString(),
                    "Destination");
            
            if(src == null) {
                parent.progress(0.0, Abortable.Status.Failure, "Unable to create source element for URI: " + inputLocator.toString());
                dataPipe = Option.none();
            }
            else if(dest == null) {
                parent.progress(0.0, Abortable.Status.Failure, "Unable to create destination element for URI: " + outPath.toString());
                dataPipe = Option.none();
            }
            else {
                Pipeline pipe = new Pipeline("file-saver");
                dataPipe = Option.some(pipe);

                pipe.addMany(src, dest);
                Pipeline.linkMany(src, dest);
            }
        }

        public static Abortable.ImplFactory factory(
                final URI outPath,
                final URI inputLocator)
        {
            return new Abortable.ImplFactory() {
                public Abortable.Impl create(Abortable parent) {
                    return new StreamCopy(
                            parent,
                            outPath,
                            inputLocator);
                }
            };
        }
        
        @Override
        public void forceAbort() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for(Pipeline pipe : dataPipe)
                        pipe.setState(State.NULL);
                }
            });
        }
        
        @Override
        public void run() {
            for(Pipeline pipe : dataPipe) {
                pipe.setState(State.PLAYING);
                pipe.getBus().connect((Bus.INFO)this);
                pipe.getBus().connect((Bus.STATE_CHANGED)this);
                pipe.getBus().connect((Bus.EOS)this);
                pipe.getBus().connect((Bus.ERROR)this);
            }
        }


        // Bus.INFO
        @Override
        public void infoMessage(
                GstObject source,
                int code,
                String message)
        {
            parent.progress(0.0, Abortable.Status.InProgress,
                    message);
        }

        // Bus.EOS
        @Override
        public void endOfStream(GstObject source) {
            parent.progress(1.0, Abortable.Status.Success,
                    "Transfer complete.");
            for(Pipeline pipe : dataPipe)
                pipe.setState(State.NULL);
        }

        // Bus.ERROR
        @Override
        public void errorMessage(
                GstObject source,
                int code,
                String message)
        {
            parent.progress(0.0, Abortable.Status.Failure,
                    "Transfer error: " + message);
            for(Pipeline pipe : dataPipe)
                pipe.setState(State.NULL);
        }

        // Bus.STATE_CHANGED
        @Override
        public void stateChanged(
                GstObject source,
                State old,
                State current,
                State pending)
        {
            switch(current)
            {
                case PLAYING:
                    parent.progress(0.0, Abortable.Status.InProgress,
                            "Transfer in progress.");
                    break;

                default:
                    break;
            }
        }
    }
    
    
    

    public static boolean canView(String url) {
        boolean answer = false;
        if(url != null) {
            if(DataURL.isDataURL(url)) {
                try {
                    DataURL dataURL = new DataURL(url);
                    String mimeType = dataURL.getMimetype();
                    if(mimeType != null) {
                        String lowercaseMimeType = mimeType.toLowerCase();
                        if(lowercaseMimeType.startsWith("video/mpeg")) {
                                answer = true;
                        }
                    }
                }
                catch(Exception e) {
                    // Ignore --> Can't view
                }
            }
            else {
                if(endsWithAny(url.toLowerCase(), ".mpe", ".mpeg", "mpg")) {
                    answer = true;
                }
            }
        }
        
        if(answer == true) {
            String mediafw = System.getProperty("org.wandora.mediafw");
            if(!"GST".equals(mediafw)) {
                answer = false;
            }
        }
        
        return answer;
    }
}
