package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.schemas.validators.MaskedMetadataValidator;
import com.constellio.model.utils.MaskUtils;
import com.constellio.model.utils.MaskUtilsException;
import com.vaadin.data.Validator;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("unchecked")
public class ListAddRemoveTextField extends ListAddRemoveField<String, TextField> {

	private String inputMask = null;

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected TextField newAddEditField() {
		BaseTextField baseTextField = new BaseTextField();
		if (inputMask != null) {
			baseTextField.setInputMask(inputMask);
			baseTextField.addValidator(new Validator() {
				@Override
				public void validate(Object value) throws InvalidValueException {
					try {
						if (value != null) {
							MaskUtils.validate(inputMask, (String) value);
						}
					} catch (MaskUtilsException e) {
						ValidationError validationError = buildValidationError(inputMask, (String) value);
						throw new MultivalueMaskInvalidValueException($(validationError), validationError);
					}
				}
			});
		}
		return baseTextField;
	}

	@Override
	protected void tryAdd() {
		try {
			super.tryAdd();
		} catch (MultivalueMaskInvalidValueException e) {
			showErrorMessage($(e.getValidationError()));
		}
	}

	public void setInputMask(String inputMask) {
		this.inputMask = inputMask;
	}

	protected void showErrorMessage(String message) {
		Notification notification = new Notification(message + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	class MultivalueMaskInvalidValueException extends Validator.InvalidValueException {
		private ValidationError validationError;

		public MultivalueMaskInvalidValueException(String message, ValidationError validationError) {
			super(message);
			this.validationError = validationError;
		}

		public ValidationError getValidationError() {
			return validationError;
		}
	}

	public ValidationError buildValidationError(String mask, String value) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(MaskedMetadataValidator.METADATA_LABEL, getCaption());
		parameters.put(MaskedMetadataValidator.MASK, mask);
		parameters.put(MaskedMetadataValidator.VALUE, value);
		return new ValidationError(MaskedMetadataValidator.class, MaskedMetadataValidator.VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK, parameters);
	}
}
