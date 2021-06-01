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



/**
 * An annotation mimicing a document, allowing to edit the part of the document
 * it marks as if it were a document for itself
 * 
 * @author sautter
 */
public interface MutableAnnotation extends QueriableAnnotation, MutableTokenSequence {
	
	/** annotation type for marking a first level section in a document */
	public static final String SECTION_TYPE = "section";
	
	/** annotation type for marking a second level section (sub section) in a document */
	public static final String SUB_SECTION_TYPE = "subSection";
	
	/** annotation type for marking a third level section (sub sub section) in a document */
	public static final String SUB_SUB_SECTION_TYPE = "subSubSection";
	
	/** annotation type for marking a paragraph in a document */
	public static final String PARAGRAPH_TYPE = "paragraph";
	
	/** annotation type for marking a sentence in a document */
	public static final String SENTENCE_TYPE = "sentence";
	
	/**
	 * Add an Annotation to this mutable Annotation. Note: If this mutable
	 * Annotation is not the root of the document Annotation hierarchy, the
	 * Annotation is also added to all mutable Annotations above this one. The
	 * Annotation's startIndex will be adjusted to the startIndex of the
	 * individual mutable Annotations.
	 * @param annotation the Annotation marking the Tokens to annotate
	 * @return an Annotation equal to the one just added (though references are
	 *         not guaranteed to be equal in the sense of '==')
	 */
	public abstract MutableAnnotation addAnnotation(Annotation annotation);
	
	/**
	 * Add an Annotation to this mutable Annotation. Note: If this mutable
	 * Annotation is not the root of the document Annotation hierarchy, the
	 * Annotation is also added to all mutable Annotations above this one. A
	 * call to this method has to be implemented to be equivalent to
	 * <code>addAnnotation(startIndex, (startIndex + size), type)</code>.
	 * @param type the type of the Annotation to make
	 * @param startIndex the index of the Token the Annotation starts at
	 *            (relative to this mutable Annotation's startIndex)
	 * @param size the number of Tokens contained in the Annotation
	 * @return the new Annotation
	 */
	public abstract MutableAnnotation addAnnotation(String type, int startIndex, int size);
	
	/**
	 * Add an Annotation to this mutable Annotation. Note: If this mutable
	 * Annotation is not the root of the document Annotation hierarchy, the
	 * Annotation is also added to all mutable Annotations above this one. A
	 * call to this method has to be implemented to be equivalent to
	 * <code>addAnnotation(type, startIndex, (endIndex - startIndex))</code>.
	 * @param startIndex the index of the Token the Annotation starts at
	 *            (relative to this mutable Annotation's startIndex)
	 * @param endIndex the index of the Token the Annotation ends before
	 *            (relative to this mutable Annotation's startIndex)
	 * @param type the type of the Annotation to make
	 * @return the new Annotation
	 */
	public abstract MutableAnnotation addAnnotation(int startIndex, int endIndex, String type);
	
	/**
	 * Remove an Annotation from this mutable Annotation.
	 * @param annotation the Annotation to be removed
	 * @return an Annotation equal to the one just removed (though references
	 *         are not guaranteed to be equal in the sense of '==')
	 */
	public abstract Annotation removeAnnotation(Annotation annotation);
	
	/**
	 * Remove an Annotation and all Tokens it spans from this mutable
	 * Annotation. Note: This method is a shortcut for
	 * removeTokens(annotation.getStartIndex(), annotation.size())
	 * @param annotation the Annotation to be removed
	 * @return a TokenSequence containing the Tokens that were just removed
	 */
	public abstract TokenSequence removeTokens(Annotation annotation);
	
	/**
	 * Get the mutable Annotation with a certain identifier contained in this
	 * one.
	 * @param id the identifier of the desired mutable Annotation
	 * @return the mutable Annotation with the specified identifier contained
	 *         in this one
	 */
	public abstract MutableAnnotation getMutableAnnotation(String id);
	
	/**
	 * Get the mutable Annotations contained in this one.
	 * @return the mutable Annotations contained in this one
	 */
	public abstract MutableAnnotation[] getMutableAnnotations();
	
	/**
	 * Get the mutable Annotations of a certain type contained in this one.
	 * @param type the type of the desired mutable Annotations
	 * @return the mutable Annotations of the specified type contained in this
	 *         one
	 */
	public abstract MutableAnnotation[] getMutableAnnotations(String type);
	
	/**
	 * Retrieve the mutable Annotations nested in this one that span a range of
	 * Tokens. This returns all nested mutable Annotations whose start index is
	 * less than or equal to the argument start index and whose end index is
	 * larger than or equal to the argument end index. Implementations must
	 * interpret the argument start and end index relative to this mutable
	 * Annotation.
	 * @param startIndex the index of the first spanned Token
	 * @param endIndex the index after the last spanned Token
	 * @return an array holding the spanning mutable Annotations
	 */
	public abstract MutableAnnotation[] getMutableAnnotationsSpanning(int startIndex, int endIndex);
	
	/**
	 * Retrieve the mutable Annotations of a given type that are nested in this
	 * one and span a range of Tokens. This returns all nested mutable
	 * Annotations of the argument type whose start index is less than or equal
	 * to the argument start index and whose end index is larger than or equal
	 * to the argument end index. Implementations must interpret the argument
	 * start and end index relative to this mutable Annotation.
	 * @param type the type of the Annotations to get
	 * @param startIndex the index of the first spanned Token
	 * @param endIndex the index after the last spanned Token
	 * @return an array holding the spanning mutable Annotations
	 */
	public abstract MutableAnnotation[] getMutableAnnotationsSpanning(String type, int startIndex, int endIndex);
//	
//	public abstract MutableAnnotation[] getMutableAnnotationsSpanning(Annotation annotation);
//	
//	public abstract MutableAnnotation[] getMutableAnnotationsSpanning(String type, Annotation annotation);
	
	/**
	 * Retrieve the mutable Annotations nested in this one that overlap with a
	 * range of Tokens. This returns all nested mutable Annotations whose start
	 * index is less than to the argument end index and whose end index is
	 * larger than the argument start index. Implementations must interpret the
	 * argument start and end index relative to this mutable Annotation.
	 * @param startIndex the index of the first Token
	 * @param endIndex the index after the last Token
	 * @return an array holding the overlapping mutable Annotations
	 */
	public abstract MutableAnnotation[] getMutableAnnotationsOverlapping(int startIndex, int endIndex);
	
	/**
	 * Retrieve the mutable Annotations of a given type that are nested in this
	 * one and overlap with a range of Tokens. This returns all nested mutable
	 * Annotations of the argument type whose start index is less than to the
	 * argument end index and whose end index is larger than the argument start
	 * index. Implementations must interpret the argument start and end index
	 * relative to this mutable Annotation.
	 * @param type the type of the Annotations to get
	 * @param startIndex the index of the first Token
	 * @param endIndex the index after the last Token
	 * @return an array holding the overlapping mutable Annotations
	 */
	public abstract MutableAnnotation[] getMutableAnnotationsOverlapping(String type, int startIndex, int endIndex);
//	
//	public abstract MutableAnnotation[] getMutableAnnotationsOverlapping(Annotation annotation);
//	
//	public abstract MutableAnnotation[] getMutableAnnotationsOverlapping(String type, Annotation annotation);
	
	/**
	 * Add a listener listening for changes to the annotation.
	 * @param al the listener to be added
	 */
	public abstract void addAnnotationListener(AnnotationListener al);
	
	/**
	 * Remove a listener from the annotation.
	 * @param al the listener to be removed
	 */
	public abstract void removeAnnotationListener(AnnotationListener al);
}
