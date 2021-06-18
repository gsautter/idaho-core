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
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Abstract implementation of an HTTPS enabler. This class handles all aspects
 * of integrating with the JRE it is running in. Sub classes only have to take
 * care of key store IO and user interaction.
 * 
 * @author sautter
 */
public abstract class AbstractHttpsEnabler extends SSLSocketFactory implements X509TrustManager {
	private static final String KEY_STORE_PASS = "whyCareItsOurOwn";
	private static ThreadLocal currentHost = new ThreadLocal();
	
	private boolean useKeyStore;
	private KeyStore keyStore;
	private X509TrustManager keyStoreTrustManager = null;
	private SSLSocketFactory sslSocketFactory;
	
	/** Constructor
	 * @param useKeyStore use a key store to persist trusted certificates?
	 */
	protected AbstractHttpsEnabler(boolean useKeyStore) {
		this.useKeyStore = useKeyStore;
	}
	
	/**
	 * Initialize the HTTPS enabler. This method does two things: First, it
	 * initializes the key store if the constructor flag indicates to use one.
	 * Second, it registers an HTTPS socket factory using the instance proper
	 * as its trust manager. If key store usage is switched on, but a key store
	 * cannot be loaded (or generated and initialized), this method throws an
	 * exception and does not register the socket factory.
	 * @throws IOException
	 */
	public void init() throws IOException {
		this.init(null);
	}
	
	/**
	 * Initialize the HTTPS enabler. This method does two things: First, it
	 * initializes the key store if the constructor flag indicates to use one.
	 * Second, it registers an HTTPS socket factory using the instance proper
	 * as its trust manager. If key store usage is switched on, but a key store
	 * cannot be loaded (or generated and initialized), this method throws an
	 * exception and does not register the socket factory. If the argument
	 * protocol is null, it defaults to 'TLSv1.2'.
	 * @param protocol the protocol to use
	 * @throws IOException
	 */
	public void init(String protocol) throws IOException {
//		
//		//	install BouncyCastle (only if we're below Java 1.8)
//		//	==> needs to be loaded from root class loader in Java 1.6
//		//	==> causes strange access violation with Java 1.6, even with plain HTTP
//		//	==> appears to not be required in Java 1.7, as HTTPS works without this there
//		String javaVersionStr = System.getProperty("java.version", "");
//		System.out.println("Java version is " + javaVersionStr);
//		String[] javaVersion = javaVersionStr.split("\\.");
//		if ((javaVersion.length < 2) || ((Integer.parseInt(javaVersion[0]) == 1) && (Integer.parseInt(javaVersion[1]) < 8))) try {
////			Security.insertProviderAt(new BouncyCastleProvider(), 1);
//			Class bcpClass = this.getClass().getClassLoader().loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
//			int bcpPos = Security.insertProviderAt(((Provider) bcpClass.newInstance()), 1);
//			System.out.println("BouncyCastle cryptography provider installed at position " + bcpPos);
//		}
//		catch (Throwable t) {
//			System.out.println("Could not add BouncyCastle cryptography provider: " + t.getMessage());
//			t.printStackTrace(System.out);
//		}
		
		//	load key store
		System.out.println("Creating key store ...");
		try {
			InputStream ksIn = this.getKeyStoreInputStream();
			
			//	we don't have a key store ...
			if (ksIn == null) {
				
				//	... but just because we didn't create it yet
				if (this.useKeyStore) {
					this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
					
					//	try to create seed certificate to populate new key store with
					X509Certificate seedCert;
					try {
						seedCert = createSeedCertificate();
						if (seedCert == null)
							System.out.println("Could not create seed certificate");
						else System.out.println("Seed certificate created");
					}
					catch (Throwable t) {
						System.out.println("Could not create seed certificate: " + t.getMessage());
						t.printStackTrace(System.out);
						seedCert = null;
						System.out.println("Falling back to pre-seeded packaged model key store");
					}
					
					//	failed to generate seed certificate (happens with non-Sun JVM ...)
					if (seedCert == null) {
						
						//	get pre-seeded packaged dummy ...
						String preSeededKeyStoreResName = AbstractHttpsEnabler.class.getName();
						preSeededKeyStoreResName = preSeededKeyStoreResName.substring(0, preSeededKeyStoreResName.lastIndexOf('.'));
						preSeededKeyStoreResName = preSeededKeyStoreResName.replaceAll("\\.", "/");
						preSeededKeyStoreResName = (preSeededKeyStoreResName + "/" + "preSeededKeyStore.ks");
						ksIn = AbstractHttpsEnabler.class.getClassLoader().getResourceAsStream(preSeededKeyStoreResName);
						
						//	... and use it to initialize key store
						this.keyStore.load(ksIn, KEY_STORE_PASS.toCharArray());
						ksIn.close();
						System.out.println("Initialized new key store from pre-seeded packaged model key store");
					}
					
					//	we have a seed certificate, create new key store and use it
					else {
						this.keyStore.load(ksIn, null);
						System.out.println("Created new key store");
						
						//	populate with seed certificate (empty key store will cause error)
						this.keyStore.setCertificateEntry("ApplicationPrivateSeedCertificate", seedCert);
						System.out.println("Added seed certificate");
					}
					
					//	persist new key store
					this.persistKeyStore();
					System.out.println("New key store persisted");
				}
			}
			
			//	load existing key store
			else {
				this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				this.keyStore.load(ksIn, KEY_STORE_PASS.toCharArray());
				ksIn.close();
				System.out.println("Restored key store");
			}
		}
		catch (KeyStoreException kse) {
			System.out.println("Could not create key store: " + kse.getMessage());
			kse.printStackTrace(System.out);
			throw new IOException(kse);
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not create key store: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
			throw new IOException(nsae);
		}
		catch (CertificateException ce) {
			System.out.println("Could not create key store: " + ce.getMessage());
			ce.printStackTrace(System.out);
			throw new IOException(ce);
		}
		
		//	initialize default trust manager with the keys we have (if any)
		if (this.keyStore != null) try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(this.keyStore);
			this.keyStoreTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not create trust manager: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
			throw new IOException(nsae);
		}
		catch (KeyStoreException kse) {
			System.out.println("Could not create trust manager: " + kse.getMessage());
			kse.printStackTrace(System.out);
			throw new IOException(kse);
		}
		
		//	create trust manager and SSL socket factory
		try {
			TrustManager[] tms = {this};
			SSLContext sslContext = SSLContext.getInstance((protocol == null) ? "TLSv1.2" : protocol);
			sslContext.init(null, tms, null);
			this.sslSocketFactory = sslContext.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(this);
		}
		catch (KeyManagementException kme) {
			System.out.println("Could not initialize SSL socket factory: " + kme.getMessage());
			kme.printStackTrace(System.out);
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not initialize SSL socket factory: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
		}
	}
	
	//	method code courtesy https://www.mayrhofer.eu.org/create-x509-certs-in-java
	private static X509Certificate createSeedCertificate() throws IOException {
		
		//	get certificate generator
		SeedCertificateGenerator seedCertGen;
		try {
			seedCertGen = getSeedCertificateGenerator();
			if (seedCertGen == null) {
				System.out.println("Could not create seed certificate generator");
				return null;
			}
			System.out.println("Got seed certificate generator: " + seedCertGen.getClass().getName());
		}
		catch (ClassNotFoundException cnfe) {
			System.out.println("Could not create seed certificate generator: " + cnfe.getMessage());
			cnfe.printStackTrace(System.out);
			throw new IOException(cnfe);
		}
		catch (NoClassDefFoundError ncdfe) {
			System.out.println("Could not create seed certificate generator: " + ncdfe.getMessage());
			ncdfe.printStackTrace(System.out);
			throw new IOException(ncdfe);
		}
		catch (Exception e) {
			System.out.println("Could not create seed certificate generator: " + e.getMessage());
			e.printStackTrace(System.out);
			throw new IOException(e);
		}
		
		//	generate key pair
		PublicKey pubKey;
		PrivateKey privKey;
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // supported by specification
			keyGen.initialize(1024, null);
			KeyPair keyPair = keyGen.generateKeyPair();
			privKey = keyPair.getPrivate();
			pubKey = keyPair.getPublic();
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not create key pair: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
			throw new IOException(nsae);
		}
		
		//	create certificate under heavy guard (this will throw all sorts of exceptions in non-Sun JVM)
		try {
			return seedCertGen.createSeedCertificate(pubKey, privKey);
		}
		catch (Exception e) {
			System.out.println("Could not create seed certificate: " + e.getMessage());
			e.printStackTrace(System.out);
			throw new IOException(e);
		}
	}
	
	static abstract class SeedCertificateGenerator {
		abstract X509Certificate createSeedCertificate(PublicKey pubKey, PrivateKey privKey) throws Exception;
	}
	
	private static String[] seedCertificateGeneratorClassNames = {
		"de.uka.ipd.idaho.easyIO.utilities.SunJdkSeedCertificateGenerator",
		"de.uka.ipd.idaho.easyIO.utilities.BouncyCastleSeedCertificateGenerator",
		"de.uka.ipd.idaho.easyIO.utilities.DefaultSeedCertificateGenerator",
	};
	
	//	we need this kind of wild construct to make build independent of JDK version or cryptography library installed
	private static SeedCertificateGenerator getSeedCertificateGenerator() throws Exception {
		for (int cn = 0; cn < seedCertificateGeneratorClassNames.length; cn++) try {
			Class seedCertGenClass = Class.forName(seedCertificateGeneratorClassNames[cn]);
			return ((SeedCertificateGenerator) seedCertGenClass.newInstance());
		}
		catch (ClassNotFoundException cnfe) {
			System.out.println("Could not create certificate generator: " + cnfe.getMessage());
			cnfe.printStackTrace(System.out);
//			throw cnfe;
		}
		catch (InstantiationException ie) {
			System.out.println("Could not create certificate generator: " + ie.getMessage());
			ie.printStackTrace(System.out);
//			throw ie;
		}
		catch (IllegalAccessException iae) {
			System.out.println("Could not create certificate generator: " + iae.getMessage());
			iae.printStackTrace(System.out);
//			throw iae;
		}
		catch (Exception e) {
			System.out.println("Could not create certificate generator: " + e.getMessage());
			e.printStackTrace(System.out);
//			throw e;
		}
		return null;
	}
//	
//	private static X509Certificate createSeedCertificate(PublicKey pubKey, PrivateKey privKey) throws IOException, ClassNotFoundException {
//		
//		//	get Java version
//		String javaVersionStr = System.getProperty("java.version", "");
//		String[] javaVersion = javaVersionStr.split("\\.");
//		boolean isBelowJava8 = ((javaVersion.length < 2) || ((Integer.parseInt(javaVersion[0]) == 1) && (Integer.parseInt(javaVersion[1]) < 8)));
//		
//		//	assemble certificate info
//		X509CertInfo info = new X509CertInfo();
//		AlgorithmId algorithmId = new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid);
//		try {
//			Date from = new Date();
//			Date to = new Date(from.getTime() + (3650 * 86400000));
//			CertificateValidity interval = new CertificateValidity(from, to);
//			BigInteger serialNumber = new BigInteger(64, new SecureRandom());
//			X500Name owner = new X500Name("CN=ApplicationPrivateSeedKey");
//			info.set(X509CertInfo.VALIDITY, interval);
//			info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber));
//			if (isBelowJava8) {
//				info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
//				info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
//			}
//			else {
//				info.set(X509CertInfo.SUBJECT, owner);
//				info.set(X509CertInfo.ISSUER, owner);
//			}
//			info.set(X509CertInfo.KEY, new CertificateX509Key(pubKey));
//			info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
//			info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmId));
//		}
//		catch (CertificateException ce) {
//			System.out.println("Could not create certificate: " + ce.getMessage());
//			ce.printStackTrace(System.out);
//			throw new IOException(ce);
//		}
//		
//		//	sign the certificate to identify the algorithm that's used
//		X509CertImpl cert = new X509CertImpl(info);
//		try {
//			cert.sign(privKey, algorithmId.getName());
//			
//			//	update the algorithm, and resign
//			algorithmId = ((AlgorithmId) cert.get(X509CertImpl.SIG_ALG));
//			info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithmId);
//			cert = new X509CertImpl(info);
//			cert.sign(privKey, algorithmId.getName());
//		}
//		catch (InvalidKeyException ike) {
//			System.out.println("Could not sign certificate: " + ike.getMessage());
//			ike.printStackTrace(System.out);
//			throw new IOException(ike);
//		}
//		catch (CertificateException ce) {
//			System.out.println("Could not sign certificate: " + ce.getMessage());
//			ce.printStackTrace(System.out);
//			throw new IOException(ce);
//		}
//		catch (NoSuchAlgorithmException nsae) {
//			System.out.println("Could not sign certificate: " + nsae.getMessage());
//			nsae.printStackTrace(System.out);
//			throw new IOException(nsae);
//		}
//		catch (NoSuchProviderException nspe) {
//			System.out.println("Could not sign certificate: " + nspe.getMessage());
//			nspe.printStackTrace(System.out);
//			throw new IOException(nspe);
//		}
//		catch (SignatureException se) {
//			System.out.println("Could not sign certificate: " + se.getMessage());
//			se.printStackTrace(System.out);
//			throw new IOException(se);
//		}
//		
//		//	finally ...
//		return cert;
//	}
	
	/**
	 * Get an input stream for loading a previously created key store from,
	 * depending upon the backing storage. This method may return null only if
	 * the implementing class decides to work without a key store (as to be
	 * indicated to the constructor), or if the key store has not been created
	 * yet in a new installation.
	 * @return an input stream to load the key store from
	 * @throws IOException
	 */
	protected abstract InputStream getKeyStoreInputStream() throws IOException;
	
	/**
	 * Get an output stream for storing a key store. This method may only
	 * return null if the implementing class decides to work without a key
	 * store, or if no writable underlying storage is available to do persist
	 * it. In the latter case, trust information is lost when the JVM dies.
	 * @return an output stream to store the key store to
	 * @throws IOException
	 */
	protected abstract OutputStream getKeyStoreOutputStream() throws IOException;
	
	/**
	 * Ask the user whether or not to accept a thus far unknown certificate
	 * chain. If this method returns true, the argument chain is added to the
	 * underlying key store.
	 * @param hostName the host name the certificate chain comes from
	 * @param chain the certificate chain in question.
	 * @return a boolean indicating whether or not to accept the chain
	 * @throws CertificateEncodingException
	 */
	protected abstract boolean askPermissionToAccept(String hostName, X509Certificate[] chain) throws CertificateEncodingException;
	
	public String[] getDefaultCipherSuites() {
		return this.sslSocketFactory.getDefaultCipherSuites();
	}
	
	public String[] getSupportedCipherSuites() {
		return this.sslSocketFactory.getSupportedCipherSuites();
	}
	public Socket createSocket(Socket sock, String host, int port, boolean autoClose) throws IOException {
		try {
			currentHost.set(host);
			return this.sslSocketFactory.createSocket(sock, host, port, autoClose);
		}
		catch (IOException ioe) {
			currentHost.remove();
			throw ioe;
		}
	}
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		try {
			currentHost.set(address.getHostName());
			return this.sslSocketFactory.createSocket(address, port, localAddress, localPort);
		}
		catch (IOException ioe) {
			currentHost.remove();
			throw ioe;
		}
	}
	public Socket createSocket(InetAddress address, int port) throws IOException {
		try {
			currentHost.set(address.getHostName());
			return this.sslSocketFactory.createSocket(address, port);
		}
		catch (IOException ioe) {
			currentHost.remove();
			throw ioe;
		}
	}
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		try {
			currentHost.set(host);
			return this.sslSocketFactory.createSocket(host, port, localHost, localPort);
		}
		catch (IOException ioe) {
			currentHost.remove();
			throw ioe;
		}
	}
	public Socket createSocket(String host, int port) throws IOException {
		try {
			currentHost.set(host);
			return this.sslSocketFactory.createSocket(host, port);
		}
		catch (IOException ioe) {
			currentHost.remove();
			throw ioe;
		}
	}
	
	private void persistKeyStore() {
		try {
			OutputStream ksOut = this.getKeyStoreOutputStream();
			if (ksOut == null)
				return;
			this.keyStore.store(ksOut, KEY_STORE_PASS.toCharArray());
			ksOut.close();
		}
		catch (KeyStoreException kse) {
			System.out.println("Could not persist key store: " + kse.getMessage());
			kse.printStackTrace(System.out);
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not persist key store: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
		}
		catch (CertificateException ce) {
			System.out.println("Could not persist key store: " + ce.getMessage());
			ce.printStackTrace(System.out);
		}
		catch (IOException ioe) {
			System.out.println("Could not persist key store: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0]; // see http://infposs.blogspot.kr/2013/06/installcert-and-java-7.html
	}
	
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new UnsupportedOperationException();
	}
	
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		String currentHost = ((String) AbstractHttpsEnabler.currentHost.get());
		AbstractHttpsEnabler.currentHost.remove();
		
		//	check with key store first
		if (this.keyStoreTrustManager != null) try {
			this.keyStoreTrustManager.checkServerTrusted(chain, authType);
			return;
		} catch (CertificateException ce) {}
		
		//	double-check with user
		if (this.askPermissionToAccept(currentHost, chain))
			this.addCertificates(currentHost, chain);
		
		//	we don't accept this certificate chain
		else throw new CertificateException("Untrusted certificate " + chain[0].getSubjectX500Principal().getName());
	}
	
	void addCertificates(String hostName, X509Certificate[] chain) throws CertificateEncodingException {
		
		//	do we have anything to add them to?
		if (this.keyStore == null)
			return;
		
		//	store certificates
		for (int c = 0; c < chain.length; c++) try {
			this.keyStore.setCertificateEntry((hostName + "-" + c), chain[c]);
		}
		catch (KeyStoreException kse) {
			System.out.println("Could not store certificate: " + kse.getMessage());
			kse.printStackTrace(System.out);
		}
		
		//	store known certificates
		this.persistKeyStore();
		
		//	update trust manager
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(this.keyStore);
			this.keyStoreTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not refresh trust manager: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
		}
		catch (KeyStoreException kse) {
			System.out.println("Could not refresh trust manager: " + kse.getMessage());
			kse.printStackTrace(System.out);
		}
	}
	
	/**
	 * Retrieve the aliases the keystore currently holds certificates for.
	 * @return an array holding the aliases
	 */
	public String[] getAliases() throws KeyStoreException {
		if (this.keyStore == null)
			return null;
		ArrayList aliases = new ArrayList();
		for (Enumeration ae = this.keyStore.aliases(); ae.hasMoreElements();)
			aliases.add(ae.nextElement());
		return ((String[]) aliases.toArray(new String[aliases.size()]));
	}
	
	/**
	 * Retrieve the certificate for a given alias.
	 * @param alias the alias to get the certificate for
	 * @return the certificate for the argument alias
	 */
	public X509Certificate getCertificate(String alias) throws KeyStoreException {
		if (this.keyStore == null)
			return null;
		return ((X509Certificate) this.keyStore.getCertificate(alias));
	}
	
	/**
	 * Retrieve the certificate for a given alias.
	 * @param alias the alias to get the certificate for
	 * @return the certificate for the argument alias
	 */
	public X509Certificate[] getCertificateChain(String alias) throws KeyStoreException {
		if (this.keyStore == null)
			return null;
		Certificate[] rawChain = this.keyStore.getCertificateChain(alias);
		if (rawChain == null)
			return new X509Certificate[0];
		X509Certificate[] chain = new X509Certificate[rawChain.length];
		for (int c = 0; c < rawChain.length; c++)
			chain[c] = ((X509Certificate) rawChain[c]);
		return chain;
	}
	
	/**
	 * Retrieve the certificates currently in the keystore.
	 * @return an array holding the certificates
	 */
	public X509Certificate[] getCertificates() throws KeyStoreException {
		if (this.keyStore == null)
			return null;
		String[] aliases = this.getAliases();
		X509Certificate[] certificates = new X509Certificate[aliases.length];
		for (int a = 0; a < aliases.length; a++)
			certificates[a] = this.getCertificate(aliases[a]);
		return certificates;
	}
	
	/**
	 * Retrieve the certificate chains currently in the keystore.
	 * @return an array holding the certificates
	 */
	public X509Certificate[][] getCertificateChains() throws KeyStoreException {
		if (this.keyStore == null)
			return null;
		String[] aliases = this.getAliases();
		X509Certificate[][] chains = new X509Certificate[aliases.length][];
		for (int a = 0; a < aliases.length; a++)
			chains[a] = this.getCertificateChain(aliases[a]);
		return chains;
	}
	
	/**
	 * Generally enable HTTPS in a JVM by installing a trust-all certificate
	 * manager. This method is mainly intended as a helper in test code, to
	 * save creating such a certificate manager there. It should be used with
	 * extreme care in other settings.
	 * @throws IOException
	 */
	public static void enableHttps() throws IOException {
		enableHttps(null);
	}
	
	/**
	 * Generally enable HTTPS in a JVM by installing a trust-all certificate
	 * manager. This method is mainly intended as a helper in test code, to
	 * save creating such a certificate manager there. It should be used with
	 * extreme care in other settings.
	 * @param protocol the protocol to use
	 * @throws IOException
	 */
	public static void enableHttps(String protocol) throws IOException {
		AbstractHttpsEnabler https = new AbstractHttpsEnabler(false) {
			protected OutputStream getKeyStoreOutputStream() throws IOException {
				return null;
			}
			protected InputStream getKeyStoreInputStream() throws IOException {
				return null;
			}
			protected boolean askPermissionToAccept(String hostName, X509Certificate[] chain) throws CertificateEncodingException {
				return true;
			}
		};
		https.init(protocol);
	}
	
	public static void main(String[] args) throws Exception {
		AbstractHttpsEnabler ahe = new AbstractHttpsEnabler(true) {
			protected InputStream getKeyStoreInputStream() throws IOException {
				File ks = new File("E:/GoldenGATEv3/Temp/KeyStoreTest");
				return ((ks.exists() ? new FileInputStream(ks) : null));
			}
			protected OutputStream getKeyStoreOutputStream() throws IOException {
				File ks = new File("E:/GoldenGATEv3/Temp/KeyStoreTest");
				return new FileOutputStream(ks);
			}
			protected boolean askPermissionToAccept(String hostName, X509Certificate[] chain) throws CertificateEncodingException {
				StringBuffer message = new StringBuffer("<HTML>");
				for (int c = 0; c < chain.length; c++) {
					if (c != 0)
						message.append("<BR/>");
					message.append("<B>" + chain[c].getSubjectX500Principal().getName() + "</B>");
					message.append("<BR/>");
					message.append("<I>" + chain[c].getIssuerX500Principal().getName() + "</I>");
					message.append("<BR/>");
				}
				message.append("</HTML>");
				int choice = JOptionPane.showConfirmDialog(null, new JLabel(message.toString()), ("Allow Connecting to " + hostName + " with these certificates?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				return (choice == JOptionPane.YES_OPTION);
			}
		};
		ahe.init();
		System.out.println(Arrays.toString(ahe.getAliases()));
		
		SocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
//		SSLSocket socket = (SSLSocket) factory.createSocket("srv1.plazi.de", 443);
		SSLSocket socket = (SSLSocket) factory.createSocket("github.com", 443);
		socket.setSoTimeout(10000);
		System.out.println("Starting SSL handshake ...");
		socket.startHandshake();
		socket.close();
		System.out.println();
		System.out.println("SSL handshake successful");
	}
}
