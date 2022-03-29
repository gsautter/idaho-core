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
package de.uka.ipd.idaho.easyIO.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Utility to assist IO provider implementations and other code to account for
 * syntax differences between different database systems. This mainly applies
 * to schema manipulation queries, but also to the clauses that limit the size
 * of selection results.
 * 
 * @author sautter
 */
public class SyntaxHelper {
	
	/**
	 * Replace a variable in a template with its value. The value must not
	 * include the variable.
	 * @param template the template
	 * @param variable the variable to replace
	 * @param value the value of the variable
	 * @return the template with the variable replaced
	 */
	public static String replaceVariable(String template, String variable, String value) {
		if (!variable.startsWith("@"))
			variable = ("@" + variable);
//		int offset;
//		while ((offset = template.indexOf(variable)) != -1)
//			template = template.substring(0, offset) + value + template.substring(offset + variable.length());
		for (int offset; (offset = template.indexOf(variable)) != -1;)
			template = template.substring(0, offset) + value + template.substring(offset + variable.length());
		return template;
	}
	
	/**
	 * Load the product specific syntax resource for a given database system,
	 * identified by the class name of the JDBC driver. If the syntax resource
	 * is not found, this method returns an empty Properties object, but never
	 * null.
	 * @param jdbcDriverClassName the class name of the JDBC driver
	 * @return the product specific syntax resource
	 */
	public static synchronized Properties loadJdbcSyntax(String jdbcDriverClassName) {
		if (jdbcSyntaxFileMappings == null) try {
			jdbcSyntaxFileMappings = loadProperties("JdbcDriversToSyntaxResources.txt", false);
			jdbcSyntaxDefault = loadProperties("default.syntax.txt", false);
		}
		catch (IOException ioe) {
			System.out.println("TableDefinition: could not load JDBC syntax file mappings: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		String jdbcSyntaxFileName = jdbcSyntaxFileMappings.getProperty(jdbcDriverClassName);
		try {
			return loadProperties(jdbcSyntaxFileName, true);
		}
		catch (IOException ioe) {
			System.out.println("TableDefinition: could not load JDBC syntax file: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			return new Properties(jdbcSyntaxDefault);
		}
	}
	private static Properties loadProperties(String name, boolean setDefaults) throws IOException {
		String shcrn = SyntaxHelper.class.getName().replaceAll("\\.", "/");
		Properties props = new Properties(setDefaults ? jdbcSyntaxDefault : null);
		InputStream propIn = TableDefinition.class.getClassLoader().getResourceAsStream(shcrn.substring(0, shcrn.lastIndexOf('/')) + "/" + name);
		if (propIn == null)
			return props;
		BufferedReader propBr = new BufferedReader(new InputStreamReader(propIn));
		String prop;
		while ((prop = propBr.readLine()) != null) {
			prop = prop.trim();
			if ((prop.length() == 0) || prop.startsWith("//"))
				continue;
			String[] propParts = prop.split("\\s*\\=\\s*", 2);
			if (propParts.length == 2)
				props.setProperty(propParts[0], propParts[1]);
		}
		propBr.close();
		return props;
	}
	private static Properties jdbcSyntaxFileMappings = null;
	private static Properties jdbcSyntaxDefault = null;
	
	//	FOR TEST PURPOSES ONLY !!!
	public static void main(String[] args) throws Exception {
		/*
org.apache.derby.jdbc.EmbeddedDriver = derby.syntax.txt

com.microsoft.jdbc.sqlserver.SQLServerDriver = mssql.syntax.txt
com.merant.datadirect.jdbc.sqlserver.SQLServerDriver = mssql.syntax.txt

org.postgresql.Driver = postgresql.syntax.txt

com.mysql.jdbc.Driver = mysql.syntax.txt
org.gjt.mm.mysql.Driver = mysql.syntax.txt

oracle.jdbc.driver.OracleDriver = oracle.syntax.txt
		 */
		Properties sqlSyntax = loadJdbcSyntax("com.mysql.jdbc.Driver");
		System.out.println(sqlSyntax);
		System.out.println(sqlSyntax.getProperty(ResultSizeLimitationClause.SYNTAX_KEY_TOP_CLAUSE));
		System.out.println(sqlSyntax.getProperty(ResultSizeLimitationClause.SYNTAX_KEY_OFFSET_LIMIT_CLAUSE));
	}
}
