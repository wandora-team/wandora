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
 */


package org.wandora.application.tools.extractors.musicbrainz;



import java.util.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;

import org.wandora.application.gui.*;
import javax.swing.*;

import org.musicbrainz.*;
import org.musicbrainz.model.*;
import org.wandora.application.tools.extractors.ExtractHelper;



/**
 *
 * @author akivela
 */
public abstract class AbstractMusicBrainzExtractor extends AbstractWandoraTool {

    // Default language of occurrences and variant names.
    public static String LANG = "en";


    /**
     * Try to retrieve topic before new is created. Setting this true may speed
     * the extraction but extraction may loose some data as topic is created only once.
     */
    public static boolean USE_EXISTING_TOPICS = false;


    public static String SIPREFIX="http://musicbrainz.org/";

    public static String ARTIST_SI=SIPREFIX+"artist";
    public static String RELEASE_SI=SIPREFIX+"release";
    public static String TRACK_SI=SIPREFIX+"track";

    public static String ARTIST_ALIAS_SI=SIPREFIX+"artist-alias/";
    public static String ARTIST_ALIAS_TYPE_SI=SIPREFIX+"artist-alias-type/";

    public static String TAG_SI=SIPREFIX+"tag";

    public static String RELEASE_TYPE_SI = SIPREFIX+"release-type/";
    public static String ARTIST_TYPE_SI = SIPREFIX+"artist-type/";

    public static String TRACK_DURATION_SI = SIPREFIX+"track-duration";
    public static String RELEASE_DATE_SI = SIPREFIX+"release-date";

    public static String DATE_SI = SIPREFIX+"date/";

    public static String MBID_SI = SIPREFIX+"MBID";

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_musicbrainz.png");
    }
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }




    // ******** TOPIC MAPS *********


    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }



    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }




    // ******** TYPE TOPICS **********


    protected static Topic getArtistTypeTopic(TopicMap tm) throws TopicMapException {
        Topic artistType = getOrCreateTopic(tm, ARTIST_SI, "musicbrainz artist");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, artistType, wandoraClass);
        return artistType;
    }


    protected static Topic getReleaseTypeTopic(TopicMap tm) throws TopicMapException {
        Topic albumType = getOrCreateTopic(tm, RELEASE_SI, "musicbrainz release");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, albumType, wandoraClass);
        return albumType;
    }

    protected static Topic getTrackTypeTopic(TopicMap tm) throws TopicMapException {
        Topic trackType=getOrCreateTopic(tm, TRACK_SI, "musicbrainz track");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, trackType, wandoraClass);
        return trackType;
    }

    protected static Topic getTagTypeTopic(TopicMap tm) throws TopicMapException {
        Topic tagType=getOrCreateTopic(tm, TAG_SI, "musicbrainz tag");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, tagType, wandoraClass);
        return tagType;
    }


    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }



    // *********** MUSICBRAINZ TOPICS ***********


    protected static Topic getArtistTopic(TopicMap tm, Artist theArtist) throws TopicMapException {
        String artistSI = theArtist.getId()+".html";
        String MBID = makeMBID(theArtist.getId());
        Topic theArtistTopic = null;
        if(USE_EXISTING_TOPICS) theArtistTopic = tm.getTopic(artistSI);
        if(theArtistTopic == null) {
            Topic artistType = getArtistTypeTopic(tm);
            theArtistTopic=tm.createTopic();
            theArtistTopic.addSubjectIdentifier(tm.createLocator(artistSI));
            theArtistTopic.setBaseName(theArtist.getName() + " ("+MBID+")");
            theArtistTopic.setDisplayName(LANG, theArtist.getName());
            theArtistTopic.addType(artistType);
        }

        try {
            if(MBID != null) {
                Topic MBIDTopic = getOrCreateTopic(tm, MBID_SI, "MBID");
                setData(tm, theArtistTopic, MBIDTopic, MBID);
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            List<Release> releases = theArtist.getReleases();
            if(releases != null && releases.size()>0) {
                for( Release release : releases ) {
                    Topic releaseTopic = getReleaseTopic(tm, release);
                    if(releaseTopic != null && theArtistTopic != null) {
                        createAssociation(tm, "artist-release", theArtistTopic, "artist", releaseTopic, "release");
                    }
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }
        
        try {
            String atype = theArtist.getType();
            if(atype != null) {
                String si = ARTIST_TYPE_SI+atype;
                if(atype.startsWith("http://")) si = atype;
                Topic artistTypeTopic = getOrCreateTopic(tm, si, atype);
                if(artistTypeTopic != null && theArtistTopic != null) {
                    createAssociation(tm, "artist-type", theArtistTopic, "artist", artistTypeTopic, "type");
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            List<ArtistAlias> aliases = theArtist.getAliases();
            if(aliases != null && aliases.size() > 0) {
                for( ArtistAlias alias : aliases ) {
                    Topic artistAliasValueTopic = getOrCreateTopic(tm, ARTIST_ALIAS_SI+alias.getValue(), alias.getValue());
                    Topic artistAliasTypeTopic = getOrCreateTopic(tm, ARTIST_ALIAS_TYPE_SI+alias.getType(), alias.getValue());
                    if(artistAliasValueTopic != null && artistAliasTypeTopic != null && theArtistTopic != null) {
                        createAssociation(tm, "artist-alias", theArtistTopic, "artist", artistAliasValueTopic, "alias", artistAliasTypeTopic, "alias-type");
                    }
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            String beginDate = theArtist.getBeginDate();
            if(beginDate != null) {
                String si = DATE_SI+beginDate;
                Topic artistBeginDate = getOrCreateTopic(tm, si, beginDate);
                if(artistBeginDate != null && theArtistTopic != null) {
                    createAssociation(tm, "artist-begin-date", theArtistTopic, "artist", artistBeginDate, "begin-date");
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            String endDate = theArtist.getEndDate();
            if(endDate != null) {
                String si = DATE_SI+endDate;
                Topic artistEndDate = getOrCreateTopic(tm, si, endDate);
                if(artistEndDate != null && theArtistTopic != null) {
                    createAssociation(tm, "artist-end-date", theArtistTopic, "artist", artistEndDate, "end-date");
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        return theArtistTopic;
    }


    
    protected static Topic getReleaseTopic(TopicMap tm, Release theRelease) throws TopicMapException {
        String releaseSI = theRelease.getId()+".html";
        String MBID = makeMBID(theRelease.getId());
        Topic theReleaseTopic = null;
        if(USE_EXISTING_TOPICS) theReleaseTopic = tm.getTopic(releaseSI);
        if(theReleaseTopic == null) {
            Topic releaseType = getReleaseTypeTopic(tm);
            theReleaseTopic=tm.createTopic();
            theReleaseTopic.addSubjectIdentifier(tm.createLocator(releaseSI));
            theReleaseTopic.setBaseName(theRelease.getTitle() + " ("+MBID+")");
            theReleaseTopic.setDisplayName(LANG, theRelease.getTitle());
            theReleaseTopic.addType(releaseType);
        }

        try {
            if(MBID != null) {
                Topic MBIDTopic = getOrCreateTopic(tm, MBID_SI, "MBID");
                setData(tm, theReleaseTopic, MBIDTopic, MBID);
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            Artist artist = theRelease.getArtist();
            if(artist != null) {
                Topic artistTopic = getArtistTopic(tm, artist);
                if(theReleaseTopic != null && artistTopic != null) {
                    createAssociation(tm, "artist-release", artistTopic, "artist", theReleaseTopic, "release");
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            String[] rtypes = theRelease.getTypes();
            if(rtypes != null && rtypes.length > 0) {
                for(int i=0; i<rtypes.length; i++) {
                    String rtype = rtypes[i];
                    if(rtype != null && rtype.length() > 0) {
                        String si = RELEASE_TYPE_SI+rtype;
                        Topic releaseTypeTopic = getOrCreateTopic(tm, si, rtype);
                        if(releaseTypeTopic != null && theReleaseTopic != null) {
                            createAssociation(tm, "release-type", theReleaseTopic, "release", releaseTypeTopic, "type");
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            String releaseDate = theRelease.getEarliestReleaseDate();
            if(releaseDate != null) {
                Topic releaseDateTopic = getOrCreateTopic(tm, RELEASE_DATE_SI, "release-date");
                setData(tm, theReleaseTopic, releaseDateTopic, releaseDate);
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        return theReleaseTopic;
    }



    protected static Topic getTrackTopic(TopicMap tm, Track theTrack) throws TopicMapException {
        String trackSI = theTrack.getId()+".html";
        String MBID = makeMBID(theTrack.getId());
        Topic theTrackTopic = null;
        if(USE_EXISTING_TOPICS) theTrackTopic = tm.getTopic(trackSI);
        if(theTrackTopic == null) {
            Topic trackType=getTrackTypeTopic(tm);
            theTrackTopic=tm.createTopic();
            theTrackTopic.addSubjectIdentifier(tm.createLocator(trackSI));
            theTrackTopic.setBaseName(theTrack.getTitle()+" ("+MBID+")");
            theTrackTopic.setDisplayName(LANG, theTrack.getTitle());
            theTrackTopic.addType(trackType);
        }

        try {
            if(MBID != null) {
                Topic MBIDTopic = getOrCreateTopic(tm, MBID_SI, "MBID");
                setData(tm, theTrackTopic, MBIDTopic, MBID);
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            List<Release> releases = theTrack.getReleases();
            if(releases != null && releases.size()>0) {
                for( Release release : releases ) {
                    Topic releaseTopic = getReleaseTopic(tm, release);
                    if(releaseTopic != null && theTrackTopic != null) {
                        createAssociation(tm, "release-track", releaseTopic, "release", theTrackTopic, "track");
                    }
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        try {
            long duration = theTrack.getDuration();
            if(duration != 0) {
                Topic trackDurationTopic = getOrCreateTopic(tm, TRACK_DURATION_SI, "track-duration");
                setData(tm, theTrackTopic, trackDurationTopic, ""+duration);
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }

        return theTrackTopic;
    }




    

    public static String makeMBID(String urlId) {
        if(urlId == null) return null;
        String MBID = urlId;
        int i = urlId.lastIndexOf('/');
        if(i != -1) {
            MBID = urlId.substring(i+1);
            if(MBID.endsWith(".html")) {
                MBID = MBID.substring(0, MBID.length()-5);
            }
        }
        return MBID;
    }




    public static Association createAssociation(TopicMap tm, String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2) throws TopicMapException {
        Topic associationTypeTopic = createSchemaTopic(tm,associationType);
        Association association = tm.createAssociation(associationTypeTopic);
        //Topic associationTypeTypeTopic = createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ASSOCIATION_TYPE);
        //associationTypeTopic.addType(associationTypeTypeTopic);
        Topic role1Topic = createSchemaTopic(tm,role1);
        Topic role2Topic = createSchemaTopic(tm,role2);
        association.addPlayer(player1Topic, role1Topic);
        association.addPlayer(player2Topic, role2Topic);
        return association;
    }




    public static Association createAssociation(TopicMap tm, String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3) throws TopicMapException {
        Topic associationTypeTopic = createSchemaTopic(tm,associationType);
        Association association = tm.createAssociation(associationTypeTopic);
        //Topic associationTypeTypeTopic = createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ASSOCIATION_TYPE);
        //associationTypeTopic.addType(associationTypeTypeTopic);
        Topic role1Topic = createSchemaTopic(tm,role1);
        Topic role2Topic = createSchemaTopic(tm,role2);
        Topic role3Topic = createSchemaTopic(tm,role3);
        association.addPlayer(player1Topic, role1Topic);
        association.addPlayer(player2Topic, role2Topic);
        association.addPlayer(player3Topic, role3Topic);
        return association;
    }


    public static Topic createSchemaTopic(TopicMap tm, String schemaName) throws TopicMapException {
        return getOrCreateTopic(tm, SIPREFIX+schemaName,schemaName+" (musicbrainz schema)");
    }





    private static void setData(TopicMap tm, Topic t, Topic type, String text) throws TopicMapException {
        if(t != null & type != null && text != null) {
            String langsi=XTMPSI.getLang(LANG);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(tm.createLocator(langsi));
            }
            t.setData(type, langT, text);
        }
    }


    
    // -------------------------------------------------------------------------

}
