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
package de.uka.ipd.idaho.stringUtils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;



/**
 * A library providing some frequently needed String functions as well as
 * constants. The functions in particular:
 * <ul>
 * <li>Porter's stemming algorithm (the porterStem() methods)</li>
 * <li>Levenshtein's editing distance algorithm (the getLevenshteinDistance()
 * methods)</li>
 * <li>A Levenshtein-based String-to-String transformation algorithm (the
 * getLevenshteinEditSequence() methods)</li>
 * </ul>
 */
public class StringUtils {
	
	/** 'null' as a character, namely '\u0000' */
	public static final char NULLCHAR = '\u0000';
	
	/** string constant containing all Latin letters in upper and lower case */
	public static final String LATIN_LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/** string constant containing all Latin vowels in upper and lower case */
	public static final String LATIN_VOWELS = "aeiouAEIOU";
	
	/** string constant containing all Latin consonants in upper and lower case */
	public static final String LATIN_CONSONANTS = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
	
	/** string constant containing all Latin letters in upper case */
	public static final String LATIN_UPPER_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/** string constant containing all Latin letters in lower case */
	public static final String LATIN_LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	
	/** string constant containing all digits 0 through 9 */
	public static final String DIGITS = "0123456789";
	
	/** string constant containing all ASCII punctuation marks */
	public static final String PUNCTUATION = "\u00B0!\"\u00A7$%&/()=\u00BF?{[]}\\@\u20AC\u00A3\u00A5+*~#'\u00B4`<>|,;.:-_^";
	
	/*
\u0020 --> ASCII space
\u0085 --> ellipsis
\u00A0 --> non-breaking space
\u1680 --> ogham space
\u2000 --> en quad
\u2001 --> em quad
\u2002 --> en space
\u2003 --> em space
\u2004 --> one third space
\u2005 --> one fourth space
\u2006 --> one sixth space
\u2007 --> figure space
\u2008 --> punctuation space
\u2009 --> thin space
\u200A --> hair space
\u2028 --> line separator
\u2029 --> paragraph separator
\u202F --> narrow non-breaking space
\u205F --> medium math space
\u3000 --> ideographic space
	 */
	/** string constant containing all Unicode spaces */
	public static final String SPACES = "\u0020\u0085\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u2028\u2029\u202F\u205F\u3000";
	
	/*
\u200B --> zero-width space
\u200C --> zero-width non-joiner
\u200D --> zero-width joiner
\u200E --> left-to-right mark
\u200F --> right-to-left mark
\u202A --> left-to-right embedding
\u202B --> right-to-left embedding
\u202C --> pop directional formatting
\u202D --> left-to-right override
\u202E --> right-to-left override
\u2060 --> word joiner
\u2061 --> function application
\u2062 --> invisible times
\u2063 --> invisible separator
\u2064 --> invisible plus
\u2065 --> <unnamed invisible character>
\u2066 --> left-to-right isolate
\u2067 --> right-to-left isolate
\u2068 --> first strong isolate
\u2069 --> pop directional isolate
\u206A --> inhibit symmetric swapping
\u206B --> activate symmetric swapping
\u206C --> inhibit Arabic form shaping
\u206D --> activate Arabic form shaping
\u206E --> national digit shapes
\u206F --> nominal digit shapes
\uFE00 --> variation selector 1
\uFE01 --> variation selector 2
\uFE02 --> variation selector 3
\uFE03 --> variation selector 4
\uFE04 --> variation selector 5
\uFE05 --> variation selector 6
\uFE06 --> variation selector 7
\uFE07 --> variation selector 8
\uFE08 --> variation selector 9
\uFE09 --> variation selector 10
\uFE0A --> variation selector 11
\uFE0B --> variation selector 12
\uFE0C --> variation selector 13
\uFE0D --> variation selector 14
\uFE0E --> variation selector 15
\uFE0F --> variation selector 16
\uFEFF --> zero-width non-breaking space
\uFFF9 --> interlinear annotation anchor
\uFFFA --> interlinear annotation separator
\uFFFB --> interlinear annotation terminator
	 */
	/** string constant containing all invisible Unicode characters, like zero-width spaces ad word joiners */
	public static final String INVISIBLE_CHARACRES = "\u200B\u200C\u200D\u200E\u200F\u202A\u202B\u202C\u202D\u202E\u2060\u2061\u2062\u2063\u2064\u2065\u2066\u2067\u2068\u2069\u206A\u206B\u206C\u206D\u206E\u206F\uFE00\uFE01\uFE02\uFE03\uFE04\uFE05\uFE06\uFE07\uFE08\uFE09\uFE0A\uFE0B\uFE0C\uFE0D\uFE0E\uFE0F\uFEFF\uFFF9\uFFFA\uFFFB";
	
	/*
- --> ASCII dash/hyphen/minus
\u00AD --> soft hyphen
\u2010 --> hyphen
\u2011 --> non-breaking hyphen
\u2012 --> figure dash
\u2013 --> en dash
\u2014 --> em dash
\u2015 --> horizontal bar
\u2212 --> minus sign
	 */
	/** string constant containing all Unicode dashes */
	public static final String DASHES = "-\u00AD\u2010\u2011\u2012\u2013\u2014\u2015\u2212";
	
	/** string constant containing all Unicode dashes usually used as hyphens (excludes figure dash, en dash, em dash, horizontal bar) */
	public static final String HYPHENS = "-\u00AD\u2010\u2011\u2212";
	
	/*
' --> high comma
\u0060 --> grave accent
\u00B4 --> acute accent
\u02B9 --> modifier prime
\u02BB --> modifier turned comma
\u02BC --> modifier apostrophe
\u02BD --> modifier inverse comma
\u02CA --> modifier acute accent
\u02CB --> modifier grave accent
\u2018 --> left single quote
\u2019 --> right single quote
\u201A --> low-9 single quote
\u201B --> high-reversed-9 single quote
\u2032 --> prime
\u2035 --> reversed prime
	 */
	/** string constant containing all Unicode single quotes or high commas and similar characters */
	public static final String SINGLE_QUOTES = "'\u0060\u00B4\u02B9\u02BB\u02BC\u02BD\u02CA\u02CB\u2018\u2019\u201A\u201B\u2032\u2035";
	
	/** string constant containing all Unicode apostrophes or high commas */
	public static final String APOSTROPHES = "'\u0060\u00B4\u02B9\u02BB\u02BC\u02BD\u02CA\u02CB\u2018\u2019\u201B\u2032\u2035";
	
	/*
\" --> ASCII double quote
\u00AB --> guillemot left
\u00BB --> guillemot right
\u02BA --> modifier double prime
\u02DD --> double acute accent
\u02EE --> double apostrophe
\u02F5 --> modifier middle double grave accent
\u02F6 --> modifier middle double acute accent
\u201C --> left double quote
\u201D --> right double quote
\u201E --> low-9 double quote
\u201F --> high-reversed-9 double quote
\u2033 --> double prime
\u2036 --> reversed double prime
\u301D --> reversed double prime quote
\u301E --> double prime quote
\u301F --> low double prime quote
	 */
	/** string constant containing all Unicode double quotes and similar characters */
	public static final String DOUBLE_QUOTES = "\"\u00AB\u00BB\u02BA\u02DD\u02EE\u02F5\u02F6\u201C\u201D\u201E\u201F\u2033\u2036\u301D\u301E\u301F";
	
	/** string constant containing punctuation marks that may appear within words, namely hyphens and apostrophes (in their various forms) */
	public static final String IN_WORD_PUNCTUATION = (HYPHENS + APOSTROPHES);
	
	/** string constant containing punctuation marks that may appear in numbers, namely ',' and '.' */
	public static final String IN_NUMBER_PUNCTUATION = ",.";
	
	/** string constant containing all brackets, opening and closing, round, square, angle, and curly */
	public static final String BRACKETS	= "({[<)}]>";
	
	/** string constant containing all opening brackets, round, square, angle, and curly. The index of a given type of bracket in this constant is the same as the index of the corresponding bracket in CLOSING_BRACKETS */
	public static final String OPENING_BRACKETS = "({[<";
	
	/** string constant containing all closing brackets, round, square, angle, and curly. The index of a given type of bracket in this constant is the same as the index of the corresponding bracket in OPENING_BRACKETS */
	public static final String CLOSING_BRACKETS = ")}]>";
	
	/** string constant containing all punctuation marks that structure a sentence, namely '!', '?', ',', ';', '.', ':', and '-' */
	public static final String SENTENCE_PUNCTUATION = "!?,;.:-";
	
	/** string constant containing all punctuation marks that end a sentence, namely '!', '?', and '.' */
	public static final String SENTENCE_ENDINGS	= "!?.";
	
	/** string constant containing all punctuation marks usually having no space before them, namely closing brackets, and '!', '?', ',', ';', '.', ':', '&acute;', and '`' */
	public static final String UNSPACED_BEFORE = "!)?]}>,;.:\u00B4`";
	
	/** string constant containing all punctuation marks usually having no space after them, namely opening brackets, '&acute;', and '`' */
	public static final String UNSPACED_AFTER = "([{<\u00B4`";
	
	/** string constant containing common abbreviations, like 'Mr.' or 'Jun.' */
	public static final String COMMON_ABBREVIATIONS = "No.Prof.Dr.Mr.Mrs.Ms.Jun.Sen.Mt.A.B.C.D.E.F.G.H.I.J.K.L.M.N.O.P.Q.R.S.T.U.V.W.X.Y.Z.";
	
	/** string constant containing common stop words, plus single letters and digits, separated by ';' */
	public static final String noiseWordString = "$;0;1;2;3;4;5;6;7;8;9;a;about;after;all;along;among;also;an;and;another;any;are;as;at;b;be;because;been;before;being;below;between;both;but;by;c;came;can;come;could;d;did;do;e;each;f;for;from;g;get;got;h;had;has;have;he;her;here;him;himself;his;how;i;if;in;inside;into;is;it;its;j;k;l;like;m;make;many;me;might;more;most;much;must;my;n;near;never;not;now;o;of;on;only;or;other;our;out;outside;over;p;q;r;s;said;same;see;should;since;so;some;still;such;t;take;than;that;the;their;them;then;there;these;they;this;those;through;to;too;u;under;up;v;very;w;was;way;we;well;were;what;when;where;which;while;who;whom;whose;will;with;without;would;x;y;you;your;z;_";
	
	/** string constant containing common stop words that may be included in proper names, separated by ';' */
	public static final String infixWordString = "and;at;by;in;of;on;over;the;under";
	
	/** string constant containing common titles, separated by ';' */
	public static final String titlesString = "King;Prof;Dr;Mr;Mrs;Ms;Jun;Sen;Sir";
	
	/** string constant containing common stop words appearing in person names, separated by ';' */
	public static final String personNameConnectorString = "of;de;von;del;la;";
	
	/** string constant containing start words of common official titles, separated by ';' */
	public static final String officialNamePartString = "King;Director;Major;Minister;Vice;President;Secretary";
	
	/** string constant containing common prepositions appearing with location names, separated by ';' */
	public static final String localityPrepositionString = "in;at;near;from;through;to;around;outside;inside";
	
	/** string constant containing common endings of location names, separated by ';' */
	public static final String localityNameEndingString = "town;ville";
	
	/** string constant containing common prefixes of location names, like 'Port' or 'Saint', separated by ';' */
	public static final String localityNameStartString = "St.;Saint;Mt.;Mount;Lake;Port;North;Northern;South;Southern;East;Eastern;West;Western;Central;Northeast;Northeastern;Northwest;Northwestern;Southeast;Southeastern;Southwest;Southwestern";
	
	/** string constant containing common suffixes of location names, like 'Island' or 'County', separated by ';' */
	public static final String localityPostpositionString = "Island;Islands;area;County;Springs;Wells;Beach;Valley;Garden;Cave";
	
	/** string constant containing common stop words appearing in location names, separated by ';' */
	public static final String localityNameConnectorString = "of;and;de;del;et;sur;am;an;der;en;la;les;&;/";
	
	/** string constant containing common parts of organization names, like 'Institution' or 'Museum', separated by ';' */
	public static final String organizationNamePartString = "Department;Ministery;Museum;Institution;Institute;Laboratory;University;Ltd;Inc;Corp;Corporation";
	
	/** string constant containing all month names */
	public static final String monthString = "January;February;March;April;May;June;July;August;September;October;November;December";
	
	/**	@return	a list of so called noise or stop words, packed in a StringVector
	 * 	e.g. the, all, also, from 
	 */
	public static StringVector getNoiseWords() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(noiseWordString, ";");
		return ret;
	}
	
	/**	@return	a list of words (conjunctions in particular) that can apper in lower case within a Named Entity, packed in a StringVector
	 * 	e.g. the, of, at, ... 
	 */
	public static StringVector getNamedEntityInfixes() {
		StringVector sv = new StringVector(false);
		sv.parseAndAddElements(infixWordString, ";");
		return sv;
	}
	
	/**	@return	a list of words that are very likely to be followed by a location name, packed in a StringVector
	 * 	e.g. near, through,	around, etc 
	 */
	public static StringVector getLocationPrepositions() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(localityPrepositionString, ";");
		return ret;
	}
	
	/**	@return	a list of words that are very likely to follow a location name or to be the last part of one, packed in a StringVector
	 * 	e.g. area, Island, County, etc 
	 */
	public static StringVector getLocationNamePostpositions() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(localityPostpositionString, ";");
		return ret;
	}
	
	/**	@return	a list of character sequences that are very likely to be the ending of a location name, packed in a StringVector
	 * 	e.g. town in Jamestown, ville in Shelbieville, etc 
	 */
	public static StringVector getLocationNameEndings() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(localityNameEndingString, ";");
		return ret;
	}
	
	/**	@return	a list of words that are very likely to be the beginning of a location name, packed in a StringVector
	 * 	e.g. Lake, Mt., Mount, etc 
	 */
	public static StringVector getLocationNameStarts() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(localityNameStartString, ";");
		return ret;
	}
	
	/**	@return	a list of words that may appear within a location name, packed in a StringVector
	 * 	e.g. and in Trinidad and Tobago, of in Isle of Man, etc 
	 */
	public static StringVector getLocationNameConnectors() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(localityNameConnectorString, ";");
		return ret;
	}
	
	/**	@return	a list of titles that indicate the beginning of a person's name, packed in a StringVector
	 * 	e.g. Dr., Prof., Mr., etc 
	 */
	public static StringVector getTitles() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(titlesString, ";");
		return ret;
	}
	
	/**	@return	a list of words that might appear within a person's name or title, packed in a StringVector
	 * 	e.g. of in Secretary of Defence, etc 
	 */
	public static StringVector getPersonNameConnectors() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(personNameConnectorString, ";");
		return ret;
	}
	
	/**	@return	a list of words that can be part of a person's title, packed in a StringVector
	 * 	e.g. Secretary, Director, Minister, etc 
	 */
	public static StringVector getOfficialNameParts() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(officialNamePartString, ";");
		return ret;
	}
	
	/**	@return	a list of words that can be part of an organization's name, packed in a StringVector
	 * 	e.g. Museum, Department, Ministery, etc 
	 */
	public static StringVector getOrganizationNameParts() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(organizationNamePartString, ";");
		return ret;
	}
	
	/**	@return	the names of the twelve months, packed in a StringVector
	 */
	public static StringVector getMonthNames() {
		StringVector ret = new StringVector(false);
		ret.parseAndAddElements(monthString, ";");
		return ret;
	}
	
	/**	@return	true if and only if the first character of string s is in upper case
	 */
	public static boolean isUpperCase(String string) {
		return isUpperCase((CharSequence) string);
	}
	
	/**	@return	true if and only if the first character of string s is in upper case
	 */
	public static boolean isUpperCase(CharSequence string) {
		if ((string == null) || (string.length() == 0))
			return false;
		string = removePunctuation(string);
//		return ((string.length() > 0) && (UPPER_CASE_LETTERS.indexOf(string.substring(0, 1)) != -1));
		char ch = string.charAt(0);
		return (Character.isLetter(ch) && Character.isUpperCase(ch));
	}
	
	/**	@return	string s, stripped of potential leading and tailing punctuation marks
	 */
	public static String removePunctuation(String string) {
		if (string == null)
			return null;
		return removePunctuation((CharSequence) string).toString();
	}
	
	/**	@return	string s, stripped of potential leading and tailing punctuation marks
	 */
	public static CharSequence removePunctuation(CharSequence string) {
		if (string == null)
			return null;
		else if ("".equals(string))
			return string;
		
		int start = 0;
		int end = string.length();
		
		//	truncate ending punctuation
		while ((end > 0) && !Character.isLetter(string.charAt(end-1)))
			end--;
//		if (end > 0) {
//			String end = string.substring(length-1);
//			while ((length > 0) && (LETTERS.indexOf(end) == -1)) {
//				if (length > 1) {
//					length--;
//					string = string.substring(0, length);
//					end = string.substring(length-1);
//				} else {
//					string = "";
//					length = 0;
//				}
//			}
//		}
		
		//	truncate leading punctuation
		while ((start < end) && !Character.isLetter(string.charAt(start)))
			start++;
//		length = string.length();
//		if (length > 0) {
//			String start = string.substring(0, 1);
//			while ((length > 0) && (LETTERS.indexOf(start) == -1)) {
//				if (length > 1) {
//					length--;
//					string = string.substring(1);
//					start = string.substring(0, 1);
//				} else {
//					string = "";
//					length = 0;
//				}
//			}
//		}
		
		return string.subSequence(start, end);
	}
	private static class PorterBox {
		char[] wordBuffer;
		int bufferLevel; 	//	number of characters in word buffer
		int stemEndIndex; 	//	position of the word stem's last character
		int endIndex;	//	position of the word's last character during the stemming process
		
		//private static final int BUFFER_GROWTH_STEP = 50; 	//	unit of size whereby the buffer is increased
		
		PorterBox(String string) {
			StringBuffer buffer = new StringBuffer();
			for (int c = 0; c < string.length(); c++) {
				char ch = Character.toLowerCase(getBaseChar(string.charAt(c)));
				if ((LATIN_LOWER_CASE_LETTERS.indexOf(ch) != -1) || (IN_WORD_PUNCTUATION.indexOf(ch) != -1))
					buffer.append(ch);
			}
			this.wordBuffer = buffer.toString().toCharArray();
			this.bufferLevel = buffer.length();
			this.endIndex = this.bufferLevel - 1;
		}
		
		/**	true if and only if the character at index is a consonant
		 */
		boolean hasConsonantAt(int index) {
			switch (this.wordBuffer[index]) {
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
				return false;
			case 'y':
				return (index == 0) ? true : !this.hasConsonantAt(index - 1);
			default:
				return true;
			}
		}
		
		/**	true if and only if the character at index-2 is a consonant, that at index-1 is a vowel and that at index a consonant again, and if the latter is neither of w, x or y 
		 */
		boolean hasConsonantVowelConsonantAt(int index) {
			if (index < 2 || !this.hasConsonantAt(index) || this.hasConsonantAt(index - 1) || !this.hasConsonantAt(index - 2))
				return false;
			int ch = this.wordBuffer[index];
			return ((ch != 'w') && (ch != 'x') && (ch != 'y'));
		}
		
		/**	countConsonantSequences() measures the number of consonant sequences between 0 and stemEndIndex.
		 * 	if c is a consonant sequence and v a vowel sequence, and (...) indicates arbitrary presence,
		 *  (c)(v) returns 0, (c)vc(v) returns 1, (c)vcvc(v) returns 2, (c)vcvcvc(v) returns 3 ...
		 */

		int countConsonantSequences() {
			int n = 0;
			int i = 0;
			while (true) {
				if (i > this.stemEndIndex) return n;
				if (!this.hasConsonantAt(i)) break;
				i++;
			}
			i++;
			while (true) {
				while (true) {
					if (i > this.stemEndIndex) return n;
					if (this.hasConsonantAt(i)) break;
					i++;
				}
				i++;
				n++;
				while (true) {
					if (i > this.stemEndIndex) return n;
					if (!this.hasConsonantAt(i)) break;
					i++;
				}
				i++;
			}
		}
		
		/**	true if and only if the buffer contains a vowel in at least one position with index less than stemEndIndex 
		 */
		boolean hasVowelInStem() {
			for (int i = 0; i <= this.stemEndIndex; i++) {
				if (!this.hasConsonantAt(i))
					return true;
			}
			return false;
		}

		/**	true if and only if the characters at index-1 and index are equal and both consonants
		 */
		boolean hasDoubleConsonantAt(int index) {
			if (index < 1)
				return false;
			if (this.wordBuffer[index] != this.wordBuffer[index - 1])
				return false;
			return this.hasConsonantAt(index);
		}
		
		/**	true if and only if the last string.length() characters in the buffer match string's characters
		 */
		boolean endsWith(String string) {
			int size = string.length();
			int offset = this.endIndex - size + 1;
			if (offset < 0)
				return false;
			for (int i = 0; i < size; i++) {
				if (this.wordBuffer[offset + i] != string.charAt(i))
					return false;
			}
			this.stemEndIndex = this.endIndex - size;
			return true;
		}

		/**	setEndTo(s) sets (stemEndIndex+1), ... , actualsize to string's characters, readjusting actualsize.
		 */
		void setEndTo(String string) {
			int l = string.length();
			int offset = this.stemEndIndex + 1;
			for (int i = 0; i < l; i++)
				this.wordBuffer[offset + i] = string.charAt(i);
			this.endIndex = this.stemEndIndex + l;
		}
		
		/**	sets the end of the buffer to string if there is at least one consonant sequence in the stem
		 */
		void replaceEndBy(String string) {
			if (this.countConsonantSequences() > 0)
				this.setEndTo(string);
		}
		
		/**	returns the stemmed word
		 */
		String getString() {
			return new String(this.wordBuffer, 0, this.endIndex+1);
		}
	}
	
	/**	@return	string s, stemmed according to Porter's Algorithm
	 */
	public static String porterStem(String s) {
		//	stem list of words
		if (s.indexOf(" ") > -1) {
			StringVector strings = new StringVector();
			strings.parseAndAddElements(s, " ");
			StringVector stemmed = porterStem(strings);
			return stemmed.concatStrings(" ");
		}
		
		//	stem single word
		else {
			PorterBox box = new PorterBox(s);
			if (box.endIndex > 1) {
				executePorterStep1(box);
				executePorterStep2(box);
				executePorterStep3(box);
				executePorterStep4(box);
				executePorterStep5(box);
				executePorterStep6(box);
			}
			return box.getString();
		}
	}
	
	/**	@return	a StringVector containing all the strings from sv, stemmed according to Porter's Algorithm
	 */
	public static StringVector porterStem(StringVector sv) {
		StringVector ret = new StringVector();
		for (int i = 0; i < sv.size(); i++)
			ret.addElement(porterStem(sv.get(i)));
		return ret;
	}
	
	/**	step 1 removes plural endings etc, e.g. -ed or -ing
	 *	caresses -> caress
	 *	ponies -> poni
	 *	ties -> ti
	 *	caress -> caress
	 *	cats -> cat
	 *	feed -> feed
	 *	agreed -> agree
	 *	disabled -> disable
	 *	matting -> mat
	 *	mating -> mate
	 *	meeting -> meet
	 *	milling -> mill
	 *	messing -> mess
	 *	meetings -> meet
	 */
	private static void executePorterStep1(PorterBox box) {
		if (box.wordBuffer[box.endIndex] == 's') {
			if (box.endsWith("sses"))
				box.endIndex -= 2;
			else if (box.endsWith("ies"))
				box.setEndTo("i");
			else if (box.wordBuffer[box.endIndex - 1] != 's')
				box.endIndex--;
		}
		if (box.endsWith("eed"))
			if (box.countConsonantSequences() > 0) box.endIndex--;
		else if ((box.endsWith("ed") || box.endsWith("ing")) && box.hasVowelInStem()) {
			box.endIndex = box.stemEndIndex;
			if (box.endsWith("at"))
				box.setEndTo("ate");
			else if (box.endsWith("bl"))
				box.setEndTo("ble");
			else if (box.endsWith("iz"))
				box.setEndTo("ize");
			else if (box.hasDoubleConsonantAt(box.endIndex)) {
				box.endIndex--;
				int ch = box.wordBuffer[box.endIndex];
				if (ch == 'l' || ch == 's' || ch == 'z')
					box.endIndex++;
			}
			else if (box.countConsonantSequences() == 1 && box.hasConsonantVowelConsonantAt(box.endIndex))
				box.setEndTo("e");
		}
	}
	
	/** step 2 turns terminal y to i if there is another vowel in the stem.*/
	private static void executePorterStep2(PorterBox box) {
		if (box.endsWith("y") && box.hasVowelInStem())
			box.wordBuffer[box.endIndex] = 'i';
	}
	
	/** step 3 maps double suffices to single ones. so -ization ( = -ize plus
	 * -ation) maps to -ize etc. note that the string before the suffix must
	 * have countConsonantSequences() > 0.
	 */
	private static void executePorterStep3(PorterBox box) {
		if (box.endIndex == 0)
			return;
		switch (box.wordBuffer[box.endIndex - 1]) {
		case 'a':
			if (box.endsWith("ational")) {
				box.replaceEndBy("ate");
				break;
			}
			if (box.endsWith("tional")) {
				box.replaceEndBy("tion");
				break;
			}
			break;
		case 'c':
			if (box.endsWith("enci")) {
				box.replaceEndBy("ence");
				break;
			}
			if (box.endsWith("anci")) {
				box.replaceEndBy("ance");
				break;
			}
			break;
		case 'e':
			if (box.endsWith("izer")) {
				box.replaceEndBy("ize");
				break;
			}
			break;
		case 'l':
			if (box.endsWith("bli")) {
				box.replaceEndBy("ble");
				break;
			}
			if (box.endsWith("alli")) {
				box.replaceEndBy("al");
				break;
			}
			if (box.endsWith("entli")) {
				box.replaceEndBy("ent");
				break;
			}
			if (box.endsWith("eli")) {
				box.replaceEndBy("e");
				break;
			}
			if (box.endsWith("ousli")) {
				box.replaceEndBy("ous");
				break;
			}
			break;
		case 'o':
			if (box.endsWith("ization")) {
				box.replaceEndBy("ize");
				break;
			}
			if (box.endsWith("ation")) {
				box.replaceEndBy("ate");
				break;
			}
			if (box.endsWith("ator")) {
				box.replaceEndBy("ate");
				break;
			}
			break;
		case 's':
			if (box.endsWith("alism")) {
				box.replaceEndBy("al");
				break;
			}
			if (box.endsWith("iveness")) {
				box.replaceEndBy("ive");
				break;
			}
			if (box.endsWith("fulness")) {
				box.replaceEndBy("ful");
				break;
			}
			if (box.endsWith("ousness")) {
				box.replaceEndBy("ous");
				break;
			}
			break;
		case 't':
			if (box.endsWith("aliti")) {
				box.replaceEndBy("al");
				break;
			}
			if (box.endsWith("iviti")) {
				box.replaceEndBy("ive");
				break;
			}
			if (box.endsWith("biliti")) {
				box.replaceEndBy("ble");
				break;
			}
			break;
		case 'g':
			if (box.endsWith("logi")) {
				box.replaceEndBy("log");
				break;
			}
		}
	}

	/** step 4 deals with -ic-, -full, -ness etc. similar strategy to step3.*/
	private static void executePorterStep4(PorterBox box) {
		switch (box.wordBuffer[box.endIndex]) {
		case 'e':
			if (box.endsWith("icate")) {
				box.replaceEndBy("ic");
				break;
			}
			if (box.endsWith("ative")) {
				box.replaceEndBy("");
				break;
			}
			if (box.endsWith("alize")) {
				box.replaceEndBy("al");
				break;
			}
			break;
		case 'i':
			if (box.endsWith("iciti")) {
				box.replaceEndBy("ic");
				break;
			}
			break;
		case 'l':
			if (box.endsWith("ical")) {
				box.replaceEndBy("ic");
				break;
			}
			if (box.endsWith("ful")) {
				box.replaceEndBy("");
				break;
			}
			break;
		case 's':
			if (box.endsWith("ness")) {
				box.replaceEndBy("");
				break;
			}
			break;
		}
	}
	
	/** step 5 takes off -ant, -ence etc., in context (C)vcvc(V).*/
	private static void executePorterStep5(PorterBox box) {
		if (box.endIndex == 0) return;
		switch (box.wordBuffer[box.endIndex - 1]) {
		case 'a':
			if (box.endsWith("al")) break;
			return;
		case 'c':
			if (box.endsWith("ance")) break;
			if (box.endsWith("ence")) break;
			return;
		case 'e':
			if (box.endsWith("er")) break;
			return;
		case 'i':
			if (box.endsWith("ic")) break;
			return;
		case 'l':
			if (box.endsWith("able")) break;
			if (box.endsWith("ible")) break;
			return;
		case 'n':
			if (box.endsWith("ant")) break;
			if (box.endsWith("ement")) break;
			if (box.endsWith("ment")) break;
			if (box.endsWith("ent")) break;
			return;
		case 'o':
			if (box.endsWith("ion") && box.stemEndIndex >= 0 && (box.wordBuffer[box.stemEndIndex] == 's' || box.wordBuffer[box.stemEndIndex] == 't')) break;
			if (box.endsWith("ou")) break;
			return;
		case 's':
			if (box.endsWith("ism")) break;
			return;
		case 't':
			if (box.endsWith("ate")) break;
			if (box.endsWith("iti")) break;
			return;
		case 'u':
			if (box.endsWith("ous")) break;
			return;
		case 'v':
			if (box.endsWith("ive")) break;
			return;
		case 'z':
			if (box.endsWith("ize")) break;
			return;
		default:
			return;
		}
		if (box.countConsonantSequences() > 1) box.endIndex = box.stemEndIndex;
	}
	
	/** step 6 removes a final -e if countConsonantSequences() > 1. */
	private static void executePorterStep6(PorterBox box) {
		box.stemEndIndex = box.endIndex;
		if (box.wordBuffer[box.endIndex] == 'e') {
			int a = box.countConsonantSequences();
			if (a > 1 || a == 1 && !box.hasConsonantVowelConsonantAt(box.endIndex - 1))
				box.endIndex--;
		}
		if (box.wordBuffer[box.endIndex] == 'l' && box.hasDoubleConsonantAt(box.endIndex) && box.countConsonantSequences() > 1)
			box.endIndex--;
	}
	
	/**	check if a String is a closing bracket closing another String that is an opening bracket
	 * @param	string1	the to-be tested closing bracket String
	 * @param	string2	the opening bracket String to test string1 against
	 * @return	true if and only if the first String is a closing bracket, the second String is an opening bracket, and both brackets match 
	 */
	public static boolean closes(String string1, String string2) {
		return closes(((CharSequence) string1), ((CharSequence) string2));
	}
	
	/**	check if a String is a closing bracket closing another String that is an opening bracket
	 * @param	string1	the to-be tested closing bracket String
	 * @param	string2	the opening bracket String to test string1 against
	 * @return	true if and only if the first String is a closing bracket, the second String is an opening bracket, and both brackets match 
	 */
	public static boolean closes(CharSequence string1, CharSequence string2) {
		if ((string1 == null) || (string1.length() != 1))
			return false;
		else if ((string2 == null) || (string2.length() != 1))
			return false;
		else return (isOpeningBracket(string2) && isClosingBracket(string1) && (OPENING_BRACKETS.indexOf(string2.charAt(0)) == CLOSING_BRACKETS.indexOf(string1.charAt(0))));
	}
	
	/**	check if a String is an opening bracket opening another String that is a closing bracket
	 * @param	string1	the to-be tested opening bracket String
	 * @param	string2	the closing bracket String to test string1 against
	 * @return	true if and only if the first String is an opening bracket, the second String is a closing bracket, and both brackets match 
	 */
	public static boolean opens(String string1, String string2) {
		return opens(((CharSequence) string1), ((CharSequence) string2));
	}
	
	/**	check if a String is an opening bracket opening another String that is a closing bracket
	 * @param	string1	the to-be tested opening bracket String
	 * @param	string2	the closing bracket String to test string1 against
	 * @return	true if and only if the first String is an opening bracket, the second String is a closing bracket, and both brackets match 
	 */
	public static boolean opens(CharSequence string1, CharSequence string2) {
		return closes(string2, string1);
	}

	/**	check if a String contains at least one vowel
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains at least one vowel
	 */
	public static boolean containsVowel(String string) {
		return containsVowel((CharSequence) string);
	}

	/**	check if a String contains at least one vowel
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains at least one vowel
	 */
	public static boolean containsVowel(CharSequence string) {
		return containsVowel(string, true);
	}

	/**	check if a String contains at least one vowel
	 * @param	string	the String to be tested
	 * @param	yIsVowel	count 'y' as a vowel?
	 * @return	true if and only if the String is a word and contains at least one vowel
	 */
	public static boolean containsVowel(CharSequence string, boolean yIsVowel) {
		if (string == null)
			return false;
		for (int c = 0; c < string.length(); c++) {
			char ch = getBaseChar(string.charAt(c));
			if (LATIN_VOWELS.indexOf(ch) != -1)
				return true;
			else if (yIsVowel && "yY".indexOf(ch) != -1)
				return true;
		}
		return false;
	}

	/**	check if a String contains at least one consonant
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains at least one consonant
	 */
	public static boolean containsConsonant(String string) {
		return containsConsonant((CharSequence) string);
	}

	/**	check if a String contains at least one consonant
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains at least one consonant
	 */
	public static boolean containsConsonant(CharSequence string) {
		return containsConsonant(string, true);
	}

	/**	check if a String contains at least one consonant
	 * @param	string	the String to be tested
	 * @param	yIsConsonant	count 'y' as a consonant?
	 * @return	true if and only if the String is a word and contains at least one consonant
	 */
	public static boolean containsConsonant(CharSequence string, boolean yIsConsonant) {
		if (string == null)
			return false;
		for (int c = 0; c < string.length(); c++) {
			char ch = getBaseChar(string.charAt(c));
			if (LATIN_CONSONANTS.indexOf(ch) != -1)
				return true;
			else if (yIsConsonant && "yY".indexOf(ch) != -1)
				return true;
		}
		return false;
	}

	/**	check if a String has a space between the previous String and itself
	 * @param	string	the String to be tested
	 * @return true if and only if the specified Stringe has a space between the previous String and itself
	 */
	public static boolean spaceBefore(String string) {
		return spaceBefore((CharSequence) string);
	}

	/**	check if a String has a space between the previous String and itself
	 * @param	string	the String to be tested
	 * @return true if and only if the specified String has a space between the previous String and itself
	 */
	public static boolean spaceBefore(CharSequence string) {
		if ((string == null) || (string.length() == 0))
			return false;
		else if (string.length() != 1)
			return true;
		else return (UNSPACED_BEFORE.indexOf(string.charAt(0)) == -1);
	}
	
	/**	check if a String has a space between itself and the subsequent String
	 * @param	string	the String to be tested
	 * @return true if and only if the specified String has a space between itself and the subsequent Sting
	 */
	public static boolean spaceAfter(String string) {
		return spaceAfter((CharSequence) string);
	}
	
	/**	check if a String has a space between itself and the subsequent String
	 * @param	string	the String to be tested
	 * @return true if and only if the specified String has a space between itself and the subsequent Sting
	 */
	public static boolean spaceAfter(CharSequence string) {
		if ((string == null) || (string.length() == 0))
			return false;
		else if (string.length() != 1)
			return true;
		else return (UNSPACED_AFTER.indexOf(string.charAt(0)) == -1);
	}
	
	/**	check if a space is to be inserted between two Strings in a text 
	 * @param	string1		the first String
	 * @param	string2		the second String
	 * @return true is and only if a space is to be inserted between the two specified Strings on concatenation
	 */
	public static boolean insertSpace(String string1, String string2) {
		return insertSpace(((CharSequence) string1), ((CharSequence) string2));
	}
	
	/**	check if a space is to be inserted between two Strings in a text 
	 * @param	string1		the first String
	 * @param	string2		the second String
	 * @return true is and only if a space is to be inserted between the two specified Strings on concatenation
	 */
	public static boolean insertSpace(CharSequence string1, CharSequence string2) {
		if ((string1 == null) || (string2 == null))
			return false;
		else return (spaceAfter(string1) && spaceBefore(string2));
		//	TODO: make clear decision
		//return true;
	}
	
	/**	check if a String is a punctuation mark ending a sentence
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a punctuation mark ending a sentence
	 */
	public static boolean isSentenceEnd(String string) {
		return isSentenceEnd((CharSequence) string);
	}
	
	/**	check if a String is a punctuation mark ending a sentence
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a punctuation mark ending a sentence
	 */
	public static boolean isSentenceEnd(CharSequence string) {
		if ((string == null) || (string.length() != 1))
			return false;
		else return (SENTENCE_ENDINGS.indexOf(string.charAt(0)) != -1);
	}

	/**	check if a String is a punctuation mark used within sentences
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a punctuation mark used for sentence punctuation
	 */
	public static boolean isSentencePunctuation(String string) {
		return isSentencePunctuation((CharSequence) string);
	}

	/**	check if a String is a punctuation mark used within sentences
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a punctuation mark used for sentence punctuation
	 */
	public static boolean isSentencePunctuation(CharSequence string) {
		if ((string == null) || (string.length() != 1))
			return false;
		else return (SENTENCE_PUNCTUATION.indexOf(string.charAt(0)) != -1);
	}

	/**	check if a String is a closing bracket
	 * @param	string	the String to be tested
	 * @return		true if and only if the String is a closing bracket
	 */
	public static boolean isClosingBracket(String string) {
		return isClosingBracket((CharSequence) string);
	}

	/**	check if a String is a closing bracket
	 * @param	string	the String to be tested
	 * @return		true if and only if the String is a closing bracket
	 */
	public static boolean isClosingBracket(CharSequence string) {
		if ((string == null) || (string.length() != 1))
			return false;
		else return (CLOSING_BRACKETS.indexOf(string.charAt(0)) != -1);
	}

	/**	check if a String is an opening bracket
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is an opening bracket
	 */
	public static boolean isOpeningBracket(String string) {
		return isOpeningBracket((CharSequence) string);
	}

	/**	check if a String is an opening bracket
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is an opening bracket
	 */
	public static boolean isOpeningBracket(CharSequence string) {
		if ((string == null) || (string.length() != 1))
			return false;
		else return (OPENING_BRACKETS.indexOf(string.charAt(0)) != -1);
	}

	/**	check if a String is a bracket
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a bracket
	 */
	public static boolean isBracket(String string) {
		return isBracket((CharSequence) string);
	}

	/**	check if a String is a bracket
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a bracket
	 */
	public static boolean isBracket(CharSequence string) {
		if ((string == null) || (string.length() != 1))
			return false;
		else return (BRACKETS.indexOf(string.charAt(0)) != -1);
	}

	/**	check if a String is a punctuation mark
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a punctuation mark
	 */
	public static boolean isPunctuation(String string) {
		return isPunctuation((CharSequence) string);
	}

	/**	check if a String is a punctuation mark
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a punctuation mark
	 */
	public static boolean isPunctuation(CharSequence string) {
		if ((string == null) || (string.length() != 1))
			return false;
		else return (PUNCTUATION.indexOf(string.charAt(0)) != -1);
	}

	/**	check if a String is a number, in particular an Arabic number
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a number (consists of digits and in-number punctuation marks only)
	 */
	public static boolean isNumber(String string) {
		return isNumber((CharSequence) string);
	}

	/**	check if a String is a number, in particular an Arabic number
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a number (consists of digits and in-number punctuation marks only)
	 */
	public static boolean isNumber(CharSequence string) {
		if (string == null)
			return false;
		boolean gotDigit = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
//			if ((DIGITS.indexOf(c) == -1) && ((IN_NUMBER_PUNCTUATION.indexOf(c) == -1) || string.length() == 1))
//				return false;
			if (Character.isDigit(ch)) {
				gotDigit = true;
				continue;
			}
			if (IN_NUMBER_PUNCTUATION.indexOf(ch) == -1)
				return false;
		}
//		return true;
		return gotDigit;
	}
	
	/**	check if a String is a Roman number
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a Roman number
	 */
	public static boolean isRomanNumber(String string) {
		return isRomanNumber((CharSequence) string);
	}
	
	/**	check if a String is a Roman number
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a Roman number
	 */
	public static boolean isRomanNumber(CharSequence string) {
		if (string == null)
			return false;
		try {
			parseRomanNumber(string.toString());
			return true;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
	
	/**	check if a String is an upper case word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains capital letters only
	 */
	public static boolean isUpperCaseWord(String string) {
		return isUpperCaseWord((CharSequence) string);
	}
	
	/**	check if a String is an upper case word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains capital letters only
	 */
	public static boolean isUpperCaseWord(CharSequence string) {
		if (string == null)
			return false;
		boolean gotLetter = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
//			if ((UPPER_CASE_LETTERS.indexOf(ch) == -1) && ((IN_WORD_PUNCTUATION.indexOf(ch) == -1) || string.length() == 1))
//				return false;
			if (Character.isLetter(ch) && Character.isUpperCase(ch)) {
				gotLetter = true;
				continue;
			}
			if (IN_WORD_PUNCTUATION.indexOf(ch) == -1)
				return false;
		}
//		return isWord(string);
		return gotLetter;
	}

	/**	check if a String is a capitalized word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and starts with a capital letter, followed by lower case letters only
	 */
	public static boolean isCapitalizedWord(String string) {
		return isCapitalizedWord((CharSequence) string);
	}

	/**	check if a String is a capitalized word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and starts with a capital letter, followed by lower case letters only
	 */
	public static boolean isCapitalizedWord(CharSequence string) {
		if (string == null)
			return false;
		boolean gotLetter = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
//			if ((c == 0) && (UPPER_CASE_LETTERS.indexOf(ch) == -1)) return false;
//			if ((c != 0) && (LOWER_CASE_LETTERS.indexOf(ch) == -1) && (IN_WORD_PUNCTUATION.indexOf(ch) == -1)) return false;
			if ((c == 0) && Character.isLetter(ch) && Character.isUpperCase(ch)) {
				gotLetter = true;
				continue;
			}
			if ((c != 0) && Character.isLetter(ch) && Character.isLowerCase(ch))  {
				gotLetter = true;
				continue;
			}
			if ((c == 0) || (IN_WORD_PUNCTUATION.indexOf(ch) == -1))
				return false;
		}
//		return isWord(string);
		return gotLetter;
	}

	/**	check if a String is a first letter up word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and starts with a capital letter
	 */
	public static boolean isFirstLetterUpWord(String string) {
		return isFirstLetterUpWord((CharSequence) string);
	}

	/**	check if a String is a first letter up word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and starts with a capital letter
	 */
	public static boolean isFirstLetterUpWord(CharSequence string) {
		if (string == null)
			return false;
//		else return (StringUtils.isWord(string) && (UPPER_CASE_LETTERS.indexOf(string.charAt(0)) != -1));
		boolean gotLetter = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
			if (Character.isLetter(ch) && ((c != 0) || Character.isUpperCase(ch))) {
				gotLetter = true;
				continue;
			}
			if ((c == 0) || (IN_WORD_PUNCTUATION.indexOf(ch) == -1))
				return false;
		}
		return gotLetter;
	}

	/**	check if a String is a lower case word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains lower case letters only
	 */
	public static boolean isLowerCaseWord(String string) {
		return isLowerCaseWord((CharSequence) string);
	}

	/**	check if a String is a lower case word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word and contains lower case letters only
	 */
	public static boolean isLowerCaseWord(CharSequence string) {
		if (string == null)
			return false;
		boolean gotLetter = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
//			if ((LOWER_CASE_LETTERS.indexOf(ch) == -1) && ((IN_WORD_PUNCTUATION.indexOf(ch) == -1) || string.length() == 1))
//				return false;
			if (Character.isLetter(ch) && Character.isLowerCase(ch)) {
				gotLetter = true;
				continue;
			}
			if (IN_WORD_PUNCTUATION.indexOf(ch) == -1)
				return false;
		}
//		return isWord(string);
		return gotLetter;
	}

	/**	check if a String is a word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word (consists of letters and in-word punctuation marks only)
	 */
	public static boolean isWord(String string) {
		return isWord((CharSequence) string);
	}

	/**	check if a String is a word
	 * @param	string	the String to be tested
	 * @return	true if and only if the String is a word (consists of letters and in-word punctuation marks only)
	 */
	public static boolean isWord(CharSequence string) {
		if (string == null)
			return false;
		boolean gotLetter = false;
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
//			if ((LETTERS.indexOf(ch) == -1) && ((IN_WORD_PUNCTUATION.indexOf(ch) == -1) || (string.length() == 1) || (c == 0)))
//				return false;
			if (Character.isLetter(ch)) {
				gotLetter = true;
				continue;
			}
			if (IN_WORD_PUNCTUATION.indexOf(ch) == -1)
				return false;
		}
		return gotLetter;
	}
	
	/**
	 * Check if there is a hyphenation between two given words, judging from
	 * morphological evidence. This method return true if (a) both words are
	 * actual words, (b) the first one ends with a hyphen, and (c) the second
	 * one is in lower case and is not a preposition usually found in an
	 * enumeration.
	 * @param firstWord the first word to check
	 * @param secondWord the second word to check
	 * @return true if there is a hyphenation between the two argument words
	 */
	public static boolean areHyphenated(String firstWord, String secondWord) {
		return areHyphenated(((CharSequence) firstWord), ((CharSequence) secondWord));
	}
	
	/**
	 * Check if there is a hyphenation between two given words, judging from
	 * morphological evidence. This method return true if (a) both words are
	 * actual words, (b) the first one ends with a hyphen, and (c) the second
	 * one is in lower case and is not a preposition usually found in an
	 * enumeration.
	 * @param firstWord the first word to check
	 * @param secondWord the second word to check
	 * @return true if there is a hyphenation between the two argument words
	 */
	public static boolean areHyphenated(CharSequence firstWord, CharSequence secondWord) {
		
		//	get and check first word string, including connected predecessors
		if ((firstWord == null) || (firstWord.length() == 0))
			return false;
		if (!isWord(firstWord))
			return false;
		if (DASHES.indexOf(firstWord.charAt(firstWord.length()-1)) == -1)
			return false;
		
		//	check second word string
		if ((secondWord == null) || (secondWord.length() == 0))
			return false;
		if (!isWord(secondWord))
			return false;
		if (secondWord.charAt(0) == Character.toUpperCase(secondWord.charAt(0)))
			return false; // starting with capital letter, not a word continued
		if ("and;or;und;oder;et;ou;y;e;o;u;ed".indexOf(secondWord.toString().toLowerCase()) != -1)
			return false; // rather looks like an enumeration continued than a word (western European languages for now)
		
		//	looking good ...
		return true;
	}
	
	/**
	 * Parse a Roman number.
	 * @param romanNumber the Roman number to parse
	 * @return the int value of the argument Roman number
	 */
	public static int parseRomanNumber(String romanNumber) {
		
		//	normalize raw string
		String nRomanNumber = romanNumber.toLowerCase().trim();
		
		//	prepare set of valid characters
		boolean[] isCharValid = {true, true, true, true, true, true, true};
		
		//	parse number
		int number = 0;
		for (int n = 0; n < nRomanNumber.length(); n++) {
			if (nRomanNumber.startsWith("m", n)) {
				if (!isCharValid[mCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": M cannot occur after " + romanNumber.substring(0, n));
				number += 1000;
			}
			else if (nRomanNumber.startsWith("d", n)) {
				if (!isCharValid[dCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": D cannot occur after " + romanNumber.substring(0, n));
				number += 500;
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("cm", n)) {
				if (!isCharValid[cCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": C cannot occur after " + romanNumber.substring(0, n));
				if (!isCharValid[mCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": M cannot occur after " + romanNumber.substring(0, (n+1)));
				number += 900;
				n++; // need to move on by 2 characters, so supplement loop increment
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("cd", n)) {
				if (!isCharValid[cCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": C cannot occur after " + romanNumber.substring(0, n));
				if (!isCharValid[dCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": D cannot occur after " + romanNumber.substring(0, (n+1)));
				number += 400;
				n++; // need to move on by 2 characters, so supplement loop increment
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("c", n)) {
				if (!isCharValid[cCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": C cannot occur after " + romanNumber.substring(0, n));
				number+= 100;
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("l", n)) {
				if (!isCharValid[lCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": L cannot occur after " + romanNumber.substring(0, n));
				number += 50;
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("xc", n)) {
				if (!isCharValid[xCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": X cannot occur after " + romanNumber.substring(0, n));
				if (!isCharValid[cCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": C cannot occur after " + romanNumber.substring(0, (n+1)));
				number += 90;
				n++; // need to move on by 2 characters, so supplement loop increment
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
				isCharValid[xCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("xl", n)) {
				if (!isCharValid[xCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": X cannot occur after " + romanNumber.substring(0, n));
				if (!isCharValid[lCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": L cannot occur after " + romanNumber.substring(0, (n+1)));
				number += 40;
				n++; // need to move on by 2 characters, so supplement loop increment
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
				isCharValid[xCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("x", n)) {
				if (!isCharValid[xCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": X cannot occur after " + romanNumber.substring(0, n));
				number += 10;
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("v", n)) {
				if (!isCharValid[vCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": V cannot occur after " + romanNumber.substring(0, n));
				number+= 5;
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
				isCharValid[xCharIndex] = false;
				isCharValid[vCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("ix", n)) {
				if (!isCharValid[iCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": I cannot occur after " + romanNumber.substring(0, n));
				if (!isCharValid[xCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": X cannot occur after " + romanNumber.substring(0, (n+1)));
				number += 9;
				n++; // need to move on by 2 characters, so supplement loop increment
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
				isCharValid[xCharIndex] = false;
				isCharValid[vCharIndex] = false;
				isCharValid[iCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("iv", n)) {
				if (!isCharValid[iCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": I cannot occur after " + romanNumber.substring(0, n));
				if (!isCharValid[vCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": V cannot occur after " + romanNumber.substring(0, (n+1)));
				number += 4;
				n++; // need to move on by 2 characters, so supplement loop increment
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
				isCharValid[xCharIndex] = false;
				isCharValid[vCharIndex] = false;
				isCharValid[iCharIndex] = false;
			}
			else if (nRomanNumber.startsWith("i", n)) {
				if (!isCharValid[iCharIndex])
					throw new NumberFormatException("Invalid Roman number " + romanNumber + ": I cannot occur after " + romanNumber.substring(0, n));
				number += 1;
				isCharValid[mCharIndex] = false;
				isCharValid[dCharIndex] = false;
				isCharValid[cCharIndex] = false;
				isCharValid[lCharIndex] = false;
				isCharValid[xCharIndex] = false;
				isCharValid[vCharIndex] = false;
			}
			else throw new NumberFormatException("Invalid Roman number " + romanNumber + ": " + nRomanNumber.substring(n, (n+1)).toUpperCase() + " cannot occur in a Roman number");
		}
		
		//	no value could be parsed, throw exception
		if (number == 0) throw new NumberFormatException("Invalid Roman number " + romanNumber);
		
		//	return number
		else return number;
	}
	private static final int mCharIndex = 0;
	private static final int dCharIndex = 1;
	private static final int cCharIndex = 2;
	private static final int lCharIndex = 3;
	private static final int xCharIndex = 4;
	private static final int vCharIndex = 5;
	private static final int iCharIndex = 6;
	
	/**
	 * Format an Arabic number as a Roman number. The number has to be at least
	 * 1 and at most 4999. If quadrupelOnes is true, 4 is represented as 'IIII'
	 * instead of 'IV', 9 as 'VIIII' instead of 'IX', 40 as 'XXXX' instead of
	 * 'XL', 90 as 'LXXXX' instead of 'XC', 400 as 'CCCC' instead of 'CD', and
	 * 900 as 'DCCCC' instead of 'CM'.
	 * @param number the number to format
	 * @param quadrupelOnes represent 4 as four ones instead of the one-five
	 *            combination
	 * @return the Roman number representation of the argument number
	 */
	public static String asRomanNumber(int number, boolean quadrupelOnes) {
		if (number < 1)
			throw new NumberFormatException("Roman numbers can only be greater-equal 1");
		if (4999 < number)
			throw new NumberFormatException("Roman numbers greater-equal 5000 are impractical");
		
		StringBuffer romanNumber = new StringBuffer();
		while (number >= 1000) {
			romanNumber.append("M");
			number -= 1000;
		}
		
		if (number >= 900) {
			romanNumber.append(quadrupelOnes ? "DCCCC" : "CM");
			number -= 900;
		}
		else if (number >= 500) {
			romanNumber.append("D");
			number -= 500;
		}
		else if (number >= 400) {
			romanNumber.append(quadrupelOnes ? "CCCC" : "CD");
			number -= 400;
		}
		
		while (number >= 100) {
			romanNumber.append("C");
			number -= 100;
		}
		
		if (number >= 90) {
			romanNumber.append(quadrupelOnes ? "LXXXX" : "XC");
			number -= 90;
		}
		else if (number >= 50) {
			romanNumber.append("L");
			number -= 50;
		}
		else if (number >= 40) {
			romanNumber.append(quadrupelOnes ? "XXXX" : "XL");
			number -= 40;
		}
		
		while (number >= 10) {
			romanNumber.append("X");
			number -= 10;
		}
		
		if (number >= 9) {
			romanNumber.append(quadrupelOnes ? "VIIII" : "IX");
			number -= 9;
		}
		else if (number >= 5) {
			romanNumber.append("V");
			number -= 5;
		}
		else if (number >= 4) {
			romanNumber.append(quadrupelOnes ? "IIII" : "IV");
			number -= 4;
		}
		
		while (number >= 1) {
			romanNumber.append("I");
			number -= 1;
		}
		
		return romanNumber.toString();
	}
	
	/**
	 * Retrieve the base character for some diacritic or accented character. If
	 * the argument character is not a diacritic and has no accents, it is
	 * simply returned. This method does not dissolve ligatures and the like;
	 * for that purpose, use the getNormalForm() method.
	 * @param ch the original character
	 * @return the base character
	 */
	public static char getBaseChar(char ch) {
		return getBaseChar(ch, null);
	}
	private static char getBaseChar(char ch, String recurseCharName) {
		if (ch < 128)
			return ch;
		if (SPACES.indexOf(ch) != -1)
			return ' ';
		if (DASHES.indexOf(ch) != -1)
			return '-';
		if (DOUBLE_QUOTES.indexOf(ch) != -1)
			return '"';
		if (SINGLE_QUOTES.indexOf(ch) != -1)
			return '\'';
		if (INVISIBLE_CHARACRES.indexOf(ch) != -1)
			return ' ';
		if (!initCharacters())
			return ch;
		if (selfBasedChars.contains(new Character(ch)))
			return ch;
		if (ch == '\u1E9E')
			return 'S'; // have to check this directly, as there is no PostScript name for this character
		if (ch == '\u00DF')
			return 's'; // check this directly, as PostScript is 'germandbls', not 'ss'
		String[] charNames = getCharNames(ch);
		for (int n = 0; n < charNames.length; n++) {
			if (baseCharMappings.containsKey(charNames[n]))
				return ((Character) baseCharMappings.get(charNames[n])).charValue();
			if ((recurseCharName != null) && (recurseCharName.length() < charNames[n].length()) && charNames[n].startsWith(recurseCharName)) {
//				System.out.println("Skipping (BC) char name '" + charNames[n] + "' (at " + n + ") prefix match with '" + recurseCharName + "'");
				continue;
			}
			for (Iterator cnsit = charNameSuffixes.iterator(); cnsit.hasNext();) {
				String cns = ((String) cnsit.next());
				if (charNames[n].endsWith(cns)) {
					String bcn = charNames[n].substring(0, (charNames[n].length()-cns.length()));
					char bch = getCharForName(bcn);
					if (bch == 0)
						continue;
					else if (bch < 128)
						return bch;
					else if (bch != ch) {
//						System.out.println("Recursing (BC) with '" + bch + "' (" + ((int) bch) + " / " + bcn + ") for char '" + ch + "' (" + ((int) ch) + " / " + charNames[n] + ")");
						return getBaseChar(bch, bcn);
					}
				}
			}
		}
		return ch;
	}
	
	/**
	 * Retrieve the normalized form of some diacritic, ligature, or accented
	 * character. If the argument character is not a diacritic or ligature and
	 * has no accents, it is simply returned. This method dissolves ligatures
	 * and the like; if that effect is undesired, use the getBaseChar() method.
	 * @param ch the original character
	 * @return the normalized form
	 */
	public static String getNormalForm(char ch) {
		return getNormalForm(ch, null);
	}
	private static String getNormalForm(char ch, String recurseCharName) {
		if (ch < 128)
			return ("" + ch);
		if (SPACES.indexOf(ch) != -1)
			return " ";
		if (DASHES.indexOf(ch) != -1)
			return "-";
		if (DOUBLE_QUOTES.indexOf(ch) != -1)
			return "\"";
		if (INVISIBLE_CHARACRES.indexOf(ch) != -1)
			return "";
		if (!initCharacters())
			return ("" + ch);
		if (ligatureMappings.containsKey(new Character(ch)))
			return ((String) ligatureMappings.get(new Character(ch)));
		if (selfBasedChars.contains(new Character(ch)))
			return ("" + ch);
		String[] charNames = getCharNames(ch);
		for (int n = 0; n < charNames.length; n++) {
			if (baseCharMappings.containsKey(charNames[n]))
				return ("" + ((Character) baseCharMappings.get(charNames[n])).charValue());
			if ((recurseCharName != null) && (recurseCharName.length() < charNames[n].length()) && charNames[n].startsWith(recurseCharName)) {
//				System.out.println("Skipping (NF) char name '" + charNames[n] + "' (at " + n + ") prefix match with '" + recurseCharName + "'");
				continue;
			}
			for (Iterator cnsit = charNameSuffixes.iterator(); cnsit.hasNext();) {
				String cns = ((String) cnsit.next());
				if (charNames[n].endsWith(cns)) {
					String bcn = charNames[n].substring(0, (charNames[n].length()-cns.length()));
					char bch = getCharForName(bcn);
					if (bch == 0)
						continue;
					else if (bch < 128)
						return ("" + bch);
					else if (bch != ch) {
//						System.out.println("Recursing (NF) with '" + bch + " (" + ((int) bch) + " / " + bcn + ")' for char '" + ch + "' (" + ((int) ch) + " / " + charNames[n] + ")");
						return getNormalForm(bch, bcn);
					}
				}
			}
		}
		return ("" + ch);
	}
	
	/**
	 * Obtain an iterator over all PostScript character names. The iterator
	 * returns the character names in no particular order, as the backing map is
	 * hash based. Further, the iterator does not support any modifications and
	 * will throw an UnsupportedOperationException for any attempt to use them.
	 * @return an iterator over all PostScript character names
	 */
	public static StringIterator getCharNameIterator() {
		if (!initCharacters())
			return null;
		final Iterator cnit = charNamesToChars.keySet().iterator();
		return new StringIterator() {
			public boolean hasNext() {
				return cnit.hasNext();
			}
			public boolean hasMoreStrings() {
				return this.hasNext();
			}
			public Object next() {
				return cnit.next();
			}
			public String nextString() {
				return ((String) this.next());
			}
			public void remove() {
				throw new UnsupportedOperationException("remove() is not supported.");
			}
		};
	}
	
	/**
	 * Resolve a PostScript character name to the respective char. For the
	 * PostScript characters names that can map to more than one char, this
	 * method always returns the first char, i.e., the numerically smalles one.
	 * To retrieve all possible chars, use the getCharsForName() method. If the
	 * character name is unknown, this method returns 0.
	 * @param charName the character name to resolve
	 * @return the char the character name maps to
	 */
	public static char getCharForName(String charName) {
		if ((charName == null) || (charName.length() == 0))
			return 0;
		if (charName.length() == 1)
			return charName.charAt(0);
		if (!initCharacters())
			return 0;
		Object co = charNamesToChars.get(charName);
		if (co instanceof Character)
			return ((Character) co).charValue();
		else if (co == null)
			return 0;
		else return ((Character[]) co)[0].charValue();
	}
	
	/**
	 * Resolve a PostScript character name to the respective chars. If the
	 * character name is unknown, this method returns an empty array, but never
	 * null.
	 * @param charName the character name to resolve
	 * @return an array holding the chars the character name maps to
	 */
	public static char[] getCharsForName(String charName) {
		if ((charName == null) || (charName.length() == 0))
			return new char[0];
		if (charName.length() == 1) {
			char[] chs = {charName.charAt(0)};
			return chs;
		}
		if (!initCharacters())
			return new char[0];
		Object cso = charNamesToChars.get(charName);
		if (cso instanceof Character) {
			char[] chs = {((Character) cso).charValue()};
			return chs;
		}
		else if (cso == null)
			return new char[0];
		else {
			char[] chs = new char[((Character[]) cso).length];
			for (int c = 0; c < chs.length; c++)
				chs[c] = ((Character[]) cso)[c].charValue();
			return chs;
		}
	}
	
	/**
	 * Retrieve the PostScript character name for a given char. For the chars
	 * the have multiple names in PostScript, this method returns the first
	 * character name. To retrieve all character names, use the getCharNames()
	 * method. If the argument char does not have a name in PostScript, this
	 * method returns null.
	 * @param ch the char to find the name for
	 * @return the PostScript name of the char
	 */
	public static String getCharName(char ch) {
		if (!initCharacters())
			return null;
		Object cno = charsToCharNames.get(new Character(ch));
		if (cno instanceof String)
			return ((String) cno);
		else if (cno == null)
			return null;
		else return ((String[]) cno)[0];
	}
	
	/**
	 * Retrieve all possible PostScript character names for a given char. If the
	 * argument char does not have a name in PostScript, this method returns
	 * an empty array, but never null.
	 * @param ch the char to find the name for
	 * @return an array holding all PostScript names of the char
	 */
	public static String[] getCharNames(char ch) {
		if (!initCharacters())
			return new String[0];
		Object cnso = charsToCharNames.get(new Character(ch));
		if (cnso == null)
			return new String[0];
		else if (cnso instanceof String) {
			String[] cns = {(String) cnso};
			return cns;
		}
		else {
			String[] cns = new String[((String[]) cnso).length];
			System.arraycopy(((String[]) cnso), 0, cns, 0, cns.length);
			Arrays.sort(cns);
			return cns;
		}
	}
	
	/**
	 * Normalize a the spaces in a string. This method replaces any visible
	 * spaces with the <code>0x0020</code> ASCII space, and also removes any
	 * invisible characters (e.g. zero-width spaces). However, this method is
	 * <b>not</b> a replacement for <code>java.lang.String.trim()</code>, as it
	 * does not remove any leading or tailing spaces altogether.
	 * @param str the string to space-normalize
	 * @return the space-normalized string
	 */
	public static String normalizeSpaces(String str) {
		if (str == null)
			return null;
		StringBuffer nStr = new StringBuffer();
		for (int c = 0; c < str.length(); c++) {
			char ch = str.charAt(c);
			if (ch <= ' ')
				nStr.append(' ');
			else if (SPACES.indexOf(ch) != -1)
				nStr.append(' ');
			else if (INVISIBLE_CHARACRES.indexOf(ch) != -1) { /* simply ignore invisible characters */ }
			else nStr.append(ch);
		}
		return nStr.toString();
	}
	
	/**
	 * Normalize a whole string. This method is essentially a shorthand for
	 * calling <code>getNormalForm()</code> on each individual character in the
	 * argument string. On top of that, this method also normalizes any spaces.
	 * @param str the string to normalize
	 * @return the normalized string
	 */
	public static String normalizeString(String str) {
		if (str == null)
			return null;
		StringBuffer nStr = new StringBuffer();
		for (int c = 0; c < str.length(); c++) {
			char ch = str.charAt(c);
			if (DASHES.indexOf(ch) != -1)
				nStr.append('-');
			else if (ch <= ' ') // <= 0x20
				nStr.append(' ');
			else if (SPACES.indexOf(ch) != -1)
				nStr.append(' ');
			else if ((0x0300 <= ch) && (ch <= 0x036F)) { /* simply ignore combining diacritic markers (we're here to get rid of those) */ }
			else if (ch == 0x7F) { /* ignore 'delete' character */ }
			else if (INVISIBLE_CHARACRES.indexOf(ch) != -1) { /* simply ignore invisible characters */ }
			else nStr.append(getNormalForm(ch));
		}
		return nStr.toString();
	}
	
	private static boolean doneInitCharacters = false;
	private static HashMap charNamesToChars = new HashMap();
	private static HashMap charsToCharNames = new HashMap();
	private static TreeSet charNameSuffixes = new TreeSet(new Comparator() {
		public int compare(Object o1, Object o2) {
			String cns1 = ((String) o1);
			String cns2 = ((String) o2);
			return ((cns1.length() == cns2.length()) ? cns1.compareTo(cns2) : (cns2.length() - cns1.length()));
		}
	});
	private static HashSet selfBasedChars = new HashSet();
	private static HashMap baseCharMappings = new HashMap();
	private static HashMap ligatureMappings = new HashMap();
	private static synchronized boolean initCharacters() {
		if (!doneInitCharacters) try {
			doInitCharacters();
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return doneInitCharacters;
	}
	
	private static synchronized void doInitCharacters() throws IOException {
		System.out.println("StringUtils: initializing PostScript character mappings ...");
		
		//	index characters by character names and vice versa
		String sucrn = StringUtils.class.getName().replaceAll("\\.", "/");
		InputStream cmis = StringUtils.class.getClassLoader().getResourceAsStream(sucrn.substring(0, sucrn.lastIndexOf('/')) + "/PostscriptUnicodeGlyphList.txt");
		BufferedReader cmr = new BufferedReader(new InputStreamReader(cmis, "UTF-8"));
		HashMap charsToCharNamesL = new HashMap();
		for (String cml; (cml = cmr.readLine()) != null;) {
			if (cml.startsWith("#"))
				continue;
			String[] cm = cml.split("\\;");
			if (cm.length != 2)
				continue;
			if (cm[0].startsWith("afii") || cm[0].startsWith("SF") || cm[0].matches(".*[0-9]+.*"))
				continue;
			if (cm[1].indexOf(' ') == -1) {
				Character ch = new Character((char) Integer.parseInt(cm[1], 16));
				charNamesToChars.put(cm[0], ch);
				ArrayList charNames = ((ArrayList) charsToCharNamesL.get(ch));
				if (charNames == null) {
					charNames = new ArrayList(1);
					charsToCharNamesL.put(ch, charNames);
				}
				charNames.add(cm[0]);
			}
			else {
				String[] ccs = cm[1].split("\\s+");
				Character[] chs = new Character[ccs.length];
				for (int c = 0; c < ccs.length; c++) {
					Character ch = new Character((char) Integer.parseInt(ccs[c], 16));
					chs[c] = ch;
					ArrayList charNames = ((ArrayList) charsToCharNamesL.get(ch));
					if (charNames == null) {
						charNames = new ArrayList(1);
						charsToCharNamesL.put(ch, charNames);
					}
					charNames.add(cm[0]);
				}
				Arrays.sort(chs);
				charNamesToChars.put(cm[0], chs);
			}
		}
		cmr.close();
		for (Iterator cit = charsToCharNamesL.keySet().iterator(); cit.hasNext();) {
			Character ch = ((Character) cit.next());
			ArrayList cns = ((ArrayList) charsToCharNamesL.get(ch));
			if (cns.size() == 1)
				charsToCharNames.put(ch, cns.get(0));
			else charsToCharNames.put(ch, cns.toArray(new String[cns.size()]));
		}
		
		//	find and index modifier suffixes
		TreeMap charNamesBySuffixes = new TreeMap();
		for (Iterator cnit = charNamesToChars.keySet().iterator(); cnit.hasNext();) {
			String cn = ((String) cnit.next());
			if (cn.length() < 2)
				continue;
			Object chs = charNamesToChars.get(cn);
			if ((chs instanceof Character) && ((Character) chs).charValue() < 128)
				continue;
			for (int e = 1; e < cn.length(); e++) {
				String cnp = cn.substring(0, e);
				if (charNamesToChars.containsKey(cnp)) {
					String cns = cn.substring(e);
					ArrayList suffixCharNames = ((ArrayList) charNamesBySuffixes.get(cns));
					if (suffixCharNames == null) {
						suffixCharNames = new ArrayList(3);
						charNamesBySuffixes.put(cns, suffixCharNames);
					}
					suffixCharNames.add(cnp);
				}
			}
		}
		for (Iterator cnsit = charNamesBySuffixes.keySet().iterator(); cnsit.hasNext();) {
			String cns = ((String) cnsit.next());
			if (cns.endsWith("roman") && ("roman".length() < cns.length()))
				continue;
			if (cns.endsWith("equal"))
				continue;
			if (cns.endsWith("greater"))
				continue;
			if (cns.endsWith("less"))
				continue;
//			if (cns.endsWith("cyrillic") && ("cyrillic".length() < cns.length()))
//				continue;
//			if (cns.endsWith("armenian") && ("armenian".length() < cns.length()))
//				continue;
//			if (cns.endsWith("hebrew") && ("hebrew".length() < cns.length()))
//				continue;
//			if (cns.endsWith("gurmukhi") && ("gurmukhi".length() < cns.length()))
//				continue;
//			if (cns.endsWith("bengali") && ("bengali".length() < cns.length()))
//				continue;
//			if (cns.endsWith("latin") && ("latin".length() < cns.length()))
//				continue;
//			if (cns.endsWith("african") && ("african".length() < cns.length()))
//				continue;
//			if (cns.endsWith("arabic") && ("arabic".length() < cns.length()))
//				continue;
//			if (cns.endsWith("dagesh") && ("dagesh".length() < cns.length()))
//				continue;
//			if (cns.endsWith("korean") && ("korean".length() < cns.length()))
//				continue;
			ArrayList suffixCharNames = ((ArrayList) charNamesBySuffixes.get(cns));
			if (suffixCharNames.size() < 2)
				continue;
			else if (suffixCharNames.size() == 2) {
				String scn1 = ((String) suffixCharNames.get(0));
				String scn2 = ((String) suffixCharNames.get(1));
				if (scn1.equalsIgnoreCase(scn2))
					continue;
			}
			if (cns.length() > 1) {
				ArrayList sSuffixCharNames = ((ArrayList) charNamesBySuffixes.get(cns.substring(1)));
				if ((sSuffixCharNames != null) && (sSuffixCharNames.size() > suffixCharNames.size())) {
					boolean gotAll = true;
					for (int s = 0; gotAll && (s < suffixCharNames.size()); s++) {
						String scn = (suffixCharNames.get(s) + cns.substring(0, 1));
						gotAll = sSuffixCharNames.contains(scn);
					}
					if (gotAll) {
//						System.out.println("Skipping suffix: " + cns + " - " + suffixCharNames);
//						System.out.println(" Reason: " + cns.substring(1) + " - " + sSuffixCharNames);
						continue;
					}
				}
			}
			charNameSuffixes.add(cns);
//			System.out.println("Suffix: " + cns + " - " + suffixCharNames);
		}
		
		//	initialize self-based chars, including Greek chars
		selfBasedChars.add(new Character('\u00B5')); // micro sign, ANSI mu
		selfBasedChars.add(new Character('\u00B0')); // degree symbol
		for (int gc = 945; gc <= 969; gc++) // lower case Greek character range
			selfBasedChars.add(new Character((char) gc));
		for (int gc = 913; gc <= 937; gc++) // upper case Greek character range
			selfBasedChars.add(new Character((char) gc));
		//	TODO extend this (as the need arises, e.g. for Cyrillic)
		
		//	initialize custom base character mappings
		baseCharMappings.put("Eng", new Character('N'));
		baseCharMappings.put("eng", new Character('n'));
		baseCharMappings.put("Eth", new Character('D'));
		baseCharMappings.put("eth", new Character('d'));
		baseCharMappings.put("quoterightn", new Character('n'));
		baseCharMappings.put("napostrophe", new Character('n'));
		for (Iterator cnit = charNamesToChars.keySet().iterator(); cnit.hasNext();) {
			String cn = ((String) cnit.next());
			if (baseCharMappings.containsKey(cn))
				continue;
			if (cn.startsWith("quotedbl"))
				baseCharMappings.put(cn, new Character('"'));
			else if (cn.startsWith("quote"))
				baseCharMappings.put(cn, new Character('\''));
		}
		baseCharMappings.put("sfthyphen", new Character('-'));
		baseCharMappings.put("hyphentwo", new Character('-'));
		baseCharMappings.put("figuredash", new Character('-'));
		baseCharMappings.put("endash", new Character('-'));
		baseCharMappings.put("emdash", new Character('-'));
		baseCharMappings.put("minus", new Character('-'));
		baseCharMappings.put("horizontalbar", new Character('-'));
		baseCharMappings.put("openbullet", new Character('\u00B0'));
		baseCharMappings.put("minute", new Character('\''));
		baseCharMappings.put("guillemotleft", new Character('"'));
		baseCharMappings.put("guillemotright", new Character('"'));
		baseCharMappings.put("guilsinglleft", new Character('"'));
		baseCharMappings.put("guilsinglright", new Character('"'));
		baseCharMappings.put("dotlessi", new Character('i'));
		baseCharMappings.put("dotlessj", new Character('j'));
		baseCharMappings.put("germandbls", new Character('s'));
		baseCharMappings.put("longs", new Character('s'));
		
		//	initialize ligatures, including Roman numbers
		mapSuffixLigatures("paren", ")");
		mapSuffixLigatures("period", ".");
		mapRomanNumberLigatures();
		mapLetterLigatures();
		mapLetterLigature("quoterightn", "'n", false, false, true);
		mapLetterLigature("napostrophe", "n'", false, false, true);
		ligatureMappings.put(new Character('\u00DF'), "ss"); // have to map this directly, as PostScript name is 'germandbls', not 'ss'
		ligatureMappings.put(new Character('\u1E9E'), "SS"); // have to map this directly, as there is no PostScript name
		
		System.out.println("StringUtils: PostScript character mappings initialized.");
		doneInitCharacters = true;
	}
	
	private static void mapLetterLigatures() {
		mapLetterLigature("AE", "AE", true, false, true);
		mapLetterLigature("OE", "OE", true, false, true);
		
		mapLetterLigature("IJ", "IJ", true, false, true);
		
		mapLetterLigature("HV", "HV", false, false, true);
		mapLetterLigature("OI", "OI", false, true, true);
		mapLetterLigature("TS", "TS", false, false, true);
		
		mapLetterLigature("LJ", "LJ", true, true, true);
		mapLetterLigature("LL", "LL", true, false, true);
		mapLetterLigature("NJ", "NJ", true, true, true);
		mapLetterLigature("DZ", "DZ", true, true, true);
		
		mapLetterLigature("FF", "FF", false, false, true);
		mapLetterLigature("FFI", "FFI", false, false, true);
		mapLetterLigature("FFL", "FFL", false, false, true);
		mapLetterLigature("FI", "FI", false, false, true);
		mapLetterLigature("FL", "FL", false, false, true);
	}
	
	private static void mapLetterLigature(String charName, String chars, boolean upper, boolean mixed, boolean lower) {
		if (upper)
			mapLigature(charName.toUpperCase(), chars.toUpperCase());
		if (mixed)
			mapLigature(capitalize(charName), capitalize(chars));
		if (lower)
			mapLigature(charName.toLowerCase(), chars.toLowerCase());
	}
	
	private static void mapRomanNumberLigatures() {
		mapRomanNumberLigature("Oneroman", "I");
		mapRomanNumberLigature("Tworoman", "II");
		mapRomanNumberLigature("Threeroman", "III");
		mapRomanNumberLigature("Fourroman", "IV");
		mapRomanNumberLigature("Fiveroman", "V");
		mapRomanNumberLigature("Sixroman", "VI");
		mapRomanNumberLigature("Sevenroman", "VII");
		mapRomanNumberLigature("Eightroman", "VIII");
		mapRomanNumberLigature("Nineroman", "IX");
		mapRomanNumberLigature("Tenroman", "X");
		mapRomanNumberLigature("Elevenroman", "XI");
		mapRomanNumberLigature("Twelveroman", "XII");
	}
	
	private static void mapRomanNumberLigature(String charName, String chars) {
		mapLigature(charName, chars);
		mapLigature(charName.toLowerCase(), chars.toLowerCase());
	}
	
	private static void mapLigature(String charName, String chars) {
		Object co = charNamesToChars.get(charName);
		Character ch = null;
		if (co instanceof Character)
			ch = ((Character) co);
		else if (co instanceof Character[])
			ch = ((Character[]) co)[0];
		else return;
//		System.out.println("Mapping ligature " + charName + " to '" + chars + "'");
		ligatureMappings.put(ch, chars);
	}
	
//	private static void mapPrefixLigatures(String suffix, String suffixChar) {
//		for (Iterator cnit = charNamesToChars.keySet().iterator(); cnit.hasNext();) {
//			String cn = ((String) cnit.next());
//			if (cn.endsWith(suffix)) {
//				String fcn = cn.substring(0, (cn.length() - suffix.length()));
//				char fc = 0;
//				if (fcn.length() == 1)
//					fc = fcn.charAt(0);
//				else if (charNamesToChars.containsKey(fcn)) {
//					Object fco = charNamesToChars.get(fcn);
//					if (fco instanceof Character)
//						fc = ((Character) fco).charValue();
//				}
//				if (fc != 0)
//					mapLigature(cn, ("" + fc + suffixChar));
//			}
//		}
//	}
//	
	private static void mapSuffixLigatures(String suffix, String suffixChar) {
		for (Iterator cnit = charNamesToChars.keySet().iterator(); cnit.hasNext();) {
			String cn = ((String) cnit.next());
			if (cn.endsWith(suffix)) {
				String fcn = cn.substring(0, (cn.length() - suffix.length()));
				char fc = 0;
				if (fcn.length() == 1)
					fc = fcn.charAt(0);
				else if (charNamesToChars.containsKey(fcn)) {
					Object fco = charNamesToChars.get(fcn);
					if (fco instanceof Character)
						fc = ((Character) fco).charValue();
				}
				if (fc != 0)
					mapLigature(cn, ("" + fc + suffixChar));
			}
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		for (int c = 33; c < 65536; c++) {
//			char ch = ((char) c);
//			if (!Character.isValidCodePoint(c))
//				continue;
//			String chn = getCharName(ch);
//			if (chn == null)
//				continue;
//			char bch = getBaseChar(ch);
//			System.out.println("Got base char for '" + ch + "' (" + c + " / " + chn + "): '" + bch + "' (" + ((int) bch) + "), normal form is '" + getNormalForm(ch) + "'");
//		}
//	}
//	
	/**
	 * Latinize a string. This method replaces Greek and Cyrillic homoglyphs of
	 * Latin characters with the corresponding Latin forms. This method retains
	 * any diacritic markers, use <code>normalizeString()</code> to remove the
	 * latter from the results of this method.<br/>
	 * <b>Beware:</b> using this method on non-Latin text might be destructive.
	 * @param str the string to latinize
	 * @return the latinized string
	 */
	public static String latinizeString(String str) {
		if (str == null)
			return null;
		StringBuffer lStr = new StringBuffer();
		for (int c = 0; c < str.length(); c++)
			lStr.append(latinize(str.charAt(c)));
		return lStr.toString();
	}
	
	private static char latinize(char ch) {
		int low = 0;
		int high = ((latinizationMappings.length / 2) - 1);
		while (low <= high) {
			int mid = ((low + high) / 2);
			char cch = latinizationMappings[mid * 2]; // binary search over even indices ...
			if (cch < ch)
				low = (mid + 1);
			else if (cch > ch)
				high = (mid - 1);
			else return latinizationMappings[(mid * 2) + 1]; // ... returning subsequent odd index on match
		}
		return ch;
	}
	private static final char[] latinizationMappings = {
		//	Greek
		'\u0386', 'A', // GREEK CAPITAL LETTER ALPHA WITH TONOS
		'\u0388', 'E', // GREEK CAPITAL LETTER EPSILON WITH TONOS
		'\u0389', 'H', // GREEK CAPITAL LETTER ETA WITH TONOS
		'\u038A', 'I', // GREEK CAPITAL LETTER IOTA WITH TONOS
		'\u038C', 'O', // GREEK CAPITAL LETTER OMICRON WITH TONOS
		'\u038E', 'Y', // GREEK CAPITAL LETTER UPSILON WITH TONOS
		'\u0390', 'I', // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
		'\u0391', 'A', // GREEK CAPITAL LETTER ALPHA
		'\u0392', 'B', // GREEK CAPITAL LETTER BETA
		'\u0395', 'E', // GREEK CAPITAL LETTER EPSILON
		'\u0396', 'Z', // GREEK CAPITAL LETTER ZETA
		'\u0397', 'H', // GREEK CAPITAL LETTER ETA
		'\u0398', 'O', // GREEK CAPITAL LETTER THETA
		'\u0399', 'I', // GREEK CAPITAL LETTER IOTA
		'\u039A', 'K', // GREEK CAPITAL LETTER KAPPA
		'\u039C', 'M', // GREEK CAPITAL LETTER MU
		'\u039D', 'N', // GREEK CAPITAL LETTER NU
		'\u039F', 'O', // GREEK CAPITAL LETTER OMICRON
		'\u03A1', 'P', // GREEK CAPITAL LETTER RHO
		'\u03A4', 'T', // GREEK CAPITAL LETTER TAU
		'\u03A5', 'Y', // GREEK CAPITAL LETTER UPSILON
		'\u03A7', 'X', // GREEK CAPITAL LETTER CHI
		'\u03AA', '\u00CF', // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA (mapped to I with dieresis)
		'\u03AB', '\u0178', // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA (mapped to Y with dieresis)
		'\u03AC', '\u00E1', // GREEK SMALL LETTER ALPHA WITH TONOS (mapped to a with acute)
		'\u03AF', '\u00ED', // GREEK SMALL LETTER IOTA WITH TONOS (mapped to i with acute)
		'\u03B1', 'a', // GREEK SMALL LETTER ALPHA
		'\u03B2', 'b', // GREEK SMALL LETTER BETA
		'\u03B3', 'y', // GREEK SMALL LETTER GAMMA
		'\u03BA', 'k', // GREEK SMALL LETTER KAPPA
		'\u03BD', 'v', // GREEK SMALL LETTER NU
		'\u03BF', 'o', // GREEK SMALL LETTER OMICRON
		'\u03C1', 'p', // GREEK SMALL LETTER RHO
		'\u03C5', 'u', // GREEK SMALL LETTER UPSILON
		'\u03C7', 'x', // GREEK SMALL LETTER CHI
		'\u03CA', '\u00EF', // GREEK SMALL LETTER IOTA WITH DIALYTIKA (mapped to i with dieresis)
		'\u03CB', '\u00FC', // GREEK SMALL LETTER UPSILON WITH DIALYTIKA (mapped to u with dieresis)
		'\u03CC', '\u00F3', // GREEK SMALL LETTER OMICRON WITH TONOS (mapped to o with acute)
		'\u03CD', '\u00FA', // GREEK SMALL LETTER UPSILON WITH TONOS (mapped to u with acute)
		'\u03CE', 'w', // GREEK SMALL LETTER OMEGA WITH TONOS
		'\u03D2', 'Y', // GREEK UPSILON WITH HOOK SYMBOL
		'\u03D3', 'Y', // GREEK UPSILON WITH ACUTE AND HOOK SYMBOL
		'\u03D4', '\u0178', // GREEK UPSILON WITH DIAERESIS AND HOOK SYMBOL (mapped to Y with dieresis)

		//	Cyrillic
		'\u0400', '\u00C8', // CYRILLIC CAPITAL LETTER IE WITH GRAVE (mapped to E with grave)
		'\u0401', '\u00CB', // CYRILLIC CAPITAL LETTER IO (mapped to E with dieresis)
		'\u0405', 'S', // CYRILLIC CAPITAL LETTER DZE
		'\u0406', 'I', // CYRILLIC CAPITAL LETTER BYELORUSSIAN-UKRAINIAN I
		'\u0407', '\u00CF', // CYRILLIC CAPITAL LETTER YI (mapped to I with dieresis)
		'\u0408', 'J', // CYRILLIC CAPITAL LETTER JE
		'\u0410', 'A', // CYRILLIC CAPITAL LETTER A
		'\u0412', 'B', // CYRILLIC CAPITAL LETTER VE
		'\u0415', 'E', // CYRILLIC CAPITAL LETTER IE
		'\u041A', 'K', // CYRILLIC CAPITAL LETTER KA
		'\u041C', 'M', // CYRILLIC CAPITAL LETTER EM
		'\u041D', 'H', // CYRILLIC CAPITAL LETTER EN
		'\u041E', 'O', // CYRILLIC CAPITAL LETTER O
		'\u0420', 'P', // CYRILLIC CAPITAL LETTER ER
		'\u0421', 'C', // CYRILLIC CAPITAL LETTER ES
		'\u0422', 'T', // CYRILLIC CAPITAL LETTER TE
		'\u0423', 'Y', // CYRILLIC CAPITAL LETTER U
		'\u0425', 'X', // CYRILLIC CAPITAL LETTER HA
		'\u0430', 'a', // CYRILLIC SMALL LETTER A
		'\u0432', 'B', // CYRILLIC SMALL LETTER VE
		'\u0433', 'r', // CYRILLIC SMALL LETTER GHE
		'\u0435', 'e', // CYRILLIC SMALL LETTER IE
		'\u043A', 'k', // CYRILLIC SMALL LETTER KA
		'\u043C', 'm', // CYRILLIC SMALL LETTER EM
		'\u043D', 'h', // CYRILLIC SMALL LETTER EN
		'\u043E', 'o', // CYRILLIC SMALL LETTER O
		'\u043F', 'n', // CYRILLIC SMALL LETTER PE
		'\u0440', 'p', // CYRILLIC SMALL LETTER ER
		'\u0441', 'c', // CYRILLIC SMALL LETTER ES
		'\u0443', 'y', // CYRILLIC SMALL LETTER U
		'\u0445', 'x', // CYRILLIC SMALL LETTER HA
		'\u044A', 'b', // CYRILLIC SMALL LETTER HARD SIGN
		'\u044C', 'b', // CYRILLIC SMALL LETTER SOFT SIGN
		'\u0450', '\u00E8', // CYRILLIC SMALL LETTER IE WITH GRAVE (mapped to e with grave)
		'\u0451', '\u00EB', // CYRILLIC SMALL LETTER IO (mapped to e with dieresis)
		'\u0455', 's', // CYRILLIC SMALL LETTER DZE
		'\u0456', 'i', // CYRILLIC SMALL LETTER BYELORUSSIAN-UKRAINIAN I
		'\u0457', '\u00EF', // CYRILLIC SMALL LETTER YI (mapped to i with dieresis)
		'\u0458', 'j', // CYRILLIC SMALL LETTER JE
		'\u0461', 'w', // CYRILLIC SMALL LETTER OMEGA
		'\u047F', 'w', // CYRILLIC SMALL LETTER OT
	};
//	
//	private static HashMap latinizationMappings = new HashMap();
//	static {
//		//	Cyrillic
//		latinizationMappings.put(new Character('\u0400'), new Character('\u00C8')); // CYRILLIC CAPITAL LETTER IE WITH GRAVE (mapped to E with grave)
//		latinizationMappings.put(new Character('\u0401'), new Character('\u00CB')); // CYRILLIC CAPITAL LETTER IO (mapped to E with dieresis)
//		latinizationMappings.put(new Character('\u0405'), new Character('S')); // CYRILLIC CAPITAL LETTER DZE
//		latinizationMappings.put(new Character('\u0406'), new Character('I')); // CYRILLIC CAPITAL LETTER BYELORUSSIAN-UKRAINIAN I
//		latinizationMappings.put(new Character('\u0407'), new Character('\u00CF')); // CYRILLIC CAPITAL LETTER YI (mapped to I with dieresis)
//		latinizationMappings.put(new Character('\u0408'), new Character('J')); // CYRILLIC CAPITAL LETTER JE
//		latinizationMappings.put(new Character('\u0410'), new Character('A')); // CYRILLIC CAPITAL LETTER A
//		latinizationMappings.put(new Character('\u0412'), new Character('B')); // CYRILLIC CAPITAL LETTER VE
//		latinizationMappings.put(new Character('\u0415'), new Character('E')); // CYRILLIC CAPITAL LETTER IE
//		latinizationMappings.put(new Character('\u041A'), new Character('K')); // CYRILLIC CAPITAL LETTER KA
//		latinizationMappings.put(new Character('\u041C'), new Character('M')); // CYRILLIC CAPITAL LETTER EM
//		latinizationMappings.put(new Character('\u041D'), new Character('H')); // CYRILLIC CAPITAL LETTER EN
//		latinizationMappings.put(new Character('\u041E'), new Character('O')); // CYRILLIC CAPITAL LETTER O
//		latinizationMappings.put(new Character('\u0420'), new Character('P')); // CYRILLIC CAPITAL LETTER ER
//		latinizationMappings.put(new Character('\u0421'), new Character('C')); // CYRILLIC CAPITAL LETTER ES
//		latinizationMappings.put(new Character('\u0422'), new Character('T')); // CYRILLIC CAPITAL LETTER TE
//		latinizationMappings.put(new Character('\u0423'), new Character('Y')); // CYRILLIC CAPITAL LETTER U
//		latinizationMappings.put(new Character('\u0425'), new Character('X')); // CYRILLIC CAPITAL LETTER HA
//		latinizationMappings.put(new Character('\u0430'), new Character('a')); // CYRILLIC SMALL LETTER A
//		latinizationMappings.put(new Character('\u0432'), new Character('B')); // CYRILLIC SMALL LETTER VE
//		latinizationMappings.put(new Character('\u0433'), new Character('r')); // CYRILLIC SMALL LETTER GHE
//		latinizationMappings.put(new Character('\u0435'), new Character('e')); // CYRILLIC SMALL LETTER IE
//		latinizationMappings.put(new Character('\u043A'), new Character('k')); // CYRILLIC SMALL LETTER KA
//		latinizationMappings.put(new Character('\u043C'), new Character('m')); // CYRILLIC SMALL LETTER EM
//		latinizationMappings.put(new Character('\u043D'), new Character('h')); // CYRILLIC SMALL LETTER EN
//		latinizationMappings.put(new Character('\u043E'), new Character('o')); // CYRILLIC SMALL LETTER O
//		latinizationMappings.put(new Character('\u043F'), new Character('n')); // CYRILLIC SMALL LETTER PE
//		latinizationMappings.put(new Character('\u0440'), new Character('p')); // CYRILLIC SMALL LETTER ER
//		latinizationMappings.put(new Character('\u0441'), new Character('c')); // CYRILLIC SMALL LETTER ES
//		latinizationMappings.put(new Character('\u0443'), new Character('y')); // CYRILLIC SMALL LETTER U
//		latinizationMappings.put(new Character('\u0445'), new Character('x')); // CYRILLIC SMALL LETTER HA
//		latinizationMappings.put(new Character('\u044A'), new Character('b')); // CYRILLIC SMALL LETTER HARD SIGN
//		latinizationMappings.put(new Character('\u044C'), new Character('b')); // CYRILLIC SMALL LETTER SOFT SIGN
//		latinizationMappings.put(new Character('\u0450'), new Character('\u00E8')); // CYRILLIC SMALL LETTER IE WITH GRAVE (mapped to e with grave)
//		latinizationMappings.put(new Character('\u0451'), new Character('\u00EB')); // CYRILLIC SMALL LETTER IO (mapped to e with dieresis)
//		latinizationMappings.put(new Character('\u0455'), new Character('s')); // CYRILLIC SMALL LETTER DZE
//		latinizationMappings.put(new Character('\u0456'), new Character('i')); // CYRILLIC SMALL LETTER BYELORUSSIAN-UKRAINIAN I
//		latinizationMappings.put(new Character('\u0457'), new Character('\u00EF')); // CYRILLIC SMALL LETTER YI (mapped to i with dieresis)
//		latinizationMappings.put(new Character('\u0458'), new Character('j')); // CYRILLIC SMALL LETTER JE
//		latinizationMappings.put(new Character('\u0461'), new Character('w')); // CYRILLIC SMALL LETTER OMEGA
//		latinizationMappings.put(new Character('\u047F'), new Character('w')); // CYRILLIC SMALL LETTER OT
//
//		//	Greek
//		latinizationMappings.put(new Character('\u0386'), new Character('A')); // GREEK CAPITAL LETTER ALPHA WITH TONOS
//		latinizationMappings.put(new Character('\u0388'), new Character('E')); // GREEK CAPITAL LETTER EPSILON WITH TONOS
//		latinizationMappings.put(new Character('\u0389'), new Character('H')); // GREEK CAPITAL LETTER ETA WITH TONOS
//		latinizationMappings.put(new Character('\u038A'), new Character('I')); // GREEK CAPITAL LETTER IOTA WITH TONOS
//		latinizationMappings.put(new Character('\u038C'), new Character('O')); // GREEK CAPITAL LETTER OMICRON WITH TONOS
//		latinizationMappings.put(new Character('\u038E'), new Character('Y')); // GREEK CAPITAL LETTER UPSILON WITH TONOS
//		latinizationMappings.put(new Character('\u0390'), new Character('I')); // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
//		latinizationMappings.put(new Character('\u0391'), new Character('A')); // GREEK CAPITAL LETTER ALPHA
//		latinizationMappings.put(new Character('\u0392'), new Character('B')); // GREEK CAPITAL LETTER BETA
//		latinizationMappings.put(new Character('\u0395'), new Character('E')); // GREEK CAPITAL LETTER EPSILON
//		latinizationMappings.put(new Character('\u0396'), new Character('Z')); // GREEK CAPITAL LETTER ZETA
//		latinizationMappings.put(new Character('\u0397'), new Character('H')); // GREEK CAPITAL LETTER ETA
//		latinizationMappings.put(new Character('\u0398'), new Character('O')); // GREEK CAPITAL LETTER THETA
//		latinizationMappings.put(new Character('\u0399'), new Character('I')); // GREEK CAPITAL LETTER IOTA
//		latinizationMappings.put(new Character('\u039A'), new Character('K')); // GREEK CAPITAL LETTER KAPPA
//		latinizationMappings.put(new Character('\u039C'), new Character('M')); // GREEK CAPITAL LETTER MU
//		latinizationMappings.put(new Character('\u039D'), new Character('N')); // GREEK CAPITAL LETTER NU
//		latinizationMappings.put(new Character('\u039F'), new Character('O')); // GREEK CAPITAL LETTER OMICRON
//		latinizationMappings.put(new Character('\u03A1'), new Character('P')); // GREEK CAPITAL LETTER RHO
//		latinizationMappings.put(new Character('\u03A4'), new Character('T')); // GREEK CAPITAL LETTER TAU
//		latinizationMappings.put(new Character('\u03A5'), new Character('Y')); // GREEK CAPITAL LETTER UPSILON
//		latinizationMappings.put(new Character('\u03A7'), new Character('X')); // GREEK CAPITAL LETTER CHI
//		latinizationMappings.put(new Character('\u03AA'), new Character('\u00CF')); // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA (mapped to I with dieresis)
//		latinizationMappings.put(new Character('\u03AB'), new Character('\u0178')); // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA (mapped to Y with dieresis)
//		latinizationMappings.put(new Character('\u03AC'), new Character('\u00E1')); // GREEK SMALL LETTER ALPHA WITH TONOS (mapped to a with acute)
//		latinizationMappings.put(new Character('\u03AF'), new Character('\u00ED')); // GREEK SMALL LETTER IOTA WITH TONOS (mapped to i with acute)
//		latinizationMappings.put(new Character('\u03B1'), new Character('a')); // GREEK SMALL LETTER ALPHA
//		latinizationMappings.put(new Character('\u03B2'), new Character('b')); // GREEK SMALL LETTER BETA
//		latinizationMappings.put(new Character('\u03B3'), new Character('y')); // GREEK SMALL LETTER GAMMA
//		latinizationMappings.put(new Character('\u03BA'), new Character('k')); // GREEK SMALL LETTER KAPPA
//		latinizationMappings.put(new Character('\u03BD'), new Character('v')); // GREEK SMALL LETTER NU
//		latinizationMappings.put(new Character('\u03BF'), new Character('o')); // GREEK SMALL LETTER OMICRON
//		latinizationMappings.put(new Character('\u03C1'), new Character('p')); // GREEK SMALL LETTER RHO
//		latinizationMappings.put(new Character('\u03C5'), new Character('u')); // GREEK SMALL LETTER UPSILON
//		latinizationMappings.put(new Character('\u03C7'), new Character('x')); // GREEK SMALL LETTER CHI
//		latinizationMappings.put(new Character('\u03CA'), new Character('\u00EF')); // GREEK SMALL LETTER IOTA WITH DIALYTIKA (mapped to i with dieresis)
//		latinizationMappings.put(new Character('\u03CB'), new Character('\u00FC')); // GREEK SMALL LETTER UPSILON WITH DIALYTIKA (mapped to u with dieresis)
//		latinizationMappings.put(new Character('\u03CC'), new Character('\u00F3')); // GREEK SMALL LETTER OMICRON WITH TONOS (mapped to o with acute)
//		latinizationMappings.put(new Character('\u03CD'), new Character('\u00FA')); // GREEK SMALL LETTER UPSILON WITH TONOS (mapped to u with acute)
//		latinizationMappings.put(new Character('\u03CE'), new Character('w')); // GREEK SMALL LETTER OMEGA WITH TONOS
//		latinizationMappings.put(new Character('\u03D2'), new Character('Y')); // GREEK UPSILON WITH HOOK SYMBOL
//		latinizationMappings.put(new Character('\u03D3'), new Character('Y')); // GREEK UPSILON WITH ACUTE AND HOOK SYMBOL
//		latinizationMappings.put(new Character('\u03D4'), new Character('\u0178')); // GREEK UPSILON WITH DIAERESIS AND HOOK SYMBOL (mapped to Y with dieresis)
//	}
	
	/**
	 * Estimate the Levenshtein distance of two strings. This method returns a
	 * lower bound of the actual Levenshtein distance of the two argument
	 * strings, case insensitive and regarding any characters but letters simply
	 * as 'other characters', which are equal. On the upside, this method works
	 * in linear time. It is intended as a crude pre-filter for actual edit
	 * distance computation, to be used in applications seeking strings similar
	 * to a given one.
	 * @param string1 the first String
	 * @param string2 the second String
	 * @return an estimate of the Levenshtein distance of the specified Strings
	 */
	public static int estimateLevenshteinDistance(String string1, String string2) {
		byte[] crossSumData = new byte[27];
		for (int dc = 0; dc < string1.length(); dc++) {
			char ch = Character.toLowerCase(string1.charAt(dc));
			if (('a' <= ch) && (ch <= 'z')) crossSumData[ch - 'a']++;
			else crossSumData[26]++;
		}
		for (int cc = 0; cc < string2.length(); cc++) {
			char ch = Character.toLowerCase(string2.charAt(cc));
			if (('a' <= ch) && (ch <= 'z')) crossSumData[ch - 'a']--;
			else crossSumData[26]--;
		}
		int crossSum = 0;
		for (int csc = 0; csc < crossSumData.length; csc++)
			crossSum += Math.abs(crossSumData[csc]);
		return Math.max(Math.abs(string1.length() - string2.length()), ((crossSum + 1) / 2));
	}
	
	/**	compute the Levenshtein distance of two Strings
	 * @param	string1			the first String
	 * @param	string2			the second String
	 * @return the Levenshtein distance of the specified Strings
	 */
	public static int getLevenshteinDistance(String string1, String string2) {
		return getLevenshteinDistance(string1, string2, 0, false, 1, 1);
	}
	
	/**	compute the Levenshtein distance of two Strings
	 * @param	string1			the first String
	 * @param	string2			the second String
	 * @param	threshold		the maximum distance (computation will stop if specified value reached)
	 * @return the Levenshtein distance of the specified Strings, maximum the specified threshold
	 * Note: a threshold of 0 will compute the entire editing distance, regardless of its value
	 */
	public static int getLevenshteinDistance(String string1, String string2, int threshold) {
		return getLevenshteinDistance(string1, string2, threshold, false, 1, 1);
	}
	
	/**	compute the Levenshtein distance of two Strings
	 * @param	string1			the first String
	 * @param	string2			the second String
	 * @param	caseSensitive	use case sensitive or case insensitive comparison
	 * @return the Levenshtein distance of the specified Strings, maximum the specified threshold
	 */
	public static int getLevenshteinDistance(String string1, String string2, boolean caseSensitive) {
		return getLevenshteinDistance(string1, string2, 0, caseSensitive, 1, 1);
	}
	
	/**	compute the Levenshtein distance of two Strings
	 * @param	string1			the first String
	 * @param	string2			the second String
	 * @param	threshold		the maximum distance (computation will stop if specified value reached)
	 * @param	caseSensitive	use case sensitive or case insensitive comparison
	 * @return the Levenshtein distance of the specified Strings, maximum the specified threshold
	 * Note: a threshold of 0 will compute the entire editing distance, regardless of its value
	 */
	public static int getLevenshteinDistance(String string1, String string2, int threshold, boolean caseSensitive) {
		return getLevenshteinDistance(string1, string2, threshold, caseSensitive, 1, 1);
	}
	
	/**	compute the Levenshtein distance of two Strings
	 * @param	string1			the first String
	 * @param	string2			the second String
	 * @param	threshold		the maximum distance (computation will stop if specified value reached)
	 * @param	caseSensitive	use case sensitive or case insensitive comparison
	 * @param	insertCost		the cost for inserting a Character
	 * @param	deleteCost		the cost for deleting a Character
	 * @return the Levenshtein distance of the specified Strings, maximum the specified threshold plus one, soon as the minimum possible distance exceeds the threshold
	 * Note: a threshold of 0 will compute the entire editing distance, regardless of its value
	 */
	public static int getLevenshteinDistance(String string1, String string2, int threshold, boolean caseSensitive, int insertCost, int deleteCost) {
		int[][] distanceMatrix; // matrix
		int length1; // length of s
		int length2; // length of t
		int minLength; // the limit for the quadratic computation
		int cost; // cost
		int substitutionFactor = ((insertCost + deleteCost) / 2);
		int distance = 0;
		
		//	fill the matrix top-left to bottom-right instead of line-wise
		int limit = 1;
		
		// Step 1
		length1 = ((string1 == null) ? 0 : string1.length());
		length2 = ((string2 == null) ? 0 : string2.length());
		minLength = ((length1 > length2) ? length2 : length1);
		
		//	Step 1.5
		if ((Math.abs(length1 - length2) > threshold) && (threshold > 0))
			return (threshold + 1);
		
		// Step 2
		distanceMatrix = new int[length1 + 1][length2 + 1];
		distanceMatrix[0][0] = 0;
		
		while (limit <= minLength) {
			distanceMatrix[limit][0] = (limit * insertCost);
			distanceMatrix[0][limit] = (limit * deleteCost);
			
			//	compute line
			for (int c = 1; c < limit; c++) {
				cost = getCost(string1.charAt(c - 1), string2.charAt(limit - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[c - 1][limit] + deleteCost, distanceMatrix[c][limit - 1] + insertCost, distanceMatrix[c - 1][limit - 1] + cost);
				distanceMatrix[c][limit] = distance;
			}
			
			//	compute column
			for (int l = 1; l < limit; l++) {
				cost = getCost(string1.charAt(limit - 1), string2.charAt(l - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[limit - 1][l] + deleteCost, distanceMatrix[limit][l - 1] + insertCost, distanceMatrix[limit - 1][l - 1] + cost);
				distanceMatrix[limit][l] = distance;
			}
			
			//	compute new corner
			cost = getCost(string1.charAt(limit - 1), string2.charAt(limit - 1), substitutionFactor, caseSensitive);
			distance = min3(distanceMatrix[limit - 1][limit] + deleteCost, distanceMatrix[limit][limit - 1] + insertCost, distanceMatrix[limit - 1][limit - 1] + cost);
			if ((distance > threshold) && (threshold > 0))
				return (threshold + 1);
			distanceMatrix[limit][limit] = distance;
			
			//	increment limit
			limit++;
		}
		
		//	Step 2.5 (compute remaining columns)
		while (limit <= length1) {
			distanceMatrix[limit][0] = (limit * insertCost);
			
			//	compute column
			for (int l = 1; l <= length2; l++) {
				cost = getCost(string1.charAt(limit - 1), string2.charAt(l - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[limit - 1][l] + deleteCost, distanceMatrix[limit][l - 1] + insertCost, distanceMatrix[limit - 1][l - 1] + cost);
				distanceMatrix[limit][l] = distance;
			}
			if ((distance > threshold) && (threshold > 0))
				return (threshold + 1);
			
			//	increment limit
			limit++;
		}
		
		//	Step 2.5b (compute remaining rows)
		while (limit <= length2) {
			distanceMatrix[0][limit] = (limit * deleteCost);
			
			//	compute line
			for (int c = 1; c <= length1; c++) {
				cost = getCost(string1.charAt(c - 1), string2.charAt(limit - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[c - 1][limit] + deleteCost, distanceMatrix[c][limit - 1] + insertCost, distanceMatrix[c - 1][limit - 1] + cost);
				distanceMatrix[c][limit] = distance;
			}
			if ((distance > threshold) && (threshold > 0))
				return (threshold + 1);
			
			//	increment limit
			limit++;
		}
		
		// Step 7
		return distanceMatrix[length1][length2];
	}
	
	/**	the constants for the levenshtein edit sequences
	 */
	public static final int LEVENSHTEIN_INSERT = Integer.MAX_VALUE;
	public static final int LEVENSHTEIN_DELETE = Integer.MIN_VALUE;
	public static final int LEVENSHTEIN_REPLACE = 1;
	public static final int LEVENSHTEIN_KEEP = 0;
	
	/**	compute the Levenshtein sequence of insert/delete/substitute operations for transforming one String into another
	 * @param	start			the String to start from
	 * @param	goal			the String to transform start into
	 * @return the Levenshtein cost for transforming start into goal, maximum the specified threshold
	 */
	public static int[] getLevenshteinEditSequence(String start, String goal) {
		return getLevenshteinEditSequence(start, goal, false, 1, 1);
	}
	
	/**	compute the Levenshtein sequence of insert/delete/substitute operations for transforming one String into another
	 * @param	start			the String to start from
	 * @param	goal			the String to transform start into
	 * @param	caseSensitive	use case sensitive or case insensitive comparison for Characters values
	 * @return the Levenshtein cost for transforming start into goal, maximum the specified threshold
	 */
	public static int[] getLevenshteinEditSequence(String start, String goal, boolean caseSensitive) {
		return getLevenshteinEditSequence(start, goal, caseSensitive, 1, 1);
	}
	
	/**	compute the Levenshtein sequence of insert/delete/substitute operations for transforming one String into another
	 * @param	start			the String to start from
	 * @param	goal			the String to transform start into
	 * @param	caseSensitive	use case sensitive or case insensitive comparison for Characters values
	 * @param	insertCost		the cost for inserting a Character
	 * @param	deleteCost		the cost for deleting a Character
	 * @return the Levenshtein cost for transforming start into goal, maximum the specified threshold
	 */
	public static int[] getLevenshteinEditSequence(String start, String goal, boolean caseSensitive, int insertCost, int deleteCost) {
		
		int[][] distanceMatrix; // matrix
		int startSize; // length of s
		int goalSize; // length of t
		int minSize; // the limit for the quadratic computation
		int substitutionFactor = ((insertCost + deleteCost) / 2);
		int cost; // cost
		int distance = 0;
	
		//	fill the matrix top-left to bottom-right instead of line-wise
		int limit = 1;
	
		// Step 1
		startSize = ((start == null) ? 0 : start.length());
		goalSize = ((goal == null) ? 0 : goal.length());
		minSize = ((startSize > goalSize) ? goalSize : startSize);
		
		// Step 2
		distanceMatrix = new int[startSize + 1][goalSize + 1];
		distanceMatrix[0][0] = 0;
		
		while (limit <= minSize) {
			distanceMatrix[limit][0] = (limit * insertCost);
			distanceMatrix[0][limit] = (limit * deleteCost);
			
			//	compute line
			for (int c = 1; c < limit; c++) {
				cost = getCost(start.charAt(c - 1), goal.charAt(limit - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[c - 1][limit] + deleteCost, distanceMatrix[c][limit - 1] + insertCost, distanceMatrix[c - 1][limit - 1] + cost);
				distanceMatrix[c][limit] = distance;
			}
			
			//	compute column
			for (int l = 1; l < limit; l++) {
				cost = getCost(start.charAt(limit - 1), goal.charAt(l - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[limit - 1][l] + deleteCost, distanceMatrix[limit][l - 1] + insertCost, distanceMatrix[limit - 1][l - 1] + cost);
				distanceMatrix[limit][l] = distance;
			}
			
			//	compute new corner
			cost = getCost(start.charAt(limit - 1), goal.charAt(limit - 1), substitutionFactor, caseSensitive);
			distance = min3(distanceMatrix[limit - 1][limit] + deleteCost, distanceMatrix[limit][limit - 1] + insertCost, distanceMatrix[limit - 1][limit - 1] + cost);
			distanceMatrix[limit][limit] = distance;
			
			//	increment limit
			limit ++;
		}
		
		//	Step 2.5a (compute remaining columns)
		while (limit <= startSize) {
			distanceMatrix[limit][0] = (limit * insertCost);
			
			//	compute column
			for (int l = 1; l <= goalSize; l++) {
				cost = getCost(start.charAt(limit - 1), goal.charAt(l - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[limit - 1][l] + deleteCost, distanceMatrix[limit][l - 1] + insertCost, distanceMatrix[limit - 1][l - 1] + cost);
				distanceMatrix[limit][l] = distance;
			}
			
			//	increment limit
			limit ++;
		}
		
		//	Step 2.5b (compute remaining rows)
		while (limit <= goalSize) {
			distanceMatrix[0][limit] = (limit * deleteCost);
			
			//	compute line
			for (int c = 1; c <= startSize; c++) {
				cost = getCost(start.charAt(c - 1), goal.charAt(limit - 1), substitutionFactor, caseSensitive);
				distance = min3(distanceMatrix[c - 1][limit] + deleteCost, distanceMatrix[c][limit - 1] + insertCost, distanceMatrix[c - 1][limit - 1] + cost);
				distanceMatrix[c][limit] = distance;
			}
			
			//	increment limit
			limit ++;
		}
		
		//	compute editing order
		int startIndex = startSize;
		int goalIndex = goalSize;
		distance = distanceMatrix[startSize][goalSize];
		int maxDistance = distance;
		ArrayList steps = new ArrayList();
		
		while ((startIndex != 0) || (goalIndex != 0) || (distance != 0)) {
			
			//	read possible steps
			int subst = (((startIndex != 0) && (goalIndex != 0)) ? distanceMatrix[startIndex - 1][goalIndex - 1] : maxDistance);
			int del = ((startIndex != 0) ? distanceMatrix[startIndex - 1][goalIndex] : maxDistance);
			int ins = ((goalIndex != 0) ? distanceMatrix[startIndex][goalIndex - 1] : maxDistance);
			
			//	substitution
			if ((subst <= del) && (subst <= ins) && (startIndex != 0) && (goalIndex != 0)) {
				if ((start.charAt(startIndex-1) == goal.charAt(goalIndex-1)) || (!caseSensitive && (Character.toLowerCase(start.charAt(startIndex-1)) == Character.toLowerCase(goal.charAt(goalIndex-1))))) {
					//System.out.println("Keep " + start.charAt(startIndex-1));
					steps.add(new Integer(LEVENSHTEIN_KEEP));
				}
				else {
					//System.out.println("Replace " + start.charAt(startIndex-1) + " by " + goal.charAt(goalIndex-1));
					steps.add(new Integer(LEVENSHTEIN_REPLACE));
				}
				startIndex--;
				goalIndex--;
				distance = subst;
			}
			
			//	insertion
			else if ((ins <= subst) && (ins <= del) && (goalIndex != 0)) {
				//System.out.println("Insert " + goal.charAt(goalIndex-1));
				steps.add(new Integer(LEVENSHTEIN_INSERT));
				goalIndex--;
				distance = ins;
			}
			
			//	deletion
			else if ((del <= subst) && (del <= ins) && (startIndex != 0)) {
				//System.out.println("Delete " + start.charAt(startIndex-1));
				steps.add(new Integer(LEVENSHTEIN_DELETE));
				startIndex--;
				distance = del;
			}
			
			//	theoretically impossible state, break in order to avoid endless loop
			else {
				//System.out.println("Impossible ... Theoretically ... F**K !!!");
				startIndex = 0;
				goalIndex = 0;
				distance = 0;
			}
		}
		Collections.reverse(steps);
		int[] editSequence = new int[steps.size()];
		for (int i = 0; i < steps.size(); i++)
			editSequence[i] = ((Integer) steps.get(i)).intValue();
		
		// Step 7
		return editSequence;
	}
	
	/**	compute edit cost for two Tokens
	 * @param	char1			the first Token
	 * @param	char2			the second Token
	 * @param	caseSensitive	use case sensitive or case insensitive comparison for the Token's values 
	 * @return the edit cost for the two Tokens
	 */
	public static int getCost(char char1, char char2, int factor, boolean caseSensitive) {
		if (char1 == char2)
			return 0;
		if (!caseSensitive && (Character.toLowerCase(char1) == Character.toLowerCase(char2)))
			return 0;
		return factor;
	}
	
	/**	compute the minimum of three int variables (helper for Levenshtein)
	 * @param	x
	 * @param	y
	 * @param	z
	 * @return the minimum of x, y and z
	 */
	public static int min3(int x, int y, int z) {
		return Math.min(x, Math.min(y, z));
	}
	
	/**	convert a String such that it has an upper case letter only at the beginning
	 * @param	string	the String to be converted
	 * @return the specified String in first-letter-up
	 */
	public static String capitalize(String string) {
		boolean upperCase = true;
		StringBuffer capitalized = new StringBuffer();
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
			if (upperCase) {
				capitalized.append(Character.toUpperCase(ch));
				upperCase = false;
			}
			else if (IN_WORD_PUNCTUATION.indexOf(ch) != -1) {
				capitalized.append(ch);
				upperCase = true;
			}
			else capitalized.append(Character.toLowerCase(ch));
		}
		return capitalized.toString();
	}
	
	/**	replace all occurencies of a substring with another substring
	 * @param	string			the String to process
	 * @param	toReplace		the substring to replace
	 * @param	replacement		the substring to serve as the replacement
	 * @return the specified String with all occurrencies of toReplace replaced with the specified replacement
	 */
	public static String replaceAll(String string, String toReplace, String replacement) {
		if ((string == null) || (toReplace == null) || (replacement == null) || (toReplace.length() == 0))
			return string;
		if (string.equals(toReplace))
			return replacement;
		StringBuffer replaced = new StringBuffer();
		for (int c = 0; c < string.length();) {
			if (string.startsWith(toReplace, c)) {
				replaced.append(replacement);
				c += toReplace.length();
			}
			else replaced.append(string.charAt(c++));
		}
		return replaced.toString();
	}
	
	/**
	 * Insert escaper characters into a string, one before every occurrence of
	 * character toEscape.
	 * @param str the string to escape
	 * @param toEscape the character to escape
	 * @param escaper the escaper character
	 * @return the escaped string
	 */
	public static String escapeChar(String str, char toEscape, char escaper) {
		if (str == null)
			return null;
		StringBuffer escaped = new StringBuffer();
		char ch;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (ch == toEscape)
				escaped.append(escaper);
			escaped.append(ch);
		}
		return escaped.toString();
	}

	/**
	 * Insert escaper characters into a string, one before every occurrence of
	 * each character contained in toEscape.
	 * @param str the string to escape
	 * @param toEscape the array containing the characters to escape
	 * @param escaper the escaper character
	 * @return the escaped string
	 */
	public static String escapeChars(String str, char toEscape[], char escaper) {
		return escapeChars(str, new String(toEscape), escaper);
	}

	/**
	 * Insert escaper characters into a string, one before every occurrence of
	 * each character contained in toEscape.
	 * @param str the string to escape
	 * @param toEscape the array containing the characters to escape
	 * @param escaper the escaper character
	 * @return the escaped string
	 */
	public static String escapeChars(String str, String toEscape, char escaper) {
		if (str == null)
			return null;
		StringBuffer escaped = new StringBuffer();
		char ch;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (toEscape.indexOf(ch) != -1)
				escaped.append(escaper);
			escaped.append(ch);
		}
		return escaped.toString();
	}
	
	/**
	 * Test if a given char sequence is a possible abbreviation of another char
	 * sequence. This method tests whether or not the possible abbreviation
	 * consists of characters that also occur in the full version, in the same
	 * order as in the abbreviation. The comparison is case sensitive.
	 * @param full the full string to test the potential abbreviation against
	 * @param abbreviation the abbeviation to test
	 * @param caseSensitive run case sensitive or case insensitive comparison?
	 * @return true if the argument abbreviation is viable for the argument full
	 *         version, false otherwise
	 */
	public static boolean isAbbreviationOf(CharSequence full, CharSequence abbreviation, boolean caseSensitive) {
		if (DEBUG_ABBREVIATIONS) System.out.println("Abbreviation matching " + abbreviation + " against " + full);
		
		//	abbreviation longer than full form ==> abbreviation match impossible
		if (full.length() < abbreviation.length()) {
			if (DEBUG_ABBREVIATIONS) System.out.println(" ==> full too short");
			return false;
		}
		
		//	remove dots
		full = removeDots(full);
		abbreviation = removeDots(abbreviation);
		
		//	abbreviation longer than full form ==> abbreviation match impossible
		if (full.length() < abbreviation.length()) {
			if (DEBUG_ABBREVIATIONS) System.out.println(" ==> de-dotted full too short");
			return false;
		}
		
		//	check letter by letter
		int a = 0;
		for (int f = 0; f < full.length(); f++) {
			
			//	we've reached the end of the abbreviation, so it fits
			if (a == abbreviation.length()) {
				if (DEBUG_ABBREVIATIONS) System.out.println(" ==> match");
				return true;
			}
			
			//	get next character of full form
			char fch = (caseSensitive ? full.charAt(f) : Character.toLowerCase(full.charAt(f)));
			
			//	check next character of abbreviation
			char ach = (caseSensitive ? abbreviation.charAt(a) : Character.toLowerCase(abbreviation.charAt(a)));
			
			//	letters match, continue to next one
			if (ach == fch) {
				a++;
				continue;
			}
			
			//	jump over internal high commas (may mark omission in abbreviations like "Intern'l")
			if (ach == '\'') {
				a++;
				if (a == abbreviation.length()) {
					if (DEBUG_ABBREVIATIONS) System.out.println(" ==> match");
					return true;
				}
				ach = (caseSensitive ? abbreviation.charAt(a) : Character.toLowerCase(abbreviation.charAt(a)));
			}
			
			//	letters match, proceed to next one 
			if (ach == fch)
				a++;
			
			//	otherwise, consider current letter of full form as omitted
		}
		
		//	we have a match if we have reached the end of the abbreviation
		if (a == abbreviation.length()) {
			if (DEBUG_ABBREVIATIONS) System.out.println(" ==> match");
			return true;
		}
		else {
			if (DEBUG_ABBREVIATIONS) System.out.println(" ==> mis-match, only got to " + abbreviation.subSequence(0, a));
			return false;
		}
	}
	private static CharSequence removeDots(CharSequence str) {
		StringBuffer dotFreeStr = new StringBuffer();
		for (int c = 0; c < str.length(); c++) {
			char ch = str.charAt(c);
			if (ch != '.')
				dotFreeStr.append(ch);
		}
		return dotFreeStr;
	}
	private static final boolean DEBUG_ABBREVIATIONS = false;
}
