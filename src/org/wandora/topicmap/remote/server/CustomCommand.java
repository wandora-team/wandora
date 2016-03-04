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
 *
 * CustomCommand.java
 *
 * Created on July 27, 2004, 2:00 PM
 */

package org.wandora.topicmap.remote.server;
import java.io.*;
import org.wandora.piccolo.*;


/**
 * You only need to implement one of the execute methods.
 *
 * @author  olli
 */
public abstract class CustomCommand {
    public abstract boolean match(String cmd);
    public void execute(String cmd,InputStream in,OutputStream out) throws IOException {
        String ret=execute(cmd);
        if(!ret.endsWith("\n")) ret+="\n";
        out.write(ret.getBytes("UTF-8"));
    }
    public String execute(String cmd){
        throw new RuntimeException("Not implemented");
    }
}
