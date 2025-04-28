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


import java.util.Set;
import java.util.regex.Pattern;

/**
 * Static class providing comparison functionality for Attributed objects.
 * 
 * @author sautter
 */
public class AttributeUtils {
	
	private static final String NCNameRegEx = "([a-zA-Z\\_][a-zA-Z0-9\\_\\-\\.]*+)"; // regular expression matching XML NCNames
	private static final String ANameRegEx = "(" + NCNameRegEx + "(\\:" + NCNameRegEx + ")?+)"; // regular expression matching XML QNames
	private static final Pattern ANamePattern = Pattern.compile(ANameRegEx);
	
	/**
	 * Check whether an attribute name String is valid
	 * @param name the attribute name String to test
	 * @return true if and only if the specified attribute name is valid (a
	 *         QName in the sense of XML)
	 */
	public static boolean isValidAttributeName(String name) {
		return ANamePattern.matcher(name).matches();
	}
	
//	private static final String AValueRegEx = "[^\\'\\\"\\&\\<\\>\\n\\r\\t]*+";	// regular expression matching admissible XML attribute values
//	private static final String AValueRegEx = "[^\\\"\\&\\<\\n\\r\\t]*+";	// regular expression matching admissible XML attribute values
//	private static final Pattern AValuePattern = Pattern.compile(AValueRegEx);
//	
//	/**
//	 * check whether an attribute value String is valid
//	 * @param value the attribute name String to test
//	 * @return true if and only if the specified attribute value is valid (does
//	 *         not contain any of the characters ', ", &amp;, &lt;, or &gt;, and
//	 *         no whitespace but simple space characters)
//	 */
//	public static boolean isValidAttributeValue(String value) {
//		return AValuePattern.matcher(value).matches();
//	}
	
	/**
	 * Attribute copy mode that sets attributes at the target object to the
	 * values from the source object, regardless if they were set with the
	 * target object before. This mode is the default.
	 */
	public static final int SET_ATTRIBUTE_COPY_MODE = 0;
	
	/**
	 * Attribute copy mode that sets attributes at the target object to the
	 * values from the source object only if they were not set with the target
	 * object before. This mode does not overwrite existing values at the target
	 * object.
	 */
	public static final int ADD_ATTRIBUTE_COPY_MODE = 1;
	
	/**
	 * Attribute copy mode that sets attributes at the target object to the
	 * values from the source object only if they were set with the target
	 * object before. This mode does not add new attributes to the target
	 * object, only changes values of existing ones. 
	 */
	public static final int OVERWRITE_ATTRIBUTE_COPY_MODE = 2;
	
	/**
	 * Copy attributes from one Attributed object to another one.
	 * @param source the Attributed object to copy the attributes from
	 * @param target the Attributed object to transfer the attributes to
	 */
	public static void copyAttributes(Attributed source, Attributed target) {
		copyAttributes(source, target, SET_ATTRIBUTE_COPY_MODE);
	}
	
	/**
	 * Copy attributes from one Attributed object to another one.
	 * @param source the Attributed object to copy the attributes from
	 * @param target the Attributed object to transfer the attributes to
	 * @param mode the copying mode, one of SET_ATTRIBUTE_COPY_MODE,
	 *            ADD_ATTRIBUTE_COPY_MODE and OVERWRITE_ATTRIBUTE_COPY_MODE
	 */
	public static void copyAttributes(Attributed source, Attributed target, int mode) {
		copyAttributes(source, target, mode, null);
	}
	
	/**
	 * Copy attributes from one Attributed object to another one.
	 * @param source the Attributed object to copy the attributes from
	 * @param target the Attributed object to transfer the attributes to
	 * @param filter a Set containing the names of the Attributes to copy
	 */
	public static void copyAttributes(Attributed source, Attributed target, Set filter) {
		copyAttributes(source, target, SET_ATTRIBUTE_COPY_MODE, filter);
	}
	
	/**
	 * Copy attributes from one Attributed object to another one.
	 * @param source the Attributed object to copy the attributes from
	 * @param target the Attributed object to transfer the attributes to
	 * @param mode the copying mode, one of SET_ATTRIBUTE_COPY_MODE,
	 *            ADD_ATTRIBUTE_COPY_MODE and OVERWRITE_ATTRIBUTE_COPY_MODE
	 * @param filter a Set containing the names of the Attributes to copy
	 */
	public static void copyAttributes(Attributed source, Attributed target, int mode, Set filter) {
		
		//	TODO allow registering attribute merger objects to merge up attribute values (akin to transformers in ImObjectTransformer)
		
		//	get attribute names of source
		String[] attributeNames = source.getAttributeNames();
		
		//	process attributes one by one
		for (int a = 0; a < attributeNames.length; a++)
			if ((filter == null) || filter.contains(attributeNames[a])) {
				if ((mode == ADD_ATTRIBUTE_COPY_MODE) && target.hasAttribute(attributeNames[a]))
					continue; // not replacing values in add mode
				if ((mode == OVERWRITE_ATTRIBUTE_COPY_MODE) && !target.hasAttribute(attributeNames[a]))
					continue; // not adding new attributes in replace-only mode
				target.setAttribute(attributeNames[a], source.getAttribute(attributeNames[a]));
			}
	}
//	
//	public static class AttributeRestriction {
//		public final String annotationType;
//		public final String attributeName;
//		//	TODO add permissions
//		public AttributeRestriction(String annotationType, String attributeName) {
//			this.annotationType = annotationType;
//			this.attributeName = attributeName;
//		}
//	}
//	
//	public static abstract class AttributeValueConstraint {
//		
//		public static class Restriction {
//			static final int VISIBLE_FLAG = 1;
//			static final int REMOVABLE_FLAG = 2;
//			static final int MODIFIABLE_FLAG = 4;
//			static final int EDITABLE_FLAG = 8;
//			
//			/** restriction type allowing full free modification and removal of attribute values (the default) */
//			public static final Restriction UNRESTRICTED = new Restriction((VISIBLE_FLAG | REMOVABLE_FLAG | MODIFIABLE_FLAG | EDITABLE_FLAG), 'U', "Unrestricted", "Values can be removed, added, and modified freely");
//			
//			/** restriction type allowing full free modification and removal of attribute values, but involve some pattern match */
//			public static final Restriction MATCHED = new Restriction((VISIBLE_FLAG | REMOVABLE_FLAG | MODIFIABLE_FLAG | EDITABLE_FLAG), 'M', "Matched", "Values can be removed, added, and modified freely, but have to match some pattern");
//			
//			/** restriction type allowing modification and removal of attribute values, but restrict values to those in a given list */
//			public static final Restriction CONTROLLED = new Restriction((VISIBLE_FLAG | REMOVABLE_FLAG | MODIFIABLE_FLAG), 'C', "Controlled", "Values can be removed and freely selectecd from defined list");
//			
//			/** restriction type allowing modification and removal of attribute values, but restrict values to ones identifying some other object in the same document */
//			public static final Restriction REFERENCING = new Restriction((VISIBLE_FLAG | REMOVABLE_FLAG | MODIFIABLE_FLAG), 'R', "Referencing", "Values reference some other object or attribute and have a defined list of valid values per document");
//			
//			/** restriction type preventing modification and removal of attribute values, on the grounds of them being referenced by some other attribute values in the same document */
//			public static final Restriction REFERENCED = new Restriction((VISIBLE_FLAG), 'T', "Referenced", "Values are referenced by values of some other attribute and thus cannot be modified or removed");
//			public static final Restriction IMPLICIT = new Restriction((VISIBLE_FLAG), 'I', "Implicit", "Values are implicit/inherited/computed instead of being set explicitly and thus cannot be modified or removed");
//			public static final Restriction PROCESS = new Restriction((VISIBLE_FLAG | REMOVABLE_FLAG), 'P', "Process", "Values contain functional information pertaining to some functional aspect of markup generation and thus can only be removed, but not modified");
//			public static final Restriction DEBUG = new Restriction((VISIBLE_FLAG), 'D', "Debug", "Values contain tracking information pertaining to details of how the result of some computation emerged and thus cannot be modified or removed");
//			/*
//	TODO Establish restriction levels for attribute values:
//	- unrestricted (code U): the default
//	- suggestion (code S): with value suggestions, but free for input
//	  - examples (maybe): types of subSection and subSubSection, type of typeStatus
//	  - value editable
//	  - attribute removable
//	  - border: yellow if selected value not suggested
//	- pattern match constraint (code M): attributes whose values must match some pattern
//	  - examples: count in specimenCount, value in date, value in geoCoordinate
//	- controlled (code C): only specific list provided values allowed
//	  - examples (maybe) types of subSection and subSubSection, type of typeStatus
//	  - attribute removable
//	  - value not editable, only selectable from list
//	  - color code: light gray
//	  - border: red if selected value not allowed
//	- referencing (code R): references some other attribute value in the document
//	  - examples: citedRefId of bibRefCitation, targetBox of caption, (rows|cols)Continue(In|From) of table, connection attributes of image, captionStartId of figureCitation and tableCitation, fontName and fontCharCodes of ImWord, documentStyleName and documentStyleId of ImDocument, ..., think of more
//	  - attribute removable
//	  - value not editable, only selectable from list
//	  - color code: light gray
//	  - border: red if selected value not in list
//	- reference-target (code T): referenced by some other attribute
//	  - examples: refId of bibRef, startId of caption
//	  - attribute not removable
//	  - value not editable, not even selectable
//	  - color code: gray
//	- implicit (code I): implicit (computed)
//	  - examples: pageId and lastPageId of annotations in ImDocumentRoot
//	  - attribute not removable
//	  - value not editable, not even selectable
//	  - color code: gray
//	- debug tracking (code D): added only by gizmos, and only for tracking purposes
//	  - examples: _step and _evidence of taxonomicName, _reason of heading
//	  - attribute not removable
//	  - value not editable, not even selectable
//	  - color code: light green
//	  - text color: dark gray
//	- functional (code F): added only by gizmos, for any kind of function
//	  - examples: flags added in feedback dialogs (OCR error on bibRef), error attributes added to subjects in (upcoming) XML QC
//	  - attribute not removable
//	  - value not editable, not even selectable
//	  - color code: light blue
//	  - text color: dark gray
//	- hidden (code H): added only by gizmos, for any kind of function, invisible in UI
//	  - examples: flags added in feedback dialogs (OCR error on bibRef), error attributes added to subjects in (upcoming) XML QC
//	  - attribute not removable
//	  - value not editable, not even selectable
//	  - color code: light blue
//	  - text color: dark gray
//			 */
//			
//			private byte flags;
//			public final char code;
//			public final String label;
//			public final String descripton;
//			Restriction(int flags, char code, String label, String descripton) {
//				this.flags = ((byte) (flags & 0xFF));
//				this.code = code;
//				this.label = label;
//				this.descripton = descripton;
//			}
//			public boolean isVisible() {
//				return ((this.flags & VISIBLE_FLAG) != 0);
//			}
//			public boolean isRemovable() {
//				return ((this.flags & REMOVABLE_FLAG) != 0);
//			}
//			public boolean isModifiable() {
//				return ((this.flags & MODIFIABLE_FLAG) != 0);
//			}
//			public boolean isEditable() {
//				return ((this.flags & EDITABLE_FLAG) != 0);
//			}
//		}
//		
//		public final String annotationType;
//		public final String attributeName;
//		public final Restriction restriction;
//		//	TODO restriction class
//		//	TODO add actual values (as abstract getter method)
//		public AttributeValueConstraint(String annotationType, String attributeName) {
//			this(annotationType, attributeName, null);
//		}
//		public AttributeValueConstraint(String annotationType, String attributeName, Restriction restriction) {
//			this.annotationType = annotationType;
//			this.attributeName = attributeName;
//			this.restriction = ((restriction == null) ? Restriction.UNRESTRICTED : restriction);
//		}
//		public abstract Object[] getSuggestedValues(Attributed subject, Attributed doc);
//		public abstract String[] getValueErrors(Attributed subject, Object value, Attributed doc);
//		public boolean isValidValue(Attributed subject, Object value, Attributed doc) {
//			return (this.getValueErrors(subject, value, doc) == null);
//		}
//	}
	
	/*

TODO make constraints tractable:
- give them name
- maybe even preserve stack trace on creation
==> easier to track where contradicting constraints coming from ...
==> ... for both attribute names and values

TODO Unify attribute value restrictions and suggestions into attribute value constraints:
- provide abstract getSuggestedValues(Attributed subject, Attributed doc) ==> Object[] values method
- provide abstract checkValue(Attributed subject, Object value, Attributed doc) ==> String[] errors method
- provide isPermittedValue(Attributed subject, Object value, Attributed doc) ==> boolean permitted method, checking errors for null
- provide UI behavior:
  - visibility ==> show in overview list at all?
  - editable (allow value entry) ==> editability of value field
  - modifiable (allow changing value by either one of entry or selection) ==> selectability from overview list
  - removability ==> availability/enablement of removal button
  - explanation of restrictions in UI behavior ==> additional tooltip (most likely to display across top above value overview)
  ==> provide restriction objects in attribute utils:
    - boolean getters for properties ...
    - ... with property data encoded in int flag vector
    ==> specific constraint subclasses can use constants ...
    ==> ... with names indicating meaning
  ==> add (static) "show hidden" config flag to attribute editor widget ...
  ==> ... and show those attributes in medium gray on light gray (at least by default) if flag set to true
    ==> set to true in master configuration (just for checking purposes) ...
    ==> ... or override altogether or help simulate errors, etc.
      ==> provide respective "Ignore Attribute Restrictions" checkbox in edit menu if in master configuration

TODO Still need to get all attribute names for suggestions, though, in spite of lazily getting existing (and/or configured) attribute values:
- have dedicated plug-in provide attribute summary for each object type ...
- ... or integrate said functionality into plug-ins responsible for attribute handling:
  - GGI attribute tool provider
  - GGE equivalent (after UI makeover and GG Core split)
- initialize on opening document, discard on closing document
- populate lazily
- use document listener to track type changes, additions, an removals
- use counting sets to track extant attribute names
==> also creates central point for providing both attribute name and attribute value suggestions via respective config files

TODO Add enforce annotation type constraints flag to annotation utils:
- default to true
- set to false from application cores if started with master configuration
- allow free entry of annotation types in that case ...
- ... maybe accompanied by (non-prompt) warning
==> facilitates all sorts of constraint tests ...
==> ... without requiring actual faulty gizmo

TODO Add enforce attribute constraints flag to attribute utils:
- default to true
- set to false from application cores if started with master configuration
- allow free entry of annotation types in that case ...
- ... maybe accompanied by (non-prompt) warning
==> facilitates all sorts of constraint tests ...
==> ... without requiring actual faulty gizmo

TODO Add enforce attribute value constraints flag to attribute utils:
- default to true
- set to false from application cores if started with master configuration
- allow free entry of annotation types in that case ...
- ... maybe accompanied by (non-prompt) warning
==> facilitates all sorts of constraint tests ...
==> ... without requiring actual faulty gizmo

Observe all those flags in UI elements observing constraints ...
... as latter might not even be possible everywhere
	 */
	
	/**
	 * Test if some Attributed object has at least the attributes of some
	 * reference object
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @return true if toTest has at least all the attributes the reference
	 *         object has
	 */
	public static boolean hasAttributesOf(Attributed toTest, Attributed reference) {
		return hasAttributesOf(toTest, reference, null);
	}
	
	/**
	 * Test if some Attributed object has at least the attributes of some
	 * reference object
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @param filter a Set containing the names of the Attributes to test
	 * @return true if toTest has at least all the attributes the reference
	 *         object has
	 */
	public static boolean hasAttributesOf(Attributed toTest, Attributed reference, Set filter) {
		
		//	get attribute names of reference
		String[] referenceAttributeNames = reference.getAttributeNames();
		
		//	process attributes one by one
		for (int a = 0; a < referenceAttributeNames.length; a++) {
			
			//	check filter
			if ((filter == null) || filter.contains(referenceAttributeNames[a]))
				if (!toTest.hasAttribute(referenceAttributeNames[a])) return false;
		}
		
		//	all checks successful
		return true;
	}
	
	/**
	 * Test if some Attributed object has at exactly the same attributes as some
	 * reference object
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @return true if toTest has at exactly the same attributes as reference
	 *         object
	 */
	public static boolean hasSameAttributes(Attributed toTest, Attributed reference) {
		return hasSameAttributes(toTest, reference, null);
	}
	
	/**
	 * Test if some Attributed object has at exactly the same attributes as some
	 * reference object
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @param filter a Set containing the names of the Attributes to test
	 * @return true if toTest has at exactly the same attributes as reference
	 *         object
	 */
	public static boolean hasSameAttributes(Attributed toTest, Attributed reference, Set filter) {
		
		//	get attribute names of reference
		String[] referenceAttributeNames = reference.getAttributeNames();
		
		//	process reference attributes one by one
		for (int a = 0; a < referenceAttributeNames.length; a++) {
			
			//	check filter
			if ((filter == null) || filter.contains(referenceAttributeNames[a]))
				if (!toTest.hasAttribute(referenceAttributeNames[a])) return false;
		}
		
		//	get attribute names of test object
		String[] toTestAttributeNames = toTest.getAttributeNames();
		
		//	process test attributes one by one
		for (int a = 0; a < toTestAttributeNames.length; a++) {
			
			//	check filter
			if ((filter == null) || filter.contains(toTestAttributeNames[a]))
				if (!reference.hasAttribute(toTestAttributeNames[a])) return false;
		}
		
		//	all checks successful
		return true;
	}
	
	/**
	 * Test if some Attributed object has at least the attributes of some
	 * reference object, with the same values
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @return true if toTest has at least all the attributes the reference
	 *         object has, and the attributes have the same values
	 */
	public static boolean hasAttributeValuesOf(Attributed toTest, Attributed reference) {
		return hasAttributeValuesOf(toTest, reference, null);
	}
	
	/**
	 * Test if some Attributed object has at least the attributes of some
	 * reference object, with the same values
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @param filter a Set containing the names of the Attributes to test
	 * @return true if toTest has at least all the attributes the reference
	 *         object has, and the attributes have the same values
	 */
	public static boolean hasAttributeValuesOf(Attributed toTest, Attributed reference, Set filter) {
		
		//	get attribute names of reference
		String[] referenceAttributeNames = reference.getAttributeNames();
		
		//	process attributes one by one
		for (int a = 0; a < referenceAttributeNames.length; a++) {
			
			//	check filter
			if ((filter == null) || filter.contains(referenceAttributeNames[a])) {
				
				//	toTest doesn't have attribute
				if (!toTest.hasAttribute(referenceAttributeNames[a])) return false;
				
				//	attribute is null in reference, check if same is true for toTest
				if (reference.getAttribute(referenceAttributeNames[a]) == null)
					if (toTest.getAttribute(referenceAttributeNames[a]) != null) return false;
				
				//	compare attributes using equals() method
				if (!reference.getAttribute(referenceAttributeNames[a]).equals(toTest.getAttribute(referenceAttributeNames[a]))) return false;
			}
		}
		
		//	all checks successful
		return true;
	}
	
	/**
	 * Test if some Attributed object has exactly the same attributes as some
	 * reference object, with the same values
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @return true if toTest has exactly the same attributes as reference
	 *         object has, and the attributes have the same values
	 */
	public static boolean hasEqualAttributes(Attributed toTest, Attributed reference) {
		return hasEqualAttributes(toTest, reference, null);
	}
	
	/**
	 * Test if some Attributed object has exactly the same attributes as some
	 * reference object, with the same values
	 * @param toTest the Attributed object to test
	 * @param reference the reference object
	 * @param filter a Set containing the names of the Attributes to test
	 * @return true if toTest has exactly the same attributes as reference
	 *         object has, and the attributes have the same values
	 */
	public static boolean hasEqualAttributes(Attributed toTest, Attributed reference, Set filter) {
		
		//	get attribute names of reference
		String[] referenceAttributeNames = reference.getAttributeNames();
		
		//	process attributes one by one
		for (int a = 0; a < referenceAttributeNames.length; a++) {
			
			//	check filter
			if ((filter == null) || filter.contains(referenceAttributeNames[a])) {
				
				//	get attribute values
				Object referenceAttribute = reference.getAttribute(referenceAttributeNames[a]);
				Object toTestAttribute = toTest.getAttribute(referenceAttributeNames[a]);
				
				//	both are null, we're OK
				if ((toTestAttribute == null) && (referenceAttribute == null))
					continue;
				
				//	either one is null ==> not equal
				if ((toTestAttribute == null) || (referenceAttribute == null))
					return false;
				
				//	values not equal ==> not equal
				if (!referenceAttribute.equals(toTestAttribute))
					return false;
			}
		}
		
		//	get attribute names of test object
		String[] toTestAttributeNames = toTest.getAttributeNames();
		
		//	process test attributes one by one (containment is sufficient here, since checks for equality have been done above)
		for (int a = 0; a < toTestAttributeNames.length; a++) {
			
			//	check filter
			if ((filter == null) || filter.contains(toTestAttributeNames[a])) {
				if (!reference.hasAttribute(toTestAttributeNames[a]))
					return false;
			}
		}
		
		//	all checks successful
		return true;
	}
}
