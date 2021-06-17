/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universitaet Karlsruhe (TH) nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.htmlXmlUtil.grammars;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;

import de.uka.ipd.idaho.htmlXmlUtil.TokenSource.Token;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.IoTools;

/**
 * Default Grammar for handling HTML, defining all the elements, etc. This
 * grammar switches on error correction in order to facilitate parsing any
 * given real-world HTML page as far as possible.
 * 
 * @author sautter
 */
public class Html extends StandardGrammar {
	
	//	HTML specific data
	private static final String tagTypeToParentTypesMappingStrings[] = {
			"p:address;applet;blockquote;body;button;center;del;dd;div;span;fieldset;form;iframe;ins;li;map;noframes;noscript;object;header;nav;main;article;section;footer;aside;address;td;th",
			"noframes:applet;blockquote;body;button;center;dd;del;div;span;fieldset;form;frameset;iframe;ins;li;map;noscript;object;header;nav;main;article;section;footer;aside;address;td;th",
			"h6:h5:h4:h3:h2:h1:table:ul:ol:menu:dl:dir:pre:noscript:hr:div:center:header:nav:main:article:section:footer:aside:address:blockquote:address:applet;blockquote;body;button;center;dd;del;div;span;fieldset;form;iframe;ins;li;map;noframes;noscript;object;header;nav;main;article;section;footer;aside;address;td;th",
			"isindex:applet;blockquote;body;center;dd;del;div;span;fieldset;form;head;iframe;ins;li;map;noframes;noscript;object;header;nav;main;article;section;footer;aside;address;td;th",
			"fieldset:applet;blockquote;body;center;dd;del;div;span;fieldset;form;iframe;ins;li;map;noframes;noscript;object;header;nav;main;article;section;footer;aside;address;td;th",
			"form:applet;blockquote;body;center;dd;del;div;span;fieldset;iframe;ins;li;map;noframes;noscript;object;header;nav;main;article;section;footer;aside;address;table;td;th;tr",
			"param:applet;object",
			"col:colgroup;table",
			"dd:dt:dl",
			"legend:fieldset",
			"frame:frameset",
			"title:style:meta:link:base:head",
			"head:html",
			"body:htm;html;noframes",
			"frameset:htm;html;frameset",
			"area:map",
			"li:ol;ul;dir;font;menu",
			"optgroup:select",
			"option:select;optgroup",
			"tbody:tfoot:thead:colgroup:caption:table;form",
			"tr:table;thead;tfoot;tbody;form",
			"th:td:tr;form",
		};
		
	private static final String tagTypeTranslationStrings[] = {
			"htm:html",
		};
	
	private static final String singularTagTypesString = "area;base;br;hr;img;input;isindex;link;meta;param";
	
	private static final String subsequentEndTagsAllowedTypesString = "frameset;ul;ol;dl;div;span";
	
	private static final String whitespaceFreeValueAttributesString = "align;clear;columns;colspan;dataformatas;dir;for;frame;frameborder;headers;http-equiv;id;maxlength;method;name;nohref;rows;rowspan;rules;scheme;scope;scrolling;shape;span;start;tabindex;type;valign;valuetype;width";
	
	private static final String[] characterEncodingsStrings = {
			"! &excl;",
			"\" &quot; &QUOT;",
			"# &num;",
			"$ &dollar;",
			"% &percnt;",
			"& &amp; &AMP;",
			"' &apos;",
			"( &lpar;",
			") &rpar;",
			"* &ast; &midast;",
			"+ &plus;",
			", &comma;",
			". &period;",
			"/ &sol;",
			", &colon;",
			", &semi;",
			"< &lt; &LT;",
			"= &equals;",
			"> &gt; &GT;",
			"? &quest;",
			", &commat;",
			"[ &lsqb; &lbrack;",
			"\\ &bsol;",
			"] &rsqb; &rbrack;",
			"^ &Hat;",
			"_ &lowbar; &UnderBar;",
			"` &grave; &DiacriticalGrave;",
			"{ &lcub; &lbrace;",
			"| &verbar; &vert; &VerticalLine;",
			"} &rcub; &rbrace;",
			//"\u00A0 &nbsp; &NonBreakingSpace;", // not escaping space
			"\u00A1 &iexcl;",
			"\u00A2 &cent;",
			"\u00A3 &pound;",
			"\u00A4 &curren;",
			"\u00A5 &yen;",
			"\u00A6 &brvbar;",
			"\u00A7 &sect;",
			"\u00A8 &Dot; &die; &DoubleDot; &uml;",
			"\u00A9 &copy; &COPY;",
			"\u00AA &ordf;",
			"\u00AB &laquo;",
			"\u00AC &not;",
			"\u00AD &shy;",
			"\u00AE &reg; &circledR; &REG;",
			"\u00AF &macr; &strns;",
			"\u00B0 &deg;",
			"\u00B1 &plusmn; &pm; &PlusMinus;",
			"\u00B2 &sup2;",
			"\u00B3 &sup3;",
			"\u00B4 &acute; &DiacriticalAcute;",
			"\u00B5 &micro;",
			"\u00B6 &para;",
			"\u00B7 &middot; &centerdot; &CenterDot;",
			"\u00B8 &cedil; &Cedilla;",
			"\u00B9 &sup1;",
			"\u00BA &ordm;",
			"\u00BB &raquo;",
			"\u00BC &frac14;",
			"\u00BD &frac12; &half;",
			"\u00BE &frac34;",
			"\u00BF &iquest;",
			"\u00C0 &Agrave;",
			"\u00C1 &Aacute;",
			"\u00C2 &Acirc;",
			"\u00C3 &Atilde;",
			"\u00C4 &Auml;",
			"\u00C5 &Aring; &angst;",
			"\u00C6 &AElig;",
			"\u00C7 &Ccedil;",
			"\u00C8 &Egrave;",
			"\u00C9 &Eacute;",
			"\u00CA &Ecirc;",
			"\u00CB &Euml;",
			"\u00CC &Igrave;",
			"\u00CD &Iacute;",
			"\u00CE &Icirc;",
			"\u00CF &Iuml;",
			"\u00D0 &ETH;",
			"\u00D1 &Ntilde;",
			"\u00D2 &Ograve;",
			"\u00D3 &Oacute;",
			"\u00D4 &Ocirc;",
			"\u00D5 &Otilde;",
			"\u00D6 &Ouml;",
			"\u00D7 &times;",
			"\u00D8 &Oslash;",
			"\u00D9 &Ugrave;",
			"\u00DA &Uacute;",
			"\u00DB &Ucirc;",
			"\u00DC &Uuml;",
			"\u00DD &Yacute;",
			"\u00DE &THORN;",
			"\u00DF &szlig;",
			"\u00E0 &agrave;",
			"\u00E1 &aacute;",
			"\u00E2 &acirc;",
			"\u00E3 &atilde;",
			"\u00E4 &auml;",
			"\u00E5 &aring;",
			"\u00E6 &aelig;",
			"\u00E7 &ccedil;",
			"\u00E8 &egrave;",
			"\u00E9 &eacute;",
			"\u00EA &ecirc;",
			"\u00EB &euml;",
			"\u00EC &igrave;",
			"\u00ED &iacute;",
			"\u00EE &icirc;",
			"\u00EF &iuml;",
			"\u00F0 &eth;",
			"\u00F1 &ntilde;",
			"\u00F2 &ograve;",
			"\u00F3 &oacute;",
			"\u00F4 &ocirc;",
			"\u00F5 &otilde;",
			"\u00F6 &ouml;",
			"\u00F7 &divide; &div;",
			"\u00F8 &oslash;",
			"\u00F9 &ugrave;",
			"\u00FA &uacute;",
			"\u00FB &ucirc;",
			"\u00FC &uuml;",
			"\u00FD &yacute;",
			"\u00FE &thorn;",
			"\u00FF &yuml;",
			"\u0100 &Amacr;",
			"\u0101 &amacr;",
			"\u0102 &Abreve;",
			"\u0103 &abreve;",
			"\u0104 &Aogon;",
			"\u0105 &aogon;",
			"\u0106 &Cacute;",
			"\u0107 &cacute;",
			"\u0108 &Ccirc;",
			"\u0109 &ccirc;",
			"\u010A &Cdot;",
			"\u010B &cdot;",
			"\u010C &Ccaron;",
			"\u010D &ccaron;",
			"\u010E &Dcaron;",
			"\u010F &dcaron;",
			"\u0110 &Dstrok;",
			"\u0111 &dstrok;",
			"\u0112 &Emacr;",
			"\u0113 &emacr;",
			"\u0116 &Edot;",
			"\u0117 &edot;",
			"\u0118 &Eogon;",
			"\u0119 &eogon;",
			"\u011A &Ecaron;",
			"\u011B &ecaron;",
			"\u011C &Gcirc;",
			"\u011D &gcirc;",
			"\u011E &Gbreve;",
			"\u011F &gbreve;",
			"\u0120 &Gdot;",
			"\u0121 &gdot;",
			"\u0122 &Gcedil;",
			"\u0124 &Hcirc;",
			"\u0125 &hcirc;",
			"\u0126 &Hstrok;",
			"\u0127 &hstrok;",
			"\u0128 &Itilde;",
			"\u0129 &itilde;",
			"\u012A &Imacr;",
			"\u012B &imacr;",
			"\u012E &Iogon;",
			"\u012F &iogon;",
			"\u0130 &Idot;",
			"\u0131 &imath; &inodot;",
			"\u0132 &IJlig;",
			"\u0133 &ijlig;",
			"\u0134 &Jcirc;",
			"\u0135 &jcirc;",
			"\u0136 &Kcedil;",
			"\u0137 &kcedil;",
			"\u0138 &kgreen;",
			"\u0139 &Lacute;",
			"\u013A &lacute;",
			"\u013B &Lcedil;",
			"\u013C &lcedil;",
			"\u013D &Lcaron;",
			"\u013E &lcaron;",
			"\u013F &Lmidot;",
			"\u0140 &lmidot;",
			"\u0141 &Lstrok;",
			"\u0142 &lstrok;",
			"\u0143 &Nacute;",
			"\u0144 &nacute;",
			"\u0145 &Ncedil;",
			"\u0146 &ncedil;",
			"\u0147 &Ncaron;",
			"\u0148 &ncaron;",
			"\u0149 &napos;",
			"\u014A &ENG;",
			"\u014B &eng;",
			"\u014C &Omacr;",
			"\u014D &omacr;",
			"\u0150 &Odblac;",
			"\u0151 &odblac;",
			"\u0152 &OElig;",
			"\u0153 &oelig;",
			"\u0154 &Racute;",
			"\u0155 &racute;",
			"\u0156 &Rcedil;",
			"\u0157 &rcedil;",
			"\u0158 &Rcaron;",
			"\u0159 &rcaron;",
			"\u015A &Sacute;",
			"\u015B &sacute;",
			"\u015C &Scirc;",
			"\u015D &scirc;",
			"\u015E &Scedil;",
			"\u015F &scedil;",
			"\u0160 &Scaron;",
			"\u0161 &scaron;",
			"\u0162 &Tcedil;",
			"\u0163 &tcedil;",
			"\u0164 &Tcaron;",
			"\u0165 &tcaron;",
			"\u0166 &Tstrok;",
			"\u0167 &tstrok;",
			"\u0168 &Utilde;",
			"\u0169 &utilde;",
			"\u016A &Umacr;",
			"\u016B &umacr;",
			"\u016C &Ubreve;",
			"\u016D &ubreve;",
			"\u016E &Uring;",
			"\u016F &uring;",
			"\u0170 &Udblac;",
			"\u0171 &udblac;",
			"\u0172 &Uogon;",
			"\u0173 &uogon;",
			"\u0174 &Wcirc;",
			"\u0175 &wcirc;",
			"\u0176 &Ycirc;",
			"\u0177 &ycirc;",
			"\u0178 &Yuml;",
			"\u0179 &Zacute;",
			"\u017A &zacute;",
			"\u017B &Zdot;",
			"\u017C &zdot;",
			"\u017D &Zcaron;",
			"\u017E &zcaron;",
			"\u0192 &fnof;",
			"\u01B5 &imped;",
			"\u01F5 &gacute;",
			"\u0237 &jmath;",
			"\u02C6 &circ;",
			"\u02C7 &caron; &Hacek;",
			"\u02D8 &breve; &Breve;",
			"\u02D9 &dot; &DiacriticalDot;",
			"\u02DA &ring;",
			"\u02DB &ogon;",
			"\u02DC &tilde; &DiacriticalTilde;",
			"\u02DD &dblac; &DiacriticalDoubleAcute;",
			"\u0311 &DownBreve;",
			"\u0391 &Alpha;",
			"\u0392 &Beta;",
			"\u0393 &Gamma;",
			"\u0394 &Delta;",
			"\u0395 &Epsilon;",
			"\u0396 &Zeta;",
			"\u0397 &Eta;",
			"\u0398 &Theta;",
			"\u0399 &Iota;",
			"\u039A &Kappa;",
			"\u039B &Lambda;",
			"\u039C &Mu;",
			"\u039D &Nu;",
			"\u039E &Xi;",
			"\u039F &Omicron;",
			"\u03A0 &Pi;",
			"\u03A1 &Rho;",
			"\u03A3 &Sigma;",
			"\u03A4 &Tau;",
			"\u03A5 &Upsilon;",
			"\u03A6 &Phi;",
			"\u03A7 &Chi;",
			"\u03A8 &Psi;",
			"\u03A9 &Omega; &ohm;",
			"\u03B1 &alpha;",
			"\u03B2 &beta;",
			"\u03B3 &gamma;",
			"\u03B4 &delta;",
			"\u03B5 &epsi; &epsilon;",
			"\u03B6 &zeta;",
			"\u03B7 &eta;",
			"\u03B8 &theta;",
			"\u03B9 &iota;",
			"\u03BA &kappa;",
			"\u03BB &lambda;",
			"\u03BC &mu;",
			"\u03BD &nu;",
			"\u03BE &xi;",
			"\u03BF &omicron;",
			"\u03C0 &pi;",
			"\u03C1 &rho;",
			"\u03C2 &sigmav; &varsigma; &sigmaf;",
			"\u03C3 &sigma;",
			"\u03C4 &tau;",
			"\u03C5 &upsi; &upsilon;",
			"\u03C6 &phi;",
			"\u03C7 &chi;",
			"\u03C8 &psi;",
			"\u03C9 &omega;",
			"\u03D1 &thetav; &vartheta; &thetasym;",
			"\u03D2 &Upsi; &upsih;",
			"\u03D5 &straightphi; &phiv; &varphi;",
			"\u03D6 &piv; &varpi;",
			"\u03DC &Gammad;",
			"\u03DD &gammad; &digamma;",
			"\u03F0 &kappav; &varkappa;",
			"\u03F1 &rhov; &varrho;",
			"\u03F5 &epsiv; &varepsilon; &straightepsilon;",
			"\u03F6 &bepsi; &backepsilon;",
			"\u0401 &IOcy;",
			"\u0402 &DJcy;",
			"\u0403 &GJcy;",
			"\u0404 &Jukcy;",
			"\u0405 &DScy;",
			"\u0406 &Iukcy;",
			"\u0407 &YIcy;",
			"\u0408 &Jsercy;",
			"\u0409 &LJcy;",
			"\u040A &NJcy;",
			"\u040B &TSHcy;",
			"\u040C &KJcy;",
			"\u040E &Ubrcy;",
			"\u040F &DZcy;",
			"\u0410 &Acy;",
			"\u0411 &Bcy;",
			"\u0412 &Vcy;",
			"\u0413 &Gcy;",
			"\u0414 &Dcy;",
			"\u0415 &IEcy;",
			"\u0416 &ZHcy;",
			"\u0417 &Zcy;",
			"\u0418 &Icy;",
			"\u0419 &Jcy;",
			"\u041A &Kcy;",
			"\u041B &Lcy;",
			"\u041C &Mcy;",
			"\u041D &Ncy;",
			"\u041E &Ocy;",
			"\u041F &Pcy;",
			"\u0420 &Rcy;",
			"\u0421 &Scy;",
			"\u0422 &Tcy;",
			"\u0423 &Ucy;",
			"\u0424 &Fcy;",
			"\u0425 &KHcy;",
			"\u0426 &TScy;",
			"\u0427 &CHcy;",
			"\u0428 &SHcy;",
			"\u0429 &SHCHcy;",
			"\u042A &HARDcy;",
			"\u042B &Ycy;",
			"\u042C &SOFTcy;",
			"\u042D &Ecy;",
			"\u042E &YUcy;",
			"\u042F &YAcy;",
			"\u0430 &acy;",
			"\u0431 &bcy;",
			"\u0432 &vcy;",
			"\u0433 &gcy;",
			"\u0434 &dcy;",
			"\u0435 &iecy;",
			"\u0436 &zhcy;",
			"\u0437 &zcy;",
			"\u0438 &icy;",
			"\u0439 &jcy;",
			"\u043A &kcy;",
			"\u043B &lcy;",
			"\u043C &mcy;",
			"\u043D &ncy;",
			"\u043E &ocy;",
			"\u043F &pcy;",
			"\u0440 &rcy;",
			"\u0441 &scy;",
			"\u0442 &tcy;",
			"\u0443 &ucy;",
			"\u0444 &fcy;",
			"\u0445 &khcy;",
			"\u0446 &tscy;",
			"\u0447 &chcy;",
			"\u0448 &shcy;",
			"\u0449 &shchcy;",
			"\u044A &hardcy;",
			"\u044B &ycy;",
			"\u044C &softcy;",
			"\u044D &ecy;",
			"\u044E &yucy;",
			"\u044F &yacy;",
			"\u0451 &iocy;",
			"\u0452 &djcy;",
			"\u0453 &gjcy;",
			"\u0454 &jukcy;",
			"\u0455 &dscy;",
			"\u0456 &iukcy;",
			"\u0457 &yicy;",
			"\u0458 &jsercy;",
			"\u0459 &ljcy;",
			"\u045A &njcy;",
			"\u045B &tshcy;",
			"\u045C &kjcy;",
			"\u045E &ubrcy;",
			"\u045F &dzcy;",
			"\u2002 &ensp;",
			"\u2003 &emsp;",
			"\u2004 &emsp13;",
			"\u2005 &emsp14;",
			"\u2007 &numsp;",
			"\u2008 &puncsp;",
			//"\u2009 &thinsp; &ThinSpace;", // not escaping space
			//"\u200A &hairsp; &VeryThinSpace;", // not escaping space
			//"\u200B &ZeroWidthSpace; &NegativeVeryThinSpace; &NegativeThinSpace; &NegativeMediumSpace; &NegativeThickSpace;", // not escaping space
			"\u200C &zwnj;",
			"\u200D &zwj;",
			"\u200E &lrm;",
			"\u200F &rlm;",
			"\u2010 &hyphen; &dash;",
			"\u2013 &ndash;",
			"\u2014 &mdash;",
			"\u2015 &horbar;",
			"\u2016 &Verbar; &Vert;",
			"\u2018 &lsquo; &OpenCurlyQuote;",
			"\u2019 &rsquo; &rsquor; &CloseCurlyQuote;",
			"\u201A &sbquo; &lsquor;",
			"\u201C &ldquo; &OpenCurlyDoubleQuote;",
			"\u201D &rdquo; &rdquor; &CloseCurlyDoubleQuote;",
			"\u201E &bdquo; &ldquor;",
			"\u2020 &dagger;",
			"\u2021 &Dagger; &ddagger;",
			"\u2022 &bull; &bullet;",
			"\u2025 &nldr;",
			"\u2026 &hellip; &mldr;",
			"\u2030 &permil;",
			"\u2031 &pertenk;",
			"\u2032 &prime;",
			"\u2033 &Prime;",
			"\u2034 &tprime;",
			"\u2035 &bprime; &backprime;",
			"\u2039 &lsaquo;",
			"\u203A &rsaquo;",
			"\u203E &oline; &OverBar;",
			"\u2041 &caret;",
			"\u2043 &hybull;",
			"\u2044 &frasl;",
			"\u204F &bsemi;",
			"\u2057 &qprime;",
			//"\u205F &MediumSpace;", // not escaping space
			"\u2060 &NoBreak;",
			"\u2061 &ApplyFunction; &af;",
			"\u2062 &InvisibleTimes; &it;",
			"\u2063 &InvisibleComma; &ic;",
			"\u20AC &euro;",
			"\u20DB &tdot; &TripleDot;",
			"\u20DC &DotDot;",
			"\u2102 &Copf; &complexes;",
			"\u2105 &incare;",
			"\u210A &gscr;",
			"\u210B &hamilt; &HilbertSpace; &Hscr;",
			"\u210C &Hfr; &Poincareplane;",
			"\u210D &quaternions; &Hopf;",
			"\u210E &planckh;",
			"\u210F &planck; &hbar; &plankv; &hslash;",
			"\u2110 &Iscr; &imagline;",
			"\u2111 &image; &Im; &imagpart; &Ifr;",
			"\u2112 &Lscr; &lagran; &Laplacetrf;",
			"\u2113 &ell;",
			"\u2115 &Nopf; &naturals;",
			"\u2116 &numero;",
			"\u2117 &copysr;",
			"\u2118 &weierp; &wp;",
			"\u2119 &Popf; &primes;",
			"\u211A &rationals; &Qopf;",
			"\u211B &Rscr; &realine;",
			"\u211C &real; &Re; &realpart; &Rfr;",
			"\u211D &reals; &Ropf;",
			"\u211E &rx;",
			"\u2122 &trade; &TRADE;",
			"\u2124 &integers; &Zopf;",
			"\u2127 &mho;",
			"\u2128 &Zfr; &zeetrf;",
			"\u2129 &iiota;",
			"\u212C &bernou; &Bernoullis; &Bscr;",
			"\u212D &Cfr; &Cayleys;",
			"\u212F &escr;",
			"\u2130 &Escr; &expectation;",
			"\u2131 &Fscr; &Fouriertrf;",
			"\u2133 &phmmat; &Mellintrf; &Mscr;",
			"\u2134 &order; &orderof; &oscr;",
			"\u2135 &alefsym; &aleph;",
			"\u2136 &beth;",
			"\u2137 &gimel;",
			"\u2138 &daleth;",
			"\u2145 &CapitalDifferentialD; &DD;",
			"\u2146 &DifferentialD; &dd;",
			"\u2147 &ExponentialE; &exponentiale; &ee;",
			"\u2148 &ImaginaryI; &ii;",
			"\u2153 &frac13;",
			"\u2154 &frac23;",
			"\u2155 &frac15;",
			"\u2156 &frac25;",
			"\u2157 &frac35;",
			"\u2158 &frac45;",
			"\u2159 &frac16;",
			"\u215A &frac56;",
			"\u215B &frac18;",
			"\u215C &frac38;",
			"\u215D &frac58;",
			"\u215E &frac78;",
			"\u2190 &larr; &leftarrow; &LeftArrow; &slarr; &ShortLeftArrow;",
			"\u2191 &uarr; &uparrow; &UpArrow; &ShortUpArrow;",
			"\u2192 &rarr; &rightarrow; &RightArrow; &srarr; &ShortRightArrow;",
			"\u2193 &darr; &downarrow; &DownArrow; &ShortDownArrow;",
			"\u2194 &harr; &leftrightarrow; &LeftRightArrow;",
			"\u2195 &varr; &updownarrow; &UpDownArrow;",
			"\u2196 &nwarr; &UpperLeftArrow; &nwarrow;",
			"\u2197 &nearr; &UpperRightArrow; &nearrow;",
			"\u2198 &searr; &searrow; &LowerRightArrow;",
			"\u2199 &swarr; &swarrow; &LowerLeftArrow;",
			"\u219A &nlarr; &nleftarrow;",
			"\u219B &nrarr; &nrightarrow;",
			"\u219D &rarrw; &rightsquigarrow;",
			"\u219E &Larr; &twoheadleftarrow;",
			"\u219F &Uarr;",
			"\u21A0 &Rarr; &twoheadrightarrow;",
			"\u21A1 &Darr;",
			"\u21A2 &larrtl; &leftarrowtail;",
			"\u21A3 &rarrtl; &rightarrowtail;",
			"\u21A4 &LeftTeeArrow; &mapstoleft;",
			"\u21A5 &UpTeeArrow; &mapstoup;",
			"\u21A6 &map; &RightTeeArrow; &mapsto;",
			"\u21A7 &DownTeeArrow; &mapstodown;",
			"\u21A9 &larrhk; &hookleftarrow;",
			"\u21AA &rarrhk; &hookrightarrow;",
			"\u21AB &larrlp; &looparrowleft;",
			"\u21AC &rarrlp; &looparrowright;",
			"\u21AD &harrw; &leftrightsquigarrow;",
			"\u21AE &nharr; &nleftrightarrow;",
			"\u21B0 &lsh; &Lsh;",
			"\u21B1 &rsh; &Rsh;",
			"\u21B2 &ldsh;",
			"\u21B3 &rdsh;",
			"\u21B5 &crarr;",
			"\u21B6 &cularr; &curvearrowleft;",
			"\u21B7 &curarr; &curvearrowright;",
			"\u21BA &olarr; &circlearrowleft;",
			"\u21BB &orarr; &circlearrowright;",
			"\u21BC &lharu; &LeftVector; &leftharpoonup;",
			"\u21BD &lhard; &leftharpoondown; &DownLeftVector;",
			"\u21BE &uharr; &upharpoonright; &RightUpVector;",
			"\u21BF &uharl; &upharpoonleft; &LeftUpVector;",
			"\u21C0 &rharu; &RightVector; &rightharpoonup;",
			"\u21C1 &rhard; &rightharpoondown; &DownRightVector;",
			"\u21C2 &dharr; &RightDownVector; &downharpoonright;",
			"\u21C3 &dharl; &LeftDownVector; &downharpoonleft;",
			"\u21C4 &rlarr; &rightleftarrows; &RightArrowLeftArrow;",
			"\u21C5 &udarr; &UpArrowDownArrow;",
			"\u21C6 &lrarr; &leftrightarrows; &LeftArrowRightArrow;",
			"\u21C7 &llarr; &leftleftarrows;",
			"\u21C8 &uuarr; &upuparrows;",
			"\u21C9 &rrarr; &rightrightarrows;",
			"\u21CA &ddarr; &downdownarrows;",
			"\u21CB &lrhar; &ReverseEquilibrium; &leftrightharpoons;",
			"\u21CC &rlhar; &rightleftharpoons; &Equilibrium;",
			"\u21CD &nlArr; &nLeftarrow;",
			"\u21CE &nhArr; &nLeftrightarrow;",
			"\u21CF &nrArr; &nRightarrow;",
			"\u21D0 &lArr; &Leftarrow; &DoubleLeftArrow;",
			"\u21D1 &uArr; &Uparrow; &DoubleUpArrow;",
			"\u21D2 &rArr; &Rightarrow; &Implies; &DoubleRightArrow;",
			"\u21D3 &dArr; &Downarrow; &DoubleDownArrow;",
			"\u21D4 &hArr; &Leftrightarrow; &DoubleLeftRightArrow; &iff;",
			"\u21D5 &vArr; &Updownarrow; &DoubleUpDownArrow;",
			"\u21D6 &nwArr;",
			"\u21D7 &neArr;",
			"\u21D8 &seArr;",
			"\u21D9 &swArr;",
			"\u21DA &lAarr; &Lleftarrow;",
			"\u21DB &rAarr; &Rrightarrow;",
			"\u21DD &zigrarr;",
			"\u21E4 &larrb; &LeftArrowBar;",
			"\u21E5 &rarrb; &RightArrowBar;",
			"\u21F5 &duarr; &DownArrowUpArrow;",
			"\u21FD &loarr;",
			"\u21FE &roarr;",
			"\u21FF &hoarr;",
			"\u2200 &forall; &ForAll;",
			"\u2201 &comp; &complement;",
			"\u2202 &part; &PartialD;",
			"\u2203 &exist; &Exists;",
			"\u2204 &nexist; &NotExists; &nexists;",
			"\u2205 &empty; &emptyset; &emptyv; &varnothing;",
			"\u2207 &nabla; &Del;",
			"\u2208 &isin; &isinv; &Element; &in;",
			"\u2209 &notin; &NotElement; &notinva;",
			"\u220B &niv; &ReverseElement; &ni; &SuchThat;",
			"\u220C &notni; &notniva; &NotReverseElement;",
			"\u220F &prod; &Product;",
			"\u2210 &coprod; &Coproduct;",
			"\u2211 &sum; &Sum;",
			"\u2212 &minus;",
			"\u2213 &mnplus; &mp; &MinusPlus;",
			"\u2214 &plusdo; &dotplus;",
			"\u2216 &setmn; &setminus; &Backslash; &ssetmn; &smallsetminus;",
			"\u2217 &lowast;",
			"\u2218 &compfn; &SmallCircle;",
			"\u221A &radic; &Sqrt;",
			"\u221D &prop; &propto; &Proportional; &vprop; &varpropto;",
			"\u221E &infin;",
			"\u221F &angrt;",
			"\u2220 &ang; &angle;",
			"\u2221 &angmsd; &measuredangle;",
			"\u2222 &angsph;",
			"\u2223 &mid; &VerticalBar; &smid; &shortmid;",
			"\u2224 &nmid; &NotVerticalBar; &nsmid; &nshortmid;",
			"\u2225 &par; &parallel; &DoubleVerticalBar; &spar; &shortparallel;",
			"\u2226 &npar; &nparallel; &NotDoubleVerticalBar; &nspar; &nshortparallel;",
			"\u2227 &and; &wedge;",
			"\u2228 &or; &vee;",
			"\u2229 &cap;",
			"\u222A &cup;",
			"\u222B &int; &Integral;",
			"\u222C &Int;",
			"\u222D &tint; &iiint;",
			"\u222E &conint; &oint; &ContourIntegral;",
			"\u222F &Conint; &DoubleContourIntegral;",
			"\u2230 &Cconint;",
			"\u2231 &cwint;",
			"\u2232 &cwconint; &ClockwiseContourIntegral;",
			"\u2233 &awconint; &CounterClockwiseContourIntegral;",
			"\u2234 &there4; &therefore; &Therefore;",
			"\u2235 &becaus; &because; &Because;",
			"\u2236 &ratio;",
			"\u2237 &Colon; &Proportion;",
			"\u2238 &minusd; &dotminus;",
			"\u223A &mDDot;",
			"\u223B &homtht;",
			"\u223C &sim; &Tilde; &thksim; &thicksim;",
			"\u223D &bsim; &backsim;",
			"\u223E &ac; &mstpos;",
			"\u223F &acd;",
			"\u2240 &wreath; &VerticalTilde; &wr;",
			"\u2241 &nsim; &NotTilde;",
			"\u2242 &esim; &EqualTilde; &eqsim;",
			"\u2243 &sime; &TildeEqual; &simeq;",
			"\u2244 &nsime; &nsimeq; &NotTildeEqual;",
			"\u2245 &cong; &TildeFullEqual;",
			"\u2246 &simne;",
			"\u2247 &ncong; &NotTildeFullEqual;",
			"\u2248 &asymp; &ap; &TildeTilde; &approx; &thkap; &thickapprox;",
			"\u2249 &nap; &NotTildeTilde; &napprox;",
			"\u224A &ape; &approxeq;",
			"\u224B &apid;",
			"\u224C &bcong; &backcong;",
			"\u224D &asympeq; &CupCap;",
			"\u224E &bump; &HumpDownHump; &Bumpeq;",
			"\u224F &bumpe; &HumpEqual; &bumpeq;",
			"\u2250 &esdot; &DotEqual; &doteq;",
			"\u2251 &eDot; &doteqdot;",
			"\u2252 &efDot; &fallingdotseq;",
			"\u2253 &erDot; &risingdotseq;",
			"\u2254 &colone; &coloneq; &Assign;",
			"\u2255 &ecolon; &eqcolon;",
			"\u2256 &ecir; &eqcirc;",
			"\u2257 &cire; &circeq;",
			"\u2259 &wedgeq;",
			"\u225A &veeeq;",
			"\u225C &trie; &triangleq;",
			"\u225F &equest; &questeq;",
			"\u2260 &ne; &NotEqual;",
			"\u2261 &equiv; &Congruent;",
			"\u2262 &nequiv; &NotCongruent;",
			"\u2264 &le; &leq;",
			"\u2265 &ge; &GreaterEqual; &geq;",
			"\u2266 &lE; &LessFullEqual; &leqq;",
			"\u2267 &gE; &GreaterFullEqual; &geqq;",
			"\u2268 &lnE; &lneqq;",
			"\u2269 &gnE; &gneqq;",
			"\u226A &Lt; &NestedLessLess; &ll;",
			"\u226B &Gt; &NestedGreaterGreater; &gg;",
			"\u226C &twixt; &between;",
			"\u226D &NotCupCap;",
			"\u226E &nlt; &NotLess; &nless;",
			"\u226F &ngt; &NotGreater; &ngtr;",
			"\u2270 &nle; &NotLessEqual; &nleq;",
			"\u2271 &nge; &NotGreaterEqual; &ngeq;",
			"\u2272 &lsim; &LessTilde; &lesssim;",
			"\u2273 &gsim; &gtrsim; &GreaterTilde;",
			"\u2274 &nlsim; &NotLessTilde;",
			"\u2275 &ngsim; &NotGreaterTilde;",
			"\u2276 &lg; &lessgtr; &LessGreater;",
			"\u2277 &gl; &gtrless; &GreaterLess;",
			"\u2278 &ntlg; &NotLessGreater;",
			"\u2279 &ntgl; &NotGreaterLess;",
			"\u227A &pr; &Precedes; &prec;",
			"\u227B &sc; &Succeeds; &succ;",
			"\u227C &prcue; &PrecedesSlantEqual; &preccurlyeq;",
			"\u227D &sccue; &SucceedsSlantEqual; &succcurlyeq;",
			"\u227E &prsim; &precsim; &PrecedesTilde;",
			"\u227F &scsim; &succsim; &SucceedsTilde;",
			"\u2280 &npr; &nprec; &NotPrecedes;",
			"\u2281 &nsc; &nsucc; &NotSucceeds;",
			"\u2282 &sub; &subset;",
			"\u2283 &sup; &supset; &Superset;",
			"\u2284 &nsub;",
			"\u2285 &nsup;",
			"\u2286 &sube; &SubsetEqual; &subseteq;",
			"\u2287 &supe; &supseteq; &SupersetEqual;",
			"\u2288 &nsube; &nsubseteq; &NotSubsetEqual;",
			"\u2289 &nsupe; &nsupseteq; &NotSupersetEqual;",
			"\u228A &subne; &subsetneq;",
			"\u228B &supne; &supsetneq;",
			"\u228D &cupdot;",
			"\u228E &uplus; &UnionPlus;",
			"\u228F &sqsub; &SquareSubset; &sqsubset;",
			"\u2290 &sqsup; &SquareSuperset; &sqsupset;",
			"\u2291 &sqsube; &SquareSubsetEqual; &sqsubseteq;",
			"\u2292 &sqsupe; &SquareSupersetEqual; &sqsupseteq;",
			"\u2293 &sqcap; &SquareIntersection;",
			"\u2294 &sqcup; &SquareUnion;",
			"\u2295 &oplus; &CirclePlus;",
			"\u2296 &ominus; &CircleMinus;",
			"\u2297 &otimes; &CircleTimes;",
			"\u2298 &osol;",
			"\u2299 &odot; &CircleDot;",
			"\u229A &ocir; &circledcirc;",
			"\u229B &oast; &circledast;",
			"\u229D &odash; &circleddash;",
			"\u229E &plusb; &boxplus;",
			"\u229F &minusb; &boxminus;",
			"\u22A0 &timesb; &boxtimes;",
			"\u22A1 &sdotb; &dotsquare;",
			"\u22A2 &vdash; &RightTee;",
			"\u22A3 &dashv; &LeftTee;",
			"\u22A4 &top; &DownTee;",
			"\u22A5 &bottom; &bot; &perp; &UpTee;",
			"\u22A7 &models;",
			"\u22A8 &vDash; &DoubleRightTee;",
			"\u22A9 &Vdash;",
			"\u22AA &Vvdash;",
			"\u22AB &VDash;",
			"\u22AC &nvdash;",
			"\u22AD &nvDash;",
			"\u22AE &nVdash;",
			"\u22AF &nVDash;",
			"\u22B0 &prurel;",
			"\u22B2 &vltri; &vartriangleleft; &LeftTriangle;",
			"\u22B3 &vrtri; &vartriangleright; &RightTriangle;",
			"\u22B4 &ltrie; &trianglelefteq; &LeftTriangleEqual;",
			"\u22B5 &rtrie; &trianglerighteq; &RightTriangleEqual;",
			"\u22B6 &origof;",
			"\u22B7 &imof;",
			"\u22B8 &mumap; &multimap;",
			"\u22B9 &hercon;",
			"\u22BA &intcal; &intercal;",
			"\u22BB &veebar;",
			"\u22BD &barvee;",
			"\u22BE &angrtvb;",
			"\u22BF &lrtri;",
			"\u22C0 &xwedge; &Wedge; &bigwedge;",
			"\u22C1 &xvee; &Vee; &bigvee;",
			"\u22C2 &xcap; &Intersection; &bigcap;",
			"\u22C3 &xcup; &Union; &bigcup;",
			"\u22C4 &diam; &diamond; &Diamond;",
			"\u22C5 &sdot;",
			"\u22C6 &sstarf; &Star;",
			"\u22C7 &divonx; &divideontimes;",
			"\u22C8 &bowtie;",
			"\u22C9 &ltimes;",
			"\u22CA &rtimes;",
			"\u22CB &lthree; &leftthreetimes;",
			"\u22CC &rthree; &rightthreetimes;",
			"\u22CD &bsime; &backsimeq;",
			"\u22CE &cuvee; &curlyvee;",
			"\u22CF &cuwed; &curlywedge;",
			"\u22D0 &Sub; &Subset;",
			"\u22D1 &Sup; &Supset;",
			"\u22D2 &Cap;",
			"\u22D3 &Cup;",
			"\u22D4 &fork; &pitchfork;",
			"\u22D5 &epar;",
			"\u22D6 &ltdot; &lessdot;",
			"\u22D7 &gtdot; &gtrdot;",
			"\u22D8 &Ll;",
			"\u22D9 &Gg; &ggg;",
			"\u22DA &leg; &LessEqualGreater; &lesseqgtr;",
			"\u22DB &gel; &gtreqless; &GreaterEqualLess;",
			"\u22DE &cuepr; &curlyeqprec;",
			"\u22DF &cuesc; &curlyeqsucc;",
			"\u22E0 &nprcue; &NotPrecedesSlantEqual;",
			"\u22E1 &nsccue; &NotSucceedsSlantEqual;",
			"\u22E2 &nsqsube; &NotSquareSubsetEqual;",
			"\u22E3 &nsqsupe; &NotSquareSupersetEqual;",
			"\u22E6 &lnsim;",
			"\u22E7 &gnsim;",
			"\u22E8 &prnsim; &precnsim;",
			"\u22E9 &scnsim; &succnsim;",
			"\u22EA &nltri; &ntriangleleft; &NotLeftTriangle;",
			"\u22EB &nrtri; &ntriangleright; &NotRightTriangle;",
			"\u22EC &nltrie; &ntrianglelefteq; &NotLeftTriangleEqual;",
			"\u22ED &nrtrie; &ntrianglerighteq; &NotRightTriangleEqual;",
			"\u22EE &vellip;",
			"\u22EF &ctdot;",
			"\u22F0 &utdot;",
			"\u22F1 &dtdot;",
			"\u22F2 &disin;",
			"\u22F3 &isinsv;",
			"\u22F4 &isins;",
			"\u22F5 &isindot;",
			"\u22F6 &notinvc;",
			"\u22F7 &notinvb;",
			"\u22F9 &isinE;",
			"\u22FA &nisd;",
			"\u22FB &xnis;",
			"\u22FC &nis;",
			"\u22FD &notnivc;",
			"\u22FE &notnivb;",
			"\u2305 &barwed; &barwedge;",
			"\u2306 &Barwed; &doublebarwedge;",
			"\u2308 &lceil; &LeftCeiling;",
			"\u2309 &rceil; &RightCeiling;",
			"\u230A &lfloor; &LeftFloor;",
			"\u230B &rfloor; &RightFloor;",
			"\u230C &drcrop;",
			"\u230D &dlcrop;",
			"\u230E &urcrop;",
			"\u230F &ulcrop;",
			"\u2310 &bnot;",
			"\u2312 &profline;",
			"\u2313 &profsurf;",
			"\u2315 &telrec;",
			"\u2316 &target;",
			"\u231C &ulcorn; &ulcorner;",
			"\u231D &urcorn; &urcorner;",
			"\u231E &dlcorn; &llcorner;",
			"\u231F &drcorn; &lrcorner;",
			"\u2322 &frown; &sfrown;",
			"\u2323 &smile; &ssmile;",
			"\u232D &cylcty;",
			"\u232E &profalar;",
			"\u2336 &topbot;",
			"\u233D &ovbar;",
			"\u233F &solbar;",
			"\u237C &angzarr;",
			"\u23B0 &lmoust; &lmoustache;",
			"\u23B1 &rmoust; &rmoustache;",
			"\u23B4 &tbrk; &OverBracket;",
			"\u23B5 &bbrk; &UnderBracket;",
			"\u23B6 &bbrktbrk;",
			"\u23DC &OverParenthesis;",
			"\u23DD &UnderParenthesis;",
			"\u23DE &OverBrace;",
			"\u23DF &UnderBrace;",
			"\u23E2 &trpezium;",
			"\u23E7 &elinters;",
			"\u2423 &blank;",
			"\u24C8 &oS; &circledS;",
			"\u2500 &boxh; &HorizontalLine;",
			"\u2502 &boxv;",
			"\u250C &boxdr;",
			"\u2510 &boxdl;",
			"\u2514 &boxur;",
			"\u2518 &boxul;",
			"\u251C &boxvr;",
			"\u2524 &boxvl;",
			"\u252C &boxhd;",
			"\u2534 &boxhu;",
			"\u253C &boxvh;",
			"\u2550 &boxH;",
			"\u2551 &boxV;",
			"\u2552 &boxdR;",
			"\u2553 &boxDr;",
			"\u2554 &boxDR;",
			"\u2555 &boxdL;",
			"\u2556 &boxDl;",
			"\u2557 &boxDL;",
			"\u2558 &boxuR;",
			"\u2559 &boxUr;",
			"\u255A &boxUR;",
			"\u255B &boxuL;",
			"\u255C &boxUl;",
			"\u255D &boxUL;",
			"\u255E &boxvR;",
			"\u255F &boxVr;",
			"\u2560 &boxVR;",
			"\u2561 &boxvL;",
			"\u2562 &boxVl;",
			"\u2563 &boxVL;",
			"\u2564 &boxHd;",
			"\u2565 &boxhD;",
			"\u2566 &boxHD;",
			"\u2567 &boxHu;",
			"\u2568 &boxhU;",
			"\u2569 &boxHU;",
			"\u256A &boxvH;",
			"\u256B &boxVh;",
			"\u256C &boxVH;",
			"\u2580 &uhblk;",
			"\u2584 &lhblk;",
			"\u2588 &block;",
			"\u2591 &blk14;",
			"\u2592 &blk12;",
			"\u2593 &blk34;",
			"\u25A1 &squ; &square; &Square;",
			"\u25AA &squf; &squarf; &blacksquare; &FilledVerySmallSquare;",
			"\u25AB &EmptyVerySmallSquare;",
			"\u25AD &rect;",
			"\u25AE &marker;",
			"\u25B1 &fltns;",
			"\u25B3 &xutri; &bigtriangleup;",
			"\u25B4 &utrif; &blacktriangle;",
			"\u25B5 &utri; &triangle;",
			"\u25B8 &rtrif; &blacktriangleright;",
			"\u25B9 &rtri; &triangleright;",
			"\u25BD &xdtri; &bigtriangledown;",
			"\u25BE &dtrif; &blacktriangledown;",
			"\u25BF &dtri; &triangledown;",
			"\u25C2 &ltrif; &blacktriangleleft;",
			"\u25C3 &ltri; &triangleleft;",
			"\u25CA &loz; &lozenge;",
			"\u25CB &cir;",
			"\u25EC &tridot;",
			"\u25EF &xcirc; &bigcirc;",
			"\u25F8 &ultri;",
			"\u25F9 &urtri;",
			"\u25FA &lltri;",
			"\u25FB &EmptySmallSquare;",
			"\u25FC &FilledSmallSquare;",
			"\u2605 &starf; &bigstar;",
			"\u2606 &star;",
			"\u260E &phone;",
			"\u2640 &female;",
			"\u2642 &male;",
			"\u2660 &spades; &spadesuit;",
			"\u2663 &clubs; &clubsuit;",
			"\u2665 &hearts; &heartsuit;",
			"\u2666 &diams; &diamondsuit;",
			"\u266A &sung;",
			"\u266D &flat;",
			"\u266E &natur; &natural;",
			"\u266F &sharp;",
			"\u2713 &check; &checkmark;",
			"\u2717 &cross;",
			"\u2720 &malt; &maltese;",
			"\u2736 &sext;",
			"\u2758 &VerticalSeparator;",
			"\u2772 &lbbrk;",
			"\u2773 &rbbrk;",
			"\u27C8 &bsolhsub;",
			"\u27C9 &suphsol;",
			"\u27E6 &lobrk; &LeftDoubleBracket;",
			"\u27E7 &robrk; &RightDoubleBracket;",
			"\u27E8 &lang; &LeftAngleBracket; &langle;",
			"\u27E9 &rang; &RightAngleBracket; &rangle;",
			"\u27EA &Lang;",
			"\u27EB &Rang;",
			"\u27EC &loang;",
			"\u27ED &roang;",
			"\u27F5 &xlarr; &longleftarrow; &LongLeftArrow;",
			"\u27F6 &xrarr; &longrightarrow; &LongRightArrow;",
			"\u27F7 &xharr; &longleftrightarrow; &LongLeftRightArrow;",
			"\u27F8 &xlArr; &Longleftarrow; &DoubleLongLeftArrow;",
			"\u27F9 &xrArr; &Longrightarrow; &DoubleLongRightArrow;",
			"\u27FA &xhArr; &Longleftrightarrow; &DoubleLongLeftRightArrow;",
			"\u27FC &xmap; &longmapsto;",
			"\u27FF &dzigrarr;",
			"\u2902 &nvlArr;",
			"\u2903 &nvrArr;",
			"\u2904 &nvHarr;",
			"\u2905 &Map;",
			"\u290C &lbarr;",
			"\u290D &rbarr; &bkarow;",
			"\u290E &lBarr;",
			"\u290F &rBarr; &dbkarow;",
			"\u2910 &RBarr; &drbkarow;",
			"\u2911 &DDotrahd;",
			"\u2912 &UpArrowBar;",
			"\u2913 &DownArrowBar;",
			"\u2916 &Rarrtl;",
			"\u2919 &latail;",
			"\u291A &ratail;",
			"\u291B &lAtail;",
			"\u291C &rAtail;",
			"\u291D &larrfs;",
			"\u291E &rarrfs;",
			"\u291F &larrbfs;",
			"\u2920 &rarrbfs;",
			"\u2923 &nwarhk;",
			"\u2924 &nearhk;",
			"\u2925 &searhk; &hksearow;",
			"\u2926 &swarhk; &hkswarow;",
			"\u2927 &nwnear;",
			"\u2928 &nesear; &toea;",
			"\u2929 &seswar; &tosa;",
			"\u292A &swnwar;",
			"\u2933 &rarrc;",
			"\u2935 &cudarrr;",
			"\u2936 &ldca;",
			"\u2937 &rdca;",
			"\u2938 &cudarrl;",
			"\u2939 &larrpl;",
			"\u293C &curarrm;",
			"\u293D &cularrp;",
			"\u2945 &rarrpl;",
			"\u2948 &harrcir;",
			"\u2949 &Uarrocir;",
			"\u294A &lurdshar;",
			"\u294B &ldrushar;",
			"\u294E &LeftRightVector;",
			"\u294F &RightUpDownVector;",
			"\u2950 &DownLeftRightVector;",
			"\u2951 &LeftUpDownVector;",
			"\u2952 &LeftVectorBar;",
			"\u2953 &RightVectorBar;",
			"\u2954 &RightUpVectorBar;",
			"\u2955 &RightDownVectorBar;",
			"\u2956 &DownLeftVectorBar;",
			"\u2957 &DownRightVectorBar;",
			"\u2958 &LeftUpVectorBar;",
			"\u2959 &LeftDownVectorBar;",
			"\u295A &LeftTeeVector;",
			"\u295B &RightTeeVector;",
			"\u295C &RightUpTeeVector;",
			"\u295D &RightDownTeeVector;",
			"\u295E &DownLeftTeeVector;",
			"\u295F &DownRightTeeVector;",
			"\u2960 &LeftUpTeeVector;",
			"\u2961 &LeftDownTeeVector;",
			"\u2962 &lHar;",
			"\u2963 &uHar;",
			"\u2964 &rHar;",
			"\u2965 &dHar;",
			"\u2966 &luruhar;",
			"\u2967 &ldrdhar;",
			"\u2968 &ruluhar;",
			"\u2969 &rdldhar;",
			"\u296A &lharul;",
			"\u296B &llhard;",
			"\u296C &rharul;",
			"\u296D &lrhard;",
			"\u296E &udhar; &UpEquilibrium;",
			"\u296F &duhar; &ReverseUpEquilibrium;",
			"\u2970 &RoundImplies;",
			"\u2971 &erarr;",
			"\u2972 &simrarr;",
			"\u2973 &larrsim;",
			"\u2974 &rarrsim;",
			"\u2975 &rarrap;",
			"\u2976 &ltlarr;",
			"\u2978 &gtrarr;",
			"\u2979 &subrarr;",
			"\u297B &suplarr;",
			"\u297C &lfisht;",
			"\u297D &rfisht;",
			"\u297E &ufisht;",
			"\u297F &dfisht;",
			"\u2985 &lopar;",
			"\u2986 &ropar;",
			"\u298B &lbrke;",
			"\u298C &rbrke;",
			"\u298D &lbrkslu;",
			"\u298E &rbrksld;",
			"\u298F &lbrksld;",
			"\u2990 &rbrkslu;",
			"\u2991 &langd;",
			"\u2992 &rangd;",
			"\u2993 &lparlt;",
			"\u2994 &rpargt;",
			"\u2995 &gtlPar;",
			"\u2996 &ltrPar;",
			"\u299A &vzigzag;",
			"\u299C &vangrt;",
			"\u299D &angrtvbd;",
			"\u29A4 &ange;",
			"\u29A5 &range;",
			"\u29A6 &dwangle;",
			"\u29A7 &uwangle;",
			"\u29A8 &angmsdaa;",
			"\u29A9 &angmsdab;",
			"\u29AA &angmsdac;",
			"\u29AB &angmsdad;",
			"\u29AC &angmsdae;",
			"\u29AD &angmsdaf;",
			"\u29AE &angmsdag;",
			"\u29AF &angmsdah;",
			"\u29B0 &bemptyv;",
			"\u29B1 &demptyv;",
			"\u29B2 &cemptyv;",
			"\u29B3 &raemptyv;",
			"\u29B4 &laemptyv;",
			"\u29B5 &ohbar;",
			"\u29B6 &omid;",
			"\u29B7 &opar;",
			"\u29B9 &operp;",
			"\u29BB &olcross;",
			"\u29BC &odsold;",
			"\u29BE &olcir;",
			"\u29BF &ofcir;",
			"\u29C0 &olt;",
			"\u29C1 &ogt;",
			"\u29C2 &cirscir;",
			"\u29C3 &cirE;",
			"\u29C4 &solb;",
			"\u29C5 &bsolb;",
			"\u29C9 &boxbox;",
			"\u29CD &trisb;",
			"\u29CE &rtriltri;",
			"\u29CF &LeftTriangleBar;",
			"\u29D0 &RightTriangleBar;",
			"\u29DC &iinfin;",
			"\u29DD &infintie;",
			"\u29DE &nvinfin;",
			"\u29E3 &eparsl;",
			"\u29E4 &smeparsl;",
			"\u29E5 &eqvparsl;",
			"\u29EB &lozf; &blacklozenge;",
			"\u29F4 &RuleDelayed;",
			"\u29F6 &dsol;",
			"\u2A00 &xodot; &bigodot;",
			"\u2A01 &xoplus; &bigoplus;",
			"\u2A02 &xotime; &bigotimes;",
			"\u2A04 &xuplus; &biguplus;",
			"\u2A06 &xsqcup; &bigsqcup;",
			"\u2A0C &qint; &iiiint;",
			"\u2A0D &fpartint;",
			"\u2A10 &cirfnint;",
			"\u2A11 &awint;",
			"\u2A12 &rppolint;",
			"\u2A13 &scpolint;",
			"\u2A14 &npolint;",
			"\u2A15 &pointint;",
			"\u2A16 &quatint;",
			"\u2A17 &intlarhk;",
			"\u2A22 &pluscir;",
			"\u2A23 &plusacir;",
			"\u2A24 &simplus;",
			"\u2A25 &plusdu;",
			"\u2A26 &plussim;",
			"\u2A27 &plustwo;",
			"\u2A29 &mcomma;",
			"\u2A2A &minusdu;",
			"\u2A2D &loplus;",
			"\u2A2E &roplus;",
			"\u2A2F &Cross;",
			"\u2A30 &timesd;",
			"\u2A31 &timesbar;",
			"\u2A33 &smashp;",
			"\u2A34 &lotimes;",
			"\u2A35 &rotimes;",
			"\u2A36 &otimesas;",
			"\u2A37 &Otimes;",
			"\u2A38 &odiv;",
			"\u2A39 &triplus;",
			"\u2A3A &triminus;",
			"\u2A3B &tritime;",
			"\u2A3C &iprod; &intprod;",
			"\u2A3F &amalg;",
			"\u2A40 &capdot;",
			"\u2A42 &ncup;",
			"\u2A43 &ncap;",
			"\u2A44 &capand;",
			"\u2A45 &cupor;",
			"\u2A46 &cupcap;",
			"\u2A47 &capcup;",
			"\u2A48 &cupbrcap;",
			"\u2A49 &capbrcup;",
			"\u2A4A &cupcup;",
			"\u2A4B &capcap;",
			"\u2A4C &ccups;",
			"\u2A4D &ccaps;",
			"\u2A50 &ccupssm;",
			"\u2A53 &And;",
			"\u2A54 &Or;",
			"\u2A55 &andand;",
			"\u2A56 &oror;",
			"\u2A57 &orslope;",
			"\u2A58 &andslope;",
			"\u2A5A &andv;",
			"\u2A5B &orv;",
			"\u2A5C &andd;",
			"\u2A5D &ord;",
			"\u2A5F &wedbar;",
			"\u2A66 &sdote;",
			"\u2A6A &simdot;",
			"\u2A6D &congdot;",
			"\u2A6E &easter;",
			"\u2A6F &apacir;",
			"\u2A70 &apE;",
			"\u2A71 &eplus;",
			"\u2A72 &pluse;",
			"\u2A73 &Esim;",
			"\u2A74 &Colone;",
			"\u2A75 &Equal;",
			"\u2A77 &eDDot; &ddotseq;",
			"\u2A78 &equivDD;",
			"\u2A79 &ltcir;",
			"\u2A7A &gtcir;",
			"\u2A7B &ltquest;",
			"\u2A7C &gtquest;",
			"\u2A7D &les; &LessSlantEqual; &leqslant;",
			"\u2A7E &ges; &GreaterSlantEqual; &geqslant;",
			"\u2A7F &lesdot;",
			"\u2A80 &gesdot;",
			"\u2A81 &lesdoto;",
			"\u2A82 &gesdoto;",
			"\u2A83 &lesdotor;",
			"\u2A84 &gesdotol;",
			"\u2A85 &lap; &lessapprox;",
			"\u2A86 &gap; &gtrapprox;",
			"\u2A87 &lne; &lneq;",
			"\u2A88 &gne; &gneq;",
			"\u2A89 &lnap; &lnapprox;",
			"\u2A8A &gnap; &gnapprox;",
			"\u2A8B &lEg; &lesseqqgtr;",
			"\u2A8C &gEl; &gtreqqless;",
			"\u2A8D &lsime;",
			"\u2A8E &gsime;",
			"\u2A8F &lsimg;",
			"\u2A90 &gsiml;",
			"\u2A91 &lgE;",
			"\u2A92 &glE;",
			"\u2A93 &lesges;",
			"\u2A94 &gesles;",
			"\u2A95 &els; &eqslantless;",
			"\u2A96 &egs; &eqslantgtr;",
			"\u2A97 &elsdot;",
			"\u2A98 &egsdot;",
			"\u2A99 &el;",
			"\u2A9A &eg;",
			"\u2A9D &siml;",
			"\u2A9E &simg;",
			"\u2A9F &simlE;",
			"\u2AA0 &simgE;",
			"\u2AA1 &LessLess;",
			"\u2AA2 &GreaterGreater;",
			"\u2AA4 &glj;",
			"\u2AA5 &gla;",
			"\u2AA6 &ltcc;",
			"\u2AA7 &gtcc;",
			"\u2AA8 &lescc;",
			"\u2AA9 &gescc;",
			"\u2AAA &smt;",
			"\u2AAB &lat;",
			"\u2AAC &smte;",
			"\u2AAD &late;",
			"\u2AAE &bumpE;",
			"\u2AAF &pre; &preceq; &PrecedesEqual;",
			"\u2AB0 &sce; &succeq; &SucceedsEqual;",
			"\u2AB3 &prE;",
			"\u2AB4 &scE;",
			"\u2AB5 &prnE; &precneqq;",
			"\u2AB6 &scnE; &succneqq;",
			"\u2AB7 &prap; &precapprox;",
			"\u2AB8 &scap; &succapprox;",
			"\u2AB9 &prnap; &precnapprox;",
			"\u2ABA &scnap; &succnapprox;",
			"\u2ABB &Pr;",
			"\u2ABC &Sc;",
			"\u2ABD &subdot;",
			"\u2ABE &supdot;",
			"\u2ABF &subplus;",
			"\u2AC0 &supplus;",
			"\u2AC1 &submult;",
			"\u2AC2 &supmult;",
			"\u2AC3 &subedot;",
			"\u2AC4 &supedot;",
			"\u2AC5 &subE; &subseteqq;",
			"\u2AC6 &supE; &supseteqq;",
			"\u2AC7 &subsim;",
			"\u2AC8 &supsim;",
			"\u2ACB &subnE; &subsetneqq;",
			"\u2ACC &supnE; &supsetneqq;",
			"\u2ACF &csub;",
			"\u2AD0 &csup;",
			"\u2AD1 &csube;",
			"\u2AD2 &csupe;",
			"\u2AD3 &subsup;",
			"\u2AD4 &supsub;",
			"\u2AD5 &subsub;",
			"\u2AD6 &supsup;",
			"\u2AD7 &suphsub;",
			"\u2AD8 &supdsub;",
			"\u2AD9 &forkv;",
			"\u2ADA &topfork;",
			"\u2ADB &mlcp;",
			"\u2AE4 &Dashv; &DoubleLeftTee;",
			"\u2AE6 &Vdashl;",
			"\u2AE7 &Barv;",
			"\u2AE8 &vBar;",
			"\u2AE9 &vBarv;",
			"\u2AEB &Vbar;",
			"\u2AEC &Not;",
			"\u2AED &bNot;",
			"\u2AEE &rnmid;",
			"\u2AEF &cirmid;",
			"\u2AF0 &midcir;",
			"\u2AF1 &topcir;",
			"\u2AF2 &nhpar;",
			"\u2AF3 &parsim;",
			"\u2AFD &parsl;",
			"\uFB00 &fflig;",
			"\uFB01 &filig;",
			"\uFB02 &fllig;",
			"\uFB03 &ffilig;",
			"\uFB04 &ffllig;",
		};
//	private static final String characterCodesString = "&nbsp;";
	private static final String[] characterDecodingsStrings = {
			" |&nbsp;|&NonBreakingSpace;",
			" |&thinsp;|&ThinSpace;",
			" |&hairsp;|&VeryThinSpace;",
			" |&ZeroWidthSpace;|&NegativeVeryThinSpace;|&NegativeThinSpace;|&NegativeMediumSpace;|&NegativeThickSpace;",
			" |&MediumSpace;",
		};
	
	//	data structures for specific data
	protected final HashMap tagTypeToParentTypesMappings = new HashMap();
	protected final Properties tagTypeTranslations = new Properties();
	protected final HashSet singularTagTypes = new HashSet();
	protected final HashSet subsequentEndTagsAllowedTypes = new HashSet();
	protected final HashSet whitespaceFreeValueAttributes = new HashSet();
	
	protected final Properties characterEncodings = new Properties();
	protected final Properties characterDecodings = new Properties();
	protected final HashSet characterCodes = new HashSet();
	
	protected final boolean isStrictXML = false;
	private int charLookahead = 6; // length of '&nbsp;'
	
	/**	Constructor
	 */
	public Html() {
		
		//	initialize tag type --> parent tag types mappings
		for (int ptm = 0; ptm < tagTypeToParentTypesMappingStrings.length; ptm++) {
			String parentTypeMapping = tagTypeToParentTypesMappingStrings[ptm].toLowerCase();
			int split = parentTypeMapping.lastIndexOf(":");
			if (split == -1)
				continue;
			if ((parentTypeMapping.length() - 2) <= split)
				continue;
			
			//	separate children and parents
			String typeStr = parentTypeMapping.substring(0, split);
			String parentTypeStr = parentTypeMapping.substring(split + ":".length());
			
			//	parse and store parents
			String[] parentTypes = IoTools.parseString(parentTypeStr, ";");
			HashSet parentTypeSet = new HashSet();
			for (int pt = 0; pt < parentTypes.length; pt++)
				parentTypeSet.add(parentTypes[pt]);
			
			//	parse child types and store mappings
			String[] types = IoTools.parseString(typeStr, ":");
			for (int t = 0; t < types.length; t++)
				this.tagTypeToParentTypesMappings.put(types[t], parentTypeSet);
		}
		
		//	initialize tag type translation
		for (int tt = 0; tt < tagTypeTranslationStrings.length; tt++) {
			String typeTranslation = tagTypeTranslationStrings[tt].toLowerCase();
			int split = typeTranslation.lastIndexOf(":");
			if (split == -1)
				continue;
			if ((typeTranslation.length() - 2) <= split)
				continue;
			
			//	separate children and parents
			String typeStr = typeTranslation.substring(0, typeTranslation.lastIndexOf(":"));
			String translation = typeTranslation.substring(typeTranslation.lastIndexOf(":") + 1);
			
			//	parse child types and store mappings
			String[] types = IoTools.parseString(typeStr, ":");
			for (int t = 0; t < types.length; t++)
				this.tagTypeTranslations.put(types[t], translation);
		}
		
		//	initialize singular tag types
		String[] types = IoTools.parseString(singularTagTypesString.toLowerCase(), ";");
		for (int t = 0; t < types.length; t++)
			this.singularTagTypes.add(types[t]);
		
		//	initialize tag types that's end tags may be subsequent
		types = IoTools.parseString(subsequentEndTagsAllowedTypesString.toLowerCase(), ";");
		for (int t = 0; t < types.length; t++)
			this.subsequentEndTagsAllowedTypes.add(types[t]);
		
		//	initialize parameters that's values may not contain whitespaces, even if quoted
		types = IoTools.parseString(whitespaceFreeValueAttributesString.toLowerCase(), ";");
		for (int a = 0; a < types.length; a++)
			this.whitespaceFreeValueAttributes.add(types[a]);
		
		//	initialize character encoding and decoding
		for (int c = 0; c < characterEncodingsStrings.length; c++) {
//			String ces = characterEncodingsStrings[i];
//			this.characterCodes.add(ces.substring(2));
//			this.characterEncodings.setProperty(ces.substring(0, 1), ces.substring(2));
//			this.characterDecodings.setProperty(ces.substring(2), ces.substring(0, 1));
//			this.charLookahead = Math.max(this.charLookahead, (ces.length() - 2));
			String[] ces = characterEncodingsStrings[c].split("\\s+");
			if (ces.length < 2)
				continue;
			this.characterEncodings.setProperty(ces[0], ces[1]);
			for (int cc = 1; cc < ces.length; cc++) {
				this.characterCodes.add(ces[cc]);
				this.characterDecodings.setProperty(ces[cc], ces[0]);
				this.charLookahead = Math.max(this.charLookahead, ces[cc].length());
			}
		}
//		this.characterDecodings.setProperty("&nbsp;", " ");
		for (int c = 0; c < characterDecodingsStrings.length; c++) {
			String[] cds = characterDecodingsStrings[c].split("\\|");
			if (cds.length < 2)
				continue;
			for (int cd = 1; cd < cds.length; cd++) {
				this.characterCodes.add(cds[cd]);
				this.characterDecodings.setProperty(cds[cd], cds[0]);
				this.charLookahead = Math.max(this.charLookahead, cds[cd].length());
			}
		}
		
		//	also observe awaiting end tags
		this.charLookahead = Math.max(this.charLookahead, (2 + "textarea".length() + 1));
//		
//		//	initialize character code list
//		types = IoTools.parseString(characterCodesString.toLowerCase(), ",");
//		for (int i = 0; i < types.length; i++)
//			this.characterCodes.add(types[i]);
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#isStrictXML()
	 */
	public boolean isStrictXML() {
		return this.isStrictXML;
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#correctCharEncoding()
	 */
	public boolean correctCharEncoding() {
		return true;
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#waitForEndTag(java.lang.String)
	 */
	public boolean waitForEndTag(String tag) {
		String type = this.getType(tag);
		return ("script".equalsIgnoreCase(type) || "style".equalsIgnoreCase(type) || "pre".equalsIgnoreCase(type) || "textarea".equalsIgnoreCase(type));
	}
	
	/**	@return	the number of tokens the verifyTokenSequence method of this Grammar needs to look ahead on verification
	 */
	public int getTokenLookahead() {
		return 2;
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#isSingularTag(java.lang.String)
	 */
	public boolean isSingularTag(String tag) {
		String t = this.getType(tag).toLowerCase();
		return (this.isSingularTagType(t) || super.isSingularTag(tag));
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#isSingularTagType(java.lang.String)
	 */
	public boolean isSingularTagType(String type) {
		String t = this.getType(type).toLowerCase();
		return this.singularTagTypes.contains(t);
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#valueMayContainWhitespace(String, String)
	 */
	public boolean valueMayContainWhitespace(String tag, String attribute) {
		return ((attribute == null) || !this.whitespaceFreeValueAttributes.contains(attribute.toLowerCase()));
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#translateTag(java.lang.String)
	 */
	public String translateTag(String tag) {
		String t = this.tagTypeTranslations.getProperty(this.getType(tag));
		return ((t != null) ? t : tag);
	}
//	
//	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#getParentTags(java.lang.String)
//	 */
//	public HashSet getParentTags(String tag) {
//		HashSet parents = new HashSet();
//		Object o = this.tagTypeToParentTypesMappings.get(tag);
//		if ((o != null) && (o instanceof HashSet))
//			parents.addAll((HashSet) o);
//		return parents;
//	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#canBeChildOf(java.lang.String, java.lang.String)
	 */
	public boolean canBeChildOf(String child, String parent) {
		Object o = this.tagTypeToParentTypesMappings.get(child.toLowerCase());
		if ((o != null) && (o instanceof HashSet)) {
			HashSet pSet = ((HashSet) o);
			return pSet.contains(parent.toLowerCase());
		} 
		else return true;
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#getCharLookahead()
	 */
	public int getCharLookahead() {
		return this.charLookahead;
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#getCharCode(char)
	 */
	public String getCharCode(char ch) {
		//	we need this hack as IE does not know &apos;, but we need to decode it anyways
//		return ((c == '\'') ? "'" : this.characterEncodings.getProperty(("" + c), ("" + c)));
		if (ch <= ' ')
			return " ";
		else if (ch == '<')
			return "&lt;";
		else if (ch == '>')
			return "&gt;";
		else if (ch == '"')
			return "&quot;";
		else if (ch == '&')
			return "&amp;";
		String chStr = Character.toString(ch);
		if (ch < 127)
			return chStr;
		else return this.characterEncodings.getProperty(chStr, chStr);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#getPlainChar(java.lang.String)
	 */
	public char getPlainChar(String code) {
		String ch = this.characterDecodings.getProperty(code);
		return ((ch == null) ? super.getPlainChar(code) : ch.charAt(0));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar#isCharCode(java.lang.String)
	 */
	public boolean isCharCode(String code) {
		return (this.characterDecodings.containsKey(code) || super.isCharCode(code));
	}
	
	/**
	 * This implementation escapes a string for HTML - in particular, it escapes
	 * &lt;, &gt;, and &quot;, but not &amp; if it occurs in a string without
	 * whitespace and after a question mark, in order not to destroy URLs that
	 * include a query. In addition, &amp; is not escaped if it is the start of
	 * an HTML character code.
	 * @param string the string to escape
	 * @return the escaped string
	 * @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#escape(java.lang.String)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar#escape(java.lang.String)
	 */
	public String escape(String string) {
		StringBuffer escapedString = new StringBuffer();
		boolean hadWhitespace = false;
		boolean hadQuestionMark = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
			if (ch == '?')
				hadQuestionMark = true;
			else if (this.isWhitespace(ch))
				hadWhitespace = true;
			if (ch == '<')
				escapedString.append("&lt;");
			else if (ch == '>')
				escapedString.append("&gt;");
			else if (ch == '"')
				escapedString.append("&quot;");
//			else if (ch == '\'') // this does a lot more harm than good, as many applications don't understand the entity
//				escapedString.append("&apos;");
			else if (ch == '&') {
				int escapeEnd = string.indexOf(';', c);
				if ((escapeEnd != -1) && ((escapeEnd - c) <= this.charLookahead) && this.isCharCode(string.substring(c, (escapeEnd+1))))
					escapedString.append(ch);
				else if (!hadWhitespace && hadQuestionMark)
					escapedString.append(ch);
				else escapedString.append("&amp;");
			}
			else escapedString.append(ch);
		}
		return escapedString.toString();
	}
	
	/** @see de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar#ckeckTokenSequence(java.util.Vector)
	 */
	public void ckeckTokenSequence(Vector ts) {
		int index = 0;
		boolean inScript = false;
		boolean inStyle = false;
		Token lastPreservedPart = null;
		Token currentPart;
		StringBuffer collector = new StringBuffer();
		int collectorStart = -1;
		
		while (index < ts.size()) {
			currentPart = ((Token) ts.get(index));
			
			//	reassemble parsed scripts
			if (inScript) {
				
				//	end of script
				if (this.isEndTag(currentPart.value) && this.getType(currentPart.value).equalsIgnoreCase("script")) {
					inScript = false;
					
					//	store script content, surround it by comment marks if no already so
					if (collector.length() > 0) {
						String script = collector.toString().trim();
//						if (script.startsWith("<!--"))
//							script = "//" + script;
//						else if (!script.startsWith("//<!--"))
//							script = "//<!--\r\n" + script;
//						if (!script.endsWith("-->"))
//							script = script + "\r\n//-->";
						
						ts.insertElementAt(new Token(script, collectorStart), index);
						index++;
					}
					
					//	clear collector and store end tag
					collector.delete(0, collector.length());
					collectorStart = -1;
					lastPreservedPart = currentPart;
					index++;
				}
				
				//	there can be no script within a script
				else if (this.isTag(currentPart.value) && this.getType(currentPart.value).equalsIgnoreCase("script"))
					ts.removeElementAt(index);
				
				//	script continues
				else {
					if (collector.length() == 0)
						collectorStart = currentPart.start;
					collector.append(currentPart.value);
					ts.removeElementAt(index);
				}
			}
			
			//	reassemble parsed styles
			else if (inStyle) {
				
				//	end of style
				if (this.isEndTag(currentPart.value) && this.getType(currentPart.value).equalsIgnoreCase("style")) {
					inStyle = false;
					
					//	store style content, surround it by comment marks if no already so
					if (collector.length() > 0) {
						String style = collector.toString().trim();
//						if (!(style.startsWith("//") && style.substring(2).trim().startsWith("<!--")) && !style.startsWith("<!--"))
//							style = "<!--\n" + style;
//						if (!style.endsWith("-->"))
//							style = style + "\n//-->";
						
						ts.insertElementAt(new Token(style, collectorStart), index);
						index++;
					}
					
					//	clear collector and store end tag
					collector.delete(0, collector.length());
					collectorStart = -1;
					lastPreservedPart = currentPart;
					index++;
				}
				
				//	there can be no style within a style
				else if (this.isTag(currentPart.value) && this.getType(currentPart.value).equalsIgnoreCase("style"))
					ts.removeElementAt(index);
					
				//	style continues
				else {
					if (collector.length() == 0)
						collectorStart = currentPart.start;
					collector.append(currentPart.value);
					ts.removeElementAt(index);
				}
			}
			
			//	check subsequent equal end tags
			else if ((lastPreservedPart != null) && this.isEndTag(currentPart.value) && currentPart.value.equalsIgnoreCase(lastPreservedPart.value) && !this.subsequentEndTagsAllowedTypes.contains(this.getType(currentPart.value).toLowerCase()))
				ts.removeElementAt(index);
//			
//			//	repair badly marked comments
//			else if (currentPart.startsWith("<!-") && currentPart.endsWith("->")) {
//				if (!currentPart.startsWith("<!--"))
//					currentPart = "<!--" + currentPart.substring("<!-".length());
//				if (!currentPart.endsWith("-->"))
//					currentPart = currentPart.substring(0, (currentPart.length() - "->".length())) + "-->";
//				ts.setElementAt(currentPart, index);
//				lastPreservedPart = currentPart;
//				index++;
//			}
			
			//	repair badly marked comments
			else if (currentPart.value.startsWith("<!-") && currentPart.value.endsWith("->")) {
				String currentPartValue = currentPart.value;
				if (!currentPartValue.startsWith("<!--"))
					currentPartValue = "<!--" + currentPartValue.substring("<!-".length());
				if (!currentPartValue.endsWith("-->"))
					currentPartValue = currentPartValue.substring(0, (currentPartValue.length() - "->".length())) + "-->";
				ts.setElementAt(new Token(currentPartValue, currentPart.start), index);
				lastPreservedPart = currentPart;
				index++;
			}
			
			//	reassemble parsed scripts
			else if (this.isTag(currentPart.value) && !this.isEndTag(currentPart.value) && this.getType(currentPart.value).equalsIgnoreCase("script")) {
				inScript = true;
				lastPreservedPart = currentPart;
				index++;
			}
			
			//	reassemble parsed styles
			else if (this.isTag(currentPart.value) && !this.isEndTag(currentPart.value) && this.getType(currentPart.value).equalsIgnoreCase("style")) {
				inStyle = true;
				lastPreservedPart = currentPart;
				index++;
			}
			
			//	otherwise simply retain the part
			else {
				lastPreservedPart = currentPart;
				index++;
			}
		}
	}
}
