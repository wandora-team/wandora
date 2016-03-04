/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2016 Wandora Team
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
package org.wandora.modules;

/**
 * A listener class that can be attached to a ModuleManager to get
 * notifications about changes in module states.
 *
 * @author olli
 */
public interface ModuleListener {
    /**
     * Called immediately before a module will be stopped.
     * @param module The module to be stopped.
     */
    public void moduleStopping(Module module);
    /**
     * Called after a module has stopped.
     * @param module The module that was stopped.
     */
    public void moduleStopped(Module module);
    /**
     * Called when a module is about to be started. The module may or may
     * not actually successfully start but it's going to be tried.
     * @param module The module that is about to be started.
     */
    public void moduleStarting(Module module);
    /**
     * Called when a module has successfully started.
     * @param module The module that was started.
     */
    public void moduleStarted(Module module);
}
