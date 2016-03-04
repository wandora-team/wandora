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
 * CalendarGenerator.java
 *
 * Created on 22. tammikuuta 2007, 14:16
 */

package org.wandora.application.tools.generators;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.util.*;
import java.text.*;


/**
 *
 * @author akivela
 */
public class CalendarGenerator extends AbstractGenerator implements WandoraTool {
    
    public static String CALENDAR_SI_BODY = "http://wandora.org/si/calendar/";
    
    
    /** Creates a new instance of CalendarGenerator */
    public CalendarGenerator() {
    }
    

    @Override
    public String getName() {
        return "Calendar topic map generator";
    }
    @Override
    public String getDescription() {
        return "Generates a calendar topic map.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Calendar topic map generator",
            "Creates a topic map that represents one year calendar.",
            true,new String[][]{
            new String[]{"Years","string", "", "Use comma (,) to separate years. Use minus (-) to define durations."},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int[] yearNumbers = null;
        ArrayList<String> yearNumberArray = new ArrayList<String>();
        try {
            String years = values.get("Years");
            StringTokenizer st = new StringTokenizer(years, ",");
            String yearToken = null;
            while(st.hasMoreTokens()) {
                yearToken = st.nextToken();
                if(yearToken.indexOf("-") != -1) {
                    StringTokenizer dst = new StringTokenizer(yearToken, "-");
                    if(dst.countTokens() == 2) {
                        try {
                            String startYearS = dst.nextToken();
                            String endYearS = dst.nextToken();
                            int startYear = Integer.parseInt(startYearS);
                            int endYear = Integer.parseInt(endYearS);
                            for(int y=startYear; y<endYear; y++) {
                                yearNumberArray.add(""+y);
                            }
                        }
                        catch(Exception e) {
                            singleLog(e);
                        }
                    }
                    else {
                        singleLog("Invalid duration given!");
                    }
                }
                else {
                    yearNumberArray.add(yearToken);
                }
            }
            // ***** Convert the string year array to integers! *****
            yearNumbers = new int[yearNumberArray.size()];
            for(int i=0; i<yearNumberArray.size(); i++) {
                yearNumbers[i] = Integer.parseInt( yearNumberArray.get(i) );
            }
        }
        catch(Exception e) {
            singleLog("Invalid year '"+values.get("The year")+"' given!");
            return;
        }

        if(yearNumbers == null) {
            singleLog("Invalid years given!");
            return;
        }
        
        setDefaultLogger();
        setProgressMax(365 * yearNumbers.length);
        setLogTitle("Calendar generator");
        log("Creating topics for calendar");
        
        int progress = 0;
        for(int i=0; i<yearNumbers.length && !forceStop(); i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, yearNumbers[i]);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            while(calendar.get(Calendar.YEAR) == yearNumbers[i] && !forceStop()) {
                setProgress(progress++);
                log("Creating topics for " + DateFormat.getDateInstance().format(calendar.getTime()));
                generateTopics(topicmap, calendar);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        if(forceStop()) log("User interruption!");
        log("Ok!");
        setState(WAIT);
    }
    
    
    
    
   
    public void generateTopics(TopicMap tm, Calendar calendar) {
        try {
            int year = calendar.get(Calendar.YEAR);
            int month = 1+calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

            Topic yearT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"year/"+year, year+" (year)");
            Topic yearType = getOrCreateTopic(tm, CALENDAR_SI_BODY+"year", "year");
            yearT.addType(yearType);
            Topic monthT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"month/"+month, month+" (month)");
            Topic monthType = getOrCreateTopic(tm, CALENDAR_SI_BODY+"month", "month");
            monthT.addType(monthType);
            Topic weekT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"weekOfYear/"+weekOfYear, weekOfYear+" (week of year)");
            Topic weekType = getOrCreateTopic(tm, CALENDAR_SI_BODY+"week", "week");
            weekT.addType(weekType);
            Topic dayOfWeekT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"dayOfWeek/"+dayOfWeek, dayOfWeek+" (day of week)");
            Topic dayOfWeekType = getOrCreateTopic(tm, CALENDAR_SI_BODY+"weekday", "weekday");
            dayOfWeekT.addType(dayOfWeekType);
            Topic dayT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"dayOfMonth/"+day, day+" (day of month)");
            Topic dayType = getOrCreateTopic(tm, CALENDAR_SI_BODY+"monthday", "monthday");
            dayT.addType(dayType);
            
            Topic dateT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"date/"+year+"-"+month+"-"+day, year+"-"+month+"-"+day);
            Topic dateType = getOrCreateTopic(tm, CALENDAR_SI_BODY+"date", "date");
            dateT.addType(dateType);
            
            Topic formattedDate = getOrCreateTopic(tm, CALENDAR_SI_BODY+"formattedDate", "formatted date");
            dateT.setData(formattedDate, getOrCreateTopic(tm, XTMPSI.getLang(null), ""), DateFormat.getDateInstance().format(calendar.getTime()));
            
            Topic monthOfYearT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"monthOfYear", "month of year");
            Association montha = tm.createAssociation(monthOfYearT);
            montha.addPlayer(yearT, yearType);
            montha.addPlayer(monthT, monthType);
            
            Topic weekOfYearT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"weekOfYear", "week of year");
            Association weeka = tm.createAssociation(weekOfYearT);
            weeka.addPlayer(yearT, yearType);
            weeka.addPlayer(weekT, weekType);
            
            Topic dayOfMonthT = getOrCreateTopic(tm, CALENDAR_SI_BODY+"dayOfMonth", "day of month");
            Association daya = tm.createAssociation(dayOfMonthT);
            daya.addPlayer(monthT, monthType);
            daya.addPlayer(dayT, dayType);
            
            Topic isDuring = getOrCreateTopic(tm, CALENDAR_SI_BODY+"isDuring", "is during");
            Association isDuringA=null;

            isDuringA = tm.createAssociation(isDuring);
            isDuringA.addPlayer(dateT, dateType);
            isDuringA.addPlayer(yearT, yearType);
            isDuringA.addPlayer(monthT, monthType);
            isDuringA.addPlayer(weekT, weekType);
            isDuringA.addPlayer(dayOfWeekT, dayOfWeekType);
            isDuringA.addPlayer(dayT, dayOfMonthT);  

        }
        catch(Exception e) {
            log(e);
        }
        
    }
    
    
    
    
    
    
    
    
}
