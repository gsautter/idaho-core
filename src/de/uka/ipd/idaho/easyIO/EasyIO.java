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
package de.uka.ipd.idaho.easyIO;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.easyIO.sql.ResultSizeLimitationClause;
import de.uka.ipd.idaho.easyIO.sql.SyntaxHelper;
import de.uka.ipd.idaho.easyIO.sql.TableDefinition;
import de.uka.ipd.idaho.stringUtils.StringUtils;

/**
 * Utility library for various IO purposes. In particular, this library can:
 * <ul>
 * <li>read and write files</li>
 * <li>escape and un-escape string values with arbitrary escape and to-escape
 * characters</li>
 * <li>prepare strings for being used in SQL queries</li>
 * <li>produce IO Provider objects</li>
 * <li>send e-mails via SMTP (this is why this class depends on mail.jar to be
 * present on the class path)</li>
 * </ul>
 * 
 * @author Guido Sautter
 */
public class EasyIO {
	
	/**
	 * Factory for IO provider implementations.
	 * @author sautter
	 */
	public static interface IoProviderFactory {
		
		/**
		 * Create an IO Provider configured based upon the specified settings.
		 * @param configuration the Properties object containing the settings
		 * @return an IO Provider configured based upon the argument settings
		 */
		public abstract IoProvider getIoProvider(Settings configuration);
		
		/**
		 * Create an IO Provider configured based upon the specified settings.
		 * If the argument requester is not null, its class loader will be used
		 * to load any JDBC drivers or other required classes, rather than the
		 * system class loader. This is helpful in situations where classes
		 * loaded through subordinate class loaders need to contribute JARs the
		 * system class loader does not have access to.
		 * @param configuration the Properties object containing the settings
		 * @param requester the object requesting the IO provider
		 * @return an IO Provider configured based upon the argument settings
		 */
		public abstract IoProvider getIoProvider(Settings configuration, Object requester);
		
		/**
		 * Create an IO Provider configured based upon the specified settings.
		 * If the argument class loader is not null, it will be used to load
		 * any JDBC drivers or other required classes, rather than the system
		 * class loader. This is helpful in situations where classes loaded
		 * through subordinate class loaders need to contribute JARs the system
		 * class loader does not have access to.
		 * @param configuration the Properties object containing the settings
		 * @return an IO Provider configured based upon the argument settings
		 */
		public abstract IoProvider getIoProvider(Settings configuration, ClassLoader classLoader);
	}
	
	/**
	 * Add an IO provider factory to provide a custom implementation of the
	 * <code>IoProvider</code> interface.
	 * @param ipf the IO provider factory to use
	 */
	public static synchronized void setIoProviderFactory(IoProviderFactory ipf) {
		ioProviderFactory = ipf;
	}
	private static IoProviderFactory ioProviderFactory = null;
	
	/**
	 * Create an IO Provider configured according to the specified settings. The
	 * implementation of the IO Provider interface uses the following settings:
	 * <ul>
	 * <li><b>Web IO:</b>
	 * <ul>
	 * <li><b>WWW.UseProxy</b>: use a proxy server for accessing the web?
	 * Specify 'YES' to indicate using a proxy server.</li>
	 * <li><b>WWW.Proxy</b>: the address of the proxy server to use.</li>
	 * <li><b>WWW.ProxyPort</b>: the port to access the proxy server through.</li>
	 * <li><b>WWW.UseProxyAuth</b>: authenticate with the proxy server? Specify
	 * 'YES' to indicate so.</li>
	 * <li><b>WWW.ProxyUser</b>: the user name to use for authenticationg with
	 * the proxy server.</li>
	 * <li><b>WWW.ProxyPassword</b>: the password to use for authenticationg
	 * with the proxy server.</li>
	 * </ul>
	 * </li>
	 * <li><b>Sending e-mail via SMTP:</b>
	 * <ul>
	 * <li><b>SMTP.Server</b>: the name of the SMTP server to use.</li>
	 * <li><b>SMTP.Port</b>: the port to access the SMTP server through.</li>
	 * <li><b>SMTP.Login</b>: the login name for the SMTP server.</li>
	 * <li><b>SMTP.Password</b>: the password for the SMTP server.</li>
	 * <li><b>SMTP.FromAddress</b>: the sender address for e-mails.</li>
	 * </ul>
	 * </li>
	 * <li><b>Database access via JDBC:</b>
	 * <ul>
	 * <li><b>JDBC.DriverClassName</b>: the class name of the JDBC driver to
	 * use.</li>
	 * <li><b>JDBC.DriverClassPath</b>: the path(s) and name(s) of the jar
	 * file(s) to load the JDBC driver from.</li>
	 * <li><b>JDBC.Driver</b>: the name of the JDBC driver to use, usually
	 * something like 'jdbc:...'</li>
	 * <li><b>JDBC.Host</b>: the name of the database server (in a machine
	 * sense)</li>
	 * <li><b>JDBC.Port</b>: the port to access the database server through</li>
	 * <li><b>JDBC.AuthMode</b>: how to authenticate with the database server?
	 * Specify 'HUP' to indicate using host-user-password authentication,
	 * otherwise a connection URL will be used.</li>
	 * <li><b>JDBC.User</b>: the user name to authenticate with the database
	 * server.</li>
	 * <li><b>JDBC.Password</b>: the password to authenticate with the database
	 * server.</li>
	 * <li><b>JDBC.DefaultDB</b>: the database to use initially. Can be changed
	 * via the SQL query 'USE &lt;DatabaseName&gt;'. The datbase specified here
	 * has to exist prior to the application to start. This is to facilitate
	 * running applications with authentication data that does not allow for
	 * creating new databases.</li>
	 * <li><b>JDBC.Url</b>: the URL to use for connecting to and authenticating
	 * with the database server. This is an alternative for host-user-password
	 * authentication and specifying a default DB.</li></li>
	 * <li><b>JDBC.TerminalSemicolon</b>: do SQL queries require a terminal
	 * semicolon for the database server to connect to? This setting is to
	 * simplify running the same application on multiple different database
	 * servers, which may differ in requiring, accepting, or denying SQL queries
	 * to be terminated with a semicolon. Depending on this setting, the IO
	 * provider makes sure every SQL query end with or does not end with a
	 * semicolon, respectively. The default is to ensure an ending semicolon, as
	 * this is most common. Specify 'NO' for this parameter to enforce SQL
	 * queries to not end with a semicolon.</li>
	 * </li>
	 * </ul>
	 * @param configuration the Properties object containing the settings
	 * @return an IO Provider configured according to the specified settings
	 */
	public static IoProvider getIoProvider(Settings configuration) {
		if (ioProviderFactory == null)
			return getIoProvider(configuration, null);
		else return ioProviderFactory.getIoProvider(configuration);
	}
	
	/**
	 * Create an IO Provider configured based upon the specified settings.
	 * If the argument requester is not null, its class loader will be used
	 * to load any JDBC drivers or other required classes, rather than the
	 * system class loader. This is helpful in situations where classes
	 * loaded through subordinate class loaders need to contribute JARs the
	 * system class loader does not have access to.
	 * @param configuration the Properties object containing the settings
	 * @param requester the object requesting the IO provider
	 * @return an IO Provider configured based upon the argument settings
	 */
	public static IoProvider getIoProvider(Settings configuration, Object requester) {
		if (ioProviderFactory == null)
			return getIoProvider(configuration, ((requester == null) ? null : requester.getClass().getClassLoader()));
		else return ioProviderFactory.getIoProvider(configuration, requester);
	}
	
	/**
	 * Create an IO Provider configured based upon the specified settings.
	 * If the argument class loader is not null, it will be used to load
	 * any JDBC drivers or other required classes, rather than the system
	 * class loader. This is helpful in situations where classes loaded
	 * through subordinate class loaders need to contribute JARs the system
	 * class loader does not have access to.
	 * @param configuration the Properties object containing the settings
	 * @return an IO Provider configured based upon the argument settings
	 */
	public static IoProvider getIoProvider(Settings configuration, ClassLoader classLoader) {
		if (ioProviderFactory == null)
			return ((configuration == null) ? null : new StandardIoProvider(configuration, classLoader));
		else return ioProviderFactory.getIoProvider(configuration, classLoader);
	}
	
	private static class StandardIoProvider implements IoProvider {
		private Settings configuration;
		private ClassLoader classLoader;
		private boolean closed = false;
		
		private boolean wwwValid = false;
		private boolean smtpValid = false;
		private boolean jdbcValid = false;
		
		//	settings for www io
		private boolean wwwProxyNeeded;
		private String wwwProxy;
		private String wwwProxyPort;
		private boolean wwwProxyAuthNeeded;
		private String wwwProxyUser;
		private String wwwProxyPassword;
		
		//	settings for SMTP IO
		private String smtpServer;
		private int smtpPort;
		private String smtpLogin;
		private String smtpPassword;
		private String smtpFromAddress;
		
		//	settings for JDBC IO
		private String jdbcDriverClassName;
		private String jdbcDriverClassPath = "";
		private String jdbcDriver;
		private String jdbcHost;
		private String jdbcPort;
		private boolean jdbcUseHostUserPassword;
		private String jdbcUser;
		private String jdbcPassword;
		private String jdbcDefaultDbSetting;
		private String jdbcUrl;
		private boolean jdbcTerminalSemicolon = true;
		private boolean jdbcKeyConstraints = true;
		
		//	time JDBC actions?
		private boolean jdbcLogTime = false;
		
		//	local objects for instant use
		private Connection jdbcCon = null;
		private int jdbcMaxReconnectAttempts = 10; // should be sensible default 
		
		//	jdbc syntax patterns
		private Properties jdbcSyntax = null;
		
		StandardIoProvider(Settings configuration, ClassLoader classLoader) {
			this.configuration = configuration;
			this.classLoader = classLoader;
			
			//	initialize WWW IO proxying
			this.wwwProxyNeeded = "YES".equalsIgnoreCase(this.configuration.getSetting("WWW.UseProxy"));
			this.wwwProxy = this.configuration.getSetting("WWW.Proxy");
			this.wwwProxyPort = this.configuration.getSetting("WWW.ProxyPort");
			this.wwwProxyAuthNeeded = "YES".equalsIgnoreCase(this.configuration.getSetting("WWW.UseProxyAuth"));
			this.wwwProxyUser = this.configuration.getSetting("WWW.ProxyUser");
			this.wwwProxyPassword = this.configuration.getSetting("WWW.ProxyPassword");
			
			//	check if WWW access is valid
			this.wwwValid = (!this.wwwProxyNeeded || (
								(this.wwwProxy != null)
								&&
								(this.wwwProxyPort != null)
								&& (
									!this.wwwProxyAuthNeeded
									|| (
										(this.wwwProxyUser != null)
										&&
										(this.wwwProxyPassword != null)
									)
								)
							));
			
			//	set proxy if needed
			if (this.wwwValid && this.wwwProxyNeeded) {
				System.getProperties().put("proxySet", "true");
				System.getProperties().put("proxyHost", this.wwwProxy);
				System.getProperties().put("proxyPort", this.wwwProxyPort);
				if (this.wwwProxyAuthNeeded) {
					//	sorry, proxy auth is not supported at the moment
				}
			}
			
			//	initialize SMTP io
			this.smtpServer = this.configuration.getSetting("SMTP.Server");
			this.smtpPort = Integer.parseInt(this.configuration.getSetting("SMTP.Port", "-1"));
			this.smtpLogin = this.configuration.getSetting("SMTP.Login");
			this.smtpPassword = this.configuration.getSetting("SMTP.Password");
			this.smtpFromAddress = this.configuration.getSetting("SMTP.FromAddress");
			
			//	check if SMTP is valid
//			this.smtpValid = ((this.smtpServer != null) &&
//							 (this.smtpPort != -1l) &&
//							 (this.smtpLogin != null) &&
//							 (this.smtpPassword != null) &&
//							 (this.smtpFromAddress != null) &&
//							 (this.smtpServerIsLocal || this.wwwValid));
			this.smtpValid = ((this.smtpServer != null) &&
					 (this.smtpPort != -1) &&
					 (this.smtpFromAddress != null));
			
			//	initialize JDBC IO
			this.jdbcDriverClassName = this.configuration.getSetting("JDBC.DriverClassName");
			this.jdbcDriverClassPath = this.configuration.getSetting("JDBC.DriverClassPath", "");
			this.jdbcDriver = this.configuration.getSetting("JDBC.Driver");
			this.jdbcHost = this.configuration.getSetting("JDBC.Host");
			this.jdbcPort = this.configuration.getSetting("JDBC.Port");
			this.jdbcUseHostUserPassword = "HUP".equalsIgnoreCase(this.configuration.getSetting("JDBC.AuthMode"));
			this.jdbcUser = this.configuration.getSetting("JDBC.User");
			this.jdbcPassword = this.configuration.getSetting("JDBC.Password");
			this.jdbcDefaultDbSetting = this.configuration.getSetting("JDBC.DefaultDB");
			this.jdbcUrl = this.configuration.getSetting("JDBC.Url");
			this.jdbcTerminalSemicolon = "YES".equalsIgnoreCase(this.configuration.getSetting("JDBC.TerminalSemicolon", "YES"));
			this.jdbcKeyConstraints = "YES".equalsIgnoreCase(this.configuration.getSetting("JDBC.KeyConstraints", "YES"));
			this.jdbcLogTime = "YES".equalsIgnoreCase(this.configuration.getSetting("JDBC.LogTime", "NO"));
			this.jdbcCon = this.getJdbcConnection(0);
			
			//	check if jdbc is valid
			this.jdbcValid = (this.jdbcCon != null);
			
			//	load product specific database features and re-connect attempt maximum
			if (this.jdbcValid) {
				this.jdbcSyntax = SyntaxHelper.loadJdbcSyntax(this.jdbcDriverClassName);
				String mra = this.configuration.getSetting("JDBC.MaxReconnectAttempts");
				if (mra != null) try {
					this.jdbcMaxReconnectAttempts = Integer.parseInt(mra);
				} catch (NumberFormatException nfe) {}
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#close()
		 */
		public void close() {
			this.closed = true;
			if (this.jdbcCon != null) try {
				this.jdbcCon.close();
				this.jdbcCon = null;
			}
			catch (SQLException sqle) {
				System.out.println("StandardIoProvider: " + sqle.getMessage() + " while closing database connection.");
			}
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#finalize()
		 */
		protected void finalize() throws Throwable {
			this.close();
		}
		
		//	create a JDBC connection according to the settings
		private Connection getJdbcConnection(int attempt) {
			if (this.closed)
				throw new IllegalStateException("StandardIoProvider is closed");
			
			try {
				
				//	make sure JDBC driver loaded and registered
				ensureJdbcDriverLoaded(this.jdbcDriverClassName, this.jdbcDriverClassPath, this.classLoader);
				
				//	using user/password authentication
				if (this.jdbcUseHostUserPassword) {
					String url;
					
					//	connection url not given --> assemble it
					if (this.jdbcUrl == null) {
						url = this.jdbcDriver + ":" + this.jdbcHost;
						if (this.jdbcPort != null)
							url = url + ":" + this.jdbcPort;
						if (this.jdbcDefaultDbSetting != null)
							url = url + this.jdbcDefaultDbSetting;
					}
					
					//	connection url given --> simply use it
					else url = this.jdbcUrl;
					
					//	get connection and return it
					return DriverManager.getConnection(url, this.jdbcUser, this.jdbcPassword);
				}
				
				//	using URL authentication
				else {
					String url;
					
					//	connection url not given --> assemble it
					if (this.jdbcUrl == null) {
						url = this.jdbcDriver + ":" + this.jdbcHost;
						if (this.jdbcPort != null)
							url = url + ":" + this.jdbcPort;
						if (this.jdbcDefaultDbSetting != null)
							url = url + this.jdbcDefaultDbSetting;
						url = url + "?" + this.jdbcUser;
						if (this.jdbcPassword != null)
							url = url + "&" + this.jdbcPassword;
					}
					
					//	connection url given --> simply use it
					else url = this.jdbcUrl;
					
					//	get connection and return it
					return DriverManager.getConnection(url);
				}
			}
//			
//			//	catch exception caused by invalid settings
//			catch (SQLException sql) {
//				System.out.println("StandardIoProvider: " + sql.getClass().getName() + " (" + sql.getMessage() + ") while creating JDBC connection.");
//				sql.printStackTrace(System.out);
//				return null;
//			}
//			
//			//	catch exception caused by missing settings
//			catch (NullPointerException npe) {
//				System.out.println("StandardIoProvider: " + npe.getClass().getName() + " (" + npe.getMessage() + ") while creating JDBC connection.");
//				npe.printStackTrace(System.out);
//				return null;
//			}
//			
//			//	catch other possible exceptions
//			catch (Exception e) {
//				System.out.println("StandardIoProvider: " + e.getClass().getName() + " (" + e.getMessage() + ") while creating JDBC connection.");
//				e.printStackTrace(System.out);
///				return null;
//			}
			
			//	catch other possible exceptions
			catch (Exception e) {
				System.out.println("StandardIoProvider: " + e.getClass().getName() + " (" + e.getMessage() + ") while creating JDBC connection.");
				e.printStackTrace(System.out);
				if (attempt < 1) // call from constructor
					return null;
				if (this.closed) // call from constructor, or shutting down
					throw new IllegalStateException("StandardIoProvider is closed");
				else if (this.jdbcMaxReconnectAttempts <= attempt)
					throw new IllegalStateException("Failed to reconnect to database in " + attempt + " attempts, giving up for now");
				else {
					try {
						Thread.sleep(1000 * attempt);
					} catch (InterruptedException ie) {}
					return this.getJdbcConnection(attempt + 1);
				}
			}
		}
		
		private static HashSet loadedJdbcDriverClasses = new HashSet(4);
		private static synchronized void ensureJdbcDriverLoaded(String className, String classPath, ClassLoader classLoader) throws Exception {
			classPath = classPath.trim();
			String classKey = (className + "@" + classPath);
			if (loadedJdbcDriverClasses.contains(classKey)) {
				System.out.println("StandardIoProvider: JDBC driver " + classKey + " loaded before");
				return;
			}
			
			//	parse class path
			String[] classPathParts = classPath.split("\\s+");
			ArrayList urlList = new ArrayList();
			for (int p = 0; p < classPathParts.length; p++) try {
				System.out.println("StandardIoProvider: adding '" + classPathParts[p] + "' to JDBC driver class path");
				if (classPathParts[p].indexOf("://") == -1)
					urlList.add(new File(classPathParts[p]).toURL());
				else urlList.add(new URL(classPathParts[p]));
			}
			catch (IOException ioe) {
				System.out.println("StandardIoProvider: error adding '" + classPathParts[p] + "' to JDBC driver class path - " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
			
			//	create class loader
			ClassLoader driverClassLoader = ((classLoader == null) ? StandardIoProvider.class.getClassLoader() : classLoader);
			if (urlList.size() != 0)
				driverClassLoader = new URLClassLoader(((URL[]) urlList.toArray(new URL[urlList.size()])), driverClassLoader);
			
			//	load JDBC driver
			Class driverClass = driverClassLoader.loadClass(className);
			System.out.println("StandardIoProvider: got JDBC driver class '" + className + "'");
			
			//	wrap and register driver instance
			DriverManager.registerDriver(new GenericDriver((Driver) driverClass.newInstance()));
			System.out.println("StandardIoProvider: JDBC driver instance wrapped");
			
			//	remember loading this one
			loadedJdbcDriverClasses.add(classKey);
		}
		
		private void reGetJdbcConnection() {
			Connection oldJdbcCon = this.jdbcCon; // need to hold on to this so we recognize whether or not some other thread beat us to re-connecting
			synchronized (this) {
				if (this.jdbcCon == oldJdbcCon)
					this.jdbcCon = this.getJdbcConnection(1); // try and dynamically reconnect after database restart
			}
		}
		
		/**
		 * Wrapper for JDBC drivers that are loaded at runtime (DriverManager
		 * requires drivers to be loaded through system class loader for some
		 * reason)
		 * 
		 * @author sautter
		 */
		private static class GenericDriver implements Driver {
			private Driver driver;
			private GenericDriver(Driver driver) {
				this.driver = driver;
			}
			public boolean acceptsURL(String url) throws SQLException {
				return this.driver.acceptsURL(url);
			}
			public Connection connect(String url, Properties properties) throws SQLException {
				return this.driver.connect(url, properties);
			}
			public int getMajorVersion() {
				return this.driver.getMajorVersion();
			}
			public int getMinorVersion() {
				return this.driver.getMinorVersion();
			}
			public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) throws SQLException {
				return this.driver.getPropertyInfo(url, properties);
			}
			public boolean jdbcCompliant() {
				return this.driver.jdbcCompliant();
			}
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				throw new SQLFeatureNotSupportedException();
//				return this.driver.getParentLogger();
			}
		}
		
		/**	check if a table with the specified name exists
		 * @param	tableName	the name of the table that's existence is to be checked
		 * @return	true if and only if a table with the specified name exists
		 */
		public boolean ensureTable(String tableName) {
			if (this.jdbcValid) {
				try {
					this.executeSelectQuery("SELECT * FROM " + tableName + " WHERE 1=0");
					return true;
				}
				catch (SQLException sqle) {
					return false;
				}
			}
			else return false;
		}
		
		/**	check if a table with the specified name exists and complies the specified definition
		 * @param	definition	the definition of the table to be validated (@see de.easyIO.TableDefinition for details)
		 * @param 	create		if set to true, the questioned table will be created or adjusted to the specified definition (extensions only)
		 * @return	true if and only if a table with the specified name and definition exists or was created or adjusted successfully
		 */
		public boolean ensureTable(String definition, boolean create) {
			return this.ensureTable((new TableDefinition(definition)), create);
		}
		
		/**	check if a table with the specified name exists and complies the specified definition
		 * @param	definition	the definition of the table to be validated
		 * @param 	create		if set to true, the questioned table will be created or adjusted to the specified definition (extensions only)
		 * @return	true if and only if a table with the specified name and definition exists or was created or adjusted successfully
		 */
		public boolean ensureTable(TableDefinition definition, boolean create) {
			if (!this.jdbcValid || (definition == null)) {
				System.out.println("StandardIoProvider: JDBC invalid or TableDefinition was null.");
				return false;
			}
			
			/* TODO create syntax resource files (as in StringUtils for postscript):
			 * - one for each supported database
			 * - content:
			 *   - mapping of generic data types to database specific ones
			 * ==> all data types become available
			 */
			
			try {
				
				//	get old definition from DB
				String columnValidationQuery = definition.getColumnValidationQuery();
				if (columnValidationQuery == null) {
					System.out.println("StandardIoProvider: empty table definition.");
					return false;
				}
				System.out.println("StandardIoProvider: ensuring table columns in " + definition.getTableName() + "\n  " + columnValidationQuery);
				SqlQueryResult sqr = this.executeSelectQuery(columnValidationQuery, true);
				System.out.println("StandardIoProvider: column validation query successful.");
				TableDefinition existingDefinition = new TableDefinition(sqr, this.jdbcSyntax, definition.getTableName());
				
				//	we got all we need
				if (existingDefinition.equals(definition))
					return true;
				
				//	updates not allowed
				if (!create)
					return false;
				
				//	get and execute update queries
				try {
					String[] updates = definition.getUpdateQueries(existingDefinition, this.jdbcSyntax);
					for (int u = 0; u < updates.length; u++) {
						System.out.println("StandardIoProvider: altering column\n  " + updates[u]);
						this.executeUpdateQuery(updates[u]);
					}
					return true;
				}
				catch (SQLException updateSqlEx) {
					System.out.println("StandardIoProvider: " + updateSqlEx.getMessage() + " while updating table.");
					return false;
				}
			}
			
			//	at least one column missing
			catch (SQLException columnSqlEx) {
				System.out.println("StandardIoProvider: caught " + columnSqlEx.getMessage() + " while ensuring table, some column is missing.");
				try {
					
					//	get old definition from DB
					String validationQuery = definition.getValidationQuery();
					System.out.println("StandardIoProvider: ensuring table " + definition.getTableName() + "\n  " + validationQuery);
					SqlQueryResult sqr = this.executeSelectQuery(validationQuery, true);
					System.out.println("StandardIoProvider: validation query successful.");
					TableDefinition existingDefinition = new TableDefinition(sqr, this.jdbcSyntax, definition.getTableName());
					
					//	we got all we need
					if (existingDefinition.equals(definition))
						return true;
					
					//	updates not allowed
					if (!create)
						return false;
					
					//	get and execute update queries
					String updateQuery = "";
					try {
						String[] updates = definition.getUpdateQueries(existingDefinition, this.jdbcSyntax);
						for (int u = 0; u < updates.length; u++) {
							updateQuery = updates[u];
							System.out.println("StandardIoProvider: creating or altering column\n  " + updates[u]);
							this.executeUpdateQuery(updates[u]);
						}
						return true;
					}
					catch (SQLException updateSqlEx) {
						System.out.println("StandardIoProvider: " + updateSqlEx.getMessage() + " while extending / updating table.\n  Query was " + updateQuery);
						return false;
					}
				}
				
				//	table doesn't exist at all
				catch (SQLException tableSqlEx) {
					System.out.println("StandardIoProvider: caught " + tableSqlEx.getMessage() + " while ensuring table, table doesn't exist.");
					String creationQuery = "";
					
					//	create table if allowed
					try {
						if (create) {
							creationQuery = definition.getCreationQuery(this.jdbcSyntax);
							System.out.println("StandardIoProvider: creating table\n  " + creationQuery);
							this.executeUpdateQuery(creationQuery);
							return true;
						}
						else return false;
					}
					
					//	exception while creating table
					catch (SQLException createSqlEx) {
						System.out.println("StandardIoProvider: " + createSqlEx.getMessage() + " while creating table.\n  Query was " + creationQuery);
						return false;
					}
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#setPrimaryKey(java.lang.String, java.lang.String)
		 */
		public boolean setPrimaryKey(String table, String column) {
			if (!this.jdbcKeyConstraints)
				return false;
			
			//	check if table and column exist
			String checkQuery = ("SELECT " + column + " FROM " + table + " WHERE 1=0;");
			try {
				System.out.println("StandardIoProvider: checking table for primary key constraint creation.\n  " + checkQuery.toString());
				this.executeSelectQuery(checkQuery.toString());
			}
			
			//	exception while checking table
			catch (SQLException checkSqlEx) {
				System.out.println("StandardIoProvider: " + checkSqlEx.getMessage() + " while checking table for primary key constraint creation.\n  Query was " + checkQuery);
				return false;
			}
			
			//	create primary key constraint
			String constraintName = (table + "_PK_" + column);
			String createQuery = this.jdbcSyntax.getProperty(TableDefinition.SYNTAX_CREATE_PRIMARY_KEY);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_KEY_CONSTRAINT_NAME_VARIABLE, constraintName);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_KEYED_TABLE_VARIABLE, table);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_KEYED_COLUMN_VARIABLE, column);
			try {
				System.out.println("StandardIoProvider: creating primary key constraint\n  " + createQuery);
				this.executeUpdateQuery(createQuery);
				return true;
			}
			
			//	exception while creating index
			catch (SQLException createSqlEx) {
				
				//	catch case of index already existing, and return true on respective exceptions
				if (this.isAlreadyExistsErrorMessage(createSqlEx.getMessage(), "constraint", constraintName))
					return true;
				
				//	other error
				System.out.println("StandardIoProvider: " + createSqlEx.getMessage() + " while creating primary key constraint.\n  Query was " + createQuery);
				return false;
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#setForeignKey(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		public boolean setForeignKey(String table, String column, String refTable, String refColumn) {
			if (!this.jdbcKeyConstraints)
				return false;
			
			//	check if table and column exist
			String checkQuery = ("SELECT " + column + " FROM " + table + " WHERE 1=0;");
			try {
				System.out.println("StandardIoProvider: checking table for constraint creation.\n  " + checkQuery.toString());
				this.executeSelectQuery(checkQuery.toString());
			}
			
			//	exception while checking table
			catch (SQLException checkSqlEx) {
				System.out.println("StandardIoProvider: " + checkSqlEx.getMessage() + " while checking table for primary key constraint creation.\n  Query was " + checkQuery);
				return false;
			}
			
			//	check if referenced table and column exist
			String refCheckQuery = ("SELECT " + refColumn + " FROM " + refTable + " WHERE 1=0;");
			try {
				System.out.println("StandardIoProvider: checking referenced table for foreign key constraint creation.\n  " + refCheckQuery.toString());
				this.executeSelectQuery(refCheckQuery.toString());
			}
			
			//	exception while checking table
			catch (SQLException checkSqlEx) {
				System.out.println("StandardIoProvider: " + checkSqlEx.getMessage() + " while checking referenced table for foreign key constraint creation.\n  Query was " + refCheckQuery);
				return false;
			}
			
			//	create foreign key constraint
			String constraintName = (table + "_" + column + "_FK_" + refTable + "_" + refColumn);
			String createQuery = this.jdbcSyntax.getProperty(TableDefinition.SYNTAX_CREATE_FOREIGN_KEY);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_KEY_CONSTRAINT_NAME_VARIABLE, constraintName);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_KEYED_TABLE_VARIABLE, table);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_KEYED_COLUMN_VARIABLE, column);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_REFERENCED_TABLE_VARIABLE, refTable);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_REFERENCED_COLUMN_VARIABLE, refColumn);
			try {
				System.out.println("StandardIoProvider: creating foreign key constraint\n  " + createQuery);
				this.executeUpdateQuery(createQuery);
				return true;
			}
			
			//	exception while creating index
			catch (SQLException createSqlEx) {
				
				//	catch case of index already existing, and return true on respective exceptions
				if (this.isAlreadyExistsErrorMessage(createSqlEx.getMessage(), "constraint", constraintName))
					return true;
				
				//	other error
				System.out.println("StandardIoProvider: " + createSqlEx.getMessage() + " while creating foreign key constraint.\n  Query was " + createQuery);
				return false;
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#indexColumn(java.lang.String, java.lang.String)
		 */
		public boolean indexColumn(String table, String column) {
			String[] columns = {column};
			return this.indexColumns(table, columns);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#indexColumns(java.lang.String, java.lang.String[])
		 */
		public boolean indexColumns(String table, String[] columns) {
			
			//	check arguments
			if ((table == null) || (table.trim().length() == 0) || (columns == null) || (columns.length == 0))
				return false;
			
			//	check if columns valid
			LinkedHashSet cols = new LinkedHashSet(columns.length);
			for (int c = 0; c < columns.length; c++) {
				if ((columns[c] != null) && (columns[c].trim().length() != 0))
					cols.add(columns[c].trim());
			}
			columns = ((String[]) cols.toArray(new String[cols.size()]));
			
			//	check if table and columns exist
			StringBuffer checkQuery = new StringBuffer("SELECT ");
			for (int c = 0; c < columns.length; c++) {
				if (c != 0)
					checkQuery.append(", ");
				checkQuery.append(columns[c]);
			}
			checkQuery.append(" FROM " + table + " WHERE 1=0;");
			try {
				System.out.println("StandardIoProvider: checking table for index creation.\n  " + checkQuery.toString());
				this.executeSelectQuery(checkQuery.toString());
			}
			
			//	exception while checking table
			catch (SQLException checkSqlEx) {
				System.out.println("StandardIoProvider: " + checkSqlEx.getMessage() + " while checking table for index creation.\n  Query was " + checkQuery);
				return false;
			}
			
			//	create index
			StringBuffer indexName = new StringBuffer(table + "_index");
			StringBuffer indexColumns = new StringBuffer();
			for (int c = 0; c < columns.length; c++) {
				indexName.append("_" + columns[c]);
				if (c != 0)
					indexColumns.append(", ");
				indexColumns.append(columns[c]);
			}
			String createQuery = this.jdbcSyntax.getProperty(TableDefinition.SYNTAX_CREATE_INDEX);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_INDEX_NAME_VARIABLE, indexName.toString());
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_INDEXED_TABLE_VARIABLE, table);
			createQuery = SyntaxHelper.replaceVariable(createQuery, TableDefinition.SYNTAX_INDEXED_COLUMNS_VARIABLE, indexColumns.toString());
			try {
				System.out.println("StandardIoProvider: creating index\n  " + createQuery);
				this.executeUpdateQuery(createQuery);
				return true;
			}
			
			//	exception while creating index
			catch (SQLException createSqlEx) {
				
				//	catch case of index already existing, and return true on respective exceptions
				if (this.isAlreadyExistsErrorMessage(createSqlEx.getMessage(), "index", indexName.toString()))
					return true;
				
				//	other error
				System.out.println("StandardIoProvider: " + createSqlEx.getMessage() + " while creating index.\n  Query was " + createQuery);
				return false;
			}
		}
		
		private boolean isAlreadyExistsErrorMessage(String cseMessage, String constructType, String constructName) {
			//	TODO figure out how to make this more generic, or base it on some pattern in syntax file
			
			if (cseMessage == null)
				return false;
			cseMessage = cseMessage.toLowerCase();
			if ((cseMessage.indexOf(constructName.toLowerCase()) == -1) && (cseMessage.indexOf(constructType) == -1))
				return false;
			
			if ((cseMessage.indexOf("already") != -1) && (cseMessage.indexOf("exists") != -1))
				return true;
			if ((cseMessage.indexOf("bereits") != -1) && (cseMessage.indexOf("vorhanden") != -1))
				return true;
			if ((cseMessage.indexOf("existiert") != -1) && (cseMessage.indexOf("bereits") != -1))
				return true;
			
			return false;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#getSelectResultSizeLimit(int, int)
		 */
		public ResultSizeLimitationClause getSelectResultSizeLimit(int offset, int limit) {
			return ResultSizeLimitationClause.createResultSizeLimitationClause(offset, limit, this.jdbcSyntax);
		}
		
		/**	execute a select query
		 * @param	query	the query to be executed	
		 * @param	copy	copy the result of the query to the result object
		 * 					(faster in iteration, but memory-intensive for large results)
		 * @return	the result of query
		 * @throws SQLException
		 */
		public SqlQueryResult executeSelectQuery(String query, boolean copy) throws SQLException {
			return this.executeSelectQuery(query, copy, true);
		}
		
		private SqlQueryResult executeSelectQuery(String query, boolean copy, boolean isOriginalRequest) throws SQLException {
			if (query == null)
				return null;
			if (this.closed)
				throw new IllegalStateException("StandardIoProvider is closed");
			if (!this.jdbcValid)
				throw new SQLException("StandardIoProvider: JDBC connection unavailable, check configuration");
			if (!"SELECT".equalsIgnoreCase(query.substring(0, "SELECT".length())))
				throw new SQLException("StandardIoProvider: not a SELECT query");
			try {
				long time = (this.jdbcLogTime ? System.currentTimeMillis() : 0);
				Statement st = null;
				SqlQueryResult sqr = null;
				try {
					st = (copy ? this.jdbcCon.createStatement() : this.jdbcCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));
					if (this.jdbcLogTime)
						System.out.println("StandardIoProvider: got statement after " + (System.currentTimeMillis() - time) + "ms");
					sqr = new SqlQueryResult(query, this.jdbcLogTime, st.executeQuery(this.prepareQuery(query)), copy);
					if (this.jdbcLogTime)
						System.out.println("StandardIoProvider: statement wrapped" + (copy ? " and copied" : "") + " after " + (System.currentTimeMillis() - time) + "ms");
					return sqr;
				}
				finally {
					if ((copy || (sqr == null)) && (st != null))
						st.close();
					if (this.jdbcLogTime)
						System.out.println("StandardIoProvider: done after " + (System.currentTimeMillis() - time) + "ms");
				}
			}
			catch (SQLException sqle) {
				if (isOriginalRequest && this.isConnectionClosedErrorMessage(sqle.getMessage())) {
					System.out.println("StandardIoProvider: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while ececuting query, attempting re-connect ...");
					this.reGetJdbcConnection(); // try and dynamically reconnect after database restart
					System.out.println(" ==> database connection re-established successfully");
					return this.executeSelectQuery(query, copy, false); // try again to process query (no use trying to reconnect again, though)
				}
				else throw sqle;
			}
		}
		
		/**	execute a select query over the default JDBC connection
		 * @param	query	the query to be executed
		 * @return	the result of query
		 * @throws SQLException
		 */
		public SqlQueryResult executeSelectQuery(String query) throws SQLException {
			return this.executeSelectQuery(query, false);
		}
		
		/**	execute an update query over the default JDBC connection
		 * @param	query	the query to be executed	
		 * @return	the return state of the query execution
		 * @throws SQLException
		 */
		public int executeUpdateQuery(String query) throws SQLException {
			return this.executeUpdateQuery(query, true);
		}
		
		private int executeUpdateQuery(String query, boolean isOriginalRequest) throws SQLException {
			if (query == null)
				return 0;
			if (this.closed)
				throw new IllegalStateException("StandardIoProvider is closed");
			if (!this.jdbcValid)
				throw new SQLException("StandardIoProvider: JDBC connection unavailable, check configuration");
			try {
				Statement st = null;
				try {
					st = this.jdbcCon.createStatement();
					return st.executeUpdate(this.prepareQuery(query));
				}
				finally {
					if (st != null)
						st.close();
				}
			}
			catch (SQLException sqle) {
				if (isOriginalRequest && this.isConnectionClosedErrorMessage(sqle.getMessage())) {
					System.out.println("StandardIoProvider: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while ececuting query, attempting re-connect ...");
					this.reGetJdbcConnection(); // try and dynamically reconnect after database restart
					System.out.println(" ==> database connection re-established successfully");
					return this.executeUpdateQuery(query, false); // try again to process query (no use trying to reconnect again, though)
				}
				else throw sqle;
			}
		}
		
		private final String prepareQuery(String query) {
			if (query.endsWith(";"))
				return (this.jdbcTerminalSemicolon ? query : query.substring(0, (query.length() - ";".length())));
			else return (this.jdbcTerminalSemicolon ? (query + ";") : query);
		}
		
		private boolean isConnectionClosedErrorMessage(String cseMessage) {
			//	TODO figure out how to make this more generic, or base it on some pattern in syntax file
			if (cseMessage == null)
				return false;
			cseMessage = cseMessage.toLowerCase();
			
			if ((cseMessage.indexOf("connection") != -1) && (cseMessage.indexOf("closed") != -1))
				return true;
			if ((cseMessage.indexOf("connection") != -1) && (cseMessage.indexOf("closing") != -1))
				return true;
			if ((cseMessage.indexOf("connection") != -1) && (cseMessage.indexOf("terminated") != -1))
				return true;
			if ((cseMessage.indexOf("connection") != -1) && (cseMessage.indexOf("terminating") != -1))
				return true;
			if ((cseMessage.indexOf("verbindung") != -1) && (cseMessage.indexOf("geschlossen") != -1))
				return true;
			if ((cseMessage.indexOf("verbindung") != -1) && (cseMessage.indexOf("beendet") != -1))
				return true;
			if ((cseMessage.indexOf("verbindung") != -1) && (cseMessage.indexOf("abgebrochen") != -1))
				return true;
			
			return false;
		}
		
		/** @see de.uka.ipd.idaho.easyIO.IoProvider#isJdbcAvailable()
		 */
		public boolean isJdbcAvailable() {
			return this.jdbcValid;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.easyIO.IoProvider#getJdbcMetaData()
		 */
		public Properties getJdbcMetadata() {
			if (!this.isJdbcAvailable())
				return null;
			Properties jmd = new Properties();
			try {
				DatabaseMetaData dmd = this.jdbcCon.getMetaData();
				jmd.setProperty("Database Server", (dmd.getDatabaseProductName() + " " + dmd.getDatabaseProductVersion()));
				jmd.setProperty("JDBC Driver", (dmd.getDriverName() + " " + dmd.getDriverVersion()));
			}
			catch (SQLException sqle) {
				jmd.setProperty("Metadata Error", sqle.getMessage());
			}
			return jmd;
		}
		
		/**	send msg as an eMail with subject sbj to toAddress
		 */
		public void smtpSend(String subject, String message, String[] toAddresses) throws Exception {
			if (this.closed)
				throw new IllegalStateException("StandardIoProvider is closed");
			if (this.smtpValid)
				EasyIO.smtpSend(subject, message, toAddresses, this.smtpFromAddress, this.smtpServer, this.smtpPort, this.smtpLogin, this.smtpPassword);
		}

		/**	@return	true if and only if this IoProvider allows to send mails
		 */
		public boolean isMessagingAvailable() {
			return this.smtpValid;
		}
	}
	
	/**
	 * Read the content of a file and return it as a String.
	 * @param file the file to read
	 * @return the content of the specified file as a String
	 * @throws IOException, if any IOException occures
	 */
	public static String readFile(File file) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer assembler = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			assembler.append(line);
			assembler.append("\n");
		}
		br.close();
		return assembler.toString();
	}
	
	/**
	 * Append a string to the end of a file. If the specified file doesn't exist
	 * in the specified path, it will be created.
	 * @param file the file to write to
	 * @param content the String to write to the end of the specified file
	 * @return true if the writing process was successful, false otherwise
	 * @throws IOException, if any IOEXception occures
	 */
	public static boolean writeFile(File file, String content) throws IOException {
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
			buf.write(content);
			buf.newLine();
			buf.flush();
			buf.close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Send the String msg as an eMail with the subject sbj to the
	 * address toAddress.
	 * @param subject the subject of the mail to be sent
	 * @param message the text of the mail to be sent
	 * @param toAddresses an array holding the addresses to send the mail to
	 * @param fromAddress the address to be specified as the sender of the mail
	 * @param server the smtp server to send the mail
	 * @param port the port to send the mail through
	 * @param login the login to the smtp server
	 * @param password the password to the smtp server
	 * @throws MessagingException, if any MessagingException occures
	 */
	public static void smtpSend(String subject, String message, String toAddresses[], String fromAddress, String server, int port, final String login, final String password) throws MessagingException {
		if (toAddresses.length == 0)
			return;
		
		//	addemble session properties
        Properties smtpSessionProps = new Properties();
        smtpSessionProps.setProperty("mail.smtp.host", server);
        if (port != 22)
        	smtpSessionProps.setProperty("mail.smtp.port", ("" + port));
        Authenticator smtpSessionAuth = null;
        if ((login != null) && (password != null)) {
        	smtpSessionProps.setProperty("mail.smtp.auth", "true");
        	smtpSessionProps.setProperty("mail.user", login);
        	smtpSessionProps.setProperty("mail.password", password);
        	smtpSessionAuth = new Authenticator() {
    			protected PasswordAuthentication getPasswordAuthentication() {
    				return new PasswordAuthentication(login, password);
    			}
    		};
        }
        
        
    	//	get session
    	Session session = ((smtpSessionAuth == null) ? Session.getInstance(smtpSessionProps) : Session.getInstance(smtpSessionProps, smtpSessionAuth));
    	
		//	assemble message ...
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(fromAddress));
		Address to[] = new Address[toAddresses.length];
		for (int t = 0; t < to.length; t++)
			to[t] = new InternetAddress(toAddresses[t]);
		msg.setRecipients(Message.RecipientType.TO, to);
   		msg.setSubject(subject);
 		msg.setText(message);
		
		//	... and send it
		Transport transport = session.getTransport("smtp");
		transport.connect();
		transport.sendMessage(msg, to);
		transport.close();
	}
	
	/**
	 * Insert escaper Characters into data, one before every appearance of
	 * Character toEscape.
	 * @param data the String to be provides with escape Characters
	 * @return the escaped string
	 */
	public static String sqlEscape(String data) {
		return StringUtils.escapeChar(data, '\'', '\'');
	}
	
	/**
	 * Prepare a String to be used in a LIKE clause in an SQL query (replace
	 * spaces and other problematic characters by wildcards).
	 * @param string the String to be prepared
	 * @return the specified String prepared to be used in a LIKE clause in an
	 *         SQL query
	 */
	public static String prepareForLIKE(String string) {
		string = string.replaceAll("\\s+", "%");
		string = string.replaceAll("\\'", "%");
		return string;
	}
	
	/**
	 * Insert escaper Characters into data, one before every appearance of
	 * Character toEscape.
	 * @param data the String to be provides with escape Characters
	 * @param toEscape the Character to be escaped
	 * @param escaper the Character to be used for escaping
	 * @return the escaped string
	 * @deprecated use StringUtils.escapeChar() instead
	 */
	public static String escape(String data, char toEscape, char escaper) {
		return StringUtils.escapeChar(data, toEscape, escaper);
	}
	
	/**
	 * Insert escaper Characters into data, one before every appearance of each
	 * Character contained in toEscape.
	 * @param data the String to be provides with escape Characters
	 * @param toEscape the array containing the Characters to be escaped
	 * @param escaper the Character to be used for escaping
	 * @return the escaped string
	 * @deprecated use StringUtils.escapeChars() instead
	 */
	public static String escape(String data, char toEscape[], char escaper) {
		return StringUtils.escapeChars(data, toEscape, escaper);
	}
	
	/**
	 * Inspect a byte array and infer the character encoding. Specifically,
	 * this method checks for byte groupings characteristic for the Unicode
	 * encodings <code>UTF-8</code>, <code>UTF-16BE</code>, and
	 * <code>UTF-16LE</code>. If no characteristic byte groupings for either
	 * one of the three encodings are found, this method returns null.
	 * @param bytes the bytes to inspect
	 * @return the presumable encoding, provided it is either one of the three
	 *            mentioned above
	 */
	public static String inferEncoding(byte[] bytes) {
		return inferEncoding(bytes, bytes.length);
	}
	
	/**
	 * Inspect a byte array and infer the character encoding. Specifically,
	 * this method checks for byte groupings characteristic for the Unicode
	 * encodings <code>UTF-8</code>, <code>UTF-16BE</code>, and
	 * <code>UTF-16LE</code>. If no characteristic byte groupings for either
	 * one of the three encodings are found, this method returns null. The
	 * argument sample limit allows to specify how many bytes of the argument
	 * array to actually inspect, e.g. if a buffer array has not been filled
	 * completely in an IO operation.
	 * @param bytes the bytes to inspect
	 * @param limit the number of bytes to actually consider
	 * @return the presumable encoding, provided it is either one of the three
	 *            mentioned above
	 */
	public static String inferEncoding(byte[] bytes, int limit) {
		int sampleSize = bytes.length;
		if (limit > 1)
			sampleSize = Math.min(sampleSize, limit);
		
		int oddZeros = 0;
		int evenZeros = 0;
		int negativeGroups = 0;
		int negativeBytes = 0;
		for (int b = 0; b < sampleSize; b++) {
			if (bytes[b] == 0) {
				if ((b & 0x00000001) == 0)
					evenZeros++;
				else oddZeros++;
			}
			
			//	UTF-8: first byte 0xC2-0xDF ==> 2 bytes, second byte 0x80-0xBF ==> values 0x0080-0x07FF
			else if ((-62 /* 0xC2-256 */ <= bytes[b]) && (bytes[b] <= -33 /* 0xDF-256 */) && ((b+1) < sampleSize) && (bytes[b+1] <= -65 /* 0xBF-256 */)) {
				negativeGroups++;
				b++; // loop increment takes care of second byte
			}
			
			//	UTF-8: first byte 0xE0 ==> 3 bytes, second byte 0xA0-0xBF, third byte 0x80-0xBF ==> values 0x0800-0x0FFF
			else if ((-32 /* 0xE0-256 */ == bytes[b]) && ((b+2) < sampleSize) && (-96 /* 0xA0-256 */ <= bytes[b+1]) && (bytes[b+1] <= -65 /* 0xBF-256 */) && (bytes[b+2] <= -65 /* 0xBF-256 */)) {
				negativeGroups++;
				b+=2; // loop increment takes care of third byte
			}
			
			//	UTF-8: first byte 0xE1-0xEF ==> 3 bytes, second byte 0x80-0xBF, third byte 0x80-0xBF ==> values 0x1000-0xFFFF
			else if ((-31 /* 0xE1-256 */ <= bytes[b]) && (bytes[b] <= -17 /* 0xEF-256 */) && ((b+2) < sampleSize) && (bytes[b+1] <= -65 /* 0xBF-256 */) && (bytes[b+2] <= -65 /* 0xBF-256 */)) {
				negativeGroups++;
				b+=2; // loop increment takes care of third byte
			}
			
			//	plain negative byte
			else if (bytes[b] < 0)
				negativeBytes++;
		}
		
		//	lots of odd zeros ==> characteristic for BasicLatin encoded in UTF-16LE
		if ((oddZeros * 3) > sampleSize)
			return "UTF-16LE";
		
		//	lots of event zeros ==> characteristic for BasicLatin encoded in UTF-16BE
		if ((evenZeros * 3) > sampleSize)
			return "UTF-16BE";
		
		//	more sequences of bytes below zero (0x80 or higher) than individual bytes ==> characteristic for UTF-8 (opposite would point to ANSI)
		if (negativeBytes < negativeGroups)
			return "UTF-8";
		
		//	hard to tell what this might be ...
		return null;
	}
	
	/**
	 * Recognize a character encoding from a byte order mark. Specifically,
	 * this method inspects the first three bytes of the argument byte array
	 * and recognizes the byte order marks for <code>UTF-8</code> (3 bytes),
	 * <code>UTF-16BE</code> (2 bytes), and <code>UTF-16LE</code> (2 bytes).
	 * If the argument byte array doesn't start with either of these three
	 * byte order marks, this method returns null.
	 * @param bytes the bytes to check
	 * @return the detected encoding, or null
	 */
	public static String getEncodingFromByteOrderMark(byte[] bytes) {
		if ((bytes.length >= 3) && (bytes[0] == -17 /* 0xEF-256 */) && (bytes[1] == -69 /* 0xBB-256 */) && (bytes[2] == -65 /* 0xBF-256 */))
			return "UTF-8";
		if (bytes.length >= 2) {
			if ((bytes[0] == -2 /* 0xFE */) && (bytes[1] == -1 /* 0xFF */)) {
				System.out.println("RECOGNIZED ENCODING FROM BOM: UTF-16BE");
				return "UTF-16BE";
			}
			if ((bytes[0] == -1 /* 0xFF */) && (bytes[1] == -2 /* 0xFE */)) {
				System.out.println("RECOGNIZED ENCODING FROM BOM: UTF-16LE");
				return "UTF-16LE";
			}
		}
		return null;
	}
	
	/**
	 * Get the end of the byte order mark in an array of bytes. Specifically,
	 * this method inspects the first three bytes of the argument byte array
	 * and recognizes the byte order marks for <code>UTF-8</code> (3 bytes),
	 * <code>UTF-16BE</code> (2 bytes), and <code>UTF-16LE</code> (2 bytes),
	 * and then returns the respective number of bytes. If the argument byte
	 * array doesn't start with either of these three byte order marks, this
	 * method returns 0.
	 * @param bytes the bytes to check
	 * @return the length of the detected byte order mark
	 */
	public static int getByteOrderMarkEnd(byte[] bytes) {
		if (bytes.length >= 2) {
			if ((bytes[0] == -2) && (bytes[1] == -1))
				return 2;
			if ((bytes[0] == -1) && (bytes[1] == -2))
				return 2;
		}
		if ((bytes.length >= 3) && (bytes[0] == -17) && (bytes[1] == -69) && (bytes[2] == -65))
			return 3;
		return 0;
	}
}
