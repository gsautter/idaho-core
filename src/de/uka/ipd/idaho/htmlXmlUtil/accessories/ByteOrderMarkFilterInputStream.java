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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This wrapper jumps over a leading byte order mark in XML files, always
 * returning '&lt;' the first byte. This helps preventing errors in components
 * that take input streams as a data source, but cannot handle byte order
 * marks. For instance, this wrapper prevents the &quot;content not allowed in
 * prolog&quot; exception thrown by Java's XML components. Using this wrapper
 * with data other than XML or HTML is likely to cause undesired behavior.
 */
public class ByteOrderMarkFilterInputStream extends FilterInputStream {
	private boolean inContent = false;
	
	/**
	 * Constructor
	 * @param in the input stream to wrap
	 */
	public ByteOrderMarkFilterInputStream(InputStream in) {
		super(in);
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	public int read() throws IOException {
		int r = super.read();
		while (!this.inContent) {
			if (r == '<')
				this.inContent = true;
			else r = super.read();
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.inContent)
			return super.read(b, off, len);
		else {
			int r = super.read();
			while (!this.inContent) {
				if (r == '<')
					this.inContent = true;
				else if (r == -1)
					return -1;
				else r = super.read();
			}
			b[off] = ((byte) r);
			return (1 + super.read(b, (off + 1), (len - 1)));
		}
	}
}