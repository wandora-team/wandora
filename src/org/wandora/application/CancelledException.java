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
 * 
 * 
 * CancelledException.java
 *
 * Created on August 30, 2004, 2:42 PM
 */

package org.wandora.application;

/**
 * An exception that is thrown when the user cancels an operation. This is
 * usually thrown by TopicPanel.applyChanges. That method will apply any
 * changes made in the topic panel. These changes may result in topics being
 * merged or split which will cause a warning dialog to be shown. The user
 * can cancel the operation with this dialog which will cause the changes done
 * in the panel to not be applied and this exception be thrown. In such a case
 * the operation that called applyChanges should be canceled. You should also
 * always call applyChanges right at the start of your tool or operation so that
 * you can cancel early and not need to revert back any changes after user
 * aborts the operation.
 *
 * @author  olli
 */
public class CancelledException extends Exception {
    
    /** Creates a new instance of CancelledException */
    public CancelledException() {
    }
    
}
