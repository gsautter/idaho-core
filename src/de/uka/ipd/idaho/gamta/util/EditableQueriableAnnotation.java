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
import de.uka.ipd.idaho.gamta.AnnotationListener;
import de.uka.ipd.idaho.gamta.EditableAnnotation;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;

/**
 * Wrapper for a queriable annotation to reflect the full API of an editable
 * annotation. Any attempt at modifying the annotations of the wrapped queriable
 * annotation results in an <code>UnsupportedOperationException</code>. This
 * class is helpful, for instance, for passing a queriable annotation through
 * an interface that requires an editable annotation, but to a specific
 * implementations that actually works fine with the functionality of a
 * queriable annotation, e.g. an annotation tool that only extracts data, but
 * does not make any actual modifications.
 * 
 * @author sautter
 */
public class EditableQueriableAnnotation extends GenericQueriableAnnotationWrapper implements EditableAnnotation {
	private String annotErrorMessage;
	
	/** Constructor
	 * @param data the queriable annotation to wrap
	 */
	public EditableQueriableAnnotation(QueriableAnnotation data) {
		this(data, null);
	}
	
	/** Constructor
	 * @param data the queriable annotation to wrap
	 * @param annotErrorMessage the error message to use in the exceptions
	 *            thrown for attempted modifications to annotations
	 */
	public EditableQueriableAnnotation(QueriableAnnotation data, String annotErrorMessage) {
		super(data);
		this.annotErrorMessage = ((annotErrorMessage == null) ? "Annotations cannot be modified on this document" : annotErrorMessage);
	}
	
	public EditableAnnotation addAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException(this.annotErrorMessage);
	}
	public EditableAnnotation addAnnotation(String type, int startIndex, int size) {
		throw new UnsupportedOperationException(this.annotErrorMessage);
	}
	public EditableAnnotation addAnnotation(int startIndex, int endIndex, String type) {
		throw new UnsupportedOperationException(this.annotErrorMessage);
	}
	public Annotation removeAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException(this.annotErrorMessage);
	}
	
	public EditableAnnotation getEditableAnnotation(String id) {
		QueriableAnnotation annot = super.getAnnotation(id);
		return ((annot == null) ? null : new EditableQueriableAnnotation(annot));
	}
	public EditableAnnotation[] getEditableAnnotations() {
		return this.wrapQueriableAnnotationsEditable(this.getEditableAnnotations());
	}
	public EditableAnnotation[] getEditableAnnotations(String type) {
		return this.wrapQueriableAnnotationsEditable(this.getEditableAnnotations(type));
	}
	public EditableAnnotation[] getEditableAnnotationsSpanning(int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsEditable(this.getEditableAnnotationsSpanning(startIndex, endIndex));
	}
	public EditableAnnotation[] getEditableAnnotationsSpanning(String type, int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsEditable(this.getEditableAnnotationsSpanning(type, startIndex, endIndex));
	}
	public EditableAnnotation[] getEditableAnnotationsOverlapping(int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsEditable(this.getEditableAnnotationsOverlapping(startIndex, endIndex));
	}
	public EditableAnnotation[] getEditableAnnotationsOverlapping(String type, int startIndex, int endIndex) {
		return this.wrapQueriableAnnotationsEditable(this.getEditableAnnotationsOverlapping(type, startIndex, endIndex));
	}
	private EditableAnnotation[] wrapQueriableAnnotationsEditable(QueriableAnnotation[] qAnnots) {
		EditableAnnotation[] eAnnots = new MutableAnnotation[qAnnots.length];
		for (int a = 0; a < eAnnots.length; a++)
			eAnnots[a] = new EditableQueriableAnnotation(qAnnots[a]);
		return eAnnots;
	}
	
	public void addAnnotationListener(AnnotationListener al) {}
	public void removeAnnotationListener(AnnotationListener al) {}
}
