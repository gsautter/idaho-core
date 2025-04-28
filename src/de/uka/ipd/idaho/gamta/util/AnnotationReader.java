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
package de.uka.ipd.idaho.gamta.util;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.AnnotationUtils.XmlOutputOptions;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.Token;

/**
 * A Reader implementation similar to StringReader, but using a
 * QueriableAnnotation as its source instead of a String. Text and attribute
 * values are escaped to comply with XML syntax rules.
 * 
 * @author sautter
 */
public class AnnotationReader extends Reader {
	private QueriableAnnotation source;
	private String indent = null;
	private XmlOutputOptions outputOptions;
	
	private Annotation[] nestedAnnotations;
	private Stack stack = new Stack();
	private int annotationPointer = 0;
	
	private Token token = null;
	private Token lastToken;
	private int tokenIndex = 0;
	
	private boolean lastWasLineBreak = true;
//	private boolean lastWasTag = true;
	
	private StringBuffer lineAssembler = new StringBuffer();
	private LinkedList lineBuffer = new LinkedList();
	private int bufferLevel = 0;
	private String string = "";
	private int stringOffset = 0;
	
	private HashSet lineBroken = new HashSet();
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 */
	public AnnotationReader(QueriableAnnotation source) {
		this(source, null, wrapXmlOutputOptions(false, null, null));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param outputIDs include annotation IDs in the output?
	 */
	public AnnotationReader(QueriableAnnotation source, boolean outputIDs) {
		this(source, null, wrapXmlOutputOptions(outputIDs, null, null));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param indent the String to insert for each level of indentation
	 *            (specifying null will result in no indentation)
	 */
	public AnnotationReader(QueriableAnnotation source, String indent) {
		this(source, indent, wrapXmlOutputOptions(false, null, null));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param outputIDs include annotation IDs in the output?
	 * @param indent the String to insert for each level of indentation
	 *            (specifying null will result in no indentation)
	 */
	public AnnotationReader(QueriableAnnotation source, boolean outputIDs, String indent) {
		this(source, indent, wrapXmlOutputOptions(outputIDs, null, null));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param typeFilter a set containing the types of the annotations to
	 *            include in the output (specifying null will include all
	 *            attributes)
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 */
	public AnnotationReader(QueriableAnnotation source, Set typeFilter, Set attributeFilter) {
		this(source, null, wrapXmlOutputOptions(false, typeFilter, attributeFilter));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param outputIDs include annotation IDs in the output?
	 * @param typeFilter a set containing the types of the annotations to
	 *            include in the output (specifying null will include all
	 *            attributes)
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 */
	public AnnotationReader(QueriableAnnotation source, boolean outputIDs, Set typeFilter, Set attributeFilter) {
		this(source, null, wrapXmlOutputOptions(outputIDs, typeFilter, attributeFilter));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param indent the String to insert for each level of indentation
	 *            (specifying null will result in no indentation)
	 * @param typeFilter a set containing the types of the annotations to
	 *            include in the output (specifying null will include all
	 *            attributes)
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 */
	public AnnotationReader(QueriableAnnotation source, String indent, Set typeFilter, Set attributeFilter) {
		this(source, indent, wrapXmlOutputOptions(false, typeFilter, attributeFilter));
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param outputIDs include annotation IDs in the output?
	 * @param indent the String to insert for each level of indentation
	 *            (specifying null will result in no indentation)
	 * @param typeFilter a set containing the types of the annotations to
	 *            include in the output (specifying null will include all
	 *            attributes)
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 */
	public AnnotationReader(QueriableAnnotation source, boolean outputIDs, String indent, Set typeFilter, Set attributeFilter) {
		this(source, indent, wrapXmlOutputOptions(outputIDs, typeFilter, attributeFilter));
	}
	
	private static final XmlOutputOptions xmlWriteOptionsNoId = new XmlOutputOptions() {
		public boolean writeAnnotations(String annotType) {
			return true;
		}
		public boolean writeAttribute(String name) {
			return true;
		}
		public boolean includeIDs(String annotType) {
			return false;
		}
		public boolean escapeTokens() {
			return true;
		}
		public boolean escapeAttributeValues() {
			return true;
		}
	};
	private static final XmlOutputOptions xmlWriteOptionsWithId = new XmlOutputOptions() {
		public boolean writeAnnotations(String annotType) {
			return true;
		}
		public boolean writeAttribute(String name) {
			return true;
		}
		public boolean includeIDs(String annotType) {
			return true;
		}
		public boolean escapeTokens() {
			return true;
		}
		public boolean escapeAttributeValues() {
			return true;
		}
	};
	//	package private for use in annotation input stream
	static XmlOutputOptions wrapXmlOutputOptions(boolean outputIDs, Set typeFilter, Set attributeFilter) {
		if ((typeFilter == null) && (attributeFilter == null))
			return (outputIDs ? xmlWriteOptionsWithId : xmlWriteOptionsNoId);
		XmlOutputOptions options = new XmlOutputOptions();
		options.setIncludeIdTypes(Collections.emptySet(), outputIDs);
		if (typeFilter != null)
			options.setAnnotationTypes(typeFilter, false);
		if (attributeFilter != null)
			options.setAttributeNames(attributeFilter, false);
		return options;
	}
	
	/**
	 * Constructor
	 * @param source the DocumentPart to read from
	 * @param indent the String to insert for each level of indentation
	 *            (specifying null will result in no indentation)
	 * @param options an object holding the output options.
	 */
	public AnnotationReader(QueriableAnnotation source, String indent, XmlOutputOptions options) {
		this.source = source;
		this.indent = (((indent == null) || (indent.length() == 0)) ? null : indent);
		this.outputOptions = options;
		
		//	get nested annotations and apply filter
		Annotation[] nestedAnnotations = this.source.getAnnotations();
		ArrayList typeFilteredAnnotations = new ArrayList();
		for (int a = 0; a < nestedAnnotations.length; a++)
			if (this.outputOptions.writeAnnotations(nestedAnnotations[a].getType()))
				typeFilteredAnnotations.add(nestedAnnotations[a]);
		this.nestedAnnotations = ((Annotation[]) typeFilteredAnnotations.toArray(new Annotation[typeFilteredAnnotations.size()]));
		
		//	make sure there is a root element
		if ((this.nestedAnnotations.length == 0) || (this.nestedAnnotations[0].size() < this.source.size())) {
			nestedAnnotations = new Annotation[this.nestedAnnotations.length + 1];
			nestedAnnotations[0] = this.source;
			System.arraycopy(this.nestedAnnotations, 0, nestedAnnotations, 1, this.nestedAnnotations.length);
			this.nestedAnnotations = nestedAnnotations;
		}
	}
	
	/** @see java.io.Reader#close()
	 */
	public void close() throws IOException {
		this.source = null;
		
		this.lineBuffer = null;
		this.lineAssembler = null;
		this.string = null;
		
		this.nestedAnnotations = null;
		this.stack = null;
		this.token = null;
		this.lastToken = null;
	}
	
	/*
	 * @see java.io.Reader#read(char[], int, int)
	 */
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (this.source == null)
			throw new IOException("Stream closed");
		
		if (this.bufferLevel < len) {
			int added = this.fillBuffer((len - this.bufferLevel));
			
			if ((this.bufferLevel == 0) && (added == 0))
				return -1;
			else this.bufferLevel += added;
		}
		
		int w = 0;
		while (w < len) {
			if (this.stringOffset == this.string.length()) {
				if (this.lineBuffer.isEmpty())
					return w;
				this.string = ((String) this.lineBuffer.removeFirst());
				this.stringOffset = 0;
			}
			else {
				cbuf[off] = this.string.charAt(this.stringOffset);
				this.stringOffset++;
				this.bufferLevel--;
				off++;
				w++;
			}
		}
		
		return len;
	}
	
	/*	produce some Strings for the buffer
	 * @param	minChars	the minimum number of chars to produce
	 * @return the number of chars actually produced (if less than minChars, the end is near)
	 */
	private int fillBuffer(int minChars) {
		int newChars = 0;
		
		while (this.tokenIndex < this.source.size()) {
			
			//	switch to next Token
			this.lastToken = this.token;
			this.token = this.source.tokenAt(this.tokenIndex);
			
			//	write end tags for annotations ending before current Token
			while ((this.stack.size() > 0) && ((((Annotation) this.stack.peek()).getStartIndex() + ((Annotation) this.stack.peek()).size()) <= this.tokenIndex)) {
				Annotation annotation = ((Annotation) this.stack.pop());
				
				//	line break only if nested annotations
				if (!this.lastWasLineBreak && this.lineBroken.contains(annotation.getAnnotationID())) {
					this.lineAssembler.append(this.outputOptions.getLineBreak());
					this.lineBuffer.addLast(this.lineAssembler.toString());
					newChars += this.lineAssembler.length();
					this.lineAssembler = new StringBuffer();
					this.lastWasLineBreak = true;
				}
				
				//	add indent (TODO maybe only for line broken annotations)
				if (this.lastWasLineBreak && (this.indent != null)) {
					for (int i = 0; i < this.stack.size(); i++)
						this.lineAssembler.append(this.indent);
				}
				
				//	add end tag
				this.lineAssembler.append("</" + annotation.getType() + ">");
				this.lastWasLineBreak = false;
				
				//	store line (omit if in-line)
				if (this.outputOptions.writeInLine(annotation.getType()))
					continue;
				this.lineAssembler.append(this.outputOptions.getLineBreak());
				this.lineBuffer.addLast(this.lineAssembler.toString());
				newChars += this.lineAssembler.length();
				this.lineAssembler = new StringBuffer();
				this.lastWasLineBreak = true;
				//this.lastWasTag = true;
			}
			
			//	insert line break if required
			if (!this.lastWasLineBreak && (this.lastToken != null) && this.lastToken.hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE)) {
				this.lineAssembler.append(this.outputOptions.getLineBreak());
				this.lineBuffer.addLast(this.lineAssembler.toString());
				newChars += this.lineAssembler.length();
				this.lineAssembler = new StringBuffer();
				this.lastWasLineBreak = true;
			}
			
			//	skip space character before unspaced punctuation (e.g. ','), after line breaks and tags, and if there is no whitespace in the token sequence
			if (/*!this.lastWasTag && */!this.lastWasLineBreak && 
					(this.lastToken != null) && !this.lastToken.hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE) && 
					Gamta.insertSpace(this.lastToken, this.token) && 
					(this.tokenIndex != 0) && (this.source.getWhitespaceAfter(this.tokenIndex-1).length() != 0)
				) this.lineAssembler.append(this.outputOptions.getSpace());
			
			//	write start tags for Annotations beginning at current Token
			while ((this.annotationPointer < this.nestedAnnotations.length) && (this.nestedAnnotations[this.annotationPointer].getStartIndex() == this.tokenIndex)) {
				Annotation annotation = this.nestedAnnotations[this.annotationPointer++];
				
				//	line break  (omit if in-line)
				if (!this.lastWasLineBreak && !this.outputOptions.writeInLine(annotation.getType())) {
					this.lineAssembler.append(this.outputOptions.getLineBreak());
					this.lineBuffer.addLast(this.lineAssembler.toString());
					newChars += this.lineAssembler.length();
					this.lineAssembler = new StringBuffer();
				}
				
				//	add indent (TODO maybe only for line broken annotations)
				if (this.lastWasLineBreak && (this.indent != null)) {
					for (int i = 0; i < this.stack.size(); i++)
						this.lineAssembler.append(this.indent);
				}
				
				//	write start tag
				this.lineAssembler.append(AnnotationUtils.produceStartTag(annotation, this.outputOptions));
				//this.lastWasTag = true;
				this.stack.push(annotation);
				
				//	line break only if nested annotations (omit if in-line)
				if (this.outputOptions.writeInLine(annotation.getType()))
					continue;
				if ((this.annotationPointer < this.nestedAnnotations.length) && AnnotationUtils.contains(annotation, this.nestedAnnotations[this.annotationPointer])) {
					this.lineAssembler.append(this.outputOptions.getLineBreak());
					this.lineBuffer.addLast(this.lineAssembler.toString());
					newChars += this.lineAssembler.length();
					this.lineAssembler = new StringBuffer();
					this.lineBroken.add(annotation.getAnnotationID());
					this.lastWasLineBreak = true;
				}
			}
			
			//	append current token
			String tokenValue = this.token.getValue();
			this.lineAssembler.append(AnnotationUtils.escapeForXml(tokenValue));
			this.lastWasLineBreak = false;
			//this.lastWasTag = false;
			
			//	switch to next token
			this.tokenIndex++;
			
			//	some token is left for triggering the stack flush, and we've written enough characters, return
			if ((this.tokenIndex < this.source.size()) && (newChars >= minChars))
				return newChars;
		}
		
		//	write end tags for annotations not closed so far
		while (this.stack.size() > 0) {
			Annotation annotation = ((Annotation) this.stack.pop());
			
			//	line break only if nested annotations
			if (!this.lastWasLineBreak && this.lineBroken.contains(annotation.getAnnotationID())) {
				this.lineAssembler.append(this.outputOptions.getLineBreak());
				this.lineBuffer.addLast(this.lineAssembler.toString());
				newChars += this.lineAssembler.length();
				this.lineAssembler = new StringBuffer();
				this.lastWasLineBreak = true;
			}
			
			//	add indent (TODO maybe only for line broken annotations)
			if (this.lastWasLineBreak && (this.indent != null)) {
				for (int i = 0; i < this.stack.size(); i++)
					this.lineAssembler.append(this.indent);
			}
			
			//	add end tag
			this.lineAssembler.append("</" + annotation.getType() + ">");
			this.lastWasLineBreak = false;
		}
		
		this.lineBuffer.addLast(this.lineAssembler.toString());
		newChars += this.lineAssembler.length();
		this.lineAssembler = new StringBuffer();
		
		return newChars;
	}
}