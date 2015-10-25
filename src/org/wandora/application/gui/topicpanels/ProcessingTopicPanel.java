/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://www.wandora.org/
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * ProcessingTopicPanel.java
 *
 * Created on 2.9.2011, 14:11:43
 */




package org.wandora.application.gui.topicpanels;


import java.awt.event.KeyEvent;
import org.wandora.application.gui.WandoraOptionPane;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.wandora.application.CancelledException;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.topicmap.TopicMap;
import processing.core.*;

//import jsyntaxpane.DefaultSyntaxKit;
import de.sciss.syntaxpane.DefaultSyntaxKit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.wandora.application.LocatorHistory;
import org.wandora.application.gui.topicpanels.processing.SketchTemplate;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.Options;
import org.wandora.utils.IObox;
// import processing.SketchTemplate;


/**
 *
 * @author Elias Tertsunen
 */


public class ProcessingTopicPanel extends JPanel implements TopicMapListener, RefreshListener, TopicPanel, ActionListener, ComponentListener {
    public boolean USE_LOCAL_OPTIONS = true;
    public boolean SAVE_SKETCH_TO_GLOBAL_OPTIONS = true;
    
    public static final int NO_SOURCE = 0;
    public static final int OCCURRENCE_SOURCE = 1;
    public static final int FILE_SOURCE = 2;
    
    public static final int DONT_AUTORUN = 0;
    public static final int AUTORUN_OCCURRENCE = 1;
    public static final int AUTORUN_SKETCH_IN_EDITOR = 2;
    public static final int AUTORUN_FILE = 4;
    
    private static int autorun = 0;
    private static String autorunSketchFile = "";
    private static boolean autoloadFromOccurrence = false;
    
    private int currentSketchSource = NO_SOURCE;
    private String currentSketchFile = null;
    private String currentSketch = null;

    private static final String buildPath = "resources/";
    private static final String sketchPath = "resources/processing/";
//    private String processingClassName; // = "ProcessingSketch";
    private static String processingClassPackage = "processing";
    
    private static final String tempReplaceStart = "/*<--||";
    private static final String tempReplaceEnd = "-->||*/";
    
    private TopicMap tm;
    private Topic rootTopic;

    private boolean isSourceCompiled = false;
    private SketchTemplate runningPApplet = null;

    private Options options = null;
    private JDialog optionsDialog = null;
    private JFrame proWin = null;
    private int lastLocX, lastLocY;
    private boolean isCurrentBuildInWindow = false;

    private int charactersBefore = 0;
    private int linesBeforeCode = 0;
    
    
    private static Font errorMessageFont = new Font("Courier New", Font.BOLD, 11);

    public static final String optionsPrefix = "options.gui.processingTopicPanel";

    public static final String PROCESSING_OCCURRENCE_TYPE_SI="http://processing.org";

//    private long classIdentifier = 0;
    
    private JPopupMenu menu = null;
    private JFileChooser fc = null;

    
    
    

    private final HashMap<String,BuildCacheEntry> buildCache=new HashMap<String,BuildCacheEntry>();
    private static class BuildCacheEntry{
        public Class<? extends SketchTemplate> cls;
        public String className;
        public String source;
    }
    
    private static final String defaultMessage = 
            "/* \n"+
            " * Welcome to Wandora's Processing topic panel!\n"+
            " * \n"+
            " * Processing topic panel is used to write and run Processing scripts.\n"+
            " * Processing script can access the topic map in Wandora via Wandora's Java API.\n"+
            " * Topic panel suits best for topic and topic map visualizations.\n"+
            " * Writing a Processing script requires programming skills.\n"+
            " * \n"+
            " * You might want to begin by trying one of our demo sketches. We have\n"+
            " * prepared three example scripts to inspire you. These sketces, titled as\n"+
            " * AssociationArches, TopicBoxes and TopicRing can be opened by clicking\n"+
            " * the Open button and selecting option Open sketch from file...\n"+
            " * To run a script press Run button down under.\n"+
            " * \n"+
            " * Learn Processing and Wandora API here\n"+
            " *    http://processing.org/\n"+
            " *    http://wandora.org/api/ \n"+
            " */\n"+
            "\n"+
            "package processing;\n"+
            "\n"+
            "import org.wandora.application.gui.topicpanels.processing.SketchTemplate;\n"+
            "import org.wandora.topicmap.*;\n"+
            "\n"+
            "public class Sketch extends SketchTemplate {\n"+
            "\n"+
            "\tpublic void setup() {\n"+
            "\t\tsize(920,640);\n"+
            "\t\tframeRate(10);\n"+
            "\t}\n"+
            "\n"+
            "\tpublic void draw() {\n"+
            "\t\tbackground(255);\n"+
            "\t\tTopic t = getCurrentTopic();\n"+
            "\t\tif( t != null ) {\n"+
            "\t\t\ttry{\n"+
            "\t\t\t\tfill(0xff000000);\n"+
            "\t\t\t\ttext(t.getBaseName(),100,100);\n"+
            "\t\t\t} catch(TopicMapException tme){tme.printStackTrace();}\n"+
            "\t\t}\n"+
            "\t}\n"+
            "}\n";
  
    
    
    
    /** Creates new form ProcessingTopicPanel */
    public ProcessingTopicPanel() {
    }
    
    
    
    @Override
    public void init() {
        Wandora wandora = Wandora.getWandora();
        if(options == null) {
            if(USE_LOCAL_OPTIONS) {
                options = new Options(wandora.getOptions());
            }
            else {
                options = wandora.getOptions();
            }
        }
        tm = wandora.getTopicMap();

        initComponents();
        this.addComponentListener(this);
        try {
            // JavaSyntaxKit syntaxKit = new JavaSyntaxKit();
            // syntaxKit.install(processingEditor);
            DefaultSyntaxKit.initKit();
            processingEditor.setContentType("text/java");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
                
        /*
        EditorKit ek=processingEditor.getEditorKit();
        if(ek instanceof DefaultSyntaxKit){
            DefaultSyntaxKit sk=(DefaultSyntaxKit)ek;
            sk.setProperty("Action.parenthesis", null); 
            sk.setProperty("Action.brackets", null);
            sk.setProperty("Action.quotes", null);
            sk.setProperty("Action.double-quotes", null);
            sk.setProperty("Action.close-curly", null);
        }*/
        readOptions();

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
        processingEditor.getInputMap().put(key, "saveOperation");
        Action saveOperation = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentSketch();
            }
        };
        processingEditor.getActionMap().put("saveOperation", saveOperation);
        innerPanel.addComponentListener(
            new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {
                    revalidate();
                    Wandora.getWandora().validate();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    revalidate();
                    Wandora.getWandora().validate();
                }

                @Override
                public void componentShown(ComponentEvent e) {
                    revalidate();
                    Wandora.getWandora().validate();
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    revalidate();
                    Wandora.getWandora().validate();
                }
            }
        );

        if(currentSketch != null) {
            processingEditor.setText(currentSketch);
        }
        else {
            processingEditor.setText(defaultMessage);
        }

        FileNameExtensionFilter sketchFilter = new FileNameExtensionFilter("Processing sketch files", "sketch");
        fc = new JFileChooser();
        fc.setFileFilter(sketchFilter);
        fc.setCurrentDirectory(new File(sketchPath));
    }
    
    
    @Override
    public boolean supportsOpenTopic() {
        return true;
    }
    
    
    
    @Override
    public void open(Topic topic) throws TopicMapException {
	rootTopic = topic;

        if(autoloadOccurrenceCheckBox.isSelected()) {
            String sketch = getProcessingSketchOccurrence();
            if(sketch != null) {
                processingEditor.setText(sketch);
            }
        }
        autorun();
        if(runningPApplet != null) {
            initializePApplet(runningPApplet);
        }
    }
    
    
    
    
    
    
    private void autorun() {
        String autorunSketch = null;
        switch(autorun) {
            case AUTORUN_OCCURRENCE: {
                autorunSketch = getProcessingSketchOccurrence();
                break; 
            }
            case AUTORUN_SKETCH_IN_EDITOR: { 
                autorunSketch = processingEditor.getText();
                break; 
            }
            case AUTORUN_FILE: {
                try {
                    if(autorunSketchFile != null && autorunSketchFile.length() > 0) {
                       autorunSketch = IObox.loadFile(autorunSketchFile);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                break; 
            }
        }
        if(autorunSketch != null) {
            execute(autorunSketch);
        }
    }
    
    
    
    private void execute(String sketch) {
        stopRunning();
        runSource(buildSource(sketch));
        if(isSourceCompiled && !isCurrentBuildInWindow) {
            tabPanel.setSelectedComponent(runPanel);
            runPanel.repaint();
        }
        else {
            tabPanel.setSelectedComponent(editorPanel);
        }
    }
    
    
    
    private void readOptions () {
        if(options != null) {
            boolean isSelected = options.getBoolean(optionsPrefix+".autoLoadOccurrence", false);
            autoloadOccurrenceCheckBox.setSelected(isSelected);

            autorun = options.getInt(optionsPrefix+".autorun", autorun);
            autorunSketchFile = options.get(optionsPrefix+".autorunSketchFile");
            if(autorunSketchFile != null) {
                autoRunFileTextField.setText(autorunSketchFile);
            }
            else {
                autoRunFileTextField.setText("");
            }
            
            currentSketch = options.get(optionsPrefix+".currentSketch");

            switch(autorun) {
                case DONT_AUTORUN: { noAutoRunRadioButton.setSelected(true); break; }
                case AUTORUN_OCCURRENCE: { autoRunOccurrenceRadioButton.setSelected(true); break; }
                case AUTORUN_SKETCH_IN_EDITOR: { autoRunSketchInEditorRadioButton.setSelected(true); break; }
                case AUTORUN_FILE: { autoRunFileRadioButton.setSelected(true); break; }
            }

            boolean isSelectedOpenInNewWindow = options.getBoolean(optionsPrefix+".runInWindow", false);
            runInWindowCheckBox.setSelected(isSelectedOpenInNewWindow);

            lastLocX = options.getInt(optionsPrefix+".windowX", 0);
            lastLocY = options.getInt(optionsPrefix+".windowY", 0);
        }
    }
    
    private void storeProcessingWindowLocation() {
	lastLocX = proWin.getLocation().x;
	lastLocY = proWin.getLocation().y;
        if(options != null) {
            options.put(optionsPrefix+".windowX", lastLocX);
            options.put(optionsPrefix+".windowY", lastLocY);
        }
    }
    
    
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        
        
        //System.out.println("Processing panel catched action command '" + c + "'.");
        
        if("Check".equalsIgnoreCase(c)) {
            String source = buildSource(processingEditor.getText());
            if(compileProcessing(source, false)) {
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "No errors found.", "Syntax checked", WandoraOptionPane.PLAIN_MESSAGE);
            }
            tabPanel.setSelectedComponent(editorPanel);
        }
        else if("Run".equalsIgnoreCase(c)) {
            String source = buildSource(processingEditor.getText());
            runSource(source);
            if(isSourceCompiled && !isCurrentBuildInWindow) {
                tabPanel.setSelectedComponent(runPanel);
            }
        }
        else if("Stop".equalsIgnoreCase(c)) {
            stopRunning();
            tabPanel.setSelectedComponent(editorPanel);
        } 
        else if("New sketch".equalsIgnoreCase(c)) {
            newSketch();
        }
        else if("Open sketch from occurrence".equalsIgnoreCase(c)) {
            loadSketchFromOccurrence();
        }
        else if("Open sketch from file...".equalsIgnoreCase(c)) {
            loadSketchFromFile();
        }
        else if("Save sketch to occurrence".equalsIgnoreCase(c)) {
            saveSketchToOccurrence();
        }
        else if("Save sketch to file...".equalsIgnoreCase(c)) {
            saveSketchToFile();
        }
        else if("Options...".equalsIgnoreCase(c)) {
            openOptionsDialog();
        }
    }
    
    
    
    
    private String buildSource(String sketch) {
        boolean useTemplate=false; // maybe make this an option in the future

	try {
//            classIdentifier = System.currentTimeMillis();
            if(useTemplate){
//                processingClassName="SketchTemplate";

                String sketchTemplate = "";
                FileInputStream stream = new FileInputStream(new File(sketchPath+"SketchTemplate.java"));

                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                    /* Instead of using default, pass in a decoder. */
                    sketchTemplate = Charset.defaultCharset().decode(bb).toString();
                }
                finally {
                    stream.close();
                }
                String upperPart = sketchTemplate.substring(0, sketchTemplate.indexOf(tempReplaceStart)+1);
                linesBeforeCode = upperPart.split(System.getProperty("line.separator")).length - 1;
                //System.out.println("Lines before code "+linesBeforeCode);

                charactersBefore = sketchTemplate.indexOf(tempReplaceStart);
                //temp = temp.substring(temp.indexOf("\n"));

                String fullSource = sketchTemplate.substring(0, sketchTemplate.indexOf(tempReplaceStart)) +
                    sketch +
                    sketchTemplate.substring(sketchTemplate.indexOf(tempReplaceEnd)+tempReplaceEnd.length(), sketchTemplate.length());

//                fullSource = fullSource.replaceAll("(?:SketchTemplate)", processingClassName+classIdentifier);
//                fullSource = fullSource.replaceFirst("(?:PApplet)", "SketchTemplate");
                return fullSource;
            }
            else {
                // (?m) turns on multi-line mode so ^ and $ match line start and end
                Pattern p=Pattern.compile("(?m)^\\s*package\\s+processing\\s*;\\s*$");
                if(!p.matcher(sketch).find()){
                    throw new Exception("The sketch file must be defined to be in the \"processing\" package. Add \"package processing;\" at the start of the sketch or modify the existing package declaration.");
                }

                return sketch;
            }
	} 
        catch (Exception ex) {
	    Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
	}
        return null;
    }
    
    
    
    
    
    /*
     * Tabpanel
     */
    private void stopRunningInPanel() {
        if(innerPanel != null) {
            Component[] components = innerPanel.getComponents();
            for(int i = 0; i < components.length; i++) {
                if(components[i] != null && components[i] instanceof PApplet) {
                    PApplet proComponent = (PApplet) components[i];
//                    proComponent.stop();
                    proComponent.dispose();
                    innerPanel.remove(proComponent);
                }
            }
        }
    }
    
    /*
     * Window
     */
    private void stopRunningInWindow() {
	if(proWin != null) {
            if(runningPApplet != null) {
                if(runningPApplet.getParent() != null) {
                    runningPApplet.getParent().remove(runningPApplet);
//                    runningPApplet.stop();
                    runningPApplet.dispose();
                    runningPApplet = null;
                }
            }
	}
    }
    
    
    private void stopRunning() {
	stopRunningInWindow();
	stopRunningInPanel();
    }
    

    

    
    // -------------------------------------------------------------------------
    
    
    
    private void runSourceInPanel(String source) {
	stopRunning();
	if(proWin != null) {
	    proWin.setVisible(false);
	    proWin.dispose();
	    storeProcessingWindowLocation();
	}
	
	isCurrentBuildInWindow = false;
	
	try {
            isSourceCompiled = compileProcessing(source);
            if(!isSourceCompiled) return;
            
	    initializePApplet(runningPApplet);
            
	    innerPanel.add(runningPApplet);
	    runningPApplet.init();
	    revalidate();
	    Wandora.getWandora().validate();
	    isSourceCompiled = true;
	} 
        catch (Exception ex) {
	    Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, null, ex);
	    isSourceCompiled = false;
	}
    }
    
    
    
    private void runSourceInWindow(String source) {
	stopRunning();
	isCurrentBuildInWindow = true;
	try {
	    isSourceCompiled = compileProcessing(source);
            if(!isSourceCompiled) return;
            
            initializePApplet(runningPApplet);

	    if(proWin == null) {
		proWin = new JFrame("Processing visual");
		proWin.setIconImage(UIBox.getImage("resources/gui/appicon/icon.gif"));
		proWin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		proWin.addWindowListener(new WindowAdapter() {
                    @Override
		    public void windowClosing(WindowEvent evt) {
			storeProcessingWindowLocation();
			stopRunning();
			proWin.dispose();
			proWin = null;
		    }
		});
	    }
	    
	    proWin.getContentPane().add(runningPApplet);
	    runningPApplet.init();

	    int timeElapsed = 0;
	    
	    while (runningPApplet.defaultSize && !runningPApplet.finished && timeElapsed < 5000)
	    try {timeElapsed += 50;Thread.sleep(50);} catch (Exception e) {}
	    
	    Dimension size = new Dimension(runningPApplet.width, runningPApplet.height);
	    
	    proWin.getContentPane().setLayout(new GridBagLayout());
	    
	    proWin.getContentPane().setSize(size);
	    proWin.getContentPane().setPreferredSize(size);
	    proWin.getContentPane().setMinimumSize(size);
	    proWin.getContentPane().setMaximumSize(size);
	    
	    runningPApplet.setPreferredSize(size);
	    runningPApplet.setMinimumSize(size);
	    runningPApplet.setMaximumSize(size);
	    
	    proWin.pack();
            
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            if(lastLocX > 0 && lastLocY > 0 && lastLocX < dim.width && lastLocY < dim.height) {
                proWin.setLocation(lastLocX, lastLocY);
            }
            else {
                Wandora.getWandora().centerWindow(proWin);
            }
            
	    proWin.setVisible(true);
	    
	    isSourceCompiled = true;
	    
	    //compile;
	}
        catch (Exception ex) {
	    Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, null, ex);
	    isSourceCompiled = false;
	}
    }
    
    
    private void initializePApplet(PApplet pa) {
        try {
            if(pa != null) {
                Class c = pa.getClass();
                Method m = c.getMethod("wandoraInit", Topic.class, ProcessingTopicPanel.class);
                m.invoke(pa, rootTopic, this);
            }
        }
        catch(Exception e) {
            System.out.println("Exception occurred while initializing Processing PApplet.");
            e.printStackTrace();
        }
    }
    
    
    private void runSource(String source) {
	runSource(source, runInWindowCheckBox.isSelected());
    }
    
    
    private void runSource(String source, boolean isForWindow) {
	if(isForWindow) {
	    runSourceInWindow(source);
	} 
        else {
	    runSourceInPanel(source);
	}
    }

    private Class<? extends SketchTemplate> checkBuildCache(String cls,String source){
        synchronized(buildCache){
            BuildCacheEntry e=buildCache.get(cls);
            if(e==null) return null;
            if(e.source.equals(source)) return e.cls;
            return null;
        }
    }

    private void addBuildCache(String clsName,String source,Class<? extends SketchTemplate> cls){
        synchronized(buildCache){
            BuildCacheEntry e=new BuildCacheEntry();
            e.className=clsName;
            e.source=source;
            e.cls=cls;
            buildCache.put(clsName,e);
        }
    }
    
    private boolean compileProcessing(String source) {
	return compileProcessing(source, true);
    }
    
    
    // With ECJ compiler. Hmm... a bit long process, should probably be broken to smaller functions.
    private boolean compileProcessing(String source, boolean replaceApplet) {
	boolean success = false;

        long classIdentifier = System.currentTimeMillis();
        String processingClassName=null;
        String originalSource=source;
        Pattern p=Pattern.compile("(?m)^\\s*public\\s+class\\s([^\\s{]+)(?:[\\s{])");
        Matcher m=p.matcher(source);
        if(m.find()){
            processingClassName=m.group(1);
            source=source.replaceAll("([^a-zA-Z_])"+processingClassName+"([^a-zA-Z0-9_])","$1"+processingClassName+classIdentifier+"$2");
        }
        else {
	    Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, "Could not resolve sketch class name. Define your class plainly in a single line without obscuring it with comments.");
            return false;
        }

        Class<? extends SketchTemplate> cls=null;

        // Build cache probably isn't needed but the code's here still. If you
        // enable it also uncomment the one line towards the end of this method.
/*        cls=checkBuildCache(processingClassName, originalSource);
        if(cls!=null){
            try{
                runningPApplet=cls.newInstance();
            }
            catch (Exception ex) {
                ex.printStackTrace();
		Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, null, ex);
		showRichErrorDialog(ex.getMessage());
                return false;
            }
            return true;
        }*/


	File sourceFile = null;
	try {
	    sourceFile = new File(sketchPath+processingClassName+classIdentifier+".java");
            sourceFile.deleteOnExit();
	    BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile));
	    out.write(source);
	    out.close();
	}
        catch (Exception ex) {
            ex.printStackTrace();
	    Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, null, ex);
	}
        
        String classPath = System.getProperty("java.class.path");
        // System.out.println("classPath == "+classPath);

	// TODO: Here a chance to optimize the compilation process greatly. 
	// Compilation includes in all the classpaths used by Wandora, which
	// increase the compilation time greatly. So only the necessary
	// classpaths should included here. The ones that the SketchTemplate needs.
	String baseCommand[] = new String[] {
            "-Xemacs",
            "-source", "1.7",
            "-target", "1.7",
            "-classpath", classPath, 
            "-nowarn", "-noExit",
            "-d", buildPath // output the classes in the buildPath
	};

	String[] sourceFiles = new String[1];
	sourceFiles[0] = sourceFile.getAbsolutePath(); 
	String[] command = (String[]) ArrayUtils.addAll(baseCommand, sourceFiles);
	ArrayList<String> errors = new ArrayList<String>();
	
	try {

            StringWriter errorWriter=new StringWriter();

            // Wrap as a PrintWriter since that's what compile() wants
            PrintWriter printErrorWriter = new PrintWriter(errorWriter);

            //result = com.sun.tools.javac.Main.compile(command, writer);
            CompilationProgress progress = null;
            PrintWriter outWriter = new PrintWriter(System.out);
            success = BatchCompiler.compile(command, outWriter, printErrorWriter, progress);

            // Close out the stream for good measure
            printErrorWriter.close();

            BufferedReader reader = new BufferedReader(new StringReader(errorWriter.toString()));
            //System.err.println(errorBuffer.toString());

            String line = null;
            while ((line = reader.readLine()) != null) {
                //System.out.println("got line " + line);  // debug

                // get first line, which contains file name, line number,
                // and at least the first line of the error message
                String errorFormat = "([\\w\\d_]+.java):(\\d+):\\s*(.*):\\s*(.*)\\s*";
                String[] pieces = PApplet.match(line, errorFormat);

                // if it's something unexpected, die and print the mess to the console
                if(pieces == null) {
                    // Send out the rest of the error message to the console.

                    if(!line.startsWith("invalid Class-Path header") && line.length() > 3) {
                        line = replaceLineNumber(line);
                        errors.add(line);
                    }

                    while ((line = reader.readLine()) != null) {
                        if(!line.startsWith("invalid Class-Path header") && line.length() > 3) {
                            line = replaceLineNumber(line);
                            errors.add(line);
                        }
                    }
                }
            }
	} 
        catch (IOException e) {
	    e.printStackTrace();
	    String bigSigh = "Error while compiling. (" + e.getMessage() + ")";
	    //e.printStackTrace();
	    errors.add(bigSigh);
	    success = false;
	}

        // Order the application to delete all class files when application closes!
        HashSet<String> compiledClassFiles = IObox.getFilesAsHash(sketchPath, ".*"+processingClassName+classIdentifier+".+", 1, 99);
        for(String filename : compiledClassFiles) {
            File compiledClassFile = new File(filename);
            //System.out.println("Delete on exit "+compiledClassFile.getAbsolutePath());
            compiledClassFile.deleteOnExit();
        }
        
	if(success) {
	    if(runningPApplet != null) {
//		runningPApplet.stop();
                runningPApplet.dispose();

                // PApplet has a bad habit of having the animation thread get stuck
                // in a loop if paused==true even if dispose has been called too.
                // Calling .stop() sets it to true so this should be avoided.
                // But just to make sure, set paused=false here as well.
                runningPApplet.paused=false;
	    }
	    try {
		File sketchFile = new File(buildPath);
		ClassLoader loader = new URLClassLoader(new URL[] { sketchFile.toURI().toURL() });
                cls=(Class<? extends SketchTemplate>)loader.loadClass(processingClassPackage+"."+processingClassName+classIdentifier);
                // add to build cache, also enable the section at start if you want to use this
//                addBuildCache(processingClassName, originalSource, cls);
		runningPApplet = cls.newInstance();
	    } 
            catch (Exception ex) {
                ex.printStackTrace();
		Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, null, ex);
		showRichErrorDialog(ex.getMessage());
                return false;
	    }
	}
	
	if(errors.size() > 0) {
	    StringBuilder erromsg = new StringBuilder();
	    for (int i = 0; i < errors.size(); i++) {
		erromsg.append(errors.get(i));
		if(i<errors.size()-1) erromsg.append("\n");
	    }
	    showRichErrorDialog(erromsg.toString());
	}
	return success;
    }
    
    
    
    private String replaceLineNumber(String line) {
	int replaceStart = line.indexOf(".java:") + 6;
	if(replaceStart > -1) {
	    int replaceEnd = line.indexOf(":", replaceStart);
	    if(replaceEnd > -1) {
                try {
                    System.out.println("line == "+line);
                    int numbar = Integer.parseInt(line.substring(replaceStart, replaceEnd));
                    numbar -= linesBeforeCode;
                    line = line.substring(0, replaceStart) + numbar + line.substring(replaceEnd, line.length());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
	    }
	}
	return line;
    }
    
    
    private void showRichErrorDialog(String msg) {
	final JTextArea area = new JTextArea();
	area.setFont(errorMessageFont);
	//area.setPreferredSize(new Dimension(520, 180));
	area.setEditable(false);
	area.setText(msg);
	
	// Make the JOptionPane resizable using the HierarchyListener
        area.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                Window window = SwingUtilities.getWindowAncestor(area);
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog)window;
                    if (!dialog.isResizable()) {
                        dialog.setResizable(true);
                    }
                }
            }
        });
	
	JScrollPane scroller = new JScrollPane(area);
	scroller.setPreferredSize(new Dimension(520, 180));
	JOptionPane.showMessageDialog(Wandora.getWandora(), scroller, "Errors", JOptionPane.PLAIN_MESSAGE);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        optionsPanel = new javax.swing.JPanel();
        optionsTabbedPane = new SimpleTabbedPane();
        autoloadPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        autoloadOccurrenceCheckBox = new SimpleCheckBox();
        runWindowPanel = new javax.swing.JPanel();
        runInWindowLabel = new SimpleLabel();
        runInWindowCheckBox = new SimpleCheckBox();
        autorunPanel = new javax.swing.JPanel();
        noAutoRunRadioButton = new SimpleRadioButton();
        autoRunOccurrenceRadioButton = new SimpleRadioButton();
        autoRunSketchInEditorRadioButton = new SimpleRadioButton();
        autoRunStoredPanel = new javax.swing.JPanel();
        autoRunFileRadioButton = new SimpleRadioButton();
        autoRunFileTextField = new SimpleField();
        autorunFileButton = new SimpleButton();
        jPanel2 = new javax.swing.JPanel();
        optionsOkButton = new SimpleButton();
        autorunButtonGroup = new javax.swing.ButtonGroup();
        tabPanel = new SimpleTabbedPane();
        editorPanel = new javax.swing.JPanel();
        editorScroller = new SimpleScrollPane();
        processingEditor = new javax.swing.JEditorPane();
        codeBottomBar = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        checkBtn = new SimpleButton();
        executeBtn = new SimpleButton();
        stopBtn = new SimpleButton();
        fillerPanel = new javax.swing.JPanel();
        ioButtonPanel = new javax.swing.JPanel();
        newBtn = new SimpleButton();
        openBtn = new SimpleButton();
        saveBtn = new SimpleButton();
        jSeparator1 = new javax.swing.JSeparator();
        optionsBtn = new SimpleButton();
        runPanel = new javax.swing.JPanel();
        runPanel2 = new javax.swing.JPanel();
        innerPanel = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        runButtonPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        stopRunningButton = new SimpleButton();

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        autoloadPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("<html>Whether or not to automatically load Processing occurrence.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        autoloadPanel.add(jLabel1, gridBagConstraints);

        autoloadOccurrenceCheckBox.setText("Autoload Processing code from occurrence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        autoloadPanel.add(autoloadOccurrenceCheckBox, gridBagConstraints);

        optionsTabbedPane.addTab("Autoload", autoloadPanel);

        runWindowPanel.setLayout(new java.awt.GridBagLayout());

        runInWindowLabel.setText("<html>Should the Processing sketch run in a window instead of a tab.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        runWindowPanel.add(runInWindowLabel, gridBagConstraints);

        runInWindowCheckBox.setText("Run in window");
        runInWindowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runInWindowCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        runWindowPanel.add(runInWindowCheckBox, gridBagConstraints);

        optionsTabbedPane.addTab("Run window", runWindowPanel);

        autorunPanel.setLayout(new java.awt.GridBagLayout());

        autorunButtonGroup.add(noAutoRunRadioButton);
        noAutoRunRadioButton.setSelected(true);
        noAutoRunRadioButton.setText("Don't autorun");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 16);
        autorunPanel.add(noAutoRunRadioButton, gridBagConstraints);

        autorunButtonGroup.add(autoRunOccurrenceRadioButton);
        autoRunOccurrenceRadioButton.setText("Autorun sketch in occurrence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 16);
        autorunPanel.add(autoRunOccurrenceRadioButton, gridBagConstraints);

        autorunButtonGroup.add(autoRunSketchInEditorRadioButton);
        autoRunSketchInEditorRadioButton.setText("Autorun sketch in editor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 16);
        autorunPanel.add(autoRunSketchInEditorRadioButton, gridBagConstraints);

        autoRunStoredPanel.setLayout(new java.awt.GridBagLayout());

        autorunButtonGroup.add(autoRunFileRadioButton);
        autoRunFileRadioButton.setText("Autorun sketch in file");
        autoRunStoredPanel.add(autoRunFileRadioButton, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        autoRunStoredPanel.add(autoRunFileTextField, gridBagConstraints);

        autorunFileButton.setText("Browse");
        autorunFileButton.setMargin(new java.awt.Insets(0, 4, 0, 4));
        autorunFileButton.setMinimumSize(new java.awt.Dimension(55, 19));
        autorunFileButton.setPreferredSize(new java.awt.Dimension(55, 19));
        autorunFileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                autorunFileButtonMouseReleased(evt);
            }
        });
        autoRunStoredPanel.add(autorunFileButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 16);
        autorunPanel.add(autoRunStoredPanel, gridBagConstraints);

        optionsTabbedPane.addTab("Autorun", autorunPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        optionsPanel.add(optionsTabbedPane, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        optionsOkButton.setText("OK");
        optionsOkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                optionsOkButtonMouseReleased(evt);
            }
        });
        jPanel2.add(optionsOkButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionsPanel.add(jPanel2, gridBagConstraints);

        setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        setLayout(new java.awt.BorderLayout());

        tabPanel.setMinimumSize(new java.awt.Dimension(400, 100));
        tabPanel.setPreferredSize(new java.awt.Dimension(400, 200));

        editorPanel.setMinimumSize(new java.awt.Dimension(400, 200));
        editorPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        editorPanel.setLayout(new java.awt.GridBagLayout());

        editorScroller.setMinimumSize(new java.awt.Dimension(400, 200));
        editorScroller.setPreferredSize(new java.awt.Dimension(400, 200));
        editorScroller.setViewportView(processingEditor);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editorPanel.add(editorScroller, gridBagConstraints);

        codeBottomBar.setLayout(new java.awt.GridBagLayout());

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        checkBtn.setText("Check");
        checkBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        checkBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        checkBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        buttonPanel.add(checkBtn, gridBagConstraints);

        executeBtn.setText("Run");
        executeBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        executeBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        executeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                executeOnMouseRelease(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        buttonPanel.add(executeBtn, gridBagConstraints);

        stopBtn.setText("Stop");
        stopBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        stopBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        stopBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                stopBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        buttonPanel.add(stopBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        codeBottomBar.add(buttonPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        codeBottomBar.add(fillerPanel, gridBagConstraints);

        ioButtonPanel.setLayout(new java.awt.GridBagLayout());

        newBtn.setText("New");
        newBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        newBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        newBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                newBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        ioButtonPanel.add(newBtn, gridBagConstraints);

        openBtn.setText("Open");
        openBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        openBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        openBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                openBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        ioButtonPanel.add(openBtn, gridBagConstraints);

        saveBtn.setText("Save");
        saveBtn.setMaximumSize(new java.awt.Dimension(53, 23));
        saveBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        saveBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                saveBtnMouseReleased(evt);
            }
        });
        ioButtonPanel.add(saveBtn, new java.awt.GridBagConstraints());

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        ioButtonPanel.add(jSeparator1, gridBagConstraints);

        optionsBtn.setText("Options");
        optionsBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        optionsBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        optionsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                optionsBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        ioButtonPanel.add(optionsBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        codeBottomBar.add(ioButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        editorPanel.add(codeBottomBar, gridBagConstraints);

        tabPanel.addTab("Processing editor", editorPanel);

        runPanel.setBackground(new java.awt.Color(255, 255, 255));
        runPanel.setMinimumSize(new java.awt.Dimension(640, 480));
        runPanel.setPreferredSize(new java.awt.Dimension(640, 480));
        runPanel.setLayout(new java.awt.GridBagLayout());

        runPanel2.setBackground(new java.awt.Color(255, 255, 255));
        runPanel2.setPreferredSize(new java.awt.Dimension(100, 100));
        runPanel2.setLayout(new java.awt.GridBagLayout());

        innerPanel.setBackground(new java.awt.Color(250, 250, 250));
        innerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 240, 240)));
        innerPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        innerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        runPanel2.add(innerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        runPanel.add(runPanel2, gridBagConstraints);

        jSeparator2.setPreferredSize(new java.awt.Dimension(200, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        runPanel.add(jSeparator2, gridBagConstraints);

        runButtonPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        runButtonPanel.add(jPanel1, gridBagConstraints);

        stopRunningButton.setText("Stop");
        stopRunningButton.setMinimumSize(new java.awt.Dimension(75, 21));
        stopRunningButton.setPreferredSize(new java.awt.Dimension(75, 21));
        stopRunningButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                stopRunningButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        runButtonPanel.add(stopRunningButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 1, 2);
        runPanel.add(runButtonPanel, gridBagConstraints);

        tabPanel.addTab("Visual", runPanel);

        add(tabPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void executeOnMouseRelease(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_executeOnMouseRelease
    runSource(buildSource(processingEditor.getText()));
    if(isSourceCompiled && !isCurrentBuildInWindow) {
	tabPanel.setSelectedComponent(runPanel);
    }
}//GEN-LAST:event_executeOnMouseRelease

private void checkBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkBtnMouseReleased
    stopRunning();
    String source = buildSource(processingEditor.getText());
    if(compileProcessing(source, false)) {
	WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "No errors found.", "Syntax check", WandoraOptionPane.PLAIN_MESSAGE);
    }
}//GEN-LAST:event_checkBtnMouseReleased

private void newBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newBtnMouseReleased
    newSketch();
}//GEN-LAST:event_newBtnMouseReleased

private void saveBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMouseReleased
    showSaveMenu(evt);
}//GEN-LAST:event_saveBtnMouseReleased

    private void runInWindowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runInWindowCheckBoxActionPerformed
	boolean isSelected = runInWindowCheckBox.isSelected();
	if(options != null) options.put(optionsPrefix+".runInWindow", ""+isSelected);
    }//GEN-LAST:event_runInWindowCheckBoxActionPerformed

    private void stopBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopBtnMouseReleased
        stopRunning();
    }//GEN-LAST:event_stopBtnMouseReleased

    private void optionsBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionsBtnMouseReleased
        openOptionsDialog();
    }//GEN-LAST:event_optionsBtnMouseReleased

    private void openBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openBtnMouseReleased
        showOpenMenu(evt);
    }//GEN-LAST:event_openBtnMouseReleased

    private void optionsOkButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionsOkButtonMouseReleased
        if(optionsDialog != null) {
            optionsDialog.setVisible(false);
        }
        if(noAutoRunRadioButton.isSelected()) autorun = DONT_AUTORUN;
        else if(autoRunOccurrenceRadioButton.isSelected()) autorun = AUTORUN_OCCURRENCE;
        else if(autoRunSketchInEditorRadioButton.isSelected()) autorun = AUTORUN_SKETCH_IN_EDITOR;
        else if(autoRunFileRadioButton.isSelected()) autorun = AUTORUN_FILE;
        autorunSketchFile = autoRunFileTextField.getText();
        
        autoloadFromOccurrence = autoloadOccurrenceCheckBox.isSelected();
        
        if(options != null) {
            options.put(optionsPrefix+".autorun", ""+autorun);
            options.put(optionsPrefix+".autorunSketchFile", autorunSketchFile);
            options.put(optionsPrefix+".autoload", Boolean.toString(autoloadFromOccurrence));
            options.put(optionsPrefix+".currentSketch", processingEditor.getText());
        }
       
    }//GEN-LAST:event_optionsOkButtonMouseReleased

    private void stopRunningButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopRunningButtonMouseReleased
        stopRunning();
        tabPanel.setSelectedComponent(editorPanel);
    }//GEN-LAST:event_stopRunningButtonMouseReleased

    private void autorunFileButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_autorunFileButtonMouseReleased
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Select Processing sketch");
        int answer = fc.showDialog(Wandora.getWandora(), "Select");
        if(answer == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if(f != null) {
                autoRunFileTextField.setText(f.getAbsolutePath());
                autoRunFileRadioButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_autorunFileButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoRunFileRadioButton;
    private javax.swing.JTextField autoRunFileTextField;
    private javax.swing.JRadioButton autoRunOccurrenceRadioButton;
    private javax.swing.JRadioButton autoRunSketchInEditorRadioButton;
    private javax.swing.JPanel autoRunStoredPanel;
    private javax.swing.JCheckBox autoloadOccurrenceCheckBox;
    private javax.swing.JPanel autoloadPanel;
    private javax.swing.ButtonGroup autorunButtonGroup;
    private javax.swing.JButton autorunFileButton;
    private javax.swing.JPanel autorunPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton checkBtn;
    private javax.swing.JPanel codeBottomBar;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JScrollPane editorScroller;
    private javax.swing.JButton executeBtn;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JPanel ioButtonPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton newBtn;
    private javax.swing.JRadioButton noAutoRunRadioButton;
    private javax.swing.JButton openBtn;
    private javax.swing.JButton optionsBtn;
    private javax.swing.JButton optionsOkButton;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTabbedPane optionsTabbedPane;
    private javax.swing.JEditorPane processingEditor;
    private javax.swing.JPanel runButtonPanel;
    private javax.swing.JCheckBox runInWindowCheckBox;
    private javax.swing.JLabel runInWindowLabel;
    private javax.swing.JPanel runPanel;
    private javax.swing.JPanel runPanel2;
    private javax.swing.JPanel runWindowPanel;
    private javax.swing.JButton saveBtn;
    private javax.swing.JButton stopBtn;
    private javax.swing.JButton stopRunningButton;
    private javax.swing.JTabbedPane tabPanel;
    // End of variables declaration//GEN-END:variables

    
    // -------------------------------------------------------------------------
    
    
    
    private void openOptionsDialog() {
        optionsDialog = new JDialog(Wandora.getWandora(), true);
        optionsDialog.setSize(500,270);
        optionsDialog.add(optionsPanel);
        optionsDialog.setTitle("Processing topic panel options");
        Wandora.getWandora().centerWindow(optionsDialog);
        optionsDialog.setVisible(true);
    }
    
    
    
    
    private void showSaveMenu(MouseEvent evt) {
        ArrayList<String> menuStruct = new ArrayList();
        menuStruct.add("Save sketch to occurrence");
        menuStruct.add("Save sketch to file...");
        showMenu(menuStruct.toArray( new String[] {} ), evt);
    }
    
    
    
    
    private void showOpenMenu(MouseEvent evt) {
        ArrayList<String> menuStruct = new ArrayList();
        menuStruct.add("Open sketch from occurrence");
        menuStruct.add("Open sketch from file...");
        showMenu(menuStruct.toArray( new String[] {} ), evt);
    }
    
    
    
    
    private void showMenu(String[] struct, MouseEvent evt) {
        menu = UIBox.makePopupMenu(struct, this);
        menu.setLocation(evt.getXOnScreen()-2, evt.getYOnScreen()-2);
        menu.show(evt.getComponent(), evt.getX()-2, evt.getY()-2);
    }
    
    
    // -------------------------------------------------------------------------
    
    public void doRefresh() throws TopicMapException {
    }
    

    public void stop() {
        saveCurrentSketchToOptions();
        stopRunning();
    }

    public LocatorHistory getTopicHistory() {
        return null;
    }
    
    
    public void refresh() throws TopicMapException {
	doRefresh();
    }
    

    public boolean applyChanges() throws CancelledException, TopicMapException {
        saveCurrentSketchToOptions();
        return true;
    }

    public JPanel getGui() {
	return this;
    }

    public Topic getTopic() throws TopicMapException {
	return rootTopic;
    }

    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_processing.png");
    }

    @Override
    public boolean noScroll(){
        return false;
    }

    
    
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }

    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }

    
    
    public Object[] getViewMenuStruct() {
        Object[] openStructure = new Object[] {
            "Open sketch from occurrence", this,
            "Open sketch from file...", this,
        };
        JMenu openMenu = new SimpleMenu("Open sketch", UIBox.getIcon("resources/gui/icons/processing.png"));
        UIBox.attachMenu(openMenu, openStructure, this);
        
        Object[] saveStructure = new Object[] {
            "Save sketch to occurrence", this,
            "Save sketch to file...", this,
        };
        JMenu saveMenu = new SimpleMenu("Save sketch", UIBox.getIcon("resources/gui/icons/processing.png"));
        UIBox.attachMenu(saveMenu, saveStructure, this);
        
        Object[] menuStructure = new Object[] {
            "New sketch", UIBox.getIcon("resources/gui/icons/processing.png"), this,
            openMenu,
            saveMenu,
            "---",
            "Check", UIBox.getIcon("resources/gui/icons/processing.png"), this,
            "Run", UIBox.getIcon("resources/gui/icons/processing.png"), this,
            "Stop", UIBox.getIcon("resources/gui/icons/processing.png"), this,
            "---",
            "Options...", UIBox.getIcon("resources/gui/icons/processing.png"), this,
        };
        return menuStructure;
    }
    
    
    public void toggleVisibility(String componentName) {
    }
    
    
    
    @Override
    public String getName(){
        return "Processing";
    }
    
    
    @Override
    public String getTitle() {
        if(rootTopic != null) return TopicToString.toString(rootTopic);
        else return getName();
    }
    
    @Override
    public int getOrder() {
        return 1010;
    }
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------- ComponentListener ---
    // -------------------------------------------------------------------------
    
    
    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }
   
    
    private void handleComponentEvent(ComponentEvent e) {
        runPanel.revalidate();
        runPanel.repaint();
        
        editorPanel.revalidate();
        editorPanel.repaint();
        
        revalidate();
        repaint();
        /* WAS:
         try {
            Dimension size = this.getParent().getParent().getSize();
            size.height -= 26;
            runPanel.setPreferredSize(size);
            editorPanel.setPreferredSize(size);
            revalidate();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
         */
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    private void saveCurrentSketchToOptions() {
        if(options != null) {
            options.put(optionsPrefix+".currentSketch", processingEditor.getText());
        }
        if(USE_LOCAL_OPTIONS && SAVE_SKETCH_TO_GLOBAL_OPTIONS) {
            try {
                Wandora.getWandora().getOptions().put(optionsPrefix+".currentSketch", processingEditor.getText());
            }
            catch(Exception e) {
                
            }
        }
    }
    
    
    public void newSketch() {
        int answer = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Erase current sketch in editor?", "Erase sketch in editor?", WandoraOptionPane.YES_NO_OPTION);
        if(answer == WandoraOptionPane.YES_OPTION) {
            currentSketchSource = NO_SOURCE;
            currentSketchFile = null;
            processingEditor.setText("");
            saveCurrentSketchToOptions();
        }
    }
    
    
    
    public void loadSketchFromOccurrence() {
        try {
            Topic otype = tm.getTopic(PROCESSING_OCCURRENCE_TYPE_SI);
            Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
            if(otype != null && olang != null) {
                currentSketchFile = null;
                String o = rootTopic.getData(otype, olang);
                if(o != null) {
                    processingEditor.setText(o);
                    saveCurrentSketchToOptions();
                }
                else {
                    WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Processing occurrence in current topic. Can't restore Processing sketch from occurrence.", "Can't restore Processing occurrence", WandoraOptionPane.INFORMATION_MESSAGE);
                }
            }
            else {
                if(otype == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Processing occurrence type. Can't restore Processing sketch from occurrence.", "Can't find Processing occurrence type", WandoraOptionPane.INFORMATION_MESSAGE);
                if(olang == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Language independent scope topic. Can't restore R script from occurrence.", "Can't find Language independent scope topic", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void loadSketchFromFile() {
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Open Processing Sketch");
        int answer = fc.showDialog(Wandora.getWandora(), "Open");
        if(answer == SimpleFileChooser.APPROVE_OPTION) {
            File scriptFile = fc.getSelectedFile();
            try {
                String script = IObox.loadFile(scriptFile);
                currentSketchFile = scriptFile.getAbsolutePath();
                if(script != null) {
                    processingEditor.setText(script);
                    saveCurrentSketchToOptions();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while restoring Processing sketch from file '"+scriptFile.getName()+"'.", "Can't restore R script", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    
    
    public void saveSketchToOccurrence() {
        String script = processingEditor.getText();
        try {
            Topic otype = tm.getTopic(PROCESSING_OCCURRENCE_TYPE_SI);
            Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
            if(otype != null && olang != null) {
                boolean storeToOccurrence = true;
                currentSketchSource = OCCURRENCE_SOURCE;
                String oldScript = rootTopic.getData(otype, olang);
                if(oldScript != null) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Current topic already contains Processing sketch occurrence. Storing sketch erases sketch in occurrence. Do you want to store the sketch to the occurrence?","Topic already has a Processing occurrence", WandoraOptionPane.INFORMATION_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) storeToOccurrence = false;
                }
                if(storeToOccurrence) {
                    rootTopic.setData(otype, olang, script);
                }
            }
            else {
                if(otype == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Processing occurrence type. Can't save Processing sketch to occurrence.", "Can't find Processing occurrence type", WandoraOptionPane.INFORMATION_MESSAGE);
                if(olang == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Language independent scope topic. Can't save Processing sketch to occurrence.", "Can't find Language independent scope topic", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while storing the Processing sketch to an occurrence to current topic.", "Can't store Processing sketch", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    
    
    public void saveSketchToFile() {
        File scriptFile = null;
        try {
            fc.setDialogTitle("Save Processing sketch");
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            int answer = fc.showDialog(Wandora.getWandora(), "Save");
            if(answer == SimpleFileChooser.APPROVE_OPTION) {
                scriptFile = fc.getSelectedFile();
                String scriptCode = processingEditor.getText();
                FileUtils.writeStringToFile(scriptFile, scriptCode, null);
                currentSketchSource = FILE_SOURCE;
            }
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while storing Processing sketch to file '"+scriptFile.getName()+"'.", "Can't save R script", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }



    public String getProcessingSketchOccurrence() {
	try {
	    Topic processing_type = tm.getTopic(PROCESSING_OCCURRENCE_TYPE_SI);
	    if(processing_type != null) {
		String sketch = rootTopic.getData(processing_type, Wandora.getWandora().getLang());
		return sketch;
	    }
	} 
        catch (TopicMapException ex) {
	    Logger.getLogger(ProcessingTopicPanel.class.getName()).log(Level.SEVERE, null, ex);
	}
        return null;
    }
    
    
    
    
    
    public void saveCurrentSketch() {
        if(currentSketchSource == OCCURRENCE_SOURCE) {
            saveSketchToOccurrence();
        }
        else if(currentSketchSource == FILE_SOURCE) {
            saveSketchToFile();
        }
    }
    
    

    
    
    // -------------------------------------------------------------------------
    

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
	doRefresh();
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
	doRefresh();
    }

    
    // -------------------------------------------------------------------------
    
    
}
