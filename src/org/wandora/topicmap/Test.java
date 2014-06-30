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
 * 
 *
 * Test.java
 *
 * Created on June 10, 2004, 4:28 PM
 */

package org.wandora.topicmap;
import java.util.*;
import java.io.*;
import java.net.*;
import org.wandora.topicmap.memory.*;
/**
 *
 * @author  olli
 */
public class Test {
    
    /** Creates a new instance of Test */
    public Test() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        TopicMap tm=new TopicMapImpl();
        System.out.println("Reading inital topic map from "+args[0]);
        InputStream in=new FileInputStream(args[0]);
        tm.importXTM(in);
        in.close();
        for(int i=1;i<args.length-1;i++){
            System.out.println("Reading next topic map from "+args[i]);
            TopicMap next=new TopicMapImpl();
            in=new FileInputStream(args[i]);
            next.importXTM(in);
            in.close();
            System.out.println("Merging");
            tm.mergeIn(next);
        }
        if(args.length>1){
            System.out.println("Exporting to "+args[args.length-1]);
            OutputStream out=new FileOutputStream(args[args.length-1]);
            tm.exportXTM(out);
            out.close();
        }
    }
    
}
