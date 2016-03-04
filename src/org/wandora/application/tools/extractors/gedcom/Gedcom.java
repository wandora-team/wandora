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
 * Gedcom.java
 *
 * Created on 2009-10-1, 20:52
 */


package org.wandora.application.tools.extractors.gedcom;

import org.wandora.application.*;
import java.io.*;
import java.util.*;


/**
 * Class is based on Mike Dean's work on GEDCOM to DAML converter. DAML converter is
 * described at http://www.daml.org/2001/01/gedcom/
 *
 * @author akivela
 */



public class Gedcom {
    public static boolean DEBUG = false;

    // map from String key to Node.
    public Hashtable keys = new Hashtable();
    
    // stack used while parsing.
    private Stack stack = new Stack();

    public int numberOfTopLevelNodes = 0;



    /**
     * return the Node with the specified key (e.g. "@I1@").
     */
    private Node lookup(String key) {
	return (Node) keys.get(key);
    }



    private void parseLine(String line) {
	int space1 = line.indexOf(' ');
	int level = Integer.parseInt(line.substring(0, space1));
        if(level == 0) numberOfTopLevelNodes++;
        
	String key = null;
	int tagStart = space1 + 1;
	if(line.charAt(tagStart) == '@') {
            int keyEnd = line.indexOf(' ', tagStart);
            key = line.substring(tagStart, keyEnd);
            tagStart = keyEnd + 1;
        }
	String tag = null;
	String value = null;
	int tagEnd = line.indexOf(' ', tagStart);
	if(tagEnd == (-1)) {
            tag = line.substring(tagStart);
        }
	else {
            tag = line.substring(tagStart, tagEnd);
            value = line.substring(tagEnd + 1);
        }

	if(DEBUG) {
            System.out.println("level = " + level);
            System.out.println("key = " + key);
            System.out.println("tag = " + tag);
            System.out.println("value = " + value);
            System.out.println();
        }

	Node node = new Node(key, tag, value);
	while(level < stack.size()) {
	    stack.pop();
        }
	stack.push(node);
	if(level > 0) {
            Node parent = (Node) stack.elementAt(level - 1);
            parent.children.add(node);
        }
    }





    /**
     * parse the specified file and return a tree representation.
     */
    public static Gedcom parse(BufferedReader breader, WandoraTool tool) throws Exception {
        Gedcom gedcom = new Gedcom();
	String line;
        int i = 0;
        try {
            while((line = breader.readLine()) != null && !tool.forceStop()) {
                gedcom.parseLine(line);
                i++;
                tool.setProgress(i / 1000);
            }
        }
        catch(Exception e) {
            tool.log("Aborting model parsing...");
            tool.log("Exception occurred while reading GEDCOM stream at line "+(i));
            tool.log(e);
        }
        tool.log("Total "+gedcom.numberOfTopLevelNodes+" top level nodes parsed...");
	return gedcom;
    }


    

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    public class Node {
	String key = null;
	String tag = null;
	String value = null;
	ArrayList children = new ArrayList();


	Node(String key, String tag, String value) {
	    this.key = key;
	    this.tag = tag;
	    this.value = value;
	    if (key != null) {
		keys.put(key, this);
            }
	}
    }
}
