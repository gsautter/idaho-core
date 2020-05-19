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
package de.uka.ipd.idaho.easyIO.util;

import java.util.Random;


/**
 * Generator for sequences of random bits. Whatever the requested output format,
 * this class generates random bit sequences in blocks of 16 bits internally,
 * using an invocation of Math.random() and a subsequent multiplication times
 * 65536.
 * 
 * @author sautter
 */
public class RandomByteSource {
	
	/**
	 * @return a new 128 bit SQL style GUID in it's hex representation as a
	 *         String
	 */
	public static String getGUID() {
		return getID(128);
	}
	
	/**
	 * @return a new ID of bitSize (rounded up to the next multiple of 16) bits
	 *         in it's hex representation (with a leading '0x') as a String
	 */
	public static String getID(int bitSize) {
		StringBuffer id = new StringBuffer(2 + ((int) Math.ceil(((double) bitSize) / 4)));
		id.append("0x");
		char[] chars = charsToHex(getRandomBits(bitSize));
		for (int i = 0; i < chars.length; i++)
			id.append(chars[i]);
		return id.toString();
	}
	
	/**
	 * @return bitSize (rounded up to the next multiple of 16) bits pack in
	 *         blocks of 8 in a byte array
	 */
	public static byte[] getRandomBytes(int bitSize) {
		char[] chars = getRandomBits(bitSize);
		byte[] ret = new byte[chars.length * 2];
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			ret[(2*i)+1] = ((byte) (c & 255));
			c >>= 8;
			ret[2*i] = ((byte) (c & 255));
		}
		return ret;
	}
	
	/**
	 * @return bitSize (rounded up to the next multiple of 16) bits pack in
	 *         blocks of 16 in a char array
	 */
	public static char[] getRandomChars(int bitSize) {
		return getRandomBits(bitSize);
	}

	/**
	 * @return bitSize (rounded up to the next multiple of 32) bits pack in
	 *         blocks of 32 in an int array
	 */
	public static int[] getRandomInts(int bitSize) {
		int size = bitSize;
		while ((size & 31) > 0) size++;	//	rise size to the next multiple of 32
		char[] chars = getRandomBits(size);
		int[] ret = new int[chars.length / 2];
		for (int i = 0; i < chars.length; i+=2) {
			int t = ((int) chars[i+1]);
			t <<= 16;
			t |= chars[i];
			ret[i/2] = t;
		}
		return ret;
	}
	
	/**
	 * @return bitSize (rounded up to the next multiple of 64) bits pack in
	 *         blocks of 64 in a long array
	 */
	public static long[] getRandomLongs(int bitSize) {
		int size = bitSize;
		while ((size & 63) > 0)
			size++; // rise size to the next multiple of 64
		char[] chars = getRandomBits(size);
		long[] ret = new long[chars.length / 4];
		for (int i = 0; i < chars.length; i += 4) {
			long l = ((int) chars[i + 3]);
			for (int j = 0; j < 3; j++) {
				l <<= 16;
				l |= chars[i + (2 - j)];
			}
			ret[i / 4] = l;
		}
		return ret;
	}

	/**
	 * @return the hex representation of the bytes in the specified array, each
	 *         char representing 4 bits
	 */
	public static char[] getHexCode(byte[] bytes) {
		int size = bytes.length;
		int stuff = 0;
		if ((size & 1) > 0) stuff = -1;
		char[] temp = new char[(size + 1) / 2];
		for (int i = stuff; i < size; i+=2) {
			char c = ((i == -1) ? ((char) 0) : ((char) bytes[i]));
			c <<= 8;
			c |= ((char) bytes[i+1]);
			temp[(i-stuff) / 2] = c;
		}
		return charsToHex(temp);
	}
	
	/**
	 * @return the hex representation of the chars in the specified array, each
	 *         char representing 4 bits
	 */
	public static char[] getHexCode(char[] chars) {
		return charsToHex(chars);
	}

	/**
	 * @return the hex representation of the ints in the specified array, each
	 *         char representing 4 bits
	 */
	public static char[] getHexCode(int[] ints) {
		int size = ints.length;
		char[] temp = new char[size * 2];
		for (int i = 0; i < size; i++) {
			int t = ints[i];
			temp[(2 * i) + 1] = ((char) (t & 65535));
			t >>= 16;
			temp[2 * i] = ((char) (t & 65535));
		}
		return charsToHex(temp);
	}

	/**
	 * @return the hex representation of the longs in the specified array, each
	 *         char representing 4 bits
	 */
	public static char[] getHexCode(long[] longs) {
		int size = longs.length;
		char[] temp = new char[size * 4];
		for (int i = 0; i < size; i++) {
			long l = longs[i];
			for (int j = 0; j < 4; j++) {
				temp[(2*i)+(3-j)] = ((char) (l & 65535));
				l >>= 16;
			}
		}
		return charsToHex(temp);
	}
	
	//	convert each 16 bit block (char) in bits into it's four-char hex representation
	private static char[] charsToHex(char[] bits) {
		int size = bits.length;
		char[] ret = new char[size * 4];
		for (int i = 0; i < size; i++) {
			char c = bits[i];
			for (int j = 0; j < 4; j++) {
				byte b = ((byte) (c & 0x0F));
//				ret[((4*i) + (3-j))] = ((b < 10) ? ((char) (b + '0')) : ((char) (b + '7'))); //	'7' = 'A' - 10
				ret[((4*i) + (3-j))] = ((b < 10) ? ((char) (b + '0')) : ((char) (b - 10 + 'A')));
				c >>= 4;
			}
		}
		return ret;
	}
	
	private static final Random random = new Random();
	
	//	generate bitSize (a multiple of 16) random bits, packed in a char array
	private static char[] getRandomBits(int bitSize) {
		int charSize = bitSize;
		while ((charSize & 0x0F) > 0)
			charSize++;	// rise size to the next multiple of 16
		charSize >>= 4;
		char[] ret = new char[charSize];
		for (int i = 0; i < charSize; i++)
			ret[i] = ((char) random.nextInt(0x00010000));
		return ret;
	}
	
	private static final String hexDigits = "0123456789ABCDEF";
	
	/**
	 * Compute the bit-wise negation of a hexadecimal string. The argument
	 * string has to contain valid HEX characters only.
	 * @param hex the hex string
	 * @return the NOT of the argument hex string
	 */
	public static String getHexNot(String hex) {
		hex = hex.toUpperCase();
		return getNotBasePow2(hex, hexDigits);
	}
	
	/**
	 * Compute the bit-wise negation of bit strings encoded in a set of base
	 * characters. The argument string has to  contain only characters
	 * contained in the base character set. Furthermore, the size of the base
	 * character set has to be a power of 2 for the result to be meaningful.
	 * @param str the encoded bit string
	 * @param baseChars the character set the two other arguments are encoded in
	 * @return the NOT of the argument bit string
	 */
	public static String getNotBasePow2(String str, String baseChars) {
		StringBuffer not = new StringBuffer();
		int mask = (baseChars.length() - 1);
		for (int c = 0; c < str.length(); c++) {
			int i = baseChars.indexOf(str.charAt(c));
			not.append(baseChars.charAt(~i & mask));
		}
		return not.toString();
	}
	
	/**
	 * Compute the bit-wise AND of two hexadecimal strings. The two argument
	 * strings have to be of the same length and contain valid HEX characters
	 * only.
	 * @param hex1 the first hex string
	 * @param hex2 the second hex string
	 * @return the AND of the two argument hex strings
	 */
	public static String getHexAnd(String hex1, String hex2) {
		hex1 = hex1.toUpperCase();
		hex2 = hex2.toUpperCase();
		return getAndBasePow2(hex1, hex2, hexDigits);
	}
	
	/**
	 * Compute the bit-wise AND of two bit strings encoded in the same set of
	 * base characters. The two argument strings have to be of the same length
	 * and contain only characters contained in the base character set.
	 * Furthermore, the size of the base character set has to be a power of 2
	 * for the result to be meaningful.
	 * @param str1 the first encoded bit string
	 * @param str2 the second encoded bit string
	 * @param baseChars the character set the two other arguments are encoded in
	 * @return the AND of the two argument bit strings
	 */
	public static String getAndBasePow2(String str1, String str2, String baseChars) {
		StringBuffer and = new StringBuffer();
		for (int c = 0; c < Math.max(str1.length(), str2.length()); c++) {
			int i1 = ((c < str1.length()) ? baseChars.indexOf(str1.charAt(c)) : 0);
			int i2 = ((c < str2.length()) ? baseChars.indexOf(str2.charAt(c)) : 0);
			and.append(baseChars.charAt(i1 & i2));
		}
		return and.toString();
	}
	
	/**
	 * Compute the bit-wise OR of two hexadecimal strings. The two argument
	 * strings have to be of the same length and contain valid HEX characters
	 * only.
	 * @param hex1 the first hex string
	 * @param hex2 the second hex string
	 * @return the OR of the two argument hex strings
	 */
	public static String getHexOr(String hex1, String hex2) {
		hex1 = hex1.toUpperCase();
		hex2 = hex2.toUpperCase();
		return getOrBasePow2(hex1, hex2, hexDigits);
	}
	
	/**
	 * Compute the bit-wise OR of two bit strings encoded in the same set of
	 * base characters. The two argument strings have to be of the same length
	 * and contain only characters contained in the base character set.
	 * Furthermore, the size of the base character set has to be a power of 2
	 * for the result to be meaningful.
	 * @param str1 the first encoded bit string
	 * @param str2 the second encoded bit string
	 * @param baseChars the character set the two other arguments are encoded in
	 * @return the OR of the two argument bit strings
	 */
	public static String getOrBasePow2(String str1, String str2, String baseChars) {
		StringBuffer or = new StringBuffer();
		for (int c = 0; c < Math.max(str1.length(), str2.length()); c++) {
			int i1 = ((c < str1.length()) ? baseChars.indexOf(str1.charAt(c)) : 0);
			int i2 = ((c < str2.length()) ? baseChars.indexOf(str2.charAt(c)) : 0);
			or.append(baseChars.charAt(i1 | i2));
		}
		return or.toString();
	}
	
	/**
	 * Compute the bit-wise eXclusive OR of two hexadecimal strings. The two
	 * argument strings have to be of the same length and contain valid HEX
	 * characters only.
	 * @param hex1 the first hex string
	 * @param hex2 the second hex string
	 * @return the XOR of the two argument hex strings
	 */
	public static String getHexXor(String hex1, String hex2) {
		hex1 = hex1.toUpperCase();
		hex2 = hex2.toUpperCase();
		return getXorBasePow2(hex1, hex2, hexDigits);
	}
	
	/**
	 * Compute the bit-wise eXclusive OR of two bit strings encoded in the same
	 * set of base characters. The two argument strings have to be of the same
	 * length and contain only characters contained in the base character set.
	 * Furthermore, the size of the base character set has to be a power of 2
	 * for the result to be meaningful.
	 * @param str1 the first encoded bit string
	 * @param str2 the second encoded bit string
	 * @param baseChars the character set the two other arguments are encoded in
	 * @return the XOR of the two argument strings
	 */
	public static String getXorBasePow2(String str1, String str2, String baseChars) {
		StringBuffer xor = new StringBuffer();
		for (int c = 0; c < Math.max(str1.length(), str2.length()); c++) {
			int i1 = ((c < str1.length()) ? baseChars.indexOf(str1.charAt(c)) : 0);
			int i2 = ((c < str2.length()) ? baseChars.indexOf(str2.charAt(c)) : 0);
			xor.append(baseChars.charAt(i1 ^ i2));
		}
		return xor.toString();
	}
	
	/**
	 * Produce a hexadecimal random string, using characters 0-9 and A-F. The
	 * argument number of bits is rounded to the next higher multiple of 4. If
	 * the argument number of bits is less than 1, this method returns is the
	 * empty string.
	 * @param bits the number of random bits to produce
	 * @return the random string
	 */
	public static String getRandomHex(int bits) {
		return getRandomStringBasePow2(bits, hexDigits);
	}
	
	/**
	 * Produce a random string from a custom set of characters. The number of
	 * random bits per character is the two logarithm of the number of supplied
	 * characters. Client code has to take care to not supply a character set
	 * with duplicates in it, as then the actual amount of randomness per
	 * character may be far lower. Also, the character set must consist of a
	 * minimum of 2 characters. If the argument number of bits is less than 1,
	 * this method returns is the empty string.
	 * @param bits the number of random bits to produce
	 * @param baseChars the characters to encode the random bits in
	 * @return the random string
	 */
	public static String getRandomString(int bits, String baseChars) {
		
		//	we have a bower of two, can work with bit shifts
		if (isPowerOfTwo(baseChars.length()))
			return getRandomStringBasePow2(bits, baseChars);
		
		//	work with integer arithmetics otherwise
		else return getRandomStringVarBase(bits, baseChars);
	}
	
	/**
	 * Produce a random string from a custom set of characters whose size is
	 * _not_ a power of 2; if the size of the character set actually _is_ a
	 * power of 2, client code should prefer the faster (bit operating)
	 * <code>getRandomStringBasePow2()</code>. The number of random bits per
	 * character is the two logarithm of the number of supplied characters.
	 * Client code has to take care to not supply a character set with
	 * duplicates in it, as then the actual amount of randomness per character
	 * may be far lower. Also, the character set must consist of a minimum of 2
	 * characters. If the argument number of bits is less than 1, this method
	 * returns is the empty string.
	 * @param bits the number of random bits to produce
	 * @param baseChars the characters to encode the random bits in
	 * @return the random string
	 */
	public static final String getRandomStringVarBase(int bits, String baseChars) {
		if (baseChars.length() < 2)
			throw new IllegalArgumentException("At least 2 characters required for random strings");
		if (bits < 1)
			return "";
		
		//	compute bits per character and float required bits
		float fBitsPerChar = ((float) (Math.log(baseChars.length()) / Math.log(2)));
		
		//	compute random generator limit
		int generatorLimit = baseChars.length();
		int charsPerGeneration = 1;
		while (((generatorLimit * baseChars.length()) < Integer.MAX_VALUE) && ((generatorLimit * baseChars.length()) > generatorLimit)) {
			generatorLimit *= baseChars.length();
			charsPerGeneration++;
		}
		
		//	generate random string
		StringBuffer randomString = new StringBuffer((int) Math.ceil(((double) bits) / fBitsPerChar));
		int buffer = 0;
		int bufferLevel = 0;
		for (float fBits = bits; fBits > 0; fBits -= fBitsPerChar) {
			if (bufferLevel == 0) {
				buffer = random.nextInt(generatorLimit);
				bufferLevel = charsPerGeneration;
			}
			randomString.append(baseChars.charAt(buffer % baseChars.length()));
			buffer /= baseChars.length();
			bufferLevel--;
		}
		return randomString.toString();
	}
	
	/**
	 * Produce a random string from a custom set of characters whose size is
	 * a power of 2; if the size of the character set is _not_ a power of 2,
	 * this method throws an <code>IllegalArgumentException</code>. Client
	 * code with such character sets must use the more flexible
	 * <code>getRandomStringVarBase()</code>. The number of random bits per
	 * character is the two logarithm of the number of supplied characters.
	 * Client code has to take care to not supply a character set with
	 * duplicates in it, as then the actual amount of randomness per character
	 * may be far lower. Also, the character set must consist of a minimum of 2
	 * characters. If the argument number of bits is less than 1, this method
	 * returns is the empty string.
	 * @param bits the number of random bits to produce
	 * @param baseChars the characters to encode the random bits in
	 * @return the random string
	 */
	public static final String getRandomStringBasePow2(int bits, String baseChars) {
		if (baseChars.length() < 2)
			throw new IllegalArgumentException("At least 2 characters required for random strings");
		if (bits < 1)
			return "";
		
		//	make sure we actually have a power-of-2 sized characters set
		int nextPow2 = getNextPowerOfTwo(baseChars.length());
		if (nextPow2 > baseChars.length()) {
			if (padBasePow2ForTest) // pad base chars to next power of 2 for testing
				baseChars = (baseChars + baseChars.substring(0, (nextPow2 - baseChars.length())));
			else throw new IllegalArgumentException("Power of 2 characters required for this method, use getRandomStringVarBase() with " + baseChars.length() + " characters");
		}
		
		//	compute character bitmask
		int bitsPerChar = Math.round((float) (Math.log(baseChars.length()) / Math.log(2)));
		int charBitMask = (baseChars.length() - 1);
		
		//	compute random generator limit
		int generatorLimit = baseChars.length();
		int charsPerGeneration = 1;
		while (((charsPerGeneration + 1) * bitsPerChar) < 32) {
			generatorLimit <<= bitsPerChar;
			charsPerGeneration++;
		}
		
		//	generate random string
		StringBuffer randomString = new StringBuffer((int) Math.ceil(((double) bits) / bitsPerChar));
		int buffer = 0;
		int bufferLevel = 0;
		for (int rBits = bits; rBits > 0; rBits -= bitsPerChar) {
			if (bufferLevel == 0) {
				buffer = random.nextInt(generatorLimit);
				bufferLevel = charsPerGeneration;
			}
			randomString.append(baseChars.charAt(buffer & charBitMask));
			buffer >>= bitsPerChar;
			bufferLevel--;
		}
		return randomString.toString();
	}
	
	private static boolean isPowerOfTwo(int i) {
		if (i <= 0)
			return false;
		while ((i & 0x01) == 0)
			i >>= 1; // right shift until last bit is 1
		return (i == 1); // if the highest 1 bit made it all the way, there were only 0 bits following it, so we have a power of 2
	}
	
	private static int getNextPowerOfTwo(int i) {
		if (i <= 0)
			return 0;
		int exp = 0;
		int iExp = 0;
		while (i > 1) {
			exp++;
			if ((i & 0x01) != 0)
				iExp = 1; // one extra left shift in result for intermediate 1 bits
			i >>= 1;
		}
		return (1 << (exp + iExp)); // result is >= i with added shift for intermediate 1 bits
	}
	
	private static boolean padBasePow2ForTest = false;
	public static void main(String[] args) throws Exception {
//		System.out.println(getRandomHex(128)); // make sure we're initialized
//		int rounds = 0x0003FFFF;
//		long start;
//		padBasePow2ForTest = true;
//		
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
//		System.out.println("Base 52 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringBasePow2(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
//		System.out.println("Base 52 BP2 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringVarBase(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
//		System.out.println("Base 52 VB x " + rounds + ": " + (System.currentTimeMillis() - start));
//		
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
//		System.out.println("Base 62 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringBasePow2(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
//		System.out.println("Base 62 BP2 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringVarBase(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
//		System.out.println("Base 62 VB x " + rounds + ": " + (System.currentTimeMillis() - start));
//		
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
//		System.out.println("Base 64 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringBasePow2(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
//		System.out.println("Base 64 BP2 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringVarBase(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
//		System.out.println("Base 64 VB x " + rounds + ": " + (System.currentTimeMillis() - start));
//		
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomString(256, hexDigits);
//		System.out.println("Base 16 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringBasePow2(256, hexDigits);
//		System.out.println("Base 16 BP2 x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++)
//			getRandomStringVarBase(256, hexDigits);
//		System.out.println("Base 16 VB x " + rounds + ": " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		for (int r = 0; r < rounds; r++) {
//			getGUID();
//			getGUID();
//		}
//		System.out.println("GUID x " + rounds + " x 2: " + (System.currentTimeMillis() - start));
		
		System.out.println(getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
		System.out.println(getRandomHex(256));
		System.out.println(getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
		System.out.println(getRandomHex(128));
		System.out.println(getRandomHex(192));
		System.out.println(getRandomHex(64));
		
		String fhex = getRandomHex(64);
		System.out.println(" First: " + fhex);
		System.out.println("   NOT: " + getHexNot(fhex));
		String shex = getRandomHex(64);
		System.out.println("Second: " + shex);
		System.out.println("   NOT: " + getHexNot(shex));
		System.out.println("    OR: " + getHexOr(fhex, shex));
		System.out.println("   AND: " + getHexAnd(fhex, shex));
		System.out.println("   XOR: " + getHexXor(fhex, shex));
		
		String fb64 = getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
		System.out.println(" First: " + fb64);
		System.out.println("   NOT: " + getNotBasePow2(fb64, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
		String sb64 = getRandomString(256, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
		System.out.println("Second: " + sb64);
		System.out.println("   NOT: " + getNotBasePow2(sb64, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
		System.out.println("    OR: " + getOrBasePow2(fb64, sb64, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
		System.out.println("   AND: " + getAndBasePow2(fb64, sb64, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
		System.out.println("   XOR: " + getXorBasePow2(fb64, sb64, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
	}
}
