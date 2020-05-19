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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.AttributeUtils;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.TokenSequence;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenMismatch;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenMismatchScore;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenMismatchScorer;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenNormalizer;
import de.uka.ipd.idaho.stringUtils.StringUtils;

/**
 * Instances of this class match up two GAMTA documents against one another in
 * a hierarchical fashion and produce a tree of matched annotation pairs. This
 * tree also contains half empty nodes for the annotations that did not find a
 * match in the respective other document. Annotation match trees come as an
 * array of nodes in depth-first order.<br/>
 * This class is intended to assess similarity (or to track changes or edits)
 * between different versions of the same document, not for general document
 * similarity assessment. While the latter is possible, this class is optimized
 * for the former.
 * 
 * @author sautter
 */
public class AnnotationTreeMatcher implements TokenNormalizer, TokenMismatchScorer {
	
	/**
	 * A pair of matching annotations. If an annotation failed to match up with
	 * another document, one of the versions is null. The parent matches are
	 * guaranteed to always contain two matching annotations.
	 * 
	 * @author sautter
	 */
	public static class AnnotationMatch/* implements Comparable*/ {
		public final AnnotationMatch[] parentMatches; 
		public final QueriableAnnotation refAnnotation; // null for additions
		public final QueriableAnnotation annotation; // null for removals
		AnnotationMatch(AnnotationMatch[] parentMatches, QueriableAnnotation refAnnotation, QueriableAnnotation annotation) {
			this.parentMatches = parentMatches;
			this.refAnnotation = refAnnotation;
			this.annotation = annotation;
		}
//		
//		public int compareTo(Object obj) {
//			AnnotationMatch annotMatch = ((AnnotationMatch) obj);
//			
//			for (int op = 0, mp = 0; (op < this.parentMatches.length) && (mp < annotMatch.parentMatches.length);) {
//				if (this.parentMatches[op] == annotMatch.parentMatches[mp]) {
//					op++;
//					mp++;
//				}
//				else return AnnotationUtils.compare(this.parentMatches[op].annotation, annotMatch.parentMatches[mp].annotation); // compare first differing parent
//			}
//			
//			if (this.parentMatches.length < annotMatch.parentMatches.length) {
//				if (this.annotation == null)
//					return AnnotationUtils.compare(this.refAnnotation, annotMatch.parentMatches[this.parentMatches.length].refAnnotation);
//				else return AnnotationUtils.compare(this.annotation, annotMatch.parentMatches[this.parentMatches.length].annotation);
//			}
//			if (annotMatch.parentMatches.length < this.parentMatches.length) {
//				if (annotMatch.annotation == null)
//					return AnnotationUtils.compare(this.parentMatches[annotMatch.parentMatches.length].refAnnotation, annotMatch.refAnnotation);
//				else return AnnotationUtils.compare(this.parentMatches[annotMatch.parentMatches.length].annotation, annotMatch.annotation);
//			}
//			
//			if ((this.annotation != null) && (annotMatch.annotation != null))
//				return AnnotationUtils.compare(this.annotation, annotMatch.annotation); // compare additions and updates
//			if ((this.refAnnotation != null) && (annotMatch.refAnnotation != null))
//				return AnnotationUtils.compare(this.refAnnotation, annotMatch.refAnnotation); // compare removals and updates
//			
//			QueriableAnnotation ownAnnot = ((this.refAnnotation == null) ? this.annotation : this.refAnnotation);
//			QueriableAnnotation matchAnnot = ((annotMatch.refAnnotation == null) ? annotMatch.annotation : annotMatch.refAnnotation);
//			
//			return AnnotationUtils.compare(ownAnnot, matchAnnot); // compare removals to additions
//			
//			//	TODOnot _somehow_ make this a total ordering
//		}
	}
	
	/** flag switching token match-up case sensitive */
	public static final int CASE_SENSITIVE = 0x01;
	
	/** flag switching token match-up accent sensitive */
	public static final int ACCENT_SENSITIVE = 0x02;
	
	/** flag specifying to observe punctuation marks in token match-up */
	public static final int PUNCTUATION_SENSITIVE = 0x04;
	
	private boolean ignoreCase;
	private boolean ignoreAccents;
	private boolean ignorePunctuation;
	
	/** Constructor
	 */
	public AnnotationTreeMatcher() {
		this(0);
	}
	
	/** Constructor
	 * @param matchFlags the match flags to use
	 */
	public AnnotationTreeMatcher(int matchFlags) {
		this.ignoreCase = ((matchFlags & CASE_SENSITIVE) == 0);
		this.ignoreAccents = ((matchFlags & ACCENT_SENSITIVE) == 0);
		this.ignorePunctuation = ((matchFlags & PUNCTUATION_SENSITIVE) == 0);;
	}
	
	/**
	 * Retrieve the token match flags.
	 * @return the match flags
	 */
	public int getMatchFlags() {
		return ((this.ignoreCase ? 0 : CASE_SENSITIVE) | (this.ignoreAccents ? 0 : ACCENT_SENSITIVE) | (this.ignorePunctuation ? 0 : PUNCTUATION_SENSITIVE));
	}
	
	/**
	 * Modify the token match flags.
	 * @param matchFlags the new match flags
	 */
	public void setMatchFlags(int matchFlags) {
		this.ignoreCase = ((matchFlags & CASE_SENSITIVE) == 0);
		this.ignoreAccents = ((matchFlags & ACCENT_SENSITIVE) == 0);
		this.ignorePunctuation = ((matchFlags & PUNCTUATION_SENSITIVE) == 0);;
	}
	
	/**
	 * Check whether or not the case insensitivity flag is set.
	 * @return true if current token match mode is case insensitive
	 */
	public boolean isIgnoringCase() {
		return this.ignoreCase;
	}
	
	/**
	 * Switch case insensitivity on or off.
	 * @param ignoreCase ignore case?
	 */
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Check whether or not the accent insensitivity flag is set.
	 * @return true if current token match mode is accent insensitive
	 */
	public boolean isIgnoringAccents() {
		return this.ignoreAccents;
	}

	/**
	 * Switch accent insensitivity on or off.
	 * @param ignoreAccents ignore accents?
	 */
	public void setIgnoreAccents(boolean ignoreAccents) {
		this.ignoreAccents = ignoreAccents;
	}

	/**
	 * Check whether or not the punctuation insensitivity flag is set.
	 * @return true if current token match mode is punctuation insensitive
	 */
	public boolean isIgnoringPunctuation() {
		return this.ignorePunctuation;
	}
	
	/**
	 * Switch punctuation insensitivity on or off.
	 * @param ignorePunctuation ignore punctuation?
	 */
	public void setIgnorePunctuation(boolean ignorePunctuation) {
		this.ignorePunctuation = ignorePunctuation;
	}
	
	private static boolean DEBUG_MATCHING = false; // TODO maybe make this into a 'verbose' property ???
	//	TODO maybe use progress monitor for output !?!
	private static final int childAnnotationIndexingThreshold = 10; // TODO optimize this, maybe better a bit higher than 2
	/* Indexing threshold test result:
	 * - slight disadvantage in smaller documents (~1.5 time slower)
	 * - considerable speedup in larger documents (~4 times faster) */
	
	private void treeMatch(LinkedList parentMatchStack, QueriableAnnotation annot, HashSet doneAnnotIDs, QueriableAnnotation refAnnot, HashSet doneRefAnnotIDs, boolean actualAnnot, ArrayList annotMatches, float idMatchThreshold) {
		if (actualAnnot) {
			doneAnnotIDs.add(annot.getAnnotationID());
			doneRefAnnotIDs.add(refAnnot.getAnnotationID());
		}
		
		AnnotationMatch[] parentMatches = ((AnnotationMatch[]) parentMatchStack.toArray(new AnnotationMatch[parentMatchStack.size()]));
		
//		long start = System.currentTimeMillis();
		QueriableAnnotation[] subAnnots = annot.getAnnotations();
//		if (parentMatchStack.size() < 2)
//			System.out.println("Got nested annotations in " + (System.currentTimeMillis() - start) + "ms");
		int lastAnnotEnd = 0;
		CountingSet childAnnotTypes = new CountingSet(new HashMap());
//		start = System.currentTimeMillis();
		for (int a = 0; a < subAnnots.length; a++) {
			if (subAnnots[a].getStartIndex() < lastAnnotEnd)
				continue; // to be or already handled recursively
			if (doneAnnotIDs.contains(subAnnots[a].getAnnotationID()))
				continue; // we've handled this one before (can happen with equally-sized nested annotations that have been handled further up the recursion tree)
			childAnnotTypes.add(subAnnots[a].getType());
			lastAnnotEnd = subAnnots[a].getEndIndex();
		}
//		if (parentMatchStack.size() < 2)
//			System.out.println("Index annotation types computed in " + (System.currentTimeMillis() - start) + "ms");
		
//		start = System.currentTimeMillis();
		HashMap subAnnotsByType = new HashMap();
		for (int a = 0; a < subAnnots.length; a++) {
			if (childAnnotTypes.getCount(subAnnots[a].getType()) < childAnnotationIndexingThreshold)
				continue;
			ArrayList typeSubAnnots = ((ArrayList) subAnnotsByType.get(subAnnots[a].getType()));
			if (typeSubAnnots == null) {
				typeSubAnnots = new ArrayList();
				subAnnotsByType.put(subAnnots[a].getType(), typeSubAnnots);
			}
			typeSubAnnots.add(subAnnots[a]);
		}
//		if (parentMatchStack.size() < 2)
//			System.out.println("Sub annotations indexed in " + (System.currentTimeMillis() - start) + "ms");
		
//		start = System.currentTimeMillis();
		QueriableAnnotation[] refSubAnnots = refAnnot.getAnnotations();
//		if (parentMatchStack.size() < 2)
//			System.out.println("Got nested reference annotations in " + (System.currentTimeMillis() - start) + "ms");
		LinkedHashMap refSubAnnotsByIDs = new LinkedHashMap();
//		start = System.currentTimeMillis();
		HashMap refSubAnnotsByType = new HashMap();
		for (int a = 0; a < refSubAnnots.length; a++) {
			if (doneRefAnnotIDs.contains(refSubAnnots[a].getAnnotationID()))
				continue;
			refSubAnnotsByIDs.put(refSubAnnots[a].getAnnotationID(), refSubAnnots[a]);
			if (childAnnotTypes.getCount(refSubAnnots[a].getType()) < childAnnotationIndexingThreshold)
				continue;
			ArrayList typeRefSubAnnots = ((ArrayList) refSubAnnotsByType.get(refSubAnnots[a].getType()));
			if (typeRefSubAnnots == null) {
				typeRefSubAnnots = new ArrayList();
				refSubAnnotsByType.put(refSubAnnots[a].getType(), typeRefSubAnnots);
			}
			typeRefSubAnnots.add(refSubAnnots[a]);
		}
//		if (parentMatchStack.size() < 2)
//			System.out.println("Reference sub annotations indexed in " + (System.currentTimeMillis() - start) + "ms");
		
		lastAnnotEnd = 0;
		for (int a = 0; a < subAnnots.length; a++) {
			if (subAnnots[a].getStartIndex() < lastAnnotEnd)
				continue; // to be or already handled recursively
			if (doneAnnotIDs.contains(subAnnots[a].getAnnotationID()))
				continue; // we've handled this one before (can happen with equally-sized nested annotations that have been handled further up the recursion tree)
			
			float useIdMatchThreshold;
			if (Float.isNaN(idMatchThreshold))
				useIdMatchThreshold = this.getIdMatchVerificationThreshold(subAnnots[a].getType(), annot.getType());
			else useIdMatchThreshold = idMatchThreshold;
			
			//	find ID match
			QueriableAnnotation mRefSubAnnot = ((useIdMatchThreshold > 1) ? null : ((QueriableAnnotation) refSubAnnotsByIDs.remove(subAnnots[a].getAnnotationID())));
			
			//	somehow scrutinize ID match (there _might_ be errors ...)
			if (mRefSubAnnot != null) {
				if (DEBUG_MATCHING) System.out.println("Seeking (ID, " + idMatchThreshold + " ==> " + useIdMatchThreshold + ") match for " + subAnnots[a].toXML());
				if (useIdMatchThreshold > 0) {
					float matchScore = TokenSequenceUtils.getSequenceSimilarity(subAnnots[a], mRefSubAnnot, 0, this, this);
					if (matchScore < useIdMatchThreshold) {
						if (DEBUG_MATCHING) System.out.println(" ==> rejected ID match (" + matchScore + " < " + useIdMatchThreshold + ") " + mRefSubAnnot.toXML());
						mRefSubAnnot = null;
					}
					else if (DEBUG_MATCHING) System.out.println(" ==> found ID match (" + matchScore + " > " + useIdMatchThreshold + ") " + mRefSubAnnot.toXML());
				}
				else if (DEBUG_MATCHING) System.out.println(" ==> found ID match " + mRefSubAnnot.toXML());
			}
			
			//	none found, try fuzzy matching
			if (mRefSubAnnot == null) {
//				start = System.currentTimeMillis();
				if (refSubAnnotsByType.containsKey(subAnnots[a].getType()))
					mRefSubAnnot = this.findMatchingAnnot((parentMatchStack.size() < 1), subAnnots[a], ((ArrayList) refSubAnnotsByType.get(subAnnots[a].getType())), annot.getType(), doneRefAnnotIDs);
				else mRefSubAnnot = this.findMatchingAnnot((parentMatchStack.size() < 1), subAnnots[a], refAnnot.getAnnotations(subAnnots[a].getType()), annot.getType(), doneRefAnnotIDs);
//				if (parentMatchStack.size() < 2)
//					System.out.println("Found matching " + subAnnots[a].getType() + " in " + (System.currentTimeMillis() - start) + "ms");
				
				//	double-check fuzzy match by reverse matching
				if (mRefSubAnnot != null) {
					QueriableAnnotation mSubAnnot;
//					start = System.currentTimeMillis();
					if (subAnnotsByType.containsKey(subAnnots[a].getType()))
						mSubAnnot = this.findMatchingAnnot((parentMatchStack.size() < 1), mRefSubAnnot, ((ArrayList) subAnnotsByType.get(mRefSubAnnot.getType())), refAnnot.getType(), doneAnnotIDs);
					else mSubAnnot = this.findMatchingAnnot((parentMatchStack.size() < 1), mRefSubAnnot, annot.getAnnotations(mRefSubAnnot.getType()), refAnnot.getType(), doneAnnotIDs);
//					if (parentMatchStack.size() < 2)
//						System.out.println("Reverse lookup done in " + (System.currentTimeMillis() - start) + "ms");
					
					//	reverse match contradiction, save lookup result for later
					if ((mSubAnnot == null) || !mSubAnnot.getAnnotationID().equals(subAnnots[a].getAnnotationID())) {
						if (DEBUG_MATCHING) System.out.println(" ==> reverse lookup mismatch: " + ((mSubAnnot == null) ? "not found" : mSubAnnot.toXML()));
						
						//	mark all nested annotations as additions ...
						for (int la = a; la < subAnnots.length; la++) {
							if (subAnnots[la].getStartIndex() >= subAnnots[a].getEndIndex())
								break;
							annotMatches.add(new AnnotationMatch(parentMatches, null, subAnnots[la]));
							doneAnnotIDs.add(subAnnots[la].getAnnotationID());
						}
						
						//	... and jump to next annotation
						lastAnnotEnd = subAnnots[a].getEndIndex();
						continue;
					}
				}
			}
			
			//	no match found, must have been added
			if (mRefSubAnnot == null) {
				annotMatches.add(new AnnotationMatch(parentMatches, null, subAnnots[a]));
				doneAnnotIDs.add(subAnnots[a].getAnnotationID());
			}
			
			//	we have a match, check attributes and match recursively
			else {
//				start = System.currentTimeMillis();
				refSubAnnotsByIDs.remove(mRefSubAnnot.getAnnotationID());
				
				AnnotationMatch annotMatch = new AnnotationMatch(parentMatches, mRefSubAnnot, subAnnots[a]);
				annotMatches.add(annotMatch);
				
				parentMatchStack.addLast(annotMatch);
				treeMatch(parentMatchStack, subAnnots[a], doneAnnotIDs, mRefSubAnnot, doneRefAnnotIDs, true, annotMatches, idMatchThreshold);
				parentMatchStack.removeLast();
				
				lastAnnotEnd = subAnnots[a].getEndIndex();
//				if (parentMatchStack.size() < 1)
//					System.out.println("Recursion returned in " + (System.currentTimeMillis() - start) + "ms from " + AnnotationUtils.produceStartTag(subAnnots[a], true));
			}
		}
		
		//	add deletions in context of parent path
//		start = System.currentTimeMillis();
		for (Iterator aidit = refSubAnnotsByIDs.keySet().iterator(); aidit.hasNext();) {
			String oldSubAnnotId = ((String) aidit.next());
			if (!doneRefAnnotIDs.add(oldSubAnnotId))
				continue;
			QueriableAnnotation oldSubAnnot = ((QueriableAnnotation) refSubAnnotsByIDs.get(oldSubAnnotId));
			annotMatches.add(new AnnotationMatch(parentMatches, oldSubAnnot, null));
		}
//		if (parentMatchStack.size() < 2)
//			System.out.println("Deletions added in " + (System.currentTimeMillis() - start) + "ms");
	}
	
	/**
	 * Require or waive attribute equality for matching for two annotations
	 * of a given type in the context of parent annotations of another given
	 * type. This default implementation simply returns false. Sub classes are
	 * welcome to overwrite it and require attribute equality either in general
	 * or for specific annotation types and contexts.
	 * @param type the annotation type
	 * @param contextType the context annotation type
	 * @return true if attribute equality is required for a match
	 */
	protected boolean requireEqualAttributes(String type, String contextType) {
		return false;
	}
	
	private QueriableAnnotation findMatchingAnnot(boolean log, QueriableAnnotation lookupAnnot, QueriableAnnotation[] targetAnnots, String contextType, HashSet doneTargetAnnotIDs) {
		if (DEBUG_MATCHING) System.out.println("Seeking (1) match for " + lookupAnnot.toXML());
		boolean requireAttributes = this.requireEqualAttributes(lookupAnnot.getType(), contextType);
		
		//	filter out previously used reference annotations
//		long start = System.currentTimeMillis();
		boolean[] useTargetAnnot = new boolean[targetAnnots.length];
		Arrays.fill(useTargetAnnot, true);
		int filterTargetAnnotCount = 0;
		for (int ta = 0; ta < targetAnnots.length; ta++)
			if (doneTargetAnnotIDs.contains(targetAnnots[ta].getAnnotationID()) || (requireAttributes && !AttributeUtils.hasEqualAttributes(targetAnnots[ta], lookupAnnot))) {
				filterTargetAnnotCount++;
				useTargetAnnot[ta] = false;
			}
		if (filterTargetAnnotCount == targetAnnots.length)
			return null; // nothing left to work with
		if (filterTargetAnnotCount != 0) {
			QueriableAnnotation[] freeTargetAnnots = new QueriableAnnotation[targetAnnots.length - filterTargetAnnotCount];
			for (int ta = 0, fra = 0; ta < targetAnnots.length; ta++) {
				if (useTargetAnnot[ta])
					freeTargetAnnots[fra++] = targetAnnots[ta];
			}
			targetAnnots = freeTargetAnnots;
		}
//		if (log) System.out.println("Sub annotations filtered (1) in " + (System.currentTimeMillis() - start) + "ms");
		
		//	go find match
		return this.findMatchingAnnotation(lookupAnnot, targetAnnots, contextType, this.getMaxTokenMatchAttempts(lookupAnnot.getType(), contextType));
	}
	
	private QueriableAnnotation findMatchingAnnot(boolean log, QueriableAnnotation lookupAnnot, ArrayList targetAnnots, String contextType, HashSet doneTargetAnnotIDs) {
		if (DEBUG_MATCHING) System.out.println("Seeking (2) match for " + lookupAnnot.toXML());
		ArrayList attributeMatchTargetAnnots = (this.requireEqualAttributes(lookupAnnot.getType(), contextType) ? new ArrayList() : null);
		
		//	filter out previously used reference annotations
//		long start = System.currentTimeMillis();
		for (int ta = 0; ta < targetAnnots.size(); ta++) {
			QueriableAnnotation targetAnnot = ((QueriableAnnotation) targetAnnots.get(ta));
			if (doneTargetAnnotIDs.contains(targetAnnot.getAnnotationID())) {
				targetAnnots.remove(ta--); // clean up, might have been matched by ID, etc.
				continue;
			}
			if (attributeMatchTargetAnnots == null)
				continue;
			if (AttributeUtils.hasEqualAttributes(targetAnnot, lookupAnnot))
				attributeMatchTargetAnnots.add(targetAnnot);
		}
		if (attributeMatchTargetAnnots != null) {
			if (attributeMatchTargetAnnots.isEmpty())
				return null; // nothing left to work with
			else targetAnnots = attributeMatchTargetAnnots;
		}
//		if (log) System.out.println("Sub annotations filtered (2) in " + (System.currentTimeMillis() - start) + "ms");
		
		//	go find match
		return this.findMatchingAnnotation(lookupAnnot, ((QueriableAnnotation[]) targetAnnots.toArray(new QueriableAnnotation[targetAnnots.size()])), contextType, this.getMaxTokenMatchAttempts(lookupAnnot.getType(), contextType));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenNormalizer#normalize(java.lang.String)
	 */
	public String normalize(String token) {
		if (token == null)
			return token;
		if (this.ignorePunctuation && StringUtils.isPunctuation(token))
			return null;
		if (this.ignoreAccents)
			token = StringUtils.normalizeString(token);
		return (this.ignoreCase ? token.toLowerCase() : token);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenMismatchScorer#getScore(de.uka.ipd.idaho.gamta.TokenSequenceUtils.TokenMismatch[], de.uka.ipd.idaho.gamta.TokenSequence, de.uka.ipd.idaho.gamta.TokenSequence, boolean)
	 */
	public TokenMismatchScore getScore(TokenMismatch[] mismatches, TokenSequence tokens, TokenSequence reference, boolean isBagMatch) {
		return TokenSequenceUtils.DEFAULT_SEQUENCE_MISMATCH_SCORER.getScore(mismatches, tokens, reference, isBagMatch);
	}
	
	/**
	 * Provide a threshold for seeking a token based match to an annotation of
	 * a given type in the context of parent annotations of another given type.
	 * This threshold pertains to the number of eligible same-type reference
	 * annotations to test before giving up on a specific matching method. If
	 * the mutual order of matching sibling annotations can be expected to be
	 * relatively similar in the two compared documents, using a low threshold
	 * will speed up matching. However, if sibling order is subject to change,
	 * a low threshold may prevent the actual best match from being found. This
	 * default implementation returns -1, indicating no threshold and a full
	 * list match-up. Sub classes are welcome to overwrite it and provide some
	 * application, type, and context specific threshold to speed up matching.
	 * @param type the annotation type
	 * @param contextType the context annotation type
	 * @return the maximum number of annotations to check on fuzzy matching
	 */
	protected int getMaxTokenMatchAttempts(String type, String contextType) {
		return -1;
	}
	
	/**
	 * Find a matching reference annotation for a given annotation. This
	 * default implementation seeks, in this order, a full sequence match, a
	 * full bag match (if allowed by <code>allowBagMatch()</code>), a fuzzy
	 * sequence match (if <code>getSequenceFuzzyMatchThreshold()</code> returns
	 * a value less than 1), and a fuzzy bag match (if
	 * <code>getBagFuzzyMatchThreshold()</code> returns a value less than 1).
	 * Sub classes are welcome to implement their own matching strategy, or to
	 * further scrutinize matches.
	 * @param annot the annotation to find a match for
	 * @param refAnnots the reference annotations to find the match in
	 * @param contextType the type of the context annotation
	 * @param maxMatchAttempts the maximum number of reference annotations to
	 *            try a match on
	 * @return a matching reference annotation or null
	 * @see de.uka.ipd.idaho.gamta.util.AnnotationTreeMatcher#getMaxTokenMatchAttempts(String, String)
	 * @see de.uka.ipd.idaho.gamta.util.AnnotationTreeMatcher#allowBagMatch(String, String)
	 * @see de.uka.ipd.idaho.gamta.util.AnnotationTreeMatcher#getSequenceFuzzyMatchThreshold(String, String)
	 * @see de.uka.ipd.idaho.gamta.util.AnnotationTreeMatcher#getBagFuzzyMatchThreshold(String, String)
	 */
	protected QueriableAnnotation findMatchingAnnotation(QueriableAnnotation annot, QueriableAnnotation[] refAnnots, String contextType, int maxMatchAttempts) {
		
		//	normalize cutoff (faster than checking for positive time and again)
		if (maxMatchAttempts <= 0)
			maxMatchAttempts = Integer.MAX_VALUE;
		if (DEBUG_MATCHING) System.out.println(" - seeking through " + refAnnots.length + " reference annotations, " + maxMatchAttempts + " at most");
		
		//	try and find full match
		for (int ra = 0; ra < refAnnots.length; ra++) {
			if (maxMatchAttempts < ra)
				break;
			if (TokenSequenceUtils.sequenceEquals(annot, refAnnots[ra], this)) {
				if (DEBUG_MATCHING) System.out.println(" ==> found equal match " + refAnnots[ra].toXML());
				return refAnnots[ra];
			}
		}
		
		//	try and find token bag match
		if (this.allowBagMatch(annot.getType(), contextType))
			for (int ra = 0; ra < refAnnots.length; ra++) {
				if (maxMatchAttempts < ra)
					break;
				if (TokenSequenceUtils.bagEquals(annot, refAnnots[ra], this)) {
					if (DEBUG_MATCHING) System.out.println(" ==> found bag equal match " + refAnnots[ra].toXML());
					return refAnnots[ra];
				}
			}
		
		//	try and find token Levenshtein match
		float sequenceMatchThreshold = this.getSequenceFuzzyMatchThreshold(annot.getType(), contextType);
		if (sequenceMatchThreshold < 1) {
			QueriableAnnotation bestMatch = null;
			float bestMatchScore = 0;
			for (int ra = 0; ra < refAnnots.length; ra++) {
				if (maxMatchAttempts < ra)
					break;
				float matchScore = TokenSequenceUtils.getSequenceSimilarity(annot, refAnnots[ra], sequenceMatchThreshold, this, this);
				if (matchScore < sequenceMatchThreshold)
					continue; // just too low
				if (matchScore > bestMatchScore) {
					bestMatch = refAnnots[ra];
					bestMatchScore = matchScore;
				}
			}
			if (bestMatch != null) {
				if (DEBUG_MATCHING) System.out.println(" ==> found sequence-similar (" + bestMatchScore + ") match " + bestMatch.toXML());
				return bestMatch;
			}
		}
		
		//	try and find token bag fuzzy match
		float bagMatchThreshold = this.getBagFuzzyMatchThreshold(annot.getType(), contextType);
		if (bagMatchThreshold < 1) {
			if (DEBUG_MATCHING) System.out.println(" - seeking bag-similar match at least " + bagMatchThreshold + " similar");
			QueriableAnnotation bestMatch = null;
			float bestMatchScore = 0;
			for (int ra = 0; ra < refAnnots.length; ra++) {
				if (maxMatchAttempts < ra)
					break;
				float matchScore = TokenSequenceUtils.getBagSimilarity(annot, refAnnots[ra], this, this);
				if (matchScore < bagMatchThreshold)
					continue; // just too low
				if (matchScore > bestMatchScore) {
					bestMatch = refAnnots[ra];
					bestMatchScore = matchScore;
				}
			}
			if (bestMatch != null) {
				if (DEBUG_MATCHING) System.out.println(" ==> found bag-similar (" + bestMatchScore + ") match " + bestMatch.toXML());
				return bestMatch;
			}
		}
		
		//	nothing found
		if (DEBUG_MATCHING) System.out.println(" ==> not found");
		return null;
	}
	
	/**
	 * Allow or disallow bag (order insensitive) matching for two annotations
	 * of a given type in the context of parent annotations of another given
	 * type. This default implementation simply returns false. Sub classes are
	 * welcome to overwrite it and enable bag matching either in general or for
	 * specific annotation types and contexts.
	 * @param type the annotation type
	 * @param contextType the context annotation type
	 * @return true if bag matching is allowed
	 */
	protected boolean allowBagMatch(String type, String contextType) {
		return false;
	}
	
	/**
	 * Get the minimum sequence (order sensitive) similarity for two
	 * annotations of a given type to match up in the context of parent
	 * annotations of another given type. To disallow sequence fuzzy matching
	 * altogether, simply return a value > 1. This default implementation does
	 * exactly the latter. Sub classes are welcome to overwrite it and enable
	 * sequence fuzzy matching either in general or for specific annotation
	 * types and contexts. Note that zero or negative return values will
	 * indicate a general match.
	 * @param type the annotation type
	 * @param contextType the context annotation type
	 * @return the minimum similarity for a match
	 */
	protected float getSequenceFuzzyMatchThreshold(String type, String contextType) {
		return Float.MAX_VALUE;
	}
	
	/**
	 * Get the minimum bag (order insensitive) similarity for two annotations
	 * of a given type to match up in the context of parent annotations of
	 * another given type. To disallow bag matching altogether, simply return
	 * a value > 1. This default implementation does exactly the latter. Sub
	 * classes are welcome to overwrite it and enable bag fuzzy matching either
	 * in general or for specific annotation types and contexts. Note that zero
	 * or negative return values will indicate a general match.
	 * @param type the annotation type
	 * @param contextType the context annotation type
	 * @return the minimum similarity for a match
	 */
	protected float getBagFuzzyMatchThreshold(String type, String contextType) {
		return Float.MAX_VALUE;
	}
	
	/**
	 * Get the minimum sequence (order sensitive) similarity for two
	 * annotations of a given type with equal identifiers to be considered a
	 * match in the context of parent annotations of another given type.
	 * To disallow ID based matching altogether, simply return a value > 1; to
	 * use ID based matches without any further scrutiny, return a value < 0.
	 * Any value in the 0 to 1 range will be used as a threshold for a sequence
	 * match verification of the ID based match. This default implementation
	 * returns a value below 0 to indicate general trust in ID based matches.
	 * @param type the annotation type
	 * @param contextType the context annotation type
	 * @return the minimum similarity for an ID based match to be used
	 */
	protected float getIdMatchVerificationThreshold(String type, String contextType) {
		return 0;
	}
	
	/**
	 * Produce a tree match for two documents or annotations.
	 * @param annotation the annotation to match
	 * @param refAnnotation the reference annotation to match against
	 * @return a tree match for the argument documents or annotations
	 */
	public AnnotationMatch[] getAnnotationMatch(QueriableAnnotation annotation, QueriableAnnotation refAnnotation) {
		return this.getAnnotationMatch(annotation, refAnnotation, Float.NaN);
	}
	
	/**
	 * Produce a tree match for two documents or annotations.
	 * @param annotation the annotation to match
	 * @param refAnnotation the reference annotation to match against
	 * @param idMatchThreshold the minimum sequence similarity for an ID based
	 *            annotation match; specify <code>Float.NaN</code> to use the
	 *            implemented (type dependent) defaults
	 * @return a tree match for the argument documents or annotations
	 */
	public AnnotationMatch[] getAnnotationMatch(QueriableAnnotation annotation, QueriableAnnotation refAnnotation, float idMatchThreshold) {
		
		//	match up new and old annotations top-down
		long start = System.currentTimeMillis();
		ArrayList annotMatches = new ArrayList();
		this.treeMatch(new LinkedList(), annotation, new HashSet(), refAnnotation, new HashSet(), false, annotMatches, idMatchThreshold);
		System.out.println("Primary tree match computed in " + (System.currentTimeMillis() - start) + "ms");
		
		//	index additions by IDs ...
		start = System.currentTimeMillis();
		HashMap additionsByAnnotIDs = new HashMap();
		for (int m = 0; m < annotMatches.size(); m++) {
			AnnotationMatch addition = ((AnnotationMatch) annotMatches.get(m));
			if (addition.refAnnotation != null) // not an addition
				continue;
			additionsByAnnotIDs.put(addition.annotation.getAnnotationID(), addition);
			annotMatches.remove(m--);
		}
		System.out.println("Additions indexed in " + (System.currentTimeMillis() - start) + "ms");
		
		//	... match up with removals (can miss out on one another due to parent annotations expanding as a result of invalid nesting)
		start = System.currentTimeMillis();
		for (int m = 0; m < annotMatches.size(); m++) {
			AnnotationMatch removal = ((AnnotationMatch) annotMatches.get(m));
			if (removal.annotation != null) // not a removal
				continue;
			AnnotationMatch addition = ((AnnotationMatch) additionsByAnnotIDs.remove(removal.refAnnotation.getAnnotationID()));
			if (addition == null)
				continue;
			
			//	find shared parent matches
			ArrayList goodParentMatches = new ArrayList();
			for (int p = 0; (p < removal.parentMatches.length) && (p < addition.parentMatches.length); p++) {
				if (removal.parentMatches[p] == addition.parentMatches[p])
					goodParentMatches.add(removal.parentMatches[p]);
				else break;
			}
			AnnotationMatch[] parentMatches = ((AnnotationMatch[]) goodParentMatches.toArray(new AnnotationMatch[goodParentMatches.size()]));
			
			//	record any broken parents in reference version
			AnnotationMatch[] brokenRefParents;
			if (parentMatches.length < removal.parentMatches.length) {
				brokenRefParents = new AnnotationMatch[removal.parentMatches.length - parentMatches.length];
				System.arraycopy(removal.parentMatches, parentMatches.length, brokenRefParents, 0, brokenRefParents.length);
			}
			else brokenRefParents = null;
			
			//	record any broken parents in new version
			AnnotationMatch[] brokenParents = null;
			if (parentMatches.length < addition.parentMatches.length) {
				brokenParents = new AnnotationMatch[addition.parentMatches.length - parentMatches.length];
				System.arraycopy(addition.parentMatches, parentMatches.length, brokenParents, 0, brokenParents.length);
			}
			else brokenParents = null;
			
			//	make (ostensibly removed) reference version relative to changed parent if required
			QueriableAnnotation refAnnot = null;
			if (parentMatches.length >= removal.parentMatches.length)
				refAnnot = removal.refAnnotation;
			else if (parentMatches.length == 0)
				refAnnot = makeRelativeTo(removal.refAnnotation, refAnnotation);
			else refAnnot = makeRelativeTo(removal.refAnnotation, parentMatches[parentMatches.length-1].refAnnotation);
			
			//	make (ostensibly added) new version relative to changed parent if required
			QueriableAnnotation annot = null;
			if (parentMatches.length >= addition.parentMatches.length)
				annot = addition.annotation;
			else if (parentMatches.length == 0)
				annot = makeRelativeTo(addition.annotation, annotation);
			else annot = makeRelativeTo(addition.annotation, parentMatches[parentMatches.length-1].annotation);
			
			//	replace removal with recovered annotation match
			annotMatches.set(m, new AnnotationMatch(parentMatches, refAnnot, annot));
			
			//	log underlying problem(s)
			if (DEBUG_MATCHING) {
				System.out.println("Compensated addition and removal of " + refAnnot.getType() + " " + refAnnot.getAnnotationID());
				if (brokenRefParents != null) {
					System.out.println("  Broken parents in old version:");
					for (int p = 0; p < brokenRefParents.length; p++) {
						System.out.println("    BROKEN: " + brokenRefParents[p].refAnnotation.toXML());
						System.out.println("       new: " + brokenRefParents[p].annotation.toXML());
					}
				}
				if (brokenParents != null) {
					System.out.println("  Broken parents in new version:");
					for (int p = 0; p < brokenParents.length; p++) {
						System.out.println("    BROKEN: " + brokenParents[p].annotation.toXML());
						System.out.println("       old: " + brokenParents[p].refAnnotation.toXML());
					}
				}
			}
		}
		System.out.println("Additions compensated in " + (System.currentTimeMillis() - start) + "ms");
		
		//	re-add unmatched additions
//		annotMatches.addAll(additionsByAnnotIDs.values());
		for (Iterator aidit = additionsByAnnotIDs.keySet().iterator(); aidit.hasNext();) {
			AnnotationMatch addition = ((AnnotationMatch) additionsByAnnotIDs.get(aidit.next()));
			mergeAddition(annotMatches, addition);
		}
		
		//	sort and return matches
//		Collections.sort(annotMatches);
		return ((AnnotationMatch[]) annotMatches.toArray(new AnnotationMatch[annotMatches.size()]));
	}
	
	private static QueriableAnnotation makeRelativeTo(QueriableAnnotation annot, QueriableAnnotation newParent) {
		QueriableAnnotation[] potNewAnnots = newParent.getAnnotations(annot.getType());
		for (int a = 0; a < potNewAnnots.length; a++) {
			if (potNewAnnots[a].getAnnotationID().equals(annot.getAnnotationID()))
				return potNewAnnots[a];
		}
		return null;
	}
	
	private static void mergeAddition(ArrayList annotMatches, AnnotationMatch addition) {
		if (annotMatches.isEmpty()) {
			annotMatches.add(addition);
			return;
		}
		
		int seekStart = (annotMatches.size() / 2);
		if (annotMatches.size() >= 32)
			for (int step = (annotMatches.size() / 4); step > 7; step /= 2) {
				AnnotationMatch annotMatch = ((AnnotationMatch) annotMatches.get(seekStart));
				int comp = compareAddition(addition, annotMatch);
				if (comp < 0)
					seekStart -= step;
				else if (comp > 0)
					seekStart += step;
				else break;
			}
		
		AnnotationMatch annotMatch = ((AnnotationMatch) annotMatches.get(seekStart));
		int comp = compareAddition(addition, annotMatch);
		if (comp < 0) {
			for (int s = (seekStart - 1); s >= 0; s--) {
				annotMatch = ((AnnotationMatch) annotMatches.get(s));
				comp = compareAddition(addition, annotMatch);
				if (comp < 0)
					continue;
				annotMatches.add((s + 1), addition); // insert addition right after first match it is greater than
				return;
			}
			annotMatches.add(0, addition); // addition is smallest match, insert at start
		}
		else if (comp > 0) {
			for (int s = (seekStart + 1); s < annotMatches.size(); s++) {
				annotMatch = ((AnnotationMatch) annotMatches.get(s));
				comp = compareAddition(addition, annotMatch);
				if (comp > 0)
					continue;
				annotMatches.add(s, addition); // insert addition right on first match it is smaller than, pushing out the latter
				return;
			}
			annotMatches.add(addition); // addition is largest match, insert at end
		}
		else annotMatches.add(seekStart, addition); // should not normally occur, but better to cover all our bases
	}
	
	private static int compareAddition(AnnotationMatch addition, AnnotationMatch annotMatch) {
		for (int ap = 0, mp = 0; (ap < addition.parentMatches.length) && (mp < annotMatch.parentMatches.length);) {
			if (addition.parentMatches[ap] == annotMatch.parentMatches[mp]) {
				ap++;
				mp++;
			}
			else return AnnotationUtils.compare(addition.parentMatches[ap].annotation, annotMatch.parentMatches[mp].annotation); // compare first differing parent
		}
		
		if (addition.parentMatches.length < annotMatch.parentMatches.length)
			return AnnotationUtils.compare(addition.annotation, annotMatch.parentMatches[addition.parentMatches.length].annotation);
		if (annotMatch.parentMatches.length < addition.parentMatches.length) {
			if (annotMatch.annotation == null)
				return 1; // keep removals before additions
			else return AnnotationUtils.compare(addition.parentMatches[annotMatch.parentMatches.length].annotation, annotMatch.annotation);
		}
		
		if (annotMatch.annotation == null)
			return 1; // keep removals before additions
		else return AnnotationUtils.compare(addition.annotation, annotMatch.annotation); // compare additions to updates
	}
}
