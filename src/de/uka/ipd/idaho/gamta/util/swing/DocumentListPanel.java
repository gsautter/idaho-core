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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList.AttributeSummary;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListBuffer;
import de.uka.ipd.idaho.stringUtils.StringVector;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * Customizable widget for displaying document lists.
 * 
 * @author sautter
 */
public class DocumentListPanel extends JPanel implements LiteratureConstants {
	private static final String[] listSortKeys = {DOCUMENT_NAME_ATTRIBUTE};
	
	private Properties listFieldLabels = new Properties();
	private StringVector listFieldOrder = new StringVector();
	private StringVector listFields = new StringVector();
	
	/**
	 * Set the fields to display. If this property is null or empty, all fields
	 * will be displayed.
	 * @param listFields the list of fields to display in the list
	 */
	public void setListFields(StringVector listFields) {
		this.listFields.clear();
		if (listFields != null)
			this.listFields.addContentIgnoreDuplicates(listFields);
	}
	
	/**
	 * Set the fields to display first, in the order they should be displayed.
	 * All other fields appear in the order they are in the backing document
	 * list.
	 * @param listFieldOrder the field to display first
	 */
	public void setListFieldOrder(StringVector listFieldOrder) {
		this.listFieldOrder.clear();
		if (listFieldOrder != null)
			this.listFieldOrder.addContentIgnoreDuplicates(listFieldOrder);
	}
	
	/**
	 * Get the display label (nice name) of a given list field.
	 * @param listFieldName the field name to get the label for
	 * @return the label for the argument field name
	 */
	public String getListFieldLabel(String fieldName) {
		return this.listFieldLabels.getProperty(fieldName);
	}
	
	/**
	 * Set the display label (nice name) for a given list field.
	 * @param fieldName the field name to set the label for
	 * @param fieldLabel the list field label to set
	 */
	public void setListFieldLabel(String fieldName, String fieldLabel) {
		if (fieldLabel == null)
			this.listFieldLabels.remove(fieldName);
		else this.listFieldLabels.setProperty(fieldName, fieldLabel);
	}
	
	/**
	 * Gets the number of filter fields if the filter panel is showing, returns
	 * 0 otherwise.
	 * @return the number of filter fields
	 */
	public int getFilterFieldCount() {
		return (this.showFilterPanel ? this.filterPanel.filters.length : 0);
	}
	
	/**
	 * Test whether or not the filter panel is showing.
	 * @return true if the filter panel is showing
	 */
	public boolean isShowingFilterPanel() {
		return this.showFilterPanel;
	}
	
	/**
	 * Toggle whether or not to show the filter panel above the document list.
	 * @param showFilterPanel show the filter panel?
	 */
	public void setShowFilterPanel(boolean showFilterPanel) {
		if (showFilterPanel == this.showFilterPanel)
			return;
		this.showFilterPanel = showFilterPanel;
		if (this.showFilterPanel)
			this.add(this.filterPanel, BorderLayout.NORTH);
		else this.remove(this.filterPanel);
		this.validate();
		this.repaint();
	}
	
	/**
	 * Retrieve the marker added to the header of the sort column if the
	 * document list is sorted in ascending order.
	 * @return the ascending sort marker
	 */
	public char getAscendingSortMarker() {
		return this.ascendingSortMarker;
	}
	
	/**
	 * Retrieve the marker added to the header of the sort column if the
	 * document list is sorted in descending order.
	 * @return the descending sort marker
	 */
	public char getDescendingSortMarker() {
		return this.descendingSortMarker;
	}
	
	/**
	 * Set the markers added to the header of the sort column if the document
	 * list is sorted in ascending or descending order, respectively.
	 * @param ascending the marker for ascending sort order
	 * @param descending the marker for descending sort order
	 */
	public void setSortMarkers(char ascending, char descending) {
		this.ascendingSortMarker = ascending;
		this.descendingSortMarker = descending;
	}
	
	/**
	 * Inject a new document list to display.
	 * @param docList the document list to display
	 */
	public void setDocumentList(DocumentListBuffer docList) {
		this.docList = docList;
		this.updateListData(null);
	}
	
	/**
	 * Set the selected document. If the argument document data object is null
	 * or not contained in the underlying document list or not visible under
	 * the current filter, this method clears the selection.
	 * @param docData the document data object to set the selection to
	 */
	public void setSelectedDocument(StringTupel docData) {
		if (docData == null) {
			this.docTable.clearSelection();
			return;
		}
		for (int d = 0; d < this.listData.length; d++)
			if (this.listData[d].data == docData) {
				this.setSelectedDocument(d);
				return;
			}
		this.setSelectedDocument(-1);
	}
	
	/**
	 * Set the selected document by its index in the currently showing list. If
	 * the argument index is -1 or larger than the currently showing document
	 * list, this method clears the selection.
	 * @param index the index of the document data object to set the selection
	 *            to
	 */
	public void setSelectedDocument(int index) {
		if (index < 0)
			this.docTable.clearSelection();
		else if (this.listData.length <= index)
			this.docTable.clearSelection();
		else {
			this.docTable.setRowSelectionInterval(index, index);
			this.docTable.scrollRectToVisible(this.docTable.getCellRect(index, 0, true));
		}
	}
	
	/**
	 * Notify the document list panel that the displayed document list was
	 * modified externally and requires a refresh.
	 */
	public void refreshDocumentList() {
		this.updateListData(this.getDocumentFilter());
	}
	
	/**
	 * Retrieve the currently selected document filter.
	 * @return the currently selected document filter
	 */
	public DocumentFilter getDocumentFilter() {
		return this.filterPanel.getFilter();
	}
	
	/**
	 * Retrieve the data of the currently selected document.
	 * @return the data of the currently selected document
	 */
	public StringTupel getSelectedDocument() {
		int row = this.docTable.getSelectedRow();
		return ((row == -1) ? null : this.listData[row].data);
	}
	
	/**
	 * Retrieve the row index of the currently selected document. Note that the
	 * values returned by this method are relative to the currently displaying
	 * documents. This means that the same document can appear at different
	 * indexes if the filter or the sort order changes.
	 * @return the row index of the currently selected document
	 */
	public int getSelectedIndex() {
		return this.docTable.getSelectedRow();
	}
	
	/**
	 * Retrieve the total number of documents in the backing buffer.
	 * @return the total number of documents in the backing buffer
	 */
	public int getDocumentCount() {
		return this.docList.size();
	}
	
	/**
	 * Retrieve the number of documents currently showing in the table.
	 * @return the number of documents currently showing in the table
	 */
	public int getVisibleDocumentCount() {
		return this.listData.length;
	}
	
	final String getFieldLabel(String fieldName) {
		String fieldLabel = this.getListFieldLabel(fieldName);
		if (fieldLabel != null)
			return fieldLabel;
		
		if (fieldName.length() < 2)
			return fieldName;
		
		StringVector parts = new StringVector();
		fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		int c = 1;
		while (c < fieldName.length()) {
			if (Character.isUpperCase(fieldName.charAt(c))) {
				parts.addElement(fieldName.substring(0, c));
				fieldName = fieldName.substring(c);
				c = 1;
			} else c++;
		}
		if (fieldName.length() != 0)
			parts.addElement(fieldName);
		
		for (int p = 0; p < (parts.size() - 1);) {
			String part1 = parts.get(p);
			String part2 = parts.get(p + 1);
			if ((part2.length() == 1) && Character.isUpperCase(part1.charAt(part1.length() - 1))) {
				part1 += part2;
				parts.setElementAt(part1, p);
				parts.remove(p+1);
			}
			else p++;
		}
		
		return parts.concatStrings(" ");
	}
	
	private class DocumentFilterPanel extends JPanel {
		
		private abstract class Filter {
			final String fieldName;
			Filter(String fieldName) {
				this.fieldName = fieldName;
			}
			abstract JComponent getOperatorSelector();
			abstract String getOperator();
			abstract JComponent getValueInputField();
			abstract String[] getFilterValues() throws RuntimeException;
		}
		
		private class StringFilter extends Filter {
			private String[] suggestionLabels;
			private Properties suggestionMappings;
			private boolean editable;
			private JTextField valueInput;
			private JComboBox valueSelector;
			StringFilter(String fieldName, AttributeSummary suggestions, boolean editable) {
				super(fieldName);
				if (suggestions == null) 
					this.editable = true;
				else {
					this.editable = editable;
					this.suggestionLabels = new String[suggestions.elementCount()];
					this.suggestionMappings = new Properties();
					for (Iterator sit = suggestions.iterator(); sit.hasNext();) {
						String suggestion = ((String) sit.next());
						if (this.editable) {
							suggestion = suggestion.replaceAll("\\s", "+");
							this.suggestionLabels[this.suggestionMappings.size()] = suggestion;
							this.suggestionMappings.setProperty(suggestion, suggestion);
						}
						else {
							String suggestionLabel = (suggestion + " (" + suggestions.getCount(suggestion) + ")");
							this.suggestionLabels[this.suggestionMappings.size()] = suggestionLabel;
							this.suggestionMappings.setProperty(suggestionLabel, suggestion);
						}
					}
				}
			}
			JComponent getOperatorSelector() {
				return new JLabel("contains (use '+' for spaces)", JLabel.CENTER);
			}
			String getOperator() {
				return null;
			}
			JComponent getValueInputField() {
				if (this.suggestionLabels == null) {
					this.valueInput = new JTextField();
					this.valueInput.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							filterDocumentList();
						}
					});
					return this.valueInput;
				}
				else {
					this.valueSelector = new JComboBox(this.suggestionLabels);
					this.valueSelector.insertItemAt("<do not filter>", 0);
					this.valueSelector.setSelectedItem("<do not filter>");
					this.valueSelector.setEditable(this.editable);
					this.valueSelector.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							filterDocumentList();
						}
					});
					return this.valueSelector;
				}
			}
			String[] getFilterValues() throws RuntimeException {
				String filterValue;
				if (this.suggestionLabels == null)
					filterValue = this.valueInput.getText().trim();
				else {
					filterValue = ((String) this.valueSelector.getSelectedItem()).trim();
					filterValue = this.suggestionMappings.getProperty(filterValue, filterValue);
				}
				
				if ((filterValue.length() == 0) || "<do not filter>".equals(filterValue))
					return null;
				
				if (this.editable) {
					String[] filterValues = filterValue.split("\\s++");
					for (int v = 0; v < filterValues.length; v++)
						filterValues[v] = filterValues[v].replaceAll("\\+", " ").trim();
					return filterValues;
				}
				else {
					String[] filterValues = {filterValue};
					return filterValues;
				}
			}
		}
		
		private class NumberFilter extends Filter {
			private String[] operatorLabels;
			private Properties operatorMappings;
			private JComboBox operatorSelector;
			private String[] suggestionLabels;
			private Properties suggestionMappings;
			private boolean editable;
			private JTextField valueInput;
			private JComboBox valueSelector;
			NumberFilter(String fieldName, AttributeSummary suggestions, boolean editable, boolean isTime) {
				super(fieldName);
				
				this.operatorLabels = new String[DocumentList.numericOperators.size()];
				this.operatorMappings = new Properties();
				for (Iterator oit = DocumentList.numericOperators.iterator(); oit.hasNext();) {
					String operator = ((String) oit.next());
					String operatorLabel;
					if (">".equals(operator))
						operatorLabel = (isTime ? "after" : "more than");
					else if (">=".equals(operator))
						operatorLabel = (isTime ? "the earliest in" : "at least");
					else if ("=".equals(operator))
						operatorLabel = "exactly in";
					else if ("<=".equals(operator))
						operatorLabel = (isTime ? "the latest in" : "at most");
					else if ("<".equals(operator))
						operatorLabel = (isTime ? "before" : "less than");
					else continue;
					this.operatorLabels[this.operatorMappings.size()] = operatorLabel;
					this.operatorMappings.setProperty(operatorLabel, operator);
				}
				
				if (suggestions == null)
					this.editable = true;
				else {
					this.editable = editable;
					this.suggestionLabels = new String[suggestions.elementCount()];
					this.suggestionMappings = new Properties();
					for (Iterator sit = suggestions.iterator(); sit.hasNext();) {
						String suggestion = ((String) sit.next());
						if (this.editable) {
							this.suggestionLabels[this.suggestionMappings.size()] = suggestion;
							this.suggestionMappings.setProperty(suggestion, suggestion);
						}
						else {
							String suggestionLabel = (suggestion + " (" + suggestions.getCount(suggestion) + ")");
							this.suggestionLabels[this.suggestionMappings.size()] = suggestionLabel;
							this.suggestionMappings.setProperty(suggestionLabel, suggestion);
						}
					}
				}
			}
			JComponent getOperatorSelector() {
				this.operatorSelector = new JComboBox(this.operatorLabels);
				this.operatorSelector.setEditable(false);
				return this.operatorSelector;
			}
			String getOperator() {
				String operator = ((String) this.operatorSelector.getSelectedItem()).trim();
				return this.operatorMappings.getProperty(operator, operator);
			}
			JComponent getValueInputField() {
				if (this.suggestionLabels == null) {
					this.valueInput = new JTextField();
					this.valueInput.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							filterDocumentList();
						}
					});
					return this.valueInput;
				}
				else {
					this.valueSelector = new JComboBox(this.suggestionLabels);
					this.valueSelector.insertItemAt("<do not filter>", 0);
					this.valueSelector.setSelectedItem("<do not filter>");
					this.valueSelector.setEditable(this.editable);
					this.valueSelector.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							filterDocumentList();
						}
					});
					return this.valueSelector;
				}
			}
			String[] getFilterValues() throws RuntimeException {
				String filterValue;
				if (this.suggestionLabels == null)
					filterValue = this.valueInput.getText().trim();
				else {
					filterValue = ((String) this.valueSelector.getSelectedItem()).trim();
					filterValue = this.suggestionMappings.getProperty(filterValue, filterValue);
				}
				
				if ((filterValue.length() == 0) || "<do not filter>".equals(filterValue))
					return null;
				
				try {
					Long.parseLong(filterValue);
				}
				catch (NumberFormatException nfe) {
					throw new RuntimeException("'" + filterValue + "' is not a valid value for " + getFieldLabel(this.fieldName) + ".");
				}
				
				String[] filterValues = {filterValue};
				return filterValues;
			}
		}
		
		private class TimeFilter extends Filter {
			private String[] operatorLabels;
			private Properties operatorMappings;
			private JComboBox operatorSelector;
			private JComboBox valueSelector;
			TimeFilter(String fieldName) {
				super(fieldName);
				
				this.operatorLabels = new String[DocumentList.numericOperators.size()];
				this.operatorMappings = new Properties();
				for (Iterator oit = DocumentList.numericOperators.iterator(); oit.hasNext();) {
					String operator = ((String) oit.next());
					String operatorLabel;
					if (">".equals(operator))
						operatorLabel = "less than";
					else if (">=".equals(operator))
						operatorLabel = "at most";
					else if ("=".equals(operator))
						operatorLabel = "exactly";
					else if ("<=".equals(operator))
						operatorLabel = "at least";
					else if ("<".equals(operator))
						operatorLabel = "more than";
					else continue;
					this.operatorLabels[this.operatorMappings.size()] = operatorLabel;
					this.operatorMappings.setProperty(operatorLabel, operator);
				}
			}
			JComponent getOperatorSelector() {
				this.operatorSelector = new JComboBox(this.operatorLabels);
				this.operatorSelector.setEditable(false);
				return this.operatorSelector;
			}
			String getOperator() {
				String operator = ((String) this.operatorSelector.getSelectedItem()).trim();
				return this.operatorMappings.getProperty(operator, operator);
			}
			JComponent getValueInputField() {
				this.valueSelector = new JComboBox();
				this.valueSelector.addItem("<do not filter>");
				this.valueSelector.addItem("one hour ago");
				this.valueSelector.addItem("one day ago");
				this.valueSelector.addItem("one week ago");
				this.valueSelector.addItem("one month ago");
				this.valueSelector.addItem("three months ago");
				this.valueSelector.addItem("one year ago");
				this.valueSelector.setEditable(false);
				return this.valueSelector;
			}
			String[] getFilterValues() throws RuntimeException {
				String filterValue = ((String) this.valueSelector.getSelectedItem()).trim();
				if ("one hour ago".equals(filterValue))
					filterValue = ("" + (System.currentTimeMillis() - ((long) (1 * 1 * 60 * 60) * 1000)));
				else if ("one day ago".equals(filterValue))
					filterValue = ("" + (System.currentTimeMillis() - ((long) (1 * 24 * 60 * 60) * 1000)));
				else if ("one week ago".equals(filterValue))
					filterValue = ("" + (System.currentTimeMillis() - ((long) (7 * 24 * 60 * 60) * 1000)));
				else if ("one month ago".equals(filterValue))
					filterValue = ("" + (System.currentTimeMillis() - ((long) (30 * 24 * 60 * 60) * 1000)));
				else if ("three months ago".equals(filterValue))
					filterValue = ("" + (System.currentTimeMillis() - ((long) (90 * 24 * 60 * 60) * 1000)));
				else if ("one year ago".equals(filterValue))
					filterValue = ("" + (System.currentTimeMillis() - ((long) (365 * 24 * 60 * 60) * 1000)));
				else return null;
				String[] filterValues = {filterValue};
				return filterValues;
			}
		}
		
		Filter[] filters;
		
		DocumentFilterPanel(DocumentListBuffer docList) {
			super(new GridBagLayout(), true);
			
			ArrayList filterList = new ArrayList();
			for (int f = 0; f < docList.listFieldNames.length; f++) {
				if (!docList.isFilterable(docList.listFieldNames[f]))
					continue;
				
				AttributeSummary das = docList.getListFieldValues(docList.listFieldNames[f]);
				Filter filter;
				if (docList.isNumeric(docList.listFieldNames[f])) {
					if (isUtcTimeField(docList.listFieldNames[f]))
						filter = new TimeFilter(docList.listFieldNames[f]);
					else filter = new NumberFilter(docList.listFieldNames[f], das, true, DOCUMENT_DATE_ATTRIBUTE.equals(docList.listFieldNames[f]));
				}
				else filter = new StringFilter(docList.listFieldNames[f], das, !docList.listFieldNames[f].endsWith("User"));
				filterList.add(filter);
			}
			this.filters = ((Filter[]) filterList.toArray(new Filter[filterList.size()]));
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets.top = 3;
			gbc.insets.bottom = 3;
			gbc.insets.left = 3;
			gbc.insets.right = 3;
			gbc.weighty = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			for (int f = 0; f < this.filters.length; f++) {
				gbc.gridx = 0;
				gbc.weightx = 0;
				this.add(new JLabel(getFieldLabel(this.filters[f].fieldName), JLabel.LEFT), gbc.clone());
				gbc.gridx = 1;
				gbc.weightx = 0;
				this.add(this.filters[f].getOperatorSelector(), gbc.clone());
				gbc.gridx = 2;
				gbc.weightx = 1;
				this.add(this.filters[f].getValueInputField(), gbc.clone());
				gbc.gridy++;
			}
		}
		
		DocumentFilter getFilter() {
			final LinkedList filterList = new LinkedList();
			for (int f = 0; f < this.filters.length; f++) {
				String[] filterValues = this.filters[f].getFilterValues();
				if ((filterValues == null) || (filterValues.length == 0))
					continue;
				
				System.out.println(this.filters[f].fieldName + " filter value is " + this.flattenArray(filterValues));
				
				if (docList.isNumeric(this.filters[f].fieldName)) {
					final long filterValue = Long.parseLong(filterValues[0]);
					final String operator = this.filters[f].getOperator();
					if ((operator != null) && DocumentList.numericOperators.contains(operator))
						filterList.addFirst(new DocumentFilter(this.filters[f].fieldName) {
							public boolean isMatch(StringTupel docData) {
								String dataValueString = docData.getValue(this.fieldName);
								if (dataValueString == null)
									return false;
								long dataValue;
								try {
									dataValue = Long.parseLong(dataValueString);
								}
								catch (NumberFormatException nfe) {
									return false;
								}
								if (">".equals(operator))
									return (dataValue > filterValue);
								else if (">=".equals(operator))
									return (dataValue >= filterValue);
								else if ("=".equals(operator))
									return (dataValue == filterValue);
								else if ("<=".equals(operator))
									return (dataValue <= filterValue);
								else if ("<".equals(operator))
									return (dataValue < filterValue);
								else return true;
							}
							public Properties toProperties() {
								Properties props = new Properties();
								props.setProperty(this.fieldName, ("" + filterValue));
								props.setProperty((this.fieldName + "Operator"), operator);
								return props;
							}
						});
				}
				else {
					final String[] filterStrings = new String[filterValues.length];
					for (int v = 0; v < filterValues.length; v++)
						filterStrings[v] = filterValues[v].replaceAll("\\s++", " ").toLowerCase();
				
					for (int s = 0; s < filterStrings.length; s++) {
						while (filterStrings[s].startsWith("%"))
							filterStrings[s] = filterStrings[s].substring(1);
						while (filterStrings[s].endsWith("%"))
							filterStrings[s] = filterStrings[s].substring(0, (filterStrings[s].length() - 1));
					}
					filterList.addLast(new DocumentFilter(this.filters[f].fieldName) {
						public boolean isMatch(StringTupel docData) {
							String dataValueString = docData.getValue(this.fieldName);
							if (dataValueString == null)
								return false;
							dataValueString = dataValueString.replaceAll("\\s++", " ").toLowerCase();
							for (int f = 0; f < filterStrings.length; f++) {
								if (dataValueString.indexOf(filterStrings[f]) != -1)
									return true;
							}
							return false;
						}
						public Properties toProperties() {
							Properties props = new Properties();
							String filterValue = flattenArray(filterStrings);
							if (filterValue != null)
								props.setProperty(this.fieldName, filterValue);
							return props;
						}
					});
				}
			}
			
			return (filterList.isEmpty() ? null : new DocumentFilter(null) {
				public boolean isMatch(StringTupel docData) {
					for (Iterator fit = filterList.iterator(); fit.hasNext();) {
						if (!((DocumentFilter) fit.next()).isMatch(docData))
							return false;
					}
					return true;
				}
				public Properties toProperties() {
					Properties props = new Properties();
					for (Iterator fit = filterList.iterator(); fit.hasNext();)
						props.putAll(((DocumentFilter) fit.next()).toProperties());
					return props;
				}
			});
		}
		
		private String flattenArray(String[] filterValues) {
			if ((filterValues == null) || (filterValues.length == 0))
				return null;
			if (filterValues.length == 1)
				return filterValues[0];
			StringBuffer filterValue = new StringBuffer(filterValues[0]);
			for (int v = 1; v < filterValues.length; v++)
				filterValue.append("\r\n" + filterValues[v]);
			return filterValue.toString();
		}
	}
	
	/**
	 * A filter to apply to the entries of a document list.
	 * 
	 * @author sautter
	 */
	public abstract class DocumentFilter {
		String fieldName;
		DocumentFilter(String fieldName) {
			this.fieldName = fieldName;
		}
		
		/**
		 * Test whether or not a document list entry matches the filter.
		 * @param docData the document data to check
		 * @return true if the argument document data matches the filter
		 */
		public abstract boolean isMatch(StringTupel docData);
		
		/**
		 * Convert the document list filter into a series of key/value pairs,
		 * e.g. for transfer.
		 * @return a Properties object representing the filter
		 */
		public abstract Properties toProperties();
	}
	
	private class DocumentTableModel implements TableModel {
		private String[] fieldNames;
		private StringTupelTray[] listData;
		
		DocumentTableModel(String[] fieldNames, StringTupelTray[] listData) {
			this.fieldNames = fieldNames;
			this.listData = listData;
		}
		
		private ArrayList listeners = new ArrayList();
		public void addTableModelListener(TableModelListener tml) {
			this.listeners.add(tml);
		}
		public void removeTableModelListener(TableModelListener tml) {
			this.listeners.remove(tml);
		}
		
		void update() {
			for (int l = 0; l < this.listeners.size(); l++)
				((TableModelListener) this.listeners.get(l)).tableChanged(new TableModelEvent(this));
		}
		
		String getFieldName(int columnIndex) {
			return this.fieldNames[columnIndex];
		}
		
		public String getColumnName(int columnIndex) {
			return getFieldLabel(this.fieldNames[columnIndex]);
		}
		public Class getColumnClass(int columnIndex) {
			return String.class;
		}
		public int getColumnCount() {
			return this.fieldNames.length;
		}
		public int getRowCount() {
			return this.listData.length;
		}
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			String fieldName = this.fieldNames[columnIndex];
			StringTupel rowData = this.listData[rowIndex].data;
			return getDisplayValue(fieldName, rowData.getValue(this.fieldNames[columnIndex], ""), rowData);
		}
	}
	
	private static final String DEFAULT_TIMESTAMP_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final DateFormat TIMESTAMP_DATE_FORMAT = new SimpleDateFormat(DEFAULT_TIMESTAMP_DATE_FORMAT);
	
	private class StringTupelTray {
		final StringTupel data;
		Object[] sortKey = new Object[0];
		StringTupelTray(StringTupel data) {
			this.data = data;
		}
		void updateSortKey(StringVector sortFields) {
			this.sortKey = new Object[sortFields.size()];
			for (int f = 0; f < sortFields.size(); f++)
				this.sortKey[f] = this.data.getValue(sortFields.get(f), "");
		}
	}
	
	private DocumentListBuffer docList;
	private StringTupelTray[] listData;
	
	private JTable docTable = new JTable();
	private DocumentTableModel docTableModel;
	
	private String mainSortField = DOCUMENT_NAME_ATTRIBUTE;
	private boolean mainSortDescending = false;
	private char ascendingSortMarker = '\u25B2';
	private char descendingSortMarker = '\u25BC';
	//	TODOne try single arrows (U+2191 and U+2193) ==> too fine to see well
	//	TODOne try double arrows (U+21D1 and U+21D3) ==> tips protrude too little to left and right of shaft
	//	TODOne try triangles (U+25B2 and U+25BC) ==> looking good as default
	
	private DocumentFilterPanel filterPanel;
	private boolean showFilterPanel;
	
	/**
	 * Constructor
	 * @param docList the document list to display
	 * @param showFilterPanel show the filter panel?
	 */
	public DocumentListPanel(DocumentListBuffer docList, boolean showFilterPanel) {
		super(new BorderLayout(), true);
		this.docList = docList;
		
		this.filterPanel = new DocumentFilterPanel(docList);
		this.showFilterPanel = showFilterPanel;
		
		final JTableHeader header = this.docTable.getTableHeader();
		if (header != null) {
			header.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (docTableModel == null)
						return;
	                int column = header.columnAtPoint(me.getPoint());
	                if (column != -1)
	                	sortList(docTableModel.getFieldName(column), true);
				}
			});
			header.setDefaultRenderer(new TableHeaderRenderer(header.getDefaultRenderer()));
		}
		
		this.docTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.docTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				int row = docTable.getSelectedRow();
				if (row == -1)
					return;
				if (me.getClickCount() > 1)
					documentSelected(listData[row].data, true);
				else {
					if (me.getButton() == MouseEvent.BUTTON1)
						documentSelected(listData[row].data, false);
					else showContextMenu(row, me);
				}
			}
		});
		
		JScrollPane docTableBox = new JScrollPane(this.docTable);
		docTableBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		if (this.showFilterPanel)
			this.add(this.filterPanel, BorderLayout.NORTH);
		this.add(docTableBox, BorderLayout.CENTER);
		
		this.setSize(new Dimension(800, 800));
		
		this.updateListData(null);
	}
	
	private class TableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer tcr;
		TableHeaderRenderer(TableCellRenderer original) {
			this.tcr = original;
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			boolean mainSortFieldHeader = mainSortField.equals(docTableModel.getFieldName(column));
			if (mainSortFieldHeader)
				value = (value + " " + (mainSortDescending ? descendingSortMarker : ascendingSortMarker));
			Component comp = this.tcr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (mainSortFieldHeader)
				comp.setFont(comp.getFont().deriveFont(Font.BOLD));
			return comp;
		}
	}
	
	private void updateListData(DocumentFilter filter) {
		
		//	cache current selection (if any)
		StringTupel selDocData = null;
		if (this.docTable.getSelectedRow() != -1)
			selDocData = this.listData[this.docTable.getSelectedRow()].data;
		
		//	update display data
		if (filter == null) {
			this.listData = new StringTupelTray[this.docList.size()];
			for (int d = 0; d < this.docList.size(); d++)
				this.listData[d] = new StringTupelTray(this.docList.get(d));
		}
		else {
			ArrayList listDataList = new ArrayList();
			for (int d = 0; d < this.docList.size(); d++) {
				StringTupel docData = this.docList.get(d);
				if (filter.isMatch(docData))
					listDataList.add(docData);
			}
			this.listData = new StringTupelTray[listDataList.size()];
			for (int d = 0; d < listDataList.size(); d++)
				this.listData[d] = new StringTupelTray((StringTupel) listDataList.get(d));
		}
		
		//	update fields
		StringVector fieldNames = new StringVector();
		fieldNames.addContent(this.listFieldOrder);
		for (int f = 0; f < this.docList.listFieldNames.length; f++) {
			String fieldName = this.docList.listFieldNames[f];
			if (this.displayField(fieldName, false))
				fieldNames.addElementIgnoreDuplicates(fieldName);
		}
		
		//	assess fields
		for (int f = 0; f < fieldNames.size(); f++) {
			String fieldName = fieldNames.get(f);
			if ((this.listFields.size() != 0) && !this.listFields.contains(fieldName)) {
				fieldNames.remove(f--);
				continue;
			}
			if (this.displayField(fieldName, true))
				continue;
			boolean fieldEmpty = true;
			for (int d = 0; d < this.listData.length; d++)
				if (!"".equals(this.listData[d].data.getValue(fieldName, ""))) {
					fieldEmpty = false;
					break;
				}
			if (fieldEmpty)
				fieldNames.remove(f--);
		}
		
		//	update list table
		this.docTableModel = new DocumentTableModel(fieldNames.toStringArray(), this.listData);
		this.docTable.setColumnModel(new DefaultTableColumnModel() {
			public TableColumn getColumn(int columnIndex) {
				TableColumn tc = super.getColumn(columnIndex);
				String fieldName = docTableModel.getFieldName(columnIndex);
				if (DOCUMENT_TITLE_ATTRIBUTE.equals(fieldName))
					return tc;
				
				if (isUtcTimeField(fieldName)) {
					tc.setPreferredWidth(120);
					tc.setMinWidth(120);
				}
				else if (docList.isNumeric(fieldName)) {
					tc.setPreferredWidth(50);
					tc.setMinWidth(50);
				}
				else {
					tc.setPreferredWidth(100);
					tc.setMinWidth(100);
				}
				
				tc.setResizable(true);
				
				return tc;
			}
		});
		this.docTable.setModel(this.docTableModel);
		
		//	make changes show
		this.sortList(this.mainSortField, false);
		this.docTable.validate();
		this.docTable.repaint();
		
		//	restore selection
		if (selDocData != null)
			this.setSelectedDocument(selDocData);
		
		//	let sub classes take their turn
		this.documentListChanged();
	}
	
	/**
	 * React to a change to the document list.
	 */
	protected void documentListChanged() {}
	
	/**
	 * Apply the current filter to the document list.
	 */
	public void filterDocumentList() {
		DocumentFilter filter = this.filterPanel.getFilter();
		
		//	handle filtering externally
		if (this.applyDocumentFilter(filter))
			return;
		
		//	filter local list
		this.updateListData(filter);
	}
	
	/**
	 * React to a change in the filter panel of the document list. The argument
	 * filter is the same one returned by <code>getDocumentFilter()</code>. An
	 * implementation of this method should apply the filter and then update
	 * the document list via the <code>setDocumentList()</code> method and then
	 * return true to indicate that the filter was applied externally. This
	 * default implementation simply returns false.
	 * @param filter the current filter.
	 */
	protected boolean applyDocumentFilter(DocumentFilter filter) {
		return false;
	}
	
	void sortList(String sortField, boolean isHeaderClick) {
		final StringVector sortFields = new StringVector();
		if (sortField != null)
			sortFields.addElement(sortField);
		sortFields.addContentIgnoreDuplicates(listSortKeys);
		
		//	check direction
		String mainSortField = sortFields.get(0);
		if (isHeaderClick && mainSortField.equals(this.mainSortField))
			this.mainSortDescending = !this.mainSortDescending;
		else this.mainSortDescending = false;
		this.mainSortField = mainSortField;
		
		//	cache current selection (if any)
		StringTupel selDocData = null;
		if (this.docTable.getSelectedRow() != -1)
			selDocData = this.listData[this.docTable.getSelectedRow()].data;
		
		//	update sort keys, and check which fields are numeric
		boolean[] isFieldNumeric = new boolean[sortFields.size()];
		Arrays.fill(isFieldNumeric, true);
		for (int d = 0; d < this.listData.length; d++) {
			this.listData[d].updateSortKey(sortFields);
			for (int f = 0; f < isFieldNumeric.length; f++) {
				if (isFieldNumeric[f]) try {
					Integer.parseInt((String) this.listData[d].sortKey[f]);
				}
				catch (NumberFormatException nfe) {
					isFieldNumeric[f] = false;
				}
			}
		}
		
		//	make field values numeric only if they are numeric throughout the list
		for (int d = 0; d < this.listData.length; d++)
			for (int f = 0; f < isFieldNumeric.length; f++) {
				if (isFieldNumeric[f])
					this.listData[d].sortKey[f] = new Integer((String) this.listData[d].sortKey[f]);
			}
		
		//	sort list
		Arrays.sort(this.listData, (this.mainSortDescending ? firstFieldDescending : allFieldsAscending));
		
		//	update display
		this.docTableModel.update();
		this.docTable.validate();
		this.docTable.repaint();
		
		//	restore selection
		this.setSelectedDocument(selDocData);
	}
	
	private static final Comparator allFieldsAscending = new Comparator() {
		public int compare(Object o1, Object o2) {
			StringTupelTray st1 = ((StringTupelTray) o1);
			StringTupelTray st2 = ((StringTupelTray) o2);
			int c = 0;
			for (int f = 0; f < st1.sortKey.length; f++) {
				if (st1.sortKey[f] instanceof Integer)
					c = (((Integer) st1.sortKey[f]).intValue() - ((Integer) st2.sortKey[f]).intValue());
				else c = ((String) st1.sortKey[f]).compareToIgnoreCase((String) st2.sortKey[f]);
				if (c != 0)
					return c;
			}
			return 0;
		}
	};
	private static final Comparator firstFieldDescending = new Comparator() {
		public int compare(Object o1, Object o2) {
			StringTupelTray st1 = ((StringTupelTray) o1);
			StringTupelTray st2 = ((StringTupelTray) o2);
			int c = 0;
			for (int f = 0; f < st1.sortKey.length; f++) {
				if (st1.sortKey[f] instanceof Integer)
					c = (((Integer) st1.sortKey[f]).intValue() - ((Integer) st2.sortKey[f]).intValue());
				else c = ((String) st1.sortKey[f]).compareToIgnoreCase((String) st2.sortKey[f]);
				if (c != 0)
					return ((f == 0) ? -c : c);
			}
			return 0;
		}
	};
	
	/**
	 * Indicate whether or not to display a filed with a given name as a column
	 * in the document table. This default implementation only hides 'docId',
	 * sub classes are welcome to overwrite it as needed to create a different
	 * behavior.
	 * @param fieldName the name of the field in question
	 * @param isEmpty display even though there are no actual values?
	 * @return true if the field should be displayed
	 */
	protected boolean displayField(String fieldName, boolean isEmpty) {
		return (!isEmpty && !DOCUMENT_ID_ATTRIBUTE.equals(fieldName));
	}
	
	/**
	 * Indicate whether or not a field contains a UTC timestamp. This default
	 * implementation assumes the latter for numeric fields whose name ends
	 * with 'Time' or 'Timestamp'. Sub classes are welcome to provide more a
	 * differentiate behavior.
	 * @param fieldName the name of the field in question
	 * @return true if the field represents a UTC timestamp
	 */
	protected boolean isUtcTimeField(String fieldName) {
		return (this.docList.isNumeric(fieldName) && (false
			|| fieldName.endsWith("Time")
			|| fieldName.equalsIgnoreCase("time")
			|| fieldName.endsWith("Timestamp")
			|| fieldName.equalsIgnoreCase("timestamp")
		));
	}
	
	/**
	 * Produce a custom display value to show in the document table rather than
	 * the actual data value, e.g. formatting UTC timestamps. This default
	 * implementation does the latter for time fields (as indicated by the
	 * <code>isUtcTimeField()</code> method). Sub classes are welcome to amend
	 * or completely change this behavior.
	 * @param fieldName the name of the field the argument value belongs to
	 * @param fieldValue the value to customize
	 * @param docData the parent document data
	 * @return the original or modified field value
	 */
	protected String getDisplayValue(String fieldName, String fieldValue, StringTupel docData) {
		if (this.isUtcTimeField(fieldName) && fieldValue.matches("[0-9]++")) try {
				return TIMESTAMP_DATE_FORMAT.format(new Date(Long.parseLong(fieldValue)));
			} catch (NumberFormatException e) {}
		return fieldValue;
	}
	
	/**
	 * React to a document being selected in the table, either with a single or
	 * a double click, as indicated by the respective argument. This method is
	 * also called when a row is selected via arrow keys (as single click) and
	 * if a user hits the return key on a document (as double click). The
	 * default implementation does nothing, sub classes are welcome to
	 * overwrite it as needed.
	 * @param docData the data of the selected document
	 * @param doubleClick was it a double click?
	 */
	protected void documentSelected(StringTupel docData, boolean doubleClick) {}
	
	void showContextMenu(final int row, MouseEvent me) {
		if (row == -1)
			return;
		JPopupMenu menu = this.getContextMenu(this.listData[row].data, me);
		if (menu != null)
			menu.show(this.docTable, me.getX(), me.getY());
	}
	
	/**
	 * Create a context menu to show for a right click on a document. If this
	 * method returns null, no context menu is shown. This default
	 * implementation does return null, sub classes are welcome to overwrite it
	 * as needed.
	 * @param docData the data of the document to show the context menu for
	 * @param me the mouse event that triggered the context menu
	 * @return a context menu to show for a right click on a document
	 */
	protected JPopupMenu getContextMenu(StringTupel docData, MouseEvent me) {
		return null;
	}
}
