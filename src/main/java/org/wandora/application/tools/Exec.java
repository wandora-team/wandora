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
 * Exec.java
 *
 * Created on 7. marraskuuta 2005, 11:29
 */

package org.wandora.application.tools;




import org.wandora.application.*;
import org.wandora.application.contexts.*;




/**
 * WandoraTool executing an external application such as WWW browser.
 *
 * @author akivela
 */
public class Exec extends AbstractWandoraTool implements WandoraTool, Runnable {
    

	private static final long serialVersionUID = 1L;
	
	
	private String command = null;
    
    public Exec(){
    }
    
    public Exec(String command){
        this.command=command;
    }
    
    
   @Override
    public void execute(Wandora admin, Context context) {
        Process process=null;
        try {
            if(command != null) {
                process=Runtime.getRuntime().exec(command,new String[]{"HOME=/home/"});
            }
            else {
                singleLog("No external command defined!");
            }
        }
        catch(Exception e) {
            return;
        }
        /*
         * TODO: Looks like the thread is never released here!
         * A tool should not be kept by the thread for ever!!!
         *
        byte[] buf=new byte[256];
        boolean running=true;
        while(running){
            int r=0;
            try{
                if(process.getInputStream().available()>0){
                    r+=process.getInputStream().read(buf);
                    System.out.write(buf,0,r);
                }
                if(process.getErrorStream().available()>0){
                    r+=process.getErrorStream().read(buf);
                    System.err.write(buf,0,r);
                }
            }catch(java.io.IOException ioe){return;}
            if(r==0){
                try{
                    Thread.sleep(500);
                }catch(InterruptedException ie){return;}
            }
        }
         **/
    }
    
    
    @Override
    public boolean allowMultipleInvocations() {
        return true;
    }

    @Override
    public String getName() {
        return "Exec";
    }

    @Override
    public String getDescription() {
        return "Execute external applications such as WWW browser.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
    
    
