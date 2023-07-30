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
 * ExportSite.java
 *
 * Created on November 5, 2004, 5:20 PM
 */

package org.wandora.application.tools.exporters.site;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.PasswordPrompt;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.GripCollections;
import org.wandora.utils.IObox;
import org.wandora.utils.ImageBox;
import org.wandora.utils.velocity.InstanceMaker;
import org.wandora.utils.velocity.JavaScriptEncoder;
import org.wandora.utils.velocity.TextBox;


/**
 * <p>
 * ExportSite is a tool to export all topics to separate files, 
 * using a velocity template. Normally velocity templates generate HTML
 * resulting interlinked HTML pages.
 * </p>
 * 
 * @author  pasi, ak
 */
public class ExportSite extends AbstractExportTool implements WandoraTool, ActionListener {

	private static final long serialVersionUID = 1L;


	public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;
   
    String templateEncoding = "UTF-8";
    String topicmapfile = "";
    String outputdir = ".";
    String siurl = null;
    String templatefile = "gui/export/sitepage.vhtml";
    String pageindextemplatefile = "gui/export/siteindex.vhtml";
    Wandora wandora = null;
    File currentDirectory = null;
    Object codec = null;
    Locale locale = Locale.getDefault();
    TopicMap topicMap = null;

    boolean resizeImages = false;
    int resizeWidth = 320;
    int resizeHeight = 200;
    int resizeQuality = 75;

    PrintWriter log;
    
    private JButton button;
    private JPanel panel;
    
    private boolean forceStop;
    

    private boolean fetchUrls = true;
    private long napAfterFetch = 100;
    
    private String authUser = null;
    private String authPassword = null;
    boolean forgetAuth = false;
    boolean useScondaryUrlSource = false;
    String secondaryUrlSource = "http://127.0.0.1/wandora/wandora?lang=fi&action=geturl&url=";
    
    String logName = "export.log";
    String filesDirectory = "files";
    
    Topic currentTopic = null;
    
    ExportSiteDialog exportDialog = null;
    
    
    /** Creates a new instance of ExportSite */
    public ExportSite() {
        this(GripCollections.addArrayToMap(new LinkedHashMap(),new Object[]{
                            "pageTemplate","gui/export/sitepage.vhtml",
                            "indexTemplate","gui/export/siteindex.vhtml",
                            "templateEncoding","UTF-8",
                            "fetchUrls","false",
                            "resizeImages","true",
                            "imageWidth","800",
                            "imageHeight","600",
                        }));
    }

    
    public ExportSite(boolean exportSite) {
        this();
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSite;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_site.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    public ExportSite(Map map) {
        if(map != null) {
            for(Iterator i = map.keySet().iterator(); i.hasNext(); ) {
                Object key = i.next();
                Object value = map.get(key);
                if(key instanceof String && value instanceof String && value != null) {
                    String skey = (String) key;
                    String svalue = (String) value;
                    //log(" MAP: " + skey + " == " + svalue);
                    if("pageTemplate".equalsIgnoreCase(skey)) templatefile = svalue;
                    else if("indexTemplate".equalsIgnoreCase(skey)) pageindextemplatefile = svalue;
                    else if("templateEncoding".equalsIgnoreCase(skey)) templateEncoding = svalue;
                    
                    else if("fetchUrls".equalsIgnoreCase(skey)) {
                        try { fetchUrls = Boolean.valueOf(svalue).booleanValue(); }
                        catch (Exception e) {}
                    }
                    else if("resizeImages".equalsIgnoreCase(skey)) {
                        try { resizeImages = Boolean.valueOf(svalue).booleanValue(); }
                        catch (Exception e) {}
                    }
                    else if("imageWidth".equalsIgnoreCase(skey)) {
                        try { resizeWidth = Integer.parseInt(svalue); }
                        catch (Exception e) {}
                    }
                    else if("imageHeight".equalsIgnoreCase(skey)) {
                        try { resizeHeight = Integer.parseInt(svalue); }
                        catch (Exception e) {}
                    }
                    else if("currentDirectory".equalsIgnoreCase(skey)) {
                        try { outputdir =  svalue; }
                        catch (Exception e) { log(e); }
                    }
                }
            }
        }
    }

    
    @Override
    public void execute(Wandora wandora, Context context) {
        forceStop = false;
        this.wandora = wandora;      
        if(wandora!=null) {
            try {
                // --- Solve first topic map to be exported
                if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
                    topicMap = makeTopicMapWith(context);
                }
                else {
                    topicMap = solveContextTopicMap(wandora, context);
                }


                 currentTopic = wandora.getOpenTopic();

                 if(exportDialog == null) {
                     exportDialog = new ExportSiteDialog(wandora, true);
                     exportDialog.setExportDirectory(outputdir);
                     exportDialog.setPageTemplate(this.templatefile);
                     exportDialog.setIndexTemplate(this.pageindextemplatefile);
                     exportDialog.shouldImportSubjectLocators(this.fetchUrls);
                     exportDialog.resizeImages(this.resizeImages);
                     exportDialog.resizeImageWidth(this.resizeWidth);
                     exportDialog.resizeImageHeight(this.resizeHeight);
                 }

                 exportDialog.setVisible(true);
                 
                 if(exportDialog.accept) {
                    panel = new JPanel();
                    panel.setLayout(new FlowLayout());
                    button = new JButton("Stop");
                    button.addActionListener(this);
                    button.setActionCommand("forcestop");
                    button.setPreferredSize(new Dimension(80, 23));
                    panel.add(button);
                    setDefaultLogger();
                    
                    if(exportDialog != null) {
                        outputdir = exportDialog.getExportDirectory();
                        templatefile = exportDialog.getPageTemplate();
                        pageindextemplatefile = exportDialog.getIndexTemplate();
                        fetchUrls = exportDialog.shouldImportSubjectLocators();
                        resizeImages = exportDialog.shouldResizeImages();
                        resizeWidth = exportDialog.resizeImageWidth();
                        resizeHeight = exportDialog.resizeImageHeight();
                    }
                    
                    try { log = new PrintWriter(outputdir + "/" + logName); }
                    catch (Exception e) { log = new PrintWriter(System.out); }
                    
                    work();
                }
            }
            catch (Exception e) {
                log(e);
            }


             /*
             try {
                
                WandoraFileChooser chooser=new WandoraFileChooser();

                chooser.setDialogTitle("Exported site to directory...");
                chooser.setFileSelectionMode(WandoraFileChooser.DIRECTORIES_ONLY);
                chooser.setFileHidingEnabled(true);
                chooser.setDialogType(WandoraFileChooser.CUSTOM_DIALOG);
                chooser.setApproveButtonToolTipText("Export site to directory...");
                
                if(chooser.open(admin, "Export")==WandoraFileChooser.APPROVE_OPTION) {
                    currentDirectory = chooser.getSelectedFile();
                    try { log = new PrintWriter(currentDirectory.getPath() + "/" + logName); }
                    catch (Exception e) { log = new PrintWriter(System.out); }
                    
                    info = new InfoThread(admin);
                    
                    panel = new JPanel();
                    panel.setLayout(new FlowLayout());
                    button = new JButton("Stop");
                    button.addActionListener(this);
                    button.setActionCommand("forcestop");
                    button.setPreferredSize(new Dimension(80, 23));
                    panel.add(button);
                    info = new InfoThread(parent, panel);
                    
                    info.start();
                    while(!info.dialog.isVisible()) {
                        Thread.yield();
                    }
                    // this will probably get executed from the ui thead so better not do anything big in this thread
                    outputdir = chooser.getSelectedFile().getPath();
                    Thread worker = new Thread(this, getName());
                    worker.start();
                }
            }
            catch (Exception e) {
                log(e);
            }
              **/
        }
    }
    
    
    
    
    @Override
    public String getName() {
        return "Export site";
    }
    @Override
    public String getDescription() {
        return "Exports topics as interlinked HTML pages generated with given Velocity template. "+
                "Tool can also download subject locator resources to local files.";
    }
    
    
    public void work() {
        log.println("Export begins...");
        log("Wait while exporting site...");
        generateFilesFromTopicMap(topicMap, templatefile, templateEncoding, pageindextemplatefile, currentTopic, outputdir, locale, codec);        
        log("Export finished. See '" + logName + "' for details!");
        log.println("Export finished");
        log.flush();
        takeNap(2000);
        setState(WAIT);
        log.close();
    }
        
        
           
        
        
    private void takeNap(long napTime) {
        try { Thread.sleep(napTime); }
        catch (Exception e) { }            
    }
    

    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private TopicMap topicMapFromFile(String topicMapFileName) {
        File f  = new File(topicMapFileName);
        try {
            return initTM(f.toURI().toURL().toString());
        } catch (MalformedURLException mue) {
            log.println("topicMapFromFile(): file '"+f.getName()+"' produced MalformedURLException. Topic map not read.");
        }
        return null;
    }
    
    
    
    
    private TopicMap initTM(String uri) {
        TopicMap tm = null;
        try {
            InputStream in=new FileInputStream(uri);
            tm=new org.wandora.topicmap.memory.TopicMapImpl();
            tm.importXTM(in);
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace(log);
        }
        return tm;
    }

    
    
    
    
    private void collectParams(Map<String,Object> localParams, Topic currentTopic, TopicMap topicMap, Locale loc) {
        localParams.put("lang", "en");

        if (currentTopic!=null) {
            localParams.put("topic",currentTopic);
        }
        try { localParams.put("collectionmaker", new InstanceMaker("java.util.HashSet")); } catch (Exception e) {}
        try { localParams.put("listmaker", new InstanceMaker("java.util.ArrayList")); } catch (Exception e) {}
        try { localParams.put("mapmaker", new InstanceMaker("java.util.HashMap")); } catch (Exception e) {}
        try { localParams.put("stackmaker", new InstanceMaker("java.util.Stack")); } catch (Exception e) {}
        localParams.put("urlencoder", org.wandora.utils.velocity.URLEncoder.class);
        localParams.put("javascriptencoder", JavaScriptEncoder.class);
        localParams.put("tmbox", new org.wandora.topicmap.TMBox());
        localParams.put("textbox", new TextBox());
        localParams.put("intparser", Integer.valueOf(0));
//        localParams.put("filter", new com.gripstudios.applications.assembly.AssemblyTopicFilter(new String[] { "http://wandora.org/si/common/picture", "http://wandora.org/si/common/video" }, "http://wandora.org/si/common/unmoderatedpicture" ));
        localParams.put("vhelper", new org.wandora.utils.velocity.VelocityMediaHelper());
        localParams.put("helper", new org.wandora.topicmap.TopicTools());
        localParams.put("topicmap", topicMap);
    }

    
    

    
    
    // Attempts to retrieve all external documents referenced by occurrences of the topic locally
    private void fetchSubjectLocator(Topic t, String directoryToStore, String filesDir, Map<String,String> urlmap)  throws TopicMapException {
        if(t.getSubjectLocator() != null) {
            String mappedName = fetchURL(t.getSubjectLocator().toExternalForm(), directoryToStore, filesDir);
            if (mappedName!=null) {
                urlmap.put(t.getSubjectLocator().toExternalForm(), mappedName);
            }
        }
    }
    
    
    
    
    // Fetch url and store it locally unless fetchUrl is set false!
    private String fetchURL(String surl, String directoryStore, String filesDir) {
        if(fetchUrls == false) return surl;
        
        String localFileName = null;
        String mappedFileName = null;
        URL url = null;
        String fileName;
        
        log("Fetching external file\n" + surl);
        log.println("Fetching external file " + surl);
        if (surl.lastIndexOf("/") != -1) fileName = surl.substring(surl.lastIndexOf("/"));
        else fileName = surl;
        localFileName = directoryStore + File.separatorChar + filesDir + File.separatorChar + fileName;
        File f = new File(localFileName);
        File d = f.getParentFile();
        if (d.mkdirs()) { log.println("Created directory '"+d.getAbsolutePath()+"' for file retrieved from url '"+surl+"'!"); }
        try {
            IObox.moveUrl(new URL(surl), f, authUser, authPassword, false);
        }
        catch (Exception e) {
            log(e);
            
            if(IObox.isAuthException(e)) {
                log.println("Authentication required to fecth url " + f.getPath());
                boolean success = false;
                PasswordPrompt pp = new PasswordPrompt((Frame) wandora, true);
                pp.setTitle(surl);
                while(!success && !forgetAuth) {
                    log.println("Consulting user!");
                    pp.setVisible(true);
                    if(!pp.wasCancelled()) {
                        authUser = pp.getUsername();
                        authPassword = new String(pp.getPassword());
                        try {
                            success = true;
                            IObox.moveUrl(new URL(surl), f, authUser, authPassword, false);
                        }
                        catch (Exception e1) {
                            if(IObox.isAuthException(e1)) success = false;
                        }
                    }
                    else {
                        forgetAuth = true;
                        authUser = null;
                        authPassword = null;
                    }
                }
            }
            else {
                if(useScondaryUrlSource) {
                    try {
                        IObox.moveUrl(new URL(secondaryUrlSource + surl), f);
                    }
                    catch (Exception e1) {
                        e1.printStackTrace(log);
                    }
                }
                else e.printStackTrace(log);
            }
        }
        if(resizeImages) {
            log("Trying to resize image\n" + f.getPath());
            log.println("Trying to resize image " + f.getPath());
            try { ImageBox.makeThumbnail(f.getPath(), f.getPath(), resizeWidth, resizeHeight, resizeQuality); }
            catch (Exception e) {  e.printStackTrace(log); }
        }

        mappedFileName = "file://" + f.getPath();
        takeNap(napAfterFetch);
        return mappedFileName;
    }
    
    

    
    
    
    
    public void generateFilesFromTopicMap(Object topicmap, String templatefilename, String templateEncoding, String indextemplatefilename, Topic currentTopic, String outputdir, Locale loc, Object codec) {
        forgetAuth = false;
        authUser = null;
        authPassword = null;
        TopicMap tm = null;
        
        Comparator c = java.text.Collator.getInstance(loc); 
        Map index = new TreeMap(c);
        if (topicmap instanceof TopicMap) {
            tm = (TopicMap) topicmap;
        } else if (topicmap instanceof String) {
            tm = topicMapFromFile((String)topicmap);
        }
        Map<String,String> globalUrlMap = new HashMap<>();
        VelocityContext context = null;
        Template template = null;
        //Template ntemplate = null;
        Template itemplate = null;
        FileWriter writer = null;
        File templateFile = null;
        //File nameTemplateFile;
        File indexTemplateFile = null;
        String templatePath = null;
        VelocityEngine velocityEngine = null;
        Map<String,Object> localParams;
        try {
            templateFile = new File(templatefilename);

            templatePath = templateFile.getParent();
            velocityEngine = new VelocityEngine();
            velocityEngine.setProperty("file.resource.loader.path", templatePath );
            //velocityEngine.setProperty("runtime.log.error.stacktrace", "false" );
            //velocityEngine.setProperty("runtime.log.warn.stacktrace", "false" );
            //velocityEngine.setProperty("runtime.log.info.stacktrace", "false" );
            velocityEngine.init();
            if(templateEncoding != null && templateEncoding.length()>0) template = velocityEngine.getTemplate(templateFile.getName(), templateEncoding);
            else template = velocityEngine.getTemplate(templateFile.getName());
            /*
            if (nametemplatefilename != null) {
                nameTemplateFile = new File(nametemplatefilename);
                if (templateEncoding != null && templateEncoding.length()>0) ntemplate = velocityEngine.getTemplate(nameTemplateFile.getName(), templateEncoding);
                else ntemplate = velocityEngine.getTemplate(nameTemplateFile.getName());
            }
            */
            if (indextemplatefilename != null) {
                indexTemplateFile = new File(indextemplatefilename);
                if (templateEncoding != null && templateEncoding.length()>0) itemplate = velocityEngine.getTemplate(indexTemplateFile.getName(), templateEncoding);
                else itemplate = velocityEngine.getTemplate(indexTemplateFile.getName());
            }
            
            // --- Iterate topics ---
            for (java.util.Iterator<Topic> ti = tm.getTopics(); ti.hasNext() && !forceStop; ) {
                Topic t = (Topic)ti.next();
                if(t == null || t.isRemoved()) continue;
                
                boolean isIndexFile = false;
                log("Exporting topic '" + getTopicName(t) + "'.");
                
                Map<String,String> urlMap = new LinkedHashMap<>();
                fetchSubjectLocator(t, outputdir, filesDirectory, urlMap);
                localParams = new HashMap<>();
                localParams.put("urlMap", urlMap);
                globalUrlMap.putAll(urlMap);
                collectParams( localParams, t, tm, loc);
                context = new VelocityContext();
                for (Iterator<String> hit = localParams.keySet().iterator(); hit.hasNext(); ) {
                    Object key = (String) hit.next();
                    if (key instanceof String && key != null) {
                        Object value = localParams.get(key);
                        context.put((String)key, value);
                    }
                }
                if(codec != null) context.put("codec", codec);
                if(outputdir != null) context.put("outputdir", ""); // for generating files in proper directories
                String outputfilename = null;
                String fulloutputfilename = null;
                outputfilename = (t.getID().hashCode()) + ".html";
                webPageIndexBuild(index, t, outputfilename, loc);
                fulloutputfilename = outputdir + File.separatorChar + outputfilename;
                writer = new FileWriter(fulloutputfilename);
                template.merge( context, writer );
                writer.flush();
                writer.close();
                log.println("Generated output file '"+fulloutputfilename+"' for '"+getTopicName(t)+"'.");
                
                // --- create starthere.html ---
                if(t != null && currentTopic != null && t.mergesWithTopic(currentTopic)) {
                    try {
                        File f = new File(fulloutputfilename);
                        File indexfile = new File(f.getParent()+File.separatorChar+"starthere.html");
                        FileOutputStream fos = new FileOutputStream(indexfile);
                        indexfile.createNewFile();
                        FileInputStream fis = new FileInputStream(f);
                        int b = -1;
                        while ((b = fis.read()) != -1) {
                            fos.write(b);
                        }
                        fos.close();
                        fis.close();
                        log.println("Created "+indexfile.getCanonicalPath()+" file as a copy of "+fulloutputfilename);
                    } 
                    catch (Exception e) {
                        log.println("Caught exception '"+e.getMessage()+"' while creating starthere.html file.");
                        e.printStackTrace(log);
                    }
                }
                log.flush();
            }
            
            // --- time to output master index file ---
            log.println("Creating index.html");
            localParams = new HashMap<>();
            localParams.put("urlMap", globalUrlMap);
            collectParams(localParams, null, tm, loc);
            context = new VelocityContext();
            context.put("index", index);
            log("Exporting index for the site!");
            for (Iterator<String> hit = localParams.keySet().iterator(); hit.hasNext(); ) {
                Object key = (String) hit.next();
                if (key instanceof String && key != null) {
                    Object value = localParams.get(key);
                    context.put((String)key, value);
                }
            }
            writer = new FileWriter(outputdir + File.separatorChar + "index.html");
            
            itemplate.merge( context, writer );
            writer.flush();
            writer.close();
            log.println("Generated page index file '"+outputdir + File.separatorChar + "index.html'");
            
        }
        catch( ResourceNotFoundException rnfe ) {
            // couldn't find the template
            log("Unable to find specified template file for velocity export!");
            log.println("Unable to find specified template file for velocity export!");
            rnfe.printStackTrace(log);
        }
        catch( ParseErrorException pee ) {
            // syntax error : problem parsing the template
            log("Unable to parse the velocity template file! Parse exp:"+pee.getLocalizedMessage());
            log.println("Unable to parse the velocity template file! Parse exp:"+pee.getLocalizedMessage());
            pee.printStackTrace(log);
            
        }
        catch( MethodInvocationException mie ) {
            // something invoked in the template threw an exception
            log("Velocity template causes method invocation exception!");
            log.println("Velocity template causes method invocation exception!");
            mie.printStackTrace(log);
        }
        catch( Exception e ) {
            log("Exception '" + e.toString() + "' occurred while exporting site!");
            log.println("Exception '" + e.toString() + "' occurred while exporting site!");        
            e.printStackTrace(log);
        }
        finally {
            if(writer != null) {
                try {
                    writer.flush();
                    writer.close();
                }
                catch (Exception e2) {}
            }
        }
     }
    
    
    
    
    
     private void webPageIndexBuild(Map index, Topic t, String webfilename, Locale loc)  throws TopicMapException {
        String dname = t.getBaseName();
        if (dname!=null) index.put(dname, webfilename);
     }

     

    @Override
     public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
         String actionCommand = actionEvent.getActionCommand();
         if("forcestop".equals(actionCommand)) {
             forceStop = true;
             log("Wait while stopping...\nThis may take few minutes depending on the current work phase!");
         }
     }     

}
