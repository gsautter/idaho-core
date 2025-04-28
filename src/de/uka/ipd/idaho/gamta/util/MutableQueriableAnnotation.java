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
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.MutableCharSequence;
import de.uka.ipd.idaho.gamta.MutableTokenSequence;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.TokenSequence;
import de.uka.ipd.idaho.gamta.TokenSequenceListener;

/**
 * Wrapper for a queriable annotation to reflect the full API of a mutable
 * annotation. Any attempt at modifying the annotations or tokens of the
 * wrapped queriable annotation results in an
 * <code>UnsupportedOperationException</code>. This class is helpful, for
 * instance, for passing an editable annotation through an interface that
 * requires a mutable annotation, but to a specific implementations that
 * actually works fine with the functionality of a queriable annotation, e.g.
 * an implementation of the <code>Analyzer</code> interface that only extracts
 * data from a document, but does not make any actual modifications.
 * 
 * @author sautter
 */
public class MutableQueriableAnnotation extends EditableQueriableAnnotation implements MutableAnnotation {
	private String annotErrorMessage;
	private String tokenErrorMessage;
	
	/** Constructor
	 * @param data the editable annotation to wrap
	 */
	public MutableQueriableAnnotation(QueriableAnnotation data) {
		this(data, null, null);
	}
	
	/** Constructor
	 * @param data the editable annotation to wrap
	 * @param annotErrorMessage the error message to use in the exceptions
	 *            thrown for attempted modifications to annotations
	 * @param tokenErrorMessage the error message to use in the exceptions
	 *            thrown for attempted token modifications
	 */
	public MutableQueriableAnnotation(QueriableAnnotation data, String annotErrorMessage, String tokenErrorMessage) {
		super(data);
		this.annotErrorMessage = ((annotErrorMessage == null) ? "Annotations cannot be modified on this document" : annotErrorMessage);
		this.tokenErrorMessage = ((tokenErrorMessage == null) ? "Characters and tokens cannot be modified on this document" : tokenErrorMessage);
	}
	
	public CharSequence setLeadingWhitespace(CharSequence whitespace) throws IllegalArgumentException {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public CharSequence setValueAt(CharSequence value, int index) throws IllegalArgumentException {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public CharSequence setWhitespaceAfter(CharSequence whitespace, int index) throws IllegalArgumentException {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public TokenSequence removeTokensAt(int index, int size) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public CharSequence insertTokensAt(CharSequence tokens, int index) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public CharSequence addTokens(CharSequence tokens) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public void clear() {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public MutableTokenSequence getMutableSubsequence(int start, int size) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	
	public void addTokenSequenceListener(TokenSequenceListener tsl) {}
	public void removeTokenSequenceListener(TokenSequenceListener tsl) {}
	public void addChar(char ch) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public void addChars(CharSequence chars) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public void insertChar(char ch, int offset) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public void insertChars(CharSequence chars, int offset) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public char removeChar(int offset) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public CharSequence removeChars(int offset, int length) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public char setChar(char ch, int offset) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public CharSequence setChars(CharSequence chars, int offset, int length) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public MutableCharSequence mutableSubSequence(int start, int end) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public void addCharSequenceListener(CharSequenceListener csl) {}
	public void removeCharSequenceListener(CharSequenceListener csl) {}

	public TokenSequence removeTokens(Annotation annotation) {
		throw new UnsupportedOperationException(this.tokenErrorMessage);
	}
	public MutableAnnotation addAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException(annotErrorMessage);
	}
	public MutableAnnotation addAnnotation(String type, int startIndex, int size) {
		throw new UnsupportedOperationException(annotErrorMessage);
	}
	public MutableAnnotation addAnnotation(int startIndex, int endIndex, String type) {
		throw new UnsupportedOperationException(annotErrorMessage);
	}
	
	public MutableAnnotation getMutableAnnotation(String id) {
		QueriableAnnotation annot = super.getAnnotation(id);
		return ((annot == null) ? null : new MutableQueriableAnnotation(annot));
	}
	public MutableAnnotation[] getMutableAnnotations() {
		return this.wrapQueriableAnnotationsMutable(this.getEditableAnnotations());
	}
	public MutableAnnotation[] getMutableAnnotations(String type) {
		return this.wrapQueriableAnnotationsMutable(this.getEditableAnnotations(type));
	}
	public MutableAnnotation[] getMutableAnnotationsSpanning(int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsMutable(this.getEditableAnnotationsSpanning(startIndex, endIndex));
	}
	public MutableAnnotation[] getMutableAnnotationsSpanning(String type, int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsMutable(this.getEditableAnnotationsSpanning(type, startIndex, endIndex));
	}
	public MutableAnnotation[] getMutableAnnotationsOverlapping(int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsMutable(this.getMutableAnnotationsOverlapping(startIndex, endIndex));
	}
	public MutableAnnotation[] getMutableAnnotationsOverlapping(String type, int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsMutable(this.getMutableAnnotationsOverlapping(type, startIndex, endIndex));
	}
	private MutableAnnotation[] wrapQueriableAnnotationsMutable(QueriableAnnotation[] eAnnots) {
		MutableAnnotation[] mAnnots = new MutableAnnotation[eAnnots.length];
		for (int a = 0; a < eAnnots.length; a++)
			mAnnots[a] = new MutableQueriableAnnotation(eAnnots[a]);
		return mAnnots;
	}
}
