package com.constellio.app.modules.complementary.esRmRobots.model;

import org.joda.time.LocalDate;

import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ClassifyConnectorFolderInParentFolderActionParameters extends ActionParameters
		implements ClassifyConnectorFolderActionParameters {

	public static final String SCHEMA_LOCAL_CODE = "classifyConnectorFolderInParentFolder";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String ACTION_AFTER_CLASSIFICATION = "actionAfterClassification";
	public static final String FOLDER_MAPPING = "folderMapping";
	public static final String DOCUMENT_MAPPING = "documentMapping";
	public static final String DEFAULT_PARENT_FOLDER = "defaultParentFolder";
	public static final String DEFAULT_OPEN_DATE = "defaultOpenDate";

	public ClassifyConnectorFolderInParentFolderActionParameters(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getInTaxonomy() {
		return null;
	}

	public ActionAfterClassification getActionAfterClassification() {
		return get(ACTION_AFTER_CLASSIFICATION);
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setActionAfterClassification(
			ActionAfterClassification actionAfterClassification) {
		set(ACTION_AFTER_CLASSIFICATION, actionAfterClassification);
		return this;
	}

	public String getDelimiter() {
		return null;
	}

	public Content getFolderMapping() {
		return get(FOLDER_MAPPING);
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setFolderMapping(Content folderMapping) {
		set(FOLDER_MAPPING, folderMapping);
		return this;
	}

	public Content getDocumentMapping() {
		return get(DOCUMENT_MAPPING);
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setDocumentMapping(Content documentMapping) {
		set(DOCUMENT_MAPPING, documentMapping);
		return this;
	}

	public String getDefaultAdminUnit() {
		return null;
	}

	@Override
	public String getDefaultUniformSubdivision() {
		return null;
	}

	public String getDefaultParentFolder() {
		return get(DEFAULT_PARENT_FOLDER);
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setDefaultParentFolder(String defaultParentFolder) {
		set(DEFAULT_PARENT_FOLDER, defaultParentFolder);
		return this;
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setDefaultParentFolder(Record defaultParentFolder) {
		set(DEFAULT_PARENT_FOLDER, defaultParentFolder);
		return this;
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setDefaultParentFolder(Folder defaultParentFolder) {
		set(DEFAULT_PARENT_FOLDER, defaultParentFolder);
		return this;
	}

	public String getDefaultCategory() {
		return null;
	}

	public LocalDate getDefaultOpenDate() {
		return get(DEFAULT_OPEN_DATE);
	}

	public ClassifyConnectorFolderInParentFolderActionParameters setDefaultOpenDate(LocalDate defaultOpenDate) {
		set(DEFAULT_OPEN_DATE, defaultOpenDate);
		return this;
	}

	public String getDefaultRetentionRule() {
		return null;
	}

	public CopyType getDefaultCopyStatus() {
		return null;
	}

	public String getPathPrefix() {
		return null;
	}

	public static ClassifyConnectorFolderInParentFolderActionParameters wrap(ActionParameters actionParameters) {
		return new ClassifyConnectorFolderInParentFolderActionParameters(
				actionParameters.getWrappedRecord(), actionParameters.getMetadataSchemaTypes());
	}

}
