/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.client.cmis.client;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class CmisSessionBuilder {

	private String appUrl;
	private String user;
	private String password;
	private String collection;
	private String loginAs;
	private boolean cacheEnabled = false;

	public static CmisSessionBuilder forAppUrl(String appUrl) {
		CmisSessionBuilder builder = new CmisSessionBuilder();
		builder.appUrl = appUrl.replace("/atom", "");
		return builder;
	}

	public static CmisSessionBuilder forLogin(String appUrl, String user, String password, String collection) {
		CmisSessionBuilder builder = new CmisSessionBuilder();
		builder.appUrl = appUrl.replace("/atom", "");
		builder.user = user;
		builder.password = password;
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

	public Session build() {
		if (user == null || appUrl == null || password == null || collection == null) {
			throw new RuntimeException("user, appUrl, password and collection parameters required");
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		if (loginAs == null) {
			parameter.put(SessionParameter.USER, user);
		} else {
			parameter.put(SessionParameter.USER, user + "=>" + loginAs);
		}
		parameter.put(SessionParameter.PASSWORD, password);

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

	public CmisSessionBuilder authenticatedBy(String user, String password) {
		this.user = user;
		this.password = password;
		return this;
	}

	public CmisSessionBuilder logedAs(String loginAs) {
		this.loginAs = loginAs;
		return this;
	}

	public CmisSessionBuilder onCollection(String collection) {
		this.collection = collection;
		return this;
	}

}
