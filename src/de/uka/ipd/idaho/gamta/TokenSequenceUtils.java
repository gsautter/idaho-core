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
package de.uka.ipd.idaho.gamta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Static class providing comparison and other functionality for token
 * sequences.
 * 
 * @author sautter
 */
public class TokenSequenceUtils {

	/**
	 * get a Token value's index in a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @return the index of the first Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int indexOf(TokenSequence ts, CharSequence token) {
		return indexOf(ts, token, 0, true);
	}

	/**
	 * get a Token value's index in a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the first Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int indexOf(TokenSequence ts, CharSequence token, boolean caseSensitive) {
		return indexOf(ts, token, 0, caseSensitive);
	}

	/**
	 * get a Token value's index in a TokenSequence, starting from a given
	 * position
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @param from the index to start at
	 * @return the index of the first Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int indexOf(TokenSequence ts, CharSequence token, int from) {
		return indexOf(ts, token, from, true);
	}

	/**
	 * get a Token value's index in a TokenSequence, starting from a given
	 * position
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @param from the index to start at
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the first Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int indexOf(TokenSequence ts, CharSequence token, int from, boolean caseSensitive) {
		for (int t = from; t < ts.size(); t++) {
			if (CharSequenceUtils.equals(ts.tokenAt(t), token))
				return t;
			else if (!caseSensitive && CharSequenceUtils.equalsIgnoreCase(ts.tokenAt(t), token))
				return t;
		}
		return -1;
	}

	/**
	 * get a Token value's index in a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @return the index of the last Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int lastIndexOf(TokenSequence ts, CharSequence token) {
		return lastIndexOf(ts, token, ts.size(), true);
	}

	/**
	 * get a Token value's index in a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the last Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int lastIndexOf(TokenSequence ts, CharSequence token, boolean caseSensitive) {
		return lastIndexOf(ts, token, ts.size(), caseSensitive);
	}

	/**
	 * get a Token value's index in a TokenSequence, starting from a given
	 * position
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @param to the index to start at
	 * @return the index of the last Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int lastIndexOf(TokenSequence ts, CharSequence token, int to) {
		return lastIndexOf(ts, token, to, true);
	}

	/**
	 * get a Token value's index in a TokenSequence, starting from a given
	 * position
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @param to the index to start at
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the last Token t in ts such that t.equals(token) is
	 *         true, or -1 if there is no such Token
	 */
	public static int lastIndexOf(TokenSequence ts, CharSequence token, int to, boolean caseSensitive) {
		for (int t = Math.min(to, (ts.size() - 1)); t > -1; t--)
			if (CharSequenceUtils.equals(ts.tokenAt(t), token))
				return t;
			else if (!caseSensitive && CharSequenceUtils.equalsIgnoreCase(ts.tokenAt(t), token))
				return t;
		return -1;
	}

	/**
	 * Test if a token sequence starts with a specific sub sequence.
	 * @param ts the TokenSequence to test
	 * @param tokens the TokenSequence to search for
	 * @return true if ts starts with tokens, false otherwise
	 */
	public static boolean startsWith(TokenSequence ts, TokenSequence tokens) {
		return startsWith(ts, tokens, 0, true);
	}
	
	/**
	 * Test if a token sequence starts with a specific sub sequence.
	 * @param ts the TokenSequence to test
	 * @param tokens the TokenSequence to search for
	 * @param caseSensitive do case sensitive comparison?
	 * @return true if ts starts with tokens, false otherwise
	 */
	public static boolean startsWith(TokenSequence ts, TokenSequence tokens, boolean caseSensitive) {
		return startsWith(ts, tokens, 0, caseSensitive);
	}
	
	/**
	 * Test if a token sequence contains a specific sub sequence at a specific
	 * index.
	 * @param ts the TokenSequence to test
	 * @param tokens the TokenSequence to search for
	 * @param from the index to test from
	 * @return true if ts starts with tokens, false otherwise
	 */
	public static boolean startsWith(TokenSequence ts, TokenSequence tokens, int from) {
		return startsWith(ts, tokens, from, true);
	}
	
	/**
	 * Test if a token sequence contains a specific sub sequence at a specific
	 * index.
	 * @param ts the TokenSequence to test
	 * @param tokens the TokenSequence to search for
	 * @param from the index to test from
	 * @param caseSensitive do case sensitive comparison?
	 * @return true if ts starts with tokens, false otherwise
	 */
	public static boolean startsWith(TokenSequence ts, TokenSequence tokens, int from, boolean caseSensitive) {
		for (int t = 0; t < tokens.size(); t++) {
			if ((from + t) >= ts.size())
				return false;
			if (!(caseSensitive ? ts.valueAt(from + t).equals(tokens.valueAt(t)) : ts.valueAt(from + t).equalsIgnoreCase(tokens.valueAt(t))))
				return false;
		}
		return true;
	}
	
	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @return the index of the first Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int indexOf(TokenSequence ts, TokenSequence tokens) {
		return indexOf(ts, tokens, 0, true);
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the first Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int indexOf(TokenSequence ts, TokenSequence tokens, boolean caseSensitive) {
		return indexOf(ts, tokens, 0, caseSensitive);
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @param from the index to start at
	 * @return the index of the first Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int indexOf(TokenSequence ts, TokenSequence tokens, int from) {
		return indexOf(ts, tokens, from, true);
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @param from the index to start at
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the first Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int indexOf(TokenSequence ts, TokenSequence tokens, int from, boolean caseSensitive) {
		if (tokens.size() == 0)
			return -1;
		Token anchor = tokens.firstToken();
		int s = indexOf(ts, anchor, from, caseSensitive);
		while (s != -1) {
			int t = 1;
			while (t != 0) {
				if (t == tokens.size())
					return s;
				else if ((s + t) == ts.size())
					return -1;
				else if (
					CharSequenceUtils.equals(ts.tokenAt(s + t), tokens.tokenAt(t))
					||
					(
						!caseSensitive 
						&&
						CharSequenceUtils.equalsIgnoreCase(ts.tokenAt(s + t), tokens.tokenAt(t)))
					)
					t++;
				else t = 0;
			}
			s = indexOf(ts, anchor, (s + 1));
		}
		return -1;
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @return the index of the last Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int lastIndexOf(TokenSequence ts, TokenSequence tokens) {
		return lastIndexOf(ts, tokens, ts.size(), true);
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the last Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int lastIndexOf(TokenSequence ts, TokenSequence tokens, boolean caseSensitive) {
		return lastIndexOf(ts, tokens, ts.size(), caseSensitive);
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @param to the index to start at
	 * @return the index of the last Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int lastIndexOf(TokenSequence ts, TokenSequence tokens, int to) {
		return lastIndexOf(ts, tokens, to, true);
	}

	/**
	 * get the start index of a subsequence of a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @param to the index to start at
	 * @param caseSensitive do case sensitive comparison?
	 * @return the index of the last Token of the TokenSequence in ts, or -1 if
	 *         there is no such TokenSequence
	 */
	public static int lastIndexOf(TokenSequence ts, TokenSequence tokens, int to, boolean caseSensitive) {
		if (tokens.size() == 1)
			return -1;
		Token anchor = tokens.firstToken();
		int s = lastIndexOf(ts, anchor, to, caseSensitive);
		while (s != -1) {
			int t = 1;
			while (t != 0) {
				if (t == tokens.size())
					return s;
				else if ((s + t) == ts.size())
					t = 0;
				else if (
					CharSequenceUtils.equals(ts.tokenAt(s + t), tokens.tokenAt(t))
					||
					(
						!caseSensitive 
						&&
						CharSequenceUtils.equalsIgnoreCase(ts.tokenAt(s + t), tokens.tokenAt(t)))
					)
					t++;
				else t = 0;
			}
			s = lastIndexOf(ts, anchor, (s - 1));
		}
		return -1;
	}

	/**
	 * check if a Token is contained in a TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @return true if and only if ts contains a Token t such that
	 *         t.equals(token) is true
	 */
	public static boolean contains(TokenSequence ts, Token token) {
		return (indexOf(ts, token) != -1);
	}

	/**
	 * check if a Token is contained in a TokenSequence, using case insensitive
	 * comparison
	 * @param ts the TokenSequence to search through
	 * @param token the Token to search for
	 * @return true if and only if ts contains a Token t such that
	 *         t.equals(token) is true
	 */
	public static boolean containsIgnoreCase(TokenSequence ts, Token token) {
		return (indexOf(ts, token, false) != -1);
	}

	/**
	 * check if a TokenSequence is contained in (i.e. is a subsequence of) a
	 * TokenSequence
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @return true if and only if ts contains a Token t such that
	 *         t.equals(token) is true
	 */
	public static boolean contains(TokenSequence ts, TokenSequence tokens) {
		return (indexOf(ts, tokens) != -1);
	}

	/**
	 * check if a TokenSequence is contained in (i.e. is a subsequence of) a
	 * TokenSequence, using case insensitive comparison
	 * @param ts the TokenSequence to search through
	 * @param tokens the TokenSequence to search for
	 * @return true if and only if ts contains a Token t such that
	 *         t.equals(token) is true
	 */
	public static boolean containsIgnoreCase(TokenSequence ts, TokenSequence tokens) {
		return (indexOf(ts, tokens, false) != -1);
	}
	
	/**
	 * Get the values of a sequence of Tokens, concatenated to a String Note:
	 * this method does not include the leading and tailing whitespaces of the
	 * specified token sequence.
	 * @param tokens the TokenSequence to concatenate
	 * @return the values of all Tokens in a TokenSequence, concatenated to a
	 *         String
	 */
	public static String concatTokens(TokenSequence tokens) {
		return concatTokens(tokens, 0, tokens.size(), false, false, -1);
	}
	
	/**
	 * Get the values of a sequence of Tokens, concatenated to a String Note:
	 * this method does not include the leading and tailing whitespaces of the
	 * specified token sequence. The returned string is restricted to the
	 * argument maximum length, with tokens omitted in the middle if the
	 * argument token sequence as a whole exceeds the limit.
	 * @param tokens the TokenSequence to concatenate
	 * @param maxLength the maximum number of characters
	 * @return the values of all Tokens in a TokenSequence, concatenated to a
	 *         String
	 */
	public static String concatTokens(TokenSequence tokens, int maxLength) {
		return concatTokens(tokens, 0, tokens.size(), false, false, maxLength);
	}
	
	/**
	 * Get the values of a sequence of Tokens, concatenated to a String Note:
	 * this method does not include the leading and tailing whitespaces of the
	 * specified token sequence.
	 * @param tokens the TokenSequence to concatenate
	 * @param normalizeWhitespace completely normalize whitespace between
	 *            tokens? (see five argument version of this method for
	 *            explanation)
	 * @param ignoreLineBreaks suppress including line breaks in result string?
	 *            (see five argument version of this method for explanation)
	 * @return the values of a sequence of Tokens, concatenated to a String
	 */
	public static String concatTokens(TokenSequence tokens, boolean normalizeWhitespace, boolean ignoreLineBreaks) {
		return concatTokens(tokens, 0, tokens.size(), normalizeWhitespace, ignoreLineBreaks, -1);
	}
	
	/**
	 * Get the values of a sequence of Tokens, concatenated to a String Note:
	 * this method does not include the leading and tailing whitespaces of the
	 * specified token sequence. The returned string is restricted to the
	 * argument maximum length, with tokens omitted in the middle if the
	 * argument token sequence as a whole exceeds the limit.
	 * @param tokens the TokenSequence to concatenate
	 * @param normalizeWhitespace completely normalize whitespace between
	 *            tokens? (see five argument version of this method for
	 *            explanation)
	 * @param ignoreLineBreaks suppress including line breaks in result string?
	 *            (see five argument version of this method for explanation)
	 * @param maxLength the maximum number of characters
	 * @return the values of a sequence of Tokens, concatenated to a String
	 */
	public static String concatTokens(TokenSequence tokens, boolean normalizeWhitespace, boolean ignoreLineBreaks, int maxLength) {
		return concatTokens(tokens, 0, tokens.size(), normalizeWhitespace, ignoreLineBreaks, maxLength);
	}
	
	/**
	 * get the values of a sequence of Tokens, concatenated to a String Note:
	 * this method does not include the leading and tailing whitespaces of the
	 * specified token sequence
	 * @param tokens the TokenSequence to concatenate
	 * @param start the index of the Token to start at
	 * @param size the number of Tokens to concatenate
	 * @return the values of a sequence of Tokens, concatenated to a String
	 */
	public static String concatTokens(TokenSequence tokens, int start, int size) {
		return concatTokens(tokens, start, size, false, false);
	}
	
	/**
	 * Get the values of a sequence of Tokens, concatenated to a String Note:
	 * this method does not include the leading and tailing whitespaces of the
	 * specified token sequence.  The returned string is restricted to the
	 * argument maximum length, with tokens omitted in the middle if the
	 * argument token sequence as a whole exceeds the limit.
	 * @param tokens the TokenSequence to concatenate
	 * @param start the index of the Token to start at
	 * @param size the number of Tokens to concatenate
	 * @param maxLength the maximum number of characters
	 * @return the values of a sequence of Tokens, concatenated to a String
	 */
	public static String concatTokens(TokenSequence tokens, int start, int size, int maxLength) {
		return concatTokens(tokens, start, size, false, false, maxLength);
	}
	
	/**
	 * Concatenate the individual token values of a token sequence to a String.
	 * Depending on the parameters, the whitespace between the token values can
	 * be normalized to different levels:<br>
	 * If <code>normalizeWhitespace</code> is false, a single whitespace
	 * character is inserted in the result String if there is one or more
	 * whitespace characters in the specified token sequence, no whitespace if
	 * there is none. If <code>normalizeWhitespace</code> is true, in turn,
	 * whitespace is inserted between two tokens t and u if
	 * <code>Gamta.insertSpace(t, u)</code> returns true, or if it is a line
	 * break.<br>
	 * If <code>ignoreLineBreaks</code> is true, every whitespace character
	 * inserted in the result string is the plain space character. If
	 * <code>ignoreLineBreaks</code> is false, in turn, the newline character
	 * is inserted after tokens having the
	 * <code>Token.PARAGRAPH_END_ATTRIBUTE</code> attribute set. <br>
	 * Note: this method does not include the leading and tailing whitespaces of
	 * the specified token sequence
	 * @param tokens the TokenSequence to concatenate
	 * @param start the index of the Token to start at
	 * @param size the number of Tokens to concatenate
	 * @param normalizeWhitespace completely normalize whitespace between
	 *            tokens? (default is false, see above for explanation)
	 * @param ignoreLineBreaks suppress including line breaks in result string?
	 *            (default is false, see above for explanation)
	 * @return the values of a sequence of Tokens, concatenated to a String
	 */
	public static String concatTokens(TokenSequence tokens, int start, int size, boolean normalizeWhitespace, boolean ignoreLineBreaks) {
		return concatTokens(tokens, start, size, normalizeWhitespace, ignoreLineBreaks, -1);
	}
	
	/**
	 * Concatenate the individual token values of a token sequence to a String.
	 * Depending on the parameters, the whitespace between the token values can
	 * be normalized to different levels:<br>
	 * If <code>normalizeWhitespace</code> is false, a single whitespace
	 * character is inserted in the result String if there is one or more
	 * whitespace characters in the specified token sequence, no whitespace if
	 * there is none. If <code>normalizeWhitespace</code> is true, in turn,
	 * whitespace is inserted between two tokens t and u if
	 * <code>Gamta.insertSpace(t, u)</code> returns true, or if it is a line
	 * break.<br>
	 * If <code>ignoreLineBreaks</code> is true, every whitespace character
	 * inserted in the result string is the plain space character. If
	 * <code>ignoreLineBreaks</code> is false, in turn, the newline character
	 * is inserted after tokens having the
	 * <code>Token.PARAGRAPH_END_ATTRIBUTE</code> attribute set. <br>
	 * Note: this method does not include the leading and tailing whitespaces of
	 * the specified token sequence
	 * @param tokens the TokenSequence to concatenate
	 * @param start the index of the Token to start at
	 * @param size the number of Tokens to concatenate
	 * @param normalizeWhitespace completely normalize whitespace between
	 *            tokens? (default is false, see above for explanation)
	 * @param ignoreLineBreaks suppress including line breaks in result string?
	 *            (default is false, see above for explanation)
	 * @param maxLength the maximum number of characters
	 * @return the values of a sequence of Tokens, concatenated to a String
	 */
	public static String concatTokens(TokenSequence tokens, int start, int size, boolean normalizeWhitespace, boolean ignoreLineBreaks, int maxLength) {
		if ((size == 0) || (tokens.size() == 0))
			return "";
		
		//	do we have to do all he hustle and dance?
		if (maxLength < 0)
			return doGetString(tokens, start, size, normalizeWhitespace, ignoreLineBreaks);
		
		//	this one's short enough
		if ((tokens.tokenAt(start + size - 1).getEndOffset() - tokens.tokenAt(start).getStartOffset()) <= maxLength)
			return doGetString(tokens, start, size, normalizeWhitespace, ignoreLineBreaks);
		
		//	get end of head
		int headEnd = start;
		int headChars = 0;
		for (; headEnd < (start + size); headEnd++) {
			headChars += tokens.tokenAt(headEnd).length();
			if ((maxLength / 2) <= headChars)
				break;
			headChars += tokens.getWhitespaceAfter(headEnd).length();
		}
		
		//	get start of tail
		int tailStart = (start + size - 1);
		int tailChars = 0;
		for (; start < tailStart; tailStart--) {
			tailChars += tokens.tokenAt(tailStart).length();
			if ((maxLength / 2) <= tailChars)
				break;
			if (tailStart == headEnd)
				break;
			tailChars += tokens.getWhitespaceAfter(tailStart - 1).length();
		}
		
		//	met in the middle, use whole string
		if ((headEnd == tailStart) || ((headEnd + 1) == tailStart))
			return doGetString(tokens, start, size, normalizeWhitespace, ignoreLineBreaks);
		
		//	give head and tail only if annotation too long
		else {
			StringBuffer string = new StringBuffer();
			appendString(tokens, start, (headEnd - start), normalizeWhitespace, ignoreLineBreaks, string);
			string.append(" ... ");
			appendString(tokens, tailStart, (start + size - tailStart), normalizeWhitespace, ignoreLineBreaks, string);
			return string.toString();
		}
//		String lastValue = tokens.valueAt(start);
//		boolean insertLineBreak = false;
//		StringBuffer result = new StringBuffer(lastValue);
//		
//		for (int t = (start + 1); t < (start + size); t++) {
//			Token token = tokens.tokenAt(t);
//			String value = token.getValue();
//
//			if (insertLineBreak)
//				result.append("\r\n");
//			else if (normalizeWhitespace ? Gamta.insertSpace(lastValue, value) : (tokens.getWhitespaceAfter(t - 1).length() != 0))
//				result.append(" ");
//
//			result.append(value);
//
//			lastValue = value;
//			insertLineBreak = (!ignoreLineBreaks && token.hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE));
//		}
//
//		return result.toString();
	}
	private static String doGetString(TokenSequence tokens, int start, int size, boolean normalizeWhitespace, boolean ignoreLineBreaks) {
		StringBuffer string = new StringBuffer();
		appendString(tokens, start, size, normalizeWhitespace, ignoreLineBreaks, string);
		return string.toString();
	}
	private static void appendString(TokenSequence tokens, int start, int size, boolean normalizeWhitespace, boolean ignoreLineBreaks, StringBuffer string) {
		String lastValue = tokens.valueAt(start);
		string.append(lastValue);
		boolean insertLineBreak = false;
		
		for (int t = (start + 1); t < (start + size); t++) {
			Token token = tokens.tokenAt(t);
			String value = token.getValue();
			
			if (insertLineBreak)
				string.append("\r\n");
			else if (normalizeWhitespace ? Gamta.insertSpace(lastValue, value) : (tokens.getWhitespaceAfter(t - 1).length() != 0))
				string.append(" ");
			
			string.append(value);
			
			lastValue = value;
			insertLineBreak = (!ignoreLineBreaks && token.hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE));
		}
	}

	/**
	 * extract all tokens from a token sequence that are words or numbers
	 * @param tokens the token sequence to process
	 * @return all token values from the specified token sequence that are words
	 *         or numbers, packed in a StringVector in their original order
	 */
	public static StringVector getTextTokens(TokenSequence tokens) {
		StringVector textTokens = new StringVector();
		Token token;
		for (int t = 0; t < tokens.size(); t++) {
			token = tokens.tokenAt(t);
			if (Gamta.isWord(token) || Gamta.isNumber(token))
				textTokens.addElement(token.getValue());
		}
		return textTokens;
	}

	/**
	 * find the index of the token at some character offset
	 * @param tokens the token sequence to process
	 * @param offset the offset to find the token for
	 * @return the index of the token at the specified offset, or -1, if there
	 *         is no such token
	 */
	public static int getTokenIndexAtOffset(TokenSequence tokens, int offset) {

		// check parameter
		if (tokens.size() == 0)
			return -1;

		// use binary search to narrow search interval
		int left = 0;
		int right = tokens.size();
		int tIndex = 0;
		while ((right - left) > 2) {
			tIndex = ((left + right) / 2);
			if (tokens.tokenAt(tIndex).getEndOffset() <= offset)
				left = tIndex;
			else if (tokens.tokenAt(tIndex).getStartOffset() <= offset)
				return tIndex;
			else right = tIndex;
		}

		// scan remaining interval
		tIndex = left;
		while (tIndex < tokens.size()) {
			if (tokens.tokenAt(tIndex).getEndOffset() <= offset)
				tIndex++;
			else if (tokens.tokenAt(tIndex).getStartOffset() <= offset)
				return tIndex;
			else tIndex++;
		}
		return -1;
	}
	
	/**
	 * A token normalizer modifies tokens to facilitate a match, e.g. by
	 * converting them to lower case or by stripping accents.
	 * 
	 * @author sautter
	 */
	public static interface TokenNormalizer {
		
		/**
		 * Normalize a token for match-up. If this method returns null, the
		 * argument token will be ignored altogether.
		 * @param token the token to normalize
		 * @param forFuzzyMatch is the normalization for a fuzzy match?
		 * @return the normalized token
		 */
		public abstract String normalize(String token);
	}
	
	/** default token normalizer, returning tokens unchanged */
	public static final TokenNormalizer DEFAULT_TOKEN_NORMALIZER = new TokenNormalizer() {
		public String normalize(String token) {
			return token;
		}
	};
	
	/**
	 * Compare two token sequences. The argument token normalizer can modify
	 * tokens before they are compared via <code>equals()</code> to remove
	 * certain aspects of a comparison, e.g. case.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @return true if the two token sequences are equals
	 */
	public static boolean sequenceEquals(TokenSequence tokens, TokenSequence reference) {
		return sequenceEquals(tokens, reference, DEFAULT_TOKEN_NORMALIZER);
	}
	
	/**
	 * Compare two token sequences. The argument token normalizer can modify
	 * tokens before they are compared via <code>equals()</code> to remove
	 * certain aspects of a comparison, e.g. case.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param normalizer the token normalizer to use
	 * @return true if the two token sequences are equals
	 */
	public static boolean sequenceEquals(TokenSequence tokens, TokenSequence reference, TokenNormalizer normalizer) {
		if (tokens.size() != reference.size())
			return false;
		for (int t = 0; t < tokens.size(); t++) {
			String token = normalizer.normalize(tokens.valueAt(t));
			String rToken = normalizer.normalize(reference.valueAt(t));
			if ((token == null) && (rToken == null))
				continue;
			if ((token == null) || (rToken == null))
				return false;
			if (!token.equals(rToken))
				return false;
		}
		return true;
	}
	
	/**
	 * Compare two token sequences in an order insensitive fashion. The
	 * argument token normalizer can modify tokens before they are compared
	 * via <code>equals()</code> to remove certain aspects of a comparison,
	 * e.g. case.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param normalizer the token normalizer to use
	 * @return true if the two token sequences are equals
	 */
	public static boolean bagEquals(TokenSequence tokens, TokenSequence reference, TokenNormalizer normalizer) {
		CountingSet bag = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER));
		for (int t = 0; t < reference.size(); t++) {
			String value = normalizer.normalize(reference.valueAt(t));
			if (value != null)
				bag.add(value);
		}
		for (int t = 0; t < tokens.size(); t++) {
			String value = normalizer.normalize(tokens.valueAt(t));
			if (value == null)
				continue;
			if (bag.contains(value))
				bag.remove(value);
			else return false;
		}
		return bag.isEmpty(); // everything matched up
	}
	
	/**
	 * A token mismatch between two token sequences.
	 * 
	 * @author sautter
	 */
	public static class TokenMismatch {
		
		/** the mismatching token (null for deletions) */
		public final String token;
		
		/** the position of the mismatching token (-1 for deletions, and in bag matches) */
		public final int tokenPos;
		
		/** the mismatching reference token (null for insertions) */
		public final String refToken;
		
		/** the position of the mismatching reference token (-1 for insertions, and in bag matches) */
		public final int refTokenPos;
		
		TokenMismatch(String token, int tokenPos, String refToken, int refTokenPos) {
			this.token = token;
			this.tokenPos = tokenPos;
			this.refToken = refToken;
			this.refTokenPos = refTokenPos;
		}
	}
	
	/**
	 * An overall score for the token mismatches between two token sequences.
	 * 
	 * @author sautter
	 */
	public static class TokenMismatchScore {
		final int matchChars;
		final int missChars;
		final int refMissChars;
		
		/**
		 * @param matchChars the number of matched characters
		 * @param missChars the number of unmatched characters in the token sequence
		 * @param refMissChars the number of unmatched characters in the reference token sequence
		 */
		public TokenMismatchScore(int matchChars, int missChars, int refMissChars) {
			this.matchChars = matchChars;
			this.missChars = missChars;
			this.refMissChars = refMissChars;
		}
	}
	
	/**
	 * An scorer for the overall token mismatches between two token sequences.
	 * 
	 * @author sautter
	 */
	public static interface TokenMismatchScorer {
		
		/**
		 * Score the mismatches between two token sequences.
		 * @param mismatches the mismatches
		 * @param tokens the token sequence
		 * @param reference the reference token sequence
		 * @param isBagMatch is it a bag match (or a sequence match)?
		 * @return the score
		 */
		public abstract TokenMismatchScore getScore(TokenMismatch[] mismatches, TokenSequence tokens, TokenSequence reference, boolean isBagMatch);
	}
	
	/** default sequence mismatch scorer, counting all characters as missed */
	public static final TokenMismatchScorer DEFAULT_SEQUENCE_MISMATCH_SCORER = new TokenMismatchScorer() {
		public TokenMismatchScore getScore(TokenMismatch[] mismatches, TokenSequence tokens, TokenSequence reference, boolean isBagMatch) {
			int tMissChars = 0;
			int rMissChars = 0;
			for (int m = 0; m < mismatches.length; m++) {
				if (mismatches[m].token != null)
					tMissChars += mismatches[m].token.length();
				if (mismatches[m].refToken != null)
					rMissChars += mismatches[m].refToken.length();
			}
			return new TokenMismatchScore(0, tMissChars, rMissChars);
		}
	};
	
	/**
	 * Compute the similarity between two token sequences. The argument token
	 * normalizer can modify tokens before they are compared via
	 * <code>equals()</code> to remove certain aspects of a comparison, e.g.
	 * case.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param minSimilarity the minimum similarity (used for cut-off)
	 * @param normalizer the token normalizer to use
	 * @param mismatchScorer the scorer for any mismatches
	 * @return the similarity of the two token sequences
	 */
	public static float getSequenceSimilarity(TokenSequence tokens, TokenSequence reference, float minSimilarity) {
		return getSequenceSimilarity(tokens, reference, minSimilarity, DEFAULT_TOKEN_NORMALIZER, DEFAULT_SEQUENCE_MISMATCH_SCORER);
	}
	
	/**
	 * Compute the similarity between two token sequences. The argument token
	 * normalizer can modify tokens before they are compared via
	 * <code>equals()</code> to remove certain aspects of a comparison, e.g.
	 * case.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param minSimilarity the minimum similarity (used for cut-off)
	 * @param normalizer the token normalizer to use
	 * @param mismatchScorer the scorer for any mismatches
	 * @return the similarity of the two token sequences
	 */
	public static float getSequenceSimilarity(TokenSequence tokens, TokenSequence reference, float minSimilarity, TokenNormalizer normalizer) {
		return getSequenceSimilarity(tokens, reference, minSimilarity, normalizer, DEFAULT_SEQUENCE_MISMATCH_SCORER);
	}
	
	/**
	 * Compute the similarity between two token sequences. The argument
	 * mismatch scorer is called upon to judge all unmatched tokens, e.g. to
	 * determine similarity based upon edit distance.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param minSimilarity the minimum similarity (used for cut-off)
	 * @param normalizer the token normalizer to use
	 * @param mismatchScorer the scorer for any mismatches
	 * @return the similarity of the two token sequences
	 */
	public static float getSequenceSimilarity(TokenSequence tokens, TokenSequence reference, float minSimilarity, TokenMismatchScorer mismatchScorer) {
		return getSequenceSimilarity(tokens, reference, minSimilarity, DEFAULT_TOKEN_NORMALIZER, mismatchScorer);
	}
	
	/**
	 * Compute the similarity between two token sequences. The argument token
	 * normalizer can modify tokens before they are compared via
	 * <code>equals()</code> to remove certain aspects of a comparison, e.g.
	 * case. The argument mismatch scorer is called upon to judge all unmatched
	 * tokens, e.g. to determine similarity based upon edit distance.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param minSimilarity the minimum similarity (used for cut-off)
	 * @param normalizer the token normalizer to use
	 * @param mismatchScorer the scorer for any mismatches
	 * @return the similarity of the two token sequences
	 */
	public static float getSequenceSimilarity(TokenSequence tokens, TokenSequence reference, float minSimilarity, TokenNormalizer normalizer, TokenMismatchScorer mismatchScorer) {
		ArrayList rTokens = new ArrayList(reference.size());
//		CountingSet rBag = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER)); ==> token normalizer handles case !!!
		CountingSet rBag = new CountingSet(new TreeMap());
		int rChars = 0;
		for (int t = 0; t < reference.size(); t++) {
			String value = normalizer.normalize(reference.valueAt(t));
			if (value == null)
				continue;
			rTokens.add(value);
			rBag.add(value);
			rChars += value.length();
		}
		
		ArrayList tTokens = new ArrayList(tokens.size());
//		CountingSet tBag = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER)); ==> token normalizer handles case !!!
		CountingSet tBag = new CountingSet(new TreeMap());
		int tChars = 0;
		for (int t = 0; t < tokens.size(); t++) {
			String value = normalizer.normalize(tokens.valueAt(t));
			if (value == null)
				continue;
			tTokens.add(value);
			tBag.add(value);
			tChars += value.length();
		}
		
		//	check bag match against threshold (sequence match cannot be higher)
		//	TODO use token mis-match scorer !!! (might well compensate mismatches against one another ...)
//		TreeSet bag = new TreeSet(String.CASE_INSENSITIVE_ORDER); ==> token normalizer handles case !!!
		TreeSet bag = new TreeSet();
		bag.addAll(rBag);
		bag.addAll(tBag);
		int rBagMissChars = 0;
		int tBagMissChars = 0;
		for (Iterator tit = bag.iterator(); tit.hasNext();) {
			String token = ((String) tit.next());
			int rCount = rBag.getCount(token);
			int tCount = tBag.getCount(token);
			if (rCount < tCount)
				tBagMissChars += ((tCount - rCount) * token.length());
			else if (tCount < rCount)
				rBagMissChars += ((rCount - tCount) * token.length());
		}
		float bagPrecision = (((float) (tChars - tBagMissChars)) / tChars);
		float bagRecall = (((float) (rChars - rBagMissChars)) / rChars);
		if ((bagPrecision * bagRecall) <= minSimilarity)
			return 0;
		
		//	eliminate token sequences against one another
//		int rMissChars = 0;
//		int tMissChars = 0;
		ArrayList mismatches = new ArrayList();
		int tPos = 0;
		int rPos = 0;
		for (int t = 0; (t < rTokens.size()) && (t < tTokens.size()); t++) {
			String rToken = ((String) rTokens.get(t));
			String tToken = ((String) tTokens.get(t));
			
			//	we have a match, eliminate and continue
			if (tToken.equalsIgnoreCase(rToken)) {
				rTokens.remove(t);
				rBag.remove(rToken);
				tTokens.remove(t);
				tBag.remove(tToken);
				tPos++;
				rPos++;
				t--;
				continue;
			}
			
			//	check which token is yet to come in other sequence
			int rTokenToComeR = rBag.getCount(rToken);
			int rTokenToComeT = tBag.getCount(rToken);
			int tTokenToComeT = tBag.getCount(tToken);
			int tTokenToComeR = rBag.getCount(tToken);
			
			//	substitution (neither token to come in other sequence)
			if ((rTokenToComeT == 0) && (tTokenToComeR == 0)) {
				mismatches.add(new TokenMismatch(tToken, tPos, rToken, rPos));
				rTokens.remove(t);
				rBag.remove(rToken);
//				rMissChars += rToken.length();
				tTokens.remove(t);
				tBag.remove(tToken);
//				tMissChars += tToken.length();
				tPos++;
				rPos++;
				t--;
				continue;
			}
			
			//	missing at least one instance in challenger tokens
			else if (rTokenToComeT < rTokenToComeR) {
				mismatches.add(new TokenMismatch(null, -1, rToken, rPos));
				rTokens.remove(t);
				rBag.remove(rToken);
//				rMissChars += rToken.length();
				rPos++;
				t--;
				continue;
			}
			
			//	missing at least one instance in reference
			else if (tTokenToComeR < tTokenToComeT) {
				mismatches.add(new TokenMismatch(tToken, tPos, null, -1));
				tTokens.remove(t);
				tBag.remove(tToken);
//				tMissChars += tToken.length();
				tPos++;
				t--;
				continue;
			}
			
			//	must be a token twiddle or shift, find which one is next to occur in other
			//	TODO maybe depend this on characters instead ??? (two short words are better to omit than one long one)
			int rTokenToComeIn = -1;
			for (int lt = (t+1); lt < tTokens.size(); lt++) {
				String ltToken = ((String) tTokens.get(lt));
				if (rToken.equalsIgnoreCase(ltToken)) {
					rTokenToComeIn = (lt - t);
					break;
				}
			}
			int tTokenToComeIn = -1;
			for (int lt = (t+1); lt < rTokens.size(); lt++) {
				String lrToken = ((String) rTokens.get(lt));
				if (tToken.equalsIgnoreCase(lrToken)) {
					tTokenToComeIn = (lt - t);
					break;
				}
			}
			
			//	reference token further out in challenger tokens, remove it
			if (rTokenToComeIn > tTokenToComeIn) {
				mismatches.add(new TokenMismatch(null, -1, rToken, rPos));
				rTokens.remove(t);
				rBag.remove(rToken);
//				rMissChars += rToken.length();
				rPos++;
				t--;
				continue;
			}
			
			//	challenger token further out in reference tokens, remove it
			else if (tTokenToComeIn > rTokenToComeIn) {
				mismatches.add(new TokenMismatch(tToken, tPos, null, -1));
				tTokens.remove(t);
				tBag.remove(tToken);
//				tMissChars += tToken.length();
				tPos++;
				t--;
				continue;
			}
			
			//	both equally far out, omit shorter one
			if (rToken.length() < tToken.length()) {
				mismatches.add(new TokenMismatch(null, -1, rToken, rPos));
				rTokens.remove(t);
				rBag.remove(rToken);
//				rMissChars += rToken.length();
				rPos++;
				t--;
				continue;
			}
			else if (tToken.length() < rToken.length()) {
				mismatches.add(new TokenMismatch(tToken, tPos, null, -1));
				tTokens.remove(t);
				tBag.remove(tToken);
//				tMissChars += tToken.length();
				tPos++;
				t--;
				continue;
			}
			
			//	both tokens equally long, omit one in sequence with more tokens left
			if (rTokens.size() > tTokens.size()) {
				mismatches.add(new TokenMismatch(null, -1, rToken, rPos));
				rTokens.remove(t);
				rBag.remove(rToken);
//				rMissChars += rToken.length();
				rPos++;
				t--;
				continue;
			}
			else if (tTokens.size() > rTokens.size()) {
				mismatches.add(new TokenMismatch(tToken, tPos, null, -1));
				tTokens.remove(t);
				tBag.remove(tToken);
//				tMissChars += tToken.length();
				tPos++;
				t--;
				continue;
			}
			
			//	equally many tokens left in both sequences, omit in challenger sequence
			//	TODO maybe use coin flip ???
			else {
				mismatches.add(new TokenMismatch(tToken, tPos, null, -1));
				tTokens.remove(t);
				tBag.remove(tToken);
//				tMissChars += tToken.length();
				tPos++;
				t--;
				continue;
			}
		}
		
		//	count whatever is left in either sequence
		for (int t = 0; t < rTokens.size(); t++) {
			String rToken = ((String) rTokens.get(t));
//			rMissChars += rToken.length();
			mismatches.add(new TokenMismatch(null, -1, rToken, rPos++));
		}
		for (int t = 0; t < tTokens.size(); t++) {
			String tToken = ((String) tTokens.get(t));
//			tMissChars += tToken.length();
			mismatches.add(new TokenMismatch(tToken, tPos++, null, -1));
		}
		
		//	finally ...
//		float seqPrecision = (((float) (tChars - tMissChars)) / tChars);
//		float seqRecall = (((float) (rChars - rMissChars)) / rChars);
		TokenMismatchScore score = mismatchScorer.getScore(((TokenMismatch[]) mismatches.toArray(new TokenMismatch[mismatches.size()])), tokens, reference, false);
		float seqPrecision = (((float) (tChars - score.missChars)) / tChars);
		float seqRecall = (((float) (rChars - score.refMissChars)) / rChars);
		return (seqPrecision * seqRecall);
	}
	
	/**
	 * Compute the similarity between two token sequences in an order
	 * insensitive fashion.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @return the order insensitive similarity of the two token sequences
	 */
	public static float getBagSimilarity(TokenSequence tokens, TokenSequence reference) {
		return getBagSimilarity(tokens, reference, DEFAULT_TOKEN_NORMALIZER, DEFAULT_SEQUENCE_MISMATCH_SCORER);
	}
	
	/**
	 * Compute the similarity between two token sequences in an order
	 * insensitive fashion. The argument token normalizer can modify tokens
	 * before they are compared via <code>equals()</code> to remove certain
	 * aspects of a comparison, e.g. case.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param normalizer the token normalizer to use
	 * @return the order insensitive similarity of the two token sequences
	 */
	public static float getBagSimilarity(TokenSequence tokens, TokenSequence reference, TokenNormalizer normalizer) {
		return getBagSimilarity(tokens, reference, normalizer, DEFAULT_SEQUENCE_MISMATCH_SCORER);
	}
	
	/**
	 * Compute the similarity between two token sequences in an order
	 * insensitive fashion. The argument mismatch scorer is called upon to
	 * judge all unmatched tokens, e.g. to determine similarity based upon
	 * edit distance.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param mismatchScorer the scorer for any mismatches
	 * @return the order insensitive similarity of the two token sequences
	 */
	public static float getBagSimilarity(TokenSequence tokens, TokenSequence reference, TokenMismatchScorer mismatchScorer) {
		return getBagSimilarity(tokens, reference, DEFAULT_TOKEN_NORMALIZER, mismatchScorer);
	}
	
	/**
	 * Compute the similarity between two token sequences in an order
	 * insensitive fashion. The argument token normalizer can modify tokens
	 * before they are compared via <code>equals()</code> to remove certain
	 * aspects of a comparison, e.g. case. The argument mismatch scorer is
	 * called upon to judge all unmatched tokens, e.g. to determine similarity
	 * based upon edit distance.
	 * @param tokens the tokens to compare
	 * @param reference the token sequence to compare to
	 * @param normalizer the token normalizer to use
	 * @param mismatchScorer the scorer for any mismatches
	 * @return the order insensitive similarity of the two token sequences
	 */
	public static float getBagSimilarity(TokenSequence tokens, TokenSequence reference, TokenNormalizer normalizer, TokenMismatchScorer mismatchScorer) {
//		CountingSet rBag = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER)); ==> token normalizer handles case !!!
		CountingSet rBag = new CountingSet(new TreeMap());
		int rChars = 0;
		for (int t = 0; t < reference.size(); t++) {
			String value = normalizer.normalize(reference.valueAt(t));
			if (value == null)
				continue;
			rBag.add(value);
			rChars += value.length();
		}
		
//		CountingSet tBag = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER)); ==> token normalizer handles case !!!
		CountingSet tBag = new CountingSet(new TreeMap());
		int tChars = 0;
		for (int t = 0; t < tokens.size(); t++) {
			String value = normalizer.normalize(tokens.valueAt(t));
			if (value == null)
				continue;
			if (rBag.contains(value))
				rBag.remove(value);
			else tBag.add(value);
			tChars += value.length();
		}
		
//		int rMissChars = 0;
//		for (Iterator tit = rBag.iterator(); tit.hasNext();) {
//			String token = ((String) tit.next());
//			rMissChars += (token.length() * rBag.getCount(token));
//		}
//		int tMissChars = 0;
//		for (Iterator tit = tBag.iterator(); tit.hasNext();) {
//			String token = ((String) tit.next());
//			tMissChars += (token.length() * tBag.getCount(token));
//		}
//		
//		float precision = (((float) (tChars - tMissChars)) / tChars);
//		float recall = (((float) (rChars - rMissChars)) / rChars);
//		return (precision * recall);
		
		ArrayList mismatches = new ArrayList();
		for (Iterator tit = rBag.iterator(); tit.hasNext();) {
			String rToken = ((String) tit.next());
			for (int c = 0; c < rBag.getCount(rToken); c++)
				mismatches.add(new TokenMismatch(null, -1, rToken, -1));
		}
		for (Iterator tit = tBag.iterator(); tit.hasNext();) {
			String tToken = ((String) tit.next());
			for (int c = 0; c < tBag.getCount(tToken); c++)
				mismatches.add(new TokenMismatch(tToken, -1, null, -1));
		}
		
		TokenMismatchScore score = mismatchScorer.getScore(((TokenMismatch[]) mismatches.toArray(new TokenMismatch[mismatches.size()])), tokens, reference, true);
		float precision = (((float) (tChars - score.missChars)) / tChars);
		float recall = (((float) (rChars - score.refMissChars)) / rChars);
		return (precision * recall);
	}
}
