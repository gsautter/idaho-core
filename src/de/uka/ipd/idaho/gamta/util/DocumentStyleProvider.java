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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Anchor;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;

/**
 * Document style provider that loads a list of documents style templates from a
 * URL, and then loads the templates proper.<br/>
 * The list has to be in TSV format with column names in the first row, and it
 * has to include at least the ID and the name of the individual document style
 * templates, to be loaded into a <code>DocumentList</code>. The individual
 * document style templates have to be in the same URL path as the list, and
 * have to be accessible either via the names or the IDs in the provided list.<br/>
 * Like the list proper, the individual document styles have to use a tab
 * separated format, with two columns holding keys and values, respectively, as
 * produced by the <code>DocumentStyle.writeData()</code> method.<br/>
 * Alternatively, the document style templates can also be loaded from a folder
 * in the local file system, and the contents of a folder can be updated with
 * the document style templates loaded from a URL.
 * 
 * @author sautter
 */
public class DocumentStyleProvider implements DocumentStyle.Provider {
	private String docStyleListUrl;
	private File docStyleFolder;
	private String docStyleFileSuffix;
	private Map docStylesByName = Collections.synchronizedMap(new TreeMap(String.CASE_INSENSITIVE_ORDER));
	
	/** Constructor
	 * @param docStyleListUrl the URL to load the document style template list from
	 */
	public DocumentStyleProvider(String docStyleListUrl) {
		this(docStyleListUrl, null, null);
	}
	
	/** Constructor
	 * @param docStyleFolder the folder document style templates are stored in
	 * @param docStyleFileSuffix the file name suffix to recognize document
	 *            style template files by (null deactivates file filtering)
	 */
	public DocumentStyleProvider(File docStyleFolder, String docStyleFileSuffix) {
		this(null, docStyleFolder, docStyleFileSuffix);
	}
	
	/** Constructor
	 * @param docStyleListUrl the URL to load the document style template list from
	 * @param docStyleFolder the folder to store document style templates in
	 * @param docStyleFileSuffix the file name suffix to recognize document
	 *            style template files by (null deactivates file filtering)
	 */
	public DocumentStyleProvider(String docStyleListUrl, File docStyleFolder, String docStyleFileSuffix) {
		this.docStyleListUrl = docStyleListUrl;
		this.docStyleFolder = docStyleFolder;
		this.docStyleFileSuffix = ((docStyleFileSuffix == null) ? "" : docStyleFileSuffix);
	}
	
	/**
	 * Initialize the document style provider. This method loads the list of
	 * document style templates from the URL handed to the constructor (if
	 * any), downloads the individual document style templates, and stores them
	 * in the folder handed to the constructor (if any). Finally, it registers
	 * the provider with <code>DocumentStyle</code> to make it available for
	 * template retrieval.
	 * @throws IOException
	 */
	public void init() throws IOException {
		
		//	we work without a local storage folder
		if (this.docStyleFolder == null)
			this.loadDocumentStylesFromUrl();
		
		//	we have a local storage folder
		else {
			
			//	load document styles from folder
			File[] docStyleFiles = this.docStyleFolder.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.getName().endsWith(docStyleFileSuffix);
				}
			});
			for (int s = 0; s < docStyleFiles.length; s++) {
				String docStyleName = docStyleFiles[s].getName();
				docStyleName = docStyleName.substring(0, (docStyleName.length() - this.docStyleFileSuffix.length()));
				if (this.docStylesByName.containsKey(docStyleName))
					continue;
				
				BufferedReader dsdBr = new BufferedReader(new InputStreamReader(new FileInputStream(docStyleFiles[s]), "UTF-8"));
				DocumentStyle.Data docStyleData = this.readDocumentStyleData(dsdBr, false);
				dsdBr.close();
				
				DocumentStyle docStyle = this.wrapDocumentStyleData(docStyleData);
				this.docStylesByName.put(docStyleName, new DocumentStyleTray(docStyle));
			}
			
			//	try updating from URL if given
			if (this.docStyleListUrl != null) try {
				this.loadDocumentStylesFromUrl();
			}
			catch (IOException ioe) {
				ioe.printStackTrace(System.out);
			}
		}
		
		//	register as document style provider (but only now that we got all we need)
		DocumentStyle.addProvider(this);
	}
	
	private void loadDocumentStylesFromUrl() throws IOException {
		
		//	get list of document styles
		BufferedReader dslBr = new BufferedReader(new InputStreamReader((new URL(this.docStyleListUrl)).openStream(), "UTF-8"));
		DocumentList dsl = this.readDocumentStyleList(dslBr);
		
		//	load and index document styles
		while (dsl.hasNextDocument()) {
			DocumentListElement dle = dsl.getNextDocument();
			String docStyleId = ((String) dle.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
			if (docStyleId == null)
				continue;
			String docStyleName = ((String) dle.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
			if (docStyleName == null)
				continue;
			
			long docStyleLastMod = Long.parseLong((String) dle.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
			DocumentStyleTray localDocStyleTray = ((DocumentStyleTray) this.docStylesByName.get(docStyleName));
			if (localDocStyleTray != null) {
				long localDocStyleLastMod = Long.parseLong((String) localDocStyleTray.docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
				if (docStyleLastMod <= localDocStyleLastMod)
					continue;
			}
			
			File docStyleFile = null;
			String docStyleFileName = null;
			if (this.docStyleFolder != null) {
				docStyleFileName = docStyleName;
				if (!docStyleFileName.endsWith(this.docStyleFileSuffix))
					docStyleFileName = (docStyleFileName + this.docStyleFileSuffix);
				docStyleFile = new File(this.docStyleFolder, docStyleName);
			}
			
			DocumentStyle.Data docStyleData = null;
			if (docStyleData == null) try {
				BufferedReader dsdBr = new BufferedReader(new InputStreamReader((new URL(this.getDocumentStyleDataUrl(this.docStyleListUrl, docStyleId, true))).openStream(), "UTF-8"));
				docStyleData = this.readDocumentStyleData(dsdBr, true);
				dsdBr.close();
			}
			catch (IOException ioe) {
				System.out.println("Failed to load data for document style '" + docStyleId + ":" + docStyleName + "': " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
			if (docStyleData == null) try {
				BufferedReader dsdBr = new BufferedReader(new InputStreamReader((new URL(this.getDocumentStyleDataUrl(this.docStyleListUrl, docStyleName, false))).openStream(), "UTF-8"));
				docStyleData = this.readDocumentStyleData(dsdBr, true);
				dsdBr.close();
			}
			catch (IOException ioe) {
				System.out.println("Failed to load data for document style '" + docStyleId + ":" + docStyleName + "': " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
			if (docStyleData == null)
				continue;
			
			DocumentStyle docStyle = this.wrapDocumentStyleData(docStyleData);
			this.docStylesByName.put(docStyleName, new DocumentStyleTray(docStyle));
			if (docStyleFile == null)
				continue;
			
			if (docStyleFile.exists()) {
				docStyleFile.renameTo(new File(docStyleFile.getAbsolutePath() + "." + docStyleLastMod + ".old"));
				docStyleFile = new File(this.docStyleFolder, docStyleFileName);
			}
			BufferedWriter dsdBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.docStyleFolder, docStyleFileName)), "UTF-8"));
			this.writeDocumentStyleData(docStyleData, dsdBw);
			dsdBw.flush();
			dsdBw.close();
			if (docStyleLastMod != -1)
				docStyleFile.setLastModified(docStyleLastMod);
		}
	}
	
	/**
	 * Read a list of document style templates from a reader, at least holding
	 * the ID and name of the document style templates in each element. URL
	 * download is first attempted via the ID, and then by name, if the ID
	 * based download fails (e.g. with an HTTP 404 'Not Found' error). This
	 * default implementation expects a list of tab separated values, looping
	 * through to <code>DocumentList.readDocumentList()</code>. Subclasses are
	 * free to overwrite this method, e.g. to support document lists using
	 * different formats.
	 * @param br the reader to read from
	 * @return the list of document style templates
	 * @throws IOException
	 */
	protected DocumentList readDocumentStyleList(BufferedReader br) throws IOException {
		return DocumentList.readDocumentList(br);
	}
	
	/**
	 * Create the URL to download the data object underlying a document style
	 * template from, given the URL of the document style list (as handed to
	 * the constructor of this class) and the document style ID or name (as
	 * contained in the elements of the document style list returned by the
	 * <code>readDocumentStyleList()</code> method). This default
	 * implementations truncates the argument list URL after the last slash and
	 * appends the argument ID or name. Subclasses are free to overwrite this
	 * method, e.g. to support other URL creation schemas.
	 * @param docStyleListUrl the URL of the document style list
	 * @param docStyleIdOrName the document stale ID or name
	 * @param isId are we dealing with a document style ID or a name?
	 * @return the URL to download the document style data object from
	 */
	protected String getDocumentStyleDataUrl(String docStyleListUrl, String docStyleIdOrName, boolean isId) {
		docStyleListUrl = docStyleListUrl.substring(0, (docStyleListUrl.lastIndexOf("/") + "/".length()));
		return (docStyleListUrl + docStyleIdOrName);
	}
	
	/**
	 * Read an object holding the data underlying a document style template
	 * from a reader. This default implementation loops through to
	 * <code>DocumentStyle.readDocumentStyleData()</code>, expecting the two
	 * column tab separated format output by
	 * <code>DocumentStyle.writeData()</code>. Subclasses are free to overwrite
	 * this method, e.g. to support other serialization formats. The argument
	 * URL/file indicator facilitates supporting two different input formats,
	 * depending on the type of source.
	 * @param br the reader to read from
	 * @param isUrl is the argument reader wrapped around a URL or a file?
	 * @return the document style template data object
	 * @throws IOException
	 */
	protected DocumentStyle.Data readDocumentStyleData(BufferedReader br, boolean isUrl) throws IOException {
		return DocumentStyle.readDocumentStyleData(br);
	}
	
	/**
	 * Write a document style data object to a writer, e.g. for local storage
	 * in a file. This default implementation loops through to
	 * <code>DocumentStyle.writeData()</code>, producing the two column tab
	 * separated format expected by
	 * <code>DocumentStyle.readDocumentStyleData()</code>. Subclasses are free
	 * to overwrite this method, e.g. to store the data in a different
	 * serialization format. Subclasses that do overwrite this method should
	 * also overwrite <code>readDocumentStyleData()</code> to support
	 * whichever format they use when reading a document style data object,
	 * from e.g. a file.
	 * @param data the document style data object to write
	 * @param bw the writer to write to
	 * @throws IOException
	 */
	protected void writeDocumentStyleData(DocumentStyle.Data data, BufferedWriter bw) throws IOException {
		DocumentStyle.writeData(data, bw);
	}
	
	/**
	 * Wrap an actual document style template around its underlying data. This
	 * default implementation returns a simple <code>DocumentStyle</code>
	 * object. Subclasses are free to overwrite this method, e.g. to use more
	 * specialized subclasses of <code>DocumentStyle</code>.
	 * @param docStyleData the document style data to wrap
	 * @return the document style wrapped around the argument data
	 */
	protected DocumentStyle wrapDocumentStyleData(DocumentStyle.Data data) {
		return new DocumentStyle(data);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Provider#getStyleFor(de.uka.ipd.idaho.gamta.Attributed)
	 */
	public DocumentStyle getStyleFor(Attributed doc) {
		
		//	use name for cache lookup if given
		String docStyleName = ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
		if ((docStyleName != null) && this.docStylesByName.containsKey(docStyleName))
			return ((DocumentStyleTray) this.docStylesByName.get(docStyleName)).docStyle;
		
		//	use anchors to find (best) matching style
		ArrayList docStyleMatches = new ArrayList();
		for (Iterator dsnit = this.docStylesByName.keySet().iterator(); dsnit.hasNext();) {
			docStyleName = ((String) dsnit.next());
			DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStylesByName.get(docStyleName));
			System.out.println("Testing " + docStyleName + " with " + docStyleTray.anchors.length + " anchors");
			DocumentStyleMatch docStyleMatch = docStyleTray.matchAgainst(doc);
			if (docStyleMatch != null) {
				docStyleMatches.add(docStyleMatch);
				System.out.println(" ==> matched " + docStyleMatch.anchorsMatched + " of " + docStyleMatch.anchorsTested + " anchors");
			}
		}
		if (docStyleMatches.isEmpty())
			return null; // nothing to work with ...
		
		//	return best matching style
		Collections.sort(docStyleMatches);
//		return ((DocumentStyleMatch) docStyleMatches.get(0)).docStyle;
		DocumentStyle docStyle = ((DocumentStyleMatch) docStyleMatches.get(0)).docStyle;
		System.out.println("Found " + docStyle);
		return docStyle;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Provider#documentStyleAssigned(de.uka.ipd.idaho.gamta.util.DocumentStyle, de.uka.ipd.idaho.gamta.Attributed)
	 */
	public void documentStyleAssigned(DocumentStyle docStyle, Attributed doc) {
		if (docStyle.hasAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE))
			doc.setAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE, docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE));
	}
	
	private static class DocumentStyleTray {
		Anchor[] anchors;
		DocumentStyle docStyle;
		DocumentStyleTray(DocumentStyle docStyle) {
			this.docStyle = docStyle;
			this.anchors = Anchor.getAnchors(this.docStyle);
		}
		DocumentStyleMatch matchAgainst(Attributed doc) {
			int anchorsMatched = 0;
			for (int a = 0; a < this.anchors.length; a++) {
				System.out.println(" - " + this.anchors[a].name);
				if (this.anchors[a].matches(doc)) {
					System.out.println("   ==> match");
					anchorsMatched++;
				}
				else if (this.anchors[a].isRequired) {
					System.out.println("   ==> mismatch on required");
					return null;
				}
				else System.out.println("   ==> mismatch");
			}
			return ((anchorsMatched == 0) ? null : new DocumentStyleMatch(this.docStyle, this.anchors.length, anchorsMatched));
		}
	}
	
	private static class DocumentStyleMatch implements Comparable {
		DocumentStyle docStyle;
		int anchorsTested;
		int anchorsMatched;
		DocumentStyleMatch(DocumentStyle docStyle, int anchorsTested, int anchorsMatched) {
			this.docStyle = docStyle;
			this.anchorsTested = anchorsTested;
			this.anchorsMatched = anchorsMatched;
		}
		public int compareTo(Object obj) {
			if (obj == this)
				return 0;
			DocumentStyleMatch dsm = ((DocumentStyleMatch) obj);
//			int c = ((dsm.anchorsMatched / dsm.anchorsTested) - (this.anchorsMatched / this.anchorsTested)); // descending order by match percentage
			int c = ((dsm.anchorsMatched * this.anchorsTested) - (this.anchorsMatched * dsm.anchorsTested)); // descending order by match percentage
			if (c != 0)
				return c;
			c = (dsm.anchorsTested - this.anchorsTested); // descending order by number of anchors
			if (c != 0)
				return c;
			return 0;
		}
	}
//	
//	public static void main(String[] args) throws Exception {
//		DocumentStyleProvider dsp = new DocumentStyleProvider("http://tb.plazi.org/GgServer/DocumentStyles/list.txt");
//		dsp.init();
////		ImDocumentStyle.getStyleFor(null);
////		StandaloneDocumentStyleProvider sdsp = new StandaloneDocumentStyleProvider(new File("E:/GoldenGATEv3/Plugins/DocumentStyleProviderData"));
//		Attributed doc = new AbstractAttributed();
//////		doc.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, "ijsem.0000.journal_article.docStyle");
////		doc.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, "we_dont_have_this.docStyle");
//////		DocumentStyle ds = DocumentStyle.getStyleFor(doc);
//		DocumentStyle ds = DocumentStyle.getStyleFor(doc);
//		System.out.println(ds);
//		System.out.println(Arrays.toString(ds.getSubsetPrefixes()));
//	}
}