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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

import de.uka.ipd.idaho.easyIO.util.RandomByteSource;
import de.uka.ipd.idaho.easyIO.util.HashUtils.MD5;

/**
 * An output stream that updates a wrapped message digester as data is written
 * to it. The hash is finalized when the <code>close()</code> method is called.
 * 
 * @author sautter
 */
public class DataHashOutputStream extends FilterOutputStream {
	private MessageDigest dataHasher;
	private byte[] dataHashBytes = null;
	private String dataHashHex = null;
	
	/** Constructor using MD5 message digest
	 * @param out the output stream to wrap
	 */
	public DataHashOutputStream(OutputStream out) {
		this(out, new MD5());
	}
	
	/** Constructor using custom message digest
	 * @param out the output stream to wrap
	 * @param dataHasher the message digest to use
	 */
	public DataHashOutputStream(OutputStream out, MessageDigest dataHasher) {
		super(out);
		this.dataHasher = dataHasher;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		this.out.write(b);
		this.dataHasher.update((byte) b);
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		this.out.write(b, off, len);
		this.dataHasher.update(b, off, len);
	}
	
	/** 
	 * This implementation first closes the wrapped input stream and then
	 * finalizes the digest of the data.
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
	 * bytes written to the stream. Before the <code>close()</code> method is
	 * called. this method returns null.
	 * @return the digest hash of the data written to the stream
	 */
	public String getDataHash() {
		return this.dataHashHex;
	}
	
	/**
	 * Retrieve the data hash as a raw byte array, i.e., the digest of all the
	 * bytes written to the stream. Before the <code>close()</code> method is
	 * called. this method returns null.
	 * @return the digest hash of the data written to the stream
	 */
	public byte[] getDataHashBytes() {
		return this.dataHashBytes;
	}
}
