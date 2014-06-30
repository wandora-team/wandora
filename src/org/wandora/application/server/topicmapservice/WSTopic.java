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
 * WSTopic.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.application.server.topicmapservice;

public class WSTopic  implements java.io.Serializable {
    private boolean full;

    private java.lang.String baseName;

    private java.lang.String[] variantTypes;

    private java.lang.String[][] variantLanguages;

    private java.lang.String[][] variantNames;

    private java.lang.String subjectLocator;

    private java.lang.String[] subjectIdentifiers;

    private java.lang.String[] types;

    private WSAssociation[] associations;

    private WSOccurrence[] occurrences;

    public WSTopic() {
    }

    public WSTopic(
           boolean full,
           java.lang.String baseName,
           java.lang.String[] variantTypes,
           java.lang.String[][] variantLanguages,
           java.lang.String[][] variantNames,
           java.lang.String subjectLocator,
           java.lang.String[] subjectIdentifiers,
           java.lang.String[] types,
           WSAssociation[] associations,
           WSOccurrence[] occurrences) {
           this.full = full;
           this.baseName = baseName;
           this.variantTypes = variantTypes;
           this.variantLanguages = variantLanguages;
           this.variantNames = variantNames;
           this.subjectLocator = subjectLocator;
           this.subjectIdentifiers = subjectIdentifiers;
           this.types = types;
           this.associations = associations;
           this.occurrences = occurrences;
    }


    /**
     * Gets the full value for this WSTopic.
     * 
     * @return full
     */
    public boolean isFull() {
        return full;
    }


    /**
     * Sets the full value for this WSTopic.
     * 
     * @param full
     */
    public void setFull(boolean full) {
        this.full = full;
    }


    /**
     * Gets the baseName value for this WSTopic.
     * 
     * @return baseName
     */
    public java.lang.String getBaseName() {
        return baseName;
    }


    /**
     * Sets the baseName value for this WSTopic.
     * 
     * @param baseName
     */
    public void setBaseName(java.lang.String baseName) {
        this.baseName = baseName;
    }


    /**
     * Gets the variantTypes value for this WSTopic.
     * 
     * @return variantTypes
     */
    public java.lang.String[] getVariantTypes() {
        return variantTypes;
    }


    /**
     * Sets the variantTypes value for this WSTopic.
     * 
     * @param variantTypes
     */
    public void setVariantTypes(java.lang.String[] variantTypes) {
        this.variantTypes = variantTypes;
    }


    /**
     * Gets the variantLanguages value for this WSTopic.
     * 
     * @return variantLanguages
     */
    public java.lang.String[][] getVariantLanguages() {
        return variantLanguages;
    }


    /**
     * Sets the variantLanguages value for this WSTopic.
     * 
     * @param variantLanguages
     */
    public void setVariantLanguages(java.lang.String[][] variantLanguages) {
        this.variantLanguages = variantLanguages;
    }


    /**
     * Gets the variantNames value for this WSTopic.
     * 
     * @return variantNames
     */
    public java.lang.String[][] getVariantNames() {
        return variantNames;
    }


    /**
     * Sets the variantNames value for this WSTopic.
     * 
     * @param variantNames
     */
    public void setVariantNames(java.lang.String[][] variantNames) {
        this.variantNames = variantNames;
    }


    /**
     * Gets the subjectLocator value for this WSTopic.
     * 
     * @return subjectLocator
     */
    public java.lang.String getSubjectLocator() {
        return subjectLocator;
    }


    /**
     * Sets the subjectLocator value for this WSTopic.
     * 
     * @param subjectLocator
     */
    public void setSubjectLocator(java.lang.String subjectLocator) {
        this.subjectLocator = subjectLocator;
    }


    /**
     * Gets the subjectIdentifiers value for this WSTopic.
     * 
     * @return subjectIdentifiers
     */
    public java.lang.String[] getSubjectIdentifiers() {
        return subjectIdentifiers;
    }


    /**
     * Sets the subjectIdentifiers value for this WSTopic.
     * 
     * @param subjectIdentifiers
     */
    public void setSubjectIdentifiers(java.lang.String[] subjectIdentifiers) {
        this.subjectIdentifiers = subjectIdentifiers;
    }


    /**
     * Gets the types value for this WSTopic.
     * 
     * @return types
     */
    public java.lang.String[] getTypes() {
        return types;
    }


    /**
     * Sets the types value for this WSTopic.
     * 
     * @param types
     */
    public void setTypes(java.lang.String[] types) {
        this.types = types;
    }


    /**
     * Gets the associations value for this WSTopic.
     * 
     * @return associations
     */
    public WSAssociation[] getAssociations() {
        return associations;
    }


    /**
     * Sets the associations value for this WSTopic.
     * 
     * @param associations
     */
    public void setAssociations(WSAssociation[] associations) {
        this.associations = associations;
    }


    /**
     * Gets the occurrences value for this WSTopic.
     * 
     * @return occurrences
     */
    public WSOccurrence[] getOccurrences() {
        return occurrences;
    }


    /**
     * Sets the occurrences value for this WSTopic.
     * 
     * @param occurrences
     */
    public void setOccurrences(WSOccurrence[] occurrences) {
        this.occurrences = occurrences;
    }

   
}
