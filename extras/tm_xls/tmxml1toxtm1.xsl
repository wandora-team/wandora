<?xml version="1.0" encoding="utf-8"?>
<!--
  ===========================================
  TM/XML 1.0 -> XTM 1.0 conversion stylesheet
  ===========================================
  
  This stylesheet translates TM/XML 1.0 into XTM 1.0.

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

  This is a slightly modified version of the stylesheet found 
  at <http://www.ontopia.net/topicmaps/tmxml.html>

  TM/XML: <http://www.ontopia.net/topicmaps/tmxml.html>
  XTM 1.0: <http://www.topicmaps.org/xtm/1.0/xtm1-20010806.html>

  Changes against the original version:
  - Support for XTM 1.1
  - Added serveral parameters

  Donated to the public domain by Lars Marius Garshol, <larsga@ontopia.net>
  All bugs are introduced by Lars Heuer <heuer[at]semagia.com>
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tm="http://psi.ontopia.net/xml/tm-xml/"
                xmlns:iso="http://psi.topicmaps.org/iso13250/model/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="1.0"
                xmlns:str="http://exslt.org/strings"
                extension-element-prefixes="str"
                xmlns="http://www.topicmaps.org/xtm/1.0/"
                exclude-result-prefixes="tm iso">

  <xsl:output method="xml" media-type="application/x-tm+xtm" encoding="utf-8" standalone="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="xtm_version" select="'1.0'"/>
  <xsl:param name="disallow_name_type" select="true()"/>
  <xsl:param name="omit_version" select="false()"/>

  <!-- used to implement topic -> reified references -->
  <xsl:key name="reifieds" match="*[@reifier]" use="@reifier"/>

  <xsl:template match="/*">
    <!-- Check version for output -->
    <xsl:if test="$xtm_version != '1.0' and $xtm_version != '1.1'">
      <xsl:message terminate="yes">Unsupported XTM version. Expected '1.0' or '1.1', got: <xsl:value-of select="$xtm_version"/></xsl:message>
    </xsl:if>
    <xsl:comment>This XTM <xsl:value-of select="$xtm_version"/> representation was automatically generated from a TM/XML 1.0 source by http://topic-maps.googlecode.com/</xsl:comment>
    <topicMap>
      <xsl:if test="not($omit_version) and $xtm_version = '1.1'">
        <xsl:attribute name="version"><xsl:value-of select="$xtm_version"/></xsl:attribute>
      </xsl:if>
      <xsl:call-template name="reifier"/>

    <!-- TOPIC -->
    <xsl:for-each select="*">
      <topic>
        <!-- ID ATTRIBUTE -->
        <xsl:attribute name="id">
          <xsl:choose>
            <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="generate-id(.)"/></xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>

        <!-- INSTANCEOF -->
        <xsl:call-template name="instance-of"/>

        <!-- SUBJECTIDENTITY -->
        <xsl:if test="tm:identifier or tm:locator or (@id and key('reifieds', @id))">
          <subjectIdentity>
            <xsl:for-each select="tm:identifier">
              <subjectIndicatorRef xlink:href="{.}"/>
            </xsl:for-each>

            <xsl:for-each select="tm:locator">
              <resourceRef xlink:href="{.}"/>
            </xsl:for-each>

            <!-- reification -->
            <xsl:if test="@id and key('reifieds', @id)">
              <subjectIndicatorRef xlink:href="#{generate-id(key('reifieds', @id))}"/>
            </xsl:if>
          </subjectIdentity>
        </xsl:if>

        <!-- BASENAME -->
        <xsl:for-each select="*[tm:value]">
          <baseName>
            <xsl:call-template name="reifier"/>
            <xsl:if test="namespace-uri() != 'http://psi.topicmaps.org/iso13250/model/' and local-name() != 'topic-name'">
              <xsl:choose>
                <xsl:when test="$xtm_version = '1.0' and $disallow_name_type">
                  <xsl:variable name="msg" select="concat('WARN: Skipping the name type declaration: ', name(), ' ')"/>
                  <xsl:comment><xsl:value-of select="$msg"/></xsl:comment>
                  <xsl:message><xsl:value-of select="$msg"/></xsl:message>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="instance-of"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
            <xsl:call-template name="scope"/>
            <baseNameString><xsl:value-of select="tm:value"/></baseNameString>
            <xsl:apply-templates select="tm:variant"/>
          </baseName>
        </xsl:for-each>

        <!-- OCCURRENCE -->
        <xsl:for-each select="*[count(tm:value) = 0]
                               [not(namespace-uri() = 'http://psi.ontopia.net/xml/tm-xml/' 
                                    and (local-name() = 'identifier' or local-name() = 'locator'))]
                               [not(@role)]">
          <occurrence>
            <xsl:call-template name="reifier"/>
            <xsl:call-template name="instance-of"/>
            <xsl:call-template name="scope"/>
            <xsl:call-template name="handle-resource"/>
          </occurrence>
        </xsl:for-each>
      </topic>

      <!-- ASSOCIATION -->
      <xsl:for-each select="*[@role]">
        <association>
          <xsl:call-template name="reifier"/>
          <xsl:call-template name="instance-of"/>

          <!-- the role played by this topic is always there -->
          <member>
            <roleSpec>
              <xsl:call-template name="topicref">
                <xsl:with-param name="ref" select="@role"/>
                <xsl:with-param name="parent" select="."/>
              </xsl:call-template>
            </roleSpec>

            <xsl:choose>
              <xsl:when test="parent::* / tm:identifier">
                <subjectIndicatorRef xlink:href="{parent::* / tm:identifier}"/>
              </xsl:when>
              <xsl:when test="parent::* / tm:locator">
                <resourceRef xlink:href="{parent::* / tm:locator}"/>
              </xsl:when>
              <xsl:otherwise>
                <topicRef xlink:href="#{parent::* / @id}"/>
              </xsl:otherwise>
            </xsl:choose>

          </member>

          <xsl:choose>
            <!-- unaries covered by the above -->

            <!-- binary -->
            <xsl:when test="@topicref">
              <member> <!-- the other role -->
                <roleSpec>
                  <xsl:call-template name="topicref">
                    <xsl:with-param name="ref" select="@otherrole"/>
                    <xsl:with-param name="parent" select="."/>
                  </xsl:call-template>
                </roleSpec>
                <xsl:call-template name="topicref">
                  <xsl:with-param name="ref" select="@topicref"/>
                  <xsl:with-param name="parent" select="."/>
                </xsl:call-template>
              </member>
            </xsl:when>

            <!-- n-ary -->
            <xsl:otherwise>
              <xsl:for-each select="*"> <!-- the other roles -->
                <member>
                  <roleSpec>
                    <xsl:call-template name="ref-to-type-element"/>
                  </roleSpec>
                  <xsl:call-template name="topicref">
                    <xsl:with-param name="ref" select="@topicref"/>
                    <xsl:with-param name="parent" select="."/>
                  </xsl:call-template>
                </member>
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>
        </association>
      </xsl:for-each>
    </xsl:for-each>
    </topicMap>
  </xsl:template>

  <xsl:template match="tm:variant">
    <!--** Converts a variant to XTM -->
    <variant>
      <parameters>
        <xsl:call-template name="scope-contents"/>
      </parameters>
      <variantName>
        <xsl:call-template name="handle-resource"/>
      </variantName>
    </variant>
  </xsl:template>

  <!-- NAMED TEMPLATES -->

  <xsl:template name="instance-of">
    <instanceOf>
      <xsl:call-template name="ref-to-type-element"/>
    </instanceOf>
  </xsl:template>

  <xsl:template name="scope">
    <xsl:if test="@scope">
      <scope>
        <xsl:call-template name="scope-contents"/>
      </scope>
    </xsl:if>
  </xsl:template>  

  <xsl:template name="scope-contents">
    <xsl:variable name="pparent" select="."/> <!-- remember across split -->

    <xsl:for-each select="str:split(@scope)">
      <xsl:call-template name="topicref">
        <xsl:with-param name="parent" select="$pparent"/>
        <xsl:with-param name="ref" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="reifier">
    <xsl:if test="@reifier">
      <xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
    </xsl:if>    
  </xsl:template>

  <xsl:template name="topicref">
    <xsl:param name="parent" select="."/>
    <xsl:param name="ref" select="."/>

    <xsl:choose>
      <xsl:when test="contains($ref, ':')">
        <xsl:variable name="prefix" select="substring-before($ref, ':')"/>
        <xsl:variable name="nsuri" select="string($parent / namespace::*[local-name() = $prefix])"/>

        <subjectIndicatorRef xlink:href="{$nsuri}{substring-after($ref, ':')}"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- is there a default namespace? -->
        <xsl:choose>
          <xsl:when test="$parent / namespace::*[local-name() = '']">
            <xsl:variable name="nsuri" select="string($parent / namespace::*[local-name() = ''])"/>
            <subjectIndicatorRef xlink:href="{$nsuri}{$ref}"/>
          </xsl:when>
          <xsl:otherwise>
            <topicRef xlink:href="#{$ref}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="ref-to-type-element">
    <xsl:choose>
      <xsl:when test="namespace-uri() = ''">
        <topicRef xlink:href="#{local-name()}"/>
      </xsl:when>
      <xsl:otherwise>
        <subjectIndicatorRef xlink:href="{namespace-uri()}{local-name()}"/>
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:template>

  <xsl:template name="handle-resource">
    <xsl:choose>
      <xsl:when test="@datatype = 'http://www.w3.org/2001/XMLSchema#anyURI'">
        <resourceRef xlink:href="{.}"/>
      </xsl:when>
      <xsl:when test="not(@datatype) or @datatype = 'http://www.w3.org/2001/XMLSchema#string'">
        <resourceData><xsl:value-of select="."/></resourceData>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$xtm_version = '1.0'">
            <!-- datatype cannot be preserved in XTM 1.0 -->
            <xsl:variable name="msg" select="concat('WARN: The datatype is not supported by XTM 1.0: ', @datatype, ' ')"/>
            <xsl:comment><xsl:value-of select="$msg"/></xsl:comment>
            <xsl:message><xsl:value-of select="$msg"/></xsl:message>
            <resourceData><xsl:value-of select="."/></resourceData>
          </xsl:when>
          <xsl:otherwise>
            <resourceData datatype="{@datatype}"><xsl:value-of select="."/></resourceData>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:template>
</xsl:stylesheet>
