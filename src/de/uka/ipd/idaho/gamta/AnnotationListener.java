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

import java.lang.ref.WeakReference;



/**
 * An observer for Annotations being added, removed, and renamed in a document.
 * All arguments handed over to methods of this interface are strictly
 * immutable. That means any attempt of changing the type, the value, or an
 * attribute of an Annotation will result in an exception being thrown.
 * 
 * @author sautter
 */
public interface AnnotationListener {
	
	/** receive notification that an Annotation was added to a document
	 * @param	doc			the document the Annotation was added to
	 * @param	annotation	the Annotation that was added to the document
	 */
	public abstract void annotationAdded(QueriableAnnotation doc, Annotation annotation);
	
	/** receive notification that an Annotation was removed from a document
	 * @param	doc			the document the Annotation was removed from
	 * @param	annotation	the Annotation that was removed from the document
	 */
	public abstract void annotationRemoved(QueriableAnnotation doc, Annotation annotation);
	
	/** receive notification that the type of an Annotation has changed
	 * @param	doc			the document the Annotation belongs to
	 * @param	annotation	the Annotation whode type changed
	 * @param	oldType		the type of the Annotation before the change (the new type is available from the annotation)
	 */
	public abstract void annotationTypeChanged(QueriableAnnotation doc, Annotation annotation, String oldType);
	
	/** receive notification that an attribute of an Annotation has changed
	 * @param	doc				the document the Annotation belongs to
	 * @param	annotation		the Annotation that had the attribute changed
	 * @param	attributeName	the name of the attribute that changed (null indicates that multiple attributes have changed at once, eg through clearAttributes() or copyAttributes(), oldValue will be null as well in these cases)
	 * @param	oldValue		the value of the attribute before the change (the new value is available from the Annotation)
	 */
	public abstract void annotationAttributeChanged(QueriableAnnotation doc, Annotation annotation, String attributeName, Object oldValue);
	
	/**
	 * Weak reference wrapper for annotation listeners. Client code that needs
	 * to be eligible for reclaiming by GC despite a sole strong reference to
	 * it still existing in a listener added to an editable annotation it wants
	 * to observe can use this class to add a weak reference link to the actual
	 * listener.
	 * 
	 * @author sautter
	 */
	public static class WeakAnnotationListener implements AnnotationListener {
		private WeakReference alWeakRef;
		private EditableAnnotation doc;
		
		/** Constructor
		 * @param al the annotation listener to wrap
		 * @param doc the document observed by the argument listener
		 */
		public WeakAnnotationListener(AnnotationListener al, EditableAnnotation doc) {
			this.alWeakRef = new WeakReference(al);
			this.doc = doc;
		}
		
		private AnnotationListener getAnnotationListener() {
			AnnotationListener al = ((AnnotationListener) this.alWeakRef.get());
			if (al == null) {
				if (this.doc != null)
					this.doc.removeAnnotationListener(this);
				this.doc = null;
			}
			return al;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.AnnotationListener#annotationAdded(de.uka.ipd.idaho.gamta.QueriableAnnotation, de.uka.ipd.idaho.gamta.Annotation)
		 */
		public void annotationAdded(QueriableAnnotation doc, Annotation annotation) {
			AnnotationListener al = this.getAnnotationListener();
			if (al != null)
				al.annotationAdded(doc, annotation);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.AnnotationListener#annotationRemoved(de.uka.ipd.idaho.gamta.QueriableAnnotation, de.uka.ipd.idaho.gamta.Annotation)
		 */
		public void annotationRemoved(QueriableAnnotation doc, Annotation annotation) {
			AnnotationListener al = this.getAnnotationListener();
			if (al != null)
				al.annotationRemoved(doc, annotation);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.AnnotationListener#annotationTypeChanged(de.uka.ipd.idaho.gamta.QueriableAnnotation, de.uka.ipd.idaho.gamta.Annotation, java.lang.String)
		 */
		public void annotationTypeChanged(QueriableAnnotation doc, Annotation annotation, String oldType) {
			AnnotationListener al = this.getAnnotationListener();
			if (al != null)
				al.annotationTypeChanged(doc, annotation, oldType);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.AnnotationListener#annotationAttributeChanged(de.uka.ipd.idaho.gamta.QueriableAnnotation, de.uka.ipd.idaho.gamta.Annotation, java.lang.String, java.lang.Object)
		 */
		public void annotationAttributeChanged(QueriableAnnotation doc, Annotation annotation, String attributeName, Object oldValue) {
			AnnotationListener al = this.getAnnotationListener();
			if (al != null)
				al.annotationAttributeChanged(doc, annotation, attributeName, oldValue);
		}
	}
}
