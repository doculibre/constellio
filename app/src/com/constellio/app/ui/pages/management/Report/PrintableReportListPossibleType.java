package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;

//TODO KILL ME Gabriel Delete class and replace with simple Folder.schemaType, etc.

public enum PrintableReportListPossibleType {
	FOLDER(Folder.SCHEMA_TYPE), DOCUMENT(Document.SCHEMA_TYPE), TASK(Task.SCHEMA_TYPE), CONTAINER(ContainerRecord.SCHEMA_TYPE),
	STORAGE_SPACE(StorageSpace.SCHEMA_TYPE), CONNECTOR_HTTP_DOCUMENT(ConnectorHttpDocument.SCHEMA_TYPE),
	CONNECTOR_LDAP_USER_DOCUMENT(ConnectorLDAPUserDocument.SCHEMA_TYPE), CONNECTOR_SMB_DOCUMENT(ConnectorSmbDocument.SCHEMA_TYPE);


	private final String schemaType;

	PrintableReportListPossibleType(String schemaType) {
		this.schemaType = schemaType;
	}

	public static PrintableReportListPossibleType getValue(String value) {
		for (PrintableReportListPossibleType e : PrintableReportListPossibleType.values()) {
			if (e.name().equals(value)) {
				return e;
			}
		}
		return null;// not found
	}

	public static PrintableReportListPossibleType getValueFromSchemaType(String value) {
		for (PrintableReportListPossibleType e : PrintableReportListPossibleType.values()) {
			if (e.getSchemaType().toLowerCase().equals(value.toLowerCase())) {
				return e;
			}
		}
		return null;// not found
	}

	public String getSchemaType() {
		return schemaType;
	}

	public String getSchemaTypeUpperCase() {
		return schemaType.toUpperCase();
	}
}
