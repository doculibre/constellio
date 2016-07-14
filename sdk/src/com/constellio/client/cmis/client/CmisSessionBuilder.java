package com.constellio.client.cmis.client;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class CmisSessionBuilder {

	private String appUrl;
	private String serviceKey;
	private String token;
	private String collection;
	private boolean cacheEnabled = false;

	public static CmisSessionBuilder forAppUrl(String appUrl) {
		CmisSessionBuilder builder = new CmisSessionBuilder();
		builder.appUrl = appUrl.replace("/atom", "");
		return builder;
	}

	public static CmisSessionBuilder forLogin(String appUrl, String serviceKey, String token, String collection) {
		CmisSessionBuilder builder = new CmisSessionBuilder();
		builder.appUrl = appUrl.replace("/atom", "");
		builder.serviceKey = serviceKey;
		builder.token = token;
		builder.collection = collection;
		return builder;
	}

	private static void acceptSelfSignedCertificates() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
	}

	public CmisSessionBuilder withEnabledCache() {
		this.cacheEnabled = true;
		return this;
	}

	public List<Repository> getRepositories() {
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// serviceKey credentials
		parameter.put(SessionParameter.USER, serviceKey);
		parameter.put(SessionParameter.PASSWORD, token);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, appUrl + (appUrl.endsWith("/") ? "" : "/") + "atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, collection);

		return factory.getRepositories(parameter);
	}

	public Session build() {
		if (serviceKey == null || appUrl == null || token == null || collection == null) {
			throw new RuntimeException("serviceKey, appUrl, token and collection parameters required");
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// serviceKey credentials
		parameter.put(SessionParameter.USER, serviceKey);
		parameter.put(SessionParameter.PASSWORD, token);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, appUrl + (appUrl.endsWith("/") ? "" : "/") + "atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, collection);

		acceptSelfSignedCertificates();
		Session session = factory.createSession(parameter);
		session.getDefaultContext().setMaxItemsPerPage(100000);
		session.getDefaultContext().setFilterString("*");
		session.getDefaultContext().setCacheEnabled(cacheEnabled);
		// Include every properties
		session.getDefaultContext().setRenditionFilterString("*");
		return session;
	}

	public CmisSessionBuilder authenticatedBy(String serviceKey, String token) {
		this.serviceKey = serviceKey;
		this.token = token;
		return this;
	}

	public CmisSessionBuilder onCollection(String collection) {
		this.collection = collection;
		return this;
	}

}
