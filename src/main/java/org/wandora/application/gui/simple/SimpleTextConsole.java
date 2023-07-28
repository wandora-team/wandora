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
 * SimpleTextConsole.java
 *
 * Created on November 16, 2004, 5:07 PM
 */


package org.wandora.application.gui.simple;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import java.io.File;
import java.util.ArrayList;
import javax.swing.text.Document;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.IObox;



/**
 * @author akivela
 */



public class SimpleTextConsole extends SimpleTextPane {

    private static final int DEFAULT_FONT_SIZE = 12;

    private static final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private Color foregroundColor = DEFAULT_FOREGROUND_COLOR;
    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;

    private StringBuilder input = null;
    private int inputPos = 0;
    
    private SimpleTextConsoleListener consoleListener = null;
    
    private ArrayList<String> history;
    private int historyMaxSize = 999;
    private int historyPtr=0;
    private String tempHistory = null;
    
    
    

    public SimpleTextConsole(SimpleTextConsoleListener cl) {
        input = new StringBuilder("");
        inputPos = 0;
        
        consoleListener = cl;

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, DEFAULT_FONT_SIZE);
        setFont(font);
        
        setForeground(foregroundColor);
        setBackground(backgroundColor);
        setCaretColor(foregroundColor);

        history = new ArrayList<String>();
    }

    
    public void setFontSize(int s) {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, s);
        setFont(font);
    }

    
    

    protected boolean onKeyPressed(KeyEvent e) {
        boolean consumed = false;

        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            Document d = this.getDocument();
            this.setCaretPosition(d.getLength()-1);
            eraseInputInConsole();
            inputPos=0;
            String output = handleInput();
            //output("\n"+output);
            historyPtr=0;
            consumed = true;
        }
        
        else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            if(inputPos < input.length()) {
                inputPos++;
                Document d = this.getDocument();
                this.setCaretPosition(d.getLength()-inputPos);
            }
            consumed = true;
        }
        
        else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if(inputPos > 0) {
                inputPos--;
                Document d = this.getDocument();
                this.setCaretPosition(d.getLength()-inputPos);
            }
            consumed = true;
        }
        
        else if(e.getKeyCode() == KeyEvent.VK_UP) {
            if(historyPtr == 0) tempHistory = input.toString();
            if(historyPtr < history.size()) {
                inputPos=0;
                historyPtr++;
                changeInput(history.get(history.size() - historyPtr));
            }
            consumed = true;
        }
        
        else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            if(historyPtr > 0) {
                inputPos=0;
                historyPtr--;
                if(historyPtr == 0) {
                    changeInput(tempHistory);
                }
                else {
                    changeInput(history.get(history.size() - historyPtr));
                }
            }
            consumed = true;
        }
        
        else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            backspace();
            consumed = true;
        }
                
        else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
            del();
            consumed = true;
        }
                
        else if(e.getKeyCode() == KeyEvent.VK_HOME) {
            inputPos=input.length();
            Document d = this.getDocument();
            this.setCaretPosition(d.getLength()-inputPos);
            consumed = true;
        }
                
        else if(e.getKeyCode() == KeyEvent.VK_END) {
            inputPos=0;
            Document d = this.getDocument();
            this.setCaretPosition(d.getLength()-inputPos);
            consumed = true;
        }
        
        else if(isValidCharacter(e.getKeyChar())) {
            output(e.getKeyChar());
            input.insert(input.length()-inputPos, e.getKeyChar());
            consumed = true;
        }
        
        if(consumed) {
            refresh();
        }
        return consumed;
    }

    
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        int cpos = getCaretPosition();
        Document d = this.getDocument();
        if(d.getLength()-cpos <= input.length()) {
            inputPos = d.getLength()-cpos;
            this.setCaretPosition(d.getLength()-inputPos);
        }
    }

    
    private boolean isValidCharacter(int c) {
        if("1234567890qwertyuiopåasdfghjklöäzxcvbnmQWERTYUIOPÅASDFGHJKLÖÄZXCVBNM,.:;-+_'*!\"#¤%&/()=?`@${[]}\\ <>|".indexOf(c) != -1) return true;
        return false;
    }
    
    
    @Override
    public void cut() {
        super.cut();
    }
    
    
    
    @Override
    public void copy() {
        super.copy();
    }
    
    
    @Override
    public void paste() {
        String t = ClipboardBox.getClipboard();
        output(t);
        input.insert(input.length()-inputPos, t);
        refresh();
    }
    
    
    
    public void paste(String t) {
        if(t != null) {
            output(t);
            input.insert(input.length()-inputPos, t);
        }
        refresh();
    }
    
    public void changeInput(String newInput) {
        backspace(input.length());
        output(newInput);
        input = new StringBuilder(newInput);
        inputPos = 0;
    }
    
    
    public String handleInput(String in) {
        input = new StringBuilder(in);
        String output = handleInput();
        historyPtr=0;
        inputPos=0;
        return output;
    }
    public String handleInput() {
        String output = null;
        String inputStr = input.toString();
        
        if(inputStr.length() > 0) {
            history.add(inputStr);
            if(history.size() > historyMaxSize) {
                history.remove(0);
            }
        }
        
        if(consoleListener != null) {
            output = consoleListener.handleInput(inputStr);
        }
        input = new StringBuilder("");
        inputPos = 0;
        return output;
    }
    
    
    
    public void output(String o) {
        try {
            if(o != null) {
                Document d = this.getDocument();
                d.insertString(d.getLength()-inputPos, o, null);
                this.setCaretPosition(d.getLength()-inputPos);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    public void output(char c) {
        try {
            Document d = this.getDocument();
            d.insertString(d.getLength()-inputPos, ""+c, null);
            this.setCaretPosition(d.getLength()-inputPos);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void del() {
        if(inputPos > 0) {
            try {
                Document d = this.getDocument();
                d.remove(d.getLength()-inputPos, 1);
                input.deleteCharAt(input.length()-inputPos);
                inputPos--;
                this.setCaretPosition(d.getLength()-inputPos);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public void backspace() {
        if(inputPos < input.length()) {
            try {
                Document d = this.getDocument();
                d.remove(d.getLength()-inputPos-1, 1);
                this.setCaretPosition(d.getLength()-inputPos);
                input.deleteCharAt(input.length()-inputPos-1);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void backspace(int n) {
        if(inputPos < n) {
            try {
                Document d = this.getDocument();
                d.remove(d.getLength()-inputPos-n, n);
                this.setCaretPosition(d.getLength()-inputPos);
                input = new StringBuilder(input.subSequence(0, input.length()-inputPos-1));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void eraseInputInConsole() {
        try {
            Document d = this.getDocument();
            int inputLen = input.length();
            d.remove(d.getLength()-inputLen, inputLen);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    

    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }

    
    

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if(e.getID() != KeyEvent.KEY_PRESSED) {
            return;
        }
        boolean consumed = onKeyPressed(e);
        if(!consumed) {
            super.processKeyEvent(e);
        }
    }

    
    

    
    
    public void refresh() {

    }
    
    
    
    

    public void clear() {
        setText("");
        this.input = new StringBuilder("");
        inputPos = 0;
        refresh();
    }

    
    
    // -------------------------------------------------------------------------
    
    
    
    public void save() {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        if(chooser.open(Wandora.getWandora(), "Export")==SimpleFileChooser.APPROVE_OPTION){
            File file = chooser.getSelectedFile();
            try {
                IObox.saveFile(file, this.getText());
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    

    
    
    public void saveInput() {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        if(chooser.open(Wandora.getWandora(), "Export")==SimpleFileChooser.APPROVE_OPTION){
            File file = chooser.getSelectedFile();
            try {
                StringBuilder data = new StringBuilder("");
                for( String o : history ) {
                    data.append(o);
                    data.append(System.getProperty("line.separator"));
                }
                data.append(input);
                IObox.saveFile(file, data.toString());
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
}
