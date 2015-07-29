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
 */

package org.wandora.application.tools.iot;

import java.awt.Dimension;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.wandora.application.Wandora;
import org.wandora.application.gui.GetTopicButton;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimplePanel;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleTextArea;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public class PingerPanel extends javax.swing.JPanel {

    private static final String PANEL_TITLE = "IoT pinger";
    
    private GetTopicButton maybeTargetButton;
    private GetTopicButton maybeSourceButton;
    private boolean expires;
    private boolean isRunning;
    private int delay;
    private TimerTask task;
    private static final DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
    private File saveFolder = null;
    
    private TopicMap tm;
    
    
    /**
     * Creates new form PingerPanel
     * @param tm
     */
    public PingerPanel(TopicMap tm) {
        this.tm = tm;
        isRunning = false;
        
        try {
            maybeTargetButton = new GetTopicButton();
            maybeSourceButton = new GetTopicButton();
        } 
        catch (Exception e) {
            return;
        }
        
        initComponents();
        
        InputVerifier verifier = new InputVerifier(){
            @Override
            public boolean verify(JComponent input) {
                JFormattedTextField tf = (JFormattedTextField)input;
                int v = Integer.parseInt(tf.getText());
                return v >= 0;
            }
        };
        
        yearField.setInputVerifier(verifier);
        monthField.setInputVerifier(verifier);
        dayField.setInputVerifier(verifier);
        hoursField.setInputVerifier(verifier);
        minutesField.setInputVerifier(verifier);
        secondsField.setInputVerifier(verifier);
        
        delayField.setInputVerifier(verifier);
        
        toggleExpirationFieldEnabled();
        setSetupEnabled(isRunning);
    }

    
    protected void openInOwnWindow(Wandora w) {
        JDialog dialog = new JDialog(w, false);
        dialog.add(this);
        dialog.setSize(600, 380);
        dialog.setMinimumSize(new Dimension(600, 380));
        dialog.setLocationRelativeTo(w);
        dialog.setVisible(true);
        dialog.setTitle(PANEL_TITLE);
    }
    
    
    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle("Choose folder for saved data");
        
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveFolder = chooser.getSelectedFile();
            saveFolderField.setText(saveFolder.getAbsolutePath());
        } else if(saveFolder == null) { // saveFolder is invalid, revert to default
            saveToggle.setSelected(false);
            saveButton.setEnabled(false);
        }
    }
    
    
    private void toggleExpirationFieldEnabled() {
        setTimeFieldsEnabled(expires);
        
        Calendar c = Calendar.getInstance();
        yearField.setValue(c.get(Calendar.YEAR));
        monthField.setValue(c.get(Calendar.MONTH)+1);
        dayField.setValue(c.get(Calendar.DAY_OF_MONTH));
        hoursField.setValue(c.get(Calendar.HOUR_OF_DAY)+1);
        minutesField.setValue(c.get(Calendar.MINUTE));
        secondsField.setValue(c.get(Calendar.SECOND));
    }
    

    private void setTimeFieldsEnabled(boolean enabled){
        yearField.setEnabled(enabled);
        monthField.setEnabled(enabled);
        dayField.setEnabled(enabled);
        hoursField.setEnabled(enabled);
        minutesField.setEnabled(enabled);
        secondsField.setEnabled(enabled);
    }
    
    
    private void setSetupEnabled(boolean running) {
        
        boolean saveOnTick = saveToggle.isSelected();
        
        targetButton.setEnabled(!running);
        sourceButton.setEnabled(!running);
        delayField.setEnabled(!running);
        sourceIsBinaryButton.setEnabled(!running);
        expiryToggle.setEnabled(!running);
        
        saveToggle.setEnabled(!running);
        saveFolderField.setEnabled(saveOnTick && !running);
        saveButton.setEnabled(saveOnTick && !running);
        
        setTimeFieldsEnabled(expires && !running);
    }
    
    
    /**
     * Logging helper passed to PingerWorker
     */
    protected interface Logger {
        void log(Exception e);
        void log(String s);
    }
    
    
    private Logger logger = new Logger() {

        @Override
        public void log(Exception e) {
            this.log(e.getMessage());
        }

        @Override
        public void log(String s) {
            logArea.append("[" + df.format(new Date()) + "] " + s + "\n");
            try {
                logArea.setCaretPosition(logArea.getLineStartOffset(logArea.getLineCount() - 1));
            }
            catch(Exception e) {
                // IGNORE
            }
        }
    };
    
    
    private long getExpiry() {
        Calendar expCal = Calendar.getInstance();
        try {
            int year = getIntegerFieldValue(yearField, expCal.get(Calendar.YEAR));
            int month = getIntegerFieldValue(monthField, expCal.get(Calendar.MONTH));
            int day = getIntegerFieldValue(dayField, expCal.get(Calendar.DAY_OF_MONTH));
            
            int hours = getIntegerFieldValue(hoursField, expCal.get(Calendar.HOUR));
            int minutes = getIntegerFieldValue(minutesField, expCal.get(Calendar.MINUTE));
            int seconds = getIntegerFieldValue(secondsField, expCal.get(Calendar.SECOND));
            
            expCal.set(
                year,
                month-1,
                day,
                hours,
                minutes,
                seconds
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
                
        return expCal.getTimeInMillis();
    }
    
    
    private int getIntegerFieldValue(JTextField field, int defaultValue) {
        if(field != null) {
            String text = field.getText();
            if(text != null && text.length() > 0) {
                try {
                    int integerValue = Integer.parseInt(text);
                    return integerValue;
                }
                catch(Exception e) {
                    // IGNORE
                }
            }
        }
        return defaultValue;
    }
    
    
    
    
    private boolean start() {
        
        final Topic targetType = ((GetTopicButton)targetButton).getTopic();
        final Topic sourceType = ((GetTopicButton)sourceButton).getTopic();
        
        if(targetType == null || sourceType == null){
            JOptionPane.showMessageDialog(this, "Target or Source type not set.");
            return false;
        }
        
        final boolean isBinary = sourceIsBinaryButton.isSelected();
        
        final long expiry = (expires) ? getExpiry() : Long.MAX_VALUE;
        
        if(saveToggle.isSelected() && saveFolder == null) {
            openFileChooser();
            if(saveFolder == null) {
                JOptionPane.showMessageDialog(this, "Save folder not set. ");
                return false;
            }
        }
        
        delay = ((Number)delayField.getValue()).intValue();
        Timer timer = new Timer();
        
        task = new TimerTask() {
            @Override
            public void run() {
                if(expires && expiry < System.currentTimeMillis()){
                    logger.log("Stopping due to expiry");
                    stop();
                    return;
                }
                
                if(expires) {
                    long tillExpiry = (expiry - System.currentTimeMillis()) / 1000;
                    startStopButton.setText("Running, stops automatically in "+tillExpiry+" seconds. Press to force stop.");
                }
                else {
                    startStopButton.setText("Running, press to stop");
                }
                statusField.setText("Running");
                
                try {
                    if(saveToggle.isSelected()){
                        PingerWorker.run(targetType, sourceType, tm, logger, isBinary, saveFolder);
                    } else {
                        PingerWorker.run(targetType, sourceType, tm, logger, isBinary);
                    }
                } 
                catch (Exception e) {
                    logger.log(e);
                }
            }
        };
        
        logger.log("Starting pinger");
        isRunning = true;
        setSetupEnabled(isRunning);
        
        timer.schedule(task, 0, Math.max(5000, delay*1000));
        return true;
    }
    
    

    
    private void stop() {
        if(task != null) {
            logger.log("Stopping pinger");
            task.cancel();
        }
        startStopButton.setText("Start");
        startStopButton.setSelected(false);
        statusField.setText("Stopped");
        setupPane.setEnabled(true);
        isRunning = false;
        setSetupEnabled(isRunning);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        statusField = new javax.swing.JTextField();
        tabs = new SimpleTabbedPane();
        Setupcontainer = new SimplePanel();
        setupPane = new SimplePanel();
        description = new SimpleTextArea();
        targetLabel = new SimpleLabel();
        targetButton = maybeTargetButton;
        sourceLabel = new SimpleLabel();
        sourceButton = maybeSourceButton;
        sourceIsBinaryButton = new SimpleCheckBox();
        delayLabel = new SimpleLabel();
        delayField = new javax.swing.JFormattedTextField();
        secondLabel = new SimpleLabel();
        expiresLabel = new SimpleLabel();
        expiryToggle = new SimpleCheckBox();
        yearLabel = new SimpleLabel();
        yearField = new javax.swing.JFormattedTextField();
        monthLabel = new SimpleLabel();
        monthField = new javax.swing.JFormattedTextField();
        dayLabel = new SimpleLabel();
        dayField = new javax.swing.JFormattedTextField();
        hoursLabel = new SimpleLabel();
        hoursField = new javax.swing.JFormattedTextField();
        minutesLabel = new SimpleLabel();
        minutesField = new javax.swing.JFormattedTextField();
        secondsLabel = new SimpleLabel();
        secondsField = new javax.swing.JFormattedTextField();
        saveLabel = new SimpleLabel();
        saveToggle = new SimpleCheckBox();
        saveFolderField = new javax.swing.JTextField();
        saveButton = new SimpleButton();
        logPane = new SimplePanel();
        logScroll = new SimpleScrollPane();
        logArea = new SimpleTextArea();
        startStopButton = new SimpleToggleButton();

        statusField.setEditable(false);
        statusField.setText("Stopped");
        statusField.setBorder(null);
        statusField.setOpaque(false);

        setMinimumSize(new java.awt.Dimension(520, 300));
        setPreferredSize(new java.awt.Dimension(520, 300));
        setLayout(new java.awt.GridBagLayout());

        Setupcontainer.setLayout(new java.awt.GridBagLayout());

        setupPane.setMaximumSize(new java.awt.Dimension(520, 300));
        setupPane.setMinimumSize(new java.awt.Dimension(520, 300));
        setupPane.setPreferredSize(new java.awt.Dimension(520, 300));
        setupPane.setLayout(new java.awt.GridBagLayout());

        description.setEditable(false);
        description.setColumns(20);
        description.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        description.setLineWrap(true);
        description.setRows(4);
        description.setText("The IoT pinger facilitates fetching data from an abritrary HTTP endpoint and storing it as an occurrence of a topic. The pinger looks for an source occurrence in each topic for the endpoint and stores the fetched response as a target occurrence of the corresponding topic. The process is repeated according to the delay parameter.\n");
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        setupPane.add(description, gridBagConstraints);

        targetLabel.setText("Target occurrence type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        setupPane.add(targetLabel, gridBagConstraints);

        targetButton.setMaximumSize(new java.awt.Dimension(35, 9));
        targetButton.setMinimumSize(new java.awt.Dimension(35, 9));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(targetButton, gridBagConstraints);

        sourceLabel.setText("Source occurrence type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 4, 4);
        setupPane.add(sourceLabel, gridBagConstraints);

        sourceButton.setMaximumSize(new java.awt.Dimension(35, 9));
        sourceButton.setMinimumSize(new java.awt.Dimension(35, 9));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(sourceButton, gridBagConstraints);

        sourceIsBinaryButton.setText("is binary");
        sourceIsBinaryButton.setToolTipText("Is the downloaded resource binary data. Check to turn the downloaded resource into a data url.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(sourceIsBinaryButton, gridBagConstraints);

        delayLabel.setText("Delay");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        setupPane.add(delayLabel, gridBagConstraints);

        delayField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        delayField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        delayField.setValue(new Integer(10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(delayField, gridBagConstraints);

        secondLabel.setText("s");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(secondLabel, gridBagConstraints);

        expiresLabel.setText("Expires");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        setupPane.add(expiresLabel, gridBagConstraints);

        expiryToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expiryToggleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(expiryToggle, gridBagConstraints);

        yearLabel.setText("Y");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(yearLabel, gridBagConstraints);

        yearField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        yearField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        yearField.setMinimumSize(new java.awt.Dimension(30, 20));
        yearField.setPreferredSize(new java.awt.Dimension(30, 20));
        yearField.setValue(new Integer(5));
        yearField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yearFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(yearField, gridBagConstraints);

        monthLabel.setText("M");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(monthLabel, gridBagConstraints);

        monthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        monthField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        monthField.setMaximumSize(new java.awt.Dimension(10, 20));
        monthField.setMinimumSize(new java.awt.Dimension(10, 20));
        monthField.setPreferredSize(new java.awt.Dimension(10, 20));
        monthField.setValue(new Integer(5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(monthField, gridBagConstraints);

        dayLabel.setText("D");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(dayLabel, gridBagConstraints);

        dayField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        dayField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dayField.setMaximumSize(new java.awt.Dimension(10, 20));
        dayField.setMinimumSize(new java.awt.Dimension(10, 20));
        dayField.setPreferredSize(new java.awt.Dimension(10, 20));
        dayField.setValue(new Integer(5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(dayField, gridBagConstraints);

        hoursLabel.setText("H");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(hoursLabel, gridBagConstraints);

        hoursField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        hoursField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hoursField.setMaximumSize(new java.awt.Dimension(10, 20));
        hoursField.setMinimumSize(new java.awt.Dimension(10, 20));
        hoursField.setPreferredSize(new java.awt.Dimension(10, 20));
        hoursField.setValue(new Integer(5));
        hoursField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hoursFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(hoursField, gridBagConstraints);

        minutesLabel.setText("M");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(minutesLabel, gridBagConstraints);

        minutesField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        minutesField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minutesField.setMaximumSize(new java.awt.Dimension(10, 20));
        minutesField.setMinimumSize(new java.awt.Dimension(10, 20));
        minutesField.setPreferredSize(new java.awt.Dimension(10, 20));
        minutesField.setValue(new Integer(5));
        minutesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minutesFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(minutesField, gridBagConstraints);

        secondsLabel.setText("S");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(secondsLabel, gridBagConstraints);

        secondsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        secondsField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        secondsField.setMaximumSize(new java.awt.Dimension(10, 20));
        secondsField.setMinimumSize(new java.awt.Dimension(10, 20));
        secondsField.setPreferredSize(new java.awt.Dimension(10, 20));
        secondsField.setValue(new Integer(5));
        secondsField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secondsFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        setupPane.add(secondsField, gridBagConstraints);

        saveLabel.setText("Save on tick");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        setupPane.add(saveLabel, gridBagConstraints);

        saveToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveToggleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(saveToggle, gridBagConstraints);

        saveFolderField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(saveFolderField, gridBagConstraints);

        saveButton.setText("Browse");
        saveButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        setupPane.add(saveButton, gridBagConstraints);

        Setupcontainer.add(setupPane, new java.awt.GridBagConstraints());

        tabs.addTab("Setup", Setupcontainer);

        logPane.setLayout(new java.awt.GridBagLayout());

        logArea.setEditable(false);
        logArea.setColumns(20);
        logArea.setRows(5);
        logArea.setMargin(new java.awt.Insets(4, 4, 4, 4));
        logScroll.setViewportView(logArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        logPane.add(logScroll, gridBagConstraints);

        tabs.addTab("Log", logPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(tabs, gridBagConstraints);

        startStopButton.setText("Start");
        startStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        add(startStopButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void yearFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yearFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_yearFieldActionPerformed

    private void secondsFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secondsFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_secondsFieldActionPerformed

    private void hoursFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hoursFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hoursFieldActionPerformed

    private void expiryToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expiryToggleActionPerformed
        expires = expiryToggle.isSelected();
        
        toggleExpirationFieldEnabled();
        
    }//GEN-LAST:event_expiryToggleActionPerformed

    private void minutesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minutesFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minutesFieldActionPerformed

    private void saveToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveToggleActionPerformed
        boolean selected = saveToggle.isSelected();
        saveFolderField.setEnabled(selected);
        saveButton.setEnabled(selected);
        if(selected && saveFolder == null) {
            openFileChooser();
        }
    }//GEN-LAST:event_saveToggleActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        openFileChooser();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void startStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopButtonActionPerformed
        isRunning = !isRunning;
        if(isRunning) {
            isRunning = start();
        }
        else {
            stop();
        }
        startStopButton.setSelected(isRunning);
    }//GEN-LAST:event_startStopButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Setupcontainer;
    private javax.swing.JFormattedTextField dayField;
    private javax.swing.JLabel dayLabel;
    private javax.swing.JFormattedTextField delayField;
    private javax.swing.JLabel delayLabel;
    private javax.swing.JTextArea description;
    private javax.swing.JLabel expiresLabel;
    private javax.swing.JCheckBox expiryToggle;
    private javax.swing.JFormattedTextField hoursField;
    private javax.swing.JLabel hoursLabel;
    private javax.swing.JTextArea logArea;
    private javax.swing.JPanel logPane;
    private javax.swing.JScrollPane logScroll;
    private javax.swing.JFormattedTextField minutesField;
    private javax.swing.JLabel minutesLabel;
    private javax.swing.JFormattedTextField monthField;
    private javax.swing.JLabel monthLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField saveFolderField;
    private javax.swing.JLabel saveLabel;
    private javax.swing.JCheckBox saveToggle;
    private javax.swing.JLabel secondLabel;
    private javax.swing.JFormattedTextField secondsField;
    private javax.swing.JLabel secondsLabel;
    private javax.swing.JPanel setupPane;
    private javax.swing.JButton sourceButton;
    private javax.swing.JCheckBox sourceIsBinaryButton;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JToggleButton startStopButton;
    private javax.swing.JTextField statusField;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JButton targetButton;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JFormattedTextField yearField;
    private javax.swing.JLabel yearLabel;
    // End of variables declaration//GEN-END:variables

}
