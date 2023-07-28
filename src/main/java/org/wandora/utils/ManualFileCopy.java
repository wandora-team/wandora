package org.wandora.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ManualFileCopy implements Abortable.Impl {

        private volatile boolean abortRequested = false;
        private String inPath, outPath;
        private Abortable parent;

        public ManualFileCopy(Abortable parent, String outPath, String inPath) {
            this.inPath = inPath;
            this.outPath = outPath;
            this.parent = parent;
        }
       
        public void forceAbort() {
            abortRequested = true;
        }
        
        public static Abortable.ImplFactory factory(final String outPath, final String inPath) {
            return new Abortable.ImplFactory() {
                public Abortable.Impl create(Abortable parent) {
                    return new ManualFileCopy(parent, outPath, inPath);
                }
            };
        }

        public void run() {
            final boolean overWrite = false;
            try {
                File source = new File(new URI(inPath));
                File target = new File(new URI(outPath));

                if (!source.exists()) {
                }

                if (target.exists() && overWrite) {
                    if (!target.delete()) {
                        // can't overwrite, abort
                        return;
                    }
                }

                final long srcLength = source.length();

                FileInputStream in = new FileInputStream(source);
                FileOutputStream out = new FileOutputStream(target);

                byte[] data = new byte[8000];
                int l = 0;
                long transferred = 0;
                int counter = 0;
                while (!abortRequested && (l = in.read(data)) > -1) {
                    out.write(data, 0, l);
                    transferred += 8000;
                    ++counter;

                    if(counter == 10) {
                        counter = 0;
                        parent.progress((double)transferred / (double)srcLength, Abortable.Status.InProgress, "Copy in progress.");
                    }
                }
                in.close();
                out.flush();
                out.close();

                parent.progress(1.0, Abortable.Status.Success, "File copy operation succeeded.");

            } catch (FileNotFoundException ex) {
                parent.progress(0.0, Abortable.Status.Failure, "File not found: " + ex.getMessage());
            } catch (IOException ex) {
                parent.progress(0.0, Abortable.Status.Failure, "IO exception: " + ex.getMessage());
            } catch (URISyntaxException e) {
                parent.progress(0.0, Abortable.Status.Failure, "Syntax error in SL URI: " + e.getMessage());
            }
        }
    }
