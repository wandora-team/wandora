/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * MultiLineLabel.java
 *
 * Created on September 11, 2004, 4:51 PM
 */

package org.wandora.utils.swing;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;



public class MultiLineLabel extends Canvas {
    
    private static final long serialVersionUID = 1L;
    
    public static final int LEFT = 0;
    public final int CENTER = 1;
    public final int RIGHT = 2;
    protected String[] lines;
    protected int num_lines;
    protected int margin_width;
    protected int margin_height;
    protected int line_width;
    protected int line_height;
    protected int line_ascent;
    protected int[] line_widths;
    protected int max_width;
    protected int alignment = LEFT;
    private List<String []> history;
    private int historyMaxSize = 1000;
    


    protected void newLabel(String label) {
        if(lines != null) {
            if(history.size() > historyMaxSize) {
                history.remove(0);
            }
            history.add(lines);
        }
        StringTokenizer t = new StringTokenizer(label, "\n");
        num_lines = t.countTokens();
        lines = new String[num_lines];
        line_widths = new int[num_lines];
        for(int i=0; i<num_lines;i++) lines[i] = t.nextToken();
    }


    protected void measure() {
        FontMetrics fm = this.getFontMetrics(this.getFont());
        if(fm == null) return;

        line_height = fm.getHeight();
        line_ascent = fm.getAscent();
        max_width = 0;
        for(int i=0; i<num_lines; i++) {
          line_widths[i] = fm.stringWidth(lines[i]);
          if(line_widths[i] > max_width) max_width = line_widths[i];
        }
    }

    public MultiLineLabel(String label, int margin_width, int margin_height, int alignment) {
        history = new ArrayList<>();
        newLabel(label);
        this.margin_width = margin_width;
        this.margin_height = margin_height;
        this.alignment = alignment;
    }

    public MultiLineLabel(String label, int margin_width, int margin_height) {
        this(label, margin_width, margin_height, LEFT);
    }

    public MultiLineLabel(String label, int alignment) {
        this(label,10,10,alignment);
    }

    public MultiLineLabel(String label) {
        this(label,10,10,LEFT);
    }


    public void setText(String text) {
        setLabel(text);
    }
    
    public void setLabel(String label) {
        newLabel(label);
        measure();
        repaint();
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        measure();
        repaint();
    }

    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        repaint();
    }

    public void setAlignment(int a) { alignment = a; repaint(); }
    public void setMarginWidth(int mw) { margin_width = mw; repaint(); }
    public void setMarginHeight(int mh) { margin_height = mh; repaint(); }
    public int getAlignment() { return alignment; }
    public int getMarginWidth() { return margin_width; }
    public int getMarginHeight() { return margin_height; }


    @Override
    public void addNotify() { super.addNotify(); measure(); }

    @Override
    public Dimension getPreferredSize() {
        Dimension superPreferred = super.getPreferredSize();
		Dimension thiPreferred = new Dimension(max_width + 2*margin_width, num_lines*line_height + 2*margin_height);
        int px = superPreferred.width > thiPreferred.width ? superPreferred.width : thiPreferred.width;
        int py = superPreferred.height > thiPreferred.height ? superPreferred.height : thiPreferred.height;
        return new Dimension(px, py);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(max_width, num_lines*line_height);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        }
        try {
            int x,y;
            Dimension d = this.getSize();   // size();
            y = line_ascent + (d.height - num_lines * line_height) / 2;

            for(int i=0; i<num_lines; i++, y+=line_height) {
              switch(alignment) {
                case LEFT: x=margin_width; break;
                case CENTER:
                default: x=(d.width - line_widths[i])/2; break;
                case RIGHT: x=d.width - margin_width -line_widths[i]; break;
              }
              g.drawString(lines[i],x,y);
            }
        }
        catch(Exception e) {
            
        }
    }
    
    
    
    
    public void setHistoryMaxSize(int maxSize) {
        historyMaxSize = maxSize;
        if(history.size() > historyMaxSize) {
            for(int i=historyMaxSize-history.size(); i>0; i--) {
                history.remove(0);
            }
        }
    }
    
    
    public String getHistoryAsString() {
        StringBuilder sb = new StringBuilder("");
        for(int i=0; i<history.size(); i++) {
            String[] historyStrings = history.get(i);
            for(int j=0; j<historyStrings.length; j++) {
                sb.append(historyStrings[j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
