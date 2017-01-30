package com.constellio.app.modules.complementary.esRmRobots.model;

import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
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

public class ClassifyConnectorFolderInTaxonomyActionParameters extends ActionParameters
		implements ClassifyConnectorFolderActionParameters {

	public static final String SCHEMA_LOCAL_CODE = "classifyConnectorTaxonomy";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String IN_TAXONOMY = "inTaxonomy";
	public static final String ACTION_AFTER_CLASSIFICATION = "actionAfterClassification";
	public static final String DELIMITER = "delimiter";
	public static final String FOLDER_MAPPING = "folderMapping";
	public static final String DOCUMENT_MAPPING = "documentMapping";
	public static final String DEFAULT_ADMIN_UNIT = "defaultAdminUnit";
	public static final String DEFAULT_UNIFORM_SUBDIVISION = "defaultUniformSubdivision";
	public static final String DEFAULT_PARENT_FOLDER = "defaultParentFolder";
	public static final String DEFAULT_CATEGORY = "defaultCategory";
	public static final String DEFAULT_RETENTION_RULE = "defaultRetentionRule";
	public static final String DEFAULT_OPEN_DATE = "defaultOpenDate";
	public static final String DEFAULT_COPY_STATUS = "defaultCopyStatus";
	public static final String PATH_PREFIX = "pathPrefix";

	public ClassifyConnectorFolderInTaxonomyActionParameters(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getInTaxonomy() {
		return get(IN_TAXONOMY);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setInTaxonomy(String taxonomyCode) {
		set(IN_TAXONOMY, taxonomyCode);
		return this;
	}

	public ActionAfterClassification getActionAfterClassification() {
		return get(ACTION_AFTER_CLASSIFICATION);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setActionAfterClassification(
			ActionAfterClassification actionAfterClassification) {
		set(ACTION_AFTER_CLASSIFICATION, actionAfterClassification);
		return this;
	}

	public String getDelimiter() {
		return get(DELIMITER);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDelimiter(String delimiter) {
		set(DELIMITER, delimiter);
		return this;
	}

	public Content getFolderMapping() {
		return get(FOLDER_MAPPING);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setFolderMapping(Content folderMapping) {
		set(FOLDER_MAPPING, folderMapping);
		return this;
	}

	public Content getDocumentMapping() {
		return get(DOCUMENT_MAPPING);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDocumentMapping(Content documentMapping) {
		set(DOCUMENT_MAPPING, documentMapping);
		return this;
	}

	public String getDefaultAdminUnit() {
		return get(DEFAULT_ADMIN_UNIT);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultAdminUnit(String defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultUniformSubdivision(String defaultUniformSubdivision) {
		set(DEFAULT_UNIFORM_SUBDIVISION, defaultUniformSubdivision);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultUniformSubdivision(UniformSubdivision defaultUniformSubdivision) {
		set(DEFAULT_UNIFORM_SUBDIVISION, defaultUniformSubdivision);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultUniformSubdivision(Record defaultUniformSubdivision) {
		set(DEFAULT_UNIFORM_SUBDIVISION, defaultUniformSubdivision);
		return this;
	}

	public String getDefaultUniformSubdivision() {
		return get(DEFAULT_UNIFORM_SUBDIVISION);
	}

	public String getDefaultParentFolder() {
		return get(DEFAULT_PARENT_FOLDER);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultParentFolder(String defaultParentFolder) {
		set(DEFAULT_PARENT_FOLDER, defaultParentFolder);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultAdminUnit(AdministrativeUnit defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultAdminUnit(Record defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public String getDefaultCategory() {
		return get(DEFAULT_CATEGORY);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultCategory(String defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultCategory(Category defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultCategory(Record defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public LocalDate getDefaultOpenDate() {
		return get(DEFAULT_OPEN_DATE);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultOpenDate(LocalDate defaultOpenDate) {
		set(DEFAULT_OPEN_DATE, defaultOpenDate);
		return this;
	}

	public String getDefaultRetentionRule() {
		return get(DEFAULT_RETENTION_RULE);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultRetentionRule(String defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultRetentionRule(RetentionRule defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultRetentionRule(Record defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public CopyType getDefaultCopyStatus() {
		return get(DEFAULT_COPY_STATUS);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setDefaultCopyStatus(CopyType defaultCopyStatus) {
		set(DEFAULT_COPY_STATUS, defaultCopyStatus);
		return this;
	}

	public String getPathPrefix() {
		return get(PATH_PREFIX);
	}

	public ClassifyConnectorFolderInTaxonomyActionParameters setPathPrefix(String pathPrefix) {
		set(PATH_PREFIX, pathPrefix);
		return this;
	}

	public static ClassifyConnectorFolderInTaxonomyActionParameters wrap(ActionParameters actionParameters) {
		return new ClassifyConnectorFolderInTaxonomyActionParameters(
				actionParameters.getWrappedRecord(), actionParameters.getMetadataSchemaTypes());
	}

}
