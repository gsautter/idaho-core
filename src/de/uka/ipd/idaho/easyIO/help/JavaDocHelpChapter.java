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
package de.uka.ipd.idaho.easyIO.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import de.uka.ipd.idaho.easyIO.streams.CharSequenceReader;
import de.uka.ipd.idaho.easyIO.streams.PeekReader;
import de.uka.ipd.idaho.htmlXmlUtil.Parser;
import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiver;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Html;

/**
 * HelpChapter loading its content on demand from the class top JavaDoc of a
 * given class. This avoids loading all content of a help into memory on
 * startup, thus preventing pages being loaded that may never be displayed.
 * The first access to the page represented by an instance of this class,
 * however, might take longer, since the page content has to be extracted
 * dynamically. Also, instances of this class only work if the Java source
 * code is available on the class path and classes have not been obfuscated.
 * 
 * @author sautter
 */
public class JavaDocHelpChapter extends HelpChapter {
	private static final Html HTML = new Html();
	private static final Parser HTML_PARSER = new Parser(HTML);
	private Class subjectClass = null;
	private StringBuffer contentBuffer = null;
	
	/**	Constructor
	 * @param title the title for this help chapter
	 * @param subjectClass the class whose JavaDoc to display
	 */
	public JavaDocHelpChapter(String title, Class subjectClass) {
		super(title, ("<HTML>The content of this chapter will be loaded from the sources of<BR><TT>" + subjectClass.getName() + "</TT><BR>on demand.</HTML>"));
		this.subjectClass = subjectClass;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.help.HelpChapter#getTextReader()
	 */
	public Reader getTextReader() {
		
		//	we've loaded this one before
		if (this.contentBuffer != null)
			return new CharSequenceReader(this.contentBuffer);
		
		//	load and buffer content
		try {
			
			//	get hold of source file
			String subjectClassSourceResName = this.subjectClass.getName();
			subjectClassSourceResName = (subjectClassSourceResName.replace('.', '/') + ".java");
			String subjectClassName = this.subjectClass.getName();
			subjectClassName = subjectClassName.substring(subjectClassName.lastIndexOf(".") + ".".length());
			
			//	extract class top JavaDoc
			StringBuffer javaDoc = new StringBuffer("<html><head>");
			javaDoc.append("<title>" + HTML.escape(this.getTitle()) + "</title>");
			javaDoc.append("</head><body><div>");
			BufferedReader jdBr = new BufferedReader(new InputStreamReader(this.subjectClass.getClassLoader().getResourceAsStream(subjectClassSourceResName)));
			extractJavaDoc(jdBr, subjectClassName, javaDoc);
			jdBr.close();
			javaDoc.append("</div></body></html>");
			
			//	create content buffer
			this.contentBuffer = new StringBuffer();
			
			//	stream content into buffer
			HTML_PARSER.stream(new CharSequenceReader(javaDoc), new TokenReceiver() {
				public void storeToken(String token, int treeDepth) throws IOException {
					if (!token.toLowerCase().startsWith("<meta "))
						contentBuffer.append(token);
					else if (token.toLowerCase().indexOf("content-type") == -1)
						contentBuffer.append(token);
				}
				public void close() throws IOException {}
			});
			
			//	finally ...
			return new CharSequenceReader(this.contentBuffer);
		}
		catch (Exception e) {
			System.out.println("... ERROR");
			return new StringReader("<HTML><TT>" + e.getClass().getName() + "</TT><BR>(" + e.getMessage() + ")<BR>while creating content reader, sorry.</HTML>");
		}
	}
//	
//	public static void main(String[] args) throws Exception {
//		Class subjectClass = AnnotationPatternMatcher.class;
//		
//		//	get hold of source file
//		String subjectClassSourceResName = subjectClass.getName();
//		subjectClassSourceResName = (subjectClassSourceResName.replace('.', '/') + ".java");
//		System.out.println("Resource is " + subjectClassSourceResName);
//		String subjectClassName = subjectClass.getName();
//		subjectClassName = subjectClassName.substring(subjectClassName.lastIndexOf(".") + ".".length());
//		System.out.println("Class name is " + subjectClassName);
//		
//		//	extract class top JavaDoc
//		ClassLoader subjectClassLoader = subjectClass.getClassLoader();
//		StringBuffer javaDoc = new StringBuffer("<html><head>");
//		javaDoc.append("<title>" + HTML.escape(subjectClassName) + "</title>");
//		javaDoc.append("</head><body><div>");
//		BufferedReader jdBr = new BufferedReader(new InputStreamReader(subjectClassLoader.getResourceAsStream(subjectClassSourceResName)));
//		extractJavaDoc(jdBr, subjectClassName, javaDoc);
//		jdBr.close();
//		javaDoc.append("</div></body></html>");
//		System.out.println(javaDoc);
//		final StringBuffer contentBuffer = new StringBuffer();
//		
//		//	stream content into buffer
//		HTML_PARSER.stream(new BufferedReader(new CharSequenceReader(javaDoc)), new TokenReceiver() {
//			public void storeToken(String token, int treeDepth) throws IOException {
//				if (!token.toLowerCase().startsWith("<meta "))
//					contentBuffer.append(token.startsWith("<") ? token : token.replaceAll("\\x20+", " "));
//				else if (token.toLowerCase().indexOf("content-type") == -1)
//					contentBuffer.append(token.startsWith("<") ? token : token.replaceAll("\\x20+", " "));
//			}
//			public void close() throws IOException {}
//		});
//		System.out.println(contentBuffer);
//	}
	
	private static void extractJavaDoc(BufferedReader sourceBr, String forClassName, StringBuffer javaDoc) throws IOException {
		PeekReader pr = new PeekReader(sourceBr, "/**".length());
		
		//	parse Java code
		StringBuffer code = new StringBuffer();
		ArrayList comments = new ArrayList();
		char lch = ((char) 0);
		while (pr.peek() != -1) {
			if (pr.startsWith("/**/", true)) {
				Comment cmt = cropBlockComment(pr, code.length());
				if (cmt != null)
					comments.add(cmt);
			}
			else if (pr.startsWith("/**", true)) {
				Comment cmt = cropJavaDoc(pr, code.length());
				if (cmt != null)
					comments.add(cmt);
			}
			else if (pr.startsWith("/*", true)) {
				Comment cmt = cropBlockComment(pr, code.length());
				if (cmt != null)
					comments.add(cmt);
			}
			else if (pr.startsWith("\"", true)) {
				cropString(pr, code);
				lch = '"';
			}
			else if (pr.startsWith("'", true)) {
				code.append((char) pr.read()); // high comma
				if (pr.peek() == '\\')
					code.append((char) pr.read()); // escaping backslash
				code.append((char) pr.read()); // char proper
				code.append((char) pr.read()); // high comma
				lch = '\'';
			}
			else if (pr.startsWith("//", true)) {
				Comment cmt = cropLineEndComment(pr, code.length());
				if (cmt != null)
					comments.add(cmt);
			}
			else {
				char ch = ((char) pr.read());
				if (' ' < ch) {
					code.append(ch);
					lch = ch;
				}
				else if (lch != ' ') {
					code.append(' ');
					lch = ' ';
				}
			}
		}
		
		//	find start of target class code
		int forClassStart;
		if (code.indexOf("class " + forClassName) == 0)
			forClassStart = 0;
		else forClassStart = code.indexOf(" class " + forClassName);
		while ((forClassStart + " class ".length() + forClassName.length()) < code.length()) {
			char fchCh = code.charAt(forClassStart + " class ".length() + forClassName.length());
			if (Character.isJavaIdentifierPart(fchCh))
				forClassStart = code.indexOf((" class " + forClassName), (forClassStart + " class ".length() + forClassName.length()));
			else break;
		}
		
		//	find last JavaDoc comment before start of target class
		Comment jdCmt = null;
		for (int c = 0; c < comments.size(); c++) {
			Comment cmt = ((Comment) comments.get(c));
			if (forClassStart < cmt.codePos)
				break;
			if (cmt.type == 'D')
				jdCmt = cmt;
		}
		if (jdCmt == null)
			return;
		
		//	finally ...
		appendJavaDoc(jdCmt.comment, javaDoc);
	}
	
	private static void appendJavaDoc(String rawJavaDoc, StringBuffer javaDoc) throws IOException {
		/* TODO format this !!!:
		 * - translate {@<xyz> ...} into HTML
		 * - maybe remove @author, @since, @deprecated tags
		 */
		boolean lastWasLineBreak = true;
		for (int c = "/**".length(); c < rawJavaDoc.length(); c++) {
			if (rawJavaDoc.startsWith("*/", c)) {
				truncateTailingLineSpace(javaDoc);
				break;
			}
			char ch = rawJavaDoc.charAt(c);
			if (ch == '\r') {
				truncateTailingLineSpace(javaDoc);
				javaDoc.append("\r\n");
				if (rawJavaDoc.startsWith("\r\n", c))
					c++; // skip newlinw as well
				lastWasLineBreak = true;
			}
			else if (ch == '\n') {
				truncateTailingLineSpace(javaDoc);
				javaDoc.append("\r\n");
				lastWasLineBreak = true;
			}
			else if (lastWasLineBreak && (ch <= ' ') /* line breaks handled above, so not skipped */)
				continue;
			else if (lastWasLineBreak && (ch == '*') /* skipping first asterisk and any spaces following it */ ) {
				lastWasLineBreak = false;
				while (((c+1) < rawJavaDoc.length()) && (" \t".indexOf(rawJavaDoc.charAt(c+1)) != -1))
					c++; // skip over any whitespace following first asterisk
			}
			else {
				javaDoc.append(ch);
				lastWasLineBreak = false;
			}
		}
	}
	private static void truncateTailingLineSpace(StringBuffer javaDoc) {
		for (int lc; (lc = (javaDoc.length() - 1)) > 0;) {
			char lch = javaDoc.charAt(lc);
			if ((lch == '\r') || (lch == '\n'))
				break;
			else if (lch <= ' ')
				javaDoc.deleteCharAt(lc);
			else break;
		}
	}
	
	private static class Comment {
		final int codePos;
		final char type;
		final String comment;
		Comment(int codePos, char type, String comment) {
			this.codePos = codePos;
			this.type = type;
			this.comment = comment;
		}
	}
	private static Comment cropJavaDoc(PeekReader pr, int codePos) throws IOException {
		StringBuffer jd = new StringBuffer();
		jd.append((char) pr.read()); // slash
		jd.append((char) pr.read()); // first asterisk
		jd.append((char) pr.read()); // second asterisk
		while (pr.peek() != -1) {
			if (pr.startsWith("*/", true)) {
				jd.append((char) pr.read()); // asterisk
				jd.append((char) pr.read()); // slash
				break;
			}
			else jd.append((char) pr.read());
		}
		return new Comment(codePos, 'D', jd.toString());
	}
	private static Comment cropBlockComment(PeekReader pr, int codePos) throws IOException {
		StringBuffer bc = new StringBuffer();
		bc.append((char) pr.read()); // slash
		bc.append((char) pr.read()); // asterisk
		while (pr.peek() != -1) {
			if (pr.startsWith("*/", true)) {
				bc.append((char) pr.read()); // asterisk
				bc.append((char) pr.read()); // slash
				break;
			}
			else bc.append((char) pr.read());
		}
		return new Comment(codePos, 'B', bc.toString());
	}
	private static void cropString(PeekReader pr, StringBuffer code) throws IOException {
		code.append((char) pr.read());
		boolean escaped = false;
		while (pr.peek() != -1) {
			if (escaped) {
				code.append((char) pr.read());
				escaped = false;
			}
			else if (pr.peek() == '"') {
				code.append((char) pr.read());
				break;
			}
			else if (pr.peek() == '\\') {
				code.append((char) pr.read());
				escaped = true;
			}
			else code.append((char) pr.read());
		}
	}
	private static Comment cropLineEndComment(PeekReader pr, int codePos) throws IOException {
		StringBuffer lec = new StringBuffer();
		lec.append((char) pr.read()); // slash
		lec.append((char) pr.read()); // slash
		while (pr.peek() != -1) {
			if ((pr.peek() == '\r') || (pr.peek() == '\n'))
				break;
			else lec.append((char) pr.read());
		}
		return new Comment(codePos, 'L', lec.toString());
	}
}
