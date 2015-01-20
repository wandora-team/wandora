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

import org.wandora.application.gui.previews.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;
import org.gstreamer.Bus;
import org.gstreamer.URIType;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.GstException;
import org.gstreamer.State;
import org.gstreamer.Element;
import org.gstreamer.elements.PlayBin;
import org.gstreamer.Pipeline;
import org.gstreamer.lowlevel.GstUriHandlerAPI;
import org.gstreamer.swing.VideoComponent;
import org.wandora.utils.Option;
import static org.wandora.utils.Option.*;
import static org.wandora.utils.Functional.*;
import org.wandora.utils.Abortable;
import org.wandora.utils.ManualFileCopy;
import org.wandora.utils.NativeFileCopy;
import org.wandora.utils.ClipboardBox;
import org.wandora.application.gui.UIBox;


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
    private final URI source;
    
    
    
    public GST(final URI subjectLocator, final Frame dlgParent, final Map<String, String> options) throws GstException {
        Gst.init("Wandora", new String[]{});
        
        this.options = options;
        this.dlgParent = dlgParent;
        this.source = subjectLocator;
        
        setLayout(new BorderLayout());
        setComponentPopupMenu(UIBox.makePopupMenu(menuStructure, this));

        this.playbin = new PlayBin("Wandora", subjectLocator);
        this.vidc = new VideoComponent();
        this.controls = new GSTControls(
                        Option.some(playAction), 
                        Option.some(pauseAction), 
                        Option.some(stopAction));

        playbin.setVideoSink(vidc.getElement());
        
        if(subjectLocator.toString().startsWith("file"))
                makeCopier = flip(curry(makeFileCopier)).invoke(subjectLocator);
        else
                makeCopier = flip(curry(makeStreamCopier)).invoke(subjectLocator);

        add(vidc, BorderLayout.NORTH);
        add(controls, BorderLayout.SOUTH);

    pauseAction.run();

    //revalidate();
    }
    
    public GST(final File inputFile, final Frame dlgParent, final Map<String, String> options)
           throws GstException
    {
        this(inputFile.toURI(), dlgParent, options);
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
                for(final String cmdRaw : Util.getOption(options, "mediaviewer")) {
                    final String cmd = cmdRaw.replaceAll("__URL__", locatorString);
                    try {
                        Runtime.getRuntime().exec(cmd);
                    }
                    catch(IOException e) {
                        
                    }
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
    public Abortable invoke(final URI destination,
                            final URI source)
    {
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
    public Abortable invoke(final URI destination,
                            final URI source)
    {
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
}
