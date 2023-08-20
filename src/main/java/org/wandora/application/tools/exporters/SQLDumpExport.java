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
 */

package org.wandora.application.tools.exporters;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.SimpleTopicMapLogger;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapLogger;
import org.wandora.topicmap.parser.XTMParser2;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 *
 * @author olli
 */
public class SQLDumpExport extends AbstractExportTool {

	private static final long serialVersionUID = 1L;

	
	public static final int I_TOPIC=0;
    public static final int I_ASSOCIATION=1;
    public static final int I_MEMBER=2;
    public static final int I_SUBJECTIDENTIFIER=3;
    public static final int I_TOPICTYPE=4;
    public static final int I_DATA=5;
    public static final int I_VARIANT=6;
    public static final int I_VARIANTSCOPE=7;

    
    @Override
    public String getName() {
        return "Topic map SQL export";
    }
    
    @Override
    public String getDescription() {
        return "Export topic map as a series of SQL insert statements. "+
                "Exported SQL statements can be imported into an empty SQL database and "+
                "Wandora can use the SQL database as a database topic map.";
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    private static int idcounter=0;    
    protected static synchronized String makeID(String prefix){
        if(idcounter>=100000) idcounter=0;
        return prefix+System.currentTimeMillis()+"-"+(idcounter++);
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Topic map SQL dump export");
        if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();        
            File file = chooser.getSelectedFile();
//            TopicMap tm=wandora.getTopicMap();
            TopicMap tm = solveContextTopicMap(wandora,context);
            
            try{
                String filePath=file.getAbsolutePath();
                File[] tempFiles=new File[8];
                for(int i=0;i<tempFiles.length;i++){
                    tempFiles[i]=File.createTempFile("wandora_",".sql");
                }
                PrintStream[] out=new PrintStream[tempFiles.length];
                for(int i=0;i<out.length;i++){
                    out[i]=new PrintStream(tempFiles[i]);
                }                
                
                log("Exporting database topic map as SQL.");
                log("Exporting topics.");
                int progress = 0;
                setProgressMax(tm.getNumTopics());
                
                Iterator<Topic> iter=tm.getTopics();
                while(iter.hasNext()) {
                    setProgress(progress++);
                    Topic t=iter.next();
                    writeTopic(t.getID(), t.getBaseName(), t.getSubjectLocator(), out);
                    
                    for(Topic type : t.getTypes()){
                        writeTopicType(t.getID(),type.getID(),out);
                    }
                    for(org.wandora.topicmap.Locator l : t.getSubjectIdentifiers()){
                        writeSubjectIdentifier(t.getID(), l.toString(), out);
                    }
                    for(Set<Topic> scope : t.getVariantScopes()){
                        String variantid=makeID("V");
                        String value=t.getVariant(scope);
                        writeVariant(variantid,t.getID(),value,out);
                        for(Topic s : scope){
                            writeVariantScope(variantid, s.getID(), out);
                        }
                    }
                    for(Topic type : t.getDataTypes()){
                        Hashtable<Topic,String> data=t.getData(type);
                        for(Map.Entry<Topic,String> e : data.entrySet()){
                            writeData(t.getID(), type.getID(), e.getKey().getID(), e.getValue(), out);
                        }
                    }
                }
                
                Iterator<Association> iter2=tm.getAssociations();
                log("Exporting associations.");
                progress = 0;
                setProgressMax(tm.getNumAssociations());
                
                while(iter2.hasNext()) {
                    setProgress(progress++);
                    Association a=iter2.next();
                    String associationid=makeID("A");
                    writeAssociation(associationid, a.getType().getID(), out);
                    for(Topic role : a.getRoles()){
                        Topic player=a.getPlayer(role);
                        writeMember(associationid, player.getID(), role.getID(), out);
                    }
                }
                
                closeStreams(out);
                for(PrintStream outStream : out) {
                    outStream.close();
                }
                
                log("Creating output file.");
                OutputStream outs=new FileOutputStream(file);
                for(File tempFile : tempFiles) {
                    appendFile(outs, tempFile);
                    tempFile.delete();
                }
                outs.close();       
                log("Ready.");
                setState(SQLDumpExport.WAIT);
            }
            catch(Exception e) {
                e.printStackTrace();
                log(e);
            }
        }
    }
    
    
    
    
    
    public void appendFile(OutputStream out,File in) throws IOException {
        InputStream ins=new FileInputStream(in);
        appendFile(out,ins);
        ins.close();
    }
    
    
    public void appendFile(OutputStream out,InputStream in) throws IOException {
        byte[] buf=new byte[4096];
        int read=-1;
        while( (read=in.read(buf))!= -1){
            out.write(buf,0,read);
        }
    }
    
    
    public String sqlEscape(String s){
        if(s==null) return "null";
        return "'"+s.replaceAll("'", "''")+"'";
    }
    
    
    public void disableForeignKeys(PrintStream out) throws IOException {
        // not tested, mysql on default settings doesn't seem to care about foreign keys anyway
        out.print("alter table TOPICTYPE disable keys;\n");
        out.print("alter table DATA disable keys;\n");
        out.print("alter table VARIANTSCOPE disable keys;\n");
        out.print("alter table VARIANT disable keys;\n");
        out.print("alter table MEMBER disable keys;\n");
        out.print("alter table ASSOCIATION disable keys;\n");
        out.print("alter table SUBJECTIDENTIFIER disabl keys;\n");
    }
    
    
    public void enableForeignKeys(PrintStream out) throws IOException {
        // not tested, mysql on default settings doesn't seem to care about foreign keys anyway
        out.print("alter table TOPICTYPE enable keys;\n");
        out.print("alter table DATA enable keys;\n");
        out.print("alter table VARIANTSCOPE enable keys;\n");
        out.print("alter table VARIANT enable keys;\n");
        out.print("alter table MEMBER enable keys;\n");
        out.print("alter table ASSOCIATION enable keys;\n");
        out.print("alter table SUBJECTIDENTIFIER enable keys;\n");
    }
    
    
/*    
    public void writeTopic(String topicid,String baseName,org.wandora.topicmap.Locator subjectLocator,PrintStream out) throws IOException{
        writeTopic(topicid,baseName,(subjectLocator==null)?null:(subjectLocator.toString()),out);
    }
    public void writeTopic(String topicid,String baseName,String subjectLocator,PrintStream out) throws IOException{
        out.print("insert into TOPIC (TOPICID,BASENAME,SUBJECTLOCATOR) values ("+sqlEscape(topicid)+","+sqlEscape(baseName)+","+sqlEscape(subjectLocator)+");\n");
    }
    public void writeTopicType(String topicid,String typeid,PrintStream out) throws IOException {
        out.print("insert into TOPICTYPE (TOPIC,TYPE) values ("+sqlEscape(topicid)+","+sqlEscape(typeid)+");\n");        
    }
    public void writeAssociation(String associationid,String typeid,PrintStream out) throws IOException {
        out.print("insert into ASSOCIATION (ASSOCIATIONID,TYPE) values ("+sqlEscape(associationid)+","+sqlEscape(typeid)+");\n");        
    }
    public void writeMember(String associationid,String playerid,String roleid,PrintStream out) throws IOException {
        out.print("insert into MEMBER (ASSOCIATION,PLAYER,ROLE) values ("+sqlEscape(associationid)+","+sqlEscape(playerid)+","+sqlEscape(roleid)+");\n");        
    }
    public void writeSubjectIdentifier(String topicid,String si,PrintStream out) throws IOException {
        out.print("insert into SUBJECTIDENTIFIER (TOPIC,SI) values ("+sqlEscape(topicid)+","+sqlEscape(si)+");\n");        
    }
    public void writeData(String topicid,String typeid,String versionid,String data,PrintStream out) throws IOException {
        out.print("insert into DATA (TOPIC,TYPE,VERSION,DATA) values ("+sqlEscape(topicid)+","+sqlEscape(typeid)+","+sqlEscape(versionid)+","+sqlEscape(data)+");\n");        
    }
    public void writeVariantScope(String variantid,String topicid,PrintStream out) throws IOException {
        out.print("insert into VARIANTSCOPE (VARIANT,TOPIC) values ("+sqlEscape(variantid)+","+sqlEscape(topicid)+");\n");        
    }
    public void writeVariant(String variantid,String topicid,String value,PrintStream out) throws IOException {
        out.print("insert into VARIANT (VARIANTID,TOPIC,VALUE) values ("+sqlEscape(variantid)+","+sqlEscape(topicid)+","+sqlEscape(value)+");\n");        
    }*/
    
    
    public void closeStream(int count,PrintStream out) throws IOException {
        if(count!=0) out.print(";\n");
    }
    
    
    public void closeStreams(PrintStream[] out) throws IOException {
        closeStream(topicCount,out[I_TOPIC]);
        closeStream(topicTypeCount,out[I_TOPICTYPE]);
        closeStream(associationCount,out[I_ASSOCIATION]);
        closeStream(memberCount,out[I_MEMBER]);
        closeStream(subjectIdentifierCount,out[I_SUBJECTIDENTIFIER]);
        closeStream(dataCount,out[I_DATA]);
        closeStream(variantScopeCount,out[I_VARIANTSCOPE]);
        closeStream(variantCount,out[I_VARIANT]);
    }
    
    
    protected int writeLimit=1;
    
    
    protected int topicCount=0;
    public void writeTopic(String topicid,String baseName,org.wandora.topicmap.Locator subjectLocator,PrintStream[] out) throws IOException{
        writeTopic(topicid, baseName, subjectLocator, out[I_TOPIC]);
    }
    public void writeTopic(String topicid,String baseName,String subjectLocator,PrintStream[] out) throws IOException{
        writeTopic(topicid, baseName, subjectLocator, out[I_TOPIC]);
    }
    public void writeTopic(String topicid,String baseName,org.wandora.topicmap.Locator subjectLocator,PrintStream out) throws IOException{
        writeTopic(topicid,baseName,(subjectLocator==null)?null:(subjectLocator.toString()),out);
    }
    public void writeTopic(String topicid,String baseName,String subjectLocator,PrintStream out) throws IOException{
        if(topicCount==0) out.print("insert into TOPIC (TOPICID,BASENAME,SUBJECTLOCATOR) values ");
        else out.print(",\n");
        out.print("("+sqlEscape(topicid)+","+sqlEscape(baseName)+","+sqlEscape(subjectLocator)+")");
        topicCount++;
        if(topicCount>=writeLimit){
            topicCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int topicTypeCount=0;
    public void writeTopicType(String topicid,String typeid,PrintStream[] out) throws IOException {
        writeTopicType(topicid, typeid, out[I_TOPICTYPE]);
    }
    public void writeTopicType(String topicid,String typeid,PrintStream out) throws IOException {
        if(topicTypeCount==0) out.print("insert into TOPICTYPE (TOPIC,TYPE) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(topicid)+","+sqlEscape(typeid)+")");        
        topicTypeCount++;
        if(topicTypeCount>=writeLimit){
            topicTypeCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int associationCount=0;
    public void writeAssociation(String associationid,String typeid,PrintStream[] out) throws IOException {
        writeAssociation(associationid, typeid, out[I_ASSOCIATION]);
    }
    public void writeAssociation(String associationid,String typeid,PrintStream out) throws IOException {
        if(associationCount==0) out.print("insert into ASSOCIATION (ASSOCIATIONID,TYPE) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(associationid)+","+sqlEscape(typeid)+")");
        associationCount++;
        if(associationCount>=writeLimit){
            associationCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int memberCount=0;
    public void writeMember(String associationid,String playerid,String roleid,PrintStream[] out) throws IOException {
        writeMember(associationid, playerid, roleid, out[I_MEMBER]);
    }
    public void writeMember(String associationid,String playerid,String roleid,PrintStream out) throws IOException {
        if(memberCount==0) out.print("insert into MEMBER (ASSOCIATION,PLAYER,ROLE) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(associationid)+","+sqlEscape(playerid)+","+sqlEscape(roleid)+")");
        memberCount++;
        if(memberCount>=writeLimit){
            memberCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int subjectIdentifierCount=0;
    public void writeSubjectIdentifier(String topicid,String si,PrintStream[] out) throws IOException {
        writeSubjectIdentifier(topicid, si, out[I_SUBJECTIDENTIFIER]);
    }
    public void writeSubjectIdentifier(String topicid,String si,PrintStream out) throws IOException {
        if(subjectIdentifierCount==0) out.print("insert into SUBJECTIDENTIFIER (TOPIC,SI) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(topicid)+","+sqlEscape(si)+")");
        subjectIdentifierCount++;
        if(subjectIdentifierCount>=writeLimit){
            subjectIdentifierCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int dataCount=0;
    public void writeData(String topicid,String typeid,String versionid,String data,PrintStream[] out) throws IOException {
        writeData(topicid, typeid, versionid, data, out[I_DATA]);
    }
    public void writeData(String topicid,String typeid,String versionid,String data,PrintStream out) throws IOException {
        if(dataCount==0) out.print("insert into DATA (TOPIC,TYPE,VERSION,DATA) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(topicid)+","+sqlEscape(typeid)+","+sqlEscape(versionid)+","+sqlEscape(data)+")");
        dataCount++;
        if(dataCount>=writeLimit){
            dataCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int variantScopeCount=0;
    public void writeVariantScope(String variantid,String topicid,PrintStream[] out) throws IOException {
        writeVariantScope(variantid, topicid, out[I_VARIANTSCOPE]);
    }
    public void writeVariantScope(String variantid,String topicid,PrintStream out) throws IOException {
        if(variantScopeCount==0) out.print("insert into VARIANTSCOPE (VARIANT,TOPIC) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(variantid)+","+sqlEscape(topicid)+")");
        variantScopeCount++;
        if(variantScopeCount>=writeLimit){
            variantScopeCount=0;
            out.print(";\n");
        }
    }
    
    
    protected int variantCount=0;
    public void writeVariant(String variantid,String topicid,String value,PrintStream[] out) throws IOException {
        writeVariant(variantid, topicid, value, out[I_VARIANT]);
    }
    public void writeVariant(String variantid,String topicid,String value,PrintStream out) throws IOException {
        if(variantCount==0) out.print("insert into VARIANT (VARIANTID,TOPIC,VALUE) values ");        
        else out.print(",\n");
        out.print("("+sqlEscape(variantid)+","+sqlEscape(topicid)+","+sqlEscape(value)+")");
        variantCount++;
        if(variantCount>=writeLimit){
            variantCount=0;
            out.print(";\n");
        }
    }

    
    
    // -------------------------------------------------------------------------
    
    
    
    public void convertXTM2ToSQL(InputStream in,PrintStream outs,TopicMapLogger logger){
        try{
            
            File[] tempFiles=new File[8];
            for(int i=0;i<tempFiles.length;i++){
                tempFiles[i]=File.createTempFile("wandora_",".sql");
            }
            PrintStream[] out=new PrintStream[tempFiles.length];
            for(int i=0;i<out.length;i++){
                out[i]=new PrintStream(tempFiles[i]);
            }
            
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            XTM2toSQL parserHandler=new XTM2toSQL(logger,out);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);            
            
            reader.parse(new InputSource(in));
            
            closeStreams(out);
            for(int i=0;i<out.length;i++) out[i].close();

            for(int i=0;i<tempFiles.length;i++){
                appendFile(outs,tempFiles[i]);
                tempFiles[i].delete();
            }
            outs.close();
            
        }
        catch(org.xml.sax.SAXParseException se) {
            logger.log("Position "+se.getLineNumber()+":"+se.getColumnNumber(), se);
        }
        catch(org.xml.sax.SAXException saxe) {
            if(! "user_interrupt".equals(saxe.getMessage())) {
                logger.log(saxe);
            }
        }
        catch(Exception e){
            logger.log(e);
        }
        
    }
    
    
    public static void main(String[] args) throws Exception {
        TopicMapLogger logger=new SimpleTopicMapLogger(System.err);
        SQLDumpExport d=new SQLDumpExport();
        d.convertXTM2ToSQL(System.in, System.out, logger);
    }
    
    
    public class XTM2toSQL extends XTMParser2 {
        protected PrintStream[] out;
        public XTM2toSQL(TopicMapLogger logger,PrintStream[] out){
            // the topic map is not used for anything since all process methods have been overridden
            super(new org.wandora.topicmap.memory.TopicMapImpl(),logger);
            this.out=out;
        }
        protected String hrefToID(String s){
            if(s.startsWith("#")) return s.substring(1);
            else logger.log("Don't know how to convert href \""+s+"\" to ID");
            return s;
        }
        public void writeVariant(String topicid,Collection<String> scope,String value,PrintStream[] out) throws IOException {
            String variantid=makeID("V");
            SQLDumpExport.this.writeVariant(variantid,topicid,value,out);
            for(String s : scope){
                writeVariantScope(variantid,hrefToID(s),out);
            }
        }
        @Override
        protected void processTopic(){
            try{
                if(parsedTopic.id==null) parsedTopic.id=makeID("T");
                if(parsedTopic.subjectLocators.size()>1)
                    logger.log("Warning, more than one subject locator found, ignoring all but one");
                String baseName=null;
                String subjectLocator=null;
                if(parsedTopic.subjectLocators.size()>0) subjectLocator=parsedTopic.subjectLocators.get(0).toString();

                for(ParsedName name : parsedTopic.names){
                    if(name.type!=null) {
                        logger.log("Warning, name has type, moving to scope");
                        if(name.scope==null) name.scope=new ArrayList<String>();
                        name.scope.add(name.type);
                    }
                    if(name.value!=null){
                        if(name.scope==null || name.scope.isEmpty()){
                            baseName=name.value;
                        }
                        else {
                            writeVariant(parsedTopic.id,name.scope,name.value,out);
                        }
                    }
                    for(ParsedVariant v : name.variants) {
                        ArrayList<String> s=new ArrayList<String>();
                        if(name.scope!=null) s.addAll(name.scope);
                        if(v.scope!=null) s.addAll(v.scope);
                        writeVariant(parsedTopic.id,s,v.data,out);
                    }
                }
                writeTopic(parsedTopic.id,baseName,subjectLocator,out);

                if(parsedTopic.types!=null){
                    for(String type : parsedTopic.types){
                        writeTopicType(parsedTopic.id,hrefToID(type),out);
                    }
                }
                for(String si : parsedTopic.subjectIdentifiers){
                    writeSubjectIdentifier(parsedTopic.id, si, out);
                }
                if(parsedTopic.itemIdentities.size()>0){
                    logger.log("Warning, ignoring item identities");
                }

                for(ParsedOccurrence o : parsedTopic.occurrences){
                    if(o.type==null){
                        logger.log("Warning, occurrence has no type, skipping.");
                        continue;
                    }
                    if(o.ref!=null){
                        logger.log("Warning, skipping resource ref occurrence");
                        continue;
                    }
                    else if(o.data==null){
                        logger.log("Warning, occurrence has no data, skipping.");
                        continue;
                    }
                    else{
                        if(o.scope==null || o.scope.isEmpty()) {
                            logger.log("Warning, occurrence has no scope, skipping");
                            continue;
                        }
                        if(o.scope.size()>1) logger.log("Warning, variant scope has more than one topic, ignoring all but one.");
                        String version=hrefToID(o.scope.get(0));
                        writeData(parsedTopic.id, hrefToID(o.type), version, o.data, out);
                    }
                }
            }
            catch(IOException ioe){
                logger.log(ioe);
            }
        }
        @Override
        protected void processAssociation(){
            try{
                if(parsedAssociation.type==null) logger.log("No type in association");
                else if(parsedAssociation.roles.isEmpty()) logger.log("No players in association");
                else {
                    String associationid=makeID("A");
                    writeAssociation(associationid,hrefToID(parsedAssociation.type),out);
                    for(ParsedRole r : parsedAssociation.roles){
                        writeMember(associationid,hrefToID(r.topic),hrefToID(r.type),out);
                    }
                }        
            }catch(IOException ioe){
                logger.log(ioe);
            }
        }
        @Override
        protected void postProcessTopicMap(){
            // do nothing
        }
    }
}
