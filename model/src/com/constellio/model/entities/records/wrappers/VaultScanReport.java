package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class VaultScanReport extends TemporaryRecord {
	public static final String SCHEMA = "vaultScanReport";
	public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;
	public static final String NUMBER_OF_DELETED_CONTENTS = "numberOfDeletedContents";
	public static final String MESSAGE = "message";

	public VaultScanReport(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}
}
