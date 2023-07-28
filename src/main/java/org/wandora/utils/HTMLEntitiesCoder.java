/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * HTMLEntitiesCoder.java
 *
 * Created on 3. joulukuuta 2004, 10:36
 */

package org.wandora.utils;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;



/**
 * This class has mappings from html entities to characters and vica versa
 * as specified in http://www.w3.org/TR/REC-html40/sgml/entities.html . There
 * are also methods to encode and decode html entities in a text.
 * @author  olli
 */
public class HTMLEntitiesCoder {
    
    
    /**
     * Maps html entities to characters. The keyes are html entities
     * without the ampersand and semicolon. Values are Character objects.
     * For example there is a mapping from the String "nbsp" to Character with
     * char code 160.
     */
    public static final Map entitiesTable = new EasyHash(new Object[]{
        "nbsp",Character.valueOf((char)160), /* no-break space = non-breaking space,
                                                  U+00A0 ISOnum */
        "iexcl",Character.valueOf((char)161), /* inverted exclamation mark, U+00A1 ISOnum */
        "cent",Character.valueOf((char)162), /* cent sign, U+00A2 ISOnum */
        "pound",Character.valueOf((char)163), /* pound sign, U+00A3 ISOnum */
        "curren",Character.valueOf((char)164), /* currency sign, U+00A4 ISOnum */
        "yen",Character.valueOf((char)165), /* yen sign = yuan sign, U+00A5 ISOnum */
        "brvbar",Character.valueOf((char)166), /* broken bar = broken vertical bar,
                                                  U+00A6 ISOnum */
        "sect",Character.valueOf((char)167), /* section sign, U+00A7 ISOnum */
        "uml",Character.valueOf((char)168), /* diaeresis = spacing diaeresis,
                                                  U+00A8 ISOdia */
        "copy",Character.valueOf((char)169), /* copyright sign, U+00A9 ISOnum */
        "ordf",Character.valueOf((char)170), /* feminine ordinal indicator, U+00AA ISOnum */
        "laquo",Character.valueOf((char)171), /* left-pointing double angle quotation mark
                                                  = left pointing guillemet, U+00AB ISOnum */
        "not",Character.valueOf((char)172), /* not sign, U+00AC ISOnum */
        "shy",Character.valueOf((char)173), /* soft hyphen = discretionary hyphen,
                                                  U+00AD ISOnum */
        "reg",Character.valueOf((char)174), /* registered sign = registered trade mark sign,
                                                  U+00AE ISOnum */
        "macr",Character.valueOf((char)175), /* macron = spacing macron = overline
                                                  = APL overbar, U+00AF ISOdia */
        "deg",Character.valueOf((char)176), /* degree sign, U+00B0 ISOnum */
        "plusmn",Character.valueOf((char)177), /* plus-minus sign = plus-or-minus sign,
                                                  U+00B1 ISOnum */
        "sup2",Character.valueOf((char)178), /* superscript two = superscript digit two
                                                  = squared, U+00B2 ISOnum */
        "sup3",Character.valueOf((char)179), /* superscript three = superscript digit three
                                                  = cubed, U+00B3 ISOnum */
        "acute",Character.valueOf((char)180), /* acute accent = spacing acute,
                                                  U+00B4 ISOdia */
        "micro",Character.valueOf((char)181), /* micro sign, U+00B5 ISOnum */
        "para",Character.valueOf((char)182), /* pilcrow sign = paragraph sign,
                                                  U+00B6 ISOnum */
        "middot",Character.valueOf((char)183), /* middle dot = Georgian comma
                                                  = Greek middle dot, U+00B7 ISOnum */
        "cedil",Character.valueOf((char)184), /* cedilla = spacing cedilla, U+00B8 ISOdia */
        "sup1",Character.valueOf((char)185), /* superscript one = superscript digit one,
                                                  U+00B9 ISOnum */
        "ordm",Character.valueOf((char)186), /* masculine ordinal indicator,
                                                  U+00BA ISOnum */
        "raquo",Character.valueOf((char)187), /* right-pointing double angle quotation mark
                                                  = right pointing guillemet, U+00BB ISOnum */
        "frac14",Character.valueOf((char)188), /* vulgar fraction one quarter
                                                  = fraction one quarter, U+00BC ISOnum */
        "frac12",Character.valueOf((char)189), /* vulgar fraction one half
                                                  = fraction one half, U+00BD ISOnum */
        "frac34",Character.valueOf((char)190), /* vulgar fraction three quarters
                                                  = fraction three quarters, U+00BE ISOnum */
        "iquest",Character.valueOf((char)191), /* inverted question mark
                                                  = turned question mark, U+00BF ISOnum */
        "Agrave",Character.valueOf((char)192), /* latin capital letter A with grave
                                                  = latin capital letter A grave,
                                                  U+00C0 ISOlat1 */
        "Aacute",Character.valueOf((char)193), /* latin capital letter A with acute,
                                                  U+00C1 ISOlat1 */
        "Acirc",Character.valueOf((char)194), /* latin capital letter A with circumflex,
                                                  U+00C2 ISOlat1 */
        "Atilde",Character.valueOf((char)195), /* latin capital letter A with tilde,
                                                  U+00C3 ISOlat1 */
        "Auml",Character.valueOf((char)196), /* latin capital letter A with diaeresis,
                                                  U+00C4 ISOlat1 */
        "Aring",Character.valueOf((char)197), /* latin capital letter A with ring above
                                                  = latin capital letter A ring,
                                                  U+00C5 ISOlat1 */
        "AElig",Character.valueOf((char)198), /* latin capital letter AE
                                                  = latin capital ligature AE,
                                                  U+00C6 ISOlat1 */
        "Ccedil",Character.valueOf((char)199), /* latin capital letter C with cedilla,
                                                  U+00C7 ISOlat1 */
        "Egrave",Character.valueOf((char)200), /* latin capital letter E with grave,
                                                  U+00C8 ISOlat1 */
        "Eacute",Character.valueOf((char)201), /* latin capital letter E with acute,
                                                  U+00C9 ISOlat1 */
        "Ecirc",Character.valueOf((char)202), /* latin capital letter E with circumflex,
                                                  U+00CA ISOlat1 */
        "Euml",Character.valueOf((char)203), /* latin capital letter E with diaeresis,
                                                  U+00CB ISOlat1 */
        "Igrave",Character.valueOf((char)204), /* latin capital letter I with grave,
                                                  U+00CC ISOlat1 */
        "Iacute",Character.valueOf((char)205), /* latin capital letter I with acute,
                                                  U+00CD ISOlat1 */
        "Icirc",Character.valueOf((char)206), /* latin capital letter I with circumflex,
                                                  U+00CE ISOlat1 */
        "Iuml",Character.valueOf((char)207), /* latin capital letter I with diaeresis,
                                                  U+00CF ISOlat1 */
        "ETH",Character.valueOf((char)208), /* latin capital letter ETH, U+00D0 ISOlat1 */
        "Ntilde",Character.valueOf((char)209), /* latin capital letter N with tilde,
                                                  U+00D1 ISOlat1 */
        "Ograve",Character.valueOf((char)210), /* latin capital letter O with grave,
                                                  U+00D2 ISOlat1 */
        "Oacute",Character.valueOf((char)211), /* latin capital letter O with acute,
                                                  U+00D3 ISOlat1 */
        "Ocirc",Character.valueOf((char)212), /* latin capital letter O with circumflex,
                                                  U+00D4 ISOlat1 */
        "Otilde",Character.valueOf((char)213), /* latin capital letter O with tilde,
                                                  U+00D5 ISOlat1 */
        "Ouml",Character.valueOf((char)214), /* latin capital letter O with diaeresis,
                                                  U+00D6 ISOlat1 */
        "times",Character.valueOf((char)215), /* multiplication sign, U+00D7 ISOnum */
        "Oslash",Character.valueOf((char)216), /* latin capital letter O with stroke
                                                  = latin capital letter O slash,
                                                  U+00D8 ISOlat1 */
        "Ugrave",Character.valueOf((char)217), /* latin capital letter U with grave,
                                                  U+00D9 ISOlat1 */
        "Uacute",Character.valueOf((char)218), /* latin capital letter U with acute,
                                                  U+00DA ISOlat1 */
        "Ucirc",Character.valueOf((char)219), /* latin capital letter U with circumflex,
                                                  U+00DB ISOlat1 */
        "Uuml",Character.valueOf((char)220), /* latin capital letter U with diaeresis,
                                                  U+00DC ISOlat1 */
        "Yacute",Character.valueOf((char)221), /* latin capital letter Y with acute,
                                                  U+00DD ISOlat1 */
        "THORN",Character.valueOf((char)222), /* latin capital letter THORN,
                                                  U+00DE ISOlat1 */
        "szlig",Character.valueOf((char)223), /* latin small letter sharp s = ess-zed,
                                                  U+00DF ISOlat1 */
        "agrave",Character.valueOf((char)224), /* latin small letter a with grave
                                                  = latin small letter a grave,
                                                  U+00E0 ISOlat1 */
        "aacute",Character.valueOf((char)225), /* latin small letter a with acute,
                                                  U+00E1 ISOlat1 */
        "acirc",Character.valueOf((char)226), /* latin small letter a with circumflex,
                                                  U+00E2 ISOlat1 */
        "atilde",Character.valueOf((char)227), /* latin small letter a with tilde,
                                                  U+00E3 ISOlat1 */
        "auml",Character.valueOf((char)228), /* latin small letter a with diaeresis,
                                                  U+00E4 ISOlat1 */
        "aring",Character.valueOf((char)229), /* latin small letter a with ring above
                                                  = latin small letter a ring,
                                                  U+00E5 ISOlat1 */
        "aelig",Character.valueOf((char)230), /* latin small letter ae
                                                  = latin small ligature ae, U+00E6 ISOlat1 */
        "ccedil",Character.valueOf((char)231), /* latin small letter c with cedilla,
                                                  U+00E7 ISOlat1 */
        "egrave",Character.valueOf((char)232), /* latin small letter e with grave,
                                                  U+00E8 ISOlat1 */
        "eacute",Character.valueOf((char)233), /* latin small letter e with acute,
                                                  U+00E9 ISOlat1 */
        "ecirc",Character.valueOf((char)234), /* latin small letter e with circumflex,
                                                  U+00EA ISOlat1 */
        "euml",Character.valueOf((char)235), /* latin small letter e with diaeresis,
                                                  U+00EB ISOlat1 */
        "igrave",Character.valueOf((char)236), /* latin small letter i with grave,
                                                  U+00EC ISOlat1 */
        "iacute",Character.valueOf((char)237), /* latin small letter i with acute,
                                                  U+00ED ISOlat1 */
        "icirc",Character.valueOf((char)238), /* latin small letter i with circumflex,
                                                  U+00EE ISOlat1 */
        "iuml",Character.valueOf((char)239), /* latin small letter i with diaeresis,
                                                  U+00EF ISOlat1 */
        "eth",Character.valueOf((char)240), /* latin small letter eth, U+00F0 ISOlat1 */
        "ntilde",Character.valueOf((char)241), /* latin small letter n with tilde,
                                                  U+00F1 ISOlat1 */
        "ograve",Character.valueOf((char)242), /* latin small letter o with grave,
                                                  U+00F2 ISOlat1 */
        "oacute",Character.valueOf((char)243), /* latin small letter o with acute,
                                                  U+00F3 ISOlat1 */
        "ocirc",Character.valueOf((char)244), /* latin small letter o with circumflex,
                                                  U+00F4 ISOlat1 */
        "otilde",Character.valueOf((char)245), /* latin small letter o with tilde,
                                                  U+00F5 ISOlat1 */
        "ouml",Character.valueOf((char)246), /* latin small letter o with diaeresis,
                                                  U+00F6 ISOlat1 */
        "divide",Character.valueOf((char)247), /* division sign, U+00F7 ISOnum */
        "oslash",Character.valueOf((char)248), /* latin small letter o with stroke,
                                                  = latin small letter o slash,
                                                  U+00F8 ISOlat1 */
        "ugrave",Character.valueOf((char)249), /* latin small letter u with grave,
                                                  U+00F9 ISOlat1 */
        "uacute",Character.valueOf((char)250), /* latin small letter u with acute,
                                                  U+00FA ISOlat1 */
        "ucirc",Character.valueOf((char)251), /* latin small letter u with circumflex,
                                                  U+00FB ISOlat1 */
        "uuml",Character.valueOf((char)252), /* latin small letter u with diaeresis,
                                                  U+00FC ISOlat1 */
        "yacute",Character.valueOf((char)253), /* latin small letter y with acute,
                                                  U+00FD ISOlat1 */
        "thorn",Character.valueOf((char)254), /* latin small letter thorn,
                                                  U+00FE ISOlat1 */
        "yuml",Character.valueOf((char)255), /* latin small letter y with diaeresis,
                                                  U+00FF ISOlat1 */        

                /* Latin Extended-B */
        "fnof",Character.valueOf((char)402), /* latin small f with hook = function
                                                    = florin, U+0192 ISOtech */

                /* Greek */
        "Alpha",Character.valueOf((char)913), /* greek capital letter alpha, U+0391 */
        "Beta",Character.valueOf((char)914), /* greek capital letter beta, U+0392 */
        "Gamma",Character.valueOf((char)915), /* greek capital letter gamma,
                                                    U+0393 ISOgrk3 */
        "Delta",Character.valueOf((char)916), /* greek capital letter delta,
                                                    U+0394 ISOgrk3 */
        "Epsilon",Character.valueOf((char)917), /* greek capital letter epsilon, U+0395 */
        "Zeta",Character.valueOf((char)918), /* greek capital letter zeta, U+0396 */
        "Eta",Character.valueOf((char)919), /* greek capital letter eta, U+0397 */
        "Theta",Character.valueOf((char)920), /* greek capital letter theta,
                                                    U+0398 ISOgrk3 */
        "Iota",Character.valueOf((char)921), /* greek capital letter iota, U+0399 */
        "Kappa",Character.valueOf((char)922), /* greek capital letter kappa, U+039A */
        "Lambda",Character.valueOf((char)923), /* greek capital letter lambda,
                                                    U+039B ISOgrk3 */
        "Mu",Character.valueOf((char)924), /* greek capital letter mu, U+039C */
        "Nu",Character.valueOf((char)925), /* greek capital letter nu, U+039D */
        "Xi",Character.valueOf((char)926), /* greek capital letter xi, U+039E ISOgrk3 */
        "Omicron",Character.valueOf((char)927), /* greek capital letter omicron, U+039F */
        "Pi",Character.valueOf((char)928), /* greek capital letter pi, U+03A0 ISOgrk3 */
        "Rho",Character.valueOf((char)929), /* greek capital letter rho, U+03A1 */
                /* there is no Sigmaf, and no U+03A2 character either */
        "Sigma",Character.valueOf((char)931), /* greek capital letter sigma,
                                                    U+03A3 ISOgrk3 */
        "Tau",Character.valueOf((char)932), /* greek capital letter tau, U+03A4 */
        "Upsilon",Character.valueOf((char)933), /* greek capital letter upsilon,
                                                    U+03A5 ISOgrk3 */
        "Phi",Character.valueOf((char)934), /* greek capital letter phi,
                                                    U+03A6 ISOgrk3 */
        "Chi",Character.valueOf((char)935), /* greek capital letter chi, U+03A7 */
        "Psi",Character.valueOf((char)936), /* greek capital letter psi,
                                                    U+03A8 ISOgrk3 */
        "Omega",Character.valueOf((char)937), /* greek capital letter omega,
                                                    U+03A9 ISOgrk3 */

        "alpha",Character.valueOf((char)945), /* greek small letter alpha,
                                                    U+03B1 ISOgrk3 */
        "beta",Character.valueOf((char)946), /* greek small letter beta, U+03B2 ISOgrk3 */
        "gamma",Character.valueOf((char)947), /* greek small letter gamma,
                                                    U+03B3 ISOgrk3 */
        "delta",Character.valueOf((char)948), /* greek small letter delta,
                                                    U+03B4 ISOgrk3 */
        "epsilon",Character.valueOf((char)949), /* greek small letter epsilon,
                                                    U+03B5 ISOgrk3 */
        "zeta",Character.valueOf((char)950), /* greek small letter zeta, U+03B6 ISOgrk3 */
        "eta",Character.valueOf((char)951), /* greek small letter eta, U+03B7 ISOgrk3 */
        "theta",Character.valueOf((char)952), /* greek small letter theta,
                                                    U+03B8 ISOgrk3 */
        "iota",Character.valueOf((char)953), /* greek small letter iota, U+03B9 ISOgrk3 */
        "kappa",Character.valueOf((char)954), /* greek small letter kappa,
                                                    U+03BA ISOgrk3 */
        "lambda",Character.valueOf((char)955), /* greek small letter lambda,
                                                    U+03BB ISOgrk3 */
        "mu",Character.valueOf((char)956), /* greek small letter mu, U+03BC ISOgrk3 */
        "nu",Character.valueOf((char)957), /* greek small letter nu, U+03BD ISOgrk3 */
        "xi",Character.valueOf((char)958), /* greek small letter xi, U+03BE ISOgrk3 */
        "omicron",Character.valueOf((char)959), /* greek small letter omicron, U+03BF NEW */
        "pi",Character.valueOf((char)960), /* greek small letter pi, U+03C0 ISOgrk3 */
        "rho",Character.valueOf((char)961), /* greek small letter rho, U+03C1 ISOgrk3 */
        "sigmaf",Character.valueOf((char)962), /* greek small letter final sigma,
                                                    U+03C2 ISOgrk3 */
        "sigma",Character.valueOf((char)963), /* greek small letter sigma,
                                                    U+03C3 ISOgrk3 */
        "tau",Character.valueOf((char)964), /* greek small letter tau, U+03C4 ISOgrk3 */
        "upsilon",Character.valueOf((char)965), /* greek small letter upsilon,
                                                    U+03C5 ISOgrk3 */
        "phi",Character.valueOf((char)966), /* greek small letter phi, U+03C6 ISOgrk3 */
        "chi",Character.valueOf((char)967), /* greek small letter chi, U+03C7 ISOgrk3 */
        "psi",Character.valueOf((char)968), /* greek small letter psi, U+03C8 ISOgrk3 */
        "omega",Character.valueOf((char)969), /* greek small letter omega,
                                                    U+03C9 ISOgrk3 */
        "thetasym",Character.valueOf((char)977), /* greek small letter theta symbol,
                                                    U+03D1 NEW */
        "upsih",Character.valueOf((char)978), /* greek upsilon with hook symbol,
                                                    U+03D2 NEW */
        "piv",Character.valueOf((char)982), /* greek pi symbol, U+03D6 ISOgrk3 */

                /* General Punctuation */
        "bull",Character.valueOf((char)8226), /* bullet = black small circle,
                                                     U+2022 ISOpub  */
                /* bullet is NOT the same as bullet operator, U+2219 */
        "hellip",Character.valueOf((char)8230), /* horizontal ellipsis = three dot leader,
                                                     U+2026 ISOpub  */
        "prime",Character.valueOf((char)8242), /* prime = minutes = feet, U+2032 ISOtech */
        "Prime",Character.valueOf((char)8243), /* double prime = seconds = inches,
                                                     U+2033 ISOtech */
        "oline",Character.valueOf((char)8254), /* overline = spacing overscore,
                                                     U+203E NEW */
        "frasl",Character.valueOf((char)8260), /* fraction slash, U+2044 NEW */

                /* Letterlike Symbols */
        "weierp",Character.valueOf((char)8472), /* script capital P = power set
                                                     = Weierstrass p, U+2118 ISOamso */
        "image",Character.valueOf((char)8465), /* blackletter capital I = imaginary part,
                                                     U+2111 ISOamso */
        "real",Character.valueOf((char)8476), /* blackletter capital R = real part symbol,
                                                     U+211C ISOamso */
        "trade",Character.valueOf((char)8482), /* trade mark sign, U+2122 ISOnum */
        "alefsym",Character.valueOf((char)8501), /* alef symbol = first transfinite cardinal,
                                                     U+2135 NEW */
                /* alef symbol is NOT the same as hebrew letter alef,
                     U+05D0 although the same glyph could be used to depict both characters */

                /* Arrows */
        "larr",Character.valueOf((char)8592), /* leftwards arrow, U+2190 ISOnum */
        "uarr",Character.valueOf((char)8593), /* upwards arrow, U+2191 ISOnum*/
        "rarr",Character.valueOf((char)8594), /* rightwards arrow, U+2192 ISOnum */
        "darr",Character.valueOf((char)8595), /* downwards arrow, U+2193 ISOnum */
        "harr",Character.valueOf((char)8596), /* left right arrow, U+2194 ISOamsa */
        "crarr",Character.valueOf((char)8629), /* downwards arrow with corner leftwards
                                                     = carriage return, U+21B5 NEW */
        "lArr",Character.valueOf((char)8656), /* leftwards double arrow, U+21D0 ISOtech */
                /* ISO 10646 does not say that lArr is the same as the 'is implied by' arrow
                    but also does not have any other character for that function. So ? lArr can
                    be used for 'is implied by' as ISOtech suggests */
        "uArr",Character.valueOf((char)8657), /* upwards double arrow, U+21D1 ISOamsa */
        "rArr",Character.valueOf((char)8658), /* rightwards double arrow,
                                                     U+21D2 ISOtech */
                /* ISO 10646 does not say this is the 'implies' character but does not have 
                     another character with this function so ?
                     rArr can be used for 'implies' as ISOtech suggests */
        "dArr",Character.valueOf((char)8659), /* downwards double arrow, U+21D3 ISOamsa */
        "hArr",Character.valueOf((char)8660), /* left right double arrow,
                                                     U+21D4 ISOamsa */

                /* Mathematical Operators */
        "forall",Character.valueOf((char)8704), /* for all, U+2200 ISOtech */
        "part",Character.valueOf((char)8706), /* partial differential, U+2202 ISOtech  */
        "exist",Character.valueOf((char)8707), /* there exists, U+2203 ISOtech */
        "empty",Character.valueOf((char)8709), /* empty set = null set = diameter,
                                                     U+2205 ISOamso */
        "nabla",Character.valueOf((char)8711), /* nabla = backward difference,
                                                     U+2207 ISOtech */
        "isin",Character.valueOf((char)8712), /* element of, U+2208 ISOtech */
        "notin",Character.valueOf((char)8713), /* not an element of, U+2209 ISOtech */
        "ni",Character.valueOf((char)8715), /* contains as member, U+220B ISOtech */
                /* should there be a more memorable name than 'ni'? */
        "prod",Character.valueOf((char)8719), /* n-ary product = product sign,
                                                     U+220F ISOamsb */
                /* prod is NOT the same character as U+03A0 'greek capital letter pi' though
                     the same glyph might be used for both */
        "sum",Character.valueOf((char)8721), /* n-ary sumation, U+2211 ISOamsb */
                /* sum is NOT the same character as U+03A3 'greek capital letter sigma'
                     though the same glyph might be used for both */
        "minus",Character.valueOf((char)8722), /* minus sign, U+2212 ISOtech */
        "lowast",Character.valueOf((char)8727), /* asterisk operator, U+2217 ISOtech */
        "radic",Character.valueOf((char)8730), /* square root = radical sign,
                                                     U+221A ISOtech */
        "prop",Character.valueOf((char)8733), /* proportional to, U+221D ISOtech */
        "infin",Character.valueOf((char)8734), /* infinity, U+221E ISOtech */
        "ang",Character.valueOf((char)8736), /* angle, U+2220 ISOamso */
        "and",Character.valueOf((char)8743), /* logical and = wedge, U+2227 ISOtech */
        "or",Character.valueOf((char)8744), /* logical or = vee, U+2228 ISOtech */
        "cap",Character.valueOf((char)8745), /* intersection = cap, U+2229 ISOtech */
        "cup",Character.valueOf((char)8746), /* union = cup, U+222A ISOtech */
        "int",Character.valueOf((char)8747), /* integral, U+222B ISOtech */
        "there4",Character.valueOf((char)8756), /* therefore, U+2234 ISOtech */
        "sim",Character.valueOf((char)8764), /* tilde operator = varies with = similar to,
                                                     U+223C ISOtech */
                /* tilde operator is NOT the same character as the tilde, U+007E,
                     although the same glyph might be used to represent both  */
        "cong",Character.valueOf((char)8773), /* approximately equal to, U+2245 ISOtech */
        "asymp",Character.valueOf((char)8776), /* almost equal to = asymptotic to,
                                                     U+2248 ISOamsr */
        "ne",Character.valueOf((char)8800), /* not equal to, U+2260 ISOtech */
        "equiv",Character.valueOf((char)8801), /* identical to, U+2261 ISOtech */
        "le",Character.valueOf((char)8804), /* less-than or equal to, U+2264 ISOtech */
        "ge",Character.valueOf((char)8805), /* greater-than or equal to,
                                                     U+2265 ISOtech */
        "sub",Character.valueOf((char)8834), /* subset of, U+2282 ISOtech */
        "sup",Character.valueOf((char)8835), /* superset of, U+2283 ISOtech */
                /* note that nsup, 'not a superset of, U+2283' is not covered by the Symbol 
                     font encoding and is not included. Should it be, for symmetry?
                     It is in ISOamsn  */ 
        "nsub",Character.valueOf((char)8836), /* not a subset of, U+2284 ISOamsn */
        "sube",Character.valueOf((char)8838), /* subset of or equal to, U+2286 ISOtech */
        "supe",Character.valueOf((char)8839), /* superset of or equal to,
                                                     U+2287 ISOtech */
        "oplus",Character.valueOf((char)8853), /* circled plus = direct sum,
                                                     U+2295 ISOamsb */
        "otimes",Character.valueOf((char)8855), /* circled times = vector product,
                                                     U+2297 ISOamsb */
        "perp",Character.valueOf((char)8869), /* up tack = orthogonal to = perpendicular,
                                                     U+22A5 ISOtech */
        "sdot",Character.valueOf((char)8901), /* dot operator, U+22C5 ISOamsb */
                /* dot operator is NOT the same character as U+00B7 middle dot */

                /* Miscellaneous Technical */
        "lceil",Character.valueOf((char)8968), /* left ceiling = apl upstile,
                                                     U+2308 ISOamsc  */
        "rceil",Character.valueOf((char)8969), /* right ceiling, U+2309 ISOamsc  */
        "lfloor",Character.valueOf((char)8970), /* left floor = apl downstile,
                                                     U+230A ISOamsc  */
        "rfloor",Character.valueOf((char)8971), /* right floor, U+230B ISOamsc  */
        "lang",Character.valueOf((char)9001), /* left-pointing angle bracket = bra,
                                                     U+2329 ISOtech */
                /* lang is NOT the same character as U+003C 'less than' 
                     or U+2039 'single left-pointing angle quotation mark' */
        "rang",Character.valueOf((char)9002), /* right-pointing angle bracket = ket,
                                                     U+232A ISOtech */
                /* rang is NOT the same character as U+003E 'greater than' 
                     or U+203A 'single right-pointing angle quotation mark' */

                /* Geometric Shapes */
        "loz",Character.valueOf((char)9674), /* lozenge, U+25CA ISOpub */

                /* Miscellaneous Symbols */
        "spades",Character.valueOf((char)9824), /* black spade suit, U+2660 ISOpub */
                /* black here seems to mean filled as opposed to hollow */
        "clubs",Character.valueOf((char)9827), /* black club suit = shamrock,
                                                     U+2663 ISOpub */
        "hearts",Character.valueOf((char)9829), /* black heart suit = valentine,
                                                     U+2665 ISOpub */
        "diams",Character.valueOf((char)9830), /* black diamond suit, U+2666 ISOpub */


                /* C0 Controls and Basic Latin */
        "quot",Character.valueOf((char)34)  , /* quotation mark = APL quote,
                                                    U+0022 ISOnum */
        "amp",Character.valueOf((char)38)  , /* ampersand, U+0026 ISOnum */
        "lt",Character.valueOf((char)60)  , /* less-than sign, U+003C ISOnum */
        "gt",Character.valueOf((char)62)  , /* greater-than sign, U+003E ISOnum */

                /* Latin Extended-A */
        "OElig",Character.valueOf((char)338) , /* latin capital ligature OE,
                                                    U+0152 ISOlat2 */
        "oelig",Character.valueOf((char)339) , /* latin small ligature oe, U+0153 ISOlat2 */
                /* ligature is a misnomer, this is a separate character in some languages */
        "Scaron",Character.valueOf((char)352) , /* latin capital letter S with caron,
                                                    U+0160 ISOlat2 */
        "scaron",Character.valueOf((char)353) , /* latin small letter s with caron,
                                                    U+0161 ISOlat2 */
        "Yuml",Character.valueOf((char)376) , /* latin capital letter Y with diaeresis,
                                                    U+0178 ISOlat2 */

                /* Spacing Modifier Letters */
        "circ",Character.valueOf((char)710) , /* modifier letter circumflex accent,
                                                    U+02C6 ISOpub */
        "tilde",Character.valueOf((char)732) , /* small tilde, U+02DC ISOdia */

                /* General Punctuation */
        "ensp",Character.valueOf((char)8194), /* en space, U+2002 ISOpub */
        "emsp",Character.valueOf((char)8195), /* em space, U+2003 ISOpub */
        "thinsp",Character.valueOf((char)8201), /* thin space, U+2009 ISOpub */
        "zwnj",Character.valueOf((char)8204), /* zero width non-joiner,
                                                    U+200C NEW RFC 2070 */
        "zwj",Character.valueOf((char)8205), /* zero width joiner, U+200D NEW RFC 2070 */
        "lrm",Character.valueOf((char)8206), /* left-to-right mark, U+200E NEW RFC 2070 */
        "rlm",Character.valueOf((char)8207), /* right-to-left mark, U+200F NEW RFC 2070 */
        "ndash",Character.valueOf((char)8211), /* en dash, U+2013 ISOpub */
        "mdash",Character.valueOf((char)8212), /* em dash, U+2014 ISOpub */
        "lsquo",Character.valueOf((char)8216), /* left single quotation mark,
                                                    U+2018 ISOnum */
        "rsquo",Character.valueOf((char)8217), /* right single quotation mark,
                                                    U+2019 ISOnum */
        "sbquo",Character.valueOf((char)8218), /* single low-9 quotation mark, U+201A NEW */
        "ldquo",Character.valueOf((char)8220), /* left double quotation mark,
                                                    U+201C ISOnum */
        "rdquo",Character.valueOf((char)8221), /* right double quotation mark,
                                                    U+201D ISOnum */
        "bdquo",Character.valueOf((char)8222), /* double low-9 quotation mark, U+201E NEW */
        "dagger",Character.valueOf((char)8224), /* dagger, U+2020 ISOpub */
        "Dagger",Character.valueOf((char)8225), /* double dagger, U+2021 ISOpub */
        "permil",Character.valueOf((char)8240), /* per mille sign, U+2030 ISOtech */
        "lsaquo",Character.valueOf((char)8249), /* single left-pointing angle quotation mark,
                                                    U+2039 ISO proposed */
                /* lsaquo is proposed but not yet ISO standardized */
        "rsaquo",Character.valueOf((char)8250), /* single right-pointing angle quotation mark,
                                                    U+203A ISO proposed */
                /* rsaquo is proposed but not yet ISO standardized */
        "euro",Character.valueOf((char)8364) , /* euro sign, U+20AC NEW */
    });
    /**
     * The inverse of table entitiesTable.
     */
    public static final Map inverseTable=new HashMap();
    static{
        Iterator iter=entitiesTable.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            inverseTable.put(e.getValue(),e.getKey());
        }
    }
    
    public static String encode(String text){
        return encode(text,true);
    }
    /**
     * Encodes all characters in the text with character code outside range
     * 32 to 127 (inclusive) and the ampersand and less-than characters.
     * If trytable is true, tries to find a html entity from the entities table.
     * If it is false or no suitable entity is found, uses an entity of the
     * form "&#xxx;" where xxx is the character code.
     */
    public static String encode(String text,boolean trytable){
        StringBuffer buf=new StringBuffer(text);
        int ptr=0;
        while(ptr<buf.length()){
            char c=buf.charAt(ptr);
            if(c<32 || c>127 || c=='&' || c=='<'){
                String ent=null;
                if(trytable) {
                    ent=(String)inverseTable.get(Character.valueOf(c));
                    if(ent!=null) ent="&"+ent+";";
                }
                if(ent==null){
                    ent="&#"+((int)c)+";";
                }
                buf.replace(ptr, ptr+1,ent);
                ptr+=ent.length();
            }
            else ptr++;
        }
        return buf.toString();
    }
    
    private static int getNumber(String ent){
        int ptr=0;
        char c=ent.charAt(0);
        boolean hex=false;
        if(c=='x' || c=='X'){
            hex=true;
            ptr=1;
        }
        for(int i=ptr;i<ent.length();i++){
            if(!Character.isDigit(ent.charAt(i))) return -1;
        }
        return Integer.parseInt(ent,(hex?16:10));
    }
    
    /**
     * Decodes html entities in the text.
     */
    public static String decode(String htmlText){
        StringBuffer buf=new StringBuffer(htmlText);
        int ind=-1;
        int ptr=0;
        while( (ind=buf.indexOf("&",ptr))!= -1 ){
            ptr=ind+1;
            int ind2=buf.indexOf(";",ind);
            if(ind2==-1){
                continue;
            }
            String ent=buf.substring(ind+1,ind2);
            if(ent.startsWith("#")) {
                    ent = ent.substring(1);
            }
            Character cha=(Character)entitiesTable.get(ent);
            if(cha!=null){
                buf.replace(ind, ind2+1,cha.toString());
            }
            else{
                int num=getNumber(ent);
                if(num!=-1){
                    buf.replace(ind,ind2+1,Character.valueOf((char)num).toString());
                }
            }
        }
        return buf.toString();
    }
    
    /** Creates a new instance of HTMLEntitiesCoder */
    public HTMLEntitiesCoder() {
    }
    
}
