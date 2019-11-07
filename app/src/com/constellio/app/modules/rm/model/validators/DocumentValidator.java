package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentValidator implements RecordValidator {

	public static final String TYPE_MUST_BE_RELATED_TO_ITS_FOLDER = "typeMustBeRelatedToItsFolder";
	public static final String TYPE_MUST_BE_RELATED_TO_ITS_RULE = "typeMustBeRelatedToItsRule";

	public static final String RULE_CODE = "ruleCode";
	public static final String ALLOWED_DOCUMENT_TYPES = "allowedDocumentTypes";
	public static final String DOCUMENT_TYPE = "documentType";

	@Override
	public void validate(RecordValidatorParams params) {
		Document document = new Document(params.getValidatedRecord(), params.getTypes());
		validate(document, params);
	}

	private void validate(Document document, RecordValidatorParams params) {
		if (document.getFolder() != null) {
			Folder folder = Folder.wrap(params.getRecord(document.getFolder()), params.getTypes());
			List<String> allowedDocumentTypes = folder.getAllowedDocumentTypes();

			if (!params.getConfigProvider().<Boolean>get(RMConfigs.ENABLE_TYPE_RESTRICTION_IN_FOLDER)
				|| allowedDocumentTypes.isEmpty()) {
				DocumentsTypeChoice choice = params.getConfigProvider().get(RMConfigs.DOCUMENTS_TYPES_CHOICE);
				if (choice == DocumentsTypeChoice.FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES
					|| choice == DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES) {
					if (folder.getRetentionRule() != null) {
						RetentionRule retentionRule = RetentionRule.wrap(params.getRecord(folder.getRetentionRule()), params.getTypes());
						if (!retentionRule.getDocumentTypes().contains(document.getType())) {
							Map<String, Object> parameters = new HashMap<>();
							parameters.put(RULE_CODE, retentionRule.getCode());
							parameters.put(ALLOWED_DOCUMENT_TYPES, retentionRule.getDocumentTypes().toString());
							parameters.put(DOCUMENT_TYPE, document.getType());
							params.getValidationErrors().add(DocumentValidator.class, TYPE_MUST_BE_RELATED_TO_ITS_RULE, parameters);
						}
					}
				}
			} else if (!allowedDocumentTypes.contains(document.getType())) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(ALLOWED_DOCUMENT_TYPES, allowedDocumentTypes.toString());
				parameters.put(DOCUMENT_TYPE, document.getType());
				params.getValidationErrors().add(DocumentValidator.class, TYPE_MUST_BE_RELATED_TO_ITS_FOLDER, parameters);
			}
		}
	}
}
