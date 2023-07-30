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
 * WandoraToolLogger.java
 *
 * Created on 31. toukokuuta 2006, 11:26
 *
 */

package org.wandora.application;


import org.wandora.topicmap.TopicMapLogger;


/**
 * Interface defines constants and methods for loggers used by <code>WandoraTool</code>
 * classes.
 *
 * @author akivela
 */
public interface WandoraToolLogger extends TopicMapLogger {
    /**
     * Constant defines logger state. When logger EXECUTEs, it accepts logs.
    */
    public static final int EXECUTE = 100;
    /**
     * Constant defines logger state. When logger WAITs, it views log history
     * and accepts no logs.
    */
    public static final int WAIT = 200;
    /**
     * Constant defines logger state. When logger is CLOSEd, it has no visual
     * representation and log history is forgot. Generally logger is CLOSEd when
     * tool exists and logging is not required any more.
     */
    public static final int CLOSE = 300;
    /**
     * Constant defines logger state. When logger is INVISIBLE, it has no visual
     * representation but accepts logs and history is available. Generally logger
     * is made INVISIBLE when tool needs to close logger temporarily.
     */
    public static final int INVISIBLE = 400;
    /**
     * Constant defines logger state. When logger is set visible, it is made
     * once again visible after temporal invisibility.
     */
    public static final int VISIBLE = 401;
    
       
    /**
     * Logs given string but doesn't add the string to logger history. The
     * method is used to log repetitive logs such as progress meter that would
     * choke the history.
     * 
     * @param message is the logged text.
     */
    @Override
    public void hlog(String message); // Historyless log == log is not saved to history!
    
    /**
     * Logs given string and adds the string to log history. History can be
     * browsed later when logging has ended.
     * 
     * @param message is the logged string message.
     */
    @Override
    public void log(String message);
    
    /**
     * Logs given string and exception.
     * 
     * @param message is the logged message.
     * @param e is the exception to be logged.
     */
    @Override
    public void log(String message, Exception e);
    
    /**
     * Logs given exception.
     * 
     * @param e is the logged exception.
     */
    @Override
    public void log(Exception e);
    
    /**
     * Logs given error.
     * 
     * @param e is the logged error.
     */
    public void log(Error e);

    /**
     * Logger may view progress information for operation. This method is used to
     * update current progress information with integer n. By default the 
     * progress information is 0..100 but user may change the value range with
     * <code>setProgressMax</code>.
     * 
     * @param n is integer value representing the state of current progress. 
     */
    @Override
    public void setProgress(int n);
    
    /**
     * Set the progress point where operation is ready. Default value is 100.
     * 
     * @param maxn is integer value representing progress when the task is ready.
     */
    @Override
    public void setProgressMax(int maxn);
    
    /**
     * Logging system may have a title. Normally title is the dialog window's
     * title. Method changes the title.
     * 
     * @param title is a string viewed as a title of logger window.
     */
    @Override
    public void setLogTitle(String title);
    
    
    /**
     * Should the logger change current log message? If true, the log should
     * keep the current message visible although new log data is generated.
     * If false, the logger is free to change the log message whenever new
     * log is generated.
     * 
     * @param lock boolean variable that locks or unlocks logger.
     */
    public void lockLog(boolean lock);
    
    
    /**
     * Returns all collected logs as a string.
     * 
     * @return String containing all logged messages.
     */
    public String getHistory();
    
    /**
     * Sets logger's current state. Supported logger states are EXECUTE,
     * WAIT, CLOSE, INVISIBLE, VISIBLE.
     * 
     * @param state of logger.
     */
    public void setState(int state);

    
    /**
     * Returns logger's current state. Supported logger states are EXECUTE,
     * WAIT, CLOSE, INVISIBLE, VISIBLE.
     * 
     * @return Integer value representing current state of logger.
     */
    public int getState();
    
    
    /**
     * <p>
     * Logger should have a mechanism to receive user interruption. Typically this
     * is realized with a Cancel or Stop button. Whenever the user interrupts the
     * operation the logger should return true as the return code of <code>forceStop</code>
     * method.
     * </p>
     * <p>
     * <code>forceStop</code> mechanism relies that the tool using the logger polls
     * <code>forceStop</code> method frequently and cancels the operation as soon as
     * true is returned.
     * </p>
     * 
     * @return boolean, if true the tool should stop immediately and return.
    */
    @Override
    public boolean forceStop();
    
    
}
