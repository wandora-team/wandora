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
 * ContextToolWrapper.java
 *
 * Created on 28. hein�kuuta 2006, 12:43
 */

package org.wandora.application.tools;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.simple.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
/**
 *
 * @author olli
 */
public class ContextToolWrapper implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	private WandoraTool wrapped;
    private Context context;
    
    /** Creates a new instance of ContextToolWrapper */
    public ContextToolWrapper(WandoraTool wrapped) {
        this(wrapped,new LayeredTopicContext());
    }
    public ContextToolWrapper(WandoraTool wrapped,Context context) {
        this.wrapped=wrapped;
        this.context=context;
    }
    
    @Override
    public WandoraToolType getType(){
        return wrapped.getType();
    }

    @Override
    public Context getContext(){
        return context;
    }
    @Override
    public void setContext(Context context){
        this.context=context;
    }
    
    @Override
    public boolean forceStop(){
        return wrapped.forceStop();
    }
    @Override
    public int getState(){
        return wrapped.getState();
    }
    @Override
    public void setState(int state){
        wrapped.setState(state);
    }
    @Override
    public void setToolLogger(WandoraToolLogger logger) {
        wrapped.setToolLogger(logger);
    }
    @Override
    public void hlog(String message){
        wrapped.hlog(message);
    }
    @Override
    public void log(String message){
        wrapped.log(message);
    }
    @Override
    public void log(String message, Exception e){
        wrapped.log(message,e);
    }
    @Override
    public void log(Exception e){
        wrapped.log(e);
    }
    @Override
    public void log(Error e){
        wrapped.log(e);
    }

    
    @Override
    public void setProgress(int n){
        wrapped.setProgress(n);
    }
    @Override
    public void setProgressMax(int maxn){
        wrapped.setProgressMax(maxn);
    }
    
    @Override
    public void setLogTitle(String title){
        wrapped.setLogTitle(title);
    }
   
    @Override
    public void lockLog(boolean lock){
        wrapped.lockLog(lock);
    }
    @Override
    public String getHistory(){
        return wrapped.getHistory();
    }
    
    
    @Override
    public String getName() {
        return wrapped.getName();
    }
    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }
    
    @Override
    public void initialize(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        wrapped.initialize(wandora,options,prefix);
    }
    @Override
    public boolean isConfigurable(){
        return wrapped.isConfigurable();
    }
    @Override
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        wrapped.configure(wandora,options,prefix);
    }
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
        wrapped.writeOptions(wandora,options,prefix);
    }    
    
    @Override
    public void execute(Wandora wandora) throws TopicMapException {
        execute(wandora, (ActionEvent) null);
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        wrapped.execute(wandora,context);
    }
    
    @Override
    public void execute(Wandora wandora, ActionEvent event)  throws TopicMapException {
        wrapped.setContext(context);
        wrapped.execute(wandora,event);
    }    
    
    @Override
    public boolean isRunning() {
        return wrapped.isRunning();
    }

    @Override
    public boolean requiresRefresh(){
        return wrapped.requiresRefresh();
    }
    
    @Override
    public SimpleMenuItem getToolMenuItem(Wandora wandora,String instanceName){
        return wrapped.getToolMenuItem(wandora,instanceName);
    }   
    
    @Override
    public Icon getIcon() {
        return wrapped.getIcon();
    }
}
