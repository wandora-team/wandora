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
 * JPEGContentHandler.java
 *
 * Created on December 19, 2001, 1:26 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;


import java.util.HashMap;
import org.wandora.piccolo.utils.crawler.*;


/**
 *
 * @author  olli
 */

public interface JPEGCommentHandler {
    public HashMap handleComment(byte[] comment);
}

