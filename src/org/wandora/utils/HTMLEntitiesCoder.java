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
 * HTMLEntitiesCoder.java
 *
 * Created on 3. joulukuuta 2004, 10:36
 */

package org.wandora.utils;
import java.util.*;

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
        "nbsp",new Character((char)160), /* no-break space = non-breaking space,
                                                  U+00A0 ISOnum */
        "iexcl",new Character((char)161), /* inverted exclamation mark, U+00A1 ISOnum */
        "cent",new Character((char)162), /* cent sign, U+00A2 ISOnum */
        "pound",new Character((char)163), /* pound sign, U+00A3 ISOnum */
        "curren",new Character((char)164), /* currency sign, U+00A4 ISOnum */
        "yen",new Character((char)165), /* yen sign = yuan sign, U+00A5 ISOnum */
        "brvbar",new Character((char)166), /* broken bar = broken vertical bar,
                                                  U+00A6 ISOnum */
        "sect",new Character((char)167), /* section sign, U+00A7 ISOnum */
        "uml",new Character((char)168), /* diaeresis = spacing diaeresis,
                                                  U+00A8 ISOdia */
        "copy",new Character((char)169), /* copyright sign, U+00A9 ISOnum */
        "ordf",new Character((char)170), /* feminine ordinal indicator, U+00AA ISOnum */
        "laquo",new Character((char)171), /* left-pointing double angle quotation mark
                                                  = left pointing guillemet, U+00AB ISOnum */
        "not",new Character((char)172), /* not sign, U+00AC ISOnum */
        "shy",new Character((char)173), /* soft hyphen = discretionary hyphen,
                                                  U+00AD ISOnum */
        "reg",new Character((char)174), /* registered sign = registered trade mark sign,
                                                  U+00AE ISOnum */
        "macr",new Character((char)175), /* macron = spacing macron = overline
                                                  = APL overbar, U+00AF ISOdia */
        "deg",new Character((char)176), /* degree sign, U+00B0 ISOnum */
        "plusmn",new Character((char)177), /* plus-minus sign = plus-or-minus sign,
                                                  U+00B1 ISOnum */
        "sup2",new Character((char)178), /* superscript two = superscript digit two
                                                  = squared, U+00B2 ISOnum */
        "sup3",new Character((char)179), /* superscript three = superscript digit three
                                                  = cubed, U+00B3 ISOnum */
        "acute",new Character((char)180), /* acute accent = spacing acute,
                                                  U+00B4 ISOdia */
        "micro",new Character((char)181), /* micro sign, U+00B5 ISOnum */
        "para",new Character((char)182), /* pilcrow sign = paragraph sign,
                                                  U+00B6 ISOnum */
        "middot",new Character((char)183), /* middle dot = Georgian comma
                                                  = Greek middle dot, U+00B7 ISOnum */
        "cedil",new Character((char)184), /* cedilla = spacing cedilla, U+00B8 ISOdia */
        "sup1",new Character((char)185), /* superscript one = superscript digit one,
                                                  U+00B9 ISOnum */
        "ordm",new Character((char)186), /* masculine ordinal indicator,
                                                  U+00BA ISOnum */
        "raquo",new Character((char)187), /* right-pointing double angle quotation mark
                                                  = right pointing guillemet, U+00BB ISOnum */
        "frac14",new Character((char)188), /* vulgar fraction one quarter
                                                  = fraction one quarter, U+00BC ISOnum */
        "frac12",new Character((char)189), /* vulgar fraction one half
                                                  = fraction one half, U+00BD ISOnum */
        "frac34",new Character((char)190), /* vulgar fraction three quarters
                                                  = fraction three quarters, U+00BE ISOnum */
        "iquest",new Character((char)191), /* inverted question mark
                                                  = turned question mark, U+00BF ISOnum */
        "Agrave",new Character((char)192), /* latin capital letter A with grave
                                                  = latin capital letter A grave,
                                                  U+00C0 ISOlat1 */
        "Aacute",new Character((char)193), /* latin capital letter A with acute,
                                                  U+00C1 ISOlat1 */
        "Acirc",new Character((char)194), /* latin capital letter A with circumflex,
                                                  U+00C2 ISOlat1 */
        "Atilde",new Character((char)195), /* latin capital letter A with tilde,
                                                  U+00C3 ISOlat1 */
        "Auml",new Character((char)196), /* latin capital letter A with diaeresis,
                                                  U+00C4 ISOlat1 */
        "Aring",new Character((char)197), /* latin capital letter A with ring above
                                                  = latin capital letter A ring,
                                                  U+00C5 ISOlat1 */
        "AElig",new Character((char)198), /* latin capital letter AE
                                                  = latin capital ligature AE,
                                                  U+00C6 ISOlat1 */
        "Ccedil",new Character((char)199), /* latin capital letter C with cedilla,
                                                  U+00C7 ISOlat1 */
        "Egrave",new Character((char)200), /* latin capital letter E with grave,
                                                  U+00C8 ISOlat1 */
        "Eacute",new Character((char)201), /* latin capital letter E with acute,
                                                  U+00C9 ISOlat1 */
        "Ecirc",new Character((char)202), /* latin capital letter E with circumflex,
                                                  U+00CA ISOlat1 */
        "Euml",new Character((char)203), /* latin capital letter E with diaeresis,
                                                  U+00CB ISOlat1 */
        "Igrave",new Character((char)204), /* latin capital letter I with grave,
                                                  U+00CC ISOlat1 */
        "Iacute",new Character((char)205), /* latin capital letter I with acute,
                                                  U+00CD ISOlat1 */
        "Icirc",new Character((char)206), /* latin capital letter I with circumflex,
                                                  U+00CE ISOlat1 */
        "Iuml",new Character((char)207), /* latin capital letter I with diaeresis,
                                                  U+00CF ISOlat1 */
        "ETH",new Character((char)208), /* latin capital letter ETH, U+00D0 ISOlat1 */
        "Ntilde",new Character((char)209), /* latin capital letter N with tilde,
                                                  U+00D1 ISOlat1 */
        "Ograve",new Character((char)210), /* latin capital letter O with grave,
                                                  U+00D2 ISOlat1 */
        "Oacute",new Character((char)211), /* latin capital letter O with acute,
                                                  U+00D3 ISOlat1 */
        "Ocirc",new Character((char)212), /* latin capital letter O with circumflex,
                                                  U+00D4 ISOlat1 */
        "Otilde",new Character((char)213), /* latin capital letter O with tilde,
                                                  U+00D5 ISOlat1 */
        "Ouml",new Character((char)214), /* latin capital letter O with diaeresis,
                                                  U+00D6 ISOlat1 */
        "times",new Character((char)215), /* multiplication sign, U+00D7 ISOnum */
        "Oslash",new Character((char)216), /* latin capital letter O with stroke
                                                  = latin capital letter O slash,
                                                  U+00D8 ISOlat1 */
        "Ugrave",new Character((char)217), /* latin capital letter U with grave,
                                                  U+00D9 ISOlat1 */
        "Uacute",new Character((char)218), /* latin capital letter U with acute,
                                                  U+00DA ISOlat1 */
        "Ucirc",new Character((char)219), /* latin capital letter U with circumflex,
                                                  U+00DB ISOlat1 */
        "Uuml",new Character((char)220), /* latin capital letter U with diaeresis,
                                                  U+00DC ISOlat1 */
        "Yacute",new Character((char)221), /* latin capital letter Y with acute,
                                                  U+00DD ISOlat1 */
        "THORN",new Character((char)222), /* latin capital letter THORN,
                                                  U+00DE ISOlat1 */
        "szlig",new Character((char)223), /* latin small letter sharp s = ess-zed,
                                                  U+00DF ISOlat1 */
        "agrave",new Character((char)224), /* latin small letter a with grave
                                                  = latin small letter a grave,
                                                  U+00E0 ISOlat1 */
        "aacute",new Character((char)225), /* latin small letter a with acute,
                                                  U+00E1 ISOlat1 */
        "acirc",new Character((char)226), /* latin small letter a with circumflex,
                                                  U+00E2 ISOlat1 */
        "atilde",new Character((char)227), /* latin small letter a with tilde,
                                                  U+00E3 ISOlat1 */
        "auml",new Character((char)228), /* latin small letter a with diaeresis,
                                                  U+00E4 ISOlat1 */
        "aring",new Character((char)229), /* latin small letter a with ring above
                                                  = latin small letter a ring,
                                                  U+00E5 ISOlat1 */
        "aelig",new Character((char)230), /* latin small letter ae
                                                  = latin small ligature ae, U+00E6 ISOlat1 */
        "ccedil",new Character((char)231), /* latin small letter c with cedilla,
                                                  U+00E7 ISOlat1 */
        "egrave",new Character((char)232), /* latin small letter e with grave,
                                                  U+00E8 ISOlat1 */
        "eacute",new Character((char)233), /* latin small letter e with acute,
                                                  U+00E9 ISOlat1 */
        "ecirc",new Character((char)234), /* latin small letter e with circumflex,
                                                  U+00EA ISOlat1 */
        "euml",new Character((char)235), /* latin small letter e with diaeresis,
                                                  U+00EB ISOlat1 */
        "igrave",new Character((char)236), /* latin small letter i with grave,
                                                  U+00EC ISOlat1 */
        "iacute",new Character((char)237), /* latin small letter i with acute,
                                                  U+00ED ISOlat1 */
        "icirc",new Character((char)238), /* latin small letter i with circumflex,
                                                  U+00EE ISOlat1 */
        "iuml",new Character((char)239), /* latin small letter i with diaeresis,
                                                  U+00EF ISOlat1 */
        "eth",new Character((char)240), /* latin small letter eth, U+00F0 ISOlat1 */
        "ntilde",new Character((char)241), /* latin small letter n with tilde,
                                                  U+00F1 ISOlat1 */
        "ograve",new Character((char)242), /* latin small letter o with grave,
                                                  U+00F2 ISOlat1 */
        "oacute",new Character((char)243), /* latin small letter o with acute,
                                                  U+00F3 ISOlat1 */
        "ocirc",new Character((char)244), /* latin small letter o with circumflex,
                                                  U+00F4 ISOlat1 */
        "otilde",new Character((char)245), /* latin small letter o with tilde,
                                                  U+00F5 ISOlat1 */
        "ouml",new Character((char)246), /* latin small letter o with diaeresis,
                                                  U+00F6 ISOlat1 */
        "divide",new Character((char)247), /* division sign, U+00F7 ISOnum */
        "oslash",new Character((char)248), /* latin small letter o with stroke,
                                                  = latin small letter o slash,
                                                  U+00F8 ISOlat1 */
        "ugrave",new Character((char)249), /* latin small letter u with grave,
                                                  U+00F9 ISOlat1 */
        "uacute",new Character((char)250), /* latin small letter u with acute,
                                                  U+00FA ISOlat1 */
        "ucirc",new Character((char)251), /* latin small letter u with circumflex,
                                                  U+00FB ISOlat1 */
        "uuml",new Character((char)252), /* latin small letter u with diaeresis,
                                                  U+00FC ISOlat1 */
        "yacute",new Character((char)253), /* latin small letter y with acute,
                                                  U+00FD ISOlat1 */
        "thorn",new Character((char)254), /* latin small letter thorn,
                                                  U+00FE ISOlat1 */
        "yuml",new Character((char)255), /* latin small letter y with diaeresis,
                                                  U+00FF ISOlat1 */        

                /* Latin Extended-B */
        "fnof",new Character((char)402), /* latin small f with hook = function
                                                    = florin, U+0192 ISOtech */

                /* Greek */
        "Alpha",new Character((char)913), /* greek capital letter alpha, U+0391 */
        "Beta",new Character((char)914), /* greek capital letter beta, U+0392 */
        "Gamma",new Character((char)915), /* greek capital letter gamma,
                                                    U+0393 ISOgrk3 */
        "Delta",new Character((char)916), /* greek capital letter delta,
                                                    U+0394 ISOgrk3 */
        "Epsilon",new Character((char)917), /* greek capital letter epsilon, U+0395 */
        "Zeta",new Character((char)918), /* greek capital letter zeta, U+0396 */
        "Eta",new Character((char)919), /* greek capital letter eta, U+0397 */
        "Theta",new Character((char)920), /* greek capital letter theta,
                                                    U+0398 ISOgrk3 */
        "Iota",new Character((char)921), /* greek capital letter iota, U+0399 */
        "Kappa",new Character((char)922), /* greek capital letter kappa, U+039A */
        "Lambda",new Character((char)923), /* greek capital letter lambda,
                                                    U+039B ISOgrk3 */
        "Mu",new Character((char)924), /* greek capital letter mu, U+039C */
        "Nu",new Character((char)925), /* greek capital letter nu, U+039D */
        "Xi",new Character((char)926), /* greek capital letter xi, U+039E ISOgrk3 */
        "Omicron",new Character((char)927), /* greek capital letter omicron, U+039F */
        "Pi",new Character((char)928), /* greek capital letter pi, U+03A0 ISOgrk3 */
        "Rho",new Character((char)929), /* greek capital letter rho, U+03A1 */
                /* there is no Sigmaf, and no U+03A2 character either */
        "Sigma",new Character((char)931), /* greek capital letter sigma,
                                                    U+03A3 ISOgrk3 */
        "Tau",new Character((char)932), /* greek capital letter tau, U+03A4 */
        "Upsilon",new Character((char)933), /* greek capital letter upsilon,
                                                    U+03A5 ISOgrk3 */
        "Phi",new Character((char)934), /* greek capital letter phi,
                                                    U+03A6 ISOgrk3 */
        "Chi",new Character((char)935), /* greek capital letter chi, U+03A7 */
        "Psi",new Character((char)936), /* greek capital letter psi,
                                                    U+03A8 ISOgrk3 */
        "Omega",new Character((char)937), /* greek capital letter omega,
                                                    U+03A9 ISOgrk3 */

        "alpha",new Character((char)945), /* greek small letter alpha,
                                                    U+03B1 ISOgrk3 */
        "beta",new Character((char)946), /* greek small letter beta, U+03B2 ISOgrk3 */
        "gamma",new Character((char)947), /* greek small letter gamma,
                                                    U+03B3 ISOgrk3 */
        "delta",new Character((char)948), /* greek small letter delta,
                                                    U+03B4 ISOgrk3 */
        "epsilon",new Character((char)949), /* greek small letter epsilon,
                                                    U+03B5 ISOgrk3 */
        "zeta",new Character((char)950), /* greek small letter zeta, U+03B6 ISOgrk3 */
        "eta",new Character((char)951), /* greek small letter eta, U+03B7 ISOgrk3 */
        "theta",new Character((char)952), /* greek small letter theta,
                                                    U+03B8 ISOgrk3 */
        "iota",new Character((char)953), /* greek small letter iota, U+03B9 ISOgrk3 */
        "kappa",new Character((char)954), /* greek small letter kappa,
                                                    U+03BA ISOgrk3 */
        "lambda",new Character((char)955), /* greek small letter lambda,
                                                    U+03BB ISOgrk3 */
        "mu",new Character((char)956), /* greek small letter mu, U+03BC ISOgrk3 */
        "nu",new Character((char)957), /* greek small letter nu, U+03BD ISOgrk3 */
        "xi",new Character((char)958), /* greek small letter xi, U+03BE ISOgrk3 */
        "omicron",new Character((char)959), /* greek small letter omicron, U+03BF NEW */
        "pi",new Character((char)960), /* greek small letter pi, U+03C0 ISOgrk3 */
        "rho",new Character((char)961), /* greek small letter rho, U+03C1 ISOgrk3 */
        "sigmaf",new Character((char)962), /* greek small letter final sigma,
                                                    U+03C2 ISOgrk3 */
        "sigma",new Character((char)963), /* greek small letter sigma,
                                                    U+03C3 ISOgrk3 */
        "tau",new Character((char)964), /* greek small letter tau, U+03C4 ISOgrk3 */
        "upsilon",new Character((char)965), /* greek small letter upsilon,
                                                    U+03C5 ISOgrk3 */
        "phi",new Character((char)966), /* greek small letter phi, U+03C6 ISOgrk3 */
        "chi",new Character((char)967), /* greek small letter chi, U+03C7 ISOgrk3 */
        "psi",new Character((char)968), /* greek small letter psi, U+03C8 ISOgrk3 */
        "omega",new Character((char)969), /* greek small letter omega,
                                                    U+03C9 ISOgrk3 */
        "thetasym",new Character((char)977), /* greek small letter theta symbol,
                                                    U+03D1 NEW */
        "upsih",new Character((char)978), /* greek upsilon with hook symbol,
                                                    U+03D2 NEW */
        "piv",new Character((char)982), /* greek pi symbol, U+03D6 ISOgrk3 */

                /* General Punctuation */
        "bull",new Character((char)8226), /* bullet = black small circle,
                                                     U+2022 ISOpub  */
                /* bullet is NOT the same as bullet operator, U+2219 */
        "hellip",new Character((char)8230), /* horizontal ellipsis = three dot leader,
                                                     U+2026 ISOpub  */
        "prime",new Character((char)8242), /* prime = minutes = feet, U+2032 ISOtech */
        "Prime",new Character((char)8243), /* double prime = seconds = inches,
                                                     U+2033 ISOtech */
        "oline",new Character((char)8254), /* overline = spacing overscore,
                                                     U+203E NEW */
        "frasl",new Character((char)8260), /* fraction slash, U+2044 NEW */

                /* Letterlike Symbols */
        "weierp",new Character((char)8472), /* script capital P = power set
                                                     = Weierstrass p, U+2118 ISOamso */
        "image",new Character((char)8465), /* blackletter capital I = imaginary part,
                                                     U+2111 ISOamso */
        "real",new Character((char)8476), /* blackletter capital R = real part symbol,
                                                     U+211C ISOamso */
        "trade",new Character((char)8482), /* trade mark sign, U+2122 ISOnum */
        "alefsym",new Character((char)8501), /* alef symbol = first transfinite cardinal,
                                                     U+2135 NEW */
                /* alef symbol is NOT the same as hebrew letter alef,
                     U+05D0 although the same glyph could be used to depict both characters */

                /* Arrows */
        "larr",new Character((char)8592), /* leftwards arrow, U+2190 ISOnum */
        "uarr",new Character((char)8593), /* upwards arrow, U+2191 ISOnum*/
        "rarr",new Character((char)8594), /* rightwards arrow, U+2192 ISOnum */
        "darr",new Character((char)8595), /* downwards arrow, U+2193 ISOnum */
        "harr",new Character((char)8596), /* left right arrow, U+2194 ISOamsa */
        "crarr",new Character((char)8629), /* downwards arrow with corner leftwards
                                                     = carriage return, U+21B5 NEW */
        "lArr",new Character((char)8656), /* leftwards double arrow, U+21D0 ISOtech */
                /* ISO 10646 does not say that lArr is the same as the 'is implied by' arrow
                    but also does not have any other character for that function. So ? lArr can
                    be used for 'is implied by' as ISOtech suggests */
        "uArr",new Character((char)8657), /* upwards double arrow, U+21D1 ISOamsa */
        "rArr",new Character((char)8658), /* rightwards double arrow,
                                                     U+21D2 ISOtech */
                /* ISO 10646 does not say this is the 'implies' character but does not have 
                     another character with this function so ?
                     rArr can be used for 'implies' as ISOtech suggests */
        "dArr",new Character((char)8659), /* downwards double arrow, U+21D3 ISOamsa */
        "hArr",new Character((char)8660), /* left right double arrow,
                                                     U+21D4 ISOamsa */

                /* Mathematical Operators */
        "forall",new Character((char)8704), /* for all, U+2200 ISOtech */
        "part",new Character((char)8706), /* partial differential, U+2202 ISOtech  */
        "exist",new Character((char)8707), /* there exists, U+2203 ISOtech */
        "empty",new Character((char)8709), /* empty set = null set = diameter,
                                                     U+2205 ISOamso */
        "nabla",new Character((char)8711), /* nabla = backward difference,
                                                     U+2207 ISOtech */
        "isin",new Character((char)8712), /* element of, U+2208 ISOtech */
        "notin",new Character((char)8713), /* not an element of, U+2209 ISOtech */
        "ni",new Character((char)8715), /* contains as member, U+220B ISOtech */
                /* should there be a more memorable name than 'ni'? */
        "prod",new Character((char)8719), /* n-ary product = product sign,
                                                     U+220F ISOamsb */
                /* prod is NOT the same character as U+03A0 'greek capital letter pi' though
                     the same glyph might be used for both */
        "sum",new Character((char)8721), /* n-ary sumation, U+2211 ISOamsb */
                /* sum is NOT the same character as U+03A3 'greek capital letter sigma'
                     though the same glyph might be used for both */
        "minus",new Character((char)8722), /* minus sign, U+2212 ISOtech */
        "lowast",new Character((char)8727), /* asterisk operator, U+2217 ISOtech */
        "radic",new Character((char)8730), /* square root = radical sign,
                                                     U+221A ISOtech */
        "prop",new Character((char)8733), /* proportional to, U+221D ISOtech */
        "infin",new Character((char)8734), /* infinity, U+221E ISOtech */
        "ang",new Character((char)8736), /* angle, U+2220 ISOamso */
        "and",new Character((char)8743), /* logical and = wedge, U+2227 ISOtech */
        "or",new Character((char)8744), /* logical or = vee, U+2228 ISOtech */
        "cap",new Character((char)8745), /* intersection = cap, U+2229 ISOtech */
        "cup",new Character((char)8746), /* union = cup, U+222A ISOtech */
        "int",new Character((char)8747), /* integral, U+222B ISOtech */
        "there4",new Character((char)8756), /* therefore, U+2234 ISOtech */
        "sim",new Character((char)8764), /* tilde operator = varies with = similar to,
                                                     U+223C ISOtech */
                /* tilde operator is NOT the same character as the tilde, U+007E,
                     although the same glyph might be used to represent both  */
        "cong",new Character((char)8773), /* approximately equal to, U+2245 ISOtech */
        "asymp",new Character((char)8776), /* almost equal to = asymptotic to,
                                                     U+2248 ISOamsr */
        "ne",new Character((char)8800), /* not equal to, U+2260 ISOtech */
        "equiv",new Character((char)8801), /* identical to, U+2261 ISOtech */
        "le",new Character((char)8804), /* less-than or equal to, U+2264 ISOtech */
        "ge",new Character((char)8805), /* greater-than or equal to,
                                                     U+2265 ISOtech */
        "sub",new Character((char)8834), /* subset of, U+2282 ISOtech */
        "sup",new Character((char)8835), /* superset of, U+2283 ISOtech */
                /* note that nsup, 'not a superset of, U+2283' is not covered by the Symbol 
                     font encoding and is not included. Should it be, for symmetry?
                     It is in ISOamsn  */ 
        "nsub",new Character((char)8836), /* not a subset of, U+2284 ISOamsn */
        "sube",new Character((char)8838), /* subset of or equal to, U+2286 ISOtech */
        "supe",new Character((char)8839), /* superset of or equal to,
                                                     U+2287 ISOtech */
        "oplus",new Character((char)8853), /* circled plus = direct sum,
                                                     U+2295 ISOamsb */
        "otimes",new Character((char)8855), /* circled times = vector product,
                                                     U+2297 ISOamsb */
        "perp",new Character((char)8869), /* up tack = orthogonal to = perpendicular,
                                                     U+22A5 ISOtech */
        "sdot",new Character((char)8901), /* dot operator, U+22C5 ISOamsb */
                /* dot operator is NOT the same character as U+00B7 middle dot */

                /* Miscellaneous Technical */
        "lceil",new Character((char)8968), /* left ceiling = apl upstile,
                                                     U+2308 ISOamsc  */
        "rceil",new Character((char)8969), /* right ceiling, U+2309 ISOamsc  */
        "lfloor",new Character((char)8970), /* left floor = apl downstile,
                                                     U+230A ISOamsc  */
        "rfloor",new Character((char)8971), /* right floor, U+230B ISOamsc  */
        "lang",new Character((char)9001), /* left-pointing angle bracket = bra,
                                                     U+2329 ISOtech */
                /* lang is NOT the same character as U+003C 'less than' 
                     or U+2039 'single left-pointing angle quotation mark' */
        "rang",new Character((char)9002), /* right-pointing angle bracket = ket,
                                                     U+232A ISOtech */
                /* rang is NOT the same character as U+003E 'greater than' 
                     or U+203A 'single right-pointing angle quotation mark' */

                /* Geometric Shapes */
        "loz",new Character((char)9674), /* lozenge, U+25CA ISOpub */

                /* Miscellaneous Symbols */
        "spades",new Character((char)9824), /* black spade suit, U+2660 ISOpub */
                /* black here seems to mean filled as opposed to hollow */
        "clubs",new Character((char)9827), /* black club suit = shamrock,
                                                     U+2663 ISOpub */
        "hearts",new Character((char)9829), /* black heart suit = valentine,
                                                     U+2665 ISOpub */
        "diams",new Character((char)9830), /* black diamond suit, U+2666 ISOpub */


                /* C0 Controls and Basic Latin */
        "quot",new Character((char)34)  , /* quotation mark = APL quote,
                                                    U+0022 ISOnum */
        "amp",new Character((char)38)  , /* ampersand, U+0026 ISOnum */
        "lt",new Character((char)60)  , /* less-than sign, U+003C ISOnum */
        "gt",new Character((char)62)  , /* greater-than sign, U+003E ISOnum */

                /* Latin Extended-A */
        "OElig",new Character((char)338) , /* latin capital ligature OE,
                                                    U+0152 ISOlat2 */
        "oelig",new Character((char)339) , /* latin small ligature oe, U+0153 ISOlat2 */
                /* ligature is a misnomer, this is a separate character in some languages */
        "Scaron",new Character((char)352) , /* latin capital letter S with caron,
                                                    U+0160 ISOlat2 */
        "scaron",new Character((char)353) , /* latin small letter s with caron,
                                                    U+0161 ISOlat2 */
        "Yuml",new Character((char)376) , /* latin capital letter Y with diaeresis,
                                                    U+0178 ISOlat2 */

                /* Spacing Modifier Letters */
        "circ",new Character((char)710) , /* modifier letter circumflex accent,
                                                    U+02C6 ISOpub */
        "tilde",new Character((char)732) , /* small tilde, U+02DC ISOdia */

                /* General Punctuation */
        "ensp",new Character((char)8194), /* en space, U+2002 ISOpub */
        "emsp",new Character((char)8195), /* em space, U+2003 ISOpub */
        "thinsp",new Character((char)8201), /* thin space, U+2009 ISOpub */
        "zwnj",new Character((char)8204), /* zero width non-joiner,
                                                    U+200C NEW RFC 2070 */
        "zwj",new Character((char)8205), /* zero width joiner, U+200D NEW RFC 2070 */
        "lrm",new Character((char)8206), /* left-to-right mark, U+200E NEW RFC 2070 */
        "rlm",new Character((char)8207), /* right-to-left mark, U+200F NEW RFC 2070 */
        "ndash",new Character((char)8211), /* en dash, U+2013 ISOpub */
        "mdash",new Character((char)8212), /* em dash, U+2014 ISOpub */
        "lsquo",new Character((char)8216), /* left single quotation mark,
                                                    U+2018 ISOnum */
        "rsquo",new Character((char)8217), /* right single quotation mark,
                                                    U+2019 ISOnum */
        "sbquo",new Character((char)8218), /* single low-9 quotation mark, U+201A NEW */
        "ldquo",new Character((char)8220), /* left double quotation mark,
                                                    U+201C ISOnum */
        "rdquo",new Character((char)8221), /* right double quotation mark,
                                                    U+201D ISOnum */
        "bdquo",new Character((char)8222), /* double low-9 quotation mark, U+201E NEW */
        "dagger",new Character((char)8224), /* dagger, U+2020 ISOpub */
        "Dagger",new Character((char)8225), /* double dagger, U+2021 ISOpub */
        "permil",new Character((char)8240), /* per mille sign, U+2030 ISOtech */
        "lsaquo",new Character((char)8249), /* single left-pointing angle quotation mark,
                                                    U+2039 ISO proposed */
                /* lsaquo is proposed but not yet ISO standardized */
        "rsaquo",new Character((char)8250), /* single right-pointing angle quotation mark,
                                                    U+203A ISO proposed */
                /* rsaquo is proposed but not yet ISO standardized */
        "euro",new Character((char)8364) , /* euro sign, U+20AC NEW */
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
                    ent=(String)inverseTable.get(new Character(c));
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
                    buf.replace(ind,ind2+1,new Character((char)num).toString());
                }
            }
        }
        return buf.toString();
    }
    
    /** Creates a new instance of HTMLEntitiesCoder */
    public HTMLEntitiesCoder() {
    }
    
}
