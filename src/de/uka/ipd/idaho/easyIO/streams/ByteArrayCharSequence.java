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
package de.uka.ipd.idaho.easyIO.streams;


/**
 * A <code>CharSequence</code> backed by a byte array. Instances of this class
 * do <b>not</b> consider character encoding, interpreting each byte as an ANSI
 * character in the 0-255 range. This class is not intended for general-purpose
 * data handling, but rather to facilitate applications like pattern matching
 * on a raw sequence of bytes, e.g. to extract specific keywords that can help
 * identify an unknown character encoding.
 * 
 * @author sautter
 */
public class ByteArrayCharSequence implements CharSequence {
	private byte[] data;
	private int offset;
	private int length;
	
	/** Constructor using full array
	 * @param data the byte array to wrap
	 */
	public ByteArrayCharSequence(byte[] data) {
		this(data, 0, data.length);
	}
	
	/** Constructor using portion of array
	 * @param data the byte array to wrap
	 * @param offset the offset of the first byte to use from the argument array
	 * @param length the number of bytes to expose from the argument array
	 */
	public ByteArrayCharSequence(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.length = length;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.CharSequence#length()
	 */
	public int length() {
		return this.length;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.CharSequence#charAt(int)
	 */
	public char charAt(int index) {
		return ((char) (this.data[this.offset + index] & 0x00FF));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	public CharSequence subSequence(int start, int end) {
		return new ByteArrayCharSequence(this.data, (this.offset + start), (end - start));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer ss = new StringBuffer();
		for (int c = 0; c < this.length; c++)
			ss.append(this.charAt(c));
		return ss.toString();
	}
}
