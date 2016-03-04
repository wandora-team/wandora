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
 */



package org.wandora.application.tools.browserextractors;

/**
 *
 * @author olli
 */
public class BrowserExtractRequest {
    public static final String APP_FIREFOX="Firefox";
    public static final String APP_THUNDERBIRD="Thunderbird";
    
    private String source;
    private String content;
    private String selectionText;
    private int selectionStart;
    private int selectionEnd;
    private String method;
    private String sourceApplication;
    
    public BrowserExtractRequest(String source,String content,String method,String sourceApplication){
        this.source=source;
        this.content=content;
        this.method=method;
        this.sourceApplication=sourceApplication;
    }
    public BrowserExtractRequest(String source,String content,String method,String sourceApplication,int selectionStart,int selectionEnd){
        this(source,content,method,sourceApplication);
        this.selectionStart=selectionStart;
        this.selectionEnd=selectionEnd;
    }
    public BrowserExtractRequest(String source,String content,String method,String sourceApplication,int selectionStart,int selectionEnd,String selectionText){
        this(source,content,method,sourceApplication);
        this.selectionStart=selectionStart;
        this.selectionEnd=selectionEnd;
        this.selectionText=selectionText;
    }
    public BrowserExtractRequest(String source,String content,String method,String sourceApplication,String selectionText){
        this(source,content,method,sourceApplication);
        this.selectionText=selectionText;
    }
    
    public String getSource(){return source;}
    public String getContent(){return content;}
    public String getSelectionText(){return selectionText;}
    public int getSelectionStart(){return selectionStart;}
    public int getSelectionEnd(){return selectionEnd;}
    public String getSelection(){
        if(selectionStart!=-1){
            return content.substring(selectionStart,selectionEnd);
        }
        else if(selectionText!=null) return selectionText;
        else return null;
    }
    public String getMethod(){return method;}
    public String getApplication(){return sourceApplication;}
    public boolean isApplication(String application){
        if(sourceApplication==null) return false;
        return sourceApplication.toLowerCase().equals(application.toLowerCase());
    }
}
