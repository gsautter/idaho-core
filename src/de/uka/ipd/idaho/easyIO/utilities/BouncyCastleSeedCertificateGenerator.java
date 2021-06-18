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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import de.uka.ipd.idaho.easyIO.utilities.AbstractHttpsEnabler.SeedCertificateGenerator;

/**
 * BouncyCastle based implementation of a seed certificate generator, using
 * classes from that library.
 * 
 * @author sautter
 */
public class BouncyCastleSeedCertificateGenerator extends SeedCertificateGenerator {
	BouncyCastleSeedCertificateGenerator() {}
	X509Certificate createSeedCertificate(PublicKey pubKey, PrivateKey privKey) throws Exception {
		
		//	fill in certificate fields
		X500Name owner = new X500Name("CN=ApplicationPrivateSeedKey");
		Date from = new Date();
		Date to = new Date(from.getTime() + (3650L * 86400000L));
		BigInteger serialNumber = new BigInteger(64, new SecureRandom());
		X509v3CertificateBuilder certificate = new JcaX509v3CertificateBuilder(owner, serialNumber, from, to, owner, pubKey);
		try {
			certificate.addExtension(Extension.subjectKeyIdentifier, false, serialNumber.toByteArray());
			certificate.addExtension(Extension.authorityKeyIdentifier, false, serialNumber.toByteArray());
			BasicConstraints constraints = new BasicConstraints(true);
			certificate.addExtension(
			    Extension.basicConstraints,
			    true,
			    constraints.getEncoded());
			KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature);
			certificate.addExtension(Extension.keyUsage, false, usage.getEncoded());
			ExtendedKeyUsage usageEx = new ExtendedKeyUsage(new KeyPurposeId[] {
			    KeyPurposeId.id_kp_serverAuth,
			    KeyPurposeId.id_kp_clientAuth
			});
			certificate.addExtension(
			    Extension.extendedKeyUsage,
			    false,
			    usageEx.getEncoded());
		}
		catch (CertIOException cie) {
			System.out.println("Could not create certificate: " + cie.getMessage());
			cie.printStackTrace(System.out);
			throw new IOException(cie);
		}
		
		//	build BouncyCastle certificate
		X509CertificateHolder holder;
		try {
			ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").build(privKey);
			holder = certificate.build(signer);
		}
		catch (OperatorCreationException oce) {
			System.out.println("Could not create certificate: " + oce.getMessage());
			oce.printStackTrace(System.out);
			throw new IOException(oce);
		}
		
		//	convert to JRE certificate
		try {
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
			converter.setProvider(new BouncyCastleProvider());
			return converter.getCertificate(holder);
		}
		catch (CertificateException ce) {
			System.out.println("Could not sign certificate: " + ce.getMessage());
			ce.printStackTrace(System.out);
			throw new IOException(ce);
		}
	}
//	
//	public static void main(String[] args) throws Exception {
//		BouncyCastleSeedCertificateGenerator bcscg = new BouncyCastleSeedCertificateGenerator();
//		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // supported by specification
//		keyGen.initialize(1024, null);
//		KeyPair keyPair = keyGen.generateKeyPair();
//		PublicKey pubKey = keyPair.getPublic();
//		PrivateKey privKey = keyPair.getPrivate();
//		X509Certificate cert = bcscg.createSeedCertificate(pubKey, privKey);
//		System.out.println(cert);
//		System.out.println(Arrays.toString(cert.getEncoded()));
//	}
}
