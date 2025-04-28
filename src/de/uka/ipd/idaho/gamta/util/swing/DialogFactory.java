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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uka.ipd.idaho.gamta.AnnotationUtils;

/**
 * Factory for dialogs. This factory returns dialogs that have the currently
 * focused frame of dialog as their owner, thus, for instance, rendering
 * integration of anlyzers in arbitrary GUIs more natural in terms of that the
 * dialogs produced will inherit their icon and style from the owner. In
 * addition, if the owner window is closed, a non-modal dialog produced by this
 * method will be closed automatically along the way.
 * 
 * @author sautter
 */
public class DialogFactory {
	
	/**
	 * Retrieve the window on top of the hierarchy of (modal or non-modal)
	 * dialogs and frames. This is useful, for instance, for setting the
	 * location of the JOptionPane dialogs relative to the current window,
	 * instead of centering it on the screen, and at the same time saves
	 * tracking which window is currently on top.
	 * @return the window on top of the hierarchy, or null, if there is no such
	 *         window
	 */
	public static Window getTopWindow() {
		Window topWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		if (topWindow != null) return topWindow;
		
//		Disabled due to Java VM bug (InternalError in sun.awt.windows.WToolkit.eventLoop(Native Method) because of JNI flaw)
//		TODO: re-enable this once VM bug is fixed
//		Frame[] frames = Frame.getFrames();
//		LinkedList windows = new LinkedList();
//		for (int f = 0; f < frames.length; f++)
//			windows.addLast(frames[f]);
//		while (windows.size() != 0) {
//			topWindow = ((Window) windows.removeFirst());
//			Window[] subWindows = topWindow.getOwnedWindows();
//			for (int w = 0; w < subWindows.length; w++)
//				windows.add(subWindows[w]);
//		}
		
		return topWindow;
	}
	
	/**
	 * Produce a dialog with the currently focused frame or dialog as its
	 * owner. This method first obtains the top window, and that produces a
	 * JDialog modal to that window.
	 * @param title the title for the dialog
	 * @param modal should the dialog be modal?
	 * @return a dialog with the currently focused frame or dialog as its owner
	 */
	public static JDialog produceDialog(String title, boolean modal) {
		Window activeWindow = getTopWindow();
		final JDialog dialog;
		
		if (activeWindow instanceof Frame) {
			dialog = new JDialog(((Frame) activeWindow), title, modal);
			if (!modal) activeWindow.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent we) {
					if (dialog.isVisible()) dialog.dispose();
				}
			});
		}
		
		else if (activeWindow instanceof Dialog) {
			dialog = new JDialog(((Dialog) activeWindow), title, modal);
			if (!modal) activeWindow.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent we) {
					if (dialog.isVisible()) dialog.dispose();
				}
			});
		}
		
		else dialog = new JDialog(((Frame) null), title, modal);
		
		return dialog;
	}
	
	/**
	 * Object displaying prompts to users.
	 * 
	 * @author sautter
	 */
	public static interface PromptProvider {
		
		/**
		 * Show an alert message to a user.
		 * @param message the <code>Object</code> to display
		 * @param title the title string for the dialog
		 * @param messageType the type of message to be displayed:
		 *          <code>JOptionPane.ERROR_MESSAGE</code>,
		 *          <code>JOptionPane.INFORMATION_MESSAGE</code>,
		 *          <code>JOptionPane.WARNING_MESSAGE</code>,
		 *          <code>JOptionPane.QUESTION_MESSAGE</code>,
		 *          or <code>JOptionPane.PLAIN_MESSAGE</code>
		 * @param icon an icon to display in the dialog that helps the user
		 *                  identify the kind of message that is being displayed
		 * @see javax.swing.JOptionPane#showMessageDialog(Component, Object, String, int, Icon)
		 */
		public abstract void alert(Object message, String title, int messageType, Icon icon);
		
		/**
		 * Show a prompt asking a user for confirmation and possibly other input.
		 * @param message the <code>Object</code> to display
		 * @param title the title string for the dialog
		 * @param optionType an int designating the options available on the dialog:
		 *          <code>JOptionPane.YES_NO_OPTION</code>,
		 *          <code>JOptionPane.YES_NO_CANCEL_OPTION</code>,
		 *          or <code>JOptionPane.OK_CANCEL_OPTION</code>
		 * @param messageType the type of message to be displayed:
		 *          <code>JOptionPane.ERROR_MESSAGE</code>,
		 *          <code>JOptionPane.INFORMATION_MESSAGE</code>,
		 *          <code>JOptionPane.WARNING_MESSAGE</code>,
		 *          <code>JOptionPane.QUESTION_MESSAGE</code>,
		 *          or <code>JOptionPane.PLAIN_MESSAGE</code>
		 * @param icon an icon to display in the dialog that helps the user
		 *                  identify the kind of message that is being displayed
		 * @return an int indicating the option selected by the user
		 * @see javax.swing.JOptionPane#showConfirmDialog(Component, Object, String, int, int, Icon)
		 */
		public abstract int confirm(Object message, String title, int optionType, int messageType, Icon icon);
	}
	
	/**
	 * Retrieve the current prompt provider.
	 * @return the current prompt provider
	 */
	public static PromptProvider getPromptProvider() {
		return promptProvider;
	}
	
	/**
	 * Set the prompt provider to use for <code>alert()</code> and
	 * <code>confirm()</code> methods. Setting the prompt provider to
	 * <code>null</code> reverts this class to the default behavior, i.e.,
	 * using <code>JOptionPane</code>.
	 * @param pp the prompt provider to use
	 */
	public static void setPromptProvider(PromptProvider pp) {
		promptProvider = pp;
	}
	
	private static PromptProvider promptProvider;
	
	/**
	 * Show an alert message to a user. If no <code>PromptProvider</code> is
	 * registered, this method loops through to <code>JOptionPane</code>. If
	 * a <code>PromptProvider</code> is registered, it is used.
	 * @param message the <code>Object</code> to display
	 * @param title the title string for the dialog
	 * @see javax.swing.JOptionPane#showMessageDialog(Component, Object, String, int)
	 */
	public static void alert(Object message, String title) {
		alert(message, title, JOptionPane.PLAIN_MESSAGE, null);
	}
	
	/**
	 * Show an alert message to a user. If no <code>PromptProvider</code> is
	 * registered, this method loops through to <code>JOptionPane</code>. If
	 * a <code>PromptProvider</code> is registered, it is used.
	 * @param message the <code>Object</code> to display
	 * @param title the title string for the dialog
	 * @param messageType the type of message to be displayed:
	 *          <code>JOptionPane.ERROR_MESSAGE</code>,
	 *          <code>JOptionPane.INFORMATION_MESSAGE</code>,
	 *          <code>JOptionPane.WARNING_MESSAGE</code>,
	 *          <code>JOptionPane.QUESTION_MESSAGE</code>,
	 *          or <code>JOptionPane.PLAIN_MESSAGE</code>
	 * @see javax.swing.JOptionPane#showMessageDialog(Component, Object, String, int)
	 */
	public static void alert(Object message, String title, int messageType) {
		alert(message, title, messageType, null);
	}
	
	/**
	 * Show an alert message to a user. If no <code>PromptProvider</code> is
	 * registered, this method loops through to <code>JOptionPane</code>. If
	 * a <code>PromptProvider</code> is registered, it is used.
	 * @param message the <code>Object</code> to display
	 * @param title the title string for the dialog
	 * @param messageType the type of message to be displayed:
	 *          <code>JOptionPane.ERROR_MESSAGE</code>,
	 *          <code>JOptionPane.INFORMATION_MESSAGE</code>,
	 *          <code>JOptionPane.WARNING_MESSAGE</code>,
	 *          <code>JOptionPane.QUESTION_MESSAGE</code>,
	 *          or <code>JOptionPane.PLAIN_MESSAGE</code>
	 * @param icon an icon to display in the dialog that helps the user
	 *                  identify the kind of message that is being displayed
	 * @see javax.swing.JOptionPane#showMessageDialog(Component, Object, String, int, Icon)
	 */
	public static void alert(Object message, String title, int messageType, Icon icon) {
		if (promptProvider == null)
			JOptionPane.showMessageDialog(getTopWindow(), message, title, messageType, icon);
		else promptProvider.alert(message, title, messageType, icon);
	}
	
	/**
	 * Show a prompt asking a user for confirmation and possibly other input.
	 * If no <code>PromptProvider</code> is registered, this method loops
	 * through to <code>JOptionPane</code>. If a <code>PromptProvider</code>
	 * is registered, it is used.
	 * @param message the <code>Object</code> to display
	 * @param title the title string for the dialog
	 * @param optionType an int designating the options available on the dialog:
	 *          <code>JOptionPane.YES_NO_OPTION</code>,
	 *          <code>JOptionPane.YES_NO_CANCEL_OPTION</code>,
	 *          or <code>JOptionPane.OK_CANCEL_OPTION</code>
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane#showConfirmDialog(Component, Object, String, int, int)
	 */
	public static int confirm(Object message, String title, int optionType) {
		return confirm(message, title, optionType, JOptionPane.PLAIN_MESSAGE, null);
	}
	
	/**
	 * Show a prompt asking a user for confirmation and possibly other input.
	 * If no <code>PromptProvider</code> is registered, this method loops
	 * through to <code>JOptionPane</code>. If a <code>PromptProvider</code>
	 * is registered, it is used.
	 * @param message the <code>Object</code> to display
	 * @param title the title string for the dialog
	 * @param optionType an int designating the options available on the dialog:
	 *          <code>JOptionPane.YES_NO_OPTION</code>,
	 *          <code>JOptionPane.YES_NO_CANCEL_OPTION</code>,
	 *          or <code>JOptionPane.OK_CANCEL_OPTION</code>
	 * @param messageType the type of message to be displayed:
	 *          <code>JOptionPane.ERROR_MESSAGE</code>,
	 *          <code>JOptionPane.INFORMATION_MESSAGE</code>,
	 *          <code>JOptionPane.WARNING_MESSAGE</code>,
	 *          <code>JOptionPane.QUESTION_MESSAGE</code>,
	 *          or <code>JOptionPane.PLAIN_MESSAGE</code>
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane#showConfirmDialog(Component, Object, String, int, int)
	 */
	public static int confirm(Object message, String title, int optionType, int messageType) {
		return confirm(message, title, optionType, messageType, null);
	}
	
	/**
	 * Show a prompt asking a user for confirmation and possibly other input.
	 * If no <code>PromptProvider</code> is registered, this method loops
	 * through to <code>JOptionPane</code>. If a <code>PromptProvider</code>
	 * is registered, it is used.
	 * @param message the <code>Object</code> to display
	 * @param title the title string for the dialog
	 * @param optionType an int designating the options available on the dialog:
	 *          <code>JOptionPane.YES_NO_OPTION</code>,
	 *          <code>JOptionPane.YES_NO_CANCEL_OPTION</code>,
	 *          or <code>JOptionPane.OK_CANCEL_OPTION</code>
	 * @param messageType the type of message to be displayed:
	 *          <code>JOptionPane.ERROR_MESSAGE</code>,
	 *          <code>JOptionPane.INFORMATION_MESSAGE</code>,
	 *          <code>JOptionPane.WARNING_MESSAGE</code>,
	 *          <code>JOptionPane.QUESTION_MESSAGE</code>,
	 *          or <code>JOptionPane.PLAIN_MESSAGE</code>
	 * @param icon an icon to display in the dialog that helps the user
	 *                  identify the kind of message that is being displayed
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane#showConfirmDialog(Component, Object, String, int, int, Icon)
	 */
	public static int confirm(Object message, String title, int optionType, int messageType, Icon icon) {
		if (promptProvider == null)
			return JOptionPane.showConfirmDialog(getTopWindow(), message, title, optionType, messageType, icon);
		else return promptProvider.confirm(message, title, optionType, messageType, icon);
	}
	
	/**
	 * A panel with one or more pairs of a label and a combo box of strings,
	 * which can be editable or not. This class is useful for prompting users
	 * for selection or input of (groups of) strings.
	 * 
	 * @author sautter
	 */
	public static class StringSelectorPanel extends JPanel {
		private String title;
		private ArrayList selectors = new ArrayList(1);
		private JPanel selectorPanel = new JPanel(new GridBagLayout(), true);
		private GridBagConstraints gbc = new GridBagConstraints();
		
		/** Constructor
		 * @param title the title to use for prompt dialogs
		 */
		public StringSelectorPanel(String title) {
			super(new BorderLayout(), true);
			this.title = title;
			this.add(this.selectorPanel, BorderLayout.NORTH);
			this.gbc.fill = GridBagConstraints.HORIZONTAL;
			this.gbc.weighty = 0;
			this.gbc.insets.left = 3;
			this.gbc.insets.right = 3;
			this.gbc.insets.top = 1;
			this.gbc.insets.bottom = 1;
		}
		
		/**
		 * Add a string selector line to the panel. This method returns the
		 * newly added string selector line for further configuration, e.g.
		 * adding listeners to the selector combo box.
		 * @param label the label string
		 * @param selectable the selectable strings
		 * @param selected the initially selected string
		 * @param allowInput allow typing in a string beside selecting one?
		 * @return the selector line that was just added
		 */
		public StringSelectorLine addSelector(String label, String[] selectable, String selected, boolean allowInput) {
			StringSelectorLine ssl = new StringSelectorLine(label, selectable, selected, allowInput);
			this.gbc.gridy = this.selectors.size();
			this.gbc.gridx = 0;
			this.gbc.weightx = 1;
			this.selectorPanel.add(ssl.label, this.gbc.clone());
			this.gbc.gridx = 1;
			this.gbc.weightx = 0;
			this.selectorPanel.add(ssl.selector, this.gbc.clone());
			this.selectors.add(ssl);
			return ssl;
		}
		
		/**
		 * Retrieve the index-th selector line as a whole
		 * @param index the index of the selector line
		 * @return the selector line at the argument index
		 */
		public StringSelectorLine selectorAt(int index) {
			return ((StringSelectorLine) this.selectors.get(index));
		}
		
		/**
		 * Retrieve the index-th string.
		 * @param index the index of the selector line to get the string from
		 * @return the string at the argument index
		 */
		public String stringAt(int index) {
			return ((StringSelectorLine) this.selectors.get(index)).getSelectedString();
		}
		
		/**
		 * Retrieve the index-th string as as an object type or attribute name.
		 * This method performs a respective validity check, and returns null
		 * if the string fails the test.
		 * @param index the index of the selector line to get the string from
		 * @param showError show an error message if the test fails?
		 * @return the index-th string as a type or name
		 */
		public String typeOrNameAt(int index, boolean showError) {
			return ((StringSelectorLine) this.selectors.get(index)).getSelectedTypeOrName(showError);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		public Dimension getPreferredSize() {
			return new Dimension(400, ((this.selectors.size() * 23) + ((this.selectors.size() - 1) * 2)));
		}
		
		/**
		 * Prompt the user with this string selector panel. This method simply
		 * displays the panel via <code>JOptionPane.showConfirmDialog()</code>.
		 * The returned value is either <code>JOptionPane.OK_OPTION</code> or
		 * <code>JOptionPane.CANCEL_OPTION</code>. This method is sensible to
		 * use only if the string selector panel is not embedded in another
		 * JComponent.
		 * @param parent the component to center upon
		 * @return an indicator for which button the user closed the prompt with
		 */
		public int prompt(Component parent) {
			return confirm(this, this.title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	/**
	 * A panel with a single pair of a label and a combo box of strings, which
	 * can be editable or not. This class is useful for prompting users for
	 * selection or input of a string.
	 * 
	 * @author sautter
	 */
	public static class StringSelectorLine {
		
		/** the label explaining the selector */
		public final JLabel label;
		
		/** the selector combo box */
		public final JComboBox selector;
		
		/**
		 * Constructor
		 * @param label the label string
		 * @param selectable the selectable strings
		 * @param selected the initially selected string
		 * @param allowInput allow typing in a string beside selecting one?
		 */
		StringSelectorLine(String label, String[] selectable, String selected, boolean allowInput) {
			this.label = new JLabel(label);
			this.selector = new JComboBox(selectable);
			this.selector.setEditable(allowInput);
			if (selected != null)
				this.selector.setSelectedItem(selected);
			else if (selectable.length != 0)
				this.selector.setSelectedIndex(0);
		}
		
		/**
		 * Retrieve the string that was selected or typed in.
		 * @return the string
		 */
		public String getSelectedString() {
			Object strObj = this.selector.getSelectedItem();
			if (strObj == null)
				return null;
			String str = strObj.toString().trim();
			return ((str.length() == 0) ? null : str);
		}
		
		/**
		 * Retrieve the string that was selected or typed in, but retrieve it
		 * as an object type or attribute name. This method performs a
		 * respective validity check, and returns null if the string fails the
		 * test.
		 * @param showError show an error message if the test fails?
		 * @return the string as a type or name
		 */
		public String getSelectedTypeOrName(boolean showError) {
			String typeOrName = this.getSelectedString();
			if (AnnotationUtils.isValidAnnotationType(typeOrName))
				return typeOrName;
			if (showError)
				alert(("'" + typeOrName + "' is not a valid type or name."), "Invalid Type Or Name", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		/**
		 * Replace the selectable strings with new ones. This method is
		 * intended for dynamically reacting on updates to other selectors, not
		 * for reuse.
		 * @param selectable the selectable strings
		 * @param selected the initially selected string
		 * @param allowInput allow typing in a string beside selecting one?
		 */
		public void setSelectableStrings(String[] selectable, String selected, boolean allowInput) {
			this.selector.setModel(new DefaultComboBoxModel(selectable));
			this.selector.setEditable(allowInput);
			if (selected != null)
				this.selector.setSelectedItem(selected);
			else if (selectable.length != 0)
				this.selector.setSelectedIndex(0);
		}
	}
	
	/**
	 * Prompt the user for a type for an annotation. If this method returns a
	 * non-null value, the returned value is a valid annotation type and can be
	 * used without further checks.
	 * @param tite the title for the prompt dialog
	 * @param text the label text for the prompt dialog
	 * @param existingTypes the existing annotation types, for selection
	 * @param existingType the current type of the annotation (may be null)
	 * @param allowInput allow manual input of a non-existing type?
	 * @return the type the user provided or selected
	 */
	public static String promptForObjectType(String title, String text, String[] existingTypes, String existingType, boolean allowInput) {
		StringSelectorPanel ssp = new StringSelectorPanel(title);
		StringSelectorLine ssl = ssp.addSelector(text, existingTypes, existingType, allowInput);
		if (ssp.prompt(getTopWindow()) != JOptionPane.OK_OPTION)
			return null;
		return ssl.getSelectedTypeOrName(true);
	}
	
	/**
	 * Container for a pair of strings, for renaming annotations or attributes.
	 * 
	 * @author sautter
	 */
	public static class StringPair {
		public final String strOld;
		public final String strNew;
		StringPair(String strOld, String strNew) {
			this.strOld = strOld;
			this.strNew = strNew;
		}
	}
	
	/**
	 * Prompt the user for a type to change on an annotation. If this method
	 * returns a non-null value, the returned pair of values are valid
	 * annotation types and can be used without further checks. On top of this,
	 * the types are not equal.
	 * @param tite the title for the prompt dialog
	 * @param textOld the label text for the existing part of the prompt dialog
	 * @param textNew the label text for the new part of the prompt dialog
	 * @param existingTypes the existing annotation types, for selection
	 * @param existingType the current type of the annotation (may be null)
	 * @param allowInput allow manual input of a non-existing type?
	 * @return the type the user provided or selected
	 */
	public static StringPair promptForObjectTypeChange(String title, String textOld, String textNew, String[] existingTypes, String existingType, boolean allowInput) {
		StringSelectorPanel ssp = new StringSelectorPanel(title);
		final StringSelectorLine sslOld = ssp.addSelector(textOld, existingTypes, existingType, false);
		final StringSelectorLine sslNew = ssp.addSelector(textNew, existingTypes, existingType, true);
		sslOld.selector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				String oldType = sslOld.getSelectedString();
				sslNew.selector.setSelectedItem(oldType);
			}
		});
		if (ssp.prompt(getTopWindow()) != JOptionPane.OK_OPTION)
			return null;
		String typeOld = sslOld.getSelectedTypeOrName(false);
		String typeNew = sslNew.getSelectedTypeOrName(true);
		if ((typeOld == null) || (typeNew == null) || typeOld.equals(typeNew))
			return null;
		return new StringPair(typeOld, typeNew);
	}
}