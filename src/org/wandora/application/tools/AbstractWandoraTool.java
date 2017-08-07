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
 * AbstractWandoraTool.java
 *
 * Created on 20. lokakuuta 2005, 19:20
 *
 */

package org.wandora.application.tools;


import org.wandora.utils.Tuples.T2;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.SimpleMenuItem;
import org.wandora.application.*;

import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import static org.wandora.application.gui.ConfirmResult.*;
import org.wandora.utils.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;



/**
 * <code>AbstractWandoraTool</code> provides basic services and methods to
 * all <code>WandoraTool</code> classes. All tools should extend this
 * abstract class.
 *
 * @author akivela
 */



public abstract class AbstractWandoraTool implements WandoraTool, Runnable {

    private WandoraToolPanel toolPanel = null;
    private Exception toolException;
    private WandoraToolLogger lastLogger = null;
    private WandoraToolLogger logger = null;
    private boolean internalForceStop;
    
    private Wandora runAdmin = null;
    private Context runContext = null;
    
    private static final HashSet<Class> toolLocks = new HashSet<Class>();
    private static final HashMap<Thread,T2<Class,Long>> toolThreads = new HashMap<Thread,T2<Class,Long>>();

    
    
    public AbstractWandoraTool() {
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------ EXECUTING TOOL ---
    // -------------------------------------------------------------------------
    
    
    /* *
     * This is first entry point to execute the tool. Use this entry point when
     * you wish to execute the tool from your own code. Method just fills the 
     * event slot with null and calls the event triggered execute.
     */
    @Override
    public void execute(Wandora wandora) throws TopicMapException {
        execute(wandora, (ActionEvent) null);
    }
    
    
    /**
     * This is the primary entry point to execute the tool. All UI 
     * actions should enter the tool here. Usually this method is called 
     * from the <code>WandoraToolActionListener</code>.
     */
    @Override
    public void execute(Wandora wandora, ActionEvent event)  throws TopicMapException {
        internalForceStop = false;
        
        if(event != null) {
            int modifiers=event.getModifiers();
            if((modifiers&ActionEvent.SHIFT_MASK)!=0 && (modifiers&ActionEvent.CTRL_MASK)!=0 && (modifiers&ActionEvent.MOUSE_EVENT_MASK)!=0 ) {
                try {
                    if(wandora != null) {
                        String className = this.getClass().getName();
                        String helpName = className.substring(className.lastIndexOf('.')+1);
                        String helpUrl = wandora.getOptions().get("helpviewer");
                        if(helpUrl != null) helpUrl = helpUrl.replaceAll("__HELP__", helpName);
                        
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(new URI(helpUrl));
                        
                        try { Thread.sleep(200); } 
                        catch(Exception e) {} // WAKEUP!
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            if((modifiers&ActionEvent.CTRL_MASK)!=0 && (modifiers&ActionEvent.MOUSE_EVENT_MASK)!=0 ) {
                try {
                    boolean configured=false;
                    if(isConfigurable()){
                        String prefix=wandora.getToolManager().getOptionsPrefix(this);
                        if(prefix == null) {
                            prefix = "options."+this.getClass().getCanonicalName();
                        }
                        configured=true;
                        configure(wandora,wandora.getOptions(),prefix);
                    }
                    if(!configured) WandoraOptionPane.showMessageDialog(wandora,"Tool is not configurable","Tool is not configurable");
                    if(!allowMultipleInvocations()){
                        synchronized(toolLocks){
                            toolLocks.remove(this.getClass());
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            if((modifiers&ActionEvent.ALT_MASK)!=0 && (modifiers&ActionEvent.MOUSE_EVENT_MASK)!=0 ) {
                try {
                    synchronized(toolLocks){
                        toolLocks.remove(this.getClass());
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(!allowMultipleInvocations()){
            synchronized(toolLocks){
                if(toolLocks.contains(this.getClass())){
                    singleLog("Tool '"+this.getName()+"' already running!\nWait or use clear tool locks.");
                    return;
                }
                toolLocks.add(this.getClass());
            }
        }
        
        runAdmin = wandora;
        if(runContext == null) { runContext = new LayeredTopicContext(); }
        
        // TODO: runContext is overwritten if the same instance of a tool  
        // is executed again.
        runContext.initialize(wandora, event, this);

        if(runInOwnThread()) {
            Thread worker = new Thread(this, getName());
            synchronized(toolThreads) {
                toolThreads.put(worker, new T2(this.getClass(), new Long(System.currentTimeMillis())));
            }
            //SwingUtilities.invokeLater(worker);
            worker.start();
        }
        else {
            run();
        }
    }
    
   
    /**
     * Runs the tool. If <code>runInOwnThread</code> returns false,
     * this method is called directly instead of creating a new <code>Thread</code>.
     * This method passes the execution to extending implementation of 
     * <code>execute</code>.
     */
    @Override
    public void run() {
        toolException = null;
        try {
            if(runAdmin != null) {
                runAdmin.setAnimated(true, this);
            }
            execute(runAdmin, runContext);
        }
        catch(Exception e) {
            if(runAdmin != null) {
                runAdmin.displayException(ErrorMessages.getMessage(e, this), e);
            }
            toolException = e;
        }
        catch(Error er) {
            if(runAdmin != null) {
                runAdmin.displayException(ErrorMessages.getMessage(er, this), er);
            }
        }
        if(!allowMultipleInvocations()) {
            synchronized(toolLocks){
                toolLocks.remove(this.getClass());
            }
        }
        if(runInOwnThread()) {
            synchronized(toolThreads) {
                try {
                    toolThreads.remove(Thread.currentThread());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
                
        try {
            if(runAdmin != null) {
                if(requiresRefresh()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runAdmin.doRefresh();
                            }
                            catch(Exception e) {
                                // SKIPPING
                            }
                        }
                    });
                }
                else {
                    // requiresRefresh() == false --> Do nothing
                }
            }
        }
        catch(Exception e) {
            System.out.println("Refresh failed in AbstractAdminTool!");
            e.printStackTrace();
        }
        
        if(runAdmin != null) {
            runAdmin.setAnimated(false, this);
        }
        if(logger != null && logger.getState() == EXECUTE) {
            System.out.println("Warning! Logger still running when leaving tool! Closing logger!");
            logger.setState(CLOSE);
        }
        if(logger != null) {
            lastLogger = logger;
            logger = null;
        }
    }
    
    
    
    /**
     * Checks if this (extended) tool is running. 
     * 
     * @return boolean true if the tool is running.
     */
    @Override
    public boolean isRunning() {
        return isRunning(this.getClass());
    }
    
    
    /**
     * Checks if given tool is already running and
     * locked.
     * 
     * @return boolean true if the tool is running.
     */
    public boolean isRunning(Class c) {
        synchronized(toolLocks){
            if(toolLocks.contains(c)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Releases a tool lock for this (extending) tool class.
     * 
     * @return boolean true if a lock was released.
     */
    public boolean clearToolLock() {
        return clearToolLock(this.getClass());
    }
    
    /**
     * Releases a tool lock for a given tool class. Tool lock prevents tool execution
     * while previous execution is unfinished. Doesn't stop nor kill the thread
     * that runs the previous execution.
     * 
     * @return boolean true if a lock was released.
     */
    public static boolean clearToolLock(Class c) {
        if(c != null) {
            synchronized(toolLocks){
                if(toolLocks.contains(c)) {
                    toolLocks.remove(c);
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * Releases all locks that prevent tools to be executed again.
     * Notice the method doesn't kill any threads. Threads must be interrupted
     * separately with interruptAllThreads method.
     * 
     * @return Integer number of locks released.
     */
    public static int clearToolLocks() {
        int n = 0;
        synchronized(toolLocks){
            n = toolLocks.size();
            toolLocks.clear();
        }
        Wandora w = Wandora.getWandora();
        if(w != null) {
            w.forceStopAnimation();
        }
        return n;
    }
    
    
    public void interruptThreads() {
        interruptThreads(this.getClass());
    }
    
    public static void interruptThreads(Class c) {
        if(c == null) return;
        synchronized(toolThreads) {
            try {
                T2<Class, Long> timedClass = null;
                for( Thread toolThread : toolThreads.keySet() ) {
                    timedClass = toolThreads.get(toolThread);
                    if(c.equals(timedClass.e1)) {
                        toolThread.interrupt();
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public static void interruptAllThreads() {
        synchronized(toolThreads) {
            try {
                for( Thread toolThread : toolThreads.keySet() ) {
                    toolThread.interrupt();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    
    public void clearThreads() {
        clearThreads(this.getClass());
    }
    
    
    public static void clearThreads(Class c) {
        if(c == null) return;
        synchronized(toolThreads) {
            ArrayList<Thread> threadList = new ArrayList<Thread>();
            try {
                T2<Class, Long> timedClass = null;
                for( Thread toolThread : toolThreads.keySet() ) {
                    timedClass = toolThreads.get(toolThread);
                    if(c.equals(timedClass.e1)) {
                        threadList.add(toolThread);
                    }
                }
                for( Thread toolThread : threadList ) {
                    toolThreads.remove(toolThread);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public static void clearAllThreads() {
        synchronized(toolThreads) {
            ArrayList<Thread> threadList = new ArrayList<Thread>();
            try {
                for( Thread toolThread : toolThreads.keySet() ) {
                    threadList.add(toolThread);
                }
                for( Thread toolThread : threadList ) {
                    toolThreads.remove(toolThread);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public ArrayList<T2<Thread,Long>> getThreads() {
        return getThreads(this.getClass());
    }
            
    public static ArrayList<T2<Thread,Long>> getThreads(Class c) {
        ArrayList<T2<Thread, Long>> threadList = new ArrayList<T2<Thread, Long>>();
        if(c != null) {
            synchronized(toolThreads) {
                try {
                    T2<Class, Long> timedClass = null;
                    for( Thread toolThread : toolThreads.keySet() ) {
                        timedClass = toolThreads.get(toolThread);
                        if(c.equals(timedClass.e1)) {
                            threadList.add( new T2(toolThread, timedClass.e2) );
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return threadList;
    }
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------- TOOL FLAVOUR ---
    // -------------------------------------------------------------------------
    
           
    /**
     * Whether or not this tool should fork own thread. If own thread is allowed,
     * the execution of the tool return immediately. If own thread is not allowed
     * the thread entering initial execute method is used. Extending classes should
     * override this method.
     */
    public boolean runInOwnThread() {
        return true;
    }
    
    /**
     * <p>
     * Tool type is used to categorize tools. Tool type has no real effect today,
     * it is merely an informative property of a tool.
     * </p>
     */
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createGenericType();
    }
    
    /**
     * Tools name represent the tool in UI unless the tool has been given
     * explicitly another GUI name. All tools should override this method and
     * return a valid string representing name of the tool.
     */
    @Override
    public String getName() {
        return "Abstract Tool";
    }
    
    /**
     * AdminToolManager views tool descriptions while user browses available
     * tools and build user customizable GUI elements such as Tools menu.
     * By default description equals the tool name.
     * All tools should override this method.
     */
    @Override
    public String getDescription() {
        return getName();
    }
    
    
    /**
     * <p>
     * If any visible topic has been changed during tool execution GUI is
     * automatically refreshed. If tool doesn't change topics but GUI still
     * requires refresh, tool should override this method and return true.</p>
     * <p>
     * For example tools that alter the GUI but change no topics should
     * return true.</p>
     */
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
    

    /**
     * Should the tool allow more than one running occurrence of same tool class.
     * Sometimes it is necessary to prevent user running same tool after previous
     * execution has ended. This is the case if tool for example requires much
     * computing power or locks computing resources. By default tool is not
     * allowed to run multiple parallel instances.
     */
    public boolean allowMultipleInvocations(){
        return false;
    }
    
    
    /**
     * <code>WandoraWandoraTool</code> should be GUI independent allowing tool
     * to be inserted into various type of GUI elements such as menus.
     * This method is used to wrap tool into <code>SimpleMenuItem</code>.
     */
    @Override
    public SimpleMenuItem getToolMenuItem(Wandora wandora, String instanceName) {
        SimpleMenuItem manageMenuItem = new SimpleMenuItem(instanceName, new WandoraToolActionListener(wandora, this));
        Icon icon=getIcon();
        if(icon!=null) {
            manageMenuItem.setIcon(icon);
        }
        String description = getDescription();
        if(description != null && description.length() > 0) {
            manageMenuItem.setToolTipText(Textbox.makeHTMLParagraph(description, 30));
        }
        return manageMenuItem;
    }
    public SimpleMenuItem getToolMenuItem(Wandora wandora, String instanceName, KeyStroke keyStroke) {
        SimpleMenuItem menuItem = getToolMenuItem(wandora,instanceName);
        if(keyStroke != null) {
            menuItem.setAccelerator(keyStroke);
        }
        return menuItem;
    }
    
    
    /**
     * All tools may have identifying graphic icon used within tool GUI elements.
     * <code>getIcon</code> should return <code>Icon</code> object of
     * the tool.
     */
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/generic_tool.png");
    }
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------- CONFIGURING TOOL ---
    // -------------------------------------------------------------------------
    
    
    
    /**
     * Initializes a tool with options saved in the options. Options for
     * this tool start with the given prefix. What options the tool saves
     * is entirely tool specific.
     */
    @Override
    public void initialize(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
    }
    /**
     * Whether this tool is configurable. Configurable tools will provide an interface
     * to configure themselves. To make a configurable tool you will need to:
     * <ul>
     * <li>Implement isConfigurable method and return true there.</li>
     * <li>Implement initialize method to initialize the tool with saved options.</li>
     * <li>Implement configure method to provide the user interface for configuration and then
     *   save the new options.</li>
     * <li>Implement writeOptions method to save the current options.</li>
     * </ul>
     */
    @Override
    public boolean isConfigurable(){
        return false;
    }
    /**
     * If the tool is configurable, shows an user interface to configure the tool. The user
     * may edit tool options which this method also need to save. Options specific
     * to this tool should be saved with keys starting with the given prefix.
     */
    @Override
    public void configure(Wandora wandora ,org.wandora.utils.Options options, String prefix) throws TopicMapException {
    }
    /**
     * If the tool is configurable, saves all current tool options. Options specific
     * to this tool should be saved with keys starting with the given prefix.
     */
    @Override
    public void writeOptions(Wandora wandora, org.wandora.utils.Options options, String prefix){
    }    

    

    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------- ACCESSING CONTEXT ---
    // -------------------------------------------------------------------------
    
    
    /**
     * Each executed tool has <code>Context</code> containing context source and
     * context objects. Context source is a GUI element that originates the
     * tool execution. <code>Context</code> object reads context source and
     * solves context objects available in tool. Generally tools modify context
     * objects. <code>setContext</code> method is used to set the <code>Context</code>
     * object that transforms context source into context objects. If tool has
     * no explicitly set context <code>AbstractWandoraTool</code> uses <code>LayeredTopicContext</code>.
     */
    @Override
    public void setContext(Context context) { runContext = context; }
    
    
    /**
     * Return tools <code>Context</code>. If tool uses default context ie.
     * <code>setContext</code> has not been called with valid <code>Context</code>
     * object, tool's context remains undefined (<code>null</code>) until tool
     * is executed. Tool context is resolved finally during execution. More over
     * context source and thus context object at least are solved during
     * execution, not before.
     */
    @Override
    public Context getContext() { return runContext; }
    
   
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------- ACCESSING LOGGER ---
    // -------------------------------------------------------------------------
    /**
     * <p>
     * <code>AbstractWandoraTool</code> provides simple framework for tool-user
     * communication. If <code>Wandora</code> object is passed to the execute method
     * logger is simple dialog window capable to output tool
     * originated messages and progress meter. Default dialog also contains simple
     * stop button allowing user to interrupt the tool execution (if tool
     * acknowledges user interrupts). Default logger dialog is activated
     * with <code>setDefaultLogger</code> method.</p>
     * <p>
     * Note: If your tool requires rich interaction between user and tool
     * You may not want to activate default logger at the very beginning of
     * your tool.</p>
     */
    public void setDefaultLogger() {
        setToolLogger(getDefaultLogger());
        setLogTitle(getName());
    }
    public WandoraToolLogger getDefaultLogger() {
        if(logger != null) return logger;
        if(runAdmin != null) {
            InfoDialog infoDialog = new InfoDialog(runAdmin);
            infoDialog.setState(InfoDialog.EXECUTE);
            return infoDialog;
        }
        return null;
    }
    public WandoraToolLogger getCurrentLogger() {
        return logger;
    }
    public WandoraToolLogger getLastLogger() {
        return lastLogger;
    }

    
    public void singleLog(String message) {
        setDefaultLogger();
        log(message);
        setState(WandoraToolLogger.WAIT);
    }
    public void singleLog(Exception e) {
        setDefaultLogger();
        log(e);
        setState(WandoraToolLogger.WAIT);
    }
    public void singleLog(String message, Exception e) {
        setDefaultLogger();
        log(message, e);
        setState(WandoraToolLogger.WAIT);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void setToolLogger(WandoraToolLogger logger) {
        this.logger = logger;
    }
    
    
    // -------------------------------------------------------------------------

    
    @Override
    public void hlog(String message) {
        if(logger != null) logger.hlog(message);
        else if(runAdmin != null) {
            WandoraOptionPane.showMessageDialog(runAdmin, message);
        }
        else System.out.println(message);
    }
    
    @Override
    public void log(String message) {
        if(logger != null) logger.log(message);
        else if(runAdmin != null) {
            WandoraOptionPane.showMessageDialog(runAdmin, message);
        }
        else System.out.println(message);
    }
    
    @Override
    public void log(String message, Exception e) {
        if(logger != null) logger.log(message, e);
        else if(runAdmin != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            WandoraOptionPane.showMessageDialog(runAdmin, message + "\n" + sw.toString(), WandoraOptionPane.ERROR_MESSAGE);
            internalForceStop = true;
        }
        else {
            System.out.println(message);
            e.printStackTrace();
        }
    }
    @Override
    public void log(Exception e) {
        if(logger != null) logger.log(e);
        else if(runAdmin != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            WandoraOptionPane.showMessageDialog(runAdmin, sw.toString(), WandoraOptionPane.ERROR_MESSAGE);
            internalForceStop = true;
        }
        else {
            e.printStackTrace();
        }
    }
    @Override
    public void log(Error e) {
        if(logger != null) logger.log(e);
        else if(runAdmin != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            WandoraOptionPane.showMessageDialog(runAdmin, sw.toString(), WandoraOptionPane.ERROR_MESSAGE);
            internalForceStop = true;
        }
        else {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setProgress(int n) {
        if(logger != null) logger.setProgress(n);
    }
    @Override
    public void setProgressMax(int maxn) {
        if(logger != null) logger.setProgressMax(maxn);
    }
    @Override
    public void setLogTitle(String title) {
        if(logger != null) logger.setLogTitle(title);
        else System.out.println(title);
    }
    
    @Override
    public void lockLog(boolean lock) {
        if(logger != null) logger.lockLog(lock);
    }
    
    
    @Override
    public String getHistory() {
        if(logger != null) return logger.getHistory();
        else return "";
    }
    
    @Override
    public void setState(int state) {
        if(logger != null) logger.setState(state);
    }
    @Override
    public int getState() {
        if(logger != null) return logger.getState();
        else return 0;
    }
    
    @Override
    public boolean forceStop() {
        if(logger != null) return logger.forceStop();
        else return internalForceStop;
    }
    
    public boolean forceStop(ConfirmResult result) {
        if(result == notoall || result == cancel) return true;
        if(logger != null) return logger.forceStop();
        else return internalForceStop;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public TopicMap solveContextTopicMap(Wandora wandora, Context context) {
        if(context != null) {
            //System.out.println("context-source: " + context.getContextSource());
            Object contextSource = context.getContextSource();
            if(contextSource != null) {
                if(contextSource instanceof TopicMap) {
                    return (TopicMap) contextSource;
                }
                else if(contextSource instanceof Layer){
                    return ((Layer)contextSource).getTopicMap();
                }
                else if(context.getContextSource() instanceof LayerTree){
                    return ((LayerTree)context.getContextSource()).getLastClickedLayer().getTopicMap();
                }
                else if(contextSource instanceof Wandora) {
                    return ((Wandora) contextSource).getTopicMap();
                }
            }
        }
        // Finally if everything else fails...
        return wandora.getTopicMap();
    }

    
    protected String solveNameForTopicMap(Wandora wandora, TopicMap topicMap) {
        if(wandora != null) {
            Layer l=wandora.layerTree.findLayerFor(topicMap);
            if(l!=null) {
                return l.getName();
            }
            if(topicMap != null && topicMap.equals(wandora.getTopicMap())) {
                return "Layer stack";
            }
        }
        return "Unknown";
    }
    
    
    
    /*
    protected Topic getTopicForSelectedLayer(Topic topic) {
        if(topic instanceof LayeredTopic) {
            topic = ((LayeredTopic) topic).getTopicForSelectedLayer();
        }
        return topic;
    }
     **/
    
    
    
    // -------------------------------------------------------------------------
    
    /*
     * Tools need topic names very often, for logging for example. 
     * This shortcut method gives an easy way to get topic's name.
     * 
     * <p>Should this method use the class <code>TopicToString</code> instead
     * of it's own name resolution code?? Yes, it should!</p>
     */
    public static String getTopicName(Topic t) {
        if(t == null) {
            return "[null]";
        }
        try {
            if(t.isRemoved()) {
                return "[removed]";
            }
            if(t.getBaseName() == null) {
                return t.getOneSubjectIdentifier().toExternalForm();
            }
            return t.getBaseName();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "[error]";
    }
    
    // --------------------------------------------------------- UNDO / REDO ---
    
    
    protected void addUndoMarker() {
        addUndoMarker(this.getName());
    }
    
    
    
    protected void addUndoMarker(String label) {
        if(runAdmin != null) {
            String cn = this.getClass().getName();
            if(!"org.wandora.application.tools.Undo".equals(cn) && !"org.wandora.application.tools.Redo".equals(cn)) {
                runAdmin.addUndoMarker(label);
            }
        }
    }
    
    
    
}

