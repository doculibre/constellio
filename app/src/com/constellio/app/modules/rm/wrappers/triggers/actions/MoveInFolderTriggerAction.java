package com.constellio.app.modules.rm.wrappers.triggers.actions;

import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class MoveInFolderTriggerAction extends TriggerAction {

	public static final String SCHEMA_LOCAL_CODE = "moveInFolder";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String FOLDER_TITLE = "folderTitle";
	public static final String FOLDER_TYPE = "folderType";

	public MoveInFolderTriggerAction(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA);
	}
}
