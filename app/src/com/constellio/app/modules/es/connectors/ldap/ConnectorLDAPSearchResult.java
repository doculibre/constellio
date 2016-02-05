package com.constellio.app.modules.es.connectors.ldap;

import java.util.Set;

public class ConnectorLDAPSearchResult {
	private Set<String> documentIds;
	private boolean errorDuringSearch = false;

	public Set<String> getDocumentIds() {
		return documentIds;
	}

	public ConnectorLDAPSearchResult setDocumentIds(Set<String> documentIds) {
		this.documentIds = documentIds;
		return this;
	}

	public boolean isErrorDuringSearch() {
		return errorDuringSearch;
	}

	public ConnectorLDAPSearchResult setErrorDuringSearch(boolean errorDuringSearch) {
		this.errorDuringSearch = errorDuringSearch;
		return this;
	}
}
