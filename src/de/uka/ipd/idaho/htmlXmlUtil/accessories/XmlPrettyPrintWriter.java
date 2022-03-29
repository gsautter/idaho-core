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
package de.uka.ipd.idaho.htmlXmlUtil.accessories;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiver;
import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiverWriter;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;

/**
 * Writer pretty printing XML that passes through.
 * 
 * @author sautter
 */
public class XmlPrettyPrintWriter extends Writer {
	static final StandardGrammar xml = new StandardGrammar();
	private TokenReceiverWriter trw;
	
	/** Constructor
	 * @param out the writer to write through to
	 */
	public XmlPrettyPrintWriter(Writer out) throws IOException {
		this(xml, out, null);
	}
	
	/** Constructor
	 * @param grammar the grammar to use for tokenization
	 * @param out the writer to write through to
	 */
	public XmlPrettyPrintWriter(Grammar grammar, Writer out) throws IOException {
		this(grammar, out, null);
	}
	
	/** Constructor
	 * @param out the writer to write through to
	 * @param indent the whitespace to add for indentation per level of tree depth
	 */
	public XmlPrettyPrintWriter(Writer out, String indent) throws IOException {
		this(xml, out, indent);
	}
	
	/** Constructor
	 * @param grammar the grammar to use for tokenization
	 * @param out the writer to write through to
	 * @param indent the whitespace to add for indentation per level of tree depth
	 */
	public XmlPrettyPrintWriter(Grammar grammar, Writer out, String indent) throws IOException {
		this.trw = new TokenReceiverWriter(grammar, new XmlPrettyPrintTokenReceiver(out, this, indent));
	}
	
	private static class XmlPrettyPrintTokenReceiver extends TokenReceiver {
		private BufferedWriter out;
		private String pendingStartTag = null;
		private int pendingStartTagTreeDepth = -1;
		private String pendingText = null;
		private int pendingTextTreeDepth = -1;
		private XmlPrettyPrintWriter parent;
		private String indent;
		XmlPrettyPrintTokenReceiver(Writer out, XmlPrettyPrintWriter parent, String indent) throws IOException {
			this.out = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
			this.parent = parent;
			if ((indent == null) || (indent.length() == 0))
				this.indent = null;
			else this.indent = indent;
		}
		public void storeToken(String token, int treeDepth) throws IOException {
			if (xml.isTag(token)) {
				if (xml.isEndTag(token)) {
					boolean flushMultiLine = ((this.pendingStartTag == null) || (this.pendingText == null) || !xml.getType(this.pendingStartTag).equals(xml.getType(token)));
					this.flush(flushMultiLine);
					if (flushMultiLine)
						this.writeIndent(treeDepth);
					this.out.write(this.parent.formatTag(token));
					this.out.newLine();
				}
				else if (xml.isSingularTag(token)) {
					this.flush(true);
					this.writeIndent(treeDepth);
					this.out.write(this.parent.formatTag(token));
					this.out.newLine();
				}
				else {
					this.flush(true);
					this.pendingStartTag = token;
					this.pendingStartTagTreeDepth = treeDepth;
				}
			}
			else if (xml.isComment(token)) {
				this.flush(true);
				this.writeIndent(treeDepth);
				this.out.write(this.parent.formatComment(token));
				this.out.newLine();
			}
			else if (xml.isDTD(token)) {
				this.flush(true);
				this.writeIndent(treeDepth);
				this.out.write(this.parent.formatDtd(token));
				this.out.newLine();
			}
			else if (xml.isProcessingInstruction(token)) {
				this.flush(true);
				this.writeIndent(treeDepth);
				this.out.write(this.parent.formatProcessingInstruction(token));
				this.out.newLine();
			}
			else {
				token = token.trim();
				if (token.length() == 0)
					return;
				token = this.parent.formatData(token);
				if ((this.pendingStartTag != null) && (this.pendingText == null)) {
					this.pendingText = token;
					this.pendingTextTreeDepth = treeDepth;
				}
				else {
					this.flush(true);
					this.writeIndent(treeDepth);
					this.out.write(token);
					this.out.newLine();
				}
			}
		}
		private void flush(boolean addLineBreaks) throws IOException {
			if (this.pendingStartTag != null) {
				this.writeIndent(this.pendingStartTagTreeDepth);
				this.out.write(this.parent.formatTag(this.pendingStartTag));
				this.pendingStartTag = null;
				this.pendingStartTagTreeDepth = -1;
				if (addLineBreaks)
					this.out.newLine();
			}
			if (this.pendingText != null) {
				if (addLineBreaks)
					this.writeIndent(this.pendingTextTreeDepth);
				this.out.write(this.pendingText);
				this.pendingText = null;
				this.pendingTextTreeDepth = -1;
				if (addLineBreaks)
					this.out.newLine();
			}
		}
		private void writeIndent(int treeDepth) throws IOException {
			if (this.indent == null)
				return;
			for (int l = 0; l < treeDepth; l++)
				this.out.write(this.indent);
		}
		public void close() throws IOException {
			this.out.flush();
			this.out.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.io.Writer#write(char[], int, int)
	 */
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.trw.write(cbuf, off, len);
	}
	
	/* (non-Javadoc)
	 * @see java.io.Writer#flush()
	 */
	public void flush() throws IOException {
		this.trw.flush();
	}
	
	/* (non-Javadoc)
	 * @see java.io.Writer#close()
	 */
	public void close() throws IOException {
		this.trw.close();
	}
	
	/**
	 * Format an XML comment for output. This default implementation simply
	 * returns the argument comment, subclasses are welcome to overwrite it as
	 * needed.
	 * @param comment the comment to format
	 * @return the formatted comment
	 */
	protected String formatComment(String comment) {
		return comment;
	}
	
	/**
	 * Format an XML processing instruction for output. This default
	 * implementation simply returns the argument processing instruction,
	 * subclasses are welcome to overwrite it as needed.
	 * @param processingInstruction the processing instruction to format
	 * @return the formatted processing instruction
	 */
	protected String formatProcessingInstruction(String processingInstruction) {
		return processingInstruction;
	}
	
	/**
	 * Format an XML DTD for output. This default implementation simply
	 * returns the argument DTD, subclasses are welcome to overwrite it as
	 * needed.
	 * @param dtd the DTD to format
	 * @return the formatted DTD
	 */
	protected String formatDtd(String dtd) {
		return dtd;
	}
	
	/**
	 * Format an XML tag for output. This default implementation simply returns
	 * the argument tag, subclasses are welcome to overwrite it as needed.
	 * @param tag the tag to format
	 * @return the formatted tag
	 */
	protected String formatTag(String tag) {
		return tag;
	}
	
	/**
	 * Format an XML data/text node for output. This default implementation<UL>
	 * <LI>flattens out the argument string to a single line</LI>
	 * <LI>reduces sequences of spaces to single spaces</LI>
	 * <LI>removes spaces before periods, commas, colons, semicolons, question
	 * and exclamation marks, and closing brackets</LI>
	 * <LI>removes spaces after opening brackets</LI>
	 * </UL>. Subclasses are welcome to overwrite this method as needed to add
	 * further normalization or reduce the described normalization procedure.
	 * @param data the data/text node to format
	 * @return the formatted data/text node
	 */
	protected String formatData(String data) {
		StringBuffer fData = new StringBuffer();
		char lCh = ((char) 0); // last char _written_
		for (int c = 0; c < data.length(); c++) {
			char ch = data.charAt(c);
			if (ch <= ' ') {
				if (lCh == ' ')
					continue; // ignore double space
				if ("([{".indexOf(lCh) != -1)
					continue; // ignore space after opening bracket
				fData.append(lCh = ' ');
			}
			else if (lCh == ' ') {
				if (".,:;!?)]}".indexOf(ch) != -1)
					fData.deleteCharAt(fData.length() - 1); // remove space before closing bracket, period, comma, etc.
				fData.append(lCh = ch);
			}
			else fData.append(lCh = ch);
		}
		return fData.toString();
	}
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//		//	TODOne test this sucker
//		//	0384433845052F6CFE68FD53FDA7FD8D_tp_l1.xml
//		File f = new File("E:/Projektdaten/TaxPubOutput/0384433845052F6CFE68FD53FDA7FD8D_tp_l1.xml");
//		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
//		XmlPrettyPrintWriter xppw = new XmlPrettyPrintWriter(new OutputStreamWriter(System.out), "    ");
//		char[] buf = new char[256];
//		for (int r; (r = br.read(buf, 0, buf.length)) != -1;)
//			xppw.write(buf, 0, r);
//		xppw.flush();
//		xppw.close();
//		br.close();
//	}
}
///**
// * Writer pretty printing XML that passes through.
// * 
// * @author sautter
// */
//public class XmlPrettyPrintWriter extends TokenReceiverWriter {
//	static final StandardGrammar xml = new StandardGrammar();
//	
//	/** Constructor
//	 * @param out the writer to write through to
//	 */
//	public XmlPrettyPrintWriter(Writer out) throws IOException {
//		this(null, out);
//	}
//	
//	/** Constructor
//	 * @param grammar the grammar to use for tokenization
//	 * @param out the writer to write through to
//	 */
//	public XmlPrettyPrintWriter(Grammar grammar, Writer out) throws IOException {
//		super(grammar, new XmlPrettyPrintTokenReceiver(out));
//	}
//	
//	private static class XmlPrettyPrintTokenReceiver extends TokenReceiver {
//		private BufferedWriter out;
//		private String pendingStartTag = null;
//		private String pendingText = null;
//		XmlPrettyPrintTokenReceiver(Writer out) throws IOException {
//			this.out = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
//		}
//		public void storeToken(String token, int treeDepth) throws IOException {
//			if (xml.isTag(token)) {
//				if (xml.isEndTag(token)) {
//					boolean flushMultiLine = ((this.pendingStartTag == null) || (this.pendingText == null) || !xml.getType(this.pendingStartTag).equals(xml.getType(token)));
//					this.flush(flushMultiLine);
//					this.out.write(token);
//					this.out.newLine();
//				}
//				else if (xml.isSingularTag(token)) {
//					this.flush(true);
//					this.out.write(token);
//					this.out.newLine();
//				}
//				else {
//					this.flush(true);
//					this.pendingStartTag = token;
//				}
//			}
//			else if (xml.isComment(token) || xml.isDTD(token) || xml.isProcessingInstruction(token)) {
//				this.flush(true);
//				this.out.write(token);
//				this.out.newLine();
//			}
//			else {
//				token = token.trim();
//				if (token.length() == 0)
//					return;
//				
//				//	TODOne_above maybe rather use StringBuffer and passing loop ?!?
//				token = token.replaceAll("\\s+", " ");
//				
//				token = token.replaceAll("\\s+\\.", ".");
//				token = token.replaceAll("\\s+\\,", ",");
//				token = token.replaceAll("\\s+\\:", ":");
//				token = token.replaceAll("\\s+\\;", ";");
//				token = token.replaceAll("\\s+\\!", "!");
//				token = token.replaceAll("\\s+\\?", "?");
//				token = token.replaceAll("\\s+\\)", ")");
//				token = token.replaceAll("\\s+\\]", "]");
//				token = token.replaceAll("\\s+\\}", "}");
//				
//				token = token.replaceAll("\\(\\s+", "(");
//				token = token.replaceAll("\\[\\s+", "[");
//				token = token.replaceAll("\\{\\s+", "{");
//				
//				if ((this.pendingStartTag != null) && (this.pendingText == null))
//					this.pendingText = token;
//				else {
//					this.flush(true);
//					this.out.write(token);
//					this.out.newLine();
//				}
//			}
//		}
//		private void flush(boolean addLineBreaks) throws IOException {
//			if (this.pendingStartTag != null) {
//				this.out.write(this.pendingStartTag);
//				this.pendingStartTag = null;
//				if (addLineBreaks)
//					this.out.newLine();
//			}
//			if (this.pendingText != null) {
//				this.out.write(this.pendingText);
//				this.pendingText = null;
//				if (addLineBreaks)
//					this.out.newLine();
//			}
//		}
//		public void close() throws IOException {
//			this.out.flush();
//			this.out.close();
//		}
//	}
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//		//	TODOne test this sucker
//		//	0384433845052F6CFE68FD53FDA7FD8D_tp_l1.xml
//		File f = new File("E:/Projektdaten/TaxPubOutput/0384433845052F6CFE68FD53FDA7FD8D_tp_l1.xml");
//		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
//		XmlPrettyPrintWriter xppw = new XmlPrettyPrintWriter(new OutputStreamWriter(System.out));
//		char[] buf = new char[256];
//		for (int r; (r = br.read(buf, 0, buf.length)) != -1;)
//			xppw.write(buf, 0, r);
//		xppw.flush();
//		xppw.close();
//		br.close();
//	}
//}
