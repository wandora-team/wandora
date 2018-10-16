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
 * EmailDataSource.java
 *
 * Created on 6. hein�kuuta 2005, 12:11
 */

package org.wandora.application.tools.extractors.email;


import org.wandora.application.tools.extractors.datum.ConfigurableDataSource;
import org.wandora.application.tools.extractors.datum.DataStructure;
import org.wandora.application.tools.extractors.datum.ExtractTool;
import org.wandora.application.tools.extractors.datum.ExtractionException;
import java.util.*;
import org.wandora.utils.*;
import javax.mail.*;




/**
 *
 * @author olli
 */
public class EmailDataSource implements ConfigurableDataSource {
    
    private Collection<Map<String,Object>> data;
    private Iterator<Map<String,Object>> iterator=null;
    private int progress=0;
    
    private ExtractTool extractTool;
    private EmailExtractorPanel panel;
    
    private String host;
    private String port;
    private String user;
    private String pass;
    private String emailStoreDir;
    
    private Object[] additionalColumns;
    
    /** Creates a new instance of EmailDataSource */
    public EmailDataSource(String host,String port,String user,String pass) {
        this(host,port,user,pass,null);
    }
    /*
     * See EmailExtractorPanel constructor about the format of additionalColumns.
     */
    public EmailDataSource(String host,String port,String user,String pass,Object[] additionalColumns) {
        this(host,port,user,pass,additionalColumns,"email/");
    }
    public EmailDataSource(String host,String port,String user,String pass,Object[] additionalColumns,String emailStoreDir) {
        this.emailStoreDir=emailStoreDir;
        this.host=host;
        this.port=port;
        this.user=user;
        this.pass=pass;
        this.additionalColumns=additionalColumns;
    }

    public DataStructure next(org.wandora.piccolo.Logger logger) throws ExtractionException {
        if(iterator==null) {
            iterator=data.iterator();
            //WandoraAdminManager manager=extractTool.getWandora().getManager();

            try{
                logger.writelog("INF","Storing email to Wandora server");
                EmailExtractorPanel.EmailSession session=panel.openEmailSession();
                Message[] msgs=session.getMessages();
                for(int i=0;i<msgs.length;i++){
                    logger.writelog("INF","Storing message "+(i+1)+"/"+msgs.length+"");
                    String msgId=msgs[i].getHeader("message-ID")[0];            
                    msgId=msgId.replaceAll("[<>]","");
                    msgId=msgId.replaceAll("@","_at_");
                    msgId=msgId.replaceAll("\\s","_");
                    if(emailStoreDir!=null){
                        
                        /*
                         * NOTE DEPRECATED AND DISABLED IMPLEMENTATION ASSUMES
                         * MANAGER OBJECT (WANDORAADMINMANAGER). 
                         * 
                         * TODO.
                         * 
                         * 
                        if(!manager.fileExists(emailStoreDir+msgId)){
                            ByteArrayOutputStream baos=new ByteArrayOutputStream();
                            msgs[i].writeTo(baos);
                            byte[] bytes=baos.toByteArray();
                            baos=null;
                            manager.upload(new ByteArrayInputStream(bytes),emailStoreDir+msgId,bytes.length,true);
                        }
                        else logger.writelog("DBG","Skipping, file already at server");
                         * 
                         * 
                         */
                    }
                }
            }catch(Exception e) {
                throw new ExtractionException(e);
            }
        }
        Object o=null;
        if(iterator.hasNext()) o=iterator.next();
        if(o!=null){
            logger.writelog("DBG","Getting next message DataStructure");
            DataStructure ret=new DataStructure(o,null,null);
            progress++;
            return ret;
        }
        else{
            logger.writelog("INF","Deleting selected messages");
            try{
                panel.deleteSelected();
            }catch(Exception e){
                e.printStackTrace();
                logger.writelog("WRN","Error deleting messages",e);
            }
            return null;
        }
    }

    public double getProgress() {
        return (double)progress/(double)data.size();
    }

    public void setExtractTool(ExtractTool extractTool) {
        this.extractTool=extractTool;
    }

    public String getSourceName() {
        return "Email";
    }

    public javax.swing.JPanel getConfigurationPanel() {

        panel=new EmailExtractorPanel(extractTool.getWandora(), 
            new Delegate<Delegate.Void,EmailExtractorPanel>(){
                public Delegate.Void invoke(EmailExtractorPanel param){
                    Collection<Map<String,Object>> panelData=param.getSelectedData();
                    data=panelData;
                    iterator=null;
                    extractTool.guiStart(EmailDataSource.this);
                    return Delegate.VOID;
                }
            },
            new Delegate<Delegate.Void,EmailExtractorPanel>(){
                public Delegate.Void invoke(EmailExtractorPanel param){
                    extractTool.guiCancel();
                    return Delegate.VOID;
                }
            },host,port,user,pass,additionalColumns);
        return panel;
    }
    
}
