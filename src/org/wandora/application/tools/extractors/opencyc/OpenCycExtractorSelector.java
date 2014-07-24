/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * OpenCycExtractorSelector.java
 *
 * Created on 22. heinäkuuta 2008, 12:17
 */

package org.wandora.application.tools.extractors.opencyc;
   
import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import org.wandora.application.tools.extractors.rdf.OWLExtractor;
import org.wandora.application.tools.extractors.rdf.OpenCYCOWLExtractor;
import org.wandora.application.tools.extractors.rdf.RDFSExtractor;



/**
 * Originally this extractor was created for openCYC rest API at 65.99.218.242.
 * However, it seems the rest API is very unstable and rarely answers requests.
 * Therefore this extractor has been changes and performs now an extraction to
 * the semantic web endpoint of the CYC at http://sw.opencyc.org/2012/05/10/concept/en/ .
 * As a consequence the number of available subextractors has decreased to one.
 * 
 * Old rest API extractors are still available but requires some changes in
 * UI. To activate old rest API extractors, move their UI tabs (panels) from
 * Other components to cycTabbedPane.
 *
 * @author  akivela
 */
public class OpenCycExtractorSelector extends JDialog {

    public static String webServiceBase = "http://65.99.218.242:8080/RESTfulCyc/";
   

    private Wandora admin = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    /** Creates new form OpenCycExtractorSelector */
    public OpenCycExtractorSelector(Wandora admin) {
        super(admin, true);
        initComponents();
        setTitle("OpenCyc extractors");
        setSize(450,300);
        admin.centerWindow(this);
        this.admin = admin;
        accepted = false;
    }

    
    

    public void setWandora(Wandora wandora) {
        this.admin = wandora;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }
    

    
    public boolean wasAccepted() {
        return accepted;
    }
    public void setAccepted(boolean b) {
        accepted = b;
    }
    
    
    
    public WandoraTool getWandoraTool(WandoraTool parentTool) {
        Component component = cycTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        
        if(swConceptPanel.equals(component)) {
            String conceptsAll = swConceptField.getText();
            String[] concepts = urlEncode(commaSplitter(conceptsAll));
            
            // http://sw.opencyc.org/2012/05/10/concept/en/Game
            String[] termUrls = completeString("http://sw.opencyc.org/2012/05/10/concept/en/__1__", concepts);
            
            // Notice the semantic web ap of CYC actually returns RDF triplets.
            // Thus, we use OpenCYCOWLExtractor.
            
            OpenCYCOWLExtractor ex = new OpenCYCOWLExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }

        // ***** TERM COMMENTS *****
        else if(commentsTab.equals(component)) {
            String termsAll = commentsField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/Constant/Food/comment
            String[] termUrls = completeString(webServiceBase+"Constant/__1__/comment", terms);
            
            OpenCycCommentExtractor ex = new OpenCycCommentExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        // ***** DENOTATIONS *****
        else if(denotationsTab.equals(component)) {
            String termsAll = denotationsField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/denotation/president
            String[] termUrls = completeString(webServiceBase+"denotation/__1__", terms);
            
            OpenCycDenotationsExtractor ex = new OpenCycDenotationsExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        // ***** SIBLINGS *****
        else if(siblingsTab.equals(component)) {
            String termsAll = siblingsField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/Constant/Retriever-Dog/siblings
            String[] termUrls = completeString(webServiceBase+"Constant/__1__/siblings", terms);
            
            OpenCycSiblingsExtractor ex = new OpenCycSiblingsExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        // ***** ISA *****
        else if(isaTab.equals(component)) {
            String termsAll = isaField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/Constant/EiffelTower/isa
            String[] termUrls = completeString(webServiceBase+"Constant/__1__/isa", terms);
            
            OpenCycIsaExtractor ex = new OpenCycIsaExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        // ***** INSTANCE *****
        else if(instancesTab.equals(component)) {
            String termsAll = instancesField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/Constant/Dog/instances
            String[] termUrls = completeString(webServiceBase+"Constant/__1__/instances", terms);
            
            OpenCycInstanceExtractor ex = new OpenCycInstanceExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        // ***** GENERALIZATIONS *****
        else if(genlsTab.equals(component)) {
            String termsAll = genlsField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/Constant/EiffelTower/genls
            String[] termUrls = completeString(webServiceBase+"Constant/__1__/genls", terms);
            
            OpenCycGenlsExtractor ex = new OpenCycGenlsExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        // ***** SPECIALIZATIONS *****
        else if(specsTab.equals(component)) {
            String termsAll = specsField.getText();
            String[] terms = urlEncode(commaSplitter(termsAll));
            
            // http://65.99.218.242:8080/RESTfulCyc/Constant/Shirt/specs
            String[] termUrls = completeString(webServiceBase+"Constant/__1__/specs", terms);
            
            OpenCycSpecsExtractor ex = new OpenCycSpecsExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        
        
        return wt;
    }
    
    
    
    
    
    
    
    public String[] commaSplitter(String str) {
        if(str.indexOf(',') != -1) {
            String[] strs = str.split(",");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.length() > 0) {
                    strList.add(s);
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
        
    }
    
    
    
    public String[] completeString(String template, String[] strs) {
        if(strs == null || template == null) return null;
        String[] completed = new String[strs.length];
        for(int i=0; i<strs.length; i++) {
            completed[i] = template.replaceAll("__1__", strs[i]);
        }
        return completed;
    }
    
    
    public String[] completeString(String template, String[] strs1, String[] strs2) {
        if(strs1 == null || strs2 == null || template == null) return null;
        if(strs1.length != strs2.length) return null;
        
        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2[i]);
        }
        return completed;
    }
    
    
    public String[] urlEncode(String[] urls) {
        if(urls == null) return null;
        String[] cleanUrls = new String[urls.length];
        for(int i=0; i<urls.length; i++) {
            cleanUrls[i] = urlEncode(urls[i]);
        }
        return cleanUrls;
    }
    
    
    
    public String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch(Exception e) {
            return url;
        }
    }
    
    
    
    
    public String getContextAsString() {
        StringBuilder sb = new StringBuilder("");
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                String str = null;
                Object o = null;
                while(contextObjects.hasNext()) {
                    str = null;
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        str = t.getBaseName();
                        if(str != null) {
                            str = str.trim();
                        }
                        else {
                            str = t.getOneSubjectIdentifier().toExternalForm();
                            if(str.indexOf('/') != -1) {
                                str = str.substring(str.lastIndexOf('/')+1);
                                str = str.trim();
                            }
                        }
                    }
                    
                    if(str != null && str.length() > 0) {
                        sb.append(str);
                        if(contextObjects.hasNext()) {
                            sb.append(", ");
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
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

        isaTab = new javax.swing.JPanel();
        isaInnerPanel = new javax.swing.JPanel();
        isaLabel = new org.wandora.application.gui.simple.SimpleLabel();
        isaField = new org.wandora.application.gui.simple.SimpleField();
        isaGetButton = new org.wandora.application.gui.simple.SimpleButton();
        instancesTab = new javax.swing.JPanel();
        instancesInnerPanel = new javax.swing.JPanel();
        instancesLabel = new org.wandora.application.gui.simple.SimpleLabel();
        instancesField = new org.wandora.application.gui.simple.SimpleField();
        instancesGetButton = new org.wandora.application.gui.simple.SimpleButton();
        genlsTab = new javax.swing.JPanel();
        genlsInnerPanel = new javax.swing.JPanel();
        genlsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        genlsField = new org.wandora.application.gui.simple.SimpleField();
        genlsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        specsTab = new javax.swing.JPanel();
        specsInnerPanel = new javax.swing.JPanel();
        specsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        specsField = new org.wandora.application.gui.simple.SimpleField();
        specsButton = new org.wandora.application.gui.simple.SimpleButton();
        siblingsTab = new javax.swing.JPanel();
        siblingsInnerPanel = new javax.swing.JPanel();
        siblingsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        siblingsField = new org.wandora.application.gui.simple.SimpleField();
        siblingsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        commentsTab = new javax.swing.JPanel();
        commentsInnerPanel = new javax.swing.JPanel();
        commentsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        commentsField = new org.wandora.application.gui.simple.SimpleField();
        commentsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        denotationsTab = new javax.swing.JPanel();
        denotationsInnerPanel = new javax.swing.JPanel();
        denotationsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        denotationsField = new org.wandora.application.gui.simple.SimpleField();
        denotationsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        cycTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        swConceptPanel = new javax.swing.JPanel();
        swConceptInnerPanel = new javax.swing.JPanel();
        swConceptLabel = new org.wandora.application.gui.simple.SimpleLabel();
        swConceptField = new org.wandora.application.gui.simple.SimpleField();
        swConceptGetButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        emptyPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        isaTab.setLayout(new java.awt.GridBagLayout());

        isaInnerPanel.setLayout(new java.awt.GridBagLayout());

        isaLabel.setText("<html>Fetch classes for given terms. Cyc classes are isa related terms. Please write term names below or get the context. Use comma (,) character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        isaInnerPanel.add(isaLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        isaInnerPanel.add(isaField, gridBagConstraints);

        isaGetButton.setLabel("Get context");
        isaGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        isaGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        isaGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        isaGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        isaGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                isaGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        isaInnerPanel.add(isaGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        isaTab.add(isaInnerPanel, gridBagConstraints);

        instancesTab.setLayout(new java.awt.GridBagLayout());

        instancesInnerPanel.setLayout(new java.awt.GridBagLayout());

        instancesLabel.setText("<html>Fetch instances for given terms. Please write term names below or get the context. Use comma (,) character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        instancesInnerPanel.add(instancesLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        instancesInnerPanel.add(instancesField, gridBagConstraints);

        instancesGetButton.setLabel("Get context");
        instancesGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        instancesGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        instancesGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        instancesGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        instancesGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                instancesGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        instancesInnerPanel.add(instancesGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        instancesTab.add(instancesInnerPanel, gridBagConstraints);

        genlsTab.setLayout(new java.awt.GridBagLayout());

        genlsInnerPanel.setLayout(new java.awt.GridBagLayout());

        genlsLabel.setText("<html>Fetch superclasses i.e. generalizations for given terms. Cyc generalizations are genls related terms. Please write term names below or get the context. Use comma (,) character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        genlsInnerPanel.add(genlsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        genlsInnerPanel.add(genlsField, gridBagConstraints);

        genlsGetButton.setLabel("Get context");
        genlsGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        genlsGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        genlsGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        genlsGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        genlsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                genlsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        genlsInnerPanel.add(genlsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        genlsTab.add(genlsInnerPanel, gridBagConstraints);

        specsTab.setLayout(new java.awt.GridBagLayout());

        specsInnerPanel.setLayout(new java.awt.GridBagLayout());

        specsLabel.setText("<html>Fetch subclasses i.e. specializations for given terms. Please write term names below or get the context. Use comma (,) character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        specsInnerPanel.add(specsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        specsInnerPanel.add(specsField, gridBagConstraints);

        specsButton.setLabel("Get context");
        specsButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        specsButton.setMaximumSize(new java.awt.Dimension(90, 20));
        specsButton.setMinimumSize(new java.awt.Dimension(90, 20));
        specsButton.setPreferredSize(new java.awt.Dimension(90, 20));
        specsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                specsButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        specsInnerPanel.add(specsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        specsTab.add(specsInnerPanel, gridBagConstraints);

        siblingsTab.setLayout(new java.awt.GridBagLayout());

        siblingsInnerPanel.setLayout(new java.awt.GridBagLayout());

        siblingsLabel.setText("<html>Fetch siblings for given terms. Please write terms below or get the context. Use comma character (,) to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        siblingsInnerPanel.add(siblingsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        siblingsInnerPanel.add(siblingsField, gridBagConstraints);

        siblingsGetButton.setLabel("Get context");
        siblingsGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        siblingsGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        siblingsGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        siblingsGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        siblingsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                siblingsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        siblingsInnerPanel.add(siblingsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        siblingsTab.add(siblingsInnerPanel, gridBagConstraints);

        commentsTab.setLayout(new java.awt.GridBagLayout());

        commentsInnerPanel.setLayout(new java.awt.GridBagLayout());

        commentsLabel.setText("<html>Fetch comments for given terms. Please write terms below or get the context. Use comma (,) character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        commentsInnerPanel.add(commentsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        commentsInnerPanel.add(commentsField, gridBagConstraints);

        commentsGetButton.setLabel("Get context");
        commentsGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        commentsGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        commentsGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        commentsGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        commentsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                commentsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        commentsInnerPanel.add(commentsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        commentsTab.add(commentsInnerPanel, gridBagConstraints);

        denotationsTab.setLayout(new java.awt.GridBagLayout());

        denotationsInnerPanel.setLayout(new java.awt.GridBagLayout());

        denotationsLabel.setText("<html>Fetch denotations for given terms. Please write terms below or get the context. Use comma (,) character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        denotationsInnerPanel.add(denotationsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        denotationsInnerPanel.add(denotationsField, gridBagConstraints);

        denotationsGetButton.setLabel("Get context");
        denotationsGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        denotationsGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        denotationsGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        denotationsGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        denotationsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                denotationsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        denotationsInnerPanel.add(denotationsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        denotationsTab.add(denotationsInnerPanel, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        swConceptPanel.setLayout(new java.awt.GridBagLayout());

        swConceptInnerPanel.setLayout(new java.awt.GridBagLayout());

        swConceptLabel.setText("<html>Extract information about CYC concepts. Please write concept names below or get context names. Use comma (,) character to separate names. Notice, a name of CYC concept is in camel case, for example 'Cat', 'IsaacNewton'.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        swConceptInnerPanel.add(swConceptLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        swConceptInnerPanel.add(swConceptField, gridBagConstraints);

        swConceptGetButton.setLabel("Get context");
        swConceptGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        swConceptGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        swConceptGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        swConceptGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        swConceptGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                swConceptGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        swConceptInnerPanel.add(swConceptGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        swConceptPanel.add(swConceptInnerPanel, gridBagConstraints);

        cycTabbedPane.addTab("Concept", swConceptPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(cycTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        emptyPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 252, Short.MAX_VALUE)
        );
        emptyPanelLayout.setVerticalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(emptyPanel, gridBagConstraints);

        okButton.setText("Extract");
        okButton.setPreferredSize(new java.awt.Dimension(75, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void commentsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_commentsGetButtonMouseReleased
    commentsField.setText(getContextAsString());
}//GEN-LAST:event_commentsGetButtonMouseReleased

private void denotationsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_denotationsGetButtonMouseReleased
    denotationsField.setText(getContextAsString());
}//GEN-LAST:event_denotationsGetButtonMouseReleased

private void siblingsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_siblingsGetButtonMouseReleased
    siblingsField.setText(getContextAsString());
}//GEN-LAST:event_siblingsGetButtonMouseReleased

private void isaGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_isaGetButtonMouseReleased
    isaField.setText(getContextAsString());
}//GEN-LAST:event_isaGetButtonMouseReleased

private void genlsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_genlsGetButtonMouseReleased
    genlsField.setText(getContextAsString());
}//GEN-LAST:event_genlsGetButtonMouseReleased

private void specsButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_specsButtonMouseReleased
    specsField.setText(getContextAsString());
}//GEN-LAST:event_specsButtonMouseReleased

private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
    accepted = true;
    setVisible(false);
}//GEN-LAST:event_okButtonMouseReleased

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    accepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void instancesGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_instancesGetButtonMouseReleased
    instancesField.setText(getContextAsString());
}//GEN-LAST:event_instancesGetButtonMouseReleased

    private void swConceptGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_swConceptGetButtonMouseReleased
        swConceptField.setText(getContextAsString());
    }//GEN-LAST:event_swConceptGetButtonMouseReleased




    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField commentsField;
    private javax.swing.JButton commentsGetButton;
    private javax.swing.JPanel commentsInnerPanel;
    private javax.swing.JLabel commentsLabel;
    private javax.swing.JPanel commentsTab;
    private javax.swing.JTabbedPane cycTabbedPane;
    private javax.swing.JTextField denotationsField;
    private javax.swing.JButton denotationsGetButton;
    private javax.swing.JPanel denotationsInnerPanel;
    private javax.swing.JLabel denotationsLabel;
    private javax.swing.JPanel denotationsTab;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JTextField genlsField;
    private javax.swing.JButton genlsGetButton;
    private javax.swing.JPanel genlsInnerPanel;
    private javax.swing.JLabel genlsLabel;
    private javax.swing.JPanel genlsTab;
    private javax.swing.JTextField instancesField;
    private javax.swing.JButton instancesGetButton;
    private javax.swing.JPanel instancesInnerPanel;
    private javax.swing.JLabel instancesLabel;
    private javax.swing.JPanel instancesTab;
    private javax.swing.JTextField isaField;
    private javax.swing.JButton isaGetButton;
    private javax.swing.JPanel isaInnerPanel;
    private javax.swing.JLabel isaLabel;
    private javax.swing.JPanel isaTab;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField siblingsField;
    private javax.swing.JButton siblingsGetButton;
    private javax.swing.JPanel siblingsInnerPanel;
    private javax.swing.JLabel siblingsLabel;
    private javax.swing.JPanel siblingsTab;
    private javax.swing.JButton specsButton;
    private javax.swing.JTextField specsField;
    private javax.swing.JPanel specsInnerPanel;
    private javax.swing.JLabel specsLabel;
    private javax.swing.JPanel specsTab;
    private javax.swing.JTextField swConceptField;
    private javax.swing.JButton swConceptGetButton;
    private javax.swing.JPanel swConceptInnerPanel;
    private javax.swing.JLabel swConceptLabel;
    private javax.swing.JPanel swConceptPanel;
    // End of variables declaration//GEN-END:variables

}
