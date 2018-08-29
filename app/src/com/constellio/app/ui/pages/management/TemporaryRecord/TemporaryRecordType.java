package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;

public enum TemporaryRecordType {
	EXPORT(ExportAudit.SCHEMA), IMPORT(ImportAudit.SCHEMA), SIP_ARCHIVES(SIParchive.SCHEMA);

	private String schema;

	TemporaryRecordType(String schema) {
		this.schema = schema;
	}

	String getSchema() {
		return this.schema;
	}

	static TemporaryRecordType getFromSchema(String schema) {
		for (TemporaryRecordType temporaryRecordType : TemporaryRecordType.values()) {
			if (temporaryRecordType.getSchema().equals(schema)) {
				return temporaryRecordType;
			}
		}
		return null;
	}
}
