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

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
import de.uka.ipd.idaho.easyIO.utilities.AbstractHttpsEnabler.SeedCertificateGenerator;

/**
 * Sun JDK based implementation of a seed certificate generator, using JRE
 * classes.
 * 
 * @author sautter
 */
public class SunJdkSeedCertificateGenerator extends SeedCertificateGenerator {
	SunJdkSeedCertificateGenerator() {}
	X509Certificate createSeedCertificate(PublicKey pubKey, PrivateKey privKey) throws Exception {
		
		//	get Java version
		String javaVersionStr = System.getProperty("java.version", "");
		String[] javaVersion = javaVersionStr.split("\\.");
		boolean isBelowJava8 = ((javaVersion.length < 2) || ((Integer.parseInt(javaVersion[0]) == 1) && (Integer.parseInt(javaVersion[1]) < 8)));
		
		//	assemble certificate info
		X509CertInfo info = new X509CertInfo();
		AlgorithmId algorithmId = new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid);
		try {
			Date from = new Date();
			Date to = new Date(from.getTime() + (3650L * 86400000L));
			CertificateValidity interval = new CertificateValidity(from, to);
			BigInteger serialNumber = new BigInteger(64, new SecureRandom());
			X500Name owner = new X500Name("CN=ApplicationPrivateSeedKey");
			info.set(X509CertInfo.VALIDITY, interval);
			info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber));
			if (isBelowJava8) {
				info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
				info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
			}
			else {
				info.set(X509CertInfo.SUBJECT, owner);
				info.set(X509CertInfo.ISSUER, owner);
			}
			info.set(X509CertInfo.KEY, new CertificateX509Key(pubKey));
			info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
			info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmId));
		}
		catch (CertificateException ce) {
			System.out.println("Could not create certificate: " + ce.getMessage());
			ce.printStackTrace(System.out);
			throw new IOException(ce);
		}
		
		//	sign the certificate to identify the algorithm that's used
		X509CertImpl cert = new X509CertImpl(info);
		try {
			cert.sign(privKey, algorithmId.getName());
			
			//	update the algorithm, and resign
			algorithmId = ((AlgorithmId) cert.get(X509CertImpl.SIG_ALG));
			info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithmId);
			cert = new X509CertImpl(info);
			cert.sign(privKey, algorithmId.getName());
		}
		catch (InvalidKeyException ike) {
			System.out.println("Could not sign certificate: " + ike.getMessage());
			ike.printStackTrace(System.out);
			throw new IOException(ike);
		}
		catch (CertificateException ce) {
			System.out.println("Could not sign certificate: " + ce.getMessage());
			ce.printStackTrace(System.out);
			throw new IOException(ce);
		}
		catch (NoSuchAlgorithmException nsae) {
			System.out.println("Could not sign certificate: " + nsae.getMessage());
			nsae.printStackTrace(System.out);
			throw new IOException(nsae);
		}
		catch (NoSuchProviderException nspe) {
			System.out.println("Could not sign certificate: " + nspe.getMessage());
			nspe.printStackTrace(System.out);
			throw new IOException(nspe);
		}
		catch (SignatureException se) {
			System.out.println("Could not sign certificate: " + se.getMessage());
			se.printStackTrace(System.out);
			throw new IOException(se);
		}
		
		//	finally ...
		return cert;
	}
//	
//	public static void main(String[] args) throws Exception {
//		SunJdkSeedCertificateGenerator sjscg = new SunJdkSeedCertificateGenerator();
//		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // supported by specification
//		keyGen.initialize(1024, null);
//		KeyPair keyPair = keyGen.generateKeyPair();
//		PublicKey pubKey = keyPair.getPublic();
//		PrivateKey privKey = keyPair.getPrivate();
//		System.out.println(sjscg.createSeedCertificate(pubKey, privKey));
//	}
}
