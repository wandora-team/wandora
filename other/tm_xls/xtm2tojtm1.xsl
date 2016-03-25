<!--
  ======================================
  XTM 2 -> JTM 1.0 conversion stylesheet
  ======================================
  
  This stylesheet translates XTM 2 into JSON Topic Maps (JTM) 1.0.

  XTM 2.0: <http://www.isotopicmaps.org/sam/sam-xtm/2006-06-19/>
  JTM 1.0: <http://www.cerny-online.com/jtm/>



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

  <xsl:output method="text" media-type="application/x-tm+jtm" encoding="utf-8"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="xtm:topicMap">
    <xsl:text>{"version":"1.0","item_type":"topicmap",</xsl:text>
    <xsl:call-template name="reifier"/>
    <xsl:apply-templates select="xtm:itemIdentity"/>
    <xsl:apply-templates select="xtm:topic"/>
    <xsl:apply-templates select="xtm:association"/>
    <xsl:if test="count(xtm:topic/xtm:instanceOf) != 0">
      <xsl:choose>
        <xsl:when test="count(xtm:association) = 0">,"associations":[</xsl:when>
        <xsl:otherwise>,</xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="xtm:topic/xtm:instanceOf/xtm:topicRef"/>
    </xsl:if>
    <xsl:if test="count(xtm:topic/xtm:instanceOf) != 0 or count(xtm:association) != 0">]</xsl:if>
    <xsl:text>}&#xA;</xsl:text>
  </xsl:template>

  <!-- topics -->
  <xsl:template match="xtm:topic">
    <xsl:choose>
      <xsl:when test="position() = 1">,"topics":[</xsl:when>
      <xsl:otherwise>,</xsl:otherwise>
    </xsl:choose>
    <xsl:text>{"item_identifiers":["</xsl:text>
    <xsl:value-of select="concat('#', @id)"/>
    <xsl:text>"</xsl:text>
    <xsl:for-each select="xtm:itemIdentity">
      <xsl:text>,</xsl:text>
      <xsl:apply-templates select="@href" mode="iri"/>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
    <xsl:apply-templates select="xtm:subjectIdentifier"/>
    <xsl:apply-templates select="xtm:subjectLocator"/>
    <xsl:apply-templates select="xtm:name"/>
    <xsl:apply-templates select="xtm:occurrence"/>
    <xsl:text>}</xsl:text>
    <xsl:if test="position() = last()">]</xsl:if>
  </xsl:template>
  
  <!-- topic types -->
  <xsl:template match="xtm:topic/xtm:instanceOf/xtm:topicRef">
    <xsl:text>{"type":"si:http://psi.topicmaps.org/iso13250/model/type-instance","roles":[{"type": "si:http://psi.topicmaps.org/iso13250/model/type","player":</xsl:text>
    <!-- type player -->
    <xsl:apply-templates select="@href" mode="topic-ref"/>
    <xsl:text>}, {"type":"si:http://psi.topicmaps.org/iso13250/model/instance","player":</xsl:text>
    <!-- instance player -->
    <xsl:value-of select="concat('&quot;ii:#', ../../@id, '&quot;')"/>
    <xsl:text>}]}</xsl:text>
    <xsl:if test="position() != last()">,</xsl:if>
  </xsl:template>

  <!-- iid != topic iids -->
  <xsl:template match="xtm:itemIdentity">
    <xsl:if test="position() = 1">,"item_identifiers":</xsl:if>
    <xsl:text>[</xsl:text>
    <xsl:apply-templates select="@href" mode="iri"/>
    <xsl:choose>
      <xsl:when test="position() = last()">]</xsl:when>
      <xsl:otherwise>,</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- sids -->
  <xsl:template match="xtm:subjectIdentifier">
    <xsl:if test="position() = 1">,"subject_identifiers":[</xsl:if>
    <xsl:apply-templates select="@href" mode="iri"/>
    <xsl:choose>
      <xsl:when test="position() = last()">]</xsl:when>
      <xsl:otherwise>,</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- slos -->
  <xsl:template match="xtm:subjectLocator">
    <xsl:if test="position() = 1">,"subject_locators":[</xsl:if>
    <xsl:apply-templates select="@href" mode="iri"/>
    <xsl:choose>
      <xsl:when test="position() = last()">]</xsl:when>
      <xsl:otherwise>,</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@href" mode="iri">
    <xsl:call-template name="string">
      <xsl:with-param name="s" select="."/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="@href|@reifier" mode="topic-ref">
    <xsl:call-template name="string">
      <xsl:with-param name="s" select="concat('ii:', .)"/>
    </xsl:call-template>
  </xsl:template>

  <!-- catch all for constructs != topic and topicMap -->
  <xsl:template match="xtm:association|xtm:occurrence|xtm:name|xtm:variant|xtm:role">
    <xsl:if test="position() = 1">
      <xsl:value-of select="concat(',&quot;', local-name(.), 's', '&quot;:[')"/>
    </xsl:if>
    <xsl:text>{</xsl:text>
    <xsl:call-template name="reifier"/>
    <xsl:apply-templates select="xtm:itemIdentity"/> 
    <xsl:apply-templates select="xtm:type"/>
    <xsl:apply-templates select="xtm:scope"/>
    <xsl:apply-templates select="xtm:value|xtm:resourceRef|xtm:resourceData|xtm:topicRef"/>
    <xsl:apply-templates select="xtm:role|xtm:variant"/>
    <xsl:text>}</xsl:text>
    <xsl:if test="position() != last()">,</xsl:if>
    <xsl:if test="position() = last() and local-name(.) != 'association'">]</xsl:if>
  </xsl:template>

  <xsl:template match="xtm:topicRef">
    <xsl:if test="parent::xtm:role">
      <xsl:text>,"player":</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="@href" mode="topic-ref"/>
    <xsl:if test="position() != last()"><xsl:text>,</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="xtm:subjectIdentifierRef">
    <xsl:if test="parent::xtm:role">
      <xsl:text>,"player":</xsl:text>
    </xsl:if>
    <xsl:call-template name="string">
      <xsl:with-param name="s" select="concat('si:', @href)"/>
    </xsl:call-template>
    <xsl:if test="position() != last()"><xsl:text>,</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="xtm:subjectLocatorRef">
    <xsl:if test="parent::xtm:role">
      <xsl:text>,"player":</xsl:text>
    </xsl:if>
    <xsl:call-template name="string">
      <xsl:with-param name="s" select="concat('sl:', @href)"/>
    </xsl:call-template>
    <xsl:if test="position() != last()"><xsl:text>,</xsl:text></xsl:if>
  </xsl:template>

  <xsl:template match="xtm:value|xtm:type|xtm:scope">
    <xsl:value-of select="concat(',&quot;', local-name(.), '&quot;:')"/>
    <xsl:if test="local-name(.) = 'scope'">[</xsl:if>
    <xsl:apply-templates select="xtm:topicRef|xtm:subjectIdentifierRef|xtm:subjectLocatorRef|text()"/>
    <xsl:if test="local-name(.) = 'scope'">]</xsl:if>
  </xsl:template>

  <!-- xsd:anyURI -->
  <xsl:template match="xtm:resourceRef|xtm:resourceData[@datatype = 'http://www.w3.org/2001/XMLSchema#anyURI']">
    <xsl:text>,"datatype": "http://www.w3.org/2001/XMLSchema#anyURI","value":</xsl:text>
    <xsl:call-template name="string">
      <xsl:with-param name="s" select="@href|."/>
    </xsl:call-template>
  </xsl:template>

  <!-- xsd:string -->
  <xsl:template match="xtm:resourceData[not(@datatype) or @datatype = 'http://www.w3.org/2001/XMLSchema#string']">
    <xsl:text>,"value":</xsl:text>
    <xsl:choose>
      <xsl:when test="not(text())">""</xsl:when>
      <xsl:otherwise><xsl:apply-templates select="text()"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- everything != xsd:string / xsd:anyURI-->
  <xsl:template match="xtm:resourceData[@datatype]">
    <xsl:text>,"datatype":</xsl:text>
    <xsl:call-template name="string">
      <xsl:with-param name="s" select="@datatype"/>
    </xsl:call-template>
    <xsl:text>,"value":</xsl:text>
    <xsl:apply-templates select="text()"/>
  </xsl:template>
  
  <xsl:template name="reifier">
    <!--** Writes reifier reference (maybe 'null') to the output.
           All constructs in the output start with the reifier property to have common structure
           to write further properties of the constructs. -->
    <xsl:text>"reifier":</xsl:text>
    <xsl:choose>
      <xsl:when test="@reifier">
        <xsl:apply-templates select="@reifier" mode="topic-ref"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>null</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="string" match="text()">
    <xsl:param name="s" select="."/>
    <xsl:text>"</xsl:text>
    <xsl:call-template name="escape-bs-string">
      <xsl:with-param name="s" select="$s"/>
    </xsl:call-template>
    <xsl:text>"</xsl:text>
  </xsl:template>
  

<!-- 
  The following code was taken from 
  <http://code.google.com/p/xml2json-xslt/source/browse/trunk/xml2json.xslt>

  Copyright (c) 2006,2008 Doeke Zanstra
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:

  Redistributions of source code must retain the above copyright notice, this 
  list of conditions and the following disclaimer. Redistributions in binary 
  form must reproduce the above copyright notice, this list of conditions and the 
  following disclaimer in the documentation and/or other materials provided with 
  the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
  THE POSSIBILITY OF SUCH DAMAGE.
-->

  <!-- Escape the backslash (\) before everything else. -->
  <xsl:template name="escape-bs-string">
    <xsl:param name="s"/>
    <xsl:choose>
      <xsl:when test="contains($s,'\')">
        <xsl:call-template name="escape-quot-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'\'),'\\')"/>
        </xsl:call-template>
        <xsl:call-template name="escape-bs-string">
          <xsl:with-param name="s" select="substring-after($s,'\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="escape-quot-string">
          <xsl:with-param name="s" select="$s"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Escape the double quote ("). -->
  <xsl:template name="escape-quot-string">
    <xsl:param name="s"/>
    <xsl:choose>
      <xsl:when test="contains($s,'&quot;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&quot;'),'\&quot;')"/>
        </xsl:call-template>
        <xsl:call-template name="escape-quot-string">
          <xsl:with-param name="s" select="substring-after($s,'&quot;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="$s"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Replace tab, line feed and/or carriage return by its matching escape code. Can't escape backslash
       or double quote here, because they don't replace characters (&#x0; becomes \t), but they prefix 
       characters (\ becomes \\). Besides, backslash should be seperate anyway, because it should be 
       processed first. This function can't do that. -->
  <xsl:template name="encode-string">
    <xsl:param name="s"/>
    <xsl:choose>
      <!-- tab -->
      <xsl:when test="contains($s,'&#x9;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&#x9;'),'\t',substring-after($s,'&#x9;'))"/>
        </xsl:call-template>
      </xsl:when>
      <!-- line feed -->
      <xsl:when test="contains($s,'&#xA;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&#xA;'),'\n',substring-after($s,'&#xA;'))"/>
        </xsl:call-template>
      </xsl:when>
      <!-- carriage return -->
      <xsl:when test="contains($s,'&#xD;')">
        <xsl:call-template name="encode-string">
          <xsl:with-param name="s" select="concat(substring-before($s,'&#xD;'),'\r',substring-after($s,'&#xD;'))"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$s"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
