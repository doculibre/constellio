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

public class ClassifyConnectorFolderDirectlyInThePlanActionParameters extends ActionParameters
		implements ClassifyConnectorFolderActionParameters {

	public static final String SCHEMA_LOCAL_CODE = "classifyConnectorFolderDirectlyInThePlan";
	public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String ACTION_AFTER_CLASSIFICATION = "actionAfterClassification";
	public static final String FOLDER_MAPPING = "folderMapping";
	public static final String DOCUMENT_MAPPING = "documentMapping";
	public static final String DEFAULT_ADMIN_UNIT = "defaultAdminUnit";
	public static final String DEFAULT_UNIFORM_SUBDIVISION = "defaultUniformSubdivision";
	public static final String DEFAULT_CATEGORY = "defaultCategory";
	public static final String DEFAULT_RETENTION_RULE = "defaultRetentionRule";
	public static final String DEFAULT_OPEN_DATE = "defaultOpenDate";
	public static final String DEFAULT_COPY_STATUS = "defaultCopyStatus";

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getInTaxonomy() {
		return null;
	}

	public ActionAfterClassification getActionAfterClassification() {
		return get(ACTION_AFTER_CLASSIFICATION);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setActionAfterClassification(
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

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setFolderMapping(Content folderMapping) {
		set(FOLDER_MAPPING, folderMapping);
		return this;
	}

	public Content getDocumentMapping() {
		return get(DOCUMENT_MAPPING);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDocumentMapping(Content documentMapping) {
		set(DOCUMENT_MAPPING, documentMapping);
		return this;
	}

	public String getDefaultAdminUnit() {
		return get(DEFAULT_ADMIN_UNIT);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultAdminUnit(String defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public String getDefaultParentFolder() {
		return null;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultAdminUnit(AdministrativeUnit defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultAdminUnit(Record defaultAdminUnit) {
		set(DEFAULT_ADMIN_UNIT, defaultAdminUnit);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultUniformSubdivision(String defaultUniformSubdivision) {
		set(DEFAULT_UNIFORM_SUBDIVISION, defaultUniformSubdivision);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultUniformSubdivision(UniformSubdivision defaultUniformSubdivision) {
		set(DEFAULT_UNIFORM_SUBDIVISION, defaultUniformSubdivision);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultUniformSubdivision(Record defaultUniformSubdivision) {
		set(DEFAULT_UNIFORM_SUBDIVISION, defaultUniformSubdivision);
		return this;
	}

	public String getDefaultUniformSubdivision() {
		return get(DEFAULT_UNIFORM_SUBDIVISION);
	}

	public String getDefaultCategory() {
		return get(DEFAULT_CATEGORY);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultCategory(String defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultCategory(Category defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultCategory(Record defaultCategory) {
		set(DEFAULT_CATEGORY, defaultCategory);
		return this;
	}

	public LocalDate getDefaultOpenDate() {
		return get(DEFAULT_OPEN_DATE);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultOpenDate(LocalDate defaultOpenDate) {
		set(DEFAULT_OPEN_DATE, defaultOpenDate);
		return this;
	}

	public String getDefaultRetentionRule() {
		return get(DEFAULT_RETENTION_RULE);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultRetentionRule(String defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultRetentionRule(RetentionRule defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultRetentionRule(Record defaultRetentionRule) {
		set(DEFAULT_RETENTION_RULE, defaultRetentionRule);
		return this;
	}

	public CopyType getDefaultCopyStatus() {
		return get(DEFAULT_COPY_STATUS);
	}

	public ClassifyConnectorFolderDirectlyInThePlanActionParameters setDefaultCopyStatus(CopyType defaultCopyStatus) {
		set(DEFAULT_COPY_STATUS, defaultCopyStatus);
		return this;
	}

	public String getPathPrefix() {
		return null;
	}

	public static ClassifyConnectorFolderDirectlyInThePlanActionParameters wrap(ActionParameters actionParameters) {
		return new ClassifyConnectorFolderDirectlyInThePlanActionParameters(
				actionParameters.getWrappedRecord(), actionParameters.getMetadataSchemaTypes());
	}

}
