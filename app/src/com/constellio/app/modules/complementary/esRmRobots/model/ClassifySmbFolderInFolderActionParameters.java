package com.constellio.app.modules.complementary.esRmRobots.model;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ClassifySmbFolderInFolderActionParameters extends ActionParameters {

	public static final String SCHEMA_LOCAL_CODE = "classifySmbFolderInFolder";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String IN_FOLDER = "inFolder";
	public static final String MAJOR_VERSIONS = "majorVersions";

	public ClassifySmbFolderInFolderActionParameters(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getInFolder() {
		return get(IN_FOLDER);
	}

	public ClassifySmbFolderInFolderActionParameters setInFolder(String folderId) {
		set(IN_FOLDER, folderId);
		return this;
	}

	public ClassifySmbFolderInFolderActionParameters setInFolder(Record folder) {
		set(IN_FOLDER, folder);
		return this;
	}

	public ClassifySmbFolderInFolderActionParameters setInFolder(Folder folder) {
		set(IN_FOLDER, folder);
		return this;
	}

	public boolean isMajorVersions() {
		return getBooleanWithDefaultValue(MAJOR_VERSIONS, true);
	}

	public Boolean getMajorVersions() {
		return get(MAJOR_VERSIONS);
	}

	public ClassifySmbFolderInFolderActionParameters setMajorVersions(boolean majorVersions) {
		set(MAJOR_VERSIONS, majorVersions);
		return this;
	}

	public static ClassifySmbFolderInFolderActionParameters wrap(ActionParameters actionParameters) {
		return new ClassifySmbFolderInFolderActionParameters(
				actionParameters.getWrappedRecord(), actionParameters.getMetadataSchemaTypes());
	}

}
