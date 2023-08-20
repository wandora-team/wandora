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
 *
 * 
 *
 * TopicMapLogger.java
 *
 * Created on 20.6.2006, 14:21
 *
 */

package org.wandora.topicmap;

/**
 *
 * @author akivela
 */
public interface TopicMapLogger {
    public void log(String message);
    public void log(String message, Exception e);
    public void log(Exception e);
    public void hlog(String message);

    public void setProgress(int n);
    public void setProgressMax(int maxn);
    
    public void setLogTitle(String title);

    public boolean forceStop();
}
