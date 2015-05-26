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

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.tika.io.IOUtils;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.DataURL;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public class UserWrapper extends AbstractFBTypeWrapper {
    
    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE + "user/";
    
    private final User user;
    
    public UserWrapper(User user){
        this.user = user;
    }
    
    // https://developers.facebook.com/docs/graph-api/reference/user
    // ToDo: refactor into sensible chunks
    @Override
    public Topic mapToTopicMap(TopicMap tm) throws TopicMapException{
        Topic langTopic = getLangTopic(tm);

        Topic userTopic = super.mapToTopicMap(tm);

        Topic userType = getOrCreateType(tm, getType());
        userTopic.setSubjectLocator(new Locator(AbstractFBGraphExtractor.URL_ROOT + user.getId()));

        
        // Simple strings to map as occurrence data
        final String[][] occurrenceData = {
            {"Name",               user.getName()},
            {"About",              user.getAbout()},
            {"Bio",                user.getBio()},
            {"Birthday",           user.getBirthday()},
            {"Email",              user.getEmail()},
            {"Gender",             user.getGender()},
            {"Link",               user.getLink()},
            {"Locale",             user.getLocale()},
            {"Political",          user.getPolitical()},
            {"RelationshipStatus", user.getRelationshipStatus()},
            {"Religion",           user.getReligion()},
            {"Quotes",             user.getQuotes()},
            {"Website",            user.getWebsite()},
            {"Currency",           user.getCurrency() != null ? user.getCurrency().getUserCurrency() : null}
        };
        

        for (String[] datum : occurrenceData) {
            Topic type = getOrCreateType(tm, datum[0]);
            userTopic.setData(type, langTopic, datum[1]);
        }
        
        NamedFacebookType userLocation = user.getLocation();
        associateNamedType(tm, userLocation, "Location", userTopic, userType);
        
        List<NamedFacebookType> athletes = user.getFavoriteAthletes();

        for(NamedFacebookType athlete: athletes){
            associateNamedType(tm, athlete, "Athlete", userTopic, userType);
        }
        
        List<NamedFacebookType> teams = user.getFavoriteTeams();

        for(NamedFacebookType team: teams){
            associateNamedType(tm, team, "Team", userTopic, userType);
        }
        
        NamedFacebookType hometown = user.getHometown();
        associateNamedType(tm, hometown, "Hometown", userTopic, userType);
        
        List<User.Work> works = user.getWork();
        Topic workType = getOrCreateType(tm, "Work");
        for(User.Work work: works){
            Topic workTopic = mapUserWork(work, tm);
            Association workAssoc = tm.createAssociation(workType);
            workAssoc.addPlayer(userTopic, userType);
            workAssoc.addPlayer(workTopic, workType);
        }
        
        addPicture(tm, userTopic);
        
        return userTopic;    
        
    }

    @Override
    public FacebookType getEnclosedEntity() {
        return this.user;
    }

    @Override
    public String getType() {
        return "User";
    }

    @Override
    public String getSIBase() {
        return SI_BASE;
    }

    private Topic mapUserWork(User.Work work, TopicMap tm) throws TopicMapException {
        UserWorkWrapper wrapper = new UserWorkWrapper(work);
        return wrapper.mapToTopicMap(tm);
    }

    private void addPicture(TopicMap tm, Topic userTopic) {
        try {
            URL imageUrl = new URL(AbstractFBGraphExtractor.URL_ROOT + this.user.getId() + "/picture");
            String contentType = imageUrl.openConnection().getContentType();
            byte[] data = IOUtils.toByteArray(imageUrl.openStream());
            DataURL u = new DataURL(contentType, data);
            
            Topic picType = getOrCreateType(tm, "Profile Picture");
            Topic langTopic = getOrCreateTopic(tm, XTMPSI.LANG_INDEPENDENT);
            userTopic.setData(picType, langTopic, u.toExternalForm());
            
            
        } catch (IOException | TopicMapException e) {
            UserWrapper.logger.log(e);
        } 
        
    }
    
}
