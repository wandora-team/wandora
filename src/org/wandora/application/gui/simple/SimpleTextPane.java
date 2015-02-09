/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
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
 * 
 * SimpleTextPane.java
 *
 * Created on November 16, 2004, 5:07 PM
 */

package org.wandora.application.gui.simple;



import com.google.api.translate.Language;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import javax.swing.undo.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.UIBox;
import org.wandora.utils.*;
import org.wandora.utils.EasyVector;
import org.wandora.utils.MSOfficeBox;
import org.wandora.utils.Textbox;
import org.wandora.utils.language.GoogleTranslateBox;
import org.wandora.utils.language.MicrosoftTranslateBox;
import org.wandora.utils.language.SelectGoogleTranslationLanguagesPanel;
import org.wandora.utils.language.SelectMicrosoftTranslationLanguagesPanel;
import org.wandora.utils.language.SelectWatsonTranslationLanguagesPanel;
import org.wandora.utils.language.WatsonTranslateBox;

/**
 *
 * @author  akivela
 */
public class SimpleTextPane extends javax.swing.JTextPane implements MouseListener, ActionListener, SimpleComponent, Printable, UndoableEditListener, DropTargetListener, DragGestureListener {
    
    private boolean DROP_FILE_NAMES_INSTEAD_FILE_CONTENT = false;
    public static final int MAX_TEXT_SIZE = 999999;
    
    
    private Border defaultBorder = null;
    private DropTarget dt;
    private Wandora admin = null;
    
    
    
    // NOTE: THIS CLASS USES SAME OPTION DOMAIN AS TEXTEDITOR CLASS!!!
    public static final String OPTIONS_PREFIX = "textEditor.";
    
    
    JPanel parent = null;
    protected JPopupMenu popup;
    protected Object[] popupStruct = new Object[] {
        "Cut", UIBox.getIcon("gui/icons/cut.png"),
        "Copy", UIBox.getIcon("gui/icons/copy.png"),
        "Paste", UIBox.getIcon("gui/icons/paste.png"),
        "Clear", UIBox.getIcon("gui/icons/clear.png"),
        "---",
        "Select all", UIBox.getIcon("gui/icons/select_all.png"),
        "---",
        "Load...", UIBox.getIcon("gui/icons/file_open.png"),
        "Save...", UIBox.getIcon("gui/icons/file_save.png"),
        "---",
        "Translate with Google...", UIBox.getIcon("gui/icons/google_translate.png"),
        "Translate with Microsoft...", UIBox.getIcon("gui/icons/microsoft_translate.png"),
        "Translate with Watson...", UIBox.getIcon("gui/icons/watson_translate.png"),
        "---",
        "Print...", UIBox.getIcon("gui/icons/print.png"),
    };
   
    private boolean shouldWrapLines = true;
    public UndoManager undo = new UndoManager();
    
    
    
    
    /** Creates a new instance of SimpleTextPane */
    public SimpleTextPane(JPanel parent) {
        this.parent = parent;
        if(parent != null && parent instanceof MouseListener) this.addMouseListener((MouseListener) parent);
        //this.addFocusListener(this);
        this.addMouseListener(this);
        setUpGui();
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(true);
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,new HashSet(new EasyVector(new Object[]{AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,0)})));
        this.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,new HashSet(new EasyVector(new Object[]{AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,InputEvent.SHIFT_DOWN_MASK)})));
    
        getDocument().addUndoableEditListener(this);
        dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        this.setDragEnabled(true);
    }
    

    public SimpleTextPane() {
        this(null);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public void setUpGui() {
        popup = UIBox.makePopupMenu(popupStruct, this);
        setComponentPopupMenu(popup);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }
        
        
    
    // ----------------------------------------------------- WRAP LONG LINES ---
    
    
    
    public void setLineWrap(boolean shouldWrap) {
        shouldWrapLines = shouldWrap;
        setSize(getSize().width+1, getSize().height);
        this.requestFocus();
    }
    public boolean getLineWrap() {
        return shouldWrapLines;
    }

    
    @Override
    public void setSize(Dimension d) {
        if(!shouldWrapLines) {
            if (d.width < getParent().getSize().width)
                d.width = getParent().getSize().width;
        }
        super.setSize(d);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if(!shouldWrapLines) return false;
        else return super.getScrollableTracksViewportWidth();
    }
    
    
    public void dropFileNames(boolean flag) {
        DROP_FILE_NAMES_INSTEAD_FILE_CONTENT = flag;
    }
    
    
    
    // ------------------------------------------------------------ PRINTING ---
    
/*    public void print() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch(PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
        }
    }*/
    
  
    @Override
    public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int param) throws java.awt.print.PrinterException {
        if (param > 0) {
            return(NO_SUCH_PAGE);
        }
        else {
            Graphics2D g2d = (Graphics2D)graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            // Turn off double buffering
            this.paint(g2d);
            // Turn double buffering back on
            return(PAGE_EXISTS);
        }
    }


    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
    // ----------------------------------------------- FIND AND REPLACE TEXT ---
    
    
    public boolean findAndSelectNext(String findThis) {
        return findAndSelectNext(findThis,false);
    }
    
    public boolean findAndSelectNext(String findThis, boolean caseSensitive) {
        boolean success = false;
        this.requestFocus();
        try {
            Document doc = getDocument();
            String findHere = doc.getText(0, doc.getLength());
            int caretLoc = getCaretPosition();
            int selectionEndLoc = getSelectionEnd();
            int startLoc = Math.max(caretLoc, selectionEndLoc);
            
            int loc = caseIndexOf(findHere, findThis, startLoc, caseSensitive);
            if(loc == -1) { caseIndexOf(findHere, findThis, 0, caseSensitive); }
            
            if(loc > -1) {
                select(loc, loc+findThis.length());
                success = true;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return success;
    }
    
    
    
    
    public boolean findAndReplaceNext(String findThis, String replaceWith) {
        return findAndReplaceNext(findThis, replaceWith, false);
    }
    public boolean findAndReplaceNext(String findThis, String replacementString, boolean caseSensitive) {
        boolean success = false;
        this.requestFocus();
        try {
            Document doc = getDocument();
            int caretLoc = getCaretPosition();
            int selectionStartLoc = getSelectionStart();
            int startLoc = Math.min(caretLoc, selectionStartLoc);
            
            String findHere = doc.getText(0, doc.getLength());
            int loc = caseIndexOf(findHere, findThis, startLoc, caseSensitive);
            if(loc > -1) {
                setCaretPosition(loc); // where loc = position of first character of the string to replace
                AttributeSet ca = this.getCharacterAttributes();
                AttributeSet pa = this.getParagraphAttributes();
                doc.remove(loc, findThis.length());
                doc.insertString(loc, replacementString, ca);
                getStyledDocument().setParagraphAttributes(loc, replacementString.length(), pa, false);
                success = true;
            }
            findAndSelectNext(findThis, caseSensitive);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }
    
    
    public int findAndReplaceAll(String findThis, String replacementString) {
        return findAndReplaceAll(findThis, replacementString, false);
    }
    
    
    public int findAndReplaceAll(String findThis, String replacementString, boolean caseSensitive) {
        int maxCount = 9999;
        int count = 0;
        while(findAndReplaceNext(findThis, replacementString, caseSensitive) && ++count < maxCount);
        return count;
    }
    
    
    
    private int caseIndexOf(String findHere, String findThis, int startLoc, boolean caseSensitive) {
        if(caseSensitive) {
            return findHere.indexOf(findThis, startLoc);
        }
        else {
            String lowerCasedFindHere = findHere.toLowerCase();
            String lowerCasedFindThis = findThis.toLowerCase();
            return lowerCasedFindHere.indexOf(lowerCasedFindThis, startLoc);
        }
    }
    
    
    
    // ---------------------------------------------------------------- UNDO ---
    
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        //Remember the edit and update the menus
        undo.addEdit(e.getEdit());
        //undoAction.updateUndoState();
        //redoAction.updateRedoState();
    }
    
    
    
    // --------------------------------------------------------------- MOUSE ---
    
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    
    
    // -------------------------------------------------------------------------    
    
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();

        if(c.equals("Copy")) {
            this.copy();
        }
        else if(c.equals("Cut")) {
            this.cut();
        }
        else if(c.equals("Paste")) {
            this.paste();
        }
        else if(c.equals("Clear")) {
            this.setText("");
        }
        else if(c.equals("Select all")) {
            this.selectAll();
        }
        else if(c.startsWith("Load")) {
            load();
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
        else if(c.startsWith("Translate with Google")) {
            try {
                Document doc = this.getDocument();
                int selectionStartLoc = this.getSelectionStart();
                int selectionEndLoc = this.getSelectionEnd();

                if(selectionStartLoc == selectionEndLoc) {
                    selectionStartLoc = 0;
                    selectionEndLoc = doc.getLength();
                }
                int d = selectionEndLoc-selectionStartLoc;
                String translateThis = doc.getText(selectionStartLoc, d);
                AttributeSet ca = this.getCharacterAttributes();

                SelectGoogleTranslationLanguagesPanel selectLanguages = new SelectGoogleTranslationLanguagesPanel();
                selectLanguages.notInTopicMapsContext();
                if(admin == null) admin = Wandora.getWandora(this);
                selectLanguages.openInDialog(admin);
                if(selectLanguages.wasAccepted()) {
                    boolean markTranslation = selectLanguages.markTranslatedText();
                    Language sourceLang = selectLanguages.getSourceLanguage();
                    Collection<Language> targetLangs = selectLanguages.getTargetLanguages();
                    for(Language targetLang : targetLangs) {
                        String translated = GoogleTranslateBox.translate(translateThis, sourceLang, targetLang, markTranslation);
                        if(translated != null) {
                            doc.remove(selectionStartLoc, d);
                            doc.insertString(selectionStartLoc, translated, ca);
                        }
                        break;
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(c.startsWith("Translate with Microsoft")) {
            try {
                Document doc = this.getDocument();
                int selectionStartLoc = this.getSelectionStart();
                int selectionEndLoc = this.getSelectionEnd();

                if(selectionStartLoc == selectionEndLoc) {
                    selectionStartLoc = 0;
                    selectionEndLoc = doc.getLength();
                }
                int d = selectionEndLoc-selectionStartLoc;
                String translateThis = doc.getText(selectionStartLoc, d);
                AttributeSet ca = this.getCharacterAttributes();

                SelectMicrosoftTranslationLanguagesPanel selectLanguages = new SelectMicrosoftTranslationLanguagesPanel();
                selectLanguages.notInTopicMapsContext();
                if(admin == null) admin = Wandora.getWandora(this);
                selectLanguages.openInDialog(admin);
                if(selectLanguages.wasAccepted()) {
                    boolean markTranslation = selectLanguages.markTranslatedText();
                    com.memetix.mst.language.Language sourceLang = selectLanguages.getSourceLanguage();
                    Collection<com.memetix.mst.language.Language> targetLangs = selectLanguages.getTargetLanguages();
                    for(com.memetix.mst.language.Language targetLang : targetLangs) {
                        String translated = MicrosoftTranslateBox.translate(translateThis, sourceLang, targetLang, markTranslation);
                        if(translated != null) {
                            doc.remove(selectionStartLoc, d);
                            doc.insertString(selectionStartLoc, translated, ca);
                        }
                        break;
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else if(c.startsWith("Translate with Watson")) {
            try {
                Document doc = this.getDocument();
                int selectionStartLoc = this.getSelectionStart();
                int selectionEndLoc = this.getSelectionEnd();

                if(selectionStartLoc == selectionEndLoc) {
                    selectionStartLoc = 0;
                    selectionEndLoc = doc.getLength();
                }
                int d = selectionEndLoc-selectionStartLoc;
                String translateThis = doc.getText(selectionStartLoc, d);
                AttributeSet ca = this.getCharacterAttributes();

                SelectWatsonTranslationLanguagesPanel selectLanguages = new SelectWatsonTranslationLanguagesPanel();
                selectLanguages.notInTopicMapsContext();
                if(admin == null) admin = Wandora.getWandora(this);
                selectLanguages.openInDialog(admin);
                if(selectLanguages.wasAccepted()) {
                    boolean markTranslation = selectLanguages.markTranslatedText();
                    String languages = selectLanguages.getSelectedLanguages();
                    String translated = WatsonTranslateBox.translate(translateThis, WatsonTranslateBox.getLanguagesCodeFor(languages), markTranslation);
                    if(translated != null) {
                        doc.remove(selectionStartLoc, d);
                        doc.insertString(selectionStartLoc, translated, ca);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    
    

    // -------------------------------------------------------------------------
    // --------------------------------------------------------- LOAD & SAVE ---
    // -------------------------------------------------------------------------
    
    
    
    
    public void load() {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Open text file");
        if(chooser.open(Wandora.getWandora(), SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            load(chooser.getSelectedFile());
            getDocument().addUndoableEditListener(this);
        }
    }
    
    
    public void load(File textFile) {
        if(textFile != null) {
            if(textFile.length() > MAX_TEXT_SIZE){
                setText("File size too big!");
            }
            else {
                String newText = "";
                try {
                    Object desc = getStyledDocument();

                    Reader inputReader = null;
                    String content = "";
                    
                    String filename = textFile.getPath().toLowerCase();
                    String extension = filename.substring(Math.max(filename.lastIndexOf(".")+1, 0));
                    
                    // --- handle rtf files ---
                    if("rtf".equals(extension)) {
                        content=Textbox.RTF2PlainText(new FileInputStream(textFile));
                        inputReader = new StringReader(content);
                    }
                    
                    // --- handle pdf files ---
                    if("pdf".equals(extension)) {
                        try {
                            PDDocument doc = PDDocument.load(textFile);
                            PDFTextStripper stripper = new PDFTextStripper();
                            content = stripper.getText(doc);
                            doc.close();
                            inputReader = new StringReader(content);
                        }
                        catch(Exception e) {
                            System.out.println("No PDF support!");
                        }
                    }
                    
                    // --- handle MS office files ---
                    if("doc".equals(extension) ||
                       "ppt".equals(extension) ||
                       "xls".equals(extension) ||
                       "vsd".equals(extension) ||
                       "odt".equals(extension)
                       ) {
                            content = MSOfficeBox.getText(new FileInputStream(textFile));
                            if(content != null) {
                                inputReader = new StringReader(content);
                            }
                    }

                    if("docx".equals(extension)) {
                            content = MSOfficeBox.getDocxText(textFile);
                            if(content != null) {
                                inputReader = new StringReader(content);
                            }
                    }
                    
                    // --- handle everything else ---
                    if(inputReader == null) {
                        inputReader = new FileReader(textFile);
                    }
                    read(inputReader, desc);
                    setCaretPosition(0);
                }
                catch(Exception e) {
                    System.out.println("Exception '" + e.toString() + "' occurred while reading file '" + textFile.getPath() + "'.");
                }
            }
        }
    }
    
    
    
    
    public void save() {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle("Save text file");
        if(chooser.open(Wandora.getWandora(), SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
        }
    }
    
    
    
    public void save(File textFile) {
        if(textFile != null) {
            try {
                FileWriter writer=new FileWriter(textFile);
                write(writer);
                writer.close();
            }
            catch(Exception e) {
                System.out.println("Exception '" + e.toString() + "' occurred while saving file '" + textFile.getPath() + "'.");
            }
        }
    }
    
    
    
    
    

    // -------------------------------------------------------------------------

    
    
    
    @Override
    public void focusGained(java.awt.event.FocusEvent focusEvent) {
        if(admin == null) admin = Wandora.getWandora(this);
        if(admin != null) {
            admin.gainFocus(this);
        }
    }
    
    @Override
    public void focusLost(java.awt.event.FocusEvent focusEvent) {
        // DO NOTHING...
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------- DRAG & DROP ---
    // -------------------------------------------------------------------------
    
    

    
    @Override
    public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        if(! UIConstants.dragBorder.equals( this.getBorder())) {
            defaultBorder = this.getBorder();
            this.setBorder(UIConstants.dragBorder);
        }
    }
    
    
    @Override
    public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent) {
        this.setBorder(defaultBorder);
    }
    
    
    @Override
    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        if(! UIConstants.dragBorder.equals( this.getBorder())) {
            defaultBorder = this.getBorder();
            this.setBorder(UIConstants.dragBorder);
        }
    }
    
    
    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {
            DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor stringFlavor = DataFlavor.stringFlavor;
            DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            Transferable tr = e.getTransferable();
            if(tr.isDataFlavorSupported(fileListFlavor)) {
                //int ret=WandoraOptionPane.showOptionDialog(this, "Would you like to load text from the dropped document?","Load or reject", WandoraOptionPane.YES_NO_OPTION);
                //if(ret==WandoraOptionPane.YES_OPTION) {
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    java.util.List<File> files = (java.util.List<File>) tr.getTransferData(fileListFlavor);
                    String fileName = null;
                    boolean CTRLPressed = ((e.getDropAction() & DnDConstants.ACTION_COPY) != 0);
                    if(DROP_FILE_NAMES_INSTEAD_FILE_CONTENT && !CTRLPressed || (!DROP_FILE_NAMES_INSTEAD_FILE_CONTENT  && CTRLPressed)) {
                        StringBuilder sb = new StringBuilder("");
                        for( File file : files ) {
                            fileName = file.getAbsolutePath();
                            sb.append(fileName);
                            sb.append('\n');
                        }
                        String text = getText();
                        if(text == null) text = "";
                        if(text.length() > 0 && !text.endsWith("\n")) text = text + "\n" + sb.toString();
                        else text = text + sb.toString();
                        setText(text);
                    }
                    else {
                        ArrayList<WandoraTool> importTools = new ArrayList<WandoraTool>();
                        for( File file : files ) {
                            fileName = file.getName().toLowerCase();
                            //if(fileName.endsWith(".txt")) {
                                load(file);
                            //}
                        }
                    }
                    e.dropComplete(true);
                //}
            }
            else if(tr.isDataFlavorSupported(stringFlavor) || tr.isDataFlavorSupported(uriListFlavor)) {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String data = null;
                if(tr.isDataFlavorSupported(uriListFlavor)) data=(String)tr.getTransferData(uriListFlavor);
                else data=(String)tr.getTransferData(stringFlavor);

                boolean handled=false;
                
                String[] split=data.split("\n");
                boolean allFiles=true;
                ArrayList<URI> uris=new ArrayList<URI>();
                for(int i=0;i<split.length;i++){
                    try{
                        URI u=new URI(split[i].trim());
                        if(u.getScheme()==null) continue;
                        if(!u.getScheme().toLowerCase().equals("file")) allFiles=false;
                        uris.add(u);
                    } catch(java.net.URISyntaxException ue){}
                }
                if(uris.size()>0){
                    if(allFiles){
                        boolean CTRLPressed = ((e.getDropAction() & DnDConstants.ACTION_COPY) != 0);
                        if(DROP_FILE_NAMES_INSTEAD_FILE_CONTENT && !CTRLPressed || (!DROP_FILE_NAMES_INSTEAD_FILE_CONTENT  && CTRLPressed)) {
                            StringBuilder sb = new StringBuilder("");
                            for(URI u : uris){
                                sb.append(u.toString());
                                sb.append('\n');
                            }
                            String text = getText();
                            if(text == null) text = "";
                            if(text.length() > 0 && !text.endsWith("\n")) text = text + "\n" + sb.toString();
                            else text = text + sb.toString();
                            setText(text);
                        }
                        else {
                            for(URI u : uris){
                                try{
                                    load(new File(u));
                                }
                                catch(IllegalArgumentException iae){iae.printStackTrace();}
                            }
                        }
                    }
                    else{
                        java.util.List<String> urls=new java.util.ArrayList<String>();
                        String text="";
                        for(URI u : uris){
                            if(text.length()>0) text+="\n";
                            text+=u.toString();
                        }
                        this.setText(text);
                    }
                    handled=true;
                }

                if(!handled){
                    this.setText(data);
                    handled=true;
                }

                e.dropComplete(true);
            }
            else {
                System.out.println("Drop rejected! Wrong data flavor!");
                e.rejectDrop();
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        this.setBorder(defaultBorder);
    }
    
    @Override
    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
    }

    @Override
    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent dragGestureEvent) {
    }    
    
    
    

}
