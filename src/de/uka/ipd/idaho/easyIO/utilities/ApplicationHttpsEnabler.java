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
package de.uka.ipd.idaho.easyIO.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import de.uka.ipd.idaho.easyIO.util.HashUtils;

/**
 * Utility managing HTTPS certificates in some location (folder) known and
 * accessible to client code, or implementing some default behavior.<br/>
 * If interactivity is activated in the constructor, this class will open a
 * prompt whenever it encounters an unknown certificate, asking the user
 * whether or not to accept the certificate.<br/>
 * If interactivity is deactivated, acceptance of unknown certificates is
 * decided by the <code>trustUnknown<code> constructor argument.<br/>
 * Sub classes seeking a more differentiated behavior (e.g. only trusting
 * specific (sub) domains) should overwrite the
 * <code>askPermissionToAccept()</code> method to implement that behavior.
 * 
 * @author sautter
 */
public class ApplicationHttpsEnabler extends AbstractHttpsEnabler {
	private File dataPath;
	private boolean interactive;
	private boolean acceptUnknown;
	
	/** Constructor using a key store persisted in the argument data path.<br/>
	 * If the argument data path exists, it has to be a directory, if it does
	 * not exist, it is created as a directory. If the data path is an existing
	 * file, or if directory creation fails, the key store is deactivated.<br/>
	 * WARNING: using a key store and at the same time default accepting all
	 * unknown certificates without user interactivity may be very unsafe.
	 * @param dataPath the folder to keep the key store in
	 * @param interactive prompt user for decision about unknown certificates?
	 * @param acceptUnknown by default accept unknown certificates?
	 */
	public ApplicationHttpsEnabler(File dataPath, boolean interactive, boolean acceptUnknown) {
		super((dataPath != null) && (dataPath.exists() ? dataPath.isDirectory() : dataPath.mkdirs()));
		this.dataPath = dataPath;
		this.interactive = interactive;
		this.acceptUnknown = acceptUnknown;
	}
	
	protected InputStream getKeyStoreInputStream() throws IOException {
		if (this.dataPath == null)
			return null;
		File kcFile = new File(this.dataPath, "knownCertificates.ks");
		return (kcFile.exists() ? new FileInputStream(kcFile) : null);
	}
	protected OutputStream getKeyStoreOutputStream() throws IOException {
		if (this.dataPath == null)
			return null;
		File kcFile = new File(dataPath, "knownCertificates.ks");
		return new FileOutputStream(kcFile);
	}
	protected boolean askPermissionToAccept(String hostName, X509Certificate[] chain) throws CertificateEncodingException {
		if (this.interactive) {
			int choice = JOptionPane.showConfirmDialog(null, new JLabel(this.buildAskPermissionMessage(chain)), ("Allow Connecting to " + hostName + " with these certificates?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			return (choice == JOptionPane.YES_OPTION);
		}
		else return this.acceptUnknown;
	}
	
	/**
	 * Build the message for the <code>askPermissionToAccept()</code> method to
	 * display to a user asking permission to accept a given certificate chain.
	 * Overwrite this method to change the appearance of the message; call this
	 * method from code overwriting <code>askPermissionToAccept()</code> to get
	 * the default message and display it by other means.
	 * @param chain the certificate chain to ask permission to accept
	 * @return the message asking for permission
	 * @throws CertificateEncodingException
	 */
	protected String buildAskPermissionMessage(X509Certificate[] chain) throws CertificateEncodingException {
		StringBuffer message = new StringBuffer("<HTML>");
		for (int c = 0; c < chain.length; c++) {
			if (c != 0)
				message.append("<BR/>");
			message.append("<B>" + chain[c].getSubjectX500Principal().getName() + "</B>");
			message.append("<BR/>");
			message.append("<I>" + chain[c].getIssuerX500Principal().getName() + "</I>");
			message.append("<BR/>");
			message.append("SHA1: <TT>" + HashUtils.getSha1(chain[c].getEncoded()) + "</TT>");
			message.append("<BR/>");
			message.append("MD5: <TT>" + HashUtils.getMd5(chain[c].getEncoded()) + "</TT>");
			message.append("<BR/>");
		}
		message.append("</HTML>");
		return message.toString();
	}
//	
//	static String getSha1(X509Certificate cert) throws CertificateEncodingException {
//		MessageDigest sha1 = null;
//		try {
//			sha1 = getSha1();
//			sha1.update(cert.getEncoded());
//			return String.valueOf(RandomByteSource.getHexCode(sha1.digest()));
//		}
//		finally {
//			returnSha1(sha1);
//		}
//	}
//	
//	static String getMd5(X509Certificate cert) throws CertificateEncodingException {
//		MessageDigest md5 = null;
//		try {
//			md5 = getMd5();
//			md5.update(cert.getEncoded());
//			return String.valueOf(RandomByteSource.getHexCode(md5.digest()));
//		}
//		finally {
//			returnMd5(md5);
//		}
//	}
//	
//	private static LinkedList poolMd5 = new LinkedList();
//	private static synchronized MessageDigest getMd5() {
//		if (poolMd5.size() != 0) {
//			MessageDigest dataHash = ((MessageDigest) poolMd5.removeFirst());
//			dataHash.reset();
//			return dataHash;
//		}
//		try {
//			MessageDigest dataHash = MessageDigest.getInstance("MD5");
//			dataHash.reset();
//			return dataHash;
//		}
//		catch (NoSuchAlgorithmException nsae) {
//			System.out.println(nsae.getClass().getName() + " (" + nsae.getMessage() + ") while creating checksum digester.");
//			nsae.printStackTrace(System.out); // should not happen, but Java don't know ...
//			return null;
//		}
//	}
//	private static synchronized void returnMd5(MessageDigest dataHash) {
//		if (dataHash != null)
//			poolMd5.addLast(dataHash);
//	}
//	
//	private static LinkedList poolSha1 = new LinkedList();
//	private static synchronized MessageDigest getSha1() {
//		if (poolSha1.size() != 0) {
//			MessageDigest dataHash = ((MessageDigest) poolSha1.removeFirst());
//			dataHash.reset();
//			return dataHash;
//		}
//		try {
//			MessageDigest dataHash = MessageDigest.getInstance("SHA1");
//			dataHash.reset();
//			return dataHash;
//		}
//		catch (NoSuchAlgorithmException nsae) {
//			System.out.println(nsae.getClass().getName() + " (" + nsae.getMessage() + ") while creating checksum digester.");
//			nsae.printStackTrace(System.out); // should not happen, but Java don't know ...
//			return null;
//		}
//	}
//	private static synchronized void returnSha1(MessageDigest dataHash) {
//		if (dataHash != null)
//			poolSha1.addLast(dataHash);
//	}
}
