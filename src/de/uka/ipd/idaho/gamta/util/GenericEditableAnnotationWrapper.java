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


import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationListener;
import de.uka.ipd.idaho.gamta.EditableAnnotation;

/**
 * This class implements a generic wrapper for arbitrary editable annotations.
 * It loops all method calls through to the wrapped annotations. The purpose of
 * this class is to provide a standard wrapper implementation of all the methods
 * in EditableAnnotation in situations where some few of the methods need to be
 * added functionality through a wrapper class. Wrappers can simply extend this
 * class and overwrite methods as needed while avoiding having to implement all
 * the other methods as well. If Tokens and Annotations retrieved from this
 * wrapper need to be wrapped as well, the respective sub classes of this class
 * should overwrite the wrapToken(), wrapAnnotation(), wrapEditableAnnotation(),
 * and wrapMutableAnnotation() methods to provide the respective wrappers.
 * 
 * @author sautter
 */
public class GenericEditableAnnotationWrapper extends GenericQueriableAnnotationWrapper implements EditableAnnotation {
	
	/**
	 * the wrapped annotation (equal to the 'annotationData' and
	 * 'queriableAnnotationData' fields of super classes, existing to save
	 * class casts)
	 */
	protected EditableAnnotation editableAnnotationData;
	
	/** Constructor
	 * @param	data	the EditableAnnotation to wrap
	 */
	public GenericEditableAnnotationWrapper(EditableAnnotation data) {
		super(data);
		this.editableAnnotationData = data;
	}
	
	public EditableAnnotation addAnnotation(Annotation annotation) {
		return this.wrapEditableAnnotation(this.editableAnnotationData.addAnnotation(annotation));
	}
	
	public EditableAnnotation addAnnotation(String type, int startIndex, int size) {
		return this.wrapEditableAnnotation(this.editableAnnotationData.addAnnotation(type, startIndex, size));
	}
	
	public EditableAnnotation addAnnotation(int startIndex, int endIndex, String type) {
		return this.wrapEditableAnnotation(this.editableAnnotationData.addAnnotation(startIndex, endIndex, type));
	}
	
	public EditableAnnotation getEditableAnnotation(String id) {
		EditableAnnotation annotation = this.editableAnnotationData.getEditableAnnotation(id);
		return ((annotation == null) ? null : this.wrapEditableAnnotation(annotation));
	}
	
	public EditableAnnotation[] getEditableAnnotations() {
		return this.wrapEditableAnnotations(this.editableAnnotationData.getEditableAnnotations());
	}
	
	public EditableAnnotation[] getEditableAnnotations(String type) {
		return this.wrapEditableAnnotations(this.editableAnnotationData.getEditableAnnotations(type));
	}
	
	public EditableAnnotation[] getEditableAnnotationsSpanning(int startIndex, int endIndex) {
		return this.wrapEditableAnnotations(this.editableAnnotationData.getEditableAnnotationsSpanning(startIndex, endIndex));
	}
	
	public EditableAnnotation[] getEditableAnnotationsSpanning(String type, int startIndex, int endIndex) {
		return this.wrapEditableAnnotations(this.editableAnnotationData.getEditableAnnotationsSpanning(type, startIndex, endIndex));
	}
	
	public EditableAnnotation[] getEditableAnnotationsOverlapping(int startIndex, int endIndex) {
		return this.wrapEditableAnnotations(this.editableAnnotationData.getEditableAnnotationsOverlapping(startIndex, endIndex));
	}
	
	public EditableAnnotation[] getEditableAnnotationsOverlapping(String type, int startIndex, int endIndex) {
		return this.wrapEditableAnnotations(this.editableAnnotationData.getEditableAnnotationsOverlapping(type, startIndex, endIndex));
	}
	
	private EditableAnnotation[] wrapEditableAnnotations(EditableAnnotation[] annotations) {
		for (int a = 0; a < annotations.length; a++)
			annotations[a] = this.wrapEditableAnnotation(annotations[a]);
		return annotations;
	}
	
	public Annotation removeAnnotation(Annotation annotation) {
		return this.editableAnnotationData.removeAnnotation(annotation);
	}
	
	public void addAnnotationListener(AnnotationListener al) {
		this.editableAnnotationData.addAnnotationListener(al);
	}
	
	public void removeAnnotationListener(AnnotationListener al) {
		this.editableAnnotationData.removeAnnotationListener(al);
	}
	
	/** wrap a MutableAnnotation before returning it in order to provide additional functionality through the wrapper class
	 * Note: This default implementation simply returns the argument MutableAnnotation, sub classes are welcome to overwrite this method as needed.
	 * @param	annotation	the MutableAnnotation to wrap
	 * @return the wrapped MutableAnnotation
	 */
	protected EditableAnnotation wrapEditableAnnotation(EditableAnnotation annotation) {
		return annotation;
	}
}
