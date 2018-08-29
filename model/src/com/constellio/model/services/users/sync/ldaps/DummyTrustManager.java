package com.constellio.model.services.users.sync.ldaps;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * http://stackoverflow.com/questions/4615163/how-to-accept-self-signed-certificates-for-jndi-ldap-connections/4829055#4829055
 */
public class DummyTrustManager implements X509TrustManager {
	public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		// do nothing
	}

	public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		// do nothing
	}

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
