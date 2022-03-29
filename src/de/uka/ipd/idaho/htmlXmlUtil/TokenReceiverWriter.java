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
package de.uka.ipd.idaho.htmlXmlUtil;

import java.io.IOException;
import java.io.Writer;

import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;

/**
 * Writer tokenizing XML output written to it, and forwarding said output to a
 * wrapped <code>TokenReceiver</code> one token at a time. Instances of this
 * class work very similar to <code>TokenSource</code>s, but without blocking
 * on the input character stream until a complete token is available.
 * 
 * @author sautter
 */
public class TokenReceiverWriter extends Writer {
	private static final boolean DEBUG = false;
	
	private Grammar grammar;
	private final char tagStart;
	private final char tagEnd;
	private final char endTagMarker;
//	private final char tagAttributeSeparator;
//	private final char tagAttributeValueSeparator;
	private final char startMarkerStart;
//	private final char endMarkerEnd;
	private final String commentStartMarker;
	private final String commentEndMarker;
	private final String processingInstructionStartMarker;
	private final String processingInstructionEndMarker;
	private final String dtdStartMarker;
	private final String dtdEndMarker;
	
	private char[] buffer = new char[2048];
	private int bufferStartPos = 0;
	private int bufferEndPos = 0;
	private int charsSinceLastTokenCheck = 0;
	
	private String awaitedEndTag = null;
	private int treeDepth = 0;
	private TokenReceiver tokenOut;
	
	/** Constructor
	 * @param tokenOut the token receiver to write to
	 */
	public TokenReceiverWriter(TokenReceiver tokenOut) {
		this(null, tokenOut);
	}
	
	/**
	 * @param grammar the grammar to use for tokenization
	 * @param tokenOut the token receiver to write to
	 */
	public TokenReceiverWriter(Grammar grammar, TokenReceiver tokenOut) {
		this.grammar = ((grammar == null) ? new StandardGrammar() : grammar);
		this.tokenOut = tokenOut;
		
		//	get characters for tag recognition
		this.tagStart = this.grammar.getTagStart();
		this.tagEnd = this.grammar.getTagEnd();
		this.endTagMarker = this.grammar.getEndTagMarker();
//		this.tagAttributeSeparator = this.grammar.getTagAttributeSeparator();
//		this.tagAttributeValueSeparator = this.grammar.getTagAttributeValueSeparator();
		this.startMarkerStart = this.grammar.getStartMarkerStartChar();
//		this.endMarkerEnd = this.grammar.getEndMarkerEndChar();
		
		//	get comment markers
		String csm = this.grammar.getCommentStartMarker();
		this.commentStartMarker = (this.grammar.correctErrors() ? csm.substring(0, (csm.length() - (csm.length() / 3))) : csm);
		String cem = this.grammar.getCommentEndMarker();
		this.commentEndMarker = (this.grammar.correctErrors() ? cem.substring(cem.length() / 3) : cem);
		
		//	get processing instruction markers
		this.processingInstructionStartMarker = this.grammar.getProcessingInstructionStartMarker();
		this.processingInstructionEndMarker = this.grammar.getProcessingInstructionEndMarker();
		
		//	get DTD markers
		this.dtdStartMarker = this.grammar.getDtdStartMarker();
		this.dtdEndMarker = this.grammar.getDtdEndMarker();
	}
	
	/* (non-Javadoc)
	 * @see java.io.Writer#write(char[], int, int)
	 */
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (this.buffer.length < (this.bufferEndPos + len)) {
			int bufferLevel = (this.bufferEndPos - this.bufferStartPos);
			char[] nBuffer = this.buffer;
			
			//	increase buffer if required ...
			if (this.buffer.length < (bufferLevel + len)) {
				int nLength = (this.buffer.length * 2);
				while (nLength < (bufferLevel + len))
					nLength *= 2;
				nBuffer = new char[nLength];
			}
			
			//	move buffer contents to start of potentially enlarged buffer
			System.arraycopy(this.buffer, this.bufferStartPos, nBuffer, 0, bufferLevel);
			this.buffer = nBuffer;
			
			//	buffer increased or not, the contents are back to the buffer starts now
			this.bufferEndPos -= this.bufferStartPos;
			this.bufferStartPos = 0;
		}
		
		//	write output
		System.arraycopy(cbuf, off, this.buffer, this.bufferEndPos, len);
		this.bufferEndPos += len;
		
		//	check for completed tokens to write
		this.charsSinceLastTokenCheck += len;
		if (this.charsSinceLastTokenCheck > 256)
			this.writeTokens(false);
	}
	
	/* (non-Javadoc)
	 * @see java.io.Writer#flush()
	 */
	public void flush() throws IOException {
		this.writeTokens(false); // violates contract of flush(), but keeping data tokens together requires it
	}
	
	/* (non-Javadoc)
	 * @see java.io.Writer#close()
	 */
	public void close() throws IOException {
		this.writeTokens(true);
		this.tokenOut.close();
	}
	
	private void writeTokens(boolean isClose) throws IOException {
		this.charsSinceLastTokenCheck = 0;
		
		//	refill buffer
		while (this.bufferStartPos < this.bufferEndPos) {
			String token = this.writeToken(((this.awaitedEndTag == null) ? null : ("" + this.tagStart + "" + this.endTagMarker + "" + this.awaitedEndTag + "" + this.tagEnd)), isClose);
			if (token == null)
				break;
			if (DEBUG) System.out.println("TokenReceiverWriter got token: " + token);
			if (token.length() == 0)
				break;
		}
	}
	
	private String writeToken(String stopTag, boolean isFlush) throws IOException {
		if (DEBUG) System.out.println("TokenReceiverWriter: producing token" + ((stopTag == null) ? "" : (" up to '" + stopTag + "'")));
		
		//	end of input
		if (this.bufferEndPos <= this.bufferStartPos)
			return null;
		
		//	waiting for some end tag
		else if (stopTag != null)
			return this.writeUpTo(stopTag, false);
		
		//	comment
		else if (this.bufferStartsWith(this.commentStartMarker, this.bufferStartPos))
			return this.writeUpTo(this.commentEndMarker, true);
		
		//	DTD
		else if (this.bufferStartsWith(this.dtdStartMarker, this.bufferStartPos))
			return this.writeUpTo(this.dtdEndMarker, true);
		
		//	processing instruction
		else if (this.bufferStartsWith(this.processingInstructionStartMarker, this.bufferStartPos))
			return this.writeUpTo(this.processingInstructionEndMarker, true);
		
		//	tag, or data starting with tag start
		else if (this.bofferHasTagStartAt(this.bufferStartPos))
			return this.writeTag();
		
		//	character data
		else return this.writeData(!isFlush);
	}
	
	private String writeUpTo(String stopSequence, boolean includeStopSequence) throws IOException {
		int ePos = this.findInBuffer(stopSequence, this.bufferStartPos);
		if (ePos == -1)
			return null;
		if (includeStopSequence)
			ePos += stopSequence.length();
		String token = new String(this.buffer, this.bufferStartPos, (ePos - this.bufferStartPos));
		this.bufferStartPos = ePos;
		this.tokenOut.storeToken(token, this.treeDepth);
		return token;
	}
	
	private String writeTag() throws IOException {
		int ePos = this.findInBuffer(this.tagEnd, this.bufferStartPos);
		if (ePos == -1)
			return null;
		ePos++; // we want that tag end marker as well !!!
		String tag = new String(this.buffer, this.bufferStartPos, (ePos - this.bufferStartPos));
		this.bufferStartPos = ePos;
		
		//	check type
		char tokenType;
		if (this.grammar.isEndTag(tag))
			tokenType = '>';
		else if (this.grammar.isSingularTag(tag))
			tokenType = '/';
		else tokenType = '<';
		
		//	tag token, might have to wait for end tag
		if ((this.awaitedEndTag != null) && this.grammar.isEndTag(tag) && this.awaitedEndTag.equalsIgnoreCase(this.grammar.getType(tag)))
			this.awaitedEndTag = null;
		else if (this.grammar.waitForEndTag(tag))
			this.awaitedEndTag = this.grammar.getType(tag);
		
		//	store token
		if (tokenType == '>')
			this.treeDepth--;
		this.tokenOut.storeToken(tag, this.treeDepth);
		if (tokenType == '<')
			this.treeDepth++;
		
		//	finally ...
		return tag;
	}
	
	private String writeData(boolean explicitEndOnly) throws IOException {
		int ePos = this.findInBuffer(this.startMarkerStart, this.bufferStartPos);
		if (ePos == -1) {
			if (explicitEndOnly)
				return null;
			else ePos = this.bufferEndPos;
		}
		else if (this.bufferStartsWith(this.commentStartMarker, ePos)) {} // start of comment / end of data
		else if (this.bufferStartsWith(this.dtdStartMarker, ePos)) {} // start of DTD / end of data
		else if (this.bufferStartsWith(this.processingInstructionStartMarker, ePos)) {} // start of processing instruction / end of data
		else if (this.bofferHasTagStartAt(ePos)) {} // start of tag, or data starting with tag start / end of data
		else if (explicitEndOnly)
			return null;
		else ePos = this.bufferEndPos;
		
		String data = new String(this.buffer, this.bufferStartPos, (ePos - this.bufferStartPos));
		this.bufferStartPos = ePos;
		this.tokenOut.storeToken(data, this.treeDepth);
		return data;
	}
	
	private boolean bofferHasTagStartAt(int atPos) {
		if (this.bufferEndPos < (atPos + 2))
			return false;
		if (this.buffer[atPos] != this.tagStart)
			return false;
		char nCh = this.buffer[atPos + 1];
		return (Character.isLetter(nCh) || ("_:".indexOf(nCh) != -1) || (nCh == this.endTagMarker));
	}
	
	private boolean bufferStartsWith(String str, int atPos) {
		if (this.bufferEndPos < (atPos + str.length()))
			return false;
		for (int c = 0; c < str.length(); c++) {
			if (this.buffer[atPos + c] != str.charAt(c))
				return false;
		}
		return true;
	}
	
	private int findInBuffer(char ch, int fromPos) {
		for (int c = fromPos; c < this.bufferEndPos; c++) {
			if (this.buffer[c] == ch)
				return c;
		}
		return -1;
	}
	
	private int findInBuffer(String str, int fromPos) {
		int sPos = this.findInBuffer(str.charAt(0), fromPos);
		if (str.length() == 1)
			return sPos;
		if (sPos == -1)
			return sPos;
		if (this.bufferStartsWith(str, sPos))
			return sPos;
		else return this.findInBuffer(str, (sPos + 1));
	}
//	
//	//	TEST ONLY !!!
//	public static void main(String[] args) throws Exception {
//		File f = new File("E:/Projektdaten/ZooKeys/ZooKeys-698-113.xml");
//		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
//		TokenReceiverWriter trw = new TokenReceiverWriter(new TokenReceiver() {
//			public void storeToken(String token, int treeDepth) throws IOException {
//				System.out.println("Token (" + treeDepth + "): '" + token + "'");
//			}
//			public void close() throws IOException {}
//		});
//		char[] buf = new char[256];
//		for (int r; (r = br.read(buf, 0, buf.length)) != -1;)
//			trw.write(buf, 0, r);
//		trw.flush();
//		trw.close();
//		br.close();
//	}
}
