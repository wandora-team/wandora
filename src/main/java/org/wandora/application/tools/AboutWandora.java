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
 * AboutApplication.java
 *
 * Created on September 11, 2004, 2:56 PM
 */

package org.wandora.application.tools;


import java.awt.Dimension;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.utils.swing.ImagePanel;
import org.wandora.utils.swing.MultiLineLabel;


/**
 * Class implements <code>WandoraAdminTool</code> and opens simple dialog
 * panel viewing general information about Wandora application.
 *
 * @author  akivela
 */
public class AboutWandora extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private javax.swing.JDialog aboutDialog;
    private MultiLineLabel textLabel;

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/info.png");
    }

    @Override
    public String getName() {
        return "About Wandora";
    }

    @Override
    public String getDescription() {
        return "Views general information about Wandora software application.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            aboutDialog=new javax.swing.JDialog(wandora,"About Wandora",true);
            aboutDialog.getContentPane().setLayout(new java.awt.BorderLayout(20,0));
            ImagePanel titleLabel = new ImagePanel("gui/label_about_wandora.png");
            aboutDialog.getContentPane().add(titleLabel,java.awt.BorderLayout.NORTH);

            String text =
                    "Wandora is a general purpose knowledge editor application.\n"+
                    "Copyright (C) 2004-2023 Wandora Team\n \n"+

                    "This program is free software: you can redistribute it\n" +
                    "and/or modify it under the terms of the GNU General Public\n"+
                    "License as published by the Free Software Foundation,\n"+
                    "either version 3 of the License, or (at your option) any\n"+
                    "later version.\n \n"+

                    "This program is distributed in the hope that it will be useful,\n"+
                    "but WITHOUT ANY WARRANTY; without even the implied warranty\n"+
                    "of MERCHANTABILITY or FITNESS FOR A PARTICULAR\n"+
                    "PURPOSE. See the GNU General Public License for\n"+
                    "more details.\n \n"+
                    "For more information see http://wandora.org\n";
            
            textLabel=new MultiLineLabel(text);
            textLabel.setVisible(true);
            textLabel.setForeground(new java.awt.Color(50,50,50));
            textLabel.setBackground(wandora.getBackground());
            textLabel.setAlignment(textLabel.CENTER);
            textLabel.setMaximumSize(new Dimension(370, 50));
            textLabel.setMinimumSize(new Dimension(370, 50));
            textLabel.setPreferredSize(new Dimension(370, 50));
            aboutDialog.getContentPane().add(textLabel, java.awt.BorderLayout.CENTER);

            SimpleButton okButton = new SimpleButton();
            okButton.setText("OK");
            okButton.setMaximumSize(new Dimension(70, 24));
            okButton.setMinimumSize(new Dimension(70, 24));
            okButton.setPreferredSize(new Dimension(70, 24));
            okButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okButtonActionPerformed(evt);
                }
            });
            javax.swing.JPanel buttonContainer = new javax.swing.JPanel(new java.awt.FlowLayout());
            buttonContainer.add(okButton);
            aboutDialog.getContentPane().add(buttonContainer, java.awt.BorderLayout.SOUTH);

            aboutDialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
            aboutDialog.setResizable(false);
            aboutDialog.pack();
            if(wandora != null) wandora.centerWindow(aboutDialog);
            aboutDialog.setVisible(true);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        aboutDialog.setVisible(false);
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }

}
