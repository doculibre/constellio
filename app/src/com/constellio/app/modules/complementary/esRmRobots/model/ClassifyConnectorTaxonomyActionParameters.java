package com.constellio.app.modules.complementary.esRmRobots.model;

import org.joda.time.LocalDate;

import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ClassifyConnectorTaxonomyActionParameters extends ActionParameters {

	public static final String SCHEMA_LOCAL_CODE = "classifyConnectorTaxonomy";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String IN_TAXONOMY = "inTaxonomy";
	public static final String ACTION_AFTER_CLASSIFICATION = "actionAfterClassification";
	public static final String DELIMITER = "delimiter";
	public static final String FOLDER_MAPPING = "folderMapping";
	public static final String DOCUMENT_MAPPING = "documentMapping";
	public static final String DEFAULT_ADMIN_UNIT = "defaultAdminUnit";
	public static final String DEFAULT_PARENT_FOLDER = "defaultParentFolder";
	public static final String DEFAULT_CATEGORY = "defaultCategory";
	public static final String DEFAULT_RETENTION_RULE = "defaultRetentionRule";
	public static final String DEFAULT_OPEN_DATE = "defaultOpenDate";
	public static final String DEFAULT_COPY_STATUS = "defaultCopyStatus";
	public static final String PATH_PREFIX = "pathPrefix";

	public ClassifyConnectorTaxonomyActionParameters(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getInTaxonomy() {
		return get(IN_TAXONOMY);
	}

	public ClassifyConnectorTaxonomyActionParameters setInTaxonomy(String taxonomyCode) {
		set(IN_TAXONOMY, taxonomyCode);
		return this;
	}

	public ActionAfterClassification getActionAfterClassification() {
		return get(ACTION_AFTER_CLASSIFICATION);
	}

	public ClassifyConnectorTaxonomyActionParameters setActionAfterClassification(
			ActionAfterClassification actionAfterClassification) {
		set(ACTION_AFTER_CLASSIFICATION, actionAfterClassification);
		return this;
	}

	public String getDelimiter() {
		return get(DELIMITER);
	}

	public ClassifyConnectorTaxonomyActionParameters setDelimiter(String delimiter) {
		set(DELIMITER, delimiter);
		return this;
	}

	public Content getFolderMapping() {
		return get(FOLDER_MAPPING);
	}

	public ClassifyConnectorTaxonomyActionParameters setFolderMapping(Content folderMapping) {
		set(FOLDER_MAPPING, folderMapping);
		return this;
	}

	public Content getDocumentMapping() {
		return get(DOCUMENT_MAPPING);
	}

	public ClassifyConnectorTaxonomyActionParameters setDocumentMapping(Content documentMapping) {
		set(DOCUMENT_MAPPING, documentMapping);
		return this;
	}

	public String getDefaultAdminUnit() {
		return get(DEFAULT_ADMIN_UNIT);
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultAdminUnit(String defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public String getDefaultParentFolder() {
		return get(DEFAULT_PARENT_FOLDER);
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultParentFolder(String defaultParentFolder) {
		set(DEFAULT_PARENT_FOLDER, defaultParentFolder);
		return this;
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultAdminUnit(AdministrativeUnit defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultAdminUnit(Record defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public String getDefaultCategory() {
		return get(DEFAULT_CATEGORY);
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultCategory(String defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultCategory(Category defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultCategory(Record defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public LocalDate getDefaultOpenDate() {
		return get(DEFAULT_OPEN_DATE);
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultOpenDate(LocalDate defaultOpenDate) {
		set(DEFAULT_OPEN_DATE, defaultOpenDate);
		return this;
	}

	public String getDefaultRetentionRule() {
		return get(DEFAULT_RETENTION_RULE);
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultRetentionRule(String defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultRetentionRule(RetentionRule defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultRetentionRule(Record defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public CopyType getDefaultCopyStatus() {
		return get(DEFAULT_COPY_STATUS);
	}

	public ClassifyConnectorTaxonomyActionParameters setDefaultCopyStatus(CopyType defaultCopyStatus) {
		set(DEFAULT_COPY_STATUS, defaultCopyStatus);
		return this;
	}

	public String getPathPrefix() {
		return get(PATH_PREFIX);
	}

	public ClassifyConnectorTaxonomyActionParameters setPathPrefix(String pathPrefix) {
		set(PATH_PREFIX, pathPrefix);
		return this;
	}

	public static ClassifyConnectorTaxonomyActionParameters wrap(ActionParameters actionParameters) {
		return new ClassifyConnectorTaxonomyActionParameters(
				actionParameters.getWrappedRecord(), actionParameters.getMetadataSchemaTypes());
	}

}
