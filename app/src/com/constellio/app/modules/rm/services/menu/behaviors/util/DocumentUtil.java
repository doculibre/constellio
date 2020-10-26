package com.constellio.app.modules.rm.services.menu.behaviors.util;

import com.constellio.app.ui.framework.components.content.UpdateContentVersionWindowImpl.ValidateFileName;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentUtil {

	public static ValidateFileName getEmailDocumentFileNameValidator(String schemaCode) {
		boolean isEmail = schemaCode.equals(com.constellio.app.modules.rm.wrappers.Email.SCHEMA);

		ValidateFileName validateFileName = null;

		if (isEmail) {
			validateFileName = new ValidateFileName() {
				@Override
				public boolean isValid(String fileName) {
					return !(fileName.endsWith(".msg") || fileName.endsWith(".eml"));
				}

				@Override
				public String getErrorMessage() {
					return $("Document.onlyMsgAndEmlDocumentAreAccepted");
				}
			};
		}
		return validateFileName;
	}
}
