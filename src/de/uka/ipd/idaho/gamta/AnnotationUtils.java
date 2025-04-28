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


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Static class providing comparison functionality for Annotations. Since the
 * methods do not check which token sequence Annotations refer to (that may not
 * even be possible in the general case), comparing Annotations that refer to
 * different token sequences may result in strange behavior of functionality
 * relying on comparison results.
 * 
 * @author sautter
 */
public class AnnotationUtils {
	
	private static final String NCTypeRegEx = "([a-zA-Z\\_][a-zA-Z0-9\\_\\-\\.]*+)"; // regular expression matching XML NCNames
	private static final String ATypeRegEx = "(" + NCTypeRegEx + "(\\:" + NCTypeRegEx + ")?+)"; // regular expression matching XML QNames
	private static final Pattern ATypePattern = Pattern.compile(ATypeRegEx);
	
	/**
	 * Comparator ordering Annotations in terms of their position. This
	 * comparator does neither consider the Annotation type, nor the attributes.
	 * It merely encapsulates the compare() method provided by this class. This
	 * object may only be used with Arrays or collections containing objects
	 * implementing the Annotation interface. Using it with any other objects
	 * causes class cast exceptions.
	 */
	public static Comparator ANNOTATION_NESTING_ORDER = new Comparator() {
		public int compare(Object o1, Object o2) {
			return AnnotationUtils.compare(((Annotation) o1), ((Annotation) o2));
		}
	};
	
	/**
	 * Check whether an annotation type String is valid.
	 * @param type the annotation type String to test
	 * @return true if and only if the specified annotation type is valid (a
	 *         QName in the sense of XML)
	 */
	public static boolean isValidAnnotationType(String type) {
		return ATypePattern.matcher(type).matches();
	}
	
	/**
	 * Check if an Annotation overlaps with another Annotation.
	 * @param annotation1 the first Annotation to test
	 * @param annotation2 the second Annotation to test
	 * @return true is and only if annotation1 has at least one Token in common
	 *         with annotation2
	 */
	public static boolean overlaps(Annotation annotation1, Annotation annotation2) {
		return ((annotation1.getStartIndex() < annotation2.getEndIndex()) && (annotation2.getStartIndex() < annotation1.getEndIndex()));
	}
	
	/**
	 * Check if an Annotation's range totally covers another Annotation.
	 * @param annotation1 the first Annotation to test
	 * @param annotation2 the second Annotation to test
	 * @return true if and only if annotation1 spans all Tokens contained in
	 *         annotation2
	 */
	public static boolean contains(Annotation annotation1, Annotation annotation2) {
		return ((annotation1.getStartIndex() <= annotation2.getStartIndex()) && (annotation1.getEndIndex() >= annotation2.getEndIndex()));
	}
	
	/**
	 * Check if an Annotation totally lies in another Annotation's range.
	 * @param annotation1 the first Annotation to test
	 * @param annotation2 the second Annotation to test
	 * @return true is and only if all Tokens contained in annotation1 are also
	 *         contained in annotation2
	 */
	public static boolean liesIn(Annotation annotation1, Annotation annotation2) {
		return ((annotation1.getStartIndex() >= annotation2.getStartIndex()) && (annotation1.getEndIndex() <= annotation2.getEndIndex()));
	}
	
	/**
	 * Check if an Annotation starts with the Token directly following the last
	 * Token of another Annotation in the backing TokenSequence.
	 * @param annotation1 the first Annotation to test
	 * @param annotation2 the second Annotation to test
	 * @return true is and only if annotation1 starts with the Token directly
	 *         following the last Token of annotation2 in the backing
	 *         TokenSequence
	 */
	public static boolean follows(Annotation annotation1, Annotation annotation2) {
		return (annotation1.getStartIndex() == annotation2.getEndIndex());
	}
	
	/**
	 * Check if an Annotation ends with the Token directly preceding the first
	 * Token of another Annotation in the backing TokenSequence.
	 * @param annotation1 the first Annotation to test
	 * @param annotation2 the second Annotation to test
	 * @return true is and only if annotation1 ends with the Token directly
	 *         preceding the first Token of annotation2 in the backing
	 *         TokenSequence
	 */
	public static boolean precedes(Annotation annotation1, Annotation annotation2) {
		return (annotation1.getEndIndex() == annotation2.getStartIndex());
	}
	
	/**
	 * Check if an Annotation neighbors another Annotation, i.e. precedes or
	 * follows it.Note: This method is provided for convenience, the returned
	 * boolean is equal to<br>
	 * <code>(precedes(annotation1, annotation2) || follows(annotation1, annotation2))</code>
	 * @param annotation1 the first Annotation to test
	 * @param annotation2 the second Annotation to test
	 * @return true is and only if annotation1 neighbors annotation2, i.e.
	 *         precedes or follows it
	 */
	public static boolean neighbors(Annotation annotation1, Annotation annotation2) {
		return (precedes(annotation1, annotation2) || follows(annotation1, annotation2));
	}
	
	/**
	 * Compare two Annotations in terms of their position (note that this method
	 * does neither consider the Annotation type, nor the attributes).
	 * @param annotation1 the first Annotation
	 * @param annotation2 the second Annotation
	 * @return a value less than, equal to, or greater than zero, depending on
	 *         whether annotation1 is less than, equal to, or greater than
	 *         annotation2 in terms of position
	 */
	public static int compare(Annotation annotation1, Annotation annotation2) {
		int s1 = annotation1.getStartIndex();
		int s2 = annotation2.getStartIndex();
		if (s1 == s2)
			return (annotation2.size() - annotation1.size());
		else return (s1 - s2);
	}
	
	/**
	 * Check if two Annotations are equal, including their type being the same.
	 * @param annotation1 the first Annotation
	 * @param annotation2 the second Annotation
	 * @return true if and only if annotation1 and annotation2 have the same
	 *         start index, size, and type
	 */
	public static boolean equals(Annotation annotation1, Annotation annotation2) {
		return equals(annotation1, annotation2, true);
	}
	
	/**
	 * Check if two Annotations are equal.
	 * @param annotation1 the first Annotation
	 * @param annotation2 the second Annotation
	 * @param compareType compare the types of the Annotations?
	 * @return true if and only if annotation1 and annotation2 have the same
	 *         start index and size, and either compareType is false, or
	 *         annotation1 and annotation2 have the same type
	 */
	public static boolean equals(Annotation annotation1, Annotation annotation2, boolean compareType) {
		if (annotation1.getStartIndex() != annotation2.getStartIndex())
			return false;
		if (annotation1.size() != annotation2.size())
			return false;
		return (!compareType || annotation1.getType().equals(annotation2.getType()));
	}
	
	/**
	 * Sort an array of Annotations according to some nesting order.
	 * @param annotations the array of annotations to sort
	 * @param nestingOrder the nesting order to use, as a space-separated string
	 *            concatenation of annotation types, outmost type first
	 */
	public static void sort(Annotation[] annotations, String nestingOrder) {
		Arrays.sort(annotations, getComparator(nestingOrder));
	}
	
	/**
	 * Produce a comparator from a specific annotation type nesting order.
	 * @param nestingOrder the nesting order to use, as a space-separated string
	 *            concatenation of annotation types, outmost type first
	 * @return a comparator for the specified type nesting order (will assume
	 *         that arguments to compare() method are Strings)
	 */
	public static Comparator getTypeComparator(final String nestingOrder) {
		
		//	do cache lookup
		Comparator comparator = ((Comparator) typeComparatorCache.get(nestingOrder));
		if (comparator != null)
			return comparator;
		
		//	parse custom nesting order
		StringVector noParser = new StringVector();
		noParser.parseAndAddElements(nestingOrder, " ");
		noParser.removeAll("");
		
		//	parse default nesting order
		StringVector dnoParser = new StringVector();
		dnoParser.parseAndAddElements(DocumentRoot.DEFAULT_ANNOTATION_NESTING_ORDER, " ");
		dnoParser.removeAll("");
		
		//	fold nesting orders
		StringVector noBuilder = new StringVector();
		for (int t = 0; t < dnoParser.size(); t++) {
			String type = dnoParser.get(t);
			if (noParser.contains(type)) {
				while (!noParser.isEmpty() && !noParser.firstElement().equals(type))
					noBuilder.addElement(noParser.remove(0));
				noParser.removeAll(type);
			}
			noBuilder.addElement(type);
		}
		noBuilder.addContentIgnoreDuplicates(noParser);
		System.out.println("Nesting order is " + noBuilder.concatStrings(", "));
		
		//	index nesting order
		final HashMap noRanking = new HashMap();
		for (int t = 0; t < noBuilder.size(); t++) {
			String type = noBuilder.get(t).trim();
			if ((type.length() != 0) && !DocumentRoot.DOCUMENT_TYPE.equals(type))
				noRanking.put(type, new Integer(t));
		}
		
		//	create comparator
		comparator = new Comparator() {
			public int compare(Object type1, Object type2) {
				
				//	keep documents outside every other Annotation, regardless of configuration
				if (DocumentRoot.DOCUMENT_TYPE.equals(type1))
					return (DocumentRoot.DOCUMENT_TYPE.equals(type2) ? 0 : -1);
				if (DocumentRoot.DOCUMENT_TYPE.equals(type2))
					return 1;
				
				//	get ordering numbers
				Integer i1 = ((Integer) noRanking.get(type1));
				Integer i2 = ((Integer) noRanking.get(type2));
				
				//	both specified, compare
				if ((i1 != null) && (i2 != null))
					return i1.compareTo(i2);
				
				//	second type not specified, assume it to be some detail
				else if (i1 != null)
					return -1;
				
				//	first type not specified, assume it to be some detail
				else if (i2 != null)
					return 1;
				
				//	none of the types specified
				else return 0;
			}
			public String toString() {
				return nestingOrder;
			}
			public boolean equals(Object obj) {
				return ((obj != null) && nestingOrder.equals(obj.toString()));
			}
			public int hashCode() {
				return nestingOrder.hashCode();
			}
		};
		
		//	cache comparator
		typeComparatorCache.put(nestingOrder, comparator);
		
		//	return comparator
		return comparator;
	}
	private static Map typeComparatorCache = Collections.synchronizedMap(new HashMap());
	
	/**
	 * Produce a comparator from a specific annotation nesting order.
	 * @param nestingOrder the nesting order to use, as a space-separated string
	 *            concatenation of annotation types, outmost type first
	 * @return a comparator for the specified type nesting order (will assume
	 *         that arguments to compare() method are Annotations)
	 */
	public static Comparator getComparator(final String nestingOrder) {
		
		//	do cache lookup
		Comparator comparator = ((Comparator) comparatorCache.get(nestingOrder));
		if (comparator != null)
			return comparator;
		
		//	get type comparator
		final Comparator typeComparator = getTypeComparator(nestingOrder);
		
		//	create comparator
		comparator = new Comparator() {
			public int compare(Object annotation1, Object annotation2) {
				int c = AnnotationUtils.compare(((Annotation) annotation1), ((Annotation) annotation2));
				return ((c == 0) ? typeComparator.compare(((Annotation) annotation1).getType(), ((Annotation) annotation2).getType()) : c);
			}
			public String toString() {
				return nestingOrder;
			}
			public boolean equals(Object obj) {
				return ((obj != null) && nestingOrder.equals(obj.toString()));
			}
			public int hashCode() {
				return nestingOrder.hashCode();
			}
		};
		
		//	cache comparator
		comparatorCache.put(nestingOrder, comparator);
		
		//	return comparator
		return comparator;
	}
	private static Map comparatorCache = Collections.synchronizedMap(new HashMap());
	
	/**
	 * Clean XML namespace declarations of a document and the annotations
	 * contained in it. In particular, this method removes attributes declaring
	 * XML namespaces that are not used in any annotation type or attribute
	 * name anywhere in the document.
	 * @param doc the document whose namespace declarations to clean
	 * @return true if the argument document was modified as a result of this
	 *            method, false otherwise
	 */
	public static boolean cleanNamespaceDeclarations(QueriableAnnotation doc) {
		
		//	collect XML namespace declarations actually used in document ...
		HashSet docXmlns = new HashSet();
		
		//	... from document attributes ...
		String[] docAns = doc.getAttributeNames();
		for (int n = 0; n < docAns.length; n++) {
			if (docAns[n].startsWith("xmlns:"))
				continue;
			if (docAns[n].indexOf(':') != -1)
				docXmlns.add(docAns[n].substring(0, docAns[n].indexOf(':')));
		}
		
		//	... to annotation types ...
		Annotation[] docAnnots = doc.getAnnotations();
		for (int a = 0; a < docAnnots.length; a++) {
			String annotType = docAnnots[a].getType();
			if (annotType.indexOf(':') != -1)
				docXmlns.add(annotType.substring(0, annotType.indexOf(':')));
			
			//	... and annotation attributes
			String[] annotAns = docAnnots[a].getAttributeNames();
			for (int n = 0; n < annotAns.length; n++) {
				if (annotAns[n].startsWith("xmlns:"))
					continue;
				if (annotAns[n].indexOf(':') != -1)
					docXmlns.add(annotAns[n].substring(0, annotAns[n].indexOf(':')));
			}
		}
		
		//	anything to clean up?
		if (docXmlns.isEmpty())
			return false;
		
		//	track namespace declaration removals
		boolean docModified = false;
		
		//	remove unused XML namespace declarations so XSLTs can define their own, from the document proper ...
		for (int n = 0; n < docAns.length; n++) {
			if (!docAns[n].startsWith("xmlns:"))
				continue;
			if (!docXmlns.contains(docAns[n].substring("xmlns:".length()))) {
				doc.removeAttribute(docAns[n]);
				docModified = true;
			}
		}
		
		//	... as well as from its annotations
		for (int a = 0; a < docAnnots.length; a++) {
			String[] annotAns = docAnnots[a].getAttributeNames();
			for (int n = 0; n < annotAns.length; n++) {
				if (!annotAns[n].startsWith("xmlns:"))
					continue;
				if (!docXmlns.contains(annotAns[n].substring("xmlns:".length()))) {
					docAnnots[a].removeAttribute(annotAns[n]);
					docModified = true;
				}
			}
		}
		
		//	did we change anything?
		return docModified;
	}
	
	private static final XmlOutputOptions startTagOptionsNoId = new XmlOutputOptions() {
		public boolean writeAttribute(String name) {
			return true;
		}
		public boolean includeIDs(String annotType) {
			return false;
		}
		public boolean escapeAttributeValues() {
			return true;
		}
	};
	private static final XmlOutputOptions startTagOptionsWithId = new XmlOutputOptions() {
		public boolean writeAttribute(String name) {
			return true;
		}
		public boolean includeIDs(String annotType) {
			return true;
		}
		public boolean escapeAttributeValues() {
			return true;
		}
	};
	
	/**
	 * Produce an XML start tag for an Annotation. This method includes all
	 * attributes, escaping their values. For more fine-grained control, use the
	 * three and four argument version of this method.
	 * @param data the Annotation to produce a start tag for
	 * @return an XML start tag for the specified Annotation
	 */
	public static String produceStartTag(Annotation data) {
		return produceStartTag(data, startTagOptionsNoId);
	}
	
	/**
	 * Produce an XML start tag for an Annotation. This method includes all
	 * attributes, escaping their values. For more fine-grained control, use the
	 * three and four argument version of this method.
	 * @param data the Annotation to produce a start tag for
	 * @param includeId include Annotation ID attribute?
	 * @return an XML start tag for the specified Annotation
	 */
	public static String produceStartTag(Annotation data, boolean includeId) {
		return produceStartTag(data, (includeId ? startTagOptionsWithId : startTagOptionsNoId));
	}
	
	/**
	 * Produce an XML start tag for an Annotation. The specified attribute
	 * filter set (if any) is checked for the individual attribute names solely
	 * by means of its contains() method; this facilitates filtering out
	 * specific attributes by excluding them explicitly, returning true for all
	 * other types.
	 * @param data the Annotation to produce a start tag for
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tag (specifying null will include all
	 *            attributes)
	 * @param escapeValues check and if necessary escape attribute values
	 *            (transform '&amp;' to '&amp;amp;', '&quot;' to '&amp;quot;',
	 *            etc.)
	 * @return an XML start tag for the specified Annotation
	 */
	public static String produceStartTag(Annotation data, Set attributeFilter, boolean escapeValues) {
		if ((attributeFilter == null) && escapeValues) // avoids frequent creation of option wrapper (just too many external calls)
			return produceStartTag(data, startTagOptionsNoId);
		else return produceStartTag(data, false, attributeFilter, escapeValues);
	}
	
	/**
	 * Produce an XML start tag for an Annotation. The specified attribute
	 * filter set (if any) is checked for the individual attribute names solely
	 * by means of its contains() method; this facilitates filtering out
	 * specific attributes by excluding them explicitly, returning true for all
	 * other types.
	 * @param data the Annotation to produce a start tag for
	 * @param includeId include Annotation ID attribute?
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tag (specifying null will include all
	 *            attributes)
	 * @param escapeValues check and if necessary escape attribute values
	 *            (transform '&amp;' to '&amp;amp;', '&quot;' to '&amp;quot;',
	 *            etc.)
	 * @return an XML start tag for the specified Annotation
	 */
	public static String produceStartTag(Annotation data, boolean includeId, Set attributeFilter, boolean escapeValues) {
		if ((attributeFilter == null) && escapeValues) // avoids frequent creation of option wrapper (just too many external calls)
			return produceStartTag(data, (includeId ? startTagOptionsWithId : startTagOptionsNoId));
		XmlOutputOptions options = new XmlOutputOptions();
		options.setIncludeIdTypes(Collections.emptySet(), includeId);
		if (attributeFilter != null)
			options.setAttributeNames(attributeFilter, false);
		options.setEscape(escapeValues);
		return produceStartTag(data, options);
	}
	
	/**
	 * Produce an XML start tag for an Annotation, including attributes and a
	 * UUID as specified by the argument options.
	 * @param data the Annotation to produce a start tag for
	 * @param options the output options specifying details
	 * @return an XML start tag for the specified Annotation
	 */
	public static String produceStartTag(Annotation data, XmlOutputOptions options) {
		StringBuffer tagAssembler = new StringBuffer("<");
		tagAssembler.append(data.getType());
		if (options.includeIDs(data.getType()) && !data.hasAttribute("id"))
			tagAssembler.append(" id=\"" + data.getAnnotationID() + "\"");
		String[] ans = data.getAttributeNames();
		for (int a = 0; a < ans.length; a++)
			if (options.writeAttribute(data.getType(), ans[a])) {
				Object value = data.getAttribute(ans[a]);
				if (value != null) {
					String valueString = value.toString();
					if (options.escapeAttributeValues())
						valueString = escapeForXml(valueString, true);
					tagAssembler.append(" " + ans[a] + "=\"" + valueString + "\"");
				}
			}
		tagAssembler.append(">");
		return tagAssembler.toString();
	}
	
	/**
	 * Produce an XML end tag for an Annotation.
	 * @param data the Annotation to produce an end tag for
	 * @return an XML end tag for the specified Annotation
	 */
	public static String produceEndTag(Annotation data) {
		return ("</" + data.getType() + ">");
	}
	
	/**
	 * Obtain an XML representation of an Annotation.
	 * @param data the Annotation to produce an XML representation for
	 * @return an XML representation of the specified Annotation
	 */
	public static String toXML(Annotation data) {
		return (produceStartTag(data) + data.getValue() + produceEndTag(data));
	}
	
	/**
	 * Obtain an XML representation of an Annotation.
	 * @param data the Annotation to produce an XML representation for
	 * @return an XML representation of the specified Annotation
	 */
	public static String toXML(QueriableAnnotation data) {
		return toXML(data, null);
	}
	
	/**
	 * Obtain an XML representation of an Annotation.
	 * @param data the Annotation to produce an XML representation for
	 * @param annotationTypes a set containing the annotation types to restrict
	 *            the output to (specifying null will write all annotations)
	 * @return an XML representation of the specified Annotation
	 */
	public static String toXML(QueriableAnnotation data, Set annotationTypes) {
		try {
			StringWriter sw = new StringWriter();
			writeXML(data, sw, annotationTypes);
			return sw.toString();
		}
		catch (IOException e) {
			return "";
		}
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
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. This method writes all annotations and includes all
	 * attributes in the start tags, escaping their values. For more
	 * fine-grained control, use the five argument version of this method.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output) throws IOException {
		return writeXML(data, output, xmlWriteOptionsNoId);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. This method writes all annotations and includes all
	 * attributes in the start tags, escaping their values. For more
	 * fine-grained control, use the five argument version of this method.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @param writeIDs include annotation IDs in the output?
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output, boolean writeIDs) throws IOException {
		return writeXML(data, output, (writeIDs ? xmlWriteOptionsWithId : xmlWriteOptionsNoId));
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. The specified annotation type set (if any) is checked
	 * for the individual annotation types solely by means of its contains()
	 * method; this facilitates filtering out specific annotation types by
	 * excluding them explicitly, returning true for all other types. This
	 * method includes all attributes in the start tags, escaping their values.
	 * For more fine-grained control, use the five argument version of this
	 * method.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @param annotationTypes a set containing the annotation types to restrict
	 *            the output to (specifying null will write all annotations)
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output, Set annotationTypes) throws IOException {
		if (annotationTypes == null) // avoids frequent creation of option wrapper (just too many external calls)
			return writeXML(data, output, xmlWriteOptionsNoId);
		else return writeXML(data, output, annotationTypes, null, true);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. This method outputs all the annotations in the
	 * argument array. It is up to client code to ensure that (a) there is a
	 * root element around the output and (b) that the order of the argument
	 * annotation array is in line with nesting rules regarding the extent of
	 * the annotations. If the latter is violated, output may be arbitrary.
	 * This method includes all attributes in the start tags, escaping their
	 * values. For more fine-grained control, use the five argument version of
	 * this method.
	 * @param data the Annotation to write
	 * @param annotations the annotations whose tags to output
	 * @param output the Writer to write to
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Annotation[] annotations, Writer output) throws IOException {
		return writeXML(data, annotations, output, null, true);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. The specified annotation type set (if any) is checked
	 * for the individual annotation types solely by means of its contains()
	 * method; this facilitates filtering out specific annotation types by
	 * excluding them explicitly, returning true for all other types. This
	 * method includes all attributes in the start tags, escaping their values.
	 * For more fine-grained control, use the five argument version of this
	 * method.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @param writeIDs include annotation IDs in the output?
	 * @param annotationTypes a set containing the annotation types to restrict
	 *            the output to (specifying null will write all annotations)
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output, boolean writeIDs, Set annotationTypes) throws IOException {
		if (annotationTypes == null) // avoids frequent creation of option wrapper (just too many external calls)
			return writeXML(data, output, (writeIDs ? xmlWriteOptionsWithId : xmlWriteOptionsNoId));
		else return writeXML(data, output, writeIDs, annotationTypes, null, true);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. This method outputs all the annotations in the
	 * argument array. It is up to client code to ensure that (a) there is a
	 * root element around the output and (b) that the order of the argument
	 * annotation array is in line with nesting rules regarding the extent of
	 * the annotations. If the latter is violated, output may be arbitrary.
	 * This method includes all attributes in the start tags, escaping their
	 * values. For more fine-grained control, use the five argument version of
	 * this method.
	 * @param data the Annotation to write
	 * @param annotations the annotations whose tags to output
	 * @param output the Writer to write to
	 * @param writeIDs include annotation IDs in the output?
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Annotation[] annotations, Writer output, boolean writeIDs) throws IOException {
		return writeXML(data, annotations, output, writeIDs, null, true);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. The specified annotation type set (if any) is checked
	 * for the individual annotation types solely by means of its contains()
	 * method; this facilitates filtering out specific annotation types by
	 * excluding them explicitly, returning true for all other types. The same
	 * applies to the attribute filter set.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @param annotationTypes a set containing the annotation types to restrict
	 *            the output to (specifying null will write all annotations)
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 * @param escape check and if necessary escape text data and attribute
	 *            values (transform '&amp;' to '&amp;amp;', '&quot;' to
	 *            '&amp;quot;', etc.)
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output, Set annotationTypes, Set attributeFilter, boolean escape) throws IOException {
		if ((annotationTypes == null) && (attributeFilter == null) && escape) // avoids frequent creation of option wrapper (just too many external calls)
			return writeXML(data, output, xmlWriteOptionsNoId);
		else return writeXML(data, output, false, annotationTypes, attributeFilter, escape);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. This method outputs all the annotations in the
	 * argument array. It is up to client code to ensure that (a) there is a
	 * root element around the output and (b) that the order of the argument
	 * annotation array is in line with nesting rules regarding the extent of
	 * the annotations. If the latter is violated, output may be arbitrary.
	 * Attribute output is controlled by The argument attribute filter set.
	 * @param data the Annotation to write
	 * @param annotations the annotations whose tags to output
	 * @param output the Writer to write to
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 * @param escape check and if necessary escape text data and attribute
	 *            values (transform '&amp;' to '&amp;amp;', '&quot;' to
	 *            '&amp;quot;', etc.)
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Annotation[] annotations, Writer output, Set attributeFilter, boolean escape) throws IOException {
		if ((attributeFilter == null) && escape) // avoids frequent creation of option wrapper (just too many external calls)
			return writeXML(data, annotations, output, xmlWriteOptionsNoId);
		else return writeXML(data, annotations, output, false, attributeFilter, escape);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. The specified annotation type set (if any) is checked
	 * for the individual annotation types solely by means of its contains()
	 * method; this facilitates filtering out specific annotation types by
	 * excluding them explicitly, returning true for all other types. The same
	 * applies to the attribute filter set.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @param writeIDs include annotation IDs in the output?
	 * @param annotationTypes a set containing the annotation types to restrict
	 *            the output to (specifying null will write all annotations)
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 * @param escape check and if necessary escape text data and attribute
	 *            values (transform '&amp;' to '&amp;amp;', '&quot;' to
	 *            '&amp;quot;', etc.)
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output, boolean writeIDs, Set annotationTypes, Set attributeFilter, boolean escape) throws IOException {
		if ((annotationTypes == null) && (attributeFilter == null) && escape) // avoids frequent creation of option wrapper (just too many external calls)
			return writeXML(data, output, (writeIDs ? xmlWriteOptionsWithId : xmlWriteOptionsNoId));
		XmlOutputOptions options = new XmlOutputOptions();
		options.setIncludeIdTypes(Collections.emptySet(), writeIDs);
		if (annotationTypes != null)
			options.setAnnotationTypes(annotationTypes, false);
		if (attributeFilter != null)
			options.setAttributeNames(attributeFilter, false);
		options.setEscape(escape);
		return writeXML(data, output, options);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer. This method outputs all the annotations in the
	 * argument array. It is up to client code to ensure that (a) there is a
	 * root element around the output and (b) that the order of the argument
	 * annotation array is in line with nesting rules regarding the extent of
	 * the annotations. If the latter is violated, output may be arbitrary. The
	 * specified attribute filter set (if any) is checked for the individual
	 * attribute names solely by means of its contains() method; this
	 * facilitates filtering out specific attributes by excluding them
	 * explicitly, returning true for all other types.
	 * @param data the Annotation to write
	 * @param annotations the annotations whose tags to output
	 * @param output the Writer to write to
	 * @param writeIDs include annotation IDs in the output?
	 * @param attributeFilter a set containing the names of the attributes to
	 *            include in the tags (specifying null will include all
	 *            attributes)
	 * @param escape check and if necessary escape text data and attribute
	 *            values (transform '&amp;' to '&amp;amp;', '&quot;' to
	 *            '&amp;quot;', etc.)
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Annotation[] annotations, Writer output, boolean writeIDs, Set attributeFilter, boolean escape) throws IOException {
		if ((attributeFilter == null) && escape) // avoids frequent creation of option wrapper (just too many external calls)
			return writeXML(data, annotations, output, (writeIDs ? xmlWriteOptionsWithId : xmlWriteOptionsNoId));
		XmlOutputOptions options = new XmlOutputOptions();
		options.setIncludeIdTypes(Collections.emptySet(), writeIDs);
		if (attributeFilter != null)
			options.setAttributeNames(attributeFilter, false);
		options.setEscape(escape);
		return writeXML(data, annotations, output, options);
	}
	
	/**
	 * Object holding options for XML output.
	 * 
	 * @author sautter
	 */
	public static class XmlOutputOptions {
		private Set annotationTypes = null;
		private boolean invertAnnotationTypes = false;
		
		private Set attributeNames = null;
		private boolean invertAttributeNames = false;
		
		private Set includeIdTypes = null;
		private boolean invertIncludeIdTypes = true;
		
		private Set inLineTypes = null;
		private boolean invertInLineTypes = true;
		
		private boolean escape = true;
		
		private String space = " ";
		private String lineBreak = "\r\n";
		
		/**
		 * Set the annotation types to include in the output (positive filter).
		 * To use a negative filter, set <code>invert</code> to true and provide
		 * a set of annotation types to exclude instead.
		 * @param annotationTypes the annotation types to include
		 * @param invert invert set containment?
		 */
		public void setAnnotationTypes(Set annotationTypes, boolean invert) {
			this.annotationTypes = annotationTypes;
			this.invertAnnotationTypes = invert;
		}
		
		/**
		 * Set the attribute names to include in the output (positive filter).
		 * To use a negative filter, set <code>invert</code> to true and provide
		 * a set of attribute names to exclude instead.
		 * @param attributeNames the attribute names to include
		 * @param invert invert set containment?
		 */
		public void setAttributeNames(Set attributeNames, boolean invert) {
			this.attributeNames = attributeNames;
			this.invertAttributeNames = invert;
		}
		
		/**
		 * Set the annotation types to include the UUID for (positive filter).
		 * To use a negative filter, set <code>invert</code> to true and provide
		 * a set of annotation types to exclude the UUID for instead.
		 * @param includeIdTypes the annotation types to include the UUIDs for
		 * @param invert invert set containment?
		 */
		public void setIncludeIdTypes(Set includeIdTypes, boolean invert) {
			this.includeIdTypes = includeIdTypes;
			this.invertIncludeIdTypes = invert;
		}
		
		/**
		 * Set the annotation types to output in-line, i.e., without line breaks
		 * before start tags and after end tags. To use a negative filter, set
		 * <code>invert</code> to true and provide a set of annotation types to
		 * actually add line breaks before start tags and after end tags for.
		 * @param inLineTypes the annotation types to output in-line
		 * @param invert invert set containment?
		 */
		public void setInLineTypes(Set inLineTypes, boolean invert) {
			this.inLineTypes = inLineTypes;
			this.invertInLineTypes = invert;
		}
		
		/**
		 * Set to false to disable escaping attribute values and textual
		 * content.
		 * @param escape the escape property to set
		 */
		public void setEscape(boolean escape) {
			this.escape = escape;
		}
		
		/**
		 * Set the sequence of characters to output for a space. This character
		 * sequence does not necessarily have to exclusively consist of space
		 * characters, and does not even need to include actual space
		 * characters. If the argument character sequence is null, this will be
		 * interpreted as a regular space character; if it represents any XML,
		 * it must be valid, as it will not be escaped before output.
		 * @param space the character sequence to output for a space
		 */
		public void setSpace(String space) {
			this.space = ((space == null) ? " " : space);
		}
		
		/**
		 * Set the sequence of characters to output for a line break. This
		 * character sequence does not necessarily have to exclusively consist
		 * of space or line break characters, and does not even need to include
		 * actual space characters. If the argument character sequence is null,
		 * this will be interpreted as a cross-platform line break; if it
		 * represents any XML, it must be valid, as it will not be escaped
		 * before output.
		 * @param lineBreak the character sequence to output for a line break
		 */
		public void setLineBreak(String lineBreak) {
			this.lineBreak = ((lineBreak == null) ? "\r\n" : lineBreak);
		}
		
		/**
		 * Check whether or not to include annotations of a given type in the
		 * output.
		 * @param annotType the annotation type to check
		 * @return true if annotations of the argument type should be output
		 */
		public boolean writeAnnotations(String annotType) {
			if (this.annotationTypes == null)
				return !this.invertAnnotationTypes;
			else return (this.annotationTypes.contains(annotType) != this.invertAnnotationTypes);
		}
		
		/**
		 * Check whether or not to include attributes with a given name in the
		 * output.
		 * @param name the attribute name to check
		 * @return true if attributes with the argument name should be output
		 */
		public boolean writeAttribute(String name) {
			if (this.attributeNames == null)
				return !this.invertAttributeNames;
			else return (this.attributeNames.contains(name) != this.invertAttributeNames);
		}
		
		/**
		 * Check whether or not to include attributes with a given name in the
		 * output for an annotation of a given type. This implementation simply
		 * defaults to the one-argument <code>writeAttribute()</code> method,
		 * sub classes may overwrite it for more fine-grained control.
		 * @param annotType the annotation type to check
		 * @param name the attribute name to check
		 * @return true if attributes with the argument name should be output
		 */
		public boolean writeAttribute(String anotType, String name) {
			return this.writeAttribute(name);
		}
		
		/**
		 * Check whether or not to include the UUIDs of annotations of a given
		 * type in the output.
		 * @param annotType the annotation type to check
		 * @return true if annotations of the argument type should be output
		 *        with their UUIDs
		 */
		public boolean includeIDs(String annotType) {
			if (this.includeIdTypes == null)
				return !this.invertIncludeIdTypes;
			else return (this.includeIdTypes.contains(annotType) != this.invertIncludeIdTypes);
		}
		
		/**
		 * Check whether or not to output the start and end tags of annotations
		 * of a given type in-line.
		 * @param annotType the annotation type to check
		 * @return true if tags of annotations of the argument type should be
		 *        output in-line
		 */
		public boolean writeInLine(String annotType) {
			if (this.inLineTypes == null)
				return !this.invertInLineTypes;
			else return (this.inLineTypes.contains(annotType) != this.invertInLineTypes);
		}
		
		/**
		 * Check whether or not to escape textual content tokens in the output.
		 * @return true if textual content tokens should be escaped
		 */
		public boolean escapeTokens() {
			return this.escape;
		}
		
		/**
		 * Check whether or not to escape attribute values in the output.
		 * @return true if attribute values should be escaped
		 */
		public boolean escapeAttributeValues() {
			return this.escape;
		}
		
		/**
		 * Get the sequence of characters to output for a space. This character
		 * sequence does not necessarily have to exclusively consist of space
		 * characters, and does not even need to include actual space
		 * characters. However, this method must not return null. If the
		 * returned character sequence represents an XML, it must be valid, as
		 * it will not be escaped before output.
		 * @return the character sequence to output for a space
		 */
		public String getSpace() {
			//	TODO add context information (after token or tag, before token or tag)
			return this.space;
		}
		
		/**
		 * Get the sequence of characters to output for a line break. This
		 * character sequence does not necessarily have to exclusively consist
		 * of space or line break characters, and does not even need to include
		 * actual space characters. However, this method must not return null.
		 * If the returned character sequence represents an XML, it must be
		 * valid, as it will not be escaped before output.
		 * @return the character sequence to output for a line break
		 */
		public String getLineBreak() {
			//	TODO add context information (after token or tag, before token or tag)
			return this.lineBreak;
		}
		
		/* TODO Facilitate controlling whitespace in GAMTA XML output:
- line breaks before and after start and end tags ...
  ==> add respective getters to XML output options
- ... as well as indentation after line breaks
  ==> track stack depth ...
  ==> ... and add getter for per-level indent to XML output options
- space between tokens ...
- ... depending upon immediately preceding end tag(s) ...
- ... as well as upcoming start tag(s)
  ==> implement latter by tracking preceding end tags (boolean) ...
  ==> ... and collecting subsequent start tags before actually anything
==> default all of this to current behavior
==> facilitates escaping standalone spaces right there ...
==> ... as well as single-line output ...
==> ... and nicely indented pretty-print
		 */
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer, using the argument options.
	 * @param data the Annotation to write
	 * @param output the Writer to write to
	 * @param options an object holding the output options.
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Writer output, XmlOutputOptions options) throws IOException {
		
		//	get annotations
		Annotation[] annotations = data.getAnnotations();
		
		//	filter annotations
		ArrayList annotationList = new ArrayList();
		for (int a = 0; a < annotations.length; a++)
			if (options.writeAnnotations(annotations[a].getType()))
				annotationList.add(annotations[a]);
		annotations = ((Annotation[]) annotationList.toArray(new Annotation[annotationList.size()]));
		
		//	make sure there is a root element
		if ((annotations.length == 0) || (annotations[0].size() < data.size())) {
			Annotation[] newNestedAnnotations = new Annotation[annotations.length + 1];
			newNestedAnnotations[0] = data;
			System.arraycopy(annotations, 0, newNestedAnnotations, 1, annotations.length);
			annotations = newNestedAnnotations;
		}
		
		//	output the whole stuff
		return writeXML(data, annotations, output, options);
	}
	
	/**
	 * Write the content of a Queriable Annotation marked up with XML to the
	 * specified Writer, using the argument options. Regardless of the latter,
	 * this method outputs all the annotations in the argument array, and any
	 * type based filtering on the part of the argument options is ignored. It
	 * is up to client code to ensure that (a) there is a root element around
	 * the output and (b) that the order of the argument annotation array is in
	 * line with nesting rules regarding the extent of the annotations. If the
	 * latter is violated, output may be arbitrary.
	 * @param data the Annotation to write
	 * @param annotations the annotations whose tags to output
	 * @param output the Writer to write to
	 * @param options an object holding the output options.
	 * @return true if and only if the output was written successfully
	 */
	public static boolean writeXML(QueriableAnnotation data, Annotation[] annotations, Writer output, XmlOutputOptions options) throws IOException {
		BufferedWriter buf = ((output instanceof BufferedWriter) ? ((BufferedWriter) output) : new BufferedWriter(output));
		
		Stack stack = new Stack();
		int annotationPointer = 0;
		
		Token token = null;
		Token lastToken;
		
		boolean lastWasLineBreak = true;
		HashSet lineBroken = new HashSet();
		
		for (int t = 0; t < data.size(); t++) {
			
			//	switch to next token
			lastToken = token;
			token = data.tokenAt(t);
			
			//	write end tags for annotations ending before current Token
			while ((stack.size() > 0) && (((Annotation) stack.peek()).getEndIndex() <= t)) {
				Annotation annotation = ((Annotation) stack.pop());
				
				//	line break only if nested annotations
				if (!lastWasLineBreak && lineBroken.contains(annotation.getAnnotationID()))
					buf.write(options.getLineBreak());
				
				//	write tag and line break
				buf.write(produceEndTag(annotation));
				lastWasLineBreak = false;
				
				//	line break (omit if in-line)
				if (options.writeInLine(annotation.getType()))
					continue;
				buf.write(options.getLineBreak());
				lastWasLineBreak = true;
			}
			
			//	add line break if required
			if (!lastWasLineBreak && (lastToken != null) && lastToken.hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE)) {
				buf.write(options.getLineBreak());
				lastWasLineBreak = true;
			}
			
			//	skip space character before unspaced punctuation (e.g. ','), after line breaks and tags, and if there is no whitespace in the token sequence
			if (!lastWasLineBreak && 
					(lastToken != null) && !lastToken.hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE) && 
					Gamta.insertSpace(lastToken, token) &&
					(t != 0) && (data.getWhitespaceAfter(t-1).length() != 0)
				) buf.write(options.getSpace());
			
			//	write start tags for annotations beginning at current token
//			while ((annotationPointer < annotations.length) && (annotations[annotationPointer].getStartIndex() == t)) {
			while ((annotationPointer < annotations.length) && (annotations[annotationPointer].getStartIndex() <= t)) {
				Annotation annotation = annotations[annotationPointer++];
				if (annotation.getStartIndex() < t)
					continue;
				stack.push(annotation);
				
				//	line break (omit if in-line)
				if (!lastWasLineBreak && !options.writeInLine(annotation.getType()))
					buf.write(options.getLineBreak());
				
				//	add start tag
				buf.write(produceStartTag(annotation, options));
				lastWasLineBreak = false;
				
				//	line break only if nested annotations (omit if in-line)
				if (options.writeInLine(annotation.getType()))
					continue;
				if ((annotationPointer < annotations.length) && AnnotationUtils.contains(annotation, annotations[annotationPointer])) {
					buf.write(options.getLineBreak());
					lastWasLineBreak = true;
					lineBroken.add(annotation.getAnnotationID());
				}
			}
			
			//	append current token
			if (options.escapeTokens())
				buf.write(escapeForXml(token.getValue()));
			else buf.write(token.getValue());
			
			//	set status
			//lastWasTag = false;
			lastWasLineBreak = false;
		}
		
		//	write end tags for annotations not closed so far
		while (stack.size() > 0) {
			Annotation annotation = ((Annotation) stack.pop());
			
			if (!lastWasLineBreak && lineBroken.contains(annotation.getAnnotationID()))
				buf.write(options.getLineBreak());
			
			buf.write(produceEndTag(annotation));
			lastWasLineBreak = false;
		}
		if (buf != output)
			buf.flush();
		return true;
	}
	
	/**
	 * Escape a string to be well-formed XML - in particular, escape &amp;,
	 * &lt;, &gt;, &quot;.
	 * @param string the string to escape
	 * @return the escaped string
	 */
	public static String escapeForXml(String string) {
		return escapeForXml(string, false);
	}
	
	/**
	 * Escape a string to be well-formed XML - in particular, escape &amp;,
	 * &lt;, &gt;, &quot;, and optionally control characters. The latter is
	 * helpful for storing attribute values that have line breaks, for instance.
	 * @param string the string to escape
	 * @param escapeControl escape control characters?
	 * @return the escaped string
	 */
	public static String escapeForXml(String string, boolean escapeControl) {
		StringBuffer escapedString = new StringBuffer();
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
			if (ch == '<')
				escapedString.append("&lt;");
			else if (ch == '>')
				escapedString.append("&gt;");
			else if (ch == '"')
				escapedString.append("&quot;");
//			else if (ch == '\'') // this does a lot more harm than good, as many applications don't understand the entity
//				escapedString.append("&apos;");
			else if (ch == '&')
				escapedString.append("&amp;");
//			else if ((ch < 32) || (ch == 127) || (ch == 129) || (ch == 141) || (ch == 143) || (ch == 144) || (ch == 157)) {
			else if ((ch < 0x0020) || (ch == 0x007F) || (ch == 0x0081) || (ch == 0x008D) || (ch == 0x008F) || (ch == 0x0090) || (ch == 0x009D)) {
				if (escapeControl && ((ch == '\t') || (ch == '\n') || (ch == '\r')))
					escapedString.append("&#x" + Integer.toString(((int) ch), 16).toUpperCase() + ";");
				else escapedString.append(' ');
			}
			else escapedString.append(ch);
		}
		return escapedString.toString();
	}
	
	/**
	 * Un-escape a string from its XML encoding - in particular, un-escape
	 * &amp;, &lt;, &gt;, &quot;, and hex encoded characters.
	 * @param escapedString the string to un-escape
	 * @return the un-escaped string
	 */
	public static String unescapeFromXml(String escapedString) {
		StringBuffer string = new StringBuffer();
		for (int c = 0; c < escapedString.length();) {
			char ch = escapedString.charAt(c);
			if (ch == '&') {
				if (escapedString.startsWith("amp;", (c+1))) {
					string.append('&');
					c+=5;
				}
				else if (escapedString.startsWith("lt;", (c+1))) {
					string.append('<');
					c+=4;
				}
				else if (escapedString.startsWith("gt;", (c+1))) {
					string.append('>');
					c+=4;
				}
				else if (escapedString.startsWith("quot;", (c+1))) {
					string.append('"');
					c+=6;
				}
				else if (escapedString.startsWith("apos;", (c+1))) {
					string.append('\'');
					c+=6;
				}
				else if (escapedString.startsWith("#x", (c+1))) {
					int sci = escapedString.indexOf(';', (c+2));
					if ((sci != -1) && (sci <= (c + "6#x".length() + 4))) try {
						ch = ((char) Integer.parseInt(escapedString.substring((c + "&#x".length()), sci), 16));
						c = sci;
					} catch (Exception e) {}
					string.append(ch);
					c++;
				}
				else if (escapedString.startsWith("#", (c+1))) {
					int sci = escapedString.indexOf(';', (c+1));
					if ((sci != -1) && (sci <= (c + "&#".length() + 4))) try {
						ch = ((char) Integer.parseInt(escapedString.substring((c + "&#".length()), sci)));
						c = sci;
					} catch (Exception e) {}
					string.append(ch);
					c++;
				}
				else if (escapedString.startsWith("x", (c+1))) {
					int sci = escapedString.indexOf(';', (c+1));
					if ((sci != -1) && (sci <= (c+4))) try {
						ch = ((char) Integer.parseInt(escapedString.substring((c + "&x".length()), sci), 16));
						c = sci;
					} catch (Exception e) {}
					string.append(ch);
					c++;
				}
				else {
					string.append(ch);
					c++;
				}
			}
			else {
				string.append(ch);
				c++;
			}
		}
		return string.toString();
	}
	
	/**
	 * Test if the annotations of a document or a part of it represent
	 * wellformed XML, i.e. if the annotations are properly nested.
	 * @param data the document to test
	 * @return true if and only if the annotations of the specified document are
	 *         properly nested
	 */
	public static boolean isWellFormedNesting(QueriableAnnotation data) {
		return isWellFormedNesting(data, null);
	}
	
	/**
	 * Test if the annotations of a document or a part of it represent
	 * wellformed XML, i.e. if the annotations are properly nested. The
	 * specified annotation type set (if any) is checked for the individual
	 * annotation types solely by means of its contains() method; this
	 * facilitates filtering out specific annotation types by excluding them
	 * explicitly, returning true for all other types.
	 * @param data the document to test
	 * @param typeFilter a set containing the annotation types to include in the
	 *            test (specifying null considers all annotations, regardless of
	 *            their type)
	 * @return true if and only if the annotations of the specified document are
	 *         properly nested
	 */
	public static boolean isWellFormedNesting(QueriableAnnotation data, Set typeFilter) {
		
		//	get annotations
		Annotation[] nestedAnnotations = data.getAnnotations();
		
		//	filter annotations
		if (typeFilter != null) {
			ArrayList annotationList = new ArrayList();
			for (int a = 0; a < nestedAnnotations.length; a++)
				if (typeFilter.contains(nestedAnnotations[a].getType()))
					annotationList.add(nestedAnnotations[a]);
			nestedAnnotations = ((Annotation[]) annotationList.toArray(new Annotation[annotationList.size()]));
		}
		
		//	make sure there is a root element
		if ((nestedAnnotations.length == 0) || (nestedAnnotations[0].size() < data.size())) {
			Annotation[] newNestedAnnotations = new Annotation[nestedAnnotations.length + 1];
			newNestedAnnotations[0] = data;
			System.arraycopy(nestedAnnotations, 0, newNestedAnnotations, 1, nestedAnnotations.length);
			nestedAnnotations = newNestedAnnotations;
		}
		
		Stack stack = new Stack();
		int annotationPointer = 0;
		
		for (int t = 0; t < data.size(); t++) {
			
			//	pop and test annotations ending before current Token
			while ((stack.size() > 0) && (((Annotation) stack.peek()).getEndIndex() <= t)) {
				Annotation annotation = ((Annotation) stack.pop());
				
				//	test if annotation ends at current index or before
				if (annotation.getEndIndex() < t) {
					stack.clear();
					return false;
				}
			}
			
			//	push annotations beginning at current Token
			while ((annotationPointer < nestedAnnotations.length) && (nestedAnnotations[annotationPointer].getStartIndex() == t)) {
				Annotation annotation = nestedAnnotations[annotationPointer];
				stack.push(annotation);
				annotationPointer++;
			}
		}
		
		//	pop and test remaining annotations
		while (stack.size() > 0) {
			Annotation annotation = ((Annotation) stack.pop());
			
			//	test if annotation ends at current index or before
			if (annotation.getEndIndex() < data.size()) {
				stack.clear();
				return false;
			}
		}
		
		//	no errors found, report success
		return true;
	}
//	
//	public static void main(String[] args) {
//		String str = "new line\ncarriage return\rtab\tdel\u007fweird\u0013end";
//		System.out.println(escapeForXml(str, false));
//		System.out.println(escapeForXml(str, true));
//		System.out.println(unescapeFromXml(escapeForXml(str, true)));
//	}
}