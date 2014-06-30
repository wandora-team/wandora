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
 * AudioScrobblerExtractor.java
 *
 * Created on 21. toukokuuta 2008, 13:28
 */

package org.wandora.application.tools.extractors.audioscrobbler;

import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;




/**
 *
 * @author  akivela
 */


public class AudioScrobblerExtractorSelector extends JDialog {

    public static String BASE_URL = "http://ws.audioscrobbler.com/1.0/";
    private Wandora admin = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    
    /** Creates new form AudioScrobblerExtractor */
    public AudioScrobblerExtractorSelector(Wandora admin) {
        super(admin, true);
        setSize(450,300);
        setTitle("Audioscrobbler extractors");
        admin.centerWindow(this);
        this.admin = admin;
        accepted = false;
        initComponents();
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
        Component component = scrobblerTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        
        // ***** ALBUM INFO *****
        if(albumInfoPanel.equals(component)) {
            String artist = albumInfoArtistField.getText();
            String album = albumInfoAlbumField.getText();
            if(artist == null || artist.length() == 0) {
                parentTool.log("No artist name given.");
                return null;
            }
            if(album == null || album.length() == 0) {
                parentTool.log("No album name given.");
                return null;
            }
            String[] artists=urlEncode(commaSplitter(artist));
            String[] albums=urlEncode(commaSplitter(album));

            String[] urls=completeString(BASE_URL+"album/__1__/__2__/info.xml",artists,albums);

            AlbumInfoExtractor aie = new AlbumInfoExtractor();
            aie.setForceUrls( urls );
//            aie.setForceUrls( new String[] { BASE_URL+"album/"+urlEncode(artist)+"/"+urlEncode(album)+"/info.xml" } );
            wt = aie;
        }
        
        // ***** SIMILAR ARTISTS *****
        else if(similarArtistsPanel.equals(component)) {
            String artist = similarArtistsField.getText();
            if(artist == null || artist.length() == 0) {
                parentTool.log("No artist name given.");
                return null;
            }
            String[] artists = urlEncode(commaSplitter(artist));
            String[] artistUrls = completeString(BASE_URL+"artist/__1__/similar.xml", artists);
            
            ArtistRelatedArtistsExtractor arae = new ArtistRelatedArtistsExtractor();
            arae.setForceUrls(artistUrls);
            wt = arae;
        }
        
        // ***** OVERALL TOP TAGS *****
        else if(topTagPanel.equals(component)) {
            TagTopTagsExtractor ex = new TagTopTagsExtractor();
            ex.setForceUrls( new String[] { BASE_URL+"tag/toptags.xml" } );
            wt = ex;
        }
        
        // ***** TOP ALBUMS WITH GIVEN TAG *******
        else if(topAlbumsWTag.equals(component)) {
            String tag = topAlbumsWTagField.getText();
            
            String[] tags = urlEncode(commaSplitter(tag));
            String[] tagUrls = completeString(BASE_URL+"tag/__1__/topalbums.xml", tags);
            
            TagTopAlbumsExtractor ex = new TagTopAlbumsExtractor();
            ex.setForceUrls( tagUrls );
            wt = ex;
        }
        
        // ***** TOP ARTISTS WITH GIVEN TAG *******
        else if(topArtistsWTag.equals(component)) {
            String tag = topArtistWTagField.getText();
            
            String[] tags = urlEncode(commaSplitter(tag));
            String[] tagUrls = completeString(BASE_URL+"tag/__1__/topartists.xml", tags);

            TagTopArtistsExtractor ex = new TagTopArtistsExtractor();
            ex.setForceUrls(tagUrls);
            wt = ex;
        }
        
        // ***** ARTIST'S TOP ALBUMS *******
        else if(artistsTopAlbumsPanel.equals(component)) {
            String artist = artistsTopAlbumsField.getText();
            
            String[] artists = urlEncode(commaSplitter(artist));
            String[] artistUrls = completeString(BASE_URL+"artist/__1__/topalbums.xml", artists);
            
            ArtistTopAlbumsExtractor ex = new ArtistTopAlbumsExtractor();
            ex.setForceUrls(artistUrls);
            wt = ex;
        }
        
        // ***** ARTIST'S TOP TRACKS *******
        else if(artistsTopTracksPanel.equals(component)) {
            String artist = artistsTopTracksField.getText();
            
            String[] artists = urlEncode(commaSplitter(artist));
            String[] artistUrls = completeString(BASE_URL+"artist/__1__/toptracks.xml", artists);
            
            ArtistTopTracksExtractor ex = new ArtistTopTracksExtractor();
            ex.setForceUrls(artistUrls);
            wt = ex;
        }
        
        // ***** ARTIST'S TOP TAGS *******
        else if(artistsTopTagsPanel.equals(component)) {
            String artist = artistsTopTagsField.getText();
            
            String[] artists = urlEncode(commaSplitter(artist));
            String[] artistUrls = completeString(BASE_URL+"artist/__1__/toptags.xml", artists);
            
            ArtistTopTagsExtractor ex = new ArtistTopTagsExtractor();
            ex.setForceUrls(artistUrls);
            wt = ex;
        }
        
        return wt;
    }
    
    
    
    
    
    
    
    public String[] commaSplitter(String str) {
        ArrayList<String> strList=new ArrayList<String>();
        int startPos=0;
        for(int i=0;i<str.length()+1;i++){
            if(i<str.length()-1 && str.charAt(i)==',' && str.charAt(i+1)==','){
                i++;
                continue;
            }
            if(i==str.length() || str.charAt(i)==','){
                String s=str.substring(startPos,i).trim().replace(",,", ",");
                if(s.length()>0) strList.add(s);
                startPos=i+1;
            }
        }
        return strList.toArray(new String[strList.size()]);
/*
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
        */
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
    
    public String getContextArtistsAsString() {
        StringBuffer sb = new StringBuffer("");

        Topic albumType=null;
        Topic artistType=null;

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

                        if(albumType==null || artistType==null){
                            albumType=t.getTopicMap().getTopic(AbstractAudioScrobblerExtractor.ALBUM_SI);
                            artistType=t.getTopicMap().getTopic(AbstractAudioScrobblerExtractor.ARTIST_SI);
                            if(albumType==null || artistType==null) break;
                        }

                        Topic artist=null;
                        for(Association a : t.getAssociations(albumType, albumType)){
                            artist=a.getPlayer(artistType);
                            if(artist!=null) break;
                        }

                        if(artist!=null){
                            str = artist.getDisplayName(AbstractAudioScrobblerExtractor.LANG);
                            if(str != null) {
                                str = str.trim();
                            }
                        }
                    }

                    if(str != null && str.length() > 0) {
                        sb.append(escapeCommas(str));
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
    
    
    public String getContextAsString() {
        StringBuffer sb = new StringBuffer("");
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
                        str = t.getDisplayName(AbstractAudioScrobblerExtractor.LANG);
                        if(str != null) {
                            str = str.trim();
                        }
                    }
                    
                    if(str != null && str.length() > 0) {
                        sb.append(escapeCommas(str));
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

    private String escapeCommas(String s){
        return s.replace(",", ",,");
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

        scrobblerTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        topTagPanel = new javax.swing.JPanel();
        topTagInnerPanel = new javax.swing.JPanel();
        overallTopTagsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        topAlbumsWTag = new javax.swing.JPanel();
        topAlbumsWTagInnerPanel = new javax.swing.JPanel();
        topAlbumsWTagLabel = new org.wandora.application.gui.simple.SimpleLabel();
        topAlbumsWTagField = new org.wandora.application.gui.simple.SimpleField();
        topAlbumsWTagGetButton = new org.wandora.application.gui.simple.SimpleButton();
        topArtistsWTag = new javax.swing.JPanel();
        topArtistWTagInnerPanel = new javax.swing.JPanel();
        topArtistWTagLabel = new org.wandora.application.gui.simple.SimpleLabel();
        topArtistWTagField = new org.wandora.application.gui.simple.SimpleField();
        topArtistsWTagGetButton = new org.wandora.application.gui.simple.SimpleButton();
        albumInfoPanel = new javax.swing.JPanel();
        albumInfoInnerPanel = new javax.swing.JPanel();
        albumInfoLabel = new org.wandora.application.gui.simple.SimpleLabel();
        albumInfoArtistLabel = new org.wandora.application.gui.simple.SimpleLabel();
        albumInfoArtistField = new org.wandora.application.gui.simple.SimpleField();
        albumInfoAlbumLabel = new org.wandora.application.gui.simple.SimpleLabel();
        albumInfoAlbumField = new org.wandora.application.gui.simple.SimpleField();
        albumInfoGetButton = new org.wandora.application.gui.simple.SimpleButton();
        similarArtistsPanel = new javax.swing.JPanel();
        similarArtistsInnerPanel = new javax.swing.JPanel();
        similarArtistsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        similarArtistsField = new org.wandora.application.gui.simple.SimpleField();
        similarArtistsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        artistsTopAlbumsPanel = new javax.swing.JPanel();
        artistsTopAlbumsInnerPanel = new javax.swing.JPanel();
        artistsTopAlbumsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        artistsTopAlbumsField = new org.wandora.application.gui.simple.SimpleField();
        artistsTopAlbumsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        artistsTopTagsPanel = new javax.swing.JPanel();
        artistsTopTagsInnerPanel = new javax.swing.JPanel();
        artistsTopTagsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        artistsTopTagsField = new org.wandora.application.gui.simple.SimpleField();
        artistsTopTagsGetButton = new org.wandora.application.gui.simple.SimpleButton();
        artistsTopTracksPanel = new javax.swing.JPanel();
        artistsTopTracksInnerPanel = new javax.swing.JPanel();
        artistsTopTracksLabel = new org.wandora.application.gui.simple.SimpleLabel();
        artistsTopTracksField = new org.wandora.application.gui.simple.SimpleField();
        artistsTopTracksGetButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        emptyPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        topTagPanel.setLayout(new java.awt.GridBagLayout());

        topTagInnerPanel.setLayout(new java.awt.GridBagLayout());

        overallTopTagsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        overallTopTagsLabel.setText("<html>Fetch most used tags. This extractor doesn't require input.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        topTagInnerPanel.add(overallTopTagsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        topTagPanel.add(topTagInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Overall top tags", topTagPanel);

        topAlbumsWTag.setLayout(new java.awt.GridBagLayout());

        topAlbumsWTagInnerPanel.setLayout(new java.awt.GridBagLayout());

        topAlbumsWTagLabel.setText("<html>Fetch albums tagged with given keyword. Please write keywords below or get the context. Use comma (,) character to separate different keywords.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        topAlbumsWTagInnerPanel.add(topAlbumsWTagLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        topAlbumsWTagInnerPanel.add(topAlbumsWTagField, gridBagConstraints);

        topAlbumsWTagGetButton.setLabel("Get context");
        topAlbumsWTagGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        topAlbumsWTagGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        topAlbumsWTagGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        topAlbumsWTagGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        topAlbumsWTagGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                topAlbumsWTagGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        topAlbumsWTagInnerPanel.add(topAlbumsWTagGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        topAlbumsWTag.add(topAlbumsWTagInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Top albums with tag", topAlbumsWTag);

        topArtistsWTag.setLayout(new java.awt.GridBagLayout());

        topArtistWTagInnerPanel.setLayout(new java.awt.GridBagLayout());

        topArtistWTagLabel.setText("<html>Fetch artists tagged with given keyword. Please write keywords below or get the context. Use comma (,) character to separate different keywords.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        topArtistWTagInnerPanel.add(topArtistWTagLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        topArtistWTagInnerPanel.add(topArtistWTagField, gridBagConstraints);

        topArtistsWTagGetButton.setLabel("Get context");
        topArtistsWTagGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        topArtistsWTagGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        topArtistsWTagGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        topArtistsWTagGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        topArtistsWTagGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                topArtistsWTagGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        topArtistWTagInnerPanel.add(topArtistsWTagGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        topArtistsWTag.add(topArtistWTagInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Top artists with tag", topArtistsWTag);

        albumInfoPanel.setLayout(new java.awt.GridBagLayout());

        albumInfoInnerPanel.setLayout(new java.awt.GridBagLayout());

        albumInfoLabel.setText("<html>Fetch information about specific album of given artists. Please write both artist and album name below.<html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        albumInfoInnerPanel.add(albumInfoLabel, gridBagConstraints);

        albumInfoArtistLabel.setText("Artist name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        albumInfoInnerPanel.add(albumInfoArtistLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        albumInfoInnerPanel.add(albumInfoArtistField, gridBagConstraints);

        albumInfoAlbumLabel.setText("Album name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        albumInfoInnerPanel.add(albumInfoAlbumLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        albumInfoInnerPanel.add(albumInfoAlbumField, gridBagConstraints);

        albumInfoGetButton.setLabel("Get context");
        albumInfoGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        albumInfoGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        albumInfoGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        albumInfoGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        albumInfoGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                albumInfoGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        albumInfoInnerPanel.add(albumInfoGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        albumInfoPanel.add(albumInfoInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Album info", albumInfoPanel);

        similarArtistsPanel.setLayout(new java.awt.GridBagLayout());

        similarArtistsInnerPanel.setLayout(new java.awt.GridBagLayout());

        similarArtistsLabel.setText("<html>Fetch related artists for given artist name. Please write artist's name below or get the context. Use comma character (,) to separate different artist names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        similarArtistsInnerPanel.add(similarArtistsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        similarArtistsInnerPanel.add(similarArtistsField, gridBagConstraints);

        similarArtistsGetButton.setLabel("Get context");
        similarArtistsGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        similarArtistsGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        similarArtistsGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        similarArtistsGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        similarArtistsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                similarArtistsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        similarArtistsInnerPanel.add(similarArtistsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        similarArtistsPanel.add(similarArtistsInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Similar artists", similarArtistsPanel);

        artistsTopAlbumsPanel.setLayout(new java.awt.GridBagLayout());

        artistsTopAlbumsInnerPanel.setLayout(new java.awt.GridBagLayout());

        artistsTopAlbumsLabel.setText("<html>Fetch artist's top albums. Please write artist's name below or get the context. Use comma (,) character to separate different artist names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        artistsTopAlbumsInnerPanel.add(artistsTopAlbumsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        artistsTopAlbumsInnerPanel.add(artistsTopAlbumsField, gridBagConstraints);

        artistsTopAlbumsGetButton.setLabel("Get context");
        artistsTopAlbumsGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        artistsTopAlbumsGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        artistsTopAlbumsGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        artistsTopAlbumsGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        artistsTopAlbumsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                artistsTopAlbumsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        artistsTopAlbumsInnerPanel.add(artistsTopAlbumsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        artistsTopAlbumsPanel.add(artistsTopAlbumsInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Artist's top albums", artistsTopAlbumsPanel);

        artistsTopTagsPanel.setLayout(new java.awt.GridBagLayout());

        artistsTopTagsInnerPanel.setLayout(new java.awt.GridBagLayout());

        artistsTopTagsLabel.setText("<html>Fetch tags attached to the given artist. Please write artist's name below or get the context. Use comma (,) character to separate different artist names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        artistsTopTagsInnerPanel.add(artistsTopTagsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        artistsTopTagsInnerPanel.add(artistsTopTagsField, gridBagConstraints);

        artistsTopTagsGetButton.setLabel("Get context");
        artistsTopTagsGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        artistsTopTagsGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        artistsTopTagsGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        artistsTopTagsGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        artistsTopTagsGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                artistsTopTagsGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        artistsTopTagsInnerPanel.add(artistsTopTagsGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        artistsTopTagsPanel.add(artistsTopTagsInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Artist's top tags", artistsTopTagsPanel);

        artistsTopTracksPanel.setLayout(new java.awt.GridBagLayout());

        artistsTopTracksInnerPanel.setLayout(new java.awt.GridBagLayout());

        artistsTopTracksLabel.setText("<html>Fetch most popular tracks of given artist. Please write artist's name below or get the context. Use comma (,) character to separate different artist names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        artistsTopTracksInnerPanel.add(artistsTopTracksLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        artistsTopTracksInnerPanel.add(artistsTopTracksField, gridBagConstraints);

        artistsTopTracksGetButton.setLabel("Get context");
        artistsTopTracksGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        artistsTopTracksGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        artistsTopTracksGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        artistsTopTracksGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        artistsTopTracksGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                artistsTopTracksGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        artistsTopTracksInnerPanel.add(artistsTopTracksGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        artistsTopTracksPanel.add(artistsTopTracksInnerPanel, gridBagConstraints);

        scrobblerTabbedPane.addTab("Artist's top tracks", artistsTopTracksPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(scrobblerTabbedPane, gridBagConstraints);
        scrobblerTabbedPane.getAccessibleContext().setAccessibleName("audioscrobbler");

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        emptyPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 219, Short.MAX_VALUE)
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
    }// </editor-fold>//GEN-END:initComponents

private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
    accepted = true;
    setVisible(false);
}//GEN-LAST:event_okButtonMouseReleased

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    accepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void topAlbumsWTagGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topAlbumsWTagGetButtonMouseReleased
    topAlbumsWTagField.setText(getContextAsString());
}//GEN-LAST:event_topAlbumsWTagGetButtonMouseReleased

private void topArtistsWTagGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topArtistsWTagGetButtonMouseReleased
    topArtistWTagField.setText(getContextAsString());
}//GEN-LAST:event_topArtistsWTagGetButtonMouseReleased

private void artistsTopTracksGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_artistsTopTracksGetButtonMouseReleased
    artistsTopTracksField.setText(getContextAsString());
}//GEN-LAST:event_artistsTopTracksGetButtonMouseReleased

private void artistsTopTagsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_artistsTopTagsGetButtonMouseReleased
    artistsTopTagsField.setText(getContextAsString());
}//GEN-LAST:event_artistsTopTagsGetButtonMouseReleased

private void artistsTopAlbumsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_artistsTopAlbumsGetButtonMouseReleased
    artistsTopAlbumsField.setText(getContextAsString());
}//GEN-LAST:event_artistsTopAlbumsGetButtonMouseReleased

private void similarArtistsGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_similarArtistsGetButtonMouseReleased
    similarArtistsField.setText(getContextAsString());
}//GEN-LAST:event_similarArtistsGetButtonMouseReleased

private void albumInfoGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_albumInfoGetButtonMouseReleased
    albumInfoArtistField.setText(getContextArtistsAsString());
    albumInfoAlbumField.setText(getContextAsString());
}//GEN-LAST:event_albumInfoGetButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField albumInfoAlbumField;
    private javax.swing.JLabel albumInfoAlbumLabel;
    private javax.swing.JTextField albumInfoArtistField;
    private javax.swing.JLabel albumInfoArtistLabel;
    private javax.swing.JButton albumInfoGetButton;
    private javax.swing.JPanel albumInfoInnerPanel;
    private javax.swing.JLabel albumInfoLabel;
    private javax.swing.JPanel albumInfoPanel;
    private javax.swing.JTextField artistsTopAlbumsField;
    private javax.swing.JButton artistsTopAlbumsGetButton;
    private javax.swing.JPanel artistsTopAlbumsInnerPanel;
    private javax.swing.JLabel artistsTopAlbumsLabel;
    private javax.swing.JPanel artistsTopAlbumsPanel;
    private javax.swing.JTextField artistsTopTagsField;
    private javax.swing.JButton artistsTopTagsGetButton;
    private javax.swing.JPanel artistsTopTagsInnerPanel;
    private javax.swing.JLabel artistsTopTagsLabel;
    private javax.swing.JPanel artistsTopTagsPanel;
    private javax.swing.JTextField artistsTopTracksField;
    private javax.swing.JButton artistsTopTracksGetButton;
    private javax.swing.JPanel artistsTopTracksInnerPanel;
    private javax.swing.JLabel artistsTopTracksLabel;
    private javax.swing.JPanel artistsTopTracksPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel overallTopTagsLabel;
    private javax.swing.JTabbedPane scrobblerTabbedPane;
    private javax.swing.JTextField similarArtistsField;
    private javax.swing.JButton similarArtistsGetButton;
    private javax.swing.JPanel similarArtistsInnerPanel;
    private javax.swing.JLabel similarArtistsLabel;
    private javax.swing.JPanel similarArtistsPanel;
    private javax.swing.JPanel topAlbumsWTag;
    private javax.swing.JTextField topAlbumsWTagField;
    private javax.swing.JButton topAlbumsWTagGetButton;
    private javax.swing.JPanel topAlbumsWTagInnerPanel;
    private javax.swing.JLabel topAlbumsWTagLabel;
    private javax.swing.JTextField topArtistWTagField;
    private javax.swing.JPanel topArtistWTagInnerPanel;
    private javax.swing.JLabel topArtistWTagLabel;
    private javax.swing.JPanel topArtistsWTag;
    private javax.swing.JButton topArtistsWTagGetButton;
    private javax.swing.JPanel topTagInnerPanel;
    private javax.swing.JPanel topTagPanel;
    // End of variables declaration//GEN-END:variables

}
