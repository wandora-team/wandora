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
 */
package org.wandora.application.tools.exporters.iiifexport;

import java.util.ArrayList;

/**
 *
 * @author olli
 */


public class Canvas extends ModelBase {
    
    protected int width=-1;
    protected int height=-1;
    protected final ArrayList<Content> images=new ArrayList<>();
    protected final ArrayList<Content> resources=new ArrayList<>();

    public Canvas(){
        setType("sc:Canvas");        
    }
    
    public void addImage(Content image){
        this.images.add(image);
        image.setOn(this);
    }
    
    public ArrayList<Content> getImagesList(){
        return images;
    }
    
    
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
 
    
    
    @Override
    public JsonLD toJsonLD() {
        // if images list is manipulated directly,
        // the on fields might not have been set.
        for(Content c : images){
            c.setOn(this);
        }
        
        return super.toJsonLD()
                .appendNotNull("width", width<0?null:width)
                .appendNotNull("height", height<0?null:height)
                .append("images", images);
    }
    
}
