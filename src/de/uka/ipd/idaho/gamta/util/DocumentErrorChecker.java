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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils;

/**
 * A document error checker detects (potential) errors of specific categories
 * and types in a document and adds them to an error protocol. This class also
 * provides a static registry for error checkers that allows client code to
 * retrieve and use these instances for running error checks on documents.
 * 
 * @author sautter
 */
public abstract class DocumentErrorChecker {
	
	/** the markup level indicating a layout related check */
	public static final String CHECK_LEVEL_LAYOUT = "layout";
	
	/** the markup level indicating a semantic check */
	public static final String CHECK_LEVEL_SEMANTICS = "semantics";
	/* Maybe rename "semantics" checking level to "markup" ...
	 * ... and then DON'T, as in GAMTA everything is markup */
	
	/**
	 * A meta error checker bundles multiple other document error checkers,
	 * e.g. by error category or checked error subjects. This is mainly a way
	 * for provider side client code to bundle error checkers. Meta checkers
	 * are allowed to indicate a 'mixed' check level if their contained error
	 * checkers work on both layout and semantics level.
	 * 
	 * @author sautter
	 */
	public static interface MetaErrorChecker {
		
		/** the markup level indicating a check at a mix of levels */
		public static final String CHECK_LEVEL_MIXED = "mixed";
		
		/**
		 * Retrieve the contained error checkers for a specific category and
		 * type of error. <code>null</code> values in the category or type
		 * arguments should be interpreted as wildcards.
		 * @param category the category of errors to get the checkers for
		 * @param type the type of errors to get the checkers for
		 * @return an array holding the matching error checkers
		 */
		public abstract DocumentErrorChecker[] getErrorCheckers(String category, String type);
	}
	
	/** the name of the error checker (never null) */
	public final String name;
	private String label;
	private String description;
	
	/**
	 * @param name the name of the error checker (must not be null)
	 */
	public DocumentErrorChecker(String name) {
		this(name, null, null);
	}
	
	/**
	 * @param name the name of the error checker (must not be null)
	 * @param label the label of the error checker, for use in a UI
	 * @param description the description of the error checker, for use in a UI
	 */
	public DocumentErrorChecker(String name, String label, String description) {
		if (name == null)
			throw new IllegalArgumentException("DocumentErrorChecker: name must not be null.");
		this.name = name;
		this.label = label;
		this.description = description;
	}
	
	/**
	 * Retrieve the label of the error checker, i.e., a nice name for use in a
	 * UI. if no label is set, this method returns the name instead.
	 * @return the label of the error checker
	 */
	public String getLabel() {
		return ((this.label == null) ? this.name : this.label);
	}
	
	/**
	 * Set the label of the error checker, for use in a UI.
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Retrieve a description of the error checker, i.e., an explanation of its
	 * checks, for use in a UI.
	 * @return the description of the error checker
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Set a description of the error checker, for use in a UI.
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Retrieves the categories of errors this document checker reports. The
	 * array returned by this method should have at least one element. In most
	 * implementations, this method will return a single error category, but
	 * this is not a strict requirement, as for instance meta checkers may well
	 * return multiple error categories if they bundle respective checkers.
	 * @return an array holding the error categories
	 */
	public abstract String[] getErrorCategories();
	
	/**
	 * Retrieve a label for an error category, i.e., a nice name for use in a
	 * UI. Implementations of this method may return null for all categories
	 * not included in the array returned by <code>getErrorCategories()</code>.
	 * @param category the error category to retrieve the label for
	 * @return the label of the argument error category
	 */
	public abstract String getErrorCategoryLabel(String category);
	
	/**
	 * Retrieve a description for an error category, i.e., an explanation of
	 * what kind of problems errors in that category indicate, for use in a UI.
	 * Implementations of this method may return null for all categories not
	 * included in the array returned by <code>getErrorCategories()</code>.
	 * @param category the error category to retrieve the description for
	 * @return the description of the argument error category
	 */
	public abstract String getErrorCategoryDescription(String category);
	
	/**
	 * Retrieves the types of errors this document checker reports in a given
	 * category, with a null argument retrieving all error types. The array
	 * returned by this method should have at least one element for each of the
	 * error categories returned from <code>getErrorCategories()</code>. In
	 * most implementations, this method will return a single error type per
	 * category, but this is not a strict requirement, as for instance meta
	 * checkers may well return multiple error types if they bundle respective
	 * checkers.
	 * @param category the error category to retrieve the error types for
	 * @return an array holding the error types
	 */
	public abstract String[] getErrorTypes(String category);
	
	/**
	 * Retrieve a label for an error type, i.e., a nice name for use in a UI.
	 * Implementations of this method may return null for all types not
	 * included in the array returned by <code>getErrorTypes()</code> for the
	 * argument category.
	 * @param category the category the argument error type belongs to
	 * @param type the error type to retrieve the label for
	 * @return the label of the argument error type
	 */
	public abstract String getErrorTypeLabel(String category, String type);
	
	/**
	 * Retrieve a description for an error type, i.e., an explanation of its
	 * what kind of problems errors of that type indicate, for use in a UI.
	 * Implementations of this method may return null for all types not
	 * included in the array returned by <code>getErrorTypes()</code> for the
	 * argument category.
	 * @param category the category the argument error type belongs to
	 * @param type the error type to retrieve the description for
	 * @return the description of the argument error type
	 */
	public abstract String getErrorTypeDescription(String category, String type);
	
	/**
	 * Retrieves the annotation types and attribute names observed in error
	 * checks of a specific category and type. Attribute names should start
	 * with an <code>@</code> to mark them as such. This method is meant to
	 * facilitate instant re-checking of a document after one or more edits to
	 * it. <code>null</code> values in the category or type arguments should
	 * be interpreted as wildcards.
	 * @param category the error category to retrieve the relevant types for
	 * @param category the error type to retrieve the relevant types for
	 * @return an array holding the relevant annotation types and attributes
	 */
	public abstract String[] getObservedTargets(String category, String type);
	
	/**
	 * Retrieves the types of annotations subject to checks for error of a
	 * specific category and type. This method is meant to facilitate instant
	 * re-checking of a document after one or more edits to it.
	 * <code>null</code> values in the category or type arguments should be
	 * interpreted as wildcards.
	 * @param category the error category to retrieve the relevant types for
	 * @param category the error type to retrieve the relevant types for
	 * @return an array holding the subject annotation types
	 */
	public abstract String[] getCheckedSubjects(String category, String type);
	
	/**
	 * Indicate whether or not checking for a given category and type of errors
	 * requires the top level document (as opposed to being able to perform the
	 * check on a derived <code>QueriableAnnotation</code>). This method will
	 * return true mostly for checks that pertain to relationships between two
	 * or more annotations (as opposed to checking for properties of individual
	 * annotations). <code>null</code> values in the category or type arguments
	 * should be interpreted as wildcards.
	 * @param category the error category in question
	 * @param type the error type in question
	 * @return true if a check for the argument category and type of errors
	 *            requires the top level document to execute in a meaningful
	 *            way
	 */
	public abstract boolean requiresTopLevelDocument(String category, String type);
	
	/**
	 * Indicate the markup level the error check works at for a given error
	 * category and type. There are only two permitted return values right now
	 * ('layout' and 'semantics'), but this list might be refined and extended
	 * in the future; furthermore, a <code>null</code> return value may be used
	 * to indicate that the error checker does not check for errors of the
	 * argument category or type. <code>null</code> values in the category or
	 * type arguments should be interpreted as wildcards.
	 * @param category the error category in question
	 * @param type the error type in question
	 * @return the markup level of the error checker
	 */
	public abstract String getCheckLevel(String category, String type);
	
	/**
	 * Indicate whether or not the error checker should by default be applied
	 * to all documents in a system. Implementations of this method will most
	 * likely return false in error checkers that apply only to a specific type
	 * of document.
	 * @return true if the error checker is intended to be applied by default
	 */
	public abstract boolean isDefaultErrorChecker();
	
	/**
	 * Metadata object describing details of a type of document errors added to
	 * an error protocol by a given document error checker, mostly intended for
	 * overview purposes. In particular, instances of this class bundle the
	 * argument data the error checker uses in its calls to the
	 * <code>DocumentErrorProtocol.addError()</code> methods of an argument
	 * error protocol from the <code>addDocumentErrors()</code> method.
	 * 
	 * @author sautter
	 */
	public static class DocumentErrorMetadata {
		
		/** the error source name */
		public final String source;
		
		/** the error category */
		public final String category;
		
		/** the error category label */
		public final String categoryLabel;
		
		/** the error type */
		public final String type;
		
		/** the error type label */
		public final String typeLabel;
		
		/** the error severity */
		public final String severity;
		
		/** the error description (or the template used for generating the
		 * latter for an error on a specific markup element) */
		public final String description;
		
		/**
		 * Constructor
		 * @param source the error source name
		 * @param category the error category
		 * @param categoryLabel the error category label
		 * @param type the error type
		 * @param typeLabel the error type label
		 * @param severity the error severity
		 * @param description the error description (or the template used for
		 *        generating the latter for an error on a specific markup
		 *        element)
		 */
		public DocumentErrorMetadata(String source, String category, String categoryLabel, String type, String typeLabel, String severity, String description) {
			this.source = source;
			this.category = category;
			this.categoryLabel = categoryLabel;
			this.type = type;
			this.typeLabel = typeLabel;
			this.severity = severity;
			this.description = description;
		}
	}
	
	/**
	 * Retrieve the metadata for all errors the error checker potentially adds
	 * to a document error protocol under. This implementation simply loops
	 * through to the one-argument version of this method with a
	 * <code>null</code> error category. Sub classes are welcome to overwrite
	 * it as needed.
	 * @param catgory the category of error to get the medatada for
	 * @return an array holding the error metadata
	 */
	public DocumentErrorMetadata[] getErrorMetadata() {
		return this.getErrorMetadata(null);
	}
	
	/**
	 * Retrieve the metadata for all errors the error checker potentially adds
	 * to a document error protocol under a given error category.
	 * Implementations should treat a <code>null</code> argument value as a
	 * wildcards. This implementation simply loops through to the two-argument
	 * version of this method with a <code>null</code> error type. Sub classes
	 * are welcome to overwrite it as needed.
	 * @param catgory the category of error to get the medatada for
	 * @return an array holding the error metadata
	 */
	public DocumentErrorMetadata[] getErrorMetadata(String category) {
		return this.getErrorMetadata(category, null);
	}
	
	/**
	 * Retrieve the metadata for all errors the error checker potentially adds
	 * to a document error protocol under a given error category and type.
	 * Implementations should treat <code>null</code> argument values for both
	 * category and type as wildcards.
	 * @param category the category of error to get the medatada for
	 * @param type  the type of error to get the medatada for
	 * @return an array holding the error metadata
	 */
	public abstract DocumentErrorMetadata[] getErrorMetadata(String category, String type);
	
	/**
	 * Check a document for errors of a specific category and type and add
	 * any detected errors to a document error protocol. <code>null</code>
	 * values in the category or type arguments should be interpreted as
	 * wildcards. The returned <code>int</code> indicates the number of errors
	 * that were added to the argument error protocol. Implementations should
	 * specify their name as the error source.
	 * @param doc the document to check
	 * @param dep the error protocol to add detected errors to
	 * @param category the category of errors to check for
	 * @param type the type of errors to check for
	 * @return the number of detected errors
	 */
	public abstract int addDocumentErrors(QueriableAnnotation doc, DocumentErrorProtocol dep, String category, String type);
	
	/**
	 * Build a label for an annotation that is subject of an error, mainly for
	 * use in the description of errors added to a protocol.
	 * @param annot the annotation to build a label for
	 * @param maxEndTokens the maximum number of tokens to use from either end
	 *            of the argument annotation
	 * @return the label for the argument annotation
	 */
	public static String buildAnnotationLabel(Annotation annot, int maxEndTokens) {
		if (annot.size() <= (maxEndTokens + 1 + maxEndTokens))
			return TokenSequenceUtils.concatTokens(annot, false, true);
		StringBuffer value = new StringBuffer();
		value.append(TokenSequenceUtils.concatTokens(annot, 0, maxEndTokens, false, true));
		value.append(" ... ");
		value.append(TokenSequenceUtils.concatTokens(annot, (annot.size() - maxEndTokens), maxEndTokens, false, true));
		return value.toString();
	}
	
	/**
	 * Register a document error checker to make it available to client code of
	 * this class.
	 * @param dec the error checker to register
	 */
	public static void registerErrorChecker(DocumentErrorChecker dec) {
		if (dec == null)
			return;
		DocumentErrorChecker regDec = getRegisteredErrorChecker(dec.name);
		if (dec == regDec)
			return; // registered before
		if (regDec != null)
			unregisterErrorChecker(regDec); // this one is being replaced
		
		errorCheckersByName.put(dec.name, dec);
		getErrorCheckerList("", true).add(dec);
		String[] categories = dec.getErrorCategories();
		for (int c = 0; c < categories.length; c++) {
			getErrorCheckerList(categories[c], true).add(dec);
			CountingSet cTypes = ((CountingSet) errorCategoriesToTypes.get(categories[c]));
			if (cTypes == null) {
				cTypes = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER));
				errorCategoriesToTypes.put(categories[c], cTypes);
			}
			String[] types = dec.getErrorTypes(categories[c]);
			for (int t = 0; t < types.length; t++) {
				getErrorCheckerList((categories[c] + "." + types[t]), true).add(dec);
				cTypes.add(types[t]);
			}
		}
		
		String[] targets = dec.getObservedTargets(null, null);
		for (int t = 0; t < targets.length; t++)
			getErrorCheckerList(("target:" + targets[t]), true).add(dec);
		String[] subjects = dec.getCheckedSubjects(null, null);
		for (int s = 0; s < subjects.length; s++)
			getErrorCheckerList(("subject:" + subjects[s]), true).add(dec);
		
		for (Iterator rlit = registryListeners.iterator(); rlit.hasNext();)
			((RegistryListener) rlit.next()).errorCheckerRegistered(dec);
	}
	
	/**
	 * Unregister a document error checker so it is no longer available to
	 * client code of this class.
	 * @param dec the error checker to unregister
	 */
	public static void unregisterErrorChecker(DocumentErrorChecker dec) {
		if (dec == null)
			return;
		DocumentErrorChecker regDec = getRegisteredErrorChecker(dec.name);
		if (dec != regDec)
			return; // not the one that was originally registered
		
		errorCheckersByName.remove(dec.name);
		LinkedHashSet ecl = getErrorCheckerList("", false);
		if (ecl != null)
			ecl.remove(dec);
		
		String[] categories = dec.getErrorCategories();
		for (int c = 0; c < categories.length; c++) {
			ecl = getErrorCheckerList(categories[c], false);
			if (ecl != null)
				ecl.remove(dec);
			CountingSet cTypes = ((CountingSet) errorCategoriesToTypes.get(categories[c]));
			String[] types = dec.getErrorTypes(categories[c]);
			for (int t = 0; t < types.length; t++) {
				ecl = getErrorCheckerList((categories[c] + "." + types[t]), false);
				if (ecl != null)
					ecl.remove(dec);
				cTypes.remove(types[t]);
			}
			if (cTypes.isEmpty())
				errorCategoriesToTypes.remove(categories[c]);
		}
		
		String[] targets = dec.getObservedTargets(null, null);
		for (int t = 0; t < targets.length; t++) {
			ecl = getErrorCheckerList(("target:" + targets[t]), false);
			if (ecl != null)
				ecl.remove(dec);
		}
		String[] subjects = dec.getCheckedSubjects(null, null);
		for (int s = 0; s < subjects.length; s++) {
			ecl = getErrorCheckerList(("subject:" + subjects[s]), false);
			if (ecl != null)
				ecl.remove(dec);
		}
		
		for (Iterator rlit = registryListeners.iterator(); rlit.hasNext();)
			((RegistryListener) rlit.next()).errorCheckerUnregistered(dec);
	}
	
	/**
	 * Listener interface for client code to observe the registration and
	 * unregistration of error checkers. This is mainly intended to enable
	 * client code that uses and applies error checkers to receive notification
	 * of changes.
	 * 
	 * @author sautter
	 */
	public static interface RegistryListener {
		
		/**
		 * Receive notification that an error checker was registered.
		 * @param errorChecker the error checker was that registered
		 */
		public abstract void errorCheckerRegistered(DocumentErrorChecker errorChecker);
		
		/**
		 * Receive notification that an error checker was unregistered.
		 * @param errorChecker the error checker was that unregistered
		 */
		public abstract void errorCheckerUnregistered(DocumentErrorChecker errorChecker);
	}
	
	/**
	 * Add a registry listener to receive notifications as error checkers are
	 * registered or unregistered.
	 * @param rl the registry listener to add
	 */
	public static void addRegistryListener(RegistryListener rl) {
		if (rl != null)
			registryListeners.add(rl);
	}
	
	/**
	 * Remove a registry listener so it ceases to receive notifications as
	 * error checkers are registered or unregistered.
	 * @param rl the registry listener to remove
	 */
	public static void removeRegistryListener(RegistryListener rl) {
		registryListeners.remove(rl);
	}
	
	/**
	 * Retrieves all categories at least one error checker is registered for.
	 * @return an array holding the error categories
	 */
	public static String[] getRegisteredErrorCategories() {
		return ((String[]) errorCategoriesToTypes.keySet().toArray(new String[errorCategoriesToTypes.size()]));
	}
	
	/**
	 * Retrieve a label for an error category, i.e., a nice name for use in a
	 * UI. If no error checker is registered for the argument category, this
	 * method returns null.
	 * @param category the error category to retrieve the label for
	 * @return the label of the argument error category
	 */
	public static String getRegisteredErrorCategoryLabel(String category) {
		if (category == null)
			return null;
		LinkedHashSet ecl = getErrorCheckerList(category, false);
		if ((ecl == null) || ecl.isEmpty())
			return null;
		for (Iterator decit = ecl.iterator(); decit.hasNext();) {
			String label = ((DocumentErrorChecker) decit.next()).getErrorCategoryLabel(category);
			if (label != null)
				return label;
		}
		return null;
	}
	
	/**
	 * Retrieve a description for an error category, i.e., an explanation of
	 * what kind of problems errors in that category indicate, for use in a UI.
	 * If no error checker is registered for the argument category, this method
	 * returns null.
	 * @param category the error category to retrieve the description for
	 * @return the description of the argument error category
	 */
	public static String getRegisteredErrorCategoryDescription(String category) {
		if (category == null)
			return null;
		LinkedHashSet ecl = getErrorCheckerList(category, false);
		if ((ecl == null) || ecl.isEmpty())
			return null;
		for (Iterator decit = ecl.iterator(); decit.hasNext();) {
			String description = ((DocumentErrorChecker) decit.next()).getErrorCategoryDescription(category);
			if (description != null)
				return description;
		}
		return null;
	}
	
	/**
	 * Retrieves all types at least one error checker is registered for in the
	 * argument category. A null argument retrieves all error types from all
	 * categories.
	 * @param category the error category to get the error types for
	 * @return an array holding the error types
	 */
	public static String[] getRegisteredErrorTypes(String category) {
		if (category == null) {
			TreeSet allTypes = new TreeSet(String.CASE_INSENSITIVE_ORDER);
			for (Iterator ecit = errorCategoriesToTypes.keySet().iterator(); ecit.hasNext();)
				allTypes.addAll((CountingSet) errorCategoriesToTypes.get(ecit.next()));
			return ((String[]) allTypes.toArray(new String[allTypes.size()]));
		}
		else {
			CountingSet cTypes = ((CountingSet) errorCategoriesToTypes.get(category));
			return ((cTypes == null) ? new String[0] : ((String[]) cTypes.toArray(new String[cTypes.elementCount()])));
		}
	}
	
	/**
	 * Retrieve a label for an error type, i.e., a nice name for use in a UI.
	 * If no error checker is registered for the argument category and type,
	 * this method returns null.
	 * @param category the category the argument error type belongs to
	 * @param type the error type to retrieve the label for
	 * @return the label of the argument error type
	 */
	public static String getRegisteredErrorTypeLabel(String category, String type) {
		if ((category == null) || (type == null))
			return null;
		LinkedHashSet ecl = getErrorCheckerList((category + "." + type), false);
		if ((ecl == null) || ecl.isEmpty())
			return null;
		for (Iterator decit = ecl.iterator(); decit.hasNext();) {
			String label = ((DocumentErrorChecker) decit.next()).getErrorTypeLabel(category, type);
			if (label != null)
				return label;
		}
		return null;
	}
	
	/**
	 * Retrieve a description for an error type, i.e., an explanation of its
	 * what kind of problems errors of that type indicate, for use in a UI. If
	 * no error checker is registered for the argument category and type, this
	 * method returns null.
	 * @param category the category the argument error type belongs to
	 * @param type the error type to retrieve the description for
	 * @return the description of the argument error type
	 */
	public static String getRegisteredErrorTypeDescription(String category, String type) {
		if ((category == null) || (type == null))
			return null;
		LinkedHashSet ecl = getErrorCheckerList((category + "." + type), false);
		if ((ecl == null) || ecl.isEmpty())
			return null;
		for (Iterator decit = ecl.iterator(); decit.hasNext();) {
			String description = ((DocumentErrorChecker) decit.next()).getErrorTypeDescription(category, type);
			if (description != null)
				return description;
		}
		return null;
	}
	
	/**
	 * Retrieve all registered error checkers.
	 * @return an array holding the error checkers
	 */
	public static DocumentErrorChecker[] getRegisteredErrorCheckers() {
		return getRegisteredErrorCheckers(null);
	}
	
	/**
	 * Retrieve all registered error checkers that check for errors of a given
	 * category. A <code>null</code> argument retrieves all error checkers.
	 * @param category the error category
	 * @return an array holding the error checkers
	 */
	public static DocumentErrorChecker[] getRegisteredErrorCheckers(String category) {
		return getRegisteredErrorCheckers(category, null);
	}
	
	/**
	 * Retrieve all registered error checkers that check for errors of a given
	 * type in a given category. A <code>null</code> argument for the category
	 * retrieves all error checkers, a <code>null</code> argument for the type
	 * retrieves all error checkers in the argument category.
	 * @param category the error category
	 * @param type the error type
	 * @return an array holding the error checkers
	 */
	public static DocumentErrorChecker[] getRegisteredErrorCheckers(String category, String type) {
		String key = ("" + ((category == null) ? "" : (category + ((type == null) ? "" : ("." + type)))));
		LinkedHashSet ecl = getErrorCheckerList(key, false);
		return ((ecl == null) ? new DocumentErrorChecker[0] : ((DocumentErrorChecker[]) ecl.toArray(new DocumentErrorChecker[ecl.size()])));
	}
	
	/**
	 * Retrieve the names of all registered error checkers.
	 * @return an array holding the error names
	 */
	public static String[] getRegisteredErrorCheckerNames() {
		return ((String[]) errorCheckersByName.keySet().toArray(new String[errorCheckersByName.size()]));
	}
	
	/**
	 * Retrieve a registered error checker by its name.
	 * @param name the name of the sought error checker
	 * @return the error checkers with the argument name
	 */
	public static DocumentErrorChecker getRegisteredErrorChecker(String name) {
		return ((DocumentErrorChecker) errorCheckersByName.get(name));
	}
	
	/**
	 * Retrieve the registered error checkers observing a given annotation type
	 * or attribute name. Attribute names should start with an <code>@</code>
	 * to mark them as such.
	 * @param target the annotation type or attribute name to get the observing
	 *            error checkers for
	 * @return an array holding the error checkers
	 */
	public static DocumentErrorChecker[] getObservingErrorCheckers(String target) {
		LinkedHashSet ecl = getErrorCheckerList(("target:" + target), false);
		return ((ecl == null) ? new DocumentErrorChecker[0] : ((DocumentErrorChecker[]) ecl.toArray(new String[ecl.size()])));
	}
	
	/**
	 * Retrieve the registered error checkers inspecting annotations of a given
	 * type.
	 * @param subject the annotation type to get the inspecting error checkers
	 *            for
	 * @return an array holding the error checkers
	 */
	public static DocumentErrorChecker[] getInspectingErrorCheckers(String subject) {
		LinkedHashSet ecl = getErrorCheckerList(("subject:" + subject), false);
		return ((ecl == null) ? new DocumentErrorChecker[0] : ((DocumentErrorChecker[]) ecl.toArray(new String[ecl.size()])));
	}
	
	private static synchronized LinkedHashSet getErrorCheckerList(String key, boolean create) {
		LinkedHashSet ecl = ((LinkedHashSet) errorCheckerListsByKey.get(key));
		if ((ecl == null) && create) {
			ecl = new LinkedHashSet();
			errorCheckerListsByKey.put(key, ecl);
		}
		return ecl;
	}
	
	private static Map errorCheckerListsByKey = Collections.synchronizedMap(new HashMap());
	private static Map errorCheckersByName = Collections.synchronizedMap(new LinkedHashMap());
	private static Map errorCategoriesToTypes = Collections.synchronizedMap(new TreeMap(String.CASE_INSENSITIVE_ORDER));
	
	private static Set registryListeners = Collections.synchronizedSet(new LinkedHashSet());
}
