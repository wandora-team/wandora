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
 *
 * 
 * TestEdge.java
 *
 * Created on 4. kesäkuuta 2007, 13:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.application.gui.topicpanels.graphpanel;
import static org.wandora.utils.Tuples.*;
import java.awt.Color;
import java.util.*;

/**
 *
 * @author olli
 */
public class TestEdge extends AbstractEdge {
    private T2<Node,Node> nodes;
    
    /** Creates a new instance of TestEdge */
    public TestEdge(Node a,Node b) {
        nodes=t2(a,b);
    }

    public T2<Node, Node> getNodes() {
        return nodes;
    }
    
}
