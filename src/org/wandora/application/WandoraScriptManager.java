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
 * WandoraScriptManager.java
 *
 * Created on 9. tammikuuta 2008, 11:23
 *
 */

package org.wandora.application;


import javax.script.*;
import org.wandora.utils.ScriptManager;
/**
 *
 * @author olli
 */
public class WandoraScriptManager extends ScriptManager {
    
    public void showScriptExceptionDialog(String scriptName,ScriptException e){
        System.out.println("Script: "+scriptName+" Line: "+e.getLineNumber()+" Column: "+e.getColumnNumber());
        e.printStackTrace();
        Throwable cause=e.getCause();
        if(cause!=null) {
            System.out.println("Cause:");
            cause.printStackTrace();
        }
    }
}
