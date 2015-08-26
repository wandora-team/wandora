/*
 * Copyright (C) 2015 akivela
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


package org.wandora.application.gui.simple;


import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import static java.lang.Math.floor;
import static java.lang.String.format;
import javax.swing.JProgressBar;


/**
 * SimpleTimeSlider is a modified progress bar used to preview progress of an
 * audio and video clips.
 *
 * @author akivela
 */
public class SimpleTimeSlider extends JProgressBar {
    
    
    
    public SimpleTimeSlider() {
        super();
        setStringPainted(true);
        setBorderPainted(false);
    }
    

    @Override
    public void setString(String txt) {
        if(txt == null) txt = "";
        if(txt.length() > 100) txt = txt.substring(0, 100) + "...";
        super.setString(txt);
    }

    
    public void setMaximum(double value) {
        super.setMaximum((int) floor(value*100));
    }
    
    
    public void setMinimum(double value) {
        super.setMinimum((int) floor(value*100));
    }
    
    
    public void setValue(double value) {
        super.setValue((int) floor(value*100));
        setString(getFormatTime((int) floor(value*100), this.getMaximum()));
        setToolTipText(getString());
    }
    
    
    @Override
    public void setValue(int value) {
        super.setValue(value*100);
        setString(getFormatTime(value*100, this.getMaximum()));
        setToolTipText(getString());
    }

    
    @Override
    public int getValue() {
        int v = super.getValue();
        return v/100;
    }
    
    
    
    public int getValueFor(MouseEvent mouseEvent) {
        if(mouseEvent == null) return getValue();
        else {
            int newValue = (( (100*(mouseEvent.getX()-getX())) / getWidth()) * getMaximum()) / (100*100);
            newValue = Math.max(0, newValue);
            newValue = Math.min(super.getMaximum(), newValue);
            return newValue;
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void addMouseMotionListener(MouseMotionListener listener) {
        setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        super.addMouseMotionListener(listener);
    }
    
    
    @Override
    public void addMouseListener(MouseListener listener) {
        if(getCursor().getType() != Cursor.W_RESIZE_CURSOR) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        super.addMouseListener(listener);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    private String getFormatTime(int elapsed, int duration) {
        elapsed = elapsed/100;
        duration = duration/100;
        
        int elapsedHours = elapsed / (60 * 60);
        if (elapsedHours > 0) {
            elapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = elapsed / 60;
        int elapsedSeconds = elapsed - elapsedMinutes * 60;

        if (duration > 0) {
            int durationHours = duration / (60 * 60);
            if (durationHours > 0) {
                duration -= durationHours * 60 * 60;
            }
            int durationMinutes = duration / 60;
            int durationSeconds = duration - durationMinutes * 60;
            if (durationHours > 0) {
                return format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } 
            else {
                return format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } 
        else {
            if (elapsedHours > 0) {
                return format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    
    
}
