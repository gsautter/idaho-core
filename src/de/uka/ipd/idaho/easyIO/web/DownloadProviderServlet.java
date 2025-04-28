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
package de.uka.ipd.idaho.easyIO.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class represents a simple data download provider, capable of making
 * data items available as part of a web application, independent of where the
 * actual data is stored on the backing machine.
 * 
 * @author sautter
 */
public class DownloadProviderServlet extends WebServlet {
	
	/** the folder to serve data from */
	protected File downloadDataFolder;
	
	/** the name of the file to return when a folder is accessed (defaults to 'index.html') */
	protected String folderIndexFileName;
	
	/** usual zero-argument constructor for class loading */
	public DownloadProviderServlet() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.WebServlet#reInit()
	 */
	protected void reInit() throws ServletException {
		
		//	load data folder and default file
		this.downloadDataFolder = new File(this.getSetting("dataFolder"));
		this.folderIndexFileName = this.getSetting("folderIndexFileName", "index.html");
	}
	
	/**
	 * Obtain the file containing the data to return for a given request. This
	 * default implementation uses the path info part of the request path as a
	 * path relative to the configured download data folder. Subclasses are
	 * welcome to overwrite this method with other means of access. If this
	 * method returns null, the request will be answered with an HTTP 404 
	 * 'Not Found' error.
	 * @param request the request to obtain the file for
	 * @return the requested file
	 */
	protected File getDownloadDataFile(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null)
			return null;
		while (pathInfo.startsWith("/"))
			pathInfo = pathInfo.substring("/".length());
		File ddFile = new File(this.downloadDataFolder, pathInfo);
		if (ddFile.isDirectory()) {
			File diFile = new File(ddFile, this.folderIndexFileName);
			return (diFile.exists() ? diFile : null);
		}
		else return (ddFile.exists() ? ddFile : null);
	}
	
	/**
	 * Obtain the MIME type to indicate for a given file. This default
	 * implementation goes by file extension. Subclasses are welcome to
	 * overwrite this method with other means of access.
	 * @param file the file to get the MIME type for
	 * @return the the MIME type of the argument file
	 */
	protected String getMimeType(File file) {
		String fileName = file.getName().toLowerCase();
		if (fileName.lastIndexOf(".") == -1)
			return "application/octet-stream";
		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
		return mimeTypes.getProperty(fileExtension, "application/octet-stream");
	}
	
	private static final Properties mimeTypes = new Properties();
	static {
		mimeTypes.setProperty(".js", "application/javascript");
		mimeTypes.setProperty(".json", "application/json");
		mimeTypes.setProperty(".pdf", "application/pdf");
		mimeTypes.setProperty(".zip", "application/zip");
		mimeTypes.setProperty(".gif", "image/gif");
		mimeTypes.setProperty(".jpg", "image/jpeg");
		mimeTypes.setProperty(".png", "image/png");
		mimeTypes.setProperty(".svg", "image/svg+xml");
		mimeTypes.setProperty(".css", "text/css");
		mimeTypes.setProperty(".csv", "text/csv");
		mimeTypes.setProperty(".htm", "text/html");
		mimeTypes.setProperty(".html", "text/html");
		mimeTypes.setProperty(".tsv", "text/plain");
		mimeTypes.setProperty(".txt", "text/plain");
		mimeTypes.setProperty(".xml", "text/xml");
		mimeTypes.setProperty(".xsl", "text/xml");
		mimeTypes.setProperty(".xslt", "text/xml");
		mimeTypes.setProperty(".xsd", "text/xml");
	}
//	private static final DateFormat MOD_TIME_DATE_FORMAT;
//	static {
//		MOD_TIME_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
//		MOD_TIME_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
//	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//	get requested file
		File sourceFile = this.getDownloadDataFile(request);
		
		//	get requested file name
		if (sourceFile == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		//	check 'If-Modified-Since' header (also HTTP timestamp)
		String ifModSince = request.getHeader("If-Modified-Since");
		if (ifModSince != null) try {
//			long minModTime = MOD_TIME_DATE_FORMAT.parse(ifModSince).getTime();
			long minModTime = parseHttpTimestamp(ifModSince);
			if (sourceFile.lastModified() < (minModTime + 1000 /* account for plain second based Linux file timestamps */)) {
				response.setHeader("Cache-Control", "no-cache");
//				response.setHeader("Last-Modified", MOD_TIME_DATE_FORMAT.format(new Date(sourceFile.lastModified())));
				response.setHeader("Last-Modified", formatHttpTimestamp(sourceFile.lastModified()));
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				Writer w = response.getWriter();
				w.write("");
				w.flush();
				return;
			}
		}
		catch (Exception e) {
			System.out.println("Error checking 'If-Modified-Since' header: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		
		//	set remaining headers
//		response.setHeader("Last-Modified", MOD_TIME_DATE_FORMAT.format(new Date(sourceFile.lastModified())));
		response.setHeader("Last-Modified", formatHttpTimestamp(sourceFile.lastModified()));
		response.setContentType(this.getMimeType(sourceFile));
		response.setContentLength((int) sourceFile.length());
		
		//	send data
		InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
		OutputStream out = new BufferedOutputStream(response.getOutputStream());
		byte[] buffer = new byte[2048];
		for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
			out.write(buffer, 0, r);
		out.flush();
		in.close();
	}
}
