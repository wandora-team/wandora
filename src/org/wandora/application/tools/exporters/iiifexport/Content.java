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
package org.wandora.application.tools.exporters.iiifexport;

import java.util.ArrayList;

/**
 *
 * @author olli
 */


public class Content extends ModelBase {
    
    public static final String MOTIVATION_PAINTING="sc:painting";
    public static final String RESOURCE_TYPE_IMAGE="dctypes:Image";
    
    protected String resourceId; // essentially the uri for the image
    protected String resourceType;
    protected final ArrayList<Service> resourceService=new ArrayList<>();
    
    protected String format; // mime format of the resource
    protected int width=-1;
    protected int height=-1;
    protected String motivation;
    protected Canvas on;
    
    public Content(){
        setType("oa:Annotation");        
        // motivation is practically always this
        setMotivation(MOTIVATION_PAINTING);
    }
    
    public void addResourceService(Service service){
        this.resourceService.add(service);
    }
    
    public ArrayList<Service> getResourceServicesList(){
        return resourceService;
    }

    public Canvas getOn() {
        return on;
    }

    public void setOn(Canvas on) {
        this.on = on;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
        
    
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }
    
    
    public JsonLD getResourceJsonLD(){
        JsonLD jsonLD=new JsonLD();
        jsonLD.append("@id", resourceId)
              .append("@type", resourceType)
              .append("format", format)
              .appendNotEmpty("service", resourceService, true);
        return jsonLD;
    }
    
    @Override
    public JsonLD toJsonLD() {
        return super.toJsonLD()
                .appendNotNull("width", width<0?null:width)
                .appendNotNull("height", height<0?null:height)
                .appendNotNull("format", format)
                .appendNotNull("motivation", motivation)
                .appendNotNull("on",on==null?null:on.getId())
                .append("resource", getResourceJsonLD());
    }
    
    
}
