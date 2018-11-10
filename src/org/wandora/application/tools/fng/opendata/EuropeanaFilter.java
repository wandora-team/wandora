/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2017 Wandora Team
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
 */
package org.wandora.application.tools.fng.opendata;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicTools;

/**
 * Postprocess Finnish National Gallery topic map for Europeana.
 * Tool removes copyrighted artwork images, copyrighted artworks and
 * artworks without images. Resulting topic map can exported with
 * FngOpenDataDublinCoreExporter for Europeana.
 * 
 *
 * @author akivela
 */
public class EuropeanaFilter extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public EuropeanaFilter() {
        
    }
    
    
    @Override
    public String getName() {
        return "FNG Europeana filter";
    }

    @Override
    public String getDescription() {
        return "Postprocess Finnish National Gallery topic maps for Europeana. "
                + "Removes copyrighted images and artists.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap tm = wandora.getTopicMap();
        
        setDefaultLogger();
        
        Topic artworkType = tm.getTopic("http://wandora.org/si/fng/artwork");
        Topic authorType = tm.getTopic("http://wandora.org/si/fng/author");
        Topic authorRole = tm.getTopic("http://wandora.org/si/fng/author-role");
        Topic artistType = tm.getTopic("http://wandora.org/si/fng/artists");
        Topic imageType = tm.getTopic("http://wandora.org/si/fng/imageoccurrence");
        Topic imageCreditType = tm.getTopic("http://kansallisgalleria.fi/P3_has_note_creditline");
        Topic langTopic = tm.getTopic("http://wandora.org/si/fng/core/langindependent");
        Topic personDeathType = tm.getTopic("http://wandora.org/si/fng/person_death");
        Topic timeType = tm.getTopic("http://wandora.org/si/fng/time");
        
        
        if(imageType != null && imageCreditType != null && langTopic != null) {
            log("Removing images with copyrights...");
            Collection<Topic> images = tm.getTopicsOfType(imageType);
            Set<Topic> imagesToBeRemoved = new LinkedHashSet<Topic>();
            for( Topic image : images ) {
                if(image != null) {
                    boolean removeImage = true;
                    String credits = image.getData(imageCreditType, langTopic);
                    if(credits != null) {
                        credits = credits.trim().toLowerCase();
                        if(credits.startsWith("vtm") 
                                || credits.startsWith("kka") 
                                || credits.startsWith("fng") 
                                || credits.startsWith("kansallisgalleria")
                                || credits.startsWith("mykk�nen, pirje")) {
                            removeImage = false;
                        }
                    }
                    if(removeImage) {
                        imagesToBeRemoved.add(image);
                    }
                }
            }

            for( Topic image : imagesToBeRemoved ) {
                image.remove();
            }
            log("Removed "+imagesToBeRemoved.size()+" image topics.");
        }
        else {
            log("Missing topics. Can't remove copyrighted image topics.");
        }
        
        
        if(artistType != null && personDeathType != null && timeType != null) {
            log("Removing artists with copyrights...");
            Collection<Topic> artists = tm.getTopicsOfType(artistType);
            Set<Topic> artistsToBeRemoved = new LinkedHashSet<Topic>();
            for( Topic artist : artists ) {
                if(artist != null) {
                    boolean removeArtist = true;
                    Collection<Topic> artistDeathTimes = TopicTools.getPlayers(artist, personDeathType, timeType);
                    if(artistDeathTimes != null && !artistDeathTimes.isEmpty()) {
                        for(Topic deathTimeTopic : artistDeathTimes) {
                            String deathTime = deathTimeTopic.getBaseName();
                            if(deathTime != null) {
                                deathTime = deathTime.trim();
                                if(deathTime.startsWith("(")) {
                                    deathTime = deathTime.substring(1);
                                }
                                if(deathTime.startsWith("[")) {
                                    deathTime = deathTime.substring(1);
                                }
                                if(deathTime.startsWith("n. ")) {
                                    deathTime = deathTime.substring(3);
                                }

                                deathTime = deathTime.substring(0,Math.min(deathTime.length(), 4));
                                try {
                                    int deathYear = Integer.parseInt(deathTime);
                                    if(deathYear > 1000 && deathYear < 1947) {
                                        removeArtist = false;
                                        break;
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(removeArtist) {
                        artistsToBeRemoved.add(artist);
                    }
                }
            }
            for( Topic artist : artistsToBeRemoved ) {
                artist.remove();
            }
            log("Removed "+artistsToBeRemoved.size()+" artist topics.");
        }
        else {
            log("Missing topics. Can't remove copyrighted artists.");
        }
        
        
        
        if(artworkType != null && imageType != null && authorType != null && authorRole != null && artistType != null) {
            log("Removing artworks without images and artists...");
            Collection<Topic> artworks = tm.getTopicsOfType(artworkType);
            Set<Topic> artworksToBeRemoved = new LinkedHashSet<Topic>();
            for( Topic artwork : artworks ) {
                if(artwork != null) {
                    boolean removeArtwork = false;
                    Collection<Association> artworkImages = artwork.getAssociations(imageType);
                    if(artworkImages == null || artworkImages.isEmpty()) {
                        removeArtwork = true;
                    }

                    if(!removeArtwork) {
                        Collection<Topic> artistAuthors = TopicTools.getPlayers(artwork, authorType, artworkType, authorRole, artistType);
                        if(artistAuthors == null || artistAuthors.isEmpty()) {
                            removeArtwork = true;
                        }
                    }

                    if(removeArtwork) {
                        artworksToBeRemoved.add(artwork);
                    }
                }
            }

            for( Topic artwork : artworksToBeRemoved ) {
                artwork.remove();
            }
            log("Removed "+artworksToBeRemoved.size()+" artwork topics.");
        }
        else {
            log("Missing topics. Can't remove artworks without images and artists.");
        }
        
        
        if(artistType != null && authorType != null) {
            log("Removing artists without artworks...");
            Collection<Topic> artists = tm.getTopicsOfType(artistType);
            Set<Topic> artistsToBeRemoved = new LinkedHashSet<Topic>();
            for( Topic artist : artists ) {
                if(artist != null) {
                    boolean removeArtist = true;
                    Collection<Association> artistArtworks = artist.getAssociations(authorType);
                    if(artistArtworks != null && !artistArtworks.isEmpty()) {
                        removeArtist = false;
                    }

                    if(removeArtist) {
                        artistsToBeRemoved.add(artist);
                    }
                }
            }
            for( Topic artist : artistsToBeRemoved ) {
                artist.remove();
            }
            log("Removed "+artistsToBeRemoved.size()+" artist topics.");
        }
        else {
            log("Missing topics. Can't remove artists without artworks.");
        }
        
        log("Ready.");
        setState(WAIT);
    }
}
