package com.constellio.app.ui.util;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.Language;
import com.vaadin.data.Validator;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ViewErrorDisplay {
	public static final String REQUIRED_FIELD = "requiredField";

	public static boolean validateFieldsContent(Map<Language, BaseTextField> baseTextFieldMap,
												BaseViewImpl listValueDomainView) {
		StringBuilder missingRequiredFields = new StringBuilder();
		BaseTextField firstFieldWithError = null;
		for (Language language : baseTextFieldMap.keySet()) {
			BaseTextField baseTextField = baseTextFieldMap.get(language);
			try {
				baseTextField.validate();
			} catch (Validator.EmptyValueException emptyValueException) {
				baseTextField.setRequiredError($(REQUIRED_FIELD));
				if (missingRequiredFields.length() != 0) {
					missingRequiredFields.append("<br/>");
				}
				missingRequiredFields.append($("requiredFieldWithName", baseTextField.getCaption()));
				if (firstFieldWithError == null) {
					firstFieldWithError = baseTextField;
				}

			}
		}

		if (firstFieldWithError != null) {
			firstFieldWithError.focus();
			listValueDomainView.showErrorMessage(missingRequiredFields.toString());
			return false;
		}

		return true;
	}

	public static void setFieldErrors(List<Language> languagesInErrors, Map<Language, BaseTextField> baseTextFieldMap,
									  String originalStyleName) {
		int i = 0;

		for (Language language : baseTextFieldMap.keySet()) {
			BaseTextField baseTextField = baseTextFieldMap.get(language);
			if (languagesInErrors.contains(language)) {
				if (i == 0) {
					baseTextField.focus();
					i++;
				}
				baseTextField.setStyleName(baseTextField.getStyleName() + " error");
			} else {
				baseTextField.setStyleName(originalStyleName);
			}
		}
	}
}
