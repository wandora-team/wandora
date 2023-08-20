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

package org.wandora.topicmap;
import java.io.PrintStream;
/**
 *
 * @author olli
 */
public class SimpleTopicMapLogger implements TopicMapLogger{

    protected PrintStream out;
    
    public SimpleTopicMapLogger(PrintStream out){
        this.out=out;
    }
    public SimpleTopicMapLogger(){
        this(System.out);
    }
    
    @Override
    public boolean forceStop() {
        return false;
    }

    @Override
    public void hlog(String message) {
        log(message);
    }

    @Override
    public void log(String message) {
        out.println(message);
    }

    @Override
    public void log(String message, Exception e) {
        out.println(message);
        log(e);
    }

    @Override
    public void log(Exception e) {
        e.printStackTrace(out);
    }

    @Override
    public void setLogTitle(String title) {
    }

    @Override
    public void setProgress(int n) {
    }

    @Override
    public void setProgressMax(int maxn) {
    }

}
