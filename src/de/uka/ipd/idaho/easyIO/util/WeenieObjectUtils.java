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


/**
 * Utility library with helper functions for storing small objects as records
 * in a byte array rather than instantiating all objects up front. This is
 * helpful for large lookup data structures, which need to hold large numbers
 * of objects just in order to have them searchable, with very few of these
 * objects actually being used over the lifetime of the data structure object
 * proper. In particular, the per-instance overhead of Java objects can become
 * too high a price to pay if the the number of objects is vast, but the
 * overall memory footprint of the data structure has to stay as small as
 * possible.
 * 
 * @author sautter
 */
public class WeenieObjectUtils {
	private WeenieObjectUtils() { /* no instance */ }
	
//	private static class IntBuffer {
//		private int[] ints = new int[16];
//		private int size = 0;
//		IntBuffer() {}
//		void add(int i) {
//			if (this.size == this.ints.length)
//				this.ints = doubleLength(this.ints);
//			this.ints[this.size++] = i;
//		}
//		void addAll(int[] ints) {
//			while (this.ints.length <= (this.size + ints.length))
//				this.ints = doubleLength(this.ints);
//			System.arraycopy(ints, 0, this.ints, this.size, ints.length);
//			this.size += ints.length;
//		}
//		int size() {
//			return this.size;
//		}
//		int get(int index) {
//			return this.ints[index];
//		}
//		int[] toArray() {
//			return trimLength(this.ints, this.size);
//		}
//		void finishResult() {
//			Arrays.sort(this.ints, 0, this.size);
//			int removed = 0;
//			for (int i = 1; i < this.size; i++) {
//				if (this.ints[i] == this.ints[i-1])
//					removed++;
//				else if (removed != 0)
//					this.ints[i-removed] = this.ints[i];
//			}
//			this.size -= removed;
//		}
//	}
//	
//	static int[] doubleLength(int[] ints) {
//		int[] cInts = new int[ints.length * 2];
//		System.arraycopy(ints, 0, cInts, 0, ints.length);
//		return cInts;
//	}
//	
//	static int[] trimLength(int[] ints, int length) {
//		if (length < ints.length) {
//			int[] cInts = new int[length];
//			System.arraycopy(ints, 0, cInts, 0, cInts.length);
//			return cInts;
//		}
//		else return ints;
//	}
	
	/**
	 * Read an atomic byte from a given offset in an array of bytes. Managing
	 * boundaries and array size is the responsibility of client code.
	 * @param data the byte array to read the atomic byte from
	 * @param offset the offset of the first byte
	 * @return the requested byte
	 */
	public static byte getByte(byte[] data, int offset) {
		return data[offset];
	}
	
	/**
	 * Store an atomic byte in a byte array at a given offset, using 1 byte.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param b the atomic byte to store
	 * @param data the byte array to store the atomic byte in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeByte(byte b, byte[] data, int offset) {
		data[offset] = b;
		return 1;
	}
	
	/**
	 * Read an atomic short from a given offset in an array of bytes, using 2
	 * bytes. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the atomic short from
	 * @param offset the offset of the first byte
	 * @return the requested short
	 */
	public static short getShort(byte[] data, int offset) {
		return getShort(data, offset, 2);
	}
	
	/**
	 * Read an atomic short from a given offset in an array of bytes. Managing
	 * boundaries and array size is the responsibility of client code. The
	 * argument length is the number of bytes to use (sensible values are 1 and
	 * 2).
	 * @param data the byte array to read the atomic short from
	 * @param offset the offset of the first byte
	 * @param length the number of bytes the short is encoded in
	 * @return the requested short
	 */
	public static short getShort(byte[] data, int offset, int length) {
		short s = 0;
		for (int b = 0; b < length; b++) {
			s <<= 8;
			s |= (data[offset + b] & 0xFF);
		}
		return s;
	}
	
	/**
	 * Store an atomic short in a byte array at a given offset, using 2 bytes.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param s the atomic short to store
	 * @param data the byte array to store the atomic short in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeShort(short s, byte[] data, int offset) {
		return storeShort(s, data, offset, 2);
	}
	
	/**
	 * Store an atomic short in a byte array at a given offset, using a given
	 * number of bytes. The argument length is the number of bytes to use,
	 * counting from the lower end of the argument short (sensible values are 1
	 * and 2). Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param l the atomic long to store
	 * @param data the byte array to store the atomic long in
	 * @param offset the offset of the first byte
	 * @param length the number of bytes to store
	 * @return the number of bytes used
	 */
	public static int storeShort(short s, byte[] data, int offset, int length) {
		for (int b = (length - 1); b >= 0; b--) {
			int sb = (s & 0x000000FF);
			if (0x0000007F < sb)
				sb -= 0x00000100;
			data[offset + b] = ((byte) sb);
			s >>>= 8;
		}
		return length;
	}
	
	/**
	 * Read an atomic int from a given offset in an array of bytes, using 4
	 * bytes. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the atomic int from
	 * @param offset the offset of the first byte
	 * @return the requested int
	 */
	public static int getInt(byte[] data, int offset) {
		return getInt(data, offset, 4);
	}
	
	/**
	 * Read an atomic int from a given offset in an array of bytes. Managing
	 * boundaries and array size is the responsibility of client code. The
	 * argument length is the number of bytes to use (sensible values are 1-4).
	 * @param data the byte array to read the atomic int from
	 * @param offset the offset of the first byte
	 * @param length the number of bytes the int is encoded in
	 * @return the requested int
	 */
	public static int getInt(byte[] data, int offset, int length) {
		int i = 0;
		for (int b = 0; b < length; b++) {
			i <<= 8;
			i |= (data[offset + b] & 0xFF);
		}
		return i;
	}
	
	/**
	 * Store an atomic int in a byte array at a given offset, using 4 bytes.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param i the atomic int to store
	 * @param data the byte array to store the atomic int in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeInt(int i, byte[] data, int offset) {
		return storeInt(i, data, offset, 4);
	}
	
	/**
	 * Store an atomic int in a byte array at a given offset, using a given
	 * number of bytes. The argument length is the number of bytes to use,
	 * counting from the lower end of the argument int (sensible values are
	 * 1-4). Managing boundaries and array size is the responsibility of client
	 * code.
	 * @param i the atomic int to store
	 * @param data the byte array to store the atomic int in
	 * @param offset the offset of the first byte
	 * @param length the number of bytes to store
	 * @return the number of bytes used
	 */
	public static int storeInt(int i, byte[] data, int offset, int length) {
		for (int b = (length - 1); b >= 0; b--) {
			int ib = (i & 0x000000FF);
			if (0x0000007F < ib)
				ib -= 0x00000100;
			data[offset + b] = ((byte) ib);
			i >>>= 8;
		}
		return length;
	}
	
	/**
	 * Read an atomic long from a given offset in an array of bytes, using 8
	 * bytes. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the atomic long from
	 * @param offset the offset of the first byte
	 * @return the requested long
	 */
	public static long getLong(byte[] data, int offset) {
		return getLong(data, offset, 8);
	}
	
	/**
	 * Read an atomic long from a given offset in an array of bytes. Managing
	 * boundaries and array size is the responsibility of client code. The
	 * argument length is the number of bytes to use (sensible values are 1-8).
	 * @param data the byte array to read the atomic long from
	 * @param offset the offset of the first byte
	 * @param length the number of bytes the long is encoded in
	 * @return the requested long
	 */
	public static long getLong(byte[] data, int offset, int length) {
		long l = 0;
		for (int b = 0; b < length; b++) {
			l <<= 8;
			l |= (data[offset + b] & 0xFF);
		}
		return l;
	}
	
	/**
	 * Store an atomic long in a byte array at a given offset, using 8 bytes.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param l the atomic long to store
	 * @param data the byte array to store the atomic long in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeLong(long l, byte[] data, int offset) {
		return storeLong(l, data, offset, 8);
	}
	
	/**
	 * Store an atomic long in a byte array at a given offset, using a given
	 * number of bytes. The argument length is the number of bytes to use,
	 * counting from the lower end of the argument long (sensible values are
	 * 1-8). Managing boundaries and array size is the responsibility of client
	 * code.
	 * @param l the atomic long to store
	 * @param data the byte array to store the atomic long in
	 * @param offset the offset of the first byte
	 * @param length the number of bytes to store
	 * @return the number of bytes used
	 */
	public static int storeLong(long l, byte[] data, int offset, int length) {
		for (int b = (length - 1); b >= 0; b--) {
			int lb = ((int) (l & 0x00000000000000FF));
			if (0x0000007F < lb)
				lb -= 0x00000100;
			data[offset + b] = ((byte) lb);
			l >>>= 8;
		}
		return length;
	}
	
	/**
	 * Read an atomic float from a given offset in an array of bytes, using 4
	 * bytes. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the atomic float from
	 * @param offset the offset of the first byte
	 * @return the requested float
	 */
	public static float getFloat(byte[] data, int offset) {
		return Float.intBitsToFloat(getInt(data, offset, 4));
	}
	
	/**
	 * Store an atomic float in a byte array at a given offset, using 4 bytes.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param f the atomic float to store
	 * @param data the byte array to store the atomic float in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeFloat(float f, byte[] data, int offset) {
		return storeInt(Float.floatToIntBits(f), data, offset, 4);
	}
	
	/**
	 * Read an atomic double from a given offset in an array of bytes, using 8
	 * bytes. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the atomic double from
	 * @param offset the offset of the first byte
	 * @return the requested double
	 */
	public static double getDouble(byte[] data, int offset) {
		return Double.longBitsToDouble(getLong(data, offset, 8));
	}
	
	/**
	 * Store an atomic double in a byte array at a given offset, using 8 bytes.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param d the atomic double to store
	 * @param data the byte array to store the atomic double in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeDouble(double d, byte[] data, int offset) {
		return storeLong(Double.doubleToLongBits(d), data, offset, 8);
	}
	
	/**
	 * Read an atomic char from a given offset in an array of bytes, using 2
	 * bytes. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the atomic char from
	 * @param offset the offset of the first byte
	 * @return the requested char
	 */
	public static char getChar(byte[] data, int offset) {
		int c = getInt(data, offset, 2);
		return ((char) (c & 0x0000FFFF));
	}
	
	/**
	 * Store an atomic char in a byte array at a given offset, using 2 bytes.
	 * Managing boundaries and array size is the responsibility of client code.
	 * @param c the atomic char to store
	 * @param data the byte array to store the atomic char in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeChar(char c, byte[] data, int offset) {
		return storeInt(((int) c), data, offset, 2);
	}
	
	/**
	 * Read a string from a given offset in an array of bytes, using 1 byte per
	 * character. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param data the byte array to read the string from
	 * @param offset the offset of the first byte
	 * @param length the length of the string
	 * @return the requested string
	 */
	public static String getStringAscii(byte[] data, int offset, int length) {
		char[] str = new char[length];
		for (int c = 0; c < str.length; c++)
			str[c] = ((char) (data[offset + c] & 0x00FF));
		return new String(str);
	}
	
	/**
	 * Encode a string in a byte array, using 1 byte per character. The
	 * returned array has the same length as the argument string. This method
	 * is mainly a convenience for encoding strings for use in the
	 * <code>compareStringsAscii()</code> method.
	 * @param str the string to encode
	 * @return the encoded string
	 */
	public static byte[] encodeStringAscii(String str) {
		byte[] bytes = new byte[str.length()];
		storeStringAscii(str, bytes, 0);
		return bytes;
	}
	
	/**
	 * Store a string in a byte array at a given offset, using 1 byte per
	 * character. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param str the string to store
	 * @param data the byte array to store the string in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeStringAscii(String str, byte[] data, int offset) {
		for (int c = 0; c < str.length(); c++) {
			char ch = str.charAt(c);
			if (0x007F < ch)
				throw new IllegalArgumentException("Invalid character " + ch + " (0x" + Integer.toString(((int) ch), 16).toUpperCase() + ") in '" + str + "' at " + c);
			data[offset + c] = ((byte) (ch & 0x00FF));
		}
		return str.length();
	}
	
	/**
	 * Compare two strings stored in byte arrays at given offsets, assuming 1
	 * byte per character. Managing boundaries and array size is the
	 * responsibility of client code.
	 * @param data1 the byte array holding the first string
	 * @param offset1 the offset of the first character of the first string
	 * @param length1 the length of the first string
	 * @param data2 the byte array holding the second string
	 * @param offset2 the offset of the first character of the second string
	 * @param length2 the length of the first string
	 * @param caseSensitive compare case sensitive?
	 * @return a value less than, equal to, or larger than 0, indicating that
	 *            the first string is less than, equal to, or larger than the
	 *            second string, respectively
	 */
	public static int compareStringsAscii(byte[] data1, int offset1, int length1, byte[] data2, int offset2, int length2, boolean caseSensitive) {
		for (int b = 0; b < Math.min(length1, length2); b++) {
			byte b1 = data1[offset1 + b];
			byte b2 = data2[offset2 + b];
			if (b1 == b2)
				continue;
			if (caseSensitive)
				return (b1 - b2);
			/* setting 32-bit (0x20), the 'lower case bit', is basically
			 * Character.toLowerCase() in Basic Latin, but way faster */
			if (isLetter(b1) && isLetter(b2)) {
				b1 = ((byte) (b1 | 0x20));
				b2 = ((byte) (b2 | 0x20));
				if (b1 == b2)
					continue;
			}
			return (b1 - b2);
		}
		return (length1 - length2);
	}
	private static boolean isLetter(byte ch) {
		/* going high-to-low evaluates fewer conditions for lower case
		 * letters, which are far more frequent than upper case ones */
		if ('z' < ch)
			return false;
		else if ('a' <= ch)
			return true;
		else if ('Z' < ch)
			return false;
		else if ('A' <= ch)
			return true;
		else return false;
	}
	
	/**
	 * Read a string from a given offset in an array of bytes, using 2 bytes
	 * per character. The argument length is in characters, so this method
	 * reads two times as many bytes. Managing boundaries and array size is the
	 * responsibility of client code.
	 * @param data the byte array to read the string from
	 * @param offset the offset of the first byte
	 * @param length the length of the string (in characters)
	 * @return the requested string
	 */
	public static String getStringUnicode(byte[] data, int offset, int length) {
		char[] str = new char[length];
		for (int c = 0; c < str.length; c++)
			str[c] = getChar(data, (offset + (c * 2)));
		return new String(str);
	}
	
	/**
	 * Encode a string in a byte array, using 2 bytes per character. The
	 * returned array has twice the length of the argument string. This method
	 * is mainly a convenience for encoding strings for use in the
	 * <code>compareStringsUnicode()</code> method.
	 * @param str the string to encode
	 * @return the encoded string
	 */
	public static byte[] encodeStringUnicode(String str) {
		byte[] bytes = new byte[str.length() * 2];
		storeStringUnicode(str, bytes, 0);
		return bytes;
	}
	
	/**
	 * Store a string in a byte array at a given offset, using 2 bytes per
	 * character. Managing boundaries and array size is the responsibility of
	 * client code.
	 * @param str the string to store
	 * @param data the byte array to store the string in
	 * @param offset the offset of the first byte
	 * @return the number of bytes used
	 */
	public static int storeStringUnicode(String str, byte[] data, int offset) {
		for (int c = 0; c < str.length(); c++)
			storeChar(str.charAt(c), data, (offset + (c * 2)));
		return (str.length() * 2);
	}
	
	/**
	 * Compare two strings stored in byte arrays at given offsets, assuming 2
	 * bytes per character. Managing boundaries and array size is the
	 * responsibility of client code.
	 * @param data1 the byte array holding the first string
	 * @param offset1 the offset of the first character of the first string
	 * @param length1 the length of the first string
	 * @param data2 the byte array holding the second string
	 * @param offset2 the offset of the first character of the second string
	 * @param length2 the length of the first string
	 * @param caseSensitive compare case sensitive?
	 * @return a value less than, equal to, or larger than 0, indicating that
	 *            the first string is less than, equal to, or larger than the
	 *            second string, respectively
	 */
	public static int compareStringsUnicode(byte[] data1, int offset1, int length1, byte[] data2, int offset2, int length2, boolean caseSensitive) {
		for (int b = 0; b < Math.min(length1, length2); b++) {
			char c1 = getChar(data1, (offset1 + (b * 2)));
			char c2 = getChar(data2, (offset2 + (b * 2)));
			if (c1 == c2)
				continue;
			if (caseSensitive)
				return (c1 - c2);
			if (Character.isLetter(c1) && Character.isLetter(c2)) {
				c1 = Character.toLowerCase(c1);
				c2 = Character.toLowerCase(c2);
				if (c1 == c2)
					continue;
			}
			return (c1 - c2);
		}
		return (length1 - length2);
	}
	
	/*
TODO Move whole bit pushing methods (plus some missing ones for other primitive types) to central place:
==> we already duplicated them once ...
- provide startsWith() and endsWith() methods, offering above semantics
- provide public static PatternMatcher class (akin to taxon name indexer wildcard search) ...
- ... offering matches() method with above byte semantics
  ==> charAt() and length() might get somewhat tricky with UTF-8, but we can figure that out
- provide conversion methods to and from hex for primitive data types (and arrays thereof)
  ==> might move that over from RandomByteSource or HashUtils
  ==> add optional "lower case" flag

TODO ALSO, simply use UTF-16BE (or whatever the leading 0s are ...) in WeenieObjectUtils:
- just provide ASCII, ANSI, and UTF-16, forget about UTF-8 (too much hassle to compare)
- provide same options in string comparison methods
- provide same options in startsWith(), endsWith(), and matches() methods ...
- ... using respective CharSequence implementations
==> and then, do provide UTF-8, but with hint that processing might be slower (if more compact) ...
==> ... and internally decode to and compare UTF-16

TODO Put WeenieObjectUtils in to-create EasyIO.primitives ...
... and add some other data structures for each primitive type:
- mainly, add lists/buffers for each type (with array doubling functions) ...
- ... named PrimitiveXyzList
- most likely also add sets ...
- ... named PrimitiveXyzSet
- ... self-sorting on additions ...
- ... and using binary search for lookups
- most likely, also add maps with primitive keys and objects for values:
  - name PrimitiveXyzKeyedMap
  - use primitive array for keys ...
  - ... and parallel object array for values

TODO Add list compression for sequential numbers for all primitive integer types in WeenieObjectUtils:
- make damn clear in JavaDoc that compression only viable for lists of non-negative numbers
- provide argument specifying number of bytes to use for sequence length

TODO Enable WeenieObjectUtils to work with larger arrays:
- allow using short[], nt[], and long[] with current byte[]-bound signatures
  ==> allows client code more freedom as to what type of array to use
- setting byte in larger primitive type pretty much is:
  - setting to zeros using AND with respective (otherwise FF) bitmask ...
  - ... and then setting via OR with respectively shifted value
- implementation basically copies what exists ...
- ... adding modulo <byteCount> and byte offset computations
  ==> keep above bitmasks in arrays, to ue with modulus
==> facilitates storing up to 16 GB (minus 8 bytes) in primitive long[], with Integer.MAX_VALUE (2 GB minus 1 byte) still array size limit
==> also, rename whole thing "WeenyObjectUtils" ...
==> ... and update ColLocal accordingly
  ==> best copy whole thing to avoid any dependency trouble ...
  ==> ... and add above extensions to new version
	 */
	
	//	FOR TEST PURPOSES ONLY
	public static void main(String[] args) {
		//	TODO use this for testing
	}
}
