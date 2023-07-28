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
 * PrintTopic.java
 *
 * Created on October 7, 2004, 5:56 PM
 */

package org.wandora.application.tools;


import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.print.attribute.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.text.*;
import javax.swing.*;
import javax.swing.text.html.*;

import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.*;



/**
 *
 * @author  akivela
 */
public class PrintTopic extends AbstractWandoraTool implements ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;
	
	private static final int NO_SORT = 0;
    private static final int SORT_DESC = -1;
    private static final int SORT = 1;
    public static final String OPTIONS_PREFIX = "printTopic.";
    
    JFrame frame;
    JPanel toolPanel;
    SimpleButton cancelBtn;
    SimpleButton printBtn;
    SimpleButton copyBtn;
    SimpleButton saveBtn;
    SimpleComboBox templateSelector;
    SimpleComboBox pageSelector;
    JPanel previewPanel;
    Preview preview;
    JScrollPane scrollArea;
    Wandora parent;
    JPanel framePanel;
    
    Topic printTopic = null;
    HashMap<String, String> templates = new HashMap<String, String>();
    
    PrinterJob printJob = null;

    

    
    
    /** Creates a new instance of PrintTopic */
    public PrintTopic() {
    }
    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/print.png");
    }
    
    @Override
    public String getName() {
        return "Print topic";
    }

    @Override
    public String getDescription() {
        return "Print current topic.";
    } 
    public boolean useDefaultGui() {
        return false;
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        this.parent = admin;
        if(frame == null) {
            initializeGui();
        }

        if(context.getContextObjects().hasNext()) {
            printTopic = (Topic) context.getContextObjects().next();
        }
        else {
            printTopic = admin.getOpenTopic();
        }
        
        String previewTitle = printTopic == null ? "No topic selected!" : (printTopic.getBaseName() == null ? printTopic.getOneSubjectIdentifier().toExternalForm() : printTopic.getBaseName());
        if(previewTitle.length() > 60) previewTitle = previewTitle.substring(0, 59) + "...";
        
        String currentTemplate = parent.options.get(OPTIONS_PREFIX+"currentTemplate");
        if(currentTemplate != null && currentTemplate.length() > 0) {
            templateSelector.setSelectedItem(currentTemplate);
        }
        
        updatePreview();

        frame.pack();
        PageFormat pageFormat = printJob.defaultPage();
        frame.setSize(new Dimension((int) pageFormat.getWidth(), (int) pageFormat.getHeight()+toolPanel.getHeight()));

        parent.centerWindow(frame);
        int x = frame.getX();
        x = x > 0 ? x : 0;
        int y = frame.getY();
        y = y > 0 ? y : 0;
        frame.setLocation(x,y);
        frame.setVisible(true);
    }
    
    
    
    
  
    private void initializeGui() {
        frame = new JFrame();
        frame.setResizable(true);
        framePanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        framePanel.setLayout(gridbag);
        
        cancelBtn = new SimpleButton("Cancel");
        printBtn = new SimpleButton("Print");
        copyBtn = new SimpleButton("Copy");
        saveBtn = new SimpleButton("Save");
        templateSelector = new SimpleComboBox();
        pageSelector = new SimpleComboBox();
        pageSelector.setEditable(false);
        pageSelector.setPreferredSize(new Dimension(50,21));
              
        cancelBtn.setPreferredSize(new Dimension(70,23));
        printBtn.setPreferredSize(new Dimension(70,23));
        copyBtn.setPreferredSize(new Dimension(70,23));
        saveBtn.setPreferredSize(new Dimension(70,23));
        
        cancelBtn.setMargin(new Insets(2,2,2,2));
        printBtn.setMargin(new Insets(2,2,2,2));
        copyBtn.setMargin(new Insets(2,2,2,2));
        saveBtn.setMargin(new Insets(2,2,2,2));
        
        templateSelector.setPreferredSize(new Dimension(150,21));
        templateSelector.setEditable(false);

        cancelBtn.setToolTipText("Cancel printing and close print preview");
        printBtn.setToolTipText("Print");
        copyBtn.setToolTipText("Copy HTML source to clipboard");
        saveBtn.setToolTipText("Save as HTML source");
        
        templateSelector.setToolTipText("Select print template");
        pageSelector.setToolTipText("Change preview page");
        
        solveTemplates();
        templateSelector.setOptions(templates.keySet());
        
        toolPanel = new JPanel(new BorderLayout());
        JPanel p1 = new JPanel(new FlowLayout());
        p1.add(templateSelector);
        p1.add(pageSelector);
        
        JPanel p2 = new JPanel(new FlowLayout());
        p2.add(copyBtn);
        p2.add(saveBtn);
        p2.add(printBtn);
        p2.add(cancelBtn);
        toolPanel.add(p1, BorderLayout.WEST);
        toolPanel.add(p2, BorderLayout.EAST);

        printJob = PrinterJob.getPrinterJob();
        PrintRequestAttributeSet printAttrs = new HashPrintRequestAttributeSet();
        previewPanel = new JPanel(new BorderLayout());
        //previewPanel.setPreferredSize(new Dimension((int) pageFormat.getWidth(), (int) pageFormat.getHeight()));
        preview = new Preview("", printJob.getPageFormat(printAttrs));
        scrollArea = new JScrollPane(preview, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        previewPanel.add(scrollArea, BorderLayout.CENTER);
        
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(previewPanel, c);
        framePanel.add(previewPanel);

        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        gridbag.setConstraints(toolPanel, c);
        framePanel.add(toolPanel);
        
        frame.add(framePanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        frame.setTitle("Print topic");
        //frame.setIconImage(UIBox.getImage("gui/appicon/48x48_24bit.png"));
        frame.setIconImage(UIBox.getImage("gui/appicon/icon.gif"));
        
        printBtn.addActionListener(this);
        cancelBtn.addActionListener(this);
        copyBtn.addActionListener(this);
        saveBtn.addActionListener(this);
        templateSelector.addActionListener(this);
        pageSelector.addActionListener(this);
        
        frame.addKeyListener(this);
        preview.addKeyListener(this);
    }
    
    
    
    
     // ---------------------------------------------------- Action Listener ---
    
    
    
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        Object evtSource = evt.getSource();
        if(evtSource == null) return;
        
        
        if(evtSource.equals(copyBtn)) {
            ClipboardBox.setClipboard(getTopicAsText(parent));
        }
        
        else if(evtSource.equals(cancelBtn)) {
            frame.setVisible(false);
        }
        
        else if(evtSource.equals(printBtn)) {
            try {
                if( preview.print() ) {
                    frame.setVisible(false);
                }
            }
            catch(Exception e) {
                log(e);
            }
            /*
            if(printJob != null) {
                printJob.setPrintable(preview);
                PrintRequestAttributeSet printAttrs = new HashPrintRequestAttributeSet();
                //printAttrs.add( new PageRanges(1,2) );
                //printAttrs.add( PrintQuality.HIGH );
                //printAttrs.add( new PrinterResolution(600,600, PrinterResolution.DPI ) );
                if(printJob.printDialog()) {
                    try {
                        preview.setPageFormat(printJob.getPageFormat(printAttrs));
                        printJob.print();
                    }
                    catch(PrinterException pe) {
                        log("Error printing: " + pe);
                    }
                }
            }
            frame.setVisible(false);
             * */
        }
        
        else if(evtSource.equals(saveBtn)) {
            if(preview != null) preview.save();
        }
        
        else if(evtSource.equals(templateSelector)) {
            //System.out.println("----------------------- Updating preview!");
            parent.options.put(OPTIONS_PREFIX+"currentTemplate", templateSelector.getSelectedItem().toString());
            updatePreview();
        }
        
        else if(evtSource.equals(pageSelector)) {
            //System.out.println("----------------------- Page change!");
            String pageString = (String) pageSelector.getSelectedItem();
            if(pageString != null && pageString.length() > 0) {
                try {
                    int page = Integer.parseInt(pageString);
                    preview.setPage(page-1);
                    preview.invalidate();
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        
        else {
            System.out.println("Unknown action event source: " +evtSource);
        }
    }
    
    
    // -------------------------------------------------------- Key Listener ---
    
    
    
    
    protected Object[] keyMap = new Object[] {
        KeyEvent.VK_1, Integer.valueOf(0),
        KeyEvent.VK_2, Integer.valueOf(1),
        KeyEvent.VK_3, Integer.valueOf(2),
        KeyEvent.VK_4, Integer.valueOf(3),
        KeyEvent.VK_5, Integer.valueOf(4),
        KeyEvent.VK_6, Integer.valueOf(5),
        KeyEvent.VK_7, Integer.valueOf(6),
        KeyEvent.VK_8, Integer.valueOf(7),
        KeyEvent.VK_9, Integer.valueOf(8),
    };
    
    public void keyTyped(KeyEvent e) {
        //System.out.println("keyTyped "+e);
    }
    public void keyPressed(KeyEvent e) {
        //System.out.println("keyPressed "+e);
    }
    public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased "+e);
        if(e == null) return;
        for(int i=0; i<keyMap.length; i+=2) {
            if(keyMap[i].equals(e.getKeyCode())) {
                int pn = ((Integer) keyMap[i+1]).intValue();
                if(pn < preview.numberOfPages) {
                    //preview.setPage(pn);
                    //System.out.println("changing page to "+pn);
                    pageSelector.setSelectedIndex(pn);
                }
                break;
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        frame.setVisible(false);
    }

    
    public String getTemplateFile() {
        String current = (String) templateSelector.getItemAt(templateSelector.getSelectedIndex());
        return templates.get(current);
    }
 
    
    
    
    public void updatePreview() {
        setPreview(printTopic, parent);
    }
    
    
    public void setPreview(Topic t, Wandora admin) {
         setPreview(t, getTemplateFile(), admin);
    }
    
    
    public void setPreview(Topic t, String template, Wandora admin) {
        //System.out.println("setPreview()");
        try {
            String text = getTopicAsText(admin, t, template, SORT);
            if(preview != null) { 
                preview.setText(text);
                preview.invalidate();
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
 
    
    
    public void updatePageSelector() {
        //System.out.println("updatePageSelector()");
        String[] pageNumbers = preview.getPageNumbers();
        pageSelector.setOptions(pageNumbers);
        if(pageNumbers.length > 0) {
            pageSelector.setSelectedIndex(0);
            preview.setPage(0);
        }

        pageSelector.invalidate();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public String getTopicAsText(Wandora admin) {
        return getTopicAsText(admin, printTopic, getTemplateFile(), SORT);
    }
    
    public String getTopicAsText(Wandora wandora, Topic t, String templateFile, int sortFlag) {
        Map<String, Object> hash = new LinkedHashMap<>();

        try {
            if(wandora!=null) {
                if(t == null) {
                    t = wandora.getOpenTopic();
                }
                if(t == null) {
                    hash.put("error", "*** Can't find topic to print. ***");
                    return "Can't find topic to print.";
                }
                               
                //log("getTopicAsTextLines: " + t.getBaseName());
                TopicMap topicMap = wandora.getTopicMap();
                List<String> temp = new ArrayList<>();
                
                
                hash.put("topicmap", topicMap);
                hash.put("topic", t);
                
                
                // --- base name
                String baseName = getTopicName(t);
                hash.put("basename", baseName);
                
                
                // --- subject locator
                if(t.getSubjectLocator() != null) {
                    Locator newSubjectLocator = t.getSubjectLocator();
                    hash.put("sl", newSubjectLocator.toExternalForm());
                }
                
                
                // --- names
                if(!t.getVariantScopes().isEmpty()) {
                    Map<List<String>,String> names = new LinkedHashMap<>();
                    for(Set<Topic> scope : t.getVariantScopes()){
                        List<String> nameScope = new ArrayList<>();
                        for(Topic t2 : scope) {
                            nameScope.add(getTopicName(t2));
                        }
                        names.put(nameScope, t.getVariant(scope));
                    }
                    if(!names.isEmpty()) {
                        hash.put("names", names);
                    }
                }
                
                
                // --- classes
                temp = new ArrayList<>();
                for(Topic t2 : t.getTypes()) {
                    temp.add(getTopicName(t2));
                }
                if(!temp.isEmpty()) {
                    hash.put("classes", sortArray(temp, sortFlag));
                }

                
                
                // --- occurrence
                Map<String,Map<String,String>> occurrences = new LinkedHashMap<>();
                Map<String,String> occurrence = new LinkedHashMap<>();
                for(Topic t2 : t.getDataTypes()) {
                    occurrence = new LinkedHashMap<>();
                    Hashtable<Topic,String> data = t.getData(t2);
                    for(Enumeration<Topic> en = data.keys(); en.hasMoreElements(); ) {
                        Topic key = (Topic) en.nextElement();
                        occurrence.put(getTopicName(key), data.get(key));
                    }
                    occurrences.put(getTopicName(t2), occurrence);
                }
                if(!temp.isEmpty()) {
                    hash.put("data", occurrences);
                }
               
                

                
                // --- associations
                Map<String,List<Map<String,String>>> associationsByType = new LinkedHashMap<>();
                Map<String,Map<String,String>> rolesByType = new HashMap<>();
                List<Map<String,String>> allAssociations = new ArrayList<>();
                
                for(Association a : t.getAssociations()) {
                    Topic type = a.getType();
                    String typeName = getTopicName(type);
                    List<Map<String,String>> typedAssociations = associationsByType.get(typeName);
                    if(typedAssociations == null) typedAssociations = new ArrayList<>();

                    Collection<Topic> aRoles = a.getRoles();
                    Map<String,String> roles = rolesByType.get(typeName);
                    if(roles == null) roles = new LinkedHashMap<>();
                    Map<String,String> association = new LinkedHashMap<>();

                    for(Iterator<Topic> aRoleIter = aRoles.iterator(); aRoleIter.hasNext(); ) {
                        Topic role = (Topic) aRoleIter.next();
                        Topic player = a.getPlayer(role);
                        String roleName = getTopicName(role);
                        String playerName = getTopicName(player);
                        association.put(roleName, playerName);
                        roles.put(roleName, roleName); // <-- IS THIS RIGHT!
                    }
                    allAssociations.add(association);
                    typedAssociations.add(association);
                    rolesByType.put(typeName, roles);
                    associationsByType.put(typeName, typedAssociations);                    
                }
                if(!associationsByType.isEmpty()) {
                    hash.put("associationsbytype", associationsByType);
                    hash.put("associationrolesbytype", rolesByType);
                    hash.put("allassociations", allAssociations);
                }

                // --- subject identifiers
                temp = new ArrayList<>();
                for(Locator l : t.getSubjectIdentifiers() ) {
                    temp.add(l.toExternalForm());
                }
                hash.put("si", temp);
                
                                
                // --- instances
                temp = new ArrayList<>();
                for(Topic t2 : topicMap.getTopicsOfType(t)) {
                    temp.add(getTopicName(t2));
                }
                if(!temp.isEmpty()) {
                    List<String> sorted = sortArray(temp, sortFlag);
                    hash.put("instances", sorted);
                }
            }
        }
        catch(Exception e) {
            hash.put("error", "*** Exception occurred while building print! ***");
            log(e);
        }
        
        return getVelocityString(wandora, hash, templateFile);
    }

    
    /*
     * Use method in AbstractWandoraTool instead!
     * 
    public String getTopicName(Topic t) {
        try {
            if(t.getBaseName() == null) {
                return t.getOneSubjectIdentifier().toExternalForm();
            }
            else {
                return t.getBaseName();
            }
        }
        catch(Exception e) {
            log(e);
            return "";
        }
    }
    */
    
    
    
    public String getVelocityString(Wandora wandora, Map hash, String templateFile) {
        VelocityContext context;
        Template template;
        StringWriter writer;
        VelocityEngine velocityEngine;
        String templateEncoding = "UTF-8";
        Object codec = null;

        try {           
            //templateFile = new File(templateName);
            //templatePath = templateFile.getParent();
            writer = new StringWriter();
            velocityEngine = new VelocityEngine();
            //velocityEngine.setProperty("file.resource.loader.path", templatePath );
            //velocityEngine.setProperty("runtime.log.error.stacktrace", "false" );
            //velocityEngine.setProperty("runtime.log.warn.stacktrace", "false" );
            //velocityEngine.setProperty("runtime.log.info.stacktrace", "false" );
            velocityEngine.init();
            context = new VelocityContext();
            context.put("topic", hash);
            context.put("date" , DateFormat.getDateTimeInstance().format(new Date()));
            if(codec != null) context.put("codec", codec);
            if(templateEncoding != null && templateEncoding.length()>0) template = velocityEngine.getTemplate(templateFile, templateEncoding);
            else template = velocityEngine.getTemplate(templateFile);
            template.merge( context, writer );
            writer.flush();
            writer.close();
            String text = writer.toString();
            //log("Processed Velocity text follows:\n" + text);
            return text;
        }
        catch( ResourceNotFoundException rnfe ) {
            // couldn't find the template
            log("Unable to find the template file '" + templateFile + "' for velocity!", rnfe);
        }
        catch( ParseErrorException pee ) {
            // syntax error : problem parsing the template
            log("Unable to parse the velocity template file '" + templateFile + "'!");
        }
        catch( MethodInvocationException mie ) {
            // something invoked in the template threw an exception
            log("Velocity template '" + templateFile + "' causes method invocation exception!", mie);
        }
        catch( Exception e ) {
            log("Exception '" + e.toString() + "' occurred while working with velocity template '" + templateFile + "'!", e);        
        }
        return "";
    }
    
    
    

    public <T> List<T> sortArray(List<T> v, int sortFlag) {
        if(sortFlag == NO_SORT) return v;
        if(v == null) return null;
        if(v.size() == 1) return v;

        T[] array = (T[]) v.toArray(new Object[0]);
        T o1;
        T o2;
        String s1;
        String s2;
        for(int i=array.length-1; i>0; i--) {
            for(int j=0; j<i ; j++) {
                o1 = array[i];
                o2 = array[j];
                if(o1 instanceof String && o2 instanceof String) {
                    s1 = (String) o1;
                    s2 = (String) o2;
                    if((sortFlag == SORT && s1.compareToIgnoreCase(s2) < 0) ||
                       (sortFlag == SORT_DESC && s1.compareToIgnoreCase(s2) > 0)) {
                            array[j] = o1;
                            array[i] = o2;
                    }
                }
            }
        }

        v = new ArrayList<>();
        v.addAll(Arrays.asList(array));
        return v;
    }
    
    
    
    
    public void solveTemplates() {
        try {
            HashSet<String> templateFiles = IObox.getFilesAsHash("resources/gui/printtemplates", ".+\\.vhtml", 1, 999);
            for(String templateFilename : templateFiles) {
                if(templateFilename != null) {
                    try {
                        //System.out.println("found template: " + templateFilename );
                        boolean nameFound = false;
                        BufferedReader reader = new BufferedReader(new FileReader(templateFilename));
                        String firstLine = reader.readLine();
                        if(firstLine != null) {
                            if(firstLine.startsWith("##TEMPLATE-NAME:")) {
                                String name = firstLine.substring(16);
                                name = name.trim();
                                if(name != null && name.length() > 0) {
                                    nameFound = true;
                                    templates.put(name, templateFilename);
                                }
                            }
                        }
                        reader.close();
                        if(nameFound == false) {
                            templates.put(templateFilename, templateFilename);
                        }
                    }
                    catch(Exception ex) {
                        log(ex);
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // ---- Preview ------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    class Preview extends JEditorPane implements Printable,  MouseListener, ActionListener {

		private static final long serialVersionUID = 1L;
		
		int numberOfPages = 0;
        int page = 0;
        protected JPopupMenu popup;
        protected Object[] popupStruct = new Object[] {
            "Copy", UIBox.getIcon("gui/icons/copy.png"),
            "Copy as image", UIBox.getIcon("gui/icons/copy.png"),
            "---",
            "Save...", UIBox.getIcon("gui/icons/file_save.png"),
            "---",
            "Print...", UIBox.getIcon("gui/icons/print.png"),
            "---",
            "Close", UIBox.getIcon("gui/icons/exit.png"),
        };
        private PageFormat pageFormat;
        
        
        
        
        
        Preview(String text, PageFormat page) {
            super("text/html", text);
            //this.putClientProperty("JEditorPane.w3cLengthUnits", Boolean.TRUE);
            pageFormat = page;
            this.addMouseListener(this);
            this.setText(text);
            this.setEditable(false);
            popup = UIBox.makePopupMenu(popupStruct, this);
            setComponentPopupMenu(popup);
        }
        
        
        /*
        public Dimension getPreferredSize() {
            if(pageFormat != null) {
                return new Dimension((int) pageFormat.getWidth(), (int) pageFormat.getHeight());
            }
            else {
                return new Dimension((int) (72*8.27), (int) (72*11.69));
            }
        }
         * */
        
        
        @Override
        public void setText(String newText) {
            setEditorKit(new HTMLEditorKit() );
            super.setText(newText);
            setCaretPosition(0);
            repaint();
        }
        
              
        public void setPage(int p) {
            page = p;
            repaint();
        }
        
        public String[] getPageNumbers() {
            String[] pageNumbers = new String[getPageCount()];
            for(int i=0; i<pageNumbers.length; i++) {
                pageNumbers[i] = ""+(i+1);
            }
            return pageNumbers;
        }
        
        
        public void setPageFormat(PageFormat page) {
            pageFormat = page;
            paint(this.getGraphics());
        }
        

        public int getPageCount() {
            return getPageCount(this.getGraphics(), pageFormat);
        }
        public int getPageCount(Graphics g, java.awt.print.PageFormat pageFormat) {
            int pageCount = (int) Math.ceil(this.getHeight() / pageFormat.getImageableHeight());
            return pageCount;
        }
        
               
                
        public int print(java.awt.Graphics g, java.awt.print.PageFormat pageFormat, int p) throws java.awt.print.PrinterException {
            if(p < getPageCount(g, pageFormat)) {
                //System.out.println("Printing page " + p);
                page = p;
                Graphics2D g2d = (Graphics2D)g;
                RepaintManager currentManager = RepaintManager.currentManager(this);    
                currentManager.setDoubleBufferingEnabled(false);
                paint(g2d);
                currentManager.setDoubleBufferingEnabled(true);
                return(PAGE_EXISTS);
            }
            else {
                return(NO_SUCH_PAGE);
            }
        }
        
        
        @Override
        public void paint(Graphics g) {
            int pc = getPageCount(g, pageFormat);
            if(pc != numberOfPages) {
                updatePageSelector();
                page = 0;
                numberOfPages = pc;
            }
            double iHeight = pageFormat.getImageableHeight();
            double iWidth = pageFormat.getImageableWidth();
            double iX = pageFormat.getImageableX();
            double iY = pageFormat.getImageableY();
            
            g.setColor(Color.WHITE);
            g.clipRect(0, 0, this.getWidth(), this.getHeight());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(new Color(245,245,245));
            g.drawRect((int) iX-1, (int) iY-1, (int) iWidth+1, (int) iHeight+1);
            
            if(g instanceof Graphics2D) {
                RenderingHints qualityHints = new RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
                RenderingHints antialiasHints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                RenderingHints metricsHints = new RenderingHints(
                    RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

                ((Graphics2D) g).addRenderingHints(qualityHints);
                ((Graphics2D) g).addRenderingHints(antialiasHints);
                ((Graphics2D) g).addRenderingHints(metricsHints);
                
                ((Graphics2D) g).translate(pageFormat.getImageableX(), pageFormat.getImageableY()-(page*iHeight));
                ((Graphics2D) g).clipRect(0, (int) (page*iHeight), (int) iWidth, (int) (iHeight));
                //System.out.println("printing=="+(page*iHeight)+"-"+((page+1)*iHeight));
                //System.out.println("((page+1)*iHeight)=="+((page+1)*iHeight));
                //g.setColor(Color.WHITE);
                //g.fillRect(0, (int) (page*iHeight), (int) iWidth, (int) iHeight);
            }
            
            super.paint(g);
        }
        
        // --------------------------------------------------------------- MOUSE ---



        public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        }

        
    
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            //System.out.println("actionPerformed at Preview");
            String c = actionEvent.getActionCommand();
            if(c.equals("Copy")) {
                ClipboardBox.setClipboard(getTopicAsText(parent));
            }
            if(c.equals("Copy as image")) {
                BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                this.print(image.getGraphics());
                ClipboardBox.setClipboard(image);
            }
            else if(c.startsWith("Save")) {
                save();
            }
            else if(c.startsWith("Print")) {
                try{
                    print();
                }catch(java.awt.print.PrinterException pe){
                    pe.printStackTrace();
                }
            }
            else if(c.startsWith("Close")) {
                frame.setVisible(false);
            }
        }
        

        public void save() {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle("Save text file");
            if(chooser.open(parent, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                save(chooser.getSelectedFile());
            }
        }



        public void save(File textFile) {
            if(textFile != null) {
                String newText = "";
                try {
                    FileWriter writer=new FileWriter(textFile);
                    write(writer);
                    writer.close();
                    //IObox.saveFile(textFile, textPane.getText());
                }
                catch(Exception e) {
                    System.out.println("Exception '" + e.toString() + "' occurred while saving file '" + textFile.getPath() + "'.");
                }
            }
        }


        
    }

}
