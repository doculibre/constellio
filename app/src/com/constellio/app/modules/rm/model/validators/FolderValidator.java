package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.modules.rm.model.enums.CopyType.SECONDARY;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class FolderValidator implements RecordValidator {

	public static final String FOLDER_CATEGORY_MUST_BE_RELATED_TO_ITS_RULE = "folderCategoryMustBeRelatedToItsRule";
	public static final String FOLDER_UNIFORM_SUBDIVISION_MUST_BE_RELATED_TO_ITS_RULE = "folderUniformSubdivisionMustBeRelatedToItsRule";
	public static final String FOLDER_OPENING_DATE_GREATER_THAN_CLOSING_DATE = "folderOpeningDataGreaterThanClosingDate";
	public static final String FOLDER_INVALID_COPY_RETENTION_RULE = "folderInvalidCopyRetentionRule";
	public static final String COPY_RETENTION_RULE_COPY_TYPE_MUST_BE_PRINCIPAL = "copyRetentionRuleCopyTypeMustBePrincipal";
	public static final String COPY_RETENTION_RULE_COPY_TYPE_MUST_BE_SECONDARY = "copyRetentionRuleCopyTypeMustBeSecondary";
	public static final String ALLOWED_DOCUMENT_TYPE_MUST_BE_RELATED_TO_ITS_RULE = "allowedDocumentTypeMustBeRelatedToItsRule";
	public static final String DOCUMENT_INSIDE_FOLDER_MUST_RESPECT_ALLOWED_DOCUMENT_TYPE = "documentInsideFolderMustRespectAllowedDocumentType";
	public static final String SUB_FOLDER_MUST_RESPECT_ALLOWED_FOLDER_TYPE = "subFolderMustRespectAllowedFolderType";
	public static final String TYPE_MUST_RESPECT_PARENT_ALLOWED_FOLDER_TYPE = "typeMustRespectParentAllowedFolderType";

	public static final String RULE_CODE = "ruleCode";
	public static final String CATEGORY_CODE = "categoryCode";
	public static final String UNIFORM_SUBDIVISION = "categoryCode";
	public static final String OPENING_DATE = "openingDate";
	public static final String CLOSING_DATE = "closingDate";
	public static final String MAIN_COPY_RULE = "mainCopyRule";
	public static final String ALLOWED_DOCUMENT_TYPES = "allowedDocumentTypes";
	public static final String ALLOWED_FOLDER_TYPES = "allowedFolderTypes";
	public static final String DOCUMENT_TYPES = "documentTypes";
	public static final String FOLDER_TYPE = "folderType";

	@Override
	public void validate(RecordValidatorParams params) {
		Folder folder = new Folder(params.getValidatedRecord(), params.getTypes());
		validate(folder, params);
	}

	private void validate(Folder folder, RecordValidatorParams params) {
		RetentionRule retentionRule = null;
		if (folder.getRetentionRule() != null) {
			retentionRule = RetentionRule.wrap(params.getRecord(folder.getRetentionRule()), params.getTypes());
		}
		boolean hasParentFolder = folder.getParentFolder() != null;
		String uniformSubdivisionId = folder.getUniformSubdivision();
		Boolean areUniformSubdivisionEnabled = (Boolean) params.getConfigProvider().get(RMConfigs.UNIFORM_SUBDIVISION_ENABLED);
		String mainCopyRuleIdEntered = folder.getMainCopyRuleIdEntered();

		if (areUniformSubdivisionEnabled && uniformSubdivisionId != null && retentionRule != null) {
			UniformSubdivision uniformSubdivision = new UniformSubdivision(params.getRecord(uniformSubdivisionId),
					params.getTypes());
			if (uniformSubdivision.getRetentionRules() == null || !uniformSubdivision.getRetentionRules()
					.contains(retentionRule.getId())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(RULE_CODE, retentionRule.getCode());
				parameters.put(UNIFORM_SUBDIVISION, uniformSubdivision.getCode());

				params.getValidationErrors()
						.add(FolderValidator.class, FOLDER_UNIFORM_SUBDIVISION_MUST_BE_RELATED_TO_ITS_RULE, parameters);
			}
		} else if (params.getConfigProvider().<Boolean>get(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER)
				   && folder.getCategory() != null) {
			Category category = Category.wrap(params.getRecord(folder.getCategory()), params.getTypes());
			if (!category.getRententionRules().contains(retentionRule.getId())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(RULE_CODE, retentionRule.getCode());
				parameters.put(CATEGORY_CODE, category.getCode());

				params.getValidationErrors().add(FolderValidator.class, FOLDER_CATEGORY_MUST_BE_RELATED_TO_ITS_RULE, parameters);
			}
		}
		if (folder.getCloseDateEntered() != null && folder.getOpeningDate() != null
			&& folder.getOpeningDate().compareTo(folder.getCloseDateEntered()) > 0) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(OPENING_DATE, folder.getOpeningDate());
			parameters.put(CLOSING_DATE, folder.getCloseDate());

			params.getValidationErrors().add(FolderValidator.class, FOLDER_OPENING_DATE_GREATER_THAN_CLOSING_DATE, parameters);
		}

		if (mainCopyRuleIdEntered != null && retentionRule != null && !hasParentFolder) {
			boolean match = false;
			for (CopyRetentionRule applicableCopyRule : folder.getApplicableCopyRules()) {
				if (mainCopyRuleIdEntered.equals(applicableCopyRule.getId())) {
					match = true;
					break;
				}
			}
			if (!match) {
				CopyRetentionRule copyRetentionRule = retentionRule.getCopyRetentionRuleWithId(mainCopyRuleIdEntered);

				Map<String, Object> parameters = new HashMap<>();
				parameters.put(MAIN_COPY_RULE, mainCopyRuleIdEntered.toString());
				parameters.put(RULE_CODE, retentionRule.getCode());

				if (copyRetentionRule != null && copyRetentionRule.getCopyType() == PRINCIPAL
					&& folder.getCopyStatus() == SECONDARY) {

					params.getValidationErrors()
							.add(FolderValidator.class, COPY_RETENTION_RULE_COPY_TYPE_MUST_BE_SECONDARY, parameters);
				} else if (copyRetentionRule != null && copyRetentionRule.getCopyType() == SECONDARY
						   && folder.getCopyStatus() == PRINCIPAL) {
					params.getValidationErrors()
							.add(FolderValidator.class, COPY_RETENTION_RULE_COPY_TYPE_MUST_BE_PRINCIPAL, parameters);

				} else {
					params.getValidationErrors().add(FolderValidator.class, FOLDER_INVALID_COPY_RETENTION_RULE, parameters);
				}
			}
		}

		if (params.getConfigProvider().<Boolean>get(RMConfigs.ENABLE_TYPE_RESTRICTION_IN_FOLDER)) {
			if (retentionRule != null) {
				DocumentsTypeChoice choice = params.getConfigProvider().get(RMConfigs.DOCUMENTS_TYPES_CHOICE);
				if (choice == DocumentsTypeChoice.FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES
					|| choice == DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES) {
					if (!retentionRule.getDocumentTypes().isEmpty() && !folder.getAllowedDocumentTypes().isEmpty() &&
						!retentionRule.getDocumentTypes().containsAll(folder.getAllowedDocumentTypes())) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put(RULE_CODE, retentionRule.getCode());
						parameters.put(ALLOWED_DOCUMENT_TYPES, retentionRule.getDocumentTypes().toString());
						parameters.put(DOCUMENT_TYPES, folder.getAllowedDocumentTypes().toString());
						params.getValidationErrors().add(FolderValidator.class, ALLOWED_DOCUMENT_TYPE_MUST_BE_RELATED_TO_ITS_RULE, parameters);
					}
				}
			}

			Folder parentFolder = null;
			if (folder.getParentFolder() != null) {
				parentFolder = Folder.wrap(params.getRecord(folder.getParentFolder()), params.getTypes());
			}

			if (parentFolder != null) {
				if (!parentFolder.getAllowedFolderTypes().isEmpty() && folder.getType() != null
					&& !parentFolder.getAllowedFolderTypes().contains(folder.getType())) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put(ALLOWED_FOLDER_TYPES, parentFolder.getAllowedFolderTypes().toString());
					parameters.put(FOLDER_TYPE, folder.getType());
					params.getValidationErrors().add(FolderValidator.class, TYPE_MUST_RESPECT_PARENT_ALLOWED_FOLDER_TYPE, parameters);
				}
			}

			if (params.getValidatedRecord().isSaved()) {
				if (!folder.getAllowedDocumentTypes().isEmpty()
					&& params.getValidatedRecord().getModifiedMetadatas(params.getTypes()).containsMetadataWithLocalCode(Folder.ALLOWED_DOCUMENT_TYPES)) {
					LogicalSearchQuery query = new LogicalSearchQuery().setQueryExecutionMethod(QueryExecutionMethod.ENSURE_INDEXED_METADATA_USED);
					MetadataSchemaType documentSchemaType = params.getTypes().getSchemaType(Document.SCHEMA_TYPE);
					query.setCondition(from(documentSchemaType)
							.where(Schemas.PATH_PARTS).isEqualTo(folder.getId())
							.andWhere(documentSchemaType.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.TYPE)).isNotIn(folder.getAllowedDocumentTypes()));
					if (params.getSearchServices().hasResults(query)) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put(ALLOWED_DOCUMENT_TYPES, folder.getAllowedDocumentTypes().toString());
						params.getValidationErrors().add(FolderValidator.class, DOCUMENT_INSIDE_FOLDER_MUST_RESPECT_ALLOWED_DOCUMENT_TYPE, parameters);
					}
				}

				if (!folder.getAllowedFolderTypes().isEmpty()
					&& params.getValidatedRecord().getModifiedMetadatas(params.getTypes()).containsMetadataWithLocalCode(Folder.ALLOWED_FOLDER_TYPES)) {
					LogicalSearchQuery query = new LogicalSearchQuery().setQueryExecutionMethod(QueryExecutionMethod.ENSURE_INDEXED_METADATA_USED);
					MetadataSchemaType folderSchemaType = params.getTypes().getSchemaType(Folder.SCHEMA_TYPE);
					query.setCondition(from(folderSchemaType)
							.where(Schemas.PATH_PARTS).isEqualTo(folder.getId())
							.andWhere(folderSchemaType.getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.TYPE)).isNotIn(folder.getAllowedFolderTypes()));
					if (params.getSearchServices().hasResults(query)) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put(ALLOWED_FOLDER_TYPES, folder.getAllowedFolderTypes().toString());
						params.getValidationErrors().add(FolderValidator.class, SUB_FOLDER_MUST_RESPECT_ALLOWED_FOLDER_TYPE, parameters);
					}
				}
			}
		}
	}

	private String formatToParameter(Object parameter) {
		if (parameter == null) {
			return "";
		}
		return parameter.toString();
	}

	private String formatToParameter(Object parameter, String suffix) {
		if (parameter == null) {
			return formatToParameter(parameter);
		} else {
			return formatToParameter(parameter) + suffix;
		}
	}
}
