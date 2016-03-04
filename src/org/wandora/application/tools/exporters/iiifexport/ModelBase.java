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
 */
package org.wandora.application.tools.exporters.iiifexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for the different IIIF model objects. All the different model
 * objects have mostly the same fields so they are all defined here. What's
 * different between the different model objects is which fields are required
 * and which are only optional. Some fields may also be automatically set by
 * different model class implementations. 
 * 
 * See http://iiif.io/api/presentation/2.0/ for details.
 * 
 * @author olli
 */


public class ModelBase implements JsonLDOutput {
    protected final ArrayList<LanguageString> label=new ArrayList<>();
    protected final HashMap<String,ArrayList<LanguageString>> metaData=new HashMap<>();
    protected final ArrayList<LanguageString> description=new ArrayList<>();
    protected String thumbnail;
    
    protected final ArrayList<LanguageString> attribution=new ArrayList<>();
    protected String logo;
    protected String license;
    
    protected String id;
    protected String type;
    // format specified in content only
    // width and height specified in canvas and content only
    // viewingDirection specified in manifest and sequence only
    
    protected String viewingHint;
    
    protected final ArrayList<String> related=new ArrayList<>();
    protected final ArrayList<String> seeAlso=new ArrayList<>();
    protected String within;
    protected final ArrayList<Service> service=new ArrayList<>();
    
    // json-ld context
    protected final ArrayList<String> context=new ArrayList<>();
    
    /**
     * Check that the fields have legal values. Mostly only checks that
     * the required fields have some value rather than null.
     * 
     * TODO: add some checks, also subclasses
     * 
     * @return true if all fields are valid.
     */
    public boolean validateFields(){
        return true;
    }
    
    public void addContext(String context){
        this.context.add(context);
    }
    
    public void addLabel(LanguageString label){
        this.label.add(label);
    }
    
    public void addMetaData(String key,LanguageString value){
        if(!metaData.containsKey(key)) metaData.put(key, new ArrayList<LanguageString>());
        ArrayList<LanguageString> l=metaData.get(key);
        l.add(value);
    }
    
    public void addDescription(LanguageString description){
        this.description.add(description);
    }
    
    public void addAttribution(LanguageString attribution){
        this.attribution.add(attribution);
    }
    
    public void addRelated(String related){
        this.related.add(related);
    }
    
    public void addSeeAlso(String seeAlso){
        this.seeAlso.add(seeAlso);
    }
    
    public void addService(Service service){
        this.service.add(service);
    }
    
    public ArrayList<String> getContextsList(){
        return context;
    }

    public ArrayList<LanguageString> getLabelsList() {
        return label;
    }

    public HashMap<String, ArrayList<LanguageString>> getMetaDataMap() {
        return metaData;
    }

    public ArrayList<LanguageString> getDescriptionsList() {
        return description;
    }

    public ArrayList<LanguageString> getAttributionsList() {
        return attribution;
    }

    public ArrayList<String> getRelatedList() {
        return related;
    }

    public ArrayList<String> getSeeAlsoList() {
        return seeAlso;
    }

    public ArrayList<Service> getServicesList() {
        return service;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getViewingHint() {
        return viewingHint;
    }

    public void setViewingHint(String viewingHint) {
        this.viewingHint = viewingHint;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public ArrayList<JsonLD> metaDataToJsonLD(){
        ArrayList<JsonLD> ret=new ArrayList<>();
        for(Map.Entry<String,ArrayList<LanguageString>> e : metaData.entrySet()){
            if(e.getValue().isEmpty()) continue;
            JsonLD jsonLD=new JsonLD();
            jsonLD.append("label",e.getKey());
            jsonLD.appendNotEmpty("value", e.getValue(), true);
            ret.add(jsonLD);
        }
        return ret;
    }
    
    @Override
    public JsonLD toJsonLD() {
        JsonLD jsonLD=new JsonLD();
        
        jsonLD.appendNotEmpty("@context", context, true)
              .appendNotNull("@type", type)
              .appendNotNull("@id", id)
              .appendNotEmpty("label", label, true)
              .appendNotNull("metadata",metaData.isEmpty()?null:metaDataToJsonLD())
              .appendNotEmpty("description", description, true)
              .appendNotEmpty("attribution", attribution, true)
              .appendNotNull("logo", logo)
              .appendNotNull("license", license)
              .appendNotNull("viewingHint", viewingHint)
              .appendNotEmpty("related", related, true)
              .appendNotEmpty("seeAlso", seeAlso, true)
              .appendNotNull("within", within)
              .appendNotEmpty("service", service, true);
        
        return jsonLD;
    }
    
        
    
    public static enum ViewingDirection {
        leftToRight("left-to-right"),
        rightToLeft("right-to-left"),
        topToBottom("top-to-bottom"),
        bottomToTop("bottom-to-top");
        
        private String label;
        private ViewingDirection(String label){
            this.label=label;
        }
        @Override
        public String toString(){return label;}
    }
}
