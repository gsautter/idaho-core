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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed;
import de.uka.ipd.idaho.gamta.util.gPath.GPath;
import de.uka.ipd.idaho.gamta.util.gPath.GPathExpression;
import de.uka.ipd.idaho.gamta.util.gPath.GPathParser;
import de.uka.ipd.idaho.gamta.util.gPath.exceptions.GPathSyntaxException;
import de.uka.ipd.idaho.gamta.util.gPath.types.GPathObject;
import de.uka.ipd.idaho.stringUtils.regExUtils.RegExUtils;

/**
 * Instances of this class describe the style of a particular type or class of
 * documents, e.g. articles from a specific journal, by means of any kind of
 * parameters. This can range from the font styles used for headings of
 * individual levels to the format of named entities like dates to font styles
 * and position information of bibliographic attributes in the article header
 * to the styles of bibliographic references, and so forth.<br>
 * All parameter values are stored as strings internally. Their external values
 * can be of other types as well, however. Values of primitive types, lists,
 * and bounding boxes are recovered via the <code>parseXyz(String)</code>
 * methods of the respective (wrapper) classes.<br>
 * It is recommended to group parameters by means of prefixing their names,
 * e.g. <code>docMeta.title.fontStyle</code> for the font style used in the
 * document title. This helps group parameters that refer to individual aspects
 * of document analysis. In addition, prefixed views can considerably shorten
 * parameter names in client code.<br>
 * Because it is impossible to a priory know all the style parameters used by
 * individual document markup and data extraction tools, this class learns all
 * parameter names it is asked for, together with their types. This allows
 * listing the names of all parameters ever asked for, and simplifies filling
 * any gaps, even if they emerge only after updates.<br>
 * To abstract the location where actual style parameter lists are stored, this
 * class statically provides a central hub for sources of parameter lists. The
 * <code>getStyleFor()</code> methods delegate to the registered providers, so
 * the latter can vary freely depending on each individual deployment scenario.
 * 
 * @author sautter
 */
public class DocumentStyle implements Attributed {
	
	/** the attribute storing the style of a document */
	public static final String DOCUMENT_STYLE_ATTRIBUTE = "docStyle";
	
	/** the attribute storing the identifier of a document style, to facilitate tracking, and as a hint for providers */
	public static final String DOCUMENT_STYLE_ID_ATTRIBUTE = "docStyleId";
	
	/** the attribute storing the name of the style of a document, as a hint for providers */
	public static final String DOCUMENT_STYLE_NAME_ATTRIBUTE = "docStyleName";
	
	/** the attribute storing the version number of a document style, for applications that actually perform versioning */
	public static final String DOCUMENT_STYLE_VERSION_ATTRIBUTE = "docStyleVersion";
	
	/** the attribute storing the last modification timestamp of a document style, for applications that actually perform change tracking */
	public static final String DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE = "docStyleLastModified";
	
	/**
	 * Abstract provider of document style parameter lists.
	 * 
	 * @author sautter
	 */
	public static interface Provider {
		
		/**
		 * Obtain the style parameter list for a given document, represented as
		 * a generic <code>Attributed</code> object. If a provider does not
		 * have a parameter list for the argument document, or the argument
		 * document cannot be matched to any parameter list, it has to return
		 * <code>null</code>.
		 * @param doc the document to obtain the style for
		 * @return the style parameter list for the argument document
		 */
		public abstract DocumentStyle getStyleFor(Attributed doc);
		
		/**
		 * Take any provider specific actions after a document style has been
		 * assigned to a document
		 * @param docStyle the document style that was assigned
		 * @param doc the document the argument style was assigned to
		 */
		public abstract void documentStyleAssigned(DocumentStyle docStyle, Attributed doc);
	}
	
//	private static LinkedHashSet providers = new LinkedHashSet(2);
	private static Set providers = Collections.synchronizedSet(new LinkedHashSet());
	
	/**
	 * Add a document style provider.
	 * @param dsp the provider to add
	 */
	public static void addProvider(Provider dsp) {
		if (dsp != null)
			providers.add(dsp);
	}
	
	/**
	 * Remove a document style provider.
	 * @param dsp the provider to remove
	 */
	public static void removeProvider(Provider dsp) {
		providers.remove(dsp);
	}
	
	/**
	 * Obtain the style parameter list for a given document, represented as a
	 * generic <code>Attributed</code> object. This method first checks if the 
	 * argument document already has a <code>DocumentStyle</code> attached to
	 * if in its 'docStyle' attribute, and if so, returns it. Otherwise, this 
	 * method delegates to the registered providers. If none are registered, or
	 * none has a style parameter list for the argument document, this method
	 * returns an empty <code>DocumentStyle</code> object, but never
	 * <code>null</code>. In any case, this method attempts to store the
	 * returned <code>DocumentStyle</code> in the 'docStyle' attribute for
	 * easier access later on.
	 * @param doc the document to obtain the style for
	 * @return the style parameter list for the argument document
	 */
	public static DocumentStyle getStyleFor(Attributed doc) {
		if (doc == null)
			return null;
		Object dsObj = doc.getAttribute(DOCUMENT_STYLE_ATTRIBUTE);
		if (dsObj instanceof DocumentStyle)
			return ((DocumentStyle) dsObj);
		DocumentStyle ds = null;
//		for (Iterator pit = providers.iterator(); pit.hasNext();) {
//			Provider dsp = ((Provider) pit.next());
//			ds = dsp.getStyleFor(doc);
//			if (ds != null) {
//				ds.getData().setProvider(dsp);
//				break;
//			}
//		}
		Provider[] dsps;
		synchronized (providers) {
			dsps = ((Provider[]) providers.toArray(new Provider[providers.size()]));
		}
		for (int p = 0; p < dsps.length; p++) {
			ds = dsps[p].getStyleFor(doc);
			if (ds != null) {
				ds.getData().setProvider(dsps[p]);
				break;
			}
		}
		if (ds != null) try {
			doc.setAttribute(DOCUMENT_STYLE_ATTRIBUTE, ds);
			if (ds.hasAttribute(DOCUMENT_STYLE_ID_ATTRIBUTE))
				doc.setAttribute(DOCUMENT_STYLE_ID_ATTRIBUTE, ds.getAttribute(DOCUMENT_STYLE_ID_ATTRIBUTE));
			if (ds.hasAttribute(DOCUMENT_STYLE_NAME_ATTRIBUTE))
				doc.setAttribute(DOCUMENT_STYLE_NAME_ATTRIBUTE, ds.getAttribute(DOCUMENT_STYLE_NAME_ATTRIBUTE));
			Provider dsp = ds.getData().getProvider();
			if (dsp != null)
				dsp.documentStyleAssigned(ds, doc);
		} catch (Throwable t) { /* catch any exception thrown from immutable documents, etc. */ }
		return ds;
	}
	
	/**
	 * An object holding the actual data of a document style parameter list.
	 * 
	 * @author sautter
	 */
	public static interface Data extends Attributed {
		
		/**
		 * Retrieve the string value of a document style parameter.
		 * @param key the hashtable key
		 * @return the string value of a document style parameter
		 */
		public abstract String getPropertyData(String key);
		
		/**
		 * Retrieve the names of all properties.
		 * @return an array holding the property names
		 */
		public abstract String[] getPropertyNames();
		
		/**
		 * Retrieve the provider the document style data object was loaded
		 * from. This facilitates tracking, custom post-assignment handling,
		 * etc. If the document style data object was loaded through other
		 * means, this method returns null.
		 * @return the provider
		 */
		public abstract Provider getProvider();
		
		/**
		 * Inject the provider the document style data object was loaded
		 * from. This facilitates tracking, custom post-assignment handling,
		 * etc.
		 * @param provider the provider to set
		 */
		public abstract void setProvider(Provider provider);
	}
	
	/**
	 * Abstract convenience implementation of <code>Data</code> providing the
	 * implementation of <code>Attributed</code> and a getter and setter pair
	 * for the provider.
	 * 
	 * @author sautter
	 */
	public static abstract class AbstractData extends AbstractAttributed implements Data {
		private Provider provider;
		
		/** Constructor for general purposes
		 */
		public AbstractData() {
			this(null);
		}
		
		/** Constructor specifying the provider
		 * @param provider the provider the data was loaded from
		 */
		public AbstractData(Provider provider) {
			this.provider = provider;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Data#getProvider()
		 */
		public Provider getProvider() {
			return this.provider;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Data#setProvider(de.uka.ipd.idaho.gamta.util.DocumentStyle.Provider)
		 */
		public void setProvider(Provider provider) {
			this.provider = provider;
		}
	}
	
	/**
	 * Convenience implementation of <code>AbstractData</code> wrapping a
	 * <code>Properties</code> object as the source for values.
	 * 
	 * @author sautter
	 */
	public static class PropertiesData extends AbstractData {
		private Properties data;
		/** Constructor for general purposes
		 * @param data the Properties object to wrap
		 */
		public PropertiesData(Properties data) {
			this(data, null);
		}
		
		/** Constructor specifying the provider
		 * @param data the Properties object to wrap
		 * @param provider the provider the data was loaded from
		 */
		public PropertiesData(Properties data, Provider provider) {
			super(provider);
			this.data = data;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Data#getPropertyData(java.lang.String)
		 */
		public String getPropertyData(String key) {
			return this.data.getProperty(key);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Data#getPropertyNames()
		 */
		public String[] getPropertyNames() {
			TreeSet propertyNames = new TreeSet(this.data.keySet());
			return ((String[]) propertyNames.toArray(new String[propertyNames.size()]));
		}
	}
	
	/**
	 * Anchors match on documents to indicate whether or not a style parameter
	 * list is applicable to the document.
	 * 
	 * @author sautter
	 */
	public static abstract class Anchor {
		
		/** the prefix of a document style template holding anchor information, namely 'anchor' */
		public static final String ANCHOR_PREFIX = "anchor";
		
		/** the name of the property whose presence indicates an anchor is essential, namely 'isRequired' */
		public static final String IS_REQUIRED_PROPERTY = "isRequired";
		
		/**	the name of the anchor */
		public final String name;
		
		/** flag indicating whether or not the anchor is required for a match (i.e., a mismatch should prevent an overall match to a document style) */
		public final boolean isRequired;
		
		/** Constructor (creates non-essential anchors)
		 * @param name the name of the anchor
		 */
		protected Anchor(String name) {
			this(name, false);
		}
		
		/** Constructor
		 * @param name the name of the anchor
		 * @param isRequired is the anchor essential for a document style?
		 */
		protected Anchor(String name, boolean isRequired) {
			this.name = name;
			this.isRequired = isRequired;
		}
		
		/**
		 * Check whether or not the anchor matches a given document,
		 * represented as a generic <code>Attributed</code> object.
		 * @param doc the document to check
		 * @return true if the anchor matches the argument document
		 */
		public abstract boolean matches(Attributed doc);
		
		/**
		 * Factory instantiating anchors from document style template subsets that
		 * hold the required properties.
		 * 
		 * @author sautter
		 */
		public static interface Factory {
			
			/**
			 * Instantiate an anchor from a document style template subset. If
			 * the argument document style template subset doesn't contain the
			 * required properties for the factory to build its specific type
			 * of anchor, this method should return null.
			 * @param name the name of the anchor
			 * @param docStyle the document style template subsets to use
			 * @return the anchor built from the argument document style
			 *    template subsets
			 */
			public abstract Anchor getAnchor(String name, DocumentStyle docStyle);
		}
		
		private static LinkedHashSet factories = new LinkedHashSet(2);
		
		/**
		 * Add a factory to help instantiate anchors.
		 * @param dsp the provider to add
		 */
		protected static void addFactory(Factory dsaf) {
			if (dsaf != null)
				factories.add(dsaf);
		}
		
		/**
		 * Instantiate anchors from the first level subsets of a document style
		 * template (subset).
		 * @param docStyle the document style template (subset) to use
		 * @return an array holding the anchors
		 */
		public static Anchor[] getAnchors(DocumentStyle docStyle) {
			String[] ssps = docStyle.getSubsetPrefixes();
			for (int p = 0; p < ssps.length; p++) {
				if (ANCHOR_PREFIX.equals(ssps[p]))
					return getAnchors(docStyle.getSubset(ANCHOR_PREFIX));
			}
			try {
				docStyle.setRememberPropertyNames(false); // must not remember what anchor factories ask for
				ArrayList dsas = new ArrayList();
				for (int p = 0; p < ssps.length; p++) {
					DocumentStyle adss = docStyle.getSubset(ssps[p]);
					Anchor dsa = null;
					for (Iterator afit = factories.iterator(); afit.hasNext();) {
						dsa = ((Factory) afit.next()).getAnchor(ssps[p], adss);
						if (dsa != null)
							break;
					}
					if (dsa != null)
						dsas.add(dsa);
				}
				return ((Anchor[]) dsas.toArray(new Anchor[dsas.size()]));
			}
			finally {
				docStyle.setRememberPropertyNames(true); // must remember everything else, though
			}
		}
		
		private static Map parameterNamesToValueClasses = Collections.synchronizedMap(new TreeMap());
		
		/**
		 * Retrieve the class of the values of a given anchor parameter. If the
		 * argument parameter name has not been mapped, this method returns the
		 * default class <code>String</code>. The argument name must be the
		 * local parameter name, as the anchor name based infix in the fully
		 * qualified parameter name is variable.
		 * @param pn the name of the parameter
		 * @return the class of the parameter values
		 */
		public static Class getParameterValueClass(String pn) {
			Class cls = ((Class) parameterNamesToValueClasses.get(pn));
			return ((cls == null) ? String.class : cls);
		}
		
		/**
		 * Map an anchor parameter name to the class of its values. This method
		 * is intended for sub classes to add the value classes of the specific
		 * parameter names they use.  The argument name must be the local
		 * parameter name, as the anchor name based infix in the fully
		 * qualified parameter name must remain variable.
		 * @param paramName the name of the anchor parameter
		 * @param valueClass the class of the anchor parameter values
		 */
		protected static void mapParameterValueClass(String paramName, Class valueClass) {
			parameterNamesToValueClasses.put(paramName, valueClass);
		}
	}
	
	/**
	 * Anchors checking for the presence of an attribute and optionally whether
	 * or not the attribute value matches a given patter. This class is
	 * intended to be used in <code>Provider</code> implementations to help
	 * match style parameter lists to documents.
	 * 
	 * @author sautter
	 */
	public static class AttributeAnchor extends Anchor {
		
		/** the name of the property holding the name of the attribute whose value to test, namely 'attributeName' */
		public static final String ATTRIBUTE_NAME_PROPERTY = "attributeName";
		
		/** the name of the property holding the pattern to test the attribute value against, namely 'valuePattern' */
		public static final String VALUE_PATTERN_PROPERTY = "valuePattern";
		
		private String attributeName;
		private Pattern valuePattern;
		
		/**
		 * @param name the name of the anchor
		 * @param attributeName the name of the attribute to check
		 */
		public AttributeAnchor(String name, String attributeName) {
			this(name, attributeName, ((Pattern) null));
		}
		
		/**
		 * @param name the name of the anchor
		 * @param isRequired is the anchor essential for a document style?
		 * @param attributeName the name of the attribute to check
		 */
		public AttributeAnchor(String name, boolean isRequired, String attributeName) {
			this(name, isRequired, attributeName, ((Pattern) null));
		}
		
		/**
		 * @param name the name of the anchor
		 * @param attributeName the name of the attribute to check
		 * @param valuePattern the pattern to match the attribute value against
		 */
		public AttributeAnchor(String name, String attributeName, String valuePattern) {
			this(name, attributeName, Pattern.compile(valuePattern));
		}
		
		/**
		 * @param name the name of the anchor
		 * @param isRequired is the anchor essential for a document style?
		 * @param attributeName the name of the attribute to check
		 * @param valuePattern the pattern to match the attribute value against
		 */
		public AttributeAnchor(String name, boolean isRequired, String attributeName, String valuePattern) {
			this(name, isRequired, attributeName, Pattern.compile(valuePattern));
		}
		
		/**
		 * @param name the name of the anchor
		 * @param attributeName the name of the attribute to check
		 * @param valuePattern the pattern to match the attribute value against
		 */
		public AttributeAnchor(String name, String attributeName, Pattern valuePattern) {
			this(name, false, attributeName, valuePattern);
		}
		
		/**
		 * @param name the name of the anchor
		 * @param isRequired is the anchor essential for a document style?
		 * @param attributeName the name of the attribute to check
		 * @param valuePattern the pattern to match the attribute value against
		 */
		public AttributeAnchor(String name, boolean isRequired, String attributeName, Pattern valuePattern) {
			super(name, isRequired);
			this.attributeName = attributeName;
			this.valuePattern = valuePattern;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Anchor#matches(de.uka.ipd.idaho.gamta.Attributed)
		 */
		public boolean matches(Attributed doc) {
			Object valueObj = doc.getAttribute(this.attributeName);
			if (valueObj == null)
				return false;
			if (this.valuePattern == null)
				return true;
			return this.valuePattern.matcher(valueObj.toString()).matches();
		}
		
		static void installFactory(/* need method call to trigger static initializers */) {
			Anchor.addFactory(new Factory() {
				public Anchor getAnchor(String name, DocumentStyle docStyle) {
					String an = docStyle.getStringProperty(ATTRIBUTE_NAME_PROPERTY, null);
					if (an == null)
						return null;
					String vps = docStyle.getStringProperty(VALUE_PATTERN_PROPERTY, null);
					if (vps == null)
						return new AttributeAnchor(name, docStyle.getBooleanProperty(IS_REQUIRED_PROPERTY, false), an, vps);
					try {
						Pattern vp = Pattern.compile(vps);
						return new AttributeAnchor(name, docStyle.getBooleanProperty(IS_REQUIRED_PROPERTY, false), an, vp);
					}
					catch (PatternSyntaxException pse) {
						System.out.println("AttributeAnchor: invalid value pattern - " + vps);
						pse.printStackTrace(System.out);
						return null;
					}
				}
			});
		}
		
		/** parameter group description for attribute anchors, for use in a UI */
		public static final ParameterGroupDescription PARAMETER_GROUP_DESCRIPTION;
		static {
			PARAMETER_GROUP_DESCRIPTION = new ParameterGroupDescription("attributeAnchor");
			PARAMETER_GROUP_DESCRIPTION.setLabel("Attribute Based Anchors");
			PARAMETER_GROUP_DESCRIPTION.setDescription("Attribute based anchors check for the presence of a given attribute and match if the attribute is present and its value matches the given pattern (if the latter is present).");
			ParameterDescription anPd = new ParameterDescription("attributeAnchor." + ATTRIBUTE_NAME_PROPERTY);
			anPd.setLabel("Attribute Name");
			anPd.setDescription("The name of the attribute to check for");
			anPd.setRequired();
			PARAMETER_GROUP_DESCRIPTION.setParameterDescription(ATTRIBUTE_NAME_PROPERTY, anPd);
			PARAMETER_GROUP_DESCRIPTION.setParamLabel(VALUE_PATTERN_PROPERTY, "Value Pattern");
			PARAMETER_GROUP_DESCRIPTION.setParamDescription(VALUE_PATTERN_PROPERTY, "A pattern to check the attribute value against");
			PARAMETER_GROUP_DESCRIPTION.setParamLabel(IS_REQUIRED_PROPERTY, "Is the Anchor Required to Match for an Overall Match?");
			PARAMETER_GROUP_DESCRIPTION.setParamDescription(IS_REQUIRED_PROPERTY, "A mismatch on a required anchor fails the overall match of a document style regardless how many other anchors do match a given document.");
			
			mapParameterValueClass(ATTRIBUTE_NAME_PROPERTY, String.class);
			mapParameterValueClass(VALUE_PATTERN_PROPERTY, Pattern.class);
			mapParameterValueClass(IS_REQUIRED_PROPERTY, Boolean.class);
		}
	}
	
	/**
	 * Anchors evaluating a GPath expression on a document. Applicable only to
	 * documents implementing <code>QueriableAnnotation</code>. This class is
	 * intended to be used in <code>Provider</code> implementations to help
	 * match style parameter lists to documents.
	 * 
	 * @author sautter
	 */
	public static class GPathAnchor extends Anchor {
		
		/** the name of the property holding the GPath expression to evaluate on documents, namely 'testExpression' */
		public static final String TEST_EXPRESSION_PROPERTY = "testExpression";
		
		private GPathExpression testExpression;
		
		/**
		 * @param name the name of the anchor
		 * @param testExpression the path expression to use for testing
		 */
		public GPathAnchor(String name, String testExpression) {
			this(name, GPathParser.parseExpression(testExpression));
		}
		
		/**
		 * @param name the name of the anchor
		 * @param isRequired is the anchor essential for a document style?
		 * @param testExpression the path expression to use for testing
		 */
		public GPathAnchor(String name, boolean isRequired, String testExpression) {
			this(name, isRequired, GPathParser.parseExpression(testExpression));
		}
		
		/**
		 * @param name the name of the anchor
		 * @param testExpression the path expression to use for testing
		 */
		public GPathAnchor(String name, GPathExpression testExpression) {
			this(name, false, testExpression);
		}
		
		/**
		 * @param name the name of the anchor
		 * @param isRequired is the anchor essential for a document style?
		 * @param testExpression the path expression to use for testing
		 */
		public GPathAnchor(String name, boolean isRequired, GPathExpression testExpression) {
			super(name, isRequired);
			this.testExpression = testExpression;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Anchor#matches(de.uka.ipd.idaho.gamta.Attributed)
		 */
		public boolean matches(Attributed doc) {
			if (doc instanceof QueriableAnnotation) {
				GPathObject result = GPath.evaluateExpression(this.testExpression, ((QueriableAnnotation) doc), null);
				return ((result == null) ? false : result.asBoolean().value);
			}
			else return false;
		}
		
		static void installFactory(/* need method call to trigger static initializers */) {
			Anchor.addFactory(new Factory() {
				public Anchor getAnchor(String name, DocumentStyle docStyle) {
					String te = docStyle.getStringProperty(TEST_EXPRESSION_PROPERTY, null);
					if (te == null)
						return null;
					try {
						GPathExpression gpte = GPathParser.parseExpression(te);
						return new GPathAnchor(name, docStyle.getBooleanProperty(IS_REQUIRED_PROPERTY, false), gpte);
					}
					catch (GPathSyntaxException gpse) {
						System.out.println("GPathAnchor: invalid GPath expression - " + te);
						gpse.printStackTrace(System.out);
						return null;
					}
				}
			});
		}
		
		/** parameter group description for GPath anchors, for use in a UI */
		public static final ParameterGroupDescription PARAMETER_GROUP_DESCRIPTION;
		static {
			PARAMETER_GROUP_DESCRIPTION = new ParameterGroupDescription("gPathAnchor");
			PARAMETER_GROUP_DESCRIPTION.setLabel("GPath Anchors");
			PARAMETER_GROUP_DESCRIPTION.setDescription("GPath anchors evaluate a given expression against documents and match if the expression evaluates to 'true'.");
			ParameterDescription tePd = new ParameterDescription("gPathAnchor." + TEST_EXPRESSION_PROPERTY);
			tePd.setLabel("Test Expression");
			tePd.setDescription("The GPath expression to evaluate against documents");
			tePd.setRequired();
			PARAMETER_GROUP_DESCRIPTION.setParameterDescription(TEST_EXPRESSION_PROPERTY, tePd);
			PARAMETER_GROUP_DESCRIPTION.setParamLabel(IS_REQUIRED_PROPERTY, "Is the Anchor Required to Match for an Overall Match?");
			PARAMETER_GROUP_DESCRIPTION.setParamDescription(IS_REQUIRED_PROPERTY, "A mismatch on a required anchor fails the overall match of a document style regardless how many other anchors do match a given document.");
			
			mapParameterValueClass(TEST_EXPRESSION_PROPERTY, String.class); // TODO use GPath type (soon as we have extensible system)
			mapParameterValueClass(IS_REQUIRED_PROPERTY, Boolean.class);
		}
	}
	
	private static Map parameterNamesToValueClasses = Collections.synchronizedMap(new TreeMap());
	private static Map listClassNamesElementClasses = Collections.synchronizedMap(new HashMap());
	
	/**
	 * Retrieve a list of all style parameter names requested so far. This
	 * enables learning new parameters manes and filling any gaps that might
	 * emerge.
	 * @return a list of all style parameter names
	 */
	public static String[] getParameterNames() {
		return ((String[]) parameterNamesToValueClasses.keySet().toArray(new String[parameterNamesToValueClasses.size()]));
	}
	
	/**
	 * Retrieve the class of the values of a given parameter. If the argument
	 * parameter has not been requested yet, this method returns the default
	 * class <code>String</code>.
	 * @param pn the name of the parameter
	 * @return the class of the parameter values
	 */
	public static Class getParameterValueClass(String pn) {
		Class cls = ((Class) parameterNamesToValueClasses.get(pn));
		return ((cls == null) ? String.class : cls);
	}
	
	/**
	 * Map a parameter name to the class of its values. This method is intended
	 * for sub classes to add their own specific types of properties to the
	 * list of available parameter names.
	 * @param paramName the fully qualified name of the parameter
	 * @param valueClass the class of the parameter values
	 */
	private static void mapParameterValueClass(String paramName, Class valueClass) {
		if (paramName.startsWith(Anchor.ANCHOR_PREFIX + "."))
			return; // no mapping for anchor properties
		parameterNamesToValueClasses.put(paramName, valueClass);
	}
	
	/**
	 * Retrieve the class of the entries of a list value class.
	 * @param listClass the list value class
	 * @return the list entry class
	 */
	public static Class getListElementClass(Class listClass) {
//		if (listClass.getName().equals(stringListClass.getName()))
//			return String.class;
//		else if (listClass.getName().equals(intListClass.getName()))
//			return Integer.class;
//		else if (listClass.getName().equals(floatListClass.getName()))
//			return Float.class;
//		else if (listClass.getName().equals(doubleListClass.getName()))
//			return Double.class;
//		else if (listClass.getName().equals(booleanListClass.getName()))
//			return Boolean.class;
//		else return listClass;
		Class elementClass = ((Class) listClassNamesElementClasses.get(listClass.getName()));
		return ((elementClass == null) ? listClass : elementClass);
	}
	
	/**
	 * Map a list (array) class to the class of its contained elements. This
	 * method is intended for sub classes to add their own specific types of
	 * properties.
	 * @param listClass the list class
	 * @param elementClass the element class
	 */
	protected static void mapListElementClass(Class listClass, Class elementClass) {
		listClassNamesElementClasses.put(listClass.getName(), elementClass);
	}
	
	/**
	 * Descriptor object for a group of document style parameters, i.e., all
	 * parameters that share the same name right up to and including the last
	 * period. This class enables consumers of such parameter groups to provide
	 * labels, explanations, and custom testing facilities for the parameters
	 * they use, for instance in order to augment editing facilities with more
	 * user friendly information about the semantics of the parameters and
	 * parameter groups they use.<br/>
	 * Attention: some parameter groups might be used by multiple consumers. In
	 * such cases, the code providing a parameter group description should check
	 * for any existing descriptions to augment first.
	 * 
	 * @author sautter
	 */
	public static class ParameterGroupDescription {
		
		/** the prefix shared between all parameter names in the group, _excluding_ the terminal period */
		public final String parameterNamePrefix;
		
		private String label;
		private String description;
		
		private HashMap parameterNamesToDescriptions = new HashMap();
		
		/** Constructor
		 * @param pnp the prefix shared between all parameter names in the group
		 */
		public ParameterGroupDescription(String pnp) {
			this.parameterNamePrefix = pnp;
		}
		
		/**
		 * Retrieve the label of the parameter group.
		 * @return the label
		 */
		public String getLabel() {
			return this.label;
		}
		
		/**
		 * Provide a label for the parameter group, e.g. for displaying in a UI.
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}
		
		/**
		 * Retrieve the description of the parameter group.
		 * @return the description
		 */
		public String getDescription() {
			return this.description;
		}
		
		/**
		 * Provide a description for the parameter group, e.g. for displaying
		 * in a UI.
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		
		/**
		 * Retrieve the names of the parameters currently described in this
		 * group description.
		 * @return an array holding the parameter names
		 */
		public String[] getParameterNames() {
			return ((String[]) this.parameterNamesToDescriptions.keySet().toArray(new String[this.parameterNamesToDescriptions.size()]));
		}
		
		/**
		 * Retrieve the description for a parameter in the group. The argument
		 * name has to be without the group prefix.
		 * @param pn the name of the parameter to obtain the label for
		 * @return the description for the argument parameter
		 */
		public ParameterDescription getParameterDescription(String pn) {
			pn = pn.substring(pn.lastIndexOf(".") + ".".length());
			return this.getParameterDescription(pn, false);
		}
		
		/**
		 * Provide a description for a parameter in the group, e.g. for displaying
		 * in a UI. The argument name has to be without the group prefix.
		 * @param pn the name of the parameter to set the label for
		 * @param pd the description for the argument parameter
		 */
		public void setParameterDescription(String pn, ParameterDescription pd) {
			String lpn = pn.substring(pn.lastIndexOf(".") + ".".length());
			if (pd == null) {
				this.parameterNamesToDescriptions.remove(lpn);
				return;
			}
			ParameterDescription epd = this.getParameterDescription(pn, false);
			this.parameterNamesToDescriptions.put(pn, pd);
			if (epd == null)
				return;
			if (pd.getLabel() == null)
				pd.setLabel(epd.getLabel());
			if (pd.getDescription() == null)
				pd.setDescription(epd.getDescription());
			if (pd.getValues() == null)
				pd.setValues(epd.getValues());
			String[] pdValues = pd.getValues();
			if (pdValues == null)
				return;
			for (int v = 0; v < pdValues.length; v++) {
				if (pd.getValueLabel(pdValues[v]) == null)
					pd.setValueLabel(pdValues[v], epd.getValueLabel(pdValues[v]));
			}
		}
		
		private ParameterDescription getParameterDescription(String lpn, boolean create) {
			ParameterDescription pd = ((ParameterDescription) this.parameterNamesToDescriptions.get(lpn));
			if ((pd == null) && create) {
				pd = new ParameterDescription(this.parameterNamePrefix + "." + lpn);
				this.parameterNamesToDescriptions.put(lpn, pd);
			}
			return pd;
		}
		
		/**
		 * Retrieve the label for a parameter in the group. The argument name
		 * has to be without the group prefix.
		 * @param pn the name of the parameter to obtain the label for
		 * @return the label for the argument parameter
		 */
		public String getParamLabel(String pn) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			return ((pd == null) ? null : pd.getLabel());
		}
		
		/**
		 * Provide a label for a parameter in the group, e.g. for displaying
		 * in a UI. The argument name has to be without the group prefix.
		 * @param pn the name of the parameter to set the label for
		 * @param label the label for the argument parameter
		 */
		public void setParamLabel(String pn, String label) {
			ParameterDescription pd = this.getParameterDescription(pn, (label != null));
			if (pd != null)
				pd.setLabel(label);
		}
		
		/**
		 * Retrieve the description for a parameter in the group. The argument
		 * name has to be without the group prefix.
		 * @param pn the name of the parameter to obtain the label for
		 * @return the description for the argument parameter
		 */
		public String getParamDescription(String pn) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			return ((pd == null) ? null : pd.getDescription());
		}
		
		/**
		 * Provide a description for a parameter in the group, e.g. for displaying
		 * in a UI. The argument name has to be without the group prefix.
		 * @param pn the name of the parameter to set the label for
		 * @param description the description for the argument parameter
		 */
		public void setParamDescription(String pn, String description) {
			ParameterDescription pd = this.getParameterDescription(pn, (description != null));
			if (pd != null)
				pd.setDescription(description);
		}
		
		/**
		 * Retrieve the default value of a parameter in the group. If there is
		 * a list of permitted values, it must contain any default value.
		 * @param pn the name of the parameter to obtain the default value for
		 * @return the default value of the parameter
		 */
		public String getParamDefaultValue(String pn) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			return ((pd == null) ? null : pd.getDefaultValue());
		}
		
		/**
		 * Provide the default value for a parameter in the group. If there is
		 * a list of permitted values for the same parameter, the default value
		 * must be contained in it. If it is not, the list of permitted values
		 * is erased.
		 * @param pn the name of the parameter to set the default value for
		 * @param defaultValue the default value to set
		 */
		public void setParamDefaultValue(String pn, String defaultValue) {
			ParameterDescription pd = this.getParameterDescription(pn, (defaultValue != null));
			if (pd != null)
				pd.setDefaultValue(defaultValue);
		}
		
		/**
		 * Retrieve a list of permitted values for a parameter in the group.
		 * This is particularly useful if there are only few meaningful values.
		 * The argument name has to be without the group prefix.
		 * @param pn the name of the parameter to obtain the values for
		 * @return the values for the argument parameter
		 */
		public String[] getParamValues(String pn) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			return ((pd == null) ? null : pd.getValues());
		}
		
		/**
		 * Provide a list of permitted values for a parameter in the group,
		 * e.g. for displaying a selection in a UI. This is particularly useful
		 * if there are only few meaningful values. The argument name has to be
		 * without the group prefix. If there is a default value for the same
		 * parameter, it must be contained in the argument list, or it is
		 * erased.
		 * @param pn the name of the parameter to set the values for
		 * @param values the values for the argument parameter
		 */
		public void setParamValues(String pn, String[] values) {
			ParameterDescription pd = this.getParameterDescription(pn, (values != null));
			if (pd != null)
				pd.setValues(values);
		}
		
		/**
		 * Retrieve the label for a specific value of a parameter in the group.
		 * The argument parameter name has to be without the group prefix.
		 * @param pn the name of the parameter to obtain the label for
		 * @param pv the parameter value to obtain the label for
		 * @return the label for the argument parameter
		 */
		public String getParamValueLabel(String pn, String pv) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			return ((pd == null) ? null : pd.getValueLabel(pv));
		}
		
		/**
		 * Provide a label for a specific value of a parameter in the group,
		 * e.g. for displaying in a UI. The argument name has to be without the
		 * group prefix.
		 * @param pn the name of the parameter to set the label for
		 * @param pv the parameter value to obtain the label for
		 * @param label the label for the argument parameter
		 */
		public void setParamValueLabel(String pn, String pv, String label) {
			ParameterDescription pd = this.getParameterDescription(pn, (label != null));
			if (pd != null)
				pd.setValueLabel(pv, label);
		}
		
		/**
		 * Mark a parameter as required for the whole parameter group to be
		 * useful. This is intended to help semantics in a configuration UI,
		 * e.g. by highlighting respective input fields. The argument name has
		 * to be without the group prefix.
		 * @param pn the name of the parameter to mark as required
		 */
		public void setParamRequired(String pn) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			if (pd != null)
				pd.setRequired();
		}
		
		/**
		 * Check if a parameter is required for the whole parameter group to be
		 * useful. This is intended to help semantics in a configuration UI,
		 * e.g. by highlighting respective input fields. The argument name has
		 * to be without the group prefix.
		 * @param pn the name of the parameter to check
		 * @return true if the parameter is required
		 */
		public boolean isParamRequired(String pn) {
			ParameterDescription pd = this.getParameterDescription(pn, false);
			return ((pd != null) && pd.isRequired());
		}
	}
	
	/**
	 * Descriptor object for an individual document style parameters. This
	 * class enables consumers of these parameters to provide labels,
	 * explanations, and custom testing facilities for the parameters they use,
	 * for instance in order to augment editing facilities with more user
	 * friendly information about the semantics of the parameters they use.<br/>
	 * Attention: some parameters might be used by multiple consumers. In such
	 * cases, the code providing a parameter description should check for any
	 * existing descriptions to augment first.
	 * 
	 * @author sautter
	 */
	/**
	 * @author sautter
	 *
	 */
	public static class ParameterDescription {
		
		/** the full name of the parameter, including the prefix */
		public final String fullName;
		
		/** the group-local name of the parameter, excluding the prefix */
		public final String localName;
		
		private String label;
		private String description;
		
		private String defaultValue = null;
		private String[] values = null;
		private Properties valuesToLabels = new Properties();
		
		private boolean required = false;
		private HashMap valuesToRequiredParameters = null;
		private HashMap valuesToExcludedParameters = null;
		
		/** Constructor
		 * @param fpn the full name of the parameter, including the prefix
		 */
		public ParameterDescription(String fpn) {
			this.fullName = fpn;
			this.localName = this.fullName.substring(this.fullName.lastIndexOf(".") + ".".length());
		}
		
		/**
		 * Retrieve the label of the parameter.
		 * @return the label
		 */
		public String getLabel() {
			return this.label;
		}
		
		/**
		 * Provide a label for the parameter, e.g. for displaying in a UI.
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}
		
		/**
		 * Retrieve the description of the parameter.
		 * @return the description
		 */
		public String getDescription() {
			return this.description;
		}
		
		/**
		 * Provide a description for the parameter, e.g. for displaying in a UI.
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		
		/**
		 * Retrieve the default value of the parameter. If there is a list of
		 * permitted values, it must contain any default value.
		 * @return the default value of the parameter
		 */
		public String getDefaultValue() {
			return this.defaultValue;
		}
		
		/**
		 * Provide the default value of the parameter. If there is a list of
		 * permitted values, the default value must be contained in it. If it
		 * is not, the list of permitted values is erased.
		 * @param defaultValue the default value to set
		 */
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			if ((this.values == null) || (this.defaultValue == null))
				return;
			for (int v = 0; v < this.values.length; v++) {
				if (this.defaultValue.equals(this.values[v]))
					return;
			}
			this.values = null;
		}
		
		/**
		 * Retrieve a list of permitted values for the parameter. This is
		 * particularly useful if there are only few meaningful values.
		 * @return the values for the parameter
		 */
		public String[] getValues() {
			return this.values;
		}
		
		/**
		 * Provide a list of permitted values for the parameter, e.g. for
		 * displaying a selection in a UI. This is particularly useful if there
		 * are only few meaningful values. If there is a non-null default value
		 * for the parameter, it must be contained in the argument list, or it
		 * is erased
		 * @param values the values for the parameter
		 */
		public void setValues(String[] values) {
			this.values = values;
			if ((this.values == null) || (this.defaultValue == null))
				return;
			for (int v = 0; v < this.values.length; v++) {
				if (this.defaultValue.equals(this.values[v]))
					return;
			}
			this.defaultValue = null;
		}
		
		/**
		 * Retrieve the label for a specific value of the parameter.
		 * @param pv the parameter value to obtain the label for
		 * @return the label for the argument parameter name
		 */
		public String getValueLabel(String pv) {
			return this.valuesToLabels.getProperty(pv);
		}
		
		/**
		 * Provide a label for a specific value of the parameter, e.g. for
		 * displaying in a UI.
		 * @param pv the parameter value to set the label for
		 * @param label the label for the argument parameter value
		 */
		public void setValueLabel(String pv, String label) {
			if (label == null)
				this.valuesToLabels.remove(pv);
			else this.valuesToLabels.setProperty(pv, label);
		}
		
		/**
		 * Mark the parameter as required for the whole parameter group to be
		 * useful. This is intended to help semantics in a configuration UI,
		 * e.g. by highlighting respective input fields.
		 */
		public void setRequired() {
			this.required = true;
		}
		
		/**
		 * Check if the parameter is required for the whole parameter group to
		 * be useful. This is intended to help semantics in a configuration UI,
		 * e.g. by highlighting respective input fields.
		 * @return true if the parameter is required
		 */
		public boolean isRequired() {
			return this.required;
		}
		
		/**
		 * Indicate that a value of this parameter requires another parameter.
		 * Setting the argument value to null indicates using this parameter
		 * generally requires the argument parameter. This is intended to help
		 * reflecting dependencies in a configuration UI, e.g. by enabling and
		 * disabling input fields.
		 * @param pv the value of this parameter requiring the other parameter
		 * @param rpn the name of the required parameter
		 */
		public void addRequiredParameter(String pv, String rpn) {
			if (rpn == null)
				return;
			if (this.valuesToRequiredParameters == null)
				this.valuesToRequiredParameters = new HashMap();
			HashSet rpns = ((HashSet) this.valuesToRequiredParameters.get(pv));
			if (rpns == null) {
				rpns = new HashSet();
				this.valuesToRequiredParameters.put(pv, rpns);
			}
			rpns.add(rpn);
		}
		
		/**
		 * Check if a specific value of this parameter requires another
		 * parameter. This is intended to help reflecting dependencies in a
		 * configuration UI, e.g. by enabling and disabling input fields.
		 * @param pv the value of this parameter
		 * @param cpn the parameter name to check
		 * @return true if the argument value of this parameter requires the
		 *        argument parameter
		 */
		public boolean requiresParameter(String pv, String cpn) {
			if (cpn == null)
				return false;
			if (this.valuesToRequiredParameters == null)
				return false;
			HashSet rpns = ((HashSet) this.valuesToRequiredParameters.get(pv));
			if ((rpns != null) && (rpns.contains(cpn)))
				return true;
			return ((pv == null) ? false : this.requiresParameter(null, cpn));
		}
		
		/**
		 * Get the names of the other parameters required for this parameter to
		 * be useful. This is intended to help reflecting dependencies in a
		 * configuration UI, e.g. by enabling and disabling input fields.
		 * @return an array holding the names of the required parameters
		 */
		public String[] getRequiredParameters() {
			return this.getRequiredParameters(null);
		}
		
		/**
		 * Get the names of the other parameters required for a specific value
		 * of this parameter. This is intended to help reflecting dependencies
		 * in a configuration UI, e.g. by enabling and disabling input fields.
		 * @param pv the value of this parameter
		 * @return an array holding the names of the required parameters
		 */
		public String[] getRequiredParameters(String pv) {
			if (this.valuesToRequiredParameters == null)
				return null;
			HashSet rpns = ((HashSet) this.valuesToRequiredParameters.get(pv));
			return ((rpns == null) ? null : ((String[]) rpns.toArray(new String[rpns.size()])));
		}
		
		/**
		 * Indicate that a value of this parameter excludes using another
		 * parameter. Setting the argument value to null indicates using this
		 * parameter generally excludes the argument parameter. This is
		 * intended to help reflecting dependencies in a configuration UI,
		 * e.g. by enabling and disabling input fields.
		 * @param pv the value of this parameter excluding the other parameter
		 * @param epn the name of the excluded parameter
		 */
		public void addExcludedParameter(String pv, String epn) {
			if (this.valuesToExcludedParameters == null)
				this.valuesToExcludedParameters = new HashMap();
			HashSet epns = ((HashSet) this.valuesToExcludedParameters.get(pv));
			if (epns == null) {
				epns = new HashSet();
				this.valuesToExcludedParameters.put(pv, epns);
			}
			epns.add(epn);
		}
		
		/**
		 * Check if a specific value of this parameter excludes using another
		 * parameter. This is intended to help reflecting dependencies in a
		 * configuration UI, e.g. by enabling and disabling input fields.
		 * @param pv the value of this parameter
		 * @param cpn the parameter name to check
		 * @return true if the argument value of this parameter excludes using
		 *        the argument parameter
		 */
		public boolean excludesParameter(String pv, String cpn) {
			if (cpn == null)
				return false;
			if (this.valuesToExcludedParameters == null)
				return false;
			HashSet epns = ((HashSet) this.valuesToExcludedParameters.get(pv));
			if ((epns != null) && (epns.contains(cpn)))
				return true;
			return ((pv == null) ? false : this.excludesParameter(null, cpn));
		}
		
		/**
		 * Get the names of the other parameters excluded by this parameter.
		 * This is intended to help reflecting dependencies in a configuration
		 * UI, e.g. by enabling and disabling input fields.
		 * @return an array holding the names of the excluded parameters
		 */
		public String[] getExcludedParameters() {
			return this.getExcludedParameters(null);
		}
		
		/**
		 * Get the names of the other parameters excluded by a specific value
		 * of this parameter. This is intended to help reflecting dependencies
		 * in a configuration UI, e.g. by enabling and disabling input fields.
		 * @param pv the value of this parameter
		 * @return an array holding the names of the excluded parameters
		 */
		public String[] getExcludedParameters(String pv) {
			if (this.valuesToExcludedParameters == null)
				return null;
			HashSet epns = ((HashSet) this.valuesToExcludedParameters.get(pv));
			return ((epns == null) ? null : ((String[]) epns.toArray(new String[epns.size()])));
		}
	}
	
	/**
	 * Interface to be implemented by descriptions of parameters or parameter
	 * groups that can provide some form of testing functionality, e.g. some
	 * output that informs a user about how the current value of a parameter
	 * or value combination in a parameter group behaves.
	 * 
	 * @author sautter
	 */
	public static interface TestableElement {
		
		/**
		 * Test the parameter or parameter group described by the implementor.
		 * In practice, the argument parameter group will mostly be a subset of
		 * a document style.
		 * @param paramGroup the parameter group containing the current values
		 */
		public abstract void test(DocumentStyle paramGroup);
	}
	
	private static Map descriptionsByParameterGroupPrefix = Collections.synchronizedMap(new TreeMap());
	
	/**
	 * Retrieve a description for a parameter group with a given prefix.
	 * @param pnp the parameter group prefix to obtain a description for
	 */
	public static ParameterGroupDescription getParameterGroupDescription(String pnp) {
		return ((ParameterGroupDescription) descriptionsByParameterGroupPrefix.get(pnp));
	}
	
	/**
	 * Add a parameter group description.
	 * @param pgd the parameter group description to add
	 */
	public static void addParameterGroupDescription(ParameterGroupDescription pgd) {
		if ((pgd != null) && (pgd.parameterNamePrefix != null))
			descriptionsByParameterGroupPrefix.put(pgd.parameterNamePrefix, pgd);
	}
	
	/**
	 * Remove a parameter group description.
	 * @param pgd the parameter group description to remove
	 */
	public static void removeParameterGroupDescription(ParameterGroupDescription pgd) {
		if ((pgd != null) && (pgd.parameterNamePrefix != null))
			descriptionsByParameterGroupPrefix.remove(pgd.parameterNamePrefix);
	}
	
	/** the actual data of the document style (there is only a getter for this
	 * one by default, sub classes are free to provide a setter as needed) */
	protected Data data;
	
	private DocumentStyle base;
	private String prefix;
	
	private boolean rememberPropertyNames = true;
	
	/** Constructor for root object
	 * @param data the document style data object to wrap
	 */
	public DocumentStyle(Data data) {
		this.data = data;
		this.base = null;
		this.prefix = null;
	}
	
	/** Constructor for subsets
	 * @param parent the parent document style to query with the argument prefix
	 * @param prefix the prefix to query the parent with
	 */
	protected DocumentStyle(DocumentStyle parent, String prefix) {
		this.base = ((parent.base == null) ? parent : parent.base);
		if ((prefix.length() != 0) && !prefix.endsWith("."))
			prefix += ".";
		this.prefix = (((parent.prefix == null) ? "" : parent.prefix) + prefix);
	}
	
	//	need to be able to deactivate property name learning when checking parameters in anchor factories
	void setRememberPropertyNames(boolean rpn) {
		if (this.base == null)
			this.rememberPropertyNames = rpn;
		else this.base.setRememberPropertyNames(rpn);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#clearAttributes()
	 */
	public void clearAttributes() {
		if (this.base == null)
			this.data.clearAttributes();
		else throw new IllegalStateException("Cannot modify attributes on subset");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#copyAttributes(de.uka.ipd.idaho.gamta.Attributed)
	 */
	public void copyAttributes(Attributed source) {
		if (this.base == null)
			this.data.copyAttributes(source);
		else throw new IllegalStateException("Cannot modify attributes on subset");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#getAttribute(java.lang.String, java.lang.Object)
	 */
	public Object getAttribute(String name, Object def) {
		return ((this.base == null) ? this.data.getAttribute(name, def) : this.base.getAttribute(name, def));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return ((this.base == null) ? this.data.getAttribute(name) : this.base.getAttribute(name));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#getAttributeNames()
	 */
	public String[] getAttributeNames() {
		return ((this.base == null) ? this.data.getAttributeNames() : this.base.getAttributeNames());
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String name) {
		return ((this.base == null) ? this.data.hasAttribute(name) : this.base.hasAttribute(name));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#removeAttribute(java.lang.String)
	 */
	public Object removeAttribute(String name) {
		if (this.base == null)
			return this.data.removeAttribute(name);
		else throw new IllegalStateException("Cannot modify attributes on subset");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#setAttribute(java.lang.String)
	 */
	public void setAttribute(String name) {
		if (this.base == null)
			this.data.setAttribute(name);
		else throw new IllegalStateException("Cannot modify attributes on subset");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed#setAttribute(java.lang.String, java.lang.Object)
	 */
	public Object setAttribute(String name, Object value) {
		if (this.base == null)
			return this.data.setAttribute(name, value);
		else throw new IllegalStateException("Cannot modify attributes on subset");
	}
	
	/**
	 * Retrieve the wrapped data object.
	 * @return the wrapped data
	 */
	public Data getData() {
		return this.data;
	}
	
	/**
	 * Retrieve the value of a document style parameter from the underlying
	 * source. If the document style object is the root of its subset 
	 * hierarchy, this method remembers the argument parameter name and value
	 * class and does a lookup to the one-argument version of this method.
	 * Otherwise, it will merely delegate to the parent document style object,
	 * prepending its wrapped prefix to the argument key.
	 * @param key the hashtable key
	 * @param valueClass the class off the property value
	 * @return the value of a document style parameter
	 */
	public String getPropertyData(String key, Class valueClass) {
		if (this.base == null) {
			if (this.rememberPropertyNames)
				mapParameterValueClass(key, valueClass);
			return this.getPropertyData(key);
		}
		else return this.base.getPropertyData((this.prefix + key), valueClass);
	}
	
	/**
	 * Retrieve the value of a document style parameter from the underlying
	 * source. This method is only ever called by the root of a document
	 * style object subset hierarchy.
	 * @param key the hashtable key
	 * @return the value of a document style parameter
	 */
	public String getPropertyData(String key) {
		return this.data.getPropertyData(key);
	}
	
	/**
	 * Obtain a sublist of this document style parameter list, comprising all
	 * parameters whose name starts with the argument prefix plus a dot. Only
	 * the name part after the prefix needs to be specified to retrieve the
	 * parameters from the sublist. This is useful for eliminating the need to
	 * specify full parameter names in client code.
	 * @param prefix the prefix for the sublist
	 * @return a sublist with the argument prefix
	 */
	public DocumentStyle getSubset(String prefix) {
		return (((prefix == null) || (prefix.trim().length() == 0)) ? this : new DocumentStyle(this, prefix.trim()));
	}
	
	/**
	 * Retrieve a string property. If the argument key is not mapped to any
	 * value, or if its mapped value fails to parse into an integer, this
	 * method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public String getStringProperty(String key, String defVal) {
		String val = this.getPropertyData(key, String.class);
		return ((val == null) ? defVal : val);
	}
	
	/**
	 * Retrieve a string property. If the argument key is not mapped to any
	 * value, or if its mapped value fails to parse into an integer, this
	 * method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public Pattern getPatternProperty(String key, Pattern defVal) {
		String val = this.getPropertyData(key, Pattern.class);
		if (val == null)
			return defVal;
		try {
			return Pattern.compile(val);
		}
		catch (PatternSyntaxException pse) {
			pse.printStackTrace(System.out);
			return defVal;
		}
	}
	
	/**
	 * Retrieve an integer property. If the argument key is not mapped to any
	 * value, or if its mapped value fails to parse into an integer, this
	 * method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public int getIntProperty(String key, int defVal) {
		String valStr = this.getPropertyData(key, Integer.class);
		if ((valStr != null) && (valStr.trim().length() != 0)) try {
			return Integer.parseInt(valStr);
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve a float (floating point) property. If the argument key is not
	 * mapped to any value, or if its mapped value fails to parse into a float,
	 * this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public float getFloatProperty(String key, float defVal) {
		String valStr = this.getPropertyData(key, Float.class);
		if ((valStr != null) && (valStr.trim().length() != 0)) try {
			return Float.parseFloat(valStr);
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve a double (floating point) property. If the argument key is not
	 * mapped to any value, or if its mapped value fails to parse into a double,
	 * this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public double getDoubleProperty(String key, double defVal) {
		String valStr = this.getPropertyData(key, Double.class);
		if ((valStr != null) && (valStr.trim().length() != 0)) try {
			return Double.parseDouble(valStr);
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve an boolean property. If the argument key is not mapped to any
	 * value, or if its mapped value fails to parse into a boolean, this
	 * method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public boolean getBooleanProperty(String key, boolean defVal) {
		String valStr = this.getPropertyData(key, Boolean.class);
		return (((valStr == null) || (valStr.trim().length() == 0)) ? defVal : Boolean.parseBoolean(valStr));
	}
	
	/**
	 * Retrieve a string list property. If the argument key is not mapped to
	 * any value, this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public String[] getStringListProperty(String key, String[] defVal, String sep) {
		String valStr = this.getPropertyData(key, stringListClass);
		if ((valStr == null) || (valStr.trim().length() == 0))
			return defVal;
		else return valStr.split("\\s*" + RegExUtils.escapeForRegEx(sep) + "\\s*");
	}
	
	/**
	 * Retrieve a string list property. If the argument key is not mapped to
	 * any value, this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public Pattern[] getPatternListProperty(String key, Pattern[] defVal, String sep) {
		String valStr = this.getPropertyData(key, patternListClass);
		if ((valStr == null) || (valStr.trim().length() == 0))
			return defVal;
		String[] valStrs = valStr.split("\\s*" + RegExUtils.escapeForRegEx(sep) + "\\s*");
		Pattern[] vals = new Pattern[valStrs.length];
		try {
			for (int v = 0; v < valStrs.length; v++)
				vals[v] = Pattern.compile(valStrs[v]);
			return vals;
		}
		catch (PatternSyntaxException pse) {
			pse.printStackTrace(System.out);
			return defVal;
		}
	}
	
	/**
	 * Retrieve an integer list property. If the argument key is not mapped to
	 * any value, or if its mapped value fails to parse into an integer list,
	 * this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public int[] getIntListProperty(String key, int[] defVal) {
		String valStr = this.getPropertyData(key, intListClass);
		if ((valStr == null) || (valStr.trim().length() == 0))
			return defVal;
		String[] valStrs = valStr.split("[^0-9]+");
		int[] vals = new int[valStrs.length];
		try {
			for (int v = 0; v < valStrs.length; v++)
				vals[v] = Integer.parseInt(valStrs[v]);
			return vals;
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve a float (floating point) list property. If the argument key is
	 * not mapped to any value, or if its mapped value fails to parse into a
	 * float list, this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public float[] getFloatListProperty(String key, float[] defVal) {
		String valStr = this.getPropertyData(key, floatListClass);
		if ((valStr == null) || (valStr.trim().length() == 0))
			return defVal;
		String[] valStrs = valStr.split("[^0-9\\,\\.]+");
		float[] vals = new float[valStrs.length];
		try {
			for (int v = 0; v < valStrs.length; v++)
				vals[v] = Float.parseFloat(valStrs[v]);
			return vals;
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve a double (floating point) list property. If the argument key is
	 * not mapped to any value, or if its mapped value fails to parse into a
	 * double list, this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public double[] getDoubleListProperty(String key, double[] defVal) {
		String valStr = this.getPropertyData(key, doubleListClass);
		if ((valStr == null) || (valStr.trim().length() == 0))
			return defVal;
		String[] valStrs = valStr.split("[^0-9\\,\\.]+");
		double[] vals = new double[valStrs.length];
		try {
			for (int v = 0; v < valStrs.length; v++)
				vals[v] = Double.parseDouble(valStrs[v]);
			return vals;
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve an boolean list property. If the argument key is not mapped to
	 * any value, or if its mapped value fails to parse into a boolean list,
	 * this method returns the argument default.
	 * @param key the hashtable key
	 * @param defVal a default value
	 * @return the value in this property list with the specified key value.
	 */
	public boolean[] getBooleanListProperty(String key, boolean[] defVal) {
		String valStr = this.getPropertyData(key, booleanListClass);
		if ((valStr == null) || (valStr.trim().length() == 0))
			return defVal;
		String[] valStrs = valStr.split("[^a-zA-Z]+");
		boolean[] vals = new boolean[valStrs.length];
		try {
			for (int v = 0; v < valStrs.length; v++)
				vals[v] = Boolean.parseBoolean(valStrs[v]);
			return vals;
		} catch (NumberFormatException nfe) {}
		return defVal;
	}
	
	/**
	 * Retrieve the names of all properties present in the document style
	 * parameter list.
	 * @return an array holding the property names
	 */
	public String[] getPropertyNames() {
		if (this.base == null)
			return this.data.getPropertyNames();
		String[] bpns = this.base.getPropertyNames();
		ArrayList pns = new ArrayList();
		for (int n = 0; n < bpns.length; n++) {
			if (bpns[n].startsWith(this.prefix))
				pns.add(bpns[n].substring(this.prefix.length()));
		}
		return ((String[]) pns.toArray(new String[pns.size()]));
	}
	
	/**
	 * Retrieve the names of all properties subsets present in the document
	 * style parameter list, i.e., a list of the prefixes for which a non-empty
	 * subset is available.
	 * @return an array holding the property names
	 */
	public String[] getSubsetPrefixes() {
		String[] pns = this.getPropertyNames();
		LinkedHashSet ssps = new LinkedHashSet();
		for (int n = 0; n < pns.length; n++) {
			if (pns[n].indexOf(".") != -1)
				ssps.add(pns[n].substring(0, pns[n].indexOf(".")));
		}
		return ((String[]) ssps.toArray(new String[ssps.size()]));
	}
	
	/**
	 * Create a string representation of the document style. This string is
	 * composed from the name, ID, and version attributes, whichever are
	 * present.
	 * @return the string representation
	 */
	public String toString() {
		String name = ((String) this.getAttribute(DOCUMENT_STYLE_NAME_ATTRIBUTE));
		String id = ((String) this.getAttribute(DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (id == null)
			return ("DocumentStyle:" + name);
		String version = ((String) this.getAttribute(DOCUMENT_STYLE_VERSION_ATTRIBUTE));
		return ("DocumentStyle:" + id + ((version == null) ? "" : ("." + version)) + ((name == null) ? "" : (":" + name)));
	}
	
	private static final Class stringListClass;
	private static final Class patternListClass;
	private static final Class intListClass;
	private static final Class floatListClass;
	private static final Class doubleListClass;
	private static final Class booleanListClass;
	static {
		try {
			stringListClass = Class.forName("[L" + String.class.getName() + ";");
			mapListElementClass(stringListClass, String.class);
			patternListClass = Class.forName("[L" + Pattern.class.getName() + ";");
			mapListElementClass(patternListClass, Pattern.class);
			intListClass = Class.forName("[I");
			mapListElementClass(intListClass, Integer.class);
			floatListClass = Class.forName("[F");
			mapListElementClass(floatListClass, Float.class);
			doubleListClass = Class.forName("[D");
			mapListElementClass(doubleListClass, Double.class);
			booleanListClass = Class.forName("[Z");
			mapListElementClass(booleanListClass, Boolean.class);
		}
		catch (ClassNotFoundException cnfe) {
			throw new RuntimeException(cnfe);
		}
		
/* TODO Consider introducing further top level citizens:
 * - JSON Object
 * - XHTML
 * - LinePattern (in ImDocumentStyle)
 * - AnnotationPattern
 * - GPath
 * - HEX
 * - Base64
 */
	}
	
	static {
		AttributeAnchor.installFactory();
		GPathAnchor.installFactory();
	}
	
	/**
	 * Output the data contained in the document style parameter list to some
	 * <code>Writer</code> as a tab separated list (keys in first column,
	 * values in second). If the argument <code>Writer</code>  is not a
	 * <code>BufferedWriter</code>, it will be wrapped in one, and flushed
	 * after all data is written.
	 * @param out the writer to write to
	 * @throws IOException
	 */
	public void writeData(Writer out) throws IOException {
		writeData(this.data, out);
	}
	
	/**
	 * Output the data contained in a document style data object to some
	 * <code>Writer</code> as a tab separated list (keys in first column,
	 * values in second). If the argument <code>Writer</code>  is not a
	 * <code>BufferedWriter</code>, it will be wrapped in one, and flushed
	 * after all data is written.
	 * @param data the document style data object to write
	 * @param out the writer to write to
	 * @throws IOException
	 */
	public static void writeData(Data data, Writer out) throws IOException {
		BufferedWriter bw = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
		
		//	write attributes
		String[] ans = data.getAttributeNames();
		for (int a = 0; a < ans.length; a++) {
			Object av = data.getAttribute(ans[a]);
			if (av == null)
				continue;
			bw.write("@" + ans[a]);
			bw.write("\t");
			bw.write(av.toString());
			bw.newLine();
		}
		
		/* TODO Facilitate multi-line property values:
		 * - indicate by mere "+" property name
		 *   ==> add to previous value if encountered
		 *     ==> need to keep previous property name and value in reading methods
		 * - split property values at line breaks on output ...
		 * - ... and output "+" line for all but first line
		 */
		
		//	write data
		String[] pns = data.getPropertyNames();
		for (int p = 0; p < pns.length; p++) {
			String pv = data.getPropertyData(pns[p]);
			if (pv == null)
				continue;
			bw.write(pns[p]);
			bw.write("\t");
			bw.write(pv);
			bw.newLine();
		}
		
		//	finally ...
		if (bw != out)
			bw.flush();
	}
	
	/**
	 * Deserialize a document style parameter list from a <code>Reader</code>.
	 * This method reads the character stream from the argument
	 * <code>Reader</code> until the end. The returned
	 * <code>DocumentStyle</code> is backed by a map.
	 * @param in the reader to read from
	 * @return the deserialized document style data object
	 * @throws IOException
	 */
	public static DocumentStyle readDocumentStyle(Reader in) throws IOException {
		return new DocumentStyle(readDocumentStyleData(in));
	}
	
	/**
	 * Deserialize a document style parameter list from a <code>Reader</code>.
	 * This method reads the character stream from the argument
	 * <code>Reader</code> until the end. The returned
	 * <code>DocumentStyle</code> is backed by a map. Indicating blank lines as
	 * a terminator helps in circumstances where a stream might have further
	 * content following after the document style data.
	 * @param in the reader to read from
	 * @param blankLineIsTerminator treat blank line as terminator?
	 * @return the deserialized document style data object
	 * @throws IOException
	 */
	public static DocumentStyle readDocumentStyle(Reader in, boolean blankLineIsTerminator) throws IOException {
		return new DocumentStyle(readDocumentStyleData(in, blankLineIsTerminator));
	}
	
	/**
	 * Deserialize the data for a document style parameter list from a
	 * <code>Reader</code>. This method reads the character stream from the
	 * argument <code>Reader</code> until the end. The returned
	 * <code>Data</code> is backed by a <code>LinkedHashMap</code>.
	 * @param in the reader to read from
	 * @return the deserialized document style data object
	 * @throws IOException
	 */
	public static Data readDocumentStyleData(Reader in) throws IOException {
		return readDocumentStyleData(in, false);
	}
	
	/**
	 * Deserialize the data for a document style parameter list from a
	 * <code>Reader</code>. This method reads the character stream from the
	 * argument <code>Reader</code> until the end. The returned
	 * <code>Data</code> is backed by a <code>Properties</code>. Indicating
	 * blank lines as a terminator helps in circumstances where a stream might
	 * have further content following after the document style data.
	 * @param in the reader to read from
	 * @param blankLineIsTerminator ignore blank lines, or end at them?
	 * @return the deserialized document style data object
	 * @throws IOException
	 */
	public static Data readDocumentStyleData(Reader in, boolean blankLineIsTerminator) throws IOException {
		BufferedReader br = ((in instanceof BufferedReader) ? ((BufferedReader) in) : new BufferedReader(in));
		
		//	create document style
		Properties dsCnt = new Properties();
		PropertiesData dsData = new PropertiesData(dsCnt);
		
		/* TODO Facilitate multi-line property values:
		 * - indicate by mere "+" property name
		 *   ==> add to previous value if encountered
		 *     ==> need to keep previous property name and value in reading methods
		 * - split property values at line breaks on output ...
		 * - ... and output "+" line for all but first line
		 */
		
		//	read attributes and properties
		for (String dsl; (dsl = br.readLine()) != null;) {
			dsl = dsl.trim();
			if (dsl.length() == 0) {
				if (blankLineIsTerminator)
					break;
				else continue;
			}
			if (dsl.startsWith("//"))
				continue;
			int nvs = dsl.indexOf("\t");
			if (nvs == -1)
				continue;
			String dn = dsl.substring(0, nvs);
			String dv = dsl.substring(nvs + "\t".length());
			if (dn.startsWith("@"))
				dsData.setAttribute(dn.substring("@".length()), dv);
			else dsCnt.setProperty(dn, dv);
		}
		
		//	finally ...
		return dsData;
	}
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//		String[] strs = {"", ""};
//		System.out.println(strs.getClass().getName());
//		int[] ints = {1, 2};
//		System.out.println(ints.getClass().getName());
//		float[] floats = {0.1f, 0.2f};
//		System.out.println(floats.getClass().getName());
//		double[] doubles = {0.1, 0.2};
//		System.out.println(doubles.getClass().getName());
//		boolean[] booleans = {true, false};
//		System.out.println(booleans.getClass().getName());
//		
//		Class.forName("[Ljava.lang.String;");
//		Class.forName("[I");
//		Class.forName("[F");
//		Class.forName("[D");
//		Class.forName("[Z");
//	}
}