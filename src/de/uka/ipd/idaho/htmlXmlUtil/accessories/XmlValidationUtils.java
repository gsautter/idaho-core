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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.uka.ipd.idaho.easyIO.utilities.ApplicationHttpsEnabler;
//import de.uka.ipd.idaho.htmlXmlUtil.accessories.XsltUtils.ByteOrderMarkFilterInputStream;

/**
 * Convenience wrapper for XML validation facilities, serving both DTDs and XML
 * Schemas. The <code>DtdValidator</code> and <code>XsdValidator</code> are
 * thread safe and can be used in concurrent scenarios.
 * 
 * @author sautter
 */
public class XmlValidationUtils {
	
	/**
	 * @param args
	 */
//	public static void main(String[] args) throws Exception {
//		// webapp example xsd: 
////		URL schemaFile = new URL("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
//		//	http://java.sun.com/xml/ns/j2ee/jsp_2_0.xsd
//		File dataPath = new File("E:/Temp/TestDocs/");
//		File schemaFile = new File(dataPath, "web-app_2_4.xsd");
//		// local file example:
//		// File schemaFile = new File("/location/to/localfile.xsd"); // etc.
//		XsdValidator validator = getXsdValidator(schemaFile);
//		File xmlFile = new File("E:/GoldenGATEv3.WebApp/WEB-INF/web.xml");
//		InputStream xmlIn = new FileInputStream(xmlFile);
//		ValidationReport vr = validator.validateXml(xmlIn);
//		if (vr != null)
//			vr.printReport(new PrintWriter(System.out));
//	}
	public static void main(String[] args) throws Exception {
		ApplicationHttpsEnabler.enableHttps();
		File dataPath = new File("E:/Projektdaten/EJT/TaxPub");
		File data = new File(dataPath, "ZooKeys-1029-139.xml");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(data));
		byte[] buffer = new byte[1024];
		for (int r; (r = bin.read(buffer, 0, buffer.length)) != -1;)
			baos.write(buffer, 0, r);
		bin.close();
		DtdValidator validator = new DtdValidator(dataPath);
		validator.setDataBaseUrl("https://zookeys.pensoft.net/nlm/");
		ValidationReport vr = validator.validateXml(baos.toByteArray());
		if (vr != null)
			vr.printReport(new PrintWriter(System.out));
	}
	
	/**
	 * Produce an XSD validator from an XML schema located at a URL.
	 * @param xsdUrl the URL of the XML schema to load
	 * @return an XSD validator produced from the XML schema located at the
	 *         specified URL
	 * @throws IOException
	 */
	public static XsdValidator getXsdValidator(URL xsdUrl) throws IOException {
		return getXsdValidator(xsdUrl, true);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema located at a URL.
	 * @param xsdUrl the URL of the XML schema to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces creating the validator from the specified URL.
	 * @return an XSD validator produced from the XML schema located at the
	 *         specified URL
	 * @throws IOException
	 */
	public static XsdValidator getXsdValidator(URL xsdUrl, boolean allowCache) throws IOException {
		if (xsdUrl == null)
			return null;
		Reader xsdIn = new OnDemandReader(xsdUrl);
		try {
			return getXsdValidator(xsdUrl.toString(), xsdIn, allowCache);
		}
		finally {
			xsdIn.close();
		}
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema located in a file.
	 * @param xsdFile the file containing the XML schema to load
	 * @return an XSD validator produced from the XML schema located in the
	 *         specified file
	 * @throws IOException
	 */
	public static XsdValidator getXsdValidator(File xsdFile) throws IOException {
		return getXsdValidator(xsdFile, true);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema located in a file.
	 * @param xsdFile the file containing the XML schema to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces creating the validator from the specified file.
	 * @return an XSD validator produced from the XML schema located in the
	 *         specified file
	 * @throws IOException
	 */
	public static XsdValidator getXsdValidator(File xsdFile, boolean allowCache) throws IOException {
		if (xsdFile == null)
			return null;
		Reader xsdIn = new OnDemandReader(xsdFile);
		try {
			XsdValidator xv = getXsdValidator(xsdFile.getAbsolutePath(), xsdIn, allowCache);
			xv.setDataPath(xsdFile.getParentFile());
			return xv;
		}
		finally {
			xsdIn.close();
		}
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema located in a file or
	 * at a URL. If the specified address starts with 'http://', it is
	 * interpreted as a URL, otherwise as a file name.
	 * @param xsdAddress the address containing the XML schema to load
	 * @return an XSD validator produced from the XML schema located at the
	 *         specified address
	 * @throws IOException
	 */
	public static XsdValidator getXsdValidator(String xsdAddress) throws IOException {
		return getXsdValidator(xsdAddress, true);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema located in a file or
	 * at a URL. If the specified address starts with 'http://', it is
	 * interpreted as a URL, otherwise as a file name.
	 * @param xsdAddress the address containing the XML schema to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces creating the validator from the specified data.
	 * @return an XSD validator produced from the XML schema located at the
	 *         specified address
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String xsdAddress, boolean allowCache) throws IOException {
		Reader xsdIn;
		if (xsdAddress.startsWith("http://") || xsdAddress.startsWith("https://"))
			xsdIn = new OnDemandReader(new URL(xsdAddress));
		else xsdIn = new OnDemandReader(new File(xsdAddress));
		try {
			return getXsdValidator(xsdAddress, xsdIn, allowCache);
		}
		finally {
			xsdIn.close();
		}
	}
	
	/* this class allows us to open an actual input stream and reader only after a cache miss */
	static class OnDemandReader extends Reader {
		private final File file;
		private final URL url;
		private Reader in;
		OnDemandReader(File file) {
			this.file = file;
			this.url = null;
		}
		OnDemandReader(URL url) {
			this.file = null;
			this.url = url;
		}
		private Reader getReader() throws IOException {
			if (this.in != null)
				return this.in;
			InputStream in;
			if (this.file != null)
				in = new FileInputStream(this.file);
			else if (this.url != null)
				in = this.url.openStream();
			else return null;
			this.in = new InputStreamReader(in, "UTF-8");
			return this.in;
		}
		public int read(CharBuffer target) throws IOException {
			return this.getReader().read(target);
		}
		public int read() throws IOException {
			return this.getReader().read();
		}
		public int read(char[] cbuf) throws IOException {
			return this.getReader().read(cbuf);
		}
		public int read(char[] cbuf, int off, int len) throws IOException {
			return this.getReader().read(cbuf, off, len);
		}
		public long skip(long n) throws IOException {
			return this.getReader().skip(n);
		}
		public boolean ready() throws IOException {
			return this.getReader().ready();
		}
		public boolean markSupported() {
			try {
				return this.getReader().markSupported();
			}
			catch (IOException ioe) {
				return false;
			}
		}
		public void mark(int readAheadLimit) throws IOException {
			this.getReader().mark(readAheadLimit);
		}
		public void reset() throws IOException {
			this.getReader().reset();
		}
		public void close() throws IOException {
			if (this.in != null)
				this.in.close();
		}
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema loaded from some input
	 * stream. If the argument name is null, there are no cache lookups, and
	 * the validator will not be cached. This method reads the argument input
	 * stream through the end and closes it afterwards.
	 * @param xsdIn an input stream to load the XML schema from
	 * @param name the name of the input stream, for caching
	 * @return an XSD validator produced from the specified XML schema
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String name, InputStream xsdIn) throws IOException {
		return getXsdValidator(name, xsdIn, true);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema loaded from some input
	 * stream. If the argument name is null, there are no cache lookups, and
	 * the validator will not be cached. This method reads the argument input
	 * stream through the end and closes it afterwards.
	 * @param xsdIn an input stream to load the XML schema from
	 * @param name the name of the input stream, for caching
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces creating the validator from the specified input
	 *            stream.
	 * @return an XSD validator produced from the specified XML schema
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String name, InputStream xsdIn, boolean allowCache) throws IOException {
		if (allowCache && (name != null) && xsdValidatorCache.containsKey(name)) {
//			System.out.println("XsltUtils: XSL Transformer Pool cache hit for '" + name + "'");
			return ((XsdValidator) xsdValidatorCache.get(name));
		}
		BufferedReader xsdBr = new BufferedReader(new InputStreamReader(new ByteOrderMarkFilterInputStream(xsdIn), "UTF-8"));
		StringBuffer xsdChars = new StringBuffer();
		char[] charBuffer = new char[1024];
		for (int r; (r = xsdBr.read(charBuffer, 0, charBuffer.length)) != -1;)
			xsdChars.append(charBuffer, 0, r);
		xsdBr.close();
		return getXsdValidator(name, xsdChars.toString(), allowCache);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema loaded from some
	 * reader. If the argument name is null, there are no cache lookups, and
	 * the validator will not be cached. This method reads the argument reader
	 * through the end and closes it afterwards.
	 * @param xsdIn a reader to load the XML schema from
	 * @param name the name of the input stream, for caching
	 * @return an XSD validator produced from the specified XML schema
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String name, Reader xsdIn) throws IOException {
		return getXsdValidator(name, xsdIn, true);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema loaded from some
	 * reader. If the argument name is null, there are no cache lookups, and
	 * the validator will not be cached. This method reads the argument reader
	 * through the end and closes it afterwards.
	 * @param xsdIn a reader to load the XML schema from
	 * @param name the name of the input stream, for caching
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces creating the validator from the specified reader.
	 * @return an XSD validator produced from the specified XML schema
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String name, Reader xsdIn, boolean allowCache) throws IOException {
		if (allowCache && (name != null) && xsdValidatorCache.containsKey(name)) {
//			System.out.println("XsltUtils: XSL Transformer Pool cache hit for '" + name + "'");
			return ((XsdValidator) xsdValidatorCache.get(name));
		}
		BufferedReader xsdBr = ((xsdIn instanceof BufferedReader) ? ((BufferedReader) xsdIn) : new BufferedReader(xsdIn));
		StringBuffer xsdChars = new StringBuffer();
		char[] charBuffer = new char[1024];
		for (int r; (r = xsdBr.read(charBuffer, 0, charBuffer.length)) != -1;)
			xsdChars.append(charBuffer, 0, r);
		xsdBr.close();
		return getXsdValidator(name, xsdChars.toString(), allowCache);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema. If the argument name
	 * is null, there are no cache lookups, and the validator will not be
	 * cached.
	 * @param name the name of the input stream, for caching
	 * @param xsdData the XML schema to load
	 * @return an XSD validator produced from the specified XML schema
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String name, String xsdData) throws IOException {
		return getXsdValidator(name, xsdData, true);
	}
	
	/**
	 * Produce an XSD validator pool from an XML schema. If the argument name
	 * is null, there are no cache lookups, and the validator will not be
	 * cached.
	 * @param name the name of the input stream, for caching
	 * @param xsdData the XML schema to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces creating the validator from the specified data.
	 * @return an XSD validator produced from the specified XML schema
	 * @throws IOException
	 */
	public static synchronized XsdValidator getXsdValidator(String name, String xsdData, boolean allowCache) throws IOException {
		if (allowCache && (name != null) && xsdValidatorCache.containsKey(name))
			return ((XsdValidator) xsdValidatorCache.get(name));
		try {
			XsdValidator xv = new XsdValidator(xsdData);
			if (name != null)
				xsdValidatorCache.put(name, xv);
			return xv;
		}
		catch (Exception e) {
			throw new IOException(e.getClass().getName() + " (" + e.getMessage() + ") while creating XSL Transformer Pool from '" + name + "'.");
		}
	}
	private static HashMap xsdValidatorCache = new HashMap();
	
	/**
	 * XML Schema (XSD) based validator for XML documents. This class forms a
	 * thread safe wrapper for a pool of actual validator instances, the latter
	 * being specific to the schema wrapped by an instance of this class.
	 * 
	 * @author sautter
	 */
	public static class XsdValidator extends XmlValidator implements LSResourceResolver {
		private String schemaData;
		private LSResourceResolver resourceResolver = null;
		
		/** Constructor
		 */
		public XsdValidator(String schemaData) {
			this(schemaData, null);
		}
		
		/** Constructor
		 * @param dataPath the folder to seek and cache DTDs in
		 */
		public XsdValidator(String schemaData, File dataPath) {
			super(dataPath);
			this.schemaData = schemaData;
		}
		
		/* (non-Javadoc)
		 * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
			if (this.verbose) {
				System.out.println("Schema resolving system ID " + systemId);
				System.out.println(" - public ID is " + publicId);
				System.out.println(" - type is " + type);
				System.out.println(" - namespace URI is " + namespaceURI);
				System.out.println(" - base URI is " + baseURI);
			}
			if (this.resourceResolver != null) {
				LSInput entitySource = this.resourceResolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
				if (entitySource != null)
					return entitySource;
			}
			byte[] bytes = this.resolveToData(publicId, systemId);
			return ((bytes == null) ? null : new LSResource(bytes, systemId, publicId, baseURI));
		}
		
		private static class LSResource implements LSInput {
			private byte[] bytes;
			private String encoding = "UTF-8";
			private String systemId;
			private String publicId;
			private String baseUri;
			LSResource(byte[] bytes, String systemId, String publicId, String baseUri) {
				this.bytes = bytes;
				this.systemId = systemId;
				this.publicId = publicId;
				this.baseUri = baseUri;
			}
			public Reader getCharacterStream() {
				try {
					return new InputStreamReader(this.getByteStream(), this.encoding);
				}
				catch (Exception e) {
					return new InputStreamReader(this.getByteStream()); // never gonna happen, but Java don't know ...
				}
			}
			public void setCharacterStream(Reader characterStream) {}
			public InputStream getByteStream() {
				return new ByteArrayInputStream(this.bytes);
			}
			public void setByteStream(InputStream byteStream) {}
			public String getStringData() {
				try {
					return new String(this.bytes, this.encoding);
				}
				catch (Exception e) {
					return new String(this.bytes); // never gonna happen, but Java don't know ...
				}
			}
			public void setStringData(String stringData) {}
			public String getSystemId() {
				return this.systemId;
			}
			public void setSystemId(String systemId) {}
			public String getPublicId() {
				return this.publicId;
			}
			public void setPublicId(String publicId) {}
			public String getBaseURI() {
				return this.baseUri;
			}
			public void setBaseURI(String baseURI) {}
			public String getEncoding() {
				return this.encoding;
			}
			public void setEncoding(String encoding) {}
			public boolean getCertifiedText() {
				return false;
			}
			public void setCertifiedText(boolean certifiedText) {}
		}

		ValidationReport validateXml(ValidationData data) {
			long start = System.currentTimeMillis();
			XsdValidatorInstance xmlValidator;
			try {
				xmlValidator = this.getXmlValidator();
			}
			catch (SAXException se) {
				if (this.verbose) {
					System.out.println("Error validating XML: " + se.getMessage());
					se.printStackTrace(System.out);
				}
				return null;
			}
			if (this.verbose) System.out.println("XML validator ensured in " + (System.currentTimeMillis() - start) + "ms");
			
			start = System.currentTimeMillis();
			try {
				return xmlValidator.validate(data, this);
			}
			finally {
				this.returnXmlValidator(xmlValidator);
				if (this.verbose) System.out.println("XML validated in " + (System.currentTimeMillis() - start) + "ms");
			}
		}

		private static class XsdValidatorInstance extends XmlValidatorInstance implements ErrorHandler {
			private Validator validator;
			XsdValidatorInstance(Validator validator) {
				this.validator = validator;
			}
			void runValidation(ValidationData data, XmlValidator parent) {
				this.validator.setErrorHandler(this);
				this.validator.setResourceResolver((XsdValidator) parent);
				try {
					this.validator.validate(new StreamSource(new ByteArrayInputStream(data.xmlBytes)));
				}
				catch (IOException ioe) {
					if (parent.verbose) {
						System.out.println("Error validating XML: " + ioe.getMessage());
						System.out.println(ioe);
					}
				}
				catch (SAXException se) {
					if (parent.verbose) {
						System.out.println("Validation fatal error: " + se.getMessage());
						System.out.println(se);
					}
				}
			}
			void reset() {
				this.validator.reset();
				this.clearErrors();
			}
		}
		private Schema schema = null;
		private LinkedList xmlValidators = new LinkedList();
		private synchronized XsdValidatorInstance getXmlValidator() throws SAXException {
			if (this.xmlValidators.size() != 0)
				return ((XsdValidatorInstance) this.xmlValidators.removeFirst());
			if (this.schema == null) {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				schemaFactory.setErrorHandler(new ErrorHandler() {
					public void warning(SAXParseException spe) throws SAXException {
						if (verbose) System.out.println("Schema Warning: " + spe.getMessage());
					}
					public void error(SAXParseException spe) throws SAXException {
						if (verbose) System.out.println("Schema Error: " + spe.getMessage());
					}
					public void fatalError(SAXParseException spe) throws SAXException {
						if (verbose) System.out.println("Schema Fatal: " + spe.getMessage());
					}
				});
				schemaFactory.setResourceResolver(this);
				this.schema = schemaFactory.newSchema(new StreamSource(new StringReader(this.schemaData)));
			}
			return new XsdValidatorInstance(this.schema.newValidator());
		}
		private synchronized void returnXmlValidator(XsdValidatorInstance xmlValidator) {
			xmlValidator.reset();
			this.xmlValidators.addLast(xmlValidator);
		}
	}
	
	/*
TODO Maybe facilitate statically registering centralized resource resolvers
==> allows providing resource from single point (e.g. via resource manager plug-in)
	 */
	
	/**
	 * Abstract parent class of both DTD and XSD validators, bundling caching
	 * of entity/resource data, handling of the various possible, and verbosity
	 * control.
	 * input sources.
	 * 
	 * @author sautter
	 */
	public static abstract class XmlValidator {
		private HashMap resolveCache = new HashMap();
		private String lastResolveUrlPrefix = null;
		private File dataPath;
		private String dataBaseUrl;
		boolean verbose = true;
		
		/** Constructor
		 */
		XmlValidator() {
			this(null);
		}
		
		/** Constructor
		 * @param dataPath the folder to seek and cache DTDs in
		 */
		XmlValidator(File dataPath) {
			this.dataPath = dataPath;
		}
		
		/**
		 * Set the path to load DTD entities or XSD resources from.
		 * @param dataPath the data path to set
		 */
		public void setDataPath(File dataPath) {
			this.dataPath = dataPath;
		}
		
		/**
		 * Set the base URL to load DTD entities or XSD resources from. The set
		 * URL will be used as a prefix for entity and resource names to
		 * resolve.
		 * @param dataBaseUrl the data base URL to set
		 */
		public void setDataBaseUrl(String dataBaseUrl) {
			if (dataBaseUrl != null) {
				while (dataBaseUrl.endsWith("/"))
					dataBaseUrl = dataBaseUrl.substring(0, (dataBaseUrl.length() - "/".length()));
			}
			this.dataBaseUrl = dataBaseUrl;
		}
		
		/**
		 * Switch verbose output on or off.
		 * @param verbose the verbose flag to set
		 */
		public void setVerbose(boolean verbose) {
			this.verbose = verbose;
		}
		
		synchronized byte[] resolveToData(String publicId, String systemId) {
			if (this.verbose) System.out.println("Asked to data resolve " + publicId + " ==> " + systemId);
			String cacheKey = ((publicId == null) ? ("SYSTEM-ID::" + systemId) : publicId);
			
			//	TODO maybe even use static resource cache shared between all validator instances
			byte[] bytes = ((byte[]) this.resolveCache.get(cacheKey));
			if (bytes != null) {
				if (this.verbose) System.out.println(" ==> found in cache");
				return bytes;
			}
			try {
				bytes = this.resolveSystemId(systemId);
			}
			catch (IOException ioe) {
				if (this.verbose) {
					System.out.println("Error resolving to custom source: " + ioe);
					System.out.println(ioe);
				}
			}
			if (bytes != null) {
				this.resolveCache.put(cacheKey, bytes);
				return bytes;
			}
			try {
				bytes = this.resolveToDataFile(systemId);
			}
			catch (IOException ioe) {
				if (this.verbose) {
					System.out.println("Error resolving to data file: " + ioe);
					System.out.println(ioe);
				}
			}
			if (bytes != null) {
				this.resolveCache.put(cacheKey, bytes);
				return bytes;
			}
			try {
				bytes = this.resolveToUrl(systemId);
			}
			catch (IOException ioe) {
				if (this.verbose) {
					System.out.println("Error resolving to URL: " + ioe);
					System.out.println(ioe);
				}
			}
			if (bytes != null) {
				this.resolveCache.put(cacheKey, bytes);
				return bytes;
			}
			try {
				bytes = this.resolveToDataUrl(systemId);
			}
			catch (IOException ioe) {
				if (this.verbose) {
					System.out.println("Error resolving to data URL: " + ioe);
					System.out.println(ioe);
				}
			}
			if (bytes != null) {
				this.resolveCache.put(cacheKey, bytes);
				return bytes;
			}
			return null;
		}
//		private byte[] resolveEntityToResource(String systemId) throws IOException {
//			System.out.println(" - resolving entity to resource: " + systemId);
//			if (systemId.lastIndexOf("/") != -1)
//				systemId = systemId.substring(systemId.lastIndexOf("/") + "/".length());
//			System.out.println(" - truncated to: " + systemId);
//			String resName = TreatmentHtmlUtils.class.getName().replaceAll("\\.", "/");
//			resName = resName.substring(0, (resName.lastIndexOf("/") + "/".length()));
//			resName = (resName + "xmlDtds/" + systemId);
//			System.out.println(" - seeking resource name: " + resName);
//			InputStream in = TreatmentHtmlUtils.class.getClassLoader().getResourceAsStream(resName);
//			if (in == null)
//				return null;
//			System.out.println("   ==> found");
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			byte[] buffer = new byte[1024];
//			for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
//				baos.write(buffer, 0, r);
//			in.close();
//			return baos.toByteArray();
//		}
		private byte[] resolveToDataFile(String systemId) throws IOException {
			if (this.dataPath == null)
				return null;
			if (this.verbose) System.out.println(" - resolving entity to data file: " + systemId);
			if (systemId.lastIndexOf("//") != -1)
				systemId = systemId.substring(systemId.lastIndexOf("//") + "//".length());
			if (this.verbose) System.out.println(" - truncated to: " + systemId);
			for (int c = systemId.lastIndexOf("/");;) {
				String dataFileName = systemId.substring(c + "/".length());
				if (this.verbose) System.out.println(" - seeking file name: " + dataFileName);
				File dataFile = new File(this.dataPath, dataFileName);
				if (dataFile.exists()) {
					if (this.verbose) System.out.println("   ==> found");
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream in = new BufferedInputStream(new FileInputStream(dataFile));
					byte[] buffer = new byte[1024];
					for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
						baos.write(buffer, 0, r);
					in.close();
					return baos.toByteArray();
				}
				else if (this.verbose) System.out.println("   ==> not found");
				if (c == -1)
					break;
				c = systemId.lastIndexOf("/", (c - 1));
			}
			return null;
		}
		private byte[] resolveToUrl(String systemId) throws IOException {
			if (this.verbose) System.out.println(" - resolving entity to URL: " + systemId);
			if (systemId.startsWith("http://") || systemId.startsWith("https://")) {
				this.lastResolveUrlPrefix = systemId.substring(0, systemId.lastIndexOf('/'));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream in = new BufferedInputStream((new URL(systemId)).openStream());
				byte[] buffer = new byte[1024];
				for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
					baos.write(buffer, 0, r);
				this.cacheToFile(systemId.substring(systemId.lastIndexOf('/') + "/".length()), baos.toByteArray());
				return baos.toByteArray();
			}
			else return this.resolveToUrlPrefix(systemId, this.lastResolveUrlPrefix);
		}
		private byte[] resolveToDataUrl(String systemId) throws IOException {
			if (this.dataBaseUrl == null)
				return null;
			if (this.verbose) System.out.println(" - resolving entity to data URL: " + systemId);
			return this.resolveToUrlPrefix(systemId, this.dataBaseUrl);
		}
		private byte[] resolveToUrlPrefix(String systemId, String urlPrefix) throws IOException {
			if (systemId.lastIndexOf("//") != -1)
				systemId = systemId.substring(systemId.lastIndexOf("//") + "//".length());
			if (this.verbose) System.out.println(" - truncated to: " + systemId);
			for (int c = systemId.lastIndexOf("/");;) try {
				String dataFileName = systemId.substring(c + "/".length());
				String dataUrl = (urlPrefix + "/" + dataFileName);
				if (this.verbose) System.out.println(" - seeking URL: " + dataUrl);
				InputStream in = new BufferedInputStream((new URL(dataUrl)).openStream());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
					baos.write(buffer, 0, r);
				if (this.verbose) System.out.println("   ==> found");
				this.cacheToFile(dataFileName, baos.toByteArray());
				return baos.toByteArray();
			}
			catch (FileNotFoundException fnfe) {
				if (this.verbose) System.out.println("   ==> not found");
				if (c == -1)
					break;
				c = systemId.lastIndexOf("/", (c - 1));
			}
			return null;
		}
		private void cacheToFile(String fileName, byte[] bytes) {
			if (this.dataPath != null) try {
				File dataFile = new File(this.dataPath, fileName);
				dataFile.getParentFile().mkdirs();
				OutputStream out = new BufferedOutputStream(new FileOutputStream(dataFile));
				out.write(bytes);
				out.flush();
				out.close();
			}
			catch (IOException ioe) {
				if (this.verbose) {
					System.out.println("Error caching entity file: " + ioe);
					System.out.println(ioe);
				}
			}
		}
		
		/**
		 * Resolve a DTD entity in a subclass specific way, e.g. against a
		 * resource on the class path, or a configured URL. This default
		 * implementation does nothing, subclasses are welcome to overwrite it
		 * as needed.
		 * @param systemId the system ID to resolve
		 * @return the bytes representing the requested system ID
		 * @throws IOException
		 */
		protected byte[] resolveSystemId(String systemId) throws IOException {
			return null;
		}
		
		static abstract class ValidationData {
			final byte[] xmlBytes;
			ValidationData(byte[] xmlBytes) {
				this.xmlBytes = xmlBytes;
			}
			abstract String[] getXmlLines();
		}
		
		private static class StringValidationData extends ValidationData {
			private String[] xmlLines;
			StringValidationData(byte[] xmlBytes, String[] xmlLines) {
				super(xmlBytes);
				this.xmlLines = xmlLines;
			}
			String[] getXmlLines() {
				return this.xmlLines;
			}
		}
		
		/**
		 * Validate an XML document provided by a character input stream.
		 * @param xmlIn the reader providing the XML document to validate
		 * @return the validation report
		 */
		public ValidationReport validateXml(Reader xmlIn) throws IOException {
			//	buffer the whole thing FIRST ... we need it for the report anyway
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(baos, "UTF-8");
			char[] buffer = new char[1024];
			ArrayList xmlLines = new ArrayList();
			StringBuffer xmlLine = new StringBuffer();
			char lastChar = ((char) 0);
			for (int r; (r = xmlIn.read(buffer, 0, buffer.length)) != -1;) {
				out.write(buffer, 0, r);
				//	generate lines along the way (no need to create another string instance from bytes !!!)
				for (int c = 0; c < r; c++) {
					if (buffer[c] == '\r') {
						xmlLines.add(xmlLine.toString());
						xmlLine.delete(0, xmlLine.length());
					}
					else if (buffer[c] == '\n') {
						if (lastChar != '\r') /* ignore second part of cross-platform line break */ {
							xmlLines.add(xmlLine.toString());
							xmlLine.delete(0, xmlLine.length());
						}
					}
					else xmlLine.append(buffer[c]);
					lastChar = buffer[c];
				}
			}
			out.flush();
			if (xmlLine.length() != 0)
				xmlLines.add(xmlLine.toString());
			return this.validateXml(new StringValidationData(baos.toByteArray(), ((String[]) xmlLines.toArray(new String[xmlLines.size()]))));
		}
		
		/**
		 * Validate an XML document.
		 * @param xml the XML document to validate
		 * @return the validation report
		 */
		public ValidationReport validateXml(String xml) {
			try {
				return this.validateXml(new StringValidationData(xml.getBytes("UTF-8"), xml.split("(\\r\\n|\\r|\\n)")));
			}
			catch (UnsupportedEncodingException usee) {
				return null; // never gonna happen, but Java don't know
			}
		}
		
		private static class ByteValidationData extends ValidationData {
			ByteValidationData(byte[] xmlBytes) {
				super(xmlBytes);
			}
			String[] getXmlLines() {
				try {
					return (new String(xmlBytes, "UTF-8")).split("(\\r\\n|\\r|\\n)");
				}
				catch (UnsupportedEncodingException usee) {
					return null; // never gonna happen, but Java don't know
				}
			}
		}
		
		/**
		 * Validate an XML document provided by a byte input stream.
		 * @param xmlIn the input stream providing the XML document to validate
		 * @return the validation report
		 */
		public ValidationReport validateXml(InputStream xmlIn) throws IOException {
			//	buffer the whole thing first ... we need the full data for the report anyway
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int r; (r = xmlIn.read(buffer, 0, buffer.length)) != -1;)
				baos.write(buffer, 0, r);
			return this.validateXml(new ByteValidationData(baos.toByteArray()));
		}
		
		/**
		 * Validate an XML document. The bytes are expected in UTF-8 encoding.
		 * @param xmlBytes the XML document to validate
		 * @return the validation report
		 */
		public ValidationReport validateXml(byte[] xmlBytes) {
			return this.validateXml(new ByteValidationData(xmlBytes));
		}
		
		abstract ValidationReport validateXml(ValidationData data);
		
		static abstract class XmlValidatorInstance implements ErrorHandler {
			private ArrayList fatals = new ArrayList();
			private ArrayList errors = new ArrayList();
			private ArrayList warnings = new ArrayList();
			public void fatalError(SAXParseException spe) throws SAXException {
				this.fatals.add(spe);
			}
			public void error(SAXParseException spe) throws SAXException {
				this.errors.add(spe);
			}
			public void warning(SAXParseException spe) throws SAXException {
				this.warnings.add(spe);
			}
			ValidationReport validate(ValidationData data, XmlValidator parent) {
				this.runValidation(data, parent);
				return this.createReport(data.getXmlLines(), parent);
			}
			abstract void runValidation(ValidationData data, XmlValidator parent);
			ValidationReport createReport(String[] xmlLines, XmlValidator parent) {
				ValidationReport.Line[] reportLines = new ValidationReport.Line[xmlLines.length];
				for (int l = 0; l < xmlLines.length; l++)
					reportLines[l] = new ValidationReport.Line(l, xmlLines[l]);
				ValidationReport report = new ValidationReport(reportLines);
				
				if (this.fatals.size() != 0) {
					report.fatals.addAll(this.fatals);
					if (parent.verbose) System.out.println("XML has " + this.errors.size() + " fatal errors:");
					for (int e = 0; e < this.fatals.size(); e++) {
						SAXParseException spe = ((SAXParseException) this.fatals.get(e));
						reportLines[spe.getLineNumber()-1 /* line is 1-based */].fatals.add(spe);
						if (parent.verbose) System.out.println(" - " + spe.getMessage());
					}
				}
				if (this.errors.size() != 0) {
					report.errors.addAll(this.errors);
					if (parent.verbose) System.out.println("XML has " + this.errors.size() + " validation errors:");
					for (int e = 0; e < this.errors.size(); e++) {
						SAXParseException spe = ((SAXParseException) this.errors.get(e));
						reportLines[spe.getLineNumber()-1 /* line is 1-based */].errors.add(spe);
						if (parent.verbose) System.out.println(" - " + spe.getMessage());
					}
				}
				if (this.warnings.size() != 0) {
					report.warnings.addAll(this.warnings);
					if (parent.verbose) System.out.println("XML has " + this.warnings.size() + " validation warnings:");
					for (int e = 0; e < this.warnings.size(); e++) {
						SAXParseException spe = ((SAXParseException) this.warnings.get(e));
						reportLines[spe.getLineNumber()-1 /* line is 1-based */].warnings.add(spe);
						if (parent.verbose) System.out.println(" - " + spe.getMessage());
					}
				}
				
				return report;
			}
			void clearErrors() {
				this.fatals.clear();
				this.errors.clear();
				this.warnings.clear();
			}
		}
	}
	
	/**
	 * An error report on the validation of an XML document.
	 * 
	 * @author sautter
	 */
	public static class ValidationReport {
		private ValidationReport.Line[] lines;
		final ArrayList fatals = new ArrayList(1);
		final ArrayList errors = new ArrayList(1);
		final ArrayList warnings = new ArrayList(1);
		ValidationReport(ValidationReport.Line[] lines) {
			this.lines = lines;
		}
		
		/**
		 * Print a validation report. This method outputs the individual lines
		 * of the input, with indicators of errors interspersed.
		 * @param out the print stream to print to
		 */
		public void printReport(PrintWriter out) {
			int lineHeadChars;
			if (this.hasErrorOrWarnings())
				lineHeadChars = 9;
			else lineHeadChars = (("" + (this.lines.length - 1)).length() + 1);
			for (int l = 0; l < this.lines.length; l++)
				this.lines[l].printReport(out, lineHeadChars);
			out.flush();
		}
		
		/**
		 * Write a validation report to some character output stream. This
		 * method outputs the individual lines of the input, with indicators
		 * of errors interspersed.
		 * @param out the writer to write to
		 */
		public void writeReport(Writer out) {
			this.printReport(new PrintWriter(out));
		}
		
		/**
		 * Retrieve a validation report as a string. The returned string
		 * consists of the individual lines of the input, with indicators of
		 * errors interspersed.
		 * @return the validation report
		 */
		public String getReport() {
			StringWriter sw = new StringWriter();
			this.printReport(new PrintWriter(sw));
			return sw.toString();
		}
		
		/**
		 * Test whether or not the validation report contains any errors. If
		 * the subject XML document is valid, this method returns false.
		 * @return true if there are any errors
		 */
		public boolean hasError() {
			return ((this.fatals.size() != 0) || (this.errors.size() != 0));
		}
		
		/**
		 * Test whether or not the validation report contains any errors or
		 * warnings. If the subject XML document is valid, this method returns
		 * false.
		 * @return true if there are any errors or warnings
		 */
		public boolean hasErrorOrWarnings() {
			return ((this.fatals.size() != 0) || (this.errors.size() != 0) || (this.warnings.size() != 0));
		}
		
		/**
		 * Retrieve all validation errors.
		 * @return an array holding the errors
		 */
		public SAXParseException[] getErrors() {
			return this.getErrors(false);
		}
		
		/**
		 * Retrieve all validation errors.
		 * @param includeWarnings include warnings as well?
		 * @return an array holding the errors
		 */
		public SAXParseException[] getErrors(boolean includeWarnings) {
			ArrayList errorList = new ArrayList();
			errorList.addAll(this.fatals);
			errorList.addAll(this.errors);
			if (includeWarnings)
				errorList.addAll(this.warnings);
			SAXParseException[] errors = ((SAXParseException[]) errorList.toArray(new SAXParseException[errorList.size()]));
			Arrays.sort(errors, errorOrder);
			return errors;
		}
		
		/**
		 * Retrieve the number of lines in the report (and hence in the input
		 * XML that was validated to create the report).
		 * @return the number of lines
		 */
		public int getLineCount() {
			return this.lines.length;
		}
		
		/**
		 * Retrieve a line of the report, corresponding to a line in the input
		 * XML that was validated to create the report.
		 * @param lineNumber the number of the line
		 * @return the line at the indicated position
		 */
		public ValidationReport.Line getLine(int lineNumber) {
			return this.lines[lineNumber];
		}
		
		/**
		 * A single line in a validation report, corresponding to a single line in
		 * the input XML.
		 * 
		 * @author sautter
		 */
		public static class Line {
			
			/** the line number (zero based) */
			public final int lineNumber;
			
			/** the line proper, as provided in the input XML */
			public final String line;
			
			final ArrayList fatals = new ArrayList(1);
			final ArrayList errors = new ArrayList(1);
			final ArrayList warnings = new ArrayList(1);
			Line(int lineNumber, String line) {
				this.lineNumber = lineNumber;
				this.line = line;
			}
			
			/**
			 * Print a validation report for the line. This method outputs the line
			 * of the original input, with indicators of errors below it.
			 * @param out the print stream to print to
			 */
			public void printReport(PrintWriter out) {
				this.printReport(out, (this.hasErrorOrWarnings() ? 9 : 0));
				out.flush();
			}
			
			/**
			 * Write a validation report for the line to some character output
			 * stream. This method outputs the line of the original input, with
			 * indicators of errors below it.
			 * @param out the writer to write to
			 */
			public void writeReport(Writer out) {
				this.printReport(new PrintWriter(out));
			}
			
			/**
			 * Retrieve a validation report for the line as a string. The returned
			 * string consists of the individual lines of the line of the original
			 * input, with indicators of errors below it.
			 * @return the validation report
			 */
			public String getReport() {
				StringWriter sw = new StringWriter();
				this.printReport(new PrintWriter(sw));
				return sw.toString();
			}
			
			void printReport(PrintWriter out, int lineHeadChars) {
				String ln = ("" + this.lineNumber);
				while (ln.length() < lineHeadChars)
					ln = (" " + ln);
				out.println(ln + " " + this.line);
				if (this.fatals.size() != 0)
					for (int e = 0; e < this.fatals.size(); e++) {
						SAXParseException spe = ((SAXParseException) this.fatals.get(e));
						this.printError(spe, " FATAL   ", out);
					}
				if (this.errors.size() != 0)
					for (int e = 0; e < this.errors.size(); e++) {
						SAXParseException spe = ((SAXParseException) this.errors.get(e));
						this.printError(spe, " ERROR   ", out);
					}
				if (this.warnings.size() != 0)
					for (int e = 0; e < this.warnings.size(); e++) {
						SAXParseException spe = ((SAXParseException) this.warnings.get(e));
						this.printError(spe, " WARNING  ", out);
					}
			}
			private void printError(SAXParseException spe, String prefix, PrintWriter out) {
				out.print(prefix);
				for (int p = 1 /* column is 1-based */; p < spe.getColumnNumber(); p++)
					out.print(" ");
				out.println("^");
				out.println("          " + spe.getMessage() + " at line " + spe.getLineNumber() + " in column " + spe.getColumnNumber());
				if (spe.getException() != null)
					out.println(" CAUSE    " + spe.getException().getMessage());
			}
			
			/**
			 * Test whether or not the validation report contains any errors for
			 * this line. If this line of the subject XML document is valid, this
			 * method returns false.
			 * @return true if there are any errors
			 */
			public boolean hasError() {
				return ((this.fatals.size() != 0) || (this.errors.size() != 0));
			}
			
			/**
			 * Test whether or not the validation report contains any errors or
			 * warnings for this line. If this line of the subject XML document is
			 * valid, this method returns false.
			 * @return true if there are any errors or warnings
			 */
			public boolean hasErrorOrWarnings() {
				return ((this.fatals.size() != 0) || (this.errors.size() != 0) || (this.warnings.size() != 0));
			}
			
			/**
			 * Retrieve all validation errors.
			 * @return an array holding the errors
			 */
			public SAXParseException[] getErrors() {
				return this.getErrors(false);
			}
			
			/**
			 * Retrieve all validation errors.
			 * @param includeWarnings include warnings as well?
			 * @return an array holding the errors
			 */
			public SAXParseException[] getErrors(boolean includeWarnings) {
				ArrayList errorList = new ArrayList();
				errorList.addAll(this.fatals);
				errorList.addAll(this.errors);
				if (includeWarnings)
					errorList.addAll(this.warnings);
				SAXParseException[] errors = ((SAXParseException[]) errorList.toArray(new SAXParseException[errorList.size()]));
				Arrays.sort(errors, errorOrder);
				return errors;
			}
		}
		
		static final Comparator errorOrder = new Comparator() {
			public int compare(Object obj1, Object obj2) {
				SAXParseException spe1 = ((SAXParseException) obj1);
				SAXParseException spe2 = ((SAXParseException) obj2);
				int c = (spe1.getLineNumber() - spe2.getLineNumber());
				return ((c == 0) ? (spe1.getColumnNumber() - spe2.getColumnNumber()) : c);
			}
		};
	}
	
	/**
	 * DTD based validator for XML documents. This class forms a thread safe
	 * wrapper for a pool of actual validator instances, the latter being
	 * shared between all instances of this class.
	 * 
	 * @author sautter
	 */
	public static class DtdValidator extends XmlValidator implements EntityResolver {
		private EntityResolver entityResolver = null;
		
		/** Constructor
		 */
		public DtdValidator() {
			super();
		}
		
		/** Constructor
		 * @param dataPath the folder to seek and cache DTDs in
		 */
		public DtdValidator(File dataPath) {
			super(dataPath);
		}
		
		/*
TODOne For DTD validator, figure out how to use fixed DTD
==> should be more flexible
==> if not possible, share static pool of validators between _all_ pool instances
  ==> vastly increased efficiency
  - requires setting resource resolver and error handler on retrieval

==> DOES NOT SEEM TO BE POSSIBLE, EXCEPT SPOOFING DOCTYPE DECLARATION ...
		 */
		
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if (this.entityResolver != null) {
				InputSource entitySource = this.entityResolver.resolveEntity(publicId, systemId);
				if (entitySource != null)
					return entitySource;
			}
			byte[] bytes = this.resolveToData(publicId, systemId);
			return ((bytes == null) ? null : new InputSource(new ByteArrayInputStream(bytes)));
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.XmlValidationUtils.AbstractValidator#doValidateXml(de.uka.ipd.idaho.htmlXmlUtil.accessories.XmlValidationUtils.AbstractValidator.ValidationData)
		 */
		ValidationReport validateXml(ValidationData data) {
			long start = System.currentTimeMillis();
			DtdValidatorInstance xmlValidator;
			try {
//				xmlValidator = this.getXmlValidator();
				xmlValidator = getXmlValidator();
			}
			catch (ParserConfigurationException pce) {
				if (this.verbose) {
					System.out.println("Error validating XML: " + pce.getMessage());
					pce.printStackTrace(System.out);
				}
				return null;
			}
			if (this.verbose) System.out.println("XML validator ensured in " + (System.currentTimeMillis() - start) + "ms");
			
			start = System.currentTimeMillis();
			try {
				return xmlValidator.validate(data, this);
			}
			finally {
//				this.returnXmlValidator(xmlValidator);
				returnXmlValidator(xmlValidator);
				if (this.verbose) System.out.println("XML validated in " + (System.currentTimeMillis() - start) + "ms");
			}
		}

		private static class DtdValidatorInstance extends XmlValidatorInstance {
			private DocumentBuilder docBuilder;
			DtdValidatorInstance(DocumentBuilder docBuilder) {
				this.docBuilder = docBuilder;
			}
			void runValidation(ValidationData data, XmlValidator parent) {
				this.docBuilder.setErrorHandler(this);
				this.docBuilder.setEntityResolver((DtdValidator) parent);
				try {
					this.docBuilder.parse(new ByteArrayInputStream(data.xmlBytes));
				}
				catch (IOException ioe) {
					if (parent.verbose) {
						System.out.println("Error validating XML: " + ioe.getMessage());
						System.out.println(ioe);
					}
				}
				catch (SAXException se) {
					if (parent.verbose) {
						System.out.println("Validation fatal error: " + se.getMessage());
						System.out.println(se);
					}
				}
			}
			void reset() {
				this.docBuilder.reset();
				this.clearErrors();
			}
		}
//		private DocumentBuilderFactory docBuilderFactory = null;
//		private LinkedList xmlValidators = new LinkedList();
//		private synchronized DtdValidatorInstance getXmlValidator() throws ParserConfigurationException {
//			if (this.xmlValidators.size() != 0)
//				return ((DtdValidatorInstance) this.xmlValidators.removeFirst());
//			if (this.docBuilderFactory == null) {
//				this.docBuilderFactory = DocumentBuilderFactory.newInstance();
//				this.docBuilderFactory.setValidating(true);
//			}
//			return new DtdValidatorInstance(this.docBuilderFactory.newDocumentBuilder());
//		}
//		private synchronized void returnXmlValidator(DtdValidatorInstance xmlValidator) {
//			xmlValidator.reset();
//			this.xmlValidators.addLast(xmlValidator);
//		}
		//	TODOne even make this static ???
		private static DocumentBuilderFactory docBuilderFactory = null;
		private static LinkedList xmlValidators = new LinkedList();
		private static synchronized DtdValidatorInstance getXmlValidator() throws ParserConfigurationException {
			if (xmlValidators.size() != 0)
				return ((DtdValidatorInstance) xmlValidators.removeFirst());
			if (docBuilderFactory == null) {
				docBuilderFactory = DocumentBuilderFactory.newInstance();
				docBuilderFactory.setValidating(true);
			}
			return new DtdValidatorInstance(docBuilderFactory.newDocumentBuilder());
		}
		private static synchronized void returnXmlValidator(DtdValidatorInstance xmlValidator) {
			xmlValidator.reset();
			xmlValidators.addLast(xmlValidator);
		}
	}
}
