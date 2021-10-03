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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList.AttributeSummary;
import de.uka.ipd.idaho.stringUtils.StringVector;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringRelation;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * List of documents residing in memory to facilitate sorting and the like in a
 * (client side) document listing.
 * 
 * @author sautter
 */
public class DocumentListBuffer extends StringRelation implements LiteratureConstants {
	private final DocumentList data;
	
	/**
	 * the field names for the document list, in the order they should be
	 * displayed
	 */
	public final String[] listFieldNames;
	
	/**
	 * Constructor
	 * @param listFieldNames the field names for the document list, in the order
	 *            they should be displayed
	 */
	public DocumentListBuffer(String[] listFieldNames) {
		this.listFieldNames = listFieldNames;
		this.data = null;
	}
	
	/**
	 * Constructor building a buffered document list around an iterator-style
	 * one, transferring all document list elements from the latter into the
	 * buffer, thereby consuming them from the argument list's getNextDocument()
	 * method until the hasNextDocument() method returns false.
	 * @param data the source document list
	 */
	public DocumentListBuffer(DocumentList data) {
		this(data, null);
	}
	
	/**
	 * Constructor building a buffered document list around an iterator-style
	 * one, transferring all document list elements from the latter into the
	 * buffer, thereby consuming them from the argument list's getNextDocument()
	 * method until the hasNextDocument() method returns false.
	 * @param data the source document list
	 * @param pm a progress monitor observing the document list being read
	 */
	public DocumentListBuffer(DocumentList data, ProgressMonitor pm) {
		this.listFieldNames = data.listFieldNames;
		this.data = data;
		
		while (data.hasNextDocument()) {
			DocumentListElement dle = data.getNextDocument();
			StringTupel st = new StringTupel(this.listFieldNames.length);
			for (int f = 0; f < this.listFieldNames.length; f++) {
				Object value = dle.getAttribute(this.listFieldNames[f]);
				if ((value != null) && (value instanceof String))
					st.setValue(this.listFieldNames[f], AnnotationUtils.unescapeFromXml(value.toString()));
			}
			this.addElement(st);
			
			if (pm == null)
				continue;
			pm.setInfo("" + this.size() + " documents read.");
			int docCount = data.getDocumentCount();
			if (docCount > 0)
				pm.setProgress((this.size() * 100) / docCount);
		}
		
		for (int f = 0; f < this.listFieldNames.length; f++) {
			AttributeSummary listFieldValues = data.getListFieldValues(this.listFieldNames[f]);
			if (listFieldValues != null)
				this.listFieldValues.put(this.listFieldNames[f], listFieldValues);
		}
		if (this.listFieldValues.isEmpty()) {
			for (int f = 0; f < this.listFieldNames.length; f++) {
				if (data.hasNoSummary(this.listFieldNames[f]))
					continue;
				this.listFieldValues.put(this.listFieldNames[f], new AttributeSummary());
			}
			for (int d = 0; d < this.size(); d++) {
				StringTupel docData = this.get(d);
				for (int f = 0; f < this.listFieldNames.length; f++) {
					if (data.hasNoSummary(this.listFieldNames[f]))
						continue;
					String fieldValue = docData.getValue(this.listFieldNames[f]);
					if ((fieldValue == null) || (fieldValue.length() == 0))
						continue;
					this.getListFieldValues(this.listFieldNames[f]).add(fieldValue);
				}
			}
			for (int f = 0; f < this.listFieldNames.length; f++) {
				if (data.hasNoSummary(this.listFieldNames[f]))
					continue;
				if (this.getListFieldValues(this.listFieldNames[f]).size() == 0)
					this.listFieldValues.remove(this.listFieldNames[f]);
			}
		}
		
		if (pm != null) {
			pm.setInfo("Document summary data read.");
			pm.setProgress(100);
		}
	}
	
	/**
	 * Indicate whether or not to create a summary of the values of a given
	 * field in the document list. This method exist so implementations can
	 * amend the built-in set of non-summarized list fields.
	 * @param listFieldName the name of the list fields
	 * @return true if values of the argument list fields should not be summarized
	 */
	public boolean hasNoSummary(String listFieldName) {
		return ((this.data == null) ? DocumentList.nonSummarizedListFieldNames.contains(listFieldName) : this.data.hasNoSummary(listFieldName));
	}
	
	/**
	 * Indicate whether or not the values of a given list fields are numeric.
	 * @param listFieldName the name of the list fields
	 * @return true if values of the argument list fields are numeric
	 */
	public boolean isNumeric(String listFieldName) {
		if (this.data == null) {
			return (false
				|| listFieldName.endsWith("Time")
				|| listFieldName.equalsIgnoreCase("time")
				|| listFieldName.endsWith("Year")
				|| listFieldName.equalsIgnoreCase("year")
				|| listFieldName.endsWith("Version")
				|| listFieldName.equalsIgnoreCase("version")
			);
		}
		else return this.data.isNumeric(listFieldName);
	}
	
	/**
	 * Indicate whether or not a given list fields can be used to filter in a
	 * backing persistent representation of a document list (e.g. a database).
	 * @param listFieldName the name of the list fields
	 * @return true if the argument list fields is suitable for filtering
	 */
	public boolean isFilterable(String listFieldName) {
		if (this.data == null) {
			return (false
				|| listFieldName.endsWith("Name")
				|| listFieldName.equalsIgnoreCase("name")
				|| listFieldName.endsWith("Id")
				|| listFieldName.equalsIgnoreCase("id")
				|| listFieldName.endsWith("Uuid")
				|| listFieldName.equalsIgnoreCase("uuid")
				|| listFieldName.endsWith("Time")
				|| listFieldName.equalsIgnoreCase("time")
				|| listFieldName.endsWith("Year")
				|| listFieldName.equalsIgnoreCase("year")
				|| listFieldName.endsWith("Version")
				|| listFieldName.equalsIgnoreCase("version")
			);
		}
		else return this.data.isFilterable(listFieldName);
	}
	
	/**
	 * Retrieve a summary of the values in a list field. The sets returned by
	 * this method are immutable. If there is no summary, this method returns
	 * null, but never an empty set. The set does not contain nulls and is
	 * sorted lexicographically.
	 * @param listFieldName the name of the field
	 * @return a set containing the summary values
	 */
	public AttributeSummary getListFieldValues(String listFieldName) {
		return ((AttributeSummary) this.listFieldValues.get(listFieldName));
	}
	
	private Map listFieldValues = new HashMap();
	
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
		if (this.size() == 0) {
			
			//	write results
			bw.write("<" + DocumentList.DOCUMENT_LIST_NODE_NAME + 
					" " + DocumentList.DOCUMENT_LIST_FIELDS_ATTRIBUTE + "=\"" + listFields.concatStrings(" ") + "\"" +
					" " + DocumentList.NON_SUMMARY_FIELDS_ATTRIBUTE + "=\"" + nonSummaryFields.concatStrings(" ") + "\"" +
					" " + DocumentList.NUMERIC_FIELDS_ATTRIBUTE + "=\"" + numericFields.concatStrings(" ") + "\"" +
					" " + DocumentList.FILTER_FIELDS_ATTRIBUTE + "=\"" + filterFields.concatStrings(" ") + "\"" +
			"/>");
			bw.newLine();
		}
		
		//	write data
		else {
			
			//	get result field names
			bw.write("<" + DocumentList.DOCUMENT_LIST_NODE_NAME + 
					" " + DocumentList.DOCUMENT_LIST_FIELDS_ATTRIBUTE + "=\"" + listFields.concatStrings(" ") + "\"" +
					" " + DocumentList.NON_SUMMARY_FIELDS_ATTRIBUTE + "=\"" + nonSummaryFields.concatStrings(" ") + "\"" +
					" " + DocumentList.NUMERIC_FIELDS_ATTRIBUTE + "=\"" + numericFields.concatStrings(" ") + "\"" +
					" " + DocumentList.FILTER_FIELDS_ATTRIBUTE + "=\"" + filterFields.concatStrings(" ") + "\"" +
					">");
			
			for (int d = 0; d < this.size(); d++) {
				StringTupel dle = this.get(d);
				bw.write("  <" + DocumentList.DOCUMENT_NODE_NAME);
				for (int f = 0; f < listFields.size(); f++) {
					String listField = listFields.get(f);
					String listFieldValue = dle.getValue(listField);
					if ((listFieldValue != null) && (listFieldValue.length() != 0))
						bw.write(" " + listField + "=\"" + AnnotationUtils.escapeForXml(listFieldValue, true) + "\"");
				}
				bw.write("/>");
				bw.newLine();
				pm.setProgress((d * 100) / this.size());
			}
			
			bw.write("</" + DocumentList.DOCUMENT_LIST_NODE_NAME + ">");
			bw.newLine();
		}
		
		if (bw != out)
			bw.flush();
		pm.setProgress(100);
	}
	
	/**
	 * Write the documents in this list to a given writer.
	 * @param out the writer to write to
	 * @throws IOException
	 */
	public void writeData(Writer out) throws IOException {
		this.writeData(out, ProgressMonitor.silent);
	}
	
	/**
	 * Write the documents in this list to a given writer.
	 * @param out the writer to write to
	 * @param pm a progress monitor observing output
	 * @throws IOException
	 */
	public void writeData(Writer out, ProgressMonitor pm) throws IOException {
		BufferedWriter bw = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
		pm.setBaseProgress(0);
		pm.setMaxProgress(100);
		bw.write("" + this.size());
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
		for (int d = 0; d < this.size(); d++) {
			StringTupel dle = this.get(d);
			bw.write(dle.toCsvString('\t', '"', this.listFieldNames));
			bw.newLine();
			pm.setProgress((d * 100) / this.size());
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
}
