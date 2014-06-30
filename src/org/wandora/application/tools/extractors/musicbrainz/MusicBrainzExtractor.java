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
 */

package org.wandora.application.tools.extractors.musicbrainz;



import java.io.*;
import java.net.*;
import java.util.*;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;

import org.musicbrainz.*;
import org.musicbrainz.model.*;
import org.musicbrainz.webservice.includes.*;
import org.musicbrainz.webservice.filter.*;
import org.musicbrainz.wsxml.element.*;


/**
 * 
 * Uses MusicBrainz Java library found at
 * http://svn.musicbrainz.org/libmusicbrainz-java/trunk/
 *
 * @author akivela
 */
public class MusicBrainzExtractor extends AbstractMusicBrainzExtractor {

    public static final String MBID_PATTERN = "\\w{8}?\\-\\w{4}?\\-\\w{4}?\\-\\w{4}?\\-\\w+?";


    @Override
    public String getName() {
        return "MusicBrainz extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert MusicBrainz data to a Topic Map";
    }

    @Override
    public boolean isConfigurable(){
        return false;
    }



    public void execute(Wandora wandora, Context context) {
        try {
            MusicBrainzExtractorSelector selector = new MusicBrainzExtractorSelector(wandora);
            selector.setVisible(true);
            setDefaultLogger();
            setProgress(0);
            if(selector.getAccepted()) {
                TopicMap tm = wandora.getTopicMap();
                if(selector.getSelectedTab() == MusicBrainzExtractorSelector.ARTIST_TAB) {
                    makeArtistQuery(selector, tm);
                }
                else if(selector.getSelectedTab() == MusicBrainzExtractorSelector.RELEASES_TAB) {
                    makeReleasesQuery(selector, tm);
                }
                else if(selector.getSelectedTab() == MusicBrainzExtractorSelector.TRACKS_TAB) {
                    makeTracksQuery(selector, tm);
                }
                else {
                    log("Unknown tab in extractor selector!");
                }
            }
            else {
                log("User cancelled the extraction.");
            }
            log("Done.");
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }



    // -------------------------------------------------------------------------


    

    public void makeArtistQuery(MusicBrainzExtractorSelector selector, TopicMap tm) {
        try {
            ArtistIncludes artistIncludes = new ArtistIncludes();
            artistIncludes.setTrackRelations(true);
            artistIncludes.setReleaseRelations(true);
            //artistIncludes.setArtistRelations(true);
            //artistIncludes.setUrlRelations(true);

            Query q = new Query();
            String artistQuery = selector.getArtists();
            if(artistQuery.matches(MBID_PATTERN)) {
                log("Found MBID. Getting artist data with MBID '"+artistQuery+"'");
                Artist artist = q.getArtistById(artistQuery, artistIncludes);
                getArtistTopic(tm, artist);

                System.out.println("artist.getName() == "+artist.getName());
                System.out.println("artist.getSortName() == "+artist.getSortName());
                System.out.println("artist.getDisambiguation() == "+artist.getDisambiguation());
                System.out.println("artist.getBeginDate() == "+artist.getBeginDate());
                System.out.println("artist.getEndDate() == "+artist.getEndDate());
                System.out.println("artist.getType() == "+artist.getType());
                System.out.println("artist.getAliases() == "+artist.getAliases());
                System.out.println("artist.getReleases() == "+artist.getReleases());
            }
            else {
                log("Searching artists with query '"+artistQuery+"'.");
                int page = selector.getPage();
                boolean getArtistDetails = selector.getAdditionalArtistInfo();
                ArtistFilter artistFilter = new ArtistFilter(100, (page-1)*100, artistQuery);
                ArtistSearchResults artists = q.getArtists(artistFilter);
                List<ArtistResult> results = artists.getArtistResults();
                log("Found total "+results.size()+" artists matching the query. Notice the maximum number of artists returned by MusicBrainz is 100. Use paging to retrieve more artists.");
                int i=1;
                for( ArtistResult result : results ) {
                    if(forceStop()) break;
                    setProgress(i);
                    Artist artist = result.getArtist();
                    int score = result.getScore();
                    log("Found MusicBrainz artist ("+i+") '"+artist.getName()+"' with score "+score);
                    if(getArtistDetails) {
                        Artist detailedArtist = q.getArtistById(artist.getId(), artistIncludes);
                        getArtistTopic(tm, detailedArtist);
                        takeNap();
                    }
                    else {
                        getArtistTopic(tm, artist);
                    }
                    i++;
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }





    public void makeReleasesQuery(MusicBrainzExtractorSelector selector, TopicMap tm) {
        try {
            ReleaseIncludes releaseIncludes = new ReleaseIncludes();
            releaseIncludes.setArtist(true);
            releaseIncludes.setArtistRelations(true);
            releaseIncludes.setCounts(true);
            releaseIncludes.setDiscs(true);
            releaseIncludes.setReleaseEvents(true);
            releaseIncludes.setReleaseRelations(true);
            releaseIncludes.setTrackRelations(true);
            releaseIncludes.setTracks(true);
            releaseIncludes.setUrlRelations(true);

            Query q = new Query();
            String releasesQuery = selector.getReleases();

            if(releasesQuery.matches(MBID_PATTERN)) {
                log("Found MBID. Getting release data with MBID '"+releasesQuery+"'");
                Release release = q.getReleaseById(releasesQuery, releaseIncludes);
                getReleaseTopic(tm, release);

                System.out.println("release.getTitle() == "+release.getTitle());
                System.out.println("release.getArtist() == "+release.getArtist());
                System.out.println("release.getAsin() == "+release.getAsin());
                System.out.println("release.getTextLanguage() == "+release.getTextLanguage());
                System.out.println("release.getTextScript() == "+release.getTextScript());
                System.out.println("release.getTypes() == "+release.getTypes());
                System.out.println("release.getDiscList() == "+release.getDiscList());
                System.out.println("release.getReleaseEventList() == "+release.getReleaseEventList());
                System.out.println("release.getTrackList() == "+release.getTrackList());
                System.out.println("release.getEarliestReleaseDate() == "+release.getEarliestReleaseDate());
                System.out.println("release.getEarliestReleaseEvent() == "+release.getEarliestReleaseEvent());
            }
            else {
                log("Searching releases with query '"+releasesQuery+"'.");
                int page = selector.getPage();
                boolean getReleaseDetails = selector.getAdditionalReleaseInfo();
                ReleaseFilter releaseFilter = new ReleaseFilter(100, (page-1)*100, releasesQuery);
                ReleaseSearchResults releases = q.getReleases(releaseFilter);
                List<ReleaseResult> results = releases.getReleaseResults();
                log("Found total "+results.size()+" releases matching the query. Notice the maximum number of releases returned by MusicBrainz is 100. Use paging to retrieve more releases.");
                int i = 1;
                for( ReleaseResult result : results ) {
                    if(forceStop()) break;
                    setProgress(i);
                    Release release = result.getRelease();
                    int score = result.getScore();
                    log("Found MusicBrainz release ("+i+") '"+release.getTitle()+"' with score "+score);
                    if(getReleaseDetails) {
                        Release detailedRelease = q.getReleaseById(release.getId(), releaseIncludes);
                        getReleaseTopic(tm, detailedRelease);
                        takeNap();
                    }
                    else {
                        getReleaseTopic(tm, release);
                    }
                    i++;
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }




    public void makeTracksQuery(MusicBrainzExtractorSelector selector, TopicMap tm) {
        try {
            TrackIncludes trackIncludes = new TrackIncludes();
            trackIncludes.setArtist(true);
            trackIncludes.setUrlRelations(true);
            trackIncludes.setTrackRelations(true);
            trackIncludes.setReleaseRelations(true);
            trackIncludes.setArtistRelations(true);
            trackIncludes.setReleases(true);
            trackIncludes.setTrackRelations(true);
            trackIncludes.setPuids(true);
            trackIncludes.setUrlRelations(true);

            Query q = new Query();
            String trackQuery = selector.getTracks();
            if(trackQuery.matches(MBID_PATTERN)) {
                log("Found MBID. Getting release data with MBID '"+trackQuery+"'");
                Track track = q.getTrackById(trackQuery, trackIncludes);
                getTrackTopic(tm, track);

                System.out.println("track.getTitle() == "+track.getTitle());
                System.out.println("track.getDuration() == "+track.getDuration());
                System.out.println("track.getArtist() == "+track.getArtist());
                System.out.println("track.getPuids() == "+track.getPuids());
                System.out.println("track.getReleases() == "+track.getReleases());
            }
            else {
                log("Searching tracks with query '"+trackQuery+"'.");
                int page = selector.getPage();
                boolean getTrackDetails = selector.getAdditionalTrackInfo();
                TrackFilter trackFilter = new TrackFilter(100, (page-1)*100, trackQuery);
                TrackSearchResults tracks = q.getTracks(trackFilter);
                List<TrackResult> results = tracks.getTrackResults();
                log("Found total "+results.size()+" releases matching the query. Notice the maximum number of tracks returned by MusicBrainz is 100. Use paging to retrieve more tracks.");
                int i = 1;
                for( TrackResult result : results ) {
                    if(forceStop()) break;
                    setProgress(i);
                    Track track = result.getTrack();
                    int score = result.getScore();
                    log("Found MusicBrainz track ("+i+") '"+track.getTitle()+"' with score "+score);
                    if(getTrackDetails) {
                        Track detailedTrack = q.getTrackById(track.getId(), trackIncludes);
                        getTrackTopic(tm, detailedTrack);
                        takeNap();
                    }
                    else {
                        getTrackTopic(tm, track);
                    }
                    i++;
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }



    private void takeNap() {
        takeNap(1100);
    }

    private void takeNap(int napTime) {
        if(napTime > 0) {
            try {
                Thread.currentThread().sleep(napTime);
            }
            catch(Exception e) {
                // WAKE UP
            }
        }
    }

}
