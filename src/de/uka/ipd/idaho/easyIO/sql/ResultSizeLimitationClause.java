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

import java.util.Properties;

/**
 * Container for generalized result size limitation clauses to use in
 * SELECT queries. Depending upon the database engine in use, either one
 * of the <code>topClause</code> and  <code>limitClause</code> will be
 * empty, so client code can safely insert both in SQL SELECT queries in
 * their respective positions.
 * 
 * @author sautter
 */
public class ResultSizeLimitationClause {
	public static final String SYNTAX_KEY_TOP_CLAUSE = "TOP_CLAUSE";
	public static final String SYNTAX_KEY_OFFSET_CLAUSE = "OFFSET_CLAUSE";
	public static final String SYNTAX_KEY_LIMIT_CLAUSE = "LIMIT_CLAUSE";
	public static final String SYNTAX_KEY_OFFSET_LIMIT_CLAUSE = "OFFSET_LIMIT_CLAUSE";
	public static final String SYNTAX_OFFSET_VARIABLE = "@offset";
	public static final String SYNTAX_LIMIT_VARIABLE = "@limit";
	
	/** the TOP clause to insert immediately after the SELECT command */
	public final String topClause;
	
	/** the OFFSET/LIMIT clause to insert at the very end of a SELECT query */
	public final String offsetLimitClause;
	
	/** Constructor
	 * @param topClause the TOP clause to insert immediately after the SELECT command
	 * @param offsetLimitClause the OFFSET/LIMIT clause to insert at the very end of a SELECT query
	 */
	public ResultSizeLimitationClause(String topClause, String offsetLimitClause) {
		this.topClause = topClause;
		this.offsetLimitClause = offsetLimitClause;
	}
	
	/**
	 * Create a query result size limitation clause using a given SQL syntax
	 * dialect.
	 * @param offset the offset to use
	 * @param limit the number of rows to return
	 * @param sqlSyntax a Properties object holding templates that mask product
	 *            specific database features
	 * @return the result size limitation clause
	 */
	public static ResultSizeLimitationClause createResultSizeLimitationClause(int offset, int limit, Properties sqlSyntax) {
		String topClause = sqlSyntax.getProperty(SYNTAX_KEY_TOP_CLAUSE, "");
		topClause = SyntaxHelper.replaceVariable(topClause, SYNTAX_OFFSET_VARIABLE, ("" + offset));
		topClause = SyntaxHelper.replaceVariable(topClause, SYNTAX_LIMIT_VARIABLE, ("" + limit));
		String offsetLimitClause;
		if ((offset >= 0) && (limit >= 0))
			offsetLimitClause = sqlSyntax.getProperty(SYNTAX_KEY_OFFSET_LIMIT_CLAUSE, "");
		else if (offset >= 0)
			offsetLimitClause = sqlSyntax.getProperty(SYNTAX_KEY_OFFSET_CLAUSE, "");
		else if (limit >= 0)
			offsetLimitClause = sqlSyntax.getProperty(SYNTAX_KEY_LIMIT_CLAUSE, "");
		else offsetLimitClause = "";
		offsetLimitClause = SyntaxHelper.replaceVariable(offsetLimitClause, SYNTAX_OFFSET_VARIABLE, ("" + offset));
		offsetLimitClause = SyntaxHelper.replaceVariable(offsetLimitClause, SYNTAX_LIMIT_VARIABLE, ("" + limit));
		return new ResultSizeLimitationClause(topClause, offsetLimitClause);
	}
}
