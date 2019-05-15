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
 *     * Neither the name of the Universität Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITÄT KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import de.uka.ipd.idaho.easyIO.util.RandomByteSource;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;

/**
 * Utility class computing the MD5 checksum of an annotation.
 * 
 * @author sautter
 */
public class AnnotationChecksumDigest {
	
	private LinkedHashSet typeFilters = null;
	private LinkedHashSet attributeFilters = null;
	
	/**
	 * Type filters allow for filtering annotations of specific types out of
	 * checksum computation.
	 * 
	 * @author sautter
	 */
	public static interface TypeFilter {
		
		/**
		 * Specify whether or not to filter annotations of a given type out of
		 * checksum computation.
		 * @param annotationType the annotation type to assess
		 * @return true if the argument annotation type should be filtered out
		 */
		public abstract boolean filterType(String annotationType);
	}
	
	/**
	 * Set base implementation of an annotation type filter, implementing
	 * <code>filterType()</code> as the inverse of <code>contains()</code> in
	 * the wrapped Set.
	 * 
	 * @author sautter
	 */
	public static class SetTypeFilter implements TypeFilter {
		private Set typeSet;
		
		/**
		 * @param typeSet
		 */
		public SetTypeFilter(Set typeSet) {
			this.typeSet = typeSet;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.AnnotationChecksumDigest.TypeFilter#filterType(java.lang.String)
		 */
		public boolean filterType(String annotationType) {
			return !this.typeSet.contains(annotationType);
		}
	}
	
	/**
	 * Add an annotation type filter to exclude annotations of specific types
	 * from checksum computation.
	 * @param tf the type filter to add
	 */
	public void addTypeFilter(TypeFilter tf) {
		if (tf == null)
			return;
		if (this.typeFilters == null)
			this.typeFilters = new LinkedHashSet();
		this.typeFilters.add(tf);
	}

	/**
	 * Remove a type filter.
	 * @param tf the type filters to remove
	 */
	public void removeTypeFilter(TypeFilter tf) {
		if (tf == null)
			return;
		if (this.typeFilters == null)
			return;
		this.typeFilters.remove(tf);
		if (this.typeFilters.isEmpty())
			this.typeFilters = null;
	}
	
	/**
	 * Attribute filters allow for filtering specific annotation attributes out
	 * of checksum computation.
	 * 
	 * @author sautter
	 */
	public static interface AttributeFilter {
		
		/**
		 * Specify whether or not to filter annotation attributes with a given
		 * name out of checksum computation.
		 * @param attributeName the annotation attribute name to assess
		 * @return true if the argument attribute should be filtered out
		 */
		public abstract boolean filterAttribute(String attributeName);
	}
	
	/**
	 * Set base implementation of an attribute filter, implementing
	 * <code>filterAttribute()</code> as the inverse of <code>contains()</code>
	 * in the wrapped Set.
	 * 
	 * @author sautter
	 */
	public static class SetAttributeFilter implements AttributeFilter {
		private Set nameSet;
		
		/**
		 * @param nameSet
		 */
		public SetAttributeFilter(Set nameSet) {
			this.nameSet = nameSet;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.AnnotationChecksumDigest.AttributeFilter#filterAttribute(java.lang.String)
		 */
		public boolean filterAttribute(String attributeName) {
			return !this.nameSet.contains(attributeName);
		}
	}
	
	/**
	 * Add an annotation type filter to exclude annotations of specific types
	 * from checksum computation.
	 * @param af the type filter to add
	 */
	public void addAttributeFilter(AttributeFilter af) {
		if (af == null)
			return;
		if (this.attributeFilters == null)
			this.attributeFilters = new LinkedHashSet();
		this.attributeFilters.add(af);
	}

	/**
	 * Remove a type filter.
	 * @param af the type filters to remove
	 */
	public void removeAttributeFilter(AttributeFilter af) {
		if (af == null)
			return;
		if (this.attributeFilters == null)
			return;
		this.attributeFilters.remove(af);
		if (this.attributeFilters.isEmpty())
			this.attributeFilters = null;
	}
	
	/**
	 * Compute the MD5 checksum of a queriable annotation.
	 * @param annotation the queriable annotation to compute the MD5 for
	 * @return the MD5 checksum as a hex string
	 * @throws IOException
	 */
	public String computeChecksum(QueriableAnnotation annotation) throws IOException {
		return this.computeChecksum(annotation, null, null);
	}
	
	/**
	 * Compute the MD5 checksum of a queriable annotation.
	 * @param annotation the queriable annotation to compute the MD5 for
	 * @param typeFilter an annotation type filter to use on top of the ones
	 *            permanently added to the digest
	 * @param attributeFilter an attribute filter to use on top of the ones
	 *            permanently added to the digest
	 * @return the MD5 checksum as a hex string
	 * @throws IOException
	 */
	public String computeChecksum(QueriableAnnotation annotation, final TypeFilter typeFilter, final AttributeFilter attributeFilter) throws IOException {
		Set aisTypeFilter = (((this.typeFilters == null) && (typeFilter == null)) ? null : new TypeFilterSet(typeFilter, this.typeFilters));
		Set aisAttributeFilter = (((this.attributeFilters == null) && (attributeFilter == null)) ? null : new AttributeFilterSet(attributeFilter, this.attributeFilters));
		AnnotationInputStream ais = new AnnotationInputStream(annotation, "UTF-8", aisTypeFilter, aisAttributeFilter);
		MessageDigest checksumDigest = getChecksumDigest();
		byte[] buffer = new byte[1024];
		for (int read; (read = ais.read(buffer)) != -1;)
			checksumDigest.update(buffer, 0, read);
		ais.close();
		byte[] checksumBytes = checksumDigest.digest();
		returnChecksumDigest(checksumDigest);
		return new String(RandomByteSource.getHexCode(checksumBytes));
	}
	
	private static LinkedList checksumDigestPool = new LinkedList();
	private static synchronized MessageDigest getChecksumDigest() {
		if (checksumDigestPool.size() != 0) {
			MessageDigest dataHash = ((MessageDigest) checksumDigestPool.removeFirst());
			dataHash.reset();
			return dataHash;
		}
		try {
			MessageDigest dataHash = MessageDigest.getInstance("MD5");
			dataHash.reset();
			return dataHash;
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println(nsae.getClass().getName() + " (" + nsae.getMessage() + ") while creating checksum digester.");
			nsae.printStackTrace(System.out); // should not happen, but Java don't know ...
			return null;
		}
	}
	private static synchronized void returnChecksumDigest(MessageDigest dataHash) {
		checksumDigestPool.addLast(dataHash);
	}
	
	private static class TypeFilterSet extends HashSet {
		private ChainTypeFilter typeFilter;
		TypeFilterSet(TypeFilter typeFilter, Set typeFilters) {
			if (typeFilter == null)
				this.typeFilter = new ChainTypeFilter(typeFilter);
			if (typeFilters != null)
				for (Iterator tfit = typeFilters.iterator(); tfit.hasNext();) {
					if (this.typeFilter == null)
						this.typeFilter = new ChainTypeFilter((TypeFilter) tfit.next());
					else this.typeFilter.appendTypeFilter((TypeFilter) tfit.next());
				}
		}
		public boolean contains(Object obj) {
			return ((obj != null) && !this.typeFilter.filterType(obj.toString()));
		}
		
		/* Self-extending chain of filters should be faster than setting up a
		 * loop over an array on each lookup due to the exclusive use of fixed
		 * references. Stack depth should not be a problem, either, with the
		 * small number of filters (maybe 3 to 5 at most) to be expected in
		 * practical use. */
		private static class ChainTypeFilter implements TypeFilter {
			private TypeFilter typeFilter;
			private ChainTypeFilter next;
			ChainTypeFilter(TypeFilter tf) {
				this.typeFilter = tf;
			}
			void appendTypeFilter(TypeFilter tf) {
				if (this.next == null)
					this.next = new ChainTypeFilter(tf);
				else this.next.appendTypeFilter(tf);
			}
			public boolean filterType(String annotationType) {
				return (this.typeFilter.filterType(annotationType) || ((this.next != null) && this.next.filterType(annotationType)));
			}
		}
	}
	
	private static class AttributeFilterSet extends HashSet {
		private ChainAttributeFilter attributeFilter;
		AttributeFilterSet(AttributeFilter attributeFilter, Set attributeFilters) {
			if (attributeFilter != null)
				this.attributeFilter = new ChainAttributeFilter(attributeFilter);
			if (attributeFilters != null)
				for (Iterator afit = attributeFilters.iterator(); afit.hasNext();) {
					if (this.attributeFilter == null)
						this.attributeFilter = new ChainAttributeFilter((AttributeFilter) afit.next());
					else this.attributeFilter.appendAttributeFilter((AttributeFilter) afit.next());
				}
		}
		public boolean contains(Object obj) {
			return ((obj != null) && !this.attributeFilter.filterAttribute(obj.toString()));
		}
		
		/* Self-extending chain of filters should be faster than setting up a
		 * loop over an array on each lookup due to the exclusive use of fixed
		 * references. Stack depth should not be a problem, either, with the
		 * small number of filters (maybe 3 to 5 at most) to be expected in
		 * practical use. */
		private static class ChainAttributeFilter implements AttributeFilter {
			private AttributeFilter attributeFilter;
			private ChainAttributeFilter next;
			ChainAttributeFilter(AttributeFilter attributeFilter) {
				this.attributeFilter = attributeFilter;
			}
			void appendAttributeFilter(AttributeFilter tf) {
				if (this.next == null)
					this.next = new ChainAttributeFilter(tf);
				else this.next.appendAttributeFilter(tf);
			}
			public boolean filterAttribute(String attributeName) {
				return (this.attributeFilter.filterAttribute(attributeName) || ((this.next != null) && this.next.filterAttribute(attributeName)));
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
