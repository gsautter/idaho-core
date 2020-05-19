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
package de.uka.ipd.idaho.gamta.util.transfer;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * List of documents in a GoldenGATE DIO, implemented iterator-style for
 * efficiency.
 * 
 * @author sautter
 */
public abstract class DocumentList implements LiteratureConstants {
	
	/** the type for the root node of the XML representation of a document list */
	public static final String DOCUMENT_LIST_NODE_NAME = "docList";
	
	/** the type for the node holding data on individual document in the XML representation of a document list */
	public static final String DOCUMENT_NODE_NAME = "doc";
	
	/** the attribute holding the complete list of fields present in the XML representation of a document list */
	public static final String DOCUMENT_LIST_FIELDS_ATTRIBUTE = "listFields";
	
	/** the attribute holding the list of non-summarized fields in the XML representation of a document list */
	public static final String NON_SUMMARY_FIELDS_ATTRIBUTE = "nonSummaryFields";
	
	/** the attribute holding the list of numeric fields in the XML representation of a document list */
	public static final String NUMERIC_FIELDS_ATTRIBUTE = "numericFields";
	
	/** the attribute holding the list of filterable fields in the XML representation of a document list */
	public static final String FILTER_FIELDS_ATTRIBUTE = "filterFields";
	
	/**
	 * Constant set containing the names of list fields for which document
	 * lists never contain value summaries. This set is immutable and contains
	 * the field names 'docId', 'docName', and 'docTitle', which tend to be
	 * different for each and every document and would thus result in very
	 * large summaries.
	 */
	public static final Set nonSummarizedListFieldNames;
	static {
		Set nslfns = new LinkedHashSet(4);
		nslfns.add(DOCUMENT_ID_ATTRIBUTE);
		nslfns.add(DOCUMENT_NAME_ATTRIBUTE);
		nslfns.add(DOCUMENT_TITLE_ATTRIBUTE);
		nonSummarizedListFieldNames = Collections.unmodifiableSet(nslfns);
	}
	
	/**
	 * Constant set containing the comparison operators that can be used for
	 * numeric field names when filtering a document list. This set is
	 * immutable.
	 */
	public static final Set numericOperators;
	static {
		Set nos = new LinkedHashSet(7);
		nos.add(">");
		nos.add(">=");
		nos.add("=");
		nos.add("<=");
		nos.add("<");
		numericOperators = Collections.unmodifiableSet(nos);
	}
	
	/**
	 * A hybrid of set and map, this class contains the distinct values of
	 * document attributes in a list, plus for each attribute value pair the
	 * number of documents having that particular value for the attribute. This
	 * is to help filtering document lists.
	 * 
	 * @author sautter
	 */
	public static class AttributeSummary extends CountingSet {
		
		/** Constructor
		 */
		public AttributeSummary() {
			super(new LinkedHashMap()); // cannot use TreeMap, as values coming in in sorted order degrade these
		}
	}
	
	/**
	 * the field names for the document list, in the order they should be
	 * displayed
	 */
	public final String[] listFieldNames;

	/**
	 * Constructor for general use
	 * @param listFieldNames the field names for the document list, in the order
	 *            they should be displayed
	 */
	public DocumentList(String[] listFieldNames) {
		this.listFieldNames = listFieldNames;
	}
	
	/**
	 * Constructor for creating wrappers
	 * @param model the document list to wrap
	 */
	public DocumentList(DocumentList model) {
		this(model, null);
	}
	
	/**
	 * Constructor for creating wrappers that add fields
	 * @param model the document list to wrap
	 * @param extensionListFieldNames an array holding additional field names
	 */
	public DocumentList(DocumentList model, String[] extensionListFieldNames) {
		if (extensionListFieldNames == null) {
			this.listFieldNames = new String[model.listFieldNames.length];
			System.arraycopy(model.listFieldNames, 0, this.listFieldNames, 0, model.listFieldNames.length);
		}
		else {
			String[] listFieldNames = new String[model.listFieldNames.length + extensionListFieldNames.length];
			System.arraycopy(model.listFieldNames, 0, listFieldNames, 0, model.listFieldNames.length);
			System.arraycopy(extensionListFieldNames, 0, listFieldNames, model.listFieldNames.length, extensionListFieldNames.length);
			this.listFieldNames = listFieldNames;
		}
		for (int f = 0; f < this.listFieldNames.length; f++)
			this.addListFieldValues(this.listFieldNames[f], model.getListFieldValues(this.listFieldNames[f]));
	}
	
	/**
	 * Indicate whether or not to create a summary of the values of a given
	 * field in the document list. This method exist so implementations can
	 * amend the built-in set of non-summarized list fields.
	 * @param listFieldName the name of the list fields
	 * @return true if values of the argument list fields should not be summarized
	 */
	public abstract boolean hasNoSummary(String listFieldName);
	
	/**
	 * Indicate whether or not the values of a given list fields are numeric.
	 * @param listFieldName the name of the list fields
	 * @return true if values of the argument list fields are numeric
	 */
	public abstract boolean isNumeric(String listFieldName);
	
	/**
	 * Indicate whether or not a given list fields can be used to filter in a
	 * backing persistent representation of a document list (e.g. a database).
	 * @param listFieldName the name of the list fields
	 * @return true if the argument list fields is suitable for filtering
	 */
	public abstract boolean isFilterable(String listFieldName);
	
	/**
	 * Retrieve a summary of the values in a list field. The sets returned by
	 * this method are immutable. If there is no summary, this method returns
	 * null, but never an empty set. The set does not contain nulls.
	 * @param listFieldName the name of the field
	 * @return a set containing the summary values
	 */
	public AttributeSummary getListFieldValues(String listFieldName) {
		return this.getListFieldValues(listFieldName, false);
	}
	
	/**
	 * Retrieve an attribute summary, and generate it if there is none. This
	 * helps in de-serializing document lists, in particular list heads. This
	 * method is only protected so sub classes can de-serialize in a custom
	 * way; it should be used with care.
	 * @param listFieldName the name of the field
	 * @param create create the summary if it doesn't exist?
	 * @return a set containing the summary values
	 */
	protected AttributeSummary getListFieldValues(String listFieldName, boolean create) {
		AttributeSummary das = ((AttributeSummary) this.listFieldValues.get(listFieldName));
		if ((das == null) && create) {
			das = new AttributeSummary();
			this.listFieldValues.put(listFieldName, das);
		}
		return das;
	}
	
	/**
	 * Add a set of values to the value summary of a list field. 
	 * @param listFieldName the name of the field
	 * @param listFieldValues the values to add
	 */
	protected final void addListFieldValues(String listFieldName, AttributeSummary listFieldValues) {
		if (nonSummarizedListFieldNames.contains(listFieldName) || this.hasNoSummary(listFieldName))
			return;
		if ((listFieldValues == null) || (listFieldValues.size() == 0))
			return;
		AttributeSummary as = this.getListFieldValues(listFieldName, true);
		for (Iterator vit = listFieldValues.iterator(); vit.hasNext();) {
			String listFieldValue = ((String) vit.next());
			as.add(listFieldValue, listFieldValues.getCount(listFieldValue));
		}
	}
	
	/**
	 * Add a set of values to the value summary of a list field. 
	 * @param listFieldName the name of the field
	 * @param listFieldValues the values to add
	 */
	protected final void addListFieldValues(DocumentListElement dle) {
		if (dle == null)
			return;
		for (int f = 0; f < this.listFieldNames.length; f++) {
			if (nonSummarizedListFieldNames.contains(this.listFieldNames[f]) || !this.hasNoSummary(this.listFieldNames[f]))
				continue;
			String listFieldValue = ((String) dle.getAttribute(this.listFieldNames[f]));
			if (listFieldValue == null)
				continue;
			this.getListFieldValues(this.listFieldNames[f], true).add(listFieldValue);
		}
	}
	
	private Map listFieldValues = new HashMap();
	
	/**
	 * Check if there is another document in the list.
	 * @return true if there is another document, false otherwise
	 */
	public abstract boolean hasNextDocument();

	/**
	 * Retrieve the next document from the list. If there is no next document, this
	 * method returns null.
	 * @return the next document in the list
	 */
	public abstract DocumentListElement getNextDocument();
	
	/**
	 * Check the total number of documents in the list. If the count is not
	 * available, this method returns -1. Otherwise, the returned value can
	 * either be the exact number of documents remaining, or a conservative
	 * estimate, if the exact number is not available. This default
	 * implementation returns -1 if getRetrievedDocumentCount() returns -1, and
	 * the sum of getRetrievedDocumentCount() and getRemainingDocumentCount()
	 * otherwise. Sub classes are welcome to overwrite it and provide a more
	 * exact estimate. They need to make sure not to use this implementation in
	 * their implementation of getRetrievedDocumentCout() or
	 * getReminingDocumentCount(), however, to prevent delegating back and forth
	 * and causing stack overflows.
	 * @return the number of documents remaining
	 */
	public int getDocumentCount() {
		int retrieved = this.getRetrievedDocumentCount();
		return ((retrieved == -1) ? -1 : (retrieved + this.getRemainingDocumentCount()));
	}
	
	/**
	 * Check the number of documents retrieved so far from the getNextDocument()
	 * method. If the count is not available, this method returns -1. This
	 * default implementation does return -1, sub classes are welcome to
	 * overwrite it and provide a count.
	 * @return the number of documents retrieved so far
	 */
	public int getRetrievedDocumentCount() {
		return -1;
	}
	
	/**
	 * Check the number of documents remaining in the list. If the count is not
	 * available, this method returns -1. Otherwise, the returned value can
	 * either be the exact number of documents remaining, or a conservative
	 * estimate, if the exact number is not available. This default
	 * implementation returns 1 if hasNextDocument() returns true, and 0
	 * otherwise. Sub classes are welcome to overwrite it and provide a more
	 * accurate estimate.
	 * @return the number of documents remaining
	 */
	public int getRemainingDocumentCount() {
		return (this.hasNextDocument() ? 1 : 0);
	}
	
	/**
	 * Write this document list to some writer as XML data.
	 * @param out the Writer to write to
	 * @throws IOException
	 */
	public void writeXml(Writer out) throws IOException {
		this.writeXml(out, ProgressMonitor.silent);
	}
	
	/**
	 * Write this document list to some writer as XML data.
	 * @param out the Writer to write to
	 * @param pm a progress monitor observing output
	 * @throws IOException
	 */
	public void writeXml(Writer out, ProgressMonitor pm) throws IOException {
		
		//	produce writer
		BufferedWriter bw = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
		
		//	prepare progress monitor
		pm.setBaseProgress(0);
		pm.setMaxProgress(100);
		
		//	gather fields and their properties
		StringVector listFields = new StringVector();
		StringVector nonSummaryFields = new StringVector();
		StringVector numericFields = new StringVector();
		StringVector filterFields = new StringVector();
		for (int f = 0; f < this.listFieldNames.length; f++) {
			listFields.addElement(this.listFieldNames[f]);
			if (this.hasNoSummary(this.listFieldNames[f]))
				nonSummaryFields.addElement(this.listFieldNames[f]);
			if (this.isNumeric(this.listFieldNames[f]))
				numericFields.addElement(this.listFieldNames[f]);
			if (this.isFilterable(this.listFieldNames[f]))
				filterFields.addElement(this.listFieldNames[f]);
		}
		
		//	write empty data
		if (!this.hasNextDocument()) {
			
			//	write results
			bw.write("<" + DOCUMENT_LIST_NODE_NAME + 
					" " + DOCUMENT_LIST_FIELDS_ATTRIBUTE + "=\"" + listFields.concatStrings(" ") + "\"" +
					" " + NON_SUMMARY_FIELDS_ATTRIBUTE + "=\"" + nonSummaryFields.concatStrings(" ") + "\"" +
					" " + NUMERIC_FIELDS_ATTRIBUTE + "=\"" + numericFields.concatStrings(" ") + "\"" +
					" " + FILTER_FIELDS_ATTRIBUTE + "=\"" + filterFields.concatStrings(" ") + "\"" +
			"/>");
			bw.newLine();
		}
		
		//	write data
		else {
			
			//	get result field names
			bw.write("<" + DOCUMENT_LIST_NODE_NAME + 
					" " + DOCUMENT_LIST_FIELDS_ATTRIBUTE + "=\"" + listFields.concatStrings(" ") + "\"" +
					" " + NON_SUMMARY_FIELDS_ATTRIBUTE + "=\"" + nonSummaryFields.concatStrings(" ") + "\"" +
					" " + NUMERIC_FIELDS_ATTRIBUTE + "=\"" + numericFields.concatStrings(" ") + "\"" +
					" " + FILTER_FIELDS_ATTRIBUTE + "=\"" + filterFields.concatStrings(" ") + "\"" +
			">");
			bw.newLine();
			
			int docCount = this.getDocumentCount();
			int wDocCount = 0;
			while (this.hasNextDocument()) {
				DocumentListElement dle = this.getNextDocument();
				
				//	write element
				bw.write("  <" + DOCUMENT_NODE_NAME);
				for (int a = 0; a < this.listFieldNames.length; a++) {
					String fieldValue = ((String) dle.getAttribute(this.listFieldNames[a]));
					if ((fieldValue != null) && (fieldValue.length() != 0))
						bw.write(" " + this.listFieldNames[a] + "=\"" + AnnotationUtils.escapeForXml(fieldValue, true) + "\"");
				}
				bw.write("/>");
				bw.newLine();
				
				//	update progress
				wDocCount++;
				pm.setProgress((wDocCount * 100) / docCount);
			}
			
			bw.write("</" + DOCUMENT_LIST_NODE_NAME + ">");
			bw.newLine();
		}
		
		//	flush Writer if it was wrapped
		if (bw != out)
			bw.flush();
		
		//	show finished
		pm.setProgress(100);
	}
	
	/**
	 * Write the documents in this list to a given writer. This method consumes
	 * the list, i.e., it iterates through to the last document list element it
	 * contains.
	 * @param out the writer to write to
	 * @throws IOException
	 */
	public void writeData(Writer out) throws IOException {
		this.writeData(out, ProgressMonitor.silent);
	}
	
	/**
	 * Write the documents in this list to a given writer. This method consumes
	 * the list, i.e., it iterates through to the last document list element it
	 * contains.
	 * @param out the writer to write to
	 * @param pm a progress monitor observing output
	 * @throws IOException
	 */
	public void writeData(Writer out, ProgressMonitor pm) throws IOException {
		BufferedWriter bw = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
		pm.setBaseProgress(0);
		pm.setMaxProgress(100);
		bw.write("" + this.getDocumentCount());
		bw.newLine();
		for (int f = 0; f < this.listFieldNames.length; f++) {
			if (f != 0)
				bw.write("\t");
			bw.write(this.listFieldNames[f]);
			if (this.hasNoSummary(this.listFieldNames[f]))
				bw.write("/S");
			if (this.isNumeric(this.listFieldNames[f]))
				bw.write("/N");
			if (this.isFilterable(this.listFieldNames[f]))
				bw.write("/F");
		}
		bw.newLine();
		int docCount = this.getDocumentCount();
		int wDocCount = 0;
		while (this.hasNextDocument()) {
			bw.write(this.getNextDocument().toTabString(this.listFieldNames));
			bw.newLine();
			wDocCount++;
			pm.setProgress((wDocCount * 100) / docCount);
		}
		for (int f = 0; f < this.listFieldNames.length; f++) {
			AttributeSummary fieldValues = this.getListFieldValues(this.listFieldNames[f]);
			if (fieldValues == null)
				continue;
			bw.write(this.listFieldNames[f]);
			for (Iterator vit = fieldValues.iterator(); vit.hasNext();) {
				String fieldValue = ((String) vit.next());
				bw.write(" " + URLEncoder.encode(fieldValue, "UTF-8") + "=" + fieldValues.getCount(fieldValue));
			}
			bw.newLine();
		}
		if (bw != out)
			bw.flush();
		pm.setProgress(100);
	}
	
	/**
	 * Wrap a document list around a reader, which provides the list's data in
	 * form of a character stream. Do not close the specified reader after this
	 * method returns. The reader is closed by the returned list after the last
	 * document list element is read.
	 * @param in the Reader to read from
	 * @return a document list that makes the data from the specified reader
	 *         available as document list elements
	 * @throws IOException
	 */
	public static DocumentList readDocumentList(Reader in) throws IOException {
		return readDocumentList(in, ProgressMonitor.silent);
	}
	
	/**
	 * Wrap a document list around a reader, which provides the list's data in
	 * form of a character stream. Do not close the specified reader after this
	 * method returns. The reader is closed by the returned list after the last
	 * document list element is read.
	 * @param in the Reader to read from
	 * @param pm a progress monitor observing the reading process
	 * @return a document list that makes the data from the specified reader
	 *         available as document list elements
	 * @throws IOException
	 */
	public static DocumentList readDocumentList(Reader in, ProgressMonitor pm) throws IOException {
		
		//	create buffered reader for document count and field names
		BufferedReader br = ((in instanceof BufferedReader) ? ((BufferedReader) in) : new BufferedReader(in));
		
		//	get total list size
		String docCountString = br.readLine();
		int docCount = -1;
		try {
			docCount = Integer.parseInt(docCountString);
		} catch (NumberFormatException nfe) {}
		
		//	get list fields
		String fieldString = br.readLine();
		String[] fields = fieldString.split("\\t");
		
		//	parse field properties off names
		HashSet nonSummaryFields = new HashSet();
		HashSet numericFields = new HashSet();
		HashSet filterFields = new HashSet();
		for (int f = 0; f < fields.length; f++) {
			int split = fields[f].indexOf('/');
			if (split == -1)
				continue;
			String fieldProps = fields[f].substring(split);
			fields[f] = fields[f].substring(0, split);
			if (fieldProps.indexOf("/S") != -1)
				nonSummaryFields.add(fields[f]);
			if (fieldProps.indexOf("/N") != -1)
				numericFields.add(fields[f]);
			if (fieldProps.indexOf("/F") != -1)
				filterFields.add(fields[f]);
		}
		
		//	create document list
		DocumentList dl = new ReaderDocumentList(fields, br, docCount, pm, nonSummaryFields, numericFields, filterFields);
		
		/*
		 * This call to hasNextDocument() is necessary to make sure attribute
		 * summaries are loaded even if client does not call hasNextDocument(),
		 * e.g. knowing that it's a list head request only.
		 */
		dl.hasNextDocument();
		
		//	finally ...
		return dl;
	}
	
	private static class ReaderDocumentList extends DocumentList {
		private BufferedReader br;
		private ProgressMonitor pm;
		private String next = null;
		private int docCount;
		private int docsRetrieved = 0;
		private int charsRead = 0;
		private int charsRetrieved = 0;
		private HashSet nonSummaryFieldNames = new HashSet();
		private HashSet numericFieldNames;
		private HashSet filterFieldNames;
		ReaderDocumentList(String[] listFieldNames, BufferedReader in, int docCount, ProgressMonitor pm, HashSet nonSummaryFieldNames, HashSet numericFieldNames, HashSet filterFieldNames) {
			super(listFieldNames);
			this.br = new BufferedReader(new FilterReader(in) {
				public int read() throws IOException {
					int r = super.read();
					if (r != -1)
						charsRead++;
					return r;
				}
				public int read(char[] cbuf, int off, int len) throws IOException {
					int r = super.read(cbuf, off, len);
					if (r != -1)
						charsRead += r;
					return r;
				}
			}, 65536);
			this.docCount = docCount;
			this.pm = ((pm == null) ? ProgressMonitor.silent : pm);
			this.pm.setBaseProgress(0);
			this.pm.setMaxProgress(100);
			this.nonSummaryFieldNames = nonSummaryFieldNames;
			this.numericFieldNames = numericFieldNames;
			this.filterFieldNames = filterFieldNames;
		}
		public int getDocumentCount() {
			return this.docCount;
		}
		public int getRetrievedDocumentCount() {
			return this.docsRetrieved;
		}
		public int getRemainingDocumentCount() {
			if (this.docCount == -1) {
				if (this.charsRetrieved == 0)
					return (this.hasNextDocument() ? 1 : 0);
				int docSize = (this.charsRetrieved / ((this.docsRetrieved == 0) ? 1 : this.docsRetrieved));
				int charsRemaining = (charsRead - this.charsRetrieved);
				return Math.round(((float) charsRemaining) / docSize);
			}
			else return (this.docCount - this.docsRetrieved);
		}
		public boolean hasNoSummary(String listFieldName) {
			return this.nonSummaryFieldNames.contains(listFieldName);
		}
		public boolean isNumeric(String listFieldName) {
			return this.numericFieldNames.contains(listFieldName);
		}
		public boolean isFilterable(String listFieldName) {
			return this.filterFieldNames.contains(listFieldName);
		}
		public boolean hasNextDocument() {
			
			//	we're good already here
			if (this.next != null)
				return true;
			
			//	input utterly depleted
			else if (this.br == null)
				return false;
			
			//	read next line
			try {
				this.next = this.br.readLine();
			}
			catch (IOException ioe) {
				ioe.printStackTrace(System.out);
			}
			
			//	input depleted
			if ((this.next == null) || (this.next.trim().length() == 0)) {
				this.closeReader();
				return false;
			}
			
			//	data line, we're good
			if (this.next.indexOf('\t') != -1) {
				this.charsRetrieved += this.next.length();
				return true;
			}
			
			//	read field value summary
			int split = this.next.indexOf(' ');
			if (split != -1) {
				String fieldName = this.next.substring(0, split);
				String[] fieldValues = this.next.substring(split + 1).split("\\s++");
				AttributeSummary as = this.getListFieldValues(fieldName, true);
				for (int v = 0; v < fieldValues.length; v++) try {
					String fieldValue = fieldValues[v];
					int countStart = fieldValue.indexOf('=');
					if (countStart == -1)
						as.add(URLDecoder.decode(fieldValue, "UTF-8"));
					else as.add(URLDecoder.decode(fieldValue.substring(0, countStart), "UTF-8"), Integer.parseInt(fieldValue.substring(countStart + "=".length())));
				} catch (IOException ioe) {}
			}
			
			//	recurse, there might be more
			this.next = null;
			return this.hasNextDocument();
		}
		public DocumentListElement getNextDocument() {
			if (!this.hasNextDocument())
				return null;
			String[] next = this.next.split("\\t");
			this.next = null;
			DocumentListElement dle = new DocumentListElement();
			for (int f = 0; f < this.listFieldNames.length; f++)
				dle.setAttribute(this.listFieldNames[f], ((f < next.length) ? next[f] : ""));
			this.docsRetrieved++;
			this.pm.setProgress((this.docsRetrieved * 100) / this.docCount);
			return dle;
		}
		private void closeReader() {
			try {
				this.br.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace(System.out);
			}
			this.br = null;
			this.pm.setProgress(100);
		}
		protected void finalize() throws Throwable {
			if (this.br != null) {
				this.br.close();
				this.br = null;
			}
		}
	}
}