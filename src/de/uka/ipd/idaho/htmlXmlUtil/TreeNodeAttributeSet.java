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
package de.uka.ipd.idaho.htmlXmlUtil;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

import de.uka.ipd.idaho.htmlXmlUtil.exceptions.ParseException;
import de.uka.ipd.idaho.htmlXmlUtil.exceptions.UnexpectedCharacterException;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;
import de.uka.ipd.idaho.htmlXmlUtil.xPath.exceptions.InvalidArgumentsException;

/**
 * Container for the attributes of an XML element, much like
 * java.util.Properties, but handling the key Strings in a case insensitive
 * fashion to facilitate handling HTML.
 * 
 * @author sautter
 */
public class TreeNodeAttributeSet {
	
	private Grammar grammar;
	
	//	map for case-insensitive access
	private Properties names = new Properties();
	
	//	map for name / value pairs
	private Properties values = new Properties();
	
	//	list for sequential access
	private ArrayList nameList = new ArrayList();
	
	private TreeNodeAttributeSet() {
		this(new StandardGrammar());
	}
	
	private TreeNodeAttributeSet(Grammar grammar) {
		this.grammar = grammar;
	}
	
	/**	check if the TreeNodeAttributeSet contains a given attribute
	 * @param	attributeName	the attribute's name
	 * @return true if and only if this TreeNodeAttributeSet contains the attribute identified by the specified name
	 */
	public boolean containsAttribute(String attributeName) {
		String key = this.getKey(attributeName);
		return this.values.containsKey(key);
	}
	
	/**	read the value of an attribute
	 * @param	attributeName	the name of the attribute
	 * @return	the value of the specified attribute, or null, if there is no such attribute
	 */
	public String getAttribute(String attributeName) {
		String key = this.getKey(attributeName);
		return this.values.getProperty(key);
	}
	
	/**	read the value of an attribute
	 * @param	attributeName	the name of the attribute
	 * @param	def				the value to return if the attribute is not set
	 * @return	the value of the specified attribute, or def, if there is no such attribute
	 */
	public String getAttribute(String attributeName, String def) {
		String key = this.getKey(attributeName);
		return this.values.getProperty(key, def);
	}
	
	/**	add a attribute / value pair
	 * @param 	attributeName	the attribute
	 * @param 	value			the attribute value
	 * @return the overwritten value of the specified attribute, or null if there was no such value
	 * 	Note: if the specified attribute is already set for this node, it's value is changed to the specified value 
	 */
	public String setAttribute(String attributeName, String value) {
		String key = this.getKey(attributeName);
		String oldValue = this.values.getProperty(key);
		this.names.setProperty(key, attributeName);
		this.values.setProperty(key, value);
		if (!this.nameList.contains(key)) this.nameList.add(key);
		return oldValue;
	}
	
	/**	remove an attribute
	 * @param	attributeName	the name of the attribute to be removed
	 * @return the attribute's value
	 */
	public String removeAttribute(String attributeName) {
		String key = this.getKey(attributeName);
		String oldValue = this.values.getProperty(key);
		this.names.remove(key);
		this.values.remove(key);
		this.nameList.remove(key);
		return oldValue;
	}
	
	/**	@return	all attribute names in an array
	 */
	public String[] getAttributeNames() {
		String[] ret = new String[this.nameList.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = this.names.getProperty((String) this.nameList.get(i));
		return ret;
	}
	
	/**	@return	all attribute / value pairs in an array
	 */
	public String[] getAttributeValuePairs(char attributeValueSeparator, char quoter) {
		return this.getAttributeValuePairs(attributeValueSeparator, quoter, this.grammar);
	}
	
	/**	@return	all attribute / value pairs in an array
	 */
	public String[] getAttributeValuePairs(Grammar grammar) {
		return this.getAttributeValuePairs(grammar.getTagAttributeValueSeparator(), grammar.getTagAttributeValueQuoter(), grammar);
	}
	
	/**	@return	all attribute / value pairs in an array
	 */
	public String[] getAttributeValuePairs(char attributeValueSeparator, char quoter, Grammar grammar) {
		String[] ret = this.getAttributeNames();
		for (int i = 0; i < ret.length; i++) {
			String value = this.getAttribute(ret[i], ret[i]);
			if (value != null)
				ret[i] += (attributeValueSeparator + "" + quoter + "" + grammar.escape(value) + "" + quoter);
		}
		return ret;
	}
	
	/**	@return	all attribute / value pairs listed in a single String
	 */
	public String getAttributeValueString(Grammar grammar) {
		String[] attributes = this.getAttributeValuePairs(grammar);
		StringBuffer assembler = new StringBuffer();
		for (int i = 0; i < attributes.length; i++) {
			if (i != 0) assembler.append(grammar.getTagAttributeSeparator());
			assembler.append(attributes[i]);
		}
		return assembler.toString();
	}
	
	/**	@return	all attribute / value pairs listed in a single String
	 */
	public String getAttributeValueString(char attributeSeparator, char attributeValueSeparator, char quoter) {
		String[] attributes = this.getAttributeValuePairs(attributeValueSeparator, quoter);
		StringBuffer assembler = new StringBuffer();
		for (int i = 0; i < attributes.length; i++) {
			if (i != 0) assembler.append(attributeSeparator);
			assembler.append(attributes[i]);
		}
		return assembler.toString();
	}
	
	/**	remove all attribute / value pairs
	 */
	public void clear() {
		this.names.clear();
		this.values.clear();
		this.nameList.clear();
	}
	
	/** check whether this TreeNodeAttributeSet is empty
	 * @return true if and only if this TreeNodeAttributeSet contains no key/value pairs
	 */
	public boolean isEmpty() {
		return this.names.isEmpty();
	}
	
	/**	@return	the number of key/value pairs contained in this TreeNodeAttributeSet
	 */
	public int size() {
		return this.names.size();
	}
	
	/**	@return	a key value for the HashMaps produced from the specified String
	 */
	protected String getKey(String string) {
		return ((string != null) ? string.toLowerCase() : null);
	}
	
	/**	parse the attribute / value pairs out of the specified tag
	 * @param 	tag			the tag to be parsed
	 * @param 	grammar		the Grammar in whose context to parse the attributes
	 * @return	the argument tag's attribute / value pairs in a Properties object
	 */
	public static TreeNodeAttributeSet getTagAttributes(String tag, Grammar grammar) {
		TreeNodeAttributeSet attributes = new TreeNodeAttributeSet(grammar);
		if ((tag != null) || (grammar != null)) try {
//			watchParse(tag);
			fillTagAttributeSet(tag, grammar, attributes, -1);
		}
		catch (ParseException pe) {
			throw new InvalidArgumentsException(pe.getMessage(), pe);
		}
		catch (IOException ioe) { /* does not happen with a StringReader, but Java don't know */ 
			ioe.printStackTrace();
		}
//		finally {
//			doneParse();
//		}
		return attributes;
	}
	
	private static void fillTagAttributeSet(String tag, Grammar grammar, TreeNodeAttributeSet attributes, int correctedAttributeValueQuoter) throws IOException {
		LookaheadReader charSource = new LookaheadReader(new StringReader(tag), grammar.getCharLookahead());
		char tagAttributeValueSeparator = grammar.getTagAttributeValueSeparator();
		char tagEnd = grammar.getTagEnd();
		char endTagMarker = grammar.getEndTagMarker();
		charSource.read(); // crop tag start
		
		//	crop end tag marker
		if (charSource.peek() == endTagMarker)
			charSource.read();
		
		//	crop qName
		String tagType = LookaheadReader.cropName(charSource);
//		System.out.println("TNAS: Got tag " + tagType);
		if (tagType.length() == 0)
//			throw new ParseException("Invalid character '" + ((char) charSource.peek()) + "', expected name");
			throw new UnexpectedCharacterException(((char) charSource.peek()), charSource.readThusFar(), "tag type");
		skipWhitespace(charSource, grammar);
		
		//	set up corrected recursion
		int lastAttributeValueQuoter = -1;
		
		//	crop attribute-value pairs
		while (charSource.peek() != -1) {
			skipWhitespace(charSource, grammar);
			
			//	end of attributes
			if ((charSource.peek() == tagEnd) || (charSource.peek() == endTagMarker))
				break;
			
			//	read attribute name
			String attribName = LookaheadReader.cropName(charSource);
//			System.out.println("TNAS: Got attribute name " + attribName);
			
			//	attribute name empty, something's wrong
			if (attribName.length() == 0) {
				
				//	escape (and thus ignore) last value quoter (likely prematurely ended attribute value, which is why we're in some weird state)
				if (grammar.correctErrors() && (lastAttributeValueQuoter != -1) && (correctedAttributeValueQuoter < lastAttributeValueQuoter)) {
					String correctedTag = (tag.substring(0, lastAttributeValueQuoter) + grammar.escape("" + tag.charAt(lastAttributeValueQuoter)) + tag.substring(lastAttributeValueQuoter + 1));
					attributes.clear();
//					System.out.println("TNAS: recursing with corrected tag " + correctedTag);
					fillTagAttributeSet(correctedTag, grammar, attributes, lastAttributeValueQuoter);
					return;
				}
//				else throw new ParseException("Invalid character '" + ((char) charSource.peek()) + "', expected name");
				else throw new UnexpectedCharacterException(((char) charSource.peek()), charSource.readThusFar(), "attribute name");
			}
			skipWhitespace(charSource, grammar);
			
			//	we have a value (tolerate missing separator if configured that way)
			String attribValue;
			if ((charSource.peek() == tagAttributeValueSeparator) || grammar.isTagAttributeValueQuoter((char) charSource.peek())) {
				if (charSource.peek() == tagAttributeValueSeparator)
					charSource.read();
				else if (!grammar.correctErrors())
//					throw new ParseException("Invalid character '" + ((char) charSource.peek()) + "', expected '" + tagAttributeValueSeparator + "'");
					throw new UnexpectedCharacterException(((char) charSource.peek()), charSource.readThusFar(), ("" + tagAttributeValueSeparator));
				skipWhitespace(charSource, grammar);
				if (grammar.isTagAttributeValueQuoter((char) charSource.peek())) {
					lastAttributeValueQuoter = charSource.readThusFar();
//					System.out.println("TNAS: quoter at " + lastAttributeValueQuoter + " (is '" + (tag.charAt(lastAttributeValueQuoter)) + "' in tag string)");
				}
				attribValue = LookaheadReader.cropAttributeValue(charSource, grammar, tagType, attribName, tagEnd, endTagMarker);
				if (grammar.isTagAttributeValueQuoter(tag.charAt(charSource.readThusFar() - 1))) {
					lastAttributeValueQuoter = (charSource.readThusFar() - 1);
//					System.out.println("TNAS: end quoter at " + lastAttributeValueQuoter + " (is '" + (tag.charAt(lastAttributeValueQuoter)) + "' in tag string)");
				}
//				System.out.println("TNAS: Got attribute value " + attribValue);
			}
			
			//	we have a standalone attribute, substitute name for value
			else {
				attribValue = attribName;
//				System.out.println("TNAS: Defaulted attribute value to " + attribValue);
			}
			
			//	append normalized attribute
			attributes.setAttribute(attribName, grammar.unescape(attribValue));
		}
	}
	
	private static void skipWhitespace(LookaheadReader charSource, Grammar grammar) throws IOException {
		while ((charSource.peek() != -1) && grammar.isWhitespace((char) charSource.peek()))
			charSource.read();
	}
	
//	private static ThreadLocal watchList = new ThreadLocal();
//	private static final Object watched = new Object();
//	public static void watch() {
//		watchList.set(watched);
//	}
//	public static void unwatch() {
//		watchList.remove();
//	}
//	
//	private static class WatchedParse {
//		final String tag;
//		final long start = System.currentTimeMillis();
//		WatchedParse(String tag) {
//			this.tag = tag;
//		}
//	}
//	private static LinkedHashMap watchedParses = new LinkedHashMap();
//	private static void watchParse(String tag) {
//		if (watchList.get() == null)
//			return;
//		synchronized(watchedParses) {
//			watchedParses.put(Long.valueOf(Thread.currentThread().getId()), new WatchedParse(tag));
//		}
//	}
//	private static void doneParse() {
//		if (watchList.get() == null)
//			return;
//		synchronized(watchedParses) {
//			watchedParses.remove(Long.valueOf(Thread.currentThread().getId()));
//		}
//	}
//	private static Thread watchedParseLogger = new Thread() {
//		public void run() {
//			while (true) {
//				try {
//					sleep(1000);
//				} catch (InterruptedException ie) {}
//				ArrayList wps;
//				synchronized (watchedParses) {
//					wps = new ArrayList(watchedParses.values());
//				}
//				long time = System.currentTimeMillis();
//				for (int p = 0; p < wps.size(); p++) {
//					WatchedParse wp = ((WatchedParse) wps.get(p));
//					if ((60 * 1000) < (time - wp.start))
//						System.out.println("TnasParseStuck: " + wp.tag);
//				}
//			}
//		}
//	};
//	static {
//		watchedParseLogger.start();
//	}
//	
//	private static final Grammar html = new Html();
//	public static void main(String[] args) throws Exception {
////		String testTag = "<a title=\"FIGURE 70. Palpipalpus hesperius Beard and Seeman, adult female, legs (right side), solenidion w \" and eupathidia (pk ' - pk '') not labelled on leg I.\" href=\"#\" onclick=\"return displayFigure('https://zenodo.org/record/251405/files/figure.png');\">";
//		String testTag = "<a title=\"View 'FIGURE 75. Pentamerismus sititoris Beard and Seeman, adult female, dorsum with details of palps and legs I - II; solenidion w \" and eupathidia (pk ' - pk '') not labelled.'\" href=\"#\" onclick=\"return displayFigure('https://zenodo.org/record/251409/files/figure.png');\">";
////		String testTag = "<a title=\"FIGURE 70. Palpipalpus hesperius Beard and Seeman, adult female, legs (right side), solenidion w \" and eupathidia (pk ' - pk '') not labelled on leg I.\" href=\"#\" onclick=\"return displayFigure('https://zenodo.org/record/251405/files/figure.png\");\">";
//		try {
//			TreeNodeAttributeSet tnas = getTagAttributes(testTag, html);
//			String ans[] = tnas.getAttributeNames();
//			for (int a = 0; a < ans.length; a++)
//				System.out.println(ans[a] + " = " + tnas.getAttribute(ans[a]));
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	public static void main(String[] args) throws Exception {
////		for (int r = 0; r < 16384; r++) {
////			StringBuffer testTagBuilder = new StringBuffer("<");
////			int typeChars = randomLength(32, 3);
////			for (int c = 0; c <= typeChars; c++)
////				testTagBuilder.append(randomChar(c == 0));
////			String testTag = (testTagBuilder.toString() + ">");
////			System.out.println("Parsing attributes from " + testTag.length() + " chars");
////			long start = System.currentTimeMillis();
////			getTagAttributes(testTag, html);
////			System.out.println(" ==> done after " + (System.currentTimeMillis() - start) + "ms");
////		}
////		for (int r = 0; r < 4096; r++) {
//		for (int r = 0; r < 65536; r++) {
//			StringBuffer testTagBuilder = new StringBuffer("<");
//			int typeChars = randomLength(32, 3);
//			for (int c = 0; c <= typeChars; c++)
//				testTagBuilder.append(randomChar(c == 0));
//			do {
//				int spaces = randomLength(2, 1);
//				for (int s = 0; s < spaces; s++)
//					testTagBuilder.append(randomSpace());
//				
//				String testTag = (testTagBuilder.toString() + ">");
////				System.out.println("Parsing attributes from " + testTag.length() + " chars: " + testTag);
//				System.out.println("Parsing attributes from " + testTag.length() + " chars");
//				long start = System.currentTimeMillis();
//				getTagAttributes(testTag, html);
//				System.out.println(" ==> done after " + (System.currentTimeMillis() - start) + "ms");
//				testTag = (testTagBuilder.toString() + "/>");
////				System.out.println("Parsing attributes from " + testTag.length() + " chars: " + testTag);
//				System.out.println("Parsing attributes from " + testTag.length() + " chars");
//				start = System.currentTimeMillis();
//				getTagAttributes(testTag, html);
//				System.out.println(" ==> done after " + (System.currentTimeMillis() - start) + "ms");
//				
//				spaces = randomLength(6, 1);
//				for (int s = 0; s <= spaces; s++)
//					testTagBuilder.append(randomSpace());
//				int nameChars = randomLength(32, 2);
//				for (int c = 0; c <= nameChars; c++)
//					testTagBuilder.append(randomChar(c == 0));
//				testTagBuilder.append('=');
//				testTagBuilder.append('"');
//				int valueChars = randomLength(1024, 4);
//				for (int c = 0; c < valueChars; c++)
//					testTagBuilder.append(randomChar(false));
//				testTagBuilder.append('"');
////				String testTag = (testTagBuilder.toString() + ">");
////				System.out.println("Parsing attributes from " + testTag.length() + " chars");
////				long start = System.currentTimeMillis();
////				getTagAttributes(testTag, html);
////				System.out.println(" ==> done after " + (System.currentTimeMillis() - start) + "ms");
//			}
////			while (testTagBuilder.length() < 16384);
//			while (testTagBuilder.length() < 1024);
//		}
//	}
//	private static int randomLength(int max, int exp) {
//		double br = Math.random();
//		double r = br;
//		for (; exp > 1; exp--)
//			r *= br;
//		return ((int) Math.floor(max * r));
//	}
//	private static char randomSpace() {
//		return " \t\r\n".charAt(randomLength(4, 4));
//	}
//	private static char randomChar(boolean lettersOnly) {
//		int ch = ((int) Math.floor((lettersOnly ? 52 : 64) * Math.random()));
//		if (ch < 26)
//			return ((char) ('A' + ch));
//		ch -= 26;
//		if (ch < 26)
//			return ((char) ('a' + ch));
//		ch -= 26;
//		if (ch < 10)
//			return ((char) ('0' + ch));
//		ch -= 10;
//		return "-_".charAt(ch);
//	}
//	
//	public static TreeNodeAttributeSet getTagAttributes(String tag, Grammar grammar) {
//		TreeNodeAttributeSet attributes = new TreeNodeAttributeSet(grammar);
//		
//		//	check attributes
//		if ((tag == null) || (grammar == null))
//			return attributes;
//		
//		char tagAttributeSeparator = grammar.getTagAttributeSeparator();
//		char tagAttributeValueSeparator = grammar.getTagAttributeValueSeparator();
//		char tagEnd = grammar.getTagEnd();
//		char endTagMarker = grammar.getEndTagMarker();
//		
//		StringBuffer assembler = new StringBuffer();
//		Vector attributeCollector = new Vector();
//		
//		boolean inQuotas = false;
//		char ch;
//		char quoter = NULLCHAR;
//		
//		//	get start of first attribute (if there is any)
//		int index = tag.indexOf("" + tagAttributeSeparator);
//		
//		//	return if no tag or no attributes in tag
//		if (!grammar.isTag(tag) || (index == -1))
//			return attributes;
//		
//		//	find attributes and values
//		index ++;
//		while (index < tag.length()) {
//			
//			ch = tag.charAt(index);
//			
//			//	in quotas
//			if (inQuotas) {
//				
//				//	end of quotas
//				if (ch == quoter) {
//					quoter = NULLCHAR;
//					inQuotas = false;
//					assembler.append(ch);
//					
//					//	store token
//					if (assembler.length() > 0)
//						attributeCollector.addElement(assembler.toString().trim());
//					assembler.delete(0, assembler.length());
//				}
//				else assembler.append(ch);
//					
//				index ++;
//			}
//			
//			//	not in quotas
//			else {
//				
//				//	start of quotas
//				if (grammar.isTagAttributeValueQuoter(ch)) {
//					
//					//	store token
//					if (assembler.length() > 0)
//						attributeCollector.addElement(assembler.toString().trim());
//					assembler.delete(0, assembler.length());
//					
//					inQuotas = true;
//					quoter = ch;
//					assembler.append(ch);
//					index ++;
//				}
//				
//				//	end of attribute or value (separator or end of tag)
//				else if ((ch == tagAttributeSeparator) || (ch == tagAttributeValueSeparator) || (ch == tagEnd) || (ch == endTagMarker)) {
//					if (ch == tagAttributeValueSeparator)
//						assembler.append(tagAttributeValueSeparator);
//					
//					//	store token
//					if (assembler.length() > 0)
//						attributeCollector.addElement(assembler.toString().trim());
//					assembler.delete(0, assembler.length());
//					index++;
//				}
//				
//				//	common character
//				else {
//					assembler.append(ch);
//					index ++;
//				}
//			}
//		}
//		
//		//	remove empty attributes
//		while (attributeCollector.contains(""))
//			attributeCollector.remove("");
//		
//		//	assembler attribute / value pairs
//		index = 0;
//		while (index < attributeCollector.size()) {
//			
//			//	get next token to be checked
//			String token = ((String) attributeCollector.get(index)).trim();
//			
//			//	token is attribute name with a subsequent value token to be expected
//			if (token.endsWith("" + tagAttributeValueSeparator)) {
//				
//				//	next token is attribute value
//				if (((index + 1) < attributeCollector.size()) && !((String) attributeCollector.get(index + 1)).trim().endsWith("" + tagAttributeValueSeparator)) {
//					
//					//	 get value
//					String value = ((String) attributeCollector.get(index + 1)).trim();
//					
//					//	remove quoters
//					if ((value.length() > 0) && grammar.isTagAttributeValueQuoter(value.charAt(0)))
//						value = value.substring(1);
//					if ((value.length() > 0) && grammar.isTagAttributeValueQuoter(value.charAt(value.length() - 1)))
//						value = value.substring(0, (value.length() - 1));
//					
//					//	store attribute / value pair
////					attributes.setAttribute(token.substring(0, (token.length() - 1)), value);
//					attributes.setAttribute(token.substring(0, (token.length() - 1)), grammar.unescape(value));
//					index += 2;
//				}
//				
//				//	next token is another attribute name with a subsequent value token to be expected, value of current token empty, store attribute with empty value
//				else {
//					attributes.setAttribute(token.substring(0, (token.length() - 1)), "");
//					index ++;
//				}
//			}
//			
//			//	token is attribute name with no subsequent value token to be expected, store valueless attribute 
//			else {
//				attributes.setAttribute(token, token);
//				index ++;
//			}
//		}
//		
//		//	return attributes
//		return attributes;
//	}
}
