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
 * An Annotation allowing for retrieving Annotations nested in it
 * 
 * @author sautter
 */
public interface QueriableAnnotation extends Annotation {
	
	/**	
	 * Retrieve the index of this QueriableAnnotation's first Token in the
	 * underlying TokenSequence.
	 * @return the absolute start index of this QueriableAnnotation
	 */
	public abstract int getAbsoluteStartIndex();
	
	/**
	 * Retrieve the start offset of this QueriableAnnotation's first Token in
	 * the underlying TokenSequence (in its property of a CharSequence).
	 * @return the absolute start offset of this QueriableAnnotation
	 */
	public abstract int getAbsoluteStartOffset();
	
	/**
	 * Retrieve an Annotation nested in this one via its identifier.
	 * @param id the identifier of the desired Annotation
	 * @return the Annotation with the argument identifier
	 */
	public abstract QueriableAnnotation getAnnotation(String id);
	
	/**
	 * Retrieve the Annotations nested in this one.
	 * @return an array holding the nested Annotations
	 */
	public abstract QueriableAnnotation[] getAnnotations();
	
	/**
	 * Retrieve the Annotations of a given type nested in this one.
	 * @param type the type of the Annotations to get
	 * @return an array holding the nested Annotations
	 */
	public abstract QueriableAnnotation[] getAnnotations(String type);
	
	/**
	 * Retrieve the Annotations nested in this one that span a range of Tokens.
	 * This returns all nested Annotations whose start index is less than or
	 * equal to the argument start index and whose end index is larger than or
	 * equal to the argument end index. Implementations must interpret the
	 * argument start and end index relative to this QueriableAnnotation.
	 * @param startIndex the index of the first spanned Token
	 * @param endIndex the index after the last spanned Token
	 * @return an array holding the spanning Annotations
	 */
	public abstract QueriableAnnotation[] getAnnotationsSpanning(int startIndex, int endIndex);
	
	/**
	 * Retrieve the Annotations of a given type that are nested in this one
	 * and span a range of Tokens. This returns all nested Annotations of the
	 * argument type whose start index is less than or equal to the argument
	 * start index and whose end index is larger than or equal to the argument
	 * end index. Implementations must interpret the argument start and end
	 * index relative to this QueriableAnnotation.
	 * @param type the type of the Annotations to get
	 * @param startIndex the index of the first spanned Token
	 * @param endIndex the index after the last spanned Token
	 * @return an array holding the spanning Annotations
	 */
	public abstract QueriableAnnotation[] getAnnotationsSpanning(String type, int startIndex, int endIndex);
//	
//	public abstract QueriableAnnotation[] getAnnotationsSpanning(Annotation annotation);
//	
//	public abstract QueriableAnnotation[] getAnnotationsSpanning(String type, Annotation annotation);
	
	/**
	 * Retrieve the Annotations nested in this one that overlap with a range of
	 * Tokens. This returns all nested Annotations whose start index is less
	 * than the argument end index and whose end index is larger than the
	 * argument start index. Implementations must interpret the argument start
	 * and end index relative to this QueriableAnnotation.
	 * @param startIndex the index of the first Token
	 * @param endIndex the index after the last Token
	 * @return an array holding the overlapping Annotations
	 */
	public abstract QueriableAnnotation[] getAnnotationsOverlapping(int startIndex, int endIndex);
	
	/**
	 * Retrieve the Annotations of a given type that are nested in this one and
	 * overlap with a range of Tokens. This returns all nested Annotations of
	 * the argument type whose start index is less than the argument end index
	 * and whose end index is larger than the argument start index.
	 * Implementations must interpret the argument start and end index relative
	 * to this QueriableAnnotation.
	 * @param type the type of the Annotations to get
	 * @param startIndex the index of the first Token
	 * @param endIndex the index after the last Token
	 * @return an array holding the overlapping Annotations
	 */
	public abstract QueriableAnnotation[] getAnnotationsOverlapping(String type, int startIndex, int endIndex);
//	
//	public abstract QueriableAnnotation[] getAnnotationsOverlapping(Annotation annotation);
//	
//	public abstract QueriableAnnotation[] getAnnotationsOverlapping(String type, Annotation annotation);
	
	/**
	 * Retrieve the types of the annotations nested in this one.
	 * @return an array holding the types of the nested annotations
	 */
	public abstract String[] getAnnotationTypes();
	
	/**
	 * Retrieve the type-based Annotation nesting order currently valid in the
	 * underlying document.
	 * @return a string representing the type-based Annotation nesting order
	 */
	public abstract String getAnnotationNestingOrder();
}
