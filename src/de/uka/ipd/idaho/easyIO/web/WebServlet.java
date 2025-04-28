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
package de.uka.ipd.idaho.easyIO.web;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import de.uka.ipd.idaho.easyIO.settings.Settings;

/**
 * Abstract class providing the basic IO facilities for service servlets. Each
 * servlet can have its data stored in a separate folder below the surrounding
 * web-app's WEB-INF folder, its so-called data path. The default data path is the
 * web-app's WEB-INF folder itself, but a specific data path can be specified as
 * the <b>dataPath</b> parameter in the web.xml.<br>
 * For sub class specific settings and parameters, each servlet in addition has
 * an instance specific configuration file, loaded from its data path. By
 * default, this file is named <b>config.cnfg</b>, but an alternative name can
 * be specified in an the <b>configFile</b> parameter in the web.xml.<br>
 * 
 * 
 * @author sautter
 */
public abstract class WebServlet extends HttpServlet implements WebConstants {
	
	/** the surrounding web-app's context path, i.e., its root folder */
	protected File rootFolder;
	
	/** the surrounding web-app's WEB-INF path */
	protected File webInfFolder;
	
	/** the folder to place caches in path */
	protected File cacheRootFolder;
	
	/** the surrounding web-app's host object */
	protected WebAppHost webAppHost;
	
	/**
	 * the servlet's data folder, nested inside the surrounding web-app's
	 * WEB-INF folder to protect files not explicitly exposed from read access;
	 * files intended to be accessible through HTTP have to be registered with
	 * the ResourceServlet class and will be served by the local instance of
	 * this class
	 */
	protected File dataFolder;
	
	/**
	 * the servlet's logical data path as a string, relative to the root path,
	 * as specified in the <code>web.xml</code>; this string is either empty,
	 * or it starts with a '/', in accordance to the usual return values of the
	 * <code>getContextPath()</code> and <code>getServletPath()</code> methods
	 * of <code>HttpServletRequest</code>)
	 */
	protected String dataPath;
	
	private Settings configStatic = new Settings();
	private Settings configDynamic = new Settings();
	private boolean configDynamicToLoad = true;
	private boolean configDynamicDirty = false;
	
	/**
	 * a settings object containing the settings from the servlet's
	 * configuration. Sub classes should use the <code>getSetting()</code>,
	 * <code>setSetting()</code>, and <code>removeSetting()</code> methods
	 * instead of accessing this object directly to facilitate automated
	 * storage of modified settings.
	 */
	protected Settings config = new WebServletSettings(this.configStatic, this.configDynamic);
	
	private class WebServletSettings extends Settings {
		private Settings ss;
		private Settings ds;
		WebServletSettings(Settings ss, Settings ds) {
			this.ss = ss;
			this.ds = ds;
		}
		public String setSetting(String key, String value) {
			String oldValue = this.ds.setSetting(key, value);
			configDynamicDirty = true;
			return oldValue;
		}
		public void setSettings(Settings settings) {
			this.ds.setSettings(settings);
			configDynamicDirty = true;
		}
		public void setProperties(Properties prop) {
			this.ds.setProperties(prop);
			configDynamicDirty = true;
		}
		public String getSetting(String key) {
			return this.ds.getSetting(key, this.ss.getSetting(key));
		}
		public String getSetting(String key, String def) {
			return this.ds.getSetting(key, this.ss.getSetting(key, def));
		}
		public String removeSetting(String key) {
			String oldValue = this.ds.removeSetting(key);
			configDynamicDirty = true;
			return oldValue;
		}
		public boolean containsKey(String key) {
			return (this.ss.containsKey(key) || this.ds.containsKey(key));
		}
		public boolean containsValue(String value) {
			return (this.ss.containsValue(value) || this.ds.containsValue(value));
		}
		public boolean hasPrefix(String prefix) {
			return this.ds.hasPrefix(prefix);
		}
		public String getPrefix() {
			return this.ds.getPrefix();
		}
		public String getFullPrefix() {
			return this.ds.getFullPrefix();
		}
		public void setPrefix(String newPrefix) {
			this.ds.setPrefix(newPrefix);
			configDynamicDirty = true;
		}
		public boolean hasSubset(String prefix) {
			return (this.ss.hasSubset(prefix) || this.ds.hasSubset(prefix));
		}
		public String[] getSubsetPrefixes() {
			return this.unionArrays(this.ss.getSubsetPrefixes(), this.ds.getSubsetPrefixes());
		}
		public String[] getLocalKeys() {
			return this.unionArrays(this.ss.getLocalKeys(), this.ds.getLocalKeys());
		}
		public String[] getKeys() {
			return this.unionArrays(this.ss.getKeys(), this.ds.getKeys());
		}
		public String[] getKeys(String prefix) {
			return this.unionArrays(this.ss.getKeys(prefix), this.ds.getKeys(prefix));
		}
		public String[] getFullKeys() {
			return this.unionArrays(this.ss.getFullKeys(), this.ds.getFullKeys());
		}
		public String[] getValues() {
			String[] ks = this.getFullKeys();
			String[] values = new String[ks.length];
			for (int k = 0; k < ks.length; k++)
				values[k] = this.getSetting(ks[k]);
			return values;
		}
		public int size() {
			return this.getFullKeys().length;
		}
		public boolean isEmpty() {
			return (this.ss.isEmpty() && this.ds.isEmpty());
		}
		public void clear() {
			this.ds.clear();
		}
		public Settings getSubset(String prefix) {
			Settings sss = this.ss.getSubset(prefix);
			Settings dss = this.ds.getSubset(prefix);
			return new WebServletSettings(sss, dss);
		}
		public void removeSubset(Settings subset) {
			this.ds.removeSubset(subset);
			configDynamicDirty = true;
		}
		String[] unionArrays(String[] strs1, String[] strs2) {
			TreeSet strs = new TreeSet();
			strs.addAll(Arrays.asList(strs1));
			strs.addAll(Arrays.asList(strs2));
			return ((String[]) strs.toArray(new String[strs.size()]));
		}
	}
	
	/**
	 * Load the servlet's configuration file. This file is expected to be
	 * located in the servlet's data path, and is named <code>config.cnfg</code>
	 * by default. The name can be overwritten by specifying an init
	 * parameter named <code>configFile</code> in the <code>web.xml</code>
	 * file of the web application. If the configuration is not loaded for the
	 * first time (e.g. on re-initialization), all settings modified via the
	 * <code>setSetting()</code> since the first invocation of this method are
	 * retained in their current values. Re-initializable servlets should call
	 * this method first thing in their implementation of the
	 * <code>reInit()</code> method.
	 */
	protected void loadConfig() {
		String configFile = this.getInitParameter("configFile");
		if (configFile == null)
			configFile = "config.cnfg";
		Settings configStatic = Settings.loadSettings(new File(this.dataFolder, configFile));
		this.configStatic.clear();
		this.configStatic.setSettings(configStatic);
		if (this.configDynamicToLoad) {
			configFile = (configFile + ".dynamic");
			Settings configDynamic = Settings.loadSettings(new File(this.dataFolder, configFile));
			this.configDynamic.clear();
			this.configDynamic.setSettings(configDynamic);
			this.configDynamicToLoad = false;
		}
	}
	
	/**
	 * This implementation loads the config file and determined the root and
	 * data paths. To prevent overwriting, it is final. Sub classes should
	 * overwrite the <code>doInit()</code> and/or <code>reInit()</code> methods
	 * instead, which exist exactly for this purpose and are called by this
	 * implementation.
	 * @see javax.servlet.GenericServlet#init()
	 */
	public final void init() throws ServletException {
		
		//	link up to webapp host
		this.webAppHost = WebAppHost.getInstance(this.getServletContext());
		this.webAppHost.registerServlet(this);
		
		//	get local environment
		this.rootFolder = this.webAppHost.getRootFolder();
		this.webInfFolder = this.webAppHost.getWebInfFolder();
		this.cacheRootFolder = this.webAppHost.getCacheRootFolder();
		
		this.dataPath = this.getInitParameter("dataPath");
		if (this.dataPath == null) {
			this.dataPath = "";
			this.dataFolder = this.webInfFolder;
		}
		else {
			this.dataPath = this.dataPath.trim();
			while (this.dataPath.startsWith("/"))
				this.dataPath = this.dataPath.substring(1);
			this.dataPath = this.dataPath.trim();
			if (this.dataPath.length() != 0)
				this.dataPath = ("/" + this.dataPath);
			this.dataFolder = new File(this.webInfFolder, this.dataPath);
		}
		
		//	load instance specific config file
		this.loadConfig();
		
		//	initialize sub class
		this.doInit();
		
		//	initialize reloadable settings (if any)
		this.reInit();
	}
	
	/**
	 * Do implementation specific one-off initialization. This method is called
	 * once when the servlet is loaded. It is intended for those parts of the
	 * initialization that are to persist throughout the lifetime of the
	 * servlet instance, like establishing a database connection. For parts of
	 * the initialization that can be modified at runtime, use the
	 * <code>reInit()</code> method. This default implementation does nothing
	 * by default, sub classes are welcome to overwrite it as needed.
	 * @throws ServletException
	 */
	protected void doInit() throws ServletException {}
	
	/**
	 * (Re-)initialize the servlet. This method is called when the servlet is
	 * loaded, after the <code>doInit()</code> method. It is intended for those
	 * parts of the initialization that can be modified during the lifetime of
	 * the servlet instance, like layout settings. For parts of the
	 * initialization that cannot be modified at runtime, use the
	 * <code>doInit()</code> method. This default implementation does nothing
	 * by default, sub classes are welcome to overwrite it as needed.
	 * @throws ServletException
	 */
	protected void reInit() throws ServletException {}
	
	/**
	 * Retrieve the external (web facing) context path of a servlet for a given
	 * request. By default, this method returns the context path of the argument
	 * request, or the value of the <code>externalContextPath</code> setting, if
	 * the latter is present. If required, subclasses are welcome to provide
	 * more sophisticated logic. This method mainly exists to facilitate the
	 * creation of proper hyperlinks if the servlt is deployed behind a proxy
	 * that uses rewrite rules to route requests to the servlet or changes the
	 * request path in other ways.
	 * @param request the request to get the web facing context path for
	 * @return the web facing context path for the argument request
	 */
	protected String getExternalContextPath(HttpServletRequest request) {
		String extPath = this.getSetting("externalContextPath");
		return ((extPath == null) ? request.getContextPath() : extPath);
	}
	
	/**
	 * Retrieve the external (web facing) servlet path of a servlet for a given
	 * request. By default, this method returns the servlet path of the argument
	 * request, or the value of the <code>externalServletPath</code> setting, if
	 * the latter is present. If required, subclasses are welcome to provide
	 * more sophisticated logic. This method mainly exists to facilitate the
	 * creation of proper hyperlinks if the servlt is deployed behind a proxy
	 * that uses rewrite rules to route requests to the servlet or changes the
	 * request path in other ways.
	 * @param request the request to get the web facing servlet path for
	 * @return the web facing servlet path for the argument request
	 */
	protected String getExternalServletPath(HttpServletRequest request) {
		String extPath = this.getSetting("externalServletPath");
		return ((extPath == null) ? request.getServletPath() : extPath);
	}
	
	/**
	 * Retrieve a setting from the servlet configuration. If the setting is not
	 * present in the particular servlet's configuration file, this method
	 * retrieves the setting from the webapp-wide configuration available from
	 * the webapp host. This is to facilitate setting default values for an
	 * entire webapp in one place and to overwrite them with servlet specific
	 * values where required.
	 * @param key the name of the setting
	 * @return the setting with the specified name
	 */
	protected String getSetting(String key) {
		String value = this.configDynamic.getSetting(key);
		if (value == null)
			value = this.configStatic.getSetting(key);
		return ((value == null) ? this.webAppHost.getSetting(key) : value);
	}
	
	/**
	 * Retrieve a setting from the node configuration. If the setting is not
	 * present in the particular servlet's configuration file, this method
	 * retrieves the setting from the webapp-wide configuration available from
	 * the webapp host. This is to facilitate setting default values for an
	 * entire webapp in one place and to overwrite them with servlet specific
	 * values where required.
	 * @param key the name of the setting
	 * @param def the default value to use if the setting does not exist
	 * @return the setting with the specified name, or the specified default
	 *         value if the setting does not exist
	 */
	protected String getSetting(String key, String def) {
		String value = this.configDynamic.getSetting(key);
		if (value == null)
			value = this.configStatic.getSetting(key);
		return ((value == null) ? this.webAppHost.getSetting(key, def) : value);
	}
	
	/**
	 * Store a setting. The change will be persisted automatically on shutdown.
	 * @param key the name of the setting
	 * @param value the value of the setting
	 * @return the previous value of the setting
	 */
	protected String setSetting(String key, String value) {
		if (value == null)
			return this.removeSetting(key);
		String oldValue = this.getSetting(key);
		this.configDynamic.setSetting(key, value);
		if (!value.equals(oldValue))
			this.configDynamicDirty = true;
		return oldValue;
	}
	
	/**
	 * Remove a setting. The change will be persisted automatically on shutdown.
	 * @param key the name of the setting
	 * @return the value of the removed setting
	 */
	protected String removeSetting(String key) {
		String oldValue = this.configDynamic.getSetting(key);
		if (oldValue != null)
			this.configDynamicDirty = true;
		return oldValue;
	}
	
	/**
	 * Persist the configuration of the servlet. This automatically happens on
	 * shutdown. This method facilitates to do so at any other point, e.g.
	 * after extensive modifications that a sub class wants to persist right
	 * away. This method also resets the <code>configDirty</code> flag if it
	 * was set by the <code>setConfigDirty()</code> method.
	 */
	protected synchronized void storeConfig() {
		if (!this.configDynamicDirty)
			return;
		String configFile = this.getInitParameter("configFile");
		if (configFile == null)
			configFile = "config.cnfg";
		configFile = (configFile + ".dynamic");
		try {
			Settings.storeSettingsAsText(new File(this.dataFolder, configFile), this.configDynamic);
			this.configDynamicDirty = false;
		} catch (Exception e) {}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public final void destroy() {
		this.exit();
		this.storeConfig();
	}
	
	/**
	 * Do sub class specific shutdown operations. This default implementation
	 * does nothing, sub classes are welcome to overwrite it as needed.
	 */
	protected void exit() {}
	
	/**
	 * Find a file with a given name somewhere in the data accessible to the
	 * host. The file is first sought in the servlet's data path, then in the
	 * surrounding web-app's WEB-INF folder, and finally in the surrounding
	 * web-app's root folder. The file name can also contain relative paths.
	 * @param fileName the name of the file to find
	 * @return a file object pointing to the sought file, or null, if the file
	 *         does not exist in any of the folders
	 */
	public File findFile(String fileName) {
		File file = new File(this.dataFolder, fileName);
		if (!file.exists())
			file = new File(this.webInfFolder, fileName);
		if (!file.exists())
			file = new File(this.rootFolder, fileName);
		return (file.exists() ? file : null);
	}
	
	/**
	 * A parsed MIME type from the 'Accept' header in an HTTP request.
	 * Instances of this class order by their associated 'q' value and the
	 * position they were specified in.<br>
	 * This class is intended to simplify implementing content negotiation in
	 * client code and subclasses.
	 *  
	 * @author sautter
	 */
	public static class AcceptMimeType implements Comparable {
		
		/** the name of the type, e.g. 'text/xml' */
		public final String name;
		
		/** the position the type was specified in */
		public final int pos;
		
		/** the 'q' value associated with the type */
		public final float q;
		
		/**
		 * @param name the name of the type
		 * @param pos the position the type was specified in
		 */
		public AcceptMimeType(String name, int pos) {
			this(name, pos, 1.0f);
		}
		
		/**
		 * @param name the name of the type
		 * @param pos the position the type was specified in
		 * @param q the 'q' value associated with the type
		 */
		public AcceptMimeType(String name, int pos, float q) {
			this.name = name;
			this.pos = pos;
			this.q = q;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object obj) {
			if (this == obj)
				return 0;
			else if (obj instanceof AcceptMimeType) {
				AcceptMimeType amt = ((AcceptMimeType) obj);
				if (this.q == amt.q)
					return (this.pos - amt.pos); // ascending order in preference
				else return Float.compare(amt.q, this.q); // descending order in q value
			}
			else return -1;
		}
		
		/**
		 * Parse an accepted MIME type from a raw header value.
		 * @param mimeType the header value
		 * @param pos the position the header was listed in
		 * @return the parsed MIME type
		 */
		public static AcceptMimeType parseAcceptMimeType(String mimeType, int pos) {
			mimeType = mimeType.replace(" ", "").toLowerCase();
			if (mimeType.indexOf(";q=") == -1)
				return new AcceptMimeType(mimeType, pos); // q not specified, default to 1.0
			String q = mimeType.substring(mimeType.indexOf(";q=") + ";q=".length());
			mimeType = mimeType.substring(0, mimeType.indexOf(";q="));
			try {
				return new AcceptMimeType(mimeType, pos, Float.parseFloat(q));
			}
			catch (NumberFormatException nfe) {
				return new AcceptMimeType(mimeType, pos, 0.01f); // q value failed to parse, default to 0.01
			}
		}
		
		/**
		 * Parse the accepted MIME types from an HTTP request. If the argument
		 * request comes without an 'Accept' header, this method returns null.
		 * Thus, an array returned from this method always has at least one
		 * element.
		 * @param request the request whose 'Accept' header to extract
		 * @return an array holding the accepted MIME types
		 */
		public static AcceptMimeType[] getAcceptMimeTypes(HttpServletRequest request) {
			ArrayList amtList = null;
			for (Enumeration ahe = request.getHeaders("Accept"); ahe.hasMoreElements();) {
				String ahStr = ((String) ahe.nextElement());
				if (amtList == null)
					amtList = new ArrayList(8);
				String[] ahs = ahStr.split("\\s*\\,\\s*");
				for (int h = 0; h < ahs.length; h++)
					amtList.add(parseAcceptMimeType(ahs[h], amtList.size()));
			}
			if (amtList == null)
				return null;
			AcceptMimeType[] amts = ((AcceptMimeType[]) amtList.toArray(new AcceptMimeType[amtList.size()]));
			Arrays.sort(amts);
			return amts;
		}
	}
	
	/**
	 * Parse an HTTP timestamp into a numerical value.
	 * @param timestamp the timestamp to parse
	 * @return the numerical value of the argument timestapm
	 * @throws ParseException
	 */
	public static long parseHttpTimestamp(String timestamp) throws ParseException {
		SimpleDateFormat pdf = getParseDateFormat();
		try {
			pdf.setTimeZone(UTC);
			Date pts = pdf.parse(timestamp);
			return pts.getTime();
		}
		finally {
			returnParseDateFormat(pdf);
		}
	}
	private static LinkedList parseDateFormats = new LinkedList();
	private static TimeZone UTC = TimeZone.getTimeZone("UTC");
	private static SimpleDateFormat getParseDateFormat() {
		synchronized (parseDateFormats) {
			if (parseDateFormats.isEmpty())
				return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
			else return ((SimpleDateFormat) parseDateFormats.removeFirst());
		}
	}
	private static void returnParseDateFormat(SimpleDateFormat pdf) {
		synchronized (parseDateFormats) {
			parseDateFormats.addLast(pdf);
		}
	}
	
	/**
	 * Format a UTC timestamp for use in HTP headers.
	 * @param timestamp the timestamp to format
	 * @return the formatted timestamp
	 */
	public static String formatHttpTimestamp(long timestamp) {
		return outputDateFormat.format(new Date(timestamp));
	}
	private static final SimpleDateFormat outputDateFormat;
	static {
		outputDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'Z", Locale.US);
		outputDateFormat.setTimeZone(UTC);
	}
//	
//	//	FOR TEST PURPOSES ONLY !!!
//	public static void main(String[] args) throws Exception {
////		String ahStr = "text/html; q=1.0, text/*; q=0.8, image/gif; q=0.6, image/jpeg; q=0.6, image/*; q=0.5, */*; q=0.1"; // from https://en.wikipedia.org/wiki/Content_negotiation
//		String ahStr = "text/turtle;q=1, application/n-triples;q=.9, application/rdf+xml;q=.8, application/ld+json;q=.7, */*;q=.1 "; // from https://github.com/plazi/treatmentBank/issues/56
//		String[] ahs = ahStr.split("\\s*\\,\\s*");
//		for (int h = 0; h < ahs.length; h++) {
//			AcceptMimeType amt = AcceptMimeType.parseAcceptMimeType(ahs[h], h);
//			System.out.println(amt.pos + ": " + amt.name + " at " + amt.q);
//		}
//	}
}