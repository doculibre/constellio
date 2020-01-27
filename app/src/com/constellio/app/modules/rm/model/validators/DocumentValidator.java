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
			Folder folder = Folder.wrap(params.getRecordSummary(document.getFolder()), params.getTypes());
			List<String> allowedDocumentTypes = folder.getAllowedDocumentTypes();

			String documentType = document.getType();
			if (!params.getConfigProvider().<Boolean>get(RMConfigs.ENABLE_TYPE_RESTRICTION_IN_FOLDER)
				|| allowedDocumentTypes.isEmpty()) {
				DocumentsTypeChoice choice = params.getConfigProvider().get(RMConfigs.DOCUMENTS_TYPES_CHOICE);
				if (folder.getRetentionRule() != null && (choice == DocumentsTypeChoice.FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES
														  || choice == DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES)) {
					RetentionRule retentionRule = RetentionRule.wrap(params.getRecord(folder.getRetentionRule()), params.getTypes());
					List<String> retentionRuleDocumentTypes = retentionRule.getDocumentTypes();
					if (!retentionRuleDocumentTypes.isEmpty() && documentType != null && !retentionRuleDocumentTypes.contains(documentType)) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put(RULE_CODE, retentionRule.getCode());
						parameters.put(ALLOWED_DOCUMENT_TYPES, retentionRuleDocumentTypes);
						parameters.put(DOCUMENT_TYPE, documentType);

						// TODO: Nous avons choisi de retirer cette validation pour le moment puisqu'elle pourrait causer des problème sur des environement de client.
						//  Comme c'est une nouvelle validation, il se peut que les environements existants ne la respecte pas et le fait de l'ajouter ferait planter l'environement.
						//  Dans le futur, nous prévoyons ajouter cette validation comme un warning au lieu d'une erreur.
						//params.getValidationErrors().add(DocumentValidator.class, TYPE_MUST_BE_RELATED_TO_ITS_RULE, parameters);
					}
				}
			} else if (documentType != null && !allowedDocumentTypes.isEmpty() && !allowedDocumentTypes.contains(documentType)) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(ALLOWED_DOCUMENT_TYPES, allowedDocumentTypes.toString());
				parameters.put(DOCUMENT_TYPE, documentType);
				params.getValidationErrors().add(DocumentValidator.class, TYPE_MUST_BE_RELATED_TO_ITS_FOLDER, parameters);
			}
		}
	}
}
