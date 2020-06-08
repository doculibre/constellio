package com.constellio.app.ui.util;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.Language;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ViewErrorDisplay {
	public static final String REQUIRED_FIELD = "requiredField";

	public static boolean validateFieldsContent(Map<Language, BaseTextField> baseTextFieldMap,
												BaseViewImpl listValueDomainView) {
		return validateFieldsContent(baseTextFieldMap.values(), listValueDomainView);
	}

	public static boolean validateFieldsContent(Collection<BaseTextField> baseTextFields,
												BaseViewImpl listValueDomainView) {
		StringBuilder errorFields = new StringBuilder();
		BaseTextField firstFieldWithError = null;
		for (BaseTextField baseTextField : baseTextFields) {
			try {
				baseTextField.validate();
			} catch (Validator.InvalidValueException invalidValueException) {
				if (errorFields.length() != 0) {
					errorFields.append("<br/>");
				}
				if (invalidValueException instanceof EmptyValueException) {
					baseTextField.setRequiredError($(REQUIRED_FIELD));
					errorFields.append($("requiredFieldWithName", "\"" + baseTextField.getCaption() + "\""));
				} else {
					errorFields.append(invalidValueException.getMessage());
				}

				if (firstFieldWithError == null) {
					firstFieldWithError = baseTextField;
				}
			}
		}

		if (firstFieldWithError != null) {
			firstFieldWithError.focus();
			listValueDomainView.showErrorMessage(errorFields.toString());
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
