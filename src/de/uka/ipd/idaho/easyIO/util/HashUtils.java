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
package de.uka.ipd.idaho.easyIO.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

/**
 * Utility class centrally providing thread safe (instance pool based) hashing
 * methods, supporting MD5 and SHA1.
 * 
 * @author sautter
 */
public class HashUtils {
	static abstract class PooledDigest extends MessageDigest {
		MessageDigest digester;
		PooledDigest(String algorithm, MessageDigest digester) {
			super(algorithm);
			this.digester = digester;
		}
		protected void engineUpdate(byte input) {
			this.digester.update(input);
		}
		protected void engineUpdate(byte[] input, int offset, int len) {
			this.digester.update(input, offset, len);
		}
		protected byte[] engineDigest() {
			return this.digester.digest();
		}
		protected void engineReset() {
			this.digester.reset();
		}
		public String toString() {
			return this.digester.toString();
		}
		protected void engineUpdate(ByteBuffer input) {
			this.digester.update(input);
		}
		protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
			return this.digester.digest(buf, offset, len);
		}
		public abstract Object clone() throws CloneNotSupportedException;
		protected int engineGetDigestLength() {
			return this.digester.getDigestLength();
		}
		protected abstract void finalize() throws Throwable;
	}
	
	/**
	 * Message digest using a pooled MD5 instance internally, returning it to
	 * the pool when this instance is finalized. This saves client code the 
	 * hassle to create its own digests and handle all the exceptions, and also
	 * saves creating more actual digest instances than necessary.
	 * 
	 * @author sautter
	 */
	public static class MD5 extends PooledDigest {
		public MD5() {
			super("MD5", getMd5());
		}
		public Object clone() throws CloneNotSupportedException {
			return new MD5();
		}
		protected void finalize() throws Throwable {
			returnMd5(this.digester);
		}
		
		/**
		 * Digest and return the result as a hexadecimal string rather than a
		 * raw byte array.
		 * @return the digest in HEX code
		 */
		public String digestHex() {
			byte[] digest = this.digest();
			return new String(RandomByteSource.getHexCode(digest));
		}
	}
	
	/**
	 * Obtain the MD5 hash of a string, in hex representation.
	 * @param str the string to hash
	 * @return the MD5 hash of the argument string
	 */
	public static String getMd5(String str) {
		try {
			return doGetMd5(str.getBytes("UTF-8"));
		}
		catch (IOException ioe) {
			System.out.println(ioe.getClass().getName() + " (" + ioe.getMessage() + ") while hashing '" + str + "'.");
			ioe.printStackTrace(System.out); // should not happen, but Java don't know ...
			return null; // should not happen, but Java don't know ...
		}
	}
	
	/**
	 * Obtain the MD5 hash of a string, in hex representation.
	 * @param str the string to hash
	 * @return the MD5 hash of the argument string
	 */
	public static String getMd5(byte[] bytes) {
		return doGetMd5(bytes);
	}
	
	private static String doGetMd5(byte[] bytes) {
		MessageDigest md5Digester = getMd5();
		try {
			md5Digester.update(bytes);
			byte[] checksumBytes = md5Digester.digest();
			return new String(RandomByteSource.getHexCode(checksumBytes));
		}
		finally {
			returnMd5(md5Digester);
		}
	}
	
	private static LinkedList poolMd5 = new LinkedList();
	private static synchronized MessageDigest getMd5() {
		if (poolMd5.size() != 0) {
			MessageDigest dataHash = ((MessageDigest) poolMd5.removeFirst());
			dataHash.reset();
			return dataHash;
		}
		try {
			MessageDigest dataHash = MessageDigest.getInstance("MD5");
			dataHash.reset();
			return dataHash;
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println(nsae.getClass().getName() + " (" + nsae.getMessage() + ") while creating checksum digester.");
			nsae.printStackTrace(System.out); // should not happen, but Java don't know ...
			return null;
		}
	}
	private static synchronized void returnMd5(MessageDigest dataHash) {
		if (dataHash == null)
			return;
		dataHash.reset();
		poolMd5.addLast(dataHash);
	}
	
	/**
	 * Message digest using a pooled SHA1 instance internally, returning it to
	 * the pool when this instance is finalized. This saves client code the 
	 * hassle to create its own digests and handle all the exceptions, and also
	 * saves creating more actual digest instances than necessary.
	 * 
	 * @author sautter
	 */
	public static class SHA1 extends PooledDigest {
		public SHA1() {
			super("SHA1", getSha1());
		}
		public Object clone() throws CloneNotSupportedException {
			return new SHA1();
		}
		protected void finalize() throws Throwable {
			returnSha1(this.digester);
		}
		
		/**
		 * Digest and return the result as a hexadecimal string rather than a
		 * raw byte array.
		 * @return the digest in HEX code
		 */
		public String digestHex() {
			byte[] digest = this.digest();
			return new String(RandomByteSource.getHexCode(digest));
		}
	}
	
	/**
	 * Obtain the SHA1 hash of a string, in hex representation.
	 * @param bytes the bytes to hash
	 * @return the SHA1 hash of the argument bytes
	 */
	public static String getSha1(String str) {
		try {
			return doGetSha1(str.getBytes("UTF-8"));
		}
		catch (IOException ioe) {
			System.out.println(ioe.getClass().getName() + " (" + ioe.getMessage() + ") while hashing '" + str + "'.");
			ioe.printStackTrace(System.out); // should not happen, but Java don't know ...
			return null; // should not happen, but Java don't know ...
		}
	}
	
	/**
	 * Obtain the SHA1 hash of a string, in hex representation.
	 * @param bytes the bytes to hash
	 * @return the SHA1 hash of the argument bytes
	 */
	public static String getSha1(byte[] bytes) {
		return doGetSha1(bytes);
	}
	
	private static String doGetSha1(byte[] bytes) {
		MessageDigest sha1Digester = getSha1();
		try {
			sha1Digester.update(bytes);
			byte[] checksumBytes = sha1Digester.digest();
			return new String(RandomByteSource.getHexCode(checksumBytes));
		}
		finally {
			returnSha1(sha1Digester);
		}
	}
	
	private static LinkedList poolSha1 = new LinkedList();
	static synchronized MessageDigest getSha1() {
		if (poolSha1.size() != 0) {
			MessageDigest dataHash = ((MessageDigest) poolSha1.removeFirst());
			dataHash.reset();
			return dataHash;
		}
		try {
			MessageDigest dataHash = MessageDigest.getInstance("SHA1");
			dataHash.reset();
			return dataHash;
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println(nsae.getClass().getName() + " (" + nsae.getMessage() + ") while creating checksum digester.");
			nsae.printStackTrace(System.out); // should not happen, but Java don't know ...
			return null;
		}
	}
	private static synchronized void returnSha1(MessageDigest dataHash) {
		if (dataHash == null)
			return;
		dataHash.reset();
		poolSha1.addLast(dataHash);
	}
}
