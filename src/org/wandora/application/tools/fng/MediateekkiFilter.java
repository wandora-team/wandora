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
 * MediateekkiFilter.java
 *
 * Created on 12. huhtikuuta 2005, 19:29
 */

package org.wandora.application.tools.fng;


import org.wandora.utils.ClipboardBox;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.utils.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;



/**
 *
 * @author akivela
 */
public class MediateekkiFilter extends AbstractWandoraTool implements WandoraTool {
    
    
    public boolean quiet = false;
    
    public void execute(Wandora admin, Context context) {
        TopicMap topicMap = admin.getTopicMap();
        execute(topicMap, admin);
    }
        
    
    
    public void execute(TopicMap topicMap, Wandora admin) {
        try {
            if(topicMap != null) {
                
                Topic videoTypeTopic = topicMap.getTopic("http://www.gripstudios.com/applications/fng/media-archive/videotype");
                Topic licenseTypeTopic = topicMap.getTopic("http://www.gripstudios.com/applications/fng/media-archive/licensetype");
                Topic vaticodeType = topicMap.getTopic("http://www.gripstudios.com/applications/fng/media-archive/vaticode");
                Topic inHouseLicense = topicMap.getTopic("http://www.gripstudios.com/applications/fng/media-archive/license/in-house_use");
                Topic videoFileTypeTopic = topicMap.getTopic("http://www.gripstudios.com/applications/fng/media-archive/videooccurrencetype");
                Topic numberOfCopiesTopic = topicMap.getTopic("http://www.gripstudios.com/applications/fng/media-archive/numberofcopies");
                
                if(videoFileTypeTopic == null) { if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no video file type topic with SI\nhttp://www.gripstudios.com/applications/fng/media-archive/videooccurrencetype", "Found no video file type topic!", JOptionPane.ERROR_MESSAGE); return; }
                if(videoTypeTopic == null) { if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no video type topic with SI\nhttp://www.gripstudios.com/applications/fng/media-archive/videotype", "Found no video type topic!", JOptionPane.ERROR_MESSAGE); return; }
                if(licenseTypeTopic == null) { if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no license type topic with SI\nhttp://www.gripstudios.com/applications/fng/media-archive/licensetype", "Found no license type topic!", JOptionPane.ERROR_MESSAGE); return; }
                if(vaticodeType == null) { if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no vati code type topic with SI\nhttp://www.gripstudios.com/applications/fng/media-archive/vaticode", "Found no vaticode type topic!", JOptionPane.ERROR_MESSAGE); return; }
                if(inHouseLicense == null) { if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no in-house license type topic with SI\nhttp://www.gripstudios.com/applications/fng/media-archive/in-house use", "Found no license type topic!", JOptionPane.ERROR_MESSAGE); return; }
                
                Topic t = null;
                Vector<Topic> deleteThese = new Vector();
                Collection videoTopics = topicMap.getTopicsOfType(videoTypeTopic);
                if(videoTopics != null && videoTopics.size() > 0) {
                    Iterator iter = videoTopics.iterator();
                    while(iter.hasNext()) {
                        t=(Topic)iter.next();
                        Collection videoFiles = getPlayers(t, videoFileTypeTopic, videoFileTypeTopic);
                        boolean hasVideoFile = videoFiles != null && videoFiles.size() > 0;
                        Collection licenses = getPlayers(t, licenseTypeTopic, licenseTypeTopic);
                        boolean inHouseLicence = licenses.contains(inHouseLicense);
                        String vaticode = t.getData(vaticodeType, "fi");
                        boolean hasVaticode = vaticode != null && vaticode.length() > 0;

                        if(!(inHouseLicence && hasVaticode && hasVideoFile)) {
                            deleteThese.add(t);
                        }
                    }

                    int count = deleteThese.size();
                    if(count > 0) {
                        int a = 0;
                        if(!quiet && admin != null) {
                            String confirmMessage = "Found " + count + " video topics without proper in-house license and vati code and video occurrence topic!\nPress Delete to delete topics.\nPress Copy to copy SIs to clipboard.";
                            String[] options = new String[] { "Delete", "Copy", "Cancel" };
                            a = JOptionPane.showOptionDialog(admin, confirmMessage,"Delete or copy", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1] );
                        }
                        if(quiet || a == JOptionPane.YES_OPTION) {
                            for(int i=0; i<count; i++) {
                                t = deleteThese.elementAt(i);
                                if(t != null) t.remove();
                            }
                            
                            // **** REMOVE LICENCE TOPICS! **** 
                            try { inHouseLicense.remove(); } catch (Exception e) {}
                            try { licenseTypeTopic.remove(); } catch (Exception e) {}
                            

                            // **** REMOVE VATI CODE & NUMBER OF COPIES OCCURRENCES! **** 
                            Vector<Topic> deleteVaticode = new Vector();
                            Collection vatiTopics = topicMap.getTopicsOfType(videoTypeTopic);
                            if(vatiTopics != null && vatiTopics.size() > 0) {
                                Iterator iterVati = vatiTopics.iterator();
                                while(iterVati.hasNext()) {
                                    t=(Topic)iterVati.next();
                                    if(t != null && !t.isRemoved()) deleteVaticode.add(t);
                                }
                            }
                            for(int i=0; i<deleteVaticode.size(); i++) {
                                t = deleteVaticode.elementAt(i);
                                if(t != null) {
                                    t.removeData(vaticodeType);
                                    t.removeData(numberOfCopiesTopic);
                                }
                            }
                            try { vaticodeType.remove(); } catch (Exception e) {}
                            try { numberOfCopiesTopic.remove(); } catch (Exception e) {}
                        
                            if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Total " + count + " topics deleted!", "Topics deleted", JOptionPane.INFORMATION_MESSAGE);
                        }
                        if(!quiet && a == JOptionPane.NO_OPTION) {
                            StringBuffer sb = new StringBuffer("");
                            for(int i=0; i<count; i++) {
                                t = deleteThese.elementAt(i);
                                if(t != null) sb.append(t.getOneSubjectIdentifier().toExternalForm() + "\n");
                            }
                            ClipboardBox.setClipboard(sb.toString());
                            JOptionPane.showMessageDialog(admin, "Topics' SIs were copied to clipboard for future use!", "Topic SIs copied", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    else {
                        if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no topics without proper in-house license and vati code and video occurrence topic!", "No topics to delete", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else {
                    if(!quiet && admin != null) JOptionPane.showMessageDialog(admin, "Found no topics without proper in-house license and vati code and video occurrence topic!", "No topics to delete", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public Vector getPlayers(Topic topic, Topic type, Topic role)  throws TopicMapException {
        Vector players = new Vector();
        
        if(type != null && role != null) {
            Collection associations = topic.getAssociations(type);
            for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                try {
                    Association a = (Association) iter.next();
                    if(a.getPlayer(role) != null) players.add(a.getPlayer(role));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    
    

    
    
    
    public String getName() {
        return "Mediateekki Filter (Kiasma Media-Archive)";
    }
    
}