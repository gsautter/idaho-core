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
package de.uka.ipd.idaho.htmlXmlUtil.exceptions;


/**
 * Exception being thrown during parsing if the parser encounters an end tag
 * without having seen a matching start tag. This exception is thrown only if
 * the grammar in use returns false for the getCorrectErrors() method.
 * Otherwise, the parser will ignore the spurious end tag.
 * 
 * @author sautter
 */
public class UnexpectedEndTagException extends ParseException {
	
	/**
	 * @param endTag the unexpected end tag
	 * @param expectedEndTag the next expected end tag
	 * @param position the offset of the offending tag
	 */
	public UnexpectedEndTagException(String endTag, String expectedEndTag, int position) {
		super(("Unexpected end tag '" + endTag + "' before '" + expectedEndTag + "' at " + position + "."), position);
	}
	
	/**
	 * Obtain the position of the offending tag causing the exception as its
	 * offset from the start of the parsed character stream.
	 * @return the position of the offending tag
	 */
	public int getPosition() {
		return super.getPosition();
	}
}
