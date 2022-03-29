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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import de.uka.ipd.idaho.easyIO.util.HashUtils.MD5;
import de.uka.ipd.idaho.easyIO.util.RandomByteSource;


/**
 * An input stream that updates a wrapped message digester as data is read or
 * skipped from it. The hash is finalized when the <code>close()</code> method
 * is called.
 * 
 * @author sautter
 */
public class DataHashInputStream extends FilterInputStream {
	private MessageDigest dataHasher;
	private byte[] dataHashBytes = null;
	private String dataHashHex = null;
	
	/** Constructor using MD5 message digest
	 * @param in the input stream to wrap
	 */
	public DataHashInputStream(InputStream in) {
		this(in, new MD5());
	}
	
	/** Constructor using custom message digest
	 * @param in the input stream to wrap
	 * @param dataHasher the message digest to use
	 */
	public DataHashInputStream(InputStream in, MessageDigest dataHasher) {
		super(in);
		this.dataHasher = dataHasher;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	public synchronized int read() throws IOException {
		int r = this.in.read();
		if (r != -1)
			this.dataHasher.update((byte) r);
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		int r = this.in.read(b, off, len);
		if (r != -1)
			this.dataHasher.update(b, off, r);
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		if (n <= 0)
			return 0;
		long toSkip = n;
		byte[] skipBuffer = new byte[(int) Math.min(2048, toSkip)];
		for (int r; 0 < toSkip;) {
			r = this.read(skipBuffer, 0, ((int) Math.min(skipBuffer.length, toSkip)));
			if (r < 0)
				break;
			toSkip -= r;
		}
		return (n - toSkip);
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#mark(int)
	 */
	public synchronized void mark(int readlimit) {}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}
	
	/** 
	 * This implementation first closes the wrapped input stream and then
	 * finalizes the MD5 hash of the data.
	 * @see java.io.FilterOutputStream#close()
	 */
	public void close() throws IOException {
		super.close();
		
		//	we have been closed before
		if (this.dataHasher == null)
			return;
		
		//	finalize hash and rename file
		this.dataHashBytes = this.dataHasher.digest();
		this.dataHashHex = new String(RandomByteSource.getHexCode(this.dataHashBytes));
		
		//	return digester to instance pool
		this.dataHasher = null;
	}
	
	/**
	 * Retrieve the data hash as a hex string, i.e., the digest of all the
	 * bytes read from the stream. Before the <code>close()</code> method is
	 * called. this method returns null.
	 * @return the digest hash of the data written to the stream
	 */
	public String getDataHash() {
		return this.dataHashHex;
	}
	
	/**
	 * Retrieve the data hash as a raw byte array, i.e., the digest of all the
	 * bytes read from the stream. Before the <code>close()</code> method is
	 * called. this method returns null.
	 * @return the digest hash of the data written to the stream
	 */
	public byte[] getDataHashBytes() {
		return this.dataHashBytes;
	}
}
