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
 */


package org.wandora.application.gui.table;

import java.util.Comparator;
import javax.swing.table.TableRowSorter;
import org.wandora.topicmap.Locator;

/**
 *
 * @author akivela
 */


public class LocatorTableRowSorter extends TableRowSorter {


    public LocatorTableRowSorter(LocatorTableModel dm) {
        super(dm);
    }


    
    @Override
    public Comparator<?> getComparator(int column) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                if(o1 == null || o2 == null) return 0;
                if(o1 instanceof Locator && o2 instanceof Locator) {
                    try {
                        String l1 = ((Locator) o1).toExternalForm();
                        String l2 = ((Locator) o2).toExternalForm();
                        return l1.compareTo(l2);
                    }
                    catch(Exception e) {
                        return 0;
                    }
                }
                else if(o1 instanceof String && o2 instanceof String) {
                    try {
                        int d = ((String) o1).compareTo((String) o2);
                        return d;
                    }
                    catch(Exception e) {}
                    return 0;
                }
                else {
                    return 0;
                }
            }
        };
    }

    
    
    @Override
    public boolean useToString(int column) {
        return false;
    }
}
