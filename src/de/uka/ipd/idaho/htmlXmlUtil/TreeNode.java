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
package de.uka.ipd.idaho.htmlXmlUtil;


import java.io.IOException;
import java.util.ArrayList;

import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;


/**
 * A TreeNode represents a single node in the tree representation of HTML/XML
 * data. It is highly similar to a DOM element and fully compatible to the DOM
 * specification, despite some method naming differences.
 * 
 * @author sautter
 */
public class TreeNode {
	private static final Grammar defaultGrammar = new StandardGrammar();
	
	/**
	 * Type for synthetic root nodes automatically added to input data having no
	 * root node of its own, i.e. for input which is a sequence of HTML/XML
	 * documents rather than a single document
	 */
	public static final String ROOT_NODE_TYPE = "R_O_O_T";
	
	/**
	 * Type for nodes holding character data
	 */
	public static final String DATA_NODE_TYPE = "D_A_T_A";
	
	/**
	 * Type for nodes holding comments
	 */
	public static final String COMMENT_NODE_TYPE = "C_O_M_M_E_N_T";
	
	/**
	 * Type for nodes holding embedded DTD data
	 */
	public static final String DTD_NODE_TYPE = "D_T_D";
	
	/**
	 * Type for nodes holding embedded processing instructions
	 */
	public static final String PROCESSING_INSTRUCTION_NODE_TYPE = "I_N_S_T_R_U_C_T_I_O_N";
	
	/**
	 * Type for attribute nodes. This type is only used in DOM compatibility
	 * mode, otherwise a TreeNode returns its attributes as plain strings.
	 */
	public static final String ATTRIBUTE_NODE_TYPE = "A_T_T_R_I_B_U_T_E";
	
	private final String nodeType;
	private String nodeValue;
	private TreeNodeAttributeSet attributes;
	
	private TreeNode parentNode;
	private ArrayList childNodes = null;
	private int documentOrderPosition = -1;
	
	private boolean marked = false;
	
	/* TODO Add track of source string offsets to TreeNode:
- track start and end offsets for tag start and end tags ...
- ... setting end tag offsets to -1 in empty tags, textual content, comments, processing instructions, and DTD nodes
- add constructor accepting start tag offsets for use in parser ...
- ... and make at least end tag offsets package private to set on encountering end tag
- most likely include in tokens ...
- ... updating token source implementations accordingly
==> facilitates syntax highlighting in upcoming string data types
  ==> do same in executable ASTs of XPath, GPath, annotation patterns, GAMTA scripts, etc.

TODO ALSO, add "report errors" mode to parsing ...
- ... correcting errors where possible ...
- ... but still reporting them to facilitate visualization
  ==> most likely add handleError() method to TokenReceiver ...
  ==> ... defaulted to doing nothing to preserve current behavior

TODO ALSO, add extended signature of storeToken() to TokenReceiver ...
- ... adding token type, start and end offsets, etc. ...
- ... by default delegating to current storeToken() method
	 */
	
	/**	Fully custom constructor
	 * @param	parent		the new node's parent node
	 * @param	type		the new node's type
	 * @param	value		the new node's value, will be empty except if type is DATA_NODE_TYPE, COMMENT_NODE_TYPE or ATTRIBUTE_NODE_TYPE
	 * @param	attributes	the new node's attribute / value pairs, stored in a java.util.Properties
	 */
	public TreeNode(TreeNode parent, String type, TreeNodeAttributeSet attributes) {
		this.nodeType = type;
		this.parentNode = parent;
		this.attributes = attributes;
	}
	
	/**	Constructor leaving the new node's attributes empty
	 * @param	parent	the new node's parent node
	 * @param	type	the new node's type
	 * @param	value	the new node's value, will be empty except if type is DATA_NODE_TYPE, COMMENT_NODE_TYPE or ATTRIBUTE_NODE_TYPE
	 */
	public TreeNode(TreeNode parent, String type, String value) {
		this(parent, type, ((TreeNodeAttributeSet) null));
		this.nodeValue = ((DATA_NODE_TYPE.equals(type) || COMMENT_NODE_TYPE.equals(type) || ATTRIBUTE_NODE_TYPE.equals(type) || PROCESSING_INSTRUCTION_NODE_TYPE.equals(type) || DTD_NODE_TYPE.equals(type)) ? value : "");
	}
	
	/**	Constructor leaving the new node's value and attributes empty
	 * @param	parent	the new node's parent node
	 * @param	type	the new node's type
	 */
	public TreeNode(TreeNode parent, String type) {
		this(parent, type, "");
	}
	
	/**	add the specified node to this node's child nodes
	 */
	public void addChildNode(TreeNode child) {
		if (child == null)
			return;
		if (this.childNodes == null)
			this.childNodes = new ArrayList(2);
		this.childNodes.add(child);
	}
	
	/**	add the specified node to this node's child nodes at the specified index
	 */
	public void insertChildNode(int index, TreeNode child) {
		if (child == null)
			return;
		if (this.childNodes == null)
			this.childNodes = new ArrayList(2);
		this.childNodes.add(index, child);
	}
	
	/**	add the specified node to this node's child nodes right before the other child node
	 */
	public void insertChildNodeBefore(TreeNode beforeChild, TreeNode child) {
		if ((child == null) || (this.childNodes == null))
			return;
		if (this.childNodes == null) {
			this.addChildNode(child);
			return;
		}
		for (int c = 0; c < this.childNodes.size(); c++)
			if (this.childNodes.get(c) == beforeChild) {
				this.childNodes.add(c, child);
				return;
			}
		this.childNodes.add(child);
	}
	
	/**	add the specified node to this node's child nodes right after the other child node
	 */
	public void insertChildNodeAfter(TreeNode afterChild, TreeNode child) {
		if (child == null)
			return;
		if (this.childNodes == null) {
			this.addChildNode(child);
			return;
		}
		if (this.childNodes == null)
			this.childNodes = new ArrayList(2);
		for (int c = 0; c < this.childNodes.size(); c++)
			if (this.childNodes.get(c) == afterChild) {
				this.childNodes.add((c+1), child);
				return;
			}
		this.childNodes.add(child);
	}
	
	/**	remove the specified node from this node's children
	 */
	public void removeChildNode(TreeNode child) {
		if (this.childNodes == null)
			return;
		for (int c = 0; c < this.childNodes.size(); c++)
			if (this.childNodes.get(c) == child) {
				this.childNodes.remove(c);
				break;
			}
		if (this.childNodes.isEmpty())
			this.childNodes = null;
	}
	
	/**	remove the specified node from this node's children
	 */
	public void removeChildNode(int index) {
		if (this.childNodes != null)
			this.childNodes.remove(index);
	}
	
	/**	replace one of this node's child nodes with another node
	 * @param	oldCchild	the child node to be replaced
	 * @param	newChildChild	the node to replace the child node with
	 * 	Note: 	If the node to be replaced is NULL or is not a child of this node, 
	 * 			the new node is added to this nodes child nodes without removing another node
	 */
	public void replaceChildNode(TreeNode oldChild, TreeNode newChild) {
		if ((this.childNodes == null) || (oldChild == null)) {
			this.addChildNode(newChild);
			return;
		}
		for (int c = 0; c < this.childNodes.size(); c++)
			if (this.childNodes.get(c) == oldChild) {
				this.childNodes.set(c, newChild);
				return;
			}
		this.addChildNode(newChild);
	}
	
	/**	replace one of this node's child nodes with another node
	 * @param	index	the index of the child node to be replaced
	 * @param	newChild	the node to replace the child node with
	 * 	Note: 	If the node to be replaced is NULL or is not a child of this node, 
	 * 			the new node is added to this nodes child nodes without removing another node
	 */
	public void setChildNode(int index, TreeNode child) {
		if (this.childNodes == null)
			this.addChildNode(child);
		else this.childNodes.set(index, child);
	}
	
	/**	remove all child nodes from this node
	 */
	public void removeChildNodes() {
		if (this.childNodes == null)
			return;
		this.childNodes.clear();
		this.childNodes = null;
	}
	
	/**	@return	true if and only if this node has at least one child node of the specified type
	 */
	public boolean hasChildNodeOfType(String type) {
		if (this.childNodes == null)
			return false;
		for (int i = 0; i < this.childNodes.size(); i++) {
			if (((TreeNode) this.childNodes.get(i)).getNodeType().equalsIgnoreCase(type))
				return true;
		}
		return false;
	}
	
	/**	@return	the number of this node's child nodes
	 */
	public int getChildNodeCount() {
		return ((this.childNodes == null) ? 0 : this.childNodes.size());
	}
	
	/**	@return	the number of this node's child nodes that have the specified type
	 */
	public int getChildNodeCount(String type) {
		if (this.childNodes == null)
			return 0;
		int typeCount = 0;
		for (int c = 0; c < this.childNodes.size(); c++) {
			if (((TreeNode) this.childNodes.get(c)).getNodeType().equalsIgnoreCase(type))
				typeCount++;
		}
		return typeCount;
	}
	
	/**	@return	the index of the argument node within this node, or -1 if the specified node is not a child node of this node
	 */
	public int getChildNodeIndex(TreeNode childNode) {
		if (this.childNodes == null)
			return -1;
		for (int c = 0; c < this.childNodes.size(); c++) {
			if (childNode == this.childNodes.get(c))
				return c;
		}
		return -1;
	}

	/**	@return	the type specific index of the argument node within this node, or -1 if the specified node is not a child node of this node
	 */
	public int getTypeSpecificChildNodeIndex(TreeNode child) {
		if (this.childNodes == null)
			return -1;
		int childTypeCount = 0;
		for (int c = 0; c < this.childNodes.size(); c++) {
			if (child == this.childNodes.get(c))
				return childTypeCount;
			if (child.getNodeType().equalsIgnoreCase(((TreeNode) this.childNodes.get(c)).getNodeType()))
				childTypeCount++;
		}
		return -1;
	}

	/**	@return	all of this node's child nodes
	 */
	public TreeNode[] getChildNodes() {
		return ((this.childNodes == null) ? new TreeNode[0] : ((TreeNode[]) this.childNodes.toArray(new TreeNode[this.childNodes.size()])));
	}
	
	/**	@return	all of this node's child nodes that have the specified type
	 */
	public TreeNode[] getChildNodes(String type) {
		if (this.childNodes == null)
			return new TreeNode[0];
		if (type == null)
			return this.getChildNodes();
		ArrayList children = new ArrayList();
		for (int c = 0; c < this.childNodes.size(); c++) {
			if (((TreeNode) this.childNodes.get(c)).getNodeType().equalsIgnoreCase(type))
				children.add(this.childNodes.get(c));
		}
		return ((TreeNode[]) children.toArray(new TreeNode[children.size()]));
	}
	
	/**	get one of this node's child nodes, specified by its over all index
	 * @param	index	the child node's index
	 * @return	the index-th of this node's child nodes
	 */
	public TreeNode getChildNode(int index) {
		return ((this.childNodes == null) ? null : ((TreeNode) this.childNodes.get(index)));
	}
	
	/**	get one of this node's child nodes, specified by its type specific index
	 * @param	type	the type of the child node
	 * @param	index	the child node's type specific index
	 * @return	the index-th of this node's child nodes, counting only nodes of the specified type
	 */
	public TreeNode getChildNode(String type, int index) {
		if (this.childNodes == null)
			return null;
		int typeChildCount = -1;
		for (int c = 0; c < this.childNodes.size(); c++) {
			TreeNode childNode = ((TreeNode) this.childNodes.get(c));
			if (childNode.getNodeType().equalsIgnoreCase(type))
				typeChildCount++;
			if (typeChildCount == index)
				return childNode;
		}
		return null;
	}
	
	/**	@return	this node's parent node
	 */
	public TreeNode getParent() {
		return this.parentNode;
	}

	/**	sets this node's parent node to the specified node	
	 */
	public void setParent(TreeNode parent) {
		this.parentNode = parent;
	}
	
	/**	@return	this node's type (if the node is an attribute node, this method returns the attribute's name)
	 */
	public String getNodeType() {
		return (ATTRIBUTE_NODE_TYPE.equals(this.nodeType) ? this.nodeValue : this.nodeType);
	}
	
	/**	@return	this node's value
	 */
	public String getNodeValue() {
		return (ATTRIBUTE_NODE_TYPE.equals(this.nodeType) ? this.parentNode.getAttribute(this.nodeValue) : this.nodeValue);
	}
	
	/**	@return	true if and only if this TreeNode has the specified attribute
	 */
	public boolean hasAttribute(String attribute) {
		return ((this.attributes != null) && this.attributes.containsAttribute(attribute));
	}
	
	/**	read the value of an attribute
	 * @param	attribute	the name of the attribute
	 * @return	the value of the specified attribute, or null, if the TreeNode has no attribute with the specified name
	 */
	public String getAttribute(String attribute) {
		return ((this.attributes == null) ? null : this.attributes.getAttribute(attribute));
	}
	
	/**	read the value of an attribute
	 * @param	attribute	the name of the attribute
	 * @return	the value of the specified attribute as a TreeNode, or null, if the TreeNode has no attribute with the specified name
	 * 	Note:	This method unnecessarily produces an additional TreeNode, it is provided only for DOM compatibility
	 */
	public TreeNode getAttributeNode(String attribute) {
		if (this.hasAttribute(attribute)) {
			TreeNode attributeNode = new TreeNode(this, ATTRIBUTE_NODE_TYPE, attribute);
			attributeNode.documentOrderPosition = this.documentOrderPosition;
			return attributeNode;
		}
		else return null;
	}
	
	/**	@return	true if and only if this TreeNode is a DOM compatibility attribute node
	 */
	public boolean isAttributeNode() {
		return ATTRIBUTE_NODE_TYPE.equals(this.nodeType);
	}
	
	/**	read the value of an attribute
	 * @param	attribute	the name of the attribute
	 * @param	def			the value to return if the attribute is not set
	 * @return	the value of the specified attribute, or def, if the TreeNode has no attribute with the specified name
	 */
	public String getAttribute(String attribute, String def) {
		return ((this.attributes == null) ? def : this.attributes.getAttribute(attribute, def));
	}
	
	/**	add a attribute & value pair to this node
	 * @param 	attribute	the attribute
	 * @param 	value		the attribute's value
	 * 	Note: if the specified attribute is already set for this node, it's value is changed to the specified value 
	 */
	public String setAttribute(String attribute, String value) {
		if (value == null)
			return this.removeAttribute(attribute);
		if (this.attributes == null)
			this.attributes = TreeNodeAttributeSet.getTagAttributes(null, null);
		return this.attributes.setAttribute(attribute, value);
	}
	
	/**	remove the specified attribute from this node
	 */
	public String removeAttribute(String attribute) {
		return ((this.attributes == null) ? null : this.attributes.removeAttribute(attribute));
	}
	
	/**	clear this node's attributes
	 */
	public void clearAttributes() {
		if (this.attributes != null)
			this.attributes.clear();
	}
	
	/**	convert this node and it's subtree to code, using a standard Grammar 
	 * @return	a code representation of this tree as a String
	 */
	public String treeToCode() {
		return this.treeToCode(defaultGrammar);
	}
	/**	convert this node and it's subtree to code, using a standard Grammar
	 * @param 	indent		the (whitespace) sequence to be inserted at the beginning of a line once per tree level for indentation
	 * @return	a code representation of this tree as a String
	 */
	public String treeToCode(String indent) {
		return this.treeToCode(indent, "", defaultGrammar);
	}
	/**	convert this node and it's subtree to code, using the specified Grammar's standard indentation
	 * @param	grammar		the Grammar according to which the code is to be generated 
	 * @return	a code representation of this tree as a String
	 */
	public String treeToCode(Grammar grammar) {
		if (grammar == null)
			grammar = defaultGrammar;
		return this.treeToCode(grammar.getStandardIndent(), "", grammar);
	}
	/**	convert this node and it's subtree to code
	 * @param 	indent		the (whitespace) sequence to be inserted at the beginning of a line once per tree level for indentation
	 * @param	grammar		the Grammar according to which the code is to be generated 
	 * @return	a code representation of this tree as a String
	 */
	public String treeToCode(String indent, Grammar grammar) {
		return this.treeToCode(indent, "", ((grammar == null) ? defaultGrammar : grammar));
	}
	
	//	main conversion method for recursive code generation out of the tree
	private String treeToCode(String perLevelIndent, String overallIndent, Grammar grammar) {
		StringBuffer code = new StringBuffer();
		this.appendTreeCode(perLevelIndent, ((perLevelIndent == null) ? null : overallIndent), grammar, code);
		return code.toString();
	}
	private void appendTreeCode(String perLevelIndent, String overallIndent, Grammar grammar, StringBuffer code) {
		
		//	comment, DTD, etc. node
		if (COMMENT_NODE_TYPE.equals(this.nodeType) || DTD_NODE_TYPE.equals(this.nodeType) || PROCESSING_INSTRUCTION_NODE_TYPE.equals(this.nodeType))
			code.append(this.nodeValue + ((overallIndent == null) ? "" : "\n"));
		
		//	root node
		else if (ROOT_NODE_TYPE.equals(this.nodeType)) {
			if (this.childNodes == null)
				return;
			for (int c = 0; c < this.childNodes.size(); c++)
				((TreeNode) this.childNodes.get(c)).appendTreeCode(perLevelIndent, overallIndent, grammar, code);
		}
		
		//	data node
		else if (DATA_NODE_TYPE.equals(this.nodeType)) {
			if (overallIndent == null)
				code.append(this.nodeValue);
			else if (this.nodeValue.trim().length() != 0)
				code.append(grammar.escape(this.nodeValue) + "\n");
		}
		
		//	no child nodes
		else if ((this.childNodes == null) || (this.childNodes.size() == 0)) {
			if (overallIndent == null)
				code.append(this.getSingularTag(grammar));
			else code.append(overallIndent + this.getSingularTag(grammar) + "\n");
		}
		
		//	single data node as only child 
		else if ((this.childNodes.size() == 1) && DATA_NODE_TYPE.equals(((TreeNode) this.childNodes.get(0)).getNodeType())) {
			if (overallIndent == null)
				code.append(this.getStartTag(grammar) + grammar.escape(((TreeNode) this.childNodes.get(0)).getNodeValue()) + this.getEndTag(grammar));
			else code.append(overallIndent + this.getStartTag(grammar) + grammar.escape(((TreeNode) this.childNodes.get(0)).getNodeValue()) + this.getEndTag(grammar) + "\n");
		}
		
		//	otherwise
		else {
			if (overallIndent == null)
				code.append(this.getStartTag(grammar));
			else code.append(overallIndent + this.getStartTag(grammar) + "\n");
			String childIndent = ((overallIndent == null) ? null : (overallIndent + perLevelIndent));
			for (int c = 0; c < this.childNodes.size(); c++)
				((TreeNode) this.childNodes.get(c)).appendTreeCode(perLevelIndent, childIndent, grammar, code);
			if (overallIndent == null)
				code.append(this.getEndTag(grammar));
			else code.append(overallIndent + this.getEndTag(grammar) + "\n");
		}
	}
	
	/**	convert this node and its subtree to code, using a standard Grammar
	 * @param	receiver	the TokenReceiver to store the code in, one token per line
	 */
	public void treeToTokens(TokenReceiver receiver) throws IOException {
		this.treeToTokens(receiver, defaultGrammar);
	}
	/**	convert this node and it's subtree to code, using a standard Grammar
	 * @param	receiver	the TokenReceiver to store the code in, one token per line
	 * @param 	indent		the (whitespace) sequence to be inserted at the beginning of a line once per tree level for indentation
	 */
	public void treeToTokens(TokenReceiver receiver, String indent) throws IOException {
		this.treeToTokens(receiver, 0, defaultGrammar);
	}
	/**	convert this node and it's subtree to code, using the specified Grammar's standard indentation
	 * @param	receiver	the TokenReceiver to store the code in, one token per line
	 * @param	grammar		the Grammar according to which the code is to be generated 
	 */
	public void treeToTokens(TokenReceiver receiver, Grammar grammar) throws IOException {
		if (grammar == null)
			grammar = defaultGrammar;
		this.treeToTokens(receiver, 0, grammar);
	}
	/**	convert this node and it's subtree to code
	 * @param	receiver	the TokenReceiver to store the code in, one token per line
	 * @param 	indent		the (whitespace) sequence to be inserted at the beginning of a line once per tree level for indentation
	 * @param	grammar		the Grammar according to which the code is to be generated 
	 */
	public void treeToTokens(TokenReceiver receiver, String indent, Grammar grammar) throws IOException {
		this.treeToTokens(receiver, 0, ((grammar == null) ? defaultGrammar : grammar));
	}
	
	//	main conversion method for recursive code output out of the tree
	private void treeToTokens(TokenReceiver receiver, int level, Grammar grammar) throws IOException {
		
		//	comment, DTD, etc node
		if (COMMENT_NODE_TYPE.equals(this.nodeType) || DTD_NODE_TYPE.equals(this.nodeType) || PROCESSING_INSTRUCTION_NODE_TYPE.equals(this.nodeType))
			receiver.storeToken(this.nodeValue, level);
		
		//	root node
		else if (ROOT_NODE_TYPE.equals(this.nodeType)) {
			if (this.childNodes == null)
				return;
			for (int c = 0; c < this.childNodes.size(); c++)
				((TreeNode) this.childNodes.get(c)).treeToTokens(receiver, level, grammar);
		}
		
		//	data node
		else if (DATA_NODE_TYPE.equals(this.nodeType))
			receiver.storeToken(grammar.escape(this.nodeValue), level);
		
		//	no child nodes
		else if ((this.childNodes == null) || (this.childNodes.size() == 0))
			receiver.storeToken(this.getSingularTag(grammar), level);
		
		//	singular data node as only child 
		else if ((this.childNodes.size() == 1) && DATA_NODE_TYPE.equals(((TreeNode) this.childNodes.get(0)).getNodeType())) {
			receiver.storeToken(this.getStartTag(grammar), level);
			receiver.storeToken(grammar.escape(((TreeNode) this.childNodes.get(0)).getNodeValue()), level);
			receiver.storeToken(this.getEndTag(grammar), level);
		}
		
		//	otherwise
		else {
			receiver.storeToken(this.getStartTag(grammar), level);
			for (int c = 0; c < this.childNodes.size(); c++)
				((TreeNode) this.childNodes.get(c)).treeToTokens(receiver, (level + 1), grammar);
			receiver.storeToken(this.getEndTag(grammar), level);
		}
	}
	
	/**	@return	a start tag for this node according to the specified Grammar, usually <'type + attributes'>
	 */
	protected String getStartTag(Grammar grammar) {
		String attributeString = this.getAttributesForTag(grammar);
		return (grammar.getTagStart() + this.nodeType + ((attributeString.length() == 0) ? "" : (grammar.getTagAttributeSeparator() + attributeString)) + grammar.getTagEnd());
	}
	
	/**	@return	a singular tag for this node according to the specified Grammar, usually 
	 * 		- <'type + attributes'/> in XML
	 * 		- <'type + attributes'> (e.g. <'BR'>) in HTML
	 * 		- null, if XML style singular tags are not desired by the specified Grammar
	 */
	protected String getSingularTag(Grammar grammar) {
		if (grammar.isStrictXML()) {
			String attributeString = this.getAttributesForTag(grammar);
			return (grammar.getTagStart() + this.nodeType + ((attributeString.length() == 0) ? "" : (grammar.getTagAttributeSeparator() + attributeString)) + grammar.getEndTagMarker() + "" + grammar.getTagEnd());
		}
		else if (grammar.isSingularTagType(this.nodeType))
			return this.getStartTag(grammar);
		else return null;
	}
	
	/**	@return	an end tag for this node according to the specified Grammar, usually </'type'>
	 */
	protected String getEndTag(Grammar grammar) {
		return (grammar.getTagStart() + "" + grammar.getEndTagMarker() + this.nodeType + grammar.getTagEnd());
	}
	
	/**	@return	the names of this node's attributes in an array
	 */
	public String[] getAttributeNames() {
		return ((this.attributes == null) ? new String[0] : this.attributes.getAttributeNames());
	}
	
	/**	@return	this node's attribute / value pairs in an array
	 */
	public String[] getAttributeValuePairs(Grammar grammar) {
		return ((this.attributes == null) ? new String[0] : this.attributes.getAttributeValuePairs(grammar));
	}
	
	/**	@return	this node's attribute / value pairs in an array
	 */
	public String[] getAttributeValuePairs(char attributeValueSeparator, char quoter) {
		return ((this.attributes == null) ? new String[0] : this.attributes.getAttributeValuePairs(attributeValueSeparator, quoter));
	}
	
	/**	@return	this node's attribute / value pairs as a list
	 */
	public String getAttributesForTag(Grammar grammar) {
		return ((this.attributes == null) ? "" : this.attributes.getAttributeValueString(grammar));
	}
	
	/**	@return	this node's attribute / value pairs as a list
	 */
	public String getAttributesForTag(char attributeSeparator, char attributeValueSeparator, char quoter) {
		return ((this.attributes == null) ? "" : this.attributes.getAttributeValueString(attributeSeparator, attributeValueSeparator, quoter));
	}
	
	/**	@return	the number of nodes in this node's subtree
	 */
	public int countNodesInSubtree() {
		if (this.childNodes == null)
			return 1;
		int numberOfNodes = 1;
		for (int c = 0; c < this.childNodes.size(); c++)
			numberOfNodes += ((TreeNode) this.childNodes.get(c)).countNodesInSubtree();
		return numberOfNodes;
	}
	
	/**	@return	the number of nodes in this node's subtree that have the specified type
	 */
	public int countNodesInSubtree(String type) {
		int numberOfNodes = 0;
		if (this.nodeType.equalsIgnoreCase(type))
			numberOfNodes++;
		if (this.childNodes == null)
			return numberOfNodes;
		for (int c = 0; c < this.childNodes.size(); c++)
			numberOfNodes += ((TreeNode) this.childNodes.get(c)).countNodesInSubtree(type);
		return numberOfNodes;
	}
	
	/**	@return	the number of leaf nodes in this node's subtree 
	 */
	public int countLeafNodesInSubtree() {
		if ((this.childNodes == null) || (this.childNodes.size() == 0))
			return 1;
		int numberOfLeaves = 0;
		for (int c = 0; c < this.childNodes.size(); c++)
			numberOfLeaves += ((TreeNode) this.childNodes.get(c)).countLeafNodesInSubtree();
		return numberOfLeaves;
	}
	
	/**	@return	the tree depth of this node, i.e. the number of steps up to the root
	 */
	public int getTreeDepth() {
		int depth = -1;
		for (TreeNode tn = this; (tn != null) && !ROOT_NODE_TYPE.equals(tn.nodeType); tn = tn.getParent())
			depth++;
		return depth;
	}
	
	/**	@return	the mark status of this node
	 */
	public boolean isMarked() {
		return this.marked;
	}
	
	/**	mark this node
	 */
	public void markNode() {
		this.marked = true;
	}
	
	/**	unmark this node
	 */
	public void unmarkNode() {
		this.marked = false;
	}
	
	/**	mark this node and all nodes in its subtree
	 */
	public void markSubtree() {
		this.marked = true;
		if (this.childNodes == null)
			return;
		for (int c = 0; c < this.childNodes.size(); c++)
			((TreeNode) this.childNodes.get(c)).markSubtree();
	}
	
	/**	unmark this node and all nodes in its subtree
	 */
	public void unmarkSubtree() {
		this.marked = false;
		if (this.childNodes == null)
			return;
		for (int c = 0; c < this.childNodes.size(); c++)
			((TreeNode) this.childNodes.get(c)).unmarkSubtree();
	}
	
	/**	@return	this TreeNode's position in the document order of the tree it belongs to, or -1 if the document order position has not been set 
	 */
	public int getDocumentOrderPosition() {
		return this.documentOrderPosition;
	}
	
	/**	make this TreeNode know its position in the document order of the tree it belongs to
	 * @param	dop		the number to set the position to
	 * @return the number the TreeNode's position was previously set to
	 */
	public int setDocumentOrderPosition(int dop) {
		int oldDop = this.documentOrderPosition;
		this.documentOrderPosition = dop;
		return oldDop;
	}
	
	/**	compute the document order position for this TreeNode and all TreeNodes in its subtree
	 * @param	firstDop	the position number to start at (will be assigned to this TreeNode)
	 * @return the last position number that was assigned
	 */
	public int computeDocumentOrderPosition(int firstDop) {
		this.documentOrderPosition = firstDop;
		if (this.childNodes == null)
			return firstDop;
		int lastPos = firstDop;
		for (int c = 0; c < this.childNodes.size(); c++)
			lastPos = ((TreeNode) this.childNodes.get(c)).computeDocumentOrderPosition(lastPos + 1);
		return lastPos;
	}
	
	/**	clear all references in the subtree of this TreeNode for faster garbage collection
	 */
	public void deleteSubtree() {
		this.parentNode = null;
		if (this.attributes != null)
			this.attributes.clear();
		this.attributes = null;
		if (this.childNodes == null)
			return;
		for (int c = 0; c < this.childNodes.size(); c++)
			((TreeNode) this.childNodes.get(c)).deleteSubtree();
		this.childNodes.clear();
		this.childNodes = null;
	}
	
	/**	@see	java.lang.Object#toString()
	 */
	public String toString() {
		if (DATA_NODE_TYPE.equals(this.nodeType))
			return this.nodeValue;
		else return this.nodeType + ": " + this.getAttributesForTag(' ', '=', '"');
	}
	
	/**	@see	java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return ((obj instanceof TreeNode) && this.equals((TreeNode) obj));
	}
	
	/**	@see	java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(TreeNode node) {
		return ((node != null) && node.toString().equals(this.toString()) && (this.documentOrderPosition == node.documentOrderPosition));
	}
}
