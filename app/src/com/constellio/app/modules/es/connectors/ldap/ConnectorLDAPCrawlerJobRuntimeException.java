package com.constellio.app.modules.es.connectors.ldap;

public class ConnectorLDAPCrawlerJobRuntimeException extends RuntimeException {
	public ConnectorLDAPCrawlerJobRuntimeException(Exception e) {
		super(e);
	}

	public static class ConnectorLDAPCrawlerJobRuntimeException_LDAPCloseExceptionJob
			extends ConnectorLDAPCrawlerJobRuntimeException {
		public ConnectorLDAPCrawlerJobRuntimeException_LDAPCloseExceptionJob(Exception e) {
			super(e);
		}
	}
}
