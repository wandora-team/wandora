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
 */



package org.wandora.utils;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * Uses a system-specific copy command provided in wandora's config with the name "copycommand"
 */
public class NativeFileCopy implements Abortable.Impl {
    private Process process;
    private Abortable parent;
    private String destination;
    private String source;
    private String[] copyCommand;

    public NativeFileCopy(Abortable parent,
                          String destination,
                          String source,
                          String[] copyCommand)
    {
        this.parent = parent;
        this.destination = destination;
        this.source = source;
        this.copyCommand = copyCommand;
    }

    public void forceAbort() {
        if(process != null) {
            synchronized(process) {
                process.notify();
            }
        }
    }

    public static Abortable.ImplFactory factory(final String[] copyCommand,
                                                final String destination,
                                                final String source)
    {
       return new Abortable.ImplFactory() {
            public Abortable.Impl create(final Abortable parent) {
                return new NativeFileCopy(parent,
                                          destination,
                                          source,
                                          copyCommand);
            }
        };
    }

    public static <T> T[] concat(T[] dest, T[] src0, T... src1) {
        int i = 0;
        for(; i < src0.length; ++i) {
            dest[i] = src0[i];
        }
        for(int j = 0; j < src1.length; ++j, ++i) {
            dest[i] = src1[j];
        }
        return dest;
    }

    public void run() {
        try {
            final String
                    inPath = new File(new URI(source)).getCanonicalPath(),
                    outPath = new File(new URI(destination)).getCanonicalPath();
            
            parent.progress(0.0,
                            Abortable.Status.InProgress,
                            "Copy operation in progress " +
                                "(copying to \"" + destination + "\")");
            
            String[] args = concat(new String[copyCommand.length + 2],
                                   copyCommand,
                                   inPath,
                                   outPath);
            
            /*{
                String intr = "[";
                for(String str : args) {
                    System.err.print(intr + str);
                    intr = ", ";
                }
                System.err.print("]\n");
            }*/
            
            
            process = Runtime.getRuntime().exec(args);
            final int result = process.waitFor();
            InputStream stream = process.getErrorStream();
            
            if (result != 0) {
                byte[] data = new byte[stream.available()];
                stream.read(data);
                parent.progress(0.0, 
                                Abortable.Status.Failure, 
                                "Copy operation failed with " +
                                    "return code " + result + 
                                    "\nProcess stderr:\n" + new String(data));
            } else {
                parent.progress(1.0,
                                Abortable.Status.Success,
                                "Copy operation succeeded!");
            }
        }
        catch(InterruptedException e) {
            process.destroy();
            parent.progress(0.0,
                            Abortable.Status.Failure,
                            "The thread was interrupted before " +
                                "the copy operation completed; " +
                                "child process terminated.");
        }
        catch(SecurityException e) {
            parent.progress(0.0,
                            Abortable.Status.Failure,
                            "No permission to start subprocess; " +
                                "grant permission or remove \"copycommand\" " +
                                "from wandora configuration.");
        }
        catch(IOException e) {
            parent.progress(0.0,
                            Abortable.Status.Failure,
                            "IO exception: " +
                                e.getMessage() +
                                "\nMake sure the \"copycommand\" tag " +
                                "in conf/options.xml is set " +
                                "to the correct system-specific copy " +
                                "command (such as /bin/cp or cmd /c copy) " +
                                "or alternatively, remove it and a slower " +
                                "file copy through java file streams " +
                                "will be used.");
        }
        catch(URISyntaxException e) {
            parent.progress(0.0,
                            Abortable.Status.Failure,
                            "Invalid SI: " + e.getMessage());
        }
    }
}
