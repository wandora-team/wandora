<?xml version="1.0" encoding="utf-8"?>
<!--
  ===========================================
  TM/XML 1.0 -> STM 1.0 conversion stylesheet
  ===========================================
  
  This stylesheet translates TM/XML 1.0 into Snello Topic Maps (STM) 1.0.

  Available parameters:
  - indentation: 
    Indicates whether the topic block in the output document should be indented.
    By default, this is set to 4 whitespaces ('    ').

  TM/XML: <http://www.ontopia.net/topicmaps/tmxml.html>
  STM 1.0: <http://www.semagia.com/tr/snello/1.0/>



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
                xmlns:tm="http://psi.ontopia.net/xml/tm-xml/"
                xmlns:iso="http://psi.topicmaps.org/iso13250/model/">

  <xsl:output method="text" media-type="application/x-tm+stm" encoding="utf-8"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="indentation" select="'    '"/>

  <xsl:template match="/*">
    <!--** Walks through the TM/XML source. Steps: -->
    <xsl:text>%encoding "utf-8"&#xA;%stm 1.0&#xA;</xsl:text>
    <xsl:text>&#xA;# This STM 1.0 representation was automatically generated from a TM/XML 1.0 source by&#xA;# http://topic-maps.googlecode.com/&#xA;</xsl:text>
    <!--@ Convert the XML namespaces into STM prefix bindings -->
    <xsl:for-each select="namespace::*[. != 'http://www.w3.org/XML/1998/namespace']">
      <xsl:sort select="local-name()"/>
      <xsl:value-of select="concat('&#xA;%prefix ', local-name(), ' ', '&lt;', ., '&gt;')"/>
      <xsl:if test="position() = last()"><xsl:text>&#xA;</xsl:text></xsl:if>
    </xsl:for-each>
    
    <!--@ Add the topic map's reifier (if any) -->
    <xsl:if test="@reifier">
      <xsl:text>&#xA;~ </xsl:text>
      <xsl:value-of select="@reifier"/>
    </xsl:if>

    <!--@ Serialize the topics -->
    <xsl:for-each select="*">
      <!--@ Decide which identity is used to introduce the topic block -->
      <xsl:variable name="main-identity">
        <xsl:choose>
          <!--@ If the topic has an id attribute, use that value as main identity -->
          <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
          <!--@ If the topic has no id attribute, use the first subject identifier (if any) -->
          <xsl:when test="tm:identifier[1]"><xsl:value-of select="concat('&lt;', tm:identifier[1], '&gt;')"/></xsl:when>
          <!--@ If the topic has neither an id nor subject identifier, use the first subject locator -->
          <xsl:when test="tm:locator[1]"><xsl:value-of select="concat('= &lt;', tm:locator[1], '&gt;')"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="not($main-identity)"><xsl:message terminate="yes">Invalid source: The topic has no identity</xsl:message></xsl:if>
      <xsl:value-of select="concat('&#xA;', $main-identity, '&#xA;')"/>

      <!--@ Process the subject identifiers -->
      <xsl:call-template name="handle-identities">
        <xsl:with-param name="identities" select="tm:identifier"/>
        <xsl:with-param name="main-identity" select="$main-identity"/>
      </xsl:call-template>

      <!--@ Process the subject locators -->
      <xsl:call-template name="handle-identities">
        <xsl:with-param name="identities" select="tm:locator"/>
        <xsl:with-param name="prefix" select="'= '"/>
        <xsl:with-param name="main-identity" select="$main-identity"/>
      </xsl:call-template>

      <!--@ Translate the 'main' topic type into an 'isa' statement unless the main topic type is 'topic' -->
      <xsl:if test="not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' and local-name() = 'topic')">
        <xsl:call-template name="indent"/>
        <xsl:value-of select="concat('isa ', name(), '&#xA;')"/>
      </xsl:if>
      <!--@ Translate all binary type-instance associations which are not 
            reified and which are in the unconstrainted scope into 'isa' statements.
            This expression finds only those associations which use the default prefix 'iso' 
            for 'http://psi.topicmaps.org/iso13250/model/'. All other type-instance relationships
            are serialized as association.
      -->
      <xsl:for-each select="iso:type-instance[@role = 'iso:instance']
                                             [@otherrole = 'iso:type']
                                             [not(@reifier)]
                                             [not(@scope)]">
        <xsl:call-template name="indent"/>
        <xsl:value-of select="concat('isa ', @topicref, '&#xA;')"/>
      </xsl:for-each>

      <!--@ Process the topic names -->
      <xsl:for-each select="*[tm:value]">
        <xsl:call-template name="indent"/>
        <xsl:text>-</xsl:text>
        <!--@ Serialize the name type iff it is not the default name type -->
        <xsl:if test="namespace-uri() != 'http://psi.topicmaps.org/iso13250/model/' and local-name() != 'topic-name'">
          <xsl:value-of select="concat(' ', name())"/>
        </xsl:if>
        <xsl:apply-templates select="@scope"/>
        <xsl:choose>
          <!-- Omit the colon if the name's type is the default type and the name is in the unconstrained scope -->
          <xsl:when test="@scope or namespace-uri() != 'http://psi.topicmaps.org/iso13250/model/' and local-name() != 'topic-name'">
            <xsl:text>: </xsl:text>
          </xsl:when>
          <xsl:otherwise><xsl:text> </xsl:text></xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="tm:value/text()"/>
        <xsl:apply-templates select="@reifier"/>
        <xsl:if test="tm:variant">
          <xsl:text> # Variants are not supported by STM</xsl:text>
        </xsl:if>
        <xsl:text>&#xA;</xsl:text>
      </xsl:for-each>

      <!--@ Process the occurrences -->
      <xsl:for-each select="*[count(tm:value) = 0]
                             [not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' 
                                  and (local-name() = 'identifier' or local-name() = 'locator'))]
                             [not(@role)]">
        <xsl:call-template name="indent"/>
        <xsl:value-of select="name()"/>
        <xsl:apply-templates select="@scope"/>
        <xsl:text>: </xsl:text>
        <xsl:call-template name="handle-literal"/>
        <xsl:apply-templates select="@reifier"/>
        <xsl:text>&#xA;</xsl:text>
      </xsl:for-each>
      
      <!-- End of topic block -->
      <xsl:text>&#xA;</xsl:text>

      <!--** Translate those associations played by the current topic 
             which do not model type-instance relationships which are not reified 
             and are not in the unconstrained scope
      -->
      <xsl:for-each select="*[@role][namespace-uri() != 'http://psi.topicmaps.org/iso13250/model/' 
                                      and local-name() != 'type-instance'
                                      and @role != 'iso:instance'
                                      and @otherrole != 'iso:type'
                                      or not(@otherrole)
                                      or @reifier
                                      or @scope]">
        <xsl:if test="position() = 1">
          <xsl:value-of select="concat('&#xA;# Associations played by ', $main-identity)"/>
        </xsl:if>
        <xsl:value-of select="concat('&#xA;', name())"/>
        <xsl:apply-templates select="@scope"/>
        <xsl:value-of select="concat('(', @role, ': ', $main-identity)"/>
        <xsl:choose>
          <!-- binary association or n-ary association -->
          <xsl:when test="@topicref"> <!-- binary association -->
            <xsl:value-of select="concat(', ', @otherrole, ': ', @topicref)"/>
          </xsl:when>
          <xsl:otherwise> <!-- n-ary association -->
            <xsl:for-each select="*">
              <xsl:text>,&#xA;</xsl:text>
              <xsl:call-template name="indent"/>
              <xsl:value-of select="concat(name(), ': ', @topicref)"/>
            </xsl:for-each> <!-- /roles -->
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>)</xsl:text>
        <xsl:apply-templates select="@reifier"/>
        <xsl:text>&#xA;</xsl:text>
      </xsl:for-each>
    </xsl:for-each> <!-- /topic -->
  </xsl:template>


  <!--=== Scope handling ===-->

  <xsl:template match="@scope">
    <!--** Translates the TM/XML scope notation to STM -->
    <!-- Add a whitespace character iff this is not the scope of an association -->
    <xsl:if test="not(../@role)"><xsl:text> </xsl:text></xsl:if>
    <xsl:value-of select="concat('@', .)"/>
  </xsl:template>

  <!--=== Reifier handling ===-->

  <xsl:template match="@reifier">
    <!--** Translates the reifier to STM -->
    <xsl:value-of select="concat(' ~ ', .)"/>
  </xsl:template>


  <!--=== Text output ===-->

  <xsl:template match="text()">
    <!--** Writes a string literal -->
    <xsl:variable name="triple-quotes" select="contains(., '&quot;')"/>
    <xsl:text>"</xsl:text>
    <xsl:if test="$triple-quotes"><xsl:text>""</xsl:text></xsl:if>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
    <xsl:if test="$triple-quotes"><xsl:text>""</xsl:text></xsl:if>
  </xsl:template>


  <!--=== Named templates ===-->

  <xsl:template name="handle-identities">
    <!--** Handles subject identifiers and subject locators -->
    <xsl:param name="identities"/>
    <xsl:param name="main-identity"/>
    <xsl:param name="prefix" select="''"/>
    <xsl:for-each select="$identities">
      <xsl:choose>
        <!-- Omit the first IRI if it is used as 'main-identity' -->
        <xsl:when test="position() = 1 and (starts-with($main-identity, '&lt;') or starts-with($main-identity, '='))"></xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="indent"/>
          <xsl:call-template name="handle-locator">
            <xsl:with-param name="loc" select="."/>
            <xsl:with-param name="prefix" select="$prefix"/>
          </xsl:call-template>
          <xsl:text>&#xA;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="handle-locator">
    <!--** Serializes an IRI -->
    <xsl:param name="loc" select="."/>
    <xsl:param name="prefix" select="''"/>
    <xsl:value-of select="concat($prefix, '&lt;', $loc, '&gt;')"/>
  </xsl:template>

  <xsl:template name="type">
    <!--** Handles the type of a name / occurrence / role -->
    <xsl:value-of select="concat(name(), ': ')"/>
  </xsl:template>

  <xsl:template name="handle-literal">
    <!--** Writes a STM literal -->
    <xsl:choose>
      <xsl:when test="not(@datatype) or @datatype = 'http://www.w3.org/2001/XMLSchema#string'">
        <xsl:apply-templates select="text()"/>
      </xsl:when>
      <xsl:when test="@datatype = 'http://www.w3.org/2001/XMLSchema#anyURI'">
        <xsl:call-template name="handle-locator"/>
      </xsl:when>
      <xsl:when test="@datatype = 'http://www.w3.org/2001/XMLSchema#dateTime'
                      or @datatype = 'http://www.w3.org/2001/XMLSchema#date'
                      or @datatype = 'http://www.w3.org/2001/XMLSchema#decimal'
                      or @datatype = 'http://www.w3.org/2001/XMLSchema#integer'">
        <xsl:value-of select="."/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="text()"/>
        <xsl:text>^^</xsl:text>
        <xsl:call-template name="handle-locator">
          <xsl:with-param name="loc" select="@datatype"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="indent">
    <!--** Writes an identation string -->
    <xsl:value-of select="$indentation"/>
  </xsl:template>

</xsl:stylesheet>
