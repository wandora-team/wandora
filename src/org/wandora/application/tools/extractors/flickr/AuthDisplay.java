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
 * AuthDisplay.java
 *
 * Created on 24. huhtikuuta 2008, 19:03
 */

package org.wandora.application.tools.extractors.flickr;


import org.wandora.application.gui.simple.SimpleLabel;
import java.awt.Color;
import javax.swing.JLabel;

/**
 *
 * @author  anttirt
 */
public class AuthDisplay extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private JLabel[] curLabels, reqLabels;
    public int requiredAuthLevel, currentAuthLevel;
    private FlickrState flickrState;
    
    
    public AuthDisplay() {
        initComponents();
    }
    
    
    /** Creates new form AuthDisplay */
    public AuthDisplay(FlickrState state, String reqAuth) {
        initComponents();
        flickrState = state;
        setRequiredLevel(reqAuth);
    }
    
    
    public void setRequiredLevel(String requiredAuth) {
        curLabels = new JLabel[] { gotNone, gotRead, gotWrite, gotDelete };
        reqLabels = new JLabel[] { reqNone, reqRead, reqWrite, reqDelete };
        
        requiredAuthLevel = FlickrState.getAuthLevel(requiredAuth);
        currentAuthLevel = FlickrState.getAuthLevel(flickrState.PermissionLevel);
        
        Color validGreen = new Color(30, 220, 30);
        Color invalidRed = new Color(220, 30, 30);
        
        for(int i = 0; i <= currentAuthLevel; ++i) {
           curLabels[i].setForeground(validGreen);
        }
        for(int i = 0; i <= requiredAuthLevel; ++i) {
            if(i <= currentAuthLevel)
                reqLabels[i].setForeground(validGreen);
            else
                reqLabels[i].setForeground(invalidRed);
        }
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        reqNone = new SimpleLabel();
        gotNone = new SimpleLabel();
        reqRead = new SimpleLabel();
        gotRead = new SimpleLabel();
        reqWrite = new SimpleLabel();
        gotWrite = new SimpleLabel();
        reqDelete = new SimpleLabel();
        gotDelete = new SimpleLabel();
        lblPermLevel = new SimpleLabel();
        lblAuthLevel = new SimpleLabel();

        setLayout(new java.awt.GridBagLayout());

        reqNone.setForeground(new java.awt.Color(102, 102, 102));
        reqNone.setText("none");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(reqNone, gridBagConstraints);

        gotNone.setForeground(new java.awt.Color(102, 102, 102));
        gotNone.setText("none");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(gotNone, gridBagConstraints);

        reqRead.setForeground(new java.awt.Color(102, 102, 102));
        reqRead.setText("read");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(reqRead, gridBagConstraints);

        gotRead.setForeground(new java.awt.Color(102, 102, 102));
        gotRead.setText("read");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(gotRead, gridBagConstraints);

        reqWrite.setForeground(new java.awt.Color(102, 102, 102));
        reqWrite.setText("write");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(reqWrite, gridBagConstraints);

        gotWrite.setForeground(new java.awt.Color(102, 102, 102));
        gotWrite.setText("write");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(gotWrite, gridBagConstraints);

        reqDelete.setForeground(new java.awt.Color(102, 102, 102));
        reqDelete.setText("delete");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        add(reqDelete, gridBagConstraints);

        gotDelete.setForeground(new java.awt.Color(102, 102, 102));
        gotDelete.setText("delete");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        add(gotDelete, gridBagConstraints);

        lblPermLevel.setText("Required authorization:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(lblPermLevel, gridBagConstraints);

        lblAuthLevel.setText("Given authorization:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(lblAuthLevel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel gotDelete;
    private javax.swing.JLabel gotNone;
    private javax.swing.JLabel gotRead;
    private javax.swing.JLabel gotWrite;
    private javax.swing.JLabel lblAuthLevel;
    private javax.swing.JLabel lblPermLevel;
    private javax.swing.JLabel reqDelete;
    private javax.swing.JLabel reqNone;
    private javax.swing.JLabel reqRead;
    private javax.swing.JLabel reqWrite;
    // End of variables declaration//GEN-END:variables
    
}
