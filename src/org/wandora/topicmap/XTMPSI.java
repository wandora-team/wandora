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
 * XTMPSI.java
 *
 * Created on June 7, 2004, 10:38 AM
 */

package org.wandora.topicmap;

/**
 * This class provides some standard topic map subject identifiers.
 * @author  olli
 */
public class XTMPSI {
    /** 
     * The base URL for XTM 1.0 (not actually an XTM PSI). 
     */
    public static final String XTM1_BASE = "http://www.topicmaps.org/xtm/1.0/"; 
    /** 
     * The base URL for all XTM 1.0 PSIs. 
     */
    public static final String PSI_DOC = XTM1_BASE + "core.xtm#";
    /** 
     * Suitability of a topic name for display; for use in the parameters of variant names.
     */
    public static final String SORT = PSI_DOC + "sort";
    /** 
     * Suitability of a topic name for use as a sort key; for use in the parameters of variant names.
     */ 
    public static final String DISPLAY = PSI_DOC + "display"; 
    /** 
     * The core concept of class-instance; the class of association that represents class-instance relationships between topics, and that is semantically equivalent to the use of &lt;instanceOf> subelements.
     */ 
    public static final String CLASS_INSTANCE = PSI_DOC + "class-instance"; 
    /** 
     * The core concept of class; the role of class as played by one of the members of a class-instance association.
     */ 
    public static final String CLASS = PSI_DOC + "class"; 
    /** 
     * The core concept of instance; the role of instance as played by one of the members of a class-instance association.
     */ 
    public static final String INSTANCE = PSI_DOC + "instance"; 
    /** 
     * The core concept of superclass-subclass; the class of association that represents superclass-subclass relationships between topics.
     */ 
    public static final String SUPERCLASS_SUBCLASS = PSI_DOC + "superclass-subclass"; 
    /** 
     * The core concept of superclass; the role of superclass as played by one of the members of a superclass-subclass association.
     */ 
    public static final String SUPERCLASS = PSI_DOC + "superclass"; 
    /** 
     * The core concept of subclass; the role of subclass as played by one of the members of a superclass-subclass association.
     */ 
    public static final String SUBCLASS = PSI_DOC + "subclass"; 
    /** 
     * The core concept of topic; the generic class to which all topics belong unless otherwise specified.
     */ 
    public static final String TOPIC = PSI_DOC + "topic"; 
    /** 
     * The core concept of association; the generic class to which all associations belong unless otherwise specified.
     */ 
    public static final String ASSOCIATION = PSI_DOC + "association"; 
    /** 
     * The core concept of occurrence; the generic class to which all occurrences belong unless otherwise specified.
     */ 
    public static final String OCCURRENCE = PSI_DOC + "occurrence";    
    

    public static final String LANGUAGE = "http://www.topicmaps.org/xtm/1.0/language.xtm";
    /**
     * Prefix for language topics.
     */    
    public static final String LANG_PREFIX = LANGUAGE + "#";
    /**
     * Language independent.
     */
    public static final String LANG_INDEPENDENT = "http://wandora.org/si/core/lang-independent";
    /**
     * Returns the subject identifier for the specified language or if a null
     * http://wandora.orgsi/core/lang-independent
     * denoting a language independent version.
     */
    public static String getLang(String lang) {
        if(lang != null && lang.length()>0) return LANG_PREFIX+lang.toLowerCase();
        else return LANG_INDEPENDENT;
    }
    
}
