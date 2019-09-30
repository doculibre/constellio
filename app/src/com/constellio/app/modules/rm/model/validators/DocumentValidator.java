package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.List;

public class DocumentValidator implements RecordValidator {

	public static final String TYPE_MUST_BE_RELATED_TO_ITS_FOLDER = "typeMustBeRelatedToItsFolder";
	public static final String TYPE_MUST_BE_RELATED_TO_ITS_RULE = "typeMustBeRelatedToItsRule";

	@Override
	public void validate(RecordValidatorParams params) {
		Document document = new Document(params.getValidatedRecord(), params.getTypes());
		validate(document, params);
	}

	private void validate(Document document, RecordValidatorParams params) {
		if (document.getFolder() != null) {
			Folder folder = Folder.wrap(params.getRecord(document.getFolder()), params.getTypes());
			List<String> allowedDocumentTypes = folder.getAllowedDocumentTypes();

			if (allowedDocumentTypes.isEmpty()){
				DocumentsTypeChoice choice = params.getConfigProvider().get(RMConfigs.DOCUMENTS_TYPES_CHOICE);
				if (choice == DocumentsTypeChoice.FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES
						|| choice == DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES)
				{
					if (folder.getRetentionRule() != null){
						RetentionRule retentionRule = RetentionRule.wrap(params.getRecord(folder.getRetentionRule()), params.getTypes());
						if (!retentionRule.getDocumentTypes().contains(document.getType())){
							params.getValidationErrors().add(DocumentValidator.class, TYPE_MUST_BE_RELATED_TO_ITS_RULE);
						}
					}
				}
			}else if (!allowedDocumentTypes.contains(document.getType())){
				params.getValidationErrors().add(DocumentValidator.class, TYPE_MUST_BE_RELATED_TO_ITS_FOLDER);
			}
		}
	}
}
