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
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
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
package de.uka.ipd.idaho.easyIO.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uka.ipd.idaho.easyIO.streams.PeekReader;

/**
 * Parser for JSON data, exclusively using core Java classes to represent
 * de-serialized objects, namely HashMap, ArrayList, String, and primitive
 * type wrapper classes.
 * 
 * @author sautter
 */
public class JsonParser {
	
	/**
	 * Observer for JSON parsing, receiving SAX style continuous notification
	 * about parsing results. None of the methods in this implementation do
	 * anything, sub classes have to overwrite the ones they need, e.g. to only
	 * extract a very specific property values from a large array of objects.
	 * 
	 * @author sautter
	 */
	public static abstract class JsonReceiver {
		
		/**
		 * Receive notification that an array has started, i.e., on the opening
		 * square bracket.
		 */
		public void arrayStarted() {}
		
		/**
		 * Receive notification that an array has ended, i.e., on the closing
		 * square bracket.
		 */
		public void arrayEnded() {}
		
		/**
		 * Receive notification that an object has started, i.e., on the opening
		 * curly bracket.
		 */
		public void objectStarted() {}
		
		/**
		 * Receive notification that an object property name was read, i.e.,
		 * after the key string of a key/value pair in an object.
		 * @param prop the property name
		 */
		public void objectPropertyRead(String prop) {}
		
		/**
		 * Receive notification that an object has ended, i.e., on the closing
		 * curly bracket.
		 */
		public void objectEnded() {}
		
		/**
		 * Receive notification that a string value was read.
		 * @param str the string value
		 */
		public void stringRead(String str) {}
		
		/**
		 * Receive notification that a number value was read.
		 * @param num the number value
		 */
		public void numberRead(Number num) {}
		
		/**
		 * Receive notification that a boolean value was read.
		 * @param bool the boolean value
		 */
		public void booleanRead(boolean bool) {}
		
		/**
		 * Receive notification that a null value was read.
		 */
		public void nullRead() {}
		
		/**
		 * Indicate the minimum lookahead length (number of upcoming characters
		 * that can be inspected in a <code>PeekReader</code> without consuming
		 * them) required by this JSON receiver to handle custom constructs.
		 * This default implementation simply returns 0. Subclasses are welcome
		 * to overwrite it as needed.
		 * @return the minimum lookahead length
		 */
		protected int getMinimumLookahead() {
			return 0;
		}
		
		/**
		 * Indicate whether or not the next character(s) in a stream of JSON
		 * characters starts a custom construct understood by this receiver.
		 * The argument <code>nextChar</code> is the one returned by calling
		 * <code>peek()</code> on the argument reader, intended for quick
		 * checks. If this method returns true, the <code>cropCustom()</code>
		 * method is expected to read and consume the whole custom construct
		 * to its very last character, leaving the argument reader looking at
		 * the next valid JSON character in the surrounding context. If an
		 * implementation of this method requires a further lookahead than 5
		 * characters (the length of the <code>false</code> constant), the
		 * calling client code must either hand a respectively parameterized
		 * receiver must overwrite the <code>getMinimumLookahead()</code> to
		 * indicate the higher lookahead length. This default implementation
		 * simply returns false, subclasses are welcome to overwrite it as
		 * needed.
		 * @param nextChar the next character to come in the argument reader
		 * @param pr the reader providing the stream of JSON data
		 * @return true if the next character(s) start a custom construct
		 */
		protected boolean startsCustom(char nextChar, PeekReader pr) throws IOException {
			return false;
		}
		
		/**
		 * Read and consume a custom construct understood by this receiver.
		 * This method must handle at least one character starting from each
		 * character the <code>startsCustom()</code> method returns true for.
		 * Custom constructs can occur in any place an array, object, string,
		 * number, or boolean can appear in, but they cannot take the place of
		 * object property names, only that of object property values. After
		 * reading and consuming the custom construct, the argument reader must
		 * be looking at the next valid JSON character in the surrounding
		 * context. It is the responsibility of implementations to ensure the
		 * latter. This default implementation does nothing, subclasses are
		 * welcome to overwrite it as needed.
		 * @param pr the reader providing the stream of JSON data
		 */
		protected void readCustom(PeekReader pr) throws IOException {}
	}
	
	/**
	 * Escape a string for JavaScript and JSON use, expecting a single quote
	 * to go around the escaped string. Use the two-argument version of this
	 * method to escape a string for other quotes, e.g. double quotes.
	 * @param str the string to escape
	 * @return the escaped string
	 */
	public static String escape(String str) {
		return escape(str, '\'');
	}
	
	/**
	 * Escape a string for JavaScript and JSON use.
	 * @param str the string to escape
	 * @param quot the quote character to go around the escaped string
	 * @return the escaped string
	 */
	public static String escape(String str, char quot) {
		if (str == null)
			return null;
		StringBuffer escaped = new StringBuffer();
		char ch;
		for (int c = 0; c < str.length(); c++) {
			ch = str.charAt(c);
			if (ch == '\r')
				escaped.append("\\r");
			else if (ch == '\n')
				escaped.append("\\n");
			else if (ch < 32)
				escaped.append(' ');
			else {
				if ((ch == quot) || (ch == '\\'))
					escaped.append('\\');
				escaped.append(ch);
			}
		}
		return escaped.toString();
	}
	
	/**
	 * Get a Map representing a JSON object from a List representing a JSON
	 * array. If the object at the argument index is not a Map, this method
	 * returns null.
	 * @param array the JSON array to retrieve the object from
	 * @param index the index of the sought object
	 * @return the Map at the argument index
	 */
	public static Map getObject(List array, int index) {
		Object map = array.get(index);
		return ((map instanceof Map) ? ((Map) map) : null);
	}
	
	/**
	 * Get a List representing a JSON array from another List representing a
	 * JSON array. If the object at the argument index is not a List, this
	 * method returns null.
	 * @param array the JSON array to retrieve the array from
	 * @param index the index of the sought array
	 * @return the List at the argument index
	 */
	public static List getArray(List array, int index) {
		Object list = array.get(index);
		return ((list instanceof List) ? ((List) list) : null);
	}
	
	/**
	 * Get a String from a list representing a JSON array. If the object at the
	 * argument index is not a String, but a Number or Boolean, it is converted
	 * to a String. If it belongs to another class, this method returns null.
	 * @param array the JSON array to retrieve the string from
	 * @param index the index of the sought string
	 * @return the String at the argument index
	 */
	public static String getString(List array, int index) {
		Object string = array.get(index);
		if (string instanceof String)
			return ((String) string);
		else if (string instanceof Number)
			return ((Number) string).toString();
		else if (string instanceof Boolean)
			return ((Boolean) string).toString();
		else return null;
	}
	
	/**
	 * Get a Number from a list representing a JSON array. If the object at the
	 * argument index is not a Number, but a String, this method attempts to
	 * convert it into a Number. If the latter fails, or the object belongs to
	 * another class, this method returns null.
	 * @param array the JSON array to retrieve the number from
	 * @param index the index of the sought number
	 * @return the Number at the argument index
	 */
	public static Number getNumber(List array, int index) {
		Object number = array.get(index);
		if (number instanceof Number)
			return ((Number) number);
		else if (number instanceof String) {
			try {
				return new Long((String) number);
			} catch (NumberFormatException nfe) {}
			try {
				return new Double((String) number);
			} catch (NumberFormatException nfe) {}
			return null;
		}
		else return null;
	}
	
	/**
	 * Get a Boolean from a list representing a JSON array. If the object at
	 * the argument index is not a Boolean, but a String, this method attempts
	 * to convert it into a boolean. If the latter fails, or the object belongs
	 * to another class, this method returns null.
	 * @param array the JSON array to retrieve the boolean from
	 * @param index the index of the sought boolean
	 * @return the Boolean at the argument index
	 */
	public static Boolean getBoolean(List array, int index) {
		Object bool = array.get(index);
		if (bool instanceof Boolean)
			return ((Boolean) bool);
		else if (bool instanceof String)
			return new Boolean((String) bool);
		else return null;
	}
	
	/**
	 * Get a Map representing a JSON object from another Map representing a
	 * JSON object. If the argument name is not mapped to a Map, this method
	 * returns null.
	 * @param object the JSON object to retrieve the object from
	 * @param name the name of the sought object
	 * @return the Map the argument name maps to
	 */
	public static Map getObject(Map object, String name) {
		Object map = object.get(name);
		return ((map instanceof Map) ? ((Map) map) : null);
	}
	
	/**
	 * Get a List representing a JSON array from a Map representing a JSON
	 * object. If the argument name is not mapped to a List, this method
	 * returns null.
	 * @param object the JSON object to retrieve the array from
	 * @param name the name of the sought array
	 * @return the List the argument name maps to
	 */
	public static List getArray(Map object, String name) {
		Object list = object.get(name);
		return ((list instanceof List) ? ((List) list) : null);
	}
	
	/**
	 * Get a String from a Map representing a JSON object. If the argument name
	 * is not mapped to a String, but a Number or Boolean, it is converted to a
	 * String. If it belongs to another class, this method returns null.
	 * @param object the JSON object to retrieve the string from
	 * @param name the name of the sought string
	 * @return the String the argument name maps to
	 */
	public static String getString(Map object, String name) {
		Object string = object.get(name);
		if (string instanceof String)
			return ((String) string);
		else if (string instanceof Number)
			return ((Number) string).toString();
		else if (string instanceof Boolean)
			return ((Boolean) string).toString();
		else return null;
	}
	
	/**
	 * Get a Number from a Map representing a JSON object. If the argument name
	 * is not mapped to a Number, but a String, this method attempts to convert
	 * it into a Number. If the latter fails, or the object belongs to another
	 * class, this method returns null.
	 * @param object the JSON object to retrieve the number from
	 * @param name the name of the sought number
	 * @return the Number the argument name maps to
	 */
	public static Number getNumber(Map object, String name) {
		Object number = object.get(name);
		if (number instanceof Number)
			return ((Number) number);
		else if (number instanceof String) {
			try {
				return new Long((String) number);
			} catch (NumberFormatException nfe) {}
			try {
				return new Double((String) number);
			} catch (NumberFormatException nfe) {}
			return null;
		}
		else return null;
	}
	
	/**
	 * Get a Boolean from a Map representing a JSON object. If the argument
	 * name is not mapped to a Boolean, but a String, this method attempts to
	 * convert it into a boolean. If the latter fails, or the object belongs to
	 * another class, this method returns null.
	 * @param object the JSON object to retrieve the boolean from
	 * @param name the name of the sought boolean
	 * @return the Boolean the argument name maps to
	 */
	public static Boolean getBoolean(Map object, String name) {
		Object bool = object.get(name);
		if (bool instanceof Boolean)
			return ((Boolean) bool);
		else if (bool instanceof String)
			return new Boolean((String) bool);
		else return null;
	}
	
	/**
	 * Serialize a JSON object into its string representation. If the argument
	 * object is neither of Map, List, String, Number, or Boolean, this method
	 * returns null. Likewise, it ignores all all content objects that are of
	 * neither of those five classes.
	 * @param obj the object to serialize
	 * @param indent the indentation to use per level, for pretty printing
	 * @return the string representation of the argument object 
	 */
	public static String toString(Object obj) {
		return toString(obj, '"');
	}
	
	/**
	 * Serialize a JSON object into its string representation. If the argument
	 * object is neither of Map, List, String, Number, or Boolean, this method
	 * returns null. Likewise, it ignores all content objects that are of
	 * neither of those five classes.
	 * @param obj the object to serialize
	 * @param quot the quotation mark to use for strings
	 * @param indent the indentation to use per level, for pretty printing
	 * @return the string representation of the argument object 
	 */
	public static String toString(Object obj, char quot) {
		return toString(obj, quot, false);
	}
	
	private static String toString(Object obj, char quot, boolean isElementOrValue) {
		if (obj instanceof Map)
			return toString(((Map) obj), quot);
		else if (obj instanceof List)
			return toString(((List) obj), quot);
		else if (obj instanceof String)
			return toString(((String) obj), quot);
		else if (obj instanceof Number)
			return toString((Number) obj);
		else if (obj instanceof Boolean)
			return toString((Boolean) obj);
		else if (isElementOrValue && (obj == null))
			return "null";
		else return null;
	}
	
	private static String toString(Map object, char quot) {
		StringBuffer sb = new StringBuffer("{");
		for (Iterator kit = object.keySet().iterator(); kit.hasNext();) {
			Object key = ((String) kit.next());
			if (!(key instanceof String))
				continue;
			String value = toString(object.get(key), quot, true);
			if (value == null)
				continue;
			if (sb.length() > 1)
				sb.append(", ");
			sb.append(toString(((String) key), quot));
			sb.append(": ");
			sb.append(value);
		}
		return sb.append("}").toString();
	}
	
	private static String toString(List array, char quot) {
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < array.size(); i++) {
			String element = toString(array.get(i), quot, true);
			if (element == null)
				continue;
			if (sb.length() > 1)
				sb.append(", ");
			sb.append(element);
		}
		return sb.append("]").toString();
	}
	
	private static String toString(String string, char quot) {
		return ("" + quot + escape(string, quot) + quot);
	}
	
	private static String toString(Number number) {
		return number.toString();
	}
	
	private static String toString(Boolean bool) {
		return bool.toString();
	}
	
	/**
	 * Serialize a JSON object into its string representation. If the argument
	 * object is neither of Map, List, String, Number, or Boolean, this method
	 * returns null. Likewise, it ignores all all content objects that are of
	 * neither of those five classes.
	 * @param obj the object to serialize
	 * @param quot the quotation mark to use for strings
	 * @return the string representation of the argument object 
	 */
	public static void writeJson(Writer out, Object obj) throws IOException {
		writeJson(out, obj, '"', null);
	}
	
	/**
	 * Serialize a JSON object into its string representation. If the argument
	 * object is neither of Map, List, String, Number, or Boolean, this method
	 * returns null. Likewise, it ignores all all content objects that are of
	 * neither of those five classes.
	 * @param obj the object to serialize
	 * @param indent the indentation to use per level, for pretty printing
	 * @return the string representation of the argument object 
	 */
	public static void writeJson(Writer out, Object obj, String indent) throws IOException {
		writeJson(out, obj, '"', indent);
	}
	
	/**
	 * Serialize a JSON object into its string representation. If the argument
	 * object is neither of Map, List, String, Number, or Boolean, this method
	 * returns null. Likewise, it ignores all content objects that are of
	 * neither of those five classes.
	 * @param obj the object to serialize
	 * @param quot the quotation mark to use for strings
	 * @return the string representation of the argument object 
	 */
	public static void writeJson(Writer out, Object obj, char quot) throws IOException {
		writeJson(out, obj, quot, null);
	}
	
	/**
	 * Serialize a JSON object into its string representation. If the argument
	 * object is neither of Map, List, String, Number, or Boolean, this method
	 * returns null. Likewise, it ignores all content objects that are of
	 * neither of those five classes.
	 * @param obj the object to serialize
	 * @param quot the quotation mark to use for strings
	 * @param indent the indentation to use per level, for pretty printing
	 * @return the string representation of the argument object 
	 */
	public static void writeJson(Writer out, Object obj, char quot, String indent) throws IOException {
		writeValueData(out, obj, quot, indent, "");
	}
	
	private static void writeValueData(Writer out, Object obj, char quot, String perLevelIndent, String currentLevelInden) throws IOException {
		if (obj instanceof Map)
			writeObject(out, ((Map) obj), quot, perLevelIndent, currentLevelInden);
		else if (obj instanceof List)
			writeArray(out, ((List) obj), quot, perLevelIndent, currentLevelInden);
		else if (obj instanceof String)
			writeString(out, ((String) obj), quot);
		else if (obj instanceof Number)
			writeNumber(out, ((Number) obj));
		else if (obj instanceof Boolean)
			writeBoolean(out, ((Boolean) obj));
		else if (obj == null)
			writeNull(out);
	}
	
	private static void writeObject(Writer out, Map object, char quot, String perLevelIndent, String currentLevelInden) throws IOException {
		String objectContentIndent = ((perLevelIndent == null) ? null : (currentLevelInden + perLevelIndent));
		int outValueCount = 0;
		out.write("{");
		for (Iterator kit = object.keySet().iterator(); kit.hasNext();) {
			Object key = ((String) kit.next());
			if (!(key instanceof String))
				continue;
			Object value = object.get(key);
			if (filterDataValue(value))
				continue;
			if (outValueCount != 0)
				out.write(",");
			if (perLevelIndent == null)
				out.write((outValueCount == 0) ? "" : " ");
			else {
				out.write("\r\n");
				out.write(objectContentIndent);
			}
			out.write(toString(((String) key), quot));
			out.write(": ");
			writeValueData(out, value, quot, perLevelIndent, objectContentIndent);
			outValueCount++;
			
		}
		if ((outValueCount != 0) && (perLevelIndent != null)) {
			out.write("\r\n");
			out.write(currentLevelInden);
		}
		out.write("}");
	}
	
	private static void writeArray(Writer out, List array, char quot, String perLevelIndent, String currentLevelInden) throws IOException {
		String arrayContentIndent = ((perLevelIndent == null) ? null : (currentLevelInden + perLevelIndent));
		int outValueCount = 0;
		out.write("[");
		for (int i = 0; i < array.size(); i++) {
			Object value = array.get(i);
			if (filterDataValue(value))
				continue;
			if (outValueCount != 0)
				out.write(",");
			if (perLevelIndent == null)
				out.write((outValueCount == 0) ? "" : " ");
			else {
				out.write("\r\n");
				out.write(arrayContentIndent);
			}
			writeValueData(out, value, quot, perLevelIndent, arrayContentIndent);
			outValueCount++;
		}
		if ((outValueCount != 0) && (perLevelIndent != null)) {
			out.write("\r\n");
			out.write(currentLevelInden);
		}
		out.write("]");
	}
	
	private static boolean filterDataValue(Object obj) {
		if (obj instanceof Map)
			return false;
		else if (obj instanceof List)
			return false;
		else if (obj instanceof String)
			return false;
		else if (obj instanceof Number)
			return false;
		else if (obj instanceof Boolean)
			return false;
		else return (obj != null);
	}
	
	private static void writeString(Writer out, String string, char quot) throws IOException {
		out.write("" + quot + escape(string, quot) + quot);
	}
	
	private static void writeNumber(Writer out, Number number) throws IOException {
		out.write(number.toString());
	}
	
	private static void writeBoolean(Writer out, Boolean bool) throws IOException {
		out.write(bool.toString());
	}
	
	private static void writeNull(Writer out) throws IOException {
		out.write("null");
	}
	
	/**
	 * Exception reporting that the parser encountered an invalid character at
	 * some point in the stream while parsing it. The <code>getPosition()</code>
	 * method indicates where the offending character is located.
	 * 
	 * @author sautter
	 */
	public static class UnexpectedCharacterException extends IOException {
		private int position;
		UnexpectedCharacterException(char ch, int position, String expected) {
			super("Unexpected character '" + ch + "' (0x" + ((ch < 0x1000) ? "0" : "") + ((ch < 0x0100) ? "0" : "") + Integer.toString(((int) ch), 16).toUpperCase() + ") at " + position + ", expected " + expected + ".");
			this.position = position;
		}
		
		/**
		 * Obtain the position of the offending character causing the exception
		 * as its offset from the start of the parsed character stream.
		 * @return the position of the offending character
		 */
		public int getPosition() {
			return this.position;
		}
	}
	
	/**
	 * Exception reporting that the parser encountered an invalid character at
	 * some point in the stream while parsing it. The <code>getPosition()</code>
	 * method indicates where the offending character is located.
	 * 
	 * @author sautter
	 */
	public static class MissingCharacterException extends IOException {
		MissingCharacterException(String expected) {
			super("Missing character at end of input, expected " + expected + ".");
		}
	}
	
	/**
	 * Parse JSON data from a character stream.
	 * @param in the reader to read from
	 * @return the de-serialized object
	 * @throws IOException
	 */
	public static Object parseJson(Reader in) throws IOException {
		return cropNext(new JsonReader(in), false);
	}
	
	/**
	 * Stream JSON data from a character stream and inform a listener of parsed
	 * content, akin to SAX access to an XML data stream. This is useful in
	 * situations where client code only needs a small portion of the JSON data
	 * provided by the argument stream.
	 * @param in the reader to read from
	 * @param receiver the receiver to notify about parsed content
	 * @throws IOException
	 */
	public static void streamJson(Reader in, JsonReceiver receiver) throws IOException {
		streamNext(new JsonReader(in, receiver.getMinimumLookahead()), false, receiver);
	}
	
	private static class JsonReader extends PeekReader {
		int position = 0;
		JsonReader(Reader in) throws IOException {
			super(in, "false".length());
		}
		JsonReader(Reader in, int minLookahead) throws IOException {
			super(in, Math.max("false".length(), minLookahead));
		}
		public int read() throws IOException {
			int read = super.read();
			if (read != -1)
				this.position++;
			return read;
		}
		public int read(char[] cbuf, int off, int len) throws IOException {
			int read = super.read(cbuf, off, len);
			if (read != -1)
				this.position += read;
			return read;
		}
		public long skip(long n) throws IOException {
			long skipped = super.skip(n);
			this.position += ((int) (skipped & 0x7FFFFFF));
			return skipped;
		}
	}
	
	private static Object cropNext(JsonReader jr, boolean inArrayOrObject) throws IOException {
		jr.skipSpace();
		if (jr.peek() == '"')
			return cropString(jr, '"', true);
		else if (jr.peek() == '\'')
			return cropString(jr, '\'', true);
		else if (jr.peek() == '{')
			return cropObject(jr);
		else if (jr.peek() == '[')
			return cropArray(jr);
		else if ("-0123456789".indexOf(jr.peek()) != -1)
			return cropNumber(jr);
		else if (jr.startsWith("null", false)) {
			jr.skip("null".length());
			return null;
		}
		else if (jr.startsWith("true", false)) {
			jr.skip("true".length());
			return Boolean.TRUE;
		}
		else if (jr.startsWith("false", false)) {
			jr.skip("false".length());
			return Boolean.FALSE;
		}
		else if (inArrayOrObject && ((jr.peek() == ',') || (jr.peek() == '}') || (jr.peek() == ']')))
			return null;
		else throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, ("\", ', {, [, -, digits, " + (inArrayOrObject ? "comma, }, ], " : "" ) + "'true', 'false', or 'null'"));
	}
	private static void streamNext(JsonReader jr, boolean inArrayOrObject, JsonReceiver receiver) throws IOException {
		jr.skipSpace();
		if (jr.peek() == '"')
			receiver.stringRead(cropString(jr, '"', true));
		else if (jr.peek() == '\'')
			receiver.stringRead(cropString(jr, '\'', true));
		else if (jr.peek() == '{')
			streamObject(jr, receiver);
		else if (jr.peek() == '[')
			streamArray(jr, receiver);
		else if ("-0123456789".indexOf(jr.peek()) != -1)
			receiver.numberRead(cropNumber(jr));
		else if (jr.startsWith("null", false)) {
			jr.skip("null".length());
			receiver.nullRead();
		}
		else if (jr.startsWith("true", false)) {
			jr.skip("true".length());
			receiver.booleanRead(true);
		}
		else if (jr.startsWith("false", false)) {
			jr.skip("false".length());
			receiver.booleanRead(false);
		}
		else if (inArrayOrObject && ((jr.peek() == ',') || (jr.peek() == '}') || (jr.peek() == ']')))
			return;
		else if (receiver.startsCustom(((char) jr.peek()), jr))
			receiver.readCustom(jr);
		else throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, ("\", ', {, [, -, digits, " + (inArrayOrObject ? "comma, }, ], " : "" ) + "'true', 'false', or 'null'"));
	}
	
	private static Map cropObject(JsonReader jr) throws IOException {
		jr.read(); // consume opening curly bracket
		jr.skipSpace();
		Map object = new LinkedHashMap();
		while (jr.peek() != '}') {
			if ((jr.peek() != '"') && (jr.peek() != '\''))
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, "\" or '");
			String key = cropString(jr, ((char) jr.peek()), false);
			jr.skipSpace();
			if (jr.peek() != ':')
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, ":");
			jr.read(); // consume colon
			Object value = cropNext(jr, true);
			object.put(key, value);
			jr.skipSpace();
			if (jr.peek() == ',') {
				jr.read(); // consume comma (also consumes a dangling one)
				jr.skipSpace();
			}
			else if (jr.peek() != '}')
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, "comma or }");
		}
		jr.read(); // consume closing curly bracket
		return object;
	}
	private static void streamObject(JsonReader jr, JsonReceiver receiver) throws IOException {
		jr.read(); // consume opening curly bracket
		jr.skipSpace();
		receiver.objectStarted();
		while (jr.peek() != '}') {
			if ((jr.peek() != '"') && (jr.peek() != '\''))
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, "\" or '");
			String key = cropString(jr, ((char) jr.peek()), false);
			jr.skipSpace();
			receiver.objectPropertyRead(key);
			if (jr.peek() != ':')
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, ":");
			jr.read(); // consume colon
			streamNext(jr, true, receiver);
			jr.skipSpace();
			if (jr.peek() == ',') {
				jr.read(); // consume comma (also consumes a dangling one)
				jr.skipSpace();
			}
			else if (jr.peek() != '}')
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, "comma or }");
		}
		jr.read(); // consume closing curly bracket
		receiver.objectEnded();
	}
	
	private static List cropArray(JsonReader jr) throws IOException {
		jr.read(); // consume opening square bracket
		jr.skipSpace();
		List array = new ArrayList();
		while (jr.peek() != ']') {
			Object value = cropNext(jr, true);
			array.add(value);
			jr.skipSpace();
			if (jr.peek() == ',') {
				jr.read(); // consume comma (also consumes a dangling one)
				jr.skipSpace();
			}
			else if (jr.peek() != ']')
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, "comma or ]");
		}
		jr.read(); // consume closing square bracket
		return array;
	}
	private static void streamArray(JsonReader jr, JsonReceiver receiver) throws IOException {
		jr.read(); // consume opening square bracket
		jr.skipSpace();
		receiver.arrayStarted();
		while (jr.peek() != ']') {
			streamNext(jr, true, receiver);
			jr.skipSpace();
			if (jr.peek() == ',') {
				jr.read(); // consume comma (also consumes a dangling one)
				jr.skipSpace();
			}
			else if (jr.peek() != ']')
				throw new UnexpectedCharacterException(((char) jr.peek()), jr.position, "comma or ]");
		}
		jr.read(); // consume closing square bracket
		receiver.arrayEnded();
	}
	
	private static String cropString(JsonReader jr, char quot, boolean isValues) throws IOException {
		jr.read(); // consume opening quotes
		boolean escaped = false;
		StringBuffer string = new StringBuffer();
		while (jr.peek() != -1) {
			char ch = ((char) jr.read());
			if (escaped) {
				if (ch == 'b')
					string.append("\b");
				else if (ch == 'f')
					string.append("\f");
				else if (ch == 'n')
					string.append("\n");
				else if (ch == 'r')
					string.append("\r");
				else if (ch == 't')
					string.append("\t");
				else if (ch == 'u') {
					StringBuffer hex = new StringBuffer();
					hex.append((char) jr.read());
					hex.append((char) jr.read());
					hex.append((char) jr.read());
					hex.append((char) jr.read());
					string.append((char) Integer.parseInt(hex.toString(), 16));
				}
				else string.append(ch);
				escaped = false;
			}
			else if (ch == '\\')
				escaped = true;
			else if (ch == quot) {
				quot = ((char) 0); // indicate string was properly terminated
				break;
			}
			else string.append(ch);
		}
		if (quot != 0) // check whether or not string was properly terminated
			throw new MissingCharacterException("" + quot);
		return string.toString();
	}
	
	private static Number cropNumber(JsonReader jr) throws IOException {
		StringBuffer numBuf = new StringBuffer();
		if (jr.peek() == '-') {
			numBuf.append((char) jr.peek());
			jr.read();
			jr.skipSpace();
		}
		StringBuffer intBuf = new StringBuffer();
		while ("0123456789".indexOf(jr.peek()) != -1) {
			numBuf.append((char) jr.peek());
			intBuf.append((char) jr.read());
		}
		jr.skipSpace();
		StringBuffer fracBuf = new StringBuffer();
		if (jr.peek() == '.') {
			numBuf.append((char) jr.read());
			while ("0123456789".indexOf(jr.peek()) != -1) {
				numBuf.append((char) jr.peek());
				fracBuf.append((char) jr.read());
			}
		}
		jr.skipSpace();
		StringBuffer expBuf = new StringBuffer();
		if ((jr.peek() == 'e') || (jr.peek() == 'E')) {
			numBuf.append('e');
			jr.read();
			if (jr.peek() == '-') {
				numBuf.append((char) jr.read());
				jr.skipSpace();
			}
			else if (jr.peek() == '+') {
				jr.read();
				jr.skipSpace();
			}
			while ("0123456789".indexOf(jr.peek()) != -1) {
				numBuf.append((char) jr.peek());
				expBuf.append((char) jr.read());
			}
		}
		Number num;
		if ((fracBuf.length() + expBuf.length()) == 0)
			num = new Long(numBuf.toString());
		else num = new Double(numBuf.toString());
		return num;
	}
	
	//	!!! EXCLSIVELY FOR TEST PURPOSES !!!
	public static void main(String[] args) throws Exception {
		String json = "{" +
					"\"id\": \"113503575767148437762\"," +
					"\"float\": 0.5," +
//					"\"name\": \"Guido Sautter\"," +
//					"\"given_name\": \"Guido\"," +
//					"\"family_name\": \"Sautter\"," +
					"\"name\": {" +
						"\"full\": \"Guido\\n Sautter\"," +
						"\"given_name\": \"Guido\"," +
						"\"family_name\": \"Sautter\"" +
					"}," +
					"\"link\": \"https://plus.google.com/113503575767148437762\"," +
					"\"gender\": \"male\"," +
					"\"married\": false," +
					"\"locale\": \"en\"," +
					"\"locales\": [-3.8e7, {\"primary\": \"en\"}, \"de\", \"fr\"]," +
					"\"end\": \"right here\"" +
				"}";
		Object obj = parseJson(new StringReader(json));
		System.out.println(obj);
		
		StringWriter sw = new StringWriter();
		writeJson(sw, obj, "    ");
		System.out.println(sw.toString());
//		
//		streamJson(new StringReader(json), new JsonReceiver() {
//			LinkedList stack = new LinkedList();
//			String indent = "";
//			public void arrayStarted() {
//				System.out.println(this.indent + "[");
//				this.stack.addLast(new ArrayList());
//				this.indent = (this.indent + "  ");
//			}
//			public void arrayEnded() {
//				this.indent = this.indent.substring(2);
//				ArrayList array = ((ArrayList) this.stack.removeLast());
//				System.out.println(this.indent + "]");
//				this.storeObject(array);
//			}
//			public void objectStarted() {
//				System.out.println(this.indent + "{");
//				this.stack.addLast(new LinkedHashMap());
//				this.indent = (this.indent + "  ");
//			}
//			public void objectPropertyRead(String prop) {
//				this.stack.addLast(prop);
//				System.out.println(this.indent + "'" + prop + "':");
//			}
//			public void objectEnded() {
//				this.indent = this.indent.substring(2);
//				Map object = ((Map) this.stack.removeLast());
//				System.out.println(this.indent + "}");
//				this.storeObject(object);
//			}
//			public void stringRead(String str) {
//				System.out.println(this.indent + "'" + str + "'");
//				this.storeObject(str);
//			}
//			public void numberRead(Number num) {
//				System.out.println(this.indent + "" + num);
//				this.storeObject(num);
//			}
//			public void booleanRead(boolean bool) {
//				System.out.println(this.indent + "" + bool);
//				this.storeObject(Boolean.valueOf(bool));
//			}
//			public void nullRead() {
//				System.out.println(this.indent + "null");
//				this.storeObject(null);
//			}
//			private void storeObject(Object object) {
//				if (this.stack.isEmpty())
//					System.out.println("RESULT: " + object);
//				else if (this.stack.getLast() instanceof String) {
//					Object key = this.stack.removeLast();
//					((Map) this.stack.getLast()).put(key, object);
//				}
//				else ((ArrayList) this.stack.getLast()).add(object);
//			}
//		});
	}
}