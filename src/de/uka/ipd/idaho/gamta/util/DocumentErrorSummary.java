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
package de.uka.ipd.idaho.gamta.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uka.ipd.idaho.gamta.Attributed;

/**
 * Summary of a document error protocol, containing only error categories and
 * types, along with counts of error severities in each. All the various
 * <code>getErrors()</code> methods return empty arrays.
 * 
 * @author sautter
 */
public class DocumentErrorSummary extends DocumentErrorProtocol {
	
	/** the ID of the document the error summary pertains to */
	public final String docId;
	
	private TreeSet errorCategories = new TreeSet();
	private TreeSet errorTypes = new TreeSet();
	private CountingSet errorCounts = new CountingSet(new TreeMap());
	private CountingSet falsePositiveCounts = new CountingSet(new TreeMap());
	private CountingSet errorSeverityCounts = new CountingSet(new TreeMap());
	private CountingSet falsePositiveSeverityCounts = new CountingSet(new TreeMap());
	
	/** Constructor
	 * @param docId the ID of the document the error summary pertains to
	 */
	public DocumentErrorSummary(String docId) {
		this.docId = docId;
	}
	
	/** Constructor
	 * @param docId the ID of the document the error summary pertains to
	 * @param dep the error protocol to summarize
	 */
	public DocumentErrorSummary(String docId, DocumentErrorProtocol dep) {
		this.docId = docId;
		if (dep == null) {}
		else if (dep instanceof DocumentErrorSummary) {}
		else {
			DocumentError[] des = dep.getErrors();
			if (des != null) {
				for (int e = 0; e < des.length; e++)
					this.addError(des[e].category, des[e].type, des[e].severity, 1, false);
			}
			DocumentError[] fps = dep.getErrors();
			if (fps != null) {
				for (int e = 0; e < fps.length; e++)
					this.addError(fps[e].category, fps[e].type, fps[e].severity, 1, true);
			}
		}
	}
	
	public int getErrorCategoryCount() {
		return this.errorCategories.size();
	}
	
	public int getErrorTypeCount() {
		return this.errorTypes.size();
	}
	
	public int getErrorCount() {
		return this.errorCounts.getCount("");
	}
	
	public int getFalsePositiveCount() {
		return this.falsePositiveCounts.getCount("");
	}
	
	public int getErrorSeverityCount(String severity) {
		return this.errorSeverityCounts.getCount(severity);
	}
	
	public int getFalsePositiveSeverityCount(String severity) {
		return this.falsePositiveSeverityCounts.getCount(severity);
	}
	
	public DocumentError[] getErrors() {
		return new DocumentError[0];
	}
	
	public int getErrorCount(String category) {
		return ((category == null) ? this.errorCounts.getCount("") : this.errorCounts.getCount(category));
	}
	
	public int getFalsePositiveCount(String category) {
		return ((category == null) ? this.falsePositiveCounts.getCount("") : this.falsePositiveCounts.getCount(category));
	}
	
	public int getErrorSeverityCount(String category, String severity) {
		return this.errorSeverityCounts.getCount(category + "." + severity);
	}
	
	public int getFalsePositiveSeverityCount(String category, String severity) {
		return this.falsePositiveSeverityCounts.getCount(category + "." + severity);
	}
	
	public DocumentError[] getErrors(String category) {
		return new DocumentError[0];
	}
	
	public int getErrorCount(String category, String type) {
		return ((category == null) ? this.errorCounts.getCount("") : ((type == null) ? this.errorCounts.getCount(category) : this.errorCounts.getCount(category + "." + type)));
	}
	
	public int getFalsePositiveCount(String category, String type) {
		return ((category == null) ? this.falsePositiveCounts.getCount("") : ((type == null) ? this.falsePositiveCounts.getCount(category) : this.falsePositiveCounts.getCount(category + "." + type)));
	}
	
	public int getErrorSeverityCount(String category, String type, String severity) {
		return this.errorSeverityCounts.getCount(category + "." + type + "." + severity);
	}
	
	public int getFalsePositiveSeverityCount(String category, String type, String severity) {
		return this.falsePositiveSeverityCounts.getCount(category + "." + type + "." + severity);
	}
	
	public DocumentError[] getErrors(String category, String type) {
		return new DocumentError[0];
	}
	
	public void addError(String source, Attributed subject, Attributed parent, String category, String type, String description, String severity, boolean falsePositive) {
		this.errorCategories.add(category);
		this.errorTypes.add(category + "." + type);
//		this.errorCounts.add("");
//		this.errorCounts.add(category);
//		this.errorCounts.add(category + "." + type);
//		this.errorSeverityCounts.add(severity);
//		this.errorSeverityCounts.add(category + "." + severity);
//		this.errorSeverityCounts.add(category + "." + type + "." + severity);
		CountingSet counts = (falsePositive ? this.falsePositiveCounts : this.errorCounts);
		counts.add("");
		counts.add(category);
		counts.add(category + "." + type);
		CountingSet severityCounts = (falsePositive ? this.falsePositiveSeverityCounts : this.errorSeverityCounts);
		severityCounts.add(severity);
		severityCounts.add(category + "." + severity);
		severityCounts.add(category + "." + type + "." + severity);
	}
	
	void addError(String category, String type, String severity, int count, boolean falsePositive) {
		this.errorCategories.add(category);
		this.errorTypes.add(category + "." + type);
//		this.errorCounts.add("", count);
//		this.errorCounts.add(category, count);
//		this.errorCounts.add((category + "." + type), count);
//		this.errorSeverityCounts.add(severity, count);
//		this.errorSeverityCounts.add((category + "." + severity), count);
//		this.errorSeverityCounts.add((category + "." + type + "." + severity), count);
		CountingSet counts = (falsePositive ? this.falsePositiveCounts : this.errorCounts);
		counts.add("", count);
		counts.add(category, count);
		counts.add((category + "." + type), count);
		CountingSet severityCounts = (falsePositive ? this.falsePositiveSeverityCounts : this.errorSeverityCounts);
		severityCounts.add(severity, count);
		severityCounts.add((category + "." + severity), count);
		severityCounts.add((category + "." + type + "." + severity), count);;
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
		return new DocumentError[0];
	}
	
	public void removeError(DocumentError error) {}
	public Comparator getErrorComparator() {
		return null;
	}
	public Attributed findErrorSubject(Attributed doc, String[] data) {
		return null;
	}
	
	/**
	 * Store an error summary to a given output stream.
	 * @param ides the error summary to store
	 * @param out the output stream to store the error summary to
	 * @throws IOException
	 */
	public static void storeErrorSummary(DocumentErrorSummary ides, OutputStream out) throws IOException {
		storeErrorSummary(ides, new OutputStreamWriter(out, "UTF-8"));
	}
	
	/**
	 * Store an error summary to a given output stream.
	 * @param des the error summary to store
	 * @param out the output stream to store the error protocol to
	 * @throws IOException
	 */
	public static void storeErrorSummary(DocumentErrorSummary des, Writer out) throws IOException {
		//	TODOnot consider zipping ==> IMF is zipped anyway, and IMD is huge, so ease of access more important
		
		//	persist error protocol
		BufferedWriter epBw = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
		String[] categories = des.getErrorCategories();
		for (int c = 0; c < categories.length; c++) {
			
			//	store category proper
			epBw.write("CATEGORY");
			epBw.write("\t" + categories[c]);
			epBw.write("\t" + des.getErrorCategoryLabel(categories[c]));
			epBw.write("\t" + des.getErrorCategoryDescription(categories[c]));
			epBw.newLine();
			
			//	store error types in current category
			String[] types = des.getErrorTypes(categories[c]);
			for (int t = 0; t < types.length; t++) {
				
				//	store type proper
				epBw.write("TYPE");
				epBw.write("\t" + types[t]);
				epBw.write("\t" + des.getErrorTypeLabel(categories[c], types[t]));
				epBw.write("\t" + des.getErrorTypeDescription(categories[c], types[t]));
				epBw.newLine();
				
				//	store error counts
				storeErrorSeverity(categories[c], types[t], DocumentError.SEVERITY_BLOCKER, des, epBw);
				storeErrorSeverity(categories[c], types[t], DocumentError.SEVERITY_CRITICAL, des, epBw);
				storeErrorSeverity(categories[c], types[t], DocumentError.SEVERITY_MAJOR, des, epBw);
				storeErrorSeverity(categories[c], types[t], DocumentError.SEVERITY_MINOR, des, epBw);
				
				//	store false positive counts
				storeFalsePositiveSeverity(categories[c], types[t], DocumentError.SEVERITY_BLOCKER, des, epBw);
				storeFalsePositiveSeverity(categories[c], types[t], DocumentError.SEVERITY_CRITICAL, des, epBw);
				storeFalsePositiveSeverity(categories[c], types[t], DocumentError.SEVERITY_MAJOR, des, epBw);
				storeFalsePositiveSeverity(categories[c], types[t], DocumentError.SEVERITY_MINOR, des, epBw);
			}
		}
		epBw.flush();
	}
	private static void storeErrorSeverity(String category, String type, String severity, DocumentErrorSummary ides, BufferedWriter epBw) throws IOException {
		int errors = ides.getErrorSeverityCount(category, type, severity);
		if (errors == 0)
			return;
		epBw.write("ERROR");
		epBw.write("\t" + severity);
		epBw.write("\t" + errors);
		epBw.newLine();
	}
	private static void storeFalsePositiveSeverity(String category, String type, String severity, DocumentErrorSummary ides, BufferedWriter epBw) throws IOException {
		int errors = ides.getErrorSeverityCount(category, type, severity);
		if (errors == 0)
			return;
		epBw.write("FALPOS");
		epBw.write("\t" + severity);
		epBw.write("\t" + errors);
		epBw.newLine();
	}
	
	/**
	 * Fill an error summary with the data provided by a given input stream.
	 * @param des the error summary to populate
	 * @param in the input stream to populate the error summary from
	 * @throws IOException
	 */
	public static void fillErrorSummary(DocumentErrorSummary des, InputStream in) throws IOException {
		fillErrorSummary(des, new InputStreamReader(in, "UTF-8"));
	}
	
	/**
	 * Fill an error summary with the data provided by a given input stream.
	 * @param des the error summary to populate
	 * @param in the input stream to populate the error summary from
	 * @throws IOException
	 */
	public static void fillErrorSummary(DocumentErrorSummary des, Reader in) throws IOException {
		//	TODOnot consider zipping ==> IMF is zipped anyway, and IMD is huge, so ease of access more important
		
		//	load error protocol, scoping error categories and types
		BufferedReader epBr = ((in instanceof BufferedReader) ? ((BufferedReader) in) : new BufferedReader(in));
		String category = "null";
		String type = "null";
		for (String line; (line = epBr.readLine()) != null;) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			
			//	parse data
			String[] data = line.split("\\t");
			if (data.length < 2)
				continue;
			
			//	read error category
			if ("CATEGORY".equals(data[0])) {
				category = data[1];
				String label = getElement(data, 2, category);
				String description = getElement(data, 3, category);
				des.addErrorCategory(category, label, description);
				continue;
			}
			
			//	read error type
			if ("TYPE".equals(data[0])) {
				type = data[1];
				String label = getElement(data, 2, type);
				String description = getElement(data, 3, type);
				des.addErrorType(category, type, label, description);
				continue;
			}
			
			//	read error
			if ("ERROR".equals(data[0])) {
				String severity = data[1];
				String count = data[2];
				des.addError(category, type, severity, Integer.parseInt(count), false);
			}
			else if ("FALPOS".equals(data[0])) {
				String severity = data[1];
				String count = data[2];
				des.addError(category, type, severity, Integer.parseInt(count), true);
			}
		}
		epBr.close();
	}
	private static String getElement(String[] data, int index, String def) {
		return ((index < data.length) ? data[index] : def);
	}
}
