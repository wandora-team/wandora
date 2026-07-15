<?xml version="1.0" encoding="utf-8"?>
<!--
  ==================================================
  XTM 2.0 -> XTM 1.0 / XTM 1.1 conversion stylesheet
  ==================================================
  
  This stylesheet translates XTM 2.0 into XTM 1.0 and XTM 1.1.

  The supported XTM 1.1 version was the last backwards compatible 
  XTM 1.1 version but was never published as an International
  Standard.
  
  By default, this stylesheet converts a XTM 2.0 to a XTM 1.0 topic
  map, but there are parameters to change the output:
  
  - xtm_version:
    '1.0' (default) or '1.1'
  - disallow_name_type: 
    true (default) or false
    If the name type is disallowed (default), the stylesheet
    issues a warning and omits the name type. 
    If this option is disabled, the name type is written to the 
    output but results in an illegal XTM 1.0 topic map (although 
    many Topic Maps engines support an <instanceOf/> for baseNames
    The value of this parameter has no meaning if the ``xtm_version`` is 
    set to '1.1'.
  - omit_version:
    false (default) or true
    If the ``xtm_version`` is set to '1.1', the <topicMap/> 
    element gets a ``version`` attribute with the value "1.1". 
    That attribute can be omitted if this parameter is set to ``true``.
    Note: Omitting the version attribute makes the XTM 1.1 output 
    invalid since the draft mandates the version attribute.
    The value of this parameter has no meaning if the ``xtm_version`` is 
    set to '1.0'.

  Note, that item identifiers are not translated and that XTM 1.0 supports
  only one subject locator. Further, the reification mechanism in XTM 1.0
  and XTM 1.1 differs from TMDM/XTM 2.0.
  If a XTM 2.0 source contains item identifiers, reification or a topic
  has more than one subject locator, the result is not a one to one mapping.
  
  XTM 1.0: <http://www.topicmaps.org/xtm/1.0/xtm1-20010806.html>
  XTM 1.1: <http://www.isotopicmaps.org/sam/sam-xtm/2005-07-20/>
  XTM 2.0: <http://www.isotopicmaps.org/sam/sam-xtm/2006-06-19/>



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
                xmlns="http://www.topicmaps.org/xtm/1.0/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xtm="http://www.topicmaps.org/xtm/"  
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xtm">

  <xsl:output method="xml" media-type="application/x-tm+xtm" encoding="utf-8" standalone="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="xtm_version" select="'1.0'"/>
  <xsl:param name="disallow_name_type" select="true()"/>
  <xsl:param name="omit_version" select="false()"/>

  <xsl:key name="reifies" match="*[@reifier]" use="substring-after(@reifier, '#')"/>

  <xsl:template match="xtm:topicMap">
    <!-- Check version for output -->
    <xsl:if test="$xtm_version != '1.0' and $xtm_version != '1.1'">
      <xsl:message terminate="yes">Unsupported XTM version. Expected '1.0' or '1.1', got: <xsl:value-of select="$xtm_version"/></xsl:message>
    </xsl:if>
    <!-- Check version of the input -->
    <xsl:if test="not(@version) or @version != '2.0'">
      <xsl:message terminate="yes">Illegal input: Expected a topicMap version attribute with the value '2.0', got: <xsl:value-of select="@version"/></xsl:message>
    </xsl:if>
    <xsl:comment>This XTM <xsl:value-of select="$xtm_version"/> representation was automatically generated from a XTM 2.0 source by http://topic-maps.googlecode.com/</xsl:comment>
    <topicMap>
      <xsl:if test="not($omit_version) and $xtm_version = '1.1'">
        <xsl:attribute name="version"><xsl:value-of select="$xtm_version"/></xsl:attribute>
      </xsl:if>
      <xsl:call-template name="reifier"/>
      <xsl:apply-templates/>
      <xsl:call-template name="post-process-reification"/>
    </topicMap>
  </xsl:template>

  <!-- topics -->
  <xsl:template match="xtm:topic">
    <topic id="{@id}">
      <!-- types -->
      <xsl:apply-templates select="xtm:instanceOf"/>
      <!-- subjectIdentity iif there are sids, slos or if the topic reifies another construct -->
      <xsl:if test="xtm:subjectIdentifier or xtm:subjectLocator or key('reifies', @id)">
        <subjectIdentity>
          <!-- reification -->
          <xsl:if test="key('reifies', @id)">
            <subjectIndicatorRef xlink:href="#{generate-id(key('reifies', @id))}"/>
          </xsl:if>
          <!-- sids / slos -->
          <xsl:apply-templates select="xtm:subjectIdentifier|xtm:subjectLocator"/>
        </subjectIdentity>
      </xsl:if>
      <xsl:apply-templates select="xtm:name"/>
      <xsl:apply-templates select="xtm:occurrence"/>
    </topic>
  </xsl:template>
  
  <!-- topic types -->
  <xsl:template match="xtm:instanceOf">
    <xsl:for-each select="xtm:topicRef">
      <instanceOf>
        <xsl:apply-templates select="."/>
      </instanceOf>
    </xsl:for-each>
  </xsl:template>
  
  <!-- sids -->
  <xsl:template match="xtm:subjectIdentifier">
      <subjectIndicatorRef xlink:href="{@href}"/>
  </xsl:template>
  
  <!-- slos -->
  <xsl:template match="xtm:subjectLocator">
    <xsl:choose>
      <xsl:when test="position() != 1 and $xtm_version != '1.1'">
        <xsl:variable name="msg" select="concat('WARN: Omitting subject locator: ', @href, ' ')"/>
        <xsl:comment><xsl:value-of select="$msg"/></xsl:comment>
        <xsl:message><xsl:value-of select="$msg"/></xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <resourceRef xlink:href="{@href}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- roles -->
  <xsl:template match="xtm:role">
    <member>
      <xsl:call-template name="reifier"/>
      <roleSpec><xsl:apply-templates select="xtm:type/xtm:topicRef"/></roleSpec>
      <xsl:apply-templates select="xtm:topicRef"/>
    </member>
  </xsl:template>

  <xsl:template match="xtm:topicRef">
    <topicRef xlink:href="{@href}"/>
  </xsl:template>

  <!-- associations / occurrences -->
  <xsl:template match="xtm:association|xtm:occurrence">
    <xsl:element name="{local-name()}">
      <xsl:call-template name="reifier"/>
      <xsl:apply-templates select="child::*"/>
    </xsl:element>
  </xsl:template>

  <!-- names -->
  <xsl:template match="xtm:name">
    <baseName>
      <xsl:call-template name="reifier"/>
      <xsl:apply-templates select="child::*"/>
    </baseName>
  </xsl:template>

  <!-- name value -->
  <xsl:template match="xtm:value">
    <baseNameString><xsl:value-of select="."/></baseNameString>
  </xsl:template>

  <!-- variants -->
  <xsl:template match="xtm:variant">
    <variant>
      <xsl:call-template name="reifier"/>
      <parameters>
        <xsl:apply-templates select="xtm:scope/xtm:topicRef"/>
      </parameters>
      <variantName><xsl:apply-templates select="xtm:resourceRef|xtm:resourceData"/></variantName>
    </variant>
  </xsl:template>

  <!-- type -->
  <xsl:template match="xtm:type">
    <xsl:choose>
      <xsl:when test="parent::xtm:name and $xtm_version = '1.0' and $disallow_name_type">
        <xsl:variable name="msg" select="concat('WARN: Skipping the name type declaration: ', xtm:topicRef/@href, ' ')"/>
        <xsl:comment><xsl:value-of select="$msg"/></xsl:comment>
        <xsl:message><xsl:value-of select="$msg"/></xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <instanceOf>
          <xsl:apply-templates select="xtm:topicRef"/>
        </instanceOf>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- scope -->
  <xsl:template match="xtm:scope">
    <scope>
      <xsl:apply-templates select="xtm:topicRef"/>
    </scope>
  </xsl:template>

  <!-- xsd:anyURI -->
  <xsl:template match="xtm:resourceRef|xtm:resourceData[@datatype = 'http://www.w3.org/2001/XMLSchema#anyURI']">
    <resourceRef xlink:href="{@href|text()}"/>
  </xsl:template>

  <!-- xsd:string and others -->
  <xsl:template match="xtm:resourceData[not(@datatype) or @datatype != 'http://www.w3.org/2001/XMLSchema#anyURI']">
    <xsl:if test="@datatype and @datatype != 'http://www.w3.org/2001/XMLSchema#string' and $xtm_version = '1.0'">
      <xsl:variable name="msg" select="concat('WARN: The datatype is not supported by XTM 1.0: ', @datatype, ' ')"/>
      <xsl:comment><xsl:value-of select="$msg"/></xsl:comment>
      <xsl:message><xsl:value-of select="$msg"/></xsl:message>
    </xsl:if>
    <resourceData>
      <xsl:if test="@datatype and $xtm_version = '1.1'">
        <xsl:attribute name="datatype"><xsl:value-of select="@datatype"/></xsl:attribute>
      </xsl:if>
      <xsl:value-of select="."/>
    </resourceData>
  </xsl:template>
  
  <!-- reification -->
  <xsl:template name="reifier">
    <xsl:if test="@reifier">
      <xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
    </xsl:if>
  </xsl:template>

  <!-- 
    Handles the reification of constructs where the referenced topic 
    has no explicit <topic/> element like
    
    <topicMap version="2.0" 
              reifier="#reifier"/>
  -->
  <xsl:template name="post-process-reification">
    <xsl:for-each select="//*[@reifier]">
      <xsl:variable name="reifier-id" select="substring-after(@reifier, '#')"/>
      <xsl:if test="not(/xtm:topicMap/xtm:topic[@id = $reifier-id])">
        <topic id="{$reifier-id}">
          <subjectIdentity>
            <subjectIndicatorRef xlink:href="#{generate-id()}"/>
          </subjectIdentity>
        </topic>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
