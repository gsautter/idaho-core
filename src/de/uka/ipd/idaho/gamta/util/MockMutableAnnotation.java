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
package de.uka.ipd.idaho.gamta.util;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.CharSequenceListener;
import de.uka.ipd.idaho.gamta.EditableAnnotation;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.MutableCharSequence;
import de.uka.ipd.idaho.gamta.MutableTokenSequence;
import de.uka.ipd.idaho.gamta.TokenSequence;
import de.uka.ipd.idaho.gamta.TokenSequenceListener;

/**
 * Wrapper enabling implementations of <code>EditableAnnotation</code> to be
 * processed by implementations of <code>Analyzer</code>, provided the latter
 * do not modify the underlying token and char sequence.
 * 
 * @author sautter
 */
public class MockMutableAnnotation extends GenericEditableAnnotationWrapper implements MutableAnnotation {
	
	/** Constructor
	 * @param data the editable annotation to wrap
	 */
	public MockMutableAnnotation(EditableAnnotation data) {
		super(data);
	}
	
	//	methods from MutableCharSequence
	public void addChar(char ch) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public void addChars(CharSequence chars) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public void insertChar(char ch, int offset) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public void insertChars(CharSequence chars, int offset) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public char removeChar(int offset) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public CharSequence removeChars(int offset, int length) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public char setChar(char ch, int offset) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public CharSequence setChars(CharSequence chars, int offset, int length) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public MutableCharSequence mutableSubSequence(int start, int end) {
		throw new UnsupportedOperationException("The character sequence underlying this document cannot be modified.");
	}
	public void addCharSequenceListener(CharSequenceListener csl) {}
	public void removeCharSequenceListener(CharSequenceListener csl) {}
	
	//	methods from MutableTokenSequence
	public CharSequence setLeadingWhitespace(CharSequence whitespace) throws IllegalArgumentException {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public CharSequence setValueAt(CharSequence value, int index) throws IllegalArgumentException {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public CharSequence setWhitespaceAfter(CharSequence whitespace, int index) throws IllegalArgumentException {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public TokenSequence removeTokensAt(int index, int size) {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public CharSequence insertTokensAt(CharSequence tokens, int index) {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public CharSequence addTokens(CharSequence tokens) {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public void clear() {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public MutableTokenSequence getMutableSubsequence(int start, int size) {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public void addTokenSequenceListener(TokenSequenceListener tsl) {}
	public void removeTokenSequenceListener(TokenSequenceListener tsl) {}
	
	//	methods from MutableAnnotation
	public TokenSequence removeTokens(Annotation annotation) {
		throw new UnsupportedOperationException("The token sequence underlying this document cannot be modified.");
	}
	public MutableAnnotation getMutableAnnotation(String id) {
		EditableAnnotation ea = this.getEditableAnnotation(id);
		return ((ea == null) ? null : new MockMutableAnnotation(ea));
	}
	public MutableAnnotation[] getMutableAnnotations() {
		return this.getMutableAnnotations(null);
	}
	public MutableAnnotation[] getMutableAnnotations(String type) {
		return wrapMutable(this.getEditableAnnotations(type));
	}
	public MutableAnnotation[] getMutableAnnotationsSpanning(int startIndex, int endIndex) {
		return this.getMutableAnnotationsSpanning(null, startIndex, endIndex);
	}
	public MutableAnnotation[] getMutableAnnotationsSpanning(String type, int startIndex, int endIndex) {
		return wrapMutable(this.getEditableAnnotationsSpanning(type, startIndex, endIndex));
	}
	public MutableAnnotation[] getMutableAnnotationsOverlapping(int startIndex, int endIndex) {
		return this.getMutableAnnotationsOverlapping(null, startIndex, endIndex);
	}
	public MutableAnnotation[] getMutableAnnotationsOverlapping(String type, int startIndex, int endIndex) {
		return wrapMutable(this.getEditableAnnotationsOverlapping(type, startIndex, endIndex));
	}
	private static MutableAnnotation[] wrapMutable(EditableAnnotation[] eas) {
		MutableAnnotation[] mas = new MutableAnnotation[eas.length];
		for (int a = 0; a < eas.length; a++)
			mas[a] = new MockMutableAnnotation(eas[a]);
		return mas;
	}
	
	//	overwritten methods from EditableAnnotation
	public MutableAnnotation addAnnotation(Annotation annotation) {
		EditableAnnotation ea = super.addAnnotation(annotation);
		return ((ea == null) ? null : new MockMutableAnnotation(ea));
	}
	public MutableAnnotation addAnnotation(String type, int startIndex, int size) {
		EditableAnnotation ea = super.addAnnotation(type, startIndex, size);
		return ((ea == null) ? null : new MockMutableAnnotation(ea));
	}
	public MutableAnnotation addAnnotation(int startIndex, int endIndex, String type) {
		EditableAnnotation ea = super.addAnnotation(startIndex, endIndex, type);
		return ((ea == null) ? null : new MockMutableAnnotation(ea));
	}
}
