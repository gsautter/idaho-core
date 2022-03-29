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
package de.uka.ipd.idaho.htmlXmlUtil.accessories;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import de.uka.ipd.idaho.htmlXmlUtil.TreeNode;

/**
 * Tool box library for executing basic path expressions on trees of TreeNode
 * objects and for generating these expressions from a given tree. It also
 * provides methods for quantitatively comparing two given trees, and for tree
 * traversal.
 * 
 * @author sautter
 */
public class TreeTools {
	//	TODO Add XML namespace cleansing facility akin to GANTA AnnotationUtils
	
  /**
	 * get all nodes of the the specified type
	 * @param root the root node of the tree to search in
	 * @param searchTag the node type to search
	 * @return the nodes that's types matche the specified one (packed in an
	 *         array in depth first order)
	 */
	public static TreeNode[] getAllNodesOfType(TreeNode root, String searchTag) {
		TreeNode[] tree = treeToDepthFirstOrder(root);
		ArrayList nodes = new ArrayList();
		for (int i = 0; i < tree.length; i++) {
			if (tree[i].getNodeType().equalsIgnoreCase(searchTag))
				nodes.add(tree[i]);
		}
		return ((TreeNode[]) nodes.toArray(new TreeNode[nodes.size()]));
	}
	
	/**
	 * get the nodes of a tree in depth first order
	 * @param root the root of the tree
	 * @return an array containing the nodes of the tree in depth first order
	 */
	public static TreeNode[] treeToDepthFirstOrder(TreeNode root) {
		if (root == null)
			return new TreeNode[0];
		
		ArrayList treeInOrder = new ArrayList();
		TreeNode currentNode = root;
		treeInOrder.add(root);
		Stack searchStack = new Stack();
		for (int n = 0; (currentNode != null) && ((currentNode != root) || (n < currentNode.getChildNodeCount()));) {
			
			//	current node has children with further children, push index on stack and descend
			if ((n < currentNode.getChildNodeCount()) && (currentNode.getChildNode(n).getChildNodeCount() != 0)) {
				searchStack.push(new Integer(n));
				currentNode = currentNode.getChildNode(n);
				treeInOrder.add(currentNode);
				n = 0;
			}
			
			//	no children with further children, switch to next child node, if there is one
			else if (n < currentNode.getChildNodeCount()) {
				treeInOrder.add(currentNode.getChildNode(n));
				n++;
			}
			
			//	no more child nodes to visit, ascend, get index of the next higher level, and switch to next node
			else {
				currentNode = currentNode.getParent();
				if (!searchStack.empty())
					n = (((Integer) searchStack.pop()).intValue() + 1);
			}
		}
		
		//	return nodes
		return ((TreeNode[]) treeInOrder.toArray(new TreeNode[treeInOrder.size()]));
	}
	
	/**
	 * get the nodes of a tree in breadth first order
	 * @param root the root of the tree
	 * @return an array containing the nodes of the tree in breadth first order
	 */
	public static TreeNode[] treeToBreadthFirstOrder(TreeNode root) {
		if (root == null)
			return new TreeNode[0];

		ArrayList treeInOrder = new ArrayList();
		treeInOrder.add(root);
		for (int n = 0; n < treeInOrder.size(); n++) {
			TreeNode node = ((TreeNode) treeInOrder.get(n));
			
			// append all children of the current node to the set of nodes, then switch to next node
			for (int cn = 0; cn < node.getChildNodeCount(); cn++)
				treeInOrder.add(node.getChildNode(cn));
		}
		
		// return nodes
		return ((TreeNode[]) treeInOrder.toArray(new TreeNode[treeInOrder.size()]));
	}
//	
//	/**
//	 * check if two trees are equal
//	 * @param root1 the root node of the first tree
//	 * @param root2 the root node of the second tree
//	 * @return true if and only if both trees are equal
//	 */
//	public static boolean treesEqual(TreeNode root1, TreeNode root2) {
//		
//		TreeNode[] tree1 = treeToDepthFirstOrder(root1);
//		TreeNode[] tree2 = treeToDepthFirstOrder(root2);
//		int index = 0;
//		
//		//	compare the trees node by node
//		while ((tree1.length == tree2.length) && (index < tree1.length))
//			index = ((tree1[index].getNodeType().equalsIgnoreCase(tree2[index].getNodeType())) ? (index + 1) : (tree1.length + 1));
//		
//		//	return comparison result
//		return ((tree1.length == tree2.length) && (index == tree1.length));
//	}
//	
//	/**
//	 * check if two trees are equal, considering only nodes affecting the page
//	 * layout structure
//	 * @param root1 the root node of the first tree
//	 * @param root2 the root node of the second tree
//	 * @return true if and only if both trees are equal in structure
//	 */
//	public static boolean treesEqualStruct(TreeNode root1, TreeNode root2) {
//		
//		TreeNode[] tree1 = removeNonStructNodes(treeToDepthFirstOrder(root1));
//		TreeNode[] tree2 = removeNonStructNodes(treeToDepthFirstOrder(root2));
//		int index = 0;
//		
//		//	compare the trees node by node
//		while ((tree1.length == tree2.length) && (index < tree1.length))
//			index = ((tree1[index].getNodeType().equalsIgnoreCase(tree2[index].getNodeType())) ? (index + 1) : (tree1.length + 1));
//		
//		//	return comparison result
//		return ((tree1.length == tree2.length) && (index == tree1.length));
//	}
//	
//	/**
//	 * compute the equality of two trees
//	 * @param root1 the root node of the first tree
//	 * @param root2 the root node of the second tree
//	 * @return the equality of the trees in percent
//	 */
//	public static int treesEquality(TreeNode root1, TreeNode root2) {
//		TreeNode[] tree1 = treeToDepthFirstOrder(root1);
//		TreeNode[] tree2 = treeToDepthFirstOrder(root2);
//
//		return compareTrees(tree1, tree2);
//	}
//
//	/**
//	 * compute the equality of two trees, considering only nodes affecting the
//	 * page layout structure
//	 * @param root1 the root node of the first tree
//	 * @param root2 the root node of the second tree
//	 * @return the equality of the trees in percent
//	 */
//	public static int treesEqualityStruct(TreeNode root1, TreeNode root2) {
//		TreeNode[] tree1 = treeToDepthFirstOrder(root1);
//		TreeNode[] tree2 = treeToDepthFirstOrder(root2);
//		
//		removeNonStructNodes(tree1);
//		removeNonStructNodes(tree2);
//		
//		return compareTrees(tree1, tree2);
//	}
//	
//	/**
//	 * remove the non-structural (text level layout) nodes
//	 * @param nodes the array containing the nodes
//	 * @return an array containing all the structural the nodes contained in the
//	 *         argument array
//	 */
//	private static TreeNode[] removeNonStructNodes(TreeNode[] nodes) {
//		Vector structNodes = new Vector();
//		for (int i = 0; i < nodes.length; i++)
//			if (NON_STRUCT_TAGS.indexOf(";" + nodes[i].getNodeType().toLowerCase() + ";") == -1) structNodes.addElement(nodes[i]);
//		return ((TreeNode[]) structNodes.toArray(new TreeNode[structNodes.size()]));
//	}
//
//	/**
//	 * compare two trees hierarchically (the closer to the root node differences
//	 * are, the more they reduce equality)
//	 * @param root1 the first tree
//	 * @param root2 the second tree
//	 * @return the match level of the two trees in percent
//	 */
//	private static int compareTrees(TreeNode[] tree1, TreeNode[] tree2) {
//		
//		//	check parameters
//		if ((tree1 == null) != (tree2 == null)) return 0;
//		if ((tree1.length == 0) != (tree2.length == 0)) return 0;
//		
//		String tree1String;
//		String tree2String;
//		TreeNodeVector tree1subtreeRoots = new TreeNodeVector();
//		TreeNodeVector tree2subtreeRoots = new TreeNodeVector();
//		Vector tree1subtrees = new Vector();
//		Vector tree2subtrees = new Vector();
//		int maxEquality1;
//		int maxEquality2;
//		
//		//	get tree strings
//		tree1[0].markSubtree();
//		tree2[0].markSubtree();
//		tree1String = getMarkedNodeString(tree1);
//		tree2String = getMarkedNodeString(tree2);
//		tree1[0].unmarkSubtree();
//		tree2[0].unmarkSubtree();
//		
//		//	check if trees are equal
//		if (tree1String.equalsIgnoreCase(tree2String)) {
//			System.gc();
//			return 100;
//		}
//		
//		//	if trees are not equal
//		else {
//			
//			//	if both trees have subtrees contained in the sets of relevant subtrees
//			if ((tree1.length > 1) && (tree2.length > 1)) {
//				
//				//	if one tree is pre-, in- or suffix of the other one, compute equality value directly
//				if (tree1String.startsWith(tree2String.substring(0, tree2[0].getNodeType().length())) && (tree1String.indexOf(tree2String.substring(tree2[0].getNodeType().length())) > -1)) {
//					System.gc();
//					return (int) ((tree2.length * 100) / tree1.length);
//				}
//				
//				else if (tree2String.startsWith(tree1String.substring(0, tree1[0].getNodeType().length())) && (tree2String.indexOf(tree1String.substring(tree1[0].getNodeType().length())) > -1)) {
//					System.gc();
//					return (int) ((tree1.length * 100) / tree2.length);
//				}
//				
//				//	otherwise
//				else {
//					
//					//	get subtrees
//					//	get root nodes of first level subtrees
//					for (int i = 0; i < tree1[0].getChildNodeCount(); i++) tree1subtreeRoots.addElement(tree1[0].getChildNode(i));
//					for (int i = 0; i < tree2[0].getChildNodeCount(); i++) tree2subtreeRoots.addElement(tree2[0].getChildNode(i));
//					
//					//	get first level subtrees
//					for (int i = 0; i < tree1subtreeRoots.size(); i++) tree1subtrees.addElement(treeToDepthFirstOrder(tree1subtreeRoots.get(i)));
//					for (int i = 0; i < tree2subtreeRoots.size(); i++) tree2subtrees.addElement(treeToDepthFirstOrder(tree2subtreeRoots.get(i)));
//					
//					//	compare subtrees in all possible combinations
//					maxEquality1 = compareTreeSets(tree1subtrees, tree2subtrees);
//					maxEquality2 = compareTreeSets(tree2subtrees, tree1subtrees);
//					
//					//	return maximum achieved equality value
//					if (maxEquality1 > maxEquality2) {
//						System.gc();
//						return maxEquality1;
//					}
//					else {
//						System.gc();
//						return maxEquality2;
//					}
//				}
//			}
//			else {
//				System.gc();
//				return 0;
//			}
//		}
//	}
//	
//	/**
//	 * concatenate the types of the marked nodes in the array to a String
//	 * @param nodes the array containing the nodes
//	 * @return the types of the marked nodes in the array concatenated to a
//	 *         String
//	 */
//	private static String getMarkedNodeString(TreeNode[] nodes) {
//		StringBuffer assembler = new StringBuffer();
//		for (int i = 0; i < nodes.length; i++)
//			if (nodes[i].isMarked()) assembler = assembler.append(nodes[i].getNodeType().toLowerCase());
//		return assembler.toString();
//	}
//
//	/**
//	 * compare all possible combinations of two sets of trees in order to find
//	 * the best match
//	 * @param set1 the first set of trees
//	 * @param set2 the second set of trees
//	 * @return the maximum match percentage
//	 */
//	private static int compareTreeSets(Vector set1, Vector set2) {
//		
//		Vector rootNodeTypes1 = new Vector();
//		Vector rootNodeTypes2 = new Vector();
//		Vector subSet1 = new Vector();
//		Vector subSet2 = new Vector();
//		
//		int heuristicMaxValue = 0;
//		int currentEquality = 0;
//		int restEquality = 0;
//		int equality = 0;
//		int maxEquality = 0;
//		
//		//	if both sets are not empty
//		if (set1.size() > 0 && set2.size() > 0) {
//			
//			//	get root nodes of all trees in the sets
//			for (int i = 0; i < set1.size(); i++) rootNodeTypes1.addElement(((TreeNode[]) set1.get(i))[0].getNodeType());
//			for (int i = 0; i < set2.size(); i++) rootNodeTypes2.addElement(((TreeNode[]) set2.get(i))[0].getNodeType());
//			
//			//	combine all trees in the first set with all in the second
//			for (int i = 0; i < rootNodeTypes1.size(); i++){
//				for (int j = 0; j < rootNodeTypes2.size(); j++) {
//					
//					//	compute maximum achievable equality value with current anchors
//					if ((set1.size() - i) < (set2.size() - j))
//						heuristicMaxValue = ((set1.size() - (i+1)) * 200);
//					else heuristicMaxValue = ((set2.size() - (j+1)) * 200);
//					
//					//	check if there can be a combination with higher equality
//					if ((((String) rootNodeTypes1.get(i)).equalsIgnoreCase((String) rootNodeTypes2.get(j))) && (((int) (heuristicMaxValue + 200) / (set1.size() + set2.size())) > maxEquality)) {
//						subSet1.clear();
//						subSet2.clear();
//						
//						//	collect all trees following the current anchors
//						for (int k = i+1; k < set1.size(); k++) subSet1.addElement(set1.get(k));
//						for (int k = j+1; k < set2.size(); k++) subSet2.addElement(set2.get(k));
//						
//						//	compare current anchor trees
//						currentEquality = compareTrees((TreeNode[]) set1.get(i), (TreeNode[]) set2.get(j));
//						
//						//	if the equality value might be higher than the highes achieved so far
//						//	check all combinations af the tree sets folloging the current anchor
//						if (((int) ((heuristicMaxValue + (currentEquality * 2)) / (set1.size() + set2.size()))) > maxEquality) {
//							restEquality = compareTreeSets(subSet1, subSet2);
//							equality = (int) (((restEquality * (subSet1.size() + subSet2.size())) + (currentEquality * 2)) / (set1.size() + set2.size()));
//							
//							if (equality > maxEquality)
//								maxEquality = equality;
//						}
//					}
//				}
//			}
//			
//			//	return maximum achieved equality value
//			System.gc();
//			return maxEquality;
//		}
//		
//		//	otherwise
//		else {
//			System.gc();
//			return 0;
//		}
//	}
//	
//	/**
//	 * encode a String so it is displayed in an HTML page a it's handed over to
//	 * this method (e.g. replace '<' with '&lt;')
//	 * @param string the String to be encoded
//	 * @return a String that is displayed in an HTML page a just as the argument
//	 *         String would be in a text area
//	 */
//	public static String encodeSpecialCharacters(String string) {
//		//	check parameter
//		if ((string == null) || (string.length() == 0)) return "";
//		
//		//	check each character
//		Html grammar = new Html();
//		StringBuffer assembler = new StringBuffer();
//		int index = 0;
//		while (index < string.length()) {
//			char currentChar = string.charAt(index);
//			
//			//	probable start of encoded character
//			if (currentChar == '&') {
//				StringBuffer codeAssembler = new StringBuffer();
//				String code = null;
//				int cIndex = 0;
//				
//				//	check if encoded character
//				while (((index + cIndex) < string.length()) && (cIndex < grammar.getCharLookahead()) && (code == null)) {
//					codeAssembler.append(string.charAt(index + cIndex));
//					if (grammar.isCharCode(codeAssembler.toString()))
//						code = codeAssembler.toString();
//					cIndex ++;
//				}
//				
//				//	if encoded character identified, append code
//				if (code != null) {
//					assembler.append(code);
//					index += cIndex;
//				}
//				
//				//	else append current character
//				else {
//					assembler.append(grammar.getCharCode(currentChar));
//					index ++;
//				}
//			}
//			
//			//	encode current character and append char
//			else {
//				assembler.append(grammar.getCharCode(currentChar));
//				index ++;
//			} 
//		}
//
//		return assembler.toString();
//	}
//
//	/**
//	 * concatenate the Strings contained in the specified vector
//	 * @param v the Vector
//	 * @param separator the separator String
//	 * @return the Strings contained in v concatenated with separator in between
//	 */
//	public static String concatVector(Vector v, String separator) {
//		StringBuffer assembler = new StringBuffer();
//		for (int i = 0; i < v.size(); i++)
//			if (v.get(i) instanceof String) assembler.append(((i > 0) ? separator : "") + ((String) v.get(i)));
//		return assembler.toString();
//	}
	
	/**
	 * remove the specified attribute from any node of the specified type
	 * @param root the root node of the page tree to process
	 * @param nodeType the type of the nodes to process (is null is specified
	 *            for this attribute, all nodes of all types will be affected)
	 * @param attribute the attribute to be removed
	 */
	public static void removeAttribute(TreeNode root, String nodeType, String attribute) {
		TreeNode[] nodes = ((nodeType != null) ? getAllNodesOfType(root, nodeType) : treeToBreadthFirstOrder(root));
		for (int n = 0; n < nodes.length; n++)
			nodes[n].removeAttribute(attribute);
	}
	
	/**
	 * remove the specified attributes from the nodes with the specified types
	 * @param root the root node of the tree to process
	 * @param nodeTypes the types of the nodes to process (is null is specified
	 *            for this attribute, all nodes of all types will be affected)
	 * @param attributes the attributes to be removed
	 */
	public static void removeAttributes(TreeNode root, String[] nodeTypes, String[] attributes) {
		HashSet types = new HashSet();
		for (int t = 0; t < nodeTypes.length; t++)
			types.add(nodeTypes[t].toLowerCase());
		TreeNode[] nodes = treeToBreadthFirstOrder(root);
		for (int n = 0; n < nodes.length; n++)
			if ((nodeTypes == null) || types.contains(nodes[n].getNodeType().toLowerCase())) {
				for (int a = 0; a < attributes.length; a++)
					nodes[n].removeAttribute(attributes[a]);
			}
	}
	
	/**
	 * add the specified attribute with the specified value to all nodes of the
	 * specified type
	 * @param root the root node of the tree to process
	 * @param nodeType the type of the nodes to process (is null is specified
	 *            for this attribute, all nodes of all types will be affected)
	 * @param attribute the attributes to be added
	 * @param value the value to set the attribute to
	 */
	public static void addAttribute(TreeNode root, String nodeType, String attribute, String value) {
		if (root == null)
			return;
		TreeNode[] nodes = ((nodeType != null) ? getAllNodesOfType(root, nodeType) : treeToBreadthFirstOrder(root));
		for (int i = 0; i < nodes.length; i++) {
			if (!nodes[i].hasAttribute(attribute))
				nodes[i].setAttribute(attribute, value);
		}
	}
//	
//	private static class TreeNodeVector {
//		private Vector vector;
//		TreeNodeVector() {
//			this.vector = new Vector();
//		}
//		public void addElement(TreeNode p) {
//			vector.addElement(p);
//		}
//		public TreeNode get(int index) {
//			return (TreeNode) vector.get(index);
//		}
//		public int size() {
//			return vector.size();
//		}
//	}
//
//	private static class IntVector {
//		private Vector vector;
//		IntVector() {
//			this.vector = new Vector();
//		}
//		public void addElement(int i) {
//			vector.addElement(new Integer(i));
//		}
//		public int get(int index) {
//			return ((Int) vector.get(index)).intValue();
//		}
//		public void insertElementAt(int i, int index) {
//			vector.insertElementAt(new Int(i), index);
//		}
//		public int remove(int index) {
//			return ((Int) vector.remove(index)).intValue();
//		}
//		public void removeElementAt(int index) {
//			vector.removeElementAt(index);
//		}
//		public int size() {
//			return vector.size();
//		}
//		public void clearTo(int i) {
//			int currentIndex = i - 1;
//			while (this.vector.size() > 0 && currentIndex > 0) {
//				this.vector.removeElementAt(0);
//				currentIndex--;
//			}
//		}
//		public void clearFrom(int i) {
//			while ((this.vector.size() > i) && (this.vector.size() > 0)) this.vector.removeElementAt(i);
//		}
//	}
//
//	private static class Int {
//		private int value;
//		Int(int val) {
//			this.value = val;
//		}
//		public int intValue() {
//			return this.value;
//		}
//	}
//
//	private static class StringVector {
//		private Vector vector;
//		StringVector() {
//			this.vector = new Vector();
//		}
//		public void addElement(String s) {
//			vector.addElement(s);
//		}
//		public String get(int index) {
//			return ((String) vector.get(index));
//		}
//		public String lastElement() {
//			return ((String) vector.lastElement());
//		}
//		public String remove(int index) {
//			return ((String) vector.remove(index));
//		}
//		public void removeElementAt(int index) {
//			vector.removeElementAt(index);
//		}
//		public void setElementAt(String s, int index) {
//			vector.setElementAt(s, index);
//		}
//		public int size() {
//			return vector.size();
//		}
//		public void clearTo(int i) {
//			int currentIndex = i - 1;
//			while (this.vector.size() > 0 && currentIndex > 0) {
//				this.vector.removeElementAt(0);
//				currentIndex--;
//			}
//		}
//		public void clearFrom(int i) {
//			while ((this.vector.size() > i) && (this.vector.size() > 0)) this.vector.removeElementAt(i);
//		}
//	}
}
