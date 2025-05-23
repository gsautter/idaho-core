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
package de.uka.ipd.idaho.gamta.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.DocumentError;
import de.uka.ipd.idaho.stringUtils.StringUtils;

/**
 * Visualization facility for document error protocols. By default, the panel
 * only contains the error category tabs in <code>BorderLayout.CENTER</code>
 * position. Client code may add other components around it if required.
 * 
 * @author sautter
 */
public class DocumentErrorProtocolDisplay extends JPanel {
	private static String ALL_ERRORS_FILTER_LABEL = "<All Errors>";
	
	private static class ErrorSeverityFilter extends JCheckBox implements ErrorFilter, ErrorDisplayListener {
		private String severity;
		private String baseLabel;
		ErrorSeverityFilter(String severity) {
			super("", true);
			this.severity = severity;
			this.baseLabel = (StringUtils.capitalize(severity) + "s");
			this.setText(this.baseLabel);
		}
		public boolean passes(DocumentError error) {
			return (this.isSelected() ? this.severity.equals(error.severity) : false);
		}
		private int matchCount = 0;
		private int displayMatchCount = 0;
		public void errorDisplayCleared() {
			this.matchCount = 0;
			this.displayMatchCount = 0;
		}
		public void errorAdded(DocumentError error, boolean displaying) {
			if (this.severity.equals(error.severity)) {
				this.matchCount++;
				if (displaying)
					this.displayMatchCount++;
			}
		}
		public void errorRemoved(DocumentError error, boolean displaying) {
			if (this.severity.equals(error.severity)) {
				this.matchCount--;
				if (displaying)
					this.displayMatchCount--;
			}
		}
		public void errorDisplayUpdated(int displaying, int total) {
//			for _active_ filters, show how many of how many overall errors match, and how many are actually showing
//			for  _inactive_ filters, show how many of how many overall errors would match, and how many would pass the other filters
			if (this.isSelected())
				this.setText(this.baseLabel + " (" + this.displayMatchCount + "/" + this.matchCount + " of " + total + ")");
			else this.setText(this.baseLabel + " (" + this.matchCount + " of " + total + ")");
		}
	}
	
	private DocumentErrorProtocol dep = null;
	private boolean depReadOnly = false;
	private Comparator depErrorOrder = null;
	private String errorCategoryOrder = null;
	private String errorTypeOrder = null;
	
	private Comparator errorTabOrder = new Comparator() {
		public int compare(Object obj1, Object obj2) {
			String ec1 = ((String) obj1);
			String ec2 = ((String) obj2);
			if (ec1.equals(ec2))
				return 0;
			if (errorCategoryOrder != null) {
				int pos1 = errorCategoryOrder.indexOf(ec1);
				int pos2 = errorCategoryOrder.indexOf(ec2);
				if (pos1 == pos2) {}
				else if (pos1 == -1)
					return 1;
				else if (pos2 == -1)
					return -1;
				else return (pos1 - pos2);
			}
			return String.CASE_INSENSITIVE_ORDER.compare(ec1, ((String) ec2));
		}
	};
	private TreeMap errorTabsByCategory = new TreeMap(this.errorTabOrder);
	private JTabbedPane errorTabs = new JTabbedPane();
	private int categoryTabPlacement = JTabbedPane.LEFT;
	private boolean muteErrorCategoryChanges = false;
	
	private String resolveButtonText = "Resolve Error";
	private JButton resolveButton = new JButton(this.resolveButtonText);
	private String falsePosButtonText = "False Positive";
	private JButton falsePosButton = new JButton(this.falsePosButtonText);
	private JButton[] customButtons = null;
	private JPanel errorButtonPanel = new JPanel(new GridLayout(1, 0, 5, 0), true);
	private boolean showButtons = true;
	
	private ErrorSeverityFilter showBlockers = new ErrorSeverityFilter(DocumentError.SEVERITY_BLOCKER);
	private ErrorSeverityFilter showCriticals = new ErrorSeverityFilter(DocumentError.SEVERITY_CRITICAL);
	private ErrorSeverityFilter showMajors = new ErrorSeverityFilter(DocumentError.SEVERITY_MAJOR);
	private ErrorSeverityFilter showMinors = new ErrorSeverityFilter(DocumentError.SEVERITY_MINOR);
	private JPanel showErrorsPanel = new JPanel(new GridLayout(1, 0), true);
	private boolean severityFiltersActive = true;
	private ErrorFilter errorSeverityFilter;
	
	private LinkedHashSet errorFilters = new LinkedHashSet();
	private LinkedHashSet errorDisplayListeners = new LinkedHashSet();
	
	private boolean highlightLabeledErrors = true;
	private boolean autoSelectNextError = true;
	
	/**
	 * Constructor
	 */
	public DocumentErrorProtocolDisplay() {
		this(null, false);
	}
	
	/**
	 * Constructor
	 * @param dep the document error protocol to show
	 */
	public DocumentErrorProtocolDisplay(DocumentErrorProtocol dep) {
		this(dep, false);
	}
	
	/**
	 * Constructor
	 * @param dep the document error protocol to show
	 * @param readOnly open in read-only mode?
	 */
	public DocumentErrorProtocolDisplay(DocumentErrorProtocol dep, boolean readOnly) {
		super(new BorderLayout(), true);
		
		//	initialize buttons ...
		this.resolveButton.setBorder(BorderFactory.createRaisedBevelBorder());
		this.resolveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeError(false);
			}
		});
		this.falsePosButton.setBorder(BorderFactory.createRaisedBevelBorder());
		this.falsePosButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeError(true);
			}
		});
		
		//	... and tray them up
		this.errorButtonPanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 3, this.errorButtonPanel.getBackground()));
		this.errorButtonPanel.add(this.resolveButton);
		this.errorButtonPanel.add(this.falsePosButton);
		
		//	configure severity filters ...
		ItemListener showSeverityListener = new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				applyErrorFilters();
			}
		};
		this.showBlockers.addItemListener(showSeverityListener);
		this.showCriticals.addItemListener(showSeverityListener);
		this.showMajors.addItemListener(showSeverityListener);
		this.showMinors.addItemListener(showSeverityListener);
		ErrorFilter[] severityFilters = {this.showBlockers, this.showCriticals, this.showMajors, this.showMinors};
		this.errorSeverityFilter = buildDisjunctiveErrorFilter(severityFilters);
		
		//	... and tray them up
		this.showErrorsPanel.add(this.showBlockers);
		this.showErrorsPanel.add(this.showCriticals);
		this.showErrorsPanel.add(this.showMajors);
		this.showErrorsPanel.add(this.showMinors);
		this.showErrorsPanel.setBorder(BorderFactory.createEtchedBorder());
		
		//	configure and add error tabs
		this.errorTabs.setTabPlacement(this.categoryTabPlacement);
		this.errorTabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				moveAccessories();
//				applySeverityFilter();
				notifyErrorCategorySelected();
				notifyErrorSelected();
			}
		});
		this.add(this.errorTabs, BorderLayout.CENTER);
		
		//	show initial content
		this.setErrorProtocol(dep, readOnly);
	}
	
	/**
	 * Set the error protocol to display.
	 * @param dep the error protocol
	 */
	public void setErrorProtocol(DocumentErrorProtocol dep) {
		this.setErrorProtocol(dep, this.depReadOnly);
	}
	
	/**
	 * Set the error protocol to display.
	 * @param dep the error protocol
	 * @param readOnly show the protocol in read-only mode?
	 */
	public void setErrorProtocol(DocumentErrorProtocol dep, boolean readOnly) {
		this.dep = dep;
		this.depReadOnly = readOnly;
		if (this.dep != null)
			this.depErrorOrder = this.dep.getErrorComparator();
		
		//	mute notifications for initialization
		this.muteErrorCategoryChanges = true;
		
		//	enable/disable buttons
		this.resolveButton.setEnabled(!this.depReadOnly);
		this.falsePosButton.setEnabled(!this.depReadOnly);
		if (this.customButtons != null) {
			for (int b = 0; b < this.customButtons.length; b++)
				this.customButtons[b].setEnabled(!this.depReadOnly);
		}
		
		//	clear error tabs
		this.errorTabs.removeAll();
		this.errorTabsByCategory.clear();
		if (this.dep == null)
			return;
		
		//	add all errors from current protocol
		String[] errorCategories = dep.getErrorCategories();
		for (int c = 0; c < errorCategories.length; c++) {
			if (this.dep.getErrorCount(errorCategories[c]) == 0)
				continue;
			ErrorCategoryDisplay ecd = new ErrorCategoryDisplay(errorCategories[c], this.dep);
			this.errorTabs.add(this.getErrorTabLabel(errorCategories[c]), ecd);
			this.errorTabsByCategory.put(errorCategories[c], ecd);
		}
		/*
TODO MAYBE, add "All Errors" category tab ...
... showing whole list of errors ...
... with two-flag setting (showByCategory, showAll) ...
... and show categories and/or overall lists accordingly
==> maybe switch based upon overall number of errors ...
==> ... or even provide "Show by Category" checkbox ...
==> ... and automatically switch to "All Errors" below some threshold
  ==> provide getter and setter for latter
		 */
		
		//	make errors show
		this.validate();
		this.repaint();
		
		//	(re)activate notifications
		this.muteErrorCategoryChanges = false;
		
		//	notify about initial error category
		this.updateAccessories();
		this.notifyErrorCategorySelected();
	}
	
	/**
	 * Check whether or not buttons are showing.
	 * @return true if buttons are showing
	 */
	public boolean isShowingButtons() {
		return this.showButtons;
	}
	
	/**
	 * Show or hide buttons that take actions on the currently selected error,
	 * both built-in and custom.
	 * @param showButtons show buttons?
	 */
	public void setShowButtons(boolean showButtons) {
		if (showButtons == this.showButtons)
			return;
		this.showButtons = showButtons;
		this.updateAccessories();
	}
	
	/**
	 * Check whether or not error severity filters are showing.
	 * @return true if error severity filters are showing
	 */
	public boolean isShowingSeverityFilters() {
		return this.severityFiltersActive;
	}
	
	/**
	 * Show or hide the checkboxes that filter errors by severity.
	 * @param showFilters show the severity filters?
	 */
	public void setShowSeverityFilters(boolean showFilters) {
		if (showFilters == this.severityFiltersActive)
			return;
		this.severityFiltersActive = showFilters;
		this.updateAccessories();
	}
	
	/**
	 * Check whether auto-selecting of next error on removal of selected is
	 * switched one on or off.
	 * @return true if auto-selecting next error is switched on
	 */
	public boolean isAutoSelectingNextError() {
		return this.autoSelectNextError;
	}
	
	/**
	 * Switch auto-selecting next error on removal of selected one on or off.
	 * @param autoSelect automatically select next error?
	 */
	public void setAutoSelectNextError(boolean autoSelect) {
		this.autoSelectNextError = autoSelect;
	}
	
	/**
	 * Retrieve the current state of the error severity filters. If the filters
	 * are not showing, this method return null.
	 * @return the severity filter state
	 */
	public String getSeverityFilterState() {
		if (!this.severityFiltersActive)
			return null;
		StringBuffer sfs = new StringBuffer();
		if (this.showBlockers.isSelected()) {
			if (sfs.length() != 0)
				sfs.append("-");
			sfs.append("BL");
		}
		if (this.showCriticals.isSelected()) {
			if (sfs.length() != 0)
				sfs.append("-");
			sfs.append("CR");
		}
		if (this.showMajors.isSelected()) {
			if (sfs.length() != 0)
				sfs.append("-");
			sfs.append("MA");
		}
		if (this.showMinors.isSelected()) {
			if (sfs.length() != 0)
				sfs.append("-");
			sfs.append("MI");
		}
		return sfs.toString();
	}
	
	/**
	 * Set the state of the error severity filters. If the argument status is
	 * null, severity filters are set to not showing.
	 * @param the severity filter state
	 */
	public void setSeverityFilterState(String filterState) {
		if (filterState == null) {
			this.setShowSeverityFilters(false);
			return;
		}
		this.showBlockers.setSelected(filterState.indexOf("BL") != -1);
		this.showCriticals.setSelected(filterState.indexOf("CR") != -1);
		this.showMajors.setSelected(filterState.indexOf("MA") != -1);
		this.showMinors.setSelected(filterState.indexOf("MI") != -1);
	}
	
	/**
	 * Get the placement of error category tabs.
	 * @return the placement of error category tabs
	 */
	public int getCategoryTabPlacement() {
		return this.categoryTabPlacement;
	}
	
	/**
	 * Set the placement of the error category tabs (default is on the left).
	 * @param tabPlacement the new error category tab placement
	 */
	public void setCategoryTabPlacement(int tabPlacement) {
		if (tabPlacement == this.categoryTabPlacement)
			return;
		this.categoryTabPlacement = tabPlacement;
		this.validate();
		this.repaint();
	}
	
	/**
	 * Set the text of the built-in 'Resolve Error' button. Setting the text to
	 * null effectively deactivates that button and removes it from the panel.
	 * @param text the button text
	 */
	public void setResolveErrorButtonText(String text) {
		if (this.resolveButtonText == text)
			return;
		if ((this.resolveButtonText == null) || (text == null)) {
			this.resolveButtonText = text;
			this.updateButtons();
		}
		else {
			this.resolveButtonText = text;
			this.resolveButton.setText(this.resolveButtonText);
			this.resolveButton.validate();
			this.resolveButton.repaint();
		}
	}
	
	/**
	 * Set the text of the built-in 'False Positive' button. Setting the text
	 * to null effectively deactivates that button and removes it from the
	 * panel.
	 * @param text the button text
	 */
	public void setFalsePositiveButtonText(String text) {
		if (this.falsePosButtonText == text)
			return;
		if ((this.falsePosButtonText == null) || (text == null)) {
			this.falsePosButtonText = text;
			this.updateButtons();
		}
		else {
			this.falsePosButtonText = text;
			this.falsePosButton.setText(this.falsePosButtonText);
			this.falsePosButton.validate();
			this.falsePosButton.repaint();
		}
	}
	
	/**
	 * Inject an array of custom buttons to take action on the currently
	 * selected error. These buttons show next to the 'Resolve Error' and
	 * 'False Positive' buttons.
	 * @param buttons the an array holding the buttons to set
	 */
	public void setCustomButtons(JButton[] buttons) {
		if (Arrays.deepEquals(buttons, this.customButtons))
			return;
		this.customButtons = buttons;
		this.updateButtons();
	}
	
	private void updateButtons() {
		this.errorButtonPanel.removeAll();
		if (this.resolveButtonText != null) {
			this.resolveButton.setText(this.resolveButtonText);
			this.errorButtonPanel.add(this.resolveButton);
		}
		if (this.falsePosButtonText != null) {
			this.falsePosButton.setText(this.falsePosButtonText);
			this.errorButtonPanel.add(this.falsePosButton);
		}
		if (this.customButtons != null) {
			for (int b = 0; b < this.customButtons.length; b++)
				this.errorButtonPanel.add(this.customButtons[b]);
		}
		this.updateAccessories();
	}
	
	void moveAccessories() {
		if (this.muteErrorCategoryChanges)
			return;
		if (this.dep == null)
			return;
		this.updateAccessories();
	}
	
	void updateAccessories() {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		if (ecd != null)
			ecd.updateAccessories();
	}
	
	void removeError(boolean falsePositive) {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		if (ecd != null)
			ecd.removeError(falsePositive);
	}
	
	void notifyErrorCategorySelected() {
		if (this.muteErrorCategoryChanges)
			return;
		if (this.dep == null)
			this.errorCategorySelected(null, 0);
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		if (ecd == null)
			this.errorCategorySelected(null, 0);
		else this.errorCategorySelected(ecd.category, this.dep.getErrorCount(ecd.category));
	}
	
	void errorCategoryChanged(ErrorCategoryDisplay ecd) {
		int index = this.errorTabs.indexOfComponent(ecd);
		if (index != -1)
			this.errorTabs.setTitleAt(index, this.getErrorTabLabel(ecd.category));
	}
	
	void errorCategoryEmpty(ErrorCategoryDisplay ecd) {
		this.errorTabs.remove(ecd);
		this.errorTabsByCategory.remove(ecd.category);
	}
	
	String getErrorTabLabel(String category) {
		return (this.dep.getErrorCategoryLabel(category) + " (" + this.dep.getErrorCount(category) + ")");
	}
	
	void notifyErrorSelected() {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		if (ecd != null)
			this.errorSelected(ecd.getSelectedError());
	}
	
	/**
	 * Retrieve the current error category order.
	 * @return the error category order
	 */
	public String getErrorCategoryOrder() {
		return this.errorCategoryOrder;
	}
	
	/**
	 * Set the error category order. Categories occurring earlier in the argument
	 * string appear before ones occurring later, and those before one that do
	 * not occur in the argument string at all. This is to allow for error order
	 * customization by client code.
	 * @param eco the error category order
	 */
	public void setErrorCategoryOrder(String eco) {
		if (this.errorTabs.getTabCount() < 2) {
			this.errorCategoryOrder = eco;
			return;
		}
		
		ErrorCategoryDisplay set = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		HashMap ets = new HashMap();
		ets.putAll(this.errorTabsByCategory);
		LinkedList etcs = new LinkedList(this.errorTabsByCategory.keySet());
		
		this.errorTabsByCategory.clear();
		this.errorCategoryOrder = eco;
		this.errorTabsByCategory.putAll(ets);
		
		boolean errorTabsInOrder = true;
		for (Iterator etcit = this.errorTabsByCategory.keySet().iterator(); etcit.hasNext();) {
			String etc = ((String) etcit.next());
			if ((etcs.size() != 0) && etc.equals(etcs.removeFirst()))
				continue;
			errorTabsInOrder = false;
			break;
		}
		if (errorTabsInOrder)
			return;
		
		this.muteErrorCategoryChanges = true;
		this.errorTabs.removeAll();
		for (Iterator etcit = this.errorTabsByCategory.keySet().iterator(); etcit.hasNext();) {
			String etc = ((String) etcit.next());
			ErrorCategoryDisplay et = ((ErrorCategoryDisplay) this.errorTabsByCategory.get(etc));
			this.errorTabs.addTab(this.getErrorTabLabel(etc), et);
		}
		this.errorTabs.setSelectedComponent(set);
		this.muteErrorCategoryChanges = false;
	}
	
	/**
	 * Retrieve the current error type order.
	 * @return the error type order
	 */
	public String getErrorTypeOrder() {
		return this.errorTypeOrder;
	}
	
	/**
	 * Set the error type order. Types occurring earlier in the argument string
	 * appear before ones occurring later, and those before one that do not
	 * occur in the argument string at all. This is to allow for error order
	 * customization by client code. Setting the error type order only has an
	 * effect if the error category order is set as well.
	 * @param eto the error type order
	 */
	public void setErrorTypeOrder(String eto) {
		this.errorTypeOrder = eto;
	}
	
	/**
	 * Notify the display that an error was added to the underlying protocol.
	 * This method is meant for notification by protocol implementations that
	 * automatically adjust to document edits.
	 * @param error the error that was added
	 */
	public void errorAdded(DocumentError error) {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabsByCategory.get(error.category));
		if (ecd == null) {
			ecd = new ErrorCategoryDisplay(error.category, this.dep);
			this.errorTabsByCategory.put(error.category, ecd);
			this.errorTabs.add(this.getErrorTabLabel(error.category), ecd);
			this.errorTabs.setSelectedComponent(ecd);
			ecd.updateAccessories();
		}
		else ecd.errorAdded(error);
	}
	
	/**
	 * Notify the display that an error was removed from the underlying protocol.
	 * This method is meant for notification by protocol implementations that
	 * automatically adjust to document edits.
	 * @param error the error that was removed
	 */
	public void errorRemoved(DocumentError error) {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabsByCategory.get(error.category));
		if (ecd != null)
			ecd.errorRemoved(error);
	}
	
	/**
	 * Notify an implementing subclass that an error category has been selected.
	 * The argument category is never null unless the last error was removed
	 * from the backing protocol. This default implementation does nothing.
	 * @param category the error category that was selected
	 * @param errorCount the number of errors in the category and type
	 */
	protected void errorCategorySelected(String category, int errorCount) {}
	
	/**
	 * Notify an implementing subclass that an error type has been selected.
	 * The argument error type is null if the wildcard filter was selected in
	 * the argument category. The argument category is never null unless the
	 * last error was removed from the backing protocol. This default
	 * implementation does nothing.
	 * @param type the error type that was selected
	 * @param category the category the selected error type belongs to
	 * @param errorCount the number of errors in the category and type
	 */
	protected void errorTypeSelected(String type, String category, int errorCount) {}
	
	/**
	 * Notify an implementing subclass that an error has been selected. The
	 * runtime class of the argument error is the one of the errors obtained
	 * from the current error protocol. This default implementation does
	 * nothing.
	 * @param error the error that was selected
	 */
	protected void errorSelected(DocumentError error) {}
	
	/**
	 * Notify an implementing subclass that an error has been removed, either
	 * as resolved or as a false positive. The runtime class of the argument
	 * error is the one of the errors obtained from the current error protocol.
	 * This default implementation does nothing.
	 * @param error the error that was removed
	 * @param falsePositive was the removed error marked as a false positive?
	 */
	protected void errorRemoved(DocumentError error, boolean falsePositive) {}
	
	/**
	 * Retrieve the currently selected error category.
	 * @return the error category
	 */
	public String getErrorCategory() {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		return ((ecd == null) ? null : ecd.category);
	}
	
	/**
	 * Select an error category programmatically.
	 * @param category the error category
	 */
	public boolean setErrorCategory(String category) {
		if (category == null)
			return false;
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabsByCategory.get(category));
		if (ecd == null)
			return false;
		this.muteErrorCategoryChanges = true;
		ecd.updateAccessories();
		this.errorTabs.setSelectedComponent(ecd);
		this.muteErrorCategoryChanges = false;
		return true;
	}
	
	/**
	 * Retrieve the currently selected error type.
	 * @return the error type
	 */
	public String getErrorType() {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		return ((ecd == null) ? null : ecd.getErrorType());
	}
	
	/**
	 * Select an error category and type programatically.
	 * @param category the error category
	 * @param type the error type
	 */
	public boolean setErrorType(String category, String type) {
		return (this.setErrorCategory(category) && ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent()).setErrorType(type));
	}
	
	/**
	 * Select a given error programatically, also selecting category and type.
	 * @param error the error to select
	 */
	public boolean setError(DocumentError error) {
		if (error == null)
			return false;
		return (this.setErrorCategory(error.category) && ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent()).setError(error));
	}
	
	/**
	 * Dispose of the error protocol display, clean up and unregister any
	 * external references, etc. This default implementation does nothing,
	 * sub classes are welcome to overwrite it as needed.
	 */
	public void dispose() {}
	
	private final Comparator errorTrayOrder = new Comparator() {
		public int compare(Object obj1, Object obj2) {
			ErrorTray et1 = ((ErrorTray) obj1);
			ErrorTray et2 = ((ErrorTray) obj2);
			if ((errorCategoryOrder != null) && !et1.error.category.equals(et2.error.category)) {
				int pos1 = errorCategoryOrder.indexOf(et1.error.category);
				int pos2 = errorCategoryOrder.indexOf(et2.error.category);
				if (pos1 == pos2) {}
				else if (pos1 == -1)
					return 1;
				else if (pos2 == -1)
					return -1;
				else return (pos1 - pos2);
			}
			if ((errorCategoryOrder != null) && (errorTypeOrder != null) && !et1.error.type.equals(et2.error.type)) {
				int pos1 = errorTypeOrder.indexOf(et1.error.type);
				int pos2 = errorTypeOrder.indexOf(et2.error.type);
				if (pos1 == pos2) {}
				else if (pos1 == -1)
					return 1;
				else if (pos2 == -1)
					return -1;
				else return (pos1 - pos2);
			}
			return ((depErrorOrder == null) ? 0 : depErrorOrder.compare(et1.error, et2.error));
		}
	};
	
	/**
	 * Provide a label for a given error. This default implementation returns
	 * null, sub classes are welcome to overwrite it as needed.
	 * @param error the error to get the label for
	 * @return the label for the argument error
	 */
	protected String getErrorLabel(DocumentError error) {
		return null;
	}
	
	/**
	 * Check whether or not this error protocol display is highlighting errors
	 * that have a label, i.e., ones for which the <code>getErrorLabel()</code>
	 * method returns a non-null value.
	 * @return true if labeled errors are highlighted, false otherwise
	 */
	public boolean isHighlightingLabeledErrors() {
		return this.highlightLabeledErrors;
	}
	
	/**
	 * Activate or deactivate highlighting of labeled errors. Setting this
	 * property to true will have labeled errors, i.e., errors for which the
	 * <code>getErrorLabel()</code> method returns a non-null value, rendered
	 * in bold.
	 * @param hle highlight labeled errors?
	 */
	public void setHighlightLabeledErrors(boolean hle) {
		if (this.highlightLabeledErrors == hle)
			return;
		this.highlightLabeledErrors = hle;
		this.validate();
		this.repaint();
	}
	
	/**
	 * Add a custom error filter to the protocol display. If the filter is
	 * added for the first time, this entails a call to the
	 * <code>applyErrorFilters()</code> method. Also, if the argument filter
	 * also is an error display listener, it is added in the latter capacity
	 * as well. The combination is mainly intended for filters that are based
	 * upon UI components that want to display status information.
	 * @param filter the error filter to add
	 */
	public void addErrorFilter(ErrorFilter filter) {
		if (filter == null)
			return;
		if (filter instanceof ErrorDisplayListener)
			this.addErrorDisplayListener((ErrorDisplayListener) filter);
		if (this.errorFilters.add(filter))
			this.applyErrorFilters();
	}
	
	/**
	 * Remove a custom error filter to the protocol display. If the filter was
	 * actually present, this entails a call to the
	 * <code>applyErrorFilters()</code> method. Also, if the argument filter
	 * also is an error display listener, it is removed in the latter capacity
	 * as well. The combination is mainly intended for filters that are based
	 * upon UI components that want to display status information.
	 * @param filter the error filter to remove
	 */
	public void removeErrorFilter(ErrorFilter filter) {
		if (filter == null)
			return;
		if (filter instanceof ErrorDisplayListener)
			this.removeErrorDisplayListener((ErrorDisplayListener) filter);
		if (this.errorFilters.remove(filter))
			this.applyErrorFilters();
	}
	
	/**
	 * Re-apply error filters, both internal and custom ones. This will update
	 * the error protocol display based upon the filters present.
	 */
	public void applyErrorFilters() {
		ErrorCategoryDisplay ecd = ((ErrorCategoryDisplay) this.errorTabs.getSelectedComponent());
		if (ecd != null)
			ecd.updateErrorList();
	}
	
	/**
	 * A custom filter for errors to show in the protocol display.
	 * 
	 * @author sautter
	 */
	public static interface ErrorFilter {
		
		/**
		 * Test whether or not a given document error passes the filter.
		 * @param error the error to check
		 * @return rue if the argument error passes the filter, false otherwise
		 */
		public abstract boolean passes(DocumentError error);
	}
	
	/**
	 * Add an error display listener to receive notifications of updates to the
	 * error display.
	 * @param edl the error display listener to add
	 */
	public void addErrorDisplayListener(ErrorDisplayListener edl) {
		if (edl != null)
			this.errorDisplayListeners.add(edl);
	}
	
	/**
	 * Add an error display listener to receive notifications of updates to the
	 * error display.
	 * @param edl the error display listener to add
	 */
	public void removeErrorDisplayListener(ErrorDisplayListener edl) {
		if (edl != null)
			this.errorDisplayListeners.remove(edl);
	}
	
	/**
	 * Observer of the document error display, to receive notification of
	 * updates to the displaying list of errors, e.g. as filters or filter
	 * settings change.
	 * 
	 * @author sautter
	 */
	public static interface ErrorDisplayListener {
		
		/**
		 * Receive notification that the error display was cleared, usually to
		 * the re-populated from scratch.
		 */
		public abstract void errorDisplayCleared();
		
		/**
		 * Receive notification that an error was added to the protocol whose
		 * content is showing in the display, regardless of whether or not the
		 * error is actually showing.
		 * @param error the error that was added
		 * @param displaying is the error actually displaying?
		 */
		public abstract void errorAdded(DocumentError error, boolean displaying);
		
		/**
		 * Receive notification that an error was removed from the protocol
		 * whose content is showing in the display, regardless of whether or
		 * not the error is actually showing.
		 * @param error the error that was removed
		 * @param displaying is the error actually displaying?
		 */
		public abstract void errorRemoved(DocumentError error, boolean displaying);
		
		/**
		 * Receive notification that the error protocol display was updated.
		 * This method is called at the end of an update, before the changes
		 * show in the UI. The number of displaying errors is the number of
		 * errors that are visible under the current filter settings.
		 * @param displaying the number of displaying errors
		 * @param total the total number of errors in the selected category
		 */
		public abstract void errorDisplayUpdated(int displaying, int total);
	}
	
	void notifyErrorDisplayCleared(ErrorDisplayListener errorTypeFilter) {
		if (this.severityFiltersActive)
			((ErrorDisplayListener) this.errorSeverityFilter).errorDisplayCleared();
		if (errorTypeFilter != null)
			errorTypeFilter.errorDisplayCleared();
		for (Iterator edlit = this.errorDisplayListeners.iterator(); edlit.hasNext();)
			((ErrorDisplayListener) edlit.next()).errorDisplayCleared();
	}
	
	void notifyErrorAdded(ErrorDisplayListener errorTypeFilter, DocumentError error, boolean displaying) {
		if (this.severityFiltersActive)
			((ErrorDisplayListener) this.errorSeverityFilter).errorAdded(error, displaying);
		if (errorTypeFilter != null)
			errorTypeFilter.errorAdded(error, displaying);
		for (Iterator edlit = this.errorDisplayListeners.iterator(); edlit.hasNext();)
			((ErrorDisplayListener) edlit.next()).errorAdded(error, displaying);
	}
	
	void notifyErrorRemoved(ErrorDisplayListener errorTypeFilter, DocumentError error, boolean displaying) {
		if (this.severityFiltersActive)
			((ErrorDisplayListener) this.errorSeverityFilter).errorRemoved(error, displaying);
		if (errorTypeFilter != null)
			errorTypeFilter.errorRemoved(error, displaying);
		for (Iterator edlit = this.errorDisplayListeners.iterator(); edlit.hasNext();)
			((ErrorDisplayListener) edlit.next()).errorRemoved(error, displaying);
	}
	
	void notifyErrorDisplayUpdated(ErrorDisplayListener errorTypeFilter, int displaying, int total) {
		if (this.severityFiltersActive)
			((ErrorDisplayListener) this.errorSeverityFilter).errorDisplayUpdated(displaying, total);
		if (errorTypeFilter != null)
			errorTypeFilter.errorDisplayUpdated(displaying, total);
		for (Iterator edlit = this.errorDisplayListeners.iterator(); edlit.hasNext();)
			((ErrorDisplayListener) edlit.next()).errorDisplayUpdated(displaying, total);
	}
	
	/**
	 * Create an error filter passing any document error that passes at least
	 * one of a number of given error filters. If any of the error filters in
	 * the argument array are also error display listeners, so is the returned
	 * combined error filter.
	 * @param filters the filters to wrap into a disjunctive one
	 * @return the disjunctive filter
	 */
	public static ErrorFilter buildDisjunctiveErrorFilter(ErrorFilter[] filters) {
		
		//	check if we need to listen to display updates
		LinkedHashSet filterList = new LinkedHashSet(filters.length);
		boolean plain = true;
		for (int f = 0; f < filters.length; f++) {
			if (filters[f] != null)
				filterList.add(filters[f]);
			if (filters[f] instanceof ErrorDisplayListener)
				plain = false;
			
		}
		
		//	return plain or listening error filter
		if (plain)
			return new PlainDisjunctiveErrorFilter(((ErrorFilter[]) filterList.toArray(new ErrorFilter[filterList.size()])));
		else return new ListeningDisjunctiveErrorFilter(((ErrorFilter[]) filterList.toArray(new ErrorFilter[filterList.size()])));
	}
	
	private static class PlainDisjunctiveErrorFilter implements ErrorFilter {
		private ErrorFilter[] filters;
		PlainDisjunctiveErrorFilter(ErrorFilter[] filters) {
			this.filters = filters;
		}
		public boolean passes(DocumentError error) {
			for (int f = 0; f < this.filters.length; f++) {
				if (this.filters[f].passes(error))
					return true;
			}
			return false;
		}
	}
	
	private static class ListeningDisjunctiveErrorFilter implements ErrorFilter, ErrorDisplayListener {
		private ErrorFilter[] filters;
		ListeningDisjunctiveErrorFilter(ErrorFilter[] filters) {
			this.filters = filters;
		}
		public boolean passes(DocumentError error) {
			for (int f = 0; f < this.filters.length; f++) {
				if (this.filters[f].passes(error))
					return true;
			}
			return false;
		}
		public void errorDisplayCleared() {
			for (int f = 0; f < this.filters.length; f++) {
				if (this.filters[f] instanceof ErrorDisplayListener)
					((ErrorDisplayListener) this.filters[f]).errorDisplayCleared();
			}
		}
		public void errorAdded(DocumentError error, boolean displaying) {
			for (int f = 0; f < this.filters.length; f++) {
				if (this.filters[f] instanceof ErrorDisplayListener)
					((ErrorDisplayListener) this.filters[f]).errorAdded(error, displaying);
			}
		}
		public void errorRemoved(DocumentError error, boolean displaying) {
			for (int f = 0; f < this.filters.length; f++) {
				if (this.filters[f] instanceof ErrorDisplayListener)
					((ErrorDisplayListener) this.filters[f]).errorRemoved(error, displaying);
			}
		}
		public void errorDisplayUpdated(int displaying, int total) {
			for (int f = 0; f < this.filters.length; f++) {
				if (this.filters[f] instanceof ErrorDisplayListener)
					((ErrorDisplayListener) this.filters[f]).errorDisplayUpdated(displaying, total);
			}
		}
	}
	
	/*
TODO MAYBE, create public static abstract class JCheckBoxErrorFilter ...
... with behavior akin to severity filters ...
... implementing listener for UI updates

TODO MAYBE, create public static abstract class AlternativeErrorFilter ...
... for wrapping in JComboBoxErrorFilter or JRadioButtonErrorFilter
  ==> provide respective factory methods
... with latter implementing behavior akin to type filter ...
... implementing listener for UI updates
	 */
	
	private class ErrorCategoryDisplay extends JPanel {
		final String category;
		private JLabel description;
		
		private ErrorTypeFilter errorTypeFilter = new ErrorTypeFilter();
		private JPanel topPanel = new JPanel(new GridLayout(0, 1, 0, 2), true);
		
		private Vector errorTrays = new Vector();
		private Vector listErrorTrays = new Vector();
		
		private ErrorListModel errorListModel = new ErrorListModel();
		private JList errorList = new JList(this.errorListModel);
		
		ErrorCategoryDisplay(String category, DocumentErrorProtocol dep) {
			super(new BorderLayout(), true);
			System.out.println("Initializing error display for category " + category);
			this.category = category;
			this.description = new JLabel("<HTML><B>" + dep.getErrorCategoryDescription(this.category) + "</B></HTML>", JLabel.CENTER);
			DocumentError[] errors = dep.getErrors(this.category);
			
			//	populate error list and filters
			for (int e = 0; e < errors.length; e++)
				this.errorTrays.add(new ErrorTray(errors[e]));
			
			//	add functionality
			this.errorTypeFilter.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(this.getBackground()), BorderFactory.createLoweredBevelBorder()));
			this.errorTypeFilter.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					updateErrorTypeFilter();
				}
			});
			
			//	build error list
			this.errorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.errorList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent lse) {
					if (!lse.getValueIsAdjusting())
						selectError();
				}
			});
//			this.errorList.addFocusListener(new FocusListener() {
//				public void focusGained(FocusEvent fe) {
//					//	TODO_ne do we really need this ??? ==> mouse click listener appears to work better
//					selectError(); // need to select (if current) error when getting back focus
//				}
//				public void focusLost(FocusEvent fe) {}
//			});
			this.errorList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					selectError();
				}
			});
			this.errorList.setCellRenderer(new ErrorListCellRenderer());
			JScrollPane errorListBox = new JScrollPane(this.errorList);
			errorListBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			errorListBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			errorListBox.getVerticalScrollBar().setBlockIncrement(50);
			errorListBox.getVerticalScrollBar().setUnitIncrement(50);
			errorListBox.setBorder(BorderFactory.createMatteBorder(3, 1, 1, 1, this.getBackground()));
			
			//	assemble the whole stuff
			this.topPanel.add(this.description);
			this.topPanel.add(this.errorTypeFilter);
			
			this.add(this.topPanel, BorderLayout.NORTH);
			this.add(errorListBox, BorderLayout.CENTER);
			
			//	make data show (also updates filters)
			this.updateErrorList();
			
			//	notify sub class
			errorTypeSelected(this.errorTypeFilter.getErrorType(), this.category, this.errorTypeFilter.getErrorCount());
		}
		
		void updateAccessories() {
			this.topPanel.removeAll();
			this.topPanel.add(this.description);
			
			//	add type selector and buttons in same row if there are no custom buttons
			if (showButtons && ((customButtons == null) || (customButtons.length == 0))) {
				JPanel functionPanel = new JPanel(new BorderLayout(), true);
				functionPanel.add(this.errorTypeFilter, BorderLayout.CENTER);
				functionPanel.add(errorButtonPanel, BorderLayout.EAST);
				this.topPanel.add(functionPanel);
			}
			else this.topPanel.add(this.errorTypeFilter);
			
			//	add severity filters
			if (severityFiltersActive)
				this.topPanel.add(showErrorsPanel);
			
			//	add buttons in separate row if there are custom buttons
			if (showButtons && ((customButtons != null) && (customButtons.length != 0)))
				this.topPanel.add(errorButtonPanel);
			
			//	show total and visible counts on severities
			this.updateErrorList();
			
			//	make the whole stuff show
			this.validate();
			this.repaint();
		}
		
		void errorAdded(DocumentError error) {
			ErrorTray et = new ErrorTray(error);
			this.errorTrays.add(et);
			Collections.sort(this.errorTrays, errorTrayOrder);
			
			//	add error, notify listeners, sort list, and update display if error showing
			boolean display = this.displayError(et.error);
			DocumentError oldSelError = this.getSelectedError();
			if (display)
				this.listErrorTrays.add(et);
			notifyErrorAdded(this.errorTypeFilter, error, display);
			notifyErrorDisplayUpdated(this.errorTypeFilter, this.listErrorTrays.size(), this.errorTrays.size());
			if (display) {
				Collections.sort(this.listErrorTrays, errorTrayOrder);
				this.errorListModel.fireContentsChanged();
			}
			
			errorCategoryChanged(this);
			if (display)
				this.notifyErrorSelectedIfChanged(oldSelError);
			else if (!this.errorTypeFilter.passes(error)) {
				this.errorTypeFilter.setErrorType(null);
				this.notifyErrorSelectedIfChanged(oldSelError);
			}
			errorTypeSelected(this.errorTypeFilter.getErrorType(), this.category, this.errorTypeFilter.getErrorCount());
		}
		private void notifyErrorSelectedIfChanged(DocumentError oldSelError) {
			DocumentError selError = this.getSelectedError();
			if ((oldSelError != null) && (oldSelError != selError))
				errorSelected(selError);
		}
		
		void errorRemoved(DocumentError error) {
			for (int t = 0; t < this.errorTrays.size(); t++) {
				ErrorTray et = ((ErrorTray) this.errorTrays.get(t));
				if (error.equals(et.error)) {
					this.removeError(t, et, false, false);
					break;
				}
			}
		}
		
		String getErrorType() {
			return this.errorTypeFilter.getErrorType();
		}
		
		DocumentError getSelectedError() {
			int si = this.errorList.getSelectedIndex();
			if (si < 0)
				return null;
			if (this.listErrorTrays.size() <= si)
				return null;
			return ((ErrorTray) this.listErrorTrays.get(this.errorList.getSelectedIndex())).error;
		}
		
		boolean setErrorType(String type) {
			return this.errorTypeFilter.setErrorType(type);
		}
		
		boolean setError(DocumentError error) {
			this.setErrorType(error.type);
			for (int e = 0; e < this.listErrorTrays.size(); e++) {
				ErrorTray et = ((ErrorTray) this.listErrorTrays.get(e));
				if (et.error.equals(error)) {
					this.errorList.setSelectedIndex(e);
					return true;
				}
			}
			return false;
		}
		
		void updateErrorTypeFilter() {
			this.updateErrorList();
			errorTypeSelected(this.errorTypeFilter.getErrorType(), this.category, this.errorTypeFilter.getErrorCount());
		}
		
		private boolean updatingErrorList = false;
		void updateErrorList() {
			if (this.updatingErrorList)
				return;
			try {
				this.updatingErrorList = true;
				this.doUpdateErrorList();
			}
			finally {
				this.updatingErrorList = false;
			}
		}
		
		private void doUpdateErrorList() {
			Collections.sort(this.errorTrays, errorTrayOrder);
			this.listErrorTrays.clear();
			notifyErrorDisplayCleared(this.errorTypeFilter);
			for (int e = 0; e < this.errorTrays.size(); e++) {
				ErrorTray et = ((ErrorTray) this.errorTrays.get(e));
				boolean display = this.displayError(et.error);
				if (display)
					this.listErrorTrays.add(et);
				notifyErrorAdded(this.errorTypeFilter, et.error, display);
			}
			notifyErrorDisplayUpdated(this.errorTypeFilter, this.listErrorTrays.size(), this.errorTrays.size());
			this.errorListModel.fireContentsChanged();
		}
		
		private boolean displayError(DocumentError error) {
			if (severityFiltersActive && !errorSeverityFilter.passes(error))
				return false;
			if (!this.errorTypeFilter.passes(error))
				return false;
			for (Iterator efit = errorFilters.iterator(); efit.hasNext();) {
				ErrorFilter ef = ((ErrorFilter) efit.next());
				if (!ef.passes(error))
					return false;
			}
			return true;
		}
		
		void selectError() {
			int ei = this.errorList.getSelectedIndex();
			while (this.listErrorTrays.size() <= ei)
				ei--;
			if (ei == -1)
				return;
			ErrorTray et = ((ErrorTray) this.listErrorTrays.get(ei));
			errorSelected(et.error);
		}
		
		void removeError(boolean falsePositive) {
			int ei = this.errorList.getSelectedIndex();
			if (ei == -1)
				return;
			if (ei < this.listErrorTrays.size())
				this.removeError(ei, ((ErrorTray) this.listErrorTrays.get(ei)), falsePositive, true);
		}
		
		void removeError(int ei, ErrorTray et, boolean falsePositive, boolean uiTriggered) {
			
			//	remove error
			this.errorTrays.remove(et);
			boolean removed = this.listErrorTrays.remove(et);
			if (removed)
				this.errorListModel.fireContentsChanged();
			
			//	remove error from protocol and notify anyone interested (only if we triggered the removal, though)
			if (uiTriggered) {
				dep.removeError(et.error);
				DocumentErrorProtocolDisplay.this.errorRemoved(et.error, falsePositive);
			}
			
			//	anything left?
			if (this.errorTrays.isEmpty()) {
				errorCategoryEmpty(this);
				return;
			}
			
			//	make sure selection is in range
			if (autoSelectNextError) {
				if (ei == this.errorTrays.size())
					this.errorList.setSelectedIndex(ei-1);
			}
			else this.errorList.clearSelection();
			
			//	decrement filter counters
			notifyErrorRemoved(this.errorTypeFilter, et.error, removed);
			notifyErrorDisplayUpdated(this.errorTypeFilter, this.listErrorTrays.size(), this.errorTrays.size());
			
			//	update tab label, and notify sub class
			errorCategoryChanged(this);
			errorTypeSelected(this.errorTypeFilter.getErrorType(), this.category, this.errorTypeFilter.getErrorCount());
			errorSelected(this.getSelectedError());
		}
		
		private class ErrorListModel extends AbstractListModel {
			public Object getElementAt(int index) {
				return ((index < listErrorTrays.size()) ? listErrorTrays.get(index) : null);
			}
			public int getSize() {
				return listErrorTrays.size();
			}
			public void fireContentsChanged() {
				super.fireContentsChanged(this, 0, this.getSize());
			}
		}
		
		private class ErrorListCellRenderer extends DefaultListCellRenderer {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component elcr = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				((JComponent) elcr).setToolTipText(((ErrorTray) value).label);
				return elcr;
			}
		}
		
		private class ErrorTypeFilter extends JComboBox implements ErrorFilter, ErrorDisplayListener {
			private ErrorType wildcardErrorType = new ErrorType(null, ALL_ERRORS_FILTER_LABEL);
			private HashMap allErrorTypes = new HashMap();
			private Vector errorTypes = new Vector();
			private ErrorTypeModel errorTypeModel = new ErrorTypeModel();
			private ErrorType activeErrorType = this.wildcardErrorType;
			ErrorTypeFilter() {
				this.setEditable(false);
				this.errorTypes.addElement(this.wildcardErrorType);
				this.setModel(this.errorTypeModel);
			}
			private class ErrorTypeModel extends DefaultComboBoxModel {
				public Object getElementAt(int index) {
					return ((index < errorTypes.size()) ? errorTypes.get(index) : null);
				}
				public int getSize() {
					return errorTypes.size();
				}
				void fireContentsChanged() {
					super.fireContentsChanged(this, 0, this.getSize());
				}
				public Object getSelectedItem() {
//					return super.getSelectedItem();
					System.out.println("ErrorTypeModel: returning selected type " + activeErrorType.type);
					return activeErrorType;
				}
				public void setSelectedItem(Object anObject) {
//					super.setSelectedItem(anObject);
					System.out.println("ErrorTypeModel: setting selected type " + ((ErrorType) anObject).type);
					activeErrorType = ((ErrorType) anObject);
					System.out.println("ErrorTypeModel: set selected type " + activeErrorType.type);
				}
			}
			String getErrorType() {
				return this.activeErrorType.type;
			}
			int getErrorCount() {
				return this.activeErrorType.matchCount;
			}
			boolean setErrorType(String type) {
				if (type == null) {
					if (this.activeErrorType.type == null)
						return true;
					if (this.errorTypes.get(0) == this.wildcardErrorType) {
						this.setSelectedIndex(0);
						return true;
					}
					return false;
				}
				for (int t = 0; t < this.errorTypes.size(); t++) {
					ErrorType cet = ((ErrorType) this.errorTypes.get(t));
					if (cet.type == null)
						continue;
					if (type.equals(cet.type)) {
						this.setSelectedItem(cet);
						return true;
					}
				}
				return false;
			}
			public boolean passes(DocumentError error) {
				return this.activeErrorType.passes(error);
			}
			public void errorDisplayCleared() {
				//System.out.println("TypeFilter: display cleared");
				this.errorTypes.clear();
				this.wildcardErrorType.matchCount = 0;
				this.wildcardErrorType.displayMatchCount = 0;
			}
			public void errorAdded(DocumentError error, boolean displaying) {
				//System.out.println("TypeFilter: adding " + error.severity + " in " + error.category + "/" + error.type);
				ErrorType et = null;
				for (int t = 0; t < this.errorTypes.size(); t++) {
					ErrorType cet = ((ErrorType) this.errorTypes.get(t));
					if (cet.type == null)
						continue;
					int c = error.type.compareTo(cet.type);
					if (c == 0) {
						et = cet;
						break;
					}
					if (c < 0) {
						et = this.getErrorType(error.category, error.type);
						this.errorTypes.add(t, et);
						break;
					}
				}
				if (et == null) {
					et = this.getErrorType(error.category, error.type);
					this.errorTypes.add(et);
				}
				et.matchCount++;
				if (displaying)
					et.displayMatchCount++;
				this.wildcardErrorType.matchCount++;
				if (displaying)
					this.wildcardErrorType.displayMatchCount++;
			}
			private ErrorType getErrorType(String category, String type) {
				ErrorType et = ((ErrorType) this.allErrorTypes.get(type));
				if (et == null) {
					et = new ErrorType(type, dep.getErrorTypeLabel(category, type));
					this.allErrorTypes.put(type, et);
				}
				else {
					et.matchCount = 0;
					et.displayMatchCount = 0;
				}
				return et;
			}
			public void errorRemoved(DocumentError error, boolean displaying) {
				//System.out.println("TypeFilter: removing " + error.severity + " in " + error.category + "/" + error.type);
				for (int t = 0; t < this.errorTypes.size(); t++) {
					ErrorType cet = ((ErrorType) this.errorTypes.get(t));
					if (cet.type == null)
						continue;
					if (cet.type.equals(error.type)) {
						cet.matchCount--;
						if (cet.matchCount == 0)
							this.errorTypes.remove(t);
						else if (displaying)
							cet.displayMatchCount--;
						break;
					}
				}
				this.wildcardErrorType.matchCount--;
				if (displaying)
					this.wildcardErrorType.displayMatchCount--;
			}
			public void errorDisplayUpdated(int displaying, int total) {
				//System.out.println("TypeFilter: finishing display update with " + displaying + " errors displaying of " + total);
				
				//	make sure we have at least "all" filter ...
				if (this.errorTypes.isEmpty())
					this.errorTypes.add(this.wildcardErrorType);
				
				//	... and have "all" filter in presence of multiple error types ...
				else if ((this.errorTypes.size() != 1) && (this.errorTypes.get(0) != this.wildcardErrorType))
					this.errorTypes.add(0, this.wildcardErrorType);
				
				//	... but not with single error type
				else if ((this.errorTypes.get(0) == this.wildcardErrorType) && (this.errorTypes.size() == 2))
					this.errorTypes.remove(0);
				
				//	switch to "all" filter on removal of last error of selected type ...
				//	... or to last remaining error type
				if (this.activeErrorType.matchCount <= 0)
					this.setSelectedItem((ErrorType) this.errorTypes.get(0));
				else if (this.errorTypes.size() == 1)
					this.setSelectedItem((ErrorType) this.errorTypes.get(0));
				
				//	make changes show
				this.errorTypeModel.fireContentsChanged();
				this.revalidate();
				this.repaint();
			}
			
			private class ErrorType implements ErrorFilter {
				final String type;
				final String label;
				int matchCount = 0;
				int displayMatchCount = 0;
				ErrorType(String type, String label) {
					this.type = type;
					this.label = label;
				}
				public boolean passes(DocumentError error) {
					return ((this.type == null) ? true : this.type.equals(error.type));
				}
				public String toString() {
//					- for _active_ filters, show how many of how many overall errors match, and how many are actually showing
//					- for  _inactive_ filters, show how many of how many overall errors would match, and how many would pass the other filters
					if (activeErrorType == this)
						return (this.label + " (" + this.displayMatchCount + "/" + this.matchCount + " of " + wildcardErrorType.matchCount + ")");
					else return (this.label + " (" + this.matchCount + " of " + wildcardErrorType.matchCount + ")");
				}
				public boolean equals(Object obj) {
					ErrorType et = ((ErrorType) obj);
					return ((this.type == null) ? (et.type == null) : this.type.equals(et.type));
				}
			}
		}
	}
	
	private class ErrorTray {
		final DocumentError error;
		final String label;
		ErrorTray(DocumentError error) {
			this.error = error;
			this.label = getErrorLabel(this.error);
		}
		public String toString() {
			String str = (StringUtils.capitalize(this.error.severity) + ": " + this.error.description);
			return ((highlightLabeledErrors && (this.label != null)) ? ("<HTML><B>" + str + "</B></HTML>") : str);
		}
	}
	
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		DocumentErrorProtocol dep = new DocumentErrorProtocol() {
			//	TODO provide this basic counting functionality in AbstractDocumentErrorProtocol in goldengate-quality-control
			//	TODO and maybe then some, e.g. category and type indexing, and also counting for individual severities
			//	TODO plus false positive keeping and ID based duplicate prevention, if only based upon abstract getErrorId() method
			private ArrayList errors = new ArrayList();
			private CountingSet errorCounts = new CountingSet(new TreeMap(String.CASE_INSENSITIVE_ORDER));
			public int getErrorCount(String category, String type) {
				return this.errorCounts.getCount(category + "." + type);
			}
			public int getErrorSeverityCount(String category, String type, String severity) {
				return this.errorCounts.getCount(category + "." + type + ":" + severity);
			}
			public DocumentError[] getErrors(String category, String type) {
				if (category == null)
					return this.getErrors();
				if (type == null)
					return this.getErrors(category);
				ArrayList errors = new ArrayList();
				for (int e = 0; e < this.errors.size(); e++) {
					if (category.equals(((DocumentError) this.errors.get(e)).category))
						errors.add(this.errors.get(e));
				}
				return ((DocumentError[]) errors.toArray(new DocumentError[errors.size()]));
			}
			public int getErrorCount(String category) {
				return this.errorCounts.getCount(category);
			}
			public int getErrorSeverityCount(String category, String severity) {
				return this.errorCounts.getCount(category + ":" + severity);
			}
			public DocumentError[] getErrors(String category) {
				if (category == null)
					return this.getErrors();
				ArrayList errors = new ArrayList();
				for (int e = 0; e < this.errors.size(); e++) {
					if (category.equals(((DocumentError) this.errors.get(e)).category))
						errors.add(this.errors.get(e));
				}
				return ((DocumentError[]) errors.toArray(new DocumentError[errors.size()]));
			}
			public int getErrorCount() {
				return this.errors.size();
			}
			public int getErrorSeverityCount(String severity) {
				return this.errorCounts.getCount(severity);
			}
			public DocumentError[] getErrors() {
				return ((DocumentError[]) this.errors.toArray(new DocumentError[this.errors.size()]));
			}
			public Attributed findErrorSubject(Attributed doc, String[] data) {
				return null;
			}
			public void addError(String source, Attributed subject, Attributed parent, String category, String type, String description, String severity, boolean falsePositive) {
				this.errors.add(new DocumentError(source, subject, category, type, description, severity) {});
				this.errorCounts.add(category);
				this.errorCounts.add(category + ":" + severity);
				this.errorCounts.add(category + "." + type);
				this.errorCounts.add(category + "." + type + ":" + severity);
			}
			public void removeError(DocumentError error) {
				this.errors.remove(error);
				this.errorCounts.remove(error.category);
				this.errorCounts.remove(error.category + ":" + error.severity);
				this.errorCounts.remove(error.category + "." + error.type);
				this.errorCounts.remove(error.category + "." + error.type + ":" + error.severity);
			}
			public boolean isFalsePositive(DocumentError error) {
				return false;
			}
			public boolean markFalsePositive(DocumentError error) {
				return false;
			}
			public boolean unmarkFalsePositive(DocumentError error) {
				return false;
			}
			public DocumentError[] getFalsePositives() {
				return null;
			}
			public Comparator getErrorComparator() {
				return null;
			}
		};
		dep.addErrorCategory("C1", "C1-Label", "C1-Description text");
		dep.addErrorType("C1", "C1T1", "C1T1-Label", "C1T1-Description text");
		dep.addError("test", null, null, "C1", "C1T1", "There is an error at 1.", DocumentError.SEVERITY_MAJOR);
		dep.addError("test", null, null, "C1", "C1T1", "There is an error at 2.", DocumentError.SEVERITY_MINOR);
		dep.addErrorType("C1", "C1T2", "C1T2-Label", "C1T2-Description text");
		dep.addError("test", null, null, "C1", "C1T2", "There is an error at 3.", DocumentError.SEVERITY_MAJOR);
		dep.addError("test", null, null, "C1", "C1T2", "There is an error at 4.", DocumentError.SEVERITY_MAJOR);
		
		dep.addErrorCategory("C2", "C2-Label", "C2-Description text");
		dep.addErrorType("C2", "C2T1", "C2T1-Label", "C2T1-Description text");
		dep.addError("test", null, null, "C2", "C2T1", "There is an error at 5.", DocumentError.SEVERITY_MAJOR);
		dep.addError("test", null, null, "C2", "C2T1", "There is an error at 6.", DocumentError.SEVERITY_MAJOR);
		dep.addErrorType("C2", "C2T2", "C2T2-Label", "C2T2-Description text");
		dep.addError("test", null, null, "C2", "C2T2", "There is an error at 7.", DocumentError.SEVERITY_MAJOR);
		dep.addError("test", null, null, "C2", "C2T2", "There is an error at 8.", DocumentError.SEVERITY_MINOR);
		
		DocumentErrorProtocolDisplay epd = new DocumentErrorProtocolDisplay(dep) {
			protected void errorSelected(DocumentError error) {
				System.out.println("Selected error " + error.description);
			}
			protected void errorRemoved(DocumentError error, boolean falsePositive) {
				System.out.println("Removed error " + error.description);
			}
		};
		JButton[] cbs = {
			new JButton("Test"),
			new JButton("Test"),
			new JButton("Test")
		};
		for (int b = 0; b < cbs.length; b++)
			cbs[b].setBorder(BorderFactory.createRaisedBevelBorder());
		epd.setCustomButtons(cbs);
		JOptionPane.showMessageDialog(null, epd, "Error Test", JOptionPane.PLAIN_MESSAGE);
	}
}
