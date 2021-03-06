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
 * OccurrenceTable.java
 *
 * Created on August 17, 2004, 12:07 PM
 */

package org.wandora.application.gui;


import java.awt.event.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;



/**
 *
 * @author  olli, akivela
 */
public interface OccurrenceTable extends MouseListener, Clipboardable {
    public static final String OPTIONS_KEY = "gui.occurrenceTable";
    public static final String VIEW_OPTIONS_KEY = "gui.occurrenceTable.view";
    public static final String ROW_HEIGHT_OPTIONS_KEY = "gui.occurrenceTable.rowHeight";
    
    public static final String VIEW_SCHEMA = "schema";
    public static final String VIEW_USED = "used";
    public static final String VIEW_USED_AND_SCHEMA = "used+schema";
    
    public static final int GOOGLE_TRANSLATE = 1000;
    public static final int MICROSOFT_TRANSLATE = 2000;
    public static final int WATSON_TRANSLATE = 3000;
    
    
    public boolean applyChanges(Topic t, Wandora wandora) throws TopicMapException;

    public Topic getTopic();

    public String getToolTipText(java.awt.event.MouseEvent e);

    public int getRowHeightOption();
    public Object getOccurrenceTableType();

    public String getPointedOccurrence();
    public Topic getPointedOccurrenceType();
    public Topic getPointedOccurrenceLang();

    public void cut();
    public void paste();
    public void append();
    public void copy();
    public String getCopyString();
    public void delete();
    public void spread();

    public void changeType();
    public void duplicateType();

    public void openURLOccurrence();
    public void downloadURLOccurrence();

    public void translate(int translationService);

}
