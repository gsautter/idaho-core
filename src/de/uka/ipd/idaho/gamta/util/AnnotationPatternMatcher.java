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

import java.io.FilterReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationListener;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.MutableTokenSequence.TokenSequenceEvent;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.TokenSequence;
import de.uka.ipd.idaho.gamta.TokenSequenceListener;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils;
import de.uka.ipd.idaho.gamta.Tokenizer;
import de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed;
import de.uka.ipd.idaho.gamta.util.gPath.GPath;
import de.uka.ipd.idaho.gamta.util.gPath.GPathExpression;
import de.uka.ipd.idaho.gamta.util.gPath.GPathParser;
import de.uka.ipd.idaho.gamta.util.gPath.exceptions.GPathException;
import de.uka.ipd.idaho.gamta.util.gPath.exceptions.GPathSyntaxException;
import de.uka.ipd.idaho.gamta.util.gPath.types.GPathObject;
import de.uka.ipd.idaho.htmlXmlUtil.TreeNodeAttributeSet;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;
import de.uka.ipd.idaho.stringUtils.Dictionary;
import de.uka.ipd.idaho.stringUtils.StringUtils;

/**
 * Matcher for patterns over annotations and intermediate literals. Annotations
 * can be filtered based on attributes and attribute values, also using regular
 * expression patterns.<br>
 * The pattern syntax is partially XML based:<br>
 * <ul>
 * <li><code>&lt;<i>type</i>&gt;</code>: matches an annotation of the specified
 * type. Further, XPath expressions can be used for filtering:<ul>
 * <li>
 * <code>&lt;<i>type</i> <i>test</i>=&quot;(xPathExpression)&quot;&gt;</code>:
 * matches an annotation of the specified type if <code>xPathExpression</code>
 * evaluates to <code>true</code> for the annotation</li>
 * </ul></li>
 * <li>
 * <code>'<i>literal</i>'</code>: matches the literal between the high commas</li>
 * <li>
 * <code>&quot;<i>pattern</i>&quot;</code>: matches any sequence of tokens
 * matched by the regular expression pattern between the double quotes</li>
 * <li>
 * <code>(<i>A</i>|<i>B</i>)</code>: matches any one match of sub pattern <i>A</i>
 * or <i>B</i></li>
 * <li>Each atomic part of an annotation pattern (annotation matchers, literals,
 * and pattern literals) can have a quantifier attached to it, akin to Java
 * regular expression patterns:<ul>
 * <li>
 * <code>{<i>m</i>,<i>n</i>}</code>: indicates that the atom immediately
 * preceding the opening curly bracket has to occur at least <i>m</i> times in
 * a match and can occur at most <i>n</i> times.</li>
 * <li>
 * <code>*</code>: equivalent to <code>{0,inf}</code>, where <code>inf</code>
 * indicates infinity</li>
 * <li>
 * <code>+</code>: equivalent to <code>{1,inf}</code>, where <code>inf</code>
 * indicates infinity</li>
 * <li>
 * <code>?</code>: equivalent to <code>{0,1}</code></li>
 * </ul></li>
 * </ul>
 * <br>
 * Examples:<br>
 * <code>&lt;lastName&gt; ', ' &lt;initials&gt;? &lt;firstName&gt; &lt;initials&gt;?</code>
 * <br>
 * This pattern matches a <code>lastName</code> annotation followed by a comma,
 * an optional leading <code>initials</code> annotation, a
 * <code>firstName</code> annotation, and an optional middle
 * <code>initials</code> annotation.<br>
 * <code>&lt;lastName lang=&quot;en&quot;&gt; ', ' &lt;initials&gt;? &lt;firstName&gt; &lt;initials&gt;?</code>
 * <br>
 * This pattern matches the same, but only if the <code>lastName</code>
 * annotation has a <code>lang</code> attribute with value <code>en</code>.<br>
 * <code>&lt;lastName lang=&quot;(en|fr|de)&quot;&gt; ', ' &lt;initials&gt;? &lt;firstName&gt; &lt;initials&gt;?</code>
 * <br>
 * This pattern again matches the same, but only if the <code>lastName</code>
 * annotation has a <code>lang</code> attribute whose value matches the regular
 * expression pattern <code>en|fr|de</code>.<br>
 * 
 * @author sautter
 */
public class AnnotationPatternMatcher {
	
	/**
	 * Normalize an annotation pattern, i.e, remove line breaks and indents.
	 * @param pattern the annotation pattern to normalize
	 * @return the argument annotation pattern in its flattened out form
	 */
	public static String normalizePattern(String pattern) {
//		AnnotationPattern ap = getPattern(Gamta.INNER_PUNCTUATION_TOKENIZER, pattern);
		AnnotationPattern ap = getPattern(Gamta.getDefaultTokenizer(), pattern);
		return ap.toString(null);
	}
	
	/**
	 * Explode an annotation pattern, i.e., add line breaks and indents to
	 * improve readability.
	 * @param pattern the annotation pattern to explode
	 * @return the argument annotation pattern in a form readable for users
	 */
	public static String explodePattern(String pattern) {
//		AnnotationPattern ap = getPattern(Gamta.INNER_PUNCTUATION_TOKENIZER, pattern);
		AnnotationPattern ap = getPattern(Gamta.getDefaultTokenizer(), pattern);
		return ap.toString("");
	}
	
	private static Grammar grammar = new StandardGrammar();
	
	private static class AnnotationPattern {
		AnnotationPatternElement[] elements;
		AnnotationPattern(AnnotationPatternElement[] elements) {
			this.elements = elements;
		}
		public String toString() {
			return this.toString("");
		}
		public String toString(String indent) {
			StringBuffer sea = new StringBuffer();
			for (int e = 0; e < this.elements.length; e++) {
				if (e != 0)
					sea.append((indent == null) ? " " : "\r\n");
				sea.append(this.elements[e].toString(indent));
			}
			return sea.toString();
		}
	}
	
	private static class AnnotationPatternElement {
		String annotationType = null;
		TreeNodeAttributeSet annotationAttributes = null;
		String[] annotationAttributeNames = null;
		GPathExpression annotationTest = null;
		String patternLiteral = null;
		TokenSequence tokenLiteral = null;
		AnnotationPatternElement[] sequenceElements = null;
		AnnotationPatternElement[] alternativeElements = null;
		int minCount = 1;
		int maxCount = 1;
		ArrayList matchAttributeSetters = null;
		AnnotationPatternElement(TokenSequence tokenLiteral) {
			this.tokenLiteral = tokenLiteral;
		}
		AnnotationPatternElement(String patternLiteral) {
			this.patternLiteral = patternLiteral;
		}
		AnnotationPatternElement(String annotationType, TreeNodeAttributeSet annotationAttributes) {
			this.annotationType = annotationType;
			if (annotationAttributes.size() != 0) {
				this.annotationAttributes = annotationAttributes;
				this.annotationAttributeNames = this.annotationAttributes.getAttributeNames();
				String annotTest = this.annotationAttributes.getAttribute("test");
				if ((annotTest != null) && (!annotTest.startsWith("(") || !annotTest.endsWith(")")))
					annotTest = ("(" + annotTest + ")"); // let's substitute enclosing expression parenthesis only symmetrically
				this.annotationTest = GPathParser.parseExpression(annotTest);
			}
		}
		AnnotationPatternElement(AnnotationPatternElement[] subElements, boolean isSequence) {
			if (isSequence)
				this.sequenceElements = subElements;
			else this.alternativeElements = subElements;
		}
		void addMatchAttributeSetter(AnnotationPatternMatchAttributeSetter apmas) {
			if (this.matchAttributeSetters == null)
				this.matchAttributeSetters = new ArrayList();
			this.matchAttributeSetters.add(apmas);
		}
		Attributed getMatchAttributes(Attributed baseAttributes, Annotation matchAnnot) {
			Attributed matchAttributes = new AbstractAttributed();
			if (baseAttributes != null)
				matchAttributes.copyAttributes(baseAttributes);
			if (this.matchAttributeSetters != null) {
				QueriableAnnotation qMatchAnnot;
				if (matchAnnot instanceof QueriableAnnotation)
					qMatchAnnot = ((QueriableAnnotation) matchAnnot);
				else qMatchAnnot = new QueriableAnnotationWrapper(matchAnnot);
				for (int s = 0; s < this.matchAttributeSetters.size(); s++) {
					AnnotationPatternMatchAttributeSetter mas = ((AnnotationPatternMatchAttributeSetter) this.matchAttributeSetters.get(s));
					mas.setMatchAttribute(matchAttributes, qMatchAnnot);
				}
			}
			return matchAttributes;
		}
		public String toString(String indent) {
			
			//	assemble quantifier
			String quantifier;
			if (this.minCount == 0) {
				if (this.maxCount == 1)
					quantifier = "?";
				else if (this.maxCount == 0xFFFF)
					quantifier = "*";
				else quantifier = ("{0," + this.maxCount + "}");
			}
			else if (this.minCount == 1) {
				if (this.maxCount == 1)
					quantifier = "";
				else if (this.maxCount == 0xFFFF)
					quantifier = "+";
				else quantifier = ("{1," + this.maxCount + "}");
			}
			else {
				if (this.minCount == this.maxCount)
					quantifier = ("{" + this.minCount + "}");
				else if (this.maxCount == 0xFFFF)
					quantifier = ("{" + this.minCount + ",}");
				else quantifier = ("{" + this.minCount + "," + this.maxCount + "}");
			}
			
			//	add any match attribute setters
			StringBuffer matchAttributeSetters = new StringBuffer();
			if (this.matchAttributeSetters != null)
				for (int s = 0; s < this.matchAttributeSetters.size(); s++) {
					AnnotationPatternMatchAttributeSetter mas = ((AnnotationPatternMatchAttributeSetter) this.matchAttributeSetters.get(s));
					matchAttributeSetters.append(" @");
					if (mas.valueProcessingExpression != null)
						matchAttributeSetters.append(mas.valueProcessingExpressionString);
					matchAttributeSetters.append(':');
					matchAttributeSetters.append(mas.attributeName);
					if (this.maxCount > 1) {
						if (AnnotationPatternMatchAttributeSetter.USE_FIRST_VALUE.equals(mas.multiValueSeparator))
							matchAttributeSetters.append("[f]");
						else if (AnnotationPatternMatchAttributeSetter.USE_LAST_VALUE.equals(mas.multiValueSeparator))
							matchAttributeSetters.append("[l]");
						else if (AnnotationPatternMatchAttributeSetter.USE_LONGEST_VALUE.equals(mas.multiValueSeparator))
							matchAttributeSetters.append("[m]");
						else if (!" ".equals(mas.multiValueSeparator))
							matchAttributeSetters.append("['" + StringUtils.escapeChar(mas.multiValueSeparator, '\'', '\\') + "']");
					}
				}
			
			//	literal
			if (this.tokenLiteral != null) {
				String tokens = this.tokenLiteral.toString();
				StringBuffer escapedTokens = new StringBuffer();
				for (int c = 0; c < tokens.length(); c++) {
					char ch = tokens.charAt(c);
					if ((ch == '\\') || (ch == '\''))
						escapedTokens.append('\\');
					escapedTokens.append(ch);
				}
				if (indent == null)
					return ("'" + escapedTokens.toString() + "'" + quantifier + matchAttributeSetters.toString());
				else return (indent + "'" + escapedTokens.toString() + "'" + quantifier + matchAttributeSetters.toString());
			}
			
			//	pattern literal
			if (this.patternLiteral != null) {
				StringBuffer escapedPattern = new StringBuffer();
				for (int c = 0; c < this.patternLiteral.length(); c++) {
					char ch = this.patternLiteral.charAt(c);
					if ((ch == '\\') || (ch == '\''))
						escapedPattern.append('\\');
					escapedPattern.append(ch);
				}
				if (indent == null)
					return ("\"" + escapedPattern.toString() + "\"" + quantifier + matchAttributeSetters.toString());
				else return (indent + "\"" + escapedPattern.toString() + "\"" + quantifier + matchAttributeSetters.toString());
			}
			
			//	annotation
			if (this.annotationType != null) {
				if (indent == null)
					return ("<" + this.annotationType + ((this.annotationAttributes == null) ? "" : (" " + this.annotationAttributes.getAttributeValueString(grammar))) + ">" + quantifier + matchAttributeSetters.toString());
				else return (indent + "<" + this.annotationType + ((this.annotationAttributes == null) ? "" : (" " + this.annotationAttributes.getAttributeValueString(grammar))) + ">" + quantifier + matchAttributeSetters.toString());
			}
			
			//	sequence sub pattern
			if (this.sequenceElements != null) {
				StringBuffer sea = new StringBuffer();
				if (indent == null) {
					sea.append("(");
					for (int e = 0; e < this.sequenceElements.length; e++) {
						if (e != 0)
							sea.append(" ");
						sea.append(this.sequenceElements[e].toString(null));
					}
					sea.append(")");
				}
				else {
					sea.append(indent + "(");
					if ((this.sequenceElements.length == 1) && ((this.sequenceElements[0].tokenLiteral != null) || (this.sequenceElements[0].annotationType != null))) {
						sea.append(this.sequenceElements[0].toString(""));
						sea.append(")");
					}
					else {
						for (int e = 0; e < this.sequenceElements.length; e++) {
							sea.append("\r\n");
							sea.append(this.sequenceElements[e].toString(indent + "  "));
						}
						sea.append("\r\n" + indent + ")");
					}
				}
				sea.append(quantifier);
				sea.append(matchAttributeSetters);
				return sea.toString();
			}
			
			//	disjunction sub pattern
			if (this.alternativeElements != null) {
				StringBuffer sea = new StringBuffer();
				if (indent == null) {
					sea.append("(");
					for (int e = 0; e < this.alternativeElements.length; e++) {
						if (e != 0)
							sea.append("|");
						sea.append(this.alternativeElements[e].toString(null));
					}
					sea.append(")");
				}
				else {
					sea.append(indent + "(");
					for (int e = 0; e < this.alternativeElements.length; e++) {
						sea.append("\r\n");
						if (e != 0)
							sea.append(indent + "  " + "|\r\n");
						sea.append(this.alternativeElements[e].toString(indent + "  "));
					}
					sea.append("\r\n" + indent + ")");
				}
				sea.append(quantifier);
				sea.append(matchAttributeSetters);
				return sea.toString();
			}
			
			//	whatever else ...
			return "";
		}
	}
	
	private static class AnnotationPatternMatchAttributeSetter {
		static final String USE_FIRST_VALUE = "USE_FIRST_VALUE";
		static final String USE_LAST_VALUE = "USE_LAST_VALUE";
		static final String USE_LONGEST_VALUE = "USE_LONGEST_VALUE";
		private String attributeName;
		private GPathExpression valueProcessingExpression;
		private String valueProcessingExpressionString;
		private String multiValueSeparator;
		AnnotationPatternMatchAttributeSetter(String attributeName, GPathExpression valueProcessingExpression, String valueProcessingExpressionString, String multiValueSeparator) {
			this.attributeName = attributeName;
			this.valueProcessingExpression = valueProcessingExpression;
			this.valueProcessingExpressionString = valueProcessingExpressionString;
			this.multiValueSeparator = multiValueSeparator;
		}
		void setMatchAttribute(Attributed matchAttributes, QueriableAnnotation elementMatch) {
			
			//	get attribute value from current match
			String av = null;
			if (this.valueProcessingExpression == null)
				av = TokenSequenceUtils.concatTokens(elementMatch, true, true);
			else try {
				GPathObject gpo = GPath.evaluateExpression(this.valueProcessingExpression, elementMatch, null);
				if ((gpo != null) && gpo.asBoolean().value)
					av = gpo.asString().value;
			}
			catch (GPathException gpe) {/* let's not fail the whole match because of some pathological input */}
			
			//	nothing to work with
			if (av == null)
				return;
			
			//	get pre-existing value
			String exAv = ((String) matchAttributes.getAttribute(this.attributeName));
			
			//	last value requested, no pre-existing value, or primary match with no possible pre-existing value to observe
			if (USE_LAST_VALUE.equals(this.multiValueSeparator) || (exAv == null))
				matchAttributes.setAttribute(this.attributeName, av);
			
			//	first value requested, would have been set above
			else if (USE_FIRST_VALUE.equals(this.multiValueSeparator)) {}
			
			//	compare if maximum length value requested
			else if (USE_LONGEST_VALUE.equals(this.multiValueSeparator)) {
				if (exAv.length() < av.length())
					matchAttributes.setAttribute(this.attributeName, av);
			}
			
			//	concatenate values
			else matchAttributes.setAttribute(this.attributeName, (exAv + this.multiValueSeparator + av));
		}
	}
	
	/**
	 * A node of a match tree, detailing on matching result in debug mode.
	 * 
	 * @author sautter
	 */
	public static class MatchTreeNode {
		AnnotationPatternElement matched;
		Annotation match;
		LinkedList children = null;
		MatchTreeNode(AnnotationPatternElement matched, Annotation match) {
			this.matched = matched;
			this.match = match;
		}
		public String getPattern() {
			return this.matched.toString(null);
		}
		public Annotation getMatch() {
			return this.match;
		}
		void addChild(MatchTreeNode child) {
			if (this.children == null)
				this.children = new LinkedList();
			this.children.addLast(child);
		}
		public MatchTreeNode[] getChildren() {
			return ((MatchTreeNode[]) this.children.toArray(new MatchTreeNode[this.children.size()]));
		}
		public String toString() {
			return this.toString("");
		}
		public String toString(String indent) {
			StringBuffer sb = new StringBuffer(indent + this.matched.toString(null) + " ==> ");
			sb.append(AnnotationUtils.produceStartTag(this.match, new HashSet() {
				public boolean contains(Object o) {
					return ((matched.annotationAttributes != null) && matched.annotationAttributes.containsAttribute(o.toString()));
				}
			}, true));
			if (this.match.size() <= 15)
				sb.append(TokenSequenceUtils.concatTokens(this.match, true, true));
			else {
				sb.append(TokenSequenceUtils.concatTokens(this.match, 0, 7, true, true));
				sb.append(" ... ");
				sb.append(TokenSequenceUtils.concatTokens(this.match, (this.match.size() - 7), 7, true, true));
			}
			sb.append(AnnotationUtils.produceEndTag(this.match));
			if (this.children != null)
				for (Iterator cit = this.children.iterator(); cit.hasNext();) {
					sb.append("\r\n");
					sb.append(((MatchTreeNode) cit.next()).toString(indent + "  "));
				}
			return sb.toString();
		}
	}
	
	/**
	 * A leaf of a match tree, detailing on matching result in debug mode.
	 * 
	 * @author sautter
	 */
	public static class MatchTreeLeaf extends MatchTreeNode {
		MatchTreeLeaf(AnnotationPatternElement matched, Annotation match) {
			super(matched, match);
		}
		void addChild(MatchTreeNode child) {}
		public MatchTreeNode[] getChildren() {
			return null;
		}
	}
	
	/**
	 * Root node of a match tree, detailing on matching result in debug mode.
	 * 
	 * @author sautter
	 */
	public static class MatchTree extends MatchTreeNode {
		MatchTree(Annotation match) {
			super(null, match);
		}
		public String toString(String indent) {
			StringBuffer sb = new StringBuffer(this.match.toXML());
			for (Iterator cit = this.children.iterator(); cit.hasNext();) {
				sb.append("\r\n");
				sb.append(((MatchTreeNode) cit.next()).toString(indent + "  "));
			}
			return sb.toString();
		}
	}
	
	private static final Comparator matchTreeOrder = new Comparator() {
		public int compare(Object obj1, Object obj2) {
			MatchTree mt1 = ((MatchTree) obj1);
			MatchTree mt2 = ((MatchTree) obj2);
			return AnnotationUtils.compare(mt1.match, mt2.match);
		}
	};
	
	/**
	 * An annotation index provides quick access to individual annotations by
	 * type and start index to speed up matching. To assist garbage collection,
	 * instances of this class should be dissolved via the
	 * <code>dispose()</code> method once they are done with.
	 * 
	 * @author sautter
	 */
	public static class AnnotationIndex {
		HashMap index = new HashMap();
		CountingSet annotTypes = new CountingSet();
		AnnotationIndex defIndex;
		QueriableAnnotation data;
		HashSet dataRetrievedTypes;
		
		/** Constructor
		 */
		public AnnotationIndex() {
			this(null, null);
		}
		
		/**
		 * Constructor
		 * @param data the queriable annotation to index
		 * @param defIndex an additional annotation index that can provide
		 *            further annotations (may be null)
		 */
		public AnnotationIndex(QueriableAnnotation data, AnnotationIndex defIndex) {
			this.defIndex = defIndex;
			this.data = data;
			if (this.data != null)
				this.dataRetrievedTypes = new HashSet();
		}
		
		/**
		 * Index a single annotations.
		 * @param annot the annotation to index
		 */
		public void addAnnotation(Annotation annot) {
			this.addAnnotation(annot, annot.getType());
		}
		
		/**
		 * Index an array of annotations.
		 * @param annots the annotations to index
		 */
		public void addAnnotations(Annotation[] annots) {
			for (int a = 0; a < annots.length; a++)
				this.addAnnotation(annots[a]);
		}
		
		/**
		 * Index a single annotations for a custom type.
		 * @param annot the annotation to index
		 * @param type the type to index the annotation for
		 */
		public void addAnnotation(Annotation annot, String type) {
			if (this.getAnnotationList(type, annot.getStartIndex(), true).add(annot))
				this.annotTypes.add(type);
		}
		
		/**
		 * Index an array of annotations for a custom type.
		 * @param annots the annotations to index
		 * @param type the type to index the annotations for
		 */
		public void addAnnotations(Annotation[] annots, String type) {
			for (int a = 0; a < annots.length; a++)
				this.addAnnotation(annots[a], type);
		}
		
		/**
		 * Remove a single annotation from the index.
		 * @param annot the annotation to remove
		 */
		public void removeAnnotation(Annotation annot) {
			this.removeAnnotation(annot, annot.getType());
		}
		
		/**
		 * Remove a single annotation from the index for a custom type.
		 * @param annot the annotation to remove
		 * @param type the type to remove the annotation for
		 */
		public void removeAnnotation(Annotation annot, String type) {
			ArrayList al = this.getAnnotationList(type, annot.getStartIndex(), false);
			if (al == null)
				return;
			if (al.remove(annot))
				this.annotTypes.remove(type);
			if (al.isEmpty())
				this.index.remove("" + annot.getStartIndex() + " " + type);
		}
		
		/**
		 * Remove an array of annotations from the index.
		 * @param annots the annotations to remove
		 */
		public void removeAnnotations(Annotation[] annots) {
			for (int a = 0; a < annots.length; a++)
				this.removeAnnotation(annots[a]);
		}
		
		/**
		 * Remove an array of annotations from the index for a custom type.
		 * @param annots the annotations to remove
		 * @param type the type to remove the annotations for
		 */
		public void removeAnnotations(Annotation[] annots, String type) {
			for (int a = 0; a < annots.length; a++)
				this.removeAnnotation(annots[a], type);
		}
		
		/**
		 * Retrieve the types of annotations indexed in this annotation index.
		 * If the annotation index has an underlying document or an underlying
		 * default annotation index, the annotation types from these two are
		 * also included.
		 * @return an array holding the annotation types
		 */
		public String[] getAnnotationTypes() {
			TreeSet annotTypes = new TreeSet(this.annotTypes);
			if (this.defIndex != null)
				annotTypes.addAll(Arrays.asList(this.defIndex.getAnnotationTypes()));
			if (this.data != null)
				annotTypes.addAll(Arrays.asList(this.data.getAnnotationTypes()));
			return ((String[]) annotTypes.toArray(new String[annotTypes.size()]));
		}
		
		/**
		 * Retrieve annotations of a specific type starting at a specific
		 * index.
		 * @param type the type of the sought annotations
		 * @param startIndex the start index of the sought annotations
		 * @return an array holding the matching annotations
		 */
		public Annotation[] getAnnotations(String type, int startIndex) {
			ArrayList al = this.getAnnotationList(type, startIndex, false);
			if ((al == null) && (this.data != null) && this.dataRetrievedTypes.add(type)) {
				this.addAnnotations(this.data.getAnnotations(type), type);
				al = this.getAnnotationList(type, startIndex, false);
			}
			if (this.defIndex != null) {
				if (al == null)
					return this.defIndex.getAnnotations(type, startIndex);
				al = new ArrayList(al);
				ArrayList dal = this.defIndex.getAnnotationList(type, startIndex, false);
				if (dal != null)
					al.addAll(dal);
			}
			return ((al == null) ? new Annotation[0] : ((Annotation[]) al.toArray(new Annotation[al.size()])));
		}
		
		ArrayList getAnnotationList(String type, int startIndex, boolean create) {
			String alk = ("" + startIndex + " " + type);
			ArrayList al = ((ArrayList) this.index.get(alk));
			if ((al == null) && create) {
				al = new AnnotationIndexList();
				this.index.put(alk, al);
			}
			return al;
		}
		
		private static class AnnotationIndexList extends ArrayList {
			AnnotationIndexList() {
				super(2);
			}
			public boolean add(Object obj) {
				if (this.size() == 0)
					return super.add(obj);
				for (int a = 0; a < this.size(); a++) {
					if (((Annotation) obj).size() == ((Annotation) this.get(a)).size())
						return false;
				}
				return super.add(obj);
			}
		}
		
		/**
		 * Dispose of the annotation index.
		 * @param disposeDefIndex also dispose of the default index?
		 */
		public void dispose(boolean disposeDefIndex) {
			this.index.clear();
			if (this.data != null) {
				this.data = null;
				this.dataRetrievedTypes.clear();
				this.dataRetrievedTypes = null;
			}
			if (disposeDefIndex && (this.defIndex != null))
				this.defIndex.dispose(disposeDefIndex);
		}
	}
	
	/**
	 * An observing annotation index keeps track of updates to a wrapped
	 * mutable annotation. To prevent resource leaks, instances of this class
	 * have to be detached from the mutable annotation they observe via the
	 * <code>dispose()</code> method once they are done with.<br>
	 * Annotations added to the observed document after an instance of this
	 * class has been attached are indexed right away.<br>
	 * Updates to the document text clear the annotation index, but not any
	 * underlying default.
	 * 
	 * @author sautter
	 */
	public static class ObservingAnnotationIndex extends AnnotationIndex implements AnnotationListener, TokenSequenceListener {
		
		/** Constructor
		 * @param data the mutable annotation to observe and index
		 * @param defIndex an additional annotation index that can provide
		 *            further annotations (may be null)
		 */
		public ObservingAnnotationIndex(MutableAnnotation data, AnnotationIndex defIndex) {
			super(data, defIndex);
			data.addTokenSequenceListener(this);
			data.addAnnotationListener(this);
		}
		public void tokenSequenceChanged(TokenSequenceEvent change) {
			this.index.clear();
			this.dataRetrievedTypes.clear();
		}
		public void annotationAdded(QueriableAnnotation doc, Annotation annotation) {
			if (this.dataRetrievedTypes.contains(annotation.getType()))
				this.getAnnotationList(annotation.getType(), annotation.getStartIndex(), true).add(annotation);
		}
		public void annotationRemoved(QueriableAnnotation doc, Annotation annotation) {
			ArrayList al = this.getAnnotationList(annotation.getType(), annotation.getStartIndex(), false);
			if (al == null)
				return;
			for (int a = 0; a < al.size(); a++) {
				if (annotation.getAnnotationID().equals(((Annotation) al.get(a)).getAnnotationID()))
					al.remove(a--);
			}
			if (al.isEmpty())
				this.index.remove("" + annotation.getStartIndex() + " " + annotation.getType());
		}
		public void annotationTypeChanged(QueriableAnnotation doc, Annotation annotation, String oldType) {
			if (this.dataRetrievedTypes.contains(annotation.getType()))
				this.getAnnotationList(annotation.getType(), annotation.getStartIndex(), true).add(annotation);
			ArrayList al = this.getAnnotationList(oldType, annotation.getStartIndex(), false);
			if (al == null)
				return;
			for (int a = 0; a < al.size(); a++) {
				if (annotation.getAnnotationID().equals(((Annotation) al.get(a)).getAnnotationID()))
					al.remove(a--);
			}
			if (al.isEmpty())
				this.index.remove("" + annotation.getStartIndex() + " " + oldType);
		}
		public void annotationAttributeChanged(QueriableAnnotation doc, Annotation annotation, String attributeName, Object oldValue) {}
		public void dispose(boolean disposeDefIndex) {
			((MutableAnnotation) this.data).removeTokenSequenceListener(this);
			((MutableAnnotation) this.data).removeAnnotationListener(this);
			super.dispose(disposeDefIndex);
		}
	}
	
	private static class MatchTreeList extends LinkedList {
		private HashSet matchKeys = new HashSet();
		public boolean add(Object obj) {
			MatchTree mt = ((MatchTree) obj);
			if (this.matchKeys.add(mt.match.getType() + "@" + mt.match.getStartIndex() + "-" + mt.match.getEndIndex()))
				return super.add(obj);
			else return false;
		}
	}
	
	/**
	 * Attempt to match an annotation pattern against a queriable annotation.
	 * The children of the argument queriable annotation will be indexed
	 * automatically.
	 * @param data the queriable annotation to match against
	 * @param pattern the pattern to match
	 * @return an array holding the matches of the pattern
	 */
	public static Annotation[] getMatches(QueriableAnnotation data, String pattern) {
		return getMatches(data, null, pattern);
	}
	
	/**
	 * Attempt to match an annotation pattern against a queriable annotation.
	 * The children of the argument queriable annotation will be indexed
	 * automatically.
	 * @param data the queriable annotation to match against
	 * @param pattern the pattern to match
	 * @return an array holding the matches of the pattern
	 */
	public static MatchTree[] getMatchTrees(QueriableAnnotation data, String pattern) {
		return getMatchTrees(data, null, pattern);
	}
	
	/**
	 * Attempt to match an annotation pattern against a queriable annotation.
	 * The children of the argument queriable annotation will be indexed
	 * automatically. The argument annotation index can contain further
	 * annotations.
	 * @param data the queriable annotation to match against
	 * @param annotationIndex an index holding additional annotations belonging
	 *            to the token sequence underneath the argument queriable
	 *            annotation
	 * @param pattern the pattern to match
	 * @return an array holding the matches of the pattern
	 */
	public static Annotation[] getMatches(QueriableAnnotation data, AnnotationIndex annotationIndex, String pattern) {
		return getMatches(((TokenSequence) data), new AnnotationIndex(data, annotationIndex), pattern);
	}
	
	/**
	 * Attempt to match an annotation pattern against a queriable annotation.
	 * The children of the argument queriable annotation will be indexed
	 * automatically. The argument annotation index can contain further
	 * annotations.
	 * @param data the queriable annotation to match against
	 * @param annotationIndex an index holding additional annotations belonging
	 *            to the token sequence underneath the argument queriable
	 *            annotation
	 * @param pattern the pattern to match
	 * @return an array holding the matches of the pattern
	 */
	public static MatchTree[] getMatchTrees(QueriableAnnotation data, AnnotationIndex annotationIndex, String pattern) {
		return getMatchTrees(((TokenSequence) data), new AnnotationIndex(data, annotationIndex), pattern);
	}
	
	/**
	 * Attempt to match an annotation pattern against a token sequence with
	 * existing annotations.
	 * @param tokens the token sequence to match against
	 * @param annotationIndex an index holding the annotations belonging to the
	 *            token sequence
	 * @param pattern the pattern to match
	 * @return an array holding the matches of the pattern
	 */
	public static Annotation[] getMatches(TokenSequence tokens, AnnotationIndex annotationIndex, String pattern) {
		MatchTree[] matchTrees = getMatchTrees(tokens, annotationIndex, pattern);
		Annotation[] matches = new Annotation[matchTrees.length];
		for (int m = 0; m < matchTrees.length; m++)
			matches[m] = matchTrees[m].getMatch();
		return matches;
	}
	
	/**
	 * Attempt to match an annotation pattern against a token sequence with
	 * existing annotations.
	 * @param tokens the token sequence to match against
	 * @param annotationIndex an index holding the annotations belonging to the
	 *            token sequence
	 * @param pattern the pattern to match
	 * @return an array holding the matches of the pattern
	 */
	public static MatchTree[] getMatchTrees(TokenSequence tokens, AnnotationIndex annotationIndex, String pattern) {
		
		//	compile pattern
		AnnotationPattern ap = getPattern(tokens.getTokenizer(), pattern);
		
		//	resolve pattern literals into temporary annotations
		AnnotationIndex patternLiteralMatchIndex = new AnnotationIndex();
		HashSet indexedPatternLiteralMatchTypes = new HashSet();
		for (int e = 0; e < ap.elements.length; e++)
			indexPatternLiteralMatches(tokens, ap.elements[e], patternLiteralMatchIndex, indexedPatternLiteralMatchTypes);
		
		//	do matching
		LinkedList matches = new MatchTreeList();
		LinkedList matchTree = new LinkedList();
		LinkedList matchAttributes = new LinkedList();
		for (int s = 0; s < tokens.size(); s++) {
			step(tokens, s, s, ap.elements, 0, 0, annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
			matchTree.clear();
		}
		
		//	sort matches
		MatchTree[] mts = ((MatchTree[]) matches.toArray(new MatchTree[matches.size()]));
		Arrays.sort(mts, matchTreeOrder);
		
		//	finally ...
		return mts;
	}
	
	private static void indexPatternLiteralMatches(TokenSequence tokens, AnnotationPatternElement ape, AnnotationIndex patternLiteralMatchIndex, HashSet indexedPatternLiteralMatchTypes) {
		
		//	pattern literal
		if (ape.patternLiteral != null) {
			String patternLiteralMatchType = ("regEx" + ape.patternLiteral.hashCode());
			if (indexedPatternLiteralMatchTypes.add(patternLiteralMatchType)) {
				Annotation[] plms = Gamta.extractAllMatches(tokens, ape.patternLiteral, true, false);
				patternLiteralMatchIndex.addAnnotations(plms, patternLiteralMatchType);
			}
		}
		
		//	sub pattern
		else if (ape.sequenceElements != null) {
			for (int e = 0; e < ape.sequenceElements.length; e++)
				indexPatternLiteralMatches(tokens, ape.sequenceElements[e], patternLiteralMatchIndex, indexedPatternLiteralMatchTypes);
		}
		
		//	disjunction
		else if (ape.alternativeElements != null) {
			for (int e = 0; e < ape.alternativeElements.length; e++)
				indexPatternLiteralMatches(tokens, ape.alternativeElements[e], patternLiteralMatchIndex, indexedPatternLiteralMatchTypes);
		}
	}
	
	private static void step(TokenSequence tokens, int matchStart, int matchFrom, AnnotationPatternElement[] pattern, int elementIndex, int elementMatchCount, AnnotationIndex annotationIndex, AnnotationIndex patternLiteralMatchIndex, LinkedList matches, LinkedList matchTree, LinkedList matchAttributes) {
		
		//	end of pattern reached, we have a match
		if (pattern.length == elementIndex) {
			if (matchStart < matchFrom) {
				Annotation match = Gamta.newAnnotation(tokens, null, matchStart, (matchFrom-matchStart));
				if (matchAttributes.size() != 0) // catch empty match
					match.copyAttributes((Attributed) matchAttributes.getLast());
				MatchTree mt = new MatchTree(match);
				for (Iterator mtit = matchTree.iterator(); mtit.hasNext();)
					mt.addChild((MatchTreeNode) mtit.next());
				matches.add(mt);
			}
			return;
		}
		
		//	we can do without (further) matches of current element
		if (pattern[elementIndex].minCount <= elementMatchCount)
			step(tokens, matchStart, matchFrom, pattern, (elementIndex+1), 0, annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
		
		//	we cannot do with any further matches of current element
		if (pattern[elementIndex].maxCount <= elementMatchCount)
			return;
		
		//	literal
		if (pattern[elementIndex].tokenLiteral != null) {
			if (TokenSequenceUtils.startsWith(tokens, pattern[elementIndex].tokenLiteral, matchFrom)) {
				Annotation mAnnot = Gamta.newAnnotation(tokens, "literal", matchFrom, pattern[elementIndex].tokenLiteral.size());
				matchTree.addLast(new MatchTreeLeaf(pattern[elementIndex], mAnnot));
				matchAttributes.addLast(pattern[elementIndex].getMatchAttributes((matchAttributes.isEmpty() ? null : ((Attributed) matchAttributes.getLast())), mAnnot));
				step(tokens, matchStart, (matchFrom + pattern[elementIndex].tokenLiteral.size()), pattern, elementIndex, (elementMatchCount + 1), annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
				matchTree.removeLast();
				matchAttributes.removeLast();
			}
			return;
		}
		
		//	pattern literal
		if (pattern[elementIndex].patternLiteral != null) {
			Annotation[] annots = patternLiteralMatchIndex.getAnnotations(("regEx" + pattern[elementIndex].patternLiteral.hashCode()), matchFrom);
			for (int a = 0; a < annots.length; a++) {
				matchTree.addLast(new MatchTreeLeaf(pattern[elementIndex], annots[a]));
				matchAttributes.addLast(pattern[elementIndex].getMatchAttributes((matchAttributes.isEmpty() ? null : ((Attributed) matchAttributes.getLast())), annots[a]));
				step(tokens, matchStart, (matchFrom + annots[a].size()), pattern, elementIndex, (elementMatchCount + 1), annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
				matchTree.removeLast();
				matchAttributes.removeLast();
			}
			return;
		}
		
		//	annotation
		if (pattern[elementIndex].annotationType != null) {
			Annotation[] annots = annotationIndex.getAnnotations(pattern[elementIndex].annotationType, matchFrom);
			for (int a = 0; a < annots.length; a++) {
				boolean filterMatch = true;
				if (pattern[elementIndex].annotationTest != null) try {
					QueriableAnnotation qAnnot;
					if (annots[a] instanceof QueriableAnnotation)
						qAnnot = ((QueriableAnnotation) annots[a]);
					else qAnnot = new QueriableAnnotationWrapper(annots[a]);
					filterMatch = GPath.evaluateExpression(pattern[elementIndex].annotationTest, qAnnot, null).asBoolean().value;
				} catch (GPathException gpe) {}
				if (filterMatch && pattern[elementIndex].annotationAttributeNames != null)
					for (int n = 0; n < pattern[elementIndex].annotationAttributeNames.length; n++) {
						if ("test".equals(pattern[elementIndex].annotationAttributeNames[n]))
							continue; // we're testing this one above
						String mValue = pattern[elementIndex].annotationAttributes.getAttribute(pattern[elementIndex].annotationAttributeNames[n]);
						if (mValue == null)
							continue;
						Object aValue = annots[a].getAttribute(pattern[elementIndex].annotationAttributeNames[n]);
						if ((aValue == null) || (!"".equals(mValue) && !"*".equals(mValue) && !mValue.equals(aValue))) {
							filterMatch = false;
							break;
						}
					}
				if (filterMatch) {
					matchTree.addLast(new MatchTreeLeaf(pattern[elementIndex], annots[a]));
					matchAttributes.addLast(pattern[elementIndex].getMatchAttributes((matchAttributes.isEmpty() ? null : ((Attributed) matchAttributes.getLast())), annots[a]));
					step(tokens, matchStart, (matchFrom + annots[a].size()), pattern, elementIndex, (elementMatchCount + 1), annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
					matchTree.removeLast();
					matchAttributes.removeLast();
				}
			}
			return;
		}
		
		//	sequence sub pattern
		if (pattern[elementIndex].sequenceElements != null) {
			LinkedList subMatches = new LinkedList();
			LinkedList subMatchTree = new LinkedList();
			LinkedList subMatchAttributes = new LinkedList();
			step(tokens, matchFrom, matchFrom, pattern[elementIndex].sequenceElements, 0, 0, annotationIndex, patternLiteralMatchIndex, subMatches, subMatchTree, subMatchAttributes);
			for (Iterator smtit = subMatches.iterator(); smtit.hasNext();) {
				MatchTree smt = ((MatchTree) smtit.next());
				Annotation mAnnot = smt.getMatch();
				mAnnot.changeTypeTo("sequence");
				MatchTreeNode mtn = new MatchTreeNode(pattern[elementIndex], mAnnot);
				for (Iterator cit = smt.children.iterator(); cit.hasNext();)
					mtn.addChild((MatchTreeNode) cit.next());
				matchTree.addLast(mtn);
				if (matchAttributes.size() != 0)
					mAnnot.copyAttributes((Attributed) matchAttributes.getLast());
				matchAttributes.addLast(pattern[elementIndex].getMatchAttributes(mAnnot, mAnnot));
				step(tokens, matchStart, (matchFrom + smt.getMatch().size()), pattern, elementIndex, (elementMatchCount + 1), annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
				matchTree.removeLast();
				matchAttributes.removeLast();
			}
		}
		
		//	disjunction sub pattern
		if (pattern[elementIndex].alternativeElements != null) {
			AnnotationPatternElement[] subPattern = {null};
			LinkedList subMatches = new LinkedList();
			LinkedList subMatchTree = new LinkedList();
			LinkedList subMatchAttributes = new LinkedList();
			for (int a = 0; a < pattern[elementIndex].alternativeElements.length; a++) {
				subPattern[0] = pattern[elementIndex].alternativeElements[a];
				step(tokens, matchFrom, matchFrom, subPattern, 0, 0, annotationIndex, patternLiteralMatchIndex, subMatches, subMatchTree, subMatchAttributes);
				for (Iterator smtit = subMatches.iterator(); smtit.hasNext();) {
					MatchTree smt = ((MatchTree) smtit.next());
					Annotation mAnnot = smt.getMatch();
					mAnnot.changeTypeTo("alternative");
					MatchTreeNode mtn = new MatchTreeNode(pattern[elementIndex], mAnnot);
					for (Iterator cit = smt.children.iterator(); cit.hasNext();)
						mtn.addChild((MatchTreeNode) cit.next());
					matchTree.addLast(mtn);
					if (matchAttributes.size() != 0)
						mAnnot.copyAttributes((Attributed) matchAttributes.getLast());
					matchAttributes.addLast(pattern[elementIndex].getMatchAttributes(mAnnot, mAnnot));
					step(tokens, matchStart, (matchFrom + smt.getMatch().size()), pattern, elementIndex, (elementMatchCount + 1), annotationIndex, patternLiteralMatchIndex, matches, matchTree, matchAttributes);
					matchTree.removeLast();
					matchAttributes.removeLast();
				}
				subMatches.clear();
			}
		}
	}
	
	private static class QueriableAnnotationWrapper extends GenericAnnotationWrapper implements QueriableAnnotation {
		private static QueriableAnnotation[] qaDummy = new QueriableAnnotation[0];
		QueriableAnnotationWrapper(Annotation data) {
			super(data);
		}
		public int getAbsoluteStartIndex() {
			return this.getStartIndex();
		}
		public int getAbsoluteStartOffset() {
			return this.getStartOffset();
		}
		public QueriableAnnotation getAnnotation(String id) {
			return null;
		}
		public QueriableAnnotation[] getAnnotations() {
			return qaDummy;
		}
		public QueriableAnnotation[] getAnnotations(String type) {
			return qaDummy;
		}
		public QueriableAnnotation[] getAnnotationsSpanning(int startIndex, int endIndex) {
			return qaDummy;
		}
		public QueriableAnnotation[] getAnnotationsSpanning(String type, int startIndex, int endIndex) {
			return qaDummy;
		}
		public QueriableAnnotation[] getAnnotationsOverlapping(int startIndex, int endIndex) {
			return qaDummy;
		}
		public QueriableAnnotation[] getAnnotationsOverlapping(String type, int startIndex, int endIndex) {
			return qaDummy;
		}
		public String[] getAnnotationTypes() {
			return new String[0];
		}
		public String getAnnotationNestingOrder() {
			return "";
		}
	}
	
//	private static HashMap patternCache = new HashMap();
	private static Map patternCache = Collections.synchronizedMap(new LinkedHashMap(16, 0.9f, true) {
		protected boolean removeEldestEntry(Entry eldest) {
			return (this.size() > 256);
		}
	});
	private static AnnotationPattern getPattern(Tokenizer tokenizer, String pattern) {
		AnnotationPattern ap = ((AnnotationPattern) patternCache.get(pattern));
		if (ap == null) try {
			ap = parsePattern(tokenizer, pattern);
			patternCache.put(pattern, ap);
		}
		catch (IOException ioe) {
			if (ioe instanceof AnnotationPatternParseException)
				throw new PatternSyntaxException(ioe.getMessage(), pattern, ((AnnotationPatternParseException) ioe).getIndex());
			else return null; // never gonna happen, but Java don't know ...
		}
		return ap;
	}
	
	private static AnnotationPattern parsePattern(final Tokenizer tokenizer, String pattern) throws IOException {
		PatternReader pr = new PatternReader(pattern);
		LinkedList elements = new LinkedList();
		for (AnnotationPatternElement ape; ((ape = cropNext(tokenizer, pr)) != null);)
			elements.add(ape);
		return new AnnotationPattern(((AnnotationPatternElement[]) elements.toArray(new AnnotationPatternElement[elements.size()])));
	}
	
	private static AnnotationPatternElement cropNext(Tokenizer tokenizer, PatternReader pr) throws IOException {
		pr.skipSpace();
		int ch = pr.peek();
		if (ch == -1)
			return null;
		
		//	crop next element on this nesting level
		AnnotationPatternElement ape;
		if (ch == '\'')
			ape = cropLiteral(tokenizer, pr);
		else if (ch == '"')
			ape = cropPattern(pr);
		else if (ch == '<')
			ape = cropAnnotation(pr);
		else if (ch == '(') {
			LinkedList spes = new LinkedList();
			boolean isSequence = cropSubPattern(tokenizer, pr, spes);
			ape = new AnnotationPatternElement(((AnnotationPatternElement[]) spes.toArray(new AnnotationPatternElement[spes.size()])), isSequence);
		}
		else throw new AnnotationPatternParseException(("Unexpected character: " + ((char) ch)), (pr.readSoFar() - 1));
		
		//	crop associated quantifier, if any
		pr.skipSpace();
		int quantifierStart = pr.readSoFar();
		if ("?*+{".indexOf(pr.peek()) != -1) {
			int quantifier = cropQuantifier(pr);
			ape.minCount = quantifier >>> 16;
			ape.maxCount = quantifier & 0x0000FFFF;
			if (ape.minCount < 0)
				throw new AnnotationPatternParseException("Invalid quantifier (min less than 0)", quantifierStart);
			if (ape.maxCount < 1)
				throw new AnnotationPatternParseException("Invalid quantifier (max less than 1)", quantifierStart);
			if (ape.maxCount < ape.minCount)
				throw new AnnotationPatternParseException("Invalid quantifier (max less than min)", quantifierStart);
		}
		
		//	crop associated match attribute label(s), if any
		pr.skipSpace();
		while (pr.peek() == '@') {
			ape.addMatchAttributeSetter(cropMatchAttributeSetter(pr));
			pr.skipSpace();
		}
		
		//	finally
		return ape;
	}
	
	private static AnnotationPatternElement cropLiteral(Tokenizer tokenizer, PatternReader pr) throws IOException {
		pr.read(); // consume leading high comma
		pr.skipSpace();
		
		//	read string literal
		String literal = cropString(pr, '\'', "high comma");
		
		//	tokenize and wrap literal
		return new AnnotationPatternElement(new CharTokenSequence(literal, tokenizer));
	}
	
	private static AnnotationPatternElement cropPattern(PatternReader pr) throws IOException {
		pr.read(); // consume leading double quote
		pr.skipSpace();
		
		//	read pattern string
		String pattern = cropString(pr, '"', "double quote");
		
		//	wrap character level pattern
		return new AnnotationPatternElement(pattern);
	}
	
	private static String cropString(PatternReader pr, char quoter, String quoterName) throws IOException {
		StringBuffer sb = new StringBuffer();
		boolean escaped = false;
		while (true) {
			int ch = pr.read();
			if (ch == -1)
				throw new AnnotationPatternParseException(("Expected closing " + quoterName), pr.readSoFar());
			if (escaped) {
				sb.append((char) ch);
				escaped = false;
			}
			else if (ch == '\\')
				escaped = true;
			else if (ch == quoter)
				break;
			else sb.append((char) ch);
		}
		return sb.toString();
	}
	
	private static AnnotationPatternElement cropAnnotation(PatternReader pr) throws IOException {
		StringBuffer annotBuffer = new StringBuffer();
		
		//	read XML tag
		boolean quoted = false;
		while (pr.peek() != -1) {
			int ch = pr.read();
			if (quoted) {
				annotBuffer.append((char) ch);
				if (ch == '"')
					quoted = false;
			}
			else {
				annotBuffer.append((char) ch);
				if (ch == '"')
					quoted = true;
				else if (ch == '>')
					break;
			}
		}
		
		//	parse and return XML tag
		String annotString = annotBuffer.toString();
		if (!annotString.endsWith(">"))
			throw new AnnotationPatternParseException("Expected closing angle bracket", (pr.readSoFar() - annotString.length()));
		String annotType = grammar.getType(annotString);
		if (!AnnotationUtils.isValidAnnotationType(annotType))
			throw new AnnotationPatternParseException(("Invalid annotation type: " + annotType), (pr.readSoFar() - annotString.length()));
		return new AnnotationPatternElement(annotType, TreeNodeAttributeSet.getTagAttributes(annotString, grammar));
	}
	
	private static boolean cropSubPattern(Tokenizer tokenizer, PatternReader pr, LinkedList subPatternElements) throws IOException {
		pr.read(); // consume leading bracket
		pr.skipSpace();
		
		//	read sub pattern recursively
		boolean isSequence = true;
		while (true) {
			int ch = pr.peek();
			if (ch == -1)
				throw new AnnotationPatternParseException("Expected closing bracket", pr.readSoFar());
			if (ch == ')') {
				pr.read();
				break;
			}
			else if (ch == '|') {
				pr.read();
				isSequence = false;
			}
			else {
				AnnotationPatternElement ape = cropNext(tokenizer, pr);
				if (ape != null)
					subPatternElements.add(ape);
				pr.skipSpace();
			}
		}
		return isSequence;
	}
	
	private static int cropQuantifier(PatternReader pr) throws IOException {
		pr.skipSpace();
		int ch = pr.read();
		if (ch == '?')
			return 0x00000001;
		else if (ch == '*')
			return 0x0000FFFF;
		else if (ch == '+')
			return 0x0001FFFF;
		
		StringBuffer quantifierBuffer = new StringBuffer();
		while (true) {
			ch = pr.read();
			if (ch == -1)
				throw new AnnotationPatternParseException("Expected closing curly bracket", pr.readSoFar());
			if (ch == '}')
				break;
			else quantifierBuffer.append((char) ch);
		}
		
		String quantifier = quantifierBuffer.toString().trim();
		String[] quantifierParts = quantifier.split("\\s*\\,\\s*", 2);
		if ((quantifierParts.length == 1) || (quantifierParts.length == 2)) try {
			int min = Integer.parseInt(quantifierParts[0]);
			int max = ((quantifierParts.length == 1) ? min : ((quantifierParts[1].length() == 0) ? 0xFFFF : Integer.parseInt(quantifierParts[1])));
			return ((min << 16) + max);
		} catch (NumberFormatException nfe) {}
		throw new AnnotationPatternParseException(("Invalid quantifier: " + quantifier), (pr.readSoFar() - quantifierBuffer.length()));
	}
	
	private static AnnotationPatternMatchAttributeSetter cropMatchAttributeSetter(PatternReader pr) throws IOException {
		int matchAttributeSetterStart = pr.readSoFar();
		
		//	consume leading '@'
		pr.read();
		
		//	anything to work with?
		if (":(".indexOf(pr.peek()) == -1)
			throw new AnnotationPatternParseException("Invalid character '" + ((char) pr.read()) + "', expected colon or opening parenthesis", matchAttributeSetterStart);
		
		//	crop value processing expression, if any
		//	TODO use GPathParser.cropExpression() once we've re-implemented the latter to do single-pass stream parsing
		GPathExpression valueProcessingExpression = null;
		String valueProcessingExpressionString = null;
		if (pr.peek() == '(') {
			StringBuffer vpe = new StringBuffer();
			char quoter = ((char) 0);
			while (pr.peek() != -1) {
				char ch = ((char) pr.read());
				vpe.append(ch);
				if (quoter != 0) {
					if (ch == quoter)
						quoter = ((char) 0);
				}
				else if ((ch == '\'') || ch == '"')
					quoter = ch;
				else if ((ch == ')') && (pr.peek() == ':'))
					break;
			}
			try {
				valueProcessingExpression = GPathParser.parseExpression(vpe.toString());
				valueProcessingExpressionString = vpe.toString();
			}
			catch (GPathSyntaxException gpse) {
				throw new AnnotationPatternParseException("Invalid value processing expression '" + vpe.toString() + "'", (pr.readSoFar() - vpe.length()));
			}
		}
		
		//	crop attribute name
		//	TODO use GPathParser.cropQName() once we've re-implemented the latter to do single-pass stream parsing
		if (pr.peek() != ':')
			throw new AnnotationPatternParseException(("Invalid character '" + ((char) pr.read()) + "', expected colon"), (pr.readSoFar() - 1));
		pr.read(); // consume colon
		StringBuffer an = new StringBuffer();
		while (pr.peek() > ' ') {
			if ((pr.peek() < 128) && (Character.isLetterOrDigit((char) pr.peek()) || ("_-:".indexOf(pr.peek()) != -1)))
				an.append((char) pr.read());
			else break;
		}
		String attributeName = an.toString();
		if (!AnnotationUtils.isValidAnnotationType(attributeName))
			throw new AnnotationPatternParseException(("Invalid annotation type: " + attributeName), (pr.readSoFar() - attributeName.length()));
		
		//	crop multi-value strategy, if any
		String multiValueSeparator = " "; // use space as separator by default
		if (pr.peek() == '[') {
			pr.read(); // consume opening square bracket
			if (pr.peek() == '\'') {
				pr.read(); // consume opening high comma
				multiValueSeparator = cropString(pr, '\'', "high comma");
				if (pr.peek() == ']')
					pr.read(); // consume closing square bracket
				else throw new AnnotationPatternParseException(("Invalid character '" + ((char) pr.peek()) + "', expected closing square bracket"), pr.readSoFar());
			}
			else {
				String mvs = cropString(pr, ']', "closing square bracket").trim();
				if ("f".equals(mvs))
					multiValueSeparator = AnnotationPatternMatchAttributeSetter.USE_FIRST_VALUE;
				else if ("l".equals(mvs))
					multiValueSeparator = AnnotationPatternMatchAttributeSetter.USE_LAST_VALUE;
				else if ("m".equals(mvs))
					multiValueSeparator = AnnotationPatternMatchAttributeSetter.USE_LONGEST_VALUE;
				else multiValueSeparator = mvs;
			}
		}
		
		//	finally ...
		return new AnnotationPatternMatchAttributeSetter(attributeName, valueProcessingExpression, valueProcessingExpressionString, multiValueSeparator);
	}
	
	private static class PatternReader extends FilterReader {
		private int lookaheadChar = -1;
		int readSoFar;
		PatternReader(String pattern) throws IOException {
			super(new StringReader(pattern));
		}
		public int read() throws IOException {
			this.lookaheadChar = -1;
			int r = super.read();
			if (r != -1)
				this.readSoFar++;
			return r;
		}
		public int read(char[] cbuf, int off, int len) throws IOException {
			this.lookaheadChar = -1;
			int r = super.read(cbuf, off, len);
			if (r != -1)
				this.readSoFar += r;
			return r;
		}
		int peek() throws IOException {
			if (this.lookaheadChar != -1)
				return this.lookaheadChar;
			this.in.mark(1);
			this.lookaheadChar = this.in.read();
			this.in.reset();
			return this.lookaheadChar;
		}
		void skipSpace() throws IOException {
			while ((this.peek() < 33) && (this.peek() != -1))
				this.read();
		}
		int readSoFar() {
			return this.readSoFar;
		}
	}
	
	private static class AnnotationPatternParseException extends IOException {
		private int index;
		AnnotationPatternParseException(String message, int index) {
			super(message);
			this.index = index;
		}
		int getIndex() {
			return index;
		}
	}
//	
//	//	PARSING AND MATCHING TEST
//	public static void main(String[] args) throws Exception {
//		String pattern;
//		pattern = "<stopWord>* <lastName> @:lastName ','? <firstName> @:firstName";
////		pattern = "(<stopWord>* <lastName>{1,2}){2,} ','? <firstName>";
////		pattern = "(<a>{1,3}|(<b><c>{2,4})){2,} ','? <d> (','? <e>)?";
////		pattern = "'Mr.'? (<fn>|<in>)+ (<i>* <ln>)+ (','? <a>)?";
////		pattern = "'Mr.'? (<fn>|\"[A-Z]\\\\.\")+@:firstName (<i>* <ln>)+@:lastName[', '] (','? <a>)?";
////		pattern = "'Mr.'? (<fn>@:fn|\"[A-Z]\\\\.\")+@(./@fn):firstName (<i>* <ln>)+@:lastName (','? <a>)?";
//		pattern = "'Mr.'? (<fn>|\"[A-Z]\\\\.\"@:initial)+@:firstName (<i>* <ln>)+@:lastName (','? <a test=\"./@something\" type=\"\">)?";
////		pattern = "<bibRefCitation>\n')'?\n<number>";
//		AnnotationPattern ap = getPattern(Gamta.INNER_PUNCTUATION_TOKENIZER, pattern);
//		System.out.println(ap.toString());
//		MutableAnnotation doc = SgmlDocumentReader.readDocument(new StringReader("Mr. <fn test=\"test\">Tommy</fn> <in>F.</in> <ln><fn>Lee</fn></ln> <i>van</i> <i>den</i> <ln>Jones</ln>, <a>Jr.</a>"));
//		MatchTree[] mts = getMatchTrees(doc, ap.toString());
//		for (int a = 0; a < mts.length; a++)
//			System.out.println(mts[a].toString());
//		Annotation[] ans = getMatches(doc, ap.toString());
//		for (int a = 0; a < ans.length; a++)
//			System.out.println(ans[a].toXML());
////		AnnotationUtils.writeXML(doc, new OutputStreamWriter(System.out));
////		System.out.println();
////		MutableAnnotation doc = SgmlDocumentReader.readDocument(new StringReader("<d>n. <l a=\"y\"><l>ln-a</l> <l a=\"x\">ln-b</l></l>, <i>i-i</i>, <l><l>ln-a</l> ln-b</l> <i>i-i</i>, Jr. n 2345.</d>"));
////		AnnotationUtils.writeXML(doc, new OutputStreamWriter(System.out));
////		Annotation[] ans = getMatches(doc, "<l a=\"(a|b|x)\"> ','? <i> ', Jr'?");
////		for (int a = 0; a < ans.length; a++) {
////			System.out.println(ans[a].toXML());
////		}
//	}
	
	/** the name of the attribute holding the list of source elements of enumerations, namely 'elements' */
	public static final String ENUMERATION_ELEMENTS_ATTRIBUTE = "elements";
	
	private static final boolean DEBUG_ENUMERATION_ASSEMBLY = false;
	
	/**
	 * Tag enumerations of the argument elements, i.e., sequences of argument
	 * element annotations separated by any single lookup match of the argument
	 * dictionary of separators.
	 * The returned enumeration annotations hold the respective source elements
	 * in a list stored in their 'elements' attribute.
	 * @param tokens the token sequence to find the enumerations in
	 * @param elements the enumeration elements
	 * @param separators the enumeration separators
	 * @return an array holding the enumerations
	 */
	public static Annotation[] getEnumerations(TokenSequence tokens, Annotation[] elements, Dictionary separators) {
		return getEnumerations(tokens, elements, elements, elements, separators, separators);
	}
	
	/**
	 * Tag enumerations of the argument elements, i.e., sequences of argument
	 * element annotations separated by any single lookup match of the argument
	 * dictionaries of separators. The separator between the last two elements
	 * comes from the dictionary of dedicated end separators, which facilitates
	 * allowing a separator like ', and' only at the very end of enumerations.
	 * The returned enumeration annotations hold the respective source elements
	 * in a list stored in their 'elements' attribute.
	 * @param tokens the token sequence to find the enumerations in
	 * @param elements the enumeration elements
	 * @param separators the enumeration separators
	 * @param endSeparators the enumeration end separators
	 * @return an array holding the enumerations
	 */
	public static Annotation[] getEnumerations(TokenSequence tokens, Annotation[] elements, Dictionary separators, Dictionary endSeparators) {
		return getEnumerations(tokens, elements, elements, elements, separators, endSeparators);
	}
	
	/**
	 * Tag enumerations of the argument elements, i.e., sequences of argument
	 * element annotations separated by any single lookup match of the argument
	 * dictionary of separators. The first element is exclusively taken from
	 * the argument start elements; the argument end elements are considered
	 * only as the terminating elements of enumerations. This facilitates
	 * restricting elements like 'et al.' to the very end of an enumeration of
	 * authors, for instance.
	 * The returned enumeration annotations hold the respective source elements
	 * in a list stored in their 'elements' attribute.
	 * @param tokens the token sequence to find the enumerations in
	 * @param startElements the enumeration start elements
	 * @param elements the enumeration elements
	 * @param endElements the enumeration end elements
	 * @param separators the enumeration separators
	 * @return an array holding the enumerations
	 */
	public static Annotation[] getEnumerations(TokenSequence tokens, Annotation[] startElements, Annotation[] elements, Annotation[] endElements, Dictionary separators) {
		return getEnumerations(tokens, startElements, elements, endElements, separators, separators);
	}
	
	/**
	 * Tag enumerations of the argument elements, i.e., sequences of argument
	 * element annotations separated by any single lookup match of the argument
	 * dictionaries of separators. The separator between the last two elements
	 * comes from the dictionary of dedicated end separators, which facilitates
	 * allowing a separator like ', and' only at the very end of enumerations.
	 * The first element is exclusively taken from the argument start elements;
	 * the argument end elements are considered only as the terminating elements
	 * of enumerations. This facilitates restricting elements like 'et al.' to
	 * the very end of an enumeration of authors, for instance.
	 * The returned enumeration annotations hold the respective source elements
	 * in a list stored in their 'elements' attribute.
	 * @param tokens the token sequence to find the enumerations in
	 * @param elements the enumeration elements
	 * @param separators the enumeration separators
	 * @param endSeparators the enumeration end separators
	 * @return an array holding the enumerations
	 */
	public static Annotation[] getEnumerations(TokenSequence tokens, Annotation[] startElements, Annotation[] elements, Annotation[] endElements, Dictionary separators, Dictionary endSeparators) {
		
		//	index enumeration parts
		AnnotationIndex enumPartIndex = new AnnotationIndex();
		enumPartIndex.addAnnotations(startElements, "enumeration");
		enumPartIndex.addAnnotations(elements, "cElement");
		enumPartIndex.addAnnotations(endElements, "eElement");
		
		//	index separators
		Annotation[] enumSeparators = Gamta.extractAllContained(tokens, separators, false, true);
		enumPartIndex.addAnnotations(enumSeparators, "separator");
		if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println("Separators are " + Arrays.toString(enumSeparators));
		Annotation[] enumEndSeparators = Gamta.extractAllContained(tokens, endSeparators, false, true);
		enumPartIndex.addAnnotations(enumEndSeparators, "endSeparator");
		if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println("End separators are " + Arrays.toString(enumEndSeparators));
		
		//	assemble enumerations
		ArrayList enums = new ArrayList();
		HashSet enumElementIdStrings = new HashSet();
		
		//	mark individual seed elements as enumerations, initializing element list
		for (int e = 0; e < startElements.length; e++) {
			Annotation enumMatch = Gamta.newAnnotation(tokens, null, startElements[e].getStartIndex(), startElements[e].size());
			ArrayList elementList = new ArrayList();
			elementList.add(startElements[e]);
			enumMatch.setAttribute(ENUMERATION_ELEMENTS_ATTRIBUTE, elementList);
			if (enumElementIdStrings.add(getElementIDs(enumMatch))) {
				enums.add(enumMatch);
				if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println("Added starting enumeration " + enumMatch.toXML());
			}
		}
		
		//	expand seed enumerations
		ArrayList newEnums = new ArrayList();
		do {
			if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println("Attempting expansion");
			newEnums.clear();
			MatchTree[] enumMatches = getMatchTrees(tokens, enumPartIndex, "<enumeration> <separator> <cElement>");
			if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println(" - got " + enumMatches.length + " expanded matches");
			for (int l = 0; l < enumMatches.length; l++) {
				Annotation enumMatch = enumMatches[l].getMatch();
				addElementList(enumMatches[l], enumMatch);
				if (enumElementIdStrings.add(getElementIDs(enumMatch))) {
					newEnums.add(enumMatch);
					if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println("Added enumeration " + enumMatch.toXML());
				}
			}
			enumPartIndex.removeAnnotations(((Annotation[]) enums.toArray(new Annotation[enums.size()])), "enumeration");
			if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println(" - got " + newEnums.size() + " new matches");
			enums.addAll(newEnums);
			enumPartIndex.addAnnotations(((Annotation[]) newEnums.toArray(new Annotation[newEnums.size()])), "enumeration");
		} while (newEnums.size() != 0);
		
		//	finalize enumerations
		enumPartIndex.addAnnotations(((Annotation[]) enums.toArray(new Annotation[enums.size()])), "enumeration");
		MatchTree[] enumMatches = getMatchTrees(tokens, enumPartIndex, "<enumeration> ((<separator>|<endSeparator>) (<cElement>|<eElement>))?");
		for (int l = 0; l < enumMatches.length; l++) {
			Annotation enumMatch = enumMatches[l].getMatch();
			addElementList(enumMatches[l], enumMatch);
			if (enumElementIdStrings.add(getElementIDs(enumMatch))) {
				newEnums.add(enumMatch);
				if (DEBUG_ENUMERATION_ASSEMBLY) System.out.println("Added terminated enumeration " + enumMatch.toXML());
			}
		}
		enums.addAll(newEnums);
		
		//	sort all enumerations found and return them
		Collections.sort(enums, AnnotationUtils.ANNOTATION_NESTING_ORDER);
		return ((Annotation[]) enums.toArray(new Annotation[enums.size()]));
	}
	
	private static void addElementList(MatchTree enumMatch, Annotation enumeration) {
		addElementList(enumMatch.getChildren(), enumeration);
	}
	
	private static void addElementList(MatchTreeNode[] mtns, Annotation enumeration) {
		for (int n = 0; n < mtns.length; n++) {
			if (mtns[n].getPattern().startsWith("<")) {
				if (mtns[n].getPattern().startsWith("<enumeration")) {
					ArrayList elementList = ((ArrayList) mtns[n].getMatch().getAttribute(ENUMERATION_ELEMENTS_ATTRIBUTE));
					if (elementList == null) {
						elementList = new ArrayList(); // initialize on starting element
						elementList.add(mtns[n].getMatch());
					}
					else elementList = new ArrayList(elementList); // copy for later expansion
					enumeration.setAttribute(ENUMERATION_ELEMENTS_ATTRIBUTE, elementList);
				}
				else if (mtns[n].getPattern().startsWith("<cElement") || mtns[n].getPattern().startsWith("<eElement")) {
					ArrayList authorNameList = ((ArrayList) enumeration.getAttribute(ENUMERATION_ELEMENTS_ATTRIBUTE));
					if (authorNameList != null)
						authorNameList.add(mtns[n].getMatch());
				}
			}
			else {
				MatchTreeNode[] cMtns = mtns[n].getChildren();
				if (cMtns != null)
					addElementList(cMtns, enumeration);
			}
		}
	}
	
	private static String getElementIDs(Annotation enumeration) {
		ArrayList elementList = ((ArrayList) enumeration.getAttribute(ENUMERATION_ELEMENTS_ATTRIBUTE));
		if ((elementList == null) || (elementList.size() == 0))
			return "";
		StringBuffer elementIDs = new StringBuffer(((Annotation) elementList.get(0)).getAnnotationID());
		for (int e = 1; e < elementList.size(); e++) {
			elementIDs.append("|");
			elementIDs.append(((Annotation) elementList.get(e)).getAnnotationID());
		}
		return elementIDs.toString();
	}
//	
//	//	ENUMERATION TEST
//	public static void main(String[] args) throws Exception {
//		final int elements = 100;
//		StringBuffer sb = new StringBuffer("John Doe");
//		for (int e = 0; e < elements; e++)
//			sb.append(", John Doe");
//		sb.append(", and John Doe");
//		TokenSequence ts = Gamta.newTokenSequence(sb, null);
//		Annotation[] annots = new Annotation[1 + elements + 1];
//		for (int a = 0; a < annots.length; a++) {
//			if ((a+1) == annots.length)
//				annots[a] = Gamta.newAnnotation(ts, "personName", ((a * 3) + 1), 2);
//			else annots[a] = Gamta.newAnnotation(ts, "personName", (a * 3), 2);
//		}
//		StringVector seps = new StringVector();
//		seps.addElement(",");
//		StringVector endSeps = new StringVector();
//		endSeps.addElement(",");
//		endSeps.addElement(", and");
//		long start = System.currentTimeMillis();
//		Annotation[] enums = getEnumerations(ts, annots, seps, endSeps);
//		System.out.println("Got " + enums.length + " enumerations in " + (System.currentTimeMillis() - start) + "ms");
//	}
}