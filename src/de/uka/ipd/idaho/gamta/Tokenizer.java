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

/**
 * general purpose tokenizer producing a char squence together with its token
 * overlay from some source of char data.
 * 
 * @author sautter
 */
public interface Tokenizer {
	
	/**
	 * Representation of an offset range in the underlaying char sequence used
	 * for tokenization. This class is not intended for long term representation
	 * of a token. The latter is up to the implementors of the TokenSequence
	 * interface.
	 * 
	 * @author sautter
	 */
	public static class CharSequenceToken {
		
		/** the start offset of the token in the char sequence
		 */
		public final int startOffset;
		
		/** the end offset of the token in the char sequence
		 */
		public final int endOffset;
		
		/** Constructor
		 * @param	startOffset		the start offset of the token in the char sequence
		 * @param	endOffset		the end offset of the token in the char sequence
		 */
		protected CharSequenceToken(int startOffset, int endOffset) {
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}
	}
	
	/**
	 * a Tokenizer for producing the token overlay on a char sequence. This
	 * tokenizer processes the char sequence block by block, i.e. in portions
	 * limited by whitespace characters. For the underlying char sequence, it is
	 * therefore sufficient to provide the char data between two whitespaces at a
	 * time. This behavior enables lazy, on-demand style loading of the char data to
	 * be tokenized.
	 * 
	 * @author sautter
	 */
	public abstract class TokenIterator {
		
		/**	the char sequence to work on
		 */
		protected CharSequence charData;
		
		/**	the current offset in the char sequence
		 */
		protected int currentOffset = 0;
		
		//	the buffer for tokens
		private ArrayList tokens = new ArrayList();
		
		/** Constructor
		 * @param	charData	the char sequence to tokenize
		 */
		protected TokenIterator(CharSequence charData) {
			this.charData = charData;
		}
		
		/**	check if more tokens available
		 * @return true if there are more tokens available, i.e. the getNextToken() method is guarantied not to return null 
		 */
		public boolean hasMoreTokens() {
			if (this.charData == null)
				return false;
			if (this.tokens.isEmpty())
				this.fillBuffer();
			if (this.tokens.isEmpty()) {
				this.charData = null;
				return false;
			}
			return true;
		}
		
		/**	get the next token
		 * @return the next token in line, or null, if there are no more tokens
		 */
		public CharSequenceToken getNextToken() {
			if (this.charData == null)
				return null;
			if (this.tokens.isEmpty())
				this.fillBuffer();
			if (this.tokens.isEmpty()) {
				this.charData = null;
				return null;
			}
			return ((CharSequenceToken) this.tokens.remove(0));
		}
		
		private synchronized void fillBuffer() {
			
			//	find start of next block
			while ((this.currentOffset < this.charData.length()) && isSpace(this.charData.charAt(this.currentOffset)))
				this.currentOffset++;
			
			//	find borders of next block
			int blockStart = this.currentOffset;
			while ((this.currentOffset < this.charData.length()) && !isSpace(this.charData.charAt(this.currentOffset)))
				this.currentOffset++;
			
			//	check for end of char data
			if (blockStart == this.currentOffset)
				return;
			
			//	get token borders
			int[] borders = this.tokenize(this.charData.subSequence(blockStart, this.currentOffset));
			if (borders.length == 0) {
				borders = new int[1];
				borders[0] = 0;
			}
			
			//	create tokens
			for (int b = 0; b < (borders.length - 1); b++)
				this.tokens.add(new CharSequenceToken((blockStart + borders[b]), (blockStart + borders[b+1])));
			this.tokens.add(new CharSequenceToken((blockStart + borders[borders.length - 1]), this.currentOffset));
		}
		
		/**	tokenize a portion of the underlying char sequence
		 * @param	chars	the portion of the underlying char sequence to tokenize (guaranteed not to contain any whitespace characters)
		 * @return an array of ints marking the starts of the individual tokens in the specified portion of the underlying char sequence
		 */
		protected abstract int[] tokenize(CharSequence chars);
		
		/**
		 * Check is a character represents whitespace. This method returns true
		 * for the following decimal values (hex in brackets): &lt;33 (&lt;21),
		 * 160 (A0), 8192-8205 (2000-200D), 8239 (202f), 8287-8288 (205f-2060),
		 * 12288 (3000), 65279 (FEFF), all of which are defined as whitespace in
		 * Unicode. None of the characters this method returns true for may be
		 * contained in a token value.
		 * @param ch the character to check
		 * @return true if the argument character represents whitespace, false
		 *         otherwise
		 */
		protected static final boolean isSpace(char ch) {
			//	covers basic controls and \u0020 --> ASCII space
			if (ch < 0x0021)
				return true;
			//	we're good up until DEL
			if (ch < 0x007F)
				return false;
			//	covers high control and \u00A0 --> non-breaking space
			if (ch < 0x00A1)
				return true;
			//	\u1680 --> ogham space
			if (ch == 0x1680)
				return true;
			//	we're good up until space block
			if (ch < 0x2000)
				return false;
			//	covers \u2000 --> en quad through \u200B --> zero-width space, and some controls to follow
			if (ch < 0x2010)
				return true;
			//	we're good up until separator characters
			if (ch < 0x2028)
				return false;
			//	covers \u2028 --> line separator through \u202F --> narrow non-breaking space
			if (ch < 0x2030)
				return true;
			//	we're good up until math space
			if (ch < 0x205F)
				return false;
			//	covers all sorts of text flow control characters and \u205F --> medium math space
			if (ch < 0x2070)
				return true;
			//	we're good up until ideographic descriptor void
			if (ch < 0x2FFC)
				return false;
			//	covers some voids and \u3000 --> ideographic space
			if (ch < 0x3001)
				return true;
			//	we're good up until variation selectors
			if (ch < 0xFE00)
				return false;
			//	variation selectors
			if (ch < 0xFE10)
				return true;
			//	we're good up until \uFEFF --> zero-width non-breaking space
			if (ch < 0xFEFF)
				return false;
			//	covers \uFEFF --> zero-width non-breaking space and its subsequent void
			if (ch < 0xFF01)
				return true;
			//	rest of UTF-16 range should be good
			return false;
		}
	}
	
	/**	obtain a token iterator returning token spans according to this Tokenizer's style of tokenization
	 * @param	cs	the char sequence for the iterator to process 
	 * @return a token iterator for the specified char sequence
	 */
	public abstract TokenIterator getTokenIterator(CharSequence cs);
	
	/**	tokenize a char sequence (produce the token overlay over a sequence of chars)
	 * @param	cs	the char sequence to be decomposed to Tokens (implementations should return a MutableTokenSequence if the runtime type of the specified char sequence is MutableCharSequence)
	 * @return a TokenSequence containing the Tokens the specified char sequence was decomposed to
	 */
	public abstract TokenSequence tokenize(CharSequence cs);
	
	/**	tokenize a char sequence (produce the token overlay over a mutable sequence of chars)
	 * @param	cs	the char sequence to be decomposed to Tokens
	 * @return a MutableTokenSequence containing the Tokens the specified char sequence was decomposed to
	 */
	public abstract MutableTokenSequence tokenize(MutableCharSequence cs);
}
