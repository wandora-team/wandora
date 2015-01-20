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
 * WSTopic.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.webapi.implementation;

public class WSTopic  implements java.io.Serializable {
    private boolean full;

    private java.lang.String baseName;

    private java.lang.String[] variantTypes;

    private java.lang.String[][] variantLanguages;

    private java.lang.String[][] variantNames;

    private java.lang.String subjectLocator;

    private java.lang.String[] subjectIdentifiers;

    private java.lang.String[] types;

    private org.wandora.webapi.implementation.WSAssociation[] associations;

    private org.wandora.webapi.implementation.WSOccurrence[] occurrences;

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
           org.wandora.webapi.implementation.WSAssociation[] associations,
           org.wandora.webapi.implementation.WSOccurrence[] occurrences) {
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
    public org.wandora.webapi.implementation.WSAssociation[] getAssociations() {
        return associations;
    }


    /**
     * Sets the associations value for this WSTopic.
     * 
     * @param associations
     */
    public void setAssociations(org.wandora.webapi.implementation.WSAssociation[] associations) {
        this.associations = associations;
    }


    /**
     * Gets the occurrences value for this WSTopic.
     * 
     * @return occurrences
     */
    public org.wandora.webapi.implementation.WSOccurrence[] getOccurrences() {
        return occurrences;
    }


    /**
     * Sets the occurrences value for this WSTopic.
     * 
     * @param occurrences
     */
    public void setOccurrences(org.wandora.webapi.implementation.WSOccurrence[] occurrences) {
        this.occurrences = occurrences;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WSTopic)) return false;
        WSTopic other = (WSTopic) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.full == other.isFull() &&
            ((this.baseName==null && other.getBaseName()==null) || 
             (this.baseName!=null &&
              this.baseName.equals(other.getBaseName()))) &&
            ((this.variantTypes==null && other.getVariantTypes()==null) || 
             (this.variantTypes!=null &&
              java.util.Arrays.equals(this.variantTypes, other.getVariantTypes()))) &&
            ((this.variantLanguages==null && other.getVariantLanguages()==null) || 
             (this.variantLanguages!=null &&
              java.util.Arrays.equals(this.variantLanguages, other.getVariantLanguages()))) &&
            ((this.variantNames==null && other.getVariantNames()==null) || 
             (this.variantNames!=null &&
              java.util.Arrays.equals(this.variantNames, other.getVariantNames()))) &&
            ((this.subjectLocator==null && other.getSubjectLocator()==null) || 
             (this.subjectLocator!=null &&
              this.subjectLocator.equals(other.getSubjectLocator()))) &&
            ((this.subjectIdentifiers==null && other.getSubjectIdentifiers()==null) || 
             (this.subjectIdentifiers!=null &&
              java.util.Arrays.equals(this.subjectIdentifiers, other.getSubjectIdentifiers()))) &&
            ((this.types==null && other.getTypes()==null) || 
             (this.types!=null &&
              java.util.Arrays.equals(this.types, other.getTypes()))) &&
            ((this.associations==null && other.getAssociations()==null) || 
             (this.associations!=null &&
              java.util.Arrays.equals(this.associations, other.getAssociations()))) &&
            ((this.occurrences==null && other.getOccurrences()==null) || 
             (this.occurrences!=null &&
              java.util.Arrays.equals(this.occurrences, other.getOccurrences())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += (isFull() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getBaseName() != null) {
            _hashCode += getBaseName().hashCode();
        }
        if (getVariantTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getVariantTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getVariantTypes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getVariantLanguages() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getVariantLanguages());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getVariantLanguages(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getVariantNames() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getVariantNames());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getVariantNames(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getSubjectLocator() != null) {
            _hashCode += getSubjectLocator().hashCode();
        }
        if (getSubjectIdentifiers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSubjectIdentifiers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSubjectIdentifiers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTypes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAssociations() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAssociations());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAssociations(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getOccurrences() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOccurrences());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOccurrences(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WSTopic.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:WandoraService", "WSTopic"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("full");
        elemField.setXmlName(new javax.xml.namespace.QName("", "full"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("baseName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "baseName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("variantTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "variantTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("variantLanguages");
        elemField.setXmlName(new javax.xml.namespace.QName("", "variantLanguages"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("variantNames");
        elemField.setXmlName(new javax.xml.namespace.QName("", "variantNames"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subjectLocator");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subjectLocator"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subjectIdentifiers");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subjectIdentifiers"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("types");
        elemField.setXmlName(new javax.xml.namespace.QName("", "types"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("associations");
        elemField.setXmlName(new javax.xml.namespace.QName("", "associations"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:WandoraService", "WSAssociation"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("occurrences");
        elemField.setXmlName(new javax.xml.namespace.QName("", "occurrences"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:WandoraService", "WSOccurrence"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
