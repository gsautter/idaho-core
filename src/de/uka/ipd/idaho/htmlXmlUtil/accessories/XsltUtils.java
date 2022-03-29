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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import de.uka.ipd.idaho.htmlXmlUtil.Parser;
import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiver;
import de.uka.ipd.idaho.htmlXmlUtil.TreeNodeAttributeSet;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XsltUtils.TransformerPool.PooledTransformer;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;

/**
 * Utility class for creating and caching XSLT transformers, and for chaining
 * transformers.
 * 
 * @author sautter
 */
public class XsltUtils {
//	
//	//	!!! for test purposes only !!!
//	public static void main(String[] args) throws Exception {
//		ApplicationHttpsEnabler.enableHttps();
//		URL xsltUrl = new URL("https://raw.githubusercontent.com/plazi/ggxml2taxpub-treatments/main/xslt/gg2tp_l1.xsl");
////		URL xsltUrl = new URL("http://tb.plazi.org/GgServer/gg2tp_l1.xsl");
//		File xsltCache = new File("E:/Temp/XsltCache");
//		getTransformer(xsltUrl, xsltCache);
//		
////		File xsltFile = new File("E:/Projektdaten/TaxPubOutput/gg2tp_l1.xsl");
////		getTransformer(xsltFile);
//		
////		File cXsltFile1 = new File("./Components/GgServerPlaziWCSData/gg2wiki.xslt");
////		getTransformer(cXsltFile1);
////		getTransformer(cXsltFile1, false);
////		if (true)
////			return;
//		
////		File xsltFile1 = new File("E:/Projektdaten/XSLT Round Trip/gg2taxonx.xsl");
////		File xsltFile2 = new File("E:/Projektdaten/XSLT Round Trip/taxonx2gg.xsl");
////		Transformer trf1 = getTransformer(xsltFile1);
////		Transformer trf2 = getTransformer(xsltFile2);
////		
//////		MutableAnnotation doc = SgmlDocumentReader.readDocument(new File("E:/Projektdaten/TaxonxTest/21211_gg1.xml"));
//////		Reader reader = new AnnotationReader(doc);
//////		reader = chain(reader, trf1);
//////		reader = chain(reader, trf2);
//////		reader = chain(reader, trf1);
//////		
//////		char[] buffer = new char[1024];
//////		int read;
//////		while ((read = reader.read(buffer, 0, buffer.length)) != -1)
//////			System.out.print(new String(buffer, 0, read));
////		MutableAnnotation doc = SgmlDocumentReader.readDocument(new File("E:/Projektdaten/TaxonxTest/21211_gg1.xml"));
////		Writer writer = new OutputStreamWriter(System.out);
////		writer = wrap(writer, trf2);
////		writer = wrap(writer, trf1);
////		AnnotationUtils.writeXML(doc, writer, null, null, true);
////		writer.close();
//	}
	
	/**
	 * Produce an InputStream that provides the output of a given XSLT
	 * transformer whose input is provided by a given InputStream. This method
	 * will use an extra Thread for sending the data from the source InputStream
	 * into the transformer. This thread is started soon as the read() method of
	 * the returned InputStream is invoked for the first time. If the argument
	 * transformer is a pooled transformer obtained from a transformer pool, it
	 * is handed back to the pool after the transformation is finished.
	 * @param source the InputStream to read data from
	 * @param transformer the transformer to run the data through
	 * @return an InputStream providing the output of the specified Transformer,
	 *         or the argument InputStream, if the specified Transformer is null
	 * @throws IOException
	 */
	public static InputStream chain(final InputStream source, final Transformer transformer) throws IOException {
		if (transformer == null) return source;
		
		final Object handshake = new Object();
		final IOException[] exception = {null};
		
		final PipedOutputStream pos = new PipedOutputStream() {
			private boolean firstWrite = true;
			public void write(int c) throws IOException {
				this.wakeReader();
				super.write(c);
			}
			public void write(byte[] b, int off, int len) throws IOException {
				this.wakeReader();
				super.write(b, off, len);
			}
			public void write(byte[] b) throws IOException {
				this.wakeReader();
				super.write(b);
			}
			public void close() throws IOException {
				this.wakeReader();
				super.close();
			}
			public synchronized void flush() throws IOException {
				this.wakeReader();
				super.flush();
			}
			private void wakeReader() {
				if (this.firstWrite) 
					synchronized (handshake) {
						this.firstWrite = false;
//						System.out.println("First data arrived, waking up reader");
						handshake.notify();
					}
			}
			/*
			 * no need for overwriting the other methods for writing, they all
			 * refer to the one-argument and three-argument write() methods
			 */
		};
		
		final Thread t = new Thread() {
			public void run() {
				try {
					try {
//						System.out.println("Start processing input");
						transformer.transform(new StreamSource(source), new StreamResult(pos));
//						System.out.println("Done processing input");
						pos.flush();
						pos.close();
					}
					catch (TransformerException te) {
						throw new IOException(te.getMessageAndLocation());
					}
				}
				catch (IOException e) {
					exception[0] = e;
				}
				finally {
					if (transformer instanceof PooledTransformer)
						((PooledTransformer) transformer).handBack();
				}
			}
		};
		
		return new PipedInputStream(pos) {
			private boolean firstRead = true;
			public synchronized int read() throws IOException {
				if (exception[0] != null)
					throw exception[0];
				
				if (this.firstRead) {
					this.firstRead = false;
					synchronized (handshake) {
						t.start();
//						System.out.println("Transformer started, waiting for data to come ...");
						try {
							handshake.wait();
						} catch (InterruptedException ie) {}
//						System.out.println("First data arrived, resuming");
					}
				}
				
				int r = super.read();
				
				if (r < 0) try {
					t.join();
				} catch (InterruptedException ie) {}
				
				return r;
			}
			/*
			 * no need for overwriting the other methods for reading, they all
			 * refer to the no-argument read() method
			 */
		};
	}
	
	/**
	 * Produce a Reader that provides the output of a given XSLT transformer
	 * whose input is provided by a given Reader. This method will use an extra
	 * Thread for sending the data from the source Reader into the transformer.
	 * This thread is started soon as the read() method of the returned Reader
	 * is invoked for the first time. If the argument transformer is a pooled
	 * transformer obtained from a transformer pool, it is handed back to the
	 * pool after the transformation is finished.
	 * @param source the Reader to read data from
	 * @param transformer the transformer to run the data through
	 * @return a Reader providing the output of the specified Transformer, or
	 *         the argument reader, if the specified Transformer is null
	 * @throws IOException
	 */
	public static Reader chain(final Reader source, final Transformer transformer) throws IOException {
		if (transformer == null) return source;
		
		final Object handshake = new Object();
		final IOException[] exception = {null};
		
		final PipedWriter pw = new PipedWriter() {
			private boolean firstWrite = true;
			public void write(char[] cbuf, int off, int len) throws IOException {
				this.wakeReader();
				super.write(cbuf, off, len);
			}
			public void write(int c) throws IOException {
				this.wakeReader();
				super.write(c);
			}
			public void close() throws IOException {
				this.wakeReader();
				super.close();
			}
			public synchronized void flush() throws IOException {
				this.wakeReader();
				super.flush();
			}
			private void wakeReader() {
				if (this.firstWrite) 
					synchronized (handshake) {
						this.firstWrite = false;
//						System.out.println("First data arrived, waking up reader");
						handshake.notify();
					}
			}
			
			/*
			 * no need for overwriting the other methods for writing, they all
			 * refer to the one-argument and three-argument write() methods
			 */
		};
		
		final Thread t = new Thread() {
			public void run() {
				try {
					try {
//						System.out.println("Start processing input");
						transformer.transform(new StreamSource(source), new StreamResult(pw));
//						System.out.println("Done processing input");
						pw.flush();
						pw.close();
					}
					catch (TransformerException te) {
						throw new IOException(te.getMessageAndLocation());
					}
				}
				catch (IOException e) {
					exception[0] = e;
				}
				finally {
					if (transformer instanceof PooledTransformer)
						((PooledTransformer) transformer).handBack();
				}
			}
		};
		
		return new PipedReader(pw) {
			private boolean firstRead = true;
			public synchronized int read() throws IOException {
				if (exception[0] != null)
					throw exception[0];
				
				if (this.firstRead) {
					this.firstRead = false;
					synchronized (handshake) {
						t.start();
//						System.out.println("Transformer started, waiting for data to come ...");
						try {
							handshake.wait();
						} catch (InterruptedException ie) {}
//						System.out.println("First data arrived, resuming");
					}
				}
				
				int r = super.read();
				
				if (r < 0) try {
					t.join();
				} catch (InterruptedException ie) {}
				
				return r;
			}
			/*
			 * no need for overwriting the other methods for reading, they all
			 * refer to the no-argument read() method
			 */
		};
	}
	
	/**
	 * An IsolatorOutputStream prevents its contained OutputStream from being
	 * flushed and closed, by overwriting the respective methods to do nothing.
	 * This provides a means of preventing the data flow to a given output
	 * stream from being terminated after an XSLT transformer has written its
	 * output to it.
	 * 
	 * @author sautter
	 */
	public static class IsolatorOutputStream extends FilterOutputStream {
		private boolean isolateFlush;
		
		/**
		 * Constructor
		 * @param out the output stream to isolate
		 */
		public IsolatorOutputStream(OutputStream out) {
			this(out, true);
		}
		
		/**
		 * Constructor
		 * @param out the output stream to isolate
		 * @param isolateFlush isolate the wrapped stream's flush method?
		 */
		public IsolatorOutputStream(OutputStream out, boolean isolateFlush) {
			super(out);
			this.isolateFlush = isolateFlush;
		}
		
		/**
		 * As by the purpose of this class, this method does nothing.
		 * @see java.io.FilterOutputStream#flush()
		 */
		public void flush() throws IOException {
			if (!this.isolateFlush)
				super.flush();
		}
		
		/**
		 * As by the purpose of this class, this method does nothing.
		 * @see java.io.FilterOutputStream#close()
		 */
		public void close() throws IOException {}
	}

	/**
	 * Wrap a given OutputStream in another OutputStream that sends data through
	 * a given XSLT transformer before writing to the argument stream. This
	 * method will use an extra Thread for sending the data from the returned
	 * OutputStream through the transformer. This thread is started soon as the
	 * write() method of the returned OutputStream is invoked for the first
	 * time. The argument OutputStream will be flushed and closed after
	 * transformation is finished, i.e., when the returned OutputStream is
	 * closed. The returned OutputStream must be closed (flushes automatically)
	 * after all to-transform data has been written to the returned OutputStream
	 * in order for the transformation thread to finish. If a given OutputStream
	 * should not be flushed and closed after the transformation, e.g. if
	 * additional data is to be written after the transformation output, wrap an
	 * IsolatorOutputStream around that OutputStream before handing it to this
	 * method. If the argument transformer is a pooled transformer obtained from
	 * a transformer pool, it is handed back to the pool after the argument
	 * writer is closed.
	 * @param target the OutputStream to write data to
	 * @param transformer the transformer to run the data through
	 * @return an OutputStream writing to the specified transformer, or the
	 *         argument OutputStream, if the specified Transformer is null
	 * @throws IOException
	 */
	public static OutputStream wrap(final OutputStream target, final Transformer transformer) throws IOException {
		if (transformer == null) return target;
		
		final IOException[] exception = {null};
		
		final PipedInputStream pis = new PipedInputStream();
		
		final Thread t = new Thread() {
			public void run() {
				try {
					try {
//						System.out.println("Start processing input");
						transformer.transform(new StreamSource(pis), new StreamResult(target));
//						System.out.println("Done processing input");
						target.flush();
						target.close();
					}
					catch (TransformerException te) {
						throw new IOException(te.getMessageAndLocation());
					}
				}
				catch (IOException e) {
					exception[0] = e;
				}
				finally {
					if (transformer instanceof PooledTransformer)
						((PooledTransformer) transformer).handBack();
				}
			}
		};
		
		return new PipedOutputStream(pis) {
			private boolean firstWrite = true;
			public void write(int c) throws IOException {
				this.wakeWriter();
				super.write(c);
			}
			public void write(byte[] b, int off, int len) throws IOException {
				this.wakeWriter();
				super.write(b, off, len);
			}
			public void write(byte[] b) throws IOException {
				this.wakeWriter();
				super.write(b);
			}
			public void close() throws IOException {
				this.wakeWriter();
				try {
					super.flush();
				} catch (Exception e) {}
				super.close();
				try {
					t.join();
//					System.out.println("Transformer finished");
				} catch (InterruptedException ie) {}
			}
			public synchronized void flush() throws IOException {
				this.wakeWriter();
				super.flush();
			}
			private void wakeWriter() throws IOException {
				if (exception[0] != null)
					throw exception[0];
				
				if (this.firstWrite) {
					this.firstWrite = false;
					t.start();
//					System.out.println("Transformer started");
				}
			}
			/*
			 * no need for overwriting the other methods for writing, they all
			 * refer to the one-argument and three-argument write() methods
			 */
		};
	}
	
	/**
	 * An IsolatorWriter prevents its contained Writer from being flushed and
	 * closed, by overwriting the respective methods to do nothing. This
	 * provides a means of preventing the data flow to a given writer from being
	 * terminated after an XSLT transformer has written its output to it.
	 * 
	 * @author sautter
	 */
	public static class IsolatorWriter extends FilterWriter {
		private boolean isolateFlush;
		
		/**
		 * Constructor
		 * @param out the writer to isolate
		 */
		public IsolatorWriter(Writer out) {
			this(out, true);
		}
		
		/**
		 * Constructor
		 * @param out the writer to isolate
		 * @param isolateFlush isolate the wrapped writer's flush method?
		 */
		public IsolatorWriter(Writer out, boolean isolateFlush) {
			super(out);
			this.isolateFlush = isolateFlush;
		}
		
		/**
		 * According to the purpose of this class, this method does nothing.
		 * @see java.io.FilterWriter#flush()
		 */
		public void flush() throws IOException {
			if (!this.isolateFlush)
				super.flush();
		}
		
		/**
		 * According to the purpose of this class, this method does nothing.
		 * @see java.io.FilterWriter#close()
		 */
		public void close() throws IOException {}
	}
	
	/**
	 * Wrap a given Writer in another Writer that sends data through a given
	 * XSLT transformer before writing to the argument writer. This method will
	 * use an extra Thread for sending the data from the returned Writer through
	 * the transformer. This thread is started soon as the write() method of the
	 * returned Writer is invoked for the first time. The argument Writer will
	 * be flushed and closed after transformation is finished, i.e., when the
	 * returned Writer is closed. The returned Writer must be closed (flushes
	 * automatically) after all to-transform data has been written to the
	 * returned Writer in order for the transformation thread to finish. If a
	 * given Writer should not be flushed and closed after the transformation,
	 * e.g. if additional data is to be written after the transformation output,
	 * wrap an IsolatorWriter around that Writer before handing it to this
	 * method. If the argument transformer is a pooled transformer obtained from
	 * a transformer pool, it is handed back to the pool after the argument
	 * writer is closed.
	 * @param target the Writer to write data to
	 * @param transformer the transformer to run the data through
	 * @return a Writer writing to the specified transformer, or the argument
	 *         Writer, if the specified Transformer is null
	 * @throws IOException
	 */
	public static Writer wrap(final Writer target, final Transformer transformer) throws IOException {
		if (transformer == null) return target;
		
		final IOException[] exception = {null};
		
		final PipedReader pr = new PipedReader();
		
		final Thread t = new Thread() {
			public void run() {
				try {
					try {
//						System.out.println("Start processing input");
						transformer.transform(new StreamSource(pr), new StreamResult(target));
//						System.out.println("Done processing input");
						target.flush();
						target.close();
					}
					catch (TransformerException te) {
						te.printStackTrace(System.out);
						throw new IOException(te.getMessageAndLocation());
					}
				}
				catch (IOException e) {
					exception[0] = e;
				}
				finally {
					if (transformer instanceof PooledTransformer)
						((PooledTransformer) transformer).handBack();
				}
			}
		};
		
		return new PipedWriter(pr) {
			private boolean firstWrite = true;
			public void write(int c) throws IOException {
				this.wakeWriter();
				super.write(c);
			}
			public void write(char[] cbuf, int off, int len) throws IOException {
				this.wakeWriter();
				super.write(cbuf, off, len);
			}
			public void close() throws IOException {
				this.wakeWriter();
				try {
					super.flush();
				} catch (Exception e) {}
				super.close();
				try {
					t.join();
//					System.out.println("Transformer finished");
				} catch (InterruptedException ie) {}
			}
			public synchronized void flush() throws IOException {
				this.wakeWriter();
				super.flush();
			}
			private void wakeWriter() throws IOException {
				if (exception[0] != null)
					throw exception[0];
				
				if (this.firstWrite) {
					this.firstWrite = false;
					t.start();
//					System.out.println("Transformer started");
				}
			}
			/*
			 * no need for overwriting the other methods for writing, they all
			 * call one of the the one-argument and three-argument write() methods
			 */
		};
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located at a URL.
	 * @param xsltUrl the URL of the stylesheet to load
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified URL
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(URL xsltUrl) throws IOException {
		return getTransformer(xsltUrl, true, null);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located at a URL. If
	 * the argument cache folder is not null, the downloaded XSLT is cached in
	 * a file located there after download, and a download failure will fall
	 * back to previously cached files.
	 * @param xsltUrl the URL of the stylesheet to load
	 * @param cacheFolder the folder to cache an XSLT to after download
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified URL
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(URL xsltUrl, File cacheFolder) throws IOException {
		return getTransformer(xsltUrl, true, cacheFolder);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located at a URL.
	 * @param xsltUrl the URL of the stylesheet to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified URL.
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified URL
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(URL xsltUrl, boolean allowCache) throws IOException {
		return getTransformer(xsltUrl, allowCache, null);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located at a URL. If
	 * the argument cache folder is not null, the downloaded XSLT is cached in
	 * a file located there after download, and a download failure will fall
	 * back to previously cached files even if cache usage is not allowed.
	 * @param xsltUrl the URL of the stylesheet to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified URL.
	 * @param cacheFolder the folder to cache an XSLT to after download
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified URL
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(URL xsltUrl, boolean allowCache, File cacheFolder) throws IOException {
		if (xsltUrl == null)
			return null;
		InputStream xsltIn = new XsltInputStream(xsltUrl, cacheFolder);
		try {
			return getTransformer(xsltUrl.toString(), xsltIn, allowCache);
		}
		finally {
			xsltIn.close();
		}
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located in a file.
	 * @param xsltFile the file containing the stylesheet to load
	 * @return an XSLT transformer produced from the stylesheet located in the
	 *         specified file
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(File xsltFile) throws IOException {
		return getTransformer(xsltFile, true);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located in a file.
	 * @param xsltFile the file containing the stylesheet to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified file.
	 * @return an XSLT transformer produced from the stylesheet located in the
	 *         specified file
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(File xsltFile, boolean allowCache) throws IOException {
		if (xsltFile == null)
			return null;
		InputStream xsltIn = new XsltInputStream(xsltFile);
		try {
			return getTransformer(xsltFile.getAbsolutePath(), xsltIn, allowCache);
		}
		finally {
			xsltIn.close();
		}
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located in a file or
	 * at a URL. If the specified address starts with  &quot;http://&quot;, it
	 * is interpreted as a URL, otherwise as a file name.
	 * @param xsltAddress the address containing the stylesheet to load
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified address
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(String xsltAddress) throws IOException {
		return getTransformer(xsltAddress, true);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located in a file or
	 * at a URL. If the specified address starts with  &quot;http://&quot;, it
	 * is interpreted as a URL, otherwise as a file name. If the argument
	 * string is a URL and the argument cache folder is not null, a downloaded
	 * XSLT is cached in a file located there after download, and a download
	 * failure will fall back to previously cached files.
	 * @param xsltAddress the address containing the stylesheet to load
	 * @param cacheFolder the folder to cache an XSLT to after download
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified address
	 * @throws IOException
	 */
	public static TransformerPool getTransformer(String xsltAddress, File cacheFolder) throws IOException {
		return getTransformer(xsltAddress, true, cacheFolder);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located in a file or
	 * at a URL. If the specified address starts with &quot;http://&quot;, it
	 * is interpreted as a URL, otherwise as a file name.
	 * @param xsltAddress the address containing the stylesheet to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified address.
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified address
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String xsltAddress, boolean allowCache) throws IOException {
		return getTransformer(xsltAddress, allowCache, null);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet located in a file or
	 * at a URL. If the specified address starts with &quot;http://&quot;, it
	 * is interpreted as a URL, otherwise as a file name. If the argument
	 * string is a URL and the argument cache folder is not null, a downloaded
	 * XSLT is cached in a file located there after download, and a download
	 * failure will fall back to previously cached files even if cache usage is
	 * not allowed.
	 * @param xsltAddress the address containing the stylesheet to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified address.
	 * @param cacheFolder the folder to cache an XSLT to after download
	 * @return an XSLT transformer produced from the stylesheet located at the
	 *         specified address
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String xsltAddress, boolean allowCache, File cacheFolder) throws IOException {
		if (xsltAddress.startsWith("http://") || xsltAddress.startsWith("https://"))
			return getTransformer(new URL(xsltAddress), allowCache, cacheFolder);
		else return getTransformer(new File(xsltAddress), allowCache);
	}
	
	/* this class allows us to open an actual input stream only after a cache miss */
	private static class XsltInputStream extends InputStream {
		private final File file;
		private final URL url;
		private long urlModTime;
		private final File urlCacheFolder;
		private BufferedInputStream in;
		XsltInputStream(File file) {
			this.file = file;
			this.url = null;
			this.urlCacheFolder = null;
		}
		XsltInputStream(URL url, File cacheFolder) {
			this.file = null;
			this.url = url;
			this.urlCacheFolder = cacheFolder;
		}
		private InputStream getInputStream() throws IOException {
			if (this.in != null)
				return this.in;
			if (this.file != null)
				this.in = new BufferedInputStream(new FileInputStream(this.file));
			if (this.url != null) try {
				HttpURLConnection urlCon = ((HttpURLConnection) this.url.openConnection());
				urlCon.connect();
				this.in = new BufferedInputStream(urlCon.getInputStream());
				this.urlModTime = urlCon.getLastModified();
			}
			catch (IOException ioe) /* try to fall back to cached result of previous download */ {
				File urlCacheFile = this.getUrlCacheFile();
				if ((urlCacheFile != null) && urlCacheFile.exists()) {
					System.out.println("XsltUtils: error loading XSLT from URL '" + this.url.toString() + "': " + ioe.getMessage());
					System.out.println("XsltUtils: falling back to local cache file '" + urlCacheFile.getAbsolutePath() + "'.");
					this.in = new BufferedInputStream(new FileInputStream(urlCacheFile));
					this.urlModTime = urlCacheFile.lastModified();
				}
				else throw ioe;
			}
 			return this.in;
		}
		public int read() throws IOException {
			return this.getInputStream().read();
		}
		public int read(byte[] b) throws IOException {
			return this.getInputStream().read(b);
		}
		public int read(byte[] b, int off, int len) throws IOException {
			return this.getInputStream().read(b, off, len);
		}
		public long skip(long n) throws IOException {
			return this.getInputStream().skip(n);
		}
		public int available() throws IOException {
			return this.getInputStream().available();
		}
		public void close() throws IOException {
			if (this.in != null)
				this.in.close();
		}
		public synchronized void mark(int readlimit) {
			try {
				this.getInputStream().mark(readlimit);
			} catch (IOException ioe) {}
		}
		public synchronized void reset() throws IOException {
			this.getInputStream().reset();
		}
		public boolean markSupported() {
			try {
				return this.getInputStream().markSupported();
			}
			catch (IOException ioe) {
				return false;
			}
		}
		private File getUrlCacheFile() {
			if (this.url == null)
				return null;
			if (this.urlCacheFolder == null)
				return null;
			String urlStr = this.url.toString();
			while (urlStr.endsWith("/"))
				urlStr = urlStr.substring(0, (urlStr.length() - "/".length()));
			urlStr = urlStr.replaceAll("[^A-Za-z0-9\\_\\-\\.]+", "_");
			return new File(this.urlCacheFolder, (urlStr + ".cached"));
		}
		void cacheDownloadResult(byte[] xsltBytes) {
			File urlCacheFile = this.getUrlCacheFile();
			if (urlCacheFile == null)
				return;
			if (urlCacheFile.exists() && (urlCacheFile.length() == xsltBytes.length) && (Math.abs(this.urlModTime - urlCacheFile.lastModified()) < 1000)) {
				System.out.println("XsltUtils: local cache file '" + urlCacheFile.getAbsolutePath() + "' up to date.");
				return;
			}
			try {
				this.urlCacheFolder.mkdirs();
				BufferedOutputStream xbOut = new BufferedOutputStream(new FileOutputStream(urlCacheFile));
				xbOut.write(xsltBytes);
				xbOut.flush();
				xbOut.close();
				if (this.urlModTime > 0)
					urlCacheFile.setLastModified(this.urlModTime);
				System.out.println("XsltUtils: cached XSLT from URL '" + this.url.toString() + "' in '" + urlCacheFile.getAbsolutePath() + "'.");
			}
			catch (IOException ioe) {
				System.out.println("XsltUtils: error caching XSLT from URL '" + this.url.toString() + "' in '" + urlCacheFile.getAbsolutePath() + "': " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet loaded from some
	 * input stream. If the argument name is null, there are no cache lookups,
	 * and the transformer pool will not be cached.
	 * @param xsltIn an input stream to load the stylesheet from
	 * @param name the name of the input stream, for caching
	 * @return an XSLT transformer produced from the specified stylesheet
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String name, InputStream xsltIn) throws IOException {
		return getTransformer(name, xsltIn, true);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet loaded from some
	 * input stream. If the argument name is null, there are no cache lookups,
	 * and the transformer pool will not be cached. This method reads the
	 * argument input stream through the end and closes it afterwards.
	 * @param xsltIn an input stream to load the stylesheet from
	 * @param name the name of the input stream, for caching
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified address.
	 * @return an XSLT transformer produced from the specified stylesheet
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String name, InputStream xsltIn, boolean allowCache) throws IOException {
		if (allowCache && (name != null) && transformerCache.containsKey(name)) {
//			System.out.println("XsltUtils: XSL Transformer Pool cache hit for '" + name + "'");
			return ((TransformerPool) transformerCache.get(name));
		}
		InputStream tis = new ByteOrderMarkFilterInputStream(xsltIn);
		ByteArrayOutputStream xsltByteBuf = new ByteArrayOutputStream();
		byte[] byteBuffer = new byte[1024];
		for (int r; (r = tis.read(byteBuffer, 0, byteBuffer.length)) != -1;)
			xsltByteBuf.write(byteBuffer, 0, r);
		tis.close();
		byte[] xsltBytes = xsltByteBuf.toByteArray();
		TransformerPool tp = doGetTransformer(name, xsltBytes, allowCache);
		if (xsltIn instanceof XsltInputStream)
			((XsltInputStream) xsltIn).cacheDownloadResult(xsltBytes);
		return tp;
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet loaded from some
	 * reader. If the argument name is null, there are no cache lookups, and
	 * the transformer pool will not be cached.
	 * @param xsltIn a reader to load the stylesheet from
	 * @param name the name of the input stream, for caching
	 * @return an XSLT transformer produced from the specified stylesheet
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String name, Reader xsltIn) throws IOException {
		return getTransformer(name, xsltIn, true);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet loaded from some
	 * reader. If the argument name is null, there are no cache lookups, and
	 * the transformer pool will not be cached. This method reads the argument
	 * reader through the end and closes it afterwards.
	 * @param xsltIn a reader to load the stylesheet from
	 * @param name the name of the input stream, for caching
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified address.
	 * @return an XSLT transformer produced from the specified stylesheet
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String name, Reader xsltIn, boolean allowCache) throws IOException {
		if (allowCache && (name != null) && transformerCache.containsKey(name)) {
//			System.out.println("XsltUtils: XSL Transformer Pool cache hit for '" + name + "'");
			return ((TransformerPool) transformerCache.get(name));
		}
		BufferedReader xsltReader = ((xsltIn instanceof BufferedReader) ? ((BufferedReader) xsltIn) : new BufferedReader(xsltIn));
		ByteArrayOutputStream xsltBytes = new ByteArrayOutputStream();
		String xsltLine;
		while ((xsltLine = xsltReader.readLine()) != null) {
			if (xsltBytes.size() == 0) {
				if (xsltLine.trim().length() == 0)
					continue;
				if (xsltLine.indexOf('<') > 0)
					xsltLine = xsltLine.substring(xsltLine.indexOf('<'));
			}
			xsltBytes.write(xsltLine.getBytes("UTF-8"));
			xsltBytes.write('\r');
			xsltBytes.write('\n');
		}
		xsltReader.close();
		return doGetTransformer(name, xsltBytes.toByteArray(), allowCache);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet. If the argument name
	 * is null, there are no cache lookups, and the transformer pool will not
	 * be cached.
	 * @param name the name of the input stream, for caching
	 * @param xsltBytes an array holding the bytes of the stylesheet to load
	 * @return an XSLT transformer produced from the specified stylesheet
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String name, byte[] xsltBytes) throws IOException {
		return getTransformer(name, xsltBytes, true);
	}
	
	/**
	 * Produce an XSLT transformer pool from a stylesheet. If the argument name
	 * is null, there are no cache lookups, and the transformer pool will not
	 * be cached.
	 * @param name the name of the input stream, for caching
	 * @param xsltBytes an array holding the bytes of the stylesheet to load
	 * @param allowCache allow using cached transformers? Specifying false
	 *            forces loading the transformer from the specified address.
	 * @return an XSLT transformer produced from the specified stylesheet
	 * @throws IOException
	 */
	public static synchronized TransformerPool getTransformer(String name, byte[] xsltBytes, boolean allowCache) throws IOException {
		byte[] cXsltBytes = new byte[xsltBytes.length];
		System.arraycopy(xsltBytes, 0, cXsltBytes, 0, xsltBytes.length);
		return doGetTransformer(name, cXsltBytes, allowCache);
	}
	
	/*
	 * this extra method is required so we can copy byte arrays that external
	 * code might have a reference to, but do not need to copy byte arrays we've
	 * read from some stream to achieve this.
	 */
	private static synchronized TransformerPool doGetTransformer(String name, byte[] xsltBytes, boolean allowCache) throws IOException {
		if (allowCache && (name != null) && transformerCache.containsKey(name)) {
//			System.out.println("XsltUtils: XSL Transformer Pool cache hit for '" + name + "'");
			return ((TransformerPool) transformerCache.get(name));
		}
		try {
			TransformerPool tp = new TransformerPool(xsltBytes);
			tp.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//			System.out.println("XsltUtils: loaded XSL Transformer Pool from '" + name + "'");
			if (name != null)
				transformerCache.put(name, tp);
			return tp;
		}
		catch (Exception e) {
			throw new IOException(e.getClass().getName() + " (" + e.getMessage() + ") while creating XSL Transformer Pool from '" + name + "'.");
		}
	}
	
	private static HashMap transformerCache = new LinkedHashMap(32, 0.75f, true) {
		protected boolean removeEldestEntry(Entry eldest) {
			return (this.size() > 256);
		}
	}; // no need for synchronized map, accessed only from synchronized code
	
	private static boolean transformerFactoryTestedForCaching = false;
	private static TransformerFactory transformerFactory = null;
	
	private static synchronized Transformer produceTransformer(byte[] stylesheet) throws TransformerConfigurationException {
		
		//	we have tested the installed factory for caching behavior and not made it available, so it is caching 
		if (transformerFactoryTestedForCaching)
			return null;
		
		//	caching behavior yet to be tested, do it right now
		try {
			System.out.println("XsltUtils: testing XSL Transformer Factory for internal caching behavior ...");
			TransformerFactory tf = TransformerFactory.newInstance();
			System.out.println(" - got XSL Transformer Factory instance");
			Transformer t1 = tf.newTransformer(new StreamSource(new InputStreamReader(new ByteArrayInputStream(stylesheet), "UTF-8")));
			System.out.println(" - got XSL Transformer instance 1");
			Transformer t2 = tf.newTransformer(new StreamSource(new InputStreamReader(new ByteArrayInputStream(stylesheet), "UTF-8")));
			System.out.println(" - got XSL Transformer instance 2");
			if (t1 != t2) {
				System.out.println(" - found factory to not be caching internally, can use centralized instance");
				transformerFactory = tf;
			}
			else System.out.println(" - found factory to be caching internally, cannot use centralized instance");
			transformerFactoryTestedForCaching = true;
			return t1;
		}
		catch (UnsupportedEncodingException uee) {
			return null; // not going to happen with UTF-8, but Java don't know ...
		}
	}
	
	/**
	 * This class mimics the interface of javax.xml.transform.Transformer. As
	 * opposed to the latter transformers, however, instances of this class
	 * <b>are</b> safe to use by multiple threads concurrently. They hold a
	 * pool of actual transformers internally, making sure each is used by only
	 * one thread at a time. The size of the internal transformer pool can be
	 * modified via the setTransformerPoolSize() method. Larger pool sizes
	 * result in better performance in high-load situations because no extra
	 * transformers have to be created to meet demand. However, this comes at
	 * the cost of some memory for keeping the transformers. The default pool
	 * size is 3. However, new transformers are only created if all existing
	 * ones are in use when another transformer is needed.<br>
	 * Parameters and properties set with this class are set with all
	 * transformers in the pool. Thus, implementations with highly
	 * purpose-specific parameters and properties should not use this class
	 * directly for their transformations, but instead retrieve an actual
	 * transformer from the getTransformer() method.<br>
	 * Error handlers are not accessible from this class directly. Thus,
	 * implementations requiring access to error handlers should not use this
	 * class directly for their transformations, but instead retrieve an actual
	 * transformer from the getTransformer() method.
	 * 
	 * @author sautter
	 */
	public static class TransformerPool extends Transformer {
		private byte[] stylesheet;
		private Transformer model;
		private HashSet usedElementNames = null;
		
		private HashMap parameters = new HashMap(3);
		private Properties properties;
		private URIResolver uriResolver;
		private ErrorListener errorListener;
		
		private LinkedList transformerPool = new LinkedList();
		private int transformerPoolSize = 3;
		
		TransformerPool(byte[] stylesheet) throws TransformerConfigurationException {
			this.stylesheet = stylesheet;
			this.model = this.produceTransformer();
			this.properties = new Properties(this.model.getOutputProperties());
		}

		private Transformer produceTransformer() throws TransformerConfigurationException {
			try {
				//	we have a centralized transformer factory, so we know it does not cache
				if (transformerFactory != null)
					return transformerFactory.newTransformer(new StreamSource(new InputStreamReader(new ByteArrayInputStream(stylesheet), "UTF-8")));
				
				//	caching behavior yet to be tested, do it right now
				if (!transformerFactoryTestedForCaching)
					return XsltUtils.produceTransformer(this.stylesheet);
				
				/*
				 * we have to create a new transformer factory for every
				 * transformer because installed factory implementation caches
				 * transformer instances internally, which effectively prevents
				 * the parallel existence of multiple transformers in the pool
				 */
				return TransformerFactory.newInstance().newTransformer(new StreamSource(new InputStreamReader(new ByteArrayInputStream(this.stylesheet), "UTF-8")));
			}
			catch (UnsupportedEncodingException uee) {
				return null; // not going to happen with UTF-8, but Java don't know ...
			}
		}
		
		HashSet getUsedElementNames() {
			if (this.usedElementNames == null)
				this.usedElementNames = XsltUtils.getUsedElementNames(this.stylesheet);
			return this.usedElementNames;
		}
		
		/**
		 * Obtain a transformer from the pool for explicit configuration an use.
		 * If no transformer is available in the pool due to heavy use, a new
		 * transformer is created.
		 * @return a transformer from the pool
		 */
		public synchronized PooledTransformer getTransformer() {
//			System.out.println("TransformerPool: retrieving transformer ...");
			PooledTransformer pt;
			try {
				if (this.transformerPool.isEmpty()) {
					pt = new PooledTransformer(this, this.produceTransformer());
//					System.out.println(" - transformer created, pool was empty");
				}
				else {
					pt = ((PooledTransformer) this.transformerPool.removeFirst());
//					System.out.println(" - transformer found in pool");
				}
			}
			catch (TransformerConfigurationException tce) {
				return null; // not going to happen after constructor goes OK, but Java don't know ...
			}
			pt.prepare(this.parameters, this.properties, this.uriResolver, this.errorListener);
//			System.out.println(" - transformer prepared");
			return pt;
		}
		
		synchronized void handBack(PooledTransformer pt) {
//			System.out.println("TransformerPool: getting back transformer ...");
			if (this.transformerPool.size() < this.transformerPoolSize) {
				this.transformerPool.addLast(pt);
//				System.out.println(" - transformer stored in pool");
			}
//			else System.out.println(" - transformer discarded, pool was full");
		}
		
		/**
		 * @return the number of transformers currently in the pool
		 */
		public int getTransformerPoolLevel() {
			return this.transformerPool.size();
		}
		
		/**
		 * @return the size of the transformer pool, i.e., the maximum number of
		 *         transformers the pool may contain
		 */
		public int getTransformerPoolSize() {
			return this.transformerPoolSize;
		}
		
		/**
		 * Set the maximum size the maximum number of transformers the pool may
		 * contain. Larger pool sizes result in better performance in high-load
		 * situations because no extra transformers have to be created to meet
		 * demand. However, this comes at the cost of some memory for keeping
		 * the transformers. The default pool size is 3.
		 * @param tps the new transformer pool size (must be 1 or greater)
		 */
		public void setTransformerPoolSize(int tps) {
			if (tps < 1)
				throw new IllegalArgumentException("Transformer pool size cannot be less than 1.");
			this.transformerPoolSize = tps;
			while (this.transformerPool.size() > this.transformerPoolSize)
				this.transformerPool.removeFirst();
		}
		
		/**
		 * This implementation retrieves a transformer from the pool, uses it,
		 * and returns it to the pool by calling its handBack() method.
		 * @see javax.xml.transform.Transformer#transform(javax.xml.transform.Source, javax.xml.transform.Result)
		 */
		public void transform(Source xmlSource, Result outputTarget) throws TransformerException {
			PooledTransformer pt = this.getTransformer();
			try {
				pt.transform(xmlSource, outputTarget);
			}
			finally {
				pt.handBack();
			}
		}
		
		/**
		 * This implementation works on the centralized parameter store of the
		 * transformer pool. The parameters are put into individual transformers
		 * right before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#getParameter(java.lang.String)
		 */
		public synchronized Object getParameter(String name) {
			return this.parameters.get(name);
		}
		
		/**
		 * This implementation works on the centralized parameter store of the
		 * transformer pool. The parameters are put into individual transformers
		 * right before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#setParameter(java.lang.String, java.lang.Object)
		 */
		public synchronized void setParameter(String name, Object value) {
			this.parameters.put(name, value);
		}
		
		/**
		 * This implementation works on the centralized parameter store of the
		 * transformer pool. The parameters are put into individual transformers
		 * right before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#clearParameters()
		 */
		public synchronized void clearParameters() {
			this.parameters.clear();
		}
		
		/**
		 * This implementation set the centralized URI resolver of the
		 * transformer pool, which is put into individual transformers right
		 * before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#setURIResolver(javax.xml.transform.URIResolver)
		 */
		public synchronized void setURIResolver(URIResolver resolver) {
			this.uriResolver = resolver;
		}
		
		/**
		 * This implementation returns the centralized URI resolver of the
		 * transformer pool, which is put into individual transformers right
		 * before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#getURIResolver()
		 */
		public synchronized URIResolver getURIResolver() {
			return this.uriResolver;
		}
		
		/**
		 * This implementation writes to the centralized property store of the
		 * transformer pool, which also contains the properties set in the
		 * stylesheet. The properties are put into individual transformers right
		 * before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#setOutputProperties(java.util.Properties)
		 */
		public synchronized void setOutputProperties(Properties oformat) {
			if (oformat == null)
				this.properties.clear();
			else this.properties.putAll(oformat);
		}
		
		/**
		 * This implementation returns a view of the centralized property store
		 * of the transformer pool, which also contains the properties set in
		 * the stylesheet. The properties are put into individual transformers
		 * right before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#getOutputProperties()
		 */
		public synchronized Properties getOutputProperties() {
			return new Properties(this.properties);
		}
		
		/**
		 * This implementation works on the centralized property store of the
		 * transformer pool, which also contains the properties set in the
		 * stylesheet. The properties are put into individual transformers right
		 * before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#setOutputProperty(java.lang.String, java.lang.String)
		 */
		public synchronized void setOutputProperty(String name, String value) throws IllegalArgumentException {
			this.properties.setProperty(name, value);
		}
		
		/**
		 * This implementation works on the centralized property store of the
		 * transformer pool. The properties are put into individual transformers
		 * right before they are returned from the getTransformer() method, and
		 * cleared again when a pooled transformer is returned to the pool by
		 * calling its handBack() method.
		 * @see javax.xml.transform.Transformer#getOutputProperty(java.lang.String)
		 */
		public synchronized String getOutputProperty(String name) throws IllegalArgumentException {
			return this.properties.getProperty(name);
		}
		
		/**
		 * This implementation does nothing, as instances of this class do not
		 * have any transformation specific internal state. It is simply
		 * provided for interface compliance.
		 * @see javax.xml.transform.Transformer#reset()
		 */
		public void reset() {}
		
		/**
		 * This implementation does not have any effect because instances of
		 * this class do not perform transformations themselves but internally
		 * delegate to actual transformers held in an internal pool for
		 * multi-thread use. Implementations requiring access to error handlers
		 * should not use this class directly for their transformations, but
		 * instead retrieve an actual transformer from the getTransformer()
		 * method.
		 * @see javax.xml.transform.Transformer#setErrorListener(javax.xml.transform.ErrorListener)
		 */
		public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
			this.errorListener = listener;
		}
		
		/**
		 * This implementation always returns null because instances of this
		 * class do not perform transformations themselves but internally
		 * delegate to actual transformers held in an internal pool for
		 * multi-thread use. Implementations requiring access to error handlers
		 * should not use this class directly for their transformations, but
		 * instead retrieve an actual transformer from the getTransformer()
		 * method.
		 * @see javax.xml.transform.Transformer#getErrorListener()
		 */
		public ErrorListener getErrorListener() {
			return this.errorListener;
		}
		
		/**
		 * This class acts as a wrapper for transformers produced by the
		 * transformer factory installed in the local JVM. All methods loop
		 * through to the wrapped transformer. Instances of this class are not
		 * safe to use by multiple threads concurrently. After a piece of code
		 * is done with a pooled transformer object, it should invoke its
		 * handBack() method, which adds it back to its parent transformer pool
		 * and resets the parameters and properties to the pool defaults.
		 * 
		 * @author sautter
		 */
		public class PooledTransformer extends Transformer {
			private TransformerPool parent;
			private Transformer transformer;
			PooledTransformer(TransformerPool parent, Transformer transformer) {
				this.parent = parent;
				this.transformer = transformer;
			}
			void prepare(HashMap parameters, Properties outFormat, URIResolver uriResolver, ErrorListener errorListener) {
				for (Iterator pit = parameters.keySet().iterator(); pit.hasNext();) {
					String name = ((String) pit.next());
					this.transformer.setParameter(name, parameters.get(name));
				}
				
				for (Iterator pit = outFormat.keySet().iterator(); pit.hasNext();) {
					String name = ((String) pit.next());
					this.transformer.setOutputProperty(name, outFormat.getProperty(name));
				}
				
				this.transformer.setURIResolver(uriResolver);
				
				if (errorListener != null)
					this.transformer.setErrorListener(errorListener);
			}
			HashSet getUsedElementNames() {
				return this.parent.getUsedElementNames();
			}
			
			/**
			 * Put the pooled transformer back into the pool. This method will
			 * also clear all parameters and properties set after retrieving the
			 * transformer from the pool, so it might behave differently
			 * afterward. Code using this class should not use a transformer
			 * after invoking this method. In fact, invoking this method should
			 * be the last thing done before letting go of the reference to the
			 * pooled transformer.
			 */
			public void handBack() {
				this.reset();
				this.parent.handBack(this);
			}
			public Object getParameter(String name) {
				return this.transformer.getParameter(name);
			}
			public void setParameter(String name, Object value) {
				this.transformer.setParameter(name, value);
			}
			public void clearParameters() {
				this.transformer.clearParameters();
			}
			public String getOutputProperty(String name) throws IllegalArgumentException {
				return this.transformer.getOutputProperty(name);
			}
			public void setOutputProperty(String name, String value) throws IllegalArgumentException {
				this.transformer.setOutputProperty(name, value);
			}
			public Properties getOutputProperties() {
				return this.transformer.getOutputProperties();
			}
			public void setOutputProperties(Properties oformat) {
				this.transformer.setOutputProperties(oformat);
			}
			public ErrorListener getErrorListener() {
				return this.transformer.getErrorListener();
			}
			public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
				this.transformer.setErrorListener(listener);
			}
			public URIResolver getURIResolver() {
				return this.transformer.getURIResolver();
			}
			public void setURIResolver(URIResolver resolver) {
				this.transformer.setURIResolver(resolver);
			}
			public void reset() {
				try {
					this.transformer.reset();
				} catch (UnsupportedOperationException uoe) {}
			}
			public void transform(Source xmlSource, Result outputTarget) throws TransformerException {
				this.transformer.transform(xmlSource, outputTarget);
			}
		}
	}
	
	/**
	 * Retrieve the names of the input XML elements used by a transformer. If
	 * the argument transformer was not loaded via this class, this method
	 * returns null. If the argument transformer uses wildcard matches, the
	 * returned set contains <code>*</code> to indicate so.
	 * @param transformer the transformer to check
	 * @return a set containing the used elements
	 */
	public static HashSet getUsedElementNames(Transformer transformer) {
		if (transformer instanceof TransformerPool)
			return ((TransformerPool) transformer).getUsedElementNames();
		else if (transformer instanceof PooledTransformer)
			return ((PooledTransformer) transformer).getUsedElementNames();
		else return null;
	}
	
	private static final Properties expressionAttributeNames = new Properties();
	static {
		expressionAttributeNames.setProperty("xsl:apply-templates", "select");
		expressionAttributeNames.setProperty("xsl:for-each", "select");
		expressionAttributeNames.setProperty("xsl:if", "test");
		expressionAttributeNames.setProperty("xsl:number", "count");
		expressionAttributeNames.setProperty("xsl:param", "select");
		expressionAttributeNames.setProperty("xsl:sort", "select");
		expressionAttributeNames.setProperty("xsl:template", "match");
		expressionAttributeNames.setProperty("xsl:value-of", "select");
		expressionAttributeNames.setProperty("xsl:variable", "select");
		expressionAttributeNames.setProperty("xsl:when", "test");
		expressionAttributeNames.setProperty("xsl:with-param", "select");
		expressionAttributeNames.setProperty("xslt:apply-templates", "select");
		expressionAttributeNames.setProperty("xslt:for-each", "select");
		expressionAttributeNames.setProperty("xslt:if", "test");
		expressionAttributeNames.setProperty("xslt:number", "count");
		expressionAttributeNames.setProperty("xslt:param", "select");
		expressionAttributeNames.setProperty("xslt:sort", "select");
		expressionAttributeNames.setProperty("xslt:template", "match");
		expressionAttributeNames.setProperty("xslt:value-of", "select");
		expressionAttributeNames.setProperty("xslt:variable", "select");
		expressionAttributeNames.setProperty("xslt:when", "test");
		expressionAttributeNames.setProperty("xslt:with-param", "select");
	}
	
	private static final Grammar xmlGrammar = new StandardGrammar();
	private static final Parser xmlParser = new Parser(xmlGrammar);
	private static final Pattern qNamePattern = Pattern.compile("(" +
			"((([a-zA-Z][a-zA-Z0-9\\_\\-]+)|\\*)\\:)?" +
			"[a-zA-Z\\_\\-][a-zA-Z0-9\\_\\-]+\\*?" +
			")", Pattern.CASE_INSENSITIVE);
	
	private static final HashSet getUsedElementNames(byte[] xsltBytes) {
		final HashSet usedElementNames = new HashSet();
		try {
			xmlParser.stream(new ByteArrayInputStream(xsltBytes), new TokenReceiver() {
				public void storeToken(String token, int treeDepth) throws IOException {
					if (xmlGrammar.isTag(token)) {
						if (xmlGrammar.isEndTag(token))
							return;
						String type = xmlGrammar.getType(token).toLowerCase();
						if (type.startsWith("xsl:") || type.startsWith("xslt:")) {
							String attributeName = expressionAttributeNames.getProperty(type);
							if (attributeName == null)
								return;
							TreeNodeAttributeSet tnas = TreeNodeAttributeSet.getTagAttributes(token, xmlGrammar);
							String expression = tnas.getAttribute(attributeName);
							if (expression == null)
								return;
							Matcher qNameMatcher = qNamePattern.matcher(expression);
							while (qNameMatcher.find()) {
								String qName = qNameMatcher.group(0);
								if (qNameMatcher.start(0) == 0) {}
								else if ("@'\"".indexOf(expression.charAt(qNameMatcher.start(0)-1)) != -1)
									continue; // skip attribute names and string literals
								else if (qNameMatcher.end(0) == expression.length()) {}
								else if ("('\"".indexOf(expression.charAt(qNameMatcher.end(0))) != -1)
									continue; // skip function names and more string literals
								else if (expression.startsWith("::", qNameMatcher.end(0)))
									continue; // skip axis names
								usedElementNames.add(qName);
								if (qName.indexOf('*') != -1)
									usedElementNames.add("*");
							}
						}
					}
				}
				public void close() throws IOException {}
			});
		}
		catch (IOException ioe) {
			usedElementNames.add("*");
		}
		return usedElementNames;
	}
}
