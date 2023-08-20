<?xml version="1.0" encoding="utf-8"?>
<!--
  ========================================
  XTM 2.0 -> CTM 1.0 conversion stylesheet
  ========================================
  
  This stylesheet translates XTM 2.0 into Compact Topic Maps Notation (CTM) 1.0.

  Available parameters:
  - indentation: 
    Indicates whether the topic block in the output document should be indented.
    By default, this is set to 4 whitespaces ('    ').

  XTM 2.0: <http://www.isotopicmaps.org/sam/sam-xtm/2006-06-19/>
  CTM 1.0: <http://www.isotopicmaps.org/ctm/>


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
                xmlns:xtm="http://www.topicmaps.org/xtm/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" media-type="application/x-tm+ctm" encoding="utf-8"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="indentation" select="'    '"/>

  <xsl:template match="xtm:topicMap">
    <!--** Matches the xtm:topicMap element Steps: -->
    <!--@ Validate the version attribute of the topicMap -->
    <xsl:if test="not(@version) or (@version != '2.0' and @version != '2.1')">
      <xsl:message terminate="yes">Illegal input: Expected a topicMap version attribute with the value '2.0', got: <xsl:value-of select="@version"/></xsl:message>
    </xsl:if>
    <xsl:text>%encoding "utf-8"&#xA;%version 1.0&#xA;</xsl:text>
    <xsl:value-of select="concat('&#xA;# This CTM 1.0 representation was automatically generated from a XTM ', @version ,' source by&#xA;# http://topic-maps.googlecode.com/&#xA;')"/>
    <xsl:call-template name="tm-reifier"/>
    <!--@ Processes the topics -->
    <xsl:apply-templates select="xtm:topic"/>
    <!--@ Processes the associations -->
    <xsl:apply-templates select="xtm:association"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:topic">
    <!--** Matches xtm:topic. Steps: -->
    <xsl:value-of select="concat('&#xA;', @id, '&#xA;')"/>
    <xsl:apply-templates select="xtm:instanceof/xtm:topicRef"/>
    <!--@ Processes topic types -->
    <xsl:for-each select="xtm:instanceOf/xtm:topicRef">
      <xsl:call-template name="indent"/>
      <xsl:text>isa </xsl:text>
      <xsl:apply-templates select="@href"/>
      <xsl:text>;&#xA;</xsl:text>
    </xsl:for-each>
    <!--@ Processes the subject identifiers -->
    <xsl:apply-templates select="xtm:subjectIdentifier"/>
    <!--@ Processes the subject locators -->
    <xsl:apply-templates select="xtm:subjectLocator"/>
    <xsl:for-each select="xtm:itemIdentity">
      <!--@ Processes the item identifiers -->
      <xsl:call-template name="indent"/>
      <xsl:value-of select="concat('^ &lt;', @href, '&gt;;&#xA;')"/>
    </xsl:for-each>
    <!--@ Processes the names -->
    <xsl:apply-templates select="xtm:name"/>
    <!--@ Processes the occurrences -->
    <xsl:apply-templates select="xtm:occurrence"/>
    <xsl:text>.&#xA;</xsl:text>
  </xsl:template>
  
   <xsl:template match="xtm:subjectIdentifier">
    <!--** Matches subject identifiers -->
    <xsl:call-template name="indent"/>
    <xsl:value-of select="concat('&lt;', @href, '&gt;;&#xA;')"/>
  </xsl:template>

  <xsl:template match="xtm:subjectLocator">
    <!--** Matches subject locators -->
    <xsl:call-template name="indent"/>
    <xsl:value-of select="concat('= &lt;', @href, '&gt;;&#xA;')"/>
  </xsl:template>

  <xsl:template match="xtm:occurrence|xtm:name">
    <!--** Matches topic names and occurrences. Steps: -->
    <xsl:call-template name="indent"/>
    <xsl:if test="local-name() = 'name'"><xsl:text>- </xsl:text></xsl:if>
    <!--@ Processes the type of the name / occurrence -->
    <xsl:apply-templates select="xtm:type"/>
    <!--@ Processes the value of the name / occurrence -->
    <xsl:apply-templates select="xtm:resourceRef|xtm:resourceData|xtm:value"/>
    <!--@ Processes the scope of the name / occurrence -->
    <xsl:apply-templates select="xtm:scope"/>
    <!--@ Processes the reifier of the name / occurrence -->
    <xsl:apply-templates select="@reifier"/>
    <!--@ Processes the variants of a name -->
    <xsl:apply-templates select="xtm:variant"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:association">
    <!--** Matches associations Steps: -->
    <xsl:text>&#xA;</xsl:text>
    <!--@ Process the association's type -->
    <xsl:apply-templates select="xtm:type"/>
    <xsl:text>(</xsl:text>
    <!--@ Process the association's roles -->
    <xsl:apply-templates select="xtm:role"/>
    <xsl:text>)</xsl:text>
    <!--@ Process the association's scope -->
    <xsl:apply-templates select="xtm:scope"/>
    <!--@ Process the association's reifier -->
    <xsl:apply-templates select="@reifier"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:role">
    <!--** Matches association roles -->
    <xsl:if test="position() != 1">
      <xsl:text>, </xsl:text>
      <xsl:if test="position() >= 2">
        <xsl:text>&#xA;</xsl:text>
        <xsl:call-template name="indent"/>
      </xsl:if>
    </xsl:if>
    <xsl:apply-templates select="xtm:type"/>
    <xsl:apply-templates select="xtm:topicRef"/>
    <xsl:apply-templates select="@reifier"/>
  </xsl:template>

  <xsl:template match="xtm:variant">
    <!--** Matches variants -->
    <xsl:text>&#xA;</xsl:text>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="xtm:resourceRef|xtm:resourceData"/>
    <xsl:apply-templates select="xtm:scope"/>
    <xsl:apply-templates select="@reifier"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:type">
    <!--** Matches the type element -->
    <xsl:apply-templates select="*"/>
    <xsl:if test="not(parent::xtm:association)"><xsl:text>: </xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="xtm:topicRef">
    <xsl:apply-templates select="@href"/>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentifierRef">
    <!--** Translates XTM 2.1 subjectIdentifierRef into the CTM subject identifier notation -->
    <xsl:value-of select="concat('&lt;', @href, '&gt;')"/>
  </xsl:template>

  <xsl:template match="xtm:subjectLocatorRef">
    <!--** Translates XTM 2.1 subjectLocatorRef into the CTM subject locator notation -->
    <xsl:value-of select="concat('= &lt;', @href, '&gt;')"/>
  </xsl:template>

  <xsl:template match="xtm:scope">
    <!--** Converts the scope -->
    <xsl:text> @</xsl:text>
    <xsl:for-each select="*">
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="topic-ref" match="@href">
    <!--** Matches the "href" attribute from topicRef and is callable to process reifiers etc. -->
    <xsl:param name="topicref" select="."/>
    <xsl:choose>
      <xsl:when test="starts-with($topicref, '#')"><xsl:value-of select="substring-after($topicref, '#')"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="concat('^&lt;', $topicref, '&gt;')"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--=== Reification handling ===-->

  <xsl:template match="@reifier">
    <!--** Matches all reifier attributes which are not an attribute of the xtm:topicMap element -->
      <xsl:text> ~ </xsl:text>
      <xsl:call-template name="topic-ref"/>
  </xsl:template>

  <xsl:template name="tm-reifier">
    <!--** Matches all reifier attributes != the xtm:topicMap reifier attribute -->
    <xsl:if test="@reifier">
      <xsl:text>~ </xsl:text>
      <xsl:call-template name="topic-ref">
        <xsl:with-param name="topicref" select="@reifier"/>
      </xsl:call-template>
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
  </xsl:template>


  <!--=== Name, occurrence, and variant value processing ===-->

  <xsl:template match="xtm:value">
    <!--** Matches the value of topic names -->
    <xsl:apply-templates select="text()"/>
  </xsl:template>

  <xsl:template match="xtm:resourceRef|xtm:resourceData[@datatype = 'http://www.w3.org/2001/XMLSchema#anyURI']">
    <!--** Matches resourceRef and resourceData with datatype = xsd:anyURI --> 
    <xsl:value-of select="concat('&lt;', @href|text(), '&gt;')"/>
  </xsl:template>

  <xsl:template match="xtm:resourceData[not(@datatype) or @datatype = 'http://www.w3.org/2001/XMLSchema#string']">
    <!--** Matches resourceData with the datatype xsd:string --> 
    <xsl:choose>
      <xsl:when test="not(text())">""</xsl:when>
      <xsl:otherwise><xsl:apply-templates select="text()"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xtm:resourceData[@datatype != 'http://www.w3.org/2001/XMLSchema#anyURI']
                                       [@datatype != 'http://www.w3.org/2001/XMLSchema#string']">
    <!--** Matches resourceData datatype != xsd:string and datatype != xsd:anyURI --> 
    <xsl:apply-templates select="text()"/>
    <xsl:text>^^</xsl:text>
    <xsl:value-of select="concat('&lt;', @datatype, '&gt;')"/>
  </xsl:template>

  
  <!--=== Text output ===-->

  <xsl:template match="text()">
    <xsl:variable name="triple-quotes" select="contains(., '&quot;')"/>
    <xsl:text>"</xsl:text>
    <xsl:if test="$triple-quotes"><xsl:text>""</xsl:text></xsl:if>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
    <xsl:if test="$triple-quotes"><xsl:text>""</xsl:text></xsl:if>
  </xsl:template>

  
  <!--=== Named templates ===-->

  <xsl:template name="indent">
    <!--** Writes an identation string (default: 4 whitespaces) -->
    <xsl:value-of select="$indentation"/>
  </xsl:template>

</xsl:stylesheet>
