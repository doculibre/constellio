package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceRuntimeException_HttpError;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceRuntimeException_IOException;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceRuntimeException_MalformedUrl;
import com.constellio.app.modules.es.connectors.http.fetcher.URLFetchingServiceRuntimeException.URLFetchingServiceServiceRuntimeException_ConnectionException;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPServicesImpl;
import com.constellio.app.modules.es.model.connectors.AuthenticationScheme;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.entities.records.Record;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class ConnectorUtil {
	private static final Logger LOGGER = LogManager.getLogger(ConnectorLDAP.class);

	public enum ConnectionStatus {
		onlyOneUrlSupported,
		authentificationFailure,
		ioError,
		httpStatusError,
		connectionError,
		badUrl,
		badUrlNoParam,
		Ok,
	}

	static class ConnectionStatusResult {
		Object param;
		ConnectionStatus connectionStatus;

		public ConnectionStatusResult(ConnectionStatus connectionStatus) {
			this.connectionStatus = connectionStatus;
		}

		public ConnectionStatusResult(Object param,
				ConnectionStatus connectionStatus) {
			this.param = param;
			this.connectionStatus = connectionStatus;
		}

		public Object getParam() {
			return param;
		}

		public void setParam(Object param) {
			this.param = param;
		}

		public ConnectionStatus getConnectionStatus() {
			return connectionStatus;
		}

		public void setConnectionStatus(ConnectionStatus connectionStatus) {
			this.connectionStatus = connectionStatus;
		}
	}

	public static ConnectionStatusResult testAuthentication(String schemaCode, Record record,
			ESSchemasRecordsServices esSchemasRecordsServices) {
		if (ConnectorSmbInstance.SCHEMA_CODE.equals(schemaCode)) {
			return smbConnectorAutheticationTest(record, esSchemasRecordsServices);
		} else if (ConnectorHttpInstance.SCHEMA_CODE.equals(schemaCode)) {
			return testHttpBasicAuthentification(record, esSchemasRecordsServices);
		} else if (ConnectorLDAPInstance.SCHEMA_CODE.equals(schemaCode)) {
			return new ConnectionStatusResult(testLDAPConnection(record, esSchemasRecordsServices));
		}

		return new ConnectionStatusResult(ConnectionStatus.Ok);
	}

	private static ConnectionStatusResult smbConnectorAutheticationTest(Record record,
			ESSchemasRecordsServices esSchemasRecordsServices) {
		ConnectorSmbInstance connectorSmbInstance = esSchemasRecordsServices.wrapConnectorSmbInstance(record);
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(connectorSmbInstance.getDomain(),
				connectorSmbInstance.getUsername(), connectorSmbInstance.getPassword());
		SmbFile smbFile;
		if (connectorSmbInstance.getSeeds().size() > 0) {
			try {
				smbFile = new SmbFile(connectorSmbInstance.getSeeds().get(0), auth);
				smbFile.connect();
			} catch (MalformedURLException e) {
				LOGGER.warn("SMB Connector test details : ", e);
				return new ConnectionStatusResult(ConnectionStatus.badUrlNoParam);
			} catch (IOException e) {
				LOGGER.warn("SMB Connector test details : ", e);
				return new ConnectionStatusResult(ConnectionStatus.ioError);
			}
		}

		return new ConnectionStatusResult(ConnectionStatus.Ok);
	}

	@Nullable
	private static ConnectionStatusResult testHttpBasicAuthentification(Record record,
			ESSchemasRecordsServices esSchemasRecordsServices) {
		ConnectorHttpInstance connectorHttpInstance = esSchemasRecordsServices.wrapConnectorHttpInstance(record);
		connectorHttpInstance.getPasssword();
		connectorHttpInstance.getUsername();
		int timeout = 60_000;
		if (connectorHttpInstance.getSeedsList().size() > 0
				&& connectorHttpInstance.getAuthenticationScheme() != AuthenticationScheme.NTLM) {
			try {
				ConnectorHttp
						.getHttpURLFetchingService(timeout, connectorHttpInstance)
						.fetch(connectorHttpInstance.getSeedsList().get(0));
			} catch (URLFetchingServiceRuntimeException_HttpError e) {
				if (!e.getErrorCode().equals(401 + "")) {
					return new ConnectionStatusResult(e.getErrorCode(), ConnectionStatus.httpStatusError);
				} else {
					return new ConnectionStatusResult(ConnectionStatus.authentificationFailure);
				}
			} catch (URLFetchingServiceServiceRuntimeException_ConnectionException e) {
				return new ConnectionStatusResult(e.getUrl(), ConnectionStatus.connectionError);
			} catch (URLFetchingServiceRuntimeException_MalformedUrl e) {
				return new ConnectionStatusResult(e.getUrl(), ConnectionStatus.badUrl);
			} catch (URLFetchingServiceRuntimeException_IOException e) {
				return new ConnectionStatusResult(ConnectionStatus.ioError);
			}
		}

		return new ConnectionStatusResult(ConnectionStatus.Ok);
	}

	@Nullable
	private static ConnectionStatus testLDAPConnection(Record record, ESSchemasRecordsServices esSchemasRecordsServices) {
		ConnectorLDAPInstance connectorLDAPInstance = esSchemasRecordsServices.wrapConnectorLDAPInstance(record);
		connectorLDAPInstance.getConnectionUsername();
		connectorLDAPInstance.getPassword();

		List<String> urls = connectorLDAPInstance.getUrls();
		if (urls.size() != 1) {
			return ConnectionStatus.onlyOneUrlSupported;
		}

		String url = urls.get(0);
		LdapContext ldapContext = null;
		try {
			ConnectorLDAP.connectToLDAP(url, connectorLDAPInstance, new ConnectorLDAPServicesImpl());
		} catch (LDAPConnectionFailure ldapConnectionFailure) {
			return ConnectionStatus.authentificationFailure;
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					LOGGER.warn("Error when closing context ", e);
				}
			}
		}
		return ConnectionStatus.Ok;
	}

	public static String getErrorMessage(ConnectionStatusResult connectionStatusResult) {

		ConnectionStatus connectionStatus = connectionStatusResult.connectionStatus;

		if (connectionStatus == ConnectionStatus.onlyOneUrlSupported
				|| ConnectionStatus.authentificationFailure == connectionStatus
				|| ConnectionStatus.ioError == connectionStatus
				|| ConnectionStatus.badUrlNoParam == connectionStatus) {
			return i18n.$("ConnectorUtil." + connectionStatusResult.getConnectionStatus().name());
		} else if (ConnectionStatus.httpStatusError == connectionStatus
				|| ConnectionStatus.connectionError == connectionStatus
				|| ConnectionStatus.badUrl == connectionStatus) {
			return i18n
					.$("ConnectorUtil." + connectionStatusResult.getConnectionStatus().name(), connectionStatusResult.getParam());
		}

		return "";
	}
}