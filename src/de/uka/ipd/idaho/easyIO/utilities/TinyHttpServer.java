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
package de.uka.ipd.idaho.easyIO.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Basic implementation of a minimal HTTP server. Even though this class has no
 * abstract methods, it is abstract, as client code has to overwrite at least
 * one of the <code>serviceXyz()</code> methods to output a meaningful response.
 * 
 * @author sautter
 */
public abstract class TinyHttpServer extends TinyServer {
	
	/** Constructor
	 * @param port the port to listen on
	 * @throws IOException if the argument port is invalid or occupied
	 */
	protected TinyHttpServer(int port) throws IOException {
		this(null, port);
	}
	
	/** Constructor
	 * @param name the name of the server (e.g. in log messages)
	 * @param port the port to listen on
	 * @throws IOException if the argument port is invalid or occupied
	 */
	protected TinyHttpServer(String name, int port) throws IOException {
		super(((name == null) ? "TinyHttpServer" : name), port);
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.utilities.TinyServer#service(java.net.Socket)
	 */
	protected void service(Socket sock) throws Exception {
		BufferedLineInputStream request = new BufferedLineInputStream(sock.getInputStream());
		BufferedLineOutputStream response = new BufferedLineOutputStream(sock.getOutputStream());
		try {
			this.service(request, response);
		}
		catch (Exception e) {
			if (response.written == 0) {
				ByteArrayOutputStream errorData = new ByteArrayOutputStream();
				PrintStream errorOut = new PrintStream(errorData, true);
				e.printStackTrace(errorOut);
				errorOut.flush();
				errorOut.close();
				this.writeStatus(500, null, response);
				this.writeHeader("Content-Type", "text/plain; charset=UTF-8", response);
				this.writeHeader("Content-Length", ("" + errorData.size()), response);
				this.writeDateHeader(-1, response);
				this.writeHeader("Server", "CoL-Local Tiny HTTP", response);
				this.writeHeader("Connection", "close", response);
				this.writeLineBreak(response);
				errorData.writeTo(response);
				this.writeLineBreak(response);
			}
			else throw e;
		}
		finally {
			request.close();
			response.flush();
			response.close();
		}
		if (this.verbose) System.out.println(this.name + ": request done");
	}
	private void service(BufferedLineInputStream request, BufferedLineOutputStream response) throws Exception {
		
		//	read header
		String firstLine = null;
		Properties headers = new Properties();
		for (String line; (line = request.readLine()) != null;) {
			
			//	store first line
			if (firstLine == null)
				firstLine = line;
			
			//	store header
			else if (line.indexOf(':') != -1)
				headers.setProperty(line.substring(0, line.indexOf(':')), line.substring(line.indexOf(':') + ":".length()).trim());
		}
		if (this.verbose) System.out.println(this.name + ": request header read");
		
		//	do we have a method?
		if (firstLine == null) {
			this.sendError("Empty header.", 400, response);
			return;
		}
		
		//	separate method, path, and query
		String method = firstLine.substring(0, firstLine.indexOf(' '));
		firstLine = firstLine.substring(firstLine.indexOf(' ') + " ".length());
		String path = firstLine.substring(0, firstLine.lastIndexOf(" HTTP/"));
		String query;
		if (path.indexOf('?') == -1)
			query = null;
		else {
			query = path.substring(path.indexOf('?') + "?".length());
			path = path.substring(0, path.indexOf('?'));
		}
		if (this.verbose) {
			System.out.println(this.name + ": request header parsed");
			System.out.println("- method is " + method);
			System.out.println("- path is " + path);
			System.out.println("- query is " + query);
			System.out.println("- headers are " + headers);
		}
		
		//	delegate to more specific method
		if ("GET".equals(method))
			this.serviceGet(path, query, headers, request, response);
		else if ("HEAD".equals(method))
			this.serviceHead(path, query, headers, request, response);
		else if ("POST".equals(method))
			this.servicePost(path, query, headers, request, response);
		else if ("PUT".equals(method))
			this.servicePut(path, query, headers, request, response);
		else if ("DELETE".equals(method))
			this.serviceDelete(path, query, headers, request, response);
		else if ("CONNECT".equals(method))
			this.serviceConnect(path, query, headers, request, response);
		else if ("OPTIONS".equals(method))
			this.serviceOptions(path, query, headers, request, response);
		else if ("TRACE".equals(method))
			this.serviceTrace(path, query, headers, request, response);
		else this.sendError(("Mehtod '" + method + "' is undefined."), 400, response);
	}
	
/* Source: https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol
GET: The GET method requests that the target resource transfers a representation of its state. GET requests should only retrieve data and should have no other effect. (This is also true of some other HTTP methods.)[1] The W3C has published guidance principles on this distinction, saying, "Web application design should be informed by the above principles, but also by the relevant limitations."[41] See safe methods below.
HEAD: The HEAD method requests that the target resource transfers a representation of its state, like for a GET request, but without the representation data enclosed in the response body. This is useful for retrieving the representation metadata in the response header, without having to transfer the entire representation.
POST: The POST method requests that the target resource processes the representation enclosed in the request according to the semantics of the target resource. For example, it is used for posting a message to an Internet forum, subscribing to a mailing list, or completing an online shopping transaction.[42]
PUT: The PUT method requests that the target resource creates or updates its state with the state defined by the representation enclosed in the request.[43]
DELETE: The DELETE method requests that the target resource deletes its state.
CONNECT: The CONNECT method request that the intermediary establishes a TCP/IP tunnel to the origin server identified by the request target. It is often used to secure connections through one or more HTTP proxies with TLS.[44][45][46] See HTTP CONNECT method.
OPTIONS: The OPTIONS method requests that the target resource transfers the HTTP methods that it supports. This can be used to check the functionality of a web server by requesting '*' instead of a specific resource.
TRACE: The TRACE method requests that the target resource transfers the received request in the response body. That way a client can see what (if any) changes or additions have been made by intermediaries.
PATCH: The PATCH method requests that the target resource modifies its state according to the partial update defined in the representation enclosed in the request.[47]
*/
	
	private static class BufferedLineInputStream extends BufferedInputStream {
		BufferedLineInputStream(InputStream in) {
			super(in);
		}
		String readLine() throws IOException {
			StringBuffer sb = new StringBuffer();
			for (int ch; (ch = this.read()) != -1;) {
				if (ch == '\r') {
					if ((this.pos < this.count) && (this.buf[this.pos] == '\n'))
						this.read(); // consume LF following after CR
					break;
				}
				else if (ch == '\n')
					break;
				else sb.append((char) ch); // only basic ASCII to be expected in headers, so this is safe
			}
			return ((sb.length() == 0) ? null : sb.toString());
		}
	}
	
	private static class BufferedLineOutputStream extends BufferedOutputStream {
		int written = 0;
		BufferedLineOutputStream(OutputStream out) {
			super(out);
		}
		public synchronized void write(int b) throws IOException {
			super.write(b);
			this.written++;
		}
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			super.write(b, off, len);
			this.written += len;
		}
		public void write(byte[] b) throws IOException {
			this.write(b, 0, b.length);
		}
	}
	
	/**
	 * Service an HTTP GET request. The request input stream is positioned at
	 * the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support GET.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void serviceGet(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("GET is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP HEAD request. The request input stream is positioned at
	 * the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support HEAD.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void serviceHead(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("HEAD is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP POST request. The request input stream is positioned at
	 * the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support POST.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void servicePost(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("POST is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP PUT request. The request input stream is positioned at
	 * the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support PUT.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void servicePut(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("PUT is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP DELETE request. The request input stream is positioned
	 * at the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support DELETE.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void serviceDelete(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("DELETE is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP CONNECT request. The request input stream is positioned
	 * at the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support CONNECT.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void serviceConnect(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("CONNECT is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP OPTIONS request. The request input stream is positioned
	 * at the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support OPTIONS.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void serviceOptions(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("OPTIONS is not supported.", 501, response);
	}
	
	/**
	 * Service an HTTP TRACE request. The request input stream is positioned at
	 * the start of the request body. No bytes have been written to the
	 * response output stream. Implementations need not close either one of the
	 * argument streams, as calling code does so. This default implementations
	 * sends a 'not implemented' error response, subclasses have to overwrite
	 * it to support TRACE.
	 * @param path the requested path
	 * @param query the query part of the request
	 * @param headers the request headers
	 * @param request the request input stream
	 * @param response the output stream to write the response to
	 * @throws Exception
	 */
	protected void serviceTrace(String path, String query, Properties headers, InputStream request, OutputStream response) throws Exception {
		this.sendError("TRACE is not supported.", 501, response);
	}
	
	/**
	 * Parse a query string into individual parameter/value pairs. Parameter
	 * values are unescaped.
	 * @param query the query to parse
	 * @return the parsed query
	 * @throws Exception
	 */
	protected Properties parseQuery(String query) throws Exception {
		if (query == null)
			return null;
		Properties pQuery = new Properties();
		String[] params = query.split("\\&");
		for (int p = 0; p < params.length; p++) {
			if (params[p].indexOf('=') == -1)
				continue;
			pQuery.setProperty(params[p].substring(0, params[p].indexOf('=')), URLDecoder.decode(params[p].substring(params[p].indexOf('=') + "=".length()), "UTF-8"));
		}
		return pQuery;
	}
	
	/**
	 * Send an error response. This method writes the status line, so client
	 * code must not write to the argument output stream before calling this
	 * method.
	 * @param message the error message
	 * @param code the error status code
	 * @param response the response output stream
	 * @throws Exception
	 */
	protected void sendError(String message, int code, OutputStream response) throws Exception {
		this.writeStatus(code, null, response);
		this.writeHeader("Content-Type", "text/plain; charset=UTF-8", response);
		this.writeHeader("Content-Length", ("" + message.length()), response);
		this.writeDateHeader(-1, response);
		this.writeHeader("Server", "Tiny HTTP Server", response);
		this.writeHeader("Connection", "close", response);
		this.writeLineBreak(response);
		response.write(message.getBytes());
		this.writeLineBreak(response);
	}
	
	/**
	 * Write the response status line. Since the status line is the first line
	 * of the response header, client code must not write to the argument
	 * output stream before calling this method.
	 * @param code the status code
	 * @param message the status message
	 * @param response the response output stream
	 * @throws Exception
	 */
	protected void writeStatus(int code, String message, OutputStream response) throws Exception {
		response.write(("HTTP/1.1 " + code).getBytes());
		if (message != null) {
			response.write((byte) ' ');
			response.write(message.getBytes());
		}
		this.writeLineBreak(response);
	}
	
	/**
	 * Write a response header. The argument strings must exclusively consist
	 * of ASCII characters and may not include control characters.
	 * @param name the header name
	 * @param value the header value
	 * @param response the response output stream
	 * @throws Exception
	 */
	protected void writeHeader(String name, String value, OutputStream response) throws Exception {
		response.write(name.getBytes());
		response.write((byte) ':');
		response.write((byte) ' ');
		response.write(value.getBytes());
		this.writeLineBreak(response);
	}
	
	private static final DateFormat timestampDateFormat;
	static {
		timestampDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
		timestampDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	/**
	 * Write the response date header. If the argument time is negative, the
	 * current time is used instead.
	 * @param time the timestamp to output as the date
	 * @param response the response output stream
	 * @throws Exception
	 */
	protected void writeDateHeader(long time, OutputStream response) throws Exception {
		this.writeHeader("Date", timestampDateFormat.format(new Date((time < 0) ? System.currentTimeMillis() : time)), response);
	}
	
	/**
	 * Write a line break, consisting of a carriage return and a line feed.
	 * @param response the response output stream
	 * @throws Exception
	 */
	protected void writeLineBreak(OutputStream response) throws Exception {
		response.write((byte) '\r');
		response.write((byte) '\n');
	}
}
