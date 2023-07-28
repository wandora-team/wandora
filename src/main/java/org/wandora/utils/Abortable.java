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
 */
package org.wandora.utils;

import org.wandora.application.gui.previews.*;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import org.wandora.utils.Option;
import org.wandora.application.gui.*;

/**
 *
 * @author anttirt
 */
public class Abortable implements Runnable {
    public interface Impl extends Runnable {
        /**
         * shall not block and must be safe to call from another thread than run
         */
        void forceAbort();
        
        /**
         * can block; will be run in a separate thread
         */
        void run();
    }
    
    public enum Status {
        InProgress,
        Success,
        Failure
    }
    
    public interface ImplFactory {
        Impl create(Abortable parent);
    }
    
    private Impl impl;
    private Thread runThread;
    private Frame dialogParent;
    private AbortableProgressDialog dlg;
    private String name;
    private Status status;

    public Status getStatus() {
        return status;
    }
    
    
    
    private static Runnable abortProc(final Impl impl) {
        return new Runnable() { public void run() { impl.forceAbort(); }};
    }
    
    private static Runnable runProc(final Impl impl) {
        return new Runnable() { public void run() { impl.run(); }};
    }
    
    public Abortable(final Frame dialogParent, final ImplFactory fac, final Option<String> name) {
        if(fac == null)
            throw new NullPointerException("null ImplFactory passed to Abortable");
        if(dialogParent == null)
            throw new NullPointerException("null dialog parent Frame passed to Abortable");
        
        this.name = name.getOrElse("");
        
        
        impl = fac.create(this);
        this.dialogParent = dialogParent;
        this.status = status.InProgress;
    }
    
    public void progress(final double ratio, final Status status, final String message) {
        this.status = status;
        if(dlg != null) {
            SwingUtilities.invokeLater(
                new Runnable() { 
                    public void run() { 
                        dlg.progress(ratio, status, message); 
                    } 
                }
            );
        }
    }

    /**
     * will block with dialog presented to user
     */
    public void run() {
        runThread = new Thread(runProc(impl));
        dlg = new AbortableProgressDialog(dialogParent, true, abortProc(impl), name);
        
        runThread.start();
        UIBox.centerWindow(dlg, dialogParent);
        dlg.setVisible(true);
    }
}