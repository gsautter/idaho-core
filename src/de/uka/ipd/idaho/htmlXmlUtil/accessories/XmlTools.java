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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import de.uka.ipd.idaho.htmlXmlUtil.accessories.XmlValidationUtils.DtdValidator;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XmlValidationUtils.ValidationReport;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XmlValidationUtils.ValidationReport.Line;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XmlValidationUtils.XsdValidator;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XsltUtils.TransformerPool;



/**
 * Command line tool for XSLT transforming and validating XML files.
 * 
 * @author sautter
 */
public class XmlTools {
	private static void printHelpText() {
		System.out.println("XML transformation and validation tool parameters:");
		System.out.println(" - '?': print this help text and exit");
		System.out.println(" - 'CORES=<N>': set the number of CPU cores to use to <N>");
		System.out.println("                <N> = 'A' uses all available CPU cores");
		System.out.println(" - 'RECURSIVE': process input folders recursively");
		System.out.println(" - 'VERBOSE': activate verbose output");
		System.out.println(" - 'XSLT=<sxlt>': use the XSLT stylesheet found at path or URL <xslt>");
		System.out.println(" - 'XSD=<xsd>': use the MXL schema found at path or URL <xsd>");
		System.out.println(" - 'DTD=<dtdFolder>': resolve DTD entities relative to <dtdFolder>");
		System.out.println(" - 'OUT=<outMode>': how to select the output destination for each input file:");
		System.out.println("   - null: output to console");
		System.out.println("   - '*<suffix>': output XSLT transformation result or XSD or DTD validation");
		System.out.println("                  report to file '<inputFileName><suffix>' in same folder path");
		System.out.println("   - '<folder>/*': output XSLT transformation result or XSD or DTD validation");
		System.out.println("                   report to file '<folder>/<inputFilePathAndName>'");
		System.out.println("   - '*/<folder>/*': output XSLT transformation result or XSD or DTD validation");
		System.out.println("                     report to file '<inputFilePath>/<folder>/<inputFileName>'");
		System.out.println("   - '*/<reportFile>': output XSD or DTD validation reports to '<reportFile>'");
		System.out.println("                       in each individual folder");
		System.out.println("   - '<reportFile>': output all XSD or DTD validation reports to '<reportFile>'");
		System.out.println("   - '<outputFile>': output XSLT transformation result to '<outputFile>' (valid");
		System.out.println("                     for single input files only)");
		System.out.println(" - every other argument is interpreted as an input file, or as a pattern for");
		System.out.println("   input files, using '*' as a wildcard (also usable for folders, with '**'");
		System.out.println("   indicating zero to arbitrarily many folder path steps)");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
//		String[] testArgs = {
//			"E:/Projektdaten/EJT/TaxPub/*.xml",
//			"XSLT=E:/GoldenGATEv3/Plugins/PensoftTaxpubDocFormatData/pensoftTaxpub2gg.xslt",
//			"VERBOSE",
//			"OUT=*/ggXml/*",
//		};
//		args = testArgs;
		if (args.length == 0) {
			printHelpText();
			return;
		}
		
		//	read arguments
		TransformerPool xslt = null;
		DtdValidator dtd = null;
		XsdValidator xsd = null;
		ArrayList argInputFiles = new ArrayList();
		boolean inputFileRecurse = false;
		String outputMode = null; // System.out, output file suffix, per folder validation report, overall validation report
		boolean verboseLogging = false;
		boolean printHelp = false;
		int maxCores = 1;
		for (int a = 0; a < args.length; a++) {
			if (args[a].startsWith("XSLT=")) {
				String xsltAddress = args[a].substring("XSLT=".length());
				if (xsltAddress.startsWith("http://") || xsltAddress.startsWith("https://"))
					xslt = XsltUtils.getTransformer(new URL(xsltAddress));
				else if ((xsltAddress.indexOf(":/") == -1) && (xsltAddress.indexOf(":\\") == -1))
					xslt = XsltUtils.getTransformer(new File(new File("."), xsltAddress));
				else xslt = XsltUtils.getTransformer(new File(xsltAddress));
			}
			else if (args[a].startsWith("XSD=")) {
				String xsdAddress = args[a].substring("XSD=".length());
				if (xsdAddress.startsWith("http://") || xsdAddress.startsWith("https://")) {
					if (xsd == null)
						xsd = XmlValidationUtils.getXsdValidator(new URL(xsdAddress));
					xsd.setDataBaseUrl(xsdAddress.substring(0, xsdAddress.lastIndexOf("/")));
				}
				else if ((xsdAddress.indexOf(":/") == -1) && (xsdAddress.indexOf(":\\") == -1)) {
					if (xsd == null)
						xsd = XmlValidationUtils.getXsdValidator(new File(new File("."), xsdAddress));
					else {
						File xsdDataPath = new File(new File("."), xsdAddress);
						if (xsdDataPath.isFile())
							xsdDataPath = xsdDataPath.getParentFile();
						if (!xsdDataPath.exists())
							xsdDataPath.mkdirs();
						xsd.setDataPath(xsdDataPath);
					}
				}
				else {
					if (xsd == null)
						xsd = XmlValidationUtils.getXsdValidator(new File(xsdAddress));
					else {
						File xsdDataPath = new File(xsdAddress);
						if (xsdDataPath.isFile())
							xsdDataPath = xsdDataPath.getParentFile();
						if (!xsdDataPath.exists())
							xsdDataPath.mkdirs();
						xsd.setDataPath(xsdDataPath);
					}
				}
			}
			else if (args[a].startsWith("DTD=")) {
				String dtdAddress = args[a].substring("DTD=".length());
				if (dtdAddress.startsWith("http://") || dtdAddress.startsWith("https://")) {
					if (dtd == null)
						dtd = new DtdValidator();
					dtd.setDataBaseUrl(dtdAddress.substring(0, dtdAddress.lastIndexOf("/")));
				}
				else if ((dtdAddress.indexOf(":/") == -1) && (dtdAddress.indexOf(":\\") == -1)) {
					if (dtd == null)
						dtd = new DtdValidator();
					File dtdDataPath = new File(new File("."), dtdAddress);
					if (dtdDataPath.isFile())
						dtdDataPath = dtdDataPath.getParentFile();
					if (!dtdDataPath.exists())
						dtdDataPath.mkdirs();
					dtd.setDataPath(dtdDataPath);
				}
				else {
					if (dtd == null)
						dtd = new DtdValidator();
					File dtdDataPath = new File(dtdAddress);
					if (dtdDataPath.isFile())
						dtdDataPath = dtdDataPath.getParentFile();
					if (!dtdDataPath.exists())
						dtdDataPath.mkdirs();
					dtd.setDataPath(dtdDataPath);
				}
			}
			else if ("RECURSIVE".equalsIgnoreCase(args[a]))
				inputFileRecurse = true;
			else if ("VERBOSE".equalsIgnoreCase(args[a]))
				verboseLogging = true;
			else if ("?".equals(args[a]))
				printHelp = true;
			else if (args[a].startsWith("OUT="))
				outputMode = args[a].substring("OUT=".length());
			else if (args[a].startsWith("CORES=")) {
				String cores = args[a].substring("CORES=".length());
				if (cores.length() == 0) {}
				else if ("A".equals(cores))
					maxCores = Runtime.getRuntime().availableProcessors();
				else try {
					int mc = Integer.parseInt(cores);
					if (0 < mc)
						maxCores = Math.min(mc, Runtime.getRuntime().availableProcessors());
					else maxCores = Math.max(1, (Runtime.getRuntime().availableProcessors() + mc));
				}
				catch (NumberFormatException nfe) {
					System.out.println("Invalid number of processor cores: " + cores);
					return;
				}
			}
			else {
				String inputFile = cleanInputFile(args[a]);
				if ((inputFile.indexOf(":/") == -1) && (inputFile.indexOf(":\\") == -1))
					argInputFiles.add(new InputFile(new File(new File("."), inputFile), inputFile));
				else argInputFiles.add(new InputFile(new File(inputFile), inputFile));
			}
		}
		if (printHelp) {
			printHelpText();
			return;
		}
		
		//	check output mode (part 1)
		if ((xslt != null) && (outputMode != null) && outputMode.startsWith("*/") && !outputMode.endsWith("/*")) {
			System.out.println("Cannot use per-folder output in XSLT mode");
			return;
		}
		
		//	default to DTD validation if no XSLT or XSD specified
		if ((xslt == null) && (xsd == null) && (dtd == null))
			dtd = new DtdValidator(new File("."));
		
		//	shut up transformer complaints (especially about DTD)
		if (xslt != null) {
			xslt.setErrorListener(new LenientErrorListener(verboseLogging));
			xslt.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}
		
		//	run through target files
		ArrayList inputFiles = new ArrayList() {
			HashSet inputFileNames = new HashSet();
			public boolean add(Object e) {
				if (this.inputFileNames.add(((InputFile) e).name))
					return super.add(e);
				else return false;
			}
		};
		for (int i = 0; i < argInputFiles.size(); i++)
			addInputFiles(((InputFile) argInputFiles.get(i)), inputFiles, outputMode, verboseLogging, inputFileRecurse);
		
		//	check output mode (part 2)
		if ((xslt != null) && (outputMode != null) && (inputFiles.size() > 1) && !outputMode.startsWith("*") && !outputMode.endsWith("/*")) {
			System.out.println("Cannot use single-file output in XSLT mode");
			return;
		}
		
		//	set validator verbosity
		if (xsd != null)
			xsd.setVerbose(verboseLogging);
		if (dtd != null)
			dtd.setVerbose(verboseLogging);
		
		//	process target files
		maxCores = Math.min(maxCores, inputFiles.size());
		HashMap vrWriters = new HashMap();
		boolean fullReport = ((xslt == null) && ((inputFiles.size() == 1) || ((outputMode != null) && ((outputMode.startsWith("*") && !outputMode.startsWith("*/")) || outputMode.endsWith("/*")))));
		if (maxCores == 1) {
			for (int i = 0; i < inputFiles.size(); i++) {
				InputFile inputFile = ((InputFile) inputFiles.get(i));
				System.out.println("[" + (i+1) + "/" + inputFiles.size() + "]: " + inputFile.name);
				handleInputFile(((InputFile) inputFiles.get(i)), xslt, dtd, xsd, outputMode, verboseLogging, vrWriters, fullReport);
			}
		}
		else {
			Thread[] threads = new Thread[maxCores];
			for (int t = 0; t < threads.length; t++)
				threads[t] = new XmlToolThread(inputFiles, xslt, dtd, xsd, outputMode, verboseLogging, vrWriters, fullReport);
			for (int t = 0; t < threads.length; t++)
				threads[t].start();
			for (int t = 0; t < threads.length; t++) try {
				threads[t].join();
			}
			catch (InterruptedException ie) {
				t--;
			}
		}
		
		//	close group output writers
		for (Iterator fit = vrWriters.keySet().iterator(); fit.hasNext();) {
			BufferedWriter out = ((BufferedWriter) vrWriters.get(fit.next()));
			out.close();
		}
	}
	
	private static String cleanInputFile(String inputFile) {
		if (inputFile.indexOf("://") != -1)
			return inputFile; // no messing with URLs
		inputFile = inputFile.replace('\\', '/');
		inputFile = inputFile.replaceAll("[\\/]{2,}", "/"); // normalize backslashes
		inputFile = inputFile.replaceAll("\\*\\*\\/(\\*\\*?\\/)+", "**/"); // replace folder wildcard sequences
		//	TODO anything else ???
		return inputFile;
	}
	
	private static class LenientErrorListener implements ErrorListener {
		private boolean verbose;
		LenientErrorListener(boolean verbose) {
			this.verbose = verbose;
		}
		public void warning(TransformerException exception) throws TransformerException {
			if (this.verbose)
				System.out.println("Transformer warning: " + exception.getMessageAndLocation());
		}
		public void error(TransformerException exception) throws TransformerException {
			if (this.verbose)
				System.out.println("Transformer error: " + exception.getMessageAndLocation());
		}
		public void fatalError(TransformerException exception) throws TransformerException {
			if (this.verbose)
				System.out.println("Transformer fatal error: " + exception.getMessageAndLocation());
		}
	}
	
	private static void addInputFiles(InputFile argInputFile, ArrayList inputFiles, String outputMode, boolean verbose, boolean recurse) {
		addInputFiles(argInputFile, "", null, inputFiles, outputMode, verbose, recurse);
	}
	
	private static void addInputFiles(InputFile argInputFile, String inputFileNamePrefix, final String inputFileNamePattern, ArrayList inputFiles, String outputMode, boolean verbose, final boolean recurse) {
		
		//	single specific input file
		if (argInputFile.file.exists() && argInputFile.file.isFile()) {
			inputFiles.add(argInputFile);
			return;
		}
		
		//	input folder
		if (argInputFile.file.exists() && argInputFile.file.isDirectory()) {
			File[] files = argInputFile.file.listFiles(new FileFilter() {
				public boolean accept(File file) {
					if (file.isDirectory())
						return recurse;
					else return ((inputFileNamePattern == null) || file.getName().matches(inputFileNamePattern));
				}
			});
			for (int f = 0; f < files.length; f++) {
				if (files[f].isDirectory() && recurse)
					addInputFiles(new InputFile(files[f], (inputFileNamePrefix + files[f].getName())), (inputFileNamePrefix + files[f].getName() + "/"), inputFileNamePattern, inputFiles, outputMode, verbose, recurse);
				else if (files[f].isFile()) {
					String fileName;
					if ((outputMode != null) && outputMode.startsWith("*/"))
						fileName = files[f].getName();
					else fileName = (inputFileNamePrefix + files[f].getName());
					inputFiles.add(new InputFile(files[f], fileName));
				}
			}
			return;
		}
		
		//	handle wildcard names
		File wildcardRootFolder;
		String wildcardPrefix;
		String wildcardSuffix;
		boolean wildcardIsFolder;
		boolean wildcardRecurse;
		
		//	folder wildcard, recursive
		if (argInputFile.name.startsWith("**/") || ((argInputFile.name.indexOf("/**/") != -1) && ((argInputFile.name.indexOf("/*/") == -1) || (argInputFile.name.indexOf("/**/") < argInputFile.name.indexOf("/*/"))))) {
			if (argInputFile.name.startsWith("**/")) {
				wildcardRootFolder = new File(".");
				wildcardPrefix = "";
			}
			else if (argInputFile.name.startsWith("/") || (argInputFile.name.indexOf(":/") != -1) || (argInputFile.name.indexOf(":\\") != -1)) {
				wildcardRootFolder = new File(argInputFile.name.substring(0, argInputFile.name.indexOf("/**/")));
				wildcardPrefix = argInputFile.name.substring(0, argInputFile.name.indexOf("/**/"));
			}
			else {
				wildcardRootFolder = new File(new File("."), argInputFile.name.substring(0, argInputFile.name.indexOf("/**/")));
				wildcardPrefix = argInputFile.name.substring(0, argInputFile.name.indexOf("/**/"));
			}
			wildcardSuffix = argInputFile.name.substring(argInputFile.name.indexOf("**/") + "**/".length());
			wildcardIsFolder = true;
			wildcardRecurse = true;
		}
		
		//	folder wildcard, single level
		else if (argInputFile.name.startsWith("*/") || ((argInputFile.name.indexOf("/*/") != -1) && ((argInputFile.name.indexOf("/**/") == -1) || (argInputFile.name.indexOf("/*/") < argInputFile.name.indexOf("/**/"))))) {
			if (argInputFile.name.startsWith("*/")) {
				wildcardRootFolder = new File(".");
				wildcardPrefix = "";
			}
			else if (argInputFile.name.startsWith("/") || (argInputFile.name.indexOf(":/") != -1) || (argInputFile.name.indexOf(":\\") != -1)) {
				wildcardRootFolder = new File(argInputFile.name.substring(0, argInputFile.name.indexOf("/*/")));
				wildcardPrefix = argInputFile.name.substring(0, argInputFile.name.indexOf("/*/"));
			}
			else {
				wildcardRootFolder = new File(new File("."), argInputFile.name.substring(0, argInputFile.name.indexOf("/*/")));
				wildcardPrefix = argInputFile.name.substring(0, argInputFile.name.indexOf("/*/"));
			}
			wildcardSuffix = argInputFile.name.substring(argInputFile.name.indexOf("*/") + "*/".length());
			wildcardIsFolder = true;
			wildcardRecurse = false;
		}
		
		//	file name wildcard only
		else if (argInputFile.name.lastIndexOf("/") != -1) {
			if (argInputFile.name.startsWith("/") || (argInputFile.name.indexOf(":/") != -1) || (argInputFile.name.indexOf(":\\") != -1))
				wildcardRootFolder = new File(argInputFile.name.substring(0, argInputFile.name.lastIndexOf("/")));
			else wildcardRootFolder = new File(new File("."), argInputFile.name.substring(0, argInputFile.name.indexOf("/")));
			wildcardPrefix = argInputFile.name.substring(0, argInputFile.name.lastIndexOf("/"));
			wildcardSuffix = argInputFile.name.substring(argInputFile.name.lastIndexOf("/") + "/".length());
			wildcardIsFolder = false;
			wildcardRecurse = false;
		}
		
		//	no folder prefix, just file name
		else {
			wildcardRootFolder = new File(".");
			wildcardPrefix = "";
			wildcardSuffix = argInputFile.name;
			wildcardIsFolder = false;
			wildcardRecurse = false;
		}
		
		if (verbose) {
			System.out.println("Wildcard data:");
			System.out.println(" - path: " + argInputFile.name);
			System.out.println(" - root folder: " + wildcardRootFolder);
			System.out.println(" - prefix: " + wildcardPrefix);
			System.out.println(" - suffix: " + wildcardSuffix);
		}
		
		//	process wildcard
		File[] files = wildcardRootFolder.listFiles();
		if (files == null) {
			//	TODOnot report error ==> we can easily be in wildcard sub path that doesn't exist ...
			return;
		}
		
		//	create folder and file name patterns
		String wildcardFolderNamePattern = ((wildcardSuffix.indexOf("/") == -1) ? null : createFileNamePattern(wildcardSuffix.substring(0, wildcardSuffix.indexOf("/"))));
		if (verbose) System.out.println(" - folder pattern: " + wildcardFolderNamePattern);
		String wildcardFileNamePattern = createFileNamePattern((wildcardSuffix.indexOf("/") == -1) ? wildcardSuffix : wildcardSuffix.substring(wildcardSuffix.lastIndexOf("/") + "/".length()));
		if (verbose) System.out.println(" - file pattern: " + wildcardFileNamePattern);
		
		//	process starting from root folder
		for (int f = 0; f < files.length; f++) {
			if (verbose) {
				System.out.println(" - inspecting " + files[f].getAbsolutePath());
				System.out.println("   name: " + files[f].getName());
			}
			if (files[f].isDirectory() && wildcardIsFolder) {
				//	wildcard can be empty
				if (wildcardRecurse && (wildcardFolderNamePattern != null) && files[f].getName().matches(wildcardFolderNamePattern)) {
					String fileName = (wildcardPrefix + "/" + files[f].getName() + wildcardSuffix.substring(wildcardSuffix.indexOf("/")));
					addInputFiles(new InputFile(new File(files[f],  wildcardSuffix.substring(wildcardSuffix.indexOf("/") + "/".length())), fileName), (fileName + "/"), wildcardFileNamePattern, inputFiles, outputMode, verbose, (recurse || wildcardRecurse));
				}
				//	wildcard match can continue after current folder
				if (wildcardRecurse) {
					String fileName = (wildcardPrefix + "/" + files[f].getName() + "/**/" + wildcardSuffix);
					addInputFiles(new InputFile(new File(files[f], ("**/" + wildcardSuffix)), fileName), (fileName + "/"), null, inputFiles, outputMode, verbose, (recurse || wildcardRecurse));
				}
				//	wildcard matched by current folder
				String fileName = (wildcardPrefix + "/" + files[f].getName() + "/" + wildcardSuffix);
				addInputFiles(new InputFile(new File(files[f], wildcardSuffix), fileName), (fileName + "/"), wildcardFileNamePattern, inputFiles, outputMode, verbose, (recurse || wildcardRecurse));
			}
			else if (files[f].isFile() && (!wildcardIsFolder || (wildcardRecurse && (wildcardFolderNamePattern == null))) && files[f].getName().matches(wildcardFileNamePattern)) {
				String fileName;
				if ((outputMode != null) && outputMode.startsWith("*/"))
					fileName = files[f].getName();
				else fileName = (wildcardPrefix + "/" + files[f].getName());
				inputFiles.add(new InputFile(files[f], fileName));
			}
		}
	}
	
	private static String createFileNamePattern(String raw) {
		StringBuffer pattern = new StringBuffer();
		for (int c = 0; c < raw.length(); c++) {
			char ch = raw.charAt(c);
			if (Character.isLetterOrDigit(ch))
				pattern.append(ch);
			else if (ch == '*')
				pattern.append("[^\\x2F\\x5C]+");
			else pattern.append("\\" + ch);
		}
		return pattern.toString();
	}
	
	private static class InputFile {
		final File file;
		final String name;
		InputFile(File file, String name) {
			this.file = file;
			this.name = name;
		}
	}
	
	private static class XmlToolThread extends Thread {
		private ArrayList inputFiles;
		private int inputFileCount;
		private Transformer xslt;
		private DtdValidator dtd;
		private XsdValidator xsd;
		private String outputMode;
		private boolean verbose;
		private HashMap vrWriters;
		private boolean fullReport;
		XmlToolThread(ArrayList inputFiles, Transformer xslt, DtdValidator dtd, XsdValidator xsd, String outputMode, boolean verbose, HashMap vrWriters, boolean fullReport) {
			this.inputFiles = inputFiles;
			this.inputFileCount = this.inputFiles.size();
			this.xslt = xslt;
			this.dtd = dtd;
			this.xsd = xsd;
			this.outputMode = outputMode;
			this.verbose = verbose;
			this.vrWriters = vrWriters;
			this.fullReport = fullReport;
		}
		public void run() {
			while (this.inputFiles.size() != 0) {
				InputFile inputFile;
				int inputFileNumber;
				synchronized (this.inputFiles) {
					if (this.inputFiles.isEmpty())
						return;
					inputFile = ((InputFile) this.inputFiles.remove(0));
					inputFileNumber = (this.inputFileCount - this.inputFiles.size());
				}
				try {
					System.out.println("[" + inputFileNumber + "/" + this.inputFileCount + "]: " + inputFile.name);
					handleInputFile(inputFile, this.xslt, this.dtd, this.xsd, this.outputMode, this.verbose, this.vrWriters, this.fullReport);
				}
				catch (Exception e) {
					System.out.println("Error processing file '" + inputFile.name + "': " + e.getMessage());
					e.printStackTrace(System.out);
				}
			}
		}
	}
	
	private static void handleInputFile(InputFile inputFile, Transformer xslt, DtdValidator dtd, XsdValidator xsd, String outputMode, boolean verbose, HashMap vrWriters, boolean fullReport) throws Exception {
		BufferedWriter out;
		boolean closeOut;
		
		//	console output
		if (outputMode == null) {
			out = new BufferedWriter(new OutputStreamWriter(System.out));
			closeOut = false;
		}
		
		//	output to local sub folder of input folder
		else if (outputMode.startsWith("*/") && outputMode.endsWith("/*") && (outputMode.length() > "*/*".length())) {
			File outputFolder = new File(inputFile.file.getParentFile(), outputMode.substring("*/".length(), (outputMode.length() - "/*".length())));
			outputFolder.mkdirs();
			File outputFile = new File(outputFolder, inputFile.file.getName());
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			closeOut = true;
		}
		
		//	output to sub folder of input root folder
		else if (outputMode.endsWith("/*") && inputFile.name.startsWith("./")) {
			File outputFolder = new File(new File("."), outputMode.substring(0, (outputMode.length() - "/*".length())));
			File outputFile = new File(outputFolder, inputFile.name.substring("./".length()));
			outputFile.getParentFile().mkdirs();
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			closeOut = true;
		}
		
		//	output to local sub folder of input folder (for start from absolute path)
		else if (outputMode.endsWith("/*")) {
			File outputFolder = new File(inputFile.file.getParentFile(), outputMode.substring(0, (outputMode.length() - "/*".length())));
			outputFolder.mkdirs();
			File outputFile = new File(outputFolder, inputFile.file.getName());
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			closeOut = true;
		}
		
		//	per-folder validation report file
		else if (outputMode.startsWith("*/") && (xslt == null)) {
			String outputFileName = (inputFile.file.getParentFile().getAbsolutePath() + outputMode.substring("*".length()));
			synchronized (vrWriters) {
				out = ((BufferedWriter) vrWriters.get(outputFileName));
				if (out == null) {
					File outputFile = new File(outputFileName);
					out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
					vrWriters.put(outputFileName, out);
				}
			}
			closeOut = false;
		}
		
		//	input file name plus suffix
		else if (outputMode.startsWith("*")) {
			File outputFile = new File(inputFile.file.getParentFile(), (inputFile.file.getName() + outputMode.substring("*".length())));
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			closeOut = true;
		}
		
		//	fixed single output file
		else {
			String outputFileName = outputMode;
			synchronized (vrWriters) {
				out = ((BufferedWriter) vrWriters.get(outputFileName));
				if (out == null) {
					File outputFile = new File(outputFileName);
					out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
					vrWriters.put(outputFileName, out);
				}
			}
			closeOut = false;
		}
		
		//	create input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile.file), "UTF-8"));
		
		//	run XSLT
		if (xslt != null)
			transform(in, xslt, out, verbose);
		else if (xsd != null)
			validate(in, inputFile.name, xsd, out, fullReport);
		else if (dtd != null)
			validate(in, inputFile.name, dtd, out, fullReport);
		
		//	clean up
		in.close();
		out.flush();
		if (closeOut)
			out.close();
	}
	
	private static void transform(Reader in, Transformer xslt, BufferedWriter out, boolean verbose) throws Exception {
		/* filter out DOCTYPE declaration (cannot seem to keep transformer from
		 * validating, and from throwing entity resolving errors past auspice
		 * of error handler ...) */
		char[] lookaheadBuffer = new char[1024];
		in.mark(lookaheadBuffer.length);
		int lookaheadLength = in.read(lookaheadBuffer);
		in.reset();
		String lookahead = new String(lookaheadBuffer, 0, lookaheadLength);
		if (verbose) System.out.println("Got lookahead: " + lookahead);
		if (lookahead.indexOf("<!DOCTYPE ") != -1) {
			int docTypeStart = lookahead.indexOf("<!DOCTYPE ");
			int docTypeEnd = lookahead.indexOf(">", (docTypeStart + "<!DOCTYPE ".length()));
			while ((docTypeEnd < lookahead.length()) && (lookahead.charAt(docTypeEnd) != '<'))
				docTypeEnd++;
			in.skip(docTypeEnd);
			if (verbose) System.out.println("Skipped " + lookahead.substring(0, docTypeEnd));
		}
		
		//	perform actual transformation
		xslt.transform(new StreamSource(in), new StreamResult(out));
	}
	
	private static void validate(Reader in, String inputName, DtdValidator dtd, BufferedWriter out, boolean fullReport) throws Exception {
		ValidationReport vr = dtd.validateXml(in);
		writeValidationReport(inputName, vr, out, fullReport);
	}
	
	private static void validate(Reader in, String inputName, XsdValidator xsd, BufferedWriter out, boolean fullReport) throws Exception {
		ValidationReport vr = xsd.validateXml(in);
		synchronized (out) {
			writeValidationReport(inputName, vr, out, fullReport);
		}
	}
	
	private static void writeValidationReport(String inputName, ValidationReport vr, BufferedWriter out, boolean fullReport) throws Exception {
		if (fullReport)
			vr.writeReport(out);
		else if (vr.hasErrorOrWarnings()) {
			out.write(inputName + ": not valid");
			out.newLine();
			for (int l = 0; l < vr.getLineCount(); l++) {
				Line vrl = vr.getLine(l);
				if (vrl.hasErrorOrWarnings())
					vrl.writeReport(out);
			}
		}
		else {
			out.write(inputName + ": valid");
			out.newLine();
		}
	}
}
