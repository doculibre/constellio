package com.constellio.app.modules.complementary.esRmRobots.model;

import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ClassifyConnectorDocumentInFolderActionParameters extends ActionParameters {

	// TODO Eventually remove SMB in local code, since action is now generic
	public static final String SCHEMA_LOCAL_CODE = "classifySmbDocumentInFolder";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String IN_FOLDER = "inFolder";
	public static final String MAJOR_VERSIONS = "majorVersions";
	public static final String VERSIONS = "versions";
	public static final String ACTION_AFTER_CLASSIFICATION = "actionAfterClassification";

	public ClassifyConnectorDocumentInFolderActionParameters(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getInFolder() {
		return get(IN_FOLDER);
	}

	public ClassifyConnectorDocumentInFolderActionParameters setInFolder(String folderId) {
		set(IN_FOLDER, folderId);
		return this;
	}

	public ClassifyConnectorDocumentInFolderActionParameters setInFolder(Record folder) {
		set(IN_FOLDER, folder);
		return this;
	}

	public ClassifyConnectorDocumentInFolderActionParameters setInFolder(Folder folder) {
		set(IN_FOLDER, folder);
		return this;
	}

	public boolean isMajorVersions() {
		return getBooleanWithDefaultValue(MAJOR_VERSIONS, true);
	}

	public Boolean getMajorVersions() {
		return get(MAJOR_VERSIONS);
	}
	
	public ClassifyConnectorDocumentInFolderActionParameters setVersions(String versions) {
		set(VERSIONS,versions);
		return this;
	}
	
	public String getVersions() {
		return get(VERSIONS);
	}

	public ClassifyConnectorDocumentInFolderActionParameters setMajorVersions(boolean majorVersions) {
		set(MAJOR_VERSIONS, majorVersions);
		return this;
	}

	public static ClassifyConnectorDocumentInFolderActionParameters wrap(ActionParameters actionParameters) {
		return new ClassifyConnectorDocumentInFolderActionParameters(
				actionParameters.getWrappedRecord(), actionParameters.getMetadataSchemaTypes());
	}

	public ActionAfterClassification getActionAfterClassification() {
		return get(ACTION_AFTER_CLASSIFICATION);
	}

	public ClassifyConnectorDocumentInFolderActionParameters setActionAfterClassification(
			ActionAfterClassification actionAfterClassification) {
		set(ACTION_AFTER_CLASSIFICATION, actionAfterClassification);
		return this;
	}
}
