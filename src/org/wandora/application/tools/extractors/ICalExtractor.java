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

package org.wandora.application.tools.extractors;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.Icon;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.TopicMap;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.data.*;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.XTMPSI;


/*
 * @author
 * Eero Lehtonen
 */

public class ICalExtractor extends AbstractExtractor {

  public static final String ICAL_SI = "http://tools.ietf.org/html/rfc5545/";
  // ****** TYPE SIS FOR GENERAL COMPONENT PROPERTIES ******
  public static final String ICAL_CALENDAR_SI = ICAL_SI + "calendar";
  public static final String ICAL_VERSION_SI = ICAL_SI + "version";
  public static final String ICAL_CALSCALE_SI = ICAL_SI + "calscale";
  public static final String ICAL_CAL_TZ_SI = ICAL_SI + "timezone";
  public static final String ICAL_NAME_SI = ICAL_SI + "name";
  public static final String ICAL_DESCRIPTION_SI = ICAL_SI + "description";
  public static final String ICAL_UID_SI = ICAL_SI + "UID";
  public static final String ICAL_URL_SI = ICAL_SI + "URL";
  public static final String ICAL_LAT_SI = ICAL_SI + "LAT";
  public static final String ICAL_LON_SI = ICAL_SI + "LON";
  public static final String ICAL_CREATED_SI = ICAL_SI + "created";
  public static final String ICAL_MODIFIED_SI = ICAL_SI + "modified";
  public static final String ICAL_START_TIME_SI = ICAL_SI + "start-time";
  public static final String ICAL_END_TIME_SI = ICAL_SI + "end-time";
  public static final String ICAL_SUMMARY_SI = ICAL_SI + "summary";
  public static final String ICAL_LOCATION_SI = ICAL_SI + "location";
  public static final String ICAL_PRIOR_SI = ICAL_SI + "priority";
  public static final String ICAL_CLASS_SI = ICAL_SI + "class";
  public static final String ICAL_ORGANIZER_SI = ICAL_SI + "organizer";
  public static final String ICAL_STATUS_SI = ICAL_SI + "status";
  public static final String ICAL_RECURRENCE_SI = ICAL_SI + "recurrenceID";
  public static final String ICAL_CAT_SI = ICAL_SI + "categories";
  public static final String ICAL_DATE_SI = ICAL_SI + "date";
  // ****** TYPE SIS FOR EVENT SPECIFIC PROPERTIES ******
  public static final String ICAL_EVENT_SI = ICAL_SI + "event/";
  public static final String ICAL_TRANSP_SI = ICAL_EVENT_SI + "transparency";
  // ****** TYPE SIS FOR VENUE SPECIFIC PROPERTIES ******
  public static final String ICAL_VENUE_SI = ICAL_SI + "venue/";
  public static final String ICAL_STADDR_SI = ICAL_VENUE_SI + "street-address";
  public static final String ICAL_EXTADDR_SI = ICAL_VENUE_SI + "extended-address";
  public static final String ICAL_LOCALITY_SI = ICAL_VENUE_SI + "locality";
  public static final String ICAL_REGION_SI = ICAL_VENUE_SI + "region";
  public static final String ICAL_COUNTRY_SI = ICAL_VENUE_SI + "country";
  public static final String ICAL_POSTALCODE_SI = ICAL_VENUE_SI + "postal-code";
  public static final String ICAL_TZID_SI = ICAL_VENUE_SI + "tzid";
  public static final String ICAL_LOCTYPE_SI = ICAL_VENUE_SI + "location-type";
  // ****** TYPE SIS FOR ALARM SPECIFIC PROPERTIES ******
  public static final String ICAL_ALARM_SI = ICAL_SI + "alarm/";
  public static final String ICAL_TRIGGER_SI = ICAL_ALARM_SI + "trigger";
  public static final String ICAL_DURATION_SI = ICAL_ALARM_SI + "duration";
  public static final String ICAL_REPEAT_SI = ICAL_ALARM_SI + "repeat";
  // ****** TYPE SIS FOR TODO SPECIFIC PROPERTIES ******
  public static final String ICAL_TODO_SI = ICAL_SI + "todo/";
  public static final String ICAL_DUE_TIME_SI = ICAL_TODO_SI + "due-time";
  public static final String ICAL_TODO_DATE_COMPLETED_SI = ICAL_TODO_SI + "date completed";
  public static final String ICAL_TODO_COMPLETED_SI = ICAL_TODO_SI + "percent-complete";

  @Override
  public WandoraToolType getType() {
    return WandoraToolType.createExtractType();
  }

  @Override
  public String getName() {
    return "iCalendar extractor";
  }

  @Override
  public String getDescription() {
    return "The iCalendar Extractor is used to extract topic map data from iCalendar sources.";
  }

  @Override
  public Icon getIcon() {
    return UIBox.getIcon(0xf133);
  }

  @Override
  public boolean runInOwnThread() {
    return true;
  }

  @Override
  public boolean useTempTopicMap() {
    return false;
  }

  @Override
  public boolean useURLCrawler() {
    return false;
  }

  public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
    return _extractTopicsFrom(new FileInputStream(f), t);
  }

  public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
    return _extractTopicsFrom(u.openStream(), t);
  }

  public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
    return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes("UTF-8")), t);
  }

  public boolean _extractTopicsFrom(InputStream inputStream, TopicMap topicMap) throws Exception {
    CalendarBuilder builder = new CalendarBuilder();
    try {
      net.fortuna.ical4j.model.Calendar calendar = builder.build(inputStream);
      return parseCalendar(calendar, topicMap);
    } catch (ParserException e) {
      log("There was an error parsing the calendar file.");
      log(e);
    }
    return false;
  }

  /*
   * Parse the given calendar object into a Wandora topic and pass the included events and venues
   * to separate parsers. Parsed topics aare then associated with the calendar.
   */
  public boolean parseCalendar(net.fortuna.ical4j.model.Calendar calendar, TopicMap topicMap) {
    try {
      String prodId = calendar.getProductId().getValue();
      if (prodId != null) {
        Property versionP = calendar.getProperty("VERSION");
        String version = versionP != null ? versionP.getValue() : "";
        Property calScaleP = calendar.getProperty("CALSCALE");
        String calScale = calScaleP != null ? calScaleP.getValue() : "";
        Property calTZP = calendar.getProperty("X-WR-TIMEZONE");
        String calTZ = calTZP != null ? calTZP.getValue() : "";
        Property calDescP = calendar.getProperty("X-WR-CALDESC");
        String calDesc = calDescP != null ? calDescP.getValue() : "";
        Property calNameP = calendar.getProperty("X-WR-CALNAME");
        String calName = calNameP != null ? calNameP.getValue() : "";
        //Use the required prodID if  the optional (extension) calname is not specified
        String calendarName = !calName.isEmpty() ? calName : prodId;

        Topic calendarType = this.getCalendarType(topicMap);
        Topic calendarTopic = getCalendarTopic(calendarName, topicMap);
        calendarTopic.setBaseName(calendarName);
        calendarTopic.setDisplayName(LANG, calendarName);
        Topic defaultLangTopic = getDefaultLangTopic(topicMap);

        //Property -> occurrence
        if (!calDesc.isEmpty()) {
          createOccurrence(topicMap, calendarTopic, defaultLangTopic, ICAL_DESCRIPTION_SI, "iCalendar description", calDesc);
        }
        //Property -> association
        if (!version.isEmpty()) {
          createAssociation(topicMap, calendarType, calendarTopic, ICAL_VERSION_SI, "version", "iCalendar calendar version", version);
        }
        if (!calScale.isEmpty()) {
          createAssociation(topicMap, calendarType, calendarTopic, ICAL_CALSCALE_SI, "calScale", "iCalendar calendar scale", calScale);
        }
        if (!calTZ.isEmpty()) {
          createAssociation(topicMap, calendarType, calendarTopic, ICAL_CAL_TZ_SI, "calTZ", "iCalendar calendar time zone", calTZ);
        }

        //Parse venues
        ComponentList venues = calendar.getComponents("VVENUE");
        if (venues != null) {
          for (Object o : venues) {
            Component component = (Component) o;
            VVenue venue = (VVenue) component;
            venue.validate();
            Topic venueTopic = parseVenue(venue, topicMap);
            Topic venueType = getVenueType(topicMap);
            if (venueTopic != null && venueType != null && calendarType != null && calendarTopic != null) {
              Association a = topicMap.createAssociation(venueType);
              a.addPlayer(calendarTopic, calendarType);
              a.addPlayer(venueTopic, venueType);
            }
          }
        }

        //Parse events
        ComponentList events = calendar.getComponents("VEVENT");
        if (events != null) {
          for (Object o : events) {
            Component component = (Component) o;
            VEvent event = (VEvent) component;
            event.validate();
            Topic eventTopic = parseEvent(event, topicMap);
            Topic eventType = getEventType(topicMap);
            if (eventTopic != null && eventType != null && calendarType != null && calendarTopic != null) {
              Association a = topicMap.createAssociation(eventType);
              a.addPlayer(calendarTopic, calendarType);
              a.addPlayer(eventTopic, eventType);
            }
          }
        }

        //Parse ToDos
        ComponentList todos = calendar.getComponents("VTODO");
        if (todos != null) {
          for (Object o : todos) {
            Component component = (Component) o;
            VToDo todo = (VToDo) component;
            todo.validate();
            Topic todoTopic = parseToDo(todo, topicMap);
            Topic todoType = getToDoType(topicMap);
            if (todoTopic != null && todoType != null && calendarType != null && calendarTopic != null) {
              Association a = topicMap.createAssociation(todoType);
              a.addPlayer(calendarTopic, calendarType);
              a.addPlayer(todoTopic, todoType);
            }
          }
        }
      } else {
        log("Calendar missing mandatory prodId -- skipping.");
      }
    } catch (Exception e) {
      log(e);
    }
    return true;
  }

  /*
   * Parse a single venue into a topic where  the venue's properties are mapped
   * to the topic's occurrences and associations.
   */
  public Topic parseVenue(VVenue venue, TopicMap topicMap) {
    try {

      Property nameP = venue.getProperty("NAME");
      String name = nameP != null ? nameP.getValue() : "";
      Property descriptionP = venue.getProperty("DESCRIPTION");
      String description = descriptionP != null ? descriptionP.getValue() : "";
      Property addressP = venue.getProperty("STREET-ADDRESS");
      String address = addressP != null ? addressP.getValue() : "";
      Property extAddressP = venue.getProperty("EXTENDED-ADDRESS");
      String extAddress = extAddressP != null ? extAddressP.getValue() : "";
      Property localityP = venue.getProperty("LOCALITY");
      String locality = localityP != null ? localityP.getValue() : "";
      Property regionP = venue.getProperty("REGION");
      String region = regionP != null ? regionP.getValue() : "";
      Property countryP = venue.getProperty("COUNTRY");
      String country = regionP != null ? countryP.getValue() : "";
      Property postalCodeP = venue.getProperty("POSTAL-CODE");
      String postalCode = regionP != null ? postalCodeP.getValue() : "";
      Property tzidP = venue.getProperty("TZID");
      String tzid = regionP != null ? tzidP.getValue() : "";
      Property locTypeP = venue.getProperty("LOCATION-TYPE");
      String locType = locTypeP != null ? locTypeP.getValue() : "";
      Property catP = venue.getProperty("CATEGORIES");
      String cat = catP != null ? catP.getValue() : "";

      Property uidP = venue.getProperty("UID");
      if (uidP != null) {

        String uid = venue.getProperty("UID").getValue();
        String basename = name.isEmpty() ? uid + " (Venue)" : name + " (Venue)";
        String displayname = name.isEmpty() ? uid : name;

        Topic venueTopic = getUTopic(ICAL_VENUE_SI + uid, getVenueType(topicMap), topicMap);
        Topic defaultLangTopic = getDefaultLangTopic(topicMap);
        venueTopic.setBaseName(basename);
        venueTopic.setDisplayName(LANG, displayname);
        Topic venueUIDtype = this.getComponentUIDType(topicMap);
        venueTopic.setData(venueUIDtype, defaultLangTopic, uid);
        Topic venueType = this.getVenueType(topicMap);

        if (!description.isEmpty()) {
          createOccurrence(topicMap, venueTopic, defaultLangTopic, ICAL_DESCRIPTION_SI, "iCalendar description", description);
        }
        if (!address.isEmpty()) {
          createOccurrence(topicMap, venueTopic, defaultLangTopic, ICAL_STADDR_SI, "iCalendar street address", address);
        }
        if (!extAddress.isEmpty()) {
          createOccurrence(topicMap, venueTopic, defaultLangTopic, ICAL_EXTADDR_SI, "iCalendar extended address", extAddress);
        }
        if (!locality.isEmpty()) {
          createOccurrence(topicMap, venueTopic, defaultLangTopic, ICAL_LOCALITY_SI, "iCalendar locality", locality);
        }
        if (!tzid.isEmpty()) {
          createOccurrence(topicMap, venueTopic, defaultLangTopic, ICAL_TZID_SI, "iCalendar time zone ID", tzid);
        }
        if (!cat.isEmpty()) {
          createAssociation(topicMap, venueType, venueTopic, ICAL_CAT_SI, "category", "iCalendar category", cat);

        }
        if (!country.isEmpty()) {
          createAssociation(topicMap, venueType, venueTopic, ICAL_COUNTRY_SI, "country", "iCalendar country", country);
        }
        if (!region.isEmpty()) {
          createAssociation(topicMap, venueType, venueTopic, ICAL_REGION_SI, "region", "iCalendar region", region);
        }
        if (!locType.isEmpty()) {
          createAssociation(topicMap, venueType, venueTopic, ICAL_LOCTYPE_SI, "loctype", "iCalendar location type", locType);
        }
        if (!postalCode.isEmpty()) {
          createAssociation(topicMap, venueType, venueTopic, ICAL_POSTALCODE_SI, "postalcode", "iCalendar postal code", postalCode);
        }
        return venueTopic;
      } else {
        log("a VVenue missing mandatory UID -- skipping.");
      }
    } catch (Exception e) {
      log(e);
    }
    return null;
  }
  /*
   * Parse a single event to a topic where the event's properties are mapped to the topic's
   * occurrences and associations.
   *
   * Optional alerts are then parsed and associated with the  event.
   */

  public Topic parseEvent(VEvent event, TopicMap topicMap) {
    try {

      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
      java.util.Calendar cal = Calendar.getInstance();

      //Possible occurrencess

      Geo ge = event.getGeographicPos();
      String lat = (ge != null) ? event.getGeographicPos().getLatitude().toString() : "";
      String lon = (ge != null) ? event.getGeographicPos().getLongitude().toString() : "";

      Description desc = event.getDescription();
      String description = desc != null ? desc.getValue() : "";

      Summary summ = event.getSummary();
      String summary = summ != null ? event.getSummary().getValue() : "";
      Url url = event.getUrl();
      String urlString = url != null ? event.getUrl().getValue() : "";

      DtStart dtstart = event.getStartDate();
      DtEnd dtend = event.getEndDate(true); //Derive from DURATION if there's no DTEND
      String startString = "";
      String endString = "";
      if (dtstart != null && !dtstart.getValue().isEmpty()) {
        Date start = event.getStartDate().getDate();
        Date end;
        //Spec: if DTEND and DURATION aren't defined...
        if (dtend == null) {
          //... and DTSTART has a parameter VALUE=DATE ie DTSTART is of type DATE instead of DATE-TIME
          //the duration for the event is assumed to be one day.
          if (dtstart.getParameter("VALUE") != null && dtstart.getParameter("VALUE").getValue().equals("DATE")) {
            cal.setTime(start);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            end = cal.getTime();
            //... else DTSTART is assumed to be of type DATE-TIME and the event to end on the start time
          } else {
            end = start;
          }
        } else {
          end = dtend.getDate();
        }
        startString = df.format(start);
        endString = df.format(end);
      }

      String modified = event.getLastModified() != null ? df.format(event.getLastModified().getDate()) : "";
      String created = event.getCreated() != null ? df.format(event.getCreated().getDate()) : "";


      //Possible assocs
      String dateString = event.getStartDate() != null ? dfDate.format(event.getStartDate().getDate()) : "";
      Transp transparency = event.getTransparency();
      String transp = transparency != null ? transparency.getValue() : "OPAQUE";
      Priority prio = event.getPriority();
      String priority = prio != null ? prio.getValue() : "";
      Clazz claz = event.getClassification();
      String clazz = claz != null ? claz.getValue() : "";
      Location location = event.getLocation();

      Property cats = event.getProperty("CATEGORIES");
      String catString = cats != null && !cats.getValue().isEmpty() ? cats.getValue() : "";
      String[] catArray = catString.isEmpty() ? null : catString.split(",");
      Organizer org = event.getOrganizer();

      String organizer = org != null ? org.getValue() : "";
      Status stat = event.getStatus();
      String status = stat != null ? event.getStatus().getValue() : "";
      RecurrenceId recur = event.getRecurrenceId();
      String recurrence = recur != null ? recur.getDate().toString() : "";

      Uid uid = event.getUid();

      if (uid != null) {

        String uidString = uid.getValue();
        String basename = summary.isEmpty() ? uidString + " (Event)" : summary + " (Event)";

        Topic defaultLangTopic = getDefaultLangTopic(topicMap);
        Topic eventType = this.getEventType(topicMap);
        Topic eventTopic = getUTopic(ICAL_EVENT_SI + uidString, getEventType(topicMap), topicMap);
        eventTopic.setBaseName(basename);
        eventTopic.setDisplayName(LANG, summary);

        createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_UID_SI, "iCalendar UID", uidString);


        if (!summary.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_SUMMARY_SI, "iCalendar summary", summary);
        }

        if (!description.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_DESCRIPTION_SI, "iCalendar description", description);
        }

        if (!urlString.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_URL_SI, "iCalendar URL", urlString);
        }

        if (!lat.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_LAT_SI, "iCalendar latitude", lat);
        }

        if (!lon.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_LON_SI, "iCalendar longitude", lon);
        }

        if (!startString.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_START_TIME_SI, "iCalendar start time", startString);
        }

        if (!endString.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_END_TIME_SI, "iCalendar end time", endString);
        }

        if (!created.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_CREATED_SI, "iCalendar time created", created);
        }

        if (!modified.isEmpty()) {
          createOccurrence(topicMap, eventTopic, defaultLangTopic, ICAL_MODIFIED_SI, "iCalendar time modified", modified);
        }

        if (!dateString.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_DATE_SI, "date", "iCalendar event date", dateString);
        }

        if (!transp.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_TRANSP_SI, "transparency", "iCalendar component transparency", transp);
        }

        if (!priority.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_PRIOR_SI, "priority", "iCalendar component priority", priority);
        }

        if (!clazz.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_CLASS_SI, "class", "iCalendar component class", clazz);
        }

        if (!organizer.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_ORGANIZER_SI, "organizer", "iCalendar component organizer", clazz);
        }

        if (!status.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_STATUS_SI, "status", "iCalendar component status", status);
        }

        if (!recurrence.isEmpty()) {
          createAssociation(topicMap, eventType, eventTopic, ICAL_RECURRENCE_SI, "recurrence", "iCalendar component recurrence", recurrence);
        }

        if (catArray != null) {
          for (String cat : catArray) {
            createAssociation(topicMap, eventType, eventTopic, ICAL_CAT_SI, "category", "iCalendar category", cat);
          }
        }

        if (location != null && !location.getValue().isEmpty()) {
          Topic eventLocationTopic;
          if (location.getParameter("VVENUE") != null) { // Use VVenue if found
            eventLocationTopic = topicMap.getTopic(ICAL_VENUE_SI + location.getParameter("VVENUE").getValue());
          } else {
            eventLocationTopic = getLocationTopic(urlEncode(location.getValue()), topicMap);
            eventLocationTopic.setBaseName(location.getValue());
            eventLocationTopic.setDisplayName(LANG, location.getValue());
          }
          if(eventLocationTopic != null && eventType != null && eventTopic != null){
            createAssociation(topicMap, eventLocationTopic, eventType, eventTopic, ICAL_LOCATION_SI, "location", location.getValue());
          }
        }

        ComponentList alarms = event.getAlarms();
        int i = 0;
        if (alarms != null) {
          for (Object o : alarms) {
            Component component = (Component) o;
            VAlarm alarm = (VAlarm) component;
            alarm.validate();
            Topic alarmTopic = parseAlarm(alarm, topicMap, uid.getValue(), summary, i);
            Topic alarmType = getAlarmType(topicMap);
            if (alarmTopic != null && alarmType != null && eventType != null && eventTopic != null) {
              Association a = topicMap.createAssociation(alarmType);
              a.addPlayer(eventTopic, eventType);
              a.addPlayer(alarmTopic, alarmType);
            }
            i++;
          }
        }

        return eventTopic;
      } else {
        log("a VEvent missing mandatory UID -- skipping.");
      }
    } catch (Exception e) {
      log(e);
    }
    return null;
  }

  /*
   * Parse a single  alarm  to a topic where the alarm's properties are mapped to the topic's
   * occurrences and associations.
   */
  public Topic parseAlarm(VAlarm alarm, TopicMap topicMap, String eventUid, String eventSummary, int i) {
    try {
      Action action = alarm.getAction();
      Trigger trigger = alarm.getTrigger();

      Description description = alarm.getDescription();
      String descString = description != null ? description.getValue() : "";
      Duration duration = alarm.getDuration();
      String durString = duration != null ? duration.getValue() : "";
      Repeat repeat = alarm.getRepeat();
      String repString = repeat != null ? repeat.getValue() : "";
      Summary summary = alarm.getSummary();
      String summString = summary != null ? summary.getValue() : "";

      if (action != null && trigger != null) {

        String trigString;
        if (trigger.getParameter("VALUE") != null && trigger.getParameter("VALUE").getValue().equals("DATE-TIME")) {
          Date trigDate = trigger.getDate();
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          trigString = df.format(trigDate);
        } else {
          Dur d = trigger.getDuration();
          trigString = d.getDays() != 0 ? d.getDays() + " days " : "";
          trigString += d.getHours() != 0 ? d.getHours() + " hours " : "";
          trigString += d.getMinutes() != 0 ? d.getMinutes() + " minutes " : "";
          trigString += d.getSeconds() != 0 ? d.getSeconds() + " seconds " : "";
          trigString += d.isNegative() ? "before" : "after";
        }
        String actString = action.getValue();
        String uidString = actString + "-" + eventUid + "-" + i;
        String basename = uidString + " (Alarm)";
        Topic defaultLangTopic = getDefaultLangTopic(topicMap);
        Topic alarmTopic = getUTopic(ICAL_ALARM_SI + uidString, getAlarmType(topicMap), topicMap);

        alarmTopic.setBaseName(basename);
        alarmTopic.setDisplayName(LANG, actString + " alarm for event \"" + eventSummary + "\"");

        Topic alarmUIDType = this.getComponentUIDType(topicMap);
        alarmTopic.setData(alarmUIDType, defaultLangTopic, uidString);

        if (!trigString.isEmpty()) {
          createOccurrence(topicMap, alarmTopic, defaultLangTopic, ICAL_TRIGGER_SI, "iCalendar alarm trigger", trigString);
        }
        if (!descString.isEmpty()) {
          createOccurrence(topicMap, alarmTopic, defaultLangTopic, ICAL_DESCRIPTION_SI, "iCalendar description", descString);
        }
        if (!durString.isEmpty()) {
          createOccurrence(topicMap, alarmTopic, defaultLangTopic, ICAL_DURATION_SI, "iCalendar alarm duration", durString);
        }
        if (!repString.isEmpty()) {
          createOccurrence(topicMap, alarmTopic, defaultLangTopic, ICAL_REPEAT_SI, "iCalendar alarm repeat", repString);
        }
        if (!summString.isEmpty()) {
          createOccurrence(topicMap, alarmTopic, defaultLangTopic, ICAL_SUMMARY_SI, "iCalendar summary", summString);
        }

        return alarmTopic;
      } else {
        log("a VAlarm is missing mandatory action or trigger -- skipping.");
      }

    } catch (Exception e) {
      log(e);
    }
    return null;
  }

  public Topic parseToDo(VToDo todo, TopicMap topicMap) {
    try {

      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
      java.util.Calendar cal = Calendar.getInstance();

      //Possible occurrences
      Uid uid = todo.getUid();
      DtStamp stamp = todo.getDateStamp();
      Summary summ = todo.getSummary();
      String summary = summ != null ? summ.getValue() : "";

      Geo ge = todo.getGeographicPos();
      String lat = (ge != null) ? todo.getGeographicPos().getLatitude().toString() : "";
      String lon = (ge != null) ? todo.getGeographicPos().getLongitude().toString() : "";
      Description desc = todo.getDescription();
      String description = desc != null ? desc.getValue() : "";
      Url url = todo.getUrl();
      String urlString = url != null ? todo.getUrl().getValue() : "";
      String percentCompleted = todo.getPercentComplete() != null ? todo.getPercentComplete().getValue() : "";
      DtStart dtstart = todo.getStartDate();
      Duration duration = todo.getDuration();
      Due due = todo.getDue();
      String startString = "";
      String dueString = "";
      
      if (duration != null && duration.getDuration() != null
          && dtstart != null && dtstart.getDate() != null) {
        Date start = todo.getStartDate().getDate();
        Date dueDate = duration.getDuration().getTime(start);
        startString = df.format(start);
        dueString = df.format(dueDate);
      } else if (due != null && due.getDate() != null) {
        Date dueDate = due.getDate();
        dueString = df.format(dueDate);
      }

      String modified = todo.getLastModified() != null ? df.format(todo.getLastModified().getDate()) : "";
      String created = todo.getCreated() != null ? df.format(todo.getCreated().getDate()) : "";
      String completed = todo.getDateCompleted() != null ? df.format(todo.getDateCompleted().getDate()) : "";

      //Possible assocs
      String dateString = todo.getDue() != null ? dfDate.format(todo.getDue().getDate()) : "";
      Priority prio = todo.getPriority();
      String priority = prio != null ? prio.getValue() : "";
      Clazz claz = todo.getClassification();
      String clazz = claz != null ? claz.getValue() : "";
      Location location = todo.getLocation();
      Property cats = todo.getProperty("CATEGORIES");
      String catString = cats != null && !cats.getValue().isEmpty() ? cats.getValue() : "";
      String[] catArray = catString.isEmpty() ? null : catString.split(",");
      Organizer org = todo.getOrganizer();
      String organizer = org != null ? org.getValue() : "";
      Status stat = todo.getStatus();
      String status = stat != null ? todo.getStatus().getValue() : "";
      RecurrenceId recur = todo.getRecurrenceId();
      String recurrence = recur != null ? recur.getDate().toString() : "";

      if (uid != null && stamp != null) {
        Topic toDoTopic = getUTopic(ICAL_TODO_SI + uid.getValue(), getToDoType(topicMap), topicMap);
        Topic toDoType = this.getToDoType(topicMap);
        Topic defaultLangTopic = getDefaultLangTopic(topicMap);

        String basename = summary.isEmpty() ? uid.getValue() + " (todo)" : summary + " (todo)";
        toDoTopic.setBaseName(basename);
        String displayname = summary.isEmpty() ? uid.getValue() : summary;
        toDoTopic.setDisplayName(LANG, displayname);

        createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_UID_SI, "iCalendar UID", uid.getValue());

        if (!summary.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_SUMMARY_SI, "iCalendar summary", summary);
        }
        if (!description.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_DESCRIPTION_SI, "iCalendar description", description);
        }
        if (!urlString.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_URL_SI, "iCalendar URL", urlString);
        }
        if (!lat.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_LAT_SI, "iCalendar latitude", lat);
        }
        if (!lon.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_LON_SI, "iCalendar longitude", lon);
        }
        if (!startString.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_START_TIME_SI, "iCalendar start time", startString);
        }
        if (!dueString.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_DUE_TIME_SI, "iCalendar due time", dueString);
        }
        if (!completed.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_TODO_DATE_COMPLETED_SI, "iCalendar ToDo date completed", completed);
        }
        if (!percentCompleted.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_TODO_COMPLETED_SI, "iCalendar ToDo percent complete", percentCompleted);
        }
        if (!created.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_CREATED_SI, "iCalendar time created", created);
        }
        if (!modified.isEmpty()) {
          createOccurrence(topicMap, toDoTopic, defaultLangTopic, ICAL_MODIFIED_SI, "iCalendar time modified", modified);
        }
        if (!dateString.isEmpty()) {
          createAssociation(topicMap, toDoType, toDoTopic, ICAL_DATE_SI, "date", "iCalendar component date", dateString);
        }
        if (!priority.isEmpty()) {
          createAssociation(topicMap, toDoType, toDoTopic, ICAL_PRIOR_SI, "priority", "iCalendar component priority", priority);
        }
        if (!clazz.isEmpty()) {
          createAssociation(topicMap, toDoType, toDoTopic, ICAL_CLASS_SI, "class", "iCalendar component class", clazz);
        }
        if (!organizer.isEmpty()) {
          createAssociation(topicMap, toDoType, toDoTopic, ICAL_ORGANIZER_SI, "organizer", "iCalendar component organizer", clazz);
        }
        if (!status.isEmpty()) {
          createAssociation(topicMap, toDoType, toDoTopic, ICAL_STATUS_SI, "status", "iCalendar component status", status);
        }
        if (!recurrence.isEmpty()) {
          createAssociation(topicMap, toDoType, toDoTopic, ICAL_RECURRENCE_SI, "recurrence", "iCalendar component recurrence", recurrence);
        }
        if (catArray != null) {
          for (String cat : catArray) {
            createAssociation(topicMap, toDoType, toDoTopic, ICAL_CAT_SI, "category", "iCalendar category", cat);
          }
        }

        if (location != null && !location.getValue().isEmpty()) {
          Topic eventLocationTopic;
          if (location.getParameter("VVENUE") != null) { // Use VVenue if found
            eventLocationTopic = topicMap.getTopic(ICAL_VENUE_SI + location.getParameter("VVENUE").getValue());
          } else {
            eventLocationTopic = getLocationTopic(urlEncode(location.getValue()), topicMap);
            eventLocationTopic.setBaseName(location.getValue());
            eventLocationTopic.setDisplayName(LANG, location.getValue());
          }
          if(eventLocationTopic != null && toDoType != null && toDoTopic != null){
            createAssociation(topicMap, eventLocationTopic, toDoType, toDoTopic, ICAL_LOCATION_SI, "location", location.getValue());
          }
        }

      } else {
        log("a VToDo is missing mandatory UID or time stamp -- skipping.");
      }

    } catch (Exception e) {
      log(e);
    }
    return null;
  }

  // -------------------------------------------------------------------------
  protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
    return getOrCreateTopic(tm, si, null);
  }

  protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
    return ExtractHelper.getOrCreateTopic(si, bn, tm);
  }

  protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn, Topic type) throws TopicMapException {
    return ExtractHelper.getOrCreateTopic(si, bn, type, tm);
  }

  protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
    ExtractHelper.makeSubclassOf(t, superclass, tm);
  }

  // -------------------------------------------------------------------------
  protected Topic getATopic(String str, String si, Topic type, TopicMap tm) throws TopicMapException {
    if (str != null && si != null) {
      str = str.trim();
      if (str.length() > 0) {
        Topic topic = getOrCreateTopic(tm, si + "/" + urlEncode(str), str);
        if (type != null) {
          topic.addType(type);
        }
        return topic;
      }
    }
    return null;
  }

  protected Topic getUTopic(String si, Topic type, TopicMap tm) throws TopicMapException {
    if (si != null) {
      si = si.trim();
      if (si.length() > 0) {
        Topic topic = getOrCreateTopic(tm, si, null);
        if (type != null) {
          topic.addType(type);
        }
        return topic;
      }
    }
    return null;
  }

  // -------------------------------------------------------------------------
  public void createAssociation(TopicMap tm, Topic pType, Topic pTopic, String SI, String siExt, String typeName, String topicName) {
    try {
      Topic type = getOrCreateTopic(tm, SI, typeName);
      Topic topic = getOrCreateTopic(tm, SI + "/" + siExt + "/" + topicName, null, type);
      createAssociation(tm, topic, pType, pTopic, SI, typeName, topicName);
    } catch (Exception e) {
      log(e);
    }
  }

  public void createAssociation(TopicMap tm, Topic topic, Topic pType, Topic pTopic, String SI, String typeName, String topicName) {
    try {
      Topic type = getOrCreateTopic(tm, SI, typeName);
      topic.setBaseName(topicName);
      topic.setDisplayName(LANG, topicName);
      if (type != null && topic != null) {
        Association a = tm.createAssociation(type);
        a.addPlayer(topic, type);
        a.addPlayer(pTopic, pType);
      }
    } catch (Exception e) {
      log(e);
    }
  }

  public void createOccurrence(TopicMap tm, Topic pTopic, Topic lt, String SI, String typeName, String topicName) {
    try {
      Topic type = getOrCreateTopic(tm, SI, typeName);
      if (type != null && pTopic != null) {
        pTopic.setData(type, lt, topicName);
      }
    } catch (Exception e) {
      log(e);
    }
  }

  // -------------------------------------------------------------------------
  public Topic getCalendarTopic(String calendar, TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_CALENDAR_SI + "/" + calendar, null, getCalendarType(tm));
  }
  public Topic getCalendarType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_CALENDAR_SI, "iCalendar calendar", getiCalendarType(tm));
  }
  public Topic getComponentUIDType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_UID_SI, "iCalendar component UID");
  }
  public Topic getDescriptionType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_DESCRIPTION_SI, "iCalendar component description");
  }
  public Topic getLocationType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_LOCATION_SI, "iCalendar component location");
  }
  public Topic getLocationTopic(String location, TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_LOCATION_SI + "/" + location, null, getLocationType(tm));
  }
  public Topic getEventType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_EVENT_SI, "iCalendar event", getiCalendarType(tm));
  }
  public Topic getVenueType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_VENUE_SI, "iCalendar venue", getiCalendarType(tm));
  }
  public Topic getAlarmType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_ALARM_SI, "iCalendar alarm", getiCalendarType(tm));
  }
  public Topic getToDoType(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, ICAL_TODO_SI, "iCalendar ToDo", getiCalendarType(tm));
  }
  
  // -------------------------------------------------------------------------
  public Topic getiCalendarType(TopicMap tm) throws TopicMapException {
    Topic type = getOrCreateTopic(tm, ICAL_SI, "iCalendar");
    Topic wandoraClass = getWandoraClass(tm);
    makeSubclassOf(tm, type, wandoraClass);
    return type;
  }
  public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
  }
  public Topic getDefaultLangTopic(TopicMap tm) throws TopicMapException {
    return getOrCreateTopic(tm, XTMPSI.getLang(LANG));
  }
  public static final String LANG = "en";
}