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
 * 
 * Created on Oct 19, 2011, 8:12:21 PM
 */

package org.wandora.application.gui.topicpanels.treemap;

/**
 *
 * @author elias, akivela
 */


public class StripTreeMap {
    MapItem[] items;
    Rect layoutBox; 
    boolean lookahead = true;

    public StripTreeMap() {
    }

    public String getName() {
           return "StripTreeMap";
    }

    public String getDescription() {
           return "An Ordered Squarified TreeMap";
    }

    public void setLookahead(boolean lookahead) {
           this.lookahead = lookahead;
    }

    public void layout(TreeModel modelx, Rect bounds) {
        items = modelx.getItems();
        layoutBox = bounds;
        int i;
        double totalSize = 0;
        for (i=0; i<items.length; i++) {
            totalSize += items[i].getSize();
            //System.out.println(items[i].getSize());
        }

        double area = layoutBox.w * layoutBox.h;
        double scaleFactor = Math.sqrt(area / totalSize);

        int finishedIndex = 0;
        int numItems = 0;
        double prevAR = 0;
        double ar = 0;
        double height;
        double yoffset = 0;
        Rect box = new Rect(layoutBox);
        box.x /= scaleFactor;
        box.y /= scaleFactor;
        box.w /= scaleFactor;
        box.h /= scaleFactor;

        while(finishedIndex < items.length) {
            debug("A: finishedIndex = " + finishedIndex);
                // Layout strip
            numItems = layoutStrip(box, finishedIndex);
                // Lookahead to second strip
            if(lookahead) {
                if((finishedIndex + numItems) < items.length) {
                    int numItems2;
                    double ar2a;
                    double ar2b;

                        // Layout 2nd strip and compute AR of first strip plus 2nd strip
                    numItems2 = layoutStrip(box, finishedIndex + numItems);
                    ar2a = computeAverageAspectRatio(finishedIndex, numItems + numItems2);
                        // Layout 1st and 2nd strips together
                    computeHorizontalBoxLayout(box, finishedIndex, numItems + numItems2);
                    ar2b = computeAverageAspectRatio(finishedIndex, numItems + numItems2);
                    debug("F: numItems2 = " + numItems2 + ", ar2a="+ar2a+", ar2b="+ar2b);

                    if(ar2b < ar2a) {
                        numItems += numItems2;
                        debug("G: numItems = " + numItems);
                    } 
                    else {
                        computeHorizontalBoxLayout(box, finishedIndex, numItems);
                        debug("H: backup numItems = " + numItems);
                    }
                }
            }

            for(i=finishedIndex; i<(finishedIndex+numItems); i++) {
                items[i].getBounds().y += yoffset;
            }
            height = items[finishedIndex].getBounds().h;
            yoffset += height;
            box.y += height;
            box.h -= height;

            finishedIndex += numItems;
        }

        Rect rect;
        for(i=0; i<items.length; i++) {
            rect = items[i].getBounds();
            rect.x *= scaleFactor;
            rect.y *= scaleFactor;
            rect.w *= scaleFactor;
            rect.h *= scaleFactor;

            rect.x += bounds.x;
            rect.y += bounds.y;
            items[i].setBounds(rect);
        }
    }

    protected int layoutStrip(Rect box, int index) {
        int numItems = 0;
        double prevAR;
        double ar = Double.MAX_VALUE;
        double height;
        do {
            prevAR = ar;
            numItems++;
            height = computeHorizontalBoxLayout(box, index, numItems);
            ar = computeAverageAspectRatio(index, numItems);
            debug("L.1: numItems="+numItems+", prevAR="+prevAR+", ar="+ar);
        } 
        while ((ar < prevAR) && ((index + numItems) < items.length));
        if(ar >= prevAR) {
            numItems--;
            height = computeHorizontalBoxLayout(box, index, numItems);
            ar = computeAverageAspectRatio(index, numItems);
            debug("L.2: backup: numItems="+numItems);
        }
        return numItems;
    }

    protected double computeHorizontalBoxLayout(Rect box, int index, int numItems) {
        int i;
        double totalSize = computeSize(index, numItems);
        double height = totalSize / box.w;
        double width;
        double x = 0;

        for(i=0; i<numItems; i++) {
            width = items[i + index].getSize() / height;
            items[i + index].setBounds(x, 0, width, height);
            x += width;
        }
        return height;
    }

    public void debug(String str) {
        /*if (DEBUG) {
            System.out.println(str);
        }*/
    }

    double computeSize(int index, int num) {
        double size = 0;
        for(int i=0; i<num; i++) {
            size += items[i+index].getSize();
        }
        return size;
    }

    double computeAverageAspectRatio(int index, int numItems) {
        double ar;
        double tar = 0;
        double w, h;
        int i;
        for(i=0; i<numItems; i++) {
            w = items[i+index].getBounds().w;
            h = items[i+index].getBounds().h;
            ar = Math.max((w / h), (h / w));
            tar += ar;
        }
        tar /= numItems;
        return tar;
    }

    double computeAspectRatio(int index) {
        double w = items[index].getBounds().w;
        double h = items[index].getBounds().h;
        double ar = Math.max((w / h), (h / w));
        return ar;
    }
}
