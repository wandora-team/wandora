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
 * RBridge.java
 *
 * Created on 21.9.2011, 14:44:26
 */

package org.wandora.application.tools.r;

import java.awt.FileDialog;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Vector;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.wandora.application.Wandora;

/**
 *
 * @author akivela
 */


public class RBridge {
    
    private static RBridge rBridge = null;
    
    
    private boolean prompt=false;

    private boolean isBusy=false;
    private Rengine engine=null;
    private final LoopCallbacks callbacks=new LoopCallbacks();
    
    private ArrayList<RBridgeListener> rBridgeListeners = new ArrayList<RBridgeListener>();
    
    private StringBuilder welcomeMessage = new StringBuilder("");

    //private Thread executeThread = null;
    private Vector<String> inputArray = new Vector<String>();
    
    
    
    private static final String rWelcomeMessage=
            "To read more about Wandora's R console, it's limitations and \n"+
            "possibilities read documentation at \n"+
            "http://wandora.org/wiki/R_in_Wandora\n"+
            "\n";
    
    
    
    private static final String rErrorMessage=
            "Unable to initialize R.\n"+
            "\n"+
            "This is likely because R has not been installed or the environment\n"+
            "variables aren't setup correctly or the rJava library has not been\n"+
            "installed.\n"+
            "\n"+
            "To setup R do the following steps. These instructions are also\n"+
            "explained in more detail in the Wandora wiki at\n"+
            "http://wandora.org/wiki/R_in_Wandora\n"+
            "\n"+
            "1. Download R from http://www.r-project.org and install it.\n"+
            "\n"+
            "2. Install the required libraries in R. You need to run R as an\n"+
            "   administrator to do this. In Windows right click the R icon\n"+
            "   select Run as Administrator. In Linux run R on console using\n"+
            "   \"sudo R\".\n"+
            "\n"+
            "   Then in the R console install the rJava package with\n"+
            "   install.packages(\"rJava\")\n"+
            "   You will likely also want to install the igraph package with\n"+
            "   install.packages(\"igraph\")\n"+
            "   and in Windows also the JavaGD packages\n"+
            "   install.packages(\"JavaGD\")\n"+
            "   You will be asked to select a mirror to download the packages\n"+
            "   from, just select the country you're in or one close to it.\n"+
            "\n"+
            "3. Next make sure that the environment variables are setup correctly.\n"+
            "   Open the SetR.bat (on Windows) or SetR.sh (on Linux) in the bin\n"+
            "   directory. Make sure the R_HOME directory is right. If you are\n"+
            "   using Windows also make sure that the version number matches your\n"+
            "   installation and that the R_ARCH matches your Java installation.\n"+
            "   Use i386 for 32-bit Java and x64 for 64-bit Java. If you did a\n"+
            "   standard installation of R then the other variables should be\n"+
            "   correct, otherwise adjust them as needed.\n"+
            "\n"+
            "You should now be able to use R in Wandora.\n"+
            "\n"+
            "The error encountered while trying to initiale R is shown below:\n\n"
            ;

    

    
    private RBridge() {
        try {
            if(engine == null) {
                // this makes JRI behave gracefully when the native library isn't found
                System.setProperty("jri.ignore.ule", "yes");

                if(!Rengine.versionCheck()) {
                    throw new Exception("R version mismatch, Java files don't match library version.");
                }
                engine=new Rengine(new String[]{},false,callbacks);
                if(engine.waitForR()) {
                    initEngine();
                    appendOutput(rWelcomeMessage);
                }
                else {
                    throw new Exception("Couldn't load R");
                }
                
                /*
                executeThread = new Thread() {
                    @Override
                    public void run() {
                        handleInputLoop();
                    }
                };
                 */
            }
        }
        catch(UnsatisfiedLinkError e){
            engine=null;
            // Wandora.getWandora().handleError(new Exception(e));
            // output.append("Unable to initialize R");
            appendOutput(rErrorMessage);

            StringWriter sw=new StringWriter();
            PrintWriter pw=new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            appendOutput("\n\n"+sw.toString());

        }
        catch(Exception e){
            engine=null;
            // Wandora.getWandora().handleError(e);
            // output.append("Unable to initialize R");
            appendOutput(rErrorMessage);

            StringWriter sw=new StringWriter();
            PrintWriter pw=new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            appendOutput("\n\n"+sw.toString());
        }
    }
    
    
    
    
    public static RBridge getRBridge() {
        if(rBridge == null) {
            rBridge = new RBridge();
        }
        return rBridge;
    }

    
    
    public void addRBridgeListener(RBridgeListener rbl) {
        rBridgeListeners.add(rbl);
        rbl.output(welcomeMessage.toString());
    }
    
    
    public void removeRBridgeListener(RBridgeListener rbl) {
        rBridgeListeners.remove(rbl);
    }
    
    
    

    private Rengine getEngine(){
        return engine;
    }
    
    
    private void initEngine(){
        engine.eval("source(\"resources/conf/rinit.r\",TRUE)");
        engine.startMainLoop();
    }

    
    
    private final Object promptLock=new Object();
    private String promptIn=null;
    private String promptInput(String promptText){
        synchronized(promptLock) {
            appendOutput(promptText);
            prompt=true;
            promptIn=null;
            while(promptIn==null) {
                try {
                    promptLock.wait(200);
                }
                catch(InterruptedException ie) {
                    return null;
                }
                engine.rniIdle();
            }
            prompt=false;
            return promptIn;
        }
    }


    
    public String handleInput(String input) {
        inputArray.add(input);
        handleInputLoop();
        return "";
    }
    
    
    private void handleInputLoop() {
        while(!inputArray.isEmpty()) {
            String input = inputArray.get(0);
            inputArray.remove(0);
            if(input != null) {
                synchronized(callbacks) { 
                    if(isBusy) {
                        continue;
                    } 
                }
                synchronized(promptLock) {
                    if(prompt) {
                        promptIn=input+"\n";
                        appendOutput(promptIn);
                        promptLock.notifyAll();
                    }
                }
            }
            try {
                Thread.currentThread().sleep(200);
            }
            catch(Exception e) {
                // WAKE UP
            }
        }
    }
    
    
    
    private void appendOutput(String text) {
        if(rBridgeListeners == null || rBridgeListeners.isEmpty()) {
            welcomeMessage.append(text);
        }
        else {
            try {
                for(RBridgeListener rbl : rBridgeListeners) {
                    rbl.output(text);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    
    
    

    // -------------------------------------------------------------------------
    
    
    
    
    private class LoopCallbacks implements RMainLoopCallbacks {

        @Override
        public String rChooseFile(Rengine re, int newFile) {
            FileDialog fd = new FileDialog(Wandora.getWandora(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
            fd.setVisible(true);
            String res=null;
            if (fd.getDirectory()!=null) res=fd.getDirectory();
            if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
            return res;
        }

        @Override
        public void rFlushConsole(Rengine re) {
            //output.setText("");
        }

        @Override
        public void rLoadHistory(Rengine re, String filename) {
        }

        @Override
        public void rSaveHistory(Rengine re, String filename) {
        }

        @Override
        public String rReadConsole(Rengine re, String prompt, int addToHistory) {
            String ret=promptInput(prompt);
            return ret;
        }

        @Override
        public void rBusy(Rengine re, int which) {
            synchronized(this){ isBusy=(which!=0); }
        }

        @Override
        public void rShowMessage(Rengine re, String message) {
            appendOutput("message: "+message);
        }

        @Override
        public void rWriteConsole(Rengine re, String text, int oType) {
            appendOutput(text);
        }

    }

    
}
