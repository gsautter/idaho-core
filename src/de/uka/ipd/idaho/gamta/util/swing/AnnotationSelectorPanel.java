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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.TokenSequence;

/**
 * Widget for displaying a list of annotations. Depending on the constructor
 * arguments, the annotation list also shows a selector checkbox next to each
 * annotation. By default, an instance of this class contains the annotation
 * list in <code>BorderLayout.CENTER</code> position. Client code may add
 * further components in the other positions of a <code>BorderLayout</code>. If
 * such a component implements the <code>AnnotationSelectorAccessory</code>
 * interface, it will be consulted before committing a dialog displayed be one
 * of the <code>showDialog()</code> methods.
 * 
 * @author sautter
 */
public class AnnotationSelectorPanel extends JPanel {
	private boolean annotationsSelectable;
	private AnnotationTablePanel annotationTable;
	private LinkedHashSet accessories;
	
	/**	Constructor
	 * @param	host			the frame this dialog is modal to
	 * @param	title			the title for the dialog
	 * @param	annotations		the Annotations to display
	 */
	public AnnotationSelectorPanel(Annotation[] annotations) {
		this(annotations, annotationTypesGeneric(annotations), true);
	}
	private static boolean annotationTypesGeneric(Annotation[] annotations) {
		for (int a = 0; a < annotations.length; a++) {
			if (!Annotation.DEFAULT_ANNOTATION_TYPE.equals(annotations[a].getType()))
				return false;
		}
		return true;
	}
	
	/**	Constructor
	 * @param	host			the frame this dialog is modal to
	 * @param	title			the title for the dialog
	 * @param	annotations		the Annotations to display
	 */
	public AnnotationSelectorPanel(Annotation[] annotations, boolean selectable) {
		this(annotations, annotationTypesGeneric(annotations), selectable);
	}
	
	/**	Constructor
	 * @param	host			the frame this dialog is modal to
	 * @param	title			the title for the dialog
	 * @param	annotations		the Annotations to display
	 */
	public AnnotationSelectorPanel(Annotation[] annotations, boolean hideType, boolean selectable) {
		super(new BorderLayout(), true);
//		this.annotations = annotations;
		this.annotationsSelectable = selectable;
		this.annotationTable = new AnnotationTablePanel(annotations, hideType, selectable);
		this.add(this.annotationTable, BorderLayout.CENTER);
	}
	
	/**
	 * Show a dialog containing this panel. If the panel allows selecting
	 * annotations, the dialog contains two buttons: an 'Annotate' button and
	 * a 'Cancel' button. Otherwise, there is a single 'OK' button that closes
	 * the dialog. Note that if selecting annotations is disabled, this method
	 * always returns false.
	 * @param parent the component to center the dialog over
	 * @param title the title for the dialog
	 * @return true if the dialog was committed
	 */
	public boolean showDialog(Component parent, String title) {
		return this.showDialog(parent, title, (this.annotationsSelectable ? "Annotate" : "OK"), null);
	}
	
	/**
	 * Show a dialog containing this panel. If the panel allows selecting
	 * annotations, the dialog contains two buttons: an 'Annotate' button and
	 * a 'Cancel' button. Otherwise, there is a single 'OK' button that closes
	 * the dialog. Note that if selecting annotations is disabled, this method
	 * always returns false. If the argument custom fields component is
	 * specified, it shows above the annotation list. Further, if that component
	 * implements the <code>AnnotationSelectorAccessory</code> interface, it is
	 * consulted before committing the dialog.
	 * @param parent the component to center the dialog over
	 * @param title the title for the dialog
	 * @param customFields a component with custom fields to display
	 * @return true if the dialog was committed
	 */
	public boolean showDialog(Component parent, String title, JComponent customFields) {
		return this.showDialog(parent, title, (this.annotationsSelectable ? "Annotate" : "OK"), customFields);
	}
	
	/**
	 * Show a dialog containing this panel. If the panel allows selecting
	 * annotations, the dialog contains two buttons: a commit button with the
	 * argument button text, and a 'Cancel' button. Otherwise, the argument
	 * button text is used for the single button that closes the dialog. Note
	 * that if selecting annotations is disabled, this method always returns
	 * false.
	 * @param parent the component to center the dialog over
	 * @param title the title for the dialog
	 * @param buttonText the text for the commit or close button
	 * @return true if the dialog was committed
	 */
	public boolean showDialog(Component parent, String title, String buttonText) {
		return this.showDialog(parent, title, buttonText, null);
	}
	
	/**
	 * Show a dialog containing this panel. If the panel allows selecting
	 * annotations, the dialog contains two buttons: a commit button with the
	 * argument button text, and a 'Cancel' button. Otherwise, the argument
	 * button text is used for the single button that closes the dialog. Note
	 * that if selecting annotations is disabled, this method always returns
	 * false. If the argument custom fields component is specified, it shows
	 * above the annotation list. Further, if that component implements the
	 * <code>AnnotationSelectorAccessory</code> interface, it is consulted
	 * before committing the dialog.
	 * @param parent the component to center the dialog over
	 * @param title the title for the dialog
	 * @param buttonText the text for the commit or close button
	 * @param customFields a component with custom fields to display
	 * @return true if the dialog was committed
	 */
	public boolean showDialog(Component parent, String title, String buttonText, final JComponent customFields) {
		return this.doShowDialog(parent, title, buttonText, customFields);
	}
	
	/* while somewhat dirty, this maintains flexibility for instances of this
	 * class to show both in locally produced and in other dialogs, show
	 * multiple times, etc. without having to keep listeners internally and
	 * cleaning them up once a dialog closes, etc. */
	private JDialog dialog = null;
	private String dialogTitle = null;
	void updateDialogTitle() {
		if (this.dialog != null)
			this.dialog.setTitle(this.dialogTitle + " (" + this.annotationTable.getSelectedAnnotationCount() + " of " + this.annotationTable.getAnnotationCount() + " Annotations Selected)");
	}
	private boolean doShowDialog(Component parent, String title, String buttonText, final JComponent customFields) {
		try {
			this.dialog = DialogFactory.produceDialog(title, true);
			this.dialogTitle = title;
			return this.doShowDialog(this.dialog, parent, title, buttonText, customFields);
		}
		finally {
			this.dialog = null;
			this.dialogTitle = null;
		}
	}
	
	private boolean doShowDialog(final JDialog dialog, Component parent, String title, String buttonText, final JComponent customFields) {
		final boolean[] committed = {false};
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		//	initialize main buttons
		if (this.annotationsSelectable) {
			JButton commitButton = new JButton(buttonText);
			commitButton.setBorder(BorderFactory.createRaisedBevelBorder());
			commitButton.setPreferredSize(new Dimension(100, 21));
			commitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (customFields instanceof AnnotationSelectorAccessory) {
						if (((AnnotationSelectorAccessory) customFields).preventCommit())
							return;
					}
					if (preventCommit())
						return;
					committed[0] = true;
					dialog.dispose();
				}
			});
			buttonPanel.add(commitButton);
		}
		JButton abortButton = new JButton(this.annotationsSelectable ? "Cancel" : buttonText);
		abortButton.setBorder(BorderFactory.createRaisedBevelBorder());
		abortButton.setPreferredSize(new Dimension(100, 21));
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();
			}
		});
		buttonPanel.add(abortButton);
		
		if (this.annotationsSelectable)
			dialog.setTitle(title + " (" + this.annotationTable.getSelectedAnnotationCount() + " of " + this.annotationTable.getAnnotationCount() + " Annotations Selected)");
		else dialog.setTitle(title + " (" + this.annotationTable.getAnnotationCount() + " Annotations)");
		
		//	put the whole stuff together
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(this, BorderLayout.CENTER);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		if (customFields != null)
			dialog.getContentPane().add(customFields, BorderLayout.NORTH);
		
		dialog.setResizable(true);
		//	TODO: compute size dependent on annotations.length
		dialog.setSize(new Dimension(500, 700));
		dialog.setLocationRelativeTo((parent == null) ? dialog.getOwner() : parent);
		dialog.setVisible(true);
		
		//	finally ...
		return committed[0];
	}
	
	/**
	 * Interface allowing integrating of custom input data checks into an
	 * annotation selector dialog.
	 * 
	 * @author sautter
	 */
	public static interface AnnotationSelectorAccessory {
		
		/**
		 * Prevent committing an annotation selector dialog, e.g. due to some
		 * error in custom input fields contained by an instance of this class.
		 * If an implementation of this method returns true, it should inform
		 * the user about the error, e.g. via a prompt.
		 * @return true if committing an annotation selector dialog should be
		 *        prevented
		 */
		public abstract boolean preventCommit();
	}
	
	boolean preventCommit() {
		if (this.accessories == null)
			return false;
		for (Iterator ait = this.accessories.iterator(); ait.hasNext();) {
			if (((AnnotationSelectorAccessory) ait.next()).preventCommit())
				return true;
		}
		return false;
	}
	
	//	overwritten to catch accessories when added to the panel
	protected void addImpl(Component comp, Object constraints, int index) {
		super.addImpl(comp, constraints, index);
		if (comp instanceof AnnotationSelectorAccessory) {
			if (this.accessories == null)
				this.accessories = new LinkedHashSet();
			this.accessories.add(comp);
		}
	}
	public void remove(int index) {
		if (this.accessories != null)
			this.accessories.remove(this.getComponent(index));
		super.remove(index);
	}
	public void remove(Component comp) {
		if (this.accessories != null)
			this.accessories.remove(comp);
		super.remove(comp);
	}
	public void removeAll() {
		if (this.accessories != null)
			this.accessories.clear();
		super.removeAll();
	}
	
	/**
	 * Retrieve the annotations selected in the table. If the
	 * <code>showOnly</code> argument was true, this method always returns
	 * an empty array.
	 * @return an array holding the selected annotations
	 */
	public Annotation[] getSelectedAnnotations() {
		return this.annotationTable.getSelectedAnnotations();
	}
	
	/**
	 * React to a mouse click on an annotation displayed in the dialog. This
	 * default implementation does nothing, sub classes are welcome to
	 * overwrite it as needed.
	 * @param rowIndex the index of the clicked row
	 * @param clickCount the number of clicks
	 */
	protected void annotationClicked(int rowIndex, int clickCount) {}
	
	private class AnnotationTablePanel extends JPanel {
		private Annotation[] annotations;
		private boolean[] selectors;
		private int selected;
		private JTable annotationTable;
		private AnnotationTablePanel(Annotation[] annotations, boolean hideType, boolean selectable) {
			super(new BorderLayout(), true);
			this.setBorder(BorderFactory.createEtchedBorder());
			
			this.annotations = annotations;
			this.selectors = new boolean[this.annotations.length];
			for (int s = 0; s < this.selectors.length; s++)
				this.selectors[s] = selectable;
			this.selected = (selectable ? this.annotations.length : -1);
			
			DisplayAnnotationTableModel atm;
			if (selectable) {
				atm = new SelectableAnnotationTableModel(this.annotations, hideType, this.selectors) {
					public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
						if ((columnIndex == 0) && (selectors[rowIndex] != ((Boolean) newValue).booleanValue())) {
							if (selectors[rowIndex])
								selected--;
							else selected++;
							updateDialogTitle();
						}
						super.setValueAt(newValue, rowIndex, columnIndex);
					}
				};
				
				JPanel buttonPanel = new JPanel();
				JButton selectAllButton = new JButton("Select All");
				selectAllButton.setBorder(BorderFactory.createRaisedBevelBorder());
				selectAllButton.setPreferredSize(new Dimension(100, 21));
				selectAllButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						selectAll();
					}
				});
				buttonPanel.add(selectAllButton);
				JButton selectNoneButton = new JButton("Select None");
				selectNoneButton.setBorder(BorderFactory.createRaisedBevelBorder());
				selectNoneButton.setPreferredSize(new Dimension(100, 21));
				selectNoneButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						selectNone();
					}
				});
				buttonPanel.add(selectNoneButton);
				this.add(buttonPanel, BorderLayout.SOUTH);
			}
			else atm = new DisplayAnnotationTableModel(this.annotations, hideType);
			
			this.annotationTable = new JTable(atm);
			for (int c = 0; c < this.annotationTable.getColumnCount(); c++) {
				int cw = atm.getColumnWidth(c);
				if (cw != -1)
					this.annotationTable.getColumnModel().getColumn(c).setMaxWidth(cw);
			}
			this.annotationTable.setDefaultRenderer(Object.class, new TooltipAwareTableRenderer((atm.getColumnCount() - 1), atm.getColumnCount()));
			
			this.annotationTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					int rowIndex = annotationTable.getSelectedRow();
					if (rowIndex != -1)
						annotationClicked(rowIndex, me.getClickCount());
				}
			});
			
			JScrollPane annotationTableBox = new JScrollPane(this.annotationTable);
			annotationTableBox.getVerticalScrollBar().setUnitIncrement(50);
			annotationTableBox.getVerticalScrollBar().setBlockIncrement(50);
			this.add(annotationTableBox, BorderLayout.CENTER);
		}
		void selectAll() {
			for (int s = 0; s < this.selectors.length; s++)
				this.selectors[s] = true;
			this.selected = this.annotations.length;
			this.annotationTable.repaint();
			this.validate();
			updateDialogTitle();
		}
		void selectNone() {
			for (int s = 0; s < this.selectors.length; s++)
				this.selectors[s] = false;
			this.selected = 0;
			this.annotationTable.repaint();
			this.validate();
			updateDialogTitle();
		}
		int getAnnotationCount() {
			return this.annotations.length;
		}
		int getSelectedAnnotationCount() {
			return this.selected;
		}
		Annotation[] getSelectedAnnotations() {
			ArrayList annotations = new ArrayList();
			for (int a = 0; a < this.annotations.length; a++) {
				if (this.selectors[a])
					annotations.add(this.annotations[a]);
			}
			return ((Annotation[]) annotations.toArray(new Annotation[annotations.size()]));
		}
	}
	
	private static class DisplayAnnotationTableModel implements TableModel {
		private static final String[] WITH_TYPE_COLUMN_NAMES = {"Type", "Start", "Size", "Value"};
		private static final String[] NO_TYPE_COLUMN_NAMES = {"Start", "Size", "Value"};
		Annotation[] annotations;
		boolean hideType;
		String[] columnNames;
		DisplayAnnotationTableModel(Annotation[] annotations, boolean hideType) {
			this.annotations = annotations;
			this.hideType = hideType;
			this.columnNames = (this.hideType ? NO_TYPE_COLUMN_NAMES : WITH_TYPE_COLUMN_NAMES);
		}
		public int getColumnCount() {
			return this.columnNames.length;
		}
		public Class getColumnClass(int columnIndex) {
			return String.class;
		}
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (this.hideType)
				columnIndex++;
			Annotation annot = this.annotations[rowIndex];
			if (columnIndex == 0)
				return annot.getType();
			else if (columnIndex == 1)
				return ("" + annot.getStartIndex());
			else if (columnIndex == 2)
				return ("" + annot.size());
			else if (columnIndex == 3)
				return annot.getValue();
			else return null;
		}
		public int getRowCount() {
			return this.annotations.length;
		}
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		public void setValueAt(Object newValue, int rowIndex, int columnIndex) {}
		public void addTableModelListener(TableModelListener l) {}
		public void removeTableModelListener(TableModelListener l) {}
		int getColumnWidth(int columnIndex) {
			if (this.hideType)
				columnIndex++;
			if (columnIndex == 0)
				return 120;
			else if (columnIndex < 3)
				return 60;
			else return -1;
		}
	}
	
	private static class SelectableAnnotationTableModel extends DisplayAnnotationTableModel {
		private boolean[] selectors;
		SelectableAnnotationTableModel(Annotation[] annotations, boolean hideType, boolean[] selectors) {
			super(annotations, hideType);
			this.selectors = selectors;
		}
		public Class getColumnClass(int columnIndex) {
			return ((columnIndex == 0) ? Boolean.class : super.getColumnClass(columnIndex - 1));
		}
		public int getColumnCount() {
			return (1 + super.getColumnCount());
		}
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Select";
			else return super.getColumnName(columnIndex - 1);
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return new Boolean(this.selectors[rowIndex]);
			else return super.getValueAt(rowIndex, (columnIndex - 1));
		}
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 0);
		}
		public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				this.selectors[rowIndex] = ((Boolean) newValue).booleanValue();
		}
		int getColumnWidth(int columnIndex) {
			if (columnIndex == 0)
				return 60;
			else return super.getColumnWidth(columnIndex - 1);
		}
	}
	
	private static class TooltipAwareTableRenderer extends DefaultTableCellRenderer {
		private boolean[] isTooltipColumn;
		private TooltipAwareTableRenderer(int tooltipColumn, int columnCount) {
			this.isTooltipColumn = new boolean[columnCount];
			Arrays.fill(this.isTooltipColumn, false);
			this.isTooltipColumn[tooltipColumn] = true;
		}
		private TooltipAwareTableRenderer(int[] tooltipColumns, int columnCount) {
			this.isTooltipColumn = new boolean[columnCount];
			Arrays.fill(this.isTooltipColumn, false);
			for (int c = 0; c < tooltipColumns.length; c++) {
				if (tooltipColumns[c] < this.isTooltipColumn.length)
					this.isTooltipColumn[tooltipColumns[c]] = true;
			}
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			JComponent component = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (this.isTooltipColumn[col])
				component.setToolTipText(this.produceTooltipText(Gamta.INNER_PUNCTUATION_TOKENIZER.tokenize(value.toString())));
			return component;
		}
		private String produceTooltipText(TokenSequence tokens) {
			if (tokens.length() < 100)
				return tokens.toString();
			StringBuffer ttt = new StringBuffer("<HTML>");
			int lineLength = 0;
			String lastToken = null;
			for (int t = 0; t < tokens.size(); t++) {
				String token = tokens.valueAt(t);
				if (lineLength > 100) {
					ttt.append("<BR>");
					lineLength = 0;
				}
				else if (Gamta.insertSpace(lastToken, token)) {
					ttt.append(" ");
					lineLength++;
				}
				ttt.append(AnnotationUtils.escapeForXml(token));
				lineLength += token.length();
				lastToken = token;
			}
			ttt.append("</HTML>");
			return ttt.toString();
		}
	}
}
