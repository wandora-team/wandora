<?xml version="1.0" encoding="utf-8"?>
<!--
  =========================================
  TM/XML 1.0 -> XTM 2 conversion stylesheet
  =========================================
  
  This stylesheet translates TM/XML 1.0 into XTM 2.

  Available parameters:
  - xtm_version:
    '2.0' (default) or '2.1'
  
  Note: Due to limitations of XTM 2.0, the translation generates
  additional item identifiers.
  
  Parts of this stylesheet were adapted from the TM/XML -> XTM 1.0
  translation stylesheet which was donated to the public domain
  by Lars Marius Garshol, see <http://www.ontopia.net/topicmaps/tmxml.html>.

  TM/XML: <http://www.ontopia.net/topicmaps/tmxml.html>
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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tm="http://psi.ontopia.net/xml/tm-xml/"
                xmlns:iso="http://psi.topicmaps.org/iso13250/model/"
                xmlns:str="http://exslt.org/strings"
                xmlns="http://www.topicmaps.org/xtm/"
                extension-element-prefixes="str"
                exclude-result-prefixes="tm iso">

  <xsl:output method="xml" media-type="application/x-tm+xtm" encoding="utf-8" standalone="yes" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="xtm_version" select="'2.0'"/>

  <xsl:template match="/*">
    <!--** Walks through the TM/XML source. Steps: -->
    <xsl:if test="$xtm_version != '2.0' and $xtm_version != '2.1'">
      <xsl:message terminate="yes">Unsupported XTM version. Expected '2.0' or '2.1'</xsl:message>
    </xsl:if>
    <xsl:comment>This XTM <xsl:value-of select="$xtm_version"/> representation was automatically generated from a TM/XML 1.0 source by http://topic-maps.googlecode.com/</xsl:comment>
    <topicMap version="{$xtm_version}">
    <!--@ Add the topic map's reifier (if any) -->
    <xsl:apply-templates select="@reifier"/>

    <!--@ Serialize the topics -->
    <xsl:for-each select="*">
      <topic>
        <!-- Add an id attribute iff the source topic has an id or if we are in the XTM 2.0 mode -->
        <xsl:if test="@id or $xtm_version = '2.0'">
          <xsl:attribute name="id">
            <xsl:choose>
              <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="generate-id(.)"/></xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:if>
        <!--@ Process the subject identifiers / subject locators -->
        <xsl:apply-templates select="tm:identifier|tm:locator"/>
  
        <!--@ Translate the 'main' topic type into an instanceOf element unless the main topic type is 'topic' -->
        <xsl:if test="not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' and local-name() = 'topic')">
          <instanceOf>
            <xsl:call-template name="ref-to-type-element"/>
          </instanceOf>
        </xsl:if>
  
        <!--@ Process the topic names -->
        <xsl:for-each select="*[tm:value]">
          <name>
            <xsl:apply-templates select="@reifier"/>
            <!--@ Serialize the name type iff it is not the default name type -->
            <xsl:if test="namespace-uri() != 'http://psi.topicmaps.org/iso13250/model/' and local-name() != 'topic-name'">
              <xsl:call-template name="type"/>
            </xsl:if>
            <xsl:apply-templates select="@scope"/>
            <value><xsl:value-of select="tm:value"/></value>
            <!--@ Process the variants -->
            <xsl:apply-templates select="tm:variant"/>
          </name>
        </xsl:for-each>
  
        <!--@ Process the occurrences -->
        <xsl:for-each select="*[count(tm:value) = 0]
                               [not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' 
                                    and (local-name() = 'identifier' or local-name() = 'locator'))]
                               [not(@role)]">
          <occurrence>
            <xsl:apply-templates select="@reifier"/>
            <xsl:call-template name="type"/>
            <xsl:apply-templates select="@scope"/>
            <xsl:call-template name="handle-literal"/>
          </occurrence>
        </xsl:for-each>
      </topic>

      <!--** Translate associations played by the current topic -->
      <xsl:for-each select="*[@role]">
        <association>
          <xsl:apply-templates select="@reifier"/>
          <xsl:call-template name="type"/>
          <xsl:apply-templates select="@scope"/>
          <role>
            <type>
              <xsl:apply-templates select="@role"/>
            </type>
            <xsl:choose>
              <!-- If the topic has an id, use that id regardless of the XTM mode -->
              <xsl:when test="../@id">
                <topicRef href="#{../@id}"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <!-- XTM 2.1 mode? -->
                  <xsl:when test="$xtm_version = '2.1'">
                    <xsl:choose>
                      <xsl:when test="../tm:identifier">
                        <subjectIdentifierRef href="{../tm:identifier[1]}"/>
                      </xsl:when>
                      <xsl:when test="../tm:locator">
                        <subjectLocatorRef href="{../tm:identifier[1]}"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:message terminate="yes">Invalid TM/XML source: The topic has no identity</xsl:message>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <!-- XTM 2.0 mode -->
                  <xsl:otherwise>
                    <topicRef href="#{generate-id(..)}"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </role>
          <xsl:choose>
            <!-- binary association or n-ary association -->
            <xsl:when test="@topicref"> <!-- binary association -->
              <role>
                <type>
                  <xsl:apply-templates select="@otherrole"/>
                </type>
                <xsl:apply-templates select="@topicref"/>
              </role>
            </xsl:when>
            <xsl:otherwise> <!-- n-ary association, where n > 2-->
              <xsl:for-each select="*">
                <role>
                  <xsl:call-template name="type"/>
                  <xsl:apply-templates select="@topicref"/>
                </role>
              </xsl:for-each> <!-- /roles -->
            </xsl:otherwise>
          </xsl:choose>
        </association>
      </xsl:for-each> <!-- / assocs -->
      <!-- Post process this topic to gather the subject identifiers iff output is XTM 2.0 -->
      <xsl:if test="$xtm_version = '2.0'">
        <xsl:call-template name="postprocess-topic"/>
      </xsl:if>
    </xsl:for-each> <!-- /topic -->
    
    </topicMap>
  </xsl:template>


  <!--=== Topic identities ===-->

  <xsl:template match="tm:identifier">
    <!--** Matches subject identifiers -->
    <subjectIdentifier href="{.}"/>
  </xsl:template>

  <xsl:template match="tm:locator">
    <!--** Matches subject locators -->
    <subjectLocator href="{.}"/>
  </xsl:template>

  <!--=== Variant output ===-->

  <xsl:template match="tm:variant">
    <!--** Converts a variant to XTM -->
    <variant>
      <xsl:apply-templates select="@reifier"/>
      <xsl:apply-templates select="@scope"/>
      <xsl:call-template name="handle-literal"/>
    </variant>
  </xsl:template>


  <!--=== Scope handling ===-->

  <xsl:template match="@scope">
    <!--** Translates the TM/XML scope notation to XTM -->
    <scope>
      <xsl:variable name="pparent" select=".."/> <!-- remember across split -->
      <xsl:for-each select="str:split(.)">
        <xsl:call-template name="topic-ref">
          <xsl:with-param name="parent" select="$pparent"/>
          <xsl:with-param name="ref" select="."/>
          <xsl:with-param name="pos" select="concat('-theme-', position())"/>
        </xsl:call-template>
      </xsl:for-each>
    </scope>
  </xsl:template>


  <!--=== Reifier handling ===-->

  <xsl:template match="@reifier">
    <!--** Translates the reifier to XTM -->
    <xsl:attribute name="reifier">
      <xsl:value-of select="concat('#', .)"/>
    </xsl:attribute>
  </xsl:template>


  <xsl:template match="@role|@otherrole|@topicref">
    <xsl:call-template name="topic-ref">
      <xsl:with-param name="parent" select=".."/>
      <xsl:with-param name="ref" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!--=== Named templates ===-->

  <xsl:template name="type">
    <!--** Creates a <type/> element -->
    <type>
      <xsl:call-template name="ref-to-type-element"/>
    </type>
  </xsl:template>

  <xsl:template name="topic-ref">
    <xsl:param name="parent" select="."/>
    <xsl:param name="ref" select="."/>
    <xsl:param name="pos" select="''"/>
    <xsl:choose>
      <xsl:when test="contains($ref, ':')">
        <xsl:variable name="prefix" select="substring-before($ref, ':')"/>
        <xsl:variable name="nsuri" select="string($parent / namespace::*[local-name() = $prefix])"/>
        <xsl:choose>
          <xsl:when test="$xtm_version = '2.1'">
            <subjectIdentifierRef href="{concat($nsuri, substring-after($ref, ':'))}"/>
          </xsl:when>
          <xsl:otherwise>
            <topicRef href="#{generate-id($parent)}{$pos}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- is there a default namespace? -->
        <xsl:choose>
          <xsl:when test="$parent / namespace::*[local-name() = '']">
            <xsl:variable name="nsuri" select="string($parent / namespace::*[local-name() = ''])"/>
            <xsl:choose>
              <xsl:when test="$xtm_version = '2.1'">
                <subjectIdentifierRef href="{$nsuri}{$ref}"/>
              </xsl:when>
              <xsl:otherwise>
                <topicRef href="#{generate-id($parent)}{$pos}"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <topicRef href="#{$ref}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="ref-to-type-element">
    <xsl:choose>
      <xsl:when test="namespace-uri() = ''">
        <topicRef href="#{local-name()}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$xtm_version = '2.1'">
            <subjectIdentifierRef href="{namespace-uri()}{local-name()}"/>
          </xsl:when>
          <xsl:otherwise>
            <topicRef href="#{generate-id(.)}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="handle-literal">
    <!--** Serializes resourceData / resourceRef -->
    <xsl:choose>
      <xsl:when test="@datatype = 'http://www.w3.org/2001/XMLSchema#anyURI'">
        <resourceRef href="{.}"/>
      </xsl:when>
      <xsl:when test="not(@datatype) or @datatype = 'http://www.w3.org/2001/XMLSchema#string'">
        <resourceData><xsl:value-of select="."/></resourceData>
      </xsl:when>
      <xsl:otherwise>>
        <resourceData datatype="{@datatype}"><xsl:value-of select="."/></resourceData>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--=== XTM 2.0 post process ===-->

  <xsl:template name="postprocess-topic">
    <!--** Postprocesses the current topic: Previously this stylesheet has created additonal 
           topic identifiers; now these topic identifiers with the corresponding subject identifier
           are written to the output.
           Note: This is only necessary for XTM 2.0 -->
    <xsl:if test="not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' and local-name() = 'topic')">
      <topic id="{generate-id(.)}">
        <subjectIdentifier href="{namespace-uri()}{local-name()}"/>
      </topic>
    </xsl:if>
    <xsl:apply-templates select="*[not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' 
                                    and (local-name() = 'identifier' or local-name() = 'locator'))]" 
                         mode="postprocess"/>
  </xsl:template>

  <xsl:template match="*[tm:value]" mode="postprocess">
    <xsl:if test="namespace-uri() != 'http://psi.topicmaps.org/iso13250/model/' and local-name() != 'topic-name'">
      <xsl:call-template name="pp-ref-to-type-element"/>
    </xsl:if>
    <xsl:apply-templates select="@*" mode="postprocess"/>
    <xsl:for-each select="tm:variant">
      <xsl:apply-templates select="@scope" mode="postprocess"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*" mode="postprocess">
    <xsl:call-template name="pp-ref-to-type-element"/>
    <xsl:apply-templates select="@*[local-name() != 'datatype']" mode="postprocess"/>
    <xsl:if test="@role">
      <xsl:apply-templates select="*" mode="postprocess"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@role|@otherrole|@topicref" mode="postprocess">
    <xsl:call-template name="pp-topic-ref">
        <xsl:with-param name="parent" select=".."/>
        <xsl:with-param name="ref" select="."/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="@scope" mode="postprocess">
    <xsl:variable name="pparent" select=".."/> <!-- remember across split -->
    <xsl:for-each select="str:split(.)">
      <xsl:call-template name="pp-topic-ref">
        <xsl:with-param name="parent" select="$pparent"/>
        <xsl:with-param name="ref" select="."/>
        <xsl:with-param name="pos" select="concat('-theme-', position())"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="pp-topic-ref">
    <xsl:param name="parent" select="."/>
    <xsl:param name="ref" select="."/>
    <xsl:param name="pos" select="''"/>
    <xsl:choose>
      <xsl:when test="contains($ref, ':')">
        <xsl:variable name="prefix" select="substring-before($ref, ':')"/>
        <xsl:variable name="nsuri" select="string($parent / namespace::*[local-name() = $prefix])"/>
        <topic id="{generate-id($parent)}{$pos}">
          <subjectIdentifier href="{concat($nsuri, substring-after($ref, ':'))}"/>
        </topic>
      </xsl:when>
      <xsl:otherwise>
        <!-- is there a default namespace? -->
        <xsl:if test="$parent / namespace::*[local-name() = '']">
          <xsl:variable name="nsuri" select="string($parent / namespace::*[local-name() = ''])"/>
          <topic id="{generate-id($parent)}{$pos}">
            <subjectIdentifier href="{$nsuri}{$ref}"/>
          </topic>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="pp-ref-to-type-element">
    <xsl:if test="namespace-uri() != ''">
      <topic id="{generate-id(.)}">
        <subjectIdentifier href="{namespace-uri()}{local-name()}"/>
      </topic>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
