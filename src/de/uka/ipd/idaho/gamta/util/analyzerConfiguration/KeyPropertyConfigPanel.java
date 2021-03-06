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
package de.uka.ipd.idaho.gamta.util.analyzerConfiguration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.util.AnalyzerDataProvider;

/**
 * Widget for editing lists of keys (e.g. a, possibly extensible, list of
 * concepts) belonging to the configuration of an Analyzer, plus custom property
 * values for each key.
 * 
 * @author sautter
 */
public class KeyPropertyConfigPanel extends AnalyzerConfigPanel {
	private AnalyzerParameter.Store parameterStore;
	
	private class Parameter {
		final String name;
		final String value;
		final String tooltip;
		final String[] permittedValues;
		final String type;
		final JLabel label;
		Parameter(String name, String value, String tooltip, String[] permittedValues, String type) {
			this.name = name;
			this.value = value;
			this.tooltip = tooltip;
			this.permittedValues = permittedValues;
			this.type = type;
			
			this.label = new JLabel(this.name, JLabel.CENTER);
			this.label.setToolTipText(this.tooltip);
			this.label.setBorder(BorderFactory.createRaisedBevelBorder());
			Dimension prefSize;
			if (AnalyzerParameter.BOOLEAN_TYPE.equals(type) || AnalyzerParameter.COLOR_TYPE.equals(type))
				prefSize = new Dimension(Math.max((this.label.getPreferredSize().width + 6), 20), 21);
			else if (AnalyzerParameter.INTEGER_TYPE.equals(type) || AnalyzerParameter.NUMBER_TYPE.equals(type))
				prefSize = new Dimension(Math.max((this.label.getPreferredSize().width + 6), 80), 21);
			else prefSize = new Dimension(Math.max((this.label.getPreferredSize().width + 6), 160), 21);
			this.label.setPreferredSize(prefSize);
			this.label.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					tablePanel.requestFocusInWindow();
				}
			});
		}
	}
	
	private class Key {
		final String key;
		final JPanel labelPanel = new JPanel(new BorderLayout(), true);
		final Properties parameterValues;
		final TreeMap parameters = new TreeMap();
		Key(String key, Properties parameterValues) {
			this.key = key;
			this.parameterValues = ((parameterValues == null) ? new Properties() : parameterValues);
			
			JLabel label = new JLabel(" " + this.key);
			label.setOpaque(true);
			label.setBackground(Color.WHITE);
			JButton remove = new JButton("<html><b>X</b></html>");
			remove.setToolTipText("Remove");
			remove.setBackground(Color.WHITE);
			remove.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), BorderFactory.createMatteBorder(0, 3, 0, 2, Color.WHITE)));
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (removeKey(Key.this.key))
						layoutTable();
				}
			});
			JPanel labelPanel = new JPanel(new BorderLayout(), true);
			labelPanel.add(label, BorderLayout.CENTER);
			labelPanel.add(remove, BorderLayout.WEST);
			labelPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
			this.labelPanel.add(labelPanel, BorderLayout.NORTH);
			this.labelPanel.setBackground(Color.WHITE);
		}
		AnalyzerParameter getParameter(Parameter parameter) {
			AnalyzerParameter ap = ((AnalyzerParameter) this.parameters.get(parameter.name));
			if (ap == null) {
				String name = (this.key + "." + parameter.name);
				String value = this.parameterValues.getProperty(name);
				ap = AnalyzerParameter.getParameter(name, value, parameter.tooltip, parameter.permittedValues, parameter.type);
				if ((value == null) && (parameter.value != null))
					ap.updateValue(parameter.value);
				this.parameters.put(parameter.name, ap);
			}
			return ap;
		}
		boolean isDirty() {
			boolean dirty = false;
			for (Iterator pit = this.parameters.values().iterator(); pit.hasNext();)
				dirty = (((AnalyzerParameter) pit.next()).isDirty() || dirty);
			return dirty;
		}
		void setClean() {
			for (Iterator pit = this.parameters.values().iterator(); pit.hasNext();)
				((AnalyzerParameter) pit.next()).setClean();
		}
	}
	
	private ArrayList parameters = new ArrayList();
	private TreeMap keys = new TreeMap();
	private HashSet removebleKeys = new HashSet();
	private String keyListName;
	
	private String keyLabelString;
	private JLabel keyLabel;
	private JPanel tablePanelSpacer = new JPanel();
	private JPanel tablePanel = new JPanel(new GridBagLayout());
	
	private boolean dirty = false;
	
	/** Constructor
	 * @param dataProvider the data provider to read and write data from and to
	 * @param parameterDataName the name of the data object to store parameters to
	 * @param keyLabel the label for the keys to edit in this panel
	 * @param title the title of this panel to display in a tabbed pane
	 * @param toolTip the tooltip of this panel to display in a tabbed pane
	 */
	public KeyPropertyConfigPanel(AnalyzerDataProvider dataProvider, String parameterDataName, String keyLabel, String title, String toolTip) {
		this(new AnalyzerParameter.DefaultStore(dataProvider, parameterDataName), keyLabel, title, toolTip);
	}
	
	/** Constructor
	 * @param parameterStore the store object to persist parameters in
	 * @param keyLabel the label for the keys to edit in this panel
	 * @param title the title of this panel to display in a tabbed pane
	 * @param toolTip the tooltip of this panel to display in a tabbed pane
	 */
	public KeyPropertyConfigPanel(AnalyzerParameter.Store parameterStore, String keyLabel, String title, String toolTip) {
		this(parameterStore, keyLabel, title, toolTip, ((parameterStore instanceof AnalyzerParameter.Source) ? ((AnalyzerParameter.Source) parameterStore) : null));
	}

	/**
	 * Constructor
	 * @param parameterStore the store object to persist parameters in
	 * @param keyLabel the label for the keys to edit in this panel
	 * @param title the title of this panel to display in a tabbed pane
	 * @param toolTip the tooltip of this panel to display in a tabbed pane
	 * @param parameterSource a source object to load existing parameters from
	 *            (may be null)
	 */
	public KeyPropertyConfigPanel(AnalyzerParameter.Store parameterStore, String keyLabel, String title, String toolTip, AnalyzerParameter.Source parameterSource) {
		this("k_e_y_s", parameterStore, keyLabel, title, toolTip, parameterSource);
	}
	
	//	this constructor exists only for AnnotationTypeConfigPanel to be able to inject 't_y_p_e_s' as the key list name
	KeyPropertyConfigPanel(String keyListName, AnalyzerParameter.Store parameterStore, String keyLabel, String title, String toolTip, final AnalyzerParameter.Source parameterSource) {
		super(null, title, toolTip);
		this.keyListName = keyListName;
		
		this.parameterStore = parameterStore;
		
		this.keyLabelString = keyLabel;
		
		this.keyLabel = new JLabel(this.keyLabelString, JLabel.CENTER);
		this.keyLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		this.keyLabel.setPreferredSize(new Dimension(160, 21));
		this.keyLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				tablePanel.requestFocusInWindow();
			}
		});
		
		this.tablePanelSpacer.setBackground(Color.WHITE);
		this.tablePanelSpacer.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				tablePanel.requestFocusInWindow();
			}
		});
		this.tablePanel.setBorder(BorderFactory.createLineBorder(this.getBackground(), 3));
		this.tablePanel.setFocusable(true);
//		this.tablePanel.addFocusListener(new FocusAdapter() {
//			public void focusGained(FocusEvent fe) {
//				System.out.println("focusGained");
//			}
//			public void focusLost(FocusEvent fe) {
//				System.out.println("focusLost");
//			}
//		});
		
//		final String upKey = "GO_UP";
//		this.tablePanel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), upKey);
//		this.tablePanel.getActionMap().put(upKey, new AbstractAction() {
//			public void actionPerformed(ActionEvent ae) {
//				System.out.println(upKey);
//				if (selectedAnnotationType > 0)
//					selectParameter(selectedAnnotationType - 1);
//				else if (selectedAnnotationType == -1)
//					selectParameter(keys.size() - 1);
//			}
//		});
//		
//		final String downKey = "GO_DOWN";
//		this.tablePanel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), downKey);
//		this.tablePanel.getActionMap().put(downKey, new AbstractAction() {
//			public void actionPerformed(ActionEvent ae) {
//				System.out.println(downKey);
//				if (selectedAnnotationType == -1)
//					selectParameter(0);
//				else if ((selectedAnnotationType + 1) < keys.size())
//					selectParameter(selectedAnnotationType + 1);
//			}
//		});
		
		JScrollPane tablePanelBox = new JScrollPane(this.tablePanel);
		tablePanelBox.getVerticalScrollBar().setUnitIncrement(50);
		tablePanelBox.getVerticalScrollBar().setBlockIncrement(100);
		
		JButton addKeyButton = new JButton("Add " + this.keyLabelString);
		addKeyButton.setBorder(BorderFactory.createRaisedBevelBorder());
		addKeyButton.setPreferredSize(new Dimension(120, 21));
		addKeyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				addKey();
			}
		});
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER), true);
		buttonPanel.add(addKeyButton);
		
		this.add(tablePanelBox, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		if (parameterSource != null) {
			String typeString = parameterSource.getParameter(this.keyListName);
			if (typeString == null)
				return;
			String[] types = typeString.trim().split("\\s+");
			for (int t = 0; t < types.length; t++) {
				final String type = types[t];
				this.addKey(type, new Properties() {
					public String getProperty(String key, String defaultValue) {
						return parameterSource.getParameter((key.startsWith(type + ".") ? key : (type + "." + key)), defaultValue);
					}
					public String getProperty(String key) {
						return this.getProperty(key, null);
					}
				});
			}
		}
	}
	
	/**
	 * Modify the color of the parameter grid (default is white).
	 * @param gridColor the new grid color
	 */
	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
		this.layoutTable();
	}
	private Color gridColor = Color.WHITE;
	
	private void layoutTable() {
		this.tablePanel.removeAll();
		
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
		
		gbc.gridx = 0;
		gbc.weightx = 1;
		boolean useSpacer = false;
		this.tablePanel.add(this.keyLabel, gbc.clone());
		for (int p = 0; p < this.parameters.size(); p++) {
			Parameter param = ((Parameter) this.parameters.get(p));
			useSpacer = (useSpacer || AnalyzerParameter.TEXT_TYPE.equals(param.type));
			gbc.gridx++;
			this.tablePanel.add(param.label, gbc.clone());
		}
		gbc.gridy++;
		
		for (Iterator atit = this.keys.keySet().iterator(); atit.hasNext();) {
			Key k = ((Key) this.keys.get(((String) atit.next())));
			gbc.gridx = 0;
			k.labelPanel.setBorder(BorderFactory.createLineBorder(this.gridColor, 1));
			this.tablePanel.add(k.labelPanel, gbc.clone());
			for (int p = 0; p < this.parameters.size(); p++) {
				gbc.gridx++;
				JComponent valueDisplay = k.getParameter(((Parameter) this.parameters.get(p))).getValueDisplay(useSpacer, true);
				valueDisplay.setBorder(BorderFactory.createLineBorder(this.gridColor, 1));
				this.tablePanel.add(valueDisplay, gbc.clone());
			}
			gbc.gridy++;
		}
		
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = (this.parameters.size() + 1);
		this.tablePanel.add(this.tablePanelSpacer, gbc.clone());
		
		this.validate();
		this.repaint();
	}
	
//	private void selectParameter(int index) {
////		if (index == -1)
////			this.linePanel.requestFocusInWindow();
////		else ((AnalyzerParameter) this.annotationTypes.get(index)).focus();
//	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.analyzerConfiguration.AnalyzerConfigPanel#isDirty()
	 */
	public boolean isDirty() {
		if (this.dirty)
			return true;
		for (Iterator kit = this.keys.values().iterator(); kit.hasNext();)
			this.dirty = (((Key) kit.next()).isDirty() || this.dirty);
		return this.dirty;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.analyzerConfiguration.AnalyzerConfigPanel#commitChanges()
	 */
	public boolean commitChanges() throws IOException {
		if (!this.isDirty())
			return false;
		StringBuffer types = new StringBuffer();
		for (Iterator kit = this.keys.values().iterator(); kit.hasNext();) {
			Key k = ((Key) kit.next());
			types.append(" " + k.key);
			if (!k.isDirty())
				continue;
			for (int p = 0; p < this.parameters.size(); p++) {
				AnalyzerParameter ap = k.getParameter(((Parameter) this.parameters.get(p)));
				if (ap.isDirty())
					this.parameterStore.storeParameter(ap.name, ap.getValue());
			}
		}
		this.parameterStore.storeParameter(this.keyListName, types.toString().trim());
		this.parameterStore.persistParameters();
		for (Iterator atit = this.keys.values().iterator(); atit.hasNext();)
			((Key) atit.next()).setClean();
		this.dirty = false;
		return true;
	}
	
	private void addKey() {
		String newKey = JOptionPane.showInputDialog(this.tablePanel, ("Please enter the " + this.keyLabelString.toLowerCase() + " to add."), ("Add " + this.keyLabelString), JOptionPane.PLAIN_MESSAGE);
		if (newKey == null)
			return;
		while (!AnnotationUtils.isValidAnnotationType(newKey) || this.keys.containsKey(newKey)) {
			newKey = ((String) JOptionPane.showInputDialog(this.tablePanel, ("'" + newKey + "' " + (this.keys.containsKey(newKey) ? "already exists" : ("is not a valid " + this.keyLabelString.toLowerCase())) + ", please revise it."), ("Add " + this.keyLabelString), JOptionPane.ERROR_MESSAGE, null, null, newKey.trim()));
			if (newKey == null)
				return;
		}
		this.addKey(newKey, null, true);
	}
	
	private boolean removeKey(String key) {
		if (this.removebleKeys.remove(key)) {
			this.keys.remove(key);
			return true;
		}
		else {
			JOptionPane.showMessageDialog(this.tablePanel, ("The " + this.keyLabelString.toLowerCase() + " '" + key + "' is built-in and cannot be removed."), ("Cannot Remove " + this.keyLabelString), JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	/**
	 * Add a key. Keys added through this method can be edited, but cannot be
	 * removed. To facilitate the latter, use the three-argument version of this
	 * method.
	 * @param key the key to add
	 * @param parameterValues a properties object holding existing parameter
	 *            values for the new key
	 */
	public void addKey(String key, Properties parameterValues) {
		this.addKey(key, parameterValues, false);
	}
	
	/**
	 * Add a key. Keys that have to be present for an anylyzer to work can be
	 * protected from deletion by setting the 'removable' argument to false.
	 * @param key the key to add
	 * @param parameterValues a properties object holding existing parameter
	 *            values for the new key
	 * @param removable allow the key to be removed?
	 */
	public void addKey(String key, Properties parameterValues, boolean removable) {
		if ((key == null) || !AnnotationUtils.isValidAnnotationType(key))
			throw new IllegalArgumentException(this.keyLabelString + " must not be empty and must not contain whitespace, line breaks, or non ASCII characters.");
		Key k = new Key(key, parameterValues);
		this.keys.put(k.key, k);
		if (removable)
			this.removebleKeys.add(key);
		this.layoutTable();
		if (this.parameters.isEmpty())
			return;
		k.getParameter((Parameter) this.parameters.get(0)).focus();
	}
	
	/**
	 * Add a parameter to the panel. The name must not be empty and must not
	 * contain whitespace, line breaks, or non ASCII characters. If the
	 * permitted values are not null, set one of the array elements to null to
	 * indicate the parameter value is freely editable; otherwise, the value
	 * will be restricted to the elements of the array. The type must be one of
	 * the constants defined in the class AnalyzerParameter.
	 * @param name the name of the parameter
	 * @param value the default value of the parameter
	 * @param tooltip a tooltip text describing the parameter (may be null)
	 * @param permittedValues an array holding the values the parameter can take
	 * @param type the type of the parameter
	 */
	public void addParameter(String name, String value, String tooltip, String[] permittedValues, String type) {
		if ((name == null) || !AnnotationUtils.isValidAnnotationType(name))
			throw new IllegalArgumentException("Parameter names must not be empty and must not contain whitespace, line breaks, or non ASCII characters.");
		AnalyzerParameter ap = AnalyzerParameter.getParameter(name, value, tooltip, permittedValues, type);
		if (ap == null)
			throw new IllegalArgumentException("'" + type + "' is not a valid parameter type.");
		this.parameters.add(new Parameter(name, ap.getValue(), tooltip, permittedValues, type));
		this.layoutTable();
	}
}
