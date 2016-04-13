<?xml version="1.0" encoding="utf-8"?>
<!-- 
  ======================================
  XTM 1.0 -> XTM 2 conversion stylesheet
  ======================================
  
  This stylesheet translates XTM 1.0 into XTM 2.
  
  Available parameters:
  - xtm_version:
    '2.0' (default) or '2.1'
  - omit_reification_identities: 
    true (default) or false
    If a subject identifier / item identifier pair is found that 
    estabilishes the reification of a construct, the item identifier /
    subject identifier is omitted if this parameter is set to ``true``
  - omit_item_identifiers
    false (default) or true
    If a construct != topic has an ``id`` attribute, an <itemIdentity/>
    element is created if this parameter is not set to ``true``.
    Note: If the ``id`` attribute is used to reify a construct, the 
    creation of the <itemIdentity/> element depends on the value of
    the ``omit_reification_identities`` parameter.
  - omit_mergemap:
    true (default) or false
    If a <mergeMap/> element is found, it is translated to a XTM 2.0
    <mergeMap/> element unless this parameter is set to ``true``.
  

  XTM 1.0: <http://www.topicmaps.org/xtm/1.0/xtm1-20010806.html>
  XTM 2.0: <http://www.isotopicmaps.org/sam/sam-xtm/2006-06-19/>

  Authors: 
  - Alexander Mikhailian <ami at spaceapplications.com>
  - Lars Heuer <heuer[at]semagia.com>

  This stylesheet is published under the same conditions as the 
  original stylesheet found at <http://www.topiwriter.com/misc/xtm1toxtm2.html>.
  
  Changes against the original version:
  - Support for XTM 2.1
  - Made "http://www.topicmaps.org/xtm/" to the default namespace and
    therefor switched from <xsl:element name="..."/> to the concrete XTM 2
    element
  - All types of the XTM 1.0 topics are taken into account
  - Made translation of the <mergeMap/> element optional
  - Dropped all tmdm:glossary terms
  - Usage of XTM 1.0 default types instead of terminating the translation
  - <member/> elements with multiple players are translated into multiple roles
  - Renamed namespace "tm1" into "xtm"
  - Support for nested variants
  - Corrected and simplyfied reification handling
  - Avoid loosing information about topics which are referenced by 
    <subjectIndicatorRef/> and <resourceRef/>

  
  Copyright (c) 2007, Space Applications Services
  Copyright (c) 2009, Semagia - Lars Heuer <http://www.semagia.com/>
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

     * Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following
       disclaimer in the documentation and/or other materials provided
       with the distribution.

     * Neither the name of the copyright holders nor the names of the 
       contributors may be used to endorse or promote products derived 
       from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xtm="http://www.topicmaps.org/xtm/1.0/"
                xmlns="http://www.topicmaps.org/xtm/"
                exclude-result-prefixes="xtm xlink">

  <xsl:output method="xml" media-type="application/x-tm+xtm" encoding="utf-8" standalone="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="xtm_version" select="'2.0'"/>
  <xsl:param name="omit_reification_identities" select="true()"/>
  <xsl:param name="omit_item_identifiers" select="false()"/>
  <xsl:param name="omit_mergemap" select="true()"/>

  <xsl:key name="reifies" 
           match="xtm:subjectIdentity/xtm:subjectIndicatorRef/@xlink:href[starts-with(., '#')]" 
           use="."/>

  <xsl:key name="reifiable" match="xtm:*[local-name() != 'topic']" use="concat('#', @id)"/>

  <xsl:template match="xtm:*" >
    <!--** Copy and change the namespace from XTM 1.0 to XTM 2 -->
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@xlink:href">
    <!--** Rename xlink:href to href -->
    <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="xtm:subjectIndicatorRef">
    <!--** Translates subjectIndicatorRef to XTM 2 -->
    <xsl:choose>
      <xsl:when test="$xtm_version = '2.0'"><topicRef href="#{generate-id(.)}"/></xsl:when>
      <xsl:otherwise><subjectIdentifierRef href="{@xlink:href}"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xtm:resourceRef[local-name(..) != 'occurrence'][local-name(..) != 'variantName']">
    <!--** Translates resourceRef to subject locator -->
    <xsl:choose>
      <xsl:when test="$xtm_version = '2.0'"><topicRef href="#{generate-id(.)}"/></xsl:when>
      <xsl:otherwise><subjectLocatorRef href="{@xlink:href}"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xtm:topicMap">
    <!--** topic map -->
    <xsl:if test="$xtm_version != '2.0' and $xtm_version != '2.1'">
      <xsl:message terminate="yes">Unsupported XTM version. Expected '2.0' or '2.1'</xsl:message>
    </xsl:if>
    <xsl:comment>This XTM <xsl:value-of select="$xtm_version"/> representation was automatically generated from a XTM 1.0 source by http://topic-maps.googlecode.com/</xsl:comment>
    <topicMap version="{$xtm_version}">
      <xsl:apply-templates select="@id"/>
      <xsl:apply-templates/>
      <xsl:if test="$xtm_version = '2.0'">
        <xsl:call-template name="post-process"/>
      </xsl:if>
    </topicMap>
  </xsl:template>

  <xsl:template match="xtm:mergeMap">
    <!--** Translate mergeMap (if it should not be omitted) -->
    <xsl:if test="not($omit_mergemap)">
      <mergeMap href="{@xlink:href}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@id">
    <!--** Convert @id into itemIdentity iff construct != topic -->
    <xsl:choose>
      <xsl:when test="key('reifies', concat('#', .))">
        <xsl:attribute name="reifier"><xsl:value-of select="concat('#', key('reifies', concat('#', .))/ancestor::xtm:topic/@id)"/></xsl:attribute>
        <xsl:if test="not($omit_reification_identities)">
          <itemIdentity href="{concat('#', .)}"/>
        </xsl:if>
      </xsl:when>
      <xsl:when test="not($omit_item_identifiers)">
        <itemIdentity href="{concat('#', .)}"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xtm:instanceOf[not(parent::xtm:topic)]|xtm:roleSpec">
    <!--** Translates the instanceOf element of constructs != topic to type and roleSpec to type -->
    <type>
      <xsl:apply-templates/>
    </type>
  </xsl:template>

  <xsl:template match="xtm:baseName">
    <!-- baseName -> name -->
    <name>
      <xsl:apply-templates select="@id"/>
      <xsl:apply-templates/>
    </name>
  </xsl:template>

  <xsl:template match="xtm:baseNameString">
    <!--** baseNameString -> value -->
    <value><xsl:value-of select="."/></value>
  </xsl:template>

  <xsl:template match="xtm:variant">
    <!--** variants -->
    <variant>
      <xsl:apply-templates select="@id"/>
      <xsl:apply-templates select="*[local-name() != 'variant']"/>
    </variant>
    <xsl:apply-templates select="xtm:variant"/>
  </xsl:template>

  <xsl:template match="xtm:parameters">
    <!--**  parameters -> scope (takes all parameters of the ancestor variants (if any) into account) -->
    <scope>
      <xsl:apply-templates/>
      <!-- add the scope of the parent variants -->
      <xsl:apply-templates select="../ancestor::xtm:variant/xtm:parameters/*"/>
    </scope>
  </xsl:template>

  <xsl:template match="xtm:variantName">
    <!--** Ignores the variantName element and processes the children of this element -->
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentity">
    <!--** Ignores the subjectIdentity element and processes the children of this element -->
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xtm:occurrence">
    <!--** Occurrences -->
    <occurrence>
      <xsl:apply-templates select="@id"/>
      <xsl:if test="count(xtm:instanceOf) = 0">
        <type><topicRef href="http://www.topicmaps.org/xtm/1.0/core.xtm#occurrence"/></type>
      </xsl:if>
      <xsl:apply-templates/>
    </occurrence>
  </xsl:template>

  <xsl:template match="xtm:association">
    <!--** Associations -->
    <association>
      <xsl:apply-templates select="@id"/>
      <xsl:if test="count(xtm:instanceOf) = 0">
        <type><topicRef href="http://www.topicmaps.org/xtm/1.0/core.xtm#association"/></type>
      </xsl:if>
      <xsl:apply-templates/>
    </association>
  </xsl:template>

  <xsl:template match="xtm:member">
    <!--** Matches association roles. Steps: -->
    <!--@ If the role has multiple players, create a role for each of these players -->
    <xsl:for-each select="xtm:topicRef|xtm:resourceRef|xtm:subjectIndicatorRef">
      <role>
        <xsl:choose>
          <!--@ If the role has no type, use a default type -->
          <xsl:when test="count(../xtm:roleSpec) = 0">
            <!-- This may cause an error if a XTM 2.0 parser checks if the 
                 <topicRef/> contains a fragment identifier 
                 Anyway, this XTM 2.0 'feature' is speculative and untyped roles are bad :)
            -->
            <type><topicRef href="http://psi.semagia.com/xtm/1.0/role"/></type>
          </xsl:when>
          <!--@ Use the role's type if specified -->
          <xsl:otherwise>
            <xsl:apply-templates select="../xtm:roleSpec"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="."/>
      </role>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentity/xtm:subjectIndicatorRef">
    <!--** subjectIdentity/subjectIndicatorRef -> subjectIdentifier -->
    <xsl:if test="not($omit_reification_identities and key('reifiable', @xlink:href))">
      <subjectIdentifier>
        <!-- renaming some of the association types and role types-->
        <xsl:choose>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#class-instance'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/type-instance</xsl:attribute>
          </xsl:when>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#class'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/type</xsl:attribute>
          </xsl:when>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#instance'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/instance</xsl:attribute>
          </xsl:when>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/supertype-subtype</xsl:attribute>
          </xsl:when>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#superclass'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/supertype</xsl:attribute>
          </xsl:when>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#subclass'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/subtype</xsl:attribute>
          </xsl:when>
          <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#sort'">
            <xsl:attribute name="href">http://psi.topicmaps.org/iso13250/model/sort</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="href"><xsl:value-of select="@xlink:href"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </subjectIdentifier>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentity/xtm:resourceRef">
    <!--** subjectIdentity/resourceRef -> subjectLocator -->
    <subjectLocator href="{@xlink:href}"/>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentity/xtm:topicRef">
    <!--** subjectIdentity/topicRef -> itemIdentity -->
    <itemIdentity href="{@xlink:href}"/>
  </xsl:template>

  <xsl:template match="xtm:topic">
    <!--** Topics -->
    <topic id="{@id}">
      <xsl:apply-templates select="xtm:subjectIdentity"/>
      <xsl:if test="count(xtm:instanceOf) != 0">
        <instanceOf>
          <xsl:for-each select="xtm:instanceOf/*">
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </instanceOf>
      </xsl:if>
      <xsl:apply-templates select="child::*[local-name() != 'instanceOf']
                                           [local-name() != 'subjectIdentity']"/>
    </topic>
  </xsl:template>

  <xsl:template name="post-process">
    <!--** Since XTM 2.0 knows only topicRef to reference topics, the information if
           a topic is referenced by a subject identifier / subject locator is lost.
           This template adds the information back. Steps: 
    -->
    <!--@ Process subject identifiers -->
    <xsl:for-each select="xtm:association/xtm:member/xtm:subjectIndicatorRef|xtm:association/xtm:member/xtm:roleSpec/xtm:subjectIndicatorRef|//xtm:instanceOf/xtm:subjectIndicatorRef|//xtm:scope/xtm:subjectIndicatorRef|//xtm:parameters/xtm:subjectIndicatorRef">
      <topic id="{generate-id(.)}">
        <subjectIdentifier href="{@xlink:href}"/>
      </topic>
    </xsl:for-each>
    <!--@ Process role players / themes which are referenced by their subject locator -->
    <xsl:for-each select="xtm:association/xtm:member/xtm:resourceRef|//xtm:scope/xtm:resourceRef">
      <topic id="{generate-id(.)}">
        <subjectLocator href="{@xlink:href}"/>
      </topic>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
