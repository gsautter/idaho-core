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
package de.uka.ipd.idaho.easyIO.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;


/**
 * Utility class for base 64 encoding and decoding.
 * 
 * @author sautter
 */
public class Base64 {
	static final char paddingChar = '=';
	static final String base64chars = 
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"abcdefghijklmnopqrstuvwxyz" +
		"0123456789" +
		"+/";
	static final int decodeBase64Char(char ch) {
		if (('A' <= ch) && (ch <= 'Z'))
			return (ch - 'A');
		else if (('a' <= ch) && (ch <= 'z'))
			return (ch - 'a' + 26);
		else if (('0' <= ch) && (ch <= '9'))
			return (ch - '0' + 52);
		else return ((ch == '+') ? 62 : 63);
	}
	
	/**
	 * Encode an array of bytes into a Base64 string
	 * @param bytes the bytes to encode
	 * @return the Base64 code of the specified bytes
	 */
	public static final String encode(int[] bytes) {
		StringBuffer base64 = new StringBuffer();
		int[] byteBlockBuffer = new int[3];
		
		for (int b = 0; b < bytes.length; b += 3) {
			int byteBlockSize = Math.min((bytes.length - b), 3);
			System.arraycopy(bytes, b, byteBlockBuffer, 0, byteBlockSize);
			if (byteBlockSize < byteBlockBuffer.length)
				Arrays.fill(byteBlockBuffer, byteBlockSize, byteBlockBuffer.length, 0);
			
			int byteBlock = (
					((byteBlockBuffer[0] & 0xFF) << 16)
					|
					((byteBlockBuffer[1] & 0xFF) << 8)
					|
					((byteBlockBuffer[2] & 0xFF) << 0)
				);
			base64.append(base64chars.charAt((byteBlock >>> 18) & 0x3F));
			base64.append(base64chars.charAt((byteBlock >>> 12) & 0x3F));
			base64.append((byteBlockSize < 2) ? paddingChar : base64chars.charAt((byteBlock >>> 6) & 0x3F));
			base64.append((byteBlockSize < 3) ? paddingChar : base64chars.charAt((byteBlock >>> 0) & 0x3F));
		}
		return base64.toString();
	}
	
	/**
	 * Output stream sending binary data through a character level connection
	 * using Base64 encoding. It is highly recommended to wrap instances of
	 * this class around a <code>BufferedWriter</code> for better efficiency.
	 * 
	 * @author sautter
	 */
	public static class EncoderOutputStream extends OutputStream {
		private Writer out;
		private int[] buffer = new int[3];
		private int bufferLevel = 0;
		
		/**
		 * Constructor
		 * @param out the Writer for transferring the data written to the stream
		 */
		public EncoderOutputStream(Writer out) {
			this.out = out;
		}
		
		/* (non-Javadoc)
		 * @see java.io.OutputStream#write(int)
		 */
		public void write(int b) throws IOException {
			if (this.out == null)
				throw new IOException("Closed.");
			this.buffer[this.bufferLevel++] = b;
			if (this.bufferLevel == this.buffer.length)
				this.flushBuffer();
		}
		
	 	/**
		 * Closes this output stream. This will pad any bytes remaining in the
		 * buffer to form a full triplet, write this data to the underlying writer,
		 * and flush the underlying writer afterward. After this method has been
		 * invoked, no further bytes should be written via the write() method
		 * because once the data has been padded, sending further bytes will result
		 * in an error. This method will not close the underlying writer, though.
		 * For this purpose, please use the close(boolean) method with true as the
		 * argument.
		 * @see java.io.OutputStream#close()
		 */
		public void close() throws IOException {
			if (this.out == null)
				throw new IOException("Closed.");
			this.close(false);
		}
		
		/**
		 * Closes this output stream. This will pad any bytes remaining in the
		 * buffer to form a full triplet, write this data to the underlying writer,
		 * and flush and optionally close the underlying writer afterward. After
		 * this method has been invoked, no further bytes should be written via the
		 * write() method because once the data has been padded, sending further
		 * bytes will result in an error.
		 * @param closeWriter close the underlying writer?
		 * @see java.io.OutputStream#close()
		 */
		public void close(boolean closeWriter) throws IOException {
			if (this.out == null)
				throw new IOException("Closed.");
			
			if (this.bufferLevel != 0)
				this.flushBuffer();
			
			if (closeWriter) {
				this.out.flush();
				this.out.close();
			}
			this.out = null;
		}
		
		private void flushBuffer() throws IOException {
			if (this.out == null)
				throw new IOException("Closed.");
			
			if (this.bufferLevel < this.buffer.length)
				Arrays.fill(this.buffer, this.bufferLevel, this.buffer.length, 0);
			int byteBlock = (
					((this.buffer[0] & 0xFF) << 16)
					|
					((this.buffer[1] & 0xFF) << 8)
					|
					((this.buffer[2] & 0xFF) << 0)
				);
			
			char[] byteBlockChars = {
					base64chars.charAt((byteBlock >>> 18) & 0x3F),
					base64chars.charAt((byteBlock >>> 12) & 0x3F),
					(this.bufferLevel < 2) ? paddingChar : base64chars.charAt((byteBlock >>> 6) & 0x3F),
					(this.bufferLevel < 3) ? paddingChar : base64chars.charAt((byteBlock >>> 0) & 0x3F),
				};
			this.bufferLevel = 0;
			this.out.write(byteBlockChars);
		}
	}
	
	/**
	 * Decode a Base64 encoded array of bytes
	 * @param base64 the Base64 string to decode
	 * @return the bytes encoded in the specified string
	 */
	public static final byte[] decode(String base64) {
		int byteCount = ((base64.length() / 4) * 3);
		if (base64.endsWith("" + paddingChar + "" + paddingChar))
			byteCount -= 2;
		else if (base64.endsWith("" + paddingChar))
			byteCount -= 1;
		byte[] bytes = new byte[byteCount];
		int bytePos = 0;
		char[] charBlock = new char[4];
		for (int c = 0; c < base64.length(); c += 4) {
			base64.getChars(c, (c + charBlock.length), charBlock, 0);
			int byteBlock = (
					(decodeBase64Char(charBlock[0]) << 18)
					|
					(decodeBase64Char(charBlock[1]) << 12)
					|
					((charBlock[2] == paddingChar) ? 0 : (decodeBase64Char(charBlock[2]) << 6))
					|
					((charBlock[3] == paddingChar) ? 0 : (decodeBase64Char(charBlock[3]) << 0))
				);
			bytes[bytePos++] = ((byte) ((byteBlock >>> 16) & 0xFF));
			if (bytePos < bytes.length)
				bytes[bytePos++] = ((byte) ((byteBlock >>> 8) & 0xFF));
			if (bytePos < bytes.length)
				bytes[bytePos++] = ((byte) ((byteBlock >>> 0) & 0xFF));
		}
		return bytes;
	}
	
	/**
	 * Input stream reading binary data that comes in Base64 encoding via a
	 * character level connection. It is highly recommended to wrap instances
	 * of this class around a <code>BufferedReader</code> for better efficiency.
	 * 
	 * @author sautter
	 */
	public static class DecoderInputStream extends InputStream {
		private Reader in;
		private int[] buffer = {-1, -1, -1};
		private int bufferPos = this.buffer.length; // initialize to end of buffer, filling on first read()
		
		/**
		 * Constructor
		 * @param in the Reader to read from
		 */
		public DecoderInputStream(Reader in) {
			this.in = in;
		}
		
		/* (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException {
			if (this.in == null)
				throw new IOException("Cloased.");
			if (this.bufferPos == -1)
				return -1;
			if (this.bufferPos == this.buffer.length)
				this.fillBuffer();
			if (this.buffer[this.bufferPos] == -1) {
				this.bufferPos = -1;
				return -1;
			}
			else return this.buffer[this.bufferPos++];
		}
		
		/* (non-Javadoc)
		 * @see java.io.InputStream#close()
		 */
		public void close() throws IOException {
			this.in.close();
			this.in = null;
		}
		
		private void fillBuffer() throws IOException {
			Arrays.fill(this.buffer, -1);
			this.bufferPos = 0;
			
			char[] charBlock = new char[4];
			int read = this.in.read(charBlock, 0, charBlock.length);
			if (read < 2) // end of stream, or at least no complete byte
				return;
			if (read < charBlock.length)
				Arrays.fill(charBlock, read, charBlock.length, paddingChar);
			
			int byteBlock = (
					(decodeBase64Char(charBlock[0]) << 18)
					|
					(decodeBase64Char(charBlock[1]) << 12)
					|
					((charBlock[2] == paddingChar) ? 0 : (decodeBase64Char(charBlock[2]) << 6))
					|
					((charBlock[3] == paddingChar) ? 0 : (decodeBase64Char(charBlock[3]) << 0))
				);
			this.buffer[0] = ((byteBlock >>> 16) & 0xFF);
			if (charBlock[2] != paddingChar)
				this.buffer[1] = ((byteBlock >>> 8) & 0xFF);
			if (charBlock[3] == paddingChar)
				this.buffer[2] = ((byteBlock >>> 0) & 0xFF);
		}
	}
}