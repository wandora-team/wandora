<?xml version="1.0" encoding="utf-8"?>
<!--
  ========================================
  XTM 1.0 -> CTM 1.0 conversion stylesheet
  ========================================
  
  This stylesheet translates XTM 1.0 into Compact Topic Maps Notation (CTM) 1.0.

  Available parameters:
  - indentation: 
    Indicates whether the topic block in the output document should be indented.
    By default, this is set to 4 whitespaces ('    ').

  XTM 1.0: <http://www.topicmaps.org/xtm/1.0/xtm1-20010806.html>
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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xtm="http://www.topicmaps.org/xtm/1.0/"
                xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:output method="text" media-type="application/x-tm+ctm" encoding="utf-8"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="indentation" select="'    '"/>

  <xsl:key name="reifies" 
           match="xtm:subjectIdentity/xtm:subjectIndicatorRef/@xlink:href[starts-with(., '#')]" 
           use="."/>

  <xsl:key name="reifiable" match="xtm:*[local-name() != 'topic']/@id" use="concat('#', .)"/>

  <xsl:template match="xtm:topicMap">
    <!--** Matches the xtm:topicMap element Steps: -->
    <xsl:text>%encoding "utf-8"&#xA;%version 1.0&#xA;</xsl:text>
    <xsl:text>&#xA;# This CTM 1.0 representation was automatically generated from a XTM 1.0 source by&#xA;# http://topic-maps.googlecode.com/&#xA;</xsl:text>
    <xsl:text>&#xA;%prefix xtm &lt;http://www.topicmaps.org/xtm/1.0/core.xtm#&gt;&#xA;%prefix tmdm &lt;http://psi.topicmaps.org/iso13250/model/&gt;&#xA;&#xA;</xsl:text>
    <!--@ Handle the reifier, if any -->
    <xsl:apply-templates select="@id"/>
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
    <xsl:for-each select="xtm:instanceOf/*">
      <xsl:call-template name="indent"/>
      <xsl:text>isa </xsl:text>
      <xsl:apply-templates select="."/>
      <xsl:text>;&#xA;</xsl:text>
    </xsl:for-each>
    <!--@ Process the identities -->
    <xsl:apply-templates select="xtm:subjectIdentity"/>
    <!--@ Processes the names -->
    <xsl:apply-templates select="xtm:baseName"/>
    <!--@ Process the occurrences -->
    <xsl:apply-templates select="xtm:occurrence"/>
    <xsl:text>.&#xA;</xsl:text>
  </xsl:template>
  
  <xsl:template match="xtm:subjectIdentity/xtm:subjectIndicatorRef">
    <!--** Translates subjectIdentity/subjectIndicatorRef into subjectIdentifier -->
    <xsl:if test="not(key('reifiable', @xlink:href))">
      <xsl:call-template name="indent"/>
      <xsl:choose>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#class-instance'">
          <xsl:text>tmdm:type-instance</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#class'">
          <xsl:text>tmdm:type</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#instance'">
          <xsl:text>tmdm:instance</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass'">
          <xsl:text>tmdm:supertype-subtype</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#superclass'">
          <xsl:text>tmdm:supertype</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#subclass'">
          <xsl:text>tmdm:subtype</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#sort'">
          <xsl:text>tmdm:sort</xsl:text>
        </xsl:when>
        <xsl:when test="@xlink:href='http://www.topicmaps.org/xtm/1.0/core.xtm#display'">
          <xsl:text>xtm:display</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="@xlink:href"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>;&#xA;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentity/xtm:resourceRef">
    <!--** subjectIdentity/resourceRef -> subjectLocator -->
    <xsl:call-template name="indent"/>
    <xsl:text>= </xsl:text>
    <xsl:apply-templates select="@xlink:href"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentity/xtm:topicRef">
    <!--** subjectIdentity/topicRef -> itemIdentity -->
    <xsl:call-template name="indent"/>
    <xsl:text>^</xsl:text>
    <xsl:apply-templates select="@xlink:href"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:subjectIndicatorRef|xtm:topicRef">
    <!--** Translates subjectIndicatorRef into subject identifier and topicRef into item identifier -->
    <xsl:apply-templates select="@xlink:href"/>
  </xsl:template>

  <xsl:template match="xtm:resourceRef[local-name(..) != 'occurrence'][local-name(..) != 'variantName']">
    <!--** Translates resourceRef into subject locator -->
    <xsl:text>= </xsl:text>
    <xsl:apply-templates select="@xlink:href"/>
  </xsl:template>

  <xsl:template match="xtm:occurrence">
    <!--** Matches occurrences. Steps: -->
    <xsl:call-template name="indent"/>
    <!--@ Process the type of the occurrence -->
    <xsl:choose>
      <xsl:when test="count(xtm:instanceOf) = 0">
        <xsl:text>xtm:occurrence: </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="xtm:instanceOf"/>
      </xsl:otherwise>
    </xsl:choose>
    <!--@ Process the value of the occurrence -->
    <xsl:apply-templates select="xtm:resourceRef|xtm:resourceData"/>
    <!--@ Process the scope of the occurrence -->
    <xsl:apply-templates select="xtm:scope"/>
    <!--@ Process the reifier of the occurrence -->
    <xsl:apply-templates select="@id"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:baseName">
    <!--** Matches topic names and occurrences. Steps: -->
    <xsl:call-template name="indent"/>
    <xsl:text>- </xsl:text>
    <!--@ Processes the type of the name. 
          Even if an instanceOf element is disallowed within baseName, some Topic Maps engines use it
    -->
    <xsl:apply-templates select="xtm:instanceOf"/>
    <!--@ Processes the value of the name -->
    <xsl:apply-templates select="xtm:baseNameString"/>
    <!--@ Processes the scope of the name -->
    <xsl:apply-templates select="xtm:scope"/>
    <!--@ Processes the reifier of the name -->
    <xsl:apply-templates select="@id"/>
    <!--@ Processes the variants -->
    <xsl:apply-templates select="xtm:variant"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:association">
    <!--** Matches associations Steps: -->
    <xsl:text>&#xA;</xsl:text>
    <xsl:choose>
      <!--@ If the association has no type, use a default type -->
      <xsl:when test="count(xtm:instanceOf) = 0">
        <xsl:text>xtm:association</xsl:text>
      </xsl:when>
      <!--@ Otherwise use the association's type -->
      <xsl:otherwise>
        <xsl:apply-templates select="xtm:instanceOf"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>(</xsl:text>
    <!--@ Process the association's roles -->
    <xsl:apply-templates select="xtm:member"/>
    <xsl:text>)</xsl:text>
    <!--@ Process the association's scope -->
    <xsl:apply-templates select="xtm:scope"/>
    <!--@ Process the association's reifier -->
    <xsl:apply-templates select="@id"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="xtm:member">
    <!--** Matches association roles. Steps: -->
    <!--@ If the role has multiple players, create a role for each of these players -->
    <xsl:for-each select="xtm:topicRef|xtm:resourceRef|xtm:subjectIndicatorRef">
      <xsl:choose>
        <!--@ If the role has no type, use a default type -->
        <xsl:when test="count(../xtm:roleSpec) = 0">
          <xsl:text>&lt;http://psi.semagia.com/xtm/1.0/role&gt;: </xsl:text>
        </xsl:when>
        <!--@ Use the role's type if specified -->
        <xsl:otherwise>
          <xsl:apply-templates select="../xtm:roleSpec"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
    </xsl:for-each>
    <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="xtm:variant">
    <!--** Matches variants. Steps: -->
    <xsl:text>&#xA;</xsl:text>
    <xsl:call-template name="indent"/>
    <xsl:call-template name="indent"/>
    <xsl:text>(</xsl:text>
    <!--@ Handle the variant's value -->
    <xsl:apply-templates select="xtm:variantName/*"/>
    <!--@ Handle the variant's scope -->
    <xsl:apply-templates select="xtm:parameters"/>
    <!--@ Handle the variant's reifier -->
    <xsl:apply-templates select="@id"/>
    <xsl:text>)</xsl:text>
    <!--@ Process the child variants -->
    <xsl:apply-templates select="xtm:variant"/>
  </xsl:template>

  <xsl:template match="xtm:instanceOf[not(parent::xtm:topic)]|xtm:roleSpec">
    <!--** Converts instanceOf and roleSpec into CTM's type notation -->
    <xsl:apply-templates/>
    <xsl:if test="not(parent::xtm:association)"><xsl:text>: </xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="@xlink:href">
    <!--** Converts xlink:href into <iri>. Steps: -->
    <xsl:choose>
      <!--@ Check if the IRI reference points to a topic by its local identifier and write the local identifier -->
      <xsl:when test="local-name(..) = 'topicRef' and local-name(../..) != 'subjectIdentity' and starts-with(., '#')"><xsl:value-of select="substring-after(., '#')"/></xsl:when>
      <!--@ Write an IRI if the IRI reference does not point to a topic by its local identifier -->
      <xsl:otherwise><xsl:value-of select="concat('&lt;', ., '&gt;')"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--=== Scope handling ===-->

  <xsl:template match="xtm:scope">
    <!--** Converts the scope -->
    <xsl:text> @</xsl:text>
    <xsl:for-each select="*">
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="xtm:parameters">
    <!--** Converts the variant's parameters into scope. If the variant has a variant parent,
           the scope is added to this variant. Steps: 
    -->
    <xsl:text> @</xsl:text>
    <!--@ Process the scope of this variant -->
    <xsl:for-each select="*">
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
    </xsl:for-each>
    <!--@ Process the scope of the ancestor variants (if any) -->
    <xsl:for-each select="../ancestor::xtm:variant/xtm:parameters/*">
      <xsl:text>, </xsl:text>
      <xsl:apply-templates select="."/>
    </xsl:for-each>
  </xsl:template>


  <!--=== Reification handling ===-->

  <xsl:template match="@id">
    <!--** Converts the XTM 1.0 reification mechanism into CTM / TMDM reification -->
    <xsl:if test="key('reifies', concat('#', .))">
      <xsl:variable name="is-topicmap" select="parent::xtm:topicMap"/>
      <!-- Add an additional whitespace if the parent is not a topicMap element .
           Having this additional whitespace char wouldn't be a syntax failure, but it looks better without.
      -->
      <xsl:if test="not($is-topicmap)"><xsl:text> </xsl:text></xsl:if>
      <xsl:value-of select="concat('~ ', key('reifies', concat('#', .))/ancestor::xtm:topic/@id)"/>
      <!-- Add a newline character if the parent is a topicMap element, again for visual reasons -->
      <xsl:if test="$is-topicmap"><xsl:text>&#xA;</xsl:text></xsl:if>
    </xsl:if>
  </xsl:template>

  <!--=== Name, occurrence, and variant value processing ===-->

  <xsl:template match="xtm:baseNameString">
    <!--** Matches the value of topic names -->
    <xsl:apply-templates select="text()"/>
  </xsl:template>

  <xsl:template match="xtm:resourceRef">
    <!--** Matches resourceRef --> 
    <xsl:apply-templates select="@xlink:href"/>
  </xsl:template>

  <xsl:template match="xtm:resourceData">
    <!--** Matches resourceData --> 
    <xsl:choose>
      <xsl:when test="not(text())">""</xsl:when>
      <xsl:otherwise><xsl:apply-templates select="text()"/></xsl:otherwise>
    </xsl:choose>
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
