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
package de.uka.ipd.idaho.gamta.util.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.AttributeUtils;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.Token;
import de.uka.ipd.idaho.gamta.TokenSequence;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * An editor widget for manipulating the attributes of an Attributed object in
 * a GUI.
 * <br>This class also provides a centralized registration point for components
 * that offer lists of suggested values for specific attributes of given
 * attributed objects.
 * 
 * @author sautter
 */
public class AttributeEditor extends JPanel {
	
	/**
	 * A provider of attribute and value suggestions for given attributed
	 * objects.
	 * 
	 * @author sautter
	 */
	public static interface AttributeValueProvider {
		
		/**
		 * Retrieve a list of suggested attributes for some attributed object.
		 * Implementations are suggested to determine the type of the argument
		 * attributed object in some way and then loop through to the version
		 * of this method that takes a string as an argument.
		 * @param attributed the attributed object the suggestions are for
		 * @return an array containing the suggestions, or null, if there are
		 *            no suggestions available from this provider
		 */
		public abstract String[] getAttributesFor(Attributed attributed);
		
		/**
		 * Get suggested attribute names for a given object type.
		 * @param type the object type to get the attribute names for
		 * @return an array holding the suggested attribute names
		 */
		public abstract String[] getAttributesFor(String type);
		
		/**
		 * Retrieve a list of value suggestions for a given attribute of some
		 * attributed object. Implementations are suggested to determine the
		 * type of the argument attributed object in some way and then loop
		 * through to the version of this method that takes two strings as
		 * arguments.
		 * @param attributed the attributed object the suggestions are for
		 * @param attributeName the name of the attribute to get the
		 *            suggestions for
		 * @return a list containing the suggestions, or null, if there is no
		 *            such list available from this provider
		 */
		public abstract AttributeValueList getValuesFor(Attributed attributed, String attributeName);
		
		/**
		 * Get suggested values for a given attribute of a given object type.
		 * @param type the type of object the attribute belongs to
		 * @param attributeName the attribute name to get the values for
		 * @return a list holding the suggested attribute values
		 */
		public abstract AttributeValueList getValuesFor(String type, String attributeName);
	}
	
	/**
	 * A list of suggested values for a given attribute. Such a list may also
	 * restrict the permitted values to the list content, effectively turning
	 * the available values into a controlled vocabulary. In that case, the
	 * list will also be used in its existing order, not sorted in ascending
	 * lexicographical order.
	 * 
	 * @author sautter
	 */
	public static class AttributeValueList extends ArrayList {
		
		/** does this list represent a controlled set of <em>permitted</em> values? This property should be used with care. */
		public final boolean isControlled;
		
		/** Constructor
		 * @param isControlled does the list represent a controlled set of values?
		 */
		public AttributeValueList(boolean isControlled) {
			this.isControlled = isControlled;
		}
		
		/** Constructor
		 * @param values a collection holding values to add
		 * @param isControlled does the list represent a controlled set of values?
		 */
		public AttributeValueList(Collection values, boolean isControlled) {
			super(values);
			this.isControlled = isControlled;
		}
		
		/**
		 * Convert the list of values into an array of strings for display
		 * purposes.
		 * @return the list content as a string array
		 */
		public String[] toStringArray() {
			return ((String[]) this.toArray(new String[this.size()]));
		}
	}
	
	private static ArrayList attributeValueProviders = new ArrayList();
	
	/**
	 * Register an attribute value provider so it is available to all future
	 * instances of this class.
	 * @param avp the attribute value provider to add
	 */
	public static void addAttributeValueProvider(AttributeValueProvider avp) {
		if (avp != null)
			attributeValueProviders.add(avp);
	}
	
	/**
	 * Remove an attribute value provider.
	 * @param avp the attribute value provider to remove
	 */
	public static void removeAttributeValueProvider(AttributeValueProvider avp) {
		attributeValueProviders.remove(avp);
	}
	
	/**
	 * Retrieve the attribute value providers currently registered.
	 * @return an array holding the attribute value providers
	 */
	public static AttributeValueProvider[] getAttributeValueProviders() {
		return ((AttributeValueProvider[]) attributeValueProviders.toArray(new AttributeValueProvider[attributeValueProviders.size()]));
	}
	
	private static final String DUMMY_ATTRIBUTE_NAME = "Attribute Name";
	private static final String DUMMY_ATTRIBUTE_VALUE = "Attribute Value";
	
	private AttributeTablePanel attributeTable = new AttributeTablePanel();
	
	private JComboBox attributeNameField = new JComboBox();
	private JComboBox attributeValueField = new JComboBox();
	
	private TreeMap contextAttributeValueSetsByName = new TreeMap();
	
	private Attributed attributed; // editing subject
	private TreeSet attributeNames = new TreeSet(); // names of attributes set on editing subject (what shows in value table)
	private TreeMap attributeValues = new TreeMap(); // all attribute values (from editing subject, context, and suggestions ... what shows in drop-downs)
	
	private boolean nameFieldKeyPressed = false;
	private boolean valueFieldKeyPressed = false;
	
	/**
	 * Constructor
	 * @param annotation the Annotation whose attributes to edit
	 */
	public AttributeEditor(Annotation annotation) {
		this(annotation, annotation.getDocument());
	}
	
	/**
	 * Constructor
	 * @param annotation the Annotation whose attributes to edit
	 * @param context the context annotation, e.g. the document the annotation
	 *            refers to (will be used to provide attribute name and value
	 *            suggestions)
	 */
	public AttributeEditor(Annotation annotation, QueriableAnnotation context) {
		this(annotation, annotation.getType(), annotation.getValue(), ((context == null) ? new Annotation[0] : context.getAnnotations(annotation.getType())));
	}
	
	/**
	 * Constructor
	 * @param annotations an array holding the Annotations whose attributes to
	 *            edit
	 * @param context the context annotation, e.g. the document the annotation
	 *            refers to (will be used to provide attribute name and value
	 *            suggestions)
	 */
	public AttributeEditor(Annotation[] annotations, QueriableAnnotation context) {
		this(new MultiAttributedWrapper(annotations), context);
	}
	private AttributeEditor(MultiAttributedWrapper annotationWraper, QueriableAnnotation context) /* need this ugly intermediate step to access contents of wrapper */ {
		this(annotationWraper, null, null, ((context == null) ? new Annotation[0] : context.getAnnotations(annotationWraper.getType())));
		//	TODO maybe use annotations of ALL present types as context ...
	}
	
	/**
	 * Constructor
	 * @param annotations an array holding the Annotations whose attributes to
	 *            edit
	 * @param context the context annotation, e.g. the document the annotation
	 *            refers to (will be used to provide attribute name and value
	 *            suggestions)
	 */
	public AttributeEditor(Annotation[] annotations, Attributed[] context) {
		this(new MultiAttributedWrapper(annotations), null, null, context);
	}
	
	/**
	 * Constructor
	 * @param token the Token whose attributes to edit
	 * @param context the context token sequence, e.g. the document the token
	 *            belongs to (will be used to provide attribute name and value
	 *            suggestions)
	 */
	public AttributeEditor(Token token, TokenSequence context) {
		this(token, Token.TOKEN_ANNOTATION_TYPE, token.getValue(), getAttributedTokens(context));
	}
	
	/**
	 * Constructor
	 * @param token the Token whose attributes to edit
	 * @param context the context token sequence, e.g. the document the token
	 *            belongs to (will be used to provide attribute name and value
	 *            suggestions)
	 */
	public AttributeEditor(Token[] tokens, TokenSequence context) {
		this(new MultiAttributedWrapper(tokens), null, null, getAttributedTokens(context));
	}
	
	private static Attributed[] getAttributedTokens(TokenSequence tokens) {
		ArrayList attributedTokens = new ArrayList();
		for (int t = 0; t < tokens.size(); t++) {
			Token token = tokens.tokenAt(t);
			if (token.getAttributeNames().length != 0)
				attributedTokens.add(token);
		}
		return ((Attributed[]) attributedTokens.toArray(new Attributed[attributedTokens.size()]));
	}
	
	/**
	 * Constructor
	 * @param attributeds an array holding the attribute bearing objects whose
	 *            attributes to edit
	 * @param type the type of the attributed object (may be null)
	 * @param value the string value of the attributed object (may be null)
	 * @param context the context, e.g. attributed objects similar to the one
	 *            whose attributes to edit (will be used to provide attribute
	 *            name and value suggestions, may be null)
	 */
	public AttributeEditor(Attributed[] attributeds, String type, String value, Attributed[] context) {
		this(new MultiAttributedWrapper(attributeds, type, value), null, null, context);
	}
	
	/**
	 * Constructor
	 * @param attributed the attribute bearing object whose attributes to edit
	 * @param type the type of the attributed object (may be null)
	 * @param value the string value of the attributed object (may be null)
	 * @param context the context, e.g. attributed objects similar to the one
	 *            whose attributes to edit (will be used to provide attribute
	 *            name and value suggestions, may be null)
	 */
	public AttributeEditor(Attributed attributed, String type, String value, Attributed[] context) {
		super(new BorderLayout(), true);
		this.attributed = attributed;
		
		//	store attributes of object being edited
		String[] attributeNames = this.attributed.getAttributeNames();
		Arrays.sort(attributeNames, String.CASE_INSENSITIVE_ORDER);
		for (int n = 0; n < attributeNames.length; n++) {
			this.attributeNames.add(attributeNames[n]);
			Object valueObject = this.attributed.getAttribute(attributeNames[n]);
			SelectableAttributeValue selectedValue;
			AttributeValueData valueData;
			if (valueObject instanceof MultiAttributedWrapper.MultiAttributeValueSet) {
				MultiAttributedWrapper.MultiAttributeValueSet multiValueObject = ((MultiAttributedWrapper.MultiAttributeValueSet) valueObject);
				selectedValue = new SelectableAttributeValue(attributeNames[n], multiValueObject, multiValueObject.values.size());
				selectedValue.inContentCount += multiValueObject.values.size();
				valueData = new AttributeValueData(attributeNames[n], selectedValue);
				if (multiValueObject.isAmbiguous()) {
					Object[] selectableValueObjects = multiValueObject.getValues();
					for (int v = 0; v < selectableValueObjects.length; v++) {
						SelectableAttributeValue selectableValue = new SelectableAttributeValue(attributeNames[n], selectableValueObjects[v]);
						selectableValue.inContentCount += multiValueObject.getValueCount(selectableValueObjects[v]);
						selectableValue.parentMultiValueSize += multiValueObject.values.size();
						valueData.addSelectableValue(selectableValue);
					}
				}
			}
			else {
				selectedValue = new SelectableAttributeValue(attributeNames[n], valueObject);
				selectedValue.inContentCount++;
				valueData = new AttributeValueData(attributeNames[n], selectedValue);
			}
			this.attributeValues.put(attributeNames[n], valueData);
		}
		
		//	get and index attributes and values of all context objects (for offering suggestions)
		for (int c = 0; (context != null) && (c < context.length); c++) {
			if (attributed == context[c])
				continue;
			if ((attributed instanceof MultiAttributedWrapper) && ((MultiAttributedWrapper) attributed).wraps(context[c]))
				continue;
			attributeNames = context[c].getAttributeNames();
			for (int n = 0; n < attributeNames.length; n++) {
				Object valueObject = context[c].getAttribute(attributeNames[n]);
				if (valueObject == null)
					continue;
				SelectableAttributeValue selectableValue = new SelectableAttributeValue(attributeNames[n], valueObject);
				selectableValue.inContextCount++;
				AttributeValueData valueData = ((AttributeValueData) this.attributeValues.get(attributeNames[n]));
				if (valueData == null) {
					valueData = new AttributeValueData(attributeNames[n], selectableValue);
					this.attributeValues.put(attributeNames[n], valueData);
				}
				else valueData.addSelectableValue(selectableValue);
			}
		}
		
		//	add attribute name suggestions from providers
		for (int p = 0; p < attributeValueProviders.size(); p++) {
			attributeNames = ((AttributeValueProvider) attributeValueProviders.get(p)).getAttributesFor(this.attributed);
			if (attributeNames == null)
				continue;
			for (int n = 0; n < attributeNames.length; n++) {
				AttributeValueData valueData = ((AttributeValueData) this.attributeValues.get(attributeNames[n]));
				if (valueData == null) {
					valueData = new AttributeValueData(attributeNames[n], null);
					this.attributeValues.put(attributeNames[n], valueData);
				}
//				valueData.fromProviderCount++;
			}
		}
		
		//	add attribute value suggestions from providers
		for (Iterator anit = this.contextAttributeValueSetsByName.keySet().iterator(); anit.hasNext();) {
			String attributeName = ((String) anit.next());
			AttributeValueData attributeValueData = ((AttributeValueData) this.attributeValues.get(attributeName));
			if (attributeValueData == null) {
				attributeValueData = new AttributeValueData(attributeName, null);
				this.attributeValues.put(attributeName, attributeValueData);
			}
			for (int p = 0; p < attributeValueProviders.size(); p++) {
				AttributeValueList attributeValueList = ((AttributeValueProvider) attributeValueProviders.get(p)).getValuesFor(this.attributed, attributeName);
				if (attributeValueList == null)
					continue;
				String[] attributeValues = attributeValueList.toStringArray();
				for (int v = 0; v < attributeValues.length; v++) {
					SelectableAttributeValue selectableValue = new SelectableAttributeValue(attributeName, attributeValues[v]);
					selectableValue.fromProviderCount++;
					attributeValueData.addSelectableValue(selectableValue);
				}
				if (attributeValueList.isControlled) {
					//	TODO update restriction status
					//	TODO update restriction compliance of selectable values
				}
			}
		}
		
		//	initialize attribute editor fields
		this.attributeNameField.setBorder(BorderFactory.createLoweredBevelBorder());
		this.attributeNameField.setEditable(true);
		this.resetAttributeNameField();
		
		this.attributeNameField.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent fe) {
				if (DUMMY_ATTRIBUTE_NAME.equals(attributeNameField.getSelectedItem()))
					attributeNameField.setSelectedItem("");
			}
		});
		((JTextComponent) this.attributeNameField.getEditor().getEditorComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				nameFieldKeyPressed = true;
			}
			public void keyReleased(KeyEvent ke) {
				nameFieldKeyPressed = false;
			}
		});
		this.attributeNameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (nameFieldKeyPressed && isVisible() && !attributeNameField.isPopupVisible())
					attributeValueField.requestFocusInWindow();
			}
		});
		this.attributeNameField.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				resetAttributeValueField();
			}
		});
		
		
		this.attributeValueField.setRenderer(new AttributeValueListCellRenderer(this.attributeValueField));
		this.attributeValueField.setBorder(BorderFactory.createLoweredBevelBorder());
		this.attributeValueField.setEditable(true);
		
		this.resetAttributeValueField();
		
		this.attributeValueField.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent fe) {
				if (DUMMY_ATTRIBUTE_VALUE.equals(attributeValueField.getSelectedItem()))
					attributeValueField.setSelectedItem("");
			}
		});
		((JTextComponent) this.attributeValueField.getEditor().getEditorComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				valueFieldKeyPressed = true;
			}
			public void keyReleased(KeyEvent ke) {
				valueFieldKeyPressed = false;
			}
		});
		this.attributeValueField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (valueFieldKeyPressed && isVisible() && !attributeValueField.isPopupVisible())
					setAttribute();
			}
		});
		
		//	initialize buttons
		final JButton setAttributeButton = new JButton(" Add / Set Attribute ");
		setAttributeButton.setBorder(BorderFactory.createRaisedBevelBorder());
		setAttributeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setAttribute();
			}
		});
		
		final JPanel attributeInputPanel = new JPanel(new BorderLayout(), true);
		attributeInputPanel.add(this.attributeNameField, BorderLayout.CENTER);
		attributeInputPanel.add(setAttributeButton, BorderLayout.EAST);
		attributeInputPanel.add(this.attributeValueField, BorderLayout.SOUTH);
		attributeInputPanel.setFocusTraversalPolicyProvider(true);
		attributeInputPanel.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
			public Component getComponentAfter(Container cont, Component comp) {
				if (this.isOrIsChildOf(comp, attributeNameField))
					return attributeValueField;
				else if (this.isOrIsChildOf(comp, attributeValueField))
					return setAttributeButton;
				else if (this.isOrIsChildOf(comp, setAttributeButton)) {
					Component aComp = super.getComponentAfter(cont, comp);
					if (this.isOrIsChildOf(aComp, attributeNameField))
						return super.getComponentAfter(cont, aComp);
					else if (this.isOrIsChildOf(aComp, attributeValueField))
						return super.getComponentAfter(cont, aComp);
					else return aComp;
				}
				else return super.getComponentAfter(cont, comp);
			}
			public Component getComponentBefore(Container cont, Component comp) {
				if (this.isOrIsChildOf(comp, attributeNameField)) {
					Component bComp = super.getComponentBefore(cont, comp);
					if (this.isOrIsChildOf(bComp, attributeValueField))
						return super.getComponentBefore(cont, bComp);
					else if (this.isOrIsChildOf(bComp, setAttributeButton))
						return super.getComponentBefore(cont, bComp);
					else return bComp;
				}
				else if (this.isOrIsChildOf(comp, attributeValueField))
					return attributeNameField;
				else if (this.isOrIsChildOf(comp, setAttributeButton))
					return attributeValueField;
				else return super.getComponentBefore(cont, comp);
			}
			private boolean isOrIsChildOf(Component cComp, Component pComp) {
				while (cComp != null) {
					if (cComp == pComp)
						return true;
					cComp = cComp.getParent();
				}
				return false;
			}
		});
		
		//	display content data (for giving users the context)
		JLabel contentTypeField;
		if (attributed instanceof MultiAttributedWrapper) {
			type = ((MultiAttributedWrapper) attributed).getType();
			int typeCount = ((MultiAttributedWrapper) attributed).getTypeCount();
			if (type == null)
				contentTypeField = new JLabel("generic");
			else if (typeCount == 1)
				contentTypeField = new JLabel(type);
			else {
				contentTypeField = new JLabel(type + " (" + (typeCount - 1) + " others)");
				String[] types = ((MultiAttributedWrapper) attributed).getTypes();
				Arrays.sort(types);
				StringBuffer typeTooltip = new StringBuffer("<HTML><UL>");
				for (int t = 0; t < types.length; t++)
					typeTooltip.append("<LI>[" + ((MultiAttributedWrapper) attributed).getTypeCount(types[t]) + "] " + AnnotationUtils.escapeForXml(types[t]) + "</LI>");
				typeTooltip.append("</UL></HTML>");
				contentTypeField.setToolTipText(typeTooltip.toString());
			}
		}
		else contentTypeField = new JLabel((type == null) ? "generic" : type);
		contentTypeField.setFont(contentTypeField.getFont().deriveFont(Font.BOLD));
		contentTypeField.setBorder(BorderFactory.createLineBorder(contentTypeField.getBackground(), 5));
		
		//	display annotation value
		JLabel contentValueField;
		if (attributed instanceof MultiAttributedWrapper) {
			value = ((MultiAttributedWrapper) attributed).getValue();
			int valueCount = ((MultiAttributedWrapper) attributed).getValueCount();
			if (valueCount == 0)
				contentValueField = new JLabel("");
			else if (valueCount == 1)
				contentValueField = new JLabel(value);
			else {
				contentValueField = new JLabel(value + " (" + (valueCount - 1) + " others)");
				String[] values = ((MultiAttributedWrapper) attributed).getValues();
				Arrays.sort(values);
				StringBuffer valueTooltip = new StringBuffer("<HTML><UL>");
				for (int v = 0; v < values.length; v++)
					valueTooltip.append("<LI>[" + ((MultiAttributedWrapper) attributed).getValueCount(values[v]) + "] " + AnnotationUtils.escapeForXml(values[v]) + "</LI>");
				valueTooltip.append("</UL></HTML>");
				contentValueField.setToolTipText(valueTooltip.toString());
			}
		}
		else {
			contentValueField = new JLabel((value == null) ? "" : value);
			if (attributed instanceof TokenSequence)
				contentValueField.setToolTipText(this.produceTooltipText((TokenSequence) attributed));
		}
		contentValueField.setBorder(BorderFactory.createLineBorder(contentValueField.getBackground(), 5));
		
		JLabel contentCountField;
		if (attributed instanceof MultiAttributedWrapper) {
			contentCountField = new JLabel("[" + ((MultiAttributedWrapper) attributed).getContentCount() + "]");
			contentCountField.setBorder(BorderFactory.createLineBorder(contentCountField.getBackground(), 5));
			contentCountField.setFont(contentTypeField.getFont().deriveFont(Font.BOLD));
		}
		else contentCountField = null;
		
		JPanel contentDataPanel = new JPanel(new BorderLayout());
		contentDataPanel.add(contentTypeField, BorderLayout.WEST);
		contentDataPanel.add(contentValueField, BorderLayout.CENTER);
		if (contentCountField != null)
			contentDataPanel.add(contentCountField, BorderLayout.EAST);
		
		//	wrap and initialize attribute table
		JScrollPane attributeTableBox = new JScrollPane(this.attributeTable);
		attributeTableBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		attributeTableBox.getVerticalScrollBar().setUnitIncrement(50);
		attributeTableBox.getVerticalScrollBar().setBlockIncrement(50);
		attributeTableBox.setViewportBorder(BorderFactory.createLoweredBevelBorder());
		this.attributeTable.updateAttributes();
		
		//	put the whole stuff together
		this.setLayout(new BorderLayout());
		this.add(contentDataPanel, BorderLayout.NORTH);
		this.add(attributeTableBox, BorderLayout.CENTER);
		this.add(attributeInputPanel, BorderLayout.SOUTH);
	}
	
	private class AttributeValueListCellRenderer implements ListCellRenderer {
		private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private JComboBox target;
		AttributeValueListCellRenderer(JComboBox target) {
			this.target = target;
		}
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if ((value instanceof SelectableAttributeValue))
				return this.getListCellRendererComponent(list, ((SelectableAttributeValue) value), index, isSelected, cellHasFocus);
			else return this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
		private Component getListCellRendererComponent(JList list, SelectableAttributeValue value, int index, boolean isSelected, boolean cellHasFocus) {
//			Component lcrc = this.defaultRenderer.getListCellRendererComponent(list, value.value, index, isSelected, cellHasFocus);
			String valueStr;
			if (value.parentMultiValueSize == 0)
				valueStr = ((value.value == null) ? "" : value.value.toString());
			else if (value.value instanceof MultiAttributedWrapper.MultiAttributeValueSet)
				valueStr = ("[" + value.parentMultiValueSize + "] " + ((value.value == null) ? "" : value.value.toString()));
			else valueStr = ("[" + value.inContentCount + "/" + value.parentMultiValueSize + "] " + ((value.value == null) ? "" : value.value.toString()));
			Component lcrc = this.defaultRenderer.getListCellRendererComponent(list, valueStr, index, isSelected, cellHasFocus);
			if (!this.target.isPopupVisible())
				return lcrc;
			int fontStyle = Font.PLAIN;
			if (value.inContentCount != 0)
				fontStyle |= Font.BOLD;
			if (value.fromProviderCount != 0)
				fontStyle |= Font.ITALIC;
			if (fontStyle != Font.PLAIN)
				lcrc.setFont(lcrc.getFont().deriveFont(fontStyle));
			return lcrc;
		}
	}
	
	private static class AttributeValueData {
		final String name; // TODO not sure if we need this ... maybe for "is restricted" lookups
		SelectableAttributeValue selectedValue;
		TreeMap selectableValues = new TreeMap(toStringOrder);
//		int inContentCount = 0;
//		int inContextCount = 0;
//		int fromProviderCount = 0;
		AttributeValueData(String name, SelectableAttributeValue selectedValue) {
			this.name = name;
			if (selectedValue != null)
				this.selectedValue = this.addSelectableValue(selectedValue);
		}
		SelectableAttributeValue addSelectableValue(SelectableAttributeValue value) {
			SelectableAttributeValue exValue = ((SelectableAttributeValue) this.selectableValues.get(value.value));
			if (exValue == value)
				return value; // selected in drop-down
			if (exValue == null) {
				this.selectableValues.put(value.value, value);
				return value;
			}
			exValue.inContentCount += value.inContentCount;
			exValue.inContextCount += value.inContextCount;
			exValue.fromProviderCount += value.fromProviderCount;
			exValue.fromUserInput |= value.fromUserInput;
			exValue.parentMultiValueSize += value.parentMultiValueSize;
			return exValue;
		}
		void removeSelectableValue(SelectableAttributeValue value) {
			if ((value != null) && (value.value != null))
				this.selectableValues.remove(value.value);
		}
		SelectableAttributeValue[] getSelectableValues() {
			return ((SelectableAttributeValue[]) this.selectableValues.values().toArray(new SelectableAttributeValue[this.selectableValues.size()]));
		}
		static final Comparator toStringOrder = new Comparator() {
			public int compare(Object obj1, Object obj2) {
				if (obj1 == obj2)
					return 0;
				else if (obj1 == null)
					return -1; // pushes empty value to front (if any)
				else if (obj2 == null)
					return 1; // pushes empty value to front (if any)
				else return (obj1.toString().compareTo(obj2.toString()));
			}
		};
	}
	
	private static class SelectableAttributeValue {
		final String name; // TODO not sure if we need this ... maybe for "is restricted" lookups
		final Object value;
		int inContentCount = 0;
		int inContextCount = 0;
		int fromProviderCount = 0;
		boolean fromUserInput = false;
		int parentMultiValueSize = 0;
		char restrictionCompliance = 'U';
		SelectableAttributeValue(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		SelectableAttributeValue(String name, Object value, boolean fromUserInput) {
			this(name, value);
			this.fromUserInput = fromUserInput;
		}
		SelectableAttributeValue(String name, Object value, int parentMultiValueSize) {
			this(name, value);
			this.parentMultiValueSize = parentMultiValueSize;
		}
		boolean isFromUserInputOnly() {
			return ((this.inContentCount == 0) && (this.inContextCount == 0) && (this.fromProviderCount == 0));
		}
		public String toString() {
			return ((this.value == null) ? "" : this.value.toString());
		}
	}
	
	private static class MultiAttributedWrapper implements Attributed {
		Attributed[] contents;
		private CountingSet contentTypes = new CountingSet(new TreeMap());
		private CountingSet contentValues = new CountingSet(new TreeMap());
		private TreeSet contentAttributeNames = new TreeSet();
		private TreeMap attributeValues = new TreeMap();
		MultiAttributedWrapper(Token[] tokens) {
			this.contents = tokens;
			this.contentTypes.add(Token.TOKEN_ANNOTATION_TYPE, this.contents.length);
			for (int t = 0; t < tokens.length; t++)
				this.contentValues.add(tokens[t].getValue());
			this.initializeAttributeValues();
		}
		MultiAttributedWrapper(Annotation[] annotations) {
			this.contents = annotations;
			for (int a = 0; a < annotations.length; a++) {
				this.contentTypes.add(annotations[a].getType());
				this.contentValues.add(annotations[a].getValue());
			}
			this.initializeAttributeValues();
		}
		MultiAttributedWrapper(Attributed[] attributeds, String type, String value) {
			this.contents = attributeds;
			this.contentTypes.add(type, this.contents.length);
			this.contentValues.add(value, this.contents.length);
			this.initializeAttributeValues();
		}
		
		boolean wraps(Attributed attributed) {
			for (int c = 0; c < this.contents.length; c++) {
				if (this.contents[c] == attributed)
					return true;
			}
			return false;
		}
		
		private void initializeAttributeValues() {
			for (int c = 0; c < this.contents.length; c++) {
				String[] attributeNames = this.contents[c].getAttributeNames();
				for (int a = 0; a < attributeNames.length; a++) {
					this.contentAttributeNames.add(attributeNames[a]);
					MultiAttributeValueSet attributeValues = ((MultiAttributeValueSet) this.attributeValues.get(attributeNames[a]));
					if (attributeValues == null) {
						attributeValues = new MultiAttributeValueSet(attributeNames[a]);
						this.attributeValues.put(attributeNames[a], attributeValues);
					}
					attributeValues.addValue(this.contents[c]);
				}
			}
		}
		
		class MultiAttributeValueSet {
			final String name; // mainly for convenience (renderers, restriction lokups, etc.)
			final CountingSet values = new CountingSet(new HashMap());
			MultiAttributeValueSet(String name) {
				this.name = name;
			}
			void addValue(Attributed attributed) {
				Object value = attributed.getAttribute(this.name);
				if (value != null)
					this.values.add(value);
			}
			Object getValue() {
				//	TODO return null if value mostly empty ?!?!?
				return (this.values.isEmpty() ? null : this.values.max());
			}
			Object[] getValues() {
				return this.values.toArray();
			}
			int getDistinctValueCount() {
				return this.values.elementCount();
			}
			int getValueCount(boolean includeEmpty) {
				return (includeEmpty ? contents.length : this.values.size());
			}
			int getValueCount(Object value) {
				return ((value == null) ? (contents.length - this.values.size()) : this.values.getCount(value));
			}
			boolean isAmbiguous() {
				return (this.values.elementCount() > (this.hasEmptyValues() ? 0 : 1));
			}
			boolean hasEmptyValues() {
				return (this.values.size() < contents.length);
			}
			boolean areValuesUnique() {
				return (this.values.size() == this.values.elementCount());
			}
			public String toString() {
				if (this.values.isEmpty())
					return "";
				else if (this.isAmbiguous())
					return "<Leave Unchanged>"; // TODO_not elaborate on actual content ==> hover text of value does that
				else return this.getValue().toString();
			}
		}
		
		int getContentCount() {
			return this.contents.length;
		}
		String getType() {
			return (this.contentTypes.isEmpty() ? null : ((String) this.contentTypes.max()));
		}
		String[] getTypes() {
			String[] values = new String[this.contentTypes.elementCount()];
			int vi = 0;
			for (Iterator vit = this.contentTypes.iterator(); vit.hasNext();)
				values[vi++] = ((String) vit.next());
			return values;
		}
		int getTypeCount() {
			return this.contentTypes.elementCount();
		}
		int getTypeCount(String type) {
			return this.contentTypes.getCount(type);
		}
		String getValue() {
			return (this.contentValues.isEmpty() ? null : ((String) this.contentValues.max()));
		}
		String[] getValues() {
			String[] values = new String[this.contentValues.elementCount()];
			int vi = 0;
			for (Iterator vit = this.contentValues.iterator(); vit.hasNext();)
				values[vi++] = ((String) vit.next());
			return values;
		}
		int getValueCount() {
			return this.contentValues.elementCount();
		}
		int getValueCount(String value) {
			return this.contentValues.getCount(value);
		}
		
		public void setAttribute(String name) {
			this.setAttribute(name, "true"); // should not be used, but let's be safe
		}
		public Object setAttribute(String name, Object value) {
			return ((value == null) ? this.attributeValues.remove(name) : this.attributeValues.put(name, value));
		}
		public Object getAttribute(String name) {
			return this.getAttribute(name, null);
		}
		public Object getAttribute(String name, Object def) {
			Object value = this.attributeValues.get(name);
			return ((value == null) ? def : value);
		}
		public boolean hasAttribute(String name) {
			return this.attributeValues.containsKey(name);
		}
		public String[] getAttributeNames() {
			return ((String[]) this.attributeValues.keySet().toArray(new String[this.attributeValues.size()]));
		}
		public Object removeAttribute(String name) {
			return this.attributeValues.remove(name);
		}
		public void copyAttributes(Attributed source) { /* not copying anything */ }
		public void clearAttributes() { /* not clearing anything */ }
		
		void writeChanges() {
			for (Iterator anit = this.contentAttributeNames.iterator(); anit.hasNext();) {
				String name = ((String) anit.next());
				if (this.attributeValues.containsKey(name))
					continue; // still extant
				for (int c = 0; c < this.contents.length; c++)
					this.contents[c].removeAttribute(name);
			}
			for (Iterator anit = this.attributeValues.keySet().iterator(); anit.hasNext();) {
				String name = ((String) anit.next());
				Object value = this.attributeValues.get(name);
				if (value instanceof MultiAttributeValueSet)
					continue; // value never set (attribute value sets are only generated in here)
				for (int c = 0; c < this.contents.length; c++) {
					if (value == null) // however this happened ...
						this.contents[c].removeAttribute(name);
					else this.contents[c].setAttribute(name, value);
				}
			}
		}
	}
	/* TODO use controlled value lists for specific attributes (especially ones with referential constraints):
- target box and page ID for captions
  ==> maybe gradually switch that pair to local ID of region (basically concatenates the two)
- refId in bibRefCitations
- captionStartId and captionsStartId-X in caption citations
- table region IDs in column and row connections
- image region IDs in image connections
- type in subSections and subSubSections
- includeIn in treatments
- pageNumber and lastPageNumber, pageid and lastPageId on but every object
- blockId and lastBlockId in paragraphs
- fontName and charCodeString in words
- docStyle, docStyleId, docStyleName, and docStyleVersion in documents
- QC approval attributes in document
- provenance attributes in documents
- ...
- think of more
==> register restrictions when initializing plug-ins (akin to style parameter group descriptions)
==> makes referential constraint logic of data model extensible

TODO MAYBE, extend AttributeUtils to take value lists (instead of AttributeEditor)
==> central extension point as supposed to ...
==> ... and then, maybe don't, as it's all about UI editing ...
==> ... component logic proper being merely subject to bugs ...

TODO facilitate specifying UI editability of attributes in AttributeUtils (or maybe in AttributeEditor):
- take object type and attribute name (and maybe current value)
- editability levels:
  - normal: (default, current) behavior
  - controlled: value checker (see below)
  - warning: "really edit this ???" and display in light gray
  - error: "cannot edit this !!!", display in gray, and disable (page IDs in GGI, etc.)
- keep in map as "<type>/@<attributeName>" => level constant or value list

TODO add AttributeValueChecker interface to AttributeUtils (or maybe to AttributeEditor) ...
... alongside static registry ...
... and checkAttributeValue(Attributed object, String attributeName, Object attributeValue) method:
  - modify attribute value (e.g. trim string)
  - throw IllegalArgumentException if value not permitted
==> facilitates extending semantics of attributes from plug-ins
==> e.g checking compatibility of licenses in ZenodoLicenseSelector
	 */
	
	private void resetAttributeNameField() {
		this.attributeNameField.setModel(new DefaultComboBoxModel((String[]) this.attributeValues.keySet().toArray(new String[this.attributeValues.size()])));
		this.attributeNameField.setSelectedItem(DUMMY_ATTRIBUTE_NAME);
	}
	
	private void resetAttributeValueField() {
		Object nameObj = this.attributeNameField.getSelectedItem();
		AttributeValueData valueData = (((nameObj == null) || DUMMY_ATTRIBUTE_NAME.equals(nameObj)) ? null : ((AttributeValueData) this.attributeValues.get(nameObj)));
		if (valueData == null) {
			this.attributeValueField.setModel(new DefaultComboBoxModel(new String[0]));
			this.attributeValueField.setSelectedItem(DUMMY_ATTRIBUTE_VALUE);
		}
		else {
			this.attributeValueField.setModel(new DefaultComboBoxModel(valueData.getSelectableValues()));
			this.attributeValueField.setSelectedItem((valueData.selectedValue == null) ? DUMMY_ATTRIBUTE_VALUE : valueData.selectedValue);
		}
	}
	
	private String produceTooltipText(TokenSequence tokens) {
		if (tokens.length() < 100) return TokenSequenceUtils.concatTokens(tokens);
		
		StringVector lines = new StringVector();
		int startToken = 0;
		int lineLength = 0;
		Token lastToken = null;
		
		for (int t = 0; t < tokens.size(); t++) {
			Token token = tokens.tokenAt(t);
			lineLength += token.length();
			if (lineLength > 100) {
				lines.addElement(TokenSequenceUtils.concatTokens(tokens, startToken, (t - startToken + 1)));
				startToken = (t + 1);
				lineLength = 0;
			}
			else if (Gamta.insertSpace(lastToken, token))
				lineLength++;
		}
		if (startToken < tokens.size())
			lines.addElement(TokenSequenceUtils.concatTokens(tokens, startToken, (tokens.size() - startToken)));
		
		return ("<HTML>" + lines.concatStrings("<BR>") + "</HTML>");
	}
	
	private void setAttribute() {
		
		//	get name
		String name = null;
		Object nameObj = attributeNameField.getSelectedItem();
		if (nameObj != null)
			name = nameObj.toString().trim();
		if ((name == null) || (name.length() == 0) || DUMMY_ATTRIBUTE_NAME.equals(name))
			return;
		if (!AttributeUtils.isValidAttributeName(name)) {
			DialogFactory.alert("Cannot add attribute. The specified name is invalid.", "Invalid Attribute Name", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//	get, check, and wrap value
		Object valueObj = attributeValueField.getSelectedItem();
		if (valueObj == null)
			return;
		SelectableAttributeValue value;
		if (valueObj instanceof SelectableAttributeValue)
			value = ((SelectableAttributeValue) valueObj);
		else {
			String valueStr = valueObj.toString();
			if ((valueStr.length() == 0) || DUMMY_ATTRIBUTE_VALUE.equals(valueStr))
				return;
			value = new SelectableAttributeValue(name, valueStr, true);
		}
		//	TODO maybe show at least warning popup if value outside any restricting lists
			
		//	set attribute
		this.attributeNames.add(name);
		
		//	get or create value data
		AttributeValueData valueData = ((AttributeValueData) this.attributeValues.get(name));
		if (valueData == null) {
			valueData = new AttributeValueData(name, null);
			this.attributeValues.put(name, valueData);
		}
		else if (valueData.selectedValue != null) {
			if (valueData.selectedValue.isFromUserInputOnly())
				valueData.removeSelectableValue(valueData.selectedValue);
			else valueData.selectedValue.fromUserInput = false;
		}
		
		//	store wrapped value
		if (value.fromUserInput)
			valueData.selectedValue = valueData.addSelectableValue(value);
		else valueData.selectedValue = value;
		
		//	refresh attribute table
		this.attributeTable.updateAttributes();
		
		//	refresh input fields
		this.resetAttributeNameField();
	}
	
	private void removeAttribute(String name) {
		
		//	update data
		this.attributeNames.remove(name);
		
		//	update value lists
		AttributeValueData valueData = ((AttributeValueData) this.attributeValues.get(name));
		if ((valueData != null) && (valueData.selectedValue != null)) {
			if (valueData.selectedValue.isFromUserInputOnly())
				valueData.removeSelectableValue(valueData.selectedValue);
			else valueData.selectedValue.fromUserInput = false;
			valueData.selectedValue = null;
		}
		
		//	refresh attribute table
		this.attributeTable.updateAttributes();
		
		//	refresh input fields
		this.resetAttributeNameField();
	}
	
	/**
	 * Write the changes made in the editor through to the attributed object
	 * being edited.
	 * @return true if there were any changes to be written, false otherwise
	 */
	public boolean writeChanges() {
		boolean modified = false;
		
		String[] oldAttributeNames = this.attributed.getAttributeNames();
		for (int a = 0; a < oldAttributeNames.length; a++) {
			if (this.attributeNames.contains(oldAttributeNames[a]))
				continue;
			this.attributed.removeAttribute(oldAttributeNames[a]);
			modified = true;
		}
		
		for (Iterator anit = this.attributeNames.iterator(); anit.hasNext();) {
			String name = ((String) anit.next());
			AttributeValueData valueData = ((AttributeValueData) this.attributeValues.get(name));
			if (valueData == null)
				continue; // should not happen, but let's be safe
			if ((valueData.selectedValue == null) || (valueData.selectedValue.value == null)) {
				if (this.attributed.removeAttribute(name) != null)
					modified = true;
			}
			else {
				Object oldValue = this.attributed.getAttribute(name);
				if (AttributeValueData.toStringOrder.compare(oldValue, valueData.selectedValue.value) != 0) {
					this.attributed.setAttribute(name, valueData.selectedValue.value);
					modified = true;
				}
			}
		}
		
		if (modified && (this.attributed instanceof MultiAttributedWrapper))
			((MultiAttributedWrapper) this.attributed).writeChanges();
		return modified;
	}
	
	/* TODO Add centralized static setters for attribute editor colors:
	 * - ID color (unique, never empty, currently teal)
	 * - unique color (unique, might be empty, currently pink)
	 * - ambiguous color (multiple non-empty values with varying frequency, empty or not, currently yellow)
	 * - single-value color (single non-empty value, but also empty, currently lime)
	 * - all-the-same color (same value everywhere, never empty, currently white)
	 * 
	 * TODO ALSO, add setters for font styles and background colors in value drop-down:
	 * - given (the base case)
	 * - extant (present in editing subjects, currently bold)
	 * - suggested (coming from some suggestion provider)
	 * - outside some restriction (for values with restricted vocabularies)
	 */
	
	private static final Color Color_LIME = new Color(200, 255, 0);
	private class AttributeTablePanel extends JPanel {
		private ArrayList lines = new ArrayList();
		private JPanel spacer = new JPanel();
		
		AttributeTablePanel() {
			super(new GridBagLayout(), true);
		}
		
		void updateAttributes() {
			this.lines.clear();
			for (Iterator anit = attributeNames.iterator(); anit.hasNext();) {
				String name = ((String) anit.next());
				AttributeValueData value = ((AttributeValueData) attributeValues.get(name));
				this.lines.add(new AttributeTableLine(name, value));
			}
			this.layoutLines();
		}
		
		void layoutLines() {
			this.removeAll();
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets.top = 0;
			gbc.insets.bottom = 0;
			gbc.insets.left = 0;
			gbc.insets.right = 0;
			gbc.weighty = 0;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.BOTH;
			
			for (int l = 0; l < this.lines.size(); l++) {
				AttributeTableLine line = ((AttributeTableLine) this.lines.get(l));
				gbc.gridx = 0;
				gbc.weightx = 0;
				this.add(line.removeButton, gbc.clone());
				gbc.gridx = 1;
				gbc.weightx = 1;
				this.add(line.displayLabel, gbc.clone());
				gbc.gridy++;
			}
			
			gbc.gridx = 0;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridwidth = 2;
			this.add(this.spacer, gbc.clone());
			
			this.validate();
			this.repaint();
		}
		
		private class AttributeTableLine {
			JButton removeButton;
			JLabel displayLabel;
			AttributeTableLine(final String name, AttributeValueData value) {
				this.removeButton = new JButton("<HTML>&nbsp;<B>X</B>&nbsp;</HTML>");
				this.removeButton.setToolTipText("Remove attribute '" + name + "'");
				this.removeButton.setBorder(BorderFactory.createRaisedBevelBorder());
				this.removeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						removeAttribute(name);
						lines.remove(AttributeTableLine.this);
						layoutLines();
					}
				});
				Color displayLabelBackground = Color.WHITE;
				if (value.selectedValue == null)
					this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B>:</HTML>");
				else if (value.selectedValue.value instanceof MultiAttributedWrapper.MultiAttributeValueSet) {
					MultiAttributedWrapper.MultiAttributeValueSet multiValue = ((MultiAttributedWrapper.MultiAttributeValueSet) value.selectedValue.value);
					Object valueObj = multiValue.getValue();
					if (multiValue.areValuesUnique()) {
						this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B>: &lt;" + multiValue.getValueCount(false) + " different unique values&gt;</HTML>");
						StringBuffer allValueObjects = new StringBuffer("<HTML>" + multiValue.getValueCount(false) + " different unique values" + (multiValue.hasEmptyValues() ? (", " + multiValue.getValueCount(null) + " empty ones") : "") + ":<UL>");
						Object[] valueObjs = multiValue.getValues();
						for (int v = 0; v < valueObjs.length; v++)
							allValueObjects.append("<LI>" + valueObjs[v].toString() + "</LI>");
						allValueObjects.append("</UL></HTML>");
						this.displayLabel.setToolTipText(allValueObjects.toString());
						displayLabelBackground = (multiValue.hasEmptyValues() ? Color.PINK : Color.CYAN);
					}
					else if (multiValue.getDistinctValueCount() > 1) {
						this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B> [" + multiValue.getValueCount(valueObj) + "/" + multiValue.getValueCount(true) + "]: " + ((valueObj == null) ? "" : valueObj.toString()) + "</HTML>");
						StringBuffer allValueObjects = new StringBuffer("<HTML>" + multiValue.getDistinctValueCount() + " different values" + (multiValue.hasEmptyValues() ? (", " + multiValue.getValueCount(null) + " empty ones") : "") + ":<UL>");
						Object[] valueObjs = multiValue.getValues();
						for (int v = 0; v < valueObjs.length; v++)
							allValueObjects.append("<LI>[" + multiValue.getValueCount(valueObjs[v]) + "/" + multiValue.getValueCount(true) + "] " + valueObjs[v].toString() + "</LI>");
						allValueObjects.append("</UL></HTML>");
						this.displayLabel.setToolTipText(allValueObjects.toString());
						displayLabelBackground = Color.YELLOW;
					}
					else if (multiValue.isAmbiguous()) /* only single non-empty value */ {
						this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B> [" + multiValue.getValueCount(valueObj) + "/" + multiValue.getValueCount(true) + "]: " + ((valueObj == null) ? "" : valueObj.toString()) + "</HTML>");
						displayLabelBackground = Color_LIME;
					}
					else if (multiValue.getValueCount(valueObj) == 1)
						this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B>: " + ((valueObj == null) ? "" : valueObj.toString()) + "</HTML>");
					else this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B> [" + multiValue.getValueCount(valueObj) + "]: " + ((valueObj == null) ? "" : valueObj.toString()) + "</HTML>");
				}
				else this.displayLabel = new JLabel("<HTML>&nbsp;<B>" + name + "</B>: " + ((value.selectedValue.value == null) ? "" : value.selectedValue.value.toString()) + "</HTML>");
				this.displayLabel.setOpaque(true);
				this.displayLabel.setBackground(displayLabelBackground);
				this.displayLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
				this.displayLabel.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent me) {
						attributeNameField.setSelectedItem(name);
					}
				});
			}
		}
	}
}