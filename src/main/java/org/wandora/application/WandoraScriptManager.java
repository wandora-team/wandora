/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 * WandoraScriptManager.java
 *
 * Created on 9. tammikuuta 2008, 11:23
 *
 */

package org.wandora.application;


import javax.script.ScriptException;

import org.wandora.utils.ScriptManager;
import org.wandora.utils.logger.Log4j2Logger;
/**
 *
 * @author olli
 */
public class WandoraScriptManager extends ScriptManager {
	private static final Log4j2Logger logger = Log4j2Logger.getLogger(WandoraScriptManager.class);
    
    public void showScriptExceptionDialog(String scriptName,ScriptException e){
    	logger.error("Script: "+scriptName+" Line: "+e.getLineNumber()+" Column: "+e.getColumnNumber());
        logger.error(e);
        Throwable cause=e.getCause();
        if(cause!=null) {
        	logger.error("Cause:");
            logger.error(cause);
        }
    }
}
