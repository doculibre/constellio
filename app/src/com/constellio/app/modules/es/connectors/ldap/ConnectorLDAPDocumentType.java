package com.constellio.app.modules.es.connectors.ldap;

public enum ConnectorLDAPDocumentType {
	USER("person", "user"), COMPUTER("computer", ""), GROUP("group", "");
	String objectClass;
	String objectCategory;

	ConnectorLDAPDocumentType(String objectCategory, String objectClass) {
		this.objectCategory = objectCategory;
		this.objectClass = objectClass;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public String getObjectCategory() {
		return objectCategory;
	}
}
