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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Segment;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationListener;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.CharSequenceListener;
import de.uka.ipd.idaho.gamta.EditableAnnotation;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.MutableCharSequence.CharSequenceEvent;
import de.uka.ipd.idaho.gamta.MutableTokenSequence.TokenSequenceEvent;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.Token;
import de.uka.ipd.idaho.gamta.TokenSequenceListener;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.MutableEditableAnnotation;
import de.uka.ipd.idaho.gamta.util.MutableQueriableAnnotation;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.SgmlDocumentReader;

/**
 * @author sautter
 *
 */
public class GamtaDocumentMarkupPanel extends JPanel implements Scrollable {
	
	/** array holding all fixed names of display properties (this does not
	 * include annotation colors or display modes, as those differ between
	 * annotation types, and the associated display property names include
	 * those very annotation types for distinction) */
	public static final String[] displayPropertyNames = {
		"token.font",
		"token.foreground",
		"token.background",
		"token.selectedForeground",
		"token.selectedBackground",
		"tag.font",
		"tag.foreground",
		"tag.background",
		"tag.selectedForeground",
		"tag.selectedBackground",
		"view.relativeStableHeight",
		"view.stableHeightLevel",
		"annot.highlightAlpha",
	};
	
	/*
TODO Further thoughts on XM document markup panel (likely for later additions, _NOT_ April deadline):
- DO add support for those default annotation colors ...
- ... and default to them before creating new random annotation color ...
  ==> also add getter for map holding said colors to use for saving application color settings
  ==> add view menu function current color settings as defaults ...
  ==> ... replacing existing colors with new ones, but _not_ evicting colors for types not present in document
    ==> maybe even add function only updating default colors for _visible_ annotations
  ==> likely do exact same for default annotation display modes ...
  ==> ... most likely restricted to visible annotations ...
  ==> ... and use those defaults in combination with document indicated nature of annotations to implement DocumentDisplay.setAnnotationsVisible()
	 */
	
	/*
TODO IN LONG HAUL, might want to create 'structured GAMTA document display panel':
- basically use same two-level approach as XM document display panel ...
- ... and choose dynamically in genetic XML view
==> should be WAY faster for whole-document XML view in GGI
==> might need to alter action providers to observe structural annotations ...
==> ... if with asking display panel about nature of given annotation
  ==> why not re-port XM action providers !?!
  ==> might require document wrapper to enforce paragraph related invariants
==> need to abstract out selection and action interfaces to use same provider plug-ins ...
==> ... most likely deriving 'simple' and 'structured' display panels from (then) abstract base panel
  ==> might even use static factory method ...
  ==> ... choosing from (possibly even internal) non-abstract classes on the fly
  ==> AND THEN, THIS DOES NOT WORK ...
  ==> ... as how the fuck would we overwrite those action getter mounting points under usage of factory ???
    ==> might specify 'document action provider' interface with respective methods to use as factory method argument
- might actually simply re-introduce paragraph panel ...
- ... using single paragraph panel for whole document in simple mode ...
- ... and more sophisticated two-level structure from XM document display panel in structured mode
  ==> might just as well re-clone and re-adjust XM document display panel ...
  ==> ... at least in parts
  ==> TEST THIS with 100+ page IMF in GGI once latter moved to new GG core
	 */
	
	/** display mode not showing annotations */
	public static final Character DISPLAY_MODE_INVISIBLE = Character.valueOf('I');
	
	/** display mode showing annotation tags */
	public static final Character DISPLAY_MODE_SHOW_TAGS = Character.valueOf('T');
	
	/** display mode reducing structural annotations to their opening tags, hiding the contents */
	public static final Character DISPLAY_MODE_FOLD_CONTENT = Character.valueOf('F');
	
	/** display mode highlighting values of detail annotations */
	public static final Character DISPLAY_MODE_SHOW_HIGHLIGHTS = Character.valueOf('H');
	
	/** the document displayed in this viewer panel */
	public final QueriableAnnotation document;
	
	/** are the annotations editable on the document displayed in this viewer panel */
	public final boolean annotsEditable;
	
	/** are the tokens editable on the document displayed in this viewer panel */
	public final boolean tokensEditable;
	
	
//	private HashMap structAnnotDisplayModes = new HashMap();
	private HashMap detailAnnotDisplayModes = new HashMap();
	
	private static final int defaultAnnotHighlightAlpha = 0x40;
	
	private TreeMap annotColors = new TreeMap();
	private TreeMap annotHighlightColors = new TreeMap();
	private int annotHighlightAlpha = defaultAnnotHighlightAlpha;
	
	final Map annotIDsToPanels = Collections.synchronizedMap(new HashMap());
	final ArrayList tokenPanels = new ArrayList();
	final ArrayList tokenPanelAtIndex = new ArrayList();
	final ArrayList tagPanels = new ArrayList();
	
//	boolean foldAnnotsEnabled = false;
//	boolean highlightAnnotsEnabled = false;
//	
	private static final Font defaultTokenTextFont = new Font("Monospaced", Font.PLAIN, 12);
	private static final Color defaultTokenTextColor = Color.BLACK;
	private static final Color defaultTokenBackgroundColor = Color.WHITE;
	private static final Color defaultSelTokenTextColor = Color.BLACK;
	private static final Color defaultSelTokenBackgroundColor = new Color(0x00, 0xFF, 0x00, 0x80);
	
	Font tokenTextFont = defaultTokenTextFont;
	Color tokenTextColor = defaultTokenTextColor;
	Color tokenBackgroundColor = defaultTokenBackgroundColor;
	Color selTokenTextColor = defaultSelTokenTextColor;
	Color selTokenBackgroundColor = defaultSelTokenBackgroundColor;
	private OutlineBorder outlineBorder = new OutlineBorder(this.tokenBackgroundColor);
	
	private static final Font defaultTagTextFont = new Font("Monospaced", Font.PLAIN, 12);
	private static final Color defaultTagTextColor = Color.BLACK;
	private static final Color defaultTagBackgroundColor = Color.WHITE;
	private static final Color defaultSelTagTextColor = Color.BLACK;
	private static final Color defaultSelTagBackgroundColor = new Color(0x00, 0x00, 0xFF, 0x55);
	
	Font tagTextFont = defaultTagTextFont;
	Color tagTextColor = defaultTagTextColor;
	Color tagBackgroundColor = defaultTagBackgroundColor;
	Color selTagTextColor = defaultSelTagTextColor;
	Color selTagBackgroundColor = defaultSelTagBackgroundColor;
	
//	int structAnnotModCount = 0;
//	int validStructAnnotModCount = 0;
//	int structAnnotTagModCount = 0;
//	int validStructAnnotTagModCount = 0;
	int tokenModCount = 0;
	int validTokenModCount = 0;
	int detailAnnotModCount = 0;
	int validDetailAnnotModCount = 0;
	int detailAnnotTagModCount = 0;
	int validDetailAnnotTagModCount = 0;
	
	int annotTagModCount = 0;
	int validAnnotTagModCount = 0;
	
	MutableAnnotation mutableDocument;
	final DocumentChangeTracker mutableDocumentChangeTracker;
	
	/*
TODO More on GAMTA document markup panel:
- use 'attributed at offset' (token or highlight end cap annotation) in place of 'anchor at offset'
  ==> should facilitate selection objects similar to XM document markup panel
- give offsets in token selections (which might be whitespace-only) ...
- ... to facilitate handling of key strokes if tokens editable
  ==> actually, build in key stroke handling in absence of any selection ...
    ==> in fact, it already is for arrow keys, regardless of selection
  ==> ... and simply consume typing (character producing) key events if tokens not editable ...
  ==> ... while applying typing key events to tokens if tokens editable
    ==> put whole thing in 'handle character changing input' method (need to consider 'DEL' and 'BKS' in naming) ...
    ==> ... simply returning in absence of selection if tokens not editable ...
    ==> ... and applying any key bound actions in presence of selection ...
    ==> ... getting latter from mounting point method
      ==> whole approach might actually also work for XM document markup panel
      ==> ALSo, add that pesky 'instant context menu' flag to XM document markup panel ...
      ==> ... which basically disabled any keyboard shortcuts ...
      ==> ... but saves right click for context menu access
         ==> might want to use checkbox menu item for toggling
- add getter for preferred visualization method for given annotation type ...
- ... akin to corresponding mounting point in XM document display panel
- ALSO, add that pesky 'Advanced ...' item to GGI and GGX (amd ultimately mew GGE) context menus ...
- ... to facilitate applying generic document processors to selected or clicked annotations
	 */
	
	/**
	 * Constructor
	 * @param document the document to display
	 * @param annotsEditable can the annotations in the argument document be modified?
	 * @param tokensEditable can the tokens in the argument document be modified?
	 */
	public GamtaDocumentMarkupPanel(QueriableAnnotation document, boolean annotsEditable, boolean tokensEditable) {
		super(new GridBagLayout(), true);
		this.document = document;
		this.annotsEditable = annotsEditable;
		this.tokensEditable = tokensEditable;
		if (this.document instanceof EditableAnnotation) {
			DocumentChangeTracker changeTracker = new DocumentChangeTracker();
			((EditableAnnotation) this.document).addAnnotationListener(changeTracker);
			if (this.document instanceof MutableAnnotation) {
				((MutableAnnotation) this.document).addTokenSequenceListener(changeTracker);
				((MutableAnnotation) this.document).addCharSequenceListener(changeTracker);
			}
			this.mutableDocumentChangeTracker = null;
		}
		else this.mutableDocumentChangeTracker = new DocumentChangeTracker();
		this.setOpaque(true);
		this.setBackground(this.tokenBackgroundColor);
		this.setBorder(this.outlineBorder); // add 5 pixels of margin around content (simply looks better in Swing UI)
		this.setFocusCycleRoot(true);
		this.setFocusTraversalPolicyProvider(true);
		this.layoutContentFull();
	}
	
	private class DocumentChangeTracker implements AnnotationListener, TokenSequenceListener, CharSequenceListener {
		public void charSequenceChanged(CharSequenceEvent change) {
			//	TODO figure this out when we rebuild GGE around new GG core
			//	TODO ==> find and flag as dirty token panel change occurred in
			if (change.inserted != null)
				for (int c = 0; c < change.inserted.length(); c++) {
					if (Gamta.SPACES.indexOf(change.inserted.charAt(c)) == -1)
						return; // tokens changed as well, this one comes in below
				}
			if (change.removed != null)
				for (int c = 0; c < change.removed.length(); c++) {
					if (Gamta.SPACES.indexOf(change.removed.charAt(c)) == -1)
						return; // tokens changed as well, this one comes in below
				}
			int low = 0;
			int high = (tokenPanels.size() - 1);
			while (low < high) {
				int mid = ((low + high) / 2);
				TokenMarkupPanel midGtmp = ((TokenMarkupPanel) tokenPanelAtIndex.get(mid));
				if (midGtmp.maxTokenEndOffset < change.offset)
					low = (mid + 1);
				else if (change.offset < midGtmp.minTokenStartOffset)
					high = (mid - 1);
				else {
					midGtmp.clean = false;
					break;
				}
			}
			tokenModCount++;
		}
		public void tokenSequenceChanged(TokenSequenceEvent change) {
			TokenMarkupPanel modGtmp = ((TokenMarkupPanel) tokenPanelAtIndex.get(change.index));
			if (modGtmp != null)
				modGtmp.clean = false;
			if (change.removed != null)
				for (int t = 0; t < change.removed.size(); t++) {
					modGtmp = ((TokenMarkupPanel) tokenPanelAtIndex.get(change.index + t));
					if (modGtmp != null)
						modGtmp.clean = false;
				}
			tokenModCount++;
		}
		public void annotationAdded(QueriableAnnotation doc, Annotation annotation) {
			if (gdvc != null) {
				if (immediatelyUpdateXdvc)
					gdvc.updateControls();
				else gdvcDocModCount++;
			}
			detailAnnotModCount++;
		}
		public void annotationRemoved(QueriableAnnotation doc, Annotation annotation) {
			if (gdvc != null) {
				if (immediatelyUpdateXdvc)
					gdvc.updateControls();
				else gdvcDocModCount++;
			}
			detailAnnotModCount++;
		}
		public void annotationTypeChanged(QueriableAnnotation doc, Annotation annotation, String oldType) {
			if (gdvc != null) {
				if (immediatelyUpdateXdvc)
					gdvc.updateControls();
				else gdvcDocModCount++;
			}
			detailAnnotModCount++;
		}
		public void annotationAttributeChanged(QueriableAnnotation doc, Annotation annotation, String attributeName, Object oldValue) {
			AnnotMarkupPanel gamp = ((AnnotMarkupPanel) annotIDsToPanels.get(annotation.getAnnotationID()));
			if (gamp != null) {
				gamp.annotTagModCount++;
				detailAnnotTagModCount++;
			}
		}
	}
	
	private static class OutlineBorder extends LineBorder {
		OutlineBorder(Color color) {
			super(color, 5);
		}
		void setColor(Color color) {
			this.lineColor = color;
		}
	}
	
	public void refreshDisplay() {
		this.recordStableViewContentPanels(true, true);
		this.layoutContentFull();
		if (this.gdvc != null)
			this.gdvc.updateControls();
		this.restoreStableViewContentPanels();
	}
	
	private void layoutContentFull() {
		this.layoutContent(false, null, null, null, null);
	}
	private void layoutContentDisplayControl(String type, Character oldMode, Character newMode) {
		Set hideTags = ((oldMode == DISPLAY_MODE_SHOW_TAGS) ? Collections.singleton(type) : null);
		Set hideHighlights = ((oldMode == DISPLAY_MODE_SHOW_HIGHLIGHTS) ? Collections.singleton(type) : null);
		Set showTags = ((newMode == DISPLAY_MODE_SHOW_TAGS) ? Collections.singleton(type) : null);
		Set showHighlights = ((newMode == DISPLAY_MODE_SHOW_HIGHLIGHTS) ? Collections.singleton(type) : null);
		this.layoutContent(true, showTags, hideTags, showHighlights, hideHighlights);
	}
	private void layoutContent(boolean retainCleanTokenPanels, Set showTags, Set hideTags, Set showHighlights, Set hideHighlights) {
		this.removeAll();
		if (DEBUG_CONTENT_RENDERING) System.out.println("Laying out document");
		
		//	collect clean token panel
		ArrayList cleanTokenPanels = new ArrayList();
		if (retainCleanTokenPanels) {
			
			//	flag dirty token panels (ones adjacent to changing annotation boundaries)
			if (showTags != null)
				for (Iterator atit = showTags.iterator(); atit.hasNext();) {
					String annotType = ((String) atit.next());
					QueriableAnnotation[] annots = this.document.getAnnotations(annotType);
					if (DEBUG_CONTENT_RENDERING) System.out.println(" - marking dirty token panels for " + annots.length + " '" + annotType + "' annotations showing tags");
					for (int a = 0; a < annots.length; a++) {
						TokenMarkupPanel lGtmp = (((annots[a].getStartIndex() != 0) && ((annots[a].getStartIndex() - 1) < this.tokenPanelAtIndex.size())) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex() - 1)) : null);
						TokenMarkupPanel sGtmp = ((annots[a].getStartIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex())) : null);
						if ((lGtmp == null) && (sGtmp == null)) {}
						else if (lGtmp == sGtmp)
							sGtmp.clean = false; // this one will be split by inserted tag
						else if (sGtmp != null) {
							TokenMarkupPanelObjectTray sGtmpStartObject = sGtmp.getObjectTrayAt(0);
							if (sGtmpStartObject.index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX)
								sGtmp.clean = false; // start tag will go after closing highlight end cap
							else if (sGtmpStartObject.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) {
								Annotation annot = ((Annotation) sGtmpStartObject.object);
								if (annots[a].size() <= annot.size())
									sGtmp.clean = false; // opening highlight end cap of this one might well go outside start panel
							}
						}
						TokenMarkupPanel eGtmp = (((annots[a].getEndIndex() - 1) < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex() - 1)) : null);
						TokenMarkupPanel tGtmp = ((annots[a].getEndIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex())) : null);
						if ((eGtmp == null) && (tGtmp == null)) {}
						else if (eGtmp == tGtmp)
							eGtmp.clean = false; // this one will be split by inserted tag
						else if (eGtmp != null) {
							TokenMarkupPanelObjectTray eGtmpEndObject = eGtmp.getObjectTrayAt(eGtmp.getAnchorCount() - 1);
							if (eGtmpEndObject.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX)
								eGtmp.clean = false; // end tag will go before opening highlight end cap
							else if (eGtmpEndObject.index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX) {
								Annotation annot = ((Annotation) eGtmpEndObject.object);
								if (annot.getStartIndex() <= annots[a].getStartIndex())
									eGtmp.clean = false; // closing highlight end cap of this one might well go outside end panel
							}
						}
					}
				}
			if (hideTags != null)
				for (Iterator atit = hideTags.iterator(); atit.hasNext();) {
					String annotType = ((String) atit.next());
					QueriableAnnotation[] annots = this.document.getAnnotations(annotType);
					if (DEBUG_CONTENT_RENDERING) System.out.println(" - marking dirty token panels for " + annots.length + " '" + annotType + "' annotations hiding tags");
					for (int a = 0; a < annots.length; a++) {
						TokenMarkupPanel lGtmp = (((annots[a].getStartIndex() != 0) && ((annots[a].getStartIndex() - 1) < this.tokenPanelAtIndex.size())) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex() - 1)) : null);
						TokenMarkupPanel sGtmp = ((annots[a].getStartIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex())) : null);
						if (lGtmp == null) {
							if ((sGtmp != null) && (sGtmp.index != 0))
								sGtmp.clean = false; // start of document, but with highlight-end-cap-only panel before it
						}
						else if (this.document.tokenAt(lGtmp.maxTokenIndex).hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE)) /* paragraph break will keep panels apart, need to be adjacent, though */ {
							if ((sGtmp != null) && (sGtmp.index != (lGtmp.index + 1)))
								sGtmp.clean = false; // at paragraph start, but potentially with highlight-end-cap-only panel before it
						}
						else /* panels likely to merge when tag taken out */ {
							lGtmp.clean = false;
							if (sGtmp != null)
								sGtmp.clean = false;
						}
						TokenMarkupPanel eGtmp = (((annots[a].getEndIndex() - 1) < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex() - 1)) : null);
						TokenMarkupPanel tGtmp = ((annots[a].getEndIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex())) : null);
						if (eGtmp == null) {}
						else if (this.document.tokenAt(eGtmp.maxTokenIndex).hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE)) /* paragraph break will keep panels apart, need to be at boundary, though */ {
							if ((tGtmp == null) && ((eGtmp.index + 1) < this.tokenPanels.size()))
								eGtmp.clean = false; // end of document, but potentially with highlight-end-cap-only panel after it
							else if ((tGtmp != null) && (eGtmp.index != (tGtmp.index - 1)))
								eGtmp.clean = false; // at paragraph end, but potentially with highlight-end-cap-only panel after it
						}
						else /* panels likely to merge when tag taken out */ {
							eGtmp.clean = false;
							if (tGtmp != null)
								tGtmp.clean = false;
						}
					}
				}
			if (showHighlights != null)
				for (Iterator atit = showHighlights.iterator(); atit.hasNext();) {
					String annotType = ((String) atit.next());
					QueriableAnnotation[] annots = this.document.getAnnotations(annotType);
					if (DEBUG_CONTENT_RENDERING) System.out.println(" - marking dirty token panels for " + annots.length + " '" + annotType + "' annotations showing highlights");
					for (int a = 0; a < annots.length; a++) {
						TokenMarkupPanel lGtmp = (((annots[a].getStartIndex() != 0) && ((annots[a].getStartIndex() - 1) < this.tokenPanelAtIndex.size())) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex() - 1)) : null);
						TokenMarkupPanel sGtmp = ((annots[a].getStartIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex())) : null);
						if ((lGtmp == null) && (sGtmp == null)) {}
						else if (lGtmp == sGtmp)
							sGtmp.clean = false; // opening highlight end cap will be inserted in middle of this one
						else if (sGtmp != null) {
							sGtmp.clean = false; // opening highlight end cap might go at start of this one
							if ((lGtmp != null) && !this.document.tokenAt(lGtmp.maxTokenIndex).hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE))
								lGtmp.clean = false; // opening highlight end cap might also go at end of this one in absence of separating paragraph break
						}
						TokenMarkupPanel eGtmp = (((annots[a].getEndIndex() - 1) < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex() - 1)) : null);
						TokenMarkupPanel tGtmp = ((annots[a].getEndIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex())) : null);
						if ((eGtmp == null) && (tGtmp == null)) {}
						else if (eGtmp == tGtmp)
							eGtmp.clean = false; // closing highlight end cap will be inserted in middle of this one
						else if (eGtmp != null) {
							eGtmp.clean = false; // closing highlight end cap might go at end of this one
							if ((tGtmp != null) && !this.document.tokenAt(eGtmp.maxTokenIndex).hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE))
								tGtmp.clean = false; // closing highlight end cap might also go at start of this one in absence of separating paragraph break
						}
					}
				}
			if (hideHighlights != null)
				for (Iterator atit = hideHighlights.iterator(); atit.hasNext();) {
					String annotType = ((String) atit.next());
					QueriableAnnotation[] annots = this.document.getAnnotations(annotType);
					if (DEBUG_CONTENT_RENDERING) System.out.println(" - marking dirty token panels for " + annots.length + " '" + annotType + "' annotations hiding highlights");
					for (int a = 0; a < annots.length; a++) {
						TokenMarkupPanel lGtmp = (((annots[a].getStartIndex() != 0) && ((annots[a].getStartIndex() - 1) < this.tokenPanelAtIndex.size())) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex() - 1)) : null);
						TokenMarkupPanel sGtmp = ((annots[a].getStartIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getStartIndex())) : null);
						if ((lGtmp == null) && (sGtmp == null)) {}
						else if (lGtmp == sGtmp)
							sGtmp.clean = false; // opening highlight end cap will be removed from middle of this one
						else if ((sGtmp != null) && sGtmp.clean)
							for (int o = 0; o < sGtmp.getAnchorCount(); o++) {
								TokenMarkupPanelObjectTray sGtmpObject = sGtmp.getObjectTrayAt(o);
								if (-1 < sGtmpObject.index)
									break; // found token, no need to look any further
								else if (sGtmpObject.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) {
									Annotation annot = ((Annotation) sGtmpObject.object);
									if (annots[a].getAnnotationID().equals(annot.getAnnotationID())) {
										sGtmp.clean = false;
										break;
									}
								}
							}
						TokenMarkupPanel eGtmp = (((annots[a].getEndIndex() - 1) < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex() - 1)) : null);
						TokenMarkupPanel tGtmp = ((annots[a].getEndIndex() < this.tokenPanelAtIndex.size()) ? ((TokenMarkupPanel) this.tokenPanelAtIndex.get(annots[a].getEndIndex())) : null);
						if ((eGtmp == null) && (tGtmp == null)) {}
						else if (eGtmp == tGtmp)
							eGtmp.clean = false; // closing highlight end cap will be removed from middle of this one
						else if ((eGtmp != null) && eGtmp.clean)
							for (int o = eGtmp.getAnchorCount(); o != 0; o--) {
								TokenMarkupPanelObjectTray eGtmpObject = eGtmp.getObjectTrayAt(o-1);
								if (-1 < eGtmpObject.index)
									break; // found token, no need to look any further
								else if (eGtmpObject.index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX) {
									Annotation annot = ((Annotation) eGtmpObject.object);
									if (annots[a].getAnnotationID().equals(annot.getAnnotationID())) {
										eGtmp.clean = false;
										break;
									}
								}
							}
					}
				}
			
			//	collect clean panels
			//	TODO can only do this in absence of upstream token modifications (otherwise indexes will be off) !!!
			//	TODO ==> figure out 'move token indexes by <XYZ>' method ...
			//	TODO ==> ... most likely counting size adjustment in affected panels when marking them as dirty during edits (by whichever means) ...
			//	TODO ==> ... and accumulating start index adjustment across token panels during above 'clean' checks
			//	TODO ==> might well have to make indexes in object trays non-final to allow adjusting
			//	TODO ==> need to do that _before_ and checks related to annotation display mode changes
			for (int p = 0; p < this.tokenPanels.size(); p++) {
				TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(p));
				if (gtmp.clean)
					cleanTokenPanels.add(gtmp);
			}
			if (DEBUG_CONTENT_RENDERING) System.out.println(" - collected " + cleanTokenPanels.size() + " clean token panels (of " + this.tokenPanels.size() + ")");
		}
		
		//	clear overall data structures
		this.tokenPanels.clear();
		this.tokenPanelAtIndex.clear();
		this.tagPanels.clear();
		
		GridBagConstraints gbc = new GridBagConstraints();
		//	TODOne adjust insets to something looking appropriate
		gbc.fill = GridBagConstraints.HORIZONTAL;
//		gbc.insets.left = 0;
//		gbc.insets.right = 0; // extend to right edge, nothing further there
//		gbc.insets.top = 5;
//		gbc.insets.bottom = 5;
		gbc.insets.left = 0;
		gbc.insets.right = 0; // extend to right edge, nothing further there
		gbc.insets.top = 0;
		gbc.insets.bottom = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		//	render content
		QueriableAnnotation[] annots = this.document.getAnnotations();
		if (DEBUG_CONTENT_RENDERING) System.out.println(" - got " + annots.length + " annotations");
		int annotIndex = 0;
		ArrayList openPanels = new ArrayList();
		ArrayList openAnnots = new ArrayList();
		ArrayList openHighlightAnnots = new ArrayList();
		int cleanTokenPanelIndex = 0;
		for (int t = 0; t <= this.document.size(); t++) {
			if (DEBUG_RENDERING_DETAILS) System.out.println(" - doing token " + t);
			JPanel lastPanel;
			boolean addedCleanTokenPanel = false;
			
			//	handle any end tags and closing highlight end caps that need to go before next token:
			if (DEBUG_RENDERING_DETAILS) System.out.println("   - checking " + openAnnots.size() + " open annotations for closing");
			for (int a = openAnnots.size(); a != 0; a--) {
				Annotation annot = ((Annotation) openAnnots.get(a-1));
				if (t < annot.getEndIndex())
					continue;
				if (DEBUG_RENDERING_DETAILS) System.out.println("     checking '" + annot.getType() + "' annotation");
				Character dm = getDetailAnnotationDisplayMode(annot.getType());
				if (dm == DISPLAY_MODE_INVISIBLE)
					continue;
				if (dm == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
					openHighlightAnnots.remove(annot);
					lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
					if (lastPanel instanceof TokenMarkupPanel)
						((TokenMarkupPanel) lastPanel).addAnnotEnd(annot);
					else {
						TokenMarkupPanel gtmp = new TokenMarkupPanel(this.tokenPanels.size());
						gtmp.addAnnotEnd(annot);
						this.tokenPanels.add(gtmp);
						gtmp.spanningHighlightAnnots.addAll(openHighlightAnnots);
						if (lastPanel == null) {
//							gbc.insets.left = 5; // need some left edge if right in main panel
							this.add(gtmp, gbc.clone());
//							gbc.insets.left = 0; // reset left edge
//							gbc.insets.top = 0; // only need top edge on first panel
							gbc.insets.top = 5; // need top edge on all but first panel
							gbc.gridy++;
						}
						else ((AnnotMarkupPanel) lastPanel).addContentPanel(gtmp);
						openPanels.add(0, gtmp);
					}
				}
				else {
					lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
					if (lastPanel instanceof TokenMarkupPanel) {
						((TokenMarkupPanel) lastPanel).layoutContent();
						openPanels.remove(0);
						lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
					}
					AnnotMarkupPanel lastGamp = ((AnnotMarkupPanel) lastPanel);
					if (!lastGamp.annot.getAnnotationID().equals(annot.getAnnotationID()))
						continue; // have to wait for end tag of interleaved annotation
					openPanels.remove(0);
					lastGamp.endTagPanel.index = this.tagPanels.size();
					this.tagPanels.add(lastGamp.endTagPanel);
					lastGamp.layoutContent();
					lastGamp.layoutTags(true, false); // TODOe adjust flags based upon display control ==> we don't even have panel open unless tags showing
					
					//	close annotation panels we had to extend to accommodate interleaved annotations
					while (openPanels.size() != 0) {
						lastGamp = ((AnnotMarkupPanel) openPanels.get(0));
						if (lastGamp.annot.getEndIndex() < t) {
							openPanels.remove(0);
							lastGamp.endTagPanel.index = this.tagPanels.size();
							this.tagPanels.add(lastGamp.endTagPanel);
							lastGamp.layoutContent();
							lastGamp.layoutTags(true, false); // TODOne adjust flags based upon display control ==> we don't even have panel open unless tags showing
						}
						else break;
					}
				}
				openAnnots.remove(a-1); // if we get here, we closed this one
			}
			
			//	end of document, we're done
			if (t == this.document.size())
				break;
			
			//	no need to add tokens if we reused unmodified token panel
			if (addedCleanTokenPanel)
				continue; // start over after reused token panel
			
			//	check for paragraph end tokens
			if (DEBUG_RENDERING_DETAILS) System.out.println("   - checking paragraph end");
			if ((t != 0) && this.document.tokenAt(t-1).hasAttribute(Token.PARAGRAPH_END_ATTRIBUTE)) {
				lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
				if (lastPanel instanceof TokenMarkupPanel) /* only need to remove token panel if not done by closing tag pair above */ {
					((TokenMarkupPanel) lastPanel).layoutContent();
					openPanels.remove(0);
				}
			}
			
			//	add any whitespace that might need adding (only if we don't start with start tag)
			if (DEBUG_RENDERING_DETAILS) System.out.println("   - checking token spacing");
			lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
			if (lastPanel instanceof TokenMarkupPanel) /* only need to add space if we already have open token panel */ {
				boolean addSpace = (t != 0);
				for (int a = annotIndex; a < annots.length; a++) {
					if (t < annots[a].getStartIndex())
						break;
					Character dm = getDetailAnnotationDisplayMode(annots[a].getType());
					if (dm == DISPLAY_MODE_INVISIBLE)
						continue;
					else if (dm == DISPLAY_MODE_SHOW_HIGHLIGHTS)
						break; // adding opening highlight end cap first, we need that space
					else {
						addSpace = false; // adding tags first, no need for any space
						break;
					}
				}
				if (addSpace)
					((TokenMarkupPanel) lastPanel).addWhitespace(this.document.getWhitespaceAfter(t-1));
			}
			
			//	handle any start tags and opening highlight end caps that need to go before next token
			if (DEBUG_RENDERING_DETAILS) System.out.println("   - checking for opening annotations");
			while (annotIndex < annots.length) {
				if (t < annots[annotIndex].getStartIndex())
					break;
				QueriableAnnotation annot = annots[annotIndex++];
				Character dm = getDetailAnnotationDisplayMode(annot.getType());
				if (dm == DISPLAY_MODE_INVISIBLE)
					continue;
				if (dm == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
					lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
					if (lastPanel instanceof TokenMarkupPanel)
						((TokenMarkupPanel) lastPanel).addAnnotStart(annot);
					else {
//						TokenMarkupPanel gtmp = new TokenMarkupPanel(this.tokenPanels.size());
						TokenMarkupPanel gtmp = null;
						while (cleanTokenPanelIndex < cleanTokenPanels.size()) {
							TokenMarkupPanel cGtmp = ((TokenMarkupPanel) cleanTokenPanels.get(cleanTokenPanelIndex));
							if (cGtmp.minTokenIndex < t) /* we've skipped past this one, somehow */ {
								cleanTokenPanelIndex++;
								continue;
							}
							else if (t < cGtmp.minTokenIndex)
								break; // no clean token panel for this start index
							TokenMarkupPanelObjectTray cGtmpStartObjectTray = cGtmp.getObjectTrayAt(0);
							if ((cGtmpStartObjectTray.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) && annot.getAnnotationID().equals(((Annotation) cGtmpStartObjectTray.object).getAnnotationID()))
								gtmp = cGtmp;
							break;
						}
						if (gtmp == null) {
							gtmp = new TokenMarkupPanel(this.tokenPanels.size());
							gtmp.addAnnotStart(annot);
						}
						else {
							gtmp.index = this.tokenPanels.size();
							gtmp.spanningHighlightAnnots.clear();
							addedCleanTokenPanel = true;
						}
						this.tokenPanels.add(gtmp);
						gtmp.spanningHighlightAnnots.addAll(openHighlightAnnots);
						if (lastPanel == null) {
//							gbc.insets.left = 5; // need some left edge if right in main panel
							this.add(gtmp, gbc.clone());
//							gbc.insets.left = 0; // reset left edge
//							gbc.insets.top = 0; // only need top edge on first panel
							gbc.insets.top = 5; // need top edge on all but first panel
							gbc.gridy++;
						}
						else ((AnnotMarkupPanel) lastPanel).addContentPanel(gtmp);
						if (addedCleanTokenPanel) {
							while ((annotIndex < annots.length) && (annots[annotIndex].getStartIndex() <= gtmp.maxTokenIndex))
								annotIndex++; // skip all annotations starting within clean panel (wouldn't be clean if anything changed)
							t = gtmp.maxTokenIndex; // loop increment will take us to start of next panel
							while (this.tokenPanelAtIndex.size() <= t)
								this.tokenPanelAtIndex.add(gtmp);
							if (DEBUG_CONTENT_RENDERING) System.out.println("   - clean token panel jumped to " + t);
							break; // we're done with annotations until end of reused panel
						}
						openPanels.add(0, gtmp); // only need panel on stack if we don't skip right past whole content
					}
					openHighlightAnnots.add(annot);
				}
				else {
					AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annot.getAnnotationID()));
					if (gamp == null) {
						gamp = new AnnotMarkupPanel(annot);
						this.annotIDsToPanels.put(annot.getAnnotationID(), gamp);
					}
					else gamp.clearContentPanels(annot);
					lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
					if (lastPanel instanceof TokenMarkupPanel) {
						((TokenMarkupPanel) lastPanel).layoutContent();
						openPanels.remove(0);
						lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
					}
					if (lastPanel instanceof AnnotMarkupPanel)
						((AnnotMarkupPanel) lastPanel).addContentPanel(gamp);
					else {
//						gbc.insets.left = 5; // need some left edge if right in main panel
						this.add(gamp, gbc.clone());
//						gbc.insets.left = 0; // reset left edge
//						gbc.insets.top = 0; // only need top edge on first panel
						gbc.insets.top = 5; // need top edge on all but first panel
						gbc.gridy++;
					}
					openPanels.add(0, gamp);
					gamp.startTagPanel.index = this.tagPanels.size();
					this.tagPanels.add(gamp.startTagPanel);
				}
				openAnnots.add(annot);
			}
			
			//	no need to add tokens if we reused unmodified token panel
			if (addedCleanTokenPanel)
				continue; // start over after reused token panel
			
			//	add token proper
			if (DEBUG_RENDERING_DETAILS) System.out.println("   - adding token proper");
			lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
			if (lastPanel instanceof TokenMarkupPanel) {
				((TokenMarkupPanel) lastPanel).addToken(this.document.tokenAt(t), t);
				this.tokenPanelAtIndex.add(lastPanel);
			}
			else {
//				TokenMarkupPanel gtmp = new TokenMarkupPanel(this.tokenPanels.size());
				TokenMarkupPanel gtmp = null;
				while (cleanTokenPanelIndex < cleanTokenPanels.size()) {
					TokenMarkupPanel cGtmp = ((TokenMarkupPanel) cleanTokenPanels.get(cleanTokenPanelIndex));
					if (cGtmp.minTokenIndex < t) /* we've skipped past this one, somehow */ {
						cleanTokenPanelIndex++;
						continue;
					}
					else if (t < cGtmp.minTokenIndex)
						break; // no clean token panel for this start index
					TokenMarkupPanelObjectTray cGtmpSObjectTray = cGtmp.getObjectTrayAt(0);
					if (-1 < cGtmpSObjectTray.index)
						gtmp = cGtmp;
					break;
				}
				if (gtmp == null) {
					gtmp = new TokenMarkupPanel(this.tokenPanels.size());
					gtmp.addToken(this.document.tokenAt(t), t);
				}
				else {
					gtmp.index = this.tokenPanels.size();
					gtmp.spanningHighlightAnnots.clear();
					addedCleanTokenPanel = true;
				}
				this.tokenPanels.add(gtmp);
				this.tokenPanelAtIndex.add(gtmp);
				gtmp.spanningHighlightAnnots.addAll(openHighlightAnnots);
//				gtmp.addToken(this.document.tokenAt(t), t);
				if (lastPanel == null) {
//					gbc.insets.left = 5; // need some left edge if right in main panel
					this.add(gtmp, gbc.clone());
//					gbc.insets.left = 0; // reset left edge
//					gbc.insets.top = 0; // only need top edge on first panel
					gbc.insets.top = 5; // need top edge on all but first panel
					gbc.gridy++;
				}
				else ((AnnotMarkupPanel) lastPanel).addContentPanel(gtmp);
				if (addedCleanTokenPanel) {
					while ((annotIndex < annots.length) && (annots[annotIndex].getStartIndex() <= gtmp.maxTokenIndex))
						annotIndex++; // skip all annotations starting within clean panel (wouldn't be clean if anything changed)
					t = gtmp.maxTokenIndex; // loop increment will take us to start of next panel
					while (this.tokenPanelAtIndex.size() <= t)
						this.tokenPanelAtIndex.add(gtmp);
					if (DEBUG_CONTENT_RENDERING) System.out.println("   - clean token panel jumped to " + t);
				}
				else openPanels.add(0, gtmp); // only need panel on stack if we don't skip right past whole content
			}
		}
		
		//	we might have token content panel still open
		if (DEBUG_CONTENT_RENDERING) System.out.println(" - closing " + openAnnots.size() + " open annotations");
		if (openPanels.size() != 0) {
			TokenMarkupPanel tp = ((TokenMarkupPanel) openPanels.remove(0));
			tp.layoutContent();
		}
		if (DEBUG_CONTENT_RENDERING) System.out.println(" - layout completed, got " + this.tokenPanelAtIndex.size() + " token indexes filled with " + this.tokenPanels.size() + " token panels for document sized " + this.document.size());
		
		//	mark content bearing token panels as clean
		int cleanPanelCount = 0;
		for (int p = 0; p < this.tokenPanels.size(); p++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(p));
			if (gtmp.clean) {
				cleanPanelCount++;
				continue; // nothing to check on this one, clean from last round
			}
			if ((gtmp.minTokenIndex == -1) || (gtmp.maxTokenIndex == -1))
				continue; // annotation highlight end caps only, little chance for reuse
			if (gtmp.maxTokenIndex < gtmp.maxTokenIndex)
				continue; // something is weird, be safe
			if (gtmp.incomingAnnotEnds.size() != 0)
				continue; // ends annotation highlights opened in preceding panel, be safe
			if (gtmp.outgoingAnnotStarts.size() != 0)
				continue; // start annotation highlights closed in subsequent panel, be safe
			gtmp.clean = true;
			cleanPanelCount++;
		}
		if (DEBUG_CONTENT_RENDERING) System.out.println(" - got " + cleanPanelCount + " token panels of " + this.tokenPanels.size() + " marked as clean for possible reuse");
		
		//	make changes visible
		this.getLayout().layoutContainer(this);
		this.validate();
		this.repaint();
		
		//	remember clean modification counters
		this.validTokenModCount = this.tokenModCount;
		this.validDetailAnnotModCount = this.detailAnnotModCount;
		this.validDetailAnnotTagModCount = this.detailAnnotTagModCount;
	}
	
	void refreshAnnotStartTags() {
		
		//	update existing tags
		QueriableAnnotation[] annots = this.document.getAnnotations();
		for (int a = 0; a < annots.length; a++) {
			AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annots[a].getAnnotationID()));
			if (gamp != null)
				gamp.validateStartTag();
		}
		
		//	make changes visible
		this.getLayout().layoutContainer(this);
		this.validate();
		this.repaint();
		
		//	remember clean tag modification counter
		this.validDetailAnnotTagModCount = this.detailAnnotTagModCount;
	}
	/*
TODO Speeding up display refresh in GAMTA document display panel:
- when toggling highlights:
- refreshing after atomic action:
  - keep track of added/removed annotations, as well as possible token edits ...
  - ... and flag affected token panels as dirty in modification tracker
    ==> really need that binary search for 'token panel at index' ...
    ==> ... or maybe even sort of B-Tree
    ==> also need to update clean (token) panel boundary indexes and offsets if tokens modified
      ==> make index property of token tray adjustable
  ==> clear 'dirty' flags only at end of layout routine ...
  ==> ... basically stating 'display in sync with data and visualization settings on this interval'
  - most likely use set of to-show annotation types for refresh
- implement different entry points for display refresh ...
- ... performing 'dirty' checks as outlined above ...
- ... and then delegating to actual layout routine to re-assemble and re-index panels ...
- ... using sketched 'reuse as clean' data structures to speed up matters
  ==> diff added annotations with display control settings after running XMT ...
  ==> ... so to hand proper visualization delta to layout routine

TODO Identifying 'clean' (i.e., still-valid) token panels in GAMTA document markup panel rendering:
- on local edits (selection actions), use 'token panel at index' to flag affected panels as dirty ...
- ... and also track any added annotation types for display refresh
  ==> treat same as using XMT
    ==> most likely simply make those local annotation type tracking (counting) sets class properties ...
    ==> ... diff with display settings at end of atomic actions ...
    ==> ... and clear after re-render
      ==> might be viable option in XM document markup panel as well
- handling upstream char sequence and token sequence modifications:
  - increment or decrement char offset and token index displacement when recording edits ...
  - ... updating 'token panel at index' list along when recoding modifications
  - run through token panels before even collecting clean ones ...
  - ... aggregate displacements in running variables ...
  - ... and adjusting stored boundary values along
  ==> give this another good think, with fresher brain ...
  ==> ... especially for handling individual typing events ...
  ==> ... which usually do not modify any annotations
    ==> showing typing input _must_ work on single token panel ...
    ==> ... even if adjusting downstream token panel boundary offsets and indexes in process
    ==> ... and possibly also 'token panel at index' list ...
    ==> ... and in very most cases should not even have to deal with token panel height change
      ==> test how added or removed line wrap behaves in current setup
    ==> use char sequence for typing, and listen out for respective events on typing actions

TODO GAMTA document markup panel, handling tags and highlights:
- keep first and last token index in token panels (latter exclusive) ...
- ... setting both to same value of 'highlight end cap only' token panels
  ==> take first token offset from last token panel end offset
- also, count number of matched and crossed paragraph ends per annotation type ...
- ... and show tags or highlights accordingly on 'ensure annotations visible'
  ==> still allow injecting fixed preferences via mounting point method ...
  ==> ... but use that statistical approach as fallback default
  ==> score 1 for starting right after or ending right before paragraph break ...
  ==> ... and score 3 for being open across paragraph break ...
  ==> ... while also keeping overall count of annotation types ...
  ==> ... and prefer showing tags if score above 1.5 (leaves some room for broken boundaries on structural annotations)
- ALSO, add 'handle keystroke' mounting point method for printable characters and spaces ...
- ... specifying 'Alt', 'Ctrl', and 'Shift' via bit mask (readily available from event) ...
- ... and add getter for current selection (to save creating selection object for each keystroke) ...
- ... OR BETTER, wrap keystroke in (sort of event) object able to generate and return selection on demand
  ==> might be sensible approach for XM document markup panel as well ...
  ==> ... paving way for keyboard shortcuts without having to implement them right away
  ==> might use same approach as for click actions (maybe renaming them to 'instant actions')
  ==> ALSO, still need to add that 'show context menu on selection finished' flag (to both GAMTA and XMF display panels)
	 */
	
	private class AnnotMarkupPanel extends JPanel {
		
		/*final */QueriableAnnotation annot; // cannot make this final, annotation views might expire
		
		private ArrayList contentPanels = new ArrayList();
		private JPanel contentPanel = new JPanel(new GridBagLayout(), true);
		final AnnotTagPanel startTagPanel;
		final AnnotTagPanel endTagPanel;
		private AnnotTagConnectorPanel tagConnectorPanel;
		
		int annotTagModCount = 0;
		int validAnnotTagModCount = 0;
		
		boolean contentFolded = false;
		
		AnnotMarkupPanel(QueriableAnnotation annot) {
			super(new BorderLayout(), true);
			this.annot = annot;
			this.setOpaque(true);
			this.setBackground(tokenBackgroundColor);
			this.startTagPanel = new AnnotTagPanel(this.annot, true, this);
			this.endTagPanel = new AnnotTagPanel(this.annot, false, this);
			this.tagConnectorPanel = new AnnotTagConnectorPanel(this.annot);
			this.contentPanel.setOpaque(true);
			this.contentPanel.setBackground(Color.WHITE);
		}
		
		public String toString() {
			return (super.toString() + ": " + AnnotationUtils.produceStartTag(this.annot, false));
		}
		
		void addContentPanel(JPanel cp) {
			//	TODOnot remember we need to re-do layout in content panel ==> handled externally by calling code
			this.contentPanels.add(cp);
		}
		
		void clearContentPanels(QueriableAnnotation annot) {
			//	TODOnot remember we need to re-do layout in content panel ==> handled externally by calling code
			this.annot = annot;
			this.startTagPanel.annot = annot;
			this.tagConnectorPanel.annot = annot;
			this.endTagPanel.annot = annot;
			this.contentPanels.clear();
		}
		
		void layoutContent() {
			this.contentPanel.removeAll();
			
			GridBagConstraints gbc = new GridBagConstraints();
			//	TODOne adjust insets to something looking appropriate
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets.left = 0;
			gbc.insets.right = 0; // extend to right edge, nothing further there
			gbc.insets.top = 5;
			gbc.insets.bottom = 5;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			
			for (int p = 0; p < this.contentPanels.size(); p++) {
				JPanel cp = ((JPanel) this.contentPanels.get(p));
				this.contentPanel.add(cp, gbc.clone());
				gbc.insets.top = 0; // only need top edge on first panel
				gbc.gridy++;
			}
			
			this.getLayout().layoutContainer(this);
			this.validate();
			this.repaint();
		}
		
		void validateStartTag() {
			if (this.validAnnotTagModCount != this.annotTagModCount) {
				this.startTagPanel.updateTag(this.contentFolded || this.contentPanels.isEmpty());
				this.validAnnotTagModCount = this.annotTagModCount;
			}
		}
		
		void layoutTags(boolean showTags, boolean foldContent) {
			this.removeAll();
			
			if (foldContent || this.contentPanels.isEmpty()) {
				this.startTagPanel.updateTag(true);
				this.add(this.startTagPanel, BorderLayout.NORTH);
				this.contentFolded = true;
			}
			else {
				this.add(this.contentPanel, BorderLayout.CENTER);
				if (showTags) {
					this.startTagPanel.updateTag(false);
					this.add(this.startTagPanel, BorderLayout.NORTH);
					this.endTagPanel.updateTag(false);
					this.add(this.endTagPanel, BorderLayout.SOUTH);
					this.tagConnectorPanel.updateTag();
					this.add(this.tagConnectorPanel, BorderLayout.WEST);
				}
				this.contentFolded = false;
			}
			
			this.getLayout().layoutContainer(this);
			//	TODO maybe add switch flag for these two to speed up changes via display control
			this.validate();
			this.repaint();
		}
		
		void toggleContentFolded() {
			if (this.contentFolded)
				this.layoutTags(true, false);
			else this.layoutTags(true, true);
		}
		
		boolean isFoldedAway() {
			for (Container pComp = this.getParent(); pComp != null; pComp = pComp.getParent()) {
				if ((pComp instanceof AnnotMarkupPanel) && ((AnnotMarkupPanel) pComp).contentFolded)
					return true;
				else if (pComp instanceof GamtaDocumentMarkupPanel)
					return false;
			}
			return true; // if we exit parent panel loop without getting to main panel, we or some parent panel is orphaned right now
		}
		
		void updateAnnotColor() {
			this.startTagPanel.updateAnnotColor();
			this.tagConnectorPanel.updateTag();
			this.endTagPanel.updateAnnotColor();
		}
		
		void updateTagPanelTextSettings() {
			this.startTagPanel.updateTextSettings();
			this.tagConnectorPanel.updateColorSettings();
			this.endTagPanel.updateTextSettings();
			this.setBackground(tokenBackgroundColor);
		}
	}
	
//	private class ParaMarkupPanel extends JPanel {
//		
//		final Paragraph para;
//		
////		private ArrayList contentPanels = new ArrayList();
//		private JPanel contentPanel = new JPanel(new GridBagLayout(), true);
//		final AnnotTagPanel startTagPanel;
//		final AnnotTagPanel endTagPanel;
//		private AnnotTagConnectorPanel tagConnectorPanel;
//		
//		final Map annotsToPanels = Collections.synchronizedMap(new HashMap());
//		final ArrayList tokenPanels = new ArrayList();
//		private ArrayList tagPanels = new ArrayList();
//		
//		int tokenModCount = 0;
//		int validTokenModCount = 0;
//		int detailAnnotModCount = 0;
//		int validDetailAnnotModCount = 0;
//		int detailAnnotTagModCount = 0;
//		int validDetailAnnotTagModCount = 0;
//		
//		int annotTagModCount = 0;
//		int validAnnotTagModCount = 0;
//		
//		boolean contentFolded = false;
//		
//		int index;
//		
//		ParaMarkupPanel(Paragraph para, int index) {
//			super(new BorderLayout(), true);
//			this.para = para;
//			this.index = index;
//			this.setOpaque(true);
//			this.setBackground(tokenTextColor);
//			this.startTagPanel = new AnnotTagPanel(null /* we're structural */, this.para, false, true, this);
//			this.endTagPanel = new AnnotTagPanel(null /* we're structural */, this.para, false, false, this);
//			this.tagConnectorPanel = new AnnotTagConnectorPanel(this.para);
//			this.contentPanel.setOpaque(true);
//			this.contentPanel.setBackground(tokenBackgroundColor);
//		}
//		
//		public String toString() {
//			return (super.toString() + ": " + createStartTag(this.para, false));
//		}
//		
//		void validateStartTag() {
//			if (this.validAnnotTagModCount != this.annotTagModCount) {
//				this.startTagPanel.updateTag(this.contentFolded);
//				this.validAnnotTagModCount = this.annotTagModCount;
//			}
//		}
//		
//		void validateContentPanel() {
//			if ((this.validDetailAnnotModCount != this.detailAnnotModCount) || (this.validTokenModCount != this.tokenModCount))
//				this.layoutTokens();
//			else if (this.validDetailAnnotTagModCount != this.detailAnnotTagModCount) {
//				for (Iterator ait = this.annotsToPanels.keySet().iterator(); ait.hasNext();)
//					((AnnotMarkupPanel) this.annotsToPanels.get(ait.next())).validateStartTag();
//				this.contentPanel.getLayout().layoutContainer(this.contentPanel);
//				this.validate();
//				this.repaint();
//				this.validDetailAnnotTagModCount = this.detailAnnotTagModCount;
//			}
//		}
//		
//		void layoutTokens() {
//			this.contentPanel.removeAll();
//			this.tokenPanels.clear();
//			this.tagPanels.clear();
//			
//			GridBagConstraints gbc = new GridBagConstraints();
//			//	TODOne adjust insets to something looking appropriate
//			gbc.fill = GridBagConstraints.HORIZONTAL;
//			gbc.insets.left = 0;
//			gbc.insets.right = 0; // extend to right edge, nothing further there
//			gbc.insets.top = 5;
//			gbc.insets.bottom = 5;
//			gbc.gridwidth = 1;
//			gbc.gridheight = 1;
//			gbc.weightx = 1;
//			gbc.weighty = 1;
//			gbc.gridx = 0;
//			gbc.gridy = 0;
//			
//			Anchor[] paraAnchors = this.para.getAnchors();
//			ArrayList openPanels = new ArrayList();
//			ArrayList openHighlightAnnots = new ArrayList();
//			for (int a = 0; a < paraAnchors.length; a++) {
//				if (paraAnchors[a].type == Anchor.TYPE_TOKEN_ANCHOR) {
//					JPanel lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
//					if (lastPanel instanceof TokenMarkupPanel)
//						((TokenMarkupPanel) lastPanel).addAnchor(paraAnchors[a]);
//					else {
//						TokenMarkupPanel gtmp = new TokenMarkupPanel(this, this.tokenPanels.size());
//						this.tokenPanels.add(gtmp);
//						gtmp.spanningHighlightAnnots.addAll(openHighlightAnnots);
//						gtmp.addAnchor(paraAnchors[a]);
//						if (lastPanel == null) {
//							this.contentPanel.add(gtmp, gbc.clone());
//							gbc.insets.top = 0; // only need top edge on first panel
//							gbc.gridy++;
//						}
//						else ((AnnotMarkupPanel) lastPanel).addContentPanel(gtmp);
//						openPanels.add(0, gtmp);
//					}
//				}
//				else if (paraAnchors[a].type == Anchor.TYPE_START_ANCHOR) {
//					Annotation annot = paraAnchors[a].getAnnotation();
//					Character dm = getDetailAnnotationDisplayMode(annot.getType());
//					if (dm == DISPLAY_MODE_INVISIBLE)
//						continue;
//					if (dm == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
//						JPanel lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
//						if (lastPanel instanceof TokenMarkupPanel)
//							((TokenMarkupPanel) lastPanel).addAnchor(paraAnchors[a]);
//						else {
//							TokenMarkupPanel gtmp = new TokenMarkupPanel(this, this.tokenPanels.size());
//							this.tokenPanels.add(gtmp);
//							gtmp.spanningHighlightAnnots.addAll(openHighlightAnnots);
//							gtmp.addAnchor(paraAnchors[a]);
//							if (lastPanel == null) {
//								this.contentPanel.add(gtmp, gbc.clone());
//								gbc.insets.top = 0; // only need top edge on first panel
//								gbc.gridy++;
//							}
//							else ((AnnotMarkupPanel) lastPanel).addContentPanel(gtmp);
//							openPanels.add(0, gtmp);
//						}
//						openHighlightAnnots.add(annot);
//					}
//					else {
//						AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotsToPanels.get(annot));
//						if (gamp == null) {
//							gamp = new AnnotMarkupPanel(this, annot);
//							this.annotsToPanels.put(annot, gamp);
//						}
//						else gamp.clearContentPanels();
//						JPanel lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
//						if (lastPanel instanceof TokenMarkupPanel) {
//							((TokenMarkupPanel) lastPanel).layoutContent();
//							openPanels.remove(0);
//							lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
//						}
//						if (lastPanel instanceof AnnotMarkupPanel)
//							((AnnotMarkupPanel) lastPanel).addContentPanel(gamp);
//						else {
//							this.contentPanel.add(gamp, gbc.clone());
//							gbc.insets.top = 0; // only need top edge on first panel
//							gbc.gridy++;
//						}
//						openPanels.add(0, gamp);
//						gamp.startTagPanel.index = this.tagPanels.size();
//						this.tagPanels.add(gamp.startTagPanel);
//					}
//				}
//				else if (paraAnchors[a].type == Anchor.TYPE_END_ANCHOR) {
//					Annotation annot = paraAnchors[a].getAnnotation();
//					Character dm = getDetailAnnotationDisplayMode(annot.getType());
//					if (dm == DISPLAY_MODE_INVISIBLE)
//						continue;
//					if (dm == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
//						openHighlightAnnots.remove(annot);
//						JPanel lastPanel = (openPanels.isEmpty() ? null : ((JPanel) openPanels.get(0)));
//						if (lastPanel instanceof TokenMarkupPanel)
//							((TokenMarkupPanel) lastPanel).addAnchor(paraAnchors[a]);
//						else {
//							TokenMarkupPanel gtmp = new TokenMarkupPanel(this, this.tokenPanels.size());
//							this.tokenPanels.add(gtmp);
//							gtmp.spanningHighlightAnnots.addAll(openHighlightAnnots);
//							gtmp.addAnchor(paraAnchors[a]);
//							if (lastPanel == null) {
//								this.contentPanel.add(gtmp, gbc.clone());
//								gbc.insets.top = 0; // only need top edge on first panel
//								gbc.gridy++;
//							}
//							else ((AnnotMarkupPanel) lastPanel).addContentPanel(gtmp);
//							openPanels.add(0, gtmp);
//						}
//					}
//					else {
//						JPanel lastPanel = ((JPanel) openPanels.get(0));
//						if (lastPanel instanceof TokenMarkupPanel) {
//							((TokenMarkupPanel) lastPanel).layoutContent();
//							openPanels.remove(0);
//							lastPanel = ((JPanel) openPanels.get(0));
//						}
//						AnnotMarkupPanel lastGamp = ((AnnotMarkupPanel) lastPanel);
//						if (lastGamp.annot != annot)
//							continue; // have to wait for end tag of interleaved annotation
//						openPanels.remove(0);
//						lastGamp.endTagPanel.index = this.tagPanels.size();
//						this.tagPanels.add(lastGamp.endTagPanel);
//						lastGamp.layoutContent();
//						lastGamp.layoutTags(true, false); // TODOe adjust flags based upon display control ==> we don't even have panel open unless tags showing
//						
//						//	close annotation panels we had to extend to accommodate interleaved annotations
//						while (openPanels.size() != 0) {
//							lastGamp = ((AnnotMarkupPanel) openPanels.get(0));
//							if (lastGamp.annot.lastToken().getAbsoluteIndex() < annot.lastToken().getAbsoluteIndex()) {
//								openPanels.remove(0);
//								lastGamp.endTagPanel.index = this.tagPanels.size();
//								this.tagPanels.add(lastGamp.endTagPanel);
//								lastGamp.layoutContent();
//								lastGamp.layoutTags(true, false); // TODOne adjust flags based upon display control ==> we don't even have panel open unless tags showing
//							}
//							else break;
//						}
//					}
//				}
//			}
//			
//			//	we might have token content panel still open
//			if (openPanels.size() != 0) {
//				TokenMarkupPanel tpp = ((TokenMarkupPanel) openPanels.remove(0));
//				tp.layoutContent();
//			}
//			
//			this.contentPanel.getLayout().layoutContainer(this.contentPanel);
//			this.validate();
//			this.repaint();
//			
//			this.validTokenModCount = this.tokenModCount;
//			this.validDetailAnnotModCount = this.detailAnnotModCount;
//			this.validDetailAnnotTagModCount = this.detailAnnotTagModCount;
//		}
//		
//		void layoutTags(boolean showTags, boolean foldContent) {
//			this.removeAll();
//			
//			if (foldContent) {
//				this.startTagPanel.updateTag(true);
//				this.add(this.startTagPanel, BorderLayout.NORTH);
//				this.contentFolded = true;
//			}
//			else {
//				this.add(this.contentPanel, BorderLayout.CENTER);
//				if (showTags) {
//					this.startTagPanel.updateTag(false);
//					this.add(this.startTagPanel, BorderLayout.NORTH);
//					this.endTagPanel.updateTag(false);
//					this.add(this.endTagPanel, BorderLayout.SOUTH);
//					this.tagConnectorPanel.updateTag();
//					this.add(this.tagConnectorPanel, BorderLayout.WEST);
//				}
//				this.contentFolded = false;
//			}
//			
//			this.getLayout().layoutContainer(this);
//			//	TODO maybe add switch flag for these two to speed up changes via display control
//			this.validate();
//			this.repaint();
//		}
//		
//		void toggleContentFolded() {
//			if (this.contentFolded)
//				this.layoutTags(true, false);
//			else this.layoutTags(true, true);
//		}
//		
//		boolean isFoldedAway() {
//			for (Container pComp = this.getParent(); pComp != null; pComp = pComp.getParent()) {
//				if ((pComp instanceof AnnotMarkupPanel) && ((AnnotMarkupPanel) pComp).contentFolded)
//					return true;
//				else if (pComp instanceof GamtaDocumentMarkupPanel)
//					return false;
//			}
//			return true; // if we exit parent panel loop without getting to main panel, we or some parent panel is orphaned right now
//		}
//		
//		void updateAnnotColor() {
//			this.startTagPanel.updateAnnotColor();
//			this.tagConnectorPanel.updateTag();
//			this.endTagPanel.updateAnnotColor();
//		}
//		
//		void updateAnnotColor(String type) {
//			if (this.para.getAnnotationCount(type) == 0)
//				return;
//			for (Iterator ait = this.annotsToPanels.keySet().iterator(); ait.hasNext();) {
//				AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotsToPanels.get(ait.next()));
//				if (type.equals(gamp.annot.getType()))
//					gamp.updateAnnotColor();
//			}
//		}
//		
//		void updateTokenPanelTextSettings() {
//			for (int t = 0; t < this.tokenPanels.size(); t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).updateTextSettings();
//			this.setBackground(tokenBackgroundColor);
//			this.contentPanel.setBackground(tokenBackgroundColor);
//		}
//		
//		void updateAnnotTagPanelTextSettings() {
//			for (Iterator ait = this.annotsToPanels.keySet().iterator(); ait.hasNext();)
//				((AnnotMarkupPanel) this.annotsToPanels.get((Annotation) ait.next())).updateTagPanelTextSettings();
//		}
//		
//		void updateTagPanelTextSettings() {
//			this.startTagPanel.updateTextSettings();
//			this.tagConnectorPanel.updateColorSettings();
//			this.endTagPanel.updateTextSettings();
//		}
//		
//		void setTokensSelectedBetween(TokenMarkupPanel firstGtmp, int firstOffset, TokenMarkupPanel lastGtmp, int lastOffset/*, boolean cleanUpAbove, boolean cleanUpBelow*/) {
//			if (firstGtmp.index < lastGtmp.index) {
//				for (int t = 0; t < firstGtmp.index; t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//				firstGtmp.setSelectedDownFrom(firstOffset);
//				for (int t = (firstGtmp.index + 1); t < lastGtmp.index; t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//				lastGtmp.setSelectedDownTo(lastOffset);
//				for (int t = (lastGtmp.index + 1); t < this.tokenPanels.size(); t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//			}
//			else if (lastGtmp.index < firstGtmp.index) {
//				for (int t = (firstGtmp.index + 1); t < this.tokenPanels.size(); t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//				firstGtmp.setSelectedUpFrom(firstOffset);
//				for (int t = (lastGtmp.index + 1); t < firstGtmp.index; t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//				lastGtmp.setSelectedUpTo(lastOffset);
//				for (int t = 0; t < lastGtmp.index; t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//			}
//			else {
//				for (int t = 0; t < firstGtmp.index; t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//				firstGtmp.setSelectedBetween(firstOffset, lastOffset);
//				for (int t = (lastGtmp.index + 1); t < this.tokenPanels.size(); t++)
//					((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//			}
//		}
//		
//		void setTokensSelectedUpTo(TokenMarkupPanel gtmp, int offset/*, boolean cleanUpAbove*/) {
//			for (int t = 0; t < gtmp.index; t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//			for (int t = (gtmp.index + 1); t < this.tokenPanels.size(); t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//			gtmp.setSelectedUpTo(offset);
//		}
//		
//		void setTokensSelectedDownFrom(TokenMarkupPanel gtmp, int offset/*, boolean cleanUpAbove*/) {
//			for (int t = 0; t < gtmp.index; t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//			gtmp.setSelectedDownFrom(offset);
//			for (int t = (gtmp.index + 1); t < this.tokenPanels.size(); t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//		}
//		
//		void setTokensSelectedDownTo(TokenMarkupPanel gtmp, int offset/*, boolean cleanUpBelow*/) {
//			for (int t = 0; t < gtmp.index; t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//			gtmp.setSelectedDownTo(offset);
//			for (int t = (gtmp.index + 1); t < this.tokenPanels.size(); t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//		}
//		
//		void setTokensSelectedUpFrom(TokenMarkupPanel gtmp, int offset/*, boolean cleanUpBelow*/) {
//			gtmp.setSelectedUpFrom(offset);
//			for (int t = 0; t < gtmp.index; t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//			for (int t = (gtmp.index + 1); t < this.tokenPanels.size(); t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).clearSelection();
//		}
//		
//		void setTokensSelected() {
//			for (int t = 0; t < this.tokenPanels.size(); t++)
//				((TokenMarkupPanel) this.tokenPanels.get(t)).setSelected();
//		}
//		
//		void clearTokenSelection(TokenMarkupPanel ntsStartGtmp) {
//			for (int t = 0; t < this.tokenPanels.size(); t++) {
//				TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(t));
//				if (gtmp != ntsStartGtmp) // don't clean up start panel of next selection
//					gtmp.clearSelection();
//			}
//		}
//		
//		void adjustTagSelection(AnnotTagSelection exTagSelection, AnnotTagSelection tagSelection) {
//			updateTagSelection(this.tagPanels, exTagSelection, tagSelection);
//		}
//		
//		void clearTagSelection(AnnotTagPanel startXtap, AnnotTagPanel endXtap, AnnotTagPanel ntsStartGatp) {
//			for (int p = Math.min(startXtap.index, endXtap.index); (p <= Math.max(startXtap.index, endXtap.index)) && (p < this.tagPanels.size()) /* need to catch selection cleanup after annotation removal */; p++) {
//				AnnotTagPanel gatp = ((AnnotTagPanel) this.tagPanels.get(p));
//				if (gatp != ntsStartGatp) // don't clean up start panel of next selection
//					gatp.clearSelection();
//			}
//		}
//		
//		TokenMarkupPanel getPanelFor(Token token) {
//			for (int p = 0; p < this.tokenPanels.size(); p++) {
//				TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(p));
//				if (token.getIndex() <= gtmp.maxTokenIndex)
//					return gtmp;
//			}
//			return null;
//		}
//	}
	
	private class TokenMarkupPanel extends JPanel implements FocusListener, KeyListener {
		final ArrayList objectTrays = new ArrayList();
		final ArrayList spanningHighlightAnnots = new ArrayList();
		
		int charWidth = -1;
		int charHeight = -1;
		JTextArea tokenArea = new JTextArea() {
			public void setFont(Font f) {
				super.setFont(f);
				charWidth = this.getColumnWidth();
				charHeight = this.getRowHeight();
			}
			public void paint(Graphics gr) {
				Color preColor = gr.getColor();
				
				//	paint background
				Dimension size = this.getSize();
				gr.setColor(this.getBackground());
				gr.fillRect(0, 0, size.width, size.height);
				
				//	get offset position width and height
				if (charWidth == -1)
					charWidth = this.getColumnWidth();
				if (charHeight == -1)
					charHeight = this.getRowHeight();
				
				//	paint highlights for spanned annotations
				for (int sa = 0; sa < spanningHighlightAnnots.size(); sa++) {
					Annotation annot = ((Annotation) spanningHighlightAnnots.get(sa));
					Color ahc = getAnnotationHighlightColor(annot.getType(), true);
					gr.setColor(ahc);
					for (int a = 0; a < objectTrayAtOffset.size(); a++) try {
						Rectangle pos = this.modelToView(a);
						pos.width = charWidth;
						gr.fillRect(pos.x, pos.y, pos.width, pos.height);
					}
					catch (BadLocationException ble) {
						System.out.println("BadLocation: " + ble.getMessage());
					}
				}
				
				//	paint annotation highlights
				for (int h = 0; h < annotHighlights.size(); h++) {
					AnnotHighlight ah = ((AnnotHighlight) annotHighlights.get(h));
					Color ahc = getAnnotationHighlightColor(ah.annotType, true);
					gr.setColor(ahc);
					for (int a = (ah.startOffset + 1); a < ah.endOffset; a++) try {
						Rectangle pos = this.modelToView(a);
						pos.width = charWidth;
						gr.fillRect(pos.x, pos.y, pos.width, pos.height);
					}
					catch (BadLocationException ble) {
						System.out.println("BadLocation: " + ble.getMessage());
					}
				}
				
				//	paint highlight end caps
				for (int h = 0; h < annotHighlights.size(); h++) {
					AnnotHighlight ah = ((AnnotHighlight) annotHighlights.get(h));
					Color ac = getAnnotationColor(ah.annotType, true);
					Color ahc = getAnnotationHighlightColor(ah.annotType, true);
					if (-1 < ah.startOffset) try {
						Rectangle pos = this.modelToView(ah.startOffset);
						pos.width = charWidth;
						pos.x++;
						pos.width--;
						gr.setColor(ahc);
						gr.fillRect(pos.x, pos.y, pos.width, pos.height);
						gr.setColor(ac);
						int my = (pos.y + (pos.height / 2));
						int rx = (pos.x + pos.width);
						int by = (pos.y + pos.height);
						gr.drawLine((pos.x + 1), my, (rx - 3), (pos.y + 2));
						gr.drawLine((pos.x + 1), my, (rx - 3), (by - 3));
						gr.drawLine((pos.x + 2), my, (rx - 2), (pos.y + 2));
						gr.drawLine((pos.x + 2), my, (rx - 2), (by - 3));
					}
					catch (BadLocationException ble) {
						System.out.println("BadLocation: " + ble.getMessage());
					}
					if (ah.endOffset < objectTrayAtOffset.size()) try {
						Rectangle pos = this.modelToView(ah.endOffset);
						pos.width = charWidth;
						pos.width--;
						gr.setColor(ahc);
						gr.fillRect(pos.x, pos.y, pos.width, pos.height);
						gr.setColor(ac);
						int my = (pos.y + (pos.height / 2));
						int rx = (pos.x + pos.width);
						int by = (pos.y + pos.height);
						gr.drawLine((pos.x + 1), (pos.y + 2), (rx - 3), my);
						gr.drawLine((pos.x + 1), (by - 3), (rx - 3), my);
						gr.drawLine((pos.x + 2), (pos.y + 2), (rx - 2), my);
						gr.drawLine((pos.x + 2), (by - 3), (rx - 2), my);
					}
					catch (BadLocationException ble) {
						System.out.println("BadLocation: " + ble.getMessage());
					}
				}
				
				//	paint text
				gr.setColor(preColor);
				super.paint(gr);
			}
		};
		private ArrayList objectTrayAtOffset = new ArrayList();
		/*
TODO XM document token panel 'anchor at offset':
- store anchor array used for populating panel
- instead of 'anchor at offset' ArrayList, use 'anchor index at offset' short array
  ==> 2 bytes per character instead of 8
  ==> most likely build dedicated index list to encapsulate both doubling of short array and lookups ...
  ==> ... and hand in anchor array as constructor argument of said index list ...
  ==> ... also providing reset(Anchor[]) method for clearing list on re-layout of panel ...
  ==> ... as well as add(short, int) method adding index n times at once ...
  ==> ... facilitating direct use of Arrays.fill()
==> might be candidate for later (post-deadline) optimization, though
		 */
		private ArrayList annotHighlights = new ArrayList();
		HashSet incomingAnnotEnds = new HashSet();
		HashSet outgoingAnnotStarts = new HashSet();
		
//		final ParaMarkupPanel para;
//		final int index;
		int index; // need to be able to adjust this when reusing token panels on refresh
		boolean clean = false;
		
		int minTokenIndex = -1;
		int maxTokenIndex = -1;
		
		int minTokenStartOffset = -1;
		int maxTokenEndOffset = -1;
		
		TokenMarkupPanel(int index) {
			super(new BorderLayout(), true);
//			this.para = para;
			this.index = index;
			this.tokenArea.setLineWrap(true);
			this.tokenArea.setWrapStyleWord(true);
			this.tokenArea.setEditable(false);
			this.tokenArea.setOpaque(false);
			this.tokenArea.setFont(tokenTextFont);
			this.tokenArea.setForeground(tokenTextColor);
			this.tokenArea.setBackground(tokenBackgroundColor);
			this.tokenArea.setSelectedTextColor(selTokenTextColor);
			this.tokenArea.setSelectionColor(selTokenBackgroundColor);
			this.tokenArea.setCaret(new DefaultCaret() {
				boolean handleDragEventLocally = false;
				public void mousePressed(final MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse pressed at " + me.getWhen());
//					System.out.println(" - " + me.toString());
//					System.out.println(" - popup trigge: " + me.isPopupTrigger());
					if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == 0) // not a left click, don't modify selection
						super.mousePressed(me);
					else if (me.isShiftDown()) {
						if (tokenSelectionStart == TokenMarkupPanel.this)
							super.mousePressed(me);
						gtmpSelectionModified(me, true);
					}
					else {
						super.mousePressed(me);
						gtmpSelectionStarted(me);
					}
				}
				public void mouseReleased(MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse released at " + me.getWhen());
//					System.out.println(" - " + me.toString());
//					System.out.println(" - popup trigge: " + me.isPopupTrigger());
					if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == 0) // not a left click, don't modify selection
						super.mouseReleased(me);
					else {
						if (this.handleDragEventLocally)
							super.mouseReleased(me);
						gtmpSelectionEnded(me, false);
					}
				}
				public void mouseClicked(MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse clicked at " + me.getWhen());
//					System.out.println(" - " + me.toString());
//					System.out.println(" - popup trigge: " + me.isPopupTrigger());
					if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == 0) // not a right click, don't show context menu
						super.mouseClicked(me);
					else gtmpSelectionEnded(me, true); // show context menu
				}
				public void mouseEntered(MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse entered at " + me.getWhen());
					this.handleDragEventLocally = true;
					super.mouseEntered(me);
				}
				public void mouseExited(MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse exited at " + me.getWhen());
					this.handleDragEventLocally = false;
					super.mouseExited(me);
				}
				public void mouseDragged(MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse dragged at " + me.getWhen() + " " + (tokenArea.contains(me.getPoint()) ? " inside" : " outside"));
					if (this.handleDragEventLocally)
						super.mouseDragged(me);
					gtmpSelectionModified(me, false);
				}
//				public void mouseMoved(MouseEvent me) {
//					System.out.println("Token panel " + TokenMarkupPanel.this.para.index + "/" + TokenMarkupPanel.this.index + ": mouse moved at " + me.getWhen());
//					super.mouseMoved(me);
//				}
				protected void adjustVisibility(Rectangle nloc) {
					//	need to deactivate this to make sure cleaning selection doesn't incur scrolling
				}
				public void focusGained(FocusEvent fe) {
					//	need to deactivate this to make sure moving focus to selection end doesn't clear selection
					System.out.println("Token area has focus gained");
				}
				public void focusLost(FocusEvent fe) {
					//	need to deactivate this to make sure moving focus to selection end doesn't clear selection
					System.out.println("Token area has focus lost");
				}
			});
			this.tokenArea.addFocusListener(this);
			this.tokenArea.addKeyListener(this);
			
			this.add(this.tokenArea, BorderLayout.CENTER);
		}
		
		public void focusGained(FocusEvent fe) {
			this.tokenArea.getCaret().setVisible(true);
		}
		public void focusLost(FocusEvent fe) {
			this.tokenArea.getCaret().setVisible(false);
		}
		
		public void keyPressed(KeyEvent ke) {
			try {
				this.doKeyPressed(ke);
			}
			catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}
		private void doKeyPressed(KeyEvent ke) throws BadLocationException {
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
				GamtaDocumentMarkupPanel.this.clearSelections(true); // wherever the jump goes ... 
			else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
				int cp = this.tokenArea.getCaretPosition();
				if (cp == 0) {
					JPanel pp = this.getPrecedingTextPanel(ke.isShiftDown(), null);
					if (pp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) pp);
						int tcp = gtmp.tokenArea.getDocument().getLength();
						if (ke.isShiftDown())
							this.extendSelectionTo(gtmp, tcp, ke);
						else this.moveCaretTo(gtmp, tcp, ke);
					}
					else if (pp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) pp);
						int tcp = gatp.tagArea.getDocument().getLength();
						this.moveCaretTo(gatp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					int tcp = computeNextLeftwardCaretPosition(this.tokenArea, cp, ke.isControlDown(), ((ke.isControlDown() && ke.isShiftDown()) ? this.tokenArea.getCaret().getMark() : cp));
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
				int cp = this.tokenArea.getCaretPosition();
				if (cp == this.tokenArea.getDocument().getLength()) {
					JPanel fp = this.getFollowingTextPanel(ke.isShiftDown(), null);
					if (fp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) fp);
						int tcp = 0;
						if (ke.isShiftDown())
							this.extendSelectionTo(gtmp, tcp, ke);
						else this.moveCaretTo(gtmp, tcp, ke);
					}
					else if (fp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) fp);
						int tcp = 0;
						this.moveCaretTo(gatp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					int tcp = computeNextRightwardCaretPosition(this.tokenArea, cp, ke.isControlDown(), ((ke.isControlDown() && ke.isShiftDown()) ? this.tokenArea.getCaret().getMark() : cp));
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_UP) {
				int cp = this.tokenArea.getCaretPosition();
				Rectangle cPos = this.tokenArea.modelToView(cp);
				Point tcPos = new Point(cPos.x, (cPos.y - (this.charHeight / 2))); // half a line height above top of current position
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going up one line from offset " + cp + " at " + cPos + " to " + tcPos);
				if (tcPos.y < 0) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel above");
					JPanel pp = this.getPrecedingTextPanel(ke.isShiftDown(), null);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> preceding panel is " + pp);
					if (pp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) pp);
						if (ke.isControlDown())
							tcPos.y = (gtmp.charHeight / 2); // half line height below top edge of token area
						else {
							Dimension tTas = gtmp.tokenArea.getSize();
							tcPos.y = (tTas.height - (gtmp.charHeight / 2)); // half line height above bottom edge of token area
						}
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gtmp, tcp, ke);
						else this.moveCaretTo(gtmp, tcp, ke);
					}
					else if (pp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) pp);
//						if (ke.isControlDown())
//							tcPos.y = (gatp.charHeight / 2); // half line height below top edge of tag area
//						else {
							Dimension tTas = gatp.tagArea.getSize();
							tcPos.y = (tTas.height - (gatp.charHeight / 2)); // half line height above bottom edge of tag area
//						}
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gatp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going up one row");
					if (ke.isControlDown())
						tcPos.y = (this.charHeight / 2); // half line height below top edge of token area
					int tcp = this.tokenArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
				int cp = this.tokenArea.getCaretPosition();
				Rectangle cPos = this.tokenArea.modelToView(cp);
				Point tcPos = new Point(cPos.x, (cPos.y + cPos.height + (this.charHeight / 2))); // half a line height below bottom of current position
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going down one line from offset " + cp + " at " + cPos + " to " + tcPos);
				Dimension tas = this.tokenArea.getSize();
				if (tas.height < tcPos.y) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel below");
					JPanel fp = this.getFollowingTextPanel(ke.isShiftDown(), null);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> following panel is " + fp);
					if (fp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) fp);
						if (ke.isControlDown()) {
							Dimension tTas = gtmp.tokenArea.getSize();
							tcPos.y = (tTas.height - (gtmp.charHeight / 2)); // half line height above bottom edge of token area
						}
						else tcPos.y = (gtmp.charHeight / 2); // half line height below top edge of token area
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gtmp, tcp, ke);
						else this.moveCaretTo(gtmp, tcp, ke);
					}
					else if (fp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) fp);
						/*if (ke.isControlDown()) {
							Dimension tTas = gatp.tagArea.getSize();
							tcPos.y = (tTas.height - (gatp.charHeight / 2)); // half line height above bottom edge of tag area
						}
						else */tcPos.y = (gatp.charHeight / 2); // half line height below top edge of token area
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gatp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going down one row");
					if (ke.isControlDown())
						tcPos.y = (tas.height - (this.charHeight / 2)); // half line height above bottom edge of token area
					int tcp = this.tokenArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_HOME) {
				int cp = this.tokenArea.getCaretPosition();
				int tcp;
				if (ke.isControlDown())
					tcp = 0; // start at start of token area proper
				else {
					Rectangle cPos = this.tokenArea.modelToView(cp);
					Point tcPos = new Point(0, cPos.y);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going to start of line from offset " + cp + " at " + cPos + " to " + tcPos);
					tcp = this.tokenArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
				}
				while (tcp < this.objectTrayAtOffset.size()) {
					TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(tcp));
					if (objectTray == null)
						tcp++; // skip over space
					else if (objectTray.index < 0)
						tcp++; // skip over annotation highlight end cap
					else break; // found token
				}
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> actually going to start of token at " + tcp);
				if (ke.isShiftDown())
					this.extendSelectionTo(this, tcp, ke);
				else this.moveCaretTo(this, tcp, ke);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_END) {
				int cp = this.tokenArea.getCaretPosition();
				int tcp;
				if (ke.isControlDown())
					tcp = this.objectTrayAtOffset.size(); // start at end of token area proper
				else {
					Rectangle cPos = this.tokenArea.modelToView(cp);
					Point tcPos = new Point((this.tokenArea.getWidth() - 1), cPos.y);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going to end of line from offset " + cp + " at " + cPos + " to " + tcPos);
					tcp = this.tokenArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
				}
				while (0 < tcp) {
					TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(tcp - 1));
					if (objectTray == null)
						tcp--; // skip over space
					else if (objectTray.index < 0)
						tcp--; // skip over annotation highlight end cap
					else break; // found token
				}
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> actually going to end of token at " + tcp);
				if (ke.isShiftDown())
					this.extendSelectionTo(this, tcp, ke);
				else this.moveCaretTo(this, tcp, ke);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP) {
				if (xdvp == null) /* not much we can do without knowing page height ... */ {
					ke.consume();
					return;
				}
				Rectangle vPos = xdvp.getViewRect();
				int cp = this.tokenArea.getCaretPosition();
				Rectangle rcPos = this.tokenArea.modelToView(cp);
				Rectangle aPos = getRelativePositionOf(this);
				Rectangle acPos = new Rectangle(rcPos);
				acPos.x += aPos.x;
				acPos.y += aPos.y;
				Point atcPos = new Point(acPos.x, ((acPos.y + (this.charHeight / 2)) - vPos.height)); // one screen height up, middle of line
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going up one screen from offset " + cp + " at " + acPos + " to " + atcPos);
				if (atcPos.y < aPos.y) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel above");
					JPanel pp = this.getPrecedingTextPanel(ke.isShiftDown(), atcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> preceding panel is " + pp);
					if (pp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) pp);
						Rectangle tPos = getRelativePositionOf(gtmp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gtmp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gtmp.charHeight / 2));
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gtmp, tcp, ke);
						else this.moveCaretTo(gtmp, tcp, ke);
					}
					else if (pp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) pp);
						Rectangle tPos = getRelativePositionOf(gatp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gatp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gatp.charHeight / 2));
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gatp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					Rectangle tPos = aPos;
					Point tcPos = new Point(atcPos);
					tcPos.x -= tPos.x;
					tcPos.y -= tPos.y;
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going up one screen");
					int tcp = this.tokenArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
				if (xdvp == null) /* not much we can do without knowing page height ... */ {
					ke.consume();
					return;
				}
				Rectangle vPos = xdvp.getViewRect();
				int cp = this.tokenArea.getCaretPosition();
				Rectangle rcPos = this.tokenArea.modelToView(cp);
				Rectangle aPos = getRelativePositionOf(this);
				Rectangle acPos = new Rectangle(rcPos);
				acPos.x += aPos.x;
				acPos.y += aPos.y;
				Point atcPos = new Point(acPos.x, ((acPos.y + (this.charHeight / 2)) + vPos.height)); // one screen height down, middle of line
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going down one screen from offset " + cp + " at " + acPos + " to " + atcPos);
				if ((aPos.y + aPos.height) < atcPos.y) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel below");
					JPanel fp = this.getFollowingTextPanel(ke.isShiftDown(), atcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> following panel is " + fp);
					if (fp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) fp);
						Rectangle tPos = getRelativePositionOf(gtmp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gtmp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gtmp.charHeight / 2));
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gtmp, tcp, ke);
						else this.moveCaretTo(gtmp, tcp, ke);
					}
					else if (fp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) fp);
						Rectangle tPos = getRelativePositionOf(gatp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gatp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gatp.charHeight / 2));
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gatp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					Rectangle tPos = aPos;
					Point tcPos = new Point(atcPos);
					tcPos.x -= tPos.x;
					tcPos.y -= tPos.y;
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going down one screen");
					int tcp = this.tokenArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
				int cp = this.tokenArea.getCaretPosition();
				Rectangle cPos = this.tokenArea.modelToView(cp);
				Point cmPos = new Point(cPos.x, (cPos.y + (this.charHeight / 2))); // middle of current line
				tokenSelectionEnded(this, cp, true, this, cmPos, null);
			}
			else if ((ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (ke.getKeyCode() == KeyEvent.VK_DELETE))
				handleKeystroke(this, ke);
			else if ((ke.getKeyCode() == KeyEvent.VK_TAB) || (ke.getKeyCode() == KeyEvent.VK_ENTER))
				handleKeystroke(this, ke);
			else if (ke.isControlDown() && (KeyEvent.VK_A <= ke.getKeyCode()) && (ke.getKeyCode() <= KeyEvent.VK_Z)) /* need to handle 'Ctrl+<letter>' this tay, as key char comes out as control character on 'typed' events */ {
				char kteChar = ((char) (((int) 'A') + (ke.getKeyCode() - KeyEvent.VK_A))); // all this relies upon is for A-Z codes being continuous sequence in correct order
//				KeyEvent kte = new KeyEvent(ke.getComponent(), KeyEvent.KEY_TYPED /* only way we can communicate actual character */, ke.getWhen(), ke.getModifiers(), KeyEvent.VK_UNDEFINED /* no other choice than this for 'typed' events */, kteChar, ke.getKeyLocation());
				KeyEvent kte = new KeyEvent(ke.getComponent(), KeyEvent.KEY_TYPED /* only way we can communicate actual character */, ke.getWhen(), ke.getModifiers(), KeyEvent.VK_UNDEFINED /* no other choice than this for 'typed' events */, kteChar, KeyEvent.KEY_LOCATION_UNKNOWN /* no other choice than this for 'typed' events */);
				handleKeystroke(this, kte);
				ke.consume(); // consume this one, we just generate our own messenger object for internal use
			}
			else ke.consume();
		}
		public void keyReleased(KeyEvent ke) {
			if (ke.getKeyCode() == KeyEvent.VK_LEFT)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_UP)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_HOME)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_END)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
				ke.consume();
			else if ((ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (ke.getKeyCode() == KeyEvent.VK_DELETE))
				ke.consume();
			else if ((ke.getKeyCode() == KeyEvent.VK_TAB) || (ke.getKeyCode() == KeyEvent.VK_ENTER))
				ke.consume();
			else if (ke.isControlDown() && (KeyEvent.VK_A <= ke.getKeyCode()) && (ke.getKeyCode() <= KeyEvent.VK_Z)) // need to handle 'Ctrl+<letter>' this tay, as key char comes out as control character on 'typed' events
				ke.consume();
			//	TODO do we need to consume events we consumed the 'key pressed' for ???
		}
		public void keyTyped(KeyEvent ke) {
			try {
				this.doKeyTyped(ke);
			}
			catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}
		/* keys (apparently) executing on 'pressed' event:
		 *   - backspace, delete
		 *   - return, tab
		 *   - arrow keys
		 *   - page up, page down
		 *   - home, end
		 */
		private void doKeyTyped(KeyEvent ke) throws BadLocationException {
			if ((ke.getKeyChar() == 0x0008 /* backspace */) || (ke.getKeyChar() == 0x007F /* delete */))
				ke.consume(); // these execute on 'key pressed' above, but still generate typing event
			else if ((ke.getKeyChar() == 0x0009 /* (horizontal) tab */) || (ke.getKeyChar() == 0x000A /* enter (line feed) */) || (ke.getKeyChar() == 0x000D /* enter (carriage return) */))
				ke.consume(); // these execute on 'key pressed' above, but still generate typing event
			else if (ke.isControlDown() && (ke.getKeyChar() < 0x0020)) // 'Ctrl+<letter>' handled above, as key char comes out as control character on 'typed' events
				ke.consume(); // these execute on 'key pressed' above, but still generate typing event
			//	no need to handle arrow keys, 'page up' or 'page down', 'home' or 'end', as those don't create typing events at all
			else handleKeystroke(this, ke);
		}
		private void moveCaretTo(TokenMarkupPanel gtmp, int cp, KeyEvent ke) {
			gtmp.tokenArea.setCaretPosition(cp);
			clearSelections(true);
			if (gtmp != this)
				gtmp.tokenArea.requestFocusInWindow();
			//	_need_to_ start selection right here, as might be extended via shift click
			tokenSelectionStarted(gtmp, cp);
			ke.consume();
			if ((gtmp != this) && (xdvp != null))
				xdvp.moveIntoView(gtmp, null, dummyDisplayAdjustmentObserver);
		}
		private void moveCaretTo(AnnotTagPanel gatp, int cp, KeyEvent ke) {
			gatp.tagArea.setCaretPosition(cp);
			clearSelections(true);
			gatp.tagArea.requestFocusInWindow();
			//	_need_to_ start selection right here, as might be extended via shift click
			tagSelectionStarted(gatp, cp);
			ke.consume();
			if (xdvp != null)
				xdvp.moveIntoView(gatp, null, dummyDisplayAdjustmentObserver);
		}
		private void extendSelectionTo(TokenMarkupPanel gtmp, int cp, KeyEvent ke) {
			if (gtmp == this)
				this.tokenArea.moveCaretPosition(cp);
			else gtmp.tokenArea.setCaretPosition(cp);
			if (gtmp != this)
				gtmp.tokenArea.requestFocusInWindow();
			tokenSelectionModified(gtmp, cp);
			ke.consume();
			if ((gtmp != this) && (xdvp != null))
				xdvp.moveIntoView(gtmp, null, dummyDisplayAdjustmentObserver);
		}
		
		private JPanel getPrecedingTextPanel(boolean forSelection, Point atcPos) {
			FocusTraversalPolicy ftp = GamtaDocumentMarkupPanel.this.getFocusTraversalPolicy();
			if (ftp == null)
				ftp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
			Component pfComp = this.tokenArea;
			Rectangle cPos = getRelativePositionOf(this);
			int rounds = 0;
			do {
				rounds++;
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Preceding text panel, round " + rounds);
				pfComp = ftp.getComponentBefore(GamtaDocumentMarkupPanel.this, pfComp);
				if (!(pfComp instanceof JTextArea))
					continue;
				Container pComp = pfComp.getParent();
				if (pComp instanceof TokenMarkupPanel) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((cPos.y + cPos.height) <= pPos.y)
						return null; //	return null if 'previous' component below us
					if ((atcPos != null) && (atcPos.y < pPos.y))
						continue; // too low for page-up
					return ((TokenMarkupPanel) pComp);
				}
				else if (!forSelection && (pComp instanceof AnnotTagPanel)) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((cPos.y + cPos.height) <= pPos.y)
						return null; //	return null if 'previous' component below us
					if ((atcPos != null) && (atcPos.y < pPos.y))
						continue; // too low for page-up
					return ((AnnotTagPanel) pComp);
				}
			} while (true);
		}
		private JPanel getFollowingTextPanel(boolean forSelection, Point atcPos) {
			FocusTraversalPolicy ftp = GamtaDocumentMarkupPanel.this.getFocusTraversalPolicy();
			if (ftp == null)
				ftp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
			Component pfComp = this.tokenArea;
			Rectangle cPos = getRelativePositionOf(this);
			int rounds = 0;
			do {
				rounds++;
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Following text panel, round " + rounds);
				pfComp = ftp.getComponentAfter(GamtaDocumentMarkupPanel.this, pfComp);
				if (!(pfComp instanceof JTextArea))
					continue;
				Container pComp = pfComp.getParent();
				if (pComp instanceof TokenMarkupPanel) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((pPos.y + pPos.height) <= cPos.y)
						return null; // return null if 'next' component above us
					if ((atcPos != null) && ((pPos.y + pPos.height) < atcPos.y))
						continue; // too high for page-down
					return ((TokenMarkupPanel) pComp);
				}
				else if (!forSelection && (pComp instanceof AnnotTagPanel)) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((pPos.y + pPos.height) <= cPos.y)
						return null; // return null if 'next' component above us
					if ((atcPos != null) && ((pPos.y + pPos.height) < atcPos.y))
						continue; // too high for page-down
					return ((AnnotTagPanel) pComp);
				}
			} while (true);
		}
		
		void updateTextSettings() {
			this.tokenArea.setFont(tokenTextFont);
			this.tokenArea.setForeground(tokenTextColor);
			this.tokenArea.setBackground(tokenBackgroundColor);
			this.tokenArea.setSelectedTextColor(selTokenTextColor);
			this.tokenArea.setSelectionColor(selTokenBackgroundColor);
		}
		
		void gtmpSelectionStarted(MouseEvent me) {
			int meOffset = this.tokenArea.viewToModel(me.getPoint());
			tokenSelectionStarted(this, meOffset);
		}
		
		void gtmpSelectionModified(MouseEvent me, boolean isShiftPress) {
			if (isShiftPress || this.tokenArea.contains(me.getPoint())) {
				int meOffset = this.tokenArea.viewToModel(me.getPoint());
				tokenSelectionModified(this, meOffset);
			}
			else {
				Point mep = me.getPoint(); // relative to token area proper
				for (Component pComp = this.tokenArea; pComp != null; pComp = pComp.getParent()) {
					if (pComp == GamtaDocumentMarkupPanel.this)
						break; // we've come up far enough, no point in going beyond main panel
					Point cp = pComp.getLocation();
					mep.x += cp.x;
					mep.y += cp.y;
				}
				Component meComp = GamtaDocumentMarkupPanel.this;
				for (Component cComp = meComp.getComponentAt(mep.x, mep.y); cComp != null;) {
					if (cComp == meComp)
						break; // no use going any deeper
					meComp = cComp;
					Point cp = cComp.getLocation();
					mep.x -= cp.x;
					mep.y -= cp.y;
					if (meComp instanceof TokenMarkupPanel)
						break; // no need to descend any further
					cComp = meComp.getComponentAt(mep.x, mep.y);
				}
				if (meComp instanceof TokenMarkupPanel) {
					TokenMarkupPanel meGtmp = ((TokenMarkupPanel) meComp);
					Point tap = meGtmp.tokenArea.getLocation();
					mep.x -= tap.x;
					mep.y -= tap.y;
					int meOffset = meGtmp.tokenArea.viewToModel(mep);
					tokenSelectionModified(meGtmp, meOffset);
				}
			}
		}
		
		void gtmpSelectionEnded(MouseEvent me, boolean checkPos) {
			int meOffset = this.tokenArea.viewToModel(me.getPoint());
			if (checkPos) try {
				System.out.println("Got offset " + meOffset + " for mouse event at " + me.getX() + "/" + me.getY());
				Rectangle meOffsetPos = this.tokenArea.modelToView(meOffset);
				System.out.println(" ==> got position " + meOffsetPos + " for offset " + meOffset);
				if (me.getX() < (meOffsetPos.x - this.charWidth))
					return;
				if ((meOffsetPos.x + this.charWidth + this.charWidth) < me.getX())
					return;
				if (me.getY() < (meOffsetPos.y - (this.charHeight / 2)))
					return;
				if ((meOffsetPos.y + meOffsetPos.height + (this.charHeight / 2)) < me.getY())
					return;
			} catch (BadLocationException ble) {}
			tokenSelectionEnded(this, meOffset, me);
		}
		
		void setSelectedBetween(int firstOffset, int lastOffset) {
			this.tokenArea.getCaret().setDot(firstOffset);
			this.tokenArea.getCaret().moveDot(lastOffset);
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedUpTo(int offset) {
			this.tokenArea.getCaret().setDot(this.tokenArea.getDocument().getLength());
			this.tokenArea.getCaret().moveDot(offset);
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedDownFrom(int offset) {
			this.tokenArea.getCaret().setDot(offset);
			this.tokenArea.getCaret().moveDot(this.tokenArea.getDocument().getLength());
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedDownTo(int offset) {
			this.tokenArea.getCaret().setDot(0);
			this.tokenArea.getCaret().moveDot(offset);
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedUpFrom(int offset) {
			this.tokenArea.getCaret().setDot(offset);
			this.tokenArea.getCaret().moveDot(0);
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelected() {
			this.tokenArea.getCaret().setDot(0);
			this.tokenArea.getCaret().moveDot(this.tokenArea.getDocument().getLength());
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void showSelection() {
			this.tokenArea.getCaret().setSelectionVisible(true);
		}
		
		void clearSelection() {
			this.tokenArea.getCaret().setDot(this.tokenArea.getCaret().getMark()); // set caret back to right where we started
			this.tokenArea.getCaret().setSelectionVisible(false);
		}
		
		public String toString() {
			return (super.toString() + ": " + this.tokenArea.getText());
		}
		
		void addToken(Token token, int index) {
			this.objectTrays.add(new TokenMarkupPanelObjectTray(token, index));
		}
		void addWhitespace(String space) {
			this.objectTrays.add(new TokenMarkupPanelObjectTray(space, TokenMarkupPanelObjectTray.WHITESPACE_INDEX));
		}
		void addAnnotStart(Annotation annot) {
			this.objectTrays.add(new TokenMarkupPanelObjectTray(annot, TokenMarkupPanelObjectTray.ANNOT_START_INDEX));
			this.outgoingAnnotStarts.add(annot.getAnnotationID());
		}
		void addAnnotEnd(Annotation annot) {
			this.objectTrays.add(new TokenMarkupPanelObjectTray(annot, TokenMarkupPanelObjectTray.ANNOT_END_INDEX));
			if (this.outgoingAnnotStarts.contains(annot.getAnnotationID()))
				this.outgoingAnnotStarts.remove(annot.getAnnotationID());
			else this.incomingAnnotEnds.add(annot.getAnnotationID());
		}
		
		TokenMarkupPanelObjectTray getObjectTrayAt(int offset) {
			if (offset < 0)
				return null;
			if (this.objectTrayAtOffset.size() <= offset)
				return null;
			return ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(offset));
		}
		
		int getAnchorCount() {
			return this.objectTrayAtOffset.size();
		}
		
		int getFirstAnchorOffsetOf(int tokenIndex) {
			if (tokenIndex < this.minTokenIndex)
				return -1;
			if (this.maxTokenIndex < tokenIndex)
				return -1;
			//	TODO use binary search here
			for (int a = 0; a < this.objectTrayAtOffset.size(); a++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(a));
				if (objectTray == null)
					continue; // we hit a space
				if (objectTray.index == tokenIndex)
					return a;
			}
			return -1;
		}
		int getFirstAnchorOffsetOf(Token token) {
			if (token.getEndOffset() < this.minTokenStartOffset)
				return -1;
			if (this.maxTokenEndOffset < token.getStartOffset())
				return -1;
			//	TODO use binary search here
			for (int a = 0; a < this.objectTrayAtOffset.size(); a++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(a));
				if (objectTray == null)
					continue; // we hit a space
				if ((-1 < objectTray.index) && (((Token) objectTray.object).getStartOffset() == token.getStartOffset()) && (((Token) objectTray.object).getEndOffset() == token.getEndOffset()))
					return a;
			}
			return -1;
		}
		
		int getLastAnchorOffsetOf(int tokenIndex) {
			if (tokenIndex < this.minTokenIndex)
				return -1;
			if (this.maxTokenIndex < tokenIndex)
				return -1;
			for (int a = (this.objectTrayAtOffset.size() - 1); a != -1; a--) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(a));
				if (objectTray == null)
					continue; // we hit a space
				if (objectTray.index == tokenIndex)
					return a;
			}
			return -1;
		}
		int getLastAnchorOffsetOf(Token token) {
			if (token.getEndOffset() < this.minTokenStartOffset)
				return -1;
			if (this.maxTokenEndOffset < token.getStartOffset())
				return -1;
			for (int a = (this.objectTrayAtOffset.size() - 1); a != -1; a--) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(a));
				if (objectTray == null)
					continue; // we hit a space
				if ((-1 < objectTray.index) && (((Token) objectTray.object).getStartOffset() == token.getStartOffset()) && (((Token) objectTray.object).getEndOffset() == token.getEndOffset()))
					return a;
			}
			return -1;
		}
		
		Rectangle getPositionOf(int tokenIndex) {
			int firstOffset = -1;
			for (int t = 0; t < this.objectTrayAtOffset.size(); t++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(t));
				if (objectTray == null)
					continue; // we hit a space
				if (objectTray.index == tokenIndex) {
					firstOffset = t;
					break;
				}
			}
			if (firstOffset == -1)
				return null;
			int lastOffset = (firstOffset + 1);
			for (int t = (firstOffset + 1); t < this.objectTrayAtOffset.size(); t++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(t));
				if (objectTray == null)
					break; // we hit a space after the target token
				if (objectTray.index == tokenIndex)
					lastOffset = (t + 1); // still same token, keep going
				else break;
			}
			try {
				Rectangle firstPos = this.tokenArea.modelToView(firstOffset);
				Rectangle lastPos = this.tokenArea.modelToView(lastOffset);
				return firstPos.union(lastPos);
			} catch (BadLocationException ble) { /* not happening, but Java is dumb */ }
			return null;
		}
		
		Rectangle getPositionOf(Token token) {
			int firstOffset = -1;
			for (int t = 0; t < this.objectTrayAtOffset.size(); t++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(t));
				if (objectTray == null)
					continue; // we hit a space
				if ((-1 < objectTray.index) && (objectTray.object == token)) {
					firstOffset = t;
					break;
				}
			}
			if (firstOffset == -1)
				return null;
			int lastOffset = (firstOffset + 1);
			for (int t = (firstOffset + 1); t < this.objectTrayAtOffset.size(); t++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(t));
				if (objectTray == null)
					break; // we hit a space after the target token
				if ((-1 < objectTray.index) && (objectTray.object == token))
					lastOffset = (t + 1); // still same token, keep going
				else break;
			}
			try {
				Rectangle firstPos = this.tokenArea.modelToView(firstOffset);
				Rectangle lastPos = this.tokenArea.modelToView(lastOffset);
				return firstPos.union(lastPos);
			} catch (BadLocationException ble) { /* not happening, but Java is dumb */ }
			return null;
		}
		
		int getStartAnchorOffsetOf(Annotation annot) {
			//	TODO use binary search here, using index of first token
			for (int t = 0; t < this.objectTrayAtOffset.size(); t++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(t));
				if (objectTray == null)
					continue; // we hit a space
//				if ((objectTray.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) && (objectTray.object == annot))
//					return t;
				if (objectTray.index != TokenMarkupPanelObjectTray.ANNOT_START_INDEX) {
					if (-1 < objectTray.index) // skip tokens as whole (moving loop index to last character of token)
						t += (((Token) objectTray.object).length() - 1 /* last character gets skipped over by loop increment */);
					continue;
				}
				if (objectTray.object == annot)
					return t;
				if (((Annotation) objectTray.object).getStartIndex() != annot.getStartIndex())
					continue;
				if (((Annotation) objectTray.object).getEndIndex() != annot.getEndIndex())
					continue;
				if (((Annotation) objectTray.object).getAnnotationID().equals(annot.getAnnotationID()))
					return t;
			}
			return -1;
		}
		int getEndAnchorOffsetOf(Annotation annot) {
			//	TODO use binary search here, using index of last token
			for (int t = (this.objectTrayAtOffset.size() - 1); t != -1; t--) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrayAtOffset.get(t));
				if (objectTray == null)
					continue; // we hit a space
//				if ((objectTray.index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX) && (objectTray.object == annot))
//					return t;
				if (objectTray.index != TokenMarkupPanelObjectTray.ANNOT_END_INDEX) {
					if (-1 < objectTray.index) // skip tokens as whole (moving loop index to first character of token)
						t -= (((Token) objectTray.object).length() - 1 /* first character gets skipped over by loop decrement */);
					continue;
				}
				if (objectTray.object == annot)
					return t;
				if (((Annotation) objectTray.object).getStartIndex() != annot.getStartIndex())
					continue;
				if (((Annotation) objectTray.object).getEndIndex() != annot.getEndIndex())
					continue;
				if (((Annotation) objectTray.object).getAnnotationID().equals(annot.getAnnotationID()))
					return t;
			}
			return -1;
		}
		Rectangle getStartAnchorPositionOf(Annotation annot) {
			int startAnchorOffset = this.getStartAnchorOffsetOf(annot);
			if (startAnchorOffset == -1)
				return null;
			try {
				Rectangle firstPos = this.tokenArea.modelToView(startAnchorOffset);
				Rectangle lastPos = this.tokenArea.modelToView(startAnchorOffset + 1);
				return firstPos.union(lastPos);
			} catch (BadLocationException ble) { /* not happening, but Java is dumb */ }
			return null;
		}
		Rectangle getEndAnchorPositionOf(Annotation annot) {
			int endAnchorOffset = this.getEndAnchorOffsetOf(annot);
			if (endAnchorOffset == -1)
				return null;
			try {
				Rectangle firstPos = this.tokenArea.modelToView(endAnchorOffset);
				Rectangle lastPos = this.tokenArea.modelToView(endAnchorOffset + 1);
				return firstPos.union(lastPos);
			} catch (BadLocationException ble) { /* not happening, but Java is dumb */ }
			return null;
		}
		
		boolean isFoldedAway() {
			for (Container pComp = this.getParent(); pComp != null; pComp = pComp.getParent()) {
				if ((pComp instanceof AnnotMarkupPanel) && ((AnnotMarkupPanel) pComp).contentFolded)
					return true;
				else if (pComp instanceof GamtaDocumentMarkupPanel)
					return false;
			}
			return true; // if we exit parent panel loop without getting to main panel, we or some parent panel is orphaned right now
		}
		
		void layoutContent() {
			this.tokenArea.setText("");
			this.objectTrayAtOffset.clear();
			this.annotHighlights.clear();
			this.minTokenIndex = -1;
			this.maxTokenIndex = -1;
			this.minTokenStartOffset = -1;
			this.maxTokenEndOffset = -1;
			
			//	add text and annotation highlight symbols
			StringBuffer text = new StringBuffer();
			Token token = null;
			HashMap annotsToHighlights = new HashMap();
			for (int t = 0; t < this.objectTrays.size(); t++) {
				TokenMarkupPanelObjectTray objectTray = ((TokenMarkupPanelObjectTray) this.objectTrays.get(t));
				if (-1 < objectTray.index) {
					token = ((Token) objectTray.object);
					String value = token.getValue();
					for (int c = 0; c < value.length(); c++) {
						text.append(value.charAt(c));
						this.objectTrayAtOffset.add(objectTray);
					}
					if (this.minTokenIndex == -1)
						this.minTokenIndex = objectTray.index;
					this.maxTokenIndex = objectTray.index;
					if (this.minTokenStartOffset == -1)
						this.minTokenStartOffset = token.getStartOffset();
					this.maxTokenEndOffset = token.getEndOffset();
				}
				else if (objectTray.index == TokenMarkupPanelObjectTray.WHITESPACE_INDEX) {
					String space = ((String) objectTray.object);
					for (int c = 0; c < space.length(); c++) {
						text.append(space.charAt(c));
						this.objectTrayAtOffset.add(objectTray);
					}
				}
				else if (objectTray.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) {
					Annotation annot = ((Annotation) objectTray.object);
					AnnotHighlight ah = new AnnotHighlight(this.objectTrayAtOffset.size(), annot.getType());
					this.annotHighlights.add(ah);
					annotsToHighlights.put(annot, ah);
					text.append(' ');
					this.objectTrayAtOffset.add(objectTray);
					this.spanningHighlightAnnots.remove(annot);
				}
				else if (objectTray.index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX) {
					Annotation annot = ((Annotation) objectTray.object);
					AnnotHighlight ah = ((AnnotHighlight) annotsToHighlights.get(annot));
					if (ah == null) /* start of this one must have been before some tag panel of nested annotation, draw from start */ {
						ah = new AnnotHighlight(-1, annot.getType());
						this.annotHighlights.add(0, ah); // the later the end tag, the earlier the start tag (normally)
					}
					ah.endOffset = this.objectTrayAtOffset.size();
					text.append(' ');
					this.objectTrayAtOffset.add(objectTray); // TODO somehow map offset to annotation type and color
					this.spanningHighlightAnnots.remove(annot);
				}
			}
			this.tokenArea.setText(text.toString());
			for (int h = 0; h < this.annotHighlights.size(); h++) {
				AnnotHighlight xah = ((AnnotHighlight) this.annotHighlights.get(h));
				if (xah.endOffset == -1)
					xah.endOffset = text.length(); // end of this one must follow after some tag panel of nested annotation, draw to end
			}
		}
	}
	private static class TokenMarkupPanelObjectTray {
		static final int WHITESPACE_INDEX = -1;
		static final int ANNOT_START_INDEX = -2;
		static final int ANNOT_END_INDEX = -4;
		final Object object; // we're OK holding actual token or annotation views here, only way tray survives display update is for annotation display mode changes
		final int index;
		TokenMarkupPanelObjectTray(Object object, int index) {
			this.object = object;
			this.index = index;
		}
	}
	private static class AnnotHighlight {
		final int startOffset;
		final String annotType;
		int endOffset = -1;
		AnnotHighlight(int startOffset, String annotType) {
			this.startOffset = startOffset;
			this.annotType = annotType;
		}
	}
	
	int computeNextLeftwardCaretPosition(JTextArea ta, int cp, boolean ctrlJump, int ss) throws BadLocationException {
		if (ctrlJump) {
			//	always jump to start of current token or, with space to left, to start of next token to left
			Segment taText = new Segment();
			ta.getDocument().getText(0, ta.getDocument().getLength(), taText);
			if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Got text: '" + taText.toString() + "'");
			if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - at position " + cp + ", to left is '" + taText.charAt(cp - 1) + "'");
			cp--; // we're looking leftwards, easier to subtract 1 up front
			char nch = taText.charAt(cp);
			while ((cp != 0) && (nch == ' ')) {
				cp--;
				nch = taText.charAt(cp);
			}
			if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipped space to position " + (cp + 1) + ", to left is '" + nch + "'");
			if (cp == 0) {// nothing but spaces except for maybe very first character, jump right to start
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> skipping lone leading character");
				return 0;
			}
			if (Character.isLetter(nch)) {
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipping sequence of letters");
				while ((cp != 0) && Character.isLetter(taText.charAt(cp - 1)))
					cp--;
				while ((cp != 0) && (ss < cp) && (taText.charAt(cp - 1) == ' '))
					cp--; // we're contracting selection starting on left, jump spaces as well
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> moving to " + cp + ", before '" + taText.charAt(cp) + "'");
				return cp; // got to start of sequence of letters
			}
			else if (Character.isDigit(nch)) {
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipping sequence of digits");
				while ((cp != 0) && Character.isDigit(taText.charAt(cp - 1)))
					cp--;
				while ((cp != 0) && (ss < cp) && (taText.charAt(cp - 1) == ' '))
					cp--; // we're contracting selection starting on left, jump spaces as well
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> moving to " + cp + ", before '" + taText.charAt(cp) + "'");
				return cp; // got to start of sequence of digits
			}
			else {
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipping sequence of punctuation marks");
				while ((cp != 0) && (nch == taText.charAt(cp - 1)))
					cp--;
				while ((cp != 0) && (ss < cp) && (taText.charAt(cp - 1) == ' '))
					cp--; // we're contracting selection starting on left, jump spaces as well
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> moving to " + cp + ", before '" + taText.charAt(cp) + "'");
				return cp; // got to start of sequence of equal punctuation marks
			}
		}
		else return (cp - 1);
	}
	int computeNextRightwardCaretPosition(JTextArea ta, int cp, boolean ctrlJump, int ss) throws BadLocationException {
		if (ctrlJump) {
			//	always jump to end of current token or, with space to right, to end of next token to right
			Segment taText = new Segment();
			ta.getDocument().getText(0, ta.getDocument().getLength(), taText);
			if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Got text: '" + taText.toString() + "'");
			if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - at position " + cp + ", to right is '" + taText.charAt(cp) + "'");
			char nch = taText.charAt(cp);
			while (((cp + 1) < taText.length()) && (nch == ' ')) {
				cp++;
				nch = taText.charAt(cp);
			}
			if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipped space to position " + cp + ", to right is '" + nch + "'");
			cp++; // we're jumping rightwards _after_ last matching character, easier to add 1 up front
			if (cp == taText.length()) {// nothing but spaces except for maybe very last character, jump right to end
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> skipping lone tailing character");
				return taText.length();
			}
			if (Character.isLetter(nch)) {
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipping sequence of letters");
				while ((cp < taText.length()) && Character.isLetter(taText.charAt(cp)))
					cp++;
				while ((cp < taText.length()) && (cp < ss) && (taText.charAt(cp) == ' '))
					cp++; // we're contracting selection starting on right, jump spaces as well
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> moving to " + cp + ", after '" + taText.charAt(cp - 1) + "'");
				return cp; // got to end of sequence of letters
			}
			else if (Character.isDigit(nch)) {
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipping sequence of digits");
				while ((cp < taText.length()) && Character.isDigit(taText.charAt(cp)))
					cp++;
				while ((cp < taText.length()) && (cp < ss) && (taText.charAt(cp) == ' '))
					cp++; // we're contracting selection starting on right, jump spaces as well
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> moving to " + cp + ", after '" + taText.charAt(cp - 1) + "'");
				return cp; // got to end of sequence of digits
			}
			else {
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" - skipping sequence of punctuation marks");
				while ((cp < taText.length()) && (nch == taText.charAt(cp)))
					cp++;
				while ((cp < taText.length()) && (cp < ss) && (taText.charAt(cp) == ' '))
					cp++; // we're contracting selection starting on right, jump spaces as well
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> moving to " + cp + ", after '" + taText.charAt(cp - 1) + "'");
				return cp; // got to end of sequence of equal punctuation marks
			}
		}
		else return (cp + 1);
	}
	
	Rectangle getRelativePositionOf(Component comp) {
		Rectangle pos = new Rectangle(0, 0, comp.getWidth(), comp.getHeight());
		for (Component pComp = comp; pComp != null; pComp = pComp.getParent()) {
			if (pComp == this)
				return pos; // we've come up far enough, no point in going beyond main panel
			Point cp = pComp.getLocation();
			pos.x += cp.x;
			pos.y += cp.y;
		}
		return null; // didn't get to main panel, component must be invisible
	}
	
	private class AnnotTagPanel extends JPanel implements FocusListener, KeyListener {
		
		/*final */Annotation annot; // cannot make this final, annotation views might expire
		final boolean isStartTag;
		private JPanel foldButtonTarget;
		
		private JButton foldButton = new JButton("-") {
			public void paint(Graphics gr) {
				Color preColor = gr.getColor();
				
				//	paint background (need to be non-opaque and then draw background ourselves to facilitate mixing colors)
				Dimension size = this.getSize();
				gr.setColor(tagBackgroundColor);
				gr.fillRect(0, 0, size.width, size.height);
				gr.setColor(this.getBackground());
				gr.fillRect(0, 0, size.width, size.height);
				
				//	paint text
				gr.setColor(preColor);
				super.paint(gr);
			}
		};
		
		int charWidth = -1;
		int charHeight = -1;
		JTextArea tagArea = new JTextArea() {
			public void setFont(Font f) {
				super.setFont(f);
				charWidth = this.getColumnWidth();
				charHeight = this.getRowHeight();
			}
			public void paint(Graphics gr) {
				Color preColor = gr.getColor();
				
				//	get offset position width and height
				if (charWidth == -1)
					charWidth = this.getColumnWidth();
				if (charHeight == -1)
					charHeight = this.getRowHeight();
				
				//	paint background (need to be non-opaque and then draw background ourselves to facilitate mixing colors)
				Dimension size = this.getSize();
				gr.setColor(tagBackgroundColor);
				gr.fillRect(0, 0, size.width, size.height);
				gr.setColor(this.getBackground());
				gr.fillRect(0, 0, size.width, size.height);
				
				//	paint text
				gr.setColor(preColor);
				super.paint(gr);
			}
		};
		
//		final ParaMarkupPanel para;
		int index = -1;
		
		AnnotTagPanel(Annotation annot, boolean isStartTag, JPanel foldButtonTarget) {
			super(new BorderLayout(), true);
//			this.para = para;
			this.annot = annot;
			this.isStartTag = isStartTag;
			this.foldButtonTarget = foldButtonTarget;
			this.setBackground(tagBackgroundColor); // need white backdrop for alpha lightened annotation color to simply be brighter
			
			this.foldButton.setFont(this.foldButton.getFont().deriveFont(Font.BOLD));
			this.foldButton.setBorder(BorderFactory.createLineBorder(this.foldButton.getBackground(), 2));
			this.foldButton.setPreferredSize(new Dimension(20, 1)); // TODO add global property for folding button and tag connector width
			this.foldButton.setOpaque(false); // need to draw background ourselves to facilitate mixing colors
			this.foldButton.setFocusable(false);
			this.foldButton.setEnabled(foldButtonTarget != null);
			this.foldButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					toggleTargetFoldStatus();
				}
			});
			this.add(this.foldButton, BorderLayout.WEST);
			
			this.tagArea.setLineWrap(true);
			this.tagArea.setWrapStyleWord(true);
			this.tagArea.setEditable(false);
			this.tagArea.setOpaque(false); // need to draw background ourselves to facilitate mixing colors
			this.tagArea.setFont(tagTextFont);
			this.tagArea.setForeground(tagTextColor);
			this.tagArea.setSelectedTextColor(selTagTextColor);
			this.tagArea.setSelectionColor(selTagBackgroundColor);
			this.tagArea.setCaret(new DefaultCaret() {
				boolean handleDragEventLocally = false;
				public void mousePressed(MouseEvent me) {
					if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == 0) // not a left click, don't modify selection
						super.mousePressed(me);
					else if (me.isShiftDown()) {
						if (tagSelectionStart == AnnotTagPanel.this)
							super.mousePressed(me);
						gatpSelectionModified(me, true);
					}
					else {
						super.mousePressed(me);
						gatpSelectionStarted(me);
					}
				}
				public void mouseReleased(MouseEvent me) {
					if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == 0) // not a left click, don't modify selection
						super.mousePressed(me);
					else {
						if (this.handleDragEventLocally)
							super.mouseReleased(me);
						gatpSelectionEnded(me);
					}
				}
				public void mouseClicked(MouseEvent me) {
					if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == 0) // not a right click, don't show context menu
						super.mouseClicked(me);
					else gatpSelectionEnded(me); // show context menu
				}
				public void mouseEntered(MouseEvent me) {
					this.handleDragEventLocally = true;
					super.mouseEntered(me);
				}
				public void mouseExited(MouseEvent me) {
					this.handleDragEventLocally = false;
					super.mouseExited(me);
				}
				public void mouseDragged(MouseEvent me) {
					if (this.handleDragEventLocally)
						super.mouseDragged(me);
					gatpSelectionModified(me, false);
				}
				protected void adjustVisibility(Rectangle nloc) {
					//	need to deactivate this to make sure cleaning selection doesn't incur scrolling
				}
				public void focusGained(FocusEvent fe) {
					//	need to deactivate this to make sure moving focus to selection end doesn't clear selection
				}
				public void focusLost(FocusEvent fe) {
					//	need to deactivate this to make sure moving focus to selection end doesn't clear selection
				}
			});
			this.tagArea.addFocusListener(this);
			this.tagArea.addKeyListener(this);
			
			//	set text area background to annot color
			this.updateTag(false);
			this.add(this.tagArea, BorderLayout.CENTER);
		}
		
		public void focusGained(FocusEvent fe) {
			this.tagArea.getCaret().setVisible(true);
		}
		public void focusLost(FocusEvent fe) {
			this.tagArea.getCaret().setVisible(false);
		}
		
		public void keyPressed(KeyEvent ke) {
			try {
				this.doKeyPressed(ke);
			}
			catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}
		private void doKeyPressed(KeyEvent ke) throws BadLocationException {
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
				GamtaDocumentMarkupPanel.this.clearSelections(true); // wherever the jump goes ... 
			else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
				int cp = this.tagArea.getCaretPosition();
				if (cp == 0) {
					JPanel pp = this.getPrecedingTextPanel(ke.isShiftDown(), null);
					if (pp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) pp);
						int tcp = gatp.tagArea.getDocument().getLength();
						if (ke.isShiftDown())
							this.extendSelectionTo(gatp, tcp, ke);
						else this.moveCaretTo(gatp, tcp, ke);
					}
					else if (pp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) pp);
						int tcp = gtmp.tokenArea.getDocument().getLength();
						this.moveCaretTo(gtmp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					int tcp = computeNextLeftwardCaretPosition(this.tagArea, cp, ke.isControlDown(), ((ke.isControlDown() && ke.isShiftDown()) ? this.tagArea.getCaret().getMark() : cp));
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
				int cp = this.tagArea.getCaretPosition();
				if (cp == this.tagArea.getDocument().getLength()) {
					JPanel fp = this.getFollowingTextPanel(ke.isShiftDown(), null);
					if (fp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) fp);
						int tcp = 0;
						if (ke.isShiftDown())
							this.extendSelectionTo(gatp, tcp, ke);
						else this.moveCaretTo(gatp, tcp, ke);
					}
					else if (fp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) fp);
						int tcp = 0;
						this.moveCaretTo(gtmp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					int tcp = computeNextRightwardCaretPosition(this.tagArea, cp, ke.isControlDown(), ((ke.isControlDown() && ke.isShiftDown()) ? this.tagArea.getCaret().getMark() : cp));
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_UP) {
				int cp = this.tagArea.getCaretPosition();
				Rectangle cPos = this.tagArea.modelToView(cp);
				Point tcPos = new Point(cPos.x, (cPos.y - (this.charHeight / 2))); // half a line height above top of current position
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going up one line from offset " + cp + " at " + cPos + " to " + tcPos);
				if (tcPos.y < 0) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel above");
					JPanel pp = this.getPrecedingTextPanel(ke.isShiftDown(), null);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> preceding panel is " + pp);
					if (pp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) pp);
						Dimension tTas = gatp.tagArea.getSize();
						tcPos.y = (tTas.height - (gatp.charHeight / 2)); // half line height above bottom edge of tag area
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gatp, tcp, ke);
						else this.moveCaretTo(gatp, tcp, ke);
					}
					else if (pp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) pp);
						if (ke.isControlDown())
							tcPos.y = (gtmp.charHeight / 2); // half line height below top edge of token area
						else {
							Dimension tTas = gtmp.tokenArea.getSize();
							tcPos.y = (tTas.height - (gtmp.charHeight / 2)); // half line height above bottom edge of token area
						}
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gtmp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going up one row");
					int tcp = this.tagArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
				int cp = this.tagArea.getCaretPosition();
				Rectangle cPos = this.tagArea.modelToView(cp);
				Point tcPos = new Point(cPos.x, (cPos.y + cPos.height + (this.charHeight / 2))); // half a line height below bottom of current position
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going down one line from offset " + cp + " at " + cPos + " to " + tcPos);
				Dimension tas = this.tagArea.getSize();
				if (tas.height < tcPos.y) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel below");
					JPanel fp = this.getFollowingTextPanel(ke.isShiftDown(), null);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> following panel is " + fp);
					if (fp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) fp);
						tcPos.y = (gatp.charHeight / 2); // half line height below top edge of tag area
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gatp, tcp, ke);
						else this.moveCaretTo(gatp, tcp, ke);
					}
					else if (fp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) fp);
						if (ke.isControlDown()) {
							Dimension tTas = gtmp.tokenArea.getSize();
							tcPos.y = (tTas.height - (gtmp.charHeight / 2)); // half line height above bottom edge of token area
						}
						else tcPos.y = (gtmp.charHeight / 2); // half line height below top edge of token area
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gtmp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going down one row");
					int tcp = this.tagArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_HOME) {
				int cp = this.tagArea.getCaretPosition();
				int tcp;
				if (ke.isControlDown())
					tcp = 0; // go to start of tag area proper
				else {
					Rectangle cPos = this.tagArea.modelToView(cp);
					Point tcPos = new Point(0, cPos.y);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going to start of line from offset " + cp + " at " + cPos + " to " + tcPos);
					tcp = this.tagArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
				}
				if (ke.isShiftDown())
					this.extendSelectionTo(this, tcp, ke);
				else this.moveCaretTo(this, tcp, ke);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_END) {
				int cp = this.tagArea.getCaretPosition();
				int tcp;
				if (ke.isControlDown())
					tcp = this.tagArea.getDocument().getLength(); // go to end of tag area proper
				else {
					Rectangle cPos = this.tagArea.modelToView(cp);
					Point tcPos = new Point((this.tagArea.getWidth() - 1), cPos.y);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going to end of line from offset " + cp + " at " + cPos + " to " + tcPos);
					tcp = this.tagArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
				}
				if (ke.isShiftDown())
					this.extendSelectionTo(this, tcp, ke);
				else this.moveCaretTo(this, tcp, ke);
			}
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP) {
				if (xdvp == null) /* not much we can do without knowing page height ... */ {
					ke.consume();
					return;
				}
				Rectangle vPos = xdvp.getViewRect();
				int cp = this.tagArea.getCaretPosition();
				Rectangle rcPos = this.tagArea.modelToView(cp);
				Rectangle aPos = getRelativePositionOf(this);
				Rectangle acPos = new Rectangle(rcPos);
				acPos.x += aPos.x;
				acPos.y += aPos.y;
				Point atcPos = new Point(acPos.x, ((acPos.y + (this.charHeight / 2)) - vPos.height)); // one screen height up, middle of line
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going up one screen from offset " + cp + " at " + acPos + " to " + atcPos);
				if (atcPos.y < aPos.y) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel above");
					JPanel pp = this.getPrecedingTextPanel(ke.isShiftDown(), atcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> preceding panel is " + pp);
					if (pp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) pp);
						Rectangle tPos = getRelativePositionOf(gatp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gatp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gatp.charHeight / 2));
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gatp, tcp, ke);
						else this.moveCaretTo(gatp, tcp, ke);
					}
					else if (pp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) pp);
						Rectangle tPos = getRelativePositionOf(gtmp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gtmp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gtmp.charHeight / 2));
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gtmp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					Rectangle tPos = aPos;
					Point tcPos = new Point(atcPos);
					tcPos.x -= tPos.x;
					tcPos.y -= tPos.y;
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going up one screen");
					int tcp = this.tagArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
				if (xdvp == null) /* not much we can do without knowing page height ... */ {
					ke.consume();
					return;
				}
				Rectangle vPos = xdvp.getViewRect();
				int cp = this.tagArea.getCaretPosition();
				Rectangle rcPos = this.tagArea.modelToView(cp);
				Rectangle aPos = getRelativePositionOf(this);
				Rectangle acPos = new Rectangle(rcPos);
				acPos.x += aPos.x;
				acPos.y += aPos.y;
				Point atcPos = new Point(acPos.x, ((acPos.y + (this.charHeight / 2)) + vPos.height)); // one screen height down, middle of line
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Going down one screen from offset " + cp + " at " + acPos + " to " + atcPos);
				if ((aPos.y + aPos.height) < atcPos.y) {
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> out of bounds, going to panel below");
					JPanel fp = this.getFollowingTextPanel(ke.isShiftDown(), atcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> following panel is " + fp);
					if (fp instanceof AnnotTagPanel) {
						AnnotTagPanel gatp = ((AnnotTagPanel) fp);
						Rectangle tPos = getRelativePositionOf(gatp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gatp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gatp.charHeight / 2));
						int tcp = gatp.tagArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						if (ke.isShiftDown())
							this.extendSelectionTo(gatp, tcp, ke);
						else this.moveCaretTo(gatp, tcp, ke);
					}
					else if (fp instanceof TokenMarkupPanel) {
						TokenMarkupPanel gtmp = ((TokenMarkupPanel) fp);
						Rectangle tPos = getRelativePositionOf(gtmp);
						Point tcPos = new Point(atcPos);
						tcPos.x -= tPos.x;
						tcPos.y -= tPos.y;
						if (tcPos.y < 0)
							tcPos.y = (gtmp.charHeight / 2);
						else if (tPos.height < tcPos.y)
							tcPos.y = (tPos.height - (gtmp.charHeight / 2));
						int tcp = gtmp.tokenArea.viewToModel(tcPos);
						if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
						this.moveCaretTo(gtmp, tcp, ke);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						ke.consume();
					}
				}
				else {
					Rectangle tPos = aPos;
					Point tcPos = new Point(atcPos);
					tcPos.x -= tPos.x;
					tcPos.y -= tPos.y;
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> in bounds, going down one screen");
					int tcp = this.tagArea.viewToModel(tcPos);
					if (DEBUG_KEYSTROKE_HANDLING) System.out.println(" ==> going to " + tcp + " at " + tcPos);
					if (ke.isShiftDown())
						this.extendSelectionTo(this, tcp, ke);
					else this.moveCaretTo(this, tcp, ke);
				}
			}
			else if (ke.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
				int cp = this.tagArea.getCaretPosition();
				Rectangle cPos = this.tagArea.modelToView(cp);
				Point cmPos = new Point(cPos.x, (cPos.y + (this.charHeight / 2))); // middle of current line
				tagSelectionEnded(this, cp, true, this, cmPos, null);
			}
			else if ((ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (ke.getKeyCode() == KeyEvent.VK_DELETE))
				handleKeystroke(this, ke); // to be implemented by subclasses
			else if ((ke.getKeyCode() == KeyEvent.VK_TAB) || (ke.getKeyCode() == KeyEvent.VK_ENTER))
				handleKeystroke(this, ke); // to be implemented by subclasses
			else if (ke.isControlDown() && (KeyEvent.VK_A <= ke.getKeyCode()) && (ke.getKeyCode() <= KeyEvent.VK_Z)) /* need to handle 'Ctrl+<letter>' this tay, as key char comes out as control character on 'typed' events */ {
				char kteChar = ((char) (((int) 'A') + (ke.getKeyCode() - KeyEvent.VK_A))); // all this relies upon is for A-Z codes being continuous sequence in correct order
//				KeyEvent kte = new KeyEvent(ke.getComponent(), KeyEvent.KEY_TYPED /* only way we can communicate actual character */, ke.getWhen(), ke.getModifiers(), KeyEvent.VK_UNDEFINED /* no other choice than this for 'typed' events */, kteChar, ke.getKeyLocation());
				KeyEvent kte = new KeyEvent(ke.getComponent(), KeyEvent.KEY_TYPED /* only way we can communicate actual character */, ke.getWhen(), ke.getModifiers(), KeyEvent.VK_UNDEFINED /* no other choice than this for 'typed' events */, kteChar, KeyEvent.KEY_LOCATION_UNKNOWN /* no other choice than this for 'typed' events */);
				handleKeystroke(this, kte);
				ke.consume(); // consume this one, we just generate out own messenger object for internal use
			}
			else ke.consume();
		}
		public void keyReleased(KeyEvent ke) {
			if (ke.getKeyCode() == KeyEvent.VK_LEFT)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_UP)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_HOME)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_END)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				ke.consume();
			else if (ke.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
				ke.consume();
			else if ((ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (ke.getKeyCode() == KeyEvent.VK_DELETE))
				ke.consume();
			else if ((ke.getKeyCode() == KeyEvent.VK_TAB) || (ke.getKeyCode() == KeyEvent.VK_ENTER))
				ke.consume();
			else if (ke.isControlDown() && (KeyEvent.VK_A <= ke.getKeyCode()) && (ke.getKeyCode() <= KeyEvent.VK_Z)) // need to handle 'Ctrl+<letter>' this tay, as key char comes out as control character on 'typed' events
				ke.consume();
		}
		public void keyTyped(KeyEvent ke) {
			try {
				this.doKeyTyped(ke);
			}
			catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}
		/* keys (apparently) executing on 'pressed' event:
		 *   - backspace, delete
		 *   - return, tab
		 *   - arrow keys
		 *   - page up, page down
		 *   - home, end
		 */
		private void doKeyTyped(KeyEvent ke) throws BadLocationException {
			if ((ke.getKeyChar() == 0x0008 /* backspace */) || (ke.getKeyChar() == 0x007F /* delete */))
				ke.consume(); // thse execute on 'key pressed' above, but still generate typing event
			else if ((ke.getKeyChar() == 0x0009 /* (horizontal) tab */) || (ke.getKeyChar() == 0x000A /* enter (line feed) */) || (ke.getKeyChar() == 0x000D /* enter (carriage return) */))
				ke.consume(); // thse execute on 'key pressed' above, but still generate typing event
			else if (ke.isControlDown() && (ke.getKeyChar() < 0x0020)) // 'Ctrl+<letter>' handled above, as key char comes out as control character on 'typed' events
				ke.consume(); // these execute on 'key pressed' above, but still generate typing event
			//	no need to handle arrow keys, 'page up' or 'page down', 'home' or 'end', as those don't create typing events at all
			else handleKeystroke(this, ke);
		}
		private void moveCaretTo(AnnotTagPanel gatp, int cp, KeyEvent ke) {
			clearSelections(true);
			gatp.tagArea.setCaretPosition(cp);
			if (gatp != this)
				gatp.tagArea.requestFocusInWindow();
			//	_need_to_ start selection right here, as might be extended via shift click
			tagSelectionStarted(gatp, cp);
			ke.consume();
			if ((gatp != this) && (xdvp != null))
				xdvp.moveIntoView(gatp, null, dummyDisplayAdjustmentObserver);
		}
		private void moveCaretTo(TokenMarkupPanel gtmp, int cp, KeyEvent ke) {
			clearSelections(true);
			gtmp.tokenArea.setCaretPosition(cp);
			gtmp.tokenArea.requestFocusInWindow();
			//	_need_to_ start selection right here, as might be extended via shift click
			tokenSelectionStarted(gtmp, cp);
			ke.consume();
			if (xdvp != null)
				xdvp.moveIntoView(gtmp, null, dummyDisplayAdjustmentObserver);
		}
		private void extendSelectionTo(AnnotTagPanel gatp, int cp, KeyEvent ke) {
			if (gatp == this)
				this.tagArea.moveCaretPosition(cp);
			else gatp.tagArea.setCaretPosition(cp);
			if (gatp != this)
				gatp.tagArea.requestFocusInWindow();
			tagSelectionModified(gatp, cp);
			ke.consume();
			if ((gatp != this) && (xdvp != null))
				xdvp.moveIntoView(gatp, null, dummyDisplayAdjustmentObserver);
		}
		
		private JPanel getPrecedingTextPanel(boolean forSelection, Point atcPos) {
			FocusTraversalPolicy ftp = GamtaDocumentMarkupPanel.this.getFocusTraversalPolicy();
			if (ftp == null)
				ftp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
			Component pfComp = this.tagArea;
			Rectangle cPos = getRelativePositionOf(this);
			int rounds = 0;
			do {
				rounds++;
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Preceding text panel, round " + rounds);
				pfComp = ftp.getComponentBefore(GamtaDocumentMarkupPanel.this, pfComp);
				if (!(pfComp instanceof JTextArea))
					continue;
				Container pComp = pfComp.getParent();
				if (pComp == null)
					continue;
				if (pComp instanceof AnnotTagPanel) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((cPos.y + cPos.height) <= pPos.y)
						return null; // return null if 'previous' component below us
					if ((atcPos != null) && (atcPos.y < pPos.y))
						continue; // too low for page-up
					return ((AnnotTagPanel) pComp);
				}
				else if (!forSelection && (pComp instanceof TokenMarkupPanel)) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((cPos.y + cPos.height) <= pPos.y)
						return null; // return null if 'previous' component below us
					if ((atcPos != null) && (atcPos.y < pPos.y))
						continue; // too low for page-up
					return ((TokenMarkupPanel) pComp);
				}
			} while (true);
		}
		private JPanel getFollowingTextPanel(boolean forSelection, Point atcPos) {
			FocusTraversalPolicy ftp = GamtaDocumentMarkupPanel.this.getFocusTraversalPolicy();
			if (ftp == null)
				ftp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
			Component pfComp = this.tagArea;
			Rectangle cPos = getRelativePositionOf(this);
			int rounds = 0;
			do {
				rounds++;
				if (DEBUG_KEYSTROKE_HANDLING) System.out.println("Following text panel, round " + rounds);
				pfComp = ftp.getComponentAfter(GamtaDocumentMarkupPanel.this, pfComp);
				if (!(pfComp instanceof JTextArea))
					continue;
				Container pComp = pfComp.getParent();
				if (pComp instanceof AnnotTagPanel) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((pPos.y + pPos.height) <= cPos.y)
						return null; // return null if 'next' component above us
					if ((atcPos != null) && ((pPos.y + pPos.height) < atcPos.y))
						continue; // too high for page-down
					return ((AnnotTagPanel) pComp);
				}
				else if (!forSelection && (pComp instanceof TokenMarkupPanel)) {
					Rectangle pPos = getRelativePositionOf(pComp);
					if (pPos == null)
						continue; // hard to tell how this stayed on keyboard focus cycle ...
					if ((pPos.y + pPos.height) <= cPos.y)
						return null; // return null if 'next' component above us
					if ((atcPos != null) && ((pPos.y + pPos.height) < atcPos.y))
						continue; // too high for page-down
					return ((TokenMarkupPanel) pComp);
				}
			} while (true);
		}
		
		void toggleTargetFoldStatus() {
			JPanel tComp;
			Rectangle tPos;
			Annotation tAnnot;
			boolean tAnnotFolded;
			if (this.foldButtonTarget instanceof AnnotMarkupPanel) {
				AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.foldButtonTarget);
				tComp = gamp.startTagPanel;
				tAnnot = gamp.annot;
				if (gamp.contentFolded) // about to un-fold, keep start tag stable
					tPos = ((xdvp == null) ? null : xdvp.getViewPositionOf(gamp.startTagPanel));
//					tPos = null; // we're always unfolding from start tag, no need to move view position at all
				else if (this.isStartTag) // about to fold from start tag, keep start tag stable
					tPos = ((xdvp == null) ? null : xdvp.getViewPositionOf(gamp.startTagPanel));
				else // about to fold from end tag, move start tag to where end tag is
					tPos = ((xdvp == null) ? null : xdvp.getViewPositionOf(gamp.endTagPanel));
				gamp.toggleContentFolded();
				tAnnotFolded = gamp.contentFolded;
			}
//			else if (this.foldButtonTarget instanceof ParaMarkupPanel) {
//				ParaMarkupPanel xpmp = ((ParaMarkupPanel) this.foldButtonTarget);
//				tComp = xpmp.startTagPanel;
//				tAnnot = xpmp.para;
//				if (xpmp.contentFolded) // about to un-fold, keep start tag stable
//					tPos = ((xdvp == null) ? null : xdvp.getViewPositionOf(xpmp.startTagPanel));
////					tPos = null; // we're always unfolding from start tag, no need to move view position at all
//				else if (this.isStartTag) // about to fold from start tag, keep start tag stable
//					tPos = ((xdvp == null) ? null : xdvp.getViewPositionOf(xpmp.startTagPanel));
//				else // about to fold from end tag, move start tag to where end tag is
//					tPos = ((xdvp == null) ? null : xdvp.getViewPositionOf(xpmp.endTagPanel));
//				xpmp.toggleContentFolded();
//				tAnnotFolded = xpmp.contentFolded;
//			}
			else return;
			for (Container pComp = this.foldButtonTarget.getParent(); pComp != null; pComp = pComp.getParent()) {
				pComp.getLayout().layoutContainer(pComp);
				if (pComp == GamtaDocumentMarkupPanel.this) {
					pComp.validate();
					pComp.repaint();
//					checkStructuralAnnotationDisplayMode(tAnnot, tAnnotFolded);
					if (xdvp == null) {}
					else if ((tComp == null) || (tPos == null)) {
						xdvp.validate();
						xdvp.repaint();
					}
					else xdvp.moveToViewPosition(tComp, tPos, dummyDisplayAdjustmentObserver);
					break;
				}
			}
		}
		
		void gatpSelectionStarted(MouseEvent me) {
			int meOffset = this.tagArea.viewToModel(me.getPoint());
			tagSelectionStarted(this, meOffset);
		}
		
		void gatpSelectionModified(MouseEvent me, boolean isShiftPress) {
			if (isShiftPress || this.tagArea.contains(me.getPoint())) {
				int meOffset = this.tagArea.viewToModel(me.getPoint());
				tagSelectionModified(this, meOffset);
			}
			else {
				Point mep = me.getPoint(); // relative to token area proper
				for (Component pComp = this.tagArea; pComp != null; pComp = pComp.getParent()) {
					if (pComp == GamtaDocumentMarkupPanel.this)
						break; // we've come up far enough, no point in going beyond main panel
					Point cp = pComp.getLocation();
					mep.x += cp.x;
					mep.y += cp.y;
				}
				Component meComp = GamtaDocumentMarkupPanel.this;
				for (Component cComp = meComp.getComponentAt(mep.x, mep.y); cComp != null;) {
					if (cComp == meComp)
						break; // no use going any deeper
					meComp = cComp;
					Point cp = cComp.getLocation();
					mep.x -= cp.x;
					mep.y -= cp.y;
					if (meComp instanceof AnnotTagPanel)
						break; // no need to descend any further
					cComp = meComp.getComponentAt(mep.x, mep.y);
				}
				if (meComp instanceof AnnotTagPanel) {
					AnnotTagPanel meGatp = ((AnnotTagPanel) meComp);
					Point tap = meGatp.tagArea.getLocation();
					mep.x -= tap.x;
					mep.y -= tap.y;
					int meOffset = meGatp.tagArea.viewToModel(mep);
					tagSelectionModified(meGatp, meOffset);
				}
			}
		}
		
		void gatpSelectionEnded(MouseEvent me) {
			int meOffset = this.tagArea.viewToModel(me.getPoint());
			tagSelectionEnded(this, meOffset, me);
		}
		
		void setSelectedBetween(int firstOffset, int lastOffset) {
			this.tagArea.getCaret().setDot(firstOffset);
			this.tagArea.getCaret().moveDot(lastOffset);
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedUpTo(int offset) {
			this.tagArea.getCaret().setDot(this.tagArea.getDocument().getLength());
			this.tagArea.getCaret().moveDot(offset);
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedDownFrom(int offset) {
			this.tagArea.getCaret().setDot(offset);
			this.tagArea.getCaret().moveDot(this.tagArea.getDocument().getLength());
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedDownTo(int offset) {
			this.tagArea.getCaret().setDot(0);
			this.tagArea.getCaret().moveDot(offset);
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelectedUpFrom(int offset) {
			this.tagArea.getCaret().setDot(offset);
			this.tagArea.getCaret().moveDot(0);
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void setSelected() {
			this.tagArea.getCaret().setDot(0);
			this.tagArea.getCaret().moveDot(this.tagArea.getDocument().getLength());
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void showSelection() {
			this.tagArea.getCaret().setSelectionVisible(true);
		}
		
		void clearSelection() {
			this.tagArea.getCaret().setDot(this.tagArea.getCaret().getMark()); // set caret back to right where we started
			this.tagArea.getCaret().setSelectionVisible(false);
		}
		
		public String toString() {
			return (super.toString() + ": " + this.tagArea.getText());
		}
		
		int getTagLength() {
			return this.tagArea.getDocument().getLength();
		}
		
		void updateTag(boolean isPanelEmpty) {
			if (this.isStartTag)
				this.tagArea.setText(createStartTag(this.annot));
			else this.tagArea.setText("</" + this.annot.getType() + ">");
			this.foldButton.setText(isPanelEmpty ? "+" : "-");
			this.updateAnnotColor();
		}
		
		void updateAnnotColor() {
			Color annotColor = getAnnotationColor(this.annot.getType(), true);
			this.foldButton.setBorder(BorderFactory.createLineBorder(annotColor, 2));
			this.setBorder(BorderFactory.createLineBorder(annotColor, 1));
			Color annotHighlightColor = getAnnotationHighlightColor(this.annot.getType(), true);
			this.foldButton.setBackground(annotHighlightColor);
			this.tagArea.setBackground(annotHighlightColor);
		}
		
		void updateTextSettings() {
			this.setBackground(tagBackgroundColor);
			this.foldButton.setFont(tagTextFont);
			this.foldButton.setForeground(tagTextColor);
			this.tagArea.setFont(tagTextFont);
			this.tagArea.setForeground(tagTextColor);
			this.tagArea.setSelectedTextColor(selTagTextColor);
			this.tagArea.setSelectionColor(selTagBackgroundColor);
		}
	}
	
	private class AnnotTagConnectorPanel extends JPanel {
		
		/*final */Annotation annot; // cannot make this final, annotation views might expire
		private Color annotColor;
		
		AnnotTagConnectorPanel(Annotation annot) {
			super(new BorderLayout(), true);
			this.annot = annot;
			this.setPreferredSize(new Dimension(20, 1));
			this.setOpaque(true);
			this.setBackground(tagBackgroundColor);
			this.setToolTipText(createStartTag(this.annot));
			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					PointSelection ps = new PointSelection(PointSelection.POINT_TYPE_ANNOTATION_TAG_CONNECTOR, AnnotTagConnectorPanel.this.annot, -1);
					clearSelections(false); // TODOne really ??? ==> yes, we're either completing pending two-click action, or making new click selection
					handleClickSelection(ps, me);
				}
			});
		}
		
		public String toString() {
			return (super.toString() + ": " + this.getToolTipText());
		}
		
		void updateTag() {
			this.annotColor = getAnnotationColor(this.annot.getType(), true);
		}
		
		void updateColorSettings() {
			this.setBackground(tagBackgroundColor);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			
			Color preColor = g.getColor();
			g.setColor(this.annotColor);
			g.fillRect(8, 0, 4, this.getHeight());
			g.setColor(preColor);
		}
	}
	
	private static String createStartTag(Annotation annot) {
		//	TODO maybe add global toggles for displaying annotation IDs and filtering attributes
		return AnnotationUtils.produceStartTag(annot, false, null, true);
	}
	
	private boolean showContextMenuOnMouseRelease = true;
	public boolean isShowingContextMenuOnMouseRelease() {
		return this.showContextMenuOnMouseRelease;
	}
	
	public void setShowContextMenuOnMouseRelease(boolean scmomr) {
		this.showContextMenuOnMouseRelease = scmomr;
	}
	
	private TokenMarkupPanel tokenSelectionStart = null;
	private int tokenSelectionStartOffset = -1;
	private TokenMarkupPanel tokenSelectionEnd = null;
	private int tokenSelectionEndOffset = -1;
	private int tokenSelectionModCount = -1;
	
	void tokenSelectionStarted(final TokenMarkupPanel gtmp, final int offset) {
		if (DEBUG_SELECTION_HANDLING) {
			System.out.println("DocPanel: mouse pressed in token panel at " + /*gtmp.para.index + "/" + */gtmp.index);
//			System.out.println("TokenPanel is " + gtmp.para.index + "/" + gtmp.index);
		}
		this.clearTokenSelection(gtmp);
		this.tokenSelectionStart = gtmp;
		this.tokenSelectionStartOffset = offset;
		this.tokenSelectionEnd = gtmp;
		this.tokenSelectionEndOffset = offset;
		this.tokenSelectionModCount = 0;
		this.tokenSelectionStart.showSelection();
		if ((this.tagSelectionStart != null) && (this.tagSelectionEnd != null))
			this.clearTagSelection(null);
	}
	
	void tokenSelectionModified(TokenMarkupPanel tsEndGtmp, int tsEndGtmpOffset) {
		
		//	get existing and new selections (with boundaries sorted and inversion indicated)
		TempTokenSelection exTokenSelection = new TempTokenSelection(this.tokenSelectionStart, this.tokenSelectionStartOffset, this.tokenSelectionEnd, this.tokenSelectionEndOffset);
		TempTokenSelection tokenSelection = new TempTokenSelection(this.tokenSelectionStart, this.tokenSelectionStartOffset, tsEndGtmp, tsEndGtmpOffset);
		
		//	clean up any de-selected paragraphs
		for (int p = exTokenSelection.firstGtmp.index; p < tokenSelection.firstGtmp.index; p++)
			((TokenMarkupPanel) this.tokenPanels.get(p)).clearSelection();
		for (int p = (tokenSelection.lastGtmp.index + 1); p <= exTokenSelection.lastGtmp.index; p++)
			((TokenMarkupPanel) this.tokenPanels.get(p)).clearSelection();
		
		//	select any newly included paragraphs (excluding new boundary paragraphs)
		for (int p = (tokenSelection.firstGtmp.index + 1); p < exTokenSelection.firstGtmp.index; p++)
			((TokenMarkupPanel) this.tokenPanels.get(p)).setSelected();
		if ((tokenSelection.firstGtmp.index < exTokenSelection.firstGtmp.index) && (exTokenSelection.firstGtmp.index < tokenSelection.lastGtmp.index))
			exTokenSelection.firstGtmp.setSelected(); // fully select former start paragraph if not boundary of updated selection
		for (int p = (exTokenSelection.lastGtmp.index + 1); p < tokenSelection.lastGtmp.index; p++)
			((TokenMarkupPanel) this.tokenPanels.get(p)).setSelected();
		if ((tokenSelection.firstGtmp.index < exTokenSelection.lastGtmp.index) && (exTokenSelection.lastGtmp.index < tokenSelection.lastGtmp.index))
			exTokenSelection.lastGtmp.setSelected(); // fully select former end paragraph if not boundary of updated selection
		
		//	select tokens inside boundary paragraphs
		if (tokenSelection.firstGtmp == tokenSelection.lastGtmp) {
			if (tokenSelection.startEndInverted)
				tokenSelection.firstGtmp.setSelectedBetween(tokenSelection.lastGtmpOffset, tokenSelection.firstGtmpOffset);
			else tokenSelection.firstGtmp.setSelectedBetween(tokenSelection.firstGtmpOffset, tokenSelection.lastGtmpOffset);
		}
		else if (tokenSelection.startEndInverted) {
			tokenSelection.lastGtmp.setSelectedUpFrom(tokenSelection.lastGtmpOffset);
			tokenSelection.firstGtmp.setSelectedUpTo(tokenSelection.firstGtmpOffset);
		}
		else {
			tokenSelection.firstGtmp.setSelectedDownFrom(tokenSelection.firstGtmpOffset);
			tokenSelection.lastGtmp.setSelectedDownTo(tokenSelection.lastGtmpOffset);
		}
		
		//	remember new selection extent
		this.tokenSelectionEnd = tsEndGtmp;
		this.tokenSelectionEndOffset = tsEndGtmpOffset;
		this.tokenSelectionModCount++;
		
		//	cancel any two-click action awaiting second click
		if (this.pendingTwoClickAction != null) {
			this.pendingTwoClickAction = null;
			if (this.twoClickActionMessenger != null)
				this.twoClickActionMessenger.twoClickActionChanged(null);
		}
	}
	
	private static class TempTokenSelection {
		final TokenMarkupPanel firstGtmp;
		final int firstGtmpOffset;
		final TokenMarkupPanel lastGtmp;
		final int lastGtmpOffset;
		final boolean startEndInverted;
		TempTokenSelection(TokenMarkupPanel startGtmp, int startGtmpOffset, TokenMarkupPanel endGtmp, int endGtmpOffset) {
			if (startGtmp.index < endGtmp.index) {
				this.firstGtmp = startGtmp;
				this.firstGtmpOffset = startGtmpOffset;
				this.lastGtmp = endGtmp;
				this.lastGtmpOffset = endGtmpOffset;
				this.startEndInverted = false;
			}
			else if (endGtmp.index < startGtmp.index) {
				this.firstGtmp = endGtmp;
				this.firstGtmpOffset = endGtmpOffset;
				this.lastGtmp = startGtmp;
				this.lastGtmpOffset = startGtmpOffset;
				this.startEndInverted = true;
			}
			else if (startGtmpOffset < endGtmpOffset) {
				this.firstGtmp = startGtmp;
				this.firstGtmpOffset = startGtmpOffset;
				this.lastGtmp = endGtmp;
				this.lastGtmpOffset = endGtmpOffset;
				this.startEndInverted = false;
			}
			else if (endGtmpOffset < startGtmpOffset) {
				this.firstGtmp = endGtmp;
				this.firstGtmpOffset = endGtmpOffset;
				this.lastGtmp = startGtmp;
				this.lastGtmpOffset = startGtmpOffset;
				this.startEndInverted = true;
			}
			else {
				this.firstGtmp = startGtmp;
				this.firstGtmpOffset = startGtmpOffset;
				this.lastGtmp = endGtmp;
				this.lastGtmpOffset = endGtmpOffset;
				this.startEndInverted = false;
			}
		}
	}
	
	void tokenSelectionEnded(TokenMarkupPanel gtmp, int offset, MouseEvent me) {
		if (me == null)
			this.tokenSelectionEnded(gtmp, offset, false, null, null, null);
		else this.tokenSelectionEnded(gtmp, offset, ((me.getModifiers() & MouseEvent.BUTTON3_MASK) != 0), me.getComponent(), me.getPoint(), me);
	}
	void tokenSelectionEnded(TokenMarkupPanel gtmp, int offset, boolean showContextMenu, Component comp, Point pt, MouseEvent meOrNull) {
		if ((this.tokenSelectionStart == null) || (this.tokenSelectionEnd == null)) {
			this.clearTokenSelection(null); // TODOne really ??? ==> yes, something is badly off ...
			return;
		}
		this.tokenSelectionEnd.tokenArea.requestFocusInWindow();
		if (this.tokenSelectionModCount == 0) {
			if (DEBUG_SELECTION_HANDLING) {
				System.out.println("Got token click selection:");
				System.out.println(" - selected is " + /*this.tokenSelectionStart.para.index + "/" + */this.tokenSelectionStart.index);
				System.out.println(" - click at offset " + this.tokenSelectionStartOffset);
			}
			PointSelection ps = createPointSelection(this.tokenSelectionStart, this.tokenSelectionStartOffset);
			this.handleClickSelection(ps, showContextMenu, comp, pt, meOrNull);
			return;
		}
		
		//	get current selection
		TempTokenSelection tokenSelection = new TempTokenSelection(this.tokenSelectionStart, this.tokenSelectionStartOffset, this.tokenSelectionEnd, this.tokenSelectionEndOffset);
		if (DEBUG_SELECTION_HANDLING) {
			System.out.println("Got token selection:");
//			System.out.println(" - paragraphs " + tokenSelection.firstXpmp.index + "-" + tokenSelection.lastXpmp.index);
			System.out.println(" - first token area at " + tokenSelection.firstGtmp.index + " selected from " + tokenSelection.firstGtmpOffset);
			System.out.println(" - last token area at " + tokenSelection.lastGtmp.index + " selected to " + tokenSelection.lastGtmpOffset);
		}
		
		//	end of injected selection, cannot show context menu
		if ((meOrNull == null) && (comp == null) && (pt == null))
			return;
		
		//	get actions from mounting point and show context menu if supposed to
		if (this.showContextMenuOnMouseRelease || showContextMenu) {
			TokenSelection ts;
			if (tokenSelection.firstGtmp == tokenSelection.lastGtmp)
				ts = new TokenSelection(tokenSelection.firstGtmp, tokenSelection.firstGtmpOffset, tokenSelection.lastGtmpOffset, tokenSelection.startEndInverted);
			else ts = new TokenSelection(tokenSelection.firstGtmp, tokenSelection.firstGtmpOffset, tokenSelection.lastGtmp, tokenSelection.lastGtmpOffset, tokenSelection.startEndInverted);
			SelectionAction[] tsActions = this.getActions(ts);
			this.showContextMenu(tsActions, comp, pt);
		}
		
		//	TODOnot clear selection when context menu option selected ...
		//	TODOne ... OR MAYBE DON'T, next click will do same thing just fine
	}
	
	private void clearTokenSelection(TokenMarkupPanel ntsStartGtmp) {
		if ((this.tokenSelectionStart != null) && (this.tokenSelectionEnd != null)) try {
			for (int p = Math.min(this.tokenSelectionStart.index, this.tokenSelectionEnd.index); p <= Math.max(this.tokenSelectionStart.index, this.tokenSelectionEnd.index); p++) {
				TokenMarkupPanel gatp = ((TokenMarkupPanel) this.tokenPanels.get(p));
				if (gatp != ntsStartGtmp) // don't clear start of next selection
					gatp.clearSelection();
			}
		}
		//	need to catch exceptions in case display panels got changed as part of refresh and stored panels stale, and fall back to full cleanup
		catch (RuntimeException re) {
			for (int p = 0; p < this.tokenPanels.size(); p++) {
				TokenMarkupPanel gatp = ((TokenMarkupPanel) this.tokenPanels.get(p));
				if (gatp != ntsStartGtmp) // still don't clear start of next selection
					gatp.clearSelection();
			}
		}
//		if ((this.tokenSelectionStart != null) && (this.tokenSelectionEnd != null))
//			(new RuntimeException("TOKEN SELECTION CLEARED")).printStackTrace(System.out);
		this.tokenSelectionStart = null;
		this.tokenSelectionStartOffset = -1;
		this.tokenSelectionEnd = null;
		this.tokenSelectionEndOffset = -1;
		this.tokenSelectionModCount = -1;
	}
	
	private AnnotTagPanel tagSelectionStart = null;
	private int tagSelectionStartOffset = -1;
	private AnnotTagPanel tagSelectionEnd = null;
	private int tagSelectionEndOffset = -1;
	private int tagSelectionModCount = -1;
	
	void tagSelectionStarted(AnnotTagPanel gatp, int offset) {
		if (DEBUG_SELECTION_HANDLING) {
			System.out.println("DocPanel: mouse pressed in token panel at " + /*((gatp.para == null) ? "-" : gatp.para.index) + "/" + */gatp.index);
//			System.out.println("TagPanel is " + ((gatp.para == null) ? "-" : gatp.para.index) + "/" + gatp.index);
		}
		this.clearTagSelection(gatp);
		this.tagSelectionStart = gatp;
		this.tagSelectionStartOffset = offset;
		this.tagSelectionEnd = gatp;
		this.tagSelectionEndOffset = offset;
		this.tagSelectionModCount = 0;
		this.tagSelectionStart.showSelection();
		if ((this.tokenSelectionStart != null) && (this.tokenSelectionEnd != null))
			this.clearTokenSelection(null);
	}
	
	void tagSelectionModified(AnnotTagPanel tsEndGatp, int tsEndGatpOffset) {
		
		//	get existing and new selections (with boundaries sorted and inversion indicated)
		AnnotTagSelection exTagSelection = new AnnotTagSelection(this.tagSelectionStart, this.tagSelectionStartOffset, this.tagSelectionEnd, this.tagSelectionEndOffset);
		AnnotTagSelection tagSelection = new AnnotTagSelection(this.tagSelectionStart, this.tagSelectionStartOffset, tsEndGatp, tsEndGatpOffset);
		
		//	tag selection, handle ourselves
		updateTagSelection(this.tagPanels, exTagSelection, tagSelection);
		
		//	remember new selection extent
		this.tagSelectionEnd = tsEndGatp;
		this.tagSelectionEndOffset = tsEndGatpOffset;
		this.tagSelectionModCount++;
		
		//	cancel any two-click action awaiting second click
		if (this.pendingTwoClickAction != null) {
			this.pendingTwoClickAction = null;
			if (this.twoClickActionMessenger != null)
				this.twoClickActionMessenger.twoClickActionChanged(null);
		}
	}
	
	static void updateTagSelection(ArrayList tagPanels, AnnotTagSelection exTagSelection, AnnotTagSelection tagSelection) {
		
		//	clean up any de-selected tags
		for (int p = exTagSelection.firstGatp.index; p < tagSelection.firstGatp.index; p++)
			((AnnotTagPanel) tagPanels.get(p)).clearSelection();
		for (int p = (tagSelection.lastGatp.index + 1); p <= exTagSelection.lastGatp.index; p++)
			((AnnotTagPanel) tagPanels.get(p)).clearSelection();
		
		//	select any newly included paragraphs (excluding new boundary paragraphs)
		for (int p = (tagSelection.firstGatp.index + 1); p < exTagSelection.firstGatp.index; p++)
			((AnnotTagPanel) tagPanels.get(p)).setSelected();
		if ((tagSelection.firstGatp.index < exTagSelection.firstGatp.index) && (exTagSelection.firstGatp.index < tagSelection.lastGatp.index))
			exTagSelection.firstGatp.setSelected(); // fully select former start tag if not boundary of updated selection
		for (int p = (exTagSelection.lastGatp.index + 1); p < tagSelection.lastGatp.index; p++)
			((AnnotTagPanel) tagPanels.get(p)).setSelected();
		if ((tagSelection.firstGatp.index < exTagSelection.lastGatp.index) && (exTagSelection.lastGatp.index < tagSelection.lastGatp.index))
			exTagSelection.lastGatp.setSelected(); // fully select former end tag if not boundary of updated selection
		
		//	select tokens inside boundary paragraphs
		if (tagSelection.firstGatp == tagSelection.lastGatp) {
			if (tagSelection.startEndInverted)
				tagSelection.firstGatp.setSelectedBetween(tagSelection.lastGatpOffset, tagSelection.firstGatpOffset);
			else tagSelection.firstGatp.setSelectedBetween(tagSelection.firstGatpOffset, tagSelection.lastGatpOffset);
		}
		else if (tagSelection.startEndInverted) {
			tagSelection.lastGatp.setSelectedUpFrom(tagSelection.lastGatpOffset);
			tagSelection.firstGatp.setSelectedUpTo(tagSelection.firstGatpOffset);
		}
		else {
			tagSelection.firstGatp.setSelectedDownFrom(tagSelection.firstGatpOffset);
			tagSelection.lastGatp.setSelectedDownTo(tagSelection.lastGatpOffset);
		}
	}
	
	private static class AnnotTagSelection {
		final AnnotTagPanel firstGatp;
		final int firstGatpOffset;
		final AnnotTagPanel lastGatp;
		final int lastGatpOffset;
		final boolean startEndInverted;
		AnnotTagSelection(AnnotTagPanel startGatp, int startGatpOffset, AnnotTagPanel endGatp, int endGatpOffset) {
			if (startGatp.index < endGatp.index) {
				this.firstGatp = startGatp;
				this.firstGatpOffset = startGatpOffset;
				this.lastGatp = endGatp;
				this.lastGatpOffset = endGatpOffset;
				this.startEndInverted = false;
			}
			else if (endGatp.index < startGatp.index) {
				this.firstGatp = endGatp;
				this.firstGatpOffset = endGatpOffset;
				this.lastGatp = startGatp;
				this.lastGatpOffset = startGatpOffset;
				this.startEndInverted = true;
			}
			else if (startGatpOffset < endGatpOffset) {
				this.firstGatp = startGatp;
				this.firstGatpOffset = startGatpOffset;
				this.lastGatp = endGatp;
				this.lastGatpOffset = endGatpOffset;
				this.startEndInverted = false;
			}
			else if (endGatpOffset < startGatpOffset) {
				this.firstGatp = endGatp;
				this.firstGatpOffset = endGatpOffset;
				this.lastGatp = startGatp;
				this.lastGatpOffset = startGatpOffset;
				this.startEndInverted = true;
			}
			else {
				this.firstGatp = startGatp;
				this.firstGatpOffset = startGatpOffset;
				this.lastGatp = endGatp;
				this.lastGatpOffset = endGatpOffset;
				this.startEndInverted = false;
			}
		}
	}
	
	void tagSelectionEnded(AnnotTagPanel gatp, int offset, MouseEvent me) {
		if (me == null)
			this.tagSelectionEnded(gatp, offset, false, null, null, null);
		else this.tagSelectionEnded(gatp, offset, ((me.getModifiers() & MouseEvent.BUTTON3_MASK) != 0), me.getComponent(), me.getPoint(), me);
	}
	void tagSelectionEnded(AnnotTagPanel gatp, int offset, boolean showContextMenu, Component comp, Point pt, MouseEvent meOrNull) {
		if ((this.tagSelectionStart == null) || (this.tagSelectionEnd == null)) {
			this.clearTagSelection(null); // TODOne really ??? ==> yes, something is badly off ...
			return;
		}
		this.tagSelectionEnd.tagArea.requestFocusInWindow();
		if (this.tagSelectionModCount == 0) {
			if (DEBUG_SELECTION_HANDLING) {
				System.out.println("Got tag click selection:");
//				System.out.println(" - selected is " + ((this.tagSelectionStart.para == null) ? "structural annotations" : ("details in para " + this.tagSelectionStart.para.index)));
				System.out.println(" - tag at " + this.tagSelectionStart.index + " selected from " + this.tagSelectionStartOffset);
			}
			int csType = (this.tagSelectionStart.isStartTag ? PointSelection.POINT_TYPE_ANNOTATION_START_TAG : PointSelection.POINT_TYPE_ANNOTATION_END_TAG);
			PointSelection ps = new PointSelection(csType, this.tagSelectionStart.annot, this.tagSelectionStartOffset);
			this.handleClickSelection(ps, showContextMenu, comp, pt, meOrNull);
			return;
		}
		
		//	get current selection
		AnnotTagSelection tagSelection = new AnnotTagSelection(this.tagSelectionStart, this.tagSelectionStartOffset, this.tagSelectionEnd, this.tagSelectionEndOffset);
		if (DEBUG_SELECTION_HANDLING) {
			System.out.println("Got tag selection:");
//			System.out.println(" - selected are " + ((tagSelection.firstGatp.para == null) ? "structural annotations" : ("details in para " + tagSelection.firstGatp.para.index)));
			System.out.println(" - first tag at " + tagSelection.firstGatp.index + " selected from " + tagSelection.firstGatpOffset);
			System.out.println(" - last tag at " + tagSelection.lastGatp.index + " selected to " + tagSelection.lastGatpOffset);
		}
		
		//	end of injected selection, cannot show context menu
		if ((meOrNull == null) && (comp == null) && (pt == null))
			return;
		
		//	get actions from mounting point and show context menu if supposed to
		if (this.showContextMenuOnMouseRelease || showContextMenu) {
			TagSelection ts;
			if (tagSelection.firstGatp == tagSelection.lastGatp)
				ts = new TagSelection(tagSelection.firstGatp, tagSelection.firstGatpOffset, tagSelection.lastGatpOffset, tagSelection.startEndInverted);
			else ts = new TagSelection(tagSelection.firstGatp, tagSelection.firstGatpOffset, tagSelection.lastGatp, tagSelection.lastGatpOffset, tagSelection.startEndInverted);
			SelectionAction[] tsActions = this.getActions(ts);
			this.showContextMenu(tsActions, comp, pt);
		}
		
		//	TODOnot clear selection when context menu option selected ...
		//	TODOne ... OR MAYBE DON'T, next click will do same thing just fine
	}
	
	private void clearTagSelection(AnnotTagPanel ntsStartGatp) {
		if ((this.tagSelectionStart != null) && (this.tagSelectionEnd != null)) try {
			for (int p = Math.min(this.tagSelectionStart.index, this.tagSelectionEnd.index); (p <= Math.max(this.tagSelectionStart.index, this.tagSelectionEnd.index)) && (p < this.tagPanels.size()) /* need to catch selection cleanup after annotation removal */; p++) {
				AnnotTagPanel gatp = ((AnnotTagPanel) this.tagPanels.get(p));
				if (gatp != ntsStartGatp) // don't clear start of next selection
					gatp.clearSelection();
			}
		}
		//	need to catch exceptions in case display panels got changed as part of refresh and stored panels stale, and fall back to full cleanup
		catch (RuntimeException re) {
			for (int p = 0; p < this.tagPanels.size(); p++) {
				AnnotTagPanel gatp = ((AnnotTagPanel) this.tagPanels.get(p));
				if (gatp != ntsStartGatp) // still don't clear start of next selection
					gatp.clearSelection();
			}
		}
//		if ((this.tagSelectionStart != null) && (this.tagSelectionEnd != null))
//			(new RuntimeException("TAG SELECTION CLEARED")).printStackTrace(System.out);
		this.tagSelectionStart = null;
		this.tagSelectionStartOffset = -1;
		this.tagSelectionEnd = null;
		this.tagSelectionEndOffset = -1;
		this.tagSelectionModCount = -1;
	}
	
	void clearSelections(boolean clearTwoClickAction) {
		this.clearTokenSelection(null);
		this.clearTagSelection(null);
		if (clearTwoClickAction && (this.pendingTwoClickAction != null)) {
			this.pendingTwoClickAction = null;
			if (this.twoClickActionMessenger != null)
				this.twoClickActionMessenger.twoClickActionChanged(null);
		}
	}
	
	void handleClickSelection(PointSelection ps, MouseEvent me) {
		this.handleClickSelection(ps, ((me.getModifiers() & MouseEvent.BUTTON3_MASK) != 0), me.getComponent(), me.getPoint(), me);
	}
	void handleClickSelection(PointSelection ps, boolean showContextMenu, Component comp, Point pt, MouseEvent meOrNull) {
		
		//	handle any pending two-click action first
		if (this.pendingTwoClickAction != null) {
			boolean handleAtomicAction = (this.pendingTwoClickAction.isAtomicAction() && !isAtomicActionRunning());
			try {
				if (handleAtomicAction) // might have been started from built-in click listener in selection action
					beginAtomicAction(this.pendingTwoClickAction.label);
				this.pendingTwoClickAction.performAction(ps);
			}
			finally {
				if (handleAtomicAction)
					endAtomicAction();
			}
			this.clearSelections(true);
			return;
		}
		
		//	cancel any context menu coming up for previous click (should be OK without synchronization lock because it all happens on EDT)
		if (this.clickContextMenu != null)
			this.clickContextMenu.cancel();
		
		//	observe click actions first (both for tag and for token selections)
		if (meOrNull != null) {
			InstantAction[] clickActions = this.getClickActions(ps, meOrNull);
			if (this.executeInstantAction(clickActions)) {
				this.clearSelections(true);
				return;
			}
		}
		
		//	normally show context menu as default action (wait for multi-click on very first click)
		if (this.showContextMenuOnMouseRelease || showContextMenu) {
			if ((meOrNull != null) && (meOrNull.getClickCount() < 2)) // TODO increase this threshold if we have triple-click actions at some point
				this.clickContextMenu = new DelayedClickContextMenu(ps, meOrNull);
			else this.showClickContextMenu(ps, comp, pt, null);
		}
//		else System.out.println("Not showing context menu for mouse click " + me.toString());
	}
	
	/*
TODO XM document markup panel context menu behavior:
- add mouse events and key typing events, respectively, as arguments to action getter mounting points ...
- ... simply to give implementations chance to check for 'Shift', 'Alt', and 'Ctrl'
  ==> should apply mostly to key typing events in practice, but might still be useful for clicks as well (at some point)

TODO XM document markup panel keyboard shortcuts and context menu behavior:
- add some sort of mounting point for selection based keyboard shortcuts ...
- ... and make damn sure to intercept from whatever text area might have focus ...
- ... getting shortcut action from mounting point ...
- ... for whatever selection exists, or wherever keyboard focus lies
  ==> actually, treat empty selection as click selection at cursor position in keyboard focus owner ...
  ==> ... and create mounting point methods for all three types of selection
  ==> also add abstract inner class for shortcut actions ...
  ==> ... but leave selection of action to return to implementation ...
  ==> ... merely handing over key typing events proper to mounting points
    ==> even facilitates implementing typing based text editing based upon click selections this way (at some point WAY AFTER APRIL DEADLINE)
  ==> ACTUALLY, add whole keyboard support only after April deadline
- add 'show context menu on selection finished' properties ...
- ... most likely separately for tokens and tags
- add selection action getter mounting point for click selections (e.g. on annotation tag connectors)
- use SwingUtilities 'is context menu trigger' call for showing context menu instead of direct check of mouse button masks
- add mouse events and key typing events, respectively, as arguments to action getter mounting points ...
- ... simply to give implementations chance to check for 'Shift', 'Alt', and 'Ctrl'
  ==> should apply mostly to key typing events in practice, but might still be useful for clicks as well (at some point)

TODO XM document markup panel keyboard inputs (mainly for shortcuts):
- work with and consume key press events (don't consume modifier keys, though) ...
- ... as key type events might not include modifier flags
- allow moving across text area boundaries (also from tokens to tags and the other way around) using arrow keys ...
- ... moving from bottom row of one panel to top row of next panel for down arrow (and other way around for up arrow) ...
- ... extending selection if shift down ...
- ... and also staying in same type of text area in that case
- use caret position as offset to get underlying anchor in token panels
- use caret position as offset to get position in annotation tag panels
  ==> add byte array to start tag producing method to record semantics of every charater ...
  ==> ... using 'T' for 'annotation type', 'N' for 'attribute name', 'V' for 'attribute value', and 'O' for 'other'
==> make damn sure caret is showing and flashing in focused text area
==> whole keyboard support might be candidate for later (post-deadline) extension, though
	 */
	DelayedClickContextMenu clickContextMenu = null;
	private class DelayedClickContextMenu extends Timer implements ActionListener {
		private PointSelection ps;
		private MouseEvent me;
		DelayedClickContextMenu(PointSelection ps, MouseEvent me) {
			/* waiting 250ms is a good compromise between reliably catching
			 * double clicks (usual gap is ~200ms) and having user wait
			 * unnecessarily long on single click */
			super(250, null);
			this.ps = ps;
			this.me = me;
			this.setRepeats(false);
			this.addActionListener(this); // cannot pass 'this' to super constructor
			this.start();
		}
		public void actionPerformed(ActionEvent ae) {
			clickContextMenu = null;
			showClickContextMenu(this.ps, this.me.getComponent(), this.me.getPoint(), this.me);
		}
		void cancel() {
			this.stop();
		}
	}
	
	private void showClickContextMenu(PointSelection ps, Component comp, Point pt, MouseEvent me) {
		this.showContextMenu(this.getActions(ps, me), comp, pt);
	}
	private void showContextMenu(SelectionAction[] actions, final Component comp, final Point pt) {
		if ((actions == null)/* || (actions.length == 0)*/) // TODO return on empty action array as well after tests
			return;
		final JPopupMenu pm = new JPopupMenu();
		final boolean[] isAdvancedSelectionAction = markAdvancedSelectionActions(actions);
		final JMenuItem[] mis = new JMenuItem[actions.length];
		int advancedSelectionActionCount = 0;
		for (int a = 0; a < actions.length; a++) {
			if (actions[a] == SelectionAction.SEPARATOR)
				continue;
//			mis[a] = actions[a].getMenuItem(this);
			mis[a] = actions[a].getContextMenuItem(this);
			this.addNotifier(mis[a], actions[a]);
			if (isAdvancedSelectionAction[a])
				advancedSelectionActionCount++;
		}
		this.fillContextMenu(pm, mis, isAdvancedSelectionAction);
		if (advancedSelectionActionCount != 0) {
			JMenuItem mmi = new JMenuItem("More ...");
			mmi.setBorder(BorderFactory.createLoweredBevelBorder());
			mmi.setBackground(new Color(240, 240, 240));
			mmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					fillContextMenu(pm, mis, null);
					pm.show(comp, pt.x, pt.y);
				}
			});
			pm.add(mmi);
		}
//		JMenuItem tmi = new JMenuItem("First Test Menu Item");
//		this.addNotifier(tmi, null);
//		pm.add(tmi); // TODOne remove this after tests !!!
		pm.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				if (DEBUG_SELECTION_HANDLING) System.out.println(" - contenxt menu become visible");
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
				if (DEBUG_SELECTION_HANDLING) System.out.println(" - contenxt menu become invisible");
			}
			public void popupMenuCanceled(PopupMenuEvent pme) {
				//	TODO try and remember whether or not last context menu was canceled before becoming invisible ...
				//	TODO ... and try and emulate 'focus gained' on clicked text area if not on 'mouse pressed'
				//	TODO ==> might help get selection visibility behavior as consistent as we'd like it
				//	TODO ==> might also want to log those events and see which differ between closing context menu with 'ESC' and closing it via click outside of it
				if (DEBUG_SELECTION_HANDLING) System.out.println(" - contenxt menu cancelled");
//				clearSelection(false); // DO NOT CLEAR SELECTION HERE, USER MIGHT SIMPLY WANT TO EXTEND IT
			}
		});
		if (DEBUG_SELECTION_HANDLING) System.out.println("Showing contenxt menu at " + pt.x + "/" + pt.y + " in " + comp);
		pm.show(comp, pt.x, pt.y);
	}
	private void fillContextMenu(JPopupMenu pm, JMenuItem[] mis, boolean[] isAdvancedSelectionAction) {
		int windowHeight = Integer.MAX_VALUE;
		Window topWindow = DialogFactory.getTopWindow();
		if ((topWindow != null) && topWindow.isVisible())
			windowHeight = topWindow.getHeight();
		else if (GraphicsEnvironment.isHeadless()) {}
		else {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (screenSize != null)
				windowHeight = screenSize.height;
		}
		pm.removeAll();
		boolean lastWasSeparator = true;
		int misHeight = 0;
		JMenu subMenu = null;
		for (int i = 0; i < mis.length; i++) {
			
			//	separator
			if (mis[i] == null) {
				if (!lastWasSeparator) {
					if (subMenu == null)
						pm.addSeparator();
					else subMenu.addSeparator();
				}
				lastWasSeparator = true;
				continue;
			}
			
			//	hide advanced actions in basic mode
			if ((isAdvancedSelectionAction != null) && isAdvancedSelectionAction[i])
				continue;
			
			//	test if menu items up to next separator fit current menu, and wrap into (new) sub menu if not (unless current (sub) menu empty or only one menu item left to add)
//			if (lastWasSeparator && (isAdvancedSelectionAction == null) && (misHeight != 0) && ((i+1) < mis.length)) {
			if (lastWasSeparator && (misHeight != 0) && ((i+1) < mis.length)) {
				int lMisHeight = 0;
				for (int li = i; li < mis.length; li++) {
					if (mis[li] == null)
						break; // got next separator, group ends
					lMisHeight += mis[li].getPreferredSize().height;
				}
				if (windowHeight < (misHeight + lMisHeight)) {
					JMenu mm = new JMenu("More ...");
					mm.setBorder(BorderFactory.createLoweredBevelBorder());
					mm.setBackground(new Color(240, 240, 240));
					if (subMenu == null)
						pm.add(mm);
					else subMenu.add(mm);
					subMenu = mm;
					misHeight = 0;
				}
			}
			
			//	add menu item to current (sub) menu
			lastWasSeparator = false;
			if (subMenu == null)
				pm.add(mis[i]);
			else subMenu.add(mis[i]);
			misHeight += mis[i].getPreferredSize().height;
		}
	}
	private void addNotifier(JMenuItem mi, final SelectionAction sa) {
		if (mi instanceof JMenu) {
			Component[] smcs = ((JMenu) mi).getMenuComponents();
			for (int c = 0; c < smcs.length; c++) {
				if (smcs[c] instanceof JMenuItem)
					this.addNotifier(((JMenuItem) smcs[c]), sa);
			}
		}
		else mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (sa instanceof TwoClickSelectionAction)
					clearSelections(false);
				else {
					clearSelections(true);
					selectionActionPerformed(sa);
				}
			}
		});
	}
	
	private boolean executeInstantAction(InstantAction[] clickActions) {
		if ((clickActions == null) || (clickActions.length == 0))
			return false;
		Arrays.sort(clickActions);
		for (int a = 0; a < clickActions.length; a++)
			if (clickActions[a].executeAction(this)) {
				clearSelections(true);
				return true;
			}
		return false;
	}
	
	//	TODO add central handling for keyboard input (only need to implement instant action handling once !!!)
	/* keys executing on 'pressed' event:
	 * - backspace, delete
	 * - return, tab
	 * - arrow keys
	 * - page up, page down
	 * - home, end
	 */
	void handleKeystroke(TokenMarkupPanel gtmp, KeyEvent ke) {
		if ((this.tokenSelectionStart == null) || (this.tokenSelectionEnd == null)) {
			this.clearTokenSelection(null); // TODOne really ??? ==> yes, something is badly off ...
			ke.consume(); // always consume event, non-editable text areas will beep otherwise (and we only want that for unallowed input)
			return;
		}
//		this.tokenSelectionEnd.tokenArea.requestFocusInWindow(); // call comes in from typing, focus is where we need it
		InstantAction[] instantActions;
		
		//	handle plain typing
//		if (this.tokenSelectionModCount == 0) { // NOT A SAFE INDICATOR, user might have gone out and come back in onto ame point with arrow keys
		if ((this.tokenSelectionStart == this.tokenSelectionEnd) && (this.tokenSelectionStartOffset == this.tokenSelectionEndOffset)) {
			if (DEBUG_KEYSTROKE_HANDLING) {
				System.out.println("Got token typing selection:");
				System.out.println(" - selected is " + /*this.tokenSelectionStart.para.index + "/" + */this.tokenSelectionStart.index);
				System.out.println(" - cursor at offset " + this.tokenSelectionStartOffset);
			}
			PointSelection leftPs = ((this.tokenSelectionStartOffset == 0) ? null : createPointSelection(gtmp, (this.tokenSelectionStartOffset - 1)));
			PointSelection rightPs = ((this.tokenSelectionStartOffset == gtmp.getAnchorCount()) ? null : createPointSelection(gtmp, this.tokenSelectionStartOffset));
			instantActions = this.getKeystrokeActions(leftPs, rightPs, ke);
		}
		
		//	handle (potential) keyboard shortcut for token selection
		else {
			TempTokenSelection tokenSelection = new TempTokenSelection(this.tokenSelectionStart, this.tokenSelectionStartOffset, this.tokenSelectionEnd, this.tokenSelectionEndOffset);
			if (DEBUG_KEYSTROKE_HANDLING) {
				System.out.println("Got token selection:");
				System.out.println(" - first token area at " + tokenSelection.firstGtmp.index + " selected from " + tokenSelection.firstGtmpOffset);
				System.out.println(" - last token area at " + tokenSelection.lastGtmp.index + " selected to " + tokenSelection.lastGtmpOffset);
			}
			TokenSelection ts;
			if (tokenSelection.firstGtmp == tokenSelection.lastGtmp)
				ts = new TokenSelection(tokenSelection.firstGtmp, tokenSelection.firstGtmpOffset, tokenSelection.lastGtmpOffset, tokenSelection.startEndInverted);
			else ts = new TokenSelection(tokenSelection.firstGtmp, tokenSelection.firstGtmpOffset, tokenSelection.lastGtmp, tokenSelection.lastGtmpOffset, tokenSelection.startEndInverted);
			instantActions = this.getKeystrokeActions(ts, ke);
		}
		
		//	execute whichever action(s) we got
		if (this.executeInstantAction(instantActions)) {
//			if (xtcp != null) 
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						restoreTokenCaretPosition(xtcp, false);
//					}
//				});
		}
		else Toolkit.getDefaultToolkit().beep();
		ke.consume(); // always consume event, non-editable text areas will beep otherwise (and we only want that for unallowed input)
	}
	private static PointSelection createPointSelection(TokenMarkupPanel tmp, int offset) {
		TokenMarkupPanelObjectTray tmpot = tmp.getObjectTrayAt(offset);
		if ((tmpot == null) || (tmpot.index == TokenMarkupPanelObjectTray.WHITESPACE_INDEX))
			return new PointSelection(PointSelection.POINT_TYPE_SPACE, ((Token) null), -1, -1);
		else if (-1 < tmpot.index) {
			Token token = ((Token) tmpot.object);
			int tokenOffset = 0;
			for (int o = offset; o != 0; o--) {
				TokenMarkupPanelObjectTray cTmpot = (tmp.getObjectTrayAt(o-1));
				if ((cTmpot == null) || (cTmpot.index == TokenMarkupPanelObjectTray.WHITESPACE_INDEX))
					break; // hit space before token start
				if (cTmpot.index < 0)
					break; // hit annotation highlight end cap before token start
				if (cTmpot.object != token)
					break; // hit preceding token
				tokenOffset++;
			}
			return new PointSelection(PointSelection.POINT_TYPE_TOKEN, token, tmpot.index, tokenOffset);
		}
		else if (tmpot.index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX)
			return new PointSelection(PointSelection.POINT_TYPE_ANNOTATION_START_CAP, ((Annotation) tmpot.object), -1);
		else if (tmpot.index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX)
			return new PointSelection(PointSelection.POINT_TYPE_ANNOTATION_END_CAP, ((Annotation) tmpot.object), -1);
		else return new PointSelection(PointSelection.POINT_TYPE_SPACE, ((Token) null), -1, -1); // just to satisfy compiler ...
	}
	private static class TokenCaretPosition {
		final int leftTokenIndex;
		final int leftTokenDist;
		final int rightTokenIndex;
		final int rightTokenDist;
		TokenCaretPosition(int leftTokenIndex, int leftTokenDist, int rightTokenIndex, int rightTokenDist) {
			this.leftTokenIndex = leftTokenIndex;
			this.leftTokenDist = leftTokenDist;
			this.rightTokenIndex = rightTokenIndex;
			this.rightTokenDist = rightTokenDist;
		}
	}
	private TokenCaretPosition getTokenCaretPosition(TokenMarkupPanel gtmp, int caretPos) {
		int leftTokenIndex = -1;
		int leftTokenDist = -1;
		int rightTokenIndex = -1;
		int rightTokenDist = -1;
		TokenMarkupPanelObjectTray xma = gtmp.getObjectTrayAt(caretPos);
		if ((xma != null) && (-1 < xma.index)) /* within token, measure exact position left and right */ {
			leftTokenIndex = xma.index;
			leftTokenDist = 0;
			for (int o = caretPos; o != 0; o--) {
				if (gtmp.getObjectTrayAt(o-1) == xma)
					leftTokenDist++;
				else break;
			}
			rightTokenIndex = xma.index;
			rightTokenDist = 0;
			for (int o = caretPos; o < gtmp.getAnchorCount(); o++) {
				if (gtmp.getObjectTrayAt(o) == xma)
					rightTokenDist++;
				else break;
			}
			return new TokenCaretPosition(leftTokenIndex, leftTokenDist, rightTokenIndex, rightTokenDist);
		}
		TokenMarkupPanel leftGtmp = gtmp;
		leftTokenDist = 0;
		for (int o = caretPos; o != 0; o--) {
			TokenMarkupPanelObjectTray lXma = gtmp.getObjectTrayAt(o-1);
			if ((lXma != null) && (-1 < lXma.index)) {
				leftTokenIndex = lXma.index;
				break;
			}
			leftTokenDist++;
			if (1 < o) { /* continue scanning current panel */ }
			else if (leftGtmp.index == 0) { /* no more panels to move to within paragraph */ }
			else {
				leftGtmp = ((TokenMarkupPanel) this.tokenPanels.get(leftGtmp.index - 1));
				o = (leftGtmp.getAnchorCount() + 1); // compensate loop decrement
			}
		}
		TokenMarkupPanel rightGtmp = gtmp;
		rightTokenDist = 0;
		for (int o = caretPos; o < gtmp.getAnchorCount(); o++) {
			TokenMarkupPanelObjectTray rXma = gtmp.getObjectTrayAt(o);
			if ((rXma != null) && (-1 < rXma.index)) {
				rightTokenIndex = rXma.index;
				break;
			}
			rightTokenDist++;
			if ((o + 1) < rightGtmp.getAnchorCount()) { /* continue scanning current panel */ }
			else if ((rightGtmp.index + 1) == this.tokenPanels.size()) { /* no more panels to move to within paragraph */ }
			else {
				rightGtmp = ((TokenMarkupPanel) this.tokenPanels.get(rightGtmp.index + 1));
				o = -1; // compensate loop increment
			}
		}
		if ((leftTokenIndex == -1) && (rightTokenIndex == -1))
			return null;
		return new TokenCaretPosition(leftTokenIndex, leftTokenDist, rightTokenIndex, rightTokenDist);
	}
	void restoreTokenCaretPosition(TokenCaretPosition xtcp, boolean selectionEnd) {
		System.out.println("Restoring token " + (selectionEnd ? "selection end" : "caret position") + " at " + xtcp.leftTokenIndex + "+" + xtcp.leftTokenDist + "/" + xtcp.rightTokenIndex + "+" + xtcp.rightTokenDist);
		TokenMarkupPanel leftGtmp = null;
		if (xtcp.leftTokenIndex == -1) {}
		else leftGtmp = ((TokenMarkupPanel) this.tokenPanelAtIndex.get(xtcp.leftTokenIndex));
		TokenMarkupPanel rightGtmp = null;
		if (xtcp.rightTokenIndex == -1) {}
		else if (xtcp.rightTokenIndex == xtcp.leftTokenIndex)
			rightGtmp = leftGtmp;
		else rightGtmp = ((TokenMarkupPanel) this.tokenPanelAtIndex.get(xtcp.rightTokenIndex));
		if ((leftGtmp == null) && (rightGtmp == null)) // no tokens panels to work with at all
			return;
		TokenMarkupPanel caretGtmp;
		int caretPos;
		if (leftGtmp == null) {
			int rightTokenStartOffset = rightGtmp.getFirstAnchorOffsetOf(xtcp.rightTokenIndex);
			if (rightTokenStartOffset == -1)
				return;
			caretGtmp = rightGtmp;
			caretPos = Math.max(0, (rightTokenStartOffset - xtcp.rightTokenDist));
		}
		else if (rightGtmp == null) {
			int leftTokenEndOffset = leftGtmp.getLastAnchorOffsetOf(xtcp.leftTokenIndex);
			if (leftTokenEndOffset == -1)
				return;
			leftTokenEndOffset++; // actually need this AFTER last anchor
			caretGtmp = leftGtmp;
			caretPos = Math.min(leftGtmp.getAnchorCount(), (leftTokenEndOffset + xtcp.leftTokenDist));
		}
		else if (leftGtmp == rightGtmp) {
			caretGtmp = leftGtmp;
			if (xtcp.leftTokenIndex == xtcp.rightTokenIndex) {
				int tokenStartOffset = leftGtmp.getFirstAnchorOffsetOf(xtcp.leftTokenIndex);
				int tokenEndOffset = rightGtmp.getLastAnchorOffsetOf(xtcp.rightTokenIndex);
				tokenEndOffset++; // actually need this AFTER last anchor
				int lCaretPos = (tokenStartOffset + xtcp.leftTokenDist);
				int rCaretPos = (tokenEndOffset - xtcp.rightTokenDist);
				caretPos = ((lCaretPos + rCaretPos) / 2);
			}
			else {
				int leftTokenEndOffset = leftGtmp.getLastAnchorOffsetOf(xtcp.leftTokenIndex);
				leftTokenEndOffset++; // actually need this AFTER last anchor
				int rightTokenStartOffset = rightGtmp.getFirstAnchorOffsetOf(xtcp.rightTokenIndex);
				int lCaretPos = (leftTokenEndOffset + xtcp.leftTokenDist);
				int rCaretPos = (rightTokenStartOffset - xtcp.rightTokenDist);
				caretPos = ((lCaretPos + rCaretPos) / 2);
			}
		}
		else {
			ArrayList gtmpAtOffset = new ArrayList();
			int leftTokenEndOffset = leftGtmp.getLastAnchorOffsetOf(xtcp.leftTokenIndex);
			leftTokenEndOffset++; // actually need this AFTER last anchor
			for (int o = leftTokenEndOffset; o <= leftGtmp.getAnchorCount(); o++)
				gtmpAtOffset.add(leftGtmp);
			for (int p = (leftGtmp.index + 1); p < rightGtmp.index; p++) {
				TokenMarkupPanel pGtmp = ((TokenMarkupPanel) this.tokenPanels.get(p));
				for (int o = 0; o < pGtmp.getAnchorCount(); o++)
					gtmpAtOffset.add(pGtmp);
			}
			int rightTokenStartOffset = rightGtmp.getFirstAnchorOffsetOf(xtcp.rightTokenIndex);
			for (int o = 0; o <= rightTokenStartOffset; o++)
				gtmpAtOffset.add(rightGtmp);
			int lCaretPos = xtcp.leftTokenDist;
			int rCaretPos = (gtmpAtOffset.size() - xtcp.rightTokenDist);
			int oCaretPos = ((lCaretPos + rCaretPos) / 2);
			caretGtmp = ((TokenMarkupPanel) gtmpAtOffset.get(oCaretPos));
			caretPos = 0;
			for (int o = oCaretPos; o != 0; o--) {
				if (gtmpAtOffset.get(o-1) == caretGtmp)
					caretPos++;
				else break;
			}
		}
		if (caretGtmp.isFoldedAway()) {
			System.out.println(" ==> tokens folded away");
			return; // nothing we could do about this one
		}
		caretGtmp.tokenArea.requestFocusInWindow();
		caretGtmp.tokenArea.setCaretPosition(caretPos);
		if (selectionEnd)
			this.tokenSelectionModified(caretGtmp, caretPos);
		else this.tokenSelectionStarted(caretGtmp, caretPos);
	}
	
	void handleKeystroke(AnnotTagPanel gatp, KeyEvent ke) {
		if ((this.tagSelectionStart == null) || (this.tagSelectionEnd == null)) {
			this.clearTagSelection(null); // TODOne really ??? ==> yes, something is badly off ...
			ke.consume(); // always consume event, non-editable text areas will beep otherwise (and we only want that for unallowed input)
			return;
		}
//		this.tagSelectionEnd.tagArea.requestFocusInWindow(); // call comes in from typing, focus is where we need it
		InstantAction[] instantActions;
		
		//	handle plain typing
//		if (this.tagSelectionModCount == 0) { // NOT A SAFE INDICATOR, user might have gone out and come back in onto ame point with arrow keys
		if ((this.tagSelectionStart == this.tagSelectionEnd) && (this.tagSelectionStartOffset == this.tagSelectionEndOffset)) {
			if (DEBUG_KEYSTROKE_HANDLING) {
				System.out.println("Got tag typing selection:");
				System.out.println(" - tag at " + this.tagSelectionStart.index + " selected from " + this.tagSelectionStartOffset);
				System.out.println(" - cursor at offset " + this.tagSelectionStartOffset);
			}
			int csType = (this.tagSelectionStart.isStartTag ? PointSelection.POINT_TYPE_ANNOTATION_START_TAG : PointSelection.POINT_TYPE_ANNOTATION_END_TAG);
			PointSelection leftPs = ((this.tagSelectionStartOffset == 0) ? null : new PointSelection(csType, this.tagSelectionStart.annot, (this.tagSelectionStartOffset - 1)));
			PointSelection rightPs = ((this.tagSelectionStartOffset == gatp.tagArea.getDocument().getLength()) ? null : new PointSelection(csType, this.tagSelectionStart.annot, this.tagSelectionStartOffset));
			instantActions = this.getKeystrokeActions(leftPs, rightPs, ke);
		}
		
		//	handle (potential) keyboard shortcut for tag selection
		else {
			AnnotTagSelection tagSelection = new AnnotTagSelection(this.tagSelectionStart, this.tagSelectionStartOffset, this.tagSelectionEnd, this.tagSelectionEndOffset);
			if (DEBUG_KEYSTROKE_HANDLING) {
				System.out.println("Got tag selection:");
				System.out.println(" - first tag at " + tagSelection.firstGatp.index + " selected from " + tagSelection.firstGatpOffset);
				System.out.println(" - last tag at " + tagSelection.lastGatp.index + " selected to " + tagSelection.lastGatpOffset);
			}
			TagSelection ts;
			if (tagSelection.firstGatp == tagSelection.lastGatp)
				ts = new TagSelection(tagSelection.firstGatp, tagSelection.firstGatpOffset, tagSelection.lastGatpOffset, tagSelection.startEndInverted);
			else ts = new TagSelection(tagSelection.firstGatp, tagSelection.firstGatpOffset, tagSelection.lastGatp, tagSelection.lastGatpOffset, tagSelection.startEndInverted);
			instantActions = this.getKeystrokeActions(ts, ke);
		}
		
		//	execute whichever action(s) we got
		if (this.executeInstantAction(instantActions)) {
//			if (xtcp != null) 
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						restoreTagCaretPosition(xtcp, false);
//					}
//				});
		}
		else Toolkit.getDefaultToolkit().beep();
		ke.consume(); // always consume event, non-editable text areas will beep otherwise (and we only want that for unallowed input)
	}
	private static class TagCaretPosition {
		final AnnotTagPanel gatp;
		final int tagLength;
		final int startDist;
//		final int endDist;
		TagCaretPosition(AnnotTagPanel gatp, int tagLength, int startDist/*, int endDist*/) {
			this.gatp = gatp;
			this.tagLength = tagLength;
			this.startDist = startDist;
//			this.endDist= endDist;
		}
	}
	private TagCaretPosition getTagCaretPosition(AnnotTagPanel gatp, int caretPos) {
		int tagLength = gatp.tagArea.getDocument().getLength();
		int startDist = caretPos;
		if (startDist < 0)
			startDist = 0;
//		int endDist = (tagLength - startDist);
//		if (endDist < 0)
//			endDist = 0;
		return new TagCaretPosition(gatp, tagLength, startDist/*, endDist*/);
	}
	private void restoreTagCaretPosition(TagCaretPosition xtcp, boolean selectionEnd) {
		System.out.println("Restoring tag " + (selectionEnd ? "selection end" : "caret position") + " in " + xtcp.gatp.annot.getType() + " at " + xtcp.startDist + " of " + xtcp.tagLength);
		boolean gatpInvisible = true;
		for (Component comp = xtcp.gatp; comp != null;) {
			if (comp == GamtaDocumentMarkupPanel.this) {
				gatpInvisible = false;
				break;
			}
			else comp = comp.getParent();
		}
		if (gatpInvisible) {
			System.out.println(" ==> tag no longer visible");
			return; // nothing we could do about this one
		}
		int tagLength = xtcp.gatp.tagArea.getDocument().getLength();
		int caretPos;
		if (tagLength == xtcp.tagLength)
			caretPos = xtcp.startDist;
		else caretPos = ((xtcp.startDist * tagLength) / xtcp.tagLength);
		if (caretPos < 0)
			caretPos = 0;
		else if (xtcp.gatp.tagArea.getDocument().getLength() < caretPos)
			caretPos = xtcp.gatp.tagArea.getDocument().getLength();
		if (caretPos <= xtcp.gatp.tagArea.getDocument().getLength())
			xtcp.gatp.tagArea.setCaretPosition(caretPos);
		else xtcp.gatp.tagArea.setCaretPosition(tagLength);
		xtcp.gatp.tagArea.requestFocusInWindow();
		xtcp.gatp.tagArea.setCaretPosition(caretPos);
		if (selectionEnd)
			this.tagSelectionModified(xtcp.gatp, caretPos);
		else this.tagSelectionStarted(xtcp.gatp, caretPos);
	}
	
	/*
More on keyboard input in GAMTA document markup panel:
- allow setting token value through point selection ...
- ... setting clean mod counter internally to avoid annoying display update ...
- ... and updating token panel 'object at offset' directly
  ==> typing support kind of integrated yet again ...
  ==> ... if with more scrutiny ...
  ==> ... for benefit of instant display update without refresh
    ==> add source panel pointer and offset to point selection
    ==> HAVE DARN GOOD THINK ABOUT THIS !!!
- likewise, allow removing annotation via point selection for deletion of highlight end cap ...
- ... again performing display updates directly and setting mod counters to clean
- most likely also allow changing annotation type or renaming attribute or changing attribute value for tag panel pont selections ...
- ... again performing display updates directly (both tag panels for type change) and setting mod counters to clean
  ==> allows scrutinizing qName changes at very least ...
  ==> ... and outputting beeps for invalid inputs (from gizmos calling those methods, of course)
  ==> REALLY HAVE DARN GOOD THINK ABOUT THIS !!!
- from both token and tag panels, hand relevant 'key pressed' and 'key typed' events down to typing handler ...
- ... also passing panel proper and event offset alongside to generate point selection objects
- instant action executed or not, consume events in common handler method
  ==> no more annoying beeps on typing within read-only text area
==> whole thing should also work in XMF document display panel ...
==> ... safe for token value updates, of course ...
==> ... but might actually allow to even add or remove space from token boundary ...
==> ... if with adding second space producing beep
  ==> whitespace point selection needs index of preceding token ...
  ==> ... and token point selection might actually well need index of token proper
- augment annotation start tag index with unescaped offsets in attribute values
  ==> only way to support attribute value editing via typing in tag panel
  ==> most likely will need to beep and do nothing on character input in middle of escaped XML entity ...
  ==> ... while removing whole entity for 'backspace' or 'delete'
  ==> most likely use array of shorts ...
  ==> ... storing 'type at position' in high 2-3 bits and non-escaped offset in remaining 14-13 bits
    ==> tags should hardly ever grow longer than 8K characters ...
    ==> ... and if so, we can switch to ints
      ==> in fact, use ints right away, as bibRefCitations and figureCitations can have _darn_ long attribute values
- add comment to two-point-selection version of instant action getter ...
- ... explaining that either one selection could be null ...
- ... and type of argument keyboard input event could be either 'key pressed' or 'key typed' ...
- ... with no two calls ever occurring for same key stroke, though
  ==> do that forking in key listeners ...
  ==> ... passing through only relevant events from either method
    ==> create (most likely static) 'handle on key pressed' method ...
    ==> ... indicating events text components handle on 'key pressed' ...
    ==> ... possibly due to them not generating 'key typed' events at all
	 */
	
	/**
	 * Retrieve the annotation types currently registered.
	 * @return an array holding the types
	 */
	public String[] getAnnotationTypes() {
		return ((String[]) this.annotColors.keySet().toArray(new String[this.annotColors.size()]));
	}
	
	/**
	 * Check whether or not a specific annotation is visible, at least in terms
	 * of its textual content. In particular, this tests whether or not any
	 * parent annotations are folded away. For empty structural annotations,
	 * this method indicates whether or not the annotation tags are visible.
	 * @param annot the annotation whose visibility to check
	 * @return true if the argument annotation is visible, false otherwise
	 */
	public boolean isAnnotationVisible(Annotation annot) {
		AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annot.getAnnotationID()));
		if (gamp != null)
			return !gamp.isFoldedAway();
		Annotation docAnnot = this.document.getAnnotation(annot.getAnnotationID());
		for (int t = docAnnot.getStartIndex(); t < docAnnot.getEndIndex(); t++) {
			Token token = this.document.tokenAt(t);
			TokenMarkupPanel gtmp = this.getTokenPanel(token);
			if (gtmp == null)
				continue;
			if (gtmp.isFoldedAway())
				t = gtmp.maxTokenIndex; // no use checking tokens one by one (we won't get highlight-end-cap-only panels fro any tokens)
			else return true;
		}
		return false;
	}
	
	public void ensureAnnotationVisible(final Annotation annot, final boolean showContent, final DisplayAdjustmentObserver dao) {
		
		//	ensure we're on the event dispatcher
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ensureAnnotationVisible(annot, showContent, dao);
				}
			});
			return;
		}
		
		//	make sure tag showing annotation not folded away
		if (this.getDetailAnnotationDisplayMode(annot.getType()) == DISPLAY_MODE_SHOW_TAGS) {
			final AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annot.getAnnotationID()));
			if (gamp == null) {
				if (dao != null)
					dao.displayAdjustmentFinished(false);
				return;
			}
			if (gamp.isFoldedAway())
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						unfoldAnnotationPanel(gamp, showContent, dao);
					}
				});
			else if (showContent && gamp.contentFolded) {
				gamp.toggleContentFolded();
				validateLayout();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ensureAnnotationVisible(annot, showContent, dao);
					}
				});
			}
			else if (dao != null)
				dao.displayAdjustmentFinished(true);
		}
		
		//	make sure tokens of other annotations not folded away
		else {
			Annotation docAnnot = this.document.getAnnotation(annot.getAnnotationID());
			for (int t = docAnnot.getStartIndex(); t < docAnnot.getEndIndex(); t++) {
				Token token = this.document.tokenAt(t);
				final TokenMarkupPanel gtmp = this.getTokenPanel(token);
				if (gtmp == null)
					continue;
				if (gtmp.isFoldedAway()) {
					this.unfoldAnnotationPanel(gtmp, true, new DisplayAdjustmentObserver() {
						public void displayAdjustmentFinished(boolean success) {
							if (success) {
								validateLayout();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										System.out.println("Asynchronous recursion after unfolding first paragraph parents");
										ensureAnnotationVisible(annot, showContent, dao);
									}
								});
							}
							else if (dao != null)
								dao.displayAdjustmentFinished(false);
						}
					});
					return;
				}
				else t = gtmp.maxTokenIndex; // no need checking tokens one by one, whole panel is folded away or not
			}
			if (dao != null)
				dao.displayAdjustmentFinished(true);
		}
	}
	private void unfoldAnnotationPanel(final JPanel annotPanel, final boolean unfoldPanel, final DisplayAdjustmentObserver dao) {
		System.out.println("Unfolding " + annotPanel);
		Annotation[] spanningAnnots;
		if (annotPanel instanceof TokenMarkupPanel)
			spanningAnnots = this.document.getAnnotationsSpanning(((TokenMarkupPanel) annotPanel).minTokenIndex, (((TokenMarkupPanel) annotPanel).maxTokenIndex + 1));
		else spanningAnnots = this.document.getAnnotationsSpanning(((AnnotMarkupPanel) annotPanel).annot.getAbsoluteStartIndex(), (((AnnotMarkupPanel) annotPanel).annot.getAbsoluteStartIndex() + ((AnnotMarkupPanel) annotPanel).annot.size()));
		System.out.println(" - got " + spanningAnnots.length + " spanning annotations");
		for (int a = 0; a < spanningAnnots.length; a++) {
			System.out.println(" - checking " + createStartTag(spanningAnnots[a]));
			Character displayMode = this.getDetailAnnotationDisplayMode(spanningAnnots[a].getType());
			if (displayMode == DISPLAY_MODE_INVISIBLE) {
				System.out.println("   ==> not showing at all");
				continue; // not showing, cannot be our culprit
			}
			AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(spanningAnnots[a].getAnnotationID()));
			if (gamp == null) {
				System.out.println("   ==> panel never created");
				continue; // never even shown, and itself folded away now
			}
			if (gamp.contentFolded) {
				System.out.println("   ==> unfolded");
				gamp.toggleContentFolded();
				this.validateLayout();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						System.out.println("   ==> asynchronous recursion after unfolding parent panel");
						unfoldAnnotationPanel(annotPanel, unfoldPanel, dao);
					}
				});
				return;
			}
			else System.out.println("   ==> not folded");
		}
		if (unfoldPanel) {
			boolean unfolded = false;
			if ((annotPanel instanceof AnnotMarkupPanel) && ((AnnotMarkupPanel) annotPanel).contentFolded) {
				((AnnotMarkupPanel) annotPanel).toggleContentFolded();
				unfolded = true;
			}
			if (unfolded) {
				this.validateLayout();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						System.out.println("   ==> asynchronous recursion after unfolding target panel");
						unfoldAnnotationPanel(annotPanel, unfoldPanel, dao);
					}
				});
				return;
			}
		}
		if (dao != null)
			dao.displayAdjustmentFinished(true);
	}
	
	private TokenMarkupPanel getTokenPanel(Token token) {
		//	TODO use binary search !!!
		for (int p = 0; p < this.tokenPanels.size(); p++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(p));
			if (gtmp.maxTokenEndOffset <= token.getStartOffset())
				continue;
			if (token.getEndOffset() <= gtmp.minTokenStartOffset)
				break;
			return gtmp;
		}
		return null;
	}
	
	/**
	 * Check whether or not a specific token is visible. In particular, this
	 * tests whether or not any parent annotations are folded away.
	 * @param token the token whose visibility to check
	 * @return true if the argument token is visible, false otherwise
	 */
	public boolean isTokenVisible(Token token) {
		TokenMarkupPanel gtmp = this.getTokenPanel(token);
		return ((gtmp != null) && !gtmp.isFoldedAway());
	}
	
	public void ensureTokenVisible(final Token token, final DisplayAdjustmentObserver dao) {
		
		//	ensure we're on the event dispatcher
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ensureTokenVisible(token, dao);
				}
			});
			return;
		}
		
		//	make sure home panel of token not folded away
		final TokenMarkupPanel gtmp = this.getTokenPanel(token);
		if (gtmp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		if (gtmp.isFoldedAway())
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					unfoldAnnotationPanel(gtmp, true, dao);
				}
			});
		else if (dao != null)
			dao.displayAdjustmentFinished(true);
	}
	
	public void setSelectedObject(final Object object, final boolean showParents, final DisplayAdjustmentObserver dao) {
		if (DEBUG_SET_SELECTED_OBJECT) System.out.println("Setting selected object to " + object);
		
		//	nothing to work with
		if (this.xdvp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		
		//	make sure to adjust selection after end of any atomic action
		if (this.isAtomicActionRunning()) {
			this.atomicActionViewPositionAdjuster = new Runnable() {
				public void run() {
					setSelectedObject(object, showParents, dao);
				}
			};
			if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> enqueued for end of atomic action " + this.atomicActionId);
			return;
		}
		
		//	ensure we're on the event dispatcher
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setSelectedObject(object, showParents, dao);
				}
			});
			if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> moved onto EDT");
			return;
		}
		
		//	ensure token or annotation not folded away
		if (object instanceof Token) {
			Token token = ((Token) object);
			TokenMarkupPanel gtmp = this.getTokenPanel(token);
			if (gtmp == null) {
				if (dao != null)
					dao.displayAdjustmentFinished(false);
				if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> token panel not found");
				return;
			}
			if (gtmp.isFoldedAway()) {
				if (!showParents) {
					if (dao != null)
						dao.displayAdjustmentFinished(false);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> cannot un-fold parent");
					return;
				}
				this.unfoldAnnotationPanel(gtmp, true, new DisplayAdjustmentObserver() {
					public void displayAdjustmentFinished(boolean success) {
						if (success) {
							validateLayout();
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									System.out.println("Asynchronous recursion after unfolding paragraph parents (1)");
									setSelectedObject(object, showParents, dao);
								}
							});
						}
						else if (dao != null)
							dao.displayAdjustmentFinished(false);
					}
				});
				if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> re-enqueued after un-folding parent");
				return;
			}
		}
		else if (object instanceof Annotation) {
			Annotation annot = ((Annotation) object);
			AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annot.getAnnotationID()));
			if (gamp == null) {
				Character displayMode = this.getDetailAnnotationDisplayMode(annot.getType());
				if (displayMode == DISPLAY_MODE_INVISIBLE) /* no panel because tags never shown, set visible and recourse */ {
					Character prefDadm = this.getPreferredDetailAnnotationShowingMode(annot.getType());
					this.setDetailAnnotationDisplayMode(annot.getType(), ((prefDadm == null) ? DISPLAY_MODE_SHOW_HIGHLIGHTS : prefDadm), true, true);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							System.out.println("Asynchronous recursion after switching structural annotation tags visible");
							setSelectedObject(object, showParents, dao);
						}
					});
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> re-enqueued after switching annotations visible");
					return;
				}
				else if (displayMode == DISPLAY_MODE_SHOW_HIGHLIGHTS) {}
				else {
					if (dao != null)
						dao.displayAdjustmentFinished(false);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> annottation panel not found (display mode is '" + displayMode + "')");
					return;
				}
			}
			else if (gamp.isFoldedAway()) {
				if (!showParents) {
					if (dao != null)
						dao.displayAdjustmentFinished(false);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> cannot un-fold parent");
					return;
				}
				this.unfoldAnnotationPanel(gamp, false, new DisplayAdjustmentObserver() {
					public void displayAdjustmentFinished(boolean success) {
						if (success) {
							validateLayout();
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									System.out.println("Asynchronous recursion after unfolding annotation parents");
									setSelectedObject(object, showParents, dao);
								}
							});
						}
						else if (dao != null)
							dao.displayAdjustmentFinished(false);
					}
				});
				if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> re-enqueued after un-folding parent");
				return;
			}
		}
		else /* cannot select document proper or supplement or whatever */ {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			if (DEBUG_SET_SELECTED_OBJECT) {
				if (object == null)
					System.out.println(" ==> cannot set selection to null");
				else System.out.println(" ==> unknown object type " + object.getClass().getName());
			}
			return;
		}
		
		//	ensure annotation highlighted or showing tags (need to do this first to ensure we have annotation panel to work with)
		if (object instanceof Annotation) {
			Annotation annot = ((Annotation) object);
			Character displayMode = this.getDetailAnnotationDisplayMode(annot.getType());
			if (displayMode == DISPLAY_MODE_INVISIBLE) {
				Character prefDadm = this.getPreferredDetailAnnotationShowingMode(annot.getType());
				this.setDetailAnnotationDisplayMode(annot.getType(), ((prefDadm == null) ? DISPLAY_MODE_SHOW_HIGHLIGHTS : prefDadm), true, true);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						System.out.println("Asynchronous recursion after switching structural annotation tags visible");
						setSelectedObject(object, showParents, dao);
					}
				});
				if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> re-enqueued after switching annotations visible");
				return;
			}
		}
		
		//	move panel associated with object to visible area and inject selection (easier to do this on one go, as we'd otherwise be getting panels time and again)
		if (object instanceof Token) {
			Token token = ((Token) object);
			TokenMarkupPanel gtmp = this.getTokenPanel(token);
			if (gtmp == null) {
				if (dao != null)
					dao.displayAdjustmentFinished(false);
				return;
			}
			int firstAnchorOffset = gtmp.getFirstAnchorOffsetOf(token);
			if (firstAnchorOffset == -1) {
				if (dao != null)
					dao.displayAdjustmentFinished(false);
				return;
			}
			int lastAnchorOffset = gtmp.getLastAnchorOffsetOf(token);
			if (lastAnchorOffset == -1) {
				if (dao != null)
					dao.displayAdjustmentFinished(false);
				return;
			}
			this.clearSelections(true);
			this.tokenSelectionStarted(gtmp, firstAnchorOffset);
			this.tokenSelectionModified(gtmp, (lastAnchorOffset + 1));
			this.tokenSelectionEnded(gtmp, (lastAnchorOffset + 1), null);
			this.xdvp.moveIntoView(gtmp, gtmp.getPositionOf(token), dao);
			if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> token selected");
			return;
		}
		else if (object instanceof Annotation) {
			Annotation annot = ((Annotation) object);
			Character displayMode = this.getDetailAnnotationDisplayMode(annot.getType());
			if (displayMode == DISPLAY_MODE_SHOW_TAGS) {
				AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annot.getAnnotationID()));
				if (gamp.contentFolded) {
					this.clearSelections(true);
					this.tagSelectionStarted(gamp.startTagPanel, 0);
					this.tagSelectionModified(gamp.startTagPanel, gamp.startTagPanel.getTagLength());
					this.tagSelectionEnded(gamp.startTagPanel, gamp.startTagPanel.getTagLength(), null);
					this.xdvp.moveIntoView(gamp.startTagPanel, null, dao);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> annotation start tag selected");
					return;
				}
				else {
					this.clearSelections(true);
					this.tagSelectionStarted(gamp.startTagPanel, 0);
					this.tagSelectionModified(gamp.endTagPanel, gamp.endTagPanel.getTagLength());
					this.tagSelectionEnded(gamp.endTagPanel, gamp.endTagPanel.getTagLength(), null);
					this.xdvp.moveIntoView(gamp.startTagPanel, null, gamp.endTagPanel, null, dao);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> annotation tags selected");
					return;
				}
			}
			else if (displayMode == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
				Annotation docAnnot = this.document.getAnnotation(annot.getAnnotationID());
				TokenMarkupPanel firstGtmp = ((TokenMarkupPanel) this.tokenPanelAtIndex.get(docAnnot.getStartIndex()));
				if (firstGtmp == null) {
					if (dao != null)
						dao.displayAdjustmentFinished(false);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> first token panel nut found (1)");
					return;
				}
				int startAnchorOffset = firstGtmp.getStartAnchorOffsetOf(docAnnot);
				if (startAnchorOffset == -1) /* nested annotation with same first token might have tags showing */ {
					if (firstGtmp.index == 0) {
						if (dao != null)
							dao.displayAdjustmentFinished(false);
						if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> first token panel nut found (2)");
						return;
					}
					if (docAnnot.getStartIndex() != firstGtmp.minTokenIndex) {
						if (dao != null)
							dao.displayAdjustmentFinished(false);
						if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> first token panel nut found (3)");
						return;
					}
					firstGtmp = ((TokenMarkupPanel) this.tokenPanels.get(firstGtmp.index - 1));
					startAnchorOffset = firstGtmp.getStartAnchorOffsetOf(docAnnot);
					if (startAnchorOffset == -1) {
						if (dao != null)
							dao.displayAdjustmentFinished(false);
						if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> annotation start cap not found");
						return;
					}
				}
				TokenMarkupPanel lastGtmp = ((TokenMarkupPanel) this.tokenPanelAtIndex.get(docAnnot.getEndIndex() - 1));
				if (lastGtmp == null) {
					if (dao != null)
						dao.displayAdjustmentFinished(false);
					if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> last token panel nut found (1)");
					return;
				}
				int endAnchorOffset = lastGtmp.getEndAnchorOffsetOf(docAnnot);
				if (endAnchorOffset == -1) /* nested annotation with same last token might have tags showing */ {
					if ((lastGtmp.index + 1) == this.tokenPanels.size()) {
						if (dao != null)
							dao.displayAdjustmentFinished(false);
						if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> last token panel nut found (2)");
						return;
					}
					if ((docAnnot.getEndIndex() - 1) != lastGtmp.maxTokenIndex) {
						if (dao != null)
							dao.displayAdjustmentFinished(false);
						if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> last token panel nut found (3)");
						return;
					}
					lastGtmp = ((TokenMarkupPanel) this.tokenPanels.get(lastGtmp.index + 1));
					endAnchorOffset = lastGtmp.getEndAnchorOffsetOf(docAnnot);
					if (endAnchorOffset == -1) {
						if (dao != null)
							dao.displayAdjustmentFinished(false);
						if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> annotation end cap nut found");
						return;
					}
				}
				this.clearSelections(true);
				this.tokenSelectionStarted(firstGtmp, startAnchorOffset);
				this.tokenSelectionModified(lastGtmp, (endAnchorOffset + 1));
				this.tokenSelectionEnded(lastGtmp, (endAnchorOffset + 1), null);
				this.xdvp.moveIntoView(firstGtmp, firstGtmp.getStartAnchorPositionOf(annot), lastGtmp, lastGtmp.getEndAnchorPositionOf(annot), dao);
				if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> annotation tokens selected");
				return;
			}
			else {
				if (dao != null)
					dao.displayAdjustmentFinished(false);
				if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> unknown annotation display mode '" + displayMode + "'");
				return;
			}
		}
		else /* cannot select document proper or supplement or whatever */ {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			if (DEBUG_SET_SELECTED_OBJECT) System.out.println(" ==> unknown object type " + object.getClass().getName());
			return;
		}
	}
	private static final boolean DEBUG_SET_SELECTED_OBJECT = true;
	
	public void setSelectedTokens(final Token firstToken, final Token lastToken, final boolean showParents, final DisplayAdjustmentObserver dao) {
		if (DEBUG_SET_SELECTED_TOKENS) System.out.println("Setting selected tokens to range between " + firstToken + " and " + lastToken);
		
		//	we can handle single-token case above
		if (firstToken == lastToken) {
			this.setSelectedObject(firstToken, showParents, dao);
			return;
		}
		
		//	nothing to work with
		if (this.xdvp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		
		//	make sure to adjust selection after end of any atomic action
		if (this.isAtomicActionRunning()) {
			this.atomicActionViewPositionAdjuster = new Runnable() {
				public void run() {
					setSelectedTokens(firstToken, lastToken, showParents, dao);
				}
			};
			if (DEBUG_SET_SELECTED_TOKENS) System.out.println(" ==> enqueued for end of atomic action " + this.atomicActionId);
			return;
		}
		
		//	ensure we're on the event dispatcher
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setSelectedTokens(firstToken, lastToken, showParents, dao);
				}
			});
			if (DEBUG_SET_SELECTED_TOKENS) System.out.println(" ==> moved onto EDT");
			return;
		}
		
		//	ensure tokens not folded away
		TokenMarkupPanel firstGtmp = this.getTokenPanel(firstToken);
		if (firstGtmp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		TokenMarkupPanel lastGtmp = this.getTokenPanel(lastToken);
		if (lastGtmp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		if ((firstGtmp.isFoldedAway() || lastGtmp.isFoldedAway()) && !showParents) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		if (firstGtmp.isFoldedAway()) {
			this.unfoldAnnotationPanel(firstGtmp, true, new DisplayAdjustmentObserver() {
				public void displayAdjustmentFinished(boolean success) {
					if (success) {
						validateLayout();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								System.out.println("Asynchronous recursion after unfolding first paragraph parents");
								setSelectedTokens(firstToken, lastToken, showParents, dao);
							}
						});
					}
					else if (dao != null)
						dao.displayAdjustmentFinished(false);
				}
			});
			return;
		}
		if (lastGtmp.isFoldedAway()) {
			this.unfoldAnnotationPanel(lastGtmp, true, new DisplayAdjustmentObserver() {
				public void displayAdjustmentFinished(boolean success) {
					if (success) {
						validateLayout();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								System.out.println("Asynchronous recursion after unfolding last paragraph parents");
								setSelectedTokens(firstToken, lastToken, showParents, dao);
							}
						});
					}
					else if (dao != null)
						dao.displayAdjustmentFinished(false);
				}
			});
			return;
		}
		
		//	move token panels associated to visible area and inject selection
//		TokenMarkupPanel firstGtmp = firstXpmp.getPanelFor(firstToken);
//		if (firstGtmp == null) {
//			if (dao != null)
//				dao.displayAdjustmentFinished(false);
//			return;
//		}
		int firstTokenFirstOffset = firstGtmp.getFirstAnchorOffsetOf(firstToken);
		if (firstTokenFirstOffset == -1) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
//		TokenMarkupPanel lastGtmp = lastXpmp.getPanelFor(lastToken);
//		if (lastGtmp == null) {
//			if (dao != null)
//				dao.displayAdjustmentFinished(false);
//			return;
//		}
		int lastTokenLastOffset = lastGtmp.getLastAnchorOffsetOf(lastToken);
		if (lastTokenLastOffset == -1) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		this.clearSelections(true);
		this.tokenSelectionStarted(firstGtmp, firstTokenFirstOffset);
		this.tokenSelectionModified(lastGtmp, (lastTokenLastOffset + 1));
		this.tokenSelectionEnded(lastGtmp, (lastTokenLastOffset + 1), null);
		this.xdvp.moveIntoView(firstGtmp, firstGtmp.getPositionOf(firstToken), lastGtmp, lastGtmp.getPositionOf(lastToken), dao);
		return;
	}
	private static final boolean DEBUG_SET_SELECTED_TOKENS = true;
	
	public void setSelectedTokens(final int firstTokenIndex, final int lastTokenIndex, final boolean showParents, final DisplayAdjustmentObserver dao) {
		System.out.println("Setting selected tokens to range between " + firstTokenIndex + " and " + lastTokenIndex);
		
		//	nothing to work with
		if (this.xdvp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		
		//	make sure to adjust selection after end of any atomic action
		if (this.isAtomicActionRunning()) {
			this.atomicActionViewPositionAdjuster = new Runnable() {
				public void run() {
					setSelectedTokens(firstTokenIndex, lastTokenIndex, showParents, dao);
				}
			};
			System.out.println(" ==> enqueued for end of atomic action " + this.atomicActionId);
			return;
		}
		
		//	ensure we're on the event dispatcher
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setSelectedTokens(firstTokenIndex, lastTokenIndex, showParents, dao);
				}
			});
			System.out.println(" ==> moved onto EDT");
			return;
		}
		
		//	ensure tokens not folded away
		TokenMarkupPanel firstGtmp = ((TokenMarkupPanel) this.tokenPanelAtIndex.get(firstTokenIndex));
		if (firstGtmp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		TokenMarkupPanel lastGtmp = ((TokenMarkupPanel) this.tokenPanelAtIndex.get(lastTokenIndex));
		if (lastGtmp == null) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		if ((firstGtmp.isFoldedAway() || lastGtmp.isFoldedAway()) && !showParents) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		if (firstGtmp.isFoldedAway()) {
			this.unfoldAnnotationPanel(firstGtmp, true, new DisplayAdjustmentObserver() {
				public void displayAdjustmentFinished(boolean success) {
					if (success) {
						validateLayout();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								System.out.println("Asynchronous recursion after unfolding first paragraph parents");
								setSelectedTokens(firstTokenIndex, lastTokenIndex, showParents, dao);
							}
						});
					}
					else if (dao != null)
						dao.displayAdjustmentFinished(false);
				}
			});
			return;
		}
		if (lastGtmp.isFoldedAway()) {
			this.unfoldAnnotationPanel(lastGtmp, true, new DisplayAdjustmentObserver() {
				public void displayAdjustmentFinished(boolean success) {
					if (success) {
						validateLayout();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								System.out.println("Asynchronous recursion after unfolding last paragraph parents");
								setSelectedTokens(firstTokenIndex, lastTokenIndex, showParents, dao);
							}
						});
					}
					else if (dao != null)
						dao.displayAdjustmentFinished(false);
				}
			});
			return;
		}
		
		//	move token panels associated to visible area and inject selection
		int firstTokenFirstOffset = firstGtmp.getFirstAnchorOffsetOf(firstTokenIndex);
		if (firstTokenFirstOffset == -1) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		int lastTokenLastOffset = lastGtmp.getLastAnchorOffsetOf(lastTokenIndex);
		if (lastTokenLastOffset == -1) {
			if (dao != null)
				dao.displayAdjustmentFinished(false);
			return;
		}
		this.clearSelections(true);
		this.tokenSelectionStarted(firstGtmp, firstTokenFirstOffset);
		this.tokenSelectionModified(lastGtmp, (lastTokenLastOffset + 1 /* need to go _after_ last character */));
		this.tokenSelectionEnded(lastGtmp, (lastTokenLastOffset + 1 /* need to go _after_ last character */), null);
		this.xdvp.moveIntoView(firstGtmp, firstGtmp.getPositionOf(firstTokenIndex), lastGtmp, lastGtmp.getPositionOf(lastTokenIndex), dao);
		return;
	}
	
	void validateLayout() {
		this.getLayout().layoutContainer(this);
		this.validate();
		this.repaint();
	}
	
	/**
	 * Callback interface for complex display adjustments, asynchronously
	 * notifying client code after completion. This is for operations that
	 * potentially require multiple steps to be asynchronously chained via
	 * <code>SwingUtilities.invokeLater()</code>. The notification callback
	 * always comes from the Swing event dispatch thread.
	 * 
	 * @author sautter
	 */
	public static interface DisplayAdjustmentObserver {
		
		/**
		 * Receive notification that the observed display adjustment if
		 * finished, as well as whether it was completed successfully or
		 * failed.
		 * @param success was the display adjustment completed successfully?
		 */
		public abstract void displayAdjustmentFinished(boolean success);
	}
	static final DisplayAdjustmentObserver dummyDisplayAdjustmentObserver = new DisplayAdjustmentObserver() {
		public void displayAdjustmentFinished(boolean success) {}
	};
	
	/**
	 * Add a listener to receive notification about changes to document display
	 * properties like annotation colors or the fonts used for rendering the
	 * document. This is not to be confused with the property change listeners
	 * registered via the <code>addPropertyChangeListener()</code> method to
	 * listen for changes to properties of a specific graphics component.
	 * @param pcl the property change listener to add
	 */
	public void addDisplayPropertyChangeListener(PropertyChangeListener pcl) {
		if (pcl == null)
			return;
		if (this.displayPropertyListeners == null)
			this.displayPropertyListeners = new ArrayList();
		if (!this.displayPropertyListeners.contains(pcl))
			this.displayPropertyListeners.add(pcl);
	}
	
	/**
	 * Remove a listener for changes to document display properties like
	 * annotation colors or the fonts used for rendering the document. This is
	 * not to be confused with the property change listeners registered via the
	 * <code>addPropertyChangeListener()</code> method to listen for changes to
	 * properties of a specific graphics component.
	 * @param pcl the property change listener to remove
	 */
	public void removeDisplayPropertyListener(PropertyChangeListener pcl) {
		if (this.displayPropertyListeners == null)
			return;
		this.displayPropertyListeners.remove(pcl);
		if (this.displayPropertyListeners.isEmpty())
			this.displayPropertyListeners = null;
	}
	
	private ArrayList displayPropertyListeners = null;
	
	private void notifyDisplayPropertyChanged(String propName, Object oldValue, Object newValue) {
		if (this.displayPropertyListeners == null)
			return;
		PropertyChangeEvent pce = new PropertyChangeEvent(this, propName, oldValue, newValue);
		for (int l = 0; l < this.displayPropertyListeners.size(); l++) try {
			((PropertyChangeListener) this.displayPropertyListeners.get(l)).propertyChange(pce);
		}
		catch (RuntimeException re) {
			System.out.println("Error notifying about property '" + propName + "' changing from " + oldValue + " to " + newValue + ": " + re.getMessage());
			re.printStackTrace(System.out);
		}
	}
	
	/**
	 * Generically retrieve the default value of a display property by its
	 * name, be it a font or a color. There is no default values for the
	 * display mode or colors of any types of annotations. If the argument
	 * name does not correspond to an actual display property, this method
	 * returns null.
	 * @param name the name of the display property to retrieve
	 * @return the default value of the display propert with the argument name
	 */
	public static Object getDisplayPropertyDefault(String name) {
		if (name == null)
			return null;
		else if ("token.font".equals(name))
			return defaultTokenTextFont;
		else if ("token.foreground".equals(name))
			return defaultTokenTextColor;
		else if ("token.background".equals(name))
			return defaultTokenBackgroundColor;
		else if ("token.selectedForeground".equals(name))
			return defaultSelTokenTextColor;
		else if ("token.selectedBackground".equals(name))
			return defaultSelTokenBackgroundColor;
		else if ("tag.font".equals(name))
			return defaultTagTextFont;
		else if ("tag.foreground".equals(name))
			return defaultTagTextColor;
		else if ("tag.background".equals(name))
			return defaultTagBackgroundColor;
		else if ("tag.selectedForeground".equals(name))
			return defaultSelTagTextColor;
		else if ("tag.selectedBackground".equals(name))
			return defaultSelTagBackgroundColor;
		else if ("annot.highlightAlpha".equals(name))
			return new Integer(defaultAnnotHighlightAlpha);
		else if ("view.relativeStableHeight".equals(name))
			return new Integer(defaultViewStabilizationHeight);
		else if ("view.stableHeightLevel".equals(name))
			return new Integer(defaultViewStabilizationLevel);
		else return null;
	}
	
	/**
	 * Check whether or not the markup panel generally supports a generic
	 * display property with a given name, independent of whether or not that
	 * property specifically applies to a given instance.The highlight color
	 * or display mode of any given type of annotation, for instance, can only
	 * ever have any actual effect if the document showing in a given markup
	 * panel actually contains annotations of that type.
	 * @param name the name of the display property to retrieve
	 * @return the current value of the display propert with the argument name
	 */
	public static boolean supportsDisplayProperty(String name) {
		if (name == null)
			return false;
		else if ("token.font".equals(name))
			return true;
		else if ("token.foreground".equals(name))
			return true;
		else if ("token.background".equals(name))
			return true;
		else if ("token.selectedForeground".equals(name))
			return true;
		else if ("token.selectedBackground".equals(name))
			return true;
		else if ("tag.font".equals(name))
			return true;
		else if ("tag.foreground".equals(name))
			return true;
		else if ("tag.background".equals(name))
			return true;
		else if ("tag.selectedForeground".equals(name))
			return true;
		else if ("tag.selectedBackground".equals(name))
			return true;
		else if ("annot.highlightAlpha".equals(name))
			return true;
		else if ("view.relativeStableHeight".equals(name))
			return true;
		else if ("view.stableHeightLevel".equals(name))
			return true;
		else if (name.startsWith("annot.")) {
			name = name.substring("annot.".length());
			if (name.endsWith(".color"))
				return true;
			else if (name.endsWith(".mode"))
				return true;
			else return false;
		}
		else return false;
	}
	
	/**
	 * Generically retrieve a display property by its name, be it a font, or a
	 * color, or the current display mode of some type of annotation. In the
	 * latter case, however, the returned vylue for non-showing annotations
	 * will be null rather than the invisible mode object. If the argument name
	 * does not correspond to an actual display property, this method returns
	 * null.
	 * @param name the name of the display property to retrieve
	 * @return the current value of the display propert with the argument name
	 */
	public Object getDisplayProperty(String name) {
		if (name == null)
			return null;
		else if ("token.font".equals(name))
			return this.tokenTextFont;
		else if ("token.foreground".equals(name))
			return this.tokenTextColor;
		else if ("token.background".equals(name))
			return this.tokenBackgroundColor;
		else if ("token.selectedForeground".equals(name))
			return this.selTokenTextColor;
		else if ("token.selectedBackground".equals(name))
			return this.selTokenBackgroundColor;
		else if ("tag.font".equals(name))
			return this.tagTextFont;
		else if ("tag.foreground".equals(name))
			return this.tagTextColor;
		else if ("tag.background".equals(name))
			return this.tagBackgroundColor;
		else if ("tag.selectedForeground".equals(name))
			return this.selTagTextColor;
		else if ("tag.selectedBackground".equals(name))
			return this.selTagBackgroundColor;
		else if ("annot.highlightAlpha".equals(name))
			return new Integer(this.annotHighlightAlpha);
		else if ("view.relativeStableHeight".equals(name))
			return new Integer(this.viewStabilizationHeight);
		else if ("view.stableHeightLevel".equals(name))
			return new Integer(this.viewStabilizationLevel);
		else if (name.startsWith("annot.")) {
			String annotType = name.substring("annot.".length());
			if (annotType.endsWith(".mode")) {
				annotType = annotType.substring(0, (annotType.length() - ".mode".length()));
				Character displayMode = this.getDetailAnnotationDisplayMode(annotType);
				return ((displayMode == DISPLAY_MODE_INVISIBLE) ? null : displayMode);
			}
			else if (annotType.endsWith(".color")) {
				annotType = annotType.substring(0, (annotType.length() - ".color".length()));
				return this.getAnnotationColor(annotType);
			}
			else return null;
		}
		else return null;
	}
	
	/**
	 * Generically set a display property by its name. If the argument object
	 * is of the wrong type for the specified display property, this method
	 * throws an illegal argument exception. If the argument name does not
	 * correspond to an actual display property, this method does nothing.
	 * Setting a display property to null resets it to the built-in default, if
	 * any exists, and does nothing otherwise, except setting the display mode
	 * for a given annotation type to null does the same as setting it to the
	 * invisible mode object.
	 * @param name the name of the display property to set
	 * @param value the value to set the display property to
	 */
	public void setDisplayProperty(String name, Object value) throws IllegalArgumentException {
		if (name == null)
			return;
		else if ("token.font".equals(name)) {
			if (value == null)
				value = defaultTokenTextFont;
			if (value instanceof Font)
				this.setTokenTextFont((Font) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to font");
		}
		else if ("token.foreground".equals(name)) {
			if (value == null)
				value = defaultTokenTextColor;
			if (value instanceof Color)
				this.setTokenTextColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("token.background".equals(name)) {
			if (value == null)
				value = defaultTokenBackgroundColor;
			if (value instanceof Color)
				this.setTokenBackgroundColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("token.selectedForeground".equals(name)) {
			if (value == null)
				value = defaultSelTokenTextColor;
			if (value instanceof Color)
				this.setSelectedTokenTextColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("token.selectedBackground".equals(name)) {
			if (value == null)
				value = defaultSelTokenBackgroundColor;
			if (value instanceof Color)
				this.setSelectedTokenBackgroundColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("tag.font".equals(name)) {
			if (value == null)
				value = defaultTagTextFont;
			if (value instanceof Font)
				this.setTagTextFont((Font) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to font");
		}
		else if ("tag.foreground".equals(name)) {
			if (value == null)
				value = defaultTagTextColor;
			if (value instanceof Color)
				this.setTagTextColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("tag.background".equals(name)) {
			if (value == null)
				value = defaultTagBackgroundColor;
			if (value instanceof Color)
				this.setTagBackgroundColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("tag.selectedForeground".equals(name)) {
			if (value == null)
				value = defaultSelTagTextColor;
			if (value instanceof Color)
				this.setSelectedTagTextColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("tag.selectedBackground".equals(name)) {
			if (value == null)
				value = defaultSelTagBackgroundColor;
			if (value instanceof Color)
				this.setSelectedTagBackgroundColor((Color) value);
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
		}
		else if ("annot.highlightAlpha".equals(name)) {
			if (value == null)
				value = Integer.valueOf(defaultAnnotHighlightAlpha);
			if (value instanceof Number)
				this.setAnnotationHighlightAlpha(((Number) value).intValue());
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to int");
		}
		else if ("view.relativeStableHeight".equals(name)) {
			if (value == null)
				value = Integer.valueOf(defaultViewStabilizationHeight);
			if (value instanceof Number)
				this.setViewStabilizationHeight(((Number) value).intValue());
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to int");
		}
		else if ("view.stableHeightLevel".equals(name)) {
			if (value == null)
				value = Integer.valueOf(defaultViewStabilizationLevel);
			if (value instanceof Number)
				this.setViewStabilizationLevel(((Number) value).intValue());
			else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to int");
		}
		else if (name.startsWith("annot.")) {
			String annotType = name.substring("annot.".length());
			if (annotType.endsWith(".mode")) {
				if (value == null)
					value = DISPLAY_MODE_INVISIBLE;
				if (value instanceof Character) {
					annotType = annotType.substring(0, (annotType.length() - ".mode".length()));
					this.setDetailAnnotationDisplayMode(annotType, ((Character) value));
				}
				else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to chracter");
			}
			else if (annotType.endsWith(".color")) {
				if (value == null)
					return; // no defaults for annotation colors
				if (value instanceof Color) {
					annotType = annotType.substring(0, (annotType.length() - ".color".length()));
					this.setAnnotationColor(annotType, ((Color) value));
				}
				else throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to color");
			}
		}
	}
	
	/**
	 * Retrieve the font used for rendering token text.
	 * @return the token text font
	 */
	public Font getTokenTextFont() {
		return this.tokenTextFont;
	}
	
	/**
	 * Set the font to use for rendering token text.
	 * @param ttf the token text font to use
	 */
	public void setTokenTextFont(Font ttf) {
		this.setTokenTextFont(ttf, true);
	}
	void setTokenTextFont(Font ttf, boolean updateImmediately) {
		Font oldTtf = this.tokenTextFont;
		this.tokenTextFont = ttf;
		if (updateImmediately)
			this.updateTokenPanelTextSettings(true);
		this.notifyDisplayPropertyChanged("token.font", oldTtf, this.tokenTextFont);
	}
	
	/**
	 * Retrieve the color used for rendering non-selected token text.
	 * @return the token text color
	 */
	public Color getTokenTextColor() {
		return this.tokenTextColor;
	}
	
	/**
	 * Set the color to use for rendering non-selected token text.
	 * @param ttc the token text color to use
	 */
	public void setTokenTextColor(Color ttc) {
		this.setTokenTextColor(ttc, true);
	}
	void setTokenTextColor(Color ttc, boolean updateImmediately) {
		Color oldTtc = this.tokenTextColor;
		this.tokenTextColor = ttc;
		if (updateImmediately)
			this.updateTokenPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("token.foreground", oldTtc, this.tokenTextColor);
	}
	
	/**
	 * Retrieve the color of the background non-selected token text is rendered
	 * on.
	 * @return the token background color
	 */
	public Color getTokenBackgroundColor() {
		return this.tokenBackgroundColor;
	}
	
	/**
	 * Set the color of the background to render non-selected token text on.
	 * @param tbc the token background color to use
	 */
	public void setTokenBackgroundColor(Color tbc) {
		this.setTokenBackgroundColor(tbc, true);
	}
	void setTokenBackgroundColor(Color tbc, boolean updateImmediately) {
		Color oldTbc = this.tokenBackgroundColor;
		this.tokenBackgroundColor = tbc;
		if (updateImmediately)
			this.updateTokenPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("token.background", oldTbc, this.tokenBackgroundColor);
	}
	
	/**
	 * Retrieve the color used for rendering selected token text.
	 * @return the color for selected token text
	 */
	public Color getSelectedTokenTextColor() {
		return this.selTokenTextColor;
	}
	
	/**
	 * Set the color to use for rendering selected token text.
	 * @param sttc the token text color to use for selections
	 */
	public void setSelectedTokenTextColor(Color sttc) {
		this.setSelectedTokenTextColor(sttc, true);
	}
	void setSelectedTokenTextColor(Color sttc, boolean updateImmediately) {
		Color oldSttc = this.selTokenTextColor;
		this.selTokenTextColor = sttc;
		if (updateImmediately)
			this.updateTokenPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("token.selectedForeground", oldSttc, this.selTokenTextColor);
	}
	
	/**
	 * Retrieve the color used for highlighting selected token text.
	 * @return the highlight color for selected token text
	 */
	public Color getSelectedTokenBackgroundColor() {
		return this.selTokenBackgroundColor;
	}
	
	/**
	 * Set the color to use for highlighting selected token text.
	 * @param stbc the highlight color for selected token text
	 */
	public void setSelectedTokenBackgroundColor(Color stbc) {
		this.setSelectedTokenBackgroundColor(stbc, true);
	}
	void setSelectedTokenBackgroundColor(Color stbc, boolean updateImmediately) {
		Color oldStbc = this.selTokenBackgroundColor;
		this.selTokenBackgroundColor = stbc;
		if (updateImmediately)
			this.updateTokenPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("token.selectedBackground", oldStbc, this.selTokenBackgroundColor);
	}
	
	private void updateTokenPanelTextSettings(boolean fontChanged) {
		if (fontChanged && (this.xdvp != null))
			this.xdvp.recordStableViewPanel(true);
		for (int p = 0; p < this.tokenPanels.size(); p++)
			((TokenMarkupPanel) this.tokenPanels.get(p)).updateTextSettings();
		this.setBackground(this.tokenBackgroundColor);
		this.outlineBorder.setColor(this.tokenBackgroundColor);
		for (Iterator aidit = this.annotIDsToPanels.keySet().iterator(); aidit.hasNext();) {
			String annotId = ((String) aidit.next());
			((AnnotMarkupPanel) this.annotIDsToPanels.get(annotId)).updateTagPanelTextSettings();
		}
		this.getLayout().layoutContainer(this);
		this.validate();
		this.repaint();
		if (fontChanged && (this.xdvp != null))
			this.xdvp.restoreStableViewPanel();
	}
	
	/**
	 * Retrieve the font used for rendering annotation tag text.
	 * @return the tag text font
	 */
	public Font getTagTextFont() {
		return this.tagTextFont;
	}
	
	/**
	 * Set the font to use for rendering annotation tag text.
	 * @param ttf the tag text font to use
	 */
	public void setTagTextFont(Font ttf) {
		this.setTagTextFont(ttf, true);
	}
	void setTagTextFont(Font ttf, boolean updateImmediately) {
		Font oldTtf = this.tagTextFont;
		this.tagTextFont = ttf;
		if (updateImmediately)
			this.updateAnnotTagPanelTextSettings(true);
		this.notifyDisplayPropertyChanged("tag.font", oldTtf, this.tagTextFont);
	}
	
	/**
	 * Retrieve the color used for rendering non-selected annotation tag text.
	 * @return the tag text color
	 */
	public Color getTagTextColor() {
		return this.tagTextColor;
	}
	
	/**
	 * Set the color to use for rendering non-selected annotation tag text.
	 * @param ttc the tag text color to use
	 */
	public void setTagTextColor(Color ttc) {
		this.setTagTextColor(ttc, true);
	}
	void setTagTextColor(Color ttc, boolean updateImmediately) {
		Color oldTtc = this.tagTextColor;
		this.tagTextColor = ttc;
		if (updateImmediately)
			this.updateAnnotTagPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("tag.foreground", oldTtc, this.tagTextColor);
	}
	
	/**
	 * Retrieve the color used for highlighting non-selected annotation tag
	 * text.
	 * @return the highlight color for non-selected annotation tag text
	 */
	public Color getTagBackgroundColor() {
		return this.tagBackgroundColor;
	}
	
	/**
	 * Set the color to use for highlighting selected annotation tag text.
	 * @param tbc the highlight color for selected annotation tag text
	 */
	public void setTagBackgroundColor(Color tbc) {
		this.setTagBackgroundColor(tbc, true);
	}
	void setTagBackgroundColor(Color tbc, boolean updateImmediately) {
		Color oldTbc = this.tagBackgroundColor;
		this.tagBackgroundColor = tbc;
		if (updateImmediately)
			this.updateAnnotTagPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("tag.background", oldTbc, this.tagBackgroundColor);
	}
	
	/**
	 * Retrieve the color used for rendering selected annotation tag text.
	 * @return the annotation tag text color used for selections
	 */
	public Color getSelectedTagTextColor() {
		return this.selTagTextColor;
	}
	
	/**
	 * Set the color to use for rendering selected annotation tag text.
	 * @param sttc the annotation tag text color to use for selections
	 */
	public void setSelectedTagTextColor(Color sttc) {
		this.setSelectedTagTextColor(sttc, true);
	}
	void setSelectedTagTextColor(Color sttc, boolean updateImmediately) {
		Color oldSttc = this.selTagTextColor;
		this.selTagTextColor = sttc;
		if (updateImmediately)
			this.updateAnnotTagPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("tag.selectedForeground", oldSttc, this.selTagTextColor);
	}
	
	/**
	 * Retrieve the color used for highlighting selected annotation tag text.
	 * @return the highlight color used for selected annotation tag text
	 */
	public Color getSelectedTagBackgroundColor() {
		return this.selTagBackgroundColor;
	}
	
	/**
	 * Set the color to use for highlighting selected annotation tag text.
	 * @param stbc the highlight color for selected annotation tag text
	 */
	public void setSelectedTagBackgroundColor(Color stbc) {
		this.setSelectedTagBackgroundColor(stbc, true);
	}
	void setSelectedTagBackgroundColor(Color stbc, boolean updateImmediately) {
		Color oldStbc = this.selTagBackgroundColor;
		this.selTagBackgroundColor = stbc;
		if (updateImmediately)
			this.updateAnnotTagPanelTextSettings(false);
		this.notifyDisplayPropertyChanged("tag.selectedBackground", oldStbc, this.selTagBackgroundColor);
	}
	
	private void updateAnnotTagPanelTextSettings(boolean fontChanged) {
		if (fontChanged && (this.xdvp != null))
			this.xdvp.recordStableViewPanel(true);
		for (Iterator aidit = this.annotIDsToPanels.keySet().iterator(); aidit.hasNext();) {
			String annotId = ((String) aidit.next());
			/*if (annot instanceof Paragraph)
				((ParaMarkupPanel) this.annotsToPanels.get(annot)).updateAnnotTagPanelTextSettings();
			else */((AnnotMarkupPanel) this.annotIDsToPanels.get(annotId)).updateTagPanelTextSettings();
		}
//		for (int p = 0; p < this.tokenPanels.size(); p++)
//			((TokenMarkupPanel) this.tokenPanels.get(p)).updateTagPanelTextSettings();
		this.getLayout().layoutContainer(this);
		this.validate();
		this.repaint();
		if (fontChanged && (this.xdvp != null))
			this.xdvp.restoreStableViewPanel();
		if (this.gdvc != null) /* uses same base color for color change buttons as tag background base color */ {
			this.gdvc.validate();
			this.gdvc.repaint();
		}
	}
	
	/**
	 * Retrieve the alpha used for drawing annotation value highlights and tag
	 * backgrounds. This alpha value is also used for the central portion of
	 * the color selector buttons representing annotation types in any
	 * associated view control panel.
	 * @return the annotation highlight alpha
	 */
	public int getAnnotationHighlightAlpha() {
		return this.annotHighlightAlpha;
	}
	
	/**
	 * Set the alpha to use for drawing annotation value highlights and tag
	 * backgrounds. This alpha value is also used for the central portion of
	 * the color selector buttons representing annotation types in any
	 * associated view control panel. The argument value must be between 0 and
	 * 255, inclusive.
	 * @param aha the annotation highlight alpha to set
	 */
	public void setAnnotationHighlightAlpha(int aha) {
		if (aha == this.annotHighlightAlpha)
			return;
		if (aha < 0x00)
			throw new IllegalArgumentException("Annotation highlight alpha cannot be less than 0");
		if (0xFF < aha)
			throw new IllegalArgumentException("Annotation highlight alpha cannot be more than 255");
		int oldAha = this.annotHighlightAlpha;
		this.annotHighlightAlpha = aha;
		this.notifyDisplayPropertyChanged("annot.highlightAlpha", new Integer(oldAha), new Integer(aha));
		for (Iterator atit = this.annotColors.keySet().iterator(); atit.hasNext();) {
			String type = ((String) atit.next());
			Color ac = ((Color) this.annotColors.get(type));
			this.annotHighlightColors.put(type, new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), this.annotHighlightAlpha));
			if (this.gdvc != null)
				this.gdvc.setAnnotColor(type, ac);
		}
		for (Iterator aidit = this.annotIDsToPanels.keySet().iterator(); aidit.hasNext();) {
			String annotId = ((String) aidit.next());
			AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annotId));
			gamp.updateAnnotColor();
		}
		this.validate();
		this.repaint();
		if (this.gdvc != null) {
			this.gdvc.validate();
			this.gdvc.repaint();
		}
	}
	
	/**
	 * Retrieve the color used for painting annotations of a specific type.
	 * @param type the type of annotation to retrieve the color for
	 * @return the color to use for painting the annotations of the argument
	 *            type
	 */
	public Color getAnnotationColor(String type) {
		return this.getAnnotationColor(type, false);
	}
	Color getAnnotationColor(String type, boolean create) {
		Color ac = ((Color) this.annotColors.get(type));
		if ((ac == null) && create) {
			ac = this.createAnnotationColor(type);
			if (ac == null)
				ac = new Color(Color.HSBtoRGB(((float) Math.random()), 0.7f, 1.0f));
			this.annotColors.put(type, ac);
//			this.annotHighlightColors.put(type, new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 0x40));
			this.annotHighlightColors.put(type, new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), this.annotHighlightAlpha));
			this.notifyDisplayPropertyChanged(("annot." + type + ".color"), null, ac);
		}
		return ac;
	}
	Color getAnnotationHighlightColor(String type, boolean create) {
		Color ahc = ((Color) this.annotHighlightColors.get(type));
		if ((ahc == null) && create)
			this.getAnnotationColor(type, create);
		return ((Color) this.annotHighlightColors.get(type));
	}
	
	/**
	 * Create a color for visualizing annotations of a given type that has not
	 * been assigned a color yet. If this method returns null, a color will be
	 * created internally, using HSB with a random hue, 70% saturation, and
	 * full brightness. This default implementation does return null and thus
	 * delegate to the internal approach, subclasses are welcome to overwrite
	 * it and provide their own logic.
	 * @param type the annotation type the color is intended for
	 * @return the color for visualizing the annotations of the argument type
	 */
	protected Color createAnnotationColor(String type) {
		return null;
	}
	
	/**
	 * Set the color to use for painting annotations of a specific type.
	 * @param type the type of annotations to set the color for
	 * @param colors the color to use for painting the annotations of the
	 *            argument type
	 */
	public void setAnnotationColor(String type, Color color) {
		Color oldColor = ((Color) this.annotColors.get(type));
		this.annotColors.put(type, color);
//		this.annotHighlightColors.put(type, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0x40));
		this.annotHighlightColors.put(type, new Color(color.getRed(), color.getGreen(), color.getBlue(), this.annotHighlightAlpha));
		boolean annotsVisible = (/*(this.getStructuralAnnotationDisplayMode(type) != DISPLAY_MODE_INVISIBLE) || */(this.getDetailAnnotationDisplayMode(type) != DISPLAY_MODE_INVISIBLE));
		if (this.isVisible() && annotsVisible) {
//			if (Annotation.PARAGRAPH_TYPE.equals(type)) {
//				for (int p = 0; p < this.paraPanels.size(); p++)
//					((ParaMarkupPanel) this.paraPanels.get(p)).updateAnnotColor();
//			}
//			//	TODO type specific index lists should speed this up considerable ...
//			else if (this.document.getAnnotationCount(type, false) != 0)
				for (Iterator aidit = this.annotIDsToPanels.keySet().iterator(); aidit.hasNext();) {
					String annotId = ((String) aidit.next());
					AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotIDsToPanels.get(annotId));
					if (type.equals(gamp.annot.getType()))
						gamp.updateAnnotColor();
				}
//			if (this.document.getStructuralAnnotationPercentage(type) < 100) {
//				for (int p = 0; p < this.paraPanels.size(); p++)
//					((ParaMarkupPanel) this.paraPanels.get(p)).updateAnnotColor(type);
//			}
			this.validate();
			this.repaint();
		}
		if (this.gdvc != null)
			this.gdvc.setAnnotColor(type, color);
		this.notifyDisplayPropertyChanged(("annot." + type + ".color"), oldColor, color);
	}
	
	public boolean areAnnotationsVisible(String type) {
//		boolean annotsVisible = false;
//		if ((this.document.getStructuralAnnotationPercentage(type) != 0) && (this.getStructuralAnnotationDisplayMode(type) != DISPLAY_MODE_INVISIBLE))
//			annotsVisible = true;
//		if ((this.document.getStructuralAnnotationPercentage(type) != 100) && (this.getDetailAnnotationDisplayMode(type) != DISPLAY_MODE_INVISIBLE))
//			annotsVisible = true;
		return (this.getDetailAnnotationDisplayMode(type) != DISPLAY_MODE_INVISIBLE);
	}
	
	public void ensureAnnotationsVisible(String type) {
//		if ((this.document.getStructuralAnnotationPercentage(type) != 0) && (this.getStructuralAnnotationDisplayMode(type) == DISPLAY_MODE_INVISIBLE))
//			this.setStructuralAnnotationDisplayMode(type, DISPLAY_MODE_SHOW_TAGS);
		if (/*(this.document.getStructuralAnnotationPercentage(type) != 100) && */(this.getDetailAnnotationDisplayMode(type) == DISPLAY_MODE_INVISIBLE)) {
			Character prefDadm = this.getPreferredDetailAnnotationShowingMode(type);
			this.setDetailAnnotationDisplayMode(type, ((prefDadm == null) ? DISPLAY_MODE_SHOW_HIGHLIGHTS : prefDadm));
		}
	}
//	
//	public Character getStructuralAnnotationDisplayMode(String type) {
//		Character mode = ((Character) this.structAnnotDisplayModes.get(type));
//		return ((mode == null) ? DISPLAY_MODE_INVISIBLE : mode);
//	}
//	
//	public void setStructuralAnnotationDisplayMode(String type, Character mode) {
//		this.setStructuralAnnotationDisplayMode(type, mode, true, (this.atomicActionId == 0));
//	}
//	void setStructuralAnnotationDisplayMode(String type, Character mode, boolean updateDisplayControl, boolean showChangesImmediately) {
//		System.out.println("Setting structural annotatation display mode for '" + type + "' to '" + mode + "'");
//		Character xMode = ((Character) this.structAnnotDisplayModes.get(type));
//		System.out.println(" - existing mode is '" + xMode + "'");
//		if (DISPLAY_MODE_SHOW_TAGS.equals(mode)) {
//			this.structAnnotDisplayModes.put(type, DISPLAY_MODE_SHOW_TAGS);
//			mode = DISPLAY_MODE_SHOW_TAGS;
//		}
//		else if (DISPLAY_MODE_FOLD_CONTENT.equals(mode)) {
//			this.structAnnotDisplayModes.put(type, DISPLAY_MODE_FOLD_CONTENT);
//			mode = DISPLAY_MODE_FOLD_CONTENT;
//		}
//		else {
//			this.structAnnotDisplayModes.put(type, DISPLAY_MODE_INVISIBLE);
//			mode = DISPLAY_MODE_INVISIBLE;
//		}
//		if ((mode == DISPLAY_MODE_FOLD_CONTENT) || (xMode == DISPLAY_MODE_FOLD_CONTENT))
//			showChangesImmediately = true; // folding doesn't happen on mass updates, and should be rare on API calls
//		if ((mode != xMode) && this.isVisible()) {
//			if (showChangesImmediately) {
//				System.out.println(" - updating document display");
//				this.recordStableViewContentPanels();
//				this.layoutContent(false, type); // we're only changing the structure, no need to modify paragraph contents
//				this.validate();
//				this.repaint();
//				this.restoreStableViewContentPanels();
//			}
//			else this.structAnnotModCount++;
//		}
//		if (this.gdvc == null) {}
//		else if (updateDisplayControl) {
//			System.out.println(" - updating display control");
//			this.gdvc.setStructAnnotDisplayMode(type, mode);
//		}
//		else this.gdvcDocModCount++;
//	}
//	void checkStructuralAnnotationDisplayMode(Annotation annot, boolean annotFolded) {
//		if (this.gdvc == null)
//			return;
//		int unfoldedCount = 0;
//		int foldedCount = 0;
//		if (annot instanceof Paragraph) {
//			for (int p = 0; p < this.paraPanels.size(); p++) {
//				ParaMarkupPanel xpmp = ((ParaMarkupPanel) this.paraPanels.get(p));
//				if (xpmp.contentFolded)
//					foldedCount++;
//				else unfoldedCount++;
//				if (annotFolded ? (unfoldedCount != 0) : (foldedCount != 0))
//					return; // we have or will have a mix, nothing to update
//			}
//		}
//		else {
//			Annotation[] annots = this.document.getAnnotations(annot.getType(), false);
//			for (int a = 0; a < annots.length; a++) {
//				AnnotMarkupPanel gamp = ((AnnotMarkupPanel) this.annotsToPanels.get(annots[a]));
//				if (gamp == null)
//					continue;
//				if (gamp.contentFolded)
//					foldedCount++;
//				else unfoldedCount++;
//				if (annotFolded ? (unfoldedCount != 0) : (foldedCount != 0))
//					return; // we have or will have a mix, nothing to update
//			}
//		}
//		Character displayMode = ((Character) this.structAnnotDisplayModes.get(annot.getType()));
//		if ((unfoldedCount == 0) && (displayMode != DISPLAY_MODE_FOLD_CONTENT)) {
//			this.structAnnotDisplayModes.put(annot.getType(), DISPLAY_MODE_FOLD_CONTENT);
//			this.gdvc.setStructAnnotDisplayMode(annot.getType(), DISPLAY_MODE_FOLD_CONTENT);
//		}
//		else if ((foldedCount == 0) && (displayMode != DISPLAY_MODE_SHOW_TAGS)) {
//			this.structAnnotDisplayModes.put(annot.getType(), DISPLAY_MODE_SHOW_TAGS);
//			this.gdvc.setStructAnnotDisplayMode(annot.getType(), DISPLAY_MODE_SHOW_TAGS);
//		}
//	}
	
	public Character getDetailAnnotationDisplayMode(String type) {
		Character mode = ((Character) this.detailAnnotDisplayModes.get(type));
		return ((mode == null) ? DISPLAY_MODE_INVISIBLE : mode);
	}
	
	public void setDetailAnnotationDisplayMode(String type, Character mode) {
		this.setDetailAnnotationDisplayMode(type, mode, true, (this.atomicActionId == 0));
	}
	void setDetailAnnotationDisplayMode(String type, Character mode, boolean updateDisplayControl, boolean showChangesImmediately) {
		Character oldMode = ((Character) this.detailAnnotDisplayModes.get(type));
		if (DISPLAY_MODE_SHOW_TAGS.equals(mode)) {
			this.detailAnnotDisplayModes.put(type, DISPLAY_MODE_SHOW_TAGS);
			mode = DISPLAY_MODE_SHOW_TAGS;
		}
		else if (DISPLAY_MODE_SHOW_HIGHLIGHTS.equals(mode)) {
			this.detailAnnotDisplayModes.put(type, DISPLAY_MODE_SHOW_HIGHLIGHTS);
			mode = DISPLAY_MODE_SHOW_HIGHLIGHTS;
		}
		else {
			this.detailAnnotDisplayModes.put(type, DISPLAY_MODE_INVISIBLE);
			mode = DISPLAY_MODE_INVISIBLE;
		}
//		if ((mode == DISPLAY_MODE_SHOW_TAGS) || (xMode == DISPLAY_MODE_SHOW_TAGS))
//			showChangesImmediately = true; // detail tag showing should rarely happen on mass updates, and should be rare on API calls
		if ((mode != oldMode) && this.isVisible()) {
			if (showChangesImmediately) {
				this.recordStableViewContentPanels(true, true);
				this.layoutContentDisplayControl(type, oldMode, mode);
				this.validate();
				this.repaint();
				this.restoreStableViewContentPanels();
			}
			else this.detailAnnotModCount++;
		}
		if (this.gdvc == null) {}
		else if (updateDisplayControl)
			this.gdvc.setDetailAnnotDisplayMode(type, mode);
		else this.gdvcDocModCount++;
		this.notifyDisplayPropertyChanged(("annot." + type + ".mode"), oldMode, mode);
	}
	
	//	TODO overwrite this to show tags for the likes of materials citations and maybe also treatment citations and treatment citation groups
	protected Character getPreferredDetailAnnotationShowingMode(String type) {
		//	TODO rename to 'showDetailAnnotationAsStructure()' and return boolean instead ???
		//	TODO ==> depends upon whether or not we might want to add further modes in future ...
		return null;
	}
	
	/**
	 * @return the stableViewHeight
	 */
	public int getViewStabilizationHeight() {
		return this.viewStabilizationHeight;
	}
	
	/**
	 * @param stableViewHeight the stableViewHeight to set
	 */
	public void setViewStabilizationHeight(int vsh) {
		if ((vsh < 0) || (100 < vsh))
			throw new IllegalArgumentException("View stabilization height has to be from 0-100 (inclusive)");
		Integer oldVsh = new Integer(this.viewStabilizationHeight);
		this.viewStabilizationHeight = vsh;
		this.notifyDisplayPropertyChanged("view.relativeStableHeight", oldVsh, new Integer(this.viewStabilizationHeight));
	}
	
	/**
	 * @return the stableViewLevel
	 */
	public int getViewStabilizationLevel() {
		return this.viewStabilizationLevel;
	}
	
	/**
	 * @param stableViewLevel the stableViewLevel to set
	 */
	public void setViewStabilizationLevel(int vsl) {
		if ((vsl < 0) || (10 < vsl))
			throw new IllegalArgumentException("View stabilization level has to be from 0-10 (inclusive)");
		Integer oldVsl = new Integer(this.viewStabilizationLevel);
		this.viewStabilizationLevel = vsl;
		this.notifyDisplayPropertyChanged("view.stableHeightLevel", oldVsl, new Integer(this.viewStabilizationLevel));
	}
	
	private static final int defaultViewStabilizationHeight = 33;
	private static final int defaultViewStabilizationLevel = 3;
	int viewStabilizationHeight = defaultViewStabilizationHeight;
	int viewStabilizationLevel = defaultViewStabilizationLevel;
	
//	private ParaMarkupPanel stableViewParaPanel = null;
//	private Rectangle stableViewParaPanelPosition = null;
	private TokenMarkupPanel stableViewTokenPanel = null;
	private Rectangle stableViewTokenPanelPosition = null;
	private TokenCaretPosition stableViewTokenSelectionStart = null;
	private TokenCaretPosition stableViewTokenSelectionEnd = null;
	private TagCaretPosition stableViewTagSelectionStart = null;
	private TagCaretPosition stableViewTagSelectionEnd = null;
	
	private void recordStableViewContentPanels(boolean recordCaretPosition, boolean recordSelection) {
		if (this.xdvp == null)
			return;
		this.stableViewTokenPanel = null;
		this.stableViewTokenPanelPosition = null;
		
		//	record any extant caret selection or position
		this.stableViewTokenSelectionStart = null;
		this.stableViewTokenSelectionEnd = null;
		this.stableViewTagSelectionStart = null;
		this.stableViewTagSelectionEnd = null;
		if (recordSelection) {
			if ((this.tokenSelectionStart != null) && (this.tokenSelectionEnd != null)) {
				this.stableViewTokenSelectionStart = this.getTokenCaretPosition(this.tokenSelectionStart, this.tokenSelectionStartOffset);
				this.stableViewTokenSelectionEnd = this.getTokenCaretPosition(this.tokenSelectionEnd, this.tokenSelectionEndOffset);
			}
			else if ((this.tagSelectionStart != null) && (this.tagSelectionEnd != null)) {
				this.stableViewTagSelectionStart = this.getTagCaretPosition(this.tagSelectionStart, this.tagSelectionStartOffset);
				this.stableViewTagSelectionEnd = this.getTagCaretPosition(this.tagSelectionEnd, this.tagSelectionEndOffset);
			}
		}
		else if (recordCaretPosition) {
			if ((this.tokenSelectionStart != null) && (this.tokenSelectionEnd != null))
				this.stableViewTokenSelectionStart = this.getTokenCaretPosition(this.tokenSelectionEnd, this.tokenSelectionEndOffset);
			else if ((this.tagSelectionStart != null) && (this.tagSelectionEnd != null))
				this.stableViewTagSelectionStart = this.getTagCaretPosition(this.tagSelectionEnd, this.tagSelectionEndOffset);
		}
		
		//	binary search (some) paragraph panel in view
		Rectangle vPos = this.xdvp.getViewRect();
		int low = 0;
		int high = (this.tokenPanels.size() - 1);
		int startVppIndex = -1;
		while (low < high) {
			int mid = ((low + high) / 2);
			TokenMarkupPanel xpmp = ((TokenMarkupPanel) this.tokenPanels.get(mid));
			//	TODO need to make sure token panel not inside folded annotation panel
			if (xpmp.isFoldedAway()) {
				for (int d = 1; d < Math.max((high - mid), (mid - low)); d++) {
					if (low <= (mid - d)) {
						TokenMarkupPanel lXpmp = ((TokenMarkupPanel) this.tokenPanels.get(mid - d));
						if (!lXpmp.isFoldedAway()) {
							mid = (mid - d);
							xpmp = lXpmp;
							break;
						}
					}
					if ((mid + d) <= high) {
						TokenMarkupPanel hXpmp = ((TokenMarkupPanel) this.tokenPanels.get(mid + d));
						if (!hXpmp.isFoldedAway()) {
							mid = (mid + d);
							xpmp = hXpmp;
							break;
						}
					}
				}
			}
			if (xpmp.isFoldedAway())
				return; // TODO we might have hit some visible panel earlier ...
			//	TODOne we might actually want to check for CONTENT PANEL of paragraph, as we're mainly after token panels
			//	==> we're collecting all paragraph panels and then selecting token panel closest to stable height from their content
			Rectangle pPos = this.xdvp.getViewPositionOf(xpmp);
			if (pPos == null)
				return; // TODO likely better simply ignore paragraph and move on
			if ((pPos.y + pPos.height) < 0)
				low = (mid + 1);
			else if (vPos.height < pPos.y)
				high = (mid - 1);
			else {
				startVppIndex = mid;
				break;
			}
		}
		if (startVppIndex == -1)
			return; // no dice, must be lots folded away
		
		//	determine whole search range (might well have small paragraphs)
		ArrayList viewTokenPanels = new ArrayList();
		viewTokenPanels.add(this.tokenPanels.get(startVppIndex));
		for (int p = startVppIndex; p != 0; p--) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(p-1));
			if (gtmp.isFoldedAway())
				continue;
			Rectangle pPos = this.xdvp.getViewPositionOf(gtmp);
			if (pPos == null)
				continue;
			if ((pPos.y + pPos.height) < 0)
				break; // we're too far up
			viewTokenPanels.add(0, gtmp);
		}
		for (int p = (startVppIndex + 1); p < this.tokenPanels.size(); p++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(p));
			if (gtmp.isFoldedAway())
				continue;
			Rectangle pPos = this.xdvp.getViewPositionOf(gtmp);
			if (pPos == null)
				continue;
			if (vPos.height < pPos.y)
				break; // we're too far down
			viewTokenPanels.add(gtmp);
		}
		
		//	compute height to keep stable, and find panel with vertical middle closest to it
		int stableHeight = ((vPos.height * this.viewStabilizationHeight) / 100);
		int minMidHeightStableHeightDist = vPos.height;
		
		//	find fully visible token panel with center closest to stable height
		for (int p = 0; p < viewTokenPanels.size(); p++) {
			TokenMarkupPanel xpmp = ((TokenMarkupPanel) viewTokenPanels.get(p));
			Rectangle pPos = this.xdvp.getViewPositionOf(xpmp);
			if (pPos == null)
				continue;
			if ((pPos.y + pPos.height) < 0)
				continue; // this one's too far up
			if (vPos.height < pPos.y)
				break; // we're too far down, nothing more to come
			if (pPos.y < 0)
				continue;
			if (vPos.height < (pPos.y + pPos.height))
				continue;
			int midHeight = (pPos.y + (pPos.height / 2));
			int midHeightStableHeightDist = Math.abs(stableHeight - midHeight);
			if (midHeightStableHeightDist < minMidHeightStableHeightDist) {
				minMidHeightStableHeightDist = midHeightStableHeightDist;
				this.stableViewTokenPanel = xpmp;
				this.stableViewTokenPanelPosition = pPos;
			}
		}
		if (this.stableViewTokenPanel != null) {
			if (DEBUG_SCROLL_STABILIZATION) {
				System.out.println("Found fully visible stable token panel " + /*this.stableViewParaPanel.index + "/" + */this.stableViewTokenPanel.index + ": " + this.stableViewTokenPanel);
				System.out.println("  middle distance to stable height " + stableHeight + " is " + minMidHeightStableHeightDist);
				System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
			}
			return; // found what we came for
		}
		
		//	find visible token panel with largest visible portion
		for (int p = 0; p < viewTokenPanels.size(); p++) {
			TokenMarkupPanel xpmp = ((TokenMarkupPanel) viewTokenPanels.get(p));
			Rectangle pPos = this.xdvp.getViewPositionOf(xpmp);
			if (pPos == null)
				continue;
			if ((pPos.y + pPos.height) < 0)
				continue; // this one's too far up
			if (vPos.height < pPos.y)
				break; // we're too far down, nothing more to come
			int midHeight = (pPos.y + (pPos.height / 2));
			int midHeightStableHeightDist = Math.abs(stableHeight - midHeight);
			if (midHeightStableHeightDist < minMidHeightStableHeightDist) {
				minMidHeightStableHeightDist = midHeightStableHeightDist;
				this.stableViewTokenPanel = xpmp;
				this.stableViewTokenPanelPosition = pPos;
			}
		}
		if (this.stableViewTokenPanel != null) {
			if (DEBUG_SCROLL_STABILIZATION) {
				System.out.println("Found partially visible stable token panel " + /*this.stableViewParaPanel.index + "/" + */this.stableViewTokenPanel.index + ": " + this.stableViewTokenPanel);
				System.out.println("  middle distance to stable height " + stableHeight + " is " + minMidHeightStableHeightDist);
				System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
			}
			return; // found what we came for
		}
		
		//	find topmost fully visible paragraph panel
		for (int p = 0; p < viewTokenPanels.size(); p++) {
			TokenMarkupPanel xpmp = ((TokenMarkupPanel) viewTokenPanels.get(p));
			Rectangle pPos = this.xdvp.getViewPositionOf(xpmp);
			if (pPos == null)
				continue;
			if ((pPos.y + pPos.height) < 0)
				continue; // this one's too far up
			if (vPos.height < pPos.y)
				break; // we're too far down, nothing more to come
			if (pPos.y < 0)
				continue;
			if (vPos.height < (pPos.y + pPos.height))
				continue;
			int midHeight = (pPos.y + (pPos.height / 2));
			int midHeightStableHeightDist = Math.abs(stableHeight - midHeight);
			if (midHeightStableHeightDist < minMidHeightStableHeightDist) {
				minMidHeightStableHeightDist = midHeightStableHeightDist;
				this.stableViewTokenPanel = xpmp;
				this.stableViewTokenPanelPosition = pPos;
			}
		}
		if (this.stableViewTokenPanel != null) {
			if (DEBUG_SCROLL_STABILIZATION) {
				System.out.println("Found fully visible stable paragraph panel " + this.stableViewTokenPanel.index + ": " + this.stableViewTokenPanel);
				System.out.println("  middle distance to stable height " + stableHeight + " is " + minMidHeightStableHeightDist);
				System.out.println("  at position " + this.stableViewTokenPanelPosition);
			}
			return; // found at least part of what we came for
		}
		
		//	find visible paragraph panel with largest visible portion
		for (int p = 0; p < viewTokenPanels.size(); p++) {
			TokenMarkupPanel xpmp = ((TokenMarkupPanel) viewTokenPanels.get(p));
			Rectangle pPos = this.xdvp.getViewPositionOf(xpmp);
			if (pPos == null)
				continue;
			if ((pPos.y + pPos.height) < 0)
				continue; // this one's too far up
			if (vPos.height < pPos.y)
				break; // we're too far down, nothing more to come
			int midHeight = (pPos.y + (pPos.height / 2));
			int midHeightStableHeightDist = Math.abs(stableHeight - midHeight);
			if (midHeightStableHeightDist < minMidHeightStableHeightDist) {
				minMidHeightStableHeightDist = midHeightStableHeightDist;
				this.stableViewTokenPanel = xpmp;
				this.stableViewTokenPanelPosition = pPos;
			}
		}
		if (this.stableViewTokenPanel != null) {
			if (DEBUG_SCROLL_STABILIZATION) {
				System.out.println("Found partially visible stable paragraph panel " + this.stableViewTokenPanel.index + ": " + this.stableViewTokenPanel);
				System.out.println("  middle distance to stable height " + stableHeight + " is " + minMidHeightStableHeightDist);
				System.out.println("  at position " + this.stableViewTokenPanelPosition);
			}
			return; // found at least part of what we came for
		}
	}
	
	
	private void restoreStableViewContentPanels() {
		if (this.xdvp == null)
			return;
		
		//	nothing to work with, e.g. due to excessive folding or injected selection
		if (this.stableViewTokenPanel == null) {
			this.stableViewTokenSelectionStart = null;
			this.stableViewTokenSelectionEnd = null;
			this.stableViewTagSelectionStart = null;
			this.stableViewTagSelectionEnd = null;
			this.xdvp.validate();
			this.xdvp.repaint();
			return;
		}
		
		//	prepare restoring any selection
		DisplayAdjustmentObserver selectionRestorer;
		if (this.stableViewTokenSelectionStart != null) {
			final TokenCaretPosition svtss = this.stableViewTokenSelectionStart;
			final TokenCaretPosition svtse = this.stableViewTokenSelectionEnd;
			this.stableViewTokenSelectionStart = null;
			this.stableViewTokenSelectionEnd = null;
			selectionRestorer = new DisplayAdjustmentObserver() {
				public void displayAdjustmentFinished(boolean success) {
					restoreTokenCaretPosition(svtss, false);
					if (svtse != null)
						restoreTokenCaretPosition(svtse, true);
				}
			};
		}
		else if (this.stableViewTagSelectionStart != null) {
			final TagCaretPosition svtss = this.stableViewTagSelectionStart;
			final TagCaretPosition svtse = this.stableViewTagSelectionEnd;
			this.stableViewTagSelectionStart = null;
			this.stableViewTagSelectionEnd = null;
			selectionRestorer = new DisplayAdjustmentObserver() {
				public void displayAdjustmentFinished(boolean success) {
					restoreTagCaretPosition(svtss, false);
					if (svtse != null)
						restoreTagCaretPosition(svtse, true);
				}
			};
		}
		else selectionRestorer = dummyDisplayAdjustmentObserver;
		
		//	restore stable view content
		this.restoreStableViewContentPanels(selectionRestorer);
	}
	private void restoreStableViewContentPanels(DisplayAdjustmentObserver dao) {
		
		//	move to extant recorded token panel, e.g. after structural update
		boolean tvtpExtant = false;
		for (Container pComp = this.stableViewTokenPanel.getParent(); pComp != null; pComp = pComp.getParent()) {
			if (pComp == this) {
				tvtpExtant = true;
				break;
			}
		}
		if (tvtpExtant) {
			if (DEBUG_SCROLL_STABILIZATION) {
				System.out.println("Restoring to extant stable token panel " + this.stableViewTokenPanel.index);
				System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
			}
			this.xdvp.moveToViewPosition(this.stableViewTokenPanel, this.stableViewTokenPanelPosition, dao);
			this.stableViewTokenPanel = null;
			this.stableViewTokenPanelPosition = null;
			return;
		}
		
		//	move to exact replacement of recorded token panel, e.g. after overzealous paragraph content refresh
		//	TODO likely use binary search, as we have to search whole document
		for (int t = 0; t < this.tokenPanels.size(); t++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(t));
			if ((this.stableViewTokenPanel.minTokenIndex == gtmp.minTokenIndex) && (gtmp.maxTokenIndex == this.stableViewTokenPanel.maxTokenIndex)) {
				if (DEBUG_SCROLL_STABILIZATION) {
					System.out.println("Restoring to replacement stable token panel " + gtmp.index);
					System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
				}
				this.xdvp.moveToViewPosition(gtmp, this.stableViewTokenPanelPosition, dao);
				this.stableViewTokenPanel = null;
				this.stableViewTokenPanelPosition = null;
				return;
			}
		}
		
		//	move to first fraction of recorded token panel, e.g. after activating detail annotation tags
		//	TODO likely use binary search, as we have to search whole document
		for (int t = 0; t < this.tokenPanels.size(); t++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(t));
			if ((this.stableViewTokenPanel.minTokenIndex <= gtmp.minTokenIndex) && (gtmp.maxTokenIndex <= this.stableViewTokenPanel.maxTokenIndex)) {
				if (DEBUG_SCROLL_STABILIZATION) {
					System.out.println("Restoring to start of stable token panel " + gtmp.index);
					System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
				}
				this.xdvp.moveToViewPosition(gtmp, this.stableViewTokenPanelPosition, dao);
				this.stableViewTokenPanel = null;
				this.stableViewTokenPanelPosition = null;
				return;
			}
		}
		
		//	move to token panel now containing recorded token panel content, e.g. after deactivating detail annotation tags
		//	TODO likely use binary search, as we have to search whole document
		for (int t = 0; t < this.tokenPanels.size(); t++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(t));
			if ((gtmp.minTokenIndex <= this.stableViewTokenPanel.minTokenIndex) && (this.stableViewTokenPanel.maxTokenIndex <= gtmp.maxTokenIndex)) {
				if (DEBUG_SCROLL_STABILIZATION) {
					System.out.println("Restoring to including stable token panel " + gtmp.index);
					System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
				}
				this.xdvp.moveToViewPosition(gtmp, this.stableViewTokenPanelPosition, dao);
				this.stableViewTokenPanel = null;
				this.stableViewTokenPanelPosition = null;
				return;
			}
		}
		
		//	move to token panel maximally overlapping content of recorded token panel, whatever might have happened
		//	TODO likely use binary search, as we have to search whole document
		int maxOverlapTokens = -1;
		TokenMarkupPanel maxOverlapGtmp = null;
		for (int t = 0; t < this.tokenPanels.size(); t++) {
			TokenMarkupPanel gtmp = ((TokenMarkupPanel) this.tokenPanels.get(t));
			if (gtmp.maxTokenIndex < this.stableViewTokenPanel.minTokenIndex)
				continue;
			if (this.stableViewTokenPanel.maxTokenIndex < gtmp.minTokenIndex)
				break;
			int overlapTokens = (Math.min(this.stableViewTokenPanel.maxTokenIndex, gtmp.maxTokenIndex) - Math.max(this.stableViewTokenPanel.minTokenIndex, gtmp.minTokenIndex));
			if (maxOverlapTokens < overlapTokens) {
				maxOverlapTokens = overlapTokens;
				maxOverlapGtmp = gtmp;
			}
		}
		if (maxOverlapGtmp == null) {
//			this.stableViewTokenPanel = null;
//			this.stableViewTokenPanelPosition = null;
//			this.restoreStableViewContentPanels(); // try again with paragraph panel only
		}
		else {
			if (DEBUG_SCROLL_STABILIZATION) {
				System.out.println("Restoring to overlapping stable token panel " + maxOverlapGtmp.index);
				System.out.println("  tokens at position " + this.stableViewTokenPanelPosition);
			}
			this.xdvp.moveToViewPosition(maxOverlapGtmp, this.stableViewTokenPanelPosition, dao);
			this.stableViewTokenPanel = null;
			this.stableViewTokenPanelPosition = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}
//	
//	/* (non-Javadoc)
//	 * @see javax.swing.JComponent#getPreferredSize()
//	 */
//	public Dimension getPreferredSize() {
//		return new Dimension(1000, 1000);
//	}
	
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		//	TODO return fraction (maybe 10%) of visible area width or height
		return 10;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		//	TODO return fraction (maybe 33%) of visible area width or height
		return 50;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
	/**
	 * Edit the attributes of some XML Markup object belonging to the document
	 * being edited in this panel. This method takes care of making the
	 * attribute changes an atomic operation.
	 * @param attributed the object whose attributes to edit
	 * @param type the type of object whose attribute to edit
	 * @param value the textual value of the object whose attribute to edit
	 */
	public void editAttributes(Attributed attributed, String type, String value) {
		this.editAttributes(attributed, type, value, null);
	}
	
	/**
	 * Edit the attributes of some XML Markup object belonging to the document
	 * being edited in this panel. This method takes care of making the
	 * attribute changes an atomic operation.
	 * @param attributed the object whose attributes to edit
	 * @param type the type of object whose attribute to edit
	 * @param value the textual value of the object whose attribute to edit
	 * @param attribName the attribute name to pre-select for editing
	 */
	public void editAttributes(Attributed attributed, String type, String value, String attribName) {
		Attributed[] context;
		int[] contextIndex = {-1};
		if (this.isAtomicActionRunning())
			context = null; // no lateral navigation if part of some larger atomic action started outside
		else if (attributed instanceof Token) {
			context = new Attributed[this.document.size()];
			Token token = ((Token) attributed);
			for (int t = 0; t < this.document.size(); t++) {
				Token cToken = this.document.tokenAt(t);
				context[t] = cToken;
				if (cToken.getStartOffset() == token.getStartOffset())
					contextIndex[0] = t;
			}
		}
		else if (attributed instanceof Annotation) {
			context = this.document.getAnnotations(((Annotation) attributed).getType());
			String annotId = ((Annotation) attributed).getAnnotationID();
			for (int a = 0; a < context.length; a++)
				if (annotId.equals(((Annotation) context[a]).getAnnotationID())) {
					contextIndex[0] = a;
					break;
				}
		}
		else context = null;
		
		while (attributed != null) {
			if (attributed instanceof Token) {
				type = Token.TOKEN_ANNOTATION_TYPE;//((Token) attributed).getType();
				value = (((Token) attributed).getValue()/* + " at " + ((Token) attributed).getIndex() + " in paragraph " + (((Token) attributed).getParagraph().getIndex())*/);
			}
			else if (attributed instanceof Annotation) {
				type = ((Annotation) attributed).getType();
//				value = TokenSequenceUtils.concatTokens(((Annotation) attributed), true, true, 40);
				value = this.getAttributeEditorAnnotationValue((Annotation) attributed);
			}
			attributed = this.editAttributes(attributed, context, contextIndex, type, value, attribName);
			attribName = null; // only pre-select attribute on original argument object
		}
	}
	
	/**
	 * Create the string to represent an annotation in an attribute editing
	 * dialog. This default implementation restricts the string to roughly 40
	 * character at most, taken from the start and the end of the annotation.
	 * Subclasses are welcome to overwrite it with a different behavior.
	 * @param annot the annotation to create the display string for
	 * @return the display string for the argument annotation
	 */
	protected String getAttributeEditorAnnotationValue(Annotation annot) {
		return TokenSequenceUtils.concatTokens(annot, true, true, 40);
	}
	
	private Attributed editAttributes(Attributed attributed, final Attributed[] context, final int[] contextIndex, final String type, String value, String attribName) {
		final AttributeEditor aePanel = new AttributeEditor(attributed, type, value, context);
		final JDialog aeDialog = DialogFactory.produceDialog("Edit Attributes", true);
		final boolean handleAtomicAction = !this.isAtomicActionRunning();
		
		JButton commit = new JButton("OK");
		commit.setBorder(BorderFactory.createRaisedBevelBorder());
		commit.setPreferredSize(new Dimension(80, 21));
		commit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (handleAtomicAction)
					beginAtomicAction("Edit " + type + " Attributes");
				getMutableDocument(); // need to get this to get listeners in place
				aePanel.writeChanges();
				attributeEditorDialogSize = aeDialog.getSize();
				attributeEditorDialogLocation = aeDialog.getLocation(attributeEditorDialogLocation);
				aeDialog.dispose();
				if (handleAtomicAction)
					endAtomicAction();
				contextIndex[0] = -1;
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.setBorder(BorderFactory.createRaisedBevelBorder());
		cancel.setPreferredSize(new Dimension(80, 21));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				attributeEditorDialogSize = aeDialog.getSize();
				attributeEditorDialogLocation = aeDialog.getLocation(attributeEditorDialogLocation);
				aeDialog.dispose();
				contextIndex[0] = -1;
			}
		});
		
		JButton previous = null;
		JButton next = null;
		if ((context != null) && (contextIndex[0] != -1)) {
			if (contextIndex[0] != 0) {
				previous = new JButton("Previous");
				previous.setBorder(BorderFactory.createRaisedBevelBorder());
				previous.setPreferredSize(new Dimension(80, 21));
				previous.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						if (handleAtomicAction)
							beginAtomicAction("Edit " + type + " Attributes");
						getMutableDocument(); // need to get this to get listeners in place
						aePanel.writeChanges();
						attributeEditorDialogSize = aeDialog.getSize();
						attributeEditorDialogLocation = aeDialog.getLocation(attributeEditorDialogLocation);
						aeDialog.dispose();
						if (handleAtomicAction)
							endAtomicAction();
						contextIndex[0]--;
					}
				});
			}
			if ((contextIndex[0] + 1) < context.length) {
				next = new JButton("Next");
				next.setBorder(BorderFactory.createRaisedBevelBorder());
				next.setPreferredSize(new Dimension(80, 21));
				next.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						if (handleAtomicAction)
							beginAtomicAction("Edit " + type + " Attributes");
						getMutableDocument(); // need to get this to get listeners in place
						aePanel.writeChanges();
						attributeEditorDialogSize = aeDialog.getSize();
						attributeEditorDialogLocation = aeDialog.getLocation(attributeEditorDialogLocation);
						aeDialog.dispose();
						if (handleAtomicAction)
							endAtomicAction();
						contextIndex[0]++;
					}
				});
			}
		}
		
		JPanel aeButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		if (previous != null)
			aeButtons.add(previous);
		aeButtons.add(commit);
		aeButtons.add(cancel);
		if (next != null)
			aeButtons.add(next);
		
		if (attribName != null)
			aePanel.setSelectedAttributeName(attribName);
		
		aeDialog.getContentPane().setLayout(new BorderLayout());
		aeDialog.getContentPane().add(aePanel, BorderLayout.CENTER);
		aeDialog.getContentPane().add(aeButtons, BorderLayout.SOUTH);
		
		aeDialog.setResizable(true);
		aeDialog.setSize(attributeEditorDialogSize);
		if (attributeEditorDialogLocation == null)
			aeDialog.setLocationRelativeTo(DialogFactory.getTopWindow());
		else aeDialog.setLocation(attributeEditorDialogLocation);
		aeDialog.setVisible(true);
		
		return (((context == null) || (contextIndex[0] == -1)) ? null : context[contextIndex[0]]);
	}
	private static Dimension attributeEditorDialogSize = new Dimension(400, 300);
	private static Point attributeEditorDialogLocation = null;
	
	private long atomicActionId = 0;
	private MarkupTool atomicActiont = null;
	private Runnable atomicActionViewPositionAdjuster = null;
	private final LinkedHashSet atomicActionListeners = new LinkedHashSet();
	private boolean forceRefreshAfterAtomicActions = false;
	
	/**
	 * Specify whether or not to force a full refresh at the end of each atomic
	 * action. Setting the flag to true may refult in increased rendering
	 * effort. Subclassed that overwrite the <code>getMutableDocument()</code>
	 * method to return anything else than the actual document displaying in
	 * this panel can use this flag to ensure the document rednering and any
	 * display control panel retrieved from the <code>getControlPanel()</code>
	 * method update properly afzer atomic action.
	 * @param forceRefresh enforce a UI refresh after each atomic action?
	 */
	public void setForceRefreshAfterAtomicActions(boolean forceRefresh) {
		this.forceRefreshAfterAtomicActions = forceRefresh;
	}
	
	/**
	 * Start an atomic action, consisting of one or more edits to the GAMTA
	 * document being edited in the panel. This default implementation loops
	 * through to <code>startAtomicAction()</code>; sub classes that overwrite
	 * it either have to make the super call, or call the latter method
	 * directly.
	 * @param label the label of the action
	 */
	public void beginAtomicAction(String label) {
		this.startAtomicAction(label, null, null, null);
	}
	
	/**
	 * Start an atomic action, consisting of one or more edits to the GATA
	 * document being edited in the panel.
	 * @param label the label of the action
	 * @param mt the Markup Tool performing the action (if any)
	 * @param annot the annotation being processed (if any)
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public final void startAtomicAction(String label, MarkupTool mt, Annotation annot, ProgressMonitor pm) {
		this.startAtomicAction(System.currentTimeMillis(), label, mt, annot, pm);
	}
	
	/**
	 * Start an atomic action, consisting of one or more edits to the GANTA
	 * document being edited in the panel.
	 * @param id the unique ID of the action (must be non-zero)
	 * @param label the label of the action
	 * @param mt the Markup Tool performing the action (if any)
	 * @param annot the annotation being processed (if any)
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public final void startAtomicAction(long id, String label, MarkupTool mt, Annotation annot, ProgressMonitor pm) {
		System.out.println("Starting atomic action '" + label + "'");
		this.atomicActionId = id;
		this.atomicActiont = mt;
		this.immediatelyUpdateXdvc = false;
		for (Iterator aalit = this.atomicActionListeners.iterator(); aalit.hasNext();)
			((AtomicActionListener) aalit.next()).atomicActionStarted(id, label, mt, annot, pm);
	}
	/*
TODO XM document markup panel refresh tracking (especially for selection actions, but also XMTs):
- if only detail annotations added or modified, we might only have to refresh a few paragraphs ...
- ... especially if no added annotations want their tags shown
- if only attributes modified, we might only have to refresh a few tag panels
==> collect what requires refreshing in respective flags in XM document change tracker ...
==> ... and observe that when refreshing at end of atomic action
  ==> most likely make change tracking integral part of atomic action handling ...
  ==> ... and create dedicated XM document listener implementation to record relevant change events
	 */
	
	/**
	 * End an atomic action, consisting of one or more edits to the GAMTA
	 * document being edited in the panel. This default implementation loops
	 * through to <code>finishAtomicAction()</code>; sub classes that overwrite
	 * it either have to make the super call, or call the latter method
	 * directly.
	 */
	public void endAtomicAction() {
		this.finishAtomicAction(null);
	}
	
	/**
	 * Finish an atomic action, consisting of one or more edits to the GAMTA
	 * document being edited in the panel.
	 */
	public final void finishAtomicAction(ProgressMonitor pm) {
		System.out.println("Finishing atomic action");
		long id = this.atomicActionId;
		for (Iterator aalit = this.atomicActionListeners.iterator(); aalit.hasNext();)
			((AtomicActionListener) aalit.next()).atomicActionFinishing(id, pm);
		
		this.atomicActionId = 0;
		this.atomicActiont = null;
		this.immediatelyUpdateXdvc = true;
		for (Iterator aalit = this.atomicActionListeners.iterator(); aalit.hasNext();)
			((AtomicActionListener) aalit.next()).atomicActionFinished(id, pm);
		System.out.println("Atomic action finished");
		
		if (this.forceRefreshAfterAtomicActions) /* ensure actual update if so configured */ {
			this.detailAnnotModCount++;
			this.detailAnnotTagModCount++;
			if (this.tokensEditable)
				this.tokenModCount++;
			this.gdvcDocModCount++;
			System.out.println("Display marked as invalid");
		}
		
		/* we need to do repainting on Swing EDT, as otherwise we
		 * might incur a deadlock between this thread and EDT on
		 * synchronized parts of UI or data structures */
		final boolean retainCaretPosition = (this.atomicActionViewPositionAdjuster == null);
		if (SwingUtilities.isEventDispatchThread()) {
			this.validateDocumentPanel(retainCaretPosition, false);
			this.validateControlPanel();
			this.validate();
			this.repaint();
			System.out.println("Direct display refresh done");
		}
		else SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				validateDocumentPanel(retainCaretPosition, false);
				validateControlPanel();
				validate();
				repaint();
				System.out.println("Indirect display refresh done");
			}
		});
		
		//	remove mutable document change tracker
		if (DEBUG_MUTABLE_DOCUMENT) System.out.println("GAMTA Markup Panel: clearing mutable document at end of atomic action " + id);
		if ((this.mutableDocument != null) && (this.mutableDocumentChangeTracker != null)) {
			this.mutableDocument.removeAnnotationListener(this.mutableDocumentChangeTracker);
			if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> annotation listener removed");
			this.mutableDocument.removeTokenSequenceListener(this.mutableDocumentChangeTracker);
			this.mutableDocument.removeCharSequenceListener(this.mutableDocumentChangeTracker);
			if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> token sequence listener removed");
			this.mutableDocument = null;
			if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> mutable document cleared");
		}
		else if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> no listeners to remove");
		
		//	move to injected view position last thing, after refresh, etc.
		if (this.atomicActionViewPositionAdjuster != null)
			SwingUtilities.invokeLater(this.atomicActionViewPositionAdjuster);
		this.atomicActionViewPositionAdjuster = null;
	}
	
	/**
	 * Register an atomic action listener to receive notifications of atomic
	 * actions happening on the markup panel.
	 * @param aal the atomic action listener to register
	 */
	public void addAtomicActionListener(AtomicActionListener aal) {
		if (aal != null)
			this.atomicActionListeners.add(aal);
	}
	
	/**
	 * Remove an atomic action listener to refrain from receiving notifications
	 * of atomic actions happening on the markup panel.
	 * @param aal the atomic action listener to un-register
	 */
	public void removeAtomicActionListener(AtomicActionListener aal) {
		this.atomicActionListeners.remove(aal);
	}
	
	/**
	 * Listener observing atomic actions happening on an Image Markup document
	 * displayed in a markup panel.
	 * 
	 * @author sautter
	 */
	public static interface AtomicActionListener {
		
		/**
		 * Receive notification that an atomic action is starting. Both the
		 * Image Markup Tool and the target annotation can be null, and will be
		 * for Selection Actions. In fact, the target annotation only ever is
		 * not null when an Image Markup Tool is run on an annotation.
		 * @param id the unique ID of the started action
		 * @param label the label of the action
		 * @param mt the Markup Tool performing the action
		 * @param annot the annotation being processed
		 * @param pm the progress monitor observing on the action (if any)
		 */
		public abstract void atomicActionStarted(long id, String label, MarkupTool mt, Annotation annot, ProgressMonitor pm);
		
		/**
		 * Receive notification that the running atomic action is finishing.
		 * This method is intended for implementors to trigger any follow-on
		 * activities, still within the running atomic action, e.g. updating
		 * derived data stored on the document proper.
		 * @param id the unique ID of the finishing action
		 * @param pm the progress monitor observing on the action (if any)
		 */
		public abstract void atomicActionFinishing(long id, ProgressMonitor pm);
		
		/**
		 * Receive notification that the running atomic action is finished. Any
		 * activities triggered by client code on this notification does not
		 * fall under the running atomic action any more.
		 * @param id the unique ID of the finished action
		 * @param pm the progress monitor observing on the action (if any)
		 */
		public abstract void atomicActionFinished(long id, ProgressMonitor pm);
	}
	
	/**
	 * Test whether or not an atomic action is running on the markup panel.
	 * This method returns true right from a call to either one of
	 * <code>beginAtomicAction()</code> and <code>startAtomicAction()</code>
	 * up to a call to either one of <code>endAtomicAction()</code> and
	 * <code>finishAtomicAction()</code>.
	 * @return true if an atomic action is running, false otherwise
	 */
	public boolean isAtomicActionRunning() {
		return (this.atomicActionId != 0);
	}
	
	/**
	 * Get the ID of the atomic action currently running on the markup panel.
	 * This method returns a non-zero value right from a call to either one of
	 * <code>beginAtomicAction()</code> and <code>startAtomicAction()</code>
	 * up to a call to either one of <code>endAtomicAction()</code> and
	 * <code>finishAtomicAction()</code>.
	 * @return the ID of the atomic action currently running
	 */
	public long getAtomicActionId() {
		return this.atomicActionId;
	}
	
	/**
	 * Test whether or not a running atomic action involves an Image Markup
	 * Tool. This method returns true right from a call to 
	 * <code>beginAtomicAction()</code> with a non-null <code>imt</code>
	 * argument up to a call to either one of <code>endAtomicAction()</code>
	 * and <code>finishAtomicAction()</code>.
	 * @return true if an atomic action is running and involves an Image Markup
	 *            Tool
	 */
	public boolean isMarkupToolRunning() {
		return (this.atomicActiont != null);
	}
	
	/**
	 * Produce a progress monitor for observing some activity on the  document
	 * showing in this editor panel. Implementations that do not support
	 * pausing/resuming and aborting can return a plain progress monitor,
	 * ignoring the two boolean arguments; ones willing to support
	 * pausing/resuming and aborting have to return a controlling progress
	 * monitor. If the returned object is a dialog, it has to be positioned and
	 * sized as desired, but need not be opened by implementations, as the code
	 * calling this method takes care of opening and closing. This default
	 * implementation returns a basic progress monitor that simply writes all
	 * output to the system console.
	 * @param title the title for the progress monitor
	 * @param text the explanation text for the progress monitor
	 * @param supportPauseResume support pausing the monitored process?
	 * @param supportAbort support aborting the monitored process?
	 * @return a progress monitor
	 */
	public ProgressMonitor getProgressMonitor(String title, String text, boolean supportPauseResume, boolean supportAbort) {
		return ProgressMonitor.dummy;
	}
	
	/**
	 * A tool to apply to an Image Markup document displayed in an instance of
	 * this class, to perform changes as a visitor.
	 * 
	 * @author sautter
	 */
	public static interface MarkupTool {
		
		/**
		 * Get a nice name for the tool, to use in a user interface.
		 * @return the label of the tool
		 */
		public abstract String getLabel();
		
		/**
		 * Get an explanation text for what the tool does, to use in a user
		 * interface.
		 * @return an explanation text for the tool
		 */
		public abstract String getTooltip();
		
		/**
		 * Get a help text explaining in detail what the tool does, to use in a
		 * user interface.
		 * @return a help text for the tool
		 */
		public abstract String getHelpText();
		
		/**
		 * Process an Image Markup document or an annotation on that document. The
		 * argument document is never null, but the argument annotation can be. If
		 * the annotation is null, the whole document is to be processed; otherwise,
		 * the annotation is to be processed, with the document providing context
		 * information. The argument markup panel provides access to the surrounding
		 * user interface, if any.
		 * @param doc the document to process
		 * @param annot the annotation to process
		 * @param gdmp the document markup panel displaying the document, if any
		 * @param pm a progress monitor observing processing progress
		 */
		public abstract void process(MutableAnnotation doc, Annotation annot, GamtaDocumentMarkupPanel gdmp, ProgressMonitor pm);
		
		//	TODO add isApplicableTo(DocumentMarkupPanel) method to indicate whether or not the XMT can work on a given document
		//	TODO use this in GGX UI to gray out items in Edit and Tools menus if document of wrong type selected (e.g. font editing on scanned document, or OCR adjustment on born-digital one)
	}
	
	/**
	 * Selection object describing a mouse click or keyboard input on some part
	 * of the document display, specifying where the click or cursor position
	 * was located, as well as what is showing at that location.
	 * 
	 * @author sautter
	 */
	public static class PointSelection {
		//	TODO most likely include reference to subject panel for direct update (no need to refresh whole text area if we have exact offset !!!)
		public static final int POINT_TYPE_ANNOTATION_START_TAG = 0x01;
		public static final int POINT_TYPE_ANNOTATION_END_TAG = 0x02;
		public static final int POINT_TYPE_ANNOTATION_TAG_CONNECTOR = 0x03;
		
		public static final int POINT_TYPE_ANNOTATION_START_CAP = 0x08;
		public static final int POINT_TYPE_ANNOTATION_END_CAP = 0x09;
		
		public static final int POINT_TYPE_SPACE = 0x10;
		public static final int POINT_TYPE_TOKEN = 0x11;
		
		public final int type;
		
		private Annotation annot;
		private int annotTagOffset;
//		private Paragraph para;
//		private int paraOffset;
		private Token token;
		private int tokenIndex;
		private int tokenOffset;
		
		PointSelection(int type, Annotation annot, int annotTagOffset) {
			this(type, annot, annotTagOffset, null, -1, -1);
		}
		PointSelection(int type, Token token, int tokenIndex, int tokenOffset) {
			this(type, null, -1, token, tokenIndex, tokenOffset);
		}
		private PointSelection(int type, Annotation annot, int annotTagOffset, Token token, int tokenIndex, int tokenOffset) {
			this.type = type;
			this.annot = annot;
			this.annotTagOffset = annotTagOffset;
//			this.para = para;
//			this.paraOffset = paraOffset;
			this.token = token;
			this.tokenOffset = tokenOffset;
		}
		
		public Annotation getAnnotation() {
			return this.annot;
		}
		public String getAttributeName() {
			if (this.type != POINT_TYPE_ANNOTATION_START_TAG)
				return null;
			this.ensureStartTagClickDetails();
			return this.attributeName;
		}
		public boolean isAnnotationTypePoint() {
			if (this.type != POINT_TYPE_ANNOTATION_START_TAG)
				return false;
			this.ensureStartTagClickDetails();
			return (this.tagClickMode == 'T');
		}
		//	TODO add getter for offset within annotation type to faculitate renaming via typing (will beep on illegal characters, though)
		public boolean isAttributeNamePoint() {
			if (this.type != POINT_TYPE_ANNOTATION_START_TAG)
				return false;
			this.ensureStartTagClickDetails();
			return (this.tagClickMode == 'N');
		}
		//	TODO add getter for offset within attribute name to faculitate renaming via typing (will beep on illegal characters, though)
		public boolean isAttributeValuePoint() {
			if (this.type != POINT_TYPE_ANNOTATION_START_TAG)
				return false;
			this.ensureStartTagClickDetails();
			return (this.tagClickMode == 'V');
		}
		//	TODO add getter for offset within attribute (unescaped !!!) value to faculitate modification via typing ...
		//	TODO ... likely auto-escaping input, and deleting escaped entities as whole
		private void ensureStartTagClickDetails() {
			if (this.tagClickMode != 'U')
				return;
			
			//	assess selected portion of start tag
			StartTagIndex sti = createStartTagIndex(this.annot);
			if (sti.index.length() <= this.annotTagOffset)
				return;
			char ic = sti.index.charAt(this.annotTagOffset);
			if (ic == 'T')
				this.tagClickMode = 'T';
			else if (ic == 'N') {
				this.tagClickMode = 'N';
				int anso = this.annotTagOffset;
				while ((anso != 0) && (sti.index.charAt(anso - 1) == 'N'))
					anso--;
				int aneo = this.annotTagOffset;
				while ((aneo < sti.tag.length()) && (sti.index.charAt(aneo) == 'N'))
					aneo++;
				this.attributeName = sti.tag.substring(anso, aneo);
			}
			else if (ic == 'V') {
				this.tagClickMode = 'V';
				int aneo = this.annotTagOffset;
				while ((aneo != 0) && (sti.index.charAt(aneo - 1) != 'N'))
					aneo--;
				int anso = aneo;
				while ((anso != 0) && (sti.index.charAt(anso - 1) == 'N'))
					anso--;
				this.attributeName = sti.tag.substring(anso, aneo);
			}
			else this.tagClickMode = 'I';
		}
		private String attributeName = null;
		private char tagClickMode = 'U'; // initialize to 'unknown'
		
//		public Paragraph getParagraph() {
//			return this.para;
//		}
//		public int getParagraphOffset() {
//			return this.paraOffset;
//		}
		public Token getToken() {
			return this.token;
		}
		public int getTokenIndex() {
			return this.tokenIndex;
		}
		public int getTokenOffset() {
			return this.tokenOffset;
		}
	}
	
	/**
	 * Selection object describing a selection in the document text, including
	 * tokens and/or annotation highlights.
	 * 
	 * @author sautter
	 */
	public class TokenSelection {
		private TokenMarkupPanel firstGtmp;
		private int firstGtmpOffset;
		private TokenMarkupPanel lastGtmp;
		private int lastGtmpOffset;
		private boolean reverseSelection;
		TokenSelection(TokenMarkupPanel gtmp, int firstOffset, int lastOffset, boolean reverseSelection) {
			this(gtmp, firstOffset, gtmp, lastOffset, reverseSelection);
		}
		TokenSelection(TokenMarkupPanel firstGtmp, int firstGtmpOffset, TokenMarkupPanel lastGtmp, int lastGtmpOffset, boolean reverseSelection) {
			this.firstGtmp = firstGtmp;
			this.firstGtmpOffset = firstGtmpOffset;
			this.lastGtmp = lastGtmp;
			this.lastGtmpOffset = lastGtmpOffset;
			this.reverseSelection = reverseSelection;
		}
		
		public boolean isReverseSelection() {
			return this.reverseSelection;
		}
		private void ensureBounradies() {
			if (this.startObjectTrays == null) {
				LinkedHashSet startObjectTrays = new LinkedHashSet();
				TokenMarkupPanel gtmp = this.firstGtmp;
				int offset = this.firstGtmpOffset;
				while (offset < gtmp.getAnchorCount()) {
					TokenMarkupPanelObjectTray objectTray = gtmp.getObjectTrayAt(offset);
					if ((objectTray.index != TokenMarkupPanelObjectTray.WHITESPACE_INDEX) || (startObjectTrays.size() != 0))
						startObjectTrays.add(objectTray);
					if (-1 < objectTray.index) /* found some token */ {
						this.firstTokenIndex = objectTray.index;
						this.firstTokenGtmp = gtmp;
						break; // found first token, all we need
					}
					offset++;
					if ((gtmp == this.lastGtmp) && (this.lastGtmpOffset <= offset))
						break; // reached end of selection
					if (offset < gtmp.getAnchorCount())
						continue; // more to come in this panel
					if ((gtmp.index + 1) == tokenPanels.size())
						break; // no more token panels to look at
					gtmp = ((TokenMarkupPanel) tokenPanels.get(gtmp.index + 1)); // switch to subsequent token panel
					while (gtmp.isFoldedAway()) /* skip over any floded token panels (no risk of skipping over selection end panel, as that must be visible) */ {
						if ((gtmp.index + 1) == tokenPanels.size()) {
							gtmp = null; // nothing more to work with
							break; // no more token panels to look at
						}
						else gtmp = ((TokenMarkupPanel) tokenPanels.get(gtmp.index + 1)); // switch to subsequent token panel
					}
					if (gtmp == null)
						break; // no more viable token panels
					else offset = 0; // found next token panek, start over from its start
				}
				this.startObjectTrays = ((TokenMarkupPanelObjectTray[]) startObjectTrays.toArray(new TokenMarkupPanelObjectTray[startObjectTrays.size()]));
			}
			if (this.endObjectTrays == null) {
				LinkedHashSet endObjectTrays = new LinkedHashSet();
				TokenMarkupPanel gtmp = this.lastGtmp;
				int offset = this.lastGtmpOffset;
				while (0 < offset) {
					TokenMarkupPanelObjectTray objectTray = gtmp.getObjectTrayAt(offset - 1);
					if ((objectTray.index != TokenMarkupPanelObjectTray.WHITESPACE_INDEX) || (endObjectTrays.size() != 0))
						endObjectTrays.add(objectTray);
					if (-1 < objectTray.index) /* found some token */ {
						this.lastTokenIndex = objectTray.index;
						this.lastTokenGtmp = gtmp;
						break; // found first token, all we need
					}
					offset--;
					if ((gtmp == this.firstGtmp) && (offset <= this.firstGtmpOffset))
						break; // reached start of selection
					if (0 < offset)
						continue; // more to come in this panel
					if (gtmp.index == 0)
						break; // no more token panels to look at
					gtmp = ((TokenMarkupPanel) tokenPanels.get(gtmp.index - 1)); // switch to preceding token panel
					while (gtmp.isFoldedAway()) /* skip over any floded token panels (no risk of skipping over selection start panel, as that must be visible) */ {
						if (gtmp.index == 0) {
							gtmp = null; // nothing more to work with
							break; // no more token panels to look at
						}
						else gtmp = ((TokenMarkupPanel) tokenPanels.get(gtmp.index - 1)); // switch to preceding token panel
					}
					if (gtmp == null)
						break; // no more viable token panels
					else offset = gtmp.getAnchorCount(); // found next token panek, start over from its end
				}
				this.endObjectTrays = ((TokenMarkupPanelObjectTray[]) endObjectTrays.toArray(new TokenMarkupPanelObjectTray[endObjectTrays.size()]));
				if (this.endObjectTrays.length < 1) /* need to reverse these trays, as we added them back to front */ {
					TokenMarkupPanelObjectTray objectTray;
					for (int t = 0; t < (this.endObjectTrays.length / 2); t++) {
						objectTray = this.endObjectTrays[t];
						this.endObjectTrays[t] = this.endObjectTrays[this.endObjectTrays.length - 1 - t];
						this.endObjectTrays[this.endObjectTrays.length - 1 - t] = objectTray;
					}
				}
			}
		}
		private TokenMarkupPanelObjectTray[] startObjectTrays = null;
		private int firstTokenIndex = -1;
		private TokenMarkupPanel firstTokenGtmp = null;
		private TokenMarkupPanelObjectTray[] endObjectTrays = null;
		private int lastTokenIndex = -1;
		private TokenMarkupPanel lastTokenGtmp = null;
		
		public boolean isSingleTokenSelection() {
			this.ensureBounradies();
			if ((this.startObjectTrays.length != 1) || (this.endObjectTrays.length != 1))
				return false;
			if (this.startObjectTrays[0] != this.endObjectTrays[0])
				return false;
			return (-1 < this.startObjectTrays[0].index);
		}
		public Token getToken() {
			this.ensureBounradies();
			if ((this.startObjectTrays.length != 1) || (this.endObjectTrays.length != 1))
				return null;
			if (this.startObjectTrays[0] != this.endObjectTrays[0])
				return null;
			return ((this.startObjectTrays[0].index < 0) ? null : ((Token) this.startObjectTrays[0].object));
		}
		public int getTokenIndex() {
			this.ensureBounradies();
			if ((this.startObjectTrays.length != 1) || (this.endObjectTrays.length != 1))
				return -1;
			if (this.startObjectTrays[0] != this.endObjectTrays[0])
				return -1;
			return ((this.startObjectTrays[0].index < 0) ? -1 : this.startObjectTrays[0].index);
		}
		
//		public Paragraph getFirstParagraph() {
//			return this.firstGtmp.para.para;
//		}
		public Token firstToken() {
			this.ensureFirstToken();
			return this.firstToken;
		}
		public int firstTokenIndex() {
			this.ensureFirstToken();
			return ((this.firstTokenIndex < 0) ? -1 : this.firstTokenIndex);
		}
		public int firstTokenStartOffset() {
			this.ensureFirstToken();
			return ((this.firstTokenStartOffset < 0) ? -1 : this.firstTokenStartOffset);
		}
		private void ensureFirstToken() {
			if (this.firstTokenStartOffset != -1)
				return;
			this.ensureBounradies();
			if (this.firstTokenIndex == -1) {
				this.firstTokenStartOffset = -2; // only space or annotation highlight end caps selected
				return;
			}
			if (this.startObjectTrays.length == 0) {
				this.firstTokenStartOffset = -2; // nothing to work with (something is _weird_)
				return;
			}
			this.firstToken = ((Token) this.startObjectTrays[this.startObjectTrays.length-1].object); // token tray is last in array, right of any leading objects
			this.firstTokenStartOffset = 0;
			if (this.firstTokenGtmp != this.firstGtmp)
				return; // selected across token panel boundary, token must be fully selected (or at least from start)
			if (this.startObjectTrays.length != 1)
				return; // space or annotation highlight end caps selected before start of token, we're done
			for (int lo = 1; lo <= this.firstGtmpOffset; lo++) {
				if (this.firstGtmp.getObjectTrayAt(this.firstGtmpOffset - lo) == this.startObjectTrays[0])
					this.firstTokenStartOffset++;
				else break;
			}
		}
		private Token firstToken = null;
		private int firstTokenStartOffset = -1; // offset _INTO_VALUE_OF_ first token (if latter partially selected)
		
//		public Paragraph getLastParagraph() {
//			return this.lastGtmp.para.para;
//		}
		public Token lastToken() {
			this.ensureLastToken();
			return this.lastToken;
		}
		public int lastTokenIndex() {
			this.ensureLastToken();
			return ((this.lastTokenIndex < 0) ? -1 : this.lastTokenIndex);
		}
		public int lastTokenEndOffset() {
			this.ensureLastToken();
			return ((this.lastTokenEndOffset < 0) ? -1 : this.lastTokenEndOffset);
		}
		private void ensureLastToken() {
			if (this.lastTokenEndOffset != -1)
				return;
			this.ensureBounradies();
			if (this.lastTokenIndex == -1) {
				this.lastTokenEndOffset = -2; // only space or annotation highlight end caps selected
				return;
			}
			if (this.endObjectTrays.length == 0) {
				this.lastTokenEndOffset = -2; // nothing to work with (something is _weird_)
				return;
			}
			this.lastToken = ((Token) this.endObjectTrays[0].object); // token tray is first in array, left of tailing any tailing objects
			this.lastTokenEndOffset = this.lastToken.length();
			if (this.lastTokenGtmp != this.lastGtmp)
				return; // selected across token panel boundary, token must be fully selected (or at least through end)
			if (this.endObjectTrays.length != 1)
				return; // space or annotation highlight end caps selected after end of token, we're done
			for (int lo = 1; lo <= this.firstGtmpOffset; lo++) {
				if (this.firstTokenGtmp.getObjectTrayAt(this.firstGtmpOffset - lo) == this.startObjectTrays[0])
					this.firstTokenStartOffset++;
				else break;
			}
			for (int lo = this.lastGtmpOffset; lo < this.lastGtmp.getAnchorCount(); lo++) {
				if (this.lastGtmp.getObjectTrayAt(lo) == this.endObjectTrays[0])
					this.lastTokenEndOffset--;
				else break;
			}
		}
		private Token lastToken = null;
		private int lastTokenEndOffset = -1; // offset _INTO_VALUE_OF_ last token (if latter partially selected)
		
//		public int getParagraphCount() {
//			return (this.lastGtmp.para.index - this.firstGtmp.para.index + 1);
//		}
//		public Paragraph[] getParagraphs() {
//			if (this.paras == null) {
//				LinkedHashSet paras = new LinkedHashSet();
//				for (int p = this.firstGtmp.para.index; p <= this.lastGtmp.para.index; p++) {
//					ParaMarkupPanel xpmp = ((ParaMarkupPanel) paraPanels.get(p));
//					if (xpmp.isFoldedAway())
//						continue;
//					paras.add(xpmp.para);
//				}
//				this.paras = ((Paragraph[]) paras.toArray(new Paragraph[paras.size()]));
//			}
//			return this.paras;
//		}
		public int getTokenCount() {
			if (this.tokenCount == -1) {
				this.ensureBounradies();
				if ((this.firstTokenIndex == -1) || (this.lastTokenIndex == -1))
					this.tokenCount = 0;
				else this.tokenCount = (this.lastTokenIndex - this.firstTokenIndex + 1);
			}
			return this.tokenCount;
		}
		public Token[] getTokens() {
			if (this.tokens == null) {
				this.ensureBounradies();
				if ((this.firstTokenIndex == -1) || (this.lastTokenIndex == -1))
					this.tokens = new Token[0];
				else {
					ArrayList tokens = new ArrayList(); // need to use list, as intermediate tokens panels might be forlded away
					TokenMarkupPanel gtmp = this.firstTokenGtmp;
					for (int t = this.firstTokenIndex; t <= this.lastTokenIndex; t++) {
						if (gtmp.maxTokenIndex < t) {
							gtmp = ((TokenMarkupPanel) tokenPanels.get(gtmp.index + 1));
							if (gtmp.isFoldedAway()) {
								t = gtmp.maxTokenIndex;
								continue; // next round will readily switch to next token panel after loop increment
							}
						}
						tokens.add(document.tokenAt(t));
					}
					this.tokens = ((Token[]) tokens.toArray(new Token[tokens.size()]));
				}
			}
			return this.tokens;
		}
//		private Paragraph[] paras = null;
		private Token[] tokens = null;
		private int tokenCount = -1;
		
		public boolean isAnnotationStartSelection() {
			this.ensureBounradies();
			if (this.startObjectTrays.length != 1)
				return false;
			return (this.startObjectTrays[0].index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX);
		}
		public boolean isAnnotationEndSelection() {
			this.ensureBounradies();
			if (this.endObjectTrays.length != 1)
				return false;
			return (this.endObjectTrays[0].index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX);
		}
		public Annotation getAnnotation() {
			if (this.isAnnotationStartSelection())
				return ((Annotation) this.startObjectTrays[0].object);
			else if (this.isAnnotationEndSelection())
				return ((Annotation) this.endObjectTrays[0].object);
			else return null;
		}
		
		public Annotation[] getOverlappingAnnotations(boolean includeTags, boolean includeHighlights) {
			if (includeTags && includeHighlights) {
				if (this.oAnnotsTH != null)
					return this.oAnnotsTH;
			}
			else if (includeTags) {
				if (this.oAnnotsT != null)
					return this.oAnnotsT;
			}
			else if (includeHighlights) {
				if (this.oAnnotsH != null)
					return this.oAnnotsH;
			}
			else return new Annotation[0];
			this.ensureBounradies();
			//	TODO might actually have to collect annotation UUIDs first, and then get them in single go
			LinkedHashSet oAnnotsTH = new LinkedHashSet();
			LinkedHashSet oAnnotsT = new LinkedHashSet();
			LinkedHashSet oAnnotsH = new LinkedHashSet();
			for (int t = 0; t < this.startObjectTrays.length; t++) {
				if (this.startObjectTrays[t].index != TokenMarkupPanelObjectTray.ANNOT_END_INDEX)
					continue; // only need tailing end caps, we get starting annotations with overlapping ones below
				oAnnotsTH.add((Annotation) this.startObjectTrays[t].object);
				oAnnotsH.add((Annotation) this.startObjectTrays[t].object);
			}
			if ((this.firstTokenIndex != -1) && (this.lastTokenIndex != -1)) {
				Annotation[] oAnnots = document.getAnnotationsOverlapping(this.firstTokenIndex, (this.lastTokenIndex + 1));
				for (int a = 0; a < oAnnots.length; a++) {
					Character displayMode = getDetailAnnotationDisplayMode(oAnnots[a].getType());
					if (displayMode == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
						oAnnotsTH.add(oAnnots[a]);
						oAnnotsH.add(oAnnots[a]);
					}
					else if ((displayMode == DISPLAY_MODE_SHOW_TAGS) || (displayMode == DISPLAY_MODE_FOLD_CONTENT)) {
						oAnnotsTH.add(oAnnots[a]);
						oAnnotsT.add(oAnnots[a]);
					}
				}
			}
			for (int t = 0; t < this.endObjectTrays.length; t++) {
				if (this.endObjectTrays[t].index != TokenMarkupPanelObjectTray.ANNOT_START_INDEX)
					continue; // only need leading end caps, we get ending annotations with overlapping ones above
				oAnnotsTH.add((Annotation) this.endObjectTrays[t].object);
				oAnnotsH.add((Annotation) this.endObjectTrays[t].object);
			}
			this.oAnnotsTH = ((Annotation[]) oAnnotsTH.toArray(new Annotation[oAnnotsTH.size()]));
			Arrays.sort(this.oAnnotsTH, AnnotationUtils.ANNOTATION_NESTING_ORDER /* type and creation order based ordering maintained by order of operations*/);
			this.oAnnotsT = ((Annotation[]) oAnnotsT.toArray(new Annotation[oAnnotsT.size()]));
			Arrays.sort(this.oAnnotsT, AnnotationUtils.ANNOTATION_NESTING_ORDER /* type and creation order based ordering maintained by order of operations*/);
			this.oAnnotsH = ((Annotation[]) oAnnotsH.toArray(new Annotation[oAnnotsH.size()]));
			Arrays.sort(this.oAnnotsH, AnnotationUtils.ANNOTATION_NESTING_ORDER /* type and creation order based ordering maintained by order of operations*/);
			if (includeTags && includeHighlights)
				return this.oAnnotsTH;
			else if (includeTags)
				return this.oAnnotsT;
			else if (includeHighlights)
				return this.oAnnotsH;
			else return new Annotation[0];
		}
		private Annotation[] oAnnotsTH = null;
		private Annotation[] oAnnotsT = null;
		private Annotation[] oAnnotsH = null;
		
		public boolean includesStartOf(Annotation annot) {
			Character displayMode = getDetailAnnotationDisplayMode(annot.getType());
			if (displayMode == DISPLAY_MODE_INVISIBLE)
				return false; // annotation not showing, cannot be visible
			this.ensureBounradies();
			if ((this.firstTokenIndex == -1) || (this.lastTokenIndex == -1)) {} // need to check for selected highlight end caps below
			else if (annot.getStartIndex() < this.firstTokenIndex)
				return false; // starts left of first token
			else if (annot.getStartIndex() == this.firstTokenIndex) {
				//	need to check this below s we also have to do this with no tokens at all
			}
			else if (annot.getStartIndex() <= this.lastTokenIndex)
				return true; // visible, and start before or at last token
			else if (annot.getStartIndex() == (this.lastTokenIndex + 1)) {
				//	need to check below if we might have selected leading highlight end cap after last token
			}
			else return false; // starts further after end
			
			for (int t = 0; t < this.startObjectTrays.length; t++) {
				if ((this.startObjectTrays[t].index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) && (this.startObjectTrays[t].object == annot))
					return true;
			}
			//	TODO might have selected across start tag from annotation highlight end caps on preceding token panel
			
			for (int t = 0; t < this.endObjectTrays.length; t++) {
				if ((this.endObjectTrays[t].index == TokenMarkupPanelObjectTray.ANNOT_START_INDEX) && (this.startObjectTrays[t].object == annot))
					return true;
			}
			//	TODO might have selected across start tag to annotation highlight end caps on subsequent token panel
			
			return false;
		}
		public boolean includesEndOf(Annotation annot) {
			Character displayMode = getDetailAnnotationDisplayMode(annot.getType());
			if (displayMode == DISPLAY_MODE_INVISIBLE)
				return false; // annotation not showing, cannot be visible
			this.ensureBounradies();
			if ((this.firstTokenIndex == -1) || (this.lastTokenIndex == -1)) {} // need to check for selected highlight end caps below
			else if (annot.getEndIndex() < this.firstTokenIndex)
				return false; // ends further left than right before first token
			else if (annot.getEndIndex() == this.firstTokenIndex) {
				//	need to check this below s we also have to do this with no tokens at all
			}
			else if (annot.getEndIndex() <= this.lastTokenIndex)
				return true; // visible, and ends before last token
			else if (annot.getStartIndex() == (this.lastTokenIndex + 1)) {
				//	need to check below if we might have selected leading highlight end cap after last token
			}
			else return false; // starts further after end
			
			for (int t = 0; t < this.startObjectTrays.length; t++) {
				if ((this.startObjectTrays[t].index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX) && (this.startObjectTrays[t].object == annot))
					return true;
			}
			//	TODO might have selected across end tag from annotation highlight end caps on preceding token panel
			
			for (int t = 0; t < this.endObjectTrays.length; t++) {
				if ((this.endObjectTrays[t].index == TokenMarkupPanelObjectTray.ANNOT_END_INDEX) && (this.startObjectTrays[t].object == annot))
					return true;
			}
			//	TODO might have selected across end tag to annotation highlight end caps on subsequent token panel
			
			return false;
		}
//		
//		public boolean includesStartOf(Paragraph para) {
//			if (para.getIndex() <= this.firstGtmp.para.para.getIndex())
//				return false;
//			else if (para.getIndex() <= this.lastGtmp.para.para.getIndex())
//				return true;
//			else return false;
//		}
//		public boolean includesEndOf(Paragraph para) {
//			if (para.getIndex() < this.firstGtmp.para.para.getIndex())
//				return false;
//			else if (para.getIndex() < this.lastGtmp.para.para.getIndex())
//				return true;
//			else return false;
//		}
/*
TODO Some thoughts on anchor based API (which whole display architecture seems to be going to):
- facilitates precise insertion of tokens or even paragraphs between or before or after anchors
- can also cope with token value changes and token removals
- will wreak havoc to UUIDs, though ...
- ... as well as local IDs, and indexes
  ==> would require non-final index in tokens and paragraphs, as well as global first token index in paragraphs
  ==> using 'token@<paraIndex>/<localIndex>' should be somewhat more stable than 'token@<absoluteIndex>'
  ==> using '<annotType>@<paraIndex>/<firstLocalIndex>-<paraIndex>/<lastLocalIndex>' should be somewhat more stable than '<annotType>@<firstAbsoluteIndex>-<lastAbsoluteIndex>'
  ==> would still require lots of index updating, and likely be quite slow
    ==> atomic anchor actions on document proper might remedy speed issues to some degree ...
    ==> ... but add whole new level of complications, especially with UNDO, etc.
- might actually work if only updating local IDs ...
- ... while keeping externally provided UUIDs stable (obviously) ...
- ... and having locally computed annotation UUIDs only ever re-compute on type changes ...
- ... while sticking with first computed LUIDs and UUIDs on token changes
  ==> sure saves updates and still has doing same things result in same UUIDs ...
  ==> ... but does make order of operations matter much more than desirable ...
  ==> ... as same XML-wise result might have different LUIDs and UUIDs depending on order of annotation and token changes
  ==> also might incur LUID and UUID collisions, especially on empty annotations
==> DO NOT expose any such functionality in GGX ...
==> ... AT VERY LEAST initially, as we still got that end of April deadline
	 */
	}
	
	/**
	 * Selection object describing a selection in the tags of document
	 * annotations currently visualized that way.
	 * 
	 * @author sautter
	 */
	public class TagSelection {
		private AnnotTagPanel firstGatp;
		private int firstGatpOffset;
		private AnnotTagPanel lastGatp;
		private int lastGatpOffset;
		private boolean reverseSelection;
		TagSelection(AnnotTagPanel gatp, int firstOffset, int lastOffset, boolean reverseSelection) {
			this(gatp, firstOffset, gatp, lastOffset, reverseSelection);
		}
		TagSelection(AnnotTagPanel firstGatp, int firstGatpOffset, AnnotTagPanel lastGatp, int lastGatpOffset, boolean reverseSelection) {
			this.firstGatp = firstGatp;
			this.firstGatpOffset = firstGatpOffset;
			this.lastGatp = lastGatp;
			this.lastGatpOffset = lastGatpOffset;
			this.reverseSelection = reverseSelection;
		}
		
		public boolean isReverseSelection() {
			return this.reverseSelection;
		}
		
		public Annotation getAnnotation() {
			if (this.firstGatp == this.lastGatp)
				return this.firstGatp.annot;
			else if (((firstGatp.index + 1) == this.lastGatp.index) && (this.firstGatp.annot == this.lastGatp.annot))
				return this.firstGatp.annot;
			else return null;
		}
		public boolean isSingleTagSelection() {
			return (this.firstGatp == this.lastGatp);
		}
		public boolean isStartTagSelection() {
			if (this.firstGatp == this.lastGatp)
				return this.firstGatp.isStartTag;
			else return false;
		}
		public boolean isEndTagSelection() {
			if (this.firstGatp == this.lastGatp)
				return !this.firstGatp.isStartTag;
			else return false;
		}
		public String getAttributeName() {
			if (this.isStartTagSelection())
				this.ensureStartTagSelectionDetails();
			return this.attributeName;
		}
		public boolean isAnnotationTypeSelected() {
			if (this.isStartTagSelection())
				this.ensureStartTagSelectionDetails();
			return (this.tagSelectionMode == 'T'); // selection mode 'type'
		}
		public boolean isAttributeNameSelected() {
			if (this.isStartTagSelection())
				this.ensureStartTagSelectionDetails();
			return ((this.tagSelectionMode == 'N') || (this.tagSelectionMode == 'B')); // selection mode 'name' or 'both'
		}
		public boolean isAttributeValueSelected() {
			if (this.isStartTagSelection())
				this.ensureStartTagSelectionDetails();
			return ((this.tagSelectionMode == 'V') || (this.tagSelectionMode == 'B')); // selection mode 'value' or 'both'
		}
		private void ensureStartTagSelectionDetails() {
			if (this.tagSelectionMode != 'U')
				return;
			
			//	assess selected portion of start tag
			StartTagIndex sti = createStartTagIndex(this.firstGatp.annot);
			int annotTypeCharCount = 0;
			int attribNameCount = 0;
			int firstAttribNameCharOffset = -1;
			int attribValueCount = 0;
			int firstAttribValueCharOffset = -1;
			for (int o = this.firstGatpOffset; o <= this.lastGatpOffset; o++) {
				if (sti.tag.length() <= o)
					break;
				char ic = sti.index.charAt(o);
				if (ic == 'T')
					annotTypeCharCount++;
				else if (ic == 'N') {
					attribNameCount++;
					if (firstAttribNameCharOffset == -1)
						firstAttribNameCharOffset = o;
					for (int lo = (o+1); lo <= this.lastGatpOffset; lo++) {
						if (sti.tag.length() <= lo)
							break;
						char lic = sti.index.charAt(lo);
						if (lic != 'N') {
							o = lo; // no need to compensate for loop increment, must have hit '=', which we can ignore
							break;
						}
					}
				}
				else if (ic == 'V') {
					attribValueCount++;
					if (firstAttribValueCharOffset == -1)
						firstAttribValueCharOffset = o;
					for (int lo = (o+1); lo <= this.lastGatpOffset; lo++) {
						if (sti.tag.length() <= lo)
							break;
						char lic = sti.index.charAt(lo);
						if (lic != 'V') {
							o = lo; // no need to compensate for loop increment, must have hit '"', which we can ignore
							break;
						}
					}
				}
			}
			
			//	annotation type included, not an attribute selection
			if (annotTypeCharCount != 0) {
				if ((attribNameCount == 0) && (attribValueCount == 0))
					this.tagSelectionMode = 'T'; // annotation type only
				else this.tagSelectionMode = 'I'; // invalid (type and some attributes)
			}
			else if (1 < attribNameCount)
				this.tagSelectionMode = 'I'; // invalid (multiple attribute names)
			else if (1 < attribValueCount)
				this.tagSelectionMode = 'I'; // invalid (multiple attribute values)
			else if ((firstAttribValueCharOffset != -1) && (firstAttribValueCharOffset < firstAttribNameCharOffset))
				this.tagSelectionMode = 'I'; // invalid (attribute value preceding attribute name)
			else if ((attribNameCount == 1) && (attribValueCount == 1))
				this.tagSelectionMode = 'B'; // both attribute name and associated value
			else if (attribNameCount == 1)
				this.tagSelectionMode = 'N'; // attribute name only
			else if (attribValueCount == 1)
				this.tagSelectionMode = 'V'; // attribute value only
			else this.tagSelectionMode = 'I'; // invalid (spaces or punctuation marks only)
			if ((this.tagSelectionMode == 'N') || (this.tagSelectionMode == 'B')) {
				while ((firstAttribNameCharOffset != 0) && (sti.index.charAt(firstAttribNameCharOffset - 1) == 'N'))
					firstAttribNameCharOffset--;
				for (int aneo = (firstAttribNameCharOffset + 1); aneo < sti.index.length(); aneo++) {
					char ic = sti.index.charAt(aneo);
					if (ic == 'N')
						continue;
					this.attributeName = sti.tag.substring(firstAttribNameCharOffset, aneo);
					break;
				}
			}
			else if (this.tagSelectionMode == 'V') {
				int aneo = firstAttribValueCharOffset;
				while ((aneo != 0) && (sti.index.charAt(aneo - 1) == 'N'))
					aneo--;
				int anso = aneo;
				while ((anso != 0) && (sti.index.charAt(anso - 1) == 'N'))
					anso--;
				this.attributeName = sti.tag.substring(anso, aneo);
			}
		}
		private String attributeName = null;
		private char tagSelectionMode = 'U'; // initialize to 'unknown'
		
		public Annotation[] getAnnotations(boolean includeOverlapping) {
			if (includeOverlapping ? (this.oAnnots == null) : (this.cAnnots == null)) {
				LinkedHashSet gotStartAnchor = new LinkedHashSet();
				LinkedHashSet gotEndAnchor = new LinkedHashSet();
				for (int p = this.firstGatp.index; p <= this.lastGatp.index; p++) {
					AnnotTagPanel gatp = ((AnnotTagPanel) tagPanels.get(p));
					(gatp.isStartTag ? gotStartAnchor : gotEndAnchor).add(gatp.annot);
				}
				if (includeOverlapping) {
					gotStartAnchor.addAll(gotEndAnchor);
					this.oAnnots = ((Annotation[]) gotStartAnchor.toArray(new Annotation[gotStartAnchor.size()]));
				}
				else {
					gotStartAnchor.retainAll(gotEndAnchor);
					this.cAnnots = ((Annotation[]) gotStartAnchor.toArray(new Annotation[gotStartAnchor.size()]));
				}
			}
			return (includeOverlapping ? this.oAnnots : this.cAnnots);
		}
		private Annotation[] cAnnots = null;
		private Annotation[] oAnnots = null;
		public boolean includesStartOf(Annotation annot) {
//			if (annot instanceof Paragraph)
//				return this.includesParagraph((Paragraph) annot);
//			Anchor[] anchors = this.getAnchors();
//			for (int a = 0; a < anchors.length; a++) {
//				if ((anchors[a].type == Anchor.TYPE_START_ANCHOR) && (anchors[a].getAnnotation() == annot))
//					return true;
//			}
			for (int p = this.firstGatp.index; p <= this.lastGatp.index; p++) {
				AnnotTagPanel gatp = ((AnnotTagPanel) tagPanels.get(p));
				if (gatp.isStartTag && (gatp.annot == annot))
					return true;
			}
			return false;
		}
		public boolean includesEndOf(Annotation annot) {
//			if (annot instanceof Paragraph)
//				return this.includesParagraph((Paragraph) annot);
//			Anchor[] anchors = this.getAnchors();
//			for (int a = 0; a < anchors.length; a++) {
//				if ((anchors[a].type == Anchor.TYPE_END_ANCHOR) && (anchors[a].getAnnotation() == annot))
//					return true;
//			}
			for (int p = this.firstGatp.index; p <= this.lastGatp.index; p++) {
				AnnotTagPanel gatp = ((AnnotTagPanel) tagPanels.get(p));
				if (!gatp.isStartTag && (gatp.annot == annot))
					return true;
			}
			return false;
		}
//		private boolean includesParagraph(Paragraph para) {
//			for (int a = 0; a < anchors.length; a++) {
//				if ((anchors[a].type == Anchor.TYPE_PARAGRAPH_ANCHOR) && (anchors[a].getParagraph() == para))
//					return true;
//			}
//			return false;
//		}
	}
	static class StartTagIndex {
		final String tag;
		final String index;
		StartTagIndex(String tag, String index) {
			this.tag = tag;
			this.index = index;
		}
	}
	static StartTagIndex createStartTagIndex(Annotation annot) {
		StringBuffer st = new StringBuffer();
		StringBuffer sti = new StringBuffer();
		st.append('<');
		sti.append('P'); // punctuation
		String type = annot.getType();
		for (int c = 0; c < type.length(); c++) {
			st.append(type.charAt(c));
			sti.append('T'); // annotation type
		}
		String[] ans = annot.getAttributeNames();
		for (int a = 0; a < ans.length; a++) {
			Object avo = annot.getAttribute(ans[a]);
			if (avo == null)
				continue;
			String avs = ((avo instanceof String) ? ((String) avo) : avo.toString());
			st.append(' ');
			sti.append('S'); // space
			for (int c = 0; c < ans[a].length(); c++) {
				st.append(ans[a].charAt(c));
				sti.append('N'); // attribute name
			}
			st.append('=');
			sti.append('P'); // punctuation
			st.append('"');
			sti.append('P'); // punctuation
			String av = AnnotationUtils.escapeForXml(avs);
			for (int c = 0; c < av.length(); c++) {
				st.append(av.charAt(c));
				sti.append('V'); // attribute name
			}
			st.append('"');
			sti.append('P'); // punctuation
		}
		st.append('>');
		sti.append('P'); // punctuation
		return new StartTagIndex(st.toString(), sti.toString());
	}
	
	/**
	 * Implementation of an action to perform for a box or word selection. Sub
	 * classes have to implement the <code>performAction()</code> method. They
	 * can further overwrite the <code>getMenuItem()</code> method, e.g. to
	 * provide a sub menu instead of a single menu item. In the latter case,
	 * the <code>performAction()</code> method should be implemented to do
	 * nothing, putting the functionality into the sub menu content.
	 * 
	 * @author sautter
	 */
	public static abstract class SelectionAction {
		
		/** including this constant action in an array of actions causes a separator to be added to the context menu */
		public static final SelectionAction SEPARATOR = new SelectionAction("SEPARATOR") {
			public boolean performAction(GamtaDocumentMarkupPanel invoker) { return false; }
		};
		
		/** the name of the selection action, identifying what the action does */
		public final String name;
		
		/** the label string representing the selection action in the context menu */
		public final String label;
		
		/** the tooltip explaining the selection action in the context menu */
		public final String tooltip;
		
		/** Constructor
		 * @param name the name of the selection action
		 */
		private SelectionAction(String name) {
			this(name, name, null);
		}
		
		/** Constructor
		 * @param name the name of the selection action
		 * @param label the label string to show in the context menu
		 */
		public SelectionAction(String name, String label) {
			this(name, label, null);
		}
		
		/** Constructor
		 * @param name the name of the selection action
		 * @param label the label string to show in the context menu
		 * @param tooltip the tooltip text for the context menu
		 */
		public SelectionAction(String name, String label, String tooltip) {
			this.name = name;
			this.label = label;
			this.tooltip = tooltip;
		}
		
		/**
		 * Indicate whether or not this selection action is an atomic action in
		 * itself. If this method returns true (and this default implementation
		 * does), the default menu item will encapsulate any call to the
		 * <code>performAction()</code> method in an atomic action, using the
		 * action label as the label for the atomic action. Sub classes can
		 * overwrite this method to change this behavior.
		 * @return true if <code>performAction()</code> is to be atomic
		 */
		protected boolean isAtomicAction() {
			return true;
		}
		
		/**
		 * Perform the action.
		 * @param invoker the component the parent menu shows on
		 * @return true if the document was changed by the method, false otherwise
		 */
		public abstract boolean performAction(GamtaDocumentMarkupPanel invoker);
		
		/**
		 * Retrieve a menu item to represent the action in the context menu.
		 * This default implementation returns a <code>JMenuItem</code> with
		 * the label and tooltip handed to the constructor, and an action
		 * listener calling the performAction() method. If the latter returns
		 * true, the argument invoker is repainted. Sub classes may overwrite
		 * this method to provide a better suited representation of themselves.
		 * As this method also handles atomicity of the changes performed in
		 * the argument document, however, sub classes overwriting this method
		 * have to take care of the latter as well.
		 * @param invoker the component the parent menu shows on
		 * @return a menu item to represent the action in the context menu
		 */
		public JMenuItem getMenuItem(final GamtaDocumentMarkupPanel invoker) {
			JMenuItem mi = new JMenuItem(this.label);
			if (this.tooltip != null)
				mi.setToolTipText(this.tooltip);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					boolean isAtomicAction = isAtomicAction();
					try {
						if (isAtomicAction)
							invoker.beginAtomicAction(label);
						boolean changed = performAction(invoker);
						if (changed) {
							invoker.validate();
							invoker.repaint();
						}
					}
					finally {
						if (isAtomicAction)
							invoker.endAtomicAction();
					}
				}
			});
			return mi;
		}
		
		JMenuItem getContextMenuItem(GamtaDocumentMarkupPanel invoker) {
			if (this.contextMenuItem == null)
				this.contextMenuItem = invoker.getContextMenuItemFor(this);
			return this.contextMenuItem;
		}
		JMenuItem contextMenuItem = null;
	}
	
	/**
	 * Create a context menu item for a selection action. This default implementation
	 * simply calls <code>getMenuItem()</code> on the argument selection action. Sub
	 * classes are welcome to overwrite this method to add furether implementation
	 * specific operations.
	 * @param action the selection action to obtain the mnu item for
	 * @return the menu item for the argument selection action
	 */
	protected JMenuItem getContextMenuItemFor(SelectionAction action) {
		return action.getMenuItem(this);
	}
	
	/**
	 * Selection action to execute straight away for an input event, i.e.,
	 * without intermediate display of a context menu. Instant actions provide
	 * a means of injecting default behavior on plain clicks, i.e., selections
	 * that do not involve mouse movement in between pressing and releasing the
	 * mouse button, as well as on keyboard input. If multiple instant actions
	 * are available for a single input event, they are consulted in order of
	 * descending priority, stopping soon as the first returns true from it
	 * <code>executeAction()</code> method. No further instant actions come to
	 * bear after that. If no instant action handles an input event, normal
	 * selection actions will display in a context menu as usual.
	 * 
	 * @author sautter
	 */
	public static abstract class InstantAction extends SelectionAction implements Comparable {
		
		/** Constructor
		 * @param name the name of the selection action
		 * @param label the label string to show in the context menu
		 */
		public InstantAction(String name, String label) {
			super(name, label);
		}
		
		/** Constructor
		 * @param name the name of the selection action
		 * @param label the label string to show in the context menu
		 * @param tooltip the tooltip text for the context menu
		 */
		public InstantAction(String name, String label, String tooltip) {
			super(name, label, tooltip);
		}
		
		public boolean performAction(GamtaDocumentMarkupPanel invoker) {
			return false;
		}
		
		/**
		 * Indicate the priority of the click action, on a 0-10 scale. In the
		 * presence of multiple actions for a single click, their
		 * <code>handleClick()</code> methods are consulted in descending
		 * priority order until the first one returns true.
		 * @return the priority of the click action
		 */
		public abstract int getPriority();
		
		/**
		 * Actually execute the action. Since the return value of this method
		 * is used for controlling behavior, implementations have to handle any
		 * atomic actions internally.
		 * @param invoker the document markup panel the click happened in
		 * @return true if the input event was handled
		 */
		public abstract boolean executeAction(GamtaDocumentMarkupPanel invoker);
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object obj) {
			return (((InstantAction) obj).getPriority() - this.getPriority());
		}
	}
	
	TwoClickSelectionAction pendingTwoClickAction = null;
	TwoClickActionMessenger twoClickActionMessenger = null;
	
	/**
	 * Selection action that works with two clicks rather than one, usually
	 * with intermediate scrolling. If a two-click action is selected in the
	 * context menu after a click, it remains active until completed by a click
	 * on a second word, or cancelled by a new selection or a click outside any
	 * word.
	 * 
	 * @author sautter
	 */
	public static abstract class TwoClickSelectionAction extends SelectionAction {
		
		/** Constructor
		 * @param name the name of the selection action
		 * @param label the label string to show in the context menu
		 */
		public TwoClickSelectionAction(String name, String label) {
			super(name, label);
		}
		
		/** Constructor
		 * @param name the name of the selection action
		 * @param label the label string to show in the context menu
		 * @param tooltip the tooltip text for the context menu
		 */
		public TwoClickSelectionAction(String name, String label, String tooltip) {
			super(name, label, tooltip);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.DocumentViewerPanel.SelectionAction#performAction()
		 */
		public final boolean performAction(GamtaDocumentMarkupPanel invoker) {
			invoker.pendingTwoClickAction = this;
			if (invoker.twoClickActionMessenger != null)
				invoker.twoClickActionMessenger.twoClickActionChanged(this);
			return false;
		}
//		
//		/**
//		 * Sub classes overwriting this method to return something else but a
//		 * single menu item have to make sure to call the <code>performAction()</code>
//		 * method, as the latter registers the two-click action as pending
//		 * @see de.uka.ipd.idaho.im.util.ImPageMarkupPanel.SelectionAction#getMenuItem(GamtaDocumentMarkupPanel)
//		 */
//		public JMenuItem getMenuItem(GamtaDocumentMarkupPanel invoker) {
//			return super.getMenuItem(invoker);
//		}
		
		/**
		 * Perform the action.
		 * @param ps a point selection describing the click to complete the
		 *        action
		 * @return true if the document was changed by the method, false
		 *        otherwise
		 */
		public abstract boolean performAction(PointSelection ps);
		
		/**
		 * Retrieve a label to display when the action is active, awaiting the
		 * second click
		 * @return the active label
		 */
		public abstract String getActiveLabel();
	}
	
	/**
	 * Set the component to notify when the two-click action changes, usually
	 * the component responsible for displaying the respective message.
	 * @param tcam the two-click action messenger to notify
	 */
	public void setTwoClickActionMessenger(TwoClickActionMessenger tcam) {
		this.twoClickActionMessenger = tcam;
	}
	
	/**
	 * A component to notify when a two-click action changes.
	 * 
	 * @author sautter
	 */
	public static interface TwoClickActionMessenger {
		/**
		 * Receive notification of a change to the pending two-click action
		 * @param tcsa the new pending two-click action
		 */
		public abstract void twoClickActionChanged(TwoClickSelectionAction tcsa);
	}
	
	/**
	 * Retrieve the available actions for a token selection. This implementation
	 * returns an empty array. Sub classes thus have to overwrite it to provide
	 * actual functionality.
	 * @param ts the token selection to obtain the actions for
	 * @return an array holding the actions
	 */
	protected SelectionAction[] getActions(TokenSelection ts) {
		return new SelectionAction[0];
	}
	
	/**
	 * Retrieve the available actions for an annotation tag selection. This
	 * implementation returns an empty array. Sub classes thus have to
	 * overwrite it to provide actual functionality.
	 * @param ts the annotation tag selection to obtain the actions for
	 * @return an array holding the actions
	 */
	protected SelectionAction[] getActions(TagSelection ts) {
		return new SelectionAction[0];
	}
	
	/**
	 * Retrieve the available actions for a mouse click based point selection,
	 * i.e., a click in any component of the markup panel. This implementation
	 * returns an empty array. Sub classes thus have to overwrite it to provide
	 * actual functionality. For keyboard triggered context menu calls, the
	 * argument mouse event is null.
	 * @param ps the point selection describing the click to obtain the actions
	 *            for
	 * @param me the mouse event representing the click
	 * @return an array holding the actions
	 */
	protected SelectionAction[] getActions(PointSelection ps, MouseEvent me) {
		return new SelectionAction[0];
	}
	
	/**
	 * Retrieve the available actions for a given number of clicks on a token
	 * or on any annotation visualization element (tags, vertical tag connector
	 * lines, or highlight end caps). This implementation returns an empty
	 * array. Sub classes thus have to overwrite it to provide actual
	 * functionality.
	 * @param ps the point selection describing the click to obtain the actions
	 *            for
	 * @param me the mouse event representing the click
	 * @return an array holding the actions
	 */
	protected InstantAction[] getClickActions(PointSelection ps, MouseEvent me) {
		return new InstantAction[0];
	}
	
	/**
	 * Assess which selection actions to consider 'advanced' functionality. If
	 * the array returned by this method contains true for a selection action,
	 * that selection action will not be visible in a context menu right away,
	 * but only show after a 'More ...' button is clicked. This helps reducing
	 * the size of the context menu. This default implementation simply returns
	 * an array containing false for each selection action, so all available
	 * actions are visible in the context menu right away. Sub classes willing
	 * to change this behavior can overwrite this method to make more
	 * discriminative decisions.
	 * @param sas an array holding all the selection actions eligible for the
	 *            context menu
	 * @return an array containing true for selection actions to be considered
	 *            advanced, and false for all others
	 */
	protected boolean[] markAdvancedSelectionActions(SelectionAction[] sas) {
		boolean[] isSaAdvanced = new boolean[sas.length];
		Arrays.fill(isSaAdvanced, false);
		return isSaAdvanced;
	}
	
	/**
	 * Retrieve the available actions for keyboard input in the presence of a
	 * token selection. This implementation returns an empty array. Sub classes
	 * thus have to overwrite it to provide actual functionality.
	 * @param ts the token selection to obtain the actions for
	 * @param ke the keyboard input event
	 * @return an array holding the actions
	 */
	protected InstantAction[] getKeystrokeActions(TokenSelection ts, KeyEvent ke) {
		return new InstantAction[0];
	}
	
	/**
	 * Retrieve the available actions for keyboard input in the presence of an
	 * annotation tag selection. This implementation returns an empty array.
	 * Sub classes thus have to overwrite it to provide actual functionality.
	 * @param ts the annotation tag selection to obtain the actions for
	 * @param ke the keyboard input event
	 * @return an array holding the actions
	 */
	protected InstantAction[] getKeystrokeActions(TagSelection ts, KeyEvent ke) {
		return new InstantAction[0];
	}
	
	/**
	 * Retrieve the available actions for keyboard input with the cursor in a
	 * specific position in the absence of a selection. The position can be
	 * withing or in between tokens, on anotation highlight end caps, or within
	 * an annotation tag. The two argument point selections contain information
	 * about the contents to the left and right of the cursor position,
	 * respecively, allowing implementations to act on either one depending
	 * upon the specific input key stroke. At the very start of an anotation
	 * tag or token text area, the left point selection will be null, and the
	 * same applies to the right point selection at the vey end of these areas.
	 * This implementation returns an empty array. Sub classes thus have to
	 * overwrite it to provide actual functionality.
	 * @param leftPs the point selection descriping the contents to the left of
	 *            the cursor position to obtain the actions for
	 * @param rightPs the point selection descriping the contents to the right
	 *            of the cursor position to obtain the actions for
	 * @param ke the keyboard input event
	 * @return an array holding the actions
	 */
	protected InstantAction[] getKeystrokeActions(PointSelection leftPs, PointSelection rightPs, KeyEvent ke) {
		return new InstantAction[0];
	}
	
	/**
	 * Receive notification that a selection action has been performed from the
	 * context menu. This default implementation does nothing, sub classes are
	 * welcome to overwrite it as needed.
	 * @param sa the selection action that has been performed
	 */
	protected void selectionActionPerformed(SelectionAction sa) {}
	
	/**
	 * Retrieve a mutable version of the document displaying in the markup.
	 * panel. 
	 * @return a mutable version of the displaying document
	 */
	public MutableAnnotation getMutableDocument() {
		if (DEBUG_MUTABLE_DOCUMENT) System.out.println("GAMTA Markup Panel: getting mutable document");
		if (this.mutableDocument != null) {
			if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> created before in atomic action " + this.atomicActionId);
			return this.mutableDocument;
		}
		this.mutableDocument = this.createMutableDocument();
		if (DEBUG_MUTABLE_DOCUMENT) {
			System.out.println(" ==> created in " + this.atomicActionId + ": " + this.mutableDocument.getClass());
			if (this.atomicActionId == 0)
				(new RuntimeException()).printStackTrace(System.out);
		}
		if (this.mutableDocumentChangeTracker != null) {
			if (this.annotsEditable) {
				this.mutableDocument.addAnnotationListener(this.mutableDocumentChangeTracker);
				if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> annotation listener added");
			}
			else if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> annotation listener waived");
			if (this.tokensEditable) {
				this.mutableDocument.addTokenSequenceListener(this.mutableDocumentChangeTracker);
				this.mutableDocument.addCharSequenceListener(this.mutableDocumentChangeTracker);
				if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> token sequence listener added");
			}
			else if (DEBUG_MUTABLE_DOCUMENT) System.out.println(" ==> token sequence listener waived");
		}
		return this.mutableDocument;
	}
	private static final boolean DEBUG_MUTABLE_DOCUMENT = true;
	
	/**
	 * This default implementation either casts the displaying document,
	 * or, if the latter is not possible, returns a wrapper round the same
	 * document. Depending upon the actual runtime type of the displaying
	 * document, a varying range of methods on these wrappers will simply throw
	 * exceptions when actually called. Subclasses are welcome to overwrite
	 * this method to provide a more graceful behavior. If this method returns
	 * anything else than the displaying document because the runtime type of
	 * the latter ecessitates it, display refreshing after atomic actions
	 * depends upon a listener added temporarily to track any changes. If the
	 * runtime type of the object returned from this method does not support
	 * listeners, display refreshing not be accurate, as there is no way for
	 * the panel proper to observe any changes. Thus, affected subclasses need
	 * to either call the <code>markContentModified()</code> method as atomic
	 * actions fonish, or set the refresh strategy to refreshing after each
	 * atomic action While the latter is simpler, it might be at the cost of
	 * somewhat reduced runtime efficiency due to unnecessary re-rendering
	 * operations.
	 * @return the mutable document to use for modifications in atomic actions
	 */
	protected MutableAnnotation createMutableDocument() {
		if ((this.document instanceof MutableAnnotation) && this.annotsEditable && this.tokensEditable)
			return ((MutableAnnotation) this.document);
		else if ((this.document instanceof EditableAnnotation) && this.annotsEditable)
			return new MutableEditableAnnotation((EditableAnnotation) this.document);
		else return new MutableQueriableAnnotation(this.document);
	}
	
	/**
	 * Apply an XML markup tool to the document displayed in this editor
	 * panel. If the argument annotation is null, the XML markup tool is
	 * applied to the whole XML markup document displayed in this editor
	 * panel.
	 * @param mt the XML markup tool to apply
	 * @param annot the annotation to apply the XML markup tool to
	 */
	public void applyMarkupTool(final MarkupTool mt, final Annotation annot) {
		
		//	get progress monitor
		final ProgressMonitor pm = this.getProgressMonitor(("Running '" + mt.getLabel() + "', Please Wait"), "", false, true);
		final ProgressMonitorWindow pmw = ((pm instanceof ProgressMonitorWindow) ? ((ProgressMonitorWindow) pm) : null);
		
		//	initialize atomic UNDO (unless handled externally)
		final boolean handleAtomicAction = !this.isAtomicActionRunning();
		if (handleAtomicAction)
			this.startAtomicAction(("Apply " + mt.getLabel()), mt, annot, pm);
		
		//	get modifiable document
		final MutableAnnotation document = this.getMutableDocument();
		
		//	apply document processor, in separate thread
		Thread mtThread = new Thread("MarkupToolApplicator") {
			public void run() {
				AnnotationListener al = null;
				try {
					
					//	wait for splash screen progress monitor to come up (we must not reach the dispose() line before the splash screen even comes up)
					while ((pmw != null) && !pmw.getWindow().isVisible()) try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {}
					
					//	count what is added and removed
					final CountingSet detailAnnotCss = new CountingSet();
					
					//	listen for annotations being added, but do not update display control for every change
					al = new AnnotationListener() {
						public void annotationAdded(QueriableAnnotation doc, Annotation annotation) {
							detailAnnotCss.add(annotation.getType());
						}
						public void annotationRemoved(QueriableAnnotation doc, Annotation annotation) {
							detailAnnotCss.remove(annotation.getType());						}
						public void annotationTypeChanged(QueriableAnnotation doc, Annotation annotation, String oldType) {
							detailAnnotCss.remove(oldType);
							detailAnnotCss.add(annotation.getType());
						}
						public void annotationAttributeChanged(QueriableAnnotation doc, Annotation annotation, String attributeName, Object oldValue) {}
					};
					document.addAnnotationListener(al);
					
					//	apply image markup tool
					mt.process(document, annot, GamtaDocumentMarkupPanel.this, pm);
					
					//	make sure newly added annotation are visible
					for (Iterator datit = detailAnnotCss.iterator(); datit.hasNext();) {
						String dat = ((String) datit.next());
						Character dadm = getDetailAnnotationDisplayMode(dat);
						if (dadm == DISPLAY_MODE_INVISIBLE) {
							Character prefDadm = getPreferredDetailAnnotationShowingMode(dat);
							setDetailAnnotationDisplayMode(dat, ((prefDadm == null) ? DISPLAY_MODE_SHOW_HIGHLIGHTS : prefDadm), false, false); // no need to update UI within atomic action
						}
					}
				}
				
				//	catch whatever might happen
				catch (Throwable t) {
					t.printStackTrace(System.out);
					DialogFactory.alert(("Error applying " + mt.getLabel() + ":\n" + t.getMessage()), "Error Running DocumentProcessor", JOptionPane.ERROR_MESSAGE, null);
				}
				
				//	clean up
				finally {
					
					//	stop listening
					if (al != null)
						document.removeAnnotationListener(al);
					
					//	finish atomic UNDO (unless handled externally), also updates display
					if (handleAtomicAction)
						finishAtomicAction(pm);
					
					//	make changes show for externally managed atomic action
					/* we need to do repainting on Swing EDT, as otherwise we
					 * might incur a deadlock between this thread and EDT on
					 * synchronized parts of UI or data structures */
					else SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							validateDocumentPanel(false, false);
							validateControlPanel();
							validate();
							repaint();
						}
					});
					
					//	dispose splash screen progress monitor
					if (pmw != null)
						pmw.close();
				}
			}
		};
		mtThread.start();
		
		//	open splash screen progress monitor (this waits)
		if (pmw != null)
			pmw.popUp(true);
	}
	
	/**
	 * Mark as invalid (and thus force refresh of) the rendering of the
	 * document displaying in the panel. Subclassed that overwrite the
	 * <code>getMutableDocument()</code> method to return anything else than
	 * the actual document displaying in this panel can use this method to
	 * ensure the rednering of the document updates properly.
	 */
	public void invalidateDocumentPanel() {
		this.detailAnnotModCount++; // emulate any sort of modification
	}
	
	/**
	 * Refresh the display of the contained document.
	 */
	public void validateDocumentPanel() {
		this.validateDocumentPanel(true, true);
	}
	void validateDocumentPanel(boolean retainCaretPosition, boolean retainSelection) {
		this.recordStableViewContentPanels(retainCaretPosition, retainSelection);
		if ((this.validDetailAnnotModCount != this.detailAnnotModCount) || (this.validTokenModCount != this.tokenModCount))
			this.layoutContentFull(); // TODO track changes from atomic actions in more detail (adding single annotation won't require full refesh !!!)
		else if (this.validDetailAnnotTagModCount != this.detailAnnotTagModCount)
			this.refreshAnnotStartTags(); // TODO track changes from atomic actions in more detail (adding single annotation won't require full refesh !!!)
		this.restoreStableViewContentPanels();
	}
	
	/**
	 * Mark as invalid (and thus force refresh of) any display control panel
	 * retrieved from the <code>getControlPanel()</code> method. Subclassed
	 * that overwrite the <code>getMutableDocument()</code> method to return
	 * anything else than the actual document displaying in this panel can use
	 * this method to ensure the control panel updates properly.
	 */
	public void invalidateControlPanel() {
		this.gdvcDocModCount++; // emulate any sort of modification
	}
	
	/**
	 * Refresh any display control panel retrieved from the
	 * <code>getControlPanel()</code> method. Client code that starts an atomic
	 * action before modifying the document should call this method after the
	 * atomic action has been finished.
	 */
	public void validateControlPanel() {
		if (this.gdvc == null)
			return;
		if (this.validXdvcDocModCount == this.gdvcDocModCount)
			return;
		this.gdvc.updateControls();
		this.validXdvcDocModCount = this.gdvcDocModCount;
	}
	
	/**
	 * Retrieve a control panel allowing to configure the display.
	 * @return the control panel
	 */
	public DocumentViewControl getControlPanel() {
		if (this.gdvc == null)
			this.gdvc = new DocumentViewControl(this);
		else this.gdvc.updateControls();
		return this.gdvc;
	}
	DocumentViewControl gdvc = null;
	private boolean immediatelyUpdateXdvc = true;
	private int gdvcDocModCount = 0;
	private int validXdvcDocModCount = 0;
	
	/**
	 * Configuration widget for XML viewer panel.
	 * 
	 * @author sautter
	 */
	public class DocumentViewControl extends JPanel {
		final GamtaDocumentMarkupPanel gdmp;
		private JLabel label = new JLabel("Display Control", JLabel.CENTER);
		private JPanel controlPanel = new JPanel(new GridBagLayout(), true);
		private JScrollPane controlPanelBox;
//		private JLabel structAnnotLabel = new JLabel("Structural Annotations", JLabel.CENTER);
//		private JButton showStructAnnotsButton = new JButton("Show All");
//		private JButton hideStructAnnotsButton = new JButton("Hide All");
//		private JPanel structAnnotButtons = new JPanel(new GridLayout(1, 2), true);
//		private TreeMap structAnnotControls = new TreeMap(String.CASE_INSENSITIVE_ORDER);
//		private StructAnnotControl paraControl;
		private JLabel detailAnnotLabel = new JLabel("Detail Annotations", JLabel.CENTER);
		private JButton showDetailAnnotsButton = new JButton("Show All");
		private JButton hideDetailAnnotsButton = new JButton("Hide All");
		private JPanel detailAnnotButtons = new JPanel(new GridLayout(1, 2), true);
		private TreeMap detailAnnotControls = new TreeMap(String.CASE_INSENSITIVE_ORDER);
		
		/*
TODO maybe add same namespace prefix based annotation type list collapsing/expanding to XM document markup panel as there is in GG editor display panel

TODO XM document markup panel display control:
- keep index of annotation panels by type to simplify forwarding changes ...
- ... likely using dedicated mini array list
==> same lists and indexes of panels might well be helpful with scrolling some element to visible portion, too

TODO ALSO, add some debug flags to switch off (currently excessive) logging
		 */
		
		DocumentViewControl(GamtaDocumentMarkupPanel gdmp) {
			super(new BorderLayout(), true);
			this.gdmp = gdmp;
			this.add(this.label, BorderLayout.NORTH);
			this.controlPanelBox = new JScrollPane(this.controlPanel);
			this.controlPanelBox.getVerticalScrollBar().setUnitIncrement(33);
			this.controlPanelBox.getVerticalScrollBar().setBlockIncrement(100);
			this.controlPanelBox.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent ce) {
					adjustScrollBar();
				}
			});
			this.add(this.controlPanelBox, BorderLayout.CENTER);
			
//			this.paraControl = new StructAnnotControl(Annotation.PARAGRAPH_TYPE);;
//			
//			this.showStructAnnotsButton.setBorder(BorderFactory.createEtchedBorder());
//			this.showStructAnnotsButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent ae) {
//					boolean allStructAnnotTypesVisible = true;
//					for (Iterator cit = structAnnotControls.keySet().iterator(); cit.hasNext();)
//						if (getStructuralAnnotationDisplayMode((String) cit.next()) == DISPLAY_MODE_INVISIBLE) {
//							allStructAnnotTypesVisible = false;
//							break;
//						}
//					if (allStructAnnotTypesVisible)
//						return;
//					for (Iterator cit = structAnnotControls.keySet().iterator(); cit.hasNext();) {
//						StructAnnotControl sac = ((StructAnnotControl) structAnnotControls.get(cit.next()));
//						if (sac.mode == DISPLAY_MODE_INVISIBLE) {
//							sac.updateDisplayMode(DISPLAY_MODE_SHOW_TAGS);
//							setStructuralAnnotationDisplayMode(sac.type, DISPLAY_MODE_SHOW_TAGS, false, false);
//						}
//					}
//					if (paraControl.mode == DISPLAY_MODE_INVISIBLE) {
//						paraControl.updateDisplayMode(DISPLAY_MODE_SHOW_TAGS);
//						setStructuralAnnotationDisplayMode(Annotation.PARAGRAPH_TYPE, DISPLAY_MODE_SHOW_TAGS, false, false);
//					}
//					validateDocumentPanel();
//				}
//			});
//			this.hideStructAnnotsButton.setBorder(BorderFactory.createEtchedBorder());
//			this.hideStructAnnotsButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent ae) {
//					boolean allStructAnnotTypesHidden = true;
//					for (Iterator cit = structAnnotControls.keySet().iterator(); cit.hasNext();)
//						if (getStructuralAnnotationDisplayMode((String) cit.next()) != DISPLAY_MODE_INVISIBLE) {
//							allStructAnnotTypesHidden = false;
//							break;
//						}
//					if (allStructAnnotTypesHidden)
//						return;
//					for (Iterator cit = structAnnotControls.keySet().iterator(); cit.hasNext();) {
//						StructAnnotControl sac = ((StructAnnotControl) structAnnotControls.get(cit.next()));
//						if (sac.mode != DISPLAY_MODE_INVISIBLE) {
//							sac.updateDisplayMode(DISPLAY_MODE_INVISIBLE);
//							setStructuralAnnotationDisplayMode(sac.type, DISPLAY_MODE_INVISIBLE, false, false);
//						}
//					}
//					if (paraControl.mode != DISPLAY_MODE_INVISIBLE) {
//						paraControl.updateDisplayMode(DISPLAY_MODE_INVISIBLE);
//						setStructuralAnnotationDisplayMode(Annotation.PARAGRAPH_TYPE, DISPLAY_MODE_INVISIBLE, false, false);
//					}
//					validateDocumentPanel();
//				}
//			});
//			this.structAnnotButtons.add(this.showStructAnnotsButton);
//			this.structAnnotButtons.add(this.hideStructAnnotsButton);
			this.showDetailAnnotsButton.setBorder(BorderFactory.createEtchedBorder());
			this.showDetailAnnotsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					boolean allDetailAnnotTypesVisible = true;
					for (Iterator cit = detailAnnotControls.keySet().iterator(); cit.hasNext();)
						if (getDetailAnnotationDisplayMode((String) cit.next()) == DISPLAY_MODE_INVISIBLE) {
							allDetailAnnotTypesVisible = false;
							break;
						}
					if (allDetailAnnotTypesVisible)
						return;
					for (Iterator cit = detailAnnotControls.keySet().iterator(); cit.hasNext();) {
						AnnotControl ac = ((AnnotControl) detailAnnotControls.get(cit.next()));
						if (ac.mode == DISPLAY_MODE_INVISIBLE) {
							Character prefDisplayMode = getPreferredDetailAnnotationShowingMode(ac.type);
							if (prefDisplayMode == null)
								prefDisplayMode = DISPLAY_MODE_SHOW_HIGHLIGHTS;
							ac.updateDisplayMode(prefDisplayMode);
							setDetailAnnotationDisplayMode(ac.type, prefDisplayMode, false, false);
						}
					}
					validateDocumentPanel(true, true);
				}
			});
			this.hideDetailAnnotsButton.setBorder(BorderFactory.createEtchedBorder());
			this.hideDetailAnnotsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					boolean allDetailAnnotTypesHidden = true;
					for (Iterator cit = detailAnnotControls.keySet().iterator(); cit.hasNext();)
						if (getDetailAnnotationDisplayMode((String) cit.next()) != DISPLAY_MODE_INVISIBLE) {
							allDetailAnnotTypesHidden = false;
							break;
						}
					if (allDetailAnnotTypesHidden)
						return;
					for (Iterator cit = detailAnnotControls.keySet().iterator(); cit.hasNext();) {
						AnnotControl ac = ((AnnotControl) detailAnnotControls.get(cit.next()));
						if (ac.mode != DISPLAY_MODE_INVISIBLE) {
							ac.updateDisplayMode(DISPLAY_MODE_INVISIBLE);
							setDetailAnnotationDisplayMode(ac.type, DISPLAY_MODE_INVISIBLE, false, false);
						}
					}
					validateDocumentPanel(true, true);
				}
			});
			this.detailAnnotButtons.add(this.showDetailAnnotsButton);
			this.detailAnnotButtons.add(this.hideDetailAnnotsButton);
			
			this.updateControls();
		}
		
		synchronized void updateControls() {
			HashMap tempControls = new HashMap();
			
//			tempControls.clear();
//			tempControls.putAll(this.structAnnotControls);
//			this.structAnnotControls.clear();
//			String[] structAnnotTypes = this.gdmp.document.getAnnotationTypes(false);
//			for (int t = 0; t < structAnnotTypes.length; t++) {
//				StructAnnotControl sac = ((StructAnnotControl) tempControls.get(structAnnotTypes[t]));
//				if (sac == null)
//					sac = new StructAnnotControl(structAnnotTypes[t]);
//				else sac.updateDisplayMode(getStructuralAnnotationDisplayMode(structAnnotTypes[t]));
//				this.structAnnotControls.put(sac.type, sac);
//			}
//			
			tempControls.clear();
			tempControls.putAll(this.detailAnnotControls);
			this.detailAnnotControls.clear();
			String[] allAnnotTypes = this.gdmp.document.getAnnotationTypes();
			System.out.println("GAMTA document panel: markup updating display control with " + allAnnotTypes.length + " types: " + Arrays.asList(allAnnotTypes));
			for (int t = 0; t < allAnnotTypes.length; t++) {
				AnnotControl ac = ((AnnotControl) tempControls.get(allAnnotTypes[t]));
				if (ac == null)
					ac = new AnnotControl(allAnnotTypes[t]);
				else ac.updateDisplayMode(getDetailAnnotationDisplayMode(allAnnotTypes[t]));
				this.detailAnnotControls.put(ac.type, ac);
			}
			
			this.layoutControls();
		}
		
		void layoutControls() {
			this.controlPanel.removeAll();
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridheight = 1;
			gbc.weighty = 0;
			gbc.gridy = 0;
			gbc.insets.left = 4;
			gbc.insets.right = 4;
			gbc.insets.top = 2;
			gbc.insets.bottom = 2;
			gbc.fill = GridBagConstraints.BOTH;
//			
//			gbc.gridwidth = 3;
//			gbc.gridx = 0;
//			gbc.weightx = 1;
//			this.controlPanel.add(this.structAnnotLabel, gbc.clone());
//			gbc.gridy++;
//			this.showStructAnnotsButton.setEnabled(this.structAnnotControls.size() != 0);
//			this.hideStructAnnotsButton.setEnabled(this.structAnnotControls.size() != 0);
//			this.controlPanel.add(this.structAnnotButtons, gbc.clone());
//			gbc.gridy++;
//			for (Iterator cit = this.structAnnotControls.keySet().iterator(); cit.hasNext();) {
//				StructAnnotControl sac = ((StructAnnotControl) this.structAnnotControls.get(cit.next()));
//				gbc.gridwidth = 1;
//				gbc.gridx = 0;
//				gbc.weightx = 0;
//				this.controlPanel.add(sac.showTags, gbc.clone());
//				gbc.gridx = 1;
//				gbc.weightx = 0;
//				this.controlPanel.add(sac.foldContent, gbc.clone());
//				gbc.gridx = 2;
//				gbc.weightx = 1;
//				this.controlPanel.add(sac.label, gbc.clone());
//				gbc.gridy++;
//			}
//			
//			gbc.gridwidth = 1;
//			gbc.gridx = 0;
//			gbc.weightx = 0;
//			this.controlPanel.add(this.paraControl.showTags, gbc.clone());
//			gbc.gridx = 1;
//			gbc.weightx = 0;
//			this.controlPanel.add(this.paraControl.foldContent, gbc.clone());
//			gbc.gridx = 2;
//			gbc.weightx = 1;
//			this.controlPanel.add(this.paraControl.label, gbc.clone());
//			gbc.gridy++;
			
			gbc.gridwidth = 3;
			gbc.gridx = 0;
			gbc.weightx = 1;
			this.controlPanel.add(this.detailAnnotLabel, gbc.clone());
			gbc.gridy++;
			this.showDetailAnnotsButton.setEnabled(this.detailAnnotControls.size() != 0);
			this.hideDetailAnnotsButton.setEnabled(this.detailAnnotControls.size() != 0);
			this.controlPanel.add(this.detailAnnotButtons, gbc.clone());
			gbc.gridy++;
			for (Iterator cit = this.detailAnnotControls.keySet().iterator(); cit.hasNext();) {
				AnnotControl ac = ((AnnotControl) this.detailAnnotControls.get(cit.next()));
				gbc.gridwidth = 1;
				gbc.gridx = 0;
				gbc.weightx = 0;
				this.controlPanel.add(ac.showTags, gbc.clone());
				gbc.gridx = 1;
				gbc.weightx = 0;
				this.controlPanel.add(ac.highlightValues, gbc.clone());
				gbc.gridx = 2;
				gbc.weightx = 1;
				this.controlPanel.add(ac.label, gbc.clone());
				gbc.gridy++;
			}
			
			gbc.gridwidth = 2;
			gbc.gridx = 0;
			gbc.weightx = 1;
			gbc.weighty = 1;
			this.controlPanel.add(new JPanel(), gbc.clone());
			
			this.adjustScrollBar();
			
			this.validate();
			this.repaint();
		}
		
		void adjustScrollBar() {
			Dimension cpSize = this.controlPanelBox.getViewport().getView().getSize();
			Dimension cpViewSize = this.controlPanelBox.getViewport().getExtentSize();
			if (cpSize.height <= cpViewSize.height)
				this.controlPanelBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			else {
				this.controlPanelBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				this.controlPanelBox.getVerticalScrollBar().setUnitIncrement(cpViewSize.height / 10);
				this.controlPanelBox.getVerticalScrollBar().setBlockIncrement(cpViewSize.height / 3);
			}
		}
		
		void setAnnotColor(String type, Color color) {
//			if (Annotation.PARAGRAPH_TYPE.equals(type))
//				this.paraControl.setColor(color);
//			else {
//				StructAnnotControl sac = ((StructAnnotControl) this.structAnnotControls.get(type));
//				if (sac != null)
//					sac.setColor(color);
				AnnotControl ac = ((AnnotControl) this.detailAnnotControls.get(type));
				if (ac != null)
					ac.setColor(color);
//			}
		}
//		void setStructAnnotDisplayMode(String type, Character mode) {
//			if (Annotation.PARAGRAPH_TYPE.equals(type))
//				this.paraControl.updateDisplayMode(mode);
//			else {
//				StructAnnotControl sac = ((StructAnnotControl) this.structAnnotControls.get(type));
//				if (sac != null)
//					sac.updateDisplayMode(mode);
//			}
//		}
		void setDetailAnnotDisplayMode(String type, Character mode) {
			AnnotControl ac = ((AnnotControl) this.detailAnnotControls.get(type));
			if (ac != null)
				ac.updateDisplayMode(mode);
		}
		
		void updateLayoutSettings(Font font, Color fc, Color bc) {
			this.updateLayoutSettings(this, font, fc, bc);
			this.updateLayoutSettings(this.label, font, fc, bc);
			this.updateLayoutSettings(this.controlPanel, font, fc, bc);
			this.updateLayoutSettings(this.controlPanelBox, font, fc, bc);
			this.updateLayoutSettings(this.controlPanelBox.getHorizontalScrollBar(), font, fc, bc);
			this.updateLayoutSettings(this.controlPanelBox.getVerticalScrollBar(), font, fc, bc);
//			this.updateLayoutSettings(this.structAnnotLabel, font, fc, bc);
//			this.updateLayoutSettings(this.structAnnotButtons, font, fc, bc);
//			this.updateLayoutSettings(this.showStructAnnotsButton, font, fc, bc);
//			this.updateLayoutSettings(this.hideStructAnnotsButton, font, fc, bc);
//			for (Iterator atit = this.structAnnotControls.keySet().iterator(); atit.hasNext();) {
//				StructAnnotControl sac = ((StructAnnotControl) this.structAnnotControls.get((String) atit.next()));
//				this.updateLayoutSettings(sac.label, font, fc, null /* need to leave background indicating annotation color */);
//				this.updateLayoutSettings(sac.showTags, font, fc, bc);
//				this.updateLayoutSettings(sac.foldContent, font, fc, bc);
//			}
//			this.updateLayoutSettings(this.paraControl.label, font, fc, null /* need to leave background indicating annotation color */);
//			this.updateLayoutSettings(this.paraControl.showTags, font, fc, bc);
//			this.updateLayoutSettings(this.paraControl.foldContent, font, fc, bc);
			this.updateLayoutSettings(this.detailAnnotLabel, font, fc, bc);
			this.updateLayoutSettings(this.detailAnnotButtons, font, fc, bc);
			this.updateLayoutSettings(this.showDetailAnnotsButton, font, fc, bc);
			this.updateLayoutSettings(this.hideDetailAnnotsButton, font, fc, bc);
			for (Iterator atit = this.detailAnnotControls.keySet().iterator(); atit.hasNext();) {
				AnnotControl ac = ((AnnotControl) this.detailAnnotControls.get((String) atit.next()));
				this.updateLayoutSettings(ac.label, font, fc, null /* need to leave background indicating annotation color */);
				this.updateLayoutSettings(ac.showTags, font, fc, bc);
				this.updateLayoutSettings(ac.highlightValues, font, fc, bc);
			}
			this.validate();
			this.repaint();
		}
		private void updateLayoutSettings(Component comp, Font font, Color fc, Color bc) {
			if (comp == null)
				return;
			if (font != null)
				comp.setFont(font);
			if (fc != null)
				comp.setForeground(fc);
			if (bc != null)
				comp.setBackground(bc);
		}
		
		class AnnotControl {
			String type;
			Color color;
			Character mode;
			JCheckBox showTags = new JCheckBox(((String) null), false);
			JCheckBox highlightValues = new JCheckBox(((String) null), false);
			JButton label = new JButton() {
				public void paint(Graphics gr) {
					Color preColor = gr.getColor();
					
					//	paint background (need to be non-opaque and then draw background ourselves to facilitate mixing colors)
					Dimension size = this.getSize();
					gr.setColor(gdmp.tagBackgroundColor);
					gr.fillRect(0, 0, size.width, size.height);
					gr.setColor(this.getBackground());
					gr.fillRect(0, 0, size.width, size.height);
					
					//	paint text
					gr.setColor(preColor);
					super.paint(gr);
				}
				public Color getForeground() {
					return gdmp.tagTextColor; // need to use tag text for label, as default text color hard to read on dark background
				}
//				public void setBackground(Color bg) {
//					super.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0x40));
//				}
			};
			AnnotControl(String type) {
				this.type = type;
				this.mode = gdmp.getDetailAnnotationDisplayMode(type);
				this.color = gdmp.getAnnotationColor(type, true);
				
				Character dm = getDetailAnnotationDisplayMode(type);
				this.showTags.setSelected(dm == DISPLAY_MODE_SHOW_TAGS);
				this.highlightValues.setSelected(dm == DISPLAY_MODE_SHOW_HIGHLIGHTS);
				this.showTags.setToolTipText("Show tags for detail annotations of type '" + type + "'?");
				this.showTags.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						updateDisplayMode(false);
					}
				});
				this.highlightValues.setToolTipText("Highlight values of detail annotations of type '" + type + "'?");
				this.highlightValues.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						updateDisplayMode(true);
					}
				});
				
				this.label.setText(type);
//				this.label.setOpaque(true);
//				this.label.setBackground(this.color);
				this.label.setBackground(gdmp.getAnnotationHighlightColor(type, true));
				this.label.setBorder(BorderFactory.createLineBorder(this.color, 2));
				this.label.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						Color color = JColorChooser.showDialog(AnnotControl.this.label, AnnotControl.this.type, AnnotControl.this.color);
						if (color != null) {
							AnnotControl.this.color = color;
//							AnnotControl.this.label.setBackground(color);
//							AnnotControl.this.label.setBorder(BorderFactory.createLineBorder(color, 2));
							AnnotControl.this.colorChanged(color);
							AnnotControl.this.label.setBackground(gdmp.getAnnotationHighlightColor(AnnotControl.this.type, true));
							AnnotControl.this.label.setBorder(BorderFactory.createLineBorder(color, 2));
						}
					}
				});
			}
			void setColor(Color color) {
				this.color = color;
//				this.label.setBackground(color);
				this.label.setBackground(gdmp.getAnnotationHighlightColor(this.type, true));
				this.label.setBorder(BorderFactory.createLineBorder(this.color, 2));
			}
			void colorChanged(Color color) {
				gdmp.setAnnotationColor(this.type, color);
			}
			void updateDisplayMode(boolean highlightChanged) {
				Character mode;
				
				//	update comes from value highlighting
				if (highlightChanged) {
					if (this.highlightValues.isSelected()) {
						this.showTags.setSelected(false); // showing highlights deactivates tags
						mode = DISPLAY_MODE_SHOW_HIGHLIGHTS;
					}
					else mode = (this.showTags.isSelected() ? DISPLAY_MODE_SHOW_TAGS : DISPLAY_MODE_INVISIBLE);
				}
				
				//	update comes from tag toggle
				else {
					if (this.showTags.isSelected()) {
						this.highlightValues.setSelected(false); // showing tags deactivates highlights
						mode = DISPLAY_MODE_SHOW_TAGS;
					}
					else mode = (this.highlightValues.isSelected() ? DISPLAY_MODE_SHOW_HIGHLIGHTS : DISPLAY_MODE_INVISIBLE);
				}
				
				//	nothing changed (also catches second event when changing second checkbox internally)
				if (mode == this.mode)
					return;
				
				//	store and forward update
				this.mode = mode;
				gdmp.setDetailAnnotationDisplayMode(this.type, mode, false, true);
			}
			void updateDisplayMode(Character mode) {
				if (mode == DISPLAY_MODE_SHOW_TAGS) {
					this.mode = mode; // makes sure event notification doesn't propagate
					this.showTags.setSelected(true); // will automatically de-select highlights if latter showing
				}
				else if (mode == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
					this.mode = mode; // makes sure event notification doesn't propagate
					this.highlightValues.setSelected(true); // will automatically de-select tags if latter showing
				}
				else if (mode == DISPLAY_MODE_INVISIBLE) {
					this.mode = mode; // makes sure event notification doesn't propagate
					if (this.highlightValues.isSelected()) // implies tags not showing
						this.highlightValues.setSelected(false);
					else if (this.showTags.isSelected()) // implies highlights not showing
						this.showTags.setSelected(false);
				}
			}
		}
//		class StructAnnotControl extends AnnotControl {
//			JCheckBox showTags = new JCheckBox(((String) null), false);
//			JCheckBox foldContent = new JCheckBox(((String) null), false);
//			StructAnnotControl(String type) {
//				super(type, gdmp.getStructuralAnnotationDisplayMode(type), gdmp.getAnnotationColor(type, true));
//				Character dm = getStructuralAnnotationDisplayMode(type);
//				this.showTags.setSelected(dm != DISPLAY_MODE_INVISIBLE);
//				this.foldContent.setSelected(dm == DISPLAY_MODE_FOLD_CONTENT);
//				this.showTags.setToolTipText("Show tags for structal annotations of type '" + type + "'?");
//				this.showTags.addItemListener(new ItemListener() {
//					public void itemStateChanged(ItemEvent ie) {
//						updateDisplayMode(false);
//					}
//				});
//				this.foldContent.setToolTipText("Fold content of structal annotations of type '" + type + "'?");
//				this.foldContent.addItemListener(new ItemListener() {
//					public void itemStateChanged(ItemEvent ie) {
//						updateDisplayMode(true);
//					}
//				});
//			}
//			void updateDisplayMode(boolean foldChanged) {
//				Character mode;
//				
//				//	update comes from content folding
//				if (foldChanged) {
//					if (this.foldContent.isSelected()) {
//						this.showTags.setSelected(true); // folding content implies tags visible
//						mode = DISPLAY_MODE_FOLD_CONTENT;
//					}
//					else mode = (this.showTags.isSelected() ? DISPLAY_MODE_SHOW_TAGS : DISPLAY_MODE_INVISIBLE);
//				}
//				
//				//	update comes from tag toggle
//				else {
//					if (this.showTags.isSelected())
//						mode = (this.foldContent.isSelected() ? DISPLAY_MODE_FOLD_CONTENT : DISPLAY_MODE_SHOW_TAGS);
//					else {
//						this.foldContent.setSelected(false); // cannot fold content without tags showing
//						mode = DISPLAY_MODE_INVISIBLE;
//					}
//				}
//				
//				//	nothing changed (also catches second event when changing second checkbox internally)
//				if (mode == this.mode)
//					return;
//				
//				//	store and forward update
//				this.mode = mode;
//				gdmp.setStructuralAnnotationDisplayMode(this.type, mode, false, true);
//			}
//			void updateDisplayMode(Character mode) {
//				if (mode == DISPLAY_MODE_SHOW_TAGS) {
//					this.mode = mode; // makes sure event notification doesn't propagate
//					if (this.foldContent.isSelected()) // implies showing tags is selected as well
//						this.foldContent.setSelected(false);
//					else this.showTags.setSelected(true);
//				}
//				else if (mode == DISPLAY_MODE_FOLD_CONTENT) {
//					this.mode = mode; // makes sure event notification doesn't propagate
//					this.foldContent.setSelected(true); // will also show tags
//				}
//				else if (mode == DISPLAY_MODE_INVISIBLE) {
//					this.mode = mode; // makes sure event notification doesn't propagate
//					this.showTags.setSelected(false); // will also clear deactivate folding
//				}
//			}
//		}
//		class DetailAnnotControl extends AnnotControl {
//			JCheckBox showTags = new JCheckBox(((String) null), false);
//			JCheckBox highlightValues = new JCheckBox(((String) null), false);
//			DetailAnnotControl(String type) {
//				super(type, gdmp.getDetailAnnotationDisplayMode(type), gdmp.getAnnotationColor(type, true));
//				Character dm = getDetailAnnotationDisplayMode(type);
//				this.showTags.setSelected(dm == DISPLAY_MODE_SHOW_TAGS);
//				this.highlightValues.setSelected(dm == DISPLAY_MODE_SHOW_HIGHLIGHTS);
//				this.showTags.setToolTipText("Show tags for detail annotations of type '" + type + "'?");
//				this.showTags.addItemListener(new ItemListener() {
//					public void itemStateChanged(ItemEvent ie) {
//						updateDisplayMode(false);
//					}
//				});
//				this.highlightValues.setToolTipText("Highlight values of detail annotations of type '" + type + "'?");
//				this.highlightValues.addItemListener(new ItemListener() {
//					public void itemStateChanged(ItemEvent ie) {
//						updateDisplayMode(true);
//					}
//				});
//			}
//			void updateDisplayMode(boolean highlightChanged) {
//				Character mode;
//				
//				//	update comes from value highlighting
//				if (highlightChanged) {
//					if (this.highlightValues.isSelected()) {
//						this.showTags.setSelected(false); // showing highlights deactivates tags
//						mode = DISPLAY_MODE_SHOW_HIGHLIGHTS;
//					}
//					else mode = (this.showTags.isSelected() ? DISPLAY_MODE_SHOW_TAGS : DISPLAY_MODE_INVISIBLE);
//				}
//				
//				//	update comes from tag toggle
//				else {
//					if (this.showTags.isSelected()) {
//						this.highlightValues.setSelected(false); // showing tags deactivates highlights
//						mode = DISPLAY_MODE_SHOW_TAGS;
//					}
//					else mode = (this.highlightValues.isSelected() ? DISPLAY_MODE_SHOW_HIGHLIGHTS : DISPLAY_MODE_INVISIBLE);
//				}
//				
//				//	nothing changed (also catches second event when changing second checkbox internally)
//				if (mode == this.mode)
//					return;
//				
//				//	store and forward update
//				this.mode = mode;
//				gdmp.setDetailAnnotationDisplayMode(this.type, mode, false, true);
//			}
//			void updateDisplayMode(Character mode) {
//				if (mode == DISPLAY_MODE_SHOW_TAGS) {
//					this.mode = mode; // makes sure event notification doesn't propagate
//					this.showTags.setSelected(true); // will automatically de-select highlights if latter showing
//				}
//				else if (mode == DISPLAY_MODE_SHOW_HIGHLIGHTS) {
//					this.mode = mode; // makes sure event notification doesn't propagate
//					this.highlightValues.setSelected(true); // will automatically de-select tags if latter showing
//				}
//				else if (mode == DISPLAY_MODE_INVISIBLE) {
//					this.mode = mode; // makes sure event notification doesn't propagate
//					if (this.highlightValues.isSelected()) // implies tags not showing
//						this.highlightValues.setSelected(false);
//					else if (this.showTags.isSelected()) // implies highlights not showing
//						this.showTags.setSelected(false);
//				}
//			}
//		}
	}
	
	/**
	 * Retrieve a panel with controls for configuring the general appearance of
	 * the markup panel, i.e., fonts and general foreground and background
	 * colors, and scroll stabilization on refreshes. The returned panel is for
	 * more long-term configuration than the display control panel,
	 * complementing the latter. Regardless, panels retrieved from this method
	 * are not intended for permanent integration in a user interface, but
	 * rather for being part of a configuration dialog or the like.
	 * @return a panel for configuring the general appearance of the markup
	 *            panel
	 */
	public ViewConfigurationPanel getConfigurationPanel() {
		return new ViewConfigurationPanel(this);
	}
	
	/**
	 * Retrieve a viewport specialized for displaying the document markup panel
	 * in a <code>JScrollPanel</code>. This is the preferred method of wrapping
	 * a <code>JScrollPanel</code> around an instance of this class, as the
	 * returned viewport interacts closely with the display panel proper for a
	 * more user friendly scrolling behavior.
	 * @return the viewport
	 */
	public DocumentViewport getViewport() {
		if (this.xdvp == null)
			this.xdvp = new DocumentViewport(this);
		return this.xdvp;
	}
	DocumentViewport xdvp = null;
	
	static final boolean DEBUG_CONTENT_RENDERING = false;
	static final boolean DEBUG_RENDERING_DETAILS = (DEBUG_CONTENT_RENDERING && false);
	static final boolean DEBUG_KEYSTROKE_HANDLING = false;
	static final boolean DEBUG_SELECTION_HANDLING = false;
	static final boolean DEBUG_SCROLL_STABILIZATION = false;
	static final boolean DEBUG_SCROLL_ADJUSTMENT = false;
	static final boolean DEBUG_SCROLL_POSITIONING = false;
	
	/**
	 * A specialized <code>JViewport</code> for showing an XM document markup
	 * panels in a <code>JScrollPane</code>. Instances of this class can be
	 * retrieved from the <code>getViewport()</code> method.
	 * 
	 * @author sautter
	 */
	public static class DocumentViewport extends JViewport implements TwoClickActionMessenger {
		private static Color halfTransparentRed = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
		private GamtaDocumentMarkupPanel gdmp;
		private String tcaMessage = null;
		DocumentViewport(GamtaDocumentMarkupPanel gdmp) {
			this.gdmp = gdmp;
			this.gdmp.setTwoClickActionMessenger(this);
			this.setView(this.gdmp);
			this.setOpaque(false);
			this.addComponentListener(new ComponentAdapter() {
//				public void componentResized(ComponentEvent ce) {
//					moveToCenterComponent();
//				}
				public void componentResized(ComponentEvent ce) {
					if (DEBUG_SCROLL_STABILIZATION) {
						System.out.println("Viewport resized to " + getSize());
						System.out.println("  document panel at " + DocumentViewport.this.gdmp.getSize());
					}
					restoreStableViewPanel();
				}
//				public void componentMoved(ComponentEvent ce) {
//					System.out.println("Viewport moved");
//				}
//				public void componentShown(ComponentEvent ce) {
//					System.out.println("Viewport shown");
//				}
//				public void componentHidden(ComponentEvent ce) {
//					System.out.println("Viewport hidden");
//				}
			});
		}
		
		//	fields and method overwrites enabling distinction between when and when not to update panel at view center
		public void setViewPosition(Point vp) {
			if (this.settingViewPositionInternal) {
//				(new Exception("Ignoring callback setting view position to " + vp)).printStackTrace(System.out);
			}
			else if ((this.setViewPosition != null) && this.setViewPosition.equals(vp)) {
//				(new Exception("Ignoring idempotent setting of view position to " + vp)).printStackTrace(System.out);
			}
			else /* we're actually scrolling due to user action, and thus need to record content position */ {
				if (DEBUG_SCROLL_STABILIZATION) System.out.println("Setting view position to " + vp);
				this.recordStableViewPanel(false);
			}
			this.setViewPositionInternal(vp);
		}
		private boolean settingViewPositionInternal = false;
		private Point setViewPosition = null;
		private void setViewPositionInternal(Point vp) {
			try {
				this.settingViewPositionInternal = true;
				super.setViewPosition(vp);
				this.setViewPosition = vp;
				this.repaint();
			}
			finally {
				this.settingViewPositionInternal = false;
			}
		}
//		
//		//	THIS IS THE SIZE OF THE VIEWPORT PROPER
//		public void setSize(Dimension size) {
//			System.out.println("Setting viewport size to " + size);
//			super.setSize(size);
//		}
//		//	THIS IS THE ACTUAL SIZE OF THE VISIBLE PART OF DOCUMENT PANEL
//		public void setExtentSize(Dimension vs) {
//			System.out.println("Setting view size to " + vs);
//			super.setExtentSize(vs);
//		}
//		//	THIS IS THE SIZE OF THE CONTAINED PANEL
//		public void setViewSize(Dimension vs) {
//			System.out.println("Setting view size to " + vs);
//			super.setViewSize(vs);
//		}
		
		//	methods for retrieving and and adjusting view position of specific document panel elements
		Rectangle getViewPositionOf(JPanel comp) {
			Point cp = comp.getLocation();
			for (Container pComp = comp.getParent(); pComp != null; pComp = pComp.getParent()) {
				if (pComp == this.gdmp) {
					Point vp = this.getViewPosition();
					cp.x -= vp.x;
					cp.y -= vp.y;
					return new Rectangle(cp.x, cp.y, comp.getWidth(), comp.getHeight());
				}
				Point pcp = pComp.getLocation();
				cp.x += pcp.x;
				cp.y += pcp.y;
			}
			return null;
		}
		void moveToViewPosition(final JPanel tComp, final Rectangle tPos, final DisplayAdjustmentObserver dao) {
			this.validate();
			this.repaint();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doMoveToViewPosition(tComp, tPos, (1 - gdmp.viewStabilizationLevel), dao); // TODO scrutinize this approach
				}
			});
		}
		void doMoveToViewPosition(final JPanel tComp, final Rectangle tPos, final int maxDev, final DisplayAdjustmentObserver dao) {
			if (DEBUG_SCROLL_ADJUSTMENT) System.out.println("Moving " + tComp + " to target position " + tPos);
			Rectangle cPos = this.getViewPositionOf(tComp);
			if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" - current position is " + cPos);
			if (cPos == null)
				return; // nothing to work with
			if (cPos.y < tPos.y) {
				Point vp = this.getViewPosition();
				if (DEBUG_SCROLL_ADJUSTMENT) {
					System.out.println(" - view position is " + vp);
					System.out.println(" - moving view up by " + (tPos.y - cPos.y));
				}
				vp.y -= (tPos.y - cPos.y);
				if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" - target view position is " + vp);
				if (vp.y < 0)
					vp.y = 0;
				if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" - setting view position to " + vp);
				this.setViewPositionInternal(vp); // TODOne need to use internal here ??? ==> yes, so we only record stable panel once view actually in desired position
			}
			else if (tPos.y < cPos.y) {
				Point vp = this.getViewPosition();
				if (DEBUG_SCROLL_ADJUSTMENT) {
					System.out.println(" - view position is " + vp);
					System.out.println(" - moving view down by " + (cPos.y - tPos.y));
				}
				vp.y += (cPos.y - tPos.y);
				if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" - target view position is " + vp);
//				Dimension vs = this.getExtentSize();
//				Dimension ps = this.gdmp.getSize();
//				if (ps.height < (vp.y + vs.height))
//					vp.y = (ps.height - vs.height);
				if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" - setting view position to " + vp);
				this.setViewPositionInternal(vp); // TODOne need to use internal here ??? ==> yes, so we only record stable panel once view actually in desired position
			}
			this.validate();
			this.repaint();
			if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> final view position is " + this.getViewPosition());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> post-event view position is " + getViewPosition());
					Dimension vs = getExtentSize();
					if (maxDev < (vs.height / 2)) /* anything higher would be useless anyway */ {
						Rectangle cPos = getViewPositionOf(tComp);
						if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> target positioned at " + cPos);
						Dimension ts = tComp.getSize();
//						int nMaxDev = ((maxDev < 1) ? (maxDev + 1) : (maxDev * 2)); // TODOne maybe use factor 3 for fatsre convergence ???
						int nMaxDev = ((maxDev < 1) ? (maxDev + 1) : (maxDev * 3));
						if ((cPos.y + cPos.height) < 0) {
							if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> re-positioning for target out top, with max deviation " + nMaxDev);
							doMoveToViewPosition(tComp, tPos, nMaxDev, dao);
						}
						else if (vs.height < cPos.y) {
							if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> re-positioning for target out bottom, with max deviation " + nMaxDev);
							doMoveToViewPosition(tComp, tPos, nMaxDev, dao);
						}
						else if (vs.height <= ts.height) {// too large to fit view
							recordStableViewPanel(false);
							if (dao != null)
								dao.displayAdjustmentFinished(true);
						}
						else if ((0 <= tPos.y) && (cPos.y < 0)) {
							if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> re-positioning for top edge violation, with max deviation " + nMaxDev);
							doMoveToViewPosition(tComp, tPos, nMaxDev, dao);
						}
						else if ((0 <= cPos.y) && (tPos.y < 0)) {
							if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> re-positioning for top edge violation, with max deviation " + nMaxDev);
							doMoveToViewPosition(tComp, tPos, nMaxDev, dao);
						}
						else if (Math.max(maxDev, 0) < Math.abs(cPos.y - tPos.y)) {
							if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> re-positioning for top deviation of " + Math.abs(cPos.y - tPos.y) + ", with max deviation " + nMaxDev);
							doMoveToViewPosition(tComp, tPos, nMaxDev, dao);
						}
						else {
							recordStableViewPanel(false);
							if (dao != null)
								dao.displayAdjustmentFinished(true);
						}
					}
					else {
						if (DEBUG_SCROLL_ADJUSTMENT) System.out.println(" ==> target positioned at " + getViewPositionOf(tComp));
						recordStableViewPanel(false);
						if (dao != null)
							dao.displayAdjustmentFinished(false);
					}
				}
			});
		}
		
		//	method for moving specific document elements into view via API call
		void moveIntoView(JPanel comp, Rectangle inCompPos, DisplayAdjustmentObserver dao) {
			this.moveIntoView(comp, inCompPos, comp, inCompPos, dao);
		}
		void moveIntoView(JPanel topComp, Rectangle inTopCompPos, JPanel bottomComp, Rectangle inBottomCompPos, DisplayAdjustmentObserver dao) {
			if (DEBUG_SCROLL_POSITIONING) System.out.println("Moving panels into view:");
			if (DEBUG_SCROLL_POSITIONING) System.out.println(" - " + topComp);
			Rectangle tcPos = this.getViewPositionOf(topComp);
			if (DEBUG_SCROLL_POSITIONING) System.out.println("   at " + tcPos);
			Rectangle itcPos = null;
			if (inTopCompPos != null) {
				itcPos = new Rectangle(inTopCompPos);
				itcPos.x += tcPos.x;
				itcPos.y += tcPos.y;
				if (DEBUG_SCROLL_POSITIONING) System.out.println("   target at " + itcPos);
			}
			if (DEBUG_SCROLL_POSITIONING) System.out.println(" - " + bottomComp);
			Rectangle bcPos = ((topComp == bottomComp) ? tcPos : this.getViewPositionOf(bottomComp));
			if (DEBUG_SCROLL_POSITIONING) System.out.println("   at " + bcPos);
			Rectangle ibcPos = null;
			if (inBottomCompPos != null) {
				ibcPos = new Rectangle(inBottomCompPos);
				ibcPos.x += bcPos.x;
				ibcPos.y += bcPos.y;
				if (DEBUG_SCROLL_POSITIONING) System.out.println("   target at " + ibcPos);
			}
			Rectangle cPos = tcPos.union(bcPos);
			if (DEBUG_SCROLL_POSITIONING) System.out.println(" - combined view position is " + cPos);
			Rectangle vPos = this.getViewRect();
			if ((0 <= cPos.y) && ((cPos.y + cPos.height) <= vPos.height)) {
				if (DEBUG_SCROLL_POSITIONING) System.out.println(" ==> within visible area");
				return; // both panels already in visible area
			}
			Rectangle icPos = null;
			if ((itcPos != null) && (ibcPos != null)) {
				icPos = itcPos.union(ibcPos);
				if (DEBUG_SCROLL_POSITIONING) System.out.println(" - combined target view position is " + icPos);
			}
			if ((icPos != null) && (0 <= icPos.y) && ((icPos.y + icPos.height) <= vPos.height)) {
				if (DEBUG_SCROLL_POSITIONING) System.out.println(" ==> target within visible area");
				return; // both in-panel targets already in visible area
			}
			int ty;
			if (icPos == null) /* we're positioning panels proper */ {
				if (cPos.height <= vPos.height)
					ty = ((vPos.height - cPos.height) / 2); // both panels fit in visible area, put in middle
				else ty = 0; // rectangle around panels too large to fit visible area, put top panel at top of visible area to show as much as possible
			}
			else /* we're positioning in-panel targets */ {
				if (icPos.height <= vPos.height)
					ty = ((vPos.height - icPos.height) / 2); // both in-panel targets fit in visible area, put in middle
				else ty = 0; // rectangle around in-panel targets too large to fit visible area, put top in-panel target at top of visible area to show as much as possible
				ty -= inTopCompPos.y; // we're still positioning panel proper below, need to adjust upwards by in-panel position to actually center in-panel targets
			}
			Rectangle tPos = new Rectangle(cPos.x, ty, cPos.width, topComp.getHeight());
			if (DEBUG_SCROLL_POSITIONING) System.out.println(" ==> target position computed as " + tPos);
			this.moveToViewPosition(topComp, tPos, dao);
		}
		
		//	fields and methods for keeping view content stable as line wrapping changes document panel height on width changes
		boolean recordStableViewPanelEnqueued = false;
		boolean restoreStableViewPanelEnqueued = false;
		JPanel stableViewPanel = null;
		Rectangle stableViewPanelPosition = null;
		void recordStableViewPanel(boolean immediately) {
			if (immediately) {
				this.doRecordStableViewPanel();
				return;
			}
			if (this.recordStableViewPanelEnqueued)
				return;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						stableViewPanel = null;
						stableViewPanelPosition = null;
						doRecordStableViewPanel();
					}
					finally {
						recordStableViewPanelEnqueued = false;
					}
				}
			});
			this.recordStableViewPanelEnqueued = true;
		}
		void doRecordStableViewPanel() {
			Rectangle vr = this.getViewRect();
			if (vr.height < 1)
				return; // nothing to work with at all ...
			int sx = (vr.x + (vr.width / 2));
//			int sy = (vr.y + (vr.height / 2));
			int sy = (vr.y + ((vr.height * this.gdmp.viewStabilizationHeight) / 100));
//			int dy = -10; // in case of of a miss, seek 10 pixels upwards first, then 10 pixels down, 20 pixels up, etc.
			int[] sys = new int[(vr.height + 9) / 10];
			sys[0] = sy;
			int syi = 1;
			for (int d = 1;; d++) {
				if (((d * 2) - 1 + 0) < sys.length) {
					if (this.gdmp.viewStabilizationHeight < 50)
						sys[(d * 2) - 1 + 0] = sy + ((10 * d * (100 - this.gdmp.viewStabilizationHeight)) / 50); // stable height in top half, look below it first
					else if (this.gdmp.viewStabilizationHeight == 50)
						sys[(d * 2) - 1 + 0] = sy - (10 * d); // stable height in middle, look above it first
					else sys[(d * 2) - 1 + 0] = sy - ((10 * d * this.gdmp.viewStabilizationHeight) / 50); // stable height in bottom half, look above it first
				}
				else break;
				if (((d * 2) - 1 + 1) < sys.length) {
					if (this.gdmp.viewStabilizationHeight < 50)
						sys[(d * 2) - 1 + 1] = sy - ((10 * d * this.gdmp.viewStabilizationHeight) / 50); // stable height in top half, look above it second
					else if (this.gdmp.viewStabilizationHeight == 50)
						sys[(d * 2) - 1 + 1] = sy + (10 * d); // stable height in middle, look below it second
					else sys[(d * 2) - 1 + 1] = sy + ((10 * d * (100 - this.gdmp.viewStabilizationHeight)) / 50); // stable height in bottom half, look below it second
				}
				else break;
			}
			if (DEBUG_SCROLL_STABILIZATION) System.out.println("Stable panel search heights from " + sy + " (at " + this.gdmp.viewStabilizationHeight + "): " + Arrays.toString(sys));
			while (this.stableViewPanel == null) {
				int x = sx;
				int y = sy;
				Component comp = this.gdmp.getComponentAt(x, y);
				if (DEBUG_SCROLL_STABILIZATION) System.out.println("Component at " + x + "/" + y + " in " + vr + " is " + comp);
				while (comp != null) {
					if (comp instanceof AnnotTagPanel) {
						this.stableViewPanel = ((JPanel) comp);
						break;
					}
					else if (comp instanceof AnnotTagConnectorPanel) {
						this.stableViewPanel = ((JPanel) comp);
						break;
					}
					else if (comp instanceof TokenMarkupPanel) {
						this.stableViewPanel = ((JPanel) comp);
						break;
					}
					Point cp = comp.getLocation();
					x -= (cp.x / 2);
					y -= cp.y;
					Component cComp = comp.getComponentAt(x, y);
					if (cComp == comp)
						break;
					comp = cComp;
					if (DEBUG_SCROLL_STABILIZATION) System.out.println(" - descended to " + cComp + " at " + cp);
				}
				if (DEBUG_SCROLL_STABILIZATION) System.out.println("Component at " + sx + "/" + sy + " in " + vr + " is " + comp);
				
				//	found nothing useful, seek a 10 pixels further up or down
				if (this.stableViewPanel == null) {
//					sy += dy;
//					/* Increasing absolute step size by 10 each time and inverting
//					 * the sign yields (sy-10), (sy+10), (sy-20), (sy+20), etc.,
//					 * i.e., stepping outwards in 10 pixel steps while alternating
//					 * between above vertical center and below vertical center. */
//					if (dy < 0)
//						dy = (10 - dy);
//					else dy = (-10 - dy);
//					if (sy < vr.y)
//						break; // too far up to make any sense
//					if ((vr.y + vr.height) < sy)
//						break; // too far down to make any sense
					if (syi < sys.length)
						sy = sys[syi++];
					else break;
				}
				
				//	record position of panel at center of view, and we're done
				else {
					this.stableViewPanelPosition = this.getViewPositionOf(this.stableViewPanel);
					if (DEBUG_SCROLL_STABILIZATION) System.out.println("  position is " + this.stableViewPanelPosition);
				}
			}
		}
		void restoreStableViewPanel() {
			if (this.stableViewPanel == null)
				return;
			if (this.stableViewPanelPosition == null)
				return;
			if (this.restoreStableViewPanelEnqueued)
				return;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						//	we should have been validated and repainted at this point, call position update directly
						doMoveToViewPosition(stableViewPanel, stableViewPanelPosition, (1 - gdmp.viewStabilizationLevel), dummyDisplayAdjustmentObserver);
					}
					finally {
						restoreStableViewPanelEnqueued = false;
					}
				}
			});
			this.restoreStableViewPanelEnqueued = true;
		}
		
		public void twoClickActionChanged(TwoClickSelectionAction tcsa) {
			this.tcaMessage = ((tcsa == null) ? null : tcsa.getActiveLabel());
			this.validate();
			this.repaint();
		}
		
		void updateLayoutSettings(Font font, Color fc, Color bc) {
			this.setFont(font);
			this.setForeground(fc);
			this.setBackground(bc);
			this.validate();
			this.repaint();
			Container pComp = this.getParent();
			pComp.setFont(font);
			pComp.setForeground(fc);
			pComp.setBackground(bc);
			if (pComp instanceof JScrollPane) {
				JScrollBar hBar = ((JScrollPane) pComp).getHorizontalScrollBar();
				if (hBar != null) {
					hBar.setFont(font);
					hBar.setForeground(fc);
					hBar.setBackground(bc);
				}
				JScrollBar vBar = ((JScrollPane) pComp).getVerticalScrollBar();
				if (vBar != null) {
					vBar.setFont(font);
					vBar.setForeground(fc);
					vBar.setBackground(bc);
				}
			}
			pComp.validate();
			pComp.repaint();
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			if (this.tcaMessage == null)
				return;
			Font f = new Font("SansSerif", Font.PLAIN, 20);
			g.setFont(f);
			TextLayout wtl = new TextLayout(this.tcaMessage, f, ((Graphics2D) g).getFontRenderContext());
			g.setColor(halfTransparentRed);
			g.fillRect(0, 0, this.getViewRect().width, ((int) Math.ceil(wtl.getBounds().getHeight() + (wtl.getDescent() * 3))));
			g.setColor(Color.white);
			((Graphics2D) g).drawString(this.tcaMessage, ((this.getViewRect().width - wtl.getAdvance()) / 2), ((int) Math.ceil(wtl.getBounds().getHeight() + wtl.getDescent())));
		}
	}
	
	public static void mainKeyEventTest(String[] args) throws Exception {
//	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		JTextArea textArea = new JTextArea();
		textArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
					ke.consume();
					return;
				}
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					ke.consume();
					return;
				}
				if (ke.getKeyCode() == KeyEvent.VK_ALT) {
					ke.consume();
					return;
				}
				System.out.println("Key pressed: " + ke.getKeyChar() + " - 0x" + Integer.toString(((int) ke.getKeyChar()), 16) + " - code " + ke.getKeyCode() + " - 0x" + Integer.toString(ke.getKeyCode(), 16));
				System.out.println("- shift is " + ke.isShiftDown());
				System.out.println("- ctrl is " + ke.isControlDown());
				System.out.println("- alt is " + ke.isAltDown());
				System.out.println("- alt-gr is " + ke.isAltGraphDown());
				/* keys executing on 'pressed' event:
				 * - backspace, delete
				 * - return, tab
				 * - arrow keys
				 * - page up, page down
				 * - home, end
				 */
				if (ke.getKeyChar() == KeyEvent.VK_BACK_SPACE)
					ke.consume();
				if (ke.getKeyChar() == KeyEvent.VK_DELETE)
					ke.consume();
			}
			public void keyReleased(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
					ke.consume();
					return;
				}
				if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
					ke.consume();
					return;
				}
				if (ke.getKeyCode() == KeyEvent.VK_ALT) {
					ke.consume();
					return;
				}
				System.out.println("Key released: " + ke.getKeyChar() + " - 0x" + Integer.toString(((int) ke.getKeyChar()), 16) + " - code " + ke.getKeyCode() + " - 0x" + Integer.toString(ke.getKeyCode(), 16));
				System.out.println("- shift is " + ke.isShiftDown());
				System.out.println("- ctrl is " + ke.isControlDown());
				System.out.println("- alt is " + ke.isAltDown());
				System.out.println("- alt-gr is " + ke.isAltGraphDown());
//				if (ke.getKeyChar() == KeyEvent.VK_BACK_SPACE)
//					ke.consume();
			}
			public void keyTyped(KeyEvent ke) {
				System.out.println("Key typed: " + ke.getKeyChar() + " - 0x" + Integer.toString(((int) ke.getKeyChar()), 16) + " - code " + ke.getKeyCode() + " - 0x" + Integer.toString(ke.getKeyCode(), 16));
				System.out.println("- shift is " + ke.isShiftDown());
				System.out.println("- ctrl is " + ke.isControlDown());
				System.out.println("- alt is " + ke.isAltDown());
				System.out.println("- alt-gr is " + ke.isAltGraphDown());
//				if (ke.getKeyChar() == 0x0008 /* backspace */)
					ke.consume();
			}
		});
		final JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(textArea, BorderLayout.CENTER);
		f.setSize(600, 400);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setVisible(true);
			}
		});
	}
	
	//	FOR TEST PURPOSES ONLY !!!
//	public static void mainDisplayTest(String[] args) throws Exception {
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		long time = System.currentTimeMillis();
		
		File docDataPath = new File("E:/Projektdaten/22777.xml"); // GG XML test
//		File docDataPath = new File("E:/Projektdaten/ZooKeys/513-6565-1-PB.xml"); // raw TaxPub test
//		
//		String docName;
//		
//		//	only use documents we have an XMF version of right now
//		docName = "23311.textDocument.xml"; // born-digital, renders perfectly fine
		
		//	load document
		MutableAnnotation doc = SgmlDocumentReader.readDocument(docDataPath);
		System.out.println("Got document with " + doc.size() + " after " + (System.currentTimeMillis() - time) + "ms");
		Annotation[] paras = doc.getAnnotations(MutableAnnotation.PARAGRAPH_TYPE);
		for (int p = 0; p < paras.length; p++)
			paras[p].lastToken().setAttribute(Token.PARAGRAPH_END_ATTRIBUTE);
		
		//	show XM document
		final JFrame f = new JFrame();
		final Object[] storedObject = {null};
		final JLabel storedObjectLabel = new JLabel("<nothing stored>");
		final GamtaDocumentMarkupPanel gdmp = new GamtaDocumentMarkupPanel(doc, true, false) {
			protected SelectionAction[] getActions(final PointSelection ps, MouseEvent me) {
				SelectionAction[] sas = {null};
				if (ps.type == PointSelection.POINT_TYPE_TOKEN)
					sas[0] = new SelectionAction("storeObject", "Store Token") {
						public boolean performAction(GamtaDocumentMarkupPanel invoker) {
							Token mt = ps.getToken();
							storedObject[0] = mt;
							storedObjectLabel.setText("Token '" + mt.getValue() + "'");
							return false;
						}
					};
				if ((ps.type == PointSelection.POINT_TYPE_ANNOTATION_START_CAP) || ps.type == PointSelection.POINT_TYPE_ANNOTATION_END_CAP)
					sas[0] = new SelectionAction("storeObject", "Store Annotation (HEC)") {
						public boolean performAction(GamtaDocumentMarkupPanel invoker) {
							Annotation xma = ps.getAnnotation();
							storedObject[0] = xma;
							storedObjectLabel.setText("Annotation '" + AnnotationUtils.produceStartTag(xma, false) + "'");
							return false;
						}
					};
				if ((ps.type == PointSelection.POINT_TYPE_ANNOTATION_START_TAG) || ps.type == PointSelection.POINT_TYPE_ANNOTATION_END_TAG)
					sas[0] = new SelectionAction("storeObject", "Store Annotation (T)") {
						public boolean performAction(GamtaDocumentMarkupPanel invoker) {
							Annotation xma = ps.getAnnotation();
							storedObject[0] = xma;
							storedObjectLabel.setText("Annotation '" + AnnotationUtils.produceStartTag(xma, false) + "'");
							return false;
						}
					};
				if (ps.type == PointSelection.POINT_TYPE_ANNOTATION_TAG_CONNECTOR)
					sas[0] = new SelectionAction("storeObject", "Store Annotation (TC)") {
						public boolean performAction(GamtaDocumentMarkupPanel invoker) {
							Annotation xma = ps.getAnnotation();
							storedObject[0] = xma;
							storedObjectLabel.setText("Annotation '" + AnnotationUtils.produceStartTag(xma, false) + "'");
							return false;
						}
					};
				return ((sas[0] == null) ? super.getActions(ps, me) : sas);
			}
		};
		System.out.println("Panel created after " + (System.currentTimeMillis() - time) + "ms");
		gdmp.layoutContentFull();
		System.out.println("Panel layout redone after " + (System.currentTimeMillis() - time) + "ms");
		JScrollPane pbox = new JScrollPane();
		pbox.getVerticalScrollBar().setUnitIncrement(50);
		pbox.getVerticalScrollBar().setBlockIncrement(50);
		final DocumentViewport vp = gdmp.getViewport();
		pbox.setViewport(vp);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(pbox, BorderLayout.CENTER);
		f.getContentPane().add(gdmp.getControlPanel(), BorderLayout.EAST);
//		JPanel fontPanel = new JPanel(new GridLayout(0, 1), true);
//		fontPanel.add(new FontPanel(p, "Token Text", p.getTokenTextFont(), p.getTokenTextColor(), p.getTokenBackgroundColor(), p.getSelectedTokenTextColor(), p.getSelectedTokenBackgroundColor(), true) {
//			void applySettings() {
//				this.gdmp.setTokenTextColor(this.textColor, false);
//				Color tbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
//				this.gdmp.setTokenBackgroundColor(tbc, false);
//				this.gdmp.setSelectedTokenTextColor(this.selTextColor, false);
//				Color stbc = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0x55);
//				this.gdmp.setSelectedTokenBackgroundColor(stbc, false);
//				this.gdmp.setTokenTextFont(this.font, true);
//			}
//		});
////		fontPanel.add(new FontPanel(p, "Selected Token Text", false, p.getSelectedTokenTextColor(), p.getSelectedTokenBackgroundColor()) {
////			void textFontChanged(Font font) {}
////			void textColorChanged() {
////				this.gdmp.setSelectedTokenTextColor(this.textColor);
////			}
////			void backgroundColorChanged() {
////				Color stbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0x80);
////				this.gdmp.setSelectedTokenBackgroundColor(stbc);
////			}
////		});
//		fontPanel.add(new FontPanel(p, "Annotation Tag Text", p.getTagTextFont(), p.getTagTextColor(), p.getTagBackgroundColor(), p.getSelectedTagTextColor(), p.getSelectedTagBackgroundColor(), true) {
//			void applySettings() {
//				this.gdmp.setTagTextColor(this.textColor, false);
//				Color tbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
//				this.gdmp.setTagBackgroundColor(tbc, false);
//				this.gdmp.setSelectedTagTextColor(this.selTextColor, false);
//				Color stbc = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0x55);
//				this.gdmp.setSelectedTagBackgroundColor(stbc, false);
//				this.gdmp.setTagTextFont(this.font, true);
//			}
//		});
////		fontPanel.add(new FontPanel(p, "Selected Annotation Tag Text", false, p.getSelectedTagTextColor(), p.getSelectedTagBackgroundColor()) {
////			void textFontChanged(Font font) {}
////			void textColorChanged() {
////				this.gdmp.setSelectedTagTextColor(this.textColor);
////			}
////			void backgroundColorChanged() {
////				Color stbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0x55);
////				this.gdmp.setSelectedTagBackgroundColor(stbc);
////			}
////		});
//		f.getContentPane().add(fontPanel, BorderLayout.SOUTH);
		JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER), true);
		JButton adjust = new JButton("Adjust Display");
		adjust.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showSettingsPanel(f, gdmp);
			}
		});
		bp.add(adjust);
		JButton select = new JButton("Show Stored Object");
		select.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (storedObject[0] == null)
					return;
				gdmp.setSelectedObject(storedObject[0], true, new DisplayAdjustmentObserver() {
					public void displayAdjustmentFinished(boolean success) {
						storedObject[0] = null;
						storedObjectLabel.setText("<nothing stored (" + (success ? "S" : "F") + ")>");
					}
				});
			}
		});
		bp.add(storedObjectLabel);
		bp.add(select);
		f.getContentPane().add(bp, BorderLayout.SOUTH);
		f.setSize(1200, 1000);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setVisible(true);
			}
		});
		System.out.println("Frame opened after " + (System.currentTimeMillis() - time) + "ms");
	}
	
	/**
	 * Panel with controls for configuring the general appearance of a document
	 * markup panel, i.e., fonts and general foreground and background colors,
	 * and scroll stabilization on refreshes, complementing the display control
	 * panel.
	 * 
	 * @author sautter
	 */
	public static class ViewConfigurationPanel extends JPanel {
		private GamtaDocumentMarkupPanel gdmp;
		private FontPanel tokenText;
		private FontPanel tagText;
		private JSlider ahAlpha;
		private JSlider vsHeight;
		private JSlider vsLevel;
		ViewConfigurationPanel(GamtaDocumentMarkupPanel gdmp) {
			super(new GridLayout(0, 1), true);
			this.gdmp = gdmp;
			
			this.tokenText = new FontPanel(this.gdmp, "Token Text", this.gdmp.getTokenTextFont(), this.gdmp.getTokenTextColor(), this.gdmp.getTokenBackgroundColor(), this.gdmp.getSelectedTokenTextColor(), this.gdmp.getSelectedTokenBackgroundColor(), false) {
				void applySettings() {
					this.gdmp.setTokenTextColor(this.textColor, false);
					Color tbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
					this.gdmp.setTokenBackgroundColor(tbc, false);
					this.gdmp.setSelectedTokenTextColor(this.selTextColor, false);
					Color stbc = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0x55);
					this.gdmp.setSelectedTokenBackgroundColor(stbc, false);
					this.gdmp.setTokenTextFont(this.font, true);
				}
			};
			
			this.tagText = new FontPanel(this.gdmp, "Annotation Tag Text", this.gdmp.getTagTextFont(), this.gdmp.getTagTextColor(), this.gdmp.getTagBackgroundColor(), this.gdmp.getSelectedTagTextColor(), this.gdmp.getSelectedTagBackgroundColor(), false) {
				void applySettings() {
					this.gdmp.setTagTextColor(this.textColor, false);
					Color tbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
					this.gdmp.setTagBackgroundColor(tbc, false);
					this.gdmp.setSelectedTagTextColor(this.selTextColor, false);
					Color stbc = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0x55);
					this.gdmp.setSelectedTagBackgroundColor(stbc, false);
					this.gdmp.setTagTextFont(this.font, true);
				}
			};
			
			this.ahAlpha = new JSlider(0, 0xFF, this.gdmp.getAnnotationHighlightAlpha());
			this.ahAlpha.setMajorTickSpacing(10);
			this.ahAlpha.setPaintLabels(true);
			this.ahAlpha.setPaintTicks(true);
			this.ahAlpha.setPaintTrack(true);
			
			JPanel ahaPanel = new JPanel(new BorderLayout(), true);
			ahaPanel.add(new JLabel("Alpha Value for Annotation Highlights and Tag Backgrounds"), BorderLayout.WEST);
			ahaPanel.add(this.ahAlpha, BorderLayout.CENTER);
			
			this.vsHeight = new JSlider(0, 100, this.gdmp.getViewStabilizationHeight());
			this.vsHeight.setMajorTickSpacing(10);
			this.vsHeight.setPaintLabels(true);
			this.vsHeight.setPaintTicks(true);
			this.vsHeight.setPaintTrack(true);
			this.vsLevel = new JSlider(0, 10, this.gdmp.getViewStabilizationLevel());
			this.vsLevel.setMajorTickSpacing(1);
			this.vsLevel.setPaintLabels(true);
			this.vsLevel.setPaintTicks(true);
			this.vsLevel.setPaintTrack(true);
			
			JPanel vsPanel = new JPanel(new GridLayout(1, 0), true);
			vsPanel.add(new JLabel("Height of Stabilized Part of In-View Document"));
			vsPanel.add(this.vsHeight);
			vsPanel.add(new JLabel("Level of Stabilization for In-View Part of Document"));
			vsPanel.add(this.vsLevel);
			
			this.add(this.tagText);
			this.add(this.tokenText);
			this.add(ahaPanel);
			this.add(vsPanel);
		}
		
		public Object getDisplayProperty(String name) {
			if (name == null)
				return null;
			if (name.startsWith("tag."))
				return this.tagText.getDisplayProperty(name.substring("tag.".length()));
			else if (name.startsWith("token."))
				return this.tokenText.getDisplayProperty(name.substring("token.".length()));
			else if ("annot.highlightAlpha".equals(name))
				return new Integer(this.ahAlpha.getValue());
			else if ("view.relativeStableHeight".equals(name))
				return new Integer(this.vsHeight.getValue());
			else if ("view.stableHeightLevel".equals(name))
				return new Integer(this.vsLevel.getValue());
			else return null;
		}
		
		public void setDisplayProperty(String name, Object value) {
			if (name == null)
				return;
			if (name.startsWith("tag.")) {
				if (value == null)
					value = getDisplayPropertyDefault(name);
				this.tagText.setDisplayProperty(name.substring("tag.".length()), value);
			}
			else if (name.startsWith("token.")) {
				if (value == null)
					value = getDisplayPropertyDefault(name);
				this.tokenText.setDisplayProperty(name.substring("token.".length()), value);
			}
			else if ("annot.highlightAlpha".equals(name)) {
				if (value == null)
					value = new Integer(defaultAnnotHighlightAlpha);
				if (value instanceof Number)
					this.ahAlpha.setValue(((Number) value).intValue());
			}
			else if ("view.relativeStableHeight".equals(name)) {
				if (value == null)
					value = new Integer(defaultViewStabilizationHeight);
				if (value instanceof Number)
					this.vsHeight.setValue(((Number) value).intValue());
			}
			else if ("view.stableHeightLevel".equals(name)) {
				if (value == null)
					value = new Integer(defaultViewStabilizationLevel);
				if (value instanceof Number)
					this.vsLevel.setValue(((Number) value).intValue());
			}
		}
		
		/**
		 * Apply the currently selected values to the target markup panel.
		 */
		public void applySettings() {
			this.tokenText.applySettings();
			this.tagText.applySettings();
			int aha = this.ahAlpha.getValue();
			this.gdmp.setAnnotationHighlightAlpha(aha);
			int vsh = this.vsHeight.getValue();
			this.gdmp.setViewStabilizationHeight(vsh);
			int vsl = this.vsLevel.getValue();
			this.gdmp.setViewStabilizationLevel(vsl);
		}
	}
	
	static void showSettingsPanel(Window w, final GamtaDocumentMarkupPanel gdmp) {
		JPanel setPanel = new JPanel(new GridLayout(0, 1), true);
		FontPanel tokenText = new FontPanel(gdmp, "Token Text", gdmp.getTokenTextFont(), gdmp.getTokenTextColor(), gdmp.getTokenBackgroundColor(), gdmp.getSelectedTokenTextColor(), gdmp.getSelectedTokenBackgroundColor(), false) {
			void applySettings() {
				this.gdmp.setTokenTextColor(this.textColor, false);
				Color tbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
				this.gdmp.setTokenBackgroundColor(tbc, false);
				this.gdmp.setSelectedTokenTextColor(this.selTextColor, false);
				Color stbc = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0x55);
				this.gdmp.setSelectedTokenBackgroundColor(stbc, false);
				this.gdmp.setTokenTextFont(this.font, true);
			}
		};
		setPanel.add(tokenText);
		FontPanel tagText = new FontPanel(gdmp, "Annotation Tag Text", gdmp.getTagTextFont(), gdmp.getTagTextColor(), gdmp.getTagBackgroundColor(), gdmp.getSelectedTagTextColor(), gdmp.getSelectedTagBackgroundColor(), false) {
			void applySettings() {
				this.gdmp.setTagTextColor(this.textColor, false);
				Color tbc = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
				this.gdmp.setTagBackgroundColor(tbc, false);
				this.gdmp.setSelectedTagTextColor(this.selTextColor, false);
				Color stbc = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0x55);
				this.gdmp.setSelectedTagBackgroundColor(stbc, false);
				this.gdmp.setTagTextFont(this.font, true);
			}
		};
		setPanel.add(tagText);
		JPanel vsPanel = new JPanel(new GridLayout(1, 0), true);
		vsPanel.add(new JLabel("Height of Stabilized Part of In-View Document"));
		JSlider vsHeight = new JSlider(0, 100, gdmp.getViewStabilizationHeight());
		vsHeight.setMajorTickSpacing(10);
		vsHeight.setPaintLabels(true);
		vsHeight.setPaintTicks(true);
		vsHeight.setPaintTrack(true);
		vsPanel.add(vsHeight);
		vsPanel.add(new JLabel("Level of Stabilization for In-View Part of Document"));
		JSlider vsLevel = new JSlider(0, 10, gdmp.getViewStabilizationLevel());
		vsLevel.setMajorTickSpacing(1);
		vsLevel.setPaintLabels(true);
		vsLevel.setPaintTicks(true);
		vsLevel.setPaintTrack(true);
		vsPanel.add(vsLevel);
		setPanel.add(vsPanel);
		int choice = JOptionPane.showConfirmDialog(w, setPanel, "Adjust Display Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return;
		tokenText.applySettings();
		tagText.applySettings();
		int vsh = vsHeight.getValue();
		gdmp.setViewStabilizationHeight(vsh);
		int vsl = vsLevel.getValue();
		gdmp.setViewStabilizationLevel(vsl);
	}
	
	private static abstract class FontPanel extends JPanel {
		private static String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		private static Integer[] fontSizes = {new Integer(8), new Integer(9), new Integer(10), new Integer(11), new Integer(12), new Integer(13), new Integer(14), new Integer(16), new Integer(18), new Integer(20), new Integer(22), new Integer(24), new Integer(28), new Integer(32), new Integer(36), new Integer(40), new Integer(44), new Integer(48)};
		Font font;
		JComboBox fontName = new JComboBox(fontNames);
		JComboBox fontSize = new JComboBox(fontSizes);
		JCheckBox bold = new JCheckBox("Bold", false);
		JCheckBox italics = new JCheckBox("Italics", false);
		Color textColor = Color.BLACK;
		JButton textLabel = new JButton("Normal Text");
		Color backgroundColor = Color.WHITE;
		JButton backgroundLabel = new JButton("Normal Background");
		Color selTextColor = Color.BLACK;
		JButton selTextLabel = new JButton("Selected Text");
		Color selBackgroundColor = Color.WHITE;
		JButton selBackgroundLabel = new JButton("Selected Background");
		GamtaDocumentMarkupPanel gdmp;
		JLabel label;
		JCheckBox applyChangesImmediately = new JCheckBox("Apply Changes Immediately?", true);
		FontPanel(GamtaDocumentMarkupPanel gdmp, final String label, Font font, Color textColor, Color backgroundColor, Color selTextColor, Color selBackgroundColor, boolean applyChangesImmediately) {
			super(new GridLayout(0, 5), true);
			this.gdmp = gdmp;
			this.label = new JLabel(label);
			this.font = font;
			this.textColor = ((textColor == null) ? Color.BLACK : textColor);
			this.backgroundColor = ((backgroundColor == null) ? Color.WHITE : backgroundColor);
			if (this.backgroundColor.getAlpha() < 0xFF)
				this.backgroundColor = new Color(this.backgroundColor.getRed(), this.backgroundColor.getGreen(), this.backgroundColor.getBlue(), 0xFF);
			this.selTextColor = ((selTextColor == null) ? Color.BLACK : selTextColor);
			this.selBackgroundColor = ((selBackgroundColor == null) ? Color.WHITE : selBackgroundColor);
			if (this.selBackgroundColor.getAlpha() < 0xFF)
				this.selBackgroundColor = new Color(this.selBackgroundColor.getRed(), this.selBackgroundColor.getGreen(), this.selBackgroundColor.getBlue(), 0xFF);
			this.applyChangesImmediately.setSelected(applyChangesImmediately);
			this.fontName.insertItemAt("SansSerif", 0);
			this.fontName.insertItemAt("Serif", 0);
			this.fontName.insertItemAt("Monospaced", 0);
			String fontName = this.font.getName();
			int fontNameIndex = -1;
			for (int f = 0; f < this.fontName.getItemCount(); f++)
				if (fontName.equals(this.fontName.getItemAt(f))) {
					fontNameIndex = f;
					break;
				}
			if (fontNameIndex == -1)
				this.fontName.setSelectedIndex(0);
			else this.fontName.setSelectedIndex(fontNameIndex);
			this.fontName.setEditable(false);
			this.fontName.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					fontChanged(true);
				}
			});
			this.fontSize.setSelectedItem(new Integer(this.font.getSize()));
			this.fontSize.setEditable(false);
			this.fontSize.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					fontChanged(true);
				}
			});
			this.bold.setSelected(this.font.isBold());
			this.bold.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					fontChanged(true);
				}
			});
			this.italics.setSelected(this.font.isItalic());
			this.italics.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					fontChanged(true);
				}
			});
			this.textLabel.setOpaque(true);
			this.textLabel.setBorder(BorderFactory.createLineBorder(this.textLabel.getBackground(), 2));
			if (textColor == null)
				this.textLabel.setEnabled(false);
			else {
				this.textLabel.setForeground(this.backgroundColor);
				this.textLabel.setBackground(this.textColor);
				this.textLabel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						Color color = JColorChooser.showDialog(FontPanel.this.textLabel, ("Letters of " + label), FontPanel.this.textColor);
						if (color != null) {
							FontPanel.this.textColor = color;
							FontPanel.this.textLabel.setBackground(color);
							FontPanel.this.backgroundLabel.setForeground(color);
//							FontPanel.this.textColorChanged();
							if (FontPanel.this.applyChangesImmediately.isSelected())
								FontPanel.this.applySettings();
						}
					}
				});
			}
			this.backgroundLabel.setOpaque(true);
			this.backgroundLabel.setBorder(BorderFactory.createLineBorder(this.backgroundLabel.getBackground(), 2));
			if (backgroundColor == null)
				this.backgroundLabel.setEnabled(false);
			else {
				this.backgroundLabel.setForeground(this.textColor);
				this.backgroundLabel.setBackground(this.backgroundColor);
				this.backgroundLabel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						Color color = JColorChooser.showDialog(FontPanel.this.backgroundLabel, ("Background of " + label), FontPanel.this.backgroundColor);
						if (color != null) {
							FontPanel.this.backgroundColor = color;
							FontPanel.this.backgroundLabel.setBackground(color);
							FontPanel.this.textLabel.setForeground(color);
							if (FontPanel.this.applyChangesImmediately.isSelected())
								FontPanel.this.applySettings();
						}
					}
				});
			}
			this.selTextLabel.setOpaque(true);
			this.selTextLabel.setBorder(BorderFactory.createLineBorder(this.selTextLabel.getBackground(), 2));
			if (textColor == null)
				this.selTextLabel.setEnabled(false);
			else {
				this.selTextLabel.setForeground(this.selBackgroundColor);
				this.selTextLabel.setBackground(this.selTextColor);
				this.selTextLabel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						Color color = JColorChooser.showDialog(FontPanel.this.selTextLabel, ("Letters of Selected " + label), FontPanel.this.textColor);
						if (color != null) {
							FontPanel.this.selTextColor = color;
							FontPanel.this.selTextLabel.setBackground(color);
							FontPanel.this.selBackgroundLabel.setForeground(color);
							if (FontPanel.this.applyChangesImmediately.isSelected())
								FontPanel.this.applySettings();
						}
					}
				});
			}
			this.selBackgroundLabel.setOpaque(true);
			this.selBackgroundLabel.setBorder(BorderFactory.createLineBorder(this.selBackgroundLabel.getBackground(), 2));
			if (backgroundColor == null)
				this.selBackgroundLabel.setEnabled(false);
			else {
				this.selBackgroundLabel.setForeground(this.selTextColor);
				this.selBackgroundLabel.setBackground(this.selBackgroundColor);
				this.selBackgroundLabel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						Color color = JColorChooser.showDialog(FontPanel.this.selBackgroundLabel, ("Background of Selected " + label), FontPanel.this.backgroundColor);
						if (color != null) {
							FontPanel.this.selBackgroundColor = color;
							FontPanel.this.selBackgroundLabel.setBackground(color);
							FontPanel.this.selTextLabel.setForeground(color);
							if (FontPanel.this.applyChangesImmediately.isSelected())
								FontPanel.this.applySettings();
						}
					}
				});
			}
			this.fontChanged(false);
			
			this.add(this.label);
			this.add(this.fontName);
			this.add(this.fontSize);
			this.add(this.bold);
			this.add(this.italics);
			this.add(this.applyChangesImmediately);
			this.add(this.textLabel);
			this.add(this.backgroundLabel);
			this.add(this.selTextLabel);
			this.add(this.selBackgroundLabel);
		}
		void fontChanged(boolean loopThrough) {
			String fontName = ((String) this.fontName.getSelectedItem());
			int fontSize = ((Integer) this.fontSize.getSelectedItem()).intValue();
			int fontStyle = Font.PLAIN;
			if (this.bold.isSelected())
				fontStyle |= Font.BOLD;
			if (this.italics.isSelected())
				fontStyle |= Font.ITALIC;
			this.font = new Font(fontName, fontStyle, fontSize);
			this.textLabel.setFont(this.font);
			this.backgroundLabel.setFont(this.font);
			this.selTextLabel.setFont(this.font);
			this.selBackgroundLabel.setFont(this.font);
			this.validate();
			this.repaint();
			if (loopThrough && this.applyChangesImmediately.isSelected())
				this.applySettings();
		}
		Object getDisplayProperty(String name) {
			if ("font".equals(name))
				return this.font;
			else if ("foreground".equals(name))
				return this.textColor;
			else if ("background".equals(name))
				return this.backgroundColor;
			else if ("selectedForeground".equals(name))
				return this.selTextColor;
			else if ("selectedBackground".equals(name))
				return this.selBackgroundColor;
			else return null;
		}
		void setDisplayProperty(String name, Object value) {
			if ("font".equals(name) && (value instanceof Font)) {
				Font font = ((Font) value);
				this.fontName.setSelectedItem(font.getFamily());
				this.fontSize.setSelectedItem(font.getSize());
				this.bold.setSelected((font.getStyle() & Font.BOLD) != 0);
				this.italics.setSelected((font.getStyle() & Font.ITALIC) != 0);
				this.font = font;
				this.textLabel.setFont(this.font);
				this.backgroundLabel.setFont(this.font);
				this.selTextLabel.setFont(this.font);
				this.selBackgroundLabel.setFont(this.font);
			}
			else if ("foreground".equals(name) && (value instanceof Color)) {
				Color color = ((Color) value);
				this.textColor = color;
				this.textLabel.setBackground(color);
				this.backgroundLabel.setForeground(color);
			}
			else if ("background".equals(name) && (value instanceof Color)) {
				Color color = ((Color) value);
				this.backgroundColor = color;
				this.backgroundLabel.setBackground(color);
				this.textLabel.setForeground(color);
			}
			else if ("selectedForeground".equals(name) && (value instanceof Color)) {
				Color color = ((Color) value);
				this.selTextColor = color;
				this.selTextLabel.setBackground(color);
				this.selBackgroundLabel.setForeground(color);
			}
			else if ("selectedBackground".equals(name) && (value instanceof Color)) {
				Color color = ((Color) value);
				this.selBackgroundColor = color;
				this.selBackgroundLabel.setBackground(color);
				this.selTextLabel.setForeground(color);
			}
		}
		abstract void applySettings();
		/*
TODO XM document markup panel font settings:
- also add 'tag color background percentage' property from [0,100] with default at (currently fixed '100 minus 25' value of) 75 ...
- ... and scale that to [0,255] alpha parameter applied in computing annotation highlight colors ...
- ... most likely offering separate properties for annotation tags and annotation highlights
- ALSO, provide import/export of display settings as text files named '<xyz>.skin.cnfg' ...
- ... just to STFU everyone who might ask about it to show off by insisting at my expense
  ==> do that from GGX proper, though ...
  ==> ... most likely exporting configured defaults ...
  ==> ... augmented with any non-default settings available from current document
		 */
	}
	/*
TODO GG application plug-in naming schemes:
- prefix anything GGI specific with 'Im'
- prefix anything GGX specific with ''
- use no prefix for GG core and GGE plug-ins
==> enforce scheme when migrating GGI to new GG core
==> observe scheme when copying GGI plug-ins for GGX
  ==> most likely only annotation actions, caption citations, and DP markup tools, anyway ...
  ==> ... and then at least also error manager, style provider, and style manager ...
  ==> ... even though latter already have 'ImageDocument...' names ...
  ==> ... and thus will clone nicely into 'lDocument...' plug-ins

TODO Managing GAMTA wrapper flags for applying IMT or XMT:
- bind wrapper flags to document display implementations ...
- ... allowing per-document adjustment ...
- ... with 'set as default' checkbox in adjustment dialog ...
- ... as well as 'apply to other open documents' checkbox
- provide 'Advanced ...' sub menu opening context menu option from DP markup tool providers ...
- ... allowing to apply whichever GG core DPs to whichever visible annotation ...
- ... using those GAMTA wrapper flags ...
- ... maybe restricted to single-tag selections and annotation highlight end cap clicks in GGX
==> make damn sure GGI XML view provider observes those flags ...
==> ... also (or at very least) in GG core based implementation
	 */
}
