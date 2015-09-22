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
 * 
 *
 * CalendarDuration.java
 *
 * Created on July 17, 2003, 4:27 PM
 */

package org.wandora.utils;


import java.util.*;




public class CalendarDuration {
    
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    
    /** Creates a new instance of CalendarDuration */
    public CalendarDuration() {
    }
    
    
    
    public CalendarDuration(Calendar start, Calendar end) {
        setStartCalendar(start);
        setEndCalendar(end);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public void setStartCalendar(Calendar calendar) {
        this.startCalendar = calendar;
    }
    
    public void setEndCalendar(Calendar calendar) {
        this.endCalendar = calendar;
    }
    
    public void setStart(int year, int mon, int day, int hour, int minute, int second) {
        if(startCalendar == null) startCalendar = Calendar.getInstance();
        startCalendar.set(year,mon,day,hour,minute,second);
    }

    public void setEnd(int year, int mon, int day, int hour, int minute, int second) {
        if(endCalendar == null) endCalendar = Calendar.getInstance();
        endCalendar.set(year,mon,day,hour,minute,second);
    }

    
    public void set(String formattedDuration) {
        if(formattedDuration.length() > 0) {
            try {
                setStart(
                    Integer.parseInt(formattedDuration.substring(0,4)),
                    Integer.parseInt(formattedDuration.substring(4,6)),
                    Integer.parseInt(formattedDuration.substring(6,8)),
                    Integer.parseInt(formattedDuration.substring(8,10)),
                    Integer.parseInt(formattedDuration.substring(10,12)),
                    Integer.parseInt(formattedDuration.substring(12,14))
                );
            }
            catch (Exception e) {
                startCalendar = null;
            }
            try {
                setEnd(
                    Integer.parseInt(formattedDuration.substring(15,19)),
                    Integer.parseInt(formattedDuration.substring(19,21)),
                    Integer.parseInt(formattedDuration.substring(21,23)),
                    Integer.parseInt(formattedDuration.substring(23,25)),
                    Integer.parseInt(formattedDuration.substring(25,27)),
                    Integer.parseInt(formattedDuration.substring(27,29))
                );
            }
            catch (Exception e) {
                endCalendar = null;
            }
        }
    }

    
    
    public Calendar getStartCalendar() {
        return this.startCalendar;
    }
    
    public Calendar getEndCalendar() {
        return this.endCalendar;
    }
    
    public String getFormattedStartCalendar() {
        return formatCalendar(startCalendar);
    }
    
    public String getFormattedEndCalendar() {
        return formatCalendar(endCalendar);
    }
    
    
    
    private String formatCalendar(Calendar cal) {
        if(cal != null) {
            return "" +
                cal.get(Calendar.DAY_OF_MONTH) + "." +
                cal.get(Calendar.MONTH) + "." +
                cal.get(Calendar.YEAR) + " " +
                cal.get(Calendar.HOUR_OF_DAY) + ":" +
                (cal.get(Calendar.MINUTE)<10 ? "0" + cal.get(Calendar.MINUTE) : "" + cal.get(Calendar.MINUTE)) + "." +
                (cal.get(Calendar.SECOND)<10 ? "0" + cal.get(Calendar.SECOND) : "" + cal.get(Calendar.SECOND));
        }
        return "";
    }
    
    
}
